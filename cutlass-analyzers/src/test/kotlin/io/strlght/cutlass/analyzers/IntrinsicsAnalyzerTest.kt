package io.strlght.cutlass.analyzers

import io.strlght.cutlass.api.DefaultAnalyzerContext
import io.strlght.cutlass.test.assert
import io.strlght.cutlass.test.toSmaliClassDef
import org.junit.Test

class IntrinsicsAnalyzerTest {
    private val intrinsicsClassDef = """
        .class public Lkotlin/jvm/internal/k;
        .super Ljava/lang/Object;
        .source "Intrinsics.java"

        # direct methods
        .method private constructor <init>()V
            .registers 1
            .line 1
            invoke-direct {p0}, Ljava/lang/Object;-><init>()V
            return-void
        .end method
        
        .method public static a(Ljava/lang/Object;Ljava/lang/String;)V
            .registers 3
            if-eqz p0, :cond_3
            return-void
            .line 2
            :cond_3
            new-instance p0, Ljava/lang/IllegalStateException;
            new-instance v0, Ljava/lang/StringBuilder;
            invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V
            invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
            const-string p1, " must not be null"
            invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
            invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
            move-result-object p1
            invoke-direct {p0, p1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
            invoke-static {p0}, Lkotlin/jvm/c/Intrinsics;->a(Ljava/lang/Throwable;)Ljava/lang/Throwable;
            check-cast p0, Ljava/lang/IllegalStateException;
            throw p0
        .end method
    """.trimIndent().toSmaliClassDef()

    @Test
    fun basic() {
        val cls = """
        .class public final Lsample/a;
        .super Ljava/lang/Object;

        # direct methods
        .method public test(Lsample/b;)V
            .locals 2
            invoke-virtual {p0}, Lsample/b;->a()Lsample/c;
            move-result-object v0
            const-string v1, "Sample.test()"
            invoke-static {v0, v1}, Lkotlin/jvm/internal/k;->a(Ljava/lang/Object;Ljava/lang/String;)V
        .end method
        """.trimIndent().toSmaliClassDef()

        IntrinsicsAnalyzer(context = DefaultAnalyzerContext()).assert(
            cls,
            intrinsicsClassDef,
            mapping = """
                sample.b -> sample.b:
                    sample.c a() -> test
            """.trimIndent()
        )
    }

    @Test
    fun withoutExplicitThis() {
        val cls = """
        .class public final Lsample/a;
        .super Ljava/lang/Object;

        # direct methods
        .method public test()V
            .locals 2
            invoke-virtual {p0}, Lsample/a;->a()Lsample/b;
            move-result-object v0
            const-string v1, "test()"
            invoke-static {v0, v1}, Lkotlin/jvm/internal/k;->a(Ljava/lang/Object;Ljava/lang/String;)V
        .end method
        """.trimIndent().toSmaliClassDef()

        IntrinsicsAnalyzer(context = DefaultAnalyzerContext()).assert(
            cls,
            intrinsicsClassDef,
            mapping = """
                sample.a -> sample.a:
                    sample.b a() -> test
            """.trimIndent()
        )
    }

    @Test
    fun withExplicitThis() {
        val cls = """
        .class public final Lsample/a;
        .super Ljava/lang/Object;

        # direct methods
        .method public test()V
            .locals 2
            invoke-virtual {p0}, Lsample/a;->a()Lsample/b;
            move-result-object v0
            const-string v1, "this.test()"
            invoke-static {v0, v1}, Lkotlin/jvm/internal/k;->a(Ljava/lang/Object;Ljava/lang/String;)V
        .end method
        """.trimIndent().toSmaliClassDef()

        IntrinsicsAnalyzer(context = DefaultAnalyzerContext()).assert(
            cls,
            intrinsicsClassDef,
            mapping = """
                sample.a -> sample.a:
                    sample.b a() -> test
            """.trimIndent()
        )
    }
}
