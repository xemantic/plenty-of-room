# C-0025 — The flexure's end joint: the end condition is a continuum and every buildable joint sits in its upper three quarters, the axial bracket is decided by ANISOTROPY and not by softness, and the joint that works is a duplex standing on end

| | |
|---|---|
| **Task** | [`T-30`](../tasks/T-30-flexure-end-joint.md) |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the joint belongs to |
| **Verification type** | **in-silico** (a partial-restraint Euler-Bernoulli flexure whose two end brackets are the two limits of one two-parameter joint, with the span solved as a root at every candidate) **+ logical** (an anisotropy argument that decides which joints can exist before any of them is evaluated) |
| **Verdict** | **PASS, and `C-0023`'s verdict survives with its remedy replaced.** The end-condition bracket is not a choice of two: `c(ρ) = 192(ρ+2)/(ρ+8)`, exactly 48 and 192 at its limits, and **every joint that supports the beam lands at `c = 83.2–191.7`, the upper 76 % of it.** The axial bracket is decided not by how *soft* a joint is but by whether it is **anisotropic**: every covalent origami motif is isotropic, so it holds the beam as firmly as it supports it, and **`C-0023`'s restrained reading is the one that survives — the crossover joint gives a 79.18 pN/nm tangent, 1.98× past its own 40 pN/nm ceiling, at every one of `k_s`'s four decades.** `C-0023`'s proposed 2 nt single-stranded hinge does not close it: a flexible link has no direction, so it buys 1.30 nm of draw-in for **1.30 nm of transverse dead band**, 43 % of §3's stroke. **The joint that works is a duplex standing NORMAL to the sheet** — `S/ℓ` across, `3EI/ℓ³` along, anisotropy `Sℓ²/(3EI)` = 102× at 8 nm — giving **span 31.64 nm (93 bp), `c` = 95.6, tangent 37.39 pN/nm, and 0.37 / 3.83 pN of beam tension at §3's 3 and 10 nm strokes.** |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED.** No joint has been built and none is a sequence design; base pairs and nucleotides make the design statement concrete, they do not specify a staple. |
| **Provenance** | `gpd/results/T-30-flexure-end-joint.json`, produced by `anchoring.FlexureEndJointStudyKt`; **16 joint records, 10 continuum records, 24 design records, 14 sensitivity records, 8 standoff-window records, 11 convergence records, 17 upstream reproductions**; **26 gate-named tests in `FlexureEndJointTest`, 135 in `anchoring`, 931 in the suite, 0 failures** on `tools/verify.sh` and on an isolated full-tree run; the result file re-run through `tools/study.sh` and diffed **byte-for-byte identical** (*"no result file changed"*) |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂**; 40 × 40 nm tile; 45 load paths on `C-0015`'s 3 × 15 grid; §3's 100 pN at the **acceptable** 3 nm and the **desired** 10 nm; joint allowables at Strunz's own 100 pN/s |
| **Consumes** | [`C-0023`](C-0023-two-sided-coupling.md) (the flexure, the membrane term, the draw-in demand, the two brackets, the 40 pN/nm ceiling, the 45 paths — **re-run as a library**, its four spans and two tangents reproduced), [`C-0024`](C-0024-attachment-entry-topology.md)/[`CH-0029`](../challenges/CH-0029-the-48-pn-allowable-is-a-30-bp-number.md) (the shear allowable **as a function of bonded length**, re-run and **inverted**), [`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile` (`k_θ`, `k_s`, `EI`, `S`, the rise, the 32 bp pitch, the allowables), [`C-0014`](C-0014-lateral-confinement.md) (`FreelyJointedChain`, the Kuhn bracket, `cableTension`, `eulerBucklingLoad`, and the convexity theorem this claim generalises), [`C-0017`](C-0017-output-coupling-stiffness.md) (the mandate, the buildable envelope), [`C-0021`](C-0021-zero-bias-resting-position.md) (that the coupling has to be two-sided at all), [`C-0006`](C-0006-tile-load-distribution-and-flatness.md) (10 / 65 pN) |
| **Raises** | [`CH-0031`](../challenges/CH-0031-a-flexible-hinge-cannot-be-anisotropic.md) against `C-0023`'s hinge remedy |

---

## The claim, in one line

**`C-0023`'s two brackets are the two limits of one two-parameter joint, and once they are written that way the design question changes shape: the end-condition factor is a continuum `c(ρ) = 192(ρ+2)/(ρ+8)` in which every joint that can support a beam sits above 83, and the axial bracket is not a question of how soft a joint is but of whether it has a DIRECTION — every covalent origami motif is isotropic and therefore holds the beam exactly as firmly as it supports it, so `C-0023`'s *ends held axially* reading is the one that survives; the single-stranded hinge it proposed to escape with is isotropic too and pays its whole axial slack back as transverse dead band; and the joint that does work is the same escape `C-0023` used one level up, bending, applied to the joint instead of the element — a duplex standing normal to the sheet, which carries the end shear along its own axis and releases the draw-in by bending, at an anisotropy the designer sets with a length.**

---

## The three cheap bounds, which ran first and decided the shape of the answer

Three divisions and one paragraph, all before any root was found.

| | bound | value | what it settled |
|---|---|---|---|
| **1** | `ρ = k_θ L/EI` at `C-0009`'s fitted crossover constant, 13.53 pN·nm/rad, on a 25–50 nm span | **`ρ = 1.4–2.9`**, i.e. `c = 70–87` | the end condition is **neither** 48 nor 192, and the "exactly 4×" bracket is the interior of one function |
| **2** | `2S/(k_a L)` at `C-0020`'s in-plane construction, 64.7 pN/nm, on the same spans | **0.85–1.36** | the joint's axial compliance is **comparable to the beam's own**: neither of `C-0023`'s readings applies unmodified |
| **3** | the **anisotropy** bound — logical, and free | `k_transverse/k_axial = 1` for **any** flexible link | a joint has to be stiff **across** and soft **along**; an isotropic element cannot do both, so `C-0023`'s ssDNA remedy is decided before it is evaluated |

Only because the first two said *"neither end"* was a two-parameter root find worth running at all. Had `ρ`
come out at 0.01 or 100 the bracket would have collapsed onto one of its own ends and this task would have
closed on a division — which is declared in the task file as falsifier 2.

---

## The model: one joint, and `C-0023`'s two brackets as its two limits

A beam of span `L`, rigidity `EI`, loaded at midspan, with an equal rotational spring at each end.
Superposing the simply supported central-load solution with a pair of end moments and imposing `θ = M/k_θ`:

&nbsp;&nbsp;&nbsp;&nbsp;`M = P L ρ/(8(ρ+2))`, &nbsp; **`c(ρ) = 192(ρ+2)/(ρ+8)`**, &nbsp; `ρ ≡ k_θ L/EI`;
&nbsp;&nbsp; **`S_eff = S/(1 + 2S/(k_a L))`**.

`c(0) = 48`, `c(∞) = 192`, `c(4) = 96`; `S_eff(k_a → ∞) = S`, `S_eff(k_a → 0) = 0`. Both are asserted as
gate-2 tests, and the whole of `C-0023`'s `E3` law — odd, two-sided, cable geometry, analytic tangent — is
re-used unchanged with `S_eff` in place of `S`. **The partial model reproduces `C-0023`'s filed element
identically at all four of its corners**, at three spans × four displacements each, to `1e−9`.

### And the draw-in shape factor is not 2.4 in between

`C-0023` records `Δ = 2.4 δ²/L` for **both** end conditions and flags it as *"not obvious"*. Integrating
`Δ = ∫(1/2)w′²dx` over the *partially* restrained shape, with `β ≡ 3ρ/(ρ+2) ∈ [0,3]`:

&nbsp;&nbsp;&nbsp;&nbsp;`g(β) = (2.4 − 1.25β + β²/6)/(1 − β/4)²`

| `ρ` | 0 | 0.5 | 1 | 2 | 4 | **8** | 16 | 64 | 256 | ∞ |
|---|---|---|---|---|---|---|---|---|---|---|
| `c` | 48.00 | 56.47 | 64.00 | 76.80 | 96.00 | **120.00** | 144.00 | 176.00 | 187.64 | 192.00 |
| `g` | **2.4000** | 2.3668 | 2.3407 | 2.3040 | 2.2667 | **2.2500** | 2.2667 | 2.3407 | 2.3824 | **2.4000** |

> **`g` is 2.4 at both ends and has an interior minimum of exactly `9/4`, at `β = 2.4`, i.e. `ρ = 8`, `c = 120`.**
> `C-0023`'s 2.4 is therefore a **ceiling** on the draw-in demand over the whole continuum, right at the two
> points it was evaluated at and up to **6.25 %** high between them. At this claim's design point `g = 2.2672`.

Asserted as a gate-2 test at both endpoints, at the exact minimum, and as a bound over nine intermediate
values — because the coincidence at the endpoints is what made the interior look uninteresting.

---

## The catalogue: six joints, and the third stiffness that decides them

A beam end transmits **three** things — a transverse shear (in both directions, because the coupling is
two-sided), an axial force and a moment. Every joint below carries all three, and is reported as
`(k_θ, k_a, k_transverse, dead band)`.

| | joint | `k_θ` [pN·nm/rad] | `k_a` [pN/nm] | `k_⊥` [pN/nm] | **anisotropy** | dead band [nm] | supports? |
|---|---|---|---|---|---|---|---|
| **`J1`** | direct antiparallel crossover | **13.53** | **64.71** | 64.71 | **1.00** | 0 | **yes** |
| `J2` | nicked continuation, one backbone intact | 683.0 | 3267 | 3267 | 1.00 | 0 | yes |
| `J2b` | doubly nicked continuation | **13.53** | **64.71** | 64.71 | 1.00 | 0 | yes |
| **`J3-2`** | **2 nt ssDNA hinge** (`C-0023`'s remedy) | 3.345 | **4.552** | **4.552** | **1.00** | **1.30** | **NO** |
| `J3-10` | 10 nt ssDNA hinge | 0.669 | 0.910 | 0.910 | 1.00 | 6.50 | **NO** |
| `J4-2` | 2-crossover clamp at the 32 bp pitch | 3857 | 129.4 | 129.4 | 1.00 | 0 | yes |
| **`J5-8`** | **normal duplex standoff, 8 nm** | **28.75** | **1.348** | **137.5** | **102.0** | 0 | **yes** |
| `J5-10` | normal duplex standoff, 10 nm | 23.00 | 0.690 | 110.0 | 159.4 | 0 | yes |

&nbsp;&nbsp;&nbsp;&nbsp;**Every isotropic joint has anisotropy exactly 1, and that is not a coincidence: a
covalent tie on Chen et al.'s softened bond has no direction, and neither does a chain.**

### The two structural statements the catalogue makes

> **1. A double nick IS a crossover.** A nicked continuation keeps one *intact* backbone, which is not a
> softened bond — it carries the duplex's own `B/a` and `S/a` — so it is effectively clamped **and**
> effectively held, the worst corner of both of `C-0023`'s brackets. Cut the second backbone at the same base
> pair and nothing continuous is left: `J2b` reproduces `J1` **to the last digit**, 44.03 nm and 79.18 pN/nm.
> That is a result of the construction, not an assumption put into it.
>
> **2. A joint has to be stiff across the beam and soft along it, and for a flexible link those are the same
> number.** The beam's own per-path stiffness is `33.3333/45 = 0.7407 pN/nm`. A 2 nt hinge supplies
> **4.55 pN/nm** across it — 6.1×, against the 10× a support needs — and a 10 nt hinge, which is what the
> *compliance* ceiling would ask for, supplies **0.91 pN/nm**, i.e. **1.23× the beam it is meant to support**.
> The support and the beam become the same spring. This is [`CH-0031`](../challenges/CH-0031-a-flexible-hinge-cannot-be-anisotropic.md).

---

## The designs, at `C-0015`'s 45 paths and §3's working point

Span solved as a **root** at every joint, exiting on the bracket width. `T(3)` and `T(10)` are the beam's own
axial tension at §3's acceptable and desired strokes.

| | joint | span [nm] | bp | `c` | `S_eff/S` | secant | **tangent** | `t/s` | `T(3)` | `T(10)` | verdict |
|---|---|---|---|---|---|---|---|---|---|---|---|
| `I1` | *ideal pin, free* (`C-0023` `E3a`) | **24.61** | 72 | 48.0 | 0.000 | 33.333 | **33.33** | 1.000 | 0 | 0 | PASS — **but no joint realises it** |
| `I2` | *ideal pin, held* (`C-0023` `E3b`) | **49.41** | 145 | 48.0 | 1.000 | 33.333 | **91.13** | 2.734 | 8.08 | **86.68** | FAIL P3 |
| `I3` | *ideal clamp, free* | 39.06 | 115 | 192.0 | 0.000 | 33.333 | 33.33 | 1.000 | 0 | 0 | PASS — idealisation |
| `I4` | *ideal clamp, held* | 54.91 | 162 | 192.0 | 1.000 | 33.333 | 75.62 | 2.269 | 6.55 | 70.69 | FAIL P3 |
| **`J1`** | **direct crossover** | **44.03** | **129** | **83.2** | **0.564** | 33.333 | **79.18** | 2.375 | 5.74 | **61.04** | **FAIL P3 (1.98×)** |
| `J2` | nicked continuation | 54.53 | 160 | 185.2 | 0.988 | 33.333 | 75.97 | 2.279 | 6.56 | 70.77 | FAIL P3 |
| `J2b` | doubly nicked | 44.03 | 129 | 83.2 | 0.564 | 33.333 | 79.18 | 2.375 | 5.74 | 61.04 | FAIL P3 |
| `J3-2` | **2 nt ssDNA hinge** | 28.37 | 83 | 55.1 | 0.055 | 33.333 | 49.54 | 1.486 | 1.35 | 13.63 | **FAIL P1** (*and* P3) |
| `J3-10` | 10 nt ssDNA hinge | 25.40 | 75 | 49.3 | 0.010 | 33.333 | 37.50 | 1.125 | 0.31 | 3.12 | **FAIL P1** |
| `J4-2` | 2-crossover clamp | 51.79 | 152 | 190.7 | 0.753 | 33.333 | 71.20 | 2.136 | 5.54 | 59.61 | FAIL P3 |
| `J5-5` | normal standoff, 5 nm | 35.42 | 104 | 115.6 | 0.082 | 33.333 | 45.88 | 1.376 | 1.28 | 13.32 | FAIL P3 |
| **`J5-8`** | **normal standoff, 8 nm** | **31.64** | **93** | **95.6** | **0.019** | **33.333** | **37.39** | **1.122** | **0.37** | **3.83** | **PASS** |
| **`J5-10`** | **normal standoff, 10 nm** | **30.44** | **90** | **87.7** | **0.009** | **33.333** | **35.59** | **1.068** | **0.20** | **2.04** | **PASS** |

Three things fall out, and none of them was assumed.

1. **Every joint that supports the beam sits at `c = 83.2–191.7` — the upper 76 % of the bracket.** The
   near-pinned joints in the table are *exactly* the ones that fail `P1`. `C-0023`'s pinned column is not
   pessimistic; it is unreachable, and the cheap bound said so in one division.
2. **`C-0023`'s restrained reading is the one that survives, for every covalent joint.** `J1`, `J2`, `J2b`,
   `J4` all fail the 40 pN/nm ceiling by 1.78–1.98×, and all put 59.6–70.8 pN of beam tension into §3's
   desired stroke.
3. **The single-stranded hinge is not excluded on the axis `C-0023` was reasoning on.** Sized on stiffness
   alone the 2 nt hinge fails `P3` too (49.54 pN/nm) and a 10 nt hinge *passes* it (37.50). Both fail `P1`.
   **The remedy fails on a requirement `C-0023` never wrote down.**

### The unmeasured constant does not rescue it — and that is the load-bearing check

`k_s` is `C-0020`'s **derived** construction, not a measurement, and `C-0020` sweeps it over four decades.
`k_θ` is `C-0009`'s **fitted** constant with Chen et al.'s own `α ∈ [0.6, 1.2]`, a factor of exactly two.

| axis | range swept | crossover-joint span | tangent | ceiling |
|---|---|---|---|---|
| `α` | 0.6 → 1.2 | 40.28 → 45.23 nm | **77.19 → 79.47** | **fails at all 3** |
| `k_s` multiplier | **×1/32 → ×128** | 29.47 → 50.96 nm | **40.24 → 85.81** | **fails at all 8** |

> **The crossover joint is past `C-0023`'s compliance ceiling at every point of a four-decade sweep of the one
> input nobody has measured** — the closest approach is 40.24 pN/nm at `k_s/32`, still above 40. Asserted as a
> gate-5 test over the whole sweep, because a verdict that depended on `k_s` would have to wait for `T-9`.

---

## The design that results, and the window it sits in

**A duplex standing NORMAL to the sheet under each end of the beam.** It is the same escape `C-0023` used to
leave the axial trade-off — *bending is signed and has a direction* — applied **one level down, to the joint
instead of the element**: the standoff carries the beam's end shear **along its own axis** (`S/ℓ`) and
releases the beam's draw-in by **bending** (`3EI/ℓ³`), so

&nbsp;&nbsp;&nbsp;&nbsp;**anisotropy = `S ℓ²/(3EI)`, which the designer sets with a length and which grows as `ℓ²`.**

| `ℓ` [nm] | bp | span [nm] | bp | `c` | **tangent** | `T(10)` [pN] | support margin | buckling margin (pinned / guided) | P1·P3·P4·P5 |
|---|---|---|---|---|---|---|---|---|---|
| 3 | 9 | 42.83 | 126 | 140.3 | 62.61 | 37.88 | 495× | 17.0 / 68.1× | fail P3 |
| 4 | 12 | 38.21 | 112 | 126.4 | 52.76 | 22.33 | 371× | 9.6 / 38.3× | fail P3 |
| 5 | 15 | 35.42 | 104 | 115.6 | 45.88 | 13.32 | 297× | 6.1 / 24.5× | fail P3, P4 |
| 6 | 18 | 33.68 | 99 | 107.4 | 41.63 | 8.36 | 248× | 4.3 / 17.0× | fail P3 |
| **7** | **21** | **32.50** | **96** | **100.9** | **39.03** | **5.53** | 212× | 3.1 / 12.5× | **PASS** |
| **8** | **24** | **31.64** | **93** | **95.6** | **37.39** | **3.83** | **186×** | **2.4 / 9.6×** | **PASS** |
| **9** | **26** | **30.98** | **91** | **91.3** | **36.32** | **2.75** | 165× | 1.9 / 7.6× | **PASS** |
| **10** | **29** | **30.44** | **90** | **87.7** | **35.59** | **2.04** | 149× | 1.5 / 6.1× | **PASS** |

> **The window is 7–10 nm (21–29 bp), and it is closed from both sides by different mechanisms.** Below, by
> `C-0023`'s **compliance ceiling** — a short standoff is axially stiff and the membrane term returns. Above,
> by `C-0017`'s own **10 nm standoff envelope** (`P5`). And across it the Euler buckling margin at the desired
> stroke falls from 3.1× to 1.5× on the conservative pinned-head reading, which is why the design point sits
> at the **short** end of a window whose short end is also where the compliance is tightest. The buckling
> margin is **reported, not adopted as a predicate** — the five predicates were declared before the run.

### The nominal design

| | |
|---|---|
| **element** | transverse duplex flexure, tile tied at midspan, 45 of them on `C-0015`'s 3 × 15 grid |
| **span** | **31.64 nm = 93 bp** |
| **end joint** | a duplex standing normal to the sheet, **8.0 nm = 24 bp**, at each end |
| **end condition realised** | `ρ = 3.95`, **`c = 95.6`** — 33 % of the way from pinned to clamped |
| **axial restraint realised** | `S_eff/S = 0.019` — **1.9 % of the held reading**, i.e. effectively free |
| **placement** | secant **33.3333 pN/nm** by construction (§3's 100 pN over 3 nm) |
| **compliance** | tangent **37.39 pN/nm**, `t/s` = 1.122 — inside `C-0023`'s 40 pN/nm ceiling with 7 % to spare, and the 12 % of `t/s` above unity is free stability margin by `C-0017`'s theorem |
| **draw-in demand** | **0.645 nm = 1.90 bp** at 3 nm (`g = 2.2672`, not 2.4), 7.17 nm at 10 nm; the joint supplies 0.277 nm per end by bending |
| **beam axial tension** | **0.373 pN** at 3 nm, **3.83 pN** at 10 nm — 3.8 % and 38 % of the 10 pN unzip allowable |
| **end shear per joint** | 1.111 pN at 3 nm, 3.70 pN at 10 nm; standoff buckling 8.87 pN (pinned head) / 35.5 pN (guided) |
| **transverse support** | 137.5 pN/nm against the beam's own 0.741 — **186×**, no dead band |
| **bonded length the joint needs** (`CH-0029`) | **4.0 bp** at the desired stroke, against Strunz's 18.8 pN at 8 bp — the joint is not the constraint |

---

## `CH-0029` applied, and it changes a verdict

The shear allowable is a function of the bonded length (18.8 pN at 8 bp, 34.8 at 16, 47.1 at 30, saturating
at **68.1 pN**), and this task **inverts** it: what bonded length does a given tension demand?

| design | `T` at 10 nm | bonded length demanded |
|---|---|---|
| **standoff design (`J5-8`)** | **3.83 pN** | **4.0 bp** — free |
| crossover joint (`J1`) | 61.04 pN | **100.2 bp** — longer than a whole staple |
| 2-crossover clamp (`J4-2`) | 59.61 pN | 82.9 bp |
| **`C-0023`'s restrained flexure (`I2`)** | **86.68 pN** | **none exists** — past the 68.1 pN loading-rate-free saturation |

> **`C-0023` reported the restrained reading as *"past the 65 pN nicked-duplex ceiling"*. Against `CH-0029`'s
> ladder it is worse than that: no hybridised bonded length of any size carries 86.7 pN, at any loading rate
> inside Strunz's measured 16–4000 pN/s.** The correction runs the same way `C-0023`'s own falsifier 5 did,
> and it removes the last reading under which the restrained flexure could be built.

## And the desired stroke puts a floor under the path count

`C-0023` read the per-path static share at §3's **acceptable** 3 nm point, where 45 paths give 2.22 pN. At the
**desired** 10 nm stroke the same coupling delivers `33.3333 × 10 = 333.33 pN`, so the share is **7.41 pN** and

&nbsp;&nbsp;&nbsp;&nbsp;**the 10 pN unzip allowable puts a floor of `333.33/10 = 33.3`, i.e. 34, load paths under
the design** — independently of the joint, the element and the layer.

`C-0015`'s flatness grid of 45 clears it by only **1.35×**. That is a **fourth** independent route to the same
count (after flatness, buildability and per-path safety at 3 nm) and **the tightest of them**; 15 paths give
22.2 pN and 8 give 41.7 pN, both past the allowable before any joint is chosen. Asserted as a gate-1 test and
reproduced as `C-0023`'s own falsifier 3 in the design table.

---

## The five verification gates

Executed as **26 gate-named tests** in `src/test/kotlin/anchoring/FlexureEndJointTest.kt`; **135 `anchoring`
tests, 931 in the suite, 0 failures**, on `tools/verify.sh` and on an independent isolated full-tree run.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | `ρ = k_θL/EI` is dimensionless and linear in the span (so `c` depends on the span, not only on the joint); `S_eff` is a force and is invariant under doubling `k_a` while halving `L`; a flexure stiffness is `EI` over a cubed length and doubling the span divides it by exactly 8; **`33.3333 × 10 / 10 = 33.3` paths** — the desired stroke's floor on the path count, pure arithmetic; unphysical arguments throw | **PASS** |
| **2 — limiting cases** | **`c(0) = 48` and `c(∞) = 192` exactly**, monotone over nine values, with `c(4) = 96`; **`S_eff → S` and `S_eff → 0`** at the two axial limits; **the partial model reproduces `C-0023`'s filed `TransverseDuplexFlexure` identically** at all four corners (pinned/clamped × free/held), at three spans × four displacements, in reaction, tangent **and** axial tension, to `1e−9`; the law stays **odd** and the tangent **even** at partial restraint, and two-sidedness is evaluated at negative argument; **`g(0) = g(∞) = 2.4` and the interior minimum is exactly `9/4` at `ρ = 8`**, bounded over nine values; the draw-in vanishes as `δ²` and is even; the membrane term is zero at zero deflection and cubic thereafter (8.00× per doubling) | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the solved span reproduces its own target secant to **1e−8** at four different joints; the span root is **exactly scan-independent** over 32 → 2048 steps (departure `0.00e+00`) and exits on the **bracket width**; the analytic tangent matches a central difference to `1e−6` at three deflections × three joints, and converges as the step is refined (`2.5e−8 → 0 → 0`); the bonded-length inversion round-trips through its own forward evaluation to `0` at four tensions; **the result file is byte-identical on two independent `tools/study.sh` runs** | **PASS** |
| **5 — literature cross-check** | `C-0023`'s four spans reproduced (**24.6094 / 39.0650 / 49.4140 / 54.9131** against 24.61 / 39.07 / 49.41 / 54.91, ≤ 1.3e−4), both tangents (**33.3333 / 91.1275** against 33.333 / 91.13), both restrained tensions (**8.0793 / 86.684** against 8.08 / 86.7) and the **0.8777 nm = 2.58 bp** draw-in demand; `Gen1Tile`'s crossover hinge and in-plane constants reproduced (13.5294, 64.7059) and Chen et al.'s `α` bracket shown to be a factor of exactly 2; `CH-0029`'s ladder reproduced (**18.796 / 34.810 / 47.107** and the **68.123 pN** saturation); `C-0017`'s mandate; the ssDNA hinge built on the **zero-force** end of the Kuhn bracket and its stiffness shown equal to `C-0014`'s own `FreelyJointedChain.gaussianStiffness`; **the crossover joint shown to fail the ceiling at all 8 `k_s` multipliers and all 3 `α` values**, and the normal standoff to clear it at all four window lengths. Worst departure over 17 reproductions: **5.9e−4** | **PASS** |

### Gate 3 — three things that are not restatements of the construction

1. **The series statement closes on the cable geometry it was not written from.** `S_eff` is *defined* as the
   beam's `S/L` in series with two joints; the test checks that the beam's own stretch **plus** twice the
   joint's extension equals the draw-in the *chord* geometry independently charges, `δ²/(L/2)`, to `1e−3` at
   two deflections and two joints. The two are different expressions of the same compatibility and neither
   was used to derive the other.
2. **A flexible link's transverse stiffness equals its axial one exactly**, asserted at four hinge lengths —
   `C-0014`'s convexity theorem in a new place, and the whole reason `J3` fails `P1` — **and the escape is
   asserted beside it**: a normal standoff's anisotropy is `Sℓ²/(3EI)` and **quadruples when its length
   doubles**, checked at 5 and 10 nm.
3. **A covalent joint has no dead band and a slack one has exactly its contour**, asserted over four covalent
   motifs and the hinge. This is the statement that turns "the hinge is soft" into "the hinge is not a
   support", and it is the one `C-0023` did not have.

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | the partial model failing to reproduce `C-0023`'s spans and tangents at its limits | **no** | all four spans and both tangents to ≤ 1.3e−4, and the *element* identically to `1e−9` |
| 2 | every candidate joint landing at one end of the bracket | **no** | they land at `c = 48.7–191.7`, and the ones that **support** the beam at 83.2–191.7 |
| 3 | the answer being insensitive to the joint | **no** | span 25.0 → 54.5 nm and tangent 33.3 → 79.2 pN/nm across the catalogue, far beyond `k_s`'s own four-decade spread of 29.5 → 51.0 nm |
| 4 | **no joint passing `P1` and `P3` together** | **very nearly, and it is the result** | **no isotropic joint does** — every covalent motif fails `P3` and every ssDNA hinge fails `P1`. Only the anisotropic standoff passes, and its window is 7–10 nm |
| 5 | the draw-in shape factor being 2.4 everywhere | **no** | 2.25–2.40, minimum exactly `9/4` at `ρ = 8` |
| 6 | `CH-0029`'s ladder making no difference | **partly, and it discriminates** | it is slack for the design (4.0 bp) and **decisive** against the restrained reading, which asks 86.7 pN — past the 68.1 pN saturation, so no length carries it |

A **result that was not anticipated**: the per-path static share at §3's *desired* stroke puts a floor of **34
load paths** under the whole design, independently of everything else — a fourth and tightest route to
`C-0015`'s 45.

---

## Does `C-0023`'s verdict survive?

**Yes, with its remedy replaced and one of its columns demoted.**

| `C-0023` said | this claim finds |
|---|---|
| the end condition is a 4× bracket, 48 or 192 | it is a **continuum**; every joint that supports the beam sits at 83.2–191.7 |
| axial restraint is the binding choice | **confirmed**, and it is decided by **anisotropy**, not by softness |
| the restrained reading fails the 40 pN/nm ceiling and the 65 pN ceiling | **confirmed**, and sharpened: past the 68.1 pN saturation, so **no bonded length carries it** |
| a 2 nt ssDNA hinge at each end absorbs the 0.88 nm | **falsified** — [`CH-0031`](../challenges/CH-0031-a-flexible-hinge-cannot-be-anisotropic.md). It pays the whole slack back as transverse dead band |
| the draw-in demand is `2.4 δ²/L` for both end conditions | right at both endpoints, **up to 6.25 % high between them**; the design point is 2.2672 |
| `E3a` is buildable at a 24.61 nm span | it is an **idealisation**; the buildable design is 31.64 nm with 8 nm standoffs |
| `E5`, the crossover hinge, is the fallback if the ends cannot draw in | **untouched**, and still the fallback |

**And `T-13` still closes.** The flexure's law is **odd at every restraint** — asserted at negative argument
here as in `C-0023` — and the placement condition is met by construction at every joint, so the two-sidedness
argument, the currency identity `F_req = k_req·σ`, and `C-0023`'s zero-bias verdict (`k ≥ k_BT/σ² =
0.4602 pN/nm`, supplied **72.4× over**, with no tether and no preload) are all untouched. What moves is the
**span** and the **tangent** — that is, `C-0023`'s own compliance ceiling and its per-path forces at the
desired stroke — and both land inside its brackets.

---

## Validity range

- **TRL 1–3. Nothing here is measured.** No joint has been built and none is a sequence design; base pairs and
  nucleotides make the design statement concrete, they do not specify a staple.
- **The two joint springs are treated as INDEPENDENT.** A real cantilever standoff has an off-diagonal
  compliance — a tip force also rotates the tip, `δ = Fℓ³/3EI + Mℓ²/2EI` — which is not modelled and which
  **softens** the joint further, so `J5`'s numbers are the **stiff** reading of it and its compliance verdict
  is conservative. Named as the first open question.
- **The membrane term is `C-0023`'s two-term large-deflection model, unchanged, with `S_eff` for `S`.** At
  3 nm on a 25–55 nm span the deflection ratio is 5–12 %, inside its range; at §3's **desired** 10 nm stroke
  it is 18–40 % and the model *understates* the stiffening, so **every 10 nm column is a lower bound**, exactly
  as `C-0023` flags. The standoff's own deflection at the desired stroke is 28–39 % of its length, likewise
  past small deflection and likewise a lower bound on the tension.
- **The cable geometry charges `2δ²/L` while the beam's own deflected shape demands `2.25–2.40 δ²/L`** — a
  1.13–1.20× gap between the term that *produces* the tension and the term that *measures* the demand. Both
  are reported and the larger is used wherever a joint is sized.
- **`k_θ = 2αB/(100a)` is `C-0009`'s CITED, FITTED constant** with Chen et al.'s own `α ∈ [0.6, 1.2]`, a factor
  of exactly two, and it is swept. **`k_s = 2αS/(100a)` is `C-0020`'s DERIVED construction and is NOT
  measured**; it is swept over `C-0020`'s own four decades and **no verdict moves across them**. `T-9` would
  settle both.
- **`EI = 230 pN·nm²` is a CanDo MODEL INPUT, not a measurement** — its implied persistence length is 55.5 nm
  against 40–47 measured — so it is the **stiff** end and every span here is correspondingly long.
- **The ssDNA hinge uses the ZERO-FORCE end of the method-systematic Kuhn bracket** (2.10 nm), because these
  elements carry ~1 pN, an order below the lowest force the 10–40 pN spectroscopy fits cover; the 1.34 nm and
  2.84 nm ends are reported beside it and **neither changes the `P1` verdict** (transverse stiffness 7.13 and
  3.37 pN/nm against the 7.41 required, with the dead band unchanged at 1.30 nm either way). The contour per
  nucleotide is the **inextensible** 0.65 nm that travels with that convention.
- **The standoff's buckling load is quoted at BOTH end conditions**, `K = 2` and `K = 1`, a factor of exactly
  4, and the binding margin is read at the conservative pinned-head one. It is **reported beside** the
  predicates, not adopted as one, because the five predicates were declared before the run.
- **Whether a duplex can be built standing normal to a single-layer sheet with a rotationally stiff base is
  not established here** — the base joint is itself one of `J1`–`J4`, and it is named as an open question.
- **One beam per load path**, exactly as `C-0023` assumes, and the same 45 attachments. The flexure array on a
  common superstructure is `T-31`'s; the lever's ability to react a downward push is `T-33`'s.
- **No zero-bias re-solve.** The joint changes the element's *geometry*, not its **sidedness**, so `C-0023`'s
  confinement verdict is inherited rather than recomputed — and that inheritance is exactly what makes the
  `T-13` statement above a transfer rather than a new result.
- **A hybridised staple domain inside a sheet is not a free oligonucleotide** (`C-0024`'s own limit), so every
  `CH-0029` bonded length here inherits `T-35`'s open question.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| duplex `EI` | 230 pN·nm² | **CITED, a CanDo MODEL INPUT** (Kim et al., *NAR* **40**:2862, 2012), **not a measurement** |
| duplex stretch modulus `S` | 1100 pN | **CITED, MEASURED**, Wang et al., *Biophys. J.* **72**:1335 (1997), in Mg²⁺ |
| crossover hinge constant `k_θ = 2αB/(100a)` | 13.53 pN·nm/rad, `α ∈ [0.6, 1.2]` | **CITED, FITTED**, Chen et al., *JACS* **136**:6995 (2014) SI §S2, via `C-0009` |
| crossover in-plane constant `k_s = 2αS/(100a)` | 64.71 pN/nm | **DERIVED** from the same construction (`C-0020`), **NOT measured**; swept four decades |
| ssDNA Kuhn length | **2.10 nm, zero-force end** | **CITED, MEASURED**, Chen et al., *PNAS* **109**:799 (2012); 1.34–1.41 nm from 10–40 pN spectroscopy (Bosco et al., *NAR* **42**:2064, 2014) reported beside it |
| ssDNA contour per nucleotide | 0.65 nm, inextensible | **CITED, MEASURED** (Sim et al. 2012; Bosco et al. 2014). The convention travels with the number |
| rise per base pair, crossover interface pitch | 0.34 nm; 32 bp | **CITED** (Douglas et al. 2009; Rothemund, *Nature* **440**:297, 2006) |
| the shear allowable's three constants | `x₀ = 0.7 nm`, `x₁ = 0.07 nm/bp`, `α = 3`, `β = 0.5` | **CITED, MEASURED**, Strunz et al., *PNAS* **96**:11277 (1999), via `C-0024`/`CH-0029`, used only inside 16–4000 pN/s |
| per-path allowables | 10 / 65 pN | **CITED** via `C-0006` |
| `C-0023`'s four spans and two tangents | 24.61 / 39.07 / 49.41 / 54.91 nm; 33.333 / 91.13 pN/nm | **CITED**, and reproduced here as gate-5 tests |
| §3 targets | 100 pN, 3 nm, 10 nm, 40 × 40 nm, 2 mM | **CITED** |

Everything else — `c(ρ)`, `g(β)`, `S_eff`, every joint's three stiffnesses, every span, tangent, draw-in,
tension, bonded length, buckling margin and window bound — is **derived here in code**, with `C-0023`'s and
`C-0024`'s pipelines **re-run rather than tabulated**.

## Still open — named, not answered

1. **The off-diagonal compliance of a cantilever standoff**, which couples the beam end's rotation to its
   axial motion. It softens the joint, so the design's compliance verdict is conservative — but it also
   couples `c` and `S_eff`, which this model treats as independent.
2. **Whether a duplex standing normal to a single-layer sheet is buildable with a rotationally stiff base.**
   The base joint is one of `J1`–`J4`, and the standoff's own `k_θ = EI/ℓ` assumes it is a clamp.
3. **`k_s`**, `C-0020`'s derived crossover in-plane constant. No verdict here moves across its four decades,
   but it is the one input that moves the crossover joint's span by more than the design tolerance. `T-9`.
4. **A pre-bowed beam**, which would make the draw-in demand relative to the *built* shape rather than to the
   straight one and is worth up to 2× in the peak tension — unpriced here.
5. **The flexure array** (`T-31`): neighbouring standoffs on a shared superstructure, which the
   independent-leaf-spring reading does not cover, and which the compliance **ceiling** makes non-conservative.

## Challenges

**Raises [`CH-0031`](../challenges/CH-0031-a-flexible-hinge-cannot-be-anisotropic.md)** against `C-0023`'s
proposed remedy for the axial-restraint bracket. **No number in `C-0023` moves** — all four of its spans, both
its tangents, both its restrained tensions and its draw-in demand are reproduced here to ≤ 5.9e−4, and the
replacement design lands **inside** its own bracket.

**None stands against this claim.** The three ways it would fail:

1. **A measurement showing the crossover's in-plane resistance is far below `k_s/32`.** Then the crossover
   joint could reach the compliance ceiling and the standoff would be unnecessary. It would take more than the
   four decades `C-0020` sweeps.
2. **A joint motif that is anisotropic without standing out of the sheet.** Nothing in the catalogue is, but
   the catalogue is six motifs and not a proof of exhaustiveness — the *argument* (a flexible link has no
   direction; a covalent tie on a softened bond has none either) is what generalises, and it would be
   falsified by a single counterexample.
3. **A demonstration that a duplex cannot stand normal to a single-layer sheet**, which would remove the only
   passing joint and hand the answer to `E5`, `C-0023`'s crossover-hinge flexure — which is unaffected,
   because it accommodates its rotation in a hinge rather than in a span.
