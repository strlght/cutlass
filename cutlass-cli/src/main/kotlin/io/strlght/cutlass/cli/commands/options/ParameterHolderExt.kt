package io.strlght.cutlass.cli.commands.options

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.types.path

internal fun CliktCommand.input() =
    argument(
        name = "FILES",
        help = "List of files to analyze"
    ).path(
        canBeFile = true,
        mustBeReadable = true,
        mustExist = true
    ).multiple(
        required = true
    )
