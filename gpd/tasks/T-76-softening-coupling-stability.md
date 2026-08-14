# T-76 — Does a strain-SOFTENING coupling still satisfy `C-0017`'s stability condition?

| | |
|---|---|
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with **`A2.2`** (the pull-in ceiling the stability condition is read against) |
| **Raised by** | [`CH-0042`](../challenges/CH-0042-a-strain-softening-coupling-has-no-free-stability-margin.md), from [`C-0030`](../claims/C-0030-coupled-standoff-joint.md) / [`T-65`](T-65-coupled-standoff-joint.md) |
| **Priority** | **high** — `C-0030`'s assembled tangent has a minimum of **22.88 pN/nm** inside the operating range and `C-0018`'s fold margins put `\|k_eff\|` at **23.5–28.0 pN/nm**. The number stability is now written on sits *inside* the requirement |
| **Verification type** | **in-silico** (`C-0018`'s equilibrium-path fold analysis re-run with `C-0030`'s **nonlinear** reaction law substituted for the affine `R = 33.333 s`, over the same `(height, model, buffer)` grid, graded against the tangency identity `k_c(s_fold) + k_eff(s_fold) = 0` computed by an independent finite difference of the field) **+ logical** (the placement clause fixes the *level* of the load line and the stability clause its *slope*; a softening element satisfies the first and can fail the second, and the two are checked separately) |

---

## Formulate

### The question, stated so it can fail

`C-0017` records a theorem and the programme has banked it four times:

> *"Placement is written on the coupling's SECANT and stability on its TANGENT,
> so a strain-stiffening element discharges both with one part
> and the whole `tangent/secant` ratio is free stability margin at zero placement cost."*

`C-0030` makes the flexure **strain-softening**: `t/s` falls from 1.095 to **0.757**,
and the assembled tangent is not even monotone —
it has an interior minimum of **22.88 pN/nm at a 4.55 nm stroke**.
The theorem's sign has flipped, so the ratio is a **debt** and not a bonus,
and the quantity the stability condition must be read on is a number no claim has computed.

**The question is not "is 22.88 bigger than 23.5".**
`C-0018` shows a pull-in ceiling is a property of a `(bias, load line)` pair,
and a *nonlinear* load line changes two things at once:

1. its **slope** at every stroke — which is the stability condition; and
2. its **level** at every stroke *other than the placement point* —
   the softening element delivers **298 pN** at the 10 nm stroke where the linear mandate delivers 460 pN,
   so the bias the tile needs to reach a given depth is **lower**, which moves the whole equilibrium path.

Only a genuine re-run of the fold sees both.
That is what this task does, and the `min_s k_tangent` reading `CH-0042` asks for
is reported beside it as the conservative summary it is.

### Units, geometry and sign conventions, locked before deriving

- Lengths **nm**, forces **pN**, moments **pN·nm**, stiffness **pN/nm** (= 1 mN/m exactly),
  pressure **pN/nm²** (= 1 MPa exactly), potential **V**;
  `k_BT = 4.141947 pN·nm` at **300 K**; aqueous `MgCl₂` at **0.5 / 2 / 10 mM**.
- `z` is normal to the electrode, positive **away** from it;
  **the electrostatic gap is the layer height, exactly** (`C-0012`'s convention, unchanged).
- The **stroke** `s = L₀ − h` is positive **downward**, toward the electrode.
  **`L₀` is a FORCE-ONSET height** (`C-0011`, `CH-0010`) at a defining load of 1.0 pN over the 40 × 40 nm tile.
- The **load line** `R(s)` is positive **upward**, i.e. resisting descent.
  Four are read, and **all four pass through the same operating point — 100 pN at 3 nm** —
  differing only in how they leave it.
- `k_es = −∂F_z/∂h`, **negative above the force maximum and positive below it** (`CH-0011`);
  every sign is quoted with the gap it applies to.
- `k_eff = k_brush + k_es`, and the actuator characteristic obeys `dW/dh = k_eff` exactly.
- **The coupled tangent along the equilibrium path is `k_c(s) + k_eff(s)`**, where `k_c(s) = dR/ds`
  is the load line's own **tangent at that stroke** — a constant for the affine lines and a
  function for `C-0030`'s. Its zero **is** the fold.
- The flexure's **mounting sense** is `C-0030`'s: **favourable** is the sense in which the midspan
  sags toward the body its standoff bases stand on (the supply of draw-in, and the softening one);
  **adverse** is the other one.

### The four load lines, declared before the run

Every one is 45 paths of `C-0030`'s element on base `B2` (two antiparallel crossovers laid **across**
the beam, `k_θb` = 261.2 pN·nm/rad) with an 8 nm standoff, its span **re-solved** so that the
assembled **secant** at the 3 nm acceptable stroke is exactly 33.3333 pN/nm.

| | line | `R(s)` | `t/s` at 3 nm | what it is |
|---|---|---|---|---|
| **L1** | **linear mandate** | `33.3333 s` | 1.000 | `C-0018`'s own coupled line — the reference the re-run is compared against, state by state |
| **L2** | **decoupled** (`C-0028`) | `C-0025`/`C-0028`'s beam | **1.095** | the strain-**stiffening** element `C-0017`'s theorem was banked on |
| **L3** | **coupled favourable** (`C-0030`) | the recommended design | **0.757** | the strain-**softening** element `CH-0042` is about |
| **L4** | **coupled adverse** (`C-0030`) | the other mounting | **1.344** | `CH-0042`'s named escape, which must be priced against `C-0023`'s ceiling |

### The acceptance predicates, declared before the run

| | predicate | threshold |
|---|---|---|
| **`Q1`** | **placement** — every line delivers §3's force at §3's stroke | assembled secant `= 33.3333 pN/nm` at 3 nm to `1e−6`, and the located operating bias `V*` **identical** across all four lines at every state |
| **`Q2`** | **stability, read the way `CH-0042` requires** | `min_s k_tangent(s)` over the operating range `>` `\|k_eff\|` at the operating point, at the state being quoted |
| **`Q3`** | **stability, read on the path** — the honest one | the fold of the equilibrium path under **that** line sits at a bias above `V*` **and** at a stroke deeper than §3's 3 nm |
| **`Q4`** | **the margin, on the axis the device is controlled on** | pull-in bias / operating bias, reported beside the stiffness margin, because the two differ by 10–40× (`C-0018`) |
| **`Q5`** | **the escape is priced** | if `L3` fails, `L4`'s assembled tangent is reported against `C-0023`'s 40 pN/nm compliance ceiling, at every standoff length in `C-0017`'s 3–10 nm envelope |

### The numeric target

Not a pass/fail but, at every one of the `3 heights × 6 models × 3 buffers × 4 load lines` states:
the pull-in bias, the fold stroke, `k_c(s_fold) + k_eff(s_fold)`, the bias margin,
and the verdict — **with the 2 mM and the 0.5 mM columns reported separately and plainly**,
because five independent routes already recommend 0.5 mM (`T-63`) and `C-0018` reports
no fold there at all.

---

## Plan

### The cheap bound, which runs first and decides whether the sweep is needed

Three comparisons, no code, on numbers `C-0017`, `C-0018` and `C-0030` already publish.

| | quantity | value |
|---|---|---|
| `C-0030`'s assembled tangent minimum over 0–10 nm | | **22.88 pN/nm** |
| `C-0017`'s stability floor `\|k_eff(3 nm)\|` at 10 nm, **2 mM** | | **23.41 – 27.91 pN/nm** |
| `C-0017`'s stability floor at 10 nm, **0.5 mM** | | **3.86 – 15.94 pN/nm** |

So the cheap bound says: **the softening element fails `Q2` at 2 mM at every one of the six models
(22.88 < 23.41) and clears it at 0.5 mM by 1.44–5.93×.**

**Falsifier 1 — the cheap bound clears 2 mM.** If `min_s k_tangent` had come out above 27.91 the
whole task would close on a division and the sweep would not be justified. It does not, by 2.3 %
at the best end, so the sweep runs.

**Falsifier 2 — the fold analysis reproduces the cheap bound.** If the re-run merely returns
"fails at 2 mM, passes at 0.5 mM" with nothing the division could not have said, the expensive
half of the method was not worth its cost, and the task file must say so rather than dress the
sweep up. What the sweep can add and the division cannot: the **level** effect (the softening line
is 35 % below the mandate at 10 nm, which moves the bias the fold occurs at), the **stroke** at
which the fold now sits (`Q3`'s second test, which `C-0018` shows is a different test from the
bias one and which already fails at 7 nm / 10 mM), and the margin on the **bias** axis, which
`C-0018` shows is 10–40× tighter than the stiffness margin.

### The method, justified against cost

**Re-use, do not re-implement.** `actuator/PullInStability.kt`'s `EquilibriumPath` already takes
the load as an arbitrary `(Double) -> Double`, so a nonlinear coupling is substituted without
touching `C-0018`'s solver at all — which is exactly what makes the comparison state-by-state
honest. `anchoring/CoupledStandoffJoint.kt` supplies the reaction law and its analytic tangent.
This task therefore owns **one new package** (`stability/`) holding the assembled-load-line
abstraction, the tangent-minimum search and the study, and edits **nothing** upstream.

**The traps this calculation is known to fall into**, each answered:

| trap (`CLAUDE.md`) | how it is answered |
|---|---|
| a pull-in bias cannot be bisected for | the path is parametrised by the **stroke** and the fold is `max_s V_eq(s)` — `C-0018`'s construction, re-used unchanged |
| a boundary maximum is not a stationary point | `foldAtBranchStart` is carried and **no tangency residual is reported** where the maximum is at an end |
| a stiffness margin is not a bias margin | **both** are reported at every state, and the ratio between them is a finding |
| a ceiling belongs to a `(bias, load line)` pair | four lines, every ceiling quoted with its own |
| `bracketedRoot`'s Illinois sign test underflows | not used; the diffuse-drop bisection exits on the **bracket width**, and the tangent minimum is a coarse scan plus golden section on the bracket |
| a golden-section extremum is floored by the noise underneath it | the tangent is **analytic** (`CoupledJointFlexure.tangentStiffness`, asserted against a central difference in `C-0030`), so the minimum is resolvable to the bracket and the convergence record measures it |
| a margin of `Infinity` is not a margin | where `k_eff > 0` there is **no** stability requirement; recorded as `null`, not as a number |
| `kotlinx.serialization` refuses `NaN`/`Infinity` | every optional field is nullable and every "no limit" routed through a finite sentinel |

**Cost.** 216 fold searches at `C-0018`'s own settings (12 coarse steps, golden section to 1e−4 nm,
2000-node Poisson-Boltzmann), against `C-0018`'s 162. Minutes, not hours, because the path is
parametrised by the **diffuse-layer drop** and never inverts the Stern series except for the
finite-difference `k_es` at the located fold — `C-0018`'s factor of ~35.

**What would falsify the approach itself.** If the located operating bias `V*` differed between
the four load lines at any state, the placement clause would not be what this task claims it is
and every comparison below would be between different devices. `Q1` asserts it as an identity to
be met exactly, not approximately.

### The five verification gates

| gate | what is checked |
|---|---|
| **1 — dimensional** | an assembled secant and tangent are `pN/nm`; doubling the path count doubles reaction, secant and tangent **exactly**; a reaction over a stroke is a secant identically; unphysical arguments throw |
| **2 — limiting cases** | an **affine** line's tangent equals its secant equals its slope at every stroke, and its tangent minimum is that slope exactly; a **decoupled** flexibility reproduces `C-0028`'s `t/s = 1.095`; the coupled favourable law has `t/s < 1` and the adverse `> 1` at the same design — the sign of `CH-0042`'s debt, asserted; a load line with an enormous stiffness admits no fold and one with zero stiffness is the free tile |
| **3 — symmetry and conservation** | **`Q1` as an identity**: the four lines deliver exactly 100 pN at 3 nm and therefore locate exactly the same `V*`; **the tangency identity on the nonlinear line** — at every interior fold `k_c(s_fold) + k_eff(s_fold) = 0`, with `k_c` from the analytic tangent and `k_es` from a central difference of a full field re-solve at fixed applied bias, two routes sharing no code |
| **4 — numerical convergence** | the tangent minimum is scan-step and bracket independent; the fold is mesh-, coarse-scan- and bracket-independent on **this** task's own quantity; the result file is byte-identical on two independent `tools/study.sh` runs |
| **5 — literature and upstream cross-check** | `C-0030`'s published design reproduced (span 31.82 nm, tangent 25.23, `t/s` 0.757, minimum 22.88 at 4.55 nm, secant at 10 nm 29.81, 298 pN, adverse tangent 44.82); `C-0018`'s own linear-line folds reproduced through this study's pipeline; `C-0017`'s stability floors at 10 nm re-derived |

---

## Verify

Executed as **19 gate-named tests** in `src/test/kotlin/stability/SofteningCouplingStabilityTest.kt`,
plus the study's own gate records. Full detail in [`C-0032`](../claims/C-0032-softening-coupling-stability.md).

| gate | result |
|---|---|
| **1 — dimensional** | **PASS.** Secant ≡ reaction/stroke at 5 strokes × 4 lines; doubling the path count multiplies reaction, secant and tangent by exactly 2; five classes of unphysical argument throw |
| **2 — limiting cases** | **PASS.** An affine line's secant = tangent = slope and its tangent minimum is that slope exactly; a preloaded affine line's secant ≠ its tangent; all four lines deliver 100 pN at 3 nm; the **sign** of `CH-0042`'s debt asserted (`t/s` > 1 decoupled and adverse, < 1 favourable); the softening line's minimum is **interior** and the stiffening one's is not; a dead load folds at the branch start; an affine line through the origin folds at the field's decay length independently of its stiffness |
| **3 — symmetry and conservation** | **PASS.** The tangency identity `k_c(s_fold) + k_eff(s_fold) = 0` to **1.167e−5** over 38 interior folds, `k_c` analytic and `k_es` from a central difference of a full field re-solve; the placement identity `V*` equal across the four lines at **144 of 144** comparisons, departure exactly `0.0`; a closed-form synthetic field whose fold obeys `R′(s)/R(s) = 1/λ`, graded for all three nonlinear lines |
| **4 — numerical convergence** | **PASS.** Tangent minimum scan-step independent to **6.7e−16** over 64 → 8192; the fold mesh independent (1000/2000/4000 → 8.7e−6, 1.8e−6, 0.0), coarse-scan independent (8/12/24 → 2.9e−10, 8.2e−11, 0.0) and bracket independent (1e−2 … 1e−6 nm → 7.9e−8 … 0.0); the result file **byte-identical** on two independent `tools/study.sh` runs |
| **5 — literature and upstream cross-check** | **PASS.** `C-0030`'s and `C-0028`'s design tables reproduced to ≤ 1e−3; `C-0018`'s pull-in band at 10 nm / 2 mM reproduced (0.1300–0.1836 V against 0.130–0.184); `C-0017`'s stability floors re-derived (23.41–27.91 / 3.86–15.94) and its `V*` reproduced to **2.417e−3**, its own published rounding |

### The declared falsifiers, and what happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | the cheap bound clearing 2 mM, closing the task on a division | **no** | 22.88 against a 23.41 floor — 2.3 % short at the best end, so the sweep ran |
| 2 | the sweep reproducing the cheap bound and nothing else | **no** | it added the **fold stroke** (the binding test, and a different one), the collapse of the bias margin to 1.0000, the confirmation of `C-0017`'s theorem in its own premise (0 states lost), and `CH-0047` |
| 3 | `V*` differing between the four lines, which would make the comparison meaningless | **no** | departure exactly `0.0` at 144 of 144 |

### The result the run was not expecting

**`CH-0042`'s own prescription is not well posed.** Minimising the tangent over `[0, 10 nm]` ranks the
*adverse* mounting (44.82 pN/nm over the range the device uses) below the *softening* one, because at
zero stroke the membrane term has not switched on — at a point where the stability requirement is
identically zero. That is [`CH-0047`](../challenges/CH-0047-a-tangent-minimum-over-zero-stroke-is-not-a-requirement.md),
and it moves no verdict in this task because the softening element's minimum is **interior**.
