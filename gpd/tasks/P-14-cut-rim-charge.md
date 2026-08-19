# P-14 — the charge presented by the **cut rim** of a DNA-origami sheet

| | |
|---|---|
| **Leaf** | `A7.4` (the electrostatic load on the tile) |
| **Raised by** | [`C-0022`](../claims/C-0022-tile-edge-load-profile.md), whose declared **falsifier 5 fired**: taking the rim from uncharged to the face areal density moves the fitted edge depth from **−0.290579117** to **−0.157533781**, a **1.845×** bracket, and neither reading is sourced |
| **Reserved IDs** | claim `C-0132`, challenges `CH-0156`, `CH-0157` |
| **Verification type** | **logical** (a charge-conservation identity and a geometric partition, both closed form) **+ in-silico** (the 2-D nonlinear Poisson-Boltzmann edge solve, `C-0022`'s own, re-run at the derived smearings) **+ literature** (cross-check of the phosphate geometry against `T-71`'s own measurement) |
| **Maturity** | TRL 1–3. Nothing here is measured. |

---

## 1. Formulate

### The question

`C-0022` solves the tile as an impermeable obstacle carrying **smeared surface charges** on its bottom face,
its top face and its rim.
The faces are set by `C-0008`'s Manning-renormalised projected charge; **the rim is set by nothing**,
and the two readings `C-0022` swept — `σ_rim = 0` and `σ_rim = σ_face` — differ by 1.845× in the fitted collar depth.

The tile's charge is **volumetric**: 10 718 phosphates in 16 000 nm³.
The surface it is smeared onto is therefore a **construction**, and the question is which construction.

### Numeric target

The rim's signed areal charge density `σ_rim` in `e/nm²`, for the §3 tile at 300 K in `MgCl₂`,
**with the family it belongs to made explicit** — or the statement that the family cannot be
narrowed without a measurement, plus the bracket it leaves and the measurement that would close it.

### Acceptance predicate (falsifiable)

**P1.** `C-0022`'s two rim-charge endpoints are reproduced from its own solver to a relative departure `< 1e-6`
before any comparison against them is quoted.

**P2.** A **charge-conservation** ledger is emitted for every candidate smearing: the total charge the model
assigns to the tile's boundary, against the tile's own Manning-renormalised charge, in 2-D (per unit edge)
and in 3-D (the whole tile). A smearing whose ledger departs from 1 by more than `1e-9` is **named as
non-conserving** and excluded from the recommended family.

**P3.** The rim density is **derived** from a stated geometric partition of the tile's volumetric charge,
in closed form, with its dependence on the tile's aspect ratio given explicitly — not fitted, not cited.

**P4.** The **two rims are distinguished**: the rim along the helices (duplex sidewalls) and the rim across
them (duplex end faces) are each characterised, and it is stated whether they carry the same areal density
and at what resolution the distinction survives.

**P5.** The 1.845× bracket is re-quoted over the recommended family, and the movement of every `C-0022`
number that a reader would carry — collar depth, collar width, total force gain, dishing over stroke —
is reported at the design point.

**P6.** The answer's transfer to the four-layer tile (`C-0109`/`C-0120`) is stated with its number,
and `C-0109`'s standing note that *"`C-0022`'s charge … [is] not re-derived here"* is discharged or left open explicitly.

### Locked units, geometry and sign conventions

- SI; lengths **nm**, charge in **elementary charges `e`**, areal charge in `e/nm²`, volumetric in `e/nm³`,
  pressure in `pN/nm²` = 1 MPa, `k_BT = 4.141947 pN·nm` at 300 K.
- **`x` is lateral, `x = 0` the tile centre-line, the rim at `x = a = 20 nm`.** `z` is normal to the
  electrode, `z = 0` the electrode, the tile occupying `z ∈ [h, h+t]` with `t = 10 nm`. Distance
  **inward from the rim** is `s = a − x`; height **above the tile's bottom face** is `ζ = z − h`.
- **The tile is negative and the electrode positive**, as in `C-0008`/`C-0022`. Every charge density
  quoted here is **signed**; magnitudes are labelled as such.
- `MgCl₂` is **2:1**: `I = 3c`, `κ² = 24π l_B c`. The saturated far-field amplitude is `12 − 6√3` at a
  **negative** wall (`C-0008`).
- The volumetric charge density `ρ` is the tile's Manning-renormalised charge over its **bounding-box**
  volume, `ρ = σ_face · 2/t`, so that the face density the whole programme uses is reproduced identically.

---

## 2. Plan

### The cheap bound, run first

**Saturation.** `CLAUDE.md`: *a charge-saturated surface makes its own charge ambiguity irrelevant*, and
`C-0125` closed a whole task on it. Before any 2-D solve, invert the 2:1 Grahame relation
(`asymmetricReducedSurfacePotential`) at every candidate rim density and read the far-field amplitude
(`asymmetricFarFieldAmplitude`). If the whole candidate family sits in saturation, the choice inside it is
worth nothing and the task closes on arithmetic. **Cost: microseconds.**

**Charge conservation.** The second cheap bound, and it needs no solver at all: a smearing that does not
put the tile's own charge on the tile's own boundary is not a reading of the tile. `σ_rim = σ_face` on a
40 × 40 × 10 nm block adds `A_rim σ_face = 1600 × σ_face` to a boundary that already carries
`2 A_face σ_face = 3200 σ_face` = the whole charge. **Cost: one division.**

### The geometry

A nearest-surface (medial-axis) partition of a uniformly charged rectangular block assigns to each
boundary element the charge of the material closer to it than to any other boundary. For a slab of
thickness `t` and half-width `a ≥ t/2` this is closed form:

- face at distance `s` from the rim: `σ_face(s) = ρ · min(t/2, s)` — the exact slab value `ρt/2` for
  `s ≥ t/2`, tapering **linearly to zero** at the rim;
- rim at height `ζ`: `σ_rim(ζ) = ρ · min(ζ, t − ζ)`, mean `ρ t/4` = **exactly half the face density**,
  independently of `ρ`, `t`, the buffer and the Manning fraction.

Generalised, the conserving family is **one parameter**: a face taper of length `ℓ` and a uniform rim
`σ_rim = ρ ℓ/2`, whose `ℓ = 0` member is `C-0022`'s headline and whose `ℓ = t/2` member is the medial one.
`C-0022`'s falsifier density `ρt/2` appears at `ℓ = t` — **with a face taper it did not apply**, which is
exactly the 25 % (2-D) / 50 % (3-D) charge it added.

### The DNA census

What each rim *presents* is then checked against the lattice: duplex end faces across the helices
(one per `areaPerHelixCrossSection`, terminal phosphates **at** the plane) against duplex sidewalls along
them (phosphates at the **measured** `r_P = 0.9086 nm` of `T-71`, i.e. 0.0914 nm inside the steric surface).
Both depths are sub-Debye and inside the 1 nm standoff `C-0022` already discards, which is the test of
whether one number can serve both rims.

### Method justification against cost

The expensive object is the 2-D Newton solve (~40 000 nodes, seconds each). The plan spends it **only**
after the two cheap bounds have decided what to solve: two licence solves reproducing `C-0022`, and a
five-member sweep of `ℓ`. Nothing else needs a field.

### What would falsify this approach

- **F1** — the licence solves do not reproduce `C-0022`'s `−0.290579117` / `−0.157533781`. Then the harness
  is not `C-0022`'s and no comparison is licensed.
- **F2** — the medial smearing's boundary charge does not equal the uncharged-rim smearing's to `1e-9`.
  Then the partition is not a partition and the whole construction is void.
- **F3** — the solved depth at the recommended member falls **outside** `C-0022`'s bracket. Then this is a
  **move**, not a narrowing, and the claim must say so.
- **F4** — the derived smearing moves the centre-line load by more than the 0.03–0.14 % `C-0022` already
  reports against `T-3a`. Then the face taper has reached the centre-line and the anchor is broken.
- **F5** — the far-field effective charge at `ρt/4` differs from that at `ρt/2` by more than 25 %.
  Then the surface is not saturated and the cheap bound does not apply.
- **F6** — the two rims' charge depths differ by more than the 1 nm standoff. Then one number cannot
  serve both and the tile's two edges must be solved separately.

---

## 3. Status

Executed in iteration 30 — see [`C-0132`](../claims/C-0132-cut-rim-charge.md)
and `gpd/results/P-14-cut-rim-charge.json`.
