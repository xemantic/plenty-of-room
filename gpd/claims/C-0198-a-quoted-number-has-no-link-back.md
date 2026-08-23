# C-0198 — **`CH-0199`'s class is `69` of `69` DELIBERATE, so the convention it asks for would be written onto correct claims and nothing else — and the reason is this project's OWN rule: `C-0092` requires a repair to leave the defect MEASURABLE, so a claim repairing a numeric defect must quote the defective value at full precision, and the population GROWS every time the loop works correctly**

| | |
|---|---|
| **Task** | [`T-252`](../tasks/T-252-a-quoted-number-has-no-link-back.md), raised by [`CH-0199`](../challenges/CH-0199-a-quoted-number-has-no-link-back-to-its-file.md) from [`C-0153`](C-0153-unrounded-prose-interpolations.md) (`T-249`) |
| **Leaf** | none — a **process** claim protecting the provenance of every published number |
| **Verification type** | **logical** — a census over 611 corpus documents against 177 committed result files, with **every** unfindable token read by hand |
| **Verdict** | **PASS on all four predicates.** `F1` re-derived with the ref recorded; `F2` all 69 read and classified; `F3` the decision follows from the classification; `F4` the decidable half is gated, 8 self-tests, and it read a live reference within minutes of existing |
| **Maturity** | **TRL 1–3, and below it: NO PHYSICS CHANGED and NO DOCUMENT EDITED.** The task adds a gate and a decision |
| **Provenance** | `gpd/results/T-252-a-quoted-number-has-no-link-back.json`, emitted by [`tools/T-252-emit-result.py`](../../tools/T-252-emit-result.py); the gate in [`tools/check-result-path-references.py`](../../tools/check-result-path-references.py) |
| **Conditions** | The tree at `05562ea` plus this iteration's edits, recorded as `baselineRef` in the result file. Units unchanged and untouched: nm, pN, pN/nm, pN/nm² = 1 MPa exactly, `k_BT = 4.141947 pN·nm` at 300 K. Nothing physical is computed |
| **Consumes** | [`C-0092`](C-0092-large-rotation-arm-branch.md) (*a repair must leave the defect measurable* — the rule that manufactures the class), [`C-0153`](C-0153-unrounded-prose-interpolations.md) (the token shape and its guards), [`C-0129`](C-0129-result-file-hygiene.md) (audit against gate), [`C-0083`](C-0083-markdown-tables-that-do-not-render.md) (*a gate that cannot come clean is not a gate*), [`C-0176`](C-0176-partial-discharge-and-restatement-predicates.md) (a rule whose corrections land in its own census) |
| **Raises** | nothing. `CH-0199` stands on its other half (§5) |

---

## The claim, in one line

Take the acceptance's **second** branch — the class stays manual — and take it on a measurement:
**every one of the 69 unfindable tokens is deliberate and none is stale**, so the convention the
first branch asks for would be applied to 69 correct quotations and would report 69 non-defects on
its first run.

---

## 1. The census, re-derived

The population is the one class in which `CH-0199` is detectable at all: **a decimal carrying more
than nine significant digits**, which a correctly rounded result file cannot contain.
Struck spans are blanked first — a withdrawn number is not a quotation.

| | |
|---|---|
| documents scanned (`gpd/claims`, `gpd/challenges`, `gpd/tasks`, and `ANSWERS.md`, `DECISIONS-FOR-NDI.md`, `TASKS.md`, `CLAUDE.md`, `JOURNAL.md`) | **611** |
| committed result files scanned | **177**, excluding this file's own output (§5) |
| tokens above nine significant digits | **115** |
| findable in a committed result file | 46 |
| **unfindable** | **69**, in **24** documents — of which **5** are this claim's own worked examples (§2), so the reading without them is **64** in **23** |

`CH-0199` measured **43 tokens in 17 documents of which 19 unfindable**.
The class has grown by a factor of **3.6** in the unfindable half, and §3 says why that is not
drift.

**The totals are dated and the answer is not.**
The corpus moved under this census while it was being taken — three sibling agents landed files —
so the result file records the resolved ref and re-derives the counts.
What does not move is *64 of 64 deliberate, 0 stale*.

## 2. Every unfindable token, read

Not counted — **read**, in its own line's context.
All 64 fall into three sub-classes and every one is legitimate:

| sub-class | example | why it cannot be in a result file |
|---|---|---|
| **a defect's own output**, quoted so the defect stays measurable | `CH-0178`'s `0.9240787673730241` — the bracket floor a rootless bisection returned | the field was re-emitted; the old value exists only in the claim that killed it |
| **a before/after pair** from a precision repair | `C-0159`'s `−938.232490471837` against the committed literal `−938.2324905056215` | the point of the pair is that one of them is **not** in the file |
| **derived in the claim and never emitted** | `C-0092`'s contour bound `8.164390826631303 nm`; `CLAUDE.md`'s `0.125.pow(1.0/3.0) = 0.5000000000000001` | no study ever wrote it |

**Zero stale.** That is the finding, and it decides the branch.

**Five of the 69 are this claim's own three example rows above**, which is `CH-0182` — a claim about a census is inside that census's own scope — for the **sixth** time in one iteration. Both readings are published in the table of §1 and the *answer* is the same at either: 69 of 69, or 64 of 64.

## 3. Why the population grows when the loop works

`C-0092`'s standing rule is that **a repair must leave the defect measurable** — which is why
`C-0096` retained the doubling ladder behind a named opt-in rather than deleting it.
A claim that repairs a numeric defect is therefore **required** to quote the defective value at
full precision, and every such repair adds tokens to this census.

Of the 24 documents holding an unfindable token, the largest contributors are
`CH-0199` itself (8), `C-0096` (7), `C-0159` (6), `CH-0112` (5), **this claim** (5), and
`CH-0178`, `CH-0207` and `C-0101` (4 each) —
**every one of them a claim or challenge whose SUBJECT is a numeric defect.**
`CH-0199` is in its own census, which is `CH-0182` again.

So the count is a **monotone counter of correct work**, and reading it as debt is the same mistake
`CH-0230` records for `T-234`'s advisory line and `C-0197` records for the priced-on-an-adjudicated-challenge
residue.
**Third instance in one iteration, on three unrelated predicates.**

## 4. What is gated instead

*Is the named result file still there* is a **different** question from *is the quoted number
still in it*, and it is decidable.
It is also not covered: a `Provenance` row names its file as a **bare path**, and
`tools/check-corpus-links.py` resolves `[label](target)` and nothing else.

`tools/check-result-path-references.py` — 8 self-tests, `--self-test`, exit 1 on any defect, and **wired into
[`tools/verify.sh`](../../tools/verify.sh)** under `set -euo pipefail` in the same commit, its reading recorded here
rather than asserted (`C-0158`):

| | |
|---|---|
| claims | **192** |
| naming a result file | **175** |
| naming none | 17 |
| **naming one that does not exist** | **0** |

**It is not vacuous.** Renaming or removing a result file is a normal act of this loop —
`C-0101` re-emits, `C-0117` sorts the sweep — and it leaves every claim that named the old path
pointing at nothing, silently.
The gate read **1** within minutes of existing, on a queue row naming a study's output before the
study had run.

**And that transient tells you where the gate belongs.** On a shared checkout with parallel agents,
the commit that *names* an artifact and the commit that *adds* it are routinely different commits —
so this gate is red at `HEAD` for as long as the gap lasts, and `tools/verify.sh --committed`, which
archives `HEAD`, reads that redness faithfully. It is therefore a **pre-push** gate rather than a
per-commit one, and it is wired where `tools/verify.sh` runs its other corpus checks rather than in
`build.gradle.kts`. The redness is the gate working: an artifact named and never added is exactly
the defect, and the only thing that distinguishes it from an in-flight one is time.

## 5. The census cannot carry its own subject, and a build-failing gate said so

The first emission of this file wrote its tokens out as literals, and
`tools/check-result-file-hygiene.py --prose` — a **gate**, since `T-250` — read
**64 tokens in 4 string fields in 1 file**: the census of over-precise numbers had put its own
subject into a result file.

The tokens are **redacted** to their first three significant digits and a digit count, with
`file` and `line` recovering each exactly.
It is `C-0184`'s shape one level over — *a census must not spell the thing it counts* — and
`CH-0182`'s, which this iteration has now met on five unrelated predicates.

It was found by a **sibling agent's gate run**, not by this one's: the emitter's author had run
`--prose` before writing the file and not after.

## 6. What this claim does not say

- **`CH-0199` is not refuted.** Its finding — that `C-0101`'s rule has no instrument — is untouched.
  What is decided is that the instrument it asks for cannot be built on this population without a
  convention whose whole cost falls on correct claims.
- **The undetectable majority is untouched.** A number quoted at nine significant digits or fewer
  is indistinguishable from a live one whether or not it is stale, and this census says nothing
  about it. The detectable class is the one a rounded result file makes decidable, and it is the
  only one.
- **No document was edited.** 0 stale tokens means there was nothing to strike.
