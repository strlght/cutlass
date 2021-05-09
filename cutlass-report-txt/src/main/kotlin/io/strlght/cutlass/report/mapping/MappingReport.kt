package io.strlght.cutlass.report.mapping

import io.strlght.cutlass.api.TypeMappingVisitor
import io.strlght.cutlass.api.Finding
import io.strlght.cutlass.api.MethodMapping
import io.strlght.cutlass.api.Report
import io.strlght.cutlass.api.TypeMapping
import io.strlght.cutlass.api.types.Members
import io.strlght.cutlass.api.types.Type
import io.strlght.cutlass.utils.ext.partitionBy

class MappingReport : Report() {
    private val typeMappingRegex by lazy {
        "(.+?) -> (.+?):".toRegex()
    }
    private val memberMappingRegex by lazy {
        "\\s+(?:\\d+:\\d+:)?(.+?) (.+?)(\\(.*?\\))?(?::\\d+:\\d+)? -> (.+)".toRegex()
    }

    override fun parse(result: TypeMapping, text: String) {
        text.lines()
            .filter { it.trimStart().firstOrNull() != '#' }
            .takeIf { it.isNotEmpty() }
            ?.partitionBy(
                skipPredicateOnFirst = true,
                addLastItemToCurrentPartition = false
            ) {
                !it.startsWith(' ')
            }
            ?.flatMap { parseTypeMapping(it) }
            ?.forEach { result.process(it) }
    }

    private fun parseTypeMapping(entries: List<String>): List<Finding> {
        val result = mutableListOf<Finding>()
        val typeMapping = parseTypeMapping(entries.first())
        result.add(typeMapping)
        val root = typeMapping.type
        entries.asSequence()
            .drop(1)
            .mapTo(result) { parseMemberMapping(root, it) }
        return result
    }

    private fun parseTypeMapping(mapping: String): Finding {
        val matchGroup = typeMappingRegex.matchEntire(mapping)
            ?: throw IllegalStateException("Failed to parse mapping")
        val (_, originalType, newType) = matchGroup.groupValues
        val currentClass = Type.fromFqName(originalType)
        return Finding.ClassName(
            currentClass,
            Type.fromFqName(newType)
        )
    }

    private fun parseMemberMapping(parent: Type, mapping: String): Finding {
        val matchGroup = memberMappingRegex.matchEntire(mapping)
            ?: throw IllegalStateException("Failed to parse mapping")
        val (_, returnTypeString, oldName, parameters, newName) = matchGroup.groupValues
        val returnType = Type.fromFqName(returnTypeString)

        return if (parameters.isNotEmpty()) {
            val parametersList = parameters
                .substring(1, parameters.lastIndex)
                .takeIf(String::isNotEmpty)
                ?.split(',')
                ?.map(Type::fromFqName)
                .orEmpty()
            Finding.MethodName(
                Members.Method(
                    parent = parent,
                    name = oldName,
                    parameterTypes = parametersList,
                    returnType = returnType
                ),
                newName
            )
        } else {
            Finding.FieldName(
                Members.Field(
                    parent = parent,
                    name = oldName,
                    type = returnType
                ),
                newName
            )
        }
    }

    override fun render(typeMapping: TypeMapping): String {
        val visitor = MappingTypeMappingVisitor()
        typeMapping.accept(visitor)
        return visitor.result.joinToString("\n")
    }

    private class MappingTypeMappingVisitor : TypeMappingVisitor {
        val result = mutableListOf<String>()

        override fun visitType(type: Type, newType: Type?) {
            val printType = newType ?: type
            result.add("${type.fqName} -> ${printType.fqName}:")
        }

        override fun visitField(field: Members.Field, newName: String) {
            result.add("${PADDING}${field.type.fqName} ${field.name} -> $newName")
        }

        override fun visitMethod(method: Members.Method, mapping: MethodMapping) {
            val signature = method.parameterTypes.joinToString(",") {
                it.fqName
            }
            val textMapping = buildString {
                append(PADDING)
                append(method.returnType.fqName, ' ', method.name)
                append('(', signature, ')')
                append(" -> ", mapping.name)
            }
            result.add(textMapping)
        }

        companion object {
            private const val PADDING = "    "
        }
    }
}
