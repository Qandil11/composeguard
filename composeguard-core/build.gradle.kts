plugins {
    kotlin("jvm")
    `maven-publish`
}

dependencies {
    implementation(kotlin("stdlib"))

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "composeguard-core"
            from(components["java"])
            pom {
                name.set("ComposeGuard Core")
                description.set("Core issue model and report generation for ComposeGuard.")
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
