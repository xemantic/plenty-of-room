# C-0130 — **§3 row (g) is 9.3× shorter and nothing was discarded, which is what makes it a rewrite rather than a deletion.** `C-0121` named it the passage most in need of one: **23 134 characters in a single table cell**, eleven revision markers, and a standing verdict that had **reversed twice**, so a reader wanting the current answer had to reconstruct it from a chronological accretion. It is now **2 489**, stating the answer in five steps, with the cell as it stood quoted **verbatim below the table** — because the reversals are the most instructive material in this deliverable and removing them would leave the answer looking inevitable. Cheap bound **0 of 11 cited**, the strongest product signal any pass has had. **6 carried in, 5 deliberately not** — and one of the five is not carried *by its own measurement*

| | |
|---|---|
| **Task** | [`T-211`](../tasks/T-211-seventh-answers-synthesis.md) — the seventh synthesis of `ANSWERS.md` |
| **Leaf** | — (a process claim; it audits the deliverable that reports every leaf) |
| **Verification type** | **logical** — a coverage partition and a rewrite, with the **five** retained checkers run before and after |
| **Verdict** | **PASS on all four predicates; the declared falsifier did not fire** — nothing was lost, and that is checked rather than asserted: the pre-rewrite cell is quoted verbatim and the tracer reports **0 ABSENT** before and after. **6 CARRIED IN, 5 NOT CARRIED.** |
| **Maturity** | **Below TRL 1–3: nothing here is physics.** No number is re-derived; every number moved is another claim's, at that claim's precision. |
| **Provenance** | [`gpd/results/T-211-seventh-answers-synthesis.json`](../results/T-211-seventh-answers-synthesis.json), emitted by the retained [`tools/T-211-emit-result.py`](../../tools/T-211-emit-result.py), whose cheap bound **and row-(g) character counts** are both derived from `git show 29b0153:ANSWERS.md` rather than typed. |
| **Conditions** | The corpus at iteration 29, base commit `29b0153`. |
| **Consumes** | [`C-0121`](C-0121-sixth-answers-synthesis.md) (which named the rewrite), and every item in `C-0122`–`C-0128` / `CH-0147`–`CH-0151` |
| **Constrains** | `ANSWERS.md` only. **No claim, number or verdict is contradicted, and none is re-derived.** No challenge is raised. |

---

## 1. The rewrite, which is the point of the pass

| | |
|---|---|
| row (g) before | **23 134** characters, one table cell, **11** revision markers |
| row (g) after | **2 489** characters — **9.3×** shorter |
| history | **preserved verbatim**, quoted below the table, unedited |

**A deliverable whose answer has reversed twice owes its reader two different things in two different
places.** The current answer belongs at the top, stated plainly. The reversals belong somewhere they can be
read as a sequence — and they are the most instructive material in this document, because the flatness
negative stood for eight iterations, was closed on four separate axes, and was then overturned by a claim
about the **body** rather than the coupling.

**So nothing was deleted.** The falsifier declared for this task was precisely that a rewrite might lose a
withdrawn reading or a number a later claim depends on; the pre-rewrite cell is quoted in full, and the
numeric tracer reads **0 ABSENT** before and after.

The rewritten row states five things: the tile is four honeycomb layers; on it the free tile is flat with
**no coupling at all**; a **coupled** tile is flat under the measured folding statistics; the stations exist
and the placement survives them; and the source's own folding measurements recommend a better cross-section
still, at two-thirds the footprint, which is decision 7 with NDI.

---

## 2. The partition

| | count |
|---|---|
| **CARRIED IN** | **6** — `C-0122`, `C-0125`, `C-0128`, `CH-0147`, `CH-0148`, `CH-0151` |
| **DELIBERATELY NOT CARRIED** | **5** — `C-0124`, `C-0126`, `C-0127`, `CH-0149`, `CH-0150` |

Cheap bound: **0 of 11** cited by ID before the pass, 9 after. That is the strongest product signal any pass
has had — `C-0106`'s was 14 of 48 and `C-0115`'s 3 of 20 — and it still under-reports, as every pass records.

**Three of the five not-carried are process claims and the reasons differ, which is why they are recorded
separately rather than as a class:**

- **`C-0124`** corrected a *different document*, `DECISIONS-FOR-NDI.md`. Its finding — that a decisions
  file's drift is **one-signed by construction**, because such a document lists what a programme cannot do —
  is about a document *class* and belongs in `CLAUDE.md`, where it is.
- **`C-0126`** reclassified the **queue**. This deliverable reports answers, not the queue's state; and the
  ruling those eight tasks are contingent on **is** carried, as decision 7.
- **`C-0127`'s own measurement is that not one claim had inherited a defective number.** So by its own
  finding this deliverable owes nothing — and **recording that is the point**. An absence with a measured
  reason is not the same as an omission, and the difference is invisible unless somebody writes it down.

---

## 3. A discharge is an edit, not a deletion

§5's *"a census of the honeycomb's attachment lattice"* was raised at iteration 25 as **the largest gap in the
four-layer line**, and discharged across iterations 26–28 by `C-0122` (the census), `CH-0151` (which corrects
it **upward**, 90/60 → 132/90, an oblique helix having two free azimuths and not one) and `C-0128` (which
prices an oblique root at **nothing** for a flexible tie and **6.01719478×** for a crossover-hinged body).

**Struck rather than deleted, with the original entry kept below it**, because the entry is the reasoning the
discharge answers — the same discipline the row-(g) rewrite follows, at the scale of a bullet.

---

## 4. The checkers, before and after

Five now, `P-26`'s challenge index having joined in iteration 28.

| | before | after |
|---|---|---|
| numeric trace | 0 ABSENT | 0 ABSENT |
| open assertions contradicted by `TASKS.md` | 0 | 0 |
| challenge statuses contradicted | 0 | 0 |
| self-contradictions | 0 | 0 |
| Markdown table defects | 0 | 0 |
| broken corpus links | 0 | 0 |
| unindexed challenges | 0 | 0 |

**No checker fired during this pass**, which is worth stating because the last two passes both introduced a
defect only one instrument could see — `C-0115` a verdict-window misattribution, `C-0121` a table cell-count
error. A rewrite of a 23 000-character cell was the likeliest place yet for the table checker to fire, and it
did not.

---

## 5. Validity range

- **It re-derives nothing.** Every number is another claim's, at that claim's own precision.
- **The `(σ, L₀)` window is un-re-run for the seventh consecutive pass**, and again for the same reason: not
  one item in range is a function of `σ`.
- **The rewritten row states the CURRENT answer.** A reader who wants to know how the programme got there, or
  which readings were withdrawn, must read the preserved history — and the row says so explicitly rather than
  leaving it to be discovered.
