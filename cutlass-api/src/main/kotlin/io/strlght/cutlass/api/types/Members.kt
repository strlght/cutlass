package io.strlght.cutlass.api.types

sealed class Members {
    abstract val parent: Type
    abstract val name: String

    data class Field(
        override val parent: Type,
        override val name: String,
        val type: Type
    ) : Members()

    data class Method(
        override val parent: Type,
        override val name: String,
        val parameterTypes: List<Type>,
        val returnType: Type
    ) : Members()
}
