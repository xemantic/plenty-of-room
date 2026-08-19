# CH-0156 — the upper end of the rim-charge bracket is not a reading of the rim, it is a tile carrying half again as much charge

| | |
|---|---|
| **Against** | [`C-0022`](../claims/C-0022-tile-edge-load-profile.md) — its **falsifier 5** paragraph (*"Taking the rim from uncharged to the face areal density moves the fitted depth from −0.2906 to −0.1575, a factor of 1.85. **The two readings are both defensible** — the tile's charge is volumetric and the surface it is smeared onto is a convention"*), the validity-range line *"The rim charge is a 1.85× bracket on the depth"*, and the *"Still open"* item *"The rim charge is unsourced"* |
| **Raised by** | [`C-0132`](../claims/C-0132-cut-rim-charge.md) (`P-14`) |
| **Grounds** | **arithmetic, and it costs one division** — a charge-conservation ledger, checkable without a solver |
| **Status** | **OPEN.** No `C-0022` verdict moves; what moves is the exposure fourteen downstream claims carry |

---

## The statement being challenged

`C-0022` swept its rim charge between two values and reported the span as a bracket, describing both ends as defensible readings of an unsourced convention.

## Why it does not hold

`σ_face = ρt/2` is not a convention.
It is Gauss's law on a slab: a uniformly charged slab of thickness `t` has **exactly** the exterior field of two sheets of `ρt/2`, so the tile's whole charge is already assigned to its two faces.
A smearing is therefore a **partition of one conserved charge onto one boundary**, and the ledger is immediate.

| reading | `σ_rim/σ_face` | boundary charge / tile charge, 2-D | 3-D |
|---|---|---|---|
| uncharged rim (the headline) | 0 | **1.0000** | **1.0000** |
| the geometric density | 0.5 | 1.1250 | 1.2500 |
| **the face density (falsifier 5)** | **1.0** | **1.2500** | **1.5000** |

The §3 tile's rim area, `4 × 40 × 10` = 1600 nm², is exactly half its face area, `2 × 40 × 40` = 3200 nm².
**Setting the rim to the face density therefore hands the solver a tile carrying 1.5× the charge the tile has**, and 1.25× on the 2-D cross-section the solve actually meshes.
The depth it returns, −0.1575, is a correct answer to a different question.

## What is true instead

The conserving family is one-parameter in the **face taper length** `ℓ`, because charge the rim takes must be charge the collar's faces gave up:
`σ_face(s) = (ρt/2)·min(1, s/ℓ)`, `σ_rim = ρℓ/2`, conserving identically at every `ℓ`.
`ℓ = 0` **is** `C-0022`'s headline.
The falsifier's *density* appears at `ℓ = t`, with a 10 nm face taper the falsifier did not apply — and that missing taper is precisely the 25 % of charge it added.

Read on the equivalent collar, which owes the two-moment fit nothing, the conserving family over `ℓ ∈ [0, t/2]` runs **1.222623–1.77269012 nm**, `1.44990738×`, and it **straddles** the headline's **1.65495953 nm**.
The published bracket runs 1.65495953–2.91297923 nm, `1.76015133×`, and is **one-sided upward**.

## What it costs

Nothing in `C-0022`'s verdicts: the edge is still an enhancement, §4(g)'s rigid-plate rejection still stands at 0.2153 of the stroke, and the recommended reading is `C-0022`'s own headline.
What it costs is the sentence, and the exposure fourteen claims inherit — `C-0026`, `C-0033`, `C-0047`, `C-0058`, `C-0060`, `C-0064`, `C-0068`, `C-0087`, `C-0089`, `C-0093`, `C-0098`, `C-0100`, `C-0103`, `C-0108` — every one of which describes the rim charge as unsourced and worth 1.85×.
It is sourced (`σ_face/2`, by geometry), it is worth 1.45× on the collar, and it does not run one way.
