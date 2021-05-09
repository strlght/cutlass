package io.strlght.cutlass.api

/**
 * An analyzers provider is responsible for creating analyzers.
 *
 * Need to be registered according to ServiceLoader documentation.
 * http://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html
 */
interface AnalyzersProvider {
    /**
     * This function must be overridden to provide custom analyzers.
     */
    fun create(contextProvider: () -> AnalyzerContext): List<Analyzer>
}
