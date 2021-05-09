package io.strlght.cutlass.cli.commands

import com.github.ajalt.clikt.core.CliktCommand

internal class RootCommand : CliktCommand(name = "cutlass") {
    override fun aliases() = mapOf(
        "a" to listOf("analyze"),
        "p" to listOf("process"),
        "r" to listOf("rewrite"),
    )

    override fun run() =
        Unit
}
