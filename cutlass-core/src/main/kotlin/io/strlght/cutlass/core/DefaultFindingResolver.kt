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
        result.asSequence()
            .filter { it is Finding.MethodName || it is Finding.FieldName }
            .forEach {
                if (it is Finding.MethodName) {
                    processOverriddenMethod(result, it, classMap)
                } else if (it is Finding.FieldName) {
                    processOverriddenField(result, it, classMap)
                }
            }
    }

    private fun processOverriddenMethod(
        result: MutableList<Finding>,
        finding: Finding.MethodName,
        classMap: Map<String, ClassDef>
    ) {
        val root = finding.method.parent.value
        val queue = mutableListOf(root)
        while (queue.isNotEmpty()) {
            val type = queue.removeFirst()
            val classDef = classMap[type] ?: continue
            val method = classDef.methods
                .firstOrNull { it.toCutlassModel() == finding.method }
                ?: continue
            if (type != root) {
                result.add(finding.copy(method = method.toCutlassModel()))
            }
            classDef.superclass?.also { queue.add(it) }
            queue.addAll(classDef.interfaces)
        }
    }

    private fun processOverriddenField(
        result: MutableList<Finding>,
        finding: Finding.FieldName,
        classMap: Map<String, ClassDef>
    ) {
        val root = finding.field.parent.value
        val queue = mutableListOf(root)
        while (queue.isNotEmpty()) {
            val type = queue.removeFirst()
            val classDef = classMap[type] ?: continue
            val field = classDef.fields
                .firstOrNull { it.toCutlassModel() == finding.field }
                ?: continue
            if (type != root) {
                result.add(finding.copy(field = field.toCutlassModel()))
            }
            classDef.superclass?.also { queue.add(it) }
            queue.addAll(classDef.interfaces)
        }
    }
}
