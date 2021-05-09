package io.strlght.cutlass.api

import io.strlght.cutlass.api.types.Members
import io.strlght.cutlass.api.types.Type

sealed class Finding {
    abstract val type: Type
    var source: String? = null

    data class ClassName(
        override val type: Type,
        val newType: Type
    ) : Finding()

    data class MethodName(
        val method: Members.Method,
        val newName: String
    ) : Finding() {
        override val type: Type
            get() = method.parent
    }

    data class FieldName(
        val field: Members.Field,
        val newName: String
    ) : Finding() {
        override val type: Type
            get() = this.field.parent
    }

    data class ParameterName(
        val method: Members.Method,
        val idx: Int,
        val parameterName: String
    ) : Finding() {
        override val type: Type
            get() = method.parent
    }
}
