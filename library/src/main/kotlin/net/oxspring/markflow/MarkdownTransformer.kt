package net.oxspring.markflow

/**
 * A single transformation step in a Markdown processing pipeline.
 *
 * Implementations may mutate the [MarkdownDocument.ast] in-place and return the same document, or
 * construct and return a new one — both are valid.
 */
fun interface MarkdownTransformer {
    fun transform(document: MarkdownDocument): MarkdownDocument
}
