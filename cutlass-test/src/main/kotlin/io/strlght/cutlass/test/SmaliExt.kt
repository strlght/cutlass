package io.strlght.cutlass.test

import org.antlr.runtime.CommonTokenStream
import org.antlr.runtime.tree.CommonTreeNodeStream
import org.jf.dexlib2.Opcodes
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.writer.builder.DexBuilder
import org.jf.smali.smaliFlexLexer
import org.jf.smali.smaliParser
import org.jf.smali.smaliTreeWalker
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStreamReader

fun InputStream.toSmaliClassDef(apiLevel: Int = 28): ClassDef {
    val dexBuilder = DexBuilder(Opcodes.forApi(apiLevel))
    val reader = InputStreamReader(this, "UTF-8")

    val lexer = smaliFlexLexer(reader, apiLevel)
    val tokens = CommonTokenStream(lexer)
    val parser = smaliParser(tokens)
    parser.setVerboseErrors(true)
    parser.setAllowOdex(false)
    parser.setApiLevel(apiLevel)
    val result = parser.smali_file()
    if (parser.numberOfSyntaxErrors > 0 ||
        lexer.numberOfSyntaxErrors > 0
    ) {
        throw IllegalStateException("Failed to parse smali file")
    }
    val t = result.tree
    val treeStream = CommonTreeNodeStream(t)
    treeStream.tokenStream = tokens
    val dexGen = smaliTreeWalker(treeStream)
    dexGen.setApiLevel(apiLevel)
    dexGen.setVerboseErrors(true)
    dexGen.setDexBuilder(dexBuilder)
    return dexGen.smali_file()
}

fun String.toSmaliClassDef(apiLevel: Int = 28): ClassDef =
    byteInputStream().use {
        it.toSmaliClassDef(apiLevel = apiLevel)
    }

fun File.toSmaliClassDef(apiLevel: Int = 28): ClassDef =
    BufferedInputStream(FileInputStream(this)).use {
        it.toSmaliClassDef(apiLevel = apiLevel)
    }
