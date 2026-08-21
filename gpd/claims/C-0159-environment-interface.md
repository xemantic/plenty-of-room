# C-0159 — **THE TWO PACKAGES WITH NO COUNTERPART IN THE FIELD ARE NOW CITABLE WITHOUT THE TILE**, and re-expressing them found the thing that reads a corpus back wrong. An `environment` interface — `pressure(h)`, `force(h, bias)`, `decayLength` — carries the SCF brush, the 1-D 2:1 Poisson-Boltzmann gap and the **2-D** electrode edge, and reproduces their committed numbers at **departure `0.0` at each file's own emission precision**: `T-1d`'s five solved SCF pressures at six digits, `T-3a`'s whole six-point bias ladder, its diffuse-layer drop, its bulk Debye length and its own `forceDecayLength` at nine, and `T-3b`'s refinement-1 centre-line load and taper width at nine. `decayLength` is a **`ScreeningLength`**, so `CH-0004` — substituting one of this project's three correct Debye lengths for another — is now a **refused operation** rather than a reading. **43 new tests, no existing file touched, and provably no committed number moved.** Two findings came out of the re-expression, and the second is a live corpus defect: the SCF layer's answer depends on **which heights were solved before it**, deterministically, at `2.2e−13` — and **`T-3a`'s own `runParameters` cannot re-run `T-3a`**, because the parameter block is rounded at the emission boundary like everything else, which is worth exactly one unit in the last emitted place of its 2 V force ([`CH-0207`](../challenges/CH-0207-a-parameter-block-cannot-re-run-its-own-study.md))

| | |
|---|---|
| **Task** | [`T-265`](../tasks/T-265-environment-interface.md) — an `environment` interface, so the two packages with no counterpart in the field can be cited without the tile |
| **Leaf** | none — step 4 of [`ARCHITECTURE.md`](../../ARCHITECTURE.md) |
| **Verification type** | **logical** (a design claim about this repository's structure) **+ in-silico** (the re-expression is checked against committed result files by re-running the solves through the new interface) |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOT empirically demonstrated.** This claim is about **software structure and provenance**, not about the device. `PASS` here means the interface returns the numbers the packages behind it already returned, and that nothing else moved. **No new physics is asserted and no object is measured** |
| **Verdict** | **PASS on `P1`, `P2`, `P3`, `P4`, `P5`.** Neither declared falsifier fired: no committed number moved (`F1`), and the interface is satisfiable **without a tile** (`F2`) — asserted of the sources, not only of the tests. Two departures below the emission precision are reported as findings rather than tolerated silently, and one of them raises [`CH-0207`](../challenges/CH-0207-a-parameter-block-cannot-re-run-its-own-study.md) |
| **Provenance** | New sources [`environment/Environment.kt`](../../src/main/kotlin/environment/Environment.kt), [`environment/Regime.kt`](../../src/main/kotlin/environment/Regime.kt), [`environment/GraftedLayerEnvironment.kt`](../../src/main/kotlin/environment/GraftedLayerEnvironment.kt), [`environment/ElectrodeGapEnvironment.kt`](../../src/main/kotlin/environment/ElectrodeGapEnvironment.kt), [`environment/ElectrodeEdgeEnvironment.kt`](../../src/main/kotlin/environment/ElectrodeEdgeEnvironment.kt), [`quantities/ScreeningLength.kt`](../../src/main/kotlin/quantities/ScreeningLength.kt), [`quantities/StatedRatio.kt`](../../src/main/kotlin/quantities/StatedRatio.kt). **43 new gate-named tests, written first and watched fail**, in [`quantities/ScreeningLengthTest.kt`](../../src/test/kotlin/quantities/ScreeningLengthTest.kt) (9), [`environment/RegimeTest.kt`](../../src/test/kotlin/environment/RegimeTest.kt) (11), [`environment/EnvironmentTest.kt`](../../src/test/kotlin/environment/EnvironmentTest.kt) (14) and [`environment/CommittedReproductionTest.kt`](../../src/test/kotlin/environment/CommittedReproductionTest.kt) (9). The whole Kotlin suite is green with them in: **3 064 tests in 172 classes, 0 failures, 0 errors**. **Not one existing file was edited** — `git status` carries these paths as additions only — so **no committed result file moves, no study is re-run, and no claim's number changes** |
| **Conditions** | 300 K; `k_BT = 4.141947 pN·nm`; lengths nm, forces pN, pressures `pN/nm²` = MPa. Buffer as stated per environment: 2 mM MgCl₂ in every reproduction here. Sign convention **fixed before deriving**: `pressure` and `force` are positive when the environment pushes the two bounding bodies **apart** |
| **Consumes** | [`C-0011`](C-0011-scf-density-profile.md) / `T-1d` (the SCF layer and its five solved pressures), [`C-0008`](C-0008-electrostatic-force-and-decay-length.md) / `T-3a` (the 1-D 2:1 gap, the Stern series, the force ladder and `forceDecayLength`), [`C-0022`](C-0022-tile-edge-load-profile.md) / `T-3b` (the 2-D edge solve and its taper fit), [`C-0003`](C-0003-crossover-valid-layer-response.md) (the `GraftedLayerModel` contract the interface is written on), [`CH-0004`](../challenges/CH-0004-screening-decay-length.md) (which decay length is which), [`C-0110`](C-0110-device-b-tall-gap.md) (counterion dominance is about ion content, not decay), [`C-0031`](C-0031-bracketed-root-repair.md) and `P-18` (the emission precisions this claim's reproductions are read at) |
| **Constrains** | Supplies the typed **regime** block [`T-268`](../tasks/T-268-emission-layer.md) has to serialise (buffer, valency, gap, bandwidth) — it exists as data now, so step 6 has something to write rather than something to invent. Raises [`CH-0207`](../challenges/CH-0207-a-parameter-block-cannot-re-run-its-own-study.md) against `gpd/README.md`'s reproducibility rule and against `CH-0205`'s *"latent, not live"* |

---

## 1. What the interface is, and why those three members

```
interface Environment {
    val name: String
    val regime: Regime                 // buffer, valency, gap, bias, band — as DATA
    val referenceArea: Double          // nm^2, ONE by default
    val referenceHeightNm: Double      // the state decayLength is read at
    val respondsToBias: Boolean
    fun pressure(heightNm: Double): Double            // pN/nm^2 at zero applied bias
    fun force(heightNm: Double, biasVolts: Double): Double   // pN over referenceArea
    val decayLength: ScreeningLength
}
```

Three implementations, all four of this repository's grafted-layer models reaching the first one:

| environment | what is behind it | `pressure` | `force` | `decayLength` |
|---|---|---|---|---|
| `GraftedLayerEnvironment` | `AlexanderBoxLayer`, `StrongStretchingLayer`, **`SelfConsistentFieldLayer`** | the model's `disjoiningPressure` | `Π·A`, **bias-independent** | `−P/(dP/dh)` in the layer |
| `ElectrodeGapEnvironment` | `PoissonBoltzmannGap` + the **Stern series** | the solve's first integral | `forceOnTile` at an applied bias | `−F/(dF/dh)` in the gap |
| `ElectrodeEdgeEnvironment` | **`PoissonBoltzmannEdge`**, the 2-D solve | width-averaged over the half-width | `−verticalForcePerUnitEdge · L` | the **lateral** taper centroid |

`referenceArea` is **one square nanometre unless the caller states a footprint**. That is the whole
point of the layer written as a default: `1600.0` is a property of §3's tile, not of a polymer layer
or an electrolyte, and importing it is the dependency this step exists to remove.

## 2. `P2` — the reproductions, at each file's own emission precision

Every row is `roundForResult(derived, digits) == committed`, i.e. **departure exactly `0.0`** at the
precision the file declares. `T-1d` is six digits (`P-18`'s solved-height precision); `T-3a` and
`T-3b` are nine.

| file | quantity | state | committed | departure |
|---|---|---|---|---|
| `T-1d` | SCF disjoining pressure | 6, 8, 10, 13, 16 nm; `N = 250`, `σ = 0.024`, des Cloizeaux | `0.22962`, `0.0991462`, `0.0489115`, `0.0179812`, `0.00603546` | **0.0** ×5 |
| `T-3a` | `forceOnTile` | 2 mM, 5 nm, 0 / 0.1 / 0.25 / 0.5 / 1 / 2 V | `−0.404614173` … `−938.23249` | **0.0** ×6 |
| `T-3a` | diffuse-layer drop | 2 mM, 5 nm, 0.25 V | `0.0954395508` | **0.0** |
| `T-3a` | bulk Debye length | 2 mM | `3.92687853` | **0.0** |
| `T-3a` | `forceDecayLength` | 2 mM, 3 nm, 0 V | `0.520486334` | **0.0** |
| `T-3b` | centre-line load | 2 mM, 10 nm, 0.192 V, refinement 1 | `0.0391263836` | **0.0** |
| `T-3b` | fitted taper width | same | `9.50854193` | **0.0** |

And the re-expression identity is checked **bit for bit**, not at an emission precision:
`environment.pressure(h) == model.disjoiningPressure(chain, h)` at 6, 10 and 16 nm, `==`, no
tolerance. The interface is the package; the reproductions above are what ties the package to the
corpus.

## 3. `P4` — the regime, and `CH-0004` made unrepresentable

A validity range in prose is respected by whoever reads it. `Regime` is the tuple a consumer is
**refused** on: `bufferMillimolar`, `electrolyte`, `counterionValency`, `temperatureKelvin`, the
separation range, the bias range and the `bandwidthHz` — `@Serializable`, so `T-268` has something
to write. `reasonToRefuse(source)` returns a **sentence** rather than a boolean, in the order this
corpus has been bitten in: the salt first, then the valency (`Ξ ∝ q³`, so divalent is a different
problem and not a rescaling), then the temperature, the ranges, and the band.

The sharper half is `decayLength`'s type. `CLAUDE.md` records that *"the Debye length"* is three
different correct numbers here, and `C-0031`'s slit eigenvalue is a fourth on a different **axis**.
A `ScreeningLength` carries `where` and `axis`, so:

```
ratioOf(gap.decayLength, gap.bulkScreeningLength)   // THROWS: where = the confined gap
                                                    //         against the bulk reservoir
ratioOf(edge.decayLength, gap.decayLength)          // THROWS: lateral against normal
statedRatio(gap.decayLength, gap.bulkScreeningLength) // the finding, with BOTH states rendered
```

`T-3a`'s own `decayOverBulkDebye` and `T-3b`'s `decayLengthOverDebye` are legitimate and are exactly
that second form — `ratioOf`'s refusal message already named the escape (*"quote the comparison with
the two states it spans"*) and the package did not provide it. `StatedRatio` does.

## 4. `P5` — no tile, asserted of the sources

Every environment in the tests is built from a **material**, a **charge density** and a **length**.
The gate is not that the test happens not to construct a tile; it is a scan of
`src/main/kotlin/environment/*.kt` asserting that none of them imports `structure`, `tile`,
`coupling` or `crossover`. It passes, and it will keep passing when somebody adds a fifth
environment — which is the difference between a property and an observation.

## 5. Finding 1 — the SCF layer is order-dependent through its warm start

`SelfConsistentFieldLayer` warm-starts each self-consistency solve from the field the **previous**
solve converged to. So its answer at one height is a function of which heights were solved before
it, deterministically:

| route | `P(6 nm)` |
|---|---|
| `pressureAt` on a virgin layer — what `T-1d`'s `pressureRoutes` calls | `0.229620432589 66836` |
| `disjoiningPressure`, the `GraftedLayerModel` contract method | `0.229620432589 61756` |

They differ by `2.2e−13` relative, and the cause is not the JIT: the contract method's own validity
check solves the **resting height** first, which leaves a different warm field behind. Measured five
times in one JVM: the three virgin readings are identical to the last bit and so are the two warmed
ones.

It moves nothing. `T-1d` is emitted at six significant digits, `P-18` set that count from the
solved-height precision, and both routes round to `0.22962`. It is recorded because it is the honest
answer to *"why is this not bit-identical"*, and because it is a second instance of `CLAUDE.md`'s
*"a search's PATH is not reproducible even when its answer is"* — here the path is a **cache**.

## 6. Finding 2 — a parameter block cannot re-run its own study

This one is a live corpus defect and it has its own challenge.

`gpd/README.md` says of `results/`: *"Every parameter of the run is in the file, so the result is
reproducible from it alone."* `T-3a`'s `runParameters.nominalTileChargeDensity` is
**`−0.398665238`**. The number the study actually used is **`−0.3986652379247042`** — the parameter
block is rounded at the emission boundary like every other number in the file.

`1.9e−10` relative in the **input**, and measured through the environment:

| fed | `forceOnTile(5 nm, 2 V)` | at nine digits | `T-3a` says |
|---|---|---|---|
| the derived charge | `−938.232490471837` | `−938.23249` | `−938.23249` ✔ |
| the committed literal | `−938.2324905056215` | `−938.232491` | ✘, one unit in the last place |
| the derived charge | `decayLength(3 nm, 0 V) = 0.5204863343834494` | `0.520486334` | `0.520486334` ✔ |
| the committed literal | `0.5204863316199002` | `0.520486332` | ✘, two units |

**It is a live channel, not a hypothetical one.** Seven call sites in `src/main/kotlin` read a
parameter block back as an input, and the corpus splits cleanly: **49 result files render their
parameter block as JSON numbers** — which the rounding layer rounds — **and 59 as strings**, which it
passes through untouched. Zero mixed. Which side a study lands on is a per-study rendering
convention with no rule behind it. See
[`CH-0207`](../challenges/CH-0207-a-parameter-block-cannot-re-run-its-own-study.md).

## 6a. What this does NOT say

It does not say any committed number is wrong. Every reproduction in §2 lands on the committed value
at the file's own precision once the input is the one the study used. What it says is that the
**file** is not a sufficient description of its own run, which is a statement about `results/`'s
contract and not about any physics in it.

## 7. Validity range

- **This is a design claim about this repository.** It is not evidence about the device, and the
  numbers in §2 are the ones already published — re-derived through a new call path, which is a
  provenance statement and not an independent confirmation.
- **The three environments are the ones `ARCHITECTURE.md` names.** `material/` and `poroelastic/`
  are listed in layer 4 and are **not** behind this interface: neither exposes a `pressure(h)` a
  separation is the argument of. A `poroelastic` environment would need a fourth member (a corner
  frequency) and the `bandwidthHz` field is where it will attach; nothing here anticipates its shape.
- **`decayLength` is a local logarithmic derivative, not a bulk screening length**, and it does not
  exist at every state: at 2 mM and 5 nm the force at zero bias is already past its own sign change,
  `−F/(dF/dh)` is `−2.49 nm`, and `ScreeningLength` refuses it. That refusal is deliberate and it is
  why `referenceBiasVolts` is a stated constructor argument.
- **The 2-D edge inverts its bias on the 1-D problem**, as `T-3b` does: a 2-D Stern inversion would
  cost ~34 edge solves for a boundary condition the tile interior already fixes. Stated, inherited,
  not repaired here.
- **`respondsToBias = false` for a grafted layer is a property of the models in `brush/`**, which
  contain no bias coordinate at all. The underlying physical statement — that ideal mobile salt
  contributes exactly zero to a neutral layer's disjoining pressure — is `CLAUDE.md`'s, and a
  *charged* or ion-binding layer would not satisfy it.
- **The regime's height range for a grafted layer is derived from the model** (dry thickness to
  resting height) and is therefore **lazy**: reading `regime` on an SCF environment costs a bracket
  of solves. `pressure` does not consult it, because the model already enforces its own range and
  adding a second differently-worded guard would be a change of behaviour.
