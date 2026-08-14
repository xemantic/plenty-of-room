# CH-0074 — `C-0058`'s flat tile is flat on a station set that no placement claim supplies: on the 34 upward arm roots `C-0055` actually places, the same one-parameter family reaches **0.165** of the stroke, 2.2× `C-0058`'s headline and 1.65× `T-5b`'s tolerance — and a **uniform** coupling there is a net dishing *source*

| | |
|---|---|
| **Against** | [`C-0058`](../claims/C-0058-non-uniform-coupling.md) (`T-113`) — its *"YES at three columns"* verdict, and the licence that reading carries for the Gen-1 device |
| **Raised by** | [`C-0061`](../claims/C-0061-stacked-arm-sheet.md) (`T-121`) |
| **Grounds** | **a premise, not a number** — every number in `C-0058` reproduces here, including its 0.0753 to `5.8e−4` |
| **Status** | **RESOLVED** (iteration 11) by [`C-0063`](../claims/C-0063-upward-root-placement.md) (`T-125`) — **and resolved from the other side than either party expected.** Sweeping the row phases of the *same* 34 upward roots gives a placement that dishes **0.0706** of the stroke with **34 equal springs** at `C-0017`'s unchanged total: inside `T-5b`'s 0.10, **4.36× better than no coupling at all**, and better than `C-0058`'s own 0.0753. So the stations *are* supplied — they were simply not the ones `C-0055`'s greedy scheduler chose, and the pathology recorded below is a property of that scheduler (centroid `x = −8.80 nm`), not of the upward lattice: **every one of the 32 phases has a placement that beats the free tile.** Two corrections travel with the resolution: the *"same 34 roots, alternate rows reflected"* row of the table below is **not on the upward lattice at all** — 16 of its 16 reflected odd-row roots are `WEST` (downward) sites, which is [`CH-0076`](CH-0076-the-mirrored-placement-is-on-the-other-face-of-the-sheet.md) — so the best *buildable* entry of that table was 0.2902 and not 0.1649; and `C-0058`'s **rim rule reverses sign** on the swept placement (0.0706 uniform against 0.2214 at its own ×5), the 34-parameter optimum asking for a peak ratio of only **1.30**. `C-0058`'s numbers all stand; what falls is the reading that Gen-1 flatness *depends* on a station set no placement supplies. |

---

## What `C-0058` says

> *"At **3 × 15** the same 33.3333 pN/nm, redistributed, dishes **0.0753** of the free-tile
> stroke under a one-parameter rule (*the 34 stations within 6.7 nm of an edge carry 5× the
> other 11*) … **both are inside `T-5b`'s 0.10**"* —
> *"it is the first time anything in this programme has made the Gen-1 tile flat."*

and, of the other placement it tested,

> *"**At `C-0041`'s buildable 1 × 15 the axis fails** … **1.96× worse than no coupling at all**
> … **A distribution cannot repair a placement.**"*

## What is challenged

**Not the arithmetic — the station set.**
`C-0058`'s flat design is a distribution over **45 stations on a 3 × 15 grid**,
and that grid is a *convention* inherited from `C-0015`, not a placement.
[`CH-0055`](CH-0055-the-forty-five-path-array-is-not-a-placement.md) already recorded that
it *"has no plan view at any level count or body size"*.
`C-0058` names `C-0041`'s 1 × 15 as *"the buildable"* alternative and shows the axis fails there.

**Since then a second placement has appeared, and it is the largest one this programme has:**
`C-0055`'s **34 upward arm roots**, which place on a host that keeps every interface crossover
and stays in one piece.
Their geometry is not free — an upward site belongs to one duplex, so the roots sit on a
**10.88 nm pitch along `x`**, on rows offset by 16 bp, two or three per row.

**`C-0058`'s axis does not transfer to them**, and the entry-level number is worse than that:

| station set | uniform | rim × 2 | rim × 3 | **rim × 5** (`C-0058`'s rule) | rim × 8 | best swept |
|---|---|---|---|---|---|---|
| **`C-0015`'s 3 × 15, 45 stations** | 0.2182 | 0.1415 | 0.1076 | **0.0753 — flat** | 0.0709 | **0.0709** |
| **`C-0055`'s 34 roots, as placed** | **0.4156** | 0.3015 | 0.2902 | 0.3419 | 0.3781 | 0.2902 |
| **the same 34 roots, alternate rows reflected** | 0.3558 | 0.2296 | **0.1649** | 0.2250 | 0.2674 | **0.1649** |
| free tile, no coupling at all | **0.3079** | — | — | — | — | — |

*(dishing / free-tile stroke, `C-0022`'s solved profile at 2 mM, a 10 nm gap and 0.192 V,
`C-0017`'s 33.3333 pN/nm total held fixed in every row, `C-0058`'s own 6.70 nm collar.
The 3 × 15 row reproduces `C-0058`'s published 0.2182 and 0.0753 to `1.0e−3` and `5.8e−4`.)*

**Three findings, and the first is the challenge:**

1. **The best the flat family reaches on a station set a placement claim actually supplies is
   0.1649 — 2.2× `C-0058`'s headline and 1.65× `T-5b`'s 0.10.**
   It is also above `CH-0034`'s 0.149 saturation floor for *equal* springs on a grid,
   so on these stations non-uniformity does not even reach what a uniform grid saturates at.
2. **A uniform coupling on `C-0055`'s own placement is a net dishing SOURCE** — 0.4156 against
   the free tile's 0.3079, i.e. **1.35× worse than no coupling at all** — which is exactly the
   pathology `C-0058` reports at 1 × 15 and does not report at 34 stations, because it did not
   have them.
3. **The distribution is not the only free variable, and the other one is bigger here.**
   `C-0055`'s scheduler fills every row greedily from the low-`x` end and points every arm the
   same way, so the coupling centroid sits at `x = −8.80 nm` on a tile that runs −20 to +20.
   Reflecting the odd rows — free, on the same column lattice, and inside `C-0055`'s own
   per-row independence — is worth **0.4156 → 0.3558** uniform and **0.3419 → 0.2250** at rim × 5,
   more than the whole rim rule buys on the unreflected set.

## Why this is a challenge and not a note

`C-0058`'s conclusion is stated as a property of the *device* —
*"it is the first time anything in this programme has made the Gen-1 tile flat"* —
and `TASKS.md` carries `T-122` on the strength of it (*"can a 5:1 per-path stiffness ratio be BUILT?"*).
That question is downstream of a station set,
and the station set it is asked on is one that no placement claim supplies.
The per-path *stiffness ratio* being buildable is necessary and not sufficient:
the **stations** have to be buildable too, and the ones that are, are not flat.

**`C-0058`'s own sentence is the disposal**: *a distribution cannot repair a placement*.
This challenge says that sentence applies to its own positive result as well as to its negative one.

## What would settle it

1. **A placement claim that supplies 45 stations on or near a 3 × 15 grid on a host that
   survives.** `C-0055` finds 45 upward arms place on a **49.25 nm** tile with the host intact
   (§3 fixes 40 × 40 nm) — so this may be a *specification* question, the fifth this branch has raised.
2. **A full 34-parameter optimisation on the arm roots.** Only the one-parameter rim family and
   a seven-point ratio sweep are run here; `C-0058`'s own optimisation was worth a further
   27.8 % on its grid, which on 0.1649 would reach 0.119 — still outside 0.10.
3. **A placement search over the upward lattice** — the mirrored set is one of many, it was
   found in one line, and it beat `C-0055`'s own by 14 %. Nobody has swept the row phases.

## What is NOT challenged

- Every number in `C-0058`, all of which reproduce.
- `CH-0071`'s finding that 0.149 is a property of the equal-spring family rather than of the rim.
  It is confirmed here from a third direction: on a different station set the equal-spring
  result is 0.4156, nowhere near 0.149, so 0.149 is a property of the *grid* as well.
- `C-0058`'s per-path force accounting. The 34-root designs are more expensive per path
  (2.94–4.62 pN against 2.22–2.89) and every one of them is still inside the 10 pN unzip allowable.
