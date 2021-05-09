package io.strlght.cutlass.analyzers.km

import kotlinx.metadata.KmClass
import kotlinx.metadata.KmClassifier

private fun String.toJvmType(): String =
    "L${dropWhile { it == '.' }.replace(".", "$")};"

internal val KmClassifier.Class.type: String
    get() = name.toJvmType()

internal val KmClass.type: String
    get() = name.toJvmType()
