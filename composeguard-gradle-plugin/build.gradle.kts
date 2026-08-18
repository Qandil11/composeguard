plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    `maven-publish`
}

dependencies {
    implementation(project(":composeguard-core"))
    implementation(project(":composeguard-rules"))

    testImplementation(kotlin("test"))
    testImplementation(gradleTestKit())
}

gradlePlugin {
    website.set("https://github.com/composeguard/composeguard")
    vcsUrl.set("https://github.com/composeguard/composeguard")
    plugins {
        create("composeGuard") {
            id = "io.github.composeguard"
            displayName = "ComposeGuard"
            description = "Static performance checks for Jetpack Compose."
            implementationClass = "io.github.composeguard.gradle.ComposeGuardPlugin"
        }
    }
}

publishing {
    repositories {
        maven {
            name = "localPluginRepository"
            url = rootProject.layout.buildDirectory.dir("local-plugin-repository").get().asFile.toURI()
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
