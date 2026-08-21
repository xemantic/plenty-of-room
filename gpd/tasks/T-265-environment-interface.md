# T-265 — an `environment` interface, so the two packages with no counterpart in the field can be cited without the tile

| | |
|---|---|
| **Leaf** | none — step 4 of [`ARCHITECTURE.md`](../../ARCHITECTURE.md) |
| **Raised by** | the iteration-39 restructure, out of [`TOOLING-NOVELTY.md`](../../TOOLING-NOVELTY.md) |
| **Status** | see [`TASKS.md`](../../TASKS.md) |

---

## 1. Formulate

### Why this one is first

The tooling survey's result is that **the moat is the regime, not the ingredients**:

- oxDNA2 *does* carry salt-dependent electrostatics, but the parameterisation is *"restricted to salt
  concentrations of 0.1 M of monovalent salt or greater"*, and magnesium *"is not included in the oxDNA model"*.
  This device's whole operating range is **0.5–10 mM MgCl₂** — below the floor, in the ion that is excluded.
- mrDNA applies an external electric field, but solves **no electrode boundary**: no diffuse layer, no Stern
  series converting a diffuse-layer drop into an applied bias, no force balance whose root is the operating point.
- The review lists *"complex interactions with charged surfaces"* among what oxDNA *"cannot be fully represented by this model"*.

So `brush/` and `electrostatics/` are the parts of this tree with no counterpart in the field —
and today they are reachable only **through** `Gen1Tile`.
An interface is what lets them be validated, tested and handed over on their own.

### Numeric target and acceptance predicates

| | predicate |
|---|---|
| **P1** | one interface carrying `pressure(h)`, `force(h, bias)` and `decayLength`, with the SCF brush and the 2-D PB edge behind it |
| **P2** | the interface reproduces each package's own **committed** numbers at departure `0.0` — this is a re-expression, not a re-derivation, and anything else is a finding |
| **P3** | every existing caller still compiles and every existing study still emits **byte-identically**; the old entry points stay |
| **P4** | the interface carries the **regime** it is valid in (buffer, valency, gap, bandwidth) as data, not as prose, so that `T-268`'s regime block has something to serialise |
| **P5** | one test that consumes the interface **without constructing a tile**, which is the whole point of the step |

### Units and conventions

Unchanged: nm, pN, pN/nm, `k_BT = 4.141947 pN·nm` at 300 K, aqueous buffer with stated Mg²⁺.
**No committed number may move.**

---

## 2. Plan

Additive. Define the interface, implement it over what exists, change no caller.
`P2` and `P3` together are what make that claim checkable rather than asserted.

`P4` is the part worth thinking about rather than typing: a regime is not a validity *sentence*, it is the
tuple a downstream consumer has to be refused on. `CLAUDE.md` records the cost of getting this wrong —
*"the Debye length" is three different numbers in this project and all three are correct in their own place*.
The `quantities/` layer built in this same restructure is the natural place for it to live, and this task is
the first consumer that layer will have.

### Cost

A day, and a re-run of nothing. `P3` is a full `tools/verify.sh`, not a spot check.

### What would falsify this approach

- **A committed number moves.** Then the interface is not a re-expression of what is there, and the difference
  has to be explained before it is adopted.
- **The interface cannot be satisfied without a tile.** Then the coupling between `brush`/`electrostatics` and
  `structure` is deeper than `ARCHITECTURE.md` assumes, and step 5 is the prerequisite rather than the sequel —
  which is a result about this repository worth filing on its own.
