package io.strlght.cutlass.analyzers

import io.strlght.cutlass.analyzers.ext.accessNone
import io.strlght.cutlass.analyzers.ext.snakeToLowerCamelCase
import io.strlght.cutlass.api.Analyzer
import io.strlght.cutlass.api.AnalyzerContext
import io.strlght.cutlass.api.Finding
import io.strlght.cutlass.api.ext.toCutlassModel
import io.strlght.cutlass.api.types.Type
import io.strlght.cutlass.utils.ext.safeCast
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.iface.Annotation
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.Field
import org.jf.dexlib2.iface.value.StringEncodedValue

class FieldAnnotationAnalyzer(context: AnalyzerContext) : Analyzer(context) {
    private val nameRegex by lazy { "[a-zA-Z_][a-zA-Z0-9_]{2,}".toRegex() }

    private fun findCommonAnnotation(classDef: ClassDef): String? =
        classDef.fields
            .mapNotNull { field ->
                field
                    .takeIf {
                        it.accessNone(
                            AccessFlags.STATIC,
                            AccessFlags.TRANSIENT
                        )
                    }
                    ?.annotations
                    ?.filter { annotation ->
                        annotation.elements
                            .singleOrNull {
                                it.value is StringEncodedValue
                            } != null
                    }
                    ?.map { it.type }
            }
            .let { fieldAnnotations ->
                val set = mutableSetOf<String>()
                fieldAnnotations.forEach { annotations ->
                    set.addAll(annotations)
                }
                fieldAnnotations.forEach { annotations ->
                    set.retainAll(annotations)
                }
                set
            }
            .singleOrNull()

    override fun process(classDef: ClassDef) {
        findCommonAnnotation(classDef)
            ?.let { annotation ->
                classDef.fields
                    .asSequence()
                    .filter(::isFieldNameObfuscated)
                    .forEach { processField(it, Type(annotation), context) }
            }
    }

    private fun isFieldNameObfuscated(field: Field): Boolean =
        field.name.length < MAX_OBFUSCATED_NAME_LENGTH

    private fun processField(
        field: Field,
        annotationType: Type,
        context: AnalyzerContext
    ) {
        field.annotations
            .singleOrNull { Type(it.type) == annotationType }
            ?.let(::extractFieldNameFromAnnotation)
            ?.also {
                context.report(
                    Finding.FieldName(
                        field.toCutlassModel(),
                        it
                    )
                )
            }
    }

    private fun extractFieldNameFromAnnotation(annotation: Annotation): String? =
        annotation.elements
            .singleOrNull()
            ?.value
            ?.safeCast<StringEncodedValue>()
            ?.value
            ?.takeIf { it.matches(nameRegex) }
            ?.snakeToLowerCamelCase()

    companion object {
        const val MAX_OBFUSCATED_NAME_LENGTH = 3
    }
}
