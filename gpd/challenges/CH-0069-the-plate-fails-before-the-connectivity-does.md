# CH-0069 — `C-0054`'s surviving window `39 ≤ n ≤ 42` lies entirely **past** the count at which the continuum reduction that priced it stops being valid: the patch criterion crosses one at **37.3 spent**, so every point of that window is a connected sheet described by a plate that no longer applies

| | |
|---|---|
| **Raised by** | [`C-0056`](../claims/C-0056-connectivity-ceiling-plate.md) (`T-120`) |
| **Against** | [`C-0054`](../claims/C-0054-consumed-crossover-sheet.md) (`T-110`) — specifically the reading of its `39 ≤ n ≤ 42` window as a region in which its **continuum companion** numbers still describe the sheet, and its *"the step is at severance, not at consumption"* |
| **Status** | **OPEN, and CONDITIONAL on [`CH-0068`](CH-0068-the-hinge-inventory-is-not-the-sheets-own.md)** — this challenge is written against `C-0054`'s window, which assumes crossovers are spent; `C-0055` argues they need not be, and if that is upheld the window and this challenge fall together. See the closing section. |
| **Grounds** | **methodological**: a validity range that was never checked, on a claim whose own *"Still open"* item 5 named the check and did not run it |

---

## What `C-0054` says

`C-0054`'s verdict is a **pigeonhole**, and nothing here touches it: a connected 15-duplex sheet
needs one retained crossover on each of its 14 interfaces, so at most **42 of 56** can be spent, and
`C-0046`'s three surviving designs all sever the tile. It then resolves `C-0046`'s open bracket and
reports

> *"the window on a sheet in **one piece** is **39 ≤ n ≤ 42**, delivering 3.005–3.063 nm"*

and, on the modelling side,

> *"the step is at severance, not at consumption. Spending 42 of 56 crossovers costs **11 %** of
> the dishing; spending three more costs **92 %**."*

Both statements are **correct as computed** — every one of `C-0054`'s numbers reproduces here, 20 of
them as gate-5 tests, worst strict departure `1.8e−2` against a value printed to two significant
digits.

## What this challenge is

**Not that the window is wrong. That the window has an unchecked axis, and the axis is the validity
of the continuum reduction the rest of the programme uses to price anything inside it.**

`C-0054`'s own *"Still open"* item 5 names it exactly:

> *"A sheet at the ceiling has **one** crossover per interface, and `C-0009`'s own localisation
> result (an anchor is carried by its two nearest crossovers) says such a sheet is not a plate in
> any useful sense — its `ℓ_⊥/d` is not re-derived here."*

`T-120` re-derived it, and the answer is worse than the item anticipated in one direction and
better in another:

| | value | |
|---|---|---|
| crossovers in an anchor's influence patch, intact | 3.929 | `C-0009`'s own |
| **the same at the ceiling (14 retained)** | **0.694** | **under one** |
| **the retained count at which it reaches one** | **18.74** | |
| **the SPENT count at which it reaches one** | **37.26** | **`C-0054`'s whole window, 39–42, is past it** |
| `ℓ_⊥/d` at the ceiling | 1.061 | **still above one** — item 5's own criterion does **not** fire |

So the criterion `C-0054` named does not detect the failure, and a criterion it did not name does —
which is why this is a methodological challenge rather than an arithmetic one.

## Why it matters, and by how much

**A criterion is not a model**, so `C-0056` also measured the consequence, and the size of it
depends entirely on which quantity is asked for:

| quantity, at the ceiling (42 spent) | smeared plate vs the lattice |
|---|---|
| peak dishing under `C-0022`'s **solved** load, 3 × 15 grid | **14.9 %** — the plate is still the nearer model |
| peak deflection under a concentrated **100 pN lever** | **33 %** (staggered retention) to **79 %** (`C-0054`'s own round robin) |
| **point compliance a coupling feels**, max over one crossover cell | **3.2×** (1.046 against 0.330 nm/pN), and the **uncoupled beam array** is nearer from **28 spent** |
| **spread** of that compliance over the cell | **2.53× on the lattice against 1.14× on the plate** — a plate cannot be inhomogeneous at all |

&nbsp;&nbsp;&nbsp;&nbsp;**So `C-0054`'s own flatness and load numbers are safe** — they are computed
on the **lattice**, and its 0.242 reproduces to `1.3e−3`. **What is not safe is any downstream use
of a plate inside that window**, and `C-0006`, `C-0010`, `C-0022` and `C-0047` are all plate-based
in part.

## The four thresholds, which is the point

`C-0056` finds that the counts a design might read as *"where the plate fails"* are four different
numbers in a strict order, asserted as a runtime check:

&nbsp;&nbsp;&nbsp;&nbsp;**28** *(the beam array becomes nearer on the point compliance)*
**< 37.3** *(the patch criterion)* **< 42** *(connectivity — `C-0054`'s)*
**< 45** *(a smooth load finally notices, and that is severance)*

**1.61× in the crossover count between the first and the last.** `C-0054` reads the third and reports
*"the step is at severance"*, which is the fourth. Both readings are right about the quantity they
were taken on; neither is the count at which the continuum reduction stops applying.

## What `C-0054` should carry, and what it should not

**It should not be withdrawn or renumbered.** The pigeonhole, the component counts, the two rigidity
conventions and their `(15/14)²` identity, the flatness, load and variance tables, and the resolved
`39` threshold are all lattice-computed and all reproduce.

**What it should carry is a validity qualifier**, in the same form the programme already uses for a
stiffness-with-a-compression and a variance-with-a-bandwidth:

> **The `39 ≤ n ≤ 42` window is a window in which the sheet is CONNECTED. It is not a window in
> which the sheet is a continuum: the patch criterion crosses one at 37.3 spent, so every point of
> the window is past it, and any plate-based number quoted inside it is out by 15 % on a smooth
> load and 33–79 % on a point-coupled one. Quote the lattice.**

## One thing that goes `C-0054`'s way

Its harmonic-versus-smeared discrepancy — the one it calls *"a change of kind, not of number"* — is
**upheld as the physics and shown not to be what fails first**. At and below the ceiling every
interface still holds a crossover, so the Voigt and Reuss readings agree to `(15/14)² = 1.1480`
**exactly**, and the series rigidity is finite. The collapse to zero belongs to the **first empty
interface**, at 43 spent — one crossover past `C-0054`'s ceiling and **six past** where the patch
criterion has already refused the plate. **So the continuum fails for a reason `C-0054` did not
name, and it fails before the reason it did name arrives.**

## How this challenge would be answered

1. **A better depletion rule.** `C-0056` uses the areal-density pitch `p_eff = p N/N_ret`. If the
   right pitch is a *local* one, every threshold moves — but toward **worse**, because a
   non-uniform retention has a larger local pitch somewhere. The direction is settled.
2. **A homogenisation theory for a sparse lattice** that beats both the smeared plate and the
   uncoupled beam array at the ceiling. Neither is a model of the ceiling; the honest model is the
   lattice.
3. **A design that applies no point load to the sheet.** Then the 15 % smooth result is the whole
   answer and the plate survives to severance, which is what `C-0054` says. `C-0017`'s coupling is a
   set of discrete attachments, so this programme does not have that design.
4. **[`CH-0068`](CH-0068-the-hinge-inventory-is-not-the-sheets-own.md), and this is the one to watch.**
   [`C-0055`](../claims/C-0055-unused-junction-site.md), filed in the same iteration, finds that a
   single-layer sheet occupies **two of the square lattice's four** crossover azimuths, so a hinge can
   be rooted **out of plane** and no interface crossover need be spent at all — *"52–60 with every
   interface intact"*. If that is upheld, the Gen-1 design sits at `N_ret = 56`, the patch count is
   `C-0009`'s own 3.93, **`C-0054`'s `39 ≤ n ≤ 42` window is not the operating point either**, and this
   challenge is moot along with the window it is written against. **`C-0056`'s criteria are unaffected
   in every case**, because they are functions of the retained count and say nothing about how a
   design arrives at one. **The two challenges should therefore be resolved together, and `CH-0068`
   first.**
