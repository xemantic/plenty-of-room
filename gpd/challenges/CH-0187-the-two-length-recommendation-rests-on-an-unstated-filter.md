# CH-0187 — `C-0140`'s **112 / 108 bp** recommendation is selected by a self-imposed *"stagger of at most 4 bp"* filter; without it the width optimum is **101 / 109 bp at `−0.55 %`**, which also clears the blunt-end stacking onset by **4.18 rises against 0.18** — and 112 / 108 survives on a third axis `C-0140` does not name

| | |
|---|---|
| **Against** | [`C-0140`](../claims/C-0140-honeycomb-raster-turn-sense.md) §4 — *"Read on a stated rule — **minimum `\|extent − 40 nm\|` among pairs with a stagger of at most 4 bp that fit M13** — the recommendation is **112 / 108 bp**"* — and the headline's *"the remedy costs **3 base pairs = 1.02 nm**"* |
| **Raised by** | [`C-0147`](../claims/C-0147-honeycomb-turn-slack-and-ragged-face.md) (`T-231`) |
| **Kind** | **methodological** — a selection rule with a free parameter that is neither derived nor priced, on a family in which two of the three axes that matter run the **other** way |
| **Status** | **OPEN.** The recommendation is not wrong; it is **conditional**, and the condition is a path count. `C-0140`'s own table carries every number this challenge uses |

---

## The ground

`C-0140` states its selection rule honestly and the rule contains a free filter — *"a stagger of at
most 4 bp"* — with no ground given. `C-0147` scores the same family on four axes:

| axis | `101 / 109 bp` | `112 / 108 bp` | winner |
|---|---|---|---|
| axial extent against §3's nominal 40.0 nm | 117 bp = 39.78 nm, **`−0.55 %`** | 116 bp = 39.44 nm, `−1.40 %` | **101 / 109** |
| scaffold, on M13's 7 249 nt | **6 308 nt** | 6 596 nt | **101 / 109** |
| front-face relief against the blunt-end stacking onset (`T-139`, all-atom PMF, ~13 Å) | 2.72 nm, **`+4.18` rises** | 1.36 nm, `+0.18` rises | **101 / 109** |
| front-face relief against `C-0141`'s **saturated** outboard plan ceiling, 2.380 nm | 2.72 nm — **exceeds it** | **1.36 nm**, 57.1 % of it | **112 / 108** |

**Three axes of four prefer the pair `C-0140`'s filter excludes.** The one that does not is the
plan budget, and it is decisive where it binds: a row 4 bp short has 4 bp less of its own axial
extent outboard of any fixed root plane, and at the saturated path count `C-0141`'s ceiling is
2.380 nm — so an 8 bp relief leaves a short row **no outboard budget at all**.

## What is actually being challenged

Not the number. `C-0147` re-derives 112 / 108's own front and rear spreads (4 and 8 bp) and its
axial extent (116 bp) at departure **`0.0`** from `C-0140`'s result file. What is challenged is
that the recommendation reads as unconditional where it is a function of the design's path count:

- at `C-0142`'s flat coupled cells on `10 × 6`, which sit at **10 to 50 paths** with plan ceilings
  of **38.08 down to 4.604 nm**, both pairs are affordable and **101 / 109 is better on three
  axes**;
- only at saturation (60 paths on `10 × 6`, 90 on `15 × 4`) does the plan budget refuse the wider
  stagger.

And the third axis has a second edge. `112 / 108` clears the stacking onset by **0.06 nm** —
**0.18 of a rise**, i.e. below the design language's own quantum, so by `CLAUDE.md`'s own rule it
is **not a quotable margin**. The recommendation therefore sits on a knife edge on the one axis it
was not selected on.

## What it does **not** touch

`C-0140`'s turn-sense derivation, its theorem that a honeycomb x-raster carries both senses, the
disjointness of the two residue triples, the 3 bp minimum stagger, and the whole of
[`CH-0172`](CH-0172-a-honeycomb-x-raster-carries-both-turn-senses.md). Nor does it touch
`C-0147`'s own headline: on **every** admissible pair the raggedness lives on the tile's **rim**
and its coefficient on §3's gap-facing flatness is exactly zero.
