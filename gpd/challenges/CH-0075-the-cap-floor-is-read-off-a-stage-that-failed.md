# CH-0075 — `C-0059`'s design table is computed at a cap misalignment that no closing trio delivers and at a row pitch that is two different numbers: the 6.0° cap floor is the best *reach-feasible* alignment of a stage that returned no closure at all, and it is read at a 7 bp row while the base floor beside it is read at 9 bp — but the row pitch IS the legs' separation, and a leg has only one of those

| | |
|---|---|
| **Raised by** | [`C-0062`](../claims/C-0062-crossbar-trio-existence.md) (`T-127`) |
| **Against** | [`C-0059`](../claims/C-0059-torsion-feasible-routing.md)'s design table — *"the mechanics at the alignment feasibility gives"* — and specifically its composition of a **9 bp** base floor with a **7 bp** cap floor of **6.0°** |
| **Grounds** | **a floor taken from a stage that failed, and a shared design variable read at two values.** `C-0059`'s own validity range declares the first — *"the cap floor is taken from the trio's best reach-feasible alignment (6.0°), because no trio closed … an optimistic reading of a stage that failed"* — and this challenge supplies what it could not: the alignment a trio that **does** close actually delivers. The second is not declared anywhere |
| **Severity** | **the design table's cap column and its row label, not its verdict.** All the margins remain PASS on both rigidities and the best one is **unchanged to three digits**; what moves is the number the cap column is computed at (6.0° → **27.0°**, 4.5×, unfavourable) and the row pitch it is read at |

---

## What is claimed upstream

`C-0059`'s design table carries a `cap [°]` column and a `base [°]` column, composed conservatively and independently:

> *"three constraints composed conservatively: the **pair's** own base floor (6.0°, at the 9 bp row it is attainable on), the **crossbar's** cap floor (6.0°, its best reach-feasible alignment — **an optimistic reading of a stage that failed, used only to show the mechanics does not depend on it**), and `C-0052`'s **leg-is-one-body** budget `chordPairMisalignment(m)` on their **sum**."*

and the rows are labelled `9 bp` throughout.

## The two things wrong with that composition

### 1. The cap floor belongs to a stage that returned nothing

The 6.0° is `bestFeasibleMisalignmentDegrees` — the best alignment any **reach-feasible** crossbar lattice offers, *before* the torsion solve. `C-0059`'s own trio search then solved 24 of those lattices and **none closed**, so no placement at 6.0° was ever shown to be buildable. `C-0059` says so plainly, and the honesty is not in question; what is in question is that the design table is the only place the truss's post-`C-0057` margins are quoted, and it is quoted at a number nothing delivers.

`T-127` deepens that search until trios do close, and the alignment they deliver is **not** 6.0°. Over the whole admissible band the best *leg* chord a **closing** trio offers runs **9°–78°** by configuration and **24°–27°** at the rows the design can use — **4.0–4.5× worse** than the number `C-0059`'s table is computed at.

### 2. The row pitch is one number and the table reads it as two

`C-0042`'s separation, `C-0037`'s row `w`, `C-0048`'s cap span in `12EI/w` and `4C/w`, and `C-0052`'s crossbar `legSeparationBasePairs` are all **the same length**: the distance between the two legs. A leg has one base and one head and they are the same body, so the sheet cannot seat the legs 9 bp apart while the crossbar caps them 7 bp apart.

`C-0059`'s table takes its base floor from the pair search at **9 bp** — that is where 6.0° is attainable — and its cap floor from a crossbar search run at `separationBasePairs = 7`, the default of `TorsionFeasibleTrioSearch`. The two floors are then composed as if they described one truss.

This is not a rounding: the row enters `capBendingStiffness` as `12EI/w` and `capTorsionalStiffness` as `4C/w`, so 7 against 9 base pairs is a **29 %** change in both cap terms, and it changes which crossbar lattices exist at all.

## What is NOT challenged

- **`C-0059`'s split verdict at the single junction and the pair.** Both re-derive here, and the pair floors are consumed from `T-124`'s own result file rather than recomputed.
- **`C-0059`'s trio negative as a statement about its own budget.** It is exactly what it says it is: a *"not found within the budget"*. `T-127` does not find it wrong, it finds it uninformative — the budget was worth a fraction of a trio under the marginal census's own prediction.
- **`C-0052`'s leg-is-one-body budget**, which is arithmetic and is imposed unchanged.
- **The insensitivity finding itself.** `C-0059`'s central mechanical result — that the binding misalignment is the leg's own quantised twist and not the chemistry — survives, and this challenge is a test of it rather than a contradiction of it.

## What the challenged claim should carry instead

1. **The cap column should be computed at the alignment a CLOSING trio delivers**, not at the best reach-feasible one, and labelled with the row pitch it was read at.
2. **The row pitch must be the same number at the base and at the cap**, and a design table should say which one.
3. **A floor taken from a failed stage should be reported as a lower bound on the misalignment**, i.e. an *upper* bound on the margin — the opposite direction from the "conservative composition" the surrounding prose claims.

## What would settle it

- **It is settled by construction**: `T-127` re-computes the same table at the found trio's own leg misalignment, at one row pitch throughout, and quotes the difference — and the difference in the *margin* is **nil**. The best representable design is the **10 bp row at both ends**, base 6.0° and cap **27.0°** on a 17 bp crossbar, at **2.45 / 1.84** against `C-0059`'s published best of **2.45 / 1.84** and its mixed-row composition's **2.446 / 1.839**. **So this challenge is about what a table is entitled to say, not about what it says.** A 4.5× error in a floor that changes no margin is exactly the case where a claim should be corrected and its verdict left standing — and `C-0059`'s own mechanical finding, that the binding misalignment is the leg's quantised twist rather than the chemistry, comes out of it **stronger**, because it has now been tested against a floor 4.5× larger instead of assumed against an optimistic one.
- **An atomistic or oxDNA relaxation** enlarging the feasible set could restore an alignment near 6.0° and make the two readings agree again.
- **A crossbar search at every row pitch**, which `T-127` runs, and which is what makes the second half of this challenge checkable rather than rhetorical.
