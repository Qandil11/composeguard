plugins {
    id("io.github.qandil11.composeguard")
}

composeGuard {
    failOnHigh = false
    minimumSeverity = "LOW"
    failOnSeverity = "HIGH"
    sourceDirs.set(listOf("src/main/kotlin"))
    excludes.add("generated")
}
