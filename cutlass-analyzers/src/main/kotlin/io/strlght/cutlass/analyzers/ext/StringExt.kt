package io.strlght.cutlass.analyzers.ext

import io.strlght.cutlass.api.types.Type
import java.util.Locale

private val snakeRegex = "_[a-zA-Z]".toRegex()

internal fun String.snakeToLowerCamelCase(): String =
    snakeRegex.replace(replaceFirstChar { it.lowercase(Locale.getDefault()) }) {
        it.value.replace("_", "")
            .uppercase(Locale.getDefault())
    }

internal fun String.simplifyTypes(
    keepPredicate: ((Type) -> Boolean)?,
): String {
    val result = StringBuilder()
    val currentType = StringBuilder()
    var idx = 0
    while (idx < length) {
        val char = this[idx]
        currentType.append(char)
        if (char == 'L') {
            val previousIdx = idx
            idx = indexOf(';', startIndex = idx)

            if (keepPredicate != null) {
                val type = substring(previousIdx + 1, idx + 1)
                type.takeIf { keepPredicate(Type(it)) }
                    ?.also { currentType.append(it) }
            }
        }
        if (char != '[') {
            result.append(currentType.toString())
            currentType.clear()
        }
        idx++
    }
    return result.toString()
}
