# P-28 — the queue cannot be picked up cold

| | |
|---|---|
| **Leaf** | none — a **process** task protecting the loop's own entry point |
| **Raised by** | the coordinator, iteration 39, on the evidence of `P-27` being un-actionable from `TASKS.md` alone |
| **Status** | see [`TASKS.md`](../../TASKS.md) |

---

## 1. Formulate

### The defect, in three measured parts

A cold session is told to start at [`TASKS.md`](../../TASKS.md).
Three things in it stopped that working, each measured before anything was written.

1. **The orientation was twelve iterations stale in its title.**
   `## Start here — the state after iteration 26` was the heading over the reading order a new
   session is instructed to trust, and the queue was at **iteration 38**.
   Its five numbered items had been annotated with supersessions *in place*, so the note was
   **correct and mis-titled** — which is worse than stale, because a reader who checks the date
   discards five correct annotations, and a reader who does not, trusts a reading order dated
   before four of the corrections it carries.

2. **A fifth of the studies had no way to run them.**
   **99 `Entry points` rows against 122 studies that write a committed result file.**
   Three of the twenty-five missing were the emitters of `P-27`'s own eight red files
   (`PlanToleranceStudy`, `DuplexPairSeparationStudy`, `BeyondMeanFieldGapStudy`),
   so the **blocking** process task of this iteration could not be actioned from this file at all.
   A result file that cannot be re-run from the repository's own documentation is not a result;
   it is a fossil.

3. **The working tree carried a shadow corpus.**
   `./--check/` was **144 result JSONs from 2026-08-19**, untracked, differing from the live
   corpus — a mis-parsed `--check` argument that had built a second `gpd/results/` beside the real
   one. Nothing read it; `CLAUDE.md` records at length what happens when a census reads a
   directory nobody meant it to.

### Numeric target and acceptance predicates

| | predicate |
|---|---|
| **P1** | the `Start here` heading names the iteration the queue is actually at, with the retitling recorded rather than silently applied |
| **P2** | `tools/check-entry-points.py` reads **0 defects** over the committed tree, and every study that writes a committed result file has a row |
| **P3** | the checker is **mechanised, not asserted** — self-tested, wired into `tools/verify.sh`, and failing in **both** directions: a missing row and a wrong `Emits` cell must each fail a **named** test |
| **P4** | `./--check/` gone, and its removal stated so that a later census cannot find it and wonder |
| **P5** | no row of the table is invented: every added row's `Emits` cell is **derived from the study's own source**, and the derivation is the checker's, not a human's |

### Units and conventions

Nothing physical is computed. No result file moves and no study is re-run.

---

## 2. Plan

### The cheap bound, and it changed the design

The census runs before the repair: `grep -rl "^fun main" src/main/kotlin` against the `-Pstudy=`
tokens of `TASKS.md`. That is one pipeline and it returns the size of the job — 25 studies, of
which one (`HelloWorldApp`) writes no result file and is not owed a row.

The **second** cheap bound is what made this a tool rather than an edit.
Deriving each missing row's `Emits` cell by hand means reading a results path out of a source,
and the naive rule is wrong in both directions:

- **ten** of the twenty-four studies read `gpd/results/T-3b-tile-edge-load-profile.json` *before*
  writing their own, so a first-match census gets ten rows wrong;
- **two** (`T-157`, `T-16`) read an upstream result file a hundred lines *after* their own
  `writeText`, so a last-match census gets two wrong.

The first draft of the checker used the last-match rule and duly reported those two as
`EMITS-MISMATCH` against rows that are **correct** — a false positive in the exact shape of a real
finding, on a gate whose whole value is being believed (`CLAUDE.md`: *a drift checker's false
positives cost more than its true ones*). The reading that is about writing at all is to follow
the **binding** to its `.writeText`, and that is what ships, with both wrong rules as named tests.

### Cost

Minutes. No solve, no Gradle, no result file.
The expensive alternative — asserting the table is complete in prose — is what produced the defect.

### What would falsify this approach

- **The table is not meant to be complete.** Then `P2` is the wrong predicate and the honest move
  is `C-0083`'s: state that the table is a *selection*, say what selects, and do not gate it.
  Rejected on the evidence: the three rows missing under `P-27` were not a selection, they were
  an omission, and they blocked the iteration's blocker.
- **A study's write path cannot be derived.** Then the `Emits` column is a human assertion and
  the checker can only compare two assertions. It can: 122 of 124 studies bind their output and
  write it through that binding, and the two that do not write no result file at all.
- **The checker fires on a correct row.** Then it is not a gate. It fired on two, the cause was
  the checker's own rule, and both are now named tests in both directions.
