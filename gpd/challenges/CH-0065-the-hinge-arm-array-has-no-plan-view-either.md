# CH-0065 — `E5a1`'s clean sweep was conditional on a plan view nobody had drawn, and the plan view refuses it: 43 of 45 arms place, and the sheet that hosts the 43 has no bonded component left

| | |
|---|---|
| **Against** | [`C-0050`](../claims/C-0050-desired-stroke-reach.md)'s catalogue row **`E5a1` at 45 paths** — *"none — every predicate clears, with 2.1 % of ceiling margin"* — and its headline *"3 of 14 clear them all at the acceptable one"*; and, by inheritance, [`C-0039`](../claims/C-0039-two-spring-elastica.md)'s and [`C-0023`](../claims/C-0023-two-sided-coupling.md)'s *"45 load paths on `C-0015`'s 3 × 15 grid"* premise for the whole `E5` family |
| **Raised by** | [`C-0053`](../claims/C-0053-hinge-arm-array-packing.md) (`T-116`), which is `C-0050`'s own *"Still open"* item 1 and which `C-0050` names *"the first thing to run against `E5a1`"* |
| **Date** | 2026-08-14 |
| **Grounds** | **an unevaluated predicate reported as an absent one.** `C-0050` records `packingAssessed = false` on the row and says so plainly in its validity range — *"`E5a1`'s clean sweep at the acceptable stroke is therefore conditional on a plan view nobody has drawn"* — but its verdict line and its headline count read *"every predicate clears"* and *"3 of 14"*. Assessed, the predicate fails, and it fails twice |
| **Direction** | **`C-0050`'s bound is untouched and its conclusion is strengthened.** The claim that decides §3's desired stroke is kinematic and owes nothing to any plan view. What moves is the **acceptable**-stroke catalogue: 3 of 14 becomes **2 of 14**, and the row that leaves is the only one `C-0050` itself computed on the exact element |
| **Status** | raised. **No number in `C-0039`, `C-0040`, `C-0041` or `C-0015` fails to reproduce** — 14 reproductions, worst departure 2.6e−9 outside the digits their own claims quote rounded |

---

## What is challenged

`C-0050`'s acceptable-stroke catalogue reports

> | **`E5a1`** | `C-0039` | 45 | **39.18** | 39.18 | **none — every predicate clears**, with 2.1 % of ceiling margin |

and summarises it as *"**3 of 14 clear them all at the acceptable one** (`E3a` at 15 paths, `C-0023`'s linear `E5`, and `E5a1` — one crossover per flexure)"*, adding that

> *"**`E5a1` is the only clearing row computed on the exact element**, and its plan view is unassessed (open item 1)."*

The challenge is to the **row**, not to the caveat. `C-0050` is scrupulous about the gap; what it cannot do is leave the count at three once the gap is closed.

---

## Ground 1 — the array places 43 of 45, and 43 is a proven maximum

`E5a1`'s hinge is **one crossover of the host sheet's own lattice**, so an arm can only be rooted where a crossover is. A row's roots are the crossovers of its **two bounding interfaces**, which carry opposite parities, so:

| | interior row | edge row |
|---|---|---|
| root sites over 40 nm | 8 | 4 |
| site pitch | **5.44 nm** (16 bp) | **10.88 nm** (32 bp) |
| an arm's demand, `arm + d` = 9.131 + 2.69 | **11.82 nm** | 11.82 nm |
| **arms carried** | **3** | **2** |

&nbsp;&nbsp;&nbsp;&nbsp;**13 × 3 + 2 × 2 = 43, against 45.**

This is not a search result that a cleverer packer could improve: the **constructive** placement (an exact per-row interval schedule with the crossovers a neighbouring row has consumed removed) reaches 43, and the per-row maxima solved **independently** — which ignore the sharing constraint and are therefore a strict upper bound — also total 43. Construction meets bound.

It holds at **every one of the 32 crossover phases** and at every one of five swept axes: `EI` at Fields et al.'s implied −25 % (43), `α = 0.6` (43), the 2.0 nm steric exclusion width instead of the 2.69 nm SAXS one (43), and the reading in which the arm's root is a **double nick as well as a crossover** (**30**).

---

## Ground 2 — and the 43 that do place leave no sheet behind, which is the ground that decides it

Two arms short of 45 would be a rounding. It is not, because **an arm is not added to the host sheet — it is cut out of it.** Its tip is a duplex end (that is what `C-0034`'s `A2` is) and its root must be doubly nicked from the rest of its own row, because a single nick is a **clamp** (`C-0025`, `CLAUDE.md`).

At the best phase, with 43 arms of 9.131 nm on the 40 × 40 nm host:

| | |
|---|---|
| host duplex length that becomes lever | **65.4 %** |
| crossovers spent as hinges | **43** |
| crossovers **buried** under an arm, and therefore deleted | **13** |
| **total demanded of an inventory of 56** | **56 — exactly 100 %** |
| crossovers surviving | **0** |
| bonded components remaining | **0** |

And the collapse is not a cliff at 43 — it is monotone and steep. Re-placing the arm at every candidate count (`L ∝ n^(1/3)`, so the arm is a **placed** quantity):

| paths | 10 | 15 | 20 | **25** | 30 | 35 | 40 | 42 | **45** |
|---|---|---|---|---|---|---|---|---|---|
| duplexes still bonded into one piece | 15 | 15 | 15 | **15** | 14 | 8 | 3 | **0** | **0** |
| crossovers surviving | 46 | 41 | 31 | **24** | 19 | 7 | 2 | **0** | **0** |

&nbsp;&nbsp;&nbsp;&nbsp;**The count that leaves the host whole is 25 — 1.80× below §3's 45, and 1.72× below the 43 the lattice would place.**

**So the threshold is not `45 → 43`; it is `45 → 25`.** `C-0040` already recorded that *"every crossover in that inventory is already a structural load path"* and that 45 hinges is 80–92 % of it; what the plan view adds is the **buried** crossovers and the **duplex length**, neither of which an inventory ledger can see.

---

## What this does NOT challenge

- **`C-0050`'s bound, and therefore its verdict on §3's desired stroke.** `s = L₀ − h < L₀ ≤ 10 nm` is a theorem about the coordinate. It contains no coupling, no element and no plan view, and nothing here touches it. If anything this strengthens it: another catalogue row leaves.
- **`C-0039`'s mechanics.** Its `E5a1` arm (9.131 nm) and assembled tangent (39.18 pN/nm) are re-derived from its own library here and reproduce to 1.7e−5 and 4.2e−5. Its elastica, its bracket finding and its desired-stroke verdict are untouched.
- **`C-0040`'s census, its fan law or its inventory.** All three reproduce exactly, and its ten best phases are recovered from a third independent construction.
- **`C-0041`'s verdict on its own element.** Its 15, its 0 of 720 and its 1 of 720 are reproduced here as a **free limiting case** of the same packer. What is shown is that its *reasons* do not transfer — `E5a1` owns no vertical member and its `arm + d` is under the column pitch — and that a different obstruction refuses the array anyway.
- **The per-path allowable, at any count.** At 45 paths `E5a1` puts 2.22 pN on a path and at 25 it puts 4.00, both far inside `C-0006`'s 10 pN unzip allowable. **Nothing here is an allowable failure.**

---

## What `C-0050` should now read

| clause | as filed | as this challenge leaves it |
|---|---|---|
| the `E5a1` row's binding constraint | *"none — every predicate clears"* | **packing and host survival** — 43 of 45 place, and the host that carries 43 is not a sheet |
| the acceptable-stroke headline | *"3 of 14 clear them all"* | **2 of 14** — `E3a` at 15 paths and `C-0023`'s linear `E5`, both of which `C-0050` itself flags as carrying idealisations it does not remove |
| *"`E5a1` is the only clearing row computed on the exact element"* | true, and favourable | **true, and now the reason the loss is expensive** |
| open item 1, *"the plan view of a 45-arm hinge-line array"* | open | **answered — negatively** |

**And the count that would clear is named**: 25 paths at a 7.236 nm arm, assembled tangent 43.18 pN/nm — 1.08× `C-0023`'s **declared** ceiling, which [`C-0049`](../claims/C-0049-compliance-ceiling-stroke.md) withdrew in the same iteration, and well inside the per-path ceiling that replaced it (83.3 pN/nm at 25 paths). Whether a 25-path `E5a1` is a design is a question for a synthesis, not for this challenge; what is established is that a **45**-path one is not.

---

## How this challenge would fail

1. **A hinge that is not a crossover of the host's own lattice.** The 5.44 nm root pitch dissolves and Ground 1 with it. But `n k_θ` **is** the crossover's constant — it is what `C-0023`, `C-0029`, `C-0034`, `C-0039` and `C-0040` all price `E5` on — so this would be a different element with a different placement.
2. **An arm that is not cut out of the host** — a duplex added *above* the sheet on an inter-layer crossover. That spares Ground 2 entirely, at the price of a **two-layer** body §3 does not describe and of `C-0009`'s and `C-0015`'s whole single-layer lattice.
3. **A larger or differently shaped host.** 45 arms place at an edge of **49.25 nm (1.23×)** or at **16 duplexes (1.07×)** — but neither is priced against Ground 2, which is a fraction of the host and does not improve with its size.
