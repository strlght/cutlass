package io.strlght.cutlass.core.resolve

import io.strlght.cutlass.api.Finding

object FindingValidator {
    private val IDENTIFIER_REGEX by lazy {
        "[a-zA-Z0-9_$-]+".toRegex()
    }
    private val VALID_CLASSNAME_REGEX by lazy {
        "L(${IDENTIFIER_REGEX.pattern}/)*${IDENTIFIER_REGEX.pattern};".toRegex()
    }

    fun isValid(finding: Finding): Boolean {
        if (!finding.type.isClass) {
            return false
        }
        return when (finding) {
            is Finding.ClassName -> validateClass(finding)
            is Finding.FieldName -> validateField(finding)
            is Finding.MethodName -> validateMethod(finding)
            is Finding.ParameterName -> validateParameter(finding)
        }
    }

    private fun validateClass(finding: Finding.ClassName): Boolean =
        finding.newType.let { it.isClass && VALID_CLASSNAME_REGEX.matches(it.value) }

    private fun validateField(finding: Finding.FieldName): Boolean =
        IDENTIFIER_REGEX.matches(finding.newName)

    private fun validateMethod(finding: Finding.MethodName): Boolean =
        IDENTIFIER_REGEX.matches(finding.newName)

    private fun validateParameter(finding: Finding.ParameterName): Boolean =
        IDENTIFIER_REGEX.matches(finding.parameterName)
}
