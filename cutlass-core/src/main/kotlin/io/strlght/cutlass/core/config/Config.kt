package io.strlght.cutlass.core.config

import java.nio.file.Path

data class Config(
    val inputType: InputType,
    val rewriteOptions: RewriteOptions? = null,
    val bytecodeApiVersion: Int = 28,
    val libraryClassesPath: Path? = null,
)
