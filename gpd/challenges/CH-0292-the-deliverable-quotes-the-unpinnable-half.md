# CH-0292 — **THE DELIVERABLE QUOTES THE ONE FIGURE OF THE PAIR ITS OWN RESULT FILE CANNOT PIN, AND THAT FIGURE OCCURS AT NONE OF THE REPOSITORY'S 298 COMMITS.** `gpd/results/T-332-fifteenth-answers-synthesis.json` records the challenge-and-claim census **twice** — `selfDescribingCounts.*.atRef` = **`246 / 213 / 459`** at a resolved `baselineRef` of `d7b7074`, and `workingTreeBeforeThisClaimsOwnFiles` = **`247 / 214 / 461`** at no state at all — and [`ANSWERS.md`](../../ANSWERS.md) line 1385 prints the second. The rule was already written, by the previous pass, into its own emitted record: *"only `atRef` is emitted, and the deliverable quotes the ref rather than the tree"*

**Against** the challenge-and-claim census as `ANSWERS.md` line 1385 states it, filed by [`C-0220`](../claims/C-0220-fifteenth-answers-synthesis.md) (`T-332`), and against the `workingTreeBeforeThisClaimsOwnFiles` key its emitter writes.
**Not against** the `atRef` half of that same file, which is correct and re-derives exactly; nor against [`C-0222`](../claims/C-0222-the-gate-census-by-reachability.md)'s block in [`DECISIONS-FOR-NDI.md`](../../DECISIONS-FOR-NDI.md), **every** figure of which is pinned to a sha and re-derives.
**From** [`C-0224`](../claims/C-0224-a-quoted-count-against-a-pinned-record.md) (`T-336`).
**Kind** — **a record at an unresolvable state is not a record a deliverable may quote**, and the failure is a *choice between two available readings* rather than a staleness.

---

## 1. The two readings, and which one is in print

| | pinned, `atRef` at `d7b7074` | unpinned, `workingTreeBeforeThisClaimsOwnFiles` | in `ANSWERS.md` |
|---|---|---|---|
| challenges | **246** | 247 | **247** |
| claims | **213** | 214 | **214** |
| together | **459** | 461 | **461** |

The forward reading in the same sentence — *"its own finished tree reads 248 / 215"* — is unpinned too.

## 2. Why this is a defect and not a rounding

`(247, 214)` and `(248, 215)` occur at **0** and **0** of the repository's **298** commits, exhaustively searched. They are readings of an uncommitted intermediate tree, so **no state anybody can name produces them** — and the file that would let a reader check them records the other reading, unquoted.

`CH-0182`'s *a census over a corpus that contains the census destroys itself* is the reason a working-tree reading exists at all, and it is not a licence to quote one: the discharge is to quote the **`atRef`** figure and let it be one pass behind, which is exactly what the fourteenth pass's own emitted note prescribes.

## 3. What no existing gate could see

[`tools/trace-answers.py`](../../tools/trace-answers.py) matches a bare token corpus-wide and `247` is cited elsewhere; the departure gate walks JSON *numbers* and this is prose; [`tools/check-corpus-identifiers.py`](../../tools/check-corpus-identifiers.py) prints the true figure on **every run** and nothing compares the two. `ANSWERS.md`'s own text says so twice — *"what is missing has never been the number and is still not: it is the **COMPARISON**, and the two gates that could make it are already wired."*

## 4. What would settle it

The substitution is handed to the coordinator with `C-0224`: quote **`246 / 213 / 459` at `d7b7074`**, naming the sha, and say that the tree reading is deliberately not quoted. Those three figures are pinned by `T-332`'s own file and re-derive there, so the corrected sentence cannot go stale — which a corrected numeral would.

| | |
|---|---|
| **Status** | **RAISED**, iteration 54 |
| **Raised by** | [`C-0224`](../claims/C-0224-a-quoted-count-against-a-pinned-record.md) (`T-336`) |
| **Moves** | one sentence of `ANSWERS.md`. **No physics, no verdict and no result file** |
