# C-0006 — Load distribution across the Gen-1 tile, and whether it stays flat

| | |
|---|---|
| **Tasks** | [`T-5`](../tasks/T-5-load-distribution.md), [`T-5b`](../tasks/T-5b-tile-flatness.md) |
| **Leaves** | `A1.2` (`T-5`), `A8.2` (`T-5b`) |
| **Verification type** | in-silico (analytic plate mechanics + a Rayleigh-Ritz orthotropic plate solve written for this task) |
| **Verdict** | **PASS** on both acceptance predicates. The `T-5b` predicate is discharged with the answer **rejected**, which is a discharge, not a failure. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** |
| **Provenance** | `gpd/results/T-5-load-distribution.json` and `gpd/results/T-5b-tile-flatness.json`, produced by `structure.TileLoadDistributionStudyKt` and `structure.TileFlatnessStudyKt` |
| **Conditions** | T = 300 K, aqueous buffer with Mg²⁺, `k_BT = 4.142 pN·nm`; 40 × 40 nm tile; 100 pN target force (§3) |
| **Raises** | [`CH-0005`](../challenges/CH-0005-rigid-tile-assumption.md) against [`C-0001`](C-0001-layer-stiffness.md) |

---

## The tile, as a plate

Derived, not asserted. Every input carries its provenance in the result JSON.

### Geometry, from measurement

| quantity | value | provenance |
|---|---|---|
| interhelical distance, single-layer sheet | **2.69 ± 0.02 nm** | **CITED, MEASURED** — Fischer et al., *Nano Lett.* **16**:4282 (2016), SAXS, ≥ 10 mM Mg²⁺ |
| interhelical distance, honeycomb | 2.536 ± 0.003 nm | same |
| crossover spacing, **per interface**, Rothemund sheet | **32 bp = 10.88 nm** | **CITED** — Rothemund, *Nature* **440**:297 (2006) |
| crossover spacing, per interface, honeycomb | 21 bp = 7.14 nm | **CITED** — Douglas et al., *Nature* **459**:414 (2009) |
| areal duplex density | **0.372 duplexes/nm** → 14.9 across a 40 nm tile | **DERIVED** |
| crossovers along one 40 nm interface | **3.68** | **DERIVED** |

> The per-interface spacing is 32 bp, not the 16 bp usually quoted: crossovers recur every
> 1.5 turns along a helix but **alternate between its two neighbours**. Using 16 bp would
> double `D_⊥`. This is the single easiest error to make in this derivation.

### The two principal rigidities

&nbsp;&nbsp;&nbsp;&nbsp;`D_∥ = EI / d` &nbsp;(parallel duplex beams) &nbsp;&nbsp;&nbsp;&nbsp;
`D_⊥ = k_θ d / p` &nbsp;(**crossover hinges only**)

| variant | `D_∥` [pN·nm] | `D_⊥` [pN·nm] | `D_k` [pN·nm] | `D_∥/D_⊥` |
|---|---|---|---|---|
| **single-layer 2D sheet (nominal)** | **85.50** | **3.345** | 42.75 | **25.6** |
| single-layer honeycomb rule | 90.69 | 4.805 | 45.35 | 18.9 |
| nominal + inter-crossover duplex twist in series | 85.50 | 2.821 | 42.75 | 30.3 |
| nominal, crossover `α = 1.2` (stiff edge) | 85.50 | 4.014 | 42.75 | 21.3 |
| nominal, crossover `α = 0.6` (soft edge) | 85.50 | 2.007 | 42.75 | 42.6 |
| four-layer honeycomb, rigid coupling | 14 310.78 | ≥ 19.222 | 181.39 | ≤ 744.5 |

**So `D_⊥ = 2.0 – 4.8 pN·nm` and `D_∥ = 85 – 91 pN·nm` for any single-layer reading of §3,
and the sheet is 19–43× stiffer along the helices than across them.**

### The dominant compliance term — leaf `A8.2`'s explicit ask

**Joint compliance, and only joint compliance.** `D_⊥ = k_θ d / p` contains no duplex
elasticity whatsoever: across the helices no duplex bends, and the sheet articulates entirely
at its crossovers. Quantitatively, at the nominal geometry the crossover hinges contribute
`p/k_θ = 0.804` of interface rotational compliance against `1/k_twist = 0.149` from the
inter-crossover duplex twist, in the same units —
**84% of the across-helix compliance sits in the crossovers**, and the duplexes carry the
remaining 16%. Along the helices the situation reverses completely and the compliance is all
duplex bending.

`k_θ = 2αB/(100a) = 13.53 pN·nm/rad` per antiparallel crossover, `α ∈ [0.6, 1.2]` —
**CITED, fitted to measurement**: Chen et al., *J. Am. Chem. Soc.* **136**:6995 (2014), SI §S2.
The same source states why no competitor number exists:

> CanDo treats crossovers as rigid constraints. […] In a multilayer structure, the crossover
> bending degree-of-freedom is largely prohibited. […] CanDo has been highly successful in its
> prediction even though it does not consider the significant bending flexibility of crossovers.

A single-layer sheet is exactly the case in which that prohibition is lifted.

---

## The governing group: `ℓ/L`

`ℓ = (D/k_f)^(1/4)`, against the tile half-width `L/2 = 20 nm`, at the `C-0001` design point
(10 nm layer, `σ = 0.024 nm⁻²`, secant `k_f = 0.012626 pN/nm³`) and across a ×[0.25, 4] sweep:

| `k_f` × | `k_f` [pN/nm³] | stroke [nm] | `ℓ_∥` [nm] | `ℓ_⊥` [nm] | `ℓ_∥/(L/2)` | `ℓ_⊥/(L/2)` | **`ℓ_⊥/p`** |
|---|---|---|---|---|---|---|---|
| 0.25 | 0.00316 | 19.80 | 12.83 | 5.71 | 0.641 | 0.285 | 0.52 |
| 0.50 | 0.00631 | 9.90 | 10.79 | 4.80 | 0.539 | 0.240 | 0.44 |
| **1.00** | **0.01263** | **4.95** | **9.07** | **4.03** | **0.454** | **0.202** | **0.37** |
| 2.00 | 0.02525 | 2.48 | 7.63 | 3.39 | 0.381 | 0.170 | 0.31 |
| 4.00 | 0.05050 | 1.24 | 6.41 | 2.85 | 0.321 | 0.143 | 0.26 |

**`ℓ/L < 1` everywhere, by a factor of 2–7.** The tile cannot bridge its own width on this
foundation in either direction. `ℓ_eff = (√(D_∥D_⊥)/k_f)^(1/4) = 6.05 nm` nominal, so the tile
contains `A/(ℓ_∥ℓ_⊥) = 43.7` mechanically independent patches.

**And `ℓ_⊥/p < 1` across the whole sweep**, which is a validity failure of the model itself:
the across-helix bending length is *shorter than the crossover spacing*, so the continuum plate
reduction is marginal and the tile is closer to **~15 quasi-independent duplex beams sharing a
polymer cushion** than to a plate. Recorded as a limitation, not hidden — see Validity.

---

## `T-5` — per-load-path force

### Distributed attachment: the load never enters the structure

A uniform load on a uniform Winkler foundation makes a free plate **translate exactly**,
whatever its rigidity: `w = q/k_f` has zero fourth derivative and satisfies the free-edge
conditions identically. The transverse shear crossing any cut is therefore **zero**, and the
solve reproduces that to machine precision:

| load path | count | force each [pN] | band |
|---|---|---|---|
| `LP-brush` — one grafted PEG chain | 38.4 | **2.60** | below isomerisation |
| `LP-crossover` — one crossover on the worst cut ∥ helices | 3.68 | **0** | below isomerisation |
| `LP-duplex` — one duplex on the worst cut ⊥ helices | 14.9 | **0** | below isomerisation |

The solver returns `2 × 10⁻¹⁴ pN` and `5 × 10⁻¹⁵ pN`, whose digits are pure roundoff; the result
file floors magnitudes below `10⁻⁹ pN` to zero, which is both more reproducible and more honest
than fifteen digits of noise. Identical at every `k_f` in the sweep, because the result does not
depend on `k_f` at all.

**A purely distributed actuator has no structural-survival problem.** That is the headline of
`T-5`, and it is a statement about the *load path topology*, not about the tile being strong.

### Discrete anchors: bounded by a saturation, not by the total load

A rigid point anchor on a compliant tile can only collect the load from an area of order `ℓ²`
around itself, because the point stiffness of a plate on a Winkler foundation is finite,
`8√(D k_f)`. The isolated-anchor saturation is therefore

&nbsp;&nbsp;&nbsp;&nbsp;**`R_max = 8 q √(D_eff/k_f) = 8 q ℓ_∥ ℓ_⊥`** &nbsp;→&nbsp; **18.3 pN at the nominal `k_f`**,

*independent of how much more tile there is*. Across the sweep it runs 36.6 pN (`k_f` ×0.25)
down to 9.2 pN (×4), because `R_max ∝ k_f^(−1/2)`. The finite-plate solve approaches that
saturation from below as the anchor stiffens, and passes slightly above it — 20.9 pN against
18.3 pN at ten times the layer stiffness — because a finite plate is softer than an infinite one:

| anchors | anchor stiffness = layer stiffness | peak anchor force [pN] | band | stroke lost |
|---|---|---|---|---|
| 1 | | **17.6** | reversible isomerisation | 18% |
| 4 | | 8.9 | below isomerisation | 36% |
| 9 | | 5.2 | below isomerisation | 45% |
| 25 | | 2.0 | below isomerisation | 50% |

**No anchor reaches the 35–60 pN disassembly band anywhere in the sweep** — the worst case
found is 32.6 pN, at `k_f` ×0.25 with anchors ten times stiffer than the whole polymer layer.
The internal transfer is milder still: the anchor force spread over the 9.3 load paths on an
`ℓ`-sized contour around it gives **1.9 pN per crossover or duplex**.

The binding cost of anchors is not strength. It is **stroke**: anchors stiff enough to hold the
tile take 18–50% of the actuation away. That is the stiffness budget `A8.2` asks for, and it
runs the opposite way from the strength budget.

### Concentrated attachment: fatal three times over

| attachments | force each [pN] | band | local dishing [nm] | dishing/stroke |
|---|---|---|---|---|
| **1** | **100.0** | **above disassembly** | **18.3** | **3.69** |
| 4 | 25.0 | reversible isomerisation | 6.96 | 1.41 |
| 9 | 11.1 | reversible isomerisation | 3.17 | 0.64 |
| 16 | 6.25 | below isomerisation | 1.70 | 0.34 |
| 49 | 2.04 | below isomerisation | 0.52 | 0.11 |

A single lever tether fails on three independent counts: 100 pN is above the 35–60 pN
disassembly band, above the ~48–65 pN single-duplex shear allowable, and above the **65 pN
overstretching ceiling** that no nicked duplex can pass at any loading rate; and its local
dishing of 18.3 nm exceeds the 10 nm layer height, so the tile would contact the electrode and
the linear model has left its own domain.

### The minimum number of load paths — the number `T-5` exists to produce

| criterion | source | minimum paths |
|---|---|---|
| below 35 pN (irreversible-disassembly band) | §4(f) | **3** |
| below 10 pN (isomerisation band) | §4(f) | **11** |
| below 48 pN (single-duplex shear, quasi-static) | Strunz et al. (1999) | 3 |
| below 10 pN (single-duplex **unzip**) | Essevaz-Roulet et al. (1997) | 11 |
| below 65 pN (overstretching ceiling, nicked duplex) | van Mameren et al. (2009) | 2 |
| **dishing below 10% of the stroke** | this work | **55** (28–110 over the sweep) |

**The flatness requirement is 5–18× stricter than every strength requirement**, and 55 exceeds
the 43.7 mechanically independent patches the tile contains. There is therefore **no discrete
attachment scheme that is flat**: the output coupling has to be continuous over the tile, or
the tile dishes.

---

## `T-5b` — deflected shape, against the stroke

Peak dishing as a fraction of the stroke, by source, at the nominal `k_f`:

| source | mechanism | peak dishing [nm] | RMS [nm] | / stroke | rigid plate? |
|---|---|---|---|---|---|
| **uniform load** | the leading order | **0.0000** | 0.0000 | **0.000** | **UPHELD, exactly** |
| electrostatic edge taper, 50% over 4 nm | finite tile at a gap ≈ `λ_D` | 1.326 | 0.411 | **0.268** | REJECTED |
| edge taper at 10% | same, shallower | 0.265 | 0.082 | 0.054 | upheld |
| 4 discrete anchors at layer stiffness | §4(g) geometry | **2.480** | 0.954 | **0.501** | REJECTED |
| 1 concentrated lever attachment | §4(g) force transfer | **18.278** | 6.965 | **3.692** | REJECTED |
| **thermal, 300 K, unloaded** | the tile's own modes | RMS **1.272** | — | **0.257** | **REJECTED** |

The edge-taper response is **exactly linear in the taper depth** — 0.2651 nm at 10% against
1.3256 nm at 50%, a ratio of 5.000 — demonstrated rather than asserted, so any depth the `T-3`
electrostatics eventually produces can be read straight off: **dishing ≈ 0.54 × depth × stroke**.

### The cheap bound was wrong at the edge, and the solve caught it

The analytic ripple transfer function `1/(1+(2πℓ/λ)⁴)` predicts that a perturbation confined to
a 4 nm rim (λ ≈ 8 nm) is attenuated to **0.010** across the helices. The numerical solve gives an
effective transmission of **0.53**. The bound is wrong by 50× — and correctly so, because a
**free edge** has no material beyond it to bend against, so an edge perturbation costs far less
curvature than an interior ripple of the same wavelength. The interior transfer function stands;
it simply does not apply at a boundary. This is the one place in the iteration where the
expensive calculation earned its cost against the cheap one.

### Thermal flatness — `A8.2`'s "no floppy modes in the workspace"

Equipartition on the Ritz functional (`k_BT K⁻¹`), unloaded, at `C-0001`'s stiffness at first
contact. Converged in the basis: 1.248 / 1.272 / 1.279 nm at degrees 8 / 12 / 16.

| `k_f` × | piston [nm] | tilt [nm] | **dishing [nm]** | at a point [nm] | dishing/piston | dishing/stroke |
|---|---|---|---|---|---|---|
| 0.25 | 1.496 | 2.116 | 1.690 | 2.238 | 1.13 | 0.085 |
| **1.00** | **0.748** | **1.058** | **1.272** | **1.365** | **1.70** | **0.257** |
| 4.00 | 0.374 | 0.529 | 0.919 | 0.870 | 2.46 | 0.742 |

**The tile's internal bending modes carry more thermal amplitude than its rigid-body piston
mode** — 1.70× at the nominal point, rising as the foundation stiffens, because the dishing
modes are stiffened by `D q⁴` and the piston mode is not stiffened at all. There are floppy
modes in the workspace, and they are the *shape* modes, not the position mode.

### The verdict, and where it flips

| load case | verdict | flips at |
|---|---|---|
| uniform load, distributed reaction | **UPHELD exactly**, at every `k_f` | never — it is `k_f`-independent |
| electrostatic edge taper | **REJECTED** at 50% depth; upheld below ~19% depth | linear in depth |
| discrete anchors | **REJECTED** across the whole sweep | not within ×[0.25, 4] |
| concentrated lever attachment | **REJECTED** by 4–7× | not within ×[0.25, 4] |
| thermal, 300 K | **REJECTED** at ≥ 0.30 × `C-0001`; upheld below | **`k_f` ≈ 0.30 × `C-0001`**, i.e. a layer stiffness of ≈ 6.1 pN/nm secant (2.2 pN/nm at first contact) over the 40 × 40 nm tile |

**Overall: the rigid-plate assumption is rejected.** It survives exactly one load case — the
idealised one — and fails every departure from it, including the unavoidable one (300 K).
And note *why* it survives that case: not because the tile is stiff (it is not; `ℓ/L ≈ 0.2–0.5)`
but because a uniform load needs no stiffness. **`C-0001` got the right answer for the wrong
reason, and the reason does not generalise.**

---

## §4(g) — the two consequences, quantified

### (i) Force transfer to the lever

- A **single** lever attachment is unusable: 100 pN per path against a 65 pN hard ceiling, and
  18.3 nm of local dishing against a 10 nm layer.
- Strength alone needs **≥ 3** attachments (35 pN) or **≥ 11** for a 10 pN margin.
- **Flatness needs ≥ 55**, which exceeds the 43.7 independent patches in the tile. The output
  coupling must therefore be **distributed over essentially the whole tile**; there is no
  concentrated design that both survives and stays flat.
- With fewer than that, the lever samples the tile at *one point* and therefore **over-travels**
  relative to the tile's mean position by the local dishing: 3.17 nm at 9 attachments against a
  4.95 nm stroke. Lever stroke and tile stroke are not the same quantity.

### (ii) What an adjacent charge sensor would see

A charge sensor integrates over the tile with the Debye weighting `e^(−z/λ_D)`, so for a height
distribution of RMS `δ` it reads `⟨e^(−z/λ_D)⟩ = e^(−⟨z⟩/λ_D) e^(δ²/2λ_D²)` —
an **apparent offset toward the electrode of `δ²/(2λ_D)`**:

&nbsp;&nbsp;&nbsp;&nbsp;`δ = 1.272 nm`, `λ_D = 4 nm` → **0.202 nm, 4.1% of the stroke.**
A systematic gain/offset error, not noise: it does not average away.

The larger effect is that **the sensor and the lever do not measure the same displacement at
all.** The sensor reports an area average; the lever samples one point. The two differ by the
dishing, **1.27 nm RMS = 26% of the stroke**. §4(g) asks what to conclude "if the two are
comparable in magnitude": they are within a factor of four, so the rigid-plate picture has to go
for anything that closes a loop between a point-coupled lever and an area-averaging sensor.

### (iii) A third consequence, not asked for but implied

The polymer layer is sampled at a *distribution* of heights rather than one, and `Π(h)` is
convex, so `⟨P⟩ > P(⟨h⟩)`. With `m_eff = 1.672` (`C-0002`) and `δ/h = 0.127`, the mean restoring
force is **+3.6%** higher than a flat-tile model gives. Small, but it runs *opposite* to
`CH-0001`'s correction and it is the mechanism by which `C-0001`'s rigid-tile assumption is not
merely unproven but quantitatively wrong. See [`CH-0005`](../challenges/CH-0005-rigid-tile-assumption.md).

---

## The five verification gates

Executed as tests, named for the gate they discharge:
`src/test/kotlin/structure/` — `LegendreTest`, `CholeskyTest`, `OrigamiSheetTest`,
`PlateOnFoundationTest`, `LoadPathsTest`.

### Gate 1 — dimensional consistency

- `ℓ = (D/k_f)^(1/4)`: `pN·nm` over `pN/nm³` is `nm⁴`, so quadrupling `D` lengthens `ℓ` by
  exactly `√2` — asserted, not assumed.
- `D_∥ = EI/d`, `D_⊥ = k_θ d/p`, `D_k = GJ/(4d)` each asserted against their closed forms.
- `EI = L_p k_BT` asserted once, in the only place the two are related.
- The Hertz point-load deflection reduces to `P/(8√(D k_f))`.

### Gate 2 — limiting cases

- **A uniform load produces no dishing at all, at rigidities spanning 10⁹** — the strongest
  falsifier available, and the one that makes the whole task tractable. Reproduced to `< 1e−9 nm`.
- An infinitely rigid plate translates under a *point* load, to `P/(k_f A)`.
- Ripple transmission → 1 at long wavelength, → 0 at short, exactly ½ at `λ = 2πℓ`, and
  monotone in between: a plate is a low-pass filter, never a band-pass one.
- A stiffer crossover stiffens only `D_⊥` and leaves `D_∥` untouched; sparser crossovers soften
  `D_⊥` proportionally; uncoupled layers add linearly and coupled layers by the parallel-axis
  theorem.
- Quadrupling `k_f` quarters the deflection.

### Gate 3 — symmetry and conservation

- **Force balance**: foundation reaction + support reactions = applied load, to `1e−8`.
- A symmetric load case produces a symmetric deflected shape and equal support forces.
- The shear crossing a cut vanishes at both free edges and, by symmetry, at the centre.
- **Equipartition**: for a rigid plate the piston fluctuation is *exactly* `√(k_BT/(k_f A))` —
  exact rather than approximate, because the piston mode `P₀P₀` has vanishing second
  derivatives, carries no bending energy, and is therefore exactly decoupled in the Ritz
  matrix. The two rigid tilts are exactly `√2` pistons, for the same reason.
- The per-path force times the path count returns the total.

### Gate 4 — numerical convergence

- The Gauss rule integrates degree `2n−1` exactly, and reproduces Legendre orthogonality
  to `1e−12`; the Ritz integrands are polynomials of known degree, so quadrature contributes
  no error at all and the **only** approximation is the basis truncation.
- The uniform-load answer is exact at *every* basis degree, including degree 2.
- The point-load deflection is monotone non-decreasing in the basis size — the certain
  property of a Ritz restriction — and changes by < 5% between degrees 16 and 20.
- The thermal dishing amplitude converges monotonically: 1.248 / 1.272 / 1.279 nm at degrees
  8 / 12 / 16, i.e. **0.5% between the last two**. Recorded in the result JSON, not just tested.

### Gate 5 — literature cross-check, premises checked against the material

- **Hertz–Westergaard** `w(0) = P/(8√(D k_f))` reproduced by the finite-plate solve on a plate
  3.6 `ℓ` wide, to within 30%.
- The isotropic reduction of the Huber form (`D_1 = νD`, `D_k = D(1−ν)/2`) asserted.
- **The interhelical distance is measured, not designed**: SAXS gives 2.69 nm for a
  single-layer sheet where Rothemund's 1 nm gap implies ~3.0 nm. The measured value is used.
- **The crossover-spacing convention was checked against the primary sources rather than
  recalled**: 16 bp is the per-helix figure, 32 bp the per-interface one, 21 bp the honeycomb
  per-interface one. All three circulate; only one is right here.
- **The §4(f) bands were traced to their primary source** — Shrestha et al., *Nucleic Acids Res.*
  **44**:6574 (2016), dual-trap optical tweezers at **5.5 pN/s** in 20 mM Tris / 10 mM MgCl₂ —
  and the trace changed their meaning. See the next section.

---

## What the literature trace changed

**The 35–60 pN band is not a per-load-path allowable.** It is the force at which an *entire
cross-section* of a 6- or 8-helix origami tube fails, with 6–8 Holliday junctions in parallel,
at one stated loading rate. Shrestha et al.'s own thesis is that the number scales with the
areal junction density along the stress axis (0.17 HJ/nm for a nanotile against 1.43 HJ/nm for
the eight-tube, giving 28 pN against 49 pN). **Using 35–60 pN as the capacity of one crossover
would overstate it by roughly the parallel-junction count.**

Per-path allowables, all traced to primary measurement:

| path | allowable | loading rate | source |
|---|---|---|---|
| hybridised staple domain, **shear** | 48 ± 2 pN (30 bp) … 65 pN | ~50 nm/s … 2697 pN/s | Strunz et al. *PNAS* **96**:11277 (1999); Morfill et al. *Biophys. J.* **93**:2400 (2007) |
| hybridised staple domain, **unzip** | **10–15 pN** | near equilibrium | Essevaz-Roulet et al. *PNAS* **94**:11935 (1997) |
| blunt-end **stacking** contact | single-digit pN, 1–5 `k_BT` | 20 nm/s | Kilchherr et al. *Science* **353**:aaf5508 (2016) |
| any **nicked** duplex — hard ceiling | **65 pN** | rate-independent | van Mameren et al. *PNAS* **106**:18231 (2009) |

Two design consequences fall straight out and are recorded here because nothing else in the
programme will otherwise pick them up:

1. **Shear rupture saturates with domain length** (~70 pN asymptote, Strunz et al.). A longer
   staple domain does not buy proportional capacity.
2. **Unzip geometry is 4–6× weaker than shear.** A load path presented in unzip geometry is
   effectively not load-bearing. That is the single largest design lever in this task, and it
   costs nothing.

---

## Validity range

Enforced in code where possible, not merely documented.

- **`ℓ_⊥ < p` across the entire sweep**, so the continuum plate reduction *across* the helices
  is marginal: the bending length is shorter than the crossover spacing. The tile is better
  described as ~15 quasi-independent duplex beams sharing a cushion. Every conclusion above is
  therefore **conservative about flatness**: a discrete lattice has *more* shape freedom than
  the plate that approximates it, not less. Emitted as `continuumPlateReductionValid` in the JSON.
- **Kirchhoff theory** is safe for a 2 nm sheet spanning 40 nm and **not** safe for the 10 nm
  four-layer reading, where thickness/span reaches 1/4 and transverse shear is neglected. That
  variant is reported as a bound, not an answer.
- **The four-layer `D_⊥` is a lower bound**: interlayer coupling is applied only along the
  helices, because the across-helix axial stiffness of a crossover is not determined anywhere.
- **`D_k` is an upper bound**: duplex torsion is taken as fully coupled to the plate slope,
  which the same soft hinges that set `D_⊥` would partly relieve.
- **Linear Winkler foundation**, where the real layer is strongly nonlinear (`C-0001` gate 2).
  Three different `k_f` are carried — at rest, secant, at the working point — and swept, rather
  than one being chosen.
- **`k_f` comes from `C-0001`, whose numbers are lower bounds per `CH-0001`** and are being
  re-derived under `T-1c`. Every conclusion is stated as a function of `k_f`; the corrections in
  flight run toward *softer*, which softens all the verdicts and flips only the thermal one,
  below 0.30 × `C-0001`.
- **No electrostatics is solved.** The load enters as a 100 pN total and a bounded edge taper.
  `T-3` owns the load model, and the linearity demonstrated above means `T-3`'s answer can be
  substituted without re-running anything.
- **Static, and the foundation is DRAINED — now a citation rather than an assumption.**
  [`C-0004`](C-0004-poroelastic-drainage.md) (`T-7`) puts the layer's drainage corner frequency
  at **91 kHz** at this design point, with 22× margin even for the 70 × 100 nm test tile. At the
  ≥ 1 kHz operating point the layer is therefore fully drained, and treating it as an *elastic*
  Winkler foundation rather than a poroelastic one is justified. Had it been undrained the
  foundation would have been stiffer and effectively incompressible, which would have raised
  `k_f`, shortened `ℓ`, and made every flatness verdict here **worse**, not better.
- **Larger test tiles are worse on flatness and much worse on damping.** `ℓ` is a material length
  and does not grow with the tile, so a 70 × 100 nm test tile has `ℓ_⊥/(L/2) ≈ 0.08` and contains
  ~180 independent patches instead of 44. `C-0004` separately reports that squeeze-out drag is
  97% of the total damping and scales as the footprint squared. Scaling the tile up therefore
  costs flatness linearly and bandwidth quadratically.
- **Rupture forces are loading-rate dependent.** Everything here is quasi-static, i.e. below
  the slowest rate any cited measurement used, so the per-path allowables are extrapolations
  downward. A 100 pN static bias is not a 5.5 pN/s ramp.

## Numbers that are cited rather than derived

Flagged per §7 of the problem definition.

- `EI = 230 pN·nm²`, `GJ = 460 pN·nm²` — **CITED**, the CanDo parameter set (Kim et al., *NAR*
  **40**:2862, 2012). These are *model inputs* in that paper, not measurements. `EI = 230` implies
  `L_p = 55.5 nm`, stiffer than the 47 nm measured in 10 mM Na⁺ and the ~40 nm measured with
  Mg²⁺ (Wang et al., 1997). Using 40 nm would lower `D_∥` by 28% and `ℓ_∥` by 8%; no conclusion
  moves. **`D_⊥` does not contain `EI` at all** except through Chen et al.'s `k_θ = 2αB/(100a)`.
- `S = 1100 pN` — **CITED, MEASURED**, Wang et al. (1997).
- `k_θ` and the factor 100 in `k_θ = 2αB/(100a)` — **CITED**, Chen et al. (2014) SI. The `1/100`
  is carried over from CanDo's *nick* softening factor and is a modelling assumption; only `α`
  was fitted. This is the **single largest open premise under this claim**, and it is swept.
- The inter-crossover twist coefficient `0.3772 k₁` — **CITED**, same source. Its geometric
  origin is not re-derived here, which is why the nominal `D_⊥` excludes it and reports it as a
  16% reduction rather than folding it in.
- The 10–35 / 35–60 pN bands — **CITED, MEASURED**, Shrestha et al. (2016), at 5.5 pN/s, and
  **whole-structure, not per-path**.
- `λ_D = 4 nm`, the 100 pN target, the 40 × 40 nm footprint, the 10 nm thickness — §3.
- `m_eff = 1.672` — **CITED**, `C-0002`.
- `k_f` — **DERIVED** from `C-0001`, itself under challenge.

Everything else is derived from these in code.

## Challenges

**Raises [`CH-0005`](../challenges/CH-0005-rigid-tile-assumption.md) against `C-0001`.**
None stands against this claim.

A further result contradicting this claim should be raised in `gpd/challenges/` with
methodological grounds rather than overwriting it.
