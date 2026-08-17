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

tasks.named("test") {
    dependsOn("testHarness", "testDeliverableTracer", "testMarkdownTables")
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
