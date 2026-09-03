plugins {
    id("markflow.convention")
    `java-gradle-plugin`
    `maven-publish`
}

dependencies {
    implementation(project(":library"))

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testRuntimeOnly(libs.junit.platform.launcher)
}

gradlePlugin {
    plugins {
        create("markflow") {
            id = "net.oxspring.markflow"
            implementationClass = "net.oxspring.markflow.gradle.MarkflowPlugin"
            displayName = "Markflow"
            description = "Gradle plugin for Markdown processing pipelines"
        }
    }
}

publishing {
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
                    // GradleRunner functional tests run in a subprocess and are not instrumented
                    // by Kover; 80% reflects achievable in-process coverage via ProjectBuilder tests.
                    minBound(80)
                }
            }
        }
    }
}
