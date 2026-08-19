# T-249 — a number emitted as a **string** is not rounded, and the census of that shape is the deliverable

| | |
|---|---|
| **Leaf** | none — a **process** task protecting the machine-readable artifact of every leaf |
| **Raised by** | [`C-0150`](../claims/C-0150-departure-spelling-set-and-the-wall-clock.md) (`T-225`), whose ten-file sweep watched `0.1686405908358076` become `…075` in three of `T-164`'s sentences on a run that changed nothing |
| **Status** | see [`TASKS.md`](../../TASKS.md) |

---

## 1. Formulate

### The defect

`JsonElement.roundedForResult` dispatches on the JSON **type** and passes strings through — correctly,
because a string is not a number.
So a `Double` interpolated into a sentence (`"… reads $x against …"`) reaches the file through
`Double.toString()`, at its full round-trip precision of up to seventeen significant digits,
inside a file that declares nine.

Two consequences, and the second is the one nobody has measured:

1. **The file is un-diffable.** `gpd/README.md` requires that a re-run which changes nothing produces no
   diff. A sixteenth-digit movement of a hot reduction — `CLAUDE.md`'s standing JIT observation — moves
   the sentence, so the diff certifies the JVM's warm-up schedule rather than the answer. Measured:
   `C-0150` saw exactly this in three `T-164` sentences.
2. **The file contradicts itself.** `T-164`'s numeric `sweep[0].bestDishingOverStroke` is `0.0651753854`
   and its own `findings[0]` sentence says `0.06517538540278571` — the same quantity to two different
   precisions, in one file, one of which no field of the file states.

### Numeric target and acceptance predicates

| | predicate |
|---|---|
| **P1** | a **shape** census over the committed corpus, run before any repair, reporting how many tokens, in how many string fields, in how many result files, carry more than `RESULT_SIGNIFICANT_DIGITS` significant digits |
| **P2** | the census is a retained tool with self-tests, and its false-positive rate is **measured** rather than asserted |
| **P3** | the census is **mutation-tested in both directions** (`C-0127`'s standard as `C-0150` raised it): narrowing the predicate must fail a named test, and widening it past its stated exclusions must fail a named test |
| **P4** | `anchoring/RowEndCrossoverStiffnessStudy.kt`'s prose is built from **rounded** values, with the digits and the floor chosen **per call site** (`C-0138`: the cure is a property of a call site), and `T-164` re-emitted |
| **P5** | what moved in `T-164` is reported **by kind** — prose / departure / other numeric / verdicts / booleans — against its **committed** version read out of `git`, and the staleness identity confirmed with a count |
| **P6** | a decision, with its ground, on whether the census is wired as a build-failing gate — and if not, the residue published **with its own cost** (`CH-0168`) |
| **P7** | every prose number in the re-emitted `T-164` equals the file's own numeric field for the same quantity, where the file carries one |

### Units and conventions

Nothing physical is computed. Units unchanged and untouched: nm, pN, pN/nm, pN/nm² = 1 MPa exactly,
`k_BT = 4.141947 pN·nm` at 300 K, aqueous buffer with stated Mg²⁺.
`T-164`'s re-emission re-runs its lattice solves; **no physics may move**.

---

## 2. Plan

### The cheap bound runs first, and it is exact rather than heuristic

The obvious census is source-side: parse `src/main/kotlin` for a bare `${…}` interpolation of a
`Double` inside a string literal that reaches a result field. That needs a type inference Python does
not have, and `C-0127`'s checker had to learn four false-positive classes before it was believable.

The **artifact**-side census is exact and needs no parsing at all: scan every committed
`gpd/results/*.json`, walk to every **string** value, and count decimal tokens carrying more than
`RESULT_SIGNIFICANT_DIGITS = 9` significant digits. A rounded number cannot exceed nine by
construction, so every hit is a number that did **not** go through the rounding layer. It runs in
seconds, over the committed corpus, and it bounds the blast radius before a single study is re-run —
which is `SESSION-PROMPT.md`'s cheap-bound rule applied to a process defect.

The source-side census is then run only as a **cross-check** on the one file being repaired, where a
type is readable by hand.

### What would falsify this approach

- **F1** — a re-emitted `T-164` moves a numeric field, a boolean or a verdict. Then the repair is not
  a rendering change and the claim that no physics moved is false.
- **F2** — the artifact census has a false positive: a token above nine digits inside a string that is
  **not** an unrounded number (a date, an identifier, a DOI, a deliberately exact literal).
  Measured by reading every distinct token class, not asserted.
- **F3** — a prose number in the re-emitted file disagrees with the file's own numeric field for the
  same quantity. Then the per-site digit choice is wrong.
- **F4** — the census, run after the repair, still finds a hit in `T-164`.
- **F5** — a mutation of the census predicate passes every test. Then the tests are testing the corpus
  rather than the predicate.
- **F6** — `T-164` has a reader, so the re-emission is a sweep rather than one file.
- **F7** — *added during execution, and it fired against the first draft rather than against the
  shipped predicate.* The shipped predicate is the first draft, i.e. the checker was never wrong and
  was therefore never tested. It was wrong: the symmetric trailing guard `(?![\w.])` refuses a number
  followed by a full stop and so missed every number at the **end of a sentence**. Recorded here
  rather than quietly repaired, because a falsifier discovered while executing is still a falsifier
  and hiding its provenance would make the test table look prescient.

### Cost

One lattice re-solve (`T-164` is ~2 min of exhaustive placement search over 163 296 placements at 18
rungs), plus the census, which is offline. `T-164` has **no readers** — to be confirmed with
`tools/result-reader-census.py --check`, not asserted — so the re-emission is one file and needs no
topological sort. If it has one, `tools/reemission-order.py` supplies the order.
