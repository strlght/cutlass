package io.strlght.cutlass.core.config

import org.jf.dexlib2.writer.io.DexDataStore
import org.jf.dexlib2.writer.io.FileDataStore
import org.jf.dexlib2.writer.io.MemoryDataStore
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

sealed class RewriteOptions {
    abstract fun createDataStore(index: Int): DexDataStore

    object InMemory : RewriteOptions() {
        override fun createDataStore(index: Int) =
            MemoryDataStore()
    }

    data class Disk(val directory: Path?) : RewriteOptions() {
        override fun createDataStore(index: Int): DexDataStore {
            val fileName =
                if (index == 0) {
                    "classes.dex"
                } else {
                    "classes$index.dex"
                }
            val dir = directory
                ?: Paths.get(fileName, "cutlass")
            val output = dir.resolve(fileName)
            Files.createDirectories(output)
            return FileDataStore(output.toFile())
        }
    }
}
