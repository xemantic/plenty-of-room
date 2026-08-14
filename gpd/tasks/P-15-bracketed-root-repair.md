# P-15 — repair `bracketedRoot`'s sign tests

| | |
|---|---|
| **Task** | `P-15` (process blocker, raised by `C-0019` / `S-143`) |
| **Leaf** | none — numerics infrastructure under `A2.1`, consumed by `A2.1` and `A2.2` |
| **Verification type** | in-silico (executable), plus a re-run-and-diff of every result file that consumes it |
| **Blocks** | `C-0003`, `C-0011`, `C-0016`, `C-0019`, `C-0027` — every height and chain-length inversion in the programme |
| **Raised by** | [`C-0019`](../claims/C-0019-mean-field-fluctuation-corrections.md), surprise `S-143`: *"`bracketedRoot` can evaluate outside its own bracket."* |

---

## Formulate

### The question

`C-0019` observed `bracketedRoot` calling its residual function at points **outside the bracket it
was given**, and diagnosed the cause correctly in one line: the Illinois step test is written on a
*product*, `atLeft * atEstimate < 0.0`, and when both factors are tiny that product underflows to
`−0.0`, whose comparison against `0.0` is `false`.

It did **not** repair it, and said why: `C-0003`, `C-0011` and `C-0016` all consume this routine,
so a repair is not a code change but a code change plus a re-run and a diff of three result files
and everything downstream of them. That is the task.

The question is therefore two questions, and both must be answered:

> **1. Is the routine correct, for every bracket and every residual magnitude?**
> **2. Does repairing it move any number this programme has published?**

The second is the one that costs, and it is the one that makes this a process blocker rather than a
cleanup. A solver defect that changes an answer invalidates a claim; a solver defect that does not
is a latent hazard for the next task that happens to hit its trigger. Which of the two it is cannot
be asserted — it has to be measured, by re-running and diffing.

### Numeric target and acceptance predicate

Falsifiable, and it resolves to exactly one of two, per result file:

- **(a) NO NUMBER MOVES.** Every `gpd/results/` file consuming the routine is byte-identical after
  the repair, at the rounding the serialisation boundary already imposes. The defect was latent;
  the claims stand unchanged; the repair is banked against the next task that would have hit it.
- **(b) A NUMBER MOVES.** Then the moved number is reported with its size and its direction, the
  consuming claim is **challenged, not overwritten**, and the challenge carries the corrected value.

Additionally, in every branch:

- **(c)** The defect is reproduced by a test that **fails before the repair and passes after**, and
  that test states the property in a form that cannot be satisfied by accident. The property is
  **scale invariance**: multiplying a residual by a positive constant cannot move its root. That is
  exactly what a product test destroys and exactly what no tolerance can restore.
- **(d)** The *entry* test `require(atLeft * atRight <= 0.0)` is checked too. It is the same defect
  in the opposite direction — two tiny residuals of the **same** sign multiply to `+0.0`, which
  satisfies `<= 0.0`, so a bracket containing no root is accepted — and `C-0019` did not name it.
- **(e)** The invariant `C-0019` actually observed being violated — *the function is never evaluated
  outside `[low, high]`* — is asserted directly, as its own test, by recording every argument.
- **(f)** The routine's own doc comment makes a **performance** claim (*"roughly an eightfold saving
  in evaluations"* over bisection) which is load-bearing, because it is the entire justification for
  using Illinois rather than the bisection `T-1` used. It is checked, not assumed.

**Falsification of the task itself, stated in advance.**

- If the scale-invariance test **passes** before the repair, the diagnosis in `S-143` is wrong,
  the escape has some other cause, and this task must be re-planned rather than closed.
- If the repair changes a published number by more than the rounding already applied at the
  serialisation boundary, then this is not a process task at all — it is a correctness challenge
  against whichever claim moved, and it must be filed as one.

---

## Plan

### Method, and the cheap bound first

The cheap bound is **arithmetic, not a run**: the smallest normal double is `≈2.2e-308`, so a
product of two residuals underflows once both are below `≈1.5e-154`. That single number decides
whether the defect is reachable at all in this programme, and it is reachable — the disjoining
pressure of a grafted layer at a 30 nm gap is already four orders of magnitude below the two terms
it is the difference of (`CLAUDE.md`), and `chainLengthForHeight` brackets over four decades of `N`.
So the defect is not hypothetical, and the expensive half — the re-run and diff — is warranted.

The repair itself is the obvious one and is not where the cost is:

1. Test **signs**, never products. The left endpoint's sign is carried as a `Boolean` *separately*
   from `atLeft`, because the Illinois halving mutates `atLeft`'s magnitude — and can eventually
   flush it to zero — while the endpoint it describes has not moved. Where the endpoint *does* move,
   it moves to a point of that same sign, so the flag is an invariant of the whole iteration.
2. Require the secant step to land **strictly inside the live bracket**, falling back to bisection
   when it does not. This is belt-and-braces over (1), and it covers two residual cases the sign
   test alone cannot: a `NaN` step from an endpoint pair whose values have both been halved into
   zero, and a step pushed onto an endpoint by rounding.
3. Keep terminating on the **bracket width**, per `CLAUDE.md` — a residual test cannot be satisfied
   below the noise floor of a quadrature of ~10³ terms, and an unreachable tolerance is silent.

The expensive half is the re-run of every study that consumes the routine, and a diff of each
emitted file against the version in `HEAD`. `T-1d`'s SCF profile sweep alone is ~33 minutes.

### Justification against cost

The alternative to repairing it is to leave a solver in the programme that can return a number that
is not a root, silently, under a condition — a residual spanning decades — that this project's own
`CLAUDE.md` records as ordinary here. There is no cheaper way to establish (b) than to re-run: the
defect's whole character is that it is invisible in the residual, so no post-hoc inspection of an
emitted file can detect it. And a defect that *is* latent is worth exactly the cost of proving it
latent, which is one sweep.

### What would falsify this approach

If the repaired routine's evaluation count were **higher** than the broken one on the residuals this
project actually solves, then Illinois would not be the right method here and the honest answer
would be to fall back to the bisection `T-1` used and pay for it. The doc comment's eightfold claim
is what makes the choice defensible, so it is measured rather than inherited — check (f).

---

## Execute

`src/main/kotlin/brush/GraftedLayer.kt` — `bracketedRoot`.
`src/test/kotlin/brush/BracketedRootTest.kt` — nine tests, written first, five failing before the
repair.

## Verify

1. **Dimensional consistency** — the routine is dimensionless in its residual by construction;
   scale invariance (test 3) is the executable statement of exactly that, and it is the test the
   defect fails.
2. **Limiting cases** — a root at either endpoint returned exactly; both orientations of the sign
   change; a bracket with no sign change rejected however small its residuals.
3. **Symmetry and conservation** — the bracket invariant itself: every evaluation lies inside
   `[low, high]`, asserted by recording every argument.
4. **Numerical convergence** — the evaluation budget against bisection's, on a well-conditioned and
   a strongly convex root.
5. **Cross-check** — the conditional-halving rule against the published Illinois method
   (Dowell & Jarratt): the halving applies to the endpoint retained **twice in a row**.
