# C-0169 — **`C-0157`'s trajectory cannot answer `T-9`'s second deliverable, on five counts, and the fifth is that no estimator for the coordinate was ever written — so the deliverable is delivered as a CRITERION instead, and the criterion settles it: at the crossover's own derived displacement stiffness `64.7058824 pN/nm` the vertical link carries `0.0120392417` of the whole present-versus-absent movement, moves `C-0015`'s registration lever by `0.370243116 %`, and is worth `0.00341573646` of the stroke — which is `1.12787346×` the ENTIRE row-end hinge unknown `C-0099` spent a task settling.** The binary reading of the constraint is right on three of four pre-registered predicates and fails on the fourth by `1.13×`; and with the link **deleted** every crossover carries **exactly zero** vertical force, so `C-0015`'s design rule does not flatten, it **ceases to exist**

| | |
|---|---|
| **Task** | [`T-9`](../tasks/T-9-crossover-hinge-constant.md) §4–§5 — the **second** of three deliverables, the crossover's vertical/axial compliance |
| **Leaf** | `A1.2`, with `A8.2` |
| **Verification type** | **in-silico** (`structure.OrigamiGrillage`, the vertical link stiffness swept globally over `C-0020`'s own four decades at all 56 crossovers, `C-0090`'s exhaustive centro-symmetric 34-root enumeration at every rung under `C-0022`'s solved collar) **+ logical** (a cheap bound that is five file checks and three divisions, and which settles the shape of the answer before any solve) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated — and NOTHING about a crossover's vertical stiffness has been measured anywhere.** The value swept is a **construction** this repository already applies to the in-plane axis, reported over four decades exactly as `C-0020` reports that one. |
| **Verdict** | **PASS on `T-9`'s second deliverable, read as the question the task fixed — *is the binary reading right?* — and NOT as a measured `k_z`, which this run states plainly it cannot supply.** `V1`, `V3` and `V4` do **not** fire; `V2` fires at `1.12787346×` on its corrected threshold and not on the one first written, and **both are published**. `F1`–`F5` did not fire. |
| **Provenance** | [`gpd/results/T-9b-crossover-vertical-compliance.json`](../results/T-9b-crossover-vertical-compliance.json), produced by `anchoring.CrossoverVerticalComplianceStudyKt`; model in `src/main/kotlin/structure/CrossoverVerticalCompliance.kt` (**new**); `OrigamiGrillage.kt`, `Gen1Tile.kt`, `CrossoverSoftening.kt`, `CrossoverLayout.kt`, `RowEndCrossoverStiffness.kt`, `BuildableRasterWidth.kt` and `UpwardRootPlacement.kt` were **read, not edited**; **19 gate-named tests** in `src/test/kotlin/structure/CrossoverVerticalComplianceTest.kt`, red-checked (one-line mutation of the construction turns **4** of them red); 10 rungs x 163 296 enumerated placements, ~8 minutes; the file was emitted **three times** across two code revisions and **every physical number is identical in all three** — run C against run D moves **0 of 420** fields but the two labels this task deliberately changed, and run B against run D moves **5 of 233** shared numeric fields, all five the reproduction rows deliberately re-keyed |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; `C-0086`'s buildable **38.08 nm** seamless raster, 15 duplexes, crossover **phase 8**, 8 columns, **56** crossovers, `d = 2.69 nm`, rise `0.34 nm`, `p = 32 bp` per interface; `C-0022`'s **solved** collar at 2 mM / 10 nm / 0.192 V; `C-0017`'s mandate over `C-0055`'s 34 roots |
| **Consumes** | [`C-0157`](C-0157-crossover-hinge-constant-from-oxdna.md) (`T-9`'s first deliverable, and the run this needed), [`C-0009`](C-0009-discrete-lattice-tile.md) (the grillage and the rigid link), [`C-0099`](C-0099-row-end-crossover-stiffness.md) (the penalty step and its unresolved bracket), [`C-0015`](C-0015-crossover-phase-and-registration.md) (the registration design rule), [`C-0020`](C-0020-in-plane-shear-lag.md) (the softened-bond displacement construction and its four-decade sweep), [`C-0090`](C-0090-buildable-raster-width.md) (the lattice and its recommended placement), [`C-0086`](C-0086-seamless-scaffold-routing.md), [`C-0022`](C-0022-tile-edge-load-profile.md) |
| **Raises** | [`CH-0217`](../challenges/CH-0217-the-penalty-was-never-compared-against-the-crossovers-own-displacement-stiffness.md) against `C-0009`, and [`CH-0218`](../challenges/CH-0218-a-pruned-trajectory-forecloses-every-question-nobody-asked-it.md) against `C-0157` |
| **Constrains** | Nothing is overturned. `C-0009`'s rigid link, `C-0015`'s registration rule, `C-0090`'s recommended placement and `C-0099`'s binary reading all keep their values; what changes is that the rigid link is now **bounded** rather than **asserted**, over the interval `C-0009`'s own convergence sweep never reached. |

---

## 1. Deliverable 1 — the cheap bound: the existing trajectory cannot answer this, on five counts

`TASKS.md`'s `T-9` row prices this deliverable *"at no extra cost"*, because the run that measured
the interduplex roll is supposed to be on disk.
Five checks, each one command, and **none of them passes**:

| | check | reading |
|---|---|---|
| 1 | `build-oxdna/`, the directory `tools/T-9-emit-result.py` reads | **absent** from this checkout |
| 2 | the raw `.dat` frames | **pruned** — `C-0157` §7, 649 MB |
| 3 | the host | `tools/oxdna/README.md` names an **Apple M1 / macOS** box; this is Linux with no oxDNA build |
| 4 | `gpd/results/T-9-crossover-hinge-constant.json` | carries **no vertical field**: `hinge/*` is an angle in degrees, `sawtooth/*` a scalar `\|Δr\|` |
| 5 | the estimators | **none computes the coordinate.** `interduplex_roll.py` computes signed **angles**; `analyse_tile.py` reduces the interhelical vector to its **norm** |

**Check 5 is the one that matters, and it is why this is a finding rather than housekeeping.**
It is the only one a retained trajectory would *not* have cured: even with all 649 MB in place,
no code in this repository computes the relative **out-of-plane** offset of two crossover-bonded
duplexes — `OrigamiGrillage.linkExtension`'s own coordinate. The measurement was never within
reach of the analysis as built; pruning removed the option of writing six more lines later.

`CH-0218` is raised on that, and it names the general form:
**a run's raw output is the only artifact from which a question nobody thought to ask while it
existed can be answered.**

### The run that could answer it, priced

| item | quantity | basis |
|---|---|---|
| the coordinate's **locality** | **local** — one relative displacement of two adjacent duplexes at one node, over the 49 sites C-0157's own seven-column design carries | the same locality as the **roll**, which `C-0157` read to better than 10 % across five replicas at 450 frames — **not** the three plate modes, which are the tile's softest and slowest and got 20.5–24.2 independent samples |
| the protocol | **unchanged** — oxDNA2, 15 × 112 bp at phase 8, 300 000 steps × 5 replicas | `C-0157`'s own run, plus a six-line estimator |
| the wall clock | **about one day** on an 8-core CPU box | `tools/oxdna/README.md`, `T-9` §3 |
| the disk | **~649 MB** of frames, which must not be pruned this time | `C-0157` §7 |
| what it is **not** | **not** the 12–55 h *per replica* the three plate rigidities need | `C-0157`'s own convergence diagnostics |
| the caveat, stated in the adverse direction | **a softer restraint decorrelates slower**, so the locality argument is self-referential the wrong way and the run must emit the vertical coordinate's own **autocorrelation time** beside its variance | `CLAUDE.md`: *quote a variance with its bandwidth* |

**So the deliverable is not abandoned; it is converted.** A measured `k_z` is worth nothing until
there is a threshold to read it against, and the threshold is three orders of magnitude cheaper.
That is what runs here.

## 2. Deliverable 2 — the cheap bound that settles the shape, and costs three divisions

`OrigamiGrillage.RIGID_LINK_STIFFNESS = 1e4 pN/nm` is a **penalty**, justified in its own KDoc
against the duplex stretch modulus per nm and against the hinge's equivalent `k_θ/d²`.
`Gen1Tile.crossoverInPlaneStiffness` meanwhile **derives** a crossover's *displacement* stiffness,
in the same file, from Chen et al.'s softened-bond construction with the duplex constant that
describes displacement rather than rotation:

&nbsp;&nbsp;&nbsp;&nbsp;`k = 2αS/(100a) = 64.7058824 pN/nm` at `α = 1`.

The vertical link and the in-plane connector are **the same two phosphate bonds resisting a
relative displacement of the same two duplexes**, on orthogonal axes.

| division | value |
|---|---|
| penalty / derived value | **154.545455×** |
| derived value / `k_θ/d² = 1.86971045` | **34.6074348×** |
| derived value as a **fraction of the penalty** | **0.00647058824** |
| `C-0099`'s channel-B bisection bracket, upper end | **0.015625** |

**The derived value lies inside the one interval of the penalty axis this repository has never
resolved** — the bracket `C-0099` returned and read as locating a *discontinuity*.
And `C-0009`'s own `linkStiffness` convergence sweep (`1e2, 1e3, 1e4, 1e5, 1e6`) has its **lowest
rung 1.54545455× above** the derived value, so the sweep that established *"the transmitted force
has stopped moving"* stopped just short of where the answer is. `CH-0217` is raised on that.

## 3. Deliverable 3 — the sweep: the link ALONE, hinge intact, at all 56 crossovers

`C-0099` swept the **14 row-end** crossovers and scaled the hinge **and** the link together.
This sweeps the **link alone, at all 56**, over `C-0020`'s own four decades — a channel no study
in this repository had run.

| rung | `k_z` (pN/nm) | best 34-root dishing | free-tile dishing | peak crossover force (pN) | `C-0015` lever |
|---|---|---|---|---|---|
| **ABSENT** (link deleted, hinge kept) | **0** | **0.34586382** | 0.307739631 | **exactly 0** | **none** |
| ×0.03125 | 2.02205882 | 0.094866631 | 0.287223651 | 0.177728702 | 1.11285331 |
| ×0.125 | 8.08823529 | 0.075833409 | 0.29391121 | 0.220497632 | 1.15314243 |
| ×0.5 | 32.3529412 | 0.0673168772 | 0.297459536 | 0.240528676 | 1.17233108 |
| **×1 — the derived value** | **64.7058824** | **0.0655626469** | 0.298217671 | 0.247473281 | **1.17637685** |
| ×2 | 129.411765 | 0.0637725927 | 0.298620229 | 0.251162501 | 1.17852904 |
| ×8 | 517.647059 | 0.0625251952 | 0.298933652 | 0.254032433 | 1.18020635 |
| ×32 | 2070.58824 | 0.0622261953 | 0.299013558 | 0.254764543 | 1.18063466 |
| ×128 | 8282.35294 | 0.062151211 | 0.299033626 | 0.254948364 | 1.18074231 |
| **RIGID** — `C-0009`'s penalty | 10000 | **0.0621469105** | 0.299034765 | 0.254959195 | 1.18074849 |

**Monotone at 7 of 7 consecutive pairs** (`F3` did not fire), and the stiff end converges onto the
penalty to `4.30e−6` of the stroke — so `1e4 pN/nm` *is* a rigid limit, which is the half of
`C-0009`'s justification that was checkable and is now checked.

## 4. The four verdicts, all fixed in `T-9`'s Plan before the sweep

| | predicate | reading | fires? |
|---|---|---|---|
| **V1** | `D_phys` crosses `T-5b`'s 0.10 | **0.0655626469** — 34.4373531 % of the convention still unused, against 37.8530895 % at the rigid link | **NO** |
| **V2** | `\|D_phys − D_rigid\|` exceeds `C-0099`'s whole row-end unknown | **0.00341573646** against **0.00302847490** — **1.12787346×** | **YES** (corrected threshold) / **no** (as first written) |
| **V3** | the peak per-crossover vertical force moves more than 19 % | **2.93612244 %** | **NO** |
| **V4** | the **ramp fraction** `R = (D_phys − D_rigid)/(D_absent − D_rigid)` exceeds 0.05 | **0.0120392417** | **NO** |

**So the binary reading of the constraint is right on three of four, and the one it fails is the
one that measures size rather than shape.** At the crossover's own derived displacement stiffness
the vertical link behaves as a *constraint*: it carries **1.2 %** of the whole present-versus-absent
movement, and the residual is not zero.

### The `V2` threshold was mis-transcribed, and both readings are published

`T-9`'s Plan registered `V2` as *"three percentage points of the convention"* and wrote the number
**`0.030`**. Three percentage points *of* `0.10` is **`0.0030`**, and `C-0099`'s own two emitted
readings differ by exactly `0.0651753854 − 0.0621469105 = 0.0030284749` of the stroke.
The registered number was a factor of **9.906×** out against the quantity its own sentence names.

The correction is published rather than applied silently:
the mis-transcribed value is retained in code as `ROW_END_UNKNOWN_MARGIN_AS_FIRST_WRITTEN`,
**both** verdicts are emitted, the registered wording is a `~~strikethrough~~` in the task file,
and the pre-registration is a **git commit one earlier than the result**, not an assertion.
`V1`, `V3` and `V4` read the same under both.

## 5. What the vertical link is worth, in the units the corpus argues in

- **It is worth `3.42` percentage points of the flatness convention** — `37.8530895 % → 34.4373531 %`
  of `T-5b` unused — against the **`3.03`** points `C-0099` measured the *entire* row-end hinge
  unknown to be worth, i.e. **`1.12787346×`** of it; and like it, **it does not cross**.
- **The peak link extension is a LENGTH, and it is far below the lattice quantum.**
  `2.54959195e−5 nm` at the penalty and `3.82458707e−3 nm` at the derived value, against the
  **0.34 nm** rise — the smallest length the design language can draw, and `CLAUDE.md`'s own
  *"a margin below 0.34 nm cannot be corrected, only removed"*. A tie whose extension is 88.9×
  below one base pair is a constraint.
- **The flatness verdict is lost only below ~2 pN/nm**, i.e. below `k_θ/d² = 1.86971045` — the
  hinge's *own* equivalent vertical stiffness. At `2.02205882 pN/nm` the tile reads **0.094866631**,
  95 % of the convention and still inside it; the crossing is below that rung. The derived value
  is **34.6×** above the hinge equivalent, so the constraint has a **32×** reserve in the one
  place where losing it would matter.

## 6. `C-0015`'s design rule: it survives, and the reason is sharper than a small number

`C-0015`'s governing variable is *"the distance from the attachment to the nearest crossover"*,
and that curve exists only because a rigid vertical tie **localises the reaction there**.
Re-measured at every rung as the spread of the peak crossover force over one registration cell at
`C-0015`'s own nine stations:

| | lever |
|---|---|
| rigid | **1.18074849** |
| the derived value | **1.17637685** — `0.370243116 %` softer |
| the link **deleted** | **none — every crossover carries EXACTLY zero vertical force** |

The zero is a symmetry statement, not a small number: the vertical link is the **only** element of
a crossover that transmits a transverse **force** between two duplexes; the dihedral spring
transmits a **moment**. So deleting it does not flatten `C-0015`'s lever — it **removes the
quantity the lever is a ratio of**.

**The design rule therefore rests entirely on the vertical link's existence and almost not at all
on its stiffness**, which is `C-0100`'s *"the only two physical states of a constraint are present
and absent"* restated on the observable that made the question urgent.

And the absent state is not a degraded design, it is a broken one: **0.34586382** of the stroke,
**5.56526169×** the rigid reading, **3.4586382×** past `T-5b`, and **worse than the uncoupled tile's own
0.307739631** — `CLAUDE.md`'s *"an attachment coupling can be a NET DISHING SOURCE"*, reached by
softening the sheet rather than by sparsening the coupling.

## 7. The falsifiers

| | statement | fired | reading |
|---|---|---|---|
| **F1** | the rigid rung does not reproduce `C-0090`'s published 34-root dishing | **NO** | departure **exactly `0.0`** against `0.0621469105` |
| **F2** | a uniform load on the free lattice dishes materially at some rung | **NO** | worst `2.61918117e−7` of the stroke over all ten rungs including the absent one — `CLAUDE.md`'s standing falsifier |
| **F3** | the response is not monotone in `k_z` | **NO** | monotone at 7 of 7 |
| **F4** | the response is flat over the whole four decades, so the sweep measures nothing | **NO** | `0.094866631` at the soft end against `0.062151211` at the stiff one |
| **F5** | the derived value falls outside `C-0099`'s unresolved bracket | **NO** | `0.00647058824` against `0.015625` |

## 8. Validity range

- **THE SWEPT VALUE IS A CONSTRUCTION, NOT A MEASUREMENT.** `2αS/(100a)` is Chen et al.'s
  softened-bond heuristic on the duplex stretch modulus, which `C-0020` already applies to the
  in-plane axis and already sweeps four decades **because nothing in the accessible literature
  gives it in any form**. Nothing here measures a crossover's vertical stiffness.
- **THE CONSTRUCTION'S AXIS IS AN ASSUMPTION.** The in-plane connector resists sliding *along* the
  helices; the vertical link resists an offset *normal* to the sheet. Both are relative
  displacements of the same two duplexes carried by the same two phosphate bonds, and that is the
  whole argument for the transfer. The four-decade sweep is what it is for, and every verdict above
  is quoted with the rung it is read at.
- **`V3`'s 19 % is `C-0009`'s count effect, and `CH-0014` disputes it downward** to 0.3–3.4 % with
  the opposite sign. So `V3` is the **loose** end of the criterion, and the registration lever —
  `0.370243116 %`, measured here — is the sharper instrument. Both are reported.
- **THE LATTICE IS SINGLE-LAYER SQUARE.** Every number is a property of `C-0009`'s grillage, whose
  crossover combinatorics are square-lattice and whose interfaces are a path graph; none of it
  transfers to a honeycomb block (`C-0154`).
- **THIS LATTICE IS NOT `C-0157`'s LATTICE, AND THE DIFFERENCE IS ONE 8 bp PLANE.**
  At `C-0086`'s 38.08 nm and phase 8, `rasterColumnLayout` puts its columns on the **even**
  junction planes and gives **8** columns with the row end admitted (a 4/4 parity split over 14
  interfaces, **56** crossovers) and **6** with it refused (3/3, **42**) — `C-0099` asserts both
  counts and both are reproduced here at departure `0.0` and `0.14` against 49.
  `C-0157`'s oxDNA design generator built **7** columns, at `x = 8 + 16k`, `k = 0…6`, and **49**
  crossovers.
  **Neither grillage reading is 7**, so the simulated tile and the tile every placement result in
  this corpus is graded on are **not the same object**: their column sets are one 8 bp plane apart,
  which is `CLAUDE.md`'s own *"a shift by one column pitch … hands every interface the OTHER
  parity's columns — a physically different sheet"*, met between a simulation and the lattice its
  answer would be read against.
  **This claim does not resolve which is right** — `C-0134`/`CH-0161` settled that at 38.08 nm the
  row-end column is a lattice point at phases 8 and 24 and that `EDGE_MARGIN` decides it, and
  `C-0157`'s generator is not that construction. It is recorded here, with both counts emitted,
  because it decides which threshold a measured `k_z` would be read against, and it is listed
  under *Still open*.
- **ONE OPERATING STATE**, `C-0022`'s solved collar at 2 mM / 10 nm / 0.192 V — the state `C-0090`
  and `C-0099` are read at. A flatness verdict needs an operating state as well as a load case.
- **THE DISHING IS A PEAK OVER A BEST-FIT PLANE**, minimised over `C-0090`'s centro-symmetric
  family with `C-0090`'s own tie-break. It is not an energy, so monotonicity in the swept variable
  is **measured** and not assumed.
- **A DEAD BAND IS NOT REPRESENTABLE HERE.** `CLAUDE.md` records that a flexible link pays its
  axial slack back as a transverse dead band of the same size; a linear `k_z` cannot express that,
  and a trajectory would show it directly as a **non-Gaussian** offset distribution. That is a
  further reason the frames matter, and it is listed as open rather than bounded.

## 9. Still open

1. **The measurement itself** — the vertical offset's variance at a crossover, from an oxDNA run
   that retains its frames. Priced in §1: it is the run that already exists, plus an estimator.
2. **`T-9`'s THIRD deliverable, the in-plane shear `k_s`**, is untouched — and it is the **same
   constant** as the one transferred here, so one measurement settles both axes *only if the
   transfer in §8 is right*. `C-0028` shows `k_s` moves a buckling verdict.
3. **Whether the restraint is a spring at all**, or a dead band and then rigid (§8).
4. **Which lattice `C-0157`'s oxDNA tile is** — 7 columns and 49 crossovers, against the 8/56 and
   6/42 `rasterColumnLayout` gives at the same width and phase (§8). One 8 bp plane, and it decides
   which threshold a measured `k_z` is read against.
5. **Where the flatness crossing actually is.** It is below `2.02205882 pN/nm` and this run did not
   bisect for it; one more rung ladder would give it, and the answer only matters if a measurement
   comes back below the hinge's own equivalent.
