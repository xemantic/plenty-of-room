# T-134 — the fabrication-tolerance literature survey

The evidence behind [`C-0072`](../claims/C-0072-plan-tolerance-model.md).
**77 query strings** across EuropePMC, arXiv and Crossref, in nine named families.

Every number below carries a **read flag**, per `CLAUDE.md`'s research practice:

- **READ DIRECTLY** — the PDF or full text was fetched and the passage read; the sentence is quoted verbatim.
- **ABSTRACT ONLY** — verbatim from a Crossref or EuropePMC abstract, and labelled as such.
- **NOT FOUND** — searched for and not found. The query strings are recorded so the negative is falsifiable by one paper.
- **DERIVED** — this project's arithmetic on a read value, and marked so it is never mistaken for a published one.

**Nothing here comes from a search-engine summary.**

---

## The headline

**A measured fabrication-tolerance distribution for a DNA-origami lattice exists, but it has never been published as one, and no paper decomposes it.**
Three real measurements bear on it, all of them in supplementary material their own main texts do not quote.
The single-layer sheet — the object this programme builds on — is the **worst-ordered** origami anyone has measured.

---

## Quantity 1 — the interhelical distance width

### Fischer, Hartl, Frank, Rädler, Liedl, Nickel, *Nano Lett.* **16**:4282 (2016)

**READ DIRECTLY** — PMC author manuscript `PMC6544510` plus the SI PDF `EMS82749-supplement-SI.pdf`.
EuropePMC's `fullTextXML` returns **zero bytes** for this article and NCBI's OA service reports it *"not Open Access"*; **both are false negatives** and the PMC article page serves it.
(PMC now gates file downloads behind a SHA-256 proof-of-work cookie; solving it is ~10 ms.)

This is the paper the 2.69 / 2.73 / 2.54 nm interhelical distances come from.
**It measures the width as well as the mean, reports the width only in the SI, and never discusses it.**

**(i) The Bragg peak's Lorentzian half-width.** SI p. S7, verbatim:

> *"q0 is the peak center and B is the HWHM (half-width half-maximum)."*

| object | `B` [Å⁻¹] | `q₀` [Å⁻¹] | `B/q₀` **(DERIVED)** |
|---|---|---|---|
| **sheet (single-layer)** | **0.0354 ± 3.8e−3** | 0.23343 ± 1.7e−3 | **15.2 %** |
| brick (square, 3 × 14) | 0.01005 ± 2.4e−4 | 0.23003 ± 1.4e−4 | 4.4 % |
| 24HB (honeycomb) | 0.01954 ± 3.9e−4 | 0.16519 ± 2.2e−4 | 11.8 % |

`B` is a **peak** width, so it contains finite-size broadening as well as disorder: `B/q₀` is a rigorous **upper bound** on the lattice-constant width.

**(ii) An explicit lattice mean and width.** SI Tables S5 and S7 are headed verbatim
`Scale | a_mean [Å] | w_a [Å] | Radius cylinder [Å] | ...` for the *"Small rigid cylinder in lattice"* model:

| object | `a_mean` [Å] | `w_a` [Å] | `w_a/a` **(DERIVED)** |
|---|---|---|---|
| **sheet (single-layer)** | **27.41** | **2.5** | **9.1 %** |
| brick (square) | 27.27 | 0.8 | 2.9 % |
| 24HB (honeycomb) | 25.1 | 1.7 | 6.8 % |

> **Flagged honestly: `w_a` is never defined in words.** The string appears only in those two table headers. It is almost certainly a Gaussian width on the lattice constant (the model is a SasView-style rigid-cylinder-on-a-lattice fit), but the paper does not say so — which is why `B/q₀` is carried beside it as the rigorously defined companion.

**A consistency check that nothing forces (DERIVED).** The ratio `w_a / HWHM_a` is 0.61, 0.67 and 0.57 across the three objects and two lattices — two entirely different width parameters tracking each other at a constant ratio, which is what a single underlying lattice disorder would produce.

**A Scherrer estimate (DERIVED).** The sheet's peak implies ~3.3 coherent lattice planes out of 24 designed helices, so its width is **disorder-limited**; the brick's ~11.4 of 14 is roughly size-limited. **No paracrystalline (size-versus-strain) decomposition exists** — see the negative queries below.

### Bai, Martin, Scheres, Dietz, *PNAS* **109**:20012 (2012)

**READ DIRECTLY** — `PMC3523823` article page and SI PDF.

**The only Debye-Waller-type disorder measurement on a DNA origami.** SI Text, verbatim:

> *"a B-factor sharpening of approximately −1,000 Å² gave a density map at the core of the object … we estimated the errors in the rotational assignments to be on the order of 2°. … this estimate results in a B-factor component of Brot = (ΔΘ D)²/2200 = ∼400 Å². … errors in the translational assignments to be on the order of ∼0.5 pixel, or ∼1.8 Å, leading to a B-factor component of Btrans = 8 π² (rmsd)² = 250 Å². That then leaves a remaining B-factor component for the intrinsic structural variability (at the core of the object) of **Bstructural = 1,000 − 400 − 250 = 350 Å², which corresponds to an rmsd in the atom positions of ∼2 Å.** … Additionally, as mentioned in the main text, the structural variability is larger at the periphery of the object than at its core."*

Main text, verbatim: *"the rmsd of the atoms at the core of the object was estimated to be in the range of 2–3 Å"*. Map resolution *"varied from 9.7 Å at the core to ∼14 Å at the periphery."*

### **The framing result** — the interhelical distance is a sawtooth, not a mean

Bai 2012, main text, Fig. 3 E/F caption, **READ DIRECTLY**, verbatim:

> *"The midpoints of neighboring dsDNA helices move on average from a minimum distance ⟨d min⟩ = 18.5 Å at the cross-over to a maximum distance of ⟨d max⟩ = 36 Å away from each other."*

Confirmed twice independently:

- **Yoo & Aksimentiev, *PNAS* **110**:20099 (2013)**, all-atom MD, **READ DIRECTLY** (`PMC3864285`): *"the DNA–DNA distance was found to range between 18 and 30 Å"*; *"the average separation of ∼24 Å"*; *"The mean interhelical distance at the junction (⟨d⟩) is 18.6 Å, consistent with the recent cryo-EM data"*; and for the thermal part, *"With the exception of the terminal array cells, the rmsf values were less than 4 Å."*
- **Snodin, Romano, Rovigatti, Ouldridge, Louis, Doye, *NAR* **47**:1585 (2019)**, oxDNA on a **2D tile**, **READ DIRECTLY** (`PMC6379721`). Fig. 3 caption verbatim: *"The weave pattern for the 2D tile … quantified by (A) the inter-helix distance and (B) **the standard deviation in this distance** as a function of base-pair index"*. Text verbatim: *"the fluctuations, which are smallest at the junctions and largest at the midpoints between the junctions, **are significantly smaller in magnitude than the variation in the interhelical distance due to the weave pattern itself**"*, with *"1.5 nm as a typical value of the difference in the interhelix distance between the maxima and minima."*

**So `2.69 nm` is a Bragg lattice constant and not a local centre-to-centre distance**, and for a single-layer sheet the dominant spread is a *deterministic* ±7.5 Å sawtooth with a smaller fluctuation about it.

### Kube et al., *Nat. Commun.* **11**:6229 (2020)

**READ DIRECTLY** (EuropePMC `fullTextXML`). The single-layer negative, verbatim:

> *"We also attempted to solve structures of variants of single-layer DNA origami tiles in square-lattice design (Rothemund Rectangle 1), but **were unsuccessful due to excessive conformational heterogeneity** (Supplementary Fig. S27)."*

and the mode, verbatim: *"DNA origami are not rigid objects, instead, they display substantial structural heterogeneity … relative domain motions similar to those seen in proteins and as **helical lattice breathing**, which does not exist in proteins."* No numeric breathing amplitude is given. Design-vs-model deviations *"were all >9.6 Å (RMSD)"*.

### Rothemund, *Nature* **440**:297 (2006)

**READ DIRECTLY** (PDF, dna.caltech.edu). The founding paper's own tolerance statement, verbatim:

> *"A range of aspect ratios implied **a gap size from 0.9 to 1.2 nm**; later designs assume 1 nm. Whatever the exact value, it is consistent"*

and *"data are consistent with an inter-helix gap of 1 nm for 1.5-turn spacing and 1.5 nm for 2.5-turn spacing"* — **the gap is a 50 % function of the crossover spacing**, i.e. a design variable rather than a constant.

### Baker, Tuckwell, Berengut et al., *ACS Nano* **12**:5791 (2018) — **NOT READ**

Blocked at the publisher, at Edinburgh Research Explorer (403 on the PDF via `curl` *and* WebFetch) and at Oxford ORA (403), despite being listed green OA. **ABSTRACT ONLY**: *"…we use SAXS to quantify the magnitude of global twist of DNA origami tiles with different crossover periodicities: these measurements highlight the extreme structural sensitivity of single-layer origami to the location of strand crossovers."* No width in the abstract. **Corroborated second-hand and read directly in Snodin 2019**, which cites it: *"Although SAXS experiments have been performed on this origami design, **the flexibility of the 2D origami meant that any features were too broad to back out an interhelix distance**"*.

---

## Quantity 2 — the base-pair rise and twist width

**Origami-specific per-step rise/twist scatter: NOT FOUND.** Generic B-DNA:

### Olson, Gorin, Lu, Hock, Zhurkin, *PNAS* **95**:11163 (1998)

**READ DIRECTLY** (`PMC21613`, Table 1, p. 11164). Heading verbatim: *"Average values and dispersion of base pair step parameters in DNA crystal complexes"*, footnote *"Dispersion noted in parentheses"*.

| sample | N | twist [°] | rise [Å] |
|---|---|---|---|
| **B-DNA** | 724 | **35.4 (6.3)** | **3.32 (0.19)** |
| P·DNA (protein-bound, culled) | 1 840 | 34.2 (5.5) | 3.36 (0.25) |

**DERIVED**: rise σ/mean = **5.7 %** (B-DNA) to 7.4 %; twist σ/mean = 16–18 %.
These are crystal ensembles including sequence and packing, **not** pure thermal fluctuation, so they are an **upper** bound on the per-step width.

### Lankaš, Šponer, Langowski, Cheatham, *Biophys. J.* **85**:2872 (2003)

**READ DIRECTLY** (`PMC1303568`, Table 1). MD diagonal force constants; **DERIVED** at `kT = 0.5925 kcal/mol`: conditional `σ_twist` = 3.5–5.3°, `σ_rise` = 0.24–0.37 Å. The caption states these are *conditional* (one parameter varied, others at equilibrium), so the **marginal** σ is larger. An incidental read value: *"the differences in standard deviations were ~3 % (0.01 Å) in rise"*, implying `σ_rise ≈ 0.33 Å` in their trajectories.

### Dietz, Douglas, Shih, *Science* **325**:725 (2009)

**READ DIRECTLY** (`PMC2737683`). Origami-level *global* twist scatter, Fig. 2G caption verbatim:

> *"Left-handed and right-handed ribbons undergo **half-turns every 235±(32 s.d.) nm (N=62) and 286±(48 s.d.) nm (N=197)**, respectively."*

**DERIVED**: 13.6 % and 16.8 % relative population scatter.

---

## Quantity 3 — AFM and population dimensional scatter

### **The load-bearing result** — Dietz, Douglas, Shih, *Science* **325**:725 (2009)

**READ DIRECTLY.** Fig. 3K caption, verbatim:

> *"Average bend angles were determined to be **0°±(3° s.d.) (N=74); 30.7°±(5.4° s.d.) (N=212); 62.4°±(5.9° s.d.) (N=208); 91.3°±(5.2° s.d.) (N=206); 121°±(8.4° s.d.) (N=212); 143.4°±(9° s.d.) (N=131); 166°±(9° s.d.) (N=106)**."*

and the decomposition, main text, verbatim:

> *"The distributions each have a half-width at half maximum of 5° to 9°. **Our toy model predicts thermally induced angular fluctuations with a standard deviation from the mean bend angle of about 2.5°** (see Note S2 and Fig. S1). **The discrepancy between expected and observed distribution widths may be due to defects.** … **A future challenge will be to improve folding quality such that thermal fluctuations alone determine the angular precision of any produced shape.**"*

**This is the only place in the accessible literature where a measured origami shape scatter is explicitly split into a thermal part and a fabrication part.**
**DERIVED**: observed/thermal = **2.1× to 3.6× in amplitude** (3.4–12× in variance), the excess attributed by the authors to defects.

### Rothemund 2006 — **READ DIRECTLY**

> *"Distances measured between pairs of '1' pixels in alternating columns (two pixel widths: **11.5 ± 0.9 nm, mean ± s.d., n = 26**) and adjacent rows (one pixel height: **6.6 ± 0.5 nm, n = 24**)"*

**DERIVED**: 7.8 % and 7.6 % relative s.d. — an **upper** bound (AFM drift and tip convolution included; the means are themselves 6–10 % above design).

### Hartl, Frank, Amenitsch, Fischer, Liedl, Nickel, *Nano Lett.* **18**:2609 (2018) — **READ DIRECTLY** (`PMC6544511`)

> *"The average center-to-center distance for the lateral (AB) configuration is **21 with a standard deviation of 4 nm**, which is in good agreement with the designed value of 21.4 nm."*

**DERIVED**: 19 % relative — but TEM on dried samples, including the gold-nanoparticle linker's own flexibility, and the authors say so.

---

## Quantity 4 — staple incorporation and attachment-stiffness scatter

### Strauss, Schueder, Haas, Nickels, Jungmann, *Nat. Commun.* **9**:1600 (2018)

**READ DIRECTLY** (EuropePMC `fullTextXML`, `PMC5913233`). **A complete per-site map over all 168 staples of a Rothemund rectangle.**

Abstract, verbatim:

> *"We find that strand incorporation strongly correlates with the position in the structure, **ranging from a minimum of 48 % on the edges to a maximum of 95 % in the center.**"*

Results, verbatim:

> *"this translates to **absolute incorporation efficiencies of 48–95 % with an average of 84 %**, in good agreement with qualitative results of relative staple abundance from next-generation sequencing."*

Corroborated by **Rothemund 2006, read directly**: *"**94 % of '1' pixels (of 1,080 observed) were visualized.**"*

### The stiffness scatter of nominally identical staple attachments — **NOT FOUND**

Nine queries across three databases (family G below) found nothing. The nearest published work measures a **different** quantity:

**Videbæk, Hayakawa, Hagan, Grason, Fraden, Rogers, *PNAS* **122**:e2500716122 (2025) — READ DIRECTLY** (`PMC12452858`). Cryo-EM multi-body refinement of origami *dimers*: *"we compute the effective moduli of these modes, finding **k x\* = 0.53 kBT/nm² and B\* = 17.5 kBT/rad²**"*; *"the fluctuations are normally distributed"*; *"the single-stranded portion of the DNA handles is the most compliant element in bound pairs."* **DERIVED**: `σ_x = 1.37 nm`, `σ_θ = 13.7°`. This is the **inter-subunit ssDNA handle**, not an intra-tile attachment — but it proves the instrument and method exist.

---

## The query strings — 77, so the negatives are falsifiable

EuropePMC via `…/webservices/rest/search?query=…&format=json&pageSize=20&resultType=core`, **8 s sleep between queries**, `--retry 4 --retry-all-errors`. Hit counts in parentheses.

**A — origami SAXS / interhelical lattice (7)**
`DOI:"10.1021/acs.nanolett.6b01335" OR DOI:"10.1021/acsnano.8b01669"` (2) ·
`"DNA origami" AND "small-angle X-ray scattering" AND lattice` (104) ·
`"DNA origami" AND "interhelical distance"` (15) ·
`"DNA origami" AND "inter-helical" AND (distribution OR variance OR "standard deviation")` (23) ·
`"DNA origami" AND SAXS AND "Lorentzian" AND peak` (8) ·
`"DNA origami" AND "correlation length" AND SAXS` (12) ·
`"square lattice" AND "DNA origami" AND "2.7 nm" OR "2.69 nm"` (175)

**B — disorder-parameter terminology (8) — the decisive negative**
`"DNA origami" AND (paracrystalline OR "paracrystal")` (**0**) ·
`"DNA origami" AND "Debye-Waller"` (**0**) ·
`"DNA nanostructure" AND "Debye-Waller"` (**0**) ·
`"DNA origami" AND "superlattice" AND ("Debye-Waller" OR "paracrystalline" OR "disorder parameter")` (**0**) ·
`"DNA" AND "Bragg peak" AND "peak width" AND "lattice distortion" AND (bundle OR condensate OR assembly)` (**0**) ·
`"DNA origami" AND "lattice disorder"` (1, HIV capsid) ·
`"paracrystalline disorder" AND DNA` (3, irrelevant) ·
`"Debye-Waller factor" AND DNA AND (lattice OR bundle OR assembly)` (118, all DNA–*nanoparticle* superlattices)

**C — cryo-EM structure and B-factor (10)**
`"DNA origami" AND "cryo-EM" AND (resolution OR "B-factor")` (286) ·
`"DNA origami" AND "cryo-electron microscopy" AND structure AND helix AND deviation` (45) ·
`AUTH:"Bai XC" AND "DNA origami"` (3) ·
`"Cryo-EM structure of a 3D DNA-origami object"` (1) ·
`"DNA origami" AND "local resolution"` (18) ·
`Kube AND "DNA origami" AND "cryo-EM" AND "molecular dynamics"` (4) ·
`"DNA origami" AND cryo-EM AND "B-factor" AND "atomic model" AND deviation` (2) ·
`"DNA origami" AND "root-mean-square deviation" AND "cryo-EM" AND helix position` (12) ·
`"DNA origami" AND "thermal fluctuation" AND "RMSF" AND "measured" AND cryo-EM` (3) ·
`"DNA origami" AND "multi-body refinement" AND mechanics` (1)

**D — twist, rise and base-step (9)**
`"Folding DNA into twisted and curved nanoscale shapes"` (3) ·
`"DNA origami" AND "global twist" AND "base pairs per turn"` (5) ·
`"Dimensions and Global Twist of Single-Layer DNA Origami Measured by Small-Angle X-ray Scattering"` (1) ·
`TITLE:"Dimensions and Global Twist of Single-Layer DNA Origami"` (1) ·
`"DNA origami" AND twist AND "10.67"` (24) ·
`"base-pair step" AND (twist OR rise) AND "standard deviation" AND (crystallograph* OR "molecular dynamics")` (120) ·
`Olson AND "DNA sequence-dependent deformability"` (10) ·
`Lankas AND "DNA basepair step deformability"` (1) ·
`"helical rise" AND "standard deviation" AND B-DNA AND fluctuation` (15)

**E — AFM and population dimensions (5)**
`"DNA origami" AND AFM AND height AND ("standard deviation" OR distribution) AND nm` (361) ·
`"DNA origami" AND "structural heterogeneity" AND (AFM OR "atomic force microscopy")` (28) ·
`"DNA origami" AND AFM AND "apparent height"` (9) ·
`"DNA origami" AND "atomic force microscopy" AND "height" AND "1.5 nm" OR "2 nm" AND rectangle` (34) ·
`"long-term ambient storage" AND "DNA origami" AND morphology` (1)

**F — staple incorporation and defectivity (7)**
`"staple strand" AND incorporation AND yield AND "DNA origami"` (61) ·
`"DNA origami" AND "missing staple" OR "staple incorporation"` (5535, `OR` too loose) ·
`"DNA origami" AND "defect" AND "single-molecule" AND yield AND quantif*` (59) ·
`"DNA-PAINT" AND "DNA origami" AND "incorporation efficiency"` (15) ·
`"Folding DNA to create nanoscale shapes and patterns"` (8) ·
`"DNA origami" AND "next-generation sequencing" AND "staple" AND abundance` (14) ·
`"DNA origami" AND "mass spectrometry" AND "stoichiometry" AND staple` (25)

**G — single-molecule stiffness and force scatter (9) — all negative on the width**
`"DNA origami" AND "single-molecule force spectroscopy" AND "rupture force" AND distribution` (10) ·
`"DNA hairpin" OR "DNA duplex" AND "rupture force" AND "standard deviation" AND "identical" AND heterogeneity` (2, conference abstracts) ·
`"molecular heterogeneity" AND "single molecule" AND DNA AND "force spectroscopy"` (42) ·
`"DNA origami" AND "spring constant" AND (variabilit* OR "standard deviation" OR scatter)` (83) ·
`"DNA origami" AND "stiffness" AND "individual" AND measurement AND distribution` (179) ·
`"nominally identical" AND (DNA OR oligonucleotide) AND (stiffness OR "spring constant" OR elasticity) AND variability` (13, none relevant) ·
`"magnetic tweezers" AND DNA AND "molecule-to-molecule" AND variability` (18) ·
`"optical tweezers" AND "DNA" AND "persistence length" AND "molecule to molecule variation"` (1) ·
`"dynamic force spectroscopy" AND "DNA" AND "rupture force" AND "loading rate" AND "width of the distribution"` (6)

**H — tolerance terminology (6) — all negative**
`"DNA origami" AND "fabrication tolerance"` (2, irrelevant) ·
`"DNA origami" AND "dimensional tolerance" OR "manufacturing tolerance"` (5535, `OR` too loose) ·
`"DNA nanostructure" AND ("positional disorder" OR "positional variance" OR "positional spread")` (1, irrelevant) ·
`"DNA origami" AND "helix position" AND "deviation from design"` (**0**) ·
`"DNA origami" AND "as-built" OR "as-designed" AND deviation AND measured` (633) ·
`"DNA origami placement" AND (accuracy OR precision) AND "standard deviation"` (3)

**I — crossover geometry and MD distributions (4)**
`"DNA origami" AND "structure factor" AND "disorder"` (8) ·
`"DNA origami" AND "Holliday junction" AND "angle" AND distribution AND "cryo-EM"` (7) ·
`"DNA origami" AND "molecular dynamics" AND "interhelical distance" AND histogram` (2) ·
`"DNA origami" AND "crossover" AND "geometry" AND "variability"` (35)

**arXiv (6)**, via `curl -sL 'https://export.arxiv.org/api/query?search_query=…'`:
`all:"DNA origami" AND all:"interhelical"` (**0**) ·
`all:"DNA origami" AND all:"small-angle X-ray"` (2) ·
`all:"DNA origami" AND all:"disorder"` (6) ·
`all:"DNA origami" AND all:"paracrystalline"` (**0**) ·
`all:"DNA nanostructure" AND all:"lattice disorder"` (**0**) ·
`all:"DNA origami" AND all:"defect" AND all:"statistics"` (**0**)

**Crossref (6)**, via `api.crossref.org/works?query.bibliographic=…`:
`DNA origami small-angle X-ray scattering interhelical distance` ·
`DNA origami lattice disorder Debye-Waller paracrystalline` ·
`DNA origami interhelical distance distribution standard deviation` ·
`DNA origami staple incorporation efficiency DNA-PAINT` ·
`DNA origami AFM height distribution population` ·
`base pair step twist rise standard deviation crystallography`

---

## Access notes worth keeping

- **PMC now gates file downloads behind a SHA-256 proof-of-work cookie** (`POW_CHALLENGE` + nonce, difficulty 4, cookie `cloudpmc-viewer-pow="<challenge>,<nonce>"`). Solving it is ~10 ms in Python, and it is the **only** route to Fischer's SI, Bai's SI and the Olson and Lankaš PDFs.
- **EuropePMC `fullTextXML` returned zero bytes** for `PMC6544510`, `PMC3523823`, `PMC21613`, `PMC1303568` and `PMC2737683` — **all five are fully readable on the PMC article page.** `CLAUDE.md` already records that an empty `fullTextXML` is not an absence of full text; this is five more instances in one survey.
- **Edinburgh Research Explorer and Oxford ORA both 403 their PDFs** to `curl` *and* to WebFetch despite being listed green OA by Unpaywall and OpenAlex.

## What measurement would close the gap

- **Quantity 1, done properly:** a synchrotron SAXS series on a single-layer sheet at several tile widths (12, 24, 48 helices), fitting the interhelical Bragg peak with a **paracrystalline (Hosemann) lattice model** rather than a bare Lorentzian. The discriminant needs no new instrument: **finite-size broadening is order-independent and scales as `1/N`; cumulative paracrystalline disorder broadens as `n²` in the reflection order and does not scale with `N`.** Measuring the second-order reflection and sweeping the helix count returns `g = σ_a/a` directly, which is exactly the tolerance `T-134` wants. Fischer's own beamlines (P08 at DESY, ID1 at ESRF) and sample preparation are already demonstrated adequate.
- **An independent check:** cryo-EM with RELION **multi-body refinement** on a twist-corrected single-layer tile, one body per duplex, which returns the covariance matrix of inter-duplex displacement directly — Videbæk 2025's method one level down. It needs a twist-corrected tile to be solvable at all, given Kube 2020's failure on the uncorrected one.
- **Quantity 4:** a magnetic-tweezers or centrifuge-force-microscope assay on a population of nominally identical staple-extension tethers, reporting the **distribution** of the force-extension constant across tethers rather than its mean. Multiplexed platforms measure hundreds of tethers in parallel; nobody has reported the width.
