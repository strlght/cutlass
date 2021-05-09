package io.strlght.cutlass.utils.ext

inline fun <T> Iterable<T>.partitionBy(
    skipPredicateOnFirst: Boolean = false,
    addLastItemToCurrentPartition: Boolean = true,
    crossinline predicate: (T) -> Boolean,
): List<List<T>> {
    val result = mutableListOf<List<T>>()
    var list = mutableListOf<T>()
    withIndex().forEach { (index, item) ->
        if (addLastItemToCurrentPartition) {
            list.add(item)
        }
        val shouldRunPredicate = !skipPredicateOnFirst || index != 0
        if (shouldRunPredicate && predicate(item)) {
            result.add(list)
            list = mutableListOf()
        }
        if (!addLastItemToCurrentPartition) {
            list.add(item)
        }
    }
    list.takeIf { it.isNotEmpty() }
        ?.also { result.add(it) }
    return result
}
