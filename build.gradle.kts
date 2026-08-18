plugins {
    kotlin("jvm") version "2.0.21" apply false
}

group = "io.github.composeguard"
version = "0.1.0"

subprojects {
    group = rootProject.group
    version = rootProject.version
}
