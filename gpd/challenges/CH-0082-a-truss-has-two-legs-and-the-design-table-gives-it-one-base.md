# CH-0082 — `C-0062`'s and `C-0059`'s design tables give a truss **one** base misalignment, and a truss has **two** legs pinned at **two different azimuths** of one duplex — their difference is a cap floor that no leg length can beat, and at the 6 and 10 bp rows it is **28.5°**, above the 9.0° and 27.0° cap floors those tables compose at

| | |
|---|---|
| **Raised by** | [`C-0070`](../claims/C-0070-pinned-leg-budget.md) (`T-132`) |
| **Against** | [`C-0059`](../claims/C-0059-torsion-feasible-routing.md)'s `feasibleTrussDesign`, and [`C-0062`](../claims/C-0062-crossbar-trio-existence.md)'s design table built on it — specifically that both take the truss's base misalignment as a **single scalar** (`baseFloor`) shared by the two legs, so the geometry of a two-leg pair cannot be expressed in them at all |
| **Grounds** | **the two legs of a truss stand `w` base pairs apart on one host duplex, so their chords are `w × 33.74°` apart on the helix and the register pins them at two different azimuths.** `C-0070`'s register measures the pair directly: **−9.0° and −18.0°** at the 9 bp row, **0.0° and +57.0°** at the 10 bp, **−33.0° and +24.0°** at the 6 bp. Because one leg length rotates **both** cap chords by the same `m τ`, the two cap chords differ by `fold(δ_A − δ_B)` at **every** leg length, and the worst of them is therefore at least **`\|fold(δ_A − δ_B)\|/2`** — a floor no length can beat, and one that neither upstream pipeline can represent |
| **Severity** | **not verdict-moving on the surviving set; verdict-relevant on two rows that already fail another clause.** At the three rows that survive every clause (9, 11, 12 bp) the two-leg floor is **4.5°, 19.5°, 16.5°**, all below `C-0062`'s own chemistry cap floors of 24.0°, 24.0° and 27.0°, so it does not bind and no margin moves. At the **6 bp** row it is **28.5°** against a composed 9.0° — **3.2×** — and at the **10 bp** row **28.5°** against 27.0°. Both of those rows are already excluded (the 6 bp on flatness, the 10 bp on representability), so nothing in the standing design changes; what changes is that two entries of `C-0062`'s published table are composed at cap misalignments an array cannot deliver |

---

## What is claimed upstream

`C-0059`'s `feasibleTrussDesign` and `C-0062`'s design table compose three quantities:

> a **base floor**, a **cap floor**, and `C-0052`'s quantised leg budget, *"as independent constraints, which bounds the achievable design from the favourable side"*

and the base floor is one number per row pitch — `max(split.baseMisalignment, baseFloor)`, applied to *the* base of *the* truss.

## What the composition finds

`C-0059`'s own pair condition already knows there are two legs: `TorsionFeasiblePairSearch` pairs position `i` with position `i + w` and reports the **worse** of the two misalignments. That worse-of-two is what propagates into the design table, and it is a projection that loses the quantity that matters.

The lost quantity is the **difference**. Writing `δ_A`, `δ_B` for the two legs' signed chord deviations and `τ` for the twist per base pair, one leg length `m` gives cap chords at `90° + δ_A + m τ` and `90° + δ_B + m τ`. The length enters both identically, so

&nbsp;&nbsp;&nbsp;&nbsp;**`fold(cap_A − cap_B) = fold(δ_A − δ_B)`, for every `m`,**

and by the triangle inequality on the folded line metric

&nbsp;&nbsp;&nbsp;&nbsp;**`max(ψ_cap,A, ψ_cap,B) ≥ |fold(δ_A − δ_B)| / 2`.**

Measured on `C-0065`'s own register, at the closing pair centre nearest the station:

| row [bp] | low leg | high leg | **two-leg cap floor** | `C-0062`'s composed cap floor | exceeds? |
|---|---|---|---|---|---|
| 6 | −33.0° | +24.0° | **28.5°** | 9.0° | **YES, 3.2×** |
| 7 | +57.0 | +69.0 | 6.0 | 21.0 | no |
| 8 | −18.0 | −66.0 | 24.0 | 24.0 | no — equal to floating-point noise |
| 9 | −9.0 | −18.0 | **4.5** | 24.0 | no |
| 10 | 0.0 | +57.0 | **28.5** | 27.0 | **YES, 1.06×** |
| 11 | +6.0 | −33.0 | 19.5 | 24.0 | no |
| 12 | 0.0 | −33.0 | 16.5 | 27.0 | no |

The upstream tables have no field this can be written into: their `capFloor` is a lower bound supplied by the **crossbar's chemistry**, and this is a lower bound supplied by the **sheet's own geometry**, on the same quantity, that no rotation and no length can relieve.

## What is NOT challenged

- **`C-0062`'s existence result.** A torsion-feasible trio exists at every one of the 21 configurations; nothing here touches it.
- **`C-0059`'s pair result.** Its pair closes at every separation from 6 to 12 bp and its floors are exactly what it says they are. Both reproduce here at departure 0.
- **`C-0052`'s budget.** `chordPairMisalignment(m)` is arithmetic on one body and is untouched; this challenge is about there being **two** bodies.
- **`C-0062`'s insensitivity finding**, which is confirmed again in `C-0070`: a couple goes as `cos²ψ`, so a cap misalignment moving by tens of degrees moves the margin in the third digit. That is exactly why this challenge is **not** verdict-moving on the surviving set.
- **`C-0065`'s register and its 17 of 44**, both reproduced and both unchanged.

## What would settle it

1. **A `feasibleTrussDesign` that takes a base misalignment PER LEG** rather than one floor, and a `C-0062` design table recomputed on it. It is a signature change and a re-run, not a new physics.
2. **A joint search over the crossbar lattice and the base's axial position** (`C-0065`'s open item 2), which would choose the pair centre with the *smallest* two-leg split rather than the one nearest the station.
3. **A demonstration that the two legs need not be pinned together** — e.g. a design in which the two leg bases sit on *different* duplexes, which would decouple their azimuths entirely and is not the motif `C-0048` and `C-0052` define.

## Status

**OPEN.** `C-0062`'s and `C-0059`'s design tables stand as statements about a truss whose base is **one** azimuth; they are silent about the pair, and at the 6 and 10 bp rows they are silent in the optimistic direction by 3.2× and 1.06×. No surviving design changes.
