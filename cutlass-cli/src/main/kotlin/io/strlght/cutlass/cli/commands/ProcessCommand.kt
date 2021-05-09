package io.strlght.cutlass.cli.commands

import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.path
import java.io.PrintStream
import java.nio.file.Paths

internal class ProcessCommand(output: PrintStream, err: PrintStream) :
    PipelineCommand(
        name = "process",
        help = "Analyze and rewrite apk/dex files in single pass",
        output = output,
        err = err,
    ) {
    private val output by option(
        "-o", "--output",
        help = "Path to rewritten dex files"
    ).path(
        canBeFile = false,
        canBeDir = true
    ).defaultLazy(defaultForHelp = "current directory") {
        Paths.get("")
    }

    private val mapping by option(
        "-m", "--mapping",
        help = "Where to write mapping"
    ).path(
        canBeFile = true,
        canBeDir = false,
        mustBeReadable = true
    ).required()

    override fun run() {
        runPipeline(
            output = output,
            mappingOutput = mapping
        )
    }
}
