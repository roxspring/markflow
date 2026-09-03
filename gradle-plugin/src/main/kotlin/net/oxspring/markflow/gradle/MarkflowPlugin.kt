package net.oxspring.markflow.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Markflow Gradle plugin.
 *
 * Registers the `markflow { }` extension block for project-level configuration.
 * Tasks are registered separately (see `LintMarkdownTask`).
 *
 * Apply via:
 * ```kotlin
 * plugins {
 *     id("net.oxspring.markflow")
 * }
 * ```
 */
class MarkflowPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension =
            target.extensions.create("markflow", MarkflowExtension::class.java, target.objects)

        // Default lint sources: all *.md files in the project, excluding the build directory.
        // The build directory exclusion is resolved lazily so it respects any custom buildDir.
        extension.lint.sources.from(
            target.provider {
                val buildRelPath =
                    target.layout.buildDirectory
                        .get()
                        .asFile
                        .relativeTo(target.projectDir)
                        .path
                target.fileTree(target.projectDir) {
                    it.include("**/*.md")
                    it.exclude("$buildRelPath/**")
                }
            },
        )
    }
}
