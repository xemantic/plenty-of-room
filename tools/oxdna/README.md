# oxDNA cross-check of the Gen-1 tile

An independent implementation of this repository's tile-mechanics study
(`T-10` / `T-5b` / `T-8`) in **oxDNA**, so that the corpus's closed-form plate
rigidities can be compared against the field's standard coarse-grained model.

`COMPARISON-p1-p2.md` records that this run declined the literal MD deliverable
for leaf `A1.2`, and `TASKS.md` carries `T-9` — the crossover hinge constant
`k_theta`, *"from oxDNA"* — as the largest open premise under the whole
structural corpus.
This directory is the machinery for answering both against the same structure.

## Why this comparison is sharp

`Gen1Tile.kt`'s duplex constants **are** CanDo's parameter set
(Kim, Kilchherr, Dietz & Bathe, *NAR* **40**:2862, 2012: `EI = 230`,
`GJ = 460 pN nm^2`, `S = 1100 pN`),
so an external model shares the constitutive inputs and differs in exactly one
place: how the crossover is represented.
`T-10` recovers three plate rigidities in closed form —

| quantity | closed form | value | rests on |
|---|---|---|---|
| `D_parallel` | `EI / d` | 85.5019 pN·nm | CanDo `EI`, cited |
| `D_perpendicular` | `k_theta · d / p` | 3.3450 pN·nm | **`k_theta`, fitted, never measured** |
| `D_k` | `GJ / (4d)` | 42.7509 pN·nm | CanDo `GJ`, cited |

— and `k_theta = 2 alpha B / (100 a) = 13.529 pN·nm/rad`,
whose **`1/100` is borrowed from CanDo's *nick* softening** (`TASKS.md`, `T-9`).
The 25.56x anisotropy `D_parallel / D_perpendicular` that drives every placement
result in the corpus has that borrowed factor in its denominator.

## The structure

The design is generated from this repository's own lattice rules, not drawn by
hand, and the generator asserts the corpus's own counts:

- 15 duplexes, **112 bp** per row — the only buildable **seamless** raster width
  near the 40 nm of §3, because a boustrophedon needs an odd number of half
  turns across its row and `112 = 7 x 16 bp` (`C-0086`). 38.08 x 37.66 nm.
- crossover columns at `x = 8 + 16k`, `k = 0..6` — **crossover phase 8**, one of
  the two centro-symmetric phases a seamless 112 bp row admits (`C-0063`).
- interface `b` takes the columns of its own parity, so the seven columns split
  **4/3** between the two crossover parities and the sheet builds **49**
  crossovers — both are `CLAUDE.md`'s numbers for a seven-column sheet.
- each crossover is a **single** strand crossing, and which side of an interface
  donates it alternates with the interface index — see the third trap below.
- geometry: rise 0.34 nm, interhelical **2.69 nm** (Fischer et al. 2016 SAXS),
  and caDNAno's **square-lattice** design twist of 10.67 bp/turn — deliberately
  not B-DNA's 10.5, so the starting structure carries the register mismatch an
  untwist-corrected raster accumulates and oxDNA relaxes against it.

The generator emits a nucleotide-index -> `(helix, offset, forward)` map and
asserts every lattice site is occupied exactly once; the analysis addresses the
lattice through that map rather than inferring it from geometry.
On the ideal configuration every designed base pair comes out at exactly
1.018 nm, which is what proves the addressing.

## Environment

- **Host**: Apple M1 (`arm64`), 8 cores, 16 GB, macOS 26.5.1. **No CUDA**, so
  `mrdna` (needs ARBD) is unavailable and oxDNA runs on CPU.
- **oxDNA** `v3.7`, commit `8028cf3`, built from source:

  ```shell
  brew install cmake
  git clone --depth 1 https://github.com/lorenzo-rovigatti/oxDNA.git
  cd oxDNA && mkdir build && cd build
  cmake .. -DCMAKE_BUILD_TYPE=Release -DPython=ON -DCMAKE_POLICY_VERSION_MINIMUM=3.5
  make -j8
  ```

  `-DCMAKE_POLICY_VERSION_MINIMUM=3.5` is needed under CMake 4.x.
  Built flags: `-O3 -DNDEBUG -march=native`, native arm64.
- **Python**: a venv with `scadnano`, `numpy`, `oxDNA-analysis-tools`.
- **Throughput**: 3360 nucleotides at **~10.9 ms/step** single-threaded
  (`DNA2`), i.e. ~10^6 particle-steps/s, which is CPU oxDNA's published figure.
  `DNA` (oxDNA1) is 3.2x faster — the Debye-Hückel term is most of the cost.
  Replicas are run in parallel because the CPU backend is serial.

## Reproducing

```shell
source .venv-oxdna/bin/activate
python tools/oxdna/gen1_tile_design.py --out build-oxdna/gen1_tile
cd build-oxdna/run
python ../../tools/oxdna/enlarge_box.py ../gen1_tile.dat start.dat 140
REPLICAS=5 bash ../../tools/oxdna/run_pipeline.sh
python ../../tools/oxdna/analyse_tile.py --traj prod1/traj.dat \
    --nucleotides ../gen1_tile-nucleotides.json --out tile.json
python ../../tools/oxdna/compare_with_corpus.py --tile tile.json
```

`tools/oxdna/test_gen1_tile_design.py` asserts all of it — the column list, the
4/3 split, the 49 crossovers, complete single coverage of the staple side, that
no site carries a double crossing, and that every remaining 8 nt staple has one
domain and therefore holds no crossover.
`tools/oxdna/test_analyse_tile.py` validates the estimator itself by
synthesising ensembles from known rigidities and recovering them (within 1.7 %,
and within 2.9 % with large P3/P4 mode contamination added, which is what the
Legendre orthogonality is there to guarantee), and recovers a known mean twist
to 0.01 deg.

## Five traps this run hit, recorded so the next one does not

**An idealised lattice cannot be relaxed against the real potential.**
Every crossover backbone bond starts at ~2.4 oxDNA units where the FENE reaches
~0.8, because the two helices' backbones do not face each other.
Relaxing that at 300 K melts the short staple domains *before* the geometry
settles: base-pair integrity fell to 55 % and the interhelical distance
*expanded* to 4.0 nm — the sheet came apart rather than relaxing.
The cure is a **temperature-free steepest descent with a harmonic backbone**
(`sim_type = min`, `interaction_type = DNA_relax`) as the first stage: nothing
can dissociate while the crossovers are pulled in.
Measured on the backbone sites, that one stage takes the structure from **63**
FENE violations to **zero**, with 100 % of base pairs intact and the
interhelical distance already showing the crossover sawtooth (mean 2.65 nm,
minimum 1.73 nm).
Note that `sim_type = min` will not even load the configuration under `DNA2` —
the over-stretched-bond check rejects it — so the two options are needed together.
A thermostatted `DNA_relax` MD stage after the minimisation is **not** wanted: it
put 47 violations back into a structure that had none.

**`max_backbone_force_far` is the FAR-FIELD force and must be far SMALLER than
`max_backbone_force`, not larger.**
oxDNA's capped backbone potential carries a factor `(fmax - finf)`, so setting
`finf > fmax` flips its sign: at `|r - r0| = 0.85` a nominal 5-unit cap with
`finf = 2 x fmax` delivers **451 pN** per bond where the documented default
`finf = 0.04` (~2 pN) delivers **36 pN**. Every crossover bond of a freshly
converted origami sits in exactly that regime, so the run explodes to `1e25`
within a few thousand steps and it reads exactly like an unrelaxable structure.
The default here is now 0.04; the option exists only to be set deliberately.

**The FENE acts between BACKBONE SITES, not between centres of mass, and the
`.dat` file stores centres of mass.**
The two differ by the backbone offset (`POS_MM_BACK1/2`) and by the helical
twist between consecutive bases, which is easily a factor of two on a bond
0.7 units long. A range check written on the stored coordinate reported ~1000
"compressed" bonds on a structure that has **none**, and sent this run down a
long detour adding relaxation stages to cure a defect that did not exist.
Reconstruct the site as `r + a1 * POS_MM_BACK1 + a2 * POS_MM_BACK2` with
`a2 = a3 x a1`, and compare against `FENE_R0_OXDNA2 = 0.7564 +/- 0.25`.
oxDNA's own error message quotes `|r - r0|`, not the bond length, so
*"exceeds acceptable values (d = 0.431)"* is a bond at 0.32 or 1.18, not at 0.43.

**A crossover must be a SINGLE strand crossing.**
Registering each crossover site from both sides — which is the natural thing to
write, and which the corpus's own `k_theta` provenance invites by speaking of
*"2 bonds per crossover"* — puts two reciprocal crossings at the same base
offset. That is geometrically over-constrained: both backbones would have to
face each other simultaneously in opposite senses. It does not relax. The
signature is a **count**: 112 over-stretched bonds against 63 designed
crossovers, all at `dh = 1, do = 0`, exactly 2x the 49 staple sites; minimisation
then stalls at 1.56 units and the real potential evaluates at `U/nt = 3042`.
Two further things follow from making the crossing one-way: the predecessor of a
segment can no longer be found by running the traversal backwards (invert the
successor map instead), and a crossover must **interrupt** the receiving helix's
own strand, because the arriving strand takes the bases downstream of the
crossing point. Which side donates then controls staple quality, and it is worth
a factor of 2.5 in how much of the staple layer sits in domains shorter than
16 nt: 70 of 134 for a uniform `b -> b+1` rule, 53 of 128 alternating by column,
**28 of 113** alternating by interface, which is the default.

**oxDNA writes ABSOLUTE positions; do not periodically unwrap them.**
`back_in_box` is off by default. Unwrapping relative to a reference nucleotide
folds any body longer than half the box onto itself, and it does so *plausibly*:
it reported a 0.93 nm rise per base pair, and would have reported a rigidity.
The analysis now asserts contiguity instead, so a genuinely wrapped trajectory
fails loudly. The same check first fired on something real and different: a
base pair that has OPENED makes its own midpoint meaningless, so those sites are
interpolated along the helix from their intact neighbours and the substituted
fraction is reported. Without that, one frayed corner staple injects a 6 nm
spike into the curvature field and swamps every mode amplitude.

**Helix rolls must be relaxed.**
Every helix starts at roll 0, which leaves the two backbones at a crossover
pointing away from each other. `Design.relax_helix_rolls()` turns each helix so
its backbone faces the neighbour it crosses to — which is what caDNAno's
8 bp / 270 deg rule expresses for the square lattice.

## What the numbers are quoted with

- **oxDNA2** (`DNA2`), `salt_concentration = 0.5` M **monovalent**, `T = 27 C`.
  §3's buffer is 2 mM MgCl2; oxDNA2 has no divalent ions, and 0.5 M
  is the standard origami proxy. This is a stated substitution, not a match.
- Sequence-averaged parameters, so the M13 scaffold slice carries no sequence
  dependence.
- The John thermostat gives Langevin dynamics with no hydrodynamics; every
  quantity used here is a **static equilibrium average**, which that does not affect.
- A fit or a variance is quoted with its **convergence diagnostic**: the
  persistence length with its fit-window sweep, the plate rigidities with mode
  autocorrelation times and a first-half/second-half split.
  A rod started straight equilibrates its short-wavelength modes long before its
  long-wavelength ones, and a single number hides that where a sweep cannot.
