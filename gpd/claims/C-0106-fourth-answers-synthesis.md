# C-0106 — **The deliverable's three mechanical checks were clean and the tile it describes was the wrong size.** `C-0080`'s third drift class is back at **twelve instances in a 48-item range** against its own five in fifty-five, and the two that matter are not corrections to numbers but **whole structural determinations with no passage at all**: §3's 40.0 nm tile is **not a buildable raster width** (112 bp = **38.08 nm**, and **12.0 % flatter** there), and §5's *"six questions for NDI"* was the **old** six — two discharged, and their two live replacements absent while §1 of the same file already named one of them. Of 48 items, **14 were reflected, 24 are carried in and 10 are deliberately not**; **0 of 48 is a function of `σ`**, for the third consecutive pass. **`F2` fired and this is the first `ANSWERS.md` pass to raise a challenge against a claim** — `CH-0121`, on a word

| | |
|---|---|
| **Task** | [`T-175`](../tasks/T-175-fourth-answers-synthesis.md) — the fourth synthesis of `ANSWERS.md`, discharging the coverage statement `C-0080` left at iteration 16 |
| **Leaf** | — (a process claim; it audits the deliverable that reports every leaf) |
| **Verification type** | **logical** (a coverage partition of `C-0081`–`C-0105` / `CH-0093`–`CH-0120` against the deliverable, then a statement-by-statement adjudication with every owning claim read) **+ in-silico** ([`tools/trace-answers.py`](../../tools/trace-answers.py) and [`tools/check-markdown-tables.py`](../../tools/check-markdown-tables.py), both unmodified, run before and after) |
| **Verdict** | **PASS on all six predicates, and BOTH declared falsifiers fired.** The three mechanical checks reported **0 ABSENT of 855, 0 stale, 0 self-contradictions** before this pass began, and they were right: no number in the file was in no claim, no task was asserted open that the queue had closed, and no task carried two statuses. **What they cannot see is a determination that has no passage.** §3's 40.0 nm tile is not a buildable seamless raster width — the admissible row lengths are 16, 48, 80, 112 and 144 bp and 40.0 nm is **117.6** — so the tile is **112 bp = 38.08 nm**, **4.8 %** narrower, at which the same placement is **12.0 % FLATTER** (**0.0621469105** against 0.0706145537) and the crossover phase collapses onto the two the programme had already chosen. The strings `38.08`, `112 bp`, `seam` and `seamless` appeared **zero** times in the 830-line deliverable, which describes that tile in §2, §3 and §5. And §5's NDI table listed the **old** six questions: `T-95` and `T-102`, discharged in iteration 14, still occupying rows 3 and 4, and the **scaffold** (`T-154`) and the **two-layer tile** (`T-166`) absent — while §1 of the same file already called `T-166` *"item 6 of `DECISIONS-FOR-NDI.md`"*. Every row was individually correct; the **set** was two iterations stale. **`F2` fired too, mildly and for the first time in four passes**: `C-0102`'s headline says the three phase-demand sets *"stay disjoint"* and two of them are identical, which its own census table shows — `CH-0121`, a word, no verdict moving. **And the new sub-class is that `C-0088`'s self-consistency check reads TASK statuses**: two of the twelve third-class instances are a **challenge** given two statuses (`CH-0083`, *"open"* in the §2 verdict table and *"RESOLVED"* twelve lines below its own second occurrence), which no existing check can see. |
| **Maturity** | **TRL 1–3, and below it: nothing here is physics.** No number is re-derived, no solver runs, no verdict of any claim is examined except for whether the deliverable reports it. The locked-unit invariant is not engaged; units matter only as the frame each transferred number is read in. |
| **Provenance** | [`gpd/results/T-175-fourth-answers-synthesis.json`](../results/T-175-fourth-answers-synthesis.json) — 48 audit rows, 12 third-class instances, 7 ground re-checks, 19 edits, 6 predicates, 3 tool observations; edits to [`ANSWERS.md`](../../ANSWERS.md) §1, §2 rows 4 and 5b, the §Task 4 prose, §3 row (g), §4 and §5. `tools/trace-answers.py`: **1016 tokens, 911 CITED, 105 ELSEWHERE, 0 ABSENT, 0 open assertions contradicted, 0 self-contradictions** after (855 / 752 / 103 / 0 / 0 / 0 before). `tools/check-markdown-tables.py`: **0 defects in 326 files** before and after the deliverable's edits, 329 after this claim, its challenge and its task file were added. |
| **Conditions** | The corpus at iteration 22: **100 claim files** (`C-0038`, `C-0043`–`C-0045`, `C-0094` unused) and **109 challenge files** (`CH-0095`, `CH-0096`, `CH-0110`, `CH-0117` unused); highest `C-0105` and `CH-0120`. `T-182` and `T-178` were running in two sibling agents while this ran and none of their output is anticipated here. |
| **Consumes** | [`C-0080`](C-0080-third-answers-synthesis.md) (the third drift class, the coverage partition and the `not carried` list this inherits), [`C-0088`](C-0088-does-the-deliverable-agree-with-itself.md) (the self-consistency check and its declared blind spot), [`C-0078`](C-0078-status-drift-in-the-deliverable.md) and [`C-0067`](C-0067-answers-reconciliation.md) (the first two checks), [`C-0051`](C-0051-second-window-resynthesis.md) (the census that must travel with a null), and every claim and challenge in `C-0081`–`C-0105` / `CH-0093`–`CH-0120` |
| **Constrains** | `ANSWERS.md`. **One claim is challenged, on a word: [`CH-0121`](../challenges/CH-0121-two-of-the-three-phase-demand-sets-are-identical-not-disjoint.md).** No number, census cell, verdict or recommendation of any claim is contradicted. |

---

## 1. The cheap bound, and what it was worth this time

One `grep` per ID over the deliverable, before any claim was opened:

| | cited by ID in `ANSWERS.md` before this pass | not cited |
|---|---|---|
| claims `C-0081`–`C-0105` (24) | 9 | **15** |
| challenges `CH-0093`–`CH-0120` (24) | 5 | **19** |

**Thirty-four of forty-eight uncited** is what said this pass had a product rather than a re-read, and it
said so in seconds.

It was also, this time, an unusually *good* predictor: **the 14 cited by ID are exactly the 14 classified
`REFLECTED`**. That is not the general case and must not be inherited as one — on `C-0080`'s range four of
five falsifier instances were in **cited** material, and the bound is stated in `C-0080` as a bound for
exactly that reason. Even here it under-reports: **four of the fourteen were reflected in §1 only** and had
to be extended into §2, §3 row (g) or §5, which is where the shape change lives.

---

## 2. The coverage audit

Full rows with reasons are in the result file. The partition:

| disposition | count | what it means |
|---|---|---|
| **REFLECTED** | 14 | already cited by ID and read correctly |
| **CARRIED IN** | 24 | material and missing; now in the file |
| **NOT CARRIED** | 10 | repository numerics, tooling or branch bookkeeping, with the reason recorded |

### The ten deliberately not carried, and why

This list is the point of predicate `P3`: `C-0080`'s fifteen-item version of it is the only reason this pass
did not re-derive its judgement, and this one is written so the fifth pass need not re-derive mine.

- `C-0082`, `C-0083`, `C-0088`, `CH-0097` — **tooling and repository hygiene.** A reader census over
  `gpd/results/`, a markdown-table checker, the self-consistency check over this very file, and a correction
  to a rounding-site table the deliverable does not carry. No number, verdict, flag or validity range of any
  claim moves. `C-0083`'s rule is *enforced* on every edit made here rather than reported in the text.
- `C-0096`, `C-0101`, `C-0105`, `CH-0112`, `CH-0099` — **repository numerics and branch bookkeeping.** The
  doubling force ladder repaired as a branch continuation (90 of 96 outstanding rows could never have moved,
  and the ceiling `CH-0099` was raised about binds at **0 of 108** rather than 8); the re-emission of eleven
  result files; a withdrawn-ceiling note in one Kotlin study's prose whose own verdict is that the
  clause-correct reading is **3.33× stricter**. The deliverable already carries `C-0049`'s withdrawal, which
  is the outward-facing half.
- `CH-0120` — **a process guard**, queued as `T-181`: an influence bank assumes a host with no self-load.
  Every published consumer runs with `crossoverPrestrains` empty, so nothing published moves; what it
  protects is the *next* such term.

Two of the ten are recorded with a caveat the next pass should read: `C-0096` and `CH-0112` are *inside* the
recommended element's own fold branch, so if `T-182` or any successor reopens that branch they stop being
bookkeeping.

---

## 3. `F1` — the falsifier, and why it fired

### What was declared, before execution

> The only drift classes left in this file are the two `C-0088` names — a superseded number that still reads
> `CITED`, and a task carrying two statuses — both of which are defects **inside passages that exist**. It
> **fires** if the deliverable is missing a whole **structural determination** of the corpus: not a number
> needing correction but a finding with **no passage at all**, and specifically one that changes an object
> the file names in its own §2, §3 or §5.

### It fired twice, and both are about objects the whole file describes

**(a) The tile is 38.08 nm, not 40.0.** A seamless boustrophedon has only *progressive* scaffold crossovers,
so Rothemund's own odd-half-turn constraint binds the **row length**: admissible widths are **16, 48, 80,
112 and 144 bp** and **40.0 nm is 117.6 bp** (`CH-0101`, `C-0086`). The step is **32 bp = 10.88 nm**, not the
rise, so no tolerance argument reaches it — the second quantity in this programme, after `C-0072`'s margin,
that is below the resolution of the design language rather than merely tight. And the correction is
**favourable and selective**: 38.08 nm is exactly seven column pitches where 40.0 is 7.35, so the row-end
scaffold crossover is a lattice point only at phases **8 and 24** — precisely `C-0063`'s two centro-symmetric
phases, onto which `C-0015`'s ten eight-column phases also collapse — the upward station lattice is
**bit-identical** to the 40 nm one at departure `0.0`, and the best 34-root placement dishes
**0.0621469105** against `T-5b`'s 0.10, **12.0 % flatter** than the 0.0706145537 the deliverable quoted
(`C-0090`). The price is that the arm must be **quantised**, the binding plan ceiling switching from the
inboard 8.19 nm to the outboard **8.16 nm** (`CH-0105`), which `C-0085`'s 24-rise arm meets exactly.

**Every §2, §3 and §5 statement about the tile was written for a tile that cannot be folded**, and no check
in the repository could see it, because a *width* that appears nowhere is not a token, a status or a
contradiction.

**(b) The six questions for NDI were the old six.** §5's table carried `T-95` (superstructure perforation)
and `T-102` (tile area) as rows 3 and 4 — both **discharged by `C-0071` in iteration 14** — and did not carry
the **scaffold** (`T-154`) or the **two-layer tile** (`T-166`) at all, although `DECISIONS-FOR-NDI.md` has
carried both since iterations 18 and 19 and although **§1 of the same file already named `T-166`** as *"item
6 of `DECISIONS-FOR-NDI.md`"*.

The instructive part is that **each row was individually correct**: the two discharged rows say
`DISCHARGED` in their own cells, so the file is not lying anywhere a reader can point at. What was two
iterations stale is the **set** — and the same is true one level up, in §1, where *"a height plus five
specification questions"* was still five because two members left and two arrived. **A count can be right
while none of the arithmetic behind it is.**

> **The classes a checker can see are the ones where a passage exists and is wrong. The class that costs
> most is a passage that is not there, and the only instrument for it is a reader who knows the corpus.**

---

## 4. The third drift class: twelve instances

`C-0080` found five in fifty-five items; this pass finds **twelve in forty-eight**, all recorded with the
reason each is invisible in the result file's `thirdDriftClass` array. They fall into four kinds:

| kind | instances | example |
|---|---|---|
| a **status word outside the checker's vocabulary** | 3 | *"`T-139` is in flight in this same iteration"* — `TASKS.md` has had `T-139` `DONE` since iteration 16, and `queue_status` reported 0 stale throughout, because *"in flight"* is not an open word |
| a **challenge** given two statuses in one file | 2 | `CH-0083` read *"open"* in the §2 verdict table and in the §Task 4 header, and *"RESOLVED"* twelve lines below the second one. `C-0088`'s check keys on **task** IDs |
| a **ground** superseded while its verdict stands | 4 | *"which `T-139` can settle either way"* — the sentence `C-0080` itself wrote as the recommendation's re-checked ground, in the iteration `T-139` landed |
| a **number or a count** overtaken | 3 | *"Eighty-four challenges against seventy-four claims"*, written as words so the numeric tracer sees no token at all; the 2.08× redundancy slope, now challenged by `CH-0119`; the 8.19 nm plan ceiling, correct only at 40.0 nm |

**The sub-class worth keeping is the second.** `C-0088` mechanised *"a task the deliverable gives two
statuses"* and explicitly scoped it to task IDs. A challenge is exactly as status-bearing as a task —
`OPEN`, `UPHELD`, `RESOLVED`, `WITHDRAWN`, `STANDS` — and the corpus carries a hundred and nine of them.
Extending `status_words` from task IDs to challenge IDs is the cheapest next increment on that tool and is
queued as **`T-183`**. It is not done here because `tools/` was in use by a sibling agent, which is the same
reason `C-0080` reported three tool observations rather than fixing them.

---

## 5. The grounds re-checked

`C-0078`'s rule — *a verdict that survives can survive on a different reason* — applied to every verdict the
range touched. Seven, and **the two that matter run in opposite directions**.

1. **`C-0071`'s recommendation stands as the best element the catalogue contains and NOT as a buildable
   design.** Survives. **Its ground has moved twice more and the second move is FAVOURABLE**, which is the
   direction a synthesis is least likely to check. `C-0080` re-based it on *"the exclusion width in this role
   is unmeasured"*; `C-0079` then **removed the question** — two unbonded duplexes are repulsive at every
   separation on four independent methods, so there is no separation to measure, only a **threshold on an
   energy** (`CH-0094`, 11.45 nm at 0.5 `k_BT` to ≤ 2.1 nm at 8 `k_BT`, straddling 2.715609 nm) — and
   `C-0085`/`CH-0100` turned the collinear term into an **integer count of rises**, **2 whole rises,
   0.67561 nm, 26.38×**, with all three of `C-0071`'s live `NONE` bands becoming real margins. **A verdict
   whose stated ground has been replaced three times in four iterations, never moving**, is worth reading as
   such, and the deliverable now says so in place.
2. **§6 task 4, `PASS`.** Survives, and its ground changed **completely**: from *"answered for two load
   lines, neither of them the recommended element's"* to **discharged for the recommended device** —
   no fold at 2 mM at **0 of 6** layer models (`C-0084`), with the branch continued past `C-0084`'s own
   force-ladder artefact to cover **0.9984** of the arm's contour (`C-0092`, `CH-0107`). The §2 verdict
   table had not moved, and still called `CH-0083` open.
3. **The annihilated weave bracket, coefficient exactly zero.** Survives, and is now **conditional on the
   tile having no seam** (`C-0081`: a seam deletes a weave extremum and puts 6–12 of the same 34 stations off
   the node, restoring the bracket at full strength and taking the worst across-row clearance to
   **−0.0023 nm**). The condition is met, because the Gen-1 sheet takes **1 680** of M13's **7 249** nt and is
   therefore not fully folded (`C-0086`). `C-0081` names the surviving reason **in advance**, which is the
   first time that has happened here.
4. **The tile can be made flat as designed.** Survives **on a different number at a different width** —
   **0.0621469105** at 38.08 nm rather than 0.0706145537 at 40.0 (`C-0090`).
5. **The flat-tile question is closed on the coupling axes.** Survives. One of the numbers measuring it is
   challenged and open: `CH-0119` charges that `C-0098`'s **−0.376769756** redundancy slope is fitted through
   placement-**searched** optima, and on the same lattice a *nested* chain fits **−0.740086889** where a
   searched one fits **+0.0610348337** (`C-0103`). `C-0098`'s **3.76×** shortfall does not depend on it.
6. **`C-0093`'s 0.24028028 is the lowest dishing attained.** Survives, qualified twice inside the range: its
   90-station grid is **not buildable** (`CH-0113` — the lattice offers at most 60) and its per-tie ceiling is
   **stated at 3.33333333 pN/nm and used at up to 1000** (`CH-0114`).
7. **0.5 mM is recommended.** Survives on **three** routes not six, as a **preference** not a requirement,
   with the largest quoted number 3.16–3.35× smaller (`C-0091`, `CH-0106`, `C-0084`, `CH-0098`). Already
   carried correctly by the coordinator in iteration 18; re-checked, nothing owed.

---

## 6. The window, and the census that travels with the null

`C-0051`'s discipline. **Of the 48 items in the range, ZERO carry a quantity that is a function of `σ`.**

They are lattice widths, crossover phases, placements, folding statistics, a stacking energy, two pull-in
folds read at fixed layer heights and buffers, a 2-D collar re-solve at fixed geometry, and eight
repository-numerics or tooling items. **0 of 6 window edges move.**

That is the **third consecutive null and the third consecutive reading rather than expectation** — and it is
still **not a re-intersection**. A re-run would cost a study to return the same null for the same reason
`C-0051` found it. What the range moves is, again, everything *below* the window: the tile's width, the
crossover phase, the flatness answer's topology, and the questions handed to NDI.

---

## 7. Verify — the five gates, as they apply to an audit

1. **Dimensional consistency.** Nothing is computed; every quantity is transferred with the unit its claim
   states. Every number written into the deliverable by this pass was **grepped out of its owning claim
   before it was written** — `38.08`, `112 bp`, `117.6`, `0.0621469105`, `0.0706145537`, `12.0 %`,
   `0.0651753854`, `1.0487309`, `0.168371808`, `0.0658484805`, `0.125068659`, `0.0400 %`, `1 680`, `7 249`,
   `5 569`, `63 %`, `11 %`, `1.66×`, `−0.0023 nm`, `0.67561 nm`, `26.38×`, `79.678`, `133.687`, `13.930`,
   `25.689`, `0.638498565`, `0.720607136`, `0.639129638`, `0.583664426`, `−0.740086889`, `+0.0610348337`,
   `0.0344013403`, `0.24028028`, `0.375506727`, `0.100166871`, `3.58698588×`, `48/49`, `1.899×`.
2. **Limiting cases.** The two mechanical checkers were run at both ends and are unmodified. The numeric
   half went 855 → 1016 tokens at 0 ABSENT throughout, which is the limiting case that matters: **161 new
   tokens, none of them unowned**.
3. **Symmetry and conservation.** The conservation law of a reconciliation is that **no statement is
   deleted**. Every superseded passage is retained in place with its supersession marked — the §5 `T-139`
   bullet is struck through rather than removed, the two discharged NDI questions keep their full text in
   their own labelled table, and the §1 knife-edge narrative retains all three of its grounds in order. The
   file's own history is recoverable from the file.
4. **Numerical convergence.** The audit's convergence question is the partition itself, checked from both
   sides: every ID in the range appears in exactly one of 48 rows, and the three dispositions sum to 48.
5. **Literature cross-check.** The "literature" of a synthesis is the claim corpus, and the cross-check is
   gate 1's grep list.

**And the checkers caught their own user, for the fourth consecutive iteration.** Two self-contradictions
were *introduced by this pass* and caught by `C-0088`'s check before the edits were finished: a sentence
containing `T-95` and `T-102` beside the phrase *"a list of open questions"*, and one containing `T-5b`
beside the word *"open"* used of a challenge. Both were the check's one-sided evidence rule working exactly
as `C-0088` designed it, and both cost a rewording. **The friction is the price of a checker that can be
believed, and it is the right price** — but it is worth recording that the OPEN word list matches inside
ordinary prose, including the word *"reopen"*.

---

## 8. What was left undone, and what should go on the queue

- **`T-183` (new): teach `status_words` to read CHALLENGE ids.** Two of this pass's twelve third-class
  instances are a challenge with two statuses, and `C-0088` scoped its check to tasks. 109 challenge files
  carry statuses; the vocabulary already exists (`OPEN`, `UPHELD`, `RESOLVED`, `WITHDRAWN`, `STANDS`,
  `DISCHARGED`). Cheap, and it closes a hole this claim found by hand. **Taken above `T-182`, the highest in
  use.**
- **`DECISIONS-FOR-NDI.md` question 6 says *"`T-165` has not been run"* and `T-165` was run in iteration 20**
  (`C-0098`). Not fixed here because that file was not this task's mandate, and it is exactly the class this
  claim exists to find. **`T-184` (new).**
- **The `(σ, L₀)` window is not re-intersected**, for the third time, with the reason now measured three
  times over.
- **`CH-0121` is owed a wording correction to `C-0102`'s title**, at whatever point `C-0102` is next edited.
  `ANSWERS.md` already carries the corrected reading.
- **`CH-0119` and `CH-0104` are open and both qualify the flat-tile closure**; `T-180` settles the first.
  §1 and row (g) now carry both as open qualifications rather than as settled physics.
- **When `T-182` lands, §1's row-end block and §5's prestrain bullet both have to be re-read.** That is this
  claim's version of `C-0080`'s `T-139` note — and `C-0080`'s note is precisely the item this pass found
  un-actioned three iterations later, in three places, so it is repeated here in the same words.
