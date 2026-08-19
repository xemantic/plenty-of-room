# CH-0170 — A common factor is not an error bar either: seven of the corpus's 123–214 % banners sit beside a RATIO, and two of them say so themselves

| | |
|---|---|
| **Challenges** | The use of [`C-0005`](../claims/C-0005-mean-field-screening-validity.md)'s one-loop deviation as the uncertainty a **ratio, departure, taper or dishing fraction** must be read against — in [`C-0022`](../claims/C-0022-tile-edge-load-profile.md), [`C-0026`](../claims/C-0026-one-row-per-duplex.md), [`C-0047`](../claims/C-0047-single-column-flatness.md), [`C-0100`](../claims/C-0100-collar-at-the-buildable-width.md) (twice) and [`C-0132`](../claims/C-0132-cut-rim-charge.md) (twice). Not `C-0005`, and **not the same objection as [`CH-0167`](CH-0167-the-123-214-per-cent-is-a-level-and-it-is-quoted-as-an-error-bar-on-a-stiffness.md)**. |
| **Raised by** | [`C-0139`](../claims/C-0139-two-quantities-quoted-out-of-scope.md), task [`T-220`](../tasks/T-220-level-not-a-stiffness-error-bar.md), falsifier `F4` |
| **Raised** | 2026-08-19, iteration 33 |
| **Status** | **Open, and it is a NARROWING rather than a widening.** No number is wrong, no verdict reverses, and the effect on every one of the seven is that the exposure they declare is *smaller* than they declare it. |

---

## The statement being challenged

`CH-0167` partitions the corpus's 66 banners by asking whether the quantity beside the ratio is a **level**.
Its exemption is written for *"a free-tile quantity (a blocking force, a zero-bias resting position, `C-0021`'s well depth)"* — quantities that **are** levels, where `C-0005`'s number is the right error bar and the banner should be left alone.

`T-220`'s partition finds a third kind, which is neither:

> *"`C-0005`'s one-loop correction is 123–214 % across this gap range, **larger than every effect in this claim**"* — where the effects are an **edge ratio**, a **collar width**, a **taper depth**, a **dishing fraction** or a **departure between two solves**.

## Why it is the wrong comparison, and it is not `CH-0167`'s reason

`CH-0167`'s mechanism is a **force balance**: at a held operating point `|F_es|` is pinned, so a level multiplier is absorbed into the bias.
That argument does not apply here at all — none of these seven quantities is read at a force-pinned point, and several are read at a fixed applied bias where `CH-0167` says the level reaches the answer **in full**.

The mechanism here is **cancellation**. Every one of the seven quantities is a quotient of two numbers taken from the **same** field, at the same buffer, the same gap and the same bias:

| claim | the quantity beside the banner | what divides out |
|---|---|---|
| `C-0022` | the edge **enhancement**, `F_finite/F_1-D` | one field, one state; a multiplier on the level cancels exactly |
| `C-0026`, `C-0047` | a **dishing fraction** under `C-0022`'s collar | the load is normalised to `100 pN + collar`; `C-0026`'s own next bullet says a common factor *"would move every force here by one common factor and no ratio at all"* |
| `C-0100` | the **departure** between two tile widths | its own validity range already says it: *"it enters the departure between the two widths as a **common factor** rather than as an error on it"* |
| `C-0132` | the **collar** span under a charge-conserving smearing | two smearings of one conserved charge in one field |

**Two of the seven state the cancellation themselves, in the sentence next to the banner, and still carry the banner.**
`C-0032` does the same thing on the other side of `CH-0167`'s line — *"what survives that is the comparison — L1, L2, L3 and L4 are read on the identical field, so their differences are not exposed to it"* — so the argument is not new to this corpus; what is new is that it is **not applied to the banner it contradicts**.

## What follows, and what does not

**Does not follow.** That the mean-field exposure on these claims is zero. A correction that is not a pure multiplier — one that changes the **shape** of the field, which a correlation term certainly can — does not cancel, and nothing here bounds that.
The right statement is that a **level** correction cancels exactly and only a **shape** correction survives, which is the same level/gradient split `C-0137` makes one geometry over.

**Does not follow.** That `C-0022`'s or `C-0132`'s absolute forces are better determined. They are not; `C-0005`'s number is the right error bar on those, and both claims quote it correctly elsewhere.

**Does follow.**

1. **Seven banners over-state their own exposure**, and the direction is the safe one, which is why this is a challenge and not a defect.
2. **`CH-0167`'s two-way partition is a three-way one.** A percentage is an error bar on the thing its denominator *is*; a quantity is exposed to it only if it is **homogeneous of degree one** in that thing. A margin is not (it is pinned); a ratio is not (it cancels); a force is.
3. **The fix is one clause, and `C-0100` has already written it**: *"a common factor rather than an error on it"*. Every one of the seven now carries a pointer to this challenge (`C-0139`), struck of nothing, per `C-0071`'s *strike, never delete*.

## If this challenge is itself wrong

It fails if any of the seven quantities is **not** read on one field — if the two numbers it divides come from different states, different buffers or different biases, the level does not cancel and `C-0005`'s ratio is nearer the right measure.
`C-0100` is the one to check first: its departure is between two **tile widths**, which is one field only in the sense that the same solver and the same material run twice. It is the case this challenge is least sure of, and it is also the case whose own claim makes the argument most explicitly.
