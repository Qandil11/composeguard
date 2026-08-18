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
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "localPluginRepository"
            url = rootProject.layout.buildDirectory.dir("local-plugin-repository").get().asFile.toURI()
        }
    }
}
