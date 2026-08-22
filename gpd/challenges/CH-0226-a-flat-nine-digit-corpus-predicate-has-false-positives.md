# CH-0226 — **A corpus over-precision predicate fixed at nine digits reports a study's own DECLARED precision as a defect — 49 leaves in 2 files, 48 of them one study rounding to three digits with a reason — and the predicate's own arithmetic over-counts by 8 more: `10.0 ** 23` is one unit in the last place from `1e23`, and Python's `**` and Java's `Math.pow` disagree at exactly that exponent.** `CH-0223`'s corpus figure is **41 369**; the same predicate, evaluated with the scale factor the Kotlin actually uses, is **41 361**, and read at each study's own declared precision it is **41 312 in 12 files**. Its headline — **41 297** in the seven emitters — is invariant under every one of these readings and is untouched

| | |
|---|---|
| **Against** | [`CH-0223`](CH-0223-seven-emitters-call-no-rounding-function.md) §2 — *"The predicate is `roundForResult(v, 9, floor = 0) != v` — the emission rule applied to the committed value"*, and the **corpus** figure it produces, *"41 369, in 17 of 152 files"* |
| **Raised by** | [`T-278`](../tasks/T-278-emission-header-residue.md), which had to build a mirror of that predicate in order to simulate its own repair offline |
| **Grounds** | **methodological** — a gate that cannot come clean, because its predicate is narrower than the rule it enforces in one direction and wider in the other |
| **Status** | **OPEN as to the residue, CLOSED as to the instrument.** The predicate is repaired in `tools/T-278-emitter-rounding-census.py`, which reads each study's declared precision from its own source; the residue it then reports is 22 leaves in 7 files, and 16 of those are outside the Kotlin rule's reach |

---

## 1. The measurement, reproduced first

`CH-0223`'s number is reproducible, and reproducing it is what exposed the two things below. A
Python mirror of `structure/ResultRounding.kt` — the parameter-record exemption, the
`record/spelling` departure map, the integral-number rendering and the absolute floor — run over
`git archive HEAD` returns **41 369 in 17 files**, to the leaf, and the seven emitters' share
**41 297 / 99.83 %** with it.

**The 41 297 is invariant and is not in dispute.** It is the same under every reading below,
because the seven declare no per-key precision and emit nothing near the arithmetic boundary of
§3. What moves is the corpus denominator.

## 2. What the flat predicate cannot see

`roundedForResult` takes a `digitsByKey` map, and a study that has measured its own precision uses
it. `electrostatics/ScaffoldRemainderStudy` (`T-195`) declares

```kotlin
digitsByKey = mapOf("relativeDeparture" to 2, "worstReproductionDeparture" to 2,
                    "boltzmannWeight" to 3),
floor = 0.0
```

with a comment giving the reason. Its 47 `boltzmannWeight` values are therefore **correct at three
significant digits**, and a nine-digit reading of the artifact alone reports all 47 as over-precise.

Read at each study's own declared precision the corpus figure is **41 319 in 14 files**:

| file | flat nine | at the study's declared precision | false positives |
|---|---|---|---|
| `T-195-scaffold-remainder.json` | 47 | 2 | **45** |
| `P-18-determined-precision.json` | 2 | 0 | 2 |
| `T-275-simulated-tile-census.json` | 2 | 0 | 2 |
| `T-267-mechanics-on-imported-design.json` | 1 | 0 | 1 |
| | **41 369 / 17 files** | **41 319 / 14 files** | **50** |

The `P-18`, `T-275` and `T-267` three are two mechanisms one level down, and one of them turned
out to be in the instrument rather than in the corpus (§3).

## 3. And the predicate's own arithmetic over-counts by 8, at exactly one decimal exponent

`roundForResult` scales by `10.0.pow(digits − 1 − floor(log10|v|))`. Kotlin's `pow` goes through
`Math.pow`, which is **correctly rounded** for an integral exponent; Python's `**` is not. Over the
range this corpus reaches they differ at **exactly one exponent**, `k = 23`:

```
10.0 ** 23      = 1.0000000000000001e+23      (Python)
float("1e23")   = 1e+23                       (the correctly-rounded decimal, and Java's Math.pow)
```

One unit in the last place, and it decides the answer: `2.1000000000000002e−15` rounds to
`2.1e−15` under the first and to **itself** under the second. `T-190` emits
`2.1000000000000002e−15`, which is the second — so the Kotlin is the second and a mirror written
with `**` is wrong.

Measured over the whole committed corpus, **14 leaves** in 5 files change their over-precision
verdict between the two conventions, every one of them at a magnitude of `1e−15`, and in every one
the corrected convention agrees with what the study actually emitted:

| | flat nine digits | at each study's declared precision |
|---|---|---|
| with `10.0 ** k` — `CH-0223`'s figure | **41 369** in 17 files | 41 319 in 14 |
| with the decimal literal, which is what the Kotlin uses | **41 361** in 14 files | **41 312** in 12 |

So of the 72 leaves `CH-0223` attributes to studies outside its seven, **49 are a study's own
declared precision and 8 are the predicate's own arithmetic**; 15 are real, and all 15 are in files
written by a Python emitter in `tools/`.

## 4. Why this is `C-0083`'s standard and not a quibble

*A gate that cannot come clean is not a gate.* Wired at nine digits flat, this predicate can never
read zero while any study in the corpus declares a per-key precision — and declaring one is exactly
what `C-0093`, `C-0129`, `C-0131` and `C-0138` spent four iterations making studies do. The gate
would fire hardest on the studies that obey the rule best, which is the rate at which a gate gets
switched off.

The repair is that the census reads the **source** for the declaration and the **artifact** for the
value — the same two-sided reading `tools/T-272-header-census.py` is built on, and for the same
reason: *a census that asked one of them twice could not see a disagreement between them.*

## 5. What is left, named rather than estimated

After `T-278`'s sweep re-emitted the seven, and with the scale of §3 corrected, the residue at each
study's declared precision is **15 leaves in 5 files, and every one of them is written by a Python
emitter in `tools/`** — `T-225` 8, `T-198` 3, `T-194` 2, `T-200` 1, `T-211` 1.

**Not one Kotlin-written result file in the corpus is over-precise at its own declared precision.**
That is the state the source-side gate can be wired on and the artifact-side one cannot, which is
§4's point stated as a number.

## 6. What would falsify this challenge

A study whose declared `digitsByKey` the repaired census reads and whose values are nevertheless
over-precise at that declaration — which would mean the declaration is not what the study applies.
One re-run of that study settles it.
