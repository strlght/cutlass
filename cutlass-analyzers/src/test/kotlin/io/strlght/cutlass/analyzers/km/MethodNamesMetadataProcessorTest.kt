package io.strlght.cutlass.analyzers.km

import io.strlght.cutlass.test.asSmaliClassDef
import kotlinx.metadata.KmClass
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmFunction
import kotlinx.metadata.KmType
import org.junit.Test

class MethodNamesMetadataProcessorTest {
    @Test
    fun basic() {
        val cls = "KmMethodsBasic.smali".asSmaliClassDef()
        val km = KmClass()
        km.name = "sample/SampleClass"
        km.functions.add(
            KmFunction(0, "method")
                .apply {
                    returnType = KmType(0).apply { classifier = KmClassifier.Class("kotlin/Unit") }
                    applyJvmExtensions()
                }
        )

        MethodNamesMetadataProcessor().assert(
            cls,
            km,
            mapping = """
                KmMethodsBasic -> KmMethodsBasic:
                    void a() -> method
            """.trimIndent()
        )
    }

    @Test
    fun sameSignature() {
        val cls = "KmMethodsSameSignature.smali".asSmaliClassDef()
        val km = KmClass()
        km.name = "sample/SampleClass"
        km.functions.add(
            KmFunction(0, "method")
                .apply {
                    returnType = KmType(0).apply { classifier = KmClassifier.Class("kotlin/Unit") }
                    applyJvmExtensions()
                }
        )

        MethodNamesMetadataProcessor().assert(
            cls,
            km,
            mapping = ""
        )
    }

    @Test
    fun sameName() {
        val cls = "KmMethodsSameName.smali".asSmaliClassDef()
        val km = KmClass()
        km.name = "sample/SampleClass"
        km.functions.add(
            KmFunction(0, "method")
                .apply {
                    returnType = KmType(0).apply { classifier = KmClassifier.Class("kotlin/Unit") }
                    applyJvmExtensions()
                }
        )

        MethodNamesMetadataProcessor().assert(
            cls,
            km,
            mapping = ""
        )
    }
}
