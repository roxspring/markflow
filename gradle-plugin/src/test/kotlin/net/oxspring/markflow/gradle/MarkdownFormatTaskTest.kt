package net.oxspring.markflow.gradle

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class MarkdownFormatTaskTest {
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
    ): MarkdownFormatTask {
        val project = ProjectBuilder.builder().withProjectDir(projectDir).build()
        val task = project.tasks.create("markdownFormat", MarkdownFormatTask::class.java)
        task.fingerprintFile.set(File(projectDir, "build/markflow/format.fingerprint"))
        return task
    }

    @Test
    fun `writes fingerprint file after formatting`(
        @TempDir projectDir: File,
    ) {
        val task = task(projectDir)
        val file = File(projectDir, "README.md").also { it.writeText(formattedMd()) }
        task.sources.from(file)

        task.format()

        assertThat(task.fingerprintFile.get().asFile).exists()
    }

    @Test
    fun `formats unformatted file in-place`(
        @TempDir projectDir: File,
    ) {
        val task = task(projectDir)
        val file = File(projectDir, "README.md").also { it.writeText(unformattedMd()) }
        task.sources.from(file)

        task.format()

        assertThat(file.readText()).isEqualTo(formattedMd())
    }

    @Test
    fun `does not rewrite already-formatted file`(
        @TempDir projectDir: File,
    ) {
        val task = task(projectDir)
        val content = formattedMd()
        val file = File(projectDir, "README.md").also { it.writeText(content) }
        val lastModified = file.lastModified()
        task.sources.from(file)

        Thread.sleep(10)
        task.format()

        assertThat(file.lastModified()).isEqualTo(lastModified)
    }

    @Test
    fun `fingerprint contains entry for each source file`(
        @TempDir projectDir: File,
    ) {
        val task = task(projectDir)
        val a = File(projectDir, "a.md").also { it.writeText(formattedMd()) }
        val b = File(projectDir, "b.md").also { it.writeText(formattedMd()) }
        task.sources.from(a, b)

        task.format()

        val fingerprint =
            task.fingerprintFile
                .get()
                .asFile
                .readText()
        assertThat(fingerprint).contains(a.path)
        assertThat(fingerprint).contains(b.path)
    }

    @Test
    fun `creates fingerprint parent directories if absent`(
        @TempDir projectDir: File,
    ) {
        val task = task(projectDir)
        task.sources.from(File(projectDir, "README.md").also { it.writeText(formattedMd()) })

        task.format()

        assertThat(
            task.fingerprintFile
                .get()
                .asFile.parentFile,
        ).isDirectory()
    }

    @Test
    fun `passes when no sources configured`(
        @TempDir projectDir: File,
    ) {
        val task = task(projectDir)
        // empty sources — should not throw
        task.format()
        assertThat(task.fingerprintFile.get().asFile).exists()
    }
}
