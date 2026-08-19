# T-50 — Beyond mean field at the actuated tile-electrode gap: a ceiling and a threshold, not a Monte Carlo

| | |
|---|---|
| **Leaf** | `A7.4`, consumed by `A2.2` and `A8.2` |
| **Priority** | **HIGH — the last unbounded exposure on the Gen-1 critical path** |
| **Verification type** | **logical** (a decomposition of the correction into a level and a gradient, and a threshold read off `C-0017`'s own force balance) **+ in-silico** (the one beyond-mean-field family member this repository already implements, measured; and the level channel's residual leak, measured) **+ literature** (what the family's other members are, and whether any published measurement reaches the gradient) |
| **Depends on** | `C-0005` (the coupling parameters and the 123–214 % one-loop ratio), `C-0017` (the margin at risk, and the force-pinned operating point), `CH-0019` (which raised this task), `CH-0035` / `C-0033` (the level/gradient split and `d ln μ/dh` as a derivative), `C-0008` (the field pipeline), `T-139`'s literature file (the measured DNA-DNA force curves in Mg²⁺) |
| **Raised by** | [`CH-0019`](../challenges/CH-0019-two-mean-field-expansions.md) |

---

## Formulate

### The question, as `CH-0019` left it

`C-0017`'s stability margin at the 10 nm design point in 2 mM MgCl₂ is **1.19–1.42×**.
`C-0005` puts the electrostatic one-loop correction at **123–214 %** of the leading term across the same gaps,
and states that `Ξ = 17–24` sits in the intermediate-coupling gap where *"neither the loop expansion about mean
field nor the virial expansion about strong coupling converges"* — no systematic theory exists there.
`C-0005` prices the only route that does reach it, primitive-model Monte Carlo, at **1–3 weeks of wall clock**,
and it is not run here.

So the deliverable is `P-6`'s shape:
**the largest effect any member of the family reaches, and the value the unknown would need for the answer to
change.** Either half is falsified by a single published measurement.

### The cheap bound that must run first, and what it is a bound ON

`CLAUDE.md`: *"A broken expansion can still be bounded, if the term it corrects is not the term that carries
the answer. Ask what the correction MULTIPLIES before concluding it is unbounded."*

Write the true force as a multiplier on the mean-field one:

&nbsp;&nbsp;&nbsp;&nbsp;`|F_true(h, V)| = μ(h, V) · |F_PB(h, V)|`, `μ > 0`, dimensionless.

`C-0005`'s 123–214 % is a statement about the **level** of `μ − 1` at a gap. `C-0017`'s margin is a statement
about a **stiffness**. `CH-0035` establishes that at a **force-pinned** operating point the level is absorbed
entirely into the bias, so the two quantities are not the same quantity and the second does not inherit the
first's error bar. **Whether the binding states are force-pinned is therefore the first thing to check, and it
is checkable from `C-0017`'s own result file with no solve at all.**

### Locked units and sign conventions

- `z` is normal to the electrode, positive **away** from it; the electrode is `z = 0`;
  **the electrostatic gap IS the layer height, exactly** (`C-0012`).
- The **stroke** `s = L₀ − h` is positive **downward**; **`L₀` is a FORCE-ONSET height** (`C-0011`, `CH-0010`).
- **The beyond-mean-field multiplier** is `μ(h, V) ≡ |F_true(h, V)| / |F_PB(h, V)|`, dimensionless,
  `μ > 1` an enhancement. It is the same *shape* of object as `C-0033`'s collar multiplier and it is a
  **different physical correction**; the two compose multiplicatively and their gradients add.
- **`g ≡ d ln μ/dh` in `nm⁻¹`, taken at FIXED APPLIED BIAS.** `g > 0` means the true force decays *more
  slowly* than the mean-field one.
- `k_es = |F_es| d ln|F_es|/dh`, `ℓ = −1/(d ln|F_es|/dh)`, so **`k_es = −|F_es|/ℓ` identically**;
  `k_es < 0` above the force maximum.
- The **stability floor** is `|k_eff| = |F_es|/ℓ − k_brush` where it is positive, and `0` where `k_eff > 0`.
  The **margin** is `33.3333 pN/nm / floor` (`C-0017`'s mandate, `C-0049`'s clause).
- `Ξ = q² l_B/μ_GC` is a property of ONE surface; `Γ = √(Ξ/2)`; `μ_GC = 1/(2π q l_B σ_s)`.
- Lengths nm, forces pN, pressure `pN/nm² = 1 MPa` exactly, stiffness pN/nm, bias V,
  buffer mM MgCl₂ (2:1, so `I = 3c`), `k_BT = 4.142 pN·nm` at 300 K, `l_B = 0.7141 nm`.

### Acceptance predicate

**`P1` — the level channel, measured rather than asserted.**
Show at every one of `C-0017`'s 54 states whether the operating point is force-pinned, and measure how much of
a *level* correction survives into the stability floor. `CH-0035` asserts *exactly zero*; the assertion has a
premise (that `ℓ` does not depend on the bias) which is **not** in the identity and is not tested anywhere in
the corpus. Report the residual leak over a level multiplier spanning `C-0005`'s own 123–214 % in **both**
directions, and report what the required bias does — a level correction that is free in the stiffness is not
necessarily free in the bias ceiling.

**`P2` — the threshold.**
The gradient `g* = d ln μ/dh` at which `C-0017`'s margin reaches 1.0, at every state where a floor exists,
in `nm⁻¹` and as the fractional change in decay length it corresponds to. Report the binding (smallest `|g*|`)
state and its buffer.

**`P3` — the ceiling.**
The largest `|d ln μ/dh|` any member of the beyond-mean-field family reaches at the operating gaps, assembled
from: (a) the one member this repository implements — finite ion size (Bikerman) — **measured**, with its own
mesh and difference-step convergence; (b) the far-field theorem that a per-surface correction is pure level and
the gradient is a **bulk** decay-constant difference, with that difference computed for MgCl₂ at 0.5–10 mM;
(c) `C-0005`'s own one-loop ratio read as a shape, with its inapplicability stated; (d) the rigorous favourable
bound `g < 1/ℓ_PB`. Say which of these are bounds and which are estimates.

**`P4` — does the Mg²⁺-does-not-condense bound transfer to oppositely charged walls?**
A plain yes/no with the reason, and — if no — what does bound the oppositely charged case.

**`P5` — the verdict on `C-0017`'s 10 nm point**, stated as one of: the correction cannot move it; the
correction can move it and the size is X; or no method reaches it and here is what would.

**`P6` — the bias the level correction demands.**
A level correction is absorbed into the bias, so report what that bias does against `C-0005`'s 0.197 V
point-ion boundary, `C-0017`'s 1 V trusted ceiling and §3's 2 V — and whether any level in the band is
unreachable at all. *Added during execution, and it is the half of `P1` that turned out to have a verdict
in it: `|F_es|` saturates in the bias, so a large suppression of the force cannot be delivered at any bias.*

### Verification gates

1. **Dimensional** — `g` in `nm⁻¹`; `|F_es| g` in pN/nm; `Ξ` dimensionless; `κ` in `nm⁻¹`.
2. **Limiting cases** — `μ ≡ 1` reproduces `C-0017` bit for bit; `g → 0` leaves every margin unmoved;
   a pure-capacitor gap (no mobile ions) has `g = 1/ℓ_PB` exactly.
3. **Symmetry and conservation** — the pinned force is the same at every buffer at fixed `(model, height)`
   (`CH-0035`'s own gate); the Bikerman solve conserves the gap's charge balance.
4. **Numerical convergence** — the Bikerman gradient converged in the mesh AND in the difference step,
   separately from the multiplier it differentiates (`CLAUDE.md`: a gradient converges worse).
5. **Literature cross-check** — the measured decay length of the DNA-DNA electrostatic force against the
   bulk Debye length, from `gpd/data/T-139-dna-dna-force-literature.md`; and a recorded search for any
   published beyond-mean-field treatment of **oppositely** charged walls in an asymmetric electrolyte.

### Declared falsifiers

- **`F1`** — the binding states are **not** force-pinned. Then the whole level/gradient split is unavailable
  and the exposure is the full 123–214 %.
- **`F2`** — the level channel's residual leak is **not** zero at the file's own precision. `CH-0035` says it
  is exactly zero; if it is not, that is a challenge against a standing claim.
- **`F3`** — a measured family member's `|d ln μ/dh|` **exceeds** the threshold `|g*|` at any 10 nm state.
  Then the exposure survives and `P5` must say so.
- **`F4`** — the Bikerman gradient is not converged to better than the spread it is being asked to resolve.
- **`F5`** — the operating gap is not in the far field, so the asymptotic level/gradient separation theorem
  does not apply there. (Expected to fire: `C-0017`'s `ℓ` at the 10 nm state is 2.77–2.99 nm against a
  3.927 nm bulk Debye length.) If it fires, the far-field route is a *structure* argument only and the
  ceiling must rest on the measured member.
- **`F6`** — a level correction inside `C-0005`'s band pushes the required bias past a ceiling
  (`C-0005`'s 0.197 V point-ion boundary, `C-0017`'s 1 V trusted ceiling, §3's 2 V).

## Plan

### Method, and its justification against cost

**The expensive calculation is named and declined, with its price.** `C-0005` costs primitive-model Monte
Carlo of the 40 × 40 nm gap at **1–3 weeks of wall clock** over 9 state points and states that the regime has
no systematic theory to grade the result against. This box has 8 cores shared with three concurrent agents.
The task therefore delivers `P-6`'s ceiling-and-threshold instead, and the threshold is the half that is
**exact**: it is read off `C-0017`'s own force balance by one division per state and needs no field solve.

**Order of work, cheapest first.**

1. **No solve at all.** Read `T-16`'s 54 requirement records. Confirm force-pinning (the same
   `electrostaticForceAtTarget` at every buffer, fixed `(model, height)`). Compute `g*` per state.
2. **No solve at all.** The favourable bound `g < 1/ℓ_PB`, and `C-0005`'s one-loop ratio differenced.
3. **Cheap solves.** `ℓ_PB(g, V)` at fixed gap over a bias ladder spanning the level correction, at the three
   binding states — the residual-leak measurement. Tens of 1-D PB solves.
4. **Cheap solves.** The Bikerman member: a gap sweep at fixed bias, point ions against a hydrated close-packed
   ceiling, `μ_B(h)` and its gradient, with mesh and difference-step convergence.
5. **Arithmetic.** The bulk decay-constant corrections (finite ion size in the MSA closure, and Bjerrum ion
   association), both derived rather than cited, and cross-checked against literature where one is found.

### What would falsify this approach

If the level/gradient split does not hold — if the binding states are not force-pinned, or the residual leak is
comparable to the threshold — then the correction reaches the answer as a level and the 123–214 % is the honest
error bar on the margin. The task then reports **`P5` = no method here reaches it** and hands NDI the Monte
Carlo costing, which is the outcome `CH-0019` predicted and which this task must be willing to return.
