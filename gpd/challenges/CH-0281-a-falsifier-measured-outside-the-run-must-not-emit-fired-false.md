# CH-0281 — A FALSIFIER WHOSE VERDICT IS MEASURED **OUTSIDE** THE RUN EMITS `"fired": false` UNCONDITIONALLY, WHICH IS A NEGATIVE THE RUN CANNOT KNOW — AND THE CORPUS ALREADY CARRIES ONE THAT SAYS `false` WHERE ITS OWN CLAIM SAYS **FIRED**

| | |
|---|---|
| **Against** | the `falsifiers[*].fired` field of the three result files carrying *"two independent runs of the study do not produce a byte-identical result file"* — [`T-316`](../results/T-316-a-searched-distribution-at-the-resolved-link.json) `F12`, [`T-322`](../results/T-322-route-b-coupled-on-its-own-stations.json) `F4`, [`T-323`](../results/T-323-the-placement-and-the-distribution-together.json) `F23` |
| **Not against** | any of their physics, and not against the falsifier itself, which is the right thing to declare — `C-0216` §14 states the problem correctly in prose and then emits the field anyway |
| **Raised by** | [`C-0217`](../claims/C-0217-the-cure-at-every-call-site.md) / [`T-328`](../tasks/T-328-the-cure-at-every-call-site.md) |
| **Grounds** | methodological — `C-0177`'s *a gate can be wired and still be unable to fail*, met on an **emitted verdict** rather than on an exit code; and `C-0182`'s *an absence read as an answer* |
| **Status** | **RAISED.** Latent rather than live: no tool in this repository reads `falsifiers[*].fired` across files today. A **reader** does |

---

## The census, and it is three files

Every result file in `gpd/results/` whose falsifier statement is about run-to-run identity:

| file | id | declared | `"fired"` | what the claim says |
|---|---|---|---|---|
| `T-316-…json` | `F12` | closed | `false` | `C-0212`: discharged by a second emission, **did not fire** |
| `T-322-…json` | `F4` | closed | `false` | discharged the same way, **did not fire** |
| `T-323-…json` | `F23` | closed | `false` | `C-0216` §14: **FIRED** — 26 of 1 252 leaves moved |

**Two of the three flags are true by accident and the third is false.** They are all produced the
same way — a literal `false` in the emitter — because a run genuinely cannot assert byte-identity
about itself. `C-0216` §14 says so in as many words: *"The study's own `falsifiers[22].fired` reads
`false`, and it has to."*

It **has to** not be `true`. It does not have to be `false`.

## Why this is the class `C-0177` named

`C-0177` found `tools/trace-answers.py`'s `main` accumulating a defect count into a dead local and
returning `0` — *a gate that cannot fail*. Here the shape is one level in: the field is not a gate,
it is a **record of a verdict**, and it records the same verdict at every possible state of the
world. A field that cannot take two values carries no information, and this one carries **negative**
information, because its name promises that it can.

The failure direction is the flattering one. A reader — human or a census nobody has written yet —
scanning `falsifiers[*].fired` over the corpus reads `T-323`'s `F23` as *did not fire*, which is
the opposite of the most load-bearing process result the study produced.

## What the challenge asks

1. That an **externally measured** falsifier emit `fired: null` — the type already admits it
   elsewhere in this corpus (`jointWinnerRankInThisScreen` is a nullable) — or a third state, and
   that its note say where the measurement lives.
2. That the note carry the **artifact** the measurement was taken on, so the reading is recoverable:
   here, `gpd/data/T-323-reproducibility/`.
3. That the general rule be stated: **a verdict a run cannot take must not be emitted as a verdict
   the run took.** `C-0182`'s registry answer applies verbatim — the getter refuses, the report has
   a third state, and `None` is an explicit declaration rather than the absence of one.

## What does NOT move

No number of `C-0212`, `C-0216` or the `T-322` claim. The two `false`s that happen to be right stay
right; the one that is wrong is wrong in a field no claim quotes, which is exactly why it survived.
This challenge is filed rather than repaired here because the repair touches three studies' emitters
and one of them is `SearchedDistributionStudy.kt`, which `T-328` is forbidden to edit.
