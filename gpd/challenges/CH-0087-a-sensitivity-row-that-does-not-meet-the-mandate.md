# CH-0087 — Three of the eight rows of `C-0069`'s Deliverable 5 present a coupling that does **not** deliver `C-0017`'s mandate, and the table carries no column that says so. The missing column is `placed/n`: the 15-path row presents **2.267×** the mandate (**75.556** against 33.333 pN/nm), the 45-path row **0.533×** — and the **2.73 nm** square-lattice row, reported only as a plan failure, delivers **0.529×** by exactly the same arithmetic. **No `C-0069` headline moves**, because every one of its deliverables is read at the self-consistent `n = 34`

| | |
|---|---|
| **Raised by** | [`C-0075`](../claims/C-0075-path-count-consistency.md) (`T-138`) |
| **Against** | [`C-0069`](../claims/C-0069-output-element-placement.md)'s Deliverable 5 — *"the sensitivities, and the one axis that closes the margin"* — and specifically its note on the 15-path row, *"fewer paths shorten the arm; **the placement is unchanged because the count is what sets the stations**"* |
| **Grounds** | **`C-0017`'s mandate is a stiffness on a SUM**, so a path count `n` sizes the element *and* counts the instances. Deliverable 5 changes the first and holds the second at 34, and it never states the total the resulting array delivers. Worse, the shortfall is not confined to the path-count rows: the delivered total is `N_placed · 33.3333/n`, so **any** row whose placed count differs from its path count misses the mandate by exactly that ratio — including the two rows the claim reports as plan failures |
| **Severity** | **a missing column in one deliverable. NO headline moves, and that is verified rather than asserted.** At `n = 34` the published and self-consistent readings return the same arm (8.16439018 nm), the same placed count (34) and the same ratio (1.000000000), so `C-0069`'s `Q5` recommendation, its `c ≤ 2.3416` window, its 22.41 nm two-support floor, its 1122 nm axial refusal and its whole eleven-row catalogue are untouched. `C-0072` recorded this as *"open item 8 … filed as `T-138` rather than challenged"*; it is filed as a challenge now because the defect is larger than the two rows that motivated it |

---

## What is claimed upstream

`C-0069` (`T-133`, iteration 13), Deliverable 5, in full:

| axis | reading | budget | arm | placed | verdict moves? |
|---|---|---|---|---|---|
| **reference** | 2.69 nm SAXS, `EI` 230, one crossover, 34 paths | 8.19 | **8.164** | **34** | — |
| **exclusion width** | **2.73 nm, the square-lattice SAXS value** | **8.15** | 8.164 | **18** | **YES** |
| exclusion width | 2.0 nm, the steric diameter | 8.88 | 8.164 | 34 | no |
| duplex `EI` | Fields et al.'s implied 172.906 | 8.19 | 7.883 | 34 | no |
| crossover `α` | 0.6 | 8.19 | 7.793 | 34 | no |
| **crossover `α`** | **1.2** | 8.19 | **8.332** | **30** | **YES** |
| **path count** | **45, `C-0015`'s own** | 8.19 | **9.131** | **24** | **YES** |
| path count | 15, `C-0041`'s buildable count | 8.19 | 5.963 | 34 | no |

with the notes *"more paths make each element LONGER (`C-0023`), so 34 is the count the plan budget prefers — and 45 stations do not exist on this lattice anyway"* and *"fewer paths shorten the arm; the placement is unchanged because the count is what sets the stations"*.

`C-0069`'s own **Conditions** row states the premise the table then departs from:

> *"`C-0017`'s **33.3333 pN/nm** as a **sum**, so a per-path secant of **0.980392 pN/nm** at §3's **acceptable 3 nm**"*

## The column that is missing

The load is carried by what is **built**, so the total an array delivers is

&nbsp;&nbsp;&nbsp;&nbsp;`k_delivered = N_placed · k_mandate / n`, &nbsp;&nbsp; hence &nbsp;&nbsp; `k_delivered / k_mandate = N_placed / n`.

`C-0017` is an **equality**, so it is met exactly when `N_placed = n` and missed in proportion otherwise. One division per row, no model, and it needs nothing the table does not already contain.

| axis | reading | `n` | placed | **`k_delivered` [pN/nm]** | **`k/33.3333`** | meets `C-0017`? |
|---|---|---|---|---|---|---|
| reference | 2.69 nm | 34 | 34 | 33.333 | **1.000** | **yes** |
| **exclusion width** | **2.73 nm** | 34 | **18** | **17.647** | **0.529** | **no** |
| exclusion width | 2.0 nm | 34 | 34 | 33.333 | 1.000 | yes |
| duplex `EI` | 172.906 | 34 | 34 | 33.333 | 1.000 | yes |
| crossover `α` | 0.6 | 34 | 34 | 33.333 | 1.000 | yes |
| **crossover `α`** | **1.2** | 34 | **30** | **29.412** | **0.882** | **no** |
| **path count** | **45** | 45 | **24** | **17.778** | **0.533** | **no** |
| **path count** | **15** | 15 | **34** | **75.556** | **2.267** | **no** |

**Four readings.**

1. **The 15-path row is the clearest case and it is a different design, not a sensitivity.** Thirty-four instances of a 15-path arm present **75.556 pN/nm** — 2.267× the mandate the claim's own Conditions row fixes. Nothing in the table says so.
2. **The 45-path row fails in both readings.** Held at 34 instances it places 24 and delivers 0.533×; tied to its own count it demands 45, which needs three roots in every row, and a 9.131 nm arm exceeds the 8.19 nm row-of-three ceiling, so the lattice carries 30 and it delivers **0.667×**. `C-0069`'s parenthesis *"45 stations do not exist on this lattice anyway"* is correct; the ratio is what turns it into a number.
3. **And the defect is not confined to the path-count rows.** The 2.73 nm row and the `α = 1.2` row are reported as **plan** failures — *"the verdict moves"* — and they are **stiffness** failures of exactly the same size, 0.529× and 0.882×. That is one arithmetic wearing two names, the same shape as `C-0072`'s discovery that `C-0069`'s and `C-0066`'s knife edges are one subtraction.
4. **Three of eight rows, not two.** Which is why this is a challenge against the *table* rather than a footnote against two of its rows.

## What the fixed array hid

Holding the instances at 34 forces a row of three at **every** count in the table, because `3a + 2(15 − a) = n` needs `a = n − 30`. So `C-0069`'s ceiling column is `pitch − d` = **8.19 nm** in every row — including the 15-path row, where one root per duplex is possible and the lattice affords **30.88 nm**.

Self-consistently the ceiling is a **step function of the count**, and the step is at exactly 31:

| `n` | 45 | 34 | 33 | 32 | 31 | **30** | 28 | 25 | **22** | 20 | **15** |
|---|---|---|---|---|---|---|---|---|---|---|---|
| **ceiling [nm]** | 8.19 | 8.19 | 8.19 | 8.19 | 8.19 | **9.535** | 9.535 | 9.535 | **14.975** | 14.975 | **30.88** |
| **margin [nm]** | −0.941 | 0.0256 | 0.122 | 0.219 | 0.319 | **1.765** | 1.972 | 2.299 | **8.088** | 8.334 | **24.917** |

**This is the escape `T-136` walks through, and it is invisible in `C-0069`'s table by construction.** `C-0072` found it by re-sizing both, and its own Deliverable 6 says so; what neither claim states is that the invisibility is caused by the fixed instance count and not by the choice of counts sampled.

## What does not move, and it is verified rather than asserted

At `n = 34` the two readings coincide **identically** — this was `T-138`'s declared falsifier and it did not fire:

| quantity | AS PUBLISHED | SELF-CONSISTENT |
|---|---|---|
| arm | 8.16439018 nm | 8.16439018 nm |
| instances demanded | 34 | 34 |
| placed | 34 | 34 |
| length ceiling | 8.1900 nm | 8.1900 nm |
| `k_delivered/33.3333` | 1.000000000 | 1.000000000 |

So **every** `C-0069` deliverable is read at the self-consistent count: the six cheap bounds, the eleven-row catalogue and its clause funnel, the `c ≤ 2.3416` two-restraint window and its 1.8 % / 2.9 % margins, the three escapes out of the plan, and the `Q5` recommendation. All eight of its Deliverable-5 rows reproduce here — arms to `≤ 5.9e−5` and **every placed count exactly** (34, 18, 34, 34, 34, 30, 24, 34).

## The general form

**A sensitivity on a variable that appears twice in a design must move it in both places, and a table that moves it in one is not a sensitivity table.** The path count sizes the element through `k_mandate/n` and counts the instances through the array; changing only the first prices a design nobody proposed.

The general instrument is cheaper than the general lesson: **when a mandate is written on a sum, put the delivered sum in the table.** It costs one division, it is dimensionally trivial, and here it catches three rows of eight — two of which are not about the path count at all.

## What would settle it in `C-0069`'s favour

1. **A reading of `C-0017` as a per-path requirement.** Then each row's per-path secant is the design variable and 34 instances of a 15-path arm is a coherent design. `C-0060`, `C-0069` itself and `C-0072` all establish it is a **sum**.
2. **A statement in the table that the path-count rows hold the array fixed on purpose** — i.e. that they price *"what if the element were sized wrong"* rather than *"what if the count were different"*. That is a defensible question and it is not the one the axis name asks.
3. **A `placed` convention that counts what is demanded rather than what is realised.** Then the 2.73 nm and `α = 1.2` rows deliver the mandate — and so would any design the plan model refuses.
