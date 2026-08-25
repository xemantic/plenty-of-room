# CH-0280 — RETAINING THE BEFORE STATE IS RIGHT AND IT MAKES A **STALE QUOTATION UNFINDABLE AS STALE**: EVERY SUPERSEDED TOKEN IS STILL IN THE CORPUS, SO A CENSUS THAT ASKS *"IS THIS NUMBER IN A COMMITTED ARTIFACT"* ANSWERS **YES** FOR A NUMBER NO LIVE ARTIFACT CARRIES

| | |
|---|---|
| **Against** | not a claim's physics — a **corpus mechanism**: `C-0092`'s *a repair must leave the defect measurable*, composed with the numeric tracing this repository does. Instanced on [`C-0216`](../claims/C-0216-the-placement-and-the-distribution-together.md) §11 |
| **Not against** | `C-0092` itself, which is correct and is what made `F23` measurable at all; nor `C-0216`'s correction pass, which caught **seven** of the eight tokens |
| **Raised by** | [`C-0217`](../claims/C-0217-the-cure-at-every-call-site.md) / [`T-328`](../tasks/T-328-the-cure-at-every-call-site.md) |
| **Grounds** | methodological — the same shape as `CLAUDE.md`'s *a number can be correctly cited, not superseded, and the wrong quantity*, reached through a **retained artifact** rather than through a claim |
| **Status** | **RAISED.** One live instance, corrected in `C-0216` by this claim. No number of any claim moves; what moves is what a token census can be relied on to say |

---

## The instance, and it is one token

`C-0216` was written against `T-323`'s **run A** and is filed against the committed artifact, which
is **run B**. `F23` fired, 26 leaves moved, and `C-0216`'s own journal entry records the response:
*"correct the seven quoted tokens — two of them in the claim's headline."*

**The eighth was missed.** `C-0216` §11's `P8` row reads

> `P8` — the same 2 × 2 at `f = 0.26` — §2 — **it ran**; interaction `−7.8 %` out of sample and `−13.0 %` in sample

and the committed artifact's `split/2/interactionPerCent` is **`−8.0`**. `C-0216` §2 has it right
(*"`−8.0 %` out of sample against `−13.0 %` in sample"*), so the claim disagrees with **itself**
across nine sections as well as with its own artifact.

## Why nothing catches it, and the reason is the retained run

`tools/trace-answers.py` traces the two **deliverables** against the claim corpus, not a claim
against its own result file. `tools/check-result-path-references.py` resolves **paths**. Neither
reads a number out of a claim and asks the artifact.

And the one census that *would* — *is this token present in a committed artifact* — now answers
**yes**, because `gpd/data/T-323-reproducibility/run-a.json` is committed and carries
`"interactionPerCent": -7.8`. **The retention that makes the defect measurable is exactly what
makes the stale quotation indistinguishable from a live one.**

That is not an argument against retention. `C-0092`'s rule is right and `C-0216` obeyed it. It is
an argument that the *cost* of retention has a name, and the name is: after a study is emitted
twice, the corpus contains **two** consistent readings of every moved leaf and a numeric census can
no longer tell which one a claim is entitled to quote.

## What the challenge asks

1. That a claim filed against the **second** of two retained emissions say so **at the point of
   each quotation** that a moved leaf reaches, not only in the section that discusses the diff —
   because a reader arriving at §11 has no way to know that §11's number is run A's.
2. That where a corpus census is taken over `gpd/` **for the purpose of tracing a claim's numbers**,
   `gpd/data/*/run-*.json` be excluded by construction — a retained before-state is evidence about
   the past, and a tracer is asking about the present.
3. That the general rule be stated the way this corpus states its others: **a superseded number is
   still in the corpus, so presence is not provenance.**

## What does NOT move

No physics. `C-0216`'s `f = 0.30` arm — every number its headline leads with — is bit-identical
across its two runs, and `−7.8` against `−8.0` is a two-significant-digit departure at the arm the
claim itself labels *determined to about three significant digits, not nine*. What is wrong is that
a reader cannot tell.
