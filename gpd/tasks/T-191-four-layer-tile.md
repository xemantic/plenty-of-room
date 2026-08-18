# T-191 — the tile §3 actually specifies: a four-layer, ~10 nm tile

| | |
|---|---|
| **Leaf** | `A8.2` (tile flatness), with `A1.2` consequences |
| **Verification type** | in-silico (beam-and-hinge grillage + orthotropic plate, both already in the tree) + logical (lattice and scaffold arithmetic) |
| **Locked units** | lengths nm, forces pN, energies `k_BT` = 4.142 pN·nm at 300 K, stiffness pN/nm, rigidity pN·nm, pressure pN/nm² = 1 MPa |
| **Conventions** | `x` along the helices, `y` across them, `z` normal to the sheet; `w` positive **downward**, compressing the polymer layer; dishing is the deflection with its area-averaged least-squares best-fit plane removed; a dishing verdict is quoted as a fraction of the free-tile stroke |
| **Conditions** | T = 300 K, aqueous buffer, 2 mM MgCl₂, `C-0022`'s solved collar at (2 mM, 10 nm gap, 0.192 V), `C-0001`'s secant foundation `k_f` = 20.201/1600 pN/nm³ |

## Formulate

### The contradiction

§3's own parameter row says **"Tile thickness ~10 nm (single-layer honeycomb)"**.
Those two clauses cannot both hold: one layer of 2 nm duplexes at the honeycomb 2.536 nm spacing is 2 nm thick, not 10.
The repository carries **both readings, in different packages, and has never reconciled them**:

- `actuator/ActuatorGeometry.kt:66` — `val tileThickness: Double = 10.0`, KDoc *"§3 says ~10 nm for a single-layer honeycomb"*;
- `electrostatics/DnaOrigamiTile.kt:65` — `val thickness: Double = 10.0`, whose KDoc states the contradiction verbatim and resolves it *toward the thick tile* (`helixCount` = 45.6 duplexes in the `edge × thickness` cross-section);
- `structure/Gen1Tile.kt` — every structural study builds `origamiSheet(INTERHELICAL_SHEET, CROSSOVER_SPACING_SHEET_BP)`, i.e. **one layer, 2 nm thick**.

So the electrostatic load and the actuator stack are computed on a ~10 nm tile and the **flatness** is computed on a 2 nm one.

Two further statements point the same way:

- `C-0086` measures the single-layer sheet at **1 680** of M13's **7 249** nt — a **4.31×** excess of scaffold that has no use;
- NDI's answer to decision 5 (2026-08-18): *"M13, circular ~7-8K nucleotides … to use exess scaffold, just make the tile thicker. The 1700 nucleotide structure the agent is proposing seems… thin and low stiffness."*

### The numeric target

The flatness negative — `ANSWERS.md` §4(g), owned by `C-0087`, `C-0089`, `C-0093` and `C-0098` — is:
under the measured staple dropout, no attachment distribution keeps the tile's dishing below
`T-5b`'s **0.10 of the stroke** at any buildable path count; the density requirement is **13 columns / 195 paths**
against a plan ceiling of **34**, i.e. **5.7×** short.

`T-191` asks whether that negative survives on the tile §3 actually specifies.

### The acceptance predicate

Falsifiable, in four parts:

- **P1** — the four-layer sheet's flexural rigidities and the reach `ℓ = (4D/k_f)^(1/4)` are re-derived here
  and reproduce `C-0006`'s published variant-table row to the emitted precision.
- **P2** — `C-0089`'s run-robustness column demand `⌈(j+1)·edgeX/ℓ⌉` is re-evaluated on the four-layer reach,
  **over the whole interlayer-coupling bracket**, and the demand is reported at both ends.
- **P3** — `C-0087`/`C-0089`'s dropout grading (`MEASURED_DEPTH`, 10 000 realisations, `C-0087`'s stream)
  is re-run on the four-layer lattice at `C-0086`'s 38.08 nm buildable width, and the 90th-percentile
  dishing over the stroke is reported against 0.10 at every path count of the density sweep.
- **P4** — the scaffold arithmetic says how many honeycomb layers one circular M13 pays for at that width.

**A `PASS` is the four numbers, not a favourable verdict.** The verdict may be that the thicker tile
does not lift the negative; that is a discharge of the predicate, not a failure of it.

### Falsifiers, stated before the run

| | falsifier | what it would mean |
|---|---|---|
| **F1** | a uniform load on the four-layer lattice dishes more than solver noise | the solver, not the physics — `CLAUDE.md`'s standing falsifier |
| **F2** | the smeared equivalent sheet's `D_∥`, `D_⊥`, `D_k` do not reproduce the four-layer `OrigamiSheet`'s | the substitution that lets a planar grillage carry a multi-layer body is invalid, and every lattice number here is void |
| **F3** | the four-layer p90 dropout dishing stays above 0.10 at every path count inside the plan ceiling | **the thicker tile does not move the flatness negative** — the branch is dead and `ANSWERS.md` §4(g) stands unchanged |
| **F4** | four honeycomb layers at the buildable width need more than 7 249 nt | the tile NDI describes cannot be folded from one M13, and the excess-scaffold argument fails |
| **F5** | the single-layer baseline re-run here does not reproduce `C-0089`'s published p90 and `C-0087`'s published column demand | the harness is not the one that produced the negative, so no comparison is licensed |

## Plan

### The cheap bound runs first, and it is already in the corpus

`C-0006`'s variant table gives the four-layer honeycomb sheet `D_∥` = 14 310.78 pN·nm against the
single-layer 85.50, and `D_⊥` ≥ 19.222 against 3.345. `C-0058`'s reach is their fourth root,
so the whole prediction is one `pow(0.25)` and needs no solve:

`ℓ = (4D/k_f)^(1/4)`, `k_f` = 0.012625625 pN/nm³.

That is written down, with its own arithmetic re-derived, **before** any lattice is assembled,
and the column demand it predicts is compared against what the solve returns.

### The interlayer coupling is the whole uncertainty, and it is 39× wide

`OrigamiSheet` offers `InterlayerCoupling.NONE` (`D ∝ n`) and `RIGID` (parallel-axis on the duplex
stretch modulus). For four honeycomb layers these differ by `1 + S Σy²/(n B)`.
No solve can narrow that; a literature cross-check on a measured multilayer bundle can bound it,
and gate 5 goes looking for one. **Both ends are carried and every number is reported at both.**

### Why a planar grillage may carry a multi-layer body, and where it may not

`OrigamiGrillage` reads exactly five things from its `OrigamiSheet` — `interhelicalDistance`,
`crossoverSpacing`, `crossoverHingeStiffness`, `duplex.bendingRigidity`, `duplex.torsionalRigidity` —
and **never reads `layers` or `interlayerCoupling`**. Building it on `Gen1Tile`'s
`four-layer-honeycomb-rigid` variant would therefore produce a lattice bit-identical to the
single-layer honeycomb one. The machinery is single-layer, and saying so is part of the deliverable.

The substitution used instead is explicit: a **smeared equivalent sheet**, one layer whose per-beam
`EI`, per-crossover `k_θ` and per-beam `GJ` are chosen so that its `D_∥`, `D_⊥` and `D_k` equal the
multi-layer body's exactly (gate 2 asserts it). What that representation **cannot** carry:

- transverse shear. At 9.608 nm thickness over a 38.08 nm span the thickness/span ratio is 0.252,
  where Kirchhoff is not safe — `C-0006` says so in its own validity range. `D_∥` is therefore an
  upper bound *again*, on top of the `RIGID` assumption.
- the honeycomb lattice's **three** crossover azimuths. `CrossoverLayout` models a two-parity
  alternation, which is the square lattice. The smeared rigidities are right; the crossover
  *combinatorics* are not, so no phase sweep is run and the `T-10` centred construction is used.
- a dropout that removes one layer's staple without removing the other three. The dropout applied
  here is `C-0087`'s single-layer statistic at the station, which is the **adverse** direction:
  a four-layer body has more redundancy per station than the measurement it is graded under.

### Method, justified against cost

| step | method | cost | why not something dearer |
|---|---|---|---|
| rigidities, reach, column demand | closed form | free | it is a fourth root |
| scaffold arithmetic | integer arithmetic | free | `C-0086` already fixed the width and the per-row base-pair count |
| free-tile and coupled dishing | the existing `OrigamiGrillage` + `InfluenceSurrogate` bank | one factorisation per host, `n` solves per station set | a 3-D finite-element brick would be the honest model of a four-layer body and would answer a question the corpus cannot compare against; the point of this task is a **matched** re-run |
| dropout grading | `C-0087`'s pre-drawn presence stream, common random numbers across sheets | 10 000 realisations × an `n × n` solve from a precomputed Gram matrix | paired sampling is what makes a 4× difference readable — `CH-0119` |

### What result would falsify this approach

If the smeared equivalent sheet's rigidities do not reproduce the multi-layer sheet's (**F2**),
the whole method is void and the answer needs a 3-D model.
If the single-layer baseline does not reproduce `C-0089` (**F5**), the harness is not the one
that produced the negative and no comparison is licensed.
Both are asserted as tests before any four-layer number is emitted.

Result: [`../results/T-191-four-layer-tile.json`](../results/T-191-four-layer-tile.json),
claim [`../claims/C-0109-four-layer-tile.md`](../claims/C-0109-four-layer-tile.md).
