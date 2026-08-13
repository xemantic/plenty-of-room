# CH-0021 — An out-of-plane concentration factor is not an in-plane one, and applying it to a lateral tether is wrong in both directions at once

| | |
|---|---|
| **Against** | [`C-0014`](../claims/C-0014-lateral-confinement.md) — its per-anchor force table, its `L_min = δ√(Sn/(2A))` design rule, and the `T-2` window constraint it derives from them |
| **Raised by** | [`C-0020`](../claims/C-0020-in-plane-shear-lag.md) (`T-15`) |
| **Date** | 2026-08-13 |
| **Grounds** | methodological — a factor transferred between two problems that have different load-path topologies, *and* transferred between two definitions that differ by the number of paths on a contour |
| **Direction** | **mixed, and that is the point.** For the design `C-0014` recommends the correction is favourable by 2.79×; for a design that differs from it only in the *direction the tether runs*, it is unfavourable by 1.24× |
| **Status** | raised. **`C-0014`'s verdict does not move** — a scheme still exists, the anisotropy theorem is untouched, and orientation is still what decides everything. What moves is the number the footprint constraint is written on |

---

## What is challenged

`C-0014` prices every in-plane load path with `C-0009`'s **out-of-plane** concentration factor, and says so in its own validity range:

> **On `C-0009`'s concentration factor.** It is applied at its worst value (7.6×) throughout, and that is
> **conservative and known to be so** […] The correct treatment is a shear-lag problem on a membrane-loaded
> lattice and **nobody has done it**; it is queued as `T-15`.

The factor enters two places:

1. the per-anchor force table — *"4 in-plane tethers, 40 nm: 3.78 pN → × `C-0009`'s 7.6 → **28.7 pN** → FAIL by 2.9× against unzip"*;
2. the design rule **`L_min = δ√(S n /(2A))`** with `n = 7.6`, giving **28.0 nm** at the 3 nm stroke and **93.3 nm** at the 10 nm stroke, and hence the `T-2` window constraint *"lateral confinement and the desired stroke are incompatible at a fixed device footprint"*.

`T-15` has now done the membrane-loaded lattice. Three separate things are wrong with the transfer, and they do not point the same way.

---

## Ground 1 — the two factors are not the same *quantity*

`C-0009` defines its factor as

&nbsp;&nbsp;&nbsp;&nbsp;**peak per-load-path force ÷ the equal share over the load paths on an `ℓ`-contour**,

and the contour carries 9.3 paths, so at its own design point the peak crossover force is 5.63 pN against an
*anchor reaction* of 15.15 pN — a **peak-to-applied ratio of 0.372**, not 3.5.
`C-0014` multiplies a tether *tension* by 7.6, i.e. it uses the number as

&nbsp;&nbsp;&nbsp;&nbsp;peak per-load-path force ÷ the **applied** force.

Those differ by the path count, and out of plane they differ by roughly **20×**.
Nothing in `C-0009` licenses the second reading; the two are named apart in `C-0020` and never mixed.

## Ground 2 — the in-plane problem has no reaction to concentrate

`C-0009`'s factor exceeds one because an out-of-plane anchor is a **reaction**: it gathers the foundation load
from an area of order `ℓ²` around itself, so the force it carries is `8q ℓ_∥ℓ_⊥`, not the force anyone applied,
and the peak is measured against an equal share of *that*.

Laterally there is nothing to gather. `C-0010` establishes — by translation invariance, as a symmetry
statement and not a small number — that a laterally homogeneous grafted layer under a non-adsorbing tile has
**exactly zero** lateral restoring stiffness. So an in-plane tether collects nothing: its own tension is the
whole of the load, and the peak per-path force is a *fraction* of it in every axis-aligned case.

&nbsp;&nbsp;&nbsp;&nbsp;**Aligned with the helices, `η = 1.0000` — the attachment's own duplex carries the whole
tension, at all 480 (phase, duplex) placements and at every crossover stiffness across four decades.**

The effective allowable is then the full **48 pN** single-duplex shear figure, against the **48/7.6 = 6.3 pN**
`C-0014` used. `L_min` at the desired 10 nm stroke falls from **93.3 nm to 33.5 nm**, and the assembly around a
40 nm tile from ~227 nm to ~107 nm.

## Ground 3 — and for a *misaligned* tether the stand-in is not conservative at all

`C-0014` calls its transfer "conservative and known to be so". Over the complete edge-to-edge placement sweep —
all 15 × 15 duplex pairs at all 32 column phases, 7200 designs — it is not.

A tether that does not pull along a duplex applies a **moment** to it, and the crossovers react that moment as
an axial **couple**, because they act on the interface line and not on the duplex axis. At the worst placement
— a rim-to-rim chord at 43° — the peak duplex axial force is **2.33×** the tether tension and the peak
crossover force **2.45×**, giving `A_eff = 4.09 pN` against `C-0014`'s 6.3, and

&nbsp;&nbsp;&nbsp;&nbsp;**`L_min` at the 10 nm stroke of 115.9 nm — 1.24× *worse* than the figure the stand-in produced.**

The overshoot is mesh-converged over nested subdivisions 1 → 8 and **saturates at 2.48** as the crossover
stiffness is swept over four decades, so it is a bracket rather than an unbounded exposure. It is the same
lever `C-0009` found out of plane, in a new plane: a short overhang held by two nearby supports over-reacts.

---

## What this does *not* challenge

- **The anisotropy theorem.** `k_lat/k_norm = secant/tangent ≤ 1` is a convexity result about a link's own
  force-extension law and has nothing to do with how the sheet distributes the load. Untouched.
- **The verdict.** Two schemes still pass; the vertical strut still fails by 40–160× and is still destabilised
  by its own duty; the in-plane tether is still the topology that escapes the theorem.
- **`L_min = δ√(Sn/(2A))` as a *form*.** It is correct, and `C-0020` re-derives its exact (non-linearised)
  counterpart `L = δ/√((1+A/S)² − 1)`, which agrees with it to 1 % at these strokes. Only `n` moves.
- **The cable term, the `r²` yaw cancellation, the over-stiffening result, `S6`/`S7`.** All consumed unchanged.
- **"Orientation decides everything and material almost nothing."** `C-0020` reaches the *same* conclusion by a
  completely different route — here it is the tether's orientation **in the plane of the sheet, relative to the
  helices**, worth a factor of 11.75 in the effective allowable at no cost in material, count or stroke.

## The remedy proposed

Replace `n = 7.6` in `C-0014`'s `L_min` table and per-anchor force table with the in-plane factor, **quoted with
the tether's alignment**, and add the alignment as a build rule beside the existing "present every joint in
shear geometry" one:

| tether direction | `n` | `A_eff` [pN] | `L_min` at 3 nm | `L_min` at 10 nm |
|---|---|---|---|---|
| **aligned with the helices** | **1.00** | **48.0** | **10.0 nm** | **33.5 nm** |
| across the helices | 3.82 | 12.6 | 19.8 nm | 65.9 nm |
| worst of 7200 placements (43° rim-to-rim) | 11.75 | 4.09 | 34.8 nm | 115.9 nm |
| ~~`C-0009`'s out-of-plane 7.6× as applied~~ | ~~7.60~~ | ~~6.32~~ | ~~28.0 nm~~ | ~~93.3 nm~~ |

And carry with it the term `L_min` does not contain: at the minimum length the tether's **normal preload** is
`n_t A √(2A/S)` to leading order — **independent of the stroke** — which is **54.9 pN for four tethers, 55 % of
the §3 100 pN target force**, at both strokes. `C-0014`'s finding that lateral confinement and the desired
stroke are incompatible at a fixed footprint therefore **does not survive in the currency it was written in**,
and is restated in `C-0020` as a preload constraint instead.

`C-0014` is annotated in place with a banner pointing here rather than edited, per `gpd/README.md`.
