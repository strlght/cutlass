package io.strlght.cutlass.core.utils.performance

import io.strlght.cutlass.api.metrics.perormance.Stage
import io.strlght.cutlass.api.metrics.perormance.Timing
import java.time.Duration

class PerformanceCollector {
    val timings get() = _timings
    private var _timings = mutableListOf<Timing>()
    private val startTimes: MutableMap<Stage, Long> = mutableMapOf()

    fun start(stage: Stage) {
        startTimes[stage] = System.currentTimeMillis()
    }

    fun finish(stage: Stage) {
        startTimes[stage]
            ?.also { start ->
                val end = System.currentTimeMillis()
                val duration = Duration.ofMillis(end - start)
                _timings.add(Timing(stage, duration))
                startTimes.remove(stage)
            }
            ?: throw IllegalStateException("Trying to finish not started state: ${stage::class.simpleName}")
    }

    inline fun <R> measure(stage: Stage, block: () -> R): R {
        start(stage)
        val result = block()
        finish(stage)
        return result
    }
}
