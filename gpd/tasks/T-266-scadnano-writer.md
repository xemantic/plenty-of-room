# T-266 — a scadnano **writer**, so the recommended tile is a file somebody else can open

| | |
|---|---|
| **Leaf** | none — completes layer 3 of [`ARCHITECTURE.md`](../../ARCHITECTURE.md) |
| **Raised by** | the iteration-39 restructure, out of [`TOOLING-NOVELTY.md`](../../TOOLING-NOVELTY.md) |
| **Status** | see [`TASKS.md`](../../TASKS.md) |

---

## 1. Formulate

### The asymmetry this closes

`ARCHITECTURE.md` records layer 3 as **exists (reader; no writer yet)**, and the reader is already proved:
`ScadnanoDesign.fromResource` derives **15 duplexes, 112 bp, phase 8, seven columns, the 4/3 parity split and
49 crossovers** from the `.sc` file `C-0157`'s oxDNA run actually simulated —
a reproduction of this corpus's own counts across two implementations in two languages, not a restatement.

The survey's finding is that **every** tool in the field reads *and writes* a design:
caDNAno, scadnano, ENSnano, Adenita, MagicDNA, DNAforge, all exporting to oxDNA/oxView.
This programme's recommended tile is a set of Kotlin constants, so it
**cannot be handed to anybody without a human redrawing it** — and a design somebody redraws is not the design
that was graded.

### Numeric target and acceptance predicates

| | predicate |
|---|---|
| **P1** | `ScadnanoDesign.write()` emitting a `.sc` that scadnano loads without warnings |
| **P2** | a **round-trip** test — `read → write → read` — reproducing every lattice fact the reader derives at departure `0.0`, on the file `C-0157` simulated |
| **P3** | the recommended `10 × 6` honeycomb block emitted as a **committed artifact**, and its derived facts asserted against the corpus's own numbers for it |
| **P4** | `checkBuildability()` run on the written file and passing — a design this repository emits must satisfy the rules this repository checks, or one of the two is wrong |
| **P5** | the artifact carries which claim recommended it and at which width reading, because §3's `40 × 40 nm` is still two readings apart (decision 8, `T-242`) |

### Units and conventions

Base-pair counts and lattice indices are integers and must round-trip **exactly**; no tolerance is admissible
on `P2`. Lengths in nm where the file carries them, at this repository's own rise and pitches.

---

## 2. Plan

The reader already contains the hard half — the lattice derivation. Writing is the inverse map, and `P2` is
what proves it is the inverse rather than a plausible second implementation.

`P4` is the payoff and it is worth stating plainly: the survey found **nothing in the field that checks a
design against a device specification**. caDNAno will let you draw a row width a boustrophedon cannot turn at;
this repository knows which widths those are (`C-0086`, `C-0136`, `C-0140`, and the `±5 bp` closure rule).
A writer makes that knowledge exportable instead of internal.

### Cost

Days rather than a day, because `P1` is a compatibility claim about somebody else's format and the only honest
test of it is loading the file in scadnano. Budget for one round of format surprises.

### What would falsify this approach

- **The round trip is not exact.** Then the reader is lossy and the counts it derives are a projection, which
  would put `C-0157`'s cross-implementation reproduction in a different light.
- **`checkBuildability()` fails on our own recommended tile.** That is the most valuable possible outcome of
  this task and it must be filed as a finding, not repaired quietly: it would mean the recommendation and the
  rules disagree, and the corpus has been grading a design its own drawing rules refuse.
