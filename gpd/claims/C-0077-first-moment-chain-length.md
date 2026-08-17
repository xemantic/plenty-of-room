# C-0077 — The chain-length gap split exactly: the convention is worth 2.82×, the physics 1.64×, and the two multiply

| | |
|---|---|
| **Task** | [`T-1e`](../tasks/T-1e.md) |
| **Leaf** | `A2.1` |
| **Verification type** | in-silico (numerical SCF, Edwards propagator — `C-0011`'s machinery, unedited), with the two trial-function models read on the **same** functional and the strong-stretching first moment closed against its Beta-function form |
| **Verdict** | **PASS** — all six acceptance predicates discharged. The definitional part of `CH-0010`'s chain-length gap is separated from the physical part by a root find, not by a scaling |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. Nothing about this layer is measured.** |
| **Provenance** | `gpd/results/T-1e-first-moment-convention.json`, produced by `brush.FirstMomentConventionStudyKt`; 558 records (183 design points × 3 profile models + 9 design-point rows across the three interaction laws), **218 s** on four threads; 13 gate-named `FirstMomentThicknessTest` tests; the result file re-run and diffed **byte-for-byte identical** |
| **Conditions** | T = 300 K, aqueous buffer (2–10 mM MgCl₂, not entering — `C-0007` puts the layer's buffer dependence at ≤ 0.4 %), `k_BT = 4.141947 pN·nm`; 40 × 40 nm tile (A = 1600 nm²); linear PEG, `M₀ = 44.053 g/mol` |
| **Consumes** | [`C-0011`](C-0011-scf-density-profile.md) (the solved layer, its grid, its resting-load convention), [`C-0003`](C-0003-crossover-valid-layer-response.md) (the interaction bracket and the cited `N` bracket), [`C-0002`](C-0002-peg-material-parameters.md) (`v₀`, `b`, `n_K`) |
| **Raises** | [`CH-0090`](../challenges/CH-0090-the-scaling-estimate-uses-the-exponent-of-a-different-quantity.md) against `C-0011`/`CH-0010` and `C-0003`; [`CH-0091`](../challenges/CH-0091-a-first-moment-ten-nanometre-layer-is-not-a-ten-nanometre-layer.md) against `C-0016` |
| **Discharges** | `CH-0010`'s first outstanding item — *"a first-moment-convention inversion … would separate the definitional part of this challenge from the physical part exactly rather than by scaling"* |

---

## The claim, in one line

**At the 10 nm design point the chain-length gap between `C-0003`'s trial functions and `C-0011`'s
solved layer is `2.819 × 1.648` and not one number: the height convention is worth `2.819×` and the
physics — the conformational normal stress neither trial function contains — is worth `1.636–1.648×`,
so `CH-0010`'s *"most of the chain-length gap is the convention"* is upheld and quantified at
**67.5 % of it on a logarithmic scale**. The exact first-moment chain is `N = 175.08` monomers,
**7.71 kDa**, which is **outside** the `190–210` `C-0011` estimated by scaling — and the two trial
functions, which disagree by 28 % in their own conventions, agree with each other to **0.76 %** in
this one.**

---

## The two conventions, named before any number

| | `L₀^F` — **force-onset** | `L₀^M = 2⟨z⟩` — **first-moment** |
|---|---|---|
| definition | the height at which the layer carries 1 pN over the tile | `2 ∫zφ dz / ∫φ dz` of the profile at that chain's own resting height |
| what it is | **where the tile sits** | a moment of the profile — what ellipsometry or reflectivity returns |
| exact for a box | no (a box's `P` reaches zero at `L`, so `L₀^F < L`) | **yes** — `⟨z⟩ = L/2` identically, at every `N` and every `σ` |
| whose convention | `T-1d`, `T-2`, `C-0016`, the whole device branch | `C-0003`'s Alexander box, and any measurement |
| threshold sensitivity of `N` | **2.494×** over two decades of the defining load | **1.103×** over the same two decades |

**Neither is more correct. They answer different questions, and a chain length quoted without one
of them is the same class of error as a stiffness quoted without a compression.**

---

## The cheap bound, and what it could not do

Run before any solver, from `C-0011`'s own committed file. `L₀^F/2⟨z⟩ = 1.8319` at the design point,
so `N_M = N_F · 1.8319^(1/p)`:

| `p` | source | `N_M` | error against the exact 175.08 |
|---|---|---|---|
| 0.4548 | `d ln L₀^F/d ln N`, 5 → 7 nm | 235.07 | **+34.3 %** |
| 0.4683 | `d ln L₀^F/d ln N`, 7 → 10 nm | 226.19 | **+29.2 %** |
| 0.5007 | `d ln 2⟨z⟩/d ln N`, 5 → 7 nm | 208.05 | +18.8 % |
| 0.5200 | `d ln 2⟨z⟩/d ln N`, 7 → 10 nm | 198.91 | +13.6 % |
| 0.50 / 0.55 | `C-0011`'s quoted `N^(0.5–0.55)` | 208.39 / 186.68 | +19.0 % / +6.6 % |
| 0.49 / 0.64 | `C-0011`'s own stated 0.49–0.64 band | 213.60 / 159.91 | +22.0 % / −8.7 % |

**Every reading at a measured exponent overstates the answer**, because the exponent is drifting
upward with `N` (0.5007 → 0.5105 → 0.5200 over the three pairs) and the extrapolation is 2.8× in `N`
beyond the range it was measured over. The published `190–210` does **not** contain 175.08 — and it
is a narrower band than the claim's own 0.49–0.64 would give (159.9–213.6). That is `CH-0090`.

The cheap bound also could not, in principle, say the three things the exact inversion says:

1. **the shape ratio is not a constant of the layer** — it runs **1.372 → 2.069** across `T-1d`'s own
   10 nm grid, so a single-point scaling cannot be transferred to the window edges;
2. **which arithmetic the decomposition takes** — that needs the factor and the difference measured
   across the grid, not at a point;
3. **how threshold-dependent `2⟨z⟩` is**, which is the whole of the convention recommendation.

---

## The numbers

### The design point — 10 nm, `σ = 0.024 nm⁻²`, three interaction laws

| | two-body | virial | **des Cloizeaux** |
|---|---|---|---|
| `N` at `L₀^F = 10 nm` (`C-0011`'s convention) | 63.042 | 62.739 | **62.108** |
| `N` at `2⟨z⟩ = 10 nm` (**this claim**) | 191.658 | 183.918 | **175.080** |
| PEG at `2⟨z⟩ = 10 nm` | 8.443 kDa | 8.102 kDa | **7.713 kDa** |
| **convention factor `N_M/N_F`** | **3.040** | **2.932** | **2.819** |
| Alexander box, `N` at `2⟨z⟩ = 10 nm` | 374.374 | 320.589 | 288.561 |
| strong stretching, `N` at `2⟨z⟩ = 10 nm` | 371.199 | 317.986 | 286.375 |
| **physics residue** (trial ÷ solved, same functional) | **1.937 – 1.953** | **1.729 – 1.743** | **1.636 – 1.648** |
| force-onset height of the `2⟨z⟩ = 10 nm` layer | 17.232 nm | 17.111 nm | **17.002 nm** |

`C-0003`'s published bracket is reproduced to the last digit by the two extreme corners:
box/two-body **374.374** against its 374.3, strong-stretching/des-Cloizeaux **224.402** against its
224.8 — which is what says this study and `C-0003` are inverting the same models.

### The decomposition — a PRODUCT, and it has three factors, not two

Against the Alexander box, whose two conventions coincide, the two-factor form is exact:

&nbsp;&nbsp;&nbsp;&nbsp;`288.561 / 62.108 = 4.646 = 2.819 × 1.648` — **exactly**, at all three interactions.

Against strong stretching it does **not** close in two factors, and the missing one is the trial
function's *own* convention factor:

&nbsp;&nbsp;&nbsp;&nbsp;`224.402 / 62.108 = 3.613 = 2.819 × 1.636 / 1.276`

where `1.27616 = 1/[(p+1)·B(p)]` with `p = 4/5` is the strong-stretching parabola's Beta-function
shape ratio — a closed form, asserted as a test. **`CH-0010`'s comparison looked like one convention
because it compared two models each read in its own.**

**The product is the transferable form and the sum is not**, and that is measured rather than
preferred: across the 61-point 10 nm grid the convention **factor** spreads **1.460×** (1.947 → 2.843)
while the convention **difference** spreads **3.742×** (34.5 → 129.1 monomers). The product form is
2.563× the more stable of the two.

### How much of the gap is the convention

| interaction | total gap vs the box | convention | physics | convention's share, `ln`-scale |
|---|---|---|---|---|
| two-body | 5.939 | 3.040 | 1.953 | **62.4 %** |
| virial | 5.110 | 2.932 | 1.743 | **65.9 %** |
| des Cloizeaux | 4.646 | 2.819 | 1.648 | **67.5 %** |

**`CH-0010`'s word "most" is upheld: 62–68 % of the gap is definitional. The rest — a clean
`1.64–1.95×` — is the physics, and it is not small.**

### The physical residue, with its uncertainty

**`1.64×` at the des Cloizeaux limb, `1.64 – 1.95×` over the measurement-anchored interaction
bracket, and `1.21 – 1.92×` across `C-0016`'s whole 10 nm window.** Its uncertainty has four named
sources and they are all in the file:

| source | contribution to the residue |
|---|---|
| the interaction law (`C-0003`'s three) | 1.636 → 1.953, **19 %** — the largest |
| box against strong stretching, on one functional | 1.636 → 1.648, **0.8 %** — negligible, and that is the finding |
| grafting density, across `C-0016`'s 10 nm window | 1.218 → 1.925, and it **falls monotonically with `σ`** |
| the node spacing, at the production `Δz = 0.2 nm` | **0.5 %** in `N_M` (see the convergence gate) |

The residue **falls with grafting density** — 1.92 at the window's lower edge, 1.22 at the upper —
which is the expected direction: the conformational term the trial functions omit is largest where
the layer is most coil-like, and the trial functions were built for the dense limit.

### The threshold — the argument for which convention to quote

At the 10 nm design point, over the two decades of defining load `C-0011` carries:

| resting load | `N` at `L₀^F = 10 nm` | `N` at `2⟨z⟩ = 10 nm` | `2⟨z⟩` of the **1 pN chain** at that wall |
|---|---|---|---|
| 0.1 pN | 43.563 | 173.258 | 5.4857 nm |
| **1 pN** | **62.108** | **175.080** | **5.4592 nm** |
| 10 pN | 108.643 | 191.112 | 5.2403 nm |

**A hundred-fold change in the defining load moves the force-onset chain by `2.494×` and the
first-moment chain by `1.103×`** — a factor of **9.31** in logarithmic sensitivity. Held at a fixed
chain the first moment moves **4.7 %** over the same two decades.

**So the first-moment thickness is the better-posed quantity, and the force-onset height is the one
the device occupies.** Those are not in conflict and the recommendation below turns on which
question is being asked.

### What a bench would order, in both conventions, across `C-0016`'s 10 nm window

| 10 nm window, `σ ∈ [0.01163, 0.26015]` | force-onset (`C-0016`'s convention) | first-moment |
|---|---|---|
| `N` | 36.6 – 74.0 | **94.7 – 198.1** |
| PEG | 1.61 – 3.26 kDa | **4.17 – 8.73 kDa** |
| ratio between the conventions | — | **2.585 – 2.843×** |
| shape ratio `L₀^F/2⟨z⟩` | — | 1.710 – 2.065 |
| where the tile sits | 10 nm, by construction | **16.08 – 18.05 nm** |

and at 7 nm, `σ ∈ [0.0296, 0.0496]`:

| 7 nm window | force-onset | first-moment |
|---|---|---|
| `N` | 25.3 – 27.3 | **88.0 – 93.6** |
| PEG | 1.11 – 1.20 kDa | **3.88 – 4.13 kDa** |
| ratio between the conventions | — | **3.430 – 3.481×** |
| where the tile sits | 7 nm, by construction | **12.73 – 13.03 nm** |

`C-0016`'s banner says the two conventions differ *"by about four times"* and names *"8–9 kDa"*. The
factor is **2.59–2.84** and the band is **4.17–8.73 kDa**; 8–9 kDa is the value at the window's
**lower edge only**. That is `CH-0091`, and the banner's *point* survives it intact.

---

## Which convention the programme should quote

**Quote the FORCE-ONSET height for the device and the FIRST-MOMENT thickness for the polymer, and
never either one alone.**

The reason is not aesthetic and it is not the threshold sensitivity. It is that **the two conventions
name different devices**, and only one of them is admitted by the specification:

> A layer specified at `2⟨z⟩ = 10 nm` puts its tile at **13.20 – 18.05 nm** across the 61-point
> grafting-density grid — **0 of 61** points inside §3's stated 5–10 nm layer-height band.

§3 specifies where the tile is. That is a force-onset statement, so **`C-0016`'s window is in the
right convention and no edge of it moves.** But a bench does not buy a force onset; it buys a
molecular weight, and the molecular weight that produces a given *measured* thickness is the
first-moment one. Both numbers therefore have to travel, and this claim is the conversion between
them.

This is the same discipline `CLAUDE.md` already records twice over — *"a grafted layer has no resting
height unless you define one"*, and *"quote `L₀` with the load it was defined at"* — applied one level
further out, to the **functional** rather than to the load. It is the **eighth** instance in this
programme of a quantity that is not well posed without the state it is read at, and the first where
the state is a *definition* rather than a load, a compression, a bandwidth or a lattice coordinate.

---

## Verification

All five gates, as executable tests. `src/test/kotlin/brush/FirstMomentThicknessTest.kt`, 13 tests,
each named for the gate it discharges.

### Gate 1 — dimensional consistency

- `2⟨z⟩` is `nm⁴/nm³`, a length; a box profile's is its own height to `1e-14`, at three chain lengths
  × three grafting densities.
- The inverted chain length reproduces the requested thickness to `1e-5` for **all three** models —
  the root find's own residual, asserted rather than assumed.

### Gate 2 — limiting cases

- **A box layer's two inversions are the same inversion**, to `1e-5`, at 61 grafting densities × 3
  heights. `2⟨z⟩ = L` exactly for a box, so its force-onset seed **is** its first-moment answer — and
  that makes every one of its 183 inversions a floating-point tie. See *"what surprised us"* below.
- **Strong stretching reproduces its closed-form Beta ratio**, `2⟨z⟩/L = 1/[(p+1)·B(p)]` with
  `p = 1/(m−1)`: **0.75 exactly** at `m = 2` and **0.783596** at `m = 9/4`, to `1e-6`. No fitted
  number anywhere, and it is what says the `θ`-substituted quadrature resolves the outer edge, where
  `(L²−z²)^0.8` has an infinite derivative and a uniform-`z` Simpson rule does not.
- The solved layer's first moment is **below** its force-onset height and its first-moment inverted
  chain is **longer**. Both directions asserted, because a sign error in the moment would flip the
  second and not the first.

### Gate 3 — symmetry and conservation

- **Departure exactly `0.0`** between the new accessor and `ScfProfile.firstMomentHeight`, at three
  wall heights. `T-1e` adds a functional to files it does not edit, so the standing quantity comes
  back bit-identical rather than merely close.
- **A first moment is scale-free**, so it is blind to the normalisation its own denominator conserves:
  chains 9× apart in length return the same self-similar ratio to `1e-9`. The coverage identity
  `∫φ dz = Nσv₀` is therefore asserted **separately**, to `1e-9`.

### Gate 4 — numerical convergence, and this quantity earns its own order

`CLAUDE.md`: *"convergence is a property of the quantity."* A first moment is not a contact pressure
and it does **not** inherit the pressure's second order:

| quantity | `Δz = 0.4` | `Δz = 0.2` | `Δz = 0.1` | observed order |
|---|---|---|---|---|
| `2⟨z⟩` | 6.07711 nm | 6.06400 | 6.05964 | **1.59** |
| `N` at a fixed `2⟨z⟩` | 32.849 | 33.176 | 33.3278 | **1.11** |
| `P` (`C-0011`, for contrast) | — | — | — | 2.08 – 2.32 |

**Both are below second order, and the inverted chain length is barely above first.** The production
grid `Δz = 0.2 nm` therefore carries `7.2e−4` in `2⟨z⟩` — the same as the pressure — and **`4.6e−3` in
`N_M`**, which is **6.5×** the pressure's grid error at the same spacing, because the root find
divides by a slope of about one half. It is stated rather than hidden, and it remains **40× inside**
the 19 % the interaction bracket contributes to the same number. The contour step is checked
in its own right and is at least first order.

The cause is structural and is the one `C-0073` named: `M = round(h/Δz)` steps the node count
discontinuously with the solved wall height, and a first moment weights the outer nodes — where the
absorbing-wall profile vanishes as `(h−z)²` — most heavily.

### Gate 5 — literature cross-check, premises checked against the material

- **The Beta-function ratio** above is the Milner-Witten-Cates parabola's own first moment, in closed
  form, and it is what the quadrature is held to.
- **The two trial-function models agree on the first moment and disagree on the edge** — under 0.8 %
  against over 20 %, asserted as a test at the design point.
- **`C-0011`'s emitted `firstMomentHeight` is reproduced** from the committed
  `gpd/results/T-1d-scf-density-profile.json` at the production grid, within the emission slack
  `5e-5` — the file emits six significant digits (`C-0073`), and asserting tighter would be a test of
  the printed digits.
- The Gaussian propagator is licensed by measurement (0.02–0.10 thermal blobs per chain, `C-0003`),
  unchanged from `C-0011`.

---

## Falsifiers, declared in `T-1e` before the run

1. **The exact inversion landing inside `C-0011`'s quoted `190–210`.** **DID NOT FIRE** — 175.08,
   outside it, and every reading at a measured exponent overstates it. The task's premise held.
2. **The decomposition not being a decomposition.** Did not fire: the product form is 2.563× the
   more stable across the grid and the choice is made on that measurement.
3. **`2⟨z⟩` as threshold-dependent as the force-onset height.** Did not fire — 1.103× against 2.494×.
4. **The physical residue at or below one.** Did not fire — 1.636–1.648 at des Cloizeaux, 1.64–1.95
   over the bracket.
5. **The accessor failing to reproduce `T-1d`'s emitted first moment.** Did not fire — departure
   `0.0` against `ScfProfile`, inside the emission slack against the committed file.

---

## Downstream — what moves and what does not

| claim | quantity | moves? | why |
|---|---|---|---|
| `C-0011` | `N(10 nm) = 62.1`, force-onset | **no** | reproduced at 62.1076 in its own convention. A second convention is added beside it; nothing is corrected |
| `C-0011` / `CH-0010` | *"`N ≈ 190–210` by scaling"* | **yes** | the exact answer is 175.08, outside the interval, and the exponent it was read at is the wrong quantity's — **`CH-0090`** |
| `C-0003` | the 224.8–374.3 bracket, read as *"profile uncertainty"* | **yes** | on one functional the two models agree to 0.76 %; most of that bracket's internal spread is a convention difference between `C-0003`'s own two models — **`CH-0090`** |
| `C-0016` / `C-0027` / `C-0051` | the 10 nm window `σ ∈ [0.0116, 0.2601]`, lower edge coil overlap | **no** | the window is in the force-onset convention and §3 specifies where the tile sits. Re-specifying at `2⟨z⟩ = 10 nm` is a **different device**, not a correction, and §3 admits it at 0 of 61 grid points. **No edge moves, no owner changes** |
| `C-0016` | *"8–9 kDa in the first-moment convention"*, *"about four times"* | **yes** | 7.71 kDa at the design point and 4.17–8.73 kDa across the window; the factor is 2.59–2.84 — **`CH-0091`** |
| `C-0002` / `C-0036` | `φ/φ#`, the concentrated crossover, both `∝ N` | **no** | both are read at the layer the tile occupies. The first-moment layer's own `φ` is emitted (0.0149 at the design point, against 0.00900) so the comparison **can** be made, but no standing verdict is read at that state |
| `C-0019` | the `α²`-versus-`N` near-cancellation | **no** | that is a perturbation at **fixed height**; a convention change is not that perturbation and its 0.92 % is not transferable to it. Stated, not recomputed |
| `C-0050` | the kinematic ceiling `L₀ − Nσv₀` | **no** | computed on `C-0003`'s chain in `C-0003`'s convention. This claim moves neither |
| `C-0017`, `C-0018`, `C-0023`, `C-0032`, `C-0033` | the coupled actuator verdicts | **no** | all five state explicitly that they consume `C-0003`'s layer and not `C-0011`'s solved profile |

**No window edge, no stroke, no stiffness and no coupling verdict moves.** What moves is one
extrapolated number, one bracket's interpretation, and one sentence of `C-0016`'s banner.

---

## Validity range

- **TRL 1–3. Nothing here is measured about this layer.** `PASS` means model-consistent and traceable.
- **Everything in `C-0011`'s validity range applies unchanged**, and it is the dominant exposure:
  mean field at `φ ≈ 0.01` with the fluctuation corrections **not bounded** (`T-1f`), an interaction
  free energy **not measured below `φ#`**, monodisperse chains, laterally uniform grafting, a rigid
  tile, mechanical only.
- **The first-moment chains are 2.8× longer and their layers 1.7× taller**, so they sit *further* from
  the equation of state's fitted range in chain length and *closer* to it in volume fraction
  (0.0149 against 0.00900). Neither direction is a new licence, and no downstream verdict is read at
  that state.
- **The sweep carries the des Cloizeaux interaction only.** The three-law spread is measured at the
  design point (convention factor 2.819–3.040, residue 1.636–1.953) and carried into the residue's
  uncertainty rather than swept.
- **The inverted `N` converges at order 1.11 in the node spacing**, not 2, and carries `4.6e−3` of
  grid error at the production spacing. Any use of `N_M` to three significant figures is at that
  precision and no better.
- **Both inversions are bracketed at a relative `1e-6`** and the outer bracket contains a whole
  resting-height solve, so the file emits at `SOLVED_HEIGHT_SIGNIFICANT_DIGITS = 6` (`C-0073`).
- **`2⟨z⟩` is measured on the profile at that chain's own force-onset resting height**, so the
  first-moment convention is not *entirely* threshold-free — it inherits 1.103× of the force-onset
  convention's 2.494× and that is reported rather than removed.
- **`Σ ≥ 1` is not re-imposed on the first-moment layers.** They are all more overlapped than their
  force-onset counterparts (5.14 against 1.82 at the design point), so the 1-D mean field is *better*
  justified there, but no window is drawn on them.

## Numbers that are cited rather than derived

- `C-0003`'s `N(10 nm, σ = 0.024) = 224.8 – 374.3` — **CITED**. The box and strong-stretching models
  **are** recomputed here at every design point, and they reproduce both endpoints (374.374, 224.402).
- `A₂ = 1.9 × 10⁻³ mol·cm³/g²`, `A₃ = 2.0 × 10⁻² cm⁶·mol/g³` — **CITED**, via `C-0003`.
- `b = 1.1 nm`, `M_K = 137 g/mol` — **CITED**, via `C-0002`.
- `C-0011`'s `190–210` scaling estimate and its `N^(0.5–0.55)` exponent — **CITED**, and challenged.

## Challenges

**Raised by this claim:** [`CH-0090`](../challenges/CH-0090-the-scaling-estimate-uses-the-exponent-of-a-different-quantity.md)
and [`CH-0091`](../challenges/CH-0091-a-first-moment-ten-nanometre-layer-is-not-a-ten-nanometre-layer.md).

**Standing against this claim:** none yet. Its two largest exposures are inherited whole from
`C-0011` — the unbounded mean-field correction at `φ ≈ 0.01`, and an interaction free energy that is
not measured over the working range — and one is its own: **the first-moment thickness converges at
order 1.11, so the third significant figure of `N_M` is not determined by the production grid.**
A result that measures either should be raised as a challenge rather than an overwrite.
