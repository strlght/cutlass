package io.strlght.cutlass.analyzers

import io.strlght.cutlass.test.AnalyzerRule
import io.strlght.cutlass.test.assert
import org.junit.Rule
import org.junit.Test

class SourceMetadataAnalyzerTest {
    @get:Rule
    val analyzerRule = AnalyzerRule(SourceMetadataAnalyzer::class.java)

    @Test
    fun reportsNameForBasicClass() {
        analyzerRule.analyzer.assert(
            """
            .class public final Lsample/a;
            .super Ljava/lang/Object;
            .source "SampleClass.kt"
            """.trimIndent(),
            mapping = """
            sample.a -> sample.SampleClass:
            """.trimIndent()
        )
    }

    @Test
    fun doesntReportNameForBasicClassWithMissingSourceFile() {
        analyzerRule.analyzer.assert(
            """
            .class public final Lsample/a;
            .super Ljava/lang/Object;
            """.trimIndent(),
            mapping = ""
        )
    }

    @Test
    fun reportsCorrectNameForInnerClasses() {
        analyzerRule.analyzer.assert(
            """
            .class public final Lsample/a${'$'}a;
            .super Ljava/lang/Object;
            .source "SampleClass.java"
            """.trimIndent(),
            mapping = ""
        )
    }

    @Test
    fun enclosingAnnotation() {
        analyzerRule.analyzer.assert(
            """
            .class public interface abstract La;
            .super Ljava/lang/Object;
            .source "Sample.java"

            # annotations
            .annotation system Ldalvik/annotation/EnclosingClass;
                value = Lb;
            .end annotation
            """.trimIndent(),
            mapping = ""
        )
    }

    @Test
    fun synthetic() {
        analyzerRule.analyzer.assert(
            """
            .class synthetic abstract La;
            .super Ljava/lang/Object;
            .source "Sample.java"
            """.trimIndent(),
            mapping = ""
        )
    }
}
