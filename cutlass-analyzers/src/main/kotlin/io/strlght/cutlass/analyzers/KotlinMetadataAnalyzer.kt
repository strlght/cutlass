package io.strlght.cutlass.analyzers

import io.strlght.cutlass.analyzers.ext.accessAll
import io.strlght.cutlass.analyzers.km.ClassNameMetadataProcessor
import io.strlght.cutlass.analyzers.km.ConstructorsMetadataProcessor
import io.strlght.cutlass.analyzers.km.FieldNamesMetadataProcessor
import io.strlght.cutlass.analyzers.km.MethodNamesMetadataProcessor
import io.strlght.cutlass.analyzers.km.SuperClassNameMetadataProcessor
import io.strlght.cutlass.api.Analyzer
import io.strlght.cutlass.api.AnalyzerContext
import io.strlght.cutlass.utils.ext.safeCast
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.iface.Annotation
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.value.ArrayEncodedValue
import org.jf.dexlib2.iface.value.EncodedValue
import org.jf.dexlib2.iface.value.StringEncodedValue

class KotlinMetadataAnalyzer(context: AnalyzerContext) : Analyzer(context) {
    internal var state: State = State.Preparing
    internal val candidates = mutableSetOf<String>()

    private val processors by lazy {
        listOf(
            ClassNameMetadataProcessor(),
            SuperClassNameMetadataProcessor(),
            FieldNamesMetadataProcessor(),
            MethodNamesMetadataProcessor(),
            ConstructorsMetadataProcessor(),
        )
    }

    private fun decodeStringArray(arrayEncodedValue: ArrayEncodedValue): Array<String> =
        arrayEncodedValue.value
            .mapNotNull { it.safeCast<StringEncodedValue>()?.value }
            .toTypedArray()

    private fun findPossibleAnnotation(classDef: ClassDef) {
        classDef.takeIf { it.accessAll(AccessFlags.ANNOTATION) }
            ?.takeIf { clsDef ->
                clsDef.virtualMethods
                    .filter {
                        it.parameterTypes.isEmpty() &&
                            it.returnType == "[Ljava/lang/String;"
                    }.count() == 2
            }
            ?.also { candidates.add(it.type) }
    }

    private fun filterOutCandidates(classDef: ClassDef) {
        classDef.annotations
            .asSequence()
            .filter { it.type in candidates }
            .forEach {
                processCandidateUsage(it)
                if (state is State.Found) {
                    return
                }
            }
    }

    private fun processCandidateUsage(annotation: Annotation) {
        val map = annotation.elements
            .filter { it.value is ArrayEncodedValue }
            .associate { it.name to decodeStringArray(it.value as ArrayEncodedValue) }
            .filterValues { it.isNotEmpty() }
            .takeIf { it.size >= 2 }

        val pbElementName = map
            ?.mapNotNull { (name, value) ->
                name.takeIf {
                    value.size == 1 &&
                        value[0].isNotEmpty() &&
                        value[0][0].code == 0
                }
            }
            ?.toList()
            ?.singleOrNull()

        val symbolsElementName = map
            ?.mapNotNull { (name, value) ->
                name.takeIf { value.size >= MAX_OBFUSCATED_NAME_LENGTH }
                    ?.takeIf {
                        value.count { it.startsWith("L") && it.endsWith(";") } >= 2
                    }
            }
            ?.toList()
            ?.singleOrNull()

        if (pbElementName != null && symbolsElementName != null) {
            state = State.Found(annotation.type, pbElementName, symbolsElementName)
            return
        }
    }

    override fun prepare(classDef: ClassDef) {
        super.prepare(classDef)
        if (state is State.Preparing) {
            findPossibleAnnotation(classDef)
        } else if (state is State.AnalyzingCandidates) {
            filterOutCandidates(classDef)
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
        if (state !is State.Found) {
            return
        }

        val annotations = classDef.annotations
        for (annotation in annotations) {
            val metadata = tryExtractMetadata(annotation)
            runCatching { metadata?.toKmClass() }
                .getOrNull()
                ?.also {
                    processors.forEach { processor ->
                        processor.process(classDef, it, context)
                    }
                }
        }
    }

    private fun tryExtractMetadata(annotation: Annotation): KotlinClassMetadata.Class? {
        val annotationData = state as? State.Found ?: return null
        if (annotation.type != annotationData.type) {
            return null
        }
        val map: MutableMap<String, EncodedValue> = hashMapOf()
        for (element in annotation.elements) {
            map[element.name] = element.value
        }
        val d1 = map[annotationData.d1]
        val d2 = map[annotationData.d2]
        if (d1 == null || d2 == null) {
            return null
        }

        val proto = decodeStringArray(d1 as ArrayEncodedValue)
        val strings = decodeStringArray(d2 as ArrayEncodedValue)
        val header = KotlinClassHeader(
            kind = KotlinClassHeader.CLASS_KIND,
            metadataVersion = KotlinClassHeader.COMPATIBLE_METADATA_VERSION,
            bytecodeVersion = KotlinClassHeader.COMPATIBLE_BYTECODE_VERSION,
            data1 = proto,
            data2 = strings,
            extraString = null,
            packageName = null,
            extraInt = null
        )
        return KotlinClassMetadata.read(header) as? KotlinClassMetadata.Class
    }

    internal sealed class State {
        object Preparing : State()
        object AnalyzingCandidates : State()
        data class Found(val type: String, val d1: String, val d2: String) : State()
    }

    companion object {
        const val MAX_OBFUSCATED_NAME_LENGTH = 3
    }
}
