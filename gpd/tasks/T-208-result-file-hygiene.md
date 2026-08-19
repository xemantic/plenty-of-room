# T-208 / T-209 / T-210 — three result-file hygiene items, one artifact

**Raised by** [`C-0127`](../claims/C-0127-format-string-repair.md) (`T-207`), §5 and its `incidentalFindings` block.
**Claim** `C-0129`. **Challenges reserved** `CH-0152`, `CH-0153`.
**Result** `gpd/results/T-208-result-file-hygiene.json`.
**Leaf** none — this is a **process** task. It protects the machine-readable artifact of every leaf.
**Verification type** **logical** (static censuses over the committed corpus, each with a stated catch set) **+ in-silico** (the affected studies re-run through `tools/study.sh` and diffed field by field against their committed version).

## Why these are one task and not three

They are one task for three reasons, and the third is the one that decided it.

1. **One class.** All three are defects *in a result file* rather than in a model: a number or a sentence that is printed wrongly, or printed to a precision it does not have, or printed as an instrument that measures nothing. `C-0127` found all three in one pass, and found the second and third **only because repairing the first made the surrounding prose legible**.
2. **One instrument.** Each is a *census over `gpd/results/`* followed by a decision about whether the census can be turned into a gate. The census is the same walk of the same 117 files; only the predicate differs.
3. **One result file, and — as it turned out — one overlapping re-emission set.** `T-209`'s named instance is `T-136`; `T-210`'s named instance is `T-148`; and `tools/reemission-order.py` puts `T-136` **before** `T-148` because `coupling/StapleDropoutStudy.kt` reads `gpd/results/T-136-two-per-row-placement.json`. Repairing them separately would have re-run `T-148` twice.

## Locked units and conventions

Nothing physical is computed here. Units are unchanged and untouched: nm, pN, pN/nm, pressure in pN/nm² = 1 MPa exactly, `k_BT = 4.141947 pN·nm` at 300 K in aqueous buffer with stated Mg²⁺. **No number this task moves is a physical quantity**; every one is a diagnostic, a precision or a sentence.

A **departure** here means what `C-0093` means by it: a dimensionless difference or ratio of two nearly equal numbers, emitted as a `convergence[*].departure` or a `reproductions[*].departure` field.

A proportion `p̂` is **saturated** when it is exactly `0.0` or exactly `1.0`.

---

## T-208 — a gate over `gpd/results/` for raw `%` conversions

### Numeric target and acceptance predicate

**P1.** A retained checker reads every file in `gpd/results/` and reports every string value carrying a Java format **conversion**. Its catch set is stated, and it is **strictly different** from `tools/check-kotlin-format-strings.py`'s: that one reads **source** and models `String.format` call sites; this one reads **output** and models nothing.

**P2.** The checker **distinguishes a conversion from a percent sign**, and both directions are self-tested: `"9.1 % of the load"` and `"48 %"` are clean; `"%.4f"`, `"%d"`, `"%,d"`, `"%08.2f"` are defects.

**P3.** The tree reads clean under the checker before it is wired, with a **stated, documented** allowlist for the files whose *record* is the defect. `C-0083`: *a gate that cannot come clean is not a gate*, and `C-0122`'s corollary — an invariant that forbids fixing something must be taught to the checker, or the checker decays into a warning.

**P4.** Wired into `./gradlew test` and `tools/verify.sh` in the pattern of the four existing checkers, self-tests as their own Gradle task.

### Falsifier

A raw conversion the checker misses, or a legitimate percent sign it flags. Either is a `FAIL` on P2.

### Plan, and the cheap bound

The cheap bound is **one regular expression over 117 files and no run**: how many result files would the gate fire on today? If the answer is more than the deliberate records, the gate cannot be wired and the task turns into a repair. Run it **before** writing the checker.

---

## T-209 — the nine-digit reproduction departure

### Numeric target and acceptance predicate

**P5.** A census of **every** `reproductions[*].departure` and `convergence[*].departure` in `gpd/results/`, reported as a count of fields and of files — not the one field `C-0127` named.

**P6.** The two-significant-digit rule is expressed **once, centrally and by name** in `src/main/kotlin/structure/ResultRounding.kt`, not as a bare `2` at a study's emission site. `C-0127`'s own diagnosis is that the rule was applied *per file* rather than *per record type*; a repair that fixes one more file the same way repeats the defect at a smaller scale.

**P7.** Every file the task re-emits reads clean under the census. Files it does not re-emit are **reported**, not silently left.

**P8.** Re-emission in the order `tools/reemission-order.py` prints, and the **dependency closure** checked rather than assumed (`CH-0131`).

### Falsifier

Any **numeric** field that is not a departure moving under the repair, or any `verdict`, `predicate` or boolean field moving. A rounding change to a diagnostic cannot move a computed quantity.

### Plan, and the cheap bound

The cheap bound is **a walk of the committed JSON and no run**: how many departure fields carry more than two significant digits? That count decides whether this is a one-file repair or a tree-wide re-emission, and therefore decides the whole shape of the deliverable.

---

## T-210 — the saturated exceedance statistic

### Numeric target and acceptance predicate

**P9.** A census of every result field that is a **standard error on a proportion**, partitioned by whether that proportion is saturated.

**P10.** `T-148`'s convergence note no longer asserts that a zero standard error *"is the resolution the verdict is quoted to"*. At `p̂ = 1` the symmetric binomial error `√(p̂(1−p̂)/n)` is **identically zero for every `n`** — it is a function of `p̂` alone at fixed `n`, and it carries no information about the sample at all. **A saturated statistic is the resolution of nothing.**

**P11.** The correct instrument is emitted beside it: a **one-sided** bound. The rule of three gives, at 95 % confidence, `p > 1 − 3/n` for `p̂ = 1` and `p < 3/n` for `p̂ = 0` — `0.9997` and `3e−4` at `n = 10 000`. It is exact in the sense that `(1 − 3/n)^n → e^{−3} = 0.0498`, and it is retained as a tested shared function beside `binomialStandardError`.

**P12.** **The verdict does not move.** `T-148`'s exceedance against `T-5b`'s 0.10 is 1.0 and stays 1.0; what moves is what the number beside it means.

### Falsifier

Any exceedance probability, dishing percentile or `flatAt*` boolean moving under the repair. This is a reporting repair; a changed verdict would mean it was not.

### Plan, and the cheap bound

The cheap bound is again a walk and no run: how many saturated proportions in the tree carry a symmetric error? If the answer is one, the repair is local; if it is hundreds, the deliverable is a measurement plus a local repair plus a statement of what a tree-wide repair would cost.

---

## What would falsify the whole approach

That any of the three counts comes back **zero** — the defect having been fixed by another agent between `C-0127` and here — or that the `T-208` count comes back **large**, in which case the gate is not wirable this iteration and `T-208` becomes a repair task with its own blast radius.

---

## The three cheap bounds, run before anything was written

Each is a walk of the committed corpus and no solve. Together they cost under a minute and they decided the whole shape of the deliverable.

| item | question | answer | what it decided |
|---|---|---|---|
| `T-208` | how many result files would the gate fire on today? | **1 of 117** — `T-207`'s own, which quotes the defective strings as its record | the tree is clean, so the gate is **wirable**, and it is wired |
| `T-209` | how many departure fields carry more than two significant digits? | **222 fields in 29 files** strict; **1 422 in 71** on the wider census | the item was raised on **one** field. At 222 a gate is unwirable and a tree-wide repair is 29 study re-runs; the deliverable becomes a central mechanism, a measured audit, and the files this task can re-emit and verify |
| `T-210` | how many saturated proportions carry a symmetric standard error? | **302 of 403 records, in 7 files** | the item was raised on **one** note. Every one of the 302 reads exactly `0.0`, and for the same reason |

**The `T-208` bound also caught its own regular expression.** The first pass, written with a space in the flag class, fired on **87 of 117 files** — every one of them a prose percentage (`"% of"`, `"% over"`). The corrected pattern is `C-0127`'s own and the count is 1. That is the false positive the checker exists to avoid, found in the cheap bound rather than after wiring, and it is why the discriminator is a self-tested part of the tool rather than a regular expression.

## What re-emission this task can honestly carry

`T-136` is read by **five** studies, and every one of them reads `parameters/*` or `recommendedPlacement` — **none reads a departure field**. A rounding change confined to departures therefore cannot reach them, which excludes four heavy re-runs for the price of a `grep`; and `coupling/StapleDropoutStudy.kt` (`T-148`) is **one of the five** and re-runs anyway for `T-210`, so the containment argument is also *measured*. The re-emission set is `T-136`, then `T-148`, which is the order `tools/reemission-order.py` prints.
