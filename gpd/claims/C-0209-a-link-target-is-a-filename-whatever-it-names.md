# C-0209 — the link gate resolved **`.md` targets only**, so a relative link to a `.py`, `.kt`, `.json`, `.sh` or directory was invisible to every gate in the tree; widened it reports **0 at `HEAD` and 0 false positives over 259 commits**, and the 86 % false-positive rate that framed the question turns out to belong to the **census that measured it** — the placeholders were 6 raw and **0** surviving the checker's own code-blanking, and the template is **not scanned**

| | |
|---|---|
| **Task** | [`T-313`](../tasks/T-313-non-md-link-targets.md), raised by [`C-0207`](../claims/C-0207-the-uniform-raster-is-flat-with-its-tethers.md) (`T-307`/`T-308`) |
| **Leaf** | — (process) |
| **Verification type** | **logical** — 50 named self-tests in the checker, a 22-row mutation table at 0 survivors over a measured and subtracted green baseline, and a replay of the shipped predicate over **every commit reachable from `HEAD`** |
| **Verdict** | **PASS on `F1`–`F7`.** `tools/check-corpus-links.py` reads `0 broken link(s)`; `tools/T-313-mutation-test.py` reads `22 mutation(s), 0 survivor(s)`; `tools/check-corpus-links.py --history` reads `259 commit(s) swept, 19 with at least one hit, 6 distinct (file, link) pair(s)`, **all six genuine and none a false positive**; `tools/T-295-mutation-input-census.py --check` reads **352 mutations over 16 harnesses, 352 fixture-backed, 0 corpus-dependent, 0 survivors, 0 defects**, the new harness's 22 among them |
| **Maturity** | TRL 1–3 process artifact. **No physics changed**: no Kotlin source is touched, no result file is emitted or re-emitted, and no number in the corpus moves |
| **Provenance** | [`tools/check-corpus-links.py`](../../tools/check-corpus-links.py) (the widened predicate, the derived scope line, the `--history` measurement, **50** named self-tests, up from 23), [`tools/T-313-mutation-test.py`](../../tools/T-313-mutation-test.py) (**22** mutations, 0 survivors), one row in [`tools/P-31-harness-census.py`](../../tools/P-31-harness-census.py)'s `HARNESSES` table and one Gradle task in [`build.gradle.kts`](../../build.gradle.kts) |
| **Conditions** | The corpus at `9b6588d` and the commits reachable from it. `tools/` and `build.gradle.kts` only |
| **Consumes** | [`C-0083`](C-0083-markdown-tables-that-do-not-render.md) (*a cross-reference is a filename, and a filename is a number like any other*; *a gate that cannot come clean is not a gate*), [`C-0122`](C-0122-honeycomb-station-lattice.md) (which introduced this checker as a side deliverable, with 11 self-tests), [`C-0198`](C-0198-a-quoted-number-has-no-link-back.md) (*a gate on a named artifact is a pre-push gate*), [`C-0176`](C-0176-partial-discharge-and-restatement-predicates.md) (*a declared list is a dated object*; *a mutation must replace a rule wholesale*), [`C-0161`](C-0161-mechanics-on-an-imported-design.md) (*a mutation that fails nothing is the finding — construct the state*), [`C-0185`](C-0185-orphaned-mutation-anchors.md) (`P-31`; the subtracted baseline), [`C-0206`](C-0206-a-harness-output-format-is-an-interface.md) (the declared row shape), [`C-0195`](C-0195-the-discriminating-input.md) (*a fixture layout is a dependency declaration*) |
| **Constrains** | every relative link in `gpd/**/*.md` and the root documents, of any target kind |
| **Raises** | [`CH-0266`](../challenges/CH-0266-the-rate-belongs-to-the-census-not-the-predicate.md), against the census `CLAUDE.md`, two `TASKS.md` rows and `JOURNAL.md` attribute to `C-0207` — the rate is a property of that census and the claim it is credited to contains none of it. `CH-0267` is reserved and **RELEASED UNUSED** |

---

## The claim, in three lines

A cross-reference is a filename **whatever it names**, and this gate read only the ones ending `.md` —
which is why two agents relocated a mutation harness out from under a Markdown link in one iteration and neither move was caught by anything.
Widened, it costs **nothing**: 595 further targets checked, **0** reported at `HEAD`, and **0 false positives** over the repository's whole history.

## 1. The cheap bound ran first and it moved the answer

The queue offered two branches — widen the predicate, or state a scope line — and asked for the false-positive rate to be **measured**, because 12 of the 14 hits the raising census reported are not defects and `CLAUDE.md` records 86 % as the rate at which a build-failing gate gets switched off.

Re-derived against **the checker's own file set and the checker's own code-blanking**, which is the only scope a shipped predicate has, at `9b6588d` — the implementation commit, before this claim and its challenge entered the corpus they measure:

| | |
|---|---|
| Markdown files the checker scans (`gpd/**/*.md` + root `*.md`) | **672** |
| relative link targets of any kind in them | **7 382** |
| of which **non-`.md`** — the class the widening adds | **595**: `.py` 280, `.json` 123, `.kt` 121, `.sh` 36, directory 17, `.txt` 7, `.sc` 5, `.kts` 4, `.toml` 2 |
| anchor-only / absolute / external | 3 / 0 / 25 |
| **broken under the widened predicate at `HEAD`** | **0** |

**Both alleged false-positive classes are out of scope by CONSTRUCTION rather than by exemption**, which is `F5` and it is asserted as two named tests rather than assumed:

- the **placeholder** class, `[label](target)`, is **6 raw occurrences and 0 surviving `_without_code`** — every one is inside an inline code span, which this checker has blanked since it was written, for exactly this reason. The raising census did not blank code. (One of the six is `T-313`'s **own task file**, quoting the token in order to state the count: `CH-0182`'s *a claim about a census enters that census*, met inside the falsifier that predicted 5.)
- the **template** class is `tools/C-0156-claim-template.md`, and `tracked_markdown()` is `gpd/**/*.md` plus the root documents. `tools/` is not in it.

So the 86 % is not a rate the widening would ever have had, the two branches are **not exclusive**, and both ship. That is `CH-0266`.

## 2. Branch 1 — the widening, and the one guard it owes

`_LINK` goes from `\]\(([^)\s]+?\.md)(?:#[^)]*)?\)` to `\]\(([^)\s]+?)(?:#[^)]*)?\)`.

The widening owes exactly one new guard: an **anchor-only** link, `](#a-heading)`, points inside the file it is written in and is not a path at all.
The old pattern excluded it only as a **side effect** of ending in `.md`; three of them are in the corpus.
The `://` and leading-`/` guards are unchanged and each gained the named test it lacked.

`relative_links_in` is factored out so that the resolver and the scope line cannot disagree about what a relative link is:
the count the summary reports and the set the gate checks are the same set.

## 3. Branch 2 — the scope line, derived on every run

`C-0083`'s standard is that a gate that cannot come clean is not a gate.
Its converse is owed too: **a gate that CAN come clean must say what it does not reach**, or a clean run is read as a statement about the whole corpus.

Two residues, both **measured on every run** rather than declared once, because a declared list is a dated object and this one would be dated by the first titled link anybody writes:

```
# 0 broken link(s) in 672 file(s)
# scope: ANY relative target kind (595 non-.md today); NOT scanned: 4 .md outside gpd/ and the root (third-party, tools); NOT matched: 0 titled, 0 angle-bracket, 0 reference-style link(s)
```

`F6` holds: all three shapes the pattern cannot see are **0** in the corpus today, so the residue is entirely the **file set**, and that is quantified rather than described.

## 4. The false-positive rate, measured over the corpus's own history

A rate measured at `HEAD` measures nothing — `HEAD` is a corpus somebody has just repaired.
`tools/check-corpus-links.py --history` replays the shipped predicate over every commit reachable from a revision, each commit's own tree supplying the existence set, blobs cached by SHA.
At `9b6588d`: **259 commits swept, 19 with at least one hit, 6 distinct `(file, link)` pairs, 26 firings.**

| commits | file | link | cause |
|---|---|---|---|
| 11 | `TASKS.md` | `tools/T-297-mutation-test.py` | named at position 205, added at 216 — an artifact named and never added |
| 6 | `TASKS.md` | `gpd/data/T-299-mutation/mutate.py` | the harness moved into `tools/` at 246 (`T-305`); the link did not |
| 3 | `TASKS.md` | `gpd/data/T-304-mutation/mutate.py` | named at 247, added at 248, moved away at 250 (`T-308`) — both causes on one path |
| 2 | `C-0203` | `../../tools/T-298-emit-result.py` | named at 235, added at 237 (*"the emitter, missed in the previous commit"*) |
| 2 | `C-0201` | `../data/T-299-mutation/mutate.py` | the same `T-305` move, from the claim |
| 2 | `C-0204` | `../data/T-304-mutation/mutate.py` | the same `T-308` move, from the claim |

**All six are genuine dangling references and none is a false positive** — `F2`.
Two of them are the live defects the raising claim reports, found at exactly the commits it says they stood at — `F3`.
**Four are new**, and nothing in this tree had ever seen them.

The two *named-before-added* rows are not an argument against the gate: `C-0198` settled that class in as many words — *"an artifact named and never added is exactly the defect, and the only thing distinguishing it from an in-flight one is time"* — and it is why this gate lives in `tools/verify.sh`, which is a **pre-push** gate, rather than in `build.gradle.kts`.

## 5. The mutation table

`tools/T-313-mutation-test.py`: **22 mutations, 0 survivors**, over a baseline run first and refused on if red.
Ten rows revert a rule and twelve over-widen it — a table that only ever narrows becomes a pattern.
Every row fails at least one **named** test; the anchor count is asserted at 1 per row.

| what is mutated | narrowed | widened |
|---|---|---|
| `_LINK` | back to `.md` only (9 tests), anchor fragment no longer stripped (1) | target may contain whitespace (1) |
| the three guards | anchor-only (1), `://` (1), absolute (1) | every relative link skipped (15) |
| code blanking | inline spans (2), fences (1) | — |
| the three shape patterns | titled (1), angle-bracket (1), reference-style (1) | titled matches every inline link (1); the census reads raw text (1) |
| the unscanned residue | narrowed to nothing (2); the count dropped from the note (1) | stops subtracting what IS scanned (3) |
| the history sweep | scope drops the root documents (1) | scope takes every `.md` (1); the added class can no longer be isolated (1) |
| the output | the scope line is not printed (1); a broken link no longer fails the run (1) | — |

**The fixture layout is a dependency declaration** (`F7`), and `tools/T-295-mutation-input-census.py` confirms it worked:
all **22** rows are `FIXTURE` in both arms, **0 corpus-dependent**, in a census reading 352 mutations over 16 harnesses with 0 defects.
`--selftest` reads the live tree at eleven assertions, so the work tree carries `tools/` wholesale, every `.md` under `gpd/`, and every root `.md` — and nothing else.
Those eleven survive `tools/T-295-mutation-input-census.py`'s treatment arm, which **empties** a corpus file rather than removing it, so an existence check still holds there.

The two assertions that read the checker's **output** rather than its return value run against a **synthetic root**, threaded through `main(argv, root=ROOT)`.
Without them a mutation that deletes the scope line fails nothing, and `C-0161` says that is the finding rather than a gap in the test list.

## 6. The five verification gates

1. **Dimensional consistency** — every quantity is an integer count of files, links or commits; no unit is involved.
2. **Limiting cases** — the empty case (a synthetic root with one claim and three root documents), the saturated case (`if True: continue`, which skips every link and fails 15 tests), and the two ends of the file set (`_in_scanned_scope` asserted over a root document, a `gpd/` claim, a `tools/` file, a `third-party/` file and a non-Markdown path).
3. **Symmetry and conservation** — the resolver and the scope line share one `relative_links_in`, so the count reported and the set checked cannot diverge; `unscanned_markdown` is asserted disjoint from `tracked_markdown` on a synthetic root.
4. **Numerical convergence** — not applicable; the measurement is exhaustive rather than sampled. The history sweep visits **every** commit reachable from the revision, not a sample of them.
5. **Literature cross-check** — none exists for a corpus convention. The corpus's own precedents are the check: `C-0198` on named-before-added, `C-0083` on a gate that cannot come clean, `C-0176` on a declared list.

### The seven declared falsifiers

Declared in [`T-313`](../tasks/T-313-non-md-link-targets.md), committed at `a166544`, **one commit before** the implementation.

| | | |
|---|---|---|
| `F1` | 0 broken at `HEAD` | **held** — `0 broken link(s) in 672 file(s)` at `9b6588d`, and 0 again at the commit this claim lands in |
| `F2` | 0 false positives over the history | **held** — 6 pairs, all six genuine |
| `F3` | it reaches the class it was written for | **held** — both `C-0207` defects found, at their own commits |
| `F4` | every changed rule fails a named test in both directions | **held** — 22 of 22, over a green subtracted baseline |
| `F5` | the two false-positive classes are out of scope by construction | **held on the half that decides, and the other half moved for the best reason.** `tools/C-0156-claim-template.md ∉ tracked_markdown()`, and **0 surviving** `_without_code` as predicted; the RAW count is **6** rather than the predicted 5, because the task file stating the count is itself scanned — `CH-0182` inside a falsifier |
| `F6` | the scope line's shape residue is measured | **held** — 0 titled, 0 angle-bracket, 0 reference-style |
| `F7` | the fixture layout is declared and the baseline green | **held** — declared in the harness docstring; `baseline: green, 0 named failures` |

**None of the seven fired.** `F2` was the one that could have refused the deliverable and it did not.

## 7. What this does NOT establish

- **The file set is not closed, and it is the interesting half.** `tools/**/*.md` and `third-party/**/*.md` are still unscanned — 4 files. Measured: `tools/C-0156-claim-template.md` carries **23** relative links, of which **23 resolve against `gpd/claims`** — the directory the template is copied **to** — and **0** against `tools/`, where it sits. So *a relative link's correctness is a property of the file the text will end up in*, and expressing that is a **declaration** problem (a template stating its destination), not a predicate problem. `third-party/` is the problem definition as received and must not be edited, so it needs the exemption `tools/check-markdown-tables.py` already gives it. Filed as `T-317`; the measurement is done and the remedy named.
- **Three link shapes are still unreachable** — titled, angle-bracket and reference-style. All three are legal Markdown and all three are **0** in the corpus today, so this is a residue that is reported rather than closed. The report is derived, so it stops being 0 the moment somebody writes one.
- **A bare path in prose is not a link and this gate does not see it.** `tools/check-result-path-references.py` gates the one place the corpus writes one systematically — a claim's `Provenance` row.
- **The history sweep is a measurement of a PREDICATE, not a repair of the past.** It reports what the gate would have said; it does not say the four newly-found defects were harmful.
- **Nothing here is empirical.** No physics, no result file, no re-emission.

## 8. Still open — named, not answered

- The **file set** (`T-317`), with its remedy measured and unimplemented.
- Whether a **template's declared destination** belongs in the template, in the checker's table, or in neither. The third option is a permanent scope line, which is what ships today.
