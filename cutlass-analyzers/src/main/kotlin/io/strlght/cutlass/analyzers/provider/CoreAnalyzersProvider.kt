package io.strlght.cutlass.analyzers.provider

import io.strlght.cutlass.analyzers.EnumStaticInitAnalyzer
import io.strlght.cutlass.analyzers.FieldAnnotationAnalyzer
import io.strlght.cutlass.analyzers.IntrinsicsAnalyzer
import io.strlght.cutlass.analyzers.IntrinsicsParametersAnalyzer
import io.strlght.cutlass.analyzers.KotlinMetadataAnalyzer
import io.strlght.cutlass.analyzers.SourceMetadataAnalyzer
import io.strlght.cutlass.analyzers.ToStringAnalyzer
import io.strlght.cutlass.api.Analyzer
import io.strlght.cutlass.api.AnalyzerContext
import io.strlght.cutlass.api.AnalyzersProvider

class CoreAnalyzersProvider : AnalyzersProvider {
    override fun create(contextProvider: () -> AnalyzerContext): List<Analyzer> =
        listOf(
            EnumStaticInitAnalyzer(contextProvider()),
            FieldAnnotationAnalyzer(contextProvider()),
            IntrinsicsParametersAnalyzer(contextProvider()),
            IntrinsicsAnalyzer(contextProvider()),
            KotlinMetadataAnalyzer(contextProvider()),
            SourceMetadataAnalyzer(contextProvider()),
            ToStringAnalyzer(contextProvider()),
        )
}
