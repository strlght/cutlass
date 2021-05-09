package io.strlght.cutlass.analyzers.km

import io.strlght.cutlass.test.toSmaliClassDef
import kotlinx.metadata.KmClass
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmConstructor
import kotlinx.metadata.KmType
import kotlinx.metadata.KmTypeProjection
import kotlinx.metadata.KmValueParameter
import kotlinx.metadata.KmVariance
import org.junit.Test

class ConstructorsMetadataProcessorTest {
    @Test
    fun basic() {
        val cls = """
            .class public final Lsample/a;
            .super Ljava/lang/Object;
            
            .method public constructor <init>(ZLsample/b;[Lsample/c;)V
                .registers 4
                invoke-direct {v0}, Ljava/lang/Object;-><init>()V
                return-void
            .end method
        """.trimIndent().toSmaliClassDef()
        val km = KmClass()
        km.name = "sample/a"
        km.constructors.add(
            KmConstructor(0).apply {
                valueParameters.add(
                    KmValueParameter(0, "").apply {
                        type = KmType(0).apply { classifier = KmClassifier.Class("kotlin/Boolean") }
                    }
                )
                valueParameters.add(
                    KmValueParameter(0, "").apply {
                        type = KmType(0).apply { classifier = KmClassifier.Class("sample/Test1") }
                    }
                )
                valueParameters.add(
                    KmValueParameter(0, "").apply {
                        type = KmType(0).apply {
                            classifier = KmClassifier.Class("kotlin/Array")
                            arguments.add(
                                KmTypeProjection(
                                    KmVariance.OUT,
                                    KmType(0).apply {
                                        classifier = KmClassifier.Class("sample/Test2")
                                    }
                                )
                            )
                        }
                    }
                )
                applyJvmExtensions()
            }
        )
        ConstructorsMetadataProcessor().assert(
            cls,
            km,
            mapping = """
                sample.b -> sample.Test1:
                sample.c -> sample.Test2:
            """.trimIndent()
        )
    }
}
