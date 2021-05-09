package io.strlght.cutlass.utils.ext

inline fun <reified U> Any.cast(): U =
    this as U

inline fun <reified U> Any.safeCast(): U? =
    this as? U
