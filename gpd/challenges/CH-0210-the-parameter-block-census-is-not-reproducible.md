# CH-0210 — `CH-0207`'s **split table is not reproducible from the corpus it was measured on**: it publishes **49 numeric / 59 string / zero mixed** and **1 004 numeric leaves**, and re-derived at the very commit it was filed on (`bed0197`, 147 result files) the corpus gives **50 files carrying at least one numeric parameter leaf, 63 carrying only strings, and 14–20 MIXED** — no key set, and no depth convention, reproduces the published row. The **thesis is upheld and strengthened**: one more file is exposed than was claimed, and *"zero mixed"* was the evidence that the exposure is a per-STUDY rendering convention, where the measurement says it is decided **per FIELD**

| | |
|---|---|
| **Against** | [`CH-0207`](CH-0207-a-parameter-block-cannot-re-run-its-own-study.md)'s *"The split nobody chose"* table and its `1 004 numeric leaves sit in the 49`, and the sentence built on them — *"Which side a study lands on is a per-study rendering convention"* |
| **Raised by** | [`C-0162`](../claims/C-0162-round-outputs-never-inputs.md) (`T-268`), while closing `CH-0207` |
| **Grounds** | **in-silico** — the census re-derived over `git archive bed0197 gpd/results`, the unmodified tree at the commit that filed `CH-0207`, under every key set and depth convention that could plausibly have produced it |
| **Status** | **OPEN** — filed with the correction in hand. It changes no verdict: `CH-0207` is closed by `C-0162` on grounds this challenge makes **stronger**, not weaker |

---

## The measurement

At `bed0197` — 147 committed result files, the state `CH-0207` measured:

| reading | files with **≥ 1 numeric** parameter leaf | files with **only strings** | **mixed** | numeric leaves |
|---|---|---|---|---|
| `parameters` + `runParameters`, all depths | **50** | 63 | **14** | 835 |
| …+ `citedInputs`, all depths | **50** | 63 | **20** | 701 |
| `parameters` + `runParameters`, depth 1 | 50 | 63 | 14 | 835 |
| `parameters` only, depth 1 | 46 | 48 | 14 | 753 |
| **`CH-0207` publishes** | **49** | **59** | **0** | **1 004** |

A search over every subset of size ≤ 4 of the eight plausible top-level block spellings
(`parameters`, `runParameters`, `citedInputs`, `conditions`, `units`, `citedNumbers`, `cheapBound`,
`bounds`), at both depth conventions, returns **no reading** with 49/59, none with zero mixed and a
numeric count above 40, and **none with 1 004 leaves**.

## Why it is worth filing rather than correcting silently

**The `0` was load-bearing for a sentence.** *"Which side a study lands on is a per-study rendering
convention — `T-1c` and `T-1d` interpolate everything into strings, `T-3a` and `T-14` emit
`Double`s"* is an argument from a clean partition, and a clean partition is what `mixed = 0` asserts.
With **14–20 mixed files** the freedom is finer than the challenge said: it is exercised **per
field**, inside one block, by whichever `to`-expression the author happened to write. That makes the
case for `C-0162`'s rule stronger — a per-study convention could at least be audited by naming the
studies, and a per-field one cannot be audited at all.

**And the direction matters for the repair's own scope.** `C-0162` swept on **50**, not 49; had it
inherited the published number it would have been one file short, and `CLAUDE.md` records what a
re-emission sweep that is one file short costs (`CH-0131`: `T-157` stale for six iterations).

## What this does not touch

Nothing in `CH-0207`'s **observation**: `T-3a`'s wall charge is committed at `−0.398665238` against
the `−0.3986652379247042` it solved with, the round trip misses that file's own 2 V force by one
unit in the last emitted place, seven call sites read a parameter block back as an input, and the
contract in `gpd/README.md` was false. `C-0162` reproduces all of it and repairs it. This challenge
is against **one table**, and it is the table nobody re-derived because the sentence around it read
as obviously true.
