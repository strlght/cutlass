package io.strlght.cutlass.core

import io.strlght.cutlass.api.DefaultAnalyzerContext
import io.strlght.cutlass.api.DefaultClassPool
import io.strlght.cutlass.api.DefaultTypeMapping
import io.strlght.cutlass.api.FindingResolver
import io.strlght.cutlass.api.TypeMapping
import io.strlght.cutlass.api.metrics.perormance.Stage
import io.strlght.cutlass.core.analysis.AnalyzerRunner
import io.strlght.cutlass.core.analysis.DefaultAnalyzerRunner
import io.strlght.cutlass.core.config.Config
import io.strlght.cutlass.core.result.CutlassResult
import io.strlght.cutlass.core.utils.concurrent.RewriteExecutorService
import io.strlght.cutlass.core.utils.concurrent.async
import io.strlght.cutlass.core.utils.concurrent.await
import io.strlght.cutlass.core.utils.performance.PerformanceCollector
import org.jf.dexlib2.Opcodes
import org.jf.dexlib2.iface.DexFile
import org.jf.dexlib2.writer.pool.DexPool
import java.nio.file.Files

class Pipeline(
    private val config: Config,
    private val analyzer: AnalyzerRunner = DefaultAnalyzerRunner(),
    private val analyzersLoader: AnalyzersLoader = DefaultAnalyzersLoader(),
    private val rewriteExecutor: RewriteExecutorService = RewriteExecutorService(),
    private val resolver: FindingResolver = DefaultFindingResolver(),
    private val typeMapping: TypeMapping? = null,
) : AutoCloseable {

    fun run(): CutlassResult {
        val performanceCollector = PerformanceCollector()

        val opcodes = Opcodes.forApi(config.bytecodeApiVersion)
        val dexFiles: List<DexFile> = runCatching { readInputFiles(opcodes, performanceCollector) }
            .getOrElse {
                return CutlassResult(
                    error = it,
                    timings = performanceCollector.timings
                )
            }

        val runAnalysis = typeMapping == null
        val classPool = typeMapping ?: DefaultTypeMapping()

        if (runAnalysis) {
            runCatching { analyze(dexFiles, classPool, performanceCollector) }
                .getOrElse {
                    return CutlassResult(
                        error = it,
                        timings = performanceCollector.timings
                    )
                }
        }

        runCatching { rewrite(dexFiles, classPool, performanceCollector) }
            .getOrElse {
                return CutlassResult(
                    error = it,
                    timings = performanceCollector.timings
                )
            }

        return CutlassResult(
            typeMapping = classPool,
            timings = performanceCollector.timings
        )
    }

    private fun readInputFiles(
        opcodes: Opcodes,
        performanceCollector: PerformanceCollector
    ) = performanceCollector.measure(Stage.LoadDex) {
        config.inputType
            .runCatching { provideDexFiles(opcodes) }
            .getOrThrow()
    }

    private fun rewrite(
        dexFiles: List<DexFile>,
        typeMapping: TypeMapping,
        performanceCollector: PerformanceCollector
    ) {
        performanceCollector.measure(Stage.RewriteDex) {
            config.rewriteOptions?.also { rewriteOptions ->
                val rewriter = RewriterImpl()
                val result = dexFiles.map {
                    rewriter.rewrite(it, typeMapping)
                }

                runCatching {
                    result
                        .mapIndexed { index, rewrittenDexFile ->
                            val store = rewriteOptions.createDataStore(index)
                            rewriteExecutor.async {
                                DexPool.writeTo(store, rewrittenDexFile)
                            }
                        }
                        .await()
                }.getOrThrow()
            }
        }
    }

    private fun analyze(
        dexFiles: List<DexFile>,
        typeMapping: TypeMapping,
        performanceCollector: PerformanceCollector
    ) {
        val analyzers = performanceCollector.measure(Stage.LoadAnalyzers) {
            analyzersLoader
                .runCatching {
                    val inputStream = config.libraryClassesPath
                        ?.let { Files.newInputStream(it) }
                        ?: this::class.java.classLoader.getResourceAsStream("android.jar")
                    val classPool = DefaultClassPool(inputStream)
                    load {
                        DefaultAnalyzerContext(libraryClassPool = classPool)
                    }
                }
                .getOrThrow()
        }
        if (analyzers.asSequence().distinctBy { it.id }.count() != analyzers.size) {
            throw IllegalStateException("Analyzer's ids should be unique")
        }

        val classes = dexFiles.flatMap { it.classes }

        performanceCollector.measure(Stage.Prepare) {
            analyzer
                .runCatching { prepare(analyzers, classes, performanceCollector) }
                .getOrThrow()
        }

        val findings = performanceCollector.measure(Stage.Analysis) {
            analyzer
                .runCatching { process(analyzers, classes, performanceCollector) }
                .getOrThrow()
        }

        performanceCollector.measure(Stage.Resolve) {
            resolver.resolve(classes, findings)
                .forEach {
                    typeMapping.process(it)
                }
        }
    }

    override fun close() {
        rewriteExecutor.close()
    }
}
