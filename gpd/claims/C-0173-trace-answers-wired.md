# C-0173 — the only retained checker that reads the two outward-facing documents **by name** was the only one not run by `tools/verify.sh`, so the numeric, task-status, challenge-status and self-consistency checks ran only when somebody remembered

| | |
|---|---|
| **Task** | `T-277`, raised by [`C-0171`](C-0171-twelfth-answers-synthesis.md) (`T-276`), which derived the checker census and found the sentence describing it right in its number and wrong in its predicate |
| **Leaf** | none — a **process** claim protecting the two deliverables |
| **Verification type** | **logical** — a census of what `tools/verify.sh` runs against what exists, plus the checker's own exit policy read from its source |
| **Verdict** | **PASS.** `tools/trace-answers.py` runs on **both** documents in `tools/verify.sh`, and it already returned its defect count, so wiring cost one line each |
| **Maturity** | **TRL 1–3, and below it: NO PHYSICS CHANGED.** No study re-run, no result file moved |
| **Provenance** | [`tools/verify.sh`](../../tools/verify.sh); the census in [`C-0171`](C-0171-twelfth-answers-synthesis.md) §on `CH-0222` |
| **Conditions** | Nothing physical is computed |
| **Consumes** | [`C-0171`](C-0171-twelfth-answers-synthesis.md) (the census), [`C-0078`](C-0078-status-drift-in-the-deliverable.md) (*a check nobody remembers to ask for is not a check*), [`C-0088`](C-0088-does-the-deliverable-agree-with-itself.md) (a checker's default is part of its logic), [`C-0083`](C-0083-markdown-tables-that-do-not-render.md) (a gate that cannot come clean is not a gate) |
| **Raises** | no challenge. No number was reserved for one |

---

## The claim, in one line

Eight checkers exist, **four** read the deliverables and **seven** were wired — and the one that was
not is `tools/trace-answers.py`, the **only** one that reads `ANSWERS.md` and `DECISIONS-FOR-NDI.md`
by name. It is now wired on both, and it can be a gate because it already **returns its defect
count** rather than printing one: `sys.exit(main())` was there the whole time.

## Why it could be wired without a repair

Three facts, each one command:

- it exits with `ABSENT + stale challenges + self-contradictions`, so it is already gate-shaped;
- it reads **no git** (no `subprocess`, no `git` invocation), so `C-0083`'s snapshot trap — a
  checker that silently checks nothing where there is no `.git` — does not apply to it;
- both documents read **0 ABSENT, 0 contradicted, 0 self-contradictions** at the commit this was
  wired at, so the gate comes clean, which is what `C-0083` requires of a gate at all.

## The shape of the omission

This is `C-0078`'s own finding about itself. That claim established *a check nobody remembers to ask
for is not a check* and retained a checker **for the numbers**; the checker was then run by hand for
twelve syntheses. Every other retained checker was wired as it was written, and this one — the
oldest and the only one aimed at the customer-facing half of the repository — was not.

---

## Validity range

- The gate checks that the deliverables' **tokens trace**, that their **task and challenge status
  assertions** agree with the queue, and that neither **contradicts itself**. It does **not** check
  that a cited number is *apt*, or that a later claim has **superseded** it — `C-0080`'s class, which
  no corpus comparison reaches.
- It is wired on the two documents `T-184` named. A third outward-facing document added later is not
  covered until its name is added, which is `C-0088`'s finding recurring — the derived-set repair
  that `C-0166` applied to the link checker's scope has **not** been applied here.
