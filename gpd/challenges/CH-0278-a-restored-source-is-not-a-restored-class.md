# CH-0278 — A MUTATION HARNESS RESTORES THE **SOURCE** IT MUTATED AND CANNOT RESTORE THE **COMPILED CLASS**, SO A RE-USED SNAPSHOT MAKES THE BASELINE MEASURE THE PREVIOUS INVOCATION'S LAST MUTANT — AND `CH-0237`'s SUBTRACTED BASELINE THEN MIS-CLASSIFIES A STALE BUILD AS A TEST GAP

| | |
|---|---|
| **Against** | [`CH-0237`](CH-0237-a-mutation-harness-layout-is-a-premise-of-its-own-measurement.md)'s subtracted baseline **as sufficient**, and the ten `TEXT-ANCHOR` Kotlin-subject harnesses [`tools/P-31-harness-census.py`](../../tools/P-31-harness-census.py) declares, none of which asserts that the snapshot it is handed has not already been mutated |
| **Not against** | `CH-0237` itself, which is **correct and is what made this visible at all** — an unsubtracted harness would have printed the same two rows as `killed` and said nothing; nor [`C-0190`](../claims/C-0190-the-departure-is-common-mode-and-what-replaces-it.md), whose stray-copy check is a different mechanism and passes here |
| **Raised by** | [`C-0216`](../claims/C-0216-the-placement-and-the-distribution-together.md) / [`T-323`](../tasks/T-323-the-placement-and-the-distribution-together.md) |
| **Grounds** | methodological — `C-0190`'s *a green baseline plus every mutation of one file surviving is a statement about the BUILD, not about the tests*, met on the **incremental compiler** rather than on a second copy of the source |
| **Status** | **RAISED, and REPAIRED in `tools/T-323-mutation-test.py`.** No number of any claim moves; what moves is what a `SURVIVED` row is allowed to mean |

---

## The defect, measured rather than argued

`tools/T-323-mutation-test.py` was run **three times**. Runs 2 and 3 differ in **one line of the
harness and nothing else** — the same snapshot, the same sources, the same 29 named tests.

| run | snapshot | baseline | verdict |
|---|---|---|---|
| 1 | fresh | exit 0, 0 named failures | 25 mutations, **9 survivors** — nine genuine fixture gaps, repaired |
| 2 | **the snapshot run 1 had already mutated** | exit 1, **1** named failure | 25 mutations, **2 survivors** (`M24`, `M25`) |
| 3 | **the same snapshot**, harness repaired | exit 0, 0 named failures | 25 mutations, **0 survivors** |

Run 2's baseline failure is
`gate 1 -- the paired median ratio is not the ratio of two order statistics`,
and that is **exactly** the test that kills `M24` and `M25`. Subtracted as baseline noise, its two
killers vanished and the two mutations were reported as **survivors**.

**The source was not at fault and this is checkable three ways.** `diff -q` puts the snapshot's
`tile/JointPlacementDistribution.kt` byte-identical to the checkout; the arithmetic is right (the
median of `a/b` over the fixture is `0.5`); and the same tree run with `--rerun-tasks` gives
**29 tests, 0 failures**. What the baseline bound to was the **compiled class** of run 1's *last*
mutation — `M25`, which inverts `a[it] / b[it]` to `b[it] / a[it]` and returns exactly the `2.0`
the assertion printed.

## Why it happens, and why it is general

A harness copies the file aside, writes a mutant, runs, and moves the copy back. That restores the
**source**. Kotlin's incremental compiler then compares the restored file against the build state
it last compiled — which is the *pre-mutation* state — finds them the same, and does nothing. The
class on disk stays the mutant's.

Within one invocation this is harmless: **a mutation always differs from the last compiled state
and therefore recompiles.** The only stale transition is back to the unmutated source, and that
happens exactly **once** — at the baseline of the *next* invocation.

**And the failure direction is the flattering one.** The mutations whose only killer is the
baseline's own failure are subtracted into `SURVIVED`, so the harness reads as having a **test
gap** when what it has is a **stale build** — and the prescribed response to a survivor is to go
and write a fixture that already exists. `C-0161`'s *a mutation that fails nothing is the finding*
is right, and this is the one case where the finding is about neither the tests nor the corpus.

## What the challenge asks

1. That the **baseline** run of every Kotlin-subject mutation harness force a recompile.
   `tools/T-323-mutation-test.py` now passes `--rerun-tasks` on the baseline alone: one line, one
   extra compile, and provably sufficient for the mechanism above.
2. That the standing instruction *"give it a snapshot, never the checkout"* be read as
   **"give it a snapshot it has not already mutated"** — the refusal the harnesses carry checks
   for `.git` and cannot see a snapshot they have themselves dirtied.
3. That a `SURVIVED` row be read as a statement about the tests **only** once the baseline is
   known to have been compiled from the source it claims to be.

## What does NOT move

No published number of any claim. `T-323`'s own mutation table is the **run 3** reading —
25 mutations, 0 survivors, over a subtracted baseline of 0 — and run 1's nine survivors were nine
real fixture gaps, all of them repaired and none of them this defect.
