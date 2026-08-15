# CH-0085 — `0.260150` is exactly a tie at four significant figures, so *"0.2601"* is the side of it one run happened to land on

| | |
|---|---|
| **Against** | [`C-0016`](../claims/C-0016-design-window.md) and [`C-0011`](../claims/C-0011-scf-density-profile.md) — their **four-figure rendering** of the 10 nm upper window edge, not any number either claim derives |
| **Raised by** | [`C-0073`](../claims/C-0073-determined-precision-of-a-result-file.md) / [`P-18`](../tasks/P-18.md) |
| **Grounds** | **methodological.** A quantity quoted at four significant figures whose fifth figure is exactly `5` is not determined at four significant figures; which way it renders is decided by digits the file has no business printing |
| **Status** | **UPHELD, and no number of either claim fails.** Every verdict, every window width, every constraint attribution and every five- and six-figure quotation stands unchanged |

---

## What the claims say

`C-0016` quotes the 10 nm window at both precisions, on different lines of the same claim:

> *"`σ ∈ [0.0116, **0.2601**] nm⁻²` at 10 nm"* (§ *The claim, in one line*)
> *"**`[0.01163, 0.26015]`**, **22.36×**"* (the window table)

`C-0011` quotes the four-figure form five times — `[0.0116, 0.2601]` — and `C-0019`, `C-0027` and
`C-0051` quote the five- and six-figure forms, `0.26015` and `0.260150`.

## The challenge

**`0.260150` is a tie at four significant figures.** Rounding it there is `0.2601` or `0.2602`
depending on the tie rule, and the rule is not stated anywhere; worse, which side the *file* lands
on is decided by the sixth and seventh figures, which `P-18` has just established are not
determined.

`P-18` made this visible without touching any physics. `T-2` reads `T-1d`'s result file and builds
its grafting-density grid out of it, so carrying `T-1d`'s emission down from nine significant
digits to the six its solver determines moved the grid — and with it the edge — by `1.5e−6`:

| | before `P-18` | after `P-18` |
|---|---|---|
| `heightWindows[2]/predicateOneHighest` | `0.260149602` | `0.26015` |
| the findings string, at four figures | *"0.01163-**0.2601** nm⁻²"* | *"0.01163-**0.2602** nm⁻²"* |
| window width | `22.3606798` → *"22.36x"* | `22.3607983` → *"22.36x"* |
| the binding constraint | coil overlap `Σ ≥ 1` / 3 nm stroke at 100 pN | **unchanged** |
| `5 nm` | empty | **unchanged** |

A movement of `1.5e−6` flipped a *printed* fourth figure by one unit, because the value sits on the
knife edge. Nothing else in 4 864 moved fields flipped anything: no boolean, no structural entry, no
five- or six-figure quotation.

## What this challenge does and does not do

**Does not touch a number.** `0.26015` is what `C-0016`'s own window table, `C-0027`, `C-0051` and
`C-0019` all quote, and it is exactly what the file now carries. The width stays `22.36×`, the
lower edge stays `0.011634`, `C-0027`'s `+1, upper` grid step to `0.288540` is untouched, and
`C-0016`'s verdict — the 10 nm window is non-empty and 22.36× wide — is unaffected at every digit
it is stated to.

**Does replace a rendering.** *"0.2601"* should be read as *"0.26015 truncated"*, not as a
four-figure result. Where a claim wants four figures for readability it should say `0.260`, which is
not a tie, or keep the five-figure `0.26015`, which is what the quantity actually is.

## The general lesson

> **A tie is not a rounding artefact, it is a statement that the quoted precision is the wrong
> one.** If the next digit is a `5`, the reader cannot recover the value and the writer has not
> chosen a precision — the renderer has. Check a headline number against its own tie before
> quoting it.

It is the same shape as `CH-0043` one level out: `CH-0043` found a *file* printing more digits than
its solver determined; this finds a *claim* quoting a number at a precision at which it is not
determined either, and the two were invisible to each other because the file's extra digits
happened to break the tie the same way every run.

## Recommendation

1. **No re-derivation.** Both claims' numbers stand as published.
2. **`T-2`'s file is kept as re-emitted**, on `C-0031`'s precedent — the tree records what the
   current code emits.
3. **Quote the 10 nm upper edge as `0.26015` or `0.260`, never `0.2601`.** Recorded in `CLAUDE.md`
   as a general rule rather than as a fact about this edge.
