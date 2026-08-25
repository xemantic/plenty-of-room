# C-0222 — the **fourth** predicate for one count was still a **shape**, and the question is **reachability**: `37` undercounts by five, `51` **overcounts by seven**, and the answer is **44**. The twelve helper-wired harnesses `CH-0286` calls *build-failing* are exactly the twelve [`tools/P-31-harness-census.py`](../../tools/P-31-harness-census.py) already declares `BY-HAND` — at **3 of 3 refs** — and that equality, not a number, is what ships as the gate

| | |
|---|---|
| **Task** | [`T-334`](../tasks/T-334-a-gate-census-is-a-reachability-question.md), raised by [`CH-0286`](../challenges/CH-0286-a-gate-wired-through-a-helper-is-invisible-to-the-census.md) (filed by [`C-0220`](C-0220-fifteenth-answers-synthesis.md), `T-332`) |
| **Leaf** | `A8.2` (process) |
| **Verification type** | **logical** — 42 named self-tests in the census, 10 in its emitter, a 23-row mutation table at 0 survivors over a measured and subtracted green baseline, and every count re-derived at **four** named states rather than inherited. `tools/T-295-mutation-input-census.py --check` reads **418 mutations over 18 harnesses, 418 fixture-backed, 0 corpus-dependent, 0 survivors, 0 defects**, this task's 23 among them and all 23 fixture-backed |
| **Verdict** | **PASS on `P1`–`P7`.** Of the nine declared falsifiers, **`F8` FIRED, as declared, and by MORE than declared** — wiring the census into `build.gradle.kts` takes its own answer from **44** to **46**, where the task file predicted 45, because the prediction was made from the count of *tasks* and the answer is a count of *tools*. `F1`, `F2`, `F3`, `F4`, `F6`, `F7` and `F9` did **not** fire; `F5` is a **hand-off** to the coordinator's `tools/verify.sh --committed`. `F2` is **tested at all three refs**, not asserted at one: `6 = 6`, `11 = 11`, `12 = 12` |
| **Maturity** | TRL 1–3 process artifact. **No physics changed.** No Kotlin source under `src/` is touched, no existing result file is emitted or re-emitted, and no physical number in the corpus moves |
| **Provenance** | [`tools/T-334-gate-census.py`](../../tools/T-334-gate-census.py) (new; the predicate, the four historical predicates kept beside it, the residue, the decomposition, the three-arm `--check`, **42** named self-tests), [`tools/T-334-mutation-test.py`](../../tools/T-334-mutation-test.py) (**23** mutations, **0** survivors), [`tools/T-334-emit-result.py`](../../tools/T-334-emit-result.py) (**10** self-tests), result [`gpd/results/T-334-the-gate-census-by-reachability.json`](../results/T-334-the-gate-census-by-reachability.json) (`baselineRef` `d9a3522`, byte-identical across two runs), one row in [`tools/P-31-harness-census.py`](../../tools/P-31-harness-census.py)'s `HARNESSES` table and three `Exec` tasks plus three `dependsOn` entries in [`build.gradle.kts`](../../build.gradle.kts) |
| **Conditions** | The corpus at **`d9a3522`** — HEAD as this task's Formulate began, **pinned rather than defaulted** — and the two earlier refs the published figures were taken at, `71d126e` and `d7b7074`. HEAD moved twice while this claim was being drafted (`23e2c58`, `5c0229a`) and the census is **44 at all three**, neither commit touching `build.gradle.kts` or `tools/`; the emitter's `--ref` defaults to the pinned sha, because a corpus-subject emitter defaulting to `HEAD` re-bases its own measurement between the draft and the emission — `CH-0246` met **within one task** rather than across iterations. `tools/`, `build.gradle.kts` and this task's own artifacts only. No `./gradlew` and no `tools/verify.sh` run: a sibling agent held studies in flight, so every gate was run directly as Python and the build is the coordinator's |
| **Consumes** | [`CH-0286`](../challenges/CH-0286-a-gate-wired-through-a-helper-is-invisible-to-the-census.md) (**upheld in its diagnosis, refuted in its number**), [`C-0210`](C-0210-fourteenth-answers-synthesis.md) §4(b) (the `37` and its predicate), [`C-0220`](C-0220-fifteenth-answers-synthesis.md) §4(b) (the `43 / 50 / 51`), [`CH-0243`](../challenges/CH-0243-a-checker-census-keyed-on-a-filename-prefix.md) and [`CH-0222`](../challenges/CH-0222-a-self-describing-count-can-be-right-and-its-predicate-wrong.md) (the first two predicates), [`C-0206`](C-0206-a-harness-output-format-is-an-interface.md) (`wired_in` as a **use**, and the declared row shape), [`C-0185`](C-0185-orphaned-mutation-anchors.md) (`P-31`; the subtracted baseline), [`C-0161`](C-0161-mechanics-on-an-imported-design.md) (*a mutation that fails nothing is the finding — construct the state*), [`C-0176`](C-0176-partial-discharge-and-restatement-predicates.md) (*a mutation must replace a rule wholesale*), [`C-0209`](C-0209-a-link-target-is-a-filename-whatever-it-names.md) (*a gate that comes clean must say what it does not reach*), [`C-0083`](C-0083-markdown-tables-that-do-not-render.md) (*a gate that cannot come clean is not a gate*), [`C-0195`](C-0195-the-discriminating-input.md) (*a fixture layout is a dependency declaration*) |
| **Constrains** | every future statement of *how many tools can fail `tools/verify.sh`*, and every future `Exec` task registered in `build.gradle.kts` |
| **Raises** | [`CH-0289`](../challenges/CH-0289-p31-wired-in-is-a-file-fact-not-a-run-fact.md). `CH-0290` is reserved and **RELEASED UNUSED**. Opens [`T-336`](../../TASKS.md) |

---

## The claim, in three lines

**A count of *what can fail a run* is a question about reachability, and all four predicates that
have answered it read a SHAPE.** At `d9a3522` the answer is **44** distinct tools — `18` that
`tools/verify.sh` runs itself, `34` that `./gradlew test` runs through an `Exec` task `:test`
depends on, and an **overlap of 8** that no published figure has ever subtracted.

**`51` overcounts it by seven**, because the twelve helper-wired Kotlin-subject harnesses it adds
are *registered and not depended on*: `build.gradle.kts`'s own comment says they are *"runnable by
name and **NOT reachable from `:test`**"*, and they are exactly the twelve `P-31` declares
`BY-HAND`. **`37` undercounts by five.**

**The deliverable is the equality, not the number.** `tools/T-334-gate-census.py --check` gates
that the `Exec` tasks unreachable from `:test` **equal** `P-31`'s `BY-HAND` set in both
directions — two objects sharing no code, a Kotlin `dependsOn` list and a hand-written Python
table — so a helper-wired harness cannot be added and leave the build green without being
declared, after which the census subtracts it by construction.

---

## 1. The predicate, and what it does not reach

> A tool `T` under `tools/` can fail a **default** `tools/verify.sh` run — no arguments, under its
> `set -euo pipefail` — iff **route A**, `tools/verify.sh` runs `tools/T` as the **command word**
> of a comment-stripped line of its own body; or **route B**, `tools/verify.sh` runs
> `./gradlew test` and `tools/T` is named inside the **balanced `commandLine(...)` span** of an
> `Exec` task **reachable from `:test`**. The answer is the **union**, de-duplicated by basename.

Two properties are deliberate. It carries **no `--self-test` filter**, because a failing self-test
fails the same run — that filter separates *gates over the corpus* from *self-tests over
fixtures*, which is a different question, reported as its own row. And it reads **`dependsOn`**,
not `commandLine`: both wiring shapes are resolved, because the resolution is `P-31`'s own
`command_line_spans`, and whether the task *runs* is then a separate fact read from the one place
that decides it.

The residue is printed on **every** run, because `C-0209` requires it — a gate that can come clean
must say what it does not reach, or a clean run is read as a statement about the whole corpus.

| not counted | at `d9a3522` | why |
|---|---|---|
| `Exec` tasks not reachable from `:test` | **12** | Gradle never executes them |
| modules reached **only** by import — `census_discharges.py`, `emission_header.py`, `queue_verdicts.py` | **3** | a defect in one fails the run and **no invocation names it** |
| `tools/snapshot.sh` | **1** | `tools/verify.sh` **sources** it; the command word is `source` |
| the Kotlin suite `./gradlew test` runs | — | most of what can fail the run, and not a tool |
| a non-default invocation | — | `--no-checks` and any `--drop` set `checks="no"` and delete route A; the trailing `"$@"` reaches `./gradlew test`, so `-x <task>` deletes a route-B member |

---

## 2. The numbers, at four named states

| | `71d126e` (`C-0210`) | `d7b7074` (`C-0220`) | `d9a3522` (`HEAD`) | this pass's tree |
|---|---|---|---|---|
| route A — `verify.sh`'s own command words | 17 | 18 | **18** | 18 |
| route B — reachable from `:test` | 32 | 34 | **34** | 36 |
| overlap (the two routes are **not** disjoint) | 7 | 8 | **8** | 8 |
| **union — distinct tools that can fail the run** | **42** | **44** | **44** | **46** |
| `Exec` tasks **not** reachable from `:test` | 6 | 11 | **12** | 12 |
| `P-31` declares `BY-HAND` | 6 | 11 | **12** | 12 |
| **equal, both directions** | **yes** | **yes** | **yes** | **yes** |

`P1`, `P2`, `P3`, `P5`, `P6` and `P7` are all reproduced by the shipped tool from the Formulate
cheap bound, exactly; there is no tolerance, these are integers.

---

## 3. Why `51` overcounts, and the coincidence that expired

Each published figure is built as `|route A| + |shape set|`, and the tool **reconstructs all four
from their own two parts** before decomposing them, so the decomposition is about the construction
the publisher used and not about one the reader supplied.

| published | by | at | reconstructs | union | difference | unreachable counted | Gradle tools the shape misses | overlap asserted away |
|---|---|---|---|---|---|---|---|---|
| `37` | `C-0210` | `71d126e` | `17 + 20` | 42 | **`+5`** | `0` | `+12` | `−7` |
| `43` | `CH-0286` | `71d126e` | `17 + 26` | 42 | **`−1`** | `−6` | `+12` | `−7` |
| `50` | `C-0220` | `d7b7074` | `18 + 32` | 44 | **`−6`** | `−11` | `+13` | `−8` |
| `51` | `C-0220` / `CH-0286` | `d9a3522` | `18 + 33` | 44 | **`−7`** | `−12` | `+13` | `−8` |

**Three signed corrections, and at `C-0210`'s own ref they nearly cancel.** `CH-0286`'s `43` sat
within **one** of the true `42` — not because the predicate was nearly right but because six
unreachable tasks, twelve missed Gradle tools and a seven-tool overlap happened to sum to `−1`.
Six iterations later the same three sum to `−7`. `CLAUDE.md`'s *decompose a ratio before predicting
it*, on a difference of two censuses; and its *a number that is too close is not a number that is
right*.

**And `C-0210` is right about the disjointness it asserted.** Its *"the two invocation sets are
disjoint"* is **true at all three refs** under its own `--self-test` filter — route A and the
literal-no-self-test Gradle set intersect in **0** at every one. The overlap of 8 appears only
when the filter is dropped, because the eight tools `verify.sh` runs are also run by Gradle **with**
a self-test flag. The disjointness is an artefact of the filter, not a property of the wiring, and
a sentence quoting the sum without the filter is quoting a premise it has discarded.

---

## 4. The gate, which is the deliverable

`tools/T-334-gate-census.py --check` has three arms, each clean at `d9a3522` and each able to fail
by a **named** test:

1. **the `Exec` tasks unreachable from `:test` equal `P-31`'s `BY-HAND` set, in both directions**;
2. every tool named in an `Exec` task's `description` is one that task's `commandLine` runs —
   `C-0206`'s use/mention consistency, over a coordinate no other gate reads;
3. every tool on either route exists under `tools/` and carries its executable bit.

**Arm 1 is not a tautology and it is not trivially satisfied.** The two sides share no code: one is
`tasks.named("test") { dependsOn(…) }` in Kotlin, the other a hand-written tuple in Python that
`tools/T-295-mutation-input-census.py` independently cross-checks against each harness's own
printed **usage** line. And the equality is a **measurement**, taken at three refs and holding at
all three, not an assertion at one — which is the discipline three predecessors on this same count
each skipped.

**It is deliberately not a gate on the wording of `DECISIONS-FOR-NDI.md`.** A gate parsing
*"`18 + 21 + 12` = FIFTY-ONE"* out of prose would be a gate on a **numeral**, which is the class of
predicate this task exists to retire.

### 4a. Why a shape predicate looked as though it worked

Measured at all three refs: **the set of literal-wired tools is exactly the set reachable from
`:test`**, and **every helper-wired tool is unreachable**, at `32 = 32`, `34 = 34` and `34 = 34`.
So the shape and the reachability coincide **today**, and a shape predicate that dropped the
self-test filter and did not add the helper set would land on the right union by accident. Nothing
enforces the coincidence — a Python harness taking an argument would be literal-wired and out of
`:test`, and a helper-wired task added to `dependsOn` would run bare and fail — which is precisely
what arm 1 pins.

---

## 5. `F8` fired as declared, and by more than declared

Wiring this census into `build.gradle.kts` adds `tools/T-334-gate-census.py` and
`tools/T-334-mutation-test.py` to the very set it counts: **44 → 46**. It was declared as an
expected fire, and the declared value was **45**, which is wrong by one — because the prediction
counted the `Exec` **tasks** the wiring adds (two were planned, three shipped) where the answer
counts **tools** (two distinct basenames over three tasks). The result file emits **both** readings
under `atBaselineRef` and `atThisPassesTree`, and every sentence names which it quotes.

`CH-0182`'s *a census destroys itself* for the ninth consecutive pass touching one, and the first
where the pass's own **forecast of the self-destruction** was on the wrong axis.

---

## 6. The mutation table, and three survivors that were the finding

**23 mutations, 0 survivors**, over a green subtracted baseline (`CH-0237`). Six rows restore a
predecessor's defect — the literal-only tool pattern, the missing reachability filter, the
`--self-test` exclusion, the disjointness assumption — and five over-widen a rule, which is
`C-0176`'s standard in both directions.

**The first run read 3 survivors and all three were about the fixture, not the tests** (`C-0161`):

| survivor | what it was |
|---|---|
| the `--self-test` exclusion | a **defective mutation**: the row carried no arguments, so `"--self-test" in str(row)` was a no-op. Repaired in the **subject** — the invocation's own flags are now carried on the row, which makes the *no-self-test-filter* choice **derivable from the same pass** instead of merely stated |
| the `Exec`-block bound widened to the whole file | the fixture's description-less task was **last**, so there was no later description to leak into it. Repaired by moving it into the middle |
| the sentinel hard-coded instead of read from the table | every fixture row spelled the sentinel as the **name** `BY_HAND`, which the mutation still resolves. Repaired by a fixture whose table **renames** the sentinel and whose row spells it **literally** |

---

## 7. Falsifier verdicts

| | declared | verdict |
|---|---|---|
| `F1` | the tool disagrees with the cheap bound at any of three refs | **did not fire** — `44 / 42 / 44` |
| `F2` | the unreachable set and `BY-HAND` are not equal at all three refs | **did not fire, and it is tested at all three**: `6 = 6`, `11 = 11`, `12 = 12` |
| `F3` | the decomposition does not sum, or does not give `−1` at `71d126e` | **did not fire** — and it needed a parameter it was not declared with: `37` counted the **literal shape only** and `43 / 50 / 51` counted **both**, so the same three terms cannot be written for both without saying which. The tool now takes it and asserts that each figure **reconstructs** from its own two parts |
| `F4` | the residue is not 3, or a tool is missing or non-executable | **did not fire** — `3`, `0`, `0` |
| `F5` | a `./gradlew test` run executes any of the twelve | **HAND-OFF** — the coordinator's `tools/verify.sh --committed`; its log names every task it ran and the twelve must be absent |
| `F6` | a mutation fails no named test, or the baseline is not empty | **did not fire** — 23 mutations, 0 survivors, baseline green (after §6's three repairs), and `tools/T-295-mutation-input-census.py --check` reports all **23 fixture-backed and 0 corpus-dependent**, so none is held open by committed corpus state (`C-0195`) |
| `F7` | `P-31 --check` moves off 0, or a harness's row count changes | **did not fire** — `30 harness(es); 567 anchor(s); 0 unresolved; wired 30 of 30`, against `29 / 544 / 0 / 29 of 29` at `HEAD`: `+1` harness and `+23` anchors, which is exactly this task's own table, and no existing harness's count moved. `tools/T-306-mutation-test.py`, whose subject is `P-31`, reads `27 mutation(s), 0 survivor(s)` |
| `F8` | wiring the tool changes the answer it reports | **FIRED, as declared** — `44 → 46`, §5 |
| `F9` | the result file is not byte-identical across two runs | **did not fire** |

---

## 8. Validity range, and what this does not settle

- **It is a statement about a DEFAULT `tools/verify.sh` run.** `--no-checks` and any `--drop`
  delete route A entirely; a trailing `-x <task>` deletes a route-B member. The count is not
  invariant under the flags, and the tool says so in its own output.
- **`F5` is not verified here.** That an `Exec` task absent from `dependsOn` is never executed is
  settled by three independent readings — the `dependsOn` list, `build.gradle.kts`'s own comment,
  and `P-31`'s `BY-HAND` declaration — and **not** by a build. No `./gradlew` was run: a sibling
  agent held studies in flight.
- **The import residue is a residue and not a count.** Three modules can fail the run and no
  invocation names them; folding them in would give `47`, and a fourth predicate answering a
  slightly different question is what this task exists to stop.
- **The deliverable's own sentence is still a typed numeral.** This claim supplies a derivation and
  a gate; nothing yet checks that what `DECISIONS-FOR-NDI.md` prints agrees with what the tool
  derives. That is `T-336`, and until it lands the recurrence is prevented only for the *wiring*
  half of the defect, not for the *quoting* half.
- **`P-31`'s `wired_in` still reports `build.gradle.kts` for the twelve.** True of the **file** and
  not of the **run** — filed as [`CH-0289`](../challenges/CH-0289-p31-wired-in-is-a-file-fact-not-a-run-fact.md) rather than answered, because it is a question about `P-31`'s own subject.

---

## 9. The hand-off

`DECISIONS-FOR-NDI.md`'s census passage carries two figures this claim moves, and **this claim's
author did not edit that file**. The substitution is reported with the exact text, and it is a
**sentence** change and not only a number change: a corrected numeral that must be re-typed next
pass has retired nothing, so the replacement quotes the **command** that derives it.

The wording is in the report to the coordinator and in `T-336`.
