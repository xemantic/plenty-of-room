# C-0065 — A trio that closes in isolation DOES repeat: all 44 of `C-0062`'s recorded trios place **34** times on `C-0063`'s placement, and the plan view is not what the composition costs — what it costs is a **register**, because a base misalignment floor is a minimum over the axial position on the host duplex and an array PINS that position at 34 stations at once; no trio registers at its station, the whole array translates 0.17–3.91 nm, and at `C-0062`'s own recommended **10 bp** row the base the array can actually reach is **57.0°** against the published **6.0°** — past the half right angle at which the base cannot be represented at all

| | |
|---|---|
| **Task** | [`T-130`](../tasks/T-130.md), raised by [`C-0062`](C-0062-crossbar-trio-existence.md) in one sentence: *"a trio that closes in isolation may not place 34 times"* |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the array belongs to |
| **Verification type** | **logical** (a lattice congruence that fixes the shape of the answer before any solve, and an exact hard-body plan packing on measured constants) **+ in-silico** (`C-0059`'s single-junction feasible set and `C-0062`'s pruned closure verdict re-run as a **register field** over one 44 bp window of a host duplex; `C-0053`'s packer and `C-0063`'s grillage under `C-0022`'s solved load, both re-run as libraries) |
| **Verdict** | **PASS, and the answer to the acceptance question is YES — every one of `C-0062`'s 44 recorded closing trios places 34 times, and the interesting result is what the composition charges for it.** Three cheap bounds settle the plan view before anything is solved: 34 truss blocks cover **0.385** of the footprint, the widest crossbar in `C-0062`'s band demands **9.49 nm** against the **10.88 nm** station pitch (where `C-0053`'s 45-path arm demands 11.82 and does *not* clear it), and — the bound that decides the shape of the whole answer — **every one of `C-0063`'s 34 stations is the SAME helical phase of its OWN host duplex**, 24 bp from that duplex's `NORTH` plane, because adjacent rows' duplexes are phase-shifted by exactly the 16 bp their sites are offset by. **So the register is ONE question, not thirty-four, and the placed count is quantised at 0 or 34.** It comes out 34: 0 overlaps, 0 leg clashes, one level, at every one of the 44 trios. **What the composition actually finds is a register.** A leg's *base* can only sit where the host's own backbone offers one, and **17 of 89** axial positions do; **no row pitch closes at the station itself**, so the array translates by **0.17 nm (9 bp) to 3.91 nm (6 bp)** — and **no choice of the sheet's phase can absorb it**, because the phase moves the station and the base lattice **together** (asserted as a gate test). Pinning that coordinate re-reads the base floor `C-0059` measured and `C-0062` composed its design table on: at the centre nearest the station the **10 bp** row reads **57.0°** against its published **6.0°** floor — **9.5×**, and past the **45°** at which `C-0037`'s `TwoLinkBase` invariant cannot represent a base at all ([`CH-0078`](../challenges/CH-0078-the-base-floor-is-a-minimum-over-a-coordinate-the-array-pins.md)). The 6.0° is available, 2.55 nm away, at the price of the tile edge. **The row pitch the array can actually build is 9 bp, at 18.0°**, and it keeps the tile flat: **0.0780** of the free-tile stroke against `C-0063`'s **0.0706** at 34 nominal stations, inside `T-5b`'s 0.10. **17 of the 44 trios survive every clause at once** — place 34, close on `C-0057`'s own verdict grid, carry a representable base, and leave the tile flat — at the **9, 11 and 12 bp** rows. **And the flexure is a different story, which is reported and not folded in**: with `C-0030`'s span the same array covers **1.84×** the footprint, needs **7 levels**, and places **12 of 34** — `C-0041`'s obstruction at 34 paths, independent of every trio. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED.** A torsion closure is a **necessary** condition and never a sufficient one (`C-0029`, `C-0052`, `C-0057`, `C-0059`, `C-0062`), so every *"places"* verdict here is an **upper bound on buildability** — and this claim's headline is a positive, which is the weak direction. The motif is unchanged and undemonstrated: no free lever has been built on a single-layer sheet at one crossover (`C-0055`, 62 recorded queries). |
| **Provenance** | `gpd/results/T-130-crossbar-array-placement.json`, produced by `anchoring.CrossbarArrayPlacementStudyKt`; model in `src/main/kotlin/anchoring/CrossbarArrayPlacement.kt`; **5 cheap bounds, 89 register positions, 7 row-pitch register records, 13 closing pair centres, 44 array placements, 10 flatness solves, 2 flexure readings, 5 sensitivities, 5 convergence records, 8 upstream reproductions, 2 budget records, 5 predicates**; **26 gate-named tests in `src/test/kotlin/anchoring/CrossbarArrayPlacementTest.kt`**; `tools/verify.sh` **BUILD SUCCESSFUL in 10 m 51 s — the whole suite, on its own isolated tree, with NOTHING dropped** (a sibling's mid-TDD `anchoring/ArmSlabClearanceTest.kt` had to be dropped by `--drop-file` during development and compiles by the final run); the result file re-run through `tools/study.sh` and diffed **byte-for-byte identical** on two independent runs |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40.0 × 40.35 nm single-layer **square-lattice** Rothemund sheet, 15 duplexes at the SAXS **2.69 nm**, 0.34 nm rise, 10.67 bp/turn, crossover phase **24** — `C-0063`'s own host; the 34 stations read from `gpd/results/T-125-*.json` and the 44 trios from `gpd/results/T-127-*.json`; plan convention `C-0041`'s and `C-0053`'s (a duplex is a rectangle of width 2.69 nm, two at exactly that are tangent and admissible; 2.0 nm swept); junction geometry `C-0029`'s via `C-0059`'s `SingleJunctionFeasibleSet` — phosphate radius **1.00 nm**, minor groove **120°**, the inherited **[0.60, 0.70] nm** window; closure `C-0062`'s per-assignment pruned verdict at `C-0059`'s own **60-step / 4-refinement** grid; register grid **0.17 nm** (2 steps per base pair) over **±22 bp**, **4** candidates per position (`C-0059`'s own cap); load `C-0022`'s **solved** edge profile at 2 mM, a 10 nm gap and 0.192 V |
| **Consumes** | [`C-0062`](C-0062-crossbar-trio-existence.md) (its 44 trios and its per-configuration closing counts, **consumed as data** from its result file and re-checked), [`C-0063`](C-0063-upward-root-placement.md) (the 34 stations at phase 24, **read from its result file** and re-derived from `upwardRootLattice`; its 0.0706 and 0.3079 reproduced), [`C-0059`](C-0059-torsion-feasible-routing.md) (`SingleJunctionFeasibleSet`, `junctionLinks`, `torsionVerdict`, and its published base floors **consumed as data**), [`C-0057`](C-0057-backbone-torsion-closure.md) (`bestLinkClosure`, against which the pruning is asserted), [`C-0055`](C-0055-unused-junction-site.md) (the upward azimuth, `upwardRootLattice`, the 10.88 nm pitch, the `(k − 2b) mod 4` azimuth rule), [`C-0053`](C-0053-hinge-arm-array-packing.md) (`PlanElement`, `elementPackingVerdict`, `elementMembersClash`, `rectanglesOverlap` — **re-run as a library**), [`C-0048`](C-0048-truss-cap.md)/[`C-0052`](C-0052-crossbar-junction-trio.md) (`CrossbarGeometry`, `capDesign`, `SolvedTrussCap`), [`C-0037`](C-0037-triangulated-standoff.md) (`TwoLinkBase` and its half-right-angle invariant), [`C-0030`](C-0030-coupled-standoff-joint.md) (`coupledFlexureSpan`), [`C-0022`](C-0022-tile-edge-load-profile.md) (the solved load), [`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile` |
| **Raises** | [`CH-0078`](../challenges/CH-0078-the-base-floor-is-a-minimum-over-a-coordinate-the-array-pins.md), against `C-0062`'s and `C-0059`'s design tables — their base misalignment floor is a minimum over the axial position on the host duplex, and an array pins that position |

---

## The claim, in one line

**`C-0062` asked whether three junctions can close on one crossbar and found that 609 lattices do; the array asks a different question, and the answer is that the crossbar assembly repeats 34 times without touching anything — every station is the same helical phase of its own duplex, so the whole array is one register question with a yes-or-no answer — while the coordinate the array thereby pins is exactly the one the base misalignment floor was minimised over, so the design table's 6.0° becomes 57.0° at the row `C-0062` recommends and 18.0° at the row next door, and the composition's real output is that the truss's row pitch is 9 base pairs and not 10.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, angles **degrees** in every reported number and radians in code; `k_BT = 4.141947 pN·nm` at **300 K** in aqueous **2 mM MgCl₂**.
- `x` runs **along** the host sheet's helices, `y` **across** them, `z` **normal and positive upward** — away from the grafted layer, which lies below the tile. Origin at the tile centre.
- **A duplex in plan is a rectangle of width `d = 2.69 nm`** — `C-0041`'s and `C-0053`'s convention verbatim, so two parallel duplexes at exactly `d` are **tangent and admissible**; the 2.0 nm steric reading is swept and moves nothing.
- **One truss instance** is `C-0048`'s cap assembly: two legs standing along `+z` from **one** host duplex at `x_root ∓ w/2`, and one crossbar duplex along `x̂` one duplex radius above their heads, displaced by the trio's own `axialPhase` in `x` and `lateralSeat` in `y`. In plan it is **one rectangle with two vertical members** — `C-0053`'s `PlanElement` exactly — and the crossbar always covers both legs, because `C-0048`'s minimum crossbar is `row + 6` bp.
- **Two containment readings, both reported.** `C-0053`'s arm is a length of the host's **own** duplex and must lie on the sheet; a truss's crossbar is a **free** duplex above the leg heads and may overhang the rim. `LEGS_ON_SHEET` is the headline; `WHOLE_BLOCK` is `C-0053`'s rule and is reported beside it.
- **The station is where the coupling enters.** For `C-0063` that is one upward crossover carrying one spring; for a truss it is **two leg bases**, each carrying half the path's stiffness — **68 entry points, not 34** — and the flatness is re-solved on them rather than assumed unchanged.
- **The upward (`EAST`) azimuth is `C-0055`'s**: 24 bp from a duplex's own `NORTH` plane, at exactly a quarter turn on the square lattice's designed 33.75°/bp, with duplex `b`'s azimuth class at plane `k` equal to `(k − 2b) mod 4`.
- **A register offset is signed and measured from the station**; a positive offset moves the instance toward `+x`.

---

## The five cheap bounds, which ran first — and the third is the method

| | bound | value | against | ratio | what it settled |
|---|---|---|---|---|---|
| **1** | the plan area of 34 truss blocks | **621.9 nm²** | 1614 nm² | **0.385** | the truss **block** is not an area problem — `C-0053`'s bound 1 in a new place, and it does not decide the task |
| **2** | the block's plan demand, `crossbar + d`, at the widest crossbar in the band | **9.49 nm** | **10.88 nm** | **0.872** | the along-row packing, before any packer runs. `C-0053`'s 45-path arm demands **11.82 nm** and does *not* clear the same pitch; `C-0055`'s 34-path arm demands 10.854 and clears it by 0.027 nm |
| **3** | **the helical phase classes of the 34 stations** | **1** | 1 | **1.000** | **the shape of the whole answer.** Every station is an `EAST` site of its **own** duplex at 24 bp from that duplex's `NORTH` plane, so the register is asked **once** and the placed count is **quantised at 0 or 34** |
| **4** | the plan area of 34 blocks **and their flexures** | **3129 nm²** | 1614 nm² | **1.939** | the **full** element cannot place at one level at all, independently of any trio — `C-0041`'s obstruction transferred to 34 paths |
| **5** | the upward site's own axial coordinate | **8.16 nm** | 10.88 nm | 0.750 | **where** the register question has to be asked, at every one of the 34 stations |

> **Bound 3 is not a bound, it is the method.** Because adjacent rows' duplexes are phase-shifted by exactly the 16 bp their upward sites are offset by, every station in every row presents the identical backbone geometry — so one register solve answers for the whole array, and the outcome cannot be an intermediate count. The declared falsifier (two or more classes, which would cap the count at the largest class) **did not fire**.

---

## Deliverable 1 — the array places, 44 times out of 44

Every one of `C-0062`'s 44 recorded trios, placed at all 34 of `C-0063`'s phase-24 stations, through `C-0053`'s own packer.

| | |
|---|---|
| trios recorded by `C-0062` | **44**, of which **39** close on `C-0057`'s own 180-step verdict grid |
| trios placing **34** times, one level | **44 of 44** |
| overlapping pairs, mutually blocking pairs, leg clashes | **0, 0, 0** at every trio |
| levels required | **1** |
| plan area used | **0.231–0.385** of the footprint |
| under `C-0053`'s **whole-block** containment | **34** at the 9 and 10 bp rows; **26** at every other pitch — the eight rim instances leave the tile |

**The 26 is the register's doing, not the packing's.** At a 3.06–4.76 nm offset the outermost station moves to `x = 19.4–21.1 nm` and its crossbar crosses the rim; the legs themselves still stand on the sheet at every pitch, which is why the two containment readings differ by exactly the outer column.

---

## Deliverable 2 — the register, which is what the composition actually costs

`C-0059`'s single-junction feasible set and `C-0062`'s pruned closure verdict, over **89** axial positions at 0.17 nm steps — **241 junction solves** — of which **17 carry a torsion-closing 90° base**.

| row [bp] | closing pair centres | **nearest offset** | **misalignment there** | `C-0059`'s published floor | **ratio** | representable? | dishing/stroke | flat? |
|---|---|---|---|---|---|---|---|---|
| 6 | 1 | **+3.91 nm** | 33.0° | 33.0° | 1.00 | yes | **0.1654** | **no** |
| 7 | 1 | +3.40 | **69.0°** | 69.0° | 1.00 | **no** | 0.1374 | no |
| 8 | 2 | +3.06 | **66.0°** | 57.0° | 1.16 | **no** | 0.1179 | no |
| **9** | **3** | **+0.17** | **18.0°** | **6.0°** | **3.00** | **yes** | **0.0780** | **YES** |
| **10** | **3** | **+0.51** | **57.0°** | **6.0°** | **9.50** | **NO** | 0.0850 | yes |
| 11 | 2 | +1.02 | 33.0° | 33.0° | 1.00 | yes | 0.0932 | yes |
| 12 | 1 | +0.85 | 33.0° | 33.0° | 1.00 | yes | 0.0925 | yes |

Four things fall out, and the second is the one that moves a design.

1. **Nothing registers at the station.** Not one of the seven row pitches carries a closing base pair centred on the upward site itself; the nearest is 0.17 nm away and the farthest 3.91. **The array therefore translates**, and it translates as one body, because every row's register is the same.
2. **The floor is not available where the array can stand.** `C-0059`'s 6.0° at the 9 and 10 bp rows is a **minimum over the axial position**, and it sits **2.72 nm** and **2.55 nm** from the station respectively. At the centres the array can actually use, the same rows read **18.0°** and **57.0°** — and 57.0° is past the **45°** at which `C-0037`'s `TwoLinkBase` invariant exchanges its restrained and free axes and `capDesign` refuses to evaluate at all. **`C-0062`'s recommended design point is the one the array cannot build.** [`CH-0078`](../challenges/CH-0078-the-base-floor-is-a-minimum-over-a-coordinate-the-array-pins.md)
3. **The sheet's own phase cannot absorb the offset.** `C-0015`'s phase is quantised to base pairs and would seem to be able to slide the stations onto the register — but it moves the crossover planes **and the host's backbone** together, so a station's coordinate in its own duplex's frame is invariant under it. Asserted as a gate-5 test over six phases.
4. **The offset is a flatness cost, and it is the binding one at three row pitches.** A translated array is no longer centro-symmetric, and `C-0063`'s congruence `2c ≡ 0 (mod 10.88 nm)` fails at every offset here. At 0.17–1.02 nm that costs 10–32 % of the dishing and stays inside `T-5b`'s 0.10; at 3.06–3.91 nm it costs 1.7–2.3× and leaves it.

---

## Deliverable 3 — what survives every clause at once

| clause | trios surviving |
|---|---|
| recorded by `C-0062` | **44** |
| … and placing **34** times at one level | **44** |
| … and closing on `C-0057`'s own verdict grid | **39** |
| … and carrying a base `C-0037` can represent at the centre the register offers | **24** |
| … **and leaving the tile flat at `T-5b`'s 0.10** | **17** — the **9, 11 and 12 bp** rows |

**The 6 bp row survives everything but the flatness** (33.0°, but at a 3.91 nm translation); **the 7, 8 and 10 bp rows fail the base**; and the **9 bp** row is the best of what is left — nearest register (0.17 nm), best representable misalignment (18.0°), flattest array (0.0780).

---

## Deliverable 4 — the flatness of what places, on `C-0063`'s own grillage

Under `C-0022`'s **solved** load at 2 mM, a 10 nm gap and 0.192 V, on the phase-24 host, with `C-0017`'s unchanged 33.3333 pN/nm as a **sum**.

| placement | stations | offset | dishing / stroke | flat? |
|---|---|---|---|---|
| NONE — the free tile | 0 | — | **0.3079** | no |
| **`C-0063`'s 34 stations, one spring each** | 34 | 0 | **0.07061** | **yes** — its published 0.0706, departure `2.1e−4` |
| the truss's **68 leg bases**, unregistered | 68 | 0 | **0.0749** | **yes** |
| **the truss's 68 leg bases, registered, 9 bp row** | 68 | **+0.17** | **0.0780** | **YES** |
| the same, 10 / 11 / 12 bp | 68 | 0.51 / 1.02 / 0.85 | 0.0850 / 0.0932 / 0.0925 | yes |
| the same, 8 / 7 / 6 bp | 68 | 3.06 / 3.40 / 3.91 | 0.1179 / 0.1374 / **0.1654** | **no** |

**Splitting one station into two leg bases costs 6.1 %** of the dishing (0.0706 → 0.0749) and **the register costs a further 4.2 %** at the 9 bp row. Both are inside the convention; neither was known before this task, because `C-0063` solved a placement of points and a truss is not a point.

---

## Deliverable 5 — the flexure, reported beside the headline and never folded into it

`C-0030`'s coupled flexure, at the span `C-0048`'s pipeline places it at, running along `−ŷ` from each cap.

| paths | span | plan area / footprint | overlapping pairs | levels required | **instances placing at one level** |
|---|---|---|---|---|---|
| **34** | **27.41 nm** | **1.84** | 186 | **7** | **12 of 34** |
| 45 (`C-0048`'s own) | 28.35 nm | 1.90 | 186 | 7 | 12 of 34 |

**This is `C-0041`'s obstruction, unchanged by 34 paths and by every trio.** The truss *standoff* repeats 34 times; the *element it caps* does not, and the count it reaches is **12**. The two readings are kept apart because the question `T-130` was set is about the trio, and the flexure array is `C-0041`'s question with a published negative answer.

---

## Sensitivities — what moves the verdict and what does not

| axis | reading | closing positions | pair centres | nearest offset | placed | verdict moves? |
|---|---|---|---|---|---|---|
| **reference** | `C-0029`'s geometry, 120° groove, `r_P` = 1.00 nm, seat 0.0, cap 4 | 17 | 3 | +0.51 | **34** | — |
| candidate cap | **12** per position rather than `C-0059`'s 4 | **21** | **5** | +0.51 | 34 | **no** |
| lateral seat | the leg seated **0.5 nm** off the host's axis | 11 | 1 | −0.85 | 34 | **no** |
| the station's datum | the `NORTH` plane read on the **other strand** (+1.209 nm) | 17 | 3 | **−1.341** | 34 | **no** |
| exclusion width | the **2.0 nm** steric diameter rather than 2.69 nm SAXS | 17 | 3 | +0.51 | 34 | **no** |

> **The datum sensitivity is applied to the OFFSETS and not to the grid, on purpose.** Re-gridding the register field around a shifted datum resamples a set that lives on a continuum, and the first attempt did exactly that and reported *"no registered centre at all"* — a statement about the grid, not about the convention. Moving the datum instead leaves the same 3 centres and moves the nearest to −1.341 nm.

---

## The five verification gates

Executed as **26 gate-named tests** in `src/test/kotlin/anchoring/CrossbarArrayPlacementTest.kt`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | an instance's plan lengths are its own base-pair counts times the rise; the array's area is additive and **doubles exactly** with the count; the whole packing verdict is **dimensionless** — scaling every length by ten leaves the overlap, clash and level counts identical; the crossbar covers both legs at `C-0048`'s minimum and above; unphysical arguments throw at **ten** entry points, including an odd axial grid, a zero window and an empty station set | **PASS** |
| **2 — limiting cases** | **the free limiting case — collapsing the trio to a bare root reproduces `C-0063`'s placement**, station for station, and packs at one level; **the pruned base closure agrees with `C-0057`'s own `bestLinkClosure`**, 0 disagreements — the assertion the whole register rests on; one instance carries the same geometry wherever it is placed; two instances on one site clash; an instance wider than its row's pitch overlaps its neighbour | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the register field is **deterministic** on repeat calls; **a finer azimuth grid never loses a closing position, uncapped** (60 ⊂ 120, and the cap is *not* monotone, which is measured rather than assumed); the **nearest registered centre and its misalignment are unchanged** at 2 and 4 axial steps per base pair (0.17 nm, 18.0°); the dishing is unchanged over nested subdivisions **1 ⊂ 2 ⊂ 4** (departure `2.9e−6`); the result file re-run through `tools/study.sh` and diffed **byte-for-byte identical** | **PASS** |
| **5 — literature and upstream** | **8 reproductions, worst departure `2.1e−4`**: `C-0063`'s **0.0706** and **0.3079**, and its 34 stations re-derived from `upwardRootLattice` and asserted equal at **departure 0**; `C-0055`'s **10.88 nm** pitch exactly; `C-0062`'s **196** band closures and **44** trios exactly; `C-0059`'s **6.0°** floor at the 10 bp row exactly; `C-0048`'s **16 bp** minimum crossbar exactly; the `EAST` site a quarter turn from the `NORTH` plane at the designed twist | **PASS** |

### Gate 3 — five things that are not restatements of the construction

1. **Every one of `C-0063`'s 34 stations is one helical phase class of its own duplex**, asserted as a census and not as an argument — and its local coordinate is exactly `EAST_SITE_BASE_PAIRS`.
2. **The sheet's own phase cannot absorb the register offset**: the station's local coordinate is invariant over six crossover phases, because the phase moves the station and the backbone together.
3. **The exact leg-clash census, computed on the legs' true positions, equals the packer's member-clash count** at three row pitches with a 0.4 nm lateral seat — the seat is common to every instance and cancels exactly, which is why the packer may carry the members on the crossbar's axis.
4. **The leg entry points inherit the stations' centro-symmetry at any axial phase** — the trio's own axial phase displaces the crossbars and not the load path, so it cannot break a symmetry the flatness depends on.
5. **A chord is a line**: a half turn leaves the misalignment unchanged at 30 azimuths, compared absolutely — `C-0062`'s gate-3 item in a new place.

---

## Validity range

- **TRL 1–3, and a *"places"* verdict is the weak direction.** A torsion closure is necessary and never sufficient; nothing here is measured, no sequence is designed, no assembly is demonstrated, and the motif is undemonstrated exactly as `C-0055` and `C-0029` leave it.
- **`C-0062`'s and `C-0059`'s whole validity ranges are inherited**, including `C-0057`'s rigid-residue model, its **marginal** occupancy test and the inherited `[0.60, 0.70] nm` window against the measured 0.607 / 0.664.
- **The register is a set of intervals on a continuum sampled at 0.17 nm**, and its *count* is not grid-converged — refining the axial grid 2 → 4 steps per base pair doubles the closing positions, exactly as `C-0062`'s closing count doubles under refinement. What the array needs is not the count but **whether a centre lies near the station**, and that is unchanged at both grids (0.17 nm, 18.0° at the 9 bp row).
- **The per-position candidate cap is `C-0059`'s 4**, and a cap is a **ranking**: it is not monotone under refinement, and at 12 candidates the register finds 21 closing positions and 5 pair centres rather than 17 and 3. Every *"does not close"* here is therefore a **capped** verdict and weaker than a *"closes"*.
- **The base junction is solved at `lateralSeat = 0`** in the headline (0.5 nm swept). `C-0059` sweeps the seat as a free variable; an array pins nothing about it, so this is a *choice*, not a constraint, and it moves the register (11 closing positions, one centre at −0.85 nm) without moving the verdict.
- **The plan model is `C-0053`'s hard-body one**, at nominal positions: no thermal excursion, no assembly tolerance, no out-of-plane bow. Conservative in the same direction as `C-0041`'s and `C-0053`'s.
- **The flatness is `C-0063`'s pipeline at ONE load state** — `C-0022`'s 2 mM, 10 nm, 0.192 V — and `C-0064` establishes that a flatness verdict at one state does not travel. `T-129` owns that question and this claim inherits its exposure unchanged.
- **The 34 and the stations are `C-0055`'s and `C-0063`'s.** A different count re-solves the placement and re-opens everything here.
- **The leg length `m` is a global variable this claim does not solve.** All 34 caps must sit at one height, so one `m` serves the whole array; the base-to-cap chord budget `chordPairMisalignment(m)` is `C-0052`'s and is **not** re-composed here against the *pinned* base misalignment. That composition is named as open item 1 and it can only tighten the verdict.
- **The greedy conflict-free count is a lower bound** on the maximum independent set. It is exact wherever the array packs cleanly (it does, at every trio) and it is a bound only in the flexure reading, where it returns 12.
- **`C-0053`'s containment rule and the legs-on-sheet rule are both reported and neither is adopted as the truth.** The headline uses the legs rule because a crossbar is a free body above the plane; under `C-0053`'s rule the count is 26 at five of the seven row pitches.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| the 44 closing trios, their crossbar lengths, row pitches, axial phases and lateral seats | — | **`C-0062`, CONSUMED AS DATA** from `gpd/results/T-127-*.json`; its 196 band closures and 44 records re-checked at departure 0 |
| the 34 stations and their host phase | phase 24, 10.88 nm pitch | **`C-0063`, CONSUMED AS DATA** from `gpd/results/T-125-*.json` **and re-derived** from `upwardRootLattice` at departure 0 |
| the base misalignment floors, 6–12 bp | 33 / 69 / 57 / 6 / 6 / 33 / 33° | **`C-0059`, CONSUMED AS DATA** from `gpd/results/T-124-*.json`, and re-read **at the station** here, which is the whole point of `CH-0078` |
| the solved edge profile | depth −0.30293 over 8.939 nm, rim −0.59388 over 1.0 nm | **`C-0022`**, read from `gpd/results/T-3b-*.json`, keyed on concentration, gap **and bias** |
| the measured backbone, the phosphodiester window, the phosphate radius, the groove angle | 0.60 / 0.70 nm, 1.00 nm, 120° | **CITED** via `C-0029`/`C-0057`; the window's own primary source **NOT FOUND** |
| interhelical distance, rise, bp/turn, crossover spacing | 2.69 nm, 0.34 nm, 10.67, 32 bp | **CITED, MEASURED** (SAXS, Fischer et al. 2016) / **CITED** (Rothemund 2006, Ke et al. 2009) |
| `EI`, `GJ`, `S`, `k_θ` | 230, 460 pN·nm², 1100 pN, 13.5294 pN·nm/rad | **CITED, CanDo MODEL INPUTS / FITTED**; none enters a count here |
| §3 targets | 100 pN, 3 nm, 40 × 40 nm, 2 mM | **CITED** |

Everything else — the plan element and its two containment rules, the phase-class census, the register field and all 13 closing pair centres, every array placement, the flatness of the realised placements, the flexure reading, the five sensitivities and the five convergence records — is **derived here in code**, with `C-0053`'s, `C-0059`'s, `C-0062`'s and `C-0063`'s pipelines **re-run rather than tabulated**.

## A departure worth recording, in the coordinator's prompt rather than in an artifact

`T-130`'s prompt describes `C-0062` as finding *"93 of 5 940 reach-feasible crossbar lattices"* carrying a closing trio, *"best at a 3.00° chord misalignment"*. **Those numbers appear nowhere in `C-0062`, in its result file, or in `C-0059`.** The filed claim reports **196 of 17 388** reach-feasible lattices in the band sweep (**609 of 49 857** solved over the whole study) and best chords of **21.0°** (band) and **9.0°** (depth run). `JOURNAL.md` already records the retraction: the figures are from the agent's *first report*, not from its claim, and were corrected in the iteration-11 entry. They are re-checked here as a gate-5 reproduction so that the record cannot drift again — **the fifth report/artifact divergence this session, and the second in which a superseded number was carried forward in a prompt.**

## Still open — named, not answered

1. **The leg-length composition.** All 34 caps sit at one height, so one leg length `m` serves the whole array, and `C-0052`'s `chordPairMisalignment(m)` budget has **not** been re-composed against the *pinned* base misalignment this claim measures. It can only tighten the verdict, and it is the first thing to run against `CH-0078`.
2. **A joint search over the register and the trio at once.** This claim takes `C-0062`'s trios as given and asks where they can stand; a search that swept the crossbar lattice **and** the base's axial position together might find a trio whose base registers at the station. Nothing rules it out and nobody has run it.
3. **The flexure array**, which places 12 of 34 and is `C-0041`'s standing negative. Whether a *different* output element — one that does not lie in plan — escapes it is the question the truss branch now hangs on.
4. **The flatness at `C-0022`'s other four states**, inherited from `C-0063` and owned by `T-129`.
5. **The scaffold and the staple breaks.** 34 instances put **136** covalent links into the host's backbone; no routing has been attempted, and `C-0055`'s 8 bp staple-break yield cost is unpriced.

## Challenges

**Raises [`CH-0078`](../challenges/CH-0078-the-base-floor-is-a-minimum-over-a-coordinate-the-array-pins.md)** against `C-0062`'s design table and, by inheritance, `C-0059`'s. **No upstream number fails to reproduce** — 8 reproductions, worst departure `2.1e−4` against a value its own claim quotes to four digits.

**None stands against this claim.** The four ways it would fail:

1. **A demonstration that a leg's base may sit anywhere on the host duplex**, which would dissolve the register entirely. It rests on `C-0029`'s covalent window and `C-0057`'s torsion closure, both of which are inherited and neither of which is measured on this junction.
2. **A finer register grid finding a centre at the station.** The closing set is a measure and its count doubles with the grid; the nearest centre does not move between 0.17 and 0.085 nm steps, but a much finer grid has not been run.
3. **A trio search that includes the base**, which is open item 2 and would supersede the register table rather than contradict it.
4. **A different containment convention.** The headline is on the legs-on-sheet rule; `C-0053`'s whole-block rule gives 26 at five of the seven row pitches, and both are reported.
