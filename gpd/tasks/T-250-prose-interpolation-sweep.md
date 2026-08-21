# T-250 — the 46 other result files whose **prose** carries an unrounded `Double`, and the promotion of the census from an audit to a gate

| | |
|---|---|
| **Leaf** | none — a **process** task protecting the machine-readable artifact of every leaf |
| **Raised by** | [`C-0153`](../claims/C-0153-unrounded-prose-interpolations.md) (`T-249`), which measured the class at **757 tokens in 47 files**, repaired `T-164`, and published the residue with its own cost |
| **Status** | see [`TASKS.md`](../../TASKS.md) |

---

## 1. Formulate

### The defect, restated

`JsonElement.roundedForResult` dispatches on the JSON **type** and passes strings through — correctly,
because a string is not a number.
By the time the serialisation boundary sees `"channel B at s = 0 is $x"` the `Double` is gone,
so the cure is **necessarily per call site** and the only thing that can hold the class closed is a
**census** wired as a gate.

`C-0153` proved the shape, measured its false-positive rate **exhaustively at 0 of 757**, repaired one
file, and left the rest.
This task is the sweep and the promotion.

### Numeric target and acceptance predicates

| | predicate |
|---|---|
| **P1** | the census, re-run over the committed corpus **before any repair**, reported with its tokens / string fields / JSON pointer sites / files, and the source-side call-site count it implies |
| **P2** | every affected result file is repaired at its **source call sites** — `Double.roundedForProse(digits, floor)`, with the digits and the floor chosen **per site** — and re-emitted |
| **P3** | the re-emission order is [`tools/reemission-order.py`](../../tools/reemission-order.py)'s topological sort over the **whole** set at once, and its dependency-constraint count is **asserted non-zero** before the order is trusted (`C-0153` found the tool silently reporting `0` on a path argument) |
| **P4** | what moved is reported **by kind** — prose digits / departure / other numeric / verdicts and wording / booleans / added / removed — against each file's **committed** version read out of `git` |
| **P5** | **nothing is stale, as an identity**: every over-precise token in a committed file's prose is replaced by *exactly* the rounding its own call site declares, counted, with the unexplained residue reported |
| **P6** | `tools/check-result-file-hygiene.py --prose` reads **0 tokens in 0 files**, and the line is promoted from the audit list to the **gate** list beside `--conversions` and `--departures` |
| **P7** | the promotion is **mutation-tested in both directions** (`C-0127`, `C-0150`): restoring the audit-only exit policy, or narrowing the gate's predicate, must each fail a **named** test |
| **P8** | a **descent manifold** (`T-129`, `T-113`, `T-133`, `T-152`) is classified as such and not reported as staleness, separated from pre-existing drift by a `--committed` control |

### Units and conventions

Nothing physical is computed. Units unchanged and untouched:
nm, pN, pN/nm, pN/nm² = 1 MPa exactly, `k_BT = 4.141947 pN·nm` at 300 K, aqueous buffer with stated Mg²⁺.
Every re-emission re-runs its study's own solves; **no physics may move**, and `P4` is how that is checked.

---

## 2. Plan

### The cheap bound runs first, and it resizes the task

The census is artifact-side and exact: a decimal token above `RESULT_SIGNIFICANT_DIGITS = 9`
significant digits inside a **string** value of a result file is a number that did not go through the
rounding layer, by construction.
Run before any repair over the working tree it reads **748 tokens in 703 string fields in 48 files**
(`T-249`'s residue of 46 plus `T-249`'s own deliberate quotation and a sibling's `T-246`).

**The token count is not the work, and this is what the cheap bound buys.**
Collapsing array indices out of the JSON pointers gives ~200 distinct sites, and the distribution is
extremely skewed: `T-21`'s 351 tokens are **340** copies of one `bindingCeilingName` expression plus
**11** `runParameters` entries, and `T-192`'s 49 are **42** copies of one `verdict` sentence plus 7.
So the source-side edit count is of order **200**, not 748, and it is bounded above by the pointer
count in every file where a pointer is a single expression.

### The shape of the repair, and why it is a judgement rather than a mechanism

Three classes, and each takes a different rounding:

1. **A whole string that is a bare number** — `"kuhnSegmentVolume" to peg.kuhnSegmentVolume.toString()`.
   These are `Map<String, String>` parameter blocks. `roundedForProse()` at the default nine digits.
2. **A number inside a sentence** — `"… phi = $phi)"`. Nine digits, default floor.
3. **A departure inside a sentence** — `"the two groupings agree to 4.440892098500626E-16 nm"`.
   `DEPARTURE_SIGNIFICANT_DIGITS = 2` and `floor = 0.0`, because `RESULT_ABSOLUTE_FLOOR` is a claim in
   the locked units (`P-18`) and the default floor would render the whole sentence as `0.0`.

Class 3 is the reason this cannot be automated: it needs to be read.

### Cost, read out of the runbook rather than guessed

37 of the 48 files are in the runbook; 7 carry a stated runtime summing to **144 min**
(`T-124` 55, `T-127` 24, `T-123` 18, `T-178` 15, `T-216` 13, `T-188` 11, `T-157` 8).
The remaining 41 are seconds to minutes.
[`tools/study-batch.sh`](../../tools/study-batch.sh) is the vehicle — one snapshot, one Gradle build,
N runs, with the copy-back **re-baselined per run** — so the sweep costs one cold compile rather than 48.

### What would falsify this approach

| | falsifier |
|---|---|
| **F1** | a re-emission moves a **numeric** field, a boolean or a verdict — the repair was not confined to prose |
| **F2** | the offline prediction of what each file's prose becomes differs from what the re-emission actually wrote |
| **F3** | a repaired file still carries a token, i.e. the source-side census missed a call site the artifact-side census sees |
| **F4** | `tools/reemission-order.py` reports **zero** dependency constraints over the set — the tool was called wrongly again |
| **F5** | a mutation of the promoted gate passes every named test |
| **F6** | a file moves in a way a `--committed` control run reproduces, i.e. the movement is pre-existing staleness or a descent manifold rather than this task's |
| **F7** | the gate cannot come clean, because a file in the set carries a token that is **deliberate** — `C-0083`: a gate that cannot come clean is not a gate |
