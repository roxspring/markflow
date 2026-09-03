package net.oxspring.markflow.gradle

import net.oxspring.markflow.MarkdownContext
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Formats Markdown source files in-place according to the Markflow formatter.
 *
 * A file is rewritten only if its content differs from the formatted output, minimising
 * unnecessary writes. A fingerprint of all formatted content is written to [fingerprintFile]
 * so that Gradle can detect when no files have changed and skip the task (UP-TO-DATE).
 *
 * Not a `@CacheableTask` — in-place formatters write to their own inputs and cannot participate
 * in the Gradle build cache. UP-TO-DATE checking via [fingerprintFile] provides equivalent
 * performance for repeated runs on already-formatted sources.
 *
 * Not wired into any lifecycle task — run explicitly:
 * ```
 * ./gradlew formatMarkdown
 * ```
 */
@DisableCachingByDefault(because = "Formats files in-place; writes to its own inputs so cannot participate in the build cache")
abstract class FormatMarkdownTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sources: ConfigurableFileCollection

    /**
     * Fingerprint file written to the build directory after a successful format run.
     * Its content is a hash of all formatted file contents, enabling UP-TO-DATE detection.
     */
    @get:OutputFile
    abstract val fingerprintFile: RegularFileProperty

    @TaskAction
    fun format() {
        val context = MarkdownContext()
        val parser = context.parser()
        val formatter = context.formatter()

        val fingerprint = StringBuilder()
        var formattedCount = 0

        sources.files.sorted().forEach { file ->
            val source = file.readText()
            val formatted = formatter.toMarkdown(parser.parse(source))
            if (source != formatted) {
                file.writeText(formatted)
                formattedCount++
                logger.lifecycle("Markflow: formatted {}", file.path)
            }
            fingerprint.appendLine(file.path)
            fingerprint.appendLine(formatted.hashCode())
        }

        fingerprintFile.get().asFile.also {
            it.parentFile.mkdirs()
            it.writeText(fingerprint.toString())
        }

        if (formattedCount > 0) {
            logger.lifecycle("Markflow: formatted {} file(s)", formattedCount)
        } else {
            logger.lifecycle("Markflow: all files already formatted")
        }
    }
}
