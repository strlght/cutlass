package io.strlght.cutlass.analyzers

import io.strlght.cutlass.analyzers.ext.accessNone
import io.strlght.cutlass.analyzers.ext.hasEnclosing
import io.strlght.cutlass.api.Analyzer
import io.strlght.cutlass.api.AnalyzerContext
import io.strlght.cutlass.api.Finding
import io.strlght.cutlass.api.ext.cutlassType
import io.strlght.cutlass.api.types.Type
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.iface.ClassDef

class SourceMetadataAnalyzer(context: AnalyzerContext) : Analyzer(context) {
    private val nameRegex by lazy { "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*".toRegex() }
    private val namesMap = mutableMapOf<Type, Int>()

    private fun extractType(classDef: ClassDef): Type? {
        val type = classDef.cutlassType
        return classDef
            .takeIf {
                !it.hasEnclosing() &&
                    it.accessNone(AccessFlags.SYNTHETIC)
            }
            ?.sourceFile
            ?.takeIf(::isValidSource)
            ?.substringBefore(".")
            ?.let { nameRegex.find(it)?.value }
            ?.takeIf { isValidClassName(type, it) }
            ?.let { type.replaceClassName(it) }
    }

    override fun prepare(classDef: ClassDef) {
        super.prepare(classDef)
        extractType(classDef)
            ?.also {
                namesMap[it] = namesMap.getOrDefault(it, 0) + 1
            }
    }

    override fun process(classDef: ClassDef) {
        extractType(classDef)
            ?.takeIf { namesMap.getOrDefault(it, 0) <= 1 }
            ?.also { context.report(Finding.ClassName(classDef.cutlassType, it)) }
    }

    private fun isValidSource(source: String) =
        source.endsWith(".kt") || source.endsWith(".java")

    private fun isValidClassName(type: Type, source: String): Boolean {
        val className = type.simpleName
        return !type.isInner &&
            className != source &&
            source.length >= className.length
    }
}
