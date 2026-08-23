# `T-300` — a scaffold LENGTH is not a scaffold PROVENANCE, and one census family carries both

| | |
|---|---|
| Leaf | — (corpus hygiene; the instrument `T-233` reads) |
| Raised by | [`C-0193`](../claims/C-0193-the-built-turn-is-a-tether.md) (`T-296`), after its own eight occurrences forced a hand override each |
| Verification type | logical (a predicate over the corpus), measured against a hand reading of every occurrence |
| Units | none — this task counts occurrences, not quantities |

## Formulate

`tools/T-234-census.py` gates five premise families.
Two of them have been **split** since they were written,
because one token was carrying two statements:

- `PLACEMENT` → `PLACEMENT` / `GRILLAGE` / `SQUARE` (`T-260`, `C-0176`);
- `WIDTH` → `WIDTH` / `ROW_SPAN` (`T-262`, `C-0176`).

The third family is a **single bare token with no line context and no refinement**.
Every occurrence of it in the corpus is therefore `MOVED` by rule,
and the only remedy a reader has is a **hand override** in `tools/T-234-classification.json`,
which `CLAUDE.md` calls a dated object.

The family conflates two statements that are about **different objects**:

- **the debt** — *which scaffold a 2009 caDNAno block was folded from*.
  That is the premise [`C-0140`](../claims/C-0140-honeycomb-raster-turn-sense.md) /
  [`CH-0173`](../challenges/CH-0173-the-built-block-turns-on-loops-not-crossovers.md) withdrew,
  settling design (i) at `60 × 126 = 7 560`;
- **not the debt** — *a scaffold **length** in a forward budget for an **unbuilt** Gen-1 tile*
  on [`C-0151`](../claims/C-0151-closing-raster-selection.md)'s `102 / 109` raster,
  an object that did not exist in 2009.
  [`C-0193`](../claims/C-0193-the-built-turn-is-a-tether.md) says so in its own prose:
  *"a statement about a Gen-1 tile nobody has folded,
  and not about which scaffold any 2009 block was folded from"*.

This is exactly `C-0176`'s shape, third instance:
**before widening a pattern, ask whether the thing it cannot see is a SECOND PREMISE,
in which case the pattern was never the right instrument.**

### The cost, measured rather than estimated

Iteration 46 spent **fifteen** hand overrides on this one family across three authors.
And the advisory debt line moves with them:
unmanaged, that pass read `28 of 93 = 0.301075269`
against `24 of 93 = 0.258064516` with the overrides in —
so the missing refinement is worth **four points of the published debt ratio**,
and it moves the ratio in the direction [`C-0179`](../claims/C-0179-the-debt-line-as-a-ratio.md)
says a *repair* pass should not.

### Acceptance predicate

Either

1. a `refine_scaffold` splitting the family by **what governs the token**, with
   - the refinement radius **swept to a plateau** rather than fitted, and the sweep reported;
   - **every** reclassification read individually, against a hand reading of all occurrences
     recorded before the rule was written;
   - **zero false positives** in the unsafe direction — an occurrence that IS the withdrawn
     premise must never be read as a budget, because that silently removes it from the gate;
   - mutation rows in **both** directions, each failing a **named** test;
   - `tools/T-234-census.py --check` exiting `0`; or

2. the stated decision that a name may govern the family after all, and why,
   with a named test that can hold the decision open.

## Plan

### The cheap bound, before any predicate is written

Two counts, both one pass over `census(root)` and neither needing a rule:

- **how many occurrences the family has** (70 at `HEAD`), and
- **how many of them a reader has already had to override by hand** (16).

If the hand-override count were small relative to the family,
a name could govern it and deliverable 2 would be the answer.
It is not: **16 of 70**, and 13 of the 16 were typed in a single iteration.

The second cheap bound is a **theorem about the instrument**, and it decides the sweep's shape
before the sweep is run.
A nearest-wins refinement is **monotone in its radius**:
a match found at radius `R` sits at distance `≤ R`, so enlarging the radius
can only *add* candidates further away, and can only change the verdict of an occurrence
for which **neither** word class matched at all.
So the sweep can only move occurrences **off** the default and never back,
and *"the plateau"* is well defined: the smallest radius at which the answer stops growing.
Combined with a default of **the debt**, that makes the smallest sufficient radius the safest one,
and it is asserted as a named test rather than argued.

### Method

1. **Read all 70 occurrences by hand first**, in a `±260` character window,
   into *debt* / *not the debt* / *borderline*, and record the labels **before** writing any rule.
   A rule measured against labels chosen afterwards measures nothing.
2. Write two word classes — provenance against budget — nearest-wins, **defaulting to the debt**.
3. Sweep the radius over `60 / 80 / 100 / 120 / 150 / 200 / 250 / 300 / 400 / 500 / 800 / 1200`
   and report the whole sweep, the flips at each step, and the false-positive and
   false-negative counts against the hand reading at every radius.
4. **Measure each candidate widening separately before adopting it** (`C-0176`):
   a widening's cost is its false positives, and a narrowing's is its false negatives.
5. Regenerate `tools/T-234-classification.json` and **emit the intermediate reading**
   (`C-0184`: a reading taken between a predicate change and its regeneration measures nothing,
   so it must be published rather than described).
6. Report the debt line before and after, in **both** denominators, and the hand-override count
   the change removes.

### What would falsify this approach

- **A false positive at any radius.** If any occurrence that genuinely asserts the withdrawn
  premise is read as a budget, the split is a silencer and must not ship: it removes an
  occurrence from a gate that exists to demand a pointer for it. Declared as `F1`.
- **No plateau.** If the answer keeps moving as the radius grows, the rule is reading
  neighbouring sentences rather than the token's own, and the constant would be fitted.
  Declared as `F2`.
- **A hand-override count that does not fall.** The task exists because a reader had to
  override sixteen occurrences; if the rule leaves sixteen, it has bought nothing. Declared as `F3`.
- **The gate stops coming clean.** `--check` must still exit `0`; a split that leaves an
  occurrence carrying a class its new family may not carry is a defect, not a finding. `F4`.
- **A mutation failing nothing.** A rule nothing asserts is not a rule (`C-0150`, `C-0176`). `F5`.

### `CH-0182`, which will bite

A claim explaining a family has to quote that family's own worked examples,
so writing the claim moves the number it reports.
**Both readings are published** — with and without this task's own artifacts —
and every sentence says which one it quotes.
And the queue row is written **without spelling the token**, so that the row asking for the
split does not add two occurrences of the thing it asks to have classified.
