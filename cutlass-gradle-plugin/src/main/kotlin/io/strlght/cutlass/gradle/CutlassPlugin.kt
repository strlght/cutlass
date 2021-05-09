package io.strlght.cutlass.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.tasks.ProguardConfigurableTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
class CutlassPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        if (!project.hasProperty("android")) {
            throw GradleException("Android plugin is required")
        }

        val extension = project.extensions.create("cutlass", CutlassExtension::class.java)

        project.afterEvaluate {
            val appExtension = project.extensions.findByType(AppExtension::class.java)
            val variants = appExtension
                ?.applicationVariants
                ?.filter { it.buildType.isMinifyEnabled }
                ?.map { it.name }
                ?.toSet()
                ?: return@afterEvaluate
            project.tasks.withType(ProguardConfigurableTask::class.java)
                .filter { it.variantName in variants }
                .forEach { _ ->
                    TODO("process task")
                }
        }
    }
}
