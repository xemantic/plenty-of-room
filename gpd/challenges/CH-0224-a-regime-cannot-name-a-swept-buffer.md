# CH-0224 — **`Regime` cannot state the regime of a study that SWEEPS the buffer, and 14 of the 21 studies that name `MagnesiumChlorideBuffer` sweep it — so `P4`'s block is `null` on every result whose regime it exists to gate**

| | |
|---|---|
| **Against** | [`C-0159`](../claims/C-0159-environment-interface.md) / `T-265`'s `Regime`, and [`ARCHITECTURE.md`](../../ARCHITECTURE.md)'s step 6 — *"a regime block (buffer, valency, gap, bandwidth) — so consuming a result outside the range it was solved in is a gate rather than a reading"* |
| **Raised by** | [`T-272`](../tasks/T-272-emission-layer-remainder.md), by the first attempt to put the block on every record |
| **Grounds** | **methodological** — the block is a property of a **record**, and it was designed for a **file** |
| **Status** | **OPEN.** The key is emitted on every record and is `null` on all of them; the reason is measured below and is a property of the type, not of the studies |

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
