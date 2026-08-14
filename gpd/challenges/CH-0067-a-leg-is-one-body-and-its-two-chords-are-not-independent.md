# CH-0067 — A leg is ONE BODY with TWO junctions, so its base chord and its cap chord are not independently choosable: they differ by `m × 33.74°`, and at `C-0048`'s own recommended leg length that difference is 78.5° — the azimuth pair its recommended design is written on does not exist at the length it is written at

| | |
|---|---|
| **Against** | [`C-0048`](../claims/C-0048-truss-cap.md) — the recommended design's **azimuth pair** (*"each base … chord **along** the flexure axis"* together with *"each cap junction … the same two-terminus chord, laid **ACROSS** the flexure axis"*) at its recommended **7.00 nm** leg; and [`C-0042`](../claims/C-0042-paired-perpendicular-junction.md) and [`C-0037`](../claims/C-0037-triangulated-standoff.md) insofar as they treat the leg **length** as a continuum |
| **Raised by** | [`C-0052`](../claims/C-0052-crossbar-junction-trio.md), task [`T-117`](../tasks/T-117-crossbar-junction-trio.md) |
| **Grounds** | **a kinematic constraint omitted** — two junctions on one rigid body, whose relative orientation is fixed by the body |
| **Severity** | **the design survives everywhere, and at `C-0048`'s own leg length the azimuth pair the constraint forces it onto is BETTER than the one it recommends.** All nine predicates PASS at every integer leg length in the 12–26 step envelope. What fails is the *availability* of the recommended pair, the *reason* given for it, and the treatment of the leg length as a free continuum |

---

## What is claimed upstream

`C-0048`'s recommended design reads

> | **each base** | `C-0029`'s two-terminus junction, chord **along** the flexure axis: 78.24 restrained / 13.53 free, 64.71 axial |
> | **each cap junction** | the same two-terminus chord, laid **ACROSS** the flexure axis — the opposite azimuth to the base's: **13.53** loaded / **78.24** free |
> | **legs** | two duplexes standing normal to the sheet, **7.00 nm = 21 bp** |

and its whole azimuth finding is that *"the cap junction's azimuth is a third conserved budget, and it has no free corner"* — the two planes are in opposition and the design *chooses* the free plane.

**The two azimuths are treated as two independent design variables.** They are not.

## Why they are one variable and a length

`C-0029`'s counting theorem fixes a junction's chord to the **terminal base pair** of the duplex end that carries it: two strand termini, on the backbone radius, at the terminal base pair's own azimuth. `C-0042`'s gate-3 identity fixes the chord direction as `ψ₀ + Δ/2 + π/2` — *a function of the body's own azimuth alone*.

A truss leg has **two** ends and **both** carry junctions. Its terminal base pairs are `m` base-pair steps apart, so their azimuths differ by `m × twist`, and therefore

&nbsp;&nbsp;&nbsp;&nbsp;**`chord_cap − chord_base = m × 33.74°`, folded modulo `π` because a chord is a line.**

Rotating the leg about its own axis moves **both** chords together. So the design may choose *where* to spend the mismatch, and it may choose `m`; it may not choose the two azimuths independently.

**This does not contradict [`CH-0056`](CH-0056-the-azimuthal-quantum-belongs-to-the-sheet.md); it completes it.** That challenge established that the quantum belongs to the **sheet** and not to a free standoff's chord, because a free duplex with **one** junction inherits no phase. A free duplex with **two** junctions inherits no phase either — its *absolute* azimuths are still continuous, and `C-0042`'s 0.00° base alignment stands untouched. What is quantised is the **relation between its two ends**, which is a different quantity on the same body, and it is the quantity a truss leg has and a lone standoff does not.

## What the constraint is worth

At the square lattice's 10.67 bp/turn, over the 12–26 step envelope `C-0048` sweeps:

| leg, steps | 13 | 16 | 19 | **21** | 24 |
|---|---|---|---|---|---|
| leg length [nm] | 4.42 | 5.44 | 6.46 | **7.14** | 8.16 |
| relative chord [deg] | 78.61 | 179.83 | 101.05 | **168.53** | 89.75 |
| **budget** — distance from the recommended 90° [deg] | **11.39** | 89.83 | 11.05 | **78.53** | **0.25** |
| `cos²` of the budget | 0.961 | 0.000 | 0.963 | **0.040** | 1.000 |

**`C-0048`'s own 7.00 nm leg rounds to 21 steps, whose budget is 78.53°** — and 78.53° from the recommended pair is, to a good approximation, *the other corner*: the corner `C-0048` itself evaluates and reports at a critical load of **6.20 pN** and margins **1.48 / 1.12** against the recommended **1.95 / 1.46**.

**And the constraint does not depend on the twist convention.** A free-standing leg is not lattice-constrained, so its twist is arguably the natural 10.5 bp/turn rather than the square lattice's 10.67. At 10.5 the 21-step budget is **90.0°** — worse, not better — and at 10.4 it is 83.1°. **21 steps is the worst available choice on all three readings**, and only **13 steps** delivers the recommended pair on all three (11.4° / 4.3° / 0.0°).

## What is NOT challenged

- **`C-0029`'s counting theorem.** This is a consequence of it, applied at both ends of one body rather than one.
- **`C-0042`'s base placement.** Its 0.00° base chord is a statement about one junction and is untouched; the leg's *absolute* azimuth is still free.
- **`C-0048`'s conserved chord budget.** `loaded + free = 91.76 pN·nm/rad` is exact and is reproduced here at seven azimuths.
- **Any published number.** `C-0052` reproduces sixteen of `C-0048`'s and `C-0042`'s to ≤ 3.4e−9 through an independently written assembly, including the whole `Sy7` design row.
- **The verdict.** Every quantised leg length in the envelope passes all nine predicates.

## And one thing the correction improves

The pair the constraint forces the design onto at 21 steps is **not worse — it is better**, and that is a second, independent finding about `C-0048`'s recommendation. `C-0048` picks the cap azimuth *"because that is where `P6` binds"* — it maximises the **free** plane's critical load. But at its own design point the **loaded** plane governs (8.947 against 9.236 pN), so moving couple *out of* the free plane and *into* the loaded one raises `min(loaded, free)`. The optimum is the **balance**, not the corner: at 21 steps the best split of the 78.53° budget is base **19°** / cap **60°**, and it delivers a higher margin than the recommended corner does.

**So the recommended azimuth pair was chosen on the plane that does not govern, and the constraint that removes it is a favour.**

## How this challenge would itself be defeated

1. **A leg that is not one rigid duplex** — two coaxial duplexes joined by a nick or a short flexible link, whose relative twist is free. `C-0029` rules out the nick (*"a double nick IS a crossover"*, and a single nick carries the duplex's own `B/a` and `S/a`); a flexible link would be a third junction and a new compliance in the leg's own axis, which nothing here prices.
2. **A leg whose two ends do not both need aligned chords** — for instance a base junction that has been given up on, which `C-0029` shows fails `P6` everywhere.
3. **A measurement of the free duplex's twist** that puts `m = 21` near 90° rather than near 0. That is a measurement of `bp/turn` to better than 0.5 %, and the three readings in circulation (10.4, 10.5, 10.67) bracket 21 steps at 83–90°, all of them on the wrong side.
