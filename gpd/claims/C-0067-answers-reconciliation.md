# C-0067 — `ANSWERS.md` reconciled statement by statement for the first time: of **84** adjudicated statements **63 trace, 16 had drifted and 2 could not be traced to any claim at all** — and the drift is **not arithmetic**, it is a deliverable that kept saying *"this is a specification gap"* about three questions the programme had already answered, and *"irreducible"* about a dishing two later claims remove

| | |
|---|---|
| **Task** | [`T-131`](../tasks/T-131-answers-reconciliation.md) |
| **Leaf** | — (a process claim; it audits the file that reports every leaf) |
| **Verification type** | **logical** (statement-by-statement adjudication against each owning claim's current status, the status read from the challenge that stands against it) **+ in-silico** (a numeric tracer over all 60 claims and 69 challenges, [`tools/trace-answers.py`](../../tools/trace-answers.py), with **22** executable checks in [`tools/test-trace-answers.py`](../../tools/test-trace-answers.py)) |
| **Verdict** | **PASS on `P1`–`P5`, and the finding is the SHAPE of the drift rather than its size.** Of **84** adjudicated statements, **63 trace** to a claim that still asserts them, **16 had drifted** and are corrected in place, **2 cannot be traced to any claim as stated**, and **4 completeness gaps** are closed. **The arithmetic is almost perfectly sound**: of **415** numeric tokens in the file as received, **414** appear in some claim or challenge and exactly **one** does not — `42.4`, a rounding of `C-0032`'s `42.38`. **What had drifted is the status of answers, not the value of numbers.** Three entries of the *"What we cannot answer"* list had been **answered** — `T-60` by `C-0033` (iteration 5), `T-75`/`T-78` by `C-0035` (iteration 5), `T-76` by `C-0032` and `C-0049` (iterations 5 and 7) — and had sat there for up to seven iterations, so the primary deliverable was **under-claiming its own programme**. The most consequential single correction runs the other way: §1's *"they differ by 32 % of the stroke, and that part is **irreducible** — forced by the tile's own electrostatic edge, which no coupling choice can remove"* is **false**, because `C-0058` reaches **0.0753** of the stroke by *distributing* the same 33.3333 pN/nm and `C-0063` reaches **0.0706** with **equal** springs by *placing* them, both inside `T-5b`'s 0.10 against **0.3079** with no coupling at all. **`ANSWERS.md`'s own header sentence — *"a synthesis, not a source"* — is now true of 82 of its 84 statements** rather than assumed of all of them. **No claim was found to be wrong**; every disagreement was `ANSWERS.md` misreporting a claim or failing to carry a later one, which is why this claim raises no challenge. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED**, and nothing here is even computed: this claim re-derives no physics. Its evidence is textual — the claim corpus — and its one executable artifact decides only whether a number appears in a file, never whether it is right. |
| **Provenance** | [`tools/trace-answers.py`](../../tools/trace-answers.py) over `ANSWERS.md`, `gpd/claims/` (60 files) and `gpd/challenges/` (69 files); **22 checks in `tools/test-trace-answers.py`, all passing**; the trace table itself is in [`T-131`](../tasks/T-131-answers-reconciliation.md), 84 rows. No Kotlin source is touched and no result file is written, so `tools/verify.sh` is reported as a **regression** check rather than as this claim's evidence: **`BUILD SUCCESSFUL in 11m`, 0 failures**, on an isolated snapshot with one concurrent agent's mid-TDD test file dropped by `--drop-file` (`src/test/kotlin/anchoring/CrossbarArrayPlacementTest.kt`, which fails `compileTestKotlin` on an unresolved reference). **The suite's test COUNT is not quoted, because `tools/verify.sh` deletes its snapshot on exit and takes `build/test-results` with it** — an earlier draft of this line carried a count and it was removed rather than guessed. |
| **Conditions** | The repository at iteration 12, branch `main`. Claim corpus `C-0001`–`C-0064`; challenge corpus `CH-0001`–`CH-0077`. Units as `ANSWERS.md` states them: T = 300 K, `k_BT = 4.142 pN·nm`, aqueous MgCl₂ at the stated concentration. |
| **Consumes** | every claim in `gpd/claims/` and every challenge in `gpd/challenges/`, read for status; **materially** [`C-0022`](C-0022-tile-edge-load-profile.md), [`C-0032`](C-0032-softening-coupling-stability.md), [`C-0033`](C-0033-collar-on-the-equilibrium-path.md), [`C-0035`](C-0035-flexure-mounting-sense.md), [`C-0049`](C-0049-compliance-ceiling-stroke.md), [`C-0051`](C-0051-second-window-resynthesis.md), [`C-0057`](C-0057-backbone-torsion-closure.md), [`C-0058`](C-0058-non-uniform-coupling.md), [`C-0061`](C-0061-stacked-arm-sheet.md), [`C-0063`](C-0063-upward-root-placement.md), [`C-0064`](C-0064-robust-distribution.md), [`CH-0034`](../challenges/CH-0034-flatness-count-saturates-under-the-solved-load.md), [`CH-0047`](../challenges/CH-0047-a-tangent-minimum-over-zero-stroke-is-not-a-requirement.md), [`CH-0059`](../challenges/CH-0059-the-desired-stroke-placement-is-below-the-stability-floor.md), [`CH-0063`](../challenges/CH-0063-the-collar-was-carried-onto-a-load-line-the-device-does-not-have.md), [`CH-0070`](../challenges/CH-0070-the-reported-optima-are-in-the-torsion-infeasible-set.md), [`CH-0071`](../challenges/CH-0071-the-saturation-floor-is-a-property-of-the-equal-spring-family.md), [`CH-0077`](../challenges/CH-0077-five-solved-states-are-four-devices.md) |
| **Raises** | **nothing.** Every disagreement found was `ANSWERS.md` against a claim, and the claim won every time. That is itself a result and is reported as `P6` below. |

---

## The claim, in one line

**A synthesis drifts by keeping its answers rather than by mis-copying its numbers: 414 of 415 numeric
tokens in the primary deliverable are in the claim corpus, and yet three questions it still calls
unanswerable were answered seven iterations ago, one property it calls irreducible is removed by two later
claims, and five of the six specification questions the programme is now waiting on were not asked at all.**

---

## Part 1 — the cheap half, which ran first and bounded the problem

The tool tests every numeric token of `ANSWERS.md` against every claim and challenge. It cannot decide
whether a number *means* what the passage says; it decides only whether the number is **in the corpus** —
which is the half that costs nothing and settles the worst failure mode outright.

| | on the file as received | after the corrections |
|---|---|---|
| numeric tokens | **415** | **555** |
| appear in a claim or challenge the same block **cites** | 305 | 440 |
| appear in a claim or challenge, but not one the block cites | 109 | 115 |
| **appear in NO claim or challenge at all** | **1** | **0** |

> The one that was absent is now present **in this claim**, which quotes it as the prior wording it is —
> so the corpus contains it exactly once, in the record of its own correction. That is the intended state
> and it is stated rather than left for the next agent to rediscover as a curiosity.

The one absent token is **`42.4`**, in §1's *"the adverse mounting is 42.4–61.0 pN/nm"*. `C-0032` says
**42.38–61.04**. It is a rounding and it moves nothing — and it is the only place in a 517-line synthesis
where a number exists that the repository does not.

**One structural defect was found and repaired in the same pass**: §3's row **(f)** was written across six
physical lines, and a Markdown table row must be one line, so the whole *Structural survival* answer — the
longest in the file — **was not rendering as a table cell at all**. Joined. This is the one place the
semantic-line-break convention does not apply, and it is worth knowing before the next agent reflows it back.

> **The 109 "elsewhere" tokens are a property of the tool's block granularity, not of the file.**
> A paragraph that names no claim inline has no citations to match against, so every number in it lands
> there. They are adjudicated by hand in Part 2 like everything else; the tool's contribution is that it
> guarantees none of them was adjudicated by *accident*.

### The tool is tested before it is trusted

`tools/test-trace-answers.py`, **22 checks**, covering both failure directions — a number present only in
an *uncited* claim must not read as CITED, a number in *no* claim must read as ABSENT — plus the
normalisation (en dash, U+2212, `×`), the identifier stripping (`C-0051`, `CH-0077`, `T-131`, `A2.1`, `§4`
are not quantities), and the substring guard (`45` must not match inside `1.45` or `450`). A false ABSENT
would send an agent to "correct" a sound number; a false CITED would let a drifted one through. Both are
silent failures, which is what makes them worth a test.

---

## Part 2 — the expensive half: 84 statements adjudicated

The full trace table is in [`T-131`](../tasks/T-131-answers-reconciliation.md). The tally:

| | count | |
|---|---|---|
| **TRACED** — the owning claim still asserts it, in the sense the passage uses | **63** | 75 % |
| **DRIFTED** — corrected in place, prior wording preserved | **16** | 19 % |
| **UNTRACEABLE** — no claim states the number or the range as such | **2** | 2 % |
| **completeness gaps** — closed | **4** | 5 % |

(One statement is both drifted and untraceable — the `42.4` — so the columns sum to 85 over 84 rows.)

### The two untraceable statements, listed rather than deleted

| | statement | what the corpus actually has | disposition |
|---|---|---|---|
| **U1** | §1: the adverse mounting is *"**42.4–61.0** pN/nm"* | `C-0032`: **42.38–61.04** | corrected to the claim's own figure; the prior wording kept in the file |
| **U2** | §2 and §3(f): the peak per-load-path force inside the window is *"**3.9–8.9 pN**"* | `C-0016`'s table, four cells: 7 nm **3.90–4.14** best and **6.58–6.90** worst; 10 nm **4.04–5.67** best and **6.77–8.90** worst. **No claim states `3.9–8.9`** | **kept**, with the owner and the construction named in the file. It is a legitimate min/max over one claim's own table — but the file is a synthesis and this is a place where it is a *source*, and it now says so |

**Neither is an error and both are worth having found.** `U2` is the more interesting: it is the shape of
number a synthesis manufactures without noticing — a true statement that no single claim makes, and that
therefore no `grep` of a claim file can confirm.

---

## Part 3 — the sixteen drifted statements, by kind

Grouping them is what shows the mechanism.

### Kind A — a question the programme has since ANSWERED, still listed as unanswerable (3 statements, and this is the worst kind)

| | `ANSWERS.md` said | answered by | when | how long it sat |
|---|---|---|---|---|
| **A1** | *"Whether `C-0018`'s pull-in bias itself moves … It is the only unresolved margin left that a cheap calculation can close"* | `C-0033` (`T-60`): `d ln μ/dh` = **0.01763–0.02011 nm⁻¹**, collar-only tangent **+2.60 to +4.99 pN/nm** strictly positive, margin **rises** to 1.021–1.028 at 10 nm / 2 mM and **falls** 0.9–3.5 % at 7 nm / 10 mM; `C-0051` then composes it at **−8.40 to −11.06 pN/nm** | iteration 5 | **7 iterations** |
| **A2** | *"Which body carries the standoffs, and what sits under the flexure's midspan … A specification gap, not a modelling one"* | `C-0035` (`T-75`, `T-78`): the sign is a product of **two** binaries so *"which body"* decides nothing alone; **exactly one of four mountings is buildable**; the body under the midspan is that mounting's own ground **by construction** | iteration 5 | **7 iterations** |
| **A3** | *"Whether a strain-softening coupling still satisfies the stability clause"* | `C-0032` (`T-76`) — **NO at 2 mM, YES at 0.5 mM** — and `C-0049` (`T-107`) on the range convention | iterations 5 and 7 | **5–7 iterations** |

> **A deliverable that under-claims is as wrong as one that over-claims, and it is harder to catch**,
> because every reviewer's instinct is to check the assertions and not the disclaimers. Three of the ten
> bullets in *"What we cannot answer, and why"* were answers.

### Kind B — a later claim supersedes the statement (7 statements)

| | `ANSWERS.md` said | corrected to | owner |
|---|---|---|---|
| **B1** | the 32 % lever/sensor split is **irreducible**, *"no coupling choice can remove it"* | 32 % is what survives a **uniform** coupling; a distributed one reaches **0.0753** and a *placed* equal-spring one **0.0706**, against **0.3079** with no coupling | `C-0058`, `C-0063`, `CH-0071` |
| **B2** | *"its sign is decided by which body carries the standoffs … §3 does not say which"* | a product of **two** binaries; unique buildable survivor `Su` | `C-0035` |
| **B3** | *"`T-67` found that the 90° routing **does** exist … the optimum is a scaffold excursion"* | the existence survives, **the routing does not** — 0 of 4, 1 of 8, 2 of 6 links close at torsion level, four excluded by a reach bound at **no torsion whatever** | `C-0057`, `CH-0070` |
| **B4** | *"**Seven of the eleven** axes"* | **ten of twelve** | `C-0051` |
| **B5** | *"`P2` closed by `C-0017`"* | closed for the **affine mandate**, **fails** for the realised coupling | `C-0032`, `C-0051` |
| **B6** | *"45 … are **needed for flatness**"* | 45 is where further attachments **stop buying** flatness | `CH-0034` |
| **B7** | *"`T-1f` … is now the binding uncertainty"* | `T-1f` bounds the **polymer** expansion at ≤ 9.4 %; the 123–214 % is the **electrostatic** one, and only `T-50` reaches it | `C-0019`, `CH-0019` |

### Kind C — the statement rests on something since withdrawn (2 statements)

| | | |
|---|---|---|
| **C1** | *"past the **40 pN/nm** compliance ceiling at 0 of 8 lengths"* | `C-0049` **withdrew** that ceiling — it is exactly `1.2 ×` a *placement* mandate. The conclusion survives on `C-0035`'s ground instead (both adverse mountings cannot place §3's effort point), so **no verdict moves and the reason does** |
| **C2** | the tangent minimum **22.88 pN/nm at 4.55 nm**, *"inside the operating range"* | `C-0049`/`CH-0047` settle the range as `[0, s*]`, and 4.55 nm is a stroke the **placed device never occupies**; read at the placement stroke the same flexure is **25.227 pN/nm** and clears **4 of `C-0017`'s 6** floors |

### Kind D — the statement is simply stale (3 statements)

| | | |
|---|---|---|
| **D1** | *"**Twenty-nine** challenges"* → **69**, against 60 claims |
| **D2** | *"**Two** paywalled papers"* → **one**. Lee et al. was obtained free from NIST's public repository, both Unpaywall and OpenAlex having reported it `closed` |
| **D3** | `C-0017`'s `K2` described as the coupling that *"supplies it"* → it is **one-sided**, which `C-0023` had to replace, and `C-0041` places only **15** of its 45 |

### Kind E — an owner named wrongly by proximity (1 statement)

**E1** — Task 1's *"At the working point, **47.7–64.1 pN/nm**"* sits under a table row citing
`C-0001` → `C-0003` → `C-0011`, and the number is **`C-0010`'s** (its foundation tangent at 100 pN).
Nothing is wrong with the number. What is wrong is that a reader following the row would not find it.

---

## Part 4 — the four completeness gaps

| | gap | closed how |
|---|---|---|
| **G1** | The deliverable is *"a height plus five specification questions"* and **the five were named nowhere in the file.** | A table of **six** — `T-63` (buffer), `P-13` (electrode), `T-95` (superstructure), `T-102` (tile area), `T-112` (which device the desired clause names), `T-115` (a 17–26 nm layer) — each stated as a **question with its threshold**, so one sentence from NDI settles it. `C-0051` names five; `P-13` is the sixth and was the **only** one `ANSWERS.md` already asked |
| **G2** | `C-0051` re-synthesises against `C-0031`–`C-0050`; **iterations 8–11 (`C-0052`–`C-0064`, `CH-0065`–`CH-0077`) are unsynthesised**, and the file did not say so | stated in §1 as a coverage sentence, with the expectation (nothing in them is a function of `σ` on inspection) marked as an expectation and not a re-run |
| **G3** | §3(g) stopped at iteration 10 | `C-0063` (0.0706 by placement with equal springs; `C-0058`'s rim rule **reverses sign** there) and `C-0064`/`CH-0077` (no distribution is flat at all five solved states — 0.1247 — **but the five states are four devices**, and over each device's own traversed range the minimax is **0.0372 / 0.0436 / 0.0619 / 0.0500**, all inside 0.10) added |
| **G4** | Task 7 quoted `C-0004`'s 22× with no later charge against it | `C-0061`'s **22.81× → 20.73×** added — the upward arm array adds **9.1 %** of the total drag and the corner falls 91.2 → **82.9 kHz**, still discharged |

---

## Part 5 — the gates

| gate | how it applies to a reconciliation | result |
|---|---|---|
| **1 — dimensional consistency** | nothing is computed; every quantity is transferred with the unit its claim states, and the tracer strips leaf IDs, section numbers and dates so none is read as a quantity | asserted in the tool's tests |
| **2 — limiting cases** | both failure directions of the tracer: a number only in an *uncited* claim must read ELSEWHERE and never CITED; a number in *no* claim must read ABSENT | asserted, 4 checks |
| **3 — symmetry and conservation** | the conservation law of an audit is that **no statement is deleted**. Every drifted row is corrected in place with its prior wording retained | 16 of 16 |
| **4 — numerical convergence** | the substring guard is the convergence question: does `45` match inside `1.45` or `450`? | asserted in both directions |
| **5 — literature cross-check** | the "literature" of a reconciliation is the claim corpus, and `SESSION-PROMPT.md` step 9 is the rule: every number written was grepped out of the claim that owns it | 84 of 84 |

### `P6` — the result that is a null, and it is worth stating

**No claim was found to be wrong.** Every one of the 16 disagreements was `ANSWERS.md` misreporting a claim
or failing to carry a later one; in no case did the claim corpus contradict itself in a way the synthesis
had faithfully transmitted. **This claim therefore raises no challenge**, which was not the expected
outcome — a 517-line synthesis assembled by several agents over eleven iterations was expected to have
propagated at least one bad number *from* a claim. It had not. The corpus is in better condition than the
file that summarises it.

One near-miss is recorded rather than challenged: **`C-0027` states the 5 nm crossing two ways** — *"the
crossing widens from `C-0016`'s 13.32× to 24.80×"* in its body and *"`C-0016`'s 13.3× crossing stands"* in
its verdict row. `ANSWERS.md` quotes 13.3×, which the verdict row endorses. It is an internal ambiguity in
`C-0027`, it moves no verdict (5 nm is empty on either reading), and it is left as a note rather than a
challenge because the two readings answer two different questions — the crossing with and without the
descent term `CH-0036` has since reduced to 0.07–0.38 nm.

---

## Validity range

- **This is a snapshot at iteration 12.** A reconciliation is stale the moment the next claim lands;
  what it establishes is that the file was consistent with the corpus **on this date**, and the tool is
  retained so the next pass costs one command rather than a day.
- **The tracer decides presence, never correctness.** A number that appears in a claim's *"what this is
  not"* section, or in a superseded banner, reads as CITED. Only the hand adjudication distinguishes them,
  and it is the hand adjudication that found all 16 drifts.
- **Statements without numbers are outside the token half entirely** and were adjudicated by reading only.
  A purely qualitative claim that has quietly been reversed would be caught only by the hand pass, and the
  hand pass is one agent's reading.
- **The trace is at statement granularity, not at word granularity.** A statement counted TRACED may carry
  a subordinate clause that is stale; the 84 rows name what was checked.

## Still open

1. **The window has not been re-synthesised against iterations 8–11** (`G2`). Recorded in `ANSWERS.md`;
   it is a task, not an edit.
2. **`T-129` decides whether `C-0063`'s flat placement is flat over a range**, and §3(g) now says so.
   Until it lands, the flat Gen-1 tile is a **single-state** result.
3. **Seven claims are still cited nowhere in `ANSWERS.md`** — `C-0036`, `C-0037`, `C-0047`, `C-0048`,
   `C-0053`, `C-0054`, `C-0056` — deliberately, as branch interior. Listed in `T-131` so the omission is a
   decision.
4. **`TASKS.md`'s open question 1 still says "Two paywalled PDFs"** in its heading while striking one of
   the two in its own list. `ANSWERS.md` is corrected; `TASKS.md`'s heading is another agent's section this
   iteration and is left alone.
