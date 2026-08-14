# C-0049 — The 40 pN/nm ceiling is owed at the PLACEMENT stroke and nowhere else, because it is `1.2 ×` the placement mandate and carries that stroke inside it; the requirement that binds beyond the working point is the per-path allowable, which runs the other way — 45 pN/nm at 45 paths and **15** at 15 — and the same answer applied to the stability FLOOR moves `C-0030`'s element from 0 of 6 to **4 of 6** of `C-0017`'s own 2 mM floors

| | |
|---|---|
| **Task** | [`T-107`](../tasks/T-107-compliance-ceiling-stroke.md) |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*) |
| **Verification type** | **logical** (an audit of what `C-0017` and `C-0018` actually require of a coupling law, each requirement read with the stroke it is written at) **+ in-silico** (every requirement and every element's response re-derived from the owning claim's own library rather than tabulated) |
| **Verdict** | **PASS, and the answer is "at the working point" — but relaxing the ceiling does not rescue anything, because what replaces it beyond the working point is tighter.** 40 pN/nm is exactly `1.2 × (100 pN / 3 nm)`: a **declared linearity tolerance on the placement discharge**, and the *same construction* at §3's desired clause is **12 pN/nm, not 40** — so reading 40 at a 10 nm stroke is not conservative, it is the wrong clause's number, 3.33× too generous. **Neither `C-0017` nor `C-0018` contains any upper bound on a coupling tangent**: placement is an EQUALITY on the secant, stability a FLOOR on the tangent, and `C-0032` measures a *stiffer* tangent **raising** `C-0018`'s 10 nm / 2 mM pull-in margin from 1.007–1.032 to 1.020–1.774. The only genuine ceiling in the stack is `C-0006`'s **per-path unzip allowable**, and it is a bound on a **force**, so as a stiffness it is `n·allowable/s` — it **tightens as `1/s`** where the declared ceiling is constant: **150 → 45 pN/nm at 45 paths and 50 → 15 at `C-0041`'s buildable 15**. `C-0039`'s `E5a16` secant at 10 nm, 69.94 pN/nm, is past **both**, so dropping the declared ceiling moves the miss from **6.6× on a declared tolerance to 1.55× and 4.66× on a CITED allowable** and moves no verdict. **And the same reasoning settles `CH-0047`/`T-76b`, which is the same question on the other side**: a device placed at 3 nm traverses `[0, 3]` and never occupies the 4.556 nm stroke `CH-0042`'s minimum is taken at — read at the placement stroke `C-0030`'s favourable flexure is **25.227 pN/nm**, which clears **4 of `C-0017`'s 6** model floors at §3's own 2 mM, where the prescribed-range minimum 22.875 clears **none**. Raises [`CH-0062`](../challenges/CH-0062-the-buildable-hinge-count-clears-the-ceiling-on-the-elastica.md). |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED.** The ceiling itself is a **declared** design tolerance, never a measurement, and saying so precisely is most of this claim. |
| **Provenance** | `gpd/results/T-108-desired-stroke-reach.json`, produced by `synthesis.DesiredStrokeReachStudyKt`; **2 ceiling readings, 2 requirement censuses of 5 entries each, 2 stability-range records, 28 catalogue rows, 15 upstream reproductions, 3 convergence records**; **27 gate-named tests in `synthesis/DesiredStrokeReachTest`**, `tools/verify.sh` **BUILD SUCCESSFUL** on its own isolated tree with one concurrent agent's mid-TDD main source dropped by `--drop-file` (`src/main/kotlin/coupling/SingleColumnFlatnessStudy.kt`, `T-101`); the result file re-run through `tools/study.sh` and reported *"no result file changed"*, and diffed byte-for-byte identical |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **2 mM MgCl₂** (and 0.5 mM where a floor is read); 40 × 40 nm tile; §3's 100 pN at the **acceptable** 3 nm and the **desired** 10 nm; path counts 45 (`C-0015`) and 15 (`C-0041`); `EI` = 230 pN·nm², `S` = 1100 pN, `k_θ` = 13.5294 pN·nm/rad |
| **Consumes** | [`C-0017`](C-0017-output-coupling-stiffness.md) (`mandatedCouplingStiffness`, the placement chord, the stability floors, the secant/tangent theorem — **re-run as a library**), [`C-0018`](C-0018-maximum-usable-bias.md) (the pull-in margin, as the direction check), [`C-0023`](C-0023-two-sided-coupling.md) (the **declared** ceiling and its element catalogue, `flexureSpanForStiffness`, `hingeArmForStiffness`, `AxialDuplexStandoff`, `TransverseDuplexFlexure` — re-run), [`C-0030`](C-0030-coupled-standoff-joint.md) (`standoffTipFlexibility`, `coupledFlexureSpan`, `CoupledJointFlexure` — re-run), [`C-0032`](C-0032-softening-coupling-stability.md)/[`CH-0042`](../challenges/CH-0042-a-strain-softening-coupling-has-no-free-stability-margin.md)/[`CH-0047`](../challenges/CH-0047-a-tangent-minimum-over-zero-stroke-is-not-a-requirement.md) (the range question), [`C-0039`](C-0039-two-spring-elastica.md) (`TwoSpringElastica`, `elasticaArmForStiffness` — re-run), [`C-0040`](C-0040-hinge-line-census.md) (`hingeLineCensus`, `tileCrossoverInventory`), [`C-0041`](C-0041-flexure-array-packing.md) (`packingLimitedPathCount`), [`C-0006`](C-0006-tile-load-distribution-and-flatness.md)/[`CH-0029`](../challenges/CH-0029-the-48-pn-allowable-is-a-30-bp-number.md) (the 10 pN unzip allowable), [`C-0014`](C-0014-lateral-confinement.md) (`√(k_BT k)/n`), [`C-0009`](C-0009-discrete-lattice-tile.md)/`Gen1Tile` |
| **Raises** | [`CH-0062`](../challenges/CH-0062-the-buildable-hinge-count-clears-the-ceiling-on-the-elastica.md) against `C-0040` |

---

## The claim, in one line

**A ceiling written as a multiple of a mandate inherits the mandate's stroke, so 40 pN/nm belongs to §3's acceptable clause and 12 pN/nm to its desired one; the acceptance stack `C-0017` and `C-0018` actually define has no tangent ceiling at all — one equality, one floor, and one ceiling that lives on a per-path FORCE and therefore tightens with the stroke instead of staying put — and the same "read it over the strokes the device traverses" that answers the ceiling also answers `CH-0047`'s floor, where it is worth four of six model verdicts.**

---

## The cheap bound, which ran first and is the whole answer

`C-0023`'s ceiling is not derived anywhere; `T-23` declares it in its predicate `P2`.
Divide it by the thing it was declared against:

&nbsp;&nbsp;&nbsp;&nbsp;**`40 / (100 pN / 3 nm) = 1.2`, exactly.**

So the ceiling is `1.2 × mandatedCouplingStiffness(F, δ)` — and a mandate carries a stroke.
Evaluated at §3's other clause:

| §3 clause | mandate | declared ceiling `1.2 ×` | per-path ceiling, 45 paths | per-path ceiling, 15 paths |
|---|---|---|---|---|
| **acceptable — 100 pN at 3 nm** | **33.3333 pN/nm** | **40.0000** | **150.0** | **50.0** |
| **desired — 100 pN at ~10 nm** | **10.0000 pN/nm** | **12.0000** | **45.0** | **15.0** |

> **Reading 40 pN/nm at a 10 nm stroke is not the conservative choice; it is the wrong clause's number, and it is 3.33× too generous.**
> The declared ceiling *falls* with the stroke, because the mandate does.

Asserted as a gate-1 test at four `(force, stroke)` pairs, because it is an identity and not a coincidence.

---

## What `C-0017` and `C-0018` actually require, each with its stroke

The census the study emits, at 45 paths and §3's acceptable clause:

| | requirement | kind | quantity | read at | value | owner | derived here |
|---|---|---|---|---|---|---|---|
| 1 | **placement** | **EQUALITY** | secant | `s* = 3 nm` | **33.3333 pN/nm** | `C-0017` `P1` | yes |
| 2 | **static stability** | **FLOOR** | tangent | **`[0, s*]`** | 23.41–27.91 pN/nm | `C-0017`, `C-0018`, `C-0032` | cited |
| 3 | **per-path unzip allowable** | **CEILING** | secant | largest stroke traversed | **`n·allowable/s`** | `C-0006`, `CH-0029` | yes |
| 4 | per-anchor thermal force | CEILING | tangent | `[0, s*]` | 48 889 pN/nm | `C-0014` | yes |
| 5 | **declared compliance ceiling** | CEILING | tangent | **`s* = 3 nm`** | **40 pN/nm** | `C-0023`, **DECLARED** | yes |

Three things follow and none of them is a matter of taste.

1. **There is no tangent ceiling in `C-0017`.** Its `P1` is an equality on the secant and its stability condition is a floor on the tangent. Its own *"too stiff"* failures — `K1`, `K5`, `K6` — fail on **placement**, i.e. on the secant, because a stiffer secant puts the operating point short of 3 nm. `K1` is 297× the mandate and delivers 0.005 nm.
2. **`C-0018` runs the favourable way in stiffness.** `C-0032` re-ran its fold with a strain-**stiffening** load line and lost **0 of 54** states, raising the 10 nm / 2 mM bias margin from 1.007–1.032 to **1.020–1.774**. A stiffer tangent at fixed placement is a *benefit* to pull-in, not a cost.
3. **A linear coupling placed at the mandate is inside the declared ceiling at every stroke**, because secant = tangent = 33.333 < 40. **So the ceiling can only ever bind on a NONLINEAR law** — which is exactly what it was written to police: how far an element's tangent may exceed the secant it was placed on before the linear reading of its law stops being useful. That is a tolerance on the placement discharge, and it is owed where the placement is.

---

## What replaces it beyond the working point, and it runs the other way

The per-path allowable is a bound on a **force**, so converting it to a stiffness divides by the stroke:

&nbsp;&nbsp;&nbsp;&nbsp;**`k_secant ≤ n·allowable/s`**, &nbsp;and&nbsp; **`(n·allowable/s)·s = n·allowable` identically.**

That is `C-0023`'s own `F_req = k_req·σ` identity, one power of the **stroke** apart instead of one power of the position bound — asserted as a gate-3 test at five strokes × four path counts.

| stroke | 8 paths | 15 paths | 34 paths | 45 paths |
|---|---|---|---|---|
| 3 nm | 26.7 | **50.0** | 113.3 | **150.0** |
| 10 nm | 8.0 | **15.0** | 34.0 | **45.0** |

Applied to the element `T-107` was raised for, `C-0039`'s `E5a16` (arm 12.7198 nm, re-derived here to 2.7e−6):

| read at | secant | tangent | assembled force | per path | against the declared ceiling | against the per-path ceiling |
|---|---|---|---|---|---|---|
| **3 nm** | **33.3333** | **36.44** | 100.0 pN | 2.22 pN | **inside, 1.10× of margin** | inside, 4.5× |
| **10 nm** | **69.94** | **264.24** | **699.4 pN** | **15.54 pN** | 6.61× past *(a stroke it is not owed at)* | **1.55× past at 45 paths, 4.66× at 15** |

> **The verdict does not move; its owner does.** `C-0039` rejected `E5a16` at the desired stroke on a **declared** tolerance by 6.6×; the same element is rejected there by a **cited, measured-family** allowable by 1.55×, and by `C-0017`'s own placement arithmetic by 2.10× on the secant.
> A number that is 1.55× past a cited allowable is a weaker-looking rejection and a **stronger-warranted** one.

---

## The same answer, applied to the stability FLOOR — `CH-0047`, and it is the same question

`CH-0042` prescribed `min_s k_tangent(s)` over `[0, 10 nm]`; `CH-0047` showed that range is not well posed, because the requirement is identically zero at `s = 0`, and left the convention open for `T-76b`.
**It is `T-107`'s question on the other side of the inequality**, and it has `T-107`'s answer: **a requirement is owed over the strokes the device traverses, `[0, s*]`.**

`C-0030`'s favourable coupled flexure, re-run here as a library:

| paths | tangent at the placement stroke | `min` over `[0, s*] = [0, 3]` | `min` over `CH-0042`'s `[0, 10]` | argmin | floors cleared, of `C-0017`'s **six** at 10 nm / 2 mM |
|---|---|---|---|---|---|
| **45** | **25.227** | **25.227** | **22.875** | 4.556 nm | **4 of 6** at the placement stroke, **0 of 6** on the prescribed range |
| **15** | **25.490** | **25.490** | **22.507** | 5.066 nm | **4 of 6** against **0 of 6** |

The six floors are `C-0017`'s own, read from its result file: 27.913, 23.414, 24.904, 27.039, 23.804, 23.953 pN/nm.
`C-0030`'s element is strain-**softening**, so the minimum over the traversed range sits at its **upper** endpoint — the placement stroke — and `CH-0042`'s interior minimum at 4.556 nm is a stroke the placed device never occupies.

> **`C-0032`'s `Q2` verdict at 10 nm / 2 mM changes from "fails at 6 of 6" to "fails at 2 of 6", and the two survivors are the two-body models.**
> The 22.875 is reproduced here to 1e−4 from `C-0030`'s own library, so this is a re-reading and not a correction of anyone's arithmetic.
> **`C-0032`'s `Q3` is untouched**: the fold walking back through §3's 3 nm target is an independent test and it does not use a tangent minimum at all.
> This does **not** recover §3's 2 mM buffer — two models still fail, and `C-0032`'s bias margin of 1.0000–1.0019 is the binding statement — but it is the difference between *"no model admits it"* and *"the admissibility is model-dependent"*, and `T-63` is decided on exactly that distinction.

---

## The five verification gates

Executed as **27 gate-named tests** in `src/test/kotlin/synthesis/DesiredStrokeReachTest.kt` (shared with `C-0050`); `tools/verify.sh` **BUILD SUCCESSFUL, 0 failures**.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | the declared ceiling is linear in the force and inverse in the stroke, exactly as the mandate is, and reduces to the mandate at `factor = 1`; the per-path ceiling is a force over a length and scales linearly in the count; the thermal ceiling is `(n·allowable)²/k_BT`; unphysical arguments throw at five entry points, including a zero placement stroke | **PASS** |
| **2 — limiting cases** | **a linear coupling placed at the mandate is inside the ceiling at every one of five strokes spanning 60×**, which is the executable form of *"the ceiling can only bind on a nonlinear law"*; `min_s k_tangent` over the two ranges is shown to differ **exactly when the minimum is at an endpoint** — identical for an interior minimum (`C-0030`'s case) and 1.27× apart for a monotone-rising one (`C-0032`'s `L4`) — and degenerate over a point range | **PASS** |
| **3 — symmetry and conservation** | **`perPathSecantCeiling(a, n, s)·s = n·a` identically**, at five strokes × four path counts — the force/stiffness identity one power of the stroke apart; the placement predicate is an EQUALITY at the placement stroke and a FORCE clause (`≥`) beyond it, asserted at three secants; **the binding constraint is the first failing predicate in declaration order**, asserted with two simultaneous failures; a refusal (`C-0039`'s folded arm) is carried as a refusal and not as a zero | **PASS** |
| **4 — numerical convergence** | the tangent-minimum sampling 64 → 4096 moves `C-0030`'s interior minimum by **2.2e−5**; the elastica's own RK4 200 → 1600 moves its desired-stroke tangent by **2.3e−9**; the result file re-emitted through `tools/study.sh` reports *"no result file changed"* | **PASS** |
| **5 — literature and upstream** | 15 reproductions, worst departure **3.0e−4** and that against `C-0023`'s own three-digit 4.11 nm: the mandate (0.0), the ceiling (0.0), `C-0017`'s 297× over-stiffness, `C-0023`'s `E3a` span 24.61, `E3b` span 49.41 and tangent 91.13, `E5` arm 4.11, `C-0039`'s `E5a16` arm 12.7198 and its four stiffnesses, `C-0030`'s span 31.821 and tangent 25.227, `C-0040`'s four-crossover census, `C-0041`'s 15-path packing limit, and `C-0032`/`CH-0047`'s 22.875 at 4.555 nm | **PASS** |

### The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | an upper bound on the tangent inside `C-0017`/`C-0018` that is not the per-path allowable | **no** | one equality, one floor, one force ceiling, one thermal ceiling three orders from binding |
| 2 | the per-path secant ceiling at the desired stroke coming out above 40 pN/nm at every buildable count | **no** | 45 pN/nm at 45 paths and **15** at `C-0041`'s 15 |
| 3 | `E5a16` clearing every remaining requirement at 10 nm once the ceiling is dropped | **no** | secant 69.94 (2.10× the desired clause's own mandate) and 15.54 pN per path |
| 4 | the traversed-range reading of the floor changing no verdict | **fired the other way** | 0 of 6 → **4 of 6**, and it is the most consequential thing in this claim |

---

## Validity range

- **TRL 1–3. Nothing here is measured.** The ceiling is a **declared** design tolerance and this claim's main content is to say what it is a tolerance *on*.
- **The stability floors are `C-0017`'s, CITED and not recomputed** — they cost a Poisson-Boltzmann solve per state and nothing here moves them. They inherit `C-0005`'s 123–214 % one-loop correction, which is larger than the margin the four-of-six count turns on, so **the count is reported as a re-reading of `C-0017`'s own numbers and never as an establishment of stability at 2 mM**.
- **`[0, s*]` is the traversed range of a device whose operating point is at `s*`.** A design that deliberately drives past its placement point — a stroke-reserve design — traverses more, and then the ceiling *and* the floor are owed over the larger interval. **Which of the two §3 intends is a specification question**, and it is the same one [`CH-0059`](../challenges/CH-0059-the-desired-stroke-placement-is-below-the-stability-floor.md) raises from the other side.
- **The zero-bias rest sits `d` below `L₀`** (0.07–0.38 nm against a coupling, `C-0021`/`C-0023`), so the true lower endpoint is `−d` rather than 0. It does not move any verdict here — every element's tangent is continuous there — and it is not carried.
- **The catalogue's mechanics are each element's own**, re-run; the lattice and packing facts are `C-0040`'s and `C-0041`'s, re-run; the allowable and the floors are cited.
- **`C-0030`'s element is the only one in the catalogue with an interior tangent minimum**, so the range question has a numerical consequence for exactly one row. For every strain-stiffening element the two readings differ only through the zero-stroke endpoint, which is `CH-0047`'s own observation.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| the compliance ceiling | 40 pN/nm | **DECLARED** by `C-0023`/`T-23`, not measured — and re-derived here as `1.2 ×` the mandate |
| stability floors at 10 nm / 2 mM | 27.913, 23.414, 24.904, 27.039, 23.804, 23.953 pN/nm | **CITED**, `C-0017` (`gpd/results/T-16-…json`), each a mean-field solve |
| stability floor at 10 nm / 0.5 mM | 3.86–15.94 pN/nm | **CITED**, `C-0017` |
| per-path unzip allowable | 10 pN | **CITED**, `C-0006`/`CH-0029` |
| duplex `EI`, `S` | 230 pN·nm², 1100 pN | **CITED**; `EI` a **CanDo MODEL INPUT**, `S` **MEASURED** (Wang 1997) |
| crossover hinge `k_θ` | 13.5294 pN·nm/rad | **CITED, FITTED** (Chen 2014, via `C-0009`) |
| `A2` anchorage couple | 78.2353 pN·nm/rad | **CITED**, `C-0034` |
| §3 targets | 100 pN, 3 nm, ~10 nm, 40 × 40 nm, 2 mM | **CITED** |

Everything else — the ceiling's decomposition, the requirement census, the per-path secant ceiling and its identity, every element's secant, tangent, assembled force and per-path force at both strokes, and both stability-range readings — is **derived here in code**, with `C-0017`'s, `C-0023`'s, `C-0030`'s, `C-0039`'s, `C-0040`'s and `C-0041`'s pipelines **re-run rather than tabulated**.

## Still open — named, not answered

1. **Whether §3 intends a device *placed* at 10 nm or a device placed at 3 nm and *driven* to 10 nm.** They are different devices and the specification does not distinguish them — [`CH-0059`](../challenges/CH-0059-the-desired-stroke-placement-is-below-the-stability-floor.md), raised by `C-0046` in the same iteration, from the other side of the same gap. This claim answers *"which stroke is the ceiling owed at"* **for either reading**, because the ceiling follows the placement.
2. **Whether the declared 1.2 tolerance is the right number at all.** Nothing in this programme derives it. What this claim establishes is that it is a tolerance on linearity at the placement point; how big that tolerance should be is a design decision nobody has argued.
3. **The 4-of-6 count is inside `C-0005`'s inherited model error.** It re-reads `C-0017`'s floors; it does not narrow them.

## Challenges

**Raises [`CH-0062`](../challenges/CH-0062-the-buildable-hinge-count-clears-the-ceiling-on-the-elastica.md)** against `C-0040`'s clause *"at the one or two a flexure can actually own, the tangent is 42.0–54.1 pN/nm and even the acceptable stroke fails `C-0023`'s own ceiling"* — those numbers are computed on `C-0034`'s series composition, which `CH-0053` supersedes; on `C-0039`'s own exact elastica the same design places at **39.18 pN/nm, inside the ceiling**.

**None stands against this claim.** The three ways it would fail:

1. **A derivation of the 40 pN/nm from something other than the mandate** — then it has its own stroke and this whole claim is about the wrong object.
2. **A demonstration that §3's desired clause is a stroke-reserve requirement on a device placed at 3 nm**, in which case the traversed range is `[0, 10]` and both the ceiling and the floor are owed over it. It would not rescue `E5a16`: the per-path allowable, which is stroke-honest either way, still rejects it by 1.55×.
3. **A per-path allowable that is not a force** — e.g. a rate-dependent one read at a fixed stroke rather than a fixed loading rate. `CH-0029` already shows the allowable is a function of bonded length and loading rate; it is not a function of the stroke, which is what this claim uses.
