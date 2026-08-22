# C-0186 — **A SUPERSEDED COUNT READS `CITED` TO EVERY CHECKER, AND BOTH DELIVERABLES CARRIED `C-0167`'s *"`0` of `64`"* HOURS AFTER [`C-0180`](C-0180-tied-honeycomb-coupled-regrade.md) MADE IT `2` OF `64`.** Eight occurrences of the count family — **five** in [`ANSWERS.md`](../../ANSWERS.md), **three** in [`DECISIONS-FOR-NDI.md`](../../DECISIONS-FOR-NDI.md) — and **all eight are annotated here**, together with **three** absolute *"no coupled cell … is flat"* sentences that the tied re-grade makes **false** rather than merely superseded, and which are therefore **struck**. Not one retained checker could have found this: the number **is** in a claim, so `tools/trace-answers.py` reads it `CITED`, and the passages carry no status word for `queue_status` and no broken link. **The pass's own finding is that the count is the least of what a synthesis owes here**: `2 of 64` is a *thin* result, and three qualifications travel with it or it will be read as a win — the recovery clears `T-5b` by **0.426 %**; the free tile's `1.12×` is a **CEILING the coupled cells never reach** rather than a multiplier, over-stating the benefit at **every one of the 64 cells**; and the ensemble's **tail runs the other way**, the 90th percentile of the paired ratio sitting above one at **27 of 64** cells. **And the recovery is not settled at all**: `CH-0228`'s allowed `8.57142857°` moves a cell by up to **0.00242194151** of the stroke, **5.7×** the deciding margin, so exactly **1 of 64** is flat at each sign and it is a *different* cell (`T-284`). **Zero census occurrences were added** — the advisory `T-233` debt line reads `24 of 88` before and after, and `tools/T-234-classification.json` is untouched

| | |
|---|---|
| **Task** | [`T-288`](../tasks/T-288-carrying-the-tied-regrade.md) — carry `C-0180`'s reversal into the two customer-facing documents, ahead of the full thirteenth synthesis |
| **Leaf** | `A8.2` |
| **Verification type** | **logical** — a reconciliation of two outward-facing documents against the claim corpus and the task queue, with every mechanised half run by a retained checker and recorded before and after, and every number `grep`ed out of the claim or the result file that owns it |
| **Verdict** | **PASS on `P1`–`P5`.** `P1`: all eight occurrences of the `0 of 64` family are annotated, none deleted, none overwritten. `P2`: the three sentences the tied re-grade **falsifies** are struck and restated; the count sentences, which are **superseded and not disputed**, are annotated in place. `P3`: the four qualifications `C-0180` publishes beside its headline — the margin, the ceiling-not-multiplier reading, the tail and the prestrain sign — are carried into both documents. `P4`: every gate clean, and the two that were not clean mid-pass are recorded in §5 with what fixed them. `P5`: the `T-276` row is annotated with what this pass did and did not do, by name |
| **Maturity** | **TRL 1–3, and below it: NO PHYSICS CHANGED.** No study was run, no result file was written, no Kotlin was touched. Every quantity is read out of [`C-0180`](C-0180-tied-honeycomb-coupled-regrade.md) or out of [`gpd/results/T-279-tied-honeycomb-regrade.json`](../results/T-279-tied-honeycomb-regrade.json), and §3 says which |
| **Provenance** | Edits to [`ANSWERS.md`](../../ANSWERS.md) (9 annotations at 8 passages) and [`DECISIONS-FOR-NDI.md`](../../DECISIONS-FOR-NDI.md) (11 annotations at 11 passages), and one row of [`TASKS.md`](../../TASKS.md). Gates run **before and after** from the checkout: [`trace-answers.py`](../../tools/trace-answers.py) on both documents, [`check-markdown-tables.py`](../../tools/check-markdown-tables.py), [`check-corpus-links.py`](../../tools/check-corpus-links.py), [`check-corpus-identifiers.py`](../../tools/check-corpus-identifiers.py), [`check-challenge-index.py`](../../tools/check-challenge-index.py), [`check-cold-start-note.py`](../../tools/check-cold-start-note.py), [`check-entry-points.py`](../../tools/check-entry-points.py), [`T-234-census.py --check`](../../tools/T-234-census.py) and [`check-queue-vocabulary.py`](../../tools/check-queue-vocabulary.py); readings in §5. **No Gradle run**: nothing in `src/` is touched, and this claim says so rather than quoting a suite count it did not take |
| **Conditions** | Documents only, on the working tree of iteration 43 with `HEAD` at `9620d3e`. The *before* reading of each deliverable is taken from `git show HEAD:<path>` rather than from the working tree, so no concurrent agent's edit is attributed here. Units unchanged and untouched: nm, pN, pN/nm, pN/nm² = 1 MPa exactly, `k_BT` = 4.142 pN·nm at 300 K |
| **Consumes** | [`C-0180`](C-0180-tied-honeycomb-coupled-regrade.md) (`T-279`) — every number carried; [`C-0175`](C-0175-drawable-raster-rim.md) (`T-254`) — the tie set and the free-tile ratio; [`C-0167`](C-0167-coupled-cells-on-the-honeycomb-grillage.md) (`T-263`) — the count being superseded; [`CH-0227`](../challenges/CH-0227-the-honeycomb-lattice-omits-the-rasters-own-turn-ties.md), [`CH-0228`](../challenges/CH-0228-every-allowed-scaffold-crossover-is-a-prestrain.md), [`CH-0234`](../challenges/CH-0234-no-verdict-reverses-was-a-free-tile-statement.md); [`C-0071`](C-0071-output-element-recommendation.md) (*strike, never delete*); [`C-0080`](C-0080-third-answers-synthesis.md) (the superseded-standing-value drift class); [`C-0171`](C-0171-twelfth-answers-synthesis.md) (the method, and the pass this one is a fragment of) |
| **Constrains** | Nothing new. The thirteenth full synthesis is still owed and the [`T-276`](../tasks/T-276-twelfth-answers-synthesis.md) row now names its residue |
| **Raises** | Nothing against a standing claim. `CH-0240` is **reserved and released unused** — the one thing that looked like a challenge is a consequence this pass created and immediately declared, in §3 |

---

## 1. The census — found, annotated, left

The search set is every spelling of the superseded count and every figure `C-0180` moves, taken
against `git show HEAD:<path>` so that a concurrent agent's edits are not attributed here.

| family | `ANSWERS.md` | `DECISIONS-FOR-NDI.md` | annotated | left |
|---|---|---|---|---|
| the count, `` `0` of `64` `` and `0 of 64` | **5** | **3** | **8 of 8** | 0 |
| *"no coupled cell … is flat"*, as an absolute | 1 | 2 | **3 of 3, STRUCK** | 0 |
| the recommended cell, `0.145086839` / `0.149852804` | 11 tokens at **5** passages | 6 tokens at **4** passages | **9 of 9 passages** | 0 |
| the uncoupled block, `0.0501417315` / `0.0522223659` | 12 tokens at **6** passages | 14 tokens at **7** passages | **12 of 13 passages** | **1** |
| the enhancement-free block, `0.132443428` | 2 tokens at 2 passages | 4 tokens at 4 passages | **6 of 6** | 0 |

**The one passage left un-annotated is a `15 × 4` comparison cell** — the *"cost of deferring"* cell
of `DECISIONS-FOR-NDI.md`'s at-a-glance decision 7, where `10 × 6`'s uncoupled dishing stands beside
three `15 × 4` readings that **`C-0180` does not re-grade at all**. Its **coupled** half *is*
annotated; giving the `10 × 6` side a tied number the `15 × 4` side cannot be given would make the
row read as a measured ordering across two lattice states when only one side moved. It is left
deliberately, and named here.

**Neither deliverable asserted anything else this iteration supersedes.** `C-0178`, `C-0179`,
`C-0181`, `C-0182`, `C-0183` and `C-0184` are process claims, and `CH-0224` is a schema question:
`grep` over both documents returns **0 hits** for each of `C-0176`, `C-0177`, `C-0178`, `C-0179`,
`C-0181`, `C-0182`, `C-0183`, `C-0184`, `CH-0224`, `CH-0229`, `CH-0230`, `CH-0231`, `CH-0232`,
`T-280`, `T-281`, `T-282`, `T-283`, `T-285` and `T-286`, and the word *"regime"* — four occurrences in
`ANSWERS.md`, nine in `DECISIONS-FOR-NDI.md` — is used throughout for a **physical** regime or for
NDI's own quoted word about a buffer range, never for this repository's `Regime` type, so `C-0181`
and `CH-0224` reach neither document. **No ratio in either document has an argument that moved**: the
only ratio the tie set could touch is `CH-0227`'s `1.12×`, and neither deliverable carried it —
`ANSWERS.md`'s three occurrences of `1.12×` are `C-0002`'s validity ceiling and `C-0068`'s margin,
and `DECISIONS-FOR-NDI.md` has none. **So the answer to the sweep is: nothing further.**

## 2. What had to be carried, and why the count alone would have misled

`C-0180`'s headline is four statements and a synthesis that carries only the first of them hands a
reader a win where the claim publishes a knife edge.

| what | value | why a reader needs it |
|---|---|---|
| the count | **`2` of `64`**, against `C-0167`'s `0` of `64` | the verdict that moved |
| the recovered cells | **0.106041029 → 0.0995744767** at 30 paths and **0.101931622 → 0.0998791032** at 50, both `f = 0.30`, both rim-graded 5:1 | *which* cells, and that both sit at the band's favourable end |
| the margin | **0.426 %** of the tolerance — 0.000425523 of the stroke — converged: 0 of 6 deciding-cell steps move it, worst departure `4.57e−4`, a factor of **9.3** | thin **and** resolved, which is a different statement from thin |
| the ceiling | free tile **0.890395426** = `1.123×`; per-realisation **median** ratio **0.902845544 to 0.988116016** = `1.012–1.108×` | the free tile is **not an order statistic of the coupled answer**; `CH-0227`'s `1.12×` over-states at **every one of the 64 cells** |
| the tail | median below one at **64 of 64**, 90th percentile of the same ratio **above** one at **27 of 64**, worst single realisation **1.15725406**, ties adverse at **0.2 %** to **27.45 %** of the ensemble | a favourable median is not a favourable ensemble |
| the sign | at **8.57142857°** each cell moves by up to **0.00242194151** of the stroke — **5.7×** the deciding margin — so exactly **1 of 64** is flat at each sign, a *different* one | `2 of 64` is **not settled**, and the open task is `T-284` |

And what does **not** move is as load-bearing as what does: **62 of 64** coupled cells still fail,
the recommended one-column cell among them; **64 of 64** coupled cells are still worse than the
uncoupled tile; **48 of 64** are still flat at the nominal with no defects; and the uncoupled block
is still what is flat, moving the favourable way to **0.0446459684** and **0.0467367262**.
**The programme's headline flatness answer is unchanged** — it stands on the uncoupled tile, and it
did before this pass.

## 3. Two numbers this pass owns, and it says so

`ANSWERS.md`'s own rule is that every number in it belongs to a claim. Two of the readings a
reader most needs — **what the tied lattice does to the cell this programme actually recommends** —
are in `C-0180`'s result file and in no claim's prose: the one-column, ten-path, equal-spring
abstract-grid cell moves

| | untied (`C-0167`, `C-0180`) | **tied** |
|---|---|---|
| `f = 0.30` | 0.145086839 | **0.138518264** |
| `f = 0.26` | 0.149852804 | **0.144085797** |

and is **not flat** in either state. A third is a count: restricted to the eight `f = 0.30`
abstract-grid cells `C-0167` reports as `0 of 8`, the tied lattice gives **1 of 8** — the
30-path rim-graded cell, and only it.

All three are `paired[*]` records of
[`gpd/results/T-279-tied-honeycomb-regrade.json`](../results/T-279-tied-honeycomb-regrade.json),
read by `placement`, `columns`, `distribution` and `compositeFraction`; none is a figure `C-0180`
states. `CLAUDE.md` records the trap they create — *a synthesis claim that quotes a token in order
to explain it becomes that token's owner*, after which a numeric tracer reports the token as
traceable to a synthesis and to nothing else. **This claim is that owner, deliberately and by
name.** The honest reading of the tracer's verdict on those three figures is *"owned by
`C-0186`, derived from `T-279`'s result file"*, not *"owned by the corpus"*.

## 4. What this pass is NOT

- **It is not the thirteenth synthesis.** It is one drift class, carried, on one reversal. No
  section of either document was re-read end to end, no self-describing count was re-derived, no
  *"what we cannot answer"* list was reconciled against the queue, and the `T-276` row now says so.
- **It settles nothing about the physics.** The tie set, its stiffness, its axial station and its
  prestrain sign are all `C-0180`'s and all carry its validity range — `k_θ` at a scaffold turn is
  *asserted* equal to `k_θ` at a staple crossover, the tie sits at `s = ±L/2` where a scaffold
  crossover sits 5 bp from a staple position, and the block carries one row length.
- **It does not re-grade `15 × 4`,** which is why §1's two left passages are left.
- **It does not touch `tools/T-234-classification.json`.** That table is keyed on occurrence
  index, so a mid-flight regeneration is exactly what `C-0176` §1b warns against; the measurement
  in §5 is that no regeneration is owed.

## 5. The gates, before and after

Every reading is taken from the checkout, `HEAD` at `9620d3e`, with the *before* column read
against `git show HEAD:<path>` for the two deliverables.

| gate | before | after |
|---|---|---|
| `trace-answers.py` (`ANSWERS.md`) | 1 808 tokens: 1 663 `CITED`, 145 `ELSEWHERE`, **0 `ABSENT`**; 1 open assertion, 0 contradicted; **0** self-contradictions | 1 891 tokens: 1 732 `CITED`, 159 `ELSEWHERE`, **0 `ABSENT`**; 1 open assertion, 0 contradicted; **0** self-contradictions |
| `trace-answers.py --answers DECISIONS-FOR-NDI.md` | 939 tokens: 704 `CITED`, 235 `ELSEWHERE`, **0 `ABSENT`**; 0 open assertions, 0 self-contradictions | 1 016 tokens: 770 `CITED`, 246 `ELSEWHERE`, **0 `ABSENT`**; 1 open assertion, **0 contradicted**, 0 self-contradictions |
| `check-markdown-tables.py` | 0 defects in 604 files | **0** defects in 609 files |
| `check-corpus-links.py` | 0 broken links in 601 files | **0** in 606 |
| `check-corpus-identifiers.py` | 0 dangling in 581 files, 387 claims and challenges | **0** in 586, **390** |
| `check-challenge-index.py` | 208 files, 208 indexed | **209 / 209** |
| `check-cold-start-note.py` | 0 defects, heading 43, journal 43 | **0**, heading 43, journal 43 |
| `check-entry-points.py` | 0 defects, 131 of 133 studies | **0**, 131 of 133 |
| `T-234-census.py --check` | **GATE 0**; 386 occurrences in 41 files; debt `24 of 88 = 0.272727273` | **GATE 0**; **386** occurrences in 41 files; debt **`24 of 88 = 0.272727273`** |
| `check-queue-vocabulary.py` | 0 defects, 281 leading verdicts over 279 rows; residue 0 | **0** defects, **282** over **280** rows; residue **0** |

The file counts rise by the two files this task adds **and by a concurrent agent's**, which is why
they are recorded as read rather than as a difference this pass owns; the `ANSWERS.md` token count
rises because annotating is writing numbers down, and the nine tokens that moved out of `ABSENT`
are §3's three readings, now owned.
*(The tracer counts tokens per passage, so the final figure depends on whether the new paragraph is
separated from the iteration-40 one by a blank line — 1 891 joined, 1 912 separated, the same
tokens either way and **0 `ABSENT`** in both. It is joined, because the two restatements above it
are.)*

**Three gates went red mid-pass, all three this pass's own drafting, which is the point of running
them.**

1. `trace-answers.py` reported a **self-contradiction on `CH-0228`**: one sentence read *"is not
   settled"* — which the checker's negation guard reads as **OPEN** — and another *"is not to be
   read as settled"*, where the intervening *"to be read as"* defeats the same guard and the bare
   word reads **SETTLED**. Both sentences meant the same thing. Rephrased to *"the count is not
   settled"*, which is what the corpus's own idiom would have written.
2. `trace-answers.py` reported **9 `ABSENT` tokens**, every one of them the three derived readings
   of §3. They are `CITED` now because **this claim states them** — which is the mechanism working
   as designed and is exactly why §3 exists rather than the numbers being quietly dropped.

**The `T-234` census is the measurement that says no regeneration is owed**: **386** occurrences in
**41** files before and after, the same debt ratio, and `GATE 0` throughout — because every
annotation here was drafted around the census's own token set, which is the `FAMILIES` tuple of
[`tools/T-234-census.py`](../../tools/T-234-census.py) and is **read out of the tool rather than
recalled**. That was a **constraint on the drafting**, not a discovery, and it is recorded here
because a later author editing those passages will not know it.

**And the third gate to go red mid-pass was this claim.** A first draft of the paragraph above
**spelled two of those tokens out** in order to say they had been avoided, and the census duly
counted them: `GATE 2 defect(s)`, 388 occurrences in 42 files, both **unclassified**, both in this
file. That is `C-0176` §1b's *a claim about a census is inside that census's own scope* — reached
from a new direction, because the earlier instance was a claim quoting the **family** it measures
and this one is a claim quoting the **tool's own pattern**. The rule that follows is one line:
**name a census's tokens by where they live, never by what they are.**

## 6. What is still owed

- **The thirteenth `ANSWERS.md` synthesis** — `T-276`'s residue, now named in its own row.
- **`T-284`**: what fixes the sign of a raster turn's
  `8.57142857°` departure. Until it is answered, `2 of 64` and `1 of 64` are both readings of the
  same object and this programme cannot say which.
- **`15 × 4` coupled on the tied lattice**, which nothing has run, and which is why §1 leaves two
  comparison passages alone.
- **Whether the recommended `10 × 6` block needs an attachment coupling at all** — `C-0180`'s own
  first open question, and now the third claim to reach it.
