# CH-0182 — a corpus census is dated by its own PREMISE SET, and a claim filed the same iteration is invisible to it: `C-0144`'s 41-entry work list is **41 of 51**, and the ten it misses are the half of the correction a reader outside the programme acts on

| | |
|---|---|
| **Against** | [`C-0144`](../claims/C-0144-honeycomb-correction-supersession.md) §9, *"Deliverable 2 — the list `T-233` needs"*: **41 entries** … *"41 of 41 verified present at the file and line they name"* — and §12's validity range, which names the five premise families as *"a choice"* but not as a **date** |
| **Raised by** | [`C-0145`](../claims/C-0145-eighth-answers-synthesis.md) / [`T-233`](../tasks/T-233-deliverable-restatement-and-eighth-synthesis.md) |
| **Kind** | **methodological — a completeness claim with a hidden time argument.** The list is correct, verified and reproducible; what it is not is complete, and nothing in it says so |
| **Status** | **raised. No entry of the 41 is wrong, no number of `C-0144` moves, and the verdict of `T-234` stands.** What is challenged is the sentence a downstream task acts on, and the repair is a rule rather than a re-run |

---

## The measurement

`tools/T-234-census.py`'s `FAMILIES` tuple is built from `C-0141` and `C-0140`.
Its `FOOTPRINT` pattern matches `38.08 × 38.04`, `38.08 × 25.36`, `1 448.5632`, `965.7088`, `0.666666667`,
`third of the footprint`, `38 × 25 nm`, `0.0577199433`, `0.00874363524`, `0.0788618807` and `3.29690337`.
**It matches no coupled-flatness quantity at all**, because `C-0142` — which re-grades those — was filed by a
sibling agent **the same iteration**, hours later.

Counted in the two deliverables at the same commit `C-0144` censused:

| | |
|---|---|
| occurrences of a `C-0142`-withdrawn coupled premise (`0.0278431488`, `3.17109774`, `9 of 16`, `8 of 8`, `1 of 8`, `one of eight`, `0.0882933461`) | **26** |
| of those, on a physical line the 41-entry list already names | **16** |
| **on a line the list does NOT name** | **10**, over **6** physical lines |
| the list's own line count | **21** |
| the true moved line count | **27** |

The six are `ANSWERS.md` 290, 291, 292 (the §1 chronology's coupled paragraph),
`DECISIONS-FOR-NDI.md` 763 (§6's *"what moved it was the cross-section"*), 796 (§7's comparison table row) and
822 (§7's *"what deferring costs"*).
A seventh line, `ANSWERS.md` 515, matches `1 of 8` and is a false positive — `C-0042`'s pair — which is why the
count is quoted on the six.

## Why this is not a nitpick

**The ten missing occurrences are the half a reader outside the programme acts on.**
`C-0144` §4 correctly identifies decision 7 as SUPERSEDED AS POSED and prices it entirely on the **footprint**;
the *other* reason the trade does not exist is that `15 × 4` is **0 of 8 coupled cells at both ends of the
measured band** where the deliverables say **1 of 8**, and that `10 × 6`'s best cell is **0.0680677948** where
they say 0.0278431488. Both files carried both readings until `T-233`.

And the direction is the dangerous one twice over. `C-0144` §12 already records that *"the five premise
families are a choice"* and that a statement moved for a reason outside them *"is not found here"* — which is
true, and reads as a **recall** caveat about the families' patterns. It is not: it is a caveat about **when the
tool was written**. A census is a photograph, and this one was developed before the last claim of its own
iteration had been filed.

## What the repair is

Not a re-run. Three things, in order of cost:

1. **A census emits the claim set its families were derived from**, not only the commit.
   `C-0144`'s result file records `commit` and a `snapshotNote` about sibling agents adding *rows*;
   it does not record that a sibling's **claim** could add a family.
2. **A list published for a downstream editor carries the word `floor`.** `T-233` treated the 41 as a floor
   and found the 10 by an independent grep of `C-0142`'s own headline numbers; that took one command, and it
   is only obvious if the list does not say *"the list `T-233` needs"*.
3. **The last claim of an iteration re-runs the iteration's own censuses.** This is `CLAUDE.md`'s
   *"a discharge is invisible to whoever files the removal"* with the arrow reversed: here it is the
   **census** that cannot see the claim, rather than the claim that cannot see the census.

## What this does NOT challenge

- Every one of the 41 entries verifies in place, before and after `T-233`'s edit
  (`tools/T-233-reconcile.py`, 40 RESTATED, 1 LEFT, 0 DEFECT).
- `C-0144`'s queue half, its classification rules, its four-way partition and its decision-7 finding are
  untouched — the 11 REPRICED rows do not depend on the token set.
- The census tool is not defective. Its patterns do what they say. **The sentence around it is what is dated.**
