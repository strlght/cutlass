package io.strlght.cutlass.analyzers

import io.strlght.cutlass.analyzers.ext.accessAll
import io.strlght.cutlass.api.Analyzer
import io.strlght.cutlass.api.AnalyzerContext
import io.strlght.cutlass.api.Finding
import io.strlght.cutlass.api.ext.toCutlassModel
import io.strlght.cutlass.api.types.Type
import io.strlght.cutlass.utils.ext.cast
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.ReferenceType
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.iface.instruction.Instruction
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.instruction.formats.Instruction11x
import org.jf.dexlib2.iface.instruction.formats.Instruction12x
import org.jf.dexlib2.iface.instruction.formats.Instruction21c
import org.jf.dexlib2.iface.instruction.formats.Instruction35c
import org.jf.dexlib2.iface.reference.FieldReference
import org.jf.dexlib2.iface.reference.MethodReference
import org.jf.dexlib2.iface.reference.Reference
import org.jf.dexlib2.iface.reference.StringReference
import java.util.EnumSet
import java.util.Locale

class IntrinsicsAnalyzer(context: AnalyzerContext) : Analyzer(context) {
    /*
        This regex is plain and simple but it doesn't really cover all possible cases.
        Examples of expressions that won't match:
            * Entity().method()
            * BigInteger(130, SecureRandom()).toString(32)
            * Entity.method1().method2()
            * Observable.just(emptyList())

        There are 2 solutions to this:
            1. Parse entire intrinsic string argument with KotlinParser (or something similar)
            2. Improve regex to cover these cases
     */
    private val intrinsicRegex by lazy {
        ("^([a-zA-Z_][a-zA-Z0-9_]+(\\[\\d+])?\\.)?" + // entity
            "([a-zA-Z_][a-zA-Z0-9_]+)(!!)?" + // method
            "(<[a-zA-Z_][a-zA-Z0-9_]+>)?(\\([^)]*\\))?$" // params
            ).toRegex()
    }
    private val candidates = mutableSetOf<Pair<String, String>>()

    override fun prepare(classDef: ClassDef) {
        classDef.methods.forEach { method ->
            method
                .takeIf {
                    it.accessAll(AccessFlags.STATIC) &&
                        it.parameterTypes == EXPECTED_PARAMETER_TYPES &&
                        it.returnType == EXPECTED_RETURN_TYPE
                }
                ?.implementation
                ?.instructions
                ?.any {
                    it is ReferenceInstruction &&
                        it.opcode == Opcode.CONST_STRING &&
                        (it.reference as? StringReference)?.string == " must not be null"
                }
                ?.takeIf { it }
                ?.also {
                    candidates.add(Pair(classDef.type, method.name))
                }
        }
    }

    override fun process(classDef: ClassDef) {
        classDef.methods.forEach(::process)
    }

    fun process(method: Method) {
        method.implementation
            ?.instructions
            ?.toList()
            ?.also(::process)
    }

    fun process(instructions: List<Instruction>) {
        instructions.forEachIndexed { index, instruction ->
            if (instruction.opcode != Opcode.INVOKE_STATIC) {
                return@forEachIndexed
            }
            val invokeInstruction = instruction as? Instruction35c ?: return@forEachIndexed
            val reference = instruction.reference as? MethodReference ?: return@forEachIndexed
            if (Pair(reference.definingClass, reference.name) in candidates &&
                reference.returnType == EXPECTED_RETURN_TYPE &&
                reference.parameterTypes == EXPECTED_PARAMETER_TYPES
            ) {
                process(instructions, index, invokeInstruction)
            }
        }
    }

    private fun process(
        instructions: List<Instruction>,
        intrinsicIndex: Int,
        intrinsicInstruction: Instruction35c
    ) {
        val valueRegister = intrinsicInstruction.registerC
        val stringRegister = intrinsicInstruction.registerD
        val stringValue = backtrackStringArgument(intrinsicIndex, instructions, stringRegister) ?: return
        if (stringValue.contains('\u2026') ||
            stringValue == "it" ||
            stringValue == "this"
        ) {
            return
        }
        val valueInstruction = backtrackValueInstruction(intrinsicIndex, instructions, valueRegister) ?: return
        processResult(stringValue, valueInstruction)
    }

    @Suppress("MagicNumber")
    private fun processResult(stringValue: String, valueInstruction: ReferenceInstruction) {
        val result = intrinsicRegex.matchEntire(
            stringValue
        ) ?: return
        val reference = valueInstruction.reference

        val isLibraryClass = valueInstruction.definingType
            ?.let { context.isLibraryType(Type(it)) }
            ?: false
        if (isLibraryClass) {
            return
        }

        val entityName = result.groups[1]?.value?.dropLast(1)
        val methodName = result.groups[3]?.value ?: return

        val isArray = result.groups[2] != null
        val isFunction = result.groups[6] != null
        if (entityName != null || isFunction) {
            if (!isArray) {
                reportTypeFinding(valueInstruction, entityName)
            }
            reportFieldFinding(reference, methodName)
            reportMethodFinding(reference, isFunction, methodName)
        }
    }

    private fun reportMethodFinding(
        reference: Reference,
        isFunction: Boolean,
        methodName: String,
    ) {
        if (reference is MethodReference) {
            val reportedName = convertToFunctionName(isFunction, methodName)
            if (reportedName != reference.name) {
                context.report(Finding.MethodName(reference.toCutlassModel(), reportedName))
            }
        }
    }

    private fun reportTypeFinding(
        valueInstruction: ReferenceInstruction,
        entityName: String?,
    ) {
        val isStatic = valueInstruction.opcode == Opcode.INVOKE_STATIC ||
            valueInstruction.opcode == Opcode.SGET_OBJECT
        if (isStatic &&
            entityName != null &&
            entityName !in INVALID_ENTITY_NAMES
        ) {
            val definingType = valueInstruction.definingType ?: return
            val type = Type(definingType)
            if (type.simpleName != entityName) {
                context.report(Finding.ClassName(type, type.replaceClassName(entityName)))
            }
        }
    }

    private fun convertToFunctionName(isFunction: Boolean, baseName: String) =
        buildString {
            if (!isFunction) {
                append("get")
                val name = baseName.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }
                append(name)
            } else {
                append(baseName)
            }
        }

    private fun reportFieldFinding(reference: Reference, methodName: String) {
        if (reference is FieldReference && reference.name != methodName) {
            context.report(Finding.FieldName(reference.toCutlassModel(), methodName))
        }
    }

    private fun backtrackValueInstruction(
        intrinsicIndex: Int,
        instructions: List<Instruction>,
        valueRegister: Int
    ): ReferenceInstruction? {
        var register = valueRegister
        for (index in intrinsicIndex downTo 0) {
            val instruction = instructions[index]
            if (instruction.opcode == Opcode.MOVE_RESULT_OBJECT &&
                instruction is Instruction11x &&
                instruction.registerA == register
            ) {
                return instructions[index - 1] as? ReferenceInstruction
            } else if (instruction.opcode == Opcode.MOVE_OBJECT &&
                instruction is Instruction12x &&
                instruction.registerA == register
            ) {
                register = instruction.registerB
            } else if (instruction.opcode in OBJECT_OPCODES &&
                instruction is OneRegisterInstruction &&
                instruction.registerA == register
            ) {
                return instruction as ReferenceInstruction
            }
        }
        return null
    }

    private fun backtrackStringArgument(
        intrinsicIndex: Int,
        instructions: List<Instruction>,
        stringRegister: Int,
    ): String? {
        for (index in intrinsicIndex.downTo(0)) {
            val instruction = instructions[index]
            if (instruction is Instruction21c &&
                instruction.opcode == Opcode.CONST_STRING &&
                instruction.registerA == stringRegister
            ) {
                return instruction.reference
                    .cast<StringReference>()
                    .string
            }
        }
        return null
    }

    private val ReferenceInstruction.definingType: String?
        get() = when (referenceType) {
            ReferenceType.FIELD -> {
                reference.cast<FieldReference>().definingClass
            }
            ReferenceType.METHOD -> {
                reference.cast<MethodReference>().definingClass
            }
            else -> {
                null
            }
        }

    companion object {
        private val EXPECTED_PARAMETER_TYPES = listOf(
            "Ljava/lang/Object;",
            "Ljava/lang/String;"
        )
        private const val EXPECTED_RETURN_TYPE = "V"

        private val OBJECT_OPCODES = EnumSet.of(Opcode.IGET_OBJECT, Opcode.SGET_OBJECT)
        private val INVALID_ENTITY_NAMES = setOf("this", "it")
    }
}
