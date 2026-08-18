# C-0110 — NDI's own objection to decision 2 is upheld, and it is worse than NDI stated: §3's 100 pN stops arriving at **13.75 nm** at 0.5 mM, so a 17–26 nm layer does not merely fail to buy device B — it loses device A as well, at 96 of 96 states

| | |
|---|---|
| **Task** | [`T-192`](../tasks/T-192-device-b-tall-gap.md) |
| **Leaf** | **`A8.2`** (the placement/stability half), with `A2.2` (the bias axis) and `A7.4` (the field) |
| **Verification type** | **in-silico** (`C-0008`'s nonlinear Poisson-Boltzmann gap solve and `C-0018`'s stroke-parametrised equilibrium path, both re-run as libraries at heights neither has ever been asked about) **+ logical** (a reachability threshold that needs no layer model at all, and an effort-point arithmetic that needs no solver) |
| **Verdict** | **PASS on `P1`, `P2`, `P3` and `P4`, and the answer is negative on every axis the task was opened to test.** Declared falsifier **`F2` fired** — and in the adverse direction, which is the finding. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED.** And the *layer* is worse off than that: **a 17–26 nm grafted PEG layer is outside every range this programme has established**, and the result file carries a nine-row per-quantity statement of exactly which and by how much. |
| **Provenance** | `gpd/results/T-192-device-b-tall-gap.json`, produced by `actuator.TallGapDeviceBStudyKt`; 30 reachability records, 6 reachability thresholds, 10 premise records, 60 layer records, 384 solved states, 9 validity departures, 15 convergence records, 16 upstream reproductions |
| **Conditions** | T = 300 K, `k_BT = 4.142 pN·nm`, `l_B = 0.7141 nm`, `ε_r = 78`; aqueous `MgCl₂` at **0.5 / 1 / 2 mM**; 40 × 40 nm Manning-renormalised tile; PEG layer at **17 / 20 / 23 / 26 nm** under **two** grafting-density rules; all six `C-0003` models; four load lines |
| **Consumes** | [`C-0008`](C-0008-electrostatic-force-and-decay-length.md) (the field, its solver, the three decay lengths, the Stern series), [`C-0018`](C-0018-maximum-usable-bias.md) (`EquilibriumPath`, the fold as `max_s V_eq(s)`, the ceiling taxonomy), [`C-0017`](C-0017-output-coupling-stiffness.md) (the mandate as an equality on a SUM, the stability floor), [`C-0046`](C-0046-fewer-longer-flexures.md) (`P10`'s `k_c = 10 pN/nm`, the `δ ≤ F/\|k_eff\|` cap), [`C-0050`](C-0050-desired-stroke-reach.md) (`s < L₀`, and the 16.63–26.12 nm escape table, **reproduced here to ≤ 2.0e−4**), [`C-0003`](C-0003-crossover-valid-layer-response.md)/[`C-0011`](C-0011-scf-density-profile.md), [`C-0002`](C-0002-peg-material-parameters.md), [`C-0005`](C-0005-mean-field-screening-validity.md) |
| **Raises** | [`CH-0126`](../challenges/CH-0126-a-tall-layer-breaks-section-3s-own-effort-point.md) and [`CH-0127`](../challenges/CH-0127-the-tall-layer-escape-is-kinematic-not-actuated.md), both against [`C-0050`](C-0050-desired-stroke-reach.md); and [`CH-0131`](../challenges/CH-0131-t-157-was-re-emitted-before-its-own-input.md) against [`C-0092`](C-0092-large-rotation-arm-branch.md) and [`C-0101`](C-0101-re-emitting-what-the-repair-moved.md), found while measuring whether this claim's own repair moved anything |
| **Repairs** | `actuator/PullInStability.kt` — a coarse-scan overshoot of three units in the last place, which killed a nine-minute sweep. All five `EquilibriumPath.fold` consumers re-emitted and measured; four byte-identical. See §7. |

---

## THE CONVENTIONS — read these before any number below

- `z` is normal to the electrode, positive **away** from it; **the electrostatic gap IS the layer height, exactly**.
- The **stroke** `s = L₀ − h` is positive **downward**, and `s < L₀` identically (`C-0050`).
- **`L₀` is a FORCE-ONSET height** (`C-0011`, `CH-0010`), not a first moment; the first moment of the same layer is 1.71–2.16× smaller (`C-0077`).
  **NDI's *"17-26 nm of polymer thickness"* is read here in the force-onset convention**, because §3 specifies a distance between two bodies.
  If NDI means a first moment, every gap in this claim is the wrong one by that factor — a **specification** question, recorded as open.
- `k_es = −∂F_z/∂h`, negative above the force maximum and positive below it (`CH-0011`).
- **A bias ceiling belongs to a `(bias, load line)` pair** (`CH-0015`), and four load lines are read:

  | line | `R(s)` | read at | what it is |
  |---|---|---|---|
  | **free** | `0` | `s = 10 nm` | the uncoupled tile — the reference a coupling has to beat |
  | **device-B** | `10 s` | `s = 10 nm` | `C-0046`'s `P10`, §3's **desired** clause on its own arithmetic |
  | **device-A** | `33.333 s` | `s = 3 nm` | `C-0017`'s mandate, §3's **acceptable** clause |
  | **dead-load** | `100 pN` | `s = 10 nm` | the constant-force load; passes through device-B's own point |

- **A layer height does not name a layer.** Two grafting-density rules are carried and **both are extrapolations**:
  **held-density** (`σ = 0.024 nm⁻²`, the §3 10 nm point's own — and the rule `C-0050`'s 16.63–26.12 nm table was itself computed at), and **§3-trend** (`σ = 2.03338696 · L₀^(−1.93683834)`, least squares through §3's three design points). They differ by **6.4948×** in `σ` at 26 nm.

---

## The claim, in one line

**NDI's stated reason for not having examined a 17–26 nm layer is right, the length it names is the right length, and the consequence is larger than the objection: measured on `C-0008`'s own solver, §3's 100 pN stops arriving across a gap of 13.75 nm at 0.5 mM, 11.92 nm at 1 mM and 10.18 nm at 2 mM — every one of them below the bottom of NDI's own band — so a 17–26 nm layer reaches §3's force target at 0 of 12 (gap, buffer) cells, admits device B at 1 of 96 swept states and one of six layer models, and refuses §3's *acceptable* clause at 96 of 96. The tall layer is not a trade of device A for device B: it loses both. What it does buy is exactly what `C-0050` priced it for and no more — the uncoupled tile reaches a 10 nm stroke at 52 of 96 tall states — so the escape is real in *displacement* and empty in *force*, and no claim had separated the two.**

---

## `P1` — the reachability bound, which is the answer to NDI

### The threshold, which is the number the objection actually needs

A grid of four heights says whether the force arrives at four places. The threshold says where it stops arriving, and it is one bisection:

| buffer | bulk `λ_D` | **deepest gap delivering 100 pN at 1.0 V** | at 1.23 V | in `λ_D` |
|---|---|---|---|---|
| **0.5 mM** | 7.8538 nm | **13.6989 nm** | **13.7498 nm** | 1.751 |
| 1 mM | 5.5534 nm | 11.8724 nm | 11.9215 nm | 2.147 |
| **2 mM** | 3.9269 nm | **10.1299 nm** | **10.1772 nm** | 2.592 |

&nbsp;&nbsp;&nbsp;&nbsp;**Every one is below NDI's 17 nm floor.** The reserve moves the threshold from 10.1299 to 13.6989 nm, a factor of **1.352** — and NDI's band *begins* **1.24×** beyond where the reserve leaves it and *ends* **1.90×** beyond.

Two things are worth reading off that table beyond its headline. First, **pushing from `CH-0007`'s 1.0 V point-ion boundary to `T-11`'s 1.23 V electrochemical bound is worth 0.05 nm** — the force saturates in bias because `C-0008`'s compact layer takes 88 % of anything above 2 V, so the whole electrochemical head-room buys nothing. Second, **the threshold in units of `λ_D` FALLS as the buffer is diluted**, 2.59 → 1.74, which is the prefactor loss showing through: diluting buys exponent and pays in amplitude.

### The four heights NDI named

`|F_es|` over the 40 × 40 nm footprint at the point-ion ceiling, and what §3 asks for:

| gap | 0.5 mM | 1 mM | 2 mM | §3 asks |
|---|---|---|---|---|
| 17 nm | **49.967079 pN** | 27.8999009 | 12.1745494 | 100 pN |
| 20 nm | 28.5521467 | 14.4713621 | 5.35517653 | 100 pN |
| 23 nm | 17.0885463 | 7.80167946 | 2.41591133 | 100 pN |
| 26 nm | **10.5739822** | 4.31579525 | **1.10569504** | 100 pN |

&nbsp;&nbsp;&nbsp;&nbsp;**0 of 12.** The best cell is 2.00× short and the worst is 90× short.

### And the other reading of *"across such a gap"*

§3 asks for 100 pN **at the stroke**, not at the resting height, so device B held at `L₀ − 10 nm` sits at a **7–16 nm** gap. That reading is tabulated too, and it is where the corner nearly survives:

| held gap (from `L₀` =) | 0.5 mM | 1 mM | 2 mM |
|---|---|---|---|
| 7 nm (17) | **0.0766 V** | 0.0895 V | 0.1128 V |
| 10 nm (20) | **0.1333 V** | 0.1868 V | 0.6795 V |
| 13 nm (23) | **0.3515 V** | not reached (73.77 pN at 1 V) | not reached (39.19) |
| 16 nm (26) | not reached (61.03 pN at 1 V) | not reached (35.14) | not reached (16.15) |

**So the device-B question was never *"can the field reach across 26 nm"*.** It is *"can the equilibrium path get from `s = 0`, where the gap is the full 17–26 nm, to `s = 10 nm`, where it is 7–16"* — and `P3` answers that.

---

## `P2` — which decay length governs, measured rather than asserted

`ℓ = −1/(d ln|F_es|/dh)`, central-differenced on the solve at fixed **applied** bias, 1.0 V:

| gap | 0.5 mM | 1 mM | 2 mM |
|---|---|---|---|
| 17 nm | 5.096 nm (**0.649** `λ_D`) | 4.401 (0.792) | 3.574 (**0.910**) |
| 20 nm | 5.620 (0.716) | 4.728 (0.851) | 3.721 (0.947) |
| 23 nm | 6.062 (0.772) | 4.973 (0.896) | 3.810 (0.970) |
| 26 nm | 6.430 (0.819) | 5.152 (0.928) | 3.862 (**0.983**) |

**`ℓ/λ_D` is a function of `κh` and not of `h`.** At 2 mM, where 17–26 nm is 4.33–6.62 Debye lengths, it is **0.910–0.983** and `C-0008`'s far-field limit of 1.0 is essentially reached: **NDI's bulk `λ_D` is exactly the right length there.** At 0.5 mM, where the same gaps are only 2.16–3.31 Debye lengths, it is **0.649–0.819** — the field decays *faster* than the bulk Debye length, so **dropping the buffer makes NDI's own estimate optimistic rather than conservative**.

> **Declared falsifier `F2` fired, in the adverse direction.** `T-192` declared that a departure above 10 % would mean this programme's *"the Debye length is three numbers"* answer applies at a tall gap. The departure is up to 35 % — and it makes the corner **worse**, not better. `F2` was written expecting the escape and got the opposite sign.

**The counterion-dominance answer does not rescue it, and it is not wrong — it is about the wrong quantity.** Computed at these gaps rather than transferred from the 5–10 nm box, the ratio is still **6.37 to 38.94**: the gap really is counterion-dominated at 17–26 nm. But the counterion screening length that follows, **1.54–1.91 nm**, is nowhere near the measured 3.6–6.4 nm decay. `C-0008` settled that at 5–10 nm; this settles it a gap decade further out, and `CH-0004`'s own escape clause fires again.

### What the reserve buys, measured

Dropping 2 mM to 0.5 mM is worth **4.10× to 9.56×** in `|F_es|` at 17–26 nm — a real and large gain, the exponent beating the fourfold prefactor loss. It is still 2.00–9.46× short. And the cleanest statement of the trade: **at 20 nm / 0.5 mM the gap is exactly as many Debye lengths as 10 nm / 2 mM, and the field there delivers 28.55 pN against 104.65 pN.**

---

## `P3` — device B, and the two devices it takes down with it

384 solved states: 4 heights × 2 density rules × 6 models × 2 buffers × 4 load lines.

| load line | ADMITTED | REFUSED | why it is refused |
|---|---|---|---|
| **free** (`R = 0`) | **52 of 96** | 44 | no bias holds the tile at a 10 nm stroke |
| **device-B** (`10 s`) | **1 of 96** | 95 | 56 no bias, 20 fold inside the stroke, 19 below the floor |
| **device-A** (`33.333 s`) | **0 of 96** | **96** | **all 96: no bias holds the tile at 3 nm at all** |
| **dead-load** (100 pN) | 0 of 96 | 96 | `k_c = 0` clears no positive floor, and the branch is empty at `s = 0` |

### The tall layer loses device A too, and that was not anticipated

A 3 nm stroke from a 17–26 nm layer leaves a **14–23 nm** gap, and the field cannot put 100 pN across it — the same threshold as `P1`, read at the acceptable clause. **§3's acceptable clause is delivered at 96 of 96 tall states by nothing.** A tall layer is not a trade of one device for the other.

### The stroke exists and the force does not

**The uncoupled tile reaches a 10 nm stroke at 52 of 96 tall states** — including 3 of 6 models at 26 nm in 0.5 mM. So a 17–26 nm layer *does* buy the kinematics `C-0050` priced it for, and only those. `C-0050`'s escape is **real in displacement and empty in force**, and no claim had separated the two. That is [`CH-0127`](../challenges/CH-0127-the-tall-layer-escape-is-kinematic-not-actuated.md).

### The single survivor, named — and it is a bracket disagreement

| | |
|---|---|
| state | **17 nm, held-density (`σ = 0.024`), 0.5 mM, `strong-stretching(virial)`** |
| operating bias `V*` at a 10 nm stroke | **0.167607 V** |
| `\|k_eff\|` there | **1.02367 pN/nm** |
| `C-0017` margin `k_c/floor` | **9.76875×** |
| fold | **none** |
| `C-0046`'s cap `δ ≤ F/\|k_eff\|` | 97.6875 nm |

**One state of 96, in one layer model of six.** The six models are `C-0003`'s own uncertainty and they do not agree that this state exists: at the same height and buffer, `alexander-box(two-body)` folds at a 5.83 nm stroke and `strong-stretching(two-body)` at 6.69. A survivor that lives in one limb of a declared bracket is a **bracket disagreement**, not a design.

### `|k_eff|`, the folds, and `C-0046`'s cap

- `|k_eff|` at the device-B operating point runs **1.024 to 59.30 pN/nm**, against `C-0046`'s 23.41–27.91 pN/nm at the 10 nm layer in 2 mM. The tall corner therefore **does** contain states softer than the floor `C-0046` refused at — 3 of the 40 gradable states clear it — and it contains states 2.1× harder as well.
- **Every one of the 20 folds inside the 10 nm stroke sits at 5.20 to 7.70 nm**, i.e. 0.52–0.77 of the demanded stroke. A fold at half the demand is not a margin question.
- `C-0046`'s composed cap `δ ≤ F/|k_eff|` is **1.686 to 97.69 nm** here against 3.58–4.27 nm at the 10 nm layer — so the *cap* is no longer what binds. What binds is that the field cannot reach.

### The tangency identity, as the independent grading route

`k_c + k_eff = 0` holds at an **interior** maximum of the equilibrium path and not at a boundary one. Over the **43** interior folds the scaled residual is **3.55e−8 to 1.54e−5**, computed by a finite difference of the field at fixed applied bias — a route numerically independent of the golden-section maximisation that located the fold.

---

## `P4` — the validity statement, loudly

Nine quantities, five of them above their established range, and **two reported precisely because they would be assumed to be departures and are not**:

| quantity | established | here | direction |
|---|---|---|---|
| **layer height `L₀`** | 5–10 nm; **no claim in this repository evaluates a taller one** | 16.9999–26.0 nm (the solved force-onset heights) | **ABOVE, 2.6×** |
| **§3's effort-point row** | ~20–25 nm above the electrode, reproduced exactly at §3's three heights (20 / 22 / 25 nm) | **32.0–41.0 nm**, and 27.0–36.0 nm with the lever bonded straight onto the tile | **ABOVE, 1.08–1.64×** |
| chain molar mass | 0.9–8.8 kDa at §3's points; `C-0002`'s 40 kDa swelling threshold | 28.04–80.00 kDa | ABOVE, and the §3-trend rule's dilute end **crosses** 40 kDa |
| grafting spacing | 3.30–6.45 nm | 6.45–16.45 nm | ABOVE at the trend rule, unchanged at held-density |
| volume fraction at rest | `C-0002`'s `φ = 0.2` ceiling | 0.0103–0.0542 | BELOW at rest; it binds under compression, so it is carried per state |
| **des Cloizeaux window `√(N_K/g_T)`** | empty at every Gen-1 chain | **0.409–0.691** | **still below 1 — the window is still EMPTY**, so the `9/4` exponent never starts and the des Cloizeaux limb stays a *fit* |
| **coil overlap `Σ = πR₀²σ`** | ≥ 1, the only criterion `CLAUDE.md` says bounds anything | **8.20–28.55** | **ABOVE 1 — the criterion HOLDS**, so a 1-D mean field is still licensed |
| stretching ratio `L₀/R₀` | the premise of every model, and `C-0003` records it is not large anywhere | 0.907–1.336 | unchanged in kind |
| mean-field electrostatics | `C-0005`: 123–214 % one-loop correction at 5–10 nm | it **falls** with the gap | **FAVOURABLE — the one axis on which the tall corner is better supported** |

### The cheapest departure of all, and it needs no solver

§3's own effort-point row puts the coupling's purchase *"~20–25 nm above the electrode"*, and at §3's three layer heights a 10 nm tile plus a 5 nm attachment reproduces that band **at both ends** — 20 / 22 / 25 nm, which is why `CLAUDE.md` records the row as fixing the standoff at 5 nm. At 17–26 nm the same stack puts the effort point at **32–41 nm**, and at **27–36 nm** even with the lever bonded straight onto the tile. **A 17–26 nm layer breaks §3's stack geometry before any field is solved.** It is arithmetic, it is a **specification** question rather than a modelling one, and no claim in this programme had noticed it. That is [`CH-0126`](../challenges/CH-0126-a-tall-layer-breaks-section-3s-own-effort-point.md).

### The grafting density is a specification gap

NDI's answer names a **thickness**; a grafted layer needs a **density**. The two rules carried here differ by **6.4948×** in `σ` at 26 nm and they disagree about the physics: the held-density rule keeps `φ(L₀)` at exactly the 10 nm design point's value — `φ = Nσv₀/L₀` is independent of `N` at fixed `σ` for a power-law interaction, and the emitted census confirms it to the digit (0.05422 at every height on the reference model) — while the trend rule dilutes to `φ = 0.0156` and chains of 80 kDa. **Which one NDI means is a question for NDI**, and it is the difference between a layer whose thermodynamics `C-0003` validated and one whose chains are past `C-0002`'s swelling threshold.

---

## The five verification gates

1. **Dimensional consistency** — the power-law fit recovers an exact power law in both amplitude and exponent; `δ ≤ F/|k_eff|` in nm from pN over pN/nm; `ℓ` recovered from `d ln|F|/dh` with the central-difference **separation named once** in one function; the counterion-dominance ratio dimensionless and reproduced from `1/(c·h)` exactly (`×2` per gap doubling, `×4` per buffer quartering).
2. **Limiting cases** — `ℓ → λ` exactly on a closed-form exponential field at four tall gaps; `holdingBias` returns **`null`** above the field's own ceiling rather than a clamped edge, and so does `tallGapDeepestReachableGap`; the held-density rule is constant in the height and the trend rule reproduces its own three §3 points to ≤ 6 %; a stable state returns **no** stability floor and therefore **no** cap, rather than an infinite margin.
3. **Symmetry and conservation** — `attraction == load` at every located branch point to 1e−6 relative; the tangency residual at all 43 interior folds, **3.55e−8 to 1.54e−5**, from an independent finite difference; the volume-fraction identity `φ = Nσv₀/h` recovered from the chain it was built from.
4. **Numerical convergence** — the Poisson-Boltzmann mesh at **a 20 nm gap**, where the graded mesh has the most work to do: 3.4e−5 / 6.9e−6 on `|F_es|` at 1000 / 2000 nodes against 4000. **Read on the decay length — a gradient of the same solve — it is 2.3e−5 / 4.6e−6**, which is *not* worse here and is reported because `CLAUDE.md` says it usually is. The difference step is 3.8e−7. The fold search's own two settings are **flat to the emitted precision** at both. Every departure is emitted at **two significant digits**.
5. **Literature cross-check** — `C-0008`'s bulk `λ_D` at three buffers (≤ 2.0e−4), its bias for a 100 pN blocking force at 5 / 7 / 10 nm (≤ 2.3e−3), its `|F_es|` at 15 nm and 2 V (1.4e−2 against a two-significant-figure published value), `C-0018`'s *"no fold at all at 10 nm in 0.5 mM"* (reproduced: **0 folds**), `C-0017`'s operating bias there (2.0e−2 against the mid-bracket), `C-0002`'s thermal blob (4.0e−4 against 1222 Kuhn segments), and **all six of `C-0050`'s escape heights to ≤ 2.0e−4**. The premises of every invoked scaling law are checked against PEG in water at the tall layer's own working volume fraction, in `premises[]`.

### Where the convergence axis had to be moved after the sweep

`CLAUDE.md`: *"a convergence axis cannot be read at a state where the quantity does not exist."* The fold axis was first written at a state named in advance — 20 nm, held-density, 0.5 mM — which has **no fold**, so it converged on `null` and reported `0` at every setting, silently. The axis's state is now selected **after** the sweep, from the device-B states that actually folded.

---

## §7 — the repair this task required

`actuator/PullInStability.kt`, `EquilibriumPath.fold`: the coarse scan evaluated `at(i * step)` with `step = strokeCeiling/coarseSteps`, and **`i * (X/n)` at `i == n` need not equal `X`**. On this task's `strokeCeiling = 25.144662445344164` nm the twelfth step landed at `25.144662445344167` — **three units in the last place above the ceiling** — and `at`'s own range `require` threw, killing a nine-minute sweep three quarters of the way through.

The repair is `at(minOf(i * step, strokeCeiling))`, and the same clamp on the golden-section bracket's upper end.

**The argument that it moves nothing is a proof**: `minOf(a, b)` returns `a` bit-identically whenever `a ≤ b`, and the only altered path is the one that previously *threw*, which therefore produced no result file to move. The bracket clamp is reachable only when `descent == coarseSteps`, which under the old code required the same scan point to have succeeded.

**And the proof was turned into a measurement, because `CLAUDE.md` says a proof about an invisible defect is not a check.** Every study in the repository that calls `EquilibriumPath.fold` was re-emitted:

| study | result file | re-run |
|---|---|---|
| `actuator.MaximumUsableBiasStudyKt` | `T-4-maximum-usable-bias.json` | **IDENTICAL** |
| `actuator.CollarEquilibriumPathStudyKt` | `T-60-collar-on-the-equilibrium-path.json` | **IDENTICAL** |
| `stability.SofteningCouplingStabilityStudyKt` | `T-76-softening-coupling-stability.json` | **IDENTICAL** |
| `stability.RecommendedElementFoldStudyKt` | `T-149-recommended-element-fold.json` | **IDENTICAL** |
| `stability.LargeRotationArmBranchStudyKt` | `T-157-large-rotation-arm-branch.json` | **17 fields moved** |

**The one that moved is not this repair, and it is a finding of its own.** A controlled A/B — an isolated copy of the tree with `PullInStability.kt` restored to `HEAD`, the same inputs, the same everything else — returns a `T-157` **byte-identical to the repaired run**. All 17 fields belong to the *input*: `T-157` reads `T-149` at run time, and `C-0101` re-emitted `T-157` **before** it re-emitted `T-149`, in the same commit. The committed `T-157` reproduces the **pre-`C-0101`** `T-149` margins digit for digit at all twelve rows. That is [`CH-0131`](../challenges/CH-0131-t-157-was-re-emitted-before-its-own-input.md), and `C-0092`'s *"the margins move by 1.0000–3.3380×"* is measuring a difference `C-0101` had already absorbed upstream — the correct reading is **1.0000 at all twelve rows**. The re-emitted file is retained, per `CLAUDE.md`'s rule that a stale file destroys the byte-for-byte re-run diff.

---

## Validity range

- **Mean field.** `C-0005`'s one-loop correction is 123–214 % of the leading term at 5–10 nm. It **falls** with the gap, so this is the one axis on which the tall corner is better supported. The **direction** of the correction for oppositely charged walls is still unknown and no claim is made.
- **Point ions**, except that `C-0008`'s Bikerman bracket is one-sided and **upward** (+0.8 % to +56 %). So every `|F_es|` here is a **lower** bound within mean field — the favourable direction for the corner, and the refusal survives it: +56 % on 49.97 pN is 78 pN against 100.
- **The layer is outside every established range above 10 nm.** See `P4` and `validityDepartures[]`.
- **The grafting density is not specified.** Two extrapolations; neither is a design.
- **`L₀` is a force-onset height.** If NDI means a first moment, every gap here is wrong by 1.71–2.16× (`C-0077`) — and in the direction that makes the corner **worse**, since the force-onset heights would then be 29–56 nm.
- **1-D.** The tile is **1.5–2.4 gap heights across** at 17–26 nm, against 4–13 at 5–10. `C-0022`/`C-0100`'s finite-tile collar is +14.7 % at 40 nm and a 10 nm gap and is **larger here**; it is **not carried**. Every force in this file is therefore a 1-D under-estimate by more than the amount `C-0100` measured — again the favourable direction, and again not enough: the shortfall is 2.00–9.46×.
- **No origami stability at low salt.** NDI's own answer to decision 3 prices 0.5 mM in folding yield, which this programme cannot see.
- **Nothing here is measured.**

## Numbers that are cited rather than derived

| number | value | what it moves |
|---|---|---|
| `ε_r` of water at 300 K | 78 | ~3 % on every force. Moves no verdict — the shortfall is 2–90×. |
| Stern capacitance | ~20 µF/cm² | **CITED FROM `C-0008`** and load-bearing for the bias mapping. **More** load-bearing at a tall gap than a short one, because the diffuse layer sees less of a hard-driven electrode. A 2× error here moves the 1.0 V ceiling, not the 13.75 nm threshold, which is read at a bias the force has already saturated in. |
| Manning surviving fraction | 11.90 % | **CITED FROM `C-0005`**. The tile is charge-saturated (`C-0008`), so a factor of three is 7 % in `σ_eff`. |
| `A₂ = 1.9e−3`, `A₃ = 2.0e−2` | mol·cm³/g² | **CITED FROM `C-0002`**, measured osmometry. |
| `C-0005`'s 1.46 nm correlation band, `CH-0007`'s 1.0 V, `T-11`'s 1.23 V | — | the ceiling list. |
| `C-0046`'s `P10` and its 23.41–27.91 pN/nm floor | — | **CITED FROM `C-0046`**, as the comparison this task exists to move. |
| NDI's answers to decisions 2 and 4, 2026-08-18 | — | the **specification** input that created this task, quoted verbatim in `T-192`. |

Everything else — the reachability thresholds, the measured decay lengths, the counterion ratios at these gaps, the two density rules, the layer census, the 384 solved states, the effort-point arithmetic and the validity table — is derived here.

## Still open — named, not answered

1. **Whether a 17–26 nm grafted PEG layer can be *grown* at either density** is a chemistry question this task does not touch, and it is the first thing a bench would ask.
2. **`C-0011`'s SCF layer has not been solved at 17–26 nm.** `C-0003`'s trial functions over-read the volume fraction by 4.15× at the 10 nm point; whether that persists at a taller layer is unmeasured. It would move the *layer* numbers and cannot move the reachability threshold, which contains no layer at all.
3. **`C-0022`'s finite-tile collar at a tall gap is not carried** and is larger there than anywhere it has been measured. Favourable for the force, unfavourable for the flatness.
4. **Whether NDI's *"17-26 nm of polymer thickness"* is a force-onset height or a first moment** — worth 1.71–2.16× in every gap here.
5. **The device-B coupling ELEMENT is not designed here.** `C-0046` places arms of 12.7–18.1 nm at the 10 nm layer; whether the same family places at a 17–26 nm layer is `C-0046`'s question, and the single surviving state does not need it answered to be a bracket disagreement.
