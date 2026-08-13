# gpd — the Formulate → Plan → Execute → Verify record

This directory *is* the deliverable.
The Kotlin under `src/` is how the numbers get produced; this is what they mean and what they are worth.

The loop is NDI's, from §5 of [the problem definition](../third-party/2026-08-ndi-gen1-problem-definition.md),
and it is run as described in [`../SESSION-PROMPT.md`](../SESSION-PROMPT.md).

| Directory | What is in it |
|---|---|
| `tasks/` | One file per task, carrying all four stages: the numeric target and acceptance predicate, the method and its cost justification, what would falsify the approach, the run, and the five verification gates. |
| `results/` | Machine-readable run output. Every parameter of the run is in the file, so the result is reproducible from it alone. Deterministic filenames — a re-run that changes nothing produces no diff. |
| `claims/` | A verified result, with provenance, validity range, acceptance verdict, and an explicit list of the numbers that are *cited* rather than derived. |
| `challenges/` | A result that contradicts a standing claim is raised here, with methodological grounds. Claims are never silently overwritten. |

## Reading a claim

Three things in every claim are load-bearing and easy to skip:

- **The verdict is `PASS` at TRL 1–3.** That means model-consistent and traceable. It does not mean measured.
- **The validity range is enforced in code**, not just documented — a downstream task that leaves it gets an exception, not a plausible number.
- **The "cited rather than derived" list** is where the claim is weakest, and it is where the next iteration's blockers come from.

## Index

| ID | Claim | Task | Leaf | Verdict |
|---|---|---|---|---|
| `C-0001` | [Stiffness of the grafted polymer layer under the Gen-1 tile](claims/C-0001-layer-stiffness.md) | [`T-1`](tasks/T-1-layer-stiffness.md) | `A2.1` | PASS — challenged by `CH-0001`, `CH-0003` |
| `C-0002` | [PEG/water material parameters, and the osmotic law the layer actually obeys](claims/C-0002-peg-material-parameters.md) | [`P-3`](tasks/P-3-peg-material-parameters.md) | premise under `A2.1` | PASS |
| `C-0003` | [The layer response from a crossover-valid free energy](claims/C-0003-crossover-valid-layer-response.md) | [`T-1c`](tasks/T-1c-crossover-valid-layer-response.md) | `A2.1` | PASS |
| `C-0004` | [Poroelastic drainage does not limit the Gen-1 actuator, and what would make it](claims/C-0004-poroelastic-drainage.md) | [`T-7`](tasks/T-7-poroelastic-drainage.md) | none (§4(d)) | PASS |
| `C-0005` | [Validity boundary of mean-field screening at 2–10 mM Mg²⁺](claims/C-0005-mean-field-screening-validity.md) | [`T-6`](tasks/T-6-mean-field-screening-validity.md) | `A7.4` | PASS |
| `C-0006` | [Load distribution across the tile, and the rejection of the rigid-plate assumption](claims/C-0006-tile-load-distribution-and-flatness.md) | [`T-5`](tasks/T-5-load-distribution.md), [`T-5b`](tasks/T-5b-tile-flatness.md) | `A1.2`, `A8.2` | PASS (verdict: rigid plate **rejected**) |
| `C-0007` | [Solvent quality versus salt: the buffer does not reach the layer's mechanics](claims/C-0007-solvent-quality-vs-salt.md) | [`P-6`](tasks/P-6-solvent-quality-vs-salt.md) | premise under `A2.1` | PASS (on both branches of its predicate) |
| `C-0008` | [The electrostatic force on the tile, and the decay length it actually has](claims/C-0008-electrostatic-force-and-decay-length.md) | [`T-3a`](tasks/T-3a-nonlinear-pb-profile.md) | `A7.4` | PASS |
| `C-0009` | [The discrete-lattice tile: where the continuum plate is upheld and where it is not](claims/C-0009-discrete-lattice-tile.md) | [`T-10`](tasks/T-10-discrete-lattice-tile.md) | `A8.2`, `A1.2` | PASS |
| `C-0010` | [Tile positional variance at 300 K, by mode and in band](claims/C-0010-tile-positional-variance.md) | [`T-8`](tasks/T-8-tile-positional-variance.md) | `A1.2` | PASS (partial against the leaf: no ensemble, no CI) |
| `C-0011` | [The SCF density profile: the 10 nm window exists, and the layer is a coil layer](claims/C-0011-scf-density-profile.md) | [`T-1d`](tasks/T-1d-scf-density-profile.md) | `A2.1` | PASS |
| `C-0012` | [Coupled stroke and blocking force: reachable, but not holdable](claims/C-0012-coupled-stroke-and-blocking-force.md) | [`T-3`](tasks/T-3-stroke-and-blocking-force.md) | `A2.2` | PASS, with two clauses failing and reported |
| `C-0013` | [The grafted-`χ` premise is inapplicable, and the bulk equation of state stands](claims/C-0013-grafted-chi-inapplicable.md) | [`P-9`](tasks/P-9-grafted-chi.md) | premise under `A2.1` | PASS |
| `C-0014` | [Lateral confinement: anchor orientation decides it, and it costs footprint](claims/C-0014-lateral-confinement.md) | [`T-12`](tasks/T-12-lateral-confinement.md) | `A1.2` | PASS |
| `C-0015` | [Crossover phase and anchor registration: the lever is registration, and flatness needs 45](claims/C-0015-crossover-phase-and-registration.md) | [`T-14`](tasks/T-14-crossover-phase-and-registration.md) | `A8.2` | PASS |
| `C-0016` | [The feasible design window: non-empty as posed, undecided once the discovered axes are added](claims/C-0016-design-window.md) | [`T-2`](tasks/T-2-design-window.md) | `A2.1` | PASS on §4(a)–(d); its P2 **closed non-empty** by `C-0017` |
| `C-0017` | [The output-coupling stiffness: fixed by §3, and a 45-attachment scheme supplies it](claims/C-0017-output-coupling-stiffness.md) | [`T-16`](tasks/T-16-output-coupling-stiffness.md) | `A8.2` | PASS — closes `C-0016`'s P2 **non-empty** |
| `C-0018` | [The maximum usable bias, in the three ceilings it is made of](claims/C-0018-maximum-usable-bias.md) | [`T-4`](tasks/T-4-maximum-usable-bias.md) | `A2.2` | PASS — the ceiling is a property of the **load line**, and pull-in binds at only 11 of 54 coupled states |
| `C-0019` | [The polymer mean field is broken at the Gen-1 layer, and the response is bounded anyway](claims/C-0019-mean-field-fluctuation-corrections.md) | [`T-1f`](tasks/T-1f-mean-field-fluctuation-corrections.md) | `A2.1` | PASS — raises `CH-0019`, `CH-0020` |
| `C-0020` | [The in-plane load path into the tile: the concentration factor is 1, and alignment buys it](claims/C-0020-in-plane-shear-lag.md) | [`T-15`](tasks/T-15-in-plane-shear-lag.md) | `A8.2` | PASS — raises `CH-0021` against `C-0014` |
| `C-0021` | [Where the tile sits at zero bias: nowhere, and what is there is a trap](claims/C-0021-zero-bias-resting-position.md) | [`T-13`](tasks/T-13-zero-bias-resting-position.md) | `A1.2` | PASS — its force requirement superseded by `C-0023`/`CH-0027` |
| `C-0022` | [The tile edge gains load, and §4(g) closes at 32 % of the stroke](claims/C-0022-tile-edge-load-profile.md) | [`T-3b`](tasks/T-3b-tile-edge-load-profile.md) | `A7.4` | PASS — raises `CH-0025`, `CH-0026` |
| `C-0023` | [A two-sided compliant DNA coupling, and the hold-down requirement that dissolves](claims/C-0023-two-sided-coupling.md) | [`T-23`](tasks/T-23-two-sided-coupling.md) | `A8.2` | PASS — raises `CH-0027` against `C-0021` |
| `C-0024` | [The attachment's entry topology: the sheet's answer is arithmetic, the joint's is not](claims/C-0024-attachment-entry-topology.md) | [`T-19`](tasks/T-19-attachment-entry-topology.md) | `A8.2` | PASS — raises `CH-0029` |
| `C-0025` | [The flexure's end joint: a joint has three stiffnesses, and only bending has a direction](claims/C-0025-flexure-end-joint.md) | [`T-30`](tasks/T-30-flexure-end-joint.md) | `A8.2` | PASS — raises `CH-0031` against `C-0023` |
| `C-0026` | [One attachment row per duplex: the exact zero, costed against the solved load](claims/C-0026-one-row-per-duplex.md) | [`T-17`](tasks/T-17-one-row-per-duplex.md) | `A8.2` | PASS — raises `CH-0033`, `CH-0034` |
| `C-0027` | [Iteration 4 moves one window edge, and it moves outward](claims/C-0027-window-resynthesis.md) | [`T-25`](tasks/T-25-window-resynthesis.md) | `A2.1` | PASS — raises `CH-0035`, `CH-0036` |
| `C-0028` | [What the standoff stands on: the base is not a clamp, and the motif is not in the literature](claims/C-0028-standoff-base-joint.md) | [`T-40`](tasks/T-40-standoff-base-joint.md) | `A8.2` | PASS with the design amended — raises `CH-0037`, `CH-0038` |
| `C-0030` | [The standoff supplies the draw-in it was charged for](claims/C-0030-coupled-standoff-joint.md) | [`T-65`](tasks/T-65-coupled-standoff-joint.md) | `A8.2` | PASS — raises `CH-0041`, `CH-0042`; closes `T-41` |

### Standing challenges

| Challenge | Against | Status |
|---|---|---|
| [`CH-0001`](challenges/CH-0001-semidilute-premise.md) — the layer is not in the semidilute regime | `C-0001` | UPHELD in part |
| [`CH-0002`](challenges/CH-0002-corrections-do-not-all-soften.md) — the corrections do not all soften the layer | `CH-0001`'s direction | **UPHELD** — the direction is withdrawn |
| [`CH-0003`](challenges/CH-0003-blob-stack-height.md) — the layer is ~1.5 blobs tall | `C-0001` | **RESOLVED** by `C-0003`: the height relation is replaced, and the layer is not a blob stack at all |
| [`CH-0004`](challenges/CH-0004-screening-decay-length.md) — "the Debye length" is three different numbers | §1/§3 as read downstream | **RESOLVED** by `C-0008`: the force's own decay length is a fourth number, and the only bias-dependent one |
| [`CH-0005`](challenges/CH-0005-rigid-tile-assumption.md) — the tile is not a rigid plate | `C-0001` | UPHELD |
| [`CH-0006`](challenges/CH-0006-solvent-quality-bound.md) — the ≤ 0.7 % solvent-quality bound is right by accident | `C-0002` | upheld in conclusion, overturned in construction |
| [`CH-0007`](challenges/CH-0007-point-ion-boundary-in-applied-bias.md) — the point-ion boundary was compared against the wrong quantity | this queue's reading of `C-0005` | **UPHELD** — ≈ 1.0 V applied, not 0.197 V |
| [`CH-0008`](challenges/CH-0008-plate-conservative-about-flatness.md) — the plate is not universally conservative about flatness | `C-0006` | raised by `C-0009`; no `C-0006` verdict moves |
| [`CH-0009`](challenges/CH-0009-worst-point-is-not-the-centre.md) — the tile's worst point is not its centre | `C-0006`'s thermal table | raised by `C-0010` |
| [`CH-0010`](challenges/CH-0010-brush-height-is-coil-height.md) — the brush height is a coil height | `C-0003` | **UPHELD in substance and split** |
| [`CH-0011`](challenges/CH-0011-electrostatic-stiffness-changes-sign.md) — `k_es` is not negative everywhere | `C-0008` | raised by `C-0012`; the collapse is arrested electrostatically |
| [`CH-0012`](challenges/CH-0012-grafted-chi-number.md) — the grafted `χ ≈ 0.60` was a units error | `C-0007`'s reading of it | **UPHELD** — the premise is dissolved |
| [`CH-0013`](challenges/CH-0013-entropic-tether-is-not-zero.md) — an entropic tether is not "essentially nothing" | `C-0010` | raised by `C-0014` |
| [`CH-0014`](challenges/CH-0014-layout-sampled-not-swept.md) — the layout space was sampled, not swept | `C-0009` | raised by `C-0015`; two sizes and one sign corrected |
| [`CH-0015`](challenges/CH-0015-usable-bias-window-is-unloaded.md) — the usable bias window is an *unloaded* property | `C-0012` | raised by `C-0016` |
| [`CH-0016`](challenges/CH-0016-coupling-requirement-is-quoted-off-operating-point.md) — the coupling requirement was quoted off the operating point | `C-0012` | **UPHELD on both grounds**; its own direction claim struck |
| [`CH-0017`](challenges/CH-0017-collapse-is-arrested-osmotically.md) — the collapse is arrested by the layer, not by the electrostatic reversal | `CH-0011` | raised by `C-0018`; `CH-0011`'s sign change **upheld and asserted**, its mechanism refuted at 324 of 324 states |
| [`CH-0019`](challenges/CH-0019-two-mean-field-expansions.md) — there are two mean fields here, and the coupling margin sits inside the other one | this queue's promotion rationale for `T-1f` | raised by `C-0019`; nothing queued can narrow the binding one |
| [`CH-0020`](challenges/CH-0020-thermal-blob-coarse-graining.md) — the thermal-blob count is a convention whose two errors cancelled | `C-0003` | raised by `C-0019`; conclusion survives, its margin falls from ~50× to 6.3× |
| [`CH-0021`](challenges/CH-0021-in-plane-factor-is-not-out-of-plane.md) — an out-of-plane concentration factor is not an in-plane one | `C-0014` | raised by `C-0020`; the stand-in is wrong in **both** directions |
| [`CH-0023`](challenges/CH-0023-placement-preload-sign.md) — `placementPreload` returns a downward preload and its KDoc reads it as upward | `C-0017` | raised by `C-0021`; no number, table or verdict moves |
| [`CH-0024`](challenges/CH-0024-stroke-is-measured-from-a-height-the-tile-never-occupies.md) — every stroke is measured from `L₀`, which the tile never occupies | `C-0012`, `C-0017` | raised by `C-0021`; 2–13 % with the coupling fitted, 79 % without |
| [`CH-0025`](challenges/CH-0025-edge-taper-is-an-edge-enhancement.md) — the electrostatic edge taper is an edge *enhancement*, and its width is not `λ_D` | `C-0006`, `C-0009` | raised by `C-0022`; both halves wrong, and they nearly cancel |
| [`CH-0026`](challenges/CH-0026-forces-are-footprint-integrated-one-dimensional-pressures.md) — every electrostatic force is a 1-D pressure times the footprint | `C-0008`, `C-0012` | raised by `C-0022`; understates by 5–19 %, one-signed |
| [`CH-0027`](challenges/CH-0027-hold-down-requirement-is-a-force-only-for-a-one-sided-stack.md) — the hold-down requirement is a force only for a *one-sided* stack | `C-0021` | raised by `C-0023`; no number moves, and the direction is favourable |
| [`CH-0029`](challenges/CH-0029-the-48-pn-allowable-is-a-30-bp-number.md) — the 48 pN per-path allowable is a *30 base-pair* number | `C-0009`, `C-0014`, `C-0020` | raised by `C-0024`; optimistic by up to 2.6×, and not monotone |
| [`CH-0031`](challenges/CH-0031-a-flexible-hinge-cannot-be-anisotropic.md) — a flexible hinge cannot be anisotropic, so it is not a support | `C-0023` | raised by `C-0025`; removes an escape rather than a number |
| [`CH-0033`](challenges/CH-0033-thermal-excitation-is-not-a-load-non-uniformity.md) — thermal excitation is not a load non-uniformity, and a concentration factor does not multiply a share | `C-0015`, `C-0017` | raised by `C-0026`; favourable, no verdict moves |
| [`CH-0034`](challenges/CH-0034-flatness-count-saturates-under-the-solved-load.md) — the flatness count saturates: 225 attachments are no flatter than 45 | `C-0006`, `C-0009`, `C-0015` | raised by `C-0026`; no count moves, the sentence does |
| [`CH-0035`](challenges/CH-0035-the-edge-correction-cannot-reach-the-window-edge.md) — the edge correction cannot reach the window edge, and helps stability rather than hurting it | `CH-0026` | raised by `C-0027`; favourable, twice |
| [`CH-0036`](challenges/CH-0036-a-correction-and-the-part-that-caused-it.md) — the shortfall is a property of a part that left the design the same day | `CH-0024` | raised by `C-0027`; 2–13 % becomes 0.6–0.9 % |
| [`CH-0041`](challenges/CH-0041-the-standoff-supplies-the-draw-in-it-was-charged-for.md) — the standoff supplies the draw-in it was charged for | `C-0025`, `C-0028` | raised by `C-0030`; the bound upheld, its sign consequence overturned in both halves |
| [`CH-0042`](challenges/CH-0042-a-strain-softening-coupling-has-no-free-stability-margin.md) — a strain-softening coupling has no free stability margin | `C-0017` | raised by `C-0030`; **OPEN**, handed to `T-76` |
| [`CH-0037`](challenges/CH-0037-the-buckling-duty-is-the-mandate-not-the-element.md) — the buckling duty is the mandate secant, not the element's own reaction | `C-0025` | raised by `C-0028`; unfavourable, 1.27–1.70×, no verdict moves |
| [`CH-0038`](challenges/CH-0038-a-standoff-grounded-at-infinity.md) — a standoff grounded at infinity | `C-0025` | raised by `C-0028`; the omitted qualifier turned a reserve into the binding constraint |
