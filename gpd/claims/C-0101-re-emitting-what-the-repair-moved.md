# C-0101 — The eleven result files `T-159`'s repair moved are re-emitted, and the judgement `T-167` was left is settled the only way it can be: **both must agree, because git already holds the history.** The re-emission then paid for itself — two of `C-0050`'s catalogue notes explained a refusal by *"the arm folds"* at a near-end rotation of **3.03 × 10¹²¹**, a diverged shooting parameter recorded as physics, and the repaired run replaces both with the real reason

| | |
|---|---|
| **Task** | [`T-167`](../../TASKS.md) — raised by [`C-0096`](C-0096-doubling-ladder-repair.md)/[`CH-0112`](../challenges/CH-0112-a-lost-branch-is-recorded-as-a-fold-and-as-a-ceiling.md); taken by the coordinator in iteration 20 while `T-165`, `T-164` and `T-160` ran in parallel |
| **Verification type** | **logical** (a policy decision with its reasons) **+ in-silico** (eleven studies re-run through `tools/study.sh` on isolated trees and diffed field by field) |
| **Verdict** | **PASS. Re-emit, and amend the claim where a number it quotes moved** — the alternative, keeping a stale file as *"the record of what the claim was written on"*, is answered by version control and costs the repository its reproducibility. **All eleven re-emitted.** The declared expectation that no verdict moves is **false**: two of `C-0050`'s notes did, and they were the two the repair exists to correct. |
| **Maturity** | **TRL 1–3.** No physics is re-derived here; eleven files are brought back into agreement with the code that produces them. |
| **Provenance** | Eleven `tools/study.sh` runs; the classified field-by-field diff in `gpd/data/T-159-downstream-diff.json`, produced by `tools/T-159-result-diff.py` |
| **Conditions** | The tree at commit `acbc4f0`, with `anchoring/TwoSpringElastica.kt` carrying `C-0096`'s branch continuation. |
| **Consumes** | [`C-0096`](C-0096-doubling-ladder-repair.md) (the repair and the diff), [`CH-0112`](../challenges/CH-0112-a-lost-branch-is-recorded-as-a-fold-and-as-a-ceiling.md), [`CH-0092`](../challenges/CH-0092-the-propagation-did-not-close.md) (the precedent) |
| **Constrains** | [`C-0050`](C-0050-desired-stroke-reach.md), whose two catalogue notes are corrected in place. `C-0039`, `C-0084` and the eight other files move numerically with **no verdict change**. |

---

## 1. The judgement

`T-167` posed it fairly: are the committed files *"the record of what their claims were written on"*, or should they be the ones the code produces? `T-159` deliberately copied nothing back, on the principle that **a moved number belongs to the claim that owns it**.

**The answer is that the question has a false premise, and the reason is one line: git already holds the history.** Every superseded file is one `git show` away, permanently and exactly. Keeping a stale file in the working tree to preserve a record duplicates version control and pays a real price for it:

- **Every future re-run diff becomes noise.** This repository's central reproducibility instrument is *"re-run the study and diff byte-for-byte"* — `C-0073`, `C-0031` and half the claims of the last ten iterations rest on it. A file that cannot be reproduced from the code makes that test meaningless for as long as it stands.
- **`CH-0092` already priced the alternative.** Two files sat stale for one iteration and their own reproduction residual recorded the staleness at `8.79e−07`, unread. That was an *accident*; leaving these would be a policy.
- **The reader-census gate (`C-0082`) assumes it.** A dependency graph over `gpd/results/` is a statement about what regenerates what.

So: **re-emit, and amend the claim wherever a number it quotes moved.** The two halves are not in tension — the claim keeps its verdict and its provenance, and the file keeps its reproducibility.

## 2. What moved

Eleven files, re-emitted; **three needed `--drop-file`** for a sibling's mid-TDD `electrostatics/EdgeWidthDependenceStudy.kt`, which is the documented multi-agent hazard and cost one retry.

| file | changed lines | |
|---|---|---|
| `T-149` | 284 | the fold study itself, intended |
| `T-79` | 38 | the elastica's own study |
| `T-136` | 20 | |
| `T-99`, `T-157` | 14 | |
| `T-108` | 12 | **carries the two verdict changes** |
| `T-134`, `T-152` | 8 | |
| `T-116`, `T-135`, `T-138` | 2 | |

## 3. The two notes that moved, and why they justify the whole exercise

`C-0096`'s falsifier `F3` predicted that `C-0050`'s reach catalogue *"explains a refusal by a fold"*. It does, and the old text is worse than the prediction:

> **before** — *"the far-end moment condition never changes sign below a near-end rotation of
> **3.033032179558636E121**: the arm folds under 108.37760001695746 pN and its elastica has no
> small-rotation branch…"*

**A shooting parameter of 3 × 10¹²¹ is not a physical rotation.** It is the doubling ladder diverging, and the study wrote it into a catalogue note as a *fold* — which is exactly `CH-0112`'s claim that **a lost branch is recorded as physics, in a vocabulary that hides it**.

The repaired run replaces both notes with the actual reasons, and they are different from each other and from a fold:

> **after (row 1)** — *"an inextensible arm of 9.985355359136125 nm cannot lift its tip 10.001 nm"* —
> a **kinematic** refusal: the contour is shorter than the demanded stroke, which needs no solver at
> all and is `C-0092`'s `δ = ∫sin φ < L` in one line.
>
> **after (row 2)** — *"the arm's small-rotation branch does not reach a stroke of 9.1259125 nm: the
> continuation reached 9.0991884812617 nm at 284.8222866488131 pN"* — a **genuine** branch limit, at
> 99.7 % of the demanded stroke rather than the 89 % the ladder reported.

**`C-0050`'s verdict does not move** — the desired stroke is still unreachable in both rows — **but its stated ground was wrong in both**, which is precisely the failure `C-0078` named: *a verdict that survives can survive on a different reason.* Corrected in place.

## 4. One thing the re-emission introduced, and it is flagged rather than fixed

The new text of row 1's sibling reads *"places, but past the **40 pN/nm** ceiling at the desired stroke"* — and **that ceiling was withdrawn by `C-0049`**, which found it to be `1.2 × (100 pN / 3 nm)`, i.e. the *acceptable* clause's number read at the *desired* stroke. The study's own prose inherits it.

It is not corrected here because correcting it means editing a study's emitted string and re-running, which is the next agent's work, not a coordinator's mid-iteration edit. **Raised as `T-169`.** The verdict it accompanies is unaffected — the row fails on the reach either way.

## 5. What this does not settle

- **Nine of the eleven moved numerically with no verdict change**, and their claims are not amended, because none quotes a moved figure. That was checked against `gpd/data/T-159-downstream-diff.json`'s classification rather than assumed.
- `C-0084`'s Deliverable 2 census and `C-0039`'s two placement rows — the other records `CH-0112` names — are **already** corrected by `C-0092` and `C-0096`, which re-read them at the repaired domain. Verified, not re-done.
- **There is still no `CLAUDE.md` rule for the general case**, and there should be one; it is added with this claim.
