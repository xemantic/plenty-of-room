# T-252 — a number quoted in a claim has no link back to the result file it came from

| | |
|---|---|
| **Leaf** | none — a **process** task, protecting the provenance of every published number |
| **Raised by** | [`CH-0199`](../challenges/CH-0199-a-quoted-number-has-no-link-back-to-its-file.md) from [`C-0153`](../claims/C-0153-unrounded-prose-interpolations.md) (`T-249`) |
| **Verification type** | **logical** — a census over the corpus's documents against every committed result file, with every hit read by hand |
| **Units** | none; every value is an integer count, a line number or a name |

---

## Formulate

`C-0101`'s rule — *re-emit the downstream file and amend the claim where it quotes a moved number* —
is executed by hand and has no instrument.
`tools/trace-answers.py` traces a **synthesis** against **claims**;
`tools/result-transfers.py` compares two **result files**;
nothing checks a **claim** against the **result file it names**.

The acceptance offers two branches:

> Either a corpus convention that marks a historical number (so an unmarked, unfindable token is a
> defect by construction) plus the twenty-line checker that reads it,
> or a recorded decision that the class stays manual and why.

### The acceptance predicate

**PASS** iff:

- **`F1`** the census is **re-derived**, not inherited, over a stated document set and a stated
  result-file set, with the ref recorded (`CH-0246`);
- **`F2`** **every** unfindable token is read by hand and classified *deliberate* or *stale* —
  a count is not a classification;
- **`F3`** whichever branch is taken is justified by that classification rather than by preference;
- **`F4`** if the decision is *manual*, the part of `CH-0199` that **is** decidable is separated
  out and gated, so the task closes with an instrument rather than with a paragraph.

**What would falsify this approach**: a single **stale** token among the unfindable — that would
make the first branch the right one and the convention worth its cost.

---

## Plan

**The cheap bound is the whole method, and it runs before any convention is designed.**
The one class in which `CH-0199` is *detectable* is a decimal carrying **more than nine
significant digits**: a correctly rounded result file cannot contain one, so an unfindable token
of that shape is either **historical** or **stale**.
Collect every such token, look it up in the concatenated text of every committed result file, and
**read every miss in its own context**.

That reading is the deliverable.
If the misses are stale, the convention is worth designing; if they are deliberate, the convention
would be written onto correct claims and its first output would be that many non-defects.

**Then separate what is decidable.**
*Is the named result file still there* is a different question from *is the quoted number still in
it*, and it is decidable — and it is **not** covered: a `Provenance` row names its file as a bare
path, and `tools/check-corpus-links.py` resolves `[label](target)` and nothing else.

**Cost**: minutes. No solve, no Gradle, no re-emission.
