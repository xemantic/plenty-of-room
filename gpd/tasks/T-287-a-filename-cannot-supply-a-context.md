# T-287 — a filename can still supply a premise family's line CONTEXT

| | |
|---|---|
| **Raised by** | [`C-0184`](../claims/C-0184-a-slug-is-not-a-statement.md) (`T-285`), while bounding its own scope |
| **Leaf** | — (process) |
| **Verification type** | logical, as executable self-tests, a mutation row on the existing harness, and a before-and-after census reading |
| **Units** | none; this is a document-integrity task |

## Formulate

`T-285` ([`C-0184`](../claims/C-0184-a-slug-is-not-a-statement.md)) stopped a premise-family token **matching** inside a link target:
`tools/T-234-census.py` blanks `<ID>-<slug>.<ext>` before it looks for its families,
on the ground that *a slug is a name and not a statement*.

The **line context** test was left alone, deliberately.
`occurrences()` matches against the blanked text and then asks whether the family's context word appears on the **original** line —
so a line whose own words say nothing about the honeycomb can still be admitted by a `honeycomb` sitting inside a neighbouring **filename**,
which is a name and asserts nothing about a row length or a tile width.
`context_distance()`, the `REMOTE-CONTEXT` diagnostic's own measurement, reads the original text for the same reason and has the same defect.

`C-0184` kept the two apart because they run in **opposite directions**:
`T-285` **removes** matches and this **admits** them, so repairing both in one pass would put two deltas of opposite sign into one before-and-after list and neither could be audited against the other.

**Numeric target.**
Either the line context blanked the same way the match is, with **every** reclassification it causes read one at a time,
or a stated decision that a link target is allowed to supply context and why.
Either way the classification is regenerated, because a family change moves a class and the table is keyed on the index.

**Acceptance predicates, falsifiable.**

- **F1 — one rule, one text.** The line context and the context distance are read from the **same** blanked text the match is read from. Blanking is length-preserving (`T-285`), so every reported offset and line number is unchanged and the change is confined to which lines carry a context word.
- **F2 — every reclassification is read one at a time.** Each occurrence the change removes is inspected individually and its class before the change recorded. The change is admissible only if it removes **no** occurrence classified `MOVED` or `DISCHARGED` — those are the debt, and a repair that hides debt is the failure direction this census exists to avoid.
- **F3 — the gate does not move.** `tools/T-234-census.py --check` reads **0** defects before and after. The `T-233` debt line is reported before and after, itemised, and any movement is attributed.
- **F4 — the two deltas stay separable.** The refinement window (`REFINE_WINDOW`, `STRUCTURAL_WINDOW`) reads the original text too, and its delta is measured **separately** and reported, whether or not it is taken in this task. `C-0184`'s reason for splitting `T-285` from `T-287` applies again one level down.
- **F5 — the classification is regenerated, last.** `tools/T-234-emit-classification.py` is run **after** the predicate work is settled, with `git status` checked first for a foreign in-flight file (`C-0176` §1b), and every hand override it **drops** is named and justified.
- **F6 — the mutation test.** Restoring the original-line reading fails at least one **named** test of `tools/T-234-census.py --self-test`, as a row of the existing `tools/T-234-mutation-test.py` that replaces the rule wholesale.

**What would falsify the approach.**
An occurrence classified `MOVED` or `DISCHARGED` that the blanking removes:
that would be a live debt statement admitted only by a filename, which is a statement the census must keep seeing.
Equally falsifying is a reclassification the reading cannot justify one at a time —
the point of the split from `T-285` is that each delta is auditable, and a delta nobody can read is not.

## Plan

**Cheap bound first, and it decides the whole task.**
The change is one expression; the measurement is a diff of two runs of `occurrences()` over the corpus.
Run it **before** writing anything: list every occurrence that appears or disappears, with its current class out of `tools/T-234-classification.json`.
If any is `MOVED` or `DISCHARGED`, the answer is *state the decision and keep the original line* — F2 has fired and the expensive half is not worth buying.
If none is, the repair is one line and the audit is a handful of readings.

**Why blanking rather than a rule about links.**
A rule that recognised a Markdown link target specifically would miss the same slug written bare —
which this corpus does, in `Provenance` and `Conditions` rows, and in prose naming a result file.
`blank_identifiers` already answers *what is a name here* and is already tested in both directions by `T-285`'s fifteen named tests;
reusing it is one call and adds no new judgement.

**Why the distance travels with the context test.**
`context_distance` exists to say *the line context said nothing about this token*.
Measured on the original text it can only under-report, because a filename is a context word it counts and the admission rule does not.
Both readings must come from one text or the diagnostic contradicts the rule it diagnoses.

**TDD.** The named tests go into `tools/T-234-census.py --self-test` first and are watched to fail;
the mutation row is written against them and run through `tools/T-234-mutation-test.py`.
Nothing new is wired: `tools/T-234-census.py --self-test`, `--check` and `tools/T-234-mutation-test.py` are already in `build.gradle.kts`.
