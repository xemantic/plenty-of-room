# T-253 — A honeycomb grillage, so a prestrain on a four-layer face can be solved at all

| | |
|---|---|
| **Leaf** | `A8.2` |
| **Raised by** | [`C-0152`](../claims/C-0152-forced-scaffold-crossover-price.md) §7 and §11 — *"the FLATNESS channel cannot be evaluated here at all … a honeycomb prestrain solve does not exist in this repository"* |
| **Claim reserved** | `C-0154`; challenges `CH-0200`, `CH-0201` |
| **Verification type** | **logical** (a graph-theoretic census of what the honeycomb lattice supplies against what `OrigamiGrillage` requires) **+ in-silico** (a new 3-D beam-and-bond lattice, its long-wavelength limits, and a linear prestrain solve) |

## 1. Formulate

### The gap, stated exactly

`CLAUDE.md` records it in two sentences:

- **`OrigamiGrillage` NEVER READS `layers` OR `interlayerCoupling`** — it takes exactly five scalars
  from its `OrigamiSheet`, so a grillage built on a four-layer variant is bit-identical to one built
  on the single-layer one.
- **`CrossoverLayout`'s two-parity alternation makes its crossover combinatorics SQUARE-LATTICE.**

So every placement, phase and plan ceiling in this corpus is a single-layer square-lattice result.
[`C-0141`](../claims/C-0141-honeycomb-station-lattice-and-placement.md) supplied the honeycomb's
**station lattice, plan ceiling and placement family** and explicitly did **not** supply a grillage
(*"the grillage is still single-layer and still square-lattice in its crossover combinatorics"*).

### Numeric target

A four-layer honeycomb block, prestrained at the **10 of 59** scaffold crossovers
`C-0152`/`CH-0188` force on the `112 / 108` raster, each at `C-0152`'s minimal rung
**`17.1428571°`**, graded as peak dishing over `C-0022`'s solved collar against `T-5b`'s **0.10** of
the stroke — or a precise statement of what such a model would require and why the existing one
cannot be adapted.

### Acceptance predicates

- **`P1`** — the cheap bound runs **before** any solver: a side-by-side census of what
  `OrigamiGrillage`'s assembly requires and what an `m × n` honeycomb block supplies, with the
  verdict *adapt* or *replace* derived from it rather than asserted.
- **`P2`** — if a lattice is built, its long-wavelength limits are **measured** against closed forms
  in both directions and in twist, and the interlayer coupling fraction `f` is an **output** of the
  lattice rather than an input.
- **`P3`** — the prestrain answer is delivered as a **rigorous ceiling over every admissible choice**
  of the ten forced sites, so that it does not depend on reconstructing the raster path.
- **`P4`** — a convergence axis is declared and reported; a reproduction of at least one standing
  published number is asserted.

### Locked units, geometry and sign conventions

Lengths **nm**, forces **pN**, energies **pN·nm** and `k_BT` = 4.141947 pN·nm at 300 K, aqueous
2 mM MgCl₂; rigidities **pN·nm**; angles **rad** internally, degrees only where a source quotes them.

Cross-section coordinates are `C-0141`'s, unchanged: a block is `m` corrugated x-raster rows of `n`
helices, site `(r, c)`, `position(r, c) = (c·d√3/2, r·3d/2 + d/2·[r+c even])`. The **helix axis** is
the third direction and carries the row's base-pair span. The plate's **in-plane across-helix**
coordinate is `y` (the `m` direction, pitch `3d/2`); the plate's **normal** is the `c` direction
(pitch `d√3/2`), and the gap-facing face is `c = 0`. Deflection `W` is positive **downward**, toward
the electrode, `C-0006`'s convention unchanged.

## 2. Plan

### The cheap bound, and it is a graph statement

`OrigamiGrillage` assembles beams at **uniform** pitch `d` with a crossover between **every**
adjacent index pair `(i, i+1)`. `CLAUDE.md` already names the structure: *"the interfaces form a
**path graph** on the duplexes"*. A graph is representable that way only if it is a path — maximum
degree **2**. A honeycomb site has **three** lattice neighbours, so the block's bond graph has
maximum degree 3 and **is not a path at any relabelling of the helices**. That is one integer and it
decides *adapt* against *replace* before a line of solver is written.

Four further mismatches are counted rather than argued: the in-plane row spacing (`d` against an
alternating `d, 2d` whose mean is `3d/2`), the bonded-interface census, the foundation tributary,
and the degrees of freedom per node (3 against 4 — the parallel-axis enhancement `layers` buys is an
**axial** effect and `OrigamiGrillage` has no axial coordinate).

### The method, justified against cost

If the census says *replace*, build `HoneycombGrillage`: 60 duplex beams with
`(W, Θ = ∂W/∂s, Φ, U)` per node, honeycomb bond combinatorics from the published class rule
(class `k` at base-pair residues `b₀ + 7k (mod 21)`, so with `b₀ ≡ 0` the crossover planes are every
7 bp and class `k` occupies plane `q ≡ k (mod 3)`), a dihedral spring `k_θ` and a normal-link
penalty per bond as `OrigamiGrillage` has, **plus** an axial slip spring `k_s` — Chen et al.'s own
`k_s = 2αS/(100a)`, already in `Gen1Tile.crossoverInPlaneStiffness`. The interlayer coupling
fraction is then whatever the lattice's own shear lag delivers.

**A dense factorisation is refused on a cost bound.** At `10 × 6`, 17 crossover planes and 4 DOF per
node the lattice is 4 080 DOF at one subdivision and 7 920 at two; dense storage is 133 MB and
490 MB and the factorisation is `O(n³)`. Ordering the DOF **node-major** makes the half-bandwidth
`4·beams + 3 = 243`, so a banded Cholesky stores `n·b` and costs `n·b²/2` — 15 MB and `2.4e8` flops
at two subdivisions, against 490 MB and `8e10`. That arithmetic is done here, before the code.

**The prestrain answer avoids reconstructing the raster path.** A prestrain is a **load**
(`C-0104`), so the field is linear in it and peak dishing is a convex seminorm of the field; the
triangle inequality therefore bounds any set of ten sites by the sum of the ten largest **unit**
responses. One factorisation and one back-substitution per candidate site settles every choice of
ten at once. `C-0152` establishes that every raster crossover sits at a row **turn**, which is the
block's axial rim, so the candidate set is the bonds at the two end planes; the unrestricted ceiling
over **all** bonds is carried beside it.

### What would falsify this approach

- **`F1`** — a uniform pressure on the honeycomb lattice on a uniform Winkler foundation produces
  zero dishing. Fires if it does not; the standing falsifier, and it **does** transfer to a load.
- **`F2`** — the lattice's measured long-wavelength `D_∥` at a rigid axial coupling equals the
  closed form `(n·B + S·Σz²)/rowPitch`. Fires on a departure above the declared tolerance.
- **`F3`** — **no** single layer of a honeycomb block is a connected sheet. Written the favourable
  way round: its **not** firing is the finding, and one connected layer falsifies it.
- **`F4`** — no `OrigamiGrillage`, at any beam count and any `CrossoverLayout`, reproduces the
  honeycomb block's bond graph. Fires if one does.
- **`F5`** — the rigorous ceiling on the ten forced crossovers is below `T-5b`'s 0.10 of the stroke.
  Fires if the ceiling is above it, in which case the question is open rather than answered.
- **`F6`** — the graded dishing settles under nested subdivisions 1/2/4.

### Validity limits accepted in advance

- The `112 / 108` raster is a **two-length** raster and the lattice carries **one** row length; the
  4 bp difference is 1.36 nm of axial extent and is not modelled.
- `k_θ` is `Gen1Tile`'s square-lattice-fitted constant swept over Chen et al.'s own `α = 0.6–1.2`;
  no honeycomb measurement of it exists here. `k_s` is a **construction**, not a measurement, and is
  swept over `Gen1Tile.CROSSOVER_IN_PLANE_SWEEP`.
- Nothing here is measured on a folded object. **TRL 1–3.**
