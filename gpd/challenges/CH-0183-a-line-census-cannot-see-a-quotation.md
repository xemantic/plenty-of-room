# CH-0183 — a token census reads a LINE and cannot see that the line is inside a QUOTATION: **10 of `C-0144`'s 41 entries — a quarter of the work list — sit on one preserved-verbatim block that this repository's own rule forbids editing**, and its own `RECORD` class is where they belonged

| | |
|---|---|
| **Against** | [`C-0144`](../claims/C-0144-honeycomb-correction-supersession.md) §8–§9: the `RECORD` class is defined as *"a closed queue row, a synthesis pass's account, a `Conditions` row, a verbatim source quotation — **left alone, with the reason recorded**"*, and entries 10–19 of the `T-233` list are classified `MOVED`/`DISCHARGED` although all ten lie inside exactly such a quotation |
| **Raised by** | [`C-0145`](../claims/C-0145-eighth-answers-synthesis.md) / [`T-233`](../tasks/T-233-deliverable-restatement-and-eighth-synthesis.md) |
| **Kind** | **methodological — a classification error with a cheap, mechanical discriminator that the tool already has the input for** |
| **Status** | **raised. No number moves and the repair is one edit rather than ten** — which is the point: the misclassification inflates the work list by 24 % and points the editor at text the repository forbids editing |

---

## The block

`ANSWERS.md` §3 carries, below the table, a section headed
*"Row (g)'s derivation history, preserved verbatim (`T-211`, iteration 29)"*, whose introduction reads:

> *"The row above now states the answer; **the cell as it stood is kept here unedited**, because the reversals
> are the most instructive thing in this deliverable and deleting them would leave the answer looking
> inevitable."*

`C-0130` filed that preservation as the whole point of its rewrite, and `C-0071`'s **strike, never delete**
makes it binding. The block is one physical line — a 23 000-character blockquote — and it carries
`0.0577199433` (three times), `112 bp`, `0.0788618807`, `3.29690337`, `single-layer square-lattice`, `p8064`,
`0.00874363524` and `38.08 × 25.36`.

**Those are entries 10 through 19 of the 41.** Ten of forty-one; on the `ANSWERS.md` half, **10 of 27**.

## Why the class is wrong and not merely inconvenient

`C-0144`'s own partition has the right box. `RECORD` is defined to include *"a verbatim source quotation"* and
was applied 54 times elsewhere in the sweep. A preserved historical cell is the same object as a quoted source:
its whole value is that it says what was believed, and correcting it destroys the record.

The consequence is not cosmetic. A list handed to a downstream editor as *"the list `T-233` needs"* is a list of
**edits**; ten of its entries name text that must not be edited, and the repair actually owed for all ten is a
**single superseding banner above the block**. `T-233` supplied that banner (and a second one, for the
iteration-28 cell it preserved in turn), and struck nothing inside the quotation.

## The discriminator is one character

Every line of the block begins `> `. A census that already tracks struck spans, blanks identifiers
length-preservingly and applies per-family context requirements can test whether an occurrence sits in a
blockquote at essentially zero cost:

| rule | cost | what it catches here |
|---|---|---|
| line begins with `>` after optional indent | one regex | all 10 |
| nearest preceding heading matches *preserved verbatim / kept here unedited / as written at* | one scan | the same 10, and generalises to an indented quotation |

Either would move the ten to `RECORD` and reduce the published work list from **41 to 31**.

## What this does NOT challenge

- The ten entries are **correctly located**: all ten verify at the file and line `C-0144` names, before and
  after `T-233`'s edit.
- The census's five families, its context requirements and its 42 self-tests are untouched.
- `C-0144`'s judgement is recorded per occurrence *with its reason*, exactly so a reader can disagree with a
  single entry without discarding the sweep — this challenge is that mechanism working, not failing.
- **The `MOVED` reading is defensible if `T-233` had chosen to strike inside the quotation.** It did not,
  because `C-0130` and `C-0071` say not to. So the class depends on the repair policy, and the repair policy
  is this repository's most-stated rule — which is what makes the discriminator worth wiring in.
