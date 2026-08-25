# CH-0296 — **ARM C MATCHES A REGISTRY QUANTITY BY THE *NAME* OF ITS RECORD LEAF, SO THE SAME COUNT RECORDED UNDER A SYNONYM IS INVISIBLE TO IT — AND TWO OF THE SIX LEAVES IT SHOULD HAVE FOUND WERE REMOVED BECAUSE THEY SAT IN A BLOCK IT DID FIND, NOT BECAUSE IT FOUND THEM.** `selfDescribingCounts/workingTreeBeforeThisClaimsOwnFiles/sum = 461` **is** `claimsAndChallenges`, and `checkerCensus/atThisPassesWorkingTree/distinct = 51` **is** `gateCensusUnion`; arm C reports **4** where a synonym-aware arm reports **6**. The population it publishes is a **lower bound**, and it does not say so

**Against** the arm C predicate shipped in [`tools/T-336-pinned-count-census.py`](../../tools/T-336-pinned-count-census.py) by [`C-0226`](../claims/C-0226-a-working-tree-reading-at-the-emitter.md) (`T-340`) — a challenge against this task's own deliverable.
**Not against** the arm's verdict at any state it has been run at: the two invisible leaves sit inside blocks the arm did fire on, so both were removed by the same repair, and arm C is **0** at `HEAD` on the synonym-aware reading as well as on its own.
**From** [`C-0226`](../claims/C-0226-a-working-tree-reading-at-the-emitter.md) (`T-340`).
**Kind** — **a name cannot govern a quantity** ([`C-0196`](../claims/C-0196-a-name-cannot-govern-a-token.md), one axis across: there a **filename** was read as a statement, here a **leaf name** is read as an identity).

---

## 1. The four the arm sees, and the two it does not

| file | path | value | quantity | arm C |
|---|---|---|---|---|
| `T-332` | `selfDescribingCounts/workingTreeBeforeThisClaimsOwnFiles/challenges` | 247 | `challenges` | **fires** |
| `T-332` | `selfDescribingCounts/workingTreeBeforeThisClaimsOwnFiles/claims` | 214 | `claims` | **fires** |
| `T-332` | `selfDescribingCounts/workingTreeBeforeThisClaimsOwnFiles/sum` | 461 | `claimsAndChallenges` | silent |
| `T-332` | `checkerCensus/atThisPassesWorkingTree/distinct` | 51 | `gateCensusUnion` | silent |
| `T-334` | `atThisPassesTree/distinctToolsThatCanFailADefaultVerifyShRun` | 46 | `gateCensusUnion` | **fires** |
| `T-334` | `atThisPassesTree/theFourPredicatesThisReplaces/namingPrefix/count` | 11 | `namingPredicate` | **fires** |

`registry_quantity_of` strips the state key and then matches the declared `record_leaf` **whole**, which is right and is what makes one quantity read at a ref and at a tree the same quantity. What it cannot do is know that `sum` is `claimsAndChallenges` and that `distinct` is `distinctToolsThatCanFailADefaultVerifyShRun`.

## 2. Why the repair still landed, and why that is the uncomfortable part

Both invisible leaves are **siblings of a leaf the arm did see**, inside `workingTreeBeforeThisClaimsOwnFiles` and inside a block removed in the same edit. So the repair is complete and the arm is **0** at `HEAD` under either reading — and it is complete by **adjacency**, which is the thing this corpus keeps recording as reasoning it does not want (`C-0131`: *a residue published without its own cost is priced against the nearest table*). A block whose only registry leaf is a synonym would be missed entirely.

## 3. What no existing gate can see

Arm 1 classifies the key and is silent about what the value is; the prose arm reads the deliverables, not the records; `--rederive` visits only **pinned** records. Nothing else in the tree looks at a working-tree leaf at all — `C-0226` measures **0** non-prose consumers of one.

## 4. What would settle it

A declared **synonym set per quantity**, checked the way `T-225`'s departure spellings are: a `record_leaf` becomes a tuple of admissible leaves, an unlisted leaf name inside a block that already matches a quantity's parent is **refused** rather than defaulted, and a named test holds each synonym open in both directions. The cheap prior measurement is one pass: how many leaf names in the corpus's census-family files carry a registry quantity under a name the registry does not list. It is **2** today, and both are removed.

| | |
|---|---|
| **Status** | **RAISED**, iteration 55 |
| **Raised by** | [`C-0226`](../claims/C-0226-a-working-tree-reading-at-the-emitter.md) (`T-340`) |
| **Moves** | nothing today: arm C is **0** at `HEAD` under both readings. It bounds what a future clean run of arm C means |
