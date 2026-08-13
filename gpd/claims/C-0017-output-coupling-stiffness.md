# C-0017 — The output-coupling stiffness is fixed by §3 at 33.333 pN/nm, it stabilises the operating point at every height, and a 45-attachment ssDNA-tuned coupling supplies it

| | |
|---|---|
| **Task** | [`T-16`](../tasks/T-16-output-coupling-stiffness.md) |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A2.2` (the operating point the budget is written at) and `A1.1`/`A1.2` (the lateral bound the same anchors also satisfy) |
| **Verification type** | **in-silico** (`C-0012`'s coupled force balance re-solved against a *load line* rather than against zero, at a bias located by bisection rather than read off a grid) **+ logical** (a load-line argument that fixes the required stiffness from §3 alone, before any solve) |
| **Verdict** | **PASS on `P1`, `P2`, `P3` and `P4`, and the direction is favourable — but declared falsifiers 4 and 5 both fired.** The margin at the worst point is 1.19×, against an inherited mean-field error of 123–214 %. The verdict is **NOT EXCLUDED**, never established. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED.** No coupling below has been built and none is proposed as a sequence design. |
| **Provenance** | `gpd/results/T-16-output-coupling-stiffness.json`, produced by `coupling.OutputCouplingStudyKt`; 54 requirement records, 324 scheme records, 36 upstream reproductions, 9 spacer designs, 6 lever budgets, 6 convergence records; **39 gate-named `coupling` tests, 685 in the suite, 0 failures**; the result file re-run and diffed **byte-for-byte identical** |
| **Conditions** | T = 300 K, `k_BT = 4.142 pN·nm`, `l_B = 0.7141 nm`, `ε_r = 78`; aqueous `MgCl₂` at 0.5 / 1 / 2 mM; 40 × 40 nm Manning-renormalised tile; PEG layer 5 / 7 / 10 nm at `σ` = 0.092 / 0.045 / 0.024 nm⁻²; all six `C-0003` models |
| **Consumes** | [`C-0012`](C-0012-coupled-stroke-and-blocking-force.md) (the characteristic, **re-run not tabulated**), [`C-0014`](C-0014-lateral-confinement.md) (the element mechanics, the convexity theorem, the lateral and yaw bounds), [`C-0015`](C-0015-crossover-phase-and-registration.md) (45 attachments as 3 × 15, the exact-zero load path), [`C-0009`](C-0009-discrete-lattice-tile.md) (the concentration factor, the 56 crossovers), [`C-0006`](C-0006-tile-load-distribution-and-flatness.md) (the per-path allowables), [`C-0003`](C-0003-crossover-valid-layer-response.md)/[`C-0008`](C-0008-electrostatic-force-and-decay-length.md) (the layer and the field, as libraries) |
| **Raises** | [`CH-0016`](../challenges/CH-0016-coupling-requirement-is-quoted-off-operating-point.md) against `C-0012` |
| **Challenged by** | [`CH-0033`](../challenges/CH-0033-thermal-excitation-is-not-a-load-non-uniformity.md), from [`C-0026`](C-0026-one-row-per-duplex.md), on failure route 2 only. **No number or verdict moves** — see the banner below |
| **Re-run by** | [`C-0027`](C-0027-window-resynthesis.md) (`T-25`) — **the margin improves** |

> ⚠️ **Re-run against iteration 4 by [`C-0027`](C-0027-window-resynthesis.md) (2026-08-13). The verdict stands
> and the margin is BETTER than published.**
>
> `C-0019` alone degrades the 10 nm / 2 mM margin to **1.11–1.25×** and `C-0022`/`CH-0026` alone restores it to
> **1.34–1.67×**; carried together the margin is **1.231–1.528×** against the 1.194–1.424× below. **The two
> corrections are the same size and opposite sign**, so `C-0019`'s "≥ 1.07×" was one half of a two-sided
> correction. `0` of the 54 states fails the mandate, before or after. **`P4` and every scheme verdict are
> untouched**; open questions 3, 4 and 5 are discharged by `C-0026`, `CH-0033` and `C-0023` respectively.

> ⚠️ **Failure route 2 of this claim is withdrawn by [`CH-0033`](../challenges/CH-0033-thermal-excitation-is-not-a-load-non-uniformity.md) (2026-08-13), from [`C-0026`](C-0026-one-row-per-duplex.md) (`T-17`).**
>
> **No number, table or verdict below changes, and `P4` stands as written with a stronger warrant than it had.**
> The route *"`T-17` finding that a real, non-uniform load restores a per-path crossover force large enough to matter,
> which would put `K2` back inside `C-0009`'s 2.3–7.6× concentration and take its 2.22 pN to 5.1–16.9 pN"* **cannot
> occur**: a concentration factor multiplies the force that *crosses an interface*, not the 2.22 pN share that *enters
> at an attachment*. Solved, the restored interface force is **0.239 pN**, concentrated **2.52–3.49×** onto four
> crossovers, i.e. **0.150 pN** — and `K2`'s per-path peak stays at its per-path static, 2.222 pN.
> Open questions 3 and 4 are discharged; `T-9` remains a bracket on the *thermal* channel only.

---

## THE CONVENTIONS — read these before any number below

- `z` is normal to the electrode, positive **away** from it; the electrode surface is `z = 0`.
- **The electrostatic gap is the layer height, exactly** (`C-0012`'s convention, unchanged).
- The **stroke** `s = L₀ − h` is positive **downward**, toward the electrode.
- **`L₀` is a FORCE-ONSET height** — the height at which the layer carries 1.0 pN over the 40 × 40 nm tile (`C-0011`, `CH-0010`). The first-moment thickness of the same layer is 1.71–2.16× smaller.
- The actuator's **characteristic** is `W(s) = |F_es(L₀−s, V)| − P(L₀−s)·A`, and `dW/ds = −k_eff` exactly.
- The **coupling reaction** `R(s)` is positive **upward**, i.e. resisting descent. **A coupling supplies stabilising stiffness only through `dR/ds > 0`; an element that goes slack as the tile descends supplies exactly nothing.**

---

## The claim, in one line

**The output-coupling requirement has two conditions, not one, and §3 fixes the binding one at `100 pN / 3 nm = 33.333… pN/nm` by arithmetic. Read at the bias where the device actually delivers §3's two targets together, the *stability* condition it has to clear is `0` at 5 nm and `0` at 7 nm — at every buffer and under all six layer models — and `3.86 – 27.91 pN/nm` at 10 nm, always below 33.333. So §3's own mandated coupling both places the operating point at 3 nm and stabilises it, everywhere in the box, and the question the programme was blocked on inverts: not *can a DNA lever be stiff enough* — forty-five duplexes in tension are 4950 pN/nm, 149× too stiff — but **can it be made compliant enough**, and the answer is a tuned 10–19 nt ssDNA spacer in series with each of `C-0015`'s own 45 flatness attachments.**

---

## `P1` — the requirement, at the bias the device actually uses

### The cheap bound, run before any code, and it is the binding half

The force delivered *to the load* between the unbiased and the biased state is `R(s₁) − R(s₀) = k_c(s₁ − s₀)`,
**independent of the preload `R₀`**. Equivalently: an unpreloaded linear coupling whose operating point sits at
§3's 3 nm while the actuator delivers §3's 100 pN there is the chord of the characteristic through the origin,
`W(s*)/s*`. Both readings give the same number:

> &nbsp;&nbsp;&nbsp;&nbsp;**`k_c* = F_target/δ_target = 100 pN / 3 nm = 33.333… pN/nm`, exactly, preload-free, and with no physics in it at all.**

This is a *dimensional* statement, not a model result, and it is **the whole of the placement condition**.
It is checked, not assumed: the study solves `W(s) = 33.333 s` as a **root** at all 54 states and gets
`s = 3.000000 nm` (spread `2.999984 – 3.000001` over the 54, the bisection's own floor), and the
`unpreloadedPlacementStiffness` it reads back off the characteristic is `33.3331 – 33.3334` at every one.

### The stability condition, read where `C-0012` never read it

`C-0012` quotes `|k_eff|` at 0.10 V and 0.25 V. **Neither is an operating bias.** The bias at which
`W(3 nm) = 100 pN` is located here by bisection, not interpolated across a grid, and the requirement is read there.

2 mM `MgCl₂`, six-model bracket, held gap `L₀ − 3 nm`:

| `L₀` | held gap | `V*` for `W(3 nm) = 100 pN` | `k_brush` | `k_es` | `k_eff` | **stability floor** | `k_c*/floor` |
|---|---|---|---|---|---|---|---|
| **5 nm** | 2.00 nm | 0.128 – 0.349 V | 717 – 4173 | −1375 – −279 | **+438 – +2799** | **0 — stable** | ∞ |
| **7 nm** | 4.00 nm | 0.083 – 0.157 V | 71.6 – 262.6 | −162.2 – −64.2 | **+7.4 – +100.4** | **0 — stable** | ∞ |
| **10 nm** | 7.00 nm | 0.128 – 0.180 V | 11.7 – 35.6 | −59.6 – −38.7 | **−27.9 – −23.4** | **23.41 – 27.91 pN/nm** | **1.19 – 1.42** |

(pN/nm.) **`0` of the 18 states at 2 mM fails §3's own mandated stiffness, and `0` of all 54.**

### The buffer is the design lever, and leaf `A2.2` is vindicated a third time

The floor at 10 nm is a strong function of the buffer, because the bias needed to reach 100 pN rises with salt
and the force's decay length shortens with it:

| 10 nm | `V*` | `ℓ = |F_es|/|k_es|` | stability floor | margin `k_c*/floor` |
|---|---|---|---|---|
| **0.5 mM** | 0.087 – 0.115 V | 4.15 – 4.20 nm | **3.86 – 15.94 pN/nm** | **2.09 – 8.65×** |
| 1 mM | 0.101 – 0.134 V | 3.55 – 3.73 nm | 10.42 – 19.36 | 1.72 – 3.20× |
| **2 mM** | 0.128 – 0.180 V | 2.77 – 2.99 nm | **23.41 – 27.91** | **1.19 – 1.42×** |

> **Dropping from §3's 2 mM to leaf `A2.2`'s low-screening point buys a factor of 6 in stability margin at the
> 10 nm design point, at no cost in stroke** (`C-0012`: the layer's mechanics are buffer-independent to ≤ 0.4 %).
> This is the third independent route to `A2.2`'s condition — `C-0012` found it on the force clause, `C-0016`
> on Reading A of the bias window, and `T-16` finds it on static stability.

### The closed form the solve is graded against

At the held gap `g`, once §3's force target is imposed, `|F_es(g,V*)| = 100 + P(g)A` is fixed, so

&nbsp;&nbsp;&nbsp;&nbsp;`k_c* − |k_eff(g)| = k_c* + k_brush(g) − |F_es(g,V*)|/ℓ(g,V*)`

and the whole margin depends on the bias only through the decay length `ℓ`. Reproduced at all 54 states to
**exactly zero** relative departure — **and that is a tautology, not an independent check**: `ElectrostaticForceCurve`
defines `k_es = |F| d ln|F|/dh` and `ℓ = −1/(d ln|F|/dh)`, so `|F|/ℓ = −k_es` identically in floating point.
It verifies that the two accessors are sign-consistent and nothing more. **It is reported here as an identity
because reporting it as a numerical agreement would be the kind of unearned `PASS` §7 evaluates.**

---

## `P2` — the supply: six schemes, one survives, and the survivor is `C-0015`'s own attachment grid

Each scheme is `n` parallel load paths, each a series chain, evaluated at all 54 solved states — 324 records.
The stroke is a **root** of `W(s) = R(s)` at every one, never a force over a stiffness.

| | scheme | `n` | `k_c` at the working point | delivered stroke | per-path peak force | verdict |
|---|---|---|---|---|---|---|
| **K1** | 45 duplex standoffs (5 nm), no spacer | 45 | **9900 pN/nm** | **0.005 nm** | 2.22 pN | **FAIL — too stiff**, 297× the mandate |
| **K2** | **45 (duplex standoff + tuned ssDNA spacer)** | **45** | **39.01 pN/nm** | **3.000 nm** | **2.22 pN** | **PASS — at all 54 states** |
| **K3** | 8 ssDNA tethers through the layer (`C-0014`'s `S3`) | 8 | 0.954 pN/nm | 7.79 nm | 95 pN | **FAIL — unstable**, and past the 65 pN ceiling |
| **K4** | 4 in-plane 40 nm tangential tethers (`C-0014`'s `S4`) | 4 | **0.0431 pN/nm** | 7.81 nm | 190 pN | **FAIL — unstable** |
| **K5** | 4 vertical duplex struts (`C-0014`'s `S1`) | 4 | 440 pN/nm | 0.113 nm | 190 pN | **FAIL — too stiff**, and past the ceiling |
| **K6** | one concentrated lever attachment | 1 | 73.3 pN/nm | 0.816 nm | **760 pN** | **FAIL — too stiff**, and 11.7× the ceiling |

**Every one of the six fails or passes identically at all 54 states** — the scheme verdicts are not
model-dependent, buffer-dependent or height-dependent.

### `K2`, stated as a design

| quantity | value |
|---|---|
| attachments | **45, as 3 × 15 — `C-0015`'s own flatness grid, one row per duplex**, against `C-0009`'s 56 crossovers |
| one path | 5 nm hybridised duplex standoff (axial, `S/L` = 220 pN/nm) **in series with** a tuned ssDNA spacer |
| **spacer contour** | **8.61 nm = 13.2 nt** at `b = 2.10 nm`; **6.74 nm = 10.4 nt** at `b = 2.84`; **12.30 nm = 18.9 nt** at `b = 1.41` |
| **dominant compliance term** | **the ssDNA spacer — 99.6 % of the path's compliance.** Leaf `A8.2`'s question, answered |
| **secant at 3 nm** | **33.333 pN/nm — places the operating point at 3.000 nm exactly** |
| **tangent at 3 nm** | **39.010 pN/nm — clears the worst stability floor (27.91) by 1.40×** |
| delivered force | **99.9994 – 100.0001 pN** across all 54 states |
| per-path static force | **2.222 pN** — 4.5× below `C-0006`'s 10 pN unzip allowable, 21.6× below shear |
| per-path thermal force | **0.283 pN** (`√(k_BT k)/n`) |
| lateral by-product | **32.36 pN/nm = 70.3×** `C-0014`'s 0.4602 pN/nm bound |
| yaw by-product | **8205 pN·nm/rad = 22.3×** `C-0014`'s 368.173 bound |

### Why the *nonlinear* coupling is the one that works, and it is a theorem

A linear coupling has secant = tangent, so it discharges placement and stability with **one** number and its
margin is `k_c*/floor` = 1.19–1.42× at the worst point. A **convex** (strain-stiffening) element has
tangent > secant, and the two conditions are read off the two different slopes:

> **The placement condition is written on the coupling's SECANT and the stability condition on its TANGENT.
> A strain-stiffening coupling therefore places with the smaller number and stabilises with the larger one,
> and the whole `tangent/secant` ratio — 1.17 for this spacer — is free stability margin at zero placement cost.**

This is `C-0014`'s convexity theorem read in the other direction, and it is the same structural fact
`CLAUDE.md` already records for the stroke and the noise (*"the secant sets the stroke, the tangent at the
working point sets the fluctuation"*). **It is what makes the ssDNA spacer, and not the duplex, the element
that closes this task.**

### The stroke it costs

`K2` places the tile at 3.000 nm against a **free** stroke at the same bias of 3.03–3.19 nm (5 nm),
3.52–4.62 nm (7 nm) and **6.06–8.57 nm (10 nm)**. So the coupling removes **1–6 % / 15–35 % / 51–65 %** of the
free stroke — but it removes it **by converting it into the 100 pN §3 asks for**, which is the transaction the
device exists to perform, not a loss. Against §3's *acceptable* 3 nm stroke the cost is zero by construction.
Against §3's *desired* 10 nm stroke, `C-0016`'s verdict — unreachable at every height and every grafting
density — is untouched.

### The lever, as a section requirement rather than an invented geometry

No lever geometry is specified in §1 or §3, so what is budgeted is the **bending rigidity a beam of a given
span needs to present 33.333 pN/nm at the tile**, under three end conditions spanning 25.6×:

| support | span | `EI` required | duplex layers | block thickness | fits inside the tile's 10 nm? |
|---|---|---|---|---|---|
| simply supported, UDL | 40 nm | **2.78e4 pN·nm²** | **2** | **5.38 nm** | **yes** |
| simply supported, UDL | 60 nm | 9.38e4 | 3 | 8.07 nm | yes |
| cantilever, UDL | 40 nm | 2.67e5 | 4 | 10.76 nm | no |
| cantilever, tip load | 40 nm | 7.11e5 | 5 | 13.45 nm | no |
| cantilever, tip load | 60 nm | 2.40e6 | 7 | 18.83 nm | no |

A four-helix bundle (`EI` = 8880 pN·nm², `C-0014`'s parallel-axis figure) presents **0.12–10.66 pN/nm**
depending on span and end condition — i.e. **the end condition alone decides whether the simplest origami
beam is 3× too soft or 3× too stiff**, and it is the largest single spread in this task after the mean field.
**The honest budget is therefore "two to seven duplex layers", and a simply-supported lever is the only one of
the three that fits inside the tile's own thickness.**

---

## `P3` — normal stabilisation and lateral confinement want the SAME anchors, and the reason is a ratio

The expected answer was *opposite*, and one half of it is true:

- **`C-0014`'s winner supplies essentially no normal stiffness.** The four in-plane tangential tethers that
  won `T-12` by 119.6× laterally contribute `k_norm` = **0.043–0.173 pN/nm — 0.13 – 0.52 %** of the 33.333
  this task requires, and as a coupling (`K4`) they fail stability outright at 10 nm.
- **`C-0014`'s worst failure over-supplies it.** The vertical struts that failed `T-12` by 40–160× deliver
  440 pN/nm, 13× the mandate, and place the operating point at 0.113 nm.

But the two requirements are not symmetric, and that is the finding:

> **The normal requirement is 33.333 pN/nm and the lateral requirement is 0.4602 pN/nm — a ratio of 72.4×.
> The convexity theorem caps `k_lat/k_norm` at 1; the tuned spacer realises 0.83. So a coupling bought for
> normal stabilisation delivers lateral confinement as a **by-product with 70× of margin**, while a scheme
> bought for lateral confinement delivers 0.4 % of the normal stiffness. The two do not want opposite anchors —
> they want the *same* anchors, sized on the normal condition, and the order of the design is what matters.**

Yaw follows on `C-0015`'s own 3 × 15 grid: `k_yaw = Σ k_i r_i²` = 8205 pN·nm/rad, **22.3×** the 368.173 bound.
And over-stiffening is not free (`C-0014`): the per-anchor thermal force `√(k_BT k)/n` is **0.283 pN** for
`K2` against 10.7 pN for the four-strut scheme and 17.4 pN for the single lever — a 38–61× penalty for
spending the same stiffness on fewer paths.

**The condition this rests on, stated rather than buried:** the lever the standoffs reach is assumed
**laterally grounded**. A superstructure free to translate supplies exactly zero lateral stiffness, by the
same symmetry argument `C-0010` makes about the layer — and `C-0014`'s `S5` shows what one soft element in
series does to such a chain: a factor of 36.

---

## `P4` — the answer does **not** rest on `T-9`, and that is a design choice rather than a measurement

`C-0009` models the crossover as a **rigid** vertical/axial constraint with nothing cited behind it, and `T-9`
has not run.

- **`K2` does not load a crossover axially at all.** Its reaction is matched to the load it opposes, one
  attachment row per duplex, and `C-0015` shows that such a scheme makes the per-load-path **crossover** force
  **exactly zero** under a uniform load — every beam carries the identical load and no interface transmits
  anything. `C-0009`'s 2.3–7.6× concentration is therefore **not** applied to it, and its per-path peak equals
  its per-path static, 2.222 pN.
- **Every concentrated scheme does.** `K5` and `K6` carry `dependsOnCrossoverAxialCompliance = true`, and both
  also fail on placement and on the 65 pN ceiling before `T-9` is reached.

> **`T-9` does not gate the programme. It gates the concentrated topologies, which are already excluded on
> three other grounds — so the answer depends on `T-9` exactly to the extent that a designer chooses a lever
> the tile pushes through one point.** The exact zero it relies on is as fragile as `C-0015` says it is: any
> load non-uniformity restores the per-path force in proportion, and `T-17` is where that is costed.

---

## The declared falsifiers, and what actually happened

| # | fired? | outcome |
|---|---|---|
| 1 — the load-line reduction is wrong | **no** | the **first** root of `W(s) = 33.333 s` at `V*` is at `3.000000 nm` at all 54 states, and `dW/ds = −k_eff` is the identity the characteristic is built on |
| 2 — `\|k_eff\| > 33.33` at 10 nm | **no** | the worst floor anywhere in the box is **27.91 pN/nm** (10 nm, 2 mM, alexander-box(two-body)), a margin of **1.19×** |
| 3 — a straddling six-model bracket | **no** | at every one of the nine `(height, buffer)` pairs all six models fall on the **same** side of 33.333 |
| 4 — the margin smaller than its own uncertainty | **YES** | 19–42 % at 10 nm / 2 mM against `C-0005`'s 123–214 % one-loop correction. **Reported as NOT EXCLUDED, never as established.** The 0.5 mM margin, 2.09–8.65×, is the only one that clears its own uncertainty |
| 5 — a scheme meeting the stiffness and failing an allowable | **YES** | `K5` (440 pN/nm, 190 pN per path) and `K6` (73.3 pN/nm, **760 pN** per path) both clear the stability floor and both break the 65 pN nicked-duplex ceiling — by 2.9× and 11.7×. They also fail placement, so the allowable is not what excludes them, but the falsifier fired as written |
| 6 — the crossover's vertical link dominating | **partly** | it does not gate `K2`; it gates `K5` and `K6`. See `P4` |

---

## Validity range

- **TRL 1–3. Nothing here is measured.** No coupling above has been built and none is proposed as a sequence design.
- **`L₀` is a FORCE-ONSET height** at a defining load of 1.0 pN over the tile (`C-0011`, `CH-0010`). The held gap `L₀ − 3 nm` inherits that convention; a bench reading these numbers in the first-moment convention would be off by 1.71–2.16× in thickness.
- **Mean-field electrostatics, inherited whole.** `C-0005` puts the one-loop correction at **123–214 % of the leading term** across the entire 5–10 nm range for Mg²⁺. **That is one order of magnitude larger than the 19–42 % margin at 2 mM and it is not reducible by a better Poisson-Boltzmann solve.** The Plan says so in advance, and it is the reason no finer field model was run.
- **The characteristic is `C-0003`'s**, at `C-0001`'s single grafting density per height — **not** `C-0011`'s solved SCF profile. Deliberate: the load line must be drawn across the same curve `C-0012` computed. `C-0016` reports that at 5 nm the solved layer is 1.22× outside `C-0003`'s bracket, so **every 5 nm number here carries that exposure** — and 5 nm is the height whose `σ` window `C-0016` has already emptied by 13.3×.
- **The held state at 5 nm is partly outside `C-0002`'s concentrated crossover.** `φ(L₀ − 3 nm)` = **0.186–0.332** at 5 nm, against 0.084–0.144 at 7 nm and 0.047–0.078 at 10 nm. The 5 nm column is therefore an extrapolation of the des Cloizeaux exponent past φ ≈ 0.2 for four of six models. **The 7 and 10 nm columns are inside every upstream validity range**, and every `V*` in the sweep is ≤ 0.349 V, well inside `CH-0007`'s ~1 V point-ion boundary; every held gap is 2.0–7.0 nm, above `C-0005`'s 1.46 nm correlation band.
- **The coupling is a LOAD LINE in one coordinate** — the tile mean, under a uniform load, the only case in which `C-0006`'s tile is rigid. A real coupling dishes the tile; the 45-attachment count is `C-0015`'s answer to exactly that and is **cited, not recomputed** against the layer this task loads.
- **The lever is a section requirement, not a design**, under three end conditions spanning 25.6×; and the superstructure it gathers into is assumed **multilayer**, where CanDo's rigid-crossover treatment is defensible.
- **The zero-bias state is not solved.** `C-0012` shows the zero-bias force is a sign-changing near-cancellation under 4 pN, so every coupling here is taken **unpreloaded** and the preload a stiffer coupling would need is reported as a relation (`R₀ = k_c s* − W(s*)`) rather than evaluated. That is `T-13`'s question.
- **5 and 10 mM MgCl₂ are not swept.** `C-0012` shows §3's 100 pN target is unreachable at **any** bias at 7 and 10 nm in 10 mM, so there is no operating point for a coupling to be sized at. The trend across 0.5 → 2 mM is monotone and the direction is stated.
- **The lateral and yaw by-products assume the coupling's far end is laterally fixed to the substrate.**

## Numbers that are CITED rather than DERIVED

| number | value | why it is cited, and what it moves |
|---|---|---|
| `C-0014`'s per-coordinate lateral bound | 0.460216 pN/nm | **CITED**, itself derived there from `k_BT` and leaf `A1.1`'s 3.0 nm. Moves only the `P3` margin, which is 70× |
| `C-0014`'s yaw bound | 368.173 pN·nm/rad | **CITED**, budgeted at the tile's corner. `P3` margin 22.3× |
| `C-0014`'s in-plane-tether `k_norm` | 0.043 – 0.173 pN/nm | **CITED FROM `C-0014`'s own table**, used as the counter-example in `P3` |
| `C-0015`'s flatness scheme | 45 as 3 × 15 | **CITED.** Used as the attachment budget and as the licence not to apply a concentration factor |
| `C-0015`'s exact-zero per-path crossover force | 0 | **CITED.** It is what makes `P4` a design choice rather than a `T-9` dependency |
| `C-0009`'s load concentration | 2.3 – 7.6× | **CITED**, applied at its worst value to every scheme whose reaction crosses the lattice |
| `C-0009`'s crossover count | 56 | **CITED.** 45 ≤ 56, so the attachments exist |
| `C-0006`'s per-path allowables | 10 / 48 / 65 pN | **CITED, MEASURED**, and loading-rate dependent (`C-0015`). **NOT** §4(f)'s 35–60 pN whole-cross-section band |
| duplex `S`, `EI`, `GJ`, interhelical `d` | 1100 pN; 230, 460 pN·nm²; 2.69 nm | **CITED**; `S` and `d` **MEASURED** (Wang 1997; Fischer 2016), `EI`/`GJ` **CanDo MODEL INPUTS**, not measurements |
| ssDNA Kuhn length and contour per nt | 1.34 – 2.84 nm; 0.65 nm/nt | **CITED, MEASURED** (Bosco 2014; Chen 2012). The spacers carry ~2.2 pN, an order below the lowest force the spectroscopy fits cover, so the **zero-force end is the applicable one** — and it is the soft one, hence conservative for a stiffness requirement |
| `C-0005`'s correlation band and one-loop correction | 1.46 nm; 123–214 % | **CITED.** The second is the largest single uncertainty in this claim |
| `C-0002`'s concentrated crossover | φ ≈ 0.2 | **CITED**, read as a ceiling |
| §3/§6's targets | 100 pN, 3 nm, 40 × 40 nm, 5/7/10 nm, 2 V | **CITED** |

Everything else — `V*` at every state, `k_brush`, `k_es`, `k_eff` and `ℓ` there, the stability floor, the
placement chord, every scheme's stiffness, delivered stroke and per-path force, the spacer contours, the lever
section budget, and the lateral and yaw by-products — is **derived here**, with `C-0012`'s pipeline re-run
rather than tabulated.

## Cross-checks passed

Executed as **39 gate-named `coupling` tests**; full detail and the corrected rows in
[`T-16`](../tasks/T-16-output-coupling-stiffness.md#verify).

- **Gate 1** — `F/δ` is a stiffness to 1e−12; a load line has the units of `W` and `R − W` vanishes at the root; series compliance adds and parallel stiffness adds; a compliance share is dimensionless and sums to 1; `k_yaw = Σ k r²`; unphysical arguments throw.
- **Gate 2** — an infinitely stiff coupling delivers zero stroke and a vanishing one the free stroke, to 1e−6; the delivered stroke is monotone decreasing in `k_c` over a decade sweep; the unpreloaded placement stiffness puts the root **exactly** at the target; a series chain is softer than its softest element; the FJC spacer reduces to `3k_BT/(L_c b)` at vanishing tension; a coupling designed for the target force delivers it to 1e−7 and its **tangent exceeds the mandate** while its secant equals it.
- **Gate 3** — the delivered force is independent of the preload over five preloads spanning ±200 pN, to 1e−12; the window is empty **exactly** when the chord is flatter than the tangent, checked against `C-0012`'s own 10 nm / 0.25 V record; `k_eff = k_brush + k_es` at every record in `C-0012`'s file; the convexity bound `k_lat ≤ k_norm` holds at every spacer state the design visits; the yaw-to-lateral ratio is the mean squared radius of `C-0015`'s own grid, exactly.
- **Gate 4** — force-curve samples 36 → 72 → 144 move the margin by **4.0e−4 then 7.2e−6**; PB mesh 2000 → 4000 → 8000 by **7.3e−6 then 1.5e−6**, each axis referred to its own finest setting; the operating-stroke bisection exits on the **bracket width** and is scan-independent to 1e−9 over 64 → 8192 steps; **the result file is byte-identical on two independent re-runs.**
- **Gate 5** — `C-0012`'s blocking force, `W(3 nm)` and `k_eff(3 nm)` reproduced at both of its grid biases at 2 mM to a **worst relative departure of 3.82e−9 over 36 comparisons**, by re-running its solver rather than copying its table; its coupling table reproduced as `|k_eff|` (5.31–15.99 at 10 nm / 0.10 V; 47.63–71.54 at 0.25 V; 85.57–276.58 at 7 nm / 0.25 V); `C-0014`'s ssDNA contour ceiling reproduced to 0.6 % and on the correct side; and **`C-0012`'s own `biasForSimultaneousTarget` shown to be a grid interpolation across `[0.1, 0.25]`, departing from the located root by up to 6.1 %**.

## Still open — named, not answered

1. **The 19–42 % margin at 2 mM is inside its own inherited uncertainty.** Nothing in this task can shrink it; only a beyond-mean-field treatment of the divalent, oppositely-charged gap can, and `C-0005` reports that no published result gives even the direction for that geometry. **The 0.5 mM operating point is the one this claim would recommend on stability grounds, and §3 does not name it.**
2. **The lever's own joints are budgeted, not solved.** A finite-element model of an origami lever on its fulcrum would collapse the 25.6× end-condition spread to one number. It is not run because that spread is still smaller than the mean-field error already carried.
3. **The dishing a real 45-attachment coupling causes is `C-0015`'s**, computed on a Winkler foundation at `C-0001`'s stiffness and **cited** here rather than recomputed against the layer this task loads. `T-17`.
4. **The exact-zero per-path crossover force is fragile.** Any load non-uniformity restores it in proportion (`C-0015`), and the `P4` verdict rests on it. `T-17` costs that; `T-3b` would supply the non-uniformity.
5. **The preloaded branch is not evaluated.** A coupling stiffer than the placement value needs a **downward** preload the layer must carry at zero bias, and three of six layer models have exactly zero stiffness at `L₀`. `T-13`.
6. **No 2-D field solve, no lateral load profile, no tile edge** — `T-3b`. The load line here is the tile mean.

## Challenges

**Raises [`CH-0016`](../challenges/CH-0016-coupling-requirement-is-quoted-off-operating-point.md)** against
`C-0012`'s *"the number an output coupling has to supply"*. **No number in `C-0012` moves** — all 36
comparisons reproduce to 3.82e−9 — and what is challenged is the table's **scope**. The challenge as first
drafted overstated its own direction and has been corrected against this run; see its Status section.

**None stands against this claim.** The two ways it would fail:

1. **A beyond-mean-field treatment moving `|k_eff|` at 10 nm above 33.333 pN/nm.** The margin at 2 mM is
   1.19–1.42× against a 123–214 % model error, so this is **not excluded**. It would not empty the window —
   it would move the design to 0.5 mM, where the margin is 2.09–8.65×.
2. **`T-17` finding that a real, non-uniform load restores a per-path crossover force large enough to matter**,
   which would put `K2` back inside `C-0009`'s 2.3–7.6× concentration and take its 2.22 pN to 5.1–16.9 pN —
   past the 10 pN unzip allowable at the worst end, though still 2.8× below duplex shear.
