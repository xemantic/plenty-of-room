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
 * `T-212`/`C-0131` promoted the **departure-precision** predicate from an audit to a second GATE:
 * the 27 files that still carried a `reproductions[*].departure` or `convergence[*].departure` at
 * more than two significant digits were re-emitted, so the tree reads **0 fields in 0 files** and
 * `C-0083`'s rule — *a gate that cannot come clean is not a gate* — is satisfied rather than
 * argued around. Its **scope** line is wider than its gate and is deliberately NOT gated: the same
 * two records carry three more spellings of the same quantity, 378 fields in 36 files, and closing
 * those is 36 further study re-runs (`T-214`). The saturated-proportion census (`T-210`/`T-213`)
 * stays an audit for the same reason, and reports what is left.
 */
tasks.register<Exec>("testResultFileHygiene") {
    group = "verification"
    description = "Runs tools/check-result-file-hygiene.py --self-test, the tests for the result-file checker"
    commandLine("$projectDir/tools/check-result-file-hygiene.py", "--self-test")
}

/*
 * `tools/check-queue-vocabulary.py` (`P-29`, `C-0177`) is the last, and it exists because the
 * convention it enforces has been in `CLAUDE.md` twice as prose and was broken a third time:
 * *a new status word must be tested in BOTH senses the day it is coined*.  Iteration 41 coined
 * `**SECOND DELIVERABLE ANSWERED**` on the `T-9` row, `queue_status` read the row CLOSED, and an
 * OPEN task left the register while `ANSWERS.md` correctly said it was live.
 *
 * TWO tasks rather than one, because this tool has a mutation test as well as self-tests, and
 * `C-0127`'s standard is that a predicate's self-tests are worth nothing unless CHANGING the
 * predicate fails a NAMED one.  Both read only in-memory fixtures; the CHECK over `TASKS.md`
 * lives in `tools/verify.sh` beside the other corpus gates, for the reason stated above.
 */
tasks.register<Exec>("testQueueVocabulary") {
    group = "verification"
    description = "Runs tools/check-queue-vocabulary.py --selftest, the tests for the queue vocabulary"
    commandLine("$projectDir/tools/check-queue-vocabulary.py", "--selftest")
}

tasks.register<Exec>("testQueueVocabularyMutations") {
    group = "verification"
    description = "Runs tools/test-check-queue-vocabulary.py, the mutation test for that predicate"
    commandLine("$projectDir/tools/test-check-queue-vocabulary.py")
}

/*
 * `tools/T-234-census.py` and its emitter (`T-260`/`T-262`, `C-0176`) are the seventh and eighth.
 * Their self-tests read only in-memory fixtures, so they wire in here beside the others.
 *
 * `--check` is deliberately NOT wired into `tools/verify.sh`, on the agent's own reasoning and
 * `C-0083`'s rule read FORWARD: the census's scope includes `TASKS.md`, which every agent edits
 * every iteration, so a new occurrence arrives unclassified through no fault of the tree. A gate
 * that fires on correct work is a gate that gets switched off. The check stays an audit and its
 * self-tests stay a gate — which is the split the repository already draws for the reader census.
 */
tasks.register<Exec>("testHoneycombCensus") {
    group = "verification"
    description = "Runs tools/T-234-census.py --self-test, the tests for the supersession census"
    commandLine("$projectDir/tools/T-234-census.py", "--self-test")
}

tasks.register<Exec>("testHoneycombCensusClassification") {
    group = "verification"
    description = "Runs tools/T-234-emit-classification.py --self-test, the tests for its emitter"
    commandLine("$projectDir/tools/T-234-emit-classification.py", "--self-test")
}

/*
 * And its MUTATION test, for the reason `C-0127` gives and `C-0176` measured on itself: a
 * predicate's self-tests are worth nothing unless changing the predicate fails a NAMED one, and
 * the first draft of this very table had 9 of 22 rows failing nothing -- eight of them because
 * the mutation was written as an ALTERNATION with the original, which is a no-op. 0.67 s.
 */
/*
 * `tools/T-278-emitter-rounding-census.py` (`T-278`, `C-0174`) and the three tools built with it.
 * Their self-tests read fixtures (the rounding mirror pins its baseline to `b853b85`, the commit
 * `CH-0223` was filed on, because `C-0174` found that a self-test reading a MUTABLE artifact expires
 * the moment the defect it asserts is repaired). The census's SOURCE half is gated in
 * `tools/verify.sh`; its artifact half is reported and not gated.
 */
tasks.register<Exec>("testColdStartNote") {
    group = "verification"
    description = "Runs tools/check-cold-start-note.py --selftest, the tests for the cold-start heading"
    commandLine("$projectDir/tools/check-cold-start-note.py", "--selftest")
}

tasks.register<Exec>("testEmitterRounding") {
    group = "verification"
    description = "Runs tools/T-278-emitter-rounding-census.py --self-test"
    commandLine("$projectDir/tools/T-278-emitter-rounding-census.py", "--self-test")
}

tasks.register<Exec>("testEmitterRoundingMutations") {
    group = "verification"
    description = "Runs tools/T-278-mutation-test.py, the mutation test for the rounding mirror"
    commandLine("$projectDir/tools/T-278-mutation-test.py")
}

tasks.register<Exec>("testRoundingSimulation") {
    group = "verification"
    description = "Runs tools/T-278-rounding-simulation.py --self-test, the offline rounding mirror"
    commandLine("$projectDir/tools/T-278-rounding-simulation.py", "--self-test")
}

tasks.register<Exec>("testSolverProvenance") {
    group = "verification"
    description = "Runs tools/T-278-solver-provenance.py --selftest"
    commandLine("$projectDir/tools/T-278-solver-provenance.py", "--selftest")
}

tasks.register<Exec>("testHoneycombCensusMutations") {
    group = "verification"
    description = "Runs tools/T-234-mutation-test.py, the mutation test for the census predicates"
    commandLine("$projectDir/tools/T-234-mutation-test.py")
}

/*
 * `P-31`.  A MUTATION HARNESS IS A REFERENCE INTO SOMEBODY ELSE'S SOURCE, and a refactor orphans
 * it.  `P-30` lifted the queue's verdict predicate into `tools/queue_verdicts.py`; five of
 * `tools/test-check-queue-vocabulary.py`'s six anchors then pointed at text that had moved one
 * file across, and `testQueueVocabularyMutations` went red at `9620d3e` — `P-30`'s own commit.
 * It stayed red for a whole iteration because two claims each EXCLUDED the task, one of them
 * having verified the red in a `git archive HEAD` tree and still attributed it to *"a concurrent
 * agent's in-flight file"*.
 *
 * `tools/P-31-harness-census.py --check` resolves every anchor and every subject symbol of every
 * mutation harness in `tools/`, at once, and fails the build on any that does not resolve. Run
 * against `git archive 9620d3e` it reports exactly the five the harness itself printed — asserted
 * as one of its own named tests, so the instrument is checked against the instance it was written
 * for. It reads only `tools/`, `build.gradle.kts` and `tools/verify.sh`, so it wires in here.
 *
 * AND THE HARNESSES THEMSELVES ARE WIRED, all of them.  Before `P-31`, 3 of 10 ran in the build
 * and the other 7 ran only when somebody remembered — which `CLAUDE.md` records five times as *a
 * convention is not a mechanism*. Together they add about 35 s to a suite that takes twenty
 * minutes.
 */
tasks.register<Exec>("testMutationAnchorSelfTests") {
    group = "verification"
    description = "Runs tools/P-31-harness-census.py --self-test, the tests for the harness census"
    commandLine("$projectDir/tools/P-31-harness-census.py", "--self-test")
}

tasks.register<Exec>("testMutationAnchors") {
    group = "verification"
    description = "Runs tools/P-31-harness-census.py --check: no mutation harness may be orphaned"
    commandLine("$projectDir/tools/P-31-harness-census.py", "--check")
}

tasks.register<Exec>("testLeadingVerdictMutations") {
    group = "verification"
    description = "Runs tools/P-30-mutation-test.py, the mutation test for the verdict predicate"
    commandLine("$projectDir/tools/P-30-mutation-test.py")
}

tasks.register<Exec>("testDebtLineMutations") {
    group = "verification"
    description = "Runs tools/T-280-mutation-test.py, the mutation test for the debt-line ratio"
    commandLine("$projectDir/tools/T-280-mutation-test.py")
}

tasks.register<Exec>("testDischargeCensusMutations") {
    group = "verification"
    description = "Runs tools/T-281-mutation-test.py, the mutation test for the discharge census"
    commandLine("$projectDir/tools/T-281-mutation-test.py")
}

tasks.register<Exec>("testQueueResidueMutations") {
    group = "verification"
    description = "Runs tools/T-283-mutation-test.py, the mutation test for the residue gate"
    commandLine("$projectDir/tools/T-283-mutation-test.py")
}

tasks.register<Exec>("testQueueColumnMutations") {
    group = "verification"
    description = "Runs tools/T-289-mutation-test.py, the mutation test for the column predicate"
    commandLine("$projectDir/tools/T-289-mutation-test.py")
}

/*
 * `tools/T-225-mutation-test.py` needs `--check` to be a gate at all: without it, an UNPROTECTED
 * classification is printed and the exit code is 0. That default was right while nothing ran it;
 * it is the wrong default for a wired task.
 */
tasks.register<Exec>("testDepartureKeyMutations") {
    group = "verification"
    description = "Runs tools/T-225-mutation-test.py --check, the per-name departure mutations"
    commandLine("$projectDir/tools/T-225-mutation-test.py", "--check")
}

tasks.register<Exec>("testProsePredicateMutations") {
    group = "verification"
    description = "Runs tools/T-249-mutation-test.py, the mutation test for the prose predicate"
    commandLine("$projectDir/tools/T-249-mutation-test.py")
}

tasks.register<Exec>("testProseGateMutations") {
    group = "verification"
    description = "Runs tools/T-250-mutation-test.py, the mutation test for the prose gate policy"
    commandLine("$projectDir/tools/T-250-mutation-test.py")
}

/*
 * `T-292` -- the COLUMN REPAIR that took `T-289`'s advisory arm to a reading of 0 and let it be
 * gated. TWO tasks, for the same reason the vocabulary gate has two: the repair carries its own
 * named self-tests (the leaf derivation, the two per-shape rules, the token-preservation proof)
 * and a mutation table over them.
 */
tasks.register<Exec>("testColumnRepair") {
    group = "verification"
    description = "Runs tools/T-292-column-repair.py --self-test, the tests for the column repair"
    commandLine("$projectDir/tools/T-292-column-repair.py", "--self-test")
}

tasks.register<Exec>("testColumnRepairMutations") {
    group = "verification"
    description = "Runs tools/T-292-mutation-test.py, the mutation test for the column repair"
    commandLine("$projectDir/tools/T-292-mutation-test.py")
}

tasks.register<Exec>("testChallengeStatusMutations") {
    group = "verification"
    description = "Runs tools/T-298-mutation-test.py, the mutation test for the challenge-status gate"
    commandLine("$projectDir/tools/T-298-mutation-test.py")
}

tasks.register<Exec>("testMutationInputCensusMutations") {
    group = "verification"
    description = "Runs tools/T-295-mutation-test.py, the mutation test for the fixture-vs-corpus census"
    commandLine("$projectDir/tools/T-295-mutation-test.py")
}

/*
 * `T-306`.  A MUTATION HARNESS'S PRINTED OUTPUT IS AN INTERFACE, and nothing declared it: three
 * collisions in two iterations, each between a harness and `tools/T-295-mutation-input-census.py`,
 * each written by a different hand and each green alone.  The shapes are now declared per harness
 * in `tools/P-31-harness-census.py`'s own table and the census parses each harness with its own,
 * so a changed output REFUSES instead of being read under another harness's semantics.  This
 * harness mutates both halves of that contract and runs both suites for every row.
 */
tasks.register<Exec>("testHarnessOutputContractMutations") {
    group = "verification"
    description = "Runs tools/T-306-mutation-test.py, the mutation test for the harness-output contract"
    commandLine("$projectDir/tools/T-306-mutation-test.py")
}

/*
 * THE FOUR KOTLIN HARNESSES, WHICH TAKE A SNAPSHOT DIRECTORY AND ARE DELIBERATELY OUT OF `:test`.
 *
 * The Kotlin-subject harnesses mutate Kotlin sources, so one mutation is one Gradle `test` run --
 * minutes each, against the 0.7 s a Python harness takes -- and they must not edit a shared
 * checkout.  An ARGUMENT is a wiring decision (`T-301`): a bare
 * `Exec` task prints the harness's usage and fails the build, which is what the first attempt at
 * wiring `T-297` did in iteration 46.  Registered here with the snapshot as a project property,
 * so they are runnable by name and NOT reachable from `:test`:
 *
 *     ./gradlew testRasterTurnTetherMutations -PmutationSnapshot=/tmp/my-snapshot
 *
 * Take the snapshot with `tools/snapshot.sh`; the harnesses refuse a directory holding a `.git`.
 * All four are declared `BY-HAND` in `P-31`'s table and `tools/T-295-mutation-input-census.py`
 * cross-checks that declaration against each harness's own printed usage line, in both directions.
 */
fun mutationSnapshotArguments(harness: String): List<String> {
    val snapshot = findProperty("mutationSnapshot") as String?
    return listOf("$projectDir/tools/$harness") + (snapshot?.let { listOf(it) } ?: emptyList())
}

tasks.register<Exec>("testCommonModeMutations") {
    group = "verification"
    description = "Runs tools/T-297-mutation-test.py <snapshot>, the Kotlin mutation test for the " +
        "crossover common mode. Needs -PmutationSnapshot=<dir>; not in :test"
    commandLine(mutationSnapshotArguments("T-297-mutation-test.py"))
}

tasks.register<Exec>("testRasterTurnTetherMutations") {
    group = "verification"
    description = "Runs tools/T-299-mutation-test.py <snapshot>, the Kotlin mutation test for the " +
        "raster-turn tether element. Needs -PmutationSnapshot=<dir>; not in :test"
    commandLine(mutationSnapshotArguments("T-299-mutation-test.py"))
}

tasks.register<Exec>("testAnchorAzimuthMutations") {
    group = "verification"
    description = "Runs tools/T-304-mutation-test.py <snapshot>, the Kotlin mutation test for the " +
        "raster-turn anchor azimuth derivation. Needs -PmutationSnapshot=<dir>; not in :test"
    commandLine(mutationSnapshotArguments("T-304-mutation-test.py"))
}

tasks.register<Exec>("testUniformRasterTetherMutations") {
    group = "verification"
    description = "Runs tools/T-307-mutation-test.py <snapshot>, the Kotlin mutation test for " +
        "route B's per-turn tether census. Needs -PmutationSnapshot=<dir>; not in :test"
    commandLine(mutationSnapshotArguments("T-307-mutation-test.py"))
}

tasks.named("test") {
    dependsOn(
        "testHarness", "testDeliverableTracer", "testMarkdownTables", "testCorpusLinks",
        "testFormatStrings", "testChallengeIndex", "testResultFileHygiene",
        "testQueueVocabulary", "testQueueVocabularyMutations",
        "testHoneycombCensus", "testHoneycombCensusClassification",
        "testHoneycombCensusMutations", "testEmitterRounding", "testEmitterRoundingMutations",
        "testRoundingSimulation", "testSolverProvenance", "testColdStartNote",
        // `P-31` -- the harness census, and the seven harnesses that ran only when somebody
        // remembered.  3 of 10 were wired before this line; all 10 are now.
        "testMutationAnchorSelfTests", "testMutationAnchors",
        "testLeadingVerdictMutations", "testDebtLineMutations", "testDischargeCensusMutations",
        "testQueueResidueMutations", "testQueueColumnMutations", "testDepartureKeyMutations",
        "testProsePredicateMutations", "testProseGateMutations",
        // `T-292` -- the column repair, and the mutation table over its own rules.
        "testColumnRepair", "testColumnRepairMutations",
        // Iteration 46 -- two harnesses landed UNWIRED, which `P-31` read as `wired: 12 of 14`.
        // `C-0185`'s whole finding is that a harness nobody remembers to run decays silently, so
        // the wiring belongs in the same iteration as the harness.  The two KOTLIN harnesses,
        // `tools/T-297-mutation-test.py` and `tools/T-299-mutation-test.py`, are registered above
        // and are deliberately NOT here: one mutation is one Gradle `test` run.  Their basenames
        // can now be spelled in a comment, because `T-306` made `P-31`'s `wired_in` a USE and not
        // a MENTION -- before that, explaining why a harness was unwired made it read as wired.
        "testMutationInputCensusMutations", "testChallengeStatusMutations",
        // `T-306` -- the harness-output contract, declared per harness and cross-checked.
        "testHarnessOutputContractMutations"
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
