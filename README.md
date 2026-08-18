# ComposeGuard

Static performance checks for Jetpack Compose.

## Phase 1 scope

This repository currently implements only **CG001: Missing Lazy List Keys**. That is deliberate: the first milestone proves the full chain before adding CG002-CG005.

Verified chain:

1. Compose/Kotlin source is read from Gradle source directories.
2. The analyser parses Kotlin with Kotlin PSI from `kotlin-compiler-embeddable`.
3. CG001 detects `items(collection) { ... }` or `itemsIndexed(collection) { ... }` inside `LazyColumn`/`LazyRow` when no `key` argument is present.
4. The `composeGuard` Gradle task prints and writes a report.
5. Tests cover rule detection, non-detection with `key`, report output, and failing the Gradle task for HIGH issues.

## Architecture decision

Phase 1 uses **Kotlin PSI directly** rather than Android Lint, Detekt, or KtLint.

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
