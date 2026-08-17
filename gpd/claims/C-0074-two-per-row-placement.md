# C-0074 — **A flat 30-root design exists, and equal springs cannot build it.** The two-per-row constraint at 30 roots is an **identity** (`2 × 15 = 30`), so the family is small enough to enumerate exhaustively — and **no** placement of it, at **any** of the 32 phases, clears `T-5b`'s 0.10 with equal springs (best **0.1667**, against `C-0063`'s 0.0789 at 34). A **distribution** at `C-0017`'s unchanged total recovers it: the placement that keeps **every nanometre** of plan margin the lattice affords — **1.7645 nm, 68.9×** `C-0069`'s knife edge and **1.31×** `C-0072`'s own reduction — dishes **0.0682** over the whole traversed range at a peak ratio of only **2.06** and 6.86 pN per path. **It is at phase 8, not `C-0063`'s phase 24, where the same construction does not clear at all**

| | |
|---|---|
| **Task** | [`T-136`](../tasks/T-136.md), raised by [`C-0072`](C-0072-plan-tolerance-model.md)'s Deliverable 6 — *"the reduction rule used here is a PLAN rule … so 0.2603 is an **upper** bound. Re-running `C-0063`'s own search under a two-per-row constraint is the follow-on"* |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with **`A1.2`** for the anchoring array |
| **Verification type** | **logical** (two exact lattice bounds — a forced count vector and a maximum-over-placements plan ceiling — both free of any solve) **+ in-silico** (`C-0063`'s exhaustive centro-symmetric enumeration and its descent, `C-0068`'s multi-state bank and `C-0064`'s minimax, all re-run as libraries under a two-per-row cap on `C-0022`'s solved loads) |
| **Verdict** | **PASS, and the declared falsifier FIRED in the half it was written for.** **With equal springs there is no flat 30-root placement at all**: the exhaustive centro-symmetric family (34 992 candidates at each of the two phases the congruence admits, and the count vector is *forced* so this is the whole symmetric family) reaches **0.166653** at phase 24 and **0.172575** at phase 8, and a 12-start descent over the **non-symmetric** family at **every one of the 32 phases** reaches **0.1670** — against `T-5b`'s **0.10**, `C-0063`'s 34-root **0.0789** and a free tile at **0.3079**. Every phase improves on `C-0072`'s plan-rule **0.2603** and none clears the convention. **But the negative belongs to the equal springs, not to the station set**: the least-squares floor over *every* phase-24 upward root is **0.00071**, 140× below the convention, and the 30-parameter minimax at `C-0017`'s unchanged total and under `C-0049`'s per-path ceiling takes six of the eight priced placements inside 0.10. **The design that answers all three predicates at once is at phase 8**: the placement carrying the lattice's maximum plan ceiling, **9.5350 nm**, i.e. a margin of **1.76451 nm** — **68.9×** `C-0069`'s 0.02561 nm knife edge, **5.19** base-pair rises, and **1.31×** `C-0072`'s own 1.3495 nm — dishes **0.06822** over the whole range the placed 2 mM / 10 nm / 0.192 V device traverses, at a peak stiffness ratio of **2.057** and a peak path force of **6.857 pN** against the 10 pN unzip allowable. **The phase is what carries this, and it is not `C-0063`'s**: at phase 24 the maximum-ceiling placements reach only **0.11239** and **0.13188** even under a distribution, both pinned at the per-path ceiling. **Two exact bounds did most of the work and neither needs a solve** — the count vector at 30 is *forced* (`2 × 15 = 30`, so "two per row" is an identity and the family is a product of per-row 2-subsets), and the largest element any 30-root placement can keep is a **bisection on a monotone capacity**, 9.5350 nm, because a placement's ceiling is a `min` over independent rows. Raises [`CH-0086`](../challenges/CH-0086-a-plan-ceiling-is-a-property-of-a-placement-not-of-a-count.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED, and the MOTIF IS NOT DEMONSTRATED** — a free lever held to a single-layer sheet by one crossover is this programme's own construct (`C-0055`, 62 recorded queries). The plan model is `C-0041`'s hard-body one at **nominal** positions, so `C-0072`'s whole tolerance argument stands over this claim unchanged: a *"places"* verdict is the weak direction. |
| **Provenance** | `gpd/results/T-136-two-per-row-placement.json`, produced by `anchoring.TwoPerRowPlacementStudyKt`; model in `src/main/kotlin/anchoring/TwoPerRowPlacement.kt`; **5 cheap bounds, 14 lattice-ceiling rows, 4 exhaustive family records over 69 984 enumerated placements, 16 Pareto rows, 20 distribution rows, 5 re-evaluated tolerance floors, 64 descent records over 202 026 evaluations, 3 cost rows, 4 convergence records, 13 upstream reproductions, 6 predicates, 7 findings**, and the recommended placement emitted row by row; **16 gate-named tests in `src/test/kotlin/anchoring/TwoPerRowPlacementTest.kt`**; `tools/verify.sh` **BUILD SUCCESSFUL in 14 m 8 s** with two sibling files mid-TDD dropped (`brush/FirstMomentThicknessTest.kt`, `anchoring/WeaveExclusionWidthTest.kt`); the result file re-run through `tools/study.sh` and diffed **byte-for-byte identical** |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40.0 × 40.35 nm single-layer **square-lattice** Rothemund sheet, 15 duplexes at the SAXS **2.69 nm**, 0.34 nm rise; `C-0022`'s **solved** edge collars at 10 nm and 7 nm, **both at 0.192 V** — the two ends of `C-0018`'s placed device (`C-0068`'s range); `C-0017`'s **33.3333 pN/nm** as a **sum** over 30 paths, i.e. a per-path secant of **1.11111 pN/nm** at §3's **acceptable 3 nm**; `C-0039`'s exact elastica arm **7.77049 nm = 23 bp** on a one-crossover root (`k_θ` = 13.5294 pN·nm/rad) and `C-0034`'s `A2` tip (78.2353); `EI` = 230 pN·nm²; `C-0053`'s footprint convention at `d` = 2.69 nm; `C-0001`'s foundation secant; free-tile stroke **4.90731 nm** |
| **Consumes** | [`C-0072`](C-0072-plan-tolerance-model.md) (its 30-root reduction re-run through its own `rowsWithoutInteriorRoots`; 0.26028, 9.12 nm and 1.3495 nm all reproduced), [`C-0068`](C-0068-range-robust-placement.md) (`MultiStateRootBank`, the device range, 0.0789 and the 0.0291 minimax both reproduced), [`C-0063`](C-0063-upward-root-placement.md) (`centroSymmetricPlacements`, `descendPlacement`, `centroSymmetricUpwardPhases`, the 34 stations **read from its result file**, 0.0706 reproduced), [`C-0069`](C-0069-output-element-placement.md) (`rootedLengthCeiling`, `rowOfThreeLengthCeiling`, `maximumRootedElementsInRow`, `StationRow`), [`C-0064`](C-0064-robust-distribution.md) (`MultiStateSurrogate`, `minimaxStiffnessDistribution`, `reachableDishingFloor`), [`C-0055`](C-0055-unused-junction-site.md) (`upwardRootLattice`, `armDirections`, the 10.88 nm pitch), [`C-0039`](C-0039-two-spring-elastica.md) (`elasticaArmForStiffness`), [`C-0049`](C-0049-compliance-ceiling-stroke.md) (`perPathStiffnessCeiling`), [`C-0022`](C-0022-collar-on-the-equilibrium-path.md) (the solved collars, **read from its result file**), [`C-0017`](C-0017-output-coupling-stiffness.md) (the mandate, **CITED**), [`C-0006`](C-0006-tile-flatness.md)/`T-5b` (the 0.10 convention and the free-tile stroke) |
| **Raises** | [`CH-0086`](../challenges/CH-0086-a-plan-ceiling-is-a-property-of-a-placement-not-of-a-count.md) against `C-0072`'s Deliverable 6 and, through it, `C-0069`'s ceiling column |

---

## The claim, in one line

**Reducing `C-0063`'s array from 34 arms to 30 buys the plan margin `C-0072` says it does and more — 1.7645 nm rather than 1.3495, because the ceiling is a property of the placement and not of the count — but it costs the one thing that made `C-0063` remarkable, which is that its 34 springs are EQUAL: no two-per-row placement of any phase is flat with equal springs, and the flat 30-root design needs a 2.06× stiffness distribution and lives at phase 8 where the 34-root design lived at phase 24.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, pressure **pN/nm²** (= 1 MPa exactly); `k_BT = 4.141947 pN·nm` at **300 K** in aqueous **2 mM MgCl₂**.
- `x` runs **along** the helices, `y` **across** them, `z` **normal and positive upward** — away from the grafted layer, which lies below the tile. `w` is positive **downward**. Origin at the tile centre.
- **Dishing** is the peak absolute departure from the area-weighted best-fit plane on the same **81 × 81** grid as `C-0026`, `C-0047`, `C-0058`, `C-0063` and `C-0064`, quoted over `C-0006`'s free-tile stroke, **4.90731 nm**.
- **A root** is the crossover tying one arm to its host duplex, on the unoccupied **`EAST`** (upward) azimuth; the coupling enters the sheet there.
- **A rooted element occupies `[root, root ± L]` and the next along the same row may start at `high + d`** — `C-0053`'s footprint convention, carried unchanged through `armDirections`.
- **An operating range is the set of states one device traverses** (`CH-0077`, `C-0068`): here gaps 10 → 7 nm at the device's own 0.192 V, both solved by `C-0022`.
- **Every argmin here is decided at six significant digits** with the placement's own canonical key as the tie-break, and emitted at nine — `CLAUDE.md`'s rule, with the absolute floor lowered to `1e−12` because a dishing ratio is **dimensionless** and `RESULT_ABSOLUTE_FLOOR` is a claim about pN.

---

## The five cheap bounds, which ran first — and two of them are proofs

| | bound | value | against | fired? | what it settled |
|---|---|---|---|---|---|
| **1** | **the per-row count 30 roots FORCE at a cap of two** | **2** | — | — | `2 × 15 = 30` **exactly**, so *"two per row"* is an **identity**, not a constraint: every row carries two and the design space is a product of per-row 2-subsets. `C-0063`'s bound 1 read at the other cap, and it shrinks the centro-symmetric family from 361 584 at 34 roots to **34 992** at 30 — small enough to enumerate exhaustively |
| **2** | **the largest element ANY 30-root placement can keep** | **9.5350 nm** | 7.77049 nm arm | — | a placement's ceiling is a `min` over **independent** rows, so `max over placements` is `sup{L : Σ_r cap_r(L) ≥ n}` — a bisection on a monotone capacity, exact, and no solve. `C-0072`'s own reduction reaches **9.12** |
| **3** | the least-squares floor over **every** phase-24 upward root, design state | **0.00071** | 0.10 | **no** | a rigorous lower bound on every 30-root subset under every distribution — 140× below the convention, so the falsifier **could not** fire before the enumeration and the enumeration was necessary |
| **4** | the same floor at the compressed end of the range | **0.00049** | 0.10 | **no** | the range floor is the larger of the two |
| **5** | `C-0072`'s standing upper bound | **0.26028** | — | — | its plan-rule reduction is a **member** of the family searched here, so the search cannot come out worse — and it does not |

> **Bound 2 is the sharpest thing in this claim and it costs nothing.** It is what turns *"C-0072 reports 9.12 nm at 30 roots"* into *"9.12 nm is what C-0072's own reduction rule happens to reach"*, and it is [`CH-0086`](../challenges/CH-0086-a-plan-ceiling-is-a-property-of-a-placement-not-of-a-count.md).

---

## Deliverable 1 — the plan ceiling as a function of the count, exactly

`maximumPlanCeilingForCount` over the phase-24 upward lattice; identical at phase 8.

| paths | cap | count vector | **the lattice's ceiling** | `C-0072`'s ceiling | arm | **best margin** | `C-0072`'s margin | understated by |
|---|---|---|---|---|---|---|---|---|
| 45 | 3 | 15 × 3 | 8.1900 | — | 9.13116 | **−0.9412** | — | the arm does not place at all |
| **34** | 3 | 4 × 3 + 11 × 2 | **8.1900** | 8.1900 | 8.16439 | **0.02561** | 0.02561 | — (a row of three is forced) |
| 31 | 3 | 1 × 3 + 14 × 2 | 8.1900 | 8.1900 | 7.87152 | 0.3185 | 0.3185 | — |
| **30** | **2** | **15 × 2** | **9.5350** | 9.1200 | **7.77049** | **1.76451** | 1.3495 | **1.31×** |
| 28 | 2 | 13 × 2 + 2 × 1 | 9.5350 | 9.1200 | 7.56281 | 1.9722 | 1.5572 | 1.27× |
| 25 | 2 | 10 × 2 + 5 × 1 | 9.5350 | 9.1200 | 7.23574 | 2.2993 | 1.8843 | 1.22× |
| **22** | 2 | 7 × 2 + 8 × 1 | **14.9750** | 9.1200 | 6.88711 | **8.0879** | 2.2329 | **3.62×** |
| **15** | 1 | 15 × 1 | **30.8800** | 20.0000 | 5.96298 | **24.917** | 14.037 | **1.78×** |

**Three readings.**

1. **The step is at 31, and it is exactly where a row of three becomes arithmetically unavoidable.** `3a + 2(15 − a) = n` needs `a = n − 30`, so `n ≥ 31` forces one and the ceiling collapses to `C-0069`'s `pitch − d = 8.19 nm`. At `n ≤ 30` no row need carry three and the ceiling is set by the **tile edge and the footprint convention** instead.
2. **The ceiling above 30 is not the tile edge, it is the converging pair.** `armDirections` lets two arms in a row point at each other, so a pair at `±16.32` admits `(16.32 + 16.32 − 2.69)/2 = 14.975 nm` and a pair at `±10.88` admits `(10.88 + 10.88 − 2.69)/2 = 9.535`. The 9.535 binds at 30 because every row must carry two and the three-site rows have nothing better.
3. **`C-0072` understates its own escape by 1.31× at 30 and by 3.62× at 22.** Its reduction rule takes the interior root of the fullest row, which is optimal for *dissolving a row of three* and is not optimal for the *ceiling*.

---

## Deliverable 2 — the exhaustive two-per-row family, and the 32-phase descent

`C-0063`'s congruence admits exactly **two** centro-symmetric phases, **8** and **24**, and at each the two-per-row family is **34 992** placements — enumerated in full, under both objectives at once.

| phase | columns | enumerated | **best** | median | worst | free tile | best margin | flat at 0.10? |
|---|---|---|---|---|---|---|---|---|
| 8 | 8 | **34 992** | **0.172575** | 0.438433 | 0.929285 | 0.307902 | 0.4195 | **no** |
| 24 | 8 | **34 992** | **0.166653** | 0.479175 | 0.971955 | 0.307902 | 0.4195 | **no** |

Both objectives — the design state alone, and the worst over the traversed range — return the **same** placement and the **same** value at both phases: at 30 roots the rest state binds the range, so the range objective adds nothing. (At 34 roots it did: `C-0068` moved 0.0706 → 0.0789.)

The non-symmetric family is `3.7e9` and is searched instead by `C-0063`'s own `descendPlacement` with `minimumPerRow = maximumPerRow = 2`, from a greedy start and eleven deterministic pseudo-random ones, **at every one of the 32 phases** — 202 026 evaluations in total.

| | range best over 32 phases |
|---|---|
| **the ten eight-column phases** (`C-0015`'s) | **0.1670 … 0.1865** |
| the twenty-two seven-column phases | 0.1729 … 0.2472 |
| **flat at 0.10** | **0 of 32** |

> **`C-0015`'s ten reappear**, as they have in five previous constructions: the eight-column phases occupy the good end of the band. **And not one of the 32 clears the convention.** The best overall is phase 7 at **0.1670**, statistically indistinguishable from the exhaustive phase-24 answer of **0.166653** — which is the useful cross-check, because the two searches are over different families by different methods and land 0.2 % apart.

---

## Deliverable 3 — the distribution, and the design that answers all three predicates

The 30-parameter minimax over the same range, at `C-0017`'s **unchanged** total and under `C-0049`'s per-path ceiling of 3.3333 pN/nm.

| placement | phase | equal springs | **minimax** | floor | peak ratio | peak path force | plan margin | flat? |
|---|---|---|---|---|---|---|---|---|
| `C-0063`'s **34** roots — the knife edge | 24 | **0.07885** | **0.02911** | 0.0031 | 1.74 | 5.128 | 0.0256 | **yes, both** |
| `C-0072`'s plan-rule 30 roots | 24 | 0.26028 | **0.07321** | 0.0057 | **3.00** | **10.000** | 1.3495 | only with a distribution |
| flattest with equal springs | 8 | 0.17258 | **0.06484** | 0.0070 | 2.35 | 7.818 | 0.4195 | only with a distribution |
| **largest plan ceiling** | **8** | 0.24236 | **0.06822** | 0.0055 | **2.057** | **6.857** | **1.76451** | **only with a distribution** |
| flattest at the largest ceiling | 8 | 0.1878 | **0.07146** | 0.0048 | 2.45 | 8.175 | 1.76451 | only with a distribution |
| flattest with equal springs | 24 | **0.16665** | 0.08936 | 0.0058 | 2.44 | 8.119 | 0.4195 | only with a distribution |
| largest plan ceiling | 24 | 0.35543 | **0.13188** | 0.0078 | **3.00** | **10.000** | 1.76451 | **NO** |
| flattest at the largest ceiling | 24 | 0.2017 | **0.11239** | 0.0060 | **3.00** | **10.000** | 1.76451 | **NO** |

**Four readings, and the last is the recommendation.**

1. **The negative belongs to the equal springs.** Six of the eight priced placements clear 0.10 once the distribution is free, at peak ratios of 2.06–2.45 — well inside what `C-0060` prices as buildable. `C-0063`'s headline was never *"34 roots are flat"*, it was *"34 EQUAL springs are flat"*, and **that** is what the reduction to 30 spends. This is [`CH-0080`](../challenges/CH-0080-the-equal-spring-advantage-belongs-to-the-ten-nanometre-layer.md) on a new axis: **the equal-spring advantage belongs to a COUNT as well as to a layer.**
2. **Phase 24 cannot carry the margin.** Both of its maximum-ceiling placements saturate the per-path ceiling (ratio 3.00, 10.000 pN) and still read 0.11239 and 0.13188. Phase 8's reads 0.0682 at 2.06. `C-0068` found *"the crossover phase is selected by the LAYER"*; here it is selected by the **plan margin**, on the same lattice and the same load.
3. **Equal-spring flatness does not predict minimax flatness.** At phase 8 the placement chosen for equal-spring flatness (0.1726 → 0.0648) and the one chosen for the ceiling (0.2424 → **0.06822**) end up 4.5 % apart under a distribution, having been 40 % apart with equal springs — and the *worse* equal-spring placement is the one that keeps the whole margin. Selecting a placement on the equal-spring objective is selecting on the wrong quantity when a distribution is available.
4. **The recommendation** is **30 roots at phase 8 on the maximum-ceiling placement**: margin **1.76451 nm** (5.19 base-pair rises, **68.9×** `C-0069`'s knife edge), dishing **0.06822** over the whole traversed range, peak ratio **2.057**, peak path force **6.857 pN** against 10 pN unzip and **3.333 pN** of §3's own duty per path. Its fifteen rows are emitted in `recommendedPlacement`; they are `{±10.88}` on the odd rows and `{±16.32}` on the even ones, centro-symmetric by construction.

---

## Deliverable 4 — `C-0072`'s five floors, re-evaluated at the 30-path arm

`C-0072` erected four floors against the 0.02561 nm knife edge and found a fifth in the literature.
**Two of them scale with the arm**, so they are re-computed here rather than carried across —
the arm shortens from 8.16439 to **7.77049 nm** when the path count falls to 30.

| floor | σ [nm] | × the 0.02561 nm knife edge | **× the 1.76451 nm recommended margin** | cleared at 30 roots? |
|---|---|---|---|---|
| the base-pair **rise** — the design quantum | 0.34000 | **13.28** | 0.193 | **yes, 5.2×** |
| the two measured SAXS interhelical means, 2.73 − 2.69 | 0.04000 | 1.56 | 0.023 | **yes, 44×** |
| the **thermal axial breathing** of the two segments the margin differences (32 bp host + **23 bp** arm) | 0.26535 | 10.36 | 0.150 | **yes, 6.6×** |
| **the arm tip's own bending at a PERFECTLY RIGID root** | **1.67822** | 65.53 | **0.951** | **yes, but by only 1.05×** |
| **Fischer et al.'s fitted single-layer lattice-constant width**, `w_a` = 2.5 Å | 0.25000 | 9.76 | 0.142 | **yes, 7.1×** |

**Two readings.**

1. **The margin becomes quotable, and it is the only one in this branch that ever has been.** Every floor `C-0072` erected — including the one measurement in print, Fischer et al.'s 9.1 % lattice-constant width for a single-layer sheet, which is **9.76×** the knife edge — sits inside 1.76451 nm.
2. **The binding floor is the arm's own bending, and it clears by 1.05 %, not by a factor.** It falls from 1.80744 to 1.67822 nm purely because the arm shortens (`σ ∝ L^(3/2)`), which is the whole of that clearance: a 30-root design with `C-0069`'s 8.164 nm arm would **not** clear it. The path-count reduction and the arm re-sizing are one move (`T-138`), and this is where that matters most.

---

## Deliverable 5 — the cost, and what does not change

| | `C-0063`'s 34 roots | `C-0072`'s 30 | **T-136's recommended 30** |
|---|---|---|---|
| per-path secant | 0.98039 pN/nm | 1.11111 | 1.11111 |
| §3's duty per path at 3 nm | 2.94118 pN | 3.33333 | 3.33333 |
| peak **solved** path force, equal springs | 2.29825 pN | 3.05572 | 2.79720 (phase-24 argmin) |
| peak path force under the recommended distribution | 5.128 pN | 10.000 | **6.857** |
| `C-0014` thermal force per path | 0.34559 pN | 0.39167 | 0.39167 |
| within the 10 pN unzip allowable | yes | at the ceiling | **yes, 1.46× clear** |

Every count keeps the per-path force inside the unzip allowable, exactly as `C-0072` reports; what the reduction costs is the **stiffness ratio**, and `C-0072`'s own plan-rule placement is the one that spends it all.

---

## The five verification gates

Executed as **16 gate-named tests** in `src/test/kotlin/anchoring/TwoPerRowPlacementTest.kt`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a plan ceiling is a **length** and scales exactly by ten when every length does; the lattice capacity falls monotonically with the element length and is capped per row; the forced per-row count is an integer identity; a count the lattice cannot carry returns `null` rather than throwing, and unphysical arguments throw at **nine** entry points | **PASS** |
| **2 — limiting cases** | one root per row gives the tile-edge ceiling `20 + max\|x\|` = **30.88 nm**; a row of three gives exactly `rowOfThreeLengthCeiling` = **8.19 nm**; the ceiling steps down at **31** and not at 34; capping at two and at three give the **same** maximum at 30, so the cap is not a restriction there; an equal-spring coupling of vanishing total stiffness reproduces the free tile to `1e−6` | **PASS** |
| **3 — symmetry and conservation** | **a uniform load on a free tile dishes exactly zero** (< `1e−9`); **the closed-form maximum ceiling equals an EXHAUSTIVE maximum** over every two-per-row placement of a toy lattice, at both caps; **the sliced multi-state bank equals an assembled `OrigamiGrillage` solve** at the same 30 stations, in the peak dishing (`1e−7`) and in every support force (`1e−6`); **forbidding the CONVERGING pose returns exactly `C-0072`'s own 9.1200 nm** at both symmetric phases, which is `CH-0086`'s central claim measured rather than argued; no concrete placement beats the closed-form ceiling; a placement's rows reflected through `(x, y) → (−x, −y)` keep their ceiling to `1e−9` | **PASS** |
| **4 — numerical convergence** | nested subdivisions 1 / 2 / 4 move the winner's range dishing by **1.4e−5** relative; the dishing grid 41 / 81 / 161 by **exactly 0.0**; the ceiling bisection over `1e−6 … 1e−12` by `< 1e−6` nm absolute; the 30-path elastica arm over RK4 steps 200 / 400 / 800 by **exactly 0.0**; a placement's ceiling is deterministic on repeat | **PASS** |
| **5 — literature and upstream** | **13 reproductions.** `C-0063`'s 0.0706146 (`2.9e−10`); `C-0068`'s 0.0789 (`6.3e−4`, on its own rounded publication); `C-0072`'s 0.26028 (`5.4e−6`), 9.12 nm (`9.4e−11`), 1.3495 nm (`8.8e−6`) and 7.77049 nm arm (`2.5e−7`); `C-0069`'s 8.16439 nm arm (`8.0e−8`) and 8.19 nm ceiling (`2.2e−16`); `C-0055`'s 10.88 nm pitch and 34 roots (`0.0`); `C-0026`'s 4.90731 nm free stroke (`7.7e−10`); `C-0022`'s 0.3079 free tile (`7.7e−6`); `C-0063`'s **two** centro-symmetric phases (`0.0`) | **PASS** |

### Gate 3 — four things that are not restatements of the construction

1. **The closed form is checked against the object it bounds.** On a three-row toy lattice every two-per-row and one-per-row placement is enumerable, and the bisected capacity bound equals the exhaustive `min`-over-rows maximum to `1e−6` at both caps. The bound is a theorem, and the test is what makes it one here.
2. **Superposition is asserted, not assumed.** The sliced 30-station surrogate reproduces a fully assembled 855-degree-of-freedom `OrigamiGrillage` solve with the same springs, in the peak dishing *and* path by path — which is the only thing standing between 202 026 cheap evaluations and 202 026 wrong ones.
3. **The falsifier that would show the solver broken.** A uniform load on a uniform Winkler foundation must dish exactly zero; it does, below `1e−9`, on the phase-24 host this claim reads every number on.
4. **`CH-0086`'s central claim, measured.** With `C-0053`'s **converging** pose forbidden — two arms in a row pointing at each other — the maximum over every two-per-row placement is **9.1200 nm** at both symmetric phases, i.e. `C-0072`'s own number to `1e−9`. So the 30-root disagreement *is* the value of that pose, and it is a measurement in the suite rather than an argument in a challenge.

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **F1** | **no two-per-row placement clears 0.10 with equal springs** | **FIRED** | 0 of 32 phases, best 0.1667 exhaustive and 0.1670 by descent. **The branch cannot have `C-0063`'s equal springs and `C-0072`'s margin at once** |
| **F2** | the sliced surrogate disagreeing with an assembled solve | no | departure `1e−7` in the peak, `1e−6` per path |
| **F3** | a uniform load dishing anything but zero | no | `< 1e−9` |
| **F4** | the capacity bisection disagreeing with an exhaustive maximum | no | `1e−6` on the toy lattice, at both caps |
| **F5** | the descent beating the exhaustive family at a symmetric phase | no | 0.1816 against 0.166653 at phase 24, 0.1791 against 0.172575 at phase 8 — the symmetry restriction *helps* here, as it did for `C-0063` |

**A result that was not anticipated:** the *worse* equal-spring placement is the one worth building. At phase 8 the maximum-ceiling placement dishes 0.2424 with equal springs — 40 % worse than the equal-spring argmin — and 0.0682 under a distribution, 5 % *better*, while keeping 4.2× the plan margin. An argmin taken on the equal-spring objective is an argmin on a quantity the design does not use.

**A second one:** at 30 roots the range objective is void. Both ends of the device's traversed range return the same placement and the same number, where at 34 roots `C-0068` measured 0.0706 → 0.0789 between them. A sparser coupling's dishing is dominated by its own sag between stations, which is load-independent, rather than by the collar it corrects.

---

## What the branch should build

**30 upward roots at crossover phase 8**, two per duplex, at `x = ±10.88 nm` on rows 0, 2, 4, 6, 8, 10, 12, 14 and `x = ±16.32 nm` on rows 1, 3, 5, 7, 9, 11, 13 — centro-symmetric, carrying `C-0039`'s 23 bp (7.77049 nm) hinge-rooted arm, with a stiffness distribution of peak ratio 2.057 summing to `C-0017`'s 33.3333 pN/nm.

| | value | against |
|---|---|---|
| plan margin | **1.76451 nm** | **68.9×** `C-0069`'s 0.02561 nm, **5.19** base-pair rises, **1.31×** `C-0072`'s 1.3495 |
| dishing over the device's whole range | **0.06822** | `T-5b`'s 0.10 — **1.47×** clear |
| peak stiffness ratio | **2.057** | `C-0060` prices 4.7–5.1 as buildable |
| peak path force at 3 nm | **6.857 pN** | the 10 pN unzip allowable — **1.46×** clear |
| §3's own duty per path | 3.33333 pN | 10 pN — 3.0× |
| what it gives up | **equal springs** | `C-0063`'s 34 roots need none |

---

## Validity range

- **TRL 1–3. Nothing here is measured and the motif is not demonstrated.** `C-0055`'s literature finding — a free lever on one crossover is this programme's own construct — is upstream of every number.
- **`C-0072`'s tolerance argument is answered rather than inherited, and the answer is marginal on one channel.** Deliverable 4 re-evaluates all five floors at the 30-path arm and all five clear — but the **arm's own bending at a rigid root** clears by only **1.05×**, and it does so only because the arm shortens with the count. The plan model here is still `C-0041`'s hard-body one at **nominal** positions, so a *"places"* verdict remains the weak direction. **The margin becomes quotable at 30 roots; it does not become comfortable.**
- **A *"places"* verdict is the weak direction**, as `C-0069` and `C-0072` both record.
- **The margin permits a re-assignment of arm directions.** `rootedLengthCeiling` asks whether *some* direction assignment admits an element of that length; the emitted `towardPositiveX` is the greedy assignment at the 7.77 nm arm. Growing the arm toward 9.535 nm would flip some rows to the converging pose. This is `C-0053`'s convention exactly as `C-0069` used it, and it is restated here because the recommendation's whole value is the margin.
- **The load is `C-0022`'s solved collar at 2 mM and the two ends of one device's range.** `C-0068` establishes that a flatness verdict does not travel between devices; **nothing here is evaluated at the 5 nm layer**, whose two states are anti-parallel (`C-0064`), and the recommendation must be re-read there before it is carried to a 5 nm device.
- **The distribution is a descent** (`minimaxStiffnessDistribution`, 12 starts, log-sum-exp continuation) and reports what it found; `reachableDishingFloor` is quoted beside every row and runs 0.0048–0.0078, so there is **an order of magnitude of unexplored room** and the peak ratios quoted are upper bounds on what is needed.
- **The per-path ceiling binds two of the eight priced rows.** Where the peak ratio reads exactly 3.00 and the peak force exactly 10.000 pN, the minimax is *capped*, not converged, and the value quoted is the best admissible rather than the best reachable.
- **The 32-phase descent is a descent.** Existence of a flat *non-symmetric* placement at some phase is **not** excluded by it; what is excluded is that any of 202 026 evaluated ones is flat with equal springs, and the exhaustive symmetric families at the two phases the congruence admits *are* complete.
- **`C-0055`'s self-consistency is re-solved at 30 by `T-138`/`C-0075`**, not assumed here: the 30-path arm is 7.77049 nm, the phase-24 lattice carries **45** of them at three per row, and 30 therefore place with room to spare.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| the 34 stations, their phase and the 10.88 nm pitch | phase 24 | **`C-0063`/`C-0055`, CONSUMED AS DATA** from `gpd/results/T-125-*.json` |
| the solved edge collars at 10 nm and 7 nm | `C-0022` | **CONSUMED AS DATA** from `gpd/results/T-3b-*.json`, keyed on all three of concentration, gap and bias |
| `C-0017`'s mandate | 33.3333 pN/nm as a **sum** | **CITED** |
| duplex `EI`, crossover `k_θ`, `C-0034`'s `A2` | 230 pN·nm²; 13.5294; 78.2353 pN·nm/rad | **CITED, CanDo MODEL INPUT** (Kim et al. 2012) / **CITED, FITTED** (Chen et al. 2014, via `C-0009`) / **`C-0034`** |
| interhelical distance, rise, crossover spacing | 2.69 nm, 0.34 nm, 32 bp | **CITED, MEASURED** (SAXS, Fischer et al. 2016) / **CITED** (Rothemund 2006, Ke et al. 2009) |
| the per-path unzip allowable, §3's targets | 10 pN; 100 pN, 3 nm, 40 × 40 nm, 2 mM | **CITED** |

Everything else — the forced count vector, the capacity bound and every ceiling in Deliverable 1, all 69 984 enumerated placements, all 202 026 descent evaluations, the sixteen distribution rows and the recommendation — is **derived here in code**, with `C-0063`'s, `C-0064`'s, `C-0068`'s, `C-0069`'s and `C-0072`'s pipelines **re-run rather than tabulated**.

## Still open — named, not answered

1. **The recommendation is not evaluated at the 5 nm layer.** `C-0068` showed a 34-root placement flat at 10 nm dishes 1.2–1.7× worse than no coupling at all at 5 nm. Nothing here says where a 30-root one sits.
2. **A flat non-symmetric 30-root placement with equal springs is not excluded**, only unfound over 202 026 evaluations. The floor says a lot of room remains; whether any of it is reachable with equal springs is open.
3. **The distribution's own buildability at 30 paths.** `C-0060` prices a two-level ratio of 4.7–5.1 as buildable on a 45-path grid; a 2.06 ratio on 30 upward-rooted arms has not been quantised onto staple lengths, and `CLAUDE.md`'s *"projection onto a constrained family is not optimisation within it"* applies.
4. **`CH-0084`'s measured 43.6 % staple dropout** is not applied to any number here. At 30 paths a 16 % mean dropout is a 16 % mandate shortfall exactly as at 34.
5. **Whether the 30-root design's per-path force survives `C-0009`'s 2.3–7.6× concentration factor** at the crossover it enters. `C-0063` reported 1.25 pN in the worst crossover on 34; the equivalent is not computed here.
6. **The two phases are the centro-symmetric ones; the recommendation is at phase 8 and the sheet's own eight-column set is `C-0015`'s ten.** Whether a *non*-symmetric placement at one of the other eight eight-column phases carries both the margin and a better distribution is unenumerated.

## Challenges

**Raises [`CH-0086`](../challenges/CH-0086-a-plan-ceiling-is-a-property-of-a-placement-not-of-a-count.md)** against `C-0072`'s Deliverable 6: a plan ceiling is a property of a **placement**, not of a count, and `C-0072`'s table reads it on its own reduction rule — understating the escape it discovered by **1.31×** at 30 roots, **3.62×** at 22 and **1.78×** at 15.

**No number in `C-0072`, `C-0069`, `C-0068`, `C-0063`, `C-0055`, `C-0026` or `C-0022` fails to reproduce** — 13 reproductions, worst departure `6.3e−4` and that against a four-digit published figure.

**None stands against this claim.** The five ways it would fail:

1. **A distribution that a staple design cannot build at 30 paths.** The whole positive verdict rests on a 2.06 peak ratio; `C-0060` prices ratios, not this one.
2. **A footprint convention in which two arms may not converge.** The 9.535 nm ceiling is `(pitch + pitch − d)/2` on a converging pair; forbid that pose and the ceiling falls to `pitch − d = 8.19` and the margin to 0.4195 nm.
3. **A different flatness convention.** At 0.10 the recommendation clears by 1.47× and the equal-spring answer misses by 1.67×; at 0.15 the equal-spring answer would still miss and at 0.20 it would pass.
4. **A 5 nm device.** Nothing here is read there, and `C-0068` shows that is where a placement's verdict reverses.
5. **`C-0055`'s self-consistency re-solved at 30 with a different arm.** The arm is `C-0039`'s exact elastica at `C-0017`'s mandate; a different joint pair moves it and the margin with it.
