# CH-0214 — **`HoneycombGrillage`'s beams stopped at the last crossover plane, so a row whose length is not a multiple of 7 bp carried an unsupported, unloaded strip — and the standing uniform-load falsifier fired on it.**

| | |
|---|---|
| **Against** | [`C-0154`](../claims/C-0154-honeycomb-grillage.md) (`T-253`) — [`tile/HoneycombGrillage.kt`](../../src/main/kotlin/tile/HoneycombGrillage.kt)'s `nodeS`, and the claim's `F1` row, which certifies the uniform-load falsifier on the object rather than on the object *at the row lengths it was run at* |
| **Raised by** | [`T-263`](../tasks/T-263-honeycomb-grillage-regrade.md) / [`C-0167`](../claims/C-0167-coupled-cells-on-the-honeycomb-grillage.md) |
| **Grounds** | **in-silico — the repository's own best falsifier, fired on a correct-looking solve** |
| **Status** | **RAISED and REPAIRED in the same iteration.** The repair adds a trailing node where the row has a remainder and adds **nothing** where it does not; **every lattice `C-0154` measured is bit-identical**, asserted as a test and confirmed by a control re-run of `T-253` in which **no number moves** |

---

## 1. What the code did

`nodeS` is built from `planeBasePairs = (0..rowBasePairs step 7)`, so the last node of every beam sits at

```
nodeS.last() = planeBasePairs.last() * rise − lengthS/2
```

which is `+lengthS/2` **if and only if `rowBasePairs` is a multiple of 7**.

Every row `C-0154` ran is: **112, 56, 224, 448** bp — `16 × 7`, `8 × 7`, `32 × 7`, `64 × 7`. The
precondition held at every point of the study that discovered the class, and it is stated nowhere:
not in the constructor's `require` block, not in the `rowBasePairs` KDoc, and not in `C-0154`'s
Conditions row.

## 2. What that costs, at the first row length that violates it

`C-0151`'s recommended block extent is **116 bp**, which is `16 × 7 + 4`. The plane ladder ends at
112 bp and `lengthS/2` is at 116 bp, so the outer **1.36 nm** of the tile — 3.4 % of the face —
carried

- **no beam element**, so `elementOf` clamped to the last element and *extrapolated* a cubic
  Hermite past its own interval,
- **no foundation**, because the Winkler term is assembled element by element, and
- **no load**, for the same reason.

An unsupported, unloaded overhang on an otherwise uniformly supported plate is exactly the state
`CLAUDE.md` names as impossible: *"a uniform load on a uniform Winkler foundation produces no
dishing at all"*. Measured on the `10 × 6` block at 116 bp under a uniform `0.0666534426 pN/nm²`,
the peak face dishing was **0.15 of the stroke** — half again `T-5b`'s whole tolerance, out of a
load case whose exact answer is **zero**.

## 3. Why it was not caught

`C-0154`'s `F1` is discharged on the object, and the object is right; what was wrong is a
**precondition on an argument**, and the study that declared the falsifier never varied that
argument off the ladder. It is `CLAUDE.md`'s *"a numerical guard's own justification is a
statement about a STATE, and it expires when the state moves"*, one level down: here it is not a
guard's justification but a whole *class invariant* that was true of every call site and stated by
none.

**And the falsifier is what found it.** `T-263` wired `F1` in because `C-0154` says a corrugated
face breaks it unless the tributaries are centred; the first smoke run at 116 bp fired it, and the
cause was three lines away from the tributary the falsifier was watching.

## 4. The repair, and the proof it moves nothing

`nodeS` now appends `subdivisions` further nodes from the last plane to `lengthS/2` **when and only
when the remainder exceeds `1e−9` nm**. Where `rowBasePairs ≡ 0 (mod 7)` the branch is not taken
and the node list is unchanged, term for term.

| | |
|---|---|
| asserted as a test | `nodesPerBeam == planeBasePairs.size` and `nodeS.last() == lengthS/2` at 42, 56 and 112 bp; the overhang present and one 4 bp remainder long at 116 bp |
| asserted as a test | a uniform pressure on the `10 × 6` block **at 116 bp** gives `p/k_f` to `1e−9` and zero dishing to `1e−9` |
| control run | `tile.HoneycombGrillageStudyKt` re-run on the repaired source: **not one numeric field of `gpd/results/T-253-honeycomb-grillage.json` moves** |

The control run did find one **prose** field of that file stale against `HEAD`'s own source
(`C-0100` where the source says `C-0056, CH-0066`), which predates this task and is re-emitted with
it — `CLAUDE.md`'s *"a `--committed` control attributes it wholly to `HEAD`'s own staleness"*.

## 5. What is NOT claimed

- No published number of `C-0154` is affected, because no row it ran had a remainder.
- The repair does not make a 116 bp block a model of `C-0151`'s **two-length** raster: the lattice
  still carries one row length, and the 7 bp stagger and the 102 bp interface window are still not
  represented. That limitation is `C-0154`'s own and is carried forward in `C-0167` §9.
- Whether a *free overhang* is the right idealisation of a raster row end past its last crossover
  column is a modelling choice, not a theorem. It is the same choice the smeared plate makes by
  running its `lengthX` to the block extent, which is what keeps the two models comparable.
