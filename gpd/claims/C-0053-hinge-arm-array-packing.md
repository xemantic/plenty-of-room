# C-0053 — `C-0041`'s verdict does NOT transfer and the array still fails: 45 `E5a1` arms clear each other as bodies at 0.685 of the footprint, but a hinge must root on a CROSSOVER, a row's roots sit at 5.44 nm against an arm needing 11.82, so the tile places **43** — and at 43 the host sheet has **no bonded component left at all**; the count that leaves it whole is **25**

| | |
|---|---|
| **Task** | [`T-116`](../tasks/T-116-hinge-arm-array-packing.md), `C-0050`'s own open item 1 and the item it names *"the first thing to run against `E5a1`"* |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme |
| **Verification type** | **logical** (exact plane geometry and an exact combinatorial placement on measured lattice constants — no mesh, no free parameter) **+ in-silico** (`C-0039`'s elastica placement re-run as a library at seventeen path counts, `C-0015`'s `CrossoverLayout` re-run over the complete 32 bp phase space, and `C-0041`'s packer reproduced as a **configuration of this one**) |
| **Verdict** | **PASS, and the answer is NO — but for a different reason than `C-0041`'s, and the reason that decides it is not a packing at all.** Both of `C-0041`'s obstructions **dissolve**: `E5a1` owns **no vertical member**, so the level-independent standoff/tie clash that killed the flexure array cannot arise at any tilt (0 blocking pairs, 0 clashes, at every orientation); and `arm + d = 11.82 nm` sits **under** the 13.33 nm three-column pitch where `C-0041`'s `span + d = 34.51 nm` sat 2.59× over it. As **bodies** the 45 arms pack, at 0.685 of the footprint and at 2 of 720 orientations — exactly `θ = 0` and `θ = π`, the sheet's own helix direction, a rooted arm's two senses. **What refuses them is the hinge lattice.** A hinge is one crossover of the host's own sheet, a row can only root on the crossovers of its **two bounding interfaces**, and those sit at **5.44 nm** (16 bp) on an interior row and **10.88 nm** on an edge row — against an arm demanding **11.82 nm** of clearance. So a row carries **three** arms and an edge row **two**, and the tile places **43 of the 45 demanded**, at the best of all 32 phases — and **43 is exact, not a search artefact**: the constructive placement equals the independent per-row upper bound. **The phases that maximise it are exactly `C-0015`'s ten centro-symmetric eight-column ones**, 6–10 and 22–26 bp, which is `C-0040`'s coincidence in a third place. **And the two missing arms are the least of it.** Each arm is a length of the host's own duplex cut free at both ends, so it removes **65.4 %** of the host's duplex length, spends **43** crossovers as hinges and **buries 13 more** under an arm — **56 of an inventory of 56, exactly 100 %** — leaving **zero** surviving crossovers and **no bonded component whatever**. The self-consistent count is **43** (the arm is a placed quantity, `L ∝ n^(1/3)`), and at 43 the host is already dust. **The count that leaves every one of the 15 duplexes bonded into one piece is 25**, at a 7.236 nm arm — **1.80× below §3's 45** — and the collapse above it is steep: 15 bonded duplexes at 25 arms, 14 at 30, 8 at 35, 3 at 40, **none at 42**. **45 arms never place, at any of five swept axes**, including the reading in which the arm's own root is a **double nick as well as a crossover** (30 place). One thing does improve on `C-0041`: the lattice placement carries **three columns**, exactly `C-0047`'s flatness break-even, so this array is **not** a net dishing source where `C-0041`'s 1 × 15 is 2.26× worse than no coupling at all. Raises [`CH-0065`](../challenges/CH-0065-the-hinge-arm-array-has-no-plan-view-either.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED, and the motif is NOT DEMONSTRATED** — `C-0028`'s and `C-0029`'s literature findings are unchanged and upstream of every number. |
| **Provenance** | `gpd/results/T-116-hinge-arm-array-packing.json`, produced by `anchoring.HingeArmArrayPackingStudyKt`; model in `src/main/kotlin/anchoring/HingeArmArrayPacking.kt`; **6 cheap bounds, 7 orientation sweeps of 720 samples each, 16 layout records, 32 phase records, 15 row records, 43 explicit placements, 17 count records, 5 sensitivities, 7 thresholds, 14 convergence records, 14 upstream reproductions**; **33 tests, 29 of them gate-named, in `src/test/kotlin/anchoring/HingeArmArrayPackingTest.kt`**; `tools/verify.sh` **BUILD SUCCESSFUL** on its own isolated tree with one concurrent agent's mid-TDD test file dropped by `--drop-file` (`src/test/kotlin/anchoring/CrossbarJunctionTrioTest.kt`, `T-117`); the result file re-run through `tools/study.sh` and diffed **byte-for-byte identical** |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40.0 × 40.35 nm tile, **15 duplexes** at the SAXS-measured 2.69 nm, footprint **1614 nm²**; single-layer **square-lattice** Rothemund sheet, 32 bp per interface; §3's 100 pN at the **acceptable** 3 nm; `C-0017`'s 33.3333 pN/nm mandate; `EI` = 230 pN·nm² (and Fields et al.'s implied 172.906 swept), `k_θ` = 13.5294 pN·nm/rad (and `α = 0.6` swept), `C-0034`'s `A2` anchorage at 78.2353 pN·nm/rad |
| **Consumes** | [`C-0039`](C-0039-two-spring-elastica.md) (`TwoSpringElastica`, `elasticaArmForStiffness` — **re-run as a library** at seventeen path counts; its `E5a1` arm 9.131 nm and tangent 39.18 pN/nm reproduced), [`C-0041`](C-0041-flexure-array-packing.md) (`PlanRectangle`, `rectanglesOverlap`, `packingVerdict`, `gridFlexureArray`, `coupledFlexureSpan` — its packer **reproduced as a configuration of this one**, and its 15, its 0 of 720 and its 1 of 720 recovered), [`C-0040`](C-0040-hinge-line-census.md) (the 32 bp per-interface pitch, the four-crossover census, the 49–56 inventory, the *"every crossover is already a structural load path"* finding), [`C-0015`](C-0015-crossover-phase-and-registration.md) (`CrossoverLayout`, the phase as a design variable, the parity rule, the 3 × 15 grid), [`C-0034`](C-0034-guided-arm-anchorage.md)/[`C-0029`](C-0029-perpendicular-junction-routing.md) (`ArmAnchorage.twoTerminus`, `BForm`, the counting theorem), [`C-0050`](C-0050-desired-stroke-reach.md) (the `E5a1` row and its `packingAssessed = false`), [`C-0049`](C-0049-compliance-ceiling-stroke.md) (**the ceiling read at the stroke it is owed at**), [`C-0047`](C-0047-single-column-flatness.md) (the three-column break-even, **CITED**), [`C-0026`](C-0026-one-row-per-duplex.md), [`C-0025`](C-0025-flexure-end-joint.md) (`J2b`: a double nick **is** a crossover), [`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile` |
| **Raises** | [`CH-0065`](../challenges/CH-0065-the-hinge-arm-array-has-no-plan-view-either.md), against `C-0050`'s `E5a1` catalogue row and, by inheritance, `C-0039`'s and `C-0023`'s *"45 load paths"* premise for the whole `E5` family |

---

## The claim, in one line

**`C-0041` refused an array because two of its members could not stand up; `E5a1` has no such members and its bodies pack, so the question moves to where the load path actually enters the sheet — and a crossover-hinged arm can only be rooted where a crossover is, which is a 5.44 nm lattice against an 11.82 nm demand, giving 43 of 45; and the deeper answer is that an arm is not something added to the sheet but something cut out of it, so the array that does place consumes 65 % of the host's duplex length and every one of its 56 crossovers, and leaves nothing bonded at all.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, areas **nm²**; `k_BT = 4.141947 pN·nm` at **300 K** in aqueous **2 mM MgCl₂**.
- **Plan.** `x` runs **along** the host sheet's helices, `y` **across** them, origin at the tile centre. `z` is positive **upward**; §1's bias pulls the tile **down**.
- **A duplex in plan is a rectangle of width `d = 2.69 nm`** — the SAXS single-layer interhelical distance — so two parallel duplexes at exactly `d` are **tangent and admissible**. `C-0041`'s convention verbatim, and deliberately the loosest defensible one; the 2.0 nm steric reading is swept and moves nothing.
- **A hinge-line arm occupies one rectangle `arm × d`, ROOTED at its hinge and not centred on it**, because the load path enters the host at the crossover. That single change of anchor is what makes `θ` and `θ + π` **different designs**, so the orientation sweep runs over `[0, 2π)` where `C-0041`'s ran over `[0, π)`.
- **`E5a1` owns NO vertical member.** Its near end is a crossover *in the host's own plane* and its far end is `C-0034`'s two-link `A2` joint. **This is the premise that does not transfer**, and it is stated rather than assumed.
- **A hinge site is a crossover of the host's own lattice.** Interface `b` carries the columns of parity `b mod 2` (`C-0015`), so a row's roots are the union of its two bounding interfaces' columns: **both** parities at a 16 bp = 5.44 nm pitch for an interior row, **one** at 32 bp = 10.88 nm for an edge row. Nothing imposes that asymmetry; it falls out of the alternation.
- **A buried crossover is deleted.** A crossover lying under an arm on one of that arm's own two interfaces would tie a free lever back to the sheet, so it is charged against the inventory as spent.
- **An arm is cut free at BOTH ends.** Its tip is a duplex end (that is what `A2` is) and its root must be discontinuous from the rest of its own row, because a single nick is a **clamp** (`C-0025`, `CLAUDE.md`). So an arm is a length of the host's duplex removed from the host's load path.
- **A residual piece shorter than one duplex diameter (2.0 nm) is TRIM, not a component** — it is the stub left where an arm roots on the lattice site nearest the edge, and a design does not build it. Counting it as a disconnected piece would report a placement artefact as a structural failure.

---

## The cheap bounds, which ran first and moved the expensive part twice

| | bound | value | against | ratio | what it settled |
|---|---|---|---|---|---|
| **1** | **plan area of 45 arms** | 1105 nm² | 1614 nm² | **0.685** | **falsifier 1 did not fire.** `C-0041`'s own was 2.59× and invited *"stack it"*; this one says the area is not the question |
| **2** | **`C-0041`'s Fact B, on this arm** | `arm + d` = **11.82 nm** | 13.33 nm pitch | **0.887** | **the along-helix obstruction REVERSES** |
| **2′** | the same, on `C-0041`'s own element | 34.51 nm | 13.33 nm | 2.588 | for contrast — this is what does not transfer |
| **3** | **`C-0041`'s Fact A**: vertical members per element | **0** | 3 | **0** | **vacuous.** No standoff, no tie, no level-independent clash |
| **4** | **the hinge-site pitch against the arm's demand** | 5.44 nm | 11.82 nm | **0.460** | **the binding currency is the crossover lattice.** A row must skip two sites out of every three |
| **5** | 45 hinges against the whole inventory | 45 | 56 | 0.804 | `C-0040`'s ledger, **before** anything is buried |

> Bounds 1–3 say `C-0041`'s answer does not transfer. Bound 4 says where the expensive part has to go, and it is not the area and not the level count — it is **the lattice**. Same shape as `C-0041`'s own cheap bound, and the opposite conclusion.

---

## Deliverable 1 — as bodies, the array packs, and the reason is precisely the two facts that fail

| element | columns | paths | length | single-level orientations of 720 | minimum overlaps | mutual blocks | clashes |
|---|---|---|---|---|---|---|---|
| **`E5a1` arm** | 1 | 15 | 9.131 | **2** | **0** | **0** | **0** |
| **`E5a1` arm** | 2 | 30 | 9.131 | **2** | **0** | **0** | **0** |
| **`E5a1` arm** | **3** | **45** | **9.131** | **2** | **0** | **0** | **0** |
| `E5a1` arm | 4 | 60 | 9.131 | **2** | 0 | 0 | 0 |
| `C-0041` flexure | 1 | 15 | 21.44 | **1** | — | 0 | 0 |
| `C-0041` flexure | 2 | 30 | 27.51 | **0** | — | 15 | 0 |
| **`C-0041` flexure** | **3** | **45** | **31.82** | **0** | — | **45** | 0 |

**The two feasible orientations are `θ = 0` and `θ = π`** — the sheet's own helix direction, in its two senses. `C-0041`'s single-column array was feasible at **one** of 720; a rooted arm doubles that, and for a reason rather than by accident: the sweep is over the full circle because the element has a direction.

**Layouts at 0°, 5°, 90°, 180°.** At three and four columns the bodies clear each other in one level and the array **overhangs the host's edge**, because a rooted arm in the outermost column of `C-0015`'s half-offset grid runs 2.46 nm past `x = +20`. On the grid **as inherited** the arm array is therefore limited to **2 columns = 30 paths** rooted (and 4 columns = 60 centred). That is a property of the inherited grid, not of the element — and the lattice placement below settles it properly, at **three** arms per interior row.

---

## Deliverable 2 — the hinge lattice, which is what actually refuses the array

**A hinge is a crossover, so an arm can only be rooted where one is.**

| | interior row | edge row |
|---|---|---|
| bounding interfaces | **2**, of opposite parity | **1** |
| root sites on a 40 nm edge | **8** | **4** |
| site pitch | **5.44 nm** (16 bp) | **10.88 nm** (32 bp) |
| an arm's demand, `arm + d` | 11.82 nm | 11.82 nm |
| **arms carried** | **3** | **2** |

&nbsp;&nbsp;&nbsp;&nbsp;**13 interior rows × 3 + 2 edge rows × 2 = 43, against the 45 demanded.**

**43 is exact.** The constructive placement — an exact per-row interval schedule, with the crossovers a previous row has already consumed removed from the next row's candidate set, run in both row orders — reaches **43**, and the per-row maxima solved **independently**, which ignore the sharing constraint and are therefore a strict upper bound, also total **43**. Construction meets bound, so no better placement exists.

| phases | inventory | arms placed | independent bound | hinges | buried | demanded | of inventory |
|---|---|---|---|---|---|---|---|
| **6–10, 22–26 bp** (10 phases) | **56** | **43** | **43** | 43 | 13 | **56** | **1.00** |
| 1–5, 11–15, 27–31 bp (15) | 49 | 36 | 43 | 36 | 12 | 48 | 0.98 |
| 17–21 bp (5) | 49 | 36 | 43 | 36 | 13 | 49 | **1.00** |
| 0, 16 bp (2) | 49 | **30** | 30 | 30 | 6–7 | 36–37 | 0.73–0.76 |

> **The phases that place the most arms are exactly `C-0015`'s ten centro-symmetric eight-column phases**, 6–10 and 22–26 bp. `C-0040` found the same ten maximise a hinge line and recorded that *"nothing in either construction forced it"*; this is a **third** construction landing on them, and it is asserted as a gate test rather than observed in a table.
>
> **Falsifier 3 (the placement reaching 45 at some phase) did not fire**, at any of the 32.

**The two missing arms are the two edge rows**, and the reason is `C-0015`'s alternation: an edge duplex has one neighbour, therefore one interface, therefore one parity, therefore twice the root pitch. It is not a boundary effect that a bigger tile would dilute — it is exactly two arms on any 15-duplex sheet.

---

## Deliverable 3 — the count is a fixed point, and the fixed point is not the answer either

The arm is a **placed** quantity (`C-0039`): `L ∝ n^(1/3)`, so fewer paths ask for a shorter arm and a shorter arm places more easily. The count the lattice carries is therefore solved self-consistently, exactly as `C-0041` solved its own fifteen.

| paths | arm [nm] | bp | placed | self-consistent | crossovers demanded / inventory | surviving | **duplexes still bonded** | per path [pN] | assembled tangent |
|---|---|---|---|---|---|---|---|---|---|
| 10 | 5.163 | 15.2 | 56 | **yes** | 10 / 56 | 46 | **15** | 10.00 | 56.80 |
| 15 | 5.963 | 17.5 | 56 | **yes** | 15 / 56 | 41 | **15** | 6.67 | 49.15 |
| 20 | 6.641 | 19.5 | 52 | **yes** | 25 / 56 | 31 | **15** | 5.00 | 45.41 |
| **25** | **7.236** | **21.3** | **43** | **yes** | **25 / 49** | **24** | **15** | **4.00** | **43.18** |
| 30 | 7.770 | 22.9 | 43 | yes | 30 / 49 | 19 | 14 | 3.33 | 41.69 |
| 35 | 8.259 | 24.3 | 43 | yes | 42 / 49 | 7 | **8** | 2.86 | 40.62 |
| 40 | 8.710 | 25.6 | 43 | yes | 47 / 49 | 2 | **3** | 2.50 | 39.81 |
| 42 | 8.882 | 26.1 | 43 | yes | **49 / 49** | **0** | **0** | 2.38 | 39.54 |
| **43** | **8.966** | **26.4** | **43** | **yes** | **49 / 49** | **0** | **0** | 2.33 | 39.42 |
| **45** | **9.131** | **26.9** | **43** | **NO** | **56 / 56** | **0** | **0** | 2.22 | **39.18** |
| 50 | 9.527 | 28.0 | 43 | no | 56 / 56 | 0 | 0 | 2.00 | 38.67 |
| 60 | 10.255 | 30.2 | 30 | no | 36 / 49 | 13 | 14 | 1.67 | 37.90 |

Three readings, and the third is the one that matters.

1. **The self-consistent packing-limited count is 43.** Two short of §3's 45, which on its own reads like a rounding.
2. **It is not a rounding, because 43 destroys the host.** At 42 and above the array spends or buries **every** crossover the sheet has, so there is **no bonded component at all** — the residual is 44 detached pieces and 14 trimmed stubs.
3. **The count that leaves the host whole — all 15 duplexes bonded into one component — is 25**, at a 7.236 nm arm. **1.80× below §3's 45 and 1.72× below the 43 the lattice would place.** The collapse above it is steep and monotone: **15 → 14 → 8 → 3 → 0** bonded duplexes at 25 → 30 → 35 → 40 → 42 arms.

> **So the threshold is not `45 → 43`. It is `45 → 25`, and the binding constraint is the host sheet's own survival**, which no packing model would have seen and which `C-0040`'s inventory ledger under-counts by the buried crossovers (11–13 of them) and by the duplex length (65.4 % of it).

**At 25 paths the assembled tangent is 43.18 pN/nm**, 1.08× `C-0023`'s **declared** 40 pN/nm ceiling — and [`C-0049`](C-0049-compliance-ceiling-stroke.md), filed one iteration earlier, withdraws that ceiling and replaces it with the per-path allowable read as a stiffness, `n·allowable/s` = **83.3 pN/nm** at 25 paths. **So the surviving design fails the ceiling that has been withdrawn and clears the one that replaced it**, and its per-path force, 4.00 pN, is 2.5× inside the 10 pN unzip allowable. Nothing here is an allowable failure.

---

## Deliverable 4 — the sensitivity sweep: 45 never place, on any axis

| axis | reading | arm [nm] | arms placed | 45 place? |
|---|---|---|---|---|
| **hinge count** | **1 — `C-0039`'s `E5a1` as filed** | **9.131** | **43** | **no** |
| **hinge count** | **2 — the arm's root is a DOUBLE NICK as well as a crossover** | 9.985 | **30** | **no** |
| duplex `EI` | Fields et al.'s implied 172.906 pN·nm² (−25 %) | 8.795 | 43 | **no** |
| crossover `α` | 0.6, the bottom of Chen et al.'s fitted bracket | 8.694 | 43 | **no** |
| exclusion width | the 2.0 nm steric diameter rather than 2.69 nm SAXS | 9.131 | 43 | **no** |

> **The second row is a finding, not a sensitivity.** An arm cut free at its root is **doubly nicked** from the rest of its own row, and `C-0025`'s `J2b` establishes that *a double nick IS a crossover* — two softened bonds in parallel, 13.53 pN·nm/rad, the same constant. So `E5a1`'s near stiffness may be **`2 k_θ` at no extra crossover**, which is favourable for the *mechanics* and **unfavourable for the packing**: the placed arm lengthens to 9.985 nm and the lattice places **30**. It is named here and **not adopted** — it is a mechanics question for `C-0039`, not a plan-view one.

---

## Deliverable 5 — the flatness consequence, which is the one thing that improves

`C-0047` establishes that the flatness break-even is at **three attachment columns**: at 1 × 15 the coupling dishes **0.695** of the stroke against **0.308** for no coupling at all — **2.26× worse than nothing** — and at 3 × 15 it dishes **0.218**.

&nbsp;&nbsp;&nbsp;&nbsp;**The lattice placement carries three arms per interior row, so the arm array meets that break-even where `C-0041`'s does not.**

| | `C-0041`'s flexure array | **this arm array** |
|---|---|---|
| attachment columns the packing admits | **1** | **3** (2 on the two edge rows) |
| `C-0047`'s dishing at that shape | 0.695 of the stroke | **0.218** (CITED, for a regular 3 × 15) |
| against `T-5b`'s 10 % convention | 7.0× | **2.2×** |
| against the uncoupled tile's 0.308 | **2.26× worse than nothing** | **1.41× better than nothing** |
| net dishing source? | **yes** | **no** |

**Falsifier 5 fired.** This is the one axis on which the hinge arm is better than the standoff flexure, and it is worth recording because it is the axis `C-0041`'s own open item 1 raised and `C-0047` answered in the negative. **It does not rescue the element** — the array still does not place, and the host does not survive it — but a future element with the same footprint would inherit the good news.

**The 0.218 is CITED from `C-0047` and not re-derived here**, and it is quoted for a **regular** 3 × 15 grid: the placement solved here is 43 irregular points (three per interior row, two per edge row, at lattice-quantised `x`), which `C-0047`'s pipeline has not been run on. The direction is unambiguous; the number is nearest-neighbour.

---

## The plan view itself

The result file carries **all 43 placements** — row, root `x`, direction, interface index, and the arm's footprint — at the best phase (6 bp, `C-0015`'s centro-symmetric eight-column lattice). The first three rows of it, to fix the picture:

| row | root `x` [nm] | runs | interface | footprint `[low, high]` |
|---|---|---|---|---|
| 0 (edge) | −19.72 | +x | 0 | [−19.72, −10.59] |
| 0 (edge) | +2.04 | −x | 0 | [−7.09, +2.04] |
| 1 | −8.84 | −x | 0 | [−17.97, −8.84] |
| 1 | −3.40 | +x | 1 | [−3.40, +5.73] |
| 1 | +18.36 | −x | 1 | [+9.23, +18.36] |
| 2 | −19.72 | +x | 2 | [−19.72, −10.59] |
| 2 | +2.04 | −x | 2 | [−7.09, +2.04] |
| 2 | +7.48 | +x | 1 | [+7.48, +16.61] |

Note that a row uses **both** of its bounding interfaces and both root senses — that is what buys the third arm, and it is why the sweep had to run over the full circle.

---

## The five verification gates

Executed as **29 gate-named tests** (of 33) in `src/test/kotlin/anchoring/HingeArmArrayPackingTest.kt`; `tools/verify.sh` **BUILD SUCCESSFUL** on its own isolated tree with a sibling's mid-TDD test file dropped.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | an arm's plan area is a length squared and the array's is additive and **doubles exactly** with the arm; the whole packing verdict is **dimensionless** — scaling every length by 10 leaves the overlap, block, clash and level counts identical at five orientations; unphysical arguments throw at ten entry points, including a negative arm, a zero width, an anchor fraction outside `[0, 1]`, a vertical member outside it, an empty array and a one-duplex sheet | **PASS** |
| **2 — limiting cases** | **`C-0041`'s packer is reproduced exactly** as a configuration of this one — every one of its six verdict fields, at three column counts × five angles; one arm packs at all 36 orientations; two collinear arms overlap below the arm and clear above `arm + d`; two arms one duplex apart across the helices are **tangent and admissible** at every offset; a row with one site carries one arm and a **cluster narrower than one arm carries two**, one each way, which a one-directional schedule puts at one; an unarmed sheet is **one** component at every phase with its whole inventory intact | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the orientation sweep is **sample-count independent** over 180 → 2880; the phase sweep is **complete at 32** — refining it ten-fold produces no count the base-pair phases do not contain; the placed arm is **RK4-step independent** 100 → 1600 at 1.5e−6; the placement is **deterministic** on repeat calls; the result file re-run through `tools/study.sh` and diffed byte-for-byte identical | **PASS** |
| **5 — literature and upstream** | **`C-0039`'s `E5a1` arm 9.131 nm (1.7e−5) and tangent 39.18 pN/nm (4.2e−5)**; `C-0017`'s mandate discharged to 6.4e−16; `C-0009`'s `k_θ` = 13.5294118 (2.6e−9); `C-0029`'s two-terminus couple 78.2352941 (2.3e−10); `C-0030`'s span 31.82 nm (2.9e−5); **`C-0041`'s 15, its 0 of 720 and its 1 of 720, all exactly**; **`C-0040`'s four-crossover hinge line at every one of the 32 phases, exactly**; **`C-0015`'s 56 at ten phases and 49 at the other twenty-two, exactly**; the SAXS 2.69 nm, the 0.34 nm rise and the 10.88 nm interface pitch. **Worst departure over 14 reproductions, excluding the values their own claims quote rounded to four digits: 2.6e−9** | **PASS** |

### Gate 3 — five things that are not restatements of the construction

1. **The verdict is invariant under a rigid rotation of the whole array** — every arm turned together about an off-centre pivot by 0.17, 0.9, `π/2` and 2.0 rad, at three internal orientations. Nothing in a separating-axis test forces that.
2. **The two interface parities' site counts sum to the column count at every one of the 32 phases** — `C-0015`'s conservation law, recovered from a construction that counts sites rather than columns.
3. **A 32 bp shift of the column lattice is the identity and a 16 bp shift hands every interface the other parity's columns** — `C-0015`'s *"the period is `p`, not `p/2`"*, which is what makes the phase sweep complete rather than half of one.
4. **Every placed arm hinges on one of its own two interfaces, injectively**, at every phase — asserted, not assumed, and it is the constraint that makes a placement a placement.
5. **Truncating a placement conserves rows, keeps it a placement and is idempotent**, and the round-robin thinning never leaves one row two arms ahead of another that still has one — which is what makes *"what a smaller array costs the host"* a statement about a smaller array rather than about the first rows of a bigger one.

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | the plan **area** exceeding the footprint | **no** | 0.685, and the task did not close on it |
| 2 | some orientation packing 45 arms in **one level** | **YES** | 2 of 720, and it is why `C-0041`'s verdict does not transfer |
| 3 | the **crossover placement** reaching 45 at some phase | **no** | 43 at the best of 32, and 43 is the proven bound |
| 4 | the host surviving the arms **connected** | **no** | zero bonded components at 42 arms and above |
| 5 | the packing admitting **three or more** columns | **YES** | three per interior row, and `C-0047`'s flatness break-even is met |
| 6 | the generalised packer failing to reproduce `C-0041` | **no** | its 15, its 0 of 720 and its 1 of 720, exactly |

**A result that was not anticipated:** the decisive obstruction is not a packing at all. The task was formulated around whether 45 rectangles fit, and they do; what refuses the array is that **an arm is not added to the sheet, it is cut out of it** — 65.4 % of the host's duplex length and 100 % of its crossovers — so the binding number is the count at which the host stops being a sheet, and that is **25**, not 43 and not 45. `C-0041`'s own surprise was of the same kind and in the opposite direction: *"the array does not fail to pack, it fails to stand up."* **This one does not fail to pack either; it fails to leave a tile behind.**

**A second one:** the ten phases that place the most arms are `C-0015`'s ten centro-symmetric phases, for the **third** independent time.

---

## Does `C-0050`'s verdict survive?

**Its bound survives untouched — it is kinematic and owes nothing to a plan view. One row of its catalogue does not.**

| `C-0050` said | this claim finds |
|---|---|
| *"3 of 14 clear every predicate at §3's acceptable stroke"* — `E3a` at 15, `C-0023`'s linear `E5`, and **`E5a1`** | **2 of 14.** `E5a1`'s row carried `packingAssessed = false`; assessed, it fails — [`CH-0065`](../challenges/CH-0065-the-hinge-arm-array-has-no-plan-view-either.md) |
| *"`E5a1` is the only clearing row computed on the exact element"* | **unchanged, and that is what makes the loss expensive**: the remaining two rows are the ones `C-0050` itself flags as carrying idealisations |
| *"its plan view is unassessed (open item 1)"* | **answered here.** The array has a plan view as bodies and none on the lattice, and the host does not survive the part that does place |
| the kinematic, validity and dead-load stroke ceilings | **untouched.** None of them contains a coupling, let alone a plan view |
| §6 task 3 is unaffected | **still unaffected**, and this claim does not touch it |

---

## Validity range

- **TRL 1–3. Nothing here is measured, and the motif is not demonstrated.** `C-0028`'s and `C-0029`'s findings are upstream of every number: no duplex has been built standing normal to a single-layer sheet, and a duplex end has at most two covalent links. `E5a1` needs no standing duplex — its arm lies **in** the sheet plane — but its `A2` anchorage is still a two-link joint at a duplex end.
- **The plan model is a hard-body one**, at nominal positions: no thermal excursion, no assembly tolerance, no out-of-plane bow. Conservative in the same direction as `C-0041`'s — a real array is *less* likely to place.
- **The host is any single-layer 40 × 40 nm Rothemund sheet.** `C-0039` grounds `E5a1`'s near end on the **tile**, and that is the reading evaluated; the arithmetic is identical for the output superstructure, because the two bodies have the same lattice and the same inventory. **Which body hosts the hinges is a design variable nothing upstream has chosen**, and neither choice survives at 43 arms.
- **The 43 is exact within the model and is an UPPER bound on what a real sheet carries.** The per-row schedule is optimal and the independent per-row bound confirms it; every additional constraint a routing would add (staple continuity, scaffold path, seam placement) can only reduce it.
- **The buried-crossover rule is the load-bearing modelling choice**, and it is stated: a crossover under an arm on that arm's own interface is charged as deleted. Without it the demand falls from 56 to 43 of 56 — still 77 % of the inventory, and the host still loses 65.4 % of its duplex length, so **no verdict moves**; what moves is the count at which the last crossover goes, from 42 to about 47.
- **The 25 is read on this claim's own connectivity model**, which is `C-0041`'s: a crossover either exists or does not, a segment is connected or not. It says nothing about the **stiffness** of the residual sheet, which is [`T-110`](../../TASKS.md)'s question and which this claim makes larger rather than smaller.
- **`C-0047`'s 0.218 is CITED for a regular 3 × 15 grid**, not re-derived on the 43-point irregular placement solved here.
- **The arm is `C-0039`'s placement at §3's ACCEPTABLE stroke.** Its desired-stroke verdict is unchanged and untouched: `C-0039` reports `E5a1`'s usable stroke as 3.119 nm and `C-0050` refuses the desired clause kinematically.
- **The double-nick reading of the root is named and not adopted.** If it is right, the near stiffness is `2 k_θ` and the placed arm 9.985 nm, at which the lattice places **30**. It is a mechanics question for `C-0039` and it runs the unfavourable way here.
- **One flexure per load path**, exactly as `C-0023`, `C-0029`, `C-0034` and `C-0039` assume.
- **[`C-0054`](C-0054-consumed-crossover-sheet.md) (`T-110`) ran concurrently and independently, and the two corroborate.** It reaches the sheet from the *interface* side — a connected sheet needs one retained crossover on **each** of its 14 interfaces, so the hinge budget is **42 of 56 = 75 %**, and all of `C-0046`'s surviving designs sever the tile. This claim reaches it from the *arm* side and lands lower, at **25**, and the difference is exactly what a plan view adds: `C-0054` charges a hinge as a **crossover removed from an interface**, while an `E5a1` arm is additionally a **length of duplex cut out of its own row** (65.4 % of the host's at 43 arms) and **buries** 11–13 further crossovers under itself. **Neither model contains the other's term, both say the surviving designs sever the tile, and 25 < 42 is the direction a plan view must move it.** Nothing here depends on `C-0054`; `T-111`, which it raised, is the same question as this task and is answered here.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| interhelical distance | 2.69 nm | **CITED, MEASURED** by SAXS, Fischer et al. (2016) |
| B-DNA steric diameter | 2.0 nm | **CITED**; the phosphate backbone *is* the surface |
| rise per base pair | 0.34 nm | **CITED**, Douglas et al. (2009) |
| crossover interface spacing | **32 bp** | **CITED**, Rothemund (2006) via `C-0015`; the whole lattice rests on it, and `C-0040` swept it |
| duplex `EI` | 230 pN·nm² | **CITED, a CanDo MODEL INPUT** (Kim et al., 2012), **not a measurement**; Fields et al.'s implied 172.906 swept |
| crossover hinge `k_θ` | 13.5294 pN·nm/rad, `α ∈ [0.6, 1.2]` | **CITED, FITTED**, Chen et al. (2014) via `C-0009`; `α = 0.6` swept |
| phosphate radius | 1.00 nm | **CITED**, Hedley et al. (2024) via `C-0029` |
| per-path unzip allowable | 10 pN | **CITED** via `C-0006`/`CH-0029` |
| `C-0047`'s dishing at 3 × 15 and at 1 × 15 | 0.218, 0.695, 0.308 | **CITED**, not re-derived here |
| `C-0023`'s compliance ceiling | 40 pN/nm | **DECLARED**, and withdrawn by `C-0049` |
| §3 parameters | 100 pN, 3 nm, 40 × 40 nm, 2 mM | **CITED** |
| `C-0039`'s, `C-0041`'s, `C-0040`'s and `C-0015`'s design numbers | — | **CITED**, and every one reproduced here as a gate-5 test |

Everything else — the generalised plan element, every orientation sweep, every layout verdict, the hinge-site lattice, the per-row schedule and its bound, the 43 placements, the buried-crossover census, the host severance, the self-consistent count, the five sensitivities and every threshold — is **derived here in code**, with `C-0039`'s, `C-0041`'s, `C-0040`'s and `C-0015`'s pipelines **re-run rather than tabulated**.

## Still open — named, not answered

1. **What a host sheet with 25–43 arms cut out of it is actually worth as a structure.** This claim reports **connectivity**; `T-110` asks for **stiffness**, and this claim makes that question much larger — at 45 arms there is no connectivity to be stiff about.
2. **Whether `E5a1`'s near stiffness is `k_θ` or `2 k_θ`.** The root is a double nick as well as a crossover, and `C-0025`'s `J2b` says a double nick **is** a crossover. It lengthens the arm by 9.3 % and costs 13 placements. **A question for `C-0039`, raised here.**
3. **Whether a multilayer or larger host escapes it.** The edge would have to reach **49.25 nm (1.23×)** or the duplex count **16 (1.07×)** for 45 to *place*; neither is priced against the host's survival, and a two-layer host — where the arms come out of one layer and the load path out of the other — is a different device that nothing in §3 describes. **The third specification question this branch has raised**, after `C-0041`'s footprint and `C-0035`'s slots.
4. **The flatness of the 43-point irregular placement**, as against `C-0047`'s regular 3 × 15. The direction is settled; the number is not.
5. **Which body hosts the hinges.** `C-0039` says the tile; the arithmetic is the same for the superstructure, and the *consequences* are not — the tile's own rigidity is what `C-0006`, `C-0009` and `C-0047` spend on flatness.

## Challenges

**Raises [`CH-0065`](../challenges/CH-0065-the-hinge-arm-array-has-no-plan-view-either.md)** against `C-0050`'s `E5a1` catalogue row — which carries `packingAssessed = false` and is now assessed — and, by inheritance, against `C-0039`'s and `C-0023`'s *"45 load paths"* premise for the whole `E5` family, in the same way `CH-0055` challenged it for the standoff flexure. **No number in `C-0039`, `C-0040`, `C-0041` or `C-0015` fails to reproduce.**

**None stands against this claim.** The four ways it would fail:

1. **A hinge that is not a crossover of the host's own lattice.** Then the root is unquantised and the 5.44 nm pitch dissolves. But `n k_θ` *is* the crossover's constant — it is what `C-0023`, `C-0029`, `C-0034`, `C-0039` and `C-0040` all price `E5` on — so this would be a different element.
2. **An arm that is not cut out of the host.** A duplex added *above* the sheet and joined to it by an inter-layer crossover would spare the host's duplex length, at the price of a **two-layer** body §3 does not describe and of `C-0009`'s and `C-0015`'s whole single-layer lattice.
3. **A demonstration that two duplexes may sit closer than 2.69 nm in plan.** Swept: at the 2.0 nm steric reading the placement is unchanged at 43.
4. **A better placement than 43.** Impossible within the model — the constructive placement meets the independent per-row upper bound.
