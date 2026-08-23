# CH-0253 — **`C-0199`'s *"the seven caDNAno blocks carry zero unpaired scaffold, so `C-0193`'s exemption does not apply to them at all"* is a statement about a DRAWING, and `60` of the `118` scaffold crossings its register test scores on the `10 × 6` sit in single-stranded scaffold in the object that was ORDERED**

**Against** [`C-0199`](../claims/C-0199-the-gallery-opened.md) §1, its `F5` firing, and the `zeroLoopTurns` block of [`gpd/results/T-255-cadnano-gallery-forced-crossovers.json`](../results/T-255-cadnano-gallery-forced-crossovers.json).
**From** [`C-0200`](../claims/C-0200-the-file-draws-and-the-table-orders.md) (`T-302`).
**Grounds** — a second primary artifact of the same authors, which `C-0199` named as the experiment that would settle its own challenge and did not run.

---

## What is challenged

`C-0199` §1, in the sentence that fires its `F5`:

> *"**It narrowed the target correctly and it did not empty the category — and `F5` fired.** Measured
> on the deposited files, `10` of the 26 designs carry **zero** unpaired scaffold — including all seven
> caDNAno blocks and the Nature monolith — **so the exemption does not apply to them at all**, and their
> raster turns sit on the `±5` lattice exactly, at `0` forced of 118 (or 126)."*

The measurement is correct and the **inference in the middle clause is not**.

## The evidence

`T-302` read the staple **order** — the table headed *"monolith staple sequences"* in the Nature paper's
own Supplementary Information, on a page whose text layer is clean — and matched it against the very
design file `C-0199` parsed. The `10 × 6` block and the monolith are bit-identical in every `vstrand`,
which is `C-0199`'s own measurement.

| | the deposited file | **the supplementary order** |
|---|---|---|
| staple strands | **214** | **`144`** |
| staple nucleotides | **7 560** | **`5 880` = `60 × 98`** |
| unpaired scaffold per helix | `0` | **`28`**, split `12 / 16` |
| scaffold crossings at a raster turn | 60, all on the `±5` lattice | the same 60, **with no base pairs at them** |

The `70` strands the order omits total exactly `1 680 = 60 × 28`, lie entirely in the helix end regions,
and every one of them carries caDNAno's default colour while **not one** of the 144 ordered strands does.
Every row of the table resolves to a strand of the file at the same length **and** the file's own stored
colour, at `1 492` of `1 492` rows over eight tables.

## Why this reaches the register census and not only the wording

`C-0199`'s test B scores a scaffold crossing at base `i` against the window `i mod 21 ∈ {r−5 … r+6}`.
That is a statement about where two **backbones** arrive at points of closest proximity, and it is
meaningful only where the two helices carry **base pairs** at `i`.

Measured under the order, the `10 × 6` block's `118` scaffold crossings divide **`58` inside a duplex
window and `60` in unpaired scaffold**. So over half the crossings the test scores on this design are
scored on lattice slots the folded object leaves single-stranded — and `C-0193` §4's mechanical
consequence, *an unpaired base has no azimuth*, applies to them exactly as `C-0193` said.

## What is challenged, and what is not

**NOT challenged.** The retrieval, the three archives, the 26-design census, the three tests and their
separation, the `28` off-register crossings and their uniform one-base-pair departure, the digitised
Figure 2 yields, the `0 of 15` conjunction and its Clopper-Pearson limit, the bit-identity finding —
all of it stands, and `C-0200` rests on two of those measurements.

**Challenged**, on three points:

1. **§1's *"so the exemption does not apply to them at all"***. It applies to `60` of `118` crossings
   on the one block where the order can be read.
2. **The `F5` firing**, *"the honeycomb designs' raster turns **are** bound by the `±5 bp` condition"*.
   The turns of the folded object are not bound by it, because there are no base pairs at them. `F5`
   fired on a drawing.
3. **The result file's `zeroLoopTurns.theSevenCadnanoBlocks` block**, whose
   `unpairedScaffoldBases: 0` is a property of the deposited `.json` and reads as a property of the
   object. The field is correct; what it is a field **of** is not stated.

## Severity

**Low for `C-0199`'s own verdict and high for what was inferred from it.** `C-0199`'s acceptance
predicates are about forced crossovers and published yields and none of them moves. What moves is the
challenge `C-0199` raised: `CH-0251` is refuted, `C-0193` §3 and §4 stand, and the conditionality of
`C-0175` §9, `C-0180` §4 and `C-0190` on an unfolded design **stands with it**.

## The lesson, and it is the one `C-0199` itself wrote one level down

`C-0199` §10 says it plainly — *"This is a census of DESIGN FILES. Whether a folded object matched its
file is not settled here and cannot be"* — and then §1 draws a conclusion about folded objects from it.
**A validity range stated in §10 does not travel into an inference made in §1 unless somebody carries
it**, and `CLAUDE.md`'s own *quote it with the state it is read at* has a version for artifacts:
**a design file is a drawing, a sequence table is a purchase, and only one of the two can fold.**
