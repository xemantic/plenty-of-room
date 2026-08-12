# CH-0007 — `C-0005`'s point-ion boundary is a diffuse-layer potential and was compared against an applied bias

| | |
|---|---|
| **Challenges** | [`C-0005`](../claims/C-0005-mean-field-screening-validity.md), its statement *"Point-ion PB fails above ≈ 0.197 V of diffuse-layer drop at 2 mM … The §3 target is **≤ 2 V — a factor of 10 above**"*, and the standing finding `TASKS.md` carries from it: *"Point-ion PB at the electrode dies above ~0.197 V of diffuse-layer drop — 10× below the §3 ≤ 2 V target."* |
| **Raised by** | [`C-0008`](../claims/C-0008-electrostatic-force-and-decay-length.md), task [`T-3a`](../tasks/T-3a-nonlinear-pb-profile.md) |
| **Raised** | 2026-08-12, iteration 4 |
| **Status** | **OPEN** — filed against a comparison, not against a computed number. The 0.197 V itself is reproduced and upheld. |

---

## What is *not* challenged

`C-0005`'s 0.197 V is **correct and is re-derived here to 0.19657 V** at 2 mM, from the same construction: the diffuse-layer potential at which the Boltzmann factor for `Cl⁻` first demands a contact density above close packing of hydrated ions. The 0.097 V for `Mg²⁺` at a negative electrode is likewise not challenged. Nor is the Stern-limited electrode charge of ~1.25 e/nm² per volt, which `C-0005` also supplies and which is in fact the number that repairs the comparison.

Nor is this the `σ_eff` matter. `C-0005` flagged its own charge-saturation ceiling as an order-of-magnitude statement and `T-3a` found it 24 % high, exactly as predicted — that is a **fulfilled** self-assessment, not a contradiction, and it is filed as a finding in `C-0008` rather than as a challenge.

## The standing statement being challenged

> **Point-ion PB fails above ≈ 0.197 V of diffuse-layer drop** at 2 mM (`Cl⁻` counterion at a positive electrode; 0.097 V for `Mg²⁺` at a negative one). The §3 target is **≤ 2 V — a factor of 10 above**.

The first sentence is right. The second compares it against a quantity of a different kind.

## The methodological grounds

**A diffuse-layer potential and an applied bias are not the same quantity, and `C-0005` supplies the element that separates them without using it.**

The interfacial potential drop at a polarisable electrode divides between a **compact (Stern) layer** and the **diffuse layer**, in series:

&nbsp;&nbsp;&nbsp;&nbsp;`V = σ_e/C_S + ψ_d`

`C-0005` itself establishes that above the point-ion threshold "the compact layer carries the potential and the electrode charge is **Stern-limited at ≈ 1.25 e/nm² per volt**". That is precisely the statement that the compact layer takes most of a large bias — and it is then set aside when the comparison with 2 V is made.

Because `σ_e(ψ_d)` grows **exponentially** in `ψ_d` while the compact term is **linear** in `σ_e`, the series divides the bias very unevenly, and increasingly so as the bias rises. Solving the coupled problem (`T-3a`, 2 mM `MgCl₂`, 20 µF/cm², 5 nm gap, Manning-renormalised tile):

| applied bias `V` | diffuse drop `ψ_d` | compact drop | compact share | `σ_e` | point-ion PB |
|---|---|---|---|---|---|
| 0.10 V | 0.0338 V | 0.066 V | 66 % | 0.083 e/nm² | valid |
| 0.25 V | 0.0954 V | 0.155 V | 62 % | 0.193 e/nm² | valid |
| 0.50 V | 0.1486 V | 0.351 V | 70 % | 0.439 e/nm² | valid |
| **1.00 V** | **0.1942 V** | 0.806 V | 81 % | 1.006 e/nm² | **valid — just** |
| 1.50 V | 0.2186 V | 1.281 V | 85 % | 1.600 e/nm² | 1.11× past |
| **2.00 V** | **0.2353 V** | 1.765 V | **88 %** | 2.203 e/nm² | **1.20× past** |

**The applied bias that produces 0.197 V of diffuse-layer drop is ≈ 1.0 V, not 0.197 V.** At the §3 ceiling of 2 V the diffuse layer carries **0.235 V — 1.20× the boundary, not 10×.**

## Why this is a challenge rather than a note

Three reasons, in increasing order of consequence.

### 1. It changes the verdict on the §3 bias range, not merely a number

"A factor of ten above the validity boundary" reads as *nothing in the §3 bias range is trustworthy*. `TASKS.md` carries it in exactly that form as a standing finding, and `T-3` was constrained by it: *"**May not** use … Gouy-Chapman electrode charge above ~0.2 V"*. The correct statement is that **the whole §3 bias range up to ~1 V is inside point-ion validity**, and the top half of it is marginally outside — a factor of 1.2, not 10.

### 2. The direction is favourable, which is the direction in which an error survives longest

An overstated invalidity is not conservative in the useful sense: it does not make a design safer, it makes a computable region look uncomputable and pushes work toward an expensive method that is not needed. `C-0005` itself names size-modified PB and then explicit-ion simulation as the escalation path "for any bias above ~0.2 V". On the corrected reading, that escalation is not triggered anywhere `T-3a` needs an answer.

### 3. It is load-bearing for the acceptance predicate of §6 task 3

`C-0008` finds the §3 100 pN target reached at **0.067–0.15 V of applied bias** at 5–7 nm in 2 mM, where `ψ_d` is 0.018–0.050 V — a factor of **4 to 11 below** the point-ion boundary. Under the challenged reading, one would have had to say that even 0.2 V was past the boundary and that the force answer was therefore untrustworthy. Under the corrected reading the 100 pN answer sits comfortably inside the region where point-ion PB is its own best theory. **The challenge is what allows §6 task 3 to be answered at all rather than deferred.**

## What follows, and what does not

**Does not follow.**

- That 0.197 V is wrong. It is right, and it is re-derived.
- That mean-field PB is *valid* in the working range. It is not — `C-0005`'s coupling-parameter result stands untouched, and the one-loop correction is still 123–214 % of the leading term at 5–10 nm. This challenge is about the **point-ion** boundary at the **electrode**, which is a different failure mode from the **correlation** boundary at the **tile**.
- That the 2 V column is now trustworthy. It is 1.2× past the point-ion boundary, and separately it is outside the aqueous electrochemical window (1.23 V thermodynamic), which no model in this project addresses.

**Does follow.**

1. **`TASKS.md`'s standing finding should be restated**: point-ion PB at the electrode dies above ~0.197 V **of diffuse-layer drop**, which corresponds to **≈ 1.0 V of applied bias** through a 20 µF/cm² compact layer, not to 0.197 V of applied bias.
2. **`T-3`'s constraint should be restated**: "may not use Gouy-Chapman electrode charge above ~0.2 V" becomes "above ~1 V of applied bias", and in any case the electrode charge should come from the Stern series rather than from Gouy-Chapman alone.
3. **The Stern capacitance is promoted from a decorative citation to a load-bearing one.** It now sets the mapping between applied bias and diffuse-layer potential, hence the location of the boundary in the variable §3 actually specifies. `T-6b` should sharpen it, or replace the series model with a size-modified treatment that has no separate compact layer at all.
4. **The escalation path in `C-0005`'s "where explicit ions become necessary", item 2, is not triggered** anywhere in the region where `T-3a` computes a force.

## If this challenge is itself wrong

The way it fails is through the compact-layer model. A 20 µF/cm² capacitance that is *constant to 2 V* is a strong assumption: real compact layers show potential-dependent capacitance, and at 2.2 e/nm² the electrode charge is in a regime where ion crowding at the outer Helmholtz plane is itself the physics the Stern model coarse-grains away. If the true compact capacitance falls steeply with charge, more of the bias reaches the diffuse layer and the boundary moves back down toward `C-0005`'s reading.

Two things limit that escape. First, the mapping is **logarithmically stiff**: because `σ_e` is exponential in `ψ_d`, halving `C_S` moves `ψ_d` at 2 V by only tens of millivolts. Second, the region that matters for the 100 pN answer is 0.07–0.15 V of applied bias, where the compact share is 62–66 % and the diffuse drop is under 0.05 V — a factor of four below the boundary. **The conclusion that the force target sits inside point-ion validity survives any plausible revision of `C_S`.** What would not survive is the specific claim that 2 V corresponds to 0.235 V.

## Resolution

**Open.** `T-6b` is the task that decides it, by sharpening or replacing the compact-layer model. Until then, `T-3` and `T-4` should quote the boundary in **applied bias** as ~1 V and state which compact-layer model they used to get there.
