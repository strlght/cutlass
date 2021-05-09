package io.strlght.cutlass.core.utils.dexlib2

import org.jf.dexlib2.base.reference.BaseFieldReference
import org.jf.dexlib2.iface.reference.FieldReference

class FieldReferenceWrapper(
    private val reference: FieldReference,
    private val name: String
) : BaseFieldReference() {
    override fun getName(): String =
        name

    override fun getType(): String =
        reference.type

    override fun getDefiningClass(): String =
        reference.definingClass
}
