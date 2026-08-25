# `T-337`'s cheap bound — retained

`cheap-bound.py` produces every number of
[`gpd/tasks/T-337-the-exceedance-beside-every-verdict.md`](../../tasks/T-337-the-exceedance-beside-every-verdict.md)
§2, over the committed corpus, with **no JVM, no solve and no third-party package**.

```
gpd/data/T-337-cheap-bound/cheap-bound.py
```

`output.txt` is its output at the commit this task's Formulate and Plan were written at.

It imports `tools/T-327-flatness-resolution.py` and reuses that census's own predicates —
`_records`, `_flat_p90_booleans`, `clopper_pearson`, `determinacy`, `resolution_band`, `FILES` —
rather than reimplementing them, so [`C-0223`](../../claims/C-0223-the-resolution-of-the-flatness-census.md)'s
population is reproduced **by construction** and not by accident.

What it answers, and why each answer is load-bearing for the scope:

| section | question | answer |
|---|---|---|
| 2a | does `C-0223` §4b's population C reproduce | `2 678` / `1 238` / `106` / `87`, and the seven-file split, member for member |
| 2b | which files and blocks carry the `87` | seven files with positives, three with none |
| 2c | how many are recoverable with **no** re-emission | `0` within-record, **`5`** by a whole-corpus `p90` join, so **`82`** need one — and all five read `UNDETERMINED` |
| 2d | what the sweep is expected to yield | `25` of the `87` sit at `p90 ≥ 0.0975`, and `7` of `7` donors in that band are inside the undetermined interval |
| 0 | the locked constants, read out of the sources rather than assumed | `TOLERANCE = 0.10` and `4 000` grading realisations at all ten studies |

**The two donor rows below `p90 = 0.0975` are a refusal, not a calibration.** The corpus offers
`5` donor records below `p90 = 0.09`, and `48` of the `87` sit there; no transfer is offered.
