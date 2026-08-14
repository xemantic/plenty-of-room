# C-0032 — A strain-softening coupling discharges §3's placement clause EXACTLY and its stability clause NOWHERE the fold binds: at 10 nm in 2 mM the pull-in margin collapses from 1.007–1.032 to 1.000–1.002 and the fold walks back through §3's own 3 nm target stroke, and the escape `CH-0042` named fails at every standoff length

| | |
|---|---|
| **Task** | [`T-76`](../tasks/T-76-softening-coupling-stability.md) |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with **`A2.2`** (the pull-in ceiling the condition is read against) |
| **Verification type** | **in-silico** (`C-0018`'s stroke-parametrised equilibrium path re-run with `C-0030`'s **nonlinear** reaction law substituted for the affine `R = 33.333 s`, over the same `(height, model, buffer)` grid — 216 fold searches against `C-0018`'s 162 — graded against the tangency identity `k_c(s_fold) + k_eff(s_fold) = 0`, with `k_c` from the element's analytic tangent and `k_es` from a central difference of a full Poisson-Boltzmann re-solve at fixed applied bias) **+ logical** (placement fixes the *level* of the load line and stability its *slope*; the two are checked separately and one is satisfied while the other is not) |
| **Verdict** | **`CH-0042` is UPHELD, and sharpened. The answer is NO at 2 mM and YES at 0.5 mM, and it is a *design decision*, not a fix.** `C-0017`'s theorem is confirmed exactly where its premise holds — the strain-**stiffening** decoupled element (`t/s` = 1.095) loses **0 of 54** states against the affine mandate and *raises* the 10 nm / 2 mM margin from 1.007–1.032 to 1.020–1.774. `C-0030`'s strain-**softening** element loses **7**, all but one of them the whole 10 nm / 2 mM column: the operating bias `V*` is **unchanged to the last bit** (placement), while the pull-in bias falls 0.7–1.8 % and — the number that decides it — **the fold's own stroke walks from 3.41–4.13 nm back to 2.80–3.17 nm, crossing §3's 3 nm target at two of six models.** The bias margin is **1.0000–1.0019**: the device sits *on* its fold. `Q2` read `CH-0042`'s way fails at 6 of 6 there (22.88 pN/nm against a 23.41–27.91 floor) and `Q3` at 6 of 6 too, by two different tests. **At 0.5 mM every predicate clears** — 22.88 against a 3.86–15.94 floor, **1.44–5.93×**, and a bias margin of **1.038–2.327** — with one model acquiring a fold the mandate does not have. **The escape fails**: the adverse mounting's assembled tangent is **42.38–61.04 pN/nm** against `C-0023`'s 40 pN/nm ceiling at **0 of 8** standoff lengths, i.e. 1.06–1.53× past it everywhere in `C-0017`'s envelope. **A third route that no upstream claim names lands 2 % short**: shortening the standoff to `ℓ = 5 nm` — the bottom of `C-0030`'s own window — raises the tangent minimum to **27.30 pN/nm**, which clears five of the six model floors at 2 mM and not the sixth, and `ℓ = 4 nm`, which would clear all six at 28.71, is excluded by `C-0030`'s `P4`. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED, and the MOTIF is not demonstrated either** — `C-0028`'s literature finding stands unchanged. And *within* mean field: `C-0005`'s one-loop correction is **123–214 %** of the leading term across this gap range, which is two orders larger than the 1.000–1.002 margin this claim reports. |
| **Provenance** | `gpd/results/T-76-softening-coupling-stability.json`, produced by `stability.SofteningCouplingStabilityStudyKt`; **4 coupling records, 216 fold records, 16 escape records, 163 upstream checks, 14 convergence records**; **19 gate-named tests in `stability/SofteningCouplingStabilityTest`**, full suite **`BUILD SUCCESSFUL`** on `tools/verify.sh`, twice — once with a sibling's then-half-written `GuidedArmAnchorageTest.kt` dropped from the snapshot per `CLAUDE.md`, and once with the whole tree and nothing dropped; the result file re-run through `tools/study.sh` and diffed **byte-for-byte identical** |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`, `l_B = 0.7141 nm`, `ε_r = 78`; aqueous `MgCl₂` at **0.5 / 2 / 10 mM**; 40 × 40 nm Manning-renormalised tile; PEG layer 5 / 7 / 10 nm at `σ` = 0.092 / 0.045 / 0.024 nm⁻²; all six `C-0003` models; **four load lines, all placed at 100 pN over 3 nm**; `C-0030`'s recommended element — 45 paths, `B2` base, 8 nm standoff, `EI` = 230 pN·nm², `S` = 1100 pN |
| **Consumes** | [`C-0018`](C-0018-maximum-usable-bias.md) (`EquilibriumPath`, `holdingBias`, `bindingCeiling`, `biasMargin` — **re-run as a library, unchanged**), [`C-0030`](C-0030-coupled-standoff-joint.md) (`CoupledJointFlexure`, `coupledFlexureSpan`, `standoffTipFlexibility`, `FlexureOrientation` — the load line), [`C-0028`](C-0028-standoff-base-joint.md) (`StandoffBase.crossovers(2)`), [`C-0017`](C-0017-output-coupling-stiffness.md) (the mandate, the stability floors, the theorem this claim tests), [`C-0023`](C-0023-two-sided-coupling.md) (the 40 pN/nm compliance ceiling), [`C-0012`](C-0012-coupled-stroke-and-blocking-force.md)/[`C-0008`](C-0008-electrostatic-force-and-decay-length.md)/[`C-0003`](C-0003-crossover-valid-layer-response.md)/[`C-0002`](C-0002-peg-material-parameters.md) (the characteristic, the field, the six layer models, the material) |
| **Resolves** | [`CH-0042`](../challenges/CH-0042-a-strain-softening-coupling-has-no-free-stability-margin.md) — **UPHELD**, on its first horn |
| **Raises** | [`CH-0047`](../challenges/CH-0047-a-tangent-minimum-over-zero-stroke-is-not-a-requirement.md) against its own challenge's reading of `min_s k_tangent` |

---

## THE CONVENTIONS — read these before any number below

- `z` is normal to the electrode, positive **away** from it; **the electrostatic gap is the layer height, exactly**.
- The **stroke** `s = L₀ − h` is positive **downward**; **`L₀` is a FORCE-ONSET height** (`C-0011`, `CH-0010`).
- A **load line** `R(s)` is positive **upward**, in pN over the whole 45-path array.
  Its **secant** `R(s)/s` is what §3's *placement* clause is written on and its **tangent** `dR/ds` is what the *stability* clause is written on;
  **they are the same number only for an affine line through the origin.**
- **All four load lines pass through the same operating point — 100 pN at 3 nm** — and differ only in how they leave it.
  That is what makes a state-by-state comparison of their folds a comparison of one device rather than of four.
- The flexure's **mounting sense** is `C-0030`'s: **favourable** is the sense in which the midspan sags toward the body its standoff bases stand on, which supplies the draw-in and is the softening one.
- `k_es = −∂F_z/∂h`, **negative above the force maximum and positive below it** (`CH-0011`); `k_eff = k_brush + k_es`.
- **Where `k_eff ≥ 0` there is no stability requirement at all**; the margin is recorded as `null`, never as an infinity.
- **A bias ceiling belongs to a `(bias, load line)` pair, never to the bias alone** (`CH-0015`).

---

## The claim, in one line

**§3's two clauses are written on two different slopes of one law, and `C-0030`'s coupled flexure satisfies the first to 2e−15 while failing the second wherever a fold exists: the placement clause fixes the level of the load line so the operating bias is *bit-identical* across all four lines at 144 of 144 comparisons, and the whole of the margin loss is therefore in the ceiling — the pull-in bias at 10 nm / 2 mM falls only 0.7–1.8 %, but the fold's own STROKE walks back from 3.41–4.13 nm to 2.80–3.17 nm, through §3's 3 nm target at two of six models, leaving a bias margin of 1.000–1.002 where `C-0018` had 1.007–1.032. `C-0017`'s theorem is not wrong — the strain-stiffening element loses nothing and gains up to 1.774× — it has simply been applied outside its premise; and the escape `CH-0042` named is 1.06–1.53× past `C-0023`'s compliance ceiling at every standoff length in `C-0017`'s envelope, so what remains is 0.5 mM, where every predicate clears with 1.44–5.93× on the stiffness axis and 1.038–2.327× on the bias axis.**

---

## The cheap bound, which ran first — and what it could not say

Three numbers, no code, before the sweep:

| | value |
|---|---|
| `C-0030`'s assembled tangent minimum over 0–10 nm | **22.88 pN/nm** |
| `C-0017`'s stability floor `\|k_eff(3 nm)\|` at 10 nm, **2 mM** | **23.41 – 27.91 pN/nm** |
| the same at **0.5 mM** | **3.86 – 15.94 pN/nm** |

> **Falsifier 1 did not fire** (the bound does *not* clear 2 mM — it is 2.3 % short at the best end), so the sweep was justified.
> It is asserted as a test: `gate 5 upstream cross-check - the cheap bound, asserted as a test`.

**Falsifier 2 was declared and did not fire either.** The sweep says four things the division cannot:

1. the **fold's stroke** moves, and that is the binding test rather than the bias one — 2 of 6 models put the fold *shallower* than §3's target;
2. the **bias** margin, which `C-0018` shows is 10–40× tighter than the stiffness margin, collapses to **1.0000**;
3. the strain-**stiffening** reading loses **nothing** and gains up to 1.774× — the theorem is confirmed in its own premise, which no comparison against a floor could establish;
4. `CH-0042`'s own `min_s k_tangent` reading is **not well posed over a range that includes zero stroke** — `CH-0047`.

---

## `Q1` — the placement clause, discharged exactly, and it is what makes the rest a comparison

| line | `t/s` at 3 nm | span [nm] | assembled secant at 3 nm | delivers at 10 nm |
|---|---|---|---|---|
| **L1** affine mandate (`C-0018`'s) | 1.0000 | — | 33.3333 | 333.3 pN |
| **L2** decoupled (`C-0028`) | **1.0952** | 31.063 | 33.3333 | 460.1 pN |
| **L3** coupled favourable (`C-0030`) | **0.7568** | 31.821 | 33.3333 | **298.1 pN** |
| **L4** coupled adverse (`C-0030`) | **1.3445** | 40.137 | 33.3333 | 657.6 pN |

- worst relative departure of the assembled secant from 33.3333: **1.998e−15**;
- **the located operating bias `V*` is identical across all four lines at 144 of 144 comparisons, to a departure of exactly `0.0`.**

> **So every difference below is in the CEILING and none of it is in the operating point.**
> That identity is not decoration: it is the reason a softening coupling can pass §3's force-and-stroke clause and fail its stability clause, and the reason the two must be checked separately.

---

## `Q3` — the fold, re-located: the headline is a STROKE, not a bias

### 10 nm layer, 2 mM `MgCl₂` — where `C-0018` found the only pull-in-bound design point

| model | `V*` [V] | `\|k_eff(3 nm)\|` | **L1** `V_pi` / `s_fold` / margin | **L3** `V_pi` / `s_fold` / margin |
|---|---|---|---|---|
| alexander-box(two-body) | 0.1568 | 27.91 | 0.1579 / 3.410 nm / **1.0071** | 0.1571 / **2.804 nm** / **1.0019** |
| alexander-box(virial) | 0.1789 | 23.41 | 0.1836 / 4.078 / 1.0264 | 0.1790 / 3.166 / **1.0009** |
| alexander-box(des-Cloizeaux) | 0.1804 | 24.90 | 0.1833 / 3.657 / 1.0160 | 0.1804 / 3.025 / **1.0000** |
| strong-stretching(two-body) | 0.1283 | 27.04 | 0.1300 / 3.578 / 1.0129 | 0.1285 / **2.838 nm** / 1.0012 |
| strong-stretching(virial) | 0.1367 | 23.80 | 0.1411 / 4.125 / 1.0317 | 0.1368 / 3.153 / 1.0008 |
| strong-stretching(des-Cloizeaux) | 0.1393 | 23.95 | 0.1432 / 3.952 / 1.0277 | 0.1394 / 3.126 / 1.0006 |

Three readings, and the third is the one that matters:

1. **The pull-in BIAS barely moves** — 0.1300–0.1836 → 0.1285–0.1804, a fall of 0.7–1.8 %. A reader watching only the bias would report a rounding error.
2. **The bias MARGIN collapses**, 1.007–1.032 → **1.0000–1.0019**. At `alexander-box(des-Cloizeaux)` the pull-in bias and the operating bias agree to four decimals: **the device is placed exactly at its own fold.**
3. **The fold's STROKE walks back 0.6–1.0 nm**, from 3.41–4.13 nm to 2.80–3.17 nm, and at **two of six models it passes through §3's 3 nm target** — so the target-stroke equilibrium exists, at a bias below the ceiling, and it is a **maximum** of the potential rather than a minimum. That is `C-0018`'s *second* test, the one it found binding only at 7 nm / 10 mM, now binding at the 10 nm design point.

> **`C-0018` warned that a bias below the pull-in bias is not sufficient and that the target stroke must also lie on the stable side of the fold. This claim is the first place both tests fail at the same design point, and the softening coupling is what did it.**

### Everywhere else, and the count

| line | states with a fold | **PASS** | lost against L1 | where |
|---|---|---|---|---|
| **L1** affine mandate | 11 of 54 | **28** | — | (reproduces `C-0018`'s 11 of 54 exactly) |
| **L2** decoupled, strain-**stiffening** | 8 of 54 | **28** | **0** | — |
| **L3** coupled favourable, strain-**softening** | **13 of 54** | **21** | **7** | 6 at 10 nm / 2 mM, 1 at 7 nm / 10 mM |
| **L4** coupled adverse | 6 of 54 | 22 | 6 | all on `CH-0047`'s artefact — see below |

> **`C-0017`'s theorem survives its own test.** The strain-stiffening element loses **nothing**, folds at **fewer** states (8 against 11), and takes the 10 nm / 2 mM margin from 1.007–1.032 to **1.020–1.774** — three of its six models lose the fold entirely and hand the ceiling back to `C-0002`'s `φ = 0.2`. **The theorem is right; `C-0030` moved outside its premise, which is exactly what `CH-0042` said.**

### 7 nm and 5 nm — unchanged, and for the reasons `C-0018` already gave

- **7 nm at 0.5 and 2 mM: `k_eff > 0` at the operating point, so the stability floor is exactly ZERO and no coupling stiffness is required at all.** All four lines pass at 12 of 12 states; the margin moves 1.19–2.46 → 1.17–2.44, i.e. by 1 %.
- **7 nm at 10 mM: both lines fail, and the softening one fails harder** — the fold sits at 1.72–2.52 nm against `C-0018`'s 1.92–2.68, so 6 of 6 models are past it against 5 of 6. 10 mM was already excluded by `C-0012`.
- **5 nm: validity-limited, exactly as `C-0018` reports** — 15 of 18 states have `V*` above `C-0002`'s `φ = 0.2` ceiling before any instability arises. The load line changes the margin by ≤ 1.5 % and no verdict.

---

## `Q2` — the reading `CH-0042` asked for, and the trap inside it

`CH-0042` requires the stability condition on `min_s k_tangent(s)` rather than at the working point. Both are computed:

| line | tangent at 3 nm | `min_s k_tangent` over **[0, 10] nm** | over **[3, 10] nm** |
|---|---|---|---|
| L1 affine | 33.3333 | 33.3333 (boundary) | 33.3333 (boundary) |
| L2 decoupled | 36.508 | **31.702 at s = 0 (boundary)** | 36.508 (boundary) |
| **L3 favourable** | 25.227 | **22.875 at s = 4.555 nm — INTERIOR** | **22.875 — INTERIOR** |
| L4 adverse | 44.817 | **23.515 at s = 0 (boundary)** | 44.817 (boundary) |

**Against the 2 mM floor at 10 nm (23.41–27.91), `L3` fails at 6 of 6 models on either range.** Against the 0.5 mM floor (3.86–15.94) it clears at 6 of 6, by **1.44–5.93×**.

> **But the `[0, 10]` reading is not well posed, and this claim raises a challenge against its own challenge.**
> At zero stroke `R = 0`, the layer carries nothing and the required bias is zero, so `k_es = k_brush = 0` and **the stability requirement is exactly zero there**. A strain-stiffening element's membrane term has not switched on at `s = 0`, so its `[0, 10]` minimum is always its bending stiffness alone — 31.70 for `L2`, 23.51 for `L4` — a number that describes the interval and not the element. It is what makes `L4` appear to fail `Q2` at 5 of 6 models when its tangent over the whole *used* range is 44.82.
> **The correct range is `[3, 10]`, and `CH-0042`'s observation survives it intact for the softening element** — because 22.875 at 4.555 nm is **interior**, which is precisely what `CH-0042` claimed and what no other line here has. See [`CH-0047`](../challenges/CH-0047-a-tangent-minimum-over-zero-stroke-is-not-a-requirement.md).

---

## `Q4` — the two margins, and they are still 10–40× apart

At 10 nm / 2 mM under the softening line:

| axis | quantity | value |
|---|---|---|
| **stiffness** | `min_s k_tangent / \|k_eff\|` | **0.820 – 0.977** (below one: the debt, realised) |
| **stiffness, at the working point** | `k_c(3 nm) / \|k_eff\|` | 0.904 – 1.078 |
| **bias** | `V_pull-in / V*` | **1.0000 – 1.0019** |

At 0.5 mM the same three are **1.44–5.93×**, **1.58–6.53×** and **1.038–2.327×**.

> **`C-0018`'s discipline holds and its direction reverses at the margin's own boundary**: a *comfortable* stiffness margin is a thin bias margin, and a stiffness margin **below one** is a bias margin of essentially exactly one. The bias axis is where a builder controls the device, and it is the axis on which this design has nothing left.

---

## `Q5` — the escape, priced honestly

`CH-0042` names one: give the flexure back its strain-stiffening by mounting it the **adverse** way. `C-0030` says that fails `C-0023`'s 40 pN/nm compliance ceiling at every length; this claim measures it across `C-0017`'s whole 3–10 nm envelope.

| `ℓ` [nm] | 3 | 4 | 5 | 6 | 7 | **8** | 9 | 10 |
|---|---|---|---|---|---|---|---|---|
| **adverse** tangent at 3 nm [pN/nm] | 61.04 | 55.63 | 51.65 | 48.72 | 46.52 | **44.82** | 43.47 | **42.38** |
| past the 40 pN/nm ceiling by | 1.526× | 1.391× | 1.291× | 1.218× | 1.163× | **1.120×** | 1.087× | **1.060×** |
| clears? | no | no | no | no | no | **no** | no | **no** |

> **0 of 8. There is no standoff length at which the adverse mounting clears `C-0023`'s ceiling**, and the *best* it reaches — 42.38 pN/nm at `ℓ = 10 nm`, the top of `C-0017`'s envelope — is still 6 % past it. The escape does not exist inside the design space.

### The third route, which no upstream claim names, and which lands 2 % short

The **favourable** mounting's tangent minimum is a function of the standoff length, and it is **not** monotone:

| `ℓ` [nm] | 3 | 4 | **5** | 6 | 7 | **8** | 9 | 10 |
|---|---|---|---|---|---|---|---|---|
| tangent at 3 nm | 47.77 | 33.97 | 27.84 | 25.64 | 25.09 | **25.23** | 25.62 | 26.11 |
| `t/s` | 1.433 | 1.019 | 0.835 | 0.769 | 0.753 | **0.757** | 0.769 | 0.783 |
| **`min_s k_tangent` [pN/nm]** | 27.66 | **28.71** | **27.30** | 25.54 | 24.04 | **22.87** | 22.02 | 21.40 |
| clears `C-0023`'s ceiling? | **no** | yes | yes | yes | yes | yes | yes | yes |
| `C-0030`'s window? | fail `P3` | fail `P4` | **PASS** | PASS | PASS | PASS | PASS | PASS |

> **Shortening the standoff to `ℓ = 5 nm` — the bottom of `C-0030`'s own window — buys 19 % of tangent minimum, 22.87 → 27.30 pN/nm.** That clears the 2 mM floor at **five of the six** layer models (23.41, 23.80, 23.95, 24.90, 27.04) and misses the sixth (27.91) by **2.2 %**. `ℓ = 4 nm` would clear all six at 28.71 — and it is excluded by `C-0030`'s `P4`, the beam's own tension against the 10 pN unzip allowable.
> **So the design space closes on 2 mM by a 2 % margin against one of six layer models, with the constraint that shuts the door being an unzip allowable two claims upstream.** That is worth naming as a task rather than a conclusion: `T-76a`.

---

## Does `CH-0042` stand?

**Yes, on its first horn, and this claim is the resolution it asked for.**

| `CH-0042` said | this claim finds |
|---|---|
| `t/s` falls 1.095 → 0.757, so the theorem's sign has flipped and the ratio is a **debt** | **confirmed and quantified**: the stiffening reading loses 0 of 54 states and the softening one loses 7 |
| the assembled tangent has an **interior** minimum of 22.88 pN/nm at 4.55 nm | **reproduced to 1e−3**, and it is the **only** line of the four whose minimum is interior |
| `\|k_eff\|` is 23.5–28.0, so the number stability is written on sits **inside** the requirement | **confirmed**: 23.41–27.91 re-derived here, and 22.875 is below all six |
| *"`\|k_eff\|` above 21.4 anywhere → the placement stiffness must be raised, which §3 forbids, or the flexure must be given back strain-stiffening — which means the adverse mounting, and that fails the compliance ceiling at every standoff length"* | **both horns hold.** 2 mM puts `\|k_eff\|` at 23.41–27.91, above 21.4 at every model; the adverse mounting is 1.06–1.53× past the ceiling at 0 of 8 lengths |
| *"If both horns hold, the branch is closed by a constraint pair that only exists once the joint is coupled"* | **the branch is not closed — it is moved.** 0.5 mM clears every predicate, and `ℓ = 5 nm` at 2 mM clears five of six. Neither was in `CH-0042`'s two-horn framing |
| the second half — the law is signed but **not odd**, so `C-0023`'s quadratic-well transfer is inexact | **not re-tested here**; `C-0030` closed `T-13` on the softer limb at 47–59× and this claim does not touch it |

**And one thing `CH-0042` got wrong, which this claim raises as a challenge in return:** its `min_s k_tangent` reading, taken over `[0, 10]`, makes the *stiffening* lines look worse than the softening one at their own zero-stroke boundary, where the requirement is identically zero. `CH-0047`.

---

## The five verification gates

Executed as **19 gate-named tests** in `src/test/kotlin/stability/SofteningCouplingStabilityTest.kt`; whole suite **`BUILD SUCCESSFUL`** on `tools/verify.sh`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a reaction over a stroke **is** the secant identically, at 5 strokes × 4 lines; doubling the path count multiplies reaction, secant **and** tangent by exactly 2; unphysical arguments throw (a secant at zero stroke, a negative stroke, a negative stiffness, a zero path count, a descending range) | **PASS** |
| **2 — limiting cases** | an affine line's secant, tangent and slope are one number and its tangent minimum is that slope **exactly**; a **preloaded** affine line's secant is not its tangent, which is why `C-0017`'s placement arithmetic is preload-free; all four lines deliver **100 pN at 3 nm** to 1e−6; the **sign** of `CH-0042`'s debt asserted directly (`t/s` > 1 decoupled and adverse, < 1 favourable); the softening line's minimum is **interior** and the stiffening one's is not; a **dead load** folds at the branch start; an affine line through the origin folds at the field's own decay length **independently of its stiffness** | **PASS** |
| **3 — symmetry and conservation** | see below | **PASS** |
| **4 — numerical convergence** | the tangent minimum is scan-step and bracket independent (64 → 4096 steps: **7.8e−16, 1.1e−15, 6.7e−16, 6.7e−16** against 8192); the fold on the real field is mesh independent (1000/2000/4000 nodes: **8.7e−6, 1.8e−6, 0.0**), coarse-scan independent (8/12/24: **2.9e−10, 8.2e−11, 0.0**) and bracket independent (1e−2 … 1e−6 nm: **7.9e−8 → 0.0**); the result file is **byte-identical** on two independent `tools/study.sh` runs | **PASS** |
| **5 — literature and upstream cross-check** | `C-0030`'s design reproduced (span **31.821** vs 31.82, tangent **25.227** vs 25.23, `t/s` **0.7568** vs 0.757, secant at 10 nm **29.81**, minimum **22.875** vs 22.88 at **4.555** vs 4.55 nm, 298.1 pN vs 298); `C-0028`'s decoupled design reproduced (span **31.063** vs 31.06, tangent **36.508** vs 36.51, `t/s` **1.0952** vs 1.095); `C-0030`'s adverse mounting reproduced (span **40.137** vs 40.14, tangent **44.817** vs 44.82); **`C-0018`'s pull-in band at 10 nm / 2 mM reproduced through this study's own pipeline** — 0.1300–0.1836 V against its published 0.130–0.184; **`C-0017`'s stability floors re-derived** — 23.41–27.91 at 2 mM and 3.86–15.94 at 0.5 mM, against its published identical values; **`C-0017`'s `V*` reproduced** to a worst departure of **2.417e−3**, which is its own published rounding | **PASS** |

### Gate 3 — three things that are not restatements of the construction

1. **The tangency identity on a NONLINEAR load line.** At every interior fold, `k_c(s_fold) + k_eff(s_fold) = 0` to a worst relative residual of **1.167e−5** over **38** interior folds — with `k_c` the element's own analytic tangent at that stroke (not a constant) and `k_es` a central difference of a full Poisson-Boltzmann re-solve at fixed applied bias, a route that shares no code with the path maximisation that located the fold. **0 of 216 folds are boundary maxima**, so no meaningless residual is reported.
2. **The placement identity, `Q1`.** The four lines locate the same operating bias at **144 of 144** comparisons, departure exactly **`0.0`** — asserted on the real field with the layer load carried, and separately on a synthetic field in the test suite.
3. **The closed-form fold of a synthetic exponential field.** `|F_es| = Aψ²e^{−h/λ}` has `V_eq(s) = √(R(s)e^{(L₀−s)/λ}/A)`, whose stationary point is `R′(s)/R(s) = 1/λ` — so the located fold is graded against a closed form rather than described, for **all three** nonlinear lines, and the affine line's fold is shown to sit at exactly `λ` whatever its stiffness. **The shape of the load line moves the fold in opposite directions for the softening and the stiffening element**, asserted as a product of two signed departures.

---

## Validity range

- **TRL 1–3. Nothing here is measured, and the MOTIF is not demonstrated either.** `C-0028`'s literature finding is upstream of every number: no duplex has been built standing normal to a single-layer sheet.
- **Mean field, inherited whole.** `C-0005`: the one-loop correction is **123–214 %** of the leading term across this gap range. **It is two orders larger than the 1.000–1.002 margin**, and the verdict is **NOT EXCLUDED**, never established. What survives that is the *comparison* — L1, L2, L3 and L4 are read on the identical field, so their differences are not exposed to it.
- **`L₀` is a FORCE-ONSET height** at a defining load of 1.0 pN over the tile (`C-0011`, `CH-0010`).
- **The layer is `C-0003`'s at `C-0001`'s single grafting density per height**, not `C-0011`'s solved profile — `C-0017`'s and `C-0018`'s own choice, so that the load line is drawn across the same characteristic. `C-0016` puts the solved layer 1.22× outside that bracket at 5 nm.
- **SMALL DEFLECTION, and the tangent minimum sits at its edge.** `C-0030` records the standoff head's rotation at the 10 nm stroke as 0.63–0.68 rad; the 3 nm placement point is inside small deflection, the **4.555 nm** minimum is at its edge, and the 10 nm secant is outside it. **A large-deflection solve would move the minimum and its direction is not known** — which is the single largest exposure of this claim's `Q2` reading.
- **The mounting sense is a SPECIFICATION GAP** (`C-0030`, `T-75`), and it is worth the whole of the difference between `L3` and `L4` here.
- **Static only.** `C-0004`'s drainage corner is 91 kHz–2.3 MHz, so the quasi-static reading is right below ~10 kHz; a bias step faster than drainage can carry the tile past a fold. **A softening coupling has a different dynamic signature from a stiffening one and that is not computed.**
- **No preload** (`T-13`). `C-0030`'s element is two-sided but its law is **not odd**, so a preload would not move its two limbs equally.
- **1-D.** No edge, no fringing, no lateral load profile (`T-3b`, `T-60`). The load line is the tile mean under a uniform load, the one case in which `C-0006`'s tile is rigid.
- **`C-0019`'s one-loop softening of `k_brush` and `C-0022`'s finite-tile enhancement are NOT carried**, exactly as `C-0018` did not carry them; `C-0027` reports they cancel at the fold to within the collar gradient's own difference-scheme spread, and that their net effect on the fold is **unresolved** (`T-60`).
- **The diffuse-layer drop is capped at 0.35 V**, the same bracket `C-0008`'s own Stern inversion uses.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| `C-0017`'s mandate | 33.3333 pN/nm | **CITED**, itself derived there from §3 alone. It is the placement target every line is solved to |
| `C-0017`'s stability floors at 10 nm | 23.41–27.91 / 3.86–15.94 pN/nm | **CITED, and re-derived here** as `\|k_eff\|` at the located operating point — a gate-5 check, not an input |
| `C-0018`'s pull-in band at 10 nm / 2 mM | 0.130–0.184 V | **CITED, and reproduced** through this study's own pipeline |
| `C-0030`'s design table | span 31.82, tangent 25.23, `t/s` 0.757, minimum 22.88 at 4.55 nm, adverse 44.82 | **CITED, and reproduced** as gate-5 tests |
| `C-0023`'s compliance ceiling | 40 pN/nm | **CITED.** It is what closes the escape, so `Q5` rests on it entirely |
| `C-0006`/`C-0024`'s unzip allowable | 10 pN | **CITED** via `C-0030`'s `P4`; it is what excludes `ℓ = 4 nm`, the one length that would clear 2 mM |
| duplex `EI`, `S` | 230 pN·nm² (**CanDo MODEL INPUT**), 1100 pN (**MEASURED**, Wang 1997) | **CITED** via `C-0009`/`Gen1Tile` |
| `C-0002`'s concentrated crossover | `φ = 0.2` | **CITED**, read as a ceiling. It binds at 41–48 of 54 states per line |
| Stern capacitance, Manning fraction, `ε_r`, `A₂`/`A₃`/`α` | 20 µF/cm², 11.90 %, 78, 1.9e−3 / 2.0e−2 / 0.49 | **CITED** via `C-0005`/`C-0008`/`C-0003` |
| §3's targets | 100 pN, 3 nm acceptable, 10 nm desired, 40 × 40 nm, 5/7/10 nm | **CITED** |

Everything else — every span, secant, tangent, tangent minimum, equilibrium path, fold, fold stroke, bias margin, stability floor, tangency residual and escape row — is **derived here in code**, with `C-0018`'s and `C-0030`'s pipelines **re-run rather than tabulated**.

## Still open — named, not answered

1. **`T-63` is a specification question, and this is the sixth independent route to it.** 0.5 mM is where this design works; §3 says 2 mM. No calculation closes it.
2. **`T-75` owns the mounting sense**, and it decides which of `L3` and `L4` the device has. `L4` fails the compliance ceiling, so the *favourable* mounting is still the only candidate — which means the softening is not escapable by that route.
3. **`T-76a` (new): the standoff length as a stability variable.** `ℓ = 5 nm` recovers 19 % of tangent minimum and clears five of six models at 2 mM; `ℓ = 4 nm` clears all six and is excluded by `C-0030`'s `P4`. Whether a slightly different span/count trade re-opens `ℓ = 4 nm` is a one-study question.
4. **The tangent minimum is read at the edge of small-deflection validity.** A large-deflection solve of `C-0030`'s element is the only thing that settles the 4.555 nm number, and it moves `Q2` in an unknown direction.
5. **The dynamic pull-in of a softening coupling is not computed.**
6. **`T-60`** — the 2-D field on the equilibrium path — is still the only thing that can move `C-0018`'s fold, and therefore this claim's floor, on the electrostatic side.

## Challenges

**Resolves [`CH-0042`](../challenges/CH-0042-a-strain-softening-coupling-has-no-free-stability-margin.md) — UPHELD.**
Both of its horns hold: `|k_eff|` is above 21.4 pN/nm at every model at 10 nm / 2 mM, and the adverse mounting fails `C-0023`'s ceiling at every standoff length. What its two-horn framing did not contain is the answer: the branch is **moved**, not closed — to 0.5 mM, and to the bottom of the standoff window.

**Raises [`CH-0047`](../challenges/CH-0047-a-tangent-minimum-over-zero-stroke-is-not-a-requirement.md)** against `CH-0042`'s own prescription that stability be read on `min_s k_tangent(s)` over the operating range: over a range that includes **zero stroke** the minimum of a strain-*stiffening* element is its bending stiffness alone, at a point where the stability requirement is identically zero, and the reading then ranks `L4` (tangent 44.82 over the used range) below `L3` (22.88). **No number in this claim's `L3` column moves** — its minimum is interior, at 4.555 nm, and is the same on both ranges — which is why the challenge is against the *method* and not against the verdict.

**None stands against this claim.** The four ways it would fail:

1. **A large-deflection solve of `C-0030`'s element moving the 4.555 nm tangent minimum upward past 27.91 pN/nm.** That is a 22 % move and it is not excluded; the 3 nm numbers would survive either way.
2. **A beyond-mean-field treatment moving `|k_eff|` at 10 nm.** `C-0005` puts the correction at 123–214 % and gives no direction for an oppositely charged divalent gap. It would not change the *comparison* between the four lines, which is read on one field.
3. **NDI specifying 0.5 mM**, in which case the whole of this claim's failure column is moot and the design stands at 1.44–5.93×.
4. **A demonstration that the standoff head and the beam end are not rigidly continuous** — `C-0030`'s failure route 2. `C12` would drop out, the law would revert to `C-0028`'s strain-stiffening one, and `C-0017`'s theorem would apply again with the margin it was banked at.
