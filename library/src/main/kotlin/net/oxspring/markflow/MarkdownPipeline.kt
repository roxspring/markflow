package net.oxspring.markflow

/**
 * An ordered composition of [MarkdownTransformer] instances.
 *
 * Each transformer is applied in declaration order. An empty pipeline returns the document
 * unchanged.
 */
class MarkdownPipeline(
    private val transformers: List<MarkdownTransformer>,
) {
    constructor(vararg transformers: MarkdownTransformer) : this(transformers.toList())

    fun transform(document: MarkdownDocument): MarkdownDocument =
        transformers.fold(document) { doc, transformer -> transformer.transform(doc) }
}
