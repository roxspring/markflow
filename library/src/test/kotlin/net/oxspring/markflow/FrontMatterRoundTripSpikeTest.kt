package net.oxspring.markflow

import org.assertj.core.api.Assertions.assertThat
import org.commonmark.ext.front.matter.YamlFrontMatterExtension
import org.commonmark.ext.front.matter.YamlFrontMatterVisitor
import org.commonmark.ext.front.matter.parser.RawContentParser
import org.commonmark.parser.Parser
import org.commonmark.renderer.markdown.MarkdownRenderer
import org.junit.jupiter.api.Test

/**
 * Spike test to empirically verify the behaviour of commonmark-java's [YamlFrontMatterExtension]
 * with [RawContentParser] before committing to it as the foundation for [MarkdownParser] (issue #4).
 *
 * Key findings documented here:
 * - [MarkdownRenderer] always appends a trailing newline — accepted as canonical (POSIX).
 * - [RawContentParser] captures content between delimiters only (no `---` markers), with a
 *   trailing newline. The [MarkdownRenderer] re-adds the `---` delimiters when rendering.
 * - [YamlFrontMatterVisitor.readRawContent] returns `""` (not null) when no front matter is present.
 * - An empty document renders as a single newline.
 */
class FrontMatterRoundTripSpikeTest {

    private val extension = YamlFrontMatterExtension.create(RawContentParser.Factory())

    private val parser = Parser.builder()
        .extensions(listOf(extension))
        .build()

    private val renderer = MarkdownRenderer.builder()
        .extensions(listOf(extension))
        .build()

    /** Renderer always appends a trailing newline — this helper makes assertions self-documenting. */
    private fun withTrailingNewline(s: String) = s + "\n"

    private fun roundTrip(input: String): String = renderer.render(parser.parse(input))

    private fun rawFrontMatter(input: String): String =
        YamlFrontMatterVisitor.readRawContent(parser.parse(input))

    private fun hasFrontMatter(input: String): Boolean = rawFrontMatter(input).isNotEmpty()

    // -------------------------------------------------------------------------
    // Round-trip fidelity — renderer always appends a trailing newline
    // -------------------------------------------------------------------------

    @Test
    fun `simple key-value front matter round-trips verbatim`() {
        val input = """
            ---
            title: Hello World
            weight: 10
            ---

            Body content here.
        """.trimIndent()

        assertThat(roundTrip(input)).isEqualTo(withTrailingNewline(input))
    }

    @Test
    fun `front matter with array values round-trips verbatim`() {
        val input = """
            ---
            title: My Doc
            tags:
              - kotlin
              - markdown
              - tooling
            ---

            Body content.
        """.trimIndent()

        assertThat(roundTrip(input)).isEqualTo(withTrailingNewline(input))
    }

    @Test
    fun `front matter with multiline literal block round-trips verbatim`() {
        val input = """
            ---
            title: My Doc
            description: |
              This is a longer description
              that spans multiple lines.
            ---

            Body content.
        """.trimIndent()

        assertThat(roundTrip(input)).isEqualTo(withTrailingNewline(input))
    }

    @Test
    fun `front matter with special characters round-trips verbatim`() {
        val input = """
            ---
            title: "Hello: World & Friends"
            subtitle: 'It''s a test'
            url: https://example.com/path?foo=bar&baz=qux
            ---

            Body content.
        """.trimIndent()

        assertThat(roundTrip(input)).isEqualTo(withTrailingNewline(input))
    }

    @Test
    fun `front matter with boolean and numeric values round-trips verbatim`() {
        val input = """
            ---
            draft: true
            weight: 42
            ratio: 3.14
            ---

            Body content.
        """.trimIndent()

        assertThat(roundTrip(input)).isEqualTo(withTrailingNewline(input))
    }

    @Test
    fun `document without front matter round-trips verbatim`() {
        val input = """
            # Heading

            Body content with no front matter.
        """.trimIndent()

        assertThat(roundTrip(input)).isEqualTo(withTrailingNewline(input))
    }

    @Test
    fun `empty document renders as single newline`() {
        assertThat(roundTrip("")).isEqualTo("\n")
    }

    @Test
    fun `fenced code blocks in body are preserved`() {
        val input = """
            ---
            title: Code Doc
            ---

            ```kotlin
            val x = 42
            println(x)
            ```
        """.trimIndent()

        assertThat(roundTrip(input)).isEqualTo(withTrailingNewline(input))
    }

    // -------------------------------------------------------------------------
    // Raw content extraction
    // -------------------------------------------------------------------------

    @Test
    fun `raw front matter contains content between delimiters without the dashes`() {
        val input = """
            ---
            title: Hello
            weight: 10
            ---

            Body.
        """.trimIndent()

        // RawContentParser captures content between --- delimiters (not including them),
        // terminated with a trailing newline
        assertThat(rawFrontMatter(input)).isEqualTo(
            """
            title: Hello
            weight: 10

            """.trimIndent(),
        )
    }

    @Test
    fun `raw front matter is empty string when absent`() {
        val input = """
            # Just a heading

            No front matter here.
        """.trimIndent()

        assertThat(rawFrontMatter(input)).isEmpty()
        assertThat(hasFrontMatter(input)).isFalse()
    }

    @Test
    fun `hasFrontMatter returns true when front matter present`() {
        val input = """
            ---
            title: Hello
            ---

            Body.
        """.trimIndent()

        assertThat(hasFrontMatter(input)).isTrue()
    }

    // -------------------------------------------------------------------------
    // Body content is unaffected
    // -------------------------------------------------------------------------

    @Test
    fun `body headings are preserved after front matter`() {
        val input = """
            ---
            title: My Doc
            ---

            # Heading One

            Some paragraph text.

            ## Heading Two

            More text.
        """.trimIndent()

        val output = roundTrip(input)
        assertThat(output).contains("# Heading One")
        assertThat(output).contains("## Heading Two")
        assertThat(output).contains("Some paragraph text.")
    }
}
