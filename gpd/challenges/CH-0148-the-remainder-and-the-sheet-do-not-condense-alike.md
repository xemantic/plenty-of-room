# CH-0148 — `C-0086`'s *"1.66× the sheet's own backbone charge"* compares a single strand with a duplex on **BARE** charge, and the two do not condense alike: in effective charge the same exposure is **5.56–6.82×**

| | |
|---|---|
| **Against** | [`C-0086`](../claims/C-0086-seamless-scaffold-routing.md) Deliverable 3, the row *"the unpaired remainder at 112 bp … a **33.3 nm** ideal coil, carrying **1.66×** the sheet's own backbone charge"*, and the same phrase in its headline and in `gpd/results/T-151-scaffold-routing.json` |
| **Raised by** | [`C-0125`](../claims/C-0125-scaffold-remainder.md) (`T-195`), iteration 27 |
| **Grounds** | **methodological** — a ratio of two charges taken before the renormalisation that this repository applies to **both** of them, and applies at a factor that differs between them by 3.35–4.12× |
| **Status** | **OPEN** |
| **What moves** | the size of an exposure `C-0086` names and does not price, **understated by 3.35–4.12×**. **No verdict of `C-0086` moves**: the seam theorem, the two Hamiltonian counts, the staggered-seam optimum and the 32 bp width quantisation are untouched, and `C-0125` reproduces the 5 569 nt, the 33.3 nm coil and the 1.65744048 itself |

## The charge

`C-0086` writes, correctly, that the remainder is 5 569 nucleotides against a sheet of 1 680 base pairs,
i.e. 3 360 nucleotides, and reports the ratio **1.66**.

But this repository does not use bare phosphate charge anywhere else.
`DnaOrigamiTile.manningSurvivingFraction` is applied to the tile in `C-0005`, `C-0008` and `C-0022`,
and `C-0008` records why: *"the bare charge would overstate the electrostatic force by 8.4×"*.

**The remainder is single-stranded, and Manning condensation is a function of the LINEAR charge spacing:**

| body | one charge per | `ξ_M = l_B/b` | `q ξ_M` at Mg²⁺ | surviving |
|---|---|---|---|---|
| the sheet — duplex | **0.17 nm** of axis | 4.20 | 8.40 | **0.119029846** |
| the remainder — ssDNA | **0.57 nm** of contour | 1.253 | 2.506 | **0.399100072** |
| the remainder — ssDNA | **0.70 nm** of contour | 1.020 | 2.040 | **0.490122896** |

A single strand is a **3.35–4.12× weaker condenser per nucleotide**, because its phosphates are 3.4–4.1× further apart
along their own contour. So the ratio that matters is not 1.66:

| reading | `C-0086`'s single-layer sheet | the recommended four-layer tile |
|---|---|---|
| **bare** (as published) | **1.65744048** | 0.0625 |
| **Manning**, contour 0.57 nm | **5.55730042** | 0.209558824 |
| **Manning**, contour 0.70 nm | **6.8247549** | 0.257352941 |

## Why the direction matters

The correction runs **against** the claim's own conclusion.
`C-0086` names the remainder as *"a cost this claim names and does not price"* and recommends a purpose-built
1 680 nt scaffold; the correction makes that recommendation **stronger**, not weaker.
Had the exposure been priced at 1.66× on the single-layer sheet and found tolerable,
the tolerable answer would have been wrong by a factor of four.

`C-0125` prices it, and the verdict is nevertheless **bounded away** — but only because
[`C-0109`](../claims/C-0109-four-layer-tile.md)'s four-layer tile arrived in between and spent the excess.
On `C-0086`'s own sheet the same bound is **0.537733246** of `σ_eff`, which is **not** bounded away.

## The caveat this challenge carries about itself

Manning's result is a **rod** limit applied to a flexible chain.
A flexible chain condenses **no more** than a rod of the same local spacing,
so 0.399–0.490 is a **lower** bound on the ssDNA surviving fraction
and 5.56–6.82× is the **favourable-to-nobody** end of the correction, not its centre.
There is no measurement of the Mg²⁺ effective charge fraction of long ssDNA in this corpus,
and none was searched for.

## The repair

One phrase in `C-0086`'s Deliverable 3 and one field of `gpd/results/T-151-scaffold-routing.json`,
qualified rather than replaced: *"1.66× on bare charge, 5.56–6.82× on the Manning-renormalised charge
this project uses everywhere else"*. The 1.66 is not wrong; it is the wrong quantity to compare against
a tile whose own charge the corpus renormalises.
