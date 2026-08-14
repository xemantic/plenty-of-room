# C-0039 — The two compositions do not bracket the arm, because both of their errors run the SAME way: the exact elastica places `E5a16` at **12.72 nm**, outside `C-0034`'s bracket on the long side — and once the rotation is exact the stroke `E5` delivers inside its own compliance ceiling is **3.88 nm, not 10**

| | |
|---|---|
| **Task** | [`T-79`](../tasks/T-79-two-spring-elastica.md) |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*) |
| **Verification type** | **in-silico** (a planar inextensible elastica with a rotational spring at each end, solved by shooting, whose vanishing-load limit is `C-0034`'s own closed form) **+ logical** (a chord bound on an inextensible arm, which needs no elastica and which no joint design can relax) |
| **Verdict** | **PASS on the method, and the design verdict it was sent to confirm FAILS.** The vanishing-load limit of the elastica reproduces `C-0034`'s `c(ρ_n, ρ_f)` at **all four textbook corners and over the interior to 1.7e−14**, which pins the field equation, both boundary conditions and every sign at once. On that solver the adopted design places at **12.7198 nm = 37.4 bp — 1.79 % OUTSIDE `C-0034`'s 11.028–12.496 nm bracket**, because the bracket's premise is wrong: *"two errors run opposite ways and very nearly cancel"* is false, both readings are corrections to the **same** linear boundary-value problem, both **stiffen** it, and applying both moves the arm further out rather than back inside. **The placement clause is discharged exactly (33.3333 pN/nm on the secant at 3 nm) and the compliance clause is not: the tangent is 36.44 pN/nm at §3's acceptable stroke — inside `C-0023`'s 40 pN/nm ceiling with 8.9 % to spare — and 264.2 pN/nm at §3's desired stroke, 6.6× past it, with the SECANT there already 69.9 pN/nm, 2.10× the mandate.** The stroke `E5a16` delivers inside its own ceiling is **3.877 nm**, and **no anchorage, no hinge count and no path count in the sweep reaches 10 nm inside the ceiling — 0 of 34 placements and 0 of 25 sensitivity points.** The cause is geometric: the arm is capped at **13.65 nm** by its own placement condition, so a 10 nm stroke is **≥ 73 %** of the arm's own contour everywhere, and the exact solve then charges a **draw-in of 5.34 nm = 15.7 bp** (42 % of the arm) that an inextensible beam cannot avoid. **`E5` reaches §3's acceptable 3 nm stroke and does not reach its desired 10 nm** — [`CH-0053`](../challenges/CH-0053-both-errors-run-the-same-way-and-the-desired-stroke-does-not-survive-them.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED**, no flexure has been built, and no routing here is a sequence design. |
| **Provenance** | `gpd/results/T-79-two-spring-elastica.json`, produced by `anchoring.TwoSpringElasticaStudyKt`; **10 cheap bounds, 8 continuum records, 34 placements, 14 stroke records, 4 bracket readings, 25 sensitivity records, 15 convergence records, 12 upstream reproductions**; **26 gate-named tests in `TwoSpringElasticaTest`**, the **whole suite** green on `tools/verify.sh` — **BUILD SUCCESSFUL, 0 failures**, on its own isolated tree, with **nothing dropped**; the result file re-run through `tools/study.sh` **twice** and reported *"no result file changed"* both times |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40 × 40 nm tile; 45 load paths on `C-0015`'s 3 × 15 grid (and 15 and 34 swept); §3's 100 pN at the **acceptable** 3 nm and the **desired** 10 nm; single-layer **square-lattice** Rothemund sheet; the arm **free to draw in** (`H = 0`) |
| **Consumes** | [`C-0034`](C-0034-guided-arm-anchorage.md) (`twoSpringArmFactor`, `guidedArmFactor`, `armRestraintParameter`, `TwoSpringArm`, `ArmAnchorage`, `farAnchorageLinkForce`, `anchoredArmForStiffness`, `twoSpringArmForStiffness` — **re-run as a library**), [`C-0029`](C-0029-perpendicular-junction-routing.md) (`RotatingHingeArm`, `hingeArmCeiling`, `rotatingArmForStiffness`, `maximumBaseRotationalStiffness`, `BForm`), [`C-0025`](C-0025-flexure-end-joint.md) (`FlexureEndJoint`, `bondedLengthForTension`), [`C-0023`](C-0023-two-sided-coupling.md) (the 40 pN/nm ceiling, the 45 paths, `SignedCouplingElement`), [`C-0017`](C-0017-output-coupling-stiffness.md) (placement on the **secant**, stability on the **tangent**), [`CH-0029`](../challenges/CH-0029-the-48-pn-allowable-is-a-30-bp-number.md) (`ShearJointAllowable`, **inverted**), [`C-0031`](C-0031-bracketed-root-repair.md) (the bracketed-root discipline), [`C-0040`](C-0040-hinge-line-census.md)/[`CH-0054`](../challenges/CH-0054-the-sixteen-crossover-hinge-line-does-not-exist.md) and [`C-0041`](C-0041-flexure-array-packing.md)/[`CH-0055`](../challenges/CH-0055-the-forty-five-path-array-is-not-a-placement.md) (**as swept premises**, not as inputs), [`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile`, [`C-0006`](C-0006-tile-load-distribution-and-flatness.md), [`C-0015`](C-0015-crossover-phase-and-registration.md) |
| **Raises** | [`CH-0053`](../challenges/CH-0053-both-errors-run-the-same-way-and-the-desired-stroke-does-not-survive-them.md) against `C-0034`'s bracket and, by inheritance, `C-0029`'s `E5g` desired-stroke verdict |
| **Challenged by** | [`CH-0059`](../challenges/CH-0059-the-desired-stroke-placement-is-below-the-stability-floor.md), from [`C-0046`](C-0046-fewer-longer-flexures.md) (`T-99`), on **what state the desired-stroke numbers are read at** — this claim's own open item 5. Every number here reproduces (arm 12.7198 at `2.7e−6`, tangents 36.44 / 264.24, secant 69.94, usable 3.877, cap 13.648, 15-path arm 8.40). The 264.2 pN/nm tangent and the 699 pN are properties of a coupling **placed for §3's acceptable stroke**; placed on the desired clause's own arithmetic (`k_c = 10 pN/nm`) the same element reaches 10 nm at **16.7–33.4 pN/nm**, inside the ceiling, delivering exactly 100 pN — and `C-0017`'s **stability floor** refuses it at 2.34–2.79×. The desired-stroke **verdict is unchanged**; the reason for it moves from the element to the actuator |

---

## The claim, in one line

**`C-0034` reported that its two readings bracket the arm because *"two errors run opposite ways and very nearly cancel"*; they do not — both are corrections to the same linear boundary-value problem, both stiffen it, and the exact composition lands outside their span on the long side — and once the arm is solved as the elastica it is, the thing that decides `E5`'s fate is not the 1.8 % in its length but the geometry that becomes visible only at exact rotation: a 10 nm stroke is three quarters of the arm's own contour, the arm must draw in 5.3 nm to deliver it, and the tangent there is 6.6× the compliance ceiling — so `E5` is an element for §3's ACCEPTABLE stroke, at every anchorage, every hinge count and every path count that was swept.**

---

## The three cheap bounds, which ran first and decided the shape of the answer

| | bound | value | what it settled |
|---|---|---|---|
| **1** | **the chord bound** — an inextensible arm's ends can never be further apart than its contour, so `e ≥ L − √(L² − δ²)` | **5.00–6.38 nm** at §3's desired stroke on the bracket's own arms — **14.7–18.8 bp**, 40–58 % of the arm | **the desired stroke is a large-deflection problem before any elastica runs.** No linear reading of it can be trusted, and the axial question is not a detail. Declared in the task file as falsifier 3, which did **not** fire |
| **2** | **the rotation at the placement point**: `sin θ/θ` at the 9° the linear reading predicts | **0.996** | the large-rotation correction *at the placement point* is ~1 %, so the exact arm should land **just above** the BVP's 12.496 nm — i.e. outside the bracket, on the long side. **It did, at 1.79 %.** Declared as falsifier 2, and it **fired as predicted** |
| **3** | **the geometric stiffening scale** `1/cos²θ` at the ~35° the desired stroke demands | **≥ 1.49** | a **lower** bound on the factor by which the exact tangent must exceed the linear one. The realised factor is **7.2×** — the bound is weak, and it is what made the exact solve worth running rather than argued about |

---

## The model, and the limiting case that pins it

A planar **inextensible elastica**: arc length `s ∈ [0, L]` from the near (hinge) end, tangent
angle `φ(s)` counter-clockwise from the undeformed axis `+x` toward the stroke direction `+z`,
`x(s) = ∫cos φ`, `z(s) = ∫sin φ`. The near end sits on `k_n = n k_θ` grounded on the tile, the far
end on `k_f` grounded on the other body, **neither body rotates**, and the far body applies a
transverse force `F`, an axial force `H` (positive in tension) and — for the reciprocity gate only
— an external tip moment `M₀`:

&nbsp;&nbsp;&nbsp;&nbsp;`EI φ′(s) = M₀ − k_f φ(L) + F(x(L) − x(s)) − H(z(L) − z(s))`

&nbsp;&nbsp;&nbsp;&nbsp;→ &nbsp; **`EI φ″ = −F cos φ + H sin φ`**, &nbsp;
**`EI φ′(0) = k_n φ(0)`**, &nbsp; **`EI φ′(L) = M₀ − k_f φ(L)`**.

Integrated by RK4 over `(φ, φ′, x, z, ∫EIφ′²/2)` and closed by **shooting** on the near-end
rotation, the far-end moment condition being the residual.

> **At vanishing load this IS the boundary-value problem `C-0034` condenses**, so its closed form
> is a limiting case that costs nothing and tests everything at once.

| `ρ_n` | `ρ_f` | `c(ρ_n,ρ_f)` closed form | elastica at `F = 1e−7` pN | departure |
|---|---|---|---|---|
| **∞** | **∞** | **12.000000** | **12.000000** | **1.1e−16** |
| **∞** | **0** | **3.000000** | **3.000000** | **2.3e−15** |
| 0 | 4.082 | 1.729173 | 1.729173 | 1.1e−15 |
| 0.700 | 0 | 0.567568 | 0.567568 | 1.7e−14 |
| 3.750 | 0.250 | 2.047516 | 2.047516 | 1.6e−15 |
| **11.300** | **4.082** | **6.168615** | **6.168615** | **8.9e−16** |
| 11.300 | 32.760 | 8.901731 | 8.901731 | 4.4e−16 |
| 60.000 | 201.200 | 11.272805 | 11.272805 | 1.1e−14 |

> The fourth corner, `c(0,0) = 0`, is a **mechanism**: a beam free to rotate at both ends carries
> no transverse load at any deflection, and the constructor **refuses** it rather than returning a
> small number. `C-0028`'s pinned-base sway column, in a third place.

---

## The bracket, and why it is not one

| reading | exact rotation | exact end condition | arm [nm] | bp | realised `c` | inside `C-0034`'s bracket |
|---|---|---|---|---|---|---|
| `C-0029` series composition, exact rotation (`C-0034`'s short end) | **yes** | no | **11.028** | 32.4 | 7.356 | yes |
| `C-0029`'s `E5g16` as filed, series at an asserted `c = 12` | yes | no | 12.242 | 36.0 | *asserted* 12 | yes |
| `C-0034` two-spring BVP, small deflection (`C-0034`'s long end) | no | **yes** | **12.496** | 36.8 | 6.284 | yes |
| **`T-79` two-spring ELASTICA, exact in BOTH** | **yes** | **yes** | **12.7198** | **37.4** | **6.335** | **NO** |

**Why the bracket fails, and it is a statement about the structure of the two errors, not about
their size.** Both readings start from the same object — the *linear* two-spring beam — and each
adds one correction:

- the **end-condition** correction (series → BVP) **stiffens** the element, because a restrained
  far end carries part of the tip moment and therefore relieves the hinge (`CH-0044`'s own
  finding, that the series composition retains only 0.726);
- the **rotation** correction (linear → exact) also **stiffens** it, because the arc shortens the
  effective span and the restoring lever falls as `cos θ` (`CH-0040`'s own finding).

Two stiffening corrections applied to one baseline do not straddle it. `C-0034` read
11.028 < 12.242 < 12.496 as a bracket because the *series+exact* reading happens to be shorter
than the *BVP+linear* one — but that is a comparison of **different** corrections to a **common**
baseline that neither of them reports, and the exact composition is the sum, not the mean.

---

## What the exact composition costs, and it is not the 1.8 %

The design re-placed at 12.7198 nm on the adopted anchorage (`C-0034`'s `A2`, the arm's own duplex
end at 78.235 pN·nm/rad), 16 crossovers, 45 paths, secant placed by construction:

| stroke [nm] | `F` per path [pN] | assembled [pN] | secant [pN/nm] | **tangent [pN/nm]** | `t/s` | `θ_near` | `θ_far` | **draw-in [nm]** | bp | hinge bond [pN] | link [pN] |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 0.5 | 0.354 | 15.9 | 31.898 | 31.976 | 1.002 | 0.66° | 1.46° | 0.011 | 0.0 | 0.058 | 1.00 |
| **3.0** | **2.222** | **100.0** | **33.3333** | **36.440** | **1.093** | **4.04°** | **8.89°** | **0.383** | **1.1** | **0.355** | **6.07** |
| **3.877** *(the usable limit, solved)* | — | — | — | **40.000** | — | — | — | — | — | — | — |
| 4.0 | 3.075 | 138.4 | 34.595 | **40.615** | 1.174 | 5.46° | 11.97° | 0.690 | 2.0 | 0.480 | 8.18 |
| 6.0 | 5.188 | 233.4 | 38.907 | **56.713** | 1.458 | 8.55° | 18.53° | 1.615 | 4.8 | 0.751 | 12.65 |
| 8.0 | 8.475 | 381.4 | 47.671 | **98.526** | 2.067 | 12.26° | 26.02° | 3.064 | 9.0 | 1.076 | 17.76 |
| **10.0** | **15.542** | **699.4** | **69.940** | **264.240** | **3.778** | **17.51°** | **35.61°** | **5.335** | **15.7** | **1.537** | **24.31** |

Five things fall out and none was assumed.

1. **The placement clause is discharged and the compliance clause is not.** 33.3333 pN/nm on the
   secant at 3 nm to 4.2e−15, by construction; 36.44 pN/nm on the tangent, `t/s = 1.093`, inside
   `C-0023`'s 40 pN/nm ceiling with **8.9 %** to spare. `C-0034` reported `t/s = 1.007` and
   `C-0029` 1.011 — both from the series composition, which understates the stiffening.
2. **The usable stroke is 3.877 nm.** Beyond it the assembled tangent leaves the ceiling, and it
   never comes back: this element is strain-**stiffening** everywhere.
3. **The desired stroke is past the ceiling by 6.6× on the tangent and past the mandate by 2.10×
   on the secant** — and the secant is the *placement* quantity, so the failure is not a
   second-order compliance remark. Delivering 10 nm would take **699 pN**, seven times §3's own
   100 pN.
4. **The draw-in at the desired stroke is 5.34 nm = 15.7 bp**, 42 % of the arm. `C-0029` quotes
   **0.095 nm** at the acceptable stroke because `RotatingHingeArm` charges only the hinge's own
   rigid swing and not the arm's bending; the exact value is **0.383 nm**, 4.0× larger, and it
   still sits above the chord bound (0.359 nm) as it must.
5. **The per-path allowable is a different verdict at the two strokes.** At 3 nm the element's own
   tension (2.22 pN), its hinge bond force (0.355) and its anchorage link force (6.07) are all
   inside `C-0006`'s 10 pN unzip allowable. At 10 nm they are **15.54, 1.54 and 24.31 pN** — two of
   three past it — and the anchorage demands **10.1 bp** of bonded length on `CH-0029`'s inverted
   ladder against `C-0034`'s 7.3.

---

## And no design in the sweep clears the desired stroke

**34 placements** — six anchorages × four hinge counts, plus the adopted anchorage at the counts
`C-0040` shows the lattice can actually supply:

| anchorage | `n` | arm [nm] | cap [nm] | tangent(3) | secant(10) | tangent(10) | **usable [nm]** | clears 10 nm inside the ceiling |
|---|---|---|---|---|---|---|---|---|
| `A1` one link | 8–64 | 8.39–9.83 | 10.09 | 40.20–41.10 | — | — | 2.88–2.97 | **no — and past the ceiling at 3 nm too** |
| **`A2` duplex end** | **1** | 9.131 | 13.648 | 39.18 | — | — | 3.119 | **no** |
| **`A2`** | **3** | 10.591 | 13.648 | 37.46 | 153.1 | 2028.0 | 3.495 | **no** |
| **`A2`** | **4** | 11.035 | 13.648 | 37.13 | 108.0 | 835.6 | 3.600 | **no** |
| **`A2`** | **16** | **12.720** | **13.648** | **36.44** | **69.94** | **264.2** | **3.877** | **no** |
| `A2` | 64 | 13.391 | 13.648 | 36.36 | 67.27 | 236.9 | 3.915 | no |
| `A4` nicked continuation | 8–64 | 13.75–15.13 | 15.381 | 35.97–36.21 | 58.2–63.4 | 160.6–203.3 | 3.99–4.13 | no |
| `A5-2` two-crossover clamp | 8–64 | 14.02–15.39 | 15.644 | 35.97–36.20 | 58.0–63.2 | 159.2–201.0 | 4.00–4.14 | no |
| **`A0` `C-0029`'s asserted ideal guide** | **8–64** | **14.07–15.45** | **15.703** | **35.97–36.20** | **58.0–63.2** | **159.2–200.9** | **4.00–4.14** | **no — not even the limit** |

> **The best usable stroke anywhere in the sweep is 4.136 nm**, at `C-0029`'s own *asserted* ideal
> guide with 64 crossovers — a design that is not a motif and that `C-0034` already showed is
> unavailable. **The desired stroke is not 1.2× away; it is 2.4× away, at the limit of the
> catalogue.**

### The verdict does not move across either premise a sibling claim challenged this iteration

| axis | swept | arm [nm] | usable [nm] | clears 10 nm inside the ceiling |
|---|---|---|---|---|
| **hinge count the lattice supplies** (`C-0040`/`CH-0054`) | **1, 2, 3, 4, 6** | 9.13–11.63 | **3.12–3.72** | **0 of 5** |
| **load path count** (`C-0041`/`CH-0055`; `CH-0029`'s floor is 34) | **15, 34, 45** | **8.40**, 11.44, 12.72 | 2.91–3.88 | **0 of 3** |
| crossover `α` (Chen et al., CITED and FITTED) | 0.6–1.2 | 11.47–13.10 | 3.65–3.94 | 0 of 4 |
| duplex `EI` (CanDo MODEL INPUT; Fields et al. imply −25 %) | ×0.75, ×1 | 11.97, 12.72 | 3.66, 3.88 | 0 of 2 |
| phosphate radius (CITED bracket) | 0.90, 1.00 nm | 12.48, 12.72 | 3.83, 3.88 | 0 of 2 |
| backbone separation (CONVENTION) | 120°, 180° | 12.39, 12.72 | 3.81, 3.88 | 0 of 2 |
| anchorage catalogue (`C-0034`'s counting theorem) | `A0`–`A5-2` | 9.14–14.78 | 2.95–4.09 | 0 of 6 |

> **0 of 25 sensitivity points and 0 of 34 placements clear §3's desired stroke inside the
> ceiling.** At `C-0041`'s **15** paths the arm places at **8.40 nm** and the desired stroke is out
> of *geometric* reach entirely — a tip cannot rise past its arm.

---

## The axial question, which turns out not to be a question

`C-0023` treats *"free to draw in"* and *"held axially"* as a design binary and prices both
(`E3a`/`E3b`). **For an inextensible arm the second option does not exist.** Holding the two ends
at their original axial separation `L` while offsetting them by `δ` puts the chord at
`√(L² + δ²) > L`, which the contour cannot reach — so the "held" reading is not a stiffer beam, it
is a **different constitutive assumption**, and its cost is a strain:

&nbsp;&nbsp;&nbsp;&nbsp;**`ε ≥ √(1 + (δ/L)²) − 1`** &nbsp;⟹&nbsp; `N ≥ S ε`

| stroke | draw-in demanded (free) | held strain | **held tension** | against the 65 pN nicked ceiling |
|---|---|---|---|---|
| **3 nm** | **0.383 nm = 1.1 bp** | 2.74 % | **30.2 pN** | inside |
| 10 nm | 5.335 nm = 15.7 bp | 27.2 % | **299.2 pN** | **4.6× past** |

So the free reading — which is what this claim solves and what `C-0029`'s `RotatingHingeArm`
assumes — is the **favourable** one, and the desired stroke fails inside it.

---

## The five verification gates

Executed as **26 gate-named tests** in `src/test/kotlin/anchoring/TwoSpringElasticaTest.kt`;
`tools/verify.sh` **BUILD SUCCESSFUL, 0 failures**, on its own isolated tree, nothing dropped.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | `ρ = kL/EI` is dimensionless; under `x → λx`, `EI → μEI`, `k → (μ/λ)k` the vanishing-load stiffness is unchanged **and the NONLINEAR solve obeys the same similarity** — a displacement `λδ` needs a force `(μ/λ²)F`, asserted to 1e−8 at `λ = 2`, `μ = 8`, which nothing in the solver imposes; unphysical arguments throw at seven entry points, including the free-free **mechanism** and a stroke past the arm's own contour | **PASS** |
| **2 — limiting cases** | the vanishing-load limit reproduces `twoSpringArmFactor` at **all four textbook corners** and over a **25-point interior grid**, worst departure **1.7e−14**; the rigid-arm free-far-end limit reproduces `C-0029`'s `RotatingHingeArm` to 1e−5 at three strokes; the free-far-end small-load limit reproduces `C-0023`'s series composition to 1e−6; **`CH-0040`'s own trigonometry is RECOVERED, not assumed** — `δ = r sin θ` to 1e−6 and `k θ = F r cos θ` to 1e−5 in the rigid limit; the draw-in is quadratic in the stroke and **never below the chord bound** at four strokes; the held reading is a strain bound | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | **nested** RK4 refinement 100 → 200 → 400 → 800 → 1600, each doubling winning **≥ 8×** and the finest at 1.6e−13 — fourth order, measured, not asserted; the placement discharges its own secant to 1e−7 and the arm cap is the same placement at a **rigid** hinge and strictly bounds every placement at four hinge counts; the tangent agrees with central differences at **two** step sizes to 1e−5; the axial tension reduces the draw-in monotonically and **never to zero**; `max_s|φ|` is carried as a validity field and stays under `π/2` at both §3 strokes; a stroke past the contour is **refused, not approximated**; **the result file was re-emitted through `tools/study.sh` twice and reported *"no result file changed"* both times** | **PASS** |
| **5 — literature and upstream** | `C-0034`'s two placements (11.028, 12.496) and its realised factor (6.284); `C-0029`'s `E5g16` arm (12.2423721), both hinge-arm ceilings (9.76624511 / 15.5029478) and the two-terminus couple (78.2352941); `C-0009`'s hinge constant (13.5294118); **`CH-0044`'s 54.61 pN/nm over-placement, reproduced BOTH from the closed form and independently from the elastica at vanishing load**; `CH-0029`'s 18.796 pN at 8 bp; and the mandate itself to 4.2e−15. Worst departure over 12 reproductions, **excluding the values their own claims quote rounded to five digits** (11.028, 12.496, 6.284, 54.61, 18.796, which land at 4.4e−5 to 8.4e−6): **2.8e−9** | **PASS** |

### Gate 3 — four things that are not restatements of the construction

1. **Maxwell-Betti reciprocity between two independently integrated off-diagonals.** The tip
   **translation** under a unit tip **moment** — a quadrature of `sin φ` over the whole arm —
   against the tip **rotation** under a unit tip **force** — an endpoint of the shooting solve.
   They agree to 1e−7, and nothing in either computation forces it. `C-0030`'s check, on a
   different structure.
2. **The global moment balance `EI(φ′(0) − φ′(L)) = F x(L) − H z(L)`** holds to 1e−10 at three
   forces × three axial loads including **compression** — the shot end curvatures against the
   integrated end position, two quantities the solver never compares.
3. **The external work equals the stored strain energy** — `∫F dδ` accumulated over 400 load
   increments against `∫EIφ′²/2 ds + ½Σ M_end θ_end` at the endpoint, to 2e−5 at three strokes.
4. **The first integral `EIφ′²/2 + F sin φ + H cos φ` is constant along the arm** to 1e−8 at three
   forces, and **the reaction is odd while the draw-in is even** at three strokes — the sidedness
   `C-0023`'s whole `CH-0027` argument rests on, re-established under the elastica.

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | the vanishing-load limit failing to reproduce `twoSpringArmFactor` | **no** | all four corners and a 25-point interior to 1.7e−14 |
| 2 | the placed arm landing **inside** 11.03–12.50 nm | **YES, as cheap bound 2 predicted** | 12.7198 nm, 1.79 % past the long end — and it is `CH-0053` |
| 3 | the chord bound at the desired stroke coming out below one base pair | **no** | 14.3–18.8 bp, and the exact draw-in is larger still |
| 4 | the exact tangent at the desired stroke landing inside 40 pN/nm | **no** | 264.2 pN/nm, 6.6× past it |
| 5 | the solve being worse than fourth order in the step count | **no** | ≥ 8× per doubling over four nested refinements |

**A result that was not anticipated:** the 1.8 % in the arm length — the number this task was
commissioned to produce — is the *least* important thing the exact composition says. What it
actually delivers is the **shape of the characteristic**: `t/s` runs 1.002 → 1.093 → 3.778 over
the stroke, and a linear reading of a 12.7 nm arm at a 10 nm stroke was never going to be a
percent-level approximation.

---

## Does `C-0034`'s verdict survive?

**Its model survives in full and is re-run rather than restated. Its bracket does not, and its
design's desired-stroke verdict does not.**

| `C-0034` said | this claim finds |
|---|---|
| `c(ρ_n, ρ_f)` is the exact two-spring end factor | **confirmed to 1.7e−14** by an independent nonlinear solver at vanishing load — the strongest possible check, and it costs nothing |
| the counting theorem, the anchorage catalogue, the fixed-point cap | **untouched and used**, not restated; the cap moves **outward** (13.428 → 13.648 nm) because the exact arm stiffens |
| *"the two compositions bracket the arm at 11.03–12.50 nm"* | **false** — 12.7198 nm, outside on the long side. [`CH-0053`](../challenges/CH-0053-both-errors-run-the-same-way-and-the-desired-stroke-does-not-survive-them.md) |
| *"two errors run opposite ways and very nearly cancel"* | **both run the same way.** They are two corrections to one baseline and both stiffen it |
| tangent 33.56 at 3 nm, `t/s` = 1.007 | **36.44 and 1.093** — still inside the ceiling, with 8.9 % rather than 16 % of margin |
| tangent **36.78 at 10 nm, inside the ceiling with 8.1 % to spare** | **264.2 pN/nm, 6.6× past it**, and the secant there is already 2.10× the mandate |
| **every two-link anchorage clears §3's desired 10 nm stroke** | **none of them does, inside the compliance ceiling** — 0 of 34 placements and 0 of 25 sensitivity points |
| draw-in 0.095 nm at the acceptable stroke (via `C-0029`) | **0.383 nm**, 4.0× larger, and **5.34 nm = 15.7 bp** at the desired stroke |
| `CH-0044`'s 54.61 pN/nm over-placement of `E5g16` | **reproduced twice**, from the closed form and from the elastica independently |

---

## Validity range

- **TRL 1–3. Nothing here is measured.** No flexure has been built, and no routing here is a
  sequence design. Base pairs make the statement concrete; they do not specify a staple.
- **The arm is modelled as a uniform inextensible Euler-Bernoulli rod at `EI = 230 pN·nm²`**,
  which is a **CanDo MODEL INPUT** and not a measurement. Swept at Fields et al.'s implied −25 %:
  the arm falls to 11.97 nm, the usable stroke to 3.66 nm, and **no verdict moves**.
- **`k_θ` is `C-0009`'s CITED, FITTED constant** and it is a **small-angle** fit. This claim
  extrapolates it to 4.0° at the acceptable stroke and **17.5°** at the desired one — which is
  `C-0029`'s open item 5, inherited unchanged and now load-bearing in the other direction: if the
  hinge *softens* at large angle the tangent at the desired stroke falls, but the arm shortens
  with it and the geometric ratio `δ/L` — which is what stiffens — gets **worse**.
- **The far anchorage is a linear rotational spring at all angles.** Its couple rests on
  `C-0020`'s **derived, unmeasured** `k_s`; swept through the whole anchorage catalogue from a
  ball joint to a two-crossover clamp, over which the usable stroke moves 2.95–4.09 nm and **no
  verdict moves**.
- **The arm is FREE to draw in (`H = 0`)**, which is the favourable reading; the held reading does
  not exist for an inextensible rod, and its extensible substitute costs 299 pN at the desired
  stroke. Where the draw-in comes from — 1.1 bp at the acceptable stroke — is a joint design and
  is not answered here.
- **The elastica is solved on its small-rotation branch.** `max_s|φ|` is carried as a field and
  stays under `π/2` at both §3 strokes on the adopted design; past that the tip force's moment arm
  reverses, the shooting residual stops being monotone, and the solver **refuses** rather than
  returning the wrong root. Two of the 34 placements report *"the arm folds before reaching the
  desired stroke"* and are recorded as failures on that ground.
- **Axial loads are restricted to `|H| L²/EI ≤ 40`**, the shooting method's own conditioning
  limit — declared, not worked around, and irrelevant to the `H = 0` primary reading.
- **The hinge count and the path count are premises this claim swept and did not settle.**
  `C-0040`/`CH-0054` and `C-0041`/`CH-0055` land against them from two directions in the same
  iteration; the verdict here holds at every value either of them admits.
- **`C-0023`'s 40 pN/nm ceiling is a DECLARED design ceiling**, 1.2× the mandate, not a measured
  limit. At a ceiling of 60 pN/nm the usable stroke would be ~6.2 nm and the desired stroke would
  still fail; the desired stroke would need a ceiling of **264 pN/nm**, i.e. 7.9× the mandate.
- **One flexure per load path**, exactly as `C-0023`, `C-0025`, `C-0029` and `C-0034` assume.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| duplex `EI` | 230 pN·nm² | **CITED, a CanDo MODEL INPUT** (Kim et al., *NAR* **40**:2862, 2012), **not a measurement**; Fields et al.'s −25 % swept |
| duplex stretch modulus `S` | 1100 pN | **CITED, MEASURED**, Wang et al., *Biophys. J.* **72**:1335 (1997) |
| crossover hinge `k_θ` | 13.53 pN·nm/rad, `α ∈ [0.6, 1.2]` | **CITED, FITTED**, Chen et al., *JACS* **136**:6995 (2014) SI §S2, via `C-0009`; swept, **no verdict moves** |
| crossover in-plane `k_s` | 64.71 pN/nm | **DERIVED** (`C-0020`), **NOT measured**, entering only through the anchorage couple |
| phosphate radius in B-form DNA | 1.00 nm | **CITED**, Hedley et al., *Phys. Rev. X* **14**:031042 (2024), via `C-0029`; the 0.90 nm fibre reading swept |
| backbone azimuthal separation | 120°/180° | **CONVENTION**, both carried, via `C-0029` |
| the shear allowable's four constants | `x₀`, `x₁`, `α`, `β` | **CITED, MEASURED**, Strunz et al. (1999), via `C-0024`/`CH-0029` |
| per-path allowables | 10 / 65 pN | **CITED** via `C-0006` |
| the compliance ceiling | 40 pN/nm | **DECLARED** by `C-0023`, not measured |
| §3 targets | 100 pN, 3 nm, 10 nm, 40 × 40 nm, 2 mM | **CITED** |
| `C-0034`'s and `C-0029`'s design numbers | 11.028, 12.496, 12.2424, 9.766, 15.503, 78.235, 54.61 | **CITED**, and every one reproduced here as a gate-5 test |

Everything else — the elastica, its field equation and boundary conditions, every placement, cap,
secant, tangent, rotation, draw-in, per-path force, usable stroke and verdict, the chord and
held-strain bounds, and the whole sensitivity sweep — is **derived here in code**, with `C-0034`'s,
`C-0029`'s, `C-0025`'s and `C-0023`'s pipelines **re-run rather than tabulated**.

## Still open — named, not answered

1. **What element, if any, reaches §3's desired 10 nm stroke.** This claim closes `E5` at the
   desired stroke on the same geometry that closed the standoff branch — a member cannot be driven
   most of its own length without stiffening — and does not propose a replacement. **`T-99`** and
   **`T-100`** are already asking the adjacent questions; neither of them has this claim's
   characteristic yet.
2. **Whether §3's desired stroke is reachable by a coupling at all**, or whether the honest answer
   to `A8.2` is that 10 nm is out of reach and 3 nm is not. Three independent routes now say the
   same thing — `CH-0040`'s cap, `C-0040`'s hinge inventory and this claim's geometry — and none
   of them was looking for it.
3. **Where the 1.1 bp of draw-in at the acceptable stroke comes from.** `C-0025` prices such a
   demand and `C-0035` shows a standoff can *supply* one; `E5` has no standoff, and nothing has
   said which joint gives.
4. **Chen et al.'s hinge constant at 17.5°**, and the anchorage couple at 35.6° — both fits are
   small-angle and both are now evaluated well outside them.
5. **A stroke-dependent compliance ceiling.** `C-0023`'s 40 pN/nm is declared at no stroke;
   whether the actuator needs it at the desired stroke or only at the working point is a question
   for `C-0017`/`C-0018`, and it decides how badly this element misses.

## Challenges

**Raises [`CH-0053`](../challenges/CH-0053-both-errors-run-the-same-way-and-the-desired-stroke-does-not-survive-them.md)**
against `C-0034`'s bracket statement and, by inheritance, `C-0029`'s `E5g` desired-stroke verdict.
**No number in either fails to reproduce** — 12 reproductions, worst departure 4.4e−5 against
values their own claims quote rounded to five digits, and 2.8e−9 otherwise.

**None stands against this claim.** The three ways it would fail:

1. **A demonstration that the arm's far end is NOT rotationally restrained against the other body**
   — that it is a ball joint, or that the second body is free to rotate. Then the element is
   `C-0029`'s swinging lever, the geometry is milder, and the arm cap falls to `A1`'s 10.09 nm,
   which is *also* below the desired stroke. Either way the desired stroke fails; what changes is
   the number.
2. **A crossover hinge whose moment is strongly sublinear above ~10°.** Then the tangent at the
   desired stroke falls — but so does the placed arm, and `δ/L` rises, so the direction of the
   correction is not obvious and would have to be solved rather than argued.
3. **A compliance ceiling above 264 pN/nm at the desired stroke**, i.e. 7.9× the mandate. That is
   not a refutation of the mechanics but a change of the acceptance clause, and it would have to
   be argued against `C-0018`'s pull-in analysis, which the stiffening moves.
