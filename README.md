# ComposeGuard

Static performance checks for Jetpack Compose.

## Current scope

Phase 1 implemented only **CG001: Missing Lazy List Keys** to prove the full chain before adding more rules.

Phase 2 adds:

- **CG002: Collection transformation during composition** for direct composable-body property initializers such as `val sorted = products.sortedBy { ... }`.
- **CG003: Mutable collection stored in Compose state** for `mutableStateOf(mutableListOf(...))`, `mutableStateOf(ArrayList(...))`, and similar mutable collection factories/types.
- **CG004: State written during composition** for obvious body-level writes to state created by `remember { mutableStateOf(...) }`, while skipping writes inside lambdas/effects/event handlers.

CG005 is intentionally not implemented yet.

Verified chain:

1. Compose/Kotlin source is read from Gradle source directories.
2. The analyser parses Kotlin with Kotlin PSI from `kotlin-compiler-embeddable`.
3. Rules CG001-CG004 run as independent Kotlin PSI-based checks.
4. The `composeGuard` Gradle task prints and writes a report.
5. Tests cover rule detection, negative cases, report output, and Gradle task build policy.

## Architecture decision

The MVP uses **Kotlin PSI directly** rather than Android Lint, Detekt, or KtLint.

Why:

- ComposeGuard should be a standalone developer tool, not an Android app or Android-runtime-dependent library.
- Kotlin PSI gives direct access to Kotlin call expressions and named arguments, which is enough for a conservative first rule.
- Detekt and KtLint are good future integration targets, but starting with their rule engines would make ComposeGuard inherit their lifecycle and plugin APIs before the core issue model is proven.
- Android Lint is powerful for Android projects, but it is heavier and less aligned with the requested Gradle-plugin-plus-analysis-engine shape for this MVP.

## Run

```bash
./gradlew test
```

The end-to-end Gradle plugin proof is in:

```text
composeguard-gradle-plugin/src/test/kotlin/io/github/composeguard/gradle/ComposeGuardPluginFunctionalTest.kt
```

## Configuration

```kotlin
composeGuard {
    // Backward-compatible build policy switch. Set false to report without failing.
    failOnHigh = true

    // Build fails when failOnHigh is true and an issue is at or above this severity.
    failOnSeverity = "HIGH"

    // Issues below this severity are omitted from the report.
    minimumSeverity = "LOW"

    sourceDirs.set(listOf("src/main/kotlin"))
    excludes.add("generated")
}
```

Severity values are `HIGH`, `MEDIUM`, and `LOW`.

## Local Consumption

For local development, an external project can consume ComposeGuard through the plugins DSL with an included build:

```kotlin
// settings.gradle.kts
pluginManagement {
    includeBuild("../composeguard")
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}
```

```kotlin
// build.gradle.kts
plugins {
    id("io.github.composeguard")
}
```

The `sample/` project uses this path.

## Local Publishing

Publish plugin artifacts and the Gradle plugin marker to a local test repository:

```bash
./gradlew publishAllPublicationsToLocalPluginRepositoryRepository
```

The repository is written to:

```text
build/local-plugin-repository
```

External projects can then use:

```kotlin
pluginManagement {
    repositories {
        maven("path/to/composeguard/build/local-plugin-repository")
        gradlePluginPortal()
    }
}
```

```kotlin
plugins {
    id("io.github.composeguard") version "0.1.0-SNAPSHOT"
}
```
