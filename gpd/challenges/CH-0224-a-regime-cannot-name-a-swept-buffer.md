# CH-0224 — **`Regime` cannot state the regime of a study that SWEEPS the buffer, and ~~14 of the 21~~ **17 of the 22** studies that name `MagnesiumChlorideBuffer` sweep it — so `P4`'s block is `null` on every result whose regime it exists to gate**

> **RESOLVED, iteration 43** — [`T-286`](../tasks/T-286-a-regime-is-a-set.md) / [`C-0181`](../claims/C-0181-a-regime-is-a-set.md). The grounds are upheld, the census is corrected to **17 sweeping and 5 fixing one, of 22** (§6), and the arity is repaired: the block is a `RegimeSet` with three emitted values. The **per-record** half of §4 is not discharged and is named as the residue.

| | |
|---|---|
| **Against** | [`C-0159`](../claims/C-0159-environment-interface.md) / `T-265`'s `Regime`, and [`ARCHITECTURE.md`](../../ARCHITECTURE.md)'s step 6 — *"a regime block (buffer, valency, gap, bandwidth) — so consuming a result outside the range it was solved in is a gate rather than a reading"* |
| **Raised by** | [`T-272`](../tasks/T-272-emission-layer-remainder.md), by the first attempt to put the block on every record |
| **Grounds** | **methodological** — the block is a property of a **record**, and it was designed for a **file** |
| **Status** | ~~**OPEN.** The key is emitted on every record and is `null` on all of them; the reason is measured below and is a property of the type, not of the studies~~ **RESOLVED, iteration 43** ([`T-286`](../tasks/T-286-a-regime-is-a-set.md) / [`C-0181`](../claims/C-0181-a-regime-is-a-set.md)). **The grounds are upheld and the arity is repaired**: `emission.regime` is now a [`RegimeSet`](../../src/main/kotlin/environment/RegimeSet.kt) — a JSON **array** — with three emitted values, `null` = *not stated* (a residue to be counted), `[]` = *no environment coordinate at all* (a claim), `[…]` = the states solved. Re-derived at `baselineRef = 9620d3ef`: **135 of 135** headed files carried `null`, **17 of 22** electrolyte studies sweep. **Repair 2 of §4 is falsified by the corpus** (`TallGapDeviceB` solves two different buffer lists over two different height ranges, so one widened `Regime` would admit a pair no record carries); repair 3 is what `RegimeSet` is. **Repair 1 — a per-record regime — is NOT discharged and is named as the residue**: a file-granular set is a *necessary* condition only, and it cannot refuse the one mistake `CLAUDE.md` has recorded (`T-3b`'s two profiles per `(concentration, gap)`, 50 read edges). That is `T-272`'s `P4` sweep with a wider edit |

---

## 1. The type

`environment/Regime` carries `bufferMillimolar: Double?`, and its KDoc is explicit that the `null`
is a **claim** and not an omission — *"`null` is a claim, not an omission: `CLAUDE.md` establishes
that ideal mobile salt exerts exactly no osmotic pressure on a neutral grafted layer"*. So the
type has exactly two states: **one** molarity, or **none at all**.

## 2. What the corpus is

Of the **21** studies in `src/main/kotlin` that name `MagnesiumChlorideBuffer`, **14** declare an
explicit list of molarities and solve every state at each of them:

| list | studies |
|---|---|
| `listOf(0.5, 2.0, 10.0)` | `TileEdgeLoadProfileStudy`, `ScaffoldRemainderStudy`, `CutRimChargeStudy`, `SofteningCouplingStabilityStudy`, `RecommendedElementFoldStudy`, `ConcentratedCrossoverStudy`, `MaximumUsableBiasStudy` |
| `listOf(2.0, 5.0, 10.0)` | `NonlinearPbProfileStudy`, `MeanFieldValidityStudy` |
| `listOf(0.5, 1.0, 2.0, 10.0)` | `BeyondMeanFieldGapStudy` |
| `listOf(0.5, 1.0, 2.0, 5.0, 10.0)` | `StrokeAndBlockingForceStudy` |
| `listOf(0.5, 1.0, 2.0)` | `OutputCouplingStudy`, `TallGapDeviceBStudy` |
| `listOf(0.5, 2.0)` | `LargeRotationArmBranchStudy` |

That is not incidental. **`C-0012`'s buffer recommendation, `C-0022`'s five headline states and
`C-0064`'s device partition are all comparisons ACROSS the buffer**, so a study of this device's
electrostatics that did *not* sweep it would be answering a different question.

## 2b. And a study at a SINGLE state cannot state one either

`Regime`'s own constructor carries `require(highestHeightNm > lowestHeightNm)` — a **strict**
inequality — so a range of one point is refused. That is the other half of the corpus:
`tile/HoneycombGrillageStudy` grades the free tile under *"`C-0022`'s solved edge collar at 2 mM,
10 nm, 0.192 V"*, selected by `value("concentration") == 2.0 && value("gapHeight") == 10.0`, and it
is a single named state rather than a range.

So the type admits exactly one shape — **one** buffer over a **range** of heights — and this corpus
is made of the two shapes it does not admit: a **sweep** over buffers (§2) and a **point** in
`(buffer, gap, bias)`. Neither is an accident: a sweep is how this programme compares buffers, and
a point is how a downstream study consumes one upstream solve.

## 3. The consequence, and it is exact

A regime block at **file** granularity therefore has to be `null` on every one of those 14 —
which is the state the type reserves for *"no electrolyte enters this model at all"*, and is the
opposite of true. Emitting a single molarity would be worse: it would be a number no record of the
file was solved at.

So the residue is not *"some studies could not be classified"*. It is:

> **the studies whose result the gate exists to refuse are exactly the studies the block cannot
> describe**, and the studies it *can* describe — the geometry, lattice, placement and plan
> corpus — are the ones with no environment coordinate at all, where `null` is correct and the
> gate has nothing to do.

`T-272`'s own plan declared the falsifier as *"a `regime` block cannot be stated for some record
— then that record is a result whose solved range nobody can name, which is a finding about the
study and not about the schema."* Measured, it is a finding about **neither**: the studies name
their range perfectly well, in a list, and the schema has no place to put a list.

## 4. What would settle it, and what it costs

Three repairs, in increasing order of price:

1. **Per-record regime.** Move the block from the file to the record type a study sweeps —
   `profiles[i].regime`, `folds[i].regime`. Correct, and it touches every `@Serializable` record
   in 21 studies plus a re-run of each: this is `T-272`'s sweep again, with a wider edit.
2. **A `Regime` whose buffer is a set.** `bufferMillimolar: Set<Double>?`, with `reasonToRefuse`
   demanding containment rather than equality. One type, one function, and every existing caller
   changes — `environment/` declares three regimes and `EnvironmentTest`/`RegimeTest` assert on
   them.
3. **A `RegimeFamily` beside `Regime`**, holding the swept axis and yielding a `Regime` per point.
   Additive, which is how layers 1–4 were landed, and it leaves two types where one would do.

None is cheap and none is this task. What is cheap is the statement: **the block as designed
describes a solve, and this corpus's studies are sweeps.**

## 5. What would falsify this challenge

A study of the electrolyte that fixes one molarity and whose file could therefore carry a real
regime. Seven of the 21 do not declare a list — `GoldElectrodePzcStudy`,
`EdgeWidthDependenceStudy`, `RaggedFaceCostStudy`, `TwoSidedCouplingStudy`,
`ZeroBiasRestingPositionStudy`, `PlanarCouplingWallStudy`, `CollarEquilibriumPathStudy` — and
each takes its concentration from a caller or from an upstream result file rather than fixing
one. If any of them is single-buffer on inspection, that is one result file whose block should be
non-null, and it is a repair of a row rather than of the type.


---

## 6. Resolution, iteration 43 — what was repaired and what was not

Filed by [`C-0181`](../claims/C-0181-a-regime-is-a-set.md) / [`T-286`](../tasks/T-286-a-regime-is-a-set.md).

**Upheld.** Every count in §1–§3 re-derives at `baselineRef = 9620d3ef`:
135 of 159 committed result files carry an emission header,
its `regime` is `null` on **135 of 135** and stated on **0**.

**§5's falsifier fires in part and reverses nothing.**
Of the seven studies this challenge could not classify,
**two sweep** (`RaggedFaceCost` at `{0.5, 1.0, 2.0}`, `CollarEquilibriumPath` at `{2.0, 10.0}`)
and **five fix one molarity**, all at 2.0 mM —
`GoldElectrodePzc`, `TwoSidedCoupling`, `ZeroBiasRestingPosition`, `PlanarCouplingWall`,
and `EdgeWidthDependence`, which selects `concentration == 2.0` out of `T-3b`.
A twenty-second study this challenge does not list, `structure/DrawableRaggedFaceStudy`,
sweeps `{0.5, 1.0, 2.0}`.
The corpus census is therefore **17 sweeping against 5 fixing one**, of 22 —
five rows, not a type.
`GoldElectrodePzcStudy`'s `listOf(2.0, 10.0)` is **not** a buffer list:
it is `C-0021`'s two readings of the tile **thickness in nm**, which is why the classification in
`tools/T-286-emit-result.py` is read per study and carries an evidence line.

**The repair is the arity.**
`Regime` describes a **solve** and a file is a **bag of solves**;
the gap and the bias are *arguments* of `pressure`/`force` and are rightly intervals,
while the molarity is a *constructor* argument that identifies **which** environment,
so a sweep instantiates several environments rather than widening one.

**What is NOT discharged**, and it is this challenge's sharpest sentence read one level down:
a file-granular set refuses a consumer asking for a state no record carries,
and cannot refuse a consumer picking the **wrong record** inside a file whose set contains it.
**50 of the 71 addressable read edges land on `T-3b`**, the file `CLAUDE.md` already records as
carrying two solved profiles per `(concentration, gap)`.
Only `§4`'s repair 1 closes that, and it is `T-272`'s.
