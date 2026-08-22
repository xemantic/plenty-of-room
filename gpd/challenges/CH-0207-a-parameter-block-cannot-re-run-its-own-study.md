# CH-0207 — a result file's **parameter block is rounded like everything else**, so it is not a description its own study can be re-run from: `T-3a`'s wall charge is committed as `−0.398665238` against the `−0.3986652379247042` it used, and feeding the committed literal back **misses that file's own 2 V force by one unit in the last emitted place**. The channel is **live**, not latent — seven call sites read a parameter block back as an input — and which files are exposed is decided by a rendering convention with no rule behind it: **49 result files write their parameters as JSON numbers and 59 as strings, zero mixed**

| | |
|---|---|
| **Against** | [`gpd/README.md`](../README.md)'s `results/` row — *"Every parameter of the run is in the file, so the result is reproducible from it alone"* — and [`CH-0205`](CH-0205-a-number-typed-as-a-string-is-untyped-as-well-as-unrounded.md)'s classification of the input-precision channel as *"**latent**, not live"* |
| **Raised by** | [`C-0159`](../claims/C-0159-environment-interface.md) (`T-265`) |
| **Grounds** | **in-silico** — the two inputs are fed to the same solver through the same call path and the outputs are rounded at the file's own declared precision; plus a census of the committed corpus |
| **Status** | **CLOSED — REPAIRED** by [`C-0162`](../claims/C-0162-round-outputs-never-inputs.md) (`T-268`), on this challenge's own **first** proposed repair: *"an input is not a result"*, as `PARAMETER_RECORDS` in `structure/ResultRounding.kt` — a sticky exemption defaulted **in the layer**, so all six Kotlin rounding entry points obey it with no edit of their own. `T-3a`'s `nominalTileChargeDensity` now reads **`−0.3986652379247042`**, exactly the value this challenge says the study solved with, and that file moved **4 parameter fields and nothing else**. ~~`T-265` is additive by mandate and a repair here moves result files~~ — it did, in the 42 files the rule can move, re-emitted in `tools/reemission-order.py`'s topological order. **Its census row is separately challenged**: [`CH-0210`](CH-0210-the-parameter-block-census-is-not-reproducible.md) finds **49 / 59 / 0** irreproducible at this challenge's own commit (**50 / 63 / 14–20**), which strengthens the thesis rather than weakening it |

---

## The observation

`T-3a` emits, in `runParameters`:

```json
"nominalTileChargeDensity": -0.398665238
```

The value the study computed and solved with is `−0.3986652379247042`
(`−tile.projectedChargeDensity × manningSurvivingFraction(2, l_B) / 2`).
The parameter block is a JSON number, so `JsonElement.roundedForResult()` rounds it to the file's
nine significant digits along with every result in the file.

`1.9e−10` relative, in an **input**. Measured through `environment/ElectrodeGapEnvironment`, which is
otherwise bit-identical to `T-3a`'s own private `Solver`:

| fed | raw | at nine digits | `T-3a` carries |
|---|---|---|---|
| the derived charge | `−938.232490471837` | `−938.23249` | `−938.23249` ✔ |
| the **committed literal** | `−938.2324905056215` | `−938.232491` | ✘ |
| the derived charge | `0.5204863343834494` | `0.520486334` | `0.520486334` ✔ |
| the **committed literal** | `0.5204863316199002` | `0.520486332` | ✘ |

(the force is `forceOnTile` at 2 mM, 5 nm, 2 V over 1600 nm²; the second pair is that file's own
`forceDecayLength` at 2 mM, 3 nm, 0 V.)

So the file's stated parameter does not reproduce the file's stated result. Neither number is
*wrong*: the physics is unchanged at ten significant digits and every verdict `C-0008` rests on
stands. What fails is `gpd/README.md`'s contract.

## Why it is not a curiosity

**Seven call sites in `src/main/kotlin` read a parameter block back as an input**, not as
documentation:

| reader | file | keys |
|---|---|---|
| `window/UpstreamResults.readScfResults` | `T-1d` | `restingLoad`, `monomerVolume`, `tileArea` |
| `window/UpstreamResults.readActuatorResults` | `T-3` | `trustedBiasCeiling`, `biasCeiling` |
| `window/UpstreamResults.readLayoutResults` | `T-14` | `tileArea`, three duplex allowables |
| `anchoring/PathCountConsistencyStudy` | `T-136` | `winnerMargin`, `recommendedMinimaxOverStroke`, … |
| `coupling/BuildableWidthCountPhaseStudy` | `T-188`, `T-3b` | `freeStrokeBuildable`, the collar |
| `structure/RaggedFaceCostStudy` | `T-218` | `recommendedAxialExtentBasePairs` |
| `stability/DoublingLadderRepairStudy` | `T-149` | `elementCeilingSafety` |

`T-136`'s `winnerMargin = 0.419511928` and `recommendedMinimaxOverStroke = 0.0682200897` are **JSON
numbers**, rounded on the way out and read straight back in. That is the channel `CH-0205` measured
on the *string* route and recorded as *"checked at `HEAD` all seven resolve to a numeric leaf, so the
channel is **latent, not live**"*. On the **numeric** route it is live, and it has been live for as
long as those readers have existed.

## The split nobody chose

Across the 147 committed result files:

| parameter block rendered as | files | rounded by the emission layer? |
|---|---|---|
| JSON **numbers** | **49** | **yes** |
| JSON **strings** | **59** | **no** — `roundedForResult` dispatches on the JSON type and passes strings through |
| mixed | **0** | — |

1 004 numeric leaves sit in the 49. Which side a study lands on is a per-study rendering convention —
`T-1c` and `T-1d` interpolate everything into strings, `T-3a` and `T-14` emit `Double`s — and it is
the convention, not any stated rule, that decides whether that study's inputs survive a round trip.
`CLAUDE.md` already records the mirror image of this (*"the integral-number rendering is a per-package
convention frozen by the committed files"*); this is the same freedom deciding something larger than
a rendering.

## What a repair would have to be

Not proposed here, and deliberately: each of these moves committed files, and `T-265` is additive.

1. **An input is not a result.** The emission layer already dispatches per key
   (`DEPARTURE_DIGITS_BY_KEY`); a parameter block is a *record type*, and the rule *"a `parameters` /
   `runParameters` / `citedInputs` leaf is emitted at full round-trip precision"* is the same shape as
   the departure rule and lives in the same place. Its blast radius is provable offline: 1 004 fields
   in 49 files.
2. **Or the contract goes.** `gpd/README.md` could say what is true instead — that a parameter block
   is a *description* and the study's source is the definition — but then the seven readers above are
   reading a description, and at least `T-136`'s two are feeding a rounded number into a solve.
3. Either way the choice should be made once and **tested**, because the failure is silent: the
   corpus's own re-emission and staleness machinery compares *outputs*, and an input that has been
   rounded reproduces its own file perfectly right up until somebody re-runs from it.

## What this does not touch

No physics. No verdict. `C-0008`'s forces, `C-0022`'s collar, `C-0011`'s pressures and every claim
downstream of them are unmoved — `C-0159` reproduces all of them at departure `0.0` once the input is
the one the study used. The challenge is against a **contract on `results/`**, and against one
sentence of `CH-0205`'s classification.
