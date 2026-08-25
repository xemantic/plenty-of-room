# `T-323` — the two runs `F23` was measured on

[`C-0216`](../../claims/C-0216-the-placement-and-the-distribution-together.md) §14 reports that
**`F23` fired**: two independent runs of `tile.JointPlacementDistributionStudyKt`, each through
`tools/study.sh` in its own snapshot, are **not** byte-identical.

A run cannot assert byte-identity about itself, so the measurement is **external** — and it is
only reproducible if both runs survive. `run-a.json` is the **first** emission, retained here
verbatim; the **second** is the committed artifact,
[`gpd/results/T-323-the-placement-and-the-distribution-together.json`](../../results/T-323-the-placement-and-the-distribution-together.json),
which `C-0216` is filed against because that is the file the corpus reads.

Reproduce the classification with:

```
python3 - gpd/data/T-323-reproducibility/run-a.json \
          gpd/results/T-323-the-placement-and-the-distribution-together.json <<'PY'
import json, sys
a, b = (json.load(open(p)) for p in sys.argv[1:3])
def walk(o, p=""):
    if isinstance(o, dict):
        for k, v in o.items(): yield from walk(v, p + "/" + k)
    elif isinstance(o, list):
        for i, v in enumerate(o): yield from walk(v, p + "/" + str(i))
    else: yield p, o
da, db = dict(walk(a)), dict(walk(b))
moved = [k for k in da if da[k] != db.get(k, "<absent>")]
print(len(da), "leaves,", len(moved), "moved")
print("booleans differing:", [k for k in da if isinstance(da[k], bool) and da[k] != db[k]])
PY
```

which reads **1 252 leaves, 26 moved** and **`0`** booleans differing.

`extract.py` is the reader `C-0216`'s numbers were taken with; point it at either file.

**What is NOT here.** No source was changed after either run, deliberately: repairing the two
defects §14 names would leave the committed emitter unable to reproduce the committed artifact.
Both repairs are queued with their call sites named.
