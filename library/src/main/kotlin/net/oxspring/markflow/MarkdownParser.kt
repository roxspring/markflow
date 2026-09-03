package net.oxspring.markflow

import org.commonmark.ext.front.matter.YamlFrontMatterVisitor
import org.commonmark.node.Document
import org.commonmark.parser.Parser
import java.io.File

/**
 * Parses Markdown source into a [MarkdownDocument].
 *
 * Obtain an instance via [MarkdownContext.parser] to ensure the extension list is consistent with
 * the [MarkdownFormatter] used in the same pipeline.
 *
 * Key behaviours (verified by `FrontMatterRoundTripSpikeTest`):
 * - Front matter is captured as a raw string (content between `---` delimiters, no delimiters
 *   included, trailing newline present).
 * - [MarkdownDocument.rawFrontMatter] is `null` when no front matter is present.
 * - Documents without front matter are unaffected.
 */
class MarkdownParser(
    context: MarkdownContext,
) {
    private val parser: Parser =
        Parser
            .builder()
            .extensions(context.extensions)
            .build()

    /** Parses a Markdown [String] into a [MarkdownDocument]. */
    fun parse(source: String): MarkdownDocument {
        val document = parser.parse(source) as Document
        val raw = YamlFrontMatterVisitor.readRawContent(document)
        return MarkdownDocument(
            ast = document,
            rawFrontMatter = raw.takeIf { it.isNotEmpty() },
        )
    }

    /** Parses a Markdown [File] into a [MarkdownDocument]. */
    fun parse(file: File): MarkdownDocument = parse(file.readText())
}
