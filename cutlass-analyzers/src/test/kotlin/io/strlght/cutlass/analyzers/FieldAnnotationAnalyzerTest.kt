package io.strlght.cutlass.analyzers

import io.strlght.cutlass.test.AnalyzerRule
import io.strlght.cutlass.test.assert
import org.junit.Rule
import org.junit.Test

class FieldAnnotationAnalyzerTest {
    @get:Rule
    val analyzerRule = AnalyzerRule(FieldAnnotationAnalyzer::class.java)

    @Test
    fun basic() {
        analyzerRule.analyzer.assert(
            """
            .class public final LFieldAnnotationBasic;
            .super Ljava/lang/Object;

            # instance fields
            .field private final a:F
                .annotation runtime Lcom/google/gson/annotations/SerializedName;
                    value = "latitude"
                .end annotation
            .end field

            .field private final b:F
                .annotation runtime Lcom/google/gson/annotations/SerializedName;
                    value = "longitude"
                .end annotation
            .end field
            """.trimIndent(),
            mapping = """
            FieldAnnotationBasic -> FieldAnnotationBasic:
                float a -> latitude
                float b -> longitude
            """.trimIndent()
        )
    }

    @Test
    fun empty() {
        analyzerRule.analyzer.assert(
            """
            .class public final LFieldAnnotationEmpty;
            .super Ljava/lang/Object;

            # instance fields
            .field private final coordLatitude:F
                .annotation runtime Lcom/google/gson/annotations/SerializedName;
                    value = "latitude"
                .end annotation
            .end field

            .field private final b:F
            """.trimIndent(),
            mapping = ""
        )
    }

    @Test
    fun sameName() {
        analyzerRule.analyzer.assert(
            """
            .class public final LFieldAnnotationSameName;
            .super Ljava/lang/Object;

            # instance fields
            .field private final coordLatitude:F
                .annotation runtime Lcom/google/gson/annotations/SerializedName;
                    value = "coord_latitude"
                .end annotation
            .end field
            """.trimIndent(),
            mapping = ""
        )
    }

    @Test
    fun missing() {
        analyzerRule.analyzer.assert(
            """
            .class public final LFieldAnnotationMissing;
            .super Ljava/lang/Object;

            # instance fields
            .field private final a:F
                .annotation runtime Lcom/google/gson/annotations/SerializedName;
                    value = "latitude"
                .end annotation
            .end field

            .field private final b:F
            """.trimIndent(),
            mapping = ""
        )
    }
}
