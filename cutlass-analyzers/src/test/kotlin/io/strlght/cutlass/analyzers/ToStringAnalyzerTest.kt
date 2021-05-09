package io.strlght.cutlass.analyzers

import io.strlght.cutlass.api.DefaultAnalyzerContext
import io.strlght.cutlass.test.assert
import org.junit.Test

class ToStringAnalyzerTest {
    @Test
    fun basic() {
        ToStringAnalyzer(context = DefaultAnalyzerContext()).assert(
            """
            .class public final LToStringBasicTest;
            .super Ljava/lang/Object;
            .source "ToStringBasicTest.kt"

            # instance fields
            .field private final a:F
            .field private final b:F

            # direct methods
            .method public toString()Ljava/lang/String;
                .locals 3
                new-instance v0, Ljava/lang/StringBuilder;
                invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V
                const-string v1, "ToStringBasicTest(coordX="
                invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                iget v1, p0, LToStringBasicTest;->a:F
                invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(F)Ljava/lang/StringBuilder;
                const-string v1, ", coordY="
                invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                iget v1, p0, LToStringBasicTest;->b:F
                invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(F)Ljava/lang/StringBuilder;
                const-string v1, ")"
                invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
                move-result-object v0
                return-object v0
            .end method
            """.trimIndent(),
            mapping = """
            ToStringBasicTest -> ToStringBasicTest:
                float a -> coordX
                float b -> coordY
            """.trimIndent()
        )
    }

    @Test
    fun className() {
        ToStringAnalyzer(context = DefaultAnalyzerContext()).assert(
            """
            .class public final La;
            .super Ljava/lang/Object;

            # instance fields
            .field private final a:F
            .field private final b:F

            # direct methods
            .method public toString()Ljava/lang/String;
                .locals 3
                new-instance v0, Ljava/lang/StringBuilder;
                invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V
                const-string v1, "ToStringClassName(coordX="
                invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                iget v1, p0, La;->a:F
                invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(F)Ljava/lang/StringBuilder;
                const-string v1, ", coordY="
                invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                iget v1, p0, La;->b:F
                invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(F)Ljava/lang/StringBuilder;
                const-string v1, ")"
                invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
                move-result-object v0
                return-object v0
            .end method
            """.trimIndent(),
            mapping = """
            a -> ToStringClassName:
                float a -> coordX
                float b -> coordY
            """.trimIndent()
        )
    }

    @Test
    fun dummyToStringBody() {
        ToStringAnalyzer(context = DefaultAnalyzerContext()).assert(
            """
            .class public final LToStringEmpty;
            .super Ljava/lang/Object;
            .source "ToStringEmpty.kt"

            # instance fields
            .field private final a:F
            .field private final b:F

            # direct methods
            .method public toString()Ljava/lang/String;
                .locals 1
                const-string v0, "ToStringEmpty(fake="
                return-object v0
            .end method
            """.trimIndent(),
            mapping = ""
        )
    }
}
