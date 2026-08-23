# C-0196 — **a name cannot govern a token either: the refinement window now reads the same blanked text the match and the line context do, and the reason the row was hard to land is measured rather than assumed — the corpus's own filenames carry `81` of the five governing words, so the in-scope zero is a coincidence of PLACEMENT and not a property of how this corpus names its files**

| | |
|---|---|
| **Task** | [`T-293`](../tasks/T-293-a-name-cannot-govern-a-token.md), raised by [`C-0189`](C-0189-a-filename-cannot-supply-a-context.md) (`T-287`) §4 |
| **Leaf** | none — a **process** claim protecting the census that measures the two customer-facing documents' debt |
| **Verification type** | **logical** — a slug census over every tracked path, and a before/after delta against the census executed out of `git show <ref>:` |
| **Verdict** | **PASS on all five predicates.** `F1` 0 removed and 0 added, `F2` 0 in-scope family changes, `F3` the gate reads 0 before and after, `F4` five named tests from the corpus's own instance and one mutation killed by exactly two of them, `F5` the cheap bound answered with a number |
| **Maturity** | **TRL 1–3, and below it: NO PHYSICS CHANGED, AND NO NUMBER ANY CLAIM QUOTES MOVED.** The census's own reading is byte-identical before and after |
| **Provenance** | `gpd/results/T-293-a-name-cannot-govern-a-token.json`, emitted by [`tools/T-293-emit-result.py`](../../tools/T-293-emit-result.py); the predicate and its five named tests in [`tools/T-234-census.py`](../../tools/T-234-census.py); the mutation row in [`tools/T-234-mutation-test.py`](../../tools/T-234-mutation-test.py) |
| **Conditions** | The tree at `1c598f8` plus this iteration's edits, recorded as `baselineRef` in the result file. Units unchanged and untouched: nm, pN, pN/nm, pN/nm² = 1 MPa exactly, `k_BT = 4.141947 pN·nm` at 300 K, aqueous buffer with stated Mg²⁺. Nothing physical is computed |
| **Consumes** | [`C-0189`](C-0189-a-filename-cannot-supply-a-context.md) (the line context, and the deferral this discharges), [`C-0184`](C-0184-a-slug-is-not-a-statement.md) (*one delta at a time*, and the index-keyed table trap), [`C-0176`](C-0176-partial-discharge-and-restatement-predicates.md) (a mutation must replace a rule wholesale; a fixture written to the shape of the change is a defect), [`C-0158`](C-0158-prose-gate-red.md) (record the gate's actual reading), [`C-0161`](C-0161-mechanics-on-an-imported-design.md) (*a mutation that fails nothing is the finding* — construct the state) |
| **Raises** | [`CH-0246`](../challenges/CH-0246-a-corpus-subject-result-file-cannot-be-re-run-as-a-control.md) — `C-0110`'s *run the consumers* is **destructive** for a result file whose subject is the corpus (§8). One residue is **recorded and deliberately not repaired** (§5) |

---

## The claim, in one line

`refine(text, …)` becomes `refine(hunted, …)`,
and the question that made the row awkward — *is the in-scope zero because the defect is rare, or
because the governing words do not occur in this corpus's slugs?* — is answered by one pass over
the **1 727** basenames tracked at the recorded ref: the words occur **81** times, so the zero is **rare**, not
**structural**, and the change is warranted on exactly the ground `T-285` and `T-287` were.

---

## 1. The cheap bound, which is the row's own and ran before the predicate was touched

`T-293`'s queue row states the first move and this claim did not inherit it:

> *ask whether the in-scope effect is zero because the defect is rare or because the refinement's
> governing words happen not to occur in this corpus's slugs — one pass over every slug in the
> corpus against the two governing-word patterns settles it.*

There are **five** patterns a refinement consults, not two.
Over the **1 727** paths tracked at the recorded ref, matched against the **basename** alone.
The scan itself walks tracked *and* untracked-but-not-ignored files — the corpus
`census.corpus_files` reads, so that an in-progress claim is visible to it — and the result
file carries **both** counts, because the second moves while the claim is being written
(`CH-0182`, §7) and the first is re-derivable from the SHA:

| pattern | slug hits | files | example |
|---|---|---|---|
| `_STRUCTURAL_MODEL` | **19** | 19 | `grillage` in `C-0154-honeycomb-grillage.md` |
| `_ATTRIBUTIVE` | **0** | 0 | — |
| `_ROW_WORDS` | **31** | 28 | `x-raster` in `CH-0172-a-honeycomb-x-raster-carries-both-turn-senses.md` |
| `_WIDTH_WORDS` | **30** | 27 | `nominal`, `width` in `CH-0101-the-nominal-tile-width-is-not-a-buildable-raster-width.md` |
| `_DRAWABLE_RASTER` | **1** | 1 | `closes` inside `forecloses`, in `CH-0218-…` — see §5 |
| **total** | **81** | | |

**So the answer is `rare`, and it decides the row.**
This corpus names its files after exactly the concepts its refinements read for,
which is not a coincidence — a claim about the grillage is called `…-grillage.md` —
so the coincidence that is missing is only that a **family token** rarely stands
within one refinement window of such a slug.
A file named tomorrow can make it bite, and nothing would notice.

Had the census read **zero**, the honest deliverable would have been the row's other branch —
*the stated decision that a name may govern a token* — and this claim would say so.

---

## 2. What changed

One expression in `occurrences()`:

```
-            name = refine(text, match.start(), match.end()) if refine else family
+            name = refine(hunted, match.start(), match.end()) if refine else family
```

`blank_identifiers` is length-preserving (`T-285`), so `match.start()` still indexes the file on
disk and every offset, line number and snippet below the change is unmoved — asserted by a named
test rather than argued.

---

## 3. The delta, measured against the committed predicate

The **before** reading is run, not remembered: `tools/T-234-census.py` and
`tools/T-234-classification.json` are read out of `git show <ref>:` and executed there.

| | before | after |
|---|---|---|
| occurrences in scope | **380** | **380** |
| removed | — | **0** |
| added | — | **0** |
| in-scope occurrences whose family changes | — | **0** |
| `tools/T-234-census.py --check` | **0** | **0** |

**`F1` is the predicate that matters and it is a structural statement, not a hope**:
a refinement runs *after* the family match and the line-context test, so it can **rename** an
occurrence and can neither create nor destroy one.
A non-zero count there would mean the edit reached a rule it is not about.

**The classification table is deliberately NOT regenerated.**
It is keyed on the occurrence **index**; no occurrence is removed or added, so no index moves,
and no in-scope occurrence changes family, so no entry is stale.
`C-0184`'s intermediate-reading trap — where blanking a token re-points every entry below it and
the defect count falls for a reason that is not a repair — does not arise here, and the reason it
does not is worth stating rather than leaving to inspection.

### The one occurrence the change moves, read individually

Over **every** tracked markdown file, exactly one:

| file | line | token | on the original text | on the blanked text | in scope |
|---|---|---|---|---|---|
| `gpd/challenges/CH-0229-a-census-assumes-a-premise-is-withdrawn-once.md` | 19 | `single-layer square-lattice` | `GRILLAGE` | `PLACEMENT` | **no** |

The sentence is *"The census's `PLACEMENT` family matches one string, `single-layer
square-lattice`, for both halves"* — a **meta**-statement about the census itself — and the only
structural-model word within `STRUCTURAL_WINDOW` of it is `grillage`,
occurring twice and **only inside two link targets**,
`C-0154-honeycomb-grillage.md` and `C-0167-coupled-cells-on-the-honeycomb-grillage.md`.
Those are **names**. They assert nothing about which structural model the sentence's token is
about, and unblanked they decided it.
The file is a challenge, which this census does not read, so the gate's reading is unchanged.

---

## 4. How the change is held open, given that its in-scope effect is zero

`T-293`'s row records the difficulty precisely — *no named test over any file this census reads
could hold it open, and it would land untested* — and offers a synthetic fixture as the obvious
answer while naming `C-0176` §1b's objection to one.

**The escape is that the corpus's own single instance is a real string.**
The two fixtures below are `CH-0229` line 19's shape — a token, and its governing word present
only inside an `<ID>-<slug>.<ext>` link target — and the discriminating pair for the *width*
refinement is the same shape on the other function:

| named test | before | after |
|---|---|---|
| `T-293 a structural-model word inside a FILENAME does not refine PLACEMENT to GRILLAGE` | **fails** | passes |
| `T-293 and the same word in the line's own PROSE still refines it` | passes | passes |
| `T-293 a row word inside a FILENAME does not refine WIDTH to ROW_SPAN` | **fails** | passes |
| `T-293 and the same word in the line's own PROSE still refines it` | passes | passes |
| `T-293 the refinement is taken on blanked text, which is length-preserving, so the occurrence's offset still indexes the ORIGINAL text` | passes | passes |

Two of the five fail before the change and pass after, and they exercise **both** refinement
functions — `refine_placement` and `refine_width` — which is what makes the pair a test of the
*rule* rather than of one call site.
The three that pass in both directions are the paired controls: without them a test that a
filename does **not** supply a word is satisfied by a predicate that never sees any word at all.

**Mutation coverage.** `tools/T-234-mutation-test.py` gains one **NARROWING** —
*the REFINEMENT window is read from the ORIGINAL text — a filename decides which of two discharges
a token takes again* — which replaces the rule wholesale (`C-0176`: never widened to
`original|mutant`).
It fails exactly the two FILENAME tests above and nothing else,
and the harness reports **0 mutations failing nothing** over its whole table.

---

## 5. A residue, recorded and deliberately not repaired

`_DRAWABLE_RASTER`'s `closes` alternative carries **no word boundary**, so it matches inside
`closest` and `forecloses`.
Over the markdown corpus: **523** whole-word occurrences of `closes` against **74** substring-only
ones (`closest` 67, `forecloses` 5, one run-together token 2).

Measured, it **bites zero times** at this ref — no `drawable` token's refinement window is decided
by such a substring — so it is a **latent** defect of the same predicate this claim edits.
It is recorded here and in the result file rather than repaired, for `C-0184`'s reason, which is
the reason this row existed at all: **one delta at a time, or none can be audited against the
others**, and a repair whose in-scope effect is also zero would land in the same commit as one
whose in-scope effect is zero for a *different* reason.

---

## 6. What this claim does not say

- It does not say the census is now correct about `CH-0229` line 19. That occurrence is out of
  scope and unclassified either way; what is claimed is that a **name** did not decide it.
- It does not say the change is worth anything **today**. Its in-scope effect is **0**, stated
  plainly, and its value is that the corpus's slugs carry 81 of the governing words, so the next
  file to be named can make it bite.
- It does not extend to the **line context**'s or the **match**'s readings, which `T-287` and
  `T-285` already own; the three signs are now all one rule and this claim closes the family.

---

## 7. `CH-0182`, for the tenth consecutive iteration, and this claim is inside it

Writing §3's worked example and this section put **three** occurrences of `single-layer square-lattice` into a file
this census reads, so the gate went red on the claim reporting it — *"unclassified:
`gpd/claims/C-0196-…md`#0 line 105 `[GRILLAGE]`"* — before a word of the claim was wrong.

Both readings are published rather than one, which is `CH-0182`'s own remedy:

| | without this claim's own three | with them |
|---|---|---|
| occurrences the census reads | 377 | **380** |
| removed / added / in-scope family changes | 0 / 0 / 0 | **0 / 0 / 0** |

**The totals move and the answer does not**, which is why every acceptance predicate of this task
is written on a **delta** and none on a count.
The two occurrences are registered by hand as `RECORD` — a table cell reporting *which string the
census matched*, and a verbatim quotation of `CH-0229`'s own sentence — because a token quoted as
**data** asserts nothing about a structural model, which is the same distinction `T-283` made for a
status word inside backticks.

---

## 8. The consumer run was attempted, and it is how `CH-0246` was found

`C-0110`'s rule is unambiguous — *a proof that a shared-source change is invisible is not a
substitute for running the consumers* — so the six emitting importers of `tools/T-234-census.py`
were run.

**All seven committed files moved, and not one movement is this change.**
Every one names a `baselineRef` older than `HEAD` and every emitter's `--ref` defaults to `HEAD`,
so the obvious command re-bases the measurement onto today's corpus.
The sharpest is `T-287-a-filename-cannot-supply-a-context.json`, whose whole subject is *the ten
occurrences a filename admitted* and which re-runs to `removed` **10 → 0** — because `T-287`
removed them.
The seven were **restored unmoved**, and the class is raised as
[`CH-0246`](../challenges/CH-0246-a-corpus-subject-result-file-cannot-be-re-run-as-a-control.md).

What replaces the run here is a **containment** argument about the consumers' **inputs**:

1. the predicate moves exactly **one** occurrence's family over every tracked markdown file (§3);
2. that occurrence is in `gpd/challenges/CH-0229-…md`;
3. `in_scope(path)` is one line — `path.startswith("gpd/claims/") or path in ("TASKS.md",) + DELIVERABLES` —
   and it excludes `gpd/challenges/`;
4. so **no** consumer reads the file the change moves, and none is stale.

That is admissible where `C-0110` refuses a proof, because it is a proof about what the consumers
**read**, not about what they compute.

**One further importer is recorded rather than repaired.**
`tools/T-234-emit-result.py` crashes on `for family, _p, _c in census_tool.FAMILIES` — `FAMILIES`
gained a fourth element in `T-260`/`T-262` — and it crashes identically under `HEAD`'s own
predicate, so it is pre-existing arity drift and not this change.
