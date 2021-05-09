package io.strlght.cutlass.core.result

import io.strlght.cutlass.api.TypeMapping
import io.strlght.cutlass.api.Result
import io.strlght.cutlass.api.metrics.perormance.Timing

class CutlassResult(
    override val error: Throwable? = null,
    override val typeMapping: TypeMapping? = null,
    override val timings: List<Timing>? = null
) : Result
