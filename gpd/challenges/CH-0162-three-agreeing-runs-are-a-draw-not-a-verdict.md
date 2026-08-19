# CH-0162 — three agreeing runs are a **draw from a distribution**, not a verdict about a file: the reading `C-0131` called *"not reproducible from `HEAD`'s own code"* is drawn by **7 of 10** independent runs, so three agreeing runs happen **34 %** of the time by chance — and the iteration-13 tree, run unmodified today, lands on the reading iteration 13 did **not** emit

| | |
|---|---|
| **Against** | [`C-0131`](../claims/C-0131-departure-and-saturation-audits.md) §6: *"Three independent runs agree on `ranges[1]` to the last digit and the committed file disagrees with all three. So `gpd/results/T-129-range-robust-placement.json` at `HEAD` **is not reproducible from `HEAD`'s own code**, and was not before this task began"* |
| **Raised by** | [`C-0135`](../claims/C-0135-descent-manifold-width.md) (`T-215`), iteration 31 |
| **Grounds** | **one ensemble and one archaeology run.** Ten independent emissions put `ranges[1]` at `0.0365712568` in **7** and at `0.0364754519` in **3**, including a **fresh run at `HEAD`** on the committed side; and `git archive cf7de13` run unmodified today lands on the reading iteration 13 did not emit |
| **Status** | **PARTLY UPHELD.** `C-0131`'s *observation* is exact and its numbers reproduce. Its *inference* — that three agreeing runs convict the baseline — is not available, and the phrase *"not reproducible from `HEAD`'s own code"* is true of the wrong object |
| **What moves** | **No physical quantity, no verdict and no recommendation.** What moves is what `T-215` was queued to establish, and the shape of the standing rule *"re-run identical code before attributing a movement"*: the re-run count a conclusion needs is set by the **basin distribution**, which is measurable and was not measured |

## The charge

`C-0131` did the right thing and did it twice: it saw a file move, it re-ran identical code, and when that was not decisive it paid for a **third** run from a `--committed` snapshot.
`CLAUDE.md` records the outcome approvingly — *"a control re-run that fires on the baseline rather than on the change is a finding, not a nuisance"* — and it is a finding.

But the inference *"three runs agree, therefore the committed file is wrong"* rests on an unstated premise: that a run of this study is **deterministic given its code and inputs**.
It is not. `ranges[1]` takes exactly two values, and over ten independent emissions the split is **7:3**.

Under that split, three independent runs all landing on the majority reading has probability `0.7³ ≈ 0.34`.
**A third of the time, `C-0131`'s experiment returns exactly the result it returned, with nothing whatever wrong with the baseline.**
Its conclusion was reached on evidence about three times weaker than it reads, and the correct reading — *"the file reproduces up to a two-valued manifold"* — is not the one it wrote.

## The decisive measurement, and it costs one run

The clustering `C-0131` saw is real and looks temporal: iterations 13 and 28 on one reading, iteration 30 and its three runs on the other.
That pattern is what makes *"something changed in the tree"* the natural hypothesis, and `C-0131` could not exclude it because it never ran a tree older than its own `HEAD`.

`git archive cf7de13 | tar -x` into a fresh snapshot and run `anchoring.RangeRobustPlacementStudyKt` unmodified.
**It emits `0.0365712568`** — the *other* reading, the one iteration 13 did not commit.

Nothing in the repository between `cf7de13` and `HEAD` is responsible.
And the fresh ensemble closes it from the other side: `B2`, a clean run at `HEAD` in its own snapshot, lands on `0.0364754519`.

## What the correction is

Not a repair to `C-0131` — its 35-file sweep, its gate and its nine predicates are untouched, and `F1`'s verdict (*"this sweep moves zero of that 0.60 %"*) is **exactly right** and is confirmed here.
The correction is to one sentence and to a habit:

- **the sentence**: `T-129` at `HEAD` **is** reproducible, up to a two-valued manifold in one of its four ranges and nine of its thirty-one subsets, with every boolean, every `bindingStates` list and every verdict bit-stable across ten emissions spanning seventeen iterations;
- **the habit**: when a re-run differs, the question *"how many runs"* has a measurable answer. A basin census is `n` runs of a study already written, and it is the difference between *"the baseline is wrong"* and *"the baseline is one draw of two"*.

## What would overturn this

A third reading of `ranges[1]`, or a run of the `cf7de13` tree that lands on `0.0364754519` **reproducibly** — which would restore a temporal explanation and make the archaeology run the outlier instead.
Ten members is a sample, and this challenge says so in the same breath as it says `C-0131`'s three were.
