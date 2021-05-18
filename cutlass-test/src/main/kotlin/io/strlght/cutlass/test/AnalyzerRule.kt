package io.strlght.cutlass.test

import io.strlght.cutlass.api.Analyzer
import io.strlght.cutlass.api.DefaultAnalyzerContext
import org.junit.rules.ExternalResource

class AnalyzerRule<T : Analyzer>(private val analyzerClass: Class<T>) : ExternalResource() {
    private lateinit var _analyzer: T

    val analyzer: T get() = _analyzer

    override fun before() {
        _analyzer = analyzerClass.declaredConstructors
            .first { it.parameterCount == 1 }
            .newInstance(DefaultAnalyzerContext()) as T
    }
}
