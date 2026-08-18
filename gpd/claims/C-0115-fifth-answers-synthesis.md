# C-0115 — **Four passes have reported *"not one of these is a function of `σ`"* beside an unmoved window, and this is the pass where that census is the WRONG SUMMARY.** Two of the twenty items in range are not values at all but **scope corrections to what the deliverable's answers were about**: §4(g)'s flatness closure was a statement about a **2 nm** tile where §3 specifies a ~10 nm one, and *"the only route to the desired stroke"* was a statement about a **displacement** rather than a force. **14 of 20 items were carried in, 4 were already reflected, 2 deliberately not** — and the cheap bound said so in seconds, at **3 of 20 cited**. All four retained checks were **clean before the pass and clean after**, which is the point; both firings **during** it were caused by this pass's own edits and both were real, so **the checker caught its own author for the third consecutive iteration**

| | |
|---|---|
| **Task** | [`T-201`](../tasks/T-201-fifth-answers-synthesis.md) — the fifth synthesis of `ANSWERS.md`, against `C-0106`–`C-0114` and `CH-0121`–`CH-0131` |
| **Leaf** | — (a process claim; it audits the deliverable that reports every leaf) |
| **Verification type** | **logical** — a coverage partition and a statement-by-statement adjudication, with [`tools/trace-answers.py`](../../tools/trace-answers.py), [`tools/check-markdown-tables.py`](../../tools/check-markdown-tables.py) and [`tools/result-reader-census.py`](../../tools/result-reader-census.py) run before and after |
| **Verdict** | **PASS on all five predicates, and the declared falsifier did NOT fire** — all four checks were clean before the pass began, so this is a synthesis and not a repair. **14 CARRIED IN, 4 REFLECTED, 2 NOT CARRIED.** The two largest items are **scope** corrections rather than numeric ones and neither is reachable by any check in the tree. **And for the second time in the programme the deliverable was found asserting a question OPEN that the corpus had answered** — the electrode PZC, answered from published measurement one iteration earlier — which is `C-0067`'s *"a deliverable that under-claims is as wrong as one that over-claims, and is far harder to catch"*. |
| **Maturity** | **Below TRL 1–3: nothing here is physics.** No number is re-derived and no solver runs; every number moved is another claim's, quoted at the precision that claim states it. |
| **Provenance** | [`gpd/results/T-201-fifth-answers-synthesis.json`](../results/T-201-fifth-answers-synthesis.json), emitted by the retained [`tools/T-201-emit-result.py`](../../tools/T-201-emit-result.py), whose cheap bound is re-derived from `git show 9ed4fdc:ANSWERS.md` rather than typed. Edits to [`ANSWERS.md`](../../ANSWERS.md) §1 (three passages), §2 (row 5b, Task 2), §3 (row (g)), §5 (two *"cannot answer"* bullets, the tile-thickness caveat, decisions rows 2/4/6). |
| **Conditions** | The corpus at iteration 24, base commit `9ed4fdc`. `T-196` and `T-200` were commissioned in the same iteration and neither had filed a claim when this was written; both are named where the deliverable depends on them. |
| **Consumes** | [`C-0106`](C-0106-fourth-answers-synthesis.md) (the method, the template and the previous coverage statement), [`C-0080`](C-0080-third-answers-synthesis.md) (the third drift class and the false-positive doctrine), [`C-0067`](C-0067-answers-reconciliation.md) (under-claiming), [`C-0088`](C-0088-does-the-deliverable-agree-with-itself.md) and [`C-0113`](C-0113-challenge-status-self-consistency.md) (the checks), and every claim and challenge in `C-0106`–`C-0114` / `CH-0121`–`CH-0131` |
| **Constrains** | `ANSWERS.md` only. **No claim, number or verdict anywhere in the corpus is contradicted, and none is re-derived.** No challenge is raised: every contradiction this pass carries was already raised by the claim that found it. |

---

## 1. The cheap bound, and what it was worth

One `grep` per ID over the deliverable, before anything was opened, derived from git so it cannot drift:

| | of 20 items in range |
|---|---|
| cited by ID in `ANSWERS.md` **before** the pass | **3** (`C-0108`, `CH-0121`, `CH-0123`) |
| uncited | **17** |
| cited after | 19 |

**Seventeen of twenty uncited** — a stronger signal than any previous pass (`C-0106`'s was 34 of 48) — and it
said in seconds that this pass had a product rather than a re-read.

It **under-reports**, as `C-0106` records, and here it under-reported in the *other* direction too:
`C-0106` itself is uncited because a synthesis claim describes the file rather than being quoted in it.

---

## 2. The partition

Full rows with reasons are in the result file.

| | count |
|---|---|
| **CARRIED IN** | **14** |
| **REFLECTED** already | 4 |
| **DELIBERATELY NOT CARRIED** | 2 |

The two not carried, with their reasons:

- **`C-0113`** — a process claim about a document checker. `ANSWERS.md` reports physics, and no answer of §6
  depends on it. Its *output* is carried: the four checker lines are quoted in §4 below.
- **`CH-0131`** — a result-file ordering defect inside `C-0101`'s re-emission sweep. It moves no answer of §6
  and no number this file quotes, and **`T-200` owns the amendment to `C-0092`**. Until that claim exists the
  deliverable would be quoting a correction with no owner, which is the drift class this pass is against.

---

## 3. Why the census is the wrong summary this time

Four passes have now reported the same census beside an unmoved window: *not one of the items in range is a
function of `σ`*. It is true again.

**And it is the wrong thing to report**, because two of this range's items are not values at all:

| the deliverable said | it was a statement about | it should have been about |
|---|---|---|
| *"the question is closed on every coupling axis this programme can reach"* (§4(g)) | a **2 nm** single-layer tile | §3's own *"~10 nm"* tile, on which the free tile is flat with **no coupling at all** (`C-0109`) |
| *"only `T-115`, a taller layer, can buy the desired stroke"* (§1) | a **displacement** the layer admits | a **force** the field must deliver, which stops arriving at 13.6989179 nm (`C-0110`, `CH-0127`) |

Neither is reachable by a numeric trace (both passages' numbers were owned and correct), by a status check
(neither task was asserted open), or by a self-consistency check (the file agreed with itself throughout).
That is `C-0106`'s finding — **a determination with no passage is invisible to every check in the tree** —
in its sharpest form yet: here the passages *existed* and were *correct*, and what was wrong was **what they
were about**.

---

## 4. The checkers, before and after, and the two firings in between

| | before | after |
|---|---|---|
| numeric trace | 1050 tokens, 937 CITED, 113 ELSEWHERE, **0 ABSENT** | 1216 tokens, 1103 CITED, 113 ELSEWHERE, **0 ABSENT** |
| open assertions contradicted by `TASKS.md` | 0 | 0 |
| challenge statuses contradicted | 0 of 119 files, 86 declared | 0 of 119 files, 86 declared |
| self-contradictions | 0 | 0 |
| Markdown table defects | 0 in 357 files | 0 in 357 files |

**Both firings during the pass were caused by this pass's own edits, and both were real.**

1. **3 ABSENT** — `0.0344`, `0.0577` and `0.0910196802`. All three were the synthesis **truncating** a number
   its owning claim states at full width (`0.0344013403`, `0.0577199433`, `0.0910197`). Repaired by restoring
   the owner's own rendering, which is the standing rule: *round a claim's number only where the precision is
   not the content*. Note the third ran the other way — the synthesis wrote **more** digits than `C-0112`
   states, which the tracer flags just as loudly, and correctly.
2. **1 STALE-OPEN and 1 SELF-CONTRADICTION, both on `T-193`** — the new PZC passage read *"ANSWERED … and
   what is open is a different question"*, which put the word *open* inside the verdict window of an
   identifier the queue records as `CLOSED`. **The sentence was wrong as written**: the residue is not
   `T-193` at all. Rephrased rather than suppressed.

**So a retained check has now caught the mistake of the person using it for the third consecutive
iteration** — and this time the check in question (`C-0113`, the challenge-status and verdict-window work)
had shipped exactly one iteration earlier, written by the same author it caught.

---

## 5. What the pass carried, section by section

| section | what moved |
|---|---|
| §1, the flat-tile closure | the four-layer scope correction: 0.0577199433 uncoupled against 0.307902368, one M13 paying for exactly four layers (6 720 of 7 249 nt), the surviving 1.16× as a statement about the *coupling* |
| §1, the NDI-questions paragraph | **both** load-bearing answers withdrawn by measurement — the tall layer buys neither clause, the two-layer permission removes the need for the coupling — plus the one reserve and its single remaining claimant |
| §1, the coverage statement | extended to `C-0081`–`C-0114` / `CH-0093`–`CH-0131`, with the census reported *and* named as the wrong summary |
| §2 row 5b | reopened, and answered from the other side than the row expected |
| §2 Task 2 | NDI's Debye objection upheld; the reach threshold, the 0/12 and 96/96 counts, `CH-0126`'s effort-point ceiling, and the concession that our own rebuttal was about ion content and never decay |
| §3 row (g) | the four-layer scope correction, its three stated qualifications, `CH-0124`/`CH-0125`, and **the whole prestrain branch** (`C-0107`, `C-0112`, `CH-0122`, `CH-0129`, `CH-0130`) which the file carried nowhere |
| §5 *"cannot answer"* | the PZC bullet rewritten from `STILL OPEN` to answered-with-a-different-residue; the row-end-prestrain bullet given its derived value and its narrowed remainder |
| §5 tile-thickness caveat | the prediction it recorded is now confirmed by measurement |
| §5 decisions rows 2, 4, 6 | rows 2 and 4 struck where measurement withdrew them; row 6 settled by `T-191` |

---

## 6. Validity range, and what this pass does NOT do

- **It re-derives nothing.** Every number is another claim's, at that claim's own precision.
- **It leaves the `(σ, L₀)` window un-re-run**, for the fifth consecutive pass and for the same reason: not
  one item in range is a function of `σ`. That remains a *reading* rather than a re-intersection.
- **Two passages are owed a re-read the moment a sibling claim lands.** §4(g) and §2 row 5b both state
  `C-0109`'s four-layer verdict as conditional on an interlayer coupling fraction whose flatness crossing
  `T-196` is locating; the conditional statement is true whatever it returns, but the *margin* is not yet
  known. This is recorded in the deliverable itself, not only here.
- **`CH-0131` is deliberately absent** and will be owed as soon as `T-200` files.
- **The third drift class is still unmechanised.** A superseded standing value whose owner still states it
  reads `CITED`, and `C-0067` records why no corpus comparison can see it. This pass found two such items by
  reading, which is the only instrument there is.
