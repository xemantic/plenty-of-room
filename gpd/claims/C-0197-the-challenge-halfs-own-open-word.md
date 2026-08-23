# C-0197 — **the arm `T-261` asks for already existed and was blind to ONE WORD: `stale_challenge_statuses` inherited a TASK's status vocabulary, and a challenge's own open state is `RAISED`, which is in neither list — so four passages of the two deliverables called an UPHELD challenge *raised* while a WIRED gate reported `0 open assertion(s) contradicted`**

| | |
|---|---|
| **Task** | [`T-261`](../tasks/T-261-a-price-on-an-adjudicated-challenge.md), raised by [`CH-0203`](../challenges/CH-0203-a-specification-question-was-posed-on-a-threshold-its-own-iteration-withdrew.md) / [`C-0155`](C-0155-tenth-answers-synthesis.md) (`T-257`) |
| **Leaf** | none — a **process** claim protecting the two customer-facing documents |
| **Verification type** | **logical** — a predicate over the two deliverables and the challenge corpus, with the false-positive rate measured **by hand over every hit** |
| **Verdict** | **PASS on all four predicates.** `F1` the arm exists and gates, with 19 named tests — 6 on the gated half and 13 on the two audit arms; `F2` **0 false positives of 4** on the gated half, **1 of 17** on the ungated residue, both by hand; `F3` the tracer exits **0** on both deliverables and all four defects are **repaired, struck not deleted**; `F4` the cause is stated and tested |
| **Maturity** | **TRL 1–3, and below it: NO PHYSICS CHANGED.** Every edit is a status word or a pointer |
| **Provenance** | `gpd/results/T-261-a-price-on-an-adjudicated-challenge.json`, emitted by [`tools/T-261-emit-result.py`](../../tools/T-261-emit-result.py) with the **before** reading executed out of `git show <ref>:`; the predicate in [`tools/trace-answers.py`](../../tools/trace-answers.py), its tests in [`tools/test-trace-answers.py`](../../tools/test-trace-answers.py) |
| **Conditions** | The tree at `2ce8ca2` plus this iteration's edits, recorded as `baselineRef` in the result file. Units unchanged and untouched: nm, pN, pN/nm, pN/nm² = 1 MPa exactly, `k_BT = 4.141947 pN·nm` at 300 K, aqueous buffer with stated Mg²⁺. Nothing physical is computed |
| **Consumes** | [`C-0088`](C-0088-does-the-deliverable-agree-with-itself.md) (the task half, and its two guards), [`C-0080`](C-0080-third-answers-synthesis.md) (*a drift checker's false positives cost more than its true ones*), [`C-0129`](C-0129-result-file-hygiene.md) (gate what can come clean, print the residue beside it), [`C-0115`](C-0115-fifth-answers-synthesis.md) (*a check's verdict window is part of its logic, and a sentence can be wrong rather than the checker*), [`C-0071`](C-0071-output-element-recommendation.md) (strike, never delete), [`C-0177`](C-0177-queue-status-vocabulary.md) (a new status word must be tested in **both** senses the day it is coined) |
| **Raises** | nothing against a standing claim. Two residues are printed and **not** gated (§4, §5), and the second is queued as its own row |

---

## The claim, in one line

`T-261` asks for an arm.
The arm has existed since `T-183`; what it lacked was the **challenge** vocabulary's own open word.
`_OPEN_WORD_ASSERTION` carries `open`, `unmeasured`, `unanswered`, `unresolved`, `undetermined`,
`still to do`, `still missing`, `not yet answered`, `not determined`, `TODO` —
every one of them a **task**'s word — and a challenge's open state is **`RAISED`**.

---

## 1. What the corpus was carrying

`CH-0240` was **UPHELD** in iteration 45 by
[`C-0190`](C-0190-the-departure-is-common-mode-and-what-replaces-it.md), and its own `**Status**`
row says so.
Four passages of the two deliverables read `` (`CH-0240`, raised) ``:

| document | line | passage |
|---|---|---|
| `ANSWERS.md` | 350 | *"And the coordinate itself is now disputed (`CH-0240`, raised)"* |
| `ANSWERS.md` | 1018 | *"the departure's coefficient … is **exactly zero** (`CH-0240`, raised); the replacement term is a per-beam torsional eigenstrain **nothing has priced**"* |
| `ANSWERS.md` | 1477 | *"for a level displacement (`CH-0240`, raised), which is `T-291`"* |
| `DECISIONS-FOR-NDI.md` | 949 | *"the coordinate this load is applied through is itself disputed (`CH-0240`, raised)"* |

The second is stale twice over: `T-291`/`C-0190` **did** price the replacement, and the sentence
says nothing has.

Measured at the baseline ref, on the ref's **own** documents:
the committed predicate reads **0**, the repaired predicate reads **4**.
That is the blindness, isolated — same text, same corpus, one word of difference.

## 2. Why a separate pattern and not a widening

`raised by` is how this corpus states a challenge's **provenance**, and `TASKS.md` is full of it.
Widening the shared `_OPEN_WORD_ASSERTION` would put a provenance idiom into the **task** half and
manufacture false positives there — so the challenge half gets its own
`_CHALLENGE_OPEN_ASSERTION`, which is the shared pattern plus a guarded `raised`.

`T-183` already pinned exactly this separation, for `_OPEN_WORD_ASSERTION` against
`_OPEN_WORD_VERDICT`, after finding one silently shadowing the other.
This claim adds a **third** list to that family and a named test asserting it is a separate object.

The guard is the provenance idiom itself — `raised` not followed by
`by`, `in`, `as`, `at`, `and` or `against`.
**It removes none of the four**, and it is precautionary: it is what keeps the predicate honest as
the corpus grows, and it is held open by two named tests written from the corpus's own phrasing.

## 3. The repair, and the tracer's own reading

All four are struck in place with the correction beside them (`C-0071`), and the second's
substantive staleness is corrected too.
`tools/trace-answers.py` now exits **0** on both documents.

**The check caught the mistake of the person who wrote it**, for the fifth consecutive family:
`C-0080` found it by hand, `C-0088` mechanised the task half, `C-0177` found the queue's, and this
one fired on the deliverables the same coordinator was about to synthesise.

## 4. Residue one — the arm the acceptance actually names, refused as a gate

`T-261`'s acceptance is an arm flagging a passage that **cites a challenge as the source of a
number** where the challenge is adjudicated.
Measured before it was written:

| predicate | hits over the two documents |
|---|---|
| any number near any adjudicated challenge | **34** |
| …and no claim named inside a 200-character window | **6** |

Hand-read, the 34 are almost entirely **corrections**, and that is structural rather than fixable:
**a correcting sentence has to NAME the challenge in order to withdraw it**, so every repair lands
in the census the gate would fire on.
That is `CH-0230`'s mechanism, met on a different predicate — the same reason `T-234`'s advisory
debt line **rises** when the documents are corrected.

So it ships as a **residue line**: printed unconditionally, counted in no exit code, which is
`C-0129`'s policy.
Of the 6, the hand reading finds 3 corrections and 2 cross-references.

## 5. Residue two — the INPUT defect, and why nothing could have caught decision 8

`stale_challenge_statuses` reads a challenge's **own file** as the authority, which is `T-183`'s
deliberate choice.
Nothing requires a later claim to annotate it.

Measured: **17 challenges over 22 sites** are adjudicated by a claim, in the claim's own words,
while their own `**Status**` row does not say so — `CH-0004`, `CH-0010`, `CH-0033`, `CH-0056`,
`CH-0068`, `CH-0078`, `CH-0083`, `CH-0089`, `CH-0093`, `CH-0101`, `CH-0103`, `CH-0151`, `CH-0157`,
`CH-0177`, `CH-0184`, `CH-0185`, `CH-0229`.

**`CH-0185` is `T-261`'s own live instance.**
`C-0148` says, in as many words, *"**`CH-0185` is ANSWERED** — the twelfth column is a box
artefact"*, and the challenge file still reads **raised**.
So the gate reports it OPEN, and a deliverable may rest a price on it indefinitely.
**The defect behind decision 8 is in the AUTHORITY, not in the predicate**, and no arm added to
this tool can reach it while the annotation is missing.

**One false positive of 17**, named with its cause: `CH-0157` in `C-0132`, where
*"`CH-0157`, and it is why the bracket has to be withdrawn"* withdraws the **bracket** and not the
challenge — the clause guard `[^.;|]` does not stop a comma-and-conjunction.
It is reported rather than tuned away, because a guard narrowed to one observed case is a test
written to the shape of the change (`C-0176` §1b).

Closing this residue means editing 17 challenge files, each needing its adjudicating claim read.
That is its own delta and it is queued as one.

## 6. What this claim does not say

- **The deliverable-side gate is not delivered.** It is refused, with the measurement, and shipped
  as the audit the acceptance itself permits.
- **A price written in WORDS is still invisible.** Decision 8's own price was *"six flat cells of
  eight against three"*, and `tokens()` has `min_digits=2`, so `six`, `eight` and `three` carry no
  token at all — only the `0.07 nm` in the same sentence does. No arm added here changes that, and
  it is the reason a numeric tracer cannot be the whole answer to `CH-0203`.
- **`gpd/challenges/README.md` is still not read.** The acceptance names it as the authority;
  `T-183` chose the file instead, on the ground that the index was incomplete. It is complete now
  (215 of 215 indexed) and the two disagree on **6** challenges, which is recorded and not acted on.
