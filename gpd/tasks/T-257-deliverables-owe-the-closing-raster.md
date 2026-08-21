# T-257 — `ANSWERS.md` and `DECISIONS-FOR-NDI.md` owe the CLOSING honeycomb raster

**Leaf:** `A8.2` (the two outward-facing documents that report every leaf)
**Agent:** AM, iteration 38.
**Reserved:** claim `C-0155`, challenges `CH-0202`, `CH-0203`.

## Formulate

The corpus filed two claims in iteration 37 that neither outward-facing document carries:

- [`C-0151`](../claims/C-0151-closing-raster-selection.md) (`T-245`) — `C-0140`'s selection filter
  and caDNAno's scaffold-closure rule are **exactly disjoint**, and the drawable two-length
  honeycomb raster is **`102 / 109 bp`**, at the **same** 116 bp = 39.44 nm axial extent, for the
  price of **one** crossover column.
- [`C-0152`](../claims/C-0152-forced-scaffold-crossover-price.md) (`T-246`) — a **forced** scaffold
  crossover costs `0.350894669 k_BT` at its ceiling, **sub-thermal**, and all ten of `CH-0188`'s
  cost `0.438634952` of one crossover column of a sheet that folds.

Both deliverables currently recommend, or retain as live reasoning, pairs that do **not** close:
`112 / 108` (10 forced crossovers) and `101 / 109`.

**Numeric target.** Every occurrence of a non-closing raster pair in either document is either
struck or explicitly labelled as retained superseded reasoning, and both documents carry
`102 / 109` with the four numbers that make it a selection rather than an assertion: the same
`116 bp = 39.44 nm` extent, **10** crossover columns, **55 of 60** stations at the determined
phase 16, and the recommended coupled cell at **`0.0773373597`** (`f = 0.30`) /
**`0.0821458169`** (`f = 0.26`) against `T-5b`'s 0.10.

**Acceptance predicates.**

- `P1` A mechanical census of every carrier token runs **before** any prose is written, and the
  work list is measured rather than remembered.
- `P2` Every number written into either document is grepped out of the claim that owns it, at that
  claim's own precision. Any ratio, percentage or min/max assembled here is quoted **with its
  construction** beside it.
- `P3` The **preference / prohibition** distinction is explicit in both documents: closure is a
  reason to *prefer* `102 / 109`, and `C-0152` prices the elastic half of violating it as
  sub-thermal, so `112 / 108` is off-rule and **not** proved unbuildable.
- `P4` No passage is deleted. Superseded text is struck (`~~…~~`) and the replacement stated beside
  it.
- `P5` Every *"still open"* / *"cannot"* sentence in `DECISIONS-FOR-NDI.md` and `ANSWERS.md` §5 is
  re-read against `TASKS.md`, because that document's known failure mode has a **sign**.
- `P6` `tools/trace-answers.py` reads **0 ABSENT, 0 contradicted, 0 STALE-OPEN, 0
  self-contradiction** on **both** documents; `check-markdown-tables.py`,
  `check-corpus-links.py`, `check-challenge-index.py` clean; `tools/T-233-reconcile.py` and
  `tools/T-234-census.py --check` no worse than before this pass.

**Verification type:** **logical** — a grep-located reconciliation of two documents against four
claims and one queue. No physics is derived, no study is run, no result file is emitted.

**Units:** locked, SI, as everywhere; base pairs and rises are integers on the 0.34 nm rise.

## Plan

**Cheap bound first, and it is the whole method.** The expensive half of a document pass is prose;
the cheap half is *locating*. Before a word is written, both documents are grepped for every token
the two new claims bear on — `112 / 108`, `101 / 109`, `102 / 109`, `39.44`, `40.46`, `−1.40 %`,
`0.18`, `CH-0187`, `C-0140`, `C-0146`, `C-0147` — so that the work list is a measurement.

**Cost justification.** There is no cheaper instrument than a grep, and the alternative — reading
2 492 lines of two documents for drift — is exactly the method `C-0067` showed does not converge.

**Falsifiers.**

- `F1` A passage already in either document **contradicts** `C-0151` or `C-0152`, so the owed edit
  is a strike rather than an addition.
- `F2` The sweep finds **nothing** the corpus has answered that a deliverable still calls open.
  (Declared the favourable way: if this does not fire, the documents were under-claiming again.)
- `F3` A derived ratio, percentage or min/max already standing in either document cannot be
  reconstructed from the claim that owns its arguments — `C-0145`'s `F2` drift class.
- `F4` A number written here cannot be grepped out of a claim.
- `F5` A deliverable contradicts **itself** after the edit.
- `F6` The preference/prohibition distinction collapses somewhere — a passage reads closure as a
  proof that `112 / 108` is unbuildable.

## Execute / Verify / File

Recorded in [`C-0155`](../claims/C-0155-tenth-answers-synthesis.md).
