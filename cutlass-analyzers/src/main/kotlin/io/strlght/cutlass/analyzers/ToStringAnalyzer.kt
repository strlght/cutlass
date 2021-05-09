package io.strlght.cutlass.analyzers

import io.strlght.cutlass.analyzers.ext.findGroup
import io.strlght.cutlass.api.Analyzer
import io.strlght.cutlass.api.AnalyzerContext
import io.strlght.cutlass.api.Finding
import io.strlght.cutlass.api.ext.cutlassType
import io.strlght.cutlass.api.ext.toCutlassModel
import io.strlght.cutlass.api.types.Type
import io.strlght.cutlass.utils.ext.cast
import io.strlght.cutlass.utils.ext.partitionBy
import io.strlght.cutlass.utils.ext.safeCast
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.instruction.Instruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.FieldReference
import org.jf.dexlib2.iface.reference.MethodReference
import org.jf.dexlib2.iface.reference.StringReference
import java.util.EnumSet

class ToStringAnalyzer(context: AnalyzerContext) : Analyzer(context) {
    private val classNameRegex by lazy { "([a-zA-Z0-9_]+.?[a-zA-Z0-9_]+)\\s*[{(\\[]".toRegex() }
    private val nameRegex by lazy { "([a-zA-Z_][a-zA-Z0-9_]*)(\\s*[=:]\\s*)".toRegex() }

    private fun extractPossibleNames(value: String): Pair<String?, String> {
        val className = classNameRegex.findGroup(value, 1)
            ?.replace('.', '$')
        val fieldName = nameRegex.findGroup(value, 1) ?: ""
        return Pair(className, fieldName)
    }

    override fun process(classDef: ClassDef) {
        classDef.methods.asSequence()
            .filter {
                it.name == "toString" &&
                    it.parameterTypes.isEmpty() &&
                    it.returnType == "Ljava/lang/String;"
            }
            .firstOrNull()
            ?.takeIf { method ->
                // skip any toString with branching for now
                method.implementation
                    ?.instructions
                    ?.firstOrNull { it.opcode in IF_OPCODES } == null
            }
            ?.let { method ->
                method.implementation?.instructions
                    ?.partitionBy(predicate = ::isStringBuilderAppendInstruction)
                    .orEmpty()
            }
            ?.takeIf { it.size >= 2 }
            ?.also { blocks ->
                var currentName = ""
                val type = classDef.cutlassType
                blocks.forEach { block ->
                    if (block.size == 1) {
                        currentName = ""
                        return@forEach
                    }
                    val instruction = block[block.lastIndex - 1]
                    currentName =
                        if (instruction.opcode == Opcode.CONST_STRING) {
                            extractNamesFromStringReference(type, instruction)
                        } else {
                            extractFieldReference(type, instruction, currentName)
                            ""
                        }
                }
            }
    }

    private fun isStringBuilderAppendInstruction(instruction: Instruction?) =
        instruction
            ?.safeCast<ReferenceInstruction>()
            ?.takeIf {
                val reference = it.reference
                it.opcode == Opcode.INVOKE_VIRTUAL &&
                    reference is MethodReference &&
                    reference.definingClass == "Ljava/lang/StringBuilder;" &&
                    reference.name == "append"
            } != null

    private fun extractNamesFromStringReference(
        type: Type,
        instruction: Instruction
    ): String {
        val value = instruction
            .cast<ReferenceInstruction>()
            .reference
            .cast<StringReference>()
            .string
            .trim()
        val names = extractPossibleNames(value)
        names.first
            ?.let {
                Pair(type, type.matchingInner(it))
            }
            ?.takeIf {
                val original = it.first.simpleName
                val found = it.second.simpleName
                original != found &&
                    found.length > original.length &&
                    original.length <= MAX_OBFUSCATED_NAME_LENGTH
            }
            ?.also {
                context.report(
                    Finding.ClassName(
                        it.first,
                        it.second
                    )
                )
            }
        return names.second
    }

    private fun extractFieldReference(
        type: Type,
        instruction: Instruction,
        currentName: String
    ) {
        if (instruction.opcode.flags and Opcode.SETS_REGISTER != 0 &&
            instruction is ReferenceInstruction &&
            currentName.isNotEmpty()
        ) {
            instruction.reference
                .safeCast<FieldReference>()
                ?.takeIf { it.definingClass == type.value }
                ?.takeIf { it.name.length < MAX_OBFUSCATED_NAME_LENGTH && it.name.length < currentName.length }
                ?.let {
                    context.report(Finding.FieldName(it.toCutlassModel(), currentName))
                }
        }
    }

    companion object {
        val IF_OPCODES: Set<Opcode> = EnumSet.of(
            Opcode.IF_EQ,
            Opcode.IF_NE,
            Opcode.IF_LT,
            Opcode.IF_GE,
            Opcode.IF_GT,
            Opcode.IF_LE,
            Opcode.IF_EQZ,
            Opcode.IF_NEZ,
            Opcode.IF_LTZ,
            Opcode.IF_GEZ,
            Opcode.IF_GTZ,
            Opcode.IF_LEZ
        )

        const val MAX_OBFUSCATED_NAME_LENGTH = 3
    }
}
