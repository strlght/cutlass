package io.strlght.cutlass.api

import org.jf.dexlib2.iface.ClassDef

interface FindingResolver {
    fun resolve(classes: List<ClassDef>, findings: List<Finding>): List<Finding>
}
