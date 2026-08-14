# C-0029 — A 90° routing exists and it is a scaffold excursion, but a duplex END has only TWO strand termini, so every perpendicular base is a HINGE with a lever arm bounded by the duplex's own radius — and a column buckles about the axis that leaves free

| | |
|---|---|
| **Task** | [`T-67`](../tasks/T-67-perpendicular-junction-routing.md) |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the joint belongs to |
| **Verification type** | **logical** (a counting theorem about strand termini, which no simulation can overturn) **+ in-silico** (B-form backbone geometry solved for the junction's realisable links, then `C-0028`'s own design pipeline re-run on the base that results, and `C-0023`'s `E5` re-solved under exact rotation) **+ literature** (~110 primary-source queries, with a `read directly` / `abstract only` / `not found` flag on every statement and four load-bearing quotes re-verified by hand) |
| **Verdict** | **PASS, and the answer is not the one the task was sent for: a routing exists, and it is the base's ARITHMETIC that fails, not its chemistry.** The two-link closure search finds a covalent configuration at **0.600 nm on both links** — inside the measured `[0.60, 0.70] nm` phosphodiester step, **zero unpaired nucleotides** — and its optimum is a **scaffold excursion**: one strand leaves the sheet duplex at **strand 1, bp 9**, forms the whole standoff, and returns at **bp 10**, with the base chord laid at **−87.8°**, i.e. across the sheet helix. But a B-form duplex has **two backbones**, so a duplex **end** has **two strand termini**, so a base joint has **at most two links** and their separation is the terminal chord `2 r_P sin(Δ/2) ≤ 2.0 nm`. **`C-0028`'s `B2` needs a 1.345 nm lever arm out of a 1.0 nm radius, and a couple goes as the square: 261.2 pN·nm/rad against a hard ceiling of 78.24 — over by 3.34×** ([`CH-0039`](../challenges/CH-0039-the-base-couple-needs-a-lever-arm-the-standoff-does-not-have.md)). And two links on a chord restrain **one axis**: about the chord the base keeps only `2 k_bond,θ` = **13.53 pN·nm/rad**, which **is** `C-0028`'s `B1`. **A column buckles about its softest axis, so `P6` fails at every length and the standoff branch closes at §3's DESIRED 10 nm stroke** — it stands at §3's **acceptable** 3 nm from `ℓ = 5` to 10 nm even on the weak axis, where the duty is only 1.111 pN. The fallback does not rescue it as filed: **`E5` is a small-rotation law evaluated at 46.9°, and its arm is capped at `(c n EI/k)^(1/3)` = 9.77 nm, BELOW the 10 nm stroke, at any hinge count** ([`CH-0040`](../challenges/CH-0040-e5-is-a-small-rotation-law-at-47-degrees.md)). **What does close it is one letter in that cube root:** a **guided** arm (`c = 12`) lifts the cap to 15.50 nm, and **`E5g16` — a 12.24 nm = 36 bp guided arm on 16 crossovers — places at 33.3333 pN/nm exactly, holds a tangent of 33.68 pN/nm at 3 nm and 38.68 at 10 nm inside `C-0023`'s ceiling, turns 23.2° at the desired stroke and puts 2.04 pN on a crossover against a 10 pN unzip allowable.** It needs no motif that is not already in every published origami. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED, and no strand routing here is a sequence design.** The closure search tests a **necessary** condition for a link and never a sufficient one. |
| **Provenance** | `gpd/results/T-67-perpendicular-junction-routing.json`, produced by `anchoring.PerpendicularJunctionStudyKt`; **4 counting bounds, 2 seats, 5 routings, 2 phase records, 48 design records, 8 thresholds, 9 hinge-arm records, 16 sensitivity records, 21 upstream reproductions, 9 literature records**; **31 gate-named tests in `PerpendicularJunctionTest`, 223 in `anchoring`, 1046 in the suite, 0 failures**; `tools/verify.sh` **BUILD SUCCESSFUL** on its own isolated tree; the result file re-run through `tools/study.sh` **twice** and reported *"no result file changed"* both times |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40 × 40 nm tile; 45 load paths on `C-0015`'s 3 × 15 grid; §3's 100 pN at the **acceptable** 3 nm and the **desired** 10 nm; single-layer **square-lattice** Rothemund sheet at the SAXS-measured 2.69 nm interhelical distance |
| **Consumes** | [`C-0028`](C-0028-standoff-base-joint.md) (`StandoffBase`, `basedNormalStandoff`, `standoffBucklingLoad`, `baseRestraintParameter`, `bucklingStroke`, `baseRotationalStiffnessThreshold`, the six predicates — **re-run as a library**), [`C-0025`](C-0025-flexure-end-joint.md) (`PartiallyRestrainedFlexure`, `flexureSpanForJoint`, `c(ρ)`, `g(β)`, `S_eff`), [`C-0023`](C-0023-two-sided-coupling.md) (`E5`, `CrossoverHingeFlexure`, `hingeArmForStiffness`, the 40 pN/nm ceiling, the 45 paths), [`C-0015`](C-0015-crossover-phase-and-registration.md) (the 32 bp phase period quantised to base pairs), [`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile` (`EI`, `S`, the rise, `d`, `k_θ`, `k_s`, the allowables), [`C-0017`](C-0017-output-coupling-stiffness.md), [`C-0006`](C-0006-tile-load-distribution-and-flatness.md) |
| **Raises** | [`CH-0039`](../challenges/CH-0039-the-base-couple-needs-a-lever-arm-the-standoff-does-not-have.md) against `C-0028`, [`CH-0040`](../challenges/CH-0040-e5-is-a-small-rotation-law-at-47-degrees.md) against `C-0023` |
| **Challenged by** | [`CH-0072`](../challenges/CH-0072-the-alignment-is-not-free-on-the-torsion-feasible-set.md), from [`C-0059`](C-0059-torsion-feasible-routing.md) (`T-124`), on **the recommended topology**: re-optimised on `C-0057`'s torsion-feasible set the **scaffold excursion cannot be aligned** — 1 of the 120 best-aligned placements closes, at a **39.0°** chord — while **two independent staples** deliver 7 closing placements, the best at **90.0°**, i.e. *exactly* the flexure axis. The existence result and the counting theorem are untouched. And [`CH-0070`](../challenges/CH-0070-the-reported-optima-are-in-the-torsion-infeasible-set.md), from [`C-0057`](C-0057-backbone-torsion-closure.md). Also [`CH-0044`](../challenges/CH-0044-c-equals-twelve-and-the-series-composition-cannot-both-be-right.md), from [`C-0034`](C-0034-guided-arm-anchorage.md); and [`CH-0054`](../challenges/CH-0054-the-sixteen-crossover-hinge-line-does-not-exist.md), from [`C-0040`](C-0040-hinge-line-census.md) (`T-81`), on **`E5g16`'s hinge count** — a 40 nm tile carries **four** crossovers in one hinge line, not sixteen. `E5g8`, `E5g16` and `E5g32` all reproduce to ≤ `3.8e−9`; what fails is the count. And [`CH-0053`](../challenges/CH-0053-both-errors-run-the-same-way-and-the-desired-stroke-does-not-survive-them.md), from [`C-0039`](C-0039-two-spring-elastica.md) (`T-79`), on the `E5g` table's *"reaches §3's desired stroke: **yes**"* — on the composition exact in both the rotation and the end condition the tangent there is **264.2 pN/nm against `C-0023`'s 40 pN/nm ceiling** and the secant **2.10× the mandate**, at **0 of 34** placements and **0 of 25** sensitivity points. `E5g16`'s arm (12.2423721) and both hinge-arm ceilings reproduce to ≤ 2.8e−9; §3's **acceptable** stroke is untouched |

---

## The claim, in one line

**`T-67` asked whether a 90° routing exists and the answer is yes — the search finds one, in base pairs, and its optimum is the scaffold excursion — but the question was the wrong one: what a perpendicular junction cannot do is not *form* but *resist*, because a duplex end presents exactly TWO strand termini and a base couple is `2 k_s a²` with `a ≤ r_P = 1.0 nm`, so `C-0028`'s 1.345 nm lever arm is not a motif nobody has built but a length that does not exist on the part; and because two links lie on a chord, the couple has ONE axis and the orthogonal one keeps only the bonds' own hinges, which is exactly the base `C-0028` showed buckles everywhere — so the standoff branch closes at §3's desired stroke and the design that survives is `E5` with the one change the placement condition's own cube root asks for.**

---

## The three cheap bounds, which ran first and decided the shape of the answer

| | bound | value | what it settled |
|---|---|---|---|
| **1** | **the terminus count** — a duplex has two backbones, so a duplex end has two strand termini | **2, and the chord `2 r_P sin(Δ/2) ≤ 2.0 nm`** | the base has **at most two links** and a lever arm of at most **1.0 nm**, against `C-0028`'s **1.345** |
| **2** | the couple ceiling against `C-0028`'s own `P6` threshold ladder | **78.24 pN·nm/rad** hard, **62.06** at the nominal 120° groove, against **68.83** required at `C-0028`'s own 8 nm | `B2`'s 261.2 is over by **3.34×**; the ceiling meets the requirement only up to **8.25 nm** (hard) and **7.50** (nominal) |
| **3** | the azimuthal quantum | `360°/10.67 = ` **33.74°/bp**, worst misalignment ±16.87°, `cos²` = **0.9158** | **the phase costs at most 8.4 %** of the couple, against the 3.34× the lever arm costs. **The phase is cheap; the ceiling is what binds** |

Only because bound 1 gives a **ceiling** rather than a value, and bound 2 puts that ceiling within a factor of 1.14 of the requirement it has to meet, was the backbone-geometry solve worth running at all. Had `C-0028`'s 1.345 nm been reachable this task would have closed in a paragraph — which is declared in the task file as falsifier 2.

---

## The counting theorem, which is the whole method

> **A B-form duplex has TWO backbones, so a duplex END presents exactly TWO strand termini**,
> at the two backbone positions of its terminal base pair.
> Every covalent link grounding the standoff has to start at one of them. Therefore:
>
> 1. a base joint has **at most two links**, whatever the routing;
> 2. their separation is the **terminal chord** `2 r_P sin(Δ/2)`, bounded by the duplex's own diameter, so the lever arm is at most `r_P` = **1.0 nm**;
> 3. two links on a chord react a moment as a **couple about the chord's perpendicular bisector only** — about the chord itself nothing is left but `2 k_bond,θ` = 13.53 pN·nm/rad, which reproduces `C-0028`'s `B1` **to the last digit** and is asserted as a gate-3 test.
>
> **No simulation can overturn a count.** An atomistic or coarse-grained model could only find the junction *additionally* frustrated; it cannot add a third backbone. This is why an oxDNA study was not the right spend, and it is the Plan section's cost justification.

The phosphate radius is not a modelling choice either: *"Phosphates (red circles) sit at a radius of `a_DNA ≈ 10 Å`"* — which **is** the duplex's own radius, because B-form DNA's 2 nm diameter is the phosphate backbone.

### The ceiling that follows, at four readings of the one parameter it depends on

| reading | `Δ` [deg] | chord [nm] | lever arm [nm] | **`k_θ_base` ceiling** | of `C-0028`'s `B2` | longest standoff its `P6` admits |
|---|---|---|---|---|---|---|
| narrow | 90 | 1.414 | 0.707 | **45.88** | 0.176 | 6.50 nm |
| **nominal** | **120** | **1.732** | **0.866** | **62.06** | **0.238** | **7.50 nm** |
| | 140 | 1.879 | 0.940 | 70.67 | 0.271 | 8.00 nm |
| wide | 154 | 1.949 | 0.974 | 74.96 | 0.287 | 8.25 nm |
| **hard, convention-free** | **180** | **2.000** | **1.000** | **78.24** | **0.300** | **8.25 nm** |

> **`C-0028`'s `B2` is over the hard ceiling by 3.34× and its `B3` (977 pN·nm/rad) by 12.5×.**
> The backbone separation `Δ` is the one parameter the couple is sensitive to and it is a
> convention as much as a measurement, so **the 180° row is carried as the bound that no
> convention can move.**

---

## The routing, in base pairs, and it is the scaffold excursion

The search sweeps the standoff's axial position, its lateral seat and its azimuth, minimising the
**window residual** of the worse link — how far a phosphate pair is from the **measured**
`[0.60, 0.70] nm` phosphodiester step, *"C3-endo (interphosphate distance 0.6 nm) to C2-endo
conformation (interphosphate distance 0.7 nm)"*. Minimising a bare *distance* instead parks the
search on the van der Waals floor, where no backbone exists — that is asserted as a gate-2 test.

| id | routing | link 1 gap | link 2 gap | nt | targets | chord azimuth | verdict |
|---|---|---|---|---|---|---|---|
| **`R1`** | **two independent staples** | **0.600** | **0.600** | **0 / 0** | **strand 1, bp 9 and bp 10 of the same sheet duplex** | **−87.8°** | **a HINGE** |
| **`R2`** | **scaffold excursion**, out at bp `m` and back at `m+1` | **0.600** | **0.600** | **0 / 0** | the same pair | −87.8° | **a HINGE** |
| `R3` | one link — a hairpin overhang, a sticky end, the literature's pin | 0.600 | — | 0 | strand 0, bp 22 | — | **a BALL JOINT** |
| `R1w` | `R1` at the 154° groove | 0.600 | 0.600 | 0 / 0 | strand 0 bp 15, strand 1 bp 19 | +38.0° | a HINGE |
| `R2w` | `R2` at the 154° groove | 0.647 | 0.647 | 0 / 0 | strand 0, bp 2 and bp 3 | −66.5° | a HINGE |

Four things fall out and none was assumed.

1. **A routing exists, and the two most different topologies find the same one.** `R1`'s free
   optimum lands on **consecutive phosphates of a single sheet strand** — which is precisely
   `R2`'s constraint. The best independent-staple routing *is* a scaffold excursion: one strand
   leaves the sheet duplex at bp `m`, forms the whole standoff as a hairpin, and returns at
   `m+1`. **`T-67`'s falsifier 3 — that no phase closes covalently — did not fire.**
2. **The seat is ON a duplex, not in the valley.** The optimum sits at `y_c = 0.168 nm`, i.e.
   essentially over the sheet duplex's own axis, with the terminal base pair at `z = 1.000 nm`.
   The valley seat exists — its face dips to **0.9386 nm**, *lower* than on a duplex, because the
   rim descends between the neighbours — but it is not where the links close best.
3. **The chord comes out across the sheet helix**, at −87.8° from the helix axis, which is the
   orientation that makes the couple useful. That is a **result** of the closure, not a choice.
4. **The wide-groove reading pays for its bigger lever arm in reach**: `R2w`'s links sit at
   0.647 nm, still inside the step but nearer its top.

> **So the answer to *"does a 90° routing exist"* is YES, and the honest reading of that yes is
> narrow: the model tests a NECESSARY condition — a phosphate pair inside the measured step with
> no van der Waals overlap — and never a sufficient one, because no backbone torsion angle is
> checked. A *"closes"* verdict is an UPPER bound on buildability. What kills the design is not
> this bound; it is the counting theorem, which is not a model.**

### And the 90° exit is not *set* by the routing at all

A two-link base pins an **azimuth**, not an **angle**: the polar tilt about the chord is a free
degree of freedom of the routing. What sets it is one-sided **sterics** — a flat end face on a
cylinder makes a **line** contact of length `2R` along the sheet helix, which blocks rocking
*along* the line to first order and leaves **exactly `asin(R_s/2R_h)` = 30.0°** of free play
*across* it, i.e. **4.00 nm** of transverse dead band at an 8 nm head against `P1`'s 0.1 nm.

And that contact is **one-sided**: it can react only `N·R` = **4.64 pN·nm** at the 8 nm design,
against the **19.48 pN·nm** the base already carries in the *loaded* plane — **3.7× short**. So
the contact is not a supplementary restraint anywhere, and **the two links carry everything.**

---

## The design that results, and the axis that closes it

`C-0028`'s pipeline re-run unchanged, at 45 paths, secant placed at 33.3333 pN/nm by
construction, duty and margin at §3's **desired** 10 nm on the element's own end shear and the
**free-head** critical load.

| base | `ℓ` | span [nm] | tangent | duty(10) | `P_c` **restrained** | margin | `P_c` **weak axis** | margin | verdict, restrained | **verdict, weak axis (adopted)** |
|---|---|---|---|---|---|---|---|---|---|---|
| `B0` ideal clamp | 8 | 31.64 | 37.39 | 5.52 | 8.87 | 1.61 | — | — | PASS | — |
| **`B2` (`C-0028`'s)** | 8 | 31.06 | 36.51 | 5.11 | 7.21 | 1.41 | — | — | — | **FAIL `P7` — not realisable** |
| `T1` hard ceiling | 5 | 32.02 | 39.16 | 6.32 | 9.71 | **1.54** | **2.46** | **0.39** | PASS | **FAIL `P6`** |
| **`T1`** | **7** | **30.58** | **36.21** | **4.97** | **5.98** | **1.20** | **1.69** | **0.34** | **PASS** | **FAIL `P6`** |
| `T1` | 8 | 30.09 | 35.48 | 4.64 | 4.89 | 1.05 | 1.46 | 0.31 | PASS | **FAIL `P6`** |
| `T1` | 9 | 29.69 | 34.98 | 4.42 | 4.07 | 0.92 | 1.21 | 0.27 | FAIL `P6` | FAIL `P6` |
| `T3` nominal 120° | 5 | 31.52 | 38.50 | 6.01 | 8.39 | 1.40 | 2.46 | 0.41 | PASS | **FAIL `P6`** |
| **`T3`** | **7** | **30.25** | **35.90** | **4.83** | **5.27** | **1.09** | **1.69** | **0.35** | **PASS** | **FAIL `P6`** |
| `T3` | 8 | 29.81 | 35.26 | 4.54 | 4.35 | 0.96 | 1.46 | 0.32 | FAIL `P6` | FAIL `P6` |
| `T4` about the chord | 8 | 27.38 | 34.07 | 4.01 | 1.46 | 0.36 | 1.46 | 0.36 | FAIL `P6` | FAIL `P6` |

Three things fall out.

1. **`P3` stops binding entirely, exactly as `C-0028` predicted for a softer base.** Every
   realisable base holds the tangent inside 40 pN/nm from 5 nm up, where the ideal clamp fails it
   below 7. The compliance ceiling is not the constraint on this base; stability is — `C-0028`'s
   finding, in a new place and with a smaller base.
2. **On the restrained axis the window survives, shifted DOWN: `ℓ = 5–8 nm` at the hard ceiling
   and 5–7 at the nominal**, against `C-0028`'s 6–10 and recommended 7–9. Both ends move,
   and the two windows overlap only at 7 nm.
3. **On the axis the routing leaves free, nothing passes anywhere.** `T4` is the whole design
   evaluated with the base's own `13.53 pN·nm/rad`, and its margin runs 0.29–0.72× over the whole
   3–10 nm sweep. **This is `C-0028`'s `B1` row, and `C-0028` already recorded that it fails `P6`
   at every length.** What is new is that it is not an alternative to `B2` but a *simultaneous*
   fact about the same joint.

> **A column buckles about its softest axis.** The adopted `P6` reading is therefore the weak-axis
> one, and **`P6` fails at every length**: the standoff branch closes at §3's **desired** 10 nm
> stroke. At §3's **acceptable** 3 nm the duty is only **1.111 pN** and the standoff clears it
> even on the weak axis from `ℓ = 5` to 10 nm — margin 2.21× at 5 nm falling to 1.01× at 10 —
> failing below 5 nm on `P3` alone.
>
> The restrained-axis reading is available only if a **second element** restrains the free axis.
> That is a truss — `T-66` — and `C-0028` already priced what it costs: a triangulated head
> cannot sway, and **sway is the draw-in**, which is the whole reason the standoff exists.

---

## `E5`, the fallback — and the one letter that closes it

`C-0023` files `E5` as *"arm 4.11 nm = 12 bp … PASS on all four, and the most compact"*, on the
linear law `1/k = r²/(nk_θ) + r³/(cEI)`, i.e. `δ = rθ`. Two things are wrong with evaluating it
at §3's strokes, and **neither needs a constitutive law**.

**1. The tip cannot rise past the arm.** `δ = r sin θ < r`, asserted as a gate-2 test at four
forces up to 10⁶ pN. A 4.11 nm arm cannot deliver a 10 nm stroke.

**2. The arm is capped below the stroke by the placement condition itself.** The hinge and the
arm's own bending are in **series**, so the assembled stiffness never exceeds `n c EI/r³` and

&nbsp;&nbsp;&nbsp;&nbsp;**`r ≤ (c·n·EI/k_target)^(1/3)` = `(3 × 45 × 230/33.3333)^(1/3)` = 9.767 nm < 10 nm.**

Asserted as a gate-5 test, together with solved arms at 1, 2, 4, 8, 64 and 1024 crossovers all
landing below it. **`E5` on a cantilever arm cannot reach §3's desired stroke at any hinge count.**

| id | law | `n` | arm [nm] | bp | cap [nm] | secant | tangent(3) | `t/s` | θ(3) | θ(10) | tangent(10) | reaches 10 nm | verdict |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **`E5`** | `C-0023`'s **linear**, as filed | 1 | **4.111** | 12.1 | 9.77 | 33.333 | **33.333** | 1.000 | **46.9°** | — | — | **no** | **FAIL** |
| `E5x` | exact rotation, cantilever | 1 | 4.624 | 13.6 | 9.77 | 33.333 | **51.65** | **1.549** | 35.5° | — | — | no | FAIL `P3` **and** stroke |
| `E5x2` | " | 2 | 5.744 | 16.9 | 9.77 | 33.333 | 40.26 | 1.208 | 24.6° | — | — | no | FAIL |
| `E5x8` | " | 8 | 8.064 | 23.7 | 9.77 | 33.333 | 33.85 | 1.015 | 9.4° | — | — | no | FAIL |
| `E5g8` | exact, **guided arm** `c = 12` | 8 | 10.306 | 30.3 | **15.50** | 33.333 | 34.69 | 1.041 | 11.9° | 38.5° | 57.1 | **yes** | PASS at 3 nm, tangent past 40 at 10 |
| **`E5g16`** | **exact, guided arm** | **16** | **12.242** | **36.0** | **15.50** | **33.333** | **33.68** | **1.011** | **7.1°** | **23.2°** | **38.68** | **yes** | **PASS** |
| `E5g32` | exact, guided arm | 32 | 13.648 | 40.1 | 15.50 | 33.333 | 33.40 | 1.002 | 4.0° | 13.2° | 34.40 | yes | PASS |
| `E5b8` | exact, 6-helix **bundle** arm | 8 | 11.764 | 34.6 | 26.51 | 33.333 | 35.63 | 1.069 | 13.5° | 47.5° | 105.9 | yes | PASS at 3 nm, far past 40 at 10 |

> **`C-0023`'s own compliance ceiling is what catches it.** `E5` as filed reports `t/s = 1.000`
> and *"placement and stability are discharged by one number"*; solved exactly at its own working
> point it is **1.549**, and the tangent is **51.65 pN/nm against a 40 pN/nm ceiling** —
> `C-0023`'s element fails `C-0023`'s own predicate, by 1.29×, on geometry it did not linearise.
>
> **And the remedy is one letter in the cube root.** `c` is the arm's end-condition factor: 3 for
> a cantilever, 12 for a guided arm. That single change lifts the cap from 9.77 to **15.50 nm**
> and reopens the branch. **A stiffer arm does not**: `E5b8`'s 6-helix bundle lifts the cap to
> 26.5 nm but its tangent at the desired stroke is 105.9 pN/nm, 2.6× past the ceiling — because
> the bundle buys arm *stiffness*, and what the stroke needs is arm *length*, which the hinge
> count buys.

### The Gen-1 output coupling this task leaves standing

| | |
|---|---|
| **element** | **`E5g16` — a crossover-hinge flexure**, 45 of them on `C-0015`'s 3 × 15 grid |
| **arm** | **12.24 nm = 36 bp**, **guided at both ends** (`c = 12`), one duplex |
| **hinge** | **16 antiparallel crossovers**, `k_θ` = 13.53 pN·nm/rad each — the standard motif, between **parallel** helices, the only one in this programme's catalogue with a measured stiffness |
| **placement** | secant **33.3333 pN/nm** at §3's 3 nm, by construction |
| **compliance** | tangent **33.68 pN/nm** at 3 nm (`t/s` = 1.011) and **38.68 pN/nm** at 10 nm — **inside `C-0023`'s 40 pN/nm ceiling at both**, with 3.3 % to spare at the desired stroke |
| **rotation** | **7.1°** at the acceptable stroke, **23.2°** at the desired one — small enough that the exact and linear laws differ by 1.1 % at the working point |
| **per-path force** | **7.41 pN** static share at 10 nm and **2.04 pN** on a crossover's backbone bonds, against `C-0006`'s 10 pN unzip allowable — 4.9× of margin on the joint |
| **draw-in demand** | **0.095 nm** at 3 nm — 0.28 bp, against `C-0025`'s 1.90 bp for the flexure |
| **buckling** | **none. `E5` has no member in axial compression at all**: the arm is loaded transverse to its own axis, so `P6` is vacuous and the constraint that closed the standoff branch does not exist here |
| **base joint** | **none needed.** The arm lies **in** the sheet plane and hinges about a crossover — no 90° junction anywhere in the design |

---

## The literature, and the search strategy that makes the negative falsifiable

Every statement carries `read directly` / `abstract only` / `not found`, the URL fetched and a
verbatim quote. A delegated search was treated as a summary and **four load-bearing quotes were
re-fetched and re-verified by hand** in this task: Rothemund's SI, Hedley et al.'s phosphate
radius, Bosco et al.'s interphosphate distances, Pan et al.'s junction stiffness, and Benson et
al.'s Mg²⁺ junction angle.

**Corpus and strategy.** ~110 distinct queries across **EuropePMC** REST search and full text,
the **arXiv** API and **Crossref** works, in nine query families: four-arm/Holliday junction open
conformation and inter-arm angle; the gridiron four-arm motif; T-junction/T-motif; three-arm
junctions; wireframe and polyhedral vertex routing with its poly-T counts; out-of-plane elements
on a 2D sheet (pillar, post, standoff, strut, leg, hairpin label, "perpendicular to the origami");
explicit statements that the double-crossover motif requires parallel helices; B-DNA phosphate
radius and P–P distance; multi-arm junction stiffness measurements.

| question | answer | flag |
|---|---|---|
| **Is there a published routing that stands a duplex rigidly perpendicular to a single-layer sheet?** | **NOT FOUND.** Zero hits for `ABSTRACT:"DNA origami" AND ABSTRACT:"pillar"`, `…"vertical" AND "helix"`, `ABSTRACT:"DNA nanostructure" AND ABSTRACT:"out-of-plane"`, `"perpendicular to the plane of the origami"`, `TITLE:"DNA origami" AND TITLE:"stand"`, `ABSTRACT:"four-arm junction" AND ABSTRACT:"90"`. | **not found** |
| **What happened the one time a protruding duplex was tried on a flat sheet?** | It was flexible, and Rothemund says why in one clause: *"The duplex markers, because they are **attached to the origami by only one covalent bond**, appear to be flexible."* **This is this task's `R3` single-link routing, observed** — the closest published precedent, and a negative one. | **read directly**, re-verified verbatim here |
| **Is the four-arm junction's 90° planar cross available in this buffer?** | **NO.** *"In the unstacked conformation … the four DNA helices form a planar cross with right angles. This conformation is however mostly present in the absence of divalent cations. When divalent cations, like Mg²⁺, are present, the Holliday junction will tend to transition into its stacked conformation where the arms of the junctions form a 60° angle."* And in the open form *"each of the four arms can move flexibly about the central junction"*. | **read directly**, re-verified |
| **Does the classic motif require parallel helices?** | Stated in print: *"the path of the scaffold has been restricted by a double-crossover motif to form **parallel** helices."* A statement of a constraint, not an impossibility proof — and **no explicit statement that a rigid 90° duplex-duplex junction is impossible was found.** | **abstract only** (Crossref, verbatim) |
| **How does the literature route a strand through a large-angle vertex?** | Through **unpaired nucleotides**, and it says so: PERDIX connects *"staples in vertices with unpaired poly(T) loops"* at *"0.42 nm per unpaired nucleotide"* and expects *"the N-arm junctions to be relatively flexible … due to the unpaired nucleotides present in vertices"*; ATHENA uses *"unpaired scaffold nucleotides … to span the distance between the 3′ and 5′ end between incoming and outgoing edges"*. **That is this task's `P7` failing, in the literature's own words.** | **read directly** |
| **Any rotational stiffness for a multi-arm DNA junction?** | **One, and it is the only number of its kind:** *"a rotational stiffness of `k_twist = 135 pN nm rad⁻¹` of the scissor-like interhelical angle `J_twist`"*, at *"interhelical distance … 1.85 nm"* and *"a right-handed twist of 60°"*. **FITTED**, not measured: *"estimated empirically … using the equilibrium distribution of `J_twist` from MD simulations of an isolated four-way junction (PDB ID: 1DCW) … and cross-validated using published FRET measurements"*. | **read directly**, re-verified |
| **The two geometric constants the theorem rests on.** | *"Phosphates (red circles) sit at a radius of `a_DNA ≈ 10 Å`"*; and the step is a measured **pair**: *"C3-endo (interphosphate distance 0.6 nm) to C2-endo conformation (interphosphate distance 0.7 nm)"*. | **read directly**, both re-verified |
| **Is a duplex hung off a face by a short ssDNA extension perpendicular?** | **NO**, and reported as a systematic distortion for a 3 nt extension: *"the Bott-ext-PTO complex might not be exactly perpendicular to the origami plane, leading to mild systematic distortion of the pattern."* | **read directly** |
| **Does the gridiron put an arm OUT of the plane of the others?** | **NOT ESTABLISHED at primary-source level.** Han et al.'s full text is closed (Science 403s article and supplement; OpenAlex `closed`, no repository copy). Its abstract says only *"Deliberate distortion of the junctions from their most relaxed conformations ensures that a scaffold strand can traverse through individual vertices in multiple directions"*. A review describes the 90° as a rotation **between two stacked layers** — a review's words, flagged as such. | **abstract only** |

### The one load-bearing cross-check, and it lands

> **Pan et al.'s 135 pN·nm/rad is the only rotational constant anyone has produced for a multi-arm
> DNA junction, and it brackets this task's ceiling from above by 1.7–2.2×** — 135 against 78.24
> (hard) and 62.06 (nominal). It restrains a *different* degree of freedom (the scissor angle
> between two coaxially stacked arm pairs, not a couple across a chord) and is *fitted to MD*, so
> it is used **only** as an order check and never as an input. Asserted as a gate-5 test. That it
> lands **above** the ceiling is the right direction: a four-way junction has four arms sharing
> two crossover strands, i.e. more backbone continuity than a two-terminus butt joint.

---

## The five verification gates

Executed as **31 gate-named tests** in `src/test/kotlin/anchoring/PerpendicularJunctionTest.kt`;
**223 `anchoring` tests, 1046 in the suite, 0 failures**, and `tools/verify.sh` **BUILD
SUCCESSFUL** on its own isolated tree.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | the two softened-bond constants carry the duplex's own units and **two of each reproduce `C-0009`'s crossover constants exactly**; a base couple is a slide stiffness times a **squared** lever arm, so doubling the arm quadruples it and a zero arm gives zero couple; the azimuthal quantum is a turn over the base pairs in it, at both lattices; **the hinge-arm ceiling is a cube root of a rigidity over a stiffness** — eight times the rigidity doubles it and eight times `c` doubles it; unphysical arguments throw at five entry points | **PASS** |
| **2 — limiting cases** | the terminal chord is **0 at `Δ = 0` and exactly `2 r_P` at 180°**, and no groove angle whatever puts a terminus outside the duplex radius; the seat height is the sheet radius **on** a duplex, **falls** in the valley to its closed form, and is symmetric about both; the link-window residual is zero across `[0.60, 0.70]` and grows **both ways** outside it; unpaired-nucleotide counts are monotone over 100 gaps; **the exact rotation law reduces to `C-0023`'s linear one to 1e−4 at a thousandth of the arm** and its `smallRotationStiffness` equals `C-0023`'s `stiffness` identically; an infinitely stiff arm leaves the pure rotation law `kθ = F r cos θ` exactly; **`δ_hinge < r` at four forces up to 1e6 pN** | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the closure search moves by **< 0.05 nm** when both continuous grids are tripled (90 × 48 → 270 × 144) and returns the **identical** configuration — objective, azimuth *and* axial position, to the last bit — on a repeat call; the exact tangent matches a central difference to 1e−5 at three displacements; **the reaction inverts its own displacement to 1e−8** at four forces; the solved arm reproduces the mandate secant to 1e−8; **the result file was re-emitted through `tools/study.sh` twice and reported *"no result file changed"* both times** | **PASS** |
| **5 — literature and upstream** | `C-0028`'s `B1` (13.529 / 64.706), `B2` (261.168), `B2u` (27.059), its `P6` threshold ladder (68.83 against 68.8; 173.61 against 173.6), its `B2` design span (31.063 against 31.06) and tangent (36.508 against 36.51); `C-0025`'s `J5-8` clamped span and tangent to **4.7e−10**; `C-0023`'s `E5` arm (4.111 against 4.11), hinge compliance share (0.9254 against 0.925) and bond force (3.396 against 3.40); the SAXS 2.69 nm; **32 bp is exactly three turns of the square lattice** to 3.1e−4; **`C-0028`'s `B2` lever arm is 1.345× the phosphate radius**; the hinge-arm ceilings 9.7666 and 15.5005; **Pan et al.'s measured-order 135 pN·nm/rad brackets the ceiling from above by < 2.2×**. Worst departure over 21 reproductions, excluding the deliberate literature *comparison*: **1.1e−3** | **PASS** |

### Gate 3 — four things that are not restatements of the construction

1. **The base couple has an axis, and the orthogonal one is exactly a crossover.** The
   unfavourable reading is asserted to equal `2 k_bond,θ` and *independently* to equal
   `StandoffBase.crossovers(1)` in **both** constants — `C-0028`'s `B1` recovered from a
   completely different construction, because two softened bonds with no lever arm *are* a
   crossover's `k_θ`. Nothing in the derivation imposed that.
2. **A column buckles about its softest axis**, asserted as a strict inequality between the two
   eigenvalues at four standoff lengths — through `C-0028`'s own `standoffBucklingLoad`, not
   through a restatement of the base constants.
3. **The couple projects as `cos²`, is even in the misalignment, and loses less than a tenth over
   a base-pair quantum** — asserted at the quantum, at zero, at `π/2` and at ±0.3 rad.
4. **The exact rotation law is odd and its horizontal draw-in even**, asserted at three
   displacements — the sidedness `C-0023`'s whole `CH-0027` argument rests on, re-established
   under the nonlinear law rather than inherited.

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | a routing with **more than two** rigid links per base | **no** | an internal nick adds termini but they sit ≥ 1 bp above the base face; the count stands |
| 2 | the ceiling **not binding** | **no** | it binds at 8 nm on the hard reading and at 7.5 on the nominal, and `B2` is over it by 3.34× |
| 3 | **the reach failing** — no phase closing covalently | **no, and it is the shape of the answer** | both links close at 0.600 nm with **zero** unpaired nucleotides. The routing exists; the *arithmetic* fails |
| 4 | `C-0028`'s pipeline not reproducing | **no** | 21 reproductions, worst departure 1.1e−3, and `C-0025`'s design to 4.7e−10 |
| 5 | **`E5` passing the acceptance unchanged** | **no, it fails twice** | `t/s` = 1.549 at its own working point, and its arm is capped below §3's desired stroke at any hinge count |
| 6 | the literature showing a rigid perpendicular junction with a characterised base | **no** | not found over ~110 queries; and the one published attempt was flexible, *"attached … by only one covalent bond"* |

**A result that was not anticipated:** `R1`'s free optimum — two *independent* staples, with the
two sheet targets unconstrained — lands on **consecutive phosphates of one strand**, which is
exactly `R2`'s constraint. The best independent routing **is** the scaffold excursion, and the
search was not told to prefer it.

---

## Does `C-0028`'s verdict survive?

**Its model does, in full. Its recommended base does not, and its window closes at the desired stroke.**

| `C-0028` said | this claim finds |
|---|---|
| `B2` = two crossovers to adjacent sheet duplexes, `k_θ_base` = 261.2 pN·nm/rad | **not realisable.** It needs a 1.345 nm lever arm out of a 1.0 nm backbone radius; the ceiling is **78.24** — [`CH-0039`](../challenges/CH-0039-the-base-couple-needs-a-lever-arm-the-standoff-does-not-have.md) |
| *"the base's ORIENTATION is worth 9.65× and it decides the design"* | **strengthened, not overturned.** Orientation is not a *choice* between `B2` and `B2u`: two links lie on a chord, so both readings are simultaneously true of the same joint, and the free axis is the one that governs buckling |
| the window is `ℓ = 6–10 nm`, recommended **7–9** | **5–8 nm** on the restrained axis at the hard ceiling, **5–7** at the nominal — and **empty** on the adopted weak-axis reading |
| *"a single crossover meets the threshold at no length … two, favourable, at every one"* | **reproduced exactly**, and both statements survive; what changes is that the favourable pair is not buildable |
| *"whether a 90° routing exists at all … is upstream of every number in this claim"* | **answered: it does.** A scaffold excursion, out at bp `m` and back at `m+1`, both links covalent at 0.600 nm |
| `B4`, a nicked continuation at 90°, is **structurally unavailable** | **confirmed**, and generalised: it is not that a nick cannot turn, it is that a duplex end has only two termini and neither of them is on the standoff's axis |
| the base constants are *"a model of a joint nobody has made"* | **still true**, and now with a ceiling that no measurement can raise |
| `T-13` still closes | **untouched.** The coupling's sidedness and the placement condition are properties of the *element*, and `E5g16` discharges both — the exact law is asserted odd at negative argument here as in `C-0023` |

---

## Validity range

- **TRL 1–3. Nothing here is measured, and the geometry is not demonstrated either.**
- **The closure search tests a NECESSARY condition and never a sufficient one**: a phosphate pair
  inside the measured `[0.60, 0.70]` nm step with no van der Waals overlap. No backbone torsion
  angle is checked and no sequence is designed. A *"closes"* verdict is an **upper bound** on
  buildability; only a *"does not close"* verdict would be a proof of impossibility.
- **The counting theorem does not inherit that caveat.** A duplex has two backbones, so a duplex
  end has two strand termini, and no force field, lattice or sequence can add a third.
- **The backbone separation `Δ` is a convention as much as a measurement** and it is the one
  parameter the couple is sensitive to: 90 / 120 / 154 / 180° give 45.9 / 62.1 / 75.0 / 78.2
  pN·nm/rad. The 180° reading is carried as a bound no convention can move.
- **The adopted `P6` is the WEAK-axis reading**, because a column buckles about its softest axis.
  The restrained-axis reading is reported beside it and is available **only** if a second element
  restrains the free axis — the one-sided blunt-end contact does not, being 3.7× short in moment.
- **That contact is modelled as a rigid line of half-width `R` on a smooth cylinder.** A real
  blunt end on a grooved duplex is softer and narrower, so its reported capacity is an **upper**
  bound and its 30° rocking freedom a **lower** one.
- **`k_s` is `C-0020`'s DERIVED, unmeasured construction and the whole base couple is `2 k_s a²`,
  so the ceiling is linear in it.** Swept over `C-0020`'s four decades: at `k_s/2` the longest
  stabilisable standoff falls to 5.5 nm and at `k_s/8` to **none**. As in `C-0028`, verdicts move
  across it — `T-9`.
- **`k_θ` is `C-0009`'s CITED, FITTED constant**, swept over Chen et al.'s `α ∈ [0.6, 1.2]`: the
  ceiling runs 37.2–74.5 and the longest stabilisable standoff 5.5–8.0 nm. **A verdict moves
  across `α` too**, which `C-0025` and `C-0028` did not find.
- **`EI = 230 pN·nm²` is a CanDo MODEL INPUT**; `C-0028` records that Fields et al.'s measured
  buckling implies 25 % less, so every critical load here is the **optimistic** end.
- **`E5`'s exact law extrapolates a SMALL-ANGLE fitted hinge constant to 4–47°.** The
  **geometric** parts — `δ ≤ r`, and the arm cap `(c n EI/k)^(1/3)` — need no constitutive law and
  are what decide the verdict.
- **The arm-bending term is put in SERIES at the tip**, which is `C-0023`'s own composition; the
  arm's own rotation is not fed back into the hinge's moment arm.
- **A guided arm (`c = 12`) is asserted, not designed.** What holds `E5g16`'s far end against
  rotation is a second anchorage on the lever, and its own compliance is not modelled here.
  **This is the largest open item under the recommended design** — `T-70`.
- **The sheet's neighbouring duplexes are given the SAME helical phase as the seat duplex.**
  `C-0015` makes the phase a design variable with a 32 bp period; letting it be chosen could only
  make closure easier, so the covalent verdict is unaffected.
- **One flexure per load path and 45 attachments**, exactly as `C-0023`, `C-0025` and `C-0028`
  assume.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| phosphate radius in B-form DNA | **1.00 nm (10 Å)** | **CITED**, Hedley, Coshic, Aksimentiev & Kornyshev, *Phys. Rev. X* **14**:031042 (2024), **READ DIRECTLY and re-verified verbatim**; the 0.90 nm fibre reading carried as a bracket |
| intrastrand phosphodiester step | **0.60–0.70 nm** | **CITED, MEASURED**, Bosco, Camunas-Soler & Ritort, *NAR* **42**:2064 (2014), **READ DIRECTLY and re-verified**. A **window**, not a number |
| ssDNA contour per nucleotide | 0.65 nm, inextensible | **CITED, MEASURED**, via `C-0025` |
| duplex `EI` | 230 pN·nm² | **CITED, a CanDo MODEL INPUT** (Kim et al., *NAR* **40**:2862, 2012), **not a measurement** |
| duplex stretch modulus `S` | 1100 pN | **CITED, MEASURED**, Wang et al., *Biophys. J.* **72**:1335 (1997) |
| crossover hinge `k_θ = 2αB/(100a)` | 13.53 pN·nm/rad, `α ∈ [0.6, 1.2]` | **CITED, FITTED**, Chen et al., *JACS* **136**:6995 (2014) SI §S2, via `C-0009` |
| crossover in-plane `k_s = 2αS/(100a)` | 64.71 pN/nm | **DERIVED** (`C-0020`), **NOT measured**; swept four decades, **and verdicts move** |
| interhelical distance | 2.69 nm | **CITED, MEASURED** by SAXS, Fischer et al. (2016) |
| base pairs per turn | 10.67 square, 10.5 honeycomb | **CITED** |
| rise per base pair | 0.34 nm | **CITED**, Douglas et al. (2009) |
| four-way junction scissor stiffness | 135 pN·nm/rad | **CITED, FITTED to MD and cross-validated against FRET**, Pan et al., *Nat. Commun.* **5**:5578 (2014), **READ DIRECTLY and re-verified**. Used **only** as a cross-check |
| per-path allowables | 10 / 65 pN | **CITED** via `C-0006` |
| §3 targets | 100 pN, 3 nm, 10 nm, 40 × 40 nm, 2 mM | **CITED** |
| `C-0028`'s `B2`, threshold ladder and design | 261.17, 68.8, 31.06 nm, 36.51 pN/nm | **CITED**, and reproduced here as gate-5 tests |
| `C-0023`'s `E5` | arm 4.11 nm, 92.5 % hinge share, 3.40 pN | **CITED**, and reproduced here |

Everything else — the counting theorem, the chord, the couple ceiling, the seat heights, the
tilt freedom and contact capacity, the closure search and its routings, the azimuthal quantum and
its `cos²` cost, every span, tangent, duty, critical load and verdict, the exact rotation law and
the arm cap — is **derived here in code**, with `C-0028`'s, `C-0025`'s and `C-0023`'s pipelines
**re-run rather than tabulated**.

## Still open — named, not answered

1. **What holds `E5g16`'s guided arm.** `c = 12` is asserted; the second anchorage's own
   compliance is not modelled, and if it is soft the cap falls back toward the cantilever's
   9.77 nm and the branch closes again. **This is the largest open item under the recommended
   design.** `T-70`.
2. **Whether a truss restrains the standoff's free axis at an affordable cost in sway.** `T-66`,
   and this task sharpens it: the truss is now needed for **stability**, not only for rigidity.
3. **`k_s`**, on which the whole base couple rests and which now moves the verdict in *two*
   claims. `T-9`.
4. **A backbone-torsion check of the closed routing.** The gaps close; whether the dihedrals do
   is a coarse-grained question, and it can only make the answer worse. `T-71`.
5. **`E5`'s hinge constant at 20–25° of rotation.** Chen et al.'s fit is small-angle; the
   recommended design turns 23.2° at the desired stroke.

## Challenges

**Raises [`CH-0039`](../challenges/CH-0039-the-base-couple-needs-a-lever-arm-the-standoff-does-not-have.md)**
against `C-0028`'s recommended base and
**[`CH-0040`](../challenges/CH-0040-e5-is-a-small-rotation-law-at-47-degrees.md)** against
`C-0023`'s `E5`. **No number in either fails to reproduce** — 21 reproductions at ≤ 1.1e−3, and
`C-0025`'s design at 4.7e−10.

**None stands against this claim.** The three ways it would fail:

1. **A published demonstration of a rigid perpendicular duplex-to-sheet junction with a
   characterised base above 78 pN·nm/rad.** The negative here is bounded by open-access indexing,
   and one paper would close it.
2. **A second element restraining the standoff's free axis at no cost in sway.** That would
   restore the 5–8 nm window, and it is `T-66`'s to find or to refuse.
3. **A demonstration that a guided end is not available on the lever.** Then `E5g16` falls back
   to the 9.77 nm cantilever cap and the programme has **no** element reaching §3's desired
   stroke — at which point the honest answer to `A8.2` is that 10 nm is out of reach and 3 nm is
   not.
