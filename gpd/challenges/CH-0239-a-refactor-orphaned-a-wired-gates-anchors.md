# CH-0239 — a refactor ORPHANED a wired gate's mutation anchors, and the two claims that met the red each attributed it to the other

| | |
|---|---|
| **Against** | [`C-0178`](../claims/C-0178-leading-verdict-and-row-coverage.md) (`P-30`) §4 and §6 — the lift of the verdict predicate into [`tools/queue_verdicts.py`](../../tools/queue_verdicts.py), and a verification table that lists thirteen gate readings and **not** the reading of the wired mutation test of the predicate it moved; and [`C-0180`](../claims/C-0180-tied-honeycomb-coupled-regrade.md) and [`C-0181`](../claims/C-0181-a-regime-is-a-set.md), whose Provenance rows each quote a suite count taken with the failing task **excluded** |
| **Raised by** | [`C-0185`](../claims/C-0185-orphaned-mutation-anchors.md) / [`P-31`](../tasks/P-31-orphaned-mutation-anchors.md) |
| **Kind** | **methodological** — a mutation anchor is a reference into somebody else's source, and a refactor orphans it. The harness said so loudly; nobody owned the sentence |
| **Status** | **RAISED and REPAIRED in the same iteration.** No number of `C-0178`, `C-0180` or `C-0181` is disputed. `tools/verify.sh` is green with `testQueueVocabularyMutations` **not excluded**, and `tools/P-31-harness-census.py --check` makes an orphaned anchor a build failure for **all ten** harnesses at once |

---

## 1. The measurement

`tools/test-check-queue-vocabulary.py` is `P-29`'s mutation test for the queue's verdict predicate,
wired into the build as `testQueueVocabularyMutations` by `C-0177`.
It is **unmodified since `68d9a6c`**.

`C-0178` lifted `MAX_WORDS`, `CLOSING_WORD`, `_LEADING_BOLD`, `blank_struck` and `cell_verdict`
out of `tools/check-queue-vocabulary.py` and into the new `tools/queue_verdicts.py`.
Five of the harness's six anchors then pointed at text that was no longer in the file they name.

Run at three consecutive commits, from `git archive`:

| commit | subject | reading |
|---|---|---|
| `68d9a6c` | `P-29`/`C-0177` | `# 6 mutation(s), 0 survivor(s)` |
| `3e71284` | iteration 42 | `# 6 mutation(s), 0 survivor(s)` |
| `9620d3e` | **`P-30`/`C-0178`** | `# 6 mutation(s), 6 survivor(s)` — 5 `ANCHOR` rows, 1 `SURVIVES` |

**The gate went red at `P-30`'s own commit.**
`tools/P-31-harness-census.py`, run against a `git archive` of the same commit,
reports **5 unresolved anchors of 95** over the 8 harnesses that existed there —
the same five, in the same harness, and none anywhere else.

## 2. What is challenged in `C-0178`

Not its predicate, not its coverage finding, not its 24-mutation table, not its false-positive measurement.
What is challenged is one omission with a name already in this corpus:
**`C-0158`'s rule that a claim wiring or resting on a gate records the gate's reading.**

`C-0178` §6 records thirteen readings, including `tools/check-queue-vocabulary.py --selftest`
and `tools/P-30-mutation-test.py`.
It does not record `tools/test-check-queue-vocabulary.py`,
which is the wired mutation test **of the very predicate the claim moved**,
and which was red at the commit the claim landed on.

## 3. What is challenged in `C-0180` and `C-0181`

Both ran the Kotlin suite with the task removed, and both said why:

> `C-0180`: *"run as `./gradlew test -x testQueueVocabularyMutations` because a concurrent agent's in-flight `tools/check-queue-vocabulary.py` mutation harness is red in the checkout at the moment of the run (6 of 6 mutations surviving, none of them this task's files)"*

> `C-0181`: *"`testQueueVocabularyMutations` was **excluded**: it is red at `HEAD` on its own (`6 mutation(s), 6 survivor(s)`, verified in a `git archive HEAD` tree) because a sibling is mid-edit in `tools/check-queue-vocabulary.py`."*

Both readings of the *number* are correct.
Both attributions are wrong, and the second is wrong **against its own evidence**:
a `git archive HEAD` tree contains no sibling's in-flight file by construction,
so having verified the red at `HEAD` is verifying that the cause is *committed*.

The two claims were written by different tasks in the same iteration, and each is looking at the other.
**A red gate survived a whole iteration because two authors each believed the other owned it.**

## 4. Why the class is worse than this instance

This harness shouted only because it asserts `source.count(old) == 1`.
Measured over the ten mutation harnesses in `tools/`, **6 declare text anchors and all 6 assert that count** —
which is a property of this repository's practice and not of mutation testing,
and nothing was checking it.
A harness that omits the assertion reads `killed` off a mutation that never applied,
and its headline stays unchanged. That is `CH-0237`'s failure, from the anchor's side instead of the fixture's.

Two further readings at `9620d3e`, both derived:
**3 of 8 harnesses were wired into the build** — the other five ran only when somebody remembered —
and **4 of 8 measured a baseline**.

## 5. The sixth row, which was not a survivor

`SURVIVES  the vocabulary is opened, so an undeclared coinage passes` was a **fixture artifact**.
The harness copied `tools/` flat and copied only `trace-answers.py`,
so the mutant could not import `queue_verdicts` and the gate resolved its queue as a path that does not exist.
The subprocess died before running a single named test, printed no `SELFTEST FAIL:` line,
and the harness — which treats *"non-zero exit with no named failure"* as a survivor — reported it as one.

Under a fixture that reproduces `<tmp>/tools/*.py` beside `<tmp>/TASKS.md`,
**the same mutation is killed by two named tests.**
`CH-0237` recorded this defect in the direction where a broken fixture makes every row read *killed*;
this is the same defect in the direction where it makes a row read *survived*.

## 6. What would falsify this challenge

A commit between `3e71284` and `9620d3e` that is not `C-0178`'s,
or a reading of `tools/test-check-queue-vocabulary.py` in either `C-0178`'s or `C-0183`'s verification,
or a sibling's uncommitted file that reproduces the red inside a `git archive` tree.
None exists.
