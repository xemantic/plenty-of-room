# T-118 — Re-synthesise the design window and the standing findings against iterations 5–7

| | |
|---|---|
| **Leaf** | `A2.1`, re-checking the acceptance strings of `A2.2`, `A1.1`, `A8.2` and `A7.4` |
| **Predecessor** | [`T-25`](T-25-window-resynthesis.md) / [`C-0027`](../claims/C-0027-window-resynthesis.md), which did exactly this for iteration 4 (`C-0018`–`C-0026`) |
| **Verification type** | **logical** (constraint intersection over a common grid, re-run) **+ in-silico** (every upstream number read from the emitting study's own result file **at run time**, keyed on every dimension its sweep varied) |
| **Status** | **DONE** (iteration 8) — claim [`C-0051`](../claims/C-0051-second-window-resynthesis.md) |

---

## Formulate

### The question

`C-0027` closed iteration 4 with a window, an axis classification, a coupling verdict and a buffer recommendation.
Three iterations have run since — twenty claims (`C-0031`–`C-0050`) and twenty challenges (`CH-0043`–`CH-0062`).
**Does any window edge move, and where does each new constraint live?**

`C-0016`'s own central finding is the discipline this task is written under:

> *a constraint that cannot narrow is invisible to an intersection*

and `C-0027` added the converse — *and so is a constraint that has been discharged*, having found one axis leave the window entirely.
So the deliverable is **not** a window and a verdict; it is a window, a verdict, **and an axis for every constraint**, with removals recorded as carefully as additions.

### Locked units and conventions

- SI throughout; lengths **nm**, forces **pN**, stiffness **pN/nm**, pressure **pN/nm²** (= 1 MPa exactly), energies `k_BT` at `T = 300 K`, `k_BT = 4.142 pN·nm`.
- Aqueous MgCl₂; §3's own **2 mM** and the recommended **0.5 mM** are both reported, and **neither is adopted** — `T-63` is a specification question for NDI.
- `σ` is the grafting density in nm⁻², on `T-1d`'s own 61-point logarithmic grid, ratio **1.10913**; every window edge is a grid point and is located to one grid ratio and no better.
- **`L₀` is a FORCE-ONSET height** (`C-0011`, `CH-0010`) at a defining load of 1.0 pN over the 40 × 40 nm tile. The first-moment thickness of the same layer is 1.71–2.16× smaller.
- The **stroke** is `s = L₀ − h`, positive downward (`C-0050`).
- A **margin** is dimensionless. A **zero** stability floor is the *absence* of a requirement and is recorded as `null`, never as an infinity (`CLAUDE.md`).

### Acceptance predicates

| | predicate | falsifiable how |
|---|---|---|
| **`P1`** | `C-0027`'s three windows re-run on the **current** result files — post-`C-0031`'s solver repair — with every edge reported against `C-0027`'s own published edge and the movement quoted **in grid steps**. | An edge that moves, or an owner that changes. |
| **`P2`** | Every constraint discovered in `C-0031`–`C-0050` and `CH-0043`–`CH-0062` classified by **axis** — `σ`-resolved / height-level / topological / specification — with the `σ` span **computed** wherever a `σ`-resolved quantity exists, and **removals** recorded as well as additions. | A `σ`-resolved constraint that binds inside a window and was not intersected. |
| **`P3`** | The three corrections now standing against `C-0018`'s 10 nm / 2 mM pull-in margin — `C-0033`'s collar, `C-0019`'s fluctuation and `C-0032`'s softening coupling — **composed on one tangent**, with the sign of the net stated. No two of them were ever carried together. | The composed tangent straddling zero, in which case the direction is unresolved and must be reported as such. |
| **`P4`** | A statement of what the programme's answer to §6 task 2 **now is**, given `C-0050`: whether the *window* is still the right object, or whether the deliverable is a **height** plus a specification question. | — |
| **`P5`** | Every upstream number consumed **reproduced from its own result file**, with the departure quoted. | A reproduction outside its emitting claim's own published rounding. |

---

## Plan

### Method, and why it is the cheap one

`T-25` established the shape: **a synthesis re-runs, it does not re-derive.**
Every number is read from the emitting study's own JSON at run time, keyed on **every** dimension that study's sweep varied (`C-0026`'s lesson, made executable in `T-25`'s accessors), and the intersection machinery is `T-2`'s.
`window.DesignWindowStudyKt` and `window.WindowResynthesisStudyKt` already exist; this task **extends** them rather than duplicating them — `resynthesisedWindows`, `ConstraintInterval`, `ResynthesisInputs` and the whole reader layer are re-used unchanged.

### The cheap bound, which runs first

**Three cheap bounds, each of which could settle a whole predicate before any code runs:**

1. **`P1`.** `C-0031` reports that the solver repair leaves `C-0016`'s window edges **byte-identical**. If that holds, `P1` is discharged by a re-run and a diff, and no new physics is needed. *Falsifier:* an edge that differs from `T-25`'s own file.
2. **`P2`.** Of the twenty claims of iterations 5–7 exactly one (`C-0036`) carries a quantity that is a function of `φ = Nσv₀/h` and therefore of `σ`. Everything else is a **count**, a **plan layout**, an **elastica geometry**, a **height-level actuator state**, or a **specification question**. If `C-0036` does not bind, no window edge can move. *Falsifier:* a second `σ`-resolved quantity.
3. **`P3`.** `C-0033` measures the collar's contribution to the coupled tangent at `C-0018`'s own fold as `+2.60` to `+4.99 pN/nm`; `C-0032` measures `C-0030`'s realised element at `22.9–25.2 pN/nm` against the `33.333` mandate, i.e. `−8` to `−10 pN/nm`. Their sum is negative before anything is solved. *Falsifier:* the element's tangent at the fold stroke coming out above the mandate.

### The two things this task must compute rather than transfer

1. **`C-0050`'s two stroke ceilings on the window's own layer.** `C-0050` evaluates the kinematic ceiling `L₀ − Nσv₀` and the validity ceiling `L₀ − Nσv₀/φ_c` on `C-0003`'s six trial-function models. The window is drawn on `C-0011`'s **solved SCF** layer, whose `N(L₀, σ)` is a different function. Both are `σ`-resolved and both could in principle narrow a window from above. `T-1d` emits `meanVolumeFraction` per design point, and `φ = Nσv₀/h` identically, so both ceilings are available on the window's own grid **for free** — and the licence must be checked at the shared points, per `CLAUDE.md`: *an upstream bracket upheld at one design point is not upheld at all of them.*
2. **The three-channel fold tangent.** At `C-0018`'s own fold the baseline coupled tangent vanishes by construction, `k_c + k_brush + k_es = 0`, so any perturbation enters as an **increment**:

   &nbsp;&nbsp;&nbsp;&nbsp;`ΔT = |F_es| · d ln μ/dh` &nbsp;(collar, `C-0033`) &nbsp;`+ k_brush(m − 1)` &nbsp;(fluctuation, `C-0019`) &nbsp;`+ [k_c(s_fold) − k_c,mandate]` &nbsp;(softening, `C-0030`/`C-0032`)

   and the **sign of `ΔT` is the direction the fold moves** — positive means deeper (margin rises), negative means shallower (margin falls, and possibly through §3's 3 nm target). The first two terms are already in `T-60`'s own decomposition; the third is new and is computed from `C-0030`'s own library at the fold stroke `T-60` reports.

### Justification against cost

The alternative to a synthesis is a re-solve: re-running `C-0018`'s 162 fold searches with `C-0030`'s nonlinear load line **and** `C-0033`'s solved collar on the field, which is `T-76`'s sweep and `T-60`'s sweep composed — hours of Poisson-Boltzmann.
The increment form above costs **one evaluation of a published beam law per fold** and gives the **sign** exactly, because the baseline tangent is zero by construction and not by approximation.
It does not give the relocated fold, and that is stated as a limit rather than papered over.

### What would falsify this approach

1. A window edge that moves — then `C-0031`'s byte-identity claim is wrong and the whole tree needs re-adjudicating.
2. A `σ`-resolved constraint from iterations 5–7 binding inside a window — then the window is still a `σ` statement and `P4`'s conclusion is wrong.
3. `ΔT` straddling zero — then `P3` is unresolved and must be reported as `C-0027` reported its own straddle.
4. An upstream number failing to reproduce from its own file — then a transfer somewhere in the corpus is a transcription and not a reading.
5. The composed `ΔT` being positive — then `C-0033`'s collar rescues `C-0032`'s softening and §3's own 2 mM buffer survives the realised coupling.

### Deliverables

- `src/main/kotlin/window/SecondResynthesis.kt` — the relations, and nothing else.
- `src/main/kotlin/window/SecondResynthesisStudy.kt` — the study main, emitting `gpd/results/T-118-window-resynthesis-two.json` deterministically.
- `src/test/kotlin/window/SecondResynthesisTest.kt` — the five gates, as gate-named tests.
- A claim, and challenges as needed.

---

## Verify

The five gates, as executable tests:

1. **Dimensional** — a stroke ceiling is a length and scales exactly with the layer under `L₀ → λL₀` at fixed `φ`; a per-path secant ceiling times its stroke is a force identically; a margin is dimensionless; unphysical arguments throw.
2. **Limiting cases** — a zero volume fraction makes the kinematic ceiling the layer height; a crossover fraction equal to the layer's own `φ` makes the validity ceiling vanish; a zero collar gradient, a unit fluctuation multiplier and a coupling tangent equal to the mandate make `ΔT` exactly zero; the identity correction set reproduces `C-0016`'s edges.
3. **Symmetry and conservation** — the intersection is order-independent; `ΔT` is additive over its three channels by construction and is asserted to be; the held volume fraction at zero stroke is the resting one.
4. **Numerical convergence** — every edge is a grid point located to 1.10913× and no better; sub-grid movements are reported as sub-grid and never as zero; the result file is byte-for-byte identical on two independent runs.
5. **Literature and upstream** — `C-0016`'s, `C-0027`'s, `C-0032`'s, `C-0033`'s, `C-0049`'s, `C-0050`'s and `C-0041`'s published figures each reproduced **from its own result file**, and a key that does not identify a unique upstream record throws.

---

## Outcome

Closed by [`C-0051`](../claims/C-0051-second-window-resynthesis.md).

| predicate | verdict |
|---|---|
| **`P1`** | **PASS — 0 of 6 edges move, 0 grid steps, no owner change**, worst edge departure exactly `0.0`. `C-0031`'s byte-identity finding confirmed by re-intersection rather than by a diff of the file it produced |
| **`P2`** | **PASS.** Twelve axes classified; of the twenty claims of iterations 5–7 exactly **one** is `σ`-resolved and it refuses the window itself. Two NEW `σ`-resolved constraints (`C-0050`'s ceilings) evaluated at all 61 grid points and **neither binds**, by 1.71–3.11×. **One axis LEAVES** the acceptance stack (`C-0049`) and a path count replaces it |
| **`P3`** | **PASS, and the falsifier did not fire.** Composed increment **−8.398 to −11.062 pN/nm at 6 of 6 models**, no straddle. `C-0033`'s collar recovers 27–49 % of what `C-0032`'s realised element costs → [`CH-0063`](../challenges/CH-0063-the-collar-was-carried-onto-a-load-line-the-device-does-not-have.md) |
| **`P4`** | **The deliverable is a HEIGHT plus five specification questions**, not a window |
| **`P5`** | **PASS.** 23 reproductions, worst departure `4.0e−4`. `C-0050`'s bound 3 found **not licensed** at the window's own upper edge — 4.15× in `φ` → [`CH-0064`](../challenges/CH-0064-the-validity-ceiling-is-read-on-a-layer-four-times-denser-than-the-window.md) |

**23 gate-named tests** in `src/test/kotlin/window/SecondResynthesisTest.kt`; the result file re-run and
diffed **byte-for-byte identical**.
