# T-240 — `ANSWERS.md` and `DECISIONS-FOR-NDI.md` owe the honeycomb TURN SLACK and the RAGGED FACE

**Leaf** — none of its own; it reports `A8.2`.
**Verification type** — **logical**. No physics is derived here. Every number written into either
document is read out of the claim that owns it, at that claim's own precision.
**Units** — locked, as everywhere: nm, pN, `k_BT` = 4.142 pN·nm at 300 K.

## Formulate

[`C-0147`](../claims/C-0147-honeycomb-turn-slack-and-ragged-face.md) (`T-230`, `T-231`) filed two
results in iteration 35 that the two outward-facing documents do not carry, because both documents
were owned by another agent that iteration.
Both results *remove* a cost the deliverables would otherwise carry, which is the direction
`C-0067`'s standing finding says is hardest to catch:
**a deliverable that under-claims is as wrong as one that over-claims.**

### Acceptance predicates

- **`P1`** — both documents state that a honeycomb raster turn needs **6 nt by reach** and that the
  built **28 nt is a CHOICE**, `4.66666667×` its own bound; and that the minimum is a **criterion,
  not a number** — reach 6, one `k_BT` 16–22 nt, one pN 29–41 nt, with the built 28 inside that
  bracket at **1.00195245–1.46667915 pN** and **0.518481856–0.7570064 `k_BT`**.
- **`P2`** — both documents state that M13 affords exactly **8 nt** at a 112 bp row
  (`60 × (112 + L) ≤ 7 249` gives `L ≤ 8`), so the uniform-row route **fits, by two nucleotides**,
  and fits only **strained**: 6.54349121–12.112167 pN, at or past the 10 pN unzip allowable.
  Any *"the widest four-layer tile is 92 bp"* reading is therefore a ceiling on **one published
  allowance**, not on the route.
- **`P3`** — both documents state that the yield half is **declared unpriceable** — no published
  measurement is on the scaffold-turn-loop axis — and quote the **threshold 8 nt** instead.
- **`P4`** — both documents state that the 4 bp ragged face costs §3's flatness **exactly zero**,
  because it is on the tile's **RIM** and not on its gap-facing surface, with the bound
  (`5.54399427e−05` on `15 × 4`, `1.68371917e−05` on `10 × 6`, against `0.0274976866` of headroom,
  a margin of **496×**); and that what it **does** cost is **plan budget** — 1.36 nm against an
  outboard bound that saturates at **2.380 nm**, i.e. **0.571** of it at 90 demanded paths.
- **`P5`** — every number written is grepped out of the claim that owns it, and any ratio,
  percentage or min/max **this document assembles** is quoted **with its construction**
  (`C-0145`'s `F2` drift class).
- **`P6`** — all five retained document checkers clean on **both** files, plus
  `tools/T-233-reconcile.py` and `tools/T-234-census.py --check` if this edit touches their scope.

### Falsifiers

- **`F1`** — a passage already in either document **contradicts** `C-0147`, so the owed edit is a
  strike rather than an addition. (Expected: not fired — a grep for `92 bp`, `31.28`, `28 nt` and
  `ragged` finds nothing in either file.)
- **`F2`** — a number written here cannot be grepped out of a claim: the synthesis has become a
  source. **Declared open**, since two of the four figures wanted (`0.571`, the 496× margin) are
  ratios.
- **`F3`** — `CH-0186`'s challenge against `CH-0173`'s 92 bp ceiling turns out to be carried by one
  of the deliverables under a different form of words, so the addition is a duplicate.
- **`F4`** — a sweep of the two documents against `TASKS.md` finds **nothing** the corpus has
  answered that a deliverable still calls open, i.e. `C-0067`'s failure mode has stopped operating.
- **`F5`** — a deliverable contradicts itself after the edit (`tools/trace-answers.py`
  self-consistency pass non-zero).

## Plan

**Cheap bound first, and it is a grep.** Locate every passage in the two documents that states a
honeycomb row length, a turn, a face or a width, *before* writing prose — the same discipline
`C-0145` used to turn *"find the moved passages"* into *"edit N physical lines"*.
Then decide, per passage, whether the owed repair is an **addition** (`C-0147` supplies something
the document never had) or a **strike** (the document states something `C-0147` withdraws).
No study is run and no result file is emitted; this task edits documents.

**What would falsify the approach**: if the honeycomb passages in the two documents turn out to be
scattered across many unrelated sections rather than concentrated in the four already known
(`ANSWERS.md` 301, 940, 1068, 1276–1277; `DECISIONS-FOR-NDI.md` 831, 860–864), a line-located edit
is the wrong instrument and the pass has to be rebuilt as a section rewrite.
