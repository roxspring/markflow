plugins {
    id("markflow.convention")
}

dependencies {
    implementation(libs.flexmark.all)
    implementation(libs.jackson.dataformat.yaml)
    implementation(libs.jackson.module.kotlin)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
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

// TODO: remove once tests are in place (issue #4)
tasks.named("koverVerify") {
    enabled = false
}
