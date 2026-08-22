# CH-0212 — **the recommended `10 × 6` block is DRAWN WITHOUT the seam a standing claim says is forced on it**, and the two statements cannot both be true of one object. `C-0119` §4 derives the seam as a **tree parity** and calls the 60-helix case *"a theorem, not an enumeration"*; `C-0160`'s committed `.sc` for the same block carries **one scaffold strand with exactly 60 domains on 60 helices** — one per helix, a pure boustrophedon — which is **`C-0161`'s own seam discriminator**, applied to this repository's own artifact and returning *no seam*. It matters because `C-0161` §4(b) has just established that **a seam breaks the column-parity alternation `CrossoverLayout` imposes by construction**, and `HoneycombCoupledStudy.kt:312` and `HoneycombPlacementStudy.kt:267` **both call `CrossoverLayout.centred`** — so on the seamed reading every coupled flatness cell and every placement on the recommended block is outside the family its own generator can produce

| | |
|---|---|
| **Against** | [`C-0119`](../claims/C-0119-honeycomb-raster-width.md) §4 — *"A seam is forced … the tree parity applies unchanged … The 60-helix case is a theorem, not an enumeration"* — and its Verdict row's *"A **seam is forced** by the same tree-parity that forces Rothemund's"*; read against [`C-0160`](../claims/C-0160-scadnano-writer.md)'s committed artifact `gpd/designs/gen1-block-honeycomb-10x6-102-109.sc` |
| **Raised by** | [`C-0165`](../claims/C-0165-eleventh-answers-synthesis.md) (`T-271`), the eleventh `ANSWERS.md` synthesis, while carrying [`C-0161`](../claims/C-0161-mechanics-on-an-imported-design.md) §4(b)'s parity restriction into the deliverables |
| **Grounds** | **logical + a census of a committed artifact** — a domain count on the emitted `.sc` file, taken with the discriminator `C-0161` §4 states in its own words, plus two `grep`s of the study sources that grade the recommended design |
| **Status** | **CLOSED, iteration 40** — [`C-0168`](../claims/C-0168-recommended-block-seam.md) (`T-274`), on the **favourable** resolution. `C-0160`'s artifact is right as drawn; `C-0119` §4's *forced* is withdrawn and annotated, its derivations upheld and reproduced at departure `0.0`. **No number moved**, and the counterfactual this challenge left unmeasured is now measured: the rectangle's own seam shape costs **1.15701888×** (`0.0240648102 → 0.0278434397`), both flat at `T-5b`'s 0.10. **Proposed reading (2) could not be taken** — the block carries **0** staple crossings and **0** columns, so it has no column parity to read — and reading (1) is answered by a **degree census**: the two raster termini are **degree one**, so no honeycomb block of this family admits a Hamiltonian cycle, and the seam turns entirely on the **second** premise, which 919 spare nucleotides close at **1.03–2.69 `k_BT`** |

---

## The observation

`C-0161` §4 gives the discriminator, on the reference implementation's rectangle, and states it as a
general fact about a boustrophedon:

> The 31 scaffold domains against 16 helices are the seam — **a boustrophedon on 16 helices has 16.**

Applied to this repository's own committed artifact for the design it recommends:

```
python3 -c "import json; d=json.load(open('gpd/designs/gen1-block-honeycomb-10x6-102-109.sc'));
s=[x for x in d['strands'] if x.get('is_scaffold')];
print(len(d['strands']), len(s), [len(x['domains']) for x in s])"
# 1 1 [60]
```

**One scaffold strand, sixty domains, sixty helices** — one domain per helix. By `C-0161`'s own
discriminator that is a boustrophedon with **no seam**, and it is the *same* count that identifies
the rectangle's 31-against-16 as a seam.

`C-0119` §4 says the opposite of the object:

> A honeycomb helix has **three** neighbours, so that graph has cycles and the argument should not
> survive. **It does** … The graph the **scaffold** may use … for an `m × n` raster is a **path**
> again. The tree parity applies unchanged. … The 60-helix case is a theorem, not an enumeration.

Both artifacts are internally consistent, both trace clean, and neither claim can see the other:
`C-0119` predates the writer by four iterations, and `C-0160`/`C-0161` never compare the emitted
scaffold against `C-0119`'s parity theorem.

## Why it matters, and it is not the scaffold budget

On its own this would be a bookkeeping disagreement about one file. `C-0161` §4(b), filed in the
same iteration, is what gives it a consequence:

> `CrossoverLayout.centred` and `CrossoverLayout.phased` alternate the parity **by construction**;
> the rectangle's seam doubles a column pitch … **Every phase-swept placement, count and flatness
> result in this corpus is over the alternating family, and a seamed sheet is outside it.**

And the recommended block's own flatness and placement cells are graded through exactly that
generator:

| call site | line |
|---|---|
| `src/main/kotlin/tile/HoneycombCoupledStudy.kt` | `columns = CrossoverLayout.centred(floor(usable / pitch).toInt() + 1, pitch)` |
| `src/main/kotlin/tile/HoneycombPlacementStudy.kt` | `val columns = CrossoverLayout.centred(floor(usable / pitch).toInt() + 1, pitch)` |

`HoneycombPlacementStudy.kt` already records the narrower half of the caveat in its own text —
*"`structure/CrossoverLayout` — `BASE_PAIRS_PER_PERIOD = 32` and a two-parity alternation"* — which
is a statement about the **period**, not about the **alternation**, and it is the alternation that
a seam breaks.

So the two claims compose into a fork, and the deliverables currently carry only one side of it:

- **If `C-0160`'s artifact is right and the block is seamless**, `C-0161`'s restriction does *not*
  bite the recommended design, every coupled cell of `C-0142` / `C-0146` / `C-0151` is in-family,
  and `C-0119`'s *"forced"* is over-stated — true of a **fully folded** circular scaffold and not of
  this one, which uses **6 330 of M13's 7 249 nt with 919 spare** (`C-0160`) and can therefore close
  through its own remainder. `CLAUDE.md` states exactly that premise structure: *"**A seam needs
  BOTH premises and dropping either removes it**"* — a path graph **and** a fully folded circular
  scaffold — *"a circular scaffold left partly unfolded closes through its own remainder."*
- **If `C-0119` is right and the seam is forced**, then the committed `.sc` is not the design the
  corpus recommends, every lattice fact `C-0160` re-derived *on that file* was re-derived on a
  different object, and every phase-swept placement and coupled flatness cell on the block is
  outside the family `CrossoverLayout` can generate.

**The favourable resolution is the likelier one and it still has to be written down**, because as
the corpus stands a reader who follows `C-0119` and a reader who opens the file reach opposite
conclusions about the same tile.

## What is *not* claimed

- **Not** that any number is wrong. Nothing here re-runs a study and nothing here moves a value.
- **Not** that `C-0119`'s tree-parity derivation is wrong. Its brute force at orders 3–7 and its
  reading of the caDNAno figure are untouched; what is challenged is the word **forced**, i.e.
  whether the theorem's second premise holds for a scaffold this design leaves 919 nt of.
- **Not** that `C-0161` §4(b) is wrong. It is upheld and this challenge is an instance of it — the
  instance `C-0161` did not look for, because it was grading a foreign design.
- **Not** that a seam would necessarily move a coupled cell. `C-0161` measures the parity exchange
  as **exactly symmetric** under one symmetric load case (departure `6.7e−12`) and says so: the
  agreement is *"a symmetry of this load case, not an insensitivity to the parity"*. Under the
  solved edge collar the two would part, and by how much is unmeasured.

## What would settle it

Two readings, both free, neither needing a solve:

1. **Which premise `C-0119`'s theorem needs.** One paragraph: does the tree parity force a seam for
   a circular scaffold that is only 87 % folded, or only for a fully folded one? `CLAUDE.md` already
   answers this for the square sheet and the answer has never been applied to the honeycomb block.
2. **What the emitted artifact's crossover column parities actually are.** `T-267`'s importer reads
   a column parity sequence out of a `.sc` file — that is how the rectangle's `[0, 1, 0, 1, 1, 0]`
   was obtained. Running it on `gen1-block-honeycomb-10x6-102-109.sc` returns the sequence directly,
   and if it alternates, this challenge closes on the artifact rather than on the argument.

Both are cheaper than the sentence that would defend either side, which is why this is filed rather
than answered.
