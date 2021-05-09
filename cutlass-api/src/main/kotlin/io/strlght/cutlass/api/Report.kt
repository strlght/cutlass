package io.strlght.cutlass.api

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText

abstract class Report {
    fun write(filePath: Path, typeMapping: TypeMapping) {
        val reportData = render(typeMapping)
        filePath.parent?.let { Files.createDirectories(it) }
        Files.write(filePath, reportData.toByteArray())
    }

    fun read(filePath: Path): TypeMapping {
        val text = filePath.readText()
        val result = DefaultTypeMapping()
        parse(result, text)
        return result
    }

    abstract fun parse(result: TypeMapping, text: String)

    abstract fun render(typeMapping: TypeMapping): String
}
