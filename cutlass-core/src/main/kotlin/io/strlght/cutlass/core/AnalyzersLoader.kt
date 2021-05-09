package io.strlght.cutlass.core

import io.strlght.cutlass.api.Analyzer
import io.strlght.cutlass.api.AnalyzerContext
import io.strlght.cutlass.api.AnalyzersProvider
import io.strlght.cutlass.api.annotations.ExperimentalAnalyzer
import java.util.ServiceLoader

interface AnalyzersLoader {
    fun load(contextProvider: () -> AnalyzerContext): List<Analyzer>
}

class DefaultAnalyzersLoader : AnalyzersLoader {
    override fun load(contextProvider: () -> AnalyzerContext): List<Analyzer> =
        ServiceLoader.load(AnalyzersProvider::class.java)
            .asSequence()
            .mapNotNull { it.create(contextProvider) }
            .flatten()
            .filter { !it::class.java.isAnnotationPresent(ExperimentalAnalyzer::class.java) }
            .toList()
}
