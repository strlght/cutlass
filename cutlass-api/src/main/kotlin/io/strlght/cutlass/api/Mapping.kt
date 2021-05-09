package io.strlght.cutlass.api

import io.strlght.cutlass.api.types.Members
import io.strlght.cutlass.api.types.Type

data class FieldMapping(
    val name: String
)

data class MethodParameterMapping(
    val name: String
)

data class MethodMapping(
    val name: String? = null,
    val parameters: MutableMap<Int, MethodParameterMapping> = mutableMapOf()
)

data class ClassMapping(
    val newType: Type? = null,
    val fieldMapping: MutableMap<Members.Field, FieldMapping> = mutableMapOf(),
    val methodMapping: MutableMap<Members.Method, MethodMapping> = mutableMapOf()
) {
    fun isEmpty(): Boolean =
        (newType?.length ?: 0) == 0 &&
            fieldMapping.isNullOrEmpty() &&
            methodMapping.isNullOrEmpty()
}
