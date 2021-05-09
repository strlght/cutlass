package io.strlght.cutlass.cli.performance

import io.strlght.cutlass.api.metrics.perormance.Stage
import io.strlght.cutlass.api.metrics.perormance.Timing

private const val INTERNAL_PREFIX = "    "

internal fun List<Timing>.format(prefix: String = INTERNAL_PREFIX): String {
    val result = StringBuilder()
    forEach {
        val stage = when (val stage = it.stage) {
            Stage.Analysis -> "analysis"
            Stage.LoadAnalyzers -> "load analyzers"
            Stage.LoadDex -> "load dex files"
            Stage.Prepare -> "prepare"
            is Stage.PrepareAnalyzer -> "${prefix}prepare ${stage.id}"
            Stage.Resolve -> "resolve"
            Stage.RewriteDex -> "rewrite"
            is Stage.RunAnalyzer -> "${prefix}analysis ${stage.id}"
        }
        val timing = it.duration.toString()
        result.append(stage, " ", timing, "\n")
    }
    return result.toString()
}
