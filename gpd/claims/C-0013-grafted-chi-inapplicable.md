# C-0013 — The grafted-`χ` premise does not transfer, and the effect it points at is bounded at 1/4.6 of its claimed size

| | |
|---|---|
| **Task** | [`P-9`](../tasks/P-9-grafted-chi.md) |
| **Leaf** | none — premise task under `A2.1`, consumed by `A2.1` (`T-1c`, `T-1d`) and `A2.2` (`T-3`) |
| **Verification type** | logical + in-silico, closed against two published measurements |
| **Verdict** | **PASS — branch (a), INAPPLICABLE**, with the bound of branch (b) supplied by an independent right-geometry measurement rather than asserted. **`C-0002`'s equation of state stands.** |
| **Maturity** | **TRL 1–3. Both inputs are fits to published measurement; nothing about the Gen-1 layer is measured.** |
| **Provenance** | `gpd/results/P-9-grafted-chi.json`, produced by `material.GraftedChiStudyKt`, 25 `GraftedChiTest` tests green |
| **Conditions** | T = 300 K, `k_BT = 4.142 pN·nm`; aqueous (the SCF fit is in D₂O, the compression fit in water) |
| **Consumes** | [`C-0007`](C-0007-solvent-quality-vs-salt.md) (`χ(300 K) = 0.372`, the water-site lattice convention), [`C-0002`](C-0002-peg-material-parameters.md) (`v₀`), [`C-0003`](C-0003-crossover-valid-layer-response.md) (the `K^(1/(m+1))` exposure) |
| **Raises** | [`CH-0012`](../challenges/CH-0012-grafted-chi-number.md) against `C-0007` |
| **Closes** | `P-9`. Discharges the exposure `C-0003` states in its validity range and the last item of `C-0002`'s "still open". |

---

## The claim, in one line

**`χ ≈ 0.60` for a grafted PEO layer is not a number any source reports.
It is a construction — the ratio `1.2` from a bespoke self-consistent field model, multiplied by the
Flory-Huggins `½`, when that model's own theta point is at `0.696` and its authors say in as many
words that `½` is not it. And the effect it was standing in for, measured independently in the
*right* geometry, is `|Δχ| ≤ 0.053` — 4.6× smaller than claimed and worth −11 % to +4 % of the
layer stiffness.**

---

## What the body of the source actually says

`C-0007` used Lee, Kim, Witte, Ohn, Choi, Akgun, Satija & Won, *J. Phys. Chem. B* **116**:7367–7378
(2012), doi:10.1021/jp301817e, **from its abstract alone**. The body was obtained from **NIST's own
repository** (`tsapps.nist.gov/publication/get_pdf.cfm?pub_id=910992`) — two of the authors are NIST
NCNR staff — and read with `pdftotext -layout`. It answers all four checks `P-9` was raised with, and
it does not answer them all the same way.

### 1. Geometry — an air/water Langmuir monolayer, not a solid-grafted brush

The title is *"Water Is a Poor Solvent for Densely Grafted Poly(ethylene oxide) Chains: A Conclusion
Drawn from a Self-Consistent Field Theory-Based Analysis of Neutron Reflectivity and Surface
Pressure−Area Isotherm Data"*. The system is a **poly(ethylene oxide)–poly(*n*-butyl acrylate)
diblock spread on an air/D₂O interface** in a Langmuir trough. There is no solid substrate anywhere
in the paper. The authors chose it deliberately:

> *"there are a couple of advantageous aspects to using PEO brushes **grafted at the air−water
> interface (rather than the solid−water interfaces)**"*

The grafting plane is a *"flat continuous water-free single-monomer-thick interfacial film"* of PnBA,
and the fit finds it **indifferent** — `κ⁻¹ = ∞`, *"the PnBA−water interface … acts on PEO neither
attractively nor repulsively"*. The reported pressure is a **lateral** surface pressure measured by
Wilhelmy plate against area per chain. **The Gen-1 layer is compressed normally against a rigid tile.
These are different observables of a layer, and only one of them is the actuator's load path.**

### 2. Grafting density — inside the Gen-1 window. **This check does not close the task.**

`C-0007` described the system as a *dense* brush and `P-9` was raised expecting the density to be the
easy way out. It is not.

| | `α` [Å²/chain] | `σ` [nm⁻²] | `Σ = σπR_G²` | `N` |
|---|---|---|---|---|
| Lee et al., condition 1 | 1350 | **0.0741** | 1.54 | 113 |
| Lee et al., condition 2 | 2200 | **0.0455** | 0.94 | 113 |
| **Gen-1 window** (`C-0003`, 10 nm) | — | **0.018 – 0.092** | — | **60 – 375** |

**Both `χ` conditions sit inside the Gen-1 grafting window, and the chain length sits inside the
Gen-1 chain-length window.** The title's *"densely grafted"* is not supported by the paper's own
reduced measure: `Σ ≈ 1` is *at* three-dimensional coil overlap, which is where `CH-0001` and
`C-0002` already established that PEG is **not** semidilute. So `P-9` had to be closed on the
parameter itself. Reported here because a reader who checked only this row would have concluded the
opposite.

### 3. What the `χ` is a parameter *of* — and this is where it closes

Not Scheutjens-Fleer, not Milner-Witten-Cates, not a lattice model. It is the authors' own
**numerical continuum (Edwards-type) SCF**, with **unequal segment volumes**, `v_PEO = 59.2 Å³` and
`v_water = 29.9 Å³`, and two adjustable parameters (`χ` and the adsorption length `κ⁻¹`).

**The fitted values are `χ = 0.789 ± 0.066` and `χ = 0.852 ± 0.051`. The string `0.60` does not occur
as a `χ` anywhere in the paper.**

And the model's own theta point is **not** `½`. The authors located it, by computing the end-to-end
distribution of a single **free** PEO chain in the same model and finding which `χ` reproduces
Gaussian statistics:

> *"the SCF prediction for `χ_PEO−water = 0.50` does not match the Gaussian behavior; the Gaussian
> statistics was precisely reproduced when `χ_PEO−water` is set to about **0.696**."*

They then say what follows, unprompted, in §3.3:

> *"It should also be pointed out that it would not be too accurate to deduce, simply on the basis of
> the result `χ_PEO−water > 1/2`, that the grafted PEO chains indeed experience an unfavorable
> ("poor") solvent environment … **the physical assumptions used in the present SCF model are not
> identical to those of the Flory−Huggins theory from which the `χ_P−S = 1/2` criterion … has been
> originally derived.** For instance, in our SCF model we used a monomer volume that is different
> from the value of the solvent volume used (i.e., `v_PEO = 59.2 Å³` and `v_water = 29.9 Å³`).
> **For this reason alone, simply setting the `χ_PEO−water` value to 0.5 in our model, for example,
> would not be able to produce results that precisely correspond to the behavior under the so-called
> θ [condition]."***

The `≈ 1.2` of the abstract is `0.852/0.696 = 1.224` — a ratio taken against **its own** theta point.
`χ ≈ 0.60` is that ratio multiplied by the Flory-Huggins `½`. **The paper's own text forbids exactly
that step.**

**And there are two linear transfers, not one, and they disagree by 37 % of the effect:**

| | preserve the **ratio** to theta | preserve the **distance** past theta |
|---|---|---|
| `χ = 0.789` | 0.567 | 0.593 |
| `χ = 0.852` | **0.612** ← the "0.60" in circulation | 0.656 |

Spread across both: **0.089**, against a claimed shift of 0.240. A parameter whose value on the
target axis depends by 37 % on which of two equally defensible maps you pick **is not a transferable
quantity**, and that is the finding, stated as a number rather than as an opinion.

**A cross-check that turns the convention offset from a suspicion into a fact.** Lee et al.'s
`v_PEO/v_water = 59.2/29.9 = **1.980**`. `C-0007` derives `v₀/v_site = **2.010**` for the same ratio
from PEG's partial specific volume and the mass density of water, with no knowledge of that paper.
**1.5 % apart.** This is `C-0007`'s own "lattice trap" — *"`χ` lives on a lattice, and the lattice
site is not always the monomer"* — appearing inside a second, independent model. The `0.696` is a
**convention**, not a fitting artefact.

### 4. Is it a solvent-quality statement? — partly, and the part that survives is not the number

Yes as a statement about *that model*: they show `χ = 0.80` visibly shrinks a free chain below
Gaussian, so the fitted layer is on the collapsed side of *its own* theta point. And the positive
surface pressure is explained, not explained away — *"the hydrophobicity … is rather marginal, and
insufficient to overcome the other opposing force of the chain conformational entropy"*.

But that is a **lateral** pressure in a model where stretching entropy supplies the repulsion.
In the Alexander-de Gennes and des Cloizeaux free energies this project actually uses, **all** of the
repulsion under normal compression is excluded volume, and the elastic term *opposes* it.
The two frameworks put the repulsion in different places, so a `χ` fitted in one is not a `χ` in the
other even before the lattice convention is considered. **That is the general reason an effective
parameter does not travel, and this is a clean instance of it.**

---

## The independent bound, in the right geometry

`C-0002` already cited Hansen, Cohen, Podgornik & Parsegian, *Biophys. J.* **84**:350 (2003) for
`a = 0.356 ± 0.07` and `0.330 ± 0.15 nm`. The body was retrieved from PMC1302616 and read.

**Why this fit is the object `P-9` needs.** Hansen et al. fitted the des Cloizeaux amplitude
`α = 0.8` to **bulk** osmometry across seven PEG molecular weights (Rand's data, 0–50 wt %), then
**held it fixed** and let `a` and `L₀` float against **grafted-layer** osmotic-stress isotherms
(Kenworthy et al.'s DSPC:PEG-5000 multilamellar liposomes, compressed **normally**). So
`(a_fit/a_bulk)` is a brush-versus-bulk excluded-volume comparison **inside one convention, one
paper, one dataset family** — and a convention that cancels out of the ratio, which matters because
`C-0003` records that the Alexander-de Gennes unity prefactor is worth **6.6×** in excluded volume.

Their whole paper exists to establish *when* such a fit is entitled to be made: the layer must be in
the bulk des Cloizeaux semidilute regime, and *"the chain-overlap condition does not provide a
sufficient criterion"* — **independently the same finding as `CH-0001`**. They discard PEG-2000 and
every coverage below `f = 0.1` on that ground. The two fits used here are the only ones that survive.

### What their layers are

| | `a` [Å] | `L₀` [nm] | `D` [Å] | `σ` [nm⁻²] | `φ` | `N` |
|---|---|---|---|---|---|---|
| nominal `f = 0.10` | 3.56 ± 0.07 | 10.5 | 26.70 | **0.1403** | 0.0911 | 113 |
| nominal `f = 0.20` | 3.30 ± 0.15 | 10.9 | 20.88 | **0.2293** | 0.1435 | 113 |

**The Gen-1 chain length and the Gen-1 height, at 1.5–2.5× the Gen-1 grafting density and 1.7–5.0×
the Gen-1 volume fraction.** That direction is what makes this a conservative test: if a
brush-specific many-body attraction switches on with density — which is precisely Lee et al.'s
mechanism — it bites **hardest** here, above the window, not inside it.

### The inversion

In the Alexander-de Gennes single-length convention, with `φ = n a³`,

&nbsp;&nbsp;&nbsp;&nbsp;`Π = α (k_BT/a³) φ^(9/4) = α k_BT n^(9/4) a^(15/4)`

so at fixed **physical** monomer density the interaction strength goes as `a^(15/4)` — derived here
as a log-slope, not quoted. Then `K ∝ v^(3/4)` from the space-filling self-avoiding correlation blob
(also derived, with `v ∝ (1 − 2χ)` on `C-0007`'s water-molecule site):

| | `K_brush/K_bulk` | `χ_eff` (blob, 3/4) | `χ_eff` (mean field, 1) | `Δχ` |
|---|---|---|---|---|
| `f = 0.10`, central | 1.066 | **0.360** | 0.363 | **−0.011** |
| `f = 0.10`, ±1σ on `a` | 0.989 – 1.147 | 0.346 – 0.373 | — | −0.026 … +0.002 |
| `f = 0.20`, central | 0.802 | **0.404** | 0.397 | **+0.033** |
| `f = 0.20`, ±1σ on `a` | 0.674 – 0.947 | 0.381 – 0.424 | — | +0.009 … +0.053 |
| **bulk (`C-0007`, measured)** | 1 | **0.372** | 0.372 | 0 |

> **A grafted PEG layer, compressed normally, measured, denser than Gen-1, sits at
> `χ_eff = 0.346 – 0.424` against a bulk `0.372`. `|Δχ| ≤ 0.053`.
> The claimed shift is `0.240`. The bound is 4.6× tighter and it straddles zero.**

The conclusion does not depend on which excluded-volume exponent is right — 3/4 and 1 give the same
answer to 0.007 — which is how `C-0007` established this transfer should be carried.

### The limiting case that makes it decisive rather than merely quantitative

**A des Cloizeaux amplitude is a positive power of a positive excluded volume. There is no effective
monomer length, however small, that represents `χ ≥ ½`.** Theta is the `K → 0` limit; it is
approached and never reached. So `χ = 0.612` is not a large correction to the free energies `C-0003`
uses — **it has no representation in them at all**, and `interactionRatioFromEffectiveChi` throws
rather than returning a number. Yet Hansen et al. obtained *good, unconstrained, two-parameter* fits
of exactly that form to exactly that geometry, with `a` landing within 2 % and 6 % of the structural
value. **A form that cannot represent a poor solvent fitted poor-solvent data. It did not, because
the data are not poor-solvent data.**

---

## Exposure — propagated, not asserted

`C-0003`, exactly: at fixed height, grafting density and compression ratio, `k ∝ K^(1/(m+1))` and
`N ∝ K^(−1/(m+1))`, so the chain length a specified height demands moves *against* the interaction
and very nearly cancels it. For the des Cloizeaux exponent `1/(m+1) = 4/13 = 0.3077`.

| interaction ratio | stiffness | stroke |
|---|---|---|
| 0.674 (weakest bounded) | **×0.886** (−11.4 %) | ×1.041 (+4.1 %) |
| 1.147 (strongest bounded) | ×1.043 (+4.3 %) | **×0.986** (−1.4 %) |
| 16× (`C-0003`'s own span, for scale) | ×2.347 | ×0.754 |

**`C-0003`'s six-model bracket at the 10 nm design point is 3.83 – 6.01 nm in stroke — ±22 % about
its midpoint. This correction is at most +4.1 %.** It is a fifth of the uncertainty already carried,
and it does not reach any decision boundary.

**Nothing downstream moves:**

- **`C-0003`'s stiffness and stroke** — inside the existing bracket; the claim's stated exposure is
  **discharged** rather than realised.
- **`T-2`'s window** — whose existence at 10 nm `C-0003` shows is decided by the *profile model*,
  not the interaction, with the two interaction laws differing by 1.45× against this correction's
  1.05×. `T-1d` remains the thing that settles it.
- **`C-0004`'s drainage** — 22× of margin, and `τ ∝ L²` with `h` cancelling; a 4 % interaction shift
  is not visible in it.
- **`T-1c`'s free energy** stays inside its own family, because the sign does not flip.

**The `16×` row is the more important one.** It says that even if this task had come back with an
*unbounded* increase in interaction strength, the stroke would have moved 25 %. **The only result
that could have moved a conclusion was a change of sign**, and both the source's own positive
surface pressure and Hansen et al.'s normal-compression fits exclude it.

---

## Validity range

- **The n-cluster many-body attraction is NOT refuted.** It is a real posited mechanism for PEO and
  the Gen-1 layer would feel it too. What is bounded is its **consequence for normal compression of
  a grafted PEG layer**, by measurement. Lee et al.'s observable is a **lateral** surface pressure.
- **The transfer down to the Gen-1 window assumes monotonicity in grafting density.** Hansen et al.
  bound the effect from **above**, at 1.5–2.5× the Gen-1 density. No PEG-brush compression
  measurement *inside* the Gen-1 window was found.
- **The `0.053` is an upper bound in a second sense too.** Attributing the whole `3.56 → 3.30 Å`
  drift to solvent quality is generous: Hansen et al. call the two values *"nearly constant"* and
  treat the difference as fit scatter.
- **The Alexander-de Gennes height relation used to recover `σ` is the one `C-0003` replaced** for
  the Gen-1 layer. It is used here because the question is what *their* fit means, and it means what
  their form says. The reconstruction is therefore internal to their paper and is not a statement
  about the Gen-1 layer's height.
- **Lee et al.'s Supporting Information is unread** (SCF equations S1.1–S1.26, paywalled at ACS).
  The exact relation between that model's `χ` and a Flory-Huggins `χ` cannot be *derived*, only
  bracketed by the two linear transfers, which differ by 0.089. That spread is the answer's own
  uncertainty and is reported, not hidden.
- **Absolute `χ` values inverted out of an Alexander-de Gennes fit inherit its unity-prefactor
  convention**, worth 6.6× in excluded volume (`C-0003`). **Only the ratio is load-bearing**, and
  the ratio is convention-free because both its terms were fitted in the same convention in the same
  paper. Nothing in this claim rests on the absolute number.
- **Medium.** Lee et al. in D₂O, Hansen et al. in water, both salt-free. The Gen-1 buffer moves the
  modulus by < 0.5 % (`C-0007`), so this is discharged.
- **Nothing here is measured about *this* layer.** `PASS` means model-consistent and traceable.

## Numbers that are cited rather than derived

- `χ = 0.789 ± 0.066`, `0.852 ± 0.051`, `χ_θ(model) = 0.696`, `R_G = 25.7 Å`,
  `v_PEO = 59.2 Å³`, `v_water = 29.9 Å³`, `α = 1350`/`2200 Å²/chain`, `DP_n = 113` — **CITED**,
  read directly in the PDF of Lee et al. (2012).
- `a = 3.56 ± 0.07 Å`, `3.30 ± 0.15 Å`, `L₀ = 105`/`109 Å`, `a_bulk = 3.5 Å`, `α = 0.8`, `N = 113` —
  **CITED**, read directly in the PMC full text of Hansen et al. (2003).
- `χ(300 K) = 0.3717` and the water-site convention — **MEASURED**, via `C-0007`.
- `v₀ = 0.06035 nm³` — **DERIVED**, via `C-0002`.
- The stroke log-slope `−0.1019` — **INHERITED from `C-0003`**, defined by its reported
  `5.81 → 4.38 nm` over 16×, and flagged in code as the one number not computed here.

Everything else is derived from those.

## The search, including what was not found

**Found and read on disk:** Lee et al. (2012), complete published PDF, from
`tsapps.nist.gov/publication/get_pdf.cfm?pub_id=910992`; Hansen et al. (2003), complete body, from
`pmc.ncbi.nlm.nih.gov/articles/PMC1302616/`.

**Negatives worth recording, because they are the reason `C-0007` had only the abstract:**

1. **Unpaywall and OpenAlex both say Lee et al. is closed and has no repository copy. Both are
   wrong.** `is_oa: false`, `oa_locations: []`, `has_repository_copy: false`; `oa_status: closed`,
   `best_oa_location: null`. The NIST TSAPPS copy exists and is indexed by neither.
2. **EuropePMC has the record but no full text** — `isOpenAccess: N`, `inPMC: N`, `hasPDF: N`.
3. **Hansen et al. is not in the PMC open-access subset** — `oa.fcgi` answers `idIsNotOpenAccess`
   and the EuropePMC `fullTextXML` endpoint returns an **empty body**, but the PMC article page
   serves the complete text. An empty `fullTextXML` is not evidence that full text is unavailable.
4. **PubMed's own record is `116(24):7367-78`**, not the `116(25):7367` that has been circulating.
5. **Lee et al.'s Supporting Information remains paywalled at ACS** and is not in the NIST PDF.
6. **No PEG-brush compression measurement inside the Gen-1 grafting window** (`σ = 0.018–0.092
   nm⁻²`) was located. Hansen et al.'s own survey of that literature reports that only
   DSPC:PEG-5000 above `f ≈ 0.1` meets the criterion for such an analysis at all — i.e. the
   measurement `P-9` would most like to have may not exist rather than be paywalled.

## Cross-checks passed

1. **Gate 1** — area per chain → `nm⁻²` pinned and squared; `Σ` shown dimensionless and the
   Alexander-de Gennes spacing shown to be a length, both by scaling every length by 1.7.
2. **Gate 2** — unit ratio returns the bulk `χ` exactly under both exponents; **no positive
   interaction ratio reaches `χ = ½`**, over six decades; the inverse map throws for `χ ≥ ½`,
   including for the `0.612` in circulation; monotonicity across a ladder.
3. **Gate 3** — the `χ` ↔ ratio map round-trips to `1e-12`; the stiffness and stroke exposures are
   exactly reciprocal at 2×, 4× and 16×; the volume fraction is recovered by the independent
   conservation route `φ = N σ v₀ / L₀` to `1e-12`.
4. **Gate 4** — 200 bisection steps on the height relation reproduce the closed-form grafting
   spacing to `1e-9`; both amplitude exponents (`15/4`, `3/4`) and the `9/4` in volume fraction are
   obtained as log-slopes of composed functions rather than read off the algebra.
5. **Gate 5** — Lee et al.'s own segment-volume ratio 1.980 against `C-0007`'s independently derived
   2.010 (1.5 %); `C-0003`'s reported `7.58 → 17.79 pN/nm` over 16× reproduced by `16^(4/13)` to
   four figures; the two excluded-volume exponents bracketing the answer to 0.007 in `χ`; and
   Hansen et al.'s brush criterion independently reproducing `CH-0001`'s finding that chain overlap
   does not imply semidilute behaviour.

## Still open — named, not answered

1. **The n-cluster attraction's effect on a *lateral* observable is not bounded**, only its effect
   on normal compression. Nothing in the Gen-1 stack currently loads the layer laterally — but
   `T-12` has just found the tile is laterally unconfined, so this may not stay true.
2. **No compression measurement inside the Gen-1 grafting window.** The bound comes from above and
   assumes monotonicity. The missing measurement is named: **normal-compression osmotic stress or
   SFA on a grafted PEG layer at `σ = 0.02–0.09 nm⁻²`, `N ≈ 100–400`.**
3. **Lee et al.'s SCF free energy is unread.** The two transfers differ by 0.089 and only the
   Supporting Information would collapse them to one.
4. **Hansen et al.'s two fits do carry the sign Lee et al. report** — the denser layer is the poorer
   effective solvent, `Δχ = +0.044` between `σ = 0.140` and `0.229 nm⁻²`, with 1σ bands that only
   just fail to overlap. It is 5× smaller than the claimed effect and inside what its own authors
   call fit scatter, but **it is not zero**, and it is the one piece of evidence pointing the other
   way. If anything reopens `P-9`, it is this.

## Challenges

**Raised by this claim:** [`CH-0012`](../challenges/CH-0012-grafted-chi-number.md) against `C-0007`.

**Standing against this claim:** none. A compression measurement of a PEG brush inside the Gen-1
window landing outside `χ_eff ∈ [0.346, 0.424]` would contradict it and should be raised as a
challenge rather than an overwrite.
