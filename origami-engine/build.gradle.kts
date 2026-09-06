/*
 * Copyright 2026 Kazimierz Pogoda / Xemantic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

/*
 * `origami-engine` — the library extracted from the corpus (see ../LIBRARY.md).
 *
 * It holds the models that have no counterpart in the DNA-nanotechnology tooling:
 * the environment (a solved grafted layer and a solved 2:1 electrolyte gap with an electrode
 * boundary), the crossover lattices and their design rules, the grillage on a foundation
 * with crossover prestrain as a load, the coupling influence bank under measured fabrication
 * dropout, and the device force balance with its pull-in fold.  It reads no result file and
 * emits none: everything that writes `gpd/results/` stays in the root project.
 *
 * The packages keep their `com.xemantic.nano.plentyofroom.*` names for now, so that the
 * root project's studies — which reference these declarations from the SAME package without
 * an import — compile unchanged against the module.  Renaming the package root is a separate,
 * later step (LIBRARY.md, "what was deliberately not done").
 */
plugins {
    `java-library`
    // shared test helpers (`Numerics.kt` and friends) live in src/testFixtures, so the root
    // project's tests can use them too
    `java-test-fixtures`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.power.assert)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "com.xemantic.nano"

val javaTarget = libs.versions.javaTarget.get()
val kotlinTarget = KotlinVersion.fromVersion(libs.versions.kotlinTarget.get())

kotlin {
    // same settings as the root project, deliberately — one compiler, two modules
    compilerOptions {
        apiVersion = kotlinTarget
        languageVersion = kotlinTarget
        jvmTarget = JvmTarget.fromTarget(javaTarget)
        freeCompilerArgs.add("-Xjdk-release=$javaTarget")
        extraWarnings = true
        progressiveMode = true
    }
}

java {
    sourceCompatibility = JavaVersion.toVersion(javaTarget)
    targetCompatibility = JavaVersion.toVersion(javaTarget)
}

tasks.withType<JavaCompile>().configureEach {
    options.release = javaTarget.toInt()
}

repositories {
    mavenCentral()
}

dependencies {
    // `api`, because the library's own types carry viktor arrays and openrndr vectors,
    // and its records are `@Serializable`
    api(libs.viktor)
    api(libs.kotlinx.serialization.json)
    api(libs.openrndr.math)
    testFixturesApi(libs.kotlin.test)
    testFixturesApi(libs.xemantic.kotlin.test)
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

tasks.test {
    useJUnitPlatform()
}
