package net.oxspring.markflow

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MarkdownFormatterTest {
    private val parser = MarkdownParser()
    private val formatter = MarkdownFormatter()

    private fun parse(input: String) = parser.parse(input.trimIndent())

    // -------------------------------------------------------------------------
    // toMarkdown — round-trip fidelity
    // -------------------------------------------------------------------------

    @Test
    fun `toMarkdown round-trips document with front matter`() {
        val input =
            """
            ---
            title: Hello World
            weight: 10
            ---

            Body content here.
            """.trimIndent()
        // MarkdownRenderer always appends a trailing newline (verified by FrontMatterRoundTripSpikeTest)
        assertThat(formatter.toMarkdown(parse(input))).startsWith(input)
    }

    @Test
    fun `toMarkdown round-trips document without front matter`() {
        val input =
            """
            # Heading

            Body content with no front matter.
            """.trimIndent()
        assertThat(formatter.toMarkdown(parse(input))).startsWith(input)
    }

    @Test
    fun `toMarkdown preserves front matter with array values`() {
        val input =
            """
            ---
            tags:
              - kotlin
              - markdown
            ---

            Body.
            """.trimIndent()
        val output = formatter.toMarkdown(parse(input))
        assertThat(output).contains("tags:")
        assertThat(output).contains("- kotlin")
        assertThat(output).contains("- markdown")
    }

    @Test
    fun `toMarkdown always appends trailing newline`() {
        val output = formatter.toMarkdown(parse("# Hello"))
        // MarkdownRenderer always appends a trailing newline (verified by FrontMatterRoundTripSpikeTest)
        assertThat(output.trimEnd().length).isLessThan(output.length)
    }

    // -------------------------------------------------------------------------
    // toHtml — front matter omitted, body rendered
    // -------------------------------------------------------------------------

    @Test
    fun `toHtml renders body as HTML`() {
        val input =
            """
            # Heading

            A paragraph.
            """.trimIndent()
        val html = formatter.toHtml(parse(input))
        assertThat(html).contains("<h1>Heading</h1>")
        assertThat(html).contains("<p>A paragraph.</p>")
    }

    @Test
    fun `toHtml omits front matter from output`() {
        val input =
            """
            ---
            title: Secret Title
            weight: 42
            ---

            Body content.
            """.trimIndent()
        val html = formatter.toHtml(parse(input))
        assertThat(html).doesNotContain("---")
        assertThat(html).doesNotContain("title: Secret Title")
        assertThat(html).doesNotContain("weight: 42")
    }

    @Test
    fun `toHtml handles document without front matter`() {
        val input =
            """
            # Hello

            No front matter.
            """.trimIndent()
        val html = formatter.toHtml(parse(input))
        assertThat(html).contains("<h1>Hello</h1>")
        assertThat(html).doesNotContain("---")
    }

    @Test
    fun `toHtml renders GFM table when TablesExtension active`() {
        val input =
            """
            | A | B |
            |---|---|
            | 1 | 2 |
            """.trimIndent()
        val html = formatter.toHtml(parse(input))
        assertThat(html).contains("<table>")
        assertThat(html).contains("<td>1</td>")
    }
}
