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
| P-5 | Decide and defend the brush-regime criterion (`Σ ≥ 5` vs `Σ > 1`) | **CLOSED** (iteration 3) | **`L₀/R₀ ≥ 1` is exactly vacuous, not merely weak**: it admits **all 183** points of the Gen-1 sweep, including a layer at `Σ = 0.063` with 22 nm grafting spacing — asserted as a test in `C-0016`. **`Σ = πR₀²σ ≥ 1` is the only criterion that bounds anything**, and it owns the lower edge of every surviving window. **Four brush criteria have now failed in this project**, each a convention asked to do a measurement's work: `Σ ≥ 5` failed thermodynamically (`CH-0001`) and geometrically (`CH-0003`), and `L₀/R₀ ≥ 1` — adopted by `T-1c` to replace it — failed twice over, first as unable to bound from below (`C-0011`) and then as vacuous (`C-0016`). |
| P-7 | Build isolation for concurrent agents: `-PbuildDirectory=<dir>` so parallel runs of one checkout stop racing on `build/test-results` | **DONE** (iteration 3) | Raised as a process blocker mid-iteration: four agents sharing one working tree could not get an authoritative `./gradlew test`, and the failure (`EOFException`, `NoSuchFileException` on the in-progress results binary) looks like a broken test rather than a broken harness. `build-*/` is git-ignored. |
| P-10 | Verification isolation at high agent concurrency: `-PbuildDirectory` alone is not sufficient | **DONE** (iteration 3) | Raised by `T-8`, which lost **fourteen** full-suite attempts to it. `P-7`'s per-agent build directory is necessary but not sufficient — the Gradle **project lock**, `~/.gradle` and the Kotlin daemon are still shared, and the incremental compiler's session state races, producing `NoClassDefFoundError` on classes nobody touched. Fixed by [`tools/verify.sh`](tools/verify.sh), which runs the suite on an isolated copy of the tree (`--committed` archives `HEAD` instead, which is what the coordinator needs before pushing). |
| P-12 | Fold the multi-agent harness workarounds into `tools/` | **DONE** (iteration 4) | Taken **first** in a parallel iteration, overriding its own low ROI ranking, because `SESSION-PROMPT.md` puts process blockers above cheap wins and three concurrent agents were about to be started. [`tools/study.sh`](tools/study.sh) runs one study on an **isolated copy** and copies back only the changed `gpd/results/` files — `-PbuildDirectory` isolates `build-*/test-results` but **not** `build-*/classes`, and `T-1d`'s sweep is 33 minutes of that exposure. [`tools/verify.sh`](tools/verify.sh) gains `--drop <pkg>`, removing a package left mid-TDD by another agent — the **third** cause of `NoClassDefFoundError`, raised by `C-0015`. |
| P-11 | Collapse the two `χ` transfer conventions into one derived map | The source's SCF free energy read, replacing the 0.089 gap between the ratio and offset transfers with a derived relation | TODO — **low, non-blocking**. Raised by `C-0013`. The Supporting Information (SCF equations) is paywalled at ACS and absent from the free NIST copy of the body. |
| P-13 | The electrode material and its potential of zero charge — a **specification** gap, not a modelling one | TODO — **low cost, high leverage** | Raised by `C-0021`. §1 says "patterned electrode" and never says of what: metal against oxide is **2.6×** on the one hold-down that cannot be designed away (`C-0021`). And a contact potential of **0.9–5.1 mV** supplies the entire thermal-scale hold-down, which no §3 parameter fixes. **One question to Kazik, not a calculation** — carried to the open-questions section below. |
| P-14 | The charge presented by the **cut rim** of a DNA-origami sheet | TODO — **medium** | Raised by `C-0022`, where the declared falsifier fired: taking the rim from uncharged to the face areal density moves the fitted edge depth from −0.291 to −0.158, a **1.85×** bracket. The tile's charge is volumetric and the surface it is smeared onto is a convention, so both readings are defensible and neither is sourced. Cheaper than it looks — a geometry question about helix ends, not a measurement. |
| P-8 | Mg²⁺/PEG coordination constant in water | TODO — medium | Raised by `C-0005`. PEG's ether oxygens coordinate cations — the mechanism behind PEO polymer electrolytes — and this is the **only** mechanism that could flip the sign of the §4(c) answer, since `T-6`'s partitioning bound counts exclusion only. **`P-6` searched independently and confirms the number does not exist in accessible literature**: the mechanism is stated for the right system in water, with MgCl₂ in the salt list, but no constant, and the quantitative multivalent-cation/PEO NMR work is in **methanol**. Needs a paywalled pull or an experiment. |
| P-9 | **The effective `χ` of a *grafted* PEG layer is not the bulk one** — bound it, or declare `C-0002`'s bulk equation of state inapplicable to a brush | **DONE** (iteration 3) — **(a) inapplicable**; `C-0002`'s bulk equation of state **stands** | Claim `C-0013`, challenge `CH-0012`. **`χ ≈ 0.60` is not in the source.** Its fits are 0.789/0.852 on a scale whose *own* theta is **0.696** — the 0.60 was `1.2 × ½`, the ratio transferred onto the wrong axis, and the paper's disclaimer forbidding exactly that step is on the same page as the number. The system is an **air/D₂O Langmuir monolayer under *lateral* compression**, not a solid-grafted layer under normal compression. Its grafting densities **do** overlap the Gen-1 window, so the task closed on the *parameter*, not on system mismatch. Bounded independently at **`\|Δχ\| ≤ 0.053`** from normal-compression fits on grafted PEG at 1.5–2.5× the Gen-1 density: −11.4 % to +4.3 % in stiffness, inside `C-0003`'s own ±22 % bracket. |
| P-6 | `χ(T, salt)` and the Mg²⁺ salting-out coefficient for PEG/water at 2–10 mM | **DONE** (iteration 3) | Claim `C-0007`, challenge `CH-0006`. **The buffer does not reach the layer's mechanics**: the mobile-ion channel is exactly zero, and the solvent-quality channel is ≤ 0.4 % of the modulus over 2–10 mM. The Mg²⁺ coefficient is **not determinable and probably not well posed** — `θ(c)` shows *minima* for Group II chlorides, and PEG forms no binodal with MgCl₂ at all, so no ATPS-derived coefficient can exist. Bounded by a threshold instead: MgCl₂ would need `k_s ≥ 92.8 K/M`, 1.35× above the ceiling any PEO salt reaches. |

## Science tasks

| ID | Task | Acceptance (abridged — full text in `gpd/tasks/`) | Leaf | Status |
|---|---|---|---|---|
| T-1 | Stiffness of the polymer layer under the tile | Number with stated model, parameters, validity range; sensitivity to grafting density reported | A2.1 | **DONE** (iteration 1) — claim `C-0001`, **challenged by `CH-0001`**: numbers stand as *lower bounds*, validity range corrected, `m = 3` excluded |
| T-1c | Layer response from a **crossover-valid** free energy, not a fixed osmotic exponent | Stiffness and stroke re-derived with `m_eff(φ)` from `C-0002`; the Alexander-de Gennes height relation either justified at φ/φ# ≈ 1 or replaced; `N(L₀)` no longer resting on the failed premise | A2.1 | **DONE** (iteration 3) — claim `C-0003`, raises `CH-0002`, **resolves `CH-0003`**, **closes `P-5`**. The height relation is **replaced**: `L₀` is exactly linear in `N`, and `σ^(1/3)` holds only for a two-body interaction while des Cloizeaux gives `σ^(5/13)`. `N(L₀)` was 5–88 % too short. |
| T-1d | Numerical SCF density profile for the Gen-1 layer | Whether the 10 nm window exists, decided by a profile whose premise is met | A2.1 | **DONE** (iteration 3) — claim `C-0011`, raises `CH-0010`. **The 10 nm window EXISTS**: `σ ∈ [0.0116, 0.2601] nm⁻²`, **22.4× wide** against strong stretching's 3.5× and the box models' *empty*, robust across all three interaction laws and across two decades of the load that defines `L₀`. **7 nm is not empty either** (`[0.0296, 0.0496]`, 1.7× wide) — which contradicts a finding carried since `C-0001`. 5 nm remains empty and the ~10 nm desired stroke is still unreachable everywhere. |
| T-1b | Free-energy functional of the compressed layer, and the `(48/35)N/g` blob-count identity | Closed form implemented and the identity verified as a test, not as an argument | A2.1 | **KILLED** (iteration 3) — absorbed into `T-1c`, which built the free energy. The blob-count identity it existed to verify is **moot**: the layer is not a blob stack (0.06 thermal blobs per chain, a 1.47-blob Alexander stack), so verifying an identity of a picture we have refuted would be spending effort to confirm a premise we no longer hold. |
| T-2 | Feasible design window in (grafting density, height, chemistry) | Non-empty region satisfying §4(a)–(d) simultaneously, or a proof of emptiness naming the binding constraint | A2.1 | **DONE** (iteration 3) — claim `C-0016`, raises `CH-0015`. **P1 non-empty at 10 nm (`σ ∈ [0.0116, 0.2601] nm⁻²`, 22.4× wide) and 7 nm (`[0.0296, 0.0496]`); empty at 5 nm by a 13.3× crossing** of coil overlap against the 3 nm stroke. Both edges at both heights owned by exactly those two constraints; **§4(c) and §4(d) bind nothing at any of 183 grid points**. **P2 is NOT CLOSED in either direction: the deciding axis is the output-coupling stiffness (`T-16`), which no claim in this programme supplies.** Height convention **FORCE-ONSET** — the polymer to order is **1.1–3.3 kDa**, against ~8–9 kDa in the first-moment convention. |
| T-3 | Stroke and blocking force vs bias, incl. ionic screening | Stroke ≥ ~3 nm and force ≥ 100 pN at ≤ 2 V, or a demonstration it is unreachable | A2.2 | **DONE** (iteration 3) — claim `C-0012`, raises `CH-0011`. **Reachable, and the operating point it is reachable at is not one the device can be held at.** 100 pN blocking at 0.065–0.699 V and 100 pN *at* a 3 nm stroke at 0.082–0.368 V — all inside `CH-0007`'s ~1 V boundary with 5–12× margin. But **`k_eff < 0` at the loaded operating point at 7 and 10 nm** (428 of 810 state points), and the *free* operating point leaves three upstream validity ranges at once above **~0.1 V**. The usable bias window is **0.02–0.1 V, not 0–2 V**. The ~10 nm desired stroke is unreachable inside validity everywhere. |
| T-4 | Electrostatic softening and pull-in: does `k_eff = k_brush + k_es` reach zero? | Max usable bias with margin, or a demonstration the osmotic divergence removes the instability | new | **DONE** (iteration 4) — claim `C-0018`, raises `CH-0017`. **Both branches answered, each for a different load line: a ceiling belongs to a `(bias, load line)` pair.** For the **coupled** device (`C-0017`'s 33.333 pN/nm) the usable bias is **0.097–0.425 V**, set by `C-0002`'s `φ = 0.2` at 43 of 54 states and by **pull-in only at 10 nm / 2 mM**, where it is **0.130–0.184 V against an operating bias of 0.128–0.180 — a margin of 1.007–1.032**, i.e. `C-0017`'s 1.19–1.42× *stiffness* reserve is under 3 % on the *bias* axis. **0.5 mM removes the fold entirely** (1.29–2.36×). At 5 nm the operating point is past `φ = 0.2` before any instability (margin < 1 at 15 of 18); at 7 nm / 10 mM the fold stroke is **1.92–2.68 nm, shallower than §3's 3 nm**. The **unloaded** tile has no pull-in at 49 of 54 states — §6's second branch, true of the free tile and nothing else — and its ceiling is **0.085–0.595 V, not 0.02–0.1 V**. A **dead load** has no stable compressed equilibrium at any bias wherever it folds (25 of 25 folds at zero stroke), its ceiling being exactly `C-0008`'s blocking bias. `C-0005`'s 1.46 nm band and `CH-0007`'s 1 V bind **nowhere**; `T-11`'s 1.23 V is 2.9× above the largest ceiling. |
| T-5 | Load distribution across the origami | Peak per-load-path force against the 35–60 pN disassembly band; distributed and concentrated attachment treated separately | A1.2 | **DONE** (iteration 3) — claim `C-0006`. Minimum load paths: **3** to stay under 35 pN, **11** under 10 pN — but **55** for dishing below 10 % of stroke, and the tile holds only 43.7 independent patches. A rigid anchor saturates at 18.3 pN however large the tile. |
| T-5b | Deflected shape of the tile under actuation load | Deformation amplitude against the stroke; rigid-plate assumption upheld or rejected | A8.2 | **DONE** (iteration 3) — claim `C-0006`, raises `CH-0005`. **Rigid-plate assumption REJECTED.** Dishing is 0 % (uniform load, exactly), 27 % (edge taper), 50 % (4 anchors), 369 % (one lever), 26 % (thermal, 1.27 nm RMS). |
| T-9 | Crossover hinge constant `k_θ` for a single-layer sheet, from oxDNA — **and, at no extra cost, the crossover's vertical/axial compliance** | A value with an uncertainty, replacing a fitted model input whose `1/100` is borrowed from CanDo's *nick* softening | new | TODO — **PROMOTED to high by `C-0015`**: its *vertical-compliance* half now decides whether the registration design rule exists at all, since the whole lever is the distance-to-nearest-crossover curve and that curve rests on the crossover being rigid in `z`. Was: **re-scoped by `C-0009`, kept at medium**. *Less* urgent as a check on `C-0006`/`C-0009`: every ratio there is flat to under 3 % across the whole admissible `α`. *More* urgent as a **force-budget** input, because the peak per-path force rises with `k_θ` and that number now sits within a factor of 2 of an unzip allowable. Extend it to produce the **crossover's vertical compliance**, which `C-0009` models as a rigid constraint and which is the single uncited assumption under it. Also — was **downgraded to low-medium** by `C-0010`, which shows a 2× change in `D_⊥` moves the tile's fluctuation by only 2.5 %, because the shape modes are foundation-dominated at `ℓ/L ≈ 0.2–0.5`. It remains the largest open premise under `C-0006`, but it blocks nothing in `T-8`. Was: **medium**. The single largest open premise under `C-0006`: everything about `D_⊥` inherits it. Cost estimate from the iteration that raised it: 2–5 k nucleotides, µs-scale umbrella sampling on 8 cores, **days not weeks — it fits this box**. Needs `g++`/`cmake` (installed under `P-7`). A **third** deliverable is now attached: the crossover's **in-plane shear** stiffness `k_s`, `C-0020`'s single undetermined input, which no accessible source gives in any form — though `C-0020` shows the *aligned* answer invariant over four decades of it, so this half is the least urgent of the three. |
| T-10 | Discrete-lattice (beam-and-hinge grillage) check of the tile, replacing the continuum plate | The plate reduction upheld or rejected; local force concentration at an anchor resolved | new | **DONE** (iteration 3) — claim `C-0009`, raises `CH-0008`. **Split verdict: the plate is upheld for smooth loads and rejected for point-coupled ones**, and its error *changes sign* with how the load meets the sheet — it understates concentrated and thermal dishing by 12–38 % and 11–20 %, and **overstates** anchored and edge-taper dishing by 1–16 %. Peak per-crossover force at a discrete anchor is **5.63 pN nominal and 11.54 pN worst-case**, a **2.3–7.6× concentration** on `C-0006`'s equal-share figure, and the worst case **reaches the 10–15 pN unzip allowable**. No `C-0006` verdict moves. Superseded note — was: **medium**. Raised because `ℓ_⊥/p = 0.26–0.52 < 1` across the whole sweep, so the continuum reduction is marginal by its own criterion, and because the plate model cannot resolve the one number `T-5` had to decline. |
| T-6 | Validity boundary of mean-field screening at 2 mM Mg²⁺ | Quantified deviation from mean-field, with the boundary stated | A7.4 | **DONE** (iteration 3) — claim `C-0005`, raises `CH-0004`. Mean field is **uncontrolled across the whole 5–10 nm working range** (deviation 123–214 % for Mg²⁺) yet **qualitatively safe** there: correlation attraction needs a gap under 1.46 nm and the layer holds the tile 3.4–6.8× outside it. Controlled PB only above 12.9 nm. |
| T-3a | 1-D nonlinear Poisson-Boltzmann profile in the actual 2:1 buffer, tile + electrode as one system | Its own `σ_eff` for `T-3`, replacing the symmetric-`z:z` ceiling `C-0005` could only quote as an order of magnitude | A7.4 | **DONE** (iteration 3) — claim `C-0008`, raises `CH-0007`, **resolves `CH-0004`**. The 2:1 first integral was derived, not adapted; `C-0005`'s ceiling is confirmed as a ceiling and is 24 % high, by exactly `6 − 3√3`. The tile is **charge-saturated**, so the charge ambiguity `C-0005` could not resolve moves `σ_eff` by only 7 %. |
| T-6b | Size-modified (Bikerman) PB at the electrode | The 0.197 V point-ion boundary sharpened, or replaced | new | **Downgraded to low** — its Bikerman half was folded into `T-3a` (the point-ion model is exactly the `n_max → ∞` limit, so it cost one function and became an executable limiting case). What remains is the **Stern capacitance**, which `CH-0007` makes load-bearing for the diffuse-drop→applied-bias mapping. |
| T-3b | 2-D Poisson-Boltzmann solve of the tile edge | The lateral load non-uniformity `T-5b` needs, as a profile rather than a parameter | A7.4 | **DONE** (iteration 4) — claim `C-0022`, raises `CH-0025` and `CH-0026`. **§4(g) CLOSES, and the edge has the opposite sign to the one three claims carried.** The rim *gains* load: `(depth, width) = (−0.303, 8.94 nm)` against `C-0006`'s assumed `(+0.50, 4.00 nm)`, the finite tile acting as one **1.65 nm larger on every side** and carrying **+14.7 %** more total force than a 1-D pressure over its footprint (+4.9 % to +19.2 % over the box, **+25.8 %** at 20 nm). Dishing **32.1 % of the stroke** (21–44 % over the foundation sweep), so the **lever/sensor split is 32 %, irreducible** — replacing `C-0012`'s 11 %–369 %, which was a statement about the coupling. Per-load-path forces from the edge are **50× below the unzip allowable**, so no `C-0015` force and no `C-0016` window edge moves adversely. Centre-line reproduces `T-3a` to 0.03–0.14 % at 21/21 points. |
| T-3c | 3-D corner solve of the tile, replacing the two-mapping bracket | The corner's contribution to the total force and to the dishing, against the 1.8-point (40 nm) / 7.2-point (20 nm) bracket `C-0022` leaves | A7.4 | TODO — **low-medium**. Raised by `C-0022`. Worth what the bracket is wide, and no more: at the Gen-1 footprint that is 1.8 percentage points of total force against a mean-field error of 123–214 %. It becomes worth more if the programme ever considers a tile below ~30 nm, where the bracket widens to 7 points and the whole edge correction reaches 26 %. |
| T-3d | Whether the PEG layer moves the edge **ratio**, not just the force | The taper depth and width re-solved with `C-0005`'s partitioning medium in the gap | A7.4 | TODO — **low**. Raised by `C-0022`. The layer amplifies the 1-D force by 1.15–1.60× (`C-0008`); `PoissonBoltzmannEdge` already accepts a `GapMediumProfile` and the sweep does not use one, so this is a parameter change and a re-run, not new code. |
| T-11 | The aqueous electrochemical window: is 1–2 V applicable at all in MgCl₂? | The usable bias ceiling set by water electrolysis (1.23 V thermodynamic), against §3's 2 V | new | TODO — **downgraded to low** by `C-0012`: every threshold in the programme is now below 0.7 V and the usable window is 0.02–0.1 V, so 1.23 V never binds. Was: low–medium. `C-0008` notes that because the force **saturates** in bias, the answer barely moves its conclusions — *"but that is luck, not an argument."* |
| T-12 | **Lateral confinement of the tile** | An anchoring scheme delivering `k_lat ≥ 0.4602 pN/nm`, or a demonstration that none is available | A1.2 | **DONE** (iteration 3) — claim `C-0014`, raises `CH-0013`. **PASS: two schemes work, and the interesting result is why the obvious one fails.** A vertical strut has the anisotropy *inverted* — it fails by 40–160×, costs 96–99 % of the stroke, and is **destabilised by its own duty** (a column at its Euler load has exactly zero lateral stiffness). What works is a load path lying *in* the surface, or the ssDNA tether `C-0010` dismissed. **Its `L_min` table and its 7.6× column are superseded by `CH-0021`** — consume `C-0020`'s table instead, **with the tether's alignment quoted**. Superseded note — was: **HIGH**, and cheap (hours). `C-0010` shows the layer's lateral restoring stiffness is **exactly zero by symmetry**, not merely small, so the tile diffuses **62.8 nm in one 1 kHz period** — 21× the §6 predicate and 1.6 tile widths. §3 specifies nothing that confines it. This is a second **topological** axis on `T-2`'s window, beside `C-0006`'s distributed-coupling constraint. |
| T-14 | Crossover phase and anchor registration as a design variable | The peak per-load-path force as a function of crossover column count and anchor placement | A8.2 | **DONE** (iteration 3) — claim `C-0015`, raises `CH-0014`. **The lever is registration (×1.43–1.60), not crossover count** — `C-0009`'s 19 % count effect is 0.3–3.4 % once registration is controlled, **and its sign flips** (7 columns is the *better* layout). The governing variable is the attachment's **distance to the nearest crossover**, and the duplex shear runs the *opposite* way along it. **Flatness needs 45 attachments as 3 × 15, not 64 as 8 × 8** — `C-0009` searched the square diagonal of a two-parameter space on a 25.6×-anisotropic sheet. **One attachment row per duplex zeroes the per-path force exactly.** |
| T-17 | "One attachment row per duplex" as an output-coupling scheme | The exact-zero per-path force costed against `T-12`'s lateral confinement and `T-13`'s hold-down, and its survival under `T-3`'s edge taper and thermal excitation quantified | A8.2 | TODO — **medium**. 45 tethers at 2.22 pN each with **exactly zero** crossover force under a uniform load (`C-0015`). Both `T-12` and `T-13` need attachments anyway, so this may be one scheme rather than three. The exact zero is as fragile as "a uniform load dishes nothing" — any non-uniformity restores it in proportion. |
| T-25 | **Re-synthesise the design window against iteration 4's results** | `C-0016`'s window and `C-0017`'s verdict re-run against `C-0018`, `C-0020`, `C-0021` and `CH-0024`, or a statement that no edge moves | A2.1 | TODO — **HIGH, and it is the coordinator's own task**. Four results landed in one iteration that touch the synthesis: the usable-bias ceiling is now a `(bias, load line)` property with a **1.007–1.032** margin at 10 nm / 2 mM (`C-0018`); lateral confinement is **no longer a footprint constraint** but buys a stroke-independent 54.9 pN preload (`C-0020`); the tile has **no zero-bias resting position** without added tethers, and the committed coupling supplies **zero** hold-down (`C-0021`); and every stroke in the programme is quoted from a height the tile never occupies, costing **2–13 %** (`CH-0024`). `C-0016`'s own lesson applies to itself — three of five axes do not resolve in `σ`, so check which axis each new constraint lives on before intersecting. |
| T-18 | Does a layout rule derived on a 40 nm tile transfer? | The along-helix registration separated from position-in-tile, on §3's 70 × 100 nm test tiles | A8.2 | TODO — low-medium. The Gen-1 tile is only **3.7 unit cells wide along the helices**, and the along-helix periodicity residual is 4.7–17.7 % against 0.3–1.9 % across them, so `C-0015`'s extrema are extrema over placements *on this tile*. |
| T-1e | Invert `N` on the **first-moment** thickness `2⟨z⟩` as well as the force-onset height | The definitional part of `CH-0010`'s chain-length gap separated from the physical part exactly, rather than by scaling | A2.1 | TODO — **medium, and cheap**; the machinery exists. `C-0011` reports `N(10 nm) = 62.1` against `C-0003`'s 224.8–374.3, and states honestly that **most** of that gap is the height convention (a 10 nm *first-moment* thickness would need `N ≈ 190–210` by scaling). What is **not** definitional is 78 pN against zero. |
| T-1f | Bound the mean-field fluctuation corrections at `φ ≈ 0.01` | The correction bounded, or declared unbounded with the missing method named | A2.1 | TODO — **now the binding uncertainty in the programme**, promoted by `C-0017`: the 10 nm coupling margin is 1.19–1.42× and it sits *inside* `C-0005`'s 123–214 % one-loop correction, so the window's survival is **not excluded rather than established** until this is bounded. Was: **medium-high**. Now **the largest unbounded exposure** under `C-0011`, which says plainly that it does not bound them: the layer sits at `φ ≈ 0.01`, which is where mean field is furthest from safe. |
| T-16 | **Minimum output-coupling stiffness** — what a DNA-origami lever can actually deliver | The coupling stiffness a lever can supply at the tile, against what the operating point demands | A8.2 | **DONE** (iteration 3) — claim `C-0017`, raises `CH-0016`. **The requirement is 33.333 pN/nm, fixed by §3 alone** (`= 100 pN / 3 nm`, preload-free) — *not* the 5–277 pN/nm range, which was a stability boundary read at biases the device does not use. Read at the located operating bias the stability floor is **0 at 5 and 7 nm** and **23.4–27.9 pN/nm at 10 nm** (2 mM). Scheme **K2** — 45 attachments on `C-0015`'s own 3 × 15 flatness grid, each a 5 nm duplex standoff in series with a **13 nt tuned ssDNA spacer** — supplies it at **2.22 pN per path** against a 10 pN allowable. **`C-0016`'s P2 closes NON-EMPTY at 7 and 10 nm.** Margin at 2 mM is only 1.19–1.42× against `C-0005`'s 123–214 % mean-field error, so the verdict is **not excluded, never established**; 0.5 mM buys 6×. |
| T-15 | The in-plane (membrane) load path into the tile, by shear lag | The in-plane force-concentration factor, replacing `C-0009`'s out-of-plane one used as a bound | A8.2 | **DONE** (iteration 4) — claim `C-0020`, raises `CH-0021`. **The factor is 1, and it is bought by a design rule.** A lateral tether collects **nothing** (`C-0010`'s exact zero) and the in-plane **sharing length is 65 nm against a 40 nm tile**, so **aligned with the helices `η = 1.0000` exactly**, at all 480 placements and over four decades of the one undetermined input — `L_min` at the **desired 10 nm stroke falls 93.3 → 33.5 nm** and the assembly from ~227 to ~107 nm. **But the stand-in was NOT conservative when misaligned**: the worst of 7200 placements gives `L_min = 115.9 nm`. **Layout is worth ×1.0000 here against `C-0015`'s ×1.43–1.60.** The declared falsifier **fired** — an oblique tether's moment is reacted by the crossovers as an axial couple, `η` → 2.33, bounded and saturating. The footprint incompatibility **changes currency**: the minimum-length tether's normal preload is `n A √(2A/S)` = **54.9 pN, stroke-independent, 55 % of the §3 target**. |
| T-19 | The attachment's entry topology — what a tether actually bonds to | The peak per-load-path force for a tether bonded across two duplexes, or onto a crossover, against the one-point-on-one-duplex model | A8.2 | **DONE** (iteration 4) — claim `C-0024`, raises `CH-0029`. **`C-0020`'s headline survives, as arithmetic.** It rests on **cut equilibrium**, not on the one-point model: on a `D`-duplex tile `η ≥ 1/D = 0.0667` for **any** topology (`A_eff ≤ 720 pN`, an edge clamp), and an `m`-duplex bond **enters at exactly `1/m`** — over 3840 designs (every width × position × 32 phases) the peak exceeds `1/m` by ≤ **4.7 %**. **The halving is exact to 4 %**, the compliant/rigid split limits bracketing it to 11.4 %. **It costs nothing in the crossover path — it pays ×1.88**, and that path never becomes binding. **Bonding onto a crossover ≡ a two-duplex bond.** **The footprint is worth nothing**: over the complete phase sweep the worst 8 bp footprint is `η = 1.0000`, its apparent relief being load shed past the first crossover column. **The design content is in the JOINT**: Strunz's own constants give **18.8 pN at 8 bp, 34.8 at 16, 47.1 at 30** against the flat 48 in use, with a **14.3 bp split break-even**. `L_min(10 nm)` = 27.7 nm for a split 32 bp staple (not ~24 — the joint binds first), 39.4 nm for an unsplit 16 bp one. |
| T-35 | Does a staple domain inside an origami sheet rupture like a free oligonucleotide? | The shear rupture force of a hybridised domain flanked by crossovers, against Strunz's free-oligo scaling, or a demonstration that no measurement exists | A8.2 | TODO — **medium-high, and it is the way `C-0024` would fail**. Every per-path shear allowable in this programme traces to an AFM measurement on **free oligonucleotides** on a PEG linker. `C-0024`'s whole joint argument — the 18.8/34.8/47.1 pN ladder and the 14.3 bp split break-even — assumes that transfers. A domain inside a sheet is flanked by crossovers and neighbours, and whether that raises or lowers its rupture force is established nowhere. `T-9`'s oxDNA could bound it. |
| T-36 | The bonded length as a first-class design variable across the programme | Every claim that consumes a "48 pN" or "10 pN" per-path allowable re-read with the domain length it actually implies | A8.2 | TODO — **medium, and cheap**. `CH-0029` shows the allowable is a function of bonded length; `C-0009`'s crossover path, `C-0014`'s tethers, `C-0017`'s 45 coupling paths and `C-0021`'s hold-down all quote it length-free. Unzip is length-independent and shear is not, so the correction is uneven and cannot be applied as one factor. |
| T-37 | Where on the tether-length axis should the design sit? | The tether length chosen against preload, stroke cost and footprint together, rather than at `L_min` | A1.2 | TODO — medium. `C-0024` shows `L_min` is a *corner*: the preload there is 25–186× `C-0021`'s requirement, and the length that delivers exactly the wanted hold-down at the 10 nm stroke is 116.6 nm. `T-13`'s hold-down, `T-12`'s confinement and `T-2`'s footprint are three constraints on **one** scalar, and nothing has yet optimised it. |
| T-21 | The semidilute→concentrated crossover for **this** layer, replacing `C-0002`'s cited 0.2–0.3 band | The `φ` at which the des Cloizeaux exponent stops being the one the layer is entitled to, derived rather than read off a band | A2.1 | TODO — **medium-high**. Raised by `C-0018`: `φ = 0.2` is the binding ceiling at **121 of 162** states, so the usable bias of the whole device now rests on a *cited* number read at the floor of a 0.2–0.3 band. Moving it to 0.3 would raise 121 ceilings. Worth more than any further electrostatics refinement. |
| T-22 | Dynamic pull-in: does a bias step carry the tile past a fold a quasi-static ramp stops at? | The fold re-evaluated against `C-0004`'s drainage corner and the tile's inertia, or a demonstration that the quasi-static reading holds below a stated frequency | A2.2 | TODO — low-medium. Raised by `C-0018`, which is static throughout. `C-0004`'s corner is 91 kHz–2.3 MHz, so the quasi-static reading is right below ~10 kHz; above that nothing in the programme covers a bias step. |
| T-20 | Membrane–bending coupling on a dished tile | Whether the in-plane and out-of-plane load paths stay decoupled once the tile is not flat | A8.2 | TODO — low-medium. `C-0020` decouples them exactly at linear order for a **flat** sheet, and `C-0006`/`C-0009`/`C-0015` all reject the rigid-plate assumption: the tile dishes by 26–369 % of the stroke. The coupling is second order in the slope and its **direction is not established**. It is the way `C-0020` would fail. |
| T-13 | Where the tile sits at zero bias | Whatever holds the tile down, named and quantified | new | **DONE** (iteration 4) — claim `C-0021`, raises `CH-0023` and `CH-0024`. **The §3 stack has NO zero-bias resting position — undefined, not large: 0 of 18 states return an equilibrium.** What *is* unavoidably there — van der Waals (0.245–28.1 pN over the electrode bracket) and the residual field (0.078–0.404 pN) — gives a **stable but NOT CONFINING** equilibrium: a `1/h³` force has a bounded potential, so the well is **0.2–5.7 `k_BT`** and **0 of 54 states confine**. Same for the device as specified (layer + `K2` + vdW + field): **1.40–5.37 `k_BT`, 0/18**. **`C-0014`'s eight substrate tethers close it**: `h₀` = **4.62–9.78 nm**, well **30.6–73.4 `k_BT`**, 18/18, RMS **0.360–0.501 nm** broadband and **0.019–0.041 nm** in band, for **0.07–0.38 nm** of stroke. **`C-0017`'s `K2` supplies EXACTLY ZERO** — 99.6 % of its compliance is ssDNA, which carries no compression — but the two tasks are one variable: **`F_down = (k_c − 33.333)·3 nm`**. The requirement is a **force**, `k_BT/3 nm = 1.3806 pN`, not a stiffness. |
| T-23 | A **two-sided** compliant DNA coupling — an antagonistic spacer pair, or a bending hinge in place of a stretched chain | An element that carries load in both directions at ≤ 40 pN/nm, or a demonstration that DNA offers none | A8.2 | **DONE** (iteration 4) — claim `C-0023`, raises `CH-0027`. **Three exist, and the requirement they were wanted for dissolves.** A **transverse duplex flexure** (span 24.61 nm = 72 bp, ends free to draw in), a **crossover-hinge flexure** (arm 4.11 nm = 12 bp, 92.5 % of its compliance in the hinge) and an **antagonistic ssDNA pair** (45 × 13 nt up against one 68 nt down limb) all place at 33.333 pN/nm exactly, stay linear, and put 2.2–3.4 pN per path against a 10 pN unzip allowable. But **two-sidedness changes the currency**: the potential above `L₀` becomes quadratic, so the requirement is `k_BT/σ² = 0.4602 pN/nm`, which §3's own mandate exceeds **72.4× unpreloaded** — `F_req = k_req·σ` identically. On `C-0021`'s own balance the tetherless device goes from **1.4–5.4 `k_BT`, 0/18 confining** to **959–7582 `k_BT`, 18/18**, RMS 2.56–12.98 → 0.217–0.352 nm, so **`C-0014`'s eight substrate tethers leave the design**. The preload branch is priced and rejected: it is a **mounting offset**, i.e. a length, and the requirement asks for 0.0409 nm against a 0.34 nm base-pair quantum that delivers 9.3× too much. One base pair of offset buys 0.5–1.1 % of `C-0018`'s bias margin; the buffer still buys 6×. |
| T-30 | The origami joint at a flexure's end — does it draw in, and does it clamp? | The flexure's end condition and axial restraint fixed by a joint model rather than bracketed | A8.2 | TODO — **medium-high, and it is the binding open choice under `C-0023`**. The two brackets are worth **2.2× in span** (24.6 → 49.4 nm) and **2.7× in tangent stiffness** (33.3 → 91.1 pN/nm), and the restrained reading **fails the compliance ceiling and breaks the 65 pN nicked ceiling at §3's desired 10 nm stroke** (86.7 pN). The demand is quantified: **0.88 nm = 2.6 base pairs of in-plane draw-in**, which a two-nucleotide single-stranded hinge at each end would absorb. A sequence-design question, not physics. |
| T-31 | Does a flexure array on a common superstructure stay as compliant as independent leaf springs? | The array stiffness against `n` independent beams, with crossover coupling between neighbours | A8.2 | TODO — medium. `C-0023` models 45 leaf springs as independent, which is the **compliant** reading — and the compliance **ceiling** is the binding side, so the assumption is *not* conservative. Crossovers between neighbouring flexures would stiffen the array toward `E3b`'s failing corner. |
| T-32 | The dishing a **two-sided** coupling causes | The tile's flatness under a coupling that loads it in both directions | A8.2 | TODO — low-medium. Every flatness result in the programme (`C-0006`, `C-0009`, `C-0015`) is for a coupling that only pulls. A two-sided element loads the tile *upward* at some attachments and *downward* at others during a thermal excursion, which `C-0006`'s uniform-load exact-rigidity result does not cover. |
| T-33 | Is the lever's own load path two-sided? | The superstructure's ability to react a downward push, budgeted as `C-0017` budgets its bending | A8.2 | TODO — **medium**. **A coupling that can push is only as two-sided as the path behind it.** `C-0023`'s whole verdict assumes the lever is vertically *and* laterally grounded — `C-0017`'s assumption, now load-bearing in a new direction. If the superstructure cannot react a downward push, `C-0021`'s force requirement returns and `C-0014`'s tethers come back with it. |
| T-24 | Does PEG bridge a DNA-origami face? | The adsorption energy per chain against `C-0021`'s 0.002–0.009 `k_BT` threshold, or a demonstration that no accessible measurement resolves it | new | TODO — **medium, and it is a shared premise**. `C-0021` shows hundredths of a `k_BT` per chain would supply the whole hold-down, so "PEG does not adsorb to DNA" cannot be read as "bridging contributes nothing". `P-8`'s missing Mg²⁺/PEG constant is the mechanism that would flip it — **and it would take `C-0010`'s exact lateral zero with it**. |
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
| `./gradlew study -Pstudy=structure.DiscreteLatticeTileStudyKt` | `T-10` | `gpd/results/T-10-discrete-lattice-tile.json` |
| `./gradlew study -Pstudy=material.GraftedChiStudyKt` | `P-9` | `gpd/results/P-9-grafted-chi.json` |
| `./gradlew study -Pstudy=anchoring.LateralConfinementStudyKt` | `T-12` | `gpd/results/T-12-lateral-confinement.json` |
| `./gradlew study -Pstudy=actuator.StrokeAndBlockingForceStudyKt` | `T-3` | `gpd/results/T-3-stroke-and-blocking-force.json` |
| `./gradlew study -Pstudy=brush.ScfDensityProfileStudyKt` | `T-1d` | `gpd/results/T-1d-scf-density-profile.json` (~33 min) |
| `./gradlew study -Pstudy=structure.CrossoverRegistrationStudyKt` | `T-14` | `gpd/results/T-14-crossover-phase-and-registration.json` (~3 min) |
| `./gradlew study -Pstudy=window.DesignWindowStudyKt` | `T-2` | `gpd/results/T-2-design-window.json` (~2 s) |
| `./gradlew study -Pstudy=coupling.OutputCouplingStudyKt` | `T-16` | `gpd/results/T-16-output-coupling-stiffness.json` |
| `./gradlew study -Pstudy=structure.InPlaneLoadPathStudyKt` | `T-15` | `gpd/results/T-15-in-plane-shear-lag.json` (~35 s) |
| `./gradlew study -Pstudy=actuator.MaximumUsableBiasStudyKt` | `T-4` | `gpd/results/T-4-maximum-usable-bias.json` (~7 min) |
| `./gradlew study -Pstudy=anchoring.ZeroBiasRestingPositionStudyKt` | `T-13` | `gpd/results/T-13-zero-bias-resting-position.json` (~40 s) |
| `./gradlew study -Pstudy=structure.EntryTopologyStudyKt` | `T-19` | `gpd/results/T-19-attachment-entry-topology.json` (~90 s) |
| `./gradlew study -Pstudy=anchoring.TwoSidedCouplingStudyKt` | `T-23` | `gpd/results/T-23-two-sided-coupling.json` |
| `./gradlew study -Pstudy=electrostatics.TileEdgeLoadProfileStudyKt` | `T-3b` | `gpd/results/T-3b-tile-edge-load-profile.json` |

Add `-PbuildDirectory=<dir>` to any Gradle command when more than one agent is working this checkout (`P-7`),
and use [`tools/verify.sh`](tools/verify.sh) for an authoritative full-suite run — at four or more concurrent
agents the shared project lock defeats `-PbuildDirectory` on its own (`P-10`).
Run a study through [`tools/study.sh`](tools/study.sh) rather than `./gradlew study` whenever another agent is
working the checkout: `-PbuildDirectory` does **not** isolate `build-*/classes`, so a sibling build can delete
the classes out from under a multi-minute run (`P-12`).
Both scripts take `--drop <pkg>`, which removes a package another agent has left mid-TDD from the copy.

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
- **The 10 nm design window exists, and so does a 7 nm one.** Under a *solved* density profile the 10 nm window
  is `σ ∈ [0.0116, 0.2601] nm⁻²` — **22.4× wide**, against strong stretching's 3.5× and the box models'
  *empty* — and 7 nm gives `[0.0296, 0.0496]`. **"Empty at 5 nm and 7 nm" is withdrawn**; it was carried from
  `C-0001` through three iterations. 5 nm is still empty and the ~10 nm desired stroke is still unreachable
  everywhere, which is `C-0001`'s one headline to survive a third independent model. (`C-0011`.)
- **The two profile models agreed with each other because they share a defect, not because they bracket an
  answer.** Neither the Alexander box nor strong stretching contains the chain's *entropic resistance to
  confinement*, and against an absorbing wall that term is not a correction — it is the **whole** disjoining
  pressure. Where both models say the tile floats free, the solved layer holds **78 pN**. (`C-0011`.)
- **The Gen-1 layer's height is a coil height, not a brush height.** `L₀ ∝ N^(0.49–0.64)`, not `N¹`, and
  `L₀ ≈ 1.6–2.9 R₀` everywhere — the resting height is set by the tail of a nearly-ideal coil, not by an
  osmotic balance. Every scaling height relation in the programme had assumed the opposite. And an SCF layer
  **has no resting height at all** unless one is defined: `P = 0` only asymptotically, so `L₀` is a convention,
  and a hundred-fold change in the defining load moves `N` by 2.5×. (`C-0011`, `CH-0010`.)
- **No part of the layer is semidilute — it sits *below* `φ#`, not above it.** `φ/φ# = 0.138` at the design
  point, peaking at 0.378. This reverses the direction `C-0003` asserted, and it puts the layer exactly where
  the fitted `αφ^(9/4)` limb is **least** constrained by data. (`C-0011`, `CH-0010`.)
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
- ~~**A bulk `χ` is not a brush `χ`, and that gap is 239× everything else in this section.**~~
  **CORRECTED by `C-0013`/`CH-0012`. A bulk `χ` and a brush `χ` differ by at most 0.053, and the 0.23 that was
  feared was a units error.** `χ ≈ 0.60` for a grafted PEO layer was `1.2 × ½` assembled from an abstract —
  the source's own fits are 0.789/0.852 on a scale whose theta is **0.696**, for an air/water Langmuir
  monolayer under *lateral* compression, and the paper forbids that transfer explicitly. Normal-compression
  osmotic stress on grafted PEG **denser** than the Gen-1 window gives `χ_eff = 0.346–0.424` against a bulk
  0.372 — worth −11.4 % to +4.3 % of the stiffness, a fifth of the model bracket already carried.
  **What remains open** is that no compression measurement exists *inside* the Gen-1 window, so the bound
  comes from above and assumes monotonicity in grafting density. (`C-0013`, `CH-0012`.)
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
- **The output coupling has two requirements and only one of them contains physics.** Stability fixes a lower
  bound; **placement** fixes the value, because the force delivered over a stroke is `k_c·Δs` — so §3's own
  100 pN and 3 nm give **33.333 pN/nm by arithmetic**. Placement is written on the coupling's **secant** and
  stability on its **tangent**, so a strain-stiffening element discharges both with one part and the
  tangent/secant ratio is free stability margin. **The stiffness was never the constraint** — 45 duplexes in
  tension are 148× too stiff; the design problem is spending stiffness as *compliance*, and the dominant
  compliance term is a 13 nt ssDNA spacer at 99.6 % of the load path. (`C-0017`, `CH-0016`.)
- **Normal stabilisation and lateral confinement want the *same* anchors, not opposite ones**, because the two
  requirements differ by **72.4×** (33.3 against 0.46 pN/nm). An anchor sized on the normal condition delivers
  lateral confinement with 70× to spare; one sized on the lateral condition delivers 0.4 % of the normal.
  (`C-0017`.)
- **Three of the five axes this programme discovered do not resolve in grafting density at all.** Flatness,
  the usable bias window and the output-coupling stiffness are height-level or topological, so they cannot
  *narrow* a window — only *close a height*. **A `(σ, L₀)` window is the wrong shape for the Gen-1 decision**,
  and a constraint that cannot narrow is invisible to an intersection. (`C-0016`.)
- **`L₀/R₀ ≥ 1` is exactly vacuous, not weak** — it admits all 183 points of the sweep, including `Σ = 0.063`
  at 22 nm spacing. Coil overlap owns the lower edge of every surviving window. (`C-0016`.)
- **The unzip exceedance `C-0015` found is unreachable inside the window** — the solved layer's foundation
  multiplier is 0.823–1.605 and `C-0015` entered the band only at ×0.25 of the foundation sweep. A loosening
  neither claim could see alone. (`C-0016`.)
- **Static stability wants the thin layer, whose window is empty; the window, the stroke and the force trade
  all want the thick one.** All three pull the same way and stability pulls against all three. **That
  inversion, not any single number, is the Gen-1 design problem.** (`C-0016`.)
- **The §6 task 3 target is reachable, and the operating point it is reachable at is not one the device can be
  held at.** 100 pN *at* a 3 nm stroke needs only 0.082–0.368 V — but at the loaded operating point
  **`k_eff < 0` at 7 nm and 10 nm**, so the §6 target requires an output coupling that supplies **5–72 pN/nm of
  its own stiffness**, comparable to the whole layer at first contact. And the *free* operating point leaves
  three upstream validity ranges at once above ~0.1 V, so **the usable bias window is 0.02–0.1 V, not 0–2 V**.
  (`C-0012`.)
- **The blocking force is not the peak output force — it understates it by up to 20×.** `dW/dh = k_eff`
  exactly, so wherever the field softens the layer the characteristic *rises* with stroke. (`C-0012`.)
- **`k_es` changes sign at 0.55–1.58 nm, and that is what arrests the collapse.** §6 task 4's second branch is
  nearly right, but the arresting mechanism is **electrostatic, not osmotic**. (`CH-0011`.)
- **The two halves of §6 task 3 run in opposite directions with layer height** — blocking force gets 10×
  harder from 5 to 10 nm while stroke gets 10× easier — so a single "bias needed" figure hides both.
  Static stability then opposes both: 5 nm is stable and 10 nm is not. (`C-0012`.)
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
- **Lateral confinement is decided by anchor *orientation*, not material — and it puts a footprint constraint
  on the device that scales with the stroke.** For any flexible link crossing the layer, `k_lat/k_norm` is the
  secant over the tangent of its force-extension law, which convexity bounds at **≤ 1**: a through-layer path
  costs at least as much normal stiffness as it buys laterally. A rigid vertical strut is 40–160× worse still,
  costs 96–99 % of the stroke, and is **destabilised by the load it exists to resist**. What works is a path
  lying *in* the surface — but such a tether converts stroke into tension geometrically, so
  **`L_min = δ√(Sn/2A)`**: 28 nm of tether for §3's acceptable 3 nm stroke, **93 nm for the desired 10 nm**,
  i.e. a ~100 nm assembly around a 40 nm tile, rising to ~230 nm. **Lateral confinement and the desired stroke
  are incompatible at a fixed footprint — not in physics.** (`C-0014`.)
- **Over-stiffening an anchor is not free.** The per-anchor thermal force is `√(k_BT k)/N`, so a 120×-margin
  scheme puts 29 pN on a load path where the minimum design puts 2.6 pN — past the unzip allowable. (`C-0014`.)
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
- **The output coupling must be near-continuous, but the count is 45 and the "more attachments than load
  paths" reading is withdrawn.** Searched over grid *shapes* rather than counts — the sheet is 25.6× stiffer
  along the helices than across them, so the square diagonal was the wrong slice — flatness needs **45
  attachments as 3 × 15**, i.e. **0.80 per crossover**, not `C-0009`'s 64 as 8 × 8 and not `C-0006`'s 55.
  Stronger: **one attachment row per duplex zeroes the peak per-load-path force exactly**, by the same
  symmetry that makes a uniform load dish nothing — and it is just as fragile, since any load non-uniformity
  restores it in proportion. (`C-0006`, `C-0009`, `C-0015`, `CH-0014`.)
- **A load-path contour average is not a peak.** A rigid anchor is carried by its **two nearest crossovers and
  essentially nothing else**, so the equal-share figure understates the peak by **2.3–7.6×**. The worst case
  reaches the 10–15 pN unzip allowable — but `C-0015` shows **layout nearly closes it**: at the design point
  every layout is clear of the band, and at the soft end the best registration removes 4.60 pN of a 4.65 pN
  excursion, landing 0.5 % inside. The lever is the attachment's **distance to the nearest crossover**
  (×1.43–1.60), not the crossover count, whose effect is 0.3–3.4 % **with the opposite sign** to `C-0009`'s.
  For a *concentrated* coupling, layout alone decides whether one attachment sits above or below the 48 pN
  duplex-shear allowable. (`C-0009`, `C-0015`.)
- **A discretisation is not automatically a relaxation.** The continuum plate errs in **both** directions and
  the sign is set by how the load meets the sheet, not by the discreteness: softer under a point load
  *entering* it, stiffer under a point *reaction* and a smooth taper, because a 2 nm duplex cannot bend across
  its own diameter and the continuum lets it. `C-0006` asserted the softening direction universally.
  (`C-0009`, `CH-0008`.)
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
- ~~**Lee et al., *J. Phys. Chem. B* 116:7367 (2012)**~~ — **STRUCK. The body was obtained free**, from NIST's
  public repository, because two coauthors are NIST staff. Worth recording as a general lesson: **Unpaywall
  and OpenAlex both report this paper `closed` and both are wrong.** Check the authors' institutions for
  federal or institutional repositories before declaring a body unreachable.
- Any PEG/PEO salt study **below 50 mM**. `C-0007` found none at all — every cloud-point and aqueous-two-phase
  paper works at 0.1–3 M, which is two orders of magnitude above the Gen-1 buffer and exactly where a
  non-monotonic `θ(c)` would have its structure. This may simply not exist rather than be paywalled.

Neither is blocking: `C-0007` bounds the effect they would pin at ≤ 0.4 % of the layer modulus, and states the
bound as a falsifiable threshold rather than a guess. They would convert a bound into a number.

**2. What is the electrode made of, and where is its potential of zero charge?**
Raised by `C-0021` as `P-13`, and it is a **specification** gap rather than a modelling one, so no amount of
calculation closes it.
§1 says *"patterned electrode"* and never says of what.
Metal against oxide is **2.6×** on the van der Waals hold-down — the one term no design can remove — which is
larger than the DNA Hamaker constant (1.17× after the square root), larger than retardation, and larger than
the polymer in the gap.
And zero *applied* bias is not zero *charge*: a contact potential of **0.9–5.1 mV**, below anything a bench
would call zero, supplies the **entire** thermal-scale hold-down by itself.
A better calculation of the wrong material is not an improvement, so the answer is bracketed and handed back
rather than chosen.

**3. Nothing has yet needed more compute than this box provides.** `T-9` (crossover hinge constant from oxDNA)
is the first queued item that would run for *days* rather than minutes — costed at 2–5 k nucleotides and
µs-scale umbrella sampling on 8 cores. It fits the machine; it does not fit inside one session.
Flagged so the decision to start it is yours rather than made by accident.
