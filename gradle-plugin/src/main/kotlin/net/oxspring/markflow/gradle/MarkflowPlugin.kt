package net.oxspring.markflow.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Markflow Gradle plugin.
 *
 * Registers:
 * - `markflow { }` extension for project-level configuration
 * - `lintMarkdown` task, wired into the `check` lifecycle
 * - `formatMarkdown` task, not wired into any lifecycle (run explicitly to fix violations)
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
        // Resolved lazily so it respects any custom buildDir configuration.
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

        val lintTask =
            target.tasks.register("lintMarkdown", LintMarkdownTask::class.java) { task ->
                task.group = "verification"
                task.description = "Checks that all Markdown files are correctly formatted."
                task.sources.from(extension.lint.sources)
            }

        target.tasks.register("formatMarkdown", FormatMarkdownTask::class.java) { task ->
            task.group = "formatting"
            task.description = "Formats all Markdown files in-place."
            task.sources.from(extension.lint.sources)
            task.fingerprintFile.set(
                target.layout.buildDirectory.file("markflow/format.fingerprint"),
            )
        }

        // Wire lintMarkdown into the check lifecycle if the base plugin is applied.
        target.plugins.withId("base") {
            target.tasks.named("check") { it.dependsOn(lintTask) }
        }
    }
}
