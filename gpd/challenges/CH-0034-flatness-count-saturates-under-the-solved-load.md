# CH-0034 — The attachment count for flatness saturates: under the load `T-3b` actually solved, 225 attachments are no flatter than 45 and neither is flat

| | |
|---|---|
| **Against** | [`C-0006`](../claims/C-0006-tile-load-distribution-and-flatness.md)'s *"the minimum number of load paths … dishing below 10 % of the stroke: 64"*, [`C-0009`](../claims/C-0009-discrete-lattice-tile.md)'s solved 64 and [`C-0015`](../claims/C-0015-crossover-phase-and-registration.md)'s **45 as 3 × 15**, **as design numbers** — i.e. as counts that make the Gen-1 tile flat, rather than as counts that make it flat *under a uniform load* |
| **Raised by** | [`C-0026`](../claims/C-0026-one-row-per-duplex.md) (`T-17`) |
| **Date** | 2026-08-13 |
| **Grounds** | methodological — an **optimisation run against the one load case in which the objective is identically zero at infinite count**, then quoted against a load case in which it saturates at a non-zero floor |
| **Direction** | **neutral on the counts, unfavourable on what they buy.** 45 remains the smallest flat one-row grid under the load its criterion is written on; under `C-0022`'s solved load **no count is flat**, and going from 45 to 225 attachments buys 6.9 percentage points of stroke and then stops |
| **Status** | raised. **No count and no verdict of `C-0006`, `C-0009` or `C-0015` moves.** What moves is the sentence *"45 attachments make the tile flat"*, which becomes *"45 attachments exhaust what attachments can do"* |

---

## What is challenged

`C-0006` derives its flatness requirement — 55, corrected by `C-0009` to 64, corrected by `C-0015` to
**45 as 3 × 15** — as the smallest attachment array keeping the peak dishing below `RIGID_PLATE_TOLERANCE` =
10 % of the stroke. All three evaluate that criterion under a **uniform** load with the attachments carrying
the reaction.

Under a uniform load a free tile on a uniform foundation dishes **exactly zero** (`C-0006`'s own gate 2), so
what the criterion is actually measuring is the *discreteness* of the reaction — the sag between attachments —
and that quantity falls monotonically to zero as attachments are added. **An objective that tends to zero
always has a crossing, and where the crossing sits is set entirely by the tolerance.**

`C-0022` has since solved the load, and it is not uniform: the rim carries an **enhancement**, and `C-0022`
reports the dishing that follows as **21–44 % of the stroke, irreducible** — *"the split is 32 % of the stroke
at the design point, and it cannot be designed away"*.

---

## Ground 1 — the count saturates, and it saturates above the tolerance

`C-0026` runs the same criterion on the same lattice with the same 33.333 pN/nm coupling, under both loads.
Peak dishing as a fraction of the free-tile stroke (4.907 nm at `k_f` × 1):

| grid | 1 × 15 | 2 × 15 | **3 × 15** | 4 × 15 | 5 × 15 | 8 × 15 | 15 × 15 |
|---|---|---|---|---|---|---|---|
| attachments | 15 | 30 | **45** | 60 | 75 | 120 | **225** |
| **uniform load** | 0.426 | 0.135 | **0.049** | 0.024 | 0.015 | 0.005 | **0.001** |
| **`C-0022`'s solved load** | 0.695 | 0.350 | **0.218** | 0.182 | 0.168 | 0.155 | **0.149** |

Under the uniform load the criterion behaves exactly as `C-0006` expects: it crosses 10 % between 30 and 45
attachments and then falls another 50× — so **`C-0015`'s 45 is confirmed, on its own load case**, and this
challenge is not a correction of it.

Under the solved load the curve **saturates at 0.149** and never reaches 0.10 at any count. Five times the
attachments buys 6.9 percentage points; the last 105 of them buy 0.6. The tile is not flat at 45, at 64, at
225, or at any number.

## Ground 2 — the two halves of the dishing have different sources and only one is bought with attachments

The saturation is not a numerical artefact; it is `C-0022`'s edge effect, which no interior attachment can
reach. Decomposed at the design point:

| term | value | bought with attachments? |
|---|---|---|
| sag between attachments | 0.049 of the stroke at 3 × 15, → 0.001 at 15 × 15 | **yes**, and it is already spent at 45 |
| `C-0022`'s edge enhancement | **0.149 of the stroke, floor** | **no** — it is a rim collar 8.9 nm wide |

`C-0022` reaches the same floor on the free plate (0.321 of the stroke) and `C-0026` reaches 0.149 with the
33.333 pN/nm coupling in place, which is the same statement with the coupling's own stiffening divided out:
**the coupling halves the edge dishing and the attachment count does not touch it.**

## Ground 3 — and therefore what 45 buys has to be restated

`C-0006`'s §4(g) conclusion — *"the output coupling must be distributed over essentially the whole tile;
there is no concentrated design that both survives and stays flat"* — **stands and is strengthened**: a
concentrated coupling is worse on both terms. What does not stand is the implication that a distributed
coupling at the derived count *achieves* flatness. It achieves the part of flatness that is a coupling
problem, and hands the rest to the electrostatics.

That is a **design-space** statement rather than a numerical one: the lever-versus-sensor split `C-0022`
resolved at 32 % of the stroke is now shown to be **the whole of the remaining dishing on the lattice too**,
and the attachment count is not an axis on which it can be attacked.

---

## What this does *not* challenge

- **The counts.** 45 (`C-0015`), 64 (`C-0009`) and 55 (`C-0006`) are all correct evaluations of their own
  criterion under their own load case, and `C-0026` reproduces `C-0015`'s: 3 × 15 is the smallest one-row grid
  under 10 % of the stroke, at 4.9 %.
- **`RIGID_PLATE_TOLERANCE` = 0.10**, which `C-0015` already flags as *"`T-5b`'s convention, not a physical
  threshold"*. The saturation is 1.5× above it and would survive a tolerance twice as loose.
- **`C-0022`'s dishing numbers**, which are consumed here and reached independently through the lattice.
- **Anything about the per-load-path forces**, which are the subject of `C-0026` proper and of
  [`CH-0033`](CH-0033-thermal-excitation-is-not-a-load-non-uniformity.md).

## The remedy proposed

Quote the flatness count **with the load case it was minimised under**, exactly as this programme now quotes a
stiffness with a compression, a variance with a bandwidth, a rupture force with a loading rate and a `k_es`
with a gap — the fifth instance of the same discipline. Concretely:

> **45 attachments as 3 × 15 is the count at which further attachments stop buying flatness**, not the count
> at which the tile becomes flat. Under `C-0022`'s solved load the residual dishing is **0.149 of the stroke**
> and is a property of the tile's rim, not of the coupling.

`C-0006`, `C-0009` and `C-0015` are annotated in place with a banner pointing here rather than edited, per
`gpd/README.md`.
