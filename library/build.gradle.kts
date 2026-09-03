plugins {
    id("markflow.convention")
    `java-library`
    `maven-publish`
}

dependencies {
    implementation(libs.commonmark)
    implementation(libs.commonmark.ext.gfm.tables)
    implementation(libs.commonmark.ext.yaml.front.matter)
    implementation(libs.jackson.dataformat.yaml)
    implementation(libs.jackson.module.kotlin)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testRuntimeOnly(libs.junit.platform.launcher)
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("library") {
            from(components["java"])

            pom {
                name = "markflow"
                description = "Kotlin/JVM library for Markdown processing pipelines"
                url = "https://github.com/roxspring/markflow"
                licenses {
                    license {
                        name = "Apache-2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/roxspring/markflow.git"
                    developerConnection = "scm:git:ssh://github.com/roxspring/markflow.git"
                    url = "https://github.com/roxspring/markflow"
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/roxspring/markflow")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

kover {
    reports {
        total {
            verify {
                rule {
                    minBound(90)
                }
            }
        }
    }
}

// Local markdown check/format tasks backed by MarkdownLinterMain.
// A substitute for the Gradle plugin's own tasks until issue #38 (composite build restructure).
val markdownFiles =
    fileTree(rootDir) {
        include("**/*.md")
        exclude("**/build/**", "**/.gradle/**")
    }

tasks.register<JavaExec>("markdownCheck") {
    group = "verification"
    description = "Checks that all Markdown files in the repository are correctly formatted."
    dependsOn(tasks.named("testClasses"))
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("net.oxspring.markflow.MarkdownLinterMainKt")
    inputs.files(markdownFiles)
    outputs.file(layout.buildDirectory.file("markflow/markdown-check.txt"))
    outputs.cacheIf { true }
    args(listOf("--check") + markdownFiles.files.sorted().map { it.absolutePath })
    doLast {
        layout.buildDirectory.file("markflow/markdown-check.txt").get().asFile.apply {
            parentFile.mkdirs()
            writeText("OK\n")
        }
    }
}

tasks.named("check") {
    dependsOn("markdownCheck")
}

tasks.register<JavaExec>("markdownFormat") {
    group = "formatting"
    description = "Formats all Markdown files in the repository in-place."
    dependsOn(tasks.named("testClasses"))
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("net.oxspring.markflow.MarkdownLinterMainKt")
    args(listOf("--format") + markdownFiles.files.sorted().map { it.absolutePath })
}
