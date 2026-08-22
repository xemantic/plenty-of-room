# T-288 — carry `C-0180`'s tied re-grade into the two customer-facing documents

**Leaf:** `A8.2` · **Raised by:** [`C-0180`](../claims/C-0180-tied-honeycomb-coupled-regrade.md) (`T-279`) and
[`CH-0234`](../challenges/CH-0234-no-verdict-reverses-was-a-free-tile-statement.md), iteration 43.

## Formulate

[`C-0167`](../claims/C-0167-coupled-cells-on-the-honeycomb-grillage.md)'s *"`0` of `64`"* is **`2` of `64`** on the
lattice that carries the raster's own 59 covalent turn ties, and both deliverables assert the superseded count.

This is `C-0080`'s **superseded-standing-value** drift class in its purest form: the number *is* in a claim, so
[`tools/trace-answers.py`](../../tools/trace-answers.py) reads every occurrence `CITED`; the passages carry no
status word `queue_status` can read and no broken link. **No retained checker can find it. A reader must.**

**Units are locked and nothing here is computed**: every quantity is read out of `C-0180` or out of
[`gpd/results/T-279-tied-honeycomb-regrade.json`](../results/T-279-tied-honeycomb-regrade.json), and the claim
records which. Dishing is a dimensionless fraction of the free stroke; `T-5b`'s convention is 0.10 at the 90th
percentile of the dropout ensemble.

### Acceptance predicates

| | |
|---|---|
| **`P1`** | Every occurrence of the `0 of 64` family in [`ANSWERS.md`](../../ANSWERS.md) and [`DECISIONS-FOR-NDI.md`](../../DECISIONS-FOR-NDI.md) is annotated, none deleted and none overwritten — `C-0071`'s *strike, never delete* |
| **`P2`** | A sentence the tied re-grade **falsifies** is **struck**; a sentence it **supersedes without disputing** is annotated in place. The two are distinguished explicitly, because `C-0167`'s count is right on the object it was taken on |
| **`P3`** | The four qualifications `C-0180` publishes beside its headline — the **margin**, the **ceiling-not-multiplier** reading, the **tail**, and the **prestrain sign** — are carried into both documents, so `2 of 64` cannot be read as settled |
| **`P4`** | Every retained checker clean **after**, with its **before** reading recorded, the deliverables' *before* taken from `git show HEAD:<path>` rather than from a shared working tree |
| **`P5`** | The [`T-276`](T-276-twelfth-answers-synthesis.md) row names what this pass did and did **not** do, so the residue is named rather than estimated |

### Falsifiers

- `F1` — a gate clean before and red after **because of this pass's edit**. **FIRED THREE TIMES**, all three this
  pass's own drafting, all three repaired; recorded in `C-0186` §5. The third is the interesting one: a paragraph
  written to say *"the census's tokens were avoided"* **spelled two of them out** and the census counted them.
- `F2` — a number that no claim owns. **FIRED**, on three readings that live only in `T-279`'s result file;
  the repair is that `C-0186` states them and says it owns them (`C-0186` §3).
- `F3` — the tied re-grade moves a number in one deliverable and not in its twin. Did **not** fire: the census in
  `C-0186` §1 is taken over both documents in one pass.
- `F4` — this iteration's other claims (`C-0178`, `C-0179`, `C-0181`–`C-0184`, `CH-0224`) supersede something in
  either deliverable. Did **not** fire: 0 hits over 19 search keys.
- `F5` — an annotation **in a deliverable** adds a `T-234` census occurrence and the classification table goes
  stale. Did **not** fire: 386 occurrences in 41 files before and after, `GATE 0`, debt `24 of 88` unchanged —
  and the same falsifier taken over the whole corpus **did** fire, on this task's own claim file, above.

## Plan

**The cheap bound first, and it is a `grep`.** Counting the occurrences before writing anything says how large the
pass is and whether it is a pass at all: 5 in `ANSWERS.md` and 3 in `DECISIONS-FOR-NDI.md`, which is an
afternoon's careful reading and not a synthesis. The expensive alternative — the full thirteenth synthesis —
is **declined here on cost** and left in `T-276`: it is owed at the end of an iteration over a **settled**
corpus, and this iteration's corpus was still moving under two agents while this ran.

**The second cheap bound is the census's own token set.** `tools/T-234-census.py` keys its classification on
occurrence **index**, so any new annotation containing one of its tokens shifts every index below it and takes the
gate red — and `C-0176` §1b forbids regenerating the table mid-flight. Reading the token set out of the tool
**before** drafting costs one `sed` and makes `F5` unfireable by construction.

**What would falsify the approach**: if annotating in place turned out to require restating a passage rather than
qualifying it — i.e. if `C-0167`'s count were *wrong* rather than *superseded* — then the whole pass would be a
rewrite and would belong in `T-276` with the rest. `C-0180` settles that in its own Constrains row: the count is
**superseded and not disputed**.

## Outcome

**DONE** (iteration 43) — [`C-0186`](../claims/C-0186-carrying-the-tied-regrade.md). 8 of 8 count occurrences
annotated, 3 absolutes struck, 20 annotations over 19 passages in the two documents, one `15 × 4` comparison
passage deliberately left, every gate clean, and `CH-0240` reserved and **released unused**.
