# C-0157 — **The corpus's fitted crossover hinge constant survives its own measurement: oxDNA brackets `k_θ` at `5.62052112 – 25.9227606 pN·nm/rad` and the fitted `13.5294118` sits inside it, so `D_⊥` and the `25.5607302×` anisotropy that drives every placement result in this programme now rest on a measurement rather than on a `1/100` borrowed from CanDo's *nick* softening.** The same run **reproduces the interhelical sawtooth this repository has cited since `C-0076` and never checked** — `3.60109097 nm` midway against Bai et al.'s cryo-EM 3.60, `1.00030305×`, visible only when each interface is read at the columns of **its own parity** — and **fails to settle the three plate rigidities at all**, at 20.5–24.2 independent samples per mode against the 100 wanted, in the direction that would have flattered the corpus

| | |
|---|---|
| **Task** | [`T-9`](../tasks/T-9-crossover-hinge-constant.md) — the crossover hinge constant for a single-layer sheet, from oxDNA |
| **Leaf** | `A1.2`, with `A8.2` |
| **Verification type** | **in-silico** (oxDNA2, five independent replicas, 450 analysed frames) **+ logical** (the design generator asserts the corpus's own lattice counts before anything is simulated) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** A coarse-grained model in a substituted buffer; no folded object is measured here. |
| **Verdict** | **PASS on the first of `T-9`'s three deliverables and on nothing else.** `F1`–`F5` did not fire. `k_θ` is bracketed and the fitted value is inside; the crossover's **vertical compliance** and **in-plane shear `k_s`** are untouched, so `T-9` stays open on two of three counts. |
| **Provenance** | [`gpd/results/T-9-crossover-hinge-constant.json`](../results/T-9-crossover-hinge-constant.json), emitted by [`tools/T-9-emit-result.py`](../../tools/T-9-emit-result.py) from the run's own derived JSON; run in [`tools/oxdna/`](../../tools/oxdna/) with [`README.md`](../../tools/oxdna/README.md) (build, environment, five traps) and [`RESULTS.md`](../../tools/oxdna/RESULTS.md) (the reading and its validity range); design generator `tools/oxdna/gen1_tile_design.py`, estimator `tools/oxdna/analyse_tile.py`, both with their own test files. |
| **Conditions** | oxDNA2 (`DNA2`), sequence-averaged, `salt_concentration = 0.5 M` **monovalent**, `T = 27 °C`, John thermostat, `dt = 0.005`, 300 000 production steps × 5 replicas, 3 360 nucleotides. `k_BT = 4.141947 pN·nm` at 300 K. Crossover spacing `p = 32 bp` (**per interface**), `d = 2.69 nm`, rise 0.34 nm. |
| **Consumes** | [`C-0086`](C-0086-seamless-scaffold-routing.md) (the 112 bp seamless width), [`C-0063`](C-0063-upward-root-placement.md) (crossover phase 8), [`C-0015`](C-0015-crossover-phase-and-registration.md) (the seven columns), [`C-0006`](C-0006-tile-load-distribution-and-flatness.md) / [`C-0009`](C-0009-discrete-lattice-tile.md) (what `k_θ` carries), `T-10` (the three closed-form rigidities) |
| **Constrains** | Nothing is overturned. `C-0006`, `C-0009`, `C-0010`, `C-0015` and every placement result downstream of `D_⊥` keep their values and **change their ground**: from a fitted model input to a bracketed measurement. No challenge is raised. |

---

## 1. What was measured, and why the coordinate is the whole argument

`k_θ` is a **dihedral** spring about a line running *along* the helices.
The observable that corresponds to it is the **interduplex roll** — the signed angle between two
adjacent duplexes' base-pair vectors about their common axis — and oxDNA does not model that
dihedral at all: its crossover is two bonded backbones, so the roll is **emergent**.
That is what makes the comparison a test of `k_θ` and not of DNA elasticity: both sides carry
CanDo's duplex constants, and they differ in exactly one place.

Measured over 49 crossover sites, 14 interfaces and 450 frames:

| | value |
|---|---|
| roll s.d. **at** a crossover column | **22.902573°** |
| roll s.d. **off** the columns | **25.879324°** |
| mean roll at a crossover | 38.1171349° |

## 2. The bracket, and why its width is an assumption rather than a sample size

Equipartition on the roll variance gives `k = k_BT/Var`, and the two readings differ in one
assumption and nothing else:

| reading | assumption | value, pN·nm/rad |
|---|---|---|
| **upper** | the hinge is the **only** constraint on that dihedral | **25.9227606** |
| **lower** | the hinge is what a crossover **adds** over the mid-span roll | **5.62052112** |

The truth is between them, because the mid-span roll is itself partly held by the crossovers 8 bp
away, so the lower reading understates what the hinge alone supplies.
**The corpus's fitted `13.5294118` sits inside**, and so do both quantities that carry it:

| | corpus | oxDNA bracket |
|---|---|---|
| `D_⊥` | 3.34504758 pN·nm | **1.38963252 – 6.40921196** |
| anisotropy `D_∥/D_⊥` | 25.5607302 | **13.3404636 – 61.5283951** |

**The bracket is `4.6121632×` wide and more compute does not narrow it.**
Its width is set by which of two readings of the same data is *the hinge*, so the only thing that
sharpens it is a different experiment — an umbrella sampling of the roll with the duplex torsion
constrained, which would separate the hinge from what sits in parallel with it.
Pooled over replicas the bracket is `4.61×`; taken as the mean of the five per-replica readings it
is the `5.1×` `RESULTS.md` quotes (27.13 ± 0.77 against 5.30 ± 0.60).
Both are reported: the pooled pair is what this file **derives** and can be rechecked, the
per-replica pair is what the run **recorded**.

## 3. The sawtooth: a citation that had never been checked

This repository has asserted Bai et al.'s deterministic 1.85 → 3.60 nm interhelical modulation
since `C-0076`, and has built a plan model, a weave-node congruence and a station-lattice argument
on its **phase**. It had never been run against a simulation of this programme's own tile.

| | corpus / literature | oxDNA | ratio |
|---|---|---|---|
| midway between columns | 3.60 nm (Bai 2012, cryo-EM) | **3.60109097 nm** | **1.00030305** |
| at a crossover column | 1.85 nm (Bai 2012, cryo-EM) | 2.22893786 nm | 1.2048 |
| mean | 2.69 nm (Fischer 2016, SAXS) | 2.95153925 nm | 1.09722649 |

99.78 % of designed base pairs are intact (99.97 % in the interior), so this is a property of the
designed lattice and not of a structure coming apart.

**And the signal is only there if each interface is read at the columns of its own parity** —
averaging over all seven columns dilutes it to a flat 2.92 / 2.90 nm.
That is `CLAUDE.md`'s *"check a measurement's PHASE against the lattice before pricing its
amplitude"*, met from the other side: the phase is what makes the amplitude visible at all.

## 4. What the run could NOT settle, quoted as a non-result

The quadratic-mode estimator — the one that matches the plate's own definition, validated to 1.7 %
on synthetic ensembles from known rigidities — **does not converge** on this trajectory:

| diagnostic | value |
|---|---|
| independent samples per mode | **20.5 – 24.2** (100 wanted) |
| first-half / second-half ratio | 0.834 / 0.886 / 1.469 |
| per-replica spread | **3.36× – 5.81×** |

The pooled reading is `D_∥ = 52.4616225`, `D_⊥ = 39.4812112`, `D_k = 24.6055492 pN·nm`.
**It is reported and explicitly not used**, because those three modes are the tile's *softest* and
therefore its *slowest*, and an under-sampled soft mode reads as **stiffer** than it is — the
direction that would have flattered a large `D_⊥`.
A spectral maximum-likelihood fit built to escape the sampling cost is **rejected**: it runs
`D_∥ = 33.7 / 24.3 / 11.0` as the basis goes to degree 2/3/4, and this run cannot separate real
short-wavelength compliance from base-pair noise in the centreline.

At the measured correlation times, settling these three numbers is **12–55 h per replica** on this
machine. That is the honest price, and it is why the field runs this class of problem on a GPU.

## 5. The controls

- **The duplex.** The *nicked* duplex the tile is built from fits `EI = 211.810255 pN·nm²` over a
  10 nm window, `0.920914152` of CanDo's continuous-duplex 230. The fit is **not converged in its
  own window** (`L_p` runs 51.1 → 247.2 nm as the window opens 10 → 60 nm), so this is a
  consistency check on the constitutive input and not a measurement of it.
- **The global twist.** All five replicas agree in **sign** and give **+25.5869441°** end-to-end on
  a raster built at caDNAno's 10.67 bp/turn against oxDNA's own ~10.5. `C-0107`'s boundary layer
  predicts 17–25° at a row end. **The magnitudes agree and these are different functionals of the
  same strain** — a register angle *at a row end* against the tile's mid-surface twist *across its
  width* — so it is a consistency check of sign and order, never an identity.
  **The cheap falsifier was not run**: build `C-0133`'s twist-corrected 110 bp raster and the twist
  should collapse.

## 6. What this closes and what it does not

**Closes**: `T-9`'s first deliverable. Every result in this corpus that rests on `k_θ` keeps its
value and changes its ground.

**Does not close**: the other two, and they are the ones two later claims turn on.
`C-0009` models the crossover as **rigid in `z`**, and `C-0100` establishes that the only two
physical states of a constraint are present and absent — so a *vertical compliance* is not a
refinement of that model but a different one.
`C-0020`'s **in-plane shear `k_s`** is untouched, and `C-0028` shows it moves a buckling verdict.
`T-9` therefore stays open, on two of three counts, and its **repricing by `C-0141`** stands:
on a `15 × 4` cross-section `k_θ` now decides the flatness verdict rather than merely carrying it.

## 7. Validity range

- **The buffer is not §3's.** oxDNA2 carries **no divalent ions**, and its Debye-Hückel term is
  parameterised at or above **0.1 M monovalent**; §3's buffer is **2 mM MgCl₂**. The run uses the
  field's standard 0.5 M monovalent proxy. This is a stated substitution, it is a property of the
  **model** rather than of this run, and it is the largest single reason a rigidity here need not
  equal one measured in the device's own buffer.
- **The tile is free** — no PEG layer, no electrode. The three rigidities are properties of the
  sheet and are the right objects to compare; `T-10`'s *thermal* numbers are not, because they sit
  on the Winkler foundation.
- **Crossover phase 8 forces fourteen lone 8 nt corner staples.** They carry no crossover, so their
  fraying costs the lattice nothing, but they do fray and the intact fraction is reported beside
  every result.
- **The raw trajectories (649 MB) were pruned after analysis.** Every field the result file marks
  **DERIVED** is recomputed from the retained JSON by `tools/T-9-emit-result.py` and is
  recheckable; every field marked **RECORDED** is not, and says so.
  `tools/oxdna/README.md` carries the recipe that regenerates the run from scratch.
