package io.strlght.cutlass.api

import io.strlght.cutlass.api.metrics.perormance.Timing

interface Result {
    val error: Throwable?
    val typeMapping: TypeMapping?
    val timings: List<Timing>?
}
