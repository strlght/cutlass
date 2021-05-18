package io.strlght.cutlass.core.resolve

import io.strlght.cutlass.api.Finding

class FindingValidator {
    companion object {
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
            finding.newType.let { it.isClass && it.isValid }

        private fun validateField(finding: Finding.FieldName): Boolean =
            true

        private fun validateMethod(finding: Finding.MethodName): Boolean =
            true

        private fun validateParameter(finding: Finding.ParameterName): Boolean =
            true
    }
}
