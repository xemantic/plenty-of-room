# T-298 — a challenge a claim has adjudicated whose own `**Status**` row does not say so

| | |
|---|---|
| **Raised by** | [`C-0197`](../claims/C-0197-the-challenge-halfs-own-open-word.md) (`T-261`) §5 |
| **Leaf** | none — a **process** task protecting the two customer-facing documents |
| **Verification type** | **logical** — a predicate over the challenge corpus and the claim corpus, with every hit read by hand and every narrowing's false negatives measured over the whole corpus before it is written |
| **Units** | none. Every value is an integer count, a challenge identifier, a claim filename or a verdict word. No physics is computed and no physical number moves |

---

## Formulate

`tools/trace-answers.py`'s `stale_challenge_statuses` gates a deliverable passage that calls a
challenge *open* when the corpus says it is closed.
Its authority for *"is this challenge closed"* is the challenge's **own file** — `T-183`'s
deliberate choice, because `gpd/challenges/README.md`'s Status cell is free prose.

**Nothing requires a later claim to annotate that row.**
So a challenge answered in a claim's own words, whose file still says *raised*, is read **OPEN**,
and a deliverable may rest a price on it indefinitely.
That is exactly how `DECISIONS-FOR-NDI` decision 8 went to a customer priced on `CH-0185`'s
threshold **in the same iteration `C-0148` withdrew it**.

`C-0197` §5 measured the residue and printed it, ungated: **17 challenges over 22 sites**, with
**one** named false positive (`CH-0157`) deliberately not tuned away, on `C-0176`'s ground that a
guard narrowed to one observed case is a test written to the shape of the change.

### The acceptance predicate

`P1` **Every one of the 17 is disposed of, in writing, by reading the claim that adjudicates it** —
either annotated in its own file with the adjudicating claim named and the verdict stated
(`UPHELD` / `UPHELD IN PART` / `RESOLVED` / `OVERTURNED` / `ANSWERED` / `DISCHARGED`, and which
part where the adjudication is partial), or excluded with a stated ground.

`P2` **`tools/trace-answers.py`'s `UNRECORDED-ADJUDICATION` residue reads 0 and becomes a gate**,
counted into the exit code, with a named test in **both** directions on the exit code itself.

`P3` **Every exclusion carries a named test and a measured false-negative count.**
The narrowing is written only after its cost is counted over the **whole** claims corpus, not
after it is seen to remove the observed case.

`P4` **`tools/trace-answers.py` exits 0 on both deliverables**, and any new `STALE-OPEN` an
annotation creates is reported rather than repaired — the two deliverables are not this task's to
edit.

`P5` **The index-versus-file disagreement is derived fresh, reported, and reconciled where the
reading is unambiguous**; where it is not, that is said.

### What is NOT in scope

- The two deliverables. An annotation can turn a passage calling a challenge *raised* into a live
  `STALE-OPEN`; that is reported to the coordinator with the exact passage.
- Adjudicating a challenge nobody has adjudicated. A status is a **verdict**, and this task may
  only record a verdict some claim already reached.

---

## Plan

**The cheap bound runs first, and it is a simulation rather than an edit.**
Before annotating anything, set all the candidate statuses to `CLOSED` in memory and run
`stale_challenge_statuses` over both deliverables.
That costs one pass and it says, in advance, exactly which passages the annotation will break —
here **one**, `ANSWERS.md` line 964, and reading it settles that the *sentence* is right and the
*checker* is wrong, which is `C-0115`'s discipline read the other way round.

**Then the reading, which is the whole cost.**
For each of the 17: locate the sentence in the adjudicating claim, read what it adjudicates and
what it leaves standing, and write a Status row that says both.
A template would defeat the task: `CH-0101` is discharged **in one item** and stands in the rest,
`CH-0010` is upheld **in substance and split**, `CH-0151` is **overturned**, and no single word
covers them.

**Then the narrowings, measured first.**
Two of the 17 are not adjudications. Each candidate guard is run over the whole claims corpus and
its removals counted **before** it is committed, and it is accepted only if every removal is one
of the intended sites.

**Method justification against cost.** There is no cheaper instrument. A count cannot decide a
verdict, and no widening of any predicate in `tools/trace-answers.py` can reach a fact that is not
written down anywhere — `C-0197` says so in as many words: *"the defect behind decision 8 is in the
AUTHORITY, not in the predicate"*. The only thing that closes it is reading 17 claims.

### What would falsify this approach

1. **A challenge among the 17 that no claim actually adjudicates, beyond the ones excluded.**
   Then the predicate is looser than two guards can repair and the residue cannot be gated at all,
   and the right outcome is `C-0129`'s: leave it printed and say why.
2. **A narrowing whose measured false negatives include a genuine adjudication.**
   Then the guard is wrong, whatever it does to the observed case.
3. **An annotation that manufactures a `STALE-OPEN` on a passage that is correct as written.**
   Then the annotation is right and the checker's window is wrong, and the checker is what must
   move — or, if it cannot, the task has made the corpus worse and must stop.
4. **A status word this repository's readers do not know.** A verdict written in a word
   `_CHALLENGE_CLOSED` does not carry is silently read as OPEN, so every word used here must be
   one the reader already knows, or be added with a test in both senses the day it is coined.
5. **The gate firing on a correct corpus after the sweep.** If `UNRECORDED-ADJUDICATION` cannot be
   brought to 0 without asserting a verdict no claim reached, it must not be gated.
