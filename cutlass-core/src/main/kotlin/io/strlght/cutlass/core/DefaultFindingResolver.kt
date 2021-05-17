package io.strlght.cutlass.core

import io.strlght.cutlass.api.Finding
import io.strlght.cutlass.api.FindingResolver
import io.strlght.cutlass.api.ext.toCutlassModel
import org.jf.dexlib2.iface.ClassDef

class DefaultFindingResolver : FindingResolver {
    override fun resolve(classes: List<ClassDef>, findings: List<Finding>): List<Finding> {
        val result = findings.toMutableList()

        val classMap = classes.associateBy { it.type }
        processOverridden(result, classMap)

        val usedNames = classMap.keys
        val classNamesToKeep = result
            .asSequence()
            .filterIsInstance<Finding.ClassName>()
            .groupingBy { it.type }
            .reduce { _, accumulator, element ->
                val current = accumulator.newType
                val candidate = element.newType
                if (current == candidate) {
                    accumulator
                } else {
                    element
                        .takeIf { candidate.length > current.length }
                        ?: accumulator
                }
            }
            .values
            .toMutableSet()
        val nonUniqueNames = classNamesToKeep
            .groupingBy { it.newType }
            .foldTo(
                destination = mutableMapOf(),
                initialValueSelector = { _, element -> mutableSetOf(element.type) },
                operation = { _, acc, element -> acc.apply { add(element.type) } }
            )
            .mapValues { it.value.size }
            .filterValues { it > 1 }
            .keys
        classNamesToKeep.removeAll { it.newType.value in usedNames || it.newType in nonUniqueNames }
        result.removeAll { it is Finding.ClassName && it !in classNamesToKeep }

        @Suppress("UnusedPrivateMember")
        val invalidReports = result.filter { !it.type.isClass }

        return result
    }

    private fun processOverridden(result: MutableList<Finding>, classMap: Map<String, ClassDef>) {
        val childTypesMap = mutableMapOf<String, MutableSet<String>>()
        classMap.values.forEach { classDef ->
            val type = classDef.type
            classDef.superclass?.also {
                childTypesMap.getOrPut(it) { mutableSetOf() }.add(type)
            }
            classDef.interfaces.forEach {
                childTypesMap.getOrPut(it) { mutableSetOf() }.add(type)
            }
        }
        result
            .toList() // copy the list to avoid CME
            .asSequence()
            .filter { it is Finding.MethodName || it is Finding.FieldName }
            .forEach {
                if (it is Finding.MethodName) {
                    processOverriddenMethod(result, it, classMap, childTypesMap)
                } else if (it is Finding.FieldName) {
                    processOverriddenField(result, it, classMap, childTypesMap)
                }
            }
    }

    private fun processOverriddenMethod(
        result: MutableList<Finding>,
        finding: Finding.MethodName,
        classMap: Map<String, ClassDef>,
        childTypesMap: Map<String, Set<String>>
    ) {
        processOverriddenMember(
            classMap = classMap,
            childTypesMap = childTypesMap,
            startingClass = finding.method.parent.value,
            firstMatchingMember = { classDef ->
                classDef.methods.firstOrNull {
                    val model = it.toCutlassModel()
                    model.name == finding.method.name &&
                        model.parameterTypes == finding.method.parameterTypes &&
                        model.returnType == finding.method.returnType
                }
            },
            onFound = {
                result.add(finding.copy(method = it.toCutlassModel())
                    .apply { source = FINDING_SOURCE })
            },
        )
    }

    private fun processOverriddenField(
        result: MutableList<Finding>,
        finding: Finding.FieldName,
        classMap: Map<String, ClassDef>,
        childTypesMap: MutableMap<String, MutableSet<String>>
    ) {
        processOverriddenMember(
            classMap = classMap,
            childTypesMap = childTypesMap,
            startingClass = finding.field.parent.value,
            firstMatchingMember = { classDef ->
                classDef.fields.firstOrNull {
                    val model = it.toCutlassModel()
                    model.name == finding.field.name &&
                        model.type == finding.field.type
                }
            },
            onFound = {
                result.add(finding.copy(field = it.toCutlassModel())
                    .apply { source = FINDING_SOURCE })
            },
        )
    }

    private inline fun <reified T> processOverriddenMember(
        classMap: Map<String, ClassDef>,
        childTypesMap: Map<String, Set<String>>,
        startingClass: String,
        crossinline firstMatchingMember: (ClassDef) -> T?,
        crossinline onFound: (T) -> Unit,
    ) {
        // propagate to parent classes
        visitMembers(
            startingClass,
            classMap,
            firstMatchingMember,
            onFound,
            addToQueue = { queue, classDef ->
                classDef.superclass?.also { queue.add(it) }
                queue.addAll(classDef.interfaces)
            },
        )
        // propagate to child classes
        visitMembers(
            startingClass,
            classMap,
            firstMatchingMember,
            onFound,
            addToQueue = { queue, classDef ->
                childTypesMap[classDef.type]?.also { queue.addAll(it) }
            },
        )
    }

    private inline fun <reified T> visitMembers(
        startingClass: String,
        classMap: Map<String, ClassDef>,
        crossinline firstMatchingMember: (ClassDef) -> T?,
        crossinline onFound: (T) -> Unit,
        crossinline addToQueue: (MutableList<String>, ClassDef) -> Unit,
    ) {
        val queue = mutableListOf(startingClass)
        while (queue.isNotEmpty()) {
            val type = queue.removeFirst()
            val classDef = classMap[type] ?: continue
            addToQueue(queue, classDef)
            val member = firstMatchingMember(classDef) ?: continue
            if (type != startingClass) {
                onFound(member)
            }
        }
    }

    companion object {
        private const val FINDING_SOURCE = "resolved"
    }
}
