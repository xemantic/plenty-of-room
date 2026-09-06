# Development

Maintenance notes for working on this project itself.
From time to time, it is worth to update the build tooling and dependencies.

## The two modules

This build has two Kotlin modules — `origami-engine`, the library, and the root project, the corpus
([`LIBRARY.md`](LIBRARY.md)) —
and each declares its own `kotlin { compilerOptions { … } }` block.

The **versions** cannot drift, because both read `javaTarget` and `kotlinTarget` from
[libs.versions.toml](gradle/libs.versions.toml).
The **flags** can: `extraWarnings`, `progressiveMode`, `-Xjdk-release` and the `powerAssert`
function list are written out twice, deliberately, so that the module has no dependency on the
root's build script.
Change one and change the other, or the library compiles under settings the corpus does not.

The module also declares `java-test-fixtures`, because the shared test helpers
(`Numerics.kt` and friends) live in `origami-engine/src/testFixtures/kotlin`
and the root project's tests use them through
`testImplementation(testFixtures(project(":origami-engine")))`.

## Update gradlew wrapper

```shell
./gradlew wrapper --gradle-version latest --distribution-type bin
```

## Update all the dependencies to the latest versions

All the gradle dependencies are managed by the
[libs.versions.toml](gradle/libs.versions.toml) file in the `gradle` dir.

To resolve the latest versions,
and apply them automatically to [libs.versions.toml](gradle/libs.versions.toml),
run the [version-catalog-update](https://github.com/littlerobots/version-catalog-update-plugin) plugin:

```shell
./gradlew versionCatalogUpdate
```

To review and pick the updates one by one instead of applying them all,
use the interactive mode:

```shell
./gradlew versionCatalogUpdate --interactive
```

then apply the staged changes with:

```shell
./gradlew versionCatalogApplyUpdates
```

> [!NOTE]
> The plugin is configured in [build.gradle.kts](build.gradle.kts)
> to preserve the manual ordering of `libs.versions.toml` (`sortByKey = false`),
> and to keep the `kotlinTarget` and `javaTarget` version constants,
> which have no `version.ref` and would otherwise be removed as unused.