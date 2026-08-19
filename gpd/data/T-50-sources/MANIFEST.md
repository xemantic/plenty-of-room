# `T-50` / `T-221` retained source — Kanduč et al., arXiv:0905.3851

| | |
|---|---|
| **Paper** | M. Kanduč, M. Trulsson, A. Naji, Y. Burak, J. Forsman, R. Podgornik, *Weak and Strong-Coupling Electrostatic Interactions between Asymmetrically Charged Planar Surfaces*, arXiv:0905.3851v1 [cond-mat.soft], 23 May 2009 |
| **Fetched** | 2026-08-19, iteration 32, for [`T-50`](../../tasks/T-50-beyond-mean-field-gap.md); retained in the repository in iteration 34 by [`T-221`](../../tasks/T-221-planar-coupling-wall.md) |
| **Route** | `https://arxiv.org/pdf/0905.3851` with `curl -sL`, then `pdftotext -layout` |
| **Read flag** | **READ DIRECTLY** — every equation quoted in [`C-0137`](../../claims/C-0137-beyond-mean-field-gap.md) and [`C-0143`](../../claims/C-0143-planar-coupling-wall.md) was read from `0905.3851.txt` |

## Why it is retained rather than only summarised

[`gpd/data/T-50-beyond-mean-field-literature.md`](../T-50-beyond-mean-field-literature.md) rows 25–30 quote this paper's Eqs. (64) and (65).
`T-221` had to go further — to Eqs. (3), (5), (6), (7), (14), (18), (61) and (62) — and one of its results, the branch boundary `D̃* = (1+ζ)/|ζ|`, is **derived from Eq. (18)** rather than quoted from anywhere.
A derivation on a source is not checkable without the source.

## The equations `C-0143` uses, with the section they are in

| equation | what it is | where |
|---|---|---|
| Eq. (1), (3) | the two delta-sheet surface charges, and `σ₁ + σ₂ < 0`, `σ₂ > σ₁` — which is what makes `σ₁` the **larger-magnitude** wall | §II |
| §II text | *"the charge of both bounding surfaces is compensated by mobile counterions … distributed in between the two surfaces. We thus neglect all coions."* | §II |
| §I text | `Ξ = q² ℓ_B/µ = 2π q³ ℓ_B² σ/e₀`, and *"if the charge valency of the counterions is q then the aforementioned distance scales as q² ℓ_B"* | §I |
| Eq. (6), (7), (8) | `µ₁ = e₀/(2πℓ_B q\|σ₁\|) ≡ µ`, `Ξ₁ ≡ Ξ`, `D̃ = D/µ`, `ζ = σ₂/σ₁` | §III |
| Eq. (14) | `λ₀` fixed by the **electroneutrality** condition against `σ₁ + σ₂` | §IV |
| Eq. (18) | `tan(2αa) = α(ζ+1)µ/(α²µ² − ζ)`, the repulsive-branch secular equation — the `α → 0` limit of which gives the branch boundary | §IV A |
| Eq. (19), (29) | `p̃₀ = α̃²` (repulsion) and `p̃₀ = −α̃²` (attraction) | §IV A, §IV B |
| Eq. (61), (62) | `f(ζ)`, in its `arctan` and `artanh` forms — each other's analytic continuation across `ζ = −√2/2` | §V B |
| Eq. (63), (64) | `\|p̃₂\| < \|p̃₀\|`, giving `Ξ < D̃/ln D̃` for `p₀ > 0` at `D̃ ≫ 1` | §V C |
| Eq. (65) | `Ξ < (ζ²/\|f(ζ)\|) e^(−2ζD̃)` for `p₀ < 0`, and *"The right hand side here is exponentially large"* | §V C |
| §V C text | *"for p0 = 0 … the leading order term is zero and the fluctuations are dominant at any finite value of Ξ. The convergence of the loop expansion has to be determined in this case by evaluating the higher order terms which we shall not consider in this paper."* | §V C |

## Caveat that travels with every number taken from here

The model is **counterion-only** — §I, *"neglecting completely the effects of salt"* — so it has no Debye length and says nothing about a decay rate directly.
Everything this repository takes from it is a statement about **that** model, transferred to the Gen-1 gap on `C-0008`'s counterion-dominance finding.
