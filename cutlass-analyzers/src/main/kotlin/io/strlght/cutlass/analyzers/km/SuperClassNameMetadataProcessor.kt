package io.strlght.cutlass.analyzers.km

import io.strlght.cutlass.api.AnalyzerContext
import io.strlght.cutlass.api.Finding
import io.strlght.cutlass.api.types.Type
import kotlinx.metadata.KmClass
import kotlinx.metadata.KmClassifier
import org.jf.dexlib2.iface.ClassDef

internal class SuperClassNameMetadataProcessor : KotlinMetadataProcessor {
    override fun process(
        classDef: ClassDef,
        kmClass: KmClass,
        context: AnalyzerContext
    ) {
        (classDef.superclass ?: classDef.interfaces.singleOrNull())
            ?.takeIf { !context.isLibraryType(Type(it)) }
            ?.let { superClass ->
                kmClass.supertypes
                    .mapNotNull { it.classifier as? KmClassifier.Class }
                    .singleOrNull()
                    ?.let { superClass to it.type }
            }
            ?.takeIf { it.first != it.second }
            ?.also {
                context.report(Finding.ClassName(Type(it.first), Type(it.second)))
            }
    }
}
