package net.oxspring.markflow

import org.commonmark.renderer.html.HtmlRenderer
import org.commonmark.renderer.markdown.MarkdownRenderer

/**
 * Renders a [MarkdownDocument] to Markdown or HTML.
 *
 * Obtain an instance via [MarkdownContext.formatter] to ensure the extension list is consistent
 * with the [MarkdownParser] used in the same pipeline.
 *
 * **Markdown output:** [MarkdownRenderer] re-emits the `YamlFrontMatterBlock` AST node verbatim
 * (including `---` delimiters). A trailing newline is always appended (POSIX convention).
 *
 * **HTML output:** The `YamlFrontMatterBlock` node is not rendered — front matter is omitted.
 */
class MarkdownFormatter(
    context: MarkdownContext,
) {
    private val markdownRenderer: MarkdownRenderer =
        MarkdownRenderer
            .builder()
            .extensions(context.extensions)
            .build()

    private val htmlRenderer: HtmlRenderer =
        HtmlRenderer
            .builder()
            .extensions(context.extensions)
            .build()

    /** Renders [document] to a Markdown string. Front matter is preserved verbatim. */
    fun toMarkdown(document: MarkdownDocument): String = markdownRenderer.render(document.ast)

    /** Renders [document] body to an HTML string. Front matter is omitted. */
    fun toHtml(document: MarkdownDocument): String = htmlRenderer.render(document.ast)
}
