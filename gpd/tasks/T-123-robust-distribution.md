# T-123 — Is ANY distribution flat at every one of `C-0022`'s solved states?

| | |
|---|---|
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the distribution belongs to |
| **Raised by** | [`C-0058`](../claims/C-0058-non-uniform-coupling.md)'s *Still open* item 2, and by [`CH-0071`](../challenges/CH-0071-the-saturation-floor-is-a-property-of-the-equal-spring-family.md)'s own second overturning condition |
| **Verification type** | **in-silico** (`C-0009`'s grillage and `C-0006`'s plate under `C-0022`'s **solved** load, on an exact multi-state Woodbury surrogate) **+ logical** (a per-state least-squares bound in the space of attachment forces, which bounds every distribution whatever, and the per-path force ceiling as arithmetic) |
| **Status** | **Formulate + Plan + Execute + Verify + File — DONE.** Claim [`C-0064`](../claims/C-0064-robust-distribution.md) |

---

## Formulate

### The question, sharpened

`C-0058` made the Gen-1 tile flat for the first time — 0.0753 of the free-tile stroke under a
one-parameter rim rule and 0.0544 under a 45-parameter optimisation, both inside `T-5b`'s 0.10 —
and then qualified it in its own Deliverable 4: **the flat design is flat at three of `C-0022`'s
five solved states**, and dishes 0.187 at the 2 nm gap where the *uniform* coupling gives 0.071.
Its minimax over all five reached only **0.1587**, marginally above `CH-0034`'s 0.149, and it says
plainly that this was *"a descent from three starts at 45 paths"* and is a **"not found"**, not a
**"does not exist"**.

So the question is not *"can a tile be made flat"* — that is answered — but

> **is there one distribution of `C-0017`'s mandated total that is flat at every state the device
> is required to be flat at, and if not, is that because the states genuinely want opposite
> distributions or because the search was thin?**

and, underneath it, a question this project's own standing discipline forces:

> **which states is the requirement owed at?** `C-0022`'s five headline states are five *gaps*.
> A quantity is not well posed without the state it is read at (six instances now: stiffness with
> a compression, variance with a bandwidth, rupture force with a loading rate, `k_es` with a gap,
> flatness count with a load case, `C-0058`'s own flatness with an operating state). **A flatness
> distribution is the seventh**, and *"flat at every one of five sampled states"* is a different
> requirement from *"flat over the range the device traverses"*.

### The acceptance predicate

**P1 — the minimax, genuinely re-run.** A real optimiser (a gradient method on a smoothed
minimax with continuation, not a coordinate descent on a nonsmooth max), many starts, over all
five of `C-0022`'s solved states, with the **worst-case** dishing as the objective. Report the
best worst-case found and whether it is inside `T-5b`'s 0.10.

**P2 — which states bind, and why.** The binding set at the optimum, and the complete
subset structure: every one of the 31 non-empty subsets of the five states optimised on its own,
so that *"the 2 nm gap is the difficulty"* and *"two states want opposite distributions"* are
distinguished by measurement rather than asserted. If two states are antagonistic, that is the
finding and it is stated as one.

**P3 — the operating range.** The minimax re-run over the states the device actually
**traverses**, with the range named and justified from `C-0018`/`C-0032`, and the discretisation
of that range checked.

**P4 — the cost and the buildability**, consumed from `C-0060` rather than re-derived: the number
of distinct stiffness **levels** the robust distribution needs, its ratio against `C-0060`'s
measured flat window `3.5 ≤ R ≤ 20`, and its quantisation against `C-0060`'s 1.0–19.1 % per base
pair. If the robust distribution needs finer granularity or more levels than `C-0060` shows are
buildable, **that is a real failure and is reported as one.**

**P5 — the load paths.** Peak per-path force at §3's 3 nm stroke against the 10 pN unzip
allowable and `C-0049`'s `n·a/s` ceiling (which tightens as `1/s`), the peak crossover force, the
peak duplex shear, and `C-0014`'s per-path thermal force `√(k_BT k)/N` generalised to unequal
paths.

### Units and conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, pressure **pN/nm² = 1 MPa exactly**;
  `k_BT = 4.141947 pN·nm` at **300 K**, aqueous buffer with **Mg²⁺**.
- `x` runs **along** the helices, `y` **across** them; the origin is the tile centre. `w` is
  positive **downward**, compressing the polymer layer (`T-5`).
- **Dishing** is the peak absolute departure from the area-weighted least-squares best-fit
  **plane** — piston and both tilts removed — on the same **81 × 81** grid as `C-0026`,
  `CH-0034`, `C-0047`, `C-0058` and `C-0060`.
- The **free-tile stroke** is the mean deflection of the *unsupported* plate under the *uniform*
  load at the same foundation stiffness — **4.90731 nm**, the unchanged normaliser.
- **Flat** means peak dishing below **10 %** of that stroke — `T-5b`'s convention via `C-0015`,
  **a convention and not a physical threshold**.
- A **collar depth is negative for an enhancement**, which is the sign `C-0022` solved; its rim
  residual term may exceed one in magnitude, which means the load *reverses sign* inside the
  collar.
- The **coupling** is `n` linear springs to ground whose stiffnesses **sum** to `C-0017`'s
  33.3333 pN/nm. The sum is the mandate; the **distribution** is the design variable.
- A **state** is a `(concentration, gap, bias)` triple of `C-0022`'s solved profiles. Every
  lookup is keyed on all three — `gpd/results/T-3b-*.json` carries two profiles per
  `(concentration, gap)`, one per operating bias, and keying on two of the three silently takes
  whichever is listed first.

---

## Plan

### The cheap bound, which runs first, and its falsifier

Dishing is **affine in the attachment forces** and every stiffness distribution produces *some*
force vector, so for each state separately the least-squares minimum over the whole of `ℝⁿ` —
no mandate, no positivity, no relation between a force and a stiffness — is a rigorous lower
bound on the peak dishing of **every** distribution at that state (the peak of a sampled field is
never below its own root mean square). The states decouple under that relaxation, so

&nbsp;&nbsp;&nbsp;&nbsp;`min_k max_s peak_s(k) ≥ max_s min_F rms_s(F)`

is a rigorous lower bound on the **minimax**, at the cost of one `n × n` Cholesky per state.

> **Declared falsifier.** If `max_s` of the per-state floor exceeds `T-5b`'s 0.10, then **no
> distribution is flat at every state**, the answer is a proven *"does not exist"* rather than a
> *"not found"*, and the expensive optimisation is unnecessary. `C-0058`'s single-state floor was
> 0.0027 at 3 × 15, so the bound is expected to be loose and to **not** fire — and saying so in
> advance is the point of running it.

### Why a real optimiser, and which one

`C-0058`'s optimiser is a **cyclic coordinate descent on a nonsmooth maximum**. That is the one
class of problem coordinate descent is known to fail on: a max of smooth functions has kinks
along the switching surfaces, and a coordinate method stalls on a kink at a point that is not
stationary, because no single coordinate direction descends even though a combination does. A
minimax reported from a coordinate descent is therefore not evidence of a floor.

The method here is:

1. **Smooth the max** by log-sum-exp over the `5 × 81 × 81` signed dishing samples, with
   continuation `μ → 0`;
2. **Analytic gradients** through the Woodbury solve — `∂F/∂k_j = (F_j/k_j²) A⁻¹ e_j` with
   `A = M + diag(1/k)`, so a gradient costs one extra triangular solve per state on the
   factorisation the objective already built;
3. **Nonlinear conjugate gradients** (Polak-Ribière with restarts) on the **log-weights**, in
   which the mandate is enforced exactly by a softmax and the positivity constraint disappears;
4. a **polish** stage on the true, unsmoothed objective using `C-0058`'s own optimiser, so the
   two searches are composed rather than compared;
5. **many starts** — the uniform coupling, a load-matched distribution per state, a 30-member rim
   family, every single-state optimum, and a deterministic seeded log-normal ensemble.

Cost justification: the influence functions depend on the model and the stations and **not on the
load**, so one multi-state surrogate costs `n + S` load cases where `C-0058` paid `S(n + 1)` —
50 solves against 230 at five states — and an objective-plus-gradient is then one 45 × 45
Cholesky and two dense passes over the sampled fields, i.e. milliseconds. That is what makes 31
subsets and ~60 starts affordable at all; an assembled solve per candidate would be weeks.

### What would falsify this approach

- **The multi-state surrogate disagreeing with `C-0058`'s `InfluenceSurrogate`**, which is an
  independent implementation of the same superposition. Asserted to `1e-12`.
- **The analytic gradient disagreeing with a central finite difference**, which would invalidate
  every CG step. Asserted to `1e-6` relative.
- **The real optimiser returning a worse point than `C-0058`'s published 0.1587**, which would
  say the smoothing or the continuation is broken rather than that the coordinate descent was
  adequate.
- **A best-found below the cheap bound**, which would say the bound is wrong.
- **A uniform load on the free tile dishing anything but zero**, the standing free falsifier.

### What this task does not do

It does not free the **placement**: every station here is `C-0026`'s grid, exactly as in
`C-0058`, and [`CH-0074`](../challenges/CH-0074-the-flat-distribution-lives-on-stations-no-placement-supplies.md)
stands over all of it — the flat distribution lives on stations no placement claim supplies.
`T-125` is concurrently sweeping whether a phase sweep of `C-0055`'s upward arm array supplies
them. **Every verdict here is conditional on that**, and the claim says exactly what `T-125`
would have to find to change it.

---

## Execute

- `src/main/kotlin/coupling/RobustDistribution.kt` — the multi-state surrogate, the analytic
  gradient, the smoothed minimax, the conjugate-gradient search, the level quantisation.
- `src/main/kotlin/coupling/RobustDistributionStudy.kt` — `main`, emitting
  `gpd/results/T-123-robust-distribution.json`.
- `src/test/kotlin/coupling/RobustDistributionTest.kt` — the gates, written first.

## Verify

The five gates, as executable tests; see the claim's gate table.

## File

Claim [`C-0064`](../claims/C-0064-robust-distribution.md).
