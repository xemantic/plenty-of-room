# CH-0220 — **A GATE WHOSE ACCEPTANCE BAND SPANS TWO DIFFERENT OBJECTS CANNOT SAY WHICH ONE IT PASSED ON.** `C-0157`'s verification type is *"the design generator asserts the corpus's own lattice counts before anything is simulated"*, and what the generator asserts is `49`, against a docstring reading *"inside the **49–56** the corpus reports the tile builds"*. But 49 and 56 are not a tolerance on one number — they are **two different lattices**, one 8 bp from the other, with **no staple crossover column in common**. A band drawn between them is satisfied by either, so the gate was incapable of detecting the substitution it existed to prevent; and the assertion is against a **transcribed literal**, not against `rasterColumnLayout`, which is the function that would have said which

| | |
|---|---|
| **Against** | [`C-0157`](../claims/C-0157-crossover-hinge-constant-from-oxdna.md)'s *Verification type* row, and the pre-simulation checks in [`tools/oxdna/gen1_tile_design.py`](../../tools/oxdna/gen1_tile_design.py) / [`tools/oxdna/test_gen1_tile_design.py`](../../tools/oxdna/test_gen1_tile_design.py) that discharge it |
| **Raised by** | [`C-0170`](../claims/C-0170-simulated-tile-census.md) (`T-275`) |
| **Grounds** | **logical** — the generator's own three assertions are read, and the census that would have discriminated is run |
| **Status** | **OPEN — filed, not repaired.** The generator is internally consistent and its three checks pass; the charge is about what they are checks *against* |

---

## The observation

`tools/oxdna/gen1_tile_design.py` states the acceptance in its own docstring:

> That gives `7 x 4 + 7 x 3 = 49` crossovers, **inside the 49-56 the corpus reports the tile builds**.

and `tools/oxdna/test_gen1_tile_design.py` discharges it three times:

```
check('crossover columns', columns, [8, 24, 40, 56, 72, 88, 104])
check('total crossovers', sum(per_interface), 49)
check('staple crossings', sum(crossings.values()), 49)
```

Every one of those passes, and every one of them compares the generator against **itself**: the
column list is the generator's own `range(8, 112, 16)` written out longhand, and 49 is its own
`7×4 + 7×3`. Nothing in the chain reaches `anchoring.rasterColumnLayout`, the function whose output
`C-0009`, `C-0015`, `C-0063`, `C-0090`, `C-0099` and `C-0169` are all graded on.

## Why the band is the defect

`C-0015`'s *"49–56"* is not an interval of uncertainty. It is the range over the 32 crossover
phases of a 38.08 nm tile, and the census (`C-0170`) says what is inside it:

| tile-centre phase | columns | staple crossovers | ties |
|---|---|---|---|
| 8 and 24 (2 of 32) | 8 admitted / 6 refused | 42 | 56 |
| every other phase (30 of 32) | 7 | **49** | 63 |

So a band from 49 to 56 admits **every phase there is** — it is a tautology on this tile, and it
reads as a check. Worse, the two endpoints are not neighbours in any design sense: their staple
column sets are **disjoint** (`CH-0219`), so the band's two ends are objects that share nothing but
their footprint.

## The general form

**An acceptance band must be a tolerance on one quantity of one object.** Where its endpoints are
the values that *different* objects take, the band cannot discriminate between those objects, and
passing it carries no information about which one is in hand. This is the same failure the corpus
already records for a **percentage** — *"a percentage is an error bar on the thing its denominator
is"* — one level up: a **range** is an error bar on the thing its endpoints are values *of*, and
here they are values of two things.

It is also `CLAUDE.md`'s *"a gate's own regular expression is the first thing its cheap bound must
test"*, met on a numeric gate rather than a textual one: the discriminator between the two lattices
is one call to a function already in the tree, and the check that was written could not fail.

## What would settle it

- **Assert against the corpus's own lattice, not against a literal.** `C-0170` shows the comparison
  is four lines: convert the design's columns to the tile-centre datum and compare against
  `rasterColumnLayout` at every phase. It returns a phase — **16** — where a band returns a boolean.
  `C-0161`'s `grillageImport` already does the conversion.
- **Quote a census, never a band, when the endpoints belong to different objects.** If the
  generator is meant to be free to draw any phase, it should say which phase it drew and the
  claim should carry it; if it is meant to draw the corpus's recommended one, the assertion is an
  equality and not a membership.
