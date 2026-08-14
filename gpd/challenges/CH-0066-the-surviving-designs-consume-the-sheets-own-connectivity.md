# CH-0066 — All three of `C-0046`'s surviving designs spend more crossovers than a connected sheet has to give: 14 of the tile's 56 are the sheet's own connectivity, so the hinge budget is 42 and the reported designs need 45, 50 and 56 — the best point of the space is `(42, 1)`, not `(56, 1)`

| | |
|---|---|
| **Against** | [`C-0046`](../claims/C-0046-fewer-longer-flexures.md) — its verdict clause *"**The best point of the whole space is `(56, 1)`**: an arm of 9.973 nm = 29.3 bp, a tangent of 38.17 pN/nm at §3's acceptable stroke, and a usable stroke of 3.312 nm"*, its Deliverable 4 table (*"**Only 3 of the 31 swept points clear even the acceptable stroke, and all three are at `h = 1`: 45, 50 and 56 paths**"*), and its *"**The window's width is 1.24× in path count**"* |
| **Raised by** | [`C-0054`](../claims/C-0054-consumed-crossover-sheet.md), task [`T-110`](../tasks/T-110-consumed-crossover-sheet.md) — which is `C-0046`'s own *"Still open"* item 1 |
| **Grounds** | **a budget spent past the point at which the thing it is drawn from stops existing** |
| **Effect on numbers** | **NONE of `C-0046`'s numbers changes and every one reproduces** — the `(45, 1)` arm to `1.7e−5`, its tangent to `4.2e−5`, its usable stroke to `2.6e−5`, the `(56, 1)` usable stroke to `4.7e−5`. **What changes is which of them is a design.** The best *buildable* point is `(42, 1)`: arm 8.882 nm = 26.1 bp, tangent 39.54 pN/nm, usable stroke **3.063 nm**. The surviving window is **`39 ≤ n ≤ 42`**, not 45–56, and `C-0046`'s unresolved `34 < n ≤ 45` threshold is **39**. The cost of the correction is **7.4 %** of the usable stroke and it buys a sheet that is a single body. |

---

## What is claimed upstream

`C-0046` sweeps the `(path count, hinge count)` trade against two ledgers — the crossover inventory `n·h ≤ 56` and the collinear-interface supply `n(h−1)p ≤ 640 nm` — and reports:

> **The best ledger-admitted point of the whole space** — `(56, 1)`, an arm of **9.973 nm = 29.3 bp**
> — … its usable stroke inside the ceiling is **3.312 nm**

and

> Of the 31 swept points at the standing placement, **3 clear §3's acceptable 3 nm stroke** …
> **The window's width is 1.24× in path count and zero in hinge count**, and what changes across it
> is only how completely the sheet is consumed.

`C-0046` sees the exposure and names it, in its own validity range and in its open item 1:

> **Converting the inventory into hinges removes it from the sheet.** The surviving design spends
> 80–100 % of the tile's crossovers, and what that does to `C-0009`'s `D_⊥`, to `C-0015`'s flatness
> grid and to `C-0006`'s load distribution is **not computed here**. It is the largest open item.

**So this challenge is not a correction of an oversight; it is the answer to a question the claim asked itself.** It is filed rather than absorbed because the answer changes which rows of the claim's own tables are designs, and `C-0046` is cited elsewhere for its best point.

## Why the ledger is one constraint short

`C-0046`'s inventory ledger is `n·h ≤ 56` — *"the tile's crossovers"*. It treats the inventory as a **budget to be spent**, and a budget with no reserve.

But the crossovers are not a stock of spare parts. They are the sheet's **only** across-helix load path (`C-0009`) and its only across-helix compliance, and — the point the ledger misses — they are the **only thing holding the duplexes together at all**. The interfaces of a single-layer sheet form a **path graph** on its 15 duplexes: duplex `b` is joined to `b−1` and `b+1` and to nothing else. So

&nbsp;&nbsp;&nbsp;&nbsp;**a connected sheet needs at least one retained crossover on each of its `D − 1 = 14` interfaces**,

and the hinge budget is not 56 but

&nbsp;&nbsp;&nbsp;&nbsp;**`N − (D − 1) = 56 − 14 = 42`, i.e. 75.0 % of the inventory.**

`C-0046`'s admissible region is **80–100 %**. Every point of it is above the ceiling.

| `C-0046`'s design | crossovers spent | fraction | **empty interfaces** | **pieces the tile is in** |
|---|---|---|---|---|
| `(45, 1)` — *"clears the acceptable stroke"* | 45 | 80.4 % | ≥ 3 | **≥ 4** |
| `(50, 1)` | 50 | 89.3 % | ≥ 8 | **≥ 9** |
| **`(56, 1)` — *"the best point of the whole space"*** | **56** | **100 %** | **14** | **15 — fifteen separate duplexes** |

This is a pigeonhole. It needs no lattice solve, no force field and no fitted constant, and it is tight: `C-0054` shows 42 leaves exactly one piece under a spreading pattern and 43 does not, and that no arrangement leaves fewer pieces than the spreading one at any level.

## The premise it rests on, stated because everything turns on it

**A crossover cannot serve as a flexure hinge and as a sheet interface at the same time.** `C-0040`'s own definitions settle it: a hinge line is *"a maximal set of crossovers that share **one interface** and **one pair of bodies**"*, and `k_θ` is the **interhelical dihedral** spring — the constant with which `C-0046` prices its arm. A hinge that turns puts its two bodies at an angle, so whatever is outboard of the line has left the sheet; and a reciprocal strand exchange has two strands and two partners, so a site exchanging with an arm is not also exchanging with the neighbouring duplex.

If that premise fails — if an arm could be joined at an azimuth a Rothemund sheet never uses — the two uses would be **additive** and this challenge falls with it. `C-0029`'s survey is the standing evidence against: *"a duplex standing normal to a single-layer sheet is NOT an established motif"*, and every published out-of-plane body on an origami plate is held by a **pin**.

## What follows, and what does not

**Does not follow.** Any change to a number `C-0046` reports. Its degeneracy result — that the arm is a function of the product `n·h` alone, so *fewer, longer* is the wrong direction — is untouched, re-run and reproduced. Its desired-stroke verdict (0 of 31 and 0 of 29) is untouched: the correction moves the path count *down*, which lowers the arm, which cannot reach 10 nm either. `CH-0059` is untouched.

**Does follow.**

1. **`(56, 1)` is not a design and should not be quoted as the branch's best point.** It is a tile in
   fifteen pieces. The best buildable point is **`(42, 1)`**, usable stroke **3.063 nm**.
2. **The window is `39 ≤ n ≤ 42`, not 45–56**, and it is bounded *above* by connectivity rather
   than by the inventory. `C-0046`'s statement that *"what changes across the window is only how
   completely the sheet is consumed"* is exactly right and is the reason the window's upper end is
   not admissible.
3. **`C-0046`'s open path-count threshold is resolved: 39.** Its bracket `34 < n ≤ 45` contained the
   answer and its grid did not sample it.
4. **The margin is thin and should be reported as such.** 3.063 nm against §3's 3.000 nm is **2.1 %**,
   on a model whose `k_θ` carries a ±20 % fitted bracket. `C-0046`'s 3.312 nm reads as a 10 % margin
   and the buildable one does not.
5. **A ledger should carry the reserve its resource needs to go on existing.** This is the same
   discipline as quoting a stiffness with its compression or a variance with its bandwidth: the
   inventory is not the budget, the inventory **minus the sheet's own connectivity** is.

## An independent confirmation, filed in the same iteration

[`C-0053`](../claims/C-0053-hinge-arm-array-packing.md) (`T-116`) solves the **plan view** of the same `E5a1` array and arrives at the same refusal from the opposite direction. It charges an arm more than this challenge does — a real arm is a length of the host's own duplex cut free at both ends, so it also **buries** crossovers beneath itself — and finds that at its self-consistent **43** arms the host has **no bonded component at all**, with **25** the largest count that leaves all fifteen duplexes bonded.

**42 is what the counting permits and 25 is what the geometry delivers.** Both refuse `C-0046`'s 45, 50 and 56, and the two bounds compose: this challenge's window `39 ≤ n ≤ 42` is **necessary and not sufficient**, and `C-0053` closes it from the other side — where `C-0046`'s own curve gives 2.469 nm at fifteen paths, below §3's acceptable stroke, so the branch's remaining room is between 25 and 42 and is narrower than either claim alone reports.

## How this challenge would itself be defeated

1. **A junction motif at an azimuth the single-layer sheet does not use** — which would make hinge
   use and interface use additive, restore the whole 56 to the budget, and reinstate `(56, 1)`.
   That is a chemistry question and `C-0029`'s survey is the standing evidence against it.
2. **A superstructure that carries the across-helix load path in place of the sheet.** Then a
   severed tile is a component of a larger body rather than a broken one. `C-0041` finds the
   superstructure severs *itself* on tie apertures, so this needs an answer of its own first.
3. **A larger tile.** More duplexes cost more interfaces but the inventory grows faster: at `D`
   duplexes and 4 crossovers per interface the ceiling is `4(D−1) − (D−1) = 3(D−1)`, i.e. **75 % at
   every size**. So the fraction does not move and only the absolute count does — 42 at 15 duplexes,
   63 at 22. That is `T-102`'s footprint question with a coefficient attached.
