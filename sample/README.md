# ComposeGuard Sample

This standalone sample applies ComposeGuard through the Gradle plugins DSL using a local included build:

```kotlin
plugins {
    id("io.github.composeguard")
}
```

Run it from this directory:

```bash
../gradlew -p . composeGuard
```

The sample intentionally sets `failOnHigh = false` so it can print all CG001-CG004 examples without failing.

It includes:

- Positive examples that should be reported for CG001-CG004.
- Negative examples that should remain clean.
- A `generated` source path excluded by configuration to verify local false-positive control.
