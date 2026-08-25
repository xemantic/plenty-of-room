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
 * `tools/cli_guard.py` (`CH-0268`, `T-319`) is the ninth retained document/tool checker, and it
 * exists because `CLAUDE.md` has recorded its trap twice and `T-272` repaired it at two call
 * sites while eleven other writers went on emitting when handed `--help`.  Same split as the
 * others: the SELF-TEST is wired here, and the census over `tools/` lives in `tools/verify.sh`,
 * because it reads a directory rather than a source file.
 */
tasks.register<Exec>("testCliGuard") {
    group = "verification"
    description = "Runs tools/cli_guard.py --self-test, the tests for the argument guard"
    commandLine("$projectDir/tools/cli_guard.py", "--self-test")
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
 * `T-313`.  The link gate's predicate was `.md`-only, so a relative link to a `.py`, `.kt`,
 * `.json`, `.sh` or directory target was invisible to every gate in this tree -- and two agents
 * relocated a mutation harness out from under a Markdown link in ONE iteration, neither noticing.
 * This harness mutates the widened predicate, the three guards it rests on, the scope line the
 * widening leaves owed, and the history sweep that measures the false-positive rate.
 */
tasks.register<Exec>("testCorpusLinkMutations") {
    group = "verification"
    description = "Runs tools/T-313-mutation-test.py, the mutation test for the widened link gate"
    commandLine("$projectDir/tools/T-313-mutation-test.py")
}

tasks.register<Exec>("testDynamicGuardProbeMutations") {
    group = "verification"
    description = "Runs tools/T-321-mutation-test.py, the mutation test for the dynamic arm of the " +
        "argument guard"
    commandLine("$projectDir/tools/T-321-mutation-test.py")
}

/*
 * `T-334` -- the census of tools that can fail a `tools/verify.sh` run, taken by REACHABILITY.
 *
 * `CH-0243` found that census keyed on a filename prefix; `C-0210` replaced it with invocations
 * in one file and named that a predicate about a FILE where the question is about a RUN;
 * `CH-0286` found that its Gradle half is a regular expression over a LITERAL, so the twelve
 * Kotlin-subject harnesses wired through `mutationSnapshotArguments` are invisible to it.  All
 * three read a SHAPE.  What decides whether an invocation can fail the run is whether `:test`
 * depends on the task that carries it, which is the `dependsOn` block at the foot of this file
 * and nothing else -- and the twelve above are deliberately absent from it.
 *
 * The gated arm is not the number.  It is that the set of `Exec` tasks unreachable from `:test`
 * must EQUAL the set of harnesses `tools/P-31-harness-census.py` declares `BY-HAND`, in both
 * directions, so a helper-wired harness cannot be added and leave the build green without being
 * declared -- after which the census subtracts it by construction.  The two sides share nothing:
 * one is the Kotlin list below, the other a hand-written Python table.
 *
 * Both tasks read only `build.gradle.kts`, `tools/verify.sh` and `tools/`, so a `--drop` or
 * `--drop-file` on a verification snapshot cannot make either fail.
 */
tasks.register<Exec>("testGateCensus") {
    group = "verification"
    description = "Runs tools/T-334-gate-census.py --self-test, the tests for the gate census"
    commandLine("$projectDir/tools/T-334-gate-census.py", "--self-test")
}

tasks.register<Exec>("testGateCensusReachability") {
    group = "verification"
    description = "Runs tools/T-334-gate-census.py --check: every Exec task unreachable from " +
        "`:test` is declared BY-HAND in P-31's table, and conversely"
    commandLine("$projectDir/tools/T-334-gate-census.py", "--check")
}

tasks.register<Exec>("testGateCensusMutations") {
    group = "verification"
    description = "Runs tools/T-334-mutation-test.py, the mutation test for the gate census"
    commandLine("$projectDir/tools/T-334-mutation-test.py")
}

/*
 * `T-336` -- a self-describing count the deliverable PRINTS, against the one a result file PINS.
 *
 * `T-334` retired the WIRING half of a recurring defect and said in its own validity range that
 * nothing checked the QUOTING half.  It also refused one shape: a gate parsing a numeral out of
 * prose and comparing it against a live derivation at `HEAD`.  That refusal turns on the
 * COMPARAND -- `CH-0182` makes agreement-with-`HEAD` unsatisfiable for a census of the corpus
 * that contains the census, and a gate that can never come clean is not a gate (`C-0083`).
 * HAVING NOTHING STABLE TO COMPARE AGAINST IS WHY SUCH A GATE DEGENERATES INTO PARSING NUMERALS.
 *
 * So the comparand here is a PINNED thing: a `(quantity, value, resolvedRef)` triple that three
 * emitters already write into committed result files and that nobody had ever read back.  A sha
 * does not move when the corpus grows, so the equality is permanent -- and the comparison needs
 * no `git` at all, which is the only reason it can be wired: `tools/snapshot.sh` excludes
 * `./.git`, so a git-dependent gate would skip silently here (`C-0177`).
 *
 * The GATED arms are the two that are clean: a recorded count must sit under a key that says
 * which state it names, in a file whose `baselineRef` resolves; and a quantity may not be
 * declared against a tool that cannot fail the run.  The PROSE arm is printed and NOT gated,
 * because it is red at `HEAD` and the task that wrote it may not edit `ANSWERS.md`; `T-339`
 * flips one constant once the substitution lands.  Both tasks read only `gpd/results/`, the two
 * deliverables, `build.gradle.kts` and `tools/`.
 */
tasks.register<Exec>("testPinnedCountCensus") {
    group = "verification"
    description = "Runs tools/T-336-pinned-count-census.py --self-test, the tests for the " +
        "pinned-count census"
    commandLine("$projectDir/tools/T-336-pinned-count-census.py", "--self-test")
}

tasks.register<Exec>("testPinnedCountCensusCheck") {
    group = "verification"
    description = "Runs tools/T-336-pinned-count-census.py --check: every recorded self-describing " +
        "count names a state that resolves, and every declared quantity is derived by a tool that " +
        "can fail the run"
    commandLine("$projectDir/tools/T-336-pinned-count-census.py", "--check")
}

tasks.register<Exec>("testPinnedCountCensusMutations") {
    group = "verification"
    description = "Runs tools/T-336-mutation-test.py, the mutation test for the pinned-count census"
    commandLine("$projectDir/tools/T-336-mutation-test.py")
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

tasks.register<Exec>("testLinkStiffnessRouteMutations") {
    group = "verification"
    description = "Runs tools/T-303-mutation-test.py <snapshot>, the Kotlin mutation test for the " +
        "crossover link-stiffness routes and the threshold bisector. Needs " +
        "-PmutationSnapshot=<dir>; not in :test"
    commandLine(mutationSnapshotArguments("T-303-mutation-test.py"))
}

tasks.register<Exec>("testRadialLinkResolutionMutations") {
    group = "verification"
    description = "Runs tools/T-310-mutation-test.py <snapshot>, the Kotlin mutation test for " +
        "the per-bond resolution of a crossover's normal link. Needs -PmutationSnapshot=<dir>; " +
        "not in :test"
    commandLine(mutationSnapshotArguments("T-310-mutation-test.py"))
}

tasks.register<Exec>("testResolvedLinkUniformRasterMutations") {
    group = "verification"
    description = "Runs tools/T-315-mutation-test.py <snapshot>, the Kotlin mutation test for " +
        "route B's uniform raster at the resolved per-bond link. Needs " +
        "-PmutationSnapshot=<dir>; not in :test"
    commandLine(mutationSnapshotArguments("T-315-mutation-test.py"))
}

tasks.register<Exec>("testCrossSectionTiedRegradeMutations") {
    group = "verification"
    description = "Runs tools/T-294-mutation-test.py <snapshot>, the Kotlin mutation test for " +
        "the 15 x 4 block graded coupled on the tied lattice and the least-squares face basis " +
        "an ODD raster-row count made necessary. Needs -PmutationSnapshot=<dir>; not in :test"
    commandLine(mutationSnapshotArguments("T-294-mutation-test.py"))
}

/**
 * `T-330` — the face's rigid basis, and the parity that decides whether three independent
 * projections are the least-squares fit at all.
 *
 * A Kotlin subject, so it takes a snapshot directory and is deliberately **not** in `:test`:
 * one mutation is one Gradle `test` run. `P-31`'s census carries it as `BY HAND`.
 */
tasks.register<Exec>("testFaceRigidBasisMutations") {
    group = "verification"
    description = "Runs tools/T-330-mutation-test.py <snapshot>, the Kotlin mutation test for " +
        "the face's rigid basis, its exact orthogonality parity and the retained " +
        "three-projection reading. Needs -PmutationSnapshot=<dir>; not in :test"
    commandLine(mutationSnapshotArguments("T-330-mutation-test.py"))
}

/*
 * `T-326` -- the two reconstructions of the face field, and the third nobody named.
 *
 * `HoneycombDeflection` FITS its rigid plane in `faceFunctional`'s owning-beam reconstruction and
 * SAMPLES the residual in `evaluate`'s nearest-beam one (`CH-0284`).  These rows hold open the
 * closed form of that gap in the face's own vertical bonds, the bond census it is written on, the
 * split quadrature in BOTH directions (`CH-0285`), and -- `C-0221`'s `P9` -- the INERTNESS of the
 * whole addition: a mutation that repoints the shipped decomposition at the new fit must fail,
 * because the eighteen committed result files rest on it not happening.
 *
 * The same Kotlin subject as `T-330`'s, so it takes a snapshot directory and is deliberately
 * **not** in `:test`, and `P-31`'s census carries it as `BY HAND`.  `T-334`'s reachability gate
 * requires the two to agree in BOTH directions, so this registration is what makes the BY-HAND
 * declaration honest rather than a harness nothing can run.
 */
tasks.register<Exec>("testFaceReconstructionMutations") {
    group = "verification"
    description = "Runs tools/T-326-mutation-test.py <snapshot>, the Kotlin mutation test for " +
        "the closed form of the fit/sample gap, the split quadrature and the inertness of the " +
        "addition. Needs -PmutationSnapshot=<dir>; not in :test"
    commandLine(mutationSnapshotArguments("T-326-mutation-test.py"))
}

tasks.register<Exec>("testSearchedDistributionMutations") {
    group = "verification"
    description = "Runs tools/T-316-mutation-test.py <snapshot>, the Kotlin mutation test for a " +
        "distribution SEARCHED at the resolved per-bond link. Needs -PmutationSnapshot=<dir>; " +
        "not in :test"
    commandLine(mutationSnapshotArguments("T-316-mutation-test.py"))
}

tasks.register<Exec>("testRouteBCoupledMutations") {
    group = "verification"
    description = "Runs tools/T-322-mutation-test.py <snapshot>, the Kotlin mutation test for " +
        "route B's own widths graded coupled on stations derived at each row length. Needs " +
        "-PmutationSnapshot=<dir>; not in :test"
    commandLine(mutationSnapshotArguments("T-322-mutation-test.py"))
}

tasks.register<Exec>("testJointPlacementDistributionMutations") {
    group = "verification"
    description = "Runs tools/T-323-mutation-test.py <snapshot>, the Kotlin mutation test for " +
        "the placement and the distribution searched TOGETHER at the resolved per-bond link. " +
        "Needs -PmutationSnapshot=<dir>; not in :test"
    commandLine(mutationSnapshotArguments("T-323-mutation-test.py"))
}

/*
 * `T-327` -- the resolution of the flatness census.
 *
 * `flatAtP90` is exactly `exceedance <= 0.10`, so a flatness verdict is a binomial statement and
 * its resolution is that proportion's sampling error rather than the discretisation departure the
 * corpus quotes.  The census reads the eighteen committed result files and WRITES NOTHING; the
 * emitter beside it is pinned to a ref, because a corpus-subject result file that defaults to
 * `HEAD` re-bases its own measurement (`CH-0246`).
 */
tasks.register<Exec>("testFlatnessResolution") {
    group = "verification"
    description = "Runs tools/T-327-flatness-resolution.py --self-test, the census and the " +
        "exact binomial instruments a flatness verdict's resolution is read with"
    commandLine("$projectDir/tools/T-327-flatness-resolution.py", "--self-test")
}

tasks.register<Exec>("testFlatnessResolutionEmitter") {
    group = "verification"
    description = "Runs tools/T-327-emit-result.py --self-test, which builds the document twice " +
        "at its pinned ref and asserts byte-identity without writing"
    commandLine("$projectDir/tools/T-327-emit-result.py", "--self-test")
}

tasks.register<Exec>("testFlatnessResolutionMutations") {
    group = "verification"
    description = "Runs tools/T-327-mutation-test.py, the mutation test for the resolution census"
    commandLine("$projectDir/tools/T-327-mutation-test.py")
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
        "testHarnessOutputContractMutations",
        // `T-313` -- the widened link predicate, its three guards and its scope line.
        "testCorpusLinkMutations",
        // `CH-0268` -- an emitter that ignores its arguments emits.
        "testCliGuard",
        // `T-321` -- the dynamic arm of that guard, which OBSERVES rather than compares.
        "testDynamicGuardProbeMutations",
        // `T-334` -- the gate census by REACHABILITY, and the mutation table over its own rules.
        "testGateCensus", "testGateCensusReachability", "testGateCensusMutations",
        // `T-336` -- a count the deliverables PRINT against the one a result file PINS.
        "testPinnedCountCensus", "testPinnedCountCensusCheck", "testPinnedCountCensusMutations",
        // `T-327` -- what a flatness verdict can resolve, and the mutation table over its rules.
        "testFlatnessResolution", "testFlatnessResolutionEmitter", "testFlatnessResolutionMutations"
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
