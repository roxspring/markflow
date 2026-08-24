rootProject.name = "markflow"

pluginManagement {
    includeBuild("convention")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include("library", "gradle-plugin")
