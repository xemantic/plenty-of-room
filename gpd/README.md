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
| `C-0004` | [Poroelastic drainage does not limit the Gen-1 actuator, and what would make it](claims/C-0004-poroelastic-drainage.md) | [`T-7`](tasks/T-7-poroelastic-drainage.md) | none (§4(d)) | PASS |

### Standing challenges

| Challenge | Against | Status |
|---|---|---|
| [`CH-0001`](challenges/CH-0001-semidilute-premise.md) — the layer is not in the semidilute regime | `C-0001` | UPHELD in part |
| [`CH-0003`](challenges/CH-0003-blob-stack-height.md) — the layer is ~1.5 blobs tall | `C-0001` | see the challenge |
