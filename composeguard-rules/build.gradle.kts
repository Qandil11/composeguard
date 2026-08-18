plugins {
    kotlin("jvm")
    `maven-publish`
}

dependencies {
    implementation(project(":composeguard-core"))
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.0.21")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "composeguard-rules"
            from(components["java"])
            pom {
                name.set("ComposeGuard Rules")
                description.set("Kotlin PSI rules for ComposeGuard.")
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
    }
    repositories {
        maven {
            name = "localPluginRepository"
            url = rootProject.layout.buildDirectory.dir("local-plugin-repository").get().asFile.toURI()
        }
    }
}
