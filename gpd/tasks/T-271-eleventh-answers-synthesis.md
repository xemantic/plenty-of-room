# T-271 — the eleventh `ANSWERS.md` synthesis, and the first one a RESTRUCTURE feeds

| | |
|---|---|
| **Leaf** | `A8.2` |
| **Verification type** | **logical** — a reconciliation of two outward-facing documents against the claim corpus and the task queue, with every mechanised half run by a retained checker and every unmechanised half stated as a named drift class |
| **Units** | nothing physical is computed here. Where a number is quoted it is quoted **at the precision its owning claim states**, and the units are the locked ones: nm, pN, pN/nm, pN/nm² = 1 MPa exactly, `k_BT = 4.142 pN·nm` at 300 K, aqueous buffer with stated Mg²⁺ |
| **Documents in scope** | [`ANSWERS.md`](../../ANSWERS.md) and [`DECISIONS-FOR-NDI.md`](../../DECISIONS-FOR-NDI.md) |
| **Predecessors** | the ten previous passes — `C-0067`, `C-0078`, `C-0080`, `C-0088`, `C-0091`, `C-0115`, `C-0124`, `C-0130`, `C-0144`, `C-0155` |

---

## 1. Formulate

### What this pass is for

Iteration 39 is a **restructure**: five of its six task rows are about how this repository is built,
emitted and gated rather than about the device.
The natural conclusion — *"a restructure iteration has nothing for a synthesis to carry"* — is the
hypothesis this task exists to falsify, and **three of iteration 39's findings are not process notes**.
They are **validity qualifiers on results this programme has already published**, and two of them
could only have been found by grading a design this repository did not draw:

1. **`C-0161` §4(b)** — `CrossoverLayout.centred` and `.phased` alternate the column parity **by
   construction**, and a *seam* doubles a column pitch, so **every phase-swept placement, count and
   flatness result in this corpus is over the ALTERNATING column-parity family**. No claim states
   this restriction, because no claim could see it without a seamed design; the reference
   implementation's own Rothemund rectangle has parities `[0, 1, 0, 1, 1, 0]`, which **no phase
   sweep here can generate**.
2. **`CH-0209`** — the field's own generator draws every Rothemund crossover as **two** strand
   crossings at adjacent offsets where this corpus draws **one**, so a crossover census is ambiguous
   by a factor of two on any imported design (**90 sites or 45** on one file), worth **`1.087×`** in
   a peak dishing. `C-0157`'s oxDNA `k_θ` bracket was measured on **one** of the two motifs.
3. **`C-0164`** — the recommended `10 × 6` block reports **ADMISSIBLE** on a lattice-aware
   buildability rule at **zero forced crossovers**, derived from the emitted file; and the reference
   rectangle had been passing the **old** rule *for the wrong reason*, on a 144 bp row width that is
   neither its 128 bp span nor any of its scaffold runs.

The third is `C-0080`'s **fourth drift class** — a **superseded ground** under an unchanged verdict.
It reads `CITED` to every tracer, because the verdict has an owner and the owner still states it;
and no check written on a verdict can see that what supports it has been replaced.

### The standing failure modes this pass must assume, from `CLAUDE.md`

- A synthesis drifts by keeping its **answers**, not by mis-copying its numbers.
- `DECISIONS-FOR-NDI.md`'s failure mode has a **sign** — every defect the two audits of it found
  **under-claims** — so it must be re-read against the queue whenever a task closes, not audited
  when somebody remembers.
- **Quote a ratio's construction beside it**, so a moved argument is visible at the point of use.
- Round a claim's number only where the precision is not the content, and **the trap runs both ways**.
- A **question put to the customer can carry a stale price** (`C-0149`), and the price is the one
  dimension this programme has no column for.
- A **census is dated by its own premise set** (`CH-0182`), so this pass's own list is a **floor**.

---

## 2. Plan

### Order, and why this order

**The cheap bound runs first**, and here the cheap bound is *the retained checkers*, all six, on
**both** documents — `C-0088`'s standing finding is that *a checker's default is part of its logic*,
and the document nobody checks is the one the customer reads. They were clean at `ee5cf1a`; anything
they report is iteration 39's, and it is found for the price of six invocations rather than a read.

1. Six checkers, both documents, recorded **before** any edit.
2. A **status** cross-check the tracer's fixed vocabulary cannot do: every challenge either document
   calls *open*, against that challenge file's own `Status` row, with struck spans blanked first.
3. A **count** check on every self-describing quantity in either document (the challenge/claim census
   in §4 of `ANSWERS.md` is the known one).
4. The three findings, carried into the passages they qualify — **the passage that states the
   verdict**, not a footnote, because an annotation on a body is not an annotation on a headline.
5. The two **open** decisions re-read against iteration 39 for a stale price and for a changed
   admissible answer shape.
6. Six checkers again, both documents, recorded **after**.

### Method choice, justified against cost

Nothing here is re-run and no result file moves: every number this pass quotes is `grep`ed out of
the claim that owns it. That is not a cost saving, it is the **rule** — a synthesis is not a source,
and a number a synthesis computes has no owner (`C-0130`).

The alternative — re-grading the corpus's phase sweeps over the non-alternating parity family — is
**correctly refused**: `C-0161` states the restriction rather than acting on it, and a synthesis
that acted on it would be manufacturing a result. What a synthesis owes is that the restriction is
**visible where the results are read**.

### Acceptance predicates

| | Predicate | Falsified by |
|---|---|---|
| `P1` | All six retained checkers read **clean on both documents after the pass**, or every residue is named with the file that owns it and the reason it is not this task's to repair | any checker defect left unnamed |
| `P2` | Each of the three findings appears in **the passage that carries the verdict it qualifies** in whichever documents state that verdict, not only in a new section | a finding filed only as an appended note under a headline that still reads as before |
| `P3` | Every challenge status either document asserts agrees with that challenge file's own `Status` row, with struck spans blanked | one disagreement |
| `P4` | Every self-describing count in either document (claims, challenges, results, artifacts) is re-derived from the tree and either matches or is corrected **by striking, never deleting** | a count left stale, or a correction that deletes |
| `P5` | Both **open** decisions are re-read against iteration 39 and either (a) carry what changed, or (b) carry an explicit statement that nothing in iteration 39 moves them | an open decision whose price or admissible-answer shape moved silently |
| `P6` | **No number in either document is changed except by striking the old one beside the new**, and every new number is `grep`ed out of its owning claim at that claim's own precision | a number rewritten in place, or quoted at a precision no claim states |

### What would falsify this approach

- **If the three findings turn out to have no carrier in either document** — i.e. neither document
  states a verdict they qualify — then this task is a claim about the corpus and not a synthesis,
  and it should say so and stop. (Checked first: `ANSWERS.md` row (g) and §5 carry all three;
  `DECISIONS-FOR-NDI.md` §7a carries the third.)
- **If a checker fires on a passage this pass wrote**, the pass has manufactured the defect it exists
  to remove, and the repair is the passage rather than a suppression (`C-0115`'s rule).
- **If the parity restriction turns out to be false** — if `CrossoverLayout` can in fact represent a
  non-alternating parity sequence through a phase argument — then finding 1 is not a scope statement
  and carrying it into a deliverable would be an over-claim in the direction the decisions file is
  already known to fail in. `C-0161` §4(b) states that `CrossoverLayout` **carries the parities
  explicitly and represents it without complaint**; what no sweep generates is a *seam*. The
  restriction is on the **swept family**, not on the data structure, and the deliverable text must
  say the second and not the first.
