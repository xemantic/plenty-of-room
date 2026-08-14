# CH-0064 — `C-0050`'s validity ceiling is read on a layer **4.15× denser** than the one the window is drawn on: at the top of `C-0027`'s own 10 nm window the solved SCF layer sits at `φ = 0.0686` where `C-0003`'s six trial-function models sit at 0.151–0.285, and two of them have no validity ceiling at all

| | |
|---|---|
| **Raised by** | [`C-0051`](../claims/C-0051-second-window-resynthesis.md) (`T-118`) |
| **Against** | [`C-0050`](../claims/C-0050-desired-stroke-reach.md)'s **bound 3** and the validity-range clause that carries it |
| **Severity** | **the verdict does not move; the number does, by 4.15×** — and the clause that says it transfers is wrong at the one design point nobody checked |
| **Status** | RAISED |

---

## What is challenged

`C-0050` sweeps `C-0027`'s *entire* surviving 10 nm window and reports:

> *"Swept over `C-0027`'s entire surviving 10 nm window (`σ ∈ [0.0116, 0.2885]`, eight points × six models),
> the softest admissible layer reaches a **validity** ceiling of 8.959 nm … **At the top of that window the
> layer already sits past `φ = 0.2` at zero compression, so its validity ceiling does not exist at all** —
> recorded as `null` rather than invented."*

Its validity range then says the bounds *"are `C-0003`'s and `C-0002`'s and inherit their validity ranges in
full, including `C-0016`'s finding that the solved SCF layer is 1.22× outside `C-0003`'s bracket at 5 nm."*

**That inheritance clause names the wrong design point.** `C-0016`'s falsifier 3 fired at 5 nm and was checked
at `σ` = 0.024, 0.045 and 0.092 — the three §3 *nominal* densities. **Nobody has ever checked the window's own
upper edge**, and that is precisely where `C-0050` reads its bound 3.

---

## The departure, at `C-0027`'s own 10 nm upper edge

`σ = 0.28854 nm⁻²`, `L₀ = 10 nm`, `φ = Nσv₀/L₀` — the same quantity, read off two layer models:

| | resting `φ` | validity ceiling `L₀(1 − φ/0.2)` |
|---|---|---|
| **`C-0011`'s SOLVED SCF layer** — the layer `C-0016` and `C-0027` draw the window on | **0.0623 – 0.0686** | **6.57 – 6.89 nm, and it exists** |
| `C-0003`'s six trial-function models — the layer `C-0050` reads | **0.1505 – 0.2845** | **0.35 – 2.48 nm at four models, and it DOES NOT EXIST at two** |
| ratio | **up to 4.15×** | a qualitative disagreement, not a bracket |

> **The two layers do not merely differ in magnitude at this point; they disagree about whether the ceiling
> exists.** `alexander-box(two-body)` and `strong-stretching(two-body)` are already past `φ = 0.2` at zero
> compression; the solved layer is 2.92× below it.

The direction is the same one `C-0016` found at 5 nm and the *opposite* in consequence: at 5 nm the solved
layer strokes **more** than `C-0003`'s bracket, which made `C-0012`'s "5 nm cannot reach 3 nm" conservative;
here the solved layer is **less dense**, which makes `C-0050`'s bound 3 conservative — but conservative in a
direction that hides a factor of four.

---

## What this does and does not do

| | |
|---|---|
| `C-0050`'s **verdict** — §3's desired 10 nm stroke is unreachable on §3's own stack | **UNTOUCHED, and this challenge strengthens the reason.** Its bound 1 is an identity and its **bound 2 is kinematic**, `L₀ − Nσv₀`, which needs no crossover at all and is short by 1.02× on its own |
| `C-0050`'s bound 3 **as a number** | **not licensed at the window's upper edge.** It should be quoted with the layer model it was read on, exactly as a stiffness is quoted with its compression |
| `C-0050`'s **8.959 nm** headline | it is a *best over the sweep*, read at `σ` = 0.0116 where the two layers are much closer; the headline survives, the sweep's upper end does not |
| `C-0018`'s `φ = 0.2` bias ceiling | **separately disputed** by `C-0036` (a one-parameter family) and `CH-0049` (the 0.2 is a reduced density). This challenge is about the **layer**, not the threshold, and it holds under all three of `C-0036`'s readings |
| the design window | **nothing.** Evaluated on its own layer the crossover clears every grid point of both surviving windows, by 1.71–2.87× depending on the reading |

---

## What would settle it

**Quote every stroke ceiling with the layer model it was read on**, and where a claim sweeps a window drawn on
a *different* layer, evaluate it on that layer. `T-1d` emits `meanVolumeFraction` per design point and
`φ = Nσv₀/h` identically, so both ceilings cost one multiplication on the window's own grid — which is what
`C-0051` does.

**The general form is a discipline this project already has five instances of** — a stiffness with a
compression, a variance with a bandwidth, a rupture force with a loading rate, `k_es` with a gap, a flatness
count with a load case. This is the sixth: **a volume-fraction ceiling with a layer model.**

## What would falsify this challenge

1. **`C-0011`'s solved profile being wrong at high `σ`.** It is the layer every window edge in this programme
   is drawn on, so that would be a much larger finding than this one.
2. **A demonstration that the two models' `φ` should agree at fixed `(L₀, σ)`.** They should not: they solve
   different chain-length inversions, and `C-0016` already records a 1.22× departure at 5 nm from the same
   cause.
3. **`C-0050` having read its window sweep on the solved layer after all.** Its own result file records
   `restingVolumeFraction` of 0.1505–0.2845 at `σ = 0.2885`, which is `C-0003`'s, and `T-1d` records
   0.0623–0.0686 at the same point.
