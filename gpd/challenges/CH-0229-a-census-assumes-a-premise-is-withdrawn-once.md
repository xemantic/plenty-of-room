# CH-0229 — a corpus census assumes a premise is withdrawn ONCE, and this corpus has withdrawn one in HALVES two iterations apart: `C-0144`'s five families share a single pointer set, and one of them needed two

| | |
|---|---|
| **Against** | [`C-0144`](../claims/C-0144-honeycomb-correction-supersession.md) §9 and the tool it retains, [`tools/T-234-census.py`](../../tools/T-234-census.py): five premise families, **one** `POINTERS` tuple, and a gate that demands a pointer from that one tuple for every `MOVED` or `DISCHARGED` occurrence |
| **Raised by** | [`C-0176`](../claims/C-0176-partial-discharge-and-restatement-predicates.md) / [`T-260`](../tasks/T-260-partial-discharge-predicate.md) |
| **Kind** | **methodological — a data structure that cannot express a fact the corpus contains.** No entry of `C-0144`'s census is wrong and no number of it moves; what it could not represent is that a premise can be withdrawn in parts, by different claims, on different dates |
| **Status** | **ANSWERED.** Filed and repaired in the same claim by [`C-0176`](../claims/C-0176-partial-discharge-and-restatement-predicates.md) (`T-260`) — the repair is `DISCHARGES` plus a per-family discharge — and ~~what stays open is the general question below, which is about every census this repository writes and not only this one~~ **the general question too is ANSWERED** by [`C-0182`](../claims/C-0182-name-the-discharge.md) (`T-281`), which makes the declaration a **registry whose getter refuses** an undeclared family and gives the report a third state, and measures the requirement over the census's own revision history |

---

## The measurement

`C-0141` supplied the honeycomb **station lattice**, the **plan ceiling**, the **placement family** and the price of an **oblique root**.
It did **not** supply a **grillage**: `OrigamiGrillage` never reads `layers`,
so every coupled cell in this corpus stayed a smeared single-layer square-lattice solve
until [`C-0154`](../claims/C-0154-honeycomb-grillage.md) built one (`T-253`) and [`C-0167`](../claims/C-0167-coupled-cells-on-the-honeycomb-grillage.md) re-graded onto it — **two iterations later**.

The census's `PLACEMENT` family matches one string, `single-layer square-lattice`, for both halves.

| | |
|---|---|
| occurrences of that string in scope | **38** |
| of those, statements about the **structural model** — the half `C-0141` did not supply | **17** |
| statements about the **placement inventory** — the half it did | **13** |
| the token used **attributively** of an object that genuinely is a square-lattice sheet | **8** |
| flagged by the gate as a `C-0140`/`C-0141` debt at a fresh regeneration of the standing emitter, and live and correct statements about the **grillage** | **2** (`C-0167`'s headline and `C-0172`'s `LatticeTag` row; a third gate defect is `T-262`'s restatement class) |
| files registered in the emitter's `CORRECTING`/`OUT_OF_SCOPE_FILES` sets **only** to hide those | **4** |

Two of those four registrations carry a comment in the emitter naming them as false positives.
A set membership silences a symptom; it does not repair a predicate,
and `CLAUDE.md` records the cost directly — *a drift checker's FALSE positives cost more than its true ones, because the tool exists in order to be believed.*

## Why this is not a nitpick

**The thing that could not be represented was not a pattern, it was the pointer set.**
The `PLACEMENT` pattern is one string and `C-0141` discharged half of what it matches,
so no regular expression over that string can be right — a fact available in seconds, before any predicate is written.
What has to change is that a **family carries its own discharge**:
a census is defined by **the discharge it is about**, and a token that spans two discharges belongs to **two censuses**.

That reframing is what makes the repair small (one map, one field) instead of a keyword arms race,
and it is what makes the gate honest: `T-234`'s census gates its own subject and **prints the other half beside it, ungated, naming the census it belongs to**.

## What is challenged, and what is not

**Not challenged:** every entry of `C-0144`'s census, its five families, its 41-entry work list, and the verdict of `T-234`. All stand.

**Challenged:** the assumption underneath, which nothing in `C-0144` states —
that a premise family has **one** correcting claim and therefore **one** pointer set.
It has now been false once, for two iterations, on the family that carries the corpus's whole coupled-flatness line.

## The general form, which is the part that stays open

`CH-0182` says a census is **dated by its premise set**.
This is the same observation on the other axis: a census is **dated by its DISCHARGE**, and a discharge can be partial.
The two compose badly — a premise withdrawn in halves has two dates, and the census inherits the earlier one.

Both are properties of *every* census this repository writes, not of this one,
and neither has a mechanical detector.
What `C-0176` supplies is the *representation* — the second date can now be written down — and not the discovery:
nothing yet tells an author that the family they are adding has two correcting claims rather than one.

## What would settle it

A rule, one line, in whatever writes the next census:
**name the claim that discharges each family, one family at a time, and refuse a family whose discharge you cannot name.**
The census then cannot be written without asking the question,
which is the difference between a convention and a mechanism —
and `CLAUDE.md` already records, four times, that a convention is not a mechanism.
