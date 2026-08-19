# C-0129 — **All three items were raised on one instance each, and all three are populations: 1, 222 and 302.** The raw-conversion gate ships **wired** because the tree reads **0 of 119**; the departure rule was applied *per file* three times and **222 fields in 29 files** still break it, so it ships as a **measured audit** plus a central mechanism plus the two files this task can re-emit and verify; and the saturated statistic is not one note but **302 records in 7 files**, every one reading exactly `0.0`. `T-136` moved **7 fields, all departures, and nothing else at all**; `T-148` moved **16, all departures**, gained **60** one-sided bounds and **2** deliberate sentences, and **0 verdicts, booleans or computed quantities moved in either**

| | |
|---|---|
| **Task** | [`T-208`/`T-209`/`T-210`](../tasks/T-208-result-file-hygiene.md), all three raised by [`C-0127`](C-0127-format-string-repair.md) (`T-207`) |
| **Leaf** | none. A **process** claim; it protects the machine-readable artifact of every leaf |
| **Verification type** | **logical** (three static censuses over the committed corpus, each with a stated catch set and a mutation-tested discriminator) **+ in-silico** (two studies re-run through `tools/study.sh` and diffed field by field against their **committed** version read out of `git`, plus a **control** re-run of identical code) |
| **Verdict** | **PASS on all twelve predicates.** `T-208`'s gate is wired into `./gradlew test` and `tools/verify.sh` and reads **0 defects over 119 result files** with **one** documented allowlist entry. `T-209`'s rule now lives once, by name, as `DEPARTURE_SIGNIFICANT_DIGITS` / `DEPARTURE_DIGITS_BY_KEY` in `structure/ResultRounding.kt`; the census falls **222 → 199** fields and **29 → 27** files, and the remaining 27 are listed rather than left. `T-210`'s instrument is `coupling.saturatedProportionBound`, the exact one-sided Clopper-Pearson limit `p > (1 − c)^(1/n)`, and the census falls **302 → 277** records in **7 → 6** files. **Falsifier `F3` fired and is answered by measurement, not by argument**: the six non-departure fields that appeared to move in `T-136` are a 30-parameter minimax descent's own irreproducibility, and a second run of identical code moves them **back onto `HEAD`**. Raises [`CH-0152`](../challenges/CH-0152-the-defect-was-read-tested-and-misdescribed-not-unread.md) and [`CH-0153`](../challenges/CH-0153-a-statistical-power-gate-discharged-by-a-statistic-that-is-identically-zero.md) |
| **Maturity** | **TRL 1–3, and below it: NO PHYSICS CHANGED.** Every number this task moved is a diagnostic, a precision or a sentence. Not one computed quantity moved in either file |
| **Provenance** | `gpd/results/T-208-result-file-hygiene.json`, emitted by `tools/T-208-emit-result.py`; the new checker `tools/check-result-file-hygiene.py` (**47** self-tests, three mutations each failing a named one); `DEPARTURE_SIGNIFICANT_DIGITS`/`DEPARTURE_DIGITS_BY_KEY` in `src/main/kotlin/structure/ResultRounding.kt` with **4** gate-named tests; `saturatedProportionBound` in `src/main/kotlin/coupling/StapleDropout.kt` with **5** gate-named tests; `anchoring.TwoPerRowPlacementStudyKt` run **twice** and `coupling.StapleDropoutStudyKt` once through `tools/study.sh`; the diff baseline is `git HEAD:gpd/results/…` in both cases |
| **Conditions** | The tree at `HEAD` of iteration 29. Units unchanged and untouched: nm, pN, pN/nm, pN/nm² = 1 MPa exactly, `k_BT = 4.141947 pN·nm` at 300 K, aqueous buffer with stated Mg²⁺ |
| **Consumes** | [`C-0127`](C-0127-format-string-repair.md) (all three items, and the re-emission discipline), [`C-0125`](C-0125-scaffold-remainder.md) (the static format checker this one is *not*), [`C-0083`](C-0083-markdown-tables-that-do-not-render.md) (**a gate that cannot come clean is not a gate** — the load-bearing decision here), [`C-0093`](C-0093-shared-body-coupling.md)/[`C-0101`](C-0101-re-emitting-what-the-repair-moved.md) (the two-digit departure rule and its two partial applications), [`C-0087`](C-0087-position-dependent-staple-dropout.md) (`T-148`, the dropout model and its gate 4), [`C-0122`](C-0122-honeycomb-station-lattice.md) (`T-203`, the corpus-link checker and the checker-wiring pattern), `P-18` (`RESULT_ABSOLUTE_FLOOR` is a claim in the locked units) |
| **Raises** | [`CH-0152`](../challenges/CH-0152-the-defect-was-read-tested-and-misdescribed-not-unread.md) against `CH-0150`/`C-0127`'s *"nobody re-reads a result file"* diagnosis, and [`CH-0153`](../challenges/CH-0153-a-statistical-power-gate-discharged-by-a-statistic-that-is-identically-zero.md) against `C-0087`'s gate-4 discharge |

---

## The claim, in one line

**Each of the three items named one instance, and each instance was a population** —
which is why the cheap bound had to run before anything was written,
and why only one of the three could honestly become a gate.

---

## 1. The three cheap bounds, and each of them changed the deliverable

Each is a walk of the committed corpus and no solve. Together they cost under a minute.

| item | raised on | measured | ratio | what it decided |
|---|---|---|---|---|
| `T-208` | *"`C-0127` repaired 13 fields across 7 files"* | **1** file fires today, and it is `T-207`'s own record | — | the tree is **clean**, so the gate is wirable, and it is wired |
| `T-209` | **1** field, `T-136`'s `reproductions[2].departure` | **222** fields in **29** files (strict); **1 422** in **71** (wide) | **222×** | a gate is **unwirable**; a tree-wide repair is 29 study re-runs; the deliverable becomes a central mechanism, a measured audit, and the two files this task can re-emit *and verify* |
| `T-210` | **1** note in `T-148` | **302** of 403 records, in **7** files | **302×** | the repair is local, the measurement is the finding, and six files are queued with their owners named |

**The `T-208` bound also caught its own regular expression, before anything was wired.**
The first pass — written with a space in the Java flag class, which is legal Java — fired on **87 of 117 files**,
every hit a prose percentage (`"% of"`, `"% over"`).
That is the false positive the tool exists to avoid, and `CLAUDE.md` is explicit that
*a drift checker's FALSE positives cost more than its true ones, because the tool exists in order to be believed*.
It is why the discriminator is a **self-tested part of the tool** rather than a regular expression in a claim.

## 2. `T-208` — the gate, and why its catch set is strictly different

`tools/check-kotlin-format-strings.py` reads **source** and models `String.format` call sites.
`tools/check-result-file-hygiene.py` reads **output** and models **nothing**: it opens what was committed.
A raw conversion can reach a result file by routes the static check does not model —
a `settles` string assembled in one function and formatted in another,
a field written by a Python emitter in `tools/`,
a hand-edited JSON — and none of those is a `String.format` call site.

**Distinguishing a conversion from a percent sign is the whole difficulty**, and it is done in two steps, both self-tested:

| step | why | what it costs |
|---|---|---|
| refuse Java's **space flag** | `% d` is a legal conversion *and* it is how all **310** of this repository's prose percentages are written | a space-flagged conversion would be missed. **No `String.format` literal in `src/` uses one** |
| restrict the conversion letter to **Java's own set** | a conversion that leaked out of a `String.format` is by construction a valid one, so this is a strict tightening | `%i`, `%l`, `%w`, `%r` stop matching — all of which are prose |

Three deliberate mutations of the discriminator each fail a **named** self-test:
restoring the space flag fails three prose tests, widening the letter set to `[a-zA-Z]` fails the non-Java-letter test,
and dropping the `%%` strip fails `"100%%d of the paths"`.
**47 self-tests**, and the allowlist is tested in **both** directions —
a fixture named `T-207-…` is skipped and an identically-contented `T-999-…` is caught.

**The allowlist is a hole, and it is the same trade `check-markdown-tables.py` makes with `third-party/`**:
`T-207`'s result file **quotes** raw conversions as its record, so a real defect in it would pass.
It is the **only** entry — this task's own result file describes the catch set in words instead of quoting a conversion,
was checked to carry none, and is therefore gated like every other file.
`C-0122`'s rule applies verbatim — *an invariant that forbids fixing something must be taught to the checker, or the checker decays into a warning*.

## 3. `T-209` — the rule survived three correct repairs because each was applied to a FILE

`C-0093` cured the trap on its own **convergence** axis.
`C-0101` cured it in the **reproduction** records of the eleven files it was re-emitting.
`C-0127` then found `T-136` still carrying `reproductions[2].departure` at nine significant digits.
Every one of those repairs is correct, and every one was applied per file.

The rule is about a **record type**, so it now lives once, by name:

```kotlin
const val DEPARTURE_SIGNIFICANT_DIGITS: Int = 2
val DEPARTURE_DIGITS_BY_KEY: Map<String, Int> = mapOf(
    "departure" to …, "relativeDeparture" to …,
    "departureFromFinest" to …, "relativeDepartureInStroke" to …
)
```

Four spellings, because a census of `gpd/results/` finds four —
and keying on whichever one the last repair happened to look at is exactly the failure mode being fixed.
`departureRatio` and `plateDeparture` are deliberately **absent**: those are ratios *between two models*, not residuals between two refinements of one.

**The census after the repair is 199 fields in 27 files, and it is published per file** rather than left implicit.
Wiring it as a gate costs one study re-run per remaining file, in `tools/reemission-order.py`'s order, plus a per-file diff.
That is the outstanding work, and it is queued rather than smuggled into a hygiene task.

## 4. `T-210` — a saturated statistic is the resolution of nothing

`T-148`'s convergence note read *"the binomial standard error at 10 000 draws is 0.0000, **which is the resolution the verdict is quoted to**"*.
It is `0.0000` because the exceedance against `T-5b`'s 0.10 is `1.0` at **all five** sample counts,
and `√(p̂(1 − p̂)/n)` at `p̂ = 1` is a function of `p̂` alone:

| draws | symmetric s.e. | one-sided 95 % bound (exact) | rule of three |
|---|---|---|---|
| 1 250 | **0** | `p > 0.997606284` | 0.9976 |
| 2 500 | **0** | `p > 0.998802425` | 0.9988 |
| 5 000 | **0** | `p > 0.999401033` | 0.9994 |
| 10 000 | **0** | `p > 0.999700472` | 0.9997 |
| 20 000 | **0** | `p > 0.999850225` | 0.99985 |

The left column reports the saturation back to itself; only the right column knows how many draws were taken.
The instrument is the exact one-sided Clopper-Pearson limit at `x = n`, `p > (1 − c)^(1/n)`,
whose large-`n` form is the **rule of three** because `ln(1/20) = −2.996`; at `n = 10 000` the two agree to **4.7e−7**.
It is retained as `coupling.saturatedProportionBound` with **five gate-named tests**, one of which asserts
`bound^n = 1 − c` exactly — the definition, not an approximation of it.

**The symmetric error is kept, not deleted.** It is uninformative rather than wrong, and removing it would break every reader of the schema.
A record is *repaired* by emitting the bound beside it, and the checker's census is written to say so — with a self-test for each direction.

## 5. What moved, measured against `git HEAD`

`tools/reemission-order.py` puts `T-136` before `T-148` (`coupling/StapleDropoutStudy.kt` reads `T-136`'s result file), and that is the order they were run in.

| result file | departure fields | other numeric | prose (wording) | verdict / boolean | fields added |
|---|---|---|---|---|---|
| `T-136-two-per-row-placement.json` | **7** | **0** | **0** | **0** | 0 |
| `T-148-staple-dropout.json` | **16** | **0** | 2 (both deliberate) | **0** | 60 |

`T-136` moved **seven fields and nothing else in a 1 743-field file**.
`T-148`'s sixteen are *all sixteen* of its moved numerics; the sixty additions are `monteCarlo[*].exceedanceOneSidedBound`,
`null` at the 35 unsaturated cells; the two sentences are the convergence note and the `P3` predicate statement, both rewritten on purpose.
**No exceedance probability, dishing percentile or `flatAt*` boolean moved. `F2` and `F4` did not fire.**

### The downstream containment cost a `grep` and excluded four heavy re-runs

`T-136` is read by **five** studies. Every one reads `parameters/*` or `recommendedPlacement`; **none reads a departure field**,
so a rounding change confined to departures cannot reach them.
That is `CH-0131`'s `min(a, b) = a` argument in a new place —
and it is not left as an argument, because **`coupling/StapleDropoutStudy.kt` is one of the five** and re-ran anyway:
**0 non-departure numeric fields moved in `T-148`.**

## 6. `F3` fired, and the answer is a measurement

The first `T-136` run moved six fields the repair cannot explain — all inside `distributions[11]`,
a 30-parameter minimax, worst relative movement **0.057**.
Rather than assert that a serialisation-rounding change cannot move a descent, the study was run a **second** time on a separate snapshot with **identical code**:

| comparison | departures moved | other numeric moved | worst | decisions |
|---|---|---|---|---|
| run A vs `HEAD` | 7 | 6 | 0.057 | 0 |
| **run A vs run B — identical code** | **0** | **6** | **0.057** | 0 |
| **run B vs `HEAD`** (run B is retained) | **7** | **0** | — | 0 |

Two runs of identical code move the same six fields by the same amount, and run B lands **back on `HEAD`**.
`CLAUDE.md` records the mechanism verbatim — *a descent on an optimal MANIFOLD has no isolated answer to be reproducible about* —
and `C-0127` measured the identical class in `T-129` one iteration earlier.

## 7. What this claim does not do

- It repairs the saturated statistic in **one of seven** files. The other six emit a zero symmetric error without asserting anything about it, so they mislead by *omission* rather than by statement. They are listed with their owners in `CH-0153` and queued.
- It leaves **199 over-precise departure fields in 27 files**. The mechanism is central and the audit is standing, so each is now one re-run away; none is a defect a reader can be misled by, because an over-precise departure is *more* digits of a number that is already correct.
- **The two-significant-digit rule is a convention and it is conservative rather than exact.** A departure `d` between two quantities each determined to nine digits is itself determined to about `9 + log₁₀ d`, which is between three and *minus two* over the range these fields occupy — but **nine** for an order-one departure like `T-130`'s `2.20588235`. Applying two digits there discards determined information; it never fabricates any. The rule is adopted as it stands and the refinement is recorded here rather than acted on.
- It asserts nothing about physics. Every number it moved is a diagnostic, a precision or a sentence.
