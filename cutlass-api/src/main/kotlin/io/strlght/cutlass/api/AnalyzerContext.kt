package io.strlght.cutlass.api

import io.strlght.cutlass.api.types.Type

interface AnalyzerContext {
    val results: List<Finding>

    fun report(finding: Finding)

    fun clear()

    fun isLibraryType(type: Type): Boolean
}

class DefaultAnalyzerContext(
    private val libraryClassPool: ClassPool = EmptyClassPool(),
) : AnalyzerContext {
    override val results: List<Finding>
        get() = _results

    private var _results: MutableList<Finding> = mutableListOf()

    override fun report(finding: Finding) {
        _results.add(finding)
    }

    override fun clear() {
        _results.clear()
    }

    override fun isLibraryType(type: Type): Boolean =
        type in libraryClassPool
}
