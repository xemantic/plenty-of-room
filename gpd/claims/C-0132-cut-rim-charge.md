# C-0132 — **The rim's areal charge density is exactly half the face's, and the model has already spent it.** `σ_rim = ρt/4 = σ_face/2 = −0.199332619 e/nm²`, a ratio of **exactly 1/2** at every slab with `t ≤ 2a`, carrying no `ρ`, no `t`, no buffer and no Manning fraction. But `σ_face = ρt/2` is **Gauss's law on a slab, not a convention**, so the tile's charge is already fully assigned and any rim charge must be taken from the **collar's face** — which makes the smearing a **one-parameter conserving family** in the face taper length `ℓ`, with `σ_rim = ρℓ/2` and `ℓ = 0` being `C-0022`'s own headline. `C-0022`'s falsifier 5 is not a member of it: it applies **1.5000** of the tile's charge in 3-D and **1.2500** in 2-D, i.e. it solved a bigger tile, and **the 1.845× bracket is WITHDRAWN rather than narrowed**. What replaces it is a *model* span, read on the convention-free collar because the fitted `(depth, width)` pair **degenerates** under a face taper (the deficit changes sign 3 times against 1, the fitted width reaches **1.43** of the tile half-width and **−12.97 nm** at `ℓ = 3t/4`, and it does not converge while the collar does at second order, ratios 3.85 and 3.84): the conserving family runs **1.222623 to 1.77269012 nm** of collar, **1.44990738×**, and it **straddles** `C-0022`'s **1.65495953 nm** — against a published **1.65495953 → 2.91297923 nm**, **1.76015133×**, that is **one-sided upward**. **Fourteen** downstream validity ranges carry an exposure that is both too large and pointed the wrong way. The saturation cheap bound ran first and **did not settle it** — halving the bare rim charge costs **6.8 %** of its far-field effective charge and quartering it **18.7 %**, but zero is the family's boundary, not a point inside it. The two rims are **different objects and one density**: 0.113875793 duplex end faces per nm² covering **35.8 %** of the across-helix plane against continuous sidewalls along the helices, separated by a charge **depth** of **0.0913621415 nm** — a number only `T-71`'s **measured** 0.9086 nm phosphate radius produces, since the round 1.0 nm makes it exactly zero — and **neither rim is cut**. The ratio transfers to `C-0109`/`C-0120`'s four-layer tile **exactly**; the density and the collar do not

| | |
|---|---|
| **Task** | [`P-14`](../tasks/P-14-cut-rim-charge.md), raised by [`C-0022`](C-0022-tile-edge-load-profile.md)'s *"Still open"* item 2 |
| **Leaf** | `A7.4` (the electrostatic load on the tile) |
| **Verification type** | **logical** (a charge-conservation ledger and a nearest-surface partition, both closed form and both needing no solve) **+ in-silico** (`C-0022`'s own 2-D nonlinear Poisson-Boltzmann edge solve, extended to a laterally shaped face charge and a vertically shaped rim charge; 14 solved states, 6 convergence solves, 4 sampled profiles, 10 plate solves) **+ literature** (the phosphate radius, **measured** in this repository by `T-71`) |
| **Verdict** | **PASS on all six predicates.** One declared falsifier, `F3`, **fired** — the recommended member's fitted depth lies outside `C-0022`'s bracket — and it is recorded rather than repaired, because the reason it fired is that the fitted pair is not a well-posed representation of a conserving smearing, which is itself a deliverable |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED.** And within mean field: `C-0005` puts the one-loop correction at **123–214 %** across this gap range, which is larger than every effect in this claim |
| **Provenance** | `gpd/results/P-14-cut-rim-charge.json`, produced by `electrostatics.CutRimChargeStudyKt`; model in `src/main/kotlin/electrostatics/CutRimCharge.kt` (**new file**); `EdgeChargeShape` and two optional arguments added to `PoissonBoltzmannEdge.solve`; **20 tests** in `src/test/kotlin/electrostatics/CutRimChargeTest.kt` (13) and `CutRimSmearingSolveTest.kt` (7). Re-run through `tools/study.sh` and diffed **byte-for-byte identical**. **`C-0022`'s own study was re-run on the extended solver and its result file is byte-for-byte identical too** — `gpd/results/T-3b-tile-edge-load-profile.json`, `tools/study.sh electrostatics.TileEdgeLoadProfileStudyKt`, *"no result file changed"* — so the two optional arguments are provably inert on every standing number. `tools/verify.sh` **BUILD SUCCESSFUL in 21 m 37 s** on its own isolated tree; `tools/check-kotlin-format-strings.py`, `tools/check-result-file-hygiene.py --conversions`, `tools/check-markdown-tables.py` and `tools/check-challenge-index.py` clean |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`, `l_B = 0.714106611 nm`, `ε_r = 78`; aqueous **MgCl₂**, a **2:1** electrolyte with `I = 3c`, at 0.5 / 2 / 10 mM; the 40 × 40 × 10 nm Manning-renormalised tile at `σ_face = −0.398665238 e/nm²`, `ρ = −0.0797330476 e/nm³`; the tile **negative** and the electrode **positive**; `C-0012`'s located operating bias, 0.192 V at the 10 nm design point |
| **Consumes** | [`C-0022`](C-0022-tile-edge-load-profile.md) (the solver, the geometry, the operating states, and its two published rim readings **reproduced before anything is quoted against them**), [`C-0008`](C-0008-electrostatic-force-and-decay-length.md) (the face charge, the ion model, the 2:1 saturation constants), [`C-0006`](C-0006-tile-load-distribution-and-flatness.md) (the plate, **consumed read-only**), [`C-0005`](C-0005-mean-field-screening-validity.md) (the validity range), [`C-0071`](C-0071-output-element-recommendation.md)-era `T-71` (`MeasuredBackbone`, the measured phosphate radius), [`C-0109`](C-0109-four-layer-tile.md)/[`C-0120`](C-0120-cross-section-comparison.md) (the four-layer body, for the transfer), [`C-0086`](C-0086-seamless-scaffold-routing.md) (the row-end scaffold crossover, for what the across-helix rim actually is) |
| **Raises** | [`CH-0156`](../challenges/CH-0156-the-rim-charge-bracket-is-a-bigger-tile.md) and [`CH-0157`](../challenges/CH-0157-the-rim-sweep-varied-the-smaller-of-two-terms.md), both against `C-0022` |

---

## The claim, in one line

**The rim's charge is not an unsourced parameter — it is `σ_face/2`, fixed by geometry — and knowing it does not close `C-0022`'s bracket, because the bracket was never a function of the rim charge alone: any rim charge is charge the collar's faces gave up, and moving it there moves it across a 90° corner onto a wall that exerts no vertical force.**

---

## 1. The cheap bounds, both run before any field solve

### 1.1 Saturation — and the first time in this programme it did not settle a charge ambiguity

`CLAUDE.md`: *a charge-saturated surface makes its own charge ambiguity irrelevant*, and `C-0125` closed a whole task on it.
It was run first here, on the 2:1 branch (`asymmetricReducedSurfacePotential` → `asymmetricFarFieldAmplitude`), at 2 mM:

| candidate | `σ` [e/nm²] | `y₀` | `A` | `A/A_sat` | `A/A_face` |
|---|---|---|---|---|---|
| uncharged rim (`C-0022`'s headline) | 0 | 0 | 0 | 0 | 0 |
| one eighth of the face density | −0.04983 | −1.2283 | −0.94849 | 0.5900 | 0.6334 |
| one quarter | −0.09967 | −1.8405 | −1.21722 | 0.7571 | 0.8129 |
| **the geometric density, `σ_face/2`** | **−0.19933** | **−2.5082** | **−1.39561** | **0.8681** | **0.9320** |
| three quarters | −0.29900 | −2.9085 | −1.46252 | 0.9097 | 0.9767 |
| the face density (`C-0022`'s falsifier 5) | −0.39867 | −3.1943 | −1.49741 | 0.9314 | 1.0000 |

`A_sat = 12 − 6√3 = 1.6077`.
**Halving the bare rim charge costs 6.8 % of its far-field effective charge and quartering it 18.7 %** — so the choice *among nonzero readings* is nearly free.
**But zero is the family's boundary, not a point inside it**, and saturation says nothing about it.
Saturation flattens a surface's response to its own magnitude; the question here was whether the surface exists.

### 1.2 The charge ledger — one division, and it removes an endpoint

`σ_face = ρt/2` is **Gauss's law on a slab**: a uniformly charged slab of thickness `t` has *exactly* the exterior field of two sheets of `ρt/2`.
So a smearing is a **partition of one conserved charge onto one boundary**, and the §3 tile's rim area (`4 × 40 × 10` = 1600 nm²) is exactly half its face area (`2 × 1600` = 3200 nm²).

| reading | `σ_rim/σ_face` | boundary charge, 2-D | boundary charge, 3-D | conserving |
|---|---|---|---|---|
| `C-0022` headline — uniform face, uncharged rim | 0 | 1.0000 | 1.0000 | **yes** |
| uniform face, rim at the geometric density | 0.5 | 1.1250 | 1.2500 | no |
| **`C-0022` falsifier 5 — uniform face, rim at the face density** | **1.0** | **1.2500** | **1.5000** | **no** |

**`C-0022`'s falsifier 5 did not sweep an unknown parameter; it solved a tile carrying half again as much charge.**
That is [`CH-0156`](../challenges/CH-0156-the-rim-charge-bracket-is-a-bigger-tile.md), and it costs one division.

---

## 2. The geometry: exactly one half, at every aspect ratio

Charge the rim takes has to be charge the faces gave up, and the faces are exact in the interior, so it comes from the **collar**.
Writing the face as a linear taper of length `ℓ` inward from the rim,

> `σ_face(s) = (ρt/2)·min(1, s/ℓ)`,  `σ_rim = ρℓ/2`,

conserves **identically at every `ℓ`**, because the face deficit `ρtℓ/2` per unit edge is exactly the rim gain `t·ρℓ/2`.
It is a **one-parameter family**, and `ℓ = 0` **is** `C-0022`'s headline.
`C-0022`'s falsifier *density* appears at `ℓ = t` — with a **10 nm face taper it did not apply**, and that missing taper is exactly the 25 % of charge it added.

The **nearest-surface (medial-axis) partition** — every element of charge assigned to the boundary element it is closest to — selects `ℓ = t/2` and gives

> **`σ_rim = ρt/4 = σ_face/2 = −0.199332619 e/nm²`**, with the rim **triangular in height**, `ρ·min(ζ, t−ζ)`, peaking at the full face density at mid-height and vanishing at both corners.

The **ratio is exactly one half for any rectangular slab with `t ≤ 2a`**, independently of `ρ`, `t`, the buffer and the Manning fraction — the four quantities a reader would expect it to carry.
All seven conserving members reproduce a boundary charge ratio of exactly 1.000000 on both cuts, and the solver's own **assembly** applies the tile's own charge to `6.6e−5 / 3.3e−12 / 6.9e−9` over the nested 1/2/4 refinement of the recommended member.

---

## 3. And the model has already spent it

The real object's face charge **does not taper**: the column of material behind every face element is the full thickness right up to the rim.
A taper is therefore a bookkeeping artefact with no counterpart in the sheet — and it moves charge across a 90° corner onto a wall that, by the structure of the stress tensor, exerts **no vertical force** (`CLAUDE.md`'s own *"an uncharged rim contributes exactly zero"*).

> **`ℓ = 0` is the only member that keeps the charge where the object puts it, so `σ_rim = 0` in this model is *forced* by `σ_face = ρt/2` — not a statement that the rim is uncharged.
> `C-0022`'s headline is the self-consistent reading of `C-0022`'s own model, and it survives on a ground it was not published on.**

The rim's charge is real and is `σ_face/2`; the impermeable, boundary-smeared representation simply has nowhere to put it that is not already occupied.

---

## 4. What replaces the 1.845×

### 4.1 The fitted pair cannot carry a conserving smearing

`edgeTaperedPressure`'s `(depth, width)` pair is a two-parameter fit to a **one-signed** collar, matched on the first two moments of the load deficit.
A conserving smearing's collar is not one-signed — the face taper lowers the load where fringing raises it.

| member | deficit sign changes outside the standoff | fitted depth | fitted width [nm] | width / half-width |
|---|---|---|---|---|
| `C-0022`'s uncharged rim | 1 | −0.290579117 | 9.0417 | 0.45 |
| `C-0022`'s falsifier 5 | 1 | −0.157533781 | 9.5685 | 0.48 |
| conserving, `ℓ = t/4` | 3 | −0.086413 | 14.7545 | 0.74 |
| conserving, `ℓ = t/2`, uniform rim | 3 | −0.013780 | 31.7338 | 1.59 |
| conserving, `ℓ = 3t/4` | 3 | −0.037562 | **−12.9665** | — |
| **medial, `ℓ = t/2`** | **3** | **−0.022006** | **28.5909** | **1.43** |

And it does not converge, while the same solves' collar does:

| refinement | uncharged collar [nm] | medial collar [nm] | medial fitted depth | medial fitted width [nm] |
|---|---|---|---|---|
| 1 | 1.6757 | 1.2488 | −0.029424 | 24.24 |
| 2 | 1.6550 | 1.2226 | −0.022006 | 28.59 |
| 4 | 1.6495 | 1.2158 | −0.016833 | 32.10 |
| **ratio** | **3.84** | **3.85** | — | — |

Both collars settle at second order; the fitted depth walks monotonically away and the width grows without settling.
**It is not that the conserving solve is unconverged — it is that the fit is not a representation of it.**

### 4.2 The bracket, read on the collar

| reading | equivalent collar [nm] | total force gain |
|---|---|---|
| conserving family, `ℓ = t/4` (**high**) | **1.77269012** | 0.16328 |
| conserving, `ℓ = t/2`, uniform rim | 1.6607 | 0.15576 |
| **`C-0022`'s headline, `ℓ = 0`** | **1.65495953** | **0.147835901** |
| medial, `ℓ = t/2` (**low**) | **1.222623** | **0.108889265** |
| `C-0022`'s falsifier 5 (**excluded**) | 2.91297923 | 0.280575636 |

> **The conserving span is 1.222623–1.77269012 nm, `1.44990738×`, and it CONTAINS `C-0022`'s headline — `+7.1 %` above and `−26.1 %` below.
> The published bracket is 1.65495953–2.91297923 nm, `1.76015133×`, and it is ONE-SIDED upward, `+76.0 %`.**

On the total force gain the same reading is 0.108889265–0.147835901 (`1.49954373×`) against a published 0.147835901–0.280575636 (`1.89788565×`).
On `C-0022`'s own fitted depth the published bracket is `1.84455115×`.

### 4.3 Which term the sweep varied

| step | collar [nm] | move |
|---|---|---|
| `C-0022`'s uncharged rim | 1.65495953 | — |
| **+ the geometric rim charge, face untouched** (what `C-0022` would have done with the right density) | 2.7065 | **+1.0515** |
| **+ the face deficit it must be taken from** | 1.222623 | **−1.4838** |

**The term `C-0022`'s solver could not express is 1.41× the one it swept, and it runs the other way.**
That is [`CH-0157`](../challenges/CH-0157-the-rim-sweep-varied-the-smaller-of-two-terms.md), and it is why the bracket has to be withdrawn rather than rescaled.

### 4.4 The direction is a property of the state, not of the construction

Substituting the medial member for the uncharged one at a 10 nm gap:

| buffer | uncharged collar [nm] | medial collar [nm] | direction |
|---|---|---|---|
| 0.5 mM | 2.2447 | 2.3183 | **rises** |
| 2 mM | 1.6550 | 1.2226 | falls |
| 10 mM | 0.9005 | **−0.2394** | falls **through zero** — the finite tile behaves as one *smaller* than its footprint |

10 mM is where `C-0022` already reports a genuine taper rather than an enhancement, so the two findings agree about which end of the buffer range is anomalous.
**No single sign can be quoted for this correction** — the eighth *"quote it with the state it is read at"* this programme has recorded.

### 4.5 And a rim charge is not one number at all

At the **identical** mean rim density −0.199332619 e/nm² and the **identical** 5.00 nm face taper, a rim uniform in height and a rim triangular in height give collars of **1.6607** and **1.222623 nm** — **1.36×** from the vertical distribution alone, as large as the whole conserving span.
The reason is mechanical rather than electrostatic: the gap is *below* the tile, so rim charge near the bottom face acts on it and rim charge at mid-height does not.
**Anyone quoting "the rim's areal charge density" has under-specified the boundary condition.**

---

## 5. The two rims, and neither of them is cut

| | across the helices | along the helices |
|---|---|---|
| what it presents | duplex **end faces**, 0.113875793 per nm², covering **35.8 %** of the plane | continuous duplex **sidewalls** |
| nearest phosphate below the plane | **0.0 nm** — the terminal phosphate lies *in* it | **0.0913621415 nm** = `R − r_P` at `T-71`'s measured 0.9086 nm |
| what closes it | a Rothemund scaffold **turns** there (`C-0086`'s row-end crossover) | it is simply the last duplex |
| areal charge density | `ρt/4` | `ρt/4` — **the same** |

They are different objects and they carry the **same** density, because the same `ρ` stands behind both and the partition depends only on the block, not on which way the helices run.
All that separates them is a charge **depth** of 0.0913621415 nm — sub-Debye at every buffer here and inside the **1.00 nm** standoff `C-0022` already discards as mesh-divergent.
**At the round 1.0 nm phosphate radius that depth is exactly zero and the question is invisible; it is `T-71`'s measured value that makes it a number at all.**

And **neither rim is *cut*** in the sense of a severed backbone: there is no missing phosphate behind either, which is what makes an uncharged rim a statement about the *smearing* rather than about the object.

---

## 6. The transfer, and `C-0109`'s note

| body | `t` [nm] | `a` [nm] | `ℓ = t/2` | `σ_rim/σ_face` |
|---|---|---|---|---|
| Gen-1 as `C-0022` solves it, 40 × 40 × 10 nm | 10.0 | 20.0 | 5.000 | **0.5** |
| four-layer honeycomb, `C-0109`'s corrected thickness | 8.589 | 19.04 | 4.295 | **0.5** |
| four-layer honeycomb, `C-0006`'s `layerSpacing` reading | 9.608 | 19.04 | 4.804 | **0.5** |
| single-layer Rothemund sheet, one duplex thick | 2.0 | 19.04 | 1.000 | **0.5** |

`C-0109` records that *"the two [numbers] that would move are `C-0022`'s charge and the stack geometry, and neither is re-derived here."*
**It is discharged for the RATIO — exactly 1/2 at every body — and left open for the SOLVE**: the absolute density is `ρt/4` and that tile has a different `ρ` and a different `t`, and the collar is a solved property of the 40 × 40 × 10 nm body `C-0022` meshed.
The §3 argument transfers whole, because it is an argument about smearing a *slab*, not about *this* slab.

---

## 7. What `C-0022` verdicts move

**None of them.**

- **§4(g)'s rejection of the rigid plate stands**, on `C-0022`'s own reading: 0.2153 of the stroke at the nominal foundation, **2.15×** past `T-5b`'s 0.10.
- **The edge is an enhancement, not a taper**, at the design point, on every conserving member: the collar is positive at 0.5 and 2 mM throughout the family.
- **The lever-versus-sensor split** and every per-load-path force in `C-0022` are unchanged, because the recommended reading is `C-0022`'s own.

What moves is the **exposure**. **Fourteen** claims carry *"an unsourced rim charge worth 1.85×"* in their validity ranges — `C-0026`, `C-0033`, `C-0047`, `C-0058`, `C-0060`, `C-0064`, `C-0068`, `C-0087`, `C-0089`, `C-0093`, `C-0098`, `C-0100`, `C-0103`, `C-0108` (counted with a whitespace-tolerant sweep; a single-space `grep` finds twelve, because two of them carry the phrase across a semantic line break).
That exposure is **not unsourced** (it is `σ_face/2`), is **not 1.85×** (it is 1.45× on the collar), and is **not one-sided upward** (it straddles).
It is also **not transmissible through the fitted pair**, so no downstream flatness claim should be re-run on it: the other end of the family reads 0.0046 of the stroke through `edgeTaperedPressure`, and that number is an artefact — a raised cosine 30.76 nm wide on a 40 nm tile is nearly uniform, and *a uniform load on a uniform foundation dishes exactly zero*.

---

## Validity range

- **NOTHING HERE IS MEASURED. TRL 1–3.**
- **MEAN FIELD**, inherited whole from `C-0005`/`C-0008`: 123–214 % at these gaps, larger than every effect here.
- **The tile is an IMPERMEABLE OBSTACLE with smeared surface charges**, exactly as in `C-0008` and `C-0022`. A real origami sheet has electrolyte in its interstices, and a permeable rim is a different boundary condition that this study does not solve.
- **The partition is a CONVENTION with a criterion, not a measurement.** Nearest-surface is the only conserving member in which every element of charge is assigned to the boundary element it is closest to; it is not derived from a field solve of the volumetric object, and no such solve is performed here.
- **TWO-DIMENSIONAL, hence a STRAIGHT edge.** `C-0022`'s corner bracket travels unchanged, and the rim charge now enters it.
- **The traction within 1 nm of the rim is not resolvable and is not used** — and that standoff is precisely where the rim census's geometry lives.
- **POINT IONS, FREE BUFFER in the gap, a MACROSCOPIC electrode, and a Stern series solved in ONE dimension** — all four inherited from `C-0022` unchanged.
- **The §3 tile geometry is internally inconsistent** (40 × 40 nm with a 10 nm thickness against *"single-layer honeycomb"*), and `DnaOrigamiTile` resolves it toward the thick tile. The **ratio** is independent of that choice; the absolute density is not, and `C-0109`/`C-0120` have since moved the body.

## Numbers that are cited rather than derived

| number | value | why it is cited, and what it moves |
|---|---|---|
| the Manning-renormalised face charge | −0.398665238 e/nm² | **CITED FROM `C-0008` via `C-0022`.** Everything here is a *repartition* of it, so it cannot be read as a second opinion about it. |
| `ε_r` of water at 300 K, the Stern capacitance, the ion model | 78, ~20 µF/cm² | **CITED**, as in `C-0005`/`C-0008`. The collar is a ratio. |
| `C-0012`'s located operating bias | 0.192 V at 10 nm | **CITED FROM `C-0012`** via `C-0017`. |
| `C-0006`'s plate and foundation sweep | — | **CONSUMED READ-ONLY.** |
| the duplex steric radius and the honeycomb pitch | 1.0 nm, 2.6 nm | **CITED** via `DnaOrigamiTile`; they set the census's coverage fraction, not the density. |
| the phosphate radius | **0.9086378584708424 nm** | **MEASURED IN THIS REPOSITORY** (`T-71`, 13 084 crystallographic linkages). It is what makes the two rims distinguishable at all. |

## Cross-checks passed

1. **Gate 1 — dimensional consistency.** `ρ` is recovered from `σ_face` and returned to it (`ρt/2 = σ_face`) and is asserted against an independent phosphate count over the bounding-box volume at `1e−6`; the rim density scales linearly and separately in `ρ` and in `ℓ`; a taper longer than the half-width, a negative taper and a zero thickness are all refused.
2. **Gate 2 — limiting cases.** `ℓ = 0` reproduces the uniform face and the uncharged rim; `ℓ = t/2` gives `σ_rim/σ_face` = 0.5 at **twelve** `(ρ, t)` combinations; `ℓ = t` reaches the falsifier's density *and* a face charge of exactly zero at the rim; the medial rim peaks at the full face density at mid-height and is exactly zero at both corners; a zero rim shape reproduces the uncharged solve to `1e−8` in the fitted depth; a **constant unit shape** reproduces the unshaped solve, and a `null` shape leaves `tileChargePerLength` and `rimLineForce` **bit-identical** (`x * 1.0` is exact in IEEE) — confirmed at the whole-study scale by re-running `T-3b` and getting the identical file back.
3. **Gate 3 — conservation.** Every conserving member's boundary charge ratio is 1 to `1e−12` on **both** cuts, algebraically; the face deficit equals the rim gain identically; and the solver's own **assembly** — a different statement, about the discretisation — applies the tile's own charge to `1e−4` at the coarsest mesh, falling with refinement. `C-0022`'s falsifier is measured at exactly 1.25 on the 2-D half-tile.
4. **Gate 4 — numerical convergence.** **Nested** 1/2/4, never 1/2/3/4: both collars converge at second order (ratios 3.84 and 3.85) and the assembly's charge residue falls `6.6e−5 → 3.3e−12 → 6.9e−9`. **The fitted depth and width do not converge, and that is reported as a result rather than refined away.**
5. **Gate 5 — literature and licence.** `C-0022`'s **−0.290579117** and **−0.157533781** are reproduced at **1.3e−9** and **3.0e−9** *before* anything is quoted against them, and its headline depth **−0.302887367** and force gain **0.147080774** at **1.5e−9** and **9.8e−10**. The phosphate radius is this repository's own measurement; the 2:1 saturation constants are `C-0008`'s derived `12 − 6√3` and 6.

## The declared falsifier that fired

**`F3` — "the recommended member's depth falls outside `C-0022`'s published bracket" — FIRED.**
−0.022006 lies outside `[−0.157534, −0.290579]`.
It is recorded rather than repaired: the reason it fired is §4.1, and the response is to re-quote the bracket on the collar and the force gain, which are integrals of the solved profile and owe the fit nothing.

`F1`, `F4`, `F5` and `F6` did not fire.
`F2` reads `no` at the assembly's own quadrature tolerance and the algebra it is a discretisation of is exact at `1e−12`.

## Still open — named, not answered

1. **What the exterior field of the *permeable* body actually is.** The whole ambiguity is an artefact of representing a permeable lattice as an impermeable body with boundary charge. A near-field ceiling is available in closed form — `ρλ_D` = **−0.6262 / −0.3131 / −0.1400 e/nm²** at 0.5 / 2 / 10 mM, i.e. **1.57 / 0.79 / 0.35** of the face density — and it does **not** conserve, because it is a different body. **The one calculation that would close the question is a solve of the permeable, volumetrically charged sheet**, which no claim in this programme has. It is a modelling change, not a measurement.
2. **The corner**, which `C-0022` brackets and nobody has solved. The rim charge now enters it.
3. **The four-layer tile's own edge solve.** The ratio transfers; the collar does not.
4. **Where the scaffold remainder rests.** `C-0125` bounds its effect on the *face* charge and notes the collar width carries no surface charge at all; a coil against a *rim* is unevaluated.

## Challenges

[`CH-0156`](../challenges/CH-0156-the-rim-charge-bracket-is-a-bigger-tile.md) and [`CH-0157`](../challenges/CH-0157-the-rim-sweep-varied-the-smaller-of-two-terms.md) are raised **by** this claim against `C-0022`.
A further result contradicting this claim should be raised in `gpd/challenges/` with methodological grounds rather than overwriting it.
