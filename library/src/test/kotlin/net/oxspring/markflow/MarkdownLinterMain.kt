package net.oxspring.markflow

import java.io.File
import kotlin.system.exitProcess

/**
 * Checks or formats Markdown files using the default [MarkdownContext].
 *
 * Usage:
 *   --check <file>...   Exit non-zero if any file is not correctly formatted.
 *   --format <file>...  Format files in-place.
 *
 * Intended for use via the `markdownCheck` and `markdownFormat` Gradle tasks in the
 * `library` module, as a local substitute until issue #38 (composite build restructure)
 * enables the Gradle plugin's own tasks to be applied directly.
 */
fun main(args: Array<String>) {
    val context = MarkdownContext()

    if (args.isEmpty()) {
        System.err.println("Usage: MarkdownLinterMain (--check|--format) <file>...")
        exitProcess(1)
    }

    val mode = args[0]
    val files = args.drop(1).map(::File)

    when (mode) {
        "--check" -> check(context, files)
        "--format" -> format(context, files)
        else -> unknown(mode)
    }
}

private fun check(
    context: MarkdownContext,
    files: List<File>,
) {
    val reformatted = reformatted(context, files)
    if (reformatted.isNotEmpty()) {
        System.err.println("The following Markdown file(s) are not correctly formatted:")
        reformatted.forEach { (file, _) -> System.err.println("  - ${file.path}") }
        System.err.println("Run ./gradlew :library:markdownFormat to fix.")
        exitProcess(1)
    }
    println("All Markdown files are correctly formatted.")
}

private fun format(
    context: MarkdownContext,
    files: List<File>,
) {
    val reformatted = reformatted(context, files)
    reformatted.forEach { (file, formatted) ->
        file.writeText(formatted)
        println("Formatted: ${file.path}")
    }
    when (reformatted.size) {
        0 -> println("All files already formatted.")
        else -> println("Formatted ${reformatted.size} file(s).")
    }
}

private fun reformatted(
    context: MarkdownContext,
    files: List<File>,
): List<Pair<File, String>> {
    val parser = context.parser()
    val formatter = context.formatter()

    return files
        .mapNotNull { file ->
            val source = file.readText()
            val formatted = formatter.toMarkdown(parser.parse(source))
            if (source != formatted) file to formatted else null
        }
}

private fun unknown(mode: String): Nothing {
    System.err.println("Unknown mode '$mode'. Expected --check or --format.")
    exitProcess(1)
}
