# CH-0084 — A path count re-sizes the **array** as well as the element, and `C-0069`'s path-count sensitivity re-sizes only the element: its 15-path row places **34** instances of a 15-path arm, which delivers **2.27×** `C-0017`'s mandate, and its 45-path row places 34 of a 45-path arm, which delivers **0.76×** — so neither off-reference row describes a coupling that meets §3, and the escape that was hiding behind the error is that **fewer paths dissolve the rows of three and buy 53× the plan margin**

| | |
|---|---|
| **Raised by** | [`C-0072`](../claims/C-0072-plan-tolerance-model.md) (`T-134`) |
| **Against** | [`C-0069`](../claims/C-0069-output-element-placement.md)'s **Deliverable 5**, the sensitivity table — specifically its two *path count* rows and the note attached to the second, *"fewer paths shorten the arm; the placement is unchanged because the count is what sets the stations"* |
| **Grounds** | **`C-0017`'s mandate is a stiffness on a SUM, so the path count appears twice**: once in the per-path secant `k₁ = 33.3333/n` that sizes the element, and once as the number of instances that are actually built. `C-0069`'s sweep changes the first and holds the second at 34. The composed array therefore presents `34 k₁(n) = 33.3333 × 34/n` pN/nm, which is the mandate only at `n = 34` |
| **Severity** | **the two off-reference rows of one sensitivity table, and none of `C-0069`'s headline numbers.** The reference row (`n = 34`) is exactly self-consistent and every deliverable in `C-0069` is read on it. What falls is the *reading* of the 15-path row as *"the placement is unchanged"* — and what the corrected reading uncovers is a design escape `C-0069` could not see, which is why this is filed as a challenge rather than a footnote |

---

## What is claimed upstream

`C-0069`'s Deliverable 5 sweeps eight axes. Two of them are the path count, and the result file records them verbatim:

| axis | reading | ceiling | arm | placed | moves? | `C-0069`'s note |
|---|---|---|---|---|---|---|
| path count | 45 paths, `C-0015`'s own | 8.19 | **9.1311565** | **24** | **yes** | *"more paths make each element LONGER (`C-0023`), so 34 is the count the plan budget prefers — and 45 stations do not exist on this lattice anyway"* |
| path count | 15 paths, `C-0041`'s buildable count | 8.19 | **5.96297903** | **34** | no | *"fewer paths shorten the arm; the placement is unchanged because the count is what sets the stations"* |

**Both arm lengths reproduce exactly here** — 9.1311565 and 5.96297903 nm through `C-0039`'s own `elasticaArmForStiffness`, to the last digit `C-0069` prints.

## What is wrong

`C-0069`'s own convention section says it plainly: *"**One load path is one element**, and `C-0017`'s mandate is a **sum**, so the per-path secant is `33.3333/34 = 0.980392 pN/nm`."* Under that convention `n` load paths are `n` elements. The sensitivity then has to move both:

&nbsp;&nbsp;&nbsp;&nbsp;`k_total(n, instances) = instances × 33.3333/n`

| row | `n` | instances placed | `k_total` [pN/nm] | against the 33.3333 mandate |
|---|---|---|---|---|
| reference | 34 | 34 | **33.333** | **1.000** — exact |
| `C-0069`'s 45-path row | 45 | **24** (its own reported count) | 17.78 | **0.533** |
| the same, had all 34 placed | 45 | 34 | 25.19 | 0.756 |
| **`C-0069`'s 15-path row** | **15** | **34** | **75.56** | **2.267** |

The 15-path row therefore describes an array that over-delivers `C-0017`'s mandate by **2.27×**. That is a **placement** error in exactly the sense `C-0060` established — *"rounding the two levels independently misses `C-0017`'s 33.3333 pN/nm by 0.40–5.44 %, which is a placement error and not a rounding nuisance"* — and it is 227 %, not 5 %.

`C-0069` half-sees the same problem on the other row and says so — *"45 stations do not exist on this lattice anyway"* — but records the row's *"placed 24"* as a verdict move all the same. Both rows are the same mistake with opposite sign.

## What the corrected sweep finds, and why it matters

Re-sizing the **array** as well as the element turns the path count from a sensitivity into a **design variable**, and it is the only one on this lattice that recovers the knife edge:

| paths | instances | arm [nm] | length ceiling [nm] | plan margin [nm] | margin / rise |
|---|---|---|---|---|---|
| **34** | **34** | **8.16439** | **8.19** | **0.0256** | **0.075** |
| 33 | 33 | 8.06840 | 8.19 | 0.1216 | 0.358 |
| 32 | 32 | 7.97080 | 8.19 | 0.2192 | 0.645 |
| 31 | 31 | 7.87152 | 8.19 | 0.3185 | 0.937 |
| **30** | **30** | **7.77049** | **9.12** | **1.3495** | **3.97** |
| 25 | 25 | 7.23574 | 9.12 | 1.8843 | 5.54 |
| 22 | 22 | 6.88711 | 9.12 | 2.2329 | 6.57 |
| 15 | 15 | 5.96298 | **20.00** | **14.037** | 41.3 |

**The ceiling itself moves, and that is the part `C-0069`'s sweep structurally could not show.** Holding the array at 34 pins the length ceiling at `pitch − d = 8.19 nm` in every row of the table, because `C-0063`'s bound 1 — `3a + 2(15 − a) = 34` — forces four rows of three and a row of three is the only configuration in which two same-sense arms sit at the bare root pitch. Drop four arms and every row carries at most two, the rows of three are gone, and the ceiling is set by the tile edge instead: **9.12 nm**. The margin goes from **0.0256 to 1.3495 nm — 53× — for 12 % of the path count.**

## What this does NOT touch

- **Every headline number in `C-0069` stands.** All of them are read at `n = 34`, where the sweep is self-consistent: the 8.19 nm budget, the 8.16439 nm arm, the 34-of-34 placement, the `c ≤ 2.3416` inequality, the 79.678/13.930 restraint ceilings, the six-mechanism census and the 22.414 nm two-support floor.
- **The direction of the 45-path row is right** and its conclusion is unaffected: more paths make each element longer, 45 stations do not exist on this lattice, and 34 is not a rounding of 45.
- **The 15-path row's *verdict* does not move either** — a 15-path array still places. What moves is the *reason*, and with it the visibility of the escape.

## What would settle it

Re-run `C-0069`'s Deliverable 5 with the instance count tied to the path count, and report `k_total/33.3333` beside every row. `C-0072` does this for the path-count axis only; the other six axes of that table do not move the count and are unaffected.

## The cost of the escape, named here because it is not free

The reduced array is **not flat**. `C-0063`'s 34 roots dish 0.0706 of the free stroke; the 30 roots left by dissolving the rows of three dish **0.2603**, against `T-5b`'s 0.10. The reduction used is a *plan* rule and not a flatness optimisation, so that number is an **upper** bound on what a re-optimised 30-root placement would give — and re-optimising is `C-0063`'s own search under a new constraint, which `C-0072` names as its follow-on rather than running.
