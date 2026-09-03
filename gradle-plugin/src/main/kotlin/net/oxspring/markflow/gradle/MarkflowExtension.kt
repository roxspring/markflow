package net.oxspring.markflow.gradle

import org.gradle.api.Action
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory

/**
 * Project-level configuration for the Markflow plugin, accessible via the `markflow { }` block.
 *
 * ```kotlin
 * markflow {
 *     lint {
 *         sources.from(fileTree("docs"))
 *     }
 * }
 * ```
 */
abstract class MarkflowExtension(
    objects: ObjectFactory,
) {
    /** Lint configuration. */
    val lint: LintExtension = objects.newInstance(LintExtension::class.java)

    /** Configures the [lint] block. */
    fun lint(action: Action<LintExtension>) = action.execute(lint)
}

/**
 * Configuration for the `lint` block within `markflow { }`.
 */
abstract class LintExtension {
    /**
     * The set of Markdown files to lint.
     *
     * Defaults to all `*.md` files under the project directory, excluding the build directory.
     * Add or replace sources as needed:
     * ```kotlin
     * lint {
     *     sources.setFrom(fileTree("docs"))
     * }
     * ```
     */
    abstract val sources: ConfigurableFileCollection
}
