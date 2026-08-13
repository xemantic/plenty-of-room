# CH-0026 — Every electrostatic force in this programme is a 1-D pressure times 1600 nm², and that understates it by 5–19 %

| | |
|---|---|
| **Raised** | 2026-08-13, by [`C-0022`](../claims/C-0022-tile-edge-load-profile.md) (`T-3b`) |
| **Against** | [`C-0008`](../claims/C-0008-electrostatic-force-and-decay-length.md) (`T-3a`) and [`C-0012`](../claims/C-0012-coupled-stroke-and-blocking-force.md) (`T-3`), and through them [`C-0016`](../claims/C-0016-design-window.md), [`C-0017`](../claims/C-0017-output-coupling-stiffness.md) and [`C-0018`](../claims/C-0018-maximum-usable-bias.md) |
| **Grounds** | methodological — a stated one-dimensional limitation, now quantified and found to be non-negligible and **one-signed** |
| **Scope** | a **multiplier on every force**, not a change to any mechanism. No mechanism, decay length, stiffness sign or stability verdict is disputed. |
| **Challenged by** | [`CH-0035`](CH-0035-the-edge-correction-cannot-reach-the-window-edge.md), from [`C-0027`](../claims/C-0027-window-resynthesis.md) (`T-25`), on the two **directional** statements only |

> ⚠️ **The measurement stands and is reproduced; the two consequence lines do not** —
> [`CH-0035`](CH-0035-the-edge-correction-cannot-reach-the-window-edge.md) (2026-08-13).
>
> **(1)** `C-0016`'s upper window edge is the stroke under a **100 pN dead load** — a specified force, with no
> field in it — so this correction is not an argument of it and it **does not move**. Asserted as a test.
> **(2)** At a **force-pinned** operating point `|F_es| = 100 pN + P(g)A` and `k_es = −|F_es|/ℓ` identically,
> so the multiplier is absorbed entirely into the bias and reaches `k_es` **not at all**; only `d ln μ/dh`
> survives, and it **lengthens** the decay. `C-0017`'s 10 nm / 2 mM margin goes from 1.19–1.42× to
> **1.34–1.67×**, not down. The original direction is right for a **fixed-bias** operating point — the free
> tile — and that scope is now stated.

---

## What is challenged

`C-0008` computes a disjoining pressure and multiplies it by the footprint:

> `F_es` [pN] over the 40 × 40 nm footprint

and lists among its validity conditions:

> **1-D.** No edge, no fringing, no lateral structure. The tile is 4–13 gap heights across.

`C-0012` inherits that force wholesale, and `C-0016`, `C-0017` and `C-0018` inherit `C-0012`.

**The tile being 4–13 gap heights across was offered as a reason the edge would not matter. `T-3b` has now measured it, and it does.**

| tile | edge | force above the 1-D × footprint value |
|---|---|---|
| **Gen-1, 40 × 40 nm** | 40 nm | **+14.7 %** (min-margin) to **+16.5 %** (additive) |
| the 70 × 100 nm test tile | 70 nm | +8.9 % to +9.4 % |
| a 20 nm tile | 20 nm | **+25.8 %** to +33.0 % |
| a 100 nm tile | 100 nm | +6.3 % to +6.6 % |

Across the §3 operating box at 0.5 and 2 mM the correction runs **+4.9 % to +19.2 %**, and it is **one-signed**: the 1-D force is a **lower** bound there. The one exception found is the tile *held* at the 3 nm stroke against a 2 nm gap, where the correction reverses to **−3.9 %**.

## Why it is one-signed, and why the direction matters

A finite capacitor's fringing field adds capacitance and therefore adds force. `C-0022` measures the same thing in an electrolyte: the finite tile behaves electrostatically as one **1.65 nm larger on every side** at the design point, 0.44–2.24 nm across the box. That collar is sub-Debye, it scales as `1/L`, and it is why the correction is 25.8 % on a 20 nm tile and 6.3 % on a 100 nm one.

The direction is the **favourable** one for every force clause and the **unfavourable** one for every stability clause — which is exactly the combination in which an error survives longest:

- **§6 task 3's 100 pN clause gets easier.** `C-0012`'s biases for 100 pN blocking fall by roughly the square root of the correction, since `|F_es|` is superlinear in bias below saturation.
- **`C-0016`'s upper window edge — the 3 nm stroke at 100 pN — moves outward**, because more force at the same bias is more stroke.
- **`C-0012`'s `k_eff < 0` problem gets worse**, because `k_es` carries the same multiplier and it is the negative term. So does `C-0017`'s stability floor, and `C-0018`'s usable bias ceiling comes down.

The multiplier is not uniform in gap: at 2 mM it is +4.9 % at a 5 nm layer, +10.6 % at 7 nm and +14.7 % at 10 nm. So it is **larger where the actuator is least stable**, and it is a differential correction rather than a rescaling.

## Why this is a challenge and not an overwrite

No number in `C-0008` or `C-0012` is wrong *as what it says it is*. `C-0022` reproduces `C-0008`'s 1-D disjoining pressure at the tile centre-line to **0.03–0.14 %** at every one of 21 state points, through a solver sharing only the ion model. What is challenged is the step from a pressure at the centre of a tile to a force on the whole of it.

And the correction is **small against the standing uncertainty**. `C-0005` puts the one-loop correction at 123–214 % of the leading term at these gaps, with the direction unpublished for oppositely charged walls. A 15 % edge correction sits an order of magnitude inside that. It is worth recording because it is *systematic and one-signed*, where the mean-field error is neither bounded nor signed.

## What would falsify this challenge

1. **A 3-D corner solve** with a corner contribution of the opposite sign large enough to cancel the straight-edge collar. The two mappings bracket the corner at 1.8 percentage points at 40 nm; cancelling 14.7 % would need it to be an order of magnitude outside that bracket.
2. **The rim charge**, which is unsourced and moves the *depth* by 1.85×. Its effect on the *total* is smaller, but it is not zero.
3. **A finite counter-electrode.** `C-0022` takes the electrode as macroscopic. A counter-pad the size of the tile would have its own edge, and the two edges would not add — this is the single most likely route to a smaller correction.

## What the challenged claims should do

Carry a **multiplier**, not a re-run. `C-0008`'s `F_es(h, V)` table and `C-0012`'s blocking forces and `k_es` are all proportional to the same footprint integral, so the correction is one number per (buffer, gap) and it is emitted in `gpd/results/T-3b-tile-edge-load-profile.json` for exactly that use. Where a verdict has margin above 1.20× — which is most of them — nothing moves. Where it does not, and `C-0017`'s 1.19× margin at 10 nm and 2 mM is the one that does not, the correction should be applied before the margin is quoted again.
