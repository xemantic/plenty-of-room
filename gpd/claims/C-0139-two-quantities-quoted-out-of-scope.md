# C-0139 — **Two numbers this corpus quotes out of their scope, measured and repaired.** `C-0005`'s **123–214 %** is an error bar on a **LEVEL** and 63 of its 100 in-scope occurrences sit beside a quantity that is not one — 56 beside a **force-pinned margin** and **7 beside a RATIO in which the level cancels**, a third class `CH-0167` did not have and which **two of the seven claims already state in their own words**. And `T-113`'s descents decided on **raw `Double`s**: three cured runs are now **byte-identical over all 5 835 fields**, and the manifold they were drawing from is **2.80 % wide in the objective and 51× in the stiffness ratio the design reads**

| | |
|---|---|
| **Tasks** | [`T-220`](../tasks/T-220-level-not-a-stiffness-error-bar.md) and [`T-226`](../tasks/T-226-nonuniform-coupling-manifold.md) — one claim, because both are *"a number is quoted outside the scope it was established in"* and both land in `ANSWERS.md` |
| **Leaf** | **`A7.4`** for `T-220` (consumed by `A2.2` and `A8.2`); **none** for `T-226`, which is a **process** result protecting the machine-readable artifact `A8.2` rests on |
| **Verification type** | **logical** (a mechanical corpus census, a per-occurrence partition retained as inspectable data, a mechanism located in the source) **+ in-silico** (**six** independent emissions of `coupling.NonUniformCouplingStudyKt` — three of `HEAD`'s uncured code, three of the cured code — plus the iteration-9 commit read out of `git`, diffed field by field) |
| **Verdict** | **PASS on all twelve predicates (`P1`–`P6` of each task).** `T-220`: the census reproduces **exactly** at 66 in 31, the gate reads **0** defects, **no verdict moves and three GROUNDS do**. `T-226`: the irreproducibility reproduces at **224** fields over three emissions, the cure makes three runs **byte-identical**, **0** booleans and **0** wordings move anywhere. **Two declared falsifiers fired** — `T-220`'s `F4` (it is a **third class**, `CH-0170`) and `T-226`'s `F5` (the cure **does** apply, so branch (a) is taken **and** the width reported) |
| **Maturity** | **TRL 1–3, and below it: NO PHYSICS CHANGED AND NO PHYSICAL NUMBER RECOMPUTED.** `T-220` recomputes nothing at all; `T-226` moves only which member of an optimal manifold a descent lands on |
| **Provenance** | `gpd/results/T-220-level-not-a-stiffness-error-bar.json` (from [`tools/T-220-census.py`](../../tools/T-220-census.py), **234** self-tests, and the retained reading in [`tools/T-220-classification.json`](../../tools/T-220-classification.json)); `gpd/results/T-226-nonuniform-coupling-manifold.json` (from [`tools/T-226-emission-diff.py`](../../tools/T-226-emission-diff.py), **24** self-tests); the mechanism repair in `src/main/kotlin/coupling/NonUniformCouplingStudy.kt` with **5 gate-named tests** in `src/test/kotlin/coupling/T226DescentDecisionPrecisionTest.kt`; `gpd/results/T-113-non-uniform-coupling.json` re-emitted and **byte-identical over three runs**; the whole suite **2 802 tests, 0 failures**; `tools/verify.sh` **BUILD SUCCESSFUL in 21 m 57 s** on an isolated copy, with every retained document gate re-run in a `.git`-less snapshot (tables, links, format strings, challenge index, result-file hygiene, departure precision, reader census) — all clean |
| **Conditions** | For `T-226`: T = 300 K, `k_BT = 4.141947 pN·nm`; the tile, the lattice, the foundation and `C-0022`'s five solved load states exactly as `C-0058` set them, free-tile stroke 4.90731 nm. **Nothing about the model changed** — only the precision at which the search takes its decisions. For `T-220`: no conditions, nothing is computed |
| **Consumes** | [`C-0137`](C-0137-beyond-mean-field-gap.md)/[`CH-0167`](../challenges/CH-0167-the-123-214-per-cent-is-a-level-and-it-is-quoted-as-an-error-bar-on-a-stiffness.md) (the level/gradient split and the two replacement thresholds), [`C-0005`](C-0005-mean-field-screening-validity.md) (the ratio itself), [`C-0138`](C-0138-departure-rule-scope.md) §8 (the observation), [`C-0135`](C-0135-descent-manifold-width.md)/[`CH-0162`](../challenges/CH-0162-three-agreeing-runs-are-a-draw-not-a-verdict.md) (the manifold, the cure and the warning about reading agreement as a verdict), [`C-0058`](C-0058-non-uniform-coupling.md)/[`C-0064`](C-0064-robust-distribution.md) (the claims amended), [`C-0071`](C-0071-output-element-recommendation.md) (*strike, never delete*), [`C-0082`](C-0082-result-reader-census.md)/[`C-0117`](C-0117-reemission-order.md) (the reader census and the re-emission order, which priced the repair before it was made) |
| **Raises** | [`CH-0170`](../challenges/CH-0170-a-common-factor-is-not-an-error-bar-either.md) — a common factor is not an error bar either, seven occurrences — and [`CH-0171`](../challenges/CH-0171-an-irreproducible-number-was-substituted-for-a-reproducible-one.md) — `0.1247` is `C-0058`'s reading at one state, substituted for `C-0064`'s `0.1254` in `CLAUDE.md` and `ANSWERS.md` |
| **Amends** | [`C-0058`](C-0058-non-uniform-coupling.md) in **twelve** places, struck rather than rewritten; and 63 occurrences across 24 claims and three documents |

---

## The claim, in two lines

**A percentage is an error bar on the thing its DENOMINATOR is, and a quantity is exposed to it only if it is homogeneous of degree one in that thing.** A stability margin is not — it is pinned by a force balance, so the level goes into the bias. A ratio is not — the level is a common factor and cancels. A force is. Of 100 in-scope occurrences of `C-0005`'s 123–214 %, **11 are on a force and are right**; the other **63 that make a comparison at all are not**, in two different ways.

**And a decision must be rounded coarser than the number it is taken on — at every call site, not once per repository.** `C-0135`'s cure was already in the tree, in a function `T-113` does not call; wrapping `T-113`'s three descent objectives at six significant digits takes it from **224 fields different between two runs** to **byte-identical over three**.

---

# Part 1 — `T-220`: the 123–214 % is a LEVEL

## 1. The census, reproduced mechanically

`CH-0167` publishes **66 occurrences in 31 claims**, plus 8 / 10 / 6 in the three documents a reader outside the programme sees.
`tools/T-220-census.py` reproduces the claim census **exactly**: `66` occurrences in `31` files. **`F1` did not fire.**

The three documents read **9 / 15 / 10** rather than 8 / 10 / 6, and the reason is recorded rather than reconciled away: iteration 32 already restated four of them and **every correction sentence names the ratio again**, and `TASKS.md` has since gained the `T-50` DONE row and the `T-220` row itself. The falsifiable half is the claim census and it is exact.

Over the whole corpus the string occurs **158** times. The scope this task edits is the one `CH-0167` names — the claims plus the three documents, **100** occurrences — and everything else is excluded **with its reason recorded in the tool**: `JOURNAL.md` (9) is an append-only history, `gpd/tasks/` (27) is a record of what was believed when each task was set, `gpd/challenges/` (17) includes `CH-0167` itself, and `CLAUDE.md`'s three sit inside `C-0005`'s own entries, which state the quantity rather than transfer it.

## 2. The partition, and it is FOUR ways rather than two

`CH-0167` partitions by asking whether the quantity is a **free-tile** one. `T-220`'s pass finds a class its exemption does not cover.

| class | count | what it is | what was done |
|---|---|---|---|
| **HELD** | **56** | a stability floor, coupling margin, fold/pull-in margin, or a window edge built on one, read at the **force-pinned** operating point where `\|F_es\| = 100 pN + P(g)A` and `k_es = −\|F_es\|/ℓ` identically, so a level multiplier is absorbed into the **bias** | restated or pointed |
| **SHAPE** | **7** | a **ratio, departure, taper or dishing fraction** read on ONE field, in which the level is a **common factor** and divides out | pointed — **`CH-0170`** |
| **LEVEL** | **11** | a force, a pressure, a bias, a well depth, a decay length: the quantity **is** a level, so `C-0005`'s ratio is the right error bar | **left alone**, with the reason recorded |
| **META** | **26** | a sentence *about* `C-0005`, `CH-0019`, `T-50` or `CH-0167`, or a bare `CITED` provenance row | **left alone** |

**63 addressed** — 6 of them by having been struck, 62 carrying a pointer (five are both) — and **37 left**, which is 37 % of the corpus's occurrences that `CH-0167` was **right** not to touch.

The classification is a **reading**, and it is retained as data (`tools/T-220-classification.json`) precisely so it can be falsified one occurrence at a time rather than as a whole. The gate is deliberately a *proximity* test — it checks that a `CH-0167` pointer **follows** each HELD or SHAPE occurrence within 900 characters, or that the occurrence lies inside a struck span — and it cannot check that the pointer says the right thing.

> **The forward-only window is not a detail.** A restatement follows the sentence it restates, so a pointer belonging to the *previous* occurrence must not discharge the next one. And struck spans count as addressed, because `C-0071`'s *strike, never delete* means a correct repair **leaves the withdrawn sentence in the file** — a checker that cannot read a strike penalises the discipline it exists to support (`C-0109`'s finding, applied to a new gate on the day it was written).

## 3. `F4` FIRED, and the third class is the interesting one

**Seven occurrences quote a level error bar against a RATIO.** In `C-0022` (*"bigger than the entire edge effect"*), `C-0026` and `C-0047` (*"larger than every effect in this claim"*, where the effects are dishing fractions under a **normalised** load), `C-0100` twice and `C-0132` twice.

The mechanism is **cancellation**, not pinning, so `CH-0167`'s argument does not reach them and a different one does. And the corpus already contains it, twice, **in the sentence next to the banner that contradicts it**:

- `C-0100`: *"it enters the **departure** between the two widths as a **common factor** rather than as an error on it, which is the only reason a 0.04 % answer is worth quoting inside a 214 % bracket"*
- `C-0026`: *"a design normalised to a fixed total would move every force here by one common factor and **no ratio at all**"*
- and `C-0032`, on the other side of `CH-0167`'s line: *"what survives that is the **comparison** — L1, L2, L3 and L4 are read on the identical field, so their differences are not exposed to it"*

**The argument is not new to this corpus; what is new is that it is not applied to the banner it contradicts.** `CH-0170`.

## 4. No verdict moves. Three GROUNDS do — and one of them is a withdrawal

| where | verdict | old ground | new ground |
|---|---|---|---|
| `C-0017`, and every claim inheriting it | *not excluded, never established* — **unchanged** | the margin (1.19–1.42×) is smaller than the model error (123–214 %) | there is **no systematic theory** at `Ξ = 17–24`, so the correction is **unquantified** — while every evaluable channel is worth at most **1.44 %** of the margin. A weaker-sounding ground and a much smaller exposure |
| `DECISIONS-FOR-NDI.md` §1 — **the strongest sentence in the file** | 0.5 mM still preferred — **unchanged** | *"the only margin in this repository that clears `C-0005`'s own 123–214 % mean-field error"* | **WITHDRAWN**: no margin ever had to clear that percentage. At 0.5 mM the **gradient** threshold is `−0.1477 nm⁻¹`, **3.9×** further away than at 2 mM |
| `C-0091`/`C-0114`/`ANSWERS.md` — the three buffer routes | three routes, common mode, not three exposures — **unchanged** | the correction is larger than each of the three advantages | the common mode is a shared **field model**, and its same-kind measure is the **gradient**, a decay length **9.73 %** shorter |

`CLAUDE.md`'s *"a verdict that survives can survive on a different reason"*, three times in one pass, and the middle one is a sentence NDI has been reading.

## 5. The replacement qualifier, and why it is of the same kind

A margin is `33.3333 / max(0, −k_eff)` with `k_eff = |F_es|/ℓ − k_brush`, so a **force** and a **decay length** are the two quantities it is built from. `C-0137` gives both thresholds at `C-0017`'s binding state (10 nm layer, 2 mM, held at 7 nm):

> **the true force would have to be `1.48–2.22×` SMALLER, or the decay length `9.73 %` SHORTER.**

They are quoted here for margins at *other* states too, which is exactly the discipline this task exists to enforce — and it is admissible for one stated reason: **the binding state is the shallowest of the eighteen that have a floor at all**, so every other state's threshold is further away.

---

# Part 2 — `T-226`: `T-113`'s descents decided on raw `Double`s

## 6. The cheap bounds ran first, and two of them shaped the task

| bound | measured | what it decided |
|---|---|---|
| **1** — who reads `T-113`? | **zero readers** (`tools/result-reader-census.py`) | a repair costs **one file** and no downstream sweep. This is what made branch (a) affordable at all |
| **2** — which optimiser does `T-113` run? | **not** `minimaxStiffnessDistribution` | `C-0135`'s cure — periodic restarts, a small-difference restart, a lattice snap, a decision-rounded objective — is **already in the tree**, in a function `T-113` does not call. All three of its optimisations call `C-0058`'s plain coordinate descent with a **raw** objective |
| **3** — who carries the eight moving numbers? | `C-0058` (5 places), `CLAUDE.md` (2), `ANSWERS.md` (1) | the amendment is bounded before any run |

> **A cure is a property of a CALL SITE, not of a repository.** `C-0135` measured this object, named the fix and applied it where it found it; `T-113` was two function calls away and inherited nothing.

## 7. The mechanism, exactly

`optimiseStiffnessDistribution` takes **every** branch on a raw `Double` comparison with no tolerance — the coarse scan's `value < bestValue`, the golden section's `leftValue < rightValue`, the refinement's `refined.second < bestValue`, the sweep acceptance `bestValue < best − 1e-15` (which at a dishing of 0.1 is `1e-14` **relative**), and the start ranking. On an optimal manifold — where the active constraints are fewer than the free directions — the terminal point is whatever the trajectory reached, so an ulp decides the answer.

**Pinned by five tests** (`T226DescentDecisionPrecisionTest`) on a deliberately chosen perturbation size: `1e-14` relative is far **above** a last-ulp difference and far **below** the six-digit decision cell.

- the descent is deterministic when nothing perturbs it;
- a `1e-14` perturbation **MOVES** the unwrapped descent on a manifold;
- the same perturbation does **not** move one decided at six significant digits;
- rounding does not degrade the optimum it finds;
- `searchDecision` quantises at six digits and leaves `0`, `±∞` alone.

The wrap is at `NonUniformCouplingStudy`'s **three call sites**, not inside the shared optimiser, because five other studies call that optimiser and their published files must not move for a repair this one file needs.

## 8. Measured: 224 fields uncured, byte-identical cured

| comparison | numeric fields moved | `OTHER` | booleans | wording |
|---|---|---|---|---|
| three **uncured** emissions (committed, run A, run B) | **224** | **0** | **0** | **0** — one prose field differs in its **digits alone** |
| three **cured** emissions | **0** | **0** | **0** | **0** — byte-identical, all 5 835 fields |
| cured against the committed file | 1 132 | 8 (convergence departures of a moved optimum) | **0** | **0** — 6 prose fields, digits only |

The moving block is always **one descent record and its transfers**, and a different one each run, exactly as `C-0138` §8 reports. **Run B landed bit-for-bit on the iteration-9 point** — `C-0135`'s *"a fresh run draws from both"*, reproduced on a second study.

**`F2` fired on the INSTRUMENT and not on the study, and it is recorded rather than repaired away.** The classifier's first pass reported **11 `OTHER`**; all eleven sat inside the minimax record and every one was an **argmin functional** — a max/min stiffness ratio, peak forces, path counts. The key list was incomplete. With it completed, `OTHER` is **0**.

**How strong is "three agreeing runs"?** `CH-0162` is explicit that agreement is a draw and not a verdict, so the load-bearing evidence here is the **mechanism**, not the sample: the decision cell is `~1e-6` relative and a last-ulp difference is `~1e-16`, so a flip needs a straddle with probability `~1e-10` per comparison — against a raw comparison, where any ulp difference flips whenever two evaluations are near-equal, which near an optimum is every time.

## 9. The manifold width — and the VALUE and the POINT part company by a factor of 18

Over three uncured emissions:

| quantity | kind | values | width |
|---|---|---|---|
| the minimax **objective** (worst of five) | **VALUE** | 0.1543 / 0.1562 / 0.1587 | **2.80 %** |
| at the design point — `C-0058`'s **0.1247** | POINT | 0.1247 / 0.1385 / 0.1403 | **11.2 %** |
| at 0.5 mM — `C-0058`'s **0.1286** | POINT | 0.1286 / 0.1475 / 0.1525 | **15.7 %** |
| `k_max·s` at 3 nm — `C-0058`'s **9.346 pN** | POINT | 9.346 / 6.978 / 9.685 | **28.0 %** |
| the **max/min stiffness ratio** | POINT | **17.3 / 134.1 / 880.7** | **98.0 %**, i.e. **51×** |
| the 45-parameter **single-state** optimum — `C-0058`'s 0.0544 | VALUE | identical in all three | **0** |
| `C-0058`'s **headline 0.0753** | — | identical in all **five** | **0** |

> **The last row is what a design reads, and it is not determined.** `C-0060`'s buildable two-level window is `3.5 ≤ R ≤ 20`; the minimax's own demanded ratio **straddles** it, from inside (17.3) to 44× outside (880.7). **The VALUE of a minimax is determined to 2.8 % and the POINT it is attained at is not determined at all** — `C-0135`'s finding reproduced on a second study, and here on a quantity that decides **buildability** rather than flatness.
>
> **`F4` did not fire as declared and something near it did.** The measured width never approaches `T-5b`'s 0.10 — the minimax is 1.54–1.59× outside it in every emission. But it crosses `C-0060`'s **stiffness-ratio** threshold, which no declared falsifier named. *Declare a falsifier on every threshold the moving quantity feeds, not only on the one the study is about.*

**The manifold is a property of the OBJECTIVE, not of the parameter count.** The same 45-parameter descent on a **single** state landed in the same basin in all three uncured emissions; only the **five-state minimax** — a max of five smooth functions, hence non-differentiable along four switching surfaces — moved.

## 10. What the cure cost, stated rather than left to be found

Against the committed file: **1 132** numeric fields, **0** booleans, **0** verdicts, **6** prose fields **in their digits alone**. One optimum is **worse** by 0.34 % (the 2 × 15 lattice, `0.2512 → 0.2520`); every other moves the favourable way — the 3 × 15 45-parameter optimum `0.0544 → 0.0482` of the stroke (**11.5 % better**, still inside `T-5b`'s 0.10) and the five-state minimax worst case `0.1587 → 0.1537` (still **1.54×** outside it, so *"no distribution found is flat at every solved state"* stands).

`C-0058` is amended in **twelve** places, struck rather than rewritten — including its provenance sentence, which claimed the file was *"byte-for-byte identical on two independent runs"* after the search's **path** diagnostics were removed from it. **Removing the path diagnostics did not make the file reproducible; it made the irreproducibility invisible.** That is `CLAUDE.md`'s *"a defect that is invisible in the answer is invisible to every check written on the answer"*, in the one place where the check was written by the claim being checked.

## 11. `CH-0171` — the substituted number is the irreproducible one

`CLAUDE.md` and `ANSWERS.md` both cite `C-0064` for a five-state minimax and both print **`0.1247`**. `C-0064`'s number is **`0.1254`** (and *"still 1.25× outside"* is `1.25 × 0.10`, which is 0.1254 and is not 0.1247). `0.1247` is `C-0058`'s minimax at **one** state.

The substitution replaced the study's **best-determined** number — a VALUE, converged to `1e-4` over a nested 1/2/4 subdivision — with its **worst-determined** one, an argmin functional **11.2 %** wide. No corpus grep can see it: `0.1247` **is** in the corpus, in a claim, at that precision. It is a different quantity. Both carriers are corrected, struck.

---

## Verification gates

1. **Dimensional** — `T-220` computes nothing. `T-226`: every width is a relative spread of one field against itself across emissions, dimensionless by construction; the diff tool refuses to spread a string or a boolean.
2. **Limiting cases** — the emission diff returns **0 moved** on two identical documents and reports a moved boolean or string as a **decision** rather than as a width (self-tested); the census gate returns `0` on a corpus with no occurrence, and `unclassified`/`stale` rather than a silent pass when the classification and the corpus disagree.
3. **Symmetry and conservation** — `C-0058`'s headline `0.0753` and the whole rim family are **bit-identical across all five emissions**, which is the conservation statement this repair is owed: a closed-form sweep must not move when a search's decision precision changes. Every `flat` boolean and every `verdict` string likewise.
4. **Numerical convergence** — the manifold width is a **maximum over a sample** of three uncured emissions plus the iteration-9 commit, monotone non-decreasing in the member count, and is a **lower** bound. The cured file's reproducibility is asserted over three runs **and** bounded by the cell-to-ulp ratio, which is the instrument `CH-0162` says the sample cannot be.
5. **Literature cross-check** — none applies; both parts are internal. The nearest thing is `CLAUDE.md`'s own standing rules, and both parts are instances of them: *decide coarser than you emit*, and *quote it with the state it is read at*.

---

## Declared falsifiers, and whether they fired

| | statement | fired | what it found |
|---|---|---|---|
| **`T-220 F1`** | the census does not reproduce at 66 in 31 | **no** | exactly 66 in 31 |
| **`T-220 F2`** | the partition is not decidable from the sentence | **no** | 101 of 101 classified, 0 unclassified, 0 stale |
| **`T-220 F3`** | a restatement moves a verdict | **no** | 0 verdicts; **three grounds**, recorded, one of them a withdrawal |
| **`T-220 F4`** | a not-HELD occurrence is quoting the level against something that is not a level either | **YES** | **seven**, a whole third class. `CH-0170` |
| **`T-226 F1`** | two fresh runs agree, i.e. the irreproducibility does not reproduce | **no** | three uncured emissions, three distinct points; run B reproduced iteration 9 bit for bit |
| **`T-226 F2`** | a moving field is neither a VALUE nor a POINT of a descent | **no** | 0 `OTHER` — after the **instrument's** own key list was completed, which is recorded |
| **`T-226 F3`** | a verdict moves between two emissions | **no** | 0 booleans, 0 wording; 6 prose fields in digits alone |
| **`T-226 F4`** | the measured width crosses `T-5b`'s 0.10 | **no** | 1.54–1.59× outside it in every emission — **but it crosses `C-0060`'s stiffness-ratio window, which no falsifier named** |
| **`T-226 F5`** | `C-0135`'s cure applies cleanly, so branch (b) was the wrong one | **YES** | it does. Branch (a) is taken **and** the width is reported, because the width is what says how much every number `C-0058` quoted was ever worth |

---

## Validity range

- **`T-220`'s partition is a READING**, not a derivation. It is retained as data so it is falsifiable one occurrence at a time.
- **The gate is a PROXIMITY test.** It checks that a pointer follows a HELD or SHAPE occurrence; it cannot check that the pointer says the right thing.
- **`tools/T-220-census.py --check` is deliberately NOT wired into `tools/verify.sh`**, for `P-21`'s reason applied to a new gate: its classification is keyed on an occurrence **index**, so a sibling adding a banner in the same iteration makes it read `unclassified` and fail a verification run for the wrong reason. It is an **audit instrument**, run on demand and listed in the `README`; its self-tests are not.
- **The classification key is fragile under concurrent editing.** It is `(file, occurrence index within file)`, so a sibling inserting a new occurrence *above* an existing one shifts every index below it. The tool reports that as `unclassified` or `stale` rather than mis-gating silently — the safe direction — and none of this task's own pointer text contains the ratio.
- **`C-0137`'s thresholds are read at ONE state.** They are admissible as the general replacement only because that state is the **shallowest** of the eighteen with a floor.
- **Nothing here narrows the mean-field exposure.** `C-0137` bounds it; this task only stops it being compared with the wrong kind of quantity.
- **`T-226`'s cure pins a PATH, not a POINT.** `C-0135` is right that rounding the decisions stabilises which branch is taken and does not make the optimal set a single point. The cured file reports one member of the manifold, chosen deterministically.
- **The manifold width is a lower bound**, a maximum over four emissions.
- **The cured optimum is BETTER, not RIGHT.** A descent reports the best point it found; `0.0482` is a lower minimum than `0.0544` on the same problem, and the reachable-dishing floor beside it is unchanged and still forbids nothing.
- **The emitted objective is now the ROUNDED one** at `optimiser[*].objective`, because the wrapped objective is what the optimiser returns. The re-solved quantities (`dishingOverStroke`, `latticePeakDishing`) are unrounded.
- **`T-113` has zero readers**, so nothing propagates. If a future study reads it, the re-emission order must be recomputed.

## Numbers that are cited rather than derived

| number | value | flag |
|---|---|---|
| `C-0005`'s one-loop deviation | 123–214 % | **CITED**, and not recomputed here — `C-0137` reproduces it to 0.14–0.47 % |
| `C-0137`'s level and gradient thresholds | `1.48–2.22×`; `9.73 %` (`g* = −0.03766 nm⁻¹`) | **CITED** from `C-0137`, which derives them |
| `C-0064`'s five-state minimax | 0.1254 | **CITED** from `C-0064` |
| `C-0060`'s buildable two-level window | `3.5 ≤ R ≤ 20` | **CITED** |
| `T-5b`'s flatness convention | 0.10 of the stroke | **CITED** |

Everything else — the census, the partition, the six emissions, every width, and the diff of every field — is derived here.

## Challenges

Two are raised **by** this claim: [`CH-0170`](../challenges/CH-0170-a-common-factor-is-not-an-error-bar-either.md) and [`CH-0171`](../challenges/CH-0171-an-irreproducible-number-was-substituted-for-a-reproducible-one.md). None stands against it.
