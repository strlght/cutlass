package io.strlght.cutlass.cli.utils

import io.strlght.cutlass.core.config.InputType
import io.strlght.cutlass.core.config.RewriteOptions
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries

internal fun List<Path>.toInputType(): InputType =
    toInputTypeOrNull()
        ?: throw IllegalStateException("Illegal input type")

internal fun List<Path>.toInputTypeOrNull(): InputType? =
    extractFilesFromDirectory(this)
        ?: mapInputToApk(this)
        ?: mapInputToDexFiles(this)

internal fun Path?.toRewriteOptions(): RewriteOptions =
    this?.let {
        if (!Files.exists(it)) {
            Files.createDirectories(it)
        }
        RewriteOptions.Disk(it)
    } ?: RewriteOptions.InMemory

private fun extractFilesFromDirectory(inputFiles: List<Path>): InputType? =
    inputFiles
        .singleOrNull()
        ?.takeIf { it.isDirectory() }
        ?.listDirectoryEntries("*.dex")
        ?.takeIf { it.isNotEmpty() }
        ?.let { InputType.DexFiles(it.toList()) }

private fun mapInputToDexFiles(inputFiles: List<Path>): InputType? =
    inputFiles
        .takeIf { files -> files.all { it.extension == "dex" } }
        ?.toList()
        ?.takeIf { it.isNotEmpty() }
        ?.let { InputType.DexFiles(it) }

private fun mapInputToApk(inputFiles: List<Path>): InputType? =
    inputFiles
        .singleOrNull()
        ?.takeIf { it.extension == "apk" && it.exists() }
        ?.let { InputType.ApkFile(it) }
