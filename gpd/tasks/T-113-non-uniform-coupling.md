# T-113 — Can a NON-UNIFORM coupling stiffness buy back the edge dishing?

| | |
|---|---|
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the distribution belongs to |
| **Raised by** | [`C-0047`](../claims/C-0047-single-column-flatness.md), *"Still open"* item 2, which names it **the last unexplored axis and the only one that could attack `CH-0034`'s floor** |
| **Verification type** | **in-silico** (`C-0009`/`C-0015`'s beam-and-hinge grillage and `C-0006`'s continuum plate, under `C-0022`'s **solved** electrostatic profile read from its own result file and keyed on concentration, gap **and bias**) **+ logical** (a closed-form least-squares bound in the space of attachment *forces*, which is a rigorous lower bound on what **any** stiffness distribution can achieve, and two lines of arithmetic that price the non-uniformity before any optimiser runs) |
| **Units** | lengths **nm**, forces **pN**, stiffness **pN/nm**, pressure **pN/nm² = 1 MPa exactly**, energy **pN·nm**; `k_BT = 4.141947 pN·nm` at **300 K**, aqueous buffer with **Mg²⁺** |
| **Maturity target** | **TRL 1–3.** Model-consistent and traceable. Nothing here is measured, and the flexure motif the count belongs to is **not demonstrated** (`C-0028`, `C-0029`). |

---

## Formulate

### The question

Every attachment in this corpus is an **equal** spring.
`C-0017`'s mandate fixes the **total** coupling stiffness at `100 pN / 3 nm = 33.3333 pN/nm`
and nothing upstream requires that total to be shared equally between the paths.
Meanwhile `C-0022` has solved where the load actually is —
the rim **gains** load inside an 8.94 nm collar —
and `CH-0034` has shown that the attachment **count** axis saturates at **0.149** of the stroke and never reaches `T-5b`'s 10 %.

**Can a non-uniform distribution of the same total stiffness — stiffer springs where the load is — buy the dishing that no count can?**

### The numeric target and the acceptance predicate

**Acceptance.** The peak dishing of a **1 × 15** and a **3 × 15** grid under `C-0022`'s solved load,
as a fraction of the free-tile stroke, for

1. a **rim-stiffened** distribution swept over the stiffening ratio at constant total,
2. the **load-matched** distribution the cheap bound predicts before the solve,
3. the **best distribution found by direct optimisation** over the per-attachment stiffnesses,
   quoted with the improvement over uniform and with the distribution itself,

each stated against **two** bars: `T-5b`'s **10 %** convention, and `C-0047`'s **0.308** free tile —
the bar the uniform 1 × 15 coupling fails by 2.26×.

**And the cost**, without which a flatness number is not a design:

4. the peak per-load-path force — the attachment's own force at the mandate's stroke, `k_max·s`,
   against the **10 pN unzip** allowable (`C-0006`/`CH-0029`) and `C-0049`'s per-path secant ceiling `n·a/s`;
   the peak crossover force and peak duplex shear from the lattice, against the same allowable and the 48–65 pN shear band;
5. the per-path **thermal** force, `C-0014`'s `√(k_BT k)/n` generalised to unequal paths, because over-stiffening is priced;
6. the **lattice** run beside the **continuum plate**, with the excess quoted and its sign reported —
   a discretisation is not automatically a relaxation.

**Falsifiable form.** `flat` ⟺ peak dishing `< 0.10 ×` the free-tile stroke —
`T-5b`'s convention via `C-0015`, **a convention and not a physical threshold**.

### Geometry and sign conventions, restated rather than inherited

- `x` runs **along** the helices, `y` **across** them; the origin is the tile centre.
- `w` is positive **downward**, compressing the polymer layer (`T-5`, unchanged).
- The tile is **40.0 nm along `x`** and 15 duplexes at the SAXS-measured `d = 2.69 nm` across `y`, i.e. 40.35 nm.
- An attachment grid of `columns × rows` places `x_i = 40(i + ½)/columns − 20` and `y_j = (j − 7)·2.69` — the duplex axes exactly (`C-0026`).
- The **load** is a downward pressure of interior value `100 pN / 1614 nm²` modified by `C-0022`'s solved collar,
  read from `gpd/results/T-3b-tile-edge-load-profile.json`.
  A collar **depth is negative for an enhancement**, which is the sign `C-0022` solved.
- The **coupling** is `n` linear springs to ground whose stiffnesses **sum to `C-0017`'s 33.3333 pN/nm**.
  That sum is the only thing the mandate fixes; the *distribution* is this task's design variable.
- **Dishing** is the peak absolute departure from the area-weighted least-squares best-fit **plane** —
  piston and both tilts removed — sampled on the same 81 × 81 grid `C-0026`, `CH-0034` and `C-0047` use.
- The **free-tile stroke** is the mean deflection of the *unsupported* plate under the *uniform* load at the same foundation stiffness:
  **4.907 nm** at `k_f × 1`, `C-0006`'s, `C-0015`'s, `C-0026`'s and `C-0047`'s normaliser, unchanged.

### The upstream gotcha this task must not walk into

`gpd/results/T-3b-*.json` carries **two** solved profiles per `(concentration, gap)` — one per operating bias.
Every profile lookup here is keyed on **`(concentration, gapHeight, appliedBias)`** and errors if the triple is absent;
the bias travels into the result file with every record.

---

## Plan

### The cheap bounds, which run first and are closed forms

**Bound 1 — the reachable-dishing floor, in the space of attachment FORCES.**
The lattice is linear, so the deflection field under the solved load with springs at the `n` stations is

&nbsp;&nbsp;&nbsp;&nbsp;`w = w_free − Σ_j F_j g_j`

with `g_j` the lattice's own influence function for a unit upward force at station `j`,
and `F_j = k_j w(x_j)` the force that station's spring happens to carry.
**Dishing is affine in `F`, and every stiffness distribution — uniform, rim-stiffened, optimal, negative-if-it-could-be — produces some `F`.**
So minimising over the *whole* of `F ∈ ℝⁿ` bounds every distribution from below,
and the minimum of the root-mean-square dishing over `F` is an ordinary linear least squares:
one `n × n` Cholesky on precomputed grid fields, no optimiser, no mandate.
Because the peak of a sampled field is never below its root mean square on the same samples,

&nbsp;&nbsp;&nbsp;&nbsp;`min_k peak dishing ≥ min_F peak dishing ≥ min_F rms dishing`

and the right-hand side is four linear-algebra operations.

**Bound 2 — the price of non-uniformity, in one line of arithmetic.**
A path carrying stiffness `k_i` delivers `k_i·s` at stroke `s`, so the per-path allowable `a` caps `k_i ≤ a/s`;
against the uniform share `K/n` that is a **ratio ceiling** of

&nbsp;&nbsp;&nbsp;&nbsp;`R_max = n·a / (s·K)`

— `C-0049`'s `n·a/s` secant ceiling divided by the mandate.
At §3's *acceptable* 3 nm that is **1.5 at 15 paths** and **4.5 at 45**, on the 10 pN unzip allowable;
at §3's *desired* 10 nm it is **0.45 at 15 paths**, i.e. below one, so at 15 paths the uniform coupling is already past the allowable and no distribution exists at all.
**The non-uniformity axis is bounded before it is swept, and it is bounded by a force.**

**Cost justification.** Bound 1 is one small Cholesky against thousands of lattice solves and it decides whether the
optimisation can possibly succeed; bound 2 is arithmetic and it decides how much of the optimiser's answer is buildable.
Neither can deliver the number the predicate asks for — the predicate is a *peak* dishing at a *specified total* — so the
expensive part still runs.

### The expensive part, and how it is made affordable

A support enters the lattice stiffness as the rank-one term `k_j b_j b_jᵀ`
(`OrigamiGrillage.solveWithAnchor` already uses this for one anchor).
For `n` supports the Woodbury identity gives, **exactly**,

&nbsp;&nbsp;&nbsp;&nbsp;`q = q_free − R (D⁻¹ + BᵀR)⁻¹ Bᵀ q_free`, `R = K⁻¹B`, `D = diag(k)`,

so one factorisation of the **unsupported** lattice plus `n + 1` triangular solves prices *every* stiffness distribution
at the cost of an `n × n` solve. The dishing projector and the grid evaluation are linear too, so the dishing field of any
candidate is a linear combination of `n + 1` precomputed grids: a candidate costs microseconds instead of a Cholesky.
That is what makes a per-attachment optimisation affordable at all, and gate 5 asserts the surrogate reproduces the
assembled solve to `1e−9`.

The optimisation itself is a deterministic cyclic coordinate descent with golden-section line searches on the
log-stiffnesses, projected onto `Σk = K` (and onto `k_i ≤ a/s` where the capped variant is run), from several starts.
It is a **descent** method reporting the **best found**, never a claim of a global optimum; the gap to bound 1 is quoted
so the reader can see how much room is left.

**What the run actually did, where it differs from this plan.** The starts are uniform, load-matched, rim × 3, rim × 0.4
and the best member of the rim sweep, with 25 sweeps and a `1e−5` relative tolerance; and the **capped** problem runs
*first*, its answer joining the uncapped problem's start set — because on the first run the uncapped descent returned a
worse point than the capped one it strictly contains. The rim sweep became two-dimensional (ratio **and** collar width)
once the one-dimensional one turned out to be non-monotone, and a **minimax over all five of `C-0022`'s solved states**
was added when the design-point optimum proved worse than uniform at the compressed ones.

### What would falsify the whole approach

1. **A uniform load on a uniform foundation producing non-zero dishing on the free tile.** Wired in as a test.
2. **The Woodbury surrogate disagreeing with the assembled solve.** Then every optimisation number is void; asserted to `1e−9`.
3. **The optimiser failing to reproduce `C-0047`'s uniform numbers** at ratio 1 — 0.695 for 1 × 15, 0.218 for 3 × 15, 0.308 free.
4. **A best-found dishing below bound 1.** That is impossible by construction, so it would mean the bound or the surrogate is wrong.
5. **The 1 × 15 optimum buying more than a few per cent.** Fifteen springs on the single line `x = 0` can only reshape the
   *across*-helix profile, and `C-0047` shows the dishing there is the *along*-helix bow. If non-uniformity rescues 1 × 15,
   the bending-length reading of `C-0047` is wrong and needs a challenge.

### The five verification gates

1. **Dimensional** — dishing is a length; the system is linear, so scaling the pressure by `λ` scales every dishing by `λ`
   and leaves `dishing/stroke` invariant; the ratio ceiling `n·a/(s·K)` is dimensionless and scales as `1/s`; unphysical
   arguments throw (a non-positive stiffness, a weight vector of the wrong length, a ceiling below the mean).
2. **Limiting cases** — a uniform load on the free tile dishes exactly zero, lattice and plate; a stiffening ratio of 1
   reproduces the uniform distribution identically; the capped projection at a ceiling equal to the mean returns the
   uniform distribution exactly; an infinite ceiling is the unconstrained problem.
3. **Symmetry and conservation** — support forces plus foundation force carry the applied load; the normalisation
   conserves the mandate exactly (`Σk = K` to `1e−12`) under every projection, including the capped one; a distribution
   and its mirror image dish identically **on the plate**, and the lattice's own centro-symmetry (not mirror symmetry,
   `C-0015`) is checked rather than assumed.
4. **Numerical convergence** — **nested** subdivisions `1 ⊂ 2 ⊂ 4` (never 1/2/3/4); the sampling grid 41/81/161;
   the plate basis degree 8/10/12; and the optimiser's own convergence, as the improvement over its last sweep.
5. **Literature and upstream cross-check** — `C-0047`'s 0.695 (1 × 15), 0.218 (3 × 15) and 0.308 (free) reproduced as the
   **uniform limiting case**; `CH-0034`'s 0.149 floor at 15 × 15; `C-0049`'s `n·a/s` secant ceiling reproduced from its own
   library; `C-0014`'s `√(k_BT k)/n` reproduced at the uniform distribution by the unequal-path generalisation;
   `C-0022`'s free-tile 0.3213 through this task's own plate.

`1/(1 + (2πℓ/λ)⁴)` — the plate-on-foundation ripple transfer function — is **not** used anywhere here:
it does not apply at a free edge, and every load case in this task is dominated by the rim.

### Deliverables

- `src/main/kotlin/coupling/NonUniformCoupling.kt` — the distributions, the projection, the Woodbury surrogate, the bound, the optimiser.
- `src/main/kotlin/coupling/NonUniformCouplingStudy.kt` — the study main.
- `src/test/kotlin/coupling/NonUniformCouplingTest.kt` — the gates, written first.
- `gpd/results/T-113-non-uniform-coupling.json`.
- A claim in `gpd/claims/`, and a challenge if a standing claim is contradicted.
