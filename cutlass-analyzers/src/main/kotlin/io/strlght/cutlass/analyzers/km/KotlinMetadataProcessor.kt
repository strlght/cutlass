package io.strlght.cutlass.analyzers.km

import io.strlght.cutlass.api.AnalyzerContext
import kotlinx.metadata.KmClass
import org.jf.dexlib2.iface.ClassDef

internal interface KotlinMetadataProcessor {
    fun process(
        classDef: ClassDef,
        kmClass: KmClass,
        context: AnalyzerContext
    )
}
