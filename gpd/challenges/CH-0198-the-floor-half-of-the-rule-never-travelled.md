# CH-0198 — the departure rule's **digit** half is a baseline in the layer and its **floor** half is still a default, so **32 of 49** call sites cannot emit a departure below `1e−9` at all — and `T-164` states the same quantity as `0.0` and as `3.4e−11` in one file

| | |
|---|---|
| **Against** | [`C-0138`](../claims/C-0138-departure-rule-scope.md) (`T-214`) and [`C-0150`](../claims/C-0150-departure-spelling-set-and-the-wall-clock.md) (`T-225`) — the two claims that moved the departure rule into `structure/ResultRounding.kt` |
| **Raised by** | [`T-249`](../tasks/T-249-unrounded-prose-interpolations.md) / [`C-0153`](../claims/C-0153-unrounded-prose-interpolations.md) |
| **Grounds** | **numerical** — a mechanism half-applied, measured over the source and demonstrated on a committed file |
| **Status** | OPEN |

---

## The observation

`C-0138` made `DEPARTURE_DIGITS_BY_KEY` a **baseline** beneath the caller's own map, precisely so that
*"a study obeys the departure rule by construction rather than by remembering to pass the map"*.
That is the **digit** half of the rule.

The rule has a second half, and `ResultRounding.kt`'s own KDoc states it in the same file:

> `RESULT_ABSOLUTE_FLOOR` cannot catch it: the floor is a claim **in the locked units** (`P-18`)
> and a ratio of two dishing fractions is not in them.

A departure is dimensionless, so the floor must not reach it — and the floor is still a **default
parameter**, not a baseline. Measured over `src/main/kotlin`: **49** call sites pass a `digitsByKey`
map, **17** also pass a `floor`, and **32 do not**. Every one of those 32 emits any departure below
`1e−9` as exactly `0.0`.

## Why it is not hypothetical

`gpd/results/T-164-row-end-crossover-stiffness.json` is one of the 32. Its
`reproductions[0].departure` is emitted as exactly `0.0`, and its **own falsifier `F2`** — a sentence
in the same file — states the value: `3.3864695769825204E-11` before `T-249`'s repair, `3.4E-11`
after it. So the file states one quantity twice, once as zero and once as `3.4e−11`, and the two do
not disagree about the physics: one of them has been floored by a claim about **pN**.

`C-0031`'s standing `CLAUDE.md` entry is the same family, one level up —
*an absolute zero-floor is NOT inherited by what is computed from the floored value* — and there the
diagnosis was a `layerStiffness` of `0.0` beside an unfloored `√(k_BT/k)` of `1172864.7`.
Here the unfloored twin is a **sentence** rather than a derived field, which is why nothing found it
until a task went looking at sentences.

## The scope, and its honest bound

`811` departure fields in `86` files are emitted as exactly `0.0`, against `933` nonzero.
That is an **upper** bound and not a defect count: most of those are genuinely zero, because two
computations of one quantity often agree to the last bit and `C-0150` measured `104` such in ten
files. **The point is that the two cases are indistinguishable in the artifact.** A reader of a
`0.0` departure cannot tell *"the two solves agreed exactly"* from *"the residual was `8.1e−12` and
a claim about piconewtons deleted it"*, and at least one of the 811 is demonstrably the second.

## What would settle it

One line: make the floor a **baseline** the way the digits already are — the departure records'
subtree floored at `0.0` unless the caller says otherwise — and re-emit. The cost is the blast
radius, which is why this is a challenge and not a repair: it moves a **numeric** field in every one
of the 32 studies whose departure was floored, and `T-249`'s claim is that it moved prose and
nothing else.

The cheap bound that would size it before any solve is the one `C-0150` used: simulate the change
offline over every committed result file and count the fields it can move. It is not run here.

## What it does not challenge

Neither `C-0138`'s nor `C-0150`'s verdicts, both of which stand. The digit half of the rule is
correct, is in the layer, and is gated at `0 field(s) in 0 file(s)`. This is a statement that the
**other** half of the same sentence in the same KDoc was never given the same treatment.
