package io.strlght.cutlass.analyzers

import io.strlght.cutlass.analyzers.ext.accessAll
import io.strlght.cutlass.analyzers.ext.isConstructor
import io.strlght.cutlass.analyzers.ext.isStaticConstructor
import io.strlght.cutlass.api.Analyzer
import io.strlght.cutlass.api.AnalyzerContext
import io.strlght.cutlass.api.Finding
import io.strlght.cutlass.api.ext.definingType
import io.strlght.cutlass.api.ext.toCutlassModel
import io.strlght.cutlass.api.types.Type
import io.strlght.cutlass.utils.ext.cast
import io.strlght.cutlass.utils.ext.partitionBy
import io.strlght.cutlass.utils.ext.safeCast
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.iface.instruction.Instruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.instruction.formats.Instruction21c
import org.jf.dexlib2.iface.instruction.formats.Instruction35c
import org.jf.dexlib2.iface.reference.FieldReference
import org.jf.dexlib2.iface.reference.MethodReference
import org.jf.dexlib2.iface.reference.StringReference

class EnumStaticInitAnalyzer(context: AnalyzerContext) : Analyzer(context) {
    private val nameRegex by lazy { "[a-zA-Z_][a-zA-Z0-9_]+".toRegex() }

    override fun process(classDef: ClassDef) {
        classDef.takeIf { it.accessAll(AccessFlags.ENUM) }
            ?.methods
            ?.firstOrNull { it.isStaticConstructor() }
            ?.also(::processConstructor)
    }

    private fun processConstructor(method: Method) {
        val type = method.definingType
        method.implementation?.instructions
            ?.partitionBy { isFieldPut(type, it) }
            ?.forEach { processNewInstanceBlock(type, it) }
    }

    private fun processNewInstanceBlock(
        type: Type,
        instructions: List<Instruction>
    ) {
        val register = instructions
            .lastOrNull { isEnumConstructorCall(type, it) }
            ?.safeCast<Instruction35c>()
            ?.registerD
            ?: return

        val nameInstruction = instructions
            .asSequence()
            .filter { it.opcode == Opcode.CONST_STRING }
            .filterIsInstance<Instruction21c>()
            .lastOrNull {
                val reference = it.reference
                it.registerA == register &&
                    reference is StringReference &&
                    reference.string.matches(nameRegex)
            }
            ?: return
        val putInstruction = instructions.last() as ReferenceInstruction

        val putReference = putInstruction.reference as? FieldReference
            ?: return
        val name = nameInstruction.reference.cast<StringReference>().string
        val fieldName = putReference.name
        if (name != fieldName) {
            context.report(
                Finding.FieldName(
                    putReference.toCutlassModel(),
                    name
                )
            )
        }
    }

    private fun isEnumConstructorCall(type: Type, instruction: Instruction): Boolean =
        instruction
            .takeIf { it.opcode == Opcode.INVOKE_DIRECT }
            ?.safeCast<Instruction35c>()
            ?.takeIf {
                val reference = it.reference
                reference is MethodReference &&
                    reference.definingClass == type.value &&
                    reference.isConstructor()
            } != null

    private fun isFieldPut(type: Type, instruction: Instruction): Boolean =
        instruction
            .takeIf { it.opcode == Opcode.SPUT_OBJECT }
            ?.safeCast<Instruction21c>()
            ?.takeIf {
                val reference = it.reference
                reference is FieldReference &&
                    reference.definingClass == type.value
            } != null
}
