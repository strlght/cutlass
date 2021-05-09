package io.strlght.cutlass.analyzers.km

import io.strlght.cutlass.api.types.Type
import kotlinx.metadata.KmClass
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmType
import org.jf.dexlib2.immutable.ImmutableClassDef
import org.junit.Test

class SuperClassNameMetadataProcessorTest {
    private fun createDummyClass(
        superType: Type? = null,
        interfaces: List<Type>? = null
    ) =
        ImmutableClassDef(
            "Lsample/a;",
            0,
            superType?.value,
            interfaces?.map { it.value },
            null,
            null,
            null,
            null
        )

    @Test
    fun basic() {
        val supertype = Type("Lsample/b;")
        val cls = createDummyClass(supertype)
        val km = KmClass().apply {
            name = "sample/SampleClass"
        }
        km.supertypes.add(KmType(0)
            .apply {
                classifier = KmClassifier.Class("sample/SampleSuper")
            })

        SuperClassNameMetadataProcessor().assert(
            cls,
            km,
            mapping = "sample.b -> sample.SampleSuper:"
        )
    }

    @Test
    fun sameName() {
        val supertype = Type("Lsample/SampleSuper;")
        val cls = createDummyClass(supertype)
        val km = KmClass().apply {
            name = "sample/SampleClass"
        }
        km.supertypes.add(KmType(0)
            .apply {
                classifier = KmClassifier.Class("sample/SampleSuper")
            })

        SuperClassNameMetadataProcessor().assert(
            cls,
            km,
            mapping = ""
        )
    }

    @Test
    fun javaStdlib() {
        val supertype = Type("Ljava/util/ArrayList;")
        val cls = createDummyClass(supertype)
        val km = KmClass().apply {
            name = "sample/SampleClass"
        }
        km.supertypes.add(KmType(0)
            .apply {
                classifier = KmClassifier.Class("java/util/ArrayList")
            })

        SuperClassNameMetadataProcessor().assert(
            cls,
            km,
            mapping = ""
        )
    }

    @Test
    fun emptySupertype() {
        val supertype = Type("Lsample/a;")
        val cls = createDummyClass(supertype)
        val km = KmClass().apply {
            name = "sample/SampleClass"
        }

        SuperClassNameMetadataProcessor().assert(
            cls,
            km,
            mapping = ""
        )
    }

    @Test
    fun singleInterface() {
        val supertype = Type("Lsample/b;")
        val cls = createDummyClass(interfaces = listOf(supertype))
        val km = KmClass().apply {
            name = "sample/SampleClass"
        }
        km.supertypes.add(KmType(0)
            .apply {
                classifier = KmClassifier.Class("sample/SampleSuper")
            })

        SuperClassNameMetadataProcessor().assert(
            cls,
            km,
            mapping = "sample.b -> sample.SampleSuper:"
        )
    }

    @Test
    fun multipleInterfaces() {
        val cls = createDummyClass(
            interfaces = listOf(
                Type("Lsample/b;"),
                Type("Lsample/c;"),
            )
        )
        val km = KmClass().apply {
            name = "sample/SampleClass"
        }
        km.supertypes.add(KmType(0)
            .apply {
                classifier = KmClassifier.Class("sample/SampleInterface1")
            })
        km.supertypes.add(KmType(0)
            .apply {
                classifier = KmClassifier.Class("sample/SampleInterface2")
            })

        SuperClassNameMetadataProcessor().assert(
            cls,
            km,
            mapping = ""
        )
    }
}