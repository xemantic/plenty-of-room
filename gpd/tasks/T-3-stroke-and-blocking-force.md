# T-3 — Stroke and blocking force versus bias, including ionic screening

| | |
|---|---|
| **Leaf** | `A2.2` — *"Field-driven deflection simulation incl. ionic screening"*, method named as Poisson-Nernst-Planck + FEM |
| **Problem definition** | §6 task 3; mechanism and sign conventions §1; parameters §3; §4(b), §4(c), §4(e); the loop §5; §7 |
| **Parent claims** | [`C-0008`](../claims/C-0008-electrostatic-force-and-decay-length.md) (`F_es(h, V)`, `k_es`, `ℓ`), [`C-0003`](../claims/C-0003-crossover-valid-layer-response.md) (`P(h)`, six models) |
| **Verification type** | in-silico (nonlinear Poisson-Boltzmann force curve × crossover-valid grafted-layer free energy, coupled by a bracketed 1-D root find) + logical |
| **Maturity** | **TRL 1–3.** Model-consistent and traceable. **Not** empirically demonstrated. |
| **Status** | **DONE** — claim [`C-0012`](../claims/C-0012-coupled-stroke-and-blocking-force.md), raises [`CH-0011`](../challenges/CH-0011-electrostatic-stiffness-changes-sign.md) |

---

## Formulate

### Why this task is not "divide a force by a stiffness"

§6 task 3 asks for *"stroke ≥ ~3 nm and force ≥ 100 pN at ≤ 2 V, or a demonstration that it is unreachable"*,
and the two halves are **different quantities** that are routinely conflated.

- The **blocking force** is the force the actuator delivers at **zero displacement**.
  At `h = L₀` the layer carries nothing — that is what `L₀` means — so the whole of `|F_es(L₀, V)|` is available to an external load.
- The **stroke** is the displacement reached against **no external load**, where `|F_es(h, V)|` is balanced by the layer's own restoring force `P(h)·A`.

They are the two ends of one curve and they are never delivered at the same operating point.
Worse, the obvious shortcut — stroke = force / stiffness — is not merely imprecise here, it is undefined:
`C-0003`'s strong-stretching models have **exactly zero** stiffness at `L₀`, because the brush's outer edge is diffuse,
so `F/k(L₀)` is infinite for three of the six models and a large overestimate for the other three.
`C-0001`'s surprise `S-1` said this in iteration 1 and it has survived every change of free energy since.

So a stroke here is a **root of a force balance**, and `C-0003` supplies `P(h)` directly rather than through a stiffness.

### The question, as a numeric target

Over §3's box — layer heights 5 / 7 / 10 nm, buffers 2 / 5 / 10 mM `MgCl₂` (extended down to 0.5 mM for leaf `A2.2`'s
low-screening operating point), applied bias 0.02–2 V, 40 × 40 nm tile, 300 K —
compute for **each** of `C-0003`'s six (profile × interaction) layer models:

1. `F_block(V) = |F_es(L₀, V)|`, the blocking force;
2. `s(V) = L₀ − h*(V)` where `h*(V)` solves `|F_es(h, V)| = P(h)·A`, the free stroke;
3. `W(s) = |F_es(L₀−s, V)| − P(L₀−s)·A`, the whole force-displacement characteristic;
4. `k_eff = k_brush(h*) + k_es(h*, V)` **at the operating point**, which is the quantity `C-0008` could not supply;
5. the drainage corner at that operating point, by scaling `C-0004`.

### Acceptance predicate

> §6 task 3: **stroke ≥ ~3 nm and force ≥ 100 pN at ≤ 2 V, or a demonstration that it is unreachable.**

Tightened, because "and" between two quantities that live at opposite ends of one curve is ambiguous,
and because the ambiguity is exactly where an actuator claim goes wrong.
Three clauses, each falsifiable on its own, reported separately at every one of the six models:

- **(a) BLOCKING** — `|F_es(L₀, V)| ≥ 100 pN` for some `V ≤ 2 V`.
- **(b) FREE STROKE** — `L₀ − h*(V) ≥ 3.0 nm` for some `V ≤ 2 V`, with `h*` a **root**, never a quotient.
- **(c) SIMULTANEOUS**, the tightened form — `W(3 nm) ≥ 100 pN` for some `V ≤ 2 V`:
  the device delivers 100 pN **at** a 3 nm stroke, rather than 100 pN and 3 nm at two different operating points.

`(c) ⇒ (a) ∧ (b)`.
A `PASS` on (a) and (b) with a `FAIL` on (c) is a real and common failure mode of an actuator, and it is reported rather than hidden.

**Falsified** for a given (model, height, buffer) if no `V ≤ 2 V` satisfies the clause.
**Falsified as an approach** if the six-model bracket straddles the predicate so widely that no verdict is model-independent —
in which case the honest deliverable is the bracket and the statement that the profile model decides it, exactly as `C-0003` concluded for the design window.

A second predicate travels with the first, because §3 asks for it and `A2.2` does not:

- **(d) BANDWIDTH** — the actuator's own settling corner at the operating point is ≥ 1 kHz.
  `C-0004` discharged drainage against `k_brush`; this task must check it against `k_eff`, which is smaller.

### Units, locked

`nm`, `pN`, `pN/nm` (= 1 mN/m exactly), `pN/nm²` (= 1 MPa exactly), `pN·nm`, `V`, `mM`, `Hz`, `K`.
`k_BT = 4.142 pN·nm` at 300 K, `k_BT/e = 25.852 mV`, `l_B = 0.7141 nm` at `ε_r = 78`.

### Geometry and sign conventions, fixed before deriving

§3 hands over three lengths that are easy to confuse: a 5 / 7 / 10 nm polymer layer, a ~10 nm tile,
and an effort point "~20–25 nm above the electrode".
The relation between them is fixed here, once, in code (`actuator/ActuatorGeometry.kt`), and not re-assumed downstream.

- `z` is normal to the electrode, **positive away from it**; the electrode surface is `z = 0`.
- The layer is grafted at `z = 0`.
  The tile's **bottom face** rests on the layer's outer surface at `z = h`.
- **The electrostatic gap IS the layer height**, exactly and by construction —
  `ActuatorGeometry.electrostaticGap` is the identity function on `h`.
  There is no free buffer sliver between tile and layer: a tile floating above an uncompressed layer feels no restoring force and simply falls until it touches,
  so contact is the operating state at every bias including zero.
- The tile's **top face** is at `z = h + 10 nm`; the **effort point** is at `z = h + 15 nm`.
  With a 5 nm lever attachment above the top face, §3's three layer heights put the effort point at exactly **20 / 22 / 25 nm** —
  §3's own band, at both ends. That is a consistency check on §3's parameter table and it is asserted as a test.
- The **tile's charge** is smeared onto the plane `z = h`, at the Manning-renormalised charge of half the tile — `C-0008`'s nominal convention, inherited unchanged.
  The tile's own 10 nm of thickness therefore enters this task **only** through the effort point, never through the gap.
- `F_es,z < 0` means toward the electrode; `k_es = −∂F_es,z/∂z < 0` (§1).
- The layer's disjoining pressure is **positive** when it pushes the tile along `+z`; `k_brush = −∂P/∂h > 0` (`C-0003`).
- The **stroke** is `L₀ − h`, positive downward.
- The **output force** at stroke `s` is `W(s) = |F_es(L₀−s, V)| − P(L₀−s)·A`, positive when the actuator can still push.

### The identity that makes the solve well-posed

&nbsp;&nbsp;&nbsp;&nbsp;`dW/dh = d|F_es|/dh − d(P·A)/dh = −|k_es| + k_brush = k_eff`, **exactly**.

The slope of the actuator characteristic *is* the effective stiffness of §1.
Everything else in this task follows from that one line:
the characteristic is not monotone in general, the non-monotonicity is `T-4`'s pull-in,
and the blocking force is not the largest force the actuator can deliver.

### What this task may not assume, and does not

Carried in from the queue and from `C-0008`, and enforced rather than noted:

- **100 pN is not inherited as available.** `T-1`'s own feedback note says so verbatim: *"`T-3` must not assume 100 pN is available; T-1 only says what the layer does if it is."*
  Every force here is computed from the electrostatics.
- **No force above ~1 V of *applied* bias is trustworthy** (`CH-0007`) — not 0.2 V, which was a diffuse-layer drop compared against an applied bias.
  The 2 V column is reported because §3 asks for it, flagged `withinTrustedBias = false`.
- **The zero-bias force is not a definite number.** `C-0008`: a sign-changing near-cancellation under 4 pN, changing sign between 4 and 5 nm, for which "no single number is defensible".
  The force curve therefore admits only strictly attractive samples and the bias sweep starts at 0.02 V.
- **Superposition is forbidden in the force path.** `C-0008`: it overstates 3.7× one way and understates 4.0× the other at the working gap.
- **`exp(−h/λ_D)` with `λ_D = 4 nm` is wrong.** The force's own decay length is 1.8–2.8 nm at the working gap and is bias-dependent; it is measured from the interpolant at every operating point rather than assumed.
- **`C-0003`, not `C-0001`.** And the **bracket**, not a single number — `CH-0002`: *"A downstream task must carry the bracket, not the single number, and not a one-sided inequality."*
- **The tile is not a rigid plate** (`C-0006`, `CH-0005`). Whose stroke is answered explicitly rather than left to the reader.

### What is deliberately excluded

- Any re-derivation of drainage. `C-0004` is **consumed** with citation.
- Any re-derivation of dishing. `C-0006`'s ratios are **cited** and applied.
- The layer-local salt term of `C-0007` (`≤ 1.7 %` of the modulus, `∝ 1/h`) — bounded, named as the only positive-feedback term downstream, and **not** put into the balance.
- Poisson-Nernst-Planck and FEM. See the Plan.

---

## Plan

### The cheap bound, run first

Before any coupled solve, two things were checked that could have killed the task outright:

1. **Is the layer's restoring force ever comparable with `F_es` at all?**
   At the dry-thickness end `Π_int(φ→1)·A` is 11 000 pN (two-body limb) to 54 000 pN (des Cloizeaux limb) over the 1600 nm² footprint,
   against a largest `|F_es|` anywhere in `C-0008`'s table of 938 pN.
   So an equilibrium **always** exists above the dry thickness, and the tile never collapses onto the electrode within §3's box.
   That is what makes the bracket for the root find provable rather than hoped for.
2. **Is a linear reading admissible?** No, and not marginally:
   three of `C-0003`'s six models have exactly zero stiffness at `L₀`.
   The cheap bound therefore does not exist for this quantity, and that is itself the cost justification for the root find.

### Method, and the justification against cost

The coupled problem is a **1-D root find**, and everything expensive in it has already been paid for by `T-1c` and `T-3a`:

- `P(h)` comes from `brush/GraftedLayer.kt` — `C-0003`'s six models, consumed as a library and **re-evaluated at every operating height**, not read off a claim table.
- `F_es(h, V)` comes from `electrostatics/PoissonBoltzmannGap.kt` — `C-0008`'s pipeline, **re-run**, not tabulated by hand.

The one new numerical object is an interpolant.
Each evaluation of `F_es(h, V)` costs a 34-step Stern-series bisection wrapped around a nonlinear Poisson-Boltzmann solve;
a root find takes tens of evaluations and this task needs one per (model × height × buffer × bias), which is hundreds.
So `F_es(h)` is **sampled once** per (buffer, bias, medium) on a geometrically graded grid and interpolated by
**shape-preserving cubic Hermite on `ln|F_es|` against `h`**.

That choice is not a convenience, it is the one that makes its own error checkable:

- `C-0008` establishes that `|F_es|` decays on its own length `ℓ`, approaching the bulk `λ_D` in the far field,
  so in the far field `ln|F|` is **exactly linear** in `h` and a scheme exact on linear data is exact there in value *and* derivative.
  That is asserted at 1e−12, against the closed form.
- The Fritsch-Carlson limiter makes the interpolant **shape-preserving**, so it cannot overshoot into a non-monotone `|F_es|`
  and therefore cannot invent a sign change in `k_es` — a spurious pull-in. A natural cubic spline would be smoother and would carry exactly that hazard.

### Why the root find is unconditionally convergent, and why its root is the stable one

`C-0001` argued bisection was the right choice because its pressure was monotone.
Here the *characteristic* is **not** monotone — `dW/dh = k_eff`, and `k_eff` changes sign at a fold — so that argument does not transfer, and saying so is part of the work.

What does transfer, on a bracket:

1. `W(L₀) = |F_es(L₀, V)| > 0`, because the layer carries nothing at `L₀`.
2. `W` at the dry-thickness end is large and negative, by the cheap bound above.
3. Scanning down from `L₀`, the **first** sign change is bracketed; bisection inside that bracket retains the sign change at every step and is therefore unconditionally convergent, needing no derivative.
4. At that first root `W` goes from positive above to negative below, so `dW/dh > 0` there, so **`k_eff > 0`**:
   **the first root below `L₀` is always the stable one.** That is a theorem about the construction, not a property of the answer, and it is asserted as a test over a family of fields.

Further roots below it are **counted** and reported: a non-zero count means an unstable root and a deeper stable one exist, i.e. a fold is nearby.
That is the number `T-4` wants, and it comes free.

### Why not Poisson-Nernst-Planck + FEM, which is what leaf `A2.2` names

`A2.2`'s method column says "Poisson-Nernst-Planck + FEM". It is not run here, and the reasons are stated rather than implied.

- **PNP adds transport to PB, and no transport quantity is in the predicate.** The stroke and the blocking force are *equilibrium* quantities: they are read from the steady state, where the Nernst-Planck fluxes vanish identically and PNP reduces to PB — the equation `C-0008` already solves, nonlinearly, in the correct 2:1 electrolyte, with a Stern layer in series and a size-modified bracket.
  PNP would buy the **transient**: the ionic relaxation time `λ_D²/D ≈ 10 ns`, which is 10⁻⁴ of `C-0004`'s drainage time and therefore not the bandwidth-limiting process. That is a number worth having and it is not worth a solver.
- **FEM buys dimensionality, and that is worth buying** — but for `T-3b`, not here.
  A 2-D solve of the tile edge is the only route to the lateral load profile that `C-0006` shows dishing is *exactly linear* in, and `C-0008` states plainly that a 1-D treatment cannot supply it.
  It would convert directly into a dishing amplitude and hence into the difference between the lever's stroke and the sensor's. It is a queued, costed item.
- **Neither repairs the dominant error.** `C-0005` puts the one-loop correction at 123–214 % of the leading term across this entire gap range: mean field is *uncontrolled* here, not merely inaccurate. A finer discretisation of a mean-field equation does not shrink a mean-field error. The only method that would is explicit-ion simulation, at `C-0005`'s 1–3 week cost, which was not spent and is not spent here.

So the honest statement is: **the equilibrium half of `A2.2`'s method is discharged by a better-conditioned equivalent; the dimensional half is deferred to `T-3b` with a named deliverable; and the accuracy half is not addressable by either.**

### What would falsify this approach

Stated in advance.

1. **If the six-model bracket on the stroke were narrower than the difference between the three acceptance clauses**, the decomposition into (a)/(b)/(c) would be pedantry rather than physics, and a single number would have been the right deliverable.
   *(Did not fire, decisively. At 10 nm and 2 mM the three clauses need 0.699 V, 0.024–0.076 V and 0.134–0.192 V — they are separated by up to 30× in bias, and the model bracket inside each is a factor of 3 at most.)*
2. **If the first root below `L₀` were ever found to have `k_eff ≤ 0`**, the stability theorem would be wrong and the whole solve would be reporting unstable equilibria.
   *(Did not fire. `k_eff > 0` at every one of the 810 free operating points, and at all fifteen synthetic fields the test sweeps.)*
3. **If the interpolant's error exceeded the six-model spread**, the numerical shortcut would be doing the work of the physics and the curve would have to be re-sampled inside the root find.
   *(Did not fire, by four orders of magnitude. The stroke is stable to 1.6e−7 across an eightfold change in force-curve samples, and the six-model spread is 30–100 %.)*
4. **If the coupled operating point turned out to be insensitive to the layer model**, `C-0003`'s whole bracket would be irrelevant to `T-3` and `T-1d` would not be blocking anything here.
   *(Did not fire. The free stroke brackets 0.93–2.77 nm at 5 nm and 4.42–8.16 nm at 10 nm at 0.10 V — a factor of 2–3 — and the 3 nm verdict at 7 nm is **straddled**, three models each way. `T-1d` is blocking.)*
5. **If `k_eff` at the operating point stayed close to `k_brush`**, the electrostatic softening would be a curiosity rather than a constraint, `T-4` would not need promoting, and the bandwidth check against `C-0004` would be redundant.
   *(**FIRED, and harder than expected — but at the loaded operating point, not the free one.** `k_eff` at the free operating point is always positive, by the stability theorem; at the **loaded** point — the tile held at the 3 nm stroke the predicate is about — it is **negative at 428 of 810** state points, including every 7 nm and 10 nm point above ~0.1 V. The predicate's own operating point is statically unstable against a dead load. This is the single largest finding of the task and it goes straight to `T-4`.)*

A sixth falsifier fired that was **not** stated in advance, and that is recorded rather than quietly absorbed:

6. **The free operating point leaves three upstream validity ranges above ~0.1 V.** The plan assumed the coupled solve would land somewhere the upstream claims cover, and checked only that an equilibrium existed above the dry thickness. It does exist — but at 0.98–1.12 nm and `φ = 0.33` at 1 V, which is inside `C-0005`'s 1.46 nm correlation band and above `C-0002`'s φ ≈ 0.2 concentrated crossover. **Only 272 of 810 free operating points are inside both.** The validity check should have been in the Plan as a predicate, not discovered in the Execute; it is now emitted per state point as `modelValid`.

---

## Execute

Code, all of it in `actuator/`, which this task owns.
`brush/`, `electrostatics/`, `material/`, `structure/` and `poroelastic/` are consumed as libraries and not edited.

| file | what it is |
|---|---|
| `src/main/kotlin/actuator/ActuatorGeometry.kt` | the gap-height identity, the tile faces, the effort point |
| `src/main/kotlin/actuator/ElectrostaticForceCurve.kt` | shape-preserving `ln|F_es|` interpolant, `k_es` and `ℓ` from its derivative |
| `src/main/kotlin/actuator/ActuatorForceBalance.kt` | the coupled solve, the characteristic, the stability count |
| `src/main/kotlin/actuator/ActuatorAcceptance.kt` | threshold crossing, and `C-0004`'s drainage corner scaled by `k_eff` |
| `src/main/kotlin/actuator/ActuatorResultRounding.kt` | deterministic serialisation — a copy of `structure/ResultRounding.kt`'s pattern, not an import |
| `src/main/kotlin/actuator/StrokeAndBlockingForceStudy.kt` | the sweep and the result file |

```shell
./gradlew study -Pstudy=actuator.StrokeAndBlockingForceStudyKt -PbuildDirectory=build-t3
./gradlew test -PbuildDirectory=build-t3
```

Tests: **29 new** in `src/test/kotlin/actuator/` — 6 geometry, 10 force curve, 13 force balance —
and 608 green in the suite.

Result: [`gpd/results/T-3-stroke-and-blocking-force.json`](../results/T-3-stroke-and-blocking-force.json),
2.1 MB, deterministic: **re-run and diffed byte-for-byte identical**.
State-point census: 18 design points (6 models × 3 heights), **810** coupled operating points
(6 models × 3 heights × 5 buffers × 9 biases), 90 threshold records, 54 layer-medium records,
126 stroke-reading records, 12 convergence records.

---

## Verify

All five gates, executed as tests. Test names carry the gate they discharge.

### Gate 1 — dimensional consistency

- The footprint is the tile edge squared, in nm²; the electrostatic gap is a length and is the layer height.
- `ℓ = F_es/k_es` is `pN / (pN/nm) = nm`, asserted as an identity at every gap rather than as a formula.
- The output work is a force times a length, and `k_eff = k_brush + k_es` is asserted additively to 1e−9.
- Unphysical geometry, non-ascending sample grids, repulsive samples and out-of-range gaps all throw.

### Gate 2 — limiting cases

- **A pure exponential force is reproduced exactly**, value and derivative, at 1e−12 — because `ln|F|` is then linear and the scheme is exact on linear data.
- A vanishing field leaves the tile at `L₀` with zero stroke; a stronger field gives a monotonically larger stroke and a smaller operating height.
- The box model opens with **finite** stiffness at `L₀` and the strong-stretching model with **none** — `C-0001`'s `S-1`, still standing.
- Scaling the interaction over 16× moves the stroke by less than 2×, reproducing `C-0003`'s exact `k ∝ K^(1/(m+1))` weakness.
- The interpolant is monotone with no overshoot, checked on a 0.01 nm sweep.

### Gate 3 — symmetry and conservation

- At the operating point the layer load equals `|F_es|` to 1e−9 of itself, and the output force vanishes there.
- `k_es < 0` and `F_es < 0` everywhere, per §1; `ℓ > 0` everywhere.
- **The first equilibrium below `L₀` has `k_eff > 0`**, asserted across fifteen (amplitude, decay length) combinations — the stability theorem, tested rather than assumed.
- **The output force peaks at a finite stroke, not at zero**, which is the electrostatic-softening signature in the force-displacement plane and the ground for `CH-0011`.

### Gate 4 — numerical convergence

- Interpolant: the worst relative error over the range falls by more than 5× per halving of the sample spacing and reaches < 1e−6 — stated as an **order over the range**, not pointwise, because pointwise at 1e−9 the comparison measures double-precision roundoff.
- Force balance: the operating height is unchanged to 1e−9 between 500 and 16 000 scan steps.
- The study emits a three-way convergence table over the whole pipeline — force-curve samples, Poisson-Boltzmann mesh nodes, and force-balance scan steps.

### Gate 5 — literature and upstream cross-check

- **`C-0003` is reproduced from the other side.** With a *constant* 100 pN curve the coupled solver must return `C-0003`'s dead-load stroke: it does, to 1e−8, by a genuinely independent route — `heightUnderLoad` brackets on the pressure, this brackets on the output force — and the result lands inside `C-0003`'s published 3.83–6.01 nm bracket at the 10 nm point.
- **`C-0008` is reproduced** through the same solver at the same state points.
- **§3's own parameter table is checked against itself**: the 20–25 nm effort point is exactly what a 10 nm tile on §3's three layer heights gives, with a 5 nm lever attachment.
- **`C-0004` is consumed, not re-derived**, and its exact `τ ∝ 1/k_layer` is what licenses substituting `k_eff`.

### Not verified, and stated as such

- Nothing is measured. `PASS` means model-consistent and traceable.
- The mean-field error is not bounded by its own expansion (`C-0005`), and for oppositely charged walls its **direction** is unknown.
- The layer's profile model is not settled (`T-1d`), and it is what decides whether the 10 nm design point exists at all.
- The lateral load profile is not computed and a 1-D treatment cannot compute it (`T-3b`).
- Mg²⁺-free is not computed; only low-Mg, down to 0.5 mM.

---

## Result

[`C-0012`](../claims/C-0012-coupled-stroke-and-blocking-force.md). In one paragraph:

**All three clauses pass, at 0.08–0.37 V — a fifth of §3's budget and inside `CH-0007`'s ~1 V boundary — and the
operating point they pass at is not one the device can be held at.** The blocking force is model-free (167 / 87 /
34 pN at 0.10 V and 2 mM, at 5 / 7 / 10 nm) because every model shares `L₀`; the free stroke is not (0.93–2.77,
2.24–5.02, 4.42–8.16 nm at the same point). The two halves of the predicate need biases differing by up to 30×
**in opposite directions with layer height**. Above ~0.1 V the unloaded tile snaps to 0.98–1.12 nm at `φ ≈ 0.33`,
outside `C-0005`'s correlation band and `C-0002`'s concentrated crossover at once, so the largest *defensible*
free stroke is 0.93–2.02 nm (5 nm), 2.24–3.96 nm (7 nm) and **4.42–6.91 nm (10 nm)** — the 3 nm target is
model-independently **unreachable at 5 nm**, **straddled at 7 nm** and **reached at 10 nm**. At the loaded
operating point `k_eff < 0` at 7 and 10 nm, so the §6 target needs an output coupling with its own stiffness.
Bandwidth is not the constraint: where a corner exists it is 98 kHz to 2.3 MHz.

## Feedback into Formulate

- **`T-4` is no longer "does `k_eff` reach zero" — it does, at the predicate's own operating point.** The
  question becomes *what happens after*, and `CH-0011` says the answer is not a runaway: `k_es` changes sign at
  0.55–1.58 nm and stops it. `T-4` should be re-formulated around the **maximum usable bias set by upstream
  validity** (≈ 0.1 V), which is an order of magnitude below any pull-in estimate.
- **`T-2`'s §4(a)/(b) axis is now decided in the same direction by two independent constraints.** Both the
  mechanical window (`C-0003`) and the coupled stroke (here) say 10 nm and reject 5 nm — and the *stability*
  constraint says the opposite. `T-2` has a genuine trade to report rather than a single ordering.
- **A new predicate is needed that `T-3` could not supply:** the minimum output-coupling stiffness that makes the
  loaded operating point stable. It is `|k_es| − k_brush` at `h = L₀ − 3 nm`, which this task emits.
- **`T-1d` blocks the 10 nm verdict**, which is the only height that passes the stroke clause inside validity.
