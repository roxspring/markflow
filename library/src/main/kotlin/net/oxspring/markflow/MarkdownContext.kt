package net.oxspring.markflow

import org.commonmark.Extension
import org.commonmark.ext.front.matter.YamlFrontMatterExtension
import org.commonmark.ext.front.matter.parser.RawContentParser
import org.commonmark.ext.gfm.tables.TablesExtension

/**
 * The shared configuration context for a Markdown processing pipeline.
 *
 * Owns the canonical extension list used by both [MarkdownParser] and [MarkdownFormatter],
 * ensuring they are always in sync. Use [parser] and [formatter] to obtain configured instances.
 *
 * @param extraExtensions Additional commonmark-java extensions beyond the built-in defaults
 *   ([YamlFrontMatterExtension] with [RawContentParser] and [TablesExtension]).
 */
class MarkdownContext(
    val extraExtensions: List<Extension> = emptyList(),
) {
    /** The full extension list: built-in defaults plus any [extraExtensions]. */
    val extensions: List<Extension> =
        listOf(
            YamlFrontMatterExtension.create(RawContentParser.Factory()),
            TablesExtension.create(),
        ) + extraExtensions

    /** Returns a [MarkdownParser] configured with this context's extensions. */
    fun parser(): MarkdownParser = MarkdownParser(this)

    /** Returns a [MarkdownFormatter] configured with this context's extensions. */
    fun formatter(): MarkdownFormatter = MarkdownFormatter(this)
}
