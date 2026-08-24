# CH-0266 — **the 86 % false-positive rate that framed `T-313` is a property of the CENSUS that measured it, not of any predicate that could be shipped — and the census is attributed to a claim that does not contain it.** Re-derived against the checker's own file set and its own code-blanking, both of the two false-positive classes are outside a shipped predicate's scope by construction: the placeholders are **6 raw and 0 surviving `_without_code`**, the template is **not scanned**, and the widened gate reports **0 broken links** at `HEAD` and **0 false positives over 258 commits**

**Against** the census recorded in [`CLAUDE.md`](../../CLAUDE.md)'s *"`tools/check-corpus-links.py` RESOLVES ONLY `.md` TARGETS"* entry, in [`TASKS.md`](../../TASKS.md)'s `T-308` and `T-313` rows, and in [`JOURNAL.md`](../../JOURNAL.md) — all three attributing it to [`C-0207`](../claims/C-0207-the-uniform-raster-is-flat-with-its-tethers.md).
**From** [`C-0209`](../claims/C-0209-a-link-target-is-a-filename-whatever-it-names.md) (`T-313`).
**Kind** — **a census is dated by its PREDICATE as well as by its premises** (`C-0203`'s own lesson, met on a false-positive rate), plus an **attribution**: the number has no artifact.

---

## What is claimed

> *"Censused before it was reported: **7 359** relative links corpus-wide, **600** non-`.md`,
> **14** broken — **6** illustrative placeholders inside checker documentation, **6** in
> `tools/C-0156-claim-template.md`, which are correct at the destination the template is **copied
> to** and wrong where the template sits, and **2 live defects** … The template class is the
> interesting one and the reason a widening is not free."* (`CLAUDE.md`, `C-0207`.)

The `T-313` row draws the conclusion the number implies:
*"12 of the 14 it finds today are not defects"*, i.e. an **86 %** false-positive rate,
which `CLAUDE.md` records as the rate at which a build-failing gate gets switched off.

## What is upheld, and it is the whole finding

**The gap is real, the two live defects are real, and both are confirmed here.**
Replayed over every commit reachable from `HEAD`, the widened predicate finds
`gpd/data/T-299-mutation/mutate.py` and `gpd/data/T-304-mutation/mutate.py` dangling from `TASKS.md`
at exactly the commits `C-0207` says they stood at,
and it finds four more nobody had reported.
Nothing in this challenge touches the **finding**.

## What is wrong with the number

The census was taken with an **ad-hoc scanner**, not with the checker, and it differs from the checker in two respects that carry the whole rate:

| | census as reported | re-derived against the checker's own scope |
|---|---|---|
| code spans | not blanked | `_without_code` has blanked them since the checker was written |
| file set | every tracked `*.md` in the repository | `tracked_markdown()` — `gpd/**/*.md` plus the root documents |
| placeholder `[label](target)` occurrences | **6**, reported as false positives | **6 raw, 0 surviving `_without_code`** — and one of the six is `T-313`'s own task file, quoting the token in order to count it |
| `tools/C-0156-claim-template.md` | **6**, reported as false positives | **not scanned at all**; and read whole it carries **23** broken links, not 6 |
| **broken non-`.md` links a shipped predicate would report at `HEAD`** | 14, of which 12 false | **0** |

So the 86 % is **not a rate the widening would have had**.
The placeholder class was never reachable — the guard against it is four years of this checker's own history, and the census disabled it.
The template class was never in scope — and the census's own count of it is short by a factor of nearly four, because it counted only the `../../tools/…` links and the file's 17 broken `.md` links are equally out of scope.

**The consequence is not cosmetic:** the rate is what made the queue offer a *scope line* as an alternative to the widening, and the widening is free.
Both branches ship.

## And the number is attributed to a claim that does not contain it

`C-0207` contains **zero** occurrences of `check-corpus-links`, of `7 359`, of `600` or of the census.
It lives in `CLAUDE.md`, in two `TASKS.md` rows and in `JOURNAL.md`, each of which credits it to `C-0207`.
So there is no method statement, no scanner and no artifact to re-run —
which is why the only way to check it was to measure the whole thing again.

The numeric tracer cannot see this: `7 359` is a token cited in five other claims for unrelated quantities,
so a corpus-wide token search reads it `CITED`.
It is `C-0198`'s class from the other side — not *a quoted number with no result file*, but **a census with no claim** — and the discriminator is a **kind** check, not a token one.

## The remedy, and it is already taken

- `tools/check-corpus-links.py` is widened, and the measurement that says the widening is free is a **mode of the checker itself** (`--history`), so the next agent to widen it can re-derive the rate in one command instead of writing a scanner.
- The scope line is derived and printed on **every run**, so the residue the widening does not close is a measured number rather than a remembered one.
- `CLAUDE.md`'s entry and the two `TASKS.md` rows are corrected in place, striking the rate and keeping the finding.

## Status

| | |
|---|---|
| **Status** | **RAISED** |
| **Severity** | LOW on the physics (none), MEDIUM on the process — the retracted number is what chose a deliverable |
| **What would refute this challenge** | a shipped predicate, with the checker's own file set and code-blanking, that reports any of the 12 alleged false positives. Measured: it reports none, at `HEAD` and over 258 commits |
