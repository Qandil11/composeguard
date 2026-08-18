# Release Checklist

## 1. GitHub Repository Creation And Publication

- Create `composeguard/composeguard` on GitHub.
- Push the local repository.
- Verify README, CHANGELOG, CONTRIBUTING, LICENSE, and CI render correctly.
- Protect the default branch and require CI before merge.

## 2. Tagging v0.1.0

- Ensure `version = "0.1.0"` in `build.gradle.kts`.
- Run `./gradlew clean test`.
- Run `../gradlew -p sample composeGuard`.
- Run `./gradlew publishAllPublicationsToLocalPluginRepositoryRepository`.
- Verify a fresh independent project can resolve `id("io.github.qandil11.composeguard") version "0.1.0"` from the local repository.
- Commit release changes.
- Tag with `git tag -a v0.1.0 -m "ComposeGuard v0.1.0"`.
- Push commits and tags.

## 3. Gradle Plugin Portal Publishing

- Create or verify the Gradle Plugin Portal account for the project owner.
- Add plugin portal credentials outside the repository.
- Confirm plugin metadata: ID, display name, description, website, VCS URL, license, developers, and SCM.
- Run the appropriate Gradle Plugin Portal publish task from a clean checkout.
- Verify the plugin page appears and installation instructions use `id("io.github.qandil11.composeguard") version "0.1.0"`.

## 4. Maven Central If Needed

- Decide whether core/rules artifacts need direct Maven Central publication.
- If yes, configure signing and Sonatype Central publishing outside the repository.
- Verify POM metadata and dependency declarations.
- Publish from a clean release tag.

## 5. GitHub Release

- Draft a GitHub Release for `v0.1.0`.
- Include the CHANGELOG notes.
- Link installation instructions and limitations.
- Attach any generated artifacts only if needed.

## 6. Independent Release Verification

- Create a new temporary project outside the ComposeGuard repository.
- Apply:

```kotlin
plugins {
    id("io.github.qandil11.composeguard") version "0.1.0"
}
```

- Add one clean Compose-like file and one file with a known CG001 finding.
- Run `./gradlew composeGuard`.
- Verify console output and both report files.
- Verify CI instructions work in a throwaway GitHub repository if practical.
