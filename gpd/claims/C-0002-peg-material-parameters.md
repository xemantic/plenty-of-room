# C-0002 — PEG/water material parameters, and the osmotic law the Gen-1 layer actually obeys

| | |
|---|---|
| **Task** | [`P-3`](../tasks/P-3-peg-material-parameters.md) |
| **Leaf** | none — premise task under `A2.1` |
| **Verification type** | logical + in-silico, closed against published measurement |
| **Verdict** | **PASS** — all seven acceptance predicates discharged, (g) by naming three open items rather than answering them |
| **Maturity** | **TRL 1–3. The equation of state is fitted to measurement; nothing about *this* layer is measured.** |
| **Provenance** | `gpd/results/P-3-peg-material-parameters.json`, produced by `material.PegMaterialStudyKt`, 119 tests green |
| **Conditions** | T = 300 K, `k_BT = 4.142 pN·nm`; aqueous. The equation of state was fitted at 20 °C in **pure water** |
| **Raises** | [`CH-0001`](../challenges/CH-0001-semidilute-premise.md) against `C-0001` |

---

## The parameter sheet

Every row carries its provenance. `DERIVED` means recomputed in code from more primitive inputs.

| symbol | quantity | value | unit | provenance |
|---|---|---|---|---|
| `M₀` | ethylene oxide monomer molar mass | 44.053 | g/mol | **DERIVED** — `C₂H₄O` from atomic weights |
| `V̄` | partial specific volume in water | 0.825 | mL/g | CITED — Cohen et al. (2009) ref 31 |
| `v₀` | **monomer volume** | 0.06035 | nm³ | **DERIVED** — `M₀V̄/N_A` |
| `v₀^(1/3)` | volumetric monomer size | 0.3922 | nm | **DERIVED** |
| `ρ` | hydrated mass density | 1.212 | g/cm³ | **DERIVED** — `1/V̄` |
| `l_c` | all-trans contour length per monomer | 0.3639 | nm | **DERIVED** — bond geometry |
| `a` | Alexander-de Gennes effective monomer length | 0.35 | nm | CITED **+ corroborated twice** |
| `v₀/a³` | reduced→physical volume-fraction correction | **1.408** | 1 | **DERIVED** |
| `b` | Kuhn length | 1.1 | nm | CITED — Rubinstein & Colby Tab. 2.1 |
| `M_K` | Kuhn segment molar mass | 137 | g/mol | CITED — same |
| `n_K` | monomers per Kuhn segment | 3.110 | 1 | **DERIVED** |
| `v_K` | Kuhn segment volume | 0.1877 | nm³ | **DERIVED** |
| `b³/v_K` | Kuhn segment aspect ratio | **7.09** | 1 | **DERIVED** |
| `d_K` | Kuhn segment effective diameter | 0.4661 | nm | **DERIVED** |
| `α` | **crossover index** of the osmotic EOS | **0.49 ± 0.01** | 1 | **MEASURED** — see below |
| `k_BT/v₀` | osmotic pressure scale | 68.63 | pN/nm² (MPa) | **DERIVED** |
| `θ` | theta temperature of PEO/water | 375 | K | CITED |
| `τ` | reduced temperature at 300 K | 0.200 | 1 | **DERIVED** |

### `a = 0.35 nm` is no longer an inherited number

`C-0001` flagged it as cited. It is now closed from two independent directions:

- **derived**: the all-trans contour length of `-CH₂-CH₂-O-`, from bond lengths and the backbone angle
  alone, is **0.3639 nm** — 4.0% away, and the ether bond-angle spread accounts for 1.2% of that;
- **fitted**: unconstrained two-parameter fits of the Alexander-de Gennes form to PEG-brush
  compression give `0.356 ± 0.07 nm` and `0.330 ± 0.15 nm` (Hansen et al. 2003).

The agreement also **identifies** what `a` is: a *contour* length. It is therefore not a volume,
and `a³ = 0.0429 nm³` is not the monomer volume — a distinction that turned out to matter (`CH-0001`).

### The Kuhn segment is a thin rod

`b³/v_K = 7.09`: PEG's statistical segment is 1.1 nm long but occupies 0.188 nm³.
No single-parameter scaling picture can be right about a segment's extent and its volume at once
when they differ by a factor of seven, which is why this claim carries three separate lengths
and forbids substituting one for another.

---

## The claim that matters

**For PEG in water, the osmotic pressure is**

&nbsp;&nbsp;&nbsp;&nbsp;**`Π(φ) = (k_BT/v₀) · [ φ/N + α φ^(9/4) ]`**, &nbsp; `α = 0.49 ± 0.01`, &nbsp; `v₀ = 0.06035 nm³`

with `φ` the physical polymer volume fraction and `N` the monomers per chain — a one-parameter
non-virial interpolation fitted by Cohen, Podgornik, Hansen & Parsegian (*J. Phys. Chem. B* **113**:3709, 2009)
to Rand's osmometry on **twelve** PEG molecular weights over 0–50 wt %, with `r² = 0.9926`,
and shown by them to coincide with the Ohta–Oono renormalisation-group equation of state.

Two consequences replace two things `C-0001` had to leave open.

### 1. The exponent is not a choice. It is a function of where the layer sits.

&nbsp;&nbsp;&nbsp;&nbsp;`m_eff(φ) = d lnΠ/d lnφ = 1 + (5/4)·x/(1+x)`, &nbsp; `x = α N φ^(5/4)`

which runs monotonically from 1 to 9/4 and equals exactly **13/8** at the crossover `φ# = (αN)^(−4/5)`,
for every material and every chain length.

**At the surviving `T-1` design points:**

| design point | `N` | `φ` | `φ#` | `φ/φ#` | regime | `m_eff` |
|---|---|---|---|---|---|---|
| `L₀`=5 nm, brush onset | 63.7 | 0.0708 | 0.0637 | 1.11 | CROSSOVER | 1.666 |
| `L₀`=7 nm, brush onset | 113.2 | 0.0439 | 0.0402 | 1.09 | CROSSOVER | 1.659 |
| `L₀`=10 nm, window lower edge | 199.4 | 0.0289 | 0.0256 | 1.13 | CROSSOVER | 1.672 |
| `L₀`=10 nm, window upper edge | 185.1 | 0.0335 | 0.0272 | 1.23 | CROSSOVER | 1.707 |

and compressed to the working height by the §3 target force, `φ/φ#` reaches only 1.30–2.24
with `m_eff` = 1.73–1.92. **The des Cloizeaux domain begins at `φ/φ# = 5` and is never reached.**

This is the answer `P-4` was raised to get, and it is the opposite of the expected one:
the crossover that binds this layer is the **dilute→semidilute** one, from below, at φ ≈ 0.026 —
not the semidilute→concentrated one at 0.2–0.3. See `CH-0001`.

### 2. The brush-onset convention is a material statement, and it is not the one we wanted

At fixed reduced grafting density `Σ`, the ratio `φ/φ#` is **independent of layer height and chain length** —
`Σ = π L₀^(6/5) σ^(3/5)` (the effective monomer length cancels), `φ ∝ σ^(2/3)`, `φ# ∝ σ^(4/15)` —
and at the conventional onset `Σ = 5` it equals

&nbsp;&nbsp;&nbsp;&nbsp;**`φ/φ# = 1.085`, for any PEG layer of any thickness.**

Proved as an exact identity in `PegWaterTest`, not observed numerically.
So `Σ ≥ 5` is not arbitrary — it corresponds to a definite point on the measured equation of state —
but that point is the **middle of the crossover**, not the semidilute regime the brush theory assumes.
This substantively resolves `P-5`: the criterion should be stated on `φ/φ#`, where it is falsifiable,
rather than on `Σ`, where it is a convention.

### 3. A genuine des Cloizeaux brush is out of reach at these thicknesses

Grafting density required to reach `φ = 5φ#`:

| `L₀` | required `σ` | `s` | `N` | `φ` | × the design window |
|---|---|---|---|---|---|
| 5 nm | 3.96 nm⁻² | 0.50 nm | 18.2 | 0.87 | 165× |
| 7 nm | 2.02 nm⁻² | 0.70 nm | 31.8 | 0.56 | 84× |
| 10 nm | 0.99 nm⁻² | 1.00 nm | 57.7 | 0.35 | 41× |

At 5 and 7 nm this is not physically realisable — the grafting spacing falls below the chain's own
Kuhn diameter (0.466 nm). At 10 nm it is a melt-like layer that §4(a) rules out as far too stiff.
**The compliance §4(a) wants and the semidilute structure the brush theory needs pull in opposite
directions for PEG**, and that tension is a `T-2` input, not a detail.

---

## Prefactors `C-0001` left open, now closed against measurement

| quantity | value |
|---|---|
| de Gennes convention `k_BT/s³` at the design point | 0.01540 pN/nm² |
| measured des Cloizeaux limb at the layer's own `φ` | 0.01157 pN/nm² |
| **ratio (measured / convention)** | **0.751** |
| height-matched SCF excluded volume `w = π²a³/4` | 0.10579 nm³ |
| measurement-consistent `w = 2α v₀ φ^(1/4)` | 0.02438 nm³ |
| **ratio** | **0.230** |

So the de Gennes prefactor-of-unity convention **overstates** the layer's osmotic pressure at first
contact by 33%, and the height-matched excluded volume is **4.3×** too large — not the ~25× `C-0001`
estimated from `w = a³(1−2χ)` with `χ ≈ 0.45`. That estimate was too pessimistic by a factor of 5.7,
because it used `a³` where the monomer volume belongs.

The measured total *bulk* pressure at that `φ` is 0.02151 pN/nm², i.e. 1.40× the de Gennes convention —
but that includes the van't Hoff limb, which a **grafted** layer does not have, since grafting removes
chain translational entropy. The des Cloizeaux limb is the right comparison for a brush.
Both numbers are reported because conflating them is easy and would flip the sign of the correction.

---

## The §2 chain-tension premise is discharged

The problem definition reports that chain tension degrades PEG's solvent quality above ~30 pN,
"within a factor of two of the tension in a densely grafted brush". At our design point it is not:

- applied: 100 pN shared by 38 chains = **2.60 pN**;
- the brush's own stretching tension `3k_BT L₀/(N_K b²)` = **1.60 pN**;
- total **4.21 pN per chain — a factor of 7 of margin.**

Stronger: the intrinsic tension is `3k_BT n_K a^(5/3) σ^(1/3) / b²`, **independent of chain length**
and only a cube root in grafting density, so even a melt-like `σ = 1 nm⁻²` layer reaches just 5.55 pN.
**The 30 pN premise cannot be violated by grafting density in this system.** It would take an applied
1.09 nN over the 40 × 40 nm tile — eleven times the §3 target force.

---

## Validity range

- **Temperature.** The equation of state was fitted at 20 °C; we evaluate at 300 K. A ≤ 2.4% shift in
  the `k_BT` prefactor, smaller than the ±2% fit uncertainty on `α`. Stated, not hidden.
- **Medium.** Fitted in **pure water**. The Gen-1 buffer is 2–10 mM MgCl₂. See open items.
- **Bulk, not grafted.** This is a bulk-solution equation of state. Its van't Hoff limb is *not* a
  brush's restoring pressure; what the limb does is locate the density at which semidilute structure
  exists at all. That is how Hansen et al. use it and it is how it is used here.
- **Concentration range.** Fitted over 0–50 wt %, which contains every `φ` in this claim.
- **Linear PEG.** `α` was fitted to linear PEG. A PS→PEG reinitiation block copolymer (§3) is not that material.
- **Finite-chain corrections** (*Biophys. J.* **101**:2790, 2011) exist and are **not** incorporated.
  Our `N ≈ 60–200` sits inside the fitted range (which included 8 k, 10 k and 20 k Da), so the
  correction is expected to be small, but this is an expectation, not a check.

## Cross-checks passed

1. **Gate 1** — `k_BT/v₀` reduces to pN/nm² = MPa; the specific-volume conversion inverts exactly.
2. **Gate 2** — both asymptotes recovered; `m_eff → 1` dilute and `→ 9/4` concentrated.
3. **Gate 3** — the des Cloizeaux limb is exactly chain-length independent (that *is* des Cloizeaux's
   result); the van't Hoff limb is exactly `∝ 1/N`; `m_eff(φ#) = 13/8` exactly, for any material.
4. **Gate 4** — the closed-form `φ#` is reproduced by bisection on the limbs to 1e-9; the closed-form
   `m_eff` is reproduced by a central difference converging at the expected order.
5. **Gate 5** — **two independent fits of the same material agree to 6.1%.** Cohen et al. (2009),
   `α = 0.49` with `φ = V̄C`, against Hansen et al. (2003), `α = 0.8` with `φ = n a³ = 0.586 w`:
   at equal physical weight fraction the des Cloizeaux limbs give 21.82 and 23.22 MPa·w^(9/4).
   Acceptance was 10%. Separately, the contour-length function reproduces polyethylene's
   crystallographic repeat (0.2534 nm) to 0.1%.

## Still open — named, not answered

Per §7: *"where a question can't be answered with the available methods, that is stated plainly."*

1. **`χ(T, salt)` in 2–10 mM MgCl₂ is not determined.** The adopted equation of state is non-virial by
   construction, so it yields neither `A₂` nor `χ`; and no source for the Mg²⁺ salting-out coefficient
   was found this iteration. What *is* bounded: 10 mM of a divalent chloride shifts `θ` by O(0.1–1 K)
   out of 375 K, ≤ 0.7% of `τ`, far below the fit uncertainty on `α`. **That bound is an argument, not
   a citation**, and it is queued as `P-6`. `χ ≈ 0.45`, cited by `C-0001`, is neither confirmed nor
   used by anything in this claim.
2. **Block-copolymer chemistry.** §3 offers PEG, PEO, or a PS→PEG block copolymer. `α` covers the first two.
3. **The 10–16 nm dense-PEG-brush height range** cited by `C-0001` remains untraced. Nothing depends on
   it; the recommendation is to delete it rather than source it.

## Challenges

`CH-0001` is raised **by** this claim against `C-0001`. None stands against this one.
