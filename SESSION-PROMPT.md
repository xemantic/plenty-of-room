# SESSION-PROMPT

The standing instruction for every agentic-loop iteration on this project.
Start a loop with:

```
/loop read @SESSION-PROMPT.md and follow the instructions in it
```

## What this project is

This repository fulfils [third-party/2026-08-ndi-gen1-problem-definition.md](third-party/2026-08-ndi-gen1-problem-definition.md) —
the NDI Gen-1 DNA-origami actuator simulation problem, posed by Jeremy Barton, Nano Dynamics Institute, August 2026.

Eight tasks, acceptance predicates only, method left to us.
NDI is explicit that **process is what is being evaluated**, not just answers —
see §5 (the GPD loop) and §7 (what counts as the loop working) of the problem definition.

The companion public repository `github.com/NanoDynamicsInst/simulation-task-map` is cloned at `../simulation-task-map`.
It carries the authoritative leaf IDs (`A2.1`, `A1.2`, `A7.4`, `A8.2`, …), the V&V matrix, and NDI's own GPD seed artifacts.
Cite leaf IDs, not prose paraphrases.

## Non-negotiable invariants

Carried over from `../simulation-task-map/AGENTS.md` and the problem definition:

- **TRL 1–3.** Nothing here is measured. `PASS` means model-consistent and traceable, never empirically demonstrated. Every claim says which it is.
- **Units are locked.** SI throughout. Lengths in nm, forces in pN, energies in k_BT **and** eV, stiffness in pN/nm (with N/m alongside where useful), pressure in pN/nm², which is **exactly 1 MPa**.
- **k_BT = 4.142 pN·nm at 300 K.** Temperature and medium (aqueous buffer, stated Mg²⁺) are named in every result.
- **Geometry and sign conventions are fixed before deriving,** and restated in the task file.
- **Inherited numbers get re-derived.** A number that is cited and not derived is flagged as such in the claim.
- **Premises of invoked scaling laws are checked against the actual material** (PEG in water with Mg²⁺ at the working volume fraction), not assumed from the textbook.
- **The cheap bound runs before the expensive calculation,** and the method choice is justified against cost in the task's Plan section.
- **Validity ranges travel with results** and are respected downstream. A downstream task that leaves an upstream validity range must say so.
- **Contradiction raises a challenge, not an overwrite.** See `gpd/challenges/`.
- **When a question cannot be answered with the available methods, say so plainly** instead of answering anyway.

## The loop — one iteration

Run **Formulate → Plan → Execute → Verify → File** (GPD, per §5 of the problem definition).

1. **Read** [TASKS.md](TASKS.md). Take the highest-priority unblocked task.
   **Process blockers outrank cheap wins** — if something blocks the loop itself, fix that first, regardless of ROI.
2. **Formulate.** Write or update `gpd/tasks/<ID>.md`: numeric target, falsifiable acceptance predicate, verification type (in-silico / instrumental / logical), locked units, geometry and sign conventions.
3. **Plan.** In the same file: method, justification against cost, and *what result would falsify this approach*. Cheap bound first.
4. **Execute.** Write the code. **TDD — the test comes first, always.** Log every parameter. Emit machine-readable results into `gpd/results/` as JSON.
5. **Verify.** All five gates, as executable tests where possible:
   1. dimensional consistency,
   2. limiting cases,
   3. symmetry and conservation (equipartition σ² = k_BT/k, fluctuation–dissipation, charge/energy conservation),
   4. numerical convergence (mesh, timestep, sampling, statistical power),
   5. literature cross-check, with the premises of any invoked scaling law checked against the actual material.
6. **File.** Record the verified result as a claim in `gpd/claims/` with provenance and an acceptance verdict.
   Feed it back into Formulate as a tightened predicate, or kill the branch and say why.
7. **Journal.** Append to [JOURNAL.md](JOURNAL.md): what was done, what was decided and why, what surprised us.
8. **Queue.** Update [TASKS.md](TASKS.md) — tick off, add what the iteration revealed, re-prioritise.
9. **Commit and push immediately.** One iteration, one (or few) commits, pushed. Never leave an iteration uncommitted.

## Working rules

- **TDD, without exception.** Test first, watch it fail, then implement. This is a numerics project; an untested number is not a result.
- **Kotlin/JVM is the numeric substrate** — viktor `F64Array` for vector arithmetic, openrndr-math for small fixed-size geometry. Add a new `main` entry point per executable study; register it in [TASKS.md](TASKS.md) and the README.
- **You are not bound to Kotlin.** Use the best tool for the problem (oxDNA, LAMMPS, GROMACS, FEniCS, Python/SciPy, …). If you bring in a tool, retain the driver scripts, inputs, and environment notes **inside this repository** so the run can be reproduced and inspected.
- **Everything built on behalf of this project stays in this project.** No throwaway scratch analysis that vanishes with the session.
- **Root on this VPS.** Install whatever is needed without asking. **But** if an experiment needs more CPU/GPU/RAM than this box has, stop the loop and ask Kazik for assistance — do not silently downscale the science to fit the machine.
- **Record every interaction with Kazik in [JOURNAL.md](JOURNAL.md)** — the question asked, the answer given, and what changed as a result.
- Markdown uses [semantic line breaks](https://sembr.org/), per [CLAUDE.md](CLAUDE.md). Never reflow a paragraph.
- Add a [CLAUDE.md](CLAUDE.md) entry whenever something surprises you, per its own rules for editing.

## Layout

```
gpd/tasks/        Formulate + Plan, one file per task, named by NDI leaf ID where one exists
gpd/results/      machine-readable run outputs (JSON), one per execution, parameters included
gpd/claims/       verified claims: statement, provenance, validity range, acceptance verdict
gpd/challenges/   formal challenges against standing claims, with methodological grounds
src/main/kotlin/  numeric models and their main entry points
src/test/kotlin/  the tests, written first
third-party/      the problem definition as received, unmodified
```

## Where to start

The problem definition names the order:
**Task 1** (stiffness of the polymer layer, `A2.1`) is cheap and everything downstream depends on it.
**Task 2** (the feasible design window, `A2.1`) is where the programme turns —
if the window is empty, NDI wants that now rather than after a year at the bench.

[TASKS.md](TASKS.md) is the live state. It wins over this section.
