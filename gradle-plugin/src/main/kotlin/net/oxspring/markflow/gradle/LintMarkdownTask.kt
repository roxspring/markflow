package net.oxspring.markflow.gradle

import net.oxspring.markflow.MarkdownContext
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Checks that all Markdown source files are formatted according to the Markflow formatter.
 *
 * A file is considered a violation if its content differs from the output of parsing it and
 * re-rendering via [net.oxspring.markflow.MarkdownFormatter.toMarkdown]. No files are modified.
 *
 * Wired into the `check` lifecycle by the [MarkflowPlugin]. Run [FormatMarkdownTask] to fix
 * violations automatically.
 */
@CacheableTask
abstract class LintMarkdownTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sources: ConfigurableFileCollection

    @TaskAction
    fun lint() {
        val context = MarkdownContext()
        val parser = context.parser()
        val formatter = context.formatter()

        val violations = mutableListOf<String>()

        sources.files.sorted().forEach { file ->
            val source = file.readText()
            val formatted = formatter.toMarkdown(parser.parse(source))
            if (source != formatted) {
                violations += file.path
                logger.warn("Markflow: {} is not formatted", file.path)
            }
        }

        if (violations.isNotEmpty()) {
            throw GradleException(
                "Markflow lint found ${violations.size} unformatted file(s). " +
                    "Run the 'formatMarkdown' task to fix.",
            )
        }
    }
}
