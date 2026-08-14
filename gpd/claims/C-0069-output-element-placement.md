# C-0069 — **No output element lies out of the plan, and none needs to** — the normal direction supplies only axial stiffness (1122 nm against a 10 nm envelope), a fold shrinks the plan budget instead of enlarging it, and a second level is reached only by a vertical member; what refuses the flexure is not the plan but its **END CONDITION**, because a midspan-loaded beam's `c` is 48–192 and an end-loaded one's is at most 12 and the span is `c^(1/3)`. The plan budget on **every** 34-root placement is exactly `pitch − d = 8.19 nm`, the midspan family's shortest possible member is **22.41 nm**, and `C-0055`/`C-0063`'s own hinge-rooted arm clears the budget by **0.0256 nm**

| | |
|---|---|
| **Task** | [`T-133`](../tasks/T-133.md), raised by [`C-0065`](C-0065-crossbar-array-placement.md)'s *Still open* item 3 — *"whether a different output element — one that does not lie in plan — escapes it is the question the truss branch now hangs on"* |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with **`A1.2`** for the anchoring scheme the array belongs to |
| **Verification type** | **logical** (a closed-form length-for-stiffness in each of DNA's compliance mechanisms, and an exact lattice bound on the plan length any rooted element may have — both free of any solve) **+ in-silico** (`C-0039`'s exact elastica, `C-0030`'s coupled flexure, `C-0023`'s transverse flexure and `C-0053`/`C-0065`'s packer re-run as libraries on `C-0063`'s own phase-24 stations) |
| **Verdict** | **PASS, and the acceptance predicate is met in its positive half — an element whose 34 instances place at one level exists, TWO of them do, and neither is out of the plan.** Of an **11-row catalogue**, **3 place** all 34 at one level and **2 survive every clause**: `C-0055`/`C-0063`'s own **hinge-rooted arm** (`Q5`, one crossover root + `C-0034`'s `A2` tip, **8.16439 nm**, 34 of 34, one level, 0.463 of the footprint, assembled tangent minimum **30.03 pN/nm** over `[0, 3]` clearing **6 of `C-0017`'s six** 2 mM floors, 2.941 pN per path, two-sided) and a **standoff-headed crank** (`Q7`, `C-0028`'s `B2` base at 8 nm + a pinned tip, **5.331 nm**, **1.53× shorter**, but clearing only **4 of 6** floors and standing on an undemonstrated normal duplex). **The answer to the question as asked is NO: no element out of the plan exists, and the escape that works is not out of the plan, it is SHORT in it.** Three closed-form bounds settle the whole element space before any packer runs. **(i) The plan budget is `pitch − d = 8.19 nm`, exactly, on every 34-root placement** — `3a + 2(15−a) = 34` forces four rows of three (`C-0063`'s bound 1), and three roots on a 10.88 nm lattice cap a rooted element at the bare pitch minus one duplex; the bisected ceiling over `C-0063`'s own rows agrees to `< 1e−9` nm. **(ii) The two-support flexure family is refused at every span and on every placement**: its `c` is bounded below by 48, so its **shortest possible** member is **22.41 nm — 2.74×** the budget, which is strictly stronger than `C-0065`'s 12 of 34. **(iii) The normal direction supplies no compliance**: a member standing along `z` and loaded along `z` is loaded **axially**, `S/k = 1122 nm`, **112×** `C-0017`'s whole 10 nm envelope. The remaining escapes fail too — a **fold** halves the along-row demand and doubles the across-row one, and the across-row pitch **is** one duplex, so at equal contour it places 28 of 34 where the straight element places 34; a second **level** is reached only by a vertical member, whose clash is level-independent (`C-0041`); a **single strand** is compact (6.04 nm) and places, and is **one-sided**, measured at negative argument rather than assumed. **What the plan budget really constrains is the end-condition factor: `c ≤ 2.3416`.** In the two-restraint plane that is a razor: at a one-crossover root the **tip joint may be no stiffer than 79.68 pN·nm/rad** and `C-0034`'s `A2` is **78.235** (1.8 % inside); at an `A2` tip the **root may be no stiffer than 13.930** and one crossover is **13.529** (2.9 % inside). **The rooted-element window has a floor as well**, `1.5 ×` the stroke = 4.50 nm, below which `C-0039`'s exact solver refuses the element outright — a pinned tip at a one-crossover root asks for 3.591 nm and is **refused by its own kinematics, being short enough to place and too short to work**. Raises [`CH-0081`](../challenges/CH-0081-a-rigid-root-demands-a-longer-arm-than-the-plan-admits.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED, and the MOTIF IS NOT DEMONSTRATED** — a free lever held to a single-layer sheet by one crossover is this programme's own construct (`C-0055`, 62 recorded queries), and no duplex has been built standing normal to a single-layer sheet (`C-0028`, `C-0029`). Both are upstream of every number here. A *"places"* verdict is the weak direction: the plan model is `C-0041`'s hard-body one at nominal positions, so a real array is **less** likely to place, not more. |
| **Provenance** | `gpd/results/T-133-output-element-placement.json`, produced by `anchoring.OutputElementPlacementStudyKt`; model in `src/main/kotlin/anchoring/OutputElementPlacement.kt`; **6 cheap bounds, 6 mechanism rows, 11 candidate elements, 4 end-restraint window rows, 2 orientation sweeps of 720 samples, 15 sensitivities (8 axes + a 7-point fold sweep), 4 convergence records, 14 upstream reproductions, 6 predicates**; **30 gate-named tests in `src/test/kotlin/anchoring/OutputElementPlacementTest.kt`**; `tools/verify.sh` **BUILD SUCCESSFUL in 12 m 32 s — the whole suite, on its own isolated tree, with NOTHING dropped**; the result file re-run through `tools/study.sh` and reported *"no result file changed"*, and diffed **byte-for-byte identical** |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40.0 × 40.35 nm single-layer **square-lattice** Rothemund sheet, 15 duplexes at the SAXS **2.69 nm**, 0.34 nm rise, crossover phase **24**; `C-0063`'s **34** upward roots read from `gpd/results/T-125-*.json`; `C-0017`'s **33.3333 pN/nm** as a **sum**, so a per-path secant of **0.980392 pN/nm** at §3's **acceptable 3 nm**; `EI` = 230 pN·nm² (Fields et al.'s implied 172.906 swept), `S` = 1100 pN, `GJ` = 460 pN·nm², `k_θ` = 13.5294 pN·nm/rad (`α ∈ [0.6, 1.2]` swept), `C-0034`'s `A2` = 78.2353 pN·nm/rad, `C-0028`'s `B2` = 261.2 pN·nm/rad; allowables 10 pN unzip and `C-0049`'s `n·a/s` |
| **Consumes** | [`C-0065`](C-0065-crossbar-array-placement.md) (its flexure reading **consumed as data** from its result file and reproduced; `placeTrussArray`, `TrussStation`, `greedyConflictFreeElements` re-run as libraries), [`C-0063`](C-0063-upward-root-placement.md) (the 34 stations at phase 24, **read from its result file**; its bound 1 re-derived), [`C-0053`](C-0053-hinge-arm-array-packing.md) (`PlanElement`, `elementPackingVerdict`, `elementOrientationSweep`, the rooted footprint convention), [`C-0055`](C-0055-unused-junction-site.md) (`armDirections`, `upwardRootLattice`, the 10.88 nm pitch), [`C-0039`](C-0039-two-spring-elastica.md) (`TwoSpringElastica`, `elasticaArmForStiffness`, `elasticaArmCeiling`, `twoSpringArmForStiffness` — **re-run**), [`C-0034`](C-0034-guided-arm-anchorage.md) (`ArmAnchorage.twoTerminus`), [`C-0030`](C-0030-coupled-standoff-joint.md) (`coupledFlexureSpan`, `CoupledJointFlexure`, `standoffTipFlexibility`), [`C-0028`](C-0028-standoff-base-joint.md) (`StandoffBase.crossovers`, `standoffBucklingLoad`), [`C-0023`](C-0023-two-sided-coupling.md) (the two-mechanism statement, `TransverseDuplexFlexure`, `AxialDuplexStandoff`, `OneSidedSpacer`, `carriesCompression`), [`C-0049`](C-0049-compliance-ceiling-stroke.md) (the traversed-range reading and `n·a/s`), [`C-0041`](C-0041-flexure-array-packing.md) (level-independence), [`C-0017`](C-0017-output-coupling-stiffness.md) (the mandate and the six 2 mM floors, **CITED**), [`C-0066`](C-0066-arm-slab-tie-clearance.md) (the 2.71561 nm and the 2.73 nm flip), [`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile` |
| **Raises** | [`CH-0081`](../challenges/CH-0081-a-rigid-root-demands-a-longer-arm-than-the-plan-admits.md), against `C-0048`'s, `C-0062`'s and `C-0065`'s truss branch — the truss standoff is a **root**, and a rigid root demands a **longer** arm than the plan admits |

---

## The claim, in one line

**Every coupling element this programme has priced lies in the plan, and so the branch went looking out of it; but the normal direction offers only axial stiffness and asks for 1122 nm of it, a fold trades the only slack the placement has for a direction whose pitch is exactly one duplex, and a second level is reached only by a vertical member — while the plan itself was never the obstruction: a duplex bent between two supports needs `(48 EI/k)^(1/3) = 22.4 nm` and the same duplex bent from one support needs `(c EI/k)^(1/3)` at `c ≤ 12`, so the element that places is the one already in the design, and the whole of the plan budget reduces to a single inequality on the end-condition factor, `c ≤ 2.34`, which one crossover and one duplex end satisfy with 2 % and 3 % to spare.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, rotational stiffness **pN·nm/rad**, angles **degrees** in every reported number and radians in code; `k_BT = 4.141947 pN·nm` at **300 K** in aqueous **2 mM MgCl₂**.
- `x` runs **along** the host sheet's helices, `y` **across** them, `z` **normal and positive upward** — away from the grafted layer, which lies below the tile. Origin at the tile centre.
- **A duplex in plan is a rectangle of width `d = 2.69 nm`** (SAXS), so two parallel duplexes at exactly `d` are **tangent and admissible** — `C-0041`'s and `C-0053`'s convention verbatim. The 2.0 nm steric and 2.73 nm square-lattice readings are swept.
- **A rooted element occupies `[root, root ± L]` and the next along the same row may start at `high + d`** — `C-0053`'s footprint convention, carried unchanged through `armDirections`. It is the convention that turns the 10.88 nm root pitch into an 8.19 nm ceiling.
- **The stations are `C-0063`'s**: 34 upward (`EAST`) roots at crossover phase 24, four rows of three and eleven of two, 10.88 nm within a row and 5.44 nm between adjacent rows.
- **One load path is one element**, and `C-0017`'s mandate is a **sum**, so the per-path secant is `33.3333/34 = 0.980392 pN/nm` at §3's acceptable 3 nm. Placement is on the **secant**, stability on the **tangent** read over the traversed `[0, 3]` (`C-0049`).
- **"Lies in the plan"** means the element's compliant member is a body whose long axis is parallel to the sheet, so its length is charged against the sheet's own plan. Out of the plan means along `z`, folded, or nested at a second level.
- **Rows may be summed independently only while a body of the element's width cannot reach the next row.** Asserted in code and reported as a field; where it fails (the fold) the count falls back to `C-0053`'s greedy conflict-free subset, which is a **lower** bound.

---

## The six cheap bounds, which ran first — and the first three answer the task

| | bound | value | against | ratio | fired? | what it settled |
|---|---|---|---|---|---|---|
| **1** | rows of three forced by the count | **4** | 4 | 1.00 | — | `C-0063`'s bound 1 in a new place: `3a + 2(15 − a) = 34` forces four rows of three, so **every** 34-root placement carries one |
| **2** | **the row-of-three ceiling, `pitch − d`** | **8.19 nm** | 10.88 nm | 0.753 | **YES** | **the plan budget of any rooted element, over every placement on the lattice** — and the bisected ceiling over `C-0063`'s own rows agrees to `< 1e−9` nm |
| **3** | **the two-support flexure's own floor, `(48 EI/k)^(1/3)`** | **22.414 nm** | 8.19 nm | **2.737** | **YES** | **the answer for the whole `E3`/`C-0030` family** — refused at every span, every end joint and every placement, which is strictly stronger than `C-0065`'s 12 of 34 |
| **4** | **the normal direction, `S/k`** | **1122 nm** | 10 nm | **112.2** | **YES** | *"out of the plan along the surface normal"* asks for 112× `C-0017`'s whole envelope |
| **5** | **the rooted element's own kinematic floor, `1.5 × stroke`** | **4.50 nm** | 8.19 nm | 0.549 | **YES** | the window has a **floor** as well as a ceiling, so it is `4.50 … 8.19 nm` — **1.82× wide** |
| **6** | the across-row slack a fold would need | **0.00 nm** | 2.69 nm | 0 | **YES** | a fold buys a direction the lattice has none of — `C-0041`'s Fact A |

> **Bounds 2, 3 and 4 answer the acceptance question between them, and the packer was run to check them rather than to find them.** Falsifier 1 (*bound 2 coming out above the midspan family's floor, which would have made this a placement search*) **did not fire** — the two are 2.74× apart. Falsifier 2 (*a mechanism outside `C-0023`'s two*) **did not fire**. Falsifier 3 (*the exact elastica refusing every admissible pair*) **fired for one candidate and not for the family** — it is bound 5, and it is a finding.

---

## Deliverable 1 — the mechanism census: the whole element space in six rows

At the per-path secant `k₁ = 0.980392 pN/nm`. Every entry is a closed form and every one is asserted to reproduce `k₁` when its own length is fed back through its own law.

| mechanism | law | length demanded | × the 8.19 nm budget | two-sided? | in plan? | verdict |
|---|---|---|---|---|---|---|
| axial stretch of a duplex | `ℓ = S/k` | **1122 nm** | **137.0** | yes | **no** | **REFUSED** — and it is the only genuinely out-of-plane mechanism DNA has |
| an entropic single strand | `L_c = 3k_BT/(k b)` | **6.035 nm** | 0.737 | **NO** | no | **REFUSED on sidedness**, measured at negative argument, not assumed |
| **rotation at a hinge on a lever** | `r = √(k_θ/k)` | **3.715 nm** | **0.454** | yes | yes | **ADMITTED** — the most compact mechanism DNA offers |
| **bending, supported once, loaded at the far end** | `L = (c EI/k)^(1/3)`, `c ∈ (0, 12]` | **≤ 14.120 nm** | ≤ 1.724 | yes | yes | **ADMITTED CONDITIONALLY** — the budget cuts the family at **`c ≤ 2.3416`** |
| bending, supported twice, loaded at midspan | the same, `c ∈ [48, 192]` | **≥ 22.414 nm** | **≥ 2.737** | yes | yes | **REFUSED at every end condition** |
| torsion of a duplex on a lever | `L = GJ/(k r²)` | **64.842 nm** at a one-duplex lever | 7.917 | yes | yes | **REFUSED** — and the lever it would need is itself in plan |

**Three readings.**

1. **The only out-of-plane mechanism is axial, and it is refused by 137×.** That is the whole of *"a normal-standing member"*: a body along `z` loaded along `z` is loaded along its own axis, which is `C-0023`'s refused corner. To bend it instead the load must be **transverse to it**, i.e. in plan — which is the same element in a different pose.
2. **The entropic strand is the compact one and it is one-sided.** 6.04 nm, well inside the budget, and it places 34 of 34 at one level (`Q11` below) — and `carriesCompression` returns **false** at a 0.5 nm probe. `C-0023`'s hold-down verdict rests on two-sidedness, so it is refused for the reason `C-0023` already gives, re-measured here rather than inherited.
3. **The bending family splits in two by TOPOLOGY, not by material**, and the split is `c^(1/3)`: 22.41–35.58 nm supported twice, ≤ 14.12 nm supported once. That is the entire content of `C-0041`'s and `C-0065`'s negative, and it is one cube root.

---

## Deliverable 2 — the catalogue, placed and then run through every clause

`C-0063`'s 34 phase-24 stations, `C-0053`'s packer, and each element's own force law. **`c`** is the realised end-condition factor `k₁L³/EI`; **margin** is `8.19 − L`.

| | element | `L` [nm] | bp | `c` | margin | placed | levels | area/footprint | secant | tangent min over `[0,3]` | floors of 6 | per path | two-sided | **places** |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `Q1` | **`C-0030`'s coupled flexure, across the rows** (`C-0065`'s own reading) | **27.412** | 80.6 | 87.80 | **−19.22** | **12** | 6 | 1.553 | 33.333 | 30.89 | 6 | 2.941 | yes | **no** |
| `Q2` | the same, **along** the rows | 27.412 | 80.6 | 87.80 | −19.22 | 23 | 2 | 1.553 | 33.333 | 30.89 | 6 | 2.941 | yes | no |
| `Q3` | `C-0023`'s `E3a`, **pinned** ends | 22.414 | 65.9 | 48.00 | −14.22 | 23 | 2 | 1.270 | 33.333 | 33.33 | 6 | 2.941 | yes | no |
| `Q4` | `C-0023`'s `E3a`, **clamped** ends | 35.580 | 104.6 | 192.00 | −27.39 | 15 | 3 | 2.016 | 33.333 | 33.33 | 6 | 2.941 | yes | no |
| **`Q5`** | **`C-0039`'s `E5a1` — one crossover root, `C-0034`'s `A2` tip** | **8.16439** | **24.0** | **2.320** | **+0.0256** | **34** | **1** | **0.463** | **33.333** | **30.03** | **6** | **2.941** | **yes** | **YES** |
| `Q6` | the same arm with a **pinned** tip | *3.591* | 10.6 | 0.197 | +4.60 | **0** | — | 0.203 | — | — | — | — | yes | **no — refused by its own kinematics** |
| **`Q7`** | **the standoff-headed crank — `C-0028`'s `B2` base at 8 nm, pinned tip** | **5.331** | **15.7** | **0.646** | **+2.859** | **34** | **1** | **0.302** | **33.333** | **25.82** | **4** | **2.941** | **yes** | **YES** |
| `Q8` | a **RIGID** root with a pinned tip | 9.247 | 27.2 | 3.370 | **−1.057** | 24 | 2 | 0.524 | 33.333 | 29.67 | 6 | 2.941 | yes | **no** |
| `Q9` | one crossover root with a **GUIDED** tip | 10.224 | 30.1 | 4.556 | −2.034 | 23 | 2 | 0.579 | 33.333 | 30.55 | 6 | 2.941 | yes | no |
| `Q10` | `C-0023`'s `E1` — a duplex along `z`, loaded along `z` | 1122.0 | 3300 | — | −1113.8 | 0 | — | 63.58 | 33.333 | 33.33 | 6 | 2.941 | yes | no |
| `Q11` | `C-0023`'s `E2` — a single strand | 6.035 | 17.8 | 0.937 | +2.155 | **34** | **1** | 0.342 | *39.83* | 33.33 | 6 | 3.514 | **NO** | places, **fails sidedness** |

**The clause funnel: 11 rows → 3 place 34 at one level → 2 survive every clause.** The row lost between the last two is `Q11`, the strand, whose entropic law is not linear (so its 3 nm secant, 39.83, is not its own Gaussian constant) and which carries no compression at all.

Four things fall out.

1. **`Q6` is refused for being TOO SHORT, and that is a new kind of refusal in this branch.** A pinned tip on a one-crossover root asks for 3.591 nm; `C-0039`'s exact solver refuses any arm below `1.5 ×` the stroke, where the tip turns more than 42° and its chord draw-in `L − √(L² − δ²)` is a large fraction of the arm. **It is short enough to place and too short to work** — the window has a floor.
2. **`Q8` is the truss reading and it fails by 12.9 %.** A *rigid* root with a free tip is `c = 3` exactly in the small-rotation limit; solved exactly it is 9.247 nm (`c = 3.37`, the exact arm always stiffens, `C-0039`), against an 8.19 nm budget — 24 of 34. **A stiffer root demands a longer arm**, which is [`CH-0081`](../challenges/CH-0081-a-rigid-root-demands-a-longer-arm-than-the-plan-admits.md).
3. **Orientation does not rescue the flexure and does not decide the arm.** Over 720 orientations on the same 34 anchors, the 27.41 nm flexure reaches **0** single-level orientations with a minimum of **14** overlapping pairs (best angle 123.5°); the 8.164 nm arm reaches **62**, best angle **0°** — the sheet's own helix direction, exactly as `C-0041` and `C-0053` found for their own elements.
4. **`Q2` beats `Q1` and neither places.** Running `C-0030`'s flexure **along** the rows instead of across them takes it from 12 of 34 at 6 levels to 23 of 34 at 2 — a real improvement that changes no verdict, and worth recording because `C-0065` evaluated only the across-the-rows pose.

---

## Deliverable 3 — the budget is one inequality on `c`, and both of the arm's joints sit inside it by 2–3 %

The plan budget `L ≤ 8.19 nm` is, at a fixed per-path stiffness, exactly

&nbsp;&nbsp;&nbsp;&nbsp;**`c(ρ_n, ρ_f) ≤ k₁ L_max³/EI = 2.3416`.**

Bisected on `C-0039`'s exact elastica, that is a window in the two end restraints:

| held | its value | the other end's ceiling | what the design actually uses | inside by |
|---|---|---|---|---|
| **root = one crossover** | 13.5294 | **tip ≤ 79.678 pN·nm/rad** | `C-0034`'s `A2`, **78.235** | **1.8 %** |
| root = two crossovers | 27.0588 | tip ≤ 47.624 | — | — |
| root = `C-0028`'s `B2` standoff head at 8 nm | 25.8990 | tip ≤ 49.460 | a pin, 0 | — |
| **tip = `C-0034`'s `A2`** | 78.2353 | **root ≤ 13.930 pN·nm/rad** | one crossover, **13.5294** | **2.9 %** |

> **Both of the arm's joints are within 3 % of the largest the plan admits, and neither was chosen for that reason** — `C-0055` chose the root because it is the unused upward azimuth and `C-0034` chose the tip because a duplex end has exactly two strand termini. That the two land inside a bound neither knew about is the sharpest thing in this claim, and it is also its largest exposure: **the design has no margin on either joint.**

---

## Deliverable 4 — the three escapes out of the plan, each priced

| escape | how it was priced | outcome |
|---|---|---|
| **along the surface normal** | a body along `z` loaded along `z` is loaded **axially**: `S/k₁` | **1122 nm** against `C-0017`'s 10 nm envelope — **112×**. To bend it instead, the load must be transverse to it, which is the plan again |
| **folded or serpentine** | a two-limb fold halves the along-row demand and doubles the across-row one; swept over the limb at **equal contour** against the straight element | **it shrinks the budget.** At contours 2–7 nm both place 34; at the straight element's own 8.19 nm ceiling the fold places **28**; above it neither places (28 against 23 at a 10 nm contour). The along-row slack is 2.69 nm and the across-row slack is **exactly zero** |
| **nested or stacked** | a second level is reached only by a **vertical member**, and `C-0041`'s clash between two vertical members is level-independent | asserted as a gate-3 test: two instances at one station clash at **every** level assignment (`levelsRequired` returns the unrealisable sentinel). And the acceptance asks for **one** level in any case |

**The fold's price beyond the plan is not priced here and is named**: every fold corner is a 90° junction, which is `C-0059`'s marginal motif and which `CLAUDE.md` records cannot be made by a nicked continuation. The geometry alone already says the fold buys nothing, so the junction cost was not spent.

---

## Deliverable 5 — the sensitivities, and the one axis that closes the margin

| axis | reading | budget | arm | placed | verdict moves? |
|---|---|---|---|---|---|
| **reference** | 2.69 nm SAXS, `EI` 230, one crossover, 34 paths | 8.19 | **8.164** | **34** | — |
| **exclusion width** | **2.73 nm, the square-lattice SAXS value** | **8.15** | 8.164 | **18** | **YES** |
| exclusion width | 2.0 nm, the steric diameter | 8.88 | 8.164 | 34 | no |
| duplex `EI` | Fields et al.'s implied 172.906 | 8.19 | 7.883 | 34 | no |
| crossover `α` | 0.6, the bottom of Chen et al.'s bracket | 8.19 | 7.793 | 34 | no |
| **crossover `α`** | **1.2, the top of the same bracket** | 8.19 | **8.332** | **30** | **YES** |
| **path count** | **45, `C-0015`'s own** | 8.19 | **9.131** | **24** | **YES** |
| path count | 15, `C-0041`'s buildable count | 8.19 | 5.963 | 34 | no |

> **Three axes close the 0.0256 nm margin and they are not exotic.** The 2.73 nm square-lattice interhelical distance (`C-0066`'s own flip, in a new place), the **top** of the fitted crossover bracket `α = 1.2` — an experimental range, not a modelling choice — and §3's own 45 paths. **The 34 is not a rounding of 45; it is the count the plan budget prefers**, because more paths make each element *longer* (`C-0023`'s `L ∝ n^(1/3)`).

---

## The five verification gates

Executed as **30 gate-named tests** in `src/test/kotlin/anchoring/OutputElementPlacementTest.kt`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a bending length is a **cube** root (doubling `c` or halving `k` multiplies it by `2^(1/3)`) and a hinge lever a **square** root (quadrupling `k_θ` doubles it); an axial length is linear in `S` and a torsional one inverse in the lever squared; the per-path budget is inverse in the count; **the whole placement verdict is dimensionless** — scaling every length by ten leaves the placed, level, overlap and clash counts identical and the area fraction unchanged; unphysical arguments throw at **sixteen** entry points, including a zero end factor, a negative hinge stiffness, a zero lever, a backwards row, a non-ascending root list and a zero bisection resolution | **PASS** |
| **2 — limiting cases** | **THE FREE LIMITING CASE — `C-0030`'s flexure reproduces `C-0065`'s 12 of 34** at 27.4119472 nm, 7 levels and 186 overlapping pairs, through `C-0065`'s own `placeTrussArray`; the solved ceiling over `C-0063`'s rows **equals the closed form** `pitch − d`; an element at the ceiling places 34 at one level and one hair longer does not; one element places at one level at **all 36** orientations; **a rigid root demands `c = 3` and is already outside the budget**; the entropic path reproduces `FreelyJointedChain`'s own Gaussian constant and is **not** two-sided; a fold never places better than the straight element of the same contour | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the length ceiling is **resolution independent** over 1e−6 → 1e−12 (departure `0.0`); the placed elastica arm is **RK4-step independent** over 200 → 800 (`0.0`); the orientation sweep is **sample-count independent** over 180 → 2880 (`0.0`); the assembled tangent minimum is **stroke-sample independent** over 150 → 1200 (`0.0`); the placement is deterministic on repeat calls; the result file re-run through `tools/study.sh` reported *"no result file changed"* and diffed **byte-for-byte identical** | **PASS** |
| **5 — literature and upstream** | **14 reproductions, worst strict departure `3.0e−7`** apart from one definitional row: `C-0065`'s 27.4119472 nm span (`0.0`) and its **12 of 34** (`0.0`); `C-0055`/`C-0063`'s 10.88 nm pitch (`0.0`), 34 stations (`0.0`) and **8.16439 nm** arm (`1.0e−7`); `C-0066`'s **2.71561 nm** (`3.0e−7`); `C-0017`'s 33.3333 (`1.0e−9`); `C-0049`'s 113.333 (`2.9e−9`); `C-0034`'s 78.2353 (`0.0`); `C-0009`'s 13.5294 (`2.6e−9`); `C-0063`'s four rows of three (`0.0`); `C-0025`'s `c(0) = 48` (`0.0`); the SAXS 2.69 nm | **PASS** |

### Gate 3 — six things that are not restatements of the construction

1. **The placement verdict is invariant under a rigid rotation of the whole array** — every element turned together about an off-centre pivot by 0.17, 0.9, `π/2` and 2.0 rad; overlap, block, clash and level counts unchanged. `C-0041`'s and `C-0053`'s test on a third element.
2. **The length ceiling is invariant under the placement's own centro-symmetry** — the rows reflected through `(x, y) → (−x, −y)` return the same 8.19 nm to `1e−9`.
3. **`bendingLengthForStiffness` and `bendingFactorForLength` are exact inverses** at six factors spanning 0.5 → 192, to `1e−12`.
4. **Every mechanism reproduces its own stiffness when its own length is fed back** through its own law — the census is a set of identities and not a table.
5. **An element with no vertical member cannot be blocked**, so its level count is decided by overlap alone; the falsifier is that giving the same array a tip vertical member makes blocking possible in principle.
6. **Stacking cannot separate two rooted elements that share a root**: two instances at one station clash at every level assignment, and the packer returns the unrealisable sentinel. `C-0041`'s level-independence, on this element.

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **F1** | the plan budget coming out above the two-support flexure's floor | **no** | 8.19 against 22.41 — **2.74×**, so the family is refused on every placement and not only `C-0063`'s |
| **F2** | a compliance mechanism outside `C-0023`'s two | **no** | six rows, all reducible to entropic or bending |
| **F3** | the exact elastica refusing every admissible end-restraint pair | **for ONE candidate, not for the family** | `Q6` at 3.591 nm falls through the solver's own `1.5 × stroke` floor — which is bound 5, and a finding |
| **F4** | the 34-instance placement of an admissible element failing at one level | **no** | `Q5` and `Q7` both place 34 at one level, 0 overlaps, 0 clashes |

**A result that was not anticipated:** the search for an element *out of* the plan returned an element *in* it, and the reason is that **the plan was never what refused the flexure — the flexure's own end condition was.** `C-0041` framed the obstruction as a packing, `C-0065` re-measured it at 34 paths and on a real placement, and both are correct; what neither could see is that `c^(1/3)` separates the two bending topologies by 2.74× before any lattice is consulted.

**A second one:** the two joints `C-0055` and `C-0034` chose for entirely unrelated reasons — the unused upward azimuth, and the fact that a duplex end has exactly two strand termini — land **1.8 %** and **2.9 %** inside a bound that this task derived and neither knew about.

---

## What the branch should build, and what it costs

| | `Q5` — the hinge-rooted arm | `Q7` — the standoff-headed crank |
|---|---|---|
| length | **8.16439 nm = 24.0 bp** | **5.331 nm = 15.7 bp** |
| margin to the plan budget | **0.0256 nm** (0.075 of a base-pair rise) | **2.859 nm** |
| root | **one antiparallel crossover** at `C-0055`'s `EAST` site | `C-0028`'s `B2` — two crossovers across, on a duplex standing normal to the sheet |
| tip | `C-0034`'s `A2`, a duplex end | a pin |
| placed | **34 of 34, one level, 0 overlaps, 0 clashes** | **34 of 34, one level** |
| plan area | 0.463 of the footprint | 0.302 |
| assembled tangent min over `[0, 3]` | **30.03 pN/nm — clears 6 of `C-0017`'s six** 2 mM floors | 25.82 — clears **4 of 6** |
| per-path force at 3 nm | 2.941 pN against the 10 pN unzip allowable — **3.4×** | the same |
| `C-0049`'s per-path secant ceiling | 113.33 pN/nm against a 33.33 secant — **3.4×** | the same |
| compression member | **none** | its standoff: **2.941 pN** duty against a **7.207 pN** free-head Euler load — **2.45×** (`CH-0037`'s reading, the element's own force and not the mandate secant) |
| motif risk | a free lever on one crossover — **undemonstrated** (`C-0055`) | that, **plus** a duplex standing normal to a single-layer sheet — **also undemonstrated** (`C-0028`, `C-0029`) |

**The recommendation is `Q5`, and it is the element the branch already has.** `Q7` is 1.53× shorter and buys 112× the plan margin, and it pays for it in a second undemonstrated motif, a compression member and two of `C-0017`'s six stability floors. **The truss array is not the output stage** — it places 34 times as a standoff (`C-0065`) and the element it would cap is either a two-support flexure (refused by 2.74×) or an arm on a rigid root (refused by 12.9 %).

---

## Validity range

- **TRL 1–3. Nothing here is measured, and neither motif is demonstrated.** `C-0055`'s and `C-0029`'s literature findings are unchanged and upstream of every number.
- **A *"places"* verdict is the weak direction.** The plan model is `C-0041`'s and `C-0053`'s hard-body one at nominal positions: no thermal excursion, no assembly tolerance, no out-of-plane bow. A real array is **less** likely to place. The 0.0256 nm margin on `Q5` is 0.075 of a base-pair rise and **any** tolerance model erases it.
- **The 8.19 nm ceiling is a property of `C-0053`'s footprint convention**, in which consecutive collinear elements need a full duplex of clearance. At a zero-gap convention it would be the bare 10.88 nm pitch; the convention is inherited and restated rather than re-derived, and the whole verdict on `Q5` lives inside its 0.0256 nm.
- **The stations, the count and the phase are `C-0063`'s and `C-0055`'s.** A different placement re-solves `C-0055`'s self-consistent 34 and everything here; the row-of-three bound survives any placement on this lattice, but the *sensitivities* are read on `C-0063`'s own rows.
- **The stability floors are `C-0017`'s, CITED and not recomputed**, and they inherit `C-0005`'s 123–214 % one-loop correction — which is larger than the 6-of-6 against 4-of-6 that separates `Q5` from `Q7`. That count is a **re-reading**, never an establishment of stability at 2 mM.
- **The end-restraint window is bisected on `C-0039`'s exact elastica** and inherits its whole validity range, including the `1.5 × stroke` floor below which it will not solve — which is used here as a *result* (bound 5) and is a **solver** convention as much as a physical one. The kinematic statement underneath it is unconditional: a lever cannot deliver a stroke longer than its own length.
- **`Q7`'s standoff is priced for buckling and for nothing else.** Its base register, its own plan footprint as a vertical member, and `CH-0078`'s pinned-register problem are **not** evaluated; on `C-0065`'s evidence a vertical member at every station is exactly the thing that makes a clash level-independent, so `Q7` is offered as a **shorter element**, not as a cheaper one.
- **The fold is refused on geometry alone.** Its compliance is not modelled and its corner junctions are not priced; the geometric result (it shrinks the budget) is what the task needed, and the junction cost would only tighten it.
- **§3's DESIRED 10 nm stroke has no rooted element at all on this lattice**, because the plan budget 8.19 nm is below it and a lever cannot deliver a stroke longer than its own length. That corroborates `C-0050`'s kinematic ceiling and `C-0066`'s plan-view one from a third direction, and it is **not** a new result.
- **One flexure per load path**, exactly as `C-0023`, `C-0030`, `C-0039` and `C-0053` assume.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| the 34 stations, their phase and the 10.88 nm pitch | phase 24 | **`C-0063`/`C-0055`, CONSUMED AS DATA** from `gpd/results/T-125-*.json`, and the four-rows-of-three arithmetic re-derived |
| the flexure's published span, placed count and level count | 27.4119472 nm, 12, 7 | **`C-0065`, CONSUMED AS DATA** from `gpd/results/T-130-*.json`, and both re-derived here |
| `C-0017`'s six stability floors at 10 nm / 2 mM | 27.913, 23.414, 24.904, 27.039, 23.804, 23.953 pN/nm | **CITED**, `gpd/results/T-16-*.json`; each a mean-field solve, inheriting `C-0005`'s correction |
| duplex `EI`, `GJ`, `S` | 230, 460 pN·nm²; 1100 pN | **CITED, CanDo MODEL INPUTS** (Kim et al., *NAR* **40**:2862, 2012) / **MEASURED** (Wang et al. 1997); Fields et al.'s implied **172.906** swept |
| crossover hinge `k_θ` | 13.5294 pN·nm/rad, `α ∈ [0.6, 1.2]` | **CITED, FITTED**, Chen et al., *JACS* **136**:6995 (2014), via `C-0009`; **both ends of the bracket swept, and the top one moves the verdict** |
| `C-0034`'s `A2` couple, `C-0028`'s `B2` couple | 78.2353, 261.2 pN·nm/rad | **`C-0034`/`C-0028`**, re-derived here from their own libraries |
| ssDNA Kuhn length | 2.10 nm, zero-force end | **CITED, MEASURED** (Chen et al., *PNAS* **109**:799, 2012) |
| interhelical distance, rise, crossover spacing | 2.69 nm (2.73 square), 0.34 nm, 32 bp | **CITED, MEASURED** (SAXS, Fischer et al. 2016) / **CITED** (Rothemund 2006, Ke et al. 2009) |
| per-path unzip allowable | 10 pN | **CITED** via `C-0006`/`CH-0029` |
| §3 targets | 100 pN, 3 nm, 10 nm, 40 × 40 nm, 2 mM | **CITED** |

Everything else — the per-path budget, all five mechanism laws and their lengths, the row-of-three ceiling and its bisected confirmation, the end-factor ceiling, every candidate's placement and every clause it is graded on, the two-restraint window, both orientation sweeps, the fold sweep, the eight sensitivities and the four convergence records — is **derived here in code**, with `C-0023`'s, `C-0030`'s, `C-0034`'s, `C-0039`'s, `C-0053`'s, `C-0063`'s and `C-0065`'s pipelines **re-run rather than tabulated**.

## Still open — named, not answered

1. **`Q5` has no margin on either joint.** The tip may be no stiffer than 79.68 and `A2` is 78.24; the root no stiffer than 13.93 and one crossover is 13.53. Any upward revision of either — and `α = 1.2` is inside Chen et al.'s own fitted bracket — takes the element outside the plan budget. **`T-9` is now a placement question as well as a stiffness one.**
2. **`Q7`'s standoff is unassessed as a plan body.** It is a vertical member at every station, and `C-0041`'s level-independence is exactly about those. Its base register (`CH-0078`) is also unevaluated.
3. **The 0.0256 nm margin against a tolerance model.** No tolerance model exists anywhere in this programme, and this is the second claim (after `C-0066`'s bound 4) whose headline sits inside 0.03 nm.
4. **The fold's compliance and its corner junctions.** Refused on geometry here; a fold whose corners are `C-0059`-feasible 90° junctions has not been solved, and its compliance is a chain of hinges rather than a beam.
5. **Whether a different 34-root placement admits a longer element.** The row-of-three bound says no on this lattice; a placement with **four** arms in some row is arithmetically impossible at 34 on 15 rows, but a different *count* would re-open it — and a different count re-solves `C-0055`'s self-consistency.
6. **The flatness of a `Q7` array.** Its coupling enters at a standoff base, not at a crossover, and `C-0063`'s grillage has not been re-solved on it.

## Challenges

**Raises [`CH-0081`](../challenges/CH-0081-a-rigid-root-demands-a-longer-arm-than-the-plan-admits.md)** against `C-0048`'s, `C-0062`'s and `C-0065`'s truss branch: a truss standoff is a **root**, and a rigid root demands a *longer* arm than the plan admits — 9.247 nm against 8.19, placing 24 of 34 — so the array that places 34 times as a standoff cannot carry an output element at all.

**No number in `C-0065`, `C-0063`, `C-0055`, `C-0039`, `C-0034`, `C-0030`, `C-0025`, `C-0023`, `C-0017` or `C-0009` fails to reproduce** — 14 reproductions, worst strict departure `3.0e−7`. One reproduction is **definitional and reported as such**: `C-0065` records **7** levels for the flexure array with the truss blocks in the same conflict graph (asserted here as a gate-2 test through its own `placeTrussArray`), while the **bare** flexure array evaluated here needs **6**. They are two different bodies; the **placed count**, which is what both verdicts rest on, is the same **12**.

**None stands against this claim.** The five ways it would fail:

1. **A footprint convention in which consecutive collinear elements need no clearance.** The budget would rise from 8.19 to 10.88 nm and `Q5`'s margin from 0.026 to 2.72 nm. The convention is `C-0053`'s and is inherited; it is the single largest lever on this claim's headline.
2. **A compliance mechanism in DNA that is neither entropic nor bending.** `C-0023` establishes there is none; a third would make the census incomplete.
3. **A measurement of `k_θ` at the top of Chen et al.'s bracket.** At `α = 1.2` the arm is 8.332 nm and places 30 of 34, and the positive verdict becomes a negative one.
4. **A demonstration that two duplexes may sit closer than 2.69 nm in plan** — or, in the other direction, that the square lattice's 2.73 nm is the right number here, which alone takes `Q5` to 18 of 34.
5. **A tolerance model.** 0.0256 nm is 0.075 of a base-pair rise, and any real scatter erases it.
