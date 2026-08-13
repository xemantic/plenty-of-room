# C-0018 — The maximum usable bias is a property of the load line, and for the device the programme decided to build it is a validity boundary everywhere except at 10 nm in 2 mM, where it is pull-in with 1–3 % of margin

| | |
|---|---|
| **Task** | [`T-4`](../tasks/T-4-maximum-usable-bias.md) |
| **Leaf** | **`A2.2`**, with `A7.4` (the field the softening comes from) |
| **Verification type** | **in-silico** (the equilibrium path of `C-0012`'s coupled balance parametrised by the **stroke**, its fold located as the maximum of the bias along it, graded against the tangency condition `k_c + k_eff = 0` computed independently by finite difference at fixed applied bias) **+ logical** |
| **Verdict** | **PASS on `P1`, `P2` and `P3`.** §6 task 4's first branch is delivered — a maximum usable bias with margin, at 162 states. Its *second* branch turns out to be true of the **unloaded** tile and of nothing else, and `CH-0011`'s mechanism for it is **refuted**: see `CH-0017`. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED.** And *within* mean field: `C-0005`'s one-loop correction is 123–214 % of the leading term across this gap range — larger than every margin below. |
| **Provenance** | `gpd/results/T-4-maximum-usable-bias.json`, produced by `actuator.MaximumUsableBiasStudyKt`; 162 ceiling records, 18 small-gap records, 324 arrest records, 24 upstream checks, 16 convergence records; **21 new gate-named `actuator` tests**; the result file re-run and diffed (see [`T-4`](../tasks/T-4-maximum-usable-bias.md#verify)) |
| **Conditions** | T = 300 K, `k_BT = 4.142 pN·nm`, `l_B = 0.7141 nm`, `ε_r = 78`; aqueous `MgCl₂` at **0.5 / 2 / 10 mM**; 40 × 40 nm Manning-renormalised tile; PEG layer 5 / 7 / 10 nm at `σ` = 0.092 / 0.045 / 0.024 nm⁻²; all six `C-0003` models; three load lines |
| **Consumes** | [`C-0012`](C-0012-coupled-stroke-and-blocking-force.md) (the characteristic, **re-run not tabulated**), [`C-0017`](C-0017-output-coupling-stiffness.md) (the coupling the device carries), [`C-0008`](C-0008-electrostatic-force-and-decay-length.md) (the field and its solver), [`C-0003`](C-0003-crossover-valid-layer-response.md) (the six layer models), [`C-0005`](C-0005-mean-field-screening-validity.md), [`C-0002`](C-0002-peg-material-parameters.md) |
| **Raises** | [`CH-0017`](../challenges/CH-0017-collapse-is-arrested-osmotically.md) against `CH-0011` |

---

## THE CONVENTIONS — read these before any number below

- `z` is normal to the electrode, positive **away** from it; **the electrostatic gap is the layer height, exactly**.
- The **stroke** `s = L₀ − h` is positive **downward**; **`L₀` is a FORCE-ONSET height** (`C-0011`, `CH-0010`).
- The **load line** `R(s)` is positive **upward**, and three are read: **free** (`R = 0`), **dead-load** (`R = 100 pN`) and **coupled** (`R = 33.333 s`, `C-0017`'s own mandated coupling). **The coupled and dead-load lines pass through the same operating point — 100 pN at 3 nm — and differ only in slope.**
- `k_es = −∂F_z/∂h`, **negative above the force maximum and positive below it**. Every sign below is quoted with **the gap it applies to**.
- **A bias ceiling belongs to a `(bias, load line)` pair, never to the bias alone.**

---

## The claim, in one line

**§6 task 4's deliverable is not one number but three ceilings on two axes, and once each is read at the operating point of the load line it belongs to, the static-stability ceiling — the one the task was named after — is the binding one at only 11 of 54 coupled states. For the device `C-0017` closed the programme on, the maximum usable bias is 0.097–0.425 V, it is set by `C-0002`'s `φ = 0.2` crossover almost everywhere, and the one place pull-in owns it is the 10 nm design point in 2 mM buffer, where the ceiling is 0.130–0.184 V against an operating bias of 0.128–0.180 V — a margin of 1.007 to 1.032, the thinnest in the programme. Dropping to 0.5 mM removes the fold entirely and hands the ceiling back to validity at 1.29–2.36× of margin.**

---

## `P1`/`P2` — the three ceilings, read at the operating point of each load line

### The headline: the coupled device (`C-0017`'s 33.333 pN/nm)

Six-model bracket at every cell. `V*` is the bias at which the device delivers §3's 100 pN at §3's 3 nm —
located by bisection here, and reproducing `C-0017`'s own table to 3.5e−3.

| `L₀` | buffer | `V*` [V] | pull-in [V] | **usable** [V] | binding ceiling | **margin** |
|---|---|---|---|---|---|---|
| **5 nm** | 0.5 mM | 0.148 – 0.318 | none | 0.097 – 0.229 | `φ = 0.2` | **0.56 – 1.09** |
| | 2 mM | 0.128 – 0.349 | none | 0.103 – 0.256 | `φ = 0.2` | **0.61 – 1.08** |
| | 10 mM | 0.155 – 0.583 | none | 0.134 – 0.423 | `φ = 0.2` | **0.73 – 1.06** |
| **7 nm** | 0.5 mM | 0.064 – 0.129 | none | 0.119 – 0.236 | `φ = 0.2` | 1.26 – 2.46 |
| | 2 mM | 0.083 – 0.157 | none | 0.117 – 0.262 | `φ = 0.2` | 1.19 – 2.17 |
| | 10 mM | 0.144 – 0.373 | 0.148 – 0.396 (5 of 6) | 0.148 – 0.396 | mixed | 1.00 – 1.44 |
| **10 nm** | 0.5 mM | 0.087 – 0.115 | none | 0.133 – 0.246 | `φ = 0.2` | **1.29 – 2.36** |
| | **2 mM** | 0.128 – 0.180 | **0.130 – 0.184 (6 of 6)** | **0.130 – 0.184** | **pull-in** | **1.007 – 1.032** |
| | 10 mM | not reached | none | 0.158 – 0.425 | `φ = 0.2` | — |

Four things follow, and only the first is what the task was called:

1. **Pull-in binds at 11 of 54 coupled states** — the 10 nm / 2 mM column and five of six models at 7 nm / 10 mM. At the other 43 the binding ceiling is `C-0002`'s concentrated crossover, which is a **validity** boundary and not a physical instability.
2. **Where pull-in does bind, the margin is 1.007–1.032.** That is `C-0017`'s 1.19–1.42× *stiffness* margin read on the bias axis, and the two are not the same size: `|k_eff|` grows steeply with bias near the fold, so a 19–42 % stiffness reserve is a **0.7–3.2 %** bias reserve. **A stiffness margin is not a bias margin, and quoting the comfortable one is the error this claim exists to prevent.**
3. **At 5 nm the margin is below 1 at 15 of 18 states.** The device's own operating point is *outside* `C-0002`'s range before any instability arises — which is `C-0017`'s own validity note (`φ(L₀−3 nm) = 0.186–0.332` at 5 nm) converted into a bias. 5 nm is not stability-limited; it is validity-limited, and it was already empty on `C-0016`'s window.
4. **The buffer is the lever, for the fourth time.** 0.5 mM removes the fold at 10 nm entirely and converts a 1.007–1.032 margin into 1.29–2.36. Leaf `A2.2`'s low-screening condition has now been vindicated by `C-0012` (force), `C-0016` (window), `C-0017` (stability floor) and here (usable bias).

### Beside it: the unloaded tile, and the load nobody should build

| load line | states with a fold | binding ceiling | usable bias [V] | operating point **not** usable | **no stable shallow branch at any bias** |
|---|---|---|---|---|---|
| **coupled** (`33.333 s`) | 11 of 54 | `φ = 0.2` in 43, pull-in in 11 | 0.097 – 0.425 | 20 of 54 | **0 of 54** |
| **dead-load** (`100 pN`) | 25 of 54, **all at the branch start** | `φ = 0.2` in 29, pull-in in 25 | 0.067 – 0.680 | 39 of 54 | **25 of 54** |
| **free** (`0`) | 5 of 54 | `φ = 0.2` in 49 | 0.085 – 0.595 | 18 of 54 | **0 of 54** |

Of the 20 coupled states whose operating point is not usable, **15 are at 5 nm** — five of the six models at all three buffers, where `V*`
exceeds the `φ = 0.2` ceiling — and **5 are 7 nm in 10 mM**, where the fold sits
at a stroke of **1.92–2.68 nm**, i.e. *shallower than §3's 3 nm*: the equilibrium at the target stroke exists,
at a bias below the ceiling, and it is a maximum of the potential rather than a minimum. **A bias below the
pull-in bias is not sufficient; the target stroke must also lie on the stable side of the fold, and those are
two different tests.** At 5 nm and 10 nm every fold is deeper than 3 nm and the second test is free.

- **The unloaded actuator has no pull-in at all at 49 of 54 states.** Its equilibrium path rises monotonically to the model's own floor: the layer's osmotic divergence removes the instability, which is exactly §6 task 4's *second* branch — **and it is true of the free tile only**. `C-0012`'s observation that only 4 of its 810 free points showed a second equilibrium is the same fact, and this claim supplies the reason.
- **The unloaded ceiling is 0.085–0.595 V, not 0.02–0.1 V.** `C-0012`'s figure came from the largest bias on its own grid that still sat inside validity; located by bisection at 2 mM the free ceiling is 0.087–0.246 V. `CH-0015` said this ceiling belongs to the unloaded tile; this claim says what it actually is, and against `C-0012`'s own `largestModelValidBias` of 0.05–0.10 V at 2 mM it is **1.7–2.5× larger**.
- **A dead load is the worst of the three and it fails in a different way.** Where it folds, it folds **at the branch start in 25 of 25 cases** — the path descends from zero stroke, so *no* compressed equilibrium is stable at *any* bias, and the "ceiling" degenerates to the bias at which the unstable branch meets zero stroke. That bias is **exactly `C-0008`'s blocking bias**: 0.0668 / 0.1128 / 0.6795 V at 5 / 7 / 10 nm in 2 mM, against `C-0008`'s published 0.067 / 0.113 / 0.679. **Two independent constructions landing on the same number is the sharpest cross-check in this claim**, and it converts `C-0012`'s "`k_eff < 0` at 428 of 810 held points" into a statement with no bias in it at all: *under a constant-force load the Gen-1 actuator has no stable compressed equilibrium, at any bias, at those states.*

### The ceiling that binds is not the one `C-0012` named

`C-0012` reported the free operating point leaving **three** ranges at once above ~0.1 V. Re-read at the operating point of each load line, **one** of them binds and the other two never do:

| range | binds at | why not |
|---|---|---|
| `C-0002`'s concentrated crossover, `φ = 0.2` | **121 of 162 states** | — |
| `C-0005`'s correlation band, gap `≥ 1.46 nm` | **0 of 162** | the layer reaches `φ = 0.2` at a gap of `5 × dry thickness`, = **1.63–3.32 nm**, which is always **above** 1.46 nm: the layer's own validity boundary is met first, everywhere |
| `CH-0007`'s point-ion boundary, 1.0 V | **0 of 162** | every ceiling here is below 0.43 V |
| `T-11`'s electrochemical window, 1.23 V | **0 of 162** | quoted as a bound; it is 2.9× above the largest ceiling in the box |

**So `T-11` stays closed and `T-6b` gains nothing**: the two ceilings that were feared are 2.9× and 2.3× above the ones that bind, and the binding one is a *polymer* boundary, not an electrostatic one.

---

## `P3` — `CH-0011`, settled as an executable check, and the mechanism half of it does not survive

`CH-0011` makes two statements. The first is upheld here as four assertions on the real Poisson-Boltzmann
solver — `src/test/kotlin/actuator/ElectrostaticStopperTest.kt`:

1. `k_es < 0` at 5, 7 and 10 nm at 0.25 V — **`C-0008`'s own result, reproduced**;
2. `|F_es|` is **not monotone**: it has a maximum, and that maximum is **below 3 nm**, i.e. inside the region `C-0008`'s sweep never entered;
3. `k_es` **changes sign there** — negative 0.3 nm above the maximum, positive 0.15 nm below it;
4. the force turns outright **repulsive** below a gap inside `C-0012`'s reported 0.55–1.58 nm band, and it is a genuine sign change: repulsive below, attractive above.

**`k_es < 0` everywhere is therefore false as written and true above the force maximum, and this is now asserted rather than described.**

The located force maximum runs **0.65 – 2.59 nm** over 18 (buffer, bias) states and `k_es` reverses at 15 of
them; at the other three the maximum has moved below the 0.35 nm sampled floor.

The second statement — *"the collapse **is** arrested, but by `k_es` reversing sign, not by the osmotic
divergence"* — is **refuted**, and that is [`CH-0017`](../challenges/CH-0017-collapse-is-arrested-osmotically.md).
Two counterfactuals, at 324 states:

| applied bias | osmotic stopper [nm] | electrostatic stopper [nm] | arrested by |
|---|---|---|---|
| 0.02 V | 3.24 – 8.54 | 0.74 – 1.62 | **osmotic**, 54 of 54 |
| 0.05 V | 2.17 – 5.47 | 0.42 – 0.75 | **osmotic**, 54 of 54 |
| 0.10 V | 1.35 – 3.38 | below the 0.35 nm floor | **osmotic**, 54 of 54 |
| 0.25 V | 0.67 – 1.92 | below the floor | **osmotic**, 54 of 54 |
| 0.50 V | 0.62 – 1.68 | below the floor | **osmotic**, 54 of 54 |
| 1.00 V | 0.62 – 1.68 | below the floor | **osmotic**, 54 of 54 |

**324 of 324, by a factor of 1.9 to 5 in gap.** The tile *does* pass the force maximum before it stops — at
1.0 V and 2 mM the maximum is at 1.75 nm and the stop at 0.62–1.68 nm, so `k_es > 0` at the arrest point,
which is `C-0012`'s 386-of-810 — but **passing the point where a force stops growing is not being stopped by
it**. The reversal changes the *stiffness* at the arrest; the *position* of the arrest is the layer's.
**§6 task 4's second branch is therefore answered YES, for the free tile, with §1's own mechanism.**

---

## Validity range

- **TRL 1–3. Nothing here is measured.**
- **Mean field, inherited whole.** `C-0005`: the one-loop correction is **123–214 %** of the leading term across the whole gap range for Mg²⁺, and for the *oppositely charged* tile-electrode pair no published result gives even the direction. **It is larger than every margin in this claim**, and the 1.007–1.032 margin at 10 nm / 2 mM is two orders below it. That verdict is **NOT EXCLUDED**, never established.
- **`L₀` is a FORCE-ONSET height** at a defining load of 1.0 pN over the tile. A bench reading these numbers in the first-moment convention would be off by 1.71–2.16× in thickness.
- **The layer is `C-0003`'s at `C-0001`'s single grafting density per height**, not `C-0011`'s solved profile — `C-0017`'s choice, for `C-0017`'s reason: the load line must be drawn across the same characteristic `C-0012` computed. `C-0016` puts the solved layer 1.22× outside `C-0003`'s bracket at 5 nm.
- **The `φ = 0.2` ceiling is a property of the layer you mean.** It is computed here at `C-0012`'s own design points, where `φ(L₀−3 nm)` is 0.19–0.33 / 0.08–0.14 / 0.05–0.08 at 5 / 7 / 10 nm (`C-0017`'s numbers). `CH-0015` quotes 0.031 / 0.019 / 0.090 for the **densest layer `C-0016`'s window admits**, which is a different grafting density and a different number; the two are not comparable and neither is wrong.
- **The fold is located on the FIRST descent of the path.** A later maximum is a state the device reaches only after having folded.
- **The load lines are AFFINE.** `C-0017`'s real coupling strain-stiffens with a tangent-over-secant of 1.17, which *raises* the fold, so the linear line is the conservative one and the margins here are lower bounds in that respect.
- **No preload** (`T-13`), **no dynamics**, **no 2-D field** (`T-3b`), **no lateral coordinate** (`T-12`: the layer's lateral stiffness is exactly zero by symmetry, so nothing here says anything about lateral stability).
- **The diffuse-layer drop is capped at 0.35 V**, the same bracket `C-0008`'s own Stern inversion uses. A state needing more is reported as a branch end, not extrapolated.
- **The small-gap diagnostics reach to 0.35 nm**, which is below `C-0005`'s 1.46 nm correlation band and above `C-0002`'s `φ = 0.2`. **The sign of `k_es` there is a statement about the model, not about the device** — `CH-0011`'s own words, and they apply to this claim's refutation of it just as much.

## Numbers that are CITED rather than DERIVED

| number | value | why it is cited, and what it moves |
|---|---|---|
| `C-0017`'s mandated coupling | 33.333 pN/nm | **CITED**, itself derived there from §3 alone. It is the slope of the coupled load line and nothing else depends on it |
| `C-0005`'s correlation band | 1.46 nm | **CITED**, used as a validity floor. It never binds |
| `C-0002`'s concentrated crossover | `φ = 0.2` | **CITED**, read as a ceiling per §2's second caveat. **It is the binding ceiling at 121 of 162 states**, so this claim is more sensitive to it than to anything else it cites |
| `CH-0007`'s point-ion boundary | ~1.0 V applied | **CITED.** Never binds |
| `T-11`'s electrochemical window | 1.23 V | **CITED**, quoted as a bound. Never binds |
| Stern capacitance | ~20 µF/cm² | **CITED**, and load-bearing: the whole diffuse-drop-to-applied-bias mapping rests on it |
| Manning surviving fraction | 11.90 % | **CITED FROM `C-0005`** via `C-0008`; the tile is charge-saturated, so a factor of three is 7 % in `σ_eff` |
| `A₂`, `A₃`, `α` | 1.9e−3, 2.0e−2, 0.49 | **CITED FROM `C-0003`/`C-0002`** |
| `ε_r` of water at 300 K | 78 | **CITED**; ~3 % on `F_es`, moves no verdict |
| §3's targets | 100 pN, 3 nm, 5/7/10 nm, 2 V | **CITED** |

Everything else — the equilibrium path, every fold, every validity bias, every margin, the force maximum,
the repulsion onset, the osmotic counterfactual and the arrest verdict — is **derived here**, with
`C-0012`'s and `C-0008`'s pipelines re-run rather than tabulated.

## Cross-checks passed

### Gate 3 — the tangency identity, which is what licenses the whole construction

At every **interior** fold the coupled tangent vanishes: `k_c + k_eff = 0` to a worst relative residual of
**9.40e−6** over the 16 interior folds, with `k_es` taken by central difference of the full re-solve at fixed
applied bias — a route that shares no code with the path maximisation that located the fold. The other 25
folds are **boundary** maxima (the dead-load line, descending from zero stroke), where the derivative need not
vanish and no residual is reported rather than a meaningless one.

### Gate 5 — the upstream reproductions, all by re-running rather than copying

| quantity | here | upstream | departure |
|---|---|---|---|
| bias for a 100 pN blocking force, 2 mM, 5 / 7 / 10 nm | 0.0668 / 0.1128 / 0.6795 V | `C-0008`: 0.067 / 0.113 / 0.679 | ≤ **2.3e−3** |
| `\|F_es\|` at 0.10 V, 2 mM, 5 / 7 / 10 nm | 167.17 / 86.70 / 34.46 pN | `C-0012`: 167.2 / 86.7 / 34.5 | ≤ **1.1e−3** |
| `V*` where `W(3 nm) = 100 pN`, 2 mM, six-model bracket | 0.1276–0.3491 / 0.0831–0.1567 / 0.1283–0.1804 V | `C-0017`: 0.128–0.349 / 0.083–0.157 / 0.128–0.180 | ≤ **3.5e−3** |
| `V*` on the coupled line against `V*` on the dead-load line | — | identity | **exactly 0.0** over 48 records |

Every departure above is the size of the *rounding* in the published table it is checked against.

Full gate table, including the two that were re-derived after the first run:
[`T-4`](../tasks/T-4-maximum-usable-bias.md#verify).

## Still open — named, not answered

1. **The 1.007–1.032 margin at 10 nm / 2 mM is two orders inside its own inherited uncertainty.** Nothing in this task shrinks it. **0.5 mM is the operating point this claim would recommend**, and §3 does not name it — the same recommendation `C-0017` made, now on a second axis.
2. **The `φ = 0.2` ceiling is doing most of the work and it is a cited number read as a ceiling.** `C-0002` gives 0.2–0.3; the floor of that band is used. Moving it to 0.3 would raise 121 of 162 ceilings.
3. **The sub-1.5 nm region is a model statement, not a device statement** — inside `C-0005`'s correlation band and above `C-0002`'s crossover. `CH-0017`'s refutation of `CH-0011` inherits that limitation in full, and says so.
4. **Static only.** A bias step faster than drainage can carry the tile past a fold a quasi-static ramp stops at. `C-0004`'s corner is 91 kHz–2.3 MHz, so the quasi-static reading is right below ~10 kHz; the dynamic pull-in is not computed.
5. **No preload** (`T-13`) and **no strain-stiffening load line**: both move the fold, in opposite directions.
