# CH-0190 — a synthesis claim that quotes a defective token **in order to diagnose it** becomes that token's owner, so the numeric tracer reports the defect as traceable — and the only two tokens in either deliverable whose sole owner is a synthesis claim are the two a synthesis claim **repaired in the other file**

| | |
|---|---|
| **Against** | [`C-0115`](../claims/C-0115-fifth-answers-synthesis.md) §, *"its owning claim states at full width (`0.0344013403`, `0.0577199433`, `0.0910197`). Repaired by restoring"*, and [`C-0067`](../claims/C-0067-answers-reconciliation.md) §U1, *"the adverse mounting is **42.4–61.0** pN/nm … corrected to the claim's own figure"* — **both repairs are complete in `ANSWERS.md` and neither reached `DECISIONS-FOR-NDI.md`**; and against [`tools/trace-answers.py`](../../tools/trace-answers.py)'s numeric pass, which cannot see either survivor |
| **Raised by** | [`C-0149`](../claims/C-0149-ninth-answers-synthesis.md) (`T-240`) |
| **Kind** | **methodological** — a retained checker whose corpus **includes the claim that diagnoses the defect**, so a diagnosed defect is indistinguishable from an owned value |
| **Status** | **OPEN as a tool question; the two instances are REPAIRED** in `DECISIONS-FOR-NDI.md` by `C-0149` |

---

## The ground

`tools/trace-answers.py` classifies every numeric token in a deliverable as `CITED` (present in a
claim the passage cites), `ELSEWHERE` (present in some other claim) or `ABSENT`.
Both deliverables read **0 ABSENT**, before this iteration and after it.

**But a synthesis claim is in the corpus the tracer searches, and a synthesis claim quotes the
defective token in order to say what is wrong with it.**
`C-0067` §U1 writes *"42.4"* so that it can say `C-0032` states **42.38–61.04**;
`C-0115` writes *"0.0344"* so that it can say `C-0093` and `C-0116` state **0.0344013403**.
From then on both tokens trace — to the claim that diagnosed them.

## The measurement, and it is one pass

Partition the tracer's own output by owner.
Measured on the corpus **as it stood at the start of iteration 36**, before this challenge existed:
of **2 097** numeric tokens across the two documents (1 505 in `ANSWERS.md`, 592 in
`DECISIONS-FOR-NDI.md`), **exactly three rows** have an owner set consisting *only* of synthesis claims
(`C-0067`, `C-0080`, `C-0106`, `C-0115`, `C-0121`, `C-0130`, `C-0145`):

| document | token | sole owner | what it is |
|---|---|---|---|
| `ANSWERS.md` | `42.4` | `C-0067` | inside `ANSWERS.md`'s **own record of the repair** — *"read `42.4–61.0`, which is `C-0032`'s figure rounded"* — i.e. correctly retained under `C-0071`'s *strike, never delete* |
| `DECISIONS-FOR-NDI.md` | `42.4` | `C-0067` | **a live, unqualified assertion**, twenty-four iterations after the repair |
| `DECISIONS-FOR-NDI.md` | `0.0344` | `C-0115` | **a live, unqualified assertion**, in the file `C-0115` itself brought into the checker's default scope |

> **The only two live survivors in either document are the two tokens the two audits were written
> about.** That is not a coincidence and it is not bad luck: a repair is applied where the checker
> **reported**, and the checker reported one file.

**And the measurement destroys itself, which is the sharpest statement of the defect.**
This challenge quotes `42.4` and `0.0344` in order to diagnose them, so from the moment it is filed both
tokens have a **non-synthesis** owner and the partition above returns **zero rows**.
Re-running it after the fact reports that the problem does not exist.
**A census over a corpus that contains the census is not repeatable**, and the date it was taken is part of
the result — the same shape as `CH-0182`'s *a census is dated by its premise set*, one level further out.

## Why the existing checks cannot reach it

- **The numeric pass cannot**, by the argument above: the token traces.
- **The status pass cannot**: neither survivor carries a task ID or a closing word.
- **The self-consistency pass cannot**: `ANSWERS.md` and `DECISIONS-FOR-NDI.md` are checked
  separately, and `trace-answers.py` has no cross-document mode at all — which is the same blind
  spot `C-0115` recorded from the other side when it found that the decision file *"carried no
  checker"* and that `tools/trace-answers.py --answers DECISIONS-FOR-NDI.md` found three of its
  defects **running unmodified**. A default was added; a **comparison** was not.
- **`tools/T-234-census.py` cannot**: its families are built from a named premise set, and a
  rounding is not a premise.

## What would close it

Two candidates, neither shipped here, and the reason is `C-0080`'s standing rule that an
**unmeasured false-positive rate is what makes a checker stop being believed**:

1. **Exclude synthesis claims from the numeric owner set**, or rank them last. Cheap, and it turns
   both survivors into `ABSENT` immediately. **The false-positive rate is not measured**: a
   synthesis claim is a legitimate owner of its own derived ratios (`C-0145`'s `4.06×` is the live
   example), so the rule would have to distinguish *diagnosed* tokens from *derived* ones, which is
   the same `superseded-by`-at-statement-granularity edge `C-0080` priced and declined.
2. **A cross-document pass**: flag any token that appears live in one deliverable and only inside a
   struck or qualified span in the other. This is the one that would have caught both instances,
   and it needs no corpus convention change — only the strike-blanking `C-0071`'s discipline
   already requires. It is the cheaper of the two and it is recommended.

## What it does NOT establish

- **Neither survivor moves a verdict.** `42.38–61.04` against `42.4–61.0` is a rounding; the
  adverse mounting fails either way — though **on a different ground**, `C-0035`'s effort point,
  since `C-0049` withdrew the 40 pN/nm ceiling the same sentence cites. `0.0344013403` against
  `0.0344` is a precision, and `C-0115`'s own finding is that **the trap runs both ways**.
- **The census is over the two deliverables only.** Nothing here says how many diagnosed tokens
  stand elsewhere in the corpus; claims are not swept by this tracer at all.
