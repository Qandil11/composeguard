import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    kotlin("jvm")
    id("org.jetbrains.intellij.platform")
}

dependencies {
    implementation(project(":composeguard-core")) {
        isTransitive = false
    }
    implementation(project(":composeguard-rules")) {
        isTransitive = false
    }

    intellijPlatform {
        intellijIdeaCommunity("2024.1.7")
        bundledPlugin("org.jetbrains.kotlin")
        testFramework(TestFrameworkType.Platform)
        pluginVerifier()
    }

    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
    projectName.set("ComposeGuard IDE")
    buildSearchableOptions.set(false)
    pluginConfiguration {
        name.set("ComposeGuard")
        version.set(project.version.toString())
        description.set("Jetpack Compose performance and correctness inspections powered by ComposeGuard.")
        ideaVersion {
            sinceBuild.set("241")
        }
        vendor {
            name.set("Qandil Tariq")
            url.set("https://github.com/Qandil11/composeguard")
        }
    }
    pluginVerification {
        ides {
            recommended()
        }
    }
}
