# T-25 — Re-synthesise the Gen-1 design window against iteration 4

| | |
|---|---|
| **Leaf** | `A2.1`, and it must re-check the acceptance strings of `A2.2`, `A1.1`, `A1.2`, `A8.2` and `A7.4` |
| **Priority** | **HIGH** — it is the coordinator's own task, and nine claims landed in one iteration that touch the synthesis |
| **Verification type** | **logical** (constraint intersection over a common grid, re-run) **+ in-silico** (every upstream number read from the emitting study's own result file at run time and re-intersected) |
| **Depends on** | `C-0016`, `C-0017` (under re-examination); `C-0018`, `C-0019`, `C-0020`, `C-0021`, `C-0022`, `C-0023`, `C-0024`, `C-0025`, `C-0026`; `CH-0019`, `CH-0021`, `CH-0024`, `CH-0025`, `CH-0026`, `CH-0027`, `CH-0029`, `CH-0031`, `CH-0033`, `CH-0034` |

---

## Formulate

### The question

`C-0016` computed the Gen-1 design window and `C-0017` closed its `P2` on the output coupling.
Nine claims and ten challenges have landed since.
**Which window edges move, which verdicts move, and which do not** — computed, not asserted.

### Locked units and conventions

Inherited from `C-0016` and `C-0017` unchanged, and restated because the whole task is a transfer:

- `z` normal to the electrode, positive away from it; **the electrostatic gap IS the layer height, exactly**.
- **`L₀` is a FORCE-ONSET height** — the height at which the layer carries 1.0 pN over the 40 × 40 nm tile
  (`C-0011`, `CH-0010`). The first-moment thickness of the same layer is 1.71–2.16× smaller.
- The stroke `s = L₀ − h` is positive **downward**. `CH-0024`: the tile's zero-bias rest is at `h₀ = L₀ − d`
  with `d > 0`, so the **delivered** stroke is `s − d` and is a different quantity from `s`.
- `φ` always the physical polymer volume fraction, `Nσv₀/h`.
- A **window width is a ratio** of its edges, never a difference.
- `k_es = |F_es| d ln|F_es|/dh`, negative above the force maximum; `ℓ = −1/(d ln|F_es|/dh)`, so
  **`k_es = −|F_es|/ℓ` identically**.
- Lengths nm, forces pN, stiffness pN/nm, `σ` nm⁻², bias V, buffer mM MgCl₂, `k_BT = 4.142 pN·nm` at 300 K.

### Acceptance predicate

**`P1` — the edges.** For each of the three §3 heights, the §4(a)–(d) window re-computed on `T-1d`'s own
61-point grid under four variants (baseline; `+ C-0019`; `+ CH-0024`; both), with **every edge stated as
moves / does not move, by how many grid steps, and whether it changes owner**.

**`P2` — the margins.** `C-0017`'s stability margin and `C-0018`'s pull-in margin recomputed with
`C-0019`'s `k_brush` degradation and `C-0022`/`CH-0026`'s edge correction applied **at the operating point**,
at every one of the 54 `(model, height, buffer)` states, and the binding one named.

**`P3` — the axes.** Every constraint the programme has discovered classified as `σ`-resolved,
height-level or topological, **with the classification computed** where a `σ`-resolved quantity exists
(the variation ratio of the constraint quantity across the grid at fixed height) rather than asserted.

**`P4` — the "≥ 3 nm" clauses.** Every acceptance clause reading "≥ 3 nm" re-read against the *delivered*
stroke under each of the three coupling topologies the programme has held, and PASS/FAIL stated.

**`P5` — the buffer.** The 0.5 mM operating point against §3's 2 mM on **every** axis that moves with the
buffer, and a recommendation on whether 0.5 mM should be the nominal.

### What would falsify this approach

1. An upstream record keyed on fewer dimensions than the sweep varied, so that the wrong record is
   consumed (`C-0026`'s own trap). Every reader here keys on **every** dimension its sweep varied and
   `require`s a unique match.
2. A correction that is not a multiplier on a quantity the window is a function of — which would mean the
   synthesis cannot carry it and must re-run the physics instead.
3. An edge whose movement is smaller than the grid ratio 1.109, which would make "it moves" unreportable.
4. The pinned-force decomposition failing — i.e. `|F_es^target|` **not** being independent of the buffer at
   fixed `(model, height)`, which is what licenses treating the edge multiplier as absorbed into the bias.
5. A window closing, or an edge changing owner, which would make this a new claim rather than a re-run.

---

## Plan

### Method, and why this one

This is a **synthesis** task, exactly as `T-2` was. Nothing new is derived. Every number is read from the
emitting study's own `gpd/results/*.json` at run time and re-intersected. `C-0016`'s own lesson —
*a constraint that cannot narrow is invisible to an intersection* — is applied to this task itself:
each new constraint is first classified by **which axis it lives on**, and only the `σ`-resolved ones enter
the intersection at all.

**The cheap bound, run first.** Before any correction is applied, ask *which clause owns each edge*.
`C-0016`'s upper edge is the **stroke under a 100 pN dead load** — a polymer clause with no field in it.
So `CH-0026`'s electrostatic edge correction **cannot** move it, whatever its size, and `CH-0026`'s own
statement that it does is checkable and is checked. That single observation decides three of the four
candidate movers before a line of arithmetic.

**The pinned-force decomposition, which is what makes `CH-0026` carryable.** At the operating point the
force balance fixes `|F_es| = 100 pN + P(g)A`, a purely mechanical quantity. Since `k_es = −|F_es|/ℓ`, a
multiplier `μ(h)` on the force is **absorbed entirely into the bias** and contributes nothing to `k_es`.
What survives is only the *gradient* of the collar,

&nbsp;&nbsp;&nbsp;&nbsp;`1/ℓ_2D = 1/ℓ_1D − d ln μ/dh`,

plus the second-order shift in `ℓ_1D` from the reduced bias. Both are computed from `T-3b`'s and `T-16`'s
own records. This is cheaper than a re-run and it is what `CH-0026` itself asks for
(*"carry a multiplier, not a re-run"*).

**Why not re-run the electrostatics.** `C-0005` puts the one-loop electrostatic correction at 123–214 % of
the leading term across this whole gap range, and `CH-0019` establishes that nothing in the queue narrows
it. A finer field solve would refine a number an order of magnitude inside its own uncertainty. The method
choice is therefore: carry the corrections as multipliers, state the residual term's direction, and spend
the effort on **which axis each constraint lives on**, which is where `C-0016` says the decision actually is.

### The corrections, and where each is read from

| correction | source file | key | what it multiplies |
|---|---|---|---|
| fluctuation, stroke | `T-1f` `propagation` | `(quantity, designPoint)` | the dead-load stroke, ×[1, 1.020] / ×[1, 1.014] |
| fluctuation, overlap | `T-1f` `propagation` | `(quantity, designPoint)` | the coil overlap, ×1.0092 / ×1.0034 |
| fluctuation, `k_brush` | `T-1f` `propagation` | `(quantity, designPoint)` | `k_brush` at the held gap, ×[0.906, 1] / ×[0.949, 1] |
| edge enhancement | `T-3b` `profiles` | `(concentration, gapHeight, appliedBias, biasSource)` | `μ(h)` on `\|F_es\|` |
| zero-bias descent | `T-13` `equilibria` | `(scenario, model, layerHeight, graftingDensity)` | the delivered stroke, `s − d` |
| stability floor | `T-16` `requirements` | `(model, layerHeight, concentration)` | — |
| pull-in margin | `T-4` `ceilings` | `(model, layerHeight, graftingDensity, concentration, loadLine)` | — |
| flatness saturation | `T-17` `restoredForces` | `(shape, profile, foundationMultiplier)` | — |

Every reader `require`s exactly one match on its full key.

### Verify — the five gates

1. **Dimensional** — `k_es = −|F_es|/ℓ` reproduced from `T-16`'s own fields at all 54 records; the edge
   multiplier and every margin dimensionless; `collar = −fraction × L/4` reproduced from `T-3b`'s own fields.
2. **Limiting** — with every correction set to identity the re-run reproduces `C-0016`'s four window edges
   **exactly**; a zero collar gradient and a zero bias shift leave the stability floor unchanged; `d → 0`
   makes the delivered window the baseline window.
3. **Symmetry/conservation** — the intersection is order-independent; **`|F_es^target|` is independent of
   the buffer at fixed `(model, height)`** to 1e−12, which is the pinned-force theorem the whole `CH-0026`
   propagation rests on.
4. **Convergence** — the collar gradient bracketed by forward, backward and central differences and the
   spread reported; every edge located to one grid ratio and no better; the result file byte-identical on
   two `tools/study.sh` runs.
5. **Upstream cross-check** — `C-0016`'s four edges, `C-0017`'s 1.19–1.42×, `C-0018`'s 1.007–1.032,
   `C-0019`'s ≥ 1.07×, `CH-0026`'s +14.7 %, `CH-0024`'s 2.62–2.93 nm and `CH-0034`'s 0.149 each reproduced
   **from its own result file**, not transcribed.

### Entry point

`./gradlew study -Pstudy=window.WindowResynthesisStudyKt` → `gpd/results/T-25-window-resynthesis.json` (~10 s).

---

## Verify — executed

All five gates as **27 gate-named tests** in `src/test/kotlin/window/ResynthesisTransferTest.kt`.
Full-suite run on an isolated snapshot; result file re-run and diffed.

| gate | test | executed | outcome |
|---|---|---|---|
| 1 dimensional | `k_es = −\|F_es\|/ℓ` at all 54 `T-16` records | **yes** | worst relative departure **5e−9**, which is `T-16`'s own 9-digit serialisation floor |
| 1 dimensional | edge multiplier reproduced from `T-3b`'s own collar width, 21 profiles | **yes** | to **1e−7**; fixes the sign convention (the emitted fraction is a *deficit*) |
| 1 dimensional | a stability margin reproduces `T-16`'s own at every finite floor | **yes** | to 1e−6 |
| 1 dimensional | unphysical arguments throw | **yes** | 3 `require`s exercised |
| 2 limiting | identity corrections reproduce `C-0016`'s four edges | **yes** | exact at 7 and 10 nm; 5 nm empty |
| 2 limiting | zero gradient + zero bias shift leave the floor unchanged | **yes** | to **1e−7** at all 54 |
| 2 limiting | `d → 0` gives the baseline window | **yes** | every index identical |
| 2 limiting | a larger descent never widens a window | **yes** | monotone at all three heights |
| 3 symmetry | `\|F_es\|` at the operating point is buffer-independent | **yes** | to **1.3e−6**, the file's rounding — the pinned-force theorem |
| 3 symmetry | the collar gradient is positive at every gap pair | **yes** | 9 of 9, which decides `CH-0026`'s direction |
| 3 symmetry | the intersection is order-independent | **yes** | forward and reversed give identical indices |
| 4 convergence | three difference schemes bracket `d ln μ/dh` | **yes** | spread < 10×, never straddling zero |
| 4 convergence | the collar interpolates and **throws** outside the sampled range | **yes** | no silent extrapolation |
| 4 convergence | every edge is a grid point, ratio constant to 1e−6 | **yes** | 1.10913 |
| 4 convergence | the fold's own movement is inside the scheme spread | **yes** | coupled tangent −2.5 to +4.0 pN/nm, straddling zero |
| 4 convergence | **result file byte-identical on two independent runs** | **yes** | `diff` clean |
| 5 cross-check | `C-0018`'s 1.007–1.032 from `T-4`'s file | **yes** | 6 folds, all pull-in-bound |
| 5 cross-check | `C-0017`'s 1.194–1.424 from `T-16`'s file | **yes** | reproduced |
| 5 cross-check | `CH-0026`'s +14.7 % **and** the +10.3 % at the held gap | **yes** | the two are different records and the keying separates them |
| 5 cross-check | a non-unique key **throws** | **yes** | `C-0026`'s lesson, executable |
| 5 cross-check | `C-0019`'s licensed brackets from `T-1f`'s file | **yes** | stroke 1.0196/1.0138, `k_brush` 0.906/0.949 |
| 5 cross-check | `CH-0024`'s 0.0717–0.3815 nm from `T-13`'s file | **yes** | reproduced |
| 5 cross-check | `CH-0034`'s 0.218 → 0.149 saturation from `T-17`'s file | **yes** | and `reachesTolerance = false` |
| 5 cross-check | the per-point descent transfer licence | **yes** | licensed at 7 and 10 nm, **not** at 5 nm |
| — regression | the `T-25` window moves exactly one edge, outward | **yes** | 10 nm upper, +1 step |
| — regression | removing the substrate tethers is worth ≥ 3 grid steps at 10 nm | **yes** | measured **4** |
| — regression | the combined coupling margin exceeds `C-0017`'s own | **yes** | 1.231–1.528 against 1.194–1.424 |

**Not executed, and named rather than claimed:** no field solve on the equilibrium path (the fold's collar
gradient is interpolated from `T-3b`'s six sampled gaps); no re-run of any upstream physics.
