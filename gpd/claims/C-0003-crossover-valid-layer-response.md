# C-0003 — The Gen-1 layer response from a crossover-valid free energy, and the height relation that replaces Alexander-de Gennes

| | |
|---|---|
| **Task** | [`T-1c`](../tasks/T-1c-crossover-valid-layer-response.md) |
| **Leaf** | `A2.1` |
| **Verification type** | in-silico (analytic derivation + numeric minimisation), closed against published measurement |
| **Verdict** | **PASS** — all six acceptance predicates discharged |
| **Maturity** | **TRL 1–3. The interaction free energy is anchored to published osmometry; nothing about *this* layer is measured.** |
| **Provenance** | `gpd/results/T-1c-crossover-valid-layer-response.json`, produced by `brush.CrossoverLayerStudyKt`; 183 design points × 6 models |
| **Conditions** | T = 300 K, aqueous, `k_BT = 4.142 pN·nm`; 40 × 40 nm tile (A = 1600 nm²); linear PEG |
| **Consumes** | [`C-0002`](C-0002-peg-material-parameters.md) (`v₀`, `b`, `n_K`, `α`), and published `A₂`, `A₃` for PEG/water |
| **Raises** | [`CH-0002`](../challenges/CH-0002-corrections-do-not-all-soften.md) against `C-0001` |
| **Challenged** | **[`CH-0010`](../challenges/CH-0010-brush-height-is-coil-height.md) — upheld in substance, 2026-08-13.** The response numbers stand; the height relation, `N(L₀)` and the `φ/φ#` placement do not. See the banner below. |
| **Resolves** | [`CH-0001`](../challenges/CH-0001-semidilute-premise.md) — see the verdict below. Corroborates [`CH-0003`](../challenges/CH-0003-blob-stack-height.md) from a third direction. |

> ⚠️ **The height relation, the chain length and the volume fraction below are challenged by
> [`CH-0010`](../challenges/CH-0010-brush-height-is-coil-height.md) (2026-08-13), raised by
> [`C-0011`](C-0011-scf-density-profile.md).** A numerical SCF density profile against *this claim's own*
> interaction free energies finds that **both profile models here omit the chain's entropic resistance
> to confinement**, which at Gen-1 densities is not a correction to the disjoining pressure but the whole
> of it: at one and the same chain (`N = 62.1`, `σ = 0.024 nm⁻²`) both models put the resting height at
> ~2.16 nm and predict the tile floats free at 10 nm, while the solved profile has it carrying **78 pN**
> there. `L₀` is **not** linear in `N` — the solved exponent is **0.49–0.64** — and `N(10 nm, 0.024)` is
> **62.1** against the 224.8–374.3 below, of which most but not all is the differing definition of
> "layer height". **The stroke and the secant stiffness below are NOT challenged and land inside their
> own brackets.** Do not consume the `N`, `φ` or `φ/φ#` columns without reading `CH-0010`.

---

## The claim, in one line

**A grafted layer has no chain translational entropy, so the exponent of its restoring pressure is
`2.00–2.56`, never the `1.66–1.92` of the bulk equation of state; and the height relation that follows from
minimising the corresponding free energy against Gaussian elasticity on the *measured* Kuhn parameters is**

&nbsp;&nbsp;&nbsp;&nbsp;**`L₀ = 0.1867 · N · σ^(5/13) nm`** &nbsp;(σ in nm⁻²; des Cloizeaux interaction, strong-stretching profile)

**with the alternative measurement-anchored interactions giving `0.1246 N σ^(1/3)` (two-body, strong stretching),
`0.1455 N σ^(5/13)` (des Cloizeaux, box) and `0.0926 N σ^(1/3)` (two-body, box) —
against the Alexander-de Gennes convention `0.1738 N σ^(1/3)`, whose unity prefactor is worth an excluded
volume 6.6× the measured one.**

---

## What replaced what

| `C-0001` used | `C-0003` uses | why |
|---|---|---|
| `Π ∝ φ^m`, `m` chosen from {9/4, 2, 3} | an interaction **free energy** `f_int(φ)`, from the measured EOS with the translational term removed | the exponent is a consequence, not a setting |
| `L₀ = N a^(5/3) σ^(1/3)`, prefactor 1 by convention | a free-energy minimum with a **derived** prefactor | the convention is worth `v = 81 Å³` against a measured `12.25 Å³` |
| blob elasticity, implicit in the blob construction | Gaussian elasticity on `b = 1.1 nm`, `n_K = 3.11` | the chain contains **0.06** thermal blobs — it is not swollen |
| `a = 0.35 nm` doing duty as a volume | `v₀`, `b`, `n_K` kept separate throughout | `C-0002` |
| `Σ ≥ 5` as the brush criterion | `L₀/R₀ ≥ 1`, reported as a number at every design point | `CH-0001` and `CH-0003` disqualified `Σ ≥ 5` twice over |

---

## The material input, and its provenance

`P-3` could not supply a second virial coefficient — the adopted crossover equation of state is non-virial by
construction. It was found by reading the primary literature, per the research practice in `CLAUDE.md`.

| symbol | value | unit | provenance |
|---|---|---|---|
| `A₂` | 1.9 × 10⁻³ | mol·cm³/g² | **MEASURED** — Hasse, Kany, Tintinger & Maurer, *Macromolecules* **28**:3540 (1995), Mw = 6902, 25.2 °C. Read in the re-tabulation of Shvets, arXiv:2010.08110 Table 2.3 — **SECONDARY, flagged** |
| `A₃` | 2.0 × 10⁻² | cm⁶·mol/g³ | **MEASURED** — Kany (Diss. 1998) via Grünfelder, Diss. Kaiserslautern 2002, Table 4.3, isopiestic, 25 °C — read directly |
| `B = v/v₀` | **0.2029** | 1 | **DERIVED** — `B = 2 A₂ M₀/V̄`, temperature-free |
| `v` | **12.25** | Å³ | **DERIVED** — `B v₀` |
| `C₃` | 1.294 | 1 | **DERIVED** — `A₃ M₀/V̄²` |
| `α` | 0.49 | 1 | **MEASURED** — `C-0002`, Cohen et al. (2009) |

The convention is `Π/(RT) = c/M + A₂c² + A₃c³`, `c` in `g/cm³`, **no factor of two**.
Three further `A₂` values were read directly and bracket the adopted one: **2.34 × 10⁻³** at 20.4 kDa
(Li et al., *Polymer* **80**:205 (2015), Table I, membrane osmometry, 25 °C), **1.715 × 10⁻³** (Kany via
Grünfelder), and **2.1 × 10⁻³** at 20 kDa (Cohen & Highsmith, *Biophys. J.* **73**:1689 (1997), Table 1,
after converting out of their `g/dl` convention). Spread ±15 %.
The molar-mass dependence is reported by four groups as `M^(−0.20)`, `M^(−0.32)`, saturating, and absent;
over 2–20 kDa the whole variation is inside that ±15 %, so it is treated as constant and said to be so.

---

## The numbers

### The height relation, and the chain length it implies

`L₀` is **exactly linear in `N`** for both profile models and any pure power-law interaction — proved as a test,
not observed — so `N(L₀)` is an exact inversion.

| `L₀` | `σ` [nm⁻²] | `N` (`C-0001`, Alexander-de Gennes) | `N` (this claim, bracket) | ratio | PEG [kDa] |
|---|---|---|---|---|---|
| 5 nm | 0.0923 | 63.7 | **67.0 – 119.5** | 1.05 – 1.88 | 3.0 – 5.3 |
| 7 nm | 0.0447 | 113.2 | **123.9 – 213.0** | 1.09 – 1.88 | 5.5 – 9.4 |
| 10 nm | 0.0240 | 199.4 | **224.8 – 374.3** | 1.13 – 1.88 | 9.9 – 16.5 |

The bracket runs from the strong-stretching/des Cloizeaux model (softest interaction demand, shortest chain)
to the box/two-body model. **Every model needs a longer chain than `C-0001` did to reach the same height.**

~~Consequently the layer sits **further** into the crossover than `C-0002` reported: `φ/φ# = 1.40 – 3.51` at the
10 nm point against `C-0002`'s 1.13, and `φ = 0.0326 – 0.0543` against 0.0289.~~
**WITHDRAWN by [`CH-0010`](../challenges/CH-0010-brush-height-is-coil-height.md).** On a solved density
profile the layer sits at `φ = 0.00900` and `φ/φ# = 0.138` — a factor of seven *below* the crossover, with
even the peak of the profile reaching only `0.378 φ#`. The direction of this correction is reversed, and
no part of the layer is semidilute.

### Stiffness, at stated compressions

**Stiffness is still not a single number at the resting height** — `C-0001`'s surprise `S-1` survives the change
of free energy intact, and for the same reason: the box profile opens with finite stiffness and the
strong-stretching profile with none, because its outer edge is diffuse.

At `L₀ = 10 nm`, `σ = 0.024 nm⁻²`, 40 × 40 nm tile:

| compression | `k` [pN/nm], bracket over six models |
|---|---|
| `h = L₀` | **0 – 13.8** (0 for the two strong-stretching models, 9.8–13.8 for the three box models) |
| `h = 0.9 L₀` | **3.3 – 17.7** |
| `h = 0.8 L₀` | **7.0 – 24.0** |
| secant to the 100 pN working point | **16.6 – 26.1** |

`C-0001` quoted `k(L₀) = 7.4 pN/nm` and `k_sec = 20.2 pN/nm` at this point.

### Stroke at the §3 target force, over the 40 × 40 nm tile

| `L₀` | `σ` [nm⁻²] | `C-0001` (m = 9/4) | this claim, bracket |
|---|---|---|---|
| 5 nm | 0.0923 | 0.73 nm | **0.47 – 1.53 nm** |
| 7 nm | 0.0447 | 2.20 nm | **1.55 – 3.21 nm** |
| 10 nm | 0.0240 | 4.95 nm | **3.83 – 6.01 nm** |

**The bracket straddles `C-0001` at every design point.** That is the substance of `CH-0002`.

### Sensitivity to grafting density

`d ln k_sec / d ln σ` at fixed layer height, over `σ ∈ [0.015, 0.1] nm⁻²`: **0.28 – 1.29**.
Still sub-quadratic, still no knee — the same qualitative conclusion `C-0001` reached, over a wider range.

### Sensitivity to the *strength* of the interaction — the `C-0007` question

At fixed layer height, grafting density and compression ratio,

&nbsp;&nbsp;&nbsp;&nbsp;**`N ∝ K^(−1/(m+1))` and `k ∝ K^(+1/(m+1))`, exactly**,

verified to 15 significant figures for both profile models. For the des Cloizeaux exponent `1/(m+1) = 4/13 = 0.308`.
Scaling the interaction free energy over a **16-fold** range at the 10 nm design point moves the stroke only
from **5.81 nm to 4.38 nm** and `k(0.8L₀)` from 7.58 to 17.79 pN/nm.

This is the answer to `C-0007`'s question, and it is a reassuring one: **the layer response is weakly sensitive
to how strong the interaction really is, because the chain length the specified height demands moves against it
and very nearly cancels it.** If the effective interaction inside a brush is half or twice the bulk one, the
stiffness moves by −20 % / +24 % and the stroke by +7 % / −7 %. See the validity range for what would *not* be
weak.

### The design window

Stroke ≥ 3 nm at 100 pN **and** the layer stretched beyond its own coil size (`L₀/R₀ ≥ 1`):

| `L₀` | window in `σ` [nm⁻²] |
|---|---|
| 5 nm | **empty** — all six models |
| 7 nm | **empty** — all six models |
| 10 nm | **empty** under both box models; `[0.018, 0.061]` (SST/des Cloizeaux), `[0.024, 0.055]` (SST/virial), `[0.033, 0.092]` (SST/two-body) |

**The ~10 nm desired stroke is unreachable at 100 pN everywhere in the box, under every model, under every
criterion.** `C-0001`'s headline on that point is confirmed, and now on a free energy that does not rest on a
failed premise.

Whether a window exists at 10 nm is **decided by the profile model, not by the interaction** — which is the
one falsifier stated in advance that fired. The box profile is a restricted trial function and therefore a
variational *upper* bound on the free energy, so the strong-stretching answer is the better one; but its own
premise is the one that fails hardest here. **`T-2` should treat the 10 nm window as possible and not
established, and the thing that would settle it is a numerical SCF profile, not a better interaction.**

---

## Verdict on `CH-0001`: partly dissolved, partly upheld, and one part reversed

`CH-0001` had three grounds and four consequences. They do not all survive.

**Dissolved.** Ground on the *exponent*. `CH-0001` carried the bulk local exponent `m_eff = 1.66–1.92` into the
brush pressure law. That is the wrong quantity. Integrating the measured equation of state gives
`f(φ) = (k_BT/v₀)[(φ lnφ)/N + (4α/5)φ^(9/4)]`, and the first term — the only term that drags the exponent below
9/4 — is the **translational entropy of whole chains**, which grafting removes. The exponent of the grafted
layer's own interaction is `2.00 – 2.56` at rest and up to `2.59` under the target load, across every model and
every design point. **`m < 2` is excluded, not `m = 9/4`.** `CH-0001` itself named this as the way it could fail.

**Upheld, and quantified.** Ground on the *height*. `L₀ = N a^(5/3)σ^(1/3)` is a blob result and could not be
repaired by changing an exponent — exactly as `CH-0001` said. Replaced here. The chain length it implies is
**5–88 % too short**, and the exponent it carries (`σ^(1/3)`) is right only for a **two-body** interaction;
the des Cloizeaux interaction gives `σ^(5/13)`.

**Reversed.** The consequence that *"every correction runs the same way — softer — so `C-0001`'s strokes are
lower bounds"*. They are not: the crossover-valid bracket straddles `C-0001` at all three heights, and the
stiffness at first contact comes out 33–87 % **higher**, not 19 % lower. `CH-0002`.

---

## Where the three disputed height exponents went

They were resolved by exhibiting which free energy produces which, as an executable test:

| interaction, minimised against Gaussian Kuhn elasticity | height |
|---|---|
| two-body `(B/2)(k_BT/v₀)φ²` | `L₀ ∝ N σ^(1/3)` |
| des Cloizeaux `α(k_BT/v₀)φ^(9/4)` | `L₀ ∝ N σ^(5/13)` |

and the third — the Alexander blob construction, which also gives `σ^(1/3)` — is **not a third answer**.
It is the des Cloizeaux interaction minimised against *blob* elasticity, `F_el ∝ h²/((N/g)ξ²)`, and with that
elasticity `σ^(1/3)` is recovered exactly. So the disagreement was never about the interaction. It was about
the elasticity, and **that is a checkable statement about the material**: blob elasticity is right when the
chain is swollen, Gaussian Kuhn elasticity when it is not.

**PEG in water at 300 K is a marginal solvent** — `v = 12.25 Å³` against a monomer volume of 60.4 Å³ — so the
thermal blob is **1222 Kuhn segments, 3799 monomers, 167 kDa**. The whole Gen-1 design space is 60–375
monomers, i.e. **0.02–0.10 of one thermal blob.** The chains are not swollen, and the blob construction does
not apply to them. That is why the height relation is replaced rather than justified.

`CH-0003` reaches the same conclusion from the layer's geometry — the Alexander-de Gennes stack is
`(Σ/π)^(5/6) = 1.47` blobs tall at the conventional onset — and `T-7`'s hydrodynamic screening length reaches
it from a third direction. Three independent routes, one conclusion: **the Gen-1 layer is not a blob stack.**

---

## Validity range

Enforced in code where it can be, and stated with a number where it cannot.

- **`N σ v₀ < h ≤ L₀`.** Below the dry thickness the volume fraction would exceed 1; above `L₀` a
  non-adsorbing layer loses contact and the pressure is zero. Evaluating outside throws.
- **`CH-0010`: the brush criterion adopted here is vacuous against a solved profile.** `L₀/R₀ ≥ 1` is
  satisfied at *every* grafting density in the `T-1d` sweep, including `Σ = πR₀²σ = 0.10`, where the
  coils are ten footprints apart. It must be carried alongside coil overlap `Σ ≥ 1`, not instead of it.
- **The strong-stretching premise is NOT met.** `L₀/R₀` spans **0.387 – 2.27** over the whole
  5–10 nm × 0.002–1.0 nm⁻² box, and **0.83 – 1.07** at `C-0001`'s own 10 nm design point, where the theory
  wants ≫ 1. Both profile models are used outside their premise. The spread between them is a **lower bound**
  on the profile uncertainty, not a full error bar. This is the same finding `CH-0003` reports as
  `L₀/R_F = 1.17–1.25`, and it is the single largest methodological weakness of this claim.
  **And most of that spread is not profile uncertainty at all** — [`CH-0090`](../challenges/CH-0090-the-scaling-estimate-uses-the-exponent-of-a-different-quantity.md),
  raised by [`C-0077`](C-0077-first-moment-chain-length.md): read on **one** functional, the
  first moment `2⟨z⟩`, the box and strong stretching agree on `N` to **0.76 %** where their own
  height conventions put them **28.4 %** apart. A box's `2⟨z⟩` is its own height identically and
  the parabola's is `1/[(p+1)B(p)] = 0.7836` of it, and that Beta constant is most of the 28 %.
  **The bracket below stands as an error bar in its own (edge) convention** — both of its endpoints
  are reproduced to the last digit, 374.374 against 374.3 and 224.402 against 224.8 — but it is
  37× too wide as a statement about how much the *profile family* matters.
- **The free energy is valid at low blob count, and that is why it was chosen.** Nothing in
  `f_int(φ)` refers to a blob: it is a *local* function of the volume fraction, taken from bulk osmometry,
  and the elasticity it is minimised against is Gaussian on the Kuhn scale, which needs no blob either.
  What remains blob-dependent is only the *profile*, not the free energy.
- **The interaction free energy below `φ#` is not measured.** The fitted `αφ^(9/4)` is constrained by data only
  where it dominates the van't Hoff limb. The two limbs are carried as a bracket and they disagree by
  **1.45×** in `Π_int` at the layer's own `φ`. That factor, not an exponent, is the uncertainty.
- **Every osmotic input is a BULK property applied to a BRUSH.** `α`, `A₂` and `A₃` are all bulk-solution
  measurements. `C-0007` reports an SCF fit to neutron reflectivity giving an effective `χ(brush) ≈ 0.60`
  against `≈ 0.372` in bulk — enough to make the two-body excluded volume negative. **This claim is weakly
  sensitive to the magnitude of that shift and strongly sensitive to its sign**: `k ∝ K^(4/13)` while `K > 0`,
  so a factor of two either way is ±20–24 % in stiffness, but a *net attractive* interaction is outside the
  family of free energies used here entirely. `C-0007`'s own source reports the brush still exerts positive
  surface pressure, so the sign does not flip; `P-9` is where it gets settled, and until it does this claim
  states the exposure rather than absorbing it.
  > **ANNOTATION, 2026-08-13 — the exposure is DISCHARGED by
  > [`C-0013`](C-0013-grafted-chi-inapplicable.md) (`P-9`), and the `χ(brush) ≈ 0.60` quoted above is
  > overturned by [`CH-0012`](../challenges/CH-0012-grafted-chi-number.md).** That number is not in the
  > source; it is a ratio taken against a Flory-Huggins `½` when the SCF model that produced it puts its own
  > theta at `0.696`, for an air/water Langmuir monolayer under *lateral* compression. Measured independently
  > by **normal** osmotic-stress compression of grafted PEG at **1.5–2.5× the Gen-1 grafting density**
  > (Hansen et al. 2003, the same fits this project already cites for `a`), the brush-versus-bulk interaction
  > ratio is **0.674 – 1.147**, i.e. `χ_eff = 0.346 – 0.424` against a bulk `0.372`. Through this claim's own
  > `k ∝ K^(4/13)` that is **−11.4 % to +4.3 %** in stiffness and **−1.4 % to +4.1 %** in stroke — a fifth of
  > the ±22 % six-model bracket already carried here. **No number in this claim moves, and the sign does not
  > flip.** `C-0013` also reproduces this claim's 16× sensitivity study from the other direction:
  > `16^(4/13) = 2.3469` against the `17.79/7.58 = 2.3470` reported above.
- **`χ` has a lattice-site convention worth ~2×** (`C-0007`). This claim does not depend on it: `B` is obtained
  from `A₂` directly, and `χ` appears only as a cross-check. On the monomer-site convention the measured `A₂`
  implies `χ = 0.399`; on the water-site convention, `0.450`. Both are marginal-solvent values and both are
  above the `χ = 0.372` that `C-0007` adopts from SAXS. `C-0001`'s cited `χ ≈ 0.45` is unsourced.
- **The compressed strong-stretching profile is the truncated parabola.** The known free-end **dead zone** near
  the wall is not resolved.
- **Medium and chemistry.** `α` fitted at 20 °C in pure water; `A₂`, `A₃` at 25 °C in pure water. The Gen-1
  buffer is 2–10 mM MgCl₂ — `C-0007` shows that changes the layer mechanics by < 0.5 %, so this is discharged.
  §3 also permits a PS→PEG block copolymer, which is not this material.
- **Mechanical only.** No electrostatics, no ion partitioning, no poroelasticity, no tile compliance.
  Rigid tile assumed — see `C-0006` / `CH-0005`.
- **Nothing here is measured about this layer.** `PASS` means model-consistent and traceable.

## Numbers that are cited rather than derived

Flagged, per §7 of the problem definition:

- **`A₂ = 1.9 × 10⁻³ mol·cm³/g²` — CITED, and read in a re-tabulation rather than in the primary source.**
  Hasse et al. (1995) is paywalled and was not read. Three values read directly (2.34, 2.10, 1.715 × 10⁻³)
  bracket it, so the number is corroborated even though its own source was not opened.
- **`A₃ = 2.0 × 10⁻² cm⁶·mol/g³` — CITED**, read directly in Grünfelder (2002) Table 4.3, whose own source
  (Kany, Diss. 1998) was not read.
- `b = 1.1 nm`, `M_K = 137 g/mol` — **CITED**, via `C-0002` (Rubinstein & Colby Table 2.1).
- `α = 0.49` — **MEASURED**, via `C-0002`.
- `χ(298 K) = 0.367` from Pedersen & Sommer (2005) — **CITED**, used only as a cross-check, read directly.

Everything else in this claim is derived from those and from the §3 parameters.

## Challenges

**Raised by this claim:** [`CH-0002`](../challenges/CH-0002-corrections-do-not-all-soften.md) against `C-0001`.

**Standing against this claim:** [`CH-0010`](../challenges/CH-0010-brush-height-is-coil-height.md),
raised 2026-08-13 by [`C-0011`](C-0011-scf-density-profile.md) — **upheld in substance and split**:
the stroke and the secant stiffness survive inside their brackets, the height relation, `N(L₀)`,
`φ` and `φ/φ#` do not, and the `L₀/R₀ ≥ 1` criterion needs `Σ ≥ 1` alongside it.

The other exposure is the bulk-vs-brush interaction parameter,
queued as `P-9`; a result there that reverses the sign of the excluded volume would contradict this claim and
should be raised as a challenge rather than an overwrite.
