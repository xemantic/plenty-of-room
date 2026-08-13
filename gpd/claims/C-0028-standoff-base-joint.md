# C-0028 — The standoff's base is not a clamp, it is not in the literature at all, and modelling it re-cuts `C-0025`'s window with a different pair of constraints: the compliance ceiling stops binding and buckling starts

| | |
|---|---|
| **Task** | [`T-40`](../tasks/T-40-standoff-base-joint.md) |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the joint belongs to |
| **Verification type** | **in-silico** (`C-0025`'s partial-restraint machinery applied one level down, with a two-spring sway-column buckling eigenvalue solved rather than an effective-length factor assumed) **+ logical** (the standoff's sway and the flexure's draw-in are one coordinate, so the two design requirements it has to meet are in direct opposition) **+ literature** (a primary-source search for the motif, with a `read directly` / `abstract only` / `not found` flag on every statement) |
| **Verdict** | **PASS with the design amended, and `C-0025`'s window survives on a base it did not specify.** All three of `C-0025`'s standoff constants are the `ρ_b → ∞` limit of a series with the base, and at a crossover base `ρ_b = 0.18–0.59` — so a single crossover leaves **32.0 %** of the assumed rotational restraint, **13.6 %** of the sway stiffness and **32.0 %** of the transverse support. The base moves the design in **opposite** directions: a softer base releases more draw-in, the membrane term collapses and the tangent **falls inside the 40 pN/nm ceiling at every length** — while the Euler load collapses **faster than the duty**, and **a single-crossover base buckles at every one of `C-0025`'s eight lengths.** The pinned limit is not a smaller number but a **mechanism**, `P_c = 0` exactly. What closes it is the base's **orientation**: two crossovers to adjacent duplexes give **261.2 pN·nm/rad** laid across the flexure and **27.06** laid along it — 9.65× for free, and the difference between a window and none. **The replacement design is a two-crossover favourable base at `ℓ = 7–9 nm`, span 31.06 nm = 91 bp at 8 nm, tangent 36.51 pN/nm, buckling margin 1.41× free-head and 1.99× at the beam's realised head restraint.** And the finding of the first rank: **the motif is not established in the published literature at all** — every out-of-plane element on an origami body is held by a **pin**, and the only rigid out-of-plane mounting in print is **triangulated**, not clamped. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED — and unlike the rest of this programme, the *geometry* is not demonstrated either.** No duplex has been built standing normal to a single-layer sheet, and the base constants are a model of a joint nobody has made. |
| **Provenance** | `gpd/results/T-40-standoff-base-joint.json`, produced by `anchoring.StandoffBaseJointStudyKt`; **8 base-motif records, 4 buckling corners, 58 design records, 13 base-sweep records, 8 thresholds, 8 duty corrections, 9 off-diagonal records, 11 sensitivity records, 8 convergence records, 18 upstream reproductions, 5 literature records**; **27 gate-named tests in `StandoffBaseJointTest`, 162 in `anchoring`, 958 in the suite, 0 failures**; the result file re-run through `tools/study.sh` and diffed **byte-for-byte identical** (*"no result file changed"*) |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40 × 40 nm tile; 45 load paths on `C-0015`'s 3 × 15 grid; §3's 100 pN at the **acceptable** 3 nm — which is also `C-0019`/`C-0023`'s **held** gap `L₀ − 3 nm` — and at the **desired** 10 nm; joint allowables at Strunz's own 100 pN/s |
| **Consumes** | [`C-0025`](C-0025-flexure-end-joint.md) (`c(ρ)`, `g(β)`, `S_eff`, `FlexureEndJoint`, `PartiallyRestrainedFlexure`, `flexureSpanForJoint`, `bondedLengthForTension`, the five predicates and the 7–10 nm window — **re-run as a library**, its `J5-8` design and both buckling loads reproduced to ≤ 1.2e−9), [`C-0023`](C-0023-two-sided-coupling.md) (the flexure, the 40 pN/nm ceiling, the 45 paths, the *ends held axially* reading), [`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile` (`k_θ`, `k_s`, `EI`, `S`, the rise, the SAXS interhelical distance), [`C-0014`](C-0014-lateral-confinement.md) (`eulerBucklingLoad`, `compressedTransverseStiffness`, `k(P) = k₀(1 − P/P_c)`, `FreelyJointedChain`), [`C-0024`](C-0024-attachment-entry-topology.md)/[`CH-0029`](../challenges/CH-0029-the-48-pn-allowable-is-a-30-bp-number.md) (the length-dependent allowable), [`C-0017`](C-0017-output-coupling-stiffness.md) (the mandate, the envelope), [`C-0006`](C-0006-tile-load-distribution-and-flatness.md) (10 / 65 pN) |
| **Raises** | [`CH-0037`](../challenges/CH-0037-the-buckling-duty-is-the-mandate-not-the-element.md) and [`CH-0038`](../challenges/CH-0038-a-standoff-grounded-at-infinity.md), both against `C-0025` |

---

## The claim, in one line

**`C-0025`'s three standoff constants are all the rigid-base limit of a series it did not write, and once the base is modelled the design question inverts: a compliant base is GOOD for the compliance ceiling that closed `C-0025`'s window from below and FATAL to the buckling stability nobody was checking, because the standoff's sway and the flexure's draw-in are literally the same coordinate — so the element cannot be made to release the one without being made to buckle in the other; what rescues it is not a stiffer material or a shorter standoff but the ORIENTATION of a two-crossover base, worth 9.65× at no cost; and the whole construction rests on a motif that, on a primary-source search, does not appear in the published literature at all.**

---

## The three cheap bounds, which ran first and decided the shape of the answer

| | bound | value | what it settled |
|---|---|---|---|
| **1** | `ρ_b = k_θ_base ℓ/EI` at `C-0009`'s crossover constant, on a 3–10 nm standoff | **`ρ_b = 0.176–0.588`** | the base is **not** a clamp: a single crossover delivers 32.0 % of the rotational restraint `C-0025` assumed |
| **2** | the Euler load in the **pinned** limit | **exactly `0`** | `P_c` is **not** bracketed by `C-0025`'s 8.87 and 35.5 pN. A pinned base with a free head is a **mechanism**, and the bracket runs to zero |
| **3** | the support series, `1/(ℓ/S + 1/k_z)` at 8 nm | **44.0 against 137.5** | the base takes **68 %** of the support compliance; `C-0025`'s number is 3.1× optimistic before anything is solved |

Only because the first two said *"neither a clamp nor negligible, and the pinned limit is a mechanism"* was the eigenvalue sweep worth running — declared as falsifier 2 in the task file.

---

## The model: `C-0025`'s own machinery, one level down

A cantilever of rigidity `EI` and length `ℓ` on a base rotational spring `k_θ_base`, with `ρ_b = k_θ_base ℓ/EI`:

&nbsp;&nbsp;&nbsp;&nbsp;**`k_θ_head = (EI/ℓ)·ρ_b/(ρ_b + 1)`**, &nbsp; **`k_sway = (3EI/ℓ³)·ρ_b/(ρ_b + 3)`**, &nbsp; **`k_⊥ = 1/(ℓ/S + 1/k_z_base)`**,

each exactly `C-0025`'s constant at `ρ_b → ∞` and each asserted as such. The buckling load is no longer a `K` factor but an eigenvalue of a **two-spring sway column**:

&nbsp;&nbsp;&nbsp;&nbsp;**`D(u) = sin u·(u² − ρ_b ρ_h) − cos u·(ρ_b + ρ_h)·u = 0`**, &nbsp; first root in `(0, π)`, &nbsp; `P_c = u²EI/ℓ²`,

whose **four corners are the four textbook effective-length factors**, and one of them is not a strut:

| `ρ_b` | `ρ_h` | `u` | `K` | `P_c` at 8 nm | |
|---|---|---|---|---|---|
| ∞ | 0 | `π/2` | 2 | **8.867 pN** | `C-0025`'s *"pinned head"* — reproduced to 4.6e−10 |
| ∞ | ∞ | `π` | 1 | **35.469 pN** | `C-0025`'s *"guided"* — reproduced to 4.6e−10 |
| 0 | ∞ | `π/2` | 2 | 8.867 pN | pinned base, guided head |
| **0** | **0** | **0** | ∞ | **0 exactly** | **a MECHANISM. Not in `C-0025`'s 4× bracket at all** |

The determinant is **symmetric in the two springs** — Maxwell-Betti for a uniform strut — which is asserted as a gate-3 test rather than built in, and both one-spring textbook equations `u tan u = ρ` and `u cot u = −ρ` fall out of it as limits.

### And the head condition is not free either — the sway IS the draw-in

> **The standoff's head translation in the flexure's plane is the column's *sway* and the flexure's *draw-in* under two names.**
> So the head cannot be held against sway without being held against draw-in, and holding it against draw-in is exactly `C-0023`'s *ends held axially* reading, whose **91.13 pN/nm** tangent is what the whole of `T-30` was spent escaping.
> **The held-head reading is therefore not available to this design at all**, and the bracket runs from a free head to a guided one and no further.

The beam's own end rotational stiffness `2EI/L` puts the *realised* reading at `ρ_h = 0.515` at the design point — nearer the free end, which is why the **free-head reading is adopted as the predicate** and the realised one is reported beside it.

---

## The base catalogue: eight motifs, and the one that decides it is an orientation

| | motif | `k_θ_base` [pN·nm/rad] | `k_z_base` [pN/nm] | `ρ_b` at 8 nm | dead band | of `C-0025`'s `k_θ` | of its sway | of its support |
|---|---|---|---|---|---|---|---|---|
| **`B0`** | ideal clamp (`C-0025`'s assumption) | ∞ | ∞ | ∞ | 0 | 1.000 | 1.000 | 1.000 |
| **`B1`** | **one antiparallel crossover** | **13.53** | 64.71 | **0.471** | 0 | **0.320** | **0.136** | **0.320** |
| `B2u` | two crossovers, **unfavourable** orientation | **27.06** | 129.41 | 0.941 | 0 | 0.485 | 0.239 | 0.485 |
| **`B2`** | two crossovers, **favourable** orientation | **261.17** | 129.41 | **9.084** | 0 | **0.901** | **0.752** | 0.485 |
| `B3` | three crossovers, favourable | 977.03 | 194.12 | 33.98 | 0 | 0.971 | 0.919 | 0.585 |
| `B4` | nicked / scaffold continuation | — | — | — | — | **STRUCTURALLY UNAVAILABLE** | | |
| `B5-2` | 2 nt poly-T junction | 3.35 | 4.55 | 0.116 | **1.30 nm** | 0.104 | 0.037 | **0.032** |
| `B5-10` | 10 nt poly-T junction | 0.67 | 0.91 | 0.023 | **6.50 nm** | 0.023 | 0.008 | 0.007 |

### The three structural statements the catalogue makes

> **1. The base's ORIENTATION is worth 9.65× and it decides the design.** Two crossovers to adjacent duplexes react a base moment as a **couple**, `k_s Σd_i²`, and a couple has an **axis**: it restrains rotation about the line perpendicular to the crossover row and does **nothing** about the line along it. So the same two staples, with the same 129.4 pN/nm of axial support, give **261.2** pN·nm/rad laid *across* the flexure and **27.06** laid *along* it — the couple `k_s d²/2 = 234.1` is the whole of the first and absent from the second. `B2` passes at four lengths; `B2u` passes nowhere but one knife-edge. **The same lesson as `C-0014`'s "an anchor's orientation decides everything and its material almost nothing", in a new place.**
>
> **2. A nicked continuation cannot turn a corner.** `C-0025` establishes that a single nick is a clamp and a double nick is a crossover — and **both are statements about a duplex continuing along its OWN axis.** A nick preserves the helix axis, so there is no B-form geometry in which a duplex continues through a nick at 90° to itself. **The stiffest joint in `C-0025`'s catalogue is unavailable as a base**, which removes the obvious way to build one.
>
> **3. A flexible base fails exactly the way `CH-0031`'s flexible hinge did.** A 2 nt poly-T base is the softest in the catalogue and therefore the best draw-in release — and it is isotropic, so it hands the standoff's **own support path** the same softness: **4.41 pN/nm** against the 7.407 that ten times the beam's per-path stiffness demands, plus a 1.30 nm dead band. It fails `P1` at every length. `C-0014`'s convexity theorem, third appearance.

---

## The design table, and the two directions the base moves it in

At 45 paths, secant placed at 33.3333 pN/nm over 3 nm by construction. Duty and margin at §3's **desired** 10 nm, on the **element's own** end shear and the **free-head** critical load.

| base | `ℓ` | span [nm] | bp | `c` | `S_eff/S` | **tangent** | `T(10)` | duty(10) | `P_c` free | **margin** | `s_buckle` | verdict |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **`B0`** | 7 | 32.50 | 96 | 100.9 | 0.0289 | **39.03** | 5.53 | 6.277 | 11.58 | **1.85** | 14.15 | PASS |
| `B0` | 8 | 31.64 | 93 | 95.6 | 0.0190 | 37.39 | 3.83 | 5.518 | 8.87 | 1.61 | 13.42 | PASS |
| `B0` | 10 | 30.44 | 90 | 87.7 | 0.0095 | 35.59 | 2.04 | 4.697 | 5.68 | **1.21** | 11.46 | PASS |
| **`B1`** | 3 | 28.68 | 84 | 69.9 | — | **38.48** | 4.38 | 5.914 | **4.26** | **0.72** | **8.14** | **FAIL P6** |
| `B1` | 8 | 27.38 | 81 | 65.3 | — | **34.07** | 0.59 | 4.012 | **1.46** | **0.36** | **3.90** | **FAIL P6** |
| `B1` | 10 | 27.16 | 80 | 64.1 | — | **33.80** | 0.37 | 3.897 | **1.12** | **0.29** | **3.04** | **FAIL P6** |
| `B2u` | 8 | 28.56 | 84 | 73.6 | — | 34.53 | 1.01 | 4.215 | 2.54 | **0.60** | 6.49 | **FAIL P6** |
| **`B2`** | 5 | 33.96 | 100 | 108.4 | 0.0528 | **42.51** | 9.32 | 7.914 | 16.50 | 2.09 | 14.50 | fail P3 |
| **`B2`** | **6** | **32.67** | **96** | **101.6** | 0.0319 | **39.53** | 6.05 | 6.509 | 12.03 | **1.85** | 14.07 | **PASS** |
| **`B2`** | **7** | **31.76** | **93** | **96.2** | 0.0206 | **37.69** | 4.13 | 5.655 | 9.16 | **1.62** | 13.41 | **PASS** |
| **`B2`** | **8** | **31.06** | **91** | **91.8** | **0.0141** | **36.51** | **2.94** | **5.113** | **7.21** | **1.41** | **12.55** | **PASS** |
| **`B2`** | **9** | **30.51** | **90** | **88.1** | 0.0100 | **35.72** | 2.16 | 4.753 | 5.82 | **1.22** | 11.55 | **PASS** |
| `B2` | 10 | 30.05 | 88 | 85.0 | 0.0074 | 35.17 | 1.64 | 4.506 | 4.80 | **1.065** | 10.49 | PASS, nominally |
| `B3` | 8 | 31.47 | 93 | 94.6 | 0.0125 | 37.11 | 3.54 | 5.387 | 8.37 | 1.55 | 13.21 | PASS |
| `B5-2` | 8 | 25.59 | 75 | 53.8 | — | 33.56 | 0.17 | 3.797 | 0.40 | 0.11 | **1.09** | **FAIL P1** |

Three things fall out, and none was assumed.

1. **The base moves compliance and stability in OPPOSITE directions, and this was the prediction written into the task file before the code ran.** A softer base releases more draw-in, so the membrane term collapses and the tangent **falls**: at a single crossover it is **33.80–38.48 pN/nm over the whole 3–10 nm range**, inside the ceiling at *every* length, where the clamped base fails it below 7 nm. The same softness collapses the Euler load faster than the duty, so `P6` fails at every length. **The window is not narrowed or widened by the base: it is re-cut by a different pair of constraints, and the binding one is buckling.**
2. **`C-0025`'s window survives — on a base it did not specify.** `B2` passes at **6–10 nm** and `B3` at 7–10, but `B1` and `B2u` pass essentially nowhere. The recommended design is `B2` at **7–9 nm**, whose margin stays above 1.22×; the 10 nm end is retained only nominally at 1.065×.
3. **`B5`'s failure mode is `P1`, not `P6`.** A poly-T base is the best draw-in release available and it never gets to be judged on stability, because it cannot support the standoff in the first place — the same ordering `CH-0031` found one level up.

### The threshold, which is the deliverable

Inverting `P6`: **what base rotational stiffness does a standoff of length `ℓ` need for its critical load to reach its own duty at the desired stroke?**

| `ℓ` [nm] | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |
|---|---|---|---|---|---|---|---|---|
| **`k_θ_base` required** [pN·nm/rad] | 23.1 | 27.0 | 32.8 | 40.6 | 51.7 | **68.8** | 99.5 | **173.6** |
| in **crossover equivalents** | 1.71 | 2.00 | 2.42 | 3.00 | 3.82 | **5.09** | 7.36 | **12.83** |
| met by **one** crossover (13.53)? | no | no | no | no | no | no | no | no |
| met by **two, favourable** (261.2)? | yes | yes | yes | yes | yes | yes | yes | yes |

> **A single crossover meets it at no length in `C-0017`'s envelope. Two in the favourable orientation meet it at every one.** The requirement rises with `ℓ` because `P_c ∝ 1/ℓ²` while the duty barely moves — so a *longer* standoff is not the conservative choice it looks like.

### The continuous sweep, at the design length

| `k_θ_base` in crossovers | 0.25 | 0.5 | **1** | **2** | 4 | **8** | 16 | 64 | 512 | ∞ |
|---|---|---|---|---|---|---|---|---|---|---|
| span [nm] | 25.60 | 26.34 | 27.38 | 28.56 | 29.63 | 30.43 | 30.96 | 31.45 | 31.62 | **31.64** |
| tangent [pN/nm] | 33.57 | 33.76 | 34.07 | 34.53 | 35.13 | 35.79 | 36.37 | 37.07 | 37.35 | **37.39** |
| `P_c` free [pN] | 0.41 | 0.78 | 1.46 | 2.54 | 4.03 | 5.62 | 6.93 | 8.31 | 8.79 | **8.87** |
| **margin** | 0.11 | 0.20 | 0.36 | 0.60 | 0.90 | **1.17** | 1.37 | 1.55 | 1.60 | **1.607** |
| `P3` / `P6` | ✓ / ✗ | ✓ / ✗ | ✓ / ✗ | ✓ / ✗ | ✓ / ✗ | ✓ / **✓** | ✓ / ✓ | ✓ / ✓ | ✓ / ✓ | ✓ / ✓ |

**`P3` passes at every point of the sweep and `P6` crosses between 4 and 8 crossover equivalents.** The compliance ceiling is not a constraint on the base at all; stability is the only one.

---

## The nominal design that results

| | |
|---|---|
| **element** | transverse duplex flexure, tile tied at midspan, 45 of them on `C-0015`'s 3 × 15 grid |
| **span** | **31.06 nm = 91 bp** (was 31.64 nm = 93 bp) |
| **end joint** | a duplex standing normal to the sheet, **8.0 nm = 24 bp** |
| **base joint** | **two antiparallel crossovers to the two adjacent sheet duplexes, the pair laid ACROSS the flexure axis** — `k_θ_base` = 261.2 pN·nm/rad, `k_z_base` = 129.4 pN/nm |
| **base restraint realised** | `ρ_b = 9.08` — **90.1 %** of a clamp in rotation, **75.2 %** in sway |
| **end condition realised** | `ρ = 3.498`, **`c = 91.81`** (was 95.64) |
| **axial restraint realised** | `S_eff/S = 0.0141` — 1.4 % of the held reading |
| **compliance** | tangent **36.51 pN/nm**, `t/s` = 1.095 — inside `C-0023`'s ceiling with **9 %** to spare |
| **beam axial tension** | **0.29 pN** at 3 nm, **2.94 pN** at 10 nm — 29 % of the 10 pN unzip allowable |
| **transverse support** | **66.7 pN/nm** against the beam's 0.741 — **90×** (was 186×), no dead band |
| **standoff duty** | **1.111 pN** at the held point (3 nm, exact by placement), **5.113 pN** at 10 nm |
| **buckling** | `P_c` = **7.21 pN** free-head (**1.41×**), **10.19 pN** at the beam's realised `ρ_h = 0.515` (**1.99×**), 28.96 pN guided |
| **buckling stroke** | the standoff buckles at a **12.55 nm** stroke — 1.26× past §3's desired 10 nm |
| **bonded length the joint needs** (`CH-0029`) | **3.9 bp** — the joint is not the constraint |

---

## The literature, which is the finding of the first rank

Every statement below was fetched and read; the flag is per finding, per `CLAUDE.md`'s research practice, and a delegated search was treated as a summary and spot-checked.

| question | answer | flag |
|---|---|---|
| **Is a duplex standing normal to a single-layer origami sheet an established motif?** | **NOT FOUND.** Out-of-plane duplexes on such sheets exist only as **hairpin or staple-extension overhangs attached at a single point**: Rothemund's own dumbbell hairpins give *"greater height contrast (3 nm above the mica) than unlabelled staples"*, i.e. ~1.5 nm, and the oxDNA study of duplex overhangs finds they *"behave more like wider stiff rods"* that lie over the sheet strongly enough to curve the whole tile. Perpendicular helices in origami are perpendicular **within** the plane (gridiron four-arm junctions) or between stacked layers; the gridiron paper states outright that the standard motif *"has been restricted by a double-crossover motif to form parallel helices"*. Six independent zero-hit phrase searches across EuropePMC full text and arXiv. | **read directly** (Rothemund 2006 PDF; arXiv:2302.09109v3), **abstract only** (Han et al. *Science* **339**:1412 and Hong et al. *Angew Chem* **55**, both verbatim from Crossref) |
| **What holds such a base in practice?** | **A PIN, in every published instance.** Marras et al.'s origami revolute joints are *"joined along an edge by flexible ssDNA scaffold connections"*, *"2 nt in all cases"*, and the paper says plainly *"the hinge axes are not ideally constrained"*. Lauback et al.'s rotor is mounted *"via a single base-pairing interaction flanked by two ssDNA bases on either side for rotational flexibility"*. Kopperger et al.'s six-helix arm is connected to its plate *"via flexible single-stranded scaffold crossovers"*. Wireframe vertices use *"3 ssnts"* or *"5 ssnts"*. **Where an out-of-plane element IS held rigidly, it is TRIANGULATED**: Pumm et al.'s inclined plates *"were held rigidly at this angle with a **set** of double-helical spacers"*. **No publication describes a rotationally stiff, clamped base for a single duplex leaving a sheet.** | **read directly** (Marras *PNAS* **112**:713; Lauback *Nat Commun* **9**:1446; Pumm *Nature* **607**:492; Madhvacharyula *Nat Commun* **16**), **abstract only** (Kopperger *Science* **359**:296, verbatim from Crossref) |
| **Does a measurement of such a joint's stiffness exist?** | For an **in-plane** origami hinge, yes: *"a stiffness (slope in Fig. 3F) of 25 pN-nm/rad that increases to 45 pN-nm/rad"*, from *"918 structures in TEM images"*, for **six** 2 nt ssDNA connections between 18-helix bundles. **For an element normal to a plate: NOT FOUND** — Marras report only that *"a few hinges were observed in the perpendicular orientation … and revealed there was little out-of-plane rotation"*, with no number. | **read directly** |
| **A short duplex under axial compression?** | **YES, and at exactly this length scale.** Fields, Meyer & Cohen: *"short dsDNA strands (<41 base pairs) resisted this force and remained straight; longer strands became bent"*, under the measured 9 pN A-T unzipping load, *"in good agreement with the buckling length predicted by linear elasticity"*. **NOT FOUND:** any measurement of a single duplex under 20 nm in axial compression inside an origami. | **read directly** |

### Two of these are load-bearing cross-checks, and both land

> **Fields et al.'s measurement validates this task's buckling model on a duplex of exactly this length.** Inverting Euler on their own 40.5 bp at 9 pN gives `EI = 172.9 pN·nm²`, i.e. a persistence length of **41.7 nm** — **inside the 40–47 nm measured band** and **25 % below CanDo's 55.5 nm model input**, exactly the direction `CLAUDE.md` records. So **every buckling load here is computed on the optimistic rigidity**; scaling the critical load alone takes the `B2` design's margin at 10 nm from 1.065 to **0.80 — below one**. That is the second independent reason to read the window as **7–9 nm**.
>
> **Marras et al.'s measurement is the only one `C-0025`'s ssDNA-hinge constant has ever had.** Their 25 pN·nm/rad over six 2 nt connections is **4.17 per connection**, against `C-0025`'s modelled **3.345** for the same 2 nt — agreement to **25 %**, and in the conservative direction. Asserted as a gate-5 test.

> **And the literature reverses the standing of the pinned base.** A pin is not a pessimistic corner invented for a sensitivity study: **it is the only base condition the published literature actually demonstrates**, and this task shows a pinned base with a free head is a *mechanism*. The remedy the literature demonstrates is likewise not a stiffer joint but a **truss** — which is what the two-crossover couple is, in miniature, and what a proper triangulated standoff would be at full size (`T-65`).

---

## The five verification gates

Executed as **27 gate-named tests** in `src/test/kotlin/anchoring/StandoffBaseJointTest.kt`; **162 `anchoring` tests, 958 in the suite, 0 failures**, on an isolated full-tree run (`tools/verify.sh`'s own snapshot was blocked by a concurrent agent's half-written `window` test, so the same snapshot was taken with that single **file** removed, per `CLAUDE.md`).

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | `ρ_b = k_θ_base ℓ/EI` is dimensionless and **linear in the standoff length** (so the same base is nearer a pin under a long standoff); a buckling load is `EI` over a squared length and **halving the length quadruples it exactly**; `CH-0037`'s arithmetic — the mandate duty is `33.3333 × 10/45/2 = 3.7037` pN identically, while the element's own end shear at 10 nm exceeds it at every window length by ≥ 1.26×, **and the two coincide exactly at 3 nm by the placement condition**, which is why the error is invisible there; unphysical arguments throw | **PASS** |
| **2 — limiting cases** | **`C-0025`'s three standoff constants are reproduced as the `ρ_b → ∞` limit** (`EI/ℓ = 28.75`, `3EI/ℓ³`, `S/ℓ`, and the anisotropy `Sℓ²/3EI`); a **pinned** base leaves exactly zero rotational restraint and exactly zero sway stiffness while the transverse support survives; **all four textbook `K` factors exactly** — `π/2`, `π`, `π/2` and **`u = 0`, the mechanism**; the general determinant **reduces to both one-spring equations**, `u tan u = ρ` and `u cot u = −ρ`, at five restraints each; a series stiffness is its softer member's limit at both ends; the two base reductions are **monotone in `ρ_b` and bounded above by the clamp** over six decades | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the eigenvalue root is **exactly scan-independent** over 64 → 2048 steps (departure `0.00e+00` at every level) and exits on the **bracket width**; it satisfies its own determinant to `1e−9` at four `(ρ_b, ρ_h)` pairs; the solved span is exactly scan-independent over 64 → 1024; **the buckling stroke round-trips through the duty it was solved from** to `1e−9`; the threshold is bracketed by the two motifs on either side of it and **rises with the standoff length**; **the result file is byte-identical on two independent `tools/study.sh` runs** | **PASS** |
| **5 — literature cross-check** | `C-0025`'s `J5-8` design reproduced — span **31.6403748**, `c` **95.6390226**, tangent **37.3911226**, `T(10)` **3.82799407**, support **137.5** — and its **eight window rows** at four lengths, all to ≤ 1.2e−9; both its buckling loads (**8.8672227 / 35.4688908**) reproduced to 4.6e−10, and **independently against `C-0014`'s own `eulerBucklingLoad` to 0.00e+00**; `Gen1Tile`'s `k_θ` and `k_s` (13.5294, 64.7059) and the SAXS `d = 2.69 nm`; `CH-0029`'s 47.107 pN at 30 bp; `C-0025`'s `J3-2` hinge constants; the off-diagonal correlation **`√3/2` to 1.3e−16** and its factor **exactly 4** at a clamped base; **Fields et al.'s measured buckling recovers a persistence length of 41.7 nm, inside the 40–47 nm measured band**; **Marras et al.'s measured hinge brackets the modelled 2 nt constant within 25 %, from above**. Worst departure over 18 reproductions, excluding the two deliberate literature *comparisons*: **1.1e−3** | **PASS** |

### Gate 3 — four things that are not restatements of the construction

1. **The buckling determinant is symmetric under exchanging the two end springs** — Maxwell-Betti for a uniform strut, asserted at three asymmetric pairs. It cannot matter which end of the column carries which spring, and nothing in the derivation imposed that.
2. **A column at its own critical load has exactly zero sway stiffness**, `C-0014`'s `k(P) = k₀(1 − P/P_c)`, asserted through *this* task's eigenvalue rather than through the `K`-factor form `C-0014` was written with.
3. **The standoff's sway stiffness and the flexure's draw-in release return the same number**, asserted directly — the identity that makes the held-head reading unavailable.
4. **The base moves compliance and stability in opposite directions**, asserted as an inequality pair: a softer base gives a strictly *lower* tangent **and** a strictly *lower* buckling margin, and at 3 nm the clamp fails `P3` where the crossover passes it. That is the task's own pre-registered prediction, tested rather than narrated.

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | the model failing to reproduce `C-0025` at `ρ_b → ∞` | **no** | its design, its eight window rows and both buckling loads to ≤ 1.2e−9 |
| 2 | every base landing at `ρ_b ≫ 1`, i.e. the clamp being a good approximation | **no** | `ρ_b = 0.023–34`, and the buildable ones at **0.12–9.1** |
| 3 | **no base passing all six predicates at any length** | **very nearly, and it is the result** | only `B2` and `B3` pass, and only above 6 and 7 nm. `B1`, `B2u` and both `B5` pass nowhere |
| 4 | the base making no difference | **no** | span 25.6 → 31.6 nm and the buckling margin 0.11 → 1.61 across the sweep |
| 5 | buckling never binding | **no** | it is **the** binding constraint, and the only one the base can fail |
| 6 | the literature showing the motif is standard and its base measured | **fired in the opposite direction** | the motif is **not established at all**, and every published base is a pin |

**A result that was not anticipated:** the sensitivity to `k_s` **changes a verdict for the first time in this programme**. `C-0025` records that *"no verdict moves across `k_s`'s four decades"*; here the two-crossover base's whole couple **is** `k_s d²/2`, so at `k_s/32` and `k_s/8` the `B2` design's buckling margin falls to **0.70 and 0.92** and `P6` fails. The unmeasured constant has become verdict-critical, which sharpens the case for `T-9` considerably. Chen et al.'s `α` bracket, by contrast, moves the margin only 1.29 → 1.44 and no verdict with it.

---

## Does `C-0025`'s verdict survive?

**Yes, with its base specified, its window trimmed at both ends and two of its numbers corrected.**

| `C-0025` said | this claim finds |
|---|---|
| `k_θ = EI/ℓ`, `k_a = 3EI/ℓ³`, `k_⊥ = S/ℓ` | all three are the **rigid-base limit of a series**; at a crossover base 32.0 / 13.6 / 32.0 % of them survive — [`CH-0038`](../challenges/CH-0038-a-standoff-grounded-at-infinity.md) |
| buckling 8.87 pN pinned-head / 35.5 pN guided, a 4× bracket | reproduced **exactly**, and the bracket is **not** 4×: the pinned-base free-head corner is a **mechanism**, `P_c = 0` |
| duty 3.70 pN at the desired stroke, margins 1.53–3.13× | the duty is the **mandate**, not the element; the element's own is 1.27–1.70× larger and the margins are **1.21–1.85×** — [`CH-0037`](../challenges/CH-0037-the-buckling-duty-is-the-mandate-not-the-element.md) |
| the window is 7–10 nm, closed below by the compliance ceiling and above by `C-0017`'s envelope | **both edges move.** The ceiling stops binding for any base softer than a clamp; the binding constraint below is now `P3` on the *stiff* bases only, and above it is **buckling**, not the envelope. The window is **6–10 nm** on `B2` and the recommended one **7–9 nm** |
| transverse support 137.5 pN/nm, 186× the beam | **66.7 pN/nm, 90×** with the base in series — still ample, and `P1` is never the constraint for a covalent base |
| the base joint is one of `J1`–`J4` | `J2`/`J2b`'s continuation is **structurally unavailable** at 90°; `J1` alone (one crossover) **buckles at every length**; what works is a **two-crossover couple in the favourable orientation**, which is not in `C-0025`'s list |
| span 31.64 nm = 93 bp, tangent 37.39 pN/nm | **31.06 nm = 91 bp, tangent 36.51 pN/nm** — a 1.8 % and 2.4 % move, both inside `C-0023`'s brackets |
| `T-13` still closes | **untouched.** The law is still odd at every restraint and the placement condition is met by construction, so `C-0023`'s two-sidedness argument and zero-bias verdict are inherited unchanged |

---

## Validity range

- **TRL 1–3. Nothing here is measured — and unlike the rest of this programme, the *geometry* is not demonstrated either.** The motif is not in the published literature, so the base constants are a model of a joint nobody has built, composed from Chen et al.'s softened bond.
- **The two joint springs are still treated as INDEPENDENT**, exactly as in `C-0025`. The off-diagonal is **bounded** here — correlation `√3/2 = 0.866` at a clamped base, 0.947 at a crossover base, and the other-displacement-fixed reading exceeds the other-load-zero one by **exactly 4** and by 9.70 — and **argued** to soften the joint, which makes `P3` conservative and **`P6` not conservative**. It is `C-0025`'s open question 1 and it is **not closed**; `T-65` owns it.
- **The head restraint is a bracket, not a number**: free (adopted, conservative), the beam's own `2EI/L` (`ρ_h = 0.515`, margin 1.99× at the design) and `6EI/L`, and guided. **The held-head reading is not available**, because the sway is the draw-in.
- **The standoff's head deflection at the desired stroke is 38–51 % of its own length** over `ℓ = 7–10 nm` and 58–96 % below it — far past small deflection, so every 10 nm column is a **lower bound** on the tension, exactly as `C-0023` and `C-0025` flag, and the short end of the sweep is not quantitative.
- **`EI = 230 pN·nm²` is a CanDo model input and Fields et al.'s measured buckling implies 25 % less.** Every buckling load here is therefore the **optimistic** end; on the measured rigidity the 10 nm margin falls below one.
- **`k_s` is `C-0020`'s derived, unmeasured construction, and for the first time in this programme it moves a verdict** — the two-crossover couple is entirely `k_s d²/2`, and `P6` fails at `k_s/8` and below. `C-0025`'s *"no verdict moves across `k_s`"* does **not** transfer here.
- **`k_θ = 2αB/(100a)` is `C-0009`'s cited, fitted constant**, swept over Chen et al.'s own `α ∈ [0.6, 1.2]`; no verdict moves across it (margin 1.29–1.44).
- **`B3` asks a 2 nm duplex to span a 5.38 nm footprint** and is reported as a bound rather than recommended.
- **The sheet beyond the base crossovers is treated as rigid.** `C-0009`'s grillage says it is not, and a compliant sheet can only lower `k_θ_base` further.
- **`B2u`'s single pass at 4 nm is a knife-edge** (margin 1.0015×) and is not a design.
- **One flexure per load path and 45 attachments**, exactly as `C-0023` and `C-0025` assume. The flexure array is `T-31`'s and the lever `T-33`'s.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| duplex `EI` | 230 pN·nm² | **CITED, a CanDo MODEL INPUT** (Kim et al., *NAR* **40**:2862, 2012), **not a measurement**; Fields et al.'s measured buckling implies **172.9** |
| duplex stretch modulus `S` | 1100 pN | **CITED, MEASURED**, Wang et al., *Biophys. J.* **72**:1335 (1997) |
| crossover hinge `k_θ = 2αB/(100a)` | 13.53 pN·nm/rad, `α ∈ [0.6, 1.2]` | **CITED, FITTED**, Chen et al., *JACS* **136**:6995 (2014) SI §S2, via `C-0009` |
| crossover in-plane `k_s = 2αS/(100a)` | 64.71 pN/nm | **DERIVED** (`C-0020`), **NOT measured**; swept four decades, **and a verdict moves** |
| interhelical distance | 2.69 nm | **CITED, MEASURED** by SAXS, Fischer et al. (2016) |
| origami hinge stiffness | 25 / 45 / 70 pN·nm/rad | **CITED, MEASURED** by TEM angular distribution over 918 structures, Marras et al., *PNAS* **112**:713 (2015), **READ DIRECTLY**. Used only as a **cross-check**, never as an input |
| duplex buckling at 40–41 bp under 9 pN | — | **CITED, MEASURED**, Fields, Meyer & Cohen, *NAR* **41**:9881 (2013), **READ DIRECTLY**. Used only as a **cross-check**, never as an input |
| ssDNA Kuhn length, contour per nucleotide | 2.10 nm, 0.65 nm | **CITED, MEASURED** (Chen et al., *PNAS* **109**:799, 2012), via `C-0025` |
| the shear allowable's constants | Strunz's four | **CITED, MEASURED**, *PNAS* **96**:11277 (1999), via `C-0024`/`CH-0029` |
| per-path allowables | 10 / 65 pN | **CITED** via `C-0006` |
| §3 targets | 100 pN, 3 nm, 10 nm, 40 × 40 nm, 2 mM | **CITED** |
| `C-0025`'s `J5-8` design and both buckling loads | 31.64 nm, 95.64, 37.39 pN/nm, 8.87 / 35.5 pN | **CITED**, and reproduced here as gate-5 tests |

Everything else — the three series reductions, the sway-column determinant and its four corners, every base motif's constants, every span, tangent, duty, critical load, buckling stroke, threshold and off-diagonal factor — is **derived here in code**, with `C-0025`'s pipeline **re-run rather than tabulated**.

## Still open — named, not answered

1. **The fully coupled 2 × 2 tip compliance of the standoff.** Bounded here, argued to soften the joint, not closed — and because it softens, it is the one unmodelled term that could take `P6` back below one. `T-65`.
2. **Whether a 90° scaffold or staple routing between a sheet duplex and a normal standoff exists at all.** The literature has no instance and a nicked continuation cannot supply it. This is upstream of every number in this claim.
3. **Whether the design should abandon the single-duplex standoff for a TRIANGULATED one** — the only rigid out-of-plane mounting the literature actually shows. It would remove the buckling problem and cost the draw-in release the standoff exists for. `T-66`.
4. **`k_s`**, which the two-crossover couple rests on entirely and which now moves a verdict. `T-9`.
5. **Whether the sheet itself can react the base moment.** Treated as rigid beyond the base crossovers; `C-0009`'s grillage says it is not, and any compliance there lowers `k_θ_base`.

## Challenges

**Raises [`CH-0037`](../challenges/CH-0037-the-buckling-duty-is-the-mandate-not-the-element.md)** (the buckling duty at the desired stroke is read on the mandate secant, not on the element's own reaction) and **[`CH-0038`](../challenges/CH-0038-a-standoff-grounded-at-infinity.md)** (all three of the standoff's stiffnesses are quoted with the base grounded at infinity), both against `C-0025`. **No number in `C-0025` fails to reproduce** — its design, its eight window rows and both its buckling loads are recovered here to ≤ 1.2e−9 — and its design lands inside its own brackets after amendment.

**None stands against this claim.** The three ways it would fail:

1. **A published demonstration of a duplex standing normal to a single-layer sheet with a characterised base.** It would replace the whole modelled bracket with a number, and it is the best possible outcome. The negative here is a strong absence, not a proof: EuropePMC full-text indexing covers open access only.
2. **A measurement of `k_s` far below `C-0020`'s construction.** At `k_s/8` the two-crossover couple no longer carries the standoff and `P6` fails everywhere — the first place in this programme where that constant decides an answer.
3. **A solved coupled joint showing the off-diagonal softens the standoff by more than ~1.4× in the sway direction.** That alone would take the recommended design's margin below one, without any other number moving.
