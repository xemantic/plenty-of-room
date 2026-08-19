# C-0091 — **The six independent routes to 0.5 mM are THREE.** One is withdrawn, **two are the other four read again** — `T-2` carries `T-3`'s own blocking bias at 15 of 15 states and `T-25` carries `T-16`'s and `T-4`'s extrema at 20 of 20 — and of the three survivors one holds on a **different ground** and one is quoted at a stroke the device never occupies. **The recommendation is unchanged and every number behind it is smaller than the deliverable says**

| | |
|---|---|
| **Task** | [`T-156`](../tasks/T-156.md) — *"how many of the six 0.5 mM routes are read on withdrawn objects?"* |
| **Leaf** | **`A2.2`** (*"electrostatic softening and pull-in: the maximum usable bias with margin"*), with `A8.2` for the coupling law two of the six are read on |
| **Verification type** | **logical** (a census over six claims, every figure re-derived from the emitting study's own result file rather than transcribed) **+ in-silico** (the arithmetic that re-reads two of the six on `C-0069`'s `Q5`) |
| **Verdict** | **PASS on `P1`–`P5`, and the count moves: six named routes are 1 withdrawn, 2 transfers and 3 independent survivors.** `C-0032`'s route is withdrawn (`CH-0098`, already recorded). **`C-0016` and `C-0027` are not routes**: `T-2`'s `biasClauses[].biasForHundredPiconewtonBlocking` is `T-3`'s own number at **15 of 15** `(height, buffer)` states at a worst departure of **0.0**, and `T-25`'s `bufferComparison` carries `T-16`'s `stabilityMargin` extrema and `T-4`'s coupled `margin` extrema at **20 of 20** comparisons at a worst departure of **2.66e−8**, which is `T-25` printing eight significant digits where `T-16` prints nine. The three survivors are `C-0012` (the force clause), `C-0017` (the static stability floor) and `C-0018` (the coupled bias margin). **All three favour 0.5 mM at every layer model** — `F1` did not fire — but **two of them are weaker than they read**: `C-0018`'s stated ground, *"0.5 mM removes the fold entirely"*, is **void** on an element that has no fold at 2 mM to remove, and survives only as a preference (**1.8706 against 1.3877, a factor of 1.3480**); and `C-0012`'s **4.9656×** is a **zero-stroke** blocking-bias ratio — at the held operating point the device occupies it is **1.4823–1.5703×**, an overstatement of **3.1621–3.3499×**. **Read at the state the device occupies, the three advantages are 1.57, 1.75 and 1.35, not 4.97.** |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED.** A census inherits every validity range of the claims it counts and narrows none. And the three survivors are **common mode**: all are downstream of `C-0008`'s single mean-field Poisson-Boltzmann model, whose one-loop correction is **123–214 %** of the leading term (`C-0005`) — larger than all three advantages. **Three routes are not three exposures.** (**RESTATED, `CH-0167`/`C-0137`**: this is an error bar on a LEVEL, and a margin read at a force-pinned operating point is not a level — the same-kind thresholds are a force **1.48–2.22×** smaller or a decay length **9.73 %** shorter.) |
| **Provenance** | `gpd/results/T-156-buffer-route-census.json`, produced by `synthesis.BufferRouteCensusStudyKt`; model in `src/main/kotlin/synthesis/BufferRouteCensus.kt`, readers in `src/main/kotlin/synthesis/BufferRouteCensusInputs.kt`; **6 route records, 15 blocking-bias transfer checks, 20 corrected-margin transfer checks, 12 floor records, 6 held-force records, 3 fixed-bias counter-readings, 3 falsifiers**; **17 gate-named tests in `src/test/kotlin/synthesis/BufferRouteCensusTest.kt`**; the result file re-run through `tools/study.sh` and diffed **byte-for-byte identical**; `tools/result-reader-census.py --emit` re-run; `tools/verify.sh` **exit 0, `BUILD SUCCESSFUL in 18m`** on the whole working tree with nothing dropped, including its result-reader census (45 self-tests, 82 studies) and Markdown-table gates (0 defects in 286 files) |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous **MgCl₂ at 0.5 and 2.0 mM**; the **recommended device** — 40 × 40 nm tile, PEG layer **10 nm** at `σ` = 0.024 nm⁻², placed at 100 pN over §3's **acceptable** 3 nm stroke; all six `C-0003` layer models; the recommended element is `C-0069`'s `Q5`, whose tangent minimum over the traversed `[0, 3 nm]` is **30.028762 pN/nm** against the mandated secant **33.3333333** |
| **Consumes** | [`C-0012`](C-0012-coupled-stroke-and-blocking-force.md), [`C-0016`](C-0016-design-window.md), [`C-0017`](C-0017-output-coupling-stiffness.md), [`C-0018`](C-0018-maximum-usable-bias.md), [`C-0027`](C-0027-window-resynthesis.md), [`C-0032`](C-0032-softening-coupling-stability.md) — the six routes, **read from their own result files**; [`C-0084`](C-0084-recommended-element-pull-in-fold.md) (the recommended arm's law and its solved margins); [`C-0049`](C-0049-compliance-ceiling-stroke.md)/[`CH-0042`](../challenges/CH-0042-a-strain-softening-coupling-has-no-free-stability-margin.md) (stability is owed on the **tangent** over `[0, s*]`); [`C-0071`](C-0071-output-element-recommendation.md)/[`C-0069`](C-0069-output-element-placement.md)/[`CH-0081`](../challenges/CH-0081-a-rigid-root-demands-a-longer-arm-than-the-plan-admits.md) (what removed `C-0030`'s flexure); [`C-0064`](C-0064-robust-distribution.md)/[`C-0068`](C-0068-range-robust-placement.md) (a state is a device); [`C-0005`](C-0005-mean-field-screening-validity.md) (the common-mode error) |
| **Resolves** | [`CH-0098`](../challenges/CH-0098-the-0-5-mM-requirement-is-quoted-for-a-withdrawn-coupling.md) item 3 — *"a count of how many of the six routes are read on withdrawn objects. That census has never been taken."* **Taken.** The challenge's own two other items were discharged by the coordinator in iteration 17 |
| **Raises** | [`CH-0106`](../challenges/CH-0106-six-routes-are-three.md) against the *"six independent routes"* sentence as it stands in `DECISIONS-FOR-NDI.md` decision 1, `ANSWERS.md` question 1 and `TASKS.md`'s `T-63` row |

---

## The claim, in one line

**Nothing about 0.5 mM changes and every number quoted for it does: the deliverable's *"six independent routes"* is 1 withdrawn + 2 transfers + 3, its strongest figure is read at zero stroke where the device sits at three nanometres of it, and its three genuine routes share one mean field whose own error is larger than all three of them.**

---

## The conventions, restated rather than inherited

- Stiffness **pN/nm**, potential **V**, concentration **mM**; `k_BT = 4.141947 pN·nm` at **300 K** in aqueous MgCl₂.
- A **route** is *a named claim plus the **one** quantity of it that is compared between 0.5 mM and §3's 2 mM.*
  Two routes are **independent** only if neither's compared quantity is the other's, transferred.
- A **buffer advantage** is a ratio oriented so that **above one favours 0.5 mM** —
  `high/low` where a smaller reading is better (a bias a target needs), `low/high` where a larger one is (a margin) —
  and it is quoted **with the state it is read at**.
- The **recommended device** is the **10 nm** layer at `σ` = 0.024 nm⁻², placed at 100 pN over 3 nm (`C-0071`, `C-0068`);
  states of other devices are **not** intersected with it (`C-0064`).
- **Stability is owed on a coupling's TANGENT over `[0, s*]`** (`C-0049`, `CH-0042`), so `C-0017`'s margin —
  which divides by the mandated **secant** — is rescaled onto `Q5`'s minimum.
  **The floor `|k_eff|` is a property of the layer, the field and the held gap and contains no coupling element at all.**
- A **transfer** verdict is taken at a tolerance **derived** from the two files' emission precision (`1e−6`),
  never at `==` on a `Double` — `CLAUDE.md`'s *"an assertion tighter than a result file's EMISSION precision is not a stronger test"*.

---

## The cheap bound, which ran first and decided half the answer

**Two of the six can be shown not to be routes without evaluating any physics**, by comparing the emitting files.
Two independent derivations of the same physics would not agree to nine digits; only a transfer does.

| comparison | states | agreeing | worst departure |
|---|---|---|---|
| `T-2`'s `biasClauses[].biasForHundredPiconewtonBlocking` against `T-3`'s `thresholds[]` | **15** | **15** | **0.0** |
| `T-25`'s `bufferComparison` `biasMargin*` / `stiffnessMarginBaseline*` against `T-4`'s coupled `margin` and `T-16`'s `stabilityMargin` extrema | **20** | **20** | **2.66e−8** |

The `2.66e−8` is not a physical difference: `T-25` prints **eight** significant digits (`2.0910536`) where `T-16` prints **nine** (`2.09105359`). A `==` comparison would have called it *not a transfer*, which is exactly the failure mode gate 4 asserts against.

**Cost justification.** The alternative — re-deriving each route's physics — costs hours of Poisson-Boltzmann solves and *cannot establish non-independence at all*, because two genuine derivations of the same number would look identical too. Only the **file** comparison proves a transfer.

---

## Deliverable 1 — the census

**10 nm layer, `σ` = 0.024 nm⁻², 0.5 mM against 2.0 mM MgCl₂, six `C-0003` models.**

| route | clause | object it is read on | object still in the design? | advantage | verdict | independence |
|---|---|---|---|---|---|---|
| **`C-0012`** | the force clause | the **unloaded** force balance at `h = L₀` — tile, field, layer, **no coupling element** | **yes**, it contains no element to withdraw | **4.9656** (0.140845 V against 0.699378 V) | **SURVIVES, same ground** | **INDEPENDENT** |
| **`C-0016`** | the bias window | *the same number*, re-intersected over a `σ` grid | yes | 4.9656 | survives | **TRANSFER of `C-0012`** |
| **`C-0017`** | the stability floor | `\|k_eff(L₀ − 3 nm)\|` at the **held** operating point — element-**independent** | **yes** | **1.7510** (2.0911 against 1.1942) | **SURVIVES, same ground** | **INDEPENDENT** |
| **`C-0018`** | the usable bias | the **affine mandate** `R = 33.3333 s` — an idealisation, never an element | **no**; the recommended element realises its *secant* and not its tangent | 1.2826 on the mandate; **1.3480 on `Q5`** | **SURVIVES, DIFFERENT ground** | **INDEPENDENT** |
| **`C-0027`** | the corrected margin | `C-0017`'s and `C-0018`'s **own result-file numbers**, corrected | as those two | 1.7564 | survives | **TRANSFER of `C-0017` and `C-0018`** |
| **`C-0032`** | the realised coupling law | `C-0030`'s strain-**softening** coupled-standoff flexure | **NO** — removed from the output role by `CH-0081`/`C-0069` | (1.0384, for the element it was measured on) | **WITHDRAWN** | — |

> **Six named routes: 1 withdrawn, 2 transfers, 3 independent survivors. One of the three survives on a different ground.**

---

## Deliverable 2 — what each survivor is worth, at the state the device occupies

### `C-0012` — the force clause is quoted at zero stroke

`C-0012`'s compared quantity is the bias 100 pN of **blocking** force needs, i.e. at `h = L₀` with the tile not moving. The device does not sit there: it sits at `L₀ − 3 nm`, delivering 100 pN. `C-0017`'s `simultaneousTargetBias` is the **same clause at that state**, and it is a different number:

| layer model | held bias, 0.5 mM | held bias, 2 mM | advantage | against the zero-stroke 4.9656 |
|---|---|---|---|---|
| alexander-box(two-body) | 0.103042 V | 0.156805 V | 1.5218 | **3.2631×** |
| alexander-box(virial) | 0.114162 | 0.178864 | 1.5668 | 3.1693 |
| alexander-box(des-Cloizeaux) | 0.114897 | 0.180428 | **1.5703** | **3.1621** |
| strong-stretching(two-body) | 0.086562 | 0.128312 | **1.4823** | **3.3499** |
| strong-stretching(virial) | 0.091683 | 0.136734 | 1.4914 | 3.3295 |
| strong-stretching(des-Cloizeaux) | 0.093208 | 0.139310 | 1.4946 | 3.3223 |

**The route stands and the number is 3.16–3.35× smaller than the one three documents quote.** This is `CLAUDE.md`'s *"quote a stiffness with its compression"* discipline applied to a buffer advantage: the clause is well posed only with the stroke it is read at.

### `C-0017` — the floor does not move at all, and the margin does

The floor is `|k_eff|` at the held gap. It contains the layer, the field and the gap and **no coupling element**, so `CH-0081`'s removal of `C-0030` and `C-0071`'s adoption of `Q5` cannot touch it:

| | 0.5 mM | 2 mM |
|---|---|---|
| floor `\|k_eff\|` over six models | **3.8557 – 15.9409 pN/nm** | **23.4145 – 27.9132 pN/nm** |
| margin on the mandated **secant** 33.3333 | 2.0911 – 8.6452 | 1.1942 – 1.4236 |
| margin on `Q5`'s **tangent** minimum 30.028762 | **1.8838 – 7.7882** | **1.0758 – 1.2825** |
| `Q5` clears the floor | 6 of 6 | **6 of 6** |

The rescaling is `30.028762/33.3333333` exactly, so **the advantage is invariant** — 1.7510 either way. **The route survives on its own ground and its absolute margins are 9.9 % smaller than published**, which is the price of reading stability on the tangent (`C-0049`) rather than on the mandate.

### `C-0018` — the conclusion survives and the ground does not

`C-0018`'s own sentence is *"dropping to 0.5 mM removes the fold entirely and hands the ceiling back to validity at 1.29–2.36× of margin."* On `C-0069`'s `Q5` there is **no fold at 2 mM at any of the six models** (`C-0084`), so **there is nothing for 0.5 mM to remove** and the ground is void. What survives is a quantified preference on the same axis:

| | 0.5 mM | 2 mM | factor |
|---|---|---|---|
| affine mandate (`C-0018`) | 1.2917 | 1.0071 | 1.2826 |
| **recommended arm `Q5`** (`C-0084`) | **1.8706** | **1.3877** | **1.3480** |

**The preference is *stronger* on `Q5` than on the mandate, and it is a preference rather than a repair.** A verdict that survives on a different reason is exactly what `CLAUDE.md` warns must be re-checked, and this is the instance.

---

## Deliverable 3 — the reading of `C-0016` that runs the OTHER way

`C-0016` is cited for *"the bias window"*, and its bias-window half is `C-0012`'s number. Its **other** half — the §(f) stability count, read at a **fixed applied bias** rather than at a held operating point — points at **2 mM**:

| applied bias | models unstable, 0.5 mM | models unstable, 2 mM | coupling demanded, 0.5 mM | coupling demanded, 2 mM |
|---|---|---|---|---|
| 0.05 V | **1 of 6** | **0 of 6** | 3.294 pN/nm | none |
| 0.10 V | 4 of 6 | 4 of 6 | 10.763 – 21.440 | **5.311 – 15.987** |
| 0.25 V | 6 of 6 | 6 of 6 | 86.080 – 109.994 | **47.629 – 71.543** |

**This is not a contradiction of `C-0017`** and the claim does not report it as one: a *held* operating point is **force-pinned**, so `k_es = −|F_es|/ℓ` and the longer decay length at low salt wins; a *fixed applied bias* is not pinned, so the larger force at low salt wins instead. `CLAUDE.md` carries the identity already. What it does mean is that *"`C-0016` recommends 0.5 mM"* is a statement about **one of that claim's two clauses**, and the deliverable does not say which.

---

## Deliverable 4 — three routes are not three exposures

All three survivors are downstream of **one** object: `C-0008`'s mean-field Poisson-Boltzmann solve.

- **`C-0012`** rides the **level** of `|F_es|` at a fixed bias.
- **`C-0017`** and **`C-0018`** ride **`1/ℓ` at a force-pinned point** — different quantities (`CLAUDE.md`: *a stability margin read at a HELD gap is not the same quantity as a fold margin on a MOVING equilibrium*), but the same mechanism.

So the six named routes are **three quantities carried by two mechanisms through one field model**, and `C-0005`'s one-loop correction — **123–214 %** of the leading term over this gap range — is **common mode to all of them and larger than every one of the three advantages (1.35, 1.57, 1.75)**. The diversification the word *independent* implies is not there, and `T-50` is the only thing that would supply it. (**RESTATED, `CH-0167`/`C-0137`**: this is an error bar on a LEVEL, and a margin read at a force-pinned operating point is not a level — the same-kind thresholds are a force **1.48–2.22×** smaller or a decay length **9.73 %** shorter.)

---

## Deliverable 5 — the corrected sentence

**What stands.** 0.5 mM remains the programme's recommendation. Every surviving route favours it, at every layer model, at the device `C-0071` recommends. Nothing here argues for 2 mM.

**What is corrected.**

| | as it stands | as the census finds it |
|---|---|---|
| the count | *"six independent routes"* | **three** — one withdrawn (`C-0032`), two transfers (`C-0016` of `C-0012`; `C-0027` of `C-0017` and `C-0018`) |
| the grounds | six converging verdicts | two mechanisms, three quantities, **one** field model, and `C-0005`'s error common mode over all of it |
| the strongest number | *"a factor of five better than at 2 mM"* (`C-0012`) | **1.48–1.57×** at the state the device occupies; the 4.97× is at **zero stroke** |
| `C-0018`'s reason | *"removes the fold entirely"* | **void** on the recommended element; the route survives as a **1.35× preference** |
| the word | already reduced from *requirement* to *recommendation* by `CH-0098` | unchanged — **but it is now a recommendation on three routes worth 1.35–1.75× at the operating point**, not six worth up to five |

**`C-0084`'s own *"seventh route"* is not a seventh.** Its *"Still open"* item 4 offers this claim as one; it is `C-0018`'s route **re-read on `Q5`**, which is how this census counts it. The number of independent routes is unchanged by `C-0084` and is three.

---

## The five verification gates

Executed as **17 gate-named tests** in `src/test/kotlin/synthesis/BufferRouteCensusTest.kt`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | a buffer advantage is dimensionless and is a ratio of **two readings of one quantity**, oriented by which direction is better; a margin is a stiffness over a stiffness; six unphysical entry points throw (a zero or negative reading, a zero secant, a negative tangent, a negative margin, a zero tolerance) | **PASS** |
| **2 — limiting cases** | rescaling a mandate margin onto the mandate itself is the **identity**; a route compared against itself is a transfer at departure **exactly 0.0**; the advantage of a quantity that does not move is **exactly 1**; two absences agree and an absence against a reading does not | **PASS** |
| **3 — symmetry and conservation** | the census is a **partition** — six named routes, each in exactly one verdict class, the three classes summing to six, exactly one withdrawal and it is `C-0032`'s; independence is a **relation**, so every transfer names a source inside the census and no route transfers from itself | **PASS** |
| **4 — numerical convergence** | **not applicable, and said so rather than faked** — the only numerics is a ratio of two read numbers. What is asserted instead is the **decision precision**: a transfer verdict is taken at a tolerance derived from the emission precision, `2.09105359` against `2.0910536` is a transfer at `1e−6` and **is not** at `1e−12`, and the slack is bounded into `[1e−8, 1e−5]` by a test so that it cannot be widened to make a verdict pass | **PASS** |
| **5 — literature and upstream** | **every quoted figure re-derived from the emitting study's own result file** — the force clause from `T-3` (and asserted **model-free**, one value per buffer over six models), the bias clauses from `T-2`, the floors and held biases from `T-16`, the coupled margins from `T-4`, the corrected margins from `T-25`, the softening folds from `T-76`, and `Q5`'s **30.028762 pN/nm** from `T-149`. Nothing is transcribed from a claim's prose | **PASS** |

### The declared falsifiers, and what happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| **F1** | a surviving route, re-read on `Q5`, **pointing at 2 mM** — which would move the conclusion and not only the count | **no** | All five non-withdrawn routes favour 0.5 mM, and so does the force clause read at the device's own operating point (1.4823–1.5703× over six models) |
| **F2** | **the six being six** — no pair sharing a number to the emission precision, which would make the cheap bound worthless | **FIRED, and it is the finding** | 3 of 6 are independent. `T-2` carries `T-3`'s blocking bias at 15 of 15 states at departure **0.0**, and `T-25` carries `T-16`'s and `T-4`'s extrema at 20 of 20 |
| **F3** | a route whose object can be classified neither in nor out of the design | **no** | All six are read on a named object: two on the unloaded field balance, one on the held operating point, one on the affine mandate path, one on `C-0030`'s flexure, one on the other routes' readings |

**A result that was not anticipated.** The census expected the *withdrawal* to be the finding and it is not — `CH-0098` had already recorded it. The finding is the **transfers**, and they are visible only in the files: every one of the six claims *states* its route in its own words, and two of them are stating a number they read from another claim's JSON at run time. **A synthesis cannot detect that by reading claims, because each claim is telling the truth about itself.**

**A second one.** `C-0016`'s two clauses disagree about the buffer, and each is correct at the state it is read at. That is not a contradiction to be resolved — it is the force-pinning identity `CLAUDE.md` already carries — but no claim in the corpus states the pair, so the deliverable inherited whichever half its author was looking at.

---

## Validity range

- **TRL 1–3. Nothing here is measured.** A census inherits every validity range of the claims it counts and narrows none.
- **Mean field, inherited whole and COMMON MODE.** See Deliverable 4. `C-0005`'s 123–214 % is larger than all three advantages. (**RESTATED, `CH-0167`/`C-0137`**: this is an error bar on a LEVEL, and a margin read at a force-pinned operating point is not a level — the same-kind thresholds are a force **1.48–2.22×** smaller or a decay length **9.73 %** shorter.)
- **The recommended device is the 10 nm layer.** The 5 nm and 7 nm rows exist in every input file and are **not** intersected with it (`C-0064`).
- **`C-0032`'s numbers all stand** for the element they were measured on. What is withdrawn is their *transfer* to `Q5`.
- **The re-reading of `C-0017`'s margin onto `Q5` is a rescaling of the numerator, not a re-solve** — exact, because the floor contains no coupling element, and the advantage is invariant under it by construction.
- **`C-0018`'s route is re-read from `C-0084`'s own solved margins**, not rescaled: a fold is a property of the path and no arithmetic substitutes for the solve.
- **A transfer is still a check.** `C-0027` re-reading `C-0017`'s number *at run time from its file* is a genuine guard against transcription. What it is not is a second derivation, and only the second is a route.

---

## Numbers that are CITED rather than DERIVED here

| number | value | flag |
|---|---|---|
| every route reading | see Deliverable 1 | **READ** from the emitting study's own result file at run time |
| `C-0005`'s one-loop correction | 123–214 % | **CITED** |
| `C-0069`'s `Q5` tangent minimum | 30.028762 pN/nm | **READ** from `T-149`, where it is re-derived from `C-0039`/`C-0034` |
| `C-0017`'s mandate as a sum | 33.3333333 pN/nm | **CITED**, derived there from §3 alone |
| §3's targets | 100 pN, 3 nm, 40 × 40 nm, 10 nm layer, 0.5/2 mM | **CITED** |

---

## Still open — named, not answered

1. **Whether `C-0016`'s two clauses can be stated as one sentence about the buffer.** They are not in conflict; no claim states the pair.
2. **`T-50`** — a beyond-mean-field treatment of the actuated gap — is the only thing that would make the three routes three *exposures* rather than one.
3. **Whether any route to 0.5 mM exists that is not downstream of `C-0008`.** None of the six is.
4. **The two transfers are still evidence about transcription** and this claim does not price that separately.

---

## Challenges

**Raises [`CH-0106`](../challenges/CH-0106-six-routes-are-three.md)** against the *"six independent routes"* sentence in `DECISIONS-FOR-NDI.md` decision 1, `ANSWERS.md` question 1 and `TASKS.md`'s `T-63` row.

**Resolves [`CH-0098`](../challenges/CH-0098-the-0-5-mM-requirement-is-quoted-for-a-withdrawn-coupling.md)** — its third *"what would settle it"* item, the census itself.

**None stands against this claim.** The three ways it would fail:

1. **A route being mis-assigned its compared quantity.** The census fixes one quantity per claim; a claim that recommends 0.5 mM on a *second* quantity nobody named would be an extra route. `C-0016`'s §(f) is exactly such a second quantity and it points the **other** way.
2. **A transfer being a coincidence.** Two derivations agreeing at `2.66e−8` over 20 comparisons and at `0.0` over 15 is not a coincidence, but it is an inference and it is stated as one.
3. **The element being removed.** If `C-0071`'s five removal routes fire, `C-0018`'s re-reading goes with them and the census reverts to the affine mandate's 1.2826.
