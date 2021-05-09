package io.strlght.cutlass.analyzers.km

import io.strlght.cutlass.utils.ext.cast
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmConstructor
import kotlinx.metadata.KmFunction
import kotlinx.metadata.KmProperty
import kotlinx.metadata.KmType
import kotlinx.metadata.jvm.JvmConstructorExtensionVisitor
import kotlinx.metadata.jvm.JvmFieldSignature
import kotlinx.metadata.jvm.JvmFunctionExtensionVisitor
import kotlinx.metadata.jvm.JvmMethodSignature
import kotlinx.metadata.jvm.JvmPropertyExtensionVisitor
import java.util.Locale
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap
import kotlin.reflect.jvm.internal.impl.name.FqNameUnsafe

private fun KmClassifier.Class.toBytecodeQualifier(): String =
    when (val name = name) {
        "kotlin/Array" -> "["
        "kotlin/Unit" -> "V"
        "kotlin/Byte" -> "B"
        "kotlin/Short" -> "S"
        "kotlin/Int" -> "I"
        "kotlin/Long" -> "J"
        "kotlin/Float" -> "F"
        "kotlin/Double" -> "D"
        "kotlin/Boolean" -> "Z"
        "kotlin/Char" -> "C"
        else ->
            (JavaToKotlinClassMap.INSTANCE
                .mapKotlinToJava(FqNameUnsafe(name.replace("/", ".")))
                ?.asString()
                ?: name)
                .let { "L${it.replace(".", "$")};" }
    }

private fun KmType.toJvmType(): String {
    val classifier = classifier as KmClassifier.Class
    val qualifier = classifier.cast<KmClassifier.Class>()
        .toBytecodeQualifier()
    val argumentQualifiers = arguments
        .asSequence()
        .mapNotNull { it.type?.toJvmType() }
        .joinToString("")
    return qualifier + argumentQualifiers
}

internal fun KmProperty.applyJvmExtensions() {
    val name = name
    val type = returnType.toJvmType()
    val propertyName =
        name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    @Suppress("UNCHECKED_CAST")
    (visitExtensions(JvmPropertyExtensionVisitor.TYPE) as? JvmPropertyExtensionVisitor)
        ?.visit(
            0,
            JvmFieldSignature(name, type),
            JvmMethodSignature("get$propertyName", "()$type"),
            JvmMethodSignature("set$propertyName", "($type)V"),
        )
}

internal fun KmFunction.applyJvmExtensions() {
    val name = name
    val parameters = valueParameters.joinToString(separator = "") {
        it.type!!.toJvmType()
    }
    val returnType = returnType.toJvmType()
    @Suppress("UNCHECKED_CAST")
    (visitExtensions(JvmFunctionExtensionVisitor.TYPE) as? JvmFunctionExtensionVisitor)
        ?.visit(
            JvmMethodSignature(name, "($parameters)$returnType"),
        )
}

internal fun KmConstructor.applyJvmExtensions() {
    val parameters = valueParameters.joinToString(separator = "") {
        it.type!!.toJvmType()
    }
    @Suppress("UNCHECKED_CAST")
    (visitExtensions(JvmConstructorExtensionVisitor.TYPE) as? JvmConstructorExtensionVisitor)
        ?.visit(
            JvmMethodSignature("<init>", "($parameters)V"),
        )
}
