package io.strlght.cutlass.core.utils.dexlib2

import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.iface.MethodParameter
import org.jf.dexlib2.iface.reference.FieldReference
import org.jf.dexlib2.iface.reference.MethodReference
import org.jf.dexlib2.rewriter.FieldReferenceRewriter
import org.jf.dexlib2.rewriter.MethodParameterRewriter
import org.jf.dexlib2.rewriter.MethodReferenceRewriter
import org.jf.dexlib2.rewriter.MethodRewriter
import org.jf.dexlib2.rewriter.Rewriter
import org.jf.dexlib2.rewriter.RewriterModule
import org.jf.dexlib2.rewriter.Rewriters
import org.jf.dexlib2.rewriter.TypeRewriter

class StatefulRewriterModule(
    private val classNameMapper: (String) -> String?,
    private val methodNameMapper: (MethodReference) -> String?,
    private val fieldNameMapper: (FieldReference) -> String?,
    private val parameterNameMapper: (Method, Int) -> String?
) : RewriterModule() {
    private lateinit var currentClass: String
    private lateinit var currentMethod: Method
    private var currentParameterIdx: Int = 0

    override fun getTypeRewriter(rewriters: Rewriters): Rewriter<String> {
        return object : TypeRewriter() {
            override fun rewriteUnwrappedType(value: String): String {
                currentClass = value
                return classNameMapper(value) ?: value
            }
        }
    }

    override fun getMethodReferenceRewriter(rewriters: Rewriters): Rewriter<MethodReference> {
        return object : MethodReferenceRewriter(rewriters) {
            override fun rewrite(methodReference: MethodReference): MethodReference {
                val result = super.rewrite(methodReference)
                return methodNameMapper(methodReference)
                    ?.let { MethodReferenceWrapper(result, it) }
                    ?: result
            }
        }
    }

    override fun getFieldReferenceRewriter(rewriters: Rewriters): Rewriter<FieldReference> {
        return object : FieldReferenceRewriter(rewriters) {
            override fun rewrite(fieldReference: FieldReference): FieldReference {
                val result = super.rewrite(fieldReference)
                return fieldNameMapper(fieldReference)
                    ?.let { FieldReferenceWrapper(result, it) }
                    ?: result
            }
        }
    }

    override fun getMethodRewriter(rewriters: Rewriters): Rewriter<Method> {
        return object : MethodRewriter(rewriters) {
            override fun rewrite(value: Method): Method {
                currentMethod = value
                currentParameterIdx = 0
                return super.rewrite(value)
            }
        }
    }

    override fun getMethodParameterRewriter(rewriters: Rewriters): Rewriter<MethodParameter> {
        return object : MethodParameterRewriter(rewriters) {
            override fun rewrite(methodParameter: MethodParameter): MethodParameter {
                val idx = currentParameterIdx
                currentParameterIdx++
                val result = super.rewrite(methodParameter)
                return parameterNameMapper(currentMethod, idx)
                    ?.let { MethodParameterWrapper(result, it) }
                    ?: result
            }
        }
    }
}
