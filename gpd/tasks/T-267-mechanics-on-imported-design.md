# T-267 — mechanics on an **imported** design, so a placement result stops being a property of one tile

| | |
|---|---|
| **Leaf** | none — step 5 of [`ARCHITECTURE.md`](../../ARCHITECTURE.md) |
| **Raised by** | the iteration-39 restructure |
| **Status** | see [`TASKS.md`](../../TASKS.md) |

---

## 1. Formulate

### What it costs not to have done this

`OrigamiGrillage` takes its lattice from `Gen1Tile`'s constants.
So every placement, phase, plan-ceiling and flatness number in this corpus is a property of **a set of
constants**, not of a design — and when iterations 33–34 found that the four-layer cross-section every one of
those numbers was solved on **is not a honeycomb** (every `edgeY` exactly 1.5× too small), the results were
**invalid rather than re-runnable**.

That is the whole cost of the missing seam, and it has already been paid once.

### Numeric target and acceptance predicates

| | predicate |
|---|---|
| **P1** | `OrigamiGrillage` and `HoneycombGrillage` constructible from a `ScadnanoDesign`, or from a lattice plus a cross-section |
| **P2** | the existing `Gen1Tile` constructor **retained**, so the step is additive |
| **P3** | at least one existing placement or flatness study re-run through the new path reproducing its committed result **bit for bit** — and note `CLAUDE.md`'s own caveat that bit-identity is assertable on `assembleLoad` and not on a solved field, so the load vector is the object to compare exactly and the solved field at `1e-10` |
| **P4** | the imported path exercised on a design the corpus did **not** produce — the `.sc` of `C-0157`'s oxDNA run — and whatever it reports recorded, whether or not it is flattering |

### Units and conventions

Unchanged. **No committed number may move**; `P3` is how that is checked.

---

## 2. Plan

Additive if `P2` holds. The work is a constructor and a mapping, not a model change.

`P4` is the one that turns this from plumbing into a result: it is the first time this repository grades a
design it did not itself parameterise, which is the capability the tooling survey says nothing in the field has.

### Cost

Depends almost entirely on how much of the lattice `OrigamiGrillage` reads implicitly.
`CLAUDE.md` already records the answer for one direction — it takes **exactly five scalars** from its sheet and
**never reads `layers` or `interlayerCoupling`** — so the surface is smaller than it looks, and that note is the
cheap bound to re-read before starting.

### What would falsify this approach

- **`P3` does not reproduce.** Then the new path is not the same object; find out which of the five scalars
  moved before adopting anything.
- **The imported design cannot be expressed.** Then the grillage's lattice assumptions are narrower than the
  designs the field draws, which is worth stating explicitly — it is the same class of finding as
  *`OrigamiGrillage`'s interfaces are a path graph and a honeycomb block's are not*.
