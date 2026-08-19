# C-0050 — §3's desired ~10 nm stroke is out of reach and its acceptable 3 nm is not, and the bound that settles it contains **no coupling at all**: the stroke is `L₀ − h`, so it is bounded by the layer's own resting height, and §3 names no layer taller than 10 nm — the best kinematic ceiling anywhere in the sweep is **9.790 nm**, the best inside `C-0002`'s validity range **8.959 nm**, and the best under §3's own 100 pN **7.424 nm**

| | |
|---|---|
| **Task** | [`T-108`](../tasks/T-108-desired-stroke-reach.md) |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with **`A2.2`** for the stroke the actuator itself can deliver |
| **Verification type** | **logical** (a kinematic identity no model can move) **+ in-silico** (`C-0003`'s six layer models re-run for three stroke ceilings over 66 states, and every element of the coupling catalogue re-run from its owning claim's library against eight predicates at both of §3's strokes — 28 rows) |
| **Verdict** | **PASS, and the answer is NO — stated plainly, as §7 asks.** The stroke is `s = L₀ − h`, so **`s < L₀` identically**, and §3 names three layer heights of which the tallest is 10 nm: **a 10 nm stroke on a 10 nm layer IS the statement `h = 0`.** Three ceilings follow, each strictly below the last and **none of them containing a coupling**: the **kinematic** one `L₀ − Nσv₀` reaches at most **9.790 nm** over the whole 66-state sweep; `C-0002`'s **validity** ceiling `L₀ − Nσv₀/0.2` — which is `C-0018`'s *own* binding bias ceiling at the 10 nm layer — at most **8.959 nm**; and the **dead-load** stroke at §3's 100 pN at most **7.424 nm**, 3.828–6.013 nm at the nominal 10 nm design point. And a coupling can only make it worse: `C-0017`'s own gate-2 theorem, re-derived here, is that the delivered stroke is **monotone decreasing** in the coupling stiffness, so the free stroke is the supremum over **every** coupling that could ever be designed — which is what lets one bound cover a catalogue rather than a search. The catalogue confirms it from the other side: **0 of 14 element rows clear every predicate at the desired stroke, and 3 of 14 clear them all at the acceptable one** (`E3a` at 15 paths, `C-0023`'s linear `E5`, and `E5a1` — one crossover per flexure). The most telling row is `E5`, which clears every *coupling*-side predicate at 10 nm and fails on the stroke: **the binding constraint is not the coupling.** What is established is **"unreachable on §3's own stack"** — stronger than *"unreachable with this catalogue"*, weaker than *"unreachable in physics"* — and the escape is priced: the layer would have to be **16.6–26.1 nm** tall to deliver 10 nm at 100 pN. **§6 task 3 is unaffected**: its predicate is the ≥ 3 nm one, and it is delivered. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED**, no layer has been grafted and no coupling has been built. The kinematic bound is the one statement here that is a **theorem about the coordinate** rather than a model result. |
| **Provenance** | `gpd/results/T-108-desired-stroke-reach.json`, produced by `synthesis.DesiredStrokeReachStudyKt`; **4 reach bounds, 66 reach records, 28 catalogue rows, 2 ceiling readings, 2 stability-range records, 9 escape records, 15 upstream reproductions, 3 convergence records**; **27 gate-named tests in `synthesis/DesiredStrokeReachTest`**, `tools/verify.sh` **BUILD SUCCESSFUL** on its own isolated tree with one concurrent agent's mid-TDD main source dropped by `--drop-file` (`src/main/kotlin/coupling/SingleColumnFlatnessStudy.kt`, `T-101`); the result file re-run through `tools/study.sh` and reported *"no result file changed"*, and diffed byte-for-byte identical |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂** (0.5 mM where a floor is read); 40 × 40 nm tile, footprint 1600 nm²; §3's layer heights **5 / 7 / 10 nm** at `σ` = 0.092 / 0.045 / 0.024 nm⁻², plus `C-0027`'s whole 10 nm window `σ ∈ [0.0116, 0.2885]`; all six `C-0003` models; §3's 100 pN at the **acceptable** 3 nm and the **desired** 10 nm; path counts 45 and 15 |
| **Consumes** | [`C-0003`](C-0003-crossover-valid-layer-response.md) (the six layer models, `chainLengthForHeight`, `load`, `equilibriumHeight` — **re-run as libraries**), [`C-0002`](C-0002-peg-material-parameters.md) (the `φ = 0.2` concentrated crossover, the virial coefficients), [`C-0001`](C-0001-layer-stiffness.md) (`layerDesignPoint`, the dead-load stroke, and its *"the reason to go outside 5–10 nm is upward"*), [`C-0018`](C-0018-maximum-usable-bias.md) (the stroke ceiling and the binding validity bias, reproduced), [`C-0017`](C-0017-output-coupling-stiffness.md) (the mandate, the stability floors, the stroke-monotonicity theorem, `firstOperatingStroke`), [`C-0023`](C-0023-two-sided-coupling.md) (the element catalogue and the declared ceiling), [`C-0030`](C-0030-coupled-standoff-joint.md), [`C-0039`](C-0039-two-spring-elastica.md), [`C-0040`](C-0040-hinge-line-census.md), [`C-0041`](C-0041-flexure-array-packing.md), [`C-0006`](C-0006-tile-load-distribution-and-flatness.md)/[`CH-0029`](../challenges/CH-0029-the-48-pn-allowable-is-a-30-bp-number.md), [`C-0049`](C-0049-compliance-ceiling-stroke.md) (**the stroke each requirement is owed at**), and [`C-0046`](C-0046-fewer-longer-flexures.md)/[`CH-0059`](../challenges/CH-0059-the-desired-stroke-placement-is-below-the-stability-floor.md) (concurrent, and **corroborated independently**) |
| **Raises** | [`CH-0062`](../challenges/CH-0062-the-buildable-hinge-count-clears-the-ceiling-on-the-elastica.md) against `C-0040` (filed with [`C-0049`](C-0049-compliance-ceiling-stroke.md)) |

---

## The claim, in one line

**Five claims searched the coupling catalogue for §3's desired stroke and none of them checked the coordinate: the stroke is the layer's compression, `L₀ − h`, so it is bounded above by `L₀`, and §3's own parameter table caps `L₀` at 10 nm — so the desired clause asks a 10 nm layer to be crushed to zero thickness, and the actuator refuses it 1.02× on geometry, 1.12× on the layer's own validity range and 1.35× under §3's own load, before any coupling exists.**

---

## The four bounds, cheapest first, and the first one is free

| | bound | best value over the whole sweep | short by | contains a coupling? |
|---|---|---|---|---|
| **1** | **`s = L₀ − h < L₀ ≤ 10 nm`** — an identity | **10.000 nm**, never attained | **1.000×**, asymptotically | **no** |
| **2** | **kinematic**: `L₀ − Nσv₀`, the dry-thickness floor | **9.790 nm** | **1.021×** | **no** |
| **3** | **validity**: `L₀ − Nσv₀/0.2`, `C-0002`'s crossover | **8.959 nm** | **1.116×** | **no** |
| **4** | **dead load**: the stroke at which the layer alone carries §3's 100 pN | **7.424 nm** | **1.347×** | **no** |

Bound 1 costs nothing and settles the question; bounds 2–4 say by how much, and where.
**Bound 3 is not an extra assumption smuggled in** — it is `C-0018`'s *own* binding bias ceiling at the 10 nm layer, at 6 of 6 layer models and at every buffer, recorded there as *"concentrated crossover (`C-0002`, phi = 0.2)"*.
Beyond it the des Cloizeaux exponent, the salt partitioning and the layer's whole equation of state are extrapolations.

At §3's three nominal design points, over `C-0003`'s six models:

| `L₀` | `σ` | `Nσv₀` | kinematic ceiling | validity ceiling | **dead-load stroke at 100 pN** | reaches 3 nm | reaches 10 nm |
|---|---|---|---|---|---|---|---|
| **5 nm** | 0.092 | 0.372–0.664 | 4.329–4.624 | 1.680–3.138 | **0.473–1.530** | **no** | **no** |
| **7 nm** | 0.045 | 0.336–0.577 | 6.417–6.661 | 4.114–5.322 | **1.537–3.197** | 1 of 6 | **no** |
| **10 nm** | 0.024 | 0.326–0.542 | **9.452–9.671** | **7.289–8.371** | **3.828–6.013** | **6 of 6** | **no** |

> The 10 nm row is `C-0016`'s and `C-0027`'s standing headline — *"§3's desired ~10 nm stroke: unreachable at every height and every grafting density"* — given its **mechanism** for the first time.
> It is not that the layer is too stiff or the field too weak. **It is that the stroke is the layer's own thickness, and §3 did not specify enough of it.**

Swept over `C-0027`'s entire surviving 10 nm window (`σ ∈ [0.0116, 0.2885]`, eight points × six models), the *softest* admissible layer reaches a **validity** ceiling of 8.959 nm and a dead-load stroke of 7.424 nm.
At the top of that window the layer already sits **past** `φ = 0.2` at zero compression, so its validity ceiling does not exist at all — recorded as `null` rather than invented.

---

## Why one bound covers the whole catalogue: the free stroke is the supremum

`C-0017` proves, as its own gate 2, that the **delivered stroke is monotone decreasing in the coupling stiffness** — a coupling converts stroke into force, and it never creates stroke.
Re-derived here over six stiffnesses spanning 300× on an explicit characteristic: the strokes fall monotonically and every one is below the free stroke.

> **So the free stroke bounds every coupled stroke, and the bounds above are free strokes.**
> No element of the catalogue, and no element anybody could design, can exceed them.
> **`T-108` is therefore not a search over couplings**, and that is why the answer is a claim rather than a sweep that ran out of candidates.

---

## The catalogue, all the same, because a synthesis has to name the binding constraint per element

Eight predicates, evaluated at both of §3's strokes, with the stroke each is owed at taken from [`C-0049`](C-0049-compliance-ceiling-stroke.md).
Every mechanical number is re-derived from the owning claim's own library; every lattice fact from `C-0040`'s census and `C-0041`'s packer.
**At the desired stroke the placement predicate is read as §3's force clause (`secant ≥ 100 pN / 10 nm`), not as an equality** — an element that over-delivers is not failing the force target.

### At §3's ACCEPTABLE 3 nm stroke — 3 of 14 clear

| element | owner | paths | tangent | stability tangent | **binding constraint** |
|---|---|---|---|---|---|
| `E1` axial duplex standoff | `C-0023`/`C-0017` `K1` | 45 | 9900 | 9900 | **placement** — 297× the mandate |
| `E3a` transverse flexure, free ends | `C-0023` | 45 | 33.33 | 33.33 | **packing** (`C-0041`) |
| **`E3a`, free ends** | `C-0023` | **15** | **33.33** | 33.33 | **none — every predicate clears** |
| `E3b` transverse flexure, held ends | `C-0023` | 45 | 91.13 | 91.13 | **compliance ceiling**, 2.28× |
| **`E5` crossover-hinge flexure (1 crossover)** | `C-0023` | 45 | **33.33** | 33.33 | **none — every predicate clears** |
| `E5a16` two-spring elastica | `C-0039` | 45 | 36.44 | 36.44 | **hinge inventory** — 720 crossovers demanded against 56 |
| `E5a4` | `C-0039` | 45 | 37.13 | 37.13 | **hinge inventory** — 180 against 56 |
| `E5a2` | `C-0039` | 45 | 38.04 | 38.04 | **hinge inventory** — 90 against 56 |
| **`E5a1`** | `C-0039` | 45 | **39.18** | 39.18 | **none — every predicate clears**, with 2.1 % of ceiling margin — [`CH-0062`](../challenges/CH-0062-the-buildable-hinge-count-clears-the-ceiling-on-the-elastica.md) |
| `C-0030` coupled flexure, favourable | `C-0030`/`C-0037` | 45 | 25.23 | 25.23 | **packing** (`C-0041`) |
| `C-0030` coupled flexure, favourable | `C-0030` | 15 | 25.49 | 25.49 | **stability at 2 mM** — 4 of `C-0017`'s 6 floors cleared (`C-0049`) |
| `C-0030` coupled flexure, adverse | `C-0030` | 45 / 15 | 44.82 / 43.79 | — | **compliance ceiling**, 1.12× and 1.09× |
| ideal linear coupling at §3's **desired** clause | hypothetical | 15 | 10.00 | 10.00 | **placement** — 10 pN/nm delivers 30 pN at 3 nm |

> **Two of the three clearing rows carry an idealisation this claim does not remove.**
> `E3a`'s *"ends free to draw in"* is `C-0023`'s own favourable reading and `C-0025`/`CH-0031` show **no covalent origami motif reaches it** — the joint that does lands inside `C-0023`'s bracket at a 31.64 nm span and 37.39 pN/nm, which is what `C-0030`'s coupled flexure prices properly.
> `C-0023`'s `E5` is a **linear** hinge-and-rigid-arm law, superseded by `C-0029`/`C-0034`/`C-0039`'s exact rotation — which is why its exact siblings `E5a1` and `E5a2` **fold** before the desired stroke while `E5` does not.
> **`E5a1` is the only clearing row computed on the exact element**, and its plan view is unassessed (open item 1).

### At §3's DESIRED 10 nm stroke — 0 of 14 clear

| element | secant | tangent | assembled | per path | **binding constraint** |
|---|---|---|---|---|---|
| `E1` | 9900 | 9900 | 99 000 pN | 2200 pN | compliance ceiling |
| `E3a`, 45 paths | 33.33 | 33.33 | 333.3 pN | 7.41 pN | **packing** (`C-0041`) |
| `E3a`, 15 paths | 33.33 | 33.33 | 333.3 pN | **22.22 pN** | **per-path unzip allowable**, 2.22× |
| `E3b` | 296.8 | 819.6 | 2968 pN | 65.96 pN | compliance ceiling |
| **`E5` (1 crossover, linear)** | **33.33** | **33.33** | 333.3 pN | 7.41 pN | **stroke reach — every coupling-side predicate CLEARS** |
| `E5a16` | **69.94** | 264.2 | **699.4 pN** | **15.54 pN** | compliance ceiling; and per-path 1.55× past |
| `E5a4` | 108.0 | 835.6 | 1080 pN | 24.00 pN | compliance ceiling |
| `E5a2`, `E5a1` | — | — | — | — | ~~**the arm folds before reaching the stroke** — `C-0039`'s solver refuses it~~ **CORRECTED, iteration 20 (`C-0101`, `T-167`; the ground only — the verdict stands).** Neither arm folds. `C-0039`'s **doubling force ladder** did, and recorded the loss as physics: the note behind this row cited a near-end rotation of **3.03 × 10¹²¹**, a diverged shooting parameter. Re-run on `C-0096`'s branch continuation the two rows refuse for **different and real** reasons — one **kinematic**, *"an inextensible arm of 9.985 nm cannot lift its tip 10.001 nm"*, which needs no solver; one a **genuine** branch limit reaching 9.099 nm of a demanded 9.126, i.e. 99.7 % rather than the 89 % the ladder reported. **The desired stroke is unreachable either way**, which is why the verdict is untouched — and `C-0078`'s rule is why the ground is: *a verdict that survives can survive on a different reason* |
| `C-0030` favourable, 45 / 15 | 29.81 / 27.34 | 41.05 / 29.58 | 298 / 273 pN | 6.63 / 18.23 pN | compliance ceiling / **per-path allowable** |
| `C-0030` adverse, 45 / 15 | 65.76 / 58.86 | 116.4 / 92.90 | 658 / 589 pN | 14.61 / 39.24 pN | compliance ceiling |
| **ideal linear coupling at 10 pN/nm** | **10.00** | **10.00** | **100.0 pN** | **6.67 pN** | **static stability at 2 mM** — and then the stroke |

Two rows carry the argument.

1. **`E5` clears every coupling-side predicate at 10 nm and fails on the stroke.** It places (over-delivers 333 pN against a 100 pN target), it is inside the declared ceiling, its per-path force is 7.41 pN against a 10 pN allowable, 45 crossovers are inside the tile's 56, and it is stable at both buffers. **The only thing it fails is the reach.** No sharper demonstration exists that the binding constraint is not the coupling. (Its linearity is `C-0023`'s idealisation, superseded by the elastica — which is why its exact siblings fold.)
2. **The ideal coupling placed at §3's own desired clause fails on stability before it fails on stroke** — 10 pN/nm against `C-0017`'s 23.41–27.91 pN/nm floor at the 10 nm layer in 2 mM. That **independently reproduces [`CH-0059`](../challenges/CH-0059-the-desired-stroke-placement-is-below-the-stability-floor.md)**, raised by `C-0046` in the same iteration from a completely different direction, and it **closes `CH-0059`'s own open item 3**: at 5 and 7 nm, where `C-0017` reports the floor as **zero** and the 10 pN/nm placement would *not* be refused, the kinematic ceiling is **4.33–4.62 nm** and **6.42–6.66 nm** — so the desired stroke is out of reach there for a reason that owes nothing to stability.

---

## What is established, and what is not

| statement | established? |
|---|---|
| *"§3's ~10 nm desired stroke is unreachable **with this catalogue**"* | **yes**, 0 of 14 rows, and by five independent routes now |
| *"…unreachable **on §3's own stack**"* — its three layer heights, its 100 pN, its buffer | **YES, and this is the claim.** It needs no catalogue at all: `s < L₀ ≤ 10 nm` |
| *"…unreachable **in physics**"* | **NO, and it is false.** A taller layer delivers it |

**The escape is a specification change, and it is priced.** The layer height at which the 100 pN dead-load stroke reaches 10 nm, at `σ` = 0.024 nm⁻², solved as a root over `C-0003`'s six models:

| model | `L₀` required |
|---|---|
| strong-stretching(two-body) | **16.63 nm** |
| strong-stretching(virial) | 19.48 nm |
| strong-stretching(des-Cloizeaux) | 19.59 nm |
| alexander-box(two-body) | 21.21 nm |
| alexander-box(des-Cloizeaux) | 26.07 nm |
| alexander-box(virial) | **26.12 nm** |

**1.7–2.6× §3's tallest layer**, and `C-0001` already recorded the direction — *"the reason to go outside the 5–10 nm range is upward"* — without pricing it.
§3's own tile-thickness row notes the effort point *"may sit ~20–25 nm above the electrode"*, so the geometry is not absurd; but a 17–26 nm layer is a different device (`C-0002`'s crossover, `C-0005`'s screening validity, `C-0007`'s drainage and `C-0012`'s bias all move), and **nothing in this programme has evaluated one.**

The other three escapes, all specification questions already in the queue and all **necessary but not sufficient**:

| escape | value | owner | what it buys |
|---|---|---|---|
| tile footprint **1.44×** (2330 nm²) | `T-102` | `C-0041` | the flexure array's plan view at the desired stroke |
| superstructure perforation (2223 nm² of slots) | `T-95` | `C-0035` | the midspan clearance at the desired stroke |
| buffer **0.5 mM** | `T-63` | `C-0032`, `C-0046` | the stability floor, for `C-0030`'s law and for a 10 pN/nm placement |

**None of them moves the stroke ceiling**, because none of them is a layer height.

---

## The five verification gates

Executed as **27 gate-named tests** in `src/test/kotlin/synthesis/DesiredStrokeReachTest.kt` (shared with `C-0049`); `tools/verify.sh` **BUILD SUCCESSFUL, 0 failures**.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | the reach ceilings are lengths and scale **exactly** with the layer under `L₀ → λL₀`, `Nσv₀ → λNσv₀`; unphysical arguments throw at five entry points, including a dry thickness above the resting height and a crossover fraction of zero | **PASS** |
| **2 — limiting cases** | the validity ceiling **tends to the kinematic one** as the crossover fraction tends to a melt and **vanishes** when it equals the layer's own resting volume fraction; the kinematic ceiling is strictly below the resting height at all three §3 design points; the dead-load stroke vanishes at zero load, rises monotonically over four loads spanning 30×, and **never leaves the kinematic ceiling**; the layer carries **exactly nothing at `L₀`**, which is the `s = 0` endpoint | **PASS** |
| **3 — symmetry and conservation** | **the delivered stroke is monotone decreasing in the coupling stiffness** over six stiffnesses spanning 300×, and every one is below the free stroke — `C-0017`'s theorem re-derived, and the licence for the whole claim; the binding constraint is the **first failing predicate in declaration order**, asserted with two simultaneous failures; a refusal is carried as a refusal, not as a zero | **PASS** |
| **4 — numerical convergence** | the dead-load bisection exits on the **bracket width** and is scan-independent 64 → 4096 to **0.0**, with the solved height reproducing the target load to 1e−7; the resting height solved for a 10 nm stroke reproduces that stroke to 1e−6 and lies above it; the elastica's own RK4 200 → 1600 moves the desired-stroke tangent by 2.3e−9; **the result file re-run through `tools/study.sh` reports *"no result file changed"*** | **PASS** |
| **5 — literature and upstream** | 15 reproductions, worst departure **3.0e−4**: **`C-0001`'s dead-load strokes at its own 10 nm design point, 3.4978 and 5.3457 nm, to 1e−6** — and none of its four models reaches 10; **`C-0018`'s stroke ceiling at 10 nm reproduced from its six dry thicknesses, 9.45–9.50 nm**; `C-0017`'s mandate and 297× over-stiffness; `C-0023`'s `E3a`, `E3b` and `E5` designs; `C-0039`'s `E5a16` arm and its four stiffnesses; `C-0030`'s span and tangent; `C-0040`'s four-crossover census; `C-0041`'s 15-path packing limit; `C-0032`/`CH-0047`'s 22.875 pN/nm at 4.555 nm | **PASS** |

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | a §3 layer height above 10 nm | **no** | §3 names 5, 7 and 10 |
| 2 | the dead-load stroke exceeding the kinematic ceiling | **no** | at 66 states |
| 3 | an element reaching 10 nm while clearing every other predicate | **no** | 0 of 14 rows |
| 4 | the delivered stroke rising with the coupling stiffness | **no** | monotone over 300× |
| 5 | a specification-neutral escape | **no** | every escape found is a specification change, and named as one |

**A result that was not anticipated:** the cleanest demonstration in the whole table is a **pass**, not a fail — `C-0023`'s `E5` clears every coupling-side predicate at the desired stroke and is refused only by the reach. The programme spent five claims establishing that the coupling cannot do it; the coupling is not what cannot do it.

---

## Validity range

- **TRL 1–3. Nothing here is measured**, and the motif is not demonstrated: `C-0028`'s and `C-0029`'s literature findings stand upstream of every element — no duplex has been built standing normal to a single-layer sheet, and a duplex end has at most two covalent links.
- **The kinematic bound is a theorem about the coordinate and is model-free.** Bounds 3 and 4 are `C-0003`'s and `C-0002`'s and inherit their validity ranges in full, including `C-0016`'s finding that the solved SCF layer is 1.22× outside `C-0003`'s bracket at 5 nm.
- **The dead-load stroke assumes the field can supply whatever force the layer demands**, capped only by validity — it is `C-0001`'s convention and it is the **favourable** reading. `C-0012` and `C-0018` show the bias ceiling binds before that in places, which only lowers the ceiling further.
- **`L₀` is a FORCE-ONSET height** (`C-0011`, `CH-0010`) at a defining load of 1.0 pN. The first-moment thickness of the same layer is 1.71–2.16× smaller, so **a bench reading these numbers in the other convention would see a still smaller stroke ceiling.**
- **The stability floors and the per-path allowable are CITED**, not recomputed; they carry `C-0005`'s 123–214 % one-loop correction and `CH-0029`'s loading-rate dependence respectively. **No verdict here turns on either** — the binding bound is kinematic. (**RESTATED, `CH-0167`/`C-0137`**: this is an error bar on a LEVEL, and a margin read at a force-pinned operating point is not a level — the same-kind thresholds are a force **1.48–2.22×** smaller or a decay length **9.73 %** shorter.)
- **The catalogue is judged at one placement convention per stroke**, per `C-0049`: equality at the placement stroke, force clause beyond it. `CH-0059` shows the specification does not distinguish the two devices; both are evaluated.
- **The plan view of the hinge-arm family is NOT assessed.** `C-0041`'s packer is written for the standoff-and-tie flexure array; no claim has solved a 45-arm hinge-line plan view, and the rows carry `packingAssessed = false` rather than a fabricated pass. **`E5a1`'s clean sweep at the acceptable stroke is therefore conditional on a plan view nobody has drawn** — and it is this claim's largest open item.
- **`C-0037`'s truss and `C-0023`'s `E4` antagonistic pair are represented by their parent elements** — the truss by `C-0030`'s coupled flexure (`C-0037` moves the tangent 25.20 → 26.09, 3.5 %, and no predicate with it), and `E4` by the elements it is built from, since its own limbs are `C-0014`'s tethers under another name and it needs a second ground under the tile.
- **`C-0046`/`T-99` ran concurrently and independently.** Its `(56, 1)` best point — arm 9.973 nm, tangent 38.17 pN/nm, usable stroke 3.312 nm — and this claim's `E5a1` at 45 paths — arm 9.131 nm, tangent 39.18 — are the same design read on two placement solvers, and they agree in direction, magnitude and verdict. **Nothing here depends on `T-99`**, and if `T-99` had found a reaching point it would still have been refused by bound 1.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| §3's layer heights and targets | 5 / 7 / 10 nm; 100 pN; 3 nm; ~10 nm; 40 × 40 nm; 2 mM | **CITED** — and the first of these is what decides this claim |
| `C-0002`'s concentrated crossover | `φ = 0.2` | **CITED**, and it is `C-0018`'s own binding ceiling |
| osmotic virial coefficients | `A₂` = 1.9e−3, `A₃` = 2.0e−2 | **CITED, MEASURED**, via `C-0002` |
| stability floors at 10 nm | 23.41–27.91 (2 mM), 3.86–15.94 (0.5 mM) pN/nm | **CITED**, `C-0017` |
| per-path unzip allowable | 10 pN | **CITED**, `C-0006`/`CH-0029` |
| duplex `EI`, `S`, `k_θ`, `d` | 230 pN·nm², 1100 pN, 13.5294 pN·nm/rad, 2.69 nm | **CITED**; `EI` and `k_θ` **MODEL INPUTS / FITTED**, `S` and `d` **MEASURED** |
| `C-0041`'s footprint and `C-0035`'s slot area | 2330 nm², 2223 nm² | **CITED**, as escapes |

Everything else — the three stroke ceilings at 66 states, the whole `σ` sweep, the dead-load strokes, the required layer heights, and every catalogue row's secant, tangent, assembled force, per-path force and predicate — is **derived here in code**, with `C-0001`'s, `C-0003`'s, `C-0017`'s, `C-0023`'s, `C-0030`'s, `C-0039`'s, `C-0040`'s and `C-0041`'s pipelines **re-run rather than tabulated**.

## Still open — named, not answered

1. **The plan view of a 45-arm hinge-line array.** `E5a1` clears every predicate this claim can evaluate at §3's acceptable stroke, and its plan view has never been drawn. `C-0041` did exactly this to the flexure array and it cost 45 paths → 15. **It is the first thing to run against `E5a1`.**
2. **A layer of 17–26 nm.** Nothing in this programme has evaluated one, and four upstream validity ranges move with it. It is a specification question for NDI (a fourth, after `T-95`, `T-102` and `T-63`) and then a task.
3. **Which device §3's desired clause names** — `CH-0059`'s question, unchanged. This claim answers *both* readings, because bound 1 does not care.
4. **`C-0016`'s and `C-0027`'s standing headline should now cite a mechanism.** They report the desired stroke unreachable *"at every height and every grafting density"*; the reason is that the stroke is the height.

## Challenges

**Raises [`CH-0062`](../challenges/CH-0062-the-buildable-hinge-count-clears-the-ceiling-on-the-elastica.md)** against `C-0040`, jointly with [`C-0049`](C-0049-compliance-ceiling-stroke.md).

**None stands against this claim.** The three ways it would fail:

1. **A layer height above 10 nm inside §3.** There is none; if NDI supplies one, bound 1 moves and everything downstream has to be re-run — which is the point of pricing the escape.
2. **A coupling that increases the delivered stroke.** That would break `C-0017`'s monotonicity theorem, which is re-derived here; it would require a coupling that *drives* the tile, i.e. a second actuator.
3. **A demonstration that the tile may compress the layer past `φ = 0.2` or onto its dry thickness.** Bound 2 is a hard geometric floor; bound 3 is a validity range and could in principle be extended by a concentrated-regime equation of state. **Even bound 2 is short of 10 nm by 1.02×**, so extending bound 3 buys 1.12× and not the answer.
