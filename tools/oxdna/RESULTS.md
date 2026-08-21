# oxDNA against the corpus: what was compared and how far it can be read

Companion to [README.md](README.md), which carries the build, the environment
and the traps.
This file is the result and its validity range.
Every number here is produced by the scripts in this directory from the
trajectories in `build-oxdna/`, and every one is quoted with the convergence
diagnostic that says how far it can be read.

## The comparison

`T-10` recovers three orthotropic plate rigidities in closed form, and this run
measures the same three from an oxDNA trajectory of the same tile by
equipartition of the plate's three constant-curvature modes.
The two sides share their constitutive inputs — `Gen1Tile.kt`'s duplex constants
*are* CanDo's — and differ in how the crossover is represented, which is what
makes the comparison a test of `k_theta` rather than of DNA elasticity.

## What the estimator is, and what makes it a measurement

For the Huber orthotropic plate

```
U = 1/2 [ D_x w_xx^2 + 2 D_1 w_xx w_yy + D_y w_yy^2 + 4 D_k w_xy^2 ]
```

the three quadratic Legendre modes `P2(xi)`, `P2(eta)` and `xi*eta` have
constant curvature, so the energy is exactly quadratic in their amplitudes and
`K = kT C^-1` inverts the measured covariance straight into the rigidities.
Legendre polynomials are orthogonal on the rectangle, so higher bending modes do
not leak into the three amplitudes.

`test_analyse_tile.py` establishes that this is an instrument rather than an
assertion, by synthesising ensembles from KNOWN rigidities and recovering them:

| check | result |
|---|---|
| three rigidity sets recovered, including the 25.6:1 anisotropic one | within 1.7 % |
| with large P3 / P4 / P2xP2 contamination added | within 2.9 % |
| known mean twist recovered | to 0.01 deg |
| invariance under random rotation + 30 nm translation per frame | 1e-13 |

The last one is not cosmetic. Production frames are a freely diffusing, freely
rotating tile, so the reference structure has to be built by iterative
alignment; a mean taken over raw frames is a blur of orientations and every
displacement measured against it is meaningless.

`test_gen1_tile_design.py` does the same job for the structure: the crossover
columns, the 4/3 parity split, the 49 crossovers, complete single coverage of
the staple side, no double crossings, and that every remaining 8 nt staple has
one domain and therefore holds no crossover.

## Validity range

- **The buffer is not §3's.** oxDNA2 has no divalent ions. The runs use
  `salt_concentration = 0.5` M **monovalent**, the standard origami proxy,
  against §3's 2 mM MgCl2. This is a stated substitution, not a match, and it is
  the largest single reason a rigidity here need not equal one measured in the
  device's own buffer.
- **The tile is FREE.** There is no PEG layer and no electrode. The three
  rigidities are properties of the sheet and are the right objects to compare;
  `T-10`'s *thermal* numbers are not, because they sit on the Winkler
  foundation. Where a fluctuation is compared it is recomputed on BOTH sides
  from the same three modes.
- **`T = 27 C`**, sequence-averaged parameters, John thermostat. Only static
  equilibrium averages are used, which the absence of hydrodynamics does not
  affect.
- **Crossover phase 8 forces 8 nt corner domains.** Fourteen staples are lone
  8-mers. They carry **no crossover**, so their fraying costs the lattice
  nothing, but they do fray and the intact fraction is reported beside every
  result. Broken pairs are interpolated along the helix from intact neighbours,
  because the midpoint of an opened pair is meaningless and one frayed corner
  otherwise injects a metre-scale spike into the curvature field.
- **A rigidity is quoted with its convergence.** Mode autocorrelation times, a
  first-half/second-half split and the per-replica spread are emitted beside
  every value. A stiff mode that has not decorrelated reads as *stiffer* than it
  is, which is the direction that would flatter the corpus.

## What the run measured

500 frames, 5 independent replicas, 300 000 steps each at `dt = 0.005` after
minimisation, a cold capped-force stage, heating and equilibration.
99.8 % of designed base pairs intact (99.97 % in the interior).

### Resolved

| quantity | corpus / literature | oxDNA | ratio |
|---|---|---|---|
| interhelical, between crossover columns | 3.60 nm (Bai 2012, cryo-EM) | **3.601 nm** | 1.000 |
| interhelical, at a crossover column | 1.85 nm (Bai 2012, cryo-EM) | 2.229 nm | 1.20 |
| interhelical, mean | 2.69 nm (Fischer 2016, SAXS) | 2.952 nm | 1.10 |
| duplex `EI`, nicked as built | 230 pN·nm² (CanDo) | 212 pN·nm² | 0.92 |
| crossover hinge `k_theta` | 13.529 pN·nm/rad (fitted) | **5.3 – 27.1** | brackets it |

The **sawtooth is reproduced**: this repository cites Bai et al.'s
1.85 -> 3.60 nm deterministic modulation and had never checked it against a
simulation of its own tile. The far end matches to three digits. Note the signal
is only visible if each interface is read at the columns of ITS OWN parity —
averaging over all seven columns dilutes it away to a flat 2.92 / 2.90 nm.

### `k_theta`, and why it is a bracket

Two readings of the same interduplex roll data, both stable to better than 10 %
across the five replicas:

| reading | assumption | value |
|---|---|---|
| upper | the hinge is the ONLY constraint at a crossover | 27.13 +/- 0.77 |
| lower | the hinge is what a crossover ADDS over mid-span | 5.30 +/- 0.60 |

The truth is between them, because the mid-span roll is itself partly held by
the crossovers 8 bp away, so the lower reading understates what the hinge alone
supplies. The corpus's fitted `13.529` — whose `1/100` is borrowed from CanDo's
*nick* softening — **sits inside the bracket**, and so do both quantities that
depend on it:

| | corpus | oxDNA bracket |
|---|---|---|
| `D_perpendicular` | 3.345 pN·nm | 1.31 – 6.71 |
| anisotropy `D_par/D_perp` | 25.56 | 12.7 – 65.2 |

So the comparison **upholds** `T-9`'s standing assumption rather than
overturning it, to within a factor of about 2.5 either way. It does not sharpen
it: the bracket is 5.1x wide and its width is set by an assumption, not by the
sampling.

### Not resolved: the three plate rigidities

The quadratic-mode estimator — the one that matches the plate's own definition,
and validated to 1.7 % on synthetic data — does **not** converge on this
trajectory:

| diagnostic | value |
|---|---|
| independent samples per mode | 20 – 24 |
| first-half / second-half ratio | 0.83, 0.89, 1.47 |
| spread across the 5 replicas | 3.0x – 5.8x |

Those three modes are the tile's SOFTEST and therefore its slowest, with
correlation times of 19 – 22 frames against 90 frames per replica. **An
under-sampled soft mode reads as STIFFER than it is**, which is the direction
that would have flattered a large `D_perpendicular`, so the pooled reading of
39.5 pN·nm is not evidence against the corpus's 3.345 and is not quoted as such.

At the measured correlation times, 100 independent samples per mode needs
**12 – 55 hours per replica** on this machine. That is the honest cost of
settling these three numbers by CPU oxDNA, and it is why the field runs this
class of problem on a GPU or in an FEM code.

A spectral maximum-likelihood fit over the whole Legendre bending spectrum was
built to escape that (higher modes decorrelate faster and carry the same
rigidities) and is **rejected**: it is strongly degree-dependent, `D_parallel`
running 33.7 / 24.3 / 11.0 as the basis goes to degree 2 / 3 / 4. Either the
tile has real short-wavelength compliance a Kirchhoff plate cannot express, or
base-pair noise in the centreline dominates the high modes. This run cannot
separate the two, so the estimator is retained as a diagnostic and not used for
a value.

### The global twist

The mean shape is an average rather than a variance, so it converges far faster.
All five replicas agree in SIGN and give **+25.6 deg** of end-to-end twist
(per replica +12.1, +19.2, +27.6, +39.3, +28.3; s.e. 4.6 deg), on a raster built
at caDNAno's 10.67 bp/turn against oxDNA's own ~10.5.

The corpus's boundary-layer analysis predicts that a 112 bp row cannot untwist
its way out — `lambda = 12–24 nm` against a 19 nm half-row, relief 17–43 %,
leaving **17–25 deg** at a row end. The magnitudes agree, but these are
different functionals of the same strain: the corpus's is a register angle AT A
ROW END and this is the tile's mid-surface twist ACROSS ITS WIDTH. Read it as a
consistency check of sign and order, not as an identity.

**The falsifier is cheap and was not run**: build the twist-corrected 110 bp
raster (`C-0133`) and the twist should collapse. Until that control exists, a
global twist of this size is consistent with the register mismatch but is not
attributed to it.
