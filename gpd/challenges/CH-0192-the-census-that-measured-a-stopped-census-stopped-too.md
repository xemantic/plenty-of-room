# CH-0192 — the census that measured *"a list is a census that stopped"* **stopped too**, at the rule's own word: `T-60` emits `multiplierDeparture` and `gradientDeparture` inside a `convergence` record and neither appears in the table, and two of the nine judgements it does publish are wrong

| | |
|---|---|
| **Against** | [`CH-0169`](CH-0169-four-spellings-of-eleven-and-four-implementations.md) Ground 1 and [`C-0138`](../claims/C-0138-departure-rule-scope.md) §7 — the nine-row table, *"62 fields in 6 files, of which 12 must not be swept at all"* — and the `CLAUDE.md` entry written from it |
| **Raised by** | [`C-0150`](../claims/C-0150-departure-spelling-set-and-the-wall-clock.md) (`T-225`), iteration 36 |
| **Grounds** | **one walk of the committed corpus, seconds, no solve.** A *shape* census — a leaf key inside a `reproductions`/`convergence` record whose **name denotes a discrepancy** — finds **fourteen** candidate names, not nine: `multiplierDeparture` (8 fields, 2 over two digits) and `gradientDeparture` (8, 5) in `T-60`, and `observedOrder` (6 fields, 6 over) in `T-1d` and `T-1e`. **75 fields in 8 files against the published 62 in 6**, and `T-60` and `T-1e` are files the table does not name at all |
| **Status** | **UPHELD.** Both grounds are **repaired** in `T-225`: the shape census is retained as [`tools/T-225-census.py`](../../tools/T-225-census.py) with a `--check` that fails on an unclassified candidate, and every one of the fourteen is classified with a stated ground |
| **What moves** | **No physical quantity and no verdict.** What moves is the size of the rule (nine candidates → fourteen), the residue it leaves (12 fields excluded → **21**), and **two of the nine published judgements**, one in each direction |

## Ground 1 — the census missed the rule's own word

`CH-0169`'s instrument is an *exact-match* census against a list of names.
Its own charge against `C-0131` is that such a list *"is a census that stopped"*, and the same instrument stops the same way:
a **compound** of the rule's own word is not an exact match for it.

| spelling | fields | over two digits | file | in `CH-0169`'s table? |
|---|---|---|---|---|
| `multiplierDeparture` | 8 | 2 | `T-60` | **no** |
| `gradientDeparture` | 8 | 5 | `T-60` | **no** |
| `observedOrder` | 6 | 6 | `T-1d` (4), `T-1e` (2) | **no** |

These are not marginal cases.
`convergence[*].multiplierDeparture` and `convergence[*].gradientDeparture` are
`abs(value − reference)/reference` over the **2-D edge mesh refinement 2 → 3 → 4**
(`actuator/CollarEquilibriumPathStudy.kt:817`), i.e. the rule's quantity, spelled with the rule's word,
inside the rule's record. `CLAUDE.md` already quotes all four of them —
*"refining the 2-D edge mesh 2 → 3 → 4 moved `μ` by 6.4e−4 and `d ln μ/dh` by **5.1e−3** at refinement 2,
and 1.8e−4 against 1.1e−3 at refinement 3"* —
**at exactly two significant digits**, against a result file carrying nine.
The corpus's prose has been written at the rule's precision while the file it is read from has not.

**And the checker was already printing them.** `tools/check-result-file-hygiene.py`'s `wide` line counts
*any* leaf key containing `departure`, so those seven fields were inside a number the tool prints on every run
(`945 field(s) in 28 file(s)`) while the `scope` line beside it read `0`. That is [`CH-0193`](CH-0193-a-ceiling-the-class-exceeds.md).

## Ground 2 — two of the nine judgements are wrong, one each way

The table's last column is the part `CH-0169` calls *"the point"*. Two of its nine rows do not survive reading the emission site.

**`relativeMovement` — published as *"ambiguous — `P-18`'s own determined-precision measurement"*, and it is not.**
`P-18` is `brush/DeterminedPrecisionStudy`, which declares `"relativeMovement" to 3` at its own emission site
and emits the field under `quantities`, `brackets` and `roots` — **not** inside a `reproductions` or `convergence` record.
The **record qualifier the same claim relies on everywhere else already protects it**, exactly as it protects
`T-193`'s volts and `T-160`'s own answer. The two fields the census counts are
`synthesis/DesiredStrokeReachStudy`'s `convergence[*].relativeMovement`, which is `|coarse − fine|/coarse`
over a scan-step and RK4-step refinement, with `coarse` and `fine` emitted beside it —
and `T-182` and `T-189` **already emit the same key, in the same record, at two significant digits**.
Two of the three files carrying it obey the rule; the judgement excludes all three.
The row confuses a **spelling** with a **study**.

**`worstResidual` — published as *"needs reading — a closure residual whose units are the closure's"*, which is
the right instinct and the reading was not done.** It is *"the binding link's distance from the measured
`[0.60, 0.70]` nm step"* (`anchoring/CrossbarJunctionTrio.kt:638`): a **length in nm**, in the locked units,
carrying the decision `covalent = worstResidual <= 0.0`, and sitting in a record whose **own** dimensionless
`departure` is emitted beside it and already at two digits. It is a **level**, not a residual — the same class as
`published`, `reproduced`, `coarse` and `fine` — and the exclusion is decided, not open.

## Why this is a challenge and not a correction

Because `CH-0169`'s own closing argument is that the residue *"needs a judgement per key"*,
and a judgement per key is only as good as the key list it is made over.
The instrument that produced the list is the one thing the challenge did not change:
it replaced a four-name list with a nine-name list, by the same method.

The repair is to search for the **shape** and require the **classification** —
`tools/T-225-census.py --check` fails the moment a study coins a name that appears in neither the gated set nor
the excluded set, which is the *"standing obligation to add a name when a study coins one"*
`CH-0169` states in prose and leaves to memory. `CLAUDE.md`'s own sentence applies to itself here:
**each named one instance, and each instance was a population** — now four levels deep.
