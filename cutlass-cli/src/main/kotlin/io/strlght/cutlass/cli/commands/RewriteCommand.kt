package io.strlght.cutlass.cli.commands

import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.path
import java.io.PrintStream

internal class RewriteCommand(output: PrintStream, err: PrintStream) :
    PipelineCommand(
        name = "rewrite",
        help = "Rewrite apk/dex files",
        output = output,
        err = err
    ) {
    private val mapping by option(
        "-m", "--mapping",
        help = "Mapping to apply to input",
    ).path(
        canBeFile = true,
        canBeDir = false,
        mustBeReadable = true
    ).required()

    override fun run() {
        runPipeline(existingMapping = mapping)
    }
}
