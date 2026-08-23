# CH-0246 — **`C-0110`'s *"run the consumers even when the change is provably invisible"* is UNAVAILABLE to a corpus-subject result file: its `--ref` defaults to `HEAD`, so the run silently re-bases the measurement onto today's corpus and OVERWRITES the record rather than checking it — seven committed files, and re-running them moved every one**

| | |
|---|---|
| **Against** | [`C-0110`](../claims/C-0110-device-b-tall-gap.md) — *"a PROOF that a shared-source change is invisible is not a substitute for re-running the consumers, because the run also checks everything the proof was not about"* — as a general rule, applied to the class of result file whose **subject is the corpus** |
| **Raised by** | [`C-0196`](../claims/C-0196-a-name-cannot-govern-a-token.md) / [`T-293`](../tasks/T-293-a-name-cannot-govern-a-token.md), while doing exactly what `C-0110` prescribes |
| **Kind** | **methodological.** No number of `C-0110` is disputed and the rule is right for every result file whose subject is a **physical model**. What is disputed is its **scope**: for a file whose subject is the mutable corpus, the control run is not a control |
| **Status** | **RAISED.** The seven files were restored, unmoved, and the containment argument that replaces the run is stated in `C-0196` §8 |

---

## 1. What happened

`T-293` changed one expression in `tools/T-234-census.py`.
Sixteen tools in `tools/` import that module; six of them emit a committed result file.
`C-0110`'s rule says: run them all, even though the change is provably invisible.

Run, **all seven moved**:

| file | its own `baselineRef` | what the re-run did |
|---|---|---|
| `T-260-partial-discharge-predicate.json` | `68d9a6c` | re-based to `1c598f8`; 379 → 380 occurrences, 39 → 41 files |
| `T-262-width-restatement-predicate.json` | `68d9a6c` | re-based to `1c598f8`; same |
| `T-280-debt-line-as-a-ratio.json` | `3e71284` | re-based; the revision history table advanced by two commits |
| `T-281-name-the-discharge.json` | `9620d3e` | re-based; per-family occurrence counts moved |
| `T-282-classification-regeneration.json` | `9620d3e` | re-based; `gateBefore` 21 → **53** |
| `T-285-a-slug-is-not-a-statement.json` | `9620d3e` | re-based; `occurrencesBefore` 394 → 407 |
| `T-287-a-filename-cannot-supply-a-context.json` | `2c04380` | re-based; `removed` **10 → 0**, because the defect it measures is repaired |

**Not one of those movements is caused by the change under test**, and the last row is the sharpest:
`T-287`'s whole subject is *the ten occurrences a filename admitted*,
and re-running it today reports **zero**, because `T-287` itself removed them.
The file stops being a record of a repair and becomes a record of the repair having happened.

## 2. Why this is not `C-0110`'s case

`C-0110`'s consumers are studies of a **physical model**: given the same code and the same
committed inputs they compute the same number, so a re-run is a *check*.
A corpus-subject emitter's input is the **repository**, which every commit changes — and these
emitters take `--ref` precisely because `CLAUDE.md` already records that
*a result file whose subject is the corpus must name the corpus state it measured*.

But naming the ref is not enough, and that is the gap:

- the **default** is `HEAD`, so the obvious command re-bases;
- and even with `--ref <its own baselineRef>` the **after** side still reads the working tree, so
  the file is still not reproducible from its own code.

So for this family the run is not a weaker control than the proof — it is **destructive**, and
`C-0110`'s rule points an agent straight at it.

## 3. What replaced the run here, and why it is sound

A **containment** argument, of the shape `C-0110`'s own sibling entry uses
(*correcting one argument of a `min` moves only the rows that argument owns*):

1. the predicate change moves exactly **one** occurrence's family over **every** tracked markdown
   file, measured and emitted (`gpd/results/T-293-a-name-cannot-govern-a-token.json`);
2. that occurrence is in `gpd/challenges/CH-0229-…md`;
3. `in_scope(path)` is `path.startswith("gpd/claims/") or path in ("TASKS.md", "ANSWERS.md", "DECISIONS-FOR-NDI.md")`
   — one line, quoted whole — so a challenge file is not read by any of the seven;
4. therefore no committed consumer is stale.

That is a **proof about the seven files' inputs**, not about their outputs, which is what makes it
admissible where `C-0110` refuses a proof about outputs.

## 4. What the remedy would be

Not proposed here, and three candidates are named so the row is actionable:

- **(a)** make `--ref` mandatory for a corpus-subject emitter, so the destructive command cannot be
  typed by accident;
- **(b)** have such an emitter **refuse** to overwrite a file whose committed `baselineRef` differs
  from the resolved one unless `--rebase` is passed;
- **(c)** decide that these files are *historical* and gate them as immutable, in which case
  `gpd/README.md`'s *"reproducible from it alone"* needs an explicit exception for the class.

**(b)** is the cheapest and is the only one that keeps the file re-runnable **as a check** at its
own ref while making the re-basing an explicit act.

## 5. What is NOT claimed

- No number of `C-0110` moves, and its rule is upheld for every physical study.
- No claim is made that any of the seven files is currently wrong. They were restored unmoved and
  the census gate reads **0**.
- The count *seven* is the consumers that emit; `tools/T-234-emit-result.py` is a **eighth**
  importer that crashes at `HEAD` on an unrelated arity drift (`FAMILIES` gained a fourth element
  in `T-260`/`T-262` and it still unpacks three) — reproduced at `HEAD`'s own predicate, so it is
  pre-existing and is recorded here rather than repaired.
