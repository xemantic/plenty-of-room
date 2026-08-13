# CH-0023 — `placementPreload` returns a **downward** preload and its documentation reads it as an upward one

| | |
|---|---|
| **Against** | [`C-0017`](../claims/C-0017-output-coupling-stiffness.md) — the `placementPreload` relation `R₀ = k_c s − W(s)` and the sentence that interprets its sign, in `coupling/CouplingRequirement.kt` |
| **Raised by** | [`C-0021`](../claims/C-0021-zero-bias-resting-position.md) (`T-13`) |
| **Date** | 2026-08-13 |
| **Grounds** | methodological — a sign convention stated in one direction and implemented in the other, in the one function that is the interface between `T-16` and `T-13` |
| **Direction** | **favourable to `C-0017`'s programme.** The preload a stiffer coupling needs is *downward*, which is exactly the quantity `T-13` was looking for, not the one `C-0017`'s prose says it cannot have |
| **Status** | raised. **No number, table or verdict in `C-0017` moves.** `placementPreload` appears in no study and in no result file; its only call sites are three assertions in `CouplingRequirementTest`, and every one of them passes with the returned value unchanged |

---

## What is challenged

`coupling/CouplingRequirement.kt`, the KDoc of `placementPreload`, quoted in full because the challenge is to two sentences of it:

> The preload in pN a coupling of [stiffness] must already carry at zero stroke for its operating point to sit at [stroke], given the actuator's [outputForce] there: `R₀ = k_c s − W(s)`.
>
> **Positive means the coupling holds the tile up at zero stroke, which nothing in the §3 stack does; negative means it pulls the tile down, which is `T-13`'s open question and is what a coupling stiffer than the placement value needs.**

The formula is right. The reading of its sign is inverted.

---

## The methodological ground

`C-0017` fixes the convention: **`R(s)` is positive upward.** A linear coupling is then `R(s) = R₀ + k_c s`, and the operating point sits at `s*` when `R(s*) = W(s*)`, so

&nbsp;&nbsp;&nbsp;&nbsp;**`R₀ = W(s*) − k_c s*`.**

The function returns `k_c s* − W(s*)`, which is `−R₀`. So for `k_c` above the placement value:

| `k_c` [pN/nm] | `placementPreload` returns | `R₀` in the claim's own convention | what the coupling actually does at `s = 0` |
|---|---|---|---|
| 33.333 | 0 | 0 | nothing — this is `K2`, and it is right |
| 39.01 | **+17.03** | **−17.03** | **pulls the tile DOWN by 17 pN** |
| 70 | +110 | −110 | pulls the tile down by 110 pN |

The KDoc reads the `+17.03` as *"holds the tile up"*. It holds it **down**. And the next clause — *"negative … is what a coupling stiffer than the placement value needs"* — is the correct physics attached to the wrong sign of the returned value: a stiffer coupling does need a downward preload, and this function returns it as a **positive** number.

`T-13` re-derives the same relation from the other end, factoring the mandate out:

&nbsp;&nbsp;&nbsp;&nbsp;**`F_down = (k_c − k_c*)·δ*`** — every `pN/nm` above §3's own 33.333 is exactly **3 pN of downward preload**,

and asserts it equal to `placementPreload` at five stiffnesses spanning 22× as a gate-3 test. **The two agree to the last bit** (compared absolutely, in pN, because near the mandate they are a catastrophic cancellation of each other). What differs is only the sentence that says which way the force points.

---

## Why it matters, given that no number moves

Because `C-0017` hands `T-13` this exact function as the interface between them, in its *Still open* list:

> **The preloaded branch is not evaluated.** A coupling stiffer than the placement value needs a **downward** preload the layer must carry at zero bias, and three of six layer models have exactly zero stiffness at `L₀`. `T-13`.

That sentence is correct, and it is the opposite of what its own function's documentation says the returned sign means. A `T-13` that had trusted the KDoc would have concluded that a stiffer coupling *lifts* the tile — the one thing that would make the zero-bias problem worse rather than better — and would have looked for the hold-down somewhere else entirely.

**The correction runs the favourable way.** `C-0021` finds that a preloaded coupling is the *cheapest* conceivable hold-down in stroke terms, because the preload it needs is spent against the coupling's own 33 pN/nm rather than against the layer's near-zero stiffness at `L₀`. What stops it being the answer is not the sign but the **topology**: `K2`'s load path is 99.6 % ssDNA spacer, which carries no compression, so it cannot be mounted with a preload of either sign. That is `C-0021`'s finding and it is unaffected by this challenge.

---

## What this does *not* challenge

- **The formula.** `k_c s − W(s)` is the magnitude of the preload and it is correct.
- **`k_c* = 33.333 pN/nm`.** Reproduced exactly by `T-13`.
- **Any verdict, table or result file in `C-0017`.** `placementPreload` is not called by `OutputCouplingStudy`; `grep` finds it in one source file and one test file and nowhere else. Every `C-0017` number stands.
- **`K2`.** Its reaction at zero stroke is evaluated, not assumed, by `T-13` and comes out **exactly zero**, which is what `C-0017` assumed.

## The remedy proposed

Replace the two sentences with:

> **Positive means the coupling must be mounted pulling the tile DOWN by this much at zero stroke** — the preload a coupling stiffer than the placement value needs, and `T-13`'s question. Negative would mean it holds the tile up, which no coupling at or below the placement value requires. The returned quantity is `−R₀` in this file's own upward-positive reaction convention; `R(s) = R₀ + k_c s` with `R₀ = W(s*) − k_c s*`.

`C-0017` is annotated in place with a banner pointing here rather than edited, per `gpd/README.md`.
