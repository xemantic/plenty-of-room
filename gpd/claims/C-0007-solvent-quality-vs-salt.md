# C-0007 — The Gen-1 buffer does not move the polymer layer, and the reason is not that the salt is dilute

| | |
|---|---|
| **Task** | [`P-6`](../tasks/P-6-solvent-quality-vs-salt.md) |
| **Leaf** | none — premise task under `A2.1`, consumed by `A2.2` (`T-3`) |
| **Verification type** | logical + in-silico, closed against published measurement |
| **Verdict** | **PASS** — on **both** branches of the acceptance predicate: the Mg²⁺ coefficient itself **cannot be determined**, and the effect it controls is nonetheless **bounded**, and small |
| **Maturity** | **TRL 1–3. `χ(T)` is fitted to measurement; the salt bound is a ceiling constructed from a published survey, not a measurement of MgCl₂. Nothing about this layer is measured.** |
| **Provenance** | `gpd/results/P-6-solvent-quality-vs-salt.json`, produced by `material.SolventQualitySaltStudyKt`, 40 `SolventQualityTest` tests green |
| **Conditions** | T = 300 K, `k_BT = 4.142 pN·nm`; aqueous, MgCl₂ 2 / 5 / 10 mM **bulk**, 1–66 mM **layer-local** per `C-0005` |
| **Consumes** | [`C-0002`](C-0002-peg-material-parameters.md) (`v₀`, `b`, `v_K`, `α`, `φ`), [`C-0005`](C-0005-mean-field-screening-validity.md) (ion partitioning, counterion inventory) |
| **Raises** | [`CH-0006`](../challenges/CH-0006-solvent-quality-bound.md) against `C-0002` |

---

## The claim, in one line

**Across the Gen-1 buffer range the polymer layer's osmotic modulus — and therefore its stiffness — changes by
less than 0.5 %, and it would take a salt stronger than any in the PEO literature to make it 1 %.
`T-3` may treat the layer's mechanics as independent of the buffer.**

The reason is *not* that 2–10 mM is a small amount of salt. Two things had to be true, and only one of them
is about concentration.

---

## Channel 1 — the mobile ions exert exactly no force, and they are not small

At 10 mM, MgCl₂ supplies 30 mM of mobile ions, a van't Hoff pressure of **0.0748 pN/nm²** — **3.48×** the
0.0215 pN/nm² the polymer layer itself musters at the 10 nm design point. This is not a term one may neglect
on magnitude grounds, and the naive worry (*"ions excluded from the polymer stiffen or soften the layer"*)
is a first-order worry about a first-order quantity.

It cancels **exactly**. At fixed ion chemical potential, ideal ions confined to the water fraction `1 − φ`
contribute a free-energy density

&nbsp;&nbsp;&nbsp;&nbsp;`f_ion(φ) = +k_BT n_s φ`

measured against the reservoir — **strictly linear in `φ`** — and the Legendre transform `Π = φ f′ − f`
annihilates any linear term. Verified numerically to `1e-9` of the ion pressure scale, not argued.

Its physical restatement is a conservation law and is the more useful form: the grafted layer's polymer
volume per unit area is **conserved** under compression, and the excluded-salt energy is proportional to
exactly that volume, so `∫f_ion dV` is **independent of the layer height**. Verified at 4, 7 and 10 nm to
`1e-12`.

> **The buffer exerts no force on the tile at any height, at any concentration, at ideal order.
> Everything the salt does to the layer's mechanics is beyond ideality — which is to say, it is a `χ`.**

That is what makes the rest of this claim the whole answer rather than one term of it.

---

## Channel 2 — solvent quality, and what `χ` for PEG in water actually is

### The measurement, which replaces an unsourced number

`C-0001` carried `χ ≈ 0.45`, flagged as cited; `C-0002` left it *"neither confirmed nor used"*.
**No primary source for it exists.** The `0.44` that circulates attached to PEG/water is
**polystyrene in toluene** — quoted for contrast in the very paper that measures PEG:

> *"For comparison, for polystyrene in toluene, for which the solvent is considered to be a good solvent,
> the chi parameter is about 0.44, so the solvent conditions are indeed very good for PEG in water at low
> temperature."*
> — Pedersen & Sommer, *Progr. Colloid Polym. Sci.* **130**:70 (2005), p. 74, read from the PDF.

The same paper measures PEG/water by SAXS (PEG 4600 in D₂O, 10–100 °C, 1–20 wt %) and fits

&nbsp;&nbsp;&nbsp;&nbsp;**`χ(T) = 1.156 − 235.3/T`**, &nbsp; `a = 1.156 ± 0.002`, `b = −235.3 ± 0.9 K`

| quantity | value | provenance |
|---|---|---|
| `χ(300 K)`, reciprocal-`T` fit | **0.3717** | **MEASURED** |
| `χ(300 K)`, theta-expansion fit (same paper, Eq. 8) | **0.3783** | **MEASURED** |
| `dχ/dT` at 300 K | `+2.614e-3` K⁻¹ | **DERIVED** |
| `dχ/dT` at the 369 K cloud point | `+1.728e-3` K⁻¹ | **DERIVED** |
| `θ` where `χ = ½` | **358.69 K** | **DERIVED** — against the paper's independently fitted 358.85 ± 1.1 K |
| water-molecule volume `v_site` | 0.03002 nm³ | **DERIVED** |
| `v₀/v_site` | **2.010** | **DERIVED** |
| **monomer excluded volume `v`** | **0.03114 nm³** | **DERIVED** — `v = v₀(v₀/v_site)(1 − 2χ)` |
| `v` from the independent `B₂(T)` fit | **0.02685 nm³** | **DERIVED** — 16 % away |
| thermal-blob volume fraction `φ** = v/v₀` | **0.516** | **DERIVED** |
| blob prefactor `C` implied by the measured `α = 0.49` | **0.40** (0.45 via `B₂`) | **DERIVED** |

**`χ ≈ 0.45` is falsified as a bulk value.** It is 0.078 away from the measurement, and the excluded volume
it implies is **2.6× too small**. Nothing in the project depended on it; the recommendation is deletion,
not correction.

### The lattice trap, which is `C-0002`'s `a` trap wearing different clothes

The measured `χ` is defined on a **water-molecule** Flory-Huggins site. Feeding it into the familiar
`v = v₀(1 − 2χ)` understates the monomer excluded volume by `v₀/v_water = 2.010`. The error is caught by the
cross-check rather than by inspection: with the correct convention the `χ` route and the independent `B₂`
route agree to **16 %**; with the naive one they disagree by **42 %**.

### The transfer function, and where `dχ/dT` is read

A salt that depresses the cloud point by `ΔT_cp` supplies at the cloud point exactly the `Δχ` that
temperature would otherwise have had to supply, because the phase boundary is a locus of constant total `χ`:

&nbsp;&nbsp;&nbsp;&nbsp;**`Δχ_salt = −(dχ/dT)|_{T_cp} · ΔT_cp`**

read at the **cloud point**, not at 300 K — they differ by 51 %. Then, because `(1 − 2χ) = 0.257` is a small
difference of two numbers near ½:

&nbsp;&nbsp;&nbsp;&nbsp;**one kelvin of cloud-point depression costs 1.35 % of PEG's excluded volume.**

`Δα/α = ¾ · Δv/v` (blob) or `Δv/v` (mean field, the bound); `ΔL₀/L₀ = ⅓ · Δv/v`;
and `ΔK/K = f_dC · [(1 + Δv/v)^e − 1]` where `f_dC = 0.715–0.745` is the des Cloizeaux share of the modulus
at the four `C-0002` design points — the van't Hoff limb is chain translational entropy and knows nothing
about solvent quality, so the modulus is always **less** sensitive than the excluded volume.

---

## The Mg²⁺ coefficient — **not determined, and probably not well posed**

**It could not be sourced, and the search is documented below.** Worse than absent: the one study that
measured it says it is not a slope. Boucher & Hines, *J. Polym. Sci. Polym. Phys. Ed.* **14**:2241 (1976),
abstract retrieved verbatim from Crossref:

> *"The major findings are that sulfates and carbonates are much more effective in reducing θ than the
> chlorides and nitrates at the same concentrations. … **Exceptional are the chlorides of Group II and LiCl
> which show minima when θ is plotted against molar salt concentration.**"*

Group II chlorides are MgCl₂/CaCl₂/SrCl₂/BaCl₂. A **non-monotonic** `θ(c)` means a single linear `k_s` is not
a well-defined quantity for MgCl₂, so the missing measurement is not a number but a curve.

**The sign asserted by §2 is not supported for this salt.** Sadeghi & Jahani, *J. Phys. Chem. B*
**116**:5234 (2012), abstract retrieved verbatim from EuropePMC:

> *"the investigated electrolytes are KCl, NH₄Cl, **MgCl₂**, (CH₃)₄NCl, NaCl, NaNO₃, Na₂CO₃, Na₂SO₄, and
> Na₃Cit … Aqueous solutions of PPG400 form aqueous two-phase systems with all the investigated salts;
> however, **other investigated polymers form aqueous two-phase systems only with Na₂CO₃, Na₂SO₄, and
> Na₃Cit**. … the salting-in effect results from **a direct binding of the cations to the ether oxygens of
> the polymers**."*

PEG + MgCl₂ has **no binodal at all**, which is also why no ATPS-derived salting-out coefficient can exist.
MgCl₂ is placed on the **salting-in** side. §2's *"kosmotropic salts drive it toward poor-solvent
conditions"* is correct for sulfates, carbonates and phosphates and is **not established for MgCl₂** — the
one salt the Gen-1 buffer actually contains.

---

## The bound, in two constructions

### The ceiling

Boucher & Hines surveyed sulfates, carbonates, nitrates and chlorides at molar concentrations and report `θ`
*"mostly between 300 and 360 °K"* against a salt-free `θ = 369 ± 3 K` (their 1978 companion,
*J. Polym. Sci. Polym. Phys. Ed.* **16**:501, abstract verbatim from Crossref). The largest depression
anywhere in that survey is therefore ≤ 69 K, at ≥ 1 M — so **`k_s ≤ 69 K/M` for the strongest PEO salts**, and
chlorides are explicitly *"much less effective"*. This is a **construction from a verbatim abstract**, not a
citation of a fitted slope, and it is conservative: if the extremes occur above 1 M the true ceiling is lower.

| span | `ΔT_cp` at the ceiling | `Δχ` | `Δv/v` | `Δα/α` | `ΔL₀/L₀` | **`ΔK/K`** | `ΔK/K` mean-field |
|---|---|---|---|---|---|---|---|
| **buffer, 2 → 10 mM** | −0.552 K | **+9.5e-4** | **−0.74 %** | −0.56 % | −0.25 % | **−0.40 %** | −0.55 % |
| **layer-local, 1 → 66 mM** (`C-0005`) | −4.49 K | +7.8e-3 | −6.04 % | −4.56 % | −2.06 % | **−3.30 %** | −4.51 % |
| buffer, at a plausible chloride 20 K/M | −0.160 K | +2.8e-4 | −0.22 % | — | — | −0.12 % | — |
| buffer, if MgCl₂ salts **in** at −20 K/M | +0.160 K | −2.8e-4 | +0.22 % | — | — | +0.12 % | — |

### The threshold — the same result as a falsifiable trigger

**The cloud-point slope MgCl₂ would need for the 2 → 10 mM step to move `v` by 1 % is 92.8 K/M** —
**1.35× above the ceiling that the strongest salting-out salts of PEO reach**, for a salt that forms no
binodal with PEG and that the ATPS literature places on the salting-in side.
A 5 % shift would need 464 K/M.

**This is the falsifier.** Anyone who produces a `k_s` for PEG + MgCl₂ above 93 K/M contradicts this claim
and `T-3` must then carry a buffer-dependent stiffness.

### The concentration the answer applies to

Per `C-0005`, the layer does not sit in the buffer. Bulk salt is **depleted** into it (`K_salt = 0.52–0.77`)
while the tile's counterions **flood** it: the gap-averaged Mg²⁺ from the tile's Manning-surviving charge is
**33 mM at a 10 nm gap and 66 mM at 5 nm**, derived here from `C-0005`'s 1276 e over the 40 × 40 nm
footprint. So the layer-local span is ~**1 → 66 mM**, a factor of 66, not the factor of 5 the buffer suggests.

**And the counterion concentration goes as `1/h`** — the inventory per unit area is fixed by the tile's
charge while the gap shrinks under actuation — so **the layer's ionic environment is a function of the
actuator's own stroke**. Compressing 10 nm → 5 nm doubles it. At the ceiling that is ≤ 1.7 % of the modulus,
i.e. still small, but it is a *stroke-dependent* stiffness term and nothing downstream carries one.

**Caveat, one-sided in a useful direction:** treating a pure counterion cloud as if it were neutral salt is
conservative in magnitude (it has no co-ion) and probably wrong in sign (PEG salting-out is anion-driven,
while cations bind and salt in).

---

## Why the answer does not matter as much as two things it uncovered

The salt shift has to be compared against something. Two comparisons dwarf it.

| comparison | ratio to the whole 2 → 10 mM buffer step |
|---|---|
| **`Δχ` between a bulk chain and a densely *grafted* one** — Lee et al. report `χ(brush)/χ(θ) ≈ 1.2` against `≈ 0.92` for free chains, i.e. `χ ≈ 0.60` vs `0.46`, from an SCF fit to neutron reflectivity | **239×** |
| **The width of the "theta temperature of PEG in water"** — 358.7 K (Flory-Huggins), 369 ± 3 K (cloud points), 373.2 K (virial), 375 K (cited by `C-0002`): a **16.3 K** band | **29×** |

The second means `C-0002`'s `τ = 0.200` is the **optimistic end** of a `τ = 0.164–0.200` band.

The first is the serious one, and it is quoted from the verbatim abstract
(Lee, Kim, Witte, Ohn, Choi, Akgun, Satija & Won, *J. Phys. Chem. B* **116**:7367, 2012, PMID 22616550):

> *"we discovered that the effective Flory-Huggins interaction parameter of the PEO brush chains is
> significantly greater than that corresponding to the θ condition … (i.e. χ(PEO-water)(brush chains)/
> χ(PEO-water)(θ condition) ≈ 1.2), suggesting that … the PEO chains are actually not 'hydrophilic' when they
> exist as polymer brush chains, because of the many body interactions that are forced to be effective in the
> brush situation."*

**`χ ≈ 0.60` is on the far side of θ: negative excluded volume, poor solvent, for the one configuration this
whole project is about.** The same abstract notes the brush still exerts positive surface pressure, because
conformational entropy dominates — so this is not a collapse of the design, but it is a direct challenge to
using *any* bulk solution property for a grafted layer, which is what `C-0002` does and what `T-1c` is doing
now. It is **not incorporated anywhere** and it is queued as `P-9`. Note also that the `0.92` in that same
sentence, giving `χ ≈ 0.46` for free chains, is the most likely origin of the folkloric "χ ≈ 0.45".

---

## Validity range

- **`χ(T)` is fitted to PEG 4600 in D₂O**, not H₂O. D₂O is the poorer solvent, so this is a **lower bound** on
  the solvent quality of PEG in H₂O — one-sided, and in the conservative direction for this claim.
- **The 69 K/M ceiling is a construction from an abstract**, at molar concentrations. Applying it at 2–10 mM
  is an extrapolation of **1.0–1.7 decades**, reported as a number by the code, and Boucher & Hines say the
  relation is **non-monotonic** for exactly the salt family in question. The bound survives this because it is
  an upper bound on a magnitude, not an interpolation of a curve — but it is not a prediction, and it must not
  be quoted as one.
- **The transfer `Δχ_salt = −(dχ/dT)|_cp ΔT_cp` assumes the salt's contribution to `χ` is itself temperature
  independent** between 300 K and the cloud point. That is the standard Setschenow reading and it is the first
  thing to attack if this claim is ever contradicted.
- **The excluded-salt cancellation is an *ideal* result.** It is exact for ions that are excluded from the
  polymer volume and otherwise ideal. Ion-specific effects, finite ion size beyond simple volume exclusion,
  Born penalties and ether-oxygen coordination all live in the `χ` channel, which is bounded, not in this one.
- **Bulk `χ`, applied to a grafted layer.** See above. This is the largest un-discharged premise in the claim.
- **`α`, `v₀`, `b`, `v_K`, `φ` are inherited from `C-0002`** and carry its validity range, including that the
  equation of state is a bulk-solution one fitted in pure water.

---

## The search, including what was not found

**Searched:** Crossref (DOI resolution and abstracts), EuropePMC REST full text and search, PubMed eSummary,
arXiv, OpenAlex, Unpaywall, and web search across ~20 query formulations. Publisher sites answered with 403
(ACS, Elsevier), an identity redirect (Springer), or a bot check (IOP); **only arXiv and EuropePMC delivered
full text reliably.**

**Found and read on disk (`pdftotext -layout`):** Pedersen & Sommer (2005) — the `χ(T)` fit, the theta
temperatures, and the polystyrene/toluene correction; Chudoba, Heyda & Dzubiella (2017), arXiv:1710.09191 —
`B₂ = 2.00 nm³/K (373.2 − T)`, and confirmation that the linear-in-`τ` treatment is a *fitted* form and not
an assumption; Hansen et al. (2003), already in the project.

**Found as verbatim abstracts (Crossref/EuropePMC), used as such and flagged:** Boucher & Hines (1976) — the
Group II chloride minima and the sulfate/carbonate ≫ chloride ordering; Boucher & Hines (1978) — `θ` for PEO
in water = 369 ± 3 K, and a `δθ/δI` correlation that exists **only for 1:1 Na/K salts**; Sadeghi & Jahani
(2012) — no PEG/MgCl₂ binodal, salting-in by cation binding to ether oxygens; Lee et al. (2012) — the grafted
`χ`.

**Looked for and could not find:**

1. **Any cloud-point or `θ` curve for PEG/PEO against MgCl₂ concentration.** The only study that measured it
   (Boucher & Hines 1976) is paywalled and pre-digital.
2. **Any Setschenow / salting-out coefficient for PEG + MgCl₂.** Probably none exists: the ATPS literature
   derives these from binodals, and PEG + MgCl₂ has no binodal.
3. **Any PEG salt study below 50 mM other than one FCS study on NaCl/LiCl/KCl/CsCl.** Every cloud-point and
   ATPS paper found works at 0.1–3 M. **This is the single most consequential gap**, because it is exactly
   where the Gen-1 buffer sits and exactly where a non-monotonic `θ(c)` would have its interesting structure.
4. **`A₂` or `χ` for PEG measured as a function of salt concentration**, for any salt.
5. **A binding constant for Mg²⁺ to PEG ether oxygens in water.** The same gap `C-0005` hit from the
   electrostatic side. The NMR work that measures multivalent-cation binding to PEO (Furó group) is in
   **methanol**. Sadeghi & Jahani's statement that cations bind ether oxygens is qualitative and covers MgCl₂
   by inclusion in their salt list, but supplies no constant. **This is `P-8`'s number and it is not there.**
6. **A primary source for `χ ≈ 0.45` for PEG/water.** None. See above.

---

## Cross-checks passed

1. **Gate 1** — molarity → `nm⁻³` pinned; the blob relation shown dimensionless by scaling all lengths.
2. **Gate 2** — `χ → ½`, `v → 0`, `B₂ → 0` at theta; `v < 0` above it (LCST sign); zero-width step gives zero.
3. **Gate 3** — the excluded-salt pressure vanishes to `1e-9`; the layer's excluded-salt energy is
   height-independent to `1e-12`; salting-in and salting-out are exactly antisymmetric.
4. **Gate 4** — the Legendre transform reproduces the equation of state to `1e-6` and converges at second
   order; the blob relation round-trips to `1e-12` and its logarithmic derivative is exactly 3/4 at every
   prefactor.
5. **Gate 5** — the published theta temperature recovered from parameters that did not fit it (0.2 K);
   two routes to the excluded volume agreeing to 16 %; the measured `α` reproduced to an order-unity blob
   prefactor from two independent inputs (0.40, 0.45); and the good-solvent premise checked against the
   actual material — `φ/φ** = 0.06–0.14`, a factor of 7 of margin.

## Still open — named, not answered

1. **`k_s(MgCl₂)` for PEG.** Not determined and probably not well posed. The missing measurement is named:
   **cloud point or `θ` of PEG/PEO against MgCl₂ concentration below 50 mM.** Bounded here, not answered.
2. **The sign.** Unresolved, and the two available indications point in opposite directions
   (Hofmeister anion ordering vs. cation binding). The bound is symmetric, so no conclusion depends on it.
3. **The grafted `χ`.** `χ(brush) ≈ 0.60` against `χ(bulk) = 0.372` — 239× the entire buffer effect and
   not incorporated. Queued as `P-9`. **This is the largest open premise in the material sheet.**
4. **Mg²⁺/PEG ether-oxygen binding constant in water.** Still missing; `P-8`.

## Challenges

[`CH-0006`](../challenges/CH-0006-solvent-quality-bound.md) is raised **by** this claim against `C-0002`'s
`≤ 0.7 % of τ` bound. None stands against this one.
