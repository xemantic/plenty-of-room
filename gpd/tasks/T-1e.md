# T-1e — Invert `N` on the FIRST-MOMENT thickness as well as on the force-onset height

| | |
|---|---|
| **Leaf** | `A2.1` |
| **Problem definition** | §6 task 1 and task 2; §3's 5–10 nm layer-height band; questions §4(a), §4(b) |
| **Raised by** | [`CH-0010`](../challenges/CH-0010-brush-height-is-coil-height.md), *"Outstanding, and queued"* item 1; and [`C-0016`](../claims/C-0016-design-window.md), whose height-convention banner says in as many words that `T-1e` has not run |
| **Verification type** | in-silico (numerical SCF, Edwards propagator — the machinery `C-0011` built), plus two closed-form limits that need no solver |
| **Maturity** | TRL 1–3. Model-consistent and traceable. **Not measured.** |
| **Reserved IDs** | claim `C-0077`, challenges `CH-0090` and `CH-0091` |
| **Status** | Formulate + Plan written before execution. Falsifiers declared below. |

---

## Formulate

### The question, as a numeric target

`C-0011` reports `N(L₀ = 10 nm, σ = 0.024 nm⁻²) = 62.1` monomers, 2.7 kDa, against `C-0003`'s
**224.8 – 374.3** monomers, 9.9 – 16.5 kDa. It states honestly that **most** of that gap is not
physics but the definition of "layer height":

> Scaling the SCF first-moment thickness by the measured `N^(0.5–0.55)` — an **extrapolation, not a
> computed design point** — a layer whose `2⟨z⟩` is 10 nm would need `N ≈ 190–210`.

**"By scaling" is the weakness this task removes.** The SCF solves a profile, so

&nbsp;&nbsp;&nbsp;&nbsp;`⟨z⟩ = ∫ z φ(z) dz / ∫ φ(z) dz`

is a *functional of the solved profile*, and `N` can be inverted on `2⟨z⟩ = L₀` by the same
bracketed root that inverts it on the force-onset height. The two inversions then differ in the
**height functional** and in nothing else — the same discipline `T-1d` used when it made its answer
and `T-1c`'s differ in the profile and in nothing else.

What is **not** definitional is `C-0011`'s other headline: at one and the same chain, the two
trial-function models say the tile floats free above ~2.2 nm and the solved layer holds **78 pN** at
10 nm. The deliverable is to say **exactly** how much of the chain-length gap is the convention and
how much is that physics, with the residue named and its own uncertainty attached.

### Acceptance predicate

`TASKS.md` states it as:

> The definitional part of `CH-0010`'s chain-length gap separated from the physical part exactly,
> rather than by scaling.

Tightened here, and discharged only when **all six** hold:

1. `N` is inverted on `2⟨z⟩ = L₀` by a **root find on the solved profile**, with no scaling
   exponent anywhere in the path from the profile to the answer.
2. The inversion is reported **at the same states the force-onset inversion is**: the
   `C-0001`/`C-0003` design point (10 nm, `σ = 0.024 nm⁻²`), the three layer heights `T-1d` swept,
   and `T-1d`'s 61-point logarithmic grafting-density grid at 10 nm — so the two conventions are
   comparable **field by field**.
3. **All three profile models are put in the same convention.** The box has `2⟨z⟩ = L` exactly and
   strong stretching does not, so a comparison that leaves each model in its own convention is
   still comparing definitions. `N` is therefore reported for the box, for strong stretching and
   for the SCF in **both** conventions, at every state.
4. The decomposition is stated as an arithmetic identity — `gap = convention × physics` or
   `gap = convention + physics` — with **which one, and why**, decided by whether the factor or the
   difference is the stable one across the grid, and not by preference.
5. The **physical residue** is reported as its own number with its own uncertainty, and that
   uncertainty names its sources (the interaction bracket, the resting-load threshold, the grid).
6. Every claim that consumes `N(L₀)`, `2⟨z⟩`, mean `φ` or the molar mass is named, and for each the
   answer is *"the verdict moves"* or *"the verdict does not move"*, with a reason. **A moved
   verdict is a challenge, not an overwrite.**

### Units, locked

SI, scaled: lengths nm, forces pN, energies pN·nm, pressures pN/nm² (`= 1 MPa` exactly),
stiffness pN/nm (`= 1 mN/m` exactly). `k_BT = 4.141947 pN·nm` at **T = 300 K**, medium **aqueous
buffer** (2–10 mM MgCl₂, not entering this task — `C-0007` puts the layer's buffer dependence at
≤ 0.4 %). Molar mass in g/mol; `M₀(PEG) = 44.053 g/mol`.

A **volume fraction** is always the physical one, `φ(z)`, with `N σ v₀/h` its mean.

### Geometry and sign conventions, fixed before deriving

Inherited verbatim from `T-1d`, because the two studies must be comparable field by field:

- `z` normal to the electrode, positive away from it, origin at the electrode surface.
- Chains grafted at `z = 0`, one end fixed there and the other free; the layer occupies `0 < z < h`.
- The tile is a **rigid, non-adsorbing wall** at height `h`; compression means `h < L₀`.
- The propagator is **absorbing (Dirichlet)** at both the grafting surface and the tile.
- The disjoining pressure `P` is positive when the layer pushes the tile along `+z`.
- The layer height is the **independent** variable and `N` follows from it.

Two conventions are the *subject* of this task and are therefore named rather than assumed:

- **The FORCE-ONSET height.** `L₀^F` is the height at which the layer carries a stated resting
  load — 1 pN over the 40 × 40 nm tile, `T-1d`'s primary threshold, with 0.1 and 10 pN carried.
  This is the height the **tile occupies**.
- **The FIRST-MOMENT thickness.** `L₀^M = 2⟨z⟩`, the first moment of the *solved* profile, measured
  **on the profile at that chain's own force-onset resting height** — which is the definition
  `C-0011` already emits as `firstMomentHeight`, so the two studies read the same functional. It is
  exactly `L` for a box profile, which is what makes it the convention under which an Alexander box
  is quoting its own height honestly.

The threshold dependence of `L₀^M` is **measured rather than assumed away**: the wall sits at a
threshold-defined height, so `2⟨z⟩` inherits some of that dependence, and how much is one of the
deliverables.

### What is deliberately excluded

No electrostatics, no ion partitioning, no poroelasticity, no tile compliance, no re-derivation of
the interaction free energy. This task changes **one functional** and nothing else.

---

## Plan

### The cheap bound, before any solver runs

`C-0011`'s emitted file already carries enough to bound the definitional share **three ways**, and
all three cost one pass over `gpd/results/T-1d-scf-density-profile.json`.

**(a) The scaling estimate as `C-0011` wrote it.** `L₀^F/2⟨z⟩ = 1.8319` at the design point, so
`N_M = N_F · 1.8319^(1/p)`. With `C-0011`'s quoted `p = 0.50–0.55` this is **186.7 – 208.4** for the
des Cloizeaux interaction and **190.9 – 218.6** across the three interactions — which is the
`≈ 190–210` in print.

**(b) The same estimate at the claim's OWN measured exponent band.** `C-0011` states the exponent of
`L₀` in `N` as **0.49 – 0.64**, not 0.50 – 0.55. Evaluated over the band the claim itself publishes,
the same formula gives **159.9 – 218.6**, i.e. a ±16 % spread rather than the ±5 % the quoted
`190–210` implies. The narrower band has no stated derivation.

**(c) The exponent read off `T-1d`'s own three heights, which is the right one.** At
`σ = 0.0240225 nm⁻²` the file carries `(L₀^F, N, 2⟨z⟩)` at 5, 7 and 10 nm:

| `L₀^F` | `N` | `2⟨z⟩` | `L₀^F/2⟨z⟩` |
|---|---|---|---|
| 5 nm | 13.834 | 2.5363 nm | 1.9714 |
| 7 nm | 28.992 | 3.6736 nm | 1.9055 |
| 10 nm | 62.094 | 5.4588 nm | 1.8319 |

from which `d ln 2⟨z⟩/d ln N` = **0.5007** (5→7), **0.5200** (7→10) — while `d ln L₀^F/d ln N` over
the *same* pairs is **0.4548** and **0.4683**. **They are different exponents**, by 10 %, and
`C-0011`'s formula uses the second where it needs the first. Extrapolating on the correct one gives
`N_M` = **198.9 – 208.0**, 8.76 – 9.16 kDa.

**So the cheap bound is worth `N_M ≈ 190 – 210` and it is worth that by luck**: two errors — the
wrong exponent (10 % low) and a narrowed band — offset. Three things it still cannot say, and they
are why the exact inversion is bought:

1. **The exponent is drifting and the extrapolation is 3.2× in `N`.** It runs 0.5007 → 0.5105 →
   0.5200 over the three pairs, i.e. `dp/d lnN ≈ +0.026`; carried to `N ≈ 200` that is `p ≈ 0.545`
   and `N_M ≈ 189`. The cheap bound's own drift is 189 – 208, ±5 %, and the physical residue it is
   the denominator of is only ~1.4×, so ±5 % of the denominator is ~12 % of the effect being
   measured. **A separation quoted to one significant figure is not a separation.**
2. **The shape ratio `L₀^F/2⟨z⟩` is not a constant of the layer** — it runs **1.372 → 2.069** across
   `T-1d`'s own 10 nm grid. A single-point scaling therefore cannot be transferred to the window
   edges, which is exactly where `C-0016` needs it.
3. **Nothing in the cheap bound says how threshold-dependent `2⟨z⟩` is.** The force-onset convention
   is a 2.5× family in `N` over two decades of the defining load (`C-0011`); whether the
   first-moment convention is or is not is the whole of deliverable 5, and no scaling can answer it.

### The method, and why this one

The machinery exists and is not rewritten. `ScfProfile.firstMomentHeight` already computes
`2⟨z⟩`; `ScfDensityProfileStudy` already computes the analytic models' `2⟨z⟩`. What is missing is
**one root find**, and it is added in a **new file** so that no standing result is disturbed:

- `src/main/kotlin/brush/FirstMomentThickness.kt` — `restingFirstMomentThickness(chain)` for any
  `GraftedLayerModel`, and `chainLengthForFirstMomentThickness(peg, thickness, σ)`, a bracketed
  root in `ln N` against `ln 2⟨z⟩` for exactly the reason `C-0011` bracketed the force-onset
  inversion: the relation is very nearly a straight line in log-log and the fixed point
  `N ← N·target/achieved` contracts by only a factor of two per pass when `L₀ ∝ N^0.5`.
- `src/main/kotlin/brush/FirstMomentConventionStudy.kt` — the study entry point, emitting
  `gpd/results/T-1e-first-moment-convention.json`.

`ScfDensityProfileStudy.kt` and `SelfConsistentField.kt` are **not edited**, and a test asserts that
the new accessor reproduces `ScfProfile.firstMomentHeight` at **departure 0.0** and reproduces
`T-1d`'s emitted `firstMomentHeight` at the design point within the emission precision.

| method | cost | role |
|---|---|---|
| read `T-1d`'s file, three scalings | milliseconds | the cheap bound above — **run before this Plan was written** |
| box `2⟨z⟩ = L` and the strong-stretching Beta ratio | closed form | the two analytic models' conventions, exactly, no solver |
| **this: bracketed `N` on the solved `2⟨z⟩`** | ~6–10 resting-height solves per state | the calculation the cheap bound cannot replace |
| a re-run of the whole `T-1d` sweep in the new convention | 33 min × the `N ≈ 3×` cost penalty | **not bought** — the deliverable is a *separation*, not a second window |

**What the grid can afford.** One first-moment inversion is an outer bracket in `ln N` whose every
evaluation is a whole force-onset resting height — so it is roughly the cost of `T-1d`'s own
inversion times the number of outer steps, and the chains are ~3× longer and the layers ~1.8×
taller, which costs another ~4–6× per solve. The affordable sweep is therefore **one interaction law
across the full 61-point grid at 10 nm**, plus **all three interaction laws at the design point and
at the three layer heights**, plus the threshold and grid sensitivities. The cost is profiled on a
single point **before** the sweep is launched and the grid is chosen from the profile, per
`CLAUDE.md`'s *"measure the cost of the inner loop before choosing the budget of the outer one"*.

### Where this can go quietly wrong, and what is done about each

**`M = round(h/Δz)` makes `2⟨z⟩` a discontinuous function of the wall height** (`C-0073`), and the
wall height here is itself solved. The first moment is a *ratio* of two quadratures over the same
nodes, so the leading node-count effect cancels — but that is a hope, not a proof, so the
convergence gate measures `2⟨z⟩` **and the inverted `N`** in their own right at `Δz` = 0.4, 0.2,
0.1 nm. `CLAUDE.md`: *"an SCF window edge is not grid-converged where a stiffness is; convergence is
a property of the quantity."* A first moment is a different quantity from a contact pressure and
gets its own order.

**The emission precision.** `brush/` emits at six significant digits via
`SOLVED_HEIGHT_SIGNIFICANT_DIGITS`. This study's numbers are downstream of **two** nested `1e-6`
brackets, so the determined precision is measured rather than assumed, and no test asserts tighter
than the emission slack.

**The saturation floor is slower than the layer** (`CLAUDE.md`). The outer bracket in `ln N` walks
**up** from a seed rather than down, so no evaluation is taken at a height where the mean volume
fraction approaches `SATURATION_FRACTION`.

**A ratio of two quadratures is scale-free**, so a wrong overall normalisation of `φ` is invisible in
`2⟨z⟩`. The conservation gate therefore asserts the normalisation separately (`∫φ dz = N σ v₀`) and
asserts that `2⟨z⟩` is *invariant* under scaling `φ`, which is the property that hides it.

### What would falsify this approach

Stated in advance, before any code was written:

1. **The exact inversion landing inside the `190 – 210` `C-0011` quotes.** Then the scaling was
   sufficient, the task's premise — that *"by scaling"* is a weakness — fails at the design point,
   and the deliverable is a confirmation rather than a separation. **This one is expected to be
   close**; the cheap bound above puts the answer at 189 – 208, so it is declared honestly and it is
   the sweep and the threshold sensitivity, not the design point, that must then carry the task.
2. **The decomposition not being a decomposition.** If neither the ratio nor the difference of the
   convention factor is stable across the grid, then `gap = convention × physics` is a per-point
   coincidence and the task must report per-point numbers and say that no separation exists.
3. **`2⟨z⟩` being as threshold-dependent as the force-onset height.** `C-0011` measures the
   force-onset convention as a 2.5× family in `N` over two decades of the defining load. If the
   first-moment convention is the same, then it is not the better-posed quantity and deliverable 5's
   recommendation cannot be made in its favour.
4. **The physical residue coming out at or below one.** The whole shape of `CH-0010` is that the
   solved layer needs a *shorter* chain than the trial functions for the same physical thickness. A
   residue at or below one would say the trial functions were right about the chain all along and
   the 78 pN is a separate finding with no chain-length consequence at all.
5. **The new accessor failing to reproduce `T-1d`'s emitted `firstMomentHeight`.** Then the two
   studies are not reading the same functional and nothing here is comparable to `C-0011`.

Outcomes are recorded in the **Execute** and **Result** sections below, after the run.

---

## Execute

Code, in the `brush` package, **tests written first**:

- `src/test/kotlin/brush/FirstMomentThicknessTest.kt` — the five gates.
- `src/main/kotlin/brush/FirstMomentThickness.kt` — the accessor and the inversion.
- `src/main/kotlin/brush/FirstMomentConventionStudy.kt` — the study entry point.

```shell
./gradlew test -PbuildDirectory=build-t1e
tools/study.sh brush.FirstMomentConventionStudyKt
```

Result: [`../results/T-1e-first-moment-convention.json`](../results/T-1e-first-moment-convention.json).

---

## Verify

All five gates, as executable tests, in `src/test/kotlin/brush/FirstMomentThicknessTest.kt` (13 tests,
each named for its gate). Full detail in [`C-0077`](../claims/C-0077-first-moment-chain-length.md);
in summary:

1. **Dimensional** — `2⟨z⟩` is `nm⁴/nm³`; a box's is its own height to `1e-14` over 9 states; the
   inverted `N` reproduces its target to `1e-5` for all three models.
2. **Limiting cases** — the box's two inversions are one inversion over 61 densities × 3 heights;
   strong stretching reproduces its closed-form Beta ratio `1/[(p+1)B(p)]`, 0.75 exactly at `m = 2`
   and 0.783596 at `m = 9/4`, to `1e-6`.
3. **Conservation** — departure exactly `0.0` against `ScfProfile.firstMomentHeight`; the ratio is
   scale-free over a 9× change in chain length, and the normalisation it is therefore blind to is
   asserted separately at `1e-9`.
4. **Convergence** — measured in its own right, and it is **not** second order: `2⟨z⟩` at **1.59**
   and the inverted `N` at **1.11** in the node spacing, against the pressure's 2.08–2.32. The
   production grid carries `7.2e−4` and `4.6e−3` respectively.
5. **Literature** — the Beta ratio; `C-0011`'s emitted `firstMomentHeight` reproduced from the
   committed file within the emission slack `5e-5`.

### Outcome of the declared falsifiers

- **(1) did NOT fire**, and it was the one expected to. The exact inversion gives **175.08** monomers
  (7.713 kDa), **outside** `C-0011`'s `190–210`; every reading at a measured exponent overstates it
  by 13.6–34.3 %, because the exponent drifts upward with `N` over an extrapolation of 2.8× in `N`.
- **(2) did not fire.** The convention **factor** spreads 1.460× across the 10 nm grid and the
  **difference** 3.742×, so the product form is the transferable one, by 2.563×.
- **(3) did not fire.** Over two decades of the defining load the force-onset chain moves **2.494×**
  and the first-moment chain **1.103×** — a factor of 9.31 in logarithmic sensitivity.
- **(4) did not fire.** The physical residue is **1.636–1.648** at des Cloizeaux and 1.64–1.95 over
  the interaction bracket.
- **(5) did not fire.** Departure `0.0`.

A sixth thing emerged that was not stated in advance, and it is the one that changed the shape of the
answer: **the decomposition has three factors, not two.** The trial functions carry a convention
factor of their own — 1 exactly for the box, `1.27616` for strong stretching — so `CH-0010`'s
comparison looked like one convention because it compared two models each read in its own.

---

## Result

Filed as [`C-0077`](../claims/C-0077-first-moment-chain-length.md), raising
[`CH-0090`](../challenges/CH-0090-the-scaling-estimate-uses-the-exponent-of-a-different-quantity.md)
against `C-0011`/`CH-0010` and `C-0003`, and
[`CH-0091`](../challenges/CH-0091-a-first-moment-ten-nanometre-layer-is-not-a-ten-nanometre-layer.md)
against `C-0016`. **No window edge, no stroke, no stiffness and no coupling verdict moves.**
