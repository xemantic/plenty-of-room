# C-0185 — **A MUTATION ANCHOR IS A REFERENCE INTO SOMEBODY ELSE'S SOURCE, AND A REFACTOR ORPHANS IT.** The wired gate `testQueueVocabularyMutations` was red for a whole iteration because `P-30` moved the predicate five of its six anchors point at, and the two claims that met the red each attributed it to the other. It shouted only because it asserts its anchor count — the class is silent in principle, so it is now a build failure for all **ten** harnesses at once. And the corrected fixture found the real gap: the vocabulary was load-bearing as a **set** and **9 of its 11 members** were held open by no named test at all

| | |
|---|---|
| **Task** | [`P-31`](../tasks/P-31-orphaned-mutation-anchors.md) |
| **Leaf** | — (process) |
| **Verification type** | **logical**, as executable self-tests, a mutation test in both directions, and a census derived by running every harness against its own subject at a stated ref |
| **Verdict** | **PASS on `F2`–`F7`; `F1` is met in the half this task owns and NOT met as written** (§7, §8). `tools/verify.sh` reads **`BUILD SUCCESSFUL in 21m 55s`** with `testQueueVocabularyMutations` **not excluded** and nine further harnesses newly wired, and every document gate passes except the `T-272` typed-handle registry — which is **red at `HEAD` itself**, on a `git archive` of `9620d3e`, for a reason no agent forbidden the index can close (`T-290`). The harness reads **17 mutations, 0 retired, 0 survivors** at a measured baseline of **0**; the census reads **0 unresolved of 155 anchors and 33 symbols over 10 harnesses**, **10 of 10 wired**, **10 of 10 measuring a baseline** |
| **Maturity** | TRL 1–3 process artifact. **No physics changed.** No Kotlin source is touched and no result file of any study moves |
| **Provenance** | [`gpd/results/P-31-mutation-harness-census.json`](../results/P-31-mutation-harness-census.json), emitted by [`tools/P-31-emit-result.py`](../../tools/P-31-emit-result.py) (**new**, 7 self-tests), which takes `--ref`, defaults it to `HEAD` and records the **resolved** sha `9620d3ef3f21aa4038055a5752cd637f49e62954` (`CH-0210`). **Every number in it is derived by running**: the census by calling it, each harness's mutation and survivor counts by parsing that harness's own summary line, the *before* half of the per-classification measurement by re-running the repaired harness against a copy of the gate with this task's own named-test block excised — an anchored, asserted-once excision, so it cannot silently no-op. **No wall-clock timing and no step counter is emitted.** New: [`tools/P-31-harness-census.py`](../../tools/P-31-harness-census.py) (**33** named tests). Repaired: [`tools/test-check-queue-vocabulary.py`](../../tools/test-check-queue-vocabulary.py) (re-anchored, fixture, baseline, 11 new mutations), [`tools/check-queue-vocabulary.py`](../../tools/check-queue-vocabulary.py) (**55 → 67** named tests), [`tools/T-249-mutation-test.py`](../../tools/T-249-mutation-test.py), [`tools/T-250-mutation-test.py`](../../tools/T-250-mutation-test.py) and [`tools/T-278-mutation-test.py`](../../tools/T-278-mutation-test.py) (each gains the baseline `CH-0237` asks every harness for), and [`build.gradle.kts`](../../build.gradle.kts) (nine new tasks). **Tests first, watched to fail**: the eleven per-classification mutations were written and run **before** the named tests that kill them and **9 of 11 survived**; the census's own two derivation tests were written before the derivation and one failed; the census file itself was imported before it existed |
| **Conditions** | Python tools, `build.gradle.kts` and documents only. Units unchanged and untouched — every number here is an integer count. The census's subject is the mutable tree, so its reading is dated by its `baselineRef` |
| **Consumes** | [`C-0177`](C-0177-queue-status-vocabulary.md) (`P-29`, the vocabulary gate and its mutation test), [`C-0178`](C-0178-leading-verdict-and-row-coverage.md) (`P-30`, the refactor and the 24-mutation standard), [`C-0183`](C-0183-residue-as-a-gate.md) (`T-283`, the residue gate and [`CH-0237`](../challenges/CH-0237-a-mutation-harness-layout-is-a-premise-of-its-own-measurement.md)'s fixture repair), [`C-0158`](C-0158-prose-gate-red.md) (a claim that wires or rests on a gate records the gate's reading), [`C-0161`](C-0161-mechanics-on-an-imported-design.md) (a mutation that fails nothing is the finding), [`C-0176`](C-0176-partial-discharge-and-restatement-predicates.md) (a mutation must replace a rule wholesale; per-classification coverage), [`C-0127`](C-0127-format-string-repair.md) (changing a predicate must fail a **named** test) |
| **Constrains** | every future refactor of any tool a mutation harness names: an orphaned anchor or an orphaned subject symbol is now a build failure. And every future mutation harness: it is declared in `tools/P-31-harness-census.py`'s table, or the census does not see it |
| **Raises** | [`CH-0239`](../challenges/CH-0239-a-refactor-orphaned-a-wired-gates-anchors.md), against [`C-0178`](C-0178-leading-verdict-and-row-coverage.md), [`C-0180`](C-0180-tied-honeycomb-coupled-regrade.md) and [`C-0181`](C-0181-a-regime-is-a-set.md) |

---

## 1. The blocker, and the diagnosis that was verified rather than inherited

`tools/verify.sh` failed on the Gradle task `testQueueVocabularyMutations`,
which runs `tools/test-check-queue-vocabulary.py`.
Its reading was `# 6 mutation(s), 6 survivor(s)`: five `ANCHOR` rows and one `SURVIVES`.

Two things were handed over as diagnosis and both were checked.

**(a) *"A sibling is mid-edit."*** Run from `git archive` at three consecutive commits:

| commit | subject | reading |
|---|---|---|
| `68d9a6c` | `P-29`/`C-0177` | `# 6 mutation(s), 0 survivor(s)` |
| `3e71284` | iteration 42 | `# 6 mutation(s), 0 survivor(s)` |
| `9620d3e` | **`P-30`/`C-0178`** | `# 6 mutation(s), 6 survivor(s)` |

The cause is **committed**, and the commit is `C-0178`'s own —
the one that lifted `MAX_WORDS`, `CLOSING_WORD`, `_LEADING_BOLD` and `blank_struck`
out of `tools/check-queue-vocabulary.py` into the new `tools/queue_verdicts.py`.
A `git archive` tree contains no sibling's in-flight file by construction,
so `C-0181`'s own verification — *"verified in a `git archive HEAD` tree"* — is verification that the cause is committed.

**(b) *"One mutation genuinely survives."*** It does not.
The harness copied `tools/` **flat** into a scratch directory and copied only `trace-answers.py`,
so the mutant could not import `queue_verdicts`, and the gate resolved its queue as `<tmp>/../TASKS.md`.
The subprocess died before running a single named test and printed no `SELFTEST FAIL:` line,
and the harness — which reads *"non-zero exit with no named failure"* as a survivor — called it one.

Under a fixture that reproduces `<tmp>/tools/*.py` beside `<tmp>/TASKS.md`,
**the same mutation is killed by two named tests**:
*the iteration-41 coinage is REFUSED* and *a scope qualifier nobody has declared is REFUSED*.

That is `CH-0237` in the quiet direction.
There, a fixture that no longer matched the tree made **all 24** rows of a table read *killed* off one `FileNotFoundError`.
Here it made one row read *survived*. Same premise, opposite sign.

## 2. The repair: five re-anchored, none retired

Every one of `P-29`'s six meanings still exists in the code.
Four moved file and two changed shape; **five are re-anchored and one needed no change**.
Meanings are preserved, not literal text — and every row still **replaces** its rule wholesale,
never widening it to `original|mutant`, which is the no-op this corpus has measured three times.

| # | mutation | was | is now |
|---|---|---|---|
| 1 | a bold run anywhere in the cell counts as a verdict | `check-queue-vocabulary.py`, `_LEADING_BOLD.match(cell)` | `queue_verdicts.py`, `_LEADING_BOLD.match(stripped)` |
| 2 | no word bound, so a bold prose sentence is a verdict | `check-queue-vocabulary.py`, `MAX_WORDS = 6` | `queue_verdicts.py`, same text |
| 3 | a verdict must be the whole cell | `check-queue-vocabulary.py`, a positive guard | `queue_verdicts.py`, the same guard inverted into an early return |
| 4 | strike-through no longer blanked | `check-queue-vocabulary.py`, `blank_struck`'s return | `queue_verdicts.py`, same text |
| 5 | the vocabulary is opened | `check-queue-vocabulary.py`, `undeclared`'s body | **unchanged** — this anchor always resolved |
| 6 | THE LIST EDIT | `check-queue-vocabulary.py`, after `"PARTLY DONE"` | same file, after `"TODO"`, which `P-30` appended |

**`RETIRED` is a named, empty list in the harness**, not an absence —
so a shrinking table is a statement and not an accident (`C-0071`'s *strike, never delete*, applied to a mutation list).

## 3. The fixture and the baseline (`CH-0237`)

The harness now builds `<tmp>/tools/{queue_verdicts,trace-answers,check-queue-vocabulary}.py`
beside `<tmp>/TASKS.md`, **measures the named failures of an unmutated copy, and subtracts them**.
The baseline reads **0**.

**A crash is neither a kill nor a survival, and that is the whole of `CH-0237` read in both directions.**
Counting a mutant that never started as a *survivor* is what this harness did;
counting it as a *kill* is what `CH-0237`'s own instance did, 24 rows off one `FileNotFoundError`.
So a crash is returned separately and **refused**: the row prints `CRASHED`, says that it measures nothing,
and the harness exits non-zero — and a crash in the **unmutated** copy aborts before any row is printed.
Checked by construction: run against a fixture with `queue_verdicts.py` deliberately withheld —
the exact defect this task repaired — the harness exits non-zero naming the missing module,
where before it printed a survivor count.

## 4. What the corrected fixture then found — the finding, not the fix

The whole-set mutation is killed. **Not one of the vocabulary's eleven members was.**

Deleting `"DONE"` from `CLOSING_VERDICTS` failed **no named test at all**,
and so did eight of the other ten.
Only `"PARTIALLY DONE"` (2 named tests) and `"TODO"` (3) were held open.

That is `T-225`'s per-name standard and `C-0176`'s —
*every classification, in both directions, must fail a named test when changed on its own* —
reached on the vocabulary this gate exists to hold closed.
`C-0177` wrote that *"the part that has content is not the vocabulary list, it is the agreement"*,
which is true and had been read as licence to leave the list untested:
the agreement check iterates **over the list**, so a member removed from it is a member the agreement no longer asks about.

**Repair.** Eleven per-classification mutations, **derived from the gate's own two sets**
so that a phrase the queue coins tomorrow is mutation-tested the day it is declared;
and twelve named tests carrying their phrases as **literals**,
because a test generated from the set under test disappears together with the member it was meant to hold open.
The twelfth is the completeness check that keeps the literal table honest in the other direction.

| | before | after |
|---|---|---|
| per-classification mutations | 11 | 11 |
| of which survive every named test | **9** | **0** |
| named tests in `tools/check-queue-vocabulary.py` | 55 | **67** |
| the harness's total reading | `17 mutations, 9 survivors` | **`17 mutations, 0 retired, 0 survivors`, baseline 0** |

## 5. The generalisation, measured

An orphaned anchor is **silent in principle**.
This one shouted only because its harness asserts `source.count(old) == 1`;
a harness without that assertion reads `killed` off a mutation that never applied, headline unchanged.

`tools/P-31-harness-census.py` resolves **every reference every mutation harness makes into its subject** —
a text **anchor**, or the **name** of an attribute on the imported subject —
and fails the build on any that does not resolve.
The harness table is declared; everything else is read out of the harness itself
(its anchors from its own mutation table, its symbol references from its own syntax tree,
its anchor assertion from an AST pattern, its baseline from its own identifiers,
its wiring from `build.gradle.kts` and `tools/verify.sh`).

| harness | kind | anchors | symbols | asserts count | baseline | wired |
|---|---|---|---|---|---|---|
| `test-check-queue-vocabulary.py` | TEXT-ANCHOR | 17/17 | 0/0 | yes | yes | yes |
| `P-30-mutation-test.py` | TEXT-ANCHOR | 24/24 | 0/0 | yes | yes | yes |
| `T-281-mutation-test.py` | TEXT-ANCHOR | 24/24 | 0/0 | yes | yes | yes |
| `T-283-mutation-test.py` | TEXT-ANCHOR | 12/12 | 0/0 | yes | yes | yes |
| `T-234-mutation-test.py` | TEXT-ANCHOR | 55/55 | 0/0 | yes | yes | yes |
| `T-280-mutation-test.py` | TEXT-ANCHOR | 23/23 | 0/0 | yes | yes | yes |
| `T-278-mutation-test.py` | ATTRIBUTE | — | 9/9 | n/a | **yes (new)** | yes |
| `T-225-mutation-test.py` | ATTRIBUTE | — | 11/11 | n/a | yes | **yes (new)** |
| `T-249-mutation-test.py` | REIMPLEMENTATION | — | 6/6 | n/a | **yes (new)** | **yes (new)** |
| `T-250-mutation-test.py` | REIMPLEMENTATION | — | 7/7 | n/a | **yes (new)** | **yes (new)** |

**`n/a` is a classification, not a gap.** A harness that mutates by reassigning a name on the imported
subject is loud about a rename for free: a renamed attribute makes the assignment a no-op, the mutant
is then identical to the original, it fails no named test, and every harness here reports that as a
`SURVIVOR` and exits 1. What those four still needed, and three of them now have, is the **baseline**
`CH-0237` left as its open residue.

**Before and after, both derived rather than remembered:**

| | at `9620d3e` | in the tree this claim leaves |
|---|---|---|
| harnesses | 8 | 10 |
| anchors declared / unresolved | 95 / **5** | 155 / **0** |
| subject symbols declared / unresolved | 31 / 0 | 33 / **0** |
| wired into the build | **3 of 8** | **10 of 10** |
| measuring a baseline | **4 of 8** | **10 of 10** |
| mutations over all harnesses, survivors | — | **204, 0** |

**The instrument is checked against the instance it was written for.**
One of the census's own named tests materialises `git archive 9620d3e` and requires it to report
**five** orphaned anchors in `test-check-queue-vocabulary.py` and **none** in any other harness —
the same five that harness itself printed as `ANCHOR` rows.
A gate that cannot report the defect it exists for is an argument, not an instrument.

## 6. Three defects found in this task's own instruments and in the build

**(a) The census's first run said four harnesses do not assert their anchor count, and all four do.**
The AST rule required the comparison's left side to be the `.count(...)` call itself;
`test-check-queue-vocabulary.py`, `P-30`, `T-281` and `T-283` bind the count to a local and compare the local.
A **false negative about exactly the property under study** — it would have said
*this harness would fail silently* about the very harness whose shout started this task.
Two named tests were written first, one failed, and the rule now follows the binding.

**(b) The build's own premise was a FILE MODE.** `tools/T-280-mutation-test.py` had never been invoked as a command, so its executable bit had never mattered; Gradle's `commandLine` execs the path directly, as every other checker in `build.gradle.kts` does, and the first authoritative run died with *"A problem occurred starting process"*. Loud, but asserted nowhere — the census now requires every declared harness to be executable.

**(c) A census of a second tree in one process was handed the first tree's modules.**
A harness imports its subject by name through `sys.path`, so `sys.modules` would have served
the working tree's `check-queue-vocabulary` to a census of an archived one —
the same class of defect this whole task is about, one level up. The census purges those names.

## 7. Acceptance predicates

| | predicate | verdict |
|---|---|---|
| **F1** | `tools/verify.sh` exits 0 with `testQueueVocabularyMutations` not excluded | **PARTIAL, and the residue is named.** The Gradle half is **`BUILD SUCCESSFUL in 21m 55s`**, 31 tasks, with the task under repair **not excluded** and nine more newly wired. The script's own exit is 1, on the **`T-272` typed-handle registry** — a gate this task did not touch, which `git archive HEAD` shows red at `9620d3e` on its own, because `P-30` and `T-280` emitted committed result files and neither regenerated `structure/ResultInputs.kt`. **A second red gate, of exactly the shape this task is about**, and it is closed by `git add -A` plus one generator run: queued as `T-290` |
| **F2** | The diagnosis is verified, not inherited | **PASS** — the red is reproduced at `9620d3e` from `git archive`, and the `SURVIVES` row is shown to be a fixture artifact by running the same mutation under a correct fixture (§1) |
| **F3** | Every mutation re-anchored with its meaning preserved, or retired by name with its reason | **PASS** — 5 re-anchored, 1 unchanged, **0 retired**, and `RETIRED` is a named empty list (§2) |
| **F4** | A measured baseline of 0 under a layout-reproducing fixture | **PASS** (§3) |
| **F5** | Every mutation fails a named test; every declared classification held open by one, measured before and after | **PASS** — 17 of 17 killed; classifications **9 → 0** silent (§4) |
| **F6** | The harness census derived by running, not typed | **PASS** — 10 harnesses, 155 anchors, 33 symbols, the wiring and the two properties all read out of the artifacts (§5) |
| **F7** | An orphaned anchor is a build failure, and a synthetic orphan fails a named test | **PASS** — `testMutationAnchors`; the census's own tests cover a synthetic orphan, a double occurrence, a missing target file and a renamed subject symbol, and the gate was run against a tree carrying a **planted undeclared harness**, which it refuses with exit 1 |

## 8. Verification, and the gates' own readings

**`tools/verify.sh` on the tree this claim leaves**, run to completion on a frozen tree:

```
BUILD SUCCESSFUL in 21m 55s          31 actionable tasks: 31 executed
                                     testQueueVocabularyMutations NOT excluded;
                                     nine harnesses newly wired, all green
result-reader census                 47 checks passed; ok: 133 studies, 145 direct + 27 transitive edges
markdown tables                      # 0 table defect(s) in 609 file(s)
corpus links                         # 0 broken link(s) in 606 file(s)
String.format conversions            clean
cold-start heading                   # 0 defect(s); heading at iteration 43, journal at 43
emitter rounding census              clean
queue vocabulary                     # 0 defect(s); 283 leading verdict(s) over 281 row(s); residue 0
trace-answers, both deliverables     0 ABSENT, 0 contradicted, 0 self-contradictions
corpus identifiers                   # 0 dangling identifier(s) in 586 file(s)
entry points                         # 0 defect(s); 131 of 133 studies emit a result file
T-272 typed-handle registry          RED — and red at HEAD: see below
```

**The one gate that does not pass is not this task's, and it is the same shape as this task's.**
`tools/T-272-emit-result-inputs.py --check` compares `structure/ResultInputs.kt` byte for byte against
what it would generate from the **tracked** result files. `P-30` and `T-280` each emitted a committed
result file at `9620d3e` and neither re-ran the generator, so `git archive HEAD | tar -x` and running
that tree's own copy of the gate **exits 1** — before any of this iteration's uncommitted work exists.
It cannot be closed from a working tree, because the registry must also carry handles for result files
that are not yet in the index; measured in a scratch copy, `git add -A` followed by one generator run
takes it from 159 handles to **166** and the check to **0**. Queued as `T-290`.

```
tools/verify.sh                                     ->  BUILD SUCCESSFUL in 21m 55s (see above)
python3 tools/test-check-queue-vocabulary.py        ->  # 17 mutation(s) (6 base + 11 per-classification), 0 retired, 0 survivor(s)
python3 tools/check-queue-vocabulary.py --selftest   ->  # 67 self-test(s), 0 failure(s)
python3 tools/P-31-harness-census.py --self-test     ->  # 33 self-test(s), 0 failure(s)
python3 tools/P-31-harness-census.py --check         ->  # 10 harness(es); 0 unresolved; wired: 10 of 10
python3 tools/P-31-emit-result.py --self-test        ->  # 7 self-test(s), 0 failure(s)
```

## 9. Validity range, and what would falsify this

- **The census is dated by its `baselineRef`.** Its subject is the mutable tree; `CH-0182` applies verbatim.
- **The declared harness table is the one thing not derived, and it is guarded rather than trusted.** `discovers_harnesses` finds a harness by its filename (`*-mutation-test.py`) **or** by its own ALL-CAPS mutation table, which is how the one exception to the naming convention — `tools/test-check-queue-vocabulary.py`, whose table is `BASE_MUTATIONS` — is found; an emitter that merely *reports* a mutation table declares lower-case names and is correctly not a harness. A harness somebody writes tomorrow and does not declare **fails the gate** rather than being invisible to it, and a declared harness that goes missing is a hard error rather than a zero. What remains outside the guard is a harness named otherwise **and** carrying no all-caps table; that would be a third convention, and there is none in the tree.
- **The census resolves references; it does not judge them.** An anchor that resolves may still be a mutation of the wrong rule, and a symbol that resolves may still be assigned a value the subject never uses. That is what each harness's own survivor count is for, and all ten now report **0**.
- **`assertsAnchorCount` and `measuresBaseline` are derived from syntax**, so a harness could satisfy either without using it. Both are reported, neither is gated; what is gated is resolution.
- **Falsified** by: a mutation harness in `tools/` absent from the table; an anchor that resolves in the census and does not resolve in its own harness; a commit other than `9620d3e` that turns the queue-vocabulary harness red; or a `git archive` tree at `9620d3e` that does not reproduce the five orphans.
