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
            .filterIsInstance<Finding.MethodName>()
            .forEach {
                processOverriddenMethod(result, it, classMap, childTypesMap)
            }
    }

    private fun processOverriddenMethod(
        result: MutableList<Finding>,
        finding: Finding.MethodName,
        classMap: Map<String, ClassDef>,
        childTypesMap: Map<String, Set<String>>
    ) {
        val startingClass = finding.method.parent.value
        val parentClasses = findAllParentClasses(classMap, startingClass, finding)
        val queue = mutableListOf<String>()
        queue.addAll(parentClasses)
        val processed = mutableSetOf<String>()
        while (queue.isNotEmpty()) {
            val type = queue.removeFirst()
            if (type in processed) {
                continue
            }

            processed.add(type)
            val childTypes = childTypesMap[type] ?: continue
            queue.addAll(childTypes)

            if (type == startingClass) {
                continue
            }

            val classDef = classMap[type] ?: continue
            classDef.methods
                .firstOrNull {
                    val model = it.toCutlassModel()
                    model.name == finding.method.name &&
                        model.parameterTypes == finding.method.parameterTypes &&
                        model.returnType == finding.method.returnType
                }
                ?.also {
                    result.add(finding.copy(method = it.toCutlassModel())
                        .apply { source = FINDING_SOURCE })
                }
        }
    }

    private fun findAllParentClasses(
        classMap: Map<String, ClassDef>,
        startingClass: String,
        finding: Finding.MethodName
    ): Set<String> {
        val parentTypes = mutableSetOf<String>()
        val queue = mutableListOf(startingClass)
        while (queue.isNotEmpty()) {
            val type = queue.removeFirst()
            val classDef = classMap[type] ?: continue

            classDef.superclass?.also { queue.add(it) }
            queue.addAll(classDef.interfaces)

            if (type == startingClass) {
                continue
            }

            classDef.methods
                .firstOrNull {
                    val model = it.toCutlassModel()
                    model.name == finding.method.name &&
                        model.parameterTypes == finding.method.parameterTypes &&
                        model.returnType == finding.method.returnType
                }
                ?.also {
                    parentTypes.add(type)
                }
        }
        return parentTypes
    }

    companion object {
        private const val FINDING_SOURCE = "resolved"
    }
}
