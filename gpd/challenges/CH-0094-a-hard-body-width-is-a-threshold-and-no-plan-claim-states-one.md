# CH-0094 — A hard-body exclusion width between two **unbonded** duplexes is not a separation but a **threshold on an energy**, because the pair has **no equilibrium separation at all**; the map runs **11.45 nm at 0.5 `k_BT` to ≤ 2.1 nm at 8 `k_BT`** and straddles the placement threshold, so **every plan claim in this branch states a width and none states the state it was read at** — the eighth instance of this project's own discipline, and the first where the missing state is an energy budget

| | |
|---|---|
| **Against** | [`C-0041`](../claims/C-0041-flexure-array-packing.md), [`C-0053`](../claims/C-0053-hinge-arm-array-packing.md), [`C-0065`](../claims/C-0065-crossbar-array-placement.md), [`C-0066`](../claims/C-0066-arm-slab-tie-clearance.md), [`C-0069`](../claims/C-0069-output-element-placement.md) and [`C-0076`](../claims/C-0076-weave-exclusion-width.md) — every claim in the branch that writes *"a duplex in plan is a rectangle of width `d`"* |
| **Raised by** | [`C-0079`](../claims/C-0079-unbonded-duplex-separation.md), task [`T-139`](../tasks/T-139.md) |
| **Grounds** | **methodological** — a quantity quoted without the state it is read at, which is this programme's own recurring failure mode; here the missing state is an **energy budget** rather than a load, a compression, a bandwidth, a loading rate or a lattice coordinate |
| **Status** | **STANDS as a statement about what a plan width IS.** It does **not** overturn any placed count: `C-0079` finds **34 of 34** at every physically calibrated reading. What it removes is the licence to quote a plan margin without naming the budget it assumes |

---

## The discipline this challenge belongs to

`CLAUDE.md` already carries seven instances:

> *"quote a stiffness with the compression it was read at"*, *"a variance with its bandwidth"*, *"a rupture force with its loading rate"*, *"`k_es` with its gap"*, *"a flatness count with its load case"*, *"a placement with the operating state it was read at"*, *"a misalignment floor with the lattice coordinate it was read at"*.

**This is the eighth, and the missing state is an ENERGY BUDGET.**

## The challenge

### Ground 1 — the pair has no equilibrium, so a width cannot be a separation

`C-0079` establishes, on four independent methods every one read directly, that two unbonded parallel B-DNA duplexes in millimolar Mg²⁺ **hold no separation at all**:

> *"In Mg²⁺-only solutions in which DNA-DNA interaction is always repulsive, the force-spacing curve extends to infinity because zero force can only be achieved at infinite DNA-DNA spacing."*
> — Meng, Timsina, Bull, Andresen & Qiu, *Biophys. J.* **118**:3019 (2020), **READ DIRECTLY**

The interaction is monotone repulsive over the entire plan-relevant range;
the only stationary point outside it is a **0.006 `k_BT`** far minimum at 37 nm, 170× below thermal and an unretarded-Lifshitz artefact.

**A hard-body width is therefore an approximation of a SOFT repulsion, and a soft repulsion has an edge only once a threshold is named.**

### Ground 2 — the map, and it straddles the placement threshold

| threshold | width [nm] | places of 34 |
|---|---|---|
| 0.5 `k_BT` | **11.45045** | 22 |
| **1 `k_BT`** — Barker-Henderson, the criterion for two **free** bodies | **8.78601** | 22 |
| 2 `k_BT` | **6.08670** | 22 |
| 5 `k_BT` | **2.69385** | **34** |
| **7.99970 `k_BT`** — the host sheet's own per-crossover energy at 2.69 nm | **≤ 2.1** | **34** |
| 10 `k_BT` | **≤ 2.1** | **34** |

**The width moves 3.93 nm per e-fold of the threshold at the loose end** (the Debye length) **and 0.24 nm per e-fold at the tight end** (the measured short-range decay).
A one-decade uncertainty in an unstated budget is therefore worth **several nanometres**, against plan margins the branch quotes to **0.0256 nm**.

### Ground 3 — the standing 2.69 nm IS a threshold, and it is 5 `k_BT`

The convention no claim derives turns out to correspond to a crossed-geometry pair energy of **5.00968 `k_BT`**,
and the 5 `k_BT` threshold reproduces it to **0.14 %**.
That is a fact about the convention, not a justification of it: `C-0041` chose 2.69 nm as *"the loosest defensible choice"* from a **Bragg lattice constant of a crossover-BONDED pair** (`C-0076`), and the coincidence was not available to it.

### Ground 4 — the budget IS nameable, and the branch should name it

`C-0079` supplies one: **the host sheet is a measured object**, and holding two of its own duplexes at the SAXS 2.69 nm costs **31.9988 `k_BT`** per 40 nm interface, i.e. **7.99970 `k_BT`** per crossover column. At that budget the affordable width is at or below the continuum model's own 2.1 nm floor.

**A thermal criterion is the wrong one here** and it is worth the whole verdict: both bodies are covalently rooted to the same sheet, so the energy is paid by the fold and not by `k_BT`. Barker-Henderson's effective hard-body diameter is the criterion for two **free** bodies colliding in solution.

**But that is an argument, not a convention**, and no plan claim in this branch makes it.

---

## What this challenge does NOT assert

**It does not move a placed count.** `C-0079` finds **34 of 34** at every width from 0.51108 nm to 2.71561 nm, and the only readings that give 22 are a lattice constant measured on a different lattice and a thermal criterion written for unattached bodies.

**It does not withdraw the 2.69 nm convention.** It reclassifies it: 2.69 nm is a defensible *conservative* plan width whose implied budget is 5 `k_BT`, and it should be quoted that way.

**It does not repair `C-0072`'s knife edge.** It supplies a second, independent reason the edge is not quotable: 0.0256 nm at 2.7 nm is **1.2373 %** of the pair energy, so the step is below the resolution of the physics as well as of the design language.

## What each claim should carry

| claim | what to add |
|---|---|
| **`C-0041`** | the width is a threshold; Fact B (2.59×) is insensitive to it and Fact A's tilt threshold is not |
| **`C-0053`** | 43 of 45 holds at every width in the bracket, so the budget is not binding there — say so |
| **`C-0065`** | the trio census is a *register* result and no width moves it |
| **`C-0066`** | bound 4 is the **crossed** geometry and its body's cost at the threshold is **4.94674 `k_BT`**, 1.62× below what one host crossover already pays |
| **`C-0069`** | `Q5`'s margin should be quoted with the budget, and the slot is **coaxial** ([`CH-0093`](CH-0093-the-collinear-clearance-is-a-stacking-allowance-not-an-exclusion.md)) |
| **`C-0076`** | its *"the width in that role is unmeasured"* is right and incomplete: the width is not a *measurable* quantity at all in the sense it assumed, because the pair has no equilibrium |

## How this challenge would fail

1. **A measured equilibrium separation for two unbonded duplexes** — then the width is a separation after all and no threshold is needed. `C-0079`'s literature survey (25 recorded queries) finds none at any Mg²⁺ concentration for random-sequence B-DNA.
2. **A demonstration that the placement verdict is insensitive to the width over the whole defensible range** — it is not: the step at 2.71561 nm takes 34 to 22.
3. **A plan claim that already states its budget.** None does; the phrase in all six is *"a duplex in plan is a rectangle of width `d = 2.69 nm` (SAXS)"*, which names a measurement of a different quantity and no threshold.
