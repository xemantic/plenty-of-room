# T-197 — Is a coupled four-layer tile flat under the measured staple dropout?

**Leaf:** `A8.2`
**Raised by:** [`C-0109`](../claims/C-0109-four-layer-tile.md), sharpened by [`C-0120`](../claims/C-0120-cross-section-comparison.md)
**Verification type:** in-silico — an influence surrogate over the grillage, with a Monte Carlo dropout
**Units:** stiffness in pN/nm, length in nm, dishing dimensionless as a fraction of the free-tile stroke

---

## Formulate

`C-0109` grades **one** distribution, **one** placement family and **one** topology on the four-layer tile,
and reports every coupled cell as **worse than the uncoupled tile**.

**That comparison is only decisive if the uncoupled tile is a design the device could have, and it is not.**
§3 requires the actuator to deliver 100 pN to a load, so `C-0017`'s mandate is an **equality on the SUM** of
the coupling stiffnesses: the total is fixed and non-zero **by specification**. What a design may choose is
how to *distribute* it. The uncoupled tile is a **reference**, never a candidate.

So the question is: **at the mandated total, is the four-layer tile flat under `C-0087`'s measured dropout** —
and does either unspent axis change the answer?

### Acceptance predicate

1. The framing above is asserted as a test: the mandate is a sum, it cannot be zero, and a graded distribution
   spends exactly the same budget as an equal one.
2. Coupled cells are graded at the mandated total on **both** cross-sections `C-0120` compares, over a range
   of path counts and both distributions, under the measured dropout.
3. The uncoupled references reproduce `C-0120`'s numbers, licensing every comparison.
4. The **90th percentile** under dropout is the reported statistic, not the zero-defect value — `C-0087`'s
   whole finding is that a flat design is a cancellation with no tolerance to a missing term.

**Falsifiers.** `F1` — no coupled cell is flat at p90 on either cross-section, so `C-0109`'s residual is
irreducible on the axes reachable here. `F2` — the uncoupled references do not reproduce. `F3` — the
**distribution** axis outperforms the **cross-section** axis, which would make the coupling the thing to
design and the tile the thing to accept.

---

## Plan

**Cheap bound first: the attachment pitch against the Winkler reach.** `CLAUDE.md` records that a coupling can
be a *net dishing source* and that the sign flips at a pitch of one bending length. Both are reported per cell,
so a cell that helps or hurts can be read against the criterion rather than only against the outcome.

**Method.** `latticeInfluenceSurrogate` over the four-layer grillage — the influence functions do not depend
on the load, so one bank serves every distribution — then `C-0087`'s measured per-site incorporation as a
Bernoulli field, 4 000 realisations on **one common stream** so that cells differ by design and not by draw.

**Justification against cost.** The surrogate makes this a bank problem rather than a solve problem: a
placement sweep costs one Cholesky per candidate instead of one full lattice solve, which is what makes
sixteen graded cells affordable. **Smoke-run at a toy sample count first**, per `CLAUDE.md`, and read it only
for the plumbing.

**What would falsify the approach.** That the abstract attachment grid does not exist on the honeycomb
lattice — every plan ceiling in this corpus is single-layer **square**-lattice, and `C-0119` establishes the
honeycomb's azimuths are a different inventory. A path count here is therefore a **request**, not a
demonstration that the stations exist, and that limitation is recorded rather than resolved.
