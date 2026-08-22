# CH-0211 — **`C-0148`'s closure predicate does NOT need the turn senses, and an imported `.sc` carries everything it does need** — so the reason `C-0160`'s validity range gives for withholding the honeycomb rule is withdrawn. Closure is `(level − 7·class) mod 21`, and a file states the **level** in its domain boundaries and the **class** in its `grid_position`: 59 crossovers of the committed block reduce to `{4, 14}`, one `b₀`, **zero forced**, with no turn sense read anywhere. The **verdict** `C-0160` recorded — that the honeycomb branch withheld rather than answered — was correct when written; the **ground** it was written on is not, and a reader taking it at face value would have concluded the derivation was blocked by the format

| | |
|---|---|
| **Against** | [`C-0160`](../claims/C-0160-scadnano-writer.md) §9, *Validity range*, final bullet — *"It does **not** implement `C-0148`'s closure predicate from the file — **that needs the turn senses, which the `.sc` format does not carry in a form this reader derives** — so a honeycomb design's drawability is still answered by `HoneycombRasterResidues` and not by the imported file"*; and §10's *Still open* item stating the same |
| **Raised by** | [`C-0164`](../claims/C-0164-lattice-aware-buildability.md) (`T-270`) |
| **Grounds** | **logical** — the closure condition is a function of two quantities, and both are read off the file: integer arithmetic over 59 crossovers, reproduced three times (this repository's construction, its new file-derived predicate, and an independent Python derivation straight out of the committed `.sc`) |
| **Status** | **RAISED and REPAIRED in the same iteration.** `C-0160`'s verdicts, its round trip, its artifacts, its counts and its mutation table are all consumed unmodified and **none of them moves**. What is withdrawn is one sentence of its validity range |

---

## The observation

`C-0148`'s closure is: reduce every raster crossover by its own bond class, `(level − 7·class) mod 21`,
and one lattice constant `b₀` must serve all of them, so at most **two** residues survive and they
are exactly ten apart.

That expression names two quantities and neither of them is a turn sense.

- **`level`** — the axial position of the crossover. `HoneycombRasterResidues` obtains it by walking
  a turn-sense ladder, because it is building the raster from two row lengths and has nothing else.
  A **file** states it: a raster crossover sits on the edge of the axial window the helix turns at,
  `end` for a forward domain and `start` for a reverse one, and the two sides of one crossover agree
  on it (asserted, not assumed).
- **`class`** — the neighbour class of the bond. It is a property of the **cross-section**, and
  scadnano's `grid_position` is the cross-section: `honeycombCellOfGridPosition` inverts scadnano's
  own published position map onto this corpus's integer cell, which `C-0160`'s own `F4` had already
  checked in the forward direction at departure `0.0`.

The turn sense enters `HoneycombRasterResidues` because a **construction** needs to know which way
each helix runs before it can place the next crossover. An **import** does not construct anything —
the crossovers are already placed, and where they are is what the file is.

## What it is worth

| | `HoneycombRasterResidues` (construction) | `honeycombClosure()` (from the file) |
|---|---|---|
| raster crossovers | 59 | **59** |
| distinct reduced residues | `{0, 10}` on the corpus's `z` | **`{4, 14}`** on the file's datum |
| `b₀` candidates | `{5}` | **`{9}`** |
| forced crossovers | 0 | **0** |

The two datums differ by the 4 bp the emission shifts, and a global shift moves every reduced residue
alike — which is the property that makes the condition convention-free, and the reason a design
nobody here drew can be graded at all.

And the predicate refuses: read out of a **drawn** `112 / 108` block — `C-0140`'s recommendation,
withdrawn by `C-0148` — the file-derived closure returns exactly the **10** forced crossovers
`C-0148` derived from the construction.

## Why this is a challenge and not a footnote

`C-0160` filed the lattice-blind check honestly, left it measurable, and named the repair as open.
The **verdict** was right. But the sentence attached to it is a statement about what the `.sc`
**format** can support, and it is the kind of sentence a later task reads as a reason not to try —
`CLAUDE.md`'s own *a verdict that survives can survive on a different reason*, and *re-check the
ground of every verdict whose premise is withdrawn*. Here the ground is the thing that was wrong,
and the correction is the whole content of `T-270`'s honeycomb branch.

Nothing in `C-0160` is repriced. No number in it moves, its two committed artifacts are byte-identical,
and its own `F2` — the fired falsifier that raised `T-270` — is upheld exactly as filed.
