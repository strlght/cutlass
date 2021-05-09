package io.strlght.cutlass.api.ext

import io.strlght.cutlass.api.types.Members
import io.strlght.cutlass.api.types.Type
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.Member
import org.jf.dexlib2.iface.reference.FieldReference
import org.jf.dexlib2.iface.reference.MethodReference

fun MethodReference.toCutlassModel(): Members.Method =
    Members.Method(
        parent = Type(definingClass),
        name = name,
        returnType = Type(returnType),
        parameterTypes = parameterTypes.map { Type(it.toString()) }
    )

fun FieldReference.toCutlassModel(): Members.Field =
    Members.Field(
        parent = Type(definingClass),
        name = name,
        type = Type(type)
    )

val ClassDef.cutlassType: Type
    get() = Type(type)

val Member.definingType: Type
    get() = Type(definingClass)
