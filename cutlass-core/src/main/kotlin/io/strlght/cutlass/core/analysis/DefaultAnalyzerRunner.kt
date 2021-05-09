package io.strlght.cutlass.core.analysis

import io.strlght.cutlass.api.Analyzer
import io.strlght.cutlass.api.Finding
import io.strlght.cutlass.api.metrics.perormance.Stage
import io.strlght.cutlass.core.utils.performance.PerformanceCollector
import org.jf.dexlib2.iface.ClassDef

class DefaultAnalyzerRunner : AnalyzerRunner {
    override fun prepare(
        analyzers: List<Analyzer>,
        classes: Iterable<ClassDef>,
        performanceCollector: PerformanceCollector
    ) {
        analyzers.forEach { analyzer ->
            performanceCollector.measure(Stage.PrepareAnalyzer(analyzer.id)) {
                do {
                    classes.forEach { cls ->
                        analyzer.runCatching {
                            prepare(cls)
                        }.getOrThrow()
                    }
                } while (analyzer.needsAnotherRound())
            }
        }
    }

    override fun process(
        analyzers: List<Analyzer>,
        classes: Iterable<ClassDef>,
        performanceCollector: PerformanceCollector
    ): List<Finding> =
        analyzers.flatMap { analyzer ->
            val source = analyzer.id
            performanceCollector.measure(Stage.RunAnalyzer(source)) {
                classes.flatMap { cls ->
                    analyzer.runCatching {
                        handle(cls)
                    }.getOrThrow()
                }
            }
        }
}
