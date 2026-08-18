# Contributing

Thanks for helping make ComposeGuard useful without making it noisy.

## Project Modules

- `composeguard-core`: issue model, severity policy, build policy, text and JSON report rendering.
- `composeguard-rules`: Kotlin PSI-based rules. Keep rules independent and deterministic.
- `composeguard-gradle-plugin`: Gradle plugin, extension, task wiring, report file output, and TestKit coverage.
- `sample`: standalone local consumer project with realistic positive and negative examples.

## Run Tests

```bash
./gradlew clean test
```

To run the standalone sample:

```bash
./gradlew -p sample composeGuard
```

## Adding A Rule

1. Create a rule class in `composeguard-rules`.
2. Implement `ComposeGuardRule`.
3. Parse and inspect Kotlin PSI only; do not add Android Lint, compiler-plugin, or runtime dependencies without an architecture discussion.
4. Register the rule in `ComposeGuardRules`.
5. Add focused unit tests with positive and negative cases.
6. Add TestKit coverage if the rule changes report output or build policy behavior.
7. Add realistic sample coverage when useful.

## Rule IDs

Use the `CG###` convention:

- `CG001` to `CG099` are reserved for Compose performance and correctness checks.
- Do not reuse an ID after release.
- Keep messages stable once published unless the old wording is incorrect.

## False-Positive Philosophy

Prefer false negatives over noisy false positives. A rule should report only when the syntax pattern is clear and the recommendation is highly likely to be useful.

Avoid speculative detections:

- Do not infer types without type resolution.
- Do not flag code inside callbacks/effects unless the rule explicitly models that scope.
- Add known limitations to the README when a useful case is intentionally skipped.

## Test Requirements

Every rule change needs:

- positive examples that should report;
- negative examples that should stay clean;
- multiline syntax coverage when relevant;
- nested composable coverage when relevant;
- lambda/effect/event-handler coverage when relevant;
- regression coverage for any false positive being fixed.
