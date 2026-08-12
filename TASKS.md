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
| P-5 | Decide and defend the brush-regime criterion (`Σ ≥ 5` vs `Σ > 1`) | **RESOLVED in substance** (iteration 2) | `Σ = 5` ⇔ `φ = 1.085 φ#` **exactly**, independent of layer height and chain length. So the convention is a real material statement — it just places the layer at the crossover, not in the semidilute regime. The criterion should be stated on `φ/φ#`, where it is falsifiable. Formal adoption belongs to `T-1c`. |
| P-6 | `χ(T, salt)` and the Mg²⁺ salting-out coefficient for PEG/water at 2–10 mM | TODO — low priority | Split out of `P-3`, which could not determine it: the adopted EOS is non-virial so yields neither `A₂` nor `χ`, and no source for the Mg²⁺ coefficient was found. Bounded by argument at ≤ 0.7% of `τ`, below the fit uncertainty on `α` — so it does not block anything yet. It will bind if `T-3`/`T-6` need solvent quality as a function of ionic strength. |

## Science tasks

| ID | Task | Acceptance (abridged — full text in `gpd/tasks/`) | Leaf | Status |
|---|---|---|---|---|
| T-1 | Stiffness of the polymer layer under the tile | Number with stated model, parameters, validity range; sensitivity to grafting density reported | A2.1 | **DONE** (iteration 1) — claim `C-0001`, **challenged by `CH-0001`**: numbers stand as *lower bounds*, validity range corrected, `m = 3` excluded |
| T-1c | Layer response from a **crossover-valid** free energy, not a fixed osmotic exponent | Stiffness and stroke re-derived with `m_eff(φ)` from `C-0002`; the Alexander-de Gennes height relation either justified at φ/φ# ≈ 1 or replaced; `N(L₀)` no longer resting on the failed premise | A2.1 | TODO — **top of the science queue**, raised by `CH-0001`. Cannot be done by swapping an exponent: `L₀ = N a^(5/3)σ^(1/3)` is itself a semidilute result and `T-1` *inverts* it to get `N`. |
| T-1b | Free-energy functional of the compressed layer, and the `(48/35)N/g` blob-count identity | Closed form implemented and the identity verified as a test, not as an argument | A2.1 | TODO — merge into `T-1c`, which needs a free energy anyway |
| T-2 | Feasible design window in (grafting density, height, chemistry) | Non-empty region satisfying §4(a)–(d) simultaneously, or a proof of emptiness naming the binding constraint | A2.1 | **BLOCKED by T-1c**. Until then `C-0001`'s band is a *lower bound on width*, not a window. `C-0002` hands T-2 a new candidate binding constraint: the densities that make the brush theory valid (σ ≥ 0.99 nm⁻² at 10 nm) are the ones §4(a) rules out as far too stiff. |
| T-3 | Stroke and blocking force vs bias, incl. ionic screening | Stroke ≥ ~3 nm and force ≥ 100 pN at ≤ 2 V, or a demonstration it is unreachable | A2.2 | UNBLOCKED by T-1 — needs the electrostatic model. Note T-1 says only what the layer does *if* 100 pN is available; T-3 must not inherit it as given. |
| T-4 | Electrostatic softening and pull-in: does `k_eff = k_brush + k_es` reach zero? | Max usable bias with margin, or a demonstration the osmotic divergence removes the instability | new | BLOCKED by T-1, T-3 |
| T-5 | Load distribution across the origami | Peak per-load-path force against the 35–60 pN disassembly band; distributed and concentrated attachment treated separately | A1.2 | TODO |
| T-5b | Deflected shape of the tile under actuation load | Deformation amplitude against the stroke; rigid-plate assumption upheld or rejected | A8.2 | TODO |
| T-6 | Validity boundary of mean-field screening at 2 mM Mg²⁺ | Quantified deviation from mean-field, with the boundary stated | A7.4 | TODO |
| T-7 | Poroelastic drainage time vs thickness and volume fraction | Bounded, with the conditions under which it would constrain ≥ 1 kHz stated | new | TODO |
| T-8 | Tile positional variance at 300 K | σ_RMS ≤ 3.0 nm for the nominal Gen-1 tile | A1.2 | TODO — nearly free given T-1. Preliminary: σ_RMS ≈ 0.28 nm at the working point and ≈ 0.75 nm unbiased, both well inside 3.0 nm, but this is the layer-normal DOF only — tilt, lateral and internal modes are not in it. |

## Entry points

| Study | Task | Emits |
|---|---|---|
| `./gradlew study -Pstudy=brush.BrushStiffnessStudyKt` | `T-1` | `gpd/results/T-1-layer-stiffness.json` |
| `./gradlew study -Pstudy=material.PegMaterialStudyKt` | `P-3` | `gpd/results/P-3-peg-material-parameters.json` |

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

## Open questions for Kazik

None outstanding. Nothing so far has needed more compute than this box provides —
`P-3` is closed-form arithmetic against published measurement, and `T-1c` is expected to be the same.
