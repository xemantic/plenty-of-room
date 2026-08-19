# T-50 — the beyond-mean-field electrostatics literature, for the DECAY RATE of a wall–wall force in mM 2:1 salt

**38 query strings** — 28 arXiv API, 7 Crossref, 3 web — plus 5 publisher/repository fetch attempts, recorded in full in §5.

Every number below carries a **read flag**, per `CLAUDE.md`'s research practice:

- **READ DIRECTLY** — the PDF or full text was fetched and the passage read; the sentence is quoted verbatim in §4.
- **ABSTRACT ONLY** — verbatim from a Crossref REST abstract, and labelled as such. (No EuropePMC query was run: none of this literature is PubMed-indexed.)
- **FIGURE AXIS** — read off axis labels; individual data points **not** digitised.
- **DERIVED** — this project's arithmetic on a read value, marked so it is never mistaken for a published number.
- **NOT FOUND** — searched for and not found; §6 records what was looked for.

**Nothing here comes from a search-engine summary.** Web search was used only to locate candidate URLs.

**Scope reminder.** `T-50` does not need the *size* of the correlation correction. At a force-pinned operating point the level of the multiplier `μ(h)` in `|F_true| = μ(h)|F_PB|` is absorbed into the bias, and only `g = d ln μ/dh` reaches the stiffness. The threshold is `|g| = 0.038 nm⁻¹` at a 7 nm gap in 2 mM MgCl₂ — equivalently, a true decay length **9.7 % shorter** than mean field (2.86 → 2.58 nm). Everything below is organised around *decay rate*, not amplitude.

---

## 1. The headline

**The asymptotic decay length of a surface force is a property of the BULK electrolyte and not of the surfaces, and this is a theorem, not an approximation.** Every surface effect this project has worried about — ion–ion correlations at the wall, the Manning/saturation charge convention, image charges, Bikerman finite ion size, the rim charge, the Stern layer — enters the **amplitude** and, if the decay is oscillatory, the **phase**. None of them can enter `g`.

Stated by Kjellander in one sentence (READ DIRECTLY, §4 Q1):

> *"Surface forces between two macroscopic bodies decay for large separations with the same decay length as in the bulk phase in contact with the surfaces, but the amplitude and, for oscillatory forces, the phase depend on the properties of the bodies."*

**And the bulk decay length of this project's electrolyte is the Debye length, to within the resolution of the field.** Cats, Evans, Härtel & van Roij measure the charge decay length `ξ_Z` of the primitive model by four independent routes (MSA integral-equation theory, two classical DFTs, and molecular dynamics) as a function of the single dimensionless group `dκ_D` (ion diameter × inverse Debye length), and find (READ DIRECTLY, §4 Q2a):

> *"At low concentrations, dκ_D < 0.5, all the approaches we consider agree that ξ_Z is close to κ_D⁻¹…"*

**This device sits at `dκ_D = 0.089–0.488` over its whole buffer range** (0.5–10 mM MgCl₂, hydrated-ion diameter 0.70–0.856 nm) — inside that window at every state (DERIVED, row 12).

**The correction, where it exists at all, has the WRONG SIGN for a stroke-shortening worry.** For `dκ_D > 0.5` the same paper finds `ξ_Z` **smaller** than the Debye length — but the device never reaches that regime, and reaching the Kirkwood crossover (monotonic → damped-oscillatory decay) would need **≥ 63 mM MgCl₂**, 6.3× the top of the specified range (DERIVED, row 14).

**Three things did not come out the way the task's framing expected, and all three matter:**

1. **The Waisman–Lebowitz/Blum MSA screening parameter `2Γ` is NOT a screening length**, and the paper that supplies its closed form says so in as many words (§4 Q2b). Evaluated naively it gives a decay length **9.9 % LONGER** than Debye at 2 mM — which is the same size as the 9.7 % threshold and in the opposite direction, i.e. exactly the kind of number that would have been quoted as an answer and would have been wrong (rows 15–18).
2. **For OPPOSITELY charged walls the loop expansion about mean field is exponentially better behaved than for like-charged walls**, and this is published with a criterion and with Monte Carlo backing at `Ξ` up to **86** (§4 Q3a–Q3c). The whole strong-coupling worry is a like-charge worry.
3. **The one published closed form for `g` that I found is a dressed-ion second-virial result, and for a pure 2:1 salt its coefficient is `q/(2(q+1)) = 1/3` EXACTLY** — no concentration, no Bjerrum length, no temperature — giving `g = −0.085 nm⁻¹` at 2 mM, **2.2× over threshold**. But its own expansion parameter at this project's surface charges is **5.4 to 10¹¹**, so the theory refuses its own evaluation here (rows 48–51). It is a warning about *where the answer must come from*, not an answer.

---

## 2. The numbers

### 2a. Q1 — the decay length is a bulk property

| # | statement | source | flag |
|---|---|---|---|
| 1 | Surface forces decay at large separation with the **same decay length as the bulk phase**; only amplitude (and oscillatory phase) depend on the bodies | Härtel & Kjellander, arXiv:2412.01653, opening sentence; published Faraday Discuss. **253** (2024) 293–295 | **READ DIRECTLY** |
| 2 | Dressed ion theory (DIT) is *"an exact reformulation[] of the statistical mechanics of simple electrolytes"*; the exact decay parameter solves `κ² = (β/ε₀ε_r) Σ_j n_j q_j q_j^eff(κ)` (spherical ions), Eq. (1) | same, Eq. (1) | **READ DIRECTLY** |
| 3 | The **only** difference from Debye–Hückel is that one factor `q_j` is replaced by the effective charge `q_j^eff(κ)`; `κ` appears on both sides, so it is an equation for `κ` | same | **READ DIRECTLY** |
| 4 | For a symmetric electrolyte, `(κ/κ_DH)² = q^eff(κ)/q`, so **underscreening (`κ < κ_DH`) happens if and only if `q^eff(κ) < q`** — *"the precise and unique condition"* | same | **READ DIRECTLY** |
| 5 | DIT primary sources | Kjellander & Mitchell, *Chem. Phys. Lett.* **200** (1992) 76; *J. Chem. Phys.* **101** (1994) 603; generalised in Kjellander, *Soft Matter* **15** (2019) 5866 | **NOT FOUND** (full texts; cited from row 1's reference list, READ DIRECTLY) |
| 6 | Independent statement of the same theorem, from a different group and a different method: the solvation force between two planar walls decays as `f_s(H) ∝ cos(2πH/λ + φ) e^(−H/ξ)`, `H → ∞`, **where `ξ` is the longest decay length in the (bulk) system** | Cats, Evans, Härtel & van Roij, arXiv:2012.02713 = *J. Chem. Phys.* **154** (2021) 124504, Eq. (49) | **READ DIRECTLY** |
| 7 | They confirm it operationally: *"We confirmed that the results for the various decay lengths in DFT were independent of the surface potential Φ₀."* | same, §IV B | **READ DIRECTLY** |
| 8 | And they close the loop between surface and bulk: `ξ = max(ξ_Z, ξ_N)` at every concentration, with *"excellent agreement between ξ and ξ_Z from MSA-based IET up to concentrations dκ_D ≈ 3"* | same, §V | **READ DIRECTLY** |
| 9 | Kirkwood's own 1936 crossover value `x_K = κ_D a ≃ 1.03`; linearised MPB (Outhwaite) `x_K ≃ 1.241`; GMSA (Leote de Carvalho & Evans, *Mol. Phys.* **83** (1994) 619) `x_K ≃ 1.228`; generalised DH (Lee & Fisher) `x_K = 1.17832` | Lee & Fisher, arXiv:cond-mat/9705235 = *Europhys. Lett.*, Eq. (5) and refs. [5],[6],[7] | **READ DIRECTLY** |

**Row 1 is the load-bearing sentence of this whole file.** It says that `g` cannot be a surface quantity at all, at large `κh`.

### 2b. Q2 — how far the true bulk decay length is from Debye, at 2–30 mM ionic strength

| # | quantity | value | conditions | source | flag |
|---|---|---|---|---|---|
| 10 | `ξ_Z ≈ κ_D⁻¹` and `ξ_N ≈ ½κ_D⁻¹` | agreement across MSA-IET, MSAc-DFT, MSAu-DFT and MD | **`dκ_D < 0.5`** | Cats 2021, §V | **READ DIRECTLY** |
| 11 | For `dκ_D > 0.5`, *"the limiting law is no longer valid and ξ_Z is found to be **smaller** than the Debye length"* | — | `0.5 < dκ_D < x_K` | Cats 2021, §IV B 2 | **READ DIRECTLY** |
| 12 | **`dκ_D` for this device** | **0.089 / 0.178 / 0.282 / 0.399** at `d = 0.70 nm`; **0.109 / 0.218 / 0.345 / 0.488** at `d = 0.856 nm` | 0.5 / 2 / 5 / 10 mM MgCl₂; `λ_D` from `C-0005` | this project | **DERIVED** |
| 13 | Kirkwood point of the RPM, measured four ways | `x_K^MSAc ≈ 1.24`, `x_K^MSAu ≈ 0.7004`, `x_K^IET ≈ 1.229`, MD `≈ 1.37` | `T* = d/λ_B = 0.685` (a 1:1 aqueous electrolyte at room T) | Cats 2021, §IV B 2 | **READ DIRECTLY** |
| 14 | **MgCl₂ concentration needed to reach the Kirkwood crossover** | **63–95 mM** (`x_K = 1.229`); 79–118 mM at the MD value 1.37 | `d = 0.856 / 0.70 nm` | this project, on row 13 | **DERIVED** |
| 15 | MSA closed form for the Blum/Waisman–Lebowitz screening parameter, for the RPM (`η = 0`): from `Γ² = πλ_B·2ρ_b/(1+dΓ)²` it follows that `2Γ(1+dΓ) = κ_D`, hence **`2Γ = [√(1+2dκ_D) − 1]/d`** | — | RPM | derivation on Cats 2021 Eq. (16); Blum/Waisman–Lebowitz refs. 26–32 therein | **DERIVED** from a **READ DIRECTLY** equation |
| 16 | **`2Γ` is NOT a physical screening length**, verbatim: *"whereas κ_D⁻¹ plays the role of a screening length in the dilute limit as we will see, 1/2Γ is merely an intermediate parameter of the theory and should not be regarded as a physical screening length"* | — | — | Cats 2021, §II A 1 | **READ DIRECTLY** |
| 17 | Naive `1/2Γ` evaluated anyway, as the trap it is | **+5.2 / +9.9 / +15.0 / +20.3 %** longer than `λ_D` | 0.5 / 2 / 5 / 10 mM, `d = 0.856 nm` | this project | **DERIVED** |
| 18 | The exact statement `2Γ → κ_D` as `dκ_D → 0` | — | — | Cats 2021, below Eq. (18) | **READ DIRECTLY** |
| 19 | **Valency and size asymmetry do not change the asymptotics**: *"we also performed PM calculations (not reported here) with various ionic valency and diameter asymmetries. The resulting asymptotic decay properties are very similar to those of the RPM reported here. We find no long decay lengths"* | — | primitive model | Cats 2021, §V | **READ DIRECTLY** |
| 20 | Where a *real* deviation from Debye lives: the experimental SFA "anomalous underscreening" is at `dκ_D > 1` and scales `κ_D ξ ∝ (dκ_D)³`; at 4.93 M NaCl, `κ_D ξ ≈ 24` measured against `≈ 2.3` for the RPM — *"about a factor 10"* | — | 1:1, molar | Cats 2021, §V | **READ DIRECTLY** |
| 21 | And it is not reproduced in the primitive model at all: *"the (R)PM in equilibrium cannot explain the experimental (SFA) measurements reporting an anomalously large decay length"* | — | — | Cats 2021, Conclusion | **READ DIRECTLY** |
| 22 | Anomalous underscreening *is* reproducible in MD once ion **clustering** is present, and it is a **concentrated**-electrolyte phenomenon; structural decay follows `λ₁ ≈ 0.4(σ/λ_D)^1.5` | — | RPM, MD | Härtel, Bültmann & Coupette, arXiv:2209.03486 = *Phys. Rev. Lett.* **130** (2023) 108202 | **READ DIRECTLY** |
| 23 | Reason the long monotonic tail is hard to see at all: `K/K′ = (κ/\|κ′\|)⁴ · \|E_r^eff(κ′)\|/\|E_r^eff(κ)\|`, and in MD `[κ/\|κ′\|]⁴ ≈ 10⁻⁵` at `βR = 13–17` | — | RPM, 0.1 M | Härtel & Kjellander, arXiv:2412.01653, Eq. (3) | **READ DIRECTLY** |
| 24 | Coupling strength scale: `βR = βq²/(4πε₀ε_r d)`; **`βR = 1.67` "corresponds to an aqueous solution of monovalent electrolyte at room temperature"**, and at that value the decay is *"dominated by a monotonic mode with a κ value not too different from κ_DH, which is typical for dilute solutions at low coupling" * | — | — | same, Fig. 1 caption and text | **READ DIRECTLY** |

**Direction of the answer to Q2: within the primitive model the true bulk decay length at 0.5–10 mM MgCl₂ is the Debye length**, and the first correction (`dκ_D > 0.5`) makes it *shorter*, not longer. The "longer than Debye" intuition in the task prompt is an *underscreening* intuition, and underscreening in this literature is a **concentrated**-electrolyte phenomenon at `dκ_D > 1`, i.e. 5–20× outside this device's range, and one the primitive model does not even reproduce.

### 2c. Q3 — beyond mean field for OPPOSITELY charged walls

| # | quantity | value | source | flag |
|---|---|---|---|---|
| 25 | Weak-coupling (loop-expansion) validity criterion for a **repulsive** mean-field pressure, from `\|p̃₂\| < \|p̃₀\|` at `D̃ ≫ 1`: **`Ξ < D̃/ln D̃`**, `D̃ = D/μ_GC` | Eq. (64) | Kanduč, Trulsson, Naji, Burak, Forsman & Podgornik, arXiv:0905.3851 | **READ DIRECTLY** |
| 26 | Same criterion where the mean-field pressure is **attractive** (only possible for `ζ = σ₂/σ₁ < 0`, i.e. oppositely charged): **`Ξ < (ζ²/\|f(ζ)\|) e^(−2ζD̃)`** — *"The right hand side here is exponentially large"* | Eq. (65) | same | **READ DIRECTLY** |
| 27 | Verbatim conclusion: *"…for charged surfaces of opposite sign, the weak-coupling analysis performs far better at finite coupling parameters and smaller inter-surface separations than for the surfaces of equal sign (ζ > 0)."* | — | same, §V C | **READ DIRECTLY** |
| 28 | **Monte Carlo backing**, at `Ξ = 0.32, 8.6, 86` and `ζ = +0.5, 0, −0.5`, `ã = 1.34`: *"for surfaces with opposite sign, ζ = −0.5, there is no big difference between PB and SC profiles"*, and *"In the case of ζ = −0.5 the difference between the strong and weak coupling results for the rescaled interaction pressure is marginal and the simulation data and the analytical results nearly coincide for all rescaled separations D̃ = D/µ."* | — | same, §VII | **READ DIRECTLY** |
| 29 | Row 25 evaluated for this device at the **bare duplex** surface (`μ_GC = 0.119 nm`, `Ξ = 24.0`, from `C-0005`) | bound = **5.96 / 11.24 / 14.43** at `D = 2 / 5 / 7 nm` → **FAILS** at every gap | this project | **DERIVED** |
| 30 | Row 25 evaluated at the **saturated far-field tile charge** (`σ_eff = 0.0568 e/nm²` at 2 mM, from `C-0005`; then `μ_GC = 1.962 nm`, `Ξ = 1.456`) | bound = **2.72 / 2.81** at `D = 5 / 7 nm` → **PASSES** (`1.456 < 2.72`) | this project | **DERIVED** |

**Row 30 against row 29 is a question this file cannot settle and should not hide**: whether the coupling criterion is owed at the bare duplex cylinder or at the smeared, charge-saturated tile wall. `CLAUDE.md` already records that `Ξ` must be read from the duplex cylinder and not the projected density — *for the local coupling*. But the criterion in row 25 is a statement about a **planar wall bounding the gap**, and the gap-facing wall of this device is charge-saturated. The two readings differ by 16.5× in `Ξ` and land on opposite sides of the same inequality.

**Caveat that must travel with rows 25–30.** Kanduč et al. is a **counterion-only** model — *"neglecting completely the effects of salt"* — so it has no Debye length and says nothing about `g` directly. It answers only the half of Q3 that asks whether mean field is more reliable for oppositely charged walls. It is.

### 2d. Q4 — measured double-layer decay lengths in divalent electrolyte

| # | statement | source | flag |
|---|---|---|---|
| 31 | *"…force profiles between particles in solutions containing divalent (and sometimes trivalent) counterions can be still very well described by DLVO theory."* | Trefalt, Palberg & Borkovec, arXiv:1606.00266, §"Forces in Multivalent Electrolytes" | **READ DIRECTLY** |
| 32 | *"This adsorption process is the principal reason why the DH theory actually is – somewhat counter intuitively – a **better approximation in the presence of multivalent counterions** than in the monovalent setting."* — the multivalent deviation is a **diffuse-layer-potential** (amplitude) effect, not a decay effect | same | **READ DIRECTLY** |
| 33 | *"The slope of these lines reflects the inverse Debye length κ, which can be **reliably calculated** … from the known salt concentration."* (silica colloidal probe, KCl, ≥ 1 mM) | same, §"Forces at Lower Concentrations of Monovalent Salts" | **READ DIRECTLY** |
| 34 | AFM colloidal probe, silica, **1 mM to 5 M**: *"The decay length of these interaction forces decreases with increasing salt concentration in agreement with the theoretical Debye screening length … and the DFT calculations"*; and at pH 9 / 45 °C *"the observed asymptotic decay lengths agree with the expected Debye screening length"* | Kumar, Cats, Alotaibi, Ayirala, Yousef, van Roij, Siretanu & Mugele, arXiv:2201.08667 | **READ DIRECTLY** |
| 35 | Same study includes *"mixed concentrated salt solutions (involving both mono- and divalent cations and anions)"* and finds *"none of the conditions explored displayed any indication of anomalous long range electrostatic forces"* | same, abstract | **READ DIRECTLY** |
| 36 | Where the multivalent deviation *does* appear: as an **additional exponential attraction at small separation**, fitted at `A = 45 mN/m`, `λ = 1 nm`, on top of a DLVO baseline whose long-range decay is left at the bulk Debye value | Kanduč, Moazzami-Gudarzi, Valmacco, Podgornik & Trefalt, arXiv:1701.08989, §III | **READ DIRECTLY** |
| 37 | And the interpretation, verbatim: *"at large separations the electrostatic potential is rather small and the mean-field behavior is sufficient to characterize the experiment but only if in addition one takes into account that electrostatic interactions between the surface and the counterions reduce the effective surface charge from its bare PB value."* | same | **READ DIRECTLY** |
| 38 | The classic SFA measurement in Mg²⁺/Ca²⁺/Sr²⁺/Ba²⁺ chloride: Pashley & Israelachvili, *J. Colloid Interface Sci.* **97** (1984) 446, DOI `10.1016/0021-9797(84)90316-3` | located, Elsevier closed, Crossref carries **no abstract** | **NOT FOUND** |

**The Q4 answer is row 32 read together with row 36**: in divalent electrolyte the *measured* long-range decay tracks the bulk Debye length computed from the nominal ionic strength, and every measured correlation effect shows up either as a reduced effective surface charge (amplitude) or as a **short-range** extra exponential of decay length ≈ 1 nm. That is precisely the `μ(h)` decomposition `T-50` assumes — with the correction living where `T-50` hopes it lives.

### 2e. Q5 — Bikerman / steric-modified PB and the decay length

| # | statement | source | flag |
|---|---|---|---|
| 39 | The Borukhov–Andelman–Orland MPB equation, verbatim as Eq. (25): `∇²ψ = (zec₀/ε)·2sinh(zeψ/kT)/[1 + 2ν sinh²(zeψ/2kT)]` | Kilic, Bazant & Ajdari, arXiv:physics/0611030 = *Phys. Rev. E* **75** (2007) 021502, Eq. (25) | **READ DIRECTLY** |
| 40 | *"There are at least three important lengths in our models. The first is the **Debye length λ_D** given by (5), **which sets the width of the diffuse layer at low voltage and low bulk concentration**, c₀."* | same, §I C | **READ DIRECTLY** |
| 41 | **The steric term is exactly second order in `ψ`, so it cannot move `κ`.** Linearising Eq. (25): `sinh²(zeψ/2kT) = O(ψ²)`, so the denominator is `1 + O(ψ²)` and the linearised equation is `∇²ψ = κ²ψ` with **the unmodified `κ`**, identically, at every `ν`. Bikerman finite ion size is therefore a pure **amplitude** effect on the far field — `g_Bikerman = 0` exactly | this project, on row 39 | **DERIVED** |
| 42 | What steric effects *do* move: the condensed layer thickness `l_c`, which *"grows sublinearly, proportionally to the square root of the potential drop"* — a boundary-layer offset, not a decay rate | same, §II A | **READ DIRECTLY** |
| 43 | Consistency check against this project's own number: `CLAUDE.md` records Bikerman **raising** `\|F_es\|` by up to 56 %. Row 41 says that is entirely a change of amplitude and contributes **zero** to `g`. The two statements are compatible and the second is the one `T-50` needs | this project | **DERIVED** |

Row 41 is a genuinely load-bearing derivation and it is one line of algebra. It also fits row 1: a finite ion size at the wall is *surface* physics, and by Kjellander's theorem surface physics cannot reach the decay length.

### 2f. Q6 — Monte Carlo of a primitive-model slit, pressure versus separation

| # | study | what it is | flag |
|---|---|---|---|
| 44 | Guldbrand, Jönsson, Wennerström & Linse, *J. Chem. Phys.* **80** (1984) 2221, DOI `10.1063/1.446912` — *"We find large deviations from the standard Poisson–Boltzmann treatment of the so called double layer force **for divalent counterions at high surface charge densities and at short separations**"*; two causes, correlation-driven condensation and a van der Waals-type attraction from correlated fluctuations; *"for some realistic values of the parameters the attraction overcomes the repulsive part"* | **LIKE-charged** walls, high `σ` | **ABSTRACT ONLY** (Crossref, verbatim) |
| 45 | Valleau, Ivkov & Torrie, *J. Chem. Phys.* **95** (1991) 520, DOI `10.1063/1.461452` — MC for the RPM between parallel charged surfaces, *"applied principally to **2:2 electrolytes** at a variety of surface charge densities"*; *"the force between the surfaces resulting from the electrolyte is almost everywhere attractive"* | **LIKE-charged**, 2:2 not 2:1 | **ABSTRACT ONLY** (Crossref, verbatim) |
| 46 | Guldbrand, Nilsson & Nordenskiöld, *J. Chem. Phys.* **85** (1986) 6686, DOI `10.1063/1.451450` — MC of hexagonally packed B-DNA; *"for monovalent counterions, the mean field Poisson–Boltzmann theory can give a reasonable reproduction of the experimental data"*; *"the correlation between the ion clouds of different DNA polyions gives rise to a significant attractive contribution … when divalent cations are present"* | cylinders, not a slit | **ABSTRACT ONLY** (Crossref, verbatim) |
| 47 | Kanduč et al., arXiv:0905.3851 — **the closest existing match to what `C-0005` prices**: MC of a planar slit at `Ξ = 0.32, 8.6, 86` with `ζ = +0.5, 0, −0.5`, pressure versus separation, PB + one-loop and strong-coupling both compared | counterion-only, **no added salt**; pressure in rescaled units | **READ DIRECTLY** |
| 48 | The one closed-form published `g`: dressed-ion theory at large separation, `f_el(h) ≃ w₀₀(h)K(h)` with `K(h) = 1 − c₀(2πℓ_B q²/κ²)(C + κh)`, Eqs. (19)–(21) | Kanduč et al. 2017, arXiv:1701.08989 | **READ DIRECTLY** |
| 49 | Row 48 differentiated: `g = d ln μ/dh = −bκ/K(h)` with **`b = c₀·2πℓ_B q²/κ²`**, and for a *pure* q:1 salt (`κ² = 4πℓ_B c₀ q(q+1)`) this reduces to **`b = q/(2(q+1))`, exactly** — 1/4, **1/3**, 3/8, 2/5 at `q = 1, 2, 3, 4`, carrying no concentration, no Bjerrum length and no temperature | this project | **DERIVED** |
| 50 | Row 49 evaluated at 2 mM MgCl₂ (`q = 2`, `b = 1/3`, `κ = 0.2548 nm⁻¹`): **`g = −0.0849 nm⁻¹`, 2.2× the 0.038 nm⁻¹ threshold**, in the *unfavourable* (steepening) direction | this project | **DERIVED** |
| 51 | …and its own validity parameter `bC` (row 48's `C = 3/2 − 2γ + 2 ln κμ + 2κμ e^(1/κμ) + 2Ei(1/κμ)`): **5.4** at the saturated tile wall (`κμ = 0.500`) and **≈ 10¹¹** at the bare duplex wall (`κμ = 0.0303`). `K(h)` is then large and negative — the second-virial expansion has failed outright, at both readings of the wall | this project | **DERIVED** |

**Rows 49–51 together are the single most useful negative result in this file.** A published beyond-mean-field theory does give a closed form for exactly the quantity `T-50` wants, it does predict a value 2.2× over the threshold, and it cannot be evaluated here because its own expansion parameter is 5–11 orders of magnitude too big. That is the *same* verdict as `C-0005`'s 123–214 %, reached on a different axis and for a different reason, and it means **the answer cannot be got from an expansion at all** — it has to come from row 1 (the theorem) plus rows 10–12 (the measured bulk decay length), or from a simulation.

---

## 3. What this settles for `T-50`, and what it does not

### Settles

1. **`g` is a bulk quantity.** Row 1, corroborated independently by rows 6–8. Nothing about the tile's charge convention, the Manning fraction, the rim charge, the Stern layer, image charges, or the wall's finite ion size can appear in `g` at large `κh`. This retires a large fraction of the parameter uncertainty `C-0005` carries, *for this one question*, without a single solve.
2. **The bulk decay length of 0.5–10 mM MgCl₂ is `λ_D`, on four independent methods.** Rows 10–12. The device sits at `dκ_D = 0.089–0.488`, inside the window where MSA-IET, two DFTs and MD all agree that `ξ_Z ≈ κ_D⁻¹`.
3. **The Kirkwood crossover is 6.3–12× away.** Row 14. There is no oscillatory-decay regime to worry about anywhere in the specified buffer range.
4. **Bikerman contributes exactly zero to `g`.** Row 41, one line of algebra on a published equation.
5. **Mean field is *better* behaved for oppositely charged walls, and this is quantified and MC-checked at `Ξ` up to 86.** Rows 25–28. The `Ξ = 17–24` alarm in `C-0005` is calibrated on the like-charged problem; the device is not that problem.
6. **The measured deviation from DLVO in divalent electrolyte is short-ranged (`λ ≈ 1 nm`) and an amplitude effect at long range.** Rows 31–37. A 1 nm extra exponential *is* a `g`-relevant object at a 2 nm gap and is essentially invisible at 7 nm — which is where the threshold is written.

### Does not settle

1. **Which wall the coupling criterion is owed at.** Rows 29 vs 30 differ by 16.5× in `Ξ` and land on opposite sides of `Ξ < D̃/ln D̃`. This is a `CLAUDE.md`-class question (*"read `Ξ` from the duplex cylinder, not the projected one"* — but the criterion is written for a planar wall) and this file cannot answer it.
2. **The device is not in the asymptotic regime everywhere.** Row 1's theorem is an `h → ∞` statement. At `κh = 0.51` (2 nm gap, 2 mM) the force is nowhere near its asymptote, and the theorem is silent there. `CLAUDE.md` already records that this project measures `ℓ/λ_D = 0.910–0.983` at 2 mM at 17–26 nm and `0.649–0.819` at 0.5 mM — i.e. the far field is *not* reached at the small end. **The `T-50` threshold at 7 nm / 2 mM is `κh = 1.78`, which is the marginal case.**
3. **No published MC exists for this exact problem.** Rows 44–47: everything found is like-charged, or salt-free, or 2:2 rather than 2:1, or cylinders. The nearest is Kanduč 2009, which is oppositely charged and at the right `Ξ` but has no salt and therefore no decay length. `C-0005`'s 1–3 week calculation is not superseded by the literature.
4. **No experimental decay length in a divalent electrolyte at mM concentration was recovered as a NUMBER.** Rows 31–35 are statements of agreement with `λ_D`, not tabulated decay lengths. Pashley & Israelachvili 1984, the obvious primary source, is closed (row 38).
5. **Whether `q^eff(κ) < q` for aqueous Mg²⁺ at these concentrations.** Row 4 makes that the exact and unique criterion for the sign of the deviation, and it is computable from DIT — but no source found evaluates it for a 2:1 aqueous electrolyte at 0.5–10 mM.

### The cheap next step this file suggests

**Evaluate row 4.** `(κ/κ_DH)² = q^eff(κ)/q` is one scalar per buffer, and it decides the *sign* of `g` before any solve, exactly as `C-0008`'s saturation bound decides a charge question before a solve. Failing that, the operational answer is rows 10–12: within the primitive model, at this device's `dκ_D`, `g = 0` to the resolution of the four methods that have been used to look.

---

## 4. The verbatim passages

### Q1 — Härtel & Kjellander, arXiv:2412.01653v1, p. 1, opening paragraph

> *"Surface forces between two macroscopic bodies decay for large separations with the same decay length as in the bulk phase in contact with the surfaces, but the amplitude and, for oscillatory forces, the phase depend on the properties of the bodies."*

And on the exact theory, p. 1:

> *"The dressed ion theory (DIT) is an exact reformulations of the statistical mechanics of simple electrolytes [2, 3] that has been generalized to ionic liquids and other fluids (see below). It gives the actual decay length 1/κ of the electrolyte from the following exact equation for the decay parameter κ … where q_j^eff is the effective charge of any of the j-ions (not only the central one). Its value differs from q_j^eff,DH. **The only difference between κ_DH and the exact κ is that one of the factors q_j in the equation is replaced by q_j^eff.**"*

And the sign criterion, p. 2:

> *"Eqn (1) for κ can in this case be written (κ/κ_DH)² = q^eff(κ)/q, which means that underscreening (i.e., κ < κ_DH) occurs when q^eff(κ) < q. **This is the precise and unique condition for underscreening to happen in the present case.**"*

### Q1 corroboration — Cats, Evans, Härtel & van Roij, arXiv:2012.02713v2, §IV C

> *"Allowing the radius of the big solute particle to become infinite we recover the case of two planar walls and then the potential of mean force yields the solvation force, or excess pressure. Since we have calculated the (bulk) charge and number decay lengths as a function of concentration, and examined the competition between these, we know the ultimate decay of the (thermodynamic) solvation force for each concentration. We denote the corresponding length scale as ξ, which represents the true correlation length in the liquid. The upshot is that the solvation force should decay as*
> *f_s(H) ∝ cos(2πH/λ + φ)e^(−H/ξ), H → ∞, (49)*
> *where ξ is the longest decay length in the system."*

§IV B (the operational check that the decay length is not a surface property):

> *"We confirmed that the results for the various decay lengths in DFT were independent of the surface potential Φ₀."*

### Q2a — Cats et al., §V (Summary and Discussion)

> *"At low concentrations, dκ_D < 0.5, all the approaches we consider agree that ξ_Z is close to κ_D⁻¹ and ξ_N is close to ½κ_D⁻¹, except for the GMSA results for ξ_N."*

§IV B 2 (the direction of the first correction):

> *"As predicted, ξ_Z in Fig. 4(a) extracted from the MFC functional (red line) is given by the Debye length for all concentrations. At very low concentrations, dκ_D ≪ 1, the true decay length must converge to the Debye length for all theories, as dictated by the limiting law. Precisely how ξ_Z κ_D approaches unity at dκ_D = 0 is important and we return to this later. **At intermediate concentrations (dκ_D > 0.5), the limiting law is no longer valid and ξ_Z is found to be smaller than the Debye length.**"*

Same section, the Kirkwood points:

> *"The kinks that are observed for the DFT results indicate that the Kirkwood transition occurs at (using the notation x = dκ_D) x_K^MSAc ≈ 1.24 and x_K^MSAu ≈ 0.7004 while the MSA IET value is x_K^IET ≈ 1.229 … There is an indication within MD of a Kirkwood point at around dκ_D ≈ 1.37…"*

§V, on asymmetric electrolytes:

> *"Although our focus was on the RPM throughout, we also performed PM calculations (not reported here) with various ionic valency and diameter asymmetries. The resulting asymptotic decay properties are very similar to those of the RPM reported here. We find no long decay lengths…"*

### Q2b — Cats et al., §II A 2. **The disavowal of `2Γ` as a screening length**

> *"The parameter 2Γ reduces to the inverse Debye length κ_D in the limit dκ_D → 0. However, whereas κ_D⁻¹ plays the role of a screening length in the dilute limit as we will see, **1/2Γ is merely an intermediate parameter of the theory and should not be regarded as a physical screening length.**"*

The equation the closed form is derived from (their Eq. 16, with `η = 0` for the RPM):

> *"Γ² = πλ_B [2ρ_b − 2d²qη + 2ρ_b d⁴η²]/(1 + dΓ)²"*

attributed by them as:

> *"…was found by Blum and others in the 70's²⁶⁻²⁹ building upon the pioneering work of Waisman and Lebowitz³⁰⁻³²."*

### Q2c — Lee & Fisher, arXiv:cond-mat/9705235, Eq. (5) and reference list

> *"By analyzing the poles of (1) we may obtain the predicted large-distance behavior of G_ZZ(r; ρ_N, T): from that we find that simple exponential decay persists only up to x ≡ κ_D a = x_K; for x > x_K the decay is oscillatory. Numerically we obtain the 'Kirkwood value' x_K ≃ 1.17832, which lies in the usually expected range [4, 5, 6, 7]."*

> *"[5] Kirkwood J.G., Chem. Rev., 19 (1936) 275, found x_K ≃ 1.03."*
> *"[6] Outhwaite C.W. … the linearized modified Poisson-Boltzmann (MPB) theory yields x_K ≃ 1.241."*
> *"[7] Leote de Carvalho R.J.F. and Evans R., (a) Molec. Phys., 83 (1994) 619 … analysis of the GMSA gives x_K ≃ 1.228."*

### Q3a — Kanduč, Trulsson, Naji, Burak, Forsman & Podgornik, arXiv:0905.3851, §V C

> *"As an approximate measure for the validity regime of this scheme, one can require that the second-order correction term is smaller than the leading order term, i.e. |p̃₂| < |p̃₀|. (63) This leads to a useful criterion identifying the regime of coupling parameters and distances in which the weak-coupling theory is applicable. For p̃₀ > 0 and by employing the closed-form expressions obtained for large separations D̃ ≫ 1, we find the validity condition Ξ < D̃/ln D̃. (64) This indicates that at a given non-vanishing Ξ, the weak-coupling scheme becomes increasingly more accurate at larger separations, while as the surfaces get closer a smaller coupling parameter needs to be chosen."*

### Q3b — same, §V C, the oppositely-charged branch. **This is the sentence Q3 asked for.**

> *"On the other hand, for p₀ < 0 (which occurs for ζ < 0) and at large separations D̃ ≫ 1, we obtain Ξ < (ζ²/|f(ζ)|)e^(−2ζD̃). (65) **The right hand side here is exponentially large meaning that for charged surfaces of opposite sign, the weak-coupling analysis performs far better at finite coupling parameters and smaller inter-surface separations than for the surfaces of equal sign (ζ > 0).**"*

### Q3c — same, §VII, the Monte Carlo

> *"For large enough Ξ > 10, the MC results slowly converge to the SC result. This is especially clear for surfaces with charges of equal sign, ζ = 0.5, whereas **for surfaces with opposite sign, ζ = −0.5, there is no big difference between PB and SC profiles.**"*

> *"In the intermediate regime of coupling parameters, the simulation results for the pressure are clearly bracketed by the two limiting analytical forms, given by the PB plus the second order correction and the SC expressions of the interaction pressure. … **In the case of ζ = −0.5 the difference between the strong and weak coupling results for the rescaled interaction pressure is marginal and the simulation data and the analytical results nearly coincide for all rescaled separations D̃ = D/µ.**"*

Scope caveat, from §I of the same paper:

> *"A proper understanding of the behavior of charged systems would thus start with the analysis of counter-ion distribution around charged macromolecular surfaces, **neglecting completely the effects of salt.**"*

### Q4a — Trefalt, Palberg & Borkovec, arXiv:1606.00266

> *"Let us first discuss the situation of multivalent counterions. The first notable point is that force profiles between particles in solutions containing divalent (and sometimes trivalent) counterions can be still very well described by DLVO theory. Such observations were made with negatively charged particles in the presence of divalent and trivalent cations [33,39] as well as positively charged particles in the presence of divalent anions [34]. Nevertheless, there are two important differences with respect to the monovalent situation. First, the multivalent ions are more effective to induce screening, since their valence is weighted more strongly in the ionic strength. At equal concentrations, multivalent ions induce a substantially smaller Debye length. Second, diffuse layer potentials are lower in magnitude than in the monovalent case. The latter difference is likely caused by specific adsorption of the multivalent counterions to the particle surfaces, which leads to partial charge neutralization, and lowers the magnitude of the surface potential. **This adsorption process is the principal reason why the DH theory actually is – somewhat counter intuitively – a better approximation in the presence of multivalent counterions than in the monovalent setting.**"*

### Q4b — Kumar, Cats, Alotaibi, Ayirala, Yousef, van Roij, Siretanu & Mugele, arXiv:2201.08667

> *"The decay length of these interaction forces decreases with increasing salt concentration in agreement with the theoretical Debye screening length (black solid line in Figure 2b), and the DFT calculations (black crosses in Figure 2b)."*

> *"In both cases, the decay lengths of the forces were consistent with the ones observed for 25 °C and unadjusted pH within the symbol size in Figure 2b. … Yet, more importantly, **the observed asymptotic decay lengths agree with the expected Debye screening length** (crosses in Figure 2b)."*

Abstract:

> *"We performed a set of systematic Atomic Force Spectroscopy measurements for aqueous salt solutions in a concentration range from 1 mM to 5 M using chloride salts of various alkali metals as well as mixed concentrated salt solutions (involving both mono- and divalent cations and anions) … **none of the conditions explored displayed any indication of anomalous long range electrostatic forces** as reported for macroscopic mica surfaces."*

### Q4c / Q6 — Kanduč, Moazzami-Gudarzi, Valmacco, Podgornik & Trefalt, arXiv:1701.08989, §III

> *"The interpretation of these observations is the following: **at large separations the electrostatic potential is rather small and the mean-field behavior is sufficient to characterize the experiment but only if in addition one takes into account that electrostatic interactions between the surface and the counterions reduce the effective surface charge from its bare PB value.** At small separations the situation is altogether different, and ion correlations induce strong attraction, which cannot be captured by the mean-field Ansatz but can be approximated by the phenomenological attractive exponential form…"*

> *"The fit of this additional exponential force shown in Fig. 2a yields A = 45 mN/m and λ = 1 nm."*

The closed form `T-50` wanted, same section:

> *"The long-distance mean-field behavior equivalently results directly from the asymptotic limit of the DI theory. In the limit of large separations, κh ≫ 1, Eq. (8) reduces to f_el(h) ≃ w₀₀(h)K(h), (19) which exhibits exponentially decaying interaction, mildly modulated by the function K(h) of the form K(h) = 1 − c₀(2πℓ_B q²/κ²)(C + κh), (20) with the constant C given by C = 3/2 − 2γ + 2 log κµ + 2κµ e^(1/κµ) + 2Ei(1/κµ). (21)"*

> *"In our experimental cases, the product κµ is typically below 0.2, and therefore for κµ ≪ 1, we can make the approximation C ≃ 2(µκ)² exp(1/κµ)."*

### Q5 — Kilic, Bazant & Ajdari, arXiv:physics/0611030, §I C

> *"There are at least three important lengths in our models. The first is the Debye length λ_D given by (5), **which sets the width of the diffuse layer at low voltage and low bulk concentration, c₀.**"*

§II B, the MPB equation itself:

> *"…the potential satisfies the modified Poisson-Boltzmann (MPB) equation, ∇²ψ = (zec₀/ε)·2sinh(zeψ/kT)/[1 + 2ν sinh²(zeψ/2kT)]. (25) Unlike the composite layer model, the MPB model can be applied to any geometry (just like the PB model). In the case of a flat diffuse layer, it gives a similar description, except that steric effects enter smoothly with increasing voltage, and there is no sharply defined condensed layer."*

§II A, on what steric effects *do* move:

> *"Generally, the condensed layer forms when the diffuse layer voltage Ψ_D becomes only a few times the thermal voltage kT/ze, and then it grows sublinearly, proportionally to the square root of the potential drop as anticipated from Poisson's equation with a constant charge density."*

### Q6 — Crossref abstracts, verbatim

Guldbrand, Jönsson, Wennerström & Linse, *J. Chem. Phys.* **80** (1984) 2221:

> *"Using a novel method the force between two charged surfaces with an intervening electrolyte solution has been determined from Monte Carlo simulations. We find large deviations from the standard Poisson–Boltzmann treatment of the so called double layer force for divalent counterions at high surface charge densities and at short separations. The deviations have two causes: (i) Due to the inclusion of the effect of ion–ion correlations the counterions concentrate more towards the charged wall reducing the overlap between the double layers; and (ii) correlated fluctuations in the ion clouds of the two surfaces lead to an attractive interaction of a van der Waals type. For some realistic values of the parameters the attraction overcomes the repulsive part and there is a net attractive force between similarly charged surfaces."*

Valleau, Ivkov & Torrie, *J. Chem. Phys.* **95** (1991) 520:

> *"Monte Carlo methods are presented for the evaluation of the various components of the force between parallel charged surfaces due to the presence between them of an electrolyte, represented by the restricted primitive model. … The methods are applied principally to 2:2 electrolytes at a variety of surface charge densities. We find, in contrast to DLVO theory, that the force between the surfaces resulting from the electrolyte is almost everywhere attractive…"*

Guldbrand, Nilsson & Nordenskiöld, *J. Chem. Phys.* **85** (1986) 6686:

> *"…It is found that for monovalent counterions, the mean field Poisson–Boltzmann theory can give a reasonable reproduction of the experimental data, even though the simulations show that its description of the electrostatic interaction can be qualitatively wrong. … Furthermore, the simulations show that the correlation between the ion clouds of different DNA polyions gives rise to a significant attractive contribution to the interaction when divalent cations are present."*

---

## 5. Queries run

### arXiv API (`https://export.arxiv.org/api/query`, `curl -sL -G --data-urlencode`)

Note: a first attempt used a hand-encoded query string (`%22…%22`) and returned **0 entries for a query that later returned 4**; every query below used `--data-urlencode`. Recording this because a silently-empty arXiv response reads exactly like a negative existence result.

1. `all:"screening decay length" AND all:electrolyte` — 0
2. `au:Kjellander AND abs:screening` — 0
3. `abs:"decay length" AND abs:"electrolyte" AND abs:"Debye"` — 4
4. `au:Kjellander_R` — 0
5. `all:Kjellander AND all:electrolyte` — 0
6. `abs:"dressed ion"` — 5 (gave arXiv:1701.08989, arXiv:1604.02301)
7. `ti:"decay of correlations" AND abs:ionic` — 0
8. `all:"Kjellander"` — 4 (gave arXiv:2412.01653, **the Q1 source**)
9. `abs:"surface forces" AND abs:"decay length" AND abs:"screened"` — 4
10. `abs:"underscreening"` — 40 (mostly Kondo physics; gave arXiv:2209.03486, arXiv:2408.15685, arXiv:2106.13197, arXiv:2308.10189, arXiv:2606.15492)
11. `abs:"Kirkwood line"` — 3 (gave arXiv:cond-mat/9705235, **the Q2c source**)
12. `all:"Kirkwood crossover" AND all:"primitive model"` — 1
13. `abs:"mean spherical approximation" AND abs:"screening" AND abs:"primitive model"` — 2
14. `abs:"oppositely charged" AND abs:"strong coupling" AND abs:counterions` — 2
15. `abs:"strong coupling" AND abs:"charged plates" AND abs:"Monte Carlo"` — 4 (gave arXiv:0905.3851, **the Q3 source**)
16. `all:"asymmetrically charged" AND all:"electric double layer"` — 0
17. `abs:"decay length" AND abs:"divalent"` — 0
18. `abs:"surface force" AND abs:"CaCl2" OR abs:"MgCl2"` — 0
19. `abs:"colloidal probe" AND abs:"screening length"` — 2 (gave arXiv:2201.08667, **a Q4 source**)
20. `abs:"steric effects" AND abs:"electrolytes" AND au:Bazant` — 3 (gave arXiv:physics/0611030, **the Q5 source**)
21. `au:Trefalt AND abs:forces` — 7 (gave arXiv:1606.00266, arXiv:1701.08989)
22. `abs:"double layer" AND abs:"Monte Carlo" AND abs:"divalent counterions"` — **0**
23. `abs:"charged plates" AND abs:"divalent" AND abs:"pressure"` — **0**
24. `all:"double layer forces" AND all:"Monte Carlo" AND all:"electrolyte"` — **0**
25. `abs:"oppositely charged" AND abs:"planar" AND abs:"electrolyte" AND abs:"simulation"` — **0**
26. `abs:"asymmetric electrolyte" AND abs:"slit" AND abs:"Monte Carlo"` — **0**
27. `abs:"2:1 electrolyte" AND abs:"double layer"` — 1 (arXiv:1603.02445, anisotropic ion shape — not a decay-length study)
28. `abs:"coupling parameter" AND abs:"Xi" AND abs:"electrostatic" AND abs:"intermediate"` — **0**

### Crossref (`https://api.crossref.org/works`)

29. `query.bibliographic=DLVO and hydration forces between mica surfaces in Mg2+ Ca2+ Sr2+ Ba2+ chloride solutions Pashley Israelachvili` — 5 hits, **no abstract on any**
30. `query.bibliographic=Electrical double layer forces A Monte Carlo study Guldbrand Jonsson Wennerstrom Linse` — 3, abstract recovered
31. `query.bibliographic=Colloid stability the forces between charged surfaces in an electrolyte Valleau Ivkov Torrie` — 3, abstract recovered
32. `query.bibliographic=Kjellander Marcelja correlation and image charge effects in electric double layers` — 3, **no abstract** on the two 1984/1985 CPL papers
33. `works/10.1063/1.446912` — full abstract
34. `works/10.1063/1.461452` — full abstract
35. `works/10.1063/1.451450` — full abstract

### Web search (used only to locate candidate URLs, never as a source of a number)

36. `Kjellander Mitchell "dressed ion theory" decay length "same for all" surfaces electrolyte exact statistical mechanics`
37. `Kjellander Soft Matter 2019 15 5866 "intimate relationship" dielectric response decay intermolecular correlations surface forces electrolytes open access pdf`
38. `arXiv "screening length" restricted primitive model "mean spherical approximation" Gamma "1+2 kappa sigma" square root closed form Waisman Lebowitz`

### Full-text fetch routes

- `https://arxiv.org/pdf/<id>` + `pdftotext -layout` — **worked first time on 10 of 11 attempts** (`2209.03486`, `2412.01653`, `2408.15685`, `cond-mat/9705235`, `2203.15428`, `0905.3851`, `2012.02713`, `2201.08667`, `1701.08989`, `1606.00266`, `2606.15492`). One PDF (`2308.10189`) has a broken xref that `pdftotext` refused (`xref num 310 not found`) — not retried, it was not load-bearing.
- `https://pubs.rsc.org/en/content/articlehtml/2024/cp/d4cp00546e` — **403** to WebFetch
- `https://pubs.rsc.org/en/content/articlepdf/2024/cp/d4cp00546e` — **403** to `curl` with a browser User-Agent, despite being listed as an Open Access Article
- `https://pubs.rsc.org/en/content/articlehtml/2019/sm/c9sm00712a` (Kjellander's *Soft Matter* review) — **403** to `curl` with a browser User-Agent
- `https://gup.ub.gu.se/search?query=…` (Gothenburg's repository, Kjellander's own institution) — 200 but no publication links parsed out; not pursued further

---

## 6. What is NOT found

1. **Kjellander & Mitchell's primary dressed-ion papers** (*Chem. Phys. Lett.* **200** (1992) 76; *J. Chem. Phys.* **101** (1994) 603) and **Kjellander's *Soft Matter* **15** (2019) 5866 review** were not obtained. AIP and RSC both refuse `curl` and WebFetch; Kjellander has essentially no arXiv presence (query 8 returns four papers, only one of which is his on this topic). **The Q1 theorem is therefore quoted from Kjellander's own 2024 Faraday Discussion contribution (arXiv:2412.01653), which is a published secondary statement by the theorem's author** — not from the 1992/1994 originals. It is corroborated independently by Cats et al. 2021 (row 6), read directly.
2. **No published Monte Carlo of a primitive-model slit at `Ξ = 15–25` with divalent counterions AND added 2:1 salt, giving pressure versus separation over 2–10 nm.** Queries 14–16, 22–28. Everything found is one of: like-charged walls (Guldbrand 1984, Valleau 1991), 2:2 rather than 2:1 (Valleau 1991), cylinders rather than a slit (Guldbrand 1986), or salt-free (Kanduč 2009). **`C-0005`'s 1–3 week calculation is not available off the shelf.**
3. **No published value of `q^eff(κ)/q` for aqueous MgCl₂ at 0.5–10 mM.** This is the single scalar that, by row 4, decides the *sign* of the deviation of the true decay length from Debye, exactly and uniquely. Nothing found evaluates it for a 2:1 aqueous electrolyte in this concentration range.
4. **No tabulated experimental decay length in a divalent electrolyte at millimolar concentration.** Rows 31–35 are *statements* that the measured decay agrees with `λ_D`; no source found publishes the fitted decay lengths alongside the Debye values as numbers for Mg²⁺ or Ca²⁺ at 0.5–10 mM. Pashley & Israelachvili 1984 (*J. Colloid Interface Sci.* **97** 446) is the obvious primary source, is located by DOI, has **no Crossref abstract**, and Elsevier is closed. Queries 17–19, 21, 29.
5. **No source found states the sign or size of a Bikerman correction to the double-layer FORCE DECAY LENGTH.** Kilic–Bazant–Ajdari discuss capacitance and the condensed-layer thickness; Borukhov–Andelman–Orland was not fetched. Row 41's `g_Bikerman = 0` is **DERIVED** from their published Eq. (25), not quoted from anyone.
6. **The Attard 1993 (*Phys. Rev. E* **48** 3604) and Ennis, Kjellander & Mitchell 1995 (*J. Chem. Phys.* **102** 975) asymptotic-analysis papers were not obtained** — both are named in Cats et al.'s Fig. 6 as sources of independent HNC/GMSA decay lengths (their refs. 17, 18, 19), which is how their existence and role are known here. Neither is on arXiv.
7. **Leote de Carvalho & Evans, *Mol. Phys.* **83** (1994) 619 was not obtained.** Its `x_K ≃ 1.228` reaches this file through Lee & Fisher's reference [7] (READ DIRECTLY) and through Cats et al.'s `x_K^IET ≈ 1.229` (READ DIRECTLY) — two independent transcriptions agreeing to 0.1 %, which is why row 9 is quotable at all.
