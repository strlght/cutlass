@file:Suppress("unused")

package io.strlght.cutlass.test

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.strlght.cutlass.api.Analyzer
import io.strlght.cutlass.api.DefaultTypeMapping
import io.strlght.cutlass.api.Finding
import io.strlght.cutlass.report.mapping.MappingReport
import org.jf.dexlib2.iface.ClassDef

fun Analyzer.runPipeline(vararg classes: String): List<Finding> =
    classes
        .map { it.toSmaliClassDef() }
        .toTypedArray()
        .let { runPipeline(*it) }

fun Analyzer.runPipeline(vararg classes: ClassDef): List<Finding> {
    do {
        classes.forEach { prepare(it) }
    } while (needsAnotherRound())
    return classes
        .flatMap { handle(it) }
}

fun Analyzer.assert(vararg classes: String, mapping: String) =
    classes
        .map { it.toSmaliClassDef() }
        .toTypedArray()
        .let { assert(*it, mapping = mapping) }

fun Analyzer.assert(vararg classes: ClassDef, mapping: String) {
    val typeMapping = DefaultTypeMapping()
    runPipeline(*classes)
        .forEach { typeMapping.process(it) }
    assertThat(MappingReport().render(typeMapping)).isEqualTo(mapping)
}
