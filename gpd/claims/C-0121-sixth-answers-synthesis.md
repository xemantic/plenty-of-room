# C-0121 — **The first synthesis pass in six whose range is mostly FAVOURABLE movement rather than correction, and the only defect it introduced was one that only the table checker could see.** Of six items, **3 carried in, 2 already reflected, 1 deliberately not** — and the three carried are the three that most change what the programme recommends: the tile is a **published, gel-verified** cross-section, its own source recommends a **6.6× flatter** one with **no coupling-fraction threshold at all**, and on that one **a coupled tile is flat at the 90th percentile under the measured folding statistics for the first time**. §5 gains an entry that is neither a measurement nor a solve: **nobody has counted the honeycomb's attachment lattice**, so every path count in that flat coupled result is a **request** rather than a demonstration that the stations exist

| | |
|---|---|
| **Task** | [`T-202`](../tasks/T-202-sixth-answers-synthesis.md) — the sixth synthesis of `ANSWERS.md`, against `C-0115`–`C-0120` |
| **Leaf** | — (a process claim; it audits the deliverable that reports every leaf) |
| **Verification type** | **logical** — a coverage partition and a statement-by-statement adjudication, with all four retained checkers run before and after |
| **Verdict** | **PASS on all four predicates; the declared falsifier did NOT fire** — every checker was clean before the pass, so this is a synthesis and not a repair. **3 CARRIED IN, 2 REFLECTED, 1 NOT CARRIED.** Cheap bound: **2 of 6** cited before, 6 after. |
| **Maturity** | **Below TRL 1–3: nothing here is physics.** No number is re-derived; every number moved is another claim's, at that claim's own precision. |
| **Provenance** | [`gpd/results/T-202-sixth-answers-synthesis.json`](../results/T-202-sixth-answers-synthesis.json), emitted by the retained [`tools/T-202-emit-result.py`](../../tools/T-202-emit-result.py), whose cheap bound is derived from `git show 90ea3f3:ANSWERS.md` rather than typed. Edits to [`ANSWERS.md`](../../ANSWERS.md) §1 (two passages and the coverage statement), §2 row 5b, §3 row (g), §5 (a new missing-measurement entry and decisions row 6). |
| **Conditions** | The corpus at iteration 26, base commit `90ea3f3`. |
| **Consumes** | [`C-0115`](C-0115-fifth-answers-synthesis.md) (the method and the previous coverage statement), [`C-0116`](C-0116-composite-fraction-threshold.md), [`C-0118`](C-0118-coupled-four-layer.md), [`C-0119`](C-0119-honeycomb-raster-width.md), [`C-0120`](C-0120-cross-section-comparison.md), and [`C-0117`](C-0117-reemission-order.md), which is deliberately not carried |
| **Constrains** | `ANSWERS.md` only. **No claim, number or verdict anywhere in the corpus is contradicted, and none is re-derived.** No challenge is raised. |

---

## 1. The partition

| | count |
|---|---|
| **CARRIED IN** | **3** — `C-0118`, `C-0119`, `C-0120` |
| **REFLECTED** already | 2 — `C-0115` (the pass being extended), `C-0116` (carried by `T-196`'s own discharge) |
| **DELIBERATELY NOT CARRIED** | 1 — `C-0117` |

**`C-0117` is not carried, and the reason is the interesting one.** It amends `C-0092`'s `A5`
margin-movement range from *"1.0000–3.3380×"* to **1.0000 everywhere**. This deliverable **never quoted that
range** — grepped, zero occurrences — so carrying the correction would mean *introducing* the number in order
to correct it. The amendment lives in `C-0092` and `CH-0131` keeps the contradiction. That is the same
judgement `C-0115` made about `CH-0131` one pass earlier, now that its owning claim exists.

Cheap bound: **2 of 6** items cited by ID before the pass, 6 after.

---

## 2. What the range is, and why it is different from the five before it

Every previous pass carried corrections, scope failures or withdrawn recommendations. `C-0115`'s two largest
items were *scope* corrections — passages that existed and were correct and were about the wrong object.

**This range is mostly favourable movement**, and saying so is part of the result, because five consecutive
passes of correction is not the base rate and a reader is entitled to know which kind of pass this is:

| | what it moved |
|---|---|
| `C-0116` | the threshold §3 row (g) had just been told the four-layer verdict was **conditional** on is **located**, at `f = 0.0788618807`, cleared by 3.29690337× |
| `C-0119` | the tile is **design (i) of the caDNAno paper** — folded, gel-analysed, one of three of seven to give sharp monomer bands — the honeycomb scaffold lattice is **integral**, and a seam is **forced** |
| `C-0120` | the source's own recommendation, `10 × 6`, is **6.6× flatter with no threshold at all** — flat even at `f = 0` — at **two-thirds** the footprint |
| `C-0118` | **9 of 16** coupled cells flat at the 90th percentile under measured folding, all eight on `10 × 6`; the **cross-section** worth 3.17109774× and the distribution carrying **no consistent sign** |

---

## 3. The new entry in §5, which is neither a measurement nor a solve

`ANSWERS.md`'s *"what we cannot answer"* list gains **a census**:

> Every plan ceiling, station lattice, crossover phase and placement in this corpus is **single-layer
> square-lattice**. The honeycomb has **three** crossover azimuths at 7 bp rather than the square lattice's
> four at 8 bp, and **nobody has counted what that offers.**

So every path count in `C-0118`'s flat coupled cells — including the ten-path winner — is a **request** rather
than a demonstration that the stations exist. It is listed because a reader would otherwise take the coupled
flatness result as **buildable**, which it is not yet shown to be. It is a lattice derivation and it is cheap;
what it is not is done.

---

## 4. The one defect this pass introduced, and only one checker could see it

Appending to §2 row 5b put the new text **inside the owner cell** instead of at the end of the verdict cell —
a Markdown row with **6 cells against 5**, which would have widened the whole table and made every correct row
the odd one out.

- The numeric trace: **clean** (every number was owned).
- The status check: **clean** (no task's status moved).
- The self-consistency check: **clean** (the document did not disagree with itself).
- `check-markdown-tables.py`: **fired immediately**, with the row and the cell counts.

Repaired by restoring the row from git and re-appending **by cell** rather than by string. **Three of the four
retained checkers were clean throughout and the fourth earned its place** — which is the argument for keeping
a checker whose failure mode no other instrument shares.

---

## 5. Validity range, and what this does NOT do

- **It re-derives nothing.** Every number is another claim's, at that claim's precision.
- **The `(σ, L₀)` window is un-re-run for the sixth consecutive pass**, and for the same reason: not one item
  in range is a function of `σ`.
- **§3 row (g) is now very long** and carries the entire history of the flatness question — its reversals, its
  withdrawn readings and its four separate re-openings. That is deliberate, because the value of the row is
  that the history is visible in one place; but it is the passage most in need of a **rewrite** rather than
  another append, and a seventh pass should consider that rather than extending it again.
- **`C-0117` is absent by decision, not by omission**, and the reason is recorded per row in the result file.
