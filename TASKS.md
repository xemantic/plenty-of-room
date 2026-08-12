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
| P-3 | PEG material parameter sheet: monomer size, Kuhn length, χ(T, salt), excluded volume, mass density, with provenance per number and a `derived`/`cited` flag | TODO — **top of the queue** | Now the binding premise under `C-0001`: it moves φ by ~1.5× and the SCF excluded volume by ~25×. NDI §7: premises checked against the material, not inherited from the textbook. |
| P-4 | Volume-fraction bookkeeping: locate the semidilute→concentrated crossover for *this* layer rather than quoting φ ≈ 0.2–0.3 | PARTIAL | φ is now emitted at every design point and comes out at 0.03–0.044 in the surviving window, so `m = 9/4` is justified there. What remains is the crossover value itself, which is still a convention. |
| P-5 | Decide and defend the brush-regime criterion (`Σ ≥ 5` vs `Σ > 1`) | TODO | It sets the **lower** edge of the T-1 window, so the window's width is convention-dependent. On `Σ > 1` the 7 nm window opens. Blocks T-2's proof-of-emptiness. |

## Science tasks

| ID | Task | Acceptance (abridged — full text in `gpd/tasks/`) | Leaf | Status |
|---|---|---|---|---|
| T-1 | Stiffness of the polymer layer under the tile | Number with stated model, parameters, validity range; sensitivity to grafting density reported | A2.1 | **DONE** (iteration 1) — claim `C-0001`, validity bounded by P-3/P-5 |
| T-1b | Free-energy functional of the compressed layer, and the `(48/35)N/g` blob-count identity | Closed form implemented and the identity verified as a test, not as an argument | A2.1 | TODO — **promoted**, T-4 cannot start without it |
| T-2 | Feasible design window in (grafting density, height, chemistry) | Non-empty region satisfying §4(a)–(d) simultaneously, or a proof of emptiness naming the binding constraint | A2.1 | TODO — **next**. T-1 hands it a single narrow band at L₀ = 10 nm, σ ∈ [0.024, 0.030] nm⁻², already bounded by two constraints. |
| T-3 | Stroke and blocking force vs bias, incl. ionic screening | Stroke ≥ ~3 nm and force ≥ 100 pN at ≤ 2 V, or a demonstration it is unreachable | A2.2 | UNBLOCKED by T-1 — needs the electrostatic model. Note T-1 says only what the layer does *if* 100 pN is available; T-3 must not inherit it as given. |
| T-4 | Electrostatic softening and pull-in: does `k_eff = k_brush + k_es` reach zero? | Max usable bias with margin, or a demonstration the osmotic divergence removes the instability | new | BLOCKED by T-1, T-3 |
| T-5 | Load distribution across the origami | Peak per-load-path force against the 35–60 pN disassembly band; distributed and concentrated attachment treated separately | A1.2 | TODO |
| T-5b | Deflected shape of the tile under actuation load | Deformation amplitude against the stroke; rigid-plate assumption upheld or rejected | A8.2 | TODO |
| T-6 | Validity boundary of mean-field screening at 2 mM Mg²⁺ | Quantified deviation from mean-field, with the boundary stated | A7.4 | TODO |
| T-7 | Poroelastic drainage time vs thickness and volume fraction | Bounded, with the conditions under which it would constrain ≥ 1 kHz stated | new | TODO |
| T-8 | Tile positional variance at 300 K | σ_RMS ≤ 3.0 nm for the nominal Gen-1 tile | A1.2 | TODO — nearly free given T-1. Preliminary: σ_RMS ≈ 0.28 nm at the working point and ≈ 0.75 nm unbiased, both well inside 3.0 nm, but this is the layer-normal DOF only — tilt, lateral and internal modes are not in it. |

## Ordering rationale

NDI names Tasks 1 and 2 as the starting point, and the dependency structure agrees:
T-1 is the cheapest thing in the programme and T-3, T-4, T-7 and T-8 all consume its output.

T-8 is the next-cheapest — equipartition against the stiffness T-1 produces — and it is a *falsifier*:
a layer compliant enough to actuate may be too compliant to hold position at 300 K.
That tension is worth exposing early, so T-8 is promoted above its position in the problem definition's numbering.

P-3 and P-4 are raised as blockers rather than niceties because T-1's answer is only as good as its premises,
and the problem definition (§2, second caveat) says exactly that:
*"Where that crossover sits for our layer decides which exponent we are entitled to."*
T-1 discharged the exponent question for the surviving window (φ ≈ 0.03–0.044 is solidly semidilute),
but it did so on a *cited* monomer size and a *conventional* crossover value.
`P-3` and `P-5` are what turn those into answers, and they now sit above `T-2` in the queue
because `T-2`'s deliverable is either a window or a *proof of emptiness* — and a proof of emptiness
resting on a convention (`Σ ≥ 5`) is not a proof.

## Standing findings that constrain everything downstream

- **Stiffness is not a single number at the resting height.** The SCF form has zero stiffness at first
  contact; the scaling form does not. Any downstream task quoting "the layer stiffness" must quote it
  at a stated compression. (`C-0001`, surprise S-1.)
- **The mechanical window is empty at 5 nm and 7 nm** and narrow at 10 nm, and §4(c)–(e) can only shrink it.
- **~10 nm stroke is unreachable at 100 pN in the brush regime** at any of the three specified heights.

## Open questions for Kazik

None outstanding.
