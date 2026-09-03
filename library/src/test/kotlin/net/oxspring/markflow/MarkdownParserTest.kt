package net.oxspring.markflow

import org.assertj.core.api.Assertions.assertThat
import org.commonmark.node.Document
import org.commonmark.node.Heading
import org.commonmark.node.Paragraph
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class MarkdownParserTest {
    private val parser = MarkdownParser()

    // -------------------------------------------------------------------------
    // Return type
    // -------------------------------------------------------------------------

    @Test
    fun `parse returns a MarkdownDocument`() {
        val doc = parser.parse("# Hello")
        assertThat(doc).isInstanceOf(MarkdownDocument::class.java)
    }

    @Test
    fun `ast is a commonmark Document node`() {
        val doc = parser.parse("# Hello")
        assertThat(doc.ast).isInstanceOf(Document::class.java)
    }

    // -------------------------------------------------------------------------
    // Front matter extraction
    // -------------------------------------------------------------------------

    @Test
    fun `rawFrontMatter is null when no front matter present`() {
        val doc =
            parser.parse(
                """
                # Just a heading

                No front matter.
                """.trimIndent(),
            )
        assertThat(doc.rawFrontMatter).isNull()
    }

    @Test
    fun `rawFrontMatter contains content between delimiters without the dashes`() {
        val input =
            """
            ---
            title: Hello
            weight: 10
            ---

            Body.
            """.trimIndent()

        assertThat(parser.parse(input).rawFrontMatter).isEqualTo(
            """
            title: Hello
            weight: 10

            """.trimIndent(),
        )
    }

    @Test
    fun `rawFrontMatter is non-null when front matter present`() {
        val input =
            """
            ---
            title: Hello
            ---

            Body.
            """.trimIndent()

        assertThat(parser.parse(input).rawFrontMatter).isNotNull()
    }

    @Test
    fun `rawFrontMatter handles array values`() {
        val input =
            """
            ---
            tags:
              - kotlin
              - markdown
            ---

            Body.
            """.trimIndent()

        val frontMatter = parser.parse(input).rawFrontMatter
        assertThat(frontMatter).contains("- kotlin")
        assertThat(frontMatter).contains("- markdown")
    }

    // -------------------------------------------------------------------------
    // AST content
    // -------------------------------------------------------------------------

    @Test
    fun `AST contains heading node`() {
        val doc = parser.parse("# My Heading")
        val heading = doc.ast.firstChild
        assertThat(heading).isInstanceOf(Heading::class.java)
        assertThat((heading as Heading).level).isEqualTo(1)
    }

    @Test
    fun `AST body is unaffected by front matter`() {
        val input =
            """
            ---
            title: My Doc
            ---

            # Heading One

            Some paragraph text.
            """.trimIndent()

        val doc = parser.parse(input)
        // YamlFrontMatterExtension inserts a YamlFrontMatterBlock as the first AST child;
        // body content follows as its next sibling.
        val heading = doc.ast.firstChild?.next
        assertThat(heading).isInstanceOf(Heading::class.java)
        assertThat((heading as Heading).level).isEqualTo(1)
        val paragraph = heading.next
        assertThat(paragraph).isInstanceOf(Paragraph::class.java)
    }

    @Test
    fun `empty document produces non-null AST with no children`() {
        val doc = parser.parse("")
        assertThat(doc.ast).isNotNull()
        assertThat(doc.ast.firstChild).isNull()
        assertThat(doc.rawFrontMatter).isNull()
    }

    // -------------------------------------------------------------------------
    // File overload
    // -------------------------------------------------------------------------

    @Test
    fun `parse(File) produces same result as parse(String)`(
        @TempDir tempDir: File,
    ) {
        val content =
            """
            ---
            title: File Test
            ---

            Body content.
            """.trimIndent()
        val file = File(tempDir, "test.md").also { it.writeText(content) }

        val fromString = parser.parse(content)
        val fromFile = parser.parse(file)

        assertThat(fromFile.rawFrontMatter).isEqualTo(fromString.rawFrontMatter)
        assertThat(fromFile.ast.firstChild?.javaClass).isEqualTo(fromString.ast.firstChild?.javaClass)
    }
}
