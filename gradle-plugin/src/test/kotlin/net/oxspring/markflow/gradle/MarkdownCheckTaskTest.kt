package net.oxspring.markflow.gradle

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class MarkdownCheckTaskTest {
    private fun formattedMd(): String {
        val nl = "\n"
        return "# Hello" + nl + nl + "World." + nl
    }

    private fun unformattedMd(): String {
        val nl = "\n"
        return "# Hello" + nl + nl + nl + nl + "World." + nl
    }

    private fun task(
        @TempDir projectDir: File,
    ): MarkdownCheckTask {
        val project = ProjectBuilder.builder().withProjectDir(projectDir).build()
        return project.tasks.create("markdownCheck", MarkdownCheckTask::class.java)
    }

    @Test
    fun `passes when no sources configured`(
        @TempDir projectDir: File,
    ) {
        val task = task(projectDir)
        // empty sources — should not throw
        task.lint()
    }

    @Test
    fun `passes when all files are already formatted`(
        @TempDir projectDir: File,
    ) {
        val task = task(projectDir)
        val file = File(projectDir, "README.md").also { it.writeText(formattedMd()) }
        task.sources.from(file)

        task.lint()
    }

    @Test
    fun `fails with GradleException when a file is not formatted`(
        @TempDir projectDir: File,
    ) {
        val task = task(projectDir)
        val file = File(projectDir, "README.md").also { it.writeText(unformattedMd()) }
        task.sources.from(file)

        assertThatThrownBy { task.lint() }
            .isInstanceOf(GradleException::class.java)
            .hasMessageContaining("1 unformatted file")
    }

    @Test
    fun `reports all unformatted files`(
        @TempDir projectDir: File,
    ) {
        val task = task(projectDir)
        val a = File(projectDir, "a.md").also { it.writeText(unformattedMd()) }
        val b = File(projectDir, "b.md").also { it.writeText(unformattedMd()) }
        task.sources.from(a, b)

        assertThatThrownBy { task.lint() }
            .isInstanceOf(GradleException::class.java)
            .hasMessageContaining("2 unformatted file")
    }

    @Test
    fun `does not modify files`(
        @TempDir projectDir: File,
    ) {
        val task = task(projectDir)
        val content = unformattedMd()
        val file = File(projectDir, "README.md").also { it.writeText(content) }
        task.sources.from(file)

        runCatching { task.lint() }

        assertThat(file.readText()).isEqualTo(content)
    }
}
