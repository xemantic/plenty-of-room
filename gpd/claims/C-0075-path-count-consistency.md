# C-0075 — **`C-0069`'s path-count sensitivity re-sizes the element and holds the array at 34, and the column that exposes it is `placed/n`.** Its 15-path row presents **2.267×** `C-0017`'s mandate and its 45-path row **0.533×** — but the same ratio also condemns **three rows the claim reports only as plan failures**, because a row that places 18 of 34 delivers **0.529×** the mandate as well. **No `C-0069` headline moves** and that is verified, not assumed: at `n = 34` the two readings are the same row to the last digit. **What the fixed array hid is the CEILING** — held at 34 instances a row of three is forced and the ceiling is `pitch − d` = 8.19 nm at every count in the table, where self-consistently it is a **step function**: 8.19 above 30, **9.535** from 30 to 24, **14.975** at 22 and **30.88** at 15

| | |
|---|---|
| **Task** | [`T-138`](../tasks/T-138.md), raised by [`C-0072`](C-0072-plan-tolerance-model.md)'s open item 8 — *"`C-0069`'s own path-count sensitivity could not have found this, because it re-sizes the element while holding the array at 34"* — and answered together with [`T-136`](../tasks/T-136.md)/[`C-0074`](C-0074-two-per-row-placement.md), because dropping to 30 roots drops to 30 paths and the two moves are one |
| **Leaf** | **`A8.2`**, with **`A1.2`** |
| **Verification type** | **logical** (one division per row, which needs no model at all) **+ in-silico** (`C-0069`'s own pipeline — `elasticaArmForStiffness`, `placeRootedOutputElement`, `rootedLengthCeiling`, `rowOfThreeLengthCeiling` — re-run rather than retyped, and `C-0074`'s closed-form ceiling per count) |
| **Verdict** | **PASS. The defect is real, it is a presentation defect, and it is worth more as a missing COLUMN than as a corrected row.** `C-0017`'s mandate is a stiffness on a **sum**, so a path count `n` sizes the element *and* counts the instances; `C-0069`'s Deliverable 5 changes the first and holds the second at **34**. Read as a delivered total, its eight rows present **1.000, 0.529, 1.000, 1.000, 1.000, 0.882, 0.533, 2.267** × the mandate — the 15-path row **75.556 pN/nm** against 33.333, and the 45-path row **17.778**. **But the ratio is `placed/n`, not `34/n`**, and that is the part neither claim saw: **three rows that `C-0069` reports only as plan failures are stiffness failures by the same number** — the 2.73 nm square-lattice row places 18 of 34 and therefore delivers **0.529×**, and the `α = 1.2` row places 30 and delivers **0.882×**. **No `C-0069` headline moves, and it is verified**: at `n = 34` the two readings return the same arm (8.16439018 nm), the same placed count (34) and the same ratio (1.000000000), and every deliverable of that claim — the six cheap bounds, the eleven-row catalogue, the two-restraint window `c ≤ 2.3416` — is read there. **What the fixed 34-instance array DID hide is the length ceiling.** Holding the instances at 34 forces a row of three (`3a + 2(15 − a) = n` needs `a = n − 30`), and a row of three caps a rooted element at `pitch − d` = **8.19 nm** — so `C-0069`'s ceiling column is **8.19 in every row**, including the 15-path one where the lattice affords **30.88**. Self-consistently the ceiling is a **step function of the count with its step at exactly 31**, and the step is what `T-136` walks through. **The coupled answer**: the 30-path arm is **7.77049 nm = 23 bp**, the phase-24 lattice carries **45** of them at three per row, so 30 place self-consistently and `C-0017` is met **exactly** — with a margin of **1.76451 nm** on the best 30-root placement, **1.31×** `C-0072`'s 1.3495 nm and **68.9×** `C-0069`'s 0.02561 nm. Raises [`CH-0087`](../challenges/CH-0087-a-sensitivity-row-that-does-not-meet-the-mandate.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. Nothing here is measured.** Everything upstream of `C-0069` — the undemonstrated one-crossover lever motif, the hard-body plan model at nominal positions — is unchanged and carried. |
| **Provenance** | `gpd/results/T-138-path-count-consistency.json`, produced by `anchoring.PathCountConsistencyStudyKt`; model in `src/main/kotlin/anchoring/PathCountConsistency.kt` and `src/main/kotlin/anchoring/TwoPerRowPlacement.kt`; **3 cheap bounds, 16 axis rows (8 axes × 2 readings), 11 self-consistent count rows, 2 convergence records, 20 upstream reproductions, 3 predicates**; **9 gate-named tests in `src/test/kotlin/anchoring/PathCountConsistencyTest.kt`**; `tools/verify.sh` **BUILD SUCCESSFUL in 14 m 8 s** with two sibling files mid-TDD dropped; the result file re-run through `tools/study.sh` and diffed **byte-for-byte identical** |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40.0 × 40.35 nm single-layer **square-lattice** Rothemund sheet, 15 duplexes at the SAXS **2.69 nm**, 0.34 nm rise, crossover phase **24**; `C-0063`'s **34** upward roots read from `gpd/results/T-125-*.json`; `C-0017`'s **33.3333 pN/nm** as a **sum** at §3's **acceptable 3 nm**; `C-0039`'s exact elastica on a one-crossover root (`k_θ` = 13.5294 pN·nm/rad, `α ∈ [0.6, 1.2]` swept) and `C-0034`'s `A2` tip (78.2353); `EI` = 230 pN·nm² (Fields et al.'s implied 172.906 swept); `C-0053`'s footprint convention at `d` = 2.69 nm (2.73 and 2.0 swept) |
| **Consumes** | [`C-0069`](C-0069-output-element-placement.md) (**every cell of its Deliverable 5 re-derived**, through its own `sensitivity` construction; `placeRootedOutputElement`, `rowOfThreeLengthCeiling`, `rootedLengthCeiling` re-run as libraries), [`C-0074`](C-0074-two-per-row-placement.md) (`maximumPlanCeilingForCount`, `latticeRootCapacity`, `balancedRowCounts`; the recommended placement **read from its result file**), [`C-0072`](C-0072-plan-tolerance-model.md) (its nine count rows and `rowsWithoutInteriorRoots` re-run; 9.12, 20.00, 1.3495 and 7.77049 reproduced), [`C-0063`](C-0063-upward-root-placement.md) (the 34 stations, **read from its result file**; bound 1 re-derived), [`C-0039`](C-0039-two-spring-elastica.md) (`elasticaArmForStiffness`), [`C-0055`](C-0055-unused-junction-site.md) (the 10.88 nm pitch), [`C-0017`](C-0017-output-coupling-stiffness.md) (the mandate as a **sum**, **CITED**), [`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile` |
| **Raises** | [`CH-0087`](../challenges/CH-0087-a-sensitivity-row-that-does-not-meet-the-mandate.md) against `C-0069`'s Deliverable 5 |

---

## The claim, in one line

**A sensitivity table on a path count has three counts in it and `C-0069`'s has two: the count the element is SIZED at, the count the array DEMANDS, and the count the plan model PLACES — and because `C-0017` is an equality on a sum, only `placed = n` meets it, so `placed/n` is a column of the table and not a footnote; adding it costs one division, moves no headline, and reveals that the fixed 34-instance array had pinned the plan ceiling at 8.19 nm in every row of a table whose whole subject was how the design changes with the count.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**; `k_BT = 4.141947 pN·nm` at **300 K** in aqueous **2 mM MgCl₂**.
- **`C-0017`'s mandate is 33.3333 pN/nm as a SUM** over the coupling, so the per-path secant at a path count `n` is `33.3333/n` at §3's **acceptable 3 nm** stroke.
- **Three counts, kept apart throughout.** `n` sizes the element; `N_demanded` is what the array asks the lattice for; `N_placed` is what `C-0041`'s hard-body plan model admits. The delivered total is `N_placed · 33.3333/n`.
- **A rooted element occupies `[root, root ± L]` and the next along the same row may start at `high + d`** — `C-0053`'s footprint convention, unchanged.
- **A built length is a whole number of base pairs and a solved length is a real number of nm**, carried separately as `C-0072` carries them.

---

## The cheap bound, which is the whole of Deliverable 1

`k_delivered/33.3333 = N_placed/n` — one division per row, no model, run before anything else.

| bound | value | fired? |
|---|---|---|
| `C-0069`'s **15-path** row read as the total it presents | **75.556 pN/nm** = **2.267×** the mandate | **YES** |
| `C-0069`'s **45-path** row read on its own reported placed count | **17.778 pN/nm** = **0.533×** | **YES** |
| the reference row, where the two readings coincide | **1.000** exactly | no |

---

## Deliverable 1 — `C-0069`'s Deliverable 5 in both readings

`AS PUBLISHED` holds the array at 34 instances and takes `placed` from `placeRootedOutputElement` on `C-0063`'s own rows. `SELF-CONSISTENT` ties the demand to `n`, takes the ceiling from `C-0074`'s closed form at the balanced count vector, and takes `placed` from the lattice's exact capacity for that arm.

| axis | reading | `n` | arm [nm] | **AS PUBLISHED** ceiling / placed / `k/33.33` | **SELF-CONSISTENT** demand / ceiling / placed / `k/33.33` |
|---|---|---|---|---|---|
| reference | 2.69 nm, `EI` 230, one crossover | 34 | 8.16439 | 8.19 / **34** / **1.000** | 34 / 8.19 / **34** / **1.000** |
| exclusion width | **2.73 nm**, square lattice | 34 | 8.16439 | 8.15 / **18** / **0.529** | 34 / 8.15 / **30** / **0.882** |
| exclusion width | 2.0 nm, steric | 34 | 8.16439 | 8.88 / 34 / 1.000 | 34 / 8.88 / 34 / 1.000 |
| duplex `EI` | 172.906 (Fields et al.) | 34 | 7.88253 | 8.19 / 34 / 1.000 | 34 / 8.19 / 34 / 1.000 |
| crossover `α` | 0.6 | 34 | 7.79311 | 8.19 / 34 / 1.000 | 34 / 8.19 / 34 / 1.000 |
| crossover `α` | **1.2** | 34 | 8.33244 | 8.19 / **30** / **0.882** | 34 / 8.19 / **30** / **0.882** |
| **path count** | **45**, `C-0015`'s own | **45** | 9.13116 | 8.19 / **24** / **0.533** | **45** / 8.19 / **30** / **0.667** |
| **path count** | **15**, `C-0041`'s buildable | **15** | 5.96298 | 8.19 / **34** / **2.267** | **15** / **30.88** / **15** / **1.000** |

**Four readings.**

1. **The two path-count rows are the extremes, and they are extreme in opposite directions.** 2.267× and 0.533×; neither is `C-0017`'s mandate and neither row is a design.
2. **But the ratio condemns three rows, not two.** A row that places fewer than it demands fails the mandate by exactly the shortfall, whichever reading is taken: **0.529×** at 2.73 nm, **0.882×** at `α = 1.2`. `C-0069` reports these as plan failures — *"the verdict moves"* — and they are stiffness failures by the same number. **The two are one arithmetic**, which is the same shape as `C-0072`'s finding that `C-0069`'s and `C-0066`'s knife edges are one subtraction.
3. **The self-consistent 15-path row is a legitimate design and the published one is not.** At 15 demanded the array is one root per duplex, the ceiling is the tile edge at **30.88 nm**, the 5.96 nm arm places all fifteen and the mandate is met exactly. Held at 34 instances the same row presents 2.267× the mandate on stations sized for a different element.
4. **The self-consistent 45-path row is still not a design.** At 45 demanded every row must carry three, the ceiling collapses to 8.19 nm, and a 9.131 nm arm places nowhere in a row of three — the lattice carries **30**, so the delivered total is 0.667×. That is `C-0055`'s self-consistency argument reproduced from the other side, and it is why the count is 34 and not 45.

---

## Deliverable 2 — the ceiling is a step function of the count, and the step is at 31

`maximumPlanCeilingForCount` (`C-0074`) at the balanced count vector, against the ceiling `C-0072`'s plan-rule reduction reaches and against `C-0069`'s fixed 8.19.

| `n` | count vector | arm [nm] | bp | **the lattice's ceiling** | `C-0072`'s | **margin** | rises | lattice capacity for this arm | placed | `k/33.33` |
|---|---|---|---|---|---|---|---|---|---|---|
| 45 | 15 × 3 | 9.13116 | 27 | 8.1900 | — | **−0.9412** | −2.77 | 30 | **30** | **0.667** |
| **34** | 4 × 3 + 11 × 2 | 8.16439 | 24 | **8.1900** | 8.1900 | **0.02561** | 0.08 | 45 | 34 | 1.000 |
| 33 | 3 × 3 + 12 × 2 | 8.06840 | 24 | 8.1900 | 8.1900 | 0.1216 | 0.36 | 45 | 33 | 1.000 |
| 32 | 2 × 3 + 13 × 2 | 7.97080 | 23 | 8.1900 | 8.1900 | 0.2192 | 0.64 | 45 | 32 | 1.000 |
| 31 | 1 × 3 + 14 × 2 | 7.87152 | 23 | 8.1900 | 8.1900 | 0.3185 | 0.94 | 45 | 31 | 1.000 |
| **30** | **15 × 2** | **7.77049** | **23** | **9.5350** | 9.1200 | **1.76451** | **5.19** | 45 | **30** | **1.000** |
| 28 | 13 × 2 + 2 × 1 | 7.56281 | 22 | 9.5350 | 9.1200 | 1.9722 | 5.80 | 45 | 28 | 1.000 |
| 25 | 10 × 2 + 5 × 1 | 7.23574 | 21 | 9.5350 | 9.1200 | 2.2993 | 6.76 | 45 | 25 | 1.000 |
| **22** | 7 × 2 + 8 × 1 | 6.88711 | 20 | **14.9750** | 9.1200 | **8.0879** | 23.79 | 45 | 22 | 1.000 |
| 20 | 5 × 2 + 10 × 1 | 6.64066 | 20 | 14.9750 | 9.1200 | 8.3343 | 24.51 | 45 | 20 | 1.000 |
| **15** | 15 × 1 | 5.96298 | 18 | **30.8800** | 20.0000 | **24.917** | 73.29 | 45 | 15 | 1.000 |

**Three readings.**

1. **The step is at 31 and it is arithmetic, not geometry.** `3a + 2(15 − a) = n` needs `a = n − 30`, so a row of three is unavoidable at `n ≥ 31` and avoidable at `n ≤ 30`. `C-0069` never sees the step because its array is fixed at 34, which is above it.
2. **Every count from 34 down to 15 is self-consistent except 45.** The lattice's capacity for the arm each count demands is 45 at every one of them, so `placed = n` and `C-0017` is met exactly. The `placed/n` column would have been all ones — which is precisely why its absence in `C-0069` looks harmless and is not.
3. **The 45-path row is refused for the reason `C-0055` gives.** Its arm is longer than the row-of-three ceiling, so no row can carry three, so 45 instances cannot exist. `C-0069`'s parenthesis — *"45 stations do not exist on this lattice anyway"* — is right, and the self-consistent reading is what makes it a number (0.667×) rather than an aside.

---

## Deliverable 3 — the coupled reading with `T-136`

At 30 roots the self-consistent per-path secant is `33.3333/30` = **1.11111 pN/nm**, which re-sizes the arm to **7.77049 nm = 23 bp**.

| question | answer |
|---|---|
| does the re-sized arm still place 30 times? | **yes, with room** — the phase-24 lattice carries **45** elements of that length at three per row, so 30 at two per row is not tight |
| is the mandate met? | **exactly** — `placed = n = 30`, ratio 1.000 |
| what is the plan margin? | **1.76451 nm** on the best 30-root placement, against **1.3495 nm** on `C-0072`'s own plan-rule reduction — the ceiling is a property of the **placement**, which is [`CH-0086`](../challenges/CH-0086-a-plan-ceiling-is-a-property-of-a-placement-not-of-a-count.md) |
| does `C-0072`'s 1.3495 survive the re-sizing? | **yes, and it was already computed at the re-sized arm** — `C-0072`'s Deliverable 6 does re-size the element with the count; what it does **not** do is optimise the placement, and that is the 1.31× |
| where does `T-136` put the design? | **phase 8**, keeping the full 9.5350 nm ceiling and dishing **0.0682** under a distribution at a peak ratio of 2.06 — while `T-136`'s **equal-spring** argmin sits at phase 24 with a margin of only 0.4195 nm and still misses `T-5b` at 0.1667 |

---

## The five verification gates

Executed as **9 gate-named tests** in `src/test/kotlin/anchoring/PathCountConsistencyTest.kt`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | the delivered total is a stiffness, exactly **linear** in the mandate, and the ratio is independent of it; the arm is a **cube-ish** root of the per-path stiffness — the realised exponent over 15 → 45 paths is inside `1/3 ± 25 %` and **above** a third, because `ρ = k_θL/EI` carries the span so the length is a **fixed point** and a longer arm buys its own end restraint; unphysical counts throw at four entry points | **PASS** |
| **2 — limiting cases** | `placed = n` gives the mandate **exactly** at 15, 22, 30, 34 and 45; the balanced count vector reproduces `C-0063`'s bound 1 (**four** rows of three at 34, eleven of two) and saturates at the cap at 30, 45 and 15; an over-full count throws | **PASS** |
| **3 — symmetry and conservation** | **the two readings coincide identically at `n = 34`** — the declared falsifier, checked as an equality on the arm, the placed count and the ratio; the closed-form ceiling per count is **never below** a concrete placement's at the same count, at one, two and three roots per row | **PASS** |
| **4 — numerical convergence** | the 30-path elastica arm over RK4 steps 200 / 400 / 800 moves by **exactly 0.0**; the ceiling bisection over resolutions `1e−6 … 1e−12` by **exactly 0.0** | **PASS** |
| **5 — literature and upstream** | **20 reproductions, worst departure `5.9e−5`.** Every one of `C-0069`'s eight Deliverable-5 rows — arms 8.16439 (`8.0e−8`), 7.88253 (`5.9e−5`), 7.79311 (`1.5e−5`), 8.33244 (`5.3e−5`), 9.13116 (`1.7e−5`), 5.96298 (`3.6e−6`); ceilings 8.19, 8.15, 8.88 (`≤ 2.2e−16`); **placed counts 34, 18, 30, 24, 34 all exactly** — plus `C-0072`'s 7.77049, 9.12, 20.00 and 1.3495, `C-0063`'s four rows of three and `C-0055`'s 10.88 nm pitch | **PASS** |

### The declared falsifier, and what happened

| falsifier | fired? | outcome |
|---|---|---|
| **a `C-0069` headline number moving under the self-consistent reading** | **NO** | at `n = 34` the two readings are the same row: arm 8.16439018 nm in both, placed 34 in both, ceiling 8.1900 in both, ratio 1.000000000 in both. **This is a correction to a presentation, not to a claim** — which is why it is filed as a challenge against Deliverable 5 and not against `C-0069`'s verdict |

**A result that was not anticipated:** the missing column is not a path-count column. `placed/n` fails on **three** rows of `C-0069`'s eight, and only one of them is a path-count row — the 2.73 nm square-lattice row and the `α = 1.2` row are reported as *plan* failures and are stiffness failures of exactly the same size. That makes `CH-0087` a statement about **every** sensitivity in the table rather than about two of them.

**A second one:** the self-consistent reading makes the 15-path row *better*, not worse. Held at 34 instances it is a 2.27× overshoot; tied to its own count it is a clean design with a **30.88 nm** ceiling and 73 base-pair rises of margin. The reading `C-0069` published is the pessimistic one for that row and the optimistic one for the ceiling.

---

## Validity range

- **TRL 1–3. Nothing here is measured.** Everything upstream of `C-0069` is carried unchanged, including the undemonstrated one-crossover lever motif and the hard-body plan model at nominal positions.
- **`placed` is computed two ways and they are not the same object.** In the published reading it is `placeRootedOutputElement` on `C-0063`'s **fixed** 34 stations; in the self-consistent reading it is the lattice's **capacity** `Σ_r min(3, maximumRootedElementsInRow(r, L))`, i.e. the best any placement of that arm could do. The second is a **ceiling** on the first, which is why the 2.73 nm row reads 18 published and 30 self-consistently. Both are exact within their own question.
- **The self-consistent ceiling assumes the balanced count vector.** `balancedRowCounts` spreads the count as evenly as the cap allows; a design free to leave rows empty could do better at counts below 30, and `C-0074`'s bound is taken at the balanced cap rather than over all vectors.
- **No flatness is evaluated here.** A count is a stiffness statement and a plan statement in this claim, and a flatness statement only in `C-0074`. The 15-path and 22-path rows have enormous plan margins and are **not** shown to be flat — `C-0072` measures 15 roots at 0.3118, worse than no coupling at all.
- **`C-0069`'s verdicts stand.** Its `Q5` recommendation, its `c ≤ 2.3416` window, its 22.41 nm two-support floor and its 1122 nm axial refusal are all read at `n = 34` and are untouched.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| `C-0017`'s mandate | 33.3333 pN/nm as a **sum** | **CITED** |
| the 34 stations and their phase | phase 24 | **`C-0063`, CONSUMED AS DATA** from `gpd/results/T-125-*.json` |
| `T-136`'s recommended placement, ceiling and margin | phase 8, 9.5350 nm, 1.76451 nm | **`C-0074`, CONSUMED AS DATA** from `gpd/results/T-136-*.json` |
| duplex `EI`, crossover `k_θ` and its `α` bracket, `C-0034`'s `A2` | 230 pN·nm²; 13.5294; 0.6–1.2; 78.2353 | **CITED, CanDo MODEL INPUT** (Kim et al. 2012) / **CITED, FITTED** (Chen et al. 2014) / **`C-0034`** |
| Fields et al.'s implied `EI` | 172.906 pN·nm² | **CITED, MEASURED** (Fields et al., *NAR* **41**:9881) |
| interhelical distance, rise, crossover spacing | 2.69 nm (2.73 square, 2.0 steric), 0.34 nm, 32 bp | **CITED, MEASURED** (SAXS, Fischer et al. 2016) / **CITED** (Rothemund 2006, Ke et al. 2009) |
| the per-path unzip allowable, §3's targets | 10 pN; 100 pN, 3 nm | **CITED** |

Everything else — both readings of all eight axes, the eleven self-consistent count rows, the ceiling step function and the coupled 30-path answer — is **derived here in code**, with `C-0069`'s and `C-0072`'s pipelines **re-run rather than tabulated**.

## Still open — named, not answered

1. **Whether a count below 30 is buildable at all.** 22 and 15 carry huge plan margins and `C-0072` measures 15 roots at 0.3118 of the stroke — worse than no coupling. Only 30 is evaluated for flatness (`C-0074`).
2. **The published `placed` column at counts other than 34 on a re-optimised placement.** The self-consistent capacity is a ceiling; no placement search is run here except `C-0074`'s at 30.
3. **`CH-0084`'s 43.6 % staple dropout is a `placed/n` shortfall of the same kind** and is not applied to any row here. A 16 % mean dropout is a 16 % mandate miss at every count.
4. **The balanced count vector is one choice.** Whether an unbalanced vector at 22 or 25 buys more ceiling is unenumerated.

## Challenges

**Raises [`CH-0087`](../challenges/CH-0087-a-sensitivity-row-that-does-not-meet-the-mandate.md)** against `C-0069`'s Deliverable 5: three of its eight rows present a coupling that does not deliver `C-0017`'s mandate, and the table carries no column that says so.

**No number in `C-0069`, `C-0072`, `C-0063` or `C-0055` fails to reproduce** — 20 reproductions, worst departure `5.9e−5` and that against a four-digit published figure; every integer placed count reproduces **exactly**.

**None stands against this claim.** The three ways it would fail:

1. **A reading of `C-0017` as a per-path mandate rather than a sum.** Then `k_delivered` is not a meaningful column. `C-0060` and `C-0069` both establish it is a sum.
2. **A `placed` convention that counts demanded rather than realised elements.** Then the ratio is `N_demanded/n` and the three plan-failure rows are exonerated — but so is any design that cannot be built.
3. **A different balanced vector or a placement search at counts other than 30.** The ceiling column would move; the `placed/n` column would not.
