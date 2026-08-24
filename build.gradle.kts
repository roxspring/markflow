plugins {
    alias(libs.plugins.git.versioning)
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.kover) apply false
}

version = "0.0.0-SNAPSHOT"
group = "net.oxspring.markflow"

gitVersioning.apply {
    refs {
        tag("v(?<version>.+)") {
            version = "\${ref.version}"
        }
        branch(".+") {
            version = "\${ref}-SNAPSHOT"
        }
    }
    rev {
        version = "\${commit}-SNAPSHOT"
    }
}

subprojects {
    version = rootProject.version
    group = rootProject.group
}
