plugins {
    kotlin("jvm")
    `java-gradle-plugin`
}

dependencies {
    implementation(project(":composeguard-core"))
    implementation(project(":composeguard-rules"))

    testImplementation(kotlin("test"))
    testImplementation(gradleTestKit())
}

gradlePlugin {
    plugins {
        create("composeGuard") {
            id = "io.github.composeguard"
            implementationClass = "io.github.composeguard.gradle.ComposeGuardPlugin"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
