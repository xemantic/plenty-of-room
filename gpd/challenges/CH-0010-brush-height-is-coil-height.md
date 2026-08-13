# CH-0010 — The Gen-1 layer's height is a coil height, and both `T-1c` profile models omit the term that sets it

| | |
|---|---|
| **Challenges** | [`C-0003`](../claims/C-0003-crossover-valid-layer-response.md) — its height relation `L₀ = 0.1867 N σ^(5/13)`, the `N(L₀)` bracket that follows from it, and the placement of the layer at `φ/φ# = 1.40–3.51`; and the `L₀/R₀ ≥ 1` brush criterion `P-5` adopted on `T-1c`'s recommendation |
| **Raised by** | [`C-0011`](../claims/C-0011-scf-density-profile.md), task [`T-1d`](../tasks/T-1d-scf-density-profile.md) |
| **Raised** | 2026-08-13, iteration 4 |
| **Status** | **Upheld in substance, and split.** `C-0003` is not withdrawn: its *response* numbers — stroke and secant stiffness — survive inside their own brackets. What is challenged is the height relation, the chain length, the volume fraction, and the criterion. |

---

## The standing statements being challenged

`C-0003`, in one line:

> **`L₀ = 0.1867 · N · σ^(5/13) nm`** (σ in nm⁻²; des Cloizeaux interaction, strong-stretching profile)

with `L₀` **exactly linear in `N`** — *"proved as a test, not observed"* — the chain-length bracket
that follows,

> | 10 nm | 0.0240 | 199.4 | **224.8 – 374.3** | 1.13 – 1.88 | 9.9 – 16.5 |

and the placement

> Consequently the layer sits **further** into the crossover than `C-0002` reported: `φ/φ# = 1.40 – 3.51`
> at the 10 nm point against `C-0002`'s 1.13, and `φ = 0.0326 – 0.0543` against 0.0289.

and, from `TASKS.md` under `P-5`:

> The criterion is **`L₀/R₀ ≥ 1`**, reported as a number at every design point.

## The contradicting result

A numerical SCF density profile against the **same** interaction free energies, the same measured
Kuhn parameters and the same geometry (`C-0011`):

| quantity, at `L₀ = 10 nm`, `σ = 0.024 nm⁻²` | `C-0003` | `C-0011` (solved profile) |
|---|---|---|
| `N` | 224.8 – 374.3 | **62.1** |
| PEG | 9.9 – 16.5 kDa | **2.7 kDa** |
| mean `φ` | 0.0326 – 0.0543 | **0.00900** |
| `φ/φ#` | 1.40 – 3.51 | **0.138** |
| exponent of `L₀` in `N` | **1, exactly** | **0.49 – 0.64** |
| stroke at 100 pN | 3.83 – 6.01 nm | 5.31 nm — **inside** |
| secant stiffness | 16.6 – 26.1 pN/nm | 18.84 pN/nm — **inside** |

and, at **one and the same chain** (`N = 62.1`, `σ = 0.024 nm⁻²`), which removes the definition of
"layer height" from the comparison entirely:

| | Alexander box | strong stretching | solved profile |
|---|---|---|---|
| resting height | 2.152 nm | 2.169 nm | 10 nm at 1 pN; `2⟨z⟩ = 5.459 nm` |
| `P` at `h = 10 nm` | **zero — no contact** | **zero — no contact** | **0.0489 MPa = 78 pN over the tile** |

Provenance: `gpd/results/T-1d-scf-density-profile.json`, `brush.ScfDensityProfileStudyKt`,
19 `SelfConsistentFieldTest` tests green, convergence orders 2.08 and 2.32 in the node spacing and
1.97 in the contour step.

## Methodological grounds

Three, and the first is the one that matters.

### 1. Both profile models omit the chain's entropic resistance to confinement

In the Alexander box, chain elasticity enters the disjoining pressure as a **pull-back** —
`P(h) = Π_int(φ) − 3k_BT σ h n_K/(N b²)` — a term that *reduces* the pressure and whose only job is
to set `L₀`. In strong stretching it does not enter the wall pressure at all: the contact-value
theorem `P(h) = Π_int(φ(h))` holds precisely because at the wall the only chains present have their
free ends there, and a free end carries no tension.

**Neither contains the positive normal stress a chain exerts on a wall that confines it.** A
Gaussian chain of `N` monomers squeezed into a slit of width `h` pushes on the walls with
`P ≈ 2σ k_BT π² D N/h³`, `D = b²/6n_K`, and that term has no counterpart in either model. It is not a
correction here. Under an absorbing wall — which is what a rigid impenetrable tile is — the volume
fraction *vanishes* at the wall, `Π_int(φ(h)) = 0` identically, and **the whole disjoining pressure is
conformational**. `C-0011` verifies this by two independent routes agreeing to 1–2 %: the
thermodynamic `−∂F/∂h` and the continuum contact-value theorem
`P = k_BT (b²/6n_K) lim φ/(v₀(h−z)²)`.

That is why the two models agree with each other to 1 % on the resting height at a fixed chain and
disagree with the solved profile by 4.6×. **They agree because they share a defect, not because they
bracket an answer** — which retires `C-0003`'s statement that the spread between them is a lower
bound on the profile uncertainty. It is a lower bound on the wrong quantity.

### 2. `L₀/R₀ ≥ 1` cannot exclude anything once `L₀` is an onset height, and it was the criterion setting the window's edge

`P-5` was closed by adopting `L₀/R₀ ≥ 1` after `Σ ≥ 5` failed thermodynamically (`CH-0001`) and
geometrically (`CH-0003`). Against a solved profile — whose `L₀` is necessarily defined by where the
layer first resists, because it has no sharp edge — the replacement is **satisfied everywhere**,
including where the chains do not touch each other:

| `L₀` | `σ` | `L₀/R₀` | `Σ = πR₀²σ` | what the layer is |
|---|---|---|---|---|
| 5 nm | 0.0041 nm⁻² | **1.77** | **0.10** | isolated mushrooms, ten coil footprints apart |
| 5 nm | 0.0751 nm⁻² | 2.38 | 1.04 | coils just touching |
| 10 nm | 0.0041 nm⁻² | 1.60 | 0.51 | coils half-overlapping |
| 10 nm | 0.0116 nm⁻² | 1.86 | 1.05 | coils just touching |

The reason is structural rather than accidental: when the layer's outer edge is the tail of a single
coil, `L₀/R₀` measures **how far into the tail the threshold sits** — a property of the threshold, not
of the layer's architecture. It cannot fall below one and therefore cannot exclude anything.

The criterion is not vacuous for `T-1c`'s two models, whose `L₀` is a sharp free-energy minimum: it
empties their 5 nm and 7 nm windows, and at 10 nm it sets the strong-stretching lower edge at
`σ = 0.0176 nm⁻²` — which coil overlap reproduces exactly. **So the objection is not that the
criterion is wrong; it is that it is a proxy that survives only while `L₀` is an ansatz edge, and
`T-2` is going to be quoting windows in whichever convention `T-1d` hands it.**

A 1-D self-consistent field also *needs* lateral homogeneity, so `Σ ≥ 1` is not merely a better
brush criterion here; it is the validity condition of the method that produced these numbers, and
`C-0011` reports the windows with and without it. With it, the 5 nm window closes again and the
10 nm window's lower edge moves from the sweep's floor to `σ = 0.012 nm⁻²`.

### 3. The layer is below the crossover, not above it — which reverses `C-0002`'s placement

`C-0002` located the layer at `φ/φ# = 1.08–1.23` and `C-0003` moved it *up* to `1.40–3.51`,
concluding that correcting the height relation pushes the layer **further into** the crossover. On
the solved profile the mean volume fraction is `0.00900` against `φ# = 0.0651`, i.e. `φ/φ# = 0.138`,
and even the **peak** of the profile only reaches `0.378 φ#`.

So the direction of that correction is reversed too, and by more than the correction itself: no part
of this layer is semidilute, by a factor of three at its densest point. This is the same failure mode
`CH-0002` identified in `CH-0001` — *"concluding a direction from the corrections one happens to
have"* — now applied to `C-0003`, which had the interaction right and the profile wrong.

## What follows, and what does not

**Does not follow.** That `C-0003` is wrong about the interaction. It is not, and `T-1d` used it
unchanged; the three interaction laws still differ by only 1.45× and still barely move the answer.
`C-0003`'s central methodological finding — that a grafted layer has no chain translational entropy,
so its osmotic exponent never falls below 2 — is untouched.

**Does not follow.** That `C-0003`'s response numbers are wrong. The stroke (5.31 nm) and the secant
stiffness (18.84 pN/nm) land **inside** its brackets. Every downstream task that consumes those two —
`T-3`, `T-4`, `T-8` — may keep using `C-0003`'s bracket.

**Does not follow.** That the whole factor of four in `N` is physics. `L₀` in `C-0003` is the edge of
a trial function and in `C-0011` a force-onset height, and they are not the same quantity. Scaling
the solved first-moment thickness by the measured `N^(0.5–0.55)`, a layer whose `2⟨z⟩` is 10 nm
would need `N ≈ 190–210` — brushing the bottom of `C-0003`'s bracket. **Most of the chain-length gap
is the convention.** What is not definitional is item 1: at a fixed chain, `P(10 nm)` is 78 pN and
both `T-1c` models say zero.

**Does follow.**

1. **`L₀` exactly linear in `N` is a property of the ansatz, not of the layer.** The solved exponent
   is 0.49–0.64 at Gen-1 grafting densities, because the resting height is set by the coil's tail
   rather than by an osmotic balance. `N(L₀)` cannot be inverted from a scaling relation here.
2. **`C-0003`'s `φ/φ# = 1.40–3.51` is withdrawn as a placement of this layer**, and with it the
   inference that the layer sits further into the crossover than `C-0002` thought. It sits below it.
3. **`P-5` is re-opened in part.** `L₀/R₀ ≥ 1` must be carried *alongside* coil overlap `Σ ≥ 1`, not
   instead of it, and any window whose edge rests on `L₀/R₀` alone is unbounded below.
4. **The 5 nm and 7 nm "empty under every model" finding does not survive.** With the overlap cut,
   7 nm has a window of `σ ∈ [0.0296, 0.0496] nm⁻²` — narrow, a factor of 1.7, and bounded below by
   coil overlap rather than by stroke. 5 nm is still empty.
5. **The chain length to order is 1.6–3.3 kDa PEG, not 10–16 kDa** — under the force-onset
   convention, and 8–9 kDa under the first-moment one. `T-2` must state which convention its window
   is in, because the polymer to buy differs by a factor of four between them.

## Resolution

`C-0003` is **not withdrawn and not overwritten**. Its height-relation section and its `N(L₀)` table
are annotated in place with a pointer here, and the `φ/φ#` line is struck through, because that is
the one assertion a downstream task could act on and be wrong.

**Outstanding, and queued:**

- **A first-moment-convention inversion.** `C-0011` reports `2⟨z⟩` at every design point but inverts
  `N` on the force-onset height only. Doing the inversion on `2⟨z⟩` as well would separate the
  definitional part of this challenge from the physical part exactly rather than by scaling.
- **Fluctuation corrections.** At `φ ≈ 0.01` the mean-field treatment is furthest from safe, and
  `C-0011` states plainly that it does not bound them.
- **`P-9`** is unchanged in size but changed in place: the layer is now entirely *below* `φ#`, where
  the fitted `αφ^(9/4)` limb is least constrained by data.

**If this challenge is itself wrong**, the way it fails is the boundary condition. Everything here
turns on the tile being *absorbing* for the chains — which is what an impenetrable wall is for a
continuum Gaussian chain, and is why the conformational term is the whole pressure. If a real DNA
origami tile is better modelled as weakly adsorbing, the depletion layer closes, the contact density
becomes finite, and the pressure moves toward `T-1c`'s `Π_int(φ(h))`. `C-0011` prices the opposite
extreme — a *reflecting* wall, which is the softest the boundary can be — and finds it needs 2.1× the
chain to reach the same height while delivering the same stroke to 2 %. Adsorption is outside that
bracket in a way this task has not bounded, and it would be a `T-2` input, not a `T-1d` one.
