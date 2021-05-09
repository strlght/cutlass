package io.strlght.cutlass.api.metrics.perormance

sealed class Stage {
    object LoadAnalyzers : Stage()
    object LoadDex : Stage()
    object Prepare : Stage()
    data class PrepareAnalyzer(val id: String) : Stage()
    object Analysis : Stage()
    data class RunAnalyzer(val id: String) : Stage()
    object Resolve : Stage()
    object RewriteDex : Stage()
}
