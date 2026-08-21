# T-9 — the crossover hinge constant `k_θ` for a single-layer sheet, from oxDNA

| | |
|---|---|
| **Leaf** | `A1.2`, with `A8.2` |
| **Raised by** | the opening structural iteration, and carried as the largest open premise under `C-0006` ever since |
| **Claim** | `C-0157` (the hinge constant; the task's other two deliverables stay open) |
| **Result** | `gpd/results/T-9-crossover-hinge-constant.json`, emitted by [`tools/T-9-emit-result.py`](../../tools/T-9-emit-result.py) |
| **Run** | [`tools/oxdna/`](../../tools/oxdna/) — [`README.md`](../../tools/oxdna/README.md) carries the build, the environment and five traps; [`RESULTS.md`](../../tools/oxdna/RESULTS.md) carries the reading and its validity range |
| **Verification type** | **in-silico** (oxDNA2, five replicas) **+ logical** (the design generator asserts the corpus's own lattice counts) |
| **Maturity** | TRL 1–3. Model-consistent and traceable. **NOT empirically demonstrated.** |

---

## 1. Formulate

### The question

`T-10` recovers three orthotropic plate rigidities in closed form.
Two of them rest on CanDo's cited duplex constants.
The third does not:

```
D_⊥ = k_θ · d / p,   k_θ = 2 α B / (100 a) = 13.529 pN·nm/rad
```

and that `1/100` is **borrowed from CanDo's *nick* softening**.
A crossover is not a nick.
The `25.56×` anisotropy `D_∥/D_⊥` that drives every placement result in this corpus has that
borrowed factor in its denominator.

### Why the comparison is sharp

`Gen1Tile.kt`'s duplex constants **are** CanDo's parameter set,
so an external model shares the constitutive inputs and differs in exactly one place:
how the crossover is represented.
CanDo treats it as a **rigid constraint** and says so;
oxDNA does not represent it at all as a distinct object — the two backbones are simply bonded —
so its interduplex roll is an *emergent* quantity rather than a modelled one.
That is what makes the run a measurement of `k_θ` rather than of DNA elasticity.

### The three deliverables, and which one this closes

1. **the hinge constant `k_θ`** — **CLOSED** by `C-0157`, as a bracket that contains the fitted value;
2. the crossover's **vertical/axial compliance** — `C-0009` models it as a rigid constraint,
   and `C-0015` makes that assumption decide whether the registration design rule exists at all — **OPEN**;
3. the crossover's **in-plane shear `k_s`** — `C-0020`'s single undetermined input,
   which `C-0028` shows moves a buckling verdict — **OPEN**.

## 2. Method

A 15-duplex, 112 bp seamless raster at crossover phase 8 is **generated from this repository's own
lattice rules** — not drawn by hand — and the generator asserts the corpus's counts before anything
is simulated: seven columns at `x = 8 + 16k`, the 4/3 parity split, 49 crossovers, complete single
coverage of the staple side, and no double crossings.
oxDNA2 relaxes it (steepest descent with a harmonic backbone first — at 300 K the short staple
domains melt before the geometry settles), then five independent replicas run 300 000 steps each.

`k_θ` is read by **equipartition on the interduplex roll**, the coordinate the spring is written on.
Two readings of the same variances differ in one assumption and bracket the answer.

## 3. What it cost

A day on an 8-core M1 with no GPU, of which the simulation is a few hours and the rest is the five
traps in `README.md`.
Settling the three plate rigidities on the same machine would need **12–55 h per replica**, which is
why they are reported as not converged rather than as a result.
