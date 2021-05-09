package io.strlght.cutlass.core.utils.dexlib2

import org.jf.dexlib2.base.reference.BaseMethodReference
import org.jf.dexlib2.iface.reference.MethodReference

class MethodReferenceWrapper(
    private val reference: MethodReference,
    private val name: String
) : BaseMethodReference() {
    override fun getDefiningClass(): String =
        reference.definingClass

    override fun getName(): String =
        name

    override fun getParameterTypes(): MutableList<out CharSequence> =
        reference.parameterTypes

    override fun getReturnType(): String =
        reference.returnType
}
