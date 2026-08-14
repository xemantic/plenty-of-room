# CH-0049 — The `0.2–0.3` band has no member of the crossover family in it, the fitted range it travels with is 35 % too narrow, and its DOI is wrong

| | |
|---|---|
| **Challenges** | [`C-0002`](../claims/C-0002-peg-material-parameters.md) — its *"the semidilute→concentrated one at 0.2–0.3"*, its validity bullet *"Concentration range. Fitted over 0–50 wt %"*, and the DOI it carries for the adopted equation of state. Through `C-0002`, [`C-0001`](../claims/C-0001-layer-stiffness.md), which introduced the band, and [`C-0018`](../claims/C-0018-maximum-usable-bias.md), which made it load-bearing at 121 of 162 states |
| **Raised by** | [`C-0036`](../claims/C-0036-concentrated-crossover.md), task [`T-21`](../tasks/T-21-concentrated-crossover.md) |
| **Raised** | 2026-08-14, iteration 5 |
| **Status** | **Raised, on three independent grounds.** `C-0002`'s parameter sheet does not move; its *bounding* statements do. Ground 1 is the one that matters. |

---

## The standing statements being challenged

`C-0002`:

> the crossover that binds this layer is the **dilute→semidilute** one, from below, at φ ≈ 0.026 —
> not the semidilute→concentrated one at 0.2–0.3.

> **Concentration range.** Fitted over 0–50 wt %, which contains every `φ` in this claim.

and its citation of the adopted equation of state, carried in code as
`Cohen, Podgornik, Hansen & Parsegian, J. Phys. Chem. B 113:3709 (2009)`, DOI `10.1021/jp8072429`.

---

## Ground 1 — the band is a **reduced density**, and no derived crossover lands in it

`T-21` derives the upper crossover from `C-0002`'s own measured parameters as a one-parameter
family, `φ_c(n) = (v_K/b³) n^(−1/2)`, `n` being the Kuhn segments the correlation blob keeps.
Evaluated on this material, in this project's **physical** volume-fraction convention:

| criterion | `φ` |
|---|---|
| `ξ = ξ_T`, Yamakawa exact `g_T` — **the textbook semidilute→concentrated boundary** | **0.0041** |
| `ξ = ξ_T`, scaling `g_T` — the same boundary, other normalisation | **0.0125** |
| the same, on `C-0007`'s Flory-Huggins excluded volume | **0.0105 – 0.0319** |
| `ξ = b`, one Kuhn segment per blob | **0.141** |
| `ξ = v₀^(1/3)` | **0.395** |

**Nothing in the family reaches 0.2.** The two constructions that do are both readings of the same
textbook expression on the wrong segment:

- `v_m/v₀` = **0.203** — `C-0007`'s parameter-sheet row, challenged separately as `CH-0048`;
- `1 − 2χ` = **0.257** with the measured `χ(300 K) = 0.3717` — which is Rubinstein & Colby's
  eq (5.36) `φ** ≈ v/b³` combined with their eq (5.1) `v = (1 − 2χ)b³`, i.e. the Flory-Huggins
  lattice site taken to **be** the monomer *and* the Kuhn length's cube at once.

R&C's `φ**` is printed in their **reduced** convention `φ = n b³` (their eqs 5.19, 5.21, 5.1 —
read directly from p. 180). For PEG the conversion to the physical fraction is `v_K/b³ = 1/7.09`,
so `0.257` reduced is **0.036** physical. **The cited band is the right expression read in the
wrong convention on the wrong segment, and it is 5.7× to 20× too large.**

This is `C-0002`'s own `a`-trap for the third time — *"never write `a` for a volume"* — and it is
also the trap `CH-0001` caught `C-0001` in when it compared a reduced density against a literature
volume fraction. `C-0001` introduced this band in the same sentence as that error.

**A negative existence claim, with its queries recorded.** No primary published measurement or
derivation places the PEG/water semidilute→concentrated crossover at `φ ≈ 0.2–0.3`. What exists:
a measured `φ‡ ≈ 0.15` (scaling estimate `0.21 ± 0.035`) for **PMMA in chloroform** — a different
polymer, abstract only, convention unchecked (Cheng, Graessley & Melnichenko, *PRL* **102**:157801);
an empirical **kinetic** boundary at ~30–35 wt % for PEG from protein-association rates
(Kozer et al., *Biophys. J.* **92**:2139, read directly), which is not a thermodynamic crossover;
and one set of unsourced lecture slides with a bare `~0.2` tick on a regime axis.
Queries: Crossref bibliographic and DOI lookups; EuropePMC REST on
`"poly(ethylene glycol)" AND "concentrated regime" AND "volume fraction"`,
`"semidilute" AND "concentrated" AND "crossover" AND "poly(ethylene glycol)"`, and five
`fullTextXML`/`?pdf=render` retrievals; five arXiv API queries including
`all:"semidilute to concentrated" AND all:crossover AND all:polymer`; Internet Archive title
searches for de Gennes and for Grosberg & Khokhlov (**not obtained; no claim is made about
what they contain**).

## Ground 2 — the fitted concentration range is 1.5–67.5 wt %, not 0–50

The paper states **no** concentration range. Its Fig. 1 caption names the source (Rand's
tabulation) and the conversion (`V̄ = 0.825 mL/g`); the source data, twelve files for the twelve
molecular weights of the paper's own legend, run **1.5 – 67.5 wt %** — 30 wt % for PEG-300 and
PEG-400, **67.5** for PEG-600, 60 for PEG-1000 and PEG-1500, 54 for PEG-8000, 41.7 for PEG-20000.
Confirmed twice: the rightmost datum of the paper's Fig. 1 sits at `log C ≈ −0.13`, which is
PEG-600 at 67.5 wt % under the caption's own conversion; and Marsh (*Biophys. J.* **86**:2630)
fits the same data over volume fractions to 0.53.

In this project's units the equation of state is therefore supported to **`φ = 0.631`** overall
and to **`φ = 0.491`** at PEG-8000, the molecular weight nearest the Gen-1 chain — against
`C-0002`'s 0.451.

**This runs in the device's favour and is reported anyway**, because it is the axis `C-0018`
actually consumes: the layer's constitutive law is a *fit*, and a fit needs data, not a blob.

Two further readings from the same source, both read directly and both consistent with `C-0002`:
the fitted `α = 0.49 ± 0.01` *is* in the physical `φ = C V̄` convention (the paper's eq 4 collapses
to `C-0002`'s form exactly under `φ ≡ C V̄`), and the paper states **no upper bound at which its
form fails** — its only quantitative domain statements are the `0.2 C#` and `5 C#` crossover
edges `C-0002` already carries.

## Ground 3 — the DOI does not resolve

`10.1021/jp8072429` returns 404 at Crossref. The correct DOI is **`10.1021/jp806893a`**, and the
full title is *"A Phenomenological One-Parameter Equation of State for Osmotic Pressures of PEG
and Other Neutral Flexible Polymers **in Good Solvents**"* — the last three words being a
qualification worth carrying, since `C-0003` establishes that PEG/water at 300 K is *marginal*.

---

## What this challenge does NOT do

- It does not move `C-0002`'s parameter sheet, its `α`, its `φ#`, its `m_eff`, or the identity
  `φ/φ# = 1.085` at `Σ = 5`. Those are untouched.
- It does not overturn `C-0002`'s **conclusion** that the binding crossover for this layer is the
  dilute→semidilute one. It sharpens it: the layer is *also* above the upper crossover, so the des
  Cloizeaux exponent is unwarranted from **both** sides at once, and the measured `φ^(9/4)` limb
  survives as a **fit** with no scaling warrant rather than as a scaling law.
- It does not by itself lower `C-0018`'s ceilings. `T-21`'s propagation does that, and reports the
  direction in both readings.

## Provenance

`gpd/results/T-21-concentrated-crossover.json`, `crossover.ConcentratedCrossoverStudyKt`,
`src/test/kotlin/crossover/ConcentratedCrossoverTest.kt`;
literature retrieved and read directly per the flags above.
