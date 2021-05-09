package io.strlght.cutlass.analyzers.km

import io.strlght.cutlass.api.ClassPool
import io.strlght.cutlass.api.DefaultAnalyzerContext
import io.strlght.cutlass.api.types.Type
import io.strlght.cutlass.test.asSmaliClassDef
import kotlinx.metadata.KmClass
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmProperty
import kotlinx.metadata.KmType
import org.junit.Test

class FieldNamesMetadataProcessorTest {
    @Test
    fun basic() {
        val cls = "KmFieldsBasic.smali".asSmaliClassDef()
        val km = KmClass()
        km.name = "sample/SampleClass"
        km.properties.add(
            KmProperty(
                0,
                "intField",
                0,
                0
            ).apply {
                returnType = KmType(0).apply { classifier = KmClassifier.Class("kotlin/Int") }
                applyJvmExtensions()
            }
        )
        km.properties.add(
            KmProperty(
                0,
                "floatField",
                0,
                0
            ).apply {
                returnType = KmType(0).apply { classifier = KmClassifier.Class("kotlin/Float") }
                applyJvmExtensions()
            }
        )

        FieldNamesMetadataProcessor().assert(
            cls,
            km,
            mapping = """
                KmFieldsBasic -> KmFieldsBasic:
                    int b -> intField
                    float a -> floatField
            """.trimIndent()
        )
    }

    @Test
    fun sameType() {
        val cls = "KmFieldsSameType.smali".asSmaliClassDef()
        val km = KmClass()
        km.name = "sample/SampleClass"
        km.properties.add(
            KmProperty(
                0,
                "intField1",
                0,
                0
            ).apply {
                returnType = KmType(0).apply { classifier = KmClassifier.Class("kotlin/Int") }
                applyJvmExtensions()
            }
        )
        km.properties.add(
            KmProperty(
                0,
                "intField2",
                0,
                0
            ).apply {
                returnType = KmType(0).apply { classifier = KmClassifier.Class("kotlin/Int") }
                applyJvmExtensions()
            }
        )

        FieldNamesMetadataProcessor().assert(
            cls,
            km,
            mapping = ""
        )
    }

    @Test
    fun singleUnknownType() {
        val cls = "KmFieldsSingleUnknownType.smali".asSmaliClassDef()
        val km = KmClass()
        km.name = "sample/SampleClass"
        km.properties.add(
            KmProperty(
                0,
                "model",
                0,
                0
            ).apply {
                returnType = KmType(0).apply { classifier = KmClassifier.Class("sample/Model") }
                applyJvmExtensions()
            }
        )

        FieldNamesMetadataProcessor().assert(
            cls,
            km,
            mapping = """
                KmFieldssingleUnknownType -> KmFieldssingleUnknownType:
                    sample.a a -> model
                sample.a -> sample.Model:
            """.trimIndent()
        )
    }

    @Test
    fun javaStdlib() {
        val cls = "KmFieldsJavaStdlib.smali".asSmaliClassDef()
        val km = KmClass()
        km.name = "sample/SampleClass"
        km.properties.add(
            KmProperty(
                0,
                "stringField",
                0,
                0
            ).apply {
                returnType = KmType(0).apply { classifier = KmClassifier.Class("kotlin/String") }
                applyJvmExtensions()
            }
        )

        FieldNamesMetadataProcessor().assert(
            cls,
            km,
            mapping = """
                KmFieldsJavaStdlib -> KmFieldsJavaStdlib:
                    java.lang.String a -> stringField
            """.trimIndent()
        )
    }

    @Test
    fun javaStdlibMismatch() {
        val cls = "KmFieldsJavaStdlib.smali".asSmaliClassDef()
        val km = KmClass()
        km.name = "sample/SampleClass"
        km.properties.add(
            KmProperty(
                0,
                "stringField",
                0,
                0
            ).apply {
                returnType = KmType(0).apply { classifier = KmClassifier.Class("sample/RandomClass") }
                applyJvmExtensions()
            }
        )

        FieldNamesMetadataProcessor().assert(
            cls,
            km,
            mapping = "",
            context = DefaultAnalyzerContext(libraryClassPool = object : ClassPool {
                override fun contains(type: Type): Boolean =
                    type.value.startsWith("Ljava/")
            })
        )
    }
}
