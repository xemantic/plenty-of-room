# T-317 — a relative link's correctness is a property of the file the text will END UP IN, and the corpus has one file where that differs from where it sits

| | |
|---|---|
| **Raised by** | [`C-0209`](../claims/C-0209-a-link-target-is-a-filename-whatever-it-names.md) (`T-313`), which closed the **link-kind** axis of the same gate and left the **file-set** axis open and measured |
| **Leaf** | — (process) |
| **Verification type** | **logical**, as named self-tests in the checker, mutation rows over every predicate changed in **both** directions, and a **replay of each candidate predicate over the whole committed history** |
| **Units** | none; every value below is an integer count, a file path or a verdict |

## Formulate

[`tools/check-corpus-links.py`](../../tools/check-corpus-links.py) resolves every relative link of
**any** target kind — `T-313` closed that axis — but only inside `tracked_markdown()`,
which is `gpd/**/*.md` plus the root documents.
Markdown anywhere else in the tree is checked by nothing.

The queue offers two deliverable shapes:

1. the **file set widened** to the Markdown the checker does not scan,
   with a way for a **template** to declare the directory it is copied to,
   and `third-party/` exempted the way [`tools/check-markdown-tables.py`](../../tools/check-markdown-tables.py) already exempts it; or
2. a **recorded decision** that the class stays out of scope, and why.

It also states the reason the choice is not obvious:
`tools/C-0156-claim-template.md` is a claim **body** that is rendered into `gpd/claims/`,
so its links are correct at a directory it does not sit in —
and a checker that resolves a link against the file's own directory is asking the wrong question about it.

### The cheap bound, run first, and it decides the shape

Re-derived here at `HEAD`, against the checker's own `tracked_markdown()` and its own code-blanking:

| | |
|---|---|
| Markdown files the checker scans | **686** |
| Markdown files it does **not** scan | **4** — `third-party/2026-08-ndi-gen1-problem-definition.md`, `tools/C-0156-claim-template.md`, `tools/oxdna/README.md`, `tools/oxdna/RESULTS.md` |
| relative links in the four | **24** — 0, 23, 0, 1 |
| broken resolved against the file's **own** directory | **23**, all of them the template's |
| broken resolved against `gpd/claims`, the directory the template is copied to | **0** |

So the whole of the file-set residue is **one file**, and the question is entirely about **where its links are read from**.

Two further facts, measured before any code is written, and each of them moves the answer:

- **The template's destination need not be declared, because it is DERIVABLE and it is UNIQUE.**
  Of the **70** directories of this tree, **exactly one** — `gpd/claims` — resolves all 23 of its links.
  A declaration is a dated object (`C-0176`); a derivation is not.
- **`third-party/` needs no repair in order to be exempt.** The problem definition as received carries **0** relative links,
  so exempting it is a scope statement rather than a suppression, and it mirrors `tools/check-markdown-tables.py`'s `_EXCLUDED_ROOTS`.

### And the false-positive rate is what refuses the naive widening

`CLAUDE.md`: *a drift checker's false positives cost more than its true ones*,
and *a rate measured at `HEAD` measures nothing*.
Replayed over every commit reachable from `HEAD` (**273**), over the Markdown outside the corpus set and outside `third-party/`:

| reading | distinct `(file, link)` pairs | commits with a hit |
|---|---|---|
| resolved **in place** (the naive widening) | **20**, every one of them the template's and every one a false positive | **130** |
| resolved against a **declared** destination | **0** | **0** |
| resolved against a **derived** destination | **0** | **0** |

A 100 % false-positive rate over 130 commits is exactly the rate at which a build-failing gate gets switched off.
The naive widening is therefore refused **on a measurement**, and the two disciplined readings are indistinguishable in what they catch — so the tie is broken on **cost**, and a derivation costs no dated object.

## Plan

**Ship branch 1, with the declaration replaced by a derivation, and record the refusal of the declaration as the measured half.**

The corpus's Markdown splits into three sets, and the checker is to name all three on every run:

1. the **corpus set** — `gpd/**/*.md` plus the root documents. Links resolved **in place**. Gated. **Unchanged**, bit for bit.
2. the **relocatable set** — every other Markdown outside the excluded roots. A file here is not necessarily read where it sits,
   so its links are judged against the **directory the whole file resolves from**: its own directory where that works, otherwise a derived one.
   Gated on the one unambiguous failure — *no directory of the repository resolves all of this file's links*.
3. the **excluded roots** — `third-party/`, the problem definition as received, which must not be edited at all.
   Counted and named, never scanned.

Then:

- `relocatable_census(root)` reports, per file, its link count and where it resolves from — `in place`, a derived directory, or **nowhere**, which is the defect.
- the summary line carries the residue in **links** as well as in files, so a reader of a clean run knows whether the blind spot is 24 links or 24 000.
- `--history --relocatable` retains the measurement above as a **mode of the checker**, which is `CH-0266`'s own remedy: the next agent to touch the file set re-derives the rate in one command instead of writing a scanner.
- mutation rows go into the existing [`tools/T-313-mutation-test.py`](../../tools/T-313-mutation-test.py), whose subject is this same file and whose `P-31` row declares no count.

**Method against cost.** The derivation is `70 directories × 24 links` of `os.path.exists`, measured at **1 ms**; the history replay is 273 commits at **~4 s** with blobs cached by SHA. Neither is a reason to prefer the cheaper wrong answer. No result file is emitted: this is a corpus convention and a gate, and `CLAUDE.md` records that a result file whose subject is the corpus is destructive to re-run.

**What would falsify the approach.** If the derived reading needs a hand exemption to come clean, or fires once over the history on anything that is not a genuine dangling reference, branch 1 is refused and the deliverable falls back to the recorded decision — with the same numbers, which are the point either way.

## Falsifiers, declared before the run

- **`F1` — clean at `HEAD`.** The widened checker reports **0** defects over all three sets. Any report that is not a genuine dangling reference refuses the widening.
- **`F2` — 0 false positives over the history.** Replayed over every commit reachable from `HEAD`, the relocatable predicate fires on **no** `(file, link)` pair that is not a genuine dangling reference. **Predicted: 0 pairs over 273 commits.** One false positive refuses branch 1.
- **`F3` — the naive reading is refused by measurement, not by taste.** The same replay with links resolved **in place** must fire, and every firing must be a false positive. **Predicted: 20 distinct pairs over 130 commits, all 20 false.** If it fires on nothing, the destination rule is unnecessary and the simpler widening ships instead.
- **`F4` — the destination is derived and UNIQUE.** Exactly **one** directory of the tree resolves all 23 of the template's links, and it is `gpd/claims`. If more than one does, the derivation is ambiguous and the declaration the row asks for is owed after all.
- **`F5` — the corpus-set gate does not move.** The defect count and the file count over `tracked_markdown()` are **0 in 686**, unchanged, and every one of `T-313`'s 22 mutation rows still fails a named test.
- **`F6` — `third-party/` is exempted without being repaired.** It carries **0** relative links, so the exemption suppresses nothing. A non-zero count means the exemption is hiding a defect and must be reported instead.
- **`F7` — the residue is quantified in links and derived on every run.** The summary line states how many relocatable files there are, how many links they carry, and how many Markdown files are excluded — all three computed, none declared.
- **`F8` — every changed rule fails a named test in BOTH directions**, over a measured and subtracted green baseline, and every new self-test is **hermetic**: `tools/T-295-mutation-input-census.py` must read the added rows as `FIXTURE`, not corpus-dependent.

## Added during execution, and it was not among the falsifiers above

Widening a tool's flag set is a `CH-0268` §4b obligation:
*refuse an unrecognised flag **even where the tool writes nothing***,
because a wired call that passes a flag the tool does not recognise is green and inert.
This checker is wired **twice** — [`build.gradle.kts`](../../build.gradle.kts) passes `--selftest` and [`tools/verify.sh`](../../tools/verify.sh) passes `--selftest` and then nothing —
and this task takes its flag count from one to five,
so a mistyped `--self-test` would have run the corpus check and exited **0**.
That is the recorded instance of the trap, in this repository, on a wired task.
The guard is added with a named test in each direction and two mutation rows;
it is declared here rather than in the falsifier list because it was not foreseen when the list was written.

## Out of scope, stated

- **The three link SHAPES the pattern cannot see** — titled, angle-bracket, reference-style — are `T-313`'s residue and are unchanged here. All three are still 0 in the corpus and still counted on every run.
- **A bare path in prose is not a link.** [`tools/check-result-path-references.py`](../../tools/check-result-path-references.py) gates the one place the corpus writes one systematically.
- **Whether a template's render is up to date with the template** is a different gate about a different defect. Measured here only as evidence, by the retained [`tools/T-317-template-render-census.py`](../../tools/T-317-template-render-census.py): at **130 of 130** commits where both exist, the template's link multiset equals its render's, and there is **no** commit where the template exists and the render does not.
