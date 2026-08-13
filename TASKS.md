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
| P-5 | Decide and defend the brush-regime criterion (`Σ ≥ 5` vs `Σ > 1`) | **DONE** (iteration 3) | Formal adoption made by `T-1c`: the criterion is **`L₀/R₀ ≥ 1`**, reported as a number at every design point, with windows emitted both with and without it so the two contributions stay separable. `Σ ≥ 5` is dropped — it failed thermodynamically (`CH-0001`) and geometrically (`CH-0003`). |
| P-7 | Build isolation for concurrent agents: `-PbuildDirectory=<dir>` so parallel runs of one checkout stop racing on `build/test-results` | **DONE** (iteration 3) | Raised as a process blocker mid-iteration: four agents sharing one working tree could not get an authoritative `./gradlew test`, and the failure (`EOFException`, `NoSuchFileException` on the in-progress results binary) looks like a broken test rather than a broken harness. `build-*/` is git-ignored. |
| P-10 | Verification isolation at high agent concurrency: `-PbuildDirectory` alone is not sufficient | **DONE** (iteration 3) | Raised by `T-8`, which lost **fourteen** full-suite attempts to it. `P-7`'s per-agent build directory is necessary but not sufficient — the Gradle **project lock**, `~/.gradle` and the Kotlin daemon are still shared, and the incremental compiler's session state races, producing `NoClassDefFoundError` on classes nobody touched. Fixed by [`tools/verify.sh`](tools/verify.sh), which runs the suite on an isolated copy of the tree (`--committed` archives `HEAD` instead, which is what the coordinator needs before pushing). |
| P-8 | Mg²⁺/PEG coordination constant in water | TODO — medium | Raised by `C-0005`. PEG's ether oxygens coordinate cations — the mechanism behind PEO polymer electrolytes — and this is the **only** mechanism that could flip the sign of the §4(c) answer, since `T-6`'s partitioning bound counts exclusion only. **`P-6` searched independently and confirms the number does not exist in accessible literature**: the mechanism is stated for the right system in water, with MgCl₂ in the salt list, but no constant, and the quantitative multivalent-cation/PEO NMR work is in **methanol**. Needs a paywalled pull or an experiment. |
| P-9 | **The effective `χ` of a *grafted* PEG layer is not the bulk one** — bound it, or declare `C-0002`'s bulk equation of state inapplicable to a brush | TODO — **HIGH**, a process blocker above `T-2` | Raised by `C-0007`. An SCF fit to neutron reflectivity puts a dense PEO brush at `χ ≈ 0.60` (above θ, formally poor solvent) against **0.372** in bulk — `Δχ = 0.23`, **239× the entire salt effect `P-6` was chartered to bound**. Every osmotic number in the programme derives from a **bulk-solution** property applied to a brush. Not a collapse: the same source reports the brush still exerts *positive* surface pressure, and `C-0003` bounds the exposure at `k ∝ K^(1/(m+1))`, so a 16× change in interaction strength is a 25 % change in stroke. **Cheap first step:** the source is used **from its abstract only** and may concern an *air-water-interface* brush — read the body and check its grafting densities against the Gen-1 window. It may be inapplicable, closing the task outright. |
| P-6 | `χ(T, salt)` and the Mg²⁺ salting-out coefficient for PEG/water at 2–10 mM | **DONE** (iteration 3) | Claim `C-0007`, challenge `CH-0006`. **The buffer does not reach the layer's mechanics**: the mobile-ion channel is exactly zero, and the solvent-quality channel is ≤ 0.4 % of the modulus over 2–10 mM. The Mg²⁺ coefficient is **not determinable and probably not well posed** — `θ(c)` shows *minima* for Group II chlorides, and PEG forms no binodal with MgCl₂ at all, so no ATPS-derived coefficient can exist. Bounded by a threshold instead: MgCl₂ would need `k_s ≥ 92.8 K/M`, 1.35× above the ceiling any PEO salt reaches. |

## Science tasks

| ID | Task | Acceptance (abridged — full text in `gpd/tasks/`) | Leaf | Status |
|---|---|---|---|---|
| T-1 | Stiffness of the polymer layer under the tile | Number with stated model, parameters, validity range; sensitivity to grafting density reported | A2.1 | **DONE** (iteration 1) — claim `C-0001`, **challenged by `CH-0001`**: numbers stand as *lower bounds*, validity range corrected, `m = 3` excluded |
| T-1c | Layer response from a **crossover-valid** free energy, not a fixed osmotic exponent | Stiffness and stroke re-derived with `m_eff(φ)` from `C-0002`; the Alexander-de Gennes height relation either justified at φ/φ# ≈ 1 or replaced; `N(L₀)` no longer resting on the failed premise | A2.1 | **DONE** (iteration 3) — claim `C-0003`, raises `CH-0002`, **resolves `CH-0003`**, **closes `P-5`**. The height relation is **replaced**: `L₀` is exactly linear in `N`, and `σ^(1/3)` holds only for a two-body interaction while des Cloizeaux gives `σ^(5/13)`. `N(L₀)` was 5–88 % too short. |
| T-1d | Numerical SCF density profile (Scheutjens-Fleer) for the Gen-1 layer | Whether the 10 nm window exists, decided by a profile whose premise is met | A2.1 | TODO — **high; blocks `T-2`'s window branch**. `T-1c`'s declared falsifier fired here: whether the 10 nm window exists is decided by the **profile**, not the interaction — empty under both box models, non-empty under strong stretching, while the two interaction laws differ by only 1.45×. Hours of CPU, and **now worth buying**: it would run against an interaction free energy anchored in measurement rather than a guessed χ, which is exactly the condition `T-1`'s cost table set before SCF was worth it. |
| T-1b | Free-energy functional of the compressed layer, and the `(48/35)N/g` blob-count identity | Closed form implemented and the identity verified as a test, not as an argument | A2.1 | **KILLED** (iteration 3) — absorbed into `T-1c`, which built the free energy. The blob-count identity it existed to verify is **moot**: the layer is not a blob stack (0.06 thermal blobs per chain, a 1.47-blob Alexander stack), so verifying an identity of a picture we have refuted would be spending effort to confirm a premise we no longer hold. |
| T-2 | Feasible design window in (grafting density, height, chemistry) | Non-empty region satisfying §4(a)–(d) simultaneously, or a proof of emptiness naming the binding constraint | A2.1 | **UNBLOCKED by T-1c**, and its deliverable has changed shape: at 10 nm **neither branch of the predicate is currently available** — the window's existence is decided by the *profile model*, not the interaction, so "non-empty region" and "proof of emptiness" are both unsupported until `T-1d`. What **is** robust across all six models: 5 nm and 7 nm are **empty**, and the ~10 nm desired stroke is **unreachable everywhere**. Inputs: §4(d) discharged as a non-constraint and tile edge bounded above at 437 nm (`C-0004`); §4(c) answered with the opposite sign (`C-0005`); and a **topological** constraint with no axis in the current window — the output coupling needs ≳ 55 load paths against 43.7 independent patches (`C-0006`). |
| T-3 | Stroke and blocking force vs bias, incl. ionic screening | Stroke ≥ ~3 nm and force ≥ 100 pN at ≤ 2 V, or a demonstration it is unreachable | A2.2 | **UNBLOCKED by T-6**, and heavily constrained by `C-0005`. T-1 says only what the layer does *if* 100 pN is available; T-3 must not inherit it as given. **Now has `F_es(h, V)` from `C-0008`** and `C-0003`'s layer response, so it is ready to run. **May use** the tabulated force with a factor-of-two-class mean-field uncertainty; the layer *amplifies* the force 1.15–1.60×; the tile-charge choice is immaterial (7 % in `σ_eff`). **May not** trust any force above ~1 V of *applied* bias (`CH-0007` — not 0.2 V, which was a diffuse-layer drop); may not treat the zero-bias force as a definite number (it is a near-cancellation under 4 pN that changes sign between 4 and 5 nm); may not use superposition (it overstates 3.7× one way, understates 4.0× the other); and may not use `exp(−h/λ_D)` with `λ_D = 4 nm` — the force's own decay length is 1.8–2.8 nm at the working gap and is bias-dependent. |
| T-4 | Electrostatic softening and pull-in: does `k_eff = k_brush + k_es` reach zero? | Max usable bias with margin, or a demonstration the osmotic divergence removes the instability | new | TODO — **PROMOTED to high; now the most likely binding constraint in the programme.** `C-0008` gives `k_es` directly from the solve, so §1's `\|k_es\| ≈ F_es/λ_D` must not be inherited (it understates by 1.0–2.6×, non-conservative for pull-in). Against `C-0001`, `\|k_es\|` reaches `k_brush` at **0.158 / 0.098 / 0.077 V** (5 / 7 / 10 nm, 2 mM) — and set against the bias needed for 100 pN the margin **inverts** across the height range: 2.4× at 5 nm, 0.87× at 7 nm, **0.11× at 10 nm**. Blocked only by `T-3`. |
| T-5 | Load distribution across the origami | Peak per-load-path force against the 35–60 pN disassembly band; distributed and concentrated attachment treated separately | A1.2 | **DONE** (iteration 3) — claim `C-0006`. Minimum load paths: **3** to stay under 35 pN, **11** under 10 pN — but **55** for dishing below 10 % of stroke, and the tile holds only 43.7 independent patches. A rigid anchor saturates at 18.3 pN however large the tile. |
| T-5b | Deflected shape of the tile under actuation load | Deformation amplitude against the stroke; rigid-plate assumption upheld or rejected | A8.2 | **DONE** (iteration 3) — claim `C-0006`, raises `CH-0005`. **Rigid-plate assumption REJECTED.** Dishing is 0 % (uniform load, exactly), 27 % (edge taper), 50 % (4 anchors), 369 % (one lever), 26 % (thermal, 1.27 nm RMS). |
| T-9 | Crossover hinge constant `k_θ` for a single-layer sheet, from oxDNA | A value with an uncertainty, replacing a fitted model input whose `1/100` is borrowed from CanDo's *nick* softening | new | TODO — **downgraded to low-medium** by `C-0010`, which shows a 2× change in `D_⊥` moves the tile's fluctuation by only 2.5 %, because the shape modes are foundation-dominated at `ℓ/L ≈ 0.2–0.5`. It remains the largest open premise under `C-0006`, but it blocks nothing in `T-8`. Was: **medium**. The single largest open premise under `C-0006`: everything about `D_⊥` inherits it. Cost estimate from the iteration that raised it: 2–5 k nucleotides, µs-scale umbrella sampling on 8 cores, **days not weeks — it fits this box**. Needs `g++`/`cmake` (installed under `P-7`). |
| T-10 | Discrete-lattice (beam-and-hinge grillage) check of the tile, replacing the continuum plate | The plate reduction upheld or rejected; local force concentration at an anchor resolved | new | TODO — **medium**. Raised because `ℓ_⊥/p = 0.26–0.52 < 1` across the whole sweep, so the continuum reduction is marginal by its own criterion, and because the plate model cannot resolve the one number `T-5` had to decline. |
| T-6 | Validity boundary of mean-field screening at 2 mM Mg²⁺ | Quantified deviation from mean-field, with the boundary stated | A7.4 | **DONE** (iteration 3) — claim `C-0005`, raises `CH-0004`. Mean field is **uncontrolled across the whole 5–10 nm working range** (deviation 123–214 % for Mg²⁺) yet **qualitatively safe** there: correlation attraction needs a gap under 1.46 nm and the layer holds the tile 3.4–6.8× outside it. Controlled PB only above 12.9 nm. |
| T-3a | 1-D nonlinear Poisson-Boltzmann profile in the actual 2:1 buffer, tile + electrode as one system | Its own `σ_eff` for `T-3`, replacing the symmetric-`z:z` ceiling `C-0005` could only quote as an order of magnitude | A7.4 | **DONE** (iteration 3) — claim `C-0008`, raises `CH-0007`, **resolves `CH-0004`**. The 2:1 first integral was derived, not adapted; `C-0005`'s ceiling is confirmed as a ceiling and is 24 % high, by exactly `6 − 3√3`. The tile is **charge-saturated**, so the charge ambiguity `C-0005` could not resolve moves `σ_eff` by only 7 %. |
| T-6b | Size-modified (Bikerman) PB at the electrode | The 0.197 V point-ion boundary sharpened, or replaced | new | **Downgraded to low** — its Bikerman half was folded into `T-3a` (the point-ion model is exactly the `n_max → ∞` limit, so it cost one function and became an executable limiting case). What remains is the **Stern capacitance**, which `CH-0007` makes load-bearing for the diffuse-drop→applied-bias mapping. |
| T-3b | 2-D Poisson-Boltzmann solve of the tile edge | The lateral load non-uniformity `T-5b` needs, as a profile rather than a parameter | new | TODO — **medium**. The only route to closing §4(g): `C-0006` makes dishing *exactly linear* in the load non-uniformity, and `C-0008` states plainly that a 1-D treatment cannot supply it. Hours, not days. |
| T-11 | The aqueous electrochemical window: is 1–2 V applicable at all in MgCl₂? | The usable bias ceiling set by water electrolysis (1.23 V thermodynamic), against §3's 2 V | new | TODO — low–medium. `C-0008` notes that because the force **saturates** in bias, the answer barely moves its conclusions — *"but that is luck, not an argument."* |
| T-12 | **Lateral confinement of the tile** | An anchoring scheme delivering `k_lat ≥ 0.4602 pN/nm`, or a demonstration that none is available | A1.2 | TODO — **HIGH**, and cheap (hours). `C-0010` shows the layer's lateral restoring stiffness is **exactly zero by symmetry**, not merely small, so the tile diffuses **62.8 nm in one 1 kHz period** — 21× the §6 predicate and 1.6 tile widths. §3 specifies nothing that confines it. This is a second **topological** axis on `T-2`'s window, beside `C-0006`'s distributed-coupling constraint. |
| T-13 | Where the tile sits at zero bias | Whatever holds the tile down, named and quantified | new | TODO — medium. A non-adsorbing layer exerts **no upward force above `L₀`**, and three of six `C-0003` models have exactly zero stiffness at `L₀`, so the unbiased tile is unconfined in *both* directions. Nothing in the §3 stack owns this and no task in the programme currently does either. |
| T-7 | Poroelastic drainage time vs thickness and volume fraction | Bounded, with the conditions under which it would constrain ≥ 1 kHz stated | new | **DONE** (iteration 3) — claim `C-0004`, raises `CH-0003`. **Not binding**: 91 kHz at the nominal design point, 22.6 kHz at the §3 worst case, 5.6 kHz under a composite worst case. §4(d) is discharged. |
| T-7b | Electro-osmotic drag on the squeeze flow: a streaming potential opposes drainage in a porous layer under a biased electrode | Bounded, or shown to be below the 22× margin `C-0004` leaves | new | TODO — **downgraded to low** by `C-0005`, which supplies a ~10⁻³ suppression **as an argument, not a verified coefficient**: the PEG layer is neutral, only 12–17 % of its thickness carries net space charge, and counterion domination raises the local conductivity 12–23×, shorting out the streaming potential. Still needs the tile's hydrodynamic zeta and `T-7`'s Brinkman length. |
| T-8 | Tile positional variance at 300 K | σ_RMS ≤ 3.0 nm for the nominal Gen-1 tile | A1.2 | **DONE** (iteration 3) — claim `C-0010`, raises `CH-0009`. **PASS at the operating point** on the declared acceptance quantity (area RMS 0.87–0.96 nm, 3.1–3.4× margin; in band below 1 kHz, 0.069–0.110 nm). Two qualifications travel with it: the tile's **worst point exceeds 3.0 nm in every state softer than the working point** (3.13–4.38 nm), and the **lateral coordinate is not part of the PASS** (`T-12`). Leaf `A1.2` is only **partly** discharged — no simulated ensemble and no 95 % CI, stated as not discharged rather than approximated. Superseded note — was: **re-scoped, promoted, and UNBLOCKED by T-1c** (σ_RMS at the working point is 0.13–0.30 nm across the new bracket, and must be quoted from the crossover-valid stiffness, not `C-0001`'s). It must consume `C-0006`, **not** `C-0001`: the 0.28 nm figure is the *piston mode alone*, and the total point fluctuation is **1.37 nm** nominal, **2.24 nm** at the soft end of the sweep — 46–75 % of the 3.0 nm predicate rather than 9 %. Still passing, but the margin is gone. Two further constraints: leaf `A1.2` demands a **simulated** σ_RMS with a **95 % CI** from a coarse-grained ensemble, not an analytic bound; and `C-0004` supplies the noise bandwidth (91 kHz corner) rather than leaving it assumed. |

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
| `./gradlew study -Pstudy=brush.CrossoverLayerStudyKt` | `T-1c` | `gpd/results/T-1c-crossover-valid-layer-response.json` (~3.5 min) |
| `./gradlew study -Pstudy=electrostatics.NonlinearPbProfileStudyKt` | `T-3a` | `gpd/results/T-3a-nonlinear-pb-profile.json` |
| `./gradlew study -Pstudy=structure.TilePositionalVarianceStudyKt` | `T-8` | `gpd/results/T-8-tile-positional-variance.json` |

Add `-PbuildDirectory=<dir>` to any Gradle command when more than one agent is working this checkout (`P-7`),
and use [`tools/verify.sh`](tools/verify.sh) for an authoritative full-suite run — at four or more concurrent
agents the shared project lock defeats `-PbuildDirectory` on its own (`P-10`).

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
- **The *bulk* solution is in the dilute→semidilute crossover, with `m_eff = 1.66–1.92` — but that is a
  bulk quantity and it does not transfer to the layer.** **The grafted layer's own osmotic exponent is
  2.00–2.56**, because the `φ lnφ/N` term that bends the bulk exponent below 9/4 is the translational
  entropy of whole chains, which grafting removes. So `m < 2` is what is excluded, not `m = 9/4`.
  (`C-0002`, `C-0003`, `CH-0002`.)
- ~~**`C-0001`'s strokes are lower bounds, and its window a lower bound on its own width.** Every
  correction found in P-3 makes the layer softer — exponent down, prefactor ×0.751, excluded volume ×0.230.~~
  **WITHDRAWN by `CH-0002`.** The direction is wrong. The height relation — which `CH-0001` had itself
  identified as the unrepairable part — is the larger effect and runs **stiffer**: stiffness at first
  contact is 33–87 % *higher*, not 19 % lower, and the stroke bracket **straddles** `C-0001` at every
  height. Concluding a direction from the corrections one happens to have is a distinct failure mode from
  getting a correction wrong, and this is an instance of it.
- **PEG in water is a *marginal* solvent, and the Gen-1 chains are not swollen at all.** The measured `A₂`
  gives an excluded volume of **12.25 Å³** against a 60.4 Å³ monomer, so the thermal blob is 1222 Kuhn
  segments — **167 kDa** — while the whole design space is 60–375 monomers, i.e. **0.02–0.10 of one blob**.
  Every blob-based statement about this layer was about a structure it does not have. (`C-0003`.)
- **The Alexander-de Gennes unity prefactor is worth 6.6× in excluded volume** — `L₀ = N a^(5/3)σ^(1/3)` is
  reproduced exactly by a two-body box layer at `v = 81.0 Å³`, against the measured 12.25 Å³. Quote it as a
  scaling, never as a number. (`C-0003`.)
- **The layer response is only weakly sensitive to the interaction strength**: `k ∝ K^(1/(m+1))` and
  `N ∝ K^(−1/(m+1))` exactly, because the chain length a specified height demands moves against the
  interaction. A **16×** change in `K` moves the stroke only from 5.81 to 4.38 nm. This bounds `P-9`'s
  exposure, and it says not to spend effort narrowing `Π_int` before checking this exponent. (`C-0003`.)
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
- ~~**Point-ion PB at the electrode dies above ~0.197 V of diffuse-layer drop — 10× below the §3 ≤ 2 V target.**~~
  **CORRECTED by `CH-0007`.** The 0.197 V is a **diffuse-layer drop**, not an applied bias, and the two differ
  by the compact-layer drop — which takes 66 % of 0.1 V and 88 % of 2 V, because the electrode charge is
  exponential in `ψ_d` while the compact term is linear in it. The boundary is therefore at **≈ 1.0 V of
  applied bias**, so §3's 2 V ceiling exceeds it by 1.2×, not by 10×. Comparing a `ψ_d` threshold against a
  §3 bias without the Stern series is the error. (`C-0005`, `C-0008`, `CH-0007`.)
- **100 pN at ≤ 2 V is reachable on the electrostatics alone, with room** — 0.067 V at 5 nm, 0.113 V at 7 nm,
  0.679 V at 10 nm in 2 mM buffer, all comfortably inside the point-ion boundary. But **the actuator is
  voltage-saturated above ~0.5 V**: a factor of 8 in bias buys 1.9× in force, so §3's 2 V ceiling is almost
  irrelevant to what the device can do. (`C-0008`.)
- **The force's decay length is a fourth number, and the only bias-dependent one** — 1.8–2.8 nm at the working
  gap, rising to the bulk `λ_D` in the far field, and `λ_D/2` at zero bias against `λ_D` under bias. None of
  `CH-0004`'s three lengths is it. (`C-0008`, resolving `CH-0004`.)
- **§4(c)'s sign reversal now carries a force: the layer *amplifies* `F_es` by 1.15–1.60×**, largest at 10 mM
  and under compression. And **finite ion size *raises* the force by up to 56 %**, so point-ion PB is a
  **lower** bound on `|F_es|`, not an upper one. (`C-0008`.)
- **§4(c) has the sign backwards: the polymer layer *protects* the field.** It admits only 52–77 % of the bulk
  salt, so the local Debye length is 1.14–1.39× **longer** inside the layer, and lengthens further under
  compression. The dielectric-decrement mechanism §4(c) names is 3.9 % at φ ≈ 0.03 — the layer is 97 % water.
  The bound is one-sided (exclusion only); cation coordination by PEG's ether oxygens could flip it (`P-8`). (`C-0005`.)
- **"The Debye length" is three different numbers here, and all three are right in their own place** — 3.93 nm
  in bulk buffer, 0.84–1.18 nm in the counterion-dominated gap, 4.5–5.5 nm inside the PEG layer. Substituting
  one for another is `CH-0004`. (`C-0005`.)
- **The layer confines the tile in one direction only.** Its lateral restoring stiffness is **exactly zero by
  symmetry** — a laterally homogeneous grafted layer under a non-adsorbing tile has a translation-invariant
  free energy — so the tile diffuses 62.8 nm in one 1 kHz period, 21× the σ_RMS predicate. And it exerts **no
  upward force above `L₀`**, so at zero bias the tile is unconfined in *both* directions. Whatever holds it
  down is not in the §3 stack. (`C-0010`; `T-12`, `T-13`.)
- **A variance without a bandwidth is the `f → ∞` limit, and here that is 13× the in-band number.** Only
  0.55–3.07 % of the tile's variance lies below 1 kHz, so the in-band σ_RMS is 0.07–0.77 nm where the
  broadband figure is 0.87–4.38 nm — and the predicate passes in band even where it fails broadband. (`C-0010`.)
- **The stroke and the noise are set by different stiffnesses, and the gap is a factor of three.** The secant
  (16.6–26.1 pN/nm) sets the stroke; the tangent at the working point (47.7–64.1) sets the fluctuation.
  Substituting one for the other is a 1.6× error in amplitude. Relatedly, **actuating the tile quiets it** —
  the piston RMS falls 4.2× from unbiased to the working point — but that is one-sided, because `k_es < 0`
  runs the other way. (`C-0010`.)
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
