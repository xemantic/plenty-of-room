# CH-0011 — `C-0008`'s "`k_es < 0` everywhere" is a universal drawn from a bounded sample, and the sign reverses in the region the actuator actually operates in

| | |
|---|---|
| **Challenges** | [`C-0008`](../claims/C-0008-electrostatic-force-and-decay-length.md), its statement *"**`k_es < 0` everywhere**, as §1 requires"*, its gate-3 item *"`k_es < 0` asserted at every working gap"*, and the reading of §1 those license: that the electrostatic contribution to stiffness is a **softening** term wherever the actuator is |
| **Raised by** | [`C-0012`](../claims/C-0012-coupled-stroke-and-blocking-force.md), task [`T-3`](../tasks/T-3-stroke-and-blocking-force.md) |
| **Raised** | 2026-08-13, iteration 5 |
| **Status** | **OPEN** — filed against a universal quantifier, not against a computed number. Every `k_es` `C-0008` tabulates is reproduced and upheld. |

---

## What is *not* challenged

Every number in `C-0008`. Its `F_es(h, V)` table is reproduced **to the digit** by `T-3` through the same
solver — −490 / −215 / −74 pN at 0.25 V and −938 / −353 / −109 pN at 2 V, 2 mM, at 5 / 7 / 10 nm — and its
`k_es` values, its decay lengths, its `σ_eff` saturation constants and its resolution of `CH-0004` all stand
untouched. Nor is §1's sign convention challenged: where the force decays with the gap, `k_es` is negative, and
that is exactly what `C-0008` computed.

Nor is this the mean-field matter. `C-0005`'s uncontrolled one-loop correction is a separate and larger problem,
and it is inherited rather than disputed.

## The standing statement being challenged

> **`k_es < 0` everywhere**, as §1 requires. The §1 form `|k_es| ≈ F_es/λ_D` is **wrong in the direction that
> matters** — it *understates* the softening — over the whole §3 box, by a factor between **1.00 and 2.64**.

The second sentence is right. The first generalises a bounded sample into a universal.

## The methodological grounds

**`C-0008`'s gap sweep starts at 3 nm. `T-3`'s coupled operating point does not.**

`C-0008` samples `GAPS = [3, 4, 5, 6, 7, 8, 10, 12, 15, 20, 25, 30] nm` and finds `k_es < 0` at every one of
them. That is a correct result about that interval. It is then written as *"everywhere"*, and the claim's
validity range — which is unusually thorough about mean field, point ions, the Stern capacitance, the tile's
charge model and the 1-D reduction — names **no minimum gap at all**.

The minimum gap matters because `|F_es(h)|` is **not monotone**. `C-0008` itself establishes the ingredient:
at zero bias the force *changes sign between 4 and 5 nm*, because the tile's confined counterion cloud exerts
an osmotic repulsion that eventually beats the Maxwell attraction. Under bias the same competition operates,
displaced to smaller separation. So `|F_es|` rises as the gap closes, reaches a **maximum**, and then falls
toward a sign change. Above the maximum `k_es < 0`; **below it `k_es > 0`, and the electrostatics *stiffens*
the layer rather than softening it.**

`T-3` locates both features by re-running `C-0008`'s own solver on a grid extended down to 0.5 nm:

| 2 mM `MgCl₂` | 0.02 V | 0.05 V | 0.10 V and above |
|---|---|---|---|
| gap below which `F_es` is **repulsive** | 1.107 nm | 0.546 nm | below 0.5 nm |

and across the whole (buffer, bias) sweep the sign change sits at **0.55 – 1.58 nm**.

## Why this is a challenge rather than a note

Three reasons, in increasing order of consequence.

### 1. The region where the sign is wrong is not a corner case — it is where the unloaded actuator sits

The coupled force balance of `C-0012` puts the **free** operating point at **0.98–1.12 nm** at 1 V and 2 mM,
across all three §3 layer heights, because the electrostatic force grows as the gap closes faster than the
layer's osmotic pressure rises over most of the range. **386 of the 810 free operating points in `T-3`'s sweep
have `k_es > 0`.** A downstream task that inherits "`k_es < 0` everywhere" gets the sign wrong at nearly half
the operating points it is asked about.

### 2. It inverts a conclusion `T-4` was about to draw, and inverts it in the favourable direction

`C-0008`'s hand-off to `T-4` is a table of *"the applied bias at which `|k_es|` equals `k_brush`"*, read as the
onset of pull-in, with the warning that beyond it the softening runs away — because on a monotone-`|F_es|`
picture nothing stops it. §1 frames the same expectation: *"a displacement toward the electrode increases the
force driving it"*, and asks *"whether that divergence removes the instability, or merely bounds it"*.

On the corrected reading there are **two** arresting mechanisms, not one. The osmotic divergence §1 names is
the first. The second is that **the driving force itself stops growing and then reverses** — an *electrostatic
stopper*, entirely absent from §1's picture and from `C-0008`'s. It is why every one of `T-3`'s 810 coupled
solves converged to a bounded equilibrium and why only 4 of them showed a second equilibrium below the first.

The direction is favourable, which is the direction in which an error survives longest — `CH-0007` made exactly
this argument about `C-0005`, and it applies again here. An overstated instability does not make a design
safer; it makes a computable region look uncomputable and pushes work toward an expensive method.

### 3. It changes what `T-4`'s deliverable *is*

§6 task 4 asks for *"either a maximum usable bias with margin to the operating point, or a demonstration that
the osmotic divergence removes the instability"*. Under the challenged reading only the first branch is
available. Under the corrected one, **neither branch is quite right**: the instability is removed, but not by
the osmotic divergence alone, and there is still a maximum usable bias — set not by pull-in but by the point at
which the operating point leaves `C-0005`'s and `C-0002`'s validity ranges (`C-0012`: at about **0.1 V**, an
order of magnitude below any pull-in estimate). `T-4` should be re-formulated around that, and it cannot be
while "`k_es < 0` everywhere" stands.

## What follows, and what does not

**Does not follow.**

- That any of `C-0008`'s numbers are wrong. They are reproduced to the digit.
- That §1's sign convention is wrong. It is right above the force maximum, which is the whole of `C-0008`'s
  sampled range and the whole of the *loaded* operating range in `C-0012`.
- That pull-in is not a problem. `C-0012` finds `k_eff < 0` at **428 of 810** *loaded* operating points, at gaps
  of 2–7 nm where `k_es` is firmly negative. The instability is real; what this challenge changes is what
  happens *after* it fires.
- That the sub-1.5 nm region is now computable. It is not: it is inside `C-0005`'s correlation band, where
  mean-field PB cannot produce the physics at all, and above `C-0002`'s concentrated crossover, where the
  layer's osmotic exponent is not the one being used. **The sign of `k_es` there is a statement about the
  model, not about the device.** That is precisely why it must be stated rather than assumed away: a model that
  is used outside its range should be reported as saying something specific, not as saying nothing.

**Does follow.**

1. **`C-0008`'s statement should be restated with its gap range**: `k_es < 0` for gaps above the force maximum,
   which is everywhere in `C-0008`'s 3–30 nm sweep; the sign reverses at 0.55–1.58 nm depending on buffer and bias.
2. **`TASKS.md`'s standing findings should carry the reversal**, because `T-4` is promoted to high and consumes them.
3. **`T-4` should be re-formulated**: the maximum usable bias is set by the validity boundary of the upstream
   claims at the coupled operating point, not by `|k_es| = k_brush` at the resting height.
4. **The escalation this exposes is `T-6b`/explicit ions at *small* gap**, not at high bias. `C-0005` priced the
   explicit-ion route at 1–3 weeks; the state points that would need it are the 1–1.5 nm ones, not the 5–10 nm ones.

## If this challenge is itself wrong

The way it fails is through the small-gap physics of the solver rather than through the argument.

At a 1 nm gap the tile-electrode separation is smaller than one hydrated Mg²⁺ diameter (0.856 nm) plus a Stern
layer at each wall, so the continuum picture that produces the osmotic repulsion is being asked for a number in
a region where "a continuum of point ions between two smooth planes" is not what is there. If the true
small-gap physics is dominated by steric contact and specific ion adsorption rather than by a diffuse-layer
osmotic pressure, the *magnitude* of the reversal is not to be trusted.

Two things limit that escape. First, the **direction** does not depend on the continuum detail: any mechanism
that resists bringing two like-signed counterion clouds together produces the same sign, and `C-0008`'s own
Bikerman bracket — finite ion size — *raises* `|F_es|` and therefore moves the maximum to **larger** gap, into
the region the models do cover. Second, `C-0008` already found the zero-bias sign change at 4–5 nm, in the
middle of its own trusted range, by the same mechanism. **The existence of the reversal is established inside
`C-0008`'s validity range; only its location under bias is extrapolated.**

## Resolution

**Open.** `T-4` is the task that decides it, by locating the force maximum and the fold together and reporting
which of them the actuator meets first. Until then, `T-4` and `T-2` should quote `k_es`'s sign **with the gap it
applies to**, and should not treat the softening as unbounded below.
