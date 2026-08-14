# C-0027 — Iteration 4 moves one window edge, and it moves outward: the Gen-1 window is non-empty at 7 and 10 nm, the coupling verdict stands with a *better* margin than it was published with, and the deciding variable is now the buffer

| | |
|---|---|
| **Task** | [`T-25`](../tasks/T-25-window-resynthesis.md) |
| **Leaf** | `A2.1`, re-checking the acceptance strings of `A2.2`, `A1.1`, `A1.2`, `A8.2` and `A7.4` |
| **Verification type** | **logical** (constraint intersection over a common grid, re-run) **+ in-silico** (every upstream number read from the emitting study's own result file **at run time**, keyed on every dimension its sweep varied, and re-intersected — nothing re-derived) |
| **Verdict** | **`C-0016`'s `P1` STANDS with one edge moved outward by one grid step; `C-0017`'s `P2` STANDS with its margin *improved*; `C-0018`'s pull-in margin STANDS, its own movement unresolved. Nothing closes. One axis LEAVES the window entirely.** |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED**, and nothing here is re-derived: every number is a transfer, and every transfer is checked. |
| **Provenance** | `gpd/results/T-25-window-resynthesis.json`, produced by `window.WindowResynthesisStudyKt`; 18 window records over 6 correction sets, 6 edge movements, 6 descent-transfer licences, 15 collar gradients, 54 corrected coupling margins, 6 corrected pull-in bounds, 11 classified axes, 9 stroke clauses, 8 buffer comparisons, a 13-entry ledger; **27 gate-named `window` tests, 65 in the package, 958 in the suite, 0 failures**; the result file re-run and diffed **byte-for-byte identical** |
| **Conditions** | T = 300 K, `k_BT = 4.142 pN·nm`; aqueous MgCl₂ (`T-16` swept 0.5 / 1 / 2 mM, `T-4` swept 0.5 / 2 / 10); 40 × 40 nm tile; linear PEG; layer heights 5 / 7 / 10 nm |
| **Consumes** | [`C-0011`](C-0011-scf-density-profile.md) (the layer grid), [`C-0015`](C-0015-crossover-phase-and-registration.md) (the layout sweep), [`C-0019`](C-0019-mean-field-fluctuation-corrections.md), [`C-0022`](C-0022-tile-edge-load-profile.md), [`C-0021`](C-0021-zero-bias-resting-position.md), [`C-0017`](C-0017-output-coupling-stiffness.md), [`C-0018`](C-0018-maximum-usable-bias.md), [`C-0026`](C-0026-one-row-per-duplex.md), and the challenges `CH-0019`, `CH-0021`, `CH-0024`, `CH-0025`, `CH-0026`, `CH-0027`, `CH-0029`, `CH-0031`, `CH-0033`, `CH-0034` |
| **Raises** | [`CH-0035`](../challenges/CH-0035-the-edge-correction-cannot-reach-the-window-edge.md) against `CH-0026`, and [`CH-0036`](../challenges/CH-0036-a-correction-and-the-part-that-caused-it.md) against `CH-0024` |
| **Re-run by** | [`C-0051`](C-0051-second-window-resynthesis.md) (`T-118`) against iterations 5–7 |

> ⚠️ **Re-run against `C-0031`–`C-0050` and `CH-0043`–`CH-0062` by [`C-0051`](C-0051-second-window-resynthesis.md)
> (2026-08-14). `P1` stands, with **zero of six edges moving and no owner changing**.**
>
> Two things below do **not** stand as written. **`P2`'s coupling verdict is the AFFINE MANDATE's**: the
> coupling the programme has (`C-0030`'s flexure) strain-*softens*, and `C-0032`'s **1.0000–1.0019** is the
> standing 10 nm / 2 mM statement. **The pull-in bound `≥ 1.108–1.134` is withdrawn** — `CH-0051` shows the
> pull-in bias falls too, and `C-0051` shows the composed fold tangent is **−8.40 to −11.06 pN/nm**.
> Axis **(p)**, `C-0023`'s 40 pN/nm compliance ceiling, **leaves the acceptance stack** (`C-0049`) and a
> path count replaces it. §3's *desired* stroke now has a **mechanism**, `s = L₀ − h` (`C-0050`).

---

## THE HEIGHT CONVENTION — read this before any number below

> **Every layer height here is a FORCE-ONSET height: `L₀` is where the layer carries 1.0 pN over the
> 40 × 40 nm tile** (`C-0011`). The first-moment thickness `2⟨z⟩` of the same layer is 1.71–2.16× smaller,
> and a bench reading this window in the wrong convention would order 8–9 kDa PEG where it needs 1.1–3.3 kDa.

---

## The claim, in one line

**Of the four results iteration 4 aimed at the design window, three live on axes an intersection cannot see and
the fourth — `CH-0024`'s delivered stroke — is very nearly cancelled by the part of the design that produced
it: `C-0023`'s two-sided coupling removes the eight substrate tethers whose preload `CH-0024` measures. The
net movement over 183 grid points at three heights is **one edge, at 10 nm, by one grid step, outward**. On
the coupling axis the two corrections that were supposed to decide the verdict run in *opposite* directions
and are of the *same size* — `C-0019` degrades the 10 nm / 2 mM margin from 1.19–1.42× to 1.11–1.25× and
`C-0022`/`CH-0026` restores it to 1.34–1.67×, giving a combined **1.23–1.53×**, better than `C-0017`
published — so `C-0019`'s "≥ 1.07×" was one half of a two-sided correction. And the single largest design
decision left is not a calculation: at 0.5 mM the same margin is **2.16–9.87×** and the pull-in fold does not
exist at all.**

---

## `P1` — the window, and exactly which edges move

Baseline reproduces `C-0016` to the digit and is asserted to in a test.

| `L₀` | `C-0016` | `+ C-0019` | `+ CH-0024` (committed device) | **`T-25` (both)** | steps moved |
|---|---|---|---|---|---|
| **5 nm** | EMPTY | EMPTY | EMPTY | **EMPTY** | — |
| **7 nm** | `[0.029552, 0.049602]`, **1.678×** | `[·, 0.055015]`, 1.862× | `[·, 0.044721]`, 1.513× | **`[0.029552, 0.049602]`, 1.678×** | **0** |
| **10 nm** | `[0.011634, 0.260150]`, **22.36×** | `[·, 0.288540]`, 24.80× | `[·, 0.260150]`, 22.36× | **`[0.011634, 0.288540]`, 24.80×** | **+1, upper** |

> **One edge moves, out of four non-empty edges, and it moves outward by one grid ratio (1.1091×).**
> At 7 nm the two corrections cancel to *exactly* the grid: `C-0019` widens by one step and `CH-0024`
> narrows by one, and the resynthesised window is `C-0016`'s window to the last digit.

**No edge changes owner.** Coil overlap `Σ ≥ 1` still owns every lower edge and §3's 3 nm stroke still owns
every upper one. 5 nm is still empty, and it is emptied **harder**: the crossing widens from `C-0016`'s
**13.32×** to **24.80×**, because the compliance clause now has to deliver `3.0 + d` rather than 3.0.
Under the *tethered* counterfactual it does not even cross — no grafting density at 5 nm delivers 3 nm at all.

### The finding the movement table hides

`CH-0024` quotes its shortfall — 2.62–2.93 nm delivered — for **`C-0021`'s device, which still carries
`C-0014`'s eight substrate tethers**. `CH-0027` removed those tethers from the design in the same iteration.
Run both:

| | 10 nm upper edge | width | 7 nm width |
|---|---|---|---|
| **committed device** (two-sided coupling, tetherless) | **0.288540** | **24.80×** | **1.678×** |
| had the substrate tethers stayed | 0.190667 | 16.39× | 1.230× |
| | **4 grid steps** | **1.51×** | **3 steps** |

> **`CH-0027`'s removal of the substrate tethers is worth four grid steps of window at 10 nm and three at
> 7 nm — where the 7 nm window would have been 1.23× wide, one grid step from empty.** `C-0023` was filed as
> a `T-13` result. It is also, and was not reported as, the largest single defence of the 7 nm window in the
> programme. That is [`CH-0036`](../challenges/CH-0036-a-correction-and-the-part-that-caused-it.md).

### The descent transfer, checked rather than assumed

`T-13` solved the zero-bias balance at one grafting density per height; the window needs it at all 61. The
first-order transfer `d = F_down/(k_c + k_layer(L₀))` is checked at the shared points:

| `L₀` | first-order `d` | `T-13`'s own bracket | licensed? |
|---|---|---|---|
| 10 nm | 0.0237 nm | 0.0184 – 0.0267 | **yes** |
| 7 nm | 0.0927 nm | 0.0426 – 0.1090 | **yes** |
| 5 nm | 0.2966 nm | 0.0506 – 0.2960 | **NO — 1.002× above the top** |

At 5 nm the transfer is 0.2 % outside and is reported as an exposure. It cannot matter: 5 nm is empty by a
13.3× crossing before any descent is applied.

---

## `P2` — the coupling verdict, and why quoting either correction alone is wrong

`C-0016`'s upper edge is the stroke under a **100 pN dead load** — a polymer clause with no field in it.
So `CH-0026`'s electrostatic edge enhancement **cannot** move it, whatever its size. That is asserted as a
test and it settles three of the four candidate movers before any arithmetic. `CH-0026`'s own statement that
*"`C-0016`'s upper window edge moves outward, because more force at the same bias is more stroke"* is wrong
about which clause owns that edge, and that is
[`CH-0035`](../challenges/CH-0035-the-edge-correction-cannot-reach-the-window-edge.md).

### The pinned-force decomposition, which is what makes `CH-0026` carryable at all

At the operating point the force balance fixes `|F_es| = 100 pN + P(g)A` — mechanics, with no field in it —
and `k_es = −|F_es|/ℓ` **identically**. Both are asserted from `T-16`'s own fields: `|F_es|` is identical
across 0.5, 1 and 2 mM at every `(model, height)` to the file's own 9-digit rounding.

> **A multiplier `μ` on the *level* of the electrostatic force is therefore absorbed entirely into the bias
> and reaches `k_es` NOT AT ALL. What survives is the collar's *gradient*:**
>
> &nbsp;&nbsp;&nbsp;&nbsp;`1/ℓ_2D = 1/ℓ_1D(V*′) − d ln μ/dh`, with `d ln μ/dh = 0.0133 – 0.0226 nm⁻¹` at the 7 nm held gap,
>
> **and the gradient is positive, so it LENGTHENS the decay and REDUCES `|k_es|`.** `CH-0026` predicts the
> opposite direction for stability clauses because it reasons at fixed *bias* where the device is held at
> fixed *force*.

### The margin at the worst point in the programme — 10 nm, 2 mM, six models

| | stability margin `33.333/floor` |
|---|---|
| `C-0017` as published | **1.194 – 1.424×** |
| `+ C-0019` alone (`k_brush` × 0.906) | **1.110 – 1.245×** |
| `+ CH-0026` alone (collar gradient + bias shift) | **1.335 – 1.668×** |
| **both** | **1.231 – 1.528×** |

> **The two corrections are of the same size and opposite sign, and the combined margin is BETTER than the one
> `C-0017` published.** `C-0019`'s "≥ 1.07×" — correct as far as it went — carried only the polymer half of a
> two-sided correction, and `C-0019` says so itself in its own validity range: *"`C-0022`/`CH-0026`'s 5–19 %
> electrostatic edge enhancement is NOT carried."*
>
> **`0` of the 54 states fails §3's own mandated 33.333 pN/nm, before or after.**

### The pull-in margin, and the one thing this synthesis cannot resolve

`C-0018`'s 1.007–1.032 at 10 nm / 2 mM moves on two axes and they are different kinds of statement:

1. **The operating bias falls, unambiguously** — the enhanced force reaches the pinned target at a bias
   8–9 % lower, through `T-16`'s own measured `dV/dF`. At unchanged pull-in bias that raises the margin to
   **≥ 1.108 – 1.134**.
2. **The pull-in bias's own movement is NOT resolved.** At `C-0018`'s own fold the coupled tangent under both
   corrections is `k_c + k_brush·m + k_es′`, and over the collar gradient's three difference schemes it runs
   **−2.5 to +4.0 pN/nm — straddling zero**. `C-0019`'s softening and `CH-0026`'s collar cancel at the fold to
   within the numerical resolution of the correction that would move it.

> **Read `C-0018`'s 1.007–1.032 as standing.** What would resolve it is cheap and is named: a 2-D field solve
> **on the equilibrium path** rather than at the six gaps `T-3b` sampled. `T-3b`'s solver already exists.

---

## `P3` — the axes, classified, and the classification computed

An axis is `σ`-resolved iff its constraint quantity varies across the grid at fixed height. Where such a
quantity exists the variation ratio is **computed**, not asserted.

| axis | source | level | `σ` span at 10 nm | can it narrow? |
|---|---|---|---|---|
| **(a)** coil overlap | `C-0011` | `σ`-resolved | **94.2×** | **yes** — owns every lower edge |
| **(a)** compliance stroke | `C-0011`/§3 | `σ`-resolved | **3.37×** | **yes** — owns every upper edge |
| **(m)** delivered stroke | `CH-0024`/`C-0021` | **`σ`-resolved, and NEW** | **1.047×** | **yes** — the only new one that can |
| **(h)** peak per-load-path force | `C-0015`/`C-0026` | `σ`-resolved | 1.80× | no — 67× clear |
| **(i)** lateral-confinement footprint | `C-0014` → `C-0020`/`CH-0021` → `C-0023` | **WITHDRAWN** | — | **the axis LEAVES the design** |
| **(g)** flatness attachment count | `C-0015`/`CH-0034` | topological, **saturating** | — | no |
| **(e)** usable bias | `C-0012` → `C-0018` | height- and buffer-level | — | no — closes a cell, not an interval |
| **(f)** output-coupling stiffness | `C-0017`/`C-0019`/`CH-0026` | height- and buffer-level | — | no |
| **(j)** zero-bias confinement | `C-0021`/`C-0023`/`CH-0027` | topological | — | no — **closed by construction, 72.4×** |
| **(k)** joint anisotropy / standoff | `C-0025`/`CH-0031` | topological, a **LENGTH** window | — | no — 7–10 nm of standoff, not a `σ` interval |
| **(l)** per-path allowable vs bonded length | `C-0024`/`CH-0029` | topological / sequence-design | — | no |

> **Iteration 4 added four axes and REMOVED one. Seven of eleven do not resolve in `σ` at all.**
> `C-0016`'s own lesson applies to itself twice over: a constraint that cannot narrow is invisible to an
> intersection — **and so is a constraint that has been discharged.** Axis (i), the footprint cost `C-0016`
> reported as unclosable for want of a §3 budget, is gone: `CH-0021` makes the in-plane factor exactly 1 and
> `CH-0027` removes the in-plane tethers from the design entirely, so there is no tether left to have a
> footprint.

### `CH-0034`: the flatness count is exhausted, not met

Under the load `T-3b` actually solved, the dishing saturates at **0.149 of the stroke** between 45 and 225
attachments and **never reaches the 0.10 tolerance at any count** — 5× the attachments buys 6.9 percentage
points and the last 105 of them buy 0.6. **This moves no window edge. It moves what the count MEANS**: 45 as
3 × 15 is where attachments stop buying flatness, not where the tile becomes flat, and the residual is a
property of `C-0022`'s rim collar, which is height-level.

---

## `P4` — the "≥ 3 nm" clauses, re-read against the delivered stroke

| coupling topology | 5 nm | 7 nm | **10 nm** |
|---|---|---|---|
| none — the `L₀` coordinate `C-0016`/`C-0017` are written in | 3.000 | 3.000 | 3.000 |
| **`C-0023`'s two-sided flexure, tetherless — the committed design** | 2.704 – 2.949 | 2.891 – 2.957 | **2.973 – 2.982** |
| `C-0021`'s device: `K2` + `C-0014`'s eight substrate tethers (`CH-0024`'s own) | 2.618 – 2.928 | 2.697 – 2.875 | 2.680 – 2.776 |

> **At the committed design and the 10 nm design point the shortfall is 0.6–0.9 %, not 2–13 %.**
> No acceptance clause reading "≥ 3 nm" fails at a size any model bracket in this programme can see: at 10 nm
> the shortfall is an order of magnitude inside `C-0019`'s own ±9.4 % on the stiffness and two orders inside
> `C-0005`'s 123–214 %. At 7 nm it is 3.6 % and at 5 nm 9.9 %, and 5 nm is empty anyway.
>
> The window absorbs it exactly: the §4(a) compliance clause is re-read as **`stroke ≥ 3.0 + d`**, i.e.
> 3.023–3.024 nm of *layer* stroke at 10 nm and 3.090–3.095 nm at 7 nm.

---

## `P5` — the buffer, and it is the one decision left

| 10 nm | pull-in folds | usable bias | bias margin | **stability margin, corrected** |
|---|---|---|---|---|
| **0.5 mM** | **0 of 6** | 0.133 – 0.246 V | 1.29 – 2.36 | **2.16 – 9.87×** |
| 1 mM | — | — | — | 1.80 – 3.53× |
| **2 mM** (§3's own) | **6 of 6** | 0.130 – 0.184 V | **1.007 – 1.032** | **1.23 – 1.53×** |

> **0.5 mM should be the NOMINAL Gen-1 operating point, not an alternative.**
>
> It is the **fifth** independent route to leaf `A2.2`'s low-screening condition — `C-0012` on the force
> clause, `C-0016` on Reading A of the bias window, `C-0017` on the stability floor, `C-0018` on the usable
> bias, and now the corrected margin. At 0.5 mM the pull-in fold **does not exist**, the stability margin is
> **1.76× larger at its worst end** and is **the only margin in the programme that clears `C-0005`'s own
> 123–214 % mean-field error**. It costs nothing: `C-0007` shows the layer's mechanics are buffer-independent
> to ≤ 0.4 %, so the window edges, the chain length, the stroke and the chemistry are all unchanged.
>
> **The alternative to adopting it is `T-50` — primitive-model Monte Carlo, costed by `C-0005` at 1–3 weeks
> of wall clock for a regime it says has no systematic theory at all.** §3 names 2 / 5 / 10 mM and does not
> name 0.5. **This is a specification question for NDI, not a calculation**, and it is the single largest open
> design decision in the programme.

---

## The Gen-1 verdict as §6 task 2 asks for it

| branch | verdict |
|---|---|
| **a non-empty region satisfying §4(a)–(d)** | **YES at 10 nm — `σ ∈ [0.0116, 0.2885] nm⁻²`, 24.8× wide — and at 7 nm — `[0.0296, 0.0496]`, 1.68× wide.** Both edges attributed, at both heights, to exactly two constraints |
| **the binding constraints** | **lower: coil overlap `Σ = πR₀²σ ≥ 1`, the 1-D mean field's own validity condition. Upper: §3's 3 nm stroke, now read as `3.0 + d` delivered.** Unchanged owners, at both heights, through nine claims and ten challenges |
| **a proof of emptiness** | **YES at 5 nm.** `C-0016`'s 13.3× crossing stands, and under `CH-0024` the compliance clause is now empty on the whole grid at that height rather than merely crossing |
| **the axes §4 does not name** | **NON-EMPTY and CONDITIONAL.** §3's own mandated 33.333 pN/nm clears the stability floor at all 54 states with 1.23–1.53× at the worst; the coupling exists as a design (45 flexures, span 31.64 nm = 93 bp, on 8 nm = 24 bp normal standoffs, `C-0025`); the zero-bias position is closed by the same part; flatness, load path, lateral confinement and yaw are all discharged by the one grid |
| **what the verdict is conditional on** | **the buffer, and a model error nothing in this programme narrows.** At §3's 2 mM the margin is 1.23–1.53× against `C-0005`'s 123–214 % one-loop electrostatic correction, so the 10 nm verdict remains **NOT EXCLUDED, never established** (`CH-0019`). At 0.5 mM it is 2.16–9.87× and clears it |
| **§3's *desired* ~10 nm stroke** | **STILL UNREACHABLE** at every height and every grafting density — `C-0001`'s one surviving headline, untouched by iteration 4 |

---

## The five verification gates

Executed as **27 gate-named tests** in `src/test/kotlin/window/ResynthesisTransferTest.kt`.

- **Gate 1** — `k_es = −|F_es|/ℓ` reproduced from `T-16`'s own fields at all 54 records to **5e−9**, which is
  that file's 9-digit serialisation floor and not a physical departure; the edge multiplier reproduced from
  `T-3b`'s own collar width (`collar = −fraction × L/4`) at all 21 profiles, which fixes the **sign**
  convention; a margin is dimensionless and reproduces `T-16`'s own; unphysical arguments throw.
- **Gate 2** — with every correction set to identity the re-run reproduces `C-0016`'s four edges **exactly**;
  a zero collar gradient and a zero bias shift leave the stability floor unchanged at all 54 records; `d → 0`
  makes the delivered window the baseline window; a **larger** descent never widens a window.
- **Gate 3** — `|F_es|` at the operating point is **independent of the buffer** at every `(model, height)`,
  which is the pinned-force theorem the whole `CH-0026` propagation rests on; the collar gradient is
  **positive at every gap pair**, which is what decides the *direction* of `CH-0026` against `CH-0026`'s own
  statement; the intersection is order-independent.
- **Gate 4** — the collar gradient bracketed by forward, backward **and** central differences, and the spread
  reported as the uncertainty rather than one scheme chosen; the collar interpolated and **never
  extrapolated** — outside `T-3b`'s sampled gap range the accessor throws; every edge is a grid point located
  to 1.1091× and no better; **the result file byte-for-byte identical on two independent runs.**
- **Gate 5** — `C-0016`'s four edges, `C-0017`'s 1.194–1.424×, `C-0018`'s 1.007–1.032, `C-0019`'s licensed
  brackets, `CH-0026`'s +14.7 %, `CH-0024`'s 0.0717–0.3815 nm and `CH-0034`'s 0.149 each reproduced **from its
  own result file**; and **a key that does not identify a unique upstream record throws**, which is the
  executable form of `C-0026`'s lesson.

### The declared falsifiers, and what happened

| # | fired? | outcome |
|---|---|---|
| 1 — a record keyed on too few dimensions | **no** | every accessor requires exactly one match. It is what separates `CH-0026`'s **+14.7 %** at the *resting* height from the **+10.3 %** at the *held gap*, where the operating point is |
| 2 — a correction that is not a multiplier | **no** | `CH-0026` needed a *decomposition* rather than a multiplication, and the decomposition is exact |
| 3 — an edge movement below the grid ratio | **YES, at 3 of 4 non-empty edges** | the corrections are real and sub-grid there. The honest report is *"does not move at this resolution"*, and that is what is reported |
| 4 — the pinned-force decomposition failing | **no** | `\|F_es\|` is buffer-independent to the file's own rounding |
| 5 — a window closing or an edge changing owner | **no** | nothing closes and no owner changes |
| 6 — the two stability corrections running the same way | **no** | they run opposite and are the same size, which is the claim |

---

## Validity range

- **TRL 1–3. Nothing here is measured, and nothing here is re-derived.** Every number is a transfer, and every
  transfer is checked against the file it came from.
- **THE HEIGHT CONVENTION IS FORCE-ONSET**, at a defining load of 1 pN over the tile. `T-1e` has not run.
- **Every edge is a grid point** on `T-1d`'s 61-point logarithmic sweep, located to **1.1091×** and no better.
  Several corrections here are smaller than that and are reported as sub-grid, **not as zero**.
- **The edge correction is a DECOMPOSITION, not a re-run.** The level term cancels at a pinned operating point;
  the collar gradient is a finite difference over a sweep whose bias covaries with the gap. `μ` is a function
  of the gap to **0.14 %** at the one gap `T-3b` sampled at three biases, which is what makes the difference
  meaningful, and all three difference schemes are reported.
- **The pull-in propagation covers only folds for which `T-16` has a coupling record at the same
  `(model, height, buffer)`.** `T-16` swept 0.5/1/2 mM and `T-4` swept 0.5/2/10, so the **7 nm / 10 mM folds
  are not propagated** and `C-0018`'s own numbers stand there.
- **`C-0019`'s brackets exist at two design points only.** At 5 nm the 7 nm multipliers are used; 5 nm is empty
  by 13.3× and a 1.4 % stroke change cannot reach it, which is asserted rather than assumed.
- **The per-point descent is FIRST ORDER** in the layer's stiffness at `L₀`, where `T-13` solved the same
  balance non-linearly. Licensed at 7 and 10 nm, **not licensed at 5 nm** (0.2 % outside).
- **Mean-field electrostatics, inherited whole.** `C-0005`: 123–214 % of the leading term across the whole
  5–10 nm range, and `CH-0019` establishes that nothing in this queue narrows it. **Every margin here is
  NOT EXCLUDED, never established.**
- **`C-0016`'s and `C-0017`'s own validity ranges travel unchanged**, including the 1.22× exposure of the
  solved layer against `C-0003`'s bracket at 5 nm.
- **The layer is neutral linear PEG.** §3 also permits PEO and a PS→PEG block copolymer, for which no osmotic
  equation of state was ever consumed in this programme.

## Numbers that are CITED rather than DERIVED

| quantity | value | unit | source | provenance |
|---|---|---|---|---|
| mandated coupling stiffness | 33.3333 | pN/nm | `C-0017` from §3 alone | **CITED** |
| acceptable stroke / tile edge | 3.0 / 40 | nm | §3, §1 | **CITED** |
| rigid-plate tolerance | 0.10 | — | `C-0015` | **CITED**, a convention not a threshold |
| stroke multiplier at 10 / 7 nm | 1.0196 / 1.0138 | — | `C-0019` | **CITED** |
| `k_brush` multiplier at 10 / 7 nm | 0.9058 / 0.9490 | — | `C-0019` | **CITED** |
| hold-down force, tetherless device | 0.815 / 3.263 / 10.760 | pN | `C-0021` | **CITED** |
| flatness floor under the solved load | 0.1490 | — | `CH-0034` | **CITED** |
| edge multiplier at the held gap, 2 mM | 1.1032 / 1.0363 / 0.9609 | — | `C-0022`/`CH-0026` | **DERIVED here** from `T-3b`'s own record, keyed on (buffer, gap, bias source) |
| coil-overlap multiplier | 1.0092 / 1.0034 | — | `C-0019` | **DERIVED here** from `T-1f`'s own edge shift |
| grid ratio | 1.10913 | — | `T-1d`'s own sweep | **DERIVED here** |

Everything else — every window edge, every edge movement, every corrected floor and margin, the collar
gradients, the descent licences, the axis spans, the delivered strokes and the buffer comparison — is
**derived here from the eight consumed result files**.

## Challenges

**Raises [`CH-0035`](../challenges/CH-0035-the-edge-correction-cannot-reach-the-window-edge.md)** against
`CH-0026`'s two directional statements, and
**[`CH-0036`](../challenges/CH-0036-a-correction-and-the-part-that-caused-it.md)** against `CH-0024`'s
delivered-stroke bracket. Neither moves a number in the claim it is raised against; both move its scope.

**None stands against this claim.** The two ways it would fail:

1. **A 2-D field solve on the equilibrium path finding the collar gradient at the fold outside the bracket
   used here.** The pull-in propagation would then resolve in one direction or the other, and the direction is
   currently undecided by ±4 pN/nm of coupled tangent.
2. **`T-21` moving `C-0002`'s `φ = 0.2` ceiling.** It is the binding ceiling at 121 of 162 of `C-0018`'s
   states, and nothing here or in `C-0019` can move it — it is the upper end of a *data* range, not a theory
   boundary, and it needs osmometry.
