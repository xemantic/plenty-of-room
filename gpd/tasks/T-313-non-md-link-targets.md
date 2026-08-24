# T-313 — a relative link to anything that is not a `.md` file is checked by nothing

| | |
|---|---|
| **Raised by** | [`C-0207`](../claims/C-0207-the-uniform-raster-is-flat-with-its-tethers.md) (`T-307`/`T-308`), on **two live defects created in one iteration by two agents making the same kind of move** — a mutation harness relocated out from under a Markdown link, in `TASKS.md` and in a claim, neither noticed by its author |
| **Leaf** | — (process) |
| **Verification type** | **logical**, as named self-tests in the checker, a mutation table over every predicate changed in **both** directions, and a **paired sweep of the shipped predicate over the whole committed history** |
| **Units** | none; every value below is an integer count, a file path or a verdict |

## Formulate

[`tools/check-corpus-links.py`](../../tools/check-corpus-links.py) resolves relative Markdown links.
Its pattern is

```
_LINK = re.compile(r"\]\(([^)\s]+?\.md)(?:#[^)]*)?\)")
```

so a `.py`, `.kt`, `.json`, `.sh` or directory target is **invisible to it**,
and therefore invisible to every gate in this tree —
`tools/check-result-path-references.py` gates a claim's `Provenance` row, which is a **bare path** rather than a link,
and nothing else reads a link target at all.

That is `C-0083`'s own finding — *a cross-reference is a filename, and a filename is a number like any other* — read **one file type across**.

The queue offers two deliverable shapes and asks for the false-positive rate to be **measured** rather than asserted:

1. the checker **widened** to resolve every relative link target, with the corpus's own false-positive classes handled; or
2. a stated **scope line** saying which link kinds the checker does not reach, so a reader knows a clean run is clean about `.md` alone.

### The cheap bound, run first, and it moves the answer

`C-0207` censused **7 359** relative links corpus-wide, **600** non-`.md`, **14** broken — 6 illustrative placeholders, 6 in `tools/C-0156-claim-template.md`, 2 live defects — i.e. an **86 % false-positive rate**, which `CLAUDE.md` records as the rate at which a build-failing gate gets switched off.

Re-derived here against **the checker's own file set and the checker's own code-blanking**, which is the only scope a shipped predicate has:

| | |
|---|---|
| Markdown files the checker scans (`gpd/**.md` + root `*.md`) | **671** |
| relative link targets of **any** kind in them | **7 380** |
| of which **non-`.md`** | **594** — `.py` 279, `.json` 123, `.kt` 121, `.sh` 36, directory 17, `.txt` 7, `.sc` 5, `.kts` 4, `.toml` 2 |
| anchor-only (`](#…)`) / absolute / external | 3 / 0 / 25 |
| **broken under the widened predicate, at `HEAD`** | **0** |

**Both of `C-0207`'s false-positive classes are outside a shipped predicate's scope by construction, not by exemption:**

- the **placeholder** class (`[label](target)`) is **5 raw occurrences and 0 surviving `_without_code`** — every one of them is inside an inline code span, which the checker has blanked since it was written. `C-0207`'s census did not blank code; the checker does.
- the **template** class is `tools/C-0156-claim-template.md`, which the checker **does not scan**: `tracked_markdown()` is `gpd/**.md` plus the root documents, and `tools/`, `third-party/` are not in it.

So the 86 % is a property of `C-0207`'s census and not of the checker,
and the two branches the queue offers are **not exclusive**: the widening is free, and the scope line is still owed for the file set.

## Plan

**Ship both branches.** Neither is expensive and the second is what makes the first honest.

1. **Widen `_LINK`** to `\]\(([^)\s]+?)(?:#[^)]*)?\)` and add the guard the widening needs: an **anchor-only** link (`](#section)`, 3 in the corpus) is not a path. The existing `://`, leading-`/` and code-blanking guards are unchanged and gain a named test each where they lack one.
2. **State the scope**, measured, in the checker and on its own summary line — what file set it scans, and which link **shapes** the regular expression cannot see. Measured today: titled `](path "title")` **0**, angle-bracket `](<path>)` **0**, reference-style `[x][y]` **0**. The residue is the **file set**, and it is quantified.
3. **Measure the false-positive rate over this repository's own history**, not over `HEAD`: replay the shipped predicate over every commit, reading each commit's own tree for existence, and read every distinct `(file, link)` pair it reports.
4. **Mutation-test every predicate changed, in both directions**, with a subtracted green baseline (`CH-0237`), in `tools/T-313-mutation-test.py`, declared in `tools/P-31-harness-census.py`'s `HARNESSES` table in the same commit and wired.

**Method against cost.** The history sweep is the expensive half at ~1 minute for 256 commits, and it is the only thing that can answer the question the queue asks — a rate measured at `HEAD` on a corpus somebody has just repaired measures nothing. It is done by reading blobs out of `git` with a SHA cache rather than by checking out 256 trees.

**What would falsify the approach.** If the widened predicate needs a **hand exemption list** to come clean, the widening is refused and branch 2 alone is shipped: `CLAUDE.md` is explicit that an honest scope line beats a gate with an unmeasured exemption list, and that a hand override keyed on anything dated is itself a dated object.

## Falsifiers, declared before the run

- **`F1` — clean at `HEAD`.** The widened predicate reports **0** broken links over the checker's own file set at the commit the claim is filed at. Any report that is not a genuine dangling reference refuses the widening.
- **`F2` — 0 false positives over the history.** Replayed over every commit reachable from `HEAD`, the class the widening **adds** (non-`.md` relative targets) fires on no `(file, link)` pair that is not a genuine dangling reference. **Predicted: 0 false positives.** One false positive refuses the widening and the deliverable falls back to branch 2.
- **`F3` — it reaches the class it was written for.** The sweep must find, dangling at the commits where they stood, both defects `C-0207` reports: `gpd/data/T-299-mutation/mutate.py` and `gpd/data/T-304-mutation/mutate.py` named from `TASKS.md`. If it does not, the predicate does not reach the class.
- **`F4` — every changed rule fails a named test in both directions**, over a measured and subtracted green baseline. A mutation that fails nothing is the finding, not a gap in the test list, and the state gets constructed.
- **`F5` — the two false-positive classes are out of scope by CONSTRUCTION.** Asserted, not assumed: every placeholder occurrence is blanked by `_without_code` (predicted 5 raw, 0 surviving), and `tools/C-0156-claim-template.md` is not in `tracked_markdown()`. If either needs suppressing by hand, branch 1 is refused.
- **`F6` — the scope line names every residue, measured.** The shapes the regular expression cannot see must be **0** in the corpus today; a non-zero count means the scope line understates the gap and must carry the number instead.
- **`F7` — the self-test's own fixture layout is declared.** `tools/check-corpus-links.py --selftest` reads the live corpus at seven assertions; the harness's copied tree must therefore contain them, and the copied set is declared in the harness rather than inferred (`CLAUDE.md`: *a fixture layout is a dependency declaration*). If the baseline is not green in that tree, nothing below it is a measurement.

## Out of scope, stated

The **file set** is a second axis and this task does not move it.
Bringing `tools/**.md` into the scan costs exactly `tools/C-0156-claim-template.md`'s **23** relative links,
every one of which resolves **against `gpd/claims`** — the directory the template is copied **to** — and none against `tools/`, where it sits.
That is the interesting half of the queue's row and it is a **declaration** problem rather than a predicate problem:
a relative link's correctness is a property of the file the text will end up in.
It is measured here and filed as its own row rather than guessed at.
