# C-0165 — **A RESTRUCTURE ITERATION IS NOT A SYNTHESIS HOLIDAY: three of iteration 39's findings are VALIDITY QUALIFIERS on published results, and two of them exist only because somebody graded a design this repository did not draw.** The largest is a **scope statement about a large body of standing work** — `CrossoverLayout.centred`/`.phased` alternate the column parity **by construction** and a seam breaks it, so **every phase-swept placement, count and flatness result in this corpus is over the ALTERNATING family** — and carrying it into the deliverables is what found the instance `C-0161` did not look for: `C-0119` says a seam is **forced** on the recommended `10 × 6` block, and `C-0160`'s committed `.sc` for that block draws **60 scaffold domains on 60 helices**, which is `C-0161`'s **own** seam discriminator returning *no seam* ([`CH-0212`](../challenges/CH-0212-the-recommended-block-is-drawn-without-the-seam-its-own-claim-forces.md)). **Seven retained checkers now exist, not six** — a sibling wired the seventh while this pass was running — and **two of the four defects this pass repaired are SELF-DESCRIBING COUNTS that no checker reads while two of the seven already print the correct value**: `ANSWERS.md`'s challenge/claim census had been stale in **four of its five** passes, and `DECISIONS-FOR-NDI.md` still said it had **no** checkers on it and `ANSWERS.md` **four**. The customer-facing document's other defect is its known sign: it named `T-9`'s crossover hinge constant as *"the only item still exceeding one session"* **four iterations after the run was made**

| | |
|---|---|
| **Task** | [`T-271`](../tasks/T-271-eleventh-answers-synthesis.md) — the eleventh `ANSWERS.md` synthesis, and the first one a **restructure** feeds |
| **Leaf** | `A8.2` |
| **Verification type** | **logical** — a reconciliation of two outward-facing documents against the claim corpus and the task queue, every mechanised half run by a retained checker and recorded before and after, every unmechanised half stated as a named drift class |
| **Verdict** | **PASS on `P1`–`P6`.** All **seven** retained checkers clean on both documents after the pass (§1); the three findings carried into the passages that state the verdicts they qualify (§2); 0 challenge-status disagreements (§1); four self-describing counts re-derived and corrected by striking (§3); both open decisions re-read against iteration 39 (§4); no number changed except beside its struck predecessor |
| **Maturity** | **TRL 1–3, and below it: NO PHYSICS CHANGED.** No study was run, no result file moved, no number was computed here. Every quantity quoted was `grep`ed out of the claim that owns it |
| **Provenance** | the seven checkers in `tools/` — [`trace-answers.py`](../../tools/trace-answers.py), [`check-corpus-links.py`](../../tools/check-corpus-links.py), [`check-markdown-tables.py`](../../tools/check-markdown-tables.py), [`check-challenge-index.py`](../../tools/check-challenge-index.py), [`check-entry-points.py`](../../tools/check-entry-points.py), [`check-result-file-hygiene.py`](../../tools/check-result-file-hygiene.py), [`check-corpus-identifiers.py`](../../tools/check-corpus-identifiers.py) — run on **both** documents before and after; the edits in [`ANSWERS.md`](../../ANSWERS.md) and [`DECISIONS-FOR-NDI.md`](../../DECISIONS-FOR-NDI.md) |
| **Conditions** | The tree at `b639895` plus this pass's edits. Nothing physical is computed; units unchanged and untouched — nm, pN, pN/nm, pN/nm² = 1 MPa exactly, `k_BT = 4.142 pN·nm` at 300 K, aqueous buffer with stated Mg²⁺ |
| **Consumes** | [`C-0161`](C-0161-mechanics-on-an-imported-design.md) (the parity restriction, the crossover census and the imported-design path), [`C-0160`](C-0160-scadnano-writer.md) (the writer and the two committed artifacts), [`C-0164`](C-0164-lattice-aware-buildability.md) (the lattice-aware rule and the `ADMISSIBLE` verdict), [`C-0157`](C-0157-crossover-hinge-constant-from-oxdna.md) (the `k_θ` bracket the census qualifies), [`C-0119`](C-0119-honeycomb-raster-width.md) (the forced seam), [`C-0162`](C-0162-round-outputs-never-inputs.md) / [`C-0158`](C-0158-prose-gate-red.md) / [`C-0159`](C-0159-environment-interface.md) / [`C-0163`](C-0163-cold-start-entry-points.md) / [`C-0166`](C-0166-dangling-identifiers.md) (the rest of iteration 39, checked for carriers), [`C-0155`](C-0155-tenth-answers-synthesis.md) and the nine passes before it (the method) |
| **Raises** | [`CH-0212`](../challenges/CH-0212-the-recommended-block-is-drawn-without-the-seam-its-own-claim-forces.md) — the recommended block is drawn without the seam its own claim forces |

---

## 1. The cheap bound ran first, and it is the checkers — all of them, on both documents

`C-0088`'s standing finding is that *a checker's default is part of its logic*, and the document
nobody checks is the one the customer reads. So the pass opened with every retained checker on
**both** deliverables, recorded before any edit.

| checker | before | after |
|---|---|---|
| `trace-answers.py --answers ANSWERS.md` | 1 661 tokens: **0 ABSENT**; 0 contradicted; 0 self-contradictions | 1 704 tokens: **0 ABSENT**; 0 contradicted; 0 self-contradictions |
| `trace-answers.py --answers DECISIONS-FOR-NDI.md` | 842 tokens: **0 ABSENT**; 0 contradicted; 0 self-contradictions | 865 tokens: **0 ABSENT**; 0 contradicted; 0 self-contradictions |
| `check-corpus-links.py` | **2 broken**, both in `TASKS.md`, both iteration 39's | **0 broken** |
| `check-markdown-tables.py` | 0 defects | 0 defects |
| `check-challenge-index.py` | 183 of 183 indexed | **184** of 184 indexed |
| `check-entry-points.py` | 0 defects | 0 defects |
| `check-corpus-identifiers.py` | **did not exist when this pass began** | 0 dangling |

Three of those rows are findings rather than readings.

**(a) The link gate was RED, and it was repaired by somebody else mid-pass.** The two broken links
were `gpd/challenges/CH-0210-the-census-is-not-reproducible-at-its-own-commit.md` — the writer
reconstructing the slug from the challenge's *subject* where the file carries its *title*, which is
`C-0083`'s exact class — and `gpd/tasks/T-272-emission-layer-remainder.md`, a task file not yet
written. Both are in `TASKS.md`, which this pass does not own. Between the opening reading and the
next, a sibling committed three times and both went green. **The reading is recorded because it was
taken**, and because a gate that goes green without anyone in this pass acting is exactly the kind
of thing a synthesis must not silently claim credit for.

**(b) A seventh checker appeared during the pass**, `tools/check-corpus-identifiers.py`
([`C-0166`](C-0166-dangling-identifiers.md), `T-273`), and it is wired into `tools/verify.sh`. It
was run, it read **0 dangling identifiers**, and **it then fired on a sentence this pass had just
written** — a bare challenge number used as an *example* of the defect the checker detects. Per
`C-0115`'s rule the repair is the passage and not a suppression, and the sentence was rephrased.
**Fourth consecutive iteration in which a retained check caught the mistake of the person using it.**

**(c) The table checker fired on this pass too**, at `ANSWERS.md:986`: a bare `|` inside inline code
in a shell command this pass had added to make a stale count derivable. GFM splits the row before it
parses backticks, so the cell count went 2 → 4. Escaped to `\|`, which works inside a code span.
**The one edit whose whole purpose was to stop a number being asserted rather than derived is the
edit that broke a table.**

A **status** cross-check the tracer's fixed vocabulary cannot perform was run beside them: every
challenge either document calls *open*, with `~~struck~~` spans blanked first, against that
challenge file's own `Status` row. **0 disagreements** over 184 challenges.

---

## 2. The three findings, carried into the passages that state the verdicts they qualify

`P2` is written the way it is because *an annotation on a claim's BODY is not an annotation on its
HEADLINE*. Each finding went into the sentence a reader meets, not into an appendix.

### (a) The alternating column-parity family — `C-0161` §4(b)

The restriction is on the **swept family**, not on the data structure, and the deliverable text says
the second and not the first: `CrossoverLayout` carries the parities explicitly and represents the
seamed sequence **without complaint**, and the reference rectangle is representable *exactly* — 90
lattice sites against 90 drawn, 0 absent. What no sweep here can generate is a **seam**, which
doubles a column pitch so that two consecutive columns serve the same interface parity; the
rectangle's sequence is `[0, 1, 0, 1, 1, 0]`.

Carried into **two** places in `ANSWERS.md`: the end of §3 row (g), which is where the flatness
verdict and its whole phase axis live, and a new bullet of §5 *"What we cannot answer, and why"*,
which names the specific results the restriction is a scope statement about — row (g)'s phase axis,
`C-0015`'s 32 phases, `C-0063`'s exhaustive centro-symmetric family, `C-0090`'s two buildable
phases, `C-0102`'s over-subscription. Neither passage claims a number moves; `C-0161` states the
restriction rather than acting on it, and a synthesis that acted on it would be manufacturing a
result.

### (b) The crossover census is a factor of two on an imported design — `CH-0209`

Carried into §3 row (g) beside (a), because it arrives with the same design; and into **both**
places `C-0157`'s `k_θ` bracket is quoted — §4's *Unanswerable questions* row and §5's `k_θ` bullet
— because that is the standing result the challenge qualifies. The wording is the challenge's own:
nothing says the bracket is wrong, it says the bracket was measured on **one** of two motifs and
this repository has no measurement on the other. **None of it touches the numbers in row (g)**,
which are this corpus's own designs drawn with one crossing per junction; what it touches is any
verdict handed to somebody else's design.

### (c) `ADMISSIBLE` at zero forced crossovers, and a replaced ground — `C-0164`, `C-0160`

This is `C-0080`'s **fourth drift class**, a superseded ground under an unchanged verdict, and it
reads `CITED` to every tracer because the verdict has an owner and the owner still states it.
Carried as a fourth iteration-39 restatement of `ANSWERS.md` §1 item 3 — the paragraph that already
carries three — and into `DECISIONS-FOR-NDI.md` §7a and its *At a glance* row, because §7a's
question is *"confirm `10 × 6`"* and what changed is that a *yes* can now be given against an object
rather than against a pair of integers in a study literal.

**Two honesty clauses travel with it in every carrier**, because the decisions file's failure mode
has a sign and this is the direction that would over-claim: `ADMISSIBLE` means *"every rule this
repository has for this design's lattice applies, and passes"* and **not** that anything is
foldable, and the emitted block **carries no staple set**, so it is a lattice artifact and not an
order.

---

## 3. What no retained checker reaches — and two of the seven already print the answer

Four defects were repaired that no gate can see. Two of them are one class.

| defect | where | why no checker reaches it |
|---|---|---|
| **`ANSWERS.md` §4's challenge/claim census** read *"one hundred and fifty-four challenges … against one hundred and forty claims"* — the tree carries **184** and **161** | `ANSWERS.md` §4 | a **self-describing count**: it is not a claim's number, so the tracer correctly finds no owner to disagree with |
| **`DECISIONS-FOR-NDI.md`'s own account of being checked** read *"`ANSWERS.md` had four retained checkers on it … and `DECISIONS-FOR-NDI.md` … had none"* — there are **seven**, and all seven read both | `DECISIONS-FOR-NDI.md`, closing section | same class, in the document whose whole closing section is *about* being checked |
| **the compute ask named a run already made** — *"the only item still exceeding one session is `T-9`, the crossover hinge constant from oxDNA"*, four iterations after `C-0157` ran it in a day on 8 cores | `DECISIONS-FOR-NDI.md`, *Two asks that are resources rather than decisions* | it **under-claims**, which is that file's known sign; and the sentence carries no number, so the numeric tracer is silent and the status tracer's phrasing list does not contain it |
| **the recommended block's seam** — `C-0119` says forced, `C-0160`'s artifact draws none | across two claims and a committed `.sc` | both statements are internally consistent and trace clean; no gate reads a design file for a seam. Filed as [`CH-0212`](../challenges/CH-0212-the-recommended-block-is-drawn-without-the-seam-its-own-claim-forces.md) |

**The count class is the one worth mechanising, and the argument is one line.**
`ANSWERS.md`'s census row has now been stale in **four of the five passes that touched it** —
*"Twenty-nine"* at iteration 4, *"Sixty-nine … against sixty"* at 12, *"Eighty-four … against
seventy-four"* at 16, *"One hundred and fifty-four … against one hundred and forty"* at 37 — and
**two of the seven retained checkers already print the correct value on every run**:
`check-challenge-index.py` prints `184 challenge file(s)` and `check-corpus-identifiers.py` prints
`344 claims and challenges exist`. **The gap is not a census, it is a comparison nobody makes.**

The repair applied here is the weaker one that needs no new tool: the row now carries the two
one-line derivations (`ls gpd/challenges/CH-*.md \| wc -l`, `ls gpd/claims/C-*.md \| wc -l`) and the
state it was read at, because a sibling filed a claim **while this pass was running** and the number
moved between two of this pass's own edits. `CH-0182`'s *a census is dated by its own premise set*,
met inside a single iteration.

---

## 4. The two open decisions, re-read against iteration 39

`C-0149` put decision 8 to NDI priced on a threshold its own iteration had withdrawn, and
`CH-0203` is the record of it. So both open decisions were re-read rather than assumed.

| | what iteration 39 does to it |
|---|---|
| **7a — confirm `10 × 6`** | **Strengthened, and only in the direction of evidence.** The block is now a committed scadnano artifact the **reference** implementation (0.21.1) loads with **zero warnings**, on which the 55 stations, phase 16, 14 bp offset, 7 bp stagger, 102 bp interface window and 116 bp = 39.44 nm extent are **re-derived** rather than asserted; and `checkBuildability()`, now lattice-aware, reads **`ADMISSIBLE` at ZERO forced crossovers** — 59 raster crossovers reducing by bond class to `{4, 14}` under one `b₀`. **No cost changes and the question is unchanged**; a *yes* is now given against an object |
| **7b — which scaffold** | **Unmoved.** Nothing in iteration 39 reads the caDNAno Methods list or its main-text rule, and `CH-0180` is unchanged |
| **8 — which width** | **Unmoved, and now stated to be unmoved rather than left silent.** Both readings are re-derived on the *same* emitted file, so drawing the object did not choose between them and could not have. There is still no margin on either side. What the file adds is that the ambiguity is **inspectable** rather than described |

**The stale-price check came back negative for both**, which is a result: `CH-0203`'s class did not
recur. What did recur, in the same file, is the **under-claiming** class — §3's third row.

---

## 5. Validity range, and what this does NOT establish

- **This claim is about two documents, not about the device.** No physics is computed and no number
  here is new. A `PASS` means the deliverables agree with the corpus and the queue at `b639895`.
- **The checker readings are a property of the moment they were taken.** The tree moved three
  commits under this pass; the *before* link reading was repaired by a sibling and the seventh
  checker did not exist when the pass opened. `CH-0182` applies to this claim's own §1.
- **`CH-0212` is filed, not answered.** This pass did not run `T-267`'s importer on the honeycomb
  block, did not re-run any coupled cell, and does not assert which of the two standing statements
  is right — only that they cannot both be.
- **The count class is repaired and not mechanised.** The comparison two existing gates would make
  free is described in §3 and deliberately not shipped here: wiring a gate is a task with its own
  mutation tests (`C-0127`'s standard), and this pass owns no tool.
- **`ANSWERS.md` §5's parity bullet names five results by claim ID and that list is a FLOOR**, taken
  from `C-0161` §4(b)'s own scope sentence. No exhaustive census of which corpus results run through
  `CrossoverLayout.centred`/`.phased` was taken.

## 6. Still open

1. **`CH-0212`** — two free readings settle it and neither was taken here.
2. **A gate on self-describing counts.** Two of the seven checkers print the numbers; nothing
   compares them to what a deliverable asserts. Four of five passes were wrong about this row.
3. **The status tracer's document-side phrasing list.** *"the only item still exceeding one session
   is `T-9`"* is an assertion about a task's status carrying **no** status word the tracer looks
   for. `CLAUDE.md` already records that the document-side vocabulary has the **silent** failure
   direction; this is a fresh instance and no phrasing was added, because adding one phrasing at a
   time is what makes such a list decay.
4. **Whether a seam would move a coupled cell.** `C-0161` measures the parity exchange as exactly
   symmetric under **one symmetric** load case and says so; under the solved edge collar the two
   would part, and by how much is unmeasured.
