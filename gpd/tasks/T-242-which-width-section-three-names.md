# T-242 — Which width reading §3 is owed for a two-length honeycomb block: the bounding box or the row length

**Leaf** — `A8.2`.
**Verification type** — **logical**, and the deliverable is a **question**.
No model in this repository can represent the object the question is about, so no calculation here
can decide it. Units locked: nm, base pairs at the 0.34 nm rise.

## Formulate

[`C-0146`](../claims/C-0146-coupled-cells-at-the-two-length-raster.md) §1 establishes that a
two-length honeycomb raster has **two defensible width readings and they are not the same tile**:

| reading | value | what it is |
|---|---|---|
| **row length** | 112 bp = **38.08 nm** | the span **every one** of the block's x-raster rows actually has |
| **bounding box** | 116 bp = **39.44 nm** | the axial extent the folded object occupies |

and that the `3.57 %` between them is **not a length at all**: it is a **4 bp = 1.36 nm inter-row
STAGGER**. Over all five of `C-0140`'s candidate pairs and both 60-helix cross-sections, every
raster row spans the **larger** of the two lengths exactly and the block exceeds it by exactly the
stagger (3–8 bp).

**A smeared plate has one `lengthX`**, so neither `OrigamiGrillage` nor `HoneycombCoupledTile` can
represent the stagger, and `C-0146` §9 records the choice as *"a specification question this claim
does not settle"*.

**It is not cosmetic.** Through `CrossoverLayout.EDGE_MARGIN` the 116 bp box clears a **twelfth**
crossover column by **0.07 nm** — one fifth of a base-pair rise — and that column alone is the
difference between **6 flat cells of 8** and **3** (`C-0146` §3, §4; `CH-0185`).

### Acceptance predicates

- **`P1`** — the question is posed in **both** outward-facing documents, with both readings, their
  values, the `3.57 %` and the fact that the difference is a **stagger**.
- **`P2`** — the **consequence** is stated: 6 flat cells of 8 against 3, through a crossover-column
  count decided by a 0.07 nm slack against a numerical guard.
- **`P3`** — **the price line is left explicitly open.** `CLAUDE.md` records that a question whose
  admissible answers are enumerated cannot return the one that was not enumerated, and that the
  missing shape is usually a **price**: when `DECISIONS-FOR-NDI.md` offered six questions as
  yes/no, all three with a cost dimension came back in a shape it had not offered. So the question
  must carry *"and if this costs something, what?"* in as many words.
- **`P4`** — the question does **not** ask this repository's models to break the tie, and says why:
  no model here has a per-row row length.
- **`P5`** — the numbering is consistent with `C-0145`'s re-posing of decision 7 as **7a/7b**,
  which is not undone.

### Falsifiers

- **`F1`** — §3 of the problem definition already fixes the convention, so the question does not
  exist. Checked by reading `third-party/2026-08-ndi-gen1-problem-definition.md` §3 directly.
- **`F2`** — the two readings turn out to be within the precision §3 states its own dimension to,
  so the question is immaterial. **Declared open.** (Checked: §3's parameter table writes
  *"Tile footprint — 40 × 40 nm (test tiles up to ~70 × 100 nm)"* — the tilde is on the **test
  tiles** and **not** on the nominal, which this corpus has been writing as *"~40 × 40 nm"*
  throughout. So the footprint is stated without a tolerance and `F2` cannot fire on the
  specification's own wording.)
- **`F3`** — a claim in the corpus already answers it, in which case this is a restatement and not
  a question.

## Plan

**Cheap bound: read §3 first.** One `grep` of the problem definition settles whether the convention
is already specified — and if it is, the whole task collapses to a restatement.
Then pose the question in `DECISIONS-FOR-NDI.md` as a numbered decision alongside the existing
seven, and mirror it into `ANSWERS.md`'s NDI table, following `C-0145`'s conventions exactly.

**What would falsify the approach**: if the answer turns out to be derivable — for instance if
every downstream consumer of the width could be shown to depend only on the row span — then this is
an analysis task and not a specification question, and it should be filed as one.
