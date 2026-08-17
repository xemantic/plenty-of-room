# C-0079 — **Two unbonded duplexes in 2 mM MgCl₂ hold NO separation at all**, and that is the answer rather than the absence of one: the interaction is repulsive at every separation, on four independent methods read directly, so the plan model's `d` is not a separation but a **threshold on an energy the plan model never states**. And the budget is measurable, because the **host sheet is a measured object**: holding two of its own duplexes at the SAXS 2.69 nm costs **7.99970 k_BT per crossover column**, against **4.94674 k_BT** for a body in `C-0066`'s gap at the placement threshold — so the affordable width is **at or below 2.1 nm** and **34 of 34 place**. `CH-0089` is **UPHELD**. The edge exists and it is **not electrostatic**: at 2 mM the Debye length, 3.92688 nm, is longer than the whole disputed bracket, and the only thing that can place an edge is the measured **0.24 nm** short-range law. And `C-0069`'s `Q5` gap is a **COAXIAL** geometry, **15.1103×** cheaper and **finite at zero gap** — what it must prevent is a blunt-end **stacking bond**, not a clash, so its length is the **0.51108–1.3 nm** stacking range and its margin is **1.41561 nm, 55.3×** the published knife edge

| | |
|---|---|
| **Task** | [`T-139`](../tasks/T-139.md), raised by [`C-0076`](C-0076-weave-exclusion-width.md)'s *Still open* item 3 and [`CH-0089`](../challenges/CH-0089-the-collinear-clearance-is-a-girth-not-a-lattice-constant.md)'s *"How this challenge would fail"* item 1 |
| **Leaf** | **`A8.2`** (the plan model the joint budget is written on), with **`A7.4`** (electrostatics) and **`A1.2`** (the anchoring array) |
| **Verification type** | **logical** (a categorical argument about what a packing predicate is a predicate *about*, and a range argument that needs no solve) **+ in-silico** (closed-form Debye-Hückel pair energies in three geometries, each against an independently written 2-D quadrature of the screened Coulomb kernel; the array-to-pair conversion recovered by numerical differentiation; Derjaguin against direct quadrature; a bracketed root for the threshold width) **+ literature** (two surveys fetched for this task, **25 recorded queries in one and 11 EuropePMC searches plus five direct-fetch groups in the other**, a read flag on every number) |
| **Verdict** | **PASS on acceptance branch 2, and the refusal comes with the number the branch was written to protect against not having.** **There is no equilibrium separation**, and this is not a modelling limitation — it is the majority finding of the field on **four independent methods**, every one read directly: osmotic stress plus XRD (*"the force-spacing curve extends to infinity because zero force can only be achieved at infinite DNA-DNA spacing"*, Meng 2020; *"DNA helices repel at all separations"*, Rau & Parsegian 1992), all-atom two-duplex PMF (Yoo & Aksimentiev 2016, Zhang 2017, He 2023), and the second virial coefficient of **free** duplexes at **3 mM Mg²⁺**, the nearest measured point to this device's buffer, *"indicating repulsion"* (Pabit 2009). **So the plan model's `d` is a THRESHOLD, not a separation** — the eighth instance in this project of a quantity that is not well posed without the state it is read at, and the first where the missing state is an **energy budget**. **And the budget is measurable, because the host sheet is a measured object**: holding two adjacent host duplexes at the SAXS 2.69 nm over one 40 nm interface costs **31.9988 k_BT**, i.e. **7.99970 k_BT per crossover column**, while a body in `C-0066`'s gap at the placement threshold costs **4.94674 k_BT** — **1.62× cheaper than what the sheet already pays**. The affordable width is therefore **at or below the continuum model's own 2.1 nm floor**, and **34 of 34 place at every reading from 0.51108 nm to 2.71561 nm**. **The cheap bound decided the method and it was right for a reason it did not anticipate**: at 2 mM the Debye length is **3.92688 nm**, longer than the entire 1.78272 nm disputed bracket, so a bespoke two-cylinder nonlinear PB solve would have resolved the one term that provably cannot place an edge at 2.7 nm — but an edge *does* exist, and it is the **measured 0.24 nm** short-range law, whose fit is `Π = 201.8 GPa·e^(−d/2.4 Å)` in **interaxial** coordinates and therefore free of the 0.183 nm hard-diameter convention that is 41 % of the quantity under test. **`C-0069`'s `Q5` is a different geometry from `C-0066`'s bound 4 and nobody had noticed**: two collinear arms are **coaxial**, not crossed, and the coaxial energy is **15.1103×** smaller and **finite at zero gap** (1.37475 k_BT). What a collinear gap must prevent is therefore a **bond and not a clash** — two blunt ends **stack**, an established origami motif worth **−4.4114 k_BT** per helix — so its length is the stacking **range**, 0.51108 nm (oxDNA2's cutoff) to 1.3 nm (where the all-atom PMF turns repulsive), giving a plan margin of **1.41561 nm, 55.28×** the published 0.0256. That is `CH-0089`'s own failure route 2 answered in the direction that **widens** the margin. **And the knife edge is unresolvable by physics as well as by fabrication**: 0.0256 nm at 2.7 nm is **1.2373 %** of the pair energy, so `C-0072`'s *"neither margin is quotable"* survives on a second, wholly independent ground. Raises [`CH-0093`](../challenges/CH-0093-the-collinear-clearance-is-a-stacking-allowance-not-an-exclusion.md) and [`CH-0094`](../challenges/CH-0094-a-hard-body-width-is-a-threshold-and-no-plan-claim-states-one.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING DERIVED HERE IS MEASURED, and the MOTIF IS NOT DEMONSTRATED** (`C-0055`, `C-0029`). The **inputs** include six measurements read directly for this task and one made by this repository (`T-71`). **Mean field is NOT controlled here and this claim says so**: `C-0005` puts `Ξ` at 17–24 for Mg²⁺ at a DNA surface with no systematic theory, the Debye-Hückel premise fails on this material (reduced surface potential **1.71034**), and like-charged rods in strong coupling is exactly the regime in which PB has the **wrong sign**. What bounds that is a **measurement, not a theory**: Mg²⁺ does not condense duplex DNA at any concentration. Every electrostatic number here is an order-of-magnitude bracket and the verdict rests on the **range** and on the **literature**, never on the magnitude. |
| **Provenance** | `gpd/results/T-139-duplex-pair-separation.json`, produced by `electrostatics.DuplexPairSeparationStudyKt`; model in `src/main/kotlin/electrostatics/DuplexPairSeparation.kt`; **3 cheap-bound records, 12 geometry records, 358 profile records, 4 stationary-point records, 6 threshold-width records, 6 calibration records, 12 width-ladder records, 4 downstream records, 11 upstream reproductions, 13 literature records each with its own read flag, 5 convergence records, 5 falsifiers, 4 predicates, 11 findings**; **34 gate-named tests in `src/test/kotlin/electrostatics/DuplexPairSeparationTest.kt`**; the two literature surveys with verbatim passages and per-number read flags in `gpd/data/T-139-dna-dna-force-literature.md` (**25 recorded queries**) and `gpd/data/T-139-blunt-end-stacking-literature.md` (11 EuropePMC searches plus Crossref, arXiv and direct-fetch groups, all recorded); `tools/verify.sh` **BUILD SUCCESSFUL in 13 m 57 s — the whole suite, on its own isolated tree, with NOTHING dropped**; the result file re-run through `tools/study.sh` and diffed **byte-for-byte identical** |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**, **2:1**, so `I = ½Σc_i z_i² = 3c = 6 mM` and `λ_D = 3.92688 nm`; `ε_r = 78`, `l_B = 0.714115 nm`; B-DNA **charge** radius `T-71`'s **MEASURED 0.908638 nm**, **hard** radius the field's conventional **1.0 nm** — *carried separately and never substituted*; bare `τ = 5.88235 e/nm`, `ξ_M = 4.20063`, Manning-surviving `τ_eff = 0.700176 e/nm` at `q = 2`; `A_DNA\|w\|DNA = 5.90 zJ` (the pessimistic end of `C-0021`'s cylinder-cylinder bracket); short-range `Π_R = 201.8 GPa`, `λ = 0.24 nm` (Meng 2020, **20 mM MgCl₂**, transferred); continuum model floor at a **0.1 nm** surface separation, plan-relevant range **20 nm**; 40.0 × 40.35 nm single-layer **square-lattice** Rothemund sheet, 15 duplexes at 2.69 nm, 0.34 nm rise, 32 bp crossover interface spacing, phase **24**; `C-0063`'s **34** upward roots read from `gpd/results/T-125-upward-root-placement.json`; `C-0055`/`C-0039`'s **8.16439 nm** arm |
| **Consumes** | [`C-0076`](C-0076-weave-exclusion-width.md) (the three roles, the placement threshold, the measured girth, `DuplexSteric` — **re-run as a library**), [`CH-0089`](../challenges/CH-0089-the-collinear-clearance-is-a-girth-not-a-lattice-constant.md) (the challenge this task was raised to decide), [`C-0069`](C-0069-output-element-placement.md) (`Q5`, the 8.19 nm budget and the 0.0256 nm margin, both reproduced), [`C-0066`](C-0066-arm-slab-tie-clearance.md) (bound 4, reproduced), [`C-0072`](C-0072-plan-tolerance-model.md) (the margin identity and the four floors), [`C-0074`](C-0074-two-per-row-placement.md) (the 30-root margin, **CITED**), [`C-0063`](C-0063-upward-root-placement.md) (`armDirections` and the 34 stations, read from its result file), [`C-0055`](C-0055-unused-junction-site.md)/[`C-0039`](C-0039-two-spring-elastica.md) (the arm), [`C-0005`](C-0005-mean-field-screening-validity.md) (the mean-field ceiling — **the reason this claim is a bracket**), [`C-0008`](C-0008-electrostatic-force-and-decay-length.md)/`MagnesiumChlorideBuffer` (the 2:1 ionic strength and the Debye length), [`C-0021`](C-0021-zero-bias-resting-position.md) (Dryden's cylinder-cylinder Hamaker constant, and *"a stable equilibrium is not a confinement"*), `DnaOrigamiTile` (the Manning renormalisation), [`T-71`](C-0057-backbone-torsion-closure.md)'s `MeasuredBackbone`, `Gen1Tile` |
| **Raises** | [`CH-0093`](../challenges/CH-0093-the-collinear-clearance-is-a-stacking-allowance-not-an-exclusion.md) against `C-0053`'s footprint convention and `C-0069`'s `Q5` budget; [`CH-0094`](../challenges/CH-0094-a-hard-body-width-is-a-threshold-and-no-plan-claim-states-one.md) against `C-0041`, `C-0053`, `C-0065`, `C-0066`, `C-0069` and `C-0076` |

---

## The claim, in one line

**The question has no answer and the answer is what that means: two unbonded duplexes in this buffer hold no separation, so a hard-body exclusion width is a threshold on an energy — and the one object in the design that measures that energy is the host sheet itself, which pays more per crossover than the disputed gap costs, at every width from the measured steric floor to the placement threshold.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, energies **pN·nm** and `k_BT`; `k_BT = 4.141947 pN·nm` at **300 K**; `1 zJ = 1 pN·nm` exactly; `1 pN/nm² = 1 MPa` exactly; `1 kcal/mol = 6.94769 pN·nm`.
- Medium **aqueous 2 mM MgCl₂**. **MgCl₂ is 2:1, so `I = 3c = 6 mM`, not 2** — a monovalent intuition understates the screening threefold.
- **`D` is an AXIS-TO-AXIS separation everywhere**; `g` is an **axial end gap** in the coaxial geometry; `D_s` is a surface separation, and *which surface* is named every time it appears.
- **There are TWO radii and they are not the same number.** The **charge** sits on the phosphate locus at `T-71`'s **measured 0.908638 nm**; the **hard body** the osmotic-stress fits and Dryden's Hamaker constant are written against is the field's conventional **1.0 nm**. They disagree about where contact is by **0.183 nm**, which is 41 % of the quantity under test, so the whole short-range law is carried in **interaxial** coordinates, in which no convention is needed at all.
- A **repulsive** energy is positive and decreases with `D`; a force is `−dE/dD`.
- **The placement threshold** is `pitch − arm = 32 × 0.34 − 8.16439083 = 2.71560917 nm`, and the **steric floor** is `2 × 0.908638 = 1.81727572 nm`.

---

## Deliverable 1 — the cheap bounds, and what each settled before any code ran

### Bound 1 — a packer is a FEASIBILITY predicate, and a feasibility predicate is not a force balance

`C-0053`'s footprint convention asks *"may these two bodies both be placed here?"*, not *"where do these two bodies go?"*.
For rigid bodies the first is answered by a steric floor and the second by an equilibrium.
`CLAUDE.md`'s *"a stable equilibrium is not a confinement"* is the same distinction one level out;
here it is sharper, because **there is no equilibrium to answer the second question with**.

**This settled which acceptance branch applies, and it cost nothing.**

### Bound 2 — the electrostatic RANGE exceeds the whole disputed bracket, so the expensive solve cannot decide

| quantity | value [nm] |
|---|---|
| Debye length at 2 mM MgCl₂, `I = 3c` | **3.92688** |
| the whole disputed bracket, `3.60 − 1.81728` | **1.78272** |
| ratio | **2.20** |

A screened Coulomb interaction whose decay length exceeds the span of every candidate width **cannot place an edge inside it**;
over the whole bracket the crossed-geometry electrostatic energy moves by less than a factor of two.
**A bespoke two-cylinder nonlinear Poisson-Boltzmann solve — days of work on `PoissonBoltzmannEdge`'s machinery — would therefore have resolved the one term with no resolving power.**
That is this task's cost justification, and it is falsifiable (F2).

> **And the bound was right for a reason it did not anticipate.** An edge *does* exist. It is the **measured short-range law**, whose decay length is **0.24 nm** — sixteen times shorter than the Debye length — and every published reading of the hydration/electrostatic crossover (26 Å, Meng 2020; 30–35 Å, Rau & Parsegian 1992 and Podgornik 1994) puts this project's 18–36 Å window almost entirely inside the short-range regime.

### Bound 3 — the three roles are three GEOMETRIES, and one width cannot serve them

| role | claim | geometry | closed form | energy at 2.71561 nm |
|---|---|---|---|---|
| `ROW_PITCH` | `C-0041`/`C-0053` (**bonded**, not in question) | **parallel** | `E/L = 2τ²l_B k_BT K₀(κD)` | 0.67137 `k_BT` **per nm** |
| `BODY_WIDTH` in a gap | **`C-0066` bound 4** — a tie's flank against an arm's end | **crossed at 90°** | `E = 2πτ²l_B k_BT e^(−κD)/κ` | **4.94674 `k_BT`** |
| `COLLINEAR_CLEARANCE` | **`C-0069` `Q5`** — one arm's tip facing the next arm's root | **coaxial, end to end** | `E = τ²l_B k_BT[e^(−κg)/κ − g E₁(κg)]` | **0.32771 `k_BT`** |

All three follow from the same kernel `τ²l_B k_BT e^(−κr)/r`; only the double integral over the two bodies' arc lengths differs, and it differs by **15.1103×** between the two roles the plan model gives the *same* number to.

---

## Deliverable 2 — there is no equilibrium separation, and the literature settles it

**Four independent methods, every statement read directly.** Verbatim passages and read flags in `gpd/data/T-139-dna-dna-force-literature.md`.

| statement | who | method | flag |
|---|---|---|---|
| *"In Mg²⁺-only solutions in which DNA-DNA interaction is always repulsive, **the force-spacing curve extends to infinity because zero force can only be achieved at infinite DNA-DNA spacing**"* | Meng, Timsina, Bull, Andresen, Qiu, *Biophys. J.* **118**:3019 (2020) | osmotic stress + XRD, 20 mM MgCl₂ | **READ DIRECTLY** |
| *"Under these salt conditions, DNA helices repel at all separations."* | Rau & Parsegian, *Biophys. J.* **61**:251 (1992) | osmotic stress + XRD | **READ DIRECTLY** |
| *"pairwise DNA–DNA forces were always repulsive … regardless of the concentration"* | Yoo & Aksimentiev, *NAR* **44**:2040 (2016) | all-atom PMF, **two** parallel duplexes | **READ DIRECTLY** |
| *"lacks any deep minimum … suggests spontaneous dissociation of DNA arrays"* | He, Qiu, Kirmizialtin, *JCTC* **19**:6831 (2023) | metadynamics, random-sequence pair, pure Mg²⁺ | **READ DIRECTLY** |
| **repulsion at 3 mM and 6 mM Mg²⁺** on *free* 25 bp duplexes with no confinement; sign change only at 10 mM **free** Mg²⁺ | Pabit et al., *NAR* **37**:3887 (2009) | second virial coefficient, SAXS | **READ DIRECTLY** |

**3 mM is the nearest measured point to this device's 2 mM and it is unambiguously repulsive.**

### The model agrees, and its own stationary points are reported rather than suppressed

| stationary point | separation [nm] | energy | is it an equilibrium? |
|---|---|---|---|
| **primary minimum** | at contact, **inside the model floor** | — | **no** — an unretarded-Lifshitz artefact; every repulsive term here is bounded and `−A R/(6 D_s)` is not |
| **barrier maximum** | **2.11977** | **7.70697 `k_BT`** | **no** — the top of that artefact's barrier, at a 0.02 nm surface separation |
| **secondary minimum** | **37.15368** | **−0.00600 `k_BT`** | **no** — **170× below thermal**, at 13.7× the placement threshold, and inside the regime where retardation makes the unretarded law an overestimate. `C-0021`'s *"a stable equilibrium is not a confinement"* applies verbatim |
| **an equilibrium separation** | — | — | **NONE EXISTS** |

**Zero local minima between the model floor and 20 nm**, asserted as a gate-3 test over 3 580 samples.

---

## Deliverable 3 — the width is a THRESHOLD, and the budget is measurable

### The map

| threshold | energy [`k_BT`] | width [nm] | margin against 2.71561 | places 34? |
|---|---|---|---|---|
| 0.5 `k_BT` | 0.5 | **11.45045** | −8.73484 | **no** |
| **thermal, 1 `k_BT`** — Barker-Henderson for two **FREE** bodies | 1.0 | **8.78601** | −6.07040 | **no** |
| 2 `k_BT` | 2.0 | **6.08670** | −3.37109 | **no** |
| **5 `k_BT`** | 5.0 | **2.69385** | **+0.02176** | **yes** |
| **the host sheet's own per-crossover energy at 2.69 nm** | **7.99970** | **≤ 2.1** (at or below the model floor) | **+0.61561** | **yes** |
| 10 `k_BT` | 10.0 | **≤ 2.1** | +0.61561 | **yes** |

> **The width moves 3.93 nm per e-fold of the threshold at the loose end and 0.24 nm per e-fold at the tight end**, because the two terms have those two decay lengths. **The whole verdict is which end of that you are on**, and the answer is a physical question about what pays.

### The budget, and why it is not `k_BT`

**A thermal criterion is the criterion for two FREE bodies colliding in solution.**
Both bodies here are **covalently rooted to the same sheet**, so the energy is paid by the fold, not by thermal motion.
And the design contains an object that *measures* what a fold pays:

| quantity | value |
|---|---|
| parallel pair energy per length at the **SAXS-measured** 2.69 nm, 2 mM | **0.799970 `k_BT`/nm** |
| over one 40 nm sheet interface | **31.9988 `k_BT`** |
| **per crossover column holding that interface** (4 columns at 32 bp) | **7.99970 `k_BT`** |
| a body in `C-0066`'s gap at the placement threshold, crossed geometry | **4.94674 `k_BT`** |
| **ratio** | **1.617× cheaper than what the sheet already pays** |

**The host sheet is not a model. It is a measured object** — Fischer et al.'s SAXS Bragg constant is a measurement of fifteen duplexes held at 2.69 nm by crossovers, and origami folds. So `7.99970 k_BT` per crossover is a *demonstrated* currency, and at that budget the pair energy is exceeded **nowhere above the continuum model's own 2.1 nm floor**.

---

## Deliverable 4 — the verdict against 2.715609 nm

| reading | flag | geometry | width [nm] | plan margin [nm] | pair energy [`k_BT`] | placed of 34 |
|---|---|---|---|---|---|---|
| **oxDNA2's coaxial-stacking cutoff** — the COLLINEAR role's real bound | CITED, read directly | **coaxial** | **0.51108** | **+2.20453** | 2.676 | **34** |
| the all-atom PMF's repulsive onset (the generous end) | CITED, read directly | **coaxial** | **1.30000** | **+1.41561** | 0.694 | **34** |
| **`T-71`'s MEASURED phosphate contact** | **MEASURED, this repository** | crossed | **1.81728** | **+0.89833** | 16.14 | **34** |
| Bai's cryo-EM weave minimum | CITED, MEASURED | crossed | 1.85000 | +0.86561 | 14.68 | **34** |
| this project's asserted steric diameter | CITED | crossed | 2.00000 | +0.71561 | 9.77 | **34** |
| **the sheet-calibrated width** (at or below the model floor) | **DERIVED here** | crossed | **≤ 2.10000** | **+0.61561** | 7.65 | **34** |
| the SAXS single-layer Bragg lattice constant | CITED, MEASURED | crossed | 2.69000 | +0.02561 | **5.0097** | **34** |
| **the placement THRESHOLD, `pitch − arm`** | DERIVED | crossed | **2.71561** | **0.00000** | **4.9467** | **34** |
| the SAXS square-lattice constant | CITED, MEASURED | crossed | 2.73000 | −0.01439 | 4.913 | 22 |
| oxDNA's 2D-tile weave mean | CITED, SIMULATED | crossed | 3.25000 | −0.53439 | 4.102 | 22 |
| Bai's cryo-EM weave maximum | CITED, MEASURED | crossed | 3.60000 | −0.88439 | 3.742 | 22 |
| the width at a **1 `k_BT`** thermal threshold | DERIVED here | crossed | **8.78601** | −6.07040 | 1.000 | 22 |

**34 of 34 place at every physically calibrated reading, and the only readings that give 22 are (i) a lattice constant measured on a *different lattice* and (ii) a thermal criterion written for bodies that are not attached to anything.**

### And the knife edge is unresolvable by physics as well as by fabrication

The pair energy at 2.69 nm is **5.00968 `k_BT`** and at 2.71561 nm it is **4.94674**.
The 0.0256 nm that separates *"places"* from *"does not place"* is **1.2373 %** of the pair energy —
which is `C-0072`'s *"neither margin is quotable"* arriving through a **second, wholly independent channel**:
its four floors are fabrication and design-language statements, and this one is a statement about the physics.

### One coincidence worth naming

The threshold that reproduces the **standing 2.69 nm convention** is **5 `k_BT`**, to **0.14 %** (2.69385 against 2.69000).
Nothing forces that; the convention was chosen as *"the loosest defensible choice"* (`C-0041`) from a Bragg constant, and it lands on a round multiple of the thermal energy in a geometry nobody had computed.

---

## Deliverable 5 — `C-0069`'s `Q5` gap is a COAXIAL geometry, and what it must prevent is a BOND

Two collinear arms in the same row lie **end to end on a common axis**.
That is not the crossed geometry `C-0066`'s tie occupies, and it is not the parallel geometry the lattice constant measures.

**The coaxial closed form is `E = τ²l_B k_BT[e^(−κg)/κ − g E₁(κg)]`, and it is FINITE at zero gap** — `τ²l_B k_BT/κ = 1.37475 k_BT` — because `g E₁(κg) → 0`.
At the placement threshold it is **0.32771 `k_BT`**, **15.1103×** below the crossed geometry at the same separation.

> **So the collinear clearance is not preventing a clash. It is preventing a BOND.**

Two blunt DNA ends **stack**, and it is an established DNA-origami motif rather than a speculation — Woo & Rothemund (*Nature Chem.* **3**:620, 2011), Gerling & Dietz (*Science* **347**:1446, 2015), Kilchherr et al. (*Science* **353**:aaf5508, 2016). All numbers **read directly** except where marked:

| quantity | value | flag |
|---|---|---|
| free energy of one blunt-end stack **between two separate origami bodies** | **−2.63 kcal/mol per helix = −4.4114 `k_BT`** (1×TAE + 12.5 mM Mg²⁺, 22 °C) | **READ DIRECTLY** (Woo & Rothemund, SI Table S4) |
| the same, single-molecule | −0.8 to −3.4 kcal/mol per stack in 20 mM MgCl₂ | **ABSTRACT ONLY** (Kilchherr 2016) |
| **the RANGE** — oxDNA2's coaxial-stacking radial term | minimum **3.4072 Å**, hard cutoff **5.1108 Å** | **READ DIRECTLY** |
| **the RANGE** — all-atom PMF | force falls past **6.5 Å**, *"becomes slightly repulsive after ∼13 Å"* | **READ DIRECTLY** (Maffeo, Luan & Aksimentiev 2012) |

**The whole attractive interaction is inside two base-pair rises**, so the collinear clearance's job is done by **0.51108 nm** on oxDNA2's reading and by **1.3 nm** on the most generous one.

| collinear clearance | plan budget `p − d` [nm] | `Q5` margin [nm] | × the published 0.0256 |
|---|---|---|---|
| the standing 2.69 nm duplex girth | 8.19000 | **+0.02561** | 1× |
| `T-71`'s measured girth, 1.81728 | 9.06272 | +0.89833 | 35.1× |
| **the all-atom stacking range, 1.3** | **9.58000** | **+1.41561** | **55.28×** |
| oxDNA2's stacking cutoff, 0.51108 | 10.36892 | **+2.20453** | 86.1× |

**This is [`CH-0089`](../challenges/CH-0089-the-collinear-clearance-is-a-girth-not-a-lattice-constant.md)'s own failure route 2 — *"a demonstration that `C-0053`'s footprint convention is charging something other than a body"* — answered, and answered in the direction that WIDENS the margin.** It is [`CH-0093`](../challenges/CH-0093-the-collinear-clearance-is-a-stacking-allowance-not-an-exclusion.md).

---

## Deliverable 6 — what moves, named claim by named claim

| claim | quantity | standing | at this task's reading | moves? |
|---|---|---|---|---|
| **`C-0066` bound 4** — the tie in the gap | the clearance the 2.71561 nm gap gives a tie | **0.0256 nm** against a 2.69 nm duplex | **≥ 0.61561 nm** against the sheet-calibrated width, and the body's own crossed-geometry cost is **1.62× below what one host crossover pays** | **YES — the number, not the structure.** `pitch − arm` is upheld and reproduced |
| **`C-0069` `Q5`** — the collinear clearance | `M = p − d − L` | **+0.02561 nm** | **+1.41561 nm** at the all-atom stacking range, **55.28×** — and the geometry is **coaxial**, so the gap contains no body at all | **YES** — `CH-0093` |
| **`C-0072` floors 1–3** | the margin against the rise, the SAXS spread, the thermal breathing | all fire against 0.0256 nm | **stop firing** at 1.41561 nm (4.16 rises) | **YES — and the conclusion does NOT**: a second, independent ground replaces them (the margin is 1.2373 % of the pair energy) |
| **`C-0072` floor 4** — the arm tip's own bending | 1.80744 nm | 70.6× the margin | **1.28×** at 1.41561 nm | **weakens and still fires** — and `C-0072` itself calls it *"a floor of resolution, not of failure"* |
| **`C-0074`'s 30-root margin** | the plan margin at 30 roots | **1.76451 nm** | **2.63723 nm** at the same substitution | **no — it moves the same way and was never binding.** What refuses the 30-root design is flatness with equal springs, which contains no exclusion width at all |
| **`C-0041` Fact B, `C-0053`'s 43, `C-0065`'s trios** | — | — | — | **no** — `C-0076` already showed none of them contains a width worth moving |

**No placed count moves at any reading below 2.71561 nm, and 34 of 34 is the verdict at every physically calibrated one.**

---

## The five verification gates

Executed as **34 gate-named tests** in `src/test/kotlin/electrostatics/DuplexPairSeparationTest.kt`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | every pair energy is pN·nm and every pressure pN/nm²; the crossed/parallel ratio is a length between `0.5/κ` and `5/κ`; **the whole pair model is invariant under a common rescaling of `(length, 1/κ)` by ten**, to `1e−12`; unphysical arguments throw at **twelve** entry points | **PASS** |
| **2 — limiting cases** | the coaxial energy is **finite at contact** and equals `τ²l_B k_BT/κ` exactly; the crossed energy diverges as `1/κ` in the unscreened limit; the finite-radius factor → 1 as `κR → 0` and is `> 1` at a real radius; Manning at `q = 1` is **exactly twice** `q = 2`; `K₀`, `K₁` and `E₁` reproduce their small- and large-argument asymptotics; the van der Waals terms scale as `1/D` and `1/D^(3/2)` exactly; the measured law divides by `e` per decay length at every separation; the Derjaguin crossing length scales as `√(Rλ)` in both arguments | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the crossed-rod quadrature against the closed form at Simpson 76/150/300: `1.4e−3 → 3.8e−6 → 1.8e−7`; the coaxial one `4.0e−4 → 2.8e−5 → 1.8e−6`; the Derjaguin quadrature at 12/24/48: `0.196 → 6.5e−4 → 2.3e−12`; **the barrier under a 4× and 16× finer locating scan: `1.2e−11` nm**; the 5 `k_BT` width under a `±1e−6` relative change of the threshold: `2.0e−6` nm, which is `κ⁻¹ × 1e−6` as the derivative demands; the threshold width checked **by substitution** rather than by its bracket | **PASS** |
| **5 — literature and upstream** | **11 reproductions, worst strict departure `3.1e−3`** (the 3.93 nm Debye length, quoted to three digits): `C-0076`'s 2.715609 (`1.7e−7`), `T-71`'s 1.817276 (`2.8e−7`), `CH-0089`'s 0.898333 (`4.5e−7`), `C-0069`/`C-0072`'s **0.02560917** (`0.0`), `C-0076`'s **34 at 2.69** (`0.0`) and **22 at 2.73** (`0.0`), the Manning charge (`5.6e−6`), and **Meng's own evaluated pressure table at three separations** (`1.7e−3`, `3.9e−5`, `2.7e−4`); the Bessel and `E₁` functions against **A&S Table 9.8**; the `kcal/mol → pN·nm` conversion; **the Debye-Hückel premise checked against THIS material and asserted VIOLATED** so that a future change silently making it pass is caught | **PASS** |

### Gate 3 — six things that are not restatements of the construction

1. **The crossed-rod closed form against a direct 2-D quadrature of the screened Coulomb kernel**, agreeing to `1.8e−7` relative. The closed form came from a change of variables to polar coordinates; the quadrature is written from the kernel and shares only `τ`, `l_B` and `κ`.
2. **The coaxial closed form the same way**, to `1.8e−6`. Its `g E₁(κg)` term came from substituting `u = s + t` in a double integral over two half-lines, and nothing forces a Simpson rule to reproduce an exponential integral.
3. **The array-to-pair conversion recovered by numerical differentiation.** `Π = −∂F/∂A` on `F = 3g(d)` and `A = (√3/2)d²`, differenced, returns the `Π` the pair energy was built from at six separations to `1e−6`. **This is the one step that could silently have carried a wrong hexagonal lattice factor**, and it is asserted rather than trusted.
4. **The parallel pair energy by integrating its own force**, to `1e−6`.
5. **The Derjaguin crossing length `2√(πRλ)` against direct quadrature over the paraboloidal gap.**
6. **Charge conservation**: the Manning-surviving line charge equals the bare charge times the surviving fraction, computed through `DnaOrigamiTile`'s independent implementation; the surviving and condensed fractions sum to exactly one; **and the finite-radius cylinder's pair energy equals its own equivalent line charge's**, which is the definition the factor was derived from and is asserted at a third call site.
7. **No local minimum** in the plan-relevant range, over 3 580 samples of the gradient.

### The declared falsifiers, and what happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **F1** | the pair interaction has a real **minimum** at finite separation above steric contact | **NO** | zero local minima between 2.1 and 20 nm. A far minimum exists at **37.15 nm** and is **0.006 `k_BT`** deep — 170× below thermal, an unretarded-Lifshitz artefact, and not a confinement. The literature is unanimous and independent |
| **F2** | the electrostatic range is **shorter** than the disputed bracket, so a solve can place an edge inside it | **NO** | 3.92688 nm against 1.78272 nm. **And the finding is stronger than the bound**: an edge exists and is sharp — the measured **0.24 nm** law — so a PB solve would have resolved the wrong term |
| **F3** | the cost of a body in `C-0066`'s gap at the measured girth **exceeds** what the host sheet demonstrably pays | **NO at the placement threshold** (4.94674 against 7.99970 `k_BT`) **and YES at the bare measured floor** (16.14 `k_BT`) | so the affordable width lies **between** them, and everywhere in that interval **34 place**. This is the falsifier that could have overturned the verdict and it half-fired, which is why the claim quotes a width **at or below 2.1 nm** rather than at the floor |
| **F4** | van der Waals is **≥ 10 %** of the repulsion anywhere in the bracket | **YES — and only at the model floor** | **23.48 %** at a 0.1 nm surface separation, falling to **6.29 %** at the placement threshold and **6.43 %** at the SAXS 2.69 nm. Its declared consequence — that a minimum may exist and F1 becomes live — was **tested and did not materialise** |
| **F5** | the **coaxial** geometry costs more than the crossed one at the same separation | **NO** | **15.1103×** the other way, and finite at zero gap. This is what turned `C-0069`'s `Q5` into a different question from `C-0066`'s bound 4 |

**What was not anticipated:** the task expected to spend its budget on the electrostatics and to come back with a bracket. The electrostatics turned out to be the term that *cannot* answer the question, the term that can is a **measured** exponential nobody in this programme had fetched, and the decisive quantity turned out to be neither — it is the **energy the host sheet already pays**, which is a calibration available from a SAXS constant this repository has been quoting for fifteen iterations.

---

## Validity range

- **TRL 1–3, and the motif is not demonstrated.** `C-0055`'s and `C-0029`'s findings are unchanged and upstream of everything here.
- **Mean field is not controlled at this material and this claim does not pretend otherwise.** `C-0005`: `Ξ = 17–24` for Mg²⁺ at a DNA surface, `Ξ ∝ q³`, **no systematic theory**, one-loop correction 123–214 %. The Debye-Hückel premise fails outright — the reduced surface potential is **1.71034**, above one even after Manning renormalisation. **Like-charged rods in the strong-coupling regime is exactly where PB has the wrong SIGN**, and what bounds that here is **empirical**: Mg²⁺ does not condense duplex DNA at any concentration (Qiu, Parsegian & Rau 2010, read directly). Every electrostatic magnitude here is an order-of-magnitude bracket; the verdict rests on the **range** of the interaction and on the **literature**.
- **The short-range law is measured at 20 mM MgCl₂ and transferred to 2 mM.** Its justification is Rau, Lee & Parsegian's own measurement that the MgCl₂ decay constant is **2.7 / 2.8 / 2.1 Å at 5 / 25 / 100 mM** — nearly salt-independent. The *electrostatic* part is computed at the device's own 2 mM and therefore **double-counts** whatever electrostatics remains in the 20 mM fit; that makes the total an **overestimate of the repulsion**, which is the conservative direction for a *"places"* verdict.
- **No force curve exists at 2 mM MgCl₂.** The lowest published Mg²⁺-only curve is 5 mM and figure-only; the lowest *parametrised* one is 20 mM; the closest **measured** point of any kind is Pabit's 3 mM second virial coefficient. That is a specification gap in the literature, not in this claim.
- **`Π_A` is not used.** Meng's fitted attractive term, `−0.3 GPa`, taken literally puts a zero crossing at 3.125 nm — which would *be* an equilibrium separation and which contradicts the same paper's own prose. It is quoted to one decimal and its sign is not resolved by the data. **The observation is the primary datum and the two-parameter fit is a summary.** Carried in the source as a bound on how wrong the repulsion-only reading can be.
- **The continuum model is not carried below a 0.1 nm surface separation**, i.e. below 2.1 nm interaxial. Both a Lifshitz `1/D_s` and a fitted exponential are meaningless at a fraction of a water diameter. **The steric floor 1.81728 nm is therefore *below* the model's own validity range**, and the two conventions disagree about where contact is by 0.183 nm.
- **The sheet calibration assumes the sheet's electrostatic and short-range self-energy is carried by its crossovers**, four columns per 40 nm interface at 32 bp. It is an order-of-magnitude statement about a demonstrated object, not a free-energy accounting of folding, and it does not include base pairing, stacking or the entropy of the scaffold.
- **The blunt-end stacking numbers are measured on *other* objects**: origami tile edges at 12.5 mM Mg²⁺ (Woo & Rothemund) and origami beams at 20 mM (Kilchherr). **No per-stack free energy as a function of Mg²⁺ exists**, and the interaction's **range** — which is what this claim uses — comes from a simulation potential and an all-atom PMF, not from a measurement.
- **A *"places"* verdict is still the weak direction.** The plan model is `C-0041`'s and `C-0053`'s hard-body one at **nominal** positions; `C-0072`'s whole tolerance argument stands over this claim unchanged, and this claim **adds** a reason the nominal verdict cannot be quoted to 0.03 nm rather than removing one.
- **No flatness, stiffness, force or stroke number is touched.** This claim moves a plan margin and no load path.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| the Mg²⁺ equation of state | `Π = Π_R e^(−d/λ)`, `λ = 2.4 Å`, `Π_R = 201.8 GPa` | **CITED, MEASURED, READ DIRECTLY** — Meng et al., *Biophys. J.* **118**:3019 (2020) |
| the four *"no equilibrium"* statements | verbatim | **CITED, READ DIRECTLY** — Meng 2020, Rau & Parsegian 1992, Yoo & Aksimentiev 2016, He 2023 |
| the nearest measured concentration | repulsion at **3 mM and 6 mM** Mg²⁺ | **CITED, MEASURED, READ DIRECTLY** — Pabit et al., *NAR* **37**:3887 (2009) |
| MgCl₂ decay constants | 2.7 / 2.8 / 2.1 Å at 5 / 25 / 100 mM | **CITED, MEASURED, READ DIRECTLY** — Rau, Lee & Parsegian, *PNAS* **81**:2621 (1984) |
| Mg²⁺ does not condense duplex DNA | verbatim | **CITED, READ DIRECTLY** — Qiu, Parsegian & Rau, *PNAS* **107**:21482 (2010) |
| blunt-end stack free energy | −2.63 kcal/mol per helix | **CITED, READ DIRECTLY** — Woo & Rothemund, *Nature Chem.* **3**:620 (2011), SI Table S4 |
| the same, single molecule | −0.8 to −3.4 kcal/mol, 20 mM MgCl₂ | **CITED, ABSTRACT ONLY (verbatim)** — Kilchherr et al., *Science* **353**:aaf5508 (2016) |
| the stacking **range** | oxDNA2 cutoff 5.1108 Å; all-atom repulsive onset ~13 Å | **CITED, READ DIRECTLY** — Henrich et al. / LAMMPS `pair_oxdna2`; Maffeo, Luan & Aksimentiev, *NAR* **40**:3812 (2012) |
| `A_DNA\|w\|DNA` | 4.33–5.90 zJ, **cylinder-cylinder** | **CITED via `C-0021`**, Dryden et al., *Langmuir* **31**:10145 (2015) |
| B-form phosphate radius | 0.908638 ± 0.066499 nm | **MEASURED, THIS REPOSITORY** (`T-71`, 13 084 linkages) |
| the SAXS interhelical distances | 2.69 / 2.73 nm | **CITED, MEASURED** — Fischer et al., *Nano Lett.* **16**:4282 (2016) |
| `ε_r`, the hard radius 1.0 nm, the rise, the crossover spacing | 78, 1.0 nm, 0.34 nm, 32 bp | **CITED** |
| the 34 stations, the phase, the 8.16439 nm arm | phase 24 | **`C-0063`/`C-0055`/`C-0039`, CONSUMED AS DATA and re-run** |

Everything else — the three closed forms and their quadratures, the array-to-pair conversion, the Derjaguin crossing length, the finite-radius factor, the stationary-point census, the threshold-versus-width map, the sheet calibration, the width ladder and its placed counts, the four downstream rows and the five falsifier verdicts — is **derived here in code**.

## Still open — named, not answered

1. **No DNA–DNA force curve exists at 2 mM MgCl₂.** The lowest parametrised one is 20 mM. A measurement at this device's own buffer would replace the transfer this claim makes, and it would move the *magnitude* of the short-range term (not its range, and not the verdict, which turns on the range).
2. **`Π_A`'s sign.** Meng's own fitted attractive term, taken literally, contradicts their own prose. Resolving it would not change this verdict — a zero crossing at 3.125 nm is above the placement threshold and would make the pair *less* repulsive there — but it is the one place in the literature where an equilibrium separation could hide.
3. **Strong coupling has no theory here and the bound is empirical.** `Ξ = 17–24` is exactly the regime in which like-charged rods can attract, and the only thing excluding it is that Mg²⁺ is not observed to condense duplex DNA. A simulation at this project's own geometry and buffer would replace an observation with a calculation.
4. **The stacking-prevention clearance is a design requirement nobody has written.** `CH-0093` converts `C-0053`'s footprint convention from an exclusion into a stacking allowance; what length that allowance should actually be — 0.51 nm, 1.3 nm, or a base-pair-quantised choice above it — is a design decision this claim brackets and does not take.
5. **Every plan claim in the branch states a width and none states the threshold it was read at.** That is `CH-0094`, and it applies to `C-0041`, `C-0053`, `C-0065`, `C-0066`, `C-0069` and `C-0076` alike.
6. **The `C-0066` tie geometry is idealised as a cylinder against a flat end face.** A real tie is a duplex with its own end joint, and the crossed-cylinder Derjaguin transform assumes `λ ≪ R`, which holds for the 0.24 nm law and not for the 3.93 nm electrostatic one — where the *exact* line-charge result is used instead.

## Challenges

**Raises [`CH-0093`](../challenges/CH-0093-the-collinear-clearance-is-a-stacking-allowance-not-an-exclusion.md)** against `C-0053`'s footprint convention and `C-0069`'s `Q5` budget,
and **[`CH-0094`](../challenges/CH-0094-a-hard-body-width-is-a-threshold-and-no-plan-claim-states-one.md)** against every plan claim in the branch.

**[`CH-0089`](../challenges/CH-0089-the-collinear-clearance-is-a-girth-not-a-lattice-constant.md) is UPHELD** and its own failure route 1 — *"a measurement or calculation of the equilibrium separation of two unbonded parallel duplexes in 2 mM MgCl₂ above 2.7156 nm"* — is **closed**: no such separation exists, and the literature says so on four methods.

**None stands against this claim.** The five ways it would fail:

1. **A measured DNA–DNA force curve at ~2 mM Mg²⁺ showing a finite equilibrium spacing.** Every published curve at every concentration says otherwise, but none is at 2 mM.
2. **A demonstration that the host sheet's crossovers do NOT pay the pair energy** — i.e. that the 2.69 nm Bragg constant is set by something other than a balance the crossovers hold. Then the calibration is wrong and the budget is unknown again, and the verdict reverts to the threshold sweep.
3. **A strong-coupling calculation at this geometry and buffer finding an attraction.** `Ξ = 17–24` admits it in principle and only an observation excludes it.
4. **A blunt-end stacking range longer than ~2.7 nm.** Two independent sources put the whole interaction inside 1.3 nm; a third disagreeing would remove `CH-0093`'s widening.
5. **A demonstration that `C-0053`'s footprint convention is charging a fabrication or routing allowance rather than a body or a bond.** `C-0069` calls it *"a full duplex of clearance"* and `C-0066` calls it *"a tie's width"*, so on the record it is a body — but a staple-routing allowance would be a third reading, and it would be a *design* number rather than a physical one.
