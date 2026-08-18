# ComposeGuard IDE

ComposeGuard IDE is a minimal IntelliJ Platform plugin proof of concept for showing ComposeGuard findings directly in Android Studio and IntelliJ IDEA while developers edit Kotlin/Jetpack Compose source.

## Architecture

The IDE plugin is a separate Gradle module:

```text
composeguard-core
composeguard-rules
composeguard-gradle-plugin
composeguard-ide-plugin
```

The existing Gradle plugin remains the command-line and CI integration. The IDE plugin depends on `composeguard-core` and `composeguard-rules`, but the core/rules modules do not depend on IntelliJ Platform APIs.

Rule text and Gradle detector outputs live in `composeguard-rules`. CG001 is shared through a source-level detector because it is useful in both Gradle and IDE contexts. CG002-CG004 keep Kotlin PSI-based detectors for the Gradle path and matching IntelliJ PSI visitors in the IDE plugin, because compiler-embeddable PSI and IDE PSI use different IntelliJ classloader/package roots. This keeps the Gradle plugin stable while avoiding compiler-embeddable dependencies in the IDE plugin distribution.

## Supported IDE Versions

The proof of concept builds against IntelliJ IDEA Community `2024.1.7` using IntelliJ Platform Gradle Plugin `2.2.0`.

This is intended as a practical Android Studio-compatible baseline for the IntelliJ Platform 2024.1 generation while preserving the repository's Java 17 build. Full Android Studio compatibility should still be verified with JetBrains Plugin Verifier and manual Android Studio sandbox testing before Marketplace publication.

## Build

```bash
./gradlew :composeguard-ide-plugin:buildPlugin
```

The plugin ZIP is written under:

```text
composeguard-ide-plugin/build/distributions/
```

## Run In Sandbox IDE

```bash
./gradlew :composeguard-ide-plugin:runIde
```

This launches a sandbox IntelliJ IDEA instance with ComposeGuard installed.

## Tests

```bash
./gradlew :composeguard-ide-plugin:test
```

## Currently Supported IDE Rules

| Rule | Status |
| --- | --- |
| CG001 Missing stable lazy-list keys | Supported |
| CG002 Collection transformations during composition | Supported |
| CG003 Mutable collections inside mutableStateOf | Supported |
| CG004 State writes during composition | Supported |

## Inspection Configuration

ComposeGuard rules are registered as separate local Kotlin inspections under the `ComposeGuard` group.

Open IntelliJ IDEA or Android Studio settings:

```text
Editor > Inspections > ComposeGuard
```

Each rule can be enabled, disabled, or assigned a severity independently through the standard inspection profile UI.

## Suppression

ComposeGuard inspections use stable rule IDs as alternative inspection IDs:

```kotlin
@Suppress("CG004")
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    count++
}
```

Standard IntelliJ/Kotlin inspection suppression is expected to work where the IDE supports local inspection suppression for Kotlin PSI elements.

## Quick Fixes

No automatic quick fixes are provided in Phase 2.

This is deliberate:

- CG001 key insertion requires a reliable item identity; guessing `id` would be noisy.
- CG002 cannot safely move arbitrary composition work into `remember` without choosing keys and preserving semantics.
- CG003 conversions to `mutableStateListOf()` can change the state type and mutation model.
- CG004 state writes need human intent to choose an event handler or effect.

## Known Limitations

- No quick fixes are provided yet.
- The IDE inspections are intentionally conservative and may prefer false negatives over noisy false positives.
- CG002-CG004 share rule text and behavior intent with Gradle rules, but use IntelliJ PSI visitors instead of the compiler-embeddable PSI detector classes to avoid IDE classloader conflicts.
- Android Studio compatibility has been checked through IntelliJ Platform compatibility verification only; manual Android Studio editor testing is still required before Marketplace publication.
- No licensing, subscriptions, account system, payment code, or telemetry is included.
- Marketplace publication is not configured or performed for the IDE plugin.
