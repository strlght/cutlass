package io.strlght.cutlass.analyzers.km

import io.strlght.cutlass.analyzers.ext.accessNone
import io.strlght.cutlass.analyzers.ext.jvmFieldSignatures
import io.strlght.cutlass.analyzers.ext.simplifyTypes
import io.strlght.cutlass.api.AnalyzerContext
import io.strlght.cutlass.api.Finding
import io.strlght.cutlass.api.ext.toCutlassModel
import io.strlght.cutlass.api.types.Type
import kotlinx.metadata.KmClass
import kotlinx.metadata.jvm.JvmFieldSignature
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.Field

internal class FieldNamesMetadataProcessor : KotlinMetadataProcessor {
    override fun process(
        classDef: ClassDef,
        kmClass: KmClass,
        context: AnalyzerContext
    ) {
        val classFields = classDef.fields
            .filter {
                it.accessNone(AccessFlags.SYNTHETIC, AccessFlags.STATIC)
            }
        val kmFields = kmClass.properties
            .mapNotNull {
                it.jvmFieldSignatures
                    .firstOrNull()
            }
        matchFields(kmFields, classFields, context)
    }

    private fun matchFields(
        kmFields: List<JvmFieldSignature>,
        classFields: List<Field>,
        context: AnalyzerContext
    ) {
        val kmMap = kmFields.groupBy { it.desc }
        val bytecodeMap = classFields.groupBy { it.type }
        kmMap.keys
            .filter { bytecodeMap.containsKey(it) }
            .forEach {
                val kmList = kmMap[it]!!
                val bytecodeList = bytecodeMap[it]!!
                matchAndReportField(kmList, bytecodeList, context)
            }

        val kmUnmatched = kmMap.keys.minus(bytecodeMap.keys)
        val bytecodeUnmatched = bytecodeMap.keys.minus(kmMap.keys)
        if (kmUnmatched.size == 1 && bytecodeUnmatched.size == 1) {
            val kmType = kmUnmatched.first()
            val bytecodeType = bytecodeUnmatched.first()
            val predicate = context::isLibraryType
            if (kmType.simplifyTypes(predicate) != bytecodeType.simplifyTypes(predicate)) {
                return
            }
            val kmList = kmMap[kmType]!!
            val bytecodeList = bytecodeMap[bytecodeType]!!
            matchAndReportField(kmList, bytecodeList, context)
        }
    }

    private fun matchAndReportField(
        kmList: List<JvmFieldSignature>,
        bytecodeList: List<Field>,
        context: AnalyzerContext
    ) {
        if (kmList.size == 1 && bytecodeList.size == 1) {
            val kmField = kmList.first()
            val bytecodeField = bytecodeList.first()
            if (context.isLibraryType(Type(kmField.desc)) xor
                context.isLibraryType(Type(bytecodeField.type))
            ) {
                return
            }
            if (bytecodeField.name != kmField.name) {
                context.report(
                    Finding.FieldName(
                        bytecodeField.toCutlassModel(),
                        kmField.name
                    )
                )
            }

            val bytecodeType = Type(bytecodeField.type).rootType
            val kmType = Type(kmField.desc).rootType
            if (kmType != bytecodeType) {
                context.report(
                    Finding.ClassName(
                        bytecodeType, kmType
                    )
                )
            }
        }
    }
}
