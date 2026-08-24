# CH-0274 — **the file-set residue was named a *declaration* problem with three admissible answers, and the answer is a fourth: the destination is DERIVABLE, and on the one file that needs it, UNIQUE.** Of this tree's **70** directories, **exactly one** — `gpd/claims` — resolves all **23** of the claim template's links, so nothing is left for a declaration to say; and the exclusion the row proposed recording as a decision was never a decision, but the glob the checker was born with

**Against** [`C-0209`](../claims/C-0209-a-link-target-is-a-filename-whatever-it-names.md) §7 and §8, [`T-313`](../tasks/T-313-non-md-link-targets.md)'s *Out of scope* section, and the `T-317` row of [`TASKS.md`](../../TASKS.md), which carry the same framing in the same words.
**From** [`C-0213`](../claims/C-0213-a-link-is-read-where-the-file-ends-up.md) (`T-317`).
**Kind** — **a question whose admissible answers you enumerate cannot return the one you did not enumerate** (`CLAUDE.md`, on the six decisions put to NDI), met on a **remedy** rather than on a specification question.

---

## What is claimed

> *"So a relative link's correctness is a property of the file the text will end up in, and expressing that is a **declaration** problem (a template stating its destination), **not a predicate problem**."* — `C-0209` §7.

> *"Whether a **template's declared destination** belongs **in the template**, in **the checker's table**, or in **neither**. The third option is a permanent scope line, which is what ships today."* — `C-0209` §8.

The `T-317` row inherits both, offering *"a way for a TEMPLATE to **declare** the directory it is copied to"* against *"a **recorded decision** that the class stays out of scope"*,
and adds the reading that follows from them: *"That is the only instance the corpus has, which is why the honest shape may be the recorded decision rather than the declaration."*

## What is upheld, and it is the finding

**Every measurement in the three artifacts reproduces**, re-derived rather than inherited:
4 unscanned Markdown files, 3 of them clean, the template's **23** relative links, **23 of 23** resolving against `gpd/claims` and **0 of 23** against `tools/`.
And the diagnosis is right and is the whole point: *a relative link's correctness is a property of the file the text will end up in*.
Nothing here touches that.

## What is wrong with the framing

**A destination that can be derived does not have to be declared, and this one can.**
A file's destination is *the directory against which its whole link set resolves*, and the repository is a finite set of directories to try — **70** of them.
Measured on the one file that needs one:

| | |
|---|---|
| candidate directories in the tree | **70** |
| directories resolving **all 23** of `tools/C-0156-claim-template.md`'s links | **1** — `gpd/claims` |

So `C-0209` §8's three options are *in the template*, *in the checker's table*, *in neither* — and the answer is **in none of the three**: it is **in the link set**, which is the only place that cannot go stale.
`C-0176`'s *a declared list is a dated object* is thereby avoided rather than paid, and the mechanism the row asked to have tested does not need to exist.

**And the second half of the framing is the same error inverted.**
Recording the class as *out of scope* would have recorded an **accident** as a **policy**: `tracked_markdown()` is `gpd/**/*.md` plus the root documents because that is what the checker's first commit globbed, not because Markdown elsewhere is exempt.
There is no artifact in this corpus deciding that `tools/**/*.md` should be unchecked.

## The measurement that settles the cost, and it goes the other way from the row's expectation

`CH-0266`'s own remedy is that such a rate must be a **mode of the checker**.
`tools/check-corpus-links.py --history --relocatable`, over every commit reachable from `HEAD`:

| reading | commits swept | commits with a hit | distinct `(file, link)` pairs |
|---|---|---|---|
| resolved **in place** — the naive widening | 273 | **130** | **20**, all the template's, **all twenty false** |
| resolved against a **derived** destination | 273 | **0** | **0** |
| resolved against a **declared** destination | 273 | **0** | **0** |

The declared and the derived readings are **indistinguishable in what they catch**, so the choice between them is a choice about **cost** alone — and the row priced only the declared one.

## What this does NOT claim

- The widening catches **nothing** that has ever existed. `C-0213` §4 states that as a number rather than hedging it, and the honest value of the gate is prospective.
- A derived destination is **weaker** than a declared one where a file carries few links: the 23-link template admits exactly one directory, a one-link file admits seven, and only the own-directory preference keeps that honest.
- `C-0209`'s **finding**, its widening, its scope line and its history instrument are untouched and are what made this measurable in one afternoon.

## Status

| | |
|---|---|
| **Status** | **RAISED and ANSWERED in the same iteration** by [`C-0213`](../claims/C-0213-a-link-is-read-where-the-file-ends-up.md) / [`T-317`](../tasks/T-317-the-markdown-the-link-gate-does-not-scan.md). The file set is widened on a derived destination, `third-party/` is excluded, and the mechanism `C-0209` §8 could not place is not built because it is not needed |
| **Severity** | LOW on the physics (none), MEDIUM on the process — the framing chose between two deliverables and the shipped one is neither |
| **What would refute this challenge** | a second directory of this repository resolving all 23 of the template's links, which would make the derivation ambiguous and the declaration owed; or a firing of the derived predicate over the history that is not a genuine dangling reference. Measured: **1** resolving directory, and **0** firings over 273 commits |
