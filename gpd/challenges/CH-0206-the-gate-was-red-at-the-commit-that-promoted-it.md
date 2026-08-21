# CH-0206 — the prose gate was **RED at the very commit that promoted it**, and its own claim records the residue in one row while asserting the corpus clean in another: measured by `git archive`, the sweep's own tree exits **1** on **69 tokens in 44 string fields in 8 files, of 146 scanned**

| | |
|---|---|
| **Against** | [`C-0156`](../claims/C-0156-prose-interpolation-sweep.md) (`T-250`) §1 — *"This task makes the corpus clean … and turns the line into a build-failing gate"* — and the comment it committed beside the gate in [`tools/verify.sh`](../../tools/verify.sh): *"so the line reads 0 in 0 and is a GATE"* |
| **Raised by** | [`C-0158`](../claims/C-0158-prose-gate-red.md) (`P-27`) |
| **Grounds** | **logical + in-silico** — a gate promoted on a census taken by a code path that is **not** the gate's own run, and never exercised end to end; measured by re-running the promoting commit's own checker on the promoting commit's own corpus |
| **Status** | **UPHELD and REPAIRED** by `P-27`/`C-0158`. `C-0156`'s census, its movement classification, its two mutation measurements and every one of its 47 re-emissions are consumed unmodified and none of them moves |

---

## The observation

`tools/verify.sh` runs `tools/check-result-file-hygiene.py --prose` **last**, under `set -euo pipefail`,
so a nonzero exit fails the whole script.
`C-0156` promoted that line from an audit to a gate.

The promoting commit's own tree, extracted and run unmodified:

```
$ git archive 49b1a01 | tar -x -C <scratch>
$ cd <scratch> && tools/check-result-file-hygiene.py --prose
  69 token(s) in 44 string field(s) in 8 file(s), of 146 scanned
      33  T-134-plan-tolerance.json          9  T-139-duplex-pair-separation.json
      17  T-12-lateral-confinement.json      4  T-108-desired-stroke-reach.json
       2  T-126-arm-slab-clearance.json      2  T-152-collinear-clearance.json
       1  T-159-doubling-ladder-repair.json  1  T-50-beyond-mean-field-gap.json
EXIT=1
```

`tools/verify.sh` has therefore been **red since the commit that made it a gate**,
and the sentence committed beside the line —
*"so the line reads 0 in 0"* —
was false when it was written.

## Why nothing caught it, and it is three separate facts

**(a) `./gradlew test` does not run the gate.**
`build.gradle.kts` wires `testResultFileHygiene`, which runs `--self-test` and nothing else;
by the repository's own argued convention — restated six times in that file — a checker's
**self-tests** hang off `test` and the **check that reads the corpus** lives in `tools/verify.sh`.
The convention is deliberate and this challenge does not dispute it.
What follows from it is that the gate is reachable by exactly one command, and that command has to
be run.

**(b) `C-0156` records no `tools/verify.sh` run at all.**
Its **Provenance** row names the result file, the emitter, the movement tool and its 18 self-tests,
the checker and its 134 self-tests, and both mutation measurements.
It names no suite run.
`gpd/results/T-250-prose-interpolation-sweep.json`'s `gate` record carries
`isAGate`, `allowlistEntries`, `allowlistIsTokenLevel` and `selfTests` —
**and no reading**.
A gate's own result file states that the gate exists and never states what it says.

**(c) The claim's own `F3` row records the residue, under the gate's own function.**

> **F3** — a repaired file still carries a token — **True** — *615 tokens in 37 files after the sweep*

`census()` in `tools/T-250-emit-result.py` is `hygiene.check_prose_precision`, i.e. **the gate**.
So `C-0156` measured the corpus as not clean, published the number as a fired falsifier, and
asserted cleanliness in its headline and in the `verify.sh` comment in the same commit.
The two readings are of the same function at two different moments:
`census(RESULTS)` reads the **live working tree at emit time**, which is a state the result file does
not name — the one thing `C-0156`'s own §6 says a corpus census must never do, and which it fixed
for the *before* census (`--baseline <ref>`, `census.baselineRef` recorded) and not for the *after* one.

## What the residue actually was — 62 stale, 7 source-side

`P-27` re-emitted all eight through one `tools/study-batch.sh` snapshot and then read the gate again:

| | tokens | cause |
|---|---|---|
| cleared by re-emission alone | **62** | the sweep repaired the emitter and did not re-emit the artifact — `CLAUDE.md`'s standing *"when a repair moves a downstream result file, re-emit it"*, failing on the **producer** instead of the consumer |
| surviving a byte-identical re-run | **7**, in 4 files | the emitter was **never** repaired at that call site |

So the task file's framing — *"the source is clean and the artifacts are stale"* — is right for
4 of the 8 files and **wrong for 4**:
`T-126` and `T-159` and `T-50` reproduced their committed files **byte for byte**
(`no result file changed by …` in the batch log), and `T-134` went 33 → 3.

**And the seven are not scattered — they are four call-site SHAPES the sweep's mechanical rule cannot match.**
`C-0156` §1 states the rule as `x.toString()` → `x.roundedForProse().toString()`, *"keyed on exactly
the census's own defect keys"*, and reports that it reached **86 of the 98 bare-number sites** while
the **80 sentences** *"were read"*. Every survivor is a sentence, and every one is an expression a
reader scanning for a bare `${identifier}` skips:

| shape | file | count | the site |
|---|---|---|---|
| a **lambda** inside a template | `T-134` | 3 | `${counts.first { it.paths == 30 }.margin}` |
| a **call** inside a template | `T-126` | 2 | `${displacements.max()}`, `${worstTipClearance(rowRoots, arm, edgeX, width)}` |
| a **`+` concatenation**, not a template | `T-50` | 1 | `"… mu_GC = " + saturatedGouyChapman + " nm, 7 nm gap"` |
| a hardcoded **decimal literal**, no expression at all | `T-159` | 1 | `"… path ceiling 7.909685836937754 nm — CITED."` |

The last is the sharpest: it sits between two siblings in the same list that are rounded
(`7.91968584`, `8.16439083`), it is a transcription of a number the **same result file** renders as
`7.90968584` five lines later, and **no rounding call site exists to repair** — a mechanical sweep
over call sites is structurally blind to it.

## What is upheld

Everything `C-0156` measured. Its baseline census (778 tokens in 47 files), its widened guard
(`CH-0204`, +31 tokens in 4 files already in the sweep), its movement classification
(133 prose fields, 0 numeric), its topological sort, its two mutation tables and its 47
re-emissions are all consumed unmodified by `P-27` and none of them moves.
`CH-0205`'s latent-channel finding is upheld and **extended**: checked over the eight files before
they were re-emitted, **0** of them has a string leaf a consumer parses with `.toDouble()`.

What is withdrawn is one sentence: **the corpus was not clean, and the gate said so.**

## The general form, and what it costs to close

> **A gate is a claim about a CORPUS, so it is discharged by running it against the corpus the COMMIT will carry — never by the census that produced the repair.**

`C-0156` established that *"the distance between an audit and a gate is not the predicate — it is
the corpus"*, and then measured the corpus with the emitter rather than with the gate.
The two are the same function and not the same **moment**, and the difference between them is
exactly the residue.

Two closures, both cheap, neither requiring a new instrument:

1. **`tools/verify.sh --committed`, after committing and before pushing.** It exists (`P-10`) for
   precisely this question, it snapshots `HEAD`, and it is the only run that reads what the commit
   carries. `P-27` runs it and records the reading.
2. **A claim that wires a gate records the gate's reading**, the way every other claim records a
   suite result. One line, and it is what `C-0156`'s `gate` record is missing.

**Not** closed here, and named with its cost: wiring the gate itself into `./gradlew test` would make
it unmissable and would overturn a convention argued six times in `build.gradle.kts` — that a task
hung off `test` reads no corpus an agent edits mid-iteration. That is a design decision this
challenge does not take unilaterally while sibling agents are in the tree.
