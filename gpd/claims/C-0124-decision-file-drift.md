# C-0124 — **The document NDI actually reads had four checkers' worth of drift and no checker at all, and every one of its defects UNDER-claims.** Of **36** enumerated assertions, **23** needed an edit: **14 stale outright**, **4** superseded by an answer block in their own section and left unstruck below it, **3** numbers carried by an **open challenge that names this file by name**, and **2** places where the file contradicts itself. And the tool needed no new logic to reach the mechanical half — `tools/trace-answers.py --answers DECISIONS-FOR-NDI.md` runs **unmodified** and reports **3** stale task assertions and **1** untraceable number. What was missing was a **default**. The second finding is the one that matters more: the tracer read `~~struck~~` text as a live assertion, so **the only repair this project permits left every flag exactly where it was** — the checker penalised the discipline it exists to support

| | |
|---|---|
| **Task** | [`T-184`](../tasks/T-184-decision-file-drift.md) — `DECISIONS-FOR-NDI.md` has the same drift class the deliverable had |
| **Leaf** | — (a process claim, in the family of `C-0067`, `C-0078`, `C-0088`, `C-0113`) |
| **Verification type** | **logical** — a statement-by-statement adjudication against the claim corpus and `TASKS.md`, plus **101** executable self-tests for the two tool changes, every one written before its implementation |
| **Verdict** | **PASS on all five predicates.** The cheap bound ran first and is reported (**31** matching lines against **36** enumerated). Every assertion carries a verdict and a named deciding artifact. Every stale one is corrected **struck, never deleted**. All four retained checkers are quoted before and after and none regresses. A checker **is** warranted and is shipped as a default rather than as new logic |
| **Maturity** | **BELOW TRL 1–3: nothing here is physics.** No number is derived. Every quantity quoted is grepped out of its owning claim |
| **Provenance** | [`gpd/results/T-184-decision-file-drift.json`](../results/T-184-decision-file-drift.json), emitted by the retained [`tools/T-184-emit-result.py`](../../tools/T-184-emit-result.py), which re-resolves every referenced task's queue status on every run |
| **Conditions** | The corpus and queue at iteration 27, after `C-0123` |
| **Consumes** | [`C-0067`](C-0067-answers-reconciliation.md), [`C-0071`](C-0071-output-element-recommendation.md), [`C-0078`](C-0078-status-drift-in-the-deliverable.md), [`C-0088`](C-0088-does-the-deliverable-agree-with-itself.md), [`C-0106`](C-0106-fourth-answers-synthesis.md) (which raised it), [`C-0113`](C-0113-challenge-status-self-consistency.md), and for the substance [`C-0090`](C-0090-buildable-raster-width.md), [`C-0109`](C-0109-four-layer-tile.md), [`C-0110`](C-0110-device-b-tall-gap.md), [`C-0111`](C-0111-gold-electrode-pzc.md), [`C-0118`](C-0118-coupled-four-layer.md), [`C-0119`](C-0119-honeycomb-raster-width.md), [`C-0122`](C-0122-honeycomb-station-lattice.md) |
| **Constrains** | `DECISIONS-FOR-NDI.md` and `tools/trace-answers.py` only. **No claim, number or verdict anywhere in the corpus is contradicted, and none is examined.** No challenge is raised |

---

## 1. The cheap bound, and what it said

One `grep -cE` over a declared phrasing set, run before the file was read:
**31 matching lines** in 725.
That is not a re-read — it is a product — and the enumeration that followed is a **superset** of it, **36**.

The pattern and both readings are in the result file.
The live recount is now **34**, and the reason is worth stating:
every correction is a **struck sentence plus a replacement**, and both carry the phrasing.
**A grep over a file repaired by striking counts up, not down** — which is exactly why it is a cheap bound and not a metric.

## 2. The verdicts

| verdict | count |
|---|---|
| **STALE** — contradicted by the corpus or the queue, and the file carries no correction anywhere | **14** |
| **SUPERSEDED-IN-FILE** — the correction is in the same section's answer block, and the assertion is unstruck below it | **4** |
| **UNDER-CHALLENGE** — the number is carried by an **open** challenge | **3** |
| **SELF-CONTRADICTION** — the file asserts both sides | **2** |
| **PATCHED-ELSEWHERE** — superseded, and the file says so somewhere else | **4** |
| **STANDS** — checked and still true | **8** |
| **ALREADY-STRUCK** — corrected by an earlier pass | **1** |

**23 needed an edit. Every single one of them under-claims.**
Not one of the 36 asserts something the programme has *not* done.
That is `C-0067`'s standing finding arriving in the second outward-facing document:
*a deliverable that under-claims is as wrong as one that over-claims and is far harder to catch*,
because a reviewer's instinct is to check the assertions and not the disclaimers —
**and a decision file is made almost entirely of disclaimers.**

## 3. The four that a reviewer would have acted on

**(a) *"Nothing has been re-derived yet."*** — the second sentence of the file's own banner.
It was true for about a day.
The work the answers opened is `T-191`–**`T-195`**, not the `T-191`–`T-194` the banner names;
**four of the five are `DONE`**, and ten claims re-derived that line across iterations 23–26.
`T-195` — the unpaired scaffold remainder as a body in the gap — is the one still `TODO`, and the file never named it.

**(b) *"But nothing in this programme has evaluated a layer that tall."*** — §2, in the *"What is established"* list.
[`C-0110`](C-0110-device-b-tall-gap.md) evaluated exactly that layer in iteration 23 and it is quoted **38 lines above**, in the same section's answer block.
The bullet sat unstruck in the part of the section a reviewer reads.
§3's 100 pN stops arriving across a gap of **13.6989179 nm at 0.5 mM**.

**(c) *"whether it exists is one evaluation of `|k_eff|` in a corner nobody has evaluated. `T-192`, and it is cheap."*** — §4.
`T-192` ran. **Decision 4's device B is refused at 96 of 96 tall states on §3's acceptable clause**,
and §4 never recorded it: the section still told a reader the question was open and cheap.

**(d) *"`T-153` … cannot be scoped, because whether it is needed at all depends on this answer."*** — §5.
`T-153` was scoped **and closed in iteration 18** ([`C-0090`](C-0090-buildable-raster-width.md)) —
**five iterations before this file was last edited**, so the sentence was already false when it was written.

## 4. A challenge named this file, by name, and nobody read it

[`CH-0125`](../challenges/CH-0125-the-four-layer-brick-is-mis-specified-in-three-ways.md), raised by `C-0109` in iteration 23 and **OPEN**,
lists its carriers as *"`ANSWERS.md` §6 and row (g), **`DECISIONS-FOR-NDI.md` (twice)**, `TASKS.md`'s `T-162` and `T-191` rows"*,
and `C-0109` §5 says the number *"should not be re-quoted without it"*.
Both instances of **0.100166871** were still in §6 unflagged, four iterations later.
[`CH-0124`](../challenges/CH-0124-the-four-layer-variant-is-a-mixed-state-not-a-bound.md) is the same story for the
`D_∥` = 14 310.78 / `D_⊥` ≥ 19.222 pair, quoted **twice**, which `C-0109` measures at **4 547.17603** and **240.931249** pN·nm.

**This is `C-0071`'s *"a discharge is invisible to whoever files the removal"* in its sharpest form yet:
the removal was not merely filed, it was filed WITH THE ADDRESS OF EVERY CARRIER**, and the address was not used.

## 5. The two self-contradictions, and the second is the more interesting

- **The PZC.** The at-a-glance table says *"the **PZC is still not given**"* and §3's own residue subsection says
  *"the material half is answered and the PZC half now is too — from the literature, read directly"*
  (`C-0111`: **0.46–0.51 V vs SHE** for Au(111)). A reader of the table would send back an `E_pzc`,
  which is **not what is wanted** — the surviving ask is the *cell's* definition of zero bias, which decides the **sign** of the force at rest.
- **The unspent axis.** Line 81 says the four-layer body *"is the first unspent design axis since iteration 20"*
  and §6 says the programme *"now has **no unspent design axis at all**"*.
  Both were written within an iteration of each other, and **the axis existed, was spent, and is what §7 is about.**

## 6. What moved the flatness answer was neither of the two things §6 named

§6's *"What deferring costs"* said what would move the flatness verdict is
*"the per-site incorporation measurement … a bench measurement … rather than another coupling design"*.
It was **neither**. It was the **cross-section**:
[`C-0118`](C-0118-coupled-four-layer.md) grades **16** coupled cells under `C-0087`'s measured staple dropout and **9 are flat** at the 90th percentile —
**all eight** `10 × 6` cells, best **0.0278431488**, against **one of eight** on `15 × 4` at **0.0882933461** —
and [`C-0122`](C-0122-honeycomb-station-lattice.md) shows it survives placement on the honeycomb's real 21 bp station ladder.
The per-site measurement is still wanted; it is no longer the only thing that could move the answer, and the paragraph said it was.

## 7. The checker: no new logic was needed, only a DEFAULT — and then one real defect

**The measurement that decided it.** `tools/trace-answers.py --answers DECISIONS-FOR-NDI.md`, **zero code changes**:

| | `ANSWERS.md` before | `DECISIONS-FOR-NDI.md` before | `DECISIONS-FOR-NDI.md` after |
|---|---|---|---|
| numeric tokens | 1 254 | 394 | 436 |
| **ABSENT** | 0 | **1** | **0** |
| open assertions contradicted by `TASKS.md` | 0 | **3** | **0** |
| stale challenge assertions | 0 | 0 | 0 |
| self-contradictions | 0 | 0 | **0** |

**So the class IS mechanisable, in its queue-status and numeric halves, and the tool already did it.**
What was shipped is therefore a default and a semantic fix, not a checker:

1. **`DEFAULT_DOCUMENTS = ["ANSWERS.md", "DECISIONS-FOR-NDI.md"]`.** `--answers` takes `nargs="+"`;
   every output row is tagged with its document, because an unlabelled row from a two-document run cannot be acted on.
2. **`strip_struck()` — and this is the defect the audit found in the tool.**
   `C-0071`'s discipline is **strike, never delete**, so every correct repair in this repository leaves the withdrawn sentence in place inside `~~ ~~`.
   The tracer read it as a live assertion.
   **Repairing a stale *"`T-191` is open"* by striking it therefore left the flag exactly where it was:
   the checker penalised the only repair the project permits.**
   Struck spans are now blanked — length- and newline-preserving, so every reported line number survives — before all four checks, the numeric one included, because a withdrawn number is precisely the one a repair should not have to keep traceable.

**83 → 101 self-tests**, 18 new, each written before its implementation.

3. **And a third gap, in a different tool.** [`tools/check-corpus-links.py`](../../tools/check-corpus-links.py)
   scanned `gpd/` **only**, so a mistyped claim slug in *either* outward-facing document was invisible to it —
   the very defect it was built for (`C-0122`: 15 broken links from slugs reconstructed from memory).
   `ROOT_DOCUMENTS` added; **11 → 19 self-tests**, the new ones run against a synthetic root so they stay fixture tests.
   **Both documents are clean**, so the gap had not yet cost anything — which is worth stating,
   because a checker widened onto a clean target is the only case in which the widening is unambiguously cheap.

**Its cost on `ANSWERS.md`, measured rather than assumed**: 1 254 → 1 251 tokens (3 were struck) and
**9 moved `CITED` → `ELSEWHERE`** (their block's citation sat inside a struck span).
**0 moved into `ABSENT`.** No number became untraceable and **no verdict of `ANSWERS.md` moved.**

## 8. What is NOT mechanisable, and the approximation is priced and deliberately not shipped

**The CORPUS class — 13 of the 36 — cannot be reached by any check here.**
*"Nothing in this programme has evaluated a layer that tall"* names **no task and no number**,
so neither the numeric trace nor the queue-status check can see it.
That is `C-0080`'s **superseded standing value** class verbatim, and its exact check still needs a
`superseded-by` edge at **statement** granularity that no claim in this corpus carries.

**The nearest approximation**, stated so it is not deferred:
flag any sentence carrying a negative-existence phrase whose section also cites a claim filed *after* that sentence's own date.
Measured **recall on this audit: 9 of 13**.
Its **false-positive rate is unmeasured**, and `C-0067`'s standing rule is that
*an unmeasured false-positive rate is what makes a checker stop being believed* — so it is **not shipped**.

**And the audit measured a false positive of the existing check, which is the evidence that rule asks for.**
Of the 3 stale-status flags, one — `T-199` at the §7 header — fired on *"the last **unmeasured** dependency"*,
a statement about a **physical calibration**, not about a task's status.
It was not resolved by suppressing it: the header genuinely conflated the **analysis** task `T-199` (closed in iteration 25)
with the **decision** it raised (outstanding, and carried in the queue as item 12), and saying so is more accurate prose.
**The checker found a real ambiguity by the wrong route** — which is worth recording, because it is the only way to tell
a false positive that is cheap from one that is not.

## 9. Acceptance

| | predicate | verdict |
|---|---|---|
| **P1** | the cheap-bound count is reported before anything is read, and the enumeration is a superset | **PASS** — 31 declared, 36 enumerated |
| **P2** | every assertion carries a verdict and a named deciding artifact | **PASS** — 36 rows in the result file |
| **P3** | every stale assertion is corrected **struck, never deleted** | **PASS** — and shipping `strip_struck()` was what made that repair visible to the checker |
| **P4** | the four retained checkers run before and after, and none regresses | **PASS** — tables 0 defects (375 → 380 files), corpus links 0 broken (366 → **370** files, the two root documents now among them), census unchanged, tracer 0 `ABSENT` on both documents and 0 on all three status checks |
| **P5** | a checker is shipped with self-tests, or its absence is priced | **PASS on both halves** — the mechanisable half is shipped (18 tests); the corpus half is priced at 9/13 recall with an unmeasured false-positive rate and withheld |

## 10. What this does not claim

- **No physics.** No number in this claim is derived; every one is grepped out of its owner.
- **`CH-0124` and `CH-0125` are not adjudicated here.** Both are open and both re-solves are owed.
  This claim flags their carriers in the decision file; it does not move a number of either.
- **`ANSWERS.md` was not edited.** Its 9 `CITED` → `ELSEWHERE` movements are a re-classification by the widened tool, not a change to the document.
- **The decision file's *substance* is not re-argued.** Decisions 1–6 stand as answered and §7 stands as asked;
  what moved is the currency of the analysis beneath them.
