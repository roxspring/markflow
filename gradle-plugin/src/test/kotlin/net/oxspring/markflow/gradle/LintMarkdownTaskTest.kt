package net.oxspring.markflow.gradle

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class LintMarkdownTaskTest {
    private fun buildFile(projectDir: File) =
        File(projectDir, "build.gradle.kts").also {
            it.writeText(
                """
                plugins {
                    id("net.oxspring.markflow")
                }
                """.trimIndent(),
            )
        }

    private fun runner(
        projectDir: File,
        vararg args: String,
    ) = GradleRunner
        .create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments(*args)
        .forwardOutput()

    /** Canonical formatted Markdown - one blank line between blocks, trailing newline. */
    private fun formattedMd(): String {
        val nl = System.lineSeparator()
        return "# Hello" + nl + nl + "World." + nl
    }

    /** Unformatted Markdown - multiple consecutive blank lines the renderer will normalise. */
    private fun unformattedMd(): String {
        val nl = System.lineSeparator()
        return "# Hello" + nl + nl + nl + nl + "World." + nl
    }

    @Test
    fun `lintMarkdown passes when all files are already formatted`(
        @TempDir projectDir: File,
    ) {
        buildFile(projectDir)
        File(projectDir, "README.md").writeText(formattedMd())

        val result = runner(projectDir, "lintMarkdown").build()

        assertThat(result.task(":lintMarkdown")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `lintMarkdown fails when a file is not formatted`(
        @TempDir projectDir: File,
    ) {
        buildFile(projectDir)
        File(projectDir, "README.md").writeText(unformattedMd())

        val result = runner(projectDir, "lintMarkdown").buildAndFail()

        assertThat(result.task(":lintMarkdown")?.outcome).isEqualTo(TaskOutcome.FAILED)
        assertThat(result.output).contains("unformatted file")
    }

    @Test
    fun `lintMarkdown skips files in build directory`(
        @TempDir projectDir: File,
    ) {
        buildFile(projectDir)
        File(projectDir, "README.md").writeText(formattedMd())
        File(projectDir, "build/generated.md").also {
            it.parentFile.mkdirs()
            it.writeText(unformattedMd())
        }

        val result = runner(projectDir, "lintMarkdown").build()

        assertThat(result.task(":lintMarkdown")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
