package io.strlght.cutlass.core.utils.concurrent

import java.io.Closeable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

fun <T> ExecutorService.async(block: () -> T): CompletableFuture<T> =
    CompletableFuture.supplyAsync({ block() }, this)

fun <T> List<CompletableFuture<T>>.await(): List<T> =
    map { it.join() }

class RewriteExecutorService(
    private val service: ExecutorService =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
) : ExecutorService by service, AutoCloseable, Closeable {
    override fun close() {
        service.shutdown()
    }
}
