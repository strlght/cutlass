@file:JvmName("Main")

package io.strlght.cutlass.cli

import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.versionOption
import io.strlght.cutlass.cli.commands.AnalyzeCommand
import io.strlght.cutlass.cli.commands.ProcessCommand
import io.strlght.cutlass.cli.commands.RewriteCommand
import io.strlght.cutlass.cli.commands.RootCommand

fun main(args: Array<String>) {
    val output = System.out
    val err = System.err
    RootCommand()
        .subcommands(
            AnalyzeCommand(output, err),
            ProcessCommand(output, err),
            RewriteCommand(output, err),
        )
        .versionOption(RootCommand::class.java.`package`.implementationVersion ?: "SNAPSHOT")
        .main(args)
}
