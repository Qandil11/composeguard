# ComposeGuard

[![CI](https://github.com/Qandil11/composeguard/actions/workflows/ci.yml/badge.svg)](https://github.com/Qandil11/composeguard/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/Qandil11/composeguard)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Version](https://img.shields.io/badge/version-0.1.0-blue)](CHANGELOG.md)

Static performance and correctness checks for Jetpack Compose.

ComposeGuard is a Gradle plugin that scans Kotlin source for common Jetpack Compose performance and state-management problems before code is merged. It is intentionally small for `v0.1.0`: four deterministic Kotlin PSI-based rules, console and JSON reports, source exclusions, and configurable build failure policy.

ComposeGuard does not depend on the Android runtime and does not use Android Lint or compiler-plugin infrastructure yet.

## Installation

ComposeGuard `v0.1.0` is available on the Gradle Plugin Portal.

Add the plugin to your project:

```kotlin
plugins {
    id("io.github.qandil11.composeguard") version "0.1.0"
}

composeGuard {
    failOnHigh.set(true)
}
```

For local development from a checked-out ComposeGuard repository:

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
    id("io.github.qandil11.composeguard")
}
```

## Run

```bash
./gradlew composeGuard
```

Reports are written to:

```text
build/reports/composeguard/composeguard.txt
build/reports/composeguard/composeguard.json
```

## Example Finding

```text
CG001 HIGH
ProductList.kt:21

Missing stable key in LazyColumn.

Detected:
items(products) { ... }

Consider:
items(
    items = products,
    key = { it.id }
) { product ->
    ...
}
```

## Rules

| Rule | Severity | Description |
| --- | --- | --- |
| CG001 | HIGH | Detects `LazyColumn`/`LazyRow` `items(...)` and `itemsIndexed(...)` calls without a stable `key` argument. |
| CG002 | MEDIUM | Detects direct collection transformations in composable body property initializers, such as `val sorted = products.sortedBy { ... }`. |
| CG003 | HIGH | Detects mutable collections stored inside `mutableStateOf(...)`. |
| CG004 | HIGH | Detects obvious Compose state writes in the composable body, such as `count++` or `state.value = ...`. |

## Minimal Configuration

```kotlin
composeGuard {
    failOnHigh.set(true)
}
```

## Configuration Reference

```kotlin
composeGuard {
    // Backward-compatible switch. Set false to report without failing the build.
    failOnHigh.set(true)

    // Build fails when failOnHigh is true and an issue is at or above this severity.
    failOnSeverity.set("HIGH")

    // Issues below this severity are omitted from reports.
    minimumSeverity.set("LOW")

    // Source roots to scan.
    sourceDirs.set(listOf("src/main/kotlin", "src/debug/kotlin"))

    // Simple path-substring exclusions.
    excludes.add("generated")
}
```

Severity values are `HIGH`, `MEDIUM`, and `LOW`.

## CI Example

```yaml
name: CI

on:
  pull_request:
  push:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew test build
      - run: ./gradlew -p sample composeGuard
```

## Local Development

Publish all local artifacts, including the Gradle plugin marker:

```bash
./gradlew publishAllPublicationsToLocalPluginRepositoryRepository
```

Artifacts are written to:

```text
build/local-plugin-repository
```

A separate test project can then resolve:

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        maven("/path/to/composeguard/build/local-plugin-repository")
        gradlePluginPortal()
    }
}
```

```kotlin
// build.gradle.kts
plugins {
    id("io.github.qandil11.composeguard") version "0.1.0"
}
```

## Limitations

ComposeGuard `v0.1.0` prefers false negatives over noisy false positives.

- Rules use Kotlin PSI syntax inspection only; no type resolution or dataflow analysis is performed.
- CG002 only reports direct composable-body property initializers. It intentionally skips transformations inside lambdas such as `remember { ... }`.
- CG004 only reports obvious writes to local state created with `remember { mutableStateOf(...) }`. It skips writes inside lambdas, effects, and event handlers.
- Exclusions are simple normalized path-substring matches, not full glob patterns.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## Support

If ComposeGuard is useful in your project, you can support its continued development through GitHub Sponsors.

## Licence

ComposeGuard is released under the Apache License 2.0. See [LICENSE](LICENSE).
