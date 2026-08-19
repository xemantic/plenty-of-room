# CH-0163 — *"the worst field moved by X"* is a statement about **which fields a study emits**, not about how far a run moved: the same flip in the same argmin reads `5.95e−3` on `max_i k_i`, `7.55e−4` on a peak support force at the **same** point, and `2.62e−3` on the objective — and the block that carries the corpus's **widest** VALUE movement is the one nobody quoted, because it emits no argmin functional at all

| | |
|---|---|
| **Against** | the repository-wide practice of reporting a re-run or re-emission difference as *"N fields moved, worst `X`"* — [`C-0131`](../claims/C-0131-departure-and-saturation-audits.md) §6 (*"moved 10 non-departure fields, worst **0.60 %**"*), and the same shape in [`C-0101`](../claims/C-0101-re-emitting-what-the-repair-moved.md), [`C-0110`](../claims/C-0110-device-b-tall-gap.md) and [`C-0129`](../claims/C-0129-result-file-hygiene.md) |
| **Raised by** | [`C-0135`](../claims/C-0135-descent-manifold-width.md) (`T-215`), iteration 31 |
| **Grounds** | **one measurement, four readings of one event.** A single change of winning start in `T-129`'s `ranges[1]` reads `5.9522e−3` on `minimaxPeakRatio` (and identically on the three other rescalings of `max_i k_i`), `7.5482e−4` on `peakSolvedPathForce` at the **same** argmin, and `2.6197e−3` on the objective itself — a factor of **7.9** between two functionals of one point |
| **Status** | **OPEN — a methodological correction, not a defect.** No number in any of the named claims is wrong; what is wrong is reading the worst-field figure as a property of the run |
| **What moves** | **Nothing physical.** What moves is what a re-emission or control-run report has to say: the worst field **and what kind of quantity it is** |

## The charge

Every diff-based claim in this repository quotes a worst relative movement.
It is the right instrument for its original job — *"did a repair move a number"* — where the answer is a zero or a non-zero and the size is secondary.

It is the wrong instrument the moment the size is the finding, because **the size is set by the emitted field list**.

`T-129`'s `ranges[*]` blocks emit five functionals of the minimax argmin. Four of them — `minimaxPeakRatio`, `peakPathStiffness`, `peakPathForceAtAcceptableStroke`, `peakThermalForce` — are `max_i k_i` rescaled by a constant, so they carry the same relative movement to one part in a million. The fifth, `peakSolvedPathForce`, is a peak **support force** over the states at the same point, and it moves **7.9× less**.

So of one event — the descent landing on the other member of a two-valued optimal set — the file could honestly have been reported as moving by `5.95e−3`, by `2.62e−3` or by `7.55e−4`, and which one is the headline depends only on which fields somebody chose to serialise.

## The sharper half: the widest VALUE movement was never quoted

`C-0131` separated its observation into *"10 non-departure fields, worst 0.60 %"* at `ranges[1]` and *"7 `subsets[*]` at ≤ `8.6e−4`"*, and treated the first as the problem.

Over a ten-member ensemble the subsets are the **worse** half in the only quantity the two blocks share:

| quantity | width |
|---|---|
| `ranges[1]` objective | `2.6197e−3` |
| worst `subsets[*]` objective | **`4.5745e−3`** |

`subsets[8]` takes **7 distinct readings** over ten members and `subsets[2]` takes **6**, against `ranges[1]`'s **2**.
The subsets look quieter for one reason: **they emit no argmin functional at all**, only `minimaxWorstOverStroke`.

A block that emits its argmin is loud; a block that does not is quiet; and the loudness ordering is the reverse of the instability ordering.

## What to do instead, and it costs nothing

Report a re-run difference as **a count by kind**, not a scalar:

> *27 fields of 1042 move: 10 objectives (worst `4.57e−3`), 5 functionals of an argmin (worst `5.95e−3`), 10 deliberate roundings, 2 prose renderings, 0 unclassified; 0 booleans and 0 verdicts.*

The classification is per-study and cheap — `T-215`'s is a set of leaf-key names — and it says the two things a scalar cannot: **what kind of quantity moved**, and **whether anything decided moved**.

`CLAUDE.md` already carries the two halves of this separately — *"a moved STRING is not necessarily a moved decision"* and *"report the residual rather than asserting byte-identity — and check what depends on the POINT rather than on the VALUE"*.
This challenge is the third: **a worst-field movement has no meaning until the field is classified**, and a claim that quotes one without saying which functional it sits on has reported an emission choice.

## What would overturn this

A study whose emitted fields are all functionals of the same kind, where the worst-field figure and the classified figure coincide — which is common, and is exactly why the practice survived.
The charge is not that the scalar is always misleading; it is that nothing in the corpus currently says **when** it is.
