# T-279 — `C-0167`'s 64 coupled cells re-graded on the TIED honeycomb lattice

**Leaf** `A8.2`.
**Verification type** in-silico (the same three-dimensional beam-and-bond lattice solve, the same
exact Woodbury coupling surrogate, and the same measured-incorporation dropout ensemble, with 59
covalent elements added to the lattice)
**+ logical** (an exact bit-identity between the empty-tie lattice and the object `C-0167`
measured, and a cheap bound taken out of `C-0167`'s own committed result file with no solve at
all).

---

## 1. Formulate

### The premise, stated as an integer

[`CH-0227`](../challenges/CH-0227-the-honeycomb-lattice-omits-the-rasters-own-turn-ties.md)
([`C-0175`](../claims/C-0175-drawable-raster-rim.md), `T-254`) established that
[`tile/HoneycombGrillage.kt`](../../src/main/kotlin/tile/HoneycombGrillage.kt)'s bond list is the
**staple** crossover lattice — the 7 bp plane ladder, `435` bonds on the recommended `10 × 6`
block — and that a Rothemund-style raster also **turns**, `H − 1 = 59` times, each turn a scaffold
crossover with zero unpaired nucleotides and therefore a covalent tie between two duplexes at
their **ends**, at `s = ±L/2`, past the last plane of the ladder.

The split is `435 + 59`. It is `C-0099`'s square-lattice `56 = 42 + 14` read on the honeycomb.

Adding them stiffens the **free** block:

| cross-section | coupling | no ties | 59 ties | ratio |
|---|---|---|---|---|
| **`10 × 6`** | `f = 0.30` | **0.0501417316** | **0.0446459684** | **0.890395426** |
| `10 × 6` | `f = 0.26` | 0.0522223659 | 0.0467367262 | 0.894956124 |
| `10 × 6` | none | 0.132443428 | 0.12738041 | 0.961772226 |

So **every uncoupled reference in `C-0154` and `C-0167` is `1.12×` too soft** at the recommended
cross-section, and `C-0167`'s *"`0` of `64`"* coupled cells were graded on that soft object. The
direction is **conservative**, which is the safe one, and that is exactly why nobody re-ran it.

### What is open, and what is NOT open

**Open**: whether any of `C-0167`'s 64 `(f, placement, columns, distribution)` cells clears
`T-5b`'s 0.10 at the 90th percentile of `C-0087`'s measured staple incorporation once the ties are
present, and by how much each cell moves.

**Not open**: the free tile, which `C-0175` has already measured; and the *direction*, which the
Loewner statement `K_tied ⪰ K_untied` fixes for the strain energy under any fixed load. It fixes
nothing about peak dishing, which is a **seminorm** of the field and not monotone in the stiffness
— which is precisely why the re-grade is owed.

### Locked units and conventions

Lengths **nm**, forces **pN**, stiffness **pN/nm**, pressure **pN/nm²** (= 1 MPa), angles **rad**
internally and degrees only in prose. `k_BT = 4.142 pN·nm` at **T = 300 K**, aqueous **2 mM
MgCl₂**. Dishing is reported **dimensionless, as a fraction of the free stroke**, which is the
convention `T-5b`'s 0.10 is written in.

`s` runs **along** the helices, `y` **across** them in the plane of the face, `z` through the
block's thickness; the origin is the face centre; `W` is positive **downward**, toward the
electrode (`C-0006`). A point load carries `force` positive downward, so a coupling's upward
support force enters as its negative.

Geometry is **consumed, never re-derived**, and is `C-0167`'s own: `10 × 6` (60 helices), block
extent **116 bp = 39.44 nm** at `C-0151`'s recommended `102 / 109` raster, `edgeY = 38.04 nm`,
`d = 2.536 nm` (SAXS), in-plane row pitch `3d/2 = 3.804 nm`, layer pitch `d√3/2 = 2.19624042 nm`,
rise `0.34 nm/bp`, crossover planes every **7 bp** with one pair per class every **21 bp**,
`k_θ = 13.5294118 pN·nm/rad`, `k_s = 64.7058824 pN/nm`, link penalty `1e4 pN/nm`. `C-0022`'s
solved collar at 2 mM / 10 nm / 0.192 V; `C-0001`'s secant foundation on the gap-facing face only;
`C-0017`'s mandate at §3's **acceptable** clause, `33.3333 pN/nm` on the SUM; seed `197197`,
**4 000** realisations on **one** common stream restricted per cell, `81 × 81` dishing grid,
`T-5b`'s **0.10**. Composite fractions **0.30** and **0.26** (`C-0116`), entering as
`hingeStiffnessEnhancement` **21.1851817** and **18.4938242**.

The ties enter with `firstAxialSign = +1` and, for the primary deliverable, **zero prestrain** —
they are a pure **stiffness**. The prestrain is a second deliverable and is stated separately,
because a prestrain is a **load** and not a stiffness (`C-0104`).

### The numeric targets

| | target |
|---|---|
| **N1** | the **untied** re-grade reproduces every one of `C-0167`'s 64 committed `p90OverStroke` and `nominalOverStroke` values, relative departure **≤ 1e−8** — same seed, same stream, same grid, same mandate |
| **N2** | the **tied** free tile reproduces `C-0175`'s **0.0446459684** (`f = 0.30`), **0.0467367262** (`f = 0.26`) and **0.12738041** (no enhancement), relative departure **≤ 1e−8** |
| **N3** | all **64** cells are graded on the tied lattice at both `f`, and the count clearing `T-5b`'s 0.10 at the 90th percentile is reported beside `C-0167`'s own **0 of 64** on the same cells |
| **N4** | the per-cell movement is reported as the **median of the per-realisation ratio** on the shared stream, cell by cell — **never** as a scalar multiplier and never as a ratio of two order statistics |
| **N5** | an **empty** tie list gives a lattice, a load vector, a point-load dual, an influence surrogate and a graded cell **bit-identical** to `C-0167`'s object |

### Acceptance predicates (falsifiable)

- **`P1`** the untied half of this study reproduces `C-0167`'s 64 committed cells at `≤ 1e−8`
  relative on both `p90` and `nominal`. *Falsified by any larger departure*, which would mean the
  two halves are not the same object measured twice and no pairing is legitimate.
- **`P2`** all 64 cells are re-graded on the tied lattice at both ends of `C-0116`'s band, and the
  `T-5b` count is reported beside `C-0167`'s. *Falsified by any cell refused for a reason the study
  cannot name.*
- **`P3`** the standing falsifier — a uniform pressure on a uniform Winkler foundation dishes
  **exactly zero** — holds on the **tied coupled** lattice, with the tributary strips one row pitch
  **centred on each beam's own axis** (`C-0154`: a corrugated honeycomb face breaks the falsifier
  otherwise).
- **`P4`** the tied-versus-untied comparison is **paired**: one common dropout stream restricted
  per cell, read **per realisation**, with the fraction of realisations at which the tied lattice
  reads better reported beside the median ratio.
- **`P5`** two convergence axes are emitted — beam subdivisions `1 → 2` and the dishing sample grid
  `41 / 81 / 161` — **at the cells whose verdict moves, on the `p90` itself**, and every
  same-quantity identity is emitted as a **threshold and a boolean**, never as a value.

### Falsifiers, declared before the run

| | statement | expected |
|---|---|---|
| **`F1`** | a uniform pressure on the tied coupled lattice dishes exactly zero (`< 1e−9`) | not to fire |
| **`F2`** | the untied reproduction of `C-0167`'s 64 cells closes at `1e−8` | not to fire |
| **`F3`** | an empty tie list is bit-identical to `C-0167`'s lattice — bond site set, load vector, point-load dual, and the solved field at `1e−10` | not to fire |
| **`F4`** | the tied free tile reproduces `C-0175`'s three readings at `1e−8` | not to fire |
| **`F5`** | **the ties move NO flatness verdict** — **declared open** | unknown; the cheap bound admits at most 8 of 64 |
| **`F6`** | the per-cell movement is a **multiplier**: every cell's median per-realisation ratio lies within `1e−3` of the free tile's `0.890395426` — **declared open** | expected to fire, and its firing is what forbids rescaling a table |
| **`F7`** | the tied lattice reads **worse** than the untied one at some cell, i.e. some median per-realisation ratio exceeds 1 — **declared open** | unknown; a Loewner increase in stiffness does not bound a seminorm |
| **`F8`** | a cell whose `T-5b` verdict the ties MOVE keeps that verdict under its own convergence axes — the beam subdivision and the dishing sample grid, taken on the `p90` itself at the deciding cell — **declared open** | added after the first full run, because the deciding margin came out at 0.43 % of the tolerance and `C-0167`'s own convergence cell is not the cell the verdict rests on |

## 2. Plan

### The cheap bound, and it runs before any solve

`C-0167`'s committed result file already carries the 64 untied `p90` values. Multiply each by the
**free tile's own** ratio `0.890395426` — the most favourable transfer any multiplier hypothesis
could offer — and count how many fall below `T-5b`'s 0.10. That is one pass over a JSON file and
it costs nothing.

**Result: 8 of 64.** The threshold is `p90 < 0.10 / 0.890395426 = 0.112309652`, and the eight
cells that meet it are every `5 × 10` rim-graded cell (four of them) plus the two `3 × 10`
rim-graded abstract-grid cells at both `f`. The tightest untied cell in the corpus, *abstract grid
on the rooting helices, `5 × 10`, rim-graded 5:1, `f = 0.30`*, sits at **0.101931622**, i.e.
**1.9 %** over the tolerance.

So the cheap bound **narrows the question to eight cells and cannot answer it**, for the reason
the queue row states: a coupling changes the load path, and `C-0154`'s own composite fraction
reads **0.2468** on the rigidity against **0.9405** on the dishing — the same lattice change is
worth 3.8× more on one functional than on the other. `C-0167` measured the same thing from the
other side: its own model change gave per-realisation median ratios running **1.064 to 2.475**
across 64 cells with a free-tile ratio of 1.868, and **six** cells where the unpaired reading has
the opposite **sign** from the paired one. A free-tile ratio is not a bound on a coupled cell in
either direction, and the eight are candidates rather than a verdict.

### The method, justified against cost

**This is a re-run and not a model, and that claim is verified rather than inherited.**

`C-0175` states that the tie extension to `HoneycombGrillage` is strictly additive with an empty
default and is asserted bit-identical as a named test. `T-279` does not inherit that: `F3` re-takes
it at the level this study needs — not only the bond count but the **crossover site set**, the
**load vector**, the **point-load dual** and the whole **influence surrogate**, because those are
the objects a coupled cell is built out of and none of them is what the existing test asserts.

`C-0058`'s `influenceSurrogate` is written against the model-agnostic `DishingSolution` interface
and `honeycombInfluenceSurrogate` (`C-0167`) is its honeycomb adapter; `HoneycombGrillage.solve`
and `pointLoadDual` take the ties through the stiffness matrix and nowhere else. So the port is
**one constructor argument**, and the cost is one extra factorisation per `(f, tie state)` plus one
back-substitution per influence function — the same `4 320` unknowns at half-bandwidth `243`
(a tie joins two beams at the **same** node, so it lives inside one node column and cannot widen
the band).

The alternative — a full lattice solve per dropout realisation — is four orders dearer for an
answer superposition gives exactly.

**Second deliverable, at almost no extra cost.** `CH-0228` establishes that **every** allowed
honeycomb scaffold crossover carries `8.57142857°` of azimuthal departure, so all 59 ties are a
**load** as well as a stiffness, and `C-0175` bounds that load on the **free** tile only. Because a
prestrain changes **no entry** of the stiffness matrix, the influence functions of the prestrained
tied lattice **are** the influence functions of the zero-prestrain tied lattice — so the second
deliverable costs one extra free-field solve per lattice and re-uses the same influence bank. Both
uniform sign assignments are graded, because no source in this repository fixes the sign.

### What would falsify the approach

- **`F3` firing.** If an empty tie list is not bit-identical to `C-0167`'s object, then the tie
  extension is not additive, the pairing is between two different models rather than two states of
  one, and every number here is a comparison of noise.
- **`F1` firing.** A corrugated honeycomb face already broke the standing uniform-load falsifier
  once (`CH-0214`); if 59 rim ties break it again, the lattice is applying a load the physics does
  not, and no dishing verdict on it is readable.
- **`F2` firing.** If the untied half does not reproduce `C-0167`, the study is not measuring the
  ties.

### What this cannot establish

- **TRL 1–3.** `k_θ` at a scaffold turn is asserted equal to `k_θ` at a staple crossover because it
  is the same covalent object, not because anything measured it — `CH-0227` §7 says so and this
  task inherits the caveat verbatim.
- The tie's axial station is taken at `s = ±L/2` exactly; a scaffold crossover sits `5 bp` from a
  staple position, so its true station is within **1.7 nm** of the rim node.
- Nothing here re-opens the placement search, the distribution rule, the raster or the
  cross-section. The stations are `C-0151`'s, the distributions `C-0058`'s two.
- The lattice still carries **no** across-helix parallel-axis term, so its `D_⊥` is the
  independent lower bound and the enhancement enters as a smeared multiplier (`C-0167` §8).
