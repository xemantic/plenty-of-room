# T-203 — What attachment lattice does a honeycomb block's top face offer?

**Leaf:** `A8.2`
**Raised by:** [`C-0118`](../claims/C-0118-coupled-four-layer.md), which named it as its own largest caveat, and carried into `ANSWERS.md` §5 by [`C-0121`](../claims/C-0121-sixth-answers-synthesis.md)
**Verification type:** logical (a lattice census) + in-silico (the snapped re-grading)
**Units:** nm and base pairs; counts dimensionless

---

## Formulate

`C-0118` produced the first coupled tile flat at the 90th percentile under the measured folding statistics,
and named its own largest caveat:

> The attachment grid is the **abstract** one. Every plan ceiling, station lattice, crossover phase and
> placement in this corpus is single-layer **square**-lattice, and the honeycomb's three azimuths at 7 bp are a
> different inventory nobody has censused. **A path count here is a request, not a demonstration that the
> stations exist.**

### Acceptance predicate

1. The honeycomb's station lattice is derived from the **primary** rules (`C-0119`'s reading of Douglas et al.)
   rather than by analogy with the square lattice, and the two are stated side by side.
2. The census is computed for both cross-sections `C-0120` compares.
3. Every path count in `C-0118`'s cells is checked against it.
4. Because a count is not a placement, the cells are **re-graded with their stations snapped to the honeycomb's
   own ladder**, and the cost of snapping is reported.

**Falsifiers.** `F1` — some cell demands more stations than the top face supplies, so its flatness is
unbuildable and the caveat stands. `F2` — snapping to the ladder destroys the flatness, so the count is
supplied and the **positions** are the obstruction.

---

## Plan

**Cheap bound first, and it is the distinction most likely to be got wrong.** The honeycomb has **three**
azimuths and a crossover position every **7 bp**, but the same *adjacent pair* only every **21 bp**. An
attachment roots on **one** azimuth, so its ladder is the **21 bp** period and not the 7 bp step — a factor of
three, and the two numbers appear in the same sentence of the source. Asserted as a test.

**Method.** Count the top face: a buried helix has all three azimuths occupied by neighbours and therefore no
free direction to root on, which is the slab analogue of the square lattice's *"a single-layer sheet occupies
two of its four azimuths"*. Then re-grade `C-0118`'s cells on snapped stations using the same surrogate
machinery, with the even and snapped grids sharing one dropout stream so the comparison is paired.

**Justification against cost.** The census is arithmetic. The re-grading is the part that matters and it is
cheap because the influence bank is load-independent. Against that, `C-0118`'s result is currently the
programme's best structural answer and it carries an explicit "may not be buildable" caveat.

**What would falsify the approach.** That a station's existence depends on the scaffold **route** rather than
on the lattice — in which case a census without a routing is not well posed. Recorded as a limitation: this
counts stations, not a placement, and says nothing about seam compatibility.
