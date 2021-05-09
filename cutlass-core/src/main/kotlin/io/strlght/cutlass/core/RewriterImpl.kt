package io.strlght.cutlass.core

import io.strlght.cutlass.api.TypeMapping
import io.strlght.cutlass.api.ext.toCutlassModel
import io.strlght.cutlass.core.utils.dexlib2.StatefulRewriterModule
import org.jf.dexlib2.iface.DexFile
import org.jf.dexlib2.rewriter.DexRewriter

class RewriterImpl {
    fun rewrite(dexFile: DexFile, typeMapping: TypeMapping): DexFile {
        val rewriterModule = StatefulRewriterModule(
            { value ->
                typeMapping.findNewType(value)
            },
            { method ->
                typeMapping.findNewMethodName(method.toCutlassModel())
            },
            { field ->
                typeMapping.findNewFieldName(field.toCutlassModel())
            },
            { method, index ->
                typeMapping.findNewMethodParameterName(
                    method.toCutlassModel(),
                    index
                )
            }
        )
        val rewriter = DexRewriter(rewriterModule)
        return rewriter.dexFileRewriter.rewrite(dexFile)
    }
}
