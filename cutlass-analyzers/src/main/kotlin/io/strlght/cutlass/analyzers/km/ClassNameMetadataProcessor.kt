package io.strlght.cutlass.analyzers.km

import io.strlght.cutlass.api.AnalyzerContext
import io.strlght.cutlass.api.Finding
import io.strlght.cutlass.api.ext.cutlassType
import io.strlght.cutlass.api.types.Type
import kotlinx.metadata.KmClass
import org.jf.dexlib2.iface.ClassDef

internal class ClassNameMetadataProcessor : KotlinMetadataProcessor {
    override fun process(
        classDef: ClassDef,
        kmClass: KmClass,
        context: AnalyzerContext
    ) {
        kmClass.type
            .takeIf { classDef.type != it }
            ?.also { context.report(Finding.ClassName(classDef.cutlassType, Type(it))) }
    }
}
