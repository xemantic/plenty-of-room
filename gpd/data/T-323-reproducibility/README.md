# `T-323` — the four emissions `F23` has been measured on

[`C-0216`](../../claims/C-0216-the-placement-and-the-distribution-together.md) §14 reports that
**`F23` fired**: two independent runs of `tile.JointPlacementDistributionStudyKt`, each through
`tools/study.sh` in its own snapshot, were **not** byte-identical — **26 of 1 252 leaves**.
[`C-0217`](../../claims/C-0217-the-cure-at-every-call-site.md) reports what two more runs say after
the repairs [`T-328`](../../tasks/T-328-the-cure-at-every-call-site.md) and
[`T-329`](../../tasks/T-329-an-identity-is-a-threshold-and-a-boolean.md) landed: **4 of 1 255**.

A run cannot assert byte-identity about itself, so the measurement is **external** — and it is only
reproducible if the runs survive. All four do:

| emission | file | emitter | what it is |
|---|---|---|---|
| **A** | `run-a.json` | `b5aa97a`, **before** `T-328`/`T-329` | the first pre-repair run |
| **B** | `run-b.json` | the same | the second pre-repair run — **the artifact `C-0216` is filed against**, retained here verbatim when `gpd/results/` moved on |
| **C** | `run-c.json` | **after** `T-328`/`T-329` | the first post-repair run |
| **D** | `../../results/T-323-the-placement-and-the-distribution-together.json` | the same | the second post-repair run, and the committed artifact |

`run-a.json` and `run-b.json` are **retained unchanged** — `C-0092`'s *a repair must leave the
defect measurable*, and without them `C-0216` §14's table is a number nobody can check. Note the
cost, which is [`CH-0280`](../../challenges/CH-0280-a-retained-before-state-hides-a-stale-quotation.md):
every token these two files carry is a token a corpus census will find, including the ones the
repair superseded.

## Reproducing both measurements

`diff.py` is the classified reader both claims take their tables from. It walks two documents to
their leaves and prints the leaf count, the moved-leaf count, the count of moved **booleans** — the
statistic `CLAUDE.md` says is what makes an irreproducibility cosmetic or not — and every moved leaf
with both readings. It exits `1` if anything moved.

**Before** — `C-0216` §14's own table:

```
gpd/data/T-323-reproducibility/diff.py \
    gpd/data/T-323-reproducibility/run-a.json \
    gpd/data/T-323-reproducibility/run-b.json
```

> `1252 leaves before, 1252 after` / `26 moved, 0 added, 0 removed` / `0 of 250 booleans moved`

**After** — `C-0217` §4's:

```
gpd/data/T-323-reproducibility/diff.py \
    gpd/data/T-323-reproducibility/run-c.json \
    gpd/results/T-323-the-placement-and-the-distribution-together.json
```

> `1255 leaves before, 1255 after` / `4 moved, 0 added, 0 removed` / `0 of 251 booleans moved`

and the four are `screens/0/spearmanAgainstSearched`, `screens/1/spearmanAgainstSearched`, and the
two sentences (`falsifiers/5/note`, `findings/5`) that render them.

**The cross-diff is the other half**, because a repair that stabilised the file by *deleting* the
moving fields would read the same as one that stabilised them:

```
gpd/data/T-323-reproducibility/diff.py \
    gpd/data/T-323-reproducibility/run-b.json \
    gpd/results/T-323-the-placement-and-the-distribution-together.json
```

> `1252 leaves before, 1255 after` / `27 moved, 11 added, 8 removed` / `0 of 249 booleans moved`

The 11 added are `T-329`'s two `identities` records and one `validity` line; the 8 removed are the
bank-slice `convergence` record that `T-329` folded into them. Of the 27 moved, 6 are `T-329`'s own
rewording, 13 are the `f = 0.26` corner and its dependants **returning to run A's reading and
staying there**, and the rest are the Spearman readings.

`extract.py` is the reader `C-0216`'s numbers were taken with; point it at any of the four.

## How the four were produced

Each run is one `tools/study.sh` invocation, which snapshots the tree into its own directory, so the
two runs of a pair share no build state, no Gradle daemon lock and no JIT history:

```
tools/study.sh tile.JointPlacementDistributionStudyKt
```

C and D were launched a minute apart from **bit-identical** snapshot `src/` trees (`diff -rq`,
clean) and took **4 051 s** and **4 077 s**. A `T323_SMOKE=1` environment variable runs the same
study at toy sample counts; **no number in any of these four files came from a smoke run.**
