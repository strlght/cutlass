package io.strlght.cutlass.api

import io.strlght.cutlass.api.types.Members
import io.strlght.cutlass.api.types.Type

interface TypeMappingVisitor {
    fun visitType(type: Type, newType: Type?)
    fun visitField(field: Members.Field, newName: String)
    fun visitMethod(method: Members.Method, mapping: MethodMapping)
}
