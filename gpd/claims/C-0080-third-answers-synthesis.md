# C-0080 — **There is a third drift class and both retained tools are blind to it by construction: a number still in the corpus, under a verdict its owning claim still states, that a LATER claim superseded.** Five instances in the range `C-0052`–`C-0078` / `CH-0065`–`CH-0092`, and the sharpest is the deliverable contradicting **itself** — §1 saying `T-45` *"is answered and the answer is a failure"* while §3 said *"(`T-45` is still unmeasured)"*, both halves individually tracing clean. Of 55 items, **23 were already reflected, 17 are carried in and 15 are deliberately not**, and **not one of the 55 is a function of `σ`**

| | |
|---|---|
| **Task** | [`T-147`](../tasks/T-147-third-answers-synthesis.md) — the third synthesis of `ANSWERS.md`, discharging the coverage statement [`C-0067`](C-0067-answers-reconciliation.md) filed in iteration 12 and [`C-0078`](C-0078-status-drift-in-the-deliverable.md) found still standing in iteration 15 |
| **Leaf** | — (a process claim; it audits the deliverable that reports every leaf) |
| **Verification type** | **logical** (a coverage partition of the claim corpus against the deliverable, then a statement-by-statement adjudication of every passage the range moves, with each owning claim read in full) **+ in-silico** ([`tools/trace-answers.py`](../../tools/trace-answers.py), 42 self-tests, run before and after and unmodified) |
| **Verdict** | **PASS on all six predicates, and the declared falsifier FIRED.** `C-0067` found the drift is in the **status** of answers and built a numeric tracer; `C-0078` found the status class back and taught the tracer to read `TASKS.md`. Both halves report **zero** before this pass. **What they cannot see is a passage whose number is still in the corpus and whose verdict its owning claim still states, superseded by a LATER claim** — because the superseded number has an owner and therefore reads `CITED`. **Five instances**, all in the programme's own headline material: the escape from `C-0071`'s knife edge is understated at **53×** where the lattice affords **68.9×** (`CH-0086`); its flatness cost is quoted at **0.2603**, a plan-rule *upper bound*, where the searched answer is that **equal springs cannot build it at all** (0.166653 / 0.172575 / 0.1670) and a **distribution can** (0.06822 at phase **8**, not `C-0063`'s 24) — *the escape costs the equal springs, not the flatness* (`C-0074`); the **0.0256 nm** knife edge is a margin against a `d` that **has never been measured in the role it is used in**, and at the measured phosphate contact three of its four floors stop firing (`CH-0089`, `C-0076`); `T-45` is answered and **past** `C-0060`'s threshold while §3 still said it was unmeasured (`CH-0084`); and NDI question 1's word ***requirement*** was earned on a strain-**softening** load line the programme no longer recommends (`CH-0083`). **The T-45 instance is the sharpest and is a fourth thing again**: it is the file disagreeing with **itself**, both halves tracing clean to the corpus, which no check written against the corpus can catch. **The window census is the null `C-0051` predicted and it is now a reading rather than an expectation: of 55 items, ZERO carry a quantity that is a function of `σ`.** Raises no challenge — **every disagreement found was the synthesis misreporting the corpus**, which is what both prior passes also found, so for the third time no claim is contradicted. |
| **Maturity** | **TRL 1–3, and lower: nothing here is physics.** No number is re-derived, no solver runs, no verdict of any claim is examined — only whether the deliverable reports them. The locked-unit invariant is not engaged; units matter only as the frame each transferred number is read in. |
| **Provenance** | `gpd/results/T-147-third-answers-synthesis.json` — 55 audit rows, 5 falsifier instances, 6 ground re-checks, 16 edits, 6 predicates, 3 tool observations; edits to [`ANSWERS.md`](../../ANSWERS.md) §1, §2, §3 rows (f) and (g), §4 and §5; `tools/trace-answers.py` **727 tokens, 620 CITED, 107 ELSEWHERE, 0 ABSENT, 1 open assertion, 0 contradicted** after the edits (613 / 503 / 110 / 0 / 1 / 0 before), and `tools/test-trace-answers.py` **all checks passed** |
| **Conditions** | The corpus as of iteration 16: **74 claim files** (`C-0038`, `C-0043`–`C-0045` unused) and **84 challenge files**, highest `C-0078` and `CH-0092`. `T-139`, `T-140` and `P-22` were in flight in three sibling agents while this ran and none of their output is anticipated here. |
| **Consumes** | [`C-0067`](C-0067-answers-reconciliation.md) (the tracer, the taxonomy and the coverage statement this discharges), [`C-0078`](C-0078-status-drift-in-the-deliverable.md) (the status half and the *"a verdict can survive on a different reason"* discipline), and every claim and challenge in `C-0052`–`C-0078` / `CH-0065`–`CH-0092` |
| **Constrains** | `ANSWERS.md`. **No claim is touched and none is contradicted.** |

---

## 1. What was audited, and the cheap bound that sized it

The range is `C-0052`–`C-0078` (27 claims) and `CH-0065`–`CH-0092` (28 challenges): **55 items**.

The cheap bound is one `grep` per ID over the deliverable, and it runs before any claim is opened.
It partitions the range in seconds:

| | cited by ID in `ANSWERS.md` before this pass | not cited |
|---|---|---|
| claims `C-0052`–`C-0078` | 16 | **11** |
| challenges `CH-0065`–`CH-0092` | 7 | **21** |

**That partition is what tells you how much prose there is**, and doing it the other way — reading 55 claims
and then asking which are missing — costs 55 reads to learn what one `grep` says.
It is also only a *bound*: being cited is not being carried correctly, which is where four of the five
falsifier instances were found.

The mechanical baseline runs first for the same reason.
`tools/trace-answers.py` reported **613 tokens, 0 ABSENT, 0 stale** before any edit — so the two
machine-checkable classes were clean by construction, and everything below is a class neither half can see.

---

## 2. The coverage audit

Full rows, with reasons, are in the result file. The partition:

| disposition | count | what it means |
|---|---|---|
| **REFLECTED** | 23 | already cited by ID and read correctly |
| **CARRIED IN** | 17 | material and missing; now in the file |
| **NOT CARRIED** | 15 | branch interior or repository numerics, with the reason recorded so the next pass inherits the judgement |

The 15 deliberately not carried, and why — this list is the point of `P3`, because
**`C-0067`'s seven-item version of it is the only reason this pass did not re-derive its judgement:**

- `C-0053`, `C-0054`, `C-0056`, `CH-0066`, `CH-0069` — **the in-plane hinge-budget branch**, whose whole
  arithmetic rests on a hinge consuming an *interface* crossover. `C-0055`'s unused **out-of-plane** azimuth
  falsified that premise, and the deliverable carries the successor (52–60 rather than 42, the sheet in one
  piece at every count). `CH-0069` records itself as *conditional on `CH-0068`* and is mooted by it.
  `C-0067` had already classified the first three as branch interior; this pass agrees and adds the reason
  it did not have then.
- `C-0067`, `C-0078` — **process claims about this file.** The deliverable is their artifact, not their
  subject.
- `C-0073`, `CH-0092` — **repository numerics.** A result file's determined precision; no verdict, flag or
  quoted figure of any claim moved. One favourable side effect is recorded in the result file rather than in
  the deliverable: `CH-0092`'s re-emission audit verifies that NDI question 1's deciding margins survive at
  the precision they are quoted at.
- `CH-0085` — **a rendering rule** (never quote the 10 nm upper edge as `0.2601`, which is exactly a
  four-figure tie). Checked and there is nothing to fix: the deliverable quotes `C-0027`/`C-0051`'s
  resynthesised **`0.2885`, 24.8×** and never the tie. Recorded so the next pass does not repeat the check.
- `CH-0067`, `CH-0075`, `CH-0082` — **truss-branch bookkeeping**; no surviving design changes and, in
  `CH-0075`'s case, the best design is unchanged to three digits.
- `CH-0076` — retires a **mirrored** placement set the top-level flatness answer no longer rests on.
- `CH-0087` — a missing column in one sensitivity table; no headline moves and that is *verified* rather
  than asserted. Its outward-facing by-product, the step-function ceiling, is carried via `C-0075`.

---

## 3. The falsifier, and why it fired

### What was declared

> The only classes of drift in this file are (i) a number in no claim and (ii) a task asserted open that the
> queue says is closed, both of which `tools/trace-answers.py` now reports at zero.
> It **fires** if a **third class** exists: a passage carrying a number that is still in the corpus and
> therefore still reads `CITED`, and a verdict still literally what its owning claim says, **which a LATER
> claim in the range has superseded**.

### It fired, five times, and all five are in headline material

| # | the passage | the tracer reads | superseded by | what it should say |
|---|---|---|---|---|
| 1 | §1: *"30 paths … buy **53×** of margin"* | `CITED` — 53 is in `C-0072` | `C-0074`, `CH-0086` | **68.9×**, on a plan ceiling of **9.5350 nm** and a margin of **1.76451 nm**. *A plan ceiling is a property of a placement, not of a count* |
| 2 | §1: *"it **loses the flatness**, 0.0706 → **0.2603**"* | `CITED` — 0.2603 is in `C-0072` | `C-0074` | 0.2603 is a plan-**rule** upper bound. Searched: equal springs reach **0.166653** (phase 24), **0.172575** (phase 8) and **0.1670** over the non-symmetric family at every phase, all outside 0.10 — and a **distribution** at `C-0017`'s unchanged total reaches **0.06822** at phase **8**. **The escape costs the equal springs, not the flatness** |
| 3 | §1: *"`pitch − d − L` = **0.0256 nm**"* and its four floors | `CITED` — every figure is in `C-0072` | `CH-0089`, `C-0076` | `d` is the girth of one **free** duplex; 2.69 nm is the packing distance of a **crossover-bonded pair**. At the measured contact **1.817276 nm** the clearance is **+0.898333 nm, 35×** the published margin and **three of the four floors stop firing** |
| 4 | §3 (g): *"(`T-45` is still unmeasured)"* | `CITED` — 34.6 is in `C-0060`; the parenthesis carries **no number at all** | `C-0072`, `CH-0084` | `T-45` is answered from published measurement — **48–95 %, mean 84 %**, i.e. **43.6 %** Bernoulli per-path scatter, **1.26×** the threshold |
| 5 | §5 q1: *"`C-0032` makes it a **requirement**"* | `CITED` — every figure is in `C-0032` | `CH-0083` | The word was earned on a strain-**softening** load line. The recommended element strain-**stiffens**, clears the 2 mM static floors at **6 of 6**, and its fold has **never been searched** |

### And instance 4 is a fourth thing again, which is the finding worth keeping

`ANSWERS.md` §1 already said, in bold, *"**`T-45`, open since iteration 3, is answered from published
measurement — and the answer is a failure.**"*
§3 row (g), two hundred lines further down, still said *"(`T-45` is still unmeasured)"*.

**Both halves trace clean.** §1's sentence is `C-0072`'s and §3's parenthesis carries no number at all, so
there is nothing for a numeric tracer to catch; and `TASKS.md` marks `T-45` **ANSWERED**, so the status
half — which looks for a *task ID* near an *open word* — does not see a parenthesis that names the task and
says *"unmeasured"* rather than *"open"*.

> **A tool that checks a document against a corpus cannot see a document that disagrees with itself.**

That is a genuinely new class, it is cheap to check (the deliverable is one file), and it is what the next
pass should be told to look for.

---

## 4. The grounds re-checked

`C-0078`'s rule — *a verdict that survives can survive on a different reason* — applied to every verdict in
the file whose premise the range withdrew. Six, and **two came out weaker than the file stated**, which is
the direction a synthesis is least likely to notice.

1. **"`C-0071`'s recommendation stands as the best element the catalogue contains and NOT as a buildable
   design."** Survives. **Its ground is now weaker, not stronger.** As written it rested on a 0.0256 nm
   margin exceeded by four independent floors; `CH-0089` stops three of them firing at the measured girth,
   and what remains is the statement that **the exclusion width in this role is unmeasured** — which no
   tolerance model repairs and which `T-139` can settle *either way*. Stated in place.
2. **Task 4, *"PASS — both branches answered, each for a different load line"*.** Survives as a statement
   about the two load lines searched. Falls as a statement about the Gen-1 device: neither is the
   recommended element's, whose fold has never been searched (`CH-0083`, open). Qualified in the table row
   and in the prose, with both directions given — `Q5`'s **30.03 pN/nm** tangent clears the 2 mM static
   floors at 6 of 6 where `C-0030`'s clears none of the 23.41–27.91 band, and a held-gap margin is still not
   a fold margin.
3. **The 0.5 mM recommendation.** Survives, on the other five routes and on the fold not existing there
   (1.038–2.327× on bias). The **escalation to a *requirement* does not transfer** to the recommended
   element. Qualified in NDI question 1.
4. **"45 attachments as a 3 × 15 grid".** `CH-0079` (open): on an **armed** tile **26 of the 45 stations do
   not exist**, and no rigid translation of a two- or three-column grid clears every row at any of
   **400 001** swept offsets. The escape is *along* the helices at **+1.7 %** of dishing. Latent behind the
   flexure-and-tie branch `CH-0081`/`C-0069` removed from the output role, and it re-binds with it. Carried
   into row (f).
5. **`T-31`, the flexure-array compliance question.** Moot at the count it was asked about — `C-0041` shows
   the 45-path array has no plan view, and the recommended element has no flexure at all. Returns at
   **15 paths**. Re-scoped in place, which is the *"a discharge is invisible to whoever files the removal"*
   lesson applied to a disclaimer rather than to a question.
6. **The 42-of-56 hinge budget.** Superseded by `CH-0068`/`C-0055`; the deliverable already carries the
   successor and the 42 appears only as the foil. No edit; recorded as deliberately not carried.

---

## 5. The window, and the census that has to travel with it

`C-0051`'s discipline: *a window that does not move is evidence of nothing until you count how many of the
new constraints are functions of its own axis.*

**Of the 55 items in the range, zero carry a quantity that is a function of `σ`.**
They are placements, plan views, lattice congruences, torsion closures, a tolerance model, a stated
recommendation and two repository-numerics claims.
So no window edge moves, and unlike iteration 12's version of the same sentence **this is a reading rather
than an expectation** — but it is still **not a re-intersection**, and a re-run would cost a study to return
the same null for the same reason `C-0051` found it. The deliverable now says exactly that.

What the range *does* move is everything below the window: the output element, the price of recommending
it, and the flatness answer.

---

## 6. Verify — the five gates, as they apply to an audit

1. **Dimensional consistency.** Nothing is computed; every quantity is transferred with the unit its claim
   states. The tracer strips claim, task, leaf, section and date identifiers so an ID is never read as a
   quantity (tested).
2. **Limiting cases.** The tracer is tested at both failure directions — a number present only in an
   *uncited* claim must not read `CITED`, and a number in no claim must read `ABSENT`. 42 checks pass.
3. **Symmetry and conservation.** The conservation law of a reconciliation is that **no statement is
   deleted**: every superseded figure is retained in place with its supersession marked, so the file's own
   history is recoverable from the file. Instances 1–5 above are all *marked*, never overwritten.
4. **Numerical convergence.** The audit's convergence question is the coverage partition itself, and it is
   checked from both sides: every ID in the range appears in exactly one of 55 audit rows, and the three
   dispositions sum to 55.
5. **Literature cross-check.** The "literature" of a synthesis is the claim corpus. **Every number written
   into the deliverable by this pass was grepped out of the claim or challenge that owns it before it was
   written** — per `SESSION-PROMPT.md` step 9 — and the grep counts are the evidence, not the memory.

---

## 7. What was left undone, and what should go on the queue

- **The `(σ, L₀)` window is not re-intersected.** Reason in Part 5, and it is now a measured reason.
- **`T-139` is in flight and decides `C-0069`'s `Q5` outright.** The deliverable names it, names the step
  function (`pitch − arm` = **2.715609 nm**, 34 of 34 below and 22 above) and names the floor and the
  counter-argument. Nothing here anticipates its answer. **When it lands, §1's plan-margin paragraph and the
  new §5 bullet both have to be re-read** — that is a queue item and it is the first thing the next
  synthesis should check.
- **`CH-0084`'s flatness half is owed a re-run, and the deliverable now says which half.** The **16 %
  mandate shortfall** follows from the mean alone and is unqualified; the **flatness fail is indicative**,
  because a Bernoulli dropout and `C-0060`'s alternating scatter share a standard deviation and not a
  spatial structure. Re-running `C-0060`'s pipeline under a *position-dependent* dropout is a task and it is
  not this one. **Raised as `T-148`.**
- **`CH-0083`'s fold has never been searched.** Running `C-0018`'s pull-in machinery on `C-0071`'s
  recommended strain-stiffening law is the only thing that closes Task 4 *for the device the programme
  recommends*. **Raised as `T-149`.**
- **Three tool observations are reported rather than fixed**, because a sibling agent was in `tools/`:
  the summary comment is printed without a leading newline and concatenates onto the last record; `IN
  PROGRESS` correctly contradicts an *open* assertion and fired on a first draft here; and **neither half
  can see a file that contradicts itself**, which is Part 3's finding and would be a genuinely new check —
  cheap, because the deliverable is one file.
