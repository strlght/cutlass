package io.strlght.cutlass.core

import io.strlght.cutlass.api.Finding
import io.strlght.cutlass.api.FindingResolver
import org.jf.dexlib2.iface.ClassDef

class DefaultFindingResolver : FindingResolver {
    override fun resolve(classes: List<ClassDef>, findings: List<Finding>): List<Finding> {
        val result = findings.toMutableList()
        val usedNames = classes.usedTypes
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

    private val List<ClassDef>.usedTypes: Set<String>
        get() = asSequence().map { it.type }.toSet()
}
