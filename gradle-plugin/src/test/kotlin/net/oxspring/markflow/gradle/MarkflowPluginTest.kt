package net.oxspring.markflow.gradle

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class MarkflowPluginTest {
    @Test
    fun `applying the plugin does not throw`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("net.oxspring.markflow")
    }

    @Test
    fun `markflow extension is registered`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("net.oxspring.markflow")
        assertThat(project.extensions.findByName("markflow")).isNotNull()
    }

    @Test
    fun `markflow extension is of type MarkflowExtension`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("net.oxspring.markflow")
        assertThat(project.extensions.findByType(MarkflowExtension::class.java)).isNotNull()
    }

    @Test
    fun `markdownCheck task is registered`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("net.oxspring.markflow")
        val task = project.tasks.named("markdownCheck", MarkdownCheckTask::class.java).get()
        assertThat(task.group).isEqualTo("verification")
    }

    @Test
    fun `markdownFormat task is registered`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("net.oxspring.markflow")
        val task = project.tasks.named("markdownFormat", MarkdownFormatTask::class.java).get()
        assertThat(task.group).isEqualTo("formatting")
    }

    @Test
    fun `lint sources default includes md files`(
        @TempDir projectDir: File,
    ) {
        val readme = File(projectDir, "README.md").also { it.writeText("# Hello") }
        val nested =
            File(projectDir, "docs/guide.md").also {
                it.parentFile.mkdirs()
                it.writeText("# Guide")
            }

        val project = ProjectBuilder.builder().withProjectDir(projectDir).build()
        project.plugins.apply("net.oxspring.markflow")

        val extension = project.extensions.getByType(MarkflowExtension::class.java)
        val canonicalFiles =
            extension.lint.sources.files
                .map { it.canonicalFile }
                .toSet()

        assertThat(canonicalFiles).contains(readme.canonicalFile, nested.canonicalFile)
    }

    @Test
    fun `lint sources default excludes build directory`(
        @TempDir projectDir: File,
    ) {
        val buildMd =
            File(projectDir, "build/tmp/generated.md").also {
                it.parentFile.mkdirs()
                it.writeText("# Generated")
            }

        val project = ProjectBuilder.builder().withProjectDir(projectDir).build()
        project.plugins.apply("net.oxspring.markflow")

        val extension = project.extensions.getByType(MarkflowExtension::class.java)
        val canonicalFiles =
            extension.lint.sources.files
                .map { it.canonicalFile }
                .toSet()

        assertThat(canonicalFiles).doesNotContain(buildMd.canonicalFile)
    }
}
