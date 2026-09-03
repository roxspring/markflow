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
