package io.strlght.cutlass.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import io.strlght.cutlass.api.TypeMapping
import io.strlght.cutlass.cli.commands.options.MetricsOptions
import io.strlght.cutlass.cli.commands.options.input
import io.strlght.cutlass.cli.performance.format
import io.strlght.cutlass.cli.utils.toInputType
import io.strlght.cutlass.cli.utils.toRewriteOptions
import io.strlght.cutlass.core.Pipeline
import io.strlght.cutlass.core.config.Config
import io.strlght.cutlass.core.result.CutlassResult
import io.strlght.cutlass.report.mapping.MappingReport
import java.io.PrintStream
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.system.exitProcess

internal abstract class PipelineCommand(
    name: String,
    help: String = "",
    private val output: PrintStream,
    private val err: PrintStream,
) : CliktCommand(name = name, help = help) {
    private val files by input()

    private val force by option(
        "-f", "--force",
        help = "Rewrite output if it exists"
    ).flag()

    private val metrics by MetricsOptions()

    private val library by option(
        "-l", "--library",
        help = "Path to android.jar"
    ).path()

    protected fun runPipeline(
        output: Path? = null,
        skipValidation: Boolean = false,
        mappingOutput: Path? = null,
        existingMapping: Path? = null,
    ) {
        val mapping = existingMapping?.let { MappingReport().read(existingMapping) }
        val rewriteOptions =
            if (output == null && skipValidation) {
                null
            } else {
                output.toRewriteOptions()
            }

        val config = Config(
            inputType = files.toInputType(),
            rewriteOptions = rewriteOptions,
            libraryClassesPath = library,
        )
        val result = Pipeline(
            config = config,
            typeMapping = mapping,
        ).use { it.run() }

        processResult(
            result = result,
            mappingOutput = mappingOutput,
        )
    }

    private fun processResult(
        result: CutlassResult,
        mappingOutput: Path?
    ) {
        result.error
            ?.also {
                err.println("Something went wrong")
                it.printStackTrace(err)
                exitProcess(-1)
            }
        result.typeMapping
            ?.also { printMapping(it, mappingOutput) }
        result.timings
            ?.takeIf { metrics.timings }
            ?.also {
                err.println("Timings:")
                err.println(it.format())
            }
    }

    private fun printMapping(
        typeMapping: TypeMapping,
        mappingOutput: Path?
    ) {
        if (mappingOutput == null) {
            output.println(MappingReport().render(typeMapping))
        } else {
            mappingOutput.let {
                if (mappingOutput.isDirectory()) {
                    mappingOutput.resolve("mapping.txt")
                } else {
                    mappingOutput
                }
            }.also {
                if (it.exists() && !force) {
                    throw IllegalStateException(
                        "mappingOutput file already exists. Use -f or --force to rewrite it."
                    )
                }
                MappingReport().write(it, typeMapping)
            }
        }
    }
}
