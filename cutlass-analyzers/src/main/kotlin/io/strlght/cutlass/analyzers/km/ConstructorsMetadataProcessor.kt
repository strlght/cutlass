package io.strlght.cutlass.analyzers.km

import io.strlght.cutlass.analyzers.ext.isConstructor
import io.strlght.cutlass.analyzers.ext.jvmSignatures
import io.strlght.cutlass.analyzers.ext.parameterTypes
import io.strlght.cutlass.analyzers.ext.parameterTypesRaw
import io.strlght.cutlass.analyzers.ext.returnType
import io.strlght.cutlass.analyzers.ext.simplifyTypes
import io.strlght.cutlass.api.AnalyzerContext
import io.strlght.cutlass.api.Finding
import io.strlght.cutlass.api.types.Type
import kotlinx.metadata.KmClass
import kotlinx.metadata.jvm.JvmMethodSignature
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.Method

internal class ConstructorsMetadataProcessor : KotlinMetadataProcessor {
    override fun process(
        classDef: ClassDef,
        kmClass: KmClass,
        context: AnalyzerContext
    ) {
        val classMethods = classDef.methods
            .filter { it.isConstructor(withStatic = false) }
        val kmMethods = kmClass.constructors.flatMap { it.jvmSignatures }
        matchMethods(classMethods, kmMethods, context)
    }

    private fun matchMethods(
        classMethods: List<Method>,
        kmMethods: List<JvmMethodSignature>,
        context: AnalyzerContext
    ) {
        val predicate = context::isLibraryType
        val kmMap = kmMethods.groupBy {
            val sb = StringBuilder("(")
            sb.append(
                it.parameterTypesRaw.simplifyTypes(predicate)
            )
            sb.append(
                ")", it.returnType.simplifyTypes(predicate)
            )
            sb.toString()
        }
        val bytecodeMap = classMethods.groupBy {
            val sb = StringBuilder("(")
            sb.append(
                it.parameterTypes.joinToString("").simplifyTypes(predicate)
            )
            sb.append(
                ")", it.returnType.simplifyTypes(predicate)
            )
            sb.toString()
        }
        kmMap.keys
            .filter { bytecodeMap.containsKey(it) }
            .forEach {
                val kmList = kmMap[it]!!
                val bytecodeList = bytecodeMap[it]!!
                matchAndReportMethod(kmList, bytecodeList, context)
            }
    }

    private fun matchAndReportMethod(
        kmList: List<JvmMethodSignature>,
        bytecodeList: List<Method>,
        context: AnalyzerContext
    ) {
        val kmMethod = kmList.singleOrNull()
        val bytecodeMethod = bytecodeList.singleOrNull()

        if (kmMethod == null || bytecodeMethod == null) {
            return
        }

        bytecodeMethod.parameterTypes
            .map { it.toString() }
            .zip(kmMethod.parameterTypes) { bytecodeType, kmType ->
                Type(bytecodeType).rootType to Type(kmType).rootType
            }
            .filter {
                it.first != it.second &&
                    !context.isLibraryType(it.first) &&
                    it.first.length > 1
            }
            .forEach {
                context.report(
                    Finding.ClassName(
                        it.first,
                        it.second
                    )
                )
            }
    }
}
