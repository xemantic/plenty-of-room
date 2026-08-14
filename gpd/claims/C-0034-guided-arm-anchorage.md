# C-0034 — `c = 12` does not survive its own anchorage, and the cap does not need it to: `c(ρ) = 12(1+ρ)/(4+ρ)` is a continuum whose restraint parameter carries the ARM, so the cap is a fixed point that **every two-link anchorage** puts above §3's desired stroke and **every one-link anchorage** puts below it

| | |
|---|---|
| **Task** | [`T-70`](../tasks/T-70-guided-arm-anchorage.md) |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the joint belongs to |
| **Verification type** | **logical** (`C-0029`'s counting theorem applied at the arm's far end — a duplex end has two strand termini and no force field can add a third) **+ in-silico** (the arm's own boundary-value problem solved in closed form at one and at two springs, and `C-0029`'s `RotatingHingeArm` pipeline re-run as a library on the realised end factor) |
| **Verdict** | **PASS, and the assertion fails while the design survives.** `c = 12` is **not** realised: at the design point the arm's end-condition factor is **7.36** (large-rotation series reading) or **6.28** (boundary-value reading), never 12. But the *cap* survives, and for a reason `C-0029` could not see from a formula evaluated at an asserted `c`: **`ρ_far = k_far r/EI` carries the ARM**, so the cap is the fixed point `r = (c(k_far r/EI)·n·EI/k)^(1/3)` and a longer arm buys its own restraint. **Every two-link anchorage clears §3's desired 10 nm stroke — 13.43 nm at the duplex-end couple, 15.18 at a singly nicked continuation, 15.44 at a two-crossover clamp, and 10.97 nm even about the CHORD, the axis `C-0029`'s counting theorem leaves free. A one-link anchorage — Rothemund's own observed failure — collapses to the cantilever's 9.77 nm and fails.** So what decides §3's desired stroke is a **link count at the far end**, not a motif's material. The design that results is **`E5a16`: an 11.03–12.50 nm = 32–37 bp arm on 16 crossovers, its far end the arm's own duplex end**, secant 33.3333 pN/nm, tangent 33.56 at 3 nm and 36.78 at 10 nm inside `C-0023`'s ceiling, 1.83 pN on a hinge crossover, and 7.3 bp of bonded length demanded at its own anchorage. **And `c = 12` and `C-0023`'s series composition cannot both be right** — [`CH-0044`](../challenges/CH-0044-c-equals-twelve-and-the-series-composition-cannot-both-be-right.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED**, no anchorage has been built, and no routing here is a sequence design. |
| **Provenance** | `gpd/results/T-70-guided-arm-anchorage.json`, produced by `anchoring.GuidedArmAnchorageStudyKt`; **10 cheap bounds, 13 continuum records, 7 anchorages, 21 designs, 24 placements, 5 series records, 4 axis records, 21 sensitivity records, 9 convergence records, 17 upstream reproductions**; **31 gate-named tests in `GuidedArmAnchorageTest`**, the **whole suite** green on `tools/verify.sh` — **BUILD SUCCESSFUL**, 0 failures, on its own isolated tree, with only a concurrent agent's mid-TDD `crossover/ConcentratedCrossover{,Test}.kt` dropped by `--drop-file`; the result file re-run through `tools/study.sh` and reported *"no result file changed"* |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40 × 40 nm tile; 45 load paths on `C-0015`'s 3 × 15 grid; §3's 100 pN at the **acceptable** 3 nm and the **desired** 10 nm; single-layer **square-lattice** Rothemund sheet |
| **Consumes** | [`C-0029`](C-0029-perpendicular-junction-routing.md) (`RotatingHingeArm`, `hingeArmCeiling`, `rotatingArmForStiffness`, `maximumBaseRotationalStiffness`, `bondHingeStiffness`, `bondSlideStiffness`, `couplePhaseProjection`, `DuplexBackbone`, `BForm` — **re-run as a library**), [`C-0025`](C-0025-flexure-end-joint.md) (`FlexureEndJoint.nickedContinuation`, `.multiCrossoverClamp`, `midspanFactor`, `bondedLengthForTension`, and the *continuum* discipline this claim repeats on a different end pair), [`C-0023`](C-0023-two-sided-coupling.md) (`CrossoverHingeFlexure`, the series composition, the 40 pN/nm ceiling, the 45 paths), [`CH-0029`](../challenges/CH-0029-the-48-pn-allowable-is-a-30-bp-number.md) (`ShearJointAllowable`, **inverted**), [`C-0015`](C-0015-crossover-phase-and-registration.md), [`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile`, [`C-0006`](C-0006-tile-load-distribution-and-flatness.md), [`C-0020`](C-0020-in-plane-shear-lag.md) |
| **Raises** | [`CH-0044`](../challenges/CH-0044-c-equals-twelve-and-the-series-composition-cannot-both-be-right.md) against `C-0029`'s `E5g` composition |

---

## The claim, in one line

**`C-0029` asked one letter of a cube root to close the programme's only surviving design and did not model what supplies it; the answer is that the letter is never 12 and never needed to be, because the restraint parameter of a beam end carries the beam's own LENGTH — so the cap is not a formula but a fixed point, a longer arm buys its own guidance, and the whole question reduces to a COUNT: two links at the far end put the cap above §3's desired stroke on every axis and at every reading, one link puts it below, and the one-link case is the only published attempt and it was observed to be floppy.**

---

## The three cheap bounds, which ran first and decided the shape of the answer

| | bound | value | what it settled |
|---|---|---|---|
| **1** | **`ρ_far` at the design arm**: `EI/r` at 12 nm against `C-0029`'s own two-terminus ceiling | `19.17` pN·nm/rad against **78.24**, so **`ρ_far = 4.08`** and **`c = 7.55`** | **neither** textbook value applies. Had `ρ_far` come out at 0.01 or 100 the continuum would have collapsed onto an end and the task would have closed in a paragraph — declared in the task file as falsifier 2 |
| **2** | **the cap at the two worst readings**, as a fixed point | **10.97 nm** about the chord, **9.77 nm** at one link | the verdict is decided by the **link count**, not by the motif. Only because bound 2 put the chord reading *above* 10 nm was the full design sweep worth running |
| **3** | **the series identity**, one line of algebra | the composition is exact **iff `ρ_far = 0`** | `c = 12` and `C-0023`'s composition cannot both be true — [`CH-0044`](../challenges/CH-0044-c-equals-twelve-and-the-series-composition-cannot-both-be-right.md), settled before any root was found |

---

## The continuum, derived rather than chosen

A beam of length `r` and rigidity `EI`, clamped in bending at the hinge end (the hinge's rigid
rotation is `C-0023`'s separate series term), carrying a transverse tip force `F` and a rotational
spring `k_far` at the tip. Superposing the cantilever tip-load and tip-moment solutions and imposing
`M = −k_far θ_B`:

&nbsp;&nbsp;&nbsp;&nbsp;`θ_B = FL²/(2EI) + ML/EI`, &nbsp; `δ = FL³/(3EI) + ML²/(2EI)`
&nbsp;&nbsp;→&nbsp;&nbsp; **`c(ρ) = 12(1 + ρ)/(4 + ρ)`, &nbsp; `ρ ≡ k_far r/EI`.**

| `ρ` | 0 | 0.25 | 0.5 | 1 | **2** | **3.75** | 4 | 8 | 16 | 37.1 | 64 | 256 | 1024 | ∞ |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **`c`** | **3.000** | 3.529 | 4.000 | 4.800 | **6.000** | **7.352** | 7.500 | 9.000 | 10.200 | 11.124 | 11.471 | 11.862 | 11.965 | **12.000** |

> **`c(0) = 3` is the cantilever, `c(∞) = 12` the guided arm, and `c(2) = 6` exactly** — the two
> textbook values are a factor of exactly four apart, as `C-0025`'s 48/192 are, and for the same
> reason. All three are asserted as gate-2 tests, together with strict monotonicity over eleven
> values.

### And the whole two-spring beam, which contains both joints

Condensing both end rotations out of the Euler-Bernoulli element stiffness matrix, for two bodies
translating relative to each other without rotating:

&nbsp;&nbsp;&nbsp;&nbsp;**`c(ρ_n, ρ_f) = 12(ρ_nρ_f + ρ_n + ρ_f)/(ρ_nρ_f + 4ρ_n + 4ρ_f + 12)`**

whose **four corners are the four textbook cases and none was assumed**: `(∞,∞) = 12` guided,
`(∞,0) = (0,∞) = 3` cantilever in **both** orders, and `(0,0) = 0` — a **mechanism**, not a weaker
beam, which is `C-0028`'s pinned-base sway column in a new place. It is symmetric in its two
arguments; the *design* is not, because the hinge must rotate and the anchorage must not.

---

## What actually holds the arm: `C-0029`'s counting theorem, at the other end

`E5`'s arm is one duplex, so **its far end is a duplex END** — and `C-0029`'s theorem transfers
verbatim: two backbones, therefore two strand termini, therefore **at most two covalent links**,
whose separation is the terminal chord and whose lever arm is bounded by the phosphate radius.

| | anchorage | links | `k` favourable | `k` chord | `ρ` at 12 nm | `c` at 12 nm | **cap [nm]** | clears 10 nm |
|---|---|---|---|---|---|---|---|---|
| **`A0`** | *`C-0029`'s asserted ideal guide* | — | ∞ | ∞ | ∞ | 12.000 | **15.503** | yes — **not a motif** |
| **`A1`** | **one covalent link — a ball joint** | **1** | **0** | 0 | 0 | **3.000** | **9.766** | **NO** |
| **`A2`** | **the arm's own duplex end, hard 180° chord** | **2** | **78.24** | **13.53** | **4.082** | **7.546** | **13.428** | **yes** |
| `A2n` | the same at the nominal 120° groove | 2 | 62.06 | 13.53 | 3.238 | 7.026 | 13.088 | yes |
| `A3` | doubly nicked continuation of a tile duplex | 2 | 78.24 | 13.53 | 4.082 | 7.546 | 13.428 | yes |
| **`A4`** | **singly nicked continuation, one backbone intact** | 3 | **683.2** | 683.2 | 35.65 | 11.092 | **15.181** | **yes** |
| `A5-2` | two-crossover clamp at the 32 bp pitch | 8 | 3856.8 | 3856.8 | 201.2 | 11.825 | 15.444 | yes |

Four things fall out and none was assumed.

1. **The cap is a fixed point, and that is why the answer is what it is.** `ρ = k r/EI` carries the
   **arm**, so a longer arm makes the *same* joint relatively stiffer — `C-0025`'s lesson, verbatim,
   in a place where it decides a pass/fail. `C-0029` evaluated `(c n EI/k)^(1/3)` at a constant `c`;
   the correct statement is `r = (c(k_far r/EI)·n·EI/k)^(1/3)`, and it is asserted as a gate-4 test
   that this reproduces both textbook caps exactly at its two ends and satisfies its own defining
   equation at four anchorages.
2. **The verdict is a COUNT, not a material.** Two links clear 10 nm at every reading; one link is
   exactly the cantilever, 9.767 nm, and fails. **`A1` is not hypothetical**: it is `C-0029`'s `R3`
   and it is the only out-of-plane element anyone has published on a flat sheet — *"the duplex
   markers, because they are **attached to the origami by only one covalent bond**, appear to be
   flexible."*
3. **A singly nicked continuation is the motif that matches the kinematics.** `C-0025`'s `J2`: one
   intact backbone is not a softened bond, so it carries the duplex's own `B/a` — and CLAUDE.md's
   own constraint that *"a nick preserves the helix axis"*, which closed the standoff's 90° base,
   **works in favour here**, because keeping the axis is exactly what *"guided"* means. The arm
   arrives collinear with the tile duplex it continues.
4. **A perfect guide is worth less than the counting theorem costs.** `A5-2`'s 3857 pN·nm/rad — 49×
   the two-terminus ceiling — buys only **15.44 nm** of cap against `A2`'s 13.43, because `c`
   saturates at 12. The cube root is doing the work in both directions.

---

## The axis — and it runs the OPPOSITE way to the standoff

Two links lie on a chord, so they restrain **one** axis: the couple `2k_bond,s a²` acts about the
chord's perpendicular bisector and about the chord itself only `2k_bond,θ` = 13.53 pN·nm/rad
survives. The arm's working bending is about the axis normal to the sheet plane, and the chord is a
**diameter of the arm's own cross-section**, so the designer *chooses* the axis with the helical
phase — quantised at `360°/10.67` = **33.74° per base pair**.

| chord orientation | misalignment | `cos²` | `k_eff` | of favourable | **cap [nm]** | clears 10 nm |
|---|---|---|---|---|---|---|
| **normal to the sheet, in phase** | 0° | 1.000 | **78.24** | 1.000 | **13.428** | yes |
| **half a base-pair quantum off (worst case)** | **16.87°** | **0.9158** | **72.79** | **0.930** | **13.324** | **yes** |
| a whole quantum off | 33.74° | 0.6915 | 58.27 | 0.745 | 12.992 | yes |
| **across the arm's bending axis (the free axis)** | 90° | 0.000 | **13.53** | 0.173 | **10.969** | **yes** |

> **The phase costs at most 7.0 % of the couple, and even the completely wrong azimuth clears the
> stroke.** This is the exact inverse of `C-0029`'s standoff verdict, and the reason is structural:
> **a column buckles about its softest axis, but `E5`'s arm carries no axial compression at all** —
> `C-0029` says so itself, *"`P6` is vacuous"* — so the axis the chord leaves free is unloaded. The
> same counting theorem that closed the standoff branch leaves this one open, and the difference is
> the load path, not the joint.

---

## The series question: the anchorage is in series, and the sign is not the one assumed

`C-0023`'s composition `1/k = r²/(n k_θ) + r³/(c EI)` charges the hinge the **whole** tip moment
`F r`. A guide carries part of it — `M_far = F r ρ_f/(2(1+ρ_f))` — so **a guide relieves the hinge**,
and the composition is exact at **exactly one corner**.

| far end at the design arm (11.03 nm, 16 crossovers) | `ρ_f` | `c` exact | `k` exact | `k` series | **retained** | `θ_A` | `θ_B` | `M_far` |
|---|---|---|---|---|---|---|---|---|
| **free — a ball joint** | 0 | 2.327 | 0.3991 | **0.3991** | **1.0000** | 3.49° | 21.63° | 0 |
| **the adopted duplex end** | 3.751 | 5.926 | 1.0161 | 0.7382 | **0.7264** | 5.00° | 10.77° | 14.71 |
| singly nicked continuation | 32.76 | 8.765 | 1.5029 | 0.9165 | 0.6098 | 6.20° | 2.21° | 26.31 |
| two-crossover clamp | 184.9 | 9.355 | 1.6041 | 0.9472 | 0.5905 | 6.44° | 0.43° | 28.73 |
| **`C-0029`'s asserted ideal guide, at its own 12.242 nm** | ∞ | **9.681** | **1.2135** | 0.7368 | **0.6072** | 5.43° | **0.00°** | 24.06 |

Three things fall out.

1. **The composition retains exactly 1.0000 at a free far end** — asserted to `1e−12` — **and
   strictly less at every restrained one.** `C-0023`'s `E5` at `c = 3` is therefore exact; `C-0029`'s
   `E5g` at `c = 12` is not, and the last row is [`CH-0044`](../challenges/CH-0044-c-equals-twelve-and-the-series-composition-cannot-both-be-right.md): **45 × 1.2135 = 54.61 pN/nm, 1.64× the mandate and past the 40 pN/nm ceiling at the secant.**
2. **The far anchorage rotates MORE than the hinge at the adopted design** — 10.77° against 5.00° —
   because 78.24 pN·nm/rad is softer than sixteen crossovers' 216.47. The "guide" is the *weaker* of
   the two joints, which is not what the word suggests and is asserted as a test.
3. **The two end moments and the applied shear balance identically**, `M_near + M_far = V·r`, at three
   arms and both §3 strokes, residual `0.0e+00` — a conservation check that nothing in the
   construction forced.

### And the two compositions bracket the arm

| reading | arm [nm] | bp | `c` realised | verdict |
|---|---|---|---|---|
| **`C-0023` series, exact rotation** (`C-0029`'s composition, this claim's realised `c`) | **11.028** | **32.4** | **7.356** | the **short** end |
| `C-0029`'s `E5g16` as filed (series, asserted `c = 12`) | 12.242 | 36.0 | *asserted* 12 | inside the bracket |
| **two-spring BVP, small deflection** (this claim's `c`, exact composition) | **12.496** | **36.8** | **6.284** | the **long** end |

> **Two errors run opposite ways and very nearly cancel.** The realised end condition is softer than
> asserted (6.28 against 12) *and* the composition it was solved with is the soft reading (0.607). In
> the BVP reading the placement length and the cap are the **same equation** — the hinge is one of the
> two springs, not a separate series term — and 12.496 nm is both. Every reading clears §3's desired
> 10 nm stroke and every reading sits below the ideal guide's 15.50 nm cap.

---

## The design that results

| | |
|---|---|
| **element** | **`E5a16` — a crossover-hinge flexure with a realised far anchorage**, 45 of them on `C-0015`'s 3 × 15 grid |
| **arm** | **11.03–12.50 nm = 32–37 bp**, one duplex, bracketed by the two compositions |
| **hinge (near end)** | **16 antiparallel crossovers**, `k_θ` = 13.53 pN·nm/rad each — unchanged from `C-0029` |
| **anchorage (far end)** | **the arm's own duplex end, two strand termini on a chord laid NORMAL to the sheet** — `k_far` = 78.24 pN·nm/rad, `ρ_f` = 3.75, **`c` = 6.28–7.36, not 12** |
| **cap** | **13.43 nm**, the fixed point on that anchorage — 1.07–1.22× above the placed arm |
| **placement** | secant **33.3333 pN/nm** at §3's 3 nm, by construction |
| **compliance** | tangent **33.56 pN/nm** at 3 nm (`t/s` = 1.007) and **36.78 pN/nm** at 10 nm — inside `C-0023`'s 40 pN/nm ceiling at both, with **8.1 %** to spare at the desired stroke |
| **rotation** | 6.4° at the acceptable stroke, **20.9°** at the desired one |
| **dominant compliance** (leaf `A8.2`'s explicit ask) | **the ARM, 58.5 %**, against the hinge's 41.5 % — `C-0023` reported 92.5 % hinge for its one-crossover `E5`, and at 16 crossovers with a realised anchorage **the dominant term has changed sides** |
| **hinge load** | **1.83 pN** on a crossover's backbone bonds at 10 nm, against `C-0006`'s 10 pN unzip allowable — 5.5× |
| **anchorage load** | couple **33.33 pN·nm** at 10 nm → **16.66 pN** per link, demanding **7.3 bp** of bonded length on `CH-0029`'s ladder, against a 68.1 pN saturation. Transverse support **64.71 pN/nm** against the element's own 0.741 — **87×**, no dead band |
| **buckling** | **none.** The arm is loaded transverse to its own axis, so `P6` is vacuous — `C-0029`'s statement, and it is what makes the free chord axis harmless |
| **base joint** | **none.** No 90° junction anywhere in the design |

### The acceptance predicates, declared before the run

| | predicate | outcome |
|---|---|---|
| **`P1`** | `c(ρ)` is a continuum with 3 and 12 as **exact** limits | **PASS** — exactly 3, exactly 12, exactly 6 at `ρ = 2`, monotone over eleven values |
| **`P2`** | the realised `c` is **strictly inside** `(3, 12)` | **PASS** — 6.28–7.36, and outside `[3.5, 11]` on neither side |
| **`P3`** | the cap on the realised `c` **exceeds 10 nm** | **PASS** — 13.43 nm adopted, 10.97 nm on the free axis, 13.32 nm at the worst phase |
| **`P4`** | a placed design exists with `r > 10 nm` and tangent inside 40 pN/nm at both strokes | **PASS at 16 and 32 crossovers; FAILS at 8** — an 8-crossover hinge places at 9.52 nm and cannot lift its tip 10 nm |
| **`P5`** | the anchorage restrains the axis the arm needs, at a bounded phase cost | **PASS** — the designer picks the azimuth; worst case costs 7.0 % |
| **`P6`** | the anchorage's own couple demands a bonded length inside `CH-0029`'s ladder | **PASS** — 7.3 bp at the desired stroke, 2.0 bp at the acceptable one |

---

## The five verification gates

Executed as **31 gate-named tests** in `src/test/kotlin/anchoring/GuidedArmAnchorageTest.kt`;
`tools/verify.sh` **BUILD SUCCESSFUL** on its own isolated tree.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | `ρ = kL/EI` is dimensionless and **linear in the span**, so the same joint doubles its restraint on a twice longer arm; the cap is a **cube root of a rigidity over a stiffness** — eight times `EI` doubles it and eight times the path count doubles it; unphysical arguments throw at eight entry points | **PASS** |
| **2 — limiting cases** | `c(0) = 3`, `c(∞) = 12`, `c(2) = 6` **exactly**, monotone over eleven values, ratio exactly 4; the two-spring factor at **all four textbook corners**, including `(0,0) = 0` — a mechanism; **symmetric** in its two arguments at four pairs; a rigid near end reduces it to `c(ρ_far)` at six restraints; a guided far end **does not rotate** and carries a moment, a free one rotates and carries none; the anchorage moment is `0` at a ball joint and `FL/2` at a guide, monotone between | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the cap **reproduces both textbook caps exactly at its ends** and satisfies its own fixed-point equation at four anchorages to `1e−9`; a completely independent **fixed-point iteration** converges to the bisected cap (`1.9e−8 → 2.4e−15` over 8 → 16 iterations); the placed arm reproduces its own target secant to `1e−7` at three anchorages and is **strictly below its own cap** at four; the two-spring placement solves its own equation to `1e−9` and equals its own cap identically; the moment balance holds to `0.0e+00` at both strokes; **the result file was re-emitted through `tools/study.sh` and reported *"no result file changed"*** | **PASS** |
| **5 — literature and upstream** | `C-0029`'s `E5g16` arm (12.2423721), tangents (33.6838074 / 38.6847197), rotation (23.1971251°), **both** hinge-arm ceilings (15.5029478 / 9.76624511), the two-terminus ceiling (78.2352941) and the chord reading (13.5294118 = `C-0028`'s `B1`); `C-0025`'s `J2` (683.2) and `J4-2` (3856.8) rebuilt through `FlexureEndJoint` itself, and its `c(0) = 48` pinned limit; `C-0009`'s crossover hinge constant; `C-0023`'s mandate; `CH-0029`'s 18.796 pN at 8 bp; and this task's own three exact limits. Worst departure over 17 reproductions, **excluding the three upstream values their own claims quote rounded** (683.0, 3857.9 and 18.796, which land at 3.5e−4, 2.8e−4 and 2.3e−5): **2.8e−9** | **PASS** |

### Gate 3 — four things that are not restatements of the construction

1. **The series composition is exact at `ρ_far = 0` and strictly soft everywhere else**, asserted to
   `1e−12`. Two independently derived expressions — a series sum and a condensed stiffness matrix —
   agree at one corner and disagree everywhere else, and **nothing in either derivation forced the
   agreement**. It is the whole content of `CH-0044`.
2. **`M_near + M_far = V·r` identically**, at three arms and both strokes, residual `0.0e+00` — the
   beam's own moment equilibrium, recovered from a solve that was written from displacements.
3. **The chord-axis reading equals `2 k_bond,θ` *and independently* equals one antiparallel
   crossover**, to the last digit — `C-0029`'s `B1` recovered at the *other* end of the element.
4. **The phase projection is even in the misalignment**, is exactly 1 in phase, and loses less than a
   tenth over a base-pair quantum — asserted at the quantum, at zero and at ±half a quantum.

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | `c(ρ)`'s limits not being 3 and 12 | **no** | exact at both, and exactly 6 at `ρ = 2` |
| 2 | `ρ_far` landing outside `[0.1, 100]` | **no** | 3.24–4.08 at the two-terminus anchorages, i.e. the interior |
| 3 | the two-spring `c` failing to reproduce the series composition at `ρ_far = 0` | **no, and it is the shape of the answer** | it reproduces it to `1e−12` there **and nowhere else** — which is `CH-0044` |
| 4 | `C-0029`'s `E5g16` numbers failing to reproduce | **no** | arm, both tangents, rotation and both ceilings to ≤ 2.8e−9 |
| 5 | **the realised cap falling below 10 nm** | **no, and the margin is a count** | 13.43 nm adopted and 10.97 nm on the *free* axis; it falls below 10 nm only at **one** link |

**A result that was not anticipated:** the *dominant compliance term* has changed sides. `C-0023`
reported 92.5 % hinge for `E5`; at 16 crossovers with a realised anchorage the **arm** carries 58.5 %.
Leaf `A8.2` asks for the dominant term by name, and it is not the one the element is named after.

---

## Does `C-0029`'s verdict survive?

**Yes — its recommendation survives, its composition does not, and its stated open item is closed.**

| `C-0029` said | this claim finds |
|---|---|
| *"A guided arm (`c = 12`) is **asserted, not designed**… this is the largest open item"* | **closed.** The realised factor is **6.28–7.36**, never 12 — and it does not need to be |
| `E5g16` is a 12.24 nm = 36 bp arm | **inside the corrected bracket 11.03–12.50 nm (32–37 bp)**, by accident of two cancelling errors |
| the cap is 15.50 nm at `c = 12` | that is the `k_far → ∞` limit of a **fixed point**; the realised cap is 13.43 nm, still 1.34× above the desired stroke |
| *"if it is soft the cap falls back toward the cantilever's 9.77 nm and the branch closes again"* | **it does — but only at ONE link.** Two links clear 10 nm on every axis, at every groove reading and at every phase |
| `E5g16` places at 33.3333 pN/nm | **not on its own `c = 12`** — 54.61 pN/nm on the BVP that `c` describes. [`CH-0044`](../challenges/CH-0044-c-equals-twelve-and-the-series-composition-cannot-both-be-right.md) |
| tangent 33.68 / 38.68 pN/nm inside the 40 pN/nm ceiling | **reproduced exactly**, and the realised design is *better*: 33.56 / 36.78 |
| the counting theorem, the closure search, `CH-0039`, `CH-0040`, the standoff verdict | **untouched**, and the theorem is **used** here rather than restated |
| *"a column buckles about its softest axis"* | **true of the standoff and vacuous for `E5`** — the arm carries no axial compression, so the free chord axis is unloaded. Same theorem, opposite consequence |

---

## Validity range

- **TRL 1–3. Nothing here is measured**, no anchorage has been built, and no routing here is a
  sequence design. Base pairs make the design statement concrete; they do not specify a staple.
- **The counting theorem is a count and inherits no modelling caveat**, but the *couple* it bounds is
  `2k_bond,θ + 2k_bond,s a²`, built on `C-0020`'s **derived, unmeasured** `k_s` and `C-0009`'s
  **cited, fitted** `k_θ`. **Verdicts move across both**: at Chen et al.'s `α = 0.6` the design's arm
  falls to 9.62 nm and **fails** the desired stroke, and at `k_s ≤ ×0.125` likewise. This is the same
  exposure `C-0029` records, in the same direction. `T-9`.
- **`c(ρ)` and the two-spring factor are SMALL-DEFLECTION** Euler-Bernoulli results, and the design
  turns 20.9° at the desired stroke. `C-0029`'s `RotatingHingeArm` supplies the large-rotation
  geometry but on the *series* composition; the exact composition supplies `ρ_f` but linearly. **The
  two bracket the arm at 11.03–12.50 nm and neither is the answer** — a large-rotation two-spring
  elastica is named as the first open item.
- **The hinge's rigid rotation and the arm's bending are composed at the tip**, exactly as `C-0023`
  and `C-0029` do; the arm's own rotation is not fed back into the hinge's moment arm.
- **`EI = 230 pN·nm²` is a CanDo MODEL INPUT, not a measurement.** At Fields et al.'s
  measured-buckling reading (−25 %) the arm falls to 10.48 nm — still above the stroke, with 4.8 % of
  margin instead of 10.3 %.
- **The backbone separation `Δ` is a convention**: 180° gives 78.24 pN·nm/rad and 120° gives 62.06,
  moving the cap 13.43 → 13.09 nm. **No verdict moves across it.** The narrow 0.90 nm fibre phosphate
  radius gives 65.94 and 13.18 nm — likewise.
- **The far anchorage's load is read from the LINEAR moment `F r ρ/(2(1+ρ))`**, which at 20.9° of
  rotation is the stiff reading; the `CH-0029` bonded length it demands is therefore an **upper**
  bound, and it is 7.3 bp against a ladder that saturates at 68.1 pN.
- **A hybridised staple domain inside a sheet is not a free oligonucleotide** (`C-0024`'s own limit),
  so the bonded length inherits `T-35`'s open question.
- **The anchorage's `k_θ` about the chord is `2 k_bond,θ` and about the bisector adds the couple; the
  two are simultaneously true of the same joint**, as `C-0029` established. Which one governs is
  decided by the load path, and here the free one carries nothing.
- **One flexure per load path and 45 attachments**, exactly as `C-0023`, `C-0025` and `C-0029` assume.
- **An 8-crossover hinge does not close.** The window in hinge count is **16–64**; below it the arm
  cannot reach the stroke and at `A0`/`A4`/`A5` the 8-crossover tangent is also past the ceiling.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| duplex `EI` | 230 pN·nm² | **CITED, a CanDo MODEL INPUT** (Kim et al., *NAR* **40**:2862, 2012), **not a measurement**; Fields et al.'s −25 % swept |
| duplex stretch modulus `S` | 1100 pN | **CITED, MEASURED**, Wang et al., *Biophys. J.* **72**:1335 (1997) |
| crossover hinge `k_θ = 2αB/(100a)` | 13.53 pN·nm/rad, `α ∈ [0.6, 1.2]` | **CITED, FITTED**, Chen et al., *JACS* **136**:6995 (2014) SI §S2, via `C-0009`; swept, **and a verdict moves** |
| crossover in-plane `k_s = 2αS/(100a)` | 64.71 pN/nm | **DERIVED** (`C-0020`), **NOT measured**; swept four decades, **and a verdict moves** |
| phosphate radius in B-form DNA | 1.00 nm | **CITED**, Hedley et al., *Phys. Rev. X* **14**:031042 (2024), via `C-0029` (**read directly** there) |
| backbone azimuthal separation | 120°/180° | **CONVENTION**, both carried, via `C-0029` |
| base pairs per turn, rise, crossover pitch | 10.67, 0.34 nm, 32 bp | **CITED** |
| the shear allowable's four constants | `x₀`, `x₁`, `α`, `β` | **CITED, MEASURED**, Strunz et al. (1999), via `C-0024`/`CH-0029` |
| per-path allowables | 10 / 65 pN | **CITED** via `C-0006` |
| §3 targets | 100 pN, 3 nm, 10 nm, 40 × 40 nm, 2 mM | **CITED** |
| `C-0029`'s `E5g16` and both ceilings | 12.242 nm, 15.503 / 9.766 nm | **CITED**, and reproduced here as gate-5 tests |

Everything else — `c(ρ)`, the two-spring factor and its four corners, the fixed-point cap, the
anchorage catalogue and its two axes, the anchorage moment and its link force, every arm, tangent,
rotation, compliance share and verdict — is **derived here in code**, with `C-0029`'s, `C-0025`'s and
`C-0023`'s pipelines **re-run rather than tabulated**.

## Still open — named, not answered

1. **A large-rotation two-spring elastica.** The two compositions bracket the arm at 11.03–12.50 nm
   and neither is exact: one has the rotation and the wrong composition, the other the composition and
   no rotation. **`T-79`.**
2. **`k_s` and `α`**, on which the anchorage couple rests and across which a verdict moves — a third
   claim now exposed to the same unmeasured constant. `T-9`.
3. **Whether the arm's far end can be a singly nicked continuation in practice**, which would take the
   cap to 15.18 nm and the realised `c` to 11.1. It is `C-0025`'s `J2` and it needs the arm collinear
   with a tile duplex at its far end — a routing question of exactly the kind `T-67` answered for the
   standoff, and not answered here.
4. **The anchorage's behaviour at 21° of rotation**, and whether Chen et al.'s small-angle fit
   survives it — `C-0029`'s open item 5, inherited unchanged.
5. **Whether 16 crossovers can be assembled into one hinge line on a 40 nm tile at all.** This claim
   takes `C-0029`'s hinge count as given and only prices what is at the *other* end.

## Challenges

**Raises [`CH-0044`](../challenges/CH-0044-c-equals-twelve-and-the-series-composition-cannot-both-be-right.md)**
against `C-0029`'s `E5g` composition. **No number in `C-0029` fails to reproduce** — its arm, both
tangents, its rotation, both ceilings, its two-terminus ceiling and its chord reading all land at
≤ 2.8e−9.

**None stands against this claim.** The three ways it would fail:

1. **A demonstration that the arm's far end presents only one covalent link.** Then the cap is 9.767
   nm and the programme has no element reaching §3's desired stroke — which is exactly `C-0029`'s own
   third failure mode, relocated from *"is a guide available"* to *"how many links does it have"*.
2. **A measurement putting `k_s` below `C-0020`'s ×0.125.** Then the two-terminus couple falls below
   22 pN·nm/rad and the arm no longer reaches the stroke.
3. **A large-rotation solve landing outside the 11.03–12.50 nm bracket**, which would mean neither
   composition brackets the truth and the placement has to be re-solved.
