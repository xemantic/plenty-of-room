# T-329 — `F9`'s and `F10`'s identity residuals emitted as a **threshold and a boolean**, not as numbers whose every digit is noise

**Leaf:** `A8.2`
**Raised by:** [`C-0216`](../claims/C-0216-the-placement-and-the-distribution-together.md) (`T-323`) §14(b).
**Companion row:** [`T-328`](T-328-the-cure-at-every-call-site.md), which carries the shared Formulate, Plan, Execute and Verify —
the two rows are one repair and one re-emission, and are filed under one claim,
[`C-0217`](../claims/C-0217-the-cure-at-every-call-site.md).

---

## Formulate

### The standing state

`T-323`'s `F9` asserts that the **bank slice** — the 55-station influence bank restricted to a
placement's indices — equals a surrogate built on that placement alone, to `1e−10` relative. `F10`
asserts that the surrogate at full presence reproduces the **assembled** solve with its own Woodbury
support forces, to `1e−9`. Both identities hold. Both are the identities the whole method rests on.

Both **residuals** were emitted as numbers, and their true value is **zero**:

| | run A | the committed run B |
|---|---|---|
| `F9`'s note | `departure 9.6E-16` | `departure 3.8E-16` |
| `F10`'s note | `departure 2.0E-14` | `departure 3.9E-14` |

`CLAUDE.md` states the rule and the reason: *a quantity that is nothing but that ulp noise must be
emitted as a THRESHOLD, never as a value — rounding cannot save it*, because **one such field makes
a whole result file permanently un-diffable, which is the check the rounding layer exists to
enable**. These two were `4` of the `26` moved leaves at `T-323`'s first emission, and unlike the
argmins they carry no information at all: the same identity, holding, twice.

### What is in scope, and one thing that is in scope and was not in the row

The row names `F9`'s and `F10`'s notes. Re-reading the source found a **third** channel for the same
residual: the bank-slice identity was **also** emitted as a `convergence` record, whose `departure`
field is the same noise and whose `fine` field is `coarse × (1 + departure)` — i.e. *synthesised
from the residual*. It survived `T-323`'s diff only because `DEPARTURE_DIGITS_BY_KEY` floors a
`convergence/departure` to `0.0` at that magnitude. That is stability **by accident of a floor**,
and the record was never a convergence axis in the first place: it has no coarse/fine pair. It is
folded into the same repair and the fold is stated rather than glossed.

### Numeric targets

| # | target |
|---|---|
| `Q1` | one rule, `identityHolds(residual, tolerance)`, in the model source, with named tests in **both** directions and a guard on the tolerance |
| `Q2` | `F9` and `F10` reporting the **declared tolerance** and the **boolean**, and not the residual |
| `Q3` | a machine-readable `identities` record carrying `what`, `quantity`, `tolerance`, `holds`, `note` — the field names taken from `T-267`'s own `identities` block rather than coined (`C-0198`: *a new schema key's name is a census over the corpus*) |
| `Q4` | the bank-slice `convergence` row removed, with its ground stated: coarse and fine are one number by construction |

### Acceptance predicate

The task passes when `Q1`–`Q4` are discharged, no residual whose true value is zero is printable
anywhere in the emitted file, and the two identities are still **asserted** — a threshold and a
boolean are a weaker *report* and exactly as strong a *test*, because `F9` and `F10` were always
declared on the threshold and never on the value.

---

## Plan

**The cheap bound is the census of the key name, and it ran before a line was written.**
`identities` occurs as a top-level key in **1** of 192 committed result files
(`T-267-mechanics-on-imported-design.json`) and nowhere else; `tolerance` occurs as a leaf 25 times
and `holds` 11. So the name is not new, it is **reused**, and the field names are that file's own.

**The rule goes in the model, not at the two call sites**, for the reason `CLAUDE.md` gives: a
duplicated rule is invisible to a mutation test of either copy, and `F9` and `F10` are two copies of
one sentence.

**Cost:** compile-time. The measurement rides on `T-328`'s two runs; there is no separate run.

### What would falsify this approach

- **If the two identities stop being asserted**, the repair has traded a reproducibility defect for
  a weaker gate, and that is not a trade this row is allowed to make.
- **If a `holds` field can be `true` for a residual that is not finite**, the report fails in the
  flattering direction.
- **If removing the bank-slice `convergence` row moves anything else in the file**, the row was
  load-bearing and the fold was wrong.

### Declared falsifiers

| id | fires if | verdict |
|---|---|---|
| `Q-F1` | any residual whose true value is zero is still printable in the emitted file after the repair | must not fire |
| `Q-F2` | either identity fails at its declared tolerance | must not fire — they are `F9` and `F10`, unchanged in substance |
| `Q-F3` | `identityHolds` reports `true` for a non-finite residual, or accepts a non-positive tolerance | must not fire |
| `Q-F4` | removing the bank-slice `convergence` row changes any other field of the file | must not fire |

---

## Execute and Verify

Executed with [`T-328`](T-328-the-cure-at-every-call-site.md) in one commit, on the same two runs,
and recorded in [`C-0217`](../claims/C-0217-the-cure-at-every-call-site.md) §3 and §5–§6.

`Q1`–`Q4` are all discharged, and `Q4` grew: re-reading the source found a **third** copy of the
bank-slice residual, emitted as a `convergence` record whose `departure` is the same noise and whose
`fine` was `coarse × (1 + departure)` — synthesised from it. It was stable only because
`DEPARTURE_DIGITS_BY_KEY` floors that key to `0.0` at `1e−16`, and it was never a convergence axis:
it has no coarse/fine pair, and `C-0216`'s own gate-4 row already said *"**nine** axes"* and listed
nine. Removing it makes the array agree with the count its owner published.

`Q-F1`–`Q-F4` did **not** fire. The emitted file now carries

```
"identities": [ { "what": "...(F9)", "tolerance": 1e-10, "holds": true, ... },
                { "what": "...(F10)", "tolerance": 1e-09, "holds": true, ... } ]
```

and `F9`/`F10`'s notes read *"the identity HOLDS to `1.0E-10`"* / *"to `1.0E-9`"* with no residual in
them. The rule is one function, `identityHolds(residual, tolerance)`, held open by three mutations
(`M32`–`M34`: the `abs`, the strict `<`, and the tolerance guard), each failing a **named** test.

**Measured effect on `F23`:** of the 26 leaves that moved before, **2** were these two sentences —
`falsifiers/8/note` and `falsifiers/9/note` — and neither can move again, because neither carries a
number any more. They are 2 of the 22 that stopped moving.
