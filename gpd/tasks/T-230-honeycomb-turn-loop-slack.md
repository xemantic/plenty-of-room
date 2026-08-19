# `T-230` — what is the MINIMUM unpaired slack a honeycomb raster turn needs, and what does a short loop cost?

| | |
|---|---|
| **Leaf** | `A8.2` |
| **Raised by** | [`C-0140`](../claims/C-0140-honeycomb-raster-turn-sense.md) *Still open* item 1, and [`CH-0173`](../challenges/CH-0173-the-built-block-turns-on-loops-not-crossovers.md) item 1 |
| **Verification type** | **logical** (a covalent **reach** bound on the measured backbone, and an exact freely-jointed-chain force/energy law — both closed forms, no solve) **+ literature** (the caDNAno per-helix allotment, read directly out of `gpd/data/T-151-sources/`, already in the repository — **zero fetches**) |
| **Units** | lengths **nm**, forces **pN**, energies **pN·nm** and `k_BT`; rise **0.34 nm/bp**; `k_BT = 4.141947 pN·nm` at 300 K |

---

## Formulate

`C-0140` establishes two complementary routes for a honeycomb x-raster whose turn sense alternates:

- **route A** — an antiparallel **scaffold crossover** at every turn, which needs **zero** unpaired
  slack and pays a **two-length** raster (112 / 108 bp, 4 bp stagger, 653 nt spare on M13);
- **route B** — an unpaired **loop** at every turn, which frees the row length from the 21 bp
  residue condition entirely and pays **scaffold**. The only folded instance of the `15 × 4`
  cross-section spends **28 nt per helix** (`126 = 98 + 28`, `60 × 126 = 7 560` exactly), and at
  that allowance the widest four-layer tile M13 affords is **92 bp = 31.28 nm**, `−21.80 %`.

So the whole width question is **how much of the 28 nt is a requirement and how much is a choice**.

**Numeric target.** A number of nucleotides per turn, `L_min`, **with its ground**, and the
maximum uniform paired row length each scaffold then affords.

**Acceptance predicates.**

- `P1` — a **reach** bound is derived from the measured backbone (`T-71`), not from a polymer
  model, and it is an *impossibility* statement: below it the turn closes at no conformation.
- `P2` — the reach bound is quoted at the **worst** relative azimuth of the two backbones, not at a
  favourable one, and the favourable end is quoted beside it.
- `P3` — the model reproduces the **zero-slack** case: a scaffold crossover at the honeycomb's own
  interhelical distance must fall inside the measured phosphodiester step, or the geometry is being
  read wrongly.
- `P4` — a cost is quoted **with its criterion**: the turn's tension and its free energy as
  functions of the loop length, over the ssDNA Kuhn-length bracket `CLAUDE.md` records as a 2×
  method systematic, with the contour per nucleotide travelling with the elastic model.
- `P5` — the maximum uniform row length is emitted for M13mp18, p7560 and p8064 at every criterion,
  and `C-0140`'s **92 / 98 / 106 bp** at the built 28 nt reproduced at departure `0.0`.
- `P6` — the yield half is priced **against a published measurement or declared unpriceable**, and
  a *threshold* is quoted in its place if it is unpriceable.

**Geometry and sign conventions, fixed before deriving.**

- Two neighbouring helices of the honeycomb, axes parallel to `z`, interaxial distance
  `d = 2.536 nm` (`Gen1Tile.INTERHELICAL_HONEYCOMB`, SAXS). The scaffold leaves the last paired
  base of helix `i` at a **phosphate** on the backbone locus, radius `r_P = 0.9086 nm`
  (`T-71`, MEASURED, 13 084 crystallographic linkages), and enters the first paired base of helix
  `i+1` at another.
- The **span** the loop must bridge is the distance between those two phosphates. Both azimuths are
  free, so the span runs over `[d − 2r_P, d + 2r_P]` = `[0.7187, 4.3533] nm`; the **worst** case is
  both backbones pointing away from the other helix.
- `n` unpaired nucleotides between two anchors make **`n + 1`** phosphodiester steps, so the
  greatest span the chain can reach is `(n + 1) × step`, with `step` the **measured** intrastrand
  P···P step: 0.6645 nm (C2′-endo) / 0.6072 (C3′-endo), P99 0.7567 / 0.7188.
- The loop is charged **per turn**. The built design's 28 nt per helix is `14 + 14`, front and rear,
  so a turn passes through `14 + 14 = 28` nt and a 60-helix tile carries `60 × L` nucleotides of
  loop (59 turns plus two half-loops at the path ends, which is `60 × L / 2 × 2`).
- Polymer model: freely jointed chain, Kuhn length `b = 2.10–2.84 nm` (**zero-force** scattering,
  which is the end a ~1 pN element needs) with contour `c = 0.65–0.70 nm/nt` (**inextensible**,
  the contour that travels with that Kuhn length). The 1.34–1.41 nm force-spectroscopy Kuhn and the
  0.57 nm/nt extensible contour are **not** mixed with them.

---

## Plan

**The cheap bound runs first, it is one division, and it may close the task.**
`(n + 1) × step ≥ span` inverts to `n ≥ span/step − 1` — a *reach* bound in exactly the sense
`CLAUDE.md` records for `O3′–P–O5′–C5′`, one scale up: below it the turn closes at **no**
conformation, and the bound needs no polymer model, no force field and no solve. If it lands far
below 28 the built allowance is a **choice**, and the expensive question becomes what a short loop
*costs* rather than whether it is possible.

**Then, and only then, the cost.** A turn loop pulled to a fraction of its contour carries a
tension and stores a free energy, and both are closed forms for a freely jointed chain:
`f = (k_BT/b) L⁻¹(x)` and `G = (k_BT L_c/b)[x u − ln(sinh u / u)]` with `x = R/L_c`, `u = L⁻¹(x)`.
Nothing here needs a simulation. The inverse Langevin is a bisection on a monotone function, and
the `coth` **must** be guarded — `CLAUDE.md` records `cosh/sinh` returning `NaN` above `u ≈ 20`
three times already.

**Why not oxDNA.** A coarse-grained simulation of a scaffold turn would cost days, would need a
sequence this design does not have, and would answer a question about *one* loop length; the FJC
law answers it over the whole range and carries its own bracket. Where the answer is a **budget**
— how many nucleotides the scaffold can spare — a closed form with a declared bracket is worth
more than one simulated instance.

**What would falsify this approach.**

- `F1` — the zero-slack crossover span `d − 2r_P` falls **outside** the measured phosphodiester
  step at 3σ. Then a scaffold crossover on the honeycomb lattice is geometrically impossible, the
  whole of route A is void, and the geometry here is being read wrongly.
- `F2` — the reach bound comes out **at or above** the built 28 nt. Then 28 is a requirement, route
  B is fixed at `92 bp` on M13, and the answer is `C-0140`'s recommendation by default.
- `F3` — the reach bound and the thermal-cost bound **disagree by more than a decade**, so that
  *"the minimum"* is not a number but a criterion; declared **open**, because the two are different
  questions and may legitimately differ.
- `F4` — the model fails to reproduce `C-0140`'s 92 / 98 / 106 bp at the built allowance.
- `F5` — the loop route at the derived minimum still fails to fit M13 at 112 bp. Then the two
  routes do not compete at all and route A wins without a comparison.
- `F6` — the FJC force law fails its own limits: `f → 0` as `x → 0`, `f → ∞` as `x → 1`,
  `f ≈ 3k_BT x/b` in the Gaussian limit.
