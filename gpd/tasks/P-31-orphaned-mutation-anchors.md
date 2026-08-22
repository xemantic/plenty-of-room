# P-31 — an orphaned mutation anchor, and the harness census that makes the class loud

**Process task.** No physics. No Kotlin. The subject is this repository's own mutation harnesses.

| | |
|---|---|
| **Kind** | process — a wired gate is red, and it is red for a reason nobody owned |
| **Verification type** | **logical**, as executable self-tests, a mutation test in both directions, and a census derived by running every harness against its own subject |
| **Units** | counts only. No wall-clock timing and no step counter is emitted (`CLAUDE.md`: *a wall clock in a result file is a step counter by another name*) |

---

## 1. Formulate — the blocker, stated as a number

`tools/verify.sh` exits non-zero on the Gradle task `testQueueVocabularyMutations`,
which runs [`tools/test-check-queue-vocabulary.py`](../../tools/test-check-queue-vocabulary.py).
Its reading is `# 6 mutation(s), 6 survivor(s)`:
five rows report `ANCHOR … anchor occurs 0 times, expected 1`
and one reports `SURVIVES … no named test failed`.

The harness is unmodified since `68d9a6c`.
The subject moved underneath it: `P-30` (`C-0178`) lifted the shared predicate into
[`tools/queue_verdicts.py`](../../tools/queue_verdicts.py),
and `T-283` (`C-0183`) changed the vocabulary block and added inline-code blanking.

**The generalisable shape.**
A mutation harness anchors on the *text* of its subject.
A refactor that moves that text **orphans** the anchor.
This one shouted only because it asserts `source.count(old) == 1`;
a harness that does not assert that reads `killed` off a mutation that never applied.
So the question this task answers is not *"how do I make the build green"* but
**which of this repository's mutation harnesses would fail loudly if their subject moved, and which would not.**

## 2. Cheap bound, run first

Three greps and three runs, before any repair:

1. `git archive <ref> | tar -x` at `68d9a6c`, `3e71284` and `9620d3e`, running the harness in each —
   which commit turned it red, and is the cause really *"a sibling's in-flight file"*?
2. Build the fixture the harness *should* have (`<tmp>/tools/*.py` beside `<tmp>/TASKS.md`) and apply the one
   anchor that still resolves — is the `SURVIVES` row a genuine survivor, or `CH-0237`'s fixture defect again?
3. `grep -c` the harness paths in `build.gradle.kts` and `tools/verify.sh` — how many of them run at all.

Each is seconds, and (1) and (2) between them decide whether the received diagnosis is the true one.

## 3. Plan — method, and its justification against cost

**Repair, then generalise.** The repair is bounded and mechanical; the census is where the value is.

- **Re-anchor** each of the six mutations onto the code as it now stands, preserving its **meaning** rather than its
  literal text. Where a mutation's meaning no longer exists, **retire it by name with its reason**; never delete silently.
- **Repair the fixture** (`CH-0237`): reproduce `<tmp>/tools/` beside `<tmp>/TASKS.md`, and **measure and subtract a
  baseline**, so that `killed` means *this mutation broke something*.
- **Close whatever the corrected fixture shows to be genuinely silent**, by writing named tests — never by weakening a
  mutation, and never by widening a rule to `original|mutant`, which is a no-op this corpus has measured three times
  (`C-0176` 9 of 22 rows, `C-0177` 2 of 6).
- **Census every mutation harness**, deriving rather than asserting: import each harness, extract its mutation table,
  and **resolve every anchor against its own subject at a stated ref**. An anchor that does not resolve is an orphan.
- **Gate it**, and wire the harnesses that only run when somebody remembers.

**Cost.** Every harness in the tree runs in under 17 s; the whole census is one pass over ten Python files and no JVM.
The alternative — a convention in `CLAUDE.md` saying *"re-run the mutation tests after a refactor"* — is what this
repository has recorded five times as *a convention is not a mechanism*, and it is what failed here.

**What would falsify this approach.** If the anchors of most harnesses could not be extracted without editing every
harness, the census would be an assertion about a table rather than a measurement of one, and the right deliverable
would be a *protocol* (`anchors()` on every harness) filed as a task rather than a tool shipped now.

## 4. Acceptance predicates

| | predicate |
|---|---|
| **F1** | `tools/verify.sh` exits **0** on the tree this task leaves, and `testQueueVocabularyMutations` is **not excluded** |
| **F2** | The received diagnosis is **verified, not inherited**: the commit that turned the harness red is named, and whether the `SURVIVES` row is a genuine survivor is decided by running the mutation under a **correct** fixture. Falsified if either is asserted rather than run |
| **F3** | Every one of the six mutations is either **re-anchored with its meaning preserved** or **retired by name with its reason**. Falsified by a silent deletion, or by a mutation that no longer replaces its rule wholesale |
| **F4** | The repaired harness reports a **measured baseline of 0** in an unmutated copy, under a fixture that reproduces the tree's layout (`CH-0237`) |
| **F5** | Every mutation in the repaired table **fails at least one named test**, and every classification the gate declares is **held open by at least one named test** — measured in both directions, before and after |
| **F6** | The harness census is **derived by running**, not typed: for every mutation harness in `tools/`, the number of anchors declared and the number that resolve against their own subject, plus whether the harness asserts its anchor count, measures a baseline, and is wired into the build |
| **F7** | An orphaned anchor is a **build failure** from now on — the census is wired, and a synthetic orphan fails a named test of the census's own self-test |

## 5. Conventions

- Counts are integers. The one ratio quoted (`wired / total`) is exact.
- A *harness* is a file in `tools/` whose job is to mutate another tool and measure which named tests fail.
- An *anchor* is the literal text a mutation replaces. An anchor **resolves** when it occurs exactly once in its target.
- A *silent* mutation is one that fails no named test — `C-0161`: that is the finding, not a gap in the list.
- The result file takes `--ref`, defaults it to `HEAD`, and records the **resolved** SHA (`CH-0210`).
