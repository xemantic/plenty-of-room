# `T-294` — the two independent emissions `F14` is measured on

`CH-0281` ([`C-0217`](../../claims/C-0217-the-cure-at-every-call-site.md), `T-328`) is right that a
falsifier whose verdict is measured **outside** the run must not emit `"fired": false` — a run
cannot assert byte-identity about itself. `T-294`'s `F14` therefore emits **`null`** with a
`measuredIn` field naming this directory, and the measurement lives here.

| file | what it is |
|---|---|
| `run-a.json` | the first emission of `gpd/results/T-294-the-tied-regrade-at-the-other-cross-section.json` |
| `run-b.json` | the second, from the same sources in the same snapshot, run to completion after the first had exited |
| `diff.py` | the comparison, by kind — the classification `CLAUDE.md` asks for, because *"`0 unclassified` and `0 verdicts`" is what makes an irreproducibility cosmetic and no scalar can say it* |

Run it with:

```
python3 gpd/data/T-294-reproducibility/diff.py
```

Both runs are retained rather than only the winner, because `C-0092`'s rule is that **a repair must
leave the defect measurable** — and here there may be no defect, in which case the retained pair is
what says so.
