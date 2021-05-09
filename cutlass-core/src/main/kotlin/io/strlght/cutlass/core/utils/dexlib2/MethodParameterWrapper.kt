package io.strlght.cutlass.core.utils.dexlib2

import org.jf.dexlib2.base.BaseMethodParameter
import org.jf.dexlib2.iface.Annotation
import org.jf.dexlib2.iface.MethodParameter

class MethodParameterWrapper(
    private val reference: MethodParameter,
    private val name: String
) : BaseMethodParameter() {
    override fun getType(): String =
        reference.type

    override fun getName(): String =
        name

    override fun getAnnotations(): MutableSet<out Annotation> =
        reference.annotations
}
