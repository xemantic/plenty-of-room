# CH-0154 — *"the rule now lives once, by name"* was true of `structure/` and false of the tree: the constant is keyed on a **leaf name** where the rule is about a **record**, and a **second rounding implementation** with no `digitsByKey` parameter at all emits six of the corpus's result files

| | |
|---|---|
| **Against** | [`C-0129`](../claims/C-0129-result-file-hygiene.md)'s `T-209` deliverable: *"The rule is about a **record type**, so it now lives once, by name"*, and its census of what is left — *"199 fields in 27 files"* |
| **Raised by** | [`C-0131`](../claims/C-0131-departure-and-saturation-audits.md) (`T-212`), iteration 30 |
| **Grounds** | **three measurements, none of which needs a solve.** (1) A bare `departure` appears under **eleven** parents in `gpd/results/`, and `T-193`'s is in **volts**; (2) `roundedForActuatorResult` has **no `digitsByKey` parameter**, so the six files on that path could not have obeyed the rule by any edit at their own emission sites; (3) the census keys on **one spelling of four** of the same quantity in the same records — 199 fields in 27 files against **601 in 63** |
| **Status** | **UPHELD and repaired in the same task** |
| **What moves** | **No physical quantity and no verdict.** What moves is the shape of the mechanism (`record/spelling` keys), the elimination of one of the six duplicate rounding implementations, and the honest size of the outstanding work: 27 files becomes 63, of which `T-212` closes 35 and `T-214` carries 36 |

## The charge

`C-0129` is right that the rule is about a record type. Its own §3 says so in those words.
What it then wrote is

```kotlin
val DEPARTURE_DIGITS_BY_KEY: Map<String, Int> = mapOf(
    "departure" to …, "relativeDeparture" to …, …
)
```

which is keyed on the **leaf name**, and `roundedForResult` applies a `digitsByKey` entry to the whole subtree under that key **wherever it appears**.
The two statements are not the same statement, and the corpus is what separates them.

### 1. A bare `departure` is not a departure

A census of the 119 committed result files finds the leaf key `departure` under **eleven** distinct parents:

| parent | fields | files | is it a residual between two refinements of one solve? |
|---|---|---|---|
| `reproductions` | 412 | 36 | **yes** |
| `upstreamChecks` | 288 | 3 | no — a comparison against a **carried** upstream number |
| `convergence` | 152 | 33 | **yes** |
| `stationLattice` | 30 | 1 | no — a lattice coordinate difference, in **nm** |
| `correctedMarginTransfers` | 20 | 1 | no |
| `blockingBiasTransfers` | 15 | 1 | no |
| `invariants` | 10 | 1 | no — **nm** |
| `identity` | 5 | 1 | no — a plan-margin regrouping residual, in **nm** |
| `potentialOfZeroCharge` | 3 | 1 | **no, and it is in VOLTS** |
| `census` | 2 | 1 | no |
| `units` | 1 | 1 | it is a **string** |

`gpd/results/T-193-gold-electrode-pzc.json` carries

```json
{"versusStandardHydrogen": 0.51, "derivedVersusStandardHydrogen": 0.511420712,
 "departure": 0.001420712,
 "readStatus": "READ DIRECTLY — Adnan et al., Phys. Chem. Chem. Phys. 26:21419 (2024) …"}
```

That is a difference of two **electrode potentials**, in the locked units, against a value read directly out of a paper.
It is exactly the kind of number `RESULT_ABSOLUTE_FLOOR` **does** reach, and two significant digits would discard determined information about a literature comparison —
the opposite of what the rule exists to do.
`C-0129`'s own §7 anticipates the argument for order-one departures (*"applying two digits there discards determined information; it never fabricates any"*) and adopts the rule anyway; **it does not anticipate a departure that is not dimensionless at all**, because the possibility is invisible from inside the leaf-name framing.

And `gpd/results/T-160-edge-width-dependence.json` carries the same spelling **twice, in one file, meaning two different things**: 63 `departures[*].relativeDeparture` fields, which are that study's **answer** and are declared at six digits at its own emission site, and 13 `reproductions`/`convergence` ones, which are diagnostics.
A map keyed on the leaf name cannot say both. A map keyed on `record/spelling` can, and does.

### 2. The rule could not reach `actuator/` at all

`src/main/kotlin/actuator/ActuatorResultRounding.kt` is the second of the **six** independent rounding implementations `CLAUDE.md` records, and its KDoc says why it is a copy:

> This is a deliberate **copy** of the pattern `structure/ResultRounding.kt` established for `T-5`, not an import of it: `T-3` owns `actuator/` and does not own `structure/`, and two agents were live in `structure/` while this ran.

The copy was correct when it was written. Its consequence is that `roundedForActuatorResult()` takes **no arguments at all** — there is no `digitsByKey` to pass — so `T-3`, `T-4`, `T-60`, `T-76`, `T-149` and `T-157` could not have carried the departure rule by any edit at their own emission sites.
`T-60` is one of the 27 files `C-0129` listed as *"each is now one re-run away"*. It was not: it was one re-run **and a change to a rounding implementation in another package** away.

The repair is a delegation, and it is a refactoring rather than a precision change because the two constants are the same — `ACTUATOR_RESULT_SIGNIFICANT_DIGITS == RESULT_SIGNIFICANT_DIGITS` and `ACTUATOR_RESULT_ABSOLUTE_FLOOR == RESULT_ABSOLUTE_FLOOR`, now asserted in `ActuatorResultRoundingTest` so that the equality cannot lapse silently.

### 3. The census measures one spelling of four

`C-0129`'s strict predicate is the leaf key `departure` inside a `reproductions` or `convergence` record.
The same records carry three more spellings of the same quantity:

| spelling | fields inside a departure record | files |
|---|---|---|
| `departure` | 564 | 40 |
| `relativeDeparture` | 503 | 35 |
| `departureFromFinest` | 337 | 27 |
| `relativeDepartureInStroke` | 12 | 1 |

Counted at more than two significant digits, the gate's predicate is **199 fields in 27 files** and the rule's own scope is **601 in 63** — a factor of **3.0** in fields and **2.3** in files.
This is `C-0129`'s own finding one level out: it opens by saying that each of its three items *"named one instance, and each instance was a population"*, and its own census then names one **spelling** of a population of four.

## Why it matters, and why it is not merely tidy

Because the gate decision rests on it.
`C-0083`'s rule — *a gate that cannot come clean is not a gate* — is a statement about a **predicate**, and a predicate can always be narrowed until the tree is clean.
`T-212` therefore wires the gate on `C-0129`'s own strict predicate, where the tree now reads **0 fields in 0 files**, and **prints the wider scope beside it, ungated**, so that the residue is published rather than defined away.
A gate that reports a number it does not enforce is the honest form of `C-0083`; a gate that reports only what it enforces is how a narrow predicate becomes a claim of cleanliness.

## What would have caught it earlier

One line: `grep`-ing the corpus for **every parent** a departure key appears under, rather than for the key.
It costs a walk of 119 files and it is the same instrument `C-0129` used three times in the same hour — applied to the *shape* of the key rather than to its *count*.
