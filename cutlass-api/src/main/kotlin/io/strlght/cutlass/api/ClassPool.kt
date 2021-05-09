package io.strlght.cutlass.api

import io.strlght.cutlass.api.types.Type
import java.io.InputStream
import java.util.jar.JarInputStream

interface ClassPool {
    operator fun contains(type: Type): Boolean
}

class EmptyClassPool : ClassPool {
    override fun contains(type: Type): Boolean =
        false
}

class DefaultClassPool(inputStream: InputStream) : ClassPool {
    private val types: Set<Type> =
        JarInputStream(inputStream).use { jar ->
            val result = mutableSetOf<Type>()
            while (true) {
                val entry = jar.nextJarEntry ?: break
                val name = entry.name
                if (name.endsWith(".class")) {
                    result.add(Type("L${name.removeSuffix(".class")};"))
                }
            }
            result
        }

    override fun contains(type: Type): Boolean =
        type in types
}
