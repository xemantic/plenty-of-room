# C-0203 — **`T-261` could not gate the residue because it had not read it: of the 17 challenges a claim adjudicates whose own `**Status**` row does not say so, 15 ARE adjudications and 2 are not — and the corpus's own repair idiom was invisible to the reader, so a Status row reading `~~OPEN~~ RESOLVED` was reported OPEN**

| | |
|---|---|
| **Task** | [`T-298`](../tasks/T-298-a-challenges-status-row-is-the-authority.md), raised by [`C-0197`](C-0197-the-challenge-halfs-own-open-word.md) (`T-261`) §5 |
| **Leaf** | none — a **process** claim protecting the two customer-facing documents |
| **Verification type** | **logical** — a predicate over the challenge corpus and the claim corpus, with every one of the 24 sites read by hand against the claim that produced it, and every narrowing's false negatives counted over the whole claims corpus **before** the guard was written |
| **Verdict** | **PASS on all five predicates.** `P1` all 17 disposed of in writing — 15 annotated with the adjudicating claim named and the verdict stated, 2 excluded on stated grounds; `P2` the `UNRECORDED-ADJUDICATION` residue goes **24 sites / 17 challenges → 0 over the seventeen** and is now **counted into the exit code**, with a named test on the exit code in both directions; `P3` both exclusions carry a named test and a measured cost — **46 → 43** pattern-1 sites over the whole claims corpus, **3 lost and 0 of them a genuine adjudication**; `P4` `tools/trace-answers.py` exits **0** on both deliverables; `P5` the index-versus-file disagreement is derived fresh at the ref, reported in both directions, and reconciled on 18 rows |
| **Maturity** | **TRL 1–3, and below it: NO PHYSICS CHANGED.** Every edit is a status word, a pointer, a regular-expression guard or a test. No result file of any study moves and no physical number is recomputed |
| **Provenance** | [`gpd/results/T-298-a-challenges-status-row-is-the-authority.json`](../results/T-298-a-challenges-status-row-is-the-authority.json), emitted by [`tools/T-298-emit-result.py`](../../tools/T-298-emit-result.py) with the **before** reading executed out of `git show <ref>:` — the ref's own tool over the ref's own `gpd/claims/` and `gpd/challenges/`. Predicate in [`tools/trace-answers.py`](../../tools/trace-answers.py), 167 named tests in [`tools/test-trace-answers.py`](../../tools/test-trace-answers.py) (151 at the ref), mutation coverage in [`tools/T-298-mutation-test.py`](../../tools/T-298-mutation-test.py) |
| **Conditions** | `baselineRef = a16d0766531f974af3f845e56c79cadc87d386df`, recorded in the result file. `HEAD` advanced twice while this task ran and the emitter re-based on it each time, which is `CH-0246`'s own design; the `before` residue is **unchanged at 24 sites over 17 challenges** at both refs, and the index census below is the emission at the SHA named here. Units unchanged and untouched: nm, pN, pN/nm, pN/nm² = 1 MPa exactly, `k_BT = 4.141947 pN·nm` at 300 K, aqueous buffer with stated Mg²⁺. Nothing physical is computed |
| **Consumes** | [`C-0197`](C-0197-the-challenge-halfs-own-open-word.md) (the residue, and the one false positive it named without tuning away), [`C-0071`](C-0071-output-element-recommendation.md) (strike, never delete), [`C-0176`](C-0176-partial-discharge-and-restatement-predicates.md) (measure a narrowing's false negatives; a mutation table needs both directions), [`C-0129`](C-0129-result-file-hygiene.md) (gate what can come clean), [`C-0115`](C-0115-fifth-answers-synthesis.md) (a check's verdict window is part of its logic — here read the other way round), [`C-0185`](C-0185-orphaned-mutation-anchors.md) (a mutation harness asserts its baseline and its anchors), [`C-0083`](C-0083-markdown-tables-that-do-not-render.md) / [`CH-0236`](../challenges/CH-0236-a-line-left-advisory-on-a-ground-about-its-own-predicate.md) (an impossibility claim about a checker is dated by the checker's own predicate) |
| **Raises** | [`CH-0255`](../challenges/CH-0255-a-cross-reference-is-not-an-index-row.md) against `tools/check-challenge-index.py`'s completeness reading |

---

## The claim, in one line

`T-261` printed the residue and declined to gate it because it *could* not be made clean.
It could: **15 of the 17 are adjudications and needed a status row, 2 are not and needed a guard** —
and the reason the annotation had never been written is that the corpus's own repair idiom,
`strike, never delete`, was **invisible to the reader that decides a challenge's status**.

---

## 1. The 17, read

A status is a **verdict** and a verdict is not a word, so each of the 17 was read against the
sentence in the claim that adjudicates it, and each Status row says what was adjudicated **and what
was not**.

| challenge | adjudicating claim | verdict written | full or partial |
|---|---|---|---|
| `CH-0004` | [`C-0008`](C-0008-electrostatic-force-and-decay-length.md) (`T-3a`) | **RESOLVED** | partial — upheld in consequence, **refuted in magnitude** |
| `CH-0010` | [`C-0077`](C-0077-first-moment-chain-length.md) (`T-1e`) | **UPHELD IN PART** | partial — its own word *"most"* quantified at 62–68 % |
| `CH-0033` | [`C-0017`](C-0017-output-coupling-stiffness.md), [`C-0015`](C-0015-crossover-phase-and-registration.md) | **UPHELD** | full — recorded in **both** targets' own banners |
| `CH-0056` | [`C-0042`](C-0042-paired-perpendicular-junction.md) (`T-97`) | **UPHELD** | partial — completed by `C-0052`/`C-0059`, the multi-junction transfer refused by `CH-0072` |
| `CH-0078` | [`C-0070`](C-0070-pinned-leg-budget.md) (`T-132`) | **UPHELD** | full — its own *"what would settle it"* item 2 discharged |
| `CH-0083` | [`C-0084`](C-0084-recommended-element-pull-in-fold.md) (`T-149`) | **RESOLVED** | full |
| `CH-0089` | [`C-0079`](C-0079-unbonded-duplex-separation.md) (`T-139`) | **UPHELD** | full — its failure route 1 closed |
| `CH-0093` | [`C-0085`](C-0085-collinear-stacking-clearance.md) (`T-152`) | **UPHELD and closed** | full |
| `CH-0101` | [`C-0090`](C-0090-buildable-raster-width.md) (`T-153`) | **DISCHARGED** | partial — in **one** item; the width statement still STANDS |
| `CH-0103` | [`C-0103`](C-0103-path-count-at-fixed-geometry.md) (`T-163`) | **UPHELD** | partial — as a bookkeeping correction; and `CH-0119` separately withdraws its own instrument |
| `CH-0151` | [`C-0141`](C-0141-honeycomb-station-lattice-and-placement.md) (`T-219`) | **OVERTURNED** | full |
| `CH-0177` | [`C-0118`](C-0118-coupled-four-layer.md) (`T-197`) | **UPHELD** | full — the *value* survives, the *monotonicity* does not |
| `CH-0184` | [`C-0148`](C-0148-face-bond-class-residues-and-row-span-columns.md) (`T-244`) | **ANSWERED** | full |
| `CH-0185` | [`C-0148`](C-0148-face-bond-class-residues-and-row-span-columns.md) (`T-243`) | **ANSWERED** | full — **and the price decision 8 rested on is withdrawn with it** |
| `CH-0229` | [`C-0182`](C-0182-name-the-discharge.md) (`T-281`) | **ANSWERED** | full |

Four of the 15 carried **no `**Status**` row at all** (`CH-0056`, `CH-0078`, `CH-0083`,
`CH-0103`), which the reader returns as `UNKNOWN` and reports silently — so those four were not
even *readable* as open.

**`CH-0185` is `T-261`'s own live instance and it is now closed at the source.**
`C-0148` said *"`CH-0185` is ANSWERED — the twelfth column is a box artefact"* and the file said
*raised*, which is why `DECISIONS-FOR-NDI` decision 8 could be put to a customer priced on a
threshold the same iteration had withdrawn.

## 2. The two that are not adjudications, and what they cost to exclude

`C-0197` named **one** false positive of 17 and deliberately did not tune it away, on `C-0176`'s
ground that a guard narrowed to one observed case is a test written to the shape of the change.
Reading all 17 finds **two**, and `C-0176`'s actual prescription — **measure the narrowing's false
negatives first** — is what makes the repair admissible rather than reactive.

| challenge | the sentence | the shape | guard |
|---|---|---|---|
| `CH-0068` | *"If `CH-0068` is upheld, the design point is `N_ret = 56` … If `CH-0068` is refused, the thresholds bind"* ([`C-0056`](C-0056-connectivity-ceiling-plate.md)) | a **conditional** — it says in as many words that the verdict is not in | a lookbehind on `if`/`unless`/`whether` |
| `CH-0157` | *"That is `CH-0157`**, and** it is why the bracket has to be withdrawn"* ([`C-0132`](C-0132-cut-rim-charge.md)) | a **clause crossing** — the thing withdrawn is the **bracket** | the clause guard also breaks on a comma plus a coordinating conjunction |

**Measured over the whole claims corpus before either was written: 46 pattern-1 sites become 43,
the three lost are exactly the three sites of these two challenges, and none of them is a genuine
adjudication.**

The conjunction list is deliberately `and|but|so|yet|or` and **not** `which`: a relative clause
keeps the challenge as its subject, and `C-0182`'s *"`CH-0229`, which raised this task, is
**ANSWERED**"* is a real adjudication that an over-wide guard loses. That cost was measured too —
including `which` loses exactly one true positive — and it is held open by a named test.

## 3. The finding: `strike, never delete` and the status reader contradicted each other

`challenge_status_of` read the raw `**Status**` cell.
`C-0071`'s discipline writes a superseded status **struck, in place**, which the corpus already
does: `CH-0224`'s cell reads `~~**OPEN.** …~~ **RESOLVED, iteration 43**`.

**That challenge has been reported OPEN ever since.**
It is `CH-0185`'s defect one level down — the repair idiom the project mandates left the gate
exactly where it was — and it is the same shape `C-0197` records for the *numeric* arm, which
blanks strikes precisely so that *"the checker does not penalise the discipline it exists to
support"*. The two status readers were the only arms that did not.

Blanking the **cell** (never the whole file) moves exactly **one** reading in the corpus,
`CH-0224` OPEN → CLOSED, which is correct and which no deliverable references.
Only the cell, because blanking the file first lets a struck block *around* the row delete the row,
turning a declared status into an `UNKNOWN` one — the direction this checker must not guess in.

### 3a. And the Status cell has a vocabulary trap of its own

`_CHALLENGE_OPEN` is `open|raised`, matched **case-insensitively** because the cell is a
declaration written in mixed case.
So a Status cell that uses either word in **ordinary prose** reopens its own challenge.
Three cells written in this task did exactly that — *"DISCHARGED in its **open** item"*,
*"**Raised** by `C-0142`"*, *"**raised** and repaired in the same claim"* — and each was rephrased.
It is `queue_status` matching `DONE` inside *"Left undone"*, on the one half of the corpus that
cannot use case to escape it.

## 4. The cheap bound ran first, and it found the checker rather than the corpus

Before a single file was edited, every candidate status was set to `CLOSED` **in memory** and
`stale_challenge_statuses` was run over both deliverables.
It predicted **one** new defect, and named it: `ANSWERS.md` line 964,

> *"(`CH-0083`, raised open in iteration 16 and **RESOLVED in iteration 17**, below)"*

which is **correct as written**. Both cancellations the checker owns — `_HISTORICAL`'s
*"open in iteration N"* and `_ANSWERING`'s *"RESOLVED"* — are present in that sentence and both sit
**outside `_OPEN_WINDOW = 24`**, whose job is to *bind an open word to its reference* and which was
never meant to bound a cancellation. A cancellation can only ever **remove** a hit, so reading it
on `_VERDICT_WINDOW = 80` — `T-183`'s own measured constant, on this same document — is strictly a
narrowing.

`C-0115` says *a check's verdict window is part of its logic, and a sentence can be wrong rather
than the checker*. This is that sentence read the other way round: here the sentence is right, and
the check's window is what has to move. **The bound cost one pass and it ran before any edit**,
which is the whole reason the answer is a repair and not a broken deliverable.

## 5. The gate

`UNRECORDED-ADJUDICATION` goes **24 sites over 17 challenges → 0 over those seventeen** and is now
counted into `tools/trace-answers.py`'s exit code, with a named test in both directions on the exit
code itself (`C-0177`/`P-29`: a gate is a claim about a corpus and it is discharged by **running**
it).

**And it fired on the first thing that landed after it, and was cleared within the hour.**
While this claim was being written, [`C-0199`](C-0199-the-gallery-opened.md) and
[`C-0200`](C-0200-the-file-draws-and-the-table-orders.md) both landed saying `CH-0251` is
**REFUTED on its central point**, while its own `**Status**` row still read `**RAISED**` — **2 sites
on 1 challenge**, in a file this task does not own. It was annotated by its owner and the residue is
**0** again. That is the whole point of the promotion: the shape that put decision 8 in front of a
customer is now a build failure at the moment it is created, instead of a residue line nobody reads
an iteration later. It is also `CH-0182` on this claim's own census — the corpus moved while the
claim reporting it was being written, twice, and the counts are dated to the ref in *Conditions*.

**16 named tests added, 151 → 167** — and one *existing* test had to be made hermetic with them:
`_run_tool` took the repository's own `gpd/claims` and `gpd/challenges` by default, so counting the
residue into the exit code made *"a deliverable that AGREES with the queue exits 0"* depend on a
**mutable artifact**. It now runs against empty directories. `CLAUDE.md`'s own note on self-tests
that read a mutable artifact, met from the other side: here the corpus moving made a **passing**
test fail while nothing it asserts had changed.
**10 mutations, 0 survivors** ([`tools/T-298-mutation-test.py`](../../tools/T-298-mutation-test.py)):
six revert a rule and four over-correct it, which is `C-0176`'s both-directions standard, and the
harness asserts its **baseline** and its **anchor counts** per `C-0185`/`CH-0237`.

## 6. The index against the files

Derived fresh at the ref rather than inherited, in **both** directions:

| reading | at the ref | now |
|---|---|---|
| challenge files with no index row of their own | **2** (`CH-0053`, and `CH-0252` which a sibling committed mid-iteration) | **0** |
| index records an adjudication the file does not | **8** | **1** |
| file records an adjudication the index does not | 8 | 8 |
| status disagreements | 30 | **24** |

Eighteen rows reconciled: 13 index rows brought to the file, 4 files brought to the index
(`CH-0003`, `CH-0007`, `CH-0016`, `CH-0160`), and `CH-0053`'s missing row written.
One is **not** reconciled and it is said. On `CH-0202` the index's `UPHELD` reports that
`C-0151`'s **verdict** holds, and not that the challenge has been adjudicated, so the file is right
and the index's word is the loose one. `CH-0251` belongs to another agent this iteration and was
reconciled by its owner, not here.

**`T-298`'s row names six in the index-says direction and this claim measures eight** — the six
plus `CH-0160` and `CH-0251`, both of which the row's reading predates — while the *status*
predicate over the same files gives a **different** six. Neither is wrong. `challenge_status_of` is
case-insensitive and `challenge_adjudicated` is not, so `CH-0010`'s *"Upheld in substance"* is
CLOSED to one reader and unadjudicated to the other. **`CH-0182`, sixth consecutive iteration: a
census is dated by its predicate as well as by its premises**, and both readings are emitted.

## 7. What this claim does not say

- **It does not adjudicate anything.** Every verdict written here was already reached by a claim,
  and is quoted with the claim that reached it. Where no claim reached one — `CH-0053`, `CH-0068`,
  `CH-0157` — none is written and the row says so.
- **The two deliverables are untouched.** The one passage the cheap bound flagged is correct as
  written and the checker moved instead; no `STALE-OPEN` remains on either document.
- **`prices_on_adjudicated` is still a residue and is still not gated**, for `CH-0230`'s reason.
  It reads **10** on `ANSWERS.md` — up from **6**, because 15 challenges became adjudicated — and
  **0** on `DECISIONS-FOR-NDI.md`. That rise is `CH-0230`'s own mechanism arriving on a second predicate:
  **the census grows when the corpus is corrected**, and it is a residue precisely because it does.
- **The mutation harness is not declared in `tools/P-31-harness-census.py`'s `HARNESSES` table**,
  which `T-298` does not own, so `P-31 --check` will report it `UNDECLARED` — the census working as
  designed. The row it wants is stated in the harness's own header. Note that `P-31 --check` is
  **already red at `HEAD`** — both `--check` and `--self-test`, on an unrelated `ORPHAN` in
  `tools/T-281-mutation-test.py` whose anchor *"the census's family map goes back to the PARTIAL
  one `C-0176"* occurs **0** times in `tools/T-234-census.py`. Verified in a `git archive HEAD`
  tree, so it is not a working-tree artefact and it is not this task's. The census also caught a name: `tools/T-298-emit-result.py` *reports* the
  mutation table and does not run it, and calling its constant `MUTATION_DIRECTIONS` made it a
  second `UNDECLARED` harness — the census being right about a name being wrong, repaired by
  lower-casing it.
- **No `ResultInputs` handle is added**, because `src/` is outside this task and the invariant is on
  a path spelled in a **main source**; this result path is spelled only in `tools/`.
