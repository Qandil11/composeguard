import org.gradle.plugin.compatibility.compatibility

plugins {
    kotlin("jvm")
    id("com.gradle.plugin-publish") version "2.1.1"
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
            id = "io.github.qandil11.composeguard"
            displayName = "ComposeGuard"
            description = "Static performance and correctness checks for Jetpack Compose."
            tags.set(listOf("android", "jetpack-compose", "kotlin", "static-analysis", "performance"))
            implementationClass = "io.github.composeguard.gradle.ComposeGuardPlugin"
            compatibility {
                features {
                    configurationCache = false
                }
            }
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
