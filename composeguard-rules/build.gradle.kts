plugins {
    kotlin("jvm")
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
