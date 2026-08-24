plugins {
    id("markflow.convention")
    `java-gradle-plugin`
}

dependencies {
    implementation(project(":library"))

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
}

gradlePlugin {
    plugins {
        create("markflow") {
            id = "net.oxspring.markflow"
            implementationClass = "net.oxspring.markflow.gradle.MarkflowPlugin"
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

// TODO: remove once tests are in place (issue #14)
tasks.named("koverVerify") {
    enabled = false
}
