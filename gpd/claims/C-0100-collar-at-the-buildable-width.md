# C-0100 — **The collar is width-independent, and the thing that is not is the way it is written down.** Re-solving `C-0022`'s 2-D Poisson-Boltzmann tile edge at `C-0086`'s buildable 38.08 nm along-helix footprint moves the whole edge effect, stated fit-free as an effective collar width, by **0.0400 %** — **a factor of eight** inside `C-0090`'s declared 0.32 % — and moves `C-0090`'s own 34-root flatness from **0.0621469068** to **0.0621026606**, **0.0712 %**. But the three numbers `C-0090` literally carries move up to **2.15 %**, and that movement is **the mesh**: `fitEdgeTaper`'s 1 nm rim standoff snaps to the first lateral node at or beyond it, the graded mesh rescales with the tile half-width, and the two collar terms therefore **re-partition** while their **sum** — the global momentum flux — does not

| | |
|---|---|
| **Task** | [`T-160`](../tasks/T-160.md), raised by [`C-0090`](C-0090-buildable-raster-width.md)'s *Still open* item 1 |
| **Leaf** | **`A2.2`** (the electrostatic load the tile carries), with **`A7.4`** (`C-0022`'s own leaf) and **`A8.2`** |
| **Verification type** | **in-silico** (`electrostatics.PoissonBoltzmannEdge`, `C-0022`'s own solver, re-run at a changed `tileHalfWidth`: 23 two-dimensional nonlinear 2:1 Poisson-Boltzmann solves over four states, four nested refinements and a nine-point half-width sweep; `structure.PlateOnFoundation`, `structure.OrigamiGrillage`, `anchoring.rasterColumnLayout`/`rasterUpwardSites`/`UpwardRootInfluenceBank` and `coupling.edgeCollarPressure` consumed **read-only**) **+ logical** (a closed-form exponential-tail model of the taper fit's own width-dependence, which is the cheap bound, is written as executable code, and is **tested against `fitEdgeTaper` itself**) |
| **Verdict** | **PASS on the acceptance's second branch, and the first branch is answered anyway.** *"The collar is width-independent to within `C-0090`'s 0.32 % placement sensitivity"* — **0.0400 %** in the fit-free measure, **0.0973 %** in the fitted triple once its quadrature limit is placed rather than snapped, and **0.0712 %** in the consequence, `C-0090`'s flatness. The collar at 38.08 nm is also **given**, in both readings. `C-0090`'s carry-forward is not merely defensible; it is checked. **Four of the six declared falsifiers fired, and they are the finding**: the *raw* fitted triple moves 2.15 %, does not converge in the mesh (6.78 / 12.86 / 2.15 / 5.56 % over refinements 1/2/3/4, a scatter larger than itself), and is not monotone in the half-width — all three signatures of a **discretisation**, and all three removed by placing the standoff exactly. Raises [`CH-0116`](../challenges/CH-0116-the-collar-split-is-a-mesh-node.md) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED.** And inside mean field: `C-0005` puts the one-loop correction at **123–214 %** of the leading term at these gaps, and for the *oppositely charged* tile-electrode pair no published result gives even the direction. **A second width does not narrow that.** What makes a 0.04 % departure worth quoting inside a 214 % bracket is that the bracket is a **common factor** on the two widths and divides out of their ratio |
| **Provenance** | `gpd/results/T-160-edge-width-dependence.json`, produced by `electrostatics.EdgeWidthDependenceStudyKt`; model in `src/main/kotlin/electrostatics/EdgeWidthDependence.kt` (**new file**; `PoissonBoltzmannEdge.kt` and `TileEdgeFringing.kt` were **read, not edited**); **23 solved 2-D state points** (14 in the state and refinement sweep, 9 in the half-width sweep), **2 cheap-bound anchorings, 1 tail fit, 63 departures, 4 flatness evaluations, 6 convergence records, 7 upstream reproductions, 5 predicates, 6 falsifiers, 9 findings, 10 validity clauses**; **23 gate-named tests** — 13 on the cheap-bound model in `src/test/kotlin/electrostatics/EdgeWidthDependenceTest.kt` and 10 on the **emitted result file** in `src/test/kotlin/electrostatics/EdgeWidthDependenceResultTest.kt`; the study is about **two and a half minutes** of compute, and its result file **carries no wall-clock time** so that a re-run diff is a statement about the answer; `tools/result-reader-census.py --emit` re-run and `--check` clean |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **MgCl₂**, which is **2:1**, so `I = 3c`, at **2 mM** (and 10 mM at one state); `ε_r = 78`, `l_B = 0.7141 nm`; Manning-renormalised tile charge **−0.398665238 e/nm²**, uncharged rim; a **10 nm** gap (5 nm at one state) at `C-0012`'s **located** operating bias 0.192 V (0.134 / 0.368 V at the others); tile thickness 10 nm; along-helix **half-width 20.00 nm** (§3's nominal 40.0 nm) against **19.04 nm** (`C-0086`'s buildable 112 bp = 38.08 nm); the **across-helix span 15 × 2.69 = 40.35 nm, UNCHANGED** (`C-0090`'s Deliverable 1); rim standoff 1.0 nm (one duplex radius) |
| **Consumes** | [`C-0022`](C-0022-tile-edge-load-profile.md) (`PoissonBoltzmannEdge`, `fitEdgeTaper`, `transverseDecayRateBound`, and its published collar **read from its result file and reproduced as the gate**), [`C-0090`](C-0090-buildable-raster-width.md) (the 38.08 nm width, the phase-8 raster lattice and its **recommended placement key**, read from its result file), [`C-0086`](C-0086-seamless-scaffold-routing.md) (112 bp), [`C-0063`](C-0063-upward-root-placement.md) (`UpwardRootInfluenceBank`), [`C-0058`](C-0058-non-uniform-coupling.md) (`InfluenceSurrogate`), [`C-0009`](C-0009-discrete-lattice-tile.md) (the grillage), [`C-0017`](C-0017-output-coupling-stiffness.md) (the mandate), [`C-0012`](C-0012-coupled-stroke-and-blocking-force.md) (the located biases), [`C-0005`](C-0005-mean-field-screening-validity.md)/[`C-0008`](C-0008-electrostatic-force-and-decay-length.md) (the ion model and the validity range) |
| **Raises** | [`CH-0116`](../challenges/CH-0116-the-collar-split-is-a-mesh-node.md) against `C-0022`'s emission of the collar as two separately meaningful load terms |

---

## The claim, in one line

**A collar cannot know how wide its tile is — the two rims are eighteen decay lengths apart — but the *fit* can, and on a graded mesh that rescales with the tile it knows through a quadrature limit rather than through the physics.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, loads **pN/nm²** (= 1 MPa exactly), line loads **pN/nm**, `k_BT = 4.141947 pN·nm` at **300 K**, aqueous **MgCl₂**, 2:1, `I = 3c`.
- `z` is normal to the electrode, positive **away** from it; `x` is lateral, `x = 0` the tile centre-line and a symmetry plane, the rim at `x = a`.
- **`a` is the ALONG-HELIX half-width**: 20.00 nm at §3's nominal footprint, **19.04 nm** at `C-0086`'s buildable 112 bp row. The across-helix span is a **count of duplexes** and does not move.
- **`s`** is distance measured **inward from the rim**: `s = 0` is the rim, `s = a` the centre-line.
- The load is reported **downward**, positive when it pushes the tile toward the electrode.
- A **depth** is `1 − load/interior` and is **negative for an enhancement**; a **width** is the raised cosine matching the deficit's first two moments; the **rim residual** is the part of the global deficit inside the 1 nm standoff, as a line load.

---

## Deliverable 1 — the cheap bound, which ran first and was right

The collar is local. `C-0022`'s deficit centroid is 2.66 nm and the **far tail** fitted here decays over **2.04427507 nm**, so the two rims of a 38.08 nm tile are **18.6 decay lengths** apart: `e^{−18.6} ≈ 8e−9`. Nothing in the field near one rim knows where the other one is.

**But `fitEdgeTaper` does.** It references the profile to the **centre-line** load, integrates the deficit over `[σ, a]` and divides two truncated moments. Model the profile beyond the near-rim structure as `p(s) = Π∞ + A e^{−s/ℓ}` and all three of `C-0022`'s deliverables have a closed form in one exponentially small number, `τ(a) = p(a) − Π∞`:

| | |
|---|---|
| `M₀(a) = E + τ(a)(a − σ + ℓ)` | `E = −A ℓ e^{−σ/ℓ}` |
| `M₁(a) = F + τ(a)[(a² − σ²)/2 + ℓ(a + ℓ)]` | `F = −A ℓ (σ + ℓ) e^{−σ/ℓ}` |
| `D(a) = E₀ + τ(a)(a + ℓ)` | `E₀ = −A ℓ` |

`E`, `F` and `E₀` contain no `a` **by construction** — that *is* the statement that the collar is width-independent — and they are calibrated from the reference solve rather than assumed. `CollarTailModel` is that algebra, and the gate test calibrates it on `fitEdgeTaper` at one half-width and predicts `fitEdgeTaper` at another, agreeing to **1e−6** on a synthetic profile.

| anchoring | `ℓ` | predicted departure at 19.04 nm | settles the 0.32 %? |
|---|---|---|---|
| **best estimate** — the far tail fitted to `C-0022`'s own solved profile (residual `4.8e−5`) | **2.04427507 nm** | **0.1310 %** | yes |
| **pessimistic** — `1/q₀`, the transverse-eigenvalue **ceiling**, with `τ` taken as the 1-D/2-D cross-solver difference | **3.34219676 nm** | **0.4433 %** | no |

**The bracket straddles `C-0090`'s 0.32 %, so the solve ran.** That is the whole cost justification, it was written into `T-160`'s Plan before execution, and the executable bound (0.131–0.443 %) reproduced the hand arithmetic in the Plan (0.07–0.44 %) closely enough to keep the same decision.

**And the bound was upheld by the clean measurement**: the solved departure of the fitted triple, with its quadrature limit placed rather than snapped, is **0.0973 %** — inside the bracket, and just under the best estimate.

---

## Deliverable 2 — the collar at 38.08 nm, in both readings

2 mM MgCl₂, 300 K, a 10 nm gap, `C-0012`'s located 0.192 V, refinement 3, 89 305 nodes, uncharged rim.

| | `a = 20.00 nm` (§3's 40.0) | `a = 19.04 nm` (buildable 38.08) | movement |
|---|---|---|---|
| taper **depth**, as `C-0022` emits it | **−0.302887367** | **−0.305339090** | 0.8095 % |
| taper **width**, as `C-0022` emits it | **8.93928311 nm** | **8.90508151 nm** | 0.3826 % |
| **rim residual depth**, as `C-0022` emits it | **−0.593889278** | **−0.581096194** | 2.1541 % |
| taper depth, standoff placed **exactly** | −0.313931226 | −0.314100198 | **0.0538 %** |
| taper width, standoff placed **exactly** | 8.81823694 nm | 8.80965280 nm | **0.0973 %** |
| rim residual depth, standoff placed **exactly** | −0.533165264 | −0.533051991 | **0.0212 %** |
| **effective collar width** — the whole effect as a length, **no fit in it** | **1.6507426 nm** | **1.65008284 nm** | **0.0400 %** |
| min-margin force fraction | **+14.708 %** | **+15.347 %** | 4.34 % |

Read the last two rows together. The **level** of the force moves by the `1/L` that `C-0090` invoked — and it **cancels out of a dishing**, because the free stroke `q/k_f` carries the same factor. The **shape**, stated fit-free, moves 0.0400 %.

**The effective collar width is the quantity this verdict is taken on.** `−totalDeficit/centrelineLoad` is the whole edge effect expressed as *"the finite tile behaves electrostatically as one this much larger on every side"*; its numerator is the **global momentum flux through one horizontal plane**, which uses no wall value, no lateral derivative, no standoff and nothing from the re-entrant corner.

---

## Deliverable 3 — why the fitted triple moves 22× further than its own clean reading, and it is the mesh

`fitEdgeTaper` advances to the first sample at or beyond the standoff:

```kotlin
while (start < distanceFromEdge.size - 1 && distanceFromEdge[start] < standoff) start++
```

`PoissonBoltzmannEdge` lays its lateral mesh as a `tanh` grading over `[0, a]` clustered at the rim, so **every node position is proportional to `a`**. The standoff lands on **1.03448385 nm** at `a = 20.00` and **1.02728868 nm** at `a = 19.04` — and the integrand there is the **peak of the enhancement**, `1.88×` the interior load, so seven picometres of quadrature limit is worth per cent of deficit.

**Three independent signatures, and each is removed by placing the standoff exactly.**

| signature | snapped standoff | standoff placed exactly |
|---|---|---|
| the worst departure of the three, refinement 3 | **2.1541 %** | **0.0973 %** |
| over refinements 1 / 2 / 3 / 4 | **6.7765 / 12.8602 / 2.1541 / 5.5640 %** — a scatter **larger than itself** | 0.1369 / 0.0946 / 0.0973 / 0.0878 % — converged |
| the half-width sweep, `a = 12 … 30 nm` | **not monotone** (8.6961 / 8.7325 / 8.8458 / 8.8646 / 8.8533 / 9.0417 / 8.9293 / 8.9188 / 9.0883 nm) | **monotone and saturating at all nine** (8.6086 / 8.7171 / 8.7829 / 8.8205 / 8.8328 / 8.8412 / 8.8521 / 8.8595 / 8.8637 nm) |

The third row is the cleanest. The exponential-tail model predicts a **strictly monotone, saturating** width-dependence; the snapped sweep alternates and the placed one does exactly what the model says, over a 6× range of `τ(a)`.

**And the sum never moves.** The effective collar width over the same sweep runs 1.6296 → 1.6567 nm, monotone, and its departure between the two design widths is 0.0400 % at refinement 3 with a **0.0234 %** scatter across refinements 1/2/3/4. The mesh moves load **between** the smooth taper and the rim residual and creates none, because their sum is the momentum flux.

> **A departure at matched refinement cancels everything two solves SHARE — the model, the ion statistics, the mean-field error, the rim-charge convention, the corner cut-off — and it does not cancel anything the changed parameter does to the DISCRETISATION. A matched refinement is not the same mesh.**

That is [`CH-0116`](../challenges/CH-0116-the-collar-split-is-a-mesh-node.md).

---

## Deliverable 4 — what it does to `C-0090`'s flatness

`C-0090`'s recommended placement is re-evaluated on **its own** host: the phase-8 raster column layout at 38.08 nm with the row end admitted, its 34-root station set read from the `bestKey` in its result file, `C-0017`'s mandate shared equally, `C-0063`'s influence bank and `C-0058`'s surrogate. **Only the load field changes.**

| collar the field is built from | free tile | 34-root dishing / stroke | movement | inside 0.32 %? | inside `T-5b`'s 0.10? |
|---|---|---|---|---|---|
| **CARRIED** — `C-0022` at `a = 20.00`, which is what `C-0090` used | 0.299034759 | **0.0621469068** | — (the gate) | — | **yes** |
| **RE-SOLVED** — `T-160` at `a = 19.04` | 0.298417443 | **0.0621026606** | **0.0712 %** | **yes** | **yes** |
| CARRIED, standoff placed exactly | 0.295670248 | 0.0619181974 | 0.3680 % *(against the carried reading)* | no | yes |
| RE-SOLVED, standoff placed exactly | 0.295765822 | 0.0619225371 | **0.0070 %** *(against the row above)* | **yes** | **yes** |

**`C-0090`'s 0.0621469105 is reproduced at 5.99e−8** and its free tile 0.299034765 at 2e−8 — the gate, and falsifier F5 did not fire.

Two things follow, and the second is the more interesting.

1. **The verdict does not move.** `T-5b`'s 0.10 is cleared by a factor of **1.61** in every reading, the movement due to the width is 0.0712 %, and **no downstream file needs re-emitting.**
2. **Re-partitioning the same 40 nm field is worth 0.368 %, which is 5.2× what the tile's own width is worth — and it is *outside* the 0.32 % `C-0090` declares.** Changing no physics at all, only where the quadrature limit is placed, moves the flatness further than solving a different tile does. The collar pair is defined only up to that choice, and the choice was never named.

---

## Deliverable 5 — it is a property of the geometry, not of the design point

All four states, at refinement 3, both half-widths.

| state | fit-free departure | worst fitted departure | worst exact-standoff departure |
|---|---|---|---|
| **2 mM, 10 nm, 0.192 V** — `C-0090`'s own load | **0.0400 %** | 2.1541 % | 0.0973 % |
| 2 mM, 10 nm, 0.134 V | 0.0341 % | 1.3763 % | 0.0822 % |
| 2 mM, **5 nm**, 0.368 V | **0.0015 %** | 2.3330 % | 0.0094 % |
| **10 mM**, 10 nm, 0.192 V — where `C-0022`'s sign reverses | **0.0758 %** | 1.7911 % | 0.0592 % |

**Both halves of the verdict hold at all four**, including the 10 mM point where the collar is a genuine *taper* rather than an enhancement (depth `+0.4199986` at `a = 20.00`) and the 5 nm layer whose rim residual has the opposite sign again. The fit-free measure is inside 0.32 % everywhere by a factor of 4 to 214; the fitted triple is outside it everywhere, by the same mechanism everywhere.

---

## The five verification gates

Executed as **23 gate-named tests** — 13 on the cheap-bound model in `EdgeWidthDependenceTest.kt` and 10 on the **emitted result file** in `EdgeWidthDependenceResultTest.kt` — plus one in-study `check` that aborts the run.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | the effective collar width is a **length**, `pN/nm` over `pN/nm²`, reconstructed from the file's own two fields at every one of 23 solves; the rim residual depth is its line load over `interior × standoff`; the predicted depth and width are invariant under a 1000× rescaling of every load; seven entry points refuse unphysical arguments | **PASS** |
| **2 — limiting cases** | the model returns its own calibration point **exactly**; a vanishing centre-line excess makes every term width-independent to `1e−15`; an infinitely wide tile returns the untruncated moments; the tail fit recovers a synthetic exponential to `1e−6` and ignores everything outside its window; the buildable half-width is exactly `112 × 0.34 / 2`; every solve is `numericallyResolved` and every centre-line reproduces the 1-D disjoining pressure | **PASS** |
| **3 — symmetry and conservation** | **the two collar terms sum to the global momentum-flux deficit at every one of 23 solves**, asserted from the emitted file at `1e−8` — the identity the whole verdict rests on; the fit-free departure inside `C-0090`'s sensitivity at every state and refinement; the model's deficit monotone in the half-width above the standoff | **PASS** |
| **4 — numerical convergence** | **nested 1/2/4 with the sweep's 3 beside them, never 1/2/3/4**, at **both** half-widths; the fit-free departure's whole scatter is smaller than itself and the fitted triple's is larger, asserted as a test; the standoff is asserted to snap to **different** nodes at the two widths; the tail fit's own residual `4.8e−5`; a nine-point half-width sweep | **PASS** — and the *non*-convergence of the fitted triple is a **result**, asserted rather than tolerated |
| **5 — literature and upstream** | **7 reproductions, all below `1e−6`**: `C-0022`'s **−0.302887367**, **8.93928311**, **2.65822321**, **−0.593889278**, **−0.147080774** and **0.0390315779** (worst departure **1.48e−9**, through the same solver at the same half-width), and `C-0090`'s **0.0621469105** (**5.99e−8**) | **PASS** |

### The declared falsifiers, and what happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **F1** | the pipeline does not reproduce `C-0022` at its own half-width | **NO** | worst departure **1.48e−9** over six published quantities; the study aborts if it exceeds `1e−6` |
| **F2** | the solved departure falls outside the cheap bound's bracket | **YES** | the **raw** fit gives 2.1541 % against 0.1310–0.4433 %. With the standoff placed exactly the same solves give **0.0973 %, inside the bracket**, and the fit-free measure gives 0.0400 %. **The firing is the diagnosis, not a refutation of the bound** |
| **F3** | a collar term moves by more than 0.32 % | **YES** | 2.1541 % — and it is the **split** that moves: their sum moves 0.0400 % and the flatness the two terms together produce moves 0.0712 % |
| **F4** | the departure does not converge in the mesh | **YES** | 6.7765 / 12.8602 / 2.1541 / 5.5640 % over refinements 1/2/3/4 — a scatter larger than the departure. The fit-free departure over the same four is 0.0596 / 0.0377 / 0.0400 / 0.0362 % |
| **F5** | the re-evaluated flatness under the carried collar is not `C-0090`'s 0.0621469105 | **NO** | **0.0621469068**, `5.99e−8` |
| **F6** | the width-dependence is not monotone across the 12–30 nm sweep | **YES** | the snapped fitted width alternates; **the exact-standoff one is monotone and saturating at all nine half-widths**, which is what the tail model predicts |

**Four of six fired and none of them falsifies the answer** — they falsify the *instrument*, which is what a declared falsifier is for. The task was formulated as *"does the collar move"*; what it found is that three of the four numbers used to ask the question are not well posed to the precision the question is asked at.

---

## Validity range

- **MEAN FIELD, inherited whole from `C-0005` and `C-0008`**: 123–214 % of the leading term at these gaps, and for the *oppositely charged* tile-electrode pair no published result gives even the direction. A second width does not narrow it. It enters the **departure** between the two widths as a **common factor** rather than as an error on it, which is the only reason a 0.04 % answer is worth quoting inside a 214 % bracket.
- **POINT IONS.** `C-0008`'s Bikerman bracket raises `|F_es|` by +0.8 % to +56 %, one-sided and upward, on both widths alike.
- **TWO-DIMENSIONAL, hence a STRAIGHT edge.** The corner is bracketed by `C-0022`'s two mappings and **not solved**, at either width.
- **The RIM CHARGE is unsourced** and `C-0022`'s falsifier 5 fired on it: uncharged against the face density is a **1.85×** bracket on the depth. A common factor on both half-widths; not re-opened. Every solve here takes the uncharged rim, as `C-0022`'s headline does.
- **The ABSOLUTE collar is not mesh-converged at refinement 3 and `C-0022` says so** — its depth moves −0.2354 / −0.2906 / −0.3076 over 1/2/4. This claim's answer is a **departure**.
- **The FITTED triple's departure is not converged either**, and that is this claim's own finding rather than a caveat about somebody else's. See `CH-0116`.
- **The gap is filled with FREE BUFFER.** `C-0005`'s partitioning layer amplifies the 1-D force by 1.15–1.60×; whether it moves the collar ratio is not computed, at either width.
- **The flatness consequence is `C-0090`'s OWN placement re-evaluated, not a re-search.** Whether a *different* placement would win under the re-solved collar is not asked.
- **The across-helix rims carry the same collar as the along-helix ones**, because `coupling.edgeCollarPressure` applies one term on the minimum margin to all four edges. That is `C-0022`'s convention, inherited rather than fixed; the two half-spans now differ (19.04 against 20.175 nm) and this study measures what that is worth (0.04 %) rather than expressing it.
- **NOTHING HERE IS MEASURED.**

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| `ε_r`(water, 300 K) | 78 | **CITED**, as in `C-0005`/`C-0008` |
| the Manning-renormalised tile charge | −0.398665238 e/nm² | **CITED FROM `C-0005` via `C-0008`**; the tile is charge-**saturated** |
| Stern capacitance | ~20 µF/cm² | **CITED**; load-bearing for the bias mapping only |
| `C-0012`'s located operating biases | 0.192 / 0.134 / 0.368 V | **CITED FROM `C-0012`** as read by `C-0017`, never a grid bias (`CH-0007`, `CH-0016`) |
| `C-0022`'s published collar at 2 mM, 10 nm, 0.192 V | −0.302887367, 8.93928311, −0.593889278, −0.147080774 | **READ FROM ITS RESULT FILE**, keyed on concentration, gap **and bias**, and **REPRODUCED at 1.48e−9** |
| `C-0090`'s recommended placement key and its 0.0621469105 | — | **READ FROM ITS RESULT FILE** and reproduced at 5.99e−8 before anything was done to it |
| the interhelical distance and the rise | 2.69 nm, 0.34 nm | **CITED, MEASURED** (SAXS, Fischer et al. 2016) / **CITED** (Rothemund 2006) |
| the duplex radius, which sets only where the corner is cut off | 1.0 nm | **CITED** (B-DNA) |

Everything else — the exponential-tail closed form and its two anchorings, the tail fit, the 23 solves at the changed half-width, the standoff diagnostic, the nine-point sweep, the four flatness evaluations and every departure — is **derived here in code**.

## What the programme should do with this

**Nothing, and that is the result.** `C-0090`'s collar terms should be **kept as they are** and no downstream result file re-emitted:

- the movement due to the width is **0.0712 %** on the only consumed quantity, `C-0090`'s flatness;
- the collar pair is defined only up to a partition choice worth **0.368 %** on that same quantity, so replacing it with the 38.08 nm pair would trade a checked 0.07 % for an unnamed 0.37 %;
- `P-19`'s standing reason against re-emission (a result file is an **input**, and re-emitting one moves every consumer) applies unchanged.

**`C-0090`'s own error estimate is corrected, favourably.** Its validity range says *"the error is of order 5 % **in the collar term**"*, inferred from the `1/L` scaling of the total enhancement. That inference reads a scaling of the **level** as a bound on the **shape**; the level does move 4.3 % and the shape moves **0.04 %**, a hundredfold. The correct cheap argument is the ratio of the tile width to the collar's decay length, which is **exponential**, not algebraic.

## Still open — named, not answered

1. **Whether a different 34-root placement wins under the re-solved collar.** `C-0090`'s exhaustive enumeration is 163 296 members at phase 8; only its argmax is re-evaluated here.
2. **Whether the two collar terms should be re-partitioned tree-wide.** `taperFitAtExactStandoff` exists and is tested; adopting it would move `T-3b` and, through it, every study that reads `T-3b` — the **most-read result file in the repository, at 16 studies** (`C-0082`). That is a `P-19`-class decision and it is not taken here.
3. **The corner is still not solved.** `C-0022`'s two mappings bracket it at 1.8 percentage points of total force at 40 nm, widening as the tile shrinks; at 38.08 nm the bracket is reported and not narrowed.
4. **144 bp = 48.96 nm**, `C-0086`'s other admissible neighbour, is not solved. It is 22 % *larger* than §3's nominal, so its collar movement runs the other way and the tail model says it is smaller.
5. **Whether the ELECTRODE is finite.** It is macroscopic here and in `C-0022`; a counter-pad the size of the tile would have its own edge and would itself care about the width.

## Challenges

**Raises [`CH-0116`](../challenges/CH-0116-the-collar-split-is-a-mesh-node.md)** against `C-0022`'s emission of the collar as two separately meaningful load terms. It does **not** challenge the collar's sign, magnitude or width, any dishing, or the existence of the standoff.

**[`C-0090`](C-0090-buildable-raster-width.md)'s *Still open* item 1 is CLOSED**, and its validity-range clause *"`C-0022`'s collar terms are CARRIED, not re-solved … A re-solve of the 2-D Poisson-Boltzmann edge at 38.08 nm is the honest fix and is not done here"* is **discharged**: the fix is done, the carry-forward holds, and the claim's own 5 % error estimate is corrected to 0.04 %.

**None stands against this claim.** The four ways it would fail:

1. **A tree-wide re-partition of the collar** (open item 2) would change the numbers this claim compares, though not the comparison: the exact-standoff reading is already reported beside the snapped one at every state.
2. **A rim charge at the face density**, which `C-0022`'s falsifier 5 shows is a 1.85× bracket on the depth. It is a common factor on the two widths, so it would have to be *width-dependent* to matter here — and it cannot be, because the rim is the same rim.
3. **A re-search of the 34-root placement** finding a different argmax under the re-solved collar, which would replace Deliverable 4's comparison with a different pair of placements.
4. **A 3-D solve** resolving the corner, which would replace the min-margin/additive bracket and with it the force-fraction row — but not the effective collar width, which is a plane integral and is already exact in 2-D.
