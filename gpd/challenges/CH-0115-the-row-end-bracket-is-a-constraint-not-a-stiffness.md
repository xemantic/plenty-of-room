# CH-0115 — **`CH-0111`'s bracket varies THREE things and only one of them is a stiffness, so 97 % of its width is not reachable by any row-end crossover that exists.** `C-0090`'s two end-of-row readings differ in the dihedral spring `k_θ`, in the vertical link **and** in the mesh node. Decomposed at 38.08 nm / phase 8 the 0.0621469105 → 0.168371808 interval is **2.85 % the dihedral spring, 97.40 % the vertical link and −0.25 % the node** — and the vertical link is a **constraint** expressing that the backbone is covalently continuous across the interface, which `C-0095` has already settled that it is. **Sweeping the only elastic element of the three over its whole range, from an interior crossover's value to exactly zero, moves the best 34-root dishing 0.0621469105 → 0.0651753854, i.e. 2.85 % of the interval and nowhere near `T-5b`'s 0.10.** A stiffness bracket whose width is 97 % constraint is not a stiffness bracket

| | |
|---|---|
| **Against** | [`CH-0111`](CH-0111-the-row-end-crossover-is-admitted-with-an-interior-crossover-s-stiffness.md)'s **Ground 2** — *"the two 'conventions' are a bracket, and it is a wide one"*, and its arithmetic *"`T-5b`'s 0.10 lies at 0.356 of the way from the admitted reading to the refused one … so a row-end crossover retaining less than roughly a third of an interior one's contribution would take the design out of the convention"*; and, through it, [`C-0090`](../claims/C-0090-buildable-raster-width.md)'s validity-range sentence that *"the end-of-row convention is a MODELLING CHOICE and both readings are carried"* |
| **Raised by** | [`C-0099`](../claims/C-0099-row-end-crossover-stiffness.md), task [`T-164`](../tasks/T-164.md) — the task `CH-0111` itself queued, executed as `CH-0111` itself ranked it |
| **Grounds** | **methodological** — a one-parameter bracket read off a two-model comparison that moves more than one parameter. `CLAUDE.md` already records the general form of the error (*"a crossover is TWO elements and only one of them is `D_⊥`"*, and *"scaling `D_⊥` is not a model of removing crossovers"*); this is the same trap in reverse, and `CH-0111`'s own *"what would settle it"* item 2 names it without drawing the consequence |
| **Status** | **STANDS, and it DISCHARGES the challenge it is raised against.** `CH-0111`'s question — *"is the row-end crossover as stiff as an interior one?"* — is answered in the only way that matters to the acceptance: **it does not matter**. Nothing in `C-0090`'s or `C-0095`'s verdicts moves; what moves is the *width of the exposure*, from 2.70925468× to **1.0487309×** (`0.0651753854/0.0621469105`) |

---

## What `CH-0111` asserts

> *"Refusing the column removes the node, the dihedral spring **and** the vertical link. Admitting
> it supplies all three at an interior crossover's value. A row-end crossover of any intermediate
> stiffness therefore lies **between** the two readings already computed."*

The premise is exactly right and the inference does not follow.
*"Any intermediate stiffness lies between the two readings"* is true of a scalar interpolation
between two lattices; it is not true of the **physical** family the challenge is asking about,
because that family varies only one of the three ingredients.

## The decomposition, measured on `C-0090`'s own pipeline

38.08 nm, phase 8, `C-0090`'s 24-rise 8.16 nm arm, 34 roots, the exhaustive centro-symmetric
family (163 296 placements) re-enumerated at each state.

| state of the 14 row-end crossovers | dishing / stroke | share of `CH-0111`'s interval | inside `T-5b`'s 0.10? |
|---|---|---|---|
| `k_θ` interior, link, node — **`C-0090`'s admitted reading** | **0.0621469105** | — | **yes** |
| `k_θ = 0`, **link and node retained** | **0.0651753854** | **2.85 %** — the dihedral spring | **yes** |
| `k_θ = 0`, **link removed**, node retained | **0.168640591** | **97.40 %** — the vertical link | no |
| the column deleted, on its own six-column host — **`C-0090`'s refused reading** | **0.168371808** | **−0.25 %** — the node | no |

**The vertical link is 34× the dihedral spring**, and it is not a stiffness at all: `C-0009`'s own
words are that it *"is a **constraint** tying two duplex surfaces together and carries no rigidity
at all"*, carried by a penalty whose value the answer must not depend on. Sweeping that penalty is
a **step and not a ramp** — at one eighth of its value the constraint is still enforced and the
lattice reads 0.0651072886, against 0.168640591 at exactly zero.

So the 2.70925468× that `C-0090` reports between its two readings is a statement about **whether
two duplexes are tied at their last base pair**, which is a covalent binary, and `C-0095` has
already answered it: the raster turn is a strand passing from one helix to the next, and Rothemund
reports that *"the last base pair does form and assumes a planar configuration"*.

## What this costs `CH-0111`'s own arithmetic

`CH-0111` computes `(0.10 − 0.0621469105)/(0.168371808 − 0.0621469105) = 0.356` and reads it as
*"a row-end crossover retaining less than roughly a third of an interior one's contribution would
take the design out of the convention"*. **Read on the reachable set that inference has no
referent**: the reachable set ends at 0.0651753854, `T-5b`'s 0.10 is not inside it, and a row-end
crossover retaining **none** of an interior one's dihedral spring still leaves **34.8 %** of the
convention unused. The 0.356 is reproduced here as 0.356348561 and is arithmetically
correct; what is wrong is the set it is a fraction of.

## What is NOT challenged

- **`CH-0111`'s Ground 1.** Rothemund does say the edge strain is unrelieved and its relief
  *"unknown"*, and no accessible source gives a row-end crossover's `k_θ`. That remains true, and
  `C-0099` delivers a ceiling and a threshold rather than a value (`P-6`).
- **`CH-0111`'s Ground 3.** It is 14 of 56 crossovers, a quarter of the across-helix load paths,
  and they sit where the collar's gradient is largest. That is why the sweep was worth running.
- **`C-0090`'s and `C-0095`'s verdicts.** Both stand, and this challenge strengthens the first:
  0.0621469105 is now the stiff end of a **1.0487309×** band rather than of a 2.70925468× one.
- **`C-0009`'s uniform crossover element.** It is not vindicated, it is shown to be *immaterial
  here* — a different `k_θ` at 14 of 56 sites is worth 4.9 % of a flatness that has 38 % of margin.

## What would overturn this challenge

1. **A row-end crossover that does not tie its two duplexes vertically** — a frayed or melted
   terminal region rather than a torsionally strained one. Then the link is absent as well as the
   spring, the state is channel B's `s = 0`, and `C-0090`'s refused reading is right after all.
   Rothemund's *"the last base pair does form"* is the evidence against it and it is one sentence.
2. **A prestrain term.** This decomposition is **linear**: it varies elastic constants and carries
   no initial stress. Rothemund's *"crossovers in tension"* is a **prestrain**, and no element of
   `C-0009`'s lattice has a term for one. That is a real gap and it is `T-172`, not a defect in
   this arithmetic.
