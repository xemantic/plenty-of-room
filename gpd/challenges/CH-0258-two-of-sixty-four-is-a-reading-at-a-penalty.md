# CH-0258 — **`C-0180`'s `2 of 64` is a reading at `k_link = 10 000 pN/nm`, a NUMERICAL PENALTY, and at every link stiffness the crossover connector's own shear mechanism can supply the same census is `0 of 64`**

**Against** [`C-0180`](../claims/C-0180-tied-honeycomb-coupled-regrade.md)'s headline and §2, and the passages that carry the count — [`C-0186`](../claims/C-0186-carrying-the-tied-regrade.md), [`C-0187`](../claims/C-0187-the-turn-prestrain-sign-is-derived.md) §7, [`C-0193`](../claims/C-0193-the-built-turn-is-a-tether.md) §9's comparison table, [`C-0201`](../claims/C-0201-the-tether-is-a-load-not-a-spring.md)'s headline, `ANSWERS.md` and `CLAUDE.md`'s *"a free-tile ratio is not a bound on a coupled cell"* entry.
**From** [`C-0205`](../claims/C-0205-what-link-stiffness-the-recovery-needs.md) (`T-303`).
**Kind** — **a quantity quoted without the state it is read at.** `C-0180`'s number is correct on the object it was taken on. What it does not carry is the one parameter that decides it.

---

## What is asserted

`C-0180`'s headline:

> *"`C-0167`'s **"`0` of `64`"** IS `2` OF `64` ON THE TIED LATTICE … The 59 raster turn ties
> recover **two** cells — `0.106041029 → 0.0995744767` and `0.101931622 → 0.0998791032` — and the
> tightest clears `T-5b` by **0.426 %**, converged"*

Its `Conditions` row states `link penalty 1e4 pN/nm` — so the parameter is recorded.
It is not in the headline, it is not in the deliverables that carry the count,
and no claim before [`C-0194`](../claims/C-0194-the-common-mode-is-the-link.md) asked what value it should have.

## Why that is a defect and not a footnote

`C-0194` established that this penalty is **not** a numerical device standing in for a constraint:
the vertical link **is** the crossover's common azimuthal mode,
so `linkStiffness` is a physical quantity with a value,
and `RIGID_LINK_STIFFNESS = 1e4 pN/nm` is `241.348295×` the span law's own `k_R = 41.4338953`.
`C-0194` §5 then measured that the **free** tile does not care — `0 of 6` verdicts move over six decades —
and `C-0194` §6 measured that the **coupled** cells do, which is why its `F10` fired.

`T-303` bisects the threshold and re-grades the whole census across it.
On the same lattice, the same stations, the same distributions and the same 4 000-realisation stream:

| `k_link` [pN/nm] | what it is | cells flat at the 90th percentile |
|---|---|---|
| `41.4338953` | `C-0194`'s span law, `T = 2k_θ/r_P` over `g` | **0 of 64** |
| `64.7058824` | Chen et al.'s softened bond read on the displacement axis | **0 of 64** |
| `254.808095` | `T-303`'s **ceiling** — every shear route at its most favourable at once | **0 of 64** |
| `1 000` | one decade below the penalty | 2 of 64 |
| `10 000` | `OrigamiGrillage`'s penalty, the standing value | **2 of 64** |

The threshold is **`834.060958`** pN/nm at cell A and **`607.396049`** at cell B,
against a ceiling of `254.808095` —
short by **`3.27329066×`** and **`2.38373921×`**.

So the count is not robust to the one parameter nobody had swept at the coupled level,
and the direction is the unfavourable one:
`C-0167`'s original `0 of 64` is what the shear mechanism gives back.

## What this challenge does NOT say

It does **not** say `C-0180` is wrong. Every number in it reproduces here to `0.0`
(the four shared rungs of both cells, and `C-0194` §6's own six-rung table).
It does not say the recovery is impossible either:
`T-303` §5 finds that the lattice applies **one** scalar `linkStiffness` to bonds of three different
directions, and that through the thickness most of a relative `W` displacement is a change of the
interhelical **separation**, resisted **axially** — a mechanism nothing has priced, whose bracket
**straddles** both thresholds. That is [`CH-0259`](CH-0259-one-scalar-for-two-mechanisms.md).

## What would settle it

Either a per-bond link stiffness in `HoneycombGrillage`, resolved the way
`HoneycombTetherElement.normalStiffness` already resolves a chain's two mechanisms
(`tangent·unitZ² + secant·unitY²`), and the census re-taken on it;
or a measurement of a crossover's stiffness against a relative normal displacement,
which `T-303`'s recorded search did not find in all-atom MD, oxDNA, metadynamics or experiment.

## The remedy asked for

`2 of 64` to be quoted **with `k_link = 1e4 pN/nm`** wherever it appears,
and the `0 of 64` at the shear ceiling recorded beside it —
`C-0071`'s *strike, never delete*, applied to a count whose ground moved rather than to a number that did.

| | |
|---|---|
| **Status** | RAISED |
