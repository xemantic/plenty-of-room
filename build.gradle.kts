import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    application
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.power.assert)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.version.catalog.update)
    alias(libs.plugins.xemantic.conventions)
}

group = "com.xemantic.nano"

/**
 * Redirects the build directory, so that concurrent builds of *this same working tree*
 * do not share one:
 *
 * ./gradlew test -PbuildDirectory=build-t1c
 *
 * Several agents run the loop against one checkout at a time. Gradle serialises the builds
 * themselves, but the test-results writer races between them and fails the run with
 * `EOFException` or `NoSuchFileException: build/test-results/test/binary/...` — a failure of
 * the harness, not of the tests. Giving each concurrent run its own build directory removes
 * the shared file entirely. Unset, the directory is `build` and nothing changes.
 */
providers.gradleProperty("buildDirectory").orNull?.let { directory ->
    layout.buildDirectory = layout.projectDirectory.dir(directory)
}

xemantic {
    description = "Evidence corpus from agentic-loop runs against the NDI Gen-1 DNA-origami actuator simulation programme"
    inceptionYear = "2026"
    applyAllConventions()
}


val javaTarget = libs.versions.javaTarget.get()
val kotlinTarget = KotlinVersion.fromVersion(libs.versions.kotlinTarget.get())

kotlin {

    // set up according to https://jakewharton.com/gradle-toolchains-are-rarely-a-good-idea/
    compilerOptions {
        apiVersion = kotlinTarget
        languageVersion = kotlinTarget
        jvmTarget = JvmTarget.fromTarget(javaTarget)
        freeCompilerArgs.add("-Xjdk-release=$javaTarget")
        extraWarnings = true
        progressiveMode = true
        //optIn.addAll("add opt ins here")
        //freeCompilerArgs.addAll()
    }

}

java {
    sourceCompatibility = JavaVersion.toVersion(javaTarget)
    targetCompatibility = JavaVersion.toVersion(javaTarget)
}

tasks.withType<JavaCompile>().configureEach {
    options.release = javaTarget.toInt()
}

application {
    mainClass = "com.xemantic.nano.plentyofroom.HelloWorldAppKt"
}

repositories {
    mavenCentral()
}

/**
 * Runs one study entry point, for example:
 *
 * ./gradlew study -Pstudy=brush.BrushStiffnessStudyKt
 *
 * Each GPD task adds its own `main` under `com.xemantic.nano.plentyofroom`
 * rather than competing for the single `application` main class.
 */
tasks.register<JavaExec>("study") {
    group = "application"
    description = "Runs a single study entry point, selected with -Pstudy=<relative main class>"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "com.xemantic.nano.plentyofroom." +
            (providers.gradleProperty("study").orNull ?: "brush.BrushStiffnessStudyKt")
}

/*
 * Runs the shell harness's own tests (task P-16).
 *
 * `tools/snapshot.sh`'s `drop_packages` and `drop_files` DELETE, and one of them has already
 * removed a package from a live working tree (`S-94`). Their guards are the only thing between a
 * mistyped argument and another agent's unfinished work, so they are held to the same standard as
 * the Kotlin side — and wired into `test` so that standard cannot quietly lapse.
 *
 * It hangs off `test` rather than off `check` on purpose: `tools/verify.sh` — the authoritative
 * run when several agents share this checkout — invokes `test`, so anything attached only to
 * `check` would never be exercised by the workflow that matters. It touches nothing under
 * `src/`, so it is unaffected by a `--drop`/`--drop-file` on the snapshot it runs in, and it
 * stays runnable on its own as `tools/test-snapshot.sh`.
 */
tasks.register<Exec>("testHarness") {
    group = "verification"
    description = "Runs tools/test-snapshot.sh, the tests for the snapshot/drop helpers"
    commandLine("$projectDir/tools/test-snapshot.sh")
}

/*
 * `tools/trace-answers.py` decides which numbers and which *statuses* in `ANSWERS.md` are
 * unsupported (`C-0067`, `C-0078`). Its 42 self-tests were invoked by nothing until `P-22`
 * looked: `P-21` was queued believing the same of `tools/test-snapshot.sh`, which has in fact
 * hung off `test` since `P-16`. Only this one was orphaned, and `C-0078`'s own sentence — *a
 * check nobody remembers to ask for is not a check* — applies to it verbatim.
 *
 * Same rationale as `testHarness`, and the same precondition: the tracer's self-test reads
 * only in-memory fixtures, so a `--drop`/`--drop-file` on a verification snapshot cannot make
 * it fail. The reader census (`P-22`) does read `src/`, and for exactly that reason it is
 * wired in `tools/verify.sh` rather than here.
 */
tasks.register<Exec>("testDeliverableTracer") {
    group = "verification"
    description = "Runs tools/test-trace-answers.py, the tests for the ANSWERS.md tracer"
    commandLine("$projectDir/tools/test-trace-answers.py")
}

/*
 * `tools/check-markdown-tables.py` (`P-23`, the coordinator's) is the third script in the same
 * position, and its 26 self-tests read only fixtures, so they belong here too. The *gate* it
 * provides over the repository's own Markdown reads the tree and is wired in `tools/verify.sh`
 * beside the reader census, for the reason stated above.
 */
tasks.register<Exec>("testMarkdownTables") {
    group = "verification"
    description = "Runs tools/test-check-markdown-tables.py, the tests for the table checker"
    commandLine("$projectDir/tools/test-check-markdown-tables.py")
}

/*
 * `tools/check-corpus-links.py` (`T-203`, `C-0122`) is the fourth. Its self-tests read only
 * in-memory fixtures, so they wire in beside the others; the CHECK itself lives in
 * `tools/verify.sh` rather than here, for the same reason the census does — it reads the corpus,
 * which an agent edits during an iteration.
 */
tasks.register<Exec>("testCorpusLinks") {
    group = "verification"
    description = "Runs tools/check-corpus-links.py --selftest, the tests for the link checker"
    commandLine("$projectDir/tools/check-corpus-links.py", "--selftest")
}

/*
 * `tools/check-kotlin-format-strings.py` (`C-0125`, repaired and wired by `T-207`/`C-0127`) is
 * the fifth. `CLAUDE.md` prescribes its arithmetic — count the `%` conversions over the WHOLE
 * parenthesised concatenation against the top-level commas of the argument list — in five
 * separate places, and until `T-207` nothing ran it: the sweep found **13 real defects in seven
 * committed studies**, whose raw conversions had reached **seven committed result files**, and
 * one of them printed the tile's length where the SAXS interhelical distance belonged.
 *
 * Java silently ignores EXTRA arguments, so this family never throws — which is why a test suite
 * that exercises every number cannot see it, and why the gate has to be static. Its self-tests
 * read only in-memory fixtures, so they wire in beside the others; the SWEEP over `src/` lives in
 * `tools/verify.sh`, for the same reason the census does.
 *
 * It is wired only now that the tree reports zero defects: a gate that cannot come clean is not a
 * gate (`C-0083`).
 */
tasks.register<Exec>("testFormatStrings") {
    group = "verification"
    description = "Runs tools/check-kotlin-format-strings.py --self-test, the tests for the format checker"
    commandLine("$projectDir/tools/check-kotlin-format-strings.py", "--self-test")
}

/*
 * `tools/check-challenge-index.py` (`P-26`) is the fifth retained document checker. Same split as
 * the others: its self-tests are in-memory and wire in here; the CHECK reads the corpus and lives
 * in `tools/verify.sh`.
 */
tasks.register<Exec>("testChallengeIndex") {
    group = "verification"
    description = "Runs tools/check-challenge-index.py --selftest, the tests for the index checker"
    commandLine("$projectDir/tools/check-challenge-index.py", "--selftest")
}

/*
 * `tools/check-result-file-hygiene.py` (`T-208`, `C-0129`) is the sixth, and it is the first that
 * reads the **output** rather than the source. Its catch set and `testFormatStrings`' are
 * strictly different: that one models `String.format` call sites, this one models nothing and
 * simply reads what was committed, so a conversion arriving by a route the static check does not
 * model — a `settles` string assembled in one function and formatted in another, a Python emitter
 * in `tools/`, a hand-edited field — is caught here and only here. `C-0127` repaired 13 fields
 * carrying 23 raw conversions across 7 committed result files, every one of which had been read.
 *
 * Only its `--conversions` predicate is a GATE. Its two other censuses — departure precision
 * (`T-209`) and saturated proportions (`T-210`) — report 222 fields in 29 files and 302 records
 * in 7, so they cannot fail a build without a tree-wide re-emission first, and `C-0083` says a
 * gate that cannot come clean is not a gate. They exit 0, by construction and by test.
 */
tasks.register<Exec>("testResultFileHygiene") {
    group = "verification"
    description = "Runs tools/check-result-file-hygiene.py --self-test, the tests for the result-file checker"
    commandLine("$projectDir/tools/check-result-file-hygiene.py", "--self-test")
}

tasks.named("test") {
    dependsOn(
        "testHarness", "testDeliverableTracer", "testMarkdownTables", "testCorpusLinks",
        "testFormatStrings", "testChallengeIndex", "testResultFileHygiene"
    )
}

dependencies {
    implementation(libs.viktor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.openrndr.math)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.xemantic.kotlin.test)
}

powerAssert {
    functions = listOf(
        "kotlin.assert",
        "com.xemantic.kotlin.test.assert",
        "com.xemantic.kotlin.test.have"
    )
}

versionCatalogUpdate {
    // preserve the manual, logically-grouped ordering of libs.versions.toml
    sortByKey = false
    keep {
        // kotlinTarget / javaTarget are plain version constants with no version.ref
        versions = setOf("kotlinTarget", "javaTarget")
        keepUnusedVersions = false
    }
}
