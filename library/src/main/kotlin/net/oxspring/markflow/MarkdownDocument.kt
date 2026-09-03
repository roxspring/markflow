package net.oxspring.markflow

import org.commonmark.ext.front.matter.YamlFrontMatterExtension
import org.commonmark.ext.front.matter.parser.RawContentParser
import org.commonmark.node.Document

/**
 * A parsed Markdown document, holding the mutable commonmark-java AST alongside the raw front
 * matter string extracted by [YamlFrontMatterExtension] with [RawContentParser].
 *
 * @property ast The mutable commonmark-java [Document] node. Transformers may modify this in-place.
 * @property rawFrontMatter The raw YAML content between the `---` delimiters, excluding the
 *   delimiters themselves, with a trailing newline. `null` when no front matter is present.
 */
data class MarkdownDocument(
    val ast: Document,
    val rawFrontMatter: String?,
)
