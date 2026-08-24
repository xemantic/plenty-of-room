# C-0213 — the link gate's **file set** was an accident of one glob, not a decision, and the row's hard half dissolves on measurement: the claim template's destination does not have to be **declared**, because of this tree's **70** directories **exactly one** resolves all **23** of its links. Widened on a derived destination the gate reports **0** at `HEAD` and **0 false positives over 273 commits**; widened the naive way it is **20 pairs over 130 commits and every one of them false**

| | |
|---|---|
| **Task** | [`T-317`](../tasks/T-317-the-markdown-the-link-gate-does-not-scan.md), raised by [`C-0209`](C-0209-a-link-target-is-a-filename-whatever-it-names.md) (`T-313`), which closed the **link-kind** axis of the same gate and left the **file-set** axis measured and open |
| **Leaf** | — (process) |
| **Verification type** | **logical** — 82 named self-tests in the checker, a 44-row mutation table at 0 survivors over a measured and subtracted green baseline, and a replay of **each candidate predicate** over every commit reachable from `HEAD` |
| **Verdict** | **PASS on `F1`–`F8`.** `tools/T-313-mutation-test.py` reads `44 mutation(s), 0 survivor(s)` over a green subtracted baseline; `tools/check-corpus-links.py --selftest` reads `all checks passed` over **82** named tests; `--history --relocatable` reads `273 commit(s) swept, 0 with at least one hit, 0 distinct (file, link) pair(s) … resolved DERIVED` and `--in-place` reads `130 with at least one hit, 20 distinct (file, link) pair(s) … resolved IN PLACE`; `tools/T-295-mutation-input-census.py --check` reads `372 mutation(s) over 16 harness(es); 372 fixture-backed, 0 corpus-dependent, 0 survivor(s), 0 revived`, this harness's 44 among them, and `tools/P-31-harness-census.py --check` reads `44/44` anchors for it. **The corpus run reports one broken link and it is not this task's**: a concurrent agent's in-flight `T-321` task file names a challenge that has not been added yet (the identifier is deliberately not spelled here — quoting it would make it dangle in this claim too, which is `CH-0182` met inside a Verdict row) — `C-0198`'s *named and never added is exactly the defect, and the only thing distinguishing it from an in-flight one is time*, i.e. the pre-push gate working. Excluding that one file the run is **0 broken link(s)** |
| **Maturity** | TRL 1–3 process artifact. **No physics changed**: no Kotlin source is touched, no result file is emitted or re-emitted, and no number in the corpus moves |
| **Provenance** | [`tools/check-corpus-links.py`](../../tools/check-corpus-links.py) (the relocatable set, the derived destination, the excluded root, the quantified scope line, the retained `--unscanned` census, the `--history --relocatable` measurement and the argument guard; **82** named self-tests, up from 50), [`tools/T-313-mutation-test.py`](../../tools/T-313-mutation-test.py) (**44** mutations, up from 22, 0 survivors), [`tools/T-317-template-render-census.py`](../../tools/T-317-template-render-census.py) (the template/render evidence of §4, retained rather than described, **6** self-tests) |
| **Conditions** | The corpus at `646b29e` plus this iteration's edits, and the 273 commits reachable from it. `tools/` only |
| **Consumes** | [`C-0209`](C-0209-a-link-target-is-a-filename-whatever-it-names.md) (the widened predicate, the derived scope line, the `--history` instrument), [`CH-0266`](../challenges/CH-0266-the-rate-belongs-to-the-census-not-the-predicate.md) (*a census is dated by its predicate*; **make the measurement a mode of the checker**), [`C-0083`](C-0083-markdown-tables-that-do-not-render.md) (*a gate that cannot come clean is not a gate*; the `third-party/` invariant), [`C-0176`](C-0176-partial-discharge-and-restatement-predicates.md) (*a declared list is a dated object*; *a mutation must replace a rule wholesale*), [`C-0161`](C-0161-mechanics-on-an-imported-design.md) (*a mutation that fails nothing is the finding — construct the state*), [`C-0185`](C-0185-orphaned-mutation-anchors.md) (`P-31`; the subtracted baseline), [`C-0195`](C-0195-the-discriminating-input.md) (*a fixture layout is a dependency declaration*), [`C-0206`](C-0206-a-harness-output-format-is-an-interface.md) (the declared row shape) |
| **Constrains** | every relative link in **every** Markdown file of this repository except the problem definition as received — the corpus set resolved in place, the relocatable set resolved where the file is read |
| **Raises** | [`CH-0274`](../challenges/CH-0274-the-destination-did-not-have-to-be-declared.md), against `C-0209` §7/§8's and the `T-317` row's naming of this as a **declaration** problem with three admissible answers |

---

## The claim, in three lines

`tracked_markdown()` is `gpd/**/*.md` plus the root documents,
and the reason was never that Markdown elsewhere is exempt — it is that nobody had looked.
The one file that made looking hard turns out to need no declaration at all,
because a directory that resolves a file's whole link set is something you can **derive**.

## 1. The cheap bound ran first and it chose the branch

The queue offered a widening *with a template declaration mechanism* or a **recorded refusal**, and asked for the refusal to be measured rather than asserted.
Re-derived here at `HEAD` — not inherited; `CH-0182` records that a census is dated by its premise set and both may have moved:

| | row's figure | re-derived |
|---|---|---|
| Markdown the checker does not scan | 4 | **4** — `third-party/2026-08-ndi-gen1-problem-definition.md`, `tools/C-0156-claim-template.md`, `tools/oxdna/README.md`, `tools/oxdna/RESULTS.md` |
| of which clean | 3 | **3** — 0, 0 and 1 relative link, the one resolving **in place** |
| the template's relative links | 23 | **23** |
| resolving against `gpd/claims` | 23 of 23 | **23 of 23** |
| resolving against `tools/`, where it sits | 0 of 23 | **0 of 23** |

Every figure of the row reproduces.
Two further measurements, neither of them in the row, are what decide the shape:

- **`third-party/` needs no repair in order to be exempt.** The problem definition as received carries **0** relative links,
  so the exemption `tools/check-markdown-tables.py` gives it suppresses nothing and is a scope statement rather than a suppression.
- **The template's destination is DERIVABLE and it is UNIQUE.** Of the **70** directories of this tree, **exactly one** — `gpd/claims` — resolves all 23 of its links.
  There is nothing left for a declaration to say.

## 2. The false-positive rate, measured over the corpus's own history — and it refuses one reading and clears two

`CH-0266`'s own remedy is that the measurement should be a **mode of the checker**, so the next agent re-derives the rate in one command instead of writing a scanner nobody can date.
`tools/check-corpus-links.py --history --relocatable` replays a candidate predicate over every commit reachable from a revision, each commit's own tree supplying the existence set **and its own directory set**:

| reading | commits swept | commits with a hit | distinct `(file, link)` pairs | false |
|---|---|---|---|---|
| resolved **in place** (`--in-place`) | 273 | **130** | **20** | **20 of 20** |
| resolved against a **derived** destination | 273 | **0** | **0** | — |

All twenty are the template's, and all twenty are correct links read from the wrong place.
**A 100 % false-positive rate over 130 commits is exactly the rate `CLAUDE.md` records as the one at which a build-failing gate gets switched off**, so the naive widening is refused on a measurement rather than on taste — `F3`.
The derived reading fires on nothing, at any commit — `F2`.

**And a declared destination scores identically to a derived one: 0 over 273.**
The two disciplined readings are indistinguishable in what they catch, so the tie breaks on **cost**, and a derivation costs no dated object.

## 3. What ships: three sets, all three named on every run

1. the **corpus set** — `gpd/**/*.md` plus the root documents. Links resolved **in place**. Gated. **Unchanged**, and `T-313`'s 22 mutation rows still all fail named tests (`F5`).
2. the **relocatable set** — every other Markdown outside an excluded root. A file here is not necessarily read where it sits,
   so it is judged against the directory that resolves **most** of its links — its own where that works, a derived one otherwise —
   and gated on the one unambiguous failure: *no directory of the repository resolves all of this file's links*.
3. the **excluded root** — `third-party/`, counted and named, never scanned.

```
# scope: ANY relative target kind (N non-.md today); 3 relocatable file(s) carrying 24 link(s), resolved where the file is READ; 4 .md outside the corpus set (third-party, tools); NOT scanned: 1 excluded (third-party); NOT matched: 0 titled, 0 angle-bracket, 0 reference-style link(s)
```

The broken-link count, the file count and `N` move with every sibling commit in a parallel iteration, so they are **not** quoted here as if they were this task's — `CH-0182`, and this claim is written while four agents are filing into the same tree.
What this task fixes are the counts that do not move under a sibling: **3** relocatable files, **24** links, **1** excluded root, **0** of each unmatched shape.

`C-0209` shipped the residue as a **file** count, and *"4 .md outside gpd/ and the root"* could be four links or four thousand — a reader of a clean run could not tell.
It is now quantified in **links** and derived on every run (`F7`).
**And widening a tool's flag set is a `CH-0268` §4b obligation**, taken here rather than deferred: this task takes the checker from one flag to five,
it is wired **twice** — `build.gradle.kts` with `--selftest`, `tools/verify.sh` with `--selftest` and then nothing —
and a mistyped `--self-test` would have run the corpus check and exited **0**, which is the wired-and-inert shape verbatim and is the instance this repository already recorded.
An unrecognised argument now exits **2** with the usage, held open by a named test in each direction and two mutation rows.

`--unscanned` is the retained census, and it reports rather than gates:

```
tools/C-0156-claim-template.md	RELOCATABLE	23 link(s)	-> read from gpd/claims
tools/oxdna/README.md	RELOCATABLE	0 link(s)	-> in place
tools/oxdna/RESULTS.md	RELOCATABLE	1 link(s)	-> in place
third-party/2026-08-ndi-gen1-problem-definition.md	EXCLUDED	-	-> the problem definition as received
```

## 4. What the widening buys, stated honestly, because it is not much

**Over the whole history it catches nothing**, and that is the number rather than a hedge: 0 pairs over 273 commits.
Three facts bound the value from below and they are all measured here:

- The template's 23 links are **already** checked, in scope, in the artifact `tools/T-250-emit-result.py` renders it into.
  Measured by [`tools/T-317-template-render-census.py`](../../tools/T-317-template-render-census.py) — retained rather than described, because `CH-0266`'s finding was a census with no artifact to re-run:
  `130 commit(s) carry both the template and its render; the link multiset is identical at 130 of them; 0 commit(s) carry the template ALONE`.
- So the surface this gate adds over the *status quo ante* is the **window between a template edit and its render**, plus `tools/oxdna/RESULTS.md`'s single in-place link, which was gated by nothing.
- What it removes is the reason nobody could see that: the residue was a file count, and it is now a link count.

A recorded refusal was the legitimate alternative and it was seriously held until §1's uniqueness measurement.
What defeated it is that **the exclusion was never a decision** — `tracked_markdown()`'s glob is an accident of the checker's first commit — and a recorded decision would have recorded an accident as a policy.
Given that the disciplined widening costs no dated object and fires on nothing, refusing to gate a number the run already computes would have been perverse.

## 5. The mutation table, and its two survivors were the finding

`tools/T-313-mutation-test.py` goes from 22 rows to **44**, at **0 survivors**, over a baseline run first and refused on if red.
The twenty-two new rows revert and over-widen every rule this task adds.

| what is mutated | narrowed | widened |
|---|---|---|
| `_EXCLUDED_ROOTS` | the problem definition stops being excluded (1) | nothing is relocatable any more (1) |
| the partition of the unscanned set | inverted (1) | — |
| `tree_directories` | collapsed to the repository root (1) | version control and build output become candidates (1) |
| `resolving_directories` | — | a destination need resolve only SOME links (1) |
| `destination_of` | the empty-link guard (1), the own-directory preference (1) | a link resolving nowhere stops removing the destination (1) |
| `relocatable_defects` | the in-place reading becomes the only reading (1), the tie-break stops preferring the file's own directory (1) | the in-place reading becomes unreachable, so the refusal cannot be re-run (1) |
| the scope line | the relocatable link count (1), the excluded root (1) | — |
| the history scope | stops excluding the problem definition (1) | widened to the corpus set (1) |
| the gate and the census | a relocatable defect stops failing the run (1), the census stops reporting (1) | the census starts GATING (1) |
| the argument guard | an unrecognised argument stops being refused (1) | anything that merely LOOKS like a flag is accepted (1) |

**Two of the twenty survived the first run, and `C-0161` says that is the finding rather than a gap in the test list. Both were `CLAUDE.md`'s *a guard whose only observable behaviour is duplicated downstream*, and they wanted opposite repairs:**

- `destination_of`'s `if not links: return own_directory` is unreachable **as the fixture stood**, because an empty link set resolves **vacuously** against every candidate and the own-directory preference then answers for it.
  It is observable in one state — where the file's own directory is *not* among the candidates — and that state was **constructed** as a named test rather than the guard deleted.
- `relocatable_defects`'s `if destination_of(...) is not None: continue` was **genuinely redundant**: a file that has a destination has nothing failing at its best candidate, so the short-circuit and the per-link naming are the same test written twice.
  It is **deleted**, and the tie-break it was hiding — that a diagnostic names the file's own directory rather than the alphabetically first — is written down and given a fixture that can tell the two apart.

The first repair is a fixture, the second is a deletion, and no reading of the table could have told them apart: it took writing down what each guard was for.
Both were caught by the harness on its first run after the rows were added.

**A third row survived, and its repair was the ROW rather than the code.** Over-widening the argument guard to `unrecognised = list(argv)` makes the subject refuse `--selftest` as well,
so the suite never runs, no named test can fail, and the harness reads it as a survivor — `C-0206`'s *a crash is not a named test*, and reporting it as a survivor is the correct conservative direction rather than a defect.
The over-widening that measures the rule has to leave `--selftest` recognised, and *anything that merely looks like a flag is accepted* is the real mistake anyway.
**A mutation that stops the suite is not a measurement of the rule; it is a measurement of the harness.**

## 6. The five verification gates

1. **Dimensional consistency** — every quantity is an integer count of files, links, directories or commits; no unit is involved.
2. **Limiting cases** — the empty case (a synthetic root with one claim and three root documents), the saturated case (`_EXCLUDED_ROOTS` widened until nothing is relocatable), a file with **no** links, a file whose links resolve **nowhere**, and the naive `in_place` reading at both ends.
3. **Symmetry and conservation** — `relocatable_markdown` and `excluded_markdown` are asserted to **partition** `unscanned_markdown` and to be disjoint, on a synthetic root; a file falling out of both halves would be checked by nothing again, which is the defect this task exists to close.
4. **Numerical convergence** — not applicable; the measurement is exhaustive rather than sampled. The history sweep visits **every** commit reachable from the revision.
5. **Literature cross-check** — none exists for a corpus convention. The corpus's own precedents are the check: `C-0083` on the `third-party/` invariant and on a gate that cannot come clean, `C-0176` on a declared list, `CH-0266` on a measurement that must be a mode of the tool.

### The eight declared falsifiers

Declared in [`T-317`](../tasks/T-317-the-markdown-the-link-gate-does-not-scan.md), written before the implementation.

| | | |
|---|---|---|
| `F1` | clean at `HEAD` over all three sets | **held** — 0 broken links attributable to this task over all three sets; the one line the run prints is a concurrent agent's in-flight `T-321` naming a challenge not yet added |
| `F2` | 0 false positives over the history, derived reading. Predicted **0** | **held** — 273 commits, 0 hits, 0 pairs |
| `F3` | the naive reading is refused by measurement. Predicted **20 pairs over 130 commits, all false** | **held** — 20 pairs, 130 commits, all twenty the template's and all twenty false |
| `F4` | the destination is derived and **unique** | **held** — 70 directories, exactly **1** resolves all 23 links, and it is `gpd/claims` |
| `F5` | the corpus-set gate does not move | **held** — 0 defects, and all 22 of `T-313`'s rows still fail a named test |
| `F6` | `third-party/` is exempted without being repaired | **held** — **0** relative links in it, so the exemption suppresses nothing |
| `F7` | the residue is quantified in links and derived on every run | **held** — `3 relocatable file(s) carrying 24 link(s)`, `1 excluded`, all computed |
| `F8` | every changed rule fails a named test in both directions, hermetically | **held on the second run, and the first run is §5.** 44 of 44, over a green subtracted baseline, and `tools/T-295-mutation-input-census.py --check` reads all 44 **fixture-backed, 0 corpus-dependent** |

**`F3` is the one that could have refused the deliverable and it refused half of it** — the naive widening, which is what the row's own framing would most naturally have produced.

## 7. What this does NOT establish

- **The gate catches nothing that has ever existed.** Stated as a number in §4 rather than hedged. Its value is prospective and the honest measure of it is the window between a template edit and its render.
- **A derived destination is WEAKER than a declared one**, and by a stated amount: a file whose links happen to resolve from some unintended directory passes.
  The exposure is bounded by the link count — the 23-link template admits exactly one directory, while a one-link file (`tools/oxdna/RESULTS.md`, naming `README.md`) admits **seven**, and only the own-directory preference keeps it honest.
  A file with one link and a mistyped target that exists elsewhere in the tree would pass; no link checker in this repository catches a wrong-but-existing target in any case.
- **Whether a template's render is up to date with its template** is a different gate about a different defect, and it is **not** shipped: `tools/T-317-template-render-census.py` reports and does not gate, and it is not wired. It is evidence for §4's honesty, at 130 of 130, and nothing rests on it staying true.
- **The three link SHAPES remain unreachable** — titled, angle-bracket, reference-style, all still 0 and all still counted. `T-313`'s residue, untouched.
- **The set of excluded roots is one tuple and it is a declared object.** It has one member, it mirrors `tools/check-markdown-tables.py`, and it is mutation-tested in both directions; it is not derived, because *"which directories of this repository must not be edited"* is not a fact the tree carries.
- **Nothing here is empirical.** No physics, no result file, no re-emission. `CLAUDE.md` records that a result file whose subject is the corpus is destructive to re-run, so this task emits none.

## 8. Still open — named, not answered

- The **link shapes** (`T-313`'s residue), reported on every run and 0 today.
- Whether the **excluded-root tuple** should ever carry more than one member. It should not until a second invariant of that kind exists, and the scope line names the one that does.
