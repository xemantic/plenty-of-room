# `T-326` — the two emissions `F9` fired on, retained

`C-0092`'s rule is that a repair must leave the defect measurable.
`F9` — *two independent emissions of `T-326` are not byte-identical, diffed outside the study* — **fired twice**, at exactly one field each time, and both pairs are kept here so the firings stay checkable rather than being replaced by an assertion that they no longer happen.

Both defects are the same one quantity: **the worst departure between two readings that are exactly equal by construction**, which at `1e-17` is pure ulp noise.

| pair | what the field was | run A / E | run B / F |
|---|---|---|---|
| [`run-a-before-the-repair.json`](run-a-before-the-repair.json) / [`run-b-before-the-repair.json`](run-b-before-the-repair.json) | the `F13` prose carried the **spread** of the `gauss6/exact` ratio, a difference of twelve readings that are meant to be one number | `4.2E-13` | `3.2E-13` |
| [`run-e-order-was-not-stable-either.json`](run-e-order-was-not-stable-either.json) / [`run-f-order-was-not-stable-either.json`](run-f-order-was-not-stable-either.json) | `closedForm[*].worstScaledDepartureExponent`, the same noise emitted as an integer **order** rather than as a value | `-18` | `-17` |

Everything else in both pairs is identical: `diff` on either pair returns those fields and nothing else.

**The second pair is the interesting one, and it refines `CLAUDE.md`'s own rule.**
The standing rule is *a quantity that is nothing but ulp noise must be emitted as a **threshold**, never as a value*.
The first repair obeyed the letter of it by emitting an integer **order**, `floor(log10(worst))` — exact, diffable, floor-proof, and **still not stable**, because noise at `4e-17` crosses a decade between runs.
So the rule needs one clause: **an order is not a threshold either.**
What is stable is a **declared** constant plus a boolean — here `T326_CLOSED_FORM_FLOOR = 1e-12`, five orders below `F1`'s own threshold and five above the observed noise, so the emitted field is a *statement the run checks* rather than a *measurement the run makes*.

After the second repair two further independent emissions are **byte-identical**, `cmp` clean, diffed outside the study.
