package io.strlght.cutlass.analyzers

import io.strlght.cutlass.api.Analyzer
import io.strlght.cutlass.api.AnalyzerContext
import io.strlght.cutlass.api.Finding
import io.strlght.cutlass.api.annotations.ExperimentalAnalyzer
import io.strlght.cutlass.api.ext.toCutlassModel
import io.strlght.cutlass.utils.ext.cast
import io.strlght.cutlass.utils.ext.safeCast
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.iface.instruction.Instruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.MethodReference
import org.jf.dexlib2.iface.reference.StringReference
import org.jf.dexlib2.util.InstructionUtil

@ExperimentalAnalyzer
class IntrinsicsAnalyzer(context: AnalyzerContext) : Analyzer(context) {
    private val nameRegex by lazy { "[a-zA-Z_][a-zA-Z0-9_]*".toRegex() }

    private var state: State = State.Preparing
    private val candidates = mutableSetOf<Pair<String, String>>()

    override fun prepare(classDef: ClassDef) {
        when (state) {
            State.Preparing -> findPossibleMethods(classDef)
            State.AnalyzingCandidates -> filterOutCandidates(classDef)
        }
    }

    private fun filterOutCandidates(classDef: ClassDef) {
        val candidate = candidates.firstOrNull { it.first == classDef.type }
            ?: return
        val methodName = candidate.second

        classDef.methods
            .firstOrNull {
                it.name == methodName &&
                    it.parameterTypes == EXPECTED_PARAMETER_TYPES &&
                    it.returnType == EXPECTED_RETURN_TYPE
            }
            ?.also { method ->
                val isSuitable = method.implementation
                    ?.instructions
                    ?.toList()
                    ?.takeIf {
                        it.firstOrNull()?.opcode == Opcode.IF_EQZ &&
                            it.lastOrNull()?.opcode == Opcode.THROW
                    } != null
                if (!isSuitable) {
                    candidates.remove(candidate)
                }
            }
    }

    private fun findPossibleMethods(classDef: ClassDef) {
        classDef.methods.forEach { method ->
            method.prologueNpeCheckPairs
                ?.mapNotNull { (_, invokeStaticInstruction) ->
                    invokeStaticInstruction
                        .safeCast<ReferenceInstruction>()
                        ?.reference
                        ?.safeCast<MethodReference>()
                        ?.takeIf {
                            it.parameterTypes == EXPECTED_PARAMETER_TYPES &&
                                it.returnType == EXPECTED_RETURN_TYPE
                        }
                        ?.let { it.definingClass to it.name }
                }
                ?.distinct()
                ?.forEach { candidates.add(it) }
        }
    }

    override fun needsAnotherRound(): Boolean {
        return if (state is State.Preparing) {
            state = State.AnalyzingCandidates
            true
        } else {
            false
        }
    }

    override fun process(classDef: ClassDef) {
        if (candidates.isEmpty()) {
            return
        }

        classDef.methods.forEach(::processMethod)
    }

    private fun processMethod(
        method: Method
    ) {
        method.prologueNpeCheckPairs
            ?.takeWhile(::isIntrinsicNpeCheck)
            ?.forEachIndexed { index, pair ->
                processNpeCheck(method, index, pair)
            }
    }

    private fun isPossibleIntrinsicNpeCheck(pair: Pair<Instruction, Instruction>): Boolean =
        pair.first.opcode == Opcode.CONST_STRING &&
            InstructionUtil.isInvokeStatic(pair.second.opcode)

    private fun isIntrinsicNpeCheck(pair: Pair<Instruction, Instruction>): Boolean {
        val reference = pair.second
            .safeCast<ReferenceInstruction>()
            ?.reference
            ?.safeCast<MethodReference>()
        return reference?.parameterTypes == EXPECTED_PARAMETER_TYPES &&
            reference.returnType == EXPECTED_RETURN_TYPE &&
            Pair(reference.definingClass, reference.name) in candidates
    }

    private fun processNpeCheck(
        method: Method,
        index: Int,
        pair: Pair<Instruction, Instruction>
    ) {
        val parameterName = pair.first
            .cast<ReferenceInstruction>()
            .reference
            .cast<StringReference>()
            .string
        if (parameterName.matches(nameRegex)) {
            context.report(
                Finding.ParameterName(
                    method.toCutlassModel(),
                    index,
                    parameterName
                )
            )
        }
    }

    private val Method.prologueNpeCheckPairs: Sequence<Pair<Instruction, Instruction>>?
        get() = implementation
            ?.instructions
            ?.asSequence()
            ?.windowed(2, step = 2)
            ?.map { it[0] to it[1] }
            ?.takeWhile(::isPossibleIntrinsicNpeCheck)

    private sealed class State {
        object Preparing : State()
        object AnalyzingCandidates : State()
    }

    companion object {
        private val EXPECTED_PARAMETER_TYPES = listOf(
            "Ljava/lang/Object;",
            "Ljava/lang/String;"
        )
        private const val EXPECTED_RETURN_TYPE = "V"
    }
}
