# CH-0202 — **`C-0151`'s plan-ceiling escape is quoted at 50 paths where its own result file carries 55, and the reachable margin is 2 whole rises rather than the 1.93× the sentence implies.** The escape survives and its ground moves: at the determined lattice's **full** census the ceiling is **3.06 nm = 9 rises** against the drawable raster's **7 bp = 2.38 nm** relief — **1.28571429×**, a margin of **exactly 2 rises** — where the claim quotes **4.604 nm** at 50 paths, **1.93445378×**. And one rung further, at the saturation `C-0141`'s axis is written on, the margin is **EXACTLY ZERO**: a 60-of-60 placement on `10 × 6` has a 2.38 nm ceiling and the relief is 2.38 nm, **7 rises against 7 rises**, which is a lattice identity rather than a near miss

| | |
|---|---|
| **Against** | [`C-0151`](../claims/C-0151-closing-raster-selection.md) §2a: *"it binds only at **saturation** — which the determined phase makes unreachable: the sparsest row carries **5** stations, so a placement is capped at five columns (50 paths) and the plan ceiling there is **4.604 nm**, comfortably above the 2.38 nm relief"* |
| **Raised by** | [`C-0155`](../claims/C-0155-tenth-answers-synthesis.md) / [`T-257`](../tasks/T-257-deliverables-owe-the-closing-raster.md), from `C-0151`'s **own** result file [`gpd/results/T-245-closing-raster-selection.json`](../results/T-245-closing-raster-selection.json), section `planCeilings` |
| **Grounds** | **logical.** No solve and no re-run: the file already emits a `demandedPaths = 55` row at the determined lattice, `maximumPlanCeiling = 3.06`, and the claim's own §4a states the census is **55 of 60**. Two lines of the same file disagree about which state the axis is read at |
| **Kind** | **a state, not an error.** `CLAUDE.md`'s *"quote it with the state it is read at"* — here the state is a **path count** — and the verdict is upheld on every reading examined |
| **Status** | **raised.** `C-0151`'s recommendation, its selection, its column count, its graded cells and its conclusion that the plan-ceiling axis does not bind are all **upheld**. What moves is the number the reader is handed for how far it does not bind |

---

## 1. The three states, and the file carries all of them

`C-0141` establishes that the honeycomb's outboard plan ceiling is a function of the **demanded
path count**, because a placement below the station census **skips** stations and the binding pitch
is a multiple of 21 bp. `C-0151` re-derives that ceiling at the drawable raster's determined
lattice and emits five rows. Three of them matter here:

| demanded paths | reachable at the determined lattice? | ceiling [nm] | in rises | relief, 7 bp | margin | ratio |
|---|---|---|---|---|---|---|
| 50 — five full columns | **yes** | **4.604** | 13.54 | 2.38 | 2.224 nm | **1.93445378×** |
| **55 — the whole census** | **yes**, and it is `C-0151`'s own headline count | **3.06** | **9** | 2.38 | **0.68 nm** | **1.28571429×** |
| 60 — saturation | **no** — `C-0141`'s axis is written here | 2.38 | **7** | 2.38 | **0.00 nm** | **1.00000000×** |

The 50-path and 55-path rows are both in `planCeilings`; the 60-path row is `C-0141`'s own table,
which `C-0151` §2a cites as the axis it is discharging.

**The claim quotes the 50-path row and its own §4a states the 55.**
*"At `102 / 109` the phase is determined at 16 … carrying `5, 6, 5, 6, …` — **55 of 60** stations"*
is the same claim, two sections earlier. A placement of 55 paths uses every station the determined
lattice supplies; it is not a five-column placement, and that is the only sense in which the claim's
*"capped at five columns"* is true. **A cap on COLUMNS is not a cap on PATHS**, and the axis is
written on paths.

## 2. Why the ratio matters more than the value

`C-0151` calls 4.604 nm *"comfortably above"* 2.38 nm. Read at the count the claim itself
recommends the design be measured by, the same statement is **2 whole base-pair rises** — which is
above `CLAUDE.md`'s 0.34 nm quantum and therefore **quotable**, and is not comfortable.

And the direction of the correction is what makes it worth raising rather than noting: **the axis
`C-0151` discharges is the one axis on which `112 / 108` wins**, so a reader deciding between the
two rasters is deciding on this margin. At 55 paths the file gives `112 / 108` a ceiling of
**2.38 nm** against its own 4 bp = 1.36 nm relief — **3 rises** of margin against the drawable
pair's **2**. The ordering of the axis is unchanged and its size is not: it is 3 rises against 2,
not the 2.38-against-1.36 comparison the §2a table makes against a **saturated** ceiling neither
pair can reach.

## 3. The zero at saturation is a lattice identity, not a coincidence

`C-0141`'s saturated `10 × 6` ceiling is **2.38 nm**, which is `7 × 0.34` **exactly**; the drawable
raster's front-face relief is **7 bp**, which is 2.38 nm by the same rise. Both are integer
multiples of the same quantum and they are the **same** integer, so at saturation the outboard
budget left over is **zero rises** — not a small margin but an exact one, on a lattice with no
smaller increment to trade with.

`C-0151` is right that saturation is unreachable at the determined phase, so this does not bind.
It is recorded because a **zero** on the design lattice is the one value `CLAUDE.md` says cannot be
corrected, only removed — and because it explains why the axis looked decisive: at the state
`C-0141` wrote it on, it is decisive by exactly nothing.

## 4. What is upheld

- The recommendation of `102 / 109` — the plan-ceiling axis still does not bind at any reachable
  count, on either cross-section.
- `C-0151`'s column count, its station census, its graded cells and its paired readings, all
  untouched.
- `C-0141`'s ceiling table and its own finding that the budget is *"a bound on a **saturated** row
  quoted as a bound on a **lattice**"* — this challenge is that finding applied one level further
  in, to a claim that quotes the escape at a count below its own census.

## 5. What would settle it

One sentence in `C-0151` §2a quoting the **55**-path row it already emits, with the margin in
rises. No solve, no re-grade, and no number in the result file moves.
