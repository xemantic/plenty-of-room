# CH-0017 — The collapse is arrested by the layer, not by the electrostatic reversal: `CH-0011`'s sign change is real, and it is not the stopper

| | |
|---|---|
| **Against** | [`CH-0011`](CH-0011-electrostatic-stiffness-changes-sign.md) — its second statement, *"the instability is removed, but not by the osmotic divergence alone"*, and specifically *"the collapse **is** arrested, but by `k_es` reversing sign at 0.55–1.58 nm, not by the osmotic divergence"* as that sentence stands in `C-0012`, in `ANSWERS.md` §6 task 4 and in `TASKS.md`'s standing findings |
| **Raised by** | [`C-0018`](../claims/C-0018-maximum-usable-bias.md) (`T-4`) |
| **Date** | 2026-08-13 |
| **Grounds** | methodological — a mechanism named from the *existence* of a feature rather than from a comparison against the alternative mechanism, and never tested against it. Both stoppers exist; only one of them is ever reached |
| **Direction** | **unfavourable to `CH-0011`, favourable to §1.** `CH-0011` itself warns that a favourable direction is the one in which an error survives longest, and its own arresting mechanism was the favourable-sounding one: it made the sub-nanometre region look self-limiting for a reason that does not operate there |
| **Status** | raised. **`CH-0011`'s FIRST statement is upheld and is now asserted as four executable tests** on the real Poisson-Boltzmann solver. Only the mechanism clause is challenged, and no number in `CH-0011` or `C-0012` is disputed |

---

## What is *not* challenged, and is now nailed down

`CH-0011`'s primary content — that `C-0008`'s *"`k_es < 0` everywhere"* is a universal drawn from a sample
whose smallest gap is 3 nm — stands, and `T-4` converts it from a reported observation into assertions
(`src/test/kotlin/actuator/ElectrostaticStopperTest.kt`, four tests, on `C-0008`'s own solver at its own tile
charge):

1. `k_es < 0` at 5, 7 and 10 nm at 0.25 V — `C-0008` reproduced;
2. `|F_es|` has a **maximum**, and it is **below 3 nm** — inside the interval `C-0008` never sampled;
3. `k_es` **changes sign there**: negative 0.3 nm above it, positive 0.15 nm below it;
4. the force turns outright **repulsive** below a gap inside `C-0012`'s reported 0.55–1.58 nm band, with the sign change verified on both sides.

The located force maximum runs **0.65 – 2.59 nm** over 18 (buffer, bias) states, and `k_es` reverses at
15 of them; at the other three the maximum has moved below the 0.35 nm sampled floor. **All of that is
`CH-0011`, and it is now in the test suite rather than in prose.**

## The statement being challenged

> On the corrected reading there are **two** arresting mechanisms, not one. The osmotic divergence §1 names is
> the first. The second is that **the driving force itself stops growing and then reverses** — an *electrostatic
> stopper* […] It is why every one of `T-3`'s 810 coupled solves converged to a bounded equilibrium.

The existence of the second mechanism is established. **What was never checked is which of the two the tile
actually meets** — and that is a comparison, not an existence claim.

## The methodological ground

Two counterfactuals settle it, and `T-4` computes both at 324 states — 3 heights × 6 layer models × 3 buffers
× 6 biases from 0.02 V to 1.0 V:

- **Remove the layer** (`P ≡ 0`): the tile stops only where `F_es` changes sign — the **repulsion onset**, which is `CH-0011`'s own 0.55–1.58 nm and this task's 0.42–1.62 nm.
- **Remove the electrostatic reversal** (hold `|F_es|` at its maximum below the peak, which is conservative — a force that kept *growing* would stop the tile deeper still): the tile stops where the layer alone carries that force, the **osmotic stopper**.

Whichever sits at the **larger gap** is the one the descending tile meets first.

| applied bias | osmotic stopper [nm] | electrostatic stopper (repulsion onset) [nm] | arrested by |
|---|---|---|---|
| 0.02 V | 3.24 – 8.54 | 0.74 – 1.62 | **osmotic**, 54 of 54 |
| 0.05 V | 2.17 – 5.47 | 0.42 – 0.75 | **osmotic**, 54 of 54 |
| 0.10 V | 1.35 – 3.38 | below the 0.35 nm sampled floor | **osmotic**, 54 of 54 |
| 0.25 V | 0.67 – 1.92 | below the floor | **osmotic**, 54 of 54 |
| 0.50 V | 0.62 – 1.68 | below the floor | **osmotic**, 54 of 54 |
| 1.00 V | 0.62 – 1.68 | below the floor | **osmotic**, 54 of 54 |

&nbsp;&nbsp;&nbsp;&nbsp;**324 of 324. The layer stops the tile between 1.9× and 5× further out than the field
could, and above 0.05 V the field's own stop is not even inside the region any model in this programme covers.**

The reason is structural rather than numerical: the repulsion onset moves to **smaller** gap as the bias rises,
because a stronger field needs more confined-counterion pressure to cancel it — while the osmotic stopper also
moves inward but is anchored by a pressure that **diverges** at the dry thickness. The two run the same way and
the divergent one always wins. This is exactly §1's expectation, and §6 task 4's second branch, restored.

## What `CH-0011` got right, and where the distinction lies

The tile **does** pass the force maximum before it stops: at 1.0 V and 2 mM the maximum is at 1.75 nm and the
osmotic stopper at 0.62–1.68 nm, so at the arrest point `k_es > 0` and the electrostatics is **stiffening** the
layer. That is `C-0012`'s *"386 of 810 free operating points have `k_es > 0`"*, and it is real.

> **But passing the point where a force stops growing is not being stopped by it.** `CH-0011` slid from
> *"`k_es` reverses before the tile stops"* — true — to *"the reversal is what arrests the collapse"* — false.
> The reversal changes the **stiffness** at the arrest point; the **position** of the arrest is set by the
> layer, and removing the layer moves it inward by a factor of 1.9 to 5 or off the bottom of every model in
> the programme.

## What follows, and what does not

**Does follow.**

1. **`CH-0011`'s mechanism clause should be struck** and replaced by: *"`k_es` reverses at 0.42–1.62 nm and stiffens the layer at the arrest point; the arrest itself is osmotic."*
2. **§6 task 4's second branch is answered YES for the free tile** — `C-0018` finds no fold at all at 49 of 54 unloaded states — and the mechanism is the one §1 proposed. The programme has been carrying the opposite.
3. **`ANSWERS.md` §6 task 4 and `TASKS.md`'s standing finding** *"`k_es` changes sign at 0.55–1.58 nm, and that is what arrests the collapse"* need the second half replaced.
4. **The escalation `CH-0011` proposed — explicit ions at 1–1.5 nm — is worth less than it looked.** It was justified by the electrostatic stopper being load-bearing. It is not: the arrest sits at 0.62–3.4 nm and is a *polymer* number there, so the beyond-mean-field question that matters is the layer's (`T-1f`), not the field's.

**Does not follow.**

- That `C-0008`'s numbers or `CH-0011`'s located sign change are wrong. Both are reproduced and asserted.
- That the sub-nanometre region is now computable. It is not: it is inside `C-0005`'s 1.46 nm correlation band and above `C-0002`'s `φ = 0.2` crossover. **This challenge inherits `CH-0011`'s own caveat in full — the sign of `k_es` there, and the position of both stoppers, are statements about the model and not about the device.** What changes is which model statement the programme carries.
- That anything upstream of the collapse moves. The fold, the ceilings and the margins of `C-0018` are computed on the *stable* branch at gaps of 1.5–7 nm and do not depend on either stopper.

## How this challenge would fail

Three ways, and the first is the one to watch.

1. **The osmotic counterfactual uses `C-0003`'s free energy at `φ` between 0.1 and 0.5**, which is past `C-0002`'s concentrated crossover — the des Cloizeaux exponent is not the one the layer is entitled to there. If the true layer is much *softer* than `C-0003` at high `φ`, the osmotic stopper moves inward and could cross the electrostatic one. It would have to be softer by roughly the factor that separates the two stoppers, 1.9–5× in gap, which at `Π ∝ φ^(9/4)` is 4–40× in pressure.
2. **The repulsion onset is located at a 0.35 nm floor.** Below 0.1 V it is resolved and above it is not, so the high-bias rows say "at most 0.35 nm" rather than a number. That is the correct direction for this challenge — a *smaller* electrostatic stopper strengthens it — but a solver artefact at the floor would matter if it ran the other way.
3. **Both counterfactuals are static.** A tile that arrives at the stopper with kinetic energy is not covered, and neither is the drainage that would damp it (`C-0004`).
