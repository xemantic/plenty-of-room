# CH-0097 — `C-0073`'s rounding-site table names the wrong studies, and `P-19`'s ranking is built on it: `coupling/` does not round `T-129` and **does** round `T-16` and `T-17`, `window/` rounds a third file, `actuator/` rounds five and two of them are in other packages — and read on a derived census the ranking **inverts**, `window/` being the cheapest of the three to re-emit and not the most exposed

| | |
|---|---|
| **Against** | [`C-0073`](../claims/C-0073-determined-precision-of-a-result-file.md)'s `roundingSites` table (`gpd/results/P-18-determined-precision.json`, field `roundingSites`), and the `P-19` ranking that quotes it |
| **Raised by** | [`C-0082`](../claims/C-0082-result-reader-census.md) / [`P-22`](../tasks/P-22.md), while re-adjudicating `P-19` on the corrected census `CH-0092` asked for |
| **Grounds** | **methodological, and the same one as `CH-0092`** — the table's study sets were assembled by inspection, and inspection is what `CH-0092` established cannot produce a census of this repository by hand. The two challenges are the same defect on two different tables of one claim. |
| **Status** | **RAISED. `C-0073`'s verdict is not affected** — direction (a) is right, the ninth digit is not worth buying, and every measured `determinedDigits` stands, because a determined precision is a property of the *solver*, not of the study list. What is corrected is **which studies each site governs**, and therefore what re-emitting one would cost. |

---

## 1. What the table says, and what the source says

`P-18` measured six independent rounding implementations and recorded, per site, which result files
it governs. Three rows are wrong about that.

| `C-0073`'s row | studies that actually reach that rounding | verdict |
|---|---|---|
| `window/WindowResultRounding.kt` **(T-2, T-25)** | `T-2`, `T-25`, **`T-118`** | **incomplete**, no ellipsis |
| `actuator/ActuatorResultRounding.kt` **(T-3, T-4)** | `T-3`, `T-4`, **`T-60`, `T-21`, `T-76`** | **incomplete**, no ellipsis — and two of the five are in **other packages** (`crossover/`, `stability/`) |
| `coupling/CouplingResultRounding.kt` **(T-113, T-123, T-129 …)** | `T-101`, `T-113`, `T-122`, `T-123`, **`T-16`, `T-17`** — and **not `T-129`** | **one named member is wrong**, which no ellipsis covers |
| `structure/ResultRounding.kt` **(shared, 43 studies)** | 50 | **not challenged** — seven studies have been added since iteration 14, so 43 was very likely right when written |

`T-129` (`anchoring/RangeRobustPlacementStudy.kt`) imports
`com.xemantic.nano.plentyofroom.structure.roundedForResult` and emits through it. It is a
`structure/` file, not a `coupling/` one. Every one of the other studies listed above existed when
`P-18` was written.

The correction is mechanical and is now derived rather than inspected:
`tools/result-reader-census.py` emits a `roundingSites` section from the same declaration graph
that produced the reader census, and `gpd/results/P-22-result-reader-census.json` carries it.

## 2. Why the members matter — `T-16` and `T-17` are the two the resyntheses read

This is not bookkeeping. `T-16` and `T-17` are read by **both** window resyntheses
(`WindowResynthesisStudy` and `SecondResynthesisStudy`), so the coupling rounding's re-emission
does not stop at `coupling/`: it propagates to `T-25` and then to `T-118`. `C-0073`'s row shows a
site with two named files and one ellipsis; the closure is **eight** result files and two test
classes.

Derived, per site — emissions, then everything reachable from them through the reader graph:

| site | studies | emits | downstream result files | tests reading either |
|---|---|---|---|---|
| `actuator/ActuatorResultRounding.kt` | 5 | `T-3`, `T-4`, `T-21`, `T-60`, `T-76` | `T-2`, `T-16`, `T-25`, `T-118` | 4 |
| `coupling/CouplingResultRounding.kt` | 6 | `T-16`, `T-17`, `T-101`, `T-113`, `T-122`, `T-123` | `T-25`, `T-118` | 2 |
| `window/WindowResultRounding.kt` | 3 | `T-2`, `T-25`, `T-118` | **none** | 1 |
| `structure/ResultRounding.kt` | 50 | 50 files | `T-2`, `T-16`, `T-17`, `T-25`, `T-101`, `T-113`, `T-118`, `T-122`, `T-123` | 4 |

## 3. What this does to `P-19`

`P-19` is *"the four rounding sites `P-18` measured and did not change"*, and `CH-0092` closes with

> **`window/` has two more consumers than that ranking assumed**, and re-emitting it at six digits
> would move `T-25` and `T-118` again.

That sentence is true of `window/` as a **reader** — it is `CH-0092`'s own corrected census of
`T-1d` and `T-1f`, which are `window/`'s *inputs*. **The cost of re-emitting a rounding site is a
statement about its outputs**, and read that way the ranking **inverts**:

- **`window/` is the CHEAPEST of the three.** It emits `T-2`, `T-25` and `T-118` and **nothing
  outside `window/` reads any of them** — `T-2` has no reader at all, `T-118` has none, and
  `T-25`'s only reader is `SecondResynthesisStudy`, which is inside the site and is re-run anyway.
  One test class (`window/SecondResynthesisTest.kt`) reads them.
- **`actuator/` is the most exposed**: five emitted files, four downstream, four test classes —
  and its determined precision is **4** digits against a printed 9, the largest over-print of the
  three.
- **`coupling/` sits between them**, and its exposure is entirely through `T-16` and `T-17`, the
  two files `C-0073`'s own row omits.

**But `window/` carries the one thing the other two do not: an ORDER.** `T-118` reads `T-25`, so a
re-emission must run `WindowResynthesisStudy` **before** `SecondResynthesisStudy`, and a re-run of
the second against a stale first is *exactly* the failure `CH-0092` documented — `T-118`'s
`reproductions/22/relativeDeparture` sat at `8.79e−7` for an iteration and collapsed to `0.0` when
the order was honoured. **The risk in `window/` is not size, it is sequence**, and that is invisible
in a per-site digit count.

## 4. Disposition

- **No re-emission is performed.** `P-19`'s standing reason holds: changing code that produces
  published results costs a re-run and a diff of everything downstream, and this is a ranking job.
- `C-0073` should be **annotated in place**, as it was for `CH-0092`, rather than overwritten.
- The corrected table is derived, not asserted: re-run `tools/result-reader-census.py --emit` and
  the `roundingSites` section is rebuilt from the sources. It is checked on every `tools/verify.sh`.
- **The generalised lesson is `CH-0092`'s, and this is its second instance in one claim**: a table
  of *"which studies does this touch"* assembled by reading is a census, and a census of this
  repository cannot be hand-held. `C-0073` produced two of them and both are wrong in the same
  direction — **too few members** — because a search finds what it is shaped to find and a reader
  stops when the list looks complete.
