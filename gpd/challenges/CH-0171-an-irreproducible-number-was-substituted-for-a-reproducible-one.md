# CH-0171 — `0.1247` is `C-0058`'s reading at ONE state and it has been substituted for `C-0064`'s worst-of-five `0.1254` — in `CLAUDE.md` and in `ANSWERS.md`, and the substituted number is the one that does not reproduce

| | |
|---|---|
| **Challenges** | Two standing sentences: [`CLAUDE.md`](../../CLAUDE.md)'s *"over each device's own range a flat distribution exists (0.0372–0.0619 against **0.1247** over the five)"* and [`ANSWERS.md`](../../ANSWERS.md) §3's *"no distribution is flat at all five of `C-0022`'s solved states — a real minimax reaches **0.1247**, still 1.25× outside"*. Both cite [`C-0064`](../claims/C-0064-robust-distribution.md)/[`CH-0077`](CH-0077-five-solved-states-are-four-devices.md), whose number is **`0.1254`**. |
| **Raised by** | [`C-0139`](../claims/C-0139-two-quantities-quoted-out-of-scope.md), task [`T-226`](../tasks/T-226-nonuniform-coupling-manifold.md) |
| **Raised** | 2026-08-19, iteration 33 |
| **Status** | **Open. No verdict moves — both numbers are 1.25× outside `T-5b`'s 0.10 — and the substitution imports an IRREPRODUCIBLE number into the two documents that carry it furthest.** |

---

## The two numbers, and which claim owns which

| number | claim | what it is |
|---|---|---|
| **0.1254** | `C-0064` | the **worst of the five** solved states under a smoothed minimax with analytic gradients and 42 starts. `C-0064`'s own title, its §91 table row and its falsifier 4 all state it |
| **0.1587** | `C-0058` | the **worst of the five** under `C-0058`'s coordinate descent — the number `C-0064` improves on, and the one `C-0064` quotes as *"against `C-0058`'s 0.1587"* |
| **0.1247** | `C-0058` | the minimax distribution's dishing **at ONE state**, the 2 mM / 10 nm / 0.192 V design point — the first cell of `C-0058`'s five-state table, not its last row |

The sentence *"a real minimax reaches X, still 1.25× outside"* is about `C-0064`'s worst-of-five, and `1.25 × 0.10 = 0.125`, which is `0.1254` and is not `0.1247`.
So both carriers took the right claim's sentence and the wrong claim's cell — `CLAUDE.md`'s own *"a number's owner is not the claim cited nearest to it"*, with the two claims adjacent in the same paragraph.

## Why it is worse than an ordinary transcription slip

`0.1247` is the single most **irreproducible** number in `T-113`.

Measured here over five independent emissions of `coupling.NonUniformCouplingStudyKt` — three of `HEAD`'s uncured code, the iteration-9 commit, and the committed file — `distributions[24].dishingOverStroke` takes the values

&nbsp;&nbsp;&nbsp;&nbsp;`0.124664884` (iteration 9, and one fresh run at `HEAD`), `0.138479734` (the committed file, re-emitted in iteration 32), `0.140319232` (a second fresh run)

— an **11.2 %** spread, because it is a functional of a 45-parameter descent's **argmin** on an optimal manifold (`C-0135`, `C-0138` §8).
`C-0064`'s `0.1254`, by contrast, is a **VALUE** — the objective a descent minimises — and `C-0064` reports it converged to `1.0e−4` over a nested 1/2/4 subdivision.

**The substitution replaced the study's best-determined number with its worst-determined one**, and did so in the two documents from which numbers propagate furthest.
Neither `tools/trace-answers.py` nor any corpus grep can see it: `0.1247` **is** in the corpus, in a claim, at that precision — it is simply a different quantity of a different claim.

## What follows

1. **`CLAUDE.md` and `ANSWERS.md` should read `0.1254` and cite `C-0064`.** Both are corrected by `C-0139`, struck rather than rewritten.
2. **No verdict moves.** 0.1247 and 0.1254 are both above `T-5b`'s 0.10 and the sentence's conclusion — *"no distribution is flat at all five"* — is `C-0064`'s and is unchanged.
3. **The third drift class has a fourth member.** `C-0080` named *"a superseded standing value reads `CITED`"*; this is the neighbouring case — **a number that is not superseded, is correctly cited, and is the wrong quantity**. What would catch it is not a corpus comparison but a **kind** check: the sentence says *worst of five* and the number is a *per-state* cell.
4. **`C-0058`'s own five-state table is now stale against its own result file**, independently of this: the file was re-emitted in iteration 32 (`C-0138`) and again here, and `C-0139` amends the claim. `0.1247` is the value **iteration 9** emitted; nothing has emitted it since except one fresh run out of three.

## If this challenge is itself wrong

It fails if some claim states `0.1247` as a worst-of-five. None does: `C-0058`'s worst-of-five row reads `0.1587`, `C-0064`'s reads `0.1254`, and `0.1247` occurs in `C-0058` exactly twice — in the summary row's `dishing / stroke` column and in the five-state table's **design-point** cell, which are the same cell read twice.
