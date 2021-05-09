package io.strlght.cutlass.core.config

import org.jf.dexlib2.DexFileFactory
import org.jf.dexlib2.Opcodes
import org.jf.dexlib2.iface.DexFile
import java.nio.file.Path

sealed class InputType {
    abstract fun provideDexFiles(opcodes: Opcodes): List<DexFile>

    data class DexFiles(val files: List<Path>) : InputType() {
        override fun provideDexFiles(opcodes: Opcodes): List<DexFile> =
            files.map {
                runCatching { DexFileFactory.loadDexFile(it.toFile(), opcodes) }
                    .getOrThrow()
            }
    }

    data class ApkFile(val path: Path) : InputType() {
        override fun provideDexFiles(opcodes: Opcodes): List<DexFile> =
            DexFileFactory.loadDexContainer(path.toFile(), opcodes)
                ?.let { container ->
                    container.dexEntryNames
                        .mapNotNull { container.getEntry(it)?.dexFile }
                }.orEmpty()
    }
}
