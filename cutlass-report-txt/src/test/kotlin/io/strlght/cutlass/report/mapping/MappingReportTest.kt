package io.strlght.cutlass.report.mapping

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.strlght.cutlass.api.DefaultTypeMapping
import io.strlght.cutlass.api.types.Members
import io.strlght.cutlass.api.types.Type
import org.junit.Test

class MappingReportTest {
    @Test
    fun read() {
        val classPool = DefaultTypeMapping()
        MappingReport().parse(
            classPool,
            """
            a.b.c -> com.example.Cls:
                java.lang.Object b -> object
            a.b.d -> com.example.Cls2:
                java.lang.Object a -> object
            """.trimIndent()
        )

        assertThat(classPool.findNewType("La/b/c;"))
            .isEqualTo("Lcom/example/Cls;")
        assertThat(classPool.findNewType("La/b/d;"))
            .isEqualTo("Lcom/example/Cls2;")

        assertThat(
            classPool.findNewFieldName(
                Members.Field(
                    Type("La/b/c;"),
                    "b",
                    Type("Ljava/lang/Object;")
                )
            )
        ).isEqualTo("object")
    }
}
