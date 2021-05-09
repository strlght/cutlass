package io.strlght.cutlass.analyzers

import io.strlght.cutlass.api.DefaultAnalyzerContext
import io.strlght.cutlass.test.assert
import org.junit.Test

class FieldAnnotationAnalyzerTest {
    @Test
    fun basic() {
        FieldAnnotationAnalyzer(context = DefaultAnalyzerContext()).assert(
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
        FieldAnnotationAnalyzer(context = DefaultAnalyzerContext()).assert(
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
        FieldAnnotationAnalyzer(context = DefaultAnalyzerContext()).assert(
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
        FieldAnnotationAnalyzer(context = DefaultAnalyzerContext()).assert(
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
