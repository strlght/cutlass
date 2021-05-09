package io.strlght.cutlass.api

import io.strlght.cutlass.api.types.Members
import io.strlght.cutlass.api.types.Type

interface TypeMapping {
    val types: Collection<Type>

    fun process(finding: Finding)

    fun findNewType(type: String): String?

    fun findNewFieldName(field: Members.Field): String?

    fun findNewMethodName(method: Members.Method): String?

    fun findNewMethodParameterName(
        method: Members.Method,
        parameterIdx: Int
    ): String?

    fun accept(typeMappingVisitor: TypeMappingVisitor)
}

class DefaultTypeMapping : TypeMapping {
    private val classMapping = mutableMapOf<Type, ClassMapping>()

    override val types: Collection<Type>
        get() = classMapping.keys

    override fun process(finding: Finding) {
        when (finding) {
            is Finding.ClassName -> {
                val type = finding.type
                val newType = finding.newType
                if (type == newType) {
                    return
                }
                val mapping = classMapping.getOrPut(type) { ClassMapping() }
                if (newType == mapping.newType) {
                    return
                }
                classMapping[type] = mapping.copy(newType = newType)
            }
            is Finding.FieldName -> {
                val classMapping = classMapping.getOrPut(finding.type) { ClassMapping() }
                classMapping.fieldMapping[finding.field] = FieldMapping(name = finding.newName)
            }
            is Finding.MethodName -> {
                val classMapping = classMapping.getOrPut(finding.type) { ClassMapping() }
                val mapping = classMapping.methodMapping.getOrPut(finding.method) { MethodMapping() }
                classMapping.methodMapping[finding.method] = mapping.copy(name = finding.newName)
            }
            is Finding.ParameterName -> {
                val classMapping = classMapping.getOrPut(finding.type) { ClassMapping() }
                val mapping = classMapping.methodMapping.getOrPut(finding.method) { MethodMapping() }
                mapping.parameters[finding.idx] = MethodParameterMapping(
                    name = finding.parameterName
                )
            }
        }
    }

    override fun findNewType(type: String): String? =
        classMapping[Type(type)]?.newType?.value

    override fun findNewFieldName(field: Members.Field): String? =
        classMapping[field.parent]
            ?.fieldMapping
            ?.get(field)
            ?.name

    override fun findNewMethodName(method: Members.Method): String? =
        classMapping[method.parent]
            ?.methodMapping
            ?.get(method)
            ?.name

    override fun findNewMethodParameterName(
        method: Members.Method,
        parameterIdx: Int
    ): String? =
        classMapping[method.parent]
            ?.methodMapping
            ?.get(method)
            ?.parameters
            ?.get(parameterIdx)
            ?.name

    override fun accept(typeMappingVisitor: TypeMappingVisitor) =
        classMapping
            .asSequence()
            .filter { (_, value) -> !value.isEmpty() }
            .forEach { (key, value) ->
                typeMappingVisitor.visitType(key, value.newType)
                value.fieldMapping
                    .forEach { (field, mapping) ->
                        typeMappingVisitor.visitField(field, mapping.name)
                    }
                value.methodMapping
                    .asSequence()
                    .filter { (_, mapping) -> mapping.name != null }
                    .forEach { (method, mapping) ->
                        typeMappingVisitor.visitMethod(method, mapping)
                    }
            }
}
