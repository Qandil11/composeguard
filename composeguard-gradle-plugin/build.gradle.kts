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
    website.set("https://github.com/Qandil11/composeguard")
    vcsUrl.set("https://github.com/Qandil11/composeguard")
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
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("ComposeGuard Gradle Plugin")
            description.set("Gradle plugin for static performance and correctness checks for Jetpack Compose.")
            url.set("https://github.com/Qandil11/composeguard")
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("composeguard")
                    name.set("ComposeGuard Contributors")
                }
            }
            scm {
                connection.set("scm:git:https://github.com/Qandil11/composeguard.git")
                developerConnection.set("scm:git:ssh://git@github.com/Qandil11/composeguard.git")
                url.set("https://github.com/Qandil11/composeguard")
            }
        }
    }
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
