# C-0088 — The deliverable contradicted itself about **two** tasks, not the one `C-0080` found by hand, and the second was created by a fix filed **ninety minutes earlier in this same iteration**. The third check is mechanised and unconditional; the fourth drift class `C-0080` names is **not mechanisable without a corpus change**, and that is the answer rather than a deferral

| | |
|---|---|
| **Task** | [`T-150`](../../TASKS.md) — raised by [`C-0080`](C-0080-third-answers-synthesis.md), taken by the coordinator in iteration 17 while `T-149`, `T-152`/`T-151` and `T-148` ran in parallel |
| **Verification type** | **logical** (a mechanical check of the deliverable against itself, with the checker verified by 57 in-memory fixtures — up from 43) |
| **Verdict** | **PASS on the half that can be mechanised, and a reasoned NO on the half that cannot.** The self-consistency check finds **two** tasks the deliverable gave contradictory statuses: `T-45`, which `C-0080` had found by hand, and **`T-95`, which did not exist when `C-0080` ran** — it was created by `P-24`'s own discharge fix earlier in this iteration, which updated one of two mentions. Both fixed; the deliverable now reports **0 ABSENT of 727, 0 stale, 0 self-contradictions**. The **fourth** class — a superseded number that still reads `CITED` — is **not mechanisable against the corpus as it stands**, because the claims carry no `superseded-by` edge, and the reason that is a finding rather than an excuse is given in Part 4. |
| **Maturity** | **TRL 1–3**, and below it: nothing here is physics. **No number, verdict or premise of any claim is touched.** |
| **Provenance** | [`tools/trace-answers.py`](../../tools/trace-answers.py) (`status_words`, `self_contradictions`, and a third unconditional report in `main`); **57 checks** in [`tools/test-trace-answers.py`](../../tools/test-trace-answers.py), written test-first; two corrections to [`ANSWERS.md`](../../ANSWERS.md) |
| **Conditions** | The corpus at commit `4c9c3ee`: 83 claims, 94 challenges. No Kotlin, no solver, no units. |
| **Consumes** | [`C-0080`](C-0080-third-answers-synthesis.md) (the finding and the blind spot), [`C-0078`](C-0078-status-drift-in-the-deliverable.md) and [`C-0067`](C-0067-answers-reconciliation.md) (the first two checks and the drift taxonomy), [`C-0071`](C-0071-output-element-recommendation.md) (the discharge that `P-24` recorded and this claim completes) |
| **Constrains** | `ANSWERS.md`. **No claim is contradicted.** |

---

## 1. The blind spot, stated exactly

There are now three checks over the primary deliverable and they differ in **what they compare it
to**:

| check | compares against | catches | first found by |
|---|---|---|---|
| numeric provenance | the **corpus** | a number in no claim | `C-0067` |
| status drift | the **queue** | a task asserted open that `TASKS.md` closed | `C-0078` |
| **self-consistency** | **itself** | a task the document gives two statuses | `C-0080`, by hand |

`C-0080` put the gap precisely and this claim is only its mechanisation:

> A tool that checks a document against a corpus cannot see a document that disagrees with itself.

The live instance was `T-45`, called *"answered from published measurement — and the answer is a
failure"* in §1 and *"(`T-45` is still unmeasured)"* in §3. **Both existing halves pass it**: §1's
sentence has an owner and reads `CITED`, and §3's parenthesis carries no number at all and no
"open" for the status half to catch.

## 2. What the check does, and what it deliberately does not

Per task ID, per **sentence**: collect every status verdict the deliverable asserts — `SETTLED`,
`OPEN` or `DISCHARGED` — and report any task carrying more than one.

The sentence, not the block, is the unit. A block long enough to hold both verdicts about
*different* tasks is common in this deliverable and is not a contradiction.

Evidence is **one-sided on purpose**: only an explicit status word counts, so a task that is merely
cited stays silent. A false positive would send an agent to reconcile two sentences that are both
correct — the failure a drift checker can least afford, since the tool exists in order to be
believed. Four guards, each a test:

| guard | why |
|---|---|
| `not answered`, `cannot be answered` → **OPEN** | the negation is the whole meaning, and it is exactly the phrasing `C-0071` uses for a discharged question |
| `open since\|for\|from` → **not OPEN** | a duration, and the same sentence usually closes it. Without this, `"T-45, open since iteration 3, is answered"` contradicts *itself*, and every genuine contradiction it takes part in becomes unreadable |
| `DISCHARGED` → its own verdict | a question that stopped applying is neither answered nor owed an answer; collapsing it into either loses the distinction `C-0071` had to invent |
| a sentence with no status word → silent | the default is *no assertion*, not *open* |

## 3. What it found — and the second one is the interesting one

**`T-45`** — the instance `C-0080` reported. Line 51 settled, line 600 open. Corrected: the §4(g)
parenthesis now says the tolerance *is* answered from published measurement, at 43.6 % relative
scatter, past its own threshold.

**`T-95` — which did not exist when `C-0080` ran.** It was created **in this iteration, about ninety
minutes before the check was written**, by `P-24`'s own fix: `C-0071` discharged `T-95` in iteration
14, `TASKS.md` had never recorded it, and the repair updated `ANSWERS.md` line 245 to say
`DISCHARGED` — and **missed a second mention at line 686** still calling it *"open question 6
below"*.

That is worth more than the fix. A hand-audit of a 700-line deliverable finds the contradictions
that exist **when it runs**; the very next edit can make a new one, and it is likeliest to do so
precisely when the edit is *a status change*, which is the class most likely to appear in more than
one place. **A one-off reconciliation cannot converge on this class and a standing check can**, and
the evidence is that the mechanisation caught its own author's incomplete repair within the hour.

> This is the third time in three iterations that a check found the mistake of the person who wrote
> it — `C-0078` on a headline list, `C-0083` on two `TASKS.md` rows the coordinator had just added,
> and now `C-0088` on `P-24`'s half-finished discharge.

## 4. The fourth class is NOT mechanisable, and that is the answer

`C-0080`'s third drift class is **a number still in the corpus, under a verdict its owning claim
still states, that a LATER claim superseded**. It reads `CITED` **precisely because it has an
owner**, so no comparison against the corpus can distinguish it from a correct citation.

Mechanising it needs the corpus to record **which claim supersedes which finding of which other
claim**, and it does not. What the claims carry is a `Consumes` row and a prose narrative; a
challenge carries an `Against` row, which is the nearest thing — but a challenge names the *claim*,
not the *number*, and `C-0080`'s five instances are all cases where the claim stands and one figure
inside it was overtaken.

Three things follow, and they are the deliverable of this half:

1. **A `superseded-by` edge would have to be written by the claim that does the superseding**, at
   the granularity of the superseded *statement*. That is a corpus convention change, not a tool,
   and it costs every future claim a row.
2. **The nearest cheap approximation is already available and is worth stating**: for any number in
   the deliverable, the *latest* claim that contains it is not necessarily the one cited. A report
   of *"cited `C-0069`, also appears in `C-0074` which is newer"* would have flagged three of
   `C-0080`'s five, and costs nothing but a date ordering. It is **not** implemented here because
   its false-positive rate is unknown and untested, and an unmeasured false-positive rate is exactly
   what guard 2 above exists to prevent.
3. **So the honest status is: the class is real, it is named, one approximation is identified and
   priced, and the exact check needs a corpus change nobody has agreed to.** `T-150`'s acceptance
   admits *"or the statement that the class cannot be mechanised"*, and this is that statement, with
   what it would take.

## 5. Result

| | before | after |
|---|---|---|
| numeric tokens ABSENT | 0 of 727 | 0 of 727 |
| open assertions the queue contradicts | 0 | 0 |
| **tasks the deliverable contradicts itself about** | **2** | **0** |
| checks in `tools/test-trace-answers.py` | 43 | **57** |

All three checks run **unconditionally** in `main`, for the reason `C-0078` gave and this claim
re-earns: *a check nobody remembers to ask for is not a check.*
