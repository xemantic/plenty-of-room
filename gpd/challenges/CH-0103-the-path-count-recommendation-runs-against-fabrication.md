# CH-0103 — **The programme's standing recommendation is to spend path count on plan margin, and fabrication charges for it on the axis nothing priced.** `C-0072` recommends 34 → 30 arms, `C-0074` supplies the 30-root placement and `C-0075` makes 30 the self-consistent count — all three trading paths for the **1.76451193 nm** margin that replaces `C-0069`'s 0.0256 nm knife edge. Under `C-0087`'s measured dropout the **path count is the dominant robustness axis** (0.8522 → 0.5327 over 15 → 90 paths, monotone at every step), so the recommendation moves *down* an axis fabrication charges *up*. **The trade is real, it is small at 34 → 30, and no claim in the corpus contains both of its terms**

| | |
|---|---|
| **Raised by** | [`C-0089`](../claims/C-0089-dropout-robust-placement.md) (`T-155`) |
| **Against** | [`C-0072`](../claims/C-0072-plan-tolerance-model.md)'s recommendation to reduce the array to 30 arms, [`C-0074`](../claims/C-0074-two-per-row-placement.md)'s recommended 30-root phase-8 placement and [`C-0075`](../claims/C-0075-path-count-consistency.md)'s self-consistent count table — specifically the reading that a **lower** path count is a strictly better design because it buys plan margin at no cost |
| **Grounds** | **the count is a robustness variable and all three claims treat it as a geometry variable.** `C-0089` sweeps the path count under the measured dropout and finds the 90th-percentile dishing **monotone decreasing** in it: 0.85219673 at 15 paths, 0.668998495 at 30, 0.614243977 at 45, 0.61234699 at 60, 0.572257776 at 75 and 0.532748246 at 90, with equal springs at `C-0017`'s unchanged total. The per-path force falls as `1/n` (6.667 → 1.111 pN against the 10 pN unzip allowable), so **nothing else in the acceptance stack opposes a higher count**. `C-0072`/`C-0074`/`C-0075` move the other way |
| **Severity** | **a MISSING COLUMN in a design table, not a wrong number — and the direction it points is now the branch's only escape.** No headline of any of the three claims moves: `C-0074`'s 0.06822 range dishing, its 9.5350 nm ceiling and its 1.76451193 nm margin are all zero-defect statements and all stand, and this challenge produces no number that contradicts them. What it says is that their *recommendation* was taken on a two-term trade of which only one term had ever been evaluated, and that the missing term is the one `C-0089` finds decisive |

---

## What is claimed upstream

`C-0072` (`T-134`, iteration 14) prices the plan tolerance of `C-0063`'s 34-arm array and finds the
margin a knife edge; `C-0069` measures it at **0.0256098233 nm** — *"`C-0055`/`C-0063`'s own
hinge-rooted arm clears the budget by 0.0256 nm"*. The recommendation that follows is to drop to
**30** arms, and `T-136`/`C-0074` supplies the placement:

> **`C-0074`** — *"The recommendation is 30 roots at phase 8 on the placement keeping the lattice's
> maximum plan ceiling 9.5350 nm — margin 1.76451 nm, 68.9× `C-0069`'s knife edge, 5.19 base-pair
> rises … dishing 0.06822 over the device's whole traversed range at a peak ratio of 2.057."*

`C-0075` then makes the count self-consistent: at 30 paths the arm is **7.77048807 nm** against a
**9.535 nm** ceiling, where at 45 it is **9.13115573 nm** against **8.19 nm** and only 30 place.

Every one of those statements is a **zero-defect** statement. The word *fabrication* appears in
none of the three trades.

## What `C-0089` measures

`T-155` sweeps the attachment count under `C-0087`'s measured per-staple incorporation map, 10 000
seeded realisations per cell, `C-0017`'s unchanged total shared equally, `C-0022`'s solved
2 mM / 10 nm / 0.192 V load:

| paths | zero-defect dishing | **90th percentile under the measured dropout** | per-path force [pN] |
|---|---|---|---|
| 15 | 0.695201577 | **0.85219673** | 6.6667 |
| 30 | 0.350380481 | **0.668998495** | 3.3333 |
| 45 | 0.21821335 | **0.614243977** | 2.2222 |
| 60 | 0.182275271 | **0.61234699** | 1.6667 |
| 75 | 0.168141958 | **0.572257776** | 1.3333 |
| 90 | 0.161116195 | **0.532748246** | 1.1111 |

and the mechanism has a closed form that needs no solve: **a dropout is an increase in the
attachment pitch**, so a design survives `j` consecutive absences only while
`columns ≥ (j + 1)·edgeX/ℓ`, with `ℓ = 12.8290845 nm`. At every count from 45 upward the
90th-percentile longest run is **3** and the demand is **13 columns, 195 paths**.

## Why this is a challenge and not a note

Three separate claims recommend the **same** move on the **same** ground, and the ground is one
half of a trade:

| | what 34 → 30 buys | what 34 → 30 costs |
|---|---|---|
| **priced** | plan margin **0.0256098233 → 1.76451193 nm**, 68.9× (`C-0072`, `C-0074`, `C-0075`) | — |
| **not priced anywhere** | — | **the path count, which is the axis fabrication charges on** |

`CLAUDE.md` already carries this shape of error twice — *"a claim's consequences are not confined
to the task it was written for"* and *"a window gains an axis when a constraint is discovered"* —
and here the axis was discovered one iteration *after* the recommendation was taken.

## What is NOT claimed

- **The trade does not reverse the recommendation at 34 → 30.** On its own station set
  `C-0074`'s 30 roots read a 90th percentile of **0.583664426** and `C-0063`'s 34 read
  **0.639129638**, so the 30-root design is nominally the *better* of the two under fabrication.
  That is a property of the **placement**, not of the count — the 30-root set is a different
  lattice at a different phase whose zero-defect equal-spring dishing (0.242359741) is 3.4× worse
  than `C-0063`'s to begin with, and the 22-cell sweep shows the count effect only when the
  station geometry is held fixed.
- **It does not change any published number.** Nothing in `C-0072`, `C-0074` or `C-0075` is
  arithmetically wrong.
- **It does not by itself make the 34-path design flat.** `C-0089`'s verdict is that *no* design
  in the family is, at any count the tile can carry.

## What would settle it

1. **A path-count sweep at FIXED station geometry on the upward lattice**, i.e. the 30-, 34- and
   45-root placements of the *same* phase graded under the dropout, so that the count and the
   placement are separated on the lattice the arms actually root on. `C-0089`'s separation is on
   the abstract `m × 15` grid.
2. **A re-run of `C-0072`'s plan-tolerance model with the path count as a free variable and the
   dropout as a second objective** — the two-term trade, run once.
3. **A per-site incorporation measurement on a coupling-bearing tile.** If the coupling's own
   incorporation is materially above the staple's, the axis this challenge is written on stops
   binding and the recommendation is unimpeachable on its original ground.

## Suggested disposition

**Record the second term in `C-0072`'s, `C-0074`'s and `C-0075`'s recommendation rows** — *"the
count also buys dropout robustness, monotonically, and this trade was taken on the plan term
alone"* — and add the fixed-geometry count sweep to the queue as **`T-163`**. No number is withdrawn.
