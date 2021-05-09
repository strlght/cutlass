package io.strlght.cutlass.test

import java.io.File

fun String.asResourceFile() =
    Unit::class.java.classLoader
        .getResource(this)!!
        .file
        .let { File(it) }

fun String.asSmaliClassDef() =
    asResourceFile().toSmaliClassDef()
