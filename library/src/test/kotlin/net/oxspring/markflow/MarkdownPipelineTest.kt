package net.oxspring.markflow

import org.assertj.core.api.Assertions.assertThat
import org.commonmark.node.Document
import org.junit.jupiter.api.Test

class MarkdownPipelineTest {
    private fun document(frontMatter: String? = null) = MarkdownDocument(Document(), frontMatter)

    // -------------------------------------------------------------------------
    // Empty pipeline
    // -------------------------------------------------------------------------

    @Test
    fun `empty pipeline returns the document unchanged`() {
        val doc = document()
        val result = MarkdownPipeline(emptyList()).transform(doc)
        assertThat(result).isSameAs(doc)
    }

    @Test
    fun `vararg constructor with no args returns document unchanged`() {
        val doc = document()
        val result = MarkdownPipeline().transform(doc)
        assertThat(result).isSameAs(doc)
    }

    // -------------------------------------------------------------------------
    // Single transformer
    // -------------------------------------------------------------------------

    @Test
    fun `single transformer is applied`() {
        val replacement = document(frontMatter = "title: replaced")
        val pipeline = MarkdownPipeline(MarkdownTransformer { replacement })

        val result = pipeline.transform(document())
        assertThat(result).isSameAs(replacement)
    }

    // -------------------------------------------------------------------------
    // Ordering
    // -------------------------------------------------------------------------

    @Test
    fun `multiple transformers execute in declared order`() {
        val log = mutableListOf<Int>()
        val pipeline =
            MarkdownPipeline(
                MarkdownTransformer { doc ->
                    log.add(1)
                    doc
                },
                MarkdownTransformer { doc ->
                    log.add(2)
                    doc
                },
                MarkdownTransformer { doc ->
                    log.add(3)
                    doc
                },
            )

        pipeline.transform(document())
        assertThat(log).containsExactly(1, 2, 3)
    }

    @Test
    fun `each transformer receives the output of the previous one`() {
        val first = document(frontMatter = "step: first")
        val second = document(frontMatter = "step: second")

        var seenBySecond: MarkdownDocument? = null
        val pipeline =
            MarkdownPipeline(
                MarkdownTransformer { first },
                MarkdownTransformer { doc ->
                    seenBySecond = doc
                    second
                },
            )

        val result = pipeline.transform(document())
        assertThat(seenBySecond).isSameAs(first)
        assertThat(result).isSameAs(second)
    }

    // -------------------------------------------------------------------------
    // SAM / lambda construction
    // -------------------------------------------------------------------------

    @Test
    fun `MarkdownTransformer can be constructed as a lambda`() {
        val transformer: MarkdownTransformer = MarkdownTransformer { doc -> doc }
        val doc = document()
        assertThat(transformer.transform(doc)).isSameAs(doc)
    }

    @Test
    fun `pipeline can be constructed with vararg transformers`() {
        val log = mutableListOf<String>()
        val pipeline =
            MarkdownPipeline(
                MarkdownTransformer { doc ->
                    log.add("a")
                    doc
                },
                MarkdownTransformer { doc ->
                    log.add("b")
                    doc
                },
            )

        pipeline.transform(document())
        assertThat(log).containsExactly("a", "b")
    }
}
