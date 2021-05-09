package io.strlght.cutlass.analyzers.ext

import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.Member
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.iface.reference.MethodReference

private val ENCLOSING_ANNOTATIONS = setOf(
    "Ldalvik/annotation/EnclosingMethod;",
    "Ldalvik/annotation/EnclosingClass;"
)

internal fun ClassDef.hasEnclosing(): Boolean =
    annotations.any { it.type in ENCLOSING_ANNOTATIONS }

internal fun MethodReference.isConstructor(): Boolean =
    name == "<init>"

internal fun Method.isStaticConstructor(): Boolean =
    accessAll(AccessFlags.CONSTRUCTOR, AccessFlags.STATIC)

internal fun Method.isConstructor(withStatic: Boolean = true): Boolean =
    accessAll(AccessFlags.CONSTRUCTOR) &&
        (withStatic || accessNone(AccessFlags.STATIC))

internal fun Method.isNotConstructor(withStatic: Boolean = true): Boolean =
    !isConstructor(withStatic = withStatic)

private fun Int.matchesAll(vararg values: AccessFlags): Boolean =
    values.all { this and it.value != 0 }

private fun Int.matchesNone(vararg values: AccessFlags): Boolean =
    values.all { this and it.value == 0 }

internal fun Member.accessAll(vararg values: AccessFlags): Boolean =
    accessFlags.matchesAll(*values)

internal fun Member.accessNone(vararg values: AccessFlags): Boolean =
    accessFlags.matchesNone(*values)

internal fun ClassDef.accessAll(vararg values: AccessFlags): Boolean =
    accessFlags.matchesAll(*values)

internal fun ClassDef.accessNone(vararg values: AccessFlags): Boolean =
    accessFlags.matchesNone(*values)
