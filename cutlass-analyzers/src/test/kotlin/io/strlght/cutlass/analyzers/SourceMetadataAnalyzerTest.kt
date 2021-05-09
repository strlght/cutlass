package io.strlght.cutlass.analyzers

import io.strlght.cutlass.api.DefaultAnalyzerContext
import io.strlght.cutlass.test.assert
import org.junit.Test

class SourceMetadataAnalyzerTest {
    @Test
    fun reportsNameForBasicClass() {
        SourceMetadataAnalyzer(context = DefaultAnalyzerContext()).assert(
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
        SourceMetadataAnalyzer(context = DefaultAnalyzerContext()).assert(
            """
            .class public final Lsample/a;
            .super Ljava/lang/Object;
            """.trimIndent(),
            mapping = ""
        )
    }

    @Test
    fun reportsCorrectNameForInnerClasses() {
        SourceMetadataAnalyzer(context = DefaultAnalyzerContext()).assert(
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
        SourceMetadataAnalyzer(context = DefaultAnalyzerContext()).assert(
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
        SourceMetadataAnalyzer(context = DefaultAnalyzerContext()).assert(
            """
            .class synthetic abstract La;
            .super Ljava/lang/Object;
            .source "Sample.java"
            """.trimIndent(),
            mapping = ""
        )
    }
}
