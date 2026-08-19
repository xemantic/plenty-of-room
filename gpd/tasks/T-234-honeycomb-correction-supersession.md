# T-234 — What did `C-0141` and `C-0140` supersede in the queue and in the corpus?

**Leaf:** — (process; it prevents wasted work and stops a withdrawn premise being re-quoted)
**Raised by:** the coordinator, reading the open queue and the corpus after iteration 33
**Verification type:** logical
**Units:** none

---

## Formulate

Iteration 33 filed two structural corrections and neither swept what it moved.

- [`C-0141`](../claims/C-0141-honeycomb-station-lattice-and-placement.md):
  **the cross-section every four-layer claim is written on is not a honeycomb.**
  A honeycomb's in-plane row pitch is `3d/2` and its layer pitch `d√3/2`,
  so every four-layer `edgeY` in the corpus is exactly **1.5×** too small;
  the footprint ordering between `15 × 4` and `10 × 6` **reverses**;
  `C-0116`'s interlayer-coupling threshold for `15 × 4` moves from `0.0788618807` to `0.276970522`,
  **inside** the measured 0.26–0.33 band, so that tile fails `T-5b` at the band's low end.
  It also establishes that a honeycomb face carries **exactly one** rooting azimuth per helix, at **30°**,
  and **no perpendicular root anywhere**.
- [`C-0140`](../claims/C-0140-honeycomb-raster-turn-sense.md):
  **a honeycomb x-raster carries BOTH turn senses**, so there is no uniform honeycomb row length at all;
  `C-0119` is overturned in the reading *"drawable at a uniform width"*;
  and design (i) is a **p7560** design, not p8064.

`CLAUDE.md` records the hazard in three forms and all three apply:
**a discharge is invisible to whoever files the removal**;
*when a branch is killed, sweep its open questions*;
and **a claim's consequences are not confined to the task it was written for**.
It also records that **every placement, phase and plan ceiling in this corpus is a single-layer SQUARE-lattice result and does not transfer to a honeycomb face** —
so if the recommended tile is honeycomb, part of the standing queue answers a question about a body the programme no longer proposes.

[`T-205`](T-205-four-layer-supersession.md)/[`C-0126`](../claims/C-0126-four-layer-supersession.md) is the precedent and the shape to follow.
It classified the **queue** three ways.
This task needs **four** classes and a **corpus** half as well,
because `C-0141` and `C-0140` do something `C-0109`–`C-0123` did not:
they **withdraw premises that standing claims and both deliverables assert as numbers**.

### Acceptance predicate

1. **A mechanical census runs BEFORE any reading**, over five named premise families —
   a four-layer footprint, a honeycomb width, a perpendicular root, a scaffold identity,
   and a placement transferred from the square lattice —
   and the census is retained as a tool with self-tests so it can be re-run.
2. **Every open queue item** is classified **SUPERSEDED** (the question no longer applies),
   **CONTINGENT** (it applies only under a reading now in doubt),
   **REPRICED** (it applies, and its cost or value changed) or **UNAFFECTED**,
   with a reason per non-`UNAFFECTED` item,
   and the open set derived from `TASKS.md` by the same `queue_status` the deliverable's checker uses.
3. **Every corpus statement** the two claims move is listed with **file, line and the exact string**,
   and every listed string is grepped out of the file it names before publishing.
4. The queue and the claims are repaired **strike-not-delete** (`C-0071`'s rule),
   each strike marked with the claim that superseded it.
   **The two deliverables are NOT edited** — `T-233` owns them next iteration —
   and what this task produces for them is the list.

**Falsifier `F1`.** If no open item and no corpus statement is moved by either claim,
the corrections are self-contained and this sweep is unnecessary — a null worth recording.

**Falsifier `F2`.** If every moved item is SUPERSEDED — i.e. the four-way classification collapses to the
two-way one `T-205` already had — then the extra classes are invention rather than discovery, and the right
answer was `C-0126`'s vocabulary unchanged.

**Falsifier `F3`.** If a listed string cannot be grepped out of the file and line it names, the census is
reporting its own reconstruction rather than the corpus, and the whole list is void.

---

## Plan

**It is a read, and the census is the cheap bound.** Grep the five premise families out of the corpus first,
count them, and use the reading only to **classify** — never to find. That ordering is what makes the
denominator falsifiable: a hand-assembled list of *"places I remembered"* has no denominator at all.

**Method.**

1. `tools/T-234-census.py --census` — the five families, by regular expression, over the claims, `TASKS.md`
   and the two deliverables, with file, line and the matched string.
2. `tools/T-234-census.py --check` — the gate: every occurrence classified as moved must carry a pointer to
   the claim that moved it within a stated window, exactly as `tools/T-220-census.py` gates `CH-0167`.
   Retained classification as data (`tools/T-234-classification.json`), so a reader can disagree one
   occurrence at a time.
3. The queue classification recorded in the emitter, per `T-205`'s precedent, with the open set derived.

**Cost.** Zero solves. Every number quoted here is already in a committed result file, and the two claims'
own numbers are read from `gpd/results/T-218-*.json` and `gpd/results/T-219-*.json` rather than transcribed.

**What would falsify the approach.** That **SUPERSEDED** is the wrong word for what a *geometry* correction
does. A corrected `edgeY` does not make *"is `10 × 6` a better tile"* moot — it **reprices** it, and the
answer may flip sign. If every item turns out to be repriced rather than superseded, the four-way partition
is really a two-way one and `F2` fires.
