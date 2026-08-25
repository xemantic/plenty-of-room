# CH-0294 — **`tools/T-327-emit-result.py`'s arm *"the pinned reading and the working-tree control agree"* is a STALENESS DETECTOR wearing a pass/fail hat: it asserts that today's corpus equals a corpus pinned at `86b3bbd`, so it must go red the first time the corpus legitimately moves — and `T-337`, the row `C-0223` itself opened, is what moved it**

**Against** [`C-0223`](../claims/C-0223-the-resolution-of-the-flatness-census.md) (`T-327`) — one named arm of [`tools/T-327-emit-result.py`](../../tools/T-327-emit-result.py)'s `--self-test`, wired as the Gradle task `testFlatnessResolutionEmitter` and therefore build-failing.
**From** [`C-0225`](../claims/C-0225-the-exceedance-beside-every-verdict.md) (`T-337`) §5, where it was **predicted before the run** and was the only gate predicted to fire.
**Kind** — a **gate-design** defect, not an arithmetic one. Every number the arm compares is correct; what is wrong is that the comparison is asserted rather than reported.

---

## 1. The statement

The emitter builds its document twice — once from a `git archive` of the pinned ref `86b3bbd`, once from the working tree — and asserts:

```
ok("T-327-emit the pinned reading and the working-tree control agree",
   document["baselineControl"]["agree"])
```

with the field's own statement reading *"this task writes no file into `gpd/results/` other than its own, so the reading at the pinned ref and the reading at this pass's tree must agree"*.

That premise is true **of `T-327`** and it is not a property of the corpus. `C-0223` §4b refuses `87` positive verdicts *and opens `T-337` to repair them*; `T-337` re-emits seven of the very eighteen files this census reads. The moment the first of them lands, `unresolvable_verdicts` shrinks, the two readings differ, and a **wired, build-failing** arm reports a defect where the corpus has done exactly what the claim asked for.

## 2. Why it cannot be repaired by re-pinning

Moving the ref forward makes the arm pass today and fail at the next legitimate movement; it is the same defect one commit later. And the file's own record must **not** be re-run to chase today's corpus — [`CH-0246`](CH-0246-a-corpus-subject-result-file-cannot-be-re-run-as-a-control.md) is explicit that re-basing a corpus-subject file overwrites the record instead of checking it. So the answer is not a different ref; it is that an *agreement between a mutable tree and a fixed commit* is a **reading**, not an invariant.

[`C-0083`](../claims/C-0083-markdown-tables-that-do-not-render.md)'s *a gate that cannot come clean is not a gate*, read on a **control** rather than on a rule.

## 3. And the reproducibility statement the arm looked like it was making is separately, and better, made

The emitter already carries **`T-327-emit two builds at the same ref are byte-identical`**. *That* is the determinism claim, it is at a fixed state, and nothing the corpus does can falsify it. The working-tree comparison was never the reproducibility arm; it was a convenience that read like one.

## 4. The remedy, applied

Applied in this iteration, minimally and in the arm's own logic:

- `baselineControl.agree` stays exactly as computed — the **data** are right and are what a reader wants.
- The field gains a `whyThisIsNotAGate` sentence, so the record explains its own verdict.
- The named arm becomes *"the pinned reading and the working-tree control are **both taken**"*, which asserts what the control is **for** — that both readings were built and the comparison was made.
- A disagreement is written to **`stderr`** as a note naming the ref, because `--self-test > /dev/null` swallows stdout ([`C-0195`](../claims/C-0195-the-discriminating-input.md)).

The emitter still runs **`18`** named arms with **`0`** failures after both repairs, and `tools/T-327-mutation-test.py` is unchanged at **`21` mutations, `0` survivors**.

## 4b. And the SAME emitter has a SECOND arm of the same class, which the Plan did **not** predict

`T-337`'s `F6` — *"a gate other than the predicted working-tree control arm goes red"* — was declared open, expected not to fire, and **fired**. The arm is

```
ok("T-327-emit the exact verdict and the normal approximation agree at every record",
   document["exactAgainstNormal"]["disagreements"] == 0
   and document["exactAgainstNormal"]["recordsCompared"] == 1184)
```

and the second conjunct is **the population as it stood at `86b3bbd`, asserted as an invariant**. `T-337` carried an exceedance into `491` further records, so the comparison now runs over **`1 931`**, and the arm went red.

**The finding it protects did not break — it got stronger.** `C-0223` §2a reports the exact Clopper-Pearson verdict and the normal approximation agreeing at `1 184` of `1 184`; over the enlarged population the disagreement count is still **`0`**, now at **`1 931` of `1 931`**. What failed was a **count**, not a claim.

This is the same defect as §1 with a different mechanism — there a comparison against a fixed commit, here a comparison against a fixed population — and [`CH-0182`](CH-0182-a-census-is-dated-by-its-premise-set.md)'s *a census is dated by its premise set*, met on a **named test** rather than on a claim.

**Repaired the same way and in the same act**: the gate keeps `disagreements == 0`, which is the content, and the frozen size becomes a **direction** — `recordsCompared >= 1184`, so a population that *grows* passes and one that *shrinks* (a record that lost its exceedance, which would be a real defect) still fails. The count is written to `stderr` on every run, so a reader sees the population the agreement was taken over instead of inferring it.

## 5. Scope

Two arms of one emitter. `C-0223`'s numbers, its identity, its stated resolution, its `7 of 19` and its `87` are untouched — `T-337` reproduces the `87` and its per-file split member for member at the same pinned ref, which is that claim's own reproduction and this challenge's only cross-check.

**Status** — **RAISED and REPAIRED in the same iteration**, 55. **Two arms, not one**: the working-tree control of §1 was predicted before the run, and §4b's population-dated count was not — `T-337`'s `F6` fired on it, which is what a falsifier declared on *"a gate nobody predicted"* is for.
