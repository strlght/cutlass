package io.strlght.cutlass.analyzers

import assertk.all
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import io.strlght.cutlass.api.DefaultAnalyzerContext
import io.strlght.cutlass.test.asSmaliClassDef
import io.strlght.cutlass.test.runPipeline
import org.junit.Test

class KotlinMetadataAnalyzerTest {
    @Test
    fun findsBasicAnnotationCandidateWithoutUsage() {
        val cls = "KotlinMetadataAnnotation.smali".asSmaliClassDef()
        val analyzer = KotlinMetadataAnalyzer(context = DefaultAnalyzerContext())
        val result = analyzer.runPipeline(cls)
        assertThat(result).all {
            isEmpty()
        }
        assertThat(analyzer.state)
            .isEqualTo(KotlinMetadataAnalyzer.State.AnalyzingCandidates)
        assertThat(analyzer.candidates)
            .isEqualTo(setOf(cls.type))
    }
}
