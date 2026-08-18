# C-0126 — **A claim can supersede a TASK, and six claims that did it in a row none of them noticed.** Of **59** open queue items, **8** optimise a coupling placement, phase or distribution on the **single-layer** tile — and the four-layer tile is flat with **no coupling at all**. They are marked **CONTINGENT**, not killed: §3's thickness row cannot hold both ways, NDI's answer resolves it **by implication** rather than by ruling, and decision 7 is unanswered, so they are **well posed only on one side of a live specification question**. **And two are STRENGTHENED** — `T-9`, the crossover hinge constant from oxDNA, is now the input the four-layer rigidity *and* `C-0116`'s threshold both rest on. A sweep that only looks for work to cancel would have missed it

| | |
|---|---|
| **Task** | [`T-205`](../tasks/T-205-four-layer-supersession.md) — what did the four-layer line supersede in the queue? |
| **Leaf** | — (a process claim) |
| **Verification type** | **logical** — a classification, with the open set derived from `TASKS.md` by the deliverable checker's own `queue_status` |
| **Verdict** | **PASS on all four predicates; the falsifier did not fire.** 8 CONTINGENT, 2 STRENGTHENED, 49 UNAFFECTED of 59 open. All ten are marked **in the queue itself**, because a classification that lives only in a claim is one an agent picking up a task will never see. |
| **Maturity** | **Below TRL 1–3: nothing here is physics.** No number is derived; the classification is a recorded judgement, per item, with its reason. |
| **Provenance** | [`gpd/results/T-205-four-layer-supersession.json`](../results/T-205-four-layer-supersession.json), emitted by the retained [`tools/T-205-emit-result.py`](../../tools/T-205-emit-result.py). |
| **Conditions** | The corpus and queue at iteration 27, after `C-0123`. |
| **Consumes** | [`C-0109`](C-0109-four-layer-tile.md), [`C-0116`](C-0116-composite-fraction-threshold.md), [`C-0118`](C-0118-coupled-four-layer.md), [`C-0119`](C-0119-honeycomb-raster-width.md), [`C-0120`](C-0120-cross-section-comparison.md), [`C-0122`](C-0122-honeycomb-station-lattice.md), [`C-0123`](C-0123-collar-aspect-ratio.md), and [`C-0071`](C-0071-output-element-recommendation.md) for the discharge discipline this extends |
| **Constrains** | `TASKS.md` only. **No claim, number or verdict is contradicted, and none is examined.** No challenge is raised. |

---

## 1. The finding, which is about the corpus rather than the device

`CLAUDE.md` records that **a discharge is invisible to whoever files the removal** — a claim that kills a
branch is not looking at the questions that branch raised, and `C-0071` had to invent the status `DISCHARGED`
because the queue had no word for *"stopped applying"*.

**This is the same failure one level up: a claim that changes the BODY is not looking at the tasks that
optimise a coupling on the old one.** Six claims moved the tile between iterations 23 and 26 and **none of the
six noticed** that eight open tasks were now conditional on a specification they had just reopened.

| | of 59 open |
|---|---|
| **CONTINGENT** on the tile-thickness ruling | **8** |
| **STRENGTHENED** by the four-layer line | **2** |
| unaffected | 49 |

---

## 2. The eight, and what they have in common

`T-142`, `T-143`, `T-174`, `T-176`, `T-177`, `T-179`, `T-180`, `T-185`.

Every one optimises a **coupling** — its placement, its crossover phase, its per-path distribution, or its
row-end prestrain — **on the 2 nm single-layer sheet**. The four-layer tile is flat at **0.0577199433** of the
stroke with no coupling at all, and `10 × 6` at **0.00874363524** with no coupling-fraction threshold either.
So on the thicker tile the quantity these tasks optimise is **not the binding one**.

**They are contingent and not dead**, and the distinction is the whole claim:

- §3's parameter row says *"Tile thickness ~10 nm (single-layer honeycomb)"*, which **cannot hold both ways**.
- NDI's answer to decision 5 resolves it toward the thick reading **by implication** — *"just make the tile
  thicker"* — and `T-166` is recorded as **ANSWERED BY IMPLICATION**, not ruled.
- **Decision 7 is with NDI and unanswered.**

**If the tile stays single-layer, all eight are live exactly as written.** Nothing is struck — `CLAUDE.md`:
*a list that only ever grows is not a record and a list that silently shrinks is worse.*

---

## 3. And two got MORE important, which a cancellation sweep would have missed

- **`T-9` — the crossover hinge constant `k_θ` from oxDNA.** A multi-layer tile's rigidity is a
  **parallel-axis enhancement over the same crossover springs**, and `C-0116` shows the flatness verdict turns
  on the interlayer coupling fraction. So `k_θ` and the crossover's vertical compliance now carry **more** of
  the answer than they did on the single-layer sheet, not less. It was already the queue's most expensive
  named item; it is now also among the most load-bearing.
- **`T-189` — can the 112 bp raster row be twist-corrected?** Queued against the row-end prestrain on a
  single-layer sheet; the four-layer tile is built from the same rows, and `C-0119` supplies the honeycomb's
  own 21 bp register — so the question survives the tile change and acquires a second lattice.

**A supersession sweep that only looks for work to cancel is not a sweep**, and the two items it would have
missed are the ones a successor should read first.

---

## 4. Validity range

- **The classification is a judgement**, recorded per item with its reason, so a reader can disagree with any
  single row without discarding the sweep.
- **CONTINGENT is not KILLED**, and the condition is named on every marked row in the queue.
- **The sweep classifies against the four-layer line only.** An item unaffected by the tile may still be
  superseded by something else; this does not look, and a general supersession audit is a different task.
- **The denominator is derived, not typed**: the open set comes from `TASKS.md` through the same
  `queue_status` the deliverable's own checker uses, so it cannot drift from the register.
