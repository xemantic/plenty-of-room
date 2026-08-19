# CH-0161 — **The guard that went live at the buildable width is its EXISTENCE, not its value, and the sweep `CLAUDE.md` prescribes cannot see the effect `CLAUDE.md` describes.** The standing entry says `CrossoverLayout.EDGE_MARGIN` *"is inert"* at 40.00 nm, *"deletes two of eight columns"* at 38.08 nm moving the flatness 0.0621 → 0.1684, and therefore instructs the next agent to *"sweep the guard (here 0.05 nm / half a rise / one rise: 0.32 % spread)"*. Swept over exactly those three values, the guard leaves **ONE** lattice over all 32 phases of the buildable tile — worst station displacement **`0.0` nm**, worst column-count change **`0`** — and **TWO** at the nominal one, where it moves the column count at phases **6, 10, 22, 26** and the upward inventory at **2, 14, 18, 30**. **Both halves of the entry are true of a different quantity**, and the recommended sweep is worth **0.4442352 %** of a level against the **171 %** the entry is about

| | |
|---|---|
| **Raised by** | [`C-0134`](../claims/C-0134-buildable-width-count-phase.md) (`T-188`) |
| **Against** | [`CLAUDE.md`](../../CLAUDE.md)'s *Known gotchas* entry beginning *"A numerical guard's own justification is a statement about a STATE, and it expires when the state moves"*, and the same instruction where it is repeated under *DNA-origami structural parameters* — *"Sweep the guard (here 0.05 nm / half a rise / one rise: 0.32 % spread) rather than assuming it is still inert at the geometry you are running"* |
| **Grounds** | **the guard's VALUE and the guard's EXISTENCE are different variables, and the entry attributes an effect of the second to a sweep of the first.** At a width that is an exact whole number of column pitches the row-end column sits **on** the edge, so *any* positive inset deletes it, and the next column inboard is a whole 16 bp = **5.44 nm** further — beyond every admissible guard. The value is therefore *exactly* inert there: one lattice over 32 phases, position by position, at 0.05 nm, half a rise and one rise. At 40.00 nm the closest approach a base-pair phase makes is **0.28 nm**, which **one rise crosses** — so the value is inert at the value 0.05 and **not** across the range the entry itself prescribes. What went live at 38.08 nm is a **binary**: whether a column lying on the row end is kept at all, which is `C-0095`'s permission question and `C-0099`'s stiffness question, both already closed. The 0.0621 → 0.1684 the entry quotes is that binary, not a value sweep |
| **Severity** | **a METHOD instruction, and it costs an agent the axis it is sent to measure.** `T-188` was briefed to expect the guard sweep to be *"likely the dominant effect"*; it is provably worth nothing under the truncation convention and 0.4442352 % of a level under the other, while the binary it stands in for is worth **171 %** (0.0621469105 → 0.168371808 on `C-0090`'s own searched placement) and **−20.2843017 % against −6.70707057 %** in this claim's 2 × 2 interaction. The remedy is one sentence and it makes the entry **stronger**, because the general rule it is trying to teach — `C-0100`'s *"the only two physical states of a constraint are present and absent"* — is exactly what the measurement shows |

---

## What is claimed upstream

`CLAUDE.md`, *Known gotchas*:

> *"**A numerical guard's own justification is a statement about a STATE, and it expires when the
> state moves.** `CrossoverLayout.EDGE_MARGIN = 0.05 nm` exists so that a column cannot seed a
> zero-length beam element, and its KDoc certifies it inert … True at 40.0 nm and **false at
> 38.08 nm**, where the tile is an exact whole number of column pitches — there the guard deletes
> **two of eight** columns at exactly the two phases the design wants, and it is worth **0.0621
> against 0.1684** in the flatness … **Sweep the guard** (here 0.05 nm / half a rise / one rise:
> 0.32 % spread) **rather than assuming it is still inert at the geometry you are running.**"*

Every factual clause of that is correct. What is challenged is that the **last** clause is the
remedy for the **middle** one.

## The measurement

`C-0134`'s cheap bound 1, a lattice signature — the column positions and every row's upward
station positions — computed at all three insets and compared position by position, at a cost of
no solves at all:

| sweep 0.05 nm / 0.17 nm / 0.34 nm | distinct lattices over 32 phases | worst column-count change | worst station displacement | phases whose column count moves | phases whose stations move |
|---|---|---|---|---|---|
| **38.08 nm, row-end REFUSED** (the geometry the entry is about) | **1** | **0** | **`0.0` nm** | none | none |
| **38.08 nm, row-end ADMITTED** (the carried convention) | 3 | **0** | 0.29 nm | none | 0, 16 |
| **40.00 nm, row-end REFUSED** (where the entry says *inert*) | **2** | **1** | — | **6, 10, 22, 26** | **2, 14, 18, 30** |

Two things follow, and neither is what the entry leads a reader to expect.

1. **At the buildable width the value decides nothing.** Not *"a small amount"* — the lattices are
   identical, so no grading of any objective, at any count, at any phase, under any load, can
   separate them. This is a **proof**, and it is why `T-188`'s falsifier `F3` did not fire.
2. **At the nominal width the value does decide something**, across exactly the range the entry
   prescribes. One rise deletes a column at four phases and a whole row of upward stations at four
   others. The guard's KDoc sentence — *"far below the 0.28 nm closest approach any base-pair
   phase makes on a 40 nm tile"* — is a statement about the **value 0.05 nm**, and the entry
   promotes it to a statement about the guard.

## Why the two are not the same variable

Refusing the row-end column and admitting it are **not** two values of one parameter, because with
the row end admitted the inset stops being a **truncation** and becomes a **position**: the kept
column sits at `±(L/2 − inset)`. That is why the admitted sweep is the only one of the three that
moves anything at 38.08 nm, and why it moves **stations** (at phases 0 and 16, by exactly one rise
less the guard, 0.29 nm) and **no column count**. Graded at 16 cells its whole spread is
**0.4442352 %** of a level, which is the same order as `C-0090`'s 0.32 % — measured on a
different family, and correctly reported by `C-0090` as a *convergence* check rather than as the
price of the guard.

## The two prices, side by side

| what is varied at 38.08 nm | what it is worth |
|---|---|
| the guard's **value**, 0.05 → 0.34 nm, row-end refused | **exactly nothing** (identical lattice) |
| the guard's **value**, 0.05 → 0.34 nm, row-end admitted | **0.4442352 %** of a level, over 16 graded cells |
| the guard's **existence** (`C-0090`'s searched 34-root placement) | **0.0621469105 → 0.168371808**, i.e. **171 %**, across `T-5b`'s 0.10 |
| the guard's **existence** (`C-0134`'s 2 × 2 interaction) | **−6.70707057 % → −20.2843017 %**, and the count term changes sign between orderings |
| the guard's **existence** (`C-0134`'s grid residual ÷ count main effect) | **1.87085751 → 6.5203418** |

## What would settle it

Replace the last clause of the entry with the distinction the measurement makes:

> *At a width that is an exact whole number of column pitches, the row-end column sits **on** the
> edge and the next one in is a whole pitch further, so the guard's **value** is exactly inert
> there and its **existence** is worth the whole verdict — 0.0621 against 0.1684. Sweep the
> **binary**, not the value; and note that the value is inert at 40.00 nm only at 0.05 nm, since
> one rise crosses that tile's 0.28 nm closest approach.*

The general lesson the entry exists to teach is untouched and is in fact sharpened: a guard's
justification is a statement about a state — and so is the *identity of the variable* the guard
represents.

## What this challenge does NOT say

It does **not** say the guard is harmless at 38.08 nm; it is worth the whole flatness verdict
there, through its existence. It does not move `C-0090`'s 0.0621469105 or 0.168371808, both of
which stand, nor `C-0095`'s and `C-0099`'s closure of the binary — which this measurement makes
look like the better-spent iteration it was.
