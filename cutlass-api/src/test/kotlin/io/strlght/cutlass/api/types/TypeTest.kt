package io.strlght.cutlass.api.types

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test

class TypeTest {
    private val type = Type("Lexample/Class;")

    @Test
    fun fqName() {
        assertThat(type.fqName).isEqualTo("example.Class")
    }

    @Test
    fun simpleName() {
        assertThat(type.simpleName).isEqualTo("Class")
    }

    @Test
    fun simpleNameSubclass() {
        val type = Type("Lexample/Class\$Inner;")
        assertThat(type.simpleName).isEqualTo("Class\$Inner")
    }

    @Test
    fun rootType() {
        val arrayType = Type("[[Lexample/Class;")
        assertThat(arrayType.rootType).isEqualTo(type)
    }
}
