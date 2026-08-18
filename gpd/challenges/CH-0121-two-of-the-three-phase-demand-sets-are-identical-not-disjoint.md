# CH-0121 — **`C-0102`'s headline says the three phase-demand sets *"stay disjoint"* at the buildable width; two of them are IDENTICAL, and its own census table says so.** The verdict is untouched — the richest set is disjoint from the other two, which is what makes the three demands irreconcilable — but the headline as written tells a reader the conflict is three-way when it is two-way, and a synthesis that transfers a headline inherits that

| | |
|---|---|
| **Raised by** | [`C-0106`](../claims/C-0106-fourth-answers-synthesis.md) (`T-175`), while carrying `C-0102` into `ANSWERS.md` §1 |
| **Against** | [`C-0102`](../claims/C-0102-crossover-phase-selection.md)'s **title line** — *"at the buildable 38.08 nm the three sets go from **10 / 10 / 2** to **2 / 2 / 2** and stay **disjoint** — richest `{0, 16}`, eight-column and centro-symmetric `{8, 24}`"* |
| **Grounds** | **a set-theoretic word used of three sets when it is true only of one against the other two.** At 38.08 nm `C-0102`'s own Deliverable 1 table gives *most columns* = `{8, 24}` and *centro-symmetric* = `{8, 24}`. Two sets that are equal are not disjoint; they have both their elements in common. The pair was never disjoint at 40.00 nm either — `C-0090` states that `C-0015`'s ten eight-column phases **collapse onto** `C-0063`'s two, i.e. `{8, 24}` was a **subset** of the ten — so the collapse takes a nested pair to an equal pair, and the only disjointness anywhere in the ledger is the richest set against the rest |
| **Severity** | **one word in a headline. No number, no census cell, no verdict and no recommendation moves.** `C-0102`'s body is precise in both places it says it (*"the richest disjoint from the other two"*, and *"two against two, and the two sets are still disjoint"*), its `all three` column reads `∅` at every width, and phase 8 is still the recommendation at 0.0658484805 |
| **Status** | **STANDS as a statement about the headline, not about the claim.** No re-run, no re-derivation, nothing withdrawn |
| **Resolution owed** | a wording correction to `C-0102`'s title line, at whatever point `C-0102` is next edited. `ANSWERS.md` already carries the corrected reading |

---

## What is claimed upstream

`C-0102`'s Deliverable 1, in full at the buildable width:

| reading | richest upward inventory | most columns | centro-symmetric | all three |
|---|---|---|---|---|
| **38.08 nm, row-end admitted** | **60**: **0, 16** | **8**: **8, 24** | 8, 24 | **∅** |

The `all three` cell is the load-bearing one and it is right: **no phase serves all three demands**, which
is what *irreconcilable* means and what the claim's verdict rests on.

## What is challenged

Only the title's *"the three sets … stay **disjoint**"*.

Three sets are pairwise disjoint when no two share an element. Here:

- richest `{0, 16}` ∩ most-columns `{8, 24}` = `∅` ✔
- richest `{0, 16}` ∩ centro-symmetric `{8, 24}` = `∅` ✔
- most-columns `{8, 24}` ∩ centro-symmetric `{8, 24}` = `{8, 24}` ✘ — they are the **same set**

So the correct headline is *"the richest set is disjoint from the other two, which have collapsed onto each
other"*.

## Why it is worth a challenge rather than a silent fix

Because it is a **transferable** error, and this challenge exists because it was nearly transferred.
A synthesis reads headlines — that is what a headline is for — and *"three demands, all disjoint"* reads as
a three-way conflict on one integer. It is a two-way conflict: the sheet-side demand (eight columns) and the
placement-side demand (a centro-symmetric root lattice) **agree** at the buildable width, and only the
inventory demand disagrees. That is a materially easier design problem than the headline describes, and it
is *also* exactly why `C-0102`'s recommendation — drop the inventory — is the right one and is available at
all.

It is the same family as `CLAUDE.md`'s *"a range that is a min/max over one claim's own table is stated by
no claim"*: a summary line that compresses a table into a word, where the table is right and the word is
not.

## What would falsify this challenge

A reading of *disjoint* under which two identical non-empty sets are disjoint, or a census in which the
eight-column and centro-symmetric sets differ at 38.08 nm. Neither exists: the claim's own table prints
`{8, 24}` twice.
