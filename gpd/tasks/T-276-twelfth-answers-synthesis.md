# T-276 — the twelfth `ANSWERS.md` synthesis, and the first one that has to carry a REVERSED verdict

| | |
|---|---|
| **Leaf** | `A8.2` |
| **Verification type** | **logical** — a reconciliation of two outward-facing documents against the claim corpus and the task queue, with every mechanised half run by a retained checker and recorded before and after, and every unmechanised half stated as a named drift class |
| **Units** | nothing physical is computed here. Every number is quoted **at the precision its owning claim states**, and the units are the locked ones: nm, pN, pN/nm, pN/nm² = 1 MPa exactly, `k_BT = 4.142 pN·nm` at 300 K, aqueous buffer with stated Mg²⁺ |
| **Documents in scope** | [`ANSWERS.md`](../../ANSWERS.md) and [`DECISIONS-FOR-NDI.md`](../../DECISIONS-FOR-NDI.md) |
| **Predecessors** | the eleven previous passes — `C-0067`, `C-0078`, `C-0080`, `C-0088`, `C-0091`, `C-0115`, `C-0124`, `C-0130`, `C-0144`, `C-0155`, `C-0165` |

---

## 1. Formulate

### What makes this pass different from the eleven before it

Every previous synthesis carried **refinements**: a number moved, a ground moved, a question was
answered, a count was re-derived. In each case the deliverable's *verdict* survived and its
supporting sentence was restated.

Iteration 40 carries a **verdict that flipped**.
[`C-0167`](../claims/C-0167-coupled-cells-on-the-honeycomb-grillage.md) (`T-263`) re-graded the
coupled cells on [`C-0154`](../claims/C-0154-honeycomb-grillage.md)'s honeycomb **grillage** instead
of on an `OrigamiGrillage` over a smeared `equivalentSheet`, and:

- the smeared reading **reproduces `C-0151`'s four published percentiles at `≤ 6.3e−10`** in the same
  process, so the difference is the model and nothing else;
- at `C-0151`'s recommended state the cell moves **`0.0773373597` → `0.145086839`** (`f = 0.30`) and
  **`0.0821458169` → `0.149852804`** (`f = 0.26`);
- **`0` of `64`** cells clear `T-5b`'s 0.10 at the 90th percentile against **`15` of `64`** smeared,
  and `C-0151`'s published *"2 flat cells of 8"* is **`0` of 8**;
- the honeycomb reads worse at **4 000 of 4 000** paired realisations at the recommended cell;
- it is **not a multiplier** — the median of the per-realisation ratios runs **`1.06375481` to
  `2.47485493`** over the 64 cells and is **not monotone in the path count** — so no published table
  can be rescaled and no design can be recovered by re-reading an existing sweep at a shifted
  tolerance;
- **what fails is the COUPLING, not the block**: the uncoupled four-layer honeycomb block is flat at
  **`0.0501417315`** and **`0.0522223659`** at the two ends of `C-0116`'s measured band, and the
  recommended cell is flat at **zero defects** (**`0.0626407003`**). It is `C-0087`'s **measured**
  staple dropout, acting on a tile whose across-helix rigidity is `24/7` smaller than the corpus
  believed, that takes it past the tolerance.

**Both deliverables state the coupled reading**, in six passages between them, and
[`CH-0213`](../challenges/CH-0213-the-recommended-coupled-design-does-not-survive-the-honeycomb-grillage.md)
names the four claims the mechanism reaches (`C-0151`, `C-0146`, `C-0142`, `C-0118`).

### The hypothesis this pass exists to falsify

*"A reversal is a value substitution: strike the old number, write the new one, done."*

It is not, for three reasons that are all in `CLAUDE.md` already:

1. **A synthesis drifts by keeping its ANSWERS, not by mis-copying its numbers.** The answer that
   has to move here is *"a coupled four-layer tile is flat under the measured folding statistics"* —
   a sentence this programme has been carrying since iteration 25 and has already restated three
   times without withdrawing it.
2. **A verdict that survives can survive on a different ground**, and its converse: a verdict that
   dies can leave a *neighbouring* verdict standing on a ground that also moved. The uncoupled
   flatness story survives; but `10 × 6`'s celebrated *"no coupling-fraction threshold at all, flat
   even at `f = 0`"* is a smeared-model statement, and on the grillage the enhancement-free block
   dishes **`0.132443428`** and is **not** flat.
3. **`DECISIONS-FOR-NDI.md`'s failure mode has a sign** — every defect its two audits found
   *under-claims* — and a reversal is the one event that can push a customer document the *other*
   way, because it turns a standing "yes" into a "no". Both directions have to be checked in one
   pass.

### The finding the cheap bound is expected to produce

`C-0167` is not new physics: the mechanism is `C-0154`'s, filed in **iteration 38**, and it is one
integer — a honeycomb block's interfaces are not a path graph, only half the in-plane adjacent pairs
are bonded, so `OrigamiSheet.acrossHelixRigidity` overstates a honeycomb `D_⊥` by `24/7 = 3.42857×`.

**Predicate `P0`, checked before any edit: is `C-0154` carried in either deliverable?**
If it is not, then the *cause* of iteration 40's reversal was in the corpus for two synthesis passes
without a carrier, and that is a drift class no retained checker can reach — because a claim about a
**model** has no verdict sentence to attach to, so a synthesis keyed on verdicts never picks it up
until a later claim converts it into one.

### The standing failure modes this pass must assume, from `CLAUDE.md`

- Round a claim's number only where the precision is not the content, and **the trap runs both ways**.
- **Quote a ratio's construction beside it**, so a moved argument is visible at the point of use.
- Check whether any number quoted has been **superseded by a later claim** — the class no tracer reaches,
  because the owner still states it.
- A **self-describing count** (how many claims, challenges, checkers) is the one number a tracer cannot
  own; derive it, do not inherit it. Two gates print two of the three every run.
- A **question put to the customer can carry a stale price**, and a **census is dated by its own premise set**.
- **Strike, never delete.**

---

## 2. Plan

### Order, and why this order

**The cheap bound runs first, and here it is two things, neither of which needs a read of either
document**: the retained checkers, and one `grep` for `C-0154`.

1. **Enumerate the tool set at the moment of running**, rather than inheriting the previous pass's
   list of seven — `CH-0182`'s *a census is dated by its premise set*, applied to the instrument.
   Run every one of them on **both** documents and record the reading **before** any edit.
2. **`grep` for `C-0154`, `24/7`, `dimer`, `acrossHelixRigidity` and `0.0449400126`** in both
   documents. One command; it either finds the mechanism or establishes `P0`.
3. **Derive** the three self-describing counts — claim files, challenge files, retained checkers —
   and, for the third, derive its three *predicates* separately (how many exist, how many are wired
   into `tools/verify.sh`, how many read the deliverables), because a count is only as good as the
   sentence it sits in.
4. **Enumerate the carriers** of every coupled-cell verdict in both documents by grepping the
   superseded numbers out of the claims that own them, not by reading for them.
5. Edit: strike in place where the sentence is a headline verdict, and add one dated restatement
   block per section rather than rewriting the history.
6. Re-run every checker, both documents, and record the after reading.

### Method justification against cost

The whole pass is `grep`, `python3 tools/*.py` and text editing. No study is run, no result file is
moved, nothing is compiled. The expensive alternative — re-running the studies whose result files the
deliverables quote — is refused on `C-0110`'s own ground: **`C-0167` has already re-run them**, and
this task's job is to carry what it found, not to re-measure it.

### What would falsify this approach

- **`F1`** — a checker that was clean before the pass is red after it, and the cause is this pass's
  own edit rather than a defect it exposed. (`C-0165` records the precedent: the edit whose whole
  purpose was to stop a number being asserted is the edit that broke a table.)
- **`F2`** — a number written into either deliverable that `grep` cannot find in the claim cited
  beside it, at the precision written.
- **`F3`** — `P0` fails, i.e. `C-0154` **is** already carried, in which case this pass's headline
  drift class does not exist and the finding is a value substitution after all.
- **`F4`** — the reversal turns out to be expressible as a multiplier, in which case striking each
  number individually is the wrong repair and a single scaling note is the right one.
- **`F5`** — the uncoupled flatness story does **not** survive, in which case this is not a synthesis
  task at all but a re-opened design question and must be requeued as one.

### Acceptance predicates

| | predicate |
|---|---|
| **`P1`** | every retained checker, enumerated at the moment of running, is clean on **both** documents after the pass, and its before reading is recorded |
| **`P2`** | every passage in either document stating a coupled-cell flatness verdict carries `C-0167`'s re-graded reading beside it, struck rather than deleted |
| **`P3`** | the three statements that **survive** — the uncoupled block's flatness, `C-0151`'s raster selection, and `C-0168`'s seam resolution — are stated as surviving, with the ground they now stand on |
| **`P4`** | every self-describing count in either document is **derived** in this pass and corrected by striking if it moved, including the checker census's three separate predicates |
| **`P5`** | `C-0169`'s conversion of `T-9`'s second deliverable is carried into both documents' *"what we cannot answer"* passages, distinguishing **bounded** from **measured** |
| **`P6`** | no number is changed except beside its struck predecessor, and every number quoted is `grep`ed out of the claim that owns it |
