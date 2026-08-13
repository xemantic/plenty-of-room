# CH-0038 — All three of the normal standoff's stiffnesses are quoted with its base grounded at infinity, and no origami base is: at a crossover base 32.0 %, 13.6 % and 32.0 % of them survive, and the Euler load runs to ZERO

| | |
|---|---|
| **Against** | [`C-0025`](../claims/C-0025-flexure-end-joint.md)'s `J5` normal standoff — `k_θ = EI/ℓ`, `k_a = 3EI/ℓ³`, `k_⊥ = S/ℓ`, anisotropy `Sℓ²/(3EI)` — and, through them, its catalogue row (28.75 / 1.348 / 137.5 / 102.0 at 8 nm), its design summary (*"transverse support 137.5 pN/nm … 186×"*), its `ℓ = 7–10 nm` window and its statement that the window is *"closed from both sides by different mechanisms … below, by `C-0023`'s compliance ceiling … above, by `C-0017`'s own 10 nm standoff envelope"* |
| **Raised by** | [`C-0028`](../claims/C-0028-standoff-base-joint.md) (`T-40`) |
| **Date** | 2026-08-13 |
| **Grounds** | methodological — **a member's stiffness quoted without the compliance of what it stands on**, which `C-0025` itself names as its own second open question and then uses unqualified in every downstream number |
| **Direction** | **mixed, and that is the point.** Base compliance is **favourable** for `C-0023`'s 40 pN/nm compliance ceiling — the constraint that closed `C-0025`'s window from below — and **fatal** for buckling stability, which `C-0025` reported as a comfortable reserve. The window survives but is **re-cut by a different pair of constraints**, and its two edges are no longer the two `C-0025` names |
| **Status** | raised. **No number in `C-0025` fails to reproduce** — its `J5-8` span (31.6403748), `c` (95.6390226), tangent (37.3911226), beam tension (3.82799407), its eight window rows and both its buckling loads (8.8672227 / 35.4688908) are all recovered in `C-0028` to ≤ 1.2e−9 |

---

## What is challenged

`C-0025` gives its one passing joint three constants:

> **The joint that works is a duplex standing NORMAL to the sheet** — `S/ℓ` across, `3EI/ℓ³` along, anisotropy `Sℓ²/(3EI)` = 102× at 8 nm

and, in its own validity range, names the assumption underneath them:

> **Whether a duplex standing normal to a single-layer sheet is buildable with a rotationally stiff base is not established here** — the base joint is itself one of `J1`–`J4`, and it is named as an open question.

**Those three expressions are the constants of a cantilever built in at its base**, and nothing in a Rothemund single-layer sheet is a built-in support. Naming the assumption is not the same as carrying it: every downstream number in `C-0025` — the span, `c`, `S_eff`, the tangent, the support margin, the buckling margins, the window bounds — is quoted at the rigid-base value with no bracket on it.

## Why it is wrong

Each of the three is a **series** with the base, not a member property. With `ρ_b = k_θ_base ℓ/EI`:

&nbsp;&nbsp;&nbsp;&nbsp;`k_θ_head = (EI/ℓ)·ρ_b/(ρ_b + 1)`, &nbsp; `k_sway = (3EI/ℓ³)·ρ_b/(ρ_b + 3)`, &nbsp; `k_⊥ = 1/(ℓ/S + 1/k_z_base)`,

and `C-0025`'s three are the `ρ_b → ∞` limit of these — reproduced as such in `C-0028`'s gate-2 tests. **At `C-0009`'s own crossover constant, `ρ_b = 0.176–0.588` over a 3–10 nm standoff**, so the limit is nowhere near.

## The size of it

At `C-0025`'s own 8 nm design point:

| | `C-0025` | one crossover | two, favourable | of `C-0025` |
|---|---|---|---|---|
| `k_θ_head` [pN·nm/rad] | **28.75** | **9.20** | 25.90 | **32.0 %** / 90.1 % |
| `k_sway` [pN/nm] | **1.348** | **0.183** | 1.013 | **13.6 %** / 75.2 % |
| `k_⊥` [pN/nm] | **137.5** | **44.0** | 66.7 | **32.0 %** / 48.5 % |
| support margin | **186×** | 59× | **90×** | |
| `P_c` free-head [pN] | **8.867** | **1.456** | 7.21 | **16.4 %** / 81.3 % |

## And the buckling bracket is not 4× — it runs to zero

`C-0025` quotes the standoff's buckling load *"at BOTH end conditions, `K = 2` and `K = 1`, a factor of exactly 4"*, and reads the binding margin at the conservative `K = 2`. Both are **clamped-base** corners. Solving the column with an elastic spring at each end,

&nbsp;&nbsp;&nbsp;&nbsp;`sin u·(u² − ρ_b ρ_h) − cos u·(ρ_b + ρ_h)·u = 0`, &nbsp; `P_c = u²EI/ℓ²`,

reproduces both of `C-0025`'s numbers exactly and exposes the corner it does not contain:

| `ρ_b` | `ρ_h` | `u` | `K` | `P_c` at 8 nm |
|---|---|---|---|---|
| ∞ | 0 | `π/2` | 2 | 8.867 pN — `C-0025`'s conservative reading |
| ∞ | ∞ | `π` | 1 | 35.469 pN |
| **0** | **0** | **0** | **∞** | **0 exactly — a MECHANISM, not a strut** |

> **A pinned base with a free head carries no axial load at all.** So `C-0025`'s conservative corner is not conservative: the honest bracket has **no lower bound above zero**, and the only question is how fast `P_c` falls. It falls fast — a single-crossover base gives **1.12–4.26 pN** over `C-0025`'s eight lengths against a duty of 3.90–5.91 pN, so **the standoff buckles at every one of them.**

## What it changes

**`C-0025`'s window survives, but neither of the mechanisms it names is the one that closes it.**

| `C-0025`'s reading | with the base modelled |
|---|---|
| closed **below** by `C-0023`'s 40 pN/nm compliance ceiling | the ceiling **stops binding entirely** for any base softer than a clamp — at a single crossover the tangent is 33.80–38.48 pN/nm over the whole 3–10 nm range |
| closed **above** by `C-0017`'s 10 nm envelope | closed above by **buckling**: the `B2` design's margin is 1.065× at 10 nm and falls below one on Fields et al.'s **measured** rigidity |
| buckling a 1.5–3.1× reserve | the **binding** constraint, and the only predicate a covalent base can fail |
| the base is *"one of `J1`–`J4`"* | `J2`/`J2b` is **structurally unavailable** at 90° (a nick preserves the helix axis); `J1` alone **buckles everywhere**. What works is a **two-crossover couple in the favourable orientation** — 261.2 pN·nm/rad — which is not in the list |

## The remedy, and it costs nothing

**Specify the base, and specify its orientation.** Two antiparallel crossovers to the two adjacent sheet duplexes, with the pair laid **across** the flexure axis so that the couple `k_s d²/2 = 234.1 pN·nm/rad` reacts the standoff's base moment. The same two staples laid **along** the flexure give 27.06 pN·nm/rad and pass nowhere — **9.65× for free, and the difference between a design and a mechanism.**

The threshold, inverted: the base needs **23.1 pN·nm/rad at 3 nm rising to 173.6 at 10 nm** — 1.71 to 12.83 crossover equivalents. One crossover meets it at no length; two in the favourable orientation meet it at every one.

## The general lesson

`CLAUDE.md` already records *"the stiffness is rarely the constraint; the compliance is"* and *"an anchor's orientation decides everything and its material almost nothing"*. This is both at once, and it adds a third:

> **A joint's stiffness is never its own member's stiffness; it is that member in series with its ground. Quote every joint constant with the ground it assumes, exactly as this project already quotes a stiffness with a compression, a variance with a bandwidth, a rupture force with a loading rate and `k_es` with a gap.**

The fifth instance of that discipline, and the first in which the omitted qualifier turned a reserve into the binding constraint.

## What would overturn this challenge

1. **A published 90° junction motif with a measured or computed rotational stiffness above ~70 pN·nm/rad.** A primary-source search found no instance of the geometry at all, and every published out-of-plane element on an origami body is held by a **pin** — so the direction of the evidence is the other way.
2. **A demonstration that the standoff head is restrained against sway by something other than the beam.** It would raise `P_c` — but sway *is* the flexure's draw-in, so anything that prevents it restores `C-0023`'s *ends held axially* reading and its 91.13 pN/nm tangent. The two cannot both be had from one element, and that is the structural core of this challenge.
