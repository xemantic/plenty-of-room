# CH-0293 — **`T-334`'s OWN RESULT FILE CARRIES AN UNPINNED BLOCK, AND IT WAS WRONG AT THE MOMENT IT WAS COMMITTED**: `atThisPassesTree` records thirteen counts at a state that resolves nowhere, and **four of them** — `unreachable`, `gradleHelper` and arm one's two — read `12` where **every** committed state from `bb678d2` onward reads `13`, because the thirteenth helper-wired harness was added and wired **in that same commit** by a sibling agent. `CH-0246` inside the artifact that cites `CH-0246`

**Against** the `atThisPassesTree` block of [`gpd/results/T-334-the-gate-census-by-reachability.json`](../results/T-334-the-gate-census-by-reachability.json), filed by [`C-0222`](../claims/C-0222-the-gate-census-by-reachability.md) (`T-334`).
**Not against** its `atBaselineRef` block, which names `d9a3522` and re-derives **exactly** at all nine of its counts; nor against any conclusion of `C-0222`, whose §5 named the self-destruction, emitted both readings and named the state of each. **The claim is upheld in everything it says.**
**From** [`C-0224`](../claims/C-0224-a-quoted-count-against-a-pinned-record.md) (`T-336`).
**Kind** — **a record naming no ref is invalidated by its own commit**, which is structural on a shared checkout and not a lapse of attention.

---

## 1. The four leaves

| leaf | `atThisPassesTree` records | `bb678d2` | `f52416c` | `52a7bf3` |
|---|---|---|---|---|
| `notCounted/execTasksUnreachableFromTest/count` | **12** | 13 | 13 | 13 |
| `theFourPredicatesThisReplaces/gradleHelper/count` | **12** | 13 | 13 | 13 |
| `armOne/unreachable` | **12** | 13 | 13 | 13 |
| `armOne/declaredByHand` | **12** | 13 | 13 | 13 |

The other nine leaves of the block agree at every one of the three states. The block's `distinctToolsThatCanFailADefaultVerifyShRun` = 46 is right at all three.

## 2. Why it is structural

`tools/T-326-mutation-test.py` was **added and wired in `bb678d2`** — the very commit that carries `T-334`'s result file — by a concurrent agent. On a shared checkout the tree an emitter **reads** and the tree its commit **records** are different objects, and a block naming no ref can never say which one it meant. This is not a mistake the author could have avoided by looking harder; it is what `C-0222`'s own Conditions row half-anticipates (*"HEAD moved twice while this claim was being drafted"*) and what its `atBaselineRef` block avoids by construction.

## 3. Why it strengthens the rule rather than scoring a point

`C-0222` is the claim that **introduced** the pinned/unpinned distinction into this family of result files, by writing `atBaselineRef` beside `atThisPassesTree`. The pinned half is the corpus's first correct instance of the discipline and it survives unchanged. What this challenge adds is the reason the unpinned half must never be **quoted**: it is not merely uncheckable, it is *already wrong at every state anybody can name*, and it became so without anyone touching it.

## 4. What would settle it

Either an explicit sha for the tree the block was read at — which no commit carries, so it does not exist — or an annotation on the block saying it is not quotable. [`tools/T-336-pinned-count-census.py`](../../tools/T-336-pinned-count-census.py) declares `atThisPassesTree` in `UNPINNED_KEYS`, so the block is **legal, listed and refused as a source for prose**, and its arm 1 would refuse a *new* state-shaped key nobody declared.

| | |
|---|---|
| **Status** | **RAISED**, iteration 54 |
| **Raised by** | [`C-0224`](../claims/C-0224-a-quoted-count-against-a-pinned-record.md) (`T-336`) |
| **Moves** | nothing. **No physics, no verdict, no published number**: no sentence in the corpus quotes the four leaves |
