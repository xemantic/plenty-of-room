# C-0076 — **The weave's coefficient on the plan margin is EXACTLY ZERO, twice over** — categorically, because `M = p − d − L` charges `d` *along* the helices between *unbonded* bodies while the weave is a separation *across* them between two duplexes *covalently linked at its own minimum*; and numerically, because `C-0055`'s upward roots are the **odd** crossover planes and the weave's extrema are the **even** ones, so all **34** of `C-0063`'s stations sit on a **node**, at every one of the 32 phases, with the whole disputed **1.2–1.75 nm** amplitude bracket annihilated. **A single width is defensible and it is the wrong question**: the placement threshold is `pitch − arm = 2.71561 nm` and the defensible *values* straddle it. **And the 1.85-versus-2.0 contradiction dissolves on this repository's own measurement** — `T-71`'s 13 084-linkage phosphate radius makes backbone contact **1.81728 nm**, so Bai's 18.5 Å clears it by **0.033 nm**, `0.35 σ`: the weave minimum **IS** the steric floor

| | |
|---|---|
| **Task** | [`T-137`](../tasks/T-137.md), raised by [`C-0072`](C-0072-plan-tolerance-model.md)'s *Still open* item 7 |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with **`A1.2`** for the anchoring array the plan model belongs to |
| **Verification type** | **logical** (the weave's phase is an integer lattice coordinate, so the value at a station is exact arithmetic and the coefficient is a parity argument) **+ in-silico** (`C-0063`'s `armDirections`, `C-0069`'s `placeRootedOutputElement` and `C-0053`'s `placeHingeArms` re-run as libraries at ten exclusion widths and under a position-dependent one) **+ literature** (two primary sources re-fetched and read directly for this task) |
| **Verdict** | **PASS — a single exclusion width IS defensible, and the measured weave does not touch the plan margin at all.** The **coefficient is exactly zero and it is zero for two independent reasons.** *Categorically*: `C-0072`'s `M = p − d − L` charges `d` **along** the helices, between two bodies that are **not bonded to each other**; Bai's and Snodin's weave is a separation **across** the helices between two duplexes that are **covalently linked by the crossover at its own minimum**. *Numerically, on the axis where the weave does live*: `C-0055`'s upward roots are the planes `k ≡ 2b+3 (mod 4)`, which is **odd for every duplex**, while the weave's extrema are the crossover planes `k ≡ 2b` and `k ≡ 2b+2 (mod 4)`, which are **even** — and a triangular wave is at its mean midway between its extrema. So **every one of `C-0063`'s 34 stations sits on a weave NODE**: the host duplex is at its ideal lattice position (`axis offset 0.0`), both bounding interfaces are at the lattice constant (`worst departure 4.4e−16 nm`), at **all 32 phases**, and **independently of the amplitude** — which annihilates the entire **1.2–1.75 nm** bracket the three sources disagree over. `C-0065`'s *"one helical phase class, so the count is quantised at 0 or 34"* extends to the weave, and it comes out **34**. **The 1.85-versus-2.0 contradiction `C-0072` left unpriced dissolves on a measurement this repository already owns**: `T-71`'s survey of 13 084 crystallographic linkages puts the B-form phosphate radius at **0.908638 nm**, so phosphate-backbone contact is **1.817276 nm** and Bai's `⟨d_min⟩ = 18.5 Å` clears it by **0.0327 nm**, **0.35** population standard deviations. No interdigitation is needed, and the coincidence says something stronger than *"no contradiction"*: **a crossover pulls its two duplexes together until their backbones touch**, which is why `d_min` is 18.5 Å and not less (all-atom MD's 18.0 Å sits `0.18 σ` the other side, so the two bracket contact to within one σ). **What the measurement does reopen is the width's VALUE, and that is where the whole verdict lives**: the placement threshold is exactly `pitch − arm = 2.715609 nm`, and the defensible readings **straddle it** — 34 of 34 place at the measured contact (1.8173), the asserted steric 2.0 and the SAXS 2.69, and **22** at Bai's midpoint 2.725, the square-lattice 2.73 and oxDNA's 3.25. At the measured girth `C-0069`'s `Q5` margin is **0.898333 nm — 2.64 base-pair rises, 35× the published 0.0256** — which clears three of `C-0072`'s four floors ([`CH-0089`](../challenges/CH-0089-the-collinear-clearance-is-a-girth-not-a-lattice-constant.md)). **And `C-0069`'s own published 18 of 34 decomposes into three answers from one 0.04 nm change**: **22** if only the collinear clearance moves, **18** if the body width moves too and overruns the 2.69 nm row pitch (its own reading, reproduced here to `0.0`), **30** if the row pitch is moved with it — which is the only physically consistent reading. Raises [`CH-0088`](../challenges/CH-0088-the-weave-read-through-the-plan-margin-is-on-the-wrong-axis.md) and [`CH-0089`](../challenges/CH-0089-the-collinear-clearance-is-a-girth-not-a-lattice-constant.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING DERIVED HERE IS MEASURED, and the MOTIF IS NOT DEMONSTRATED** (`C-0055`, `C-0029`). The **inputs** include three measurements: two re-fetched and read directly for this task (Bai 2012, Snodin 2019) and one made by this repository (`T-71`). Every literature number carries a read flag in `gpd/data/T-137-weave-literature.md`. |
| **Provenance** | `gpd/results/T-137-weave-exclusion-width.json`, produced by `anchoring.WeaveExclusionWidthStudyKt`; model in `src/main/kotlin/anchoring/WeaveExclusionWidth.kt`; **3 role records, 10 width records, 34 station records, 32 phase records, 18 packing records, 4 steric records, 10 literature records each with its own read flag, 6 inherited-claim records, 4 convergence records, 10 upstream reproductions, 6 predicates, 9 findings**; **20 gate-named tests in `src/test/kotlin/anchoring/WeaveExclusionWidthTest.kt`**; the literature survey and its verbatim passages in `gpd/data/T-137-weave-literature.md`; `tools/verify.sh` **BUILD SUCCESSFUL in 14 m 38 s — the whole suite, on its own isolated tree, with NOTHING dropped**; the result file re-run through `tools/study.sh`, reported *"no result file changed"*, and diffed **byte-for-byte identical** |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40.0 × 40.35 nm single-layer **square-lattice** Rothemund sheet, 15 duplexes at the SAXS **2.69 nm**, 0.34 nm rise, **32 bp** crossover interface spacing, crossover phase **24**; `C-0063`'s **34** upward roots read from `gpd/results/T-125-upward-root-placement.json`; `C-0055`/`C-0039`'s **8.16439 nm** arm and `C-0053`'s **9.131 nm** `E5a1`. **The weave itself is measured at `[Na⁺] = 0.5 M` (oxDNA) and in vitrified buffer (cryo-EM), NOT at 2 mM MgCl₂**, and that is a validity note rather than a transfer. |
| **Consumes** | [`C-0055`](C-0055-unused-junction-site.md) (`upwardHingeSites`, `CrossoverAzimuth`, the `(k − 2b) mod 4` rule and the 8 bp plane lattice — **re-run as libraries**, and the parity argument is derived from them rather than restated), [`C-0063`](C-0063-upward-root-placement.md) (**the placement itself**, read from its result file; `armDirections`), [`C-0069`](C-0069-output-element-placement.md) (`rowOfThreeLengthCeiling`, `placeRootedOutputElement`, `StationRow` — **re-run**; its 8.19, 0.0256, 34 and **18** all reproduced), [`C-0066`](C-0066-arm-slab-tie-clearance.md) (the 2.71561 nm tip gap, reproduced), [`C-0053`](C-0053-hinge-arm-array-packing.md) (`placeHingeArms`, `maximumArmsInRow`, the footprint convention — **re-run at five widths**; its 43 reproduced at every one), [`C-0041`](C-0041-flexure-array-packing.md) (`PLAN_TANGENCY_TOLERANCE`, Facts A and B), [`C-0065`](C-0065-crossbar-array-placement.md) (the one-phase-class congruence, **extended**), [`C-0072`](C-0072-plan-tolerance-model.md) (the margin identity and the weave bracket, both reproduced and one of them challenged), [`T-71`](../claims/C-0057-backbone-torsion-closure.md)'s `MeasuredBackbone` (the **measured** phosphate radius), `Gen1Tile` |
| **Raises** | [`CH-0088`](../challenges/CH-0088-the-weave-read-through-the-plan-margin-is-on-the-wrong-axis.md) against `C-0072`'s Deliverable 7 weave table; [`CH-0089`](../challenges/CH-0089-the-collinear-clearance-is-a-girth-not-a-lattice-constant.md) against `C-0066`'s bound 4 and `C-0069`'s `Q5` |

---

## The claim, in one line

**The measured weave is a real, deterministic, well-phased 1.5 nm sawtooth — and the design's own stations are all sitting on its nodes, so it says nothing about whether the array places; what it does say is that the 2.69 nm in the plan model was never an exclusion width in the first place, and the only exclusion width anybody has measured is this repository's own 1.817 nm phosphate-backbone contact, at which the branch's knife edge is 35× wider than published.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**; `k_BT = 4.141947 pN·nm` at **300 K** in aqueous **2 mM MgCl₂**.
- `x` runs **along** the host sheet's helices, `y` **across** them, `z` normal and positive upward.
- **A plane index `k`** is `C-0055`'s 8 bp crossover plane, `x = phase + k·8·rise`. Duplex `b`'s azimuth at plane `k` is entry `(k − 2b) mod 4` of `NORTH, WEST, SOUTH, EAST`.
- **Interface `b`** is the interface between duplex `b` and duplex `b+1`. **The weave `D_b(x)`** is the distance between the **helix axes** (the base-pair midpoints) of duplexes `b` and `b+1` — the definition *both* primary sources give verbatim.
- **The plan margin is `M = p − d − L`**, `C-0072`'s identity: root pitch minus exclusion width minus the element's plan length, all **along `x`**.
- **A read flag travels with every literature number**, and this claim's are in `gpd/data/T-137-weave-literature.md`.

---

## Deliverable 1 — the cheap bound: three quantities, one symbol, and the weave measures one of them

`C-0041`, `C-0053`, `C-0065`, `C-0066` and `C-0069` all write the same sentence — *"a duplex in plan is a rectangle of width `d = 2.69 nm`"* — and then use `d` for three different things.

| role | axis | between | what it is | weave applies? | defensible value |
|---|---|---|---|---|---|
| **`ROW_PITCH`** | **across** the helices | two **crossover-bonded** host duplexes | the sheet's own lattice pitch, which `C-0015`'s grid and `C-0063`'s rows are laid on | **YES** | **2.69 nm**, and the weave is its variation |
| **`BODY_WIDTH`** | across | **one free body** | the plan girth of an arm above the sheet, or of a tie standing normal to it | **no** | **1.8173 nm** (`T-71`, measured) |
| **`COLLINEAR_CLEARANCE`** | **along** the helices | two **unbonded** bodies | the gap `C-0053`'s footprint convention charges between one element's tip and the next one's root — **the `d` in `M = p − d − L`** | **no** | **1.8173 nm**, the girth of whatever stands in the gap |

**Two conditions have to hold for the weave to be a measurement of a slot, and the plan margin's slot fails both.** It is the wrong **axis** — Bai and Snodin both measure a distance between helix *axes*, perpendicular to them, and a triangular wave along `x` in that perpendicular distance has no component *along* `x`. And it is the wrong **bonding** — the weave's minimum is *at* the crossover, i.e. at the one point where the two bodies are held by a covalent Holliday junction; a hard-body exclusion width is a statement about bodies that are **not** held to each other.

> **This bound cost nothing and it settles the acceptance predicate.** It is the same class of error as `CH-0021` (*"a concentration factor and a per-path share live on different cuts"*) and `CH-0004` (*"the Debye length is three numbers and all three are correct in their own place"*) — a quantity carried into a slot that names it but does not mean it.

---

## Deliverable 2 — the congruence: every station is a NODE, and the amplitude bracket is annihilated

### The parity argument, in two lines

`C-0055`'s lattice, consumed rather than restated:

- duplex `b` crosses over to `b+1` at planes **`k ≡ 2b (mod 4)`** and to `b−1` at **`k ≡ 2b+2 (mod 4)`** — both **even**;
- duplex `b`'s **upward** (`EAST`) sites are at **`k ≡ 2b+3 (mod 4)`** — **odd for every `b`**, because `2b` is even.

Snodin measures the weave as a **triangular** wave with its **minimum at the interface's own crossovers** and its **maximum at the adjacent interfaces' crossovers** — the extrema are exactly the even planes. A triangular wave takes its **mean** midway between consecutive extrema, and the odd planes are exactly midway.

**So every upward root is a node of the weave on both of its bounding interfaces, and the host duplex's own axis is at its ideal lattice position there.**

### What that measures, on `C-0063`'s actual placement

| quantity | value |
|---|---|
| stations sitting on a weave node | **34 of 34** |
| worst interhelical departure from the lattice constant at a station, **over all 32 phases** | **`4.4e−16` nm** (the result file prints `0.0`: it is below `RESULT_ABSOLUTE_FLOOR`, and in nm that floor is honest) |
| worst host-duplex axis offset at a station, over all 32 phases | **`0.0` nm** |
| across-row clearance at every station, at the measured girth | **0.87272 nm**, identically |
| dependence on the weave amplitude, swept `0 → 2.5 nm` | **`≤ 1e−14` nm** — a gate-3 test |

> **The amplitude bracket the literature disagrees over — 1.2 nm (all-atom), 1.5 nm (oxDNA, on this project's own object), 1.75 nm (cryo-EM) — has coefficient exactly zero at the stations.** The one quantity three papers cannot agree on is the one quantity the design does not depend on.

### And it holds for the two edge duplexes as well

Snodin excludes the top and bottom helices because they are *"only constrained on one side"*. Under that reading the two edge duplexes are straight and the two edge interfaces carry **half** the amplitude — the node structure is untouched, because a straight duplex has no excursion and its zig-zagging partner is at its own mean at every odd plane. The claim is therefore **stronger** at the edges, not weaker.

### The design rule this hands out for free

A **24 bp** arm is **three** crossover planes, an odd number, so its root is a node and its **tip sits at an antinode** — the host duplex under the tip is displaced **0.375 nm** (at Snodin's amplitude) toward its other neighbour. **An element of an even number of planes — 16 bp or 32 bp — puts both of its ends on nodes.** Anything that has to register at an element's *tip* should be quantised that way, and it costs nothing.

---

## Deliverable 3 — the packer, re-run

`armDirectionsWithClearance` takes the clearance as a **function of position**; it reproduces `C-0063`'s `armDirections` **bit for bit** at a constant function, over five widths × four lengths × fifteen rows (gate 2, departure `0.0`). No shared main source was edited.

### The count against the exclusion width — and the threshold is arithmetic

| exclusion width [nm] | reading | read flag | `M = p − d − L` [nm] | over the rise | placed of 34 |
|---|---|---|---|---|---|
| **1.81728** | **`T-71`'s MEASURED phosphate contact** | **MEASURED** | **+0.89833** | **2.64** | **34** |
| 1.80 | narrow fibre phosphate | CITED | +0.91561 | 2.69 | 34 |
| **1.85** | **Bai's weave MINIMUM** | CITED, MEASURED | +0.86561 | 2.55 | **34** |
| **2.00** | this project's asserted steric diameter | CITED | **+0.71561** | 2.10 | **34** |
| **2.69** | SAXS single-layer lattice constant | CITED, MEASURED | **+0.02561** | 0.075 | **34** |
| **2.71561** | **the threshold, `pitch − arm`** | DERIVED | **0.0** | 0 | **34** |
| 2.725 | Bai's sawtooth midpoint | DERIVED from a MEASUREMENT | −0.00939 | −0.028 | **22** |
| 2.73 | SAXS square-lattice constant | CITED, MEASURED | −0.01439 | −0.042 | **22** |
| 3.25 | Snodin's oxDNA 2D-tile mean | CITED, SIMULATED | −0.53439 | −1.57 | **22** |
| **3.60** | **Bai's weave MAXIMUM** | CITED, MEASURED | −0.88439 | −2.60 | **22** |

**The verdict is a step function of the width and the step is at `pitch − arm = 2.715609 nm`** — a lattice quantity with no fitted parameter in it. Everything below places 34; everything above places 22. **The defensible readings straddle it by 0.9 nm on one side and 0.9 nm on the other**, and the standing 2.69 nm sits **0.0256 nm** below it.

### `C-0053`'s in-plane array does not move at all

| width [nm] | 1.81728 | 2.00 | 2.69 | 2.725 | 2.73 |
|---|---|---|---|---|---|
| `E5a1` arms placed of 45, best of 32 phases | **43** | **43** | **43** | **43** | **43** |

`C-0053`'s *"at the 2.0 nm steric reading the placement is unchanged at 43"* is reproduced, and extended over the whole bracket. Its binding constraint is the hinge lattice's root pitch, which no exclusion width touches.

### The category error, priced rather than argued

Substituting the weave into the collinear slot — the operation `C-0072`'s table performs — places **28 of 34** at Snodin's amplitude on the SAXS mean and **22 of 34** at Bai's, resolution-independent over four decades of `x`-snapping (a gate-4 test). **Neither is *"places comfortably"* nor *"does not place at all"*.** `C-0072` propagated onto the **margin** and the **count** is a step function of it: that is `CH-0088`'s second ground.

### One number, three answers — `C-0069`'s published 18 of 34 decomposes

Moving the interhelical constant by **0.04 nm**, from 2.69 to the square lattice's 2.73:

| which of the three roles is moved | placed of 34 |
|---|---|
| all three at 2.69 — the standing design | **34** |
| the **collinear clearance** alone (rows left at 2.69, all-or-nothing per row) | **22** |
| **`C-0069`'s own reading** — width 2.73 against a 2.69 nm row pitch, so the bodies also overlap **across** the rows | **18** *(reproduced to `0.0`)* |
| **the row pitch moved with it** — the only physically consistent reading, because the rows **are** the sheet's duplexes at whatever the interhelical distance is | **30** |

> **A 1.5 % change in one constant is worth 12 arms or 4, depending on which of three roles it is applied to.** `C-0069`'s 18 is not wrong; it is a reading in which the arms are made **wider than the rows they sit in**, which is arithmetically a statement about the *body width* and not about the interhelical distance at all.

### `C-0041`'s Fact A weakens; its verdict does not

Fact A is that adjacent-row bodies bury each other's ties at any tilt, because `2.69 cos θ < 2.69`. At a body girth `w` on a row pitch `a` the condition is `θ > arccos(w/a)`:

| girth | tilt at which Fact A starts to bite |
|---|---|
| 2.69 nm (the lattice constant, `C-0041`'s reading) | **0°** — every tilt |
| 2.00 nm (asserted steric) | **41.97°** |
| **1.81728 nm (measured)** | **47.50°** |

**Fact B is untouched** — 34.51 nm of span-plus-duplex against a 13.33 nm column pitch, **2.59×**, and no width in the bracket moves it. `C-0041`'s *"0 of 720 orientations"* therefore stands, **on Fact B alone**. `CLAUDE.md`'s *"a verdict that survives can survive on a different reason"*, in a new place.

---

## Deliverable 4 — the adjudication: 1.85 nm against 2.0 nm

`C-0072` left this: *"the weave minimum is inside the 2.0 nm steric diameter this project asserts … the resolution is presumably that a crossover-linked pair interdigitates its grooves, but nothing in this programme has priced it."*

**It does not need to be priced, because the round 2.0 nm is a convention and this repository has the measurement.** `T-71` surveyed 876 X-ray DNA-only RCSB entries and 13 084 crystallographic linkages and emitted the constants into source; the B-form C2′-endo population phosphate radius is **0.908638 nm**, SD **0.066499 nm**.

| reading of the duplex's steric diameter | value [nm] | admits Bai's 18.5 Å? | σ above contact |
|---|---|---|---|
| this project's asserted 2 × 10 Å | 2.000 | **no** | — |
| the narrow fibre reading, 2 × 9.0 Å | 1.800 | yes | — |
| **`T-71`'s MEASURED contact, 2 × 0.908638** | **1.817276** | **YES, by 0.0327 nm** | **+0.35** |
| the same, against Yoo's all-atom `d_min` of 18.0 Å | 1.817276 | no, by 0.017 nm | **−0.18** |

**Three statements, in order of strength.**

1. **There is no contradiction.** The measured phosphate-backbone contact is **1.817 nm**, `CLAUDE.md`'s own 8.9–9.4 Å bracket contains it, and Bai's 18.5 Å is outside it. The 2.0 nm is a round number quoted from a single 2024 secondary reading (`a_DNA ≈ 10 Å`), and it is **9.1 % above** what 13 084 measured linkages give.
2. **The coincidence is the finding, not the clearance.** The two independent measurements — a cryo-EM origami weave minimum and a crystallographic backbone radius — agree to **1.8 %**, and all-atom MD's 18.0 Å sits `0.18 σ` the *other* side. **The weave minimum IS the steric floor**: a crossover pulls its two duplexes together until their backbones touch, which is why `⟨d_min⟩` is 18.5 Å and not less. Nothing in either source forces that agreement.
3. **Even if it did not clear, a hard-body model would not apply there.** The weave's minimum is at the crossover, where the two duplexes are held by a covalent Holliday junction. A steric exclusion is a statement about bodies not otherwise held.

> **`CLAUDE.md`'s entry *"the phosphate radius in B-DNA is 10 Å, which IS the duplex's steric radius"* should be read with this repository's own 0.9086 ± 0.0665 nm beside it** — that is the `CLAUDE.md` amendment this task files.

---

## Deliverable 5 — the five claims that inherit the convention

| claim | role it uses | standing verdict | under the weave | moves? |
|---|---|---|---|---|
| **`C-0041`** | collinear + row pitch | 0 of 720 orientations; the tile carries exactly 15 | **unchanged** — Fact B is 2.59× and no width in the bracket moves it; **Fact A weakens** to a 47.5° threshold | **no** |
| **`C-0053`** | collinear | 43 of 45; 0 surviving crossovers | **unchanged at every width**, 43 throughout | **no** |
| **`C-0065`** | row pitch + body width | all 44 trios place 34 times; the register is what costs | **unchanged, and REINFORCED** — the one helical phase class is also one weave phase class | **no** |
| **`C-0066`** | collinear — bound 4, the tie in the gap | *"the gap clears a duplex by 0.0256 nm"* | the gap clears a **tie's own measured girth** by **0.89833 nm** | **YES** — `CH-0089` |
| **`C-0069`** | collinear — `Q5`, the row-of-three ceiling | budget 8.19 nm, margin 0.0256 nm | budget **9.06272 nm**, margin **0.89833 nm**; the **count is unchanged at 34** | **YES** — `CH-0089` |
| `C-0072` | the weave read through `M` | the weave brackets the verdict +0.866 → −0.884 nm | **the coefficient is exactly zero** | **YES** — `CH-0088` |

**No placed count moves.** What moves is a **margin** — and it is the margin `C-0072` built four floors and a whole tolerance model on.

---

## The five verification gates

Executed as **20 gate-named tests** in `src/test/kotlin/anchoring/WeaveExclusionWidthTest.kt`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a weave distance is a length and scales with every length, over 41 positions; an axis offset is a length and a zero-amplitude weave carries none; unphysical arguments throw at **ten** entry points, including a peak-to-peak larger than twice the mean, a zero rise, a zero plane spacing and a negative clearance function | **PASS** |
| **2 — limiting cases** | **THE FREE LIMITING CASE — a constant clearance function reproduces `C-0063`'s `armDirections` EXACTLY**, at 5 widths × 4 lengths × 15 rows; a zero-amplitude weave is the lattice constant at 121 positions on 14 interfaces; the minimum sits at the interface's own crossovers and the maximum at its neighbours', at 17 periods on every interface; `C-0069`'s 8.19 nm budget and 0.0256 nm margin reproduce | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the weave-substituted placed count is identical with `x` snapped at **0.1, 0.01, 0.001 and 0.0001 nm**; the weave is Lipschitz with constant `Δ/(2·planeSpacing)` and a snapped evaluation converges **linearly**, asserted at three resolutions over 4001 positions; a station's plane coordinate is an **integer**, so its weave value needs no grid at all (departure `0.0`); **the placement threshold BISECTED in the width, exiting on the bracket at `1e−12` nm, reproduces the closed form `pitch − arm`** — the closed form is not a fit and this is its falsifier | **PASS** |
| **5 — literature and upstream** | **10 reproductions, and every one of the four counts is exact**: `C-0069`'s 8.19 (`0.0`), its margin 0.0256 (`9.2e−6`), its 34 at 2.69 (`0.0`) and **its 18 at 2.73 through its own pipeline (`0.0`)**; `C-0066`'s 2.71561 (`8.3e−7`); `C-0053`'s **43** (`0.0`); `C-0063`'s **34** (`0.0`); `C-0072`'s +0.866 and −0.884 (`3.9e−4` each, its own rounding); **Bai's sawtooth midpoint against Fischer's square-lattice Bragg constant, 2.725 against 2.73 — 0.18 %, two methods on the same lattice type, and nothing forces it**; the measured phosphate contact asserted to admit the measured weave minimum | **PASS** |

### Gate 3 — five things that are not restatements of the construction

1. **The congruence at every phase.** All upward sites at all 32 phases sit on odd planes, with `|axis offset| ≤ 1e−12` and `|D − a| ≤ 1e−12` on both bounding interfaces. The construction imposes the *azimuth* rule and the *triangular* shape separately; that their parities are complementary is not imposed anywhere.
2. **A single per-duplex zig-zag reproduces the per-interface profile on all 14 interfaces simultaneously**, asserted at 201 positions — `y_{b+1}(x) − y_b(x) = D_b(x)` to `1e−12`, from two independently written functions.
3. **The amplitude has coefficient exactly zero at the stations**, swept `0 → 2.5 nm`, `≤ 1e−14` nm.
4. **The lattice mean is conserved**: the four plane offsets of one period sum to `0.0` for every duplex, and the weave integrates to the lattice constant over its own period (a triangular wave sampled commensurately integrates exactly, `≤ 1e−12`).
5. **Parity is a design variable**: an arm of an **odd** number of planes puts its tip at an antinode and an **even** one at a node, asserted at the maximum and at the mean.

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **F1** | **the weave's coefficient on the plan margin is non-zero**, so `C-0072`'s bracket stands and the verdict really is undecided | **NO** | zero categorically (wrong axis, wrong bonding) **and** zero numerically (every station a node) |
| **F2** | the stations do not sit at a fixed weave phase, so the value is a distribution | **NO** | 34 of 34 on a node, at all 32 phases, `4.4e−16` nm |
| **F3** | the measured phosphate radius does not admit 18.5 Å, so the interpenetration is real | **NO against Bai (+0.35 σ), and it grazes against Yoo (−0.18 σ)** — reported rather than suppressed; the honest statement is that the weave minimum and the steric floor coincide **to within one σ** |
| **F4** | a position-dependent width changes a placed count | **NO in the weave's own role** (34 either way) — **and YES if the weave is substituted into the collinear slot**, which is the challenge and not the result |
| **F5** | the weave's mean is not the lattice constant | **PARTLY — and this is the surprise.** Bai's midpoint reproduces Fischer's *square-lattice* constant to 0.18 %, but **oxDNA's own mean for a 2D tile is 3.25 nm, 21 % above the SAXS single-layer 2.69 nm** it is supposed to be a mean of. The mean is a **live disagreement** and it is on the same side of the threshold as the weave maximum |

**What was not anticipated:** the amplitude — the thing the task was raised to price — turns out to be the *best-behaved* of the three weave parameters at this design, and the **mean**, which nobody thought was in question, is the one with a 1.25× spread that straddles the placement threshold.

---

## Validity range

- **TRL 1–3, and the motif is not demonstrated.** `C-0055`'s and `C-0029`'s findings are unchanged and upstream of everything here.
- **The weave is measured on other objects at other ionic strengths.** Bai's is a **multilayer** square-lattice brick in vitrified buffer; Snodin's is an oxDNA **2D tile** at `[Na⁺] = 0.5 M`; Yoo's is all-atom MD. **None is a single-layer sheet at 2 mM MgCl₂.** Snodin's is the closest object and its amplitude is the default here; Snodin also reports that removing the electrostatics reduces the oscillation by only ~20 %, which is the only handle on the salt transfer and it is weak.
- **The node congruence is a property of the LATTICE, not of the amplitude or the salt** — it survives any symmetric periodic weave whose extrema are the crossover planes, which is exactly what Snodin states. It would fail if the weave were **asymmetric** between the two half-periods (an interface whose two bounding crossover columns are not equivalent), and the seam of a Rothemund rectangle is precisely such a place: Snodin reports that *"one group of double-helix pairs has a particularly large section without any junctions and so opens up to the largest extent here"*. **The seam is not modelled and no station is checked against it.**
- **`WeaveProfile` is a two-parameter model of a measured curve, not a solve.** It carries a mean and a peak-to-peak and asserts a triangular shape; the shape is Snodin's own word and the extrema locations are his own sentence, but no residual against his Figure 3 has been computed, because the figure's data are not published as numbers.
- **The measured phosphate radius is a B-form C2′-endo *population* value from crystal structures**, and a crystallographic contact distance is not the same as an in-solution exclusion width at 2 mM MgCl₂ — electrostatics will hold two unbonded duplexes further apart than steric contact. **1.8173 nm is therefore a FLOOR on the exclusion width and not an estimate of it**; that is why this claim reports a straddled threshold rather than a new margin.
- **The counter-reading is stated rather than buried**: the same weave measurement says crossover-bonded duplexes splay to **3.60 nm** where nothing pins them, which argues the free-body separation **up** as readily as the steric floor argues it **down**. What is *measured* is the floor.
- **`C-0069`'s 18 of 34 is reproduced exactly and is not withdrawn.** Its arithmetic is correct; what this claim adds is which of three roles the 0.04 nm was applied to.
- **The edge-duplex reading is a model, not a measurement.** Snodin excludes those helices; the halved-amplitude straight-edge reading here is a construction, and it is carried only to show the node congruence is not weakened at the edges.
- **No flatness, stiffness or force number is touched.** This claim moves a plan margin and no load path.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| weave minimum / maximum | 1.85 / 3.60 nm | **CITED, MEASURED** (cryo-EM, Bai et al., *PNAS* **109**:20012, 2012), **re-fetched and read directly for this task** |
| weave peak-to-peak on a **2D tile** | 1.5 nm | **CITED, SIMULATED** (oxDNA, Snodin et al., *NAR* **47**:1585, 2019), **re-fetched and read directly** |
| weave phase rule (minima at the crossovers, maxima at the adjacent pair's crossovers, period 32 bp) and the triangular shape | — | **CITED, READ DIRECTLY** (Snodin et al.) — the load-bearing input, verbatim in `gpd/data/T-137-weave-literature.md` |
| oxDNA 2D-tile weave mean | 3.25 nm | **CITED, SIMULATED, meaning INFERRED** — the paper prints Å where it means nm in that sentence |
| all-atom weave window | 1.80–3.00 nm | **CITED via `gpd/data/T-134-tolerance-literature.md`, NOT re-fetched by this task** |
| interhelical distances | 2.69 nm sheet, 2.73 nm square | **CITED, MEASURED** (SAXS, Fischer et al., *Nano Lett.* **16**:4282, 2016) |
| rise, crossover interface spacing, the four azimuths at 8 bp | 0.34 nm, 32 bp | **CITED** (Rothemund 2006, Ke et al. 2009, via `C-0015`/`C-0055`) |
| asserted duplex steric diameter | 2.0 nm | **CITED** (`CLAUDE.md`, from Hedley et al. 2024) — and **challenged here against this repository's own measurement** |
| B-form phosphate radius | 0.908638 ± 0.066499 nm | **MEASURED, THIS REPOSITORY** (`T-71`, 13 084 crystallographic linkages) |
| the 34 stations, the phase, the 10.88 nm pitch, the 8.16439 nm arm, the 9.131 nm `E5a1` | phase 24 | **`C-0063`/`C-0055`/`C-0039`/`C-0053`, CONSUMED AS DATA and re-run** |

Everything else — the parity argument, the zig-zag model and its consistency, all 34 station records, all 32 phase records, the nine width readings and their margins, the placement threshold, the three-way decomposition of `C-0069`'s 18, Fact A's tilt thresholds, the steric adjudication and its σ, and the four convergence records — is **derived here in code**.

## Still open — named, not answered

1. **The mean, not the amplitude.** oxDNA's 3.25 nm for a 2D tile is 21 % above the SAXS 2.69 nm, and it is on the far side of the placement threshold. Nothing in this programme reconciles a simulated interhelix distance with a measured Bragg constant, and the disagreement is now the binding uncertainty on every plan verdict in the branch.
2. **The seam.** Snodin reports that a Rothemund rectangle's scaffold seam gives one group of helix pairs *"a particularly large section without any junctions"* which *"opens up to the largest extent"*. That is exactly the asymmetric case in which the node congruence would fail, and no station has been checked against a seam because this programme's sheet model has none.
3. **An exclusion width for two unbonded duplexes in 2 mM MgCl₂.** The measured floor is 1.817 nm and the equilibrium separation is set by a force balance no plan model contains. A single Poisson-Boltzmann or oxDNA pair calculation would supply it, and it decides `Q5` outright because the threshold is 2.7156 nm.
4. **The weave under compression.** Every measurement here is of an unloaded sheet. `C-0022`'s solved load and the 3 nm stroke are not in it.
5. **Whether `C-0063`'s placement should be re-optimised at an even plane count.** A 16 bp or 32 bp element puts both ends on nodes; the current 24 bp arm does not, and nothing has priced what the antinode at the tip costs the tie registration `C-0066` depends on.

## Challenges

**Raises [`CH-0088`](../challenges/CH-0088-the-weave-read-through-the-plan-margin-is-on-the-wrong-axis.md)** against `C-0072`'s Deliverable 7 weave table, and **[`CH-0089`](../challenges/CH-0089-the-collinear-clearance-is-a-girth-not-a-lattice-constant.md)** against `C-0066`'s bound 4 and `C-0069`'s `Q5`.

**None stands against this claim.** The five ways it would fail:

1. **A measured weave that is not symmetric about the odd planes** — an asymmetric or non-triangular profile would move the node off the station. Snodin's own seam is the candidate, and it is open item 2.
2. **A demonstration that the collinear clearance is a lattice quantity after all** — i.e. that two collinear elements above the sheet are held apart at a lattice constant by something. Nothing in the plan model supplies such a mechanism, but nothing rules one out either.
3. **An exclusion width for two unbonded duplexes above 2.7156 nm.** Then 34 becomes 22 and `C-0069`'s `Q5` fails outright, at the *stiff* end rather than the knife edge — which is the direction the oxDNA mean points.
4. **A single-layer measurement of the interhelical distance disagreeing with 2.69 nm.** The whole verdict is a step function at the third digit of this constant.
5. **A crossover model in which the two duplexes are held at 18.5 Å by something other than contact.** Then Deliverable 4's coincidence is a coincidence and the adjudication survives on the fibre bracket alone, which is weaker but still sufficient.
