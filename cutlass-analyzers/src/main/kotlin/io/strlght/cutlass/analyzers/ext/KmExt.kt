package io.strlght.cutlass.analyzers.ext

import io.strlght.cutlass.utils.ext.safeCast
import kotlinx.metadata.Flags
import kotlinx.metadata.KmClass
import kotlinx.metadata.KmConstructor
import kotlinx.metadata.KmConstructorExtensionVisitor
import kotlinx.metadata.KmFunction
import kotlinx.metadata.KmFunctionExtensionVisitor
import kotlinx.metadata.KmProperty
import kotlinx.metadata.KmPropertyExtensionVisitor
import kotlinx.metadata.impl.extensions.KmExtension
import kotlinx.metadata.jvm.JvmConstructorExtensionVisitor
import kotlinx.metadata.jvm.JvmFieldSignature
import kotlinx.metadata.jvm.JvmFunctionExtensionVisitor
import kotlinx.metadata.jvm.JvmMethodSignature
import kotlinx.metadata.jvm.JvmPropertyExtensionVisitor

internal val JvmMethodSignature.parameterTypes: List<String>
    get() {
        val raw = parameterTypesRaw
        val result = mutableListOf<String>()
        val currentType = StringBuilder()
        var idx = 0
        while (idx < raw.length) {
            val char = raw[idx]
            currentType.append(char)
            if (char == 'L') {
                val previousIdx = idx
                idx = raw.indexOf(';', startIndex = idx)
                currentType.append(raw.substring(previousIdx + 1, idx + 1))
            }
            if (char != '[') {
                result.add(currentType.toString())
                currentType.clear()
            }
            idx++
        }
        return result
    }

internal val JvmMethodSignature.parameterTypesRaw: String
    get() = desc.substring(
        desc.indexOf("(") + 1,
        desc.indexOf(")")
    )
internal val JvmMethodSignature.returnType: String
    get() = desc.substring(desc.indexOf(")") + 1)

internal val KmFunction.jvmMethodSignatures: List<JvmMethodSignature>
    get() {
        val signatures = mutableListOf<JvmMethodSignature>()
        val delegate = object : JvmFunctionExtensionVisitor() {
            override fun visit(signature: JvmMethodSignature?) {
                super.visit(signature)
                signature?.also { signatures.add(it) }
            }
        }
        visitExtensions(JvmFunctionExtensionVisitor.TYPE)
            .safeCast<KmExtension<KmFunctionExtensionVisitor>>()
            ?.accept(delegate)

        return signatures
    }

internal val KmProperty.jvmFieldSignatures: List<JvmFieldSignature>
    get() {
        val signatures = mutableListOf<JvmFieldSignature>()
        val delegate = object : JvmPropertyExtensionVisitor() {
            override fun visit(
                jvmFlags: Flags,
                fieldSignature: JvmFieldSignature?,
                getterSignature: JvmMethodSignature?,
                setterSignature: JvmMethodSignature?
            ) {
                super.visit(jvmFlags, fieldSignature, getterSignature, setterSignature)
                fieldSignature?.also { signatures.add(it) }
            }
        }
        visitExtensions(JvmPropertyExtensionVisitor.TYPE)
            .safeCast<KmExtension<KmPropertyExtensionVisitor>>()
            ?.accept(delegate)
        return signatures
    }

internal val KmProperty.jvmMethodsSignatures: List<JvmMethodSignature>
    get() {
        val signatures = mutableListOf<JvmMethodSignature>()
        val delegate = object : JvmPropertyExtensionVisitor() {
            override fun visit(
                jvmFlags: Flags,
                fieldSignature: JvmFieldSignature?,
                getterSignature: JvmMethodSignature?,
                setterSignature: JvmMethodSignature?
            ) {
                super.visit(jvmFlags, fieldSignature, getterSignature, setterSignature)
                getterSignature?.also { signatures.add(it) }
                setterSignature?.also { signatures.add(it) }
            }
        }
        visitExtensions(JvmPropertyExtensionVisitor.TYPE)
            .safeCast<KmExtension<KmPropertyExtensionVisitor>>()
            ?.accept(delegate)
        return signatures
    }

internal val KmConstructor.jvmSignatures: List<JvmMethodSignature>
    get() {
        val signatures = mutableListOf<JvmMethodSignature>()
        val delegate = object : JvmConstructorExtensionVisitor() {
            override fun visit(signature: JvmMethodSignature?) {
                super.visit(signature)
                signature?.also { signatures.add(it) }
            }
        }
        visitExtensions(JvmConstructorExtensionVisitor.TYPE)
            .safeCast<KmExtension<KmConstructorExtensionVisitor>>()
            ?.accept(delegate)
        return signatures
    }

internal val KmClass.jvmMethodsSignatures: List<JvmMethodSignature>
    get() {
        val signatures = mutableListOf<JvmMethodSignature>()
        functions.flatMapTo(signatures) {
            it.jvmMethodSignatures
        }
        properties.flatMapTo(signatures) {
            it.jvmMethodsSignatures
        }
        return signatures
    }
