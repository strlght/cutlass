package io.strlght.cutlass.cli.commands

import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import java.io.PrintStream

internal class AnalyzeCommand(output: PrintStream, err: PrintStream) :
    PipelineCommand(
        name = "analyze",
        help = "Analyze apk/dex files and print mapping",
        output = output,
        err = err,
    ) {
    private val output by option(
        "-o", "--output",
        help = "Where to write mapping"
    ).path(
        canBeFile = true,
        canBeDir = true
    )

    private val validate by option(
        "--validate",
        help = "Rewrite dex in memory to validate mapping"
    ).flag()

    override fun run() {
        runPipeline(
            mappingOutput = output,
            skipValidation = !validate
        )
    }
}
