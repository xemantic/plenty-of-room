# T-282 — regenerate the `T-234` classification, and say what the regeneration is allowed to launder

| | |
|---|---|
| **Raised by** | [`C-0179`](../claims/C-0179-the-debt-line-as-a-ratio.md) (`T-280`), which verified that every defect standing at the time was pre-existing and deliberately did **not** regenerate |
| **Leaf** | — (process) |
| **Verification type** | logical — a before/after reading of an existing gate, itemised, with every reclassification read one at a time |
| **Units** | none; this is a document-integrity task |

## Formulate

`tools/T-234-classification.json` is a **reading**, retained as data so that a reader can disagree one
occurrence at a time, and regenerable from stated rules so that an occurrence a later edit adds is
classified by the same rule the rest were.
It has gone stale again: `python3 tools/T-234-census.py --check` reads **21** defects on the working tree,
**18** of them at the last commit.

None of the 21 is a wrong reading of a design premise.
They are the two shapes a stale index table has:
occurrences the corpus has gained since the table was written (**15 unclassified**),
and entries whose class was correct under the pre-`C-0176` family map and is not correct under the split one (**6 wrong discharge**).

The task is one command.
What makes it a task rather than a chore is **when** it may be run and **what it may not quietly do**.

**Numeric target.**
`python3 tools/T-234-census.py --check` exits **0**;
the delta between the before and after readings is itemised occurrence by occurrence;
every hand override either survives or is reported by name as dropped;
and no reclassification is accepted that the reader cannot state a ground for.

**Acceptance predicates, falsifiable.**

- **F1 — the gate comes clean.** `--check` exits 0 after the regeneration, and the reading is recorded before and after.
- **F2 — the delta is itemised, not summarised.** Every one of the 21 defects is named and matched to what removed it: the predicate repair of `T-285`, or a rule of the emitter, and which rule.
- **F3 — no hand override is silently re-pointed.** `C-0176` keys a hand override on the occurrence's own **neighbourhood** and drops it **loudly** when that text is rewritten. Every override is counted before and after and any drop is reported by name. A silently re-pointed reader's call is the one failure this mechanism exists to prevent, so a drop that is *not* reported falsifies the run.
- **F4 — the regeneration may not launder a defect.** Where regenerating would classify something the reader knows to be wrong, the entry is left and said so: `C-0083`'s *a gate that cannot come clean is not a gate* has an inverse, and a green gate that is not honest is worse than a stale one that is. Every class the emitter assigns to an occurrence entering the table for the first time is read.
- **F5 — the ordering is the point.** `T-285` blanks a filename before matching, which removes occurrences and therefore **moves every index below them**. The regeneration must run **after** it, or the table it writes is keyed on indices that the very next run of the census will not produce.
- **F6 — the tree is quiet, and this is checked rather than assumed.** `C-0176` §1b records a regeneration that had itself gone stale because three further claims entered the corpus while it was being taken. `git status` is read before the regeneration and what it shows is reported: an in-flight **half-written** source is a reason to stop, a finished uncommitted artifact is part of the corpus and must be swept in.
- **F7 — the debt line is reported before and after, in both denominators.** The two deliverables are not edited by this task, so their debt should move only by whatever `T-285` removes from them. A movement larger than that is unexplained and falsifies the run.

**What would falsify the approach.**
An occurrence whose regenerated class is wrong and whose correct class cannot be reached by a stated rule.
That would mean the emitter's rules no longer describe the corpus,
and the answer would be a rule change or a hand override — never a regeneration that buries it.

## Plan

**Cheap bound first, and it is the reading itself.**
The gate already itemises. Capture `--check` before the change, capture it after, and diff.
The before-list partitions into four groups without any code being written:
four unclassified `TASKS.md` rows added since the last regeneration,
eight unclassified occurrences in `C-0175` (a claim filed in iteration 42),
three unclassified occurrences in two claims filed in iteration 43,
and six wrong-discharge entries on rows written before `C-0176` split `PLACEMENT` into `PLACEMENT`/`GRILLAGE` and `WIDTH` into `WIDTH`/`ROW_SPAN`.
Only the last group needs a judgement, and the judgement was already made — in `C-0176`, which supplied the split and could not retro-fit the table because it is keyed on the index.

**Why regenerate rather than hand-edit the eighteen.**
A hand edit is an override, and an override is a **dated object**: it survives the next regeneration and therefore
outlives the reading that produced it. The emitter exists so that the class is a rule and not a memory.
Six hand overrides are carried deliberately and each has a written ground; adding eighteen more,
all of them reproducible from the rules, would make the table a transcript again.

**Why now and not in the iteration that found it.**
`C-0176` §1b is the ground: a regeneration taken mid-iteration inherits whatever is in flight,
and the inherited table it found had itself gone stale inside one task.
The check is `git status`, and it is recorded in the claim rather than asserted.
