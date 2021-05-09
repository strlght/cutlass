package io.strlght.cutlass.analyzers.ext

internal fun Regex.findGroup(string: String, group: Int): String? =
    find(string)
        ?.groups
        ?.get(group)
        ?.value
