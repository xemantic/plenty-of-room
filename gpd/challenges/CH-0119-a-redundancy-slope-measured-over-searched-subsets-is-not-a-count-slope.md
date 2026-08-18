# CH-0119 — **A redundancy slope measured over placement-SEARCHED subsets is not a count slope, and on one lattice the two differ by a sign.** `C-0098`'s Deliverable 3 reports the real upward lattice's redundancy slope as **−0.376769756**, *"2.08× SHALLOWER"* than the abstract grid's, and offers a lattice mechanism for it: the upward line's bare 32 bp pitch fixes the column count at four, so a further tie is added inside a column rather than as a new one. That mechanism may well be true. **What is challenged is that the number measures it**: the six points it is fitted through are the output of a first-improvement subset **descent**, and on the same lattice, at the same phase, under the same ensemble, `C-0103` measures **−0.740086889** on a *nested* chain and **+0.0610348337** on a *searched* one — the same data, differing only in whether the placement was allowed to move, and the searched fit has the **wrong sign**

| | |
|---|---|
| **Raised by** | [`C-0103`](../claims/C-0103-path-count-at-fixed-geometry.md) (`T-163`) |
| **Against** | [`C-0098`](../claims/C-0098-shared-body-placement-and-distribution.md)'s **Deliverable 3** — the real-lattice redundancy fit, its slope **−0.376769756**, its *"2.08× shallower"* reading and the mechanism attributed to it |
| **Grounds** | **a slope fitted through six placement-searched optima confounds the count with the tightness of the search at each count, and `C-0098` records the premise for this itself.** Its own validity range says *"the subset search is a first-improvement descent on an objective that ranks at ρ = 0.47, so the fixed-count rows are **upper bounds** on what the real lattice reaches at those counts, and the 34 → 45 non-monotonicity is a property of the search rather than of the lattice."* An upper bound whose tightness varies with the count is exactly what a slope cannot be read through. `C-0103` measures the size of the effect on an array at phase 24: **nested chains −0.740086889 and −0.704431429; the placement-searched family +0.0610348337**, i.e. **12.1×** apart in magnitude and opposite in sign |
| **Severity** | **one number and its mechanism, not a verdict.** `C-0098`'s headline **0.375506727**, its 60-site census, its kinematic limit, its 1.026× distribution axis and its whole negative are untouched — the 3.76× shortfall does not depend on the slope. What moves is (a) the **value** −0.376769756 as a measurement of the lattice's count axis, (b) the **comparison** *"2.08× shallower than the abstract grid's"*, and (c) the **causal claim** that the shallowness is the 32 bp pitch. `C-0098`'s own predicted-at-60 figure (0.374687365) is a reading of the same fit and travels with it |

---

## What is claimed upstream

`C-0098` Deliverable 3, in full:

| | `C-0093`, abstract `m × 15` grids | `C-0098`, the real upward lattice |
|---|---|---|
| points | 7 | 6 |
| **slope `d ln p90 / d ln n`** | **−0.784357442** | **−0.376769756** |
| predicted at the lattice's 60 sites | 0.30833421 | 0.374687365 |
| fitted crossing at 0.10 | 252.126899 | 1998.70962 — not quotable |

> *"The real lattice's redundancy slope is 2.08× SHALLOWER than the abstract grid's, because the
> upward line's bare 32 bp pitch fixes the column count at **four** and every further tie is
> added *inside* those columns rather than as a new one. A dropout is an increase in the
> attachment **pitch**, and a tie that shares a column with its neighbours does not shorten that
> pitch."*

And the six points it is fitted through are its own subset-search rows: 15, 20, 26, 34, 45 and
53 ties at phase 24, each the output of `descendTieSubset` — *"a first-improvement descent … the
result is reported as what was found"* — one of which (34 → 45, 0.429016162 → 0.460342175) is
already non-monotone and is attributed by `C-0098` itself to the search.

## Why this is a challenge and not a note

**`C-0098` states the premise and then draws the conclusion the premise forbids.** It says the
rows are upper bounds of unstated tightness; a slope is a statement about how a quantity changes
with a variable, and a sequence of upper bounds whose slack varies with that variable does not
carry one. It then goes further and attributes the *value* to a lattice property.

`C-0103` supplies the size of the confound, on an array at the same phase of the same lattice
under the same ensemble and the same incorporation field:

| family at phase 24, six counts 22–45 | fitted slope | monotone? |
|---|---|---|
| **nested chain A** (`C-0072`'s interior-root rule) — station geometry held fixed | **−0.740086889** | yes, at every step |
| **nested chain B** (the centro-symmetric mirror-pair rule) — geometry held fixed | **−0.704431429** | yes, at every step |
| **placement-searched** (`C-0098`'s own `descendTieSubset` on `C-0089`'s cheap objective) | **+0.0610348337** | **no** |

The two nested chains agree with each other to 5 %. The searched family, built from the same
53 candidate sites, the same load, the same 10 000 realisations and the same stiffness
convention, fits a slope of the **opposite sign** — because a descent at 22 paths finds a
placement 2.2× better than a nested subset does while at 34 paths it finds one only 1.6× better,
and the difference between those two slacks is the whole apparent count effect. **A search that
is uniformly tighter at low counts manufactures a shallow slope, and one that is tighter at high
counts manufactures a steep one.**

## What is NOT claimed

- **`C-0098`'s mechanism is not refuted.** The 32 bp pitch really does fix the columns at four,
  and that really would flatten a count axis. The challenge is that its own Deliverable 3 does
  not measure it, so the 2.08× is not evidence for it.
- **The topologies differ.** `C-0098` measures a **shared body** and `C-0103` an **array**, and
  the two are not required to have the same slope. The transferable part is methodological: the
  sign flip in `C-0103` happens *within one topology*, so it is a property of the search and not
  of the topology.
- **No headline of `C-0098` moves.** Its 0.375506727, its 100 % exceedance, its 3.76× gap, its
  60-site census, `CH-0113` and `CH-0114` are all untouched.
- **`C-0093`'s abstract-grid slope (−0.784357442) is not challenged**: its points are equal
  tributary grids at fixed construction, not search outputs.

## What would settle it

1. **A NESTED count chain on the shared-body topology at phase 24** — the same six counts, the
   subsets nested by construction rather than searched — and its slope beside the −0.376769756.
   It is `C-0103`'s construction applied to `C-0098`'s topology and costs six graded cells.
2. **A tightness measurement on `C-0098`'s own rows**: the oracle floor
   (`reachableDishingFloorAt`) at each searched count divided by the searched value, which says
   directly whether the slack varies with the count. `C-0098` already computes that floor at one
   count.
3. **An exhaustive enumeration at two counts.** `C-0098` names this as one of its own four
   failure routes. Two exact optima would bound the slack at both ends of the fit.

## Suggested disposition

**Requalify `C-0098`'s Deliverable 3 as a fitted description of its own searched rows rather than
a measurement of the lattice's count axis**, withdraw the *"2.08× shallower"* comparison and the
mechanism attached to it until (1) is run, and keep the row with the qualification `C-0098`
already wrote in its validity range. Add the nested shared-body chain to the queue as **`T-180`**.
No number of `C-0098` is withdrawn.
