# T-1c — Layer response from a crossover-valid free energy, not a fixed osmotic exponent

| | |
|---|---|
| **Leaf** | `A2.1` (`../../../simulation-task-map/knowledge/program_tasks_feynman_path.csv`) |
| **Problem definition** | §6 task 1; premises in §2; parameters in §3; questions §4(a), §4(b) |
| **Raised by** | [`CH-0001`](../challenges/CH-0001-semidilute-premise.md), outstanding item 1 |
| **Also resolves** | [`CH-0003`](../challenges/CH-0003-blob-stack-height.md), raised against `C-0001` by `C-0004` mid-iteration |
| **Supersedes** | `T-1b` (the free-energy functional), which is absorbed here — this task needs a free energy anyway |
| **Verification type** | in-silico (analytic derivation + numeric minimisation), closed against published measurement |
| **Maturity** | TRL 1–3. Model-consistent and traceable. **Not measured.** |
| **Status** | Executed, verified, filed as claim [`C-0003`](../claims/C-0003-crossover-valid-layer-response.md), raising [`CH-0002`](../challenges/CH-0002-corrections-do-not-all-soften.md) |

---

## Formulate

### The question, as a numeric target

`C-0001` produced a stiffness and a stroke from the de Gennes scaling form with a *chosen* osmotic exponent,
and obtained the chain length `N` by inverting the Alexander-de Gennes height relation `L₀ = N a^(5/3) σ^(1/3)`.
`CH-0001` showed that relation is itself a semidilute blob result whose premise fails for this layer,
so **the chain length under every number in `C-0001` rests on an unestablished premise**, not merely the exponent.

Produce `L₀(N, σ)`, `N(L₀, σ)`, `P(h)`, `k(h)` and the stroke at the §3 target force
from a free energy that is valid **across** the dilute→semidilute crossover,
with the height relation derived rather than inherited,
and report the spread between the admissible free energies as the uncertainty.

### Acceptance predicate

`TASKS.md` states it as:

> Stiffness and stroke re-derived with a crossover-valid free energy rather than a fixed osmotic exponent;
> the Alexander-de Gennes height relation `L₀ = N a^(5/3) σ^(1/3)` either justified at φ/φ# ≈ 1 or replaced;
> `N(L₀)` no longer resting on the failed premise.

Tightened here, and discharged only when **all six** hold:

1. The layer's interaction free energy is obtained from the **measured** PEG/water thermodynamics of `C-0002`
   by an explicit, checkable operation — not by selecting an exponent — and the operation is verified
   by differentiating back to the measured pressure.
2. At least **two independent, measurement-anchored** interaction free energies are carried,
   one valid below the crossover and one above, and the spread between them is reported as the uncertainty
   on every headline number.
3. `L₀(N, σ)` is a **free-energy minimum with a derived prefactor**, containing no convention factor of unity,
   and the elasticity it is minimised against uses parameters that are independent of the failed premise.
4. The three mutually inconsistent height exponents (`σ^(1/3)` mean-field, `σ^(5/13)` des Cloizeaux,
   `σ^(1/3)` blob) are **resolved by exhibiting which free energy produces which**, as an executable test,
   rather than by choosing one.
5. `N(L₀)` is obtained by inverting **that** minimum, and the difference from `C-0001`'s `N` is reported.
6. Every premise that is still unmet is stated **with a number attached**, not as a caveat in prose.

### Units, locked

SI, scaled: lengths nm, forces pN, energies pN·nm, pressures pN/nm² (`= 1 MPa` exactly),
stiffness pN/nm (`= 1 mN/m` exactly).
`k_BT = 4.142 pN·nm` at **T = 300 K**, medium **aqueous buffer** (2–10 mM MgCl₂, not yet entering this task).
Energies also reported in eV via `1 eV = 160.2177 pN·nm`; `k_BT(300 K) = 25.85 meV`.

Osmotic virial coefficients are quoted in the convention `Π/(RT) = c/M + A₂c² + A₃c³`,
`c` in `g/cm³`, **no factor of two** — the activity-coefficient convention `a₂₂ = 2 M A₂` differs by exactly that
and is the single most likely way to get this wrong.
`A₂` in `mol·cm³/g²`, `A₃` in `cm⁶·mol/g³`.

A **volume fraction** is always the physical one, `φ = N σ v₀ / h`, per `C-0002`.

### Geometry and sign conventions, fixed before deriving

Restated verbatim from `T-1`, because a downstream task must not have to look them up:

- `z` normal to the electrode, positive away from it, origin at the electrode surface
  (top of the dielectric if one is present).
- Chains grafted at `z = 0`; the layer occupies `0 < z < L`.
- The tile is a **rigid, non-adsorbing wall** at height `h`. Compression means `h < L₀`.
- The disjoining pressure `P` is positive when the layer pushes the tile along `+z`.
- Stiffness `k = −∂F/∂h = −A ∂P/∂h`, positive for a restoring layer.
- The layer height is the **independent** variable and the chain length `N` follows from it,
  because §3 specifies heights (5 / 7 / 10 nm) and leaves `σ` open.

One convention is **added** here, and it is the substantive one:

- The layer is **grafted**, so it has no chain translational entropy,
  and the van't Hoff limb of a bulk equation of state is removed before that equation of state is used.

### What is deliberately excluded

No electrostatics, no ion partitioning, no poroelasticity, no tile compliance.
This is the purely mechanical restoring term. Everything else is `T-3`, `T-4`, `T-6`, `T-7`, `T-5b`.

---

## Plan

### The chain of reasoning, and where each link can break

**Step 1 — get a free energy, not a pressure.**
`C-0002` adopted the measured `Π(φ) = (k_BT/v₀)[φ/N + αφ^(9/4)]`.
Integrating it through `f(φ) = φ ∫ Π(φ')/φ'² dφ'` gives

&nbsp;&nbsp;&nbsp;&nbsp;`f(φ) = (k_BT/v₀)[ (φ lnφ)/N + (4α/5) φ^(9/4) ]`

whose first term is the **translational entropy of whole chains**.
A grafted layer does not have it. Removing it leaves an interaction free energy whose osmotic pressure is
`Π_int = α(k_BT/v₀)φ^(9/4)` — exponent exactly 9/4 at *every* density.

If that is right it **partly dissolves** `CH-0001`: the local exponent `m_eff = 1.66–1.92` that `CH-0001`
carried into the brush pressure law is a property of the *bulk* pressure, and the term that bends it away
from 9/4 is precisely the one grafting removes. `CH-0001` names this as the way it could fail.
**Falsifiable:** differentiate the constructed free energy back and check it reproduces the measured pressure.

**Step 2 — but the interaction limb is not measured below `φ#`.**
The fitted `αφ^(9/4)` is a crossover *interpolation*. Below `φ#` the measured pressure is dominated by the
van't Hoff limb, so the data constrain the interaction term only weakly there.
Physically it must cross over from an unscreened two-body form `(B/2)(k_BT/v₀)φ²` to the screened `φ^(9/4)`.
**Where** it crosses is set by `B`, and `P-3` could not supply `B`:
the adopted equation of state is non-virial by construction and yields neither `A₂` nor `χ`.

So `B` has to come from somewhere else, and there are two routes:
matching the two limbs at `φ#` (a *construction*, with no independent content),
or a published `A₂` for PEG in water (a *measurement*).
Per the research practice in `CLAUDE.md` the second is worth the search cost, and it is where this iteration
spent most of its effort. Both are carried, so the cost of the construction is visible as a number.

**Step 3 — replace the Alexander-de Gennes height, do not re-use it.**
Three height relations are in circulation and they disagree:

| route | height |
|---|---|
| two-body/mean-field free energy, minimised against Gaussian elasticity | `h ∝ N σ^(1/3)` |
| des Cloizeaux free energy, minimised against Gaussian elasticity | `h ∝ N σ^(5/13)` |
| Alexander blob construction, `ξ = s`, string of blobs | `h ∝ N σ^(1/3)` |

The disagreement is **not** in the interaction term. It is in the **elasticity**:
the blob construction implicitly uses blob elasticity, `F_el ∝ h²/((N/g)ξ²)`, and with that elasticity the
des Cloizeaux free energy reproduces `σ^(1/3)` exactly. So the question "which height relation" reduces to
"which elasticity", and *that* is a checkable statement about the material:
Gaussian elasticity on the Kuhn scale is right when the chain is **not swollen**,
i.e. when it is shorter than a thermal blob.
That is a number this project can compute — `g_T = (b³/v)²` — once `B` is known from step 2.

**Falsifiable in advance:** if the chain turned out to contain many thermal blobs, the blob elasticity
would be the right one, `σ^(1/3)` would be restored on its own terms, and this whole step would collapse
back to `C-0001`'s height relation with a corrected prefactor.

**Step 4 — the cheap bound before the expensive calculation, per §5.**

| method | cost | role |
|---|---|---|
| **Alexander box profile**, closed form for a power-law interaction | microseconds | **the cheap bound, run first** |
| **Generalised Milner-Witten-Cates strong stretching**, numeric | seconds | the calibrating calculation |
| Scheutjens-Fleer numerical SCF | hours | not run — see below |
| Coarse-grained MD (Martini PEG + explicit Mg²⁺) | days-weeks of CPU | not run — see below |

The box profile is closed form and gives `L₀`, `N(L₀)`, `P(h)` and `k(h)` for any power-law interaction
in one line each. It is known to *underestimate* the height, because it forbids the layer a diffuse edge.

The strong-stretching theory is the expensive one and it is still seconds:
the self-consistent potential is parabolic, `μ(φ(z)) = λ − A z²`, because the parabola is a property of the
**chain elasticity and the equal-time condition on chain trajectories**, not of the interactions —
so the same potential admits an arbitrary local interaction free energy.
Invert numerically for `φ(z)`, fix `λ` by conserving `∫φ dz = N σ v₀`, and take the wall pressure from the
contact-value theorem `P(h) = Π_int(φ(h))`.
That gives the whole response with **no semidilute premise anywhere except inside `Π_int`**,
which is exactly what `T-1c` was raised to achieve.

**Why not Scheutjens-Fleer or MD.**
Both would buy a real density profile, and neither would buy a better *interaction*:
they would be run with a Flory `χ` or a Martini bead parameterisation, and the answer's uncertainty here is
dominated by the interaction free energy at `φ ≈ 0.03`, which two published osmometry datasets already
disagree about by ~50%. Simulating past a measurement disagreement is spending CPU to add a third opinion.
This is the same judgement `P-3` recorded and it is recorded again because it is the load-bearing one.
They become worth running when — and only when — the profile uncertainty, not the interaction uncertainty,
is what binds. It is not: see Verify.

### What would falsify this approach

Stated in advance, before the run:

1. **The reconstructed free energy failing to differentiate back to the measured pressure.**
   Then the Legendre inversion is wrong and nothing downstream of it is worth reading.
2. **The chain containing many thermal blobs.** Then Gaussian elasticity on the Kuhn scale is the wrong
   elasticity, blob elasticity is the right one, and `L₀ ∝ N σ^(1/3)` stands on its own terms — the
   Alexander-de Gennes relation would be *justified*, not replaced, and the task's answer would be
   "`C-0001`'s form was right, only its prefactor was a convention".
3. **The two profile models disagreeing by more than the two interaction laws do.** Then the profile, not
   the interaction, is what binds, the analytic route has to yield to numerical SCF, and this iteration's
   cost justification is wrong.
4. **The generalised strong-stretching solver failing to reproduce the standing `MilnerWittenCates`
   implementation in the two-body limit.** Then the generalisation is not a generalisation.
5. **The corrections not all running the same way.** `CH-0001` asserts they do — "every correction found here
   makes the layer *softer*" — so if any correction found here makes it *stiffer*, `CH-0001`'s
   "`C-0001`'s strokes are lower bounds" has to be challenged rather than inherited.

Outcome:

- **(1) did not fire.** The reconstructed free energy differentiates back to the measured pressure to 1e-6.
- **(2) did not fire — it fired in the opposite direction, and hard.** The chain contains **0.06** thermal
  blobs, not many. Gaussian Kuhn elasticity is the right elasticity and the Alexander-de Gennes relation is
  replaced, not justified. `CH-0003`, which arrived mid-iteration from `T-7`, says the same thing from the
  layer's geometry — the blob stack is `(Σ/π)^(5/6) = 1.47` deep — and the two are independent.
- **(3) FIRED.** Whether a design window exists at 10 nm is decided by the **profile** model, not by the
  interaction: empty under both box models, `[0.018, 0.061] nm⁻²` under the strong-stretching ones, while the
  two interaction laws differ by only 1.45× in `Π_int`. The cost justification for staying analytic therefore
  holds for the *response* but **not** for `T-2`'s window question, and the claim says so: a numerical SCF
  density profile is now the thing worth buying, and a better interaction law is not.
- **(4) did not fire.** The generalised solver reproduces the standing `MilnerWittenCates` implementation to
  1e-7 in the two-body limit.
- **(5) fired**, and `CH-0002` is the consequence.

---

## Execute

Code, in the `brush` package, tests written first:

- `src/main/kotlin/brush/InteractionFreeEnergy.kt` — the interaction free-energy family
  (`PowerLawInteraction`, `AdditiveInteraction`), the Legendre transform in both directions,
  the removal of the translational term from the measured equation of state,
  the virial-coefficient conversions, and the thermal-blob diagnostic.
- `src/main/kotlin/brush/GraftedLayer.kt` — `GraftedChain` on the measured Kuhn parameters,
  `AlexanderBoxLayer` (the cheap bound) and `StrongStretchingLayer` (the generalised MWC solver),
  both minimising the *same* free energy over different profile families.
- `src/main/kotlin/brush/CrossoverLayerStudy.kt` — the study entry point.

Tests: `src/test/kotlin/brush/InteractionFreeEnergyTest.kt`, `src/test/kotlin/brush/GraftedLayerTest.kt`.

```shell
./gradlew test
./gradlew study -Pstudy=brush.CrossoverLayerStudyKt
```

Result: [`../results/T-1c-crossover-valid-layer-response.json`](../results/T-1c-crossover-valid-layer-response.json) —
183 design points (3 layer heights × 61 log-spaced grafting densities from 0.002 to 1.0 nm⁻²) × 6 models,
plus premise diagnostics, the like-for-like comparison against `C-0001`, and the stroke windows.
Deterministic: no timestamp.

### The literature search, and what it cost

Most of this iteration's wall-clock went on `A₂`, because `CLAUDE.md` forbids taking it from a search summary
and the canonical source (Hasse, Kany, Tintinger & Maurer, *Macromolecules* **28**:3540, 1995) is paywalled.
What was actually read, and how:

| source | what was read | status |
|---|---|---|
| Li, Turesson, Haglund, Cabane & Skepö, *Polymer* **80**:205 (2015), Table I | `A₂ = 2.34e-3` at `Mn = 20 400`, membrane osmometry, 25 °C, milliQ water | **read directly** |
| Cohen & Highsmith, *Biophys. J.* **73**:1689 (1997), Table 1 + Eqs. 2–4 | `A = 2.1e-4` at 20 kDa in their `g/dl` convention, `⇒ A₂ = 2.1e-3` | **read directly** (PMC1181067) |
| Kany (Diss. 1998) via Grünfelder, Diss. Kaiserslautern 2002, Table 4.3 | `A₂ = 1.715e-3`, 25 °C, isopiestic | **read directly** |
| Hasse et al. (1995) via Shvets, arXiv:2010.08110, Table 2.3 | `A₂ = 1.9e-3` at `Mw = 6902`, 25.2 °C | **secondary** — flagged |
| Fedicheva, Diss. Kaiserslautern 2007, Eqs. 2-19, 2-25, 2-34 | the exact Maurer-group convention, and the `a₂₂ = 2 M A₂` factor-of-two trap | **read directly** |
| Pedersen & Sommer, *Progr. Colloid Polym. Sci.* **130**:70 (2005) | `χ = a + b/T`, `a = 1.156`, `b = −235.3 K` ⇒ `χ(298 K) = 0.367`, SAXS on PEG-4600/D₂O | **read directly** |

Adopted: `A₂ = 1.9e-3 mol·cm³/g²`, `A₃ = 2.0e-2 cm⁶·mol/g³`. Spread across the four `A₂` values: ±15%.
The molar-mass dependence is reported by four groups as `M^(−0.20)`, `M^(−0.32)`, saturating, and absent —
over 2–20 kDa the total variation is inside the ±15%, so it is treated as constant and said to be so.

---

## Verify

All five gates, executed as tests rather than asserted in prose. Test names carry the gate they discharge.

### Gate 1 — dimensional consistency

- `Π = φ μ/v₀ − f` holds to 1e-12 for every interaction — the Legendre transform the whole file rests on.
- The des Cloizeaux interaction reproduces `ScalingEquationOfState.desCloizeauxPressure` **exactly**,
  so the free energy and the standing `C-0002` equation of state cannot drift apart.
- `μ` is an energy per monomer: the two-body potential is exactly `B k_BT φ`.
- Pressure × area reduces to a load in pN; stiffness per area × area to pN/nm.
- **`L₀` is exactly linear in `N`** for both profile models and any pure power-law interaction —
  which is what makes `N(L₀)` an exact inversion rather than an iteration.

### Gate 2 — limiting cases

- Every model exerts exactly zero pressure at its own equilibrium height, and pushes the tile away
  and stiffens monotonically down to `h/L₀ = 0.2`.
- **The three disputed height exponents are each reproduced by the free energy that implies them**:
  the two-body interaction gives `σ^(1/3)` and the des Cloizeaux interaction `σ^(5/13)`, to 1e-9,
  in *both* profile models. That is acceptance item 4, discharged as a test rather than an argument.
- The box profile opens with **finite** stiffness at first contact and the strong-stretching profile with
  **none** — `C-0001`'s surprise `S-1` survives the change of free energy intact.
- **The generalised strong-stretching solver reproduces the standing `MilnerWittenCates` implementation
  exactly** — height, wall density, pressure and stiffness, to 1e-7 — when given a two-body interaction and
  the Kuhn parameters collapsed onto the old single monomer size. Falsifier 4 did not fire.

### Gate 3 — symmetry and conservation

- The strong-stretching profile conserves the grafted coverage `∫φ dz = N σ v₀` to 1e-8 at every compression.
- **`−∂F/∂h = P(h)` for every model**, checked against a free energy assembled *independently* from the
  profile. This is the contact-value theorem `P = Π_int(φ(h))` verified thermodynamically rather than
  assumed: normal stress must be uniform through the layer, and at the wall the only chains present have
  their free ends there and so carry no tension. The elastic part of `F` is computable from the profile alone
  through the identity `F_el = ∫ A z² φ(z)/v₀ dz`, which removes the need to invert an end distribution.
- The work done compressing the layer equals the free energy it gains, to 1e-4 by quadrature.
- `Π'(φ)/μ'(φ) = φ/v₀` exactly, for every interaction — the identity the strong-stretching stiffness is
  written on, asserted rather than assumed.
- Equipartition `σ_RMS = sqrt(k_BT/k)` is reachable from the same stiffness the actuation uses — the `T-8` hand-off.

### Gate 4 — numerical convergence

- Every model's analytic stiffness matches a central difference of its own pressure to 1e-5,
  at three compressions.
- The closed-form equilibrium height matches the height solved numerically from the coverage constraint
  to 1e-8, for both profile models — two genuinely different routes, one through a Beta function
  (Lanczos `logΓ`) and one through Simpson quadrature and bisection.
- The profile quadrature converges with the panel count, and the production setting (1024 panels,
  in the `z = h sinθ` variable that removes the outer-edge singularity) is converged to below 1e-8
  against an 8192-panel reference — four orders of magnitude below the ±15% spread on `A₂`.
- The height under a load inverts the pressure law to 1e-8.
- The `λ` solve is a safeguarded Newton iteration on a **proved** bracket
  `[μ(Nσv₀/h), μ(Nσv₀/h) + A h²]`, not a guessed one.

### Gate 5 — literature cross-check, premises checked against the material

- **The conversion of the published `A₂` is checked against a published excluded volume computed a different
  way**: Shvets' `v = 2 M₀² A₂/N_A` gives 12.2 Å³, and this project's `B = 2 A₂ M₀/V̄` then `v = B v₀` has to
  land on the same number. It does, to 1e-9. This is the factor-of-two trap, closed.
- **Two independent measurements of solvent quality agree that PEG/water is marginal**: the osmotic `A₂`
  gives `v = 12.25 Å³` against a monomer volume of `60.4 Å³`, i.e. `χ = 0.399` on the monomer-site convention
  and `0.450` on the water-site one; SAXS on PEG-4600 in D₂O gives `χ(300 K) = 0.372`. `C-0007`, filed by the
  concurrent `P-6` iteration, adopts the SAXS value and shows the site convention is worth a factor of 2.010 —
  **this task does not depend on which convention is right**, because `B` comes from `A₂` directly and `χ`
  appears only as this cross-check. `C-0001`'s cited `χ ≈ 0.45` is unsourced and, per `C-0007`, was the value
  for **polystyrene in toluene**.
- **The thermal blob is 1222 Kuhn segments** (3800 monomers, 167 kDa) and the whole Gen-1 design space is
  60–300 monomers. The chains are **inside their own thermal blob by a factor of 13–63** — they are not
  swollen, and Gaussian elasticity on the measured Kuhn parameters is the right one. Falsifier 2 did not fire.
- **The Alexander-de Gennes unity prefactor is converted into a number**: `L₀ = N a^(5/3)σ^(1/3)` is
  reproduced exactly by a two-body box layer at `B = 6 n_K a⁵/(v₀ b²) = 1.342`, i.e. an excluded volume of
  81.0 Å³ — **6.6× the measured 12.2 Å³**. The convention was not neutral.
- **The matching construction is priced**: `B` matched at `φ#` is 1.9× the measured `B`. Had `A₂` not been
  found, this iteration would have overstated the layer's two-body interaction by that factor.
- **The two measured descriptions of the same material disagree** by **1.45×** in `Π_int` at the layer's own
  volume fraction — `0.0105 MPa` from the measured virial expansion against `0.0152 MPa` from the des
  Cloizeaux limb of the measured crossover equation of state. That disagreement, not an exponent, is the
  uncertainty on the answer.
- **The response is weakly sensitive to that disagreement, and the identity is exact.** At fixed layer height,
  grafting density and compression ratio, `N ∝ K^(−1/(m+1))` and `k ∝ K^(+1/(m+1))` — verified to 15
  significant figures for both profile models. For `m = 9/4` that is `K^(4/13)`, so a **16-fold** change in the
  interaction strength moves the stroke only from 5.81 nm to 4.38 nm. This is the sensitivity `C-0007` asks
  every claim built on a bulk property to state before applying it to a brush; the claim carries it, together
  with the one thing it would *not* be weak to — a change in the **sign** of the excluded volume.

### Not verified, and stated as such

- **The strong-stretching premise is not met, and this is the largest methodological weakness here.**
  `L₀/R₀` spans **0.387 – 2.27** across the whole 5–10 nm × 0.002–1.0 nm⁻² box and **0.83 – 1.07** at
  `C-0001`'s own 10 nm design point, where the theory wants ≫ 1. This is the same finding `CH-0003` reports
  as `L₀/R_F = 1.17–1.25`, arrived at independently. Both profile models are used outside their premise, the
  spread between them is a *lower* bound on the profile uncertainty, and falsifier (3) fired on exactly this.
- **Where the free energy stands on low blob count, stated because `CH-0003` asks:** nothing in `f_int(φ)`
  refers to a blob. It is a *local* function of the volume fraction taken from bulk osmometry, and the
  elasticity it is minimised against is Gaussian on the measured Kuhn scale, which needs no blob either.
  What remains blob-dependent is the **profile**, not the free energy — which is why falsifier (3) fired
  where it did and not elsewhere.
- The compressed strong-stretching profile is the truncated parabola; the known free-end **dead zone**
  near the wall is not resolved.
- `α` was fitted to linear PEG in **pure water** at 20 °C; `A₂` and `A₃` were measured in pure water at 25 °C.
  The Gen-1 buffer is 2–10 mM MgCl₂ and §3 permits a PS→PEG block copolymer. Neither is this material.
- The `A₂ = 1.9e-3` adopted as the central value was read in a **re-tabulation**, not in Hasse et al.
  themselves. Three values read directly bracket it.
- Nothing here is measured *about this layer*. `PASS` means model-consistent and traceable.

---

## Result

Filed as [`C-0003`](../claims/C-0003-crossover-valid-layer-response.md),
raising [`CH-0002`](../challenges/CH-0002-corrections-do-not-all-soften.md) against `C-0001`'s
"the strokes below are lower bounds" banner, which it inherited from `CH-0001`.

## Feedback into Formulate

- **`T-2` is unblocked**, but not with a window — with a **conditional** one. At 5 and 7 nm it is empty under
  every model. At 10 nm it is empty under both box profiles and `[0.018, 0.061] nm⁻²` under the
  strong-stretching ones, so its *existence* turns on the profile. `T-2` must say that, and its
  proof-of-emptiness branch cannot be taken on this evidence either.
- **`T-1b` is absorbed and closed**: the free-energy functional exists, and the `(48/35)N/g` blob-count
  identity it was raised to verify is now moot — the blob construction is not the one this layer obeys.
- **`P-6` is partly closed**: `χ = 0.37–0.40` for PEG/water at 300 K, from two independent measurements.
  What remains open is only the Mg²⁺ dependence.
- **A new premise task is revealed** and is sharper than the one `CH-0001` raised: PEG/water is a marginal
  solvent whose chains at Gen-1 lengths are not swollen — 0.06 thermal blobs — yet the adopted equation of
  state's semidilute limb is the **good-solvent** `φ^(9/4)`, fitted over 0–50 wt %. Those two statements are
  in tension and the tension is not resolved here. It is bounded, though: the response goes as `K^(4/13)`,
  so the tension is worth at most tens of percent unless it changes the *sign* of the interaction.
- **The expensive calculation is now worth buying, and it was not before.** Falsifier (3) fired: the profile,
  not the interaction, decides whether the 10 nm window exists. A Scheutjens-Fleer numerical SCF profile —
  hours of CPU — would settle it, and would be run against an interaction free energy that is now anchored in
  measurement rather than in a guessed `χ`. That is the condition `T-1`'s cost table said would have to be met
  before SCF was worth running, and it has been met.
