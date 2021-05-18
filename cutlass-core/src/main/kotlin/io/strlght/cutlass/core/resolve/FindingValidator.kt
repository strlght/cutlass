package io.strlght.cutlass.core.resolve

import io.strlght.cutlass.api.Finding

class FindingValidator {
    companion object {
        private const val IDENTIFIER = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*"
        private val VALID_CLASSNAME_REGEX by lazy {
            "L($IDENTIFIER/)*$IDENTIFIER;".toRegex()
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
            true

        private fun validateMethod(finding: Finding.MethodName): Boolean =
            true

        private fun validateParameter(finding: Finding.ParameterName): Boolean =
            true
    }
}
