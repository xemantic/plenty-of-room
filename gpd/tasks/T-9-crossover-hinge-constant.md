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

---

# T-9, second deliverable — the crossover's **vertical/axial compliance**

Added in iteration 40. `C-0157` closed the first deliverable; this section is the second.

## 4. Formulate

### The question, and why it is not a request for a number

`C-0009` models a crossover's vertical link as a **rigid constraint** —
`OrigamiGrillage.RIGID_LINK_STIFFNESS = 1e4 pN/nm`, a *penalty* whose value the answer is asserted
not to depend on.
`C-0099` measured that assertion on one channel and found it a **step, not a ramp**:
an eighth of the penalty still enforces the constraint,
and its bisection returned the bracket `[0, 0.015625]` of the penalty —
`[0, 156.25] pN/nm` — which it read as locating a **discontinuity** rather than a threshold.
`CLAUDE.md` records the conclusion as *"the only two physical states of a constraint are present and
absent"*, so a covalent tie is a **binary** and asking how stiff it is, is asking the wrong question.

The question this deliverable must answer is therefore **not** *"what is `k_z`"*.
It is: **is that binary reading right — does the crossover's vertical stiffness fall on the flat
part of the response or on the moving part?**

### The coordinate, stated before anything is measured

`OrigamiGrillage.linkExtension` is

&nbsp;&nbsp;&nbsp;&nbsp;`e = (w_b + (d/2)φ_b) − (w_{b+1} − (d/2)φ_{b+1})`

— the relative **out-of-plane** displacement of the two duplex **surfaces** that face each other
across the interface, in nm, at a crossover node.
It is *not* the roll (a dihedral, `C-0157`'s coordinate) and *not* the interhelical distance
(an in-plane separation, the sawtooth `C-0157` reproduced).
`k_z` is the stiffness conjugate to `e`, in **pN/nm**.

Sign conventions unchanged from `C-0009`: `x` along the helices, `y` across them,
`z` normal and positive upward; `d = 2.69 nm`, rise `a = 0.34 nm`,
`p = 32 bp` per interface, `k_BT = 4.141947 pN·nm` at 300 K.

### The acceptance predicate — four verdicts, all fixed here, before the sweep

Let `k_z*` be the swept variable and `D(k_z)` the best 34-root centro-symmetric dishing of the
38.08 nm phase-8 tile under `C-0022`'s solved collar, over `C-0090`'s own enumeration.

| | predicate | what it decides |
|---|---|---|
| **V1** | `D` at the physical `k_z` crosses `T-5b`'s **0.10** of the stroke | the rigid model is **wrong for the flatness verdict** |
| **V2** | `D` moves by more than **3.0 percentage points** of the convention — the whole worth `C-0099` prices the row-end unknown at | it is **material** even if V1 holds |
| **V3** | the peak per-crossover vertical force moves by more than **19 %** — `C-0015`'s own count effect, the smallest movement that claim reports as material | the **registration design rule**'s lever moves |
| **V4** | the response between the rigid limit and the physical value is a **ramp** rather than a **step** — i.e. the physical value sits where `dD/d ln k_z ≠ 0` | the **binary reading is wrong** |

**Operationally, fixed here so that none of the four can be read off after the fact.**
Write `D_rigid = D(1e4)`, `D_absent` the same objective with the link deleted
(`CrossoverSoftening(hinge = 1.0, link = 0.0)` at every crossover), and `D_phys = D(64.7)`.

- **V1** fires iff `D_phys > 0.10`.
- **V2** fires iff `|D_phys − D_rigid| > `~~`0.030`~~ **`0.0030284749`** — three percentage points
  *of the convention*.
  **CORRECTED after the sweep, and the correction is published rather than applied silently.**
  Three percentage points *of* `0.10` is `0.0030`, not `0.030`, and `C-0099`'s own two emitted
  readings differ by exactly `0.0651753854 − 0.0621469105 = 0.0030284749` of the stroke.
  The number registered here before the sweep was a factor of ten out against the quantity the
  same sentence names; it is retained in code as `ROW_END_UNKNOWN_MARGIN_AS_FIRST_WRITTEN`,
  **both** verdicts are emitted, and the registered wording is in this file's own git history
  one commit before the result. `V1`, `V3` and `V4` read the same under both.
- **V3** fires iff `|F_phys − F_rigid| / F_rigid > 0.19`, `F` the peak per-crossover vertical force.
- **V4** fires iff the **ramp fraction**
  `R = (D_phys − D_rigid) / (D_absent − D_rigid)` exceeds **0.05** —
  i.e. the physical value carries more than a twentieth of the whole present-versus-absent movement
  that the binary reading says is the only thing there is.
  `R = 0` is the binary reading exactly; `R = 1` is a link that does not exist.

`PASS` here means *model-consistent and traceable*, never empirically demonstrated:
every number below is a property of `C-0009`'s lattice and of this repository's own derived
constants, and **nothing about a crossover's vertical stiffness has been measured anywhere.**

## 5. Plan

### Cheap bound 0 — can the existing trajectory answer this at all? (runs first, costs nothing)

The row's premise is that the trajectory that measured the roll is on disk.
Five checks, each one command:

1. `build-oxdna/` — the directory `tools/T-9-emit-result.py` reads — is **absent** from this checkout;
2. `C-0157` §7 states the raw trajectories (**649 MB**) were pruned after the analysis;
3. no oxDNA binary exists on this host, and `tools/oxdna/README.md` names an **Apple M1 / macOS**
   host where this box is Linux;
4. the retained result file `gpd/results/T-9-crossover-hinge-constant.json` carries **no vertical
   field of any kind** — `hinge/*` is angular, `sawtooth/*` is a scalar `|Δr|`;
5. and there is **no estimator to run even if the trajectory returned**:
   `interduplex_roll.py` computes signed angles only,
   and `analyse_tile.py` reduces the interhelical vector to its **norm**,
   which cannot separate an out-of-plane offset from an in-plane one.

If those hold, the honest deliverable is a **criterion plus a priced run**, not a measured `k_z` —
and the price must be stated with the sampling argument that supports it,
not asserted.

### Cheap bound 1 — the physical value, from this repository's own construction (no MD)

`Gen1Tile.crossoverInPlaneStiffness` already derives a crossover's **displacement** stiffness:
Chen et al.'s softened-bond construction with the **stretch** modulus substituted for the bending
rigidity,

&nbsp;&nbsp;&nbsp;&nbsp;`k = 2αS/(100a) = 64.7 pN/nm` at `α = 1`,

and `C-0020` reports every result that uses it over a **four-decade** sweep
(`Gen1Tile.CROSSOVER_IN_PLANE_SWEEP`, `0.03125 … 128` → **2.02 – 8282 pN/nm**).
The vertical link is the **same two phosphate bonds resisting a relative displacement of the same
two duplexes**, on the orthogonal axis, so the same construction applies to it unchanged.
That is a construction and not a measurement, and it is swept, exactly as `C-0020` sweeps it.

Three divisions then predict the shape of the answer before any solve:

- `RIGID_LINK_STIFFNESS / k_z = 1e4 / 64.7 = **154.5**` — the penalty is 155× the constant;
- `k_z / (k_θ/d²) = 64.7 / 1.870 = **34.6**` — but only 35× the hinge's own equivalent;
- `k_z` as a fraction of the penalty is **0.006471**, which lies **inside `C-0099`'s unresolved
  bracket `[0, 0.015625]`** — the one interval its bisection did not resolve.

**If the cheap bound is right, the physical value sits in the gap `C-0099` left open,
and V4 is the deliverable.**

### The method, and its cost against the alternative

One `OrigamiGrillage` per rung with `linkStiffness` set globally — the constructor already takes it —
and `C-0099`'s own object: 38.08 nm, phase 8, `C-0022`'s solved collar, `C-0090`'s exhaustive
centro-symmetric 34-root family, `C-0090`'s own tie-break, so the rigid rung must **reproduce
`0.0621469105`** or the object is not the one the corpus is about.
`C-0099` swept the **14 row-end** crossovers and scaled the hinge **and** the link together;
this sweeps the **link alone, hinge intact, at all 49 crossovers**, which is the channel
`T-9`'s vertical deliverable needs and which no study in this repository has run.

Cost: a grillage solve and one influence bank per rung, seconds each; the exhaustive placement
enumeration is `C-0099`'s and is ~1 minute per rung. Against that, an oxDNA re-run is a day.
**The lattice threshold has to exist before a measurement of `k_z` means anything**, and it is
three orders of magnitude cheaper, so it runs first regardless of whether the trajectory returns.

### What would falsify this approach

- **F1** the rigid rung does not reproduce `C-0099`'s `0.0621469105` → the object is wrong;
- **F2** a uniform load on the free lattice dishes materially at any rung → the solver is wrong
  (`CLAUDE.md`'s standing falsifier);
- **F3** the response is **not monotone** in `k_z` → there is no threshold and no bisection is
  meaningful (`C-0070`'s discipline);
- **F4** the response is flat over the whole four decades → the sweep is measuring nothing and the
  binary reading is right for a reason the cheap bound did not anticipate;
- **F5** `k_z` at `α = 1` falls **outside** `C-0099`'s `[0, 0.015625]` bracket → the cheap bound's
  central arithmetic is wrong and the framing goes with it.
