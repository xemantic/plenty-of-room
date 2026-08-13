# CH-0013 — An entropic tether's transverse stiffness is not "essentially nothing", and the limit it vanishes in is a design choice

| | |
|---|---|
| **Against** | [`C-0010`](../claims/C-0010-tile-positional-variance.md) — *"The lateral mode: not bounded by the layer, and not given a number it has not earned"*, the reachability bracket |
| **Raised by** | [`C-0014`](../claims/C-0014-lateral-confinement.md) (`T-12`) |
| **Date** | 2026-08-13 |
| **Grounds** | methodological — a limit taken at the wrong point, and one term of a two-term law read as the whole of it |
| **Direction** | **favourable to `C-0010`'s programme.** The requirement is easier to meet than `C-0010` reported, not harder |
| **Status** | raised. **No verdict, table or number in `C-0010` moves.** Its central finding — the layer supplies exactly zero, so an anchoring scheme is required at `k ≥ 0.4602 pN/nm` — is confirmed, and `T-12` exists because of it |

---

## What is challenged

`C-0010` closes its lateral section with a reachability bracket, quoted in full because the challenge is to one sentence of it:

> Whether that is reachable is stated as a **bracket, not as a design**:
> a clamped 10 nm duplex strut gives `3EI/L³ = 0.69 pN/nm` and clears it by 1.5×;
> a 20 nm one gives 0.086 pN/nm and misses by 5.4×;
> **a flexible single-stranded tether gives essentially nothing at zero tension,
> because a chain's transverse stiffness is `F/L` and vanishes with the tension.**
> **Short and stiff, or not at all.** No lateral stiffness is asserted here.

The same statement appears in the result file (`lateral.verdict`) and in `CLAUDE.md`'s *Fluctuation budgets* section by implication.

**The two duplex-strut numbers are reproduced exactly by `T-12` from `EI` and `L` and are not in dispute.**
What is challenged is the third line and the conclusion drawn from it.

---

## The methodological ground

`F/L` is the transverse stiffness of a taut chain, and it is correct. The error is **where it was evaluated.**

A tether from the substrate to the tile is not at zero tension and cannot be: **the geometry stretches it.**
The tile sits at the layer height `h`, the tether's far end is on the substrate, so the tether spans `h` whether it wants to or not,
and the tension it carries is whatever its own force-extension law returns at that extension.
Evaluating `F/L` "at zero tension" evaluates it at an extension the tether does not have.

Carried out at the extension the §3 geometry imposes, the two descriptions turn out to be **the same number**:

- an ideal chain of contour `L_c` and Kuhn length `b` is a **linear, isotropic** spring of constant `k = 3k_BT/(L_c b)` in *every* Cartesian direction, because a Gaussian chain's components are independent;
- its tension at extension `h` is therefore `f = k h`;
- so `f/h = k` — **the "F/L" transverse stiffness and the entropic spring constant are the same quantity**, and neither is zero.

The transverse stiffness vanishes only in the limit `L_c b ≫ h²`, where the chain is slack.
**That limit is a design choice — the length of the spacer — not a property of single-stranded DNA.**

---

## The number

At the §3 10 nm layer, for a tether whose natural coil size matches the gap (`L_c b = h²`):

&nbsp;&nbsp;&nbsp;&nbsp;**`k = 3k_BT/h² = 3 × 4.142/100 = 0.1243 pN/nm` — 27 % of the entire `A1.1` requirement, from one tether.**

Across every case `T-12` evaluates — 4 or 8 tethers, 5/7/10 nm layers, the whole measured Kuhn bracket —
the per-tether lateral stiffness runs **0.115–0.266 pN/nm**, i.e. **25–58 % of the whole requirement each**.

The design rule that replaces "short and stiff, or not at all" is a **contour-length ceiling**:

&nbsp;&nbsp;&nbsp;&nbsp;**`L_c b ≤ 3 N k_BT/k_req`**, i.e. `L_c ≤ 52.6 nm` (**81 nt**) for four tethers and `≤ 103.4 nm` (**159 nt**) for eight, at `b = 2.10 nm`.

**An 81-nucleotide spacer is an ordinary staple extension.** It is not short, it is not stiff, and it meets leaf `A1.1`'s bound.

### And the correction runs the *right* way on the cost, too

`C-0010`'s "short and stiff" instruction points at exactly the element `T-12` finds unusable.
A 10 nm duplex strut does deliver 0.69 pN/nm laterally — and its **axial** stiffness is `S/L = 110 pN/nm`,
twice the whole layer's tangent stiffness under the tile, so it takes **87 % of the stroke**;
and it carries the actuation load in compression, buckling at 5.7–22.7 pN against the 25–100 pN it would have to bear,
at which point its lateral stiffness is exactly zero.

The entropic tether is the **equality case** of `C-0014`'s anisotropy theorem (`k_lat = k_norm` for a linear spring),
so it costs the theoretical minimum: **1.4–8.5 % of the stroke** across the whole evaluated set.

**The line `C-0010` dismissed is the cheapest through-layer scheme available, and the one it recommended is the most expensive.**

---

## What this does *not* challenge

- **The exact zero.** The layer's lateral restoring stiffness is zero by symmetry. That is `C-0010`'s finding, it is why `T-12` exists, and nothing here touches it.
- **The 62.8 nm diffusive excursion** and the drag it comes from. Consumed unchanged.
- **The requirement.** `k_lat ≥ 0.460216 pN/nm` is re-derived from `k_BT` in `T-12` and agrees to the last digit.
- **The duplex-strut bracket.** 0.69 and 0.08625 pN/nm are reproduced from `EI` and `L` as a `T-12` gate-5 test. They are right as *bending* stiffnesses; what `C-0010` did not compute — and explicitly declined to, saying "no lateral stiffness is asserted here" — is what a strut costs in the normal direction.
- **`C-0010`'s verdict, on any quantity.** The tile still passes §6 task 8 at the operating point and the lateral coordinate is still not part of that pass.

`C-0010` said of this bracket that it was *"stated as a bracket, not as a design"*, and that is exactly the right posture — the challenge is that one bracket endpoint was placed at zero when it belongs at a quarter of the requirement.

## The remedy proposed

Replace the sentence in `C-0010`'s lateral section (and the matching `lateral.verdict` string, on the next run of `T-8`) with:

> a flexible single-stranded tether is a **linear entropic spring of `3k_BT/(L_c b)` in every direction**, so it meets the bound provided its contour obeys `L_c b ≤ 3 N k_BT/k_req` — 81 nt for four tethers, 159 nt for eight. It is the *cheapest* option in the normal direction, not the emptiest.

`C-0010` is annotated in place with a banner pointing here rather than edited, per `gpd/README.md`.
