package io.strlght.cutlass.api.types

import org.jf.dexlib2.util.TypeUtils

@JvmInline
value class Type(val value: String) : Comparable<Type> {
    val length get() = value.length
    val fqName get() = value.toFqName()
    val simpleName
        get() = value
            .substring(1, value.length - 1)
            .substringAfterLast('/')

    val isInner get() = simpleName.contains('$')

    private val arrayArity get() = value.indexOfFirst { it != '[' }
    val rootType
        get() =
            if (arrayArity != 0) {
                Type(value.trimStart('['))
            } else {
                this
            }

    val isClass get() = arrayArity == 0 && value.startsWith("L")

    fun replaceClassName(new: String): Type {
        val pkg = TypeUtils.getPackage(value)
        val newType = buildString {
            append("L")
            if (pkg.isNotEmpty()) {
                append(pkg, "/")
            }
            append(new, ";")
        }
        return Type(newType)
    }

    fun matchingInner(className: String): Type {
        val name = simpleName
        val innerLevel = name.count { it == '$' }
        val findingInnerLevel = className.count { it == '$' }
        val diff = innerLevel - findingInnerLevel
        if (diff <= 0) {
            return replaceClassName(className)
        }
        var replaceStart = 0
        var count = 0
        while (count < diff - findingInnerLevel) {
            replaceStart = name.indexOf('$', startIndex = replaceStart + 1)
            count++
        }
        val newClassName = name.substring(0, replaceStart) + '$' + className
        return replaceClassName(newClassName)
    }

    override fun compareTo(other: Type): Int =
        value.compareTo(other.value)

    override fun toString(): String =
        value

    companion object {
        private val PRIMITIVE_MAP = mutableMapOf(
            Type("F") to "float",
            Type("D") to "double",
            Type("Z") to "boolean",
            Type("C") to "char",
            Type("B") to "byte",
            Type("S") to "short",
            Type("I") to "int",
            Type("J") to "long",
            Type("V") to "void",
        )

        private fun String.toFqName(): String {
            val type = Type(trimStart('['))
            val arrayCount = length - type.length
            var result = PRIMITIVE_MAP[type]
                ?: type.value
                    .substring(1, type.length - 1)
                    .replace("/", ".")
            result += "[]".repeat(arrayCount)
            return result
        }

        fun fromFqName(name: String): Type {
            val type = name.trimEnd('[', ']')
            val descriptor = PRIMITIVE_MAP
                .asSequence()
                .singleOrNull { it.value == name }
                ?.value
                ?: "L${type.replace('.', '/')};"

            val arrayArity = (name.length - type.length) / 2
            return Type("[".repeat(arrayArity) + descriptor)
        }
    }
}
