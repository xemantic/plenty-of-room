# T-6 — Validity boundary of mean-field screening at 2 mM Mg²⁺ in the Gen-1 geometry

| | |
|---|---|
| **Leaf** | `A7.4` (`../simulation-task-map/knowledge/program_tasks_feynman_path.csv`) |
| **Problem definition** | §6 task 6; mechanism and sign conventions §1; parameters §3; questions §4(c) and §4(e); process §5, §7 |
| **Verification type** | in-silico (closed-form evaluation of published, MC-validated asymptotic criteria) + logical |
| **Maturity** | TRL 1–3. Model-consistent and traceable. **Not measured.** |
| **Status** | Executed, verified, filed as claim [`C-0005`](../claims/C-0005-mean-field-screening-validity.md); raises [`CH-0004`](../challenges/CH-0004-screening-decay-length.md) |

---

## Formulate

### The question, as a numeric target

For the Gen-1 stack — a 40 × 40 nm DNA-origami tile held 5–10 nm above a biased electrode by a grafted PEG layer, under 2 / 5 / 10 mM MgCl₂ —
produce **the size of the error Poisson-Boltzmann commits**, as a dimensionless number at each working gap,
together with **the geometric boundary** separating the region where PB may be used from the region where it may not,
and the point beyond which explicit ions become unavoidable.

This is a **validity** task, not a force task.
It hands `T-3` (stroke and blocking force) and `T-4` (electrostatic softening and pull-in) a defensible statement of what they may assume.
It does not compute a force, and a force computed here would be outside its own acceptance predicate.

### Acceptance predicate

> Quantified deviation from mean-field, with the boundary stated.

Discharged when all five hold:

1. the deviation is a **number with a definition**, not an adjective — specifically the ratio of the first correction term to the leading term in a convergent-or-not expansion whose parameter is named;
2. it is emitted at every gap in the §3 working range (5 / 7 / 10 nm) and every §3 buffer (2 / 5 / 10 mM);
3. the boundary is stated as a **separation in nm** and a **bias in volts**, both derived, not as a qualitative regime label;
4. the control parameter is computed from the **tile's own derived surface charge density**, not from a literature value for "DNA";
5. every input that is cited rather than derived is listed as such, and the sensitivity of the verdict to it is reported.

### Units, locked

SI, scaled: lengths nm, charge in elementary charges `e`, areal charge density `e/nm²`, number density `nm⁻³`,
concentration mM, potential V, energy pN·nm (`= 1e-21 J`), temperature K.
`k_BT = 4.142 pN·nm` at **T = 300 K**; `k_BT/e = 25.852 mV`; `1 eV = 160.2177 pN·nm`.
Medium **aqueous MgCl₂ buffer**, `ε_r = 78`.

### Geometry and sign conventions, fixed before deriving

Restated from `T-1` and from §1 of the problem definition, because `T-4` depends on the sign and it is easy to flip:

- `z` is normal to the electrode, positive **away** from it, origin at the electrode surface.
- The tile is a rigid plate at height `h`; **"gap" always means the tile-electrode separation `h`**, never the polymer layer height, which happens to be numerically equal at rest.
- The tile carries **net negative** charge (phosphate backbone). Positive electrode bias attracts it toward `−z`.
- Therefore, per §1, the electrostatic contribution to the stiffness is **negative**:
  `k_es = −∂F_es,z/∂z < 0`, and `k_eff(z, V) = k_brush(z) + k_es(z, V)`.
  §1 estimates `|k_es| ≈ F_es/λ_D`. **That estimate is challenged by this task** — see `CH-0004`.
- `σ_s` is always a **magnitude** in `e/nm²`. Signs live in the geometry, never in `σ_s`.
- `Ξ = 2π q³ l_B² σ_s` and `μ = 1/(2π q l_B σ_s)`, both positive by construction.

### Conventions that are traps, fixed here

Three quantities in this literature are written with the same symbol or the same words, and substituting one for another moves a threshold:

| quantity | this project's convention | the other convention in circulation |
|---|---|---|
| Manning parameter | `ξ_M = l_B/b`, **valency-free**; condensation criterion `q ξ_M > 1` | Naji et al. Eq. (28) write `ξ = q l_B τ`, valency **included** — hence their 4.1 and 8.2 for the same DNA |
| lateral counterion spacing | `a_⊥ = sqrt(q/σ_s)`, Naji Eq. (5), used in the Rouzina-Bloomfield range | Wigner-Seitz `a_WS = sqrt(q/(π σ_s))`, `√π = 1.772` smaller, used for the plasma parameter |
| "surface charge density of the tile" | three different numbers — see below | one number, usually the wrong one |

### What is deliberately excluded

No force, no stroke, no stiffness — those are `T-3` and `T-4`.
No explicit-ion simulation: the whole point is to decide whether one is warranted, before spending the CPU.
No charge regulation at the electrode, no dielectric layer of §1, no origami internal mechanics (`T-5`).

---

## Plan

### Method, and the justification against cost

**Chosen: closed-form evaluation of published, Monte-Carlo-validated asymptotic criteria. Cost: milliseconds.**

The candidates, in cost order:

| Method | Cost | Verdict |
|---|---|---|
| **Coupling-parameter analysis against published MC-calibrated criteria** | ms | **chosen** |
| Size-modified (Bikerman) PB, 1-D | minutes | Would sharpen the *electrode* answer only. Deferred — the cheap bound already shows the electrode is Stern-dominated, which Bikerman would confirm rather than overturn. Queued. |
| Anisotropic/nonlinear PB with the real origami charge pattern (FEniCS) | hours | Buys a better mean-field answer. Buys nothing about whether mean field is *valid*, which is the question. Wrong tool for this predicate. |
| Monte Carlo / MD with explicit `Mg²⁺` (LAMMPS, primitive model) | days on 8 cores | The only route that settles the intermediate-coupling regime. **Not run** — and the cheap bound is what says where it would have to be run, and that it is not needed for the tile-electrode force. |

The decisive argument for the cheap bound is not cost, it is **fitness**. The expensive routes are all *better solvers of the mean-field problem or of one specific system*; the acceptance predicate asks for the **size of the error mean field commits**, which is a property of the expansion, and the expansion parameter is a closed-form expression in the surface charge density and the counterion valency. There is no simulation that answers "how wrong is PB" more directly than evaluating the loop parameter of PB.

Where the cheap route genuinely cannot reach — the intermediate-coupling gap `1 < Ξ < 100`, where neither expansion converges — no amount of money buys a closed form either, because *no systematic theory exists*. That is stated as an open item rather than papered over.

### Why the Netz/Naji criteria and not something else

They are the only published criteria that (a) define a single dimensionless control parameter for mean-field validity, (b) state the boundary as an inequality in the geometry, and (c) have been calibrated against Monte Carlo across seven decades of the parameter.
Source, read in full rather than recalled:
**A. Naji, S. Jungblut, A. G. Moreira, R. R. Netz, *Electrostatic interactions in strongly-coupled soft matter*, Physica A 352:131 (2005)**, arXiv:cond-mat/0508767.

`CLAUDE.md`'s research-practice rule paid for itself before a single number was computed: **four arXiv identifiers recalled from memory for exactly these papers all resolved to unrelated articles** — one to a paper on electricity-price risk management. The identifiers were found by querying the arXiv API and the PDFs read with `pdftotext -layout`.

### The premise that has to be checked before the criteria may be used at all

The Naji/Netz results are derived for **two uniformly charged planar walls, point counterions, no salt**.
Our system has salt. Transferring a salt-free criterion into a buffered system is exactly the kind of unchecked premise `CH-0001` was raised about, so it is checked rather than assumed:

**Count the ions in the gap.** The tile's Manning-renormalised charge facing the gap requires 319 `Mg²⁺` for electroneutrality. At 2 mM the bulk buffer puts 9.6 `Mg²⁺` in that volume. The counterion-to-bulk ratio is **33:1 at 2 mM and 5 nm, and never falls below 3.3:1 anywhere in the §3 box**.

So the gap is counterion-dominated by between one and one-and-a-half orders of magnitude, and the salt-free limit is the **appropriate** approximation here, not merely the available one. That check is executed as a test, not asserted.

### The three surface charge densities, and which one `Ξ` is read from

A block of packed duplexes does not have one areal charge density, and the choice moves `Ξ` by a factor of seven:

| reading | value | where it belongs |
|---|---|---|
| duplex cylinder surface, `τ/(2πR)` | 0.936 e/nm² | **local** coupling — what a condensed counterion sits on |
| single row of helices, projected | 2.26 e/nm² | the other reading of §3's "single-layer honeycomb" |
| whole 10 nm tile, projected on its footprint | 6.70 e/nm² | **far field** — the plane a distant electrode sees |

The selection is not a preference. The high projected densities describe the **far field**, and the far field is precisely where PB works; the region where mean field fails is within `a_⊥ ≈ 1.5 nm` of an actual phosphate, and at that range the only surface in sight is the duplex's own cylinder. So `Ξ` is read from 0.936 e/nm², and the others are emitted to show what the answer would have been had the wrong one been used. Note the projected value gives a PB contact density **89× past close packing**, which is its own reductio.

### What would falsify this approach

Stated in advance:

1. **The gap turning out to be bulk-dominated** (counterion:bulk ratio below ~1). Then the salt-free criteria are being used outside their premise and the whole analysis would have to be redone with a salt-screened correlation treatment.
2. **`Ξ` landing below ~1.** Then PB is simply valid, this task is a one-line answer, and the effort is wasted.
3. **`Ξ` landing above ~10³.** Then the *strong-coupling* asymptotics apply, the answer is again a closed form, and mean field is not "approximately wrong" but simply the wrong theory.
4. **The two independent statements of the boundary disagreeing** — the full ratio of Naji Eq. (19) to Eq. (13), against Naji's own closed-form Eq. (20). Disagreement beyond ~20% would mean an equation was mis-transcribed from a PDF, which is the most likely failure mode of a method that reads formulas out of papers.
5. **The derived tile charge exceeding a single M13 scaffold origami** (~14 500 nt). That would mean the packing model is wrong by a factor of two or more.

Outcomes: (1) did not fire — 3.3:1 at worst. (2) and (3) did not fire, and the *reason* they did not is the finding: `Ξ = 17–24` sits in the intermediate gap where **neither** asymptotic theory is available. (4) did not fire — 12.91 nm against 13.52 nm, 4.7% apart. (5) did not fire — 10 718 nt.

---

## Execute

Code: `src/main/kotlin/electrostatics/` — `Electrolyte.kt`, `ChargedSurface.kt`, `DnaOrigamiTile.kt`,
`PolymerLayerPartitioning.kt`, `ConfinedGap.kt`, `MeanFieldValidityStudy.kt`.
Tests, written first and watched fail: `src/test/kotlin/electrostatics/` — 73 tests, all green.

```shell
./gradlew test
./gradlew study -Pstudy=electrostatics.MeanFieldValidityStudyKt
```

Result: [`../results/T-6-mean-field-screening-validity.json`](../results/T-6-mean-field-screening-validity.json) —
3 buffers × 8 surface models × 3 Manning valencies × 18 deviation points × 5 layer volume fractions × 9 gap inventories,
every run parameter and every cited input logged in the file. Deterministic: no timestamp.

New fundamental constants (`ELEMENTARY_CHARGE`, `VACUUM_PERMITTIVITY`, `AVOGADRO_CONSTANT`) live in
`electrostatics/Electrolyte.kt` rather than `Physics.kt` — flagged for promotion once a second task needs them.

---

## Verify

All five gates, executed as tests. Test names carry the gate they discharge.

### Gate 1 — dimensional consistency

- `l_B` is derived from four SI constants, and its **defining property** is asserted rather than its formula: two unit charges at `z = l_B` cost exactly `1 k_BT`, at `2 l_B` exactly `0.5`, and a divalent pair at `l_B` exactly `4`.
- The two definitions of the coupling parameter agree exactly: `Ξ = 2π q³ l_B² σ_s` against `Ξ = q² l_B/μ`, written independently in the implementation.
- `Γ = sqrt(Ξ/2)` holds exactly under the Wigner-Seitz convention — which is what *pins* the "geometrical prefactor of order one" that Naji Eq. (5) leaves free.
- The two lateral-spacing conventions are asserted to differ by exactly `√π`, so the trap is visible instead of latent.
- Nucleotide count is conserved between the packing model and the charge model.
- Unit algebra: `0.936 e/nm² = 0.150 C/m²`; `20 µF/cm² = 1.248 e/(V·nm²)`; mM ↔ nm⁻³ inverts exactly.

### Gate 2 — limiting cases

- `Ξ → 0`: the deviation vanishes, and grows **linearly** in `Ξ` at fixed geometry — because the loop parameter *is* `Ξ`.
- `Ξ → ∞`: the strong-coupling pressure changes sign at exactly `Δ* = 2μ` (Naji Eq. 16) and saturates at `−1`.
- `Ξ ∝ q³` confirmed at `q = 1, 2, 3` — a factor of 8 and 27.
- `φ → 0`: every ion partition coefficient returns exactly 1 and the local Debye length is unchanged. Maxwell-Garnett returns `ε_w` at `φ = 0` and exactly `ε_p` at `φ = 1`.
- Manning: below the condensation threshold (`q ξ_M ≤ 1`) the bare charge survives whole — realised by a hypothetical duplex stretched to 1 nm charge spacing.
- The Debye length scales as `c^(−1/2)`, exactly.

### Gate 3 — symmetry and conservation

- **Charge conservation of the buffer**: `2 n_Mg − n_Cl = 0` at every concentration.
- **Charge conservation across Manning condensation**: condensed + free `= 1` exactly, at every valency; and the *effective* Manning parameter after condensation is exactly `1/q` — the fixed point of the theory, asserted as an identity rather than observed.
- **Electroneutrality of the mean-field profile**: integrating Naji Eq. (9) over the half-space must return `σ_s/q` counterions per unit area. Checked analytically *and* by Simpson quadrature over `[0, 100μ]` plus the analytic tail, agreeing to 1e-9. The tail matters — the profile decays only as `z⁻²` and carries 1% of the coverage beyond `100μ`, so dropping it would have been the error.
- **Electroneutrality of the gap**, counted rather than assumed: this is the check that licenses the salt-free criteria, and it is also the source of the counterion-domination finding.
- **Donnan consistency**: the salt partition coefficient is the stoichiometric geometric mean `(K₊K₋²)^(1/3)`, which is what keeps the neutral layer electroneutral; the local Debye length is tied to it by `1/√K`.
- The counterion count in the gap is invariant under gap height while the dominance ratio falls as `1/h` — charge conservation stated as an invariance.

### Gate 4 — numerical convergence

- The transcendental PB pressure equation (Naji, after Eq. 13) is solved by **bisection**, chosen because on `(0, (π/r)²)` the left-hand side increases monotonically from 0 to `+∞`, so bisection is unconditionally convergent and cannot escape the principal branch — which Newton can, the tangent's pole sitting exactly at the upper bracket. Verified **by substitution into the original equation** at six separations, residual `< 1e-9`, with the root asserted to lie below the pole.
- The large-separation asymptote (Naji Eq. 14) is checked as a **convergence order**, not a tolerance: the relative departure falls as `1/r` — ratios 9.973 then 9.997 across decades — which would catch a wrong asymptote that happened to be numerically close.
- The validity boundary is asserted to satisfy its own definition (`deviation = 1` to 1e-6) and to be bracketed on both sides.
- **Two independent statements of the same boundary agree**: 12.91 nm from the full Eq. (19)/(13) ratio against 13.52 nm from Naji's closed-form Eq. (20), 4.7% apart. This is the anti-transcription-error check named in the Plan, and it is the one that would have caught a mis-read PDF.

### Gate 5 — literature cross-check, with premises checked against the material

- **Naji et al. Table I is reproduced at their own parameters.** For DNA at `σ_s = 0.9 e/nm²`, `q = 2`, `l_B = 7.1 Å` they list `μ = 1.2 Å`, `Ξ = 22.4`. We get `μ = 1.25 Å`, `Ξ = 22.8`. At **our** derived `σ_s = 0.936 e/nm²` and `l_B = 0.7141 nm`, `Ξ = 24.0` — 7% from their tabulated value, the difference being entirely the surface charge density.
- **The published Wigner-crystallisation threshold is recovered in the other direction.** Naji quote `Γ_c ≈ 125 ↔ Ξ_c ≈ 3.1e4`. With `Γ = sqrt(Ξ/2)` that is `Ξ_c = 2 × 125² = 31 250`, within 1% — which is how the Wigner-Seitz prefactor convention gets *pinned* rather than guessed.
- **The Manning parameter of B-DNA** comes out at `ξ_M = 4.20` from `l_B/b` with `b = 0.17 nm` derived from the B-form rise; Naji's Table I lists 4.1 and 8.2 for `q = 1, 2` in the valency-inclusive convention. Agreement to 2%.
- **The B-DNA surface charge density lands on the textbook 0.150 C/m²** to three digits, from the rise and the radius alone.
- **§3's Debye length is confirmed, and now derived.** `λ_D = 3.927 nm` at 2 mM against §3's "~4 nm" — 1.8% apart. §3's number was cited; it is no longer.
- **The premise of the invoked criteria is checked against the actual system, not assumed** — the counterion-domination count above. This is the analogue of what `CH-0001` found missing in `C-0001`.
- **The dielectric-constant sensitivity is reported rather than hidden**: `ε_r = 78` against Naji's 80 moves `l_B` by 2.5% and `Ξ` by 5%, which does not move any verdict; the *hydrated-radius* choice moves `Ξ` from 16.8 to 17.8 across the `Ξ = 17` threshold, and that one is reported as a straddle rather than resolved.

### Not verified, and stated as such

- **The transfer of a like-charge two-wall criterion to an oppositely-charged pair is not justified by any published result.** The coupling parameter is a property of each surface separately and transfers; the *attraction* thresholds (`Ξ > 12`, `Ξ ≈ 17`, `Ξ ≈ 30`) are a two-like-walls result and are explicitly **not** transferred. They are reported because the tile-versus-tile and helix-versus-helix questions are like-charge questions.
- **The intermediate-coupling regime has no systematic theory** — Naji et al. say so themselves. Our `Ξ = 17–24` is inside it. This is a limitation of the literature, not of the implementation, and cannot be repaired by a better closed form.
- **§4(c) is not closed.** The partitioning bound counts only exclusion mechanisms, so it is a *one-sided* bound. PEG-cation coordination runs the other way and no binding constant for Mg²⁺/PEG in water was located. Stated plainly, per §7.
- **Nothing here is measured.** `PASS` means model-consistent and traceable.

---

## Result

Filed as [`C-0005`](../claims/C-0005-mean-field-screening-validity.md).
Raises [`CH-0004`](../challenges/CH-0004-screening-decay-length.md) against the `λ_D`-as-decay-length assumption inherited from §1/§3.

## Feedback into Formulate

- **`T-3` may use PB** for the tile-electrode force at 5–10 nm — but with the Manning-renormalised, saturation-capped effective charge, a **local** screening length of ~1 nm rather than 4 nm, a **Stern-limited** electrode charge above 0.2 V, and a stated factor-of-two uncertainty. Not with the bare charge, not with Gouy-Chapman at 2 V, not with `exp(−h/4 nm)`.
- **`T-4`'s pull-in analysis inherits a sharper `|k_es|`** than §1 estimates, because the decay length is shorter. `CH-0004`.
- **`T-5` inherits an unresolved question**: whether 2–10 mM Mg²⁺ can drive helix-helix attraction *inside* the origami. The Rouzina-Bloomfield range is 1.46 nm against a 2.6 nm interhelical distance — a margin of 1.8×, which is smaller than the spread between our surface-charge models.
- **A new premise task** is warranted on Mg²⁺/PEG coordination, which is the one mechanism that could flip the sign of the §4(c) answer.
- **`T-7b`** (electro-osmotic coupling against drainage, raised by `T-7`/`C-0004`) gets a cheap bound out of the gap inventory — three independent suppressions totalling ~`10⁻³` — offered in `C-0005` as an **argument**, not a verified coefficient. It still needs the tile's hydrodynamic zeta potential and `T-7`'s Brinkman length.
