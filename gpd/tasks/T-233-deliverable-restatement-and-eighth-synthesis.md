# T-233 — Restate the four-layer numbers in the two outward-facing documents, and the eighth `ANSWERS.md` synthesis

**Leaf:** — (process; the two documents are what a reader outside the programme sees)
**Raised by:** [`C-0141`](../claims/C-0141-honeycomb-station-lattice-and-placement.md), repriced by [`C-0140`](../claims/C-0140-honeycomb-raster-turn-sense.md), listed by [`C-0144`](../claims/C-0144-honeycomb-correction-supersession.md)
**Verification type:** logical
**Units:** none new; every number restated is read from the claim that owns it

---

## Formulate

Two deliverables — [`ANSWERS.md`](../../ANSWERS.md) and [`DECISIONS-FOR-NDI.md`](../../DECISIONS-FOR-NDI.md) —
carry four-layer footprint, threshold, station and coupled-flatness numbers that iteration 33 and 34 withdrew.

[`C-0144`](../claims/C-0144-honeycomb-correction-supersession.md) §9 already published the work list:
**41 entries**, 27 in `ANSWERS.md` and 14 in `DECISIONS-FOR-NDI.md`,
each with file, 1-based line, premise family, matched token, class and the exact line,
in `gpd/results/T-234-honeycomb-correction-supersession.json` under `deliverableListForT233`.
**41 of 41 verified in place** when it was published.
This task does **not** rebuild that list; it reconciles it mechanically and then edits.

The eighth `ANSWERS.md` synthesis is folded in because it touches the same two files:
thirteen claims — `C-0131` … `C-0143` — have been filed since the seventh
([`C-0130`](../claims/C-0130-seventh-answers-synthesis.md)), and one of them closes `T-50`.

### The numeric target

| | as the deliverables stand | as the corpus stands |
|---|---|---|
| `15 × 4` footprint | 38.08 × 38.04 nm, *"essentially §3's square"* | **38.08 × 56.524 nm — 1.40084263 of §3's 40.35** (`C-0141`) |
| `10 × 6` footprint | 38.08 × 25.36 nm, *"a third of the footprint"* | **38.08 × 37.504 nm — 0.929467162 of §3's** (`C-0141`) |
| `15 × 4` free-tile dishing | 0.0577199433 | **0.0978155002** (`C-0141`) |
| `10 × 6` free-tile dishing | 0.00874363524 | **0.0240648102** (`C-0141`) |
| `15 × 4` coupling-fraction threshold | `f` = 0.0788618807, cleared 3.29690337× | **0.276970522 — INSIDE the measured 0.26–0.33 band** (`C-0141`) |
| `15 × 4` at `f` = 0.26 | 0.0612595739, flat | **0.101759944 — FAILS `T-5b`** (`C-0141`) |
| coupled cells flat at the 90th percentile | 9 of 16, all eight `10 × 6` cells | **4 of 16, all four on `10 × 6`; `15 × 4` is 0 of 8 at both ends of the band** (`C-0142`) |
| best coupled cell | 0.0278431488 | **0.0680677948** (`C-0142`) |
| the cross-section's worth | 3.17109774× | **2.13543134×** (`C-0142`) |
| station census, top face | 132 on `15 × 4`, 90 on `10 × 6`, at 60° | **90 and 60, at 30°, one azimuth per helix, no perpendicular root** (`C-0141`) |
| tile width | 38.08 nm, uniform 112 bp | **112 / 108 bp, axial extent 116 bp = 39.44 nm** (`C-0140`) |
| the scaffold | p8064 | **NOT ESTABLISHED — the paper contradicts itself at exactly design (i)** (`CH-0180`) |

### Acceptance predicates

- **`P1`** Every one of `C-0144`'s 41 entries is located by **exact line text**, not by line number, and dispositioned
  **RESTATED / LEFT / DEFECT** with a reason. The reconciliation is mechanical and retained,
  and a `DEFECT` — a withdrawn premise still standing unqualified — fails the gate.
- **`P2`** Every number written into either deliverable is grepped out of the claim that owns it,
  at the precision that claim states — neither rounded nor extended.
- **`P3`** *Decision 7* is re-posed or withdrawn, in writing, with the trade it assumed named as absent.
- **`P4`** The eighth synthesis pass runs on **both** documents: every *"still open"*, *"cannot"* and
  *"unmeasured"* sentence re-read against `TASKS.md`, and every claim filed since `C-0130` reconciled.
- **`P5`** `tools/trace-answers.py` reads **0 ABSENT, 0 contradicted, 0 self-contradiction** on both files;
  `check-markdown-tables.py`, `check-corpus-links.py`, `check-challenge-index.py` and `tools/verify.sh` are clean.

### Falsifiers

- **`F1`** An entry of the 41 cannot be located by its exact line text — the list is stale and must be rebuilt.
- **`F2`** A number written here cannot be grepped out of a claim — the synthesis has become a source.
- **`F3`** Decision 7 survives as posed: the footprint trade it offers still exists somewhere in the corpus.
- **`F4`** The sweep finds nothing the deliverables call open that the corpus has answered —
  i.e. `C-0067`'s under-claiming failure mode has stopped operating.
- **`F5`** A deliverable contradicts itself, and the contradiction is one a previous pass introduced.

## Plan

**Cheap bound first, and here it is a reconciliation rather than an arithmetic.**
The expensive half of this task is reading; the mechanical half is locating.
So the 41 entries are located by exact line text **before any prose is written**
(`tools/T-233-reconcile.py`), which converts *"find the moved passages"* into *"edit 21 physical lines"*
and makes the edit auditable: a re-run after the edit must find each entry either struck or restated.

The synthesis half then runs on the corpus, not on memory:
`tools/trace-answers.py` for numeric provenance and queue status,
plus a hand read of every claim filed since `C-0130` against the two documents' assertions.

**What would falsify this approach:** if the 41 entries turn out not to be the moved set —
if editing them leaves a withdrawn premise standing in either file that a fresh census finds —
then the token-shaped census is the wrong instrument and a row-shaped or section-shaped one is needed.
`C-0144` §12 already records the two known blind spots (five premise families are a choice; the pointer test is forward only).
