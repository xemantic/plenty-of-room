# CH-0196 — **`C-0055`'s *"the price, which is published"* quotes Ke et al. across an ellipsis that removes the authors' own declination of the attribution.** The yield observation is published; the attribution to the 8 bp staple break is **explicitly deferred by the paper** — *"Alternatively, simply having a large number of layers with our default crossover pattern may be destabilizing, **irrespective of the position of the breaks**"*, and *"**Future systematic studies will be required** to determine the relative importance of these staple breaks"*. Both sentences fall inside the quoted paragraph and neither survives the quotation. **No number is moved and no verdict is overturned; what is withdrawn is the word *"cost"***

| | |
|---|---|
| **Against** | [`C-0055`](../claims/C-0055-unused-junction-site.md) Deliverable 6 — *"**This is the cost of the escape and it is a folding-yield cost, not a geometric one**"* — and, through it, `CLAUDE.md`'s standing entry *"A crossover 8 bp from another forces an 8 nt staple domain, and **that is a published YIELD cost**"*, which is the sentence the challenge is really about because it is the one other tasks read |
| **Raised by** | [`C-0152`](../claims/C-0152-forced-scaffold-crossover-price.md) / [`T-246`](../tasks/T-246-forced-scaffold-crossover-price.md) §6, result [`gpd/results/T-246-forced-scaffold-crossover-price.json`](../results/T-246-forced-scaffold-crossover-price.json), section `literature` |
| **Grounds** | **literature.** One paragraph of `PMC2821935`, **read directly**, retained at [`gpd/data/T-246-sources/PMC2821935.txt`](../data/T-246-sources/PMC2821935.txt). No solve, no arithmetic |
| **Kind** | **a scope correction, not an error.** Every word `C-0055` quotes is in the paper and is quoted accurately. What the ellipsis removes is the paper's **scope statement** — and `CLAUDE.md` records in as many words that *"an explicit exclusion is far stronger evidence than a null search"* and that one must *"read the scope statement of the paper that has the right coordinate"*. Here the scope statement was inside the quoted passage and was elided |
| **Status** | **raised.** `C-0055`'s geometry, its junction-site census and its verdict are untouched. The 8 bp staple break is still the pattern an upward hinge creates, and lower yield is still what Ke et al. **observed** |

---

## 1. The paragraph, entire

`PMC2821935` (Ke et al., *J. Am. Chem. Soc.* **131**:15903, 2009), read directly:

> *"In our default design strategy, some staple breaks must be implemented between crossovers 8 bp
> apart. For the two-layer and three-layer structures, very few such breaks need to be
> incorporated. However, for the six-layer design, many such breaks must be used. We observed
> significantly lower yield for these structures. Introducing these breaks may be destabilizing for
> the structure. **Alternatively, simply having a large number of layers with our default crossover
> pattern may be destabilizing, irrespective of the position of the breaks.** For the 8 × 8 design,
> we avoided the implementation of such staple breaks by omitting many crossovers in the core of
> the block. For this design, we observed a high yield of well-folded structures. These results
> suggest that omitting crossovers produces more relaxed structures that are easier to realize
> **or else** that the omission of staple breaks positioned between crossovers 8 bp apart could
> improve folding quality as well. **Future systematic studies will be required to determine the
> relative importance of these staple breaks toward affecting folding efficiency.**"*

The two bolded sentences are what the standing quotation's `…` removes. The paper offers **two**
candidate causes — the staple breaks, and the layer count *irrespective of the breaks* — declines to
choose between them, and says a future study is required. Its own summary sentence carries an
*"or else"*.

## 2. What is withdrawn, and what is not

| standing wording | status |
|---|---|
| *"some staple breaks must be implemented between crossovers 8 bp apart"* | **upheld** — a design rule, stated as fact |
| *"We observed significantly lower yield"* | **upheld** — an observation, and it is real |
| *"they raised the yield of their 8 × 8 block by omitting crossovers"* | **upheld** — also stated as fact |
| **"that is a published YIELD cost"** *of the 8 bp break* | **withdrawn.** The paper attributes it to two candidate causes and chooses neither |
| **"The cost of a denser crossover pattern is folding yield, not geometry"** | **survives on the OTHER cause.** *"Simply having a large number of layers with our default crossover pattern may be destabilizing"* is itself a statement that a denser crossover pattern costs yield — so the sentence is true, and it is true for a reason the quotation had removed |

**And there is no number in it either way.** *"Significantly lower"* and *"a high yield"* are the
paper's own words; it publishes no percentage for this comparison. Any downstream reading that
treats the 8 bp break as a *quantified* cost is reading a number the source does not print.

## 3. Why it matters here rather than only in principle

`T-246` was sent to price a **forced scaffold crossover** and told that Ke et al.'s 8 bp domains
were *"the nearest priced analogue in this corpus"*. They are the nearest analogue and they are
**not priced** — so the analogue could not be used, and `C-0152` had to deliver `P-6`'s ceiling and
threshold instead. A corpus that records an unattributed observation as a *cost* will keep offering
it to tasks that need a *number*.

## 4. Scope

- `C-0055`'s junction-site census, its 34-arm count and its acceptance verdict are consumed
  unmodified and are untouched.
- Nothing graded moves. No result file is re-emitted; the challenge is a wording correction to a
  claim's Deliverable 6 and to one `CLAUDE.md` entry.
- The remedy is one sentence in each place: quote the paragraph **including** its alternative and
  its deferral, and call it *"a yield observation whose attribution the authors decline"*.
