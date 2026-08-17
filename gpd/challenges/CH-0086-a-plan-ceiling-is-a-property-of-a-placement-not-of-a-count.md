# CH-0086 — A plan ceiling is a property of a **placement**, not of a count: `C-0072`'s Deliverable 6 reads its ceiling column on its own **plan-rule reduction** and calls it *"the ceiling"*, understating the escape it discovered by **1.31×** at 30 roots (9.5350 nm against 9.12), **3.62×** at 22 (14.975 against 9.12) and **1.78×** at 15 (30.88 against 20.00). The correction runs the **favourable** way — the margin at 30 roots is **1.76451 nm, 68.9×** `C-0069`'s knife edge rather than 53× — and it costs a bisection on a monotone capacity, with no solve at all

| | |
|---|---|
| **Raised by** | [`C-0074`](../claims/C-0074-two-per-row-placement.md) (`T-136`) |
| **Against** | [`C-0072`](../claims/C-0072-plan-tolerance-model.md)'s Deliverable 6 — its `length ceiling` column and the sentence *"Dissolve them and every row carries at most two; **the ceiling is then set by the tile edge, at 9.12 nm**"* — and, through it, [`C-0069`](../claims/C-0069-output-element-placement.md)'s own ceiling column, which is 8.19 nm in every row of a table whose subject is how the design changes with the count |
| **Grounds** | **the quantity is a maximum over placements and both claims evaluate it at one placement.** A placement's plan ceiling is `min` over its rows of that row's own ceiling, and the rows are **independent**, so `max over placements of min over rows` is exactly `sup{L : Σ_r min(m, maximumRootedElementsInRow(r, L)) ≥ n}` — a bisection on a monotone capacity, using `C-0069`'s own library, exact and free of any solve. `C-0072`'s ceiling column is that quantity evaluated at the output of `rowsWithoutInteriorRoots`, a deterministic reduction rule chosen to dissolve rows of three and not to maximise a ceiling |
| **Severity** | **a column of one deliverable, and the correction is FAVOURABLE.** `C-0072`'s verdict — that the two knife edges are one lattice quantity, that four floors exceed it, that the design which recovers the margin is a reduced path count — is untouched and reproduces here to `≤ 9e−6`. What moves is **how much** the reduction recovers: 53× becomes **68.9×**, and at 22 roots 87× becomes 316×. **A challenge whose correction improves the claim it is against is still a challenge**, because the number was presented as a property of the count and is not one |

---

## What is claimed upstream

`C-0072` (`T-134`, iteration 14), Deliverable 6, verbatim:

> *"**The ceiling itself moves at 30, and that is the whole finding.** `C-0063`'s bound 1 — `3a + 2(15 − a) = 34` — forces **four rows of three**, and a row of three is the only configuration in which two same-sense arms sit at the bare root pitch. Dissolve them and every row carries at most two; **the ceiling is then set by the tile edge, at 9.12 nm**. **The margin goes 53× for 12 % of the path count.**"*

and its table carries a `length ceiling` column reading **8.19** at 34, 33, 32 and 31, **9.12** at 30, 28, 25 and 22, and **20.00** at 15.

`C-0069` (`T-133`, iteration 13) carries the same column in its Deliverable 5 and it reads **8.19 nm in every row**, including the 15-path one, under the note *"the placement is unchanged because the count is what sets the stations"*.

**Both sentences attribute the ceiling to the count. It is a property of the placement, and the count only bounds it.**

## What the bound says

The construction is three lines and it is a proof rather than a search.

1. A placement's plan ceiling is `min_r ceiling(row r)`, because a rooted element of length `L` places iff **every** row admits it.
2. The rows are independent — no element of one row can reach another, the row pitch being one duplex — so a count vector may be chosen freely subject to `Σ_r k_r = n` and `k_r ≤ m`.
3. Therefore `max over placements of min over rows = sup{ L : C(L) ≥ n }` with `C(L) = Σ_r min(m, maximumRootedElementsInRow(r, L))`, and `C` is non-increasing in `L`, so a bisection is exact.

`maximumRootedElementsInRow` is **`C-0069`'s own function**, and it enumerates subsets in descending size rather than filling greedily, so each row's capacity is exact.

## The correction

`maximumPlanCeilingForCount` on the phase-24 upward lattice, against `C-0072`'s own reduction. `C-0074`'s Deliverable 1 in full; the rows where the two disagree:

| paths | arm [nm] | `C-0072`'s ceiling | **the lattice's ceiling** | `C-0072`'s margin | **the best margin** | understated by |
|---|---|---|---|---|---|---|
| 34 | 8.16439 | 8.1900 | **8.1900** | 0.02561 | 0.02561 | — (a row of three is forced) |
| 31 | 7.87152 | 8.1900 | **8.1900** | 0.3185 | 0.3185 | — |
| **30** | 7.77049 | 9.1200 | **9.5350** | 1.3495 | **1.76451** | **1.31×** |
| 28 | 7.56281 | 9.1200 | **9.5350** | 1.5572 | 1.9722 | 1.27× |
| 25 | 7.23574 | 9.1200 | **9.5350** | 1.8843 | 2.2993 | 1.22× |
| **22** | 6.88711 | 9.1200 | **14.9750** | 2.2329 | **8.0879** | **3.62×** |
| 20 | 6.64066 | 9.1200 | **14.9750** | 2.4793 | 8.3343 | 3.36× |
| **15** | 5.96298 | 20.0000 | **30.8800** | 14.037 | **24.917** | **1.78×** |

**Where the extra nanometre comes from, and it is the converging pose.**

`C-0072`'s sentence says the 30-root ceiling is *"set by the tile edge"*. It is set by `C-0053`'s **footprint convention** on a **converging** pair: two arms in one row may point at each other, needing `2L + d ≤ gap`, so a pair at `±10.88 nm` admits

&nbsp;&nbsp;&nbsp;&nbsp;`L ≤ (10.88 + 10.88 − 2.69)/2 = 9.535 nm`

and a pair at `±16.32` admits `(16.32 + 16.32 − 2.69)/2 = 14.975`. **Re-run with the converging pose forbidden, the maximum over placements is `9.1200 nm` at both centro-symmetric phases — `C-0072`'s number exactly, and asserted as a gate-3 test of `C-0074` rather than argued here.** So the disagreement at 30 roots *is* the value of that pose, and the reason `C-0072` does not get it is that its reduction leaves a binding row whose two roots are `{−10.88, 0.0}`: one at the origin, and the outer one's own reach to the tile edge is `20 − 10.88 = 9.12`.

At 22 roots the reduction rule leaves a three-site row carrying two where a four-site row could have carried them, and the four-site row's converging pair admits 14.975; hence 3.62×. At 15 roots, one per row, the ceiling is `20 + max|x|` minimised over the rows — **30.88 nm** on the outermost 10.88 nm site — where `C-0072`'s reduction leaves a root at the origin and reads 20.00.

## What does and does not move

**Does not move.** The identity `M = p − d − L` and its `4.4e−16` agreement between the two groupings; all four thermal and quantised floors against the **34**-root knife edge; the correlation analysis and its 7× common-mode factor; the twist coefficient of exactly zero; the seat's non-monotonicity; the whole literature deliverable; `T-45`'s answer from Strauss et al.; and the verdict that **at 34 roots the margin is not quotable**. Every one of these is read at 34 roots, where the two ceilings agree exactly because a row of three is forced.

**Does move.** The magnitude of the escape Deliverable 6 discovered. *"The margin goes 53× for 12 % of the path count"* becomes **68.9×**, and the design at 30 roots clears **all five** of `C-0072`'s own floors — including Fischer et al.'s measured 9.1 % lattice-constant width, 0.25 nm, by **7.1×** — where at 9.12 nm it clears them by 5.4×. The weakest floor, the arm tip's own bending at a rigid root, is cleared by **1.05×** at 1.76451 nm and would be **missed** at 1.3495.

**And it moves `C-0069`'s reading of its own sensitivity table**, which is the separate matter of [`CH-0087`](CH-0087-a-sensitivity-row-that-does-not-meet-the-mandate.md): held at 34 instances a row of three is forced at every count, so `C-0069`'s ceiling column cannot show the step at 31 that this bound makes visible.

## The general form

**A ceiling taken over a family is not a ceiling read at one member of it**, and the failure mode here is that the member was chosen by a rule written for a *different* objective — `rowsWithoutInteriorRoots` dissolves rows of three, which is what its documentation says it does, and nothing in it maximises a ceiling.

This is the same shape as `CLAUDE.md`'s *"an argmin selected on a coordinate orthogonal to the answer manufactures its own sensitivity"* (`C-0057`), and the same shape as *"a saturation measured inside one family is not a floor of the object"* — read here on a **maximum** rather than a minimum. It joins the standing discipline of *quote it with the state it is read at*, and the state is again a **placement**: `C-0074` is the eighth instance in this project and the second where the state is a set of lattice coordinates rather than a load.

## What would settle it in `C-0072`'s favour

1. **A footprint convention forbidding two arms in a row to converge.** Then the maximum over placements is **9.1200 nm** — `C-0072`'s own number — and the whole disagreement at 30 roots vanishes. It is measured in `C-0074`'s suite rather than argued, and the convention is `C-0053`'s, which both claims inherit and which admits the pose. The 22-root and 15-root disagreements survive it, being about which rows the reduction empties rather than about the pose.
2. **A reason the plan-rule reduction is the placement that must be built** — a flatness or a registration constraint that selects it. `C-0074` finds the opposite: under a distribution `C-0072`'s reduction reads **0.0732** at a peak ratio pinned to `C-0049`'s per-path ceiling, worse than every phase-8 placement priced (0.0648–0.0715) and with 24 % less plan margin.
3. **A demonstration that `maximumRootedElementsInRow` is not exact.** It is asserted against an exhaustive enumeration on a toy lattice as a gate-3 test of `C-0074`, at both caps, to `1e−6`.
