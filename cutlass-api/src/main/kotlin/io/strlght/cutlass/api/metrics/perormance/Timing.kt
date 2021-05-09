package io.strlght.cutlass.api.metrics.perormance

import java.time.Duration

data class Timing(val stage: Stage, val duration: Duration)
