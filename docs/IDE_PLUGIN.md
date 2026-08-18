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

CG001 is shared through `Cg001LazyListKeyDetector`, a source-level detector owned by `composeguard-rules`. The Gradle rule and IDE inspection both call this detector, so the IDE integration does not introduce a second independently evolving CG001 definition.

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
| CG002 Collection transformations during composition | Not exposed in IDE yet |
| CG003 Mutable collections inside mutableStateOf | Not exposed in IDE yet |
| CG004 State writes during composition | Not exposed in IDE yet |

## Known Limitations

- CG001 is the only IDE inspection in this phase.
- No quick fixes are provided yet.
- The inspection is source-pattern based and intentionally conservative.
- No licensing, subscriptions, account system, payment code, or telemetry is included.
- Marketplace publication is not configured or performed for the IDE plugin.
