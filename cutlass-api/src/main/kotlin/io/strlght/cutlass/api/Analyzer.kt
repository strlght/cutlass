package io.strlght.cutlass.api

import org.jf.dexlib2.iface.ClassDef

abstract class Analyzer(
    protected val context: AnalyzerContext
) : AnalyzerContext by context {
    open val id: String by lazy { this::class.java.simpleName }

    open fun prepare(classDef: ClassDef) =
        Unit

    open fun needsAnotherRound(): Boolean =
        false

    fun handle(classDef: ClassDef): List<Finding> {
        context.clear()
        process(classDef)
        return context.results.onEach { it.source = id }
    }

    protected abstract fun process(classDef: ClassDef)
}
