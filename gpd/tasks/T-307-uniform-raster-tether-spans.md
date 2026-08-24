# T-307 — **Route B's own UNIFORM raster has no determined span: grade the three widths WITH their tethers, over the lattice phase**

| | |
|---|---|
| **Leaf** | **`A8.2`** |
| **Raised by** | [`C-0204`](../claims/C-0204-the-anchor-azimuth-is-determined.md) (`T-304`) §6, whose `F7` was declared open and **FIRED** |
| **Verification type** | **logical** (exact integer residue arithmetic on this repository's own honeycomb crossover lattice, reproduced against `T-304`'s committed `uniformRasters`) **+ in-silico** (the same honeycomb grillage and the same `T-299` tether element, at a **per-turn** span) |
| **Units** | nm, pN, pN/nm, pN·nm. `k_BT = 4.141947 pN·nm` at **T = 300 K**, aqueous **2 mM MgCl₂** |

---

## Formulate

### What is being asked, and why it is not already answered

[`C-0204`](../claims/C-0204-the-anchor-azimuth-is-determined.md) established that route B's tether span is
**not a bracket**: on the drawable `102 / 109` raster every one of the 59 raster turns anchors at an *allowed*
scaffold crossover, so the span is **`0.787091706 nm`** at all 59, and the free tile is then flat at
**`16 of 16`** surviving corners.

**That answer is bought by drawability.** Route B does not need caDNAno's residue condition at all — an
unpaired base has no azimuth (`C-0193` §4) — and at the built `28 nt` allowance route B's own **uniform**
paired rows are `92 / 98 / 106 bp` (`31.28 / 33.32 / 36.04 nm`; `C-0201` §7), **none** of which closes.
So `b₀` is a free **design variable** there and the 59 turns take a **distribution** of spans:
over all 21 phases of all three, `2` to `5` distinct spans whose worst member runs
**`3.93454333–4.35327572 nm`** — reaching `C-0201`'s worst corner exactly at some phases.

`C-0201` §7 grades those three widths **untied** (`0.0425678289 / 0.0422200543 / 0.0451172785`, all flat),
so **nobody has priced a uniform route-B tile carrying the tethers its own geometry implies.**

### The numeric target

For each uniform paired row `L ∈ {92, 98, 106} bp`, each lattice phase `b₀ ∈ [0, 21)` and each chain
corner, the **free-tile** peak dishing over the stroke, with all 59 tethers present at **their own
per-turn spans** and with the chain's preload as a load.

### The acceptance predicate

1. The per-turn span census reproduces `T-304`'s committed `uniformRasters` record — `closes`,
   `distinctSpanCount`, `minimumSpan`, `maximumSpan`, `meanSpan`, `turnsInsideTheAlignedHalf` — at
   **all 63** `(scaffold, phase)` rows, to `1e−9`.
2. The **untied** re-grade at each of the three row lengths reproduces `C-0201` §7's own
   `0.0425678289 / 0.0422200543 / 0.0451172785` to `1e−8`.
3. An **empty** tether list is bit-identical to the untethered lattice on `assembleLoad`, at each of the
   three row lengths.
4. A uniform pressure on the free tethered lattice with the preload **off** dishes exactly zero, at each of
   the three row lengths — the standing falsifier, taken here because **`92 mod 7 = 1` and `106 mod 7 = 1`**
   and only `98` is a multiple of the 7 bp crossover-plane pitch.
5. The `C-0104` triangle-inequality ceiling built from the 59 unit-tension influence fields is **not
   exceeded** by any measured tethered dishing.
6. Every cell states whether it is flat against `T-5b`'s `0.10`, and the **best phase per width** is named
   with the criterion it is best on.
7. Convergence is re-taken on the **deciding quantity at the deciding cell**.

### Conventions, fixed before deriving

- **Block** — `10 × 6` honeycomb, `C-0167`'s own geometry: `d = 2.536 nm` (SAXS), row pitch `3d/2`, column
  pitch `d√3/2`, face column `0`, `10` raster rows of `6` helices, `60` beams, **59** raster turns.
- **Row length** — `rowBasePairs = L`, the *paired* row. `edgeX = L × 0.34 nm`, `edgeY` unchanged at
  `10 × rowPitch`; the interior pressure is `Gen1Tile.TARGET_FORCE / (edgeX·edgeY)` and the collar is
  `C-0022`'s solved profile re-laid on that `edgeX` — **exactly** `C-0201` §7's construction, so the untied
  reproduction is a control on the geometry as well as on the code.
- **Rise** `0.34 nm/bp`; **phosphate radius** `r_P = 0.908637858 nm` (`T-71`, measured).
- **Azimuth** in degrees from the line of centres pointing at the bonded neighbour, folded into
  `(−180°, +180°]`; `AZIMUTH_PER_BASE_PAIR = +240/7°` **per base pair of increasing `z`**, reversed with the
  datum. Standard datum reading: `firstAxialSign = +1`, `mirrored = false`, `axialReversed = false`
  (`C-0204` `F4` shows the span invariant over all eight).
- **Anchor offset** `0` — the loop sits **outboard** of the duplex, `C-0200`'s reading of the built block
  and `C-0201` §7's own width arithmetic.
- **Loop length** — the **built 28 nt** allowance, which is *what fixes* `92 / 98 / 106` (`perHelix − 28`).
  Its two readings are swept: `28 / 28` (`C-0193`) and `C-0200`'s ordered `24 / 32` split in **both** rim
  assignments, since which rim takes the short half is a free convention of that reading.
- **Chain** — freely jointed, zero-force ssDNA Kuhn `b ∈ {2.10, 2.84} nm` with the **inextensible** contour
  `c ∈ {0.65, 0.70} nm/nt` that travels with it (`T-230`). `f = (k_BT/b)·L⁻¹(x/L_c)`, always **positive**:
  a chain at any `x > 0` pulls.
- **Sign of the preload** — the chain's pull enters as `+f·unitZ` times the link gradient (`T-299`), so the
  **nine in-plane** turns contribute exactly zero and the **fifty** through-thickness ones carry it. That
  zero is a property of the **model** (no in-plane transverse coordinate) and not of the chain.
- **Dishing** — `peakDishing(81) / freeStroke`, `freeStroke` the mean deflection under the uniform interior
  pressure on the **same** lattice; tolerance `T-5b = 0.10`. `C-0167`'s convention throughout.
- **Composite fraction** `f = 0.30` (Kauert et al.'s measured band), `k_link = 1e4 pN/nm`,
  `subdivisions = 1` at every headline cell.

---

## Plan

### Cheap bound 1 — arithmetic, no solve at all

The span distribution per `(width, phase)` is already emitted in
[`gpd/results/T-304-raster-turn-anchor-azimuth.json`](../results/T-304-raster-turn-anchor-azimuth.json)'s
`uniformRasters`, and it is re-derived here rather than read. From it, `f(x)` is one inverse Langevin per
turn — so the **tension distribution** and its ratio to `C-0204`'s determined-span tension
(`0.175872271–0.479548487 pN`) are available before anything is compiled. `C-0204` measures the preload's
own worth on the free tile at the determined span as `0.00708426936–0.0195297045` of the stroke; a tension
`k×` larger predicts a preload contribution of order `k×` that, on top of `C-0201` §7's untied
`0.042–0.045`, is what decides the verdict. **This runs first and it is quoted whether or not it agrees.**

### Cheap bound 2 — the influence bank, `60` solves per width and no re-solve after

A tether's tension is a **load** in `C-0104`'s exact sense: it changes no entry of the stiffness matrix and
the field is exactly linear in it. So on the **untied** lattice one factorisation plus `59` unit-tension
solves plus one pressure solve gives, for **every** `(phase, corner)` with no further solve:

- a **rigorous** triangle-inequality ceiling `D_pressure + Σ_k |f_k|·D_k` on the peak dishing, and
- an exact **linear-superposition estimate** of the field, which differs from the answer only by the
  tether's own **stiffness** — the term `C-0201` §1 measures at `0.95 %` of the free tile.

`3 × 60 = 180` solves on `3` factorisations.

### The expensive half, and why it is affordable

The exact reading needs the tether **stiffness** in the matrix, so one factorisation per
`(width, phase, corner)`: `3 × 21 × 12 = 756`. At `L = 92 bp` the lattice is `15` nodes per beam,
`3 600` degrees of freedom and a half-bandwidth of `243`, so a banded Cholesky is `n·b²/2 ≈ 1.1e8` flops —
sub-second. The bank's estimate is then **verified against** the exact solve rather than substituted for
it, and their departure **measures** what the stiffness is worth.

### What would falsify this approach

- If the uniform-load falsifier fires at `92` or `106 bp`, the lattice does not represent those rows and
  **no number below is about them** — that is checked first, before anything else is graded.
- If the untied re-grade does not reproduce `C-0201` §7, the geometry is not the one that claim graded and
  the comparison is not controlled.
- If the triangle-inequality ceiling is exceeded, the preload is not entering as a load and `C-0104`'s
  linearity — on which the whole cheap bound rests — does not hold here.

### Declared falsifiers, before the run

| # | falsifier |
|---|---|
| `F1` | a uniform pressure on the free tethered lattice, preload **off**, dishes more than `1e−9` of the stroke at any of `92 / 98 / 106 bp` |
| `F2` | the untied re-grade fails to reproduce `C-0201` §7's `0.0425678289 / 0.0422200543 / 0.0451172785` to `1e−8` |
| `F3` | the per-turn span census fails to reproduce `T-304`'s committed `uniformRasters` at any of the 63 rows to `1e−9` |
| `F4` | an empty tether list is not bit-identical to the untethered lattice on `assembleLoad` at some row length |
| `F5` *(open)* | **no** lattice phase of **any** of the three uniform widths leaves the free tile inside `T-5b`'s `0.10` at **every** corner |
| `F6` *(open)* | the phase that minimises the free-tile dishing is **not** the phase `C-0204` names best on `turnsInsideTheAlignedHalf` (phase `7` at all three widths) |
| `F7` | the `C-0104` triangle-inequality ceiling is exceeded by a measured tethered dishing at some cell |
| `F8` *(open)* | the flatness verdict at the deciding cell moves under beam subdivision `1 → 2` |
| `F9` | the reach bound refuses at some corner — a span at or above the chain's own contour |

`F5`, `F6` and `F8` are declared **open**: their firing is a finding, not a defect.

### Explicitly out of scope, and named

- **The coupled 64 cells at these widths.** `C-0167`'s placements, station lattices and distributions are
  derived at the `116 bp` block extent; re-deriving them at `92 / 98 / 106 bp` is a placement search, not a
  re-grade. `C-0201` and `C-0204` both read `0 of 64` coupled at every tethered state on the recommended
  raster, so the coupled question is named and not answered here.
- **Choosing the row length for the span.** The three widths are what the three scaffolds afford at the
  built allowance; a design that traded paired length against span is a different task.
- **The built block's own `b₀`**, which needs a register read of the deposited `10 × 6` file.
