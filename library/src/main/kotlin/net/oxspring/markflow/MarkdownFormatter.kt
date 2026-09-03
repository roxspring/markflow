package net.oxspring.markflow

import org.commonmark.ext.front.matter.YamlFrontMatterExtension
import org.commonmark.ext.front.matter.parser.RawContentParser
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.renderer.html.HtmlRenderer
import org.commonmark.renderer.markdown.MarkdownRenderer

/**
 * Renders a [MarkdownDocument] to Markdown or HTML.
 *
 * Both renderers are configured with the same extensions as [MarkdownParser] by default
 * ([YamlFrontMatterExtension] with [RawContentParser] and [TablesExtension]).
 *
 * **Markdown output:** [MarkdownRenderer] re-emits the `YamlFrontMatterBlock` AST node verbatim
 * (including `---` delimiters). A trailing newline is always appended (POSIX convention).
 *
 * **HTML output:** The `YamlFrontMatterBlock` node is not rendered — front matter is omitted.
 */
class MarkdownFormatter(
    extraExtensions: List<org.commonmark.Extension> = emptyList(),
) {
    private val extensions =
        listOf(
            YamlFrontMatterExtension.create(RawContentParser.Factory()),
            TablesExtension.create(),
        ) + extraExtensions

    private val markdownRenderer: MarkdownRenderer =
        MarkdownRenderer
            .builder()
            .extensions(extensions)
            .build()

    private val htmlRenderer: HtmlRenderer =
        HtmlRenderer
            .builder()
            .extensions(extensions)
            .build()

    /** Renders [document] to a Markdown string. Front matter is preserved verbatim. */
    fun toMarkdown(document: MarkdownDocument): String = markdownRenderer.render(document.ast)

    /** Renders [document] body to an HTML string. Front matter is omitted. */
    fun toHtml(document: MarkdownDocument): String = htmlRenderer.render(document.ast)
}
