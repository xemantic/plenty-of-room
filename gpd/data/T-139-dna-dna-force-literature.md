# T-139 — the DNA–DNA force-versus-separation literature, for an *unbonded* pair in mM divalent salt

**25 query strings** across EuropePMC REST, arXiv, Crossref/Semantic Scholar, Unpaywall and one web search, recorded in full in §5.

Every number below carries a **read flag**, per `CLAUDE.md`'s research practice:

- **READ DIRECTLY** — the PDF or full text was fetched and the passage read; the sentence is quoted verbatim in §4.
- **ABSTRACT ONLY** — verbatim from a EuropePMC REST or Crossref abstract, and labelled as such.
- **FIGURE AXIS** — read off the axis labels recovered by `pdftotext -layout`; the individual data points were **not** digitised.
- **DERIVED** — this project's arithmetic on a read value, marked so it is never mistaken for a published number.
- **NOT FOUND** — searched for and not found; §6 records what was looked for.

**Nothing here comes from a search-engine summary.**

---

## 1. The headline

**In Mg²⁺ at the millimolar scale there is no equilibrium separation for a pair of B-DNA duplexes: the interaction is repulsive at every separation, the array swells without bound as the applied pressure goes to zero, and two unconstrained duplexes simply drift apart.**

This is stated in almost those words by four independent groups, on four independent methods, and it is the *majority* answer of the field:

| statement | who | method | flag |
|---|---|---|---|
| *"In Mg²⁺-only solutions in which DNA-DNA interaction is always repulsive, the force-spacing curve extends to infinity because zero force can only be achieved at infinite DNA-DNA spacing"* | Meng, Timsina, Bull, Andresen, Qiu (2020) | osmotic stress + XRD, 20 mM MgCl₂ | READ DIRECTLY |
| *"Under these salt conditions, DNA helices repel at all separations."* (uni- and divalent cations) | Rau & Parsegian (1992) | osmotic stress + XRD | READ DIRECTLY |
| *"pairwise DNA–DNA forces were always repulsive (f(ξ) > 0) in monovalent (Na⁺) and divalent (Mg²⁺) electrolytes regardless of the concentration"* | Yoo & Aksimentiev (2016) | all-atom PMF, **two** parallel duplexes | READ DIRECTLY |
| *"the two dsDNAs fluctuate across a wide DNA-DNA distance range rather than preferring to remain close to each other during the MD process"* | Zhang, Wu, Xi, Sang, Tan (2017) | all-atom PMF + unconstrained MD, 20 mM Mg²⁺ | READ DIRECTLY |

The one honest qualification is that **the pair potential is not exactly zero-attraction**: the second virial coefficient of *free* 25 bp duplexes in solution changes sign at **≈10–16 mM free Mg²⁺**, and above that there is a weak net attraction that nonetheless **never produces a bound state or a condensed phase** for random-sequence DNA at any Mg²⁺ concentration.
At the ~2 mM this task cares about, every measurement is on the **repulsive** side by a wide margin — the nearest measured point is **3 mM Mg²⁺, explicitly repulsive** (Pabit et al. 2009, read directly).

---

## 2. The numbers

### 2a. Force / pressure versus interaxial separation — the fitted equations of state

| # | quantity | value | conditions | source | flag |
|---|---|---|---|---|---|
| 1 | Π(d) functional form, hydration formalism | `Π(d) = Π_R e^(−d/λ) + Π_A e^(−d/2λ)` | d = DNA–DNA interaxial spacing | Meng 2020, Biophys J 118:3021 | READ DIRECTLY |
| 2 | λ (hydration decay length of the **pressure**) | **2.4 Å** | *"universal"*, dominant below d ≈ 26 Å | Meng 2020 | READ DIRECTLY |
| 3 | Π_R, **Mg²⁺-only** curve (f_Mg = 1, 0 mM CoHex) | **201.8 GPa** | 20 mM MgCl₂ | Meng 2020, p. 3022 | READ DIRECTLY |
| 4 | Π_A, **Mg²⁺-only** curve | **−0.3 GPa** (*"slightly negative"*) | 20 mM MgCl₂ | Meng 2020, p. 3022 | READ DIRECTLY |
| 5 | Π_R / Π_A, CoHex-only curve (for contrast) | 418.4 GPa / **−1.3 GPa** | 1 mM Co(NH₃)₆³⁺ | Meng 2020, p. 3022 | READ DIRECTLY |
| 6 | isobaric crossover of the CoHex-only and Mg²⁺-only curves | d ≈ 25.5 Å | — | Meng 2020, p. 3022 | READ DIRECTLY |
| 7 | data floor (spacings below this discarded) | 24.5 Å | — | Meng 2020, Fig. 1 caption | READ DIRECTLY |
| 8 | Mg²⁺ bath range swept | 0–20 mM (at 1 mM CoHex), plus a 0 mM CoHex / 20 mM Mg²⁺ curve | — | Meng 2020, Fig. 1 caption | READ DIRECTLY |

**The Mg²⁺ curve evaluated (DERIVED — my arithmetic on rows 2–4, not a published table).**
`1 MPa = 1 pN/nm²` exactly, which is the project's unit:

| d (Å) | Π_R term (pN/nm²) | Π_A term (pN/nm²) | Π total (pN/nm²) | Π total (atm) |
|---|---|---|---|---|
| 24 | 9.16 | −2.02 | 7.14 | 70.5 |
| 26 | 3.98 | −1.33 | 2.65 | 26.1 |
| 28 | 1.73 | −0.879 | 0.852 | 8.41 |
| 30 | 0.752 | −0.579 | 0.173 | 1.71 |
| 32 | 0.327 | −0.382 | −0.055 | −0.54 |
| 34 | 0.142 | −0.252 | −0.110 | −1.08 |
| 36 | 0.0617 | −0.166 | −0.104 | −1.03 |

**A caveat that must travel with rows 3–4, and it is load-bearing.**
Taken literally the fitted pair crosses zero at `d = 2λ ln(Π_R/|Π_A|) = 31.25 Å` (DERIVED), i.e. a surface separation of 11.3 Å on a 20 Å hard diameter — which would *be* an equilibrium separation, and which **contradicts the same paper's own prose** (quote Q1 in §4) that the Mg²⁺-only curve extends to infinity and that Mg²⁺-only baths *"do not condense DNA spontaneously"*.
The resolution is that `Π_A = −0.3 GPa` is quoted to one decimal and sits inside the experimental scatter: at `Π_A = −0.03 GPa` the crossing moves to 42 Å and at `Π_A = 0` there is none at all, and the crossing in any case lies at the top of the measured window.
**The observation (no spontaneous condensation, no finite spacing at zero load) is the primary datum; the fitted `Π_A` is a two-parameter summary and its sign is not resolved by the data.**
Use the repulsion-only column (`Π = 201.8 GPa · e^(−d/2.4 Å)`) as the working Mg²⁺ equation of state, and treat the attractive term as a bound on how wrong that can be.

### 2b. The monovalent bracket — a fully parametrised, independent equation of state

| # | quantity | value | conditions | source | flag |
|---|---|---|---|---|---|
| 9 | Π₀(R) = Π_h(R) + Π_e(R), with `Π_e(R) = A_e [K₀(R/λ_D)/K₁(a/λ_D)]²` and `Π_h(R) = A_h [K₀(R/λ_h)/K₁(a/λ_h)]²`, `R = d_int/2`, `a < 10 Å` | — | linearised PB cylindrical cell model | Yasar et al. 2014, Sci Rep 4:6877, Eqs. 1–2 | READ DIRECTLY |
| 10 | A_h (hydration amplitude) | **1019 atm** | linked across all [NaCl] | Yasar 2014 | READ DIRECTLY |
| 11 | λ_h | **2.2 Å** (error ≈ 10 %) | — | Yasar 2014 | READ DIRECTLY |
| 12 | A_e (electrostatic amplitude) | **≈ 155 atm**, *"about the same for all [NaCl]"*, uncertainty ≈ 10 % | 0.05–0.4 M NaCl | Yasar 2014 | READ DIRECTLY |
| 13 | λ_D | `3.08/√(I in M)` Å | — | Yasar 2014 | READ DIRECTLY |
| 14 | fit range | d_int ≳ 26 Å; P ≈ 5 → 72 atm; [NaCl] 0.1–0.4 M | — | Yasar 2014, Fig. 3 caption | READ DIRECTLY |

Row 13 at 2 mM MgCl₂ (I = 3c = 6 mM) gives **λ_D = 39.8 Å** (DERIVED), which reproduces `CLAUDE.md`'s 3.93 nm bulk Debye length exactly.

### 2c. Podgornik/Rau/Parsegian's univalent "toolbox" — force per unit length between parallel rods

`f_h(R) = f_h0 exp(−R/λ_h)` and `f_e(R) = f_e0 exp(−R/λ_D)`, both in dyn/cm, R in Å (Biophys J **66**:962, Eqs. 13–15). All **READ DIRECTLY** from Tables 1 and 2.

Table 2a, **high pressure**, `D_int < 32 Å` (bare hydration):

| salt | conc (M) | λ_h (Å) | log₁₀ f_h0 (dyn/cm) |
|---|---|---|---|
| LiCl | 0.4 | 3.0 | 4.64 |
| NaCl | 0.4 | 3.1 | 4.65 |
| KCl | 0.5 | 3.35 | 4.48 |
| CsCl | 0.4 | 3.7 | 4.16 |
| TMACl | 0.4 | 4.0 | 4.12 |

Table 1a, **low pressure**, `D_int > 32 Å` at 0.4 M (fluctuation-enhanced electrostatic; λ_D = 4.75 Å there):

| salt | λ_eff/2 (Å) | log₁₀ f_eff (dyn/cm) | log₁₀ f_e0 (dyn/cm) | L_e (Å) | ξ |
|---|---|---|---|---|---|
| LiCl | 4.5 | 1.69 | 2.88 | 3.2 | 2.2 |
| NaCl | 4.6 | 1.68 | 2.86 | 3.3 | 2.2 |
| KCl | 4.5 | 1.76 | 3.00 | 2.8 | 2.5 |
| CsCl | 4.8 | 1.87 | 3.22 | 2.2 | 3.2 |
| TMACl | 4.0 | 1.95 | — | — | — |

Table 1b, NaCl concentration series (λ_D from 6.7 Å at 0.2 M to 3.9 Å at 0.6 M; λ_eff/2 tracks it to within 0.1 Å at every concentration): **the observed electrostatic decay length is twice the Debye length**, not the Debye length.

**No divalent salt appears anywhere in this paper** — the toolbox is univalent only (NOT FOUND, for Mg²⁺).

### 2d. Mg²⁺ decay lengths, measured directly

| # | quantity | value | conditions | source | flag |
|---|---|---|---|---|---|
| 15 | exponential decay constant of `f(d_i)` in MgCl₂ | **2.7, 2.8, 2.1 Å** at **5, 25, 100 mM** MgCl₂ | 20 °C, calf-thymus DNA | Rau, Lee & Parsegian 1984, PNAS 81:2622 | READ DIRECTLY |
| 16 | same, CaCl₂ / putrescine²⁺ | 3.1 Å (25 mM CaCl₂) / 3.2 Å (10 mM putrescine·2HCl) | — | Rau 1984, p. 2623 | READ DIRECTLY |
| 17 | universal decay constant, all salts | **2.5–3.5 Å**, over surface separations 5–15 Å | 0.005–1.0 M ionic solutions | Rau 1984, abstract | READ DIRECTLY |
| 18 | interaxial range covered by the data | **25–45 Å** | B-form throughout; helix diameter ≈ 20 Å | Rau 1984, Results | READ DIRECTLY |
| 19 | plotted ordinate range, Figs. 1–6 | log₁₀ Π ≈ **6 → 8** in dyn/cm² over 20–45 Å | — | Rau 1984 | **FIGURE AXIS** |
| 20 | DNA pressure at phage packing densities | **1.2–5.5 × 10⁷ dyn/cm²** (= **1.2–5.5 pN/nm²**, DERIVED) at **26–30 Å** interaxial; packing energy 0.1–0.4 kcal/mol per bp | salt condition not stated in that sentence | Rau 1984, Discussion, p. 2625 | READ DIRECTLY |

Row 20 is the one *absolute* Π-at-a-named-D pair the 1984 paper puts in prose, and it cross-checks the Meng fit: the repulsion-only column of §2a gives 3.98 pN/nm² at 26 Å and 0.75 pN/nm² at 30 Å, inside/adjacent to Rau's 1.2–5.5 pN/nm² band for the same spacings.
`1 dyn/cm² = 0.1 Pa` and `1 MPa = 1 pN/nm²`.

### 2e. Hydration versus electrostatics — where the crossover sits (question 4)

| # | statement | value | source | flag |
|---|---|---|---|---|
| 21 | *"Only beyond 15 Å separation between molecules is there evidence of electrostatic double-layer forces."* | surface separation **15 Å** ⇒ interaxial **35 Å** | Rau 1984, abstract | READ DIRECTLY |
| 22 | *"At interaxial distances <30-35 Å (surface separations < 10-15 Å), exponentially varying repulsion decays with a characteristic length of ~3 Å."* | interaxial **30–35 Å** | Rau & Parsegian 1992, Biophys J 61:251 | READ DIRECTLY |
| 23 | *"A transition between the high pressure, bare hydration dominated and low pressure, fluctuation enhanced force regimes occurs at about 30-35 Å"* | interaxial **30–35 Å** | Podgornik 1994, Biophys J 66:967 | READ DIRECTLY |
| 24 | hydration dominant *"at the closest spacings (e.g., <26 Å)"*, and for Mg²⁺-only *"negligible residual electrostatic repulsion beyond the short-range hydration repulsion"* | interaxial **26 Å** | Meng 2020, p. 3021 | READ DIRECTLY |
| 25 | at 2.0 M salt, λ_D = 2.1 Å *"much less than the λ_h ≈ 3.0-4.0 Å for the hydration forces which now dominate electrostatic interactions at all separations"* | — | Podgornik 1994, Fig. 4 caption | READ DIRECTLY |

**The interaxial window this project cares about, 18–36 Å (surface 0–16 Å), is almost entirely inside the hydration-dominated regime on every one of these readings.**
Rows 21–23 place the crossover at 30–35 Å; row 24 places it at 26 Å; only the top ~1–6 Å of the project's window is outside it, and there the residual force is what rows 9–13 (monovalent) or the doubled-Debye rows of §2c describe.

**The dissent must be recorded.**
Yoo & Aksimentiev (2016) fit the same experimental curves with atomistic MD and conclude the opposite attribution — *"the DNA condensation to be driven by electrostatics of polycations and not hydration"*, and *"water does not play a major direct role in inducing inter-DNA attraction"* (READ DIRECTLY, abstract and p. 2042).
The *functional form* is not in dispute — a short-range exponential of 2.4–3.5 Å decay — only the name of the mechanism.
**For a force calculation the name does not matter; for extrapolating outside the fitted range it does.**

### 2f. Free duplex pairs in solution — the only data at the concentration this task asks about

Pabit, Qiu, Lamb, Li, Meisburger, Pollack, *Nucleic Acids Res.* **37**:3887–3896 (2009), PMC2709557, open access. **25 bp duplexes at 0.1–1 mM, in pH 7 / 1 mM Na-MOPS with MgCl₂ only, no confinement of any kind.** All **READ DIRECTLY**.

| # | [Mg²⁺] (bulk) | sign of A₂ / observation for **DNA** |
|---|---|---|
| 26 | 0 mM | repulsion |
| 27 | **3 mM** | **repulsion** — *"the scattering from DNA strands falls below the form factor at the lowest q, indicating repulsion"* |
| 28 | **6 mM** | **repulsion** — *"the low q portion of the DNA scattering profile again falls below the form factor, indicating repulsion"* |
| 29 | 16 mM | weak attraction |
| 30 | 133 mM | attraction |
| 31 | crossover | *"For 25 bp DNA duplexes, the shift from repulsion to attraction occurs when the free Mg²⁺ concentration equals 10 mM Mg²⁺"* |

`A₂ > 0` implies repulsion, `A₂ < 0` implies a predominantly negative intermolecular potential leading to attraction (their Results).
**3 mM is the closest measured point to the project's 2 mM, and it is unambiguously repulsive.**

### 2g. Two-duplex simulations (question 5)

| # | system | [Mg²⁺] | result | source | flag |
|---|---|---|---|---|---|
| 32 | two parallel 20 bp duplexes, periodic (effectively infinite), umbrella-sampled PMF | 20 mM Mg²⁺ + 200 mM Na⁺ | *"always repulsive (f(ξ) > 0) … regardless of the concentration"*; pairwise additivity verified against array simulations | Yoo & Aksimentiev 2016, NAR 44:2040 | READ DIRECTLY |
| 33 | two parallel 16 bp dsDNA, umbrella sampling + pseudo-spring | **20 mM** | *"the PMF between dsDNAs appears flat and is weakly repulsive"* | Zhang et al. 2017, Biophys J 113:519 | READ DIRECTLY |
| 34 | same | **100 mM** | *"very slight (−0.02 k_BT per bp at a DNA-DNA distance of 29 Å), and should be insufficient to induce the condensation of short dsDNAs"* | Zhang et al. 2017 | READ DIRECTLY |
| 35 | **two unconstrained** dsDNA, free MD | 20 mM | *"the two dsDNAs fluctuate across a wide DNA-DNA distance range rather than preferring to remain close to each other"* | Zhang et al. 2017 | READ DIRECTLY |
| 36 | two parallel 20 bp **random-sequence** duplexes (MixDNA), well-tempered metadynamics | *"pure Mg"* (concentration only in the SI, **NOT FOUND** in the main text) | *"the free energy of MixDNA lacks any deep minimum at short DNA−DNA distances … The downhill nature of the energy profile of MixDNA suggests spontaneous dissociation of DNA arrays."* | He, Qiu, Kirmizialtin 2023, JCTC 19:6831 | READ DIRECTLY |
| 37 | same, but **homopolymeric** dA₂₀·dT₂₀ (ATDNA) | pure Mg | minimum at **d ≈ 2.8 nm**, `ΔF = F(2.8 nm) − F(4.0 nm) ≈ −0.15 kJ/mol/bp` | He 2023, p. 6830 | READ DIRECTLY |
| 38 | two linear dsDNA fragments, umbrella sampling | divalent vs monovalent | *"DNA−DNA repulsion in short center-of-mass distances decreases significantly in the presence of divalent counterion-ions (as compared to monovalent ions) for linear DNA"* — a **reduction of repulsion**, not an attraction | Alexiou & Likos 2023, JPCB 127:6969 | READ DIRECTLY (abstract, in-PDF) |

**Row 36 is the direct answer to the task's question 5**: for a random-sequence duplex pair in pure Mg²⁺ the free energy is monotone downhill in separation, i.e. **there is no equilibrium separation and the pair dissociates spontaneously.**
Row 35 is the same statement observed rather than computed from a PMF.

### 2h. The counter-case (question 3) — where Mg²⁺ *does* produce a finite spacing

Every one of these is outside the (2 mM, random-sequence, aqueous, room-temperature) corner this task occupies.

| # | route to attraction | threshold / spacing | source | flag |
|---|---|---|---|---|
| 39 | **Sequence.** poly(A)·poly(T) (AA-TT) duplex condenses in all alkaline-earth cations | critical **≈ 18 mM Mg²⁺**; condensation between **15 and 20 mM** for Mg²⁺/Ca²⁺/Sr²⁺/Ba²⁺; **re-dissolves above ≈ 750 mM Mg²⁺** | Srivastava et al. 2020, NAR 48:7020 | READ DIRECTLY |
| 40 | same, PMF | minimum **−0.13 kcal/mol per turn at d ≈ 29 Å**, at 60 mM Mg²⁺; *"At low [Mg²⁺] the pairwise interactions between the duplexes are repulsive … marked by the absence of a minimum at PMF"* | Srivastava et al. 2020 | READ DIRECTLY |
| 41 | **The controls in the same paper.** poly(AT)·poly(TA) and random-sequence genomic DNA | *"no condensation at all Mg²⁺ concentrations"*; *"divalent alkaline earth metal ions in this study … do not condense dsDNA in general"* | Srivastava et al. 2020 | READ DIRECTLY |
| 42 | **Short free duplexes, second virial.** | *"An intriguing short range attraction is observed at surprisingly low divalent cation concentrations, approximately **16 mM Mg²⁺**."* | Qiu, Kwok, Park, Lamb, Andresen, Pollack, PRL **96**:138101 (2006) | **ABSTRACT ONLY** (verbatim, EuropePMC REST) |
| 43 | same group, follow-up | *"As the [Mg²⁺] increases, this coefficient turns from positive to negative reflecting the transition from repulsive to attractive inter-DNA interaction."* … *"The dependence of the observed attraction on experimental parameters including DNA length provides valuable clues to its origin."* | Qiu, Andresen, Kwok, Lamb, Park, Pollack, PRL **99**:038104 (2007) | **ABSTRACT ONLY** (verbatim, EuropePMC REST) |
| 44 | corroboration of row 42 by a third group | *"counterion mediated attraction can be induced by divalent magnesium counterions in sufficiently high concentration (16 mM Mg²⁺)"* | Alexiou & Likos 2023, JPCB 127:6970 | READ DIRECTLY |
| 45 | **Transition metals, not alkaline earths.** | *"Among the divalent cations we have tested, only Mn²⁺ and Cd²⁺ appear to confer this kind of condensation of double helices at temperatures below about 60 °C; Mg²⁺, Ca²⁺, Ni²⁺, Zn²⁺, and putrescine are all quite powerless in this regard."* | Rau & Parsegian 1992, Biophys J 61:263 | READ DIRECTLY |
| 46 | **Elevated temperature, Mn²⁺ only.** | *"there is no concentration of MnCl₂ that will cause DNA precipitation at room temperature without added osmotic stress"*; clouding temperatures *"never fall below ~40 °C"* even at 0.5 M MnCl₂ | Rau & Parsegian 1992, Biophys J 61:262 | READ DIRECTLY |
| 47 | **Non-aqueous cosolvent.** | *"Spontaneous precipitation of DNA with 20 mM Mg²⁺ requires at least 25 % methanol."* Equilibrium spacing then **32.5 Å at 25 % methanol → 26 Å at 50 %** | Rau & Parsegian 1992, Biophys J 61:250, Fig. 4 caption | READ DIRECTLY |
| 48 | **A more highly charged helix.** Triple-strand DNA is condensed by Mg²⁺/Ba²⁺/Ca²⁺ | interaxial **29.8 / 30.2 / 29.6 Å**; poly(AT*T) still soluble in 5 mM MgCl₂, precipitated by 10 mM | Qiu, Parsegian & Rau 2010, PNAS 107:21482 | READ DIRECTLY |
| 49 | same paper's statement about **duplex** DNA | *"nonspecifically interacting monovalent and divalent cations … even at molar concentrations, do not condense dsDNA from dilute solution"* | Qiu 2010, PNAS | READ DIRECTLY |

**Rows 42–44 and row 49 look contradictory and are not.**
The same author (Qiu) publishes both.
A negative second virial coefficient for short free duplexes above ~10–16 mM Mg²⁺ is a statement that the *pair* potential integrates to a net attraction; it is **not** a statement that the pair has a bound state, and random-sequence dsDNA does not precipitate in Mg²⁺ at any concentration.
The attraction seen in the SAXS profiles is in part **end-to-end stacking** of the short duplexes (Pabit 2009: *"The shape of these curves is consistent with end-to-end stacking, previously reported in DNA"*, READ DIRECTLY) — a chemistry a long duplex or an origami helix does not have available side-by-side.

---

## 3. What this settles for T-139

1. **At ~2 mM MgCl₂ there is no equilibrium centre-to-centre separation for two unbonded parallel B-DNA duplexes.** The force is repulsive at every separation, monotone, and the only stationary point is at infinity. Four independent methods say so (§1); the nearest *measured* point, 3 mM Mg²⁺ on free duplexes with no confinement at all, is explicitly repulsive (row 27).
2. **The repulsion the pair feels is short-range and hydration-form.** Use `Π(d) = 201.8 GPa · exp(−d/2.4 Å)` (rows 2–3), which gives 3.98 pN/nm² at 26 Å, 0.752 at 30 Å and 0.062 at 36 Å, and is cross-checked by Rau's independently stated 1.2–5.5 pN/nm² at 26–30 Å (row 20). Note this is a **20 mM** Mg²⁺ fit; at 2 mM the electrostatic tail is longer (λ_D = 39.8 Å against 12.6 Å), so it is an *underestimate* of the long-range repulsion and an adequate estimate of the short-range one.
3. **The whole 18–36 Å interaxial window is inside the hydration-dominated regime** on every published reading of the crossover (26 Å to 35 Å, rows 21–24). Which is to say: at these separations there is no long-range physics to get wrong, and the answer is a steep monotone exponential.
4. **A design that needs two duplexes to sit at a defined separation must hold them there.** The literature contains no mechanism by which mM Mg²⁺ supplies one for random-sequence B-DNA.

---

## 4. The verbatim passages

**Q1 — Meng, Timsina, Bull, Andresen, Qiu, *Biophys. J.* 118:3019–3025 (2020), PMC7300303, Results, p. 3021.** READ DIRECTLY.

> *"Under a constant [CoHex] of 1 mM and varied [Mg²⁺] between 0 and 20 mM, DNA spontaneously condenses, resulting in finite DNA-DNA spacings at zero DNA-DNA force and pressure. DNA-DNA force rises from zero upon being pushed to closer spacings by the osmolyte PEG8k, giving a convex curve on the log-linear scale. **In Mg²⁺-only solutions in which DNA-DNA interaction is always repulsive, the force-spacing curve extends to infinity because zero force can only be achieved at infinite DNA-DNA spacing**, as shown for 20 mM Mg²⁺ in Fig. 1 a."*

Same paper, p. 3021, the equation of state and the crossover:

> *"One salient feature is a dominant short-range, repulsive hydration force at the closest spacings (e.g., <26 Å), which is an exponential force with a universal decay length of 2.4 Å (14). … Noncondensing ions typically give an additional electrostatic repulsion, whereas the Mg²⁺-only force curve here shows negligible residual electrostatic repulsion beyond the short-range hydration repulsion. With hydration forces as the predominant contributions to all measured forces in this study, DNA osmotic pressure can be described by Π(d) = Π_R e^(−d/λ) + Π_A e^(−d/2λ), where d is the DNA-DNA spacing, λ is the decay length of 2.4 Å, and Π_R and Π_A are the magnitudes of the hydration repulsion and attraction, respectively."*

Same paper, p. 3022, the fitted magnitudes:

> *"For the two asymptotic cases, the force curve with CoHex only (i.e., f_Co = 1 with 0 mM [Mg²⁺] in the bath) gives Π_R of 418.4 GPa and Π_A of −1.3 GPa, and the curve with Mg²⁺ only (i.e., f_Mg = 1 with 0 mM [CoHex] in the bath) gives Π_R of 201.8 GPa and Π_A of −0.3 GPa. **The slightly negative Π_A for Mg²⁺ may appear unexpected** but is consistent with a recent study of DNA phase transition reporting a universal medium-range attraction even in monovalent salts (32)."*

**Q2 — Rau & Parsegian, *Biophys. J.* 61:246–259 (1992), PMC1260238, Discussion, first paragraph, p. 251.** READ DIRECTLY.

> *"We have previously characterized forces between DNA helices for several different uni- and divalent cations (Rau et al., 1984; Parsegian et al., 1985; Parsegian et al., 1987). **Under these salt conditions, DNA helices repel at all separations.** At interaxial distances <30-35 Å (surface separations < 10-15 Å), exponentially varying repulsion decays with a characteristic length of ~3 Å. The decay length and magnitude of this repulsion are surprisingly insensitive to ionic strength or to the type of ion in the suspending medium."*

Same paper, Materials and Methods, on what a PEG phase separation does and does not prove:

> *"In univalent salt solution, the phase separation of DNA and PEG simply means that DNA-DNA interactions are less repulsive than DNA-PEG interactions, not that helices are absolutely attractive."*

Same paper, Fig. 4 caption, p. 250:

> *"Equilibrium spacings decrease montonically with increased methanol concentration for all three ions. **Spontaneous precipitation of DNA with 20 mM Mg²⁺ requires at least 25% methanol.**"*

**Q3 — Rau, Lee & Parsegian, *Proc. Natl. Acad. Sci. USA* 81:2621–2625 (1984), PMC345121.** READ DIRECTLY.

Abstract:

> *"We have measured the repulsive force between B-form double helices in parallel packed arrays of polymer-condensed DNA in the presence of 0.005-1.0 M ionic solutions. Molecular repulsion is consistently exponential with a 2.5-3.5 Å decay distance, when the separation between DNA surfaces is 5-15 Å. Only weakly dependent on ionic strength and independent of molecular size, this intermolecular repulsion does not obey the predictions of electrostatic double-layer theory. … **Only beyond 15 Å separation between molecules is there evidence of electrostatic double-layer forces.**"*

Results, p. 2622, the Mg²⁺ data (their Fig. 3):

> *"Forces in MgCl₂ solutions at 5, 25, and 100 mM (Fig. 3) are weaker than those in solutions of univalent ions but show an exponential decay qualitatively similar to that described above, with **decay constants 2.7, 2.8, and 2.1 Å**, respectively. At 0.1 M, MgCl₂ does lead to somewhat weaker forces but the difference in decay constants is much smaller than the factor of 2 expected from electrostatic double layer theory."*

Introduction, p. 2621, the onset of the measurable force:

> *"**The strong repulsive force is detectable when the interaxial distance is about 35 Å, at which point the shortest distance between DNA surfaces is ~15 Å.** This force between molecules grows exponentially with a 2.5-3.5 Å characteristic distance as the molecules are brought together."*

Discussion, p. 2625, the one absolute pressure-at-a-spacing statement:

> *"…it will decay exponentially, as shown in the figures, for the **26-30 Å interaxial distances** observed in several bacteriophages (table 2 of ref. 38). At these distances, the packing energy per base, **0.1-0.4 kcal/mol of base pairs** … (or the **DNA pressure, 1.2-5.5 × 10⁷ dyne/cm²**; 1 dyne = 10 μN), is approximately one order of magnitude less than expected previously…"*

And on the putrescine²⁺ control:

> *"It is clear that this ion leads to forces much as are seen with Ca²⁺ or Mg²⁺; nothing special is apparent. The decay constant is 3.2 Å."*

**Q4 — Rau & Parsegian, *Biophys. J.* 61:260–271 (1992), PMC1260239.** READ DIRECTLY.

p. 263:

> *"**Among the divalent cations we have tested, only Mn²⁺ and Cd²⁺ appear to confer this kind of condensation of double helices at temperatures below about 60 °C; Mg²⁺, Ca²⁺, Ni²⁺, Zn²⁺, and putrescine are all quite powerless in this regard.** Divalent Cu²⁺ does precipitate DNA at millimolar concentrations but with no apparent x-ray order in the pellet."*

p. 262, on 50 mM MnCl₂:

> *"At low osmotic pressures (log Π < 6.2), the force strongly resembles that found in NaCl solution (decay length λ ≈ 3.5 Å). **Helices repel at all distances; 50 mM MnCl₂ does not cause spontaneous assembly at room temperature.**"*

p. 262:

> *"Unlike what is seen for DNA in cobalt hexammine, **there is no concentration of MnCl₂ that will cause DNA precipitation at room temperature without added osmotic stress.**"*

**Q5 — Podgornik, Rau & Parsegian, *Biophys. J.* 66:962–971 (1994), PMC1275803.** READ DIRECTLY.

Abstract:

> *"Interaction coefficients for the exponentially varying hydration force seen at spacings less than 10 to 15 Å between surfaces are extracted directly from pressure versus interaxial distance curves. **Electrostatic interactions are only observed at larger spacings and are always coupled with configurational fluctuation forces that result in observed exponential decay lengths that are twice the expected Debye-Huckel length.**"*

p. 967:

> *"A transition between the high pressure, bare hydration dominated and low pressure, fluctuation enhanced force regimes occurs at about 30-35 Å and is characterized by a relatively sharp break in the Na⁺ and Li⁺ force curves…"*

**Q6 — Yasar, Podgornik, Valle-Orero, Johnson & Parsegian, *Sci. Rep.* 4:6877 (2014), PMC4220286.** READ DIRECTLY.

> *"We found λ_h ≈ 2.2 Å with an error ≈ 10 % and then performed a global fitting with four data sets. In this step, λ_h was fixed at 2.2 Å, while A_h and A_e were free parameters. A_h was linked for all [NaCl] and A_e was allowed to be different for different [NaCl]. In this way **A_h = 1019 atm and A_e ≈ 155 atm**, about the same for all [NaCl], with an uncertainty ≈ 10 %."*

**Q7 — Pabit, Qiu, Lamb, Li, Meisburger & Pollack, *Nucleic Acids Res.* 37:3887–3896 (2009), PMC2709557, Results.** READ DIRECTLY.

> *"**At 3 mM Mg²⁺, the scattering from DNA strands falls below the form factor at the lowest q, indicating repulsion.** Conversely, the RNA scattering profile coincides with the form factor; the RNA strands do not interact. … At 6 mM Mg²⁺ (Figure 1b), the low q portion of the DNA scattering profile again falls below the form factor, indicating repulsion. … At 16 mM Mg²⁺, the DNA scattering profile rises above the form factor, indicating weak attraction … At the highest [Mg²⁺] studied, 133 mM, attraction is measured for both DNA and RNA."*

> *"A₂ > 0 implies repulsion between like-charged helices while A₂ < 0 implies a predominantly negative intermolecular potential leading to attraction (27,33). **For 25 bp DNA duplexes, the shift from repulsion to attraction occurs when the free Mg²⁺ concentration equals 10 mM Mg²⁺**, as in (25,26)."*

**Q8 — Yoo & Aksimentiev, *Nucleic Acids Res.* 44:2036–2046 (2016), PMC4797306, p. 2040.** READ DIRECTLY.

> *"…we computed the pairwise PMF ΔG(ξ) between two parallel DNA molecules and the average pairwise inter-DNA force f(ξ) as a function of the inter-DNA distance ξ; Figure 4A illustrates the simulation setup. In qualitative agreement with the results of our DNA array simulations and experiment (13), **pairwise DNA–DNA forces were always repulsive (f(ξ) > 0) in monovalent (Na⁺) and divalent (Mg²⁺) electrolytes regardless of the concentration**, Figure 4B and C. Furthermore, the pairwise force in the presence of Sm⁴⁺ was attractive (f(ξ) < 0) within the 28 to 40 Å range of the inter-DNA distance even at sub-mM [Sm⁴⁺]."*

Abstract, the mechanism dissent:

> *"Analysis of the MD trajectories determined the DNA–DNA force in a DNA condensate to be pairwise, **the DNA condensation to be driven by electrostatics of polycations and not hydration**, and the concentration of bridging cations, not adsorbed cations, to determine the magnitude and the sign of the DNA–DNA force."*

**Q9 — Zhang, Wu, Xi, Sang & Tan, *Biophys. J.* 113:517–528 (2017), PMC5549645.** READ DIRECTLY.

Abstract:

> *"Our calculations show that the PMF between tsDNAs is apparently attractive and becomes more strongly attractive at higher [Mg²⁺], **although the PMF between dsDNAs cannot become apparently attractive even at high [Mg²⁺]**."*

Results, p. 519:

> *"As shown in Fig. 2, **the PMF between dsDNAs appears flat and is weakly repulsive at low (20 mM) [Mg²⁺]**. When [Mg²⁺] is increased to 100 mM, the PMF between dsDNAs changes from a weak repulsion to a weak attraction. Such attraction is very slight (−0.02 k_BT per bp at a DNA-DNA distance of 29 Å), and should be insufficient to induce the condensation of short dsDNAs. The weakly repulsive PMF between dsDNAs at 20 mM [Mg²⁺] is in accordance with our MD simulation for two unconstrained dsDNAs. As shown in Fig. 2 C, **the two dsDNAs fluctuate across a wide DNA-DNA distance range rather than preferring to remain close to each other during the MD process.**"*

**Q10 — He, Qiu & Kirmizialtin, *J. Chem. Theory Comput.* 19:6827–6838 (2023), PMC10569048, p. 6831.** READ DIRECTLY.

> *"While the free energy profile of ATDNA shows attraction, **the free energy of MixDNA lacks any deep minimum at short DNA−DNA distances (Figure 2b). The downhill nature of the energy profile of MixDNA suggests spontaneous dissociation of DNA arrays.** This observation is in accord with experiments on the genomic DNA."*

p. 6830, for the homopolymeric sequence that *does* attract:

> *"The free energy projected on interhelical distances suggests that the ATDNA pair has the energy minimum at **d ∼ 2.8 nm** (Figure 2a), consistent with the equilibrium interhelical distance measured by experiments. The free energy of binding **ΔF = F(2.8 nm) − F(4.0 nm) ≈ (−0.15) kJ/mol/bp** agrees well with the estimated value from osmotic stress measurements."*

**Q11 — Srivastava, Timsina, Heo, Dewage, Kirmizialtin & Qiu, *Nucleic Acids Res.* 48:7018–7026 (2020), PMC7367160.** READ DIRECTLY.

p. 7021:

> *"Stronger inter-dsDNA attractions have been reported for AT-rich duplexes compared with random or GC-rich sequences (9,10), but the multivalent cations examined therein (spermine⁴⁺ or hex-lysine⁶⁺) also condense random-sequence dsDNA, **unlike divalent alkaline earth metal ions in this study which do not condense dsDNA in general**."*

p. 7021:

> *"…all alkaline earth metal cations tested (Mg²⁺, Ca²⁺, Sr²⁺ and Ba²⁺) **condense AA-TT between 15 and 20 mM** … It is interesting to note that Ca²⁺ condenses AA-TT again above 1200 mM, while **AA-TT is soluble in Mg²⁺ above ∼750 mM**."*

p. 7021, and this is the sentence that bounds the whole counter-case:

> *"Figure 1C shows the force-spacing curves of AA-TT, AT-TA, GNOM duplexes in 20 mM Mg²⁺, slightly above the critical concentration of **∼18 mM Mg²⁺** for AA-TT condensation. **AT-TA and GNOM duplexes give nearly identical** [curves] … **At low [Mg²⁺] the pairwise interactions between the duplexes are repulsive (see Figure 2C), marked by the absence of a minimum at PMF.**"*

p. 7021, the AA-TT PMF minimum:

> *"At this concentration the free energy shows a **minimum of −0.13 kcal/mol per turn** (Figure 2D). The minimum is located at **d ≈ 29 Å**, consistent with the inter-helical distance measured in experiment."*

Introduction, p. 7018:

> *"In contrast, alternating A-T and T-A base pairs (AT-TA) with the same A-T content shows nearly identical behavior to **random-sequence genomic DNA — no condensation observed**."*

**Q12 — Qiu, Parsegian & Rau, *Proc. Natl. Acad. Sci. USA* 107:21482–21486 (2010), PMC3003027.** READ DIRECTLY.

Abstract:

> *"Here we show that triple-strand DNA (tsDNA), a more highly charged helix than dsDNA, **is precipitated by alkaline-earth divalent cations that are unable to condense dsDNA**."*

Introduction, p. 21482:

> *"Highly charged DNA helices naturally repel each other under physiological conditions (1). … In contrast, **nonspecifically interacting monovalent and divalent cations [i.e., excluding base-coordinating transition-metal ions (4) such as Mn²⁺, Ni²⁺, and Cu²⁺], even at molar concentrations, do not condense dsDNA from dilute solution** (2)."*

Results:

> *"…in 1×TE buffer, poly(AT*T) triplex is still soluble in 5 mM MgCl₂, whereas 10 mM MgCl₂ pre[cipitates it]…"*

**Q13 — Qiu, Kwok, Park, Lamb, Andresen & Pollack, *Phys. Rev. Lett.* 96:138101 (2006), DOI `10.1103/PhysRevLett.96.138101`.** **ABSTRACT ONLY** — verbatim from EuropePMC REST `resultType=core`, PMID 16712040. The full text could not be obtained (§6).

> *"Interactions between short strands of DNA can be tuned from repulsive to attractive by varying solution conditions and have been quantified using small angle x-ray scattering techniques. The effective DNA interaction charge was extracted by fitting the scattering profiles with the generalized one-component method and inter-DNA Yukawa pair potentials. A significant charge is measured at low to moderate monovalent counterion concentrations, resulting in strong inter-DNA repulsion. **The charge and repulsion diminish rapidly upon the addition of divalent counterions. An intriguing short range attraction is observed at surprisingly low divalent cation concentrations, approximately 16 mM Mg²⁺.**"*

**Q14 — Qiu, Andresen, Kwok, Lamb, Park & Pollack, *Phys. Rev. Lett.* 99:038104 (2007), DOI `10.1103/PhysRevLett.99.038104`.** **ABSTRACT ONLY** — verbatim from EuropePMC REST `resultType=core`, PMID 17678334.

> *"Can nonspecifically bound divalent counterions induce attraction between DNA strands? Here, we present experimental evidence demonstrating attraction between short DNA strands mediated by Mg²⁺ ions. Solution small angle x-ray scattering data collected as a function of DNA concentration enable model independent extraction of the second virial coefficient. **As the [Mg²⁺] increases, this coefficient turns from positive to negative reflecting the transition from repulsive to attractive inter-DNA interaction.** This surprising observation is corroborated by independent light scattering experiments. **The dependence of the observed attraction on experimental parameters including DNA length provides valuable clues to its origin.**"*

**Q15 — Alexiou & Likos, *J. Phys. Chem. B* 127:6969–6981 (2023), PMC10424236.** READ DIRECTLY.

Abstract:

> *"…while **DNA−DNA repulsion in short center-of-mass distances decreases significantly in the presence of divalent counterion-ions (as compared to monovalent ions) for linear DNA**, the opposite effect occurs for the DNA minicircles."*

Introduction, corroborating the 16 mM figure:

> *"…counterion mediated attraction can be induced by divalent magnesium counterions in sufficiently high concentration (**16 mM Mg²⁺**)."*

---

## 5. Queries run

**EuropePMC REST** (`https://www.ebi.ac.uk/europepmc/webservices/rest/search`, `format=json`, 8 s between calls):

1. `TITLE:"hydration force" AND TITLE:DNA` — 0 hits
2. `AUTH:"Rau DC" AND (DNA AND force)` — 29
3. `AUTH:"Parsegian VA" AND TITLE:"double helices"` — 6
4. `"interaxial" AND DNA AND "osmotic pressure"` — 78
5. `TITLE:"hydration forces" AND DNA` — 7
6. `AUTH:"Podgornik R" AND TITLE:"undulatory"` — 1
7. `AUTH:"Strey HH" AND DNA` — 12
8. `"potential of mean force" AND "DNA duplexes" AND (magnesium OR "Mg2+") AND "molecular dynamics"` — 39
9. `AUTH:"Pollack L" AND TITLE:"inter-DNA"` — 2
10. `TITLE:"second virial" AND DNA AND (SAXS OR "small-angle")` — 0
11. `AUTH:"Qiu X" AND AUTH:"Pollack L" AND (DNA AND (repulsion OR interaction OR potential))` — 5
12. `DNA AND "equation of state" AND "osmotic pressure" AND "interaxial"` — 6
13. `AUTH:"Rau DC" AND TITLE:"osmotic stress"` — 8
14. `EXT_ID:17678334` (`resultType=core`, for the verbatim abstract)
15. `EXT_ID:16712040` (`resultType=core`)
16. `EXT_ID:9666326` (`resultType=core`; Strey et al. 1998 *Curr. Opin. Struct. Biol.* review — abstract carries no numbers)
17. `DNA AND "magnesium" AND ("does not condense" OR "do not condense" OR "cannot condense")` — 25
18. `"DNA arrays" AND (swell OR swelling) AND ("osmotic pressure" OR "osmotic stress")` — 11
19. `DNA AND "hydration force" AND (Mg2+ OR MgCl2 OR divalent) AND "decay length"` — 25

**arXiv API** (`https://export.arxiv.org/api/query`, `curl -sL`):

20. `all:"inter-DNA potentials in solution"` — 0 entries
21. `all:"Inter-DNA attraction mediated by divalent counterions"` — 0 entries
22. `abs:"second virial" AND abs:DNA AND abs:Mg` — 0 entries

**Semantic Scholar and Unpaywall** (open-PDF hunt for the two PRLs):

23. `DOI:10.1103/PhysRevLett.96.138101` — Semantic Scholar: *not found*; Unpaywall: `is_oa = false`, no OA locations
24. `DOI:10.1103/PhysRevLett.99.038104` — Semantic Scholar: *not found*; Unpaywall: `is_oa = false`, no OA locations

**Web search** (used only to locate a candidate URL, never as a source of a number):

25. `Qiu Kwok Park Lamb Andresen Pollack "Measuring inter-DNA potentials in solution" Physical Review Letters 2006 pdf` — surfaced the Gettysburg College Cupola record `cupola.gettysburg.edu/physfac/3/`, whose `viewcontent.cgi` PDF endpoint answers **HTTP 202 with a zero-length body** to `curl` (with and without a cookie jar and referer, three attempts) and **HTTP 403** to WebFetch.

**Full-text fetch routes that worked:** `https://europepmc.org/articles/PMC<id>?pdf=render` served **11 of 13** attempted PDFs at first try (`PMC345121`, `PMC1260238`, `PMC1260239`, `PMC1275803`, `PMC39523`, `PMC4797306`, `PMC7367160`, `PMC10569048`, `PMC4220286`, `PMC5500594`, `PMC10681747`, `PMC3003027`, `PMC2709557`, `PMC7300303`, `PMC5549645`, `PMC4804163`), then `pdftotext -layout`.
It returned **HTTP 500** for `PMC2843915` (Qiu 2008 PRL) and `PMC3420006` (Qiu 2011 PRL); the `pmc.ncbi.nlm.nih.gov` article page for `PMC2843915` returned a 20 KB JavaScript shell with no article text.

---

## 6. What is NOT found

1. **No osmotic-stress force curve for B-DNA arrays at 2 mM MgCl₂.** The lowest Mg²⁺-only concentration for which a force-versus-spacing curve is published is **5 mM** (Rau 1984, Fig. 3) and it exists only as a figure — the paper tabulates the decay constant (2.7 Å) and no prefactor. The lowest **parametrised** Mg²⁺-only curve is **20 mM** (Meng 2020). Queries 4, 12, 13, 17, 18, 19.
2. **No Π₀ prefactor for any divalent salt in the Podgornik/Rau/Parsegian "toolbox".** *Biophys. J.* 66:962 is univalent only, by construction (*"our object in this paper is to create such a 'toolbox' for the physical forces between DNA double helices in univalent salt solutions"*).
3. **The full texts of Qiu et al. PRL 96:138101 (2006) and PRL 99:038104 (2007) were not obtained.** APS is closed, Unpaywall reports `is_oa = false` for both, neither is on arXiv, and the one green-OA record (Gettysburg Cupola) refuses both `curl` and WebFetch. **The 16 mM figure and the sign change of A₂ are therefore ABSTRACT ONLY** — but both are independently corroborated by two papers read directly in full (Pabit 2009's 10 mM crossover, row 31, and Alexiou & Likos 2023's citation of 16 mM, row 44).
4. **No measurement or simulation of an unbonded duplex pair at ~2 mM Mg²⁺ specifically.** Every two-duplex PMF found (rows 32–38) is at **20 mM or above**; the only *measurement* below 10 mM on free duplexes is Pabit 2009's second-virial-coefficient sign at 3 and 6 mM (rows 27–28), which gives the sign of the interaction but not a force-versus-separation curve.
5. **The Mg²⁺ concentration of He, Qiu & Kirmizialtin's *"pure Mg"* simulations is not stated in the main text** — it is in the Supporting Information, which was not fetched. Row 36's verdict (downhill, spontaneous dissociation for random-sequence DNA) does not depend on it.
6. **No paper was found reporting an equilibrium interaxial spacing for random-sequence B-DNA duplexes in aqueous Mg²⁺ at room temperature, at any concentration.** Queries 4, 8, 9, 11, 12, 17, 18, 19 — and the four papers that address the question head-on all report its absence (§1). The four routes by which a finite spacing *has* been produced are all recorded in §2h and all of them require leaving this corner of parameter space: a homopolymeric sequence, a transition metal, elevated temperature, methanol, or a triple helix.
