# C-0154 — **A honeycomb block's interfaces are NOT A PATH GRAPH, and that one integer says `OrigamiGrillage` must be replaced rather than parameterised.** A honeycomb site has **three** lattice neighbours where an `OrigamiGrillage` bonds beam `i` to beam `i+1` and to nothing else — maximum degree **3** against **2** — so no relabelling of the helices puts every bond between consecutive indices; the boundary is exact and sits at `n = 2`. Built instead as a three-dimensional beam-and-bond lattice with an **axial** coordinate, the same object then says three things the smeared sheet could not: **one layer of a honeycomb block is not a sheet at all** but **5** dimer components on `10 × 6` and **8** on `15 × 4`; `D_∥` reproduces `multiLayerRigidities` at `C-0141`'s corrected `3d/2` pitch to **`2.8e−15`** while `D_⊥` is **`7/24 = 0.291666667`** of the same formula, **`24/7`** overstated, because only half the in-plane adjacent pairs are bonded and an interlayer bond carries half the lever arm; and the **composite fraction becomes an OUTPUT**, measured at **0.246803583** on `10 × 6` and **0.406535456** on `15 × 4` — *straddling* the 0.26–0.33 band the corpus inherits — and it moves **0.0717149752 → 0.737066133** over a `56 → 448 bp` row, so it is a **length**, not a material constant. And the question `C-0152` refused is answered: at `10 × 6` the ceiling on **all ten** forced scaffold crossovers, over **every** choice of sites, is **0.0797106495** of the stroke at the calibrated coupling and **0.0881294066** at the lattice's own — both **inside** `T-5b`'s 0.10 — and the departure that would reach the tolerance is **22.1867557–27.1460514°**, **1.29–1.58×** the **17.1428571°** the raster needs. **The forced crossovers never DECIDE the verdict at any state**

| | |
|---|---|
| **Task** | [`T-253`](../tasks/T-253-honeycomb-grillage.md) — a honeycomb grillage, so a prestrain on a four-layer face can be solved at all |
| **Leaf** | `A8.2` |
| **Verification type** | **logical** (an exact graph census of what the honeycomb lattice supplies against what `OrigamiGrillage`'s assembly requires, run **before** any solver) **+ in-silico** (a new three-dimensional beam-and-bond lattice, its long-wavelength limits in both directions, a Schur-complement measurement of the composite fraction, and a linear prestrain influence bank over every bond) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** Nothing here is measured on a folded object. The crossover-class rule is read from the caDNAno paper; `k_θ` is Chen et al.'s fitted constant and `k_s` their own softened-bond **construction**, swept over four decades. |
| **Verdict** | **PASS on all four predicates. `F1`–`F6` did not fire; `F7` FIRED and its firing is the finding.** `P1` the cheap bound is a six-row census with a per-row `adaptable` flag and it runs before the solver; `P2` both long-wavelength limits are measured against closed forms and the composite fraction is a Schur complement rather than an input; `P3` the prestrain answer is a triangle-inequality ceiling over **every** choice of ten sites and needs no raster path; `P4` two convergence axes and **five** reproductions are emitted, one of them exact at `2.8e−15`. |
| **Provenance** | [`gpd/results/T-253-honeycomb-grillage.json`](../results/T-253-honeycomb-grillage.json) (`tile.HoneycombGrillageStudyKt`, **new**); models [`tile/HoneycombGrillage.kt`](../../src/main/kotlin/tile/HoneycombGrillage.kt) and [`structure/BandedCholesky.kt`](../../src/main/kotlin/structure/BandedCholesky.kt) (**both new files**; `tile/HoneycombFaceLattice.kt`, `tile/HoneycombBondClassResidues.kt`, `tile/FourLayerTile.kt` and `structure/OrigamiGrillage.kt` were **read, not edited**). **30 gate-named tests written first and watched fail** — [`tile/HoneycombGrillageTest.kt`](../../src/test/kotlin/tile/HoneycombGrillageTest.kt) (**20**) and [`structure/BandedCholeskyTest.kt`](../../src/test/kotlin/structure/BandedCholeskyTest.kt) (**10**). Result file **BYTE-IDENTICAL across two independent JVM runs**. |
| **Conditions** | T = 300 K, aqueous 2 mM MgCl₂, `k_BT` = 4.141947 pN·nm. Honeycomb `d` = 2.536 nm (SAXS); in-plane row pitch `3d/2` = 3.804 nm; layer pitch `d√3/2` = 2.19624042 nm; rise 0.34 nm/bp; crossover planes every **7 bp**, one pair every **21 bp**, class `c` on plane `q ≡ c (mod 3)`. Row 112 bp = 38.08 nm; cross-sections `10 × 6` (38.04 nm in plane) and `15 × 4` (57.06 nm). `k_θ` = 13.5294118 pN·nm/rad at `α` = 1; `k_s` = 64.7058824 pN/nm, swept ×`1e−4 … 1e4`; link penalty `1e4` pN/nm. `C-0001`'s secant foundation, on the **gap-facing face only**; `C-0022`'s solved collar at 2 mM / 10 nm / 0.192 V read from `gpd/results/T-3b-tile-edge-load-profile.json`; §3's 100 pN over the face; `T-5b`'s 0.10; 81 × 81 dishing grid; 1 and 2 beam subdivisions. The forced departure **17.1428571°** and the 10-of-59 census read at run time from `gpd/results/T-246-forced-scaffold-crossover-price.json`. |
| **Consumes** | [`C-0141`](C-0141-honeycomb-station-lattice-and-placement.md) (the cross-section, the two pitches, the `m × n` block — `HoneycombFaceLattice.kt` used **unmodified**), [`C-0148`](C-0148-face-bond-class-residues-and-row-span-columns.md) (the published class rule and `honeycombBondClass`), [`C-0152`](C-0152-forced-scaffold-crossover-price.md) / [`CH-0188`](../challenges/CH-0188-the-recommended-raster-does-not-close.md) (the departure and the census), [`C-0104`](C-0104-row-end-prestrain.md) (a prestrain is a **load**, and the triangle inequality), [`C-0022`](C-0022-tile-edge-load-profile.md), [`C-0001`](C-0001-layer-stiffness.md), [`C-0116`](C-0116-composite-fraction-threshold.md), [`C-0120`](C-0120-cross-section-comparison.md), [`C-0109`](C-0109-four-layer-tile.md) |
| **Constrains** | **`C-0152`'s open question 1 is ANSWERED** and its refusal of the flatness channel is discharged. **Two challenges are raised.** [`CH-0200`](../challenges/CH-0200-the-across-helix-rigidity-formula-does-not-describe-a-honeycomb.md) against `OrigamiSheet.acrossHelixRigidity` and `multiLayerRigidities` as models of a honeycomb block; [`CH-0201`](../challenges/CH-0201-the-composite-fraction-is-a-length-not-a-constant.md) against the transfer of Kauert et al.'s 0.26–0.33 onto a 38 nm tile. |

---

## 1. The cheap bound, and it decided *replace* before a line of solver

`CLAUDE.md` names the gap in two sentences — *"`OrigamiGrillage` NEVER READS `layers` OR
`interlayerCoupling`"* and *"`CrossoverLayout`'s two-parity alternation makes its crossover
combinatorics SQUARE-LATTICE"*. What it does not say is whether the class can be **extended**.
Six requirements were counted side by side, and the first is one integer:

| requirement | `OrigamiGrillage` assumes | a honeycomb block supplies | adaptable |
|---|---|---|---|
| **the interface graph** | beam `i` bonded to `i+1` only, so the interfaces are a **PATH** — maximum degree **2** (`C-0056`, `CH-0066`) | **three** lattice neighbours per site, maximum degree **3** at every `m × n` with `n ≥ 3` | **no** |
| the across-helix beam spacing | a uniform pitch `d` | an alternating `d, 2d` about a mean of `3d/2` — the corrugation | **no** |
| which adjacent pairs are bonded | **every** adjacent pair, at alternating column parity | exactly **half** the in-plane ones, decided by `(r + c) mod 2` | **no** |
| the crossover combinatorics | a **two**-parity alternation, 16 / 32 bp | **three** bond classes at 7 bp, one pair every 21 bp | **no** |
| degrees of freedom per node | **three**; no axial coordinate | the parallel-axis term `layers` buys is `S Σz²`, an **axial** strain | **no** |
| the foundation | a Winkler strip under **every** beam | **one** gap-facing face; `3m` buried helices touch no polymer | **no** |

A graph is representable as `OrigamiGrillage`'s interface structure only if it is a path, and the
maximum degree of a path is two. **So no relabelling of the sixty helices can work, and the
question is settled with no arithmetic beyond a degree count.** The boundary is exact and it was
asserted rather than assumed: at **two** helices per row the maximum degree is **2**, so an
`m × 2` block *is* path-representable — which is why the statement has to be made about `n ≥ 3`
and not about honeycombs in general.

## 2. One layer of a honeycomb block is not a sheet

The in-plane bond `(r, c) → (r+1, c)` exists only where `r + c` is even, so **half** the in-plane
adjacent pairs carry no bond at all. A single layer therefore falls apart:

| | helices | face helices | in-plane interfaces | interlayer interfaces | **components of ONE layer** |
|---|---|---|---|---|---|
| **`10 × 6`** | 60 | 10 | 27 | 50 | **5** |
| **`15 × 4`** | 60 | 15 | 28 | 45 | **8** |

They are **dimers**. The consequence is mechanical rather than decorative: **the across-helix load
path in a honeycomb block necessarily traverses the thickness**, so `OrigamiSheet`'s
`acrossHelixRigidity = layers × k_θ d / p` — a statement about a stack of independent single-layer
sheets — describes a body a honeycomb block does not contain. That is `CH-0200`.

## 3. What the lattice measures at long wavelength, and which half of the corpus survives

Two smooth fields are imposed and the energy read. Along the helices the composite-compatible field
carries **zero** slip and **zero** link extension identically, so its energy *is* the parallel-axis
closed form; across the helices the beams neither bend nor twist and the hinges carry all of it.

| | measured on the lattice | `multiLayerRigidities` at `d` | at `C-0141`'s `3d/2` | lattice / corpus at `3d/2` |
|---|---|---|---|---|
| `10 × 6` `D_∥`, rigid composite | **24771.776** | 37157.664 | **24771.776** | **1.0** (departure `2.8e−15`) |
| `10 × 6` `D_⊥`, independent | **12.6141869** | 28.8324271 | 43.2486406 | **0.291666667 = 7/24** |
| `15 × 4` `D_∥`, rigid composite | **7215.85068** | 10823.776 | **7215.85068** | **1.0** |
| `15 × 4` `D_⊥`, independent | **8.30934531** | 19.2216181 | 28.8324271 | **0.288194444** |

**The same formula that reproduces `D_∥` exactly overstates `D_⊥` by `24/7 = 3.42857×`.** The
reason is entirely in §2's census: an in-plane bond carries a full `d` of lever arm and an
interlayer bond only `d/2`, and only half the in-plane pairs are bonded at all. A smeared sheet has
no parameter that can see either fact.

## 4. The composite fraction is an OUTPUT, and it is a LENGTH

`OrigamiSheet` carries `InterlayerCoupling.NONE`/`RIGID`, and `FourLayerTile` interpolates between
them at a **fitted** fraction. Adding the axial coordinate and Chen et al.'s own slip spring
`k_s = 2αS/(100a)` makes it a **measurement**: impose the bending kinematics, relax the axial
coordinates to equilibrium — a Schur complement, one extra factorisation and no iteration — and
read where the realised rigidity sits between the two limits.

| | independent | parallel axis | **realised** | **`f`** | 0.26–0.33 |
|---|---|---|---|---|---|
| **`10 × 6`** | 362.776025 | 24771.776 | **6387.00468** | **0.246803583** | **below** |
| **`15 × 4`** | 241.850683 | 7215.85068 | **3077.02895** | **0.406535456** | **above** |

**The two 60-helix cross-sections fall on opposite sides of the band the corpus applies to both.**

And it is a **length**. Swept over the row at `10 × 6`, one cross-section, one `k_s`, one load case:

| row | 56 bp = 19.04 nm | 112 bp = 38.08 nm | 224 bp = 76.16 nm | 448 bp = 152.32 nm |
|---|---|---|---|---|
| **`f`** | **0.0717149752** | **0.246803583** | **0.476262276** | **0.737066133** |

In *uniform* bending an infinitely long composite is rigid whatever its connectors, because the
axial force in each layer is constant and no shear flows; what a partial composite measures is a
**boundary layer at the free ends**. So Kauert et al.'s 0.26–0.33, read on 740 nm to 2 µm bundles
by bending fluctuations, is neither this length nor this load case, and that the 38 nm reading
lands beside it is **not** a validation. That is `CH-0201`.

**And the fraction is not even one number on one lattice.** Read on the *dishing* under `C-0022`'s
collar instead of on the *rigidity*, the same `10 × 6` block at the same nominal `k_s` reports
**0.940471** against **0.246804** — **3.81×** — because the dishing moves only 0.149649 → 0.125948
of the stroke over four decades of `k_s` and is dominated by the across-helix compliance instead.
`CLAUDE.md`'s *quote it with the state it is read at*, on a calibration rather than on a stiffness.

## 5. The prestrain — a rigorous ceiling over every choice of ten sites

`C-0104` establishes that a prestrain is a **load**, so the field is linear in it, and peak dishing
is a convex **seminorm** of the field. The sum of the ten largest **unit** responses therefore
bounds **every** choice of ten sites, and one factorisation plus **435** back-substitutions settles
the whole placement question **without reconstructing the raster path** — which is exactly the half
`C-0152` could not run.

| | across-helix enhancement | free tile | **ten largest** | **ceiling** | rim-only ceiling | inside `T-5b` |
|---|---|---|---|---|---|---|
| `10 × 6` | 1.0 (none carried) | 0.127358454 | 0.279799381 | 0.407157835 | 0.370361485 | **no** |
| **`10 × 6`** | **21.1851817** (calibrated) | **0.0449400126** | **0.0347706369** | **0.0797106495** | 0.0737723859 | **YES** |
| **`10 × 6`** | **17.6059172** (this lattice's own `f`) | **0.0477844467** | **0.0403449599** | **0.0881294066** | 0.0815816716 | **YES** |
| `15 × 4` | 1.0 | 0.312237799 | 0.48567377 | 0.797911569 | 0.747863554 | no |
| `15 × 4` | 9.65079217 | 0.227177955 | 0.105713956 | 0.332891911 | 0.318106129 | no |
| `15 × 4` | 12.7228458 | 0.220064299 | 0.0855856842 | 0.305649983 | 0.292462133 | no |

**At every state in which the free tile is flat, the ceiling is also inside the tolerance**, and at
every state in which the ceiling is outside it the **free tile alone** already is. So `F5` — *"the
ten forced crossovers never decide the flatness verdict"* — does not fire, and that is the answer:
what decides the verdict is the across-helix coupling, not the forcing.

At the recommended `10 × 6` the departure that would take the ceiling to 0.10 is
**22.1867557–27.1460514°** against the **17.1428571°** the raster needs — **1.29–1.58×** of margin
— and `C-0152`'s own arithmetic says the next rung of the lattice is **34.2857143°**, which is
past it at both ends. **`C-0152`'s severity verdict is upheld on a second, independent axis.**

## 6. What made it affordable

Ordering the degrees of freedom **node-major** — every beam's unknowns at one node column before
any unknown at the next — makes every coupling either within a node column (bonds) or between two
consecutive ones (beam elements), so the half-bandwidth is **243** on **4080** unknowns:

| | dense | banded | ratio |
|---|---|---|---|
| storage | **133.1712 MB** | **7.96416 MB** | **16.7213115×** |
| work | `O(n³)` | `n b²/2` | ~`10²` |

That arithmetic is in the task file, **before** the code, and it is why the whole study — two
cross-sections, three couplings, a ten-point `k_s` sweep, a four-point length sweep and **2535**
unit prestrain responses — runs in **under half a minute**.

## 7. The five gates

| gate | how it was discharged |
|---|---|
| **1 — dimensional** | every bond asserted **exactly** one lattice constant long in the emitted `(y, z)` and its unit vector asserted against the same difference; the face tributaries asserted one row pitch wide, centred on their own beam, and summing to the in-plane width; rigidities pN·nm, stiffnesses pN/nm, angles rad internally |
| **2 — limiting cases** | a `1 × 2` block carries one interface and **no** in-plane bond; a one-helix row falls into `⌈m/2⌉` components at every `m` from 2 to 12; the imposed along-helix field's energy asserted equal to `½κ²L(N·EI + S Σz²)` with slip, link and hinge energies at `<1e−18`; the across-helix field's energy asserted equal to the hinge sum with beam, axial, slip and link at `<1e−18`; a vanishing slip spring returns the **independent** limit and a rigid one cannot exceed the parallel-axis one |
| **3 — symmetry / conservation** | the standing uniform-load falsifier holds (`F1`); the global force balance closes at `5.7e−12` (`F6`); a prestrain changes **no** entry of the stiffness matrix, asserted band-entry by band-entry; the prestrain response is exactly linear in the angle and an all-zero map is the unstrained lattice; **the axial pin is asserted to remove a rigid mode and nothing else** — moving it from beam 0 to beam 7 leaves the dishing and the mean deflection unchanged; the rigid-mode duals asserted equal to the two-field quadrature they replace |
| **4 — numerical convergence** | nested beam subdivisions 1 / 2 / 4 (test) and 1 / 2 (study, departure `8.1e−5`); the dishing sample grid 41 / 81 / 161 (departure `0.0`); the banded factor asserted to reconstruct its own matrix and to reproduce the dense Cholesky to `1e−12` on a 60 × 60 system |
| **5 — literature cross-check** | the crossover-class rule read from `C-0148`'s `honeycombBondClass`, itself read from the caDNAno paper, and the **in-plane bonds asserted to be all one class** with the interlayer ones sharing the other two; a class asserted to recur every **21 bp** on one interface; five reproductions, the tightest at `2.8e−15` |

## 8. Falsifiers

| | statement | fired | note |
|---|---|---|---|
| **`F1`** | a uniform pressure on a uniform Winkler foundation produces zero dishing | **no** | the standing falsifier **does** transfer to a load, and it is what catches a wrong tributary — which is why each face beam's strip is centred on its own axis and not tiled |
| **`F2`** | the measured long-wavelength `D_∥` equals the parallel-axis closed form | **no** | the imposed field's slip and link extension vanish identically, so the agreement tests the axial arms and the geometry at once |
| **`F3`** | **no** single layer of a honeycomb block is a connected sheet | **no** | written the favourable way round; **its not firing is the finding**, 5 and 8 components |
| **`F4`** | no `OrigamiGrillage`, at any beam count and any `CrossoverLayout`, reproduces a four-layer honeycomb block's bond graph | **no** | maximum degree 3 against a path's 2; the boundary is exact at `n = 2` |
| **`F5`** | the ten forced crossovers never **decide** the flatness verdict | **no** | **the question `C-0152` refused**, and this is the right way round: where the free tile already exceeds the tolerance the forcing is not what breaks the design |
| **`F6`** | the global force balance holds | **no** | relative residual `5.7e−12` |
| **`F7`** | the composite fraction is a property of the **crossovers**, so a bundle-measured value transfers to a 38 nm tile | **FIRED** | **its firing is the finding**: 0.0717149752 to 0.737066133 over 56 → 448 bp on one lattice, one `k_s` and one load case |

## 9. Validity range, and what this does NOT establish

- **TRL 1–3**, model-consistent and traceable, not empirically demonstrated.
- **The lattice carries no across-helix parallel-axis term.** The layers' membrane action across
  the helices needs an **in-plane transverse** coordinate this model does not have, so its `D_⊥` is
  the **independent** one and therefore a **lower** bound. The bracket is run at **three** ends —
  none, the corpus's calibrated enhancement, and the one this lattice's own measured `f` implies
  through `k_s/k_θ = S/B` — and the two ends that carry any interlayer coupling agree on **every**
  verdict. At the end that carries none the **free** tile already exceeds the tolerance.
- **The `112 / 108` raster is a TWO-LENGTH raster and this lattice carries ONE row length.** The
  4 bp difference is 1.36 nm of axial extent and is not modelled. `C-0151`'s recommended `102 / 109`
  raster **closes** and forces nothing, so this answer is about the alternative it displaced.
- **The forced crossovers are SCAFFOLD crossovers, 5 bp from a staple position**, and the lattice's
  node stations are the staple planes. That is precisely why the answer is delivered as a ceiling
  over **every** bond rather than as a value at ten named ones.
- `k_θ` is `Gen1Tile`'s square-lattice-fitted constant; **no honeycomb measurement of it exists in
  this repository.** `k_s` is a **construction**, not a measurement, and is swept over four decades.
- Kirchhoff is not safe at these thicknesses (`C-0109`, `C-0120`): transverse shear is not carried,
  so every `D_∥` here is an upper bound.
- **The block is FREE.** No attachment coupling is applied, so every dishing number is `C-0109`'s
  **uncoupled reference** and not a design — `CLAUDE.md`'s *"always run the uncoupled tile as the
  reference"*, read the other way round.
- The foundation acts on the gap-facing face only and the opposite face is free, which is the
  physical arrangement and not a simplification.

## 10. Open questions

- What the **across-helix** parallel-axis term is worth once an in-plane transverse coordinate is
  carried. `CLAUDE.md` records that `k_s/k_θ = S/B` makes the enhancement the same factor both
  ways; this lattice can **test** that rather than assume it, and doing so removes the only bracket
  in §5.
- Whether a **coupled** honeycomb block — `C-0142`'s and `C-0151`'s cells re-graded on this lattice
  rather than on a smeared equivalent sheet — moves any flatness verdict. Every coupled cell in the
  corpus is a smeared single-layer square-lattice solve.
- Whether the raster **turn**'s position on the block's axial rim gives it the zero coefficient
  `C-0147` proved for the raggedness (`T-254`). The ceiling here is placement-free and therefore
  does not answer it; the rim-restricted ceiling is 0.0737723859–0.0815816716, **7.4 %** below the
  unrestricted one, so the answer is a refinement rather than a reversal.
- What a **per-layer** defect does. This lattice can remove one crossover of one interface of one
  layer, which a smeared equivalent sheet cannot express at all — `C-0087`'s dropout statistics
  have never been read on a multi-layer body.
