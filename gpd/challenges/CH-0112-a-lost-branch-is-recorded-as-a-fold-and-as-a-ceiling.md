# CH-0112 — **One lost branch was recorded THREE times, in three vocabularies: as a FOLD in `C-0039`'s placement table, as a CEILING in `C-0084`'s census, and as a NOTE in `C-0050`'s reach catalogue** — repaired, *"the arm folds before reaching it"* is wrong at 2 of `C-0039`'s 34 placements and the *"element model branch end"* that `CH-0099` was raised about binds at **0 of 108** states rather than 8. **And a fourth record moved for a different reason**: two of `C-0046`'s binding-constraint lists are decided by a tie at the 10 pN allowable that the emitted precision cannot resolve

| | |
|---|---|
| **Raised by** | [`C-0096`](../claims/C-0096-doubling-ladder-repair.md) (`T-159`) |
| **Against** | **(a)** [`C-0039`](../claims/C-0039-two-spring-elastica.md)'s placement table, at the two rows it records as *"FAILS the desired stroke: the arm folds before reaching it"*; **(b)** [`C-0084`](../claims/C-0084-recommended-element-pull-in-fold.md) Deliverable 2's census — *"at **12 of 108** states the branch ended there … and at **8** of those the branch-end bias is the binding ceiling"*; **(c)** [`C-0050`](../claims/C-0050-desired-stroke-reach.md)'s reach catalogue, at the two elastica rows whose refusal note says the arm folds; **and separately (d)** [`C-0046`](../claims/C-0046-fewer-longer-flexures.md)'s binding-constraint lists at two of its 60 trade rows. **Not** against any of those claims' verdicts, all of which survive and one of which is strengthened |
| **Grounds** | `C-0092`/`CH-0107` established that `C-0039`'s **doubling** force ladder loses the small-rotation branch rather than reaching its end, and re-read the **12** states of the recommended device. `T-159` repairs the ladder in place — `forceForDisplacement` continues the branch, anchoring each force step's shooting root on the previous accepted one, and **refuses** rather than returning a root off the branch — and then re-runs **every** study that consumes the changed source. Three published records were written on the lost branch and none of them was in `C-0092`'s twelve |
| **Severity** | **four recorded GROUNDS are wrong and every verdict stands.** `C-0039`'s *"the arm folds"* becomes *"places, but past the 40 pN/nm ceiling at the desired stroke"* at 2 of 34 placements, and its headline — `clears 10 nm` false at 0 of 34 — is **unchanged**, because the two rows fail on the ceiling instead. `C-0084`'s element-boundary census goes **12 → 0** and **8 → 0**, and its bias-margin minima are unchanged to the last digit. `C-0050`'s two notes explain a refusal by a fold where one is a plain contour. `C-0046`'s two lists change under **any** last-ulp perturbation, and no emitted number of either row moves at all |

---

## What is claimed upstream

**`C-0039`**, in the 34-row placement table `T-79` emits, records two rows as failing the desired stroke *because the arm folds*:

| anchorage | hinges | arm | recorded verdict |
|---|---|---|---|
| `A2` duplex end, two strand termini | **3** | 10.2468098 nm | *"FAILS the desired stroke: the arm folds before reaching it"* |
| `A2` duplex end, two strand termini | **4** | 10.6939531 nm | *"FAILS the desired stroke: the arm folds before reaching it"* |

**`C-0084`**, Deliverable 2:

> This is a model boundary and it is reported as one. At **12 of 108** states the branch ended there
> rather than at a fold or at the field's own ceiling, and at **8** of those the branch-end bias is the
> binding ceiling.

## What the repaired solve finds

**(a) `C-0039`'s two rows do not fold. They reach the desired stroke.**

| anchorage | hinges | `reachesDesiredStroke` | secant at 10 nm | tangent at 10 nm | draw-in at 10 nm | repaired verdict |
|---|---|---|---|---|---|---|
| `A2` | 3 | `false` → **`true`** | **263.687031** pN/nm | 8147.38354 | 8.36952853 nm | *"places, but past the 40 pN/nm ceiling at the desired stroke"* |
| `A2` | 4 | `false` → **`true`** | **134.312278** pN/nm | 1480.59159 | 7.19915255 nm | *"places, but past the 40 pN/nm ceiling at the desired stroke"* |

The verdict census over the 34 placements moves from
`{past the ceiling 24, arm shorter than 10 nm 8, folds 2}` to
`{past the ceiling 26, arm shorter than 10 nm 8}` — **the "folds" class is empty.**
Every `findings` string of `T-79` is **byte-identical** across the two runs, which is the evidence
that `C-0039`'s headline is untouched: *"it does not clear §3's desired 10 nm on any anchorage or
hinge count in the sweep"* was true and is still true, on a different ground at 2 of 34 rows.

**(b) `C-0084`'s element boundary binds nowhere.**

| census | published | repaired |
|---|---|---|
| branches that ended on the **element model** | **12 of 108** | **0 of 108** |
| states where the **element-model branch end** is the binding ceiling | **8 of 108** | **0 of 108** |
| `LQ5` binding ceilings over its 54 rows | `{φ = 0.2: 38, pull-in: 4, element: 8, point-ion: 4}` | `{φ = 0.2: 44, pull-in: 4, point-ion: 6}` |
| the element's own path stroke ceiling | **7.909685836937754 nm** | **8.130407059319396 nm** |
| bias margin at 10 nm / 2 mM | 1.3877–2.5764 | **1.3877–7.3137** |
| bias margin at 10 nm / 0.5 mM | 1.8706–3.4699 | **1.8706–10.9072** |

Both bands reproduce `C-0092`'s twelve to the digits it published, on the whole 108-row sweep rather
than on twelve of it — and **both minima are unchanged**, which is the number that governs.

**(c) `C-0050`'s reach catalogue explains two refusals with a fold that is not there.**

| row | element | span | published note | repaired note |
|---|---|---|---|---|
| 15 | `E5a2` two-spring elastica arm | 9.98535536 nm | *"the far-end moment condition never changes sign … the arm folds under 108.378 pN and its elastica has no small-rotation branch"* | *"an inextensible arm of 9.985355359136125 nm cannot lift its tip 10.001 nm"* |
| 17 | `E5a1` two-spring elastica arm | 9.1311565 nm | *"… the arm folds under 97.168 pN and its elastica has no small-rotation branch"* | *"the arm's small-rotation branch does not reach a stroke of 9.1259125 nm: the continuation reached 9.0991884812617 nm at 284.8222866488131 pN …"* |

Both rows carry `secant = 0.0` before and after — they refuse either way — so `C-0050`'s
*"0 of 14 rows clear at the desired stroke"* is untouched. What moves is the **reason**, and the
first of the two is the sharpest: an arm of 9.985 nm was said to *fold* under a stroke of 10.001 nm
when in fact **it is shorter than the stroke it is being asked for**, which is pure geometry and
needs no elastica at all.

## Why (a), (b) and (c) are one challenge

They have one cause and it is not the elastica. A doubling ladder that steps over the last
single-root tip force does not report a branch end; it reports having lost the branch. Where a
*placement table* asks *"does this arm reach 10 nm"*, the lost branch is recorded as **a fold**.
Where an *equilibrium path* asks *"how far can this load line be driven"*, the same lost branch is
recorded as **a model domain** — and `CH-0099` was then raised, correctly, about a taxonomy that had
no name for it. Where a *reach catalogue* asks *"why did this element not answer"*, it is recorded
as **a note**.

> **A solver's bracketing strategy had been written into three claims in three different
> vocabularies, and no claim could see any other's copy of it.**

## (d) And a fourth record moved, for a different reason

`C-0046`'s trade table carries, per row, the list of constraints that bind. Two of its 60 rows
change that list under the repair — and **no emitted number of either row moves at all**:

| row | placement | paths | hinges | published binding list | repaired |
|---|---|---|---|---|---|
| 5 | `P3` secant 33.3333 at 3 nm | 10 | 2 | `[geometric reach, compliance ceiling]` | `[`**`unzip allowable at the working point`**`, geometric reach, compliance ceiling]` |
| 38 | `P10` secant 10 at 10 nm | 10 | 4 | `[unzip at the working point, unzip at the target stroke, C-0017 stability floor]` | `[C-0017 stability floor]` |

The cause is in the file: both rows emit `forcePerPathAtWorking = 10.0` against a **10 pN** unzip
allowable. `C-0017`'s mandate places 100 pN over 10 paths, so the per-path force **is** the allowable
by construction, and the comparison is a floating-point tie whose side a last-ulp change decides.
`CLAUDE.md` already records the rule — *a strict `>` between two quantities that can be EQUAL BY
CONSTRUCTION reports a floating-point tie as a finding* — and `CH-0085` records its companion: *a
tie is a statement that the quoted precision is the wrong one.* **Neither row's verdict moves**:
both fail before and after, on the constraints they keep.

This item is **not** caused by the lost branch. Any last-ulp perturbation anywhere upstream would
have flipped it, and the next one will. It is filed here because it was found here.

## What this does NOT challenge

- **`C-0039`'s verdict.** *"`clears 10 nm` is false at 0 of 34 placements"* stands, and so does every
  one of its findings strings, its cap (12.7198 nm), its draw-in and its `t/s` numbers.
- **`C-0084`'s verdict.** *"No fold at 2 mM at 0 of 6 models"* stands; the re-run finds **no fold
  anywhere it did not already find one**, and the worst margins do not move.
- **`C-0092`'s verdict**, which this repair reproduces on the whole sweep instead of on a twelfth of
  it, and whose contour bound is a theorem no solver can move.
- **`CH-0099`'s grounds.** *A coupling element has a domain and `C-0018`'s three-candidate list has
  no name for it* is right, and the fourth candidate `C-0084` added is right to exist. What is
  withdrawn is its **severity**: the 2.567–3.740× inflation it priced at 8 states is the price of a
  boundary that was 0.22 nm too shallow, and at the corrected boundary the candidate is
  **unexercised**. `CH-0099` is **discharged in its consequence and upheld in its grounds** — the
  same shape as `C-0084`'s own resolution of `CH-0083`.
- **The measurement of the artefact.** `C-0039`'s doubling ladder is retained as an opt-in
  `BranchStrategy.DOUBLING_LADDER` for exactly this reason: `C-0092`'s evidence is a measurement of
  it, and a repair that made its predecessor unmeasurable would replace one unfalsifiable number
  with another. `ladderRefusalStroke()` still reproduces 7.9196867 nm.

## What would settle it

1. **An amendment to `C-0039`'s placement table** replacing *"the arm folds before reaching it"* at
   its two `A2` rows with the repaired reading, and recording that its headline is unchanged.
2. **An amendment to `C-0084` Deliverable 2** replacing the 12-of-108 and 8-of-108 censuses with
   zero, and its 10 nm bands with the repriced ones — the minima unchanged.
3. **A one-line amendment to `C-0050`'s catalogue notes**, which explain two refusals by a fold and
   should explain one of them by a contour.
4. **A decision precision for `C-0046`'s binding lists** — an absolute tolerance at the comparison,
   as `CLAUDE.md` prescribes, so a tie at the allowable is reported as a tie and not as a finding.
5. **A decision on `CH-0099`.** Its candidate should stay in `bindingCeiling`'s vocabulary — a future
   element with a real domain will exercise it — and its quoted price should be withdrawn.
6. **Nothing on the window.** No edge of the design window is a function of this boundary.

## Status

**OPEN.** Filed with `C-0096` (`T-159`). The evidence is
`gpd/results/T-159-doubling-ladder-repair.json` — the `classification`, `repricing` and `downstream`
sections — and `gpd/data/T-159-downstream-diff.json`, which carries the field-by-field diff of every
re-run study with each movement classified.
