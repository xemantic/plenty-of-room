# P-9 — the effective `χ` of a *grafted* PEG layer

| | |
|---|---|
| **Task** | `P-9` (process blocker, raised by `C-0007` and ranked above `T-2`) |
| **Leaf** | none — premise task under `A2.1`, consumed by `A2.1` (`T-1c`, `T-1d`) and `A2.2` (`T-3`) |
| **Verification type** | logical + in-silico, closed against two published measurements |
| **Blocks** | every osmotic number in the programme — `C-0001`, `C-0002`, `C-0003`, `C-0004` all apply a **bulk-solution** property to a **brush** |
| **Raised by** | [`C-0007`](../claims/C-0007-solvent-quality-vs-salt.md), still-open item 3: *"`χ(brush) ≈ 0.60` against `χ(bulk) = 0.372` — 239× the entire buffer effect and not incorporated. **This is the largest open premise in the material sheet.**"* |

---

## Formulate

### The question

`C-0007` closed the salt question and, in closing it, opened a larger one.
The buffer moves the layer's osmotic modulus by less than 0.5 %.
But the `χ` that bound is computed *from* is a **bulk-solution** measurement,
and `C-0007` found a report that a grafted PEO layer sits at an effective `χ ≈ 0.60` —
above θ, formally poor solvent, **negative excluded volume** — against `0.372` in bulk.

`Δχ = 0.23` is **239× the entire salt effect `P-6` was chartered to bound**.
If it transfers, `C-0002`'s equation of state is the wrong input to `C-0003`,
`T-1c`'s free energy is outside its own family (a net attractive interaction is not in it),
and `T-2`'s window rests on a property of a different system.

So the question is not "what is the grafted `χ`". It is:

> **Does the reported grafted-layer `χ` transfer to the Gen-1 layer at all —
> and if it does not, is the effect it points at nonetheless bounded by something we can measure against?**

### Numeric target and acceptance predicate

Falsifiable, and it must resolve to exactly one of three:

- **(a) INAPPLICABLE** — the result does not transfer to the Gen-1 layer (wrong geometry, wrong
  grafting-density regime, or an effective parameter of a different model), **with the reason
  established from the body of the source rather than asserted**. `P-9` closes and `C-0002`'s
  equation of state stands.
- **(b) APPLICABLE AND BOUNDED** — a grafting-density-dependent correction to the interaction
  strength, with `C-0003`'s `k ∝ K^(1/(m+1))` exposure propagated into the stiffness and the
  stroke, and a statement of whether any `T-1c`/`T-2` conclusion moves.
- **(c) APPLICABLE AND UNBOUNDED with available methods** — stated plainly per §7, with the
  missing measurement named, and `C-0002`'s equation of state re-qualified as **bulk only**
  wherever it is consumed.

Additionally, in every branch:

- **(d)** The four checks the task was raised with are answered *individually*, and an answer of
  "this check does **not** close the task" is reported as such rather than quietly dropped:
  1. solid-grafted brush or air-water-interface monolayer?
  2. grafting densities in `nm⁻²` against the Gen-1 window (`σ ≈ 0.018–0.092 nm⁻²`, `N ≈ 60–375`)?
  3. what is `χ` fitted *against* — which model, which lattice convention, which reference state?
  4. is `χ(brush)/χ(θ) ≈ 1.2` even a solvent-quality statement, given that the same source
     reports the layer still exerting *positive* surface pressure?
- **(e)** The **cheap bound runs first**, per §5. This project already holds a PEG-brush
  compression dataset (Hansen et al. 2003, cited by `C-0002` for `a = 0.356 ± 0.07` and
  `0.330 ± 0.15 nm`). If a brush's interaction were genuinely `Δχ = 0.23` weaker than bulk,
  would those fits have come out where they did? That question is answerable from material the
  project already has, and it is answered before anything else.
- **(f)** Any contradiction of `C-0007` is filed as a **challenge**, never as an edit.
- **(g)** Anything that cannot be pinned is named as such and left open.

**Falsification of the task itself, stated in advance.**

- If the source's `χ` turns out to be a Flory-Huggins `χ` on a stated lattice, fitted to a
  solid-grafted brush under **normal** compression inside the Gen-1 window, then the number
  transfers, the answer is (b) or (c), and `C-0002`'s equation of state must be re-qualified.
- If the Hansen et al. compression fits, inverted onto the same convention, come out at
  `χ_eff ≥ 0.5`, then the bulk equation of state is contradicted by measurement in the *right*
  geometry and this task has found a much bigger problem than it was raised to find.
- If the two independent brush measurements **disagree with each other** by more than either
  disagrees with bulk, then neither bounds anything and the answer is (c).
- If the grafting densities of the source turn out to sit **inside** the Gen-1 window, the
  easy dismissal is unavailable and the task must be closed on the parameter itself or not at
  all. *(This one fired. See Execute.)*

### Units, temperature, medium

Locked, per `Physics.kt`: lengths in **nm** on export and in **Å** where a source reports Å
(both fits do); grafting density `nm⁻²`; area per chain `Å²/chain`; volumes `nm³`;
pressure `pN/nm²` = MPa exactly; `k_BT = 4.142 pN·nm` at **300 K**; aqueous.
`χ`, excluded-volume ratios and interaction ratios are dimensionless.

### Conventions fixed before deriving

`P-3` had to disarm three meanings of `a`; `P-6` had to disarm the lattice site of `χ`.
This task has to disarm three more, and each one is capable of producing the answer on its own.

| trap | statement |
|---|---|
| **A model's `χ` has a theta point, and it is not always ½** | `χ = ½` is the theta condition **of Flory-Huggins theory**. A self-consistent field model with unequal segment volumes has its own theta point, and comparing its fitted `χ` against ½ is a category error. |
| **There are two linear transfers between `χ` scales and they disagree** | preserve the *ratio* to theta, or preserve the *distance* past theta. Both are defensible a priori; the gap between them is the size of the non-transferability, and it is reported as a number. |
| **A lateral surface pressure is not a normal disjoining pressure** | a Langmuir trough measures `Π` against area *per chain*, in the plane. The Gen-1 layer is compressed **normally** against a rigid tile. They are different observables of the same layer. |
| **Only ratios are convention-free** | `C-0003` records that the Alexander-de Gennes unity prefactor is worth **6.6×** in excluded volume. An absolute `χ` inverted out of an AdG fit inherits that; the **ratio** of a brush fit to a bulk fit *in the same paper, in the same convention* does not. Only the ratio is load-bearing here. |
| **sign** | positive `Δχ` = poorer solvent. Positive interaction ratio = stronger repulsion. Poorer solvent ⇒ weaker interaction ⇒ **softer** layer and **longer** stroke, because `C-0003`'s `N(L₀)` moves against it. |

---

## Plan

### Method, and why this one

**Read the bodies. Then run the cheap bound. No simulation.**

1. **The cheap first step is a literature retrieval, not a calculation.** `C-0007` used Lee et al.
   *from its abstract alone*, and three of the four checks above are answerable only from the
   body. Retrieval is minutes; being wrong about this premise is the whole material sheet.
2. **Then the cheap bound, from material the project already holds.** Hansen et al. (2003) fitted
   the Alexander-de Gennes compression law to *measured* osmotic-stress isotherms of PEG-grafted
   bilayers, holding the des Cloizeaux amplitude at the value they had fitted to **bulk**
   osmometry **in the same paper**, and letting the effective monomer length float. That is an
   excluded-volume comparison of a grafted layer against bulk, inside one convention and one
   dataset family — which is exactly the object `P-9` needs and which no amount of modelling
   would improve on.
3. **No MD and no SCF.** The `CLAUDE.md` research practice says it directly and `P-6` said it
   before: for solvent quality of PEG, published measurement beats simulation, and the expensive
   method would be *less* trustworthy, not merely more expensive. Here it is stronger still —
   the quantity in dispute **is itself an SCF fit**, so answering an SCF fit with another SCF fit
   would compare two models to each other and never touch a measurement. A numerical SCF profile
   is queued as `T-1d` for a different question (the *profile*, not the interaction) and is
   costed there.
4. **Propagate through `C-0003`'s exact exponents, not through a new model.** `k ∝ K^(1/(m+1))`
   and `N ∝ K^(−1/(m+1))` are exact and already verified to 15 significant figures. Whatever
   bound comes out of step 2 goes through them unchanged.

### What would falsify this approach

Stated in advance:

- **The Hansen fits do not constrain solvent quality.** If the effective monomer length entered
  only the *shape* of the isotherm and not its *amplitude*, the fit would say nothing about the
  interaction strength and the cheap bound would be empty. (It does not: with `D` eliminated by
  the height relation, `a` enters the pressure scale as `a^(−15/2)` at fixed `L₀`, and as
  `a^(15/4)` at fixed physical monomer density. It is *only* an amplitude parameter.)
- **The two sources measure the same thing and disagree.** Then neither bounds anything.
- **The grafted `χ` is a Flory-Huggins `χ` after all.** Then the transfer is licensed and the
  answer is not (a).
- **The Hansen inversion lands above ½.** Then bulk osmometry is contradicted in the right
  geometry, and this task has found the larger problem rather than closing the smaller one.

### Cost

Minutes of compute. The expense is retrieval and reading — one paywalled body that turned out to
be free from a source neither Unpaywall nor OpenAlex indexes, and one that is free from PMC but
not in the open-access subset. That is where a premise task's budget belongs.

---

## Execute

### Both bodies obtained

| source | route | what it cost |
|---|---|---|
| **Lee et al. (2012)**, *J. Phys. Chem. B* **116**:7367–7378, doi:10.1021/jp301817e | **`tsapps.nist.gov/publication/get_pdf.cfm?pub_id=910992`** — NIST's own repository, because Akgun and Satija are NIST NCNR staff | `pdftotext -layout`, 805 lines. Unpaywall says `is_oa: false`, OpenAlex says `oa_status: closed`; **both are wrong**. ACS refuses. |
| **Hansen et al. (2003)**, *Biophys. J.* **84**:350–355, PMC1302616 | `pmc.ncbi.nlm.nih.gov/articles/PMC1302616/` | the EuropePMC `fullTextXML` endpoint returns **empty** and `oa.fcgi` answers `idIsNotOpenAccess`, but the PMC article page serves the complete body. Inline equations are images and did not survive extraction; the Appendix carries them in prose. |

The **Supporting Information** of Lee et al. (SCF equations S1.1–S1.26, Figures S1–S5) is *not* in
the NIST PDF and remains paywalled. Its absence is carried as an open item, not papered over.

### Code

`src/main/kotlin/material/GraftedChi.kt`, additive to the `material` package
(`PegWater.kt`, `OsmoticEquationOfState.kt` and `SolventQuality.kt` are untouched).
Entry point `material.GraftedChiStudyKt`, emitting `gpd/results/P-9-grafted-chi.json`.
Tests in `src/test/kotlin/material/GraftedChiTest.kt`, **written first** — 25 tests.

The package makes the central distinction *structural*: `ScfBrushChiFit` carries a model's own
theta point as a required constructor parameter, so a caller cannot compare its `χ` against ½ by
accident; `AlexanderDeGennesBrushFit` carries the bulk `a` its own paper fitted, so the ratio is
always taken inside one convention.

Results are carried in [`C-0013`](../claims/C-0013-grafted-chi-inapplicable.md). The short version:

| quantity | value |
|---|---|
| Lee et al.'s fitted `χ` | **0.789 ± 0.066** and **0.852 ± 0.051** — *not* 0.60, which appears nowhere |
| the theta point **of that model** | **0.696**, located by the authors, *not* ½ |
| geometry | **air/D₂O Langmuir monolayer**, PEO-PnBA diblock, indifferent grafting plane |
| its grafting densities | **0.0455 and 0.0741 nm⁻²** — **inside** the Gen-1 window |
| its reduced grafting density `Σ` | **0.94 and 1.54** — at coil overlap, *not* dense |
| the two transfers onto the Flory-Huggins axis | **0.567–0.612** (ratio) vs **0.593–0.656** (offset) |
| spread between them | **0.089**, i.e. **37 %** of the shift the ratio transfer claims |
| Hansen et al.'s grafting densities | **0.140 and 0.229 nm⁻²**, `φ = 0.091` and `0.143`, `N = 113`, `L₀ = 10.5`–`10.9 nm` |
| **their brush/bulk interaction ratio** | **0.674 – 1.147** over both coverages and both 1σ bands |
| **their effective `χ`** | **0.346 – 0.424** against a bulk **0.372** |
| **`|Δχ|` bounded by measurement in the right geometry** | **≤ 0.053**, against the **0.240** claimed |
| exposure in stiffness | **−11.4 % to +4.3 %** |
| exposure in stroke | **−1.4 % to +4.1 %** |

### The falsifier that fired

**The grafting-density escape route is not available.** Lee et al.'s title says *"densely grafted"*,
but both of its `χ` conditions sit **inside** the Gen-1 window and its chain length of 113 monomers
sits inside the Gen-1 60–375. `P-9` had to be closed on the parameter itself, which is harder and
is what makes the answer worth having.

---

## Verify — the five gates

### 1. Dimensional consistency — **PASS**

Area per chain → grafting density is the only unit conversion, and it is squared: `Å²/chain` →
`nm⁻²` carries a factor of 100, and the grafting spacing carries it cubed into the pressure scale.
Checked as an experiment rather than on paper: `Σ = σ π R_G²` is asserted invariant to `1e-12`
under scaling every length by 1.7, and the Alexander-de Gennes spacing
`D = (N a^(5/3)/L₀)^(3/2)` is asserted to scale by exactly that factor under the same operation.

### 2. Limiting cases — **PASS**, and this gate carries the decisive result

- An interaction ratio of exactly 1 returns exactly the bulk `χ`, under **both** excluded-volume
  exponents, to `1e-12`.
- **No positive interaction ratio, however small, produces `χ ≥ ½`.** A des Cloizeaux amplitude is
  a positive power of a positive excluded volume; theta is the `K → 0` limit and is approached,
  never reached. Asserted over six decades of ratio.
- The inverse map **throws** for `χ ≥ ½`, and the test asserts that it throws for the `0.612` the
  ratio transfer produces. **That is the executable form of the finding**: the value in
  circulation for a grafted layer has *no representation* as an Alexander-de Gennes or des
  Cloizeaux interaction at all. It is not a large correction to `C-0003`'s free energy; it is
  outside the family.
- Monotonicity: a weaker interaction is always a poorer effective solvent, asserted across a ladder.
- Below a ratio of ~`1e-12` the difference from ½ stops being representable in a double and rounds
  onto it. Asserted with `<=` and a comment rather than a strict inequality — `CLAUDE.md` warns
  against asserting exact equilibrium values in floating point, and this is the same hazard.

### 3. Symmetry and conservation — **PASS**

- The `χ` ↔ interaction-ratio map round-trips exactly, to `1e-12`, at five ratios.
- The stiffness and stroke exposures are pure power laws, so halving and doubling the interaction
  give **exactly reciprocal** factors — asserted at 2×, 4× and 16× to `1e-12`.
- The volume fraction is recovered by a second, independent route: `φ = N σ v₀ / L₀` is the
  conservation statement that the polymer volume per unit area is fixed by the grafting, and it
  agrees with `n v₀` to `1e-12`. This is the check that would catch a slipped factor of ten
  between ångströms and nanometres, which is the only real hazard in the reconstruction.

### 4. Numerical convergence — **PASS**

The grafting spacing is closed form here, but the relation it inverts is the one `C-0003`
*replaced*, so it is not trusted: 200 bisection steps on `L₀ = N a^(5/3) D^(−2/3)` reproduce the
closed form to `1e-9`. The two amplitude exponents are obtained as **log-slopes of the composed
functions** rather than read off the algebra — `15/4` in the effective monomer length at fixed
physical density, and `3/4` in the excluded volume from the space-filling self-avoiding blob,
both to `1e-9` or better, with the `9/4` in volume fraction falling out of the same construction.

### 5. Literature cross-check — **PASS**, on four independent legs

1. **Lee et al.'s SCF carries the same unequal-site trap `C-0007` names, and to 1.5 %.** Their
   model uses `v_PEO = 59.2 Å³` and `v_water = 29.9 Å³`, a ratio of **1.980**; `C-0007` derives
   **2.010** for the same ratio from PEG's partial specific volume and the mass density of water,
   without reference to that paper. Two independent routes to the factor that displaces a lattice
   `χ`. This is what establishes the `0.696` as a **convention offset** rather than a fit artefact.
2. **`C-0003`'s 16-fold sensitivity study is reproduced to four figures from a different
   direction.** `C-0003` reports `k(0.8L₀)` moving from **7.58 to 17.79 pN/nm** over a 16× change
   in interaction strength — a ratio of 2.34697. The exponent `1/(m+1) = 4/13` carried here gives
   `16^(4/13) = 2.34692`. Two numbers this task did not fit, reproducing the exposure exponent.
3. **The two excluded-volume transfer exponents bracket the answer and it does not move.**
   `3/4` (blob) and `1` (mean field) give `χ_eff = 0.360`/`0.363` and `0.404`/`0.397` at the two
   coverages — the conclusion is insensitive to which is right, which `C-0007` established is the
   correct way to carry this transfer.
4. **The premise of the invoked law is checked against the actual material, and it is the premise
   Hansen et al. themselves impose.** Their whole paper exists to establish when an
   Alexander-de Gennes fit is *entitled* to be made: the layer must be in the bulk des Cloizeaux
   semidilute regime, which the chain-overlap criterion does **not** guarantee — the same
   statement `CH-0001` reached independently for this project. They discard PEG-2000 and all
   coverages below `f = 0.1` on exactly that ground, and the two fits used here are the only ones
   that survive it.

---

## What could not be verified, and is therefore not claimed

- **The functional form of Lee et al.'s SCF free energy is unread.** It is in Supporting
  Information behind the ACS paywall. The model's theta point is taken from the main text, where
  it is stated as a number and as a calibration procedure; the exact relation between that `χ` and
  a Flory-Huggins `χ` therefore cannot be *derived*, only bracketed by the two linear transfers —
  which differ by 0.089. That spread is reported as the answer's own uncertainty, not hidden.
- **The n-cluster many-body attraction is not refuted.** What is bounded is its consequence for
  **normal** compression of a grafted PEG layer. Lee et al.'s observable is a **lateral** surface
  pressure, and the two are not the same measurement.
- **No PEG-brush compression measurement inside the Gen-1 grafting window was found.** Hansen et
  al. bound it from **above**, at 1.5–2.5× the density, and the transfer down to the Gen-1 window
  assumes any density-driven effect is **monotone** in grafting density. Stated as an assumption.
- **Hansen et al.'s two fitted monomer lengths do show the sign Lee et al. report.** The denser
  layer is the poorer effective solvent, by 0.044 in `χ`, and the two 1σ bands only just fail to
  overlap. Hansen et al. call the values *"nearly constant"* and treat the difference as fit
  scatter. Attributing all of it to solvent quality is therefore an **upper** bound, and it is
  reported rather than suppressed because it is the one piece of evidence that runs the other way.
