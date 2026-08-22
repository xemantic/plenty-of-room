# T-263 — re-grade the coupled honeycomb cells on the honeycomb GRILLAGE, not on a smeared sheet

**Leaf** `A8.2`.
**Verification type** in-silico (a beam-and-bond lattice solve plus a Monte Carlo dropout ensemble)
**+ logical** (an exact algebraic identity between the surrogate and the assembled solve, and a
crossover-column census that costs no solve).

---

## 1. Formulate

### The premise, stated as an integer

`C-0154` (`T-253`) built `HoneycombGrillage` and measured two things that between them invalidate
every coupled cell in this corpus:

- `OrigamiSheet.acrossHelixRigidity = layers × k_θ d / p` — the formula every four-layer claim
  here consumes through `equivalentSheet` — is **`24/7 = 3.42857×` overstated** on a honeycomb
  block, because only half the in-plane adjacent pairs are bonded and an interlayer bond carries
  half the lever arm. **The same function reproduces `D_∥` at `2.8e−15`**, which is what makes
  this a finding and not a bug.
- One layer of such a block is **not a sheet** but **5** dimer components on `10 × 6`, so the
  across-helix load path necessarily traverses the thickness — a body a smeared single-layer
  square-lattice `OrigamiGrillage` does not contain at all.

The consequence is already measured on the **free** tile: `C-0154` reads **0.0449400126** of the
stroke at `10 × 6` where `C-0141` reports **0.0240648102** — **1.868×**, and still inside `T-5b`.
**What is open is the COUPLED margin**, and `C-0146`/`C-0151`'s recommendation rests on it:
`C-0151`'s headline is *"2 flat cells of 8"*, `0.0773373597` at `f = 0.30` and `0.0821458169` at
`f = 0.26`, both graded on `OrigamiGrillage` over an `equivalentSheet` whose `D_⊥` is now known to
be 3.43× too large.

### Locked units and conventions

Lengths **nm**, forces **pN**, stiffness **pN/nm**, pressure **pN/nm²** (= 1 MPa), angles **rad**
internally and degrees only in prose. `k_BT = 4.142 pN·nm` at **T = 300 K**, aqueous **2 mM
MgCl₂**. Dishing is reported **dimensionless, as a fraction of the free stroke**, which is the
convention `T-5b`'s 0.10 is written in.

`s` runs **along** the helices, `y` **across** them in the plane of the face, `z` through the
block's thickness; the origin is the face centre; `W` is positive **downward**, toward the
electrode (`C-0006`). The block is `10 × 6` — ten corrugated x-raster rows of six helices,
`C-0141`'s object — with the gap-facing **face** carrying one helix per raster row.

Geometry is **consumed, never re-derived**: `d = 2.536 nm` (SAXS), in-plane row pitch `3d/2 =
3.804 nm`, layer pitch `d√3/2 = 2.19624042 nm`, rise `0.34 nm/bp`, crossover planes every **7 bp**
with one pair per class every **21 bp**.

### The numeric targets

| | target |
|---|---|
| **N1** | the ported surrogate reproduces `C-0154`'s free `10 × 6` peak dishing **0.0449400126** at 112 bp and enhancement 21.1851817, relative departure **≤ 1e−8** |
| **N2** | the free stroke on the honeycomb lattice equals `p/k_f` **exactly** (`≤ 1e−12` relative), so the normalising stroke is **identical** to `C-0151`'s and the re-grade moves only the structural model |
| **N3** | every one of `C-0151`'s recommended cells — `102 / 109 bp`, `10 × 6`, path counts **10 / 20 / 30 / 50**, distributions **equal springs** and **rim-graded 5:1**, placements **abstract grid** and **determined station lattice** — is re-graded at **both** ends of `C-0116`'s measured band, `f = 0.26` and `f = 0.30`, with the uncoupled reference beside it |
| **N4** | the surrogate at full presence reproduces an **assembled** solve of the same coupling to `≤ 1e−9` relative — an exact algebraic identity, not a tolerance |
| **N5** | the honeycomb lattice's own per-interface crossover column count is emitted beside the **10** the smeared model was swept over |

### Acceptance predicates (falsifiable)

- **`P1`** the free-tile reproduction of `C-0154` closes at `≤ 1e−8` **relative**, at 112 bp and at
  both enhancements `C-0154` itself carries. *Falsified by any larger departure*, which would mean
  the ported load path is not the one `C-0154` measured.
- **`P2`** all `2 × 2 × 4 × 2 = 32` recommended cells are graded at both `f`, and the count that
  clears `T-5b`'s 0.10 at the 90th percentile is reported **beside** `C-0151`'s own count on the
  same cells. *Falsified by any cell refused for a reason the study cannot name.*
- **`P3`** the standing falsifier — a uniform pressure on a uniform Winkler foundation dishes
  **exactly zero** — holds on the **coupled** lattice, and the tributary strips are one row pitch
  **centred on each beam's own axis** (`C-0154`: a corrugated face breaks the falsifier otherwise).
- **`P4`** the comparison against `C-0151` is **paired**: one common dropout stream restricted per
  cell, read **per realisation**, never as a ratio of two order statistics.
- **`P5`** two convergence axes are emitted — beam subdivisions **1 / 2** and the dishing sample
  grid **41 / 81 / 161** — and the departures are quoted at **two** significant digits.

### What would falsify the approach

| | statement | if it fires |
|---|---|---|
| **`F1`** | a uniform pressure on the coupled honeycomb lattice produces zero face dishing | the tributary or the foundation is wrong; **stop** |
| **`F2`** | the free-tile reproduction of `C-0154` closes | the port is not the object `C-0154` measured; **stop** |
| **`F3`** | the surrogate at full presence equals the assembled solve | superposition is being applied to a nonlinear object; **stop** |
| **`F4`** | Betti holds between a point load and the pressure field — `∫ p w_point dA = w_pressure(s, y)` | the two quadratures disagree, so the dual is not the work conjugate; **stop** |
| **`F5`** | **the honeycomb re-grade changes no flatness verdict** | *declared and EXPECTED TO FIRE* — the free tile alone moves 1.868×, and if no coupled verdict moves, the correction is cosmetic and the task's premise is wrong |
| **`F6`** | the honeycomb lattice's per-interface crossover column count equals the smeared model's | *declared* — the smeared model puts `crossoverColumns` columns on the tile at `p/2`, alternating parity, so each interface should receive half of them; agreement is a cross-check and disagreement is a second overstatement |

**Reciprocity is NOT offered as a gate here.** The point-load dual is written as the exact gradient
of the same `evaluate` the sampling uses, so `M = eᵀK⁻¹e` is symmetric **by construction** and the
residual measures nothing. `F4` is the test that has content, because it compares the point
functional against the **pressure quadrature**, which is a different rule on different points.

---

## 2. Plan

### The cheap bound, before any Monte Carlo

Three integers, and they run before a solver:

1. **The crossover census.** At 116 bp the plane ladder is `0, 7, …, 112` — **17** planes — and a
   bond of class `c` occupies plane `q ≡ c (mod 3)`, so each interface receives **5 or 6** columns
   at the 21 bp pitch. `C-0151` grades at **10** crossover columns on a `CrossoverLayout` of pitch
   `p/2 = 3.57 nm` whose two parities alternate, i.e. **5** per interface. If those agree, the
   whole of the re-grade is the `24/7` and the dimer topology; if they do not, there is a second
   overstatement nobody has counted.
2. **The free stroke.** Under a uniform pressure both models translate rigidly, so both give
   `p/k_f` and the normalising stroke cannot move. The re-grade is therefore a **controlled**
   comparison: same extent, same grid, same stroke, same mandate, same stream.
3. **The direction.** The free tile is already measured at 1.868× worse. A coupling that was
   marginal at `0.0773` on the smeared sheet cannot survive a uniform 1.868× — so the run's job is
   to say **how many** cells survive, not **whether** the number moves.

### Method, and why it is the cheap one

**A port, not a new model.** `influenceSurrogate` in `coupling/NonUniformCoupling.kt` is already
written against a `DishingSolution` interface and is model-agnostic; `latticeInfluenceSurrogate`
and `plateInfluenceSurrogate` are its two existing adapters. The whole port is therefore

- **one function on `HoneycombGrillage`** — the work-conjugate dual of a unit downward point load
  on the face at `(s, y)`, which is the gradient of `evaluate`, plus a defaulted `pointLoads`
  parameter on the existing `solve`; and
- **one adapter**, `honeycombInfluenceSurrogate`, ~15 lines.

Everything downstream — `dropoutEnsemble`, `dropoutDishingSample`, `summariseDropoutDishing`,
`worstSinglePathRemoval`, `pairedRatioSummary`, `honeycombSnappedGrid`, `twoLengthSnappedGrid`,
`measuredDepthIncorporation` — is consumed unmodified. **Nothing `C-0142`, `C-0146` or `C-0151`
published can move**, because no source they run through is edited.

**Cost.** `C-0154` records the lattice at **under half a minute** for a whole cross-section on a
node-major banded solve: `4080`–`7920` unknowns at half-bandwidth **243**, so one factorisation is
`n b²/2 ≈ 2e8` flops and each influence function is one back-substitution. 51 influence solves per
cell geometry, one factorisation per `(cross-section, enhancement, row length)`; the Monte Carlo is
`4 000 × 6561 × pathCount` field evaluations, which is what `C-0151` already paid. Estimated
**five to ten minutes** for 32 cells plus the reproductions and the convergence axes.

The alternative — a full 3-D FE solve per realisation — is `4 000 ×` a factorisation per cell and
is **four orders** dearer for an answer superposition gives exactly.

### The order of work

1. Tests first, named for their gate, and watched fail.
2. The dual and the adapter.
3. The cheap-bound census (no solver).
4. The reproduction of `C-0154` at 112 bp, both enhancements.
5. The 32 recommended cells at `f = 0.26` and `f = 0.30`, plus the model's own **lower** bound
   (enhancement `1.0`, `C-0154` §9's third end) on the abstract grid.
6. The paired comparison against `C-0151`'s committed cells, per realisation.
7. Convergence, falsifiers, JSON, claim.

### What this deliberately does NOT do

- **It carries ONE row length.** `C-0154` §9 already records it: the `102 / 109` raster's 7 bp
  stagger and its 102 bp **interface window** are not modelled, and the lattice is built at the
  **block extent**, 116 bp = 39.44 nm, which is the width `C-0151` grades at. What that costs is
  measured on the smeared model in `C-0151` (one crossover column, 2 flat cells against 3) and is
  **not** re-measured here.
- **It carries no across-helix parallel-axis term.** The layers' membrane action across the helices
  needs an in-plane transverse coordinate `HoneycombGrillage` does not have, so its `D_⊥` is the
  **independent** one and a **lower** bound; the bracket is run at three ends, exactly as `C-0154`
  runs it, and the composite fraction enters as `hingeStiffnessEnhancement`.
- **It does not re-open the placement search.** The stations are `C-0151`'s.
