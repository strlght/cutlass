package io.strlght.cutlass.analyzers.km

import io.strlght.cutlass.api.types.Type
import kotlinx.metadata.KmClass
import org.jf.dexlib2.immutable.ImmutableClassDef
import org.junit.Test

class ClassNameMetadataProcessorTest {
    private fun createDummyClass(type: Type) =
        ImmutableClassDef(
            type.value,
            0,
            null,
            null,
            null,
            null,
            null,
            null
        )

    @Test
    fun basic() {
        val type = Type("Lsample/a;")
        val cls = createDummyClass(type)
        val km = KmClass()
        km.name = "sample/SampleClass"

        ClassNameMetadataProcessor().assert(
            cls,
            km,
            mapping = "sample.a -> sample.SampleClass:"
        )
    }

    @Test
    fun sameName() {
        val type = Type("Lsample/SampleClass;")
        val cls = createDummyClass(type)
        val km = KmClass()
        km.name = "sample/SampleClass"

        ClassNameMetadataProcessor().assert(
            cls,
            km,
            mapping = ""
        )
    }
}
