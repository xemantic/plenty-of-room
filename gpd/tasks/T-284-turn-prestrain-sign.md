# T-284 — What sets the sign of a raster turn's `8.57142857°` departure

**Leaf** `A8.2`.
**Verification type** **logical** (a congruence on caDNAno's own `±5 bp` rule, exhaustible over the
59 turns of the recommended raster and over every free convention, with no solver at all)
**+ in-silico** (the derived assignment graded through the same beam-and-bond lattice, the same
exact Woodbury coupling surrogate and the same measured-incorporation dropout ensemble `C-0180`
used, so the two answers are comparable cell for cell).

---

## 1. Formulate

### The question, stated as a binary

[`C-0180`](../claims/C-0180-tied-honeycomb-coupled-regrade.md) §4 grades the recommended
`10 × 6` block's 64 coupled cells at `CH-0228`'s allowed departure and finds:

| cell | zero prestrain | `+8.57142857°` on every turn | `−8.57142857°` on every turn |
|---|---|---|---|
| abstract grid, `3 × 10` = 30 paths, rim-graded, `f = 0.30` | **0.0995744767** — flat | 0.10014682 — not flat | **0.0993228684** — flat |
| abstract grid on the rooting helices, `5 × 10` = 50 paths, rim-graded, `f = 0.30` | **0.0998791032** — flat | **0.099361573** — flat | 0.10109276 — not flat |

**At each sign exactly one of the two recovered cells is flat, and it is a different one.**
[`C-0175`](../claims/C-0175-drawable-raster-rim.md) §8 sweeps three sign assignments on the **free**
tile — `0.0460995878`, `0.0457993778`, `0.0457993778` — a 0.7 % spread that decides nothing there,
and calls the sign *"fixed by no source in this repository"*.

So the whole coupled recovery the tied lattice buys rests on a binary nobody has derived.

### The premise nobody has read as a congruence

`C-0152` §5 fixes the magnitude and says where it comes from:

> caDNAno's `±5` is an integer approximation to **5.25**, so an ALLOWED scaffold crossover already
> carries `8.57142857°` — 0.25 bp off the exact half turn, **on either side**.

*On either side.* A scaffold crossover placed `+5 bp` from its pair's staple position sits **0.25 bp
short** of the exact downstream half turn; one placed `−5 bp` sits **0.25 bp past** the exact
upstream one. The two carry the **same magnitude and opposite signs**, and which one a given turn
takes is not a convention: it is `C-0148`'s closure condition, which this repository already models
in [`tile/HoneycombBondClassResidues.kt`](../../src/main/kotlin/tile/HoneycombBondClassResidues.kt)
as `HoneycombRasterResidues.reducedResidues` — `(level − 7·class) mod 21` at every raster crossover,
required to equal `b₀ ± 5` for **one** lattice constant `b₀`.

**The sign of a turn's departure is the sign of its own `±5`.** Nothing in this repository has read
it that way, and it costs no solve.

### What is open, and what is not

**Open**: (a) whether the closure condition **determines** the assignment on the recommended
`102 / 109` raster, and if so what it is; (b) how much of the freedom survives; (c) whether
`C-0180`'s two recovered cells are flat at the **derived** assignment, at whatever freedom is left.

**Not open**: the magnitude, which is `C-0152`'s; the tie set, its census and its axial stations,
which are `C-0175`'s; the lattice, the stations, the distributions, the dropout stream and the
tolerance, which are `C-0167`'s and `C-0180`'s and are consumed unmodified.

### Locked units and conventions

Lengths **nm**, forces **pN**, stiffness **pN/nm**, pressure **pN/nm²** (= 1 MPa), angles **rad**
internally and degrees only in prose. `k_BT = 4.142 pN·nm` at **T = 300 K**, aqueous **2 mM MgCl₂**.
Dishing is reported **dimensionless, as a fraction of the free stroke**, which is the convention
`T-5b`'s 0.10 is written in.

`s` runs **along** the helices, `y` **across** them in the plane of the face, `z` through the
block's thickness; the origin is the face centre; `W` is positive **downward**, toward the electrode
(`C-0006`). A point load carries `force` positive downward.

**Lattice conventions, fixed before deriving and restated:**

- Axial positions are **integer base pairs on one global `z`**, `C-0140`'s convention, datum at the
  first raster crossover. Residues are `mod 21`, non-negative.
- Viewed from `+z` the backbone azimuth **increases** counter-clockwise with `z`, so one base pair
  is `+240/7 = 34.2857143°` and one class step (`+7 bp`) is `+240°`. This is
  `HoneycombRasterTurnSense`'s own convention, unchanged.
- A crossover displaced `δ` base pairs from the position its own backbones face at rotates **both**
  backbones by `δ · 240/7°` **in the same sense** —
  [`tile/ForcedCrossoverPrice.kt`](../../src/main/kotlin/tile/ForcedCrossoverPrice.kt) states it
  verbatim, and it is the whole reason a departure has a sign at all.
- The block is `10 × 6` at `C-0151`'s `102 / 109` raster, 116 bp extent, `firstAxialSign = +1`,
  `mirrored = false`, `axialReversed = false` — `C-0180`'s Conditions unchanged.
- The tie prestrain is the model's `Φ_upper − Φ_lower` at the tie, `upper`/`lower` being the larger
  and smaller **beam index** `rasterRow · n + column`. That is a labelling, not a geometry, and §3
  says what follows from it.

---

## 2. Plan

### The cheap bound is the whole of deliverable (a), and it runs before any code

`HoneycombRasterResidues(10, 6, 102, 109)` already computes `reducedResidues`. Three integers
settle the question:

1. **How many distinct reduced residues does the raster carry?** Closure permits at most two, `10`
   apart. Two distinct values pin `b₀` **uniquely**, because `+10` and `−10 ≡ 11` are different
   residues mod 21 — so `{r, r+10}` admits `b₀ = r + 5` and nothing else.
2. **Which turns take which?** One pass over the 59.
3. **Is the pattern a theorem or a coincidence?** A helix's length is the difference of its two
   crossover levels, so `C-0136`'s residue `(L − 7Δ) mod 21 ∈ {0, 10, 11}` says it directly:
   residue `0` carries the sign **through** the helix, `10` and `11` **flip** it.

If (1) returns two residues then the `2^59` sign assignments collapse to at most **two** — the
global phase — and deliverable (a) is discharged for the price of one modular reduction.

### What the derivation cannot fix, and why that has to be said rather than assumed

The map from a *displacement* `δ = ±5 bp` to the model's *relative-roll* prestrain needs a
geometric sense for `Φ_upper − Φ_lower`. Because the displacement rotates **both** backbones the
same way, the departure is **common-mode** in that coordinate, and no lattice fact orients it. The
honest deliverable is therefore: the **relative** signs among the 59 are derived, the **one global
phase** is not, and both phases are graded. If the verdict is the same at both, deliverable (b)
follows from deliverable (a) and the margin is quoted at the worse phase.

### The grade, and why it is a re-run and not a model

The prestrain is a **load** (`C-0104`), so a per-tie assignment changes no entry of the stiffness
matrix. Consequences, all of them cost savings that are also gates:

- the influence bank is taken on `withoutPrestrain`, which is the **same object** for every
  assignment, so **one** bank per composite fraction serves both phases and the zero-prestrain
  control (`C-0104`'s trap, made structural);
- the free field is **linear** in the assignment vector, so `field(a) + field(−a) = 2·field(0)`
  exactly — a falsifier that costs one extra solve and needs no analytic answer;
- `honeycombScaffoldTurnTies` already returns the tie list; a per-tie prestrain is
  `tie.copy(prestrainRadians = …)`, which is what `RasterTurnPrestrainStudy` does. **No shared
  source is edited.**

Cost: 2 composite fractions × (1 zero-prestrain lattice + 2 prestrained lattices) factorisations,
16 influence banks, and 64 cells × 2 phases = **128 gradings** at 4 000 realisations on `C-0167`'s
common stream — the same size as `C-0180`'s own deliverable 5, which ran.

### The standing falsifier does NOT transfer, and the replacement is stated

*"A uniform load on a uniform Winkler foundation produces no dishing at all"* is asserted on the
**pressure**, at zero prestrain, and it holds. It must **not** be asserted on a prestrained lattice:
a uniform prestrain is an **eigenstrain** that relaxes into a cylinder of curvature `θ₀/d`, and
`CLAUDE.md` records that asserting the zero there reports a correct solver as broken. The derived
assignment is not uniform, so the cylinder is not the answer either — which is exactly why the
linearity identity above is the gate, and not a zero.

### What would falsify this approach

- **The raster carries three distinct reduced residues**, i.e. it does not close — then no `b₀`
  exists, no assignment is determined, and the honest answer is that the question has no answer at
  `102 / 109` either. (`112 / 108` is run as the control precisely because it is that case.)
- **The raster carries one distinct residue** — then the assignment is uniform, `C-0180`'s two
  readings were the right two, and the derivation buys nothing but a name for them.
- **The derived assignment does not reproduce `C-0175` §8's own sweep.** If the derived assignment
  is one of the three that claim measured, the free-tile peak dishing must reproduce that claim's
  committed number; a disagreement means the tie machinery here is not the tie machinery there.
- **The partition moves with a free convention.** If flipping `firstAxialSign` or `mirrored` changes
  *which turns* take which sign, once each convention's own datum handedness travels with it, then
  the "derivation" is a convention and must be reported as one.

---

## 3. Acceptance predicates

| # | predicate |
|---|---|
| `F1` | the 59 raster crossovers of `10 × 6` at `102 / 109` take **exactly two** distinct reduced residues, `10` apart, so `b₀` has **exactly one** candidate |
| `F2` | the derived per-turn displacement is **not constant** — `C-0180`'s uniform sweep is not the lattice's assignment |
| `F3` | the partition of the 59 turns into the two displacements is the **same** at `firstAxialSign = ±1` and at `mirrored = false/true`, when read against the axial **rim** the turn sits at |
| `F4` | the derived assignment reproduces `C-0175` §8's free-tile reading at `f = 0.30` to `1e−8` |
| `F5` | the free field is **linear** in the assignment: `peak\|field(a) + field(−a) − 2·field(0)\|` is zero to `1e−9` of the stroke |
| `F6` | a uniform pressure on the tied, zero-prestrain lattice dishes exactly `0.0` |
| `F7` | **declared OPEN** — the two cells `C-0180` recovers are flat at **both** global phases of the derived assignment |
| `F8` | **declared OPEN** — the flat census over all 64 cells is the **same** at both global phases |
| `F9` | `112 / 108` returns **no** determined assignment, so the drawable raster is the only one of the two on which this question has an answer at all |
| `F10` | the shared influence bank — one bank per composite fraction, on the zero-prestrain lattice — equals a surrogate built the long way, to `1e−9` |
| `F11` | **added after the fact, and the reason is stated**: a raster carrying **one** reduced residue leaves **two** `b₀` candidates, and the class must refuse rather than guess. It is not in the list above because the corpus owns no such raster — a mutation relaxing that very check failed **nothing**, which said the state had to be **constructed**. `112 / 119` on `10 × 6` is one |

`F7` and `F8` are declared open before the run: either outcome is the finding, and which one it is
decides whether this task delivers (b) as well as (a). They fired, so it does not, and §5 of
[`C-0187`](../claims/C-0187-the-turn-prestrain-sign-is-derived.md) says so and bounds the exposure
instead.
