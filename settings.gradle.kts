import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

plugins {
    id("org.jetbrains.intellij.platform.settings") version "2.2.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
        intellijPlatform {
            defaultRepositories()
        }
    }
}

rootProject.name = "ComposeGuard"

include(":composeguard-core")
include(":composeguard-rules")
include(":composeguard-gradle-plugin")
include(":composeguard-ide-plugin")
