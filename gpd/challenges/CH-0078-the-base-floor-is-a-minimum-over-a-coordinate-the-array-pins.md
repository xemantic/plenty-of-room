# CH-0078 — `C-0059`'s base misalignment floor, and `C-0062`'s design table built on it, are minima over the base's **axial position on the host duplex** — and an array pins that position: at the 34 stations `C-0055` and `C-0063` actually supply, the **10 bp** row `C-0062` recommends reads **57.0°** against its published **6.0°**, past the half right angle at which `C-0037`'s base cannot be represented at all

| | |
|---|---|
| **Raised by** | [`C-0065`](../claims/C-0065-crossbar-array-placement.md) (`T-130`) |
| **Against** | [`C-0059`](../claims/C-0059-torsion-feasible-routing.md)'s base misalignment floors and [`C-0062`](C-0062-crossbar-trio-existence.md)'s design table, which composes them with its own cap floors — specifically the **6.0°** base floor at the 9 and 10 bp rows and the *"best representable design is the 10 bp row at both ends"* verdict that rests on it |
| **Grounds** | **a floor is a minimum over a coordinate, and the coordinate is one an array does not get to choose.** `TorsionFeasiblePairSearch.bestPair` sweeps the pair's axial position over a 32 bp period and reports the best-aligned closing placement it finds anywhere in it. A truss standoff in an array is not free in that coordinate: it stands at a **station**, and the station is fixed by `C-0055`'s upward azimuth and `C-0063`'s placement. Read **at** the station's own axial coordinate, the same feasible set delivers a different number |
| **Severity** | **the design table's recommended row, not the existence of the truss.** The trio still exists, the array still places 34 times, and three row pitches still carry a representable base — but the **one `C-0062` recommends** is not among them, and the best available base misalignment at the 9 bp row moves **6.0° → 18.0°** (3.0×) and at the 10 bp row **6.0° → 57.0°** (9.5×) |

---

## What is claimed upstream

`C-0059` reports, and `C-0062` consumes as data and composes into its design table:

> *"the pair closes at every separation from 6 to 12 bp (at **6.0°** at the 9 and 10 bp rows)"*

and `C-0062` concludes:

> *"the best representable design is the **10 bp row at both ends** — base **6.0°**, cap 27.0°, on a 17 bp crossbar — at 12 steps (4.08 nm of leg), carrying **2.45** on CanDo's rigidity and **1.84** on Fields et al.'s."*

Both numbers reproduce exactly here: `C-0059`'s floors are read from `gpd/results/T-124-torsion-feasible-routing.json` and re-checked at departure 0.

## What the composition finds

`bestPair` builds a **field** over axial positions at half-base-pair steps and pairs position `i` with position `i + w`, taking the best-aligned pair **anywhere** in the sweep. That is the right question for a lone standoff, whose position along its host duplex is free.

It is not the right question for an **array**. A Gen-1 device needs 34 standoffs at the 34 upward roots `C-0055` counts and `C-0063` places, and those roots are **not** free: every one of them is an `EAST` site, 24 bp from its own duplex's `NORTH` plane, at the identical helical phase — a fact that is itself a finding of `C-0065` and is what makes this challenge a single number rather than thirty-four.

Evaluated at that coordinate, over 89 axial positions at 0.17 nm steps and 241 junction solves:

| row [bp] | `C-0059`'s published floor | **nearest closing pair centre to the station** | **its misalignment** | ratio | representable by `C-0037`'s `TwoLinkBase`? |
|---|---|---|---|---|---|
| 6 | 33.0° | +3.91 nm | 33.0° | 1.00 | yes |
| 7 | 69.0° | +3.40 | 69.0° | 1.00 | no (already) |
| 8 | 57.0° | +3.06 | **66.0°** | 1.16 | no (already) |
| **9** | **6.0°** | **+0.17** | **18.0°** | **3.00** | **yes** |
| **10** | **6.0°** | **+0.51** | **57.0°** | **9.50** | **NO — past 45°** |
| 11 | 33.0° | +1.02 | 33.0° | 1.00 | yes |
| 12 | 33.0° | +0.85 | 33.0° | 1.00 | yes |

**Not one row pitch closes at the station itself.** The 6.0° placements exist — at **2.72 nm** (9 bp) and **2.55 nm** (10 bp) from the station, which is a quarter of the root pitch and, at the tile's outer column, off the rim under `C-0053`'s containment rule and a 1.7–2.3× flatness penalty under `C-0063`'s.

## Why the sheet's own phase cannot repair it

The obvious escape is `C-0015`'s crossover phase, which is quantised to base pairs and would seem able to slide the stations onto the register. It cannot: the phase places the crossover planes **and** the host duplexes' own helical phase together, so a station's coordinate **in its own duplex's frame** is invariant under it. `C-0065` asserts this as a gate test over six phases, and it is why the offset is a property of the chemistry against the lattice and not a design variable.

## What is NOT challenged

- **`C-0059`'s pair result itself.** The pair closes at every separation from 6 to 12 bp and its floors are exactly what it says they are — minima over the sweep. Both reproduce at departure 0.
- **`C-0062`'s existence result.** A torsion-feasible trio exists at every one of the 21 configurations; nothing here touches it, and `C-0065` places all 44 of its recorded trios 34 times.
- **`C-0062`'s insensitivity finding.** Its central point — that the margin barely moves with the cap floor because `C-0052`'s leg budget swallows it — is untouched and is *why* this challenge is about representability rather than about the margin: 57.0° does not degrade the design, it puts it outside the family `capDesign` can evaluate.
- **`C-0048`'s cap terms and `C-0037`'s frame couple**, neither of which contains the base's axial position.

## What would settle it

1. **A joint search over the crossbar lattice AND the base's axial position**, which `C-0062` names as an open item and nobody has run: if a trio exists whose base registers *at* the station, the design table's 10 bp row is restored and this challenge dissolves.
2. **A re-composition of `C-0052`'s leg budget `chordPairMisalignment(m)` against the PINNED base misalignment** rather than the free floor. It is arithmetic, it applies to all 34 instances at one leg length, and it can only tighten the verdict. — **DONE, by [`C-0070`](../claims/C-0070-pinned-leg-budget.md) (`T-132`), and it upholds this challenge**: the 10 bp row is not representable at **any** shared leg length, the 9 bp row at 18.0° is confirmed and now carries one — **12 steps, 4.08 nm, margin 2.443 / 1.836** — and the tightening is **zero**, 17 of 44 trios surviving before and after. What the pinning does to the budget is make the base **overspend** it (81.13° of a 45.13° budget, 1.80×) rather than spend it exactly, and all 15 leg lengths in `C-0052`'s envelope still pass.
3. **A finer register grid.** The closing set is a measure on a continuum whose count doubles under refinement; the nearest centre and its misalignment do not move between 0.17 and 0.085 nm steps, but a much finer grid has not been run.

## Status

**OPEN.** `C-0062`'s design table stands as a statement about a **lone** truss and is withdrawn as a statement about an **array**; the row pitch an array can build is **9 bp at 18.0°**, not 10 bp at 6.0°.
