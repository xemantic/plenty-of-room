# CH-0286 — **THE CHECKER CENSUS'S THIRD DERIVATION IS A REGULAR EXPRESSION OVER A STRING LITERAL, AND SINCE ITERATION 46 THE KOTLIN-SUBJECT MUTATION HARNESSES ARE WIRED THROUGH A HELPER — SO `THIRTY-SEVEN` WAS AN UNDERCOUNT BY SIX AT ITS OWN REF AND THE HONEST FIGURE AT THIS TREE IS `FIFTY-ONE`.** `build.gradle.kts` wires **twelve** build-failing tools as `commandLine(mutationSnapshotArguments("T-294-mutation-test.py"))` rather than as `commandLine("$projectDir/tools/…")`, because a harness that mutates Kotlin sources must be handed a snapshot directory and cannot be run bare. The census's own `GRADLE_GATE_PATTERN` cannot see one of them. `CH-0243` found a predicate that was a **filename prefix**; `C-0210` replaced it with *invocations in one file* and named that a predicate about a **file** where the question is about a **run**; this is a predicate about a **LITERAL** where the question is about an **INVOCATION**, and it is the same defect a third time

**Against** the self-describing checker census as [`DECISIONS-FOR-NDI.md`](../../DECISIONS-FOR-NDI.md) states it, and against [`C-0210`](../claims/C-0210-fourteenth-answers-synthesis.md) §4(b), whose *"the number of distinct tools that can fail `tools/verify.sh` is `16 + 1 + 20` = THIRTY-SEVEN"* is correct under its own stated predicate and is an undercount of the quantity the sentence names.
**Not against** any physics, any flatness verdict, or [`tools/T-319-emit-result.py`](../../tools/T-319-emit-result.py)'s emitted `checkerCensus` block, which records the predicate beside every count and is therefore self-describing in the way `C-0210` intended.
**From** [`C-0220`](../claims/C-0220-fifteenth-answers-synthesis.md) (`T-332`), the fifteenth `ANSWERS.md` synthesis.
**Kind** — **a census is dated by its PREDICATE as well as by its premises**, third instance on this one count, and the first where what dates it is a **wiring idiom** introduced for an unrelated and correct reason.

---

## 1. The two wirings

`build.gradle.kts` invokes a tool as a build-failing gate in two shapes.

| shape | example | visible to `GRADLE_GATE_PATTERN`? | count at this tree |
|---|---|---|---|
| a **literal** path | `commandLine("$projectDir/tools/check-entry-points.py")` | **yes** | **21** |
| a **helper** call | `commandLine(mutationSnapshotArguments("T-294-mutation-test.py"))` | **no** | **12** |

The pattern is
`commandLine\("\$projectDir/tools/([^"]+)"((?:, "[^"]+")*)\)`,
which requires the literal to be the argument of `commandLine` itself.
The helper exists because `CLAUDE.md` records why it has to:
a harness that mutates **Kotlin** sources must not mutate the shared checkout,
so it takes a snapshot directory — and a bare `Exec` task therefore prints its usage and fails the build.
The wiring is correct; the census cannot see it.

The twelve are `T-294`, `T-297`, `T-299`, `T-303`, `T-304`, `T-307`, `T-310`, `T-315`, `T-316`,
`T-322`, `T-323` and `T-330`'s mutation harnesses.
They are **disjoint** from the 21 literal invocations and from `tools/verify.sh`'s own 18,
so the three sets add.

## 2. The numbers, at two refs

| | `tools/verify.sh`'s own | `build.gradle.kts`, literal | `build.gradle.kts`, helper | union |
|---|---|---|---|---|
| `71d126e` — `C-0210`'s baseline | 17 | 20 | **6** | **43** |
| this pass's tree | 18 | 21 | **12** | **51** |

`C-0210` published **37** at the first row, which is `16 + 1 + 20`
(its 17 read as 16 gates plus one fixture).
The quantity its sentence names — *distinct tools that can fail `tools/verify.sh`* —
was **43** at that ref and is **51** now.

## 3. Why this is a challenge and not a correction

Because the same claim that published the number **also published the predicate**,
and the predicate is right about what it measures.
What is wrong is the identification of that measurement with the question,
and `C-0210` §4(b) says so about its own predecessor in as many words.
The value of recording it is that the count is **still rising**:
six helper-wired harnesses at iteration 48 and twelve at iteration 52,
so a sentence that quotes only the literal form drifts further from the run every iteration.

## 4. What would settle it

A derivation that names **both** shapes — which is what
[`C-0220`](../claims/C-0220-fifteenth-answers-synthesis.md) now writes into
`DECISIONS-FOR-NDI.md` — or, better, a reading taken from
[`tools/P-31-harness-census.py`](../../tools/P-31-harness-census.py),
which already resolves every harness's wiring and does not infer it from a shape.
This challenge does **not** ask for a change to
[`tools/T-319-emit-result.py`](../../tools/T-319-emit-result.py):
that emitter records the predicate with the number,
which is exactly the discipline that makes the defect visible.

| | |
|---|---|
| **Status** | **RAISED**, iteration 52 |
| **Raised by** | [`C-0220`](../claims/C-0220-fifteenth-answers-synthesis.md) (`T-332`) |
| **Moves** | the self-describing checker census in [`DECISIONS-FOR-NDI.md`](../../DECISIONS-FOR-NDI.md), corrected in the same pass. **No physics, no verdict and no result file** |
