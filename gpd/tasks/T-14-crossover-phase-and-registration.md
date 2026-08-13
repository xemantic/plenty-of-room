# T-14 — Crossover phase and anchor registration as a design variable

| | |
|---|---|
| **Leaves** | `A8.2` (structural rigidity / mode analysis — *"identify the dominant compliance term … and budget stiffness at the joints"*, `../../../simulation-task-map/knowledge/program_tasks_feynman_path.csv`) |
| **Problem definition** | §6 tasks 5 and 5b; questions §4(f) and §4(g); parameters §3 |
| **Verification type** | in-silico (the `T-10` beam-and-hinge grillage, re-parameterised by the staple layout's own free variables and swept completely over both) |
| **Maturity** | TRL 1–3. Model-consistent and traceable. **Not measured.** |
| **Status** | Executed, verified, filed as claim [`C-0015`](../claims/C-0015-crossover-phase-and-registration.md) |
| **Raised by** | [`C-0009`](../claims/C-0009-discrete-lattice-tile.md), which found the lever while answering a different question and recorded that **nothing in the programme owns it** |

---

## Formulate

### Why this task exists

`C-0009` reports, in its convergence section and again in its anchor-phase section, two numbers it had no charter to pursue:

> **The crossover count is not a convergence parameter and does not converge** — it is a physical property of the design, and moving from 7 to 8 columns moves the peak crossover force by 19 %.

> Where the *anchor* sits within the unit cell is worth another 30 %: 5.11 pN on a crossover, 5.56 pN between duplexes, 5.76 pN mid-span on a duplex axis, **6.66 pN on a duplex axis at a crossover column**.

Both are decided by the staple layout, at no cost in material, force or stroke.
They matter because `C-0009` also found the peak per-crossover force at a discrete anchor is **5.63 pN nominal and 11.54 pN worst-case**, and the worst case **reaches the 10–15 pN single-duplex unzip allowable**.
A lever of tens of per cent on a quantity sitting within a factor of two of an allowable is a design result.

`C-0009` further reports that the lattice's flatness-versus-attachment-count curve is **non-monotone** — worse at 121 attachments than at 100, worse at 196 than at 169 —
so *where* attachments land relative to the crossovers matters as much as how many there are.
That is the same phenomenon and it belongs here.

### The design variable, restated correctly before anything is computed

`C-0009` varied a **count** of crossover columns. A count is not what a staple layout chooses.

The column lattice has a fixed pitch: crossovers recur every 16 bp along a helix but **alternate between its two neighbours**, so one interface is linked every `p = 32 bp = 10.88 nm` and the columns — counting both parities — sit at `p/2 = 5.44 nm`.
What a layout chooses is the **phase** of that lattice relative to the tile edge.
The count is a *consequence*: a 40 nm tile spans `40/5.44 = 7.35` column pitches, so a phase that lets eight columns fit gives eight and a phase that pushes one off the edge gives seven.

Two things follow, and both are load-bearing.

1. **The phase is quantised to base pairs.** A staple can only cross over at a base pair, so the phase takes exactly `32` values, not a continuum. A *complete* sweep is therefore possible, and this task performs one rather than sampling.
2. **The period of the phase is `p = 32 bp`, not `p/2 = 16 bp`.** Shifting the column lattice by one *column* pitch leaves the set of column positions inside the footprint unchanged — so the geometry looks identical — but hands every interface the **other parity's** columns, which is a physically different sheet. A sweep over `[0, p/2)` covers half the design space. This is the exact analogue of the per-helix / per-interface confusion that `CLAUDE.md` records as doubling the across-helix rigidity, and it is asserted as a test rather than assumed.

The **registration** of an attachment is its position within the one-crossover unit cell.
The crossovers form a *centred* rectangular lattice — primitive vectors `(p/2, d)` and `(p/2, −d)` — whose primitive cell has area `p·d = 29.27 nm²` and holds exactly one crossover.
Sweeping an attachment over a `p × d` rectangle is therefore a **complete** registration sweep, not a sample of one; `C-0009` could afford four points in it.

### The question, as a numeric target

Produce, on the `T-10` lattice and with `C-0009`'s ingredients unchanged:

1. the **peak per-load-path force** as a complete function of column phase and attachment registration, for both load classes `C-0009` separates (load *reacted* at a discrete anchor, load *entering* at a concentrated attachment), with the best and worst layouts named and their margins to the 10–15 pN unzip, ~48 pN shear and 65 pN nicked-ceiling allowables;
2. whether **registration alone** can keep the worst case clear of the unzip allowable across `C-0009`'s `k_f ×[0.25, 4]` sweep and Chen et al.'s admissible `α ∈ [0.6, 1.2]`;
3. the **explanation** of the non-monotone flatness curve, as a commensurability computed independently of any solve, with the good commensurabilities named as a design rule;
4. whether the **two levers compose** or trade.

### Acceptance predicate

Discharged when all six hold.

1. The peak per-load-path force is computed on a **complete** sweep of the crossover column phase — all **32** base-pair phases of the 32 bp per-interface period — and of the attachment registration over the **whole** `p × d` one-crossover unit cell, for both load classes, with the best and worst layouts named by phase in base pairs and registration in nm and the margin of each to the three per-path allowables reported.
2. The **period** of the phase variable is established from the model rather than assumed, and shown to be `p = 32 bp` and not `p/2 = 16 bp` — as an exact identity on the layout, not as a tolerance. Likewise the **symmetry group** of each phase is derived rather than inherited: `C-0009`'s centro-symmetry is a property of *its* eight-column lattice, not of the sheet.
3. Whether registration can keep the worst case clear of the unzip allowable is answered **yes or no with a number**, over the `k_f ×[0.25, 4]` sweep, over `α ∈ [0.6, 1.2]`, and at the out-of-range isotropic probe `CH-0005` names.
4. The **ranking** of layouts — which phase is best, which registration is worst — is reported as a function of `k_f` and of `k_θ`, and either shown invariant, which makes the *rule* a lattice-geometry result immune to `T-1c` and `T-9`, or reported as depending on them. The **forces** are reported per state regardless, because `C-0009` records that its forces, unlike its ratios, do move with `k_θ`.
5. The non-monotone flatness curve is **reproduced and explained** by a commensurability metric that is pure arithmetic in the attachment row count and the duplex count, the metric is shown to predict `C-0009`'s named anomalies, and the rule is tested **causally** by transposing attachment grids at fixed attachment count and fixed force per attachment. The continuum plate, which has no duplex lattice, is run as the control.
6. The two levers are composed: the registration span is reported at the best and at the worst phase and the joint extremes named, so that "compose or trade" is answered with a number.

### Units, locked

SI, scaled, per `P-2`: lengths nm, forces pN, moments and energies pN·nm, pressures pN/nm² (`= 1 MPa` exactly),
stiffness pN/nm (`= 1 mN/m` exactly), flexural rigidity pN·nm, foundation stiffness per unit area pN/nm³,
hinge stiffness pN·nm/rad. **Phase in base pairs**, and in nm alongside.
`k_BT = 4.142 pN·nm` at **T = 300 K**, medium **aqueous buffer with Mg²⁺**.

### Geometry and sign conventions, fixed before deriving

Restated rather than inherited, per §5 of the problem definition.

- `x` runs **along** the DNA helices, `y` **across** them, `z` normal to the electrode; the origin of `(x, y)` is at the centre of the footprint.
- The deflection `w(x, y)` is measured **downward**, positive when the polymer layer is compressed.
- A crossover's transmitted force is signed so that positive is transmitted from the far side of its interface toward the near one — the sign of the cut-equilibrium shear it must equal (`T-10`).
- **Phase.** The column lattice is `x = φ + k·(p/2)`, `k ∈ ℤ`, truncated to the columns strictly inside the footprint. `φ` is quoted in base pairs from the tile centre and has period `p`. Column `k` serves the interfaces of parity `k mod 2` — the parity of the index in the *infinite* lattice, so that a column leaving the footprint changes the count without changing which interface any surviving column serves.
- **Registration.** `alongCell` is the attachment's `x` measured from the tile centre over one full `p`; `acrossCell` is its `y` measured from a duplex axis over one full `d`. The two ends of the across range, `±d/2`, are the two sides of the same crossover line and are carried as a built-in consistency check.
- **Footprint.** 40 × 40.35 nm, 15 duplexes — `T-10`'s, unchanged, so nothing here is a footprint effect.

### What is deliberately excluded

- **No staple layout is invented.** Whether a Gen-1 tile has seven or eight crossover columns and at what phase is a property of a design nobody in this programme has. What is produced is the **sensitivity** and the **rule**; a reported "best phase" is a statement about the lattice, not a claim about the Gen-1 tile.
- **No oxDNA.** `k_θ` is swept, not derived; that is `T-9`, costed at days, and it needs the coordinator's go-ahead. The crossover's **vertical/axial** compliance stays a rigid constraint, inherited from `C-0009` and named in the validity range as the one assumption under it with nothing cited behind it.
- **No electrostatics.** The load enters as a uniform 100 pN or as a concentrated 100 pN, exactly as in `T-10`; `T-3` owns the load model and `C-0009` showed the lattice response linear in the taper depth.
- **No new elasticity.** Every physical ingredient is `C-0009`'s, so that any difference reported here is the layout and nothing else.

---

## Plan

### The cheap bound, run first — and it decides what the answer can be made of

Before any lattice is assembled, the phase table is pure arithmetic: for each of the 32 base-pair phases, the column count, the crossover count, the parity split, the gap between the outermost column and the tile edge, and the symmetry group from `n + N` parity.

This settles in advance **how much of any phase effect can possibly be a rigidity effect**.
The phase can move the crossover count by at most one column's worth out of 56, so `D_⊥` — which the lattice carries as `k_θ` per crossover — moves by at most a few per cent.
Any phase effect larger than that is therefore **load-path topology and not the sheet getting softer**, and that is a conclusion available for the price of counting.

The commensurability metric is arithmetic too. The attachment grid `insetGrid` lays down puts row `j` at `y_j = −L_y/2 + L_y(j + ½)/s`, and with `L_y = N d` exactly, the offset of that row from the nearest duplex axis is the distance of `N(j + ½)/s − ½` from the nearest integer — a function of `s` and `N` alone. No elasticity, no foundation stiffness, no solve. **That is what makes the explanation of the flatness curve immune to `T-1c` and `T-9`.**

### The expensive calculation, and why it is affordable at all

**Chosen: the `T-10` grillage, unchanged, with the anchor applied as a rank-one (Sherman-Morrison) update instead of an assembled term.**

An anchor enters the stiffness as `k_a b bᵀ` with `b` the nodal basis vector of its attachment point — exactly rank one — so

&nbsp;&nbsp;&nbsp;&nbsp;`(K + k_a b bᵀ)⁻¹ f = K⁻¹f − k_a (bᵀK⁻¹f)(K⁻¹b)/(1 + k_a bᵀK⁻¹b)`,

which is algebraically identical to assembling the anchor and re-factorising, and is asserted against it as a test to `1e−9`.

This is what makes the question askable. A registration map is a two-dimensional sweep; re-factorising an 855-degree-of-freedom lattice at every point costs `O(n³)` each, while the update costs two triangular solves, `O(n²)`. A complete 288-point registration cell therefore costs roughly what **one** assembled anchored case cost `T-10`. Sampling the cell at four points was `C-0009`'s only affordable option, and replacing that sample by a sweep is the deliverable.

| method | cost | why not |
|---|---|---|
| the phase table and the commensurability arithmetic | milliseconds | run first; they bound the rigidity channel and explain the flatness curve, but they cannot produce a force |
| **`T-10` grillage + rank-one anchor updates** | ~4 minutes for the whole study | **chosen** |
| `T-10` grillage with each anchor assembled | ~40 minutes, and it grows as `n³` | the same answer at ten times the cost; it is what `C-0009` could afford four points of |
| refine the continuum plate | seconds | the plate has no crossovers, no duplex lattice and no unit cell — **registration does not exist in it**. It is run here only as the *control* for the commensurability claim |
| oxDNA / CanDo | days | `T-9`'s territory, and CanDo's crossovers are rigid by its authors' statement, which removes the only across-helix compliance a single-layer sheet has |

Prefer published measurement on the actual material, per the research practice — and here, as in `T-10`, there is none: **no crossover force in a loaded origami sheet has ever been measured**, and the quantity under study is a load-path topology rather than a material constant. A lattice sweep is the cheap route and MD is the expensive one.

### What would falsify this approach

Stated in advance, per §5. The outcome of each is in Verify.

1. **The phase sweep coming out flat** — a peak-to-peak spread of a few per cent over all 32 phases. Then there is no lever, and `C-0009`'s 19 % was an artefact of comparing a seven-column lattice against an eight-column one that differ in more than the count.
2. **The phase having period `p/2`.** Then the parity analysis above is wrong and the design variable is half the size claimed.
3. **The best and worst layouts moving with `k_f` or `k_θ`.** Then the rule is not lattice geometry, and it cannot be designed for until `T-1c` and `T-9` land. (Would not make the answer wrong; it would make it conditional, and it must then be reported that way.)
4. **The registration map failing to be lattice-periodic** — translating an attachment by `(p, 0)` or by the centring vector `(p/2, d)` changing the peak force by as much as the registration itself does. Then what is being called registration is contaminated by position-in-tile and the cell is not the right variable.
5. **The commensurability metric failing to order the flatness anomalies**, or the transposed grids at fixed attachment count showing no difference. Then the non-monotonicity has some other cause and the design rule is not earned.
6. **The continuum plate showing the same commensurability structure.** The plate has no duplex lattice, so if it did, the effect is a property of the attachment grid alone and nothing about the sheet.
7. **The rank-one update disagreeing with an assembled anchor.** Then every number in the sweep is an artefact of the fast path.
8. **The extreme phases failing to converge in the mesh.** The phase that pushes a column to 0.28 nm from the tile edge makes the shortest beam element in the sweep, and it is the only place the discretisation could bite.

---

## Execute

Code: `src/main/kotlin/structure/` — `CrossoverLayout.kt` (the phase-parameterised column layout and the commensurability arithmetic),
`CrossoverRegistrationStudy.kt` (the study), and two additive changes to `OrigamiGrillage.kt`:
the column layout becomes a `CrossoverLayout` with the `T-10` count-based constructor retained verbatim as a secondary constructor,
and `solveWithAnchor` / `solveWithEachAnchor` add the rank-one anchor update.
Tests, written first: `src/test/kotlin/structure/CrossoverLayoutTest.kt`, 21 tests named for the gate they discharge.
`Cholesky.kt`, `OrigamiSheet.kt`, `PlateOnFoundation.kt`, `Gen1Tile.kt`, `LoadPaths.kt` and `ResultRounding.kt` are unchanged.

```shell
./gradlew test -PbuildDirectory=build-t14
./gradlew study -PbuildDirectory=build-t14 -Pstudy=structure.CrossoverRegistrationStudyKt
```

Result: [`../results/T-14-crossover-phase-and-registration.json`](../results/T-14-crossover-phase-and-registration.json), deterministic in filename **and** content —
re-run and diffed to confirm, with `ResultRounding.kt` applied at the serialisation boundary.

32 base-pair phases × a 32 × 9 = 288-point registration cell at the design point;
32 phases × a 8 × 5 = 40-point cell at each of 5 foundation stiffnesses, 4 hinge stiffnesses and 4 `(k_f, k_θ)` corners;
14 square attachment grids and 7 transposed pairs on both models; 3 axis-versus-interface pairs;
6 lattice-periodicity translations; 6 mesh-convergence points.
Whole study: **~4 minutes**.

---

## Verify

### The cheap bound, and what it decided in advance

The phase table costs nothing and it bounds the rigidity channel before any lattice is assembled:

| | |
|---|---|
| phases | **32**, of which **10** hold eight columns and **22** hold seven |
| crossovers | 56 at eight columns, **49** at seven |
| `D_⊥` change | exactly `49/56 = 0.875` — **12.5 %** |
| shortest edge element in the sweep | 0.28 nm, at base pairs 6, 10, 22 and 26 |
| symmetry | centro-symmetric at **10** phases, **trivial at 22** |

So the phase can move the across-helix rigidity by at most 12.5 %, and the observed force effect at fixed registration is 4.5–9.0 % — *smaller* than the rigidity channel. **The phase is not a load-path-topology lever; it is a rigidity lever, and a small one.** The large lever is registration, which changes no rigidity at all. That inversion of the expected answer is what the cheap bound bought.

### The five gates

#### Gate 1 — dimensional consistency

- The centred layout reproduces `T-10`'s column construction exactly, positions to `1e−12` and parities identically, so nothing already published moves.
- The nominal `T-10` lattice **is** the phase at half a column pitch — asserted, not assumed.
- `BASE_PAIRS_PER_PERIOD × 0.34 nm` is exactly the per-interface crossover spacing.
- A 40 nm tile holds 7 or 8 columns at every one of the 32 phases, and both occur.
- **The rank-one anchor update equals a re-assembled anchored lattice** — deflection, peak crossover force, peak duplex shear and anchor force, all to `1e−12`. The batched registration map equals mapping the single-anchor solve to the same tolerance. Without this the entire sweep is an artefact of the fast path.
- The attachment offsets are pure arithmetic: the spread vanishes for row counts 1, 3, 5, 6, 10, 15 over 15 duplexes, and the distinct-offset count is `s/gcd(s, N)` — asserted at `s` = 10, 11, 12, 14, 15.

#### Gate 2 — limiting cases

| statement | outcome |
|---|---|
| the phase has period `p`, not `p/2` | **exact**: `φ + p` reproduces positions to `1e−9` **and** parities identically; `φ + p/2` reproduces the positions and **inverts every parity** |
| a phased lattice still costs `½ k_θ (κd)²` per crossover under `w = ½κy²` | exact to `1e−9` at four phases |
| a uniform load dishes a phased lattice | **zero** (`< 1e−9 nm`) at four phases, and a runtime `check` in the study |
| a rigid anchor takes the whole load off the foundation | 99.9 % |
| rows on the duplex axes beat an incommensurate row count of equal size | **×6.6** with the plate divided out; the equal-registration control gives ×1.84 |

#### Gate 3 — symmetry and conservation

- **Centro-symmetry is derived, not inherited.** It holds exactly when `n + N` is odd: residual `< 1e−8` at eight columns, `> 1e−3` at seven. `C-0009` asserted it as a property of the sheet; it is a property of the phase. → `CH-0014`, ground 2.
- Force balance under a rank-one anchor: foundation + anchor = applied, to `1e−8`.
- **The registration variable is lattice-periodic** — the test that decides whether the unit cell is the right variable at all:

| translation | residual | note |
|---|---|---|
| `(0, 2d)` — two duplexes across | **0.33 – 1.89 %** | a clean lattice vector at this tile size |
| `(p/2, d)` — the centring vector | 0.09 – 8.82 % | |
| `(p, 0)` — one per-interface spacing along the helices | **4.65 – 17.70 %** | `p = 10.88 nm` is **27 %** of the 40 nm footprint |

&nbsp;&nbsp;&nbsp;&nbsp;**A 40 nm tile is only 3.7 unit cells wide along the helices, so at this size "registration" and "position in tile" are not cleanly separable in `x`.** Reported, not assumed away; only the across-helix vector is asserted as a test.

#### Gate 4 — numerical convergence

| phase | edge gap | subdivisions 1 / 2 / 4, anchored peak crossover [pN] |
|---|---|---|
| bp 6 (**tightest**) | 0.280 nm | 7.0563 / 6.9867 / **6.9834** — 0.05 % |
| bp 0 (loosest) | 3.680 nm | 7.4127 / 7.4120 / **7.4120** — `1e−4` |

The tightest phase makes a beam element twenty times shorter than the rest of the mesh and it is still converged to 0.05 %. Nested refinements only (1 ⊂ 2 ⊂ 4), per the monotonicity caveat `T-10` recorded.

**Two reproducibility defects were found here, one fixed and one measured and reported.**

*Fixed.* Rounding at the serialisation boundary is not sufficient when a result file contains an **argmin**. The phase sweep is flat to under 0.5 % within a column count, two phases tied to the last unit in the last place at one layer state, and `minByOrNull` returned whichever the JIT's summation order favoured — so a re-run differed in one integer field while every number in the file was identical. The extremum selection is now made on the *rounded* value with the index as tie-break.

*Measured, not hidden.* Five independent runs of the whole study were compared. Two pairs came out **byte-identical** (28 254 of 28 254 fields); one triple agreed on **23 077 of 23 079**, the two exceptions both being `centredAnchorPeakCrossoverStiffAnchor` and both moving by **one unit in the ninth significant digit** — `1.4e−9` and `8.3e−9` relative. The residual is therefore intermittent rather than systematic. The cause is structural rather than incidental: a crossover force is a **penalty-derived constraint force**, `10⁴ pN/nm` multiplying a difference of two nearly equal nodal deflections, so catastrophic cancellation amplified by the penalty puts its floating-point noise floor at roughly `1e−11` relative — above the `1e−16` the nine-significant-digit rounding convention assumes. Lowering `RESULT_SIGNIFICANT_DIGITS` would have hidden it and would have re-written `T-5`, `T-5b`, `T-8` and `T-10`'s result files as a side effect, so it is reported instead. **No conclusion in this task turns on the ninth significant digit of anything.**

#### Gate 5 — literature cross-check and controls

- **`C-0009`'s four named anchor placements are reproduced to 0.01 pN** — 5.11 / 5.56 / 5.76 / 6.66 — wired in as a test. Its seven- and eight-column concentrated-lever forces are reproduced exactly, 44.146 / 37.139 pN.
- **The continuum plate is run as the control** for every commensurability statement. Its flatness curve is monotone at all fourteen square grids where the lattice's is not, which is what licenses attributing the non-monotonicity to the duplex lattice. Wired in as a test.
- The control also **refuted a rule**: "put attachments on the duplex axes, not on the crossover interfaces" costs ×1.84–2.09 on the lattice but ×1.70–2.10 on the plate, so the excess is 0.87–1.21 — it is the grid moving toward a free edge, not the lattice. Reported as refuted.
- Crossover topology from Rothemund 2006, interhelical distance from Fischer et al. 2016 (SAXS), duplex rigidities from CanDo, `k_θ` from Chen et al. 2014 — all as `C-0009` traced them, unchanged, so any difference here is the layout.

### The falsifiers, and whether they fired

| falsifier | fired? | outcome |
|---|---|---|
| 1. the phase sweep coming out flat | **no** | the joint span is ×1.43–1.60 at every layer state — but see 3: it is registration, not phase |
| 2. the phase having period `p/2` | **no** | period is `p`, asserted as an identity; a `p/2` shift inverts every interface parity |
| 3. the best and worst layouts moving with `k_f` or `k_θ` | **partially, and informatively** | the **coarse** ranking (7 columns best, 8 worst) is invariant across all thirteen states; the **base-pair phase within a count** does move, and is worth under 0.5 %, so it is not a design decision. The forces move and are quoted per state |
| 4. the registration map failing to be lattice-periodic | **partially** | 0.33–1.89 % across the helices, **4.65–17.70 % along** them. The cell is the right variable in `y` and only approximately so in `x` on a 3.7-cell-wide tile. Stated in the validity range |
| 5. the commensurability metric failing to order the anomalies | **no**, but it is not a complete ordering | the offset spread puts rows 7, 11 and 14 at the top, which orders all three anomalies `C-0009` named; it misorders rows 6 against rows 13. No single scalar was found that orders all thirteen row counts, and that is stated rather than hidden |
| 6. the plate showing the same commensurability structure | **no** | the plate's curve is monotone at all fourteen grids |
| 7. the rank-one update disagreeing with an assembled anchor | **no** | `1e−12` |
| 8. the extreme phases failing to converge | **no** | 0.05 % at the 0.28 nm edge gap |

### The predicate, item by item

1. complete 32-phase × 288-point registration sweep, both load classes, best and worst named with their placements and their margins to unzip / shear / the 65 pN ceiling — **yes**;
2. the period established as `p = 32 bp` by an identity, and the symmetry group derived per phase rather than inherited — **yes**, and it raises [`CH-0014`](../challenges/CH-0014-layout-sampled-not-swept.md);
3. **no, by 0.5 %**: the best layout brings the worst case from 14.647 pN to 10.049 pN, against a 10 pN unzip lower edge. Answered with a number over `k_f ×[0.25, 4]`, `α ∈ [0.6, 1.2]`, four corners and the isotropic probe — **yes**;
4. the coarse ranking shown invariant across every state and the forces reported per state — **yes**;
5. the non-monotone curve reproduced and explained, with the plate as the control, the arithmetic metric predicting all three named anomalies, and a causal transposition test at fixed count **with its own controls** — **yes**, and one candidate rule refuted by its control;
6. the levers composed: joint ×1.60 against a separable product of ×1.54, i.e. 104 % — **they compose, they do not trade** — **yes**.

## Result

Filed as [`C-0015`](../claims/C-0015-crossover-phase-and-registration.md),
which raises [`CH-0014`](../challenges/CH-0014-layout-sampled-not-swept.md) against `C-0009`.

## Feedback into Formulate

- **`T-2`'s topological constraint loosens by 30 %.** `C-0006` and `C-0009` put a constraint with no axis in the design window — flatness needing 55, then 64, attachment points against 43.7 patches and 56 crossovers. Over grid *shapes* it is **45**, i.e. **0.80 attachments per crossover**, and the "more attachments than crossovers" statement inverts.
- **A new design primitive for `T-5`/`T-12`/`T-13`: one attachment row per duplex.** It sets the peak per-load-path force to **exactly zero** by symmetry, which removes the §4(f) question from the output coupling entirely — for a uniform load. What it costs is 45 tethers instead of 4, and `T-12` (lateral confinement) and `T-13` (what holds the tile down) both need attachments anyway, so the three may share one scheme.
- **`T-9` gains a third deliverable.** `C-0009` re-scoped it to the force budget; this task shows the force budget's *largest* term is the attachment's distance to the nearest crossover, and the whole curve rests on the crossover being **rigid in `z`**. A compliant crossover flattens that curve and shrinks the lever. `T-9`'s vertical-compliance half is now the thing that decides whether this design rule exists.
- **The tile is 3.7 unit cells wide along the helices.** Nothing in the programme has said what that means for §3's "test tiles up to ~70 × 100 nm": on a 100 nm tile the along-helix registration would be a clean variable where here it is contaminated at the 5–18 % level. A layout rule derived on a 40 nm tile may not transfer.
- **A rule was refuted by its own control**, and the control cost one plate solve per point. Every commensurability statement in this project should carry the continuum's answer beside it for exactly that reason.
