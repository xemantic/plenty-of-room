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
| `C-0010` | [Tile positional variance at 300 K, by mode and in band](claims/C-0010-tile-positional-variance.md) | [`T-8`](tasks/T-8-tile-positional-variance.md) | `A1.2` | PASS (partial against the leaf: no ensemble, no CI) |
| `C-0008` | [The electrostatic force on the tile, and the decay length it actually has](claims/C-0008-electrostatic-force-and-decay-length.md) | [`T-3a`](tasks/T-3a-nonlinear-pb-profile.md) | `A7.4` | PASS |
| `C-0006` | [Load distribution across the tile, and the rejection of the rigid-plate assumption](claims/C-0006-tile-load-distribution-and-flatness.md) | [`T-5`](tasks/T-5-load-distribution.md), [`T-5b`](tasks/T-5b-tile-flatness.md) | `A1.2`, `A8.2` | PASS (verdict: rigid plate **rejected**) |
| `C-0007` | [Solvent quality versus salt: the buffer does not reach the layer's mechanics](claims/C-0007-solvent-quality-vs-salt.md) | [`P-6`](tasks/P-6-solvent-quality-vs-salt.md) | premise under `A2.1` | PASS (on both branches of its predicate) |

### Standing challenges

| Challenge | Against | Status |
|---|---|---|
| [`CH-0001`](challenges/CH-0001-semidilute-premise.md) — the layer is not in the semidilute regime | `C-0001` | UPHELD in part |
| [`CH-0002`](challenges/CH-0002-corrections-do-not-all-soften.md) — the corrections do not all soften the layer | `CH-0001`'s direction | **UPHELD** — the direction is withdrawn |
| [`CH-0003`](challenges/CH-0003-blob-stack-height.md) — the layer is ~1.5 blobs tall | `C-0001` | **RESOLVED** by `C-0003`: the height relation is replaced, and the layer is not a blob stack at all |
| [`CH-0004`](challenges/CH-0004-screening-decay-length.md) — "the Debye length" is three different numbers | §1/§3 as read downstream | **RESOLVED** by `C-0008`: the force's own decay length is a fourth number, and the only bias-dependent one |
| [`CH-0009`](challenges/CH-0009-worst-point-is-not-the-centre.md) — the tile's worst point is not its centre | `C-0006`'s thermal table | raised by `C-0010` |
| [`CH-0007`](challenges/CH-0007-point-ion-boundary-in-applied-bias.md) — the point-ion boundary was compared against the wrong quantity | this queue's reading of `C-0005` | **UPHELD** — ≈ 1.0 V applied, not 0.197 V |
| [`CH-0005`](challenges/CH-0005-rigid-tile-assumption.md) — the tile is not a rigid plate | `C-0001` | UPHELD |
| [`CH-0006`](challenges/CH-0006-solvent-quality-bound.md) — the ≤ 0.7 % solvent-quality bound is right by accident | `C-0002` | upheld in conclusion, overturned in construction |
