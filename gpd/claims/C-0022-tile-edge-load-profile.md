# C-0022 — The lateral electrostatic load profile at the tile edge: an edge *enhancement*, not a taper, and §4(g) closes

| | |
|---|---|
| **Task** | [`T-3b`](../tasks/T-3b-tile-edge-load-profile.md) |
| **Leaf** | `A7.4` |
| **Verification type** | in-silico (graded finite-volume Newton solve of the **2-D** asymmetric nonlinear Poisson-Boltzmann problem around a charged obstacle, preconditioned conjugate gradients, plus a closed-form transverse-eigenvalue cheap bound) + logical |
| **Verdict** | **PASS** — all six clauses of the `T-3b` predicate discharged, and **two of the six declared falsifiers fired**, which is recorded rather than repaired |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** And *within* mean field: `C-0005` puts the one-loop correction at 123–214 % of the leading term across this gap range. **Adding a dimension does not reduce that**, and nothing here claims it does. |
| **Provenance** | `gpd/results/T-3b-tile-edge-load-profile.json`, produced by `electrostatics.TileEdgeLoadProfileStudyKt`; 21 solved 2-D state points, 9 cheap-bound points, 11 convergence points, 8 dishing cases, 4 tile sizes; **38** new `electrostatics` tests (22 on the 2-D solve, 16 on the cheap bound and the fit); the result file re-run and diffed |
| **Conditions** | T = 300 K, `k_BT = 4.142 pN·nm`, `l_B = 0.7141 nm`, `ε_r = 78`; aqueous `MgCl₂` at 0.5 / 2 / 10 mM; 40 × 40 × 10 nm Manning-renormalised tile at `σ_t = −0.3987 e/nm²`; biases from `C-0012`'s located operating bracket |
| **Consumes** | [`C-0008`](C-0008-electrostatic-force-and-decay-length.md) (the ion model, the tile charge, the Stern series), [`C-0012`](C-0012-coupled-stroke-and-blocking-force.md)/[`C-0017`](C-0017-output-coupling-stiffness.md) (the operating bias), [`C-0006`](C-0006-tile-load-distribution-and-flatness.md) (the plate, consumed read-only), [`C-0009`](C-0009-discrete-lattice-tile.md) (the lattice correction), [`C-0005`](C-0005-mean-field-screening-validity.md) (the validity range) |
| **Raises** | [`CH-0025`](../challenges/CH-0025-edge-taper-is-an-edge-enhancement.md) against `C-0006`/`C-0009`; [`CH-0026`](../challenges/CH-0026-forces-are-footprint-integrated-one-dimensional-pressures.md) against `C-0008`/`C-0012` |

---

## The claim

**The electrostatic edge effect on the Gen-1 tile has the opposite sign to the one every downstream claim assumed. The rim does not lose load, it gains it: over the whole §3 operating box at 0.5 and 2 mM the finite tile carries 5 % to 19 % *more* total electrostatic force than a 1-D pressure times its footprint, and it behaves electrostatically as a tile 0.4–2.2 nm larger on every side. The dishing that follows is 21 % to 44 % of the stroke — which confirms §4(g)'s rejection of the rigid plate and, for the first time, gives the irreducible lever-versus-sensor split a single number: 32 % of the stroke at the design point, replacing the 11 %–369 % band `C-0012` had to quote.**

Three subsidiary statements, each of which replaces an inherited one:

1. **`C-0006`'s edge taper had the wrong sign and the wrong width.** Its assumed 50 % taper over one Debye length (4 nm) becomes a **−0.30** taper — an enhancement — over an equivalent width of **8.9 nm**, at the design point. The two happen to give similar *dishing magnitudes* (26.8 % against 32.1 %), which is why no verdict moves and a challenge rather than an overwrite is the right instrument.
2. **The cheap bound was right about the width and wrong about the sign**, which is a more interesting failure than the factor of two the Plan predicted.
3. **The rim charge, which no source in this project supplies, is load-bearing after all** — the declared falsifier fired.

---

## The numbers

### The cheap bound, run before any 2-D solve

The transverse eigenproblem `−φ'' + κ_loc²(z)φ = q²φ` with `φ(0) = 0` (potentiostatic electrode) and `φ'(h) = 0` (fixed-charge tile), linearised about `T-3a`'s own 1-D profile, plus its closed form `q₀² ≥ κ² + (π/2h)²`.

| buffer | gap | `λ_D` | **`1/q₀` ceiling** | `1/q` linearised | ceiling / 4 nm | cheap depth |
|---|---|---|---|---|---|---|
| 0.5 mM | 5 nm | 7.854 | 2.950 | 1.457 | 0.738 | +0.719 |
| | 10 nm | 7.854 | 4.946 | 2.616 | 1.236 | +0.679 |
| **2 mM** | 5 nm | 3.927 | 2.473 | 1.328 | 0.618 | +0.653 |
| | 7 nm | 3.927 | 2.946 | 1.755 | 0.737 | +0.630 |
| | **10 nm** | 3.927 | **3.342** | **2.241** | 0.836 | **+0.570** |
| 10 mM | 10 nm | 1.756 | 1.693 | 1.529 | 0.423 | +0.503 |

(lengths nm, depth dimensionless.)

**The width half held.** The solved deficit centroid is 0.71–2.73 nm against ceilings of 1.69–4.95 nm — inside the bound at every one of the 21 state points, as it must be, since `κ_loc² ≥ κ²` pointwise. And the bound already contradicted `C-0006`: at 2 mM it is **0.62–0.84** of the 4 nm rim width that claim assumed, and it **narrows as the gap closes**, which is the opposite of the intuition that a wider gap leaks less.

**The depth half failed, and not in the way the Plan predicted.** The Plan wrote *"expected error: about a factor of two, one-sided in neither direction."* The half-plane superposition anchor gives **+0.50 to +0.72** — a taper — and the nonlinear solve gives **−0.06 to −0.52** at 0.5 and 2 mM. **The error is the sign.** Superposition sees a rim losing field lines; the nonlinear solve sees the fringing field of a finite capacitor, and fringing *adds* capacitance and therefore force.

### The solved lateral profile

`Π(x)` from the 2-D solve, reduced to the `(depth, width)` pair `edgeTaperedPressure` consumes by matching the first two moments of the load deficit outside a 1 nm rim standoff. Biases are `C-0012`'s **located** operating bracket, not grid points.

| buffer | `L₀` | bias | 1-D load | 2-D centre | ratio | **depth** | **width** | decay `ℓ` | **force gain** |
|---|---|---|---|---|---|---|---|---|---|
| **0.5 mM** | 5 nm | 0.122 V | 0.18660 | 0.18650 | 0.9995 | −0.331 | 5.92 | 1.76 | **+4.91 %** |
| | 7 nm | 0.155 V | 0.15029 | 0.15018 | 0.9993 | −0.453 | 7.65 | 2.27 | **+11.18 %** |
| | 10 nm | 0.134 V | 0.06286 | 0.06277 | 0.9987 | **−0.508** | 9.16 | 2.73 | **+19.23 %** |
| **2 mM** | 5 nm | 0.368 V | 0.40530 | 0.40530 | 1.0000 | −0.250 | 6.86 | 2.04 | +4.93 % |
| | 7 nm | 0.155 V | 0.08889 | 0.08890 | 1.0001 | −0.307 | 7.64 | 2.27 | +10.63 % |
| | **10 nm** | **0.192 V** | **0.03902** | **0.03903** | **1.0003** | **−0.303** | **8.94** | **2.66** | **+14.71 %** |
| **10 mM** | 5 nm | 0.122 V | 0.05142 | 0.05144 | 1.0003 | −0.119 | 6.52 | 1.94 | +5.62 % |
| | 10 nm | 0.192 V | 0.00401 | 0.00402 | 1.0014 | **+0.420** | 2.40 | 0.71 | +8.81 % |
| **held at 3 nm stroke** | 2 nm gap | 0.368 V | 1.99282 | 1.99274 | 1.0000 | −0.052 | 6.56 | 1.95 | **−3.91 %** |

(loads in `pN/nm²`, lengths nm. "Force gain" is the min-margin mapping onto the square 40 nm tile.)

**Every one of the 21 state points is `numericallyResolved`**, and every one reproduces `T-3a`'s 1-D disjoining pressure at the centre-line to between **0.03 % and 0.14 %** — through a solver that shares only `IonModel` with it. That is falsifier 1, declared in advance as the one that kills everything downstream, and it did not fire.

**The sign is not universal, and the two exceptions are informative.** At **10 mM and a 10 nm gap** the depth is genuinely positive — a real taper — because strong screening lets the rim lose more than the fringing adds. And at the **2 nm held gap** the total force is 3.9 % *lower* than 1-D. So the enhancement is a property of the Gen-1 operating box (low salt, working gap), not a law, and the crossing is inside the §3 parameter ranges.

### The shape of the profile, at the design point

2 mM, 10 nm, 0.192 V. Interior load 0.03903 `pN/nm²`.

| `s` from rim [nm] | 0.05 | 0.20 | 0.49 | 0.76 | **0.99** | 1.49 | 2.03 | 3.01 | 4.96 | 8.05 | 12.0 | 20.0 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| load / interior | −39.5 | −7.64 | −0.08 | **1.61** | **1.88** | 1.69 | 1.45 | 1.22 | 1.07 | 1.014 | 1.002 | 1.000 |

**The profile has three regions and only the middle one is a "taper" in any recognisable sense.** Beyond ~5 nm the load is the 1-D one. Between roughly 0.8 and 5 nm it is **enhanced, peaking at 1.88×** about a nanometre inside the rim. And inside ~0.5 nm it **reverses sign** — the field concentration at the rim lifts the tile there — which is a genuine feature of a sharp 90° edge and the reason the fit carries a standoff.

### The dishing, on `C-0006`'s own plate

The plate solver, its rigidities and its foundation stiffnesses are **consumed read-only**, so this is `C-0006`'s number with a new load and not a second opinion. The edge effect is applied as two superposed raised cosines — the smooth term and the rim residual — which `C-0006`'s demonstrated exact linearity in depth licenses.

| case | `k_f` × | depth | width | stroke | **peak dishing** | **/stroke** | rigid? |
|---|---|---|---|---|---|---|---|
| solved edge effect | 0.25 | −0.303 | 8.94 | 19.80 | 4.175 | **0.211** | REJECTED |
| solved edge effect | 0.50 | −0.303 | 8.94 | 9.90 | 2.597 | **0.262** | REJECTED |
| **solved edge effect** | **1.00** | **−0.303** | **8.94** | **4.95** | **1.590** | **0.321** | **REJECTED** |
| solved edge effect | 2.00 | −0.303 | 8.94 | 2.48 | 0.949 | **0.383** | REJECTED |
| solved edge effect | 4.00 | −0.303 | 8.94 | 1.24 | 0.544 | **0.440** | REJECTED |
| … smooth term only | 1.00 | −0.303 | 8.94 | 4.95 | 1.066 | 0.215 | REJECTED |
| `C-0006`'s **assumed** taper | 1.00 | +0.500 | 4.00 | 4.95 | 1.326 | 0.268 | REJECTED |
| solved depth at `C-0006`'s width | 1.00 | −0.303 | 4.00 | 4.95 | 0.803 | 0.162 | REJECTED |

**Applying `C-0009`'s lattice correction for a smooth edge taper (×0.944–0.994) gives 30.3–31.9 % at the design point.** The rigid-plate verdict is REJECTED across the whole foundation sweep by a factor of 2.1–4.4, and the two halves of `C-0006`'s wrong assumption — sign and width — very nearly cancel in the dishing: 26.8 % assumed against 32.1 % solved, a 1.20× move.

**Neither half cancels on its own**, which is why the challenge is worth raising: at `C-0006`'s width the solved depth would give 16.2 %, and at the solved width `C-0006`'s depth would give more. The agreement is a coincidence of two errors, not a vindication.

### The lever-versus-sensor split, resolved

`C-0012` had to write: *"a loop closed between a point-coupled lever and an area-averaging sensor is comparing two numbers that differ by 11 % to 369 % of the stroke"*, that range being `C-0006`'s attachment-scheme ratios from 49 attachments down to one lever.

> **The 11 %–369 % band is a statement about the output coupling, which is a design choice. The number that is *not* a design choice — the irreducible split that survives a perfectly distributed coupling — is the electrostatic edge effect's own dishing: `0.321` of the stroke at the design point (`0.303–0.319` on the lattice), and `0.211–0.440` across the foundation sweep.**

So the split is **32 % of the stroke, and it cannot be designed away.** `C-0016` and `C-0009` require ≥ 45–64 attachments for flatness, which drives the *attachment* contribution below 11 %; below that floor the edge takes over and it does not move.

An area-averaging charge sensor also reads the tile mean plus `δ²/(2λ_D)` with `δ` the RMS dishing: 0.535 nm here, giving **0.036 nm**, 0.7 % of the stroke — six times smaller than `C-0012`'s 0.206 nm, because the edge-driven shape is far less peaked than the thermal one.

### What the edge costs — or rather pays — the total force

The tapered load integrates to a different total from the 1-D pressure over the footprint, which is what **every** force in `C-0008` and `C-0012` is.

| tile | edge | min-margin mapping | additive mapping |
|---|---|---|---|
| **Gen-1, 40 × 40 nm** | 40 nm | **+14.71 %** | **+16.51 %** |
| the 70 × 100 nm test tile (short side) | 70 nm | +8.85 % | +9.43 % |
| a 20 nm tile | 20 nm | **+25.82 %** | +33.01 % |
| a 100 nm tile | 100 nm | +6.32 % | +6.60 % |

The two mappings **bracket the corner**, which this task does not solve: the minimum-margin construction (the one `edgeTaperedPressure` uses) counts a corner once and understates it; the additive one counts it twice and overstates it. The bracket is 1.8 percentage points at 40 nm.

Stated without a mapping at all: **the finite tile behaves electrostatically as one 1.65 nm larger on every side** at the design point, and 0.44–2.24 nm across the operating box. That collar is sub-Debye and it is what the whole effect amounts to.

### The per-load-path forces the edge produces

Through `C-0006`'s own shear routes, per crossover on the worst cut parallel to the helices and per duplex on the worst cut across them:

| `k_f` × | peak crossover force | peak duplex force |
|---|---|---|
| 0.25 | **0.201 pN** | 0.124 pN |
| 1.00 | **0.163 pN** | 0.092 pN |
| 4.00 | 0.132 pN | 0.058 pN |

**Nothing here approaches any allowable.** The smallest per-path allowable in the programme is the 10–15 pN single-duplex unzip, and the worst case above is **50× below it**. `C-0015`'s worst case of 11.54 pN comes from a *discrete anchor*, not from the load, and the edge adds under 2 % to it in quadrature. **The edge does not move a `C-0015` force into an allowable it was clear of.**

---

## Validity range

Respected downstream, and enforced in code where enforceable.

- **MEAN FIELD**, inherited whole from `C-0005` and `C-0008`: 123–214 % at these gaps, and for the *oppositely charged* tile-electrode pair no published result gives even the direction. **A 2-D mean-field solve is still a mean-field solve.** This is the largest uncertainty on every number here and it is bigger than the entire edge effect.
- **POINT IONS.** `C-0008`'s Bikerman bracket raises `|F_es|` by +0.8 % to +56 %, one-sided and upward. It is a *scale* correction and the taper is a *ratio*, which is the argument for not repeating it — and that argument is itself untested here.
- **TWO-DIMENSIONAL, hence a STRAIGHT edge.** The corner is **bracketed by two mappings and not solved**; the bracket is 1.8 percentage points of total force at 40 nm and it widens as the tile shrinks (7.2 points at 20 nm).
- **The rim charge is a 1.85× bracket on the depth** — see the falsifier below. It is *not* a bracket on the rim's own vertical force, which is exactly zero for an uncharged rim.
- **The traction within 1 nm of the rim is not resolvable and is not used.** It is accounted for as a rim line load against the global momentum-flux total.
- **The gap is filled with FREE BUFFER.** `C-0005`'s partitioning layer amplifies the 1-D force by 1.15–1.60×; whether it moves the taper *ratio* is not computed.
- **The Stern series is solved in ONE dimension** and its diffuse-layer potential imposed laterally uniformly. The electrode's compact layer is not re-solved near the rim.
- **The tile is an IMPERMEABLE OBSTACLE with face charges**, exactly as in `C-0008`. A real origami sheet has electrolyte in its interstices.
- **The dishing is `C-0006`'s plate with a new load** and inherits `C-0006`'s validity range whole — linear Winkler foundation, Kirchhoff plate, drained layer (`C-0004`).
- **NOTHING HERE IS MEASURED.**

## Numbers that are cited rather than derived

| number | value | why it is cited, and what it moves |
|---|---|---|
| `ε_r` of water at 300 K | 78 | **CITED**, as in `C-0005`/`C-0008`. ~3 % on the load, ~0 on the ratio. |
| Manning surviving fraction | 11.90 % | **CITED FROM `C-0005` via `C-0008`.** The tile is charge-saturated. |
| Stern capacitance | ~20 µF/cm² | **CITED.** Load-bearing for the bias mapping only (`CH-0007`); the taper is a ratio. |
| `C-0012`'s located operating bias | 0.122–0.368 / 0.082–0.155 / 0.134–0.192 V | **CITED FROM `C-0012`**, as read by `C-0017`. The sweep shows the taper moves under 8 % across each bracket, so this choice is not load-bearing — but it is made correctly anyway, because the project has twice made it wrongly. |
| `C-0006`'s plate, foundation and assumed taper | — | **CITED FROM `C-0006`**, and the solver is **consumed read-only**. |
| `C-0009`'s lattice/plate ratio, smooth taper | 0.944–0.994 | **CITED FROM `C-0009`**, applied rather than ignored. |
| duplex radius, for the rim standoff | 1.0 nm | **CITED** (B-DNA), and it sets only where the unresolvable corner is cut off. |

Everything else — the transverse eigenproblem and its closed form, the 2-D discretisation, the traction from the stress tensor, the global momentum-flux total, the moment fit, the collar width, the dishing and the per-path forces — is derived here.

## Cross-checks passed

1. **Gate 1** — the 2-D load is asserted against `T-3a`'s in the same `pN/nm²`, so a dropped `k_BT` would show as a factor of 4.142; the rim line force is asserted to be a force **per unit length** by scaling with the rim height; the transverse decay bound is asserted through its own quadrature identity, not its formula; the local screening at `y = 0` is asserted to **be** `κ² = 24π l_B c`, which is what would catch a valency dropped from a Boltzmann factor; the equivalent width is asserted through the moment identity it is fitted from.
2. **Gate 2** — nothing charged gives exactly zero load everywhere; the tile's top face is unloaded deep under the tile to 4e−4 of the load, which is the contact-value theorem for an isolated plate; the transverse eigenvalue reduces to its **exact closed form** on uniform screening, to `κ` as `h → ∞` and to `π/2h` as `κ → 0`; the taper fit **round-trips** on the very raised cosine `edgeTaperedPressure` generates; a wider gap gives a weaker load; the edge effect is bounded in depth and **narrower than the tile half-width** (falsifiers 3 and 4, asserted rather than inspected).
3. **Gate 3** — the 2-D charge balance closes to **9.3e−4** at refinement 4, by surface charges read from one-sided derivatives, which is a *different* discrete operator from the flux balance the assembly uses; the load profile is flat at the symmetry plane; **two independent reference planes** agree on the centre-line load to 1.5e−5; and the whole force is recovered by a **global momentum-flux route through one plane**, which owes the corner nothing.
4. **Gate 4** — **nested** 1/2/4 refinement, never 1/2/3/4: the centre-line load converges at second order (errors 5.7e−5, 1.6e−5, 4.1e−6 against `T-3a`), the depth converges (−0.2354 / −0.2906 / −0.3076, ratio 3.2), the global deficit converges at second order, and the two-plane spread falls 4.3e−4 → 7.5e−5 → 1.5e−5. **The lateral domain (20 vs 40 nm), the headroom (16 vs 24 nm) and the far-field boundary condition (Dirichlet against Neumann) move nothing beyond the sixth digit.** Every state point carries `numericallyResolved`; 21 of 21 pass.
5. **Gate 5** — **`T-3a`'s 1-D pressure is reproduced at the centre-line to 0.03–0.14 % at every one of 21 state points**; the solved decay length respects the cheap bound's ceiling everywhere; `C-0006`'s own 1.326 nm dishing at its own assumed taper is reproduced through the same plate solver before being replaced; and the **contact-value route is confirmed to be the bad one** — it is 248 % wrong at refinement 1 and still 1–10 % wrong at the sweep mesh, which is `CLAUDE.md`'s recorded gotcha reproduced in two dimensions.

## The two declared falsifiers that fired

Per §5 and §7: the Plan named six results that would falsify the approach, and it is recorded which fired rather than which were survived.

**Falsifier 5 — "the rim charge mattering" — FIRED.** Taking the rim from uncharged to the face areal density moves the fitted depth from **−0.2906 to −0.1575**, a factor of **1.85**. The two readings are both defensible — the tile's charge is volumetric and the surface it is smeared onto is a convention — so the edge effect is reported as a bracket between them and the rim charge joins the list of unsourced numbers this programme carries. It is **not** a bracket on the rim's own vertical force, which is *exactly* zero for an uncharged rim by the structure of the stress tensor.

**A sixth, undeclared falsifier fired too, and it is the reason the fit has a standoff:** the corner traction is not merely mesh-dependent but mesh-**divergent** — refining 1 → 2 → 4 takes the rim-node load through 10.8, 32.5, 90.8 `pN/nm²` while everything else converges at second order. That is the `r^(−2/3)` traction of a 90° re-entrant corner with one more lateral derivative taken through it. A real origami rim is a row of 2 nm duplex ends. The pointwise profile inside 1 nm is therefore discarded and its content recovered from the global balance.

## Still open — named, not answered

Per §7: *"where a question can't be answered with the available methods, that is stated plainly."*

1. **The corner is not solved.** Two mappings bracket it at 1.8 percentage points of total force for the Gen-1 tile and 7.2 at 20 nm. Only a 3-D solve closes it, and it is worth what the bracket is wide.
2. **The rim charge is unsourced** and it is a 1.85× bracket on the depth. Nothing in the accessible literature gives the charge presented by the cut end of a DNA-origami sheet.
3. **Whether the PEG layer moves the taper ratio** is not computed. The solver accepts a medium; the sweep does not use one.
4. **The direction of the correlation correction for oppositely charged walls remains unknown** (`C-0005`, `C-0008`), and it dominates everything here.
5. **The electrode is taken as macroscopic.** A counter-pad the size of the tile would have its own edge and its own enhancement, and the two would not add.

## Challenges

[`CH-0025`](../challenges/CH-0025-edge-taper-is-an-edge-enhancement.md) is raised **by** this claim against `C-0006`'s and `C-0009`'s edge-taper load case — its sign and its width, not its dishing verdict.
[`CH-0026`](../challenges/CH-0026-forces-are-footprint-integrated-one-dimensional-pressures.md) is raised **by** this claim against `C-0008` and `C-0012`, whose every force is a 1-D pressure multiplied by 1600 nm².

A further result contradicting this claim should be raised in `gpd/challenges/` with methodological grounds rather than overwriting it.
