# TASKS — the queue

Live state of the loop.
Priority rule: **process blockers (`P-*`) outrank science tasks, and cheap bounds outrank expensive calculations.**
This overrides the default pull toward whatever has the best ROI.

Status vocabulary: `TODO` · `IN PROGRESS` · `BLOCKED` · `DONE` · `KILLED` (branch abandoned, with reason).

IDs: `T*` are the eight tasks of the problem definition §6, `P*` are process tasks we raised ourselves.
The `Leaf` column is the NDI `simulation-task-map` ID the work traces to.

## Process blockers

| ID | Task | Status | Notes |
|---|---|---|---|
| P-1 | GPD loop skeleton: `SESSION-PROMPT.md`, `JOURNAL.md`, `TASKS.md`, `gpd/{tasks,results,claims,challenges}` | DONE | Iteration 1 |
| P-2 | Locked units and constants module, plus the machine-readable result envelope every task writes through | DONE | Iteration 1 — `src/main/kotlin/Physics.kt`, and the `StudyResult` envelope that carries units, conventions, validity and every run parameter alongside the numbers |
| P-3 | PEG material parameter sheet: monomer size, Kuhn length, χ(T, salt), excluded volume, mass density, with provenance per number and a `derived`/`cited` flag | **DONE** (iteration 2) | Claim `C-0002`. `a = 0.35 nm` closed (derived + fitted); the measured osmotic EOS adopted; χ named as *not determined* and split off as `P-6`. Raised `CH-0001`. |
| P-4 | Volume-fraction bookkeeping: locate the crossover for *this* layer rather than quoting φ ≈ 0.2–0.3 | **DONE** (iteration 2) | Answered, and the answer inverted the question: the binding crossover is the **dilute→semidilute** one at φ# ≈ 0.026, approached from *below*, not the semidilute→concentrated one. The layer sits at φ/φ# = 1.08–1.23. `C-0002`. |
| P-5 | Decide and defend the brush-regime criterion (`Σ ≥ 5` vs `Σ > 1`) | **RESOLVED in substance, REOPENED in scope** (iterations 2–3) | `Σ = 5` ⇔ `φ = 1.085 φ#` **exactly**, independent of layer height and chain length. So the convention is a real material statement — it just places the layer at the crossover, not in the semidilute regime. `CH-0003` adds a second, independent failure of the same convention: the blob-stack height is `L₀/s = (Σ/π)^(5/6)` identically, i.e. **1.47 blobs at `Σ = 5`**. The criterion should therefore carry *both* `φ/φ#` and the blob count, where each is falsifiable. Formal adoption belongs to `T-1c`. |
| P-7 | Build isolation for concurrent agents: `-PbuildDirectory=<dir>` so parallel runs of one checkout stop racing on `build/test-results` | **DONE** (iteration 3) | Raised as a process blocker mid-iteration: four agents sharing one working tree could not get an authoritative `./gradlew test`, and the failure (`EOFException`, `NoSuchFileException` on the in-progress results binary) looks like a broken test rather than a broken harness. `build-*/` is git-ignored. |
| P-8 | Mg²⁺/PEG coordination constant in water | TODO — **medium**, raised by `C-0005`. PEG's ether oxygens coordinate cations — the mechanism behind PEO polymer electrolytes — and this is the **only** mechanism that could flip the sign of the §4(c) answer. `T-6`'s partitioning bound counts exclusion only, so it is one-sided. **`P-6` searched independently and confirms the number does not exist in accessible literature**: it found the mechanism stated for the right system in water (cation binding to ether oxygens, with MgCl₂ in the salt list) but no constant, and the quantitative multivalent-cation/PEO NMR work is in **methanol**. This needs a paywalled pull or an experiment. |
| P-9 | **The effective `χ` of a *grafted* PEG layer is not the bulk one** — bound it, or declare `C-0002`'s bulk equation of state inapplicable to a brush | TODO — **HIGH, a process blocker beside `T-1c` and above `T-2`**. Raised by `C-0007`. An SCF fit to neutron reflectivity puts a dense PEO brush at `χ ≈ 0.60` (above θ, formally poor solvent) against **0.372** in bulk — `Δχ = 0.23`, **239× the entire salt effect `P-6` was chartered to bound**. Every osmotic number in `C-0001`, `C-0002` and `T-1c` derives from a **bulk-solution** property applied to a brush. The same source reports the brush still exerts *positive* surface pressure, so this is not a collapse — but it is the largest un-discharged premise in the material sheet. **Cheap first step, before any modelling:** the source is currently used **from its abstract only**, and it may concern an *air-water-interface* brush rather than a solid-grafted one — read the body and check its grafting densities against the Gen-1 window. The result may be inapplicable here, which would close the task outright. |
| P-6 | `χ(T, salt)` and the Mg²⁺ salting-out coefficient for PEG/water at 2–10 mM | **DONE** (iteration 3) | Claim `C-0007`, challenge `CH-0006`. **The buffer does not reach the layer's mechanics**: ≤ 0.4 % of the modulus over 2–10 mM. The Mg²⁺ coefficient is **not determinable and probably not well posed** — `θ(c)` shows *minima* for Group II chlorides, and PEG forms no binodal with MgCl₂ at all, so no ATPS-derived coefficient can exist. Bounded by a threshold instead: MgCl₂ would need `k_s ≥ 92.8 K/M`, 1.35× above the ceiling any PEO salt reaches. | Split out of `P-3`, which could not determine it: the adopted EOS is non-virial so yields neither `A₂` nor `χ`, and no source for the Mg²⁺ coefficient was found. Bounded by argument at ≤ 0.7% of `τ`, below the fit uncertainty on `α` — so it does not block anything yet. It will bind if `T-3`/`T-6` need solvent quality as a function of ionic strength. |

## Science tasks

| ID | Task | Acceptance (abridged — full text in `gpd/tasks/`) | Leaf | Status |
|---|---|---|---|---|
| T-1 | Stiffness of the polymer layer under the tile | Number with stated model, parameters, validity range; sensitivity to grafting density reported | A2.1 | **DONE** (iteration 1) — claim `C-0001`, **challenged by `CH-0001`**: numbers stand as *lower bounds*, validity range corrected, `m = 3` excluded |
| T-1c | Layer response from a **crossover-valid** free energy, not a fixed osmotic exponent | Stiffness and stroke re-derived with `m_eff(φ)` from `C-0002`; the Alexander-de Gennes height relation either justified at φ/φ# ≈ 1 or replaced; `N(L₀)` no longer resting on the failed premise | A2.1 | IN PROGRESS (iteration 3) — **top of the science queue**, raised by `CH-0001` and now also by `CH-0003`. Cannot be done by swapping an exponent: `L₀ = N a^(5/3)σ^(1/3)` is itself a semidilute result and `T-1` *inverts* it to get `N`. The free energy must also be valid at **low blob count** (1.48–1.73), and MWC strong stretching is outside its own premise (`L₀/R_F` = 1.17–1.25). |
| T-1b | Free-energy functional of the compressed layer, and the `(48/35)N/g` blob-count identity | Closed form implemented and the identity verified as a test, not as an argument | A2.1 | TODO — merge into `T-1c`, which needs a free energy anyway |
| T-2 | Feasible design window in (grafting density, height, chemistry) | Non-empty region satisfying §4(a)–(d) simultaneously, or a proof of emptiness naming the binding constraint | A2.1 | **BLOCKED by T-1c**. Until then `C-0001`'s band is a *lower bound on width*, not a window. `C-0002` hands T-2 a new candidate binding constraint: the densities that make the brush theory valid (σ ≥ 0.99 nm⁻² at 10 nm) are the ones §4(a) rules out as far too stiff. `C-0004` hands it three more: §4(d) is **discharged** as a non-constraint, tile edge is bounded above at 437 nm by drainage, and the `Σ = 5` lower edge is now challenged twice over. |
| T-3 | Stroke and blocking force vs bias, incl. ionic screening | Stroke ≥ ~3 nm and force ≥ 100 pN at ≤ 2 V, or a demonstration it is unreachable | A2.2 | **UNBLOCKED by T-6**, and heavily constrained by `C-0005`. T-1 says only what the layer does *if* 100 pN is available; T-3 must not inherit it as given. **May use** PB at 5–10 nm with a stated factor-of-two uncertainty, and the Manning-renormalised charge (11.9 %, 1276 e). **May not** use the bare charge, Gouy-Chapman electrode charge above ~0.2 V, or `exp(−h/λ_D)` with `λ_D = 4 nm` — the gap is counterion-dominated and its local screening length is 0.84–1.18 nm. |
| T-4 | Electrostatic softening and pull-in: does `k_eff = k_brush + k_es` reach zero? | Max usable bias with margin, or a demonstration the osmotic divergence removes the instability | new | BLOCKED by T-1c, T-3, T-3a. **Must not inherit §1's `\|k_es\| ≈ F_es/λ_D`** — `CH-0004` shows the error runs in the non-conservative direction for pull-in. |
| T-5 | Load distribution across the origami | Peak per-load-path force against the 35–60 pN disassembly band; distributed and concentrated attachment treated separately | A1.2 | **DONE** (iteration 3) — claim `C-0006`. Minimum load paths: **3** to stay under 35 pN, **11** under 10 pN — but **55** for dishing below 10 % of stroke, and the tile holds only 43.7 independent patches. A rigid anchor saturates at 18.3 pN however large the tile. |
| T-5b | Deflected shape of the tile under actuation load | Deformation amplitude against the stroke; rigid-plate assumption upheld or rejected | A8.2 | **DONE** (iteration 3) — claim `C-0006`, raises `CH-0005`. **Rigid-plate assumption REJECTED.** Dishing is 0 % (uniform load, exactly), 27 % (edge taper), 50 % (4 anchors), 369 % (one lever), 26 % (thermal, 1.27 nm RMS). |
| T-9 | Crossover hinge constant `k_θ` for a single-layer sheet, from oxDNA | A value with an uncertainty, replacing a fitted model input whose `1/100` is borrowed from CanDo's *nick* softening | new | TODO — **medium**. The single largest open premise under `C-0006`: everything about `D_⊥` inherits it. Cost estimate from the iteration that raised it: 2–5 k nucleotides, µs-scale umbrella sampling on 8 cores, **days not weeks — it fits this box**. Needs `g++`/`cmake` (installed under `P-7`). |
| T-10 | Discrete-lattice (beam-and-hinge grillage) check of the tile, replacing the continuum plate | The plate reduction upheld or rejected; local force concentration at an anchor resolved | new | TODO — **medium**. Raised because `ℓ_⊥/p = 0.26–0.52 < 1` across the whole sweep, so the continuum reduction is marginal by its own criterion, and because the plate model cannot resolve the one number `T-5` had to decline. |
| T-6 | Validity boundary of mean-field screening at 2 mM Mg²⁺ | Quantified deviation from mean-field, with the boundary stated | A7.4 | **DONE** (iteration 3) — claim `C-0005`, raises `CH-0004`. Mean field is **uncontrolled across the whole 5–10 nm working range** (deviation 123–214 % for Mg²⁺) yet **qualitatively safe** there: correlation attraction needs a gap under 1.46 nm and the layer holds the tile 3.4–6.8× outside it. Controlled PB only above 12.9 nm. |
| T-3a | 1-D nonlinear Poisson-Boltzmann profile in the actual 2:1 buffer, tile + electrode as one system | Its own `σ_eff` for `T-3`, replacing the symmetric-`z:z` ceiling `C-0005` could only quote as an order of magnitude | A7.4 | TODO — **high, and cheap (minutes)**. Settles `CH-0004` and is a prerequisite for `T-4`. |
| T-6b | Size-modified (Bikerman) PB at the electrode | The 0.197 V point-ion boundary sharpened, or replaced | new | TODO — low–medium, minutes. The cheap step that must run before any explicit-ion MC is considered. |
| T-7 | Poroelastic drainage time vs thickness and volume fraction | Bounded, with the conditions under which it would constrain ≥ 1 kHz stated | new | **DONE** (iteration 3) — claim `C-0004`, raises `CH-0003`. **Not binding**: 91 kHz at the nominal design point, 22.6 kHz at the §3 worst case, 5.6 kHz under a composite worst case. §4(d) is discharged. |
| T-7b | Electro-osmotic drag on the squeeze flow: a streaming potential opposes drainage in a porous layer under a biased electrode | Bounded, or shown to be below the 22× margin `C-0004` leaves | new | TODO — **downgraded to low** by `C-0005`, which supplies a ~10⁻³ suppression **as an argument, not a verified coefficient**: the PEG layer is neutral, only 12–17 % of its thickness carries net space charge, and counterion domination raises the local conductivity 12–23×, shorting out the streaming potential. Still needs the tile's hydrodynamic zeta and `T-7`'s Brinkman length. |
| T-8 | Tile positional variance at 300 K | σ_RMS ≤ 3.0 nm for the nominal Gen-1 tile | A1.2 | TODO — **re-scoped and promoted**, **BLOCKED by T-1c** for the stiffness. It must consume `C-0006`, **not** `C-0001`: the 0.28 nm figure is the *piston mode alone*, and the total point fluctuation is **1.37 nm** nominal, **2.24 nm** at the soft end of the sweep — 46–75 % of the 3.0 nm predicate rather than 9 %. Still passing, but the margin is gone. Two further constraints: leaf `A1.2` demands a **simulated** σ_RMS with a **95 % CI** from a coarse-grained ensemble, not an analytic bound; and `C-0004` supplies the noise bandwidth (91 kHz corner) rather than leaving it assumed. |

## Entry points

| Study | Task | Emits |
|---|---|---|
| `./gradlew study -Pstudy=brush.BrushStiffnessStudyKt` | `T-1` | `gpd/results/T-1-layer-stiffness.json` |
| `./gradlew study -Pstudy=material.PegMaterialStudyKt` | `P-3` | `gpd/results/P-3-peg-material-parameters.json` |
| `./gradlew study -Pstudy=poroelastic.PoroelasticDrainageStudyKt` | `T-7` | `gpd/results/T-7-poroelastic-drainage.json` |
| `./gradlew study -Pstudy=structure.TileLoadDistributionStudyKt` | `T-5` | `gpd/results/T-5-load-distribution.json` |
| `./gradlew study -Pstudy=structure.TileFlatnessStudyKt` | `T-5b` | `gpd/results/T-5b-tile-flatness.json` |
| `./gradlew study -Pstudy=electrostatics.MeanFieldValidityStudyKt` | `T-6` | `gpd/results/T-6-mean-field-screening-validity.json` |
| `./gradlew study -Pstudy=material.SolventQualitySaltStudyKt` | `P-6` | `gpd/results/P-6-solvent-quality-vs-salt.json` |

Add `-PbuildDirectory=<dir>` to any Gradle command when more than one agent is working this checkout (`P-7`).

## Ordering rationale

NDI names Tasks 1 and 2 as the starting point, and the dependency structure agrees:
T-1 is the cheapest thing in the programme and T-3, T-4, T-7 and T-8 all consume its output.

T-8 is the next-cheapest — equipartition against the stiffness T-1 produces — and it is a *falsifier*:
a layer compliant enough to actuate may be too compliant to hold position at 300 K.
That tension is worth exposing early, so T-8 is promoted above its position in the problem definition's numbering.

P-3 and P-4 were raised as blockers rather than niceties because T-1's answer is only as good as its premises,
and the problem definition (§2, second caveat) says exactly that:
*"Where that crossover sits for our layer decides which exponent we are entitled to."*
That judgement was vindicated harder than expected: P-3 found the premise **violated**, not merely unchecked,
and the violation propagates into the chain length T-1 derives, not just into the exponent it applies.
Hence `T-1c` now sits above `T-2` for the same reason `P-3` sat above it before —
`T-2`'s deliverable is either a window or a *proof of emptiness*, and neither can rest on a failed premise.

## Standing findings that constrain everything downstream

- **Stiffness is not a single number at the resting height.** The SCF form has zero stiffness at first
  contact; the scaling form does not. Any downstream task quoting "the layer stiffness" must quote it
  at a stated compression. (`C-0001`, surprise S-1.)
- **The layer is in the dilute→semidilute crossover, not the semidilute regime.** `φ/φ# = 1.08–1.23`
  unperturbed, 1.30–2.24 under the target force; the des Cloizeaux domain starts at 5 and is never
  reached. The osmotic exponent is `m_eff = 1.66–1.92`, so **`m = 9/4` and `m = 3` are both excluded.**
  (`C-0002`, `CH-0001`.)
- **`C-0001`'s strokes are lower bounds, and its window a lower bound on its own width.** Every
  correction found in P-3 makes the layer softer — exponent down, prefactor ×0.751, excluded volume ×0.230.
  So *"empty at 5 nm and 7 nm"* and *"~10 nm stroke unreachable"* are **provisional pending `T-1c`**,
  and are no longer safe to quote as findings.
- **A compliant brush and a semidilute brush may be mutually exclusive for PEG.** Reaching the
  des Cloizeaux domain needs σ = 0.99 nm⁻² at 10 nm and 3.96 nm⁻² at 5 nm — the latter closer than one
  Kuhn diameter, so unrealisable; the former melt-like and ruled out by §4(a) for stiffness. (`C-0002`.)
- **The §2 chain-tension caveat is discharged.** 4.2 pN per chain at the design point against a ~30 pN
  threshold, and structurally incapable of reaching it by grafting density alone. (`C-0002`.)
- **§4(d) poroelasticity is discharged as a non-constraint, with its boundary named.** 91 kHz at the
  nominal design point, 22.6 kHz at the §3 worst case, 5.6 kHz under a composite worst case — against a
  1 kHz requirement. Drainage is a **footprint** problem, not a thickness problem (`τ ∝ L²`, `h` cancels),
  and a **denser** layer drains faster, so the binding direction is dilution. The design would have to
  leave the poroelastic model's own domain of validity before poroelasticity could bind. (`C-0004`.)
- **The buffer does not reach the layer's mechanics.** The mobile-ion channel is **exactly zero** by a
  conservation argument — ideal excluded salt gives a free energy strictly linear in φ, which `Π = φf′ − f`
  annihilates — despite carrying **3.5× the layer's own osmotic pressure** at 10 mM. The solvent-quality
  channel is ≤ 0.4 % of the modulus over 2–10 mM; it would take a salt 1.35× stronger than any in the PEO
  literature to reach 1 %. **But the layer-local Mg²⁺ is 33–66 mM, not 2–10 mM, and goes as `1/h`** — a
  ≤ 1.7 % stroke-dependent stiffness term, and the only positive-feedback term anywhere downstream. (`C-0007`.)
- **`χ` for PEG/water is 0.372 at 300 K, measured — not the 0.45 that was cited, which has no primary source
  at all** (the 0.44 in circulation is *polystyrene in toluene*). And `χ` carries a lattice-site convention
  worth a factor of **2.010**, the exact analogue of `C-0002`'s three meanings of `a`. (`C-0007`.)
- **A bulk `χ` is not a brush `χ`, and that gap is 239× everything else in this section.** A dense grafted PEO
  layer is reported at `χ ≈ 0.60` — formally poor solvent — against 0.372 in bulk. Every osmotic number in the
  programme is a bulk-solution property applied to a brush. (`C-0007`, `P-9`.)
- **Mean-field screening is uncontrolled across the whole working range, and qualitatively safe across it.**
  The deviation is 123–214 % for Mg²⁺ at 5–10 nm gaps, so PB is not merely inaccurate there but outside the
  control of its own expansion; yet correlation attraction needs a gap under 1.46 nm, which the layer never
  allows. Both edges of the boundary miss the working gap — one 3.4× below, one 1.3× above. **`Ξ ∝ q³`: the
  divalence does this, not the surface charge** (Na⁺ at the same surface gives `Ξ = 3.0` against 24). (`C-0005`.)
- **Point-ion PB at the electrode dies above ~0.197 V of diffuse-layer drop — 10× below the §3 ≤ 2 V target.**
  Above it the electrode charge is Stern-limited at ~1.25 e/nm² per volt, not Gouy-Chapman, and dilution helps
  only logarithmically. Any bias-dependent result above 0.2 V needs size-modified PB. (`C-0005`.)
- **§4(c) has the sign backwards: the polymer layer *protects* the field.** It admits only 52–77 % of the bulk
  salt, so the local Debye length is 1.14–1.39× **longer** inside the layer, and lengthens further under
  compression. The dielectric-decrement mechanism §4(c) names is 3.9 % at φ ≈ 0.03 — the layer is 97 % water.
  The bound is one-sided (exclusion only); cation coordination by PEG's ether oxygens could flip it (`P-8`). (`C-0005`.)
- **"The Debye length" is three different numbers here, and all three are right in their own place** — 3.93 nm
  in bulk buffer, 0.84–1.18 nm in the counterion-dominated gap, 4.5–5.5 nm inside the PEG layer. Substituting
  one for another is `CH-0004`. (`C-0005`.)
- **The tile is not a rigid plate.** `ℓ/L = 0.14–0.64`; it is rigid only for a perfectly uniform load,
  where it is rigid *exactly*, whatever its rigidity. Any concentrated coupling, discrete anchor, load
  non-uniformity or thermal excitation dishes it by 26–369 % of the stroke. A point-coupled lever and an
  area-averaging charge sensor therefore **do not measure the same displacement** — they differ by 26 % of
  the stroke. §4(g)'s own test for abandoning the rigid-plate picture is met. (`C-0006`, `CH-0005`.)
- **No discrete attachment scheme is flat.** Flatness needs ≳ 55 load paths, more than the 43.7 independent
  patches the tile contains, so the output coupling has to be effectively continuous. This is a constraint on
  design *topology*, and `T-2`'s window has no axis for it yet. (`C-0006`.)
- **The 35–60 pN band is not a per-load-path allowable.** It is a *whole-cross-section* disassembly force for
  a 6–8-helix tube at 5.5 pN/s, and a DNA rupture force without a loading rate is not a material constant.
  Per path, use single-duplex shear (~48–65 pN) or unzip (10–15 pN), with 65 pN a hard ceiling. (`C-0006`.)
- **The layer is ~1.5 blobs tall.** `L₀/s = (Σ/π)^(5/6)` identically, so the conventional `Σ = 5` onset
  buys 1.47 blobs and a ten-blob stack needs `Σ ≈ 50`. This is a *geometric* failure of the same
  convention `CH-0001` failed thermodynamically, and the two are inverse powers of the same `Σ`.
  Strong-stretching theory is also outside its own premise here: `L₀/R_F = 1.17–1.25`. (`CH-0003`.)

## Open questions for Kazik

**1. Two paywalled PDFs would close the only genuinely missing measurements in `P-6` and `P-8`.**
This is an **access** limit, not a compute limit — ACS, Elsevier, Springer and IOP all refuse an automated fetch,
and Crossref/EuropePMC serve only the abstracts (which is how the bound in `C-0007` was built at all).

- **Boucher & Hines, *J. Polym. Sci. Polym. Phys. Ed.* 14:2241 (1976)** — the one study that measured Group II
  chlorides against PEO, and so the only source for the θ-versus-[MgCl₂] curve *including the minima* that make
  a linear salting-out coefficient ill-posed in the first place. Paywalled and pre-digital; only the abstract
  is reachable, via Crossref.
- **Lee et al., *J. Phys. Chem. B* 116:7367 (2012)** — the grafted-`χ ≈ 0.60` result that `P-9` now rests on.
  It is currently used **from its abstract alone**, and `P-9` cannot be settled either way without the body:
  its grafting densities and whether its brush is solid-grafted or at an air-water interface decide whether
  the result applies to the Gen-1 layer at all.
- Any PEG/PEO salt study **below 50 mM**. `C-0007` found none at all — every cloud-point and aqueous-two-phase
  paper works at 0.1–3 M, which is two orders of magnitude above the Gen-1 buffer and exactly where a
  non-monotonic `θ(c)` would have its structure. This may simply not exist rather than be paywalled.

Neither is blocking: `C-0007` bounds the effect they would pin at ≤ 0.4 % of the layer modulus, and states the
bound as a falsifiable threshold rather than a guess. They would convert a bound into a number.

**2. Nothing has yet needed more compute than this box provides.** `T-9` (crossover hinge constant from oxDNA)
is the first queued item that would run for *days* rather than minutes — costed at 2–5 k nucleotides and
µs-scale umbrella sampling on 8 cores. It fits the machine; it does not fit inside one session.
Flagged so the decision to start it is yours rather than made by accident.
