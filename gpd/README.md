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
