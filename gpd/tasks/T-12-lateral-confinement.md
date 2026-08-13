# T-12 — Lateral confinement of the Gen-1 tile

| | |
|---|---|
| **Leaf** | `A1.2` (the 3.0 nm positional bound), with `A1.1` as its bound table and **`A8.2`** for *"identify the dominant compliance term … and budget stiffness at the joints"* |
| **Problem definition** | §6 task 8 (the predicate); §3 (geometry, stroke, force, bandwidth); §4(f) (survival); §4(g) (force transfer); §1 (the stack); §5, §7 (process) |
| **Verification type** | in-silico (closed-form element mechanics assembled into a 4-DOF anchor stiffness) + **logical** (a convexity theorem that decides the topology before any number is computed) |
| **Maturity** | TRL 1–3. Model-consistent and traceable. **Not measured.** |
| **Status** | Executed, verified, filed as claim [`C-0014`](../claims/C-0014-lateral-confinement.md) |
| **Consumes** | [`C-0010`](../claims/C-0010-tile-positional-variance.md) (the requirement, the zero, the diffusion), [`C-0006`](../claims/C-0006-tile-load-distribution-and-flatness.md) (the plate, the anchors, the allowables), [`C-0009`](../claims/C-0009-discrete-lattice-tile.md) (the per-anchor concentration factor), [`C-0003`](../claims/C-0003-crossover-valid-layer-response.md) (the layer), [`C-0004`](../claims/C-0004-poroelastic-drainage.md) (the drag), [`C-0008`](../claims/C-0008-electrostatic-force-and-decay-length.md) (the field) |
| **Raises** | [`CH-0013`](../challenges/CH-0013-entropic-tether-is-not-zero.md) against `C-0010` |

---

## Formulate

### The gap this task exists to close

[`C-0010`](../claims/C-0010-tile-positional-variance.md) established that the grafted layer's **lateral restoring stiffness is exactly zero, by symmetry** —
the free energy of a laterally homogeneous grafted layer under a laterally homogeneous non-adsorbing tile
is invariant under lateral translation, so equipartition does not apply to that coordinate at all.
The untethered tile therefore **diffuses 62.8 nm in one 1 kHz period**
(`D = 1.969 × 10⁶ nm²/s`, from a Brinkman shear drag on `C-0004`'s permeability),
which is 21× the §6 task 8 predicate and 1.6 tile widths.

`C-0010` states the requirement and **explicitly declines to specify a scheme**, because §3 specifies none.
It brackets reachability — a clamped 10 nm duplex strut at `3EI/L³ = 0.69 pN/nm`, a 20 nm one at 0.086,
a flexible tether at "essentially nothing" — and says *"short and stiff, or not at all. No lateral stiffness is asserted here."*

**This task closes that gap: it either names a scheme that meets the requirement, or demonstrates that none compatible with §3 does and names the binding constraint.**

### The question, as a numeric target

Leaf `A1.1`'s own bound table supplies the translational requirement, and it is **re-derived from `k_BT` alone** rather than accepted:

&nbsp;&nbsp;&nbsp;&nbsp;**`k_lat ≥ k_BT/σ² = 4.142/3.0² = 0.460216 pN/nm`** at `σ = 3.0 nm`, 300 K.

§6 task 8 says "σ_RMS", and `C-0010` had to declare which reading of the *normal* coordinate it was answering.
The lateral coordinate has the same ambiguity and it is declared here in advance, with the other readings reported alongside:

| reading | what it is | required `k_lat` | required `k_yaw` |
|---|---|---|---|
| **per-coordinate (DECLARED ACCEPTANCE, = leaf `A1.1`)** | `σ_x ≤ 3.0 nm`, one Cartesian coordinate | **0.460216 pN/nm** | **368.17 pN·nm/rad** |
| radial | `√(σ_x² + σ_y²) ≤ 3.0 nm` | 0.920433 | — |
| worst-point combined | the in-plane RMS of the tile's **corner**, translation *and* yaw together, `≤ 3.0 nm` | 1.380649 | 1104.5 |

The declared acceptance is the **per-coordinate** reading, because that is the reading leaf `A1.1` tabulates
and the one `C-0010` passed the requirement down in. The other two are reported with their margins,
because `CH-0009`'s lesson — *a spatially varying fluctuation has no single "at a point" value* — applies verbatim in the plane.

### The yaw currency, justified rather than assumed

The symmetry argument that kills the layer's lateral stiffness kills its yaw stiffness by the same step,
and `C-0010` says so. A tile that rotates is as useless as one that slides, so yaw is budgeted here in the **same currency**:

&nbsp;&nbsp;&nbsp;&nbsp;**a yaw of `ψ` moves the tile's worst material point — its corner, at `r_c = L/√2 = 28.284 nm` — by `r_c ψ`,
and that displacement is held to the same 3.0 nm.**

&nbsp;&nbsp;&nbsp;&nbsp;`ψ_rms ≤ 3.0/28.284 = 0.106066 rad` &nbsp;→&nbsp; **`k_yaw ≥ k_BT/ψ² = 368.17 pN·nm/rad`.**

Three reasons for the corner rather than a footprint average:

1. it is `CH-0009`'s finding applied in-plane — the corner is the worst point and naming a point fluctuation without naming the point is the error `CH-0009` was raised about;
2. §4(g)'s lever samples the tile at a *point*, and the point it samples is not the centroid;
3. it makes yaw and translation commensurable, so the two can be added into one worst-point number.

The footprint-RMS radius reading (`r_rms = √(L²/6) = 16.330 nm`, hence `k_yaw ≥ 122.72 pN·nm/rad`)
is **3.0× weaker** and is reported alongside, not used.

### Units, locked

SI in the programme's scaled form.
Lengths and RMS amplitudes in **nm**; forces in **pN**; energies in **pN·nm** and `k_BT`;
translational stiffness in **pN/nm** (= mN/m); **rotational stiffness in `pN·nm/rad`**;
areal stiffness in **pN/nm³**; bending rigidity `EI` and torsional rigidity `GJ` in **pN·nm²**;
stretch modulus in **pN**; angles in **rad**; contour lengths in **nm** and in **nucleotides**.
`k_BT = 4.142 pN·nm` at 300 K, aqueous buffer, 2/5/10 mM MgCl₂.

### Geometry and sign conventions, fixed before deriving

Inherited unchanged from [`T-5`](T-5-load-distribution.md), [`T-5b`](T-5b-tile-flatness.md) and [`T-8`](T-8-tile-positional-variance.md),
and extended by the three this task needs:

- `x` along the helices, `y` across them, `z` **upward** from the electrode, origin at the centre of the 40 × 40 nm footprint;
  the plate tasks use `w` positive *downward* and this task's `z` is its negative, which matters only in signs that are stated.
- the tile's mid-plane sits at the layer height `h` ∈ {5, 7, 10} nm (§3); the electrode is at `z = 0`.
- **an anchor is a two-node link** between a point on the substrate (or on a substrate-fixed body) and a point `r_i = (x_i, y_i)` on the tile,
  with a unit axis `n̂`, an **axial** stiffness `k_a` along `n̂` and a **transverse** stiffness `k_t` perpendicular to it.
  Its 3 × 3 contribution is `K_i = k_a n̂n̂ᵀ + k_t (I − n̂n̂ᵀ)`, which is the *only* place the two element stiffnesses meet.
- **`θ` is measured from the surface normal**: `θ = 0` is a strut standing on the substrate under the tile, `θ = 90°` a tether lying in the plane.
  This is the single most important convention in the task, because the answer is a statement about `θ`.
- a **positive** lateral stiffness restores; a **positive** normal stiffness resists the actuator, i.e. it is a cost.

### What "an anchoring scheme" has to deliver, in full

Discharged when all seven hold:

1. `k_lat ≥ 0.460216 pN/nm` **and** `k_yaw ≥ 368.17 pN·nm/rad` at 300 K in the §3 geometry, from element mechanics with **the boundary conditions named** — clamped-clamped, clamped-guided and clamped-pinned differ by factors of 4 and 16 and an origami-to-substrate joint is not obviously any of them;
2. the per-anchor force is checked against the **per-path** allowables (`C-0006`: duplex shear ~48–65 pN, unzip 10–15 pN, 65 pN hard ceiling because every origami helix is nicked — **not** §4(f)'s 35–60 pN, which is a whole-cross-section number), **with `C-0009`'s 2.3–7.6× concentration factor applied**, and the joint geometry declared as shear or unzip;
3. the **cost to the normal direction** is quantified: added normal stiffness against `C-0003`'s layer secant `16.6–26.1 pN/nm`, the stroke lost, and any static preload against the §3 100 pN;
4. the anchors' interaction with the layer is stated — a through-layer anchor displaces polymer, sits in its osmotic pressure, and has its length set by the layer height, which is also the stroke axis;
5. whether the lateral anchors can be the **same** attachments as the ≥ 64 the output coupling needs for flatness (`C-0009`), or must be additional;
6. **either** a scheme passes 1–4 simultaneously, **or** it is shown that no scheme compatible with §3 does, naming the binding constraint;
7. all five gates pass, with gate 3 checking something *independent* of the construction.

### What is deliberately excluded

- **The output coupling.** `C-0006`/`C-0009` own the ≥ 64 attachments flatness needs; this task owns the lateral degrees of freedom only, and says where the two interact.
- **Electrostatic softening.** `k_es < 0` acts on the *normal* coordinate (§1); its lateral analogue is the patterned-electrode corrugation, which is treated here as a **ceiling and a threshold** rather than solved, because a 2-D Poisson-Boltzmann solve is `T-3b`'s costed queue item and `C-0008` states plainly that a 1-D treatment cannot supply it.
- **Adsorption.** A layer that adsorbed the tile would confine it laterally by pinning; the §3 layer is non-adsorbing and that premise is what makes the lateral stiffness zero in the first place.
- **The unbiased state.** `T-13` owns it. Every corrugation scheme here is *proportional to the load the tile already carries* and therefore vanishes at zero bias; that is stated, not hidden.

---

## Plan

### The cheap bound is a theorem, and it runs before any number

The cheapest thing available is not a calculation but a **one-line convexity argument**, and it decides the topology:

> **The anisotropy theorem.** Let a flexible link run from the substrate to the tile, spanning the gap `h`,
> with a force-extension law `f(x)`, `f(0) = 0`, convex (`f'' ≥ 0` — every polymer and every duplex strain-stiffens).
> Its **transverse** stiffness at extension `h` is the tension over the length, `f(h)/h`;
> its **axial** stiffness is the tangent, `f′(h)`.
> Convexity with `f(0) = 0` gives `f(h) = ∫₀ʰ f′ ≤ h f′(h)`, so
>
> &nbsp;&nbsp;&nbsp;&nbsp;**`k_lat/k_norm = secant/tangent ≤ 1`, with equality only for a linear spring.**
>
> **No load path that crosses the layer can buy lateral stiffness more cheaply than one-for-one in normal stiffness.**

That is worth running first because it costs nothing and it **rules out the obvious scheme before it is costed**:
an anchor standing on the substrate under the tile (`θ = 0`) is exactly the geometry whose anisotropy runs the wrong way.
It also tells us what to look for — `θ = 90°`, a load path *parallel* to the surface, to which the theorem does not apply
because such a link does not have to accommodate the stroke axially.

The theorem is a bound, not the answer: a **rigid rod** is not covered by it (its lateral stiffness is bending, not tension),
and rods do far *worse* than the bound rather than better. Both are computed.

### Then closed-form element mechanics, and why not anything more expensive

| | closed-form element mechanics (chosen) | oxDNA/CG ensemble of the anchored tile | 2-D PB solve of a patterned electrode |
|---|---|---|---|
| what it gives | `k_a`, `k_t` per element from `EI`, `GJ`, `S`, and the FJC; assembled into `k_x`, `k_y`, `k_z`, `k_yaw` exactly | a sampled anchor stiffness with a genuine CI | the lateral corrugation of the field, as a profile |
| cost | seconds | days on 8 cores, **and it has no polymer layer** (`C-0010`'s argument, unchanged) | hours — but it is `T-3b`'s, and it is queued |
| what it would add here | — | noise on numbers that are closed forms | the one number the corrugation branch is missing |

The decisive row is the second: the elements here (a beam in bending, a rod in tension, a freely jointed chain, an Euler column)
are **textbook closed forms whose inputs are already cited in `C-0006`**, and a coarse-grained ensemble would return
a statistical interval around the same expressions while omitting the layer that sets the geometry.
Per `CLAUDE.md`'s research practice, the corrugation branches are closed with **a ceiling and a threshold**
rather than a value, which is what `P-6` did when the quantity it was sent for turned out not to exist.

### The schemes to be evaluated, declared in advance

| id | scheme | topology | why it is in the list |
|---|---|---|---|
| `S1` | vertical duplex struts through the layer | `θ = 0`, rigid | `C-0010`'s own bracket. The scheme anyone would try first |
| `S2` | vertical four-helix-bundle struts | `θ = 0`, rigid | the stiff extreme of `S1`; tests whether stiffening the strut rescues it |
| `S3` | ssDNA entropic tethers through the layer | `θ = 0`, flexible | the equality case of the anisotropy theorem, and the standard way origami is attached to a surface |
| `S4` | surface-parallel duplex tethers, tile edge to a **coplanar fixed frame** | `θ = 90°` | the topology the theorem points at |
| `S5` | the same, but anchored to a single-duplex **post** | `θ = 90°` + a `θ = 0` post in series | isolates the dominant compliance term — leaf `A8.2`'s explicit ask |
| `S6` | lateral **grafting-density** patterning (a pad under the tile) | field, no anchor | `C-0010` names it as *"a design lever nobody has costed"* |
| `S7` | lateral **electrode charge** patterning | field, no anchor | §1 says the electrode *is* patterned |

`S1`–`S5` are evaluated at all three §3 layer heights and on both duplex bending rigidities
(CanDo's `EI = 230 pN·nm²` and the Mg²⁺-measured `L_p = 40 nm`, `EI = 165.7 pN·nm²`), and under both plausible end conditions.
`S6` and `S7` are ceilings with thresholds attached, and are labelled as such everywhere they appear.

### The stroke budget, declared before it is spent

An anchor's normal stiffness is subtracted from the actuator, so a cost budget has to be named in advance or it will be
chosen after the fact. **The budget is 10 % of the stroke**, i.e.

&nbsp;&nbsp;&nbsp;&nbsp;`Σ k_z,anchors ≤ 0.1111 × k_layer,secant`, evaluated at the **soft** end of `C-0003`'s bracket (16.6 pN/nm), so `Σ k_z ≤ 1.84 pN/nm`.

10 % is chosen because `C-0006` already reports discrete normal anchors costing **18–50 %** of the actuation and calls that
"the binding cost of anchors"; a lateral scheme that spends less than a fifth of what the normal ones spend is worth having,
and one that spends more is not obviously better than doing without.

### What would falsify this approach — stated in advance

1. **A through-layer link whose lateral stiffness exceeds its normal stiffness.** That would falsify the anisotropy theorem,
   which is the spine of the whole argument. It cannot happen for a convex law, so if the code reports it, the code is wrong.
2. **A per-anchor force reaching an allowable.** Then *strength*, not stiffness, is the binding constraint,
   and the framing of the task changes from "can it be stiff enough" to "can it be stiff enough without tearing".
3. **The entropic tether coming out at zero stiffness.** That would confirm `C-0010`'s line and kill the cheapest scheme.
4. **Every scheme exceeding the 10 % stroke budget.** Then lateral confinement and §3's stroke are genuinely incompatible
   and `T-2`'s window loses a dimension — which is a result, and the one NDI most wants early.
5. **The freely-jointed-chain tangent disagreeing with its Gaussian limit at low force.** A coding error, not a physics one,
   because the FJC *is* Gaussian there.
6. **The exact `r²` cancellation between the yaw and translation requirements failing.**
   For anchors at the same radius as the point the yaw budget is written at, the two requirements are claimed to be
   *identically* the same condition, independent of that radius. That is an algebraic claim and it is asserted as a test.
7. **A corrugation ceiling far above the requirement.** Then the anchorless branches would be the answer and the
   mechanical schemes would be beside the point — and this task would have to hand the branch to `T-3b` rather than close it.

### The cross-claim inputs, and how they are used

| from | what is taken | how |
|---|---|---|
| `C-0010` | `k_lat ≥ 0.460216 pN/nm`; `D = 1.969 × 10⁶ nm²/s`; the exact zero | requirement re-derived from `k_BT`; the diffusivity **cited** and used only to state what happens without a scheme |
| `C-0003` | layer secant `16.6–26.1 pN/nm`, tangent at the working point `47.7–64.2`, stroke `3.83–6.01 nm` at 10 nm | **cited** as the denominator of every stroke cost |
| `C-0006` | `EI`, `GJ`, `S`, the interhelical distance, the per-path allowables, the 100 pN | **cited**; the allowables are the per-path ones, never §4(f)'s cross-section band |
| `C-0009` | the **2.3–7.6× anchor force concentration factor**, the 56 crossovers, the 64 attachments flatness needs | **cited**, applied at its worst value to every per-anchor force here |
| `C-0004` | the Brinkman screening length and the drag | **cited**, only to restate the no-scheme baseline |
| `C-0008` | the electrostatic force and the length it decays on | **cited**, as the energy scale of the `S7` ceiling |

---

## Execute

```shell
./gradlew test -PbuildDirectory=build-t12
./gradlew study -Pstudy=anchoring.LateralConfinementStudyKt -PbuildDirectory=build-t12
tools/verify.sh
```

Code, all in the new package `src/main/kotlin/anchoring/` — nothing outside it was created or modified,
because `structure/`, `brush/` and `actuator/` are being worked concurrently:

| file | what is in it |
|---|---|
| `AnchorElement.kt` | the element mechanics: beam transverse stiffness with named end conditions, rod axial stiffness, bundle `EI` by the parallel-axis theorem, Euler buckling with its effective-length factor, the geometric softening of a compressed strut, and the freely-jointed chain (exact inverse Langevin by bisection) |
| `AnchorScheme.kt` | a link's 3 × 3 stiffness `k_a n̂n̂ᵀ + k_t(I − n̂n̂ᵀ)`, the assembly into `k_x`, `k_y`, `k_z`, `k_yaw`, and the exact placement rule |
| `LateralConfinementBudget.kt` | the requirement in its three readings, the anisotropy theorem as an executable check, the stroke cost, the per-anchor force with `C-0009`'s concentration factor |
| `LayerCorrugation.kt` | the `S6` and `S7` ceilings and their thresholds |
| `LateralConfinementStudy.kt` | the study entry point, emitting the result JSON |

`roundedForResult` is **imported** from `structure`, not reimplemented — it is public, and duplicating the
serialisation-boundary rounding would be exactly the way to get two files that drift.

Result: [`../results/T-12-lateral-confinement.json`](../results/T-12-lateral-confinement.json), deterministic —
verified by re-running and diffing, no change.

Tests: `src/test/kotlin/anchoring/`, each named for the gate it discharges.

---

`roundedForResult` is **imported** from `structure` rather than reimplemented; `structure/` was read and not modified,
because two other agents are working it concurrently.

52 tests in four gate-named files: `AnchorElementTest` (17), `AnchorSchemeTest` (8),
`LateralConfinementBudgetTest` (16), `LayerCorrugationTest` (11). All green.

**TDD, recorded rather than claimed.** The four test files were written before any of the five source files existed and
the first `compileTestKotlin` failed on every symbol. The first green run then failed **three** tests, all on the same
real defect: `cosh(u)/sinh(u)` overflows to `NaN` above `u ≈ 20`, so the freely-jointed chain returned `NaN` at high
tension and the design bisection silently converged to its lower bracket. Fixed by expanding `coth` at both ends — the
same trap `CLAUDE.md` already records for `brinkmanShearDrag` and for `1 − tanh(x)/x`, now met a third time.

---

## Verify

### The five gates

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | `EI/L³` divides by exactly 8 when the length doubles and multiplies by exactly 4 when the rigidity quadruples; `S/L`; `π²EI/(KL)²` quarters at double length; `3k_BT/(L_c b)` is an energy over an area; a yaw stiffness is a lateral stiffness times a squared radius; a corrugation stiffness is an energy over a squared length and its threshold inverts its own ceiling | **PASS** |
| **2 — limiting cases** | the two end conditions differ by **exactly 4** in transverse stiffness *and* in buckling load; a bundle reduces to `n EI₁` for coincident helices and gains exactly `S Σy²` when separated; a compressed strut loses **exactly all** its transverse stiffness at its Euler load and **raises** past it rather than extrapolating; the chain is Gaussian at low force and asymptotes to but never reaches its contour; the cable term vanishes as the **cube** of the offset; a link along the normal puts its axial stiffness in `z` and its transverse in the plane, and a link in the plane swaps them **exactly**; a single central anchor has **zero** yaw stiffness whatever its lateral stiffness; a corrugation whose period divides the tile width does **nothing at all**; leaf `A1.1`'s bound table reproduced from `k_BT` alone | **PASS** |
| **3 — symmetry and conservation** | four independent checks, none of them a restatement of the equipartition the requirement is built from — see below | **PASS** |
| **4 — numerical convergence** | the chain inversion round-trips `f → x → f` to `1e−9` and **exits on the bracket width, never on a residual**; the tangent stiffness is smooth and monotone through the small-argument series join; the tether design solve reproduces its own requirement to `1e−7` at every count and span and approaches its Gaussian closed form to `1e−3` for a slack chain; the electrode optimum is a genuine interior maximum inside the domain the expression is valid on | **PASS** |
| **5 — literature cross-check** | `C-0010`'s strut bracket **reproduced from `EI` and `L`** (0.69 and 0.08625 pN/nm); `EI = L_p k_BT` and CanDo's implied 55.5 nm against the measured 40 nm in Mg²⁺; the **ssDNA elasticity read from primary sources for this task** and shown to be a method-systematic bracket rather than noise; `C-0006`'s ripple transfer exactly ½ at `λ = 2πℓ`; `C-0006`'s dishing exactly linear in the modulation depth; `C-0009`'s concentration factor applied rather than the equal share | **PASS** |

### Gate 3 — what is checked, given that equipartition *is* the requirement

Asserting `σ² = k_BT/k` would be a tautology here, exactly as it was in `T-8`. Four independent things instead:

1. **The trace invariant.** `k_xx + k_yy + k_zz = k_a + 2k_t` for a link at *any* orientation — a property of the projector
   `k_a n̂n̂ᵀ + k_t(I − n̂n̂ᵀ)` that correct arithmetic in one orientation would not guarantee. Asserted over 15 (polar, azimuth) pairs.
2. **The exact `r²` cancellation.** The yaw and translation margins stand in the ratio `(r_anchor/r_budget)²` identically,
   asserted over radii spanning 20× — an algebraic claim about *placement*, independent of any stiffness or count.
3. **The cable statics identity.** `F_z = T·δ/√(L²+δ²)`, with the tension and the normal force computed by separate code paths.
4. **The anisotropy theorem itself**, checked against the chain it is applied to at three contour lengths and six tensions,
   including its equality case (`1.0000` at vanishing force) and its monotone departure from it.

### The declared falsifiers

| falsifier | fired? | outcome |
|---|---|---|
| 1. a through-layer link stiffer across than along | no | the ratio never exceeds 1; the equality case is reached exactly at zero force |
| 2. a per-anchor force reaching an allowable | **YES — and it is a result** | the 120×-margin in-plane scheme puts **28.7 pN** on one path against a **10 pN unzip** allowable, while the minimum design puts 2.6 pN. **Over-stiffening is not free**, because the anchor force goes as `√(k_BT k)/N` |
| 3. the entropic tether coming out at zero | **no — and this is `CH-0013`** | 0.115–0.266 pN/nm per tether, i.e. 25–58 % of the whole requirement each |
| 4. every scheme over the 10 % stroke budget | no | two topologies come in at 0.26–8.5 % |
| 5. the FJC tangent disagreeing with its Gaussian limit | no | agree to `1e−6` |
| 6. the `r²` cancellation failing | no | exact at every radius tested |
| 7. a corrugation ceiling far above the requirement | **partly** | the electrode ceiling is 2.68× the bound, so the branch is **not excluded** — but the depth it needs costs 36 % of the stroke in dishing. Handed to `T-3b` rather than concluded |

### The predicate, item by item

1. `k_lat ≥ 0.4602` **and** `k_yaw ≥ 368.17` from element mechanics with the end conditions named — **yes**, both end conditions carried throughout;
2. per-anchor force against the **per-path** allowables with `C-0009`'s concentration factor — **yes**, at its worst value, with the reason it is conservative for an in-plane load stated rather than assumed;
3. the cost to the normal direction — **yes**: added stiffness, stroke lost, static preload, and the cable term that the linearisation misses;
4. the anchors' interaction with the layer — **yes**, and the omissions are named with their direction;
5. same anchors as the flatness attachments or additional — **yes: additional**;
6. a scheme passes, or none does — **two pass**, and the one that fails does so for a reason that generalises;
7. five gates with an independent gate 3 — **yes**.

## Result

Filed as [`C-0014`](../claims/C-0014-lateral-confinement.md). **PASS.**

- **The obvious scheme fails, and the reason is the finding.** A strut standing under the tile has the required stiffness
  anisotropy *inverted*: `cEI/(SL²) = 0.006–0.025`, against a floor of 1 that a convexity theorem sets for any flexible
  through-layer link. It takes 87–99 % of the stroke and buckles under the actuation load, at which point its lateral
  stiffness is exactly zero.
- **Two schemes pass.** Eight ssDNA tethers (the theorem's equality case, 5.6 % of the stroke, no frame, and they supply
  9.4 pN of the downward preload `T-13` says nothing in the §3 stack provides), and four surface-parallel 40 nm duplex
  tethers to a coplanar fixed frame (120× and 239× margin, 0.26–1.03 % of the stroke, but a ~100 nm assembly).
- **The yaw budget is not free, and it is decided by a layout choice worth 638–2551×.** Four *radial* 40 nm in-plane
  tethers give `k_yaw` = 34.5–138 pN·nm/rad against the 368 required and **fail**; the same four rotated into the
  *tangential* direction give 88 000 and pass by 239×. Same parts, same count, same cost.
- **Lateral confinement and the §3 desired stroke are in tension, and it is quantified:** `L_min = δ√(S n/2A)`,
  so 28 nm of tether for a 3 nm stroke and **93 nm for a 10 nm stroke** — a `T-2` window constraint on the device footprint,
  not on the physics.

## Feedback into Formulate

- **`T-2` gains a third constraint, and it is geometric.** Beside `C-0006`'s distributed-coupling requirement and
  `C-0010`'s lateral-stiffness requirement, the window now carries a **standoff**: the frame a lateral scheme anchors to
  must sit `L_min ∝ stroke` away, so the 40 × 40 nm tile is a ~100 nm assembly at a 3 nm stroke and a ~230 nm one at 10 nm.
- **`T-13` is partly answered as a by-product.** A taut entropic tether pulls the tile *down* with 4.6–9.4 pN, which is
  exactly the missing preload — and it is the same element that confines it laterally. `T-13` should evaluate that before
  inventing anything else.
- **A new task, `T-15`:** the in-plane load path into the tile is a shear-lag problem on a membrane-loaded lattice and
  nobody has done it. `C-0009`'s 7.6× is an out-of-plane number used here as a bound; if the true in-plane factor is 1,
  every minimum tether length above shrinks by up to `√7.6 = 2.8×` and the 10 nm stroke becomes reachable at 34 nm.
- **`T-3b` inherits a specific question**, not a general one: what fraction of the tile-electrode interaction energy is
  laterally modulated by a 63 nm electrode pattern? Above 37 % the tile is confined laterally with no anchors at all —
  and dished by a third of its stroke.
