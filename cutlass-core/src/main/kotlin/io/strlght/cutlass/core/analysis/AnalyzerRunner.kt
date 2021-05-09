package io.strlght.cutlass.core.analysis

import io.strlght.cutlass.api.Analyzer
import io.strlght.cutlass.api.Finding
import io.strlght.cutlass.core.utils.performance.PerformanceCollector
import org.jf.dexlib2.iface.ClassDef

interface AnalyzerRunner {
    fun prepare(
        analyzers: List<Analyzer>,
        classes: Iterable<ClassDef>,
        performanceCollector: PerformanceCollector
    )

    fun process(
        analyzers: List<Analyzer>,
        classes: Iterable<ClassDef>,
        performanceCollector: PerformanceCollector
    ): List<Finding>
}
