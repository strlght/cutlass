package io.strlght.cutlass.cli.commands.options

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option

internal class MetricsOptions : OptionGroup("Metrics options") {
    val timings by option(
        "-p", "--performance",
        help = "Print performance metrics"
    ).flag()
}
