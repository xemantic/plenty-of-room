# T-220 — Restate the 123–214 % qualifier wherever it is attached to a HELD quantity

| | |
|---|---|
| **Leaf** | `A7.4` (the beyond-mean-field exposure), consumed by `A2.2` and `A8.2` |
| **Raised by** | [`CH-0167`](../challenges/CH-0167-the-123-214-per-cent-is-a-level-and-it-is-quoted-as-an-error-bar-on-a-stiffness.md), from [`C-0137`](../claims/C-0137-beyond-mean-field-gap.md) (`T-50`) |
| **Verification type** | **logical** — a corpus census, a per-occurrence partition against a stated predicate, and a retained mechanical checker |
| **Units** | none of this task's own; the quantities it partitions are forces (pN), stiffnesses (pN/nm), decay lengths (nm) and dimensionless margins |

## Formulate

`C-0005` computes `Ξ|P⁽¹⁾|/P_PB` — the ratio of the one-loop correction to the leading term of a **pressure** — and reports 123–214 % over 5–20 nm.
That is an error bar on the **LEVEL** of the electrostatic force.

The corpus quotes it 66 times in 31 claims, plus 24 times across `ANSWERS.md`, `TASKS.md` and `DECISIONS-FOR-NDI.md`,
as the uncertainty a **stability margin**, a **fold margin** or a **window edge** must be read against.
`C-0137` measures that those quantities are read at a **force-pinned** operating point, where `|F_es| = 100 pN + P(g)A` is fixed by a mechanical balance
and `k_es = −|F_es|/ℓ` identically — so a multiplier on the level is absorbed into the bias and does not enter the margin as a multiplier at all.

### Acceptance predicate

Every occurrence of the 123–214 % attached to a **held**, load-line quantity is either

- **RESTATED** with `C-0137`'s own thresholds — *a force `1.48–2.22×` smaller, or a decay length `9.73 %` shorter* — which are quantities of the same kind as the margin; or
- **STRUCK with a pointer** to `CH-0167`/`C-0137`, per `C-0071`'s *strike, never delete*;

and every occurrence attached to a **free-tile** quantity (a blocking force, a zero-bias resting position, a well depth, a bare force level, or a statement *about* `C-0005` itself) is **LEFT**, with the reason recorded.

`P1` The census reproduces mechanically: 66 occurrences in 31 claims.
`P2` Every occurrence is partitioned HELD / FREE / META by a stated, retained predicate, and the partition is emitted per occurrence.
`P3` No occurrence classified HELD is left unaddressed in the four documents a reader outside the programme sees.
`P4` No number is deleted; every withdrawn sentence survives struck.
`P5` `tools/trace-answers.py` is clean on **both** deliverables after the edits, and `tools/check-corpus-links.py` / `check-markdown-tables.py` pass.
`P6` The **ground** of every verdict whose qualifier is restated is re-checked, and any verdict now surviving on a different ground is said so explicitly.

## Plan

**Cheap bound first, and it is the whole method.** The census is a `grep`; the partition is the expensive part and it is a *reading*, not a solve.
No solver runs in this task at all — `C-0137` already supplies the replacement thresholds and this task spends nothing re-deriving them.

1. Reproduce the census mechanically (`tools/T-220-census.py`), emitting one record per occurrence with its file, line, surrounding sentence and classification.
2. Classify with a stated predicate, applied to the **quantity the qualifier is attached to** and not to the claim's subject:
   - **HELD** — a stability floor, a coupling margin, a pull-in/fold margin, a window edge built on one, or any margin read at the biased operating point `C-0017` defines.
   - **FREE** — a blocking force, a zero-bias hold-down, a well depth, a force level or ratio, or a statement about the *field* rather than about a margin.
   - **META** — a sentence *about* `C-0005`, `CH-0019`, `T-50` or `CH-0167`, including the corrections iteration 32 already filed.
3. Edit the four external documents and `C-0017` first (the challenge's own honest minimum), then sweep the claims.
4. Re-check the ground of each restated verdict.

### What would falsify this approach

- `F1` — the census does not reproduce at 66/31, meaning the challenge counted something else.
- `F2` — the partition is not decidable from the sentence: an occurrence that is neither clearly held nor clearly free.
- `F3` — a restatement moves a verdict. `C-0137` says the margins survive; if restating one loses it, the challenge is bigger than a scope repair.
- `F4` — a FREE occurrence turns out to be quoting the level against a quantity that is not a level either, so `C-0005`'s number is the wrong error bar there too, for a different reason.
