# T-221 — Which wall is a planar coupling criterion owed at, the bare duplex or the charge-saturated gap face?

| | |
|---|---|
| **Leaf** | `A7.4`, consumed by `A2.2` and `A8.2` |
| **Priority** | **MEDIUM** — the one thing [`C-0137`](../claims/C-0137-beyond-mean-field-gap.md) explicitly does not settle |
| **Verification type** | **logical** (a scale-covariance identity that reduces the criterion to one closed form, and a 2 × 2 decomposition of the disputed factor) **+ in-silico** (both of Kanduč et al.'s branches evaluated over the whole candidate wall family and the whole admissible asymmetry range) **+ literature** (the criterion's own derivation, read directly, for what its variables are defined to be) |
| **Depends on** | `C-0137` (which raised it), `C-0005` / `T-6` (the coupling parameters, the saturated charge, the standing band structure), `C-0008` (the sign of the gap force), `gpd/data/T-50-beyond-mean-field-literature.md` (rows 25–30) |
| **Raised by** | [`C-0137`](../claims/C-0137-beyond-mean-field-gap.md) §`P4` |

---

## Formulate

### The question, as `C-0137` left it

> *"Evaluated here, the repulsive criterion gives a bound of **14.43** against `Ξ = 24.00` at the **bare duplex** wall (**FAILS**) and **2.80** against `Ξ = 1.455` at the **charge-saturated gap-facing** wall (**PASSES**). The two readings are `16.5×` apart in `Ξ` and land on opposite sides of one inequality. `CLAUDE.md` says to read `Ξ` from the duplex cylinder — *for the local coupling* — while the criterion is written for a **planar wall bounding the gap**. Which wall it is owed at is queued, not answered."*

The criterion is Kanduč, Trulsson, Naji, Burak, Forsman & Podgornik (arXiv:0905.3851) **Eq. (64)**:

&nbsp;&nbsp;&nbsp;&nbsp;`Ξ < D̃ / ln D̃`, &nbsp; `D̃ = D/μ`, &nbsp; `μ = e₀/(2π l_B q σ₁)`, &nbsp; `Ξ = q² l_B/μ = 2π q³ l_B² σ₁/e₀`.

### The deliverable

A **stated rule with its ground** for which `σ` enters `Ξ` and `μ` — or the statement that both readings must be carried, which is this repository's standing discipline for an unresolved convention and is a full answer if that is what the physics says.

Plus, because a convention question is only worth an iteration if something depends on it: **whether the 16.5× moves any verdict this programme depends on.**

### Locked units and sign conventions

- `z` normal to the electrode, positive **away** from it; **the electrostatic gap IS the layer height** (`C-0012`).
- `σ` is always a **magnitude** in `e/nm²`; `q = 2` for `Mg²⁺`; `l_B = 0.7141 nm` at `T = 300 K`, `ε_r = 78`.
- `μ ≡ μ_GC = 1/(2π q l_B σ)` in nm; `Ξ = q² l_B/μ`; `D̃ = D/μ`, `D` the wall-wall separation in nm.
- Kanduč's `σ₁` is the **larger-magnitude** wall by his own Eq. (3) (`σ₁ + σ₂ < 0`, `σ₂ > σ₁`), and `ζ = σ₂/σ₁`; oppositely charged walls are `−1 < ζ < 0`.
- `Ξ` and `μ` are properties of **one** surface, `σ₁`.
- A criterion is quoted **with its wall and with its gap** — the discipline `CLAUDE.md` applies to a stiffness and a compression.

### The acceptance predicates

- **`P1`** — the criterion's dependence on the disputed convention is isolated in closed form, with no solve, and the threshold `σ*(D)` at which it changes verdict is emitted.
- **`P2`** — the disputed factor is decomposed over the two axes it bundles (**geometry**: cylinder ↔ smeared plane; **charge convention**: bare ↔ renormalised), and it is stated which axis carries the verdict.
- **`P3`** — a rule is stated, with a ground that is a property of the criterion's own derivation rather than a preference.
- **`P4`** — both of Kanduč's branches are evaluated (Eq. 64, repulsive; Eq. 65, attractive), because this device's walls are oppositely charged and `C-0008` solves its gap force as an **attraction**.
- **`P5`** — whether any verdict this programme depends on moves, measured rather than asserted.
- **`P6`** — `P-6`'s ceiling-and-threshold: what the answer would have to be for the verdict to change, and whether any published number reaches it.

---

## Plan

### The cheap bound that must run first

`Ξ` and `D̃` are **both linear in `σ₁`**, so their ratio carries no convention at all:

&nbsp;&nbsp;&nbsp;&nbsp;`Ξ/D̃ = (q² l_B/μ)/(D/μ) = q² l_B / D`, exactly.

So Eq. (64) — `Ξ ln D̃ < D̃` — is **equivalent** to

&nbsp;&nbsp;&nbsp;&nbsp;**`ln(D/μ) < D/(q² l_B)`**,

i.e. the wall convention enters the criterion **only as `ln σ₁`**. The 16.5× the task is about is `ln 16.5 = 2.80` in the criterion's own variable, against a threshold distance of order one. That is one line of algebra, it runs before anything is compiled, and it says at once (a) that the naive *"16.5× apart"* overstates the disagreement by `16.5/2.80 = 5.9×` in the variable that decides, and (b) whether it nevertheless still flips the verdict.

It also gives the threshold in closed form, with no bisection:

&nbsp;&nbsp;&nbsp;&nbsp;`μ*(D) = D e^{−D/(q² l_B)}`, &nbsp; `Ξ*(D) = (q² l_B/D) e^{D/(q² l_B)}`, &nbsp; `σ*(D) = 1/(2π q l_B μ*)`.

### The 2 × 2 that the question's own wording bundles

*"Bare duplex"* versus *"charge-saturated gap face"* moves **two** things at once. Separate them and sweep the square:

| | **bare** | **renormalised** |
|---|---|---|
| **duplex cylinder** | `σ = τ/(2πR)` | Manning-surviving `σ` |
| **smeared gap face** | `σ_face = ρt/2` (`CLAUDE.md`'s Gauss's-law partition), and the single-helix-layer reading | `C-0005`'s saturated `σ_eff = κ/(π l_B q)` |

`CLAUDE.md`'s *"a two-factor move has a TOTAL and an INTERACTION, never an X term and a Y term"* applies verbatim: measure the square, do not subtract.

### Why the branch has to be checked too

Eq. (64) is derived for `p₀ > 0`. This device's walls are **oppositely charged** and `C-0008` solves the gap force as an **attraction** at every operating state, so the applicable criterion is Kanduč's Eq. (65), `Ξ < (ζ²/|f(ζ)|) e^{−2ζD̃}`. `C-0137` and `BeyondMeanFieldGap.kt` both dispose of it in one clause — *"the right hand side here is exponentially large"* — and neither evaluates it. **The exponential is at fixed `ζ`, and this programme has never measured `ζ`.** So the plan evaluates Eq. (65) over the whole admissible `ζ` and takes its **infimum on the branch's own domain**, which needs the branch boundary; that follows from Eq. (18) in the `α → 0` limit as `D̃* = (1+ζ)/|ζ|` and costs no solve either.

### Method justification against cost

Everything above is closed form. No Poisson-Boltzmann solve is needed and none is run: the disputed quantity is an **input** to a published inequality, not an output of a field. The only numerical work is a bisection for `f(ζ)`'s branch continuity gate, a scan for the infimum of Eq. (65), and a reproduction of `C-0005`'s five one-loop deviations, all of which are milliseconds. **The expensive route — a primitive-model Monte Carlo — would not answer this question at all**, because it would have to be *told* which wall to build.

### What result would falsify this approach

If the criterion's verdict turned out to depend on the **geometry** axis rather than the charge-convention axis, then the question `C-0137` asked (*cylinder or plane*) would be the right question and this decomposition would be answering a different one. If Eq. (65)'s infimum on its own branch turned out to be exponentially large after all, then the whole straddle would be an artefact of evaluating the wrong branch and no rule would be needed. Both are checked and both are declared falsifiers.

### Declared falsifiers

| | statement | what it would mean |
|---|---|---|
| **`F1`** | the **geometry** axis flips Eq. (64)'s verdict at some candidate and gap | `C-0137`'s framing is the right one and the decomposition is wrong |
| **`F2`** | the **charge-convention** axis does **not** flip the verdict | the straddle is not what it was reported to be |
| **`F3`** | Eq. (65)'s infimum over its own admissible `ζ` exceeds Eq. (64)'s bound by more than `2×` | *"exponentially large"* is operative and the branch, not the wall, is the answer |
| **`F4`** | `C-0005`'s published `123–214 %` is **not** reproduced by the bare duplex reading | the corpus has not in fact committed to a wall, and the precedent argument fails |
| **`F5`** | adopting the saturated reading moves `C-0017`'s stability margin by more than 2 %, i.e. past the band `C-0137` measured for the same factor | the convention reaches an answer and not only a flag |
| **`F6`** | the renormalised readings sit inside Eq. (64)'s own asymptotic domain `D̃ ≫ 1` | their PASS is entitled and the domain argument fails |

### Verification gates

1. **Dimensional** — `Ξ`, `D̃`, `ζ`, `f(ζ)` and every margin dimensionless; `μ`, `D`, `σ*` in nm and `e/nm²`.
2. **Limiting cases** — `Ξ/D̃` independent of `σ` to the last ulp; `D̃/ln D̃` minimal at `D̃ = e` with value `e`; `f(ζ)` continuous across its own branch point `ζ = −√2/2` where Eq. (61)'s `arctan` form and Eq. (62)'s `artanh` form are each other's analytic continuation; `D̃*(ζ) → ∞` as `ζ → 0⁻` and `→ 0` as `ζ → −1`.
3. **Symmetry and conservation** — the closed-form threshold `Ξ*(D)` agrees with a bisection on `D̃/ln D̃ − Ξ`; the Manning-renormalised cylinder's `μ_GC` equals the helix radius **exactly**, an identity that carries no rise, no Bjerrum length and no valency.
4. **Numerical convergence** — the infimum of Eq. (65) over `ζ` on three nested grids; the `f(ζ)` branch continuity to the tolerance the two forms support.
5. **Literature cross-check** — `C-0005`'s five published one-loop deviations reproduced at the bare duplex wall; `T-6`'s own emitted `loopExpansionValidityGap` reproduced; the criterion quoted verbatim from the paper.
