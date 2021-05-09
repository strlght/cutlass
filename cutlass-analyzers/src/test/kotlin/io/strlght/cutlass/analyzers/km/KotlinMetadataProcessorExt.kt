package io.strlght.cutlass.analyzers.km

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.strlght.cutlass.api.AnalyzerContext
import io.strlght.cutlass.api.DefaultAnalyzerContext
import io.strlght.cutlass.api.DefaultTypeMapping
import io.strlght.cutlass.report.mapping.MappingReport
import kotlinx.metadata.KmClass
import org.jf.dexlib2.iface.ClassDef

internal fun KotlinMetadataProcessor.assert(
    classDef: ClassDef,
    kmClass: KmClass,
    mapping: String,
    context: AnalyzerContext = DefaultAnalyzerContext(),
) {
    process(classDef, kmClass, context)

    val classPool = DefaultTypeMapping()
    context.results.forEach { classPool.process(it) }
    assertThat(MappingReport().render(classPool)).isEqualTo(mapping)
}
