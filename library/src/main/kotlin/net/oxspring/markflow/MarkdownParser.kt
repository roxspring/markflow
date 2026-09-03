package net.oxspring.markflow

import org.commonmark.Extension
import org.commonmark.ext.front.matter.YamlFrontMatterExtension
import org.commonmark.ext.front.matter.YamlFrontMatterVisitor
import org.commonmark.ext.front.matter.parser.RawContentParser
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.node.Document
import org.commonmark.parser.Parser
import java.io.File

/**
 * Parses Markdown formatted source into a [MarkdownDocument].
 *
 * The parser is configured with [YamlFrontMatterExtension] (using [RawContentParser] for verbatim
 * round-trip fidelity) and [TablesExtension] by default. Additional extensions may be supplied via
 * [extraExtensions].
 *
 * Key behaviours (verified by `FrontMatterRoundTripSpikeTest`):
 * - Front matter is captured as a raw string (content between `---` delimiters, no delimiters
 *   included, trailing newline present).
 * - `rawFrontMatter` is `null` when no front matter is present.
 * - Documents without front matter are unaffected.
 */
class MarkdownParser(
    extraExtensions: List<Extension> = emptyList(),
) {
    private val extensions =
        listOf(
            YamlFrontMatterExtension.create(RawContentParser.Factory()),
            TablesExtension.create(),
        ) + extraExtensions

    private val parser: Parser =
        Parser
            .builder()
            .extensions(extensions)
            .build()

    /** Parses a Markdown [String] into a [MarkdownDocument]. */
    fun parse(source: String): MarkdownDocument {
        val ast = parser.parse(source) as Document
        val raw = YamlFrontMatterVisitor.readRawContent(ast)
        return MarkdownDocument(
            ast = ast,
            rawFrontMatter = raw.takeIf { it.isNotEmpty() },
        )
    }

    /** Parses a Markdown [File] into a [MarkdownDocument]. */
    fun parse(file: File): MarkdownDocument = parse(file.readText())
}
