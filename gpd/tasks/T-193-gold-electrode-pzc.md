# T-193 — Template-stripped gold: the hold-down at the answered material, and where its potential of zero charge sits

| | |
|---|---|
| **Leaf** | `A1.2` (the 3.0 nm positional bound, read at zero bias), inheriting [`C-0021`](../claims/C-0021-zero-bias-resting-position.md) whole |
| **Problem definition** | §1 (*"patterned electrode"*, which never says of what); §3 (buffer, heights, bias); §4(a) |
| **Verification type** | **logical** (the identification of the gap model's *"applied bias"* with the rational potential `E − E_pzc`, which is a reading of the code and not a calculation) **+ in-silico** (`C-0021`'s van der Waals term and `C-0008`'s gap field re-run at gold alone, the field parametrised by the diffuse drop rather than by the bias) **+ literature** (a primary, open-access, directly-read measurement of `E_pzc` for Au(111) in a mM aqueous electrolyte) |
| **Maturity** | TRL 1–3. Model-consistent and traceable. **Nothing here is measured by this project**; the `E_pzc` is somebody else's measurement, on a different surface preparation in a different electrolyte. |
| **Status** | Executed, verified, filed as claim [`C-0111`](../claims/C-0111-gold-electrode-pzc.md) |
| **Consumes** | [`C-0021`](../claims/C-0021-zero-bias-resting-position.md) (`M3`, `M4`, the thermal scale, the whole field library), [`C-0008`](../claims/C-0008-electrostatic-force-and-decay-length.md) (the gap solve and the Stern series), [`C-0005`](../claims/C-0005-mean-field-screening-validity.md) (the point-ion boundary, on **both** signs) |
| **Raises** | nothing — no standing claim is contradicted; `C-0021`'s second open question is **answered**, and the residue is one specification question for NDI |

---

## Formulate

### Why this task exists now

NDI answered decision 3 on **2026-08-18**: *"Defaulting to template stripped gold for initial experiments."*

`C-0021` priced the zero-bias hold-down over **four** candidate electrode materials and found that the specification gap
— §1 says *"patterned electrode"* and never says of what —
was worth **2.6×**, larger than the DNA Hamaker bracket (1.17×), larger than retardation, larger than the polymer in the gap.
That gap is now closed on one axis and still open on another,
and the two halves have very different weights.

**Half 1 is bookkeeping.** Gold is one of `C-0021`'s own four rows.
Reading `M4` at gold alone is a re-read of a table that exists,
and the only questions are what the bracket collapse is worth, which end it collapses onto, and whether any verdict moves.

**Half 2 is the load-bearing half.** `C-0021`'s `M3` closes on a threshold rather than a value:

> *"A contact potential of a few millivolts — below anything a bench would call zero — supplies the entire hold-down. The zero-bias resting position is therefore set by a quantity the problem definition does not contain."*

and it files that as its second open question, marked **a MEASUREMENT, not a calculation**.
Template-stripped gold narrows where to look:
it is a (111)-dominated surface with a large single-crystal electrochemistry literature behind it.

### The numeric targets

1. **The gold-alone reading of `M4`**: force, well depth and negative stiffness at 5 / 7 / 10 nm on both readings of §3's tile thickness, with the four-material bracket beside it and the narrowing measured.
2. **`E_pzc`** of template-stripped / annealed Au(111) in a mM-ionic-strength aqueous electrolyte, **with its reference electrode and its conditions**, or a query-recorded absence.
3. **The rational potential the device sits at** if its electrode is held at zero volt on a common aqueous scale, expressed as a multiple of `C-0021`'s own thermal-scale threshold, **with its sign**.

### The acceptance predicate

**PASS** if all four hold:

- (a) the gold-alone re-read **reproduces** `C-0021`'s published gold rows to the emitted precision;
- (b) whether any verdict of `C-0021` moves is **verified rather than repeated**, and any *ground* that moved is named even where the verdict did not;
- (c) a primary `E_pzc` is obtained **with its reference electrode and conditions**, read directly, or a negative existence result is filed with its query strings and its expected yield;
- (d) where a number cannot be sourced, the **threshold** it would have to cross for the answer to change is stated instead of a guess.

### Conventions, fixed before deriving

- A force is **positive down** in the hold-down tables (`T-13`'s convention); the solved field's `forceOnTile` is **negative toward the electrode**. Both are emitted.
- The **rational potential** is `E − E_σ=0`, positive when the electrode carries positive free charge.
- `E_pzc` travels with its reference electrode. Where a source prints two scales, both are carried, because the pair is a transcription check.

---

## Plan

### The cheap bound first, and here it is not a bound but an identification

Before any solve: **what is the model's `appliedBias`?**

`diffusePotentialOfAppliedBias` solves `V = ψ_d + σ_e(ψ_d)/C_S`.
With no tile present, `V = 0` makes the whole interfacial drop vanish,
which is the definition of an electrode carrying no free charge.
So the model's *"applied bias"* is the **rational potential**, not a potentiostat setting,
and `C-0021`'s contact-potential table is a table of rational potentials.

That is a reading of forty lines of existing code, it costs nothing, and it decides the whole shape of the answer:
the missing quantity is an `E_pzc`, and the answer to *"what contact potential does the device sit at"*
is `0 − E_pzc` on whatever scale the drive defines its zero.

### Method

1. **Half 1** re-runs `C-0021`'s own van der Waals library at gold, beside the four-material span, and asserts the reproduction.
2. **Half 2** searches the literature for `E_pzc`, checking `gpd/data/` first (`CLAUDE.md`: it has paid three times), then EuropePMC with recorded queries.
3. The field is re-read **parametrised by the diffuse drop**, per `CLAUDE.md`'s own rule — one solve gives the force *and* the bias that produced it, where the inverse costs ~34 — and the three sign landmarks (thermal-scale lift, no net force, thermal-scale hold-down) are bisected on `ψ_d` inside a bracket the ladder certifies. `C-0021`'s three published thresholds are reproduced by that opposite route, which is a genuinely independent check rather than a restatement.

### What would falsify this approach

- **If the model's `appliedBias` were a potential against an external reference**, the whole identification collapses and the PZC is irrelevant. Falsified by reading `diffusePotentialOfAppliedBias`; it is not.
- **If a published `E_pzc` for gold in a mM electrolyte came out within a few millivolts of any common reference zero**, the residual field would be genuinely negligible and `C-0021`'s `M3` row would stand unqualified. It does not: it is two orders of magnitude away.
- **If the ψ_d-parametrised route failed to reproduce `C-0021`'s bias-bisected thresholds**, one of the two is wrong and no conclusion is available from either.
- **If the rational potential the PZC implies fell inside `C-0005`'s point-ion boundary**, a force could be quoted there and the answer would be a number rather than a threshold. It does not, on the tighter of the two boundaries — which is the `Mg²⁺`-at-a-negative-electrode one, 0.097 V, and the PZC puts the electrode on exactly that side.

### Cost justification

The whole task is one literature pass and ~120 gap solves at `C-0021`'s own node counts.
No new physics is built, and deliberately so: the question is whether a *specification* answer moves an existing result,
and the cheapest honest way to answer that is to re-run the existing result at the answered specification.
