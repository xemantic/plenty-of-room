# CH-0012 — `C-0007`'s grafted-`χ` of 0.60, and its 239×, are constructions the source forbids

| | |
|---|---|
| **Challenges** | [`C-0007`](../claims/C-0007-solvent-quality-vs-salt.md) — the row *"`Δχ` between a bulk chain and a densely **grafted** one … `χ ≈ 0.60` vs `0.46` … **239×**"*, and still-open item 3 |
| **Raised by** | [`C-0013`](../claims/C-0013-grafted-chi-inapplicable.md), task [`P-9`](../tasks/P-9-grafted-chi.md) |
| **Raised** | 2026-08-13, iteration 4 |
| **Status** | **UPHELD in its warning, OVERTURNED in its number.** `C-0007`'s conclusion — that a bulk `χ` is not automatically a brush `χ`, and that this is the largest un-discharged premise in the material sheet — was **right, and raising it was right**. Every number attached to it is wrong, and the premise is now discharged rather than open. |

---

## Why this is filed at all, given that raising the question was correct

Because `C-0007` said what it was doing, and said it accurately:

> **The grafted-`χ` number is quoted from a verbatim abstract**, not from a passage in the body,
> and is used only as a flag, never as an input.

That was the right way to carry it, and the flag did its job: it produced `P-9`, `P-9` read the body,
and the body says something different. What has to be filed is that the number then **escaped the
flag** — it reached `TASKS.md`'s standing findings, `C-0002`'s "still open", and `C-0003`'s validity
range, in each case as *"`χ(brush) ≈ 0.60`"* without the qualifier travelling with it. A number that
was explicitly not an input has been quoted four times as though it were one. That is the failure
mode this repository files challenges to catch.

---

## The standing statements being challenged

`C-0007`, "Why the answer does not matter as much as two things it uncovered":

> | **`Δχ` between a bulk chain and a densely *grafted* one** — Lee et al. report
> | `χ(brush)/χ(θ) ≈ 1.2` against `≈ 0.92` for free chains, i.e. **`χ ≈ 0.60` vs `0.46`**, from an
> | SCF fit to neutron reflectivity | **239×** |
>
> **`χ ≈ 0.60` is on the far side of θ: negative excluded volume, poor solvent, for the one
> configuration this whole project is about.**

and still-open item 3:

> **The grafted `χ`.** `χ(brush) ≈ 0.60` against `χ(bulk) = 0.372` — 239× the entire buffer effect
> and not incorporated. Queued as `P-9`. **This is the largest open premise in the material sheet.**

---

## The four contradicting results

### 1. `0.60` is not in the paper. The fitted values are `0.789` and `0.852`.

Lee, Kim, Witte, Ohn, Choi, Akgun, Satija & Won, *J. Phys. Chem. B* **116**:7367–7378 (2012), §3.3,
read from the PDF:

> *"The best-fit `χ_PEO−water` values … were found to be **0.789 ± 0.066** (at `α = 1350 Å²/chain`)
> and **0.852 ± 0.051** (at `α = 2200 Å²/chain`)"*

The string `0.60` does not occur as a `χ` anywhere in the paper. It is `1.2 × ½`, computed from the
abstract's ratio.

### 2. That multiplication is exactly the step the paper forbids, in its own words

The `1.2` is a ratio to the theta point **of the SCF model**, and that model's theta point is at
`χ = 0.696`, not `½`. The authors located it themselves, by finding which `χ` reproduces Gaussian
end-to-end statistics for a **free** chain in the same model:

> *"the SCF prediction for `χ_PEO−water = 0.50` does not match the Gaussian behavior; the Gaussian
> statistics was precisely reproduced when `χ_PEO−water` is set to about **0.696**."*

and then say what follows:

> *"**the physical assumptions used in the present SCF model are not identical to those of the
> Flory−Huggins theory from which the `χ_P−S = 1/2` criterion … has been originally derived.** For
> instance, in our SCF model we used a monomer volume that is different from the value of the
> solvent volume used (i.e., `v_PEO = 59.2 Å³` and `v_water = 29.9 Å³`). **For this reason alone,
> simply setting the `χ_PEO−water` value to 0.5 in our model, for example, would not be able to
> produce results that precisely correspond to the behavior under the so-called θ [condition]."***

`0.852/0.696 = 1.224` is the paper's `≈ 1.2`. `1.224 × ½ = 0.612` is `C-0007`'s `0.60`.
**The disclaimer is on the same page as the number.**

This is not a new class of error for this project. It is **`C-0007`'s own lattice trap**, which that
claim names better than anyone:

> **`χ` lives on a lattice, and the lattice site is not always the monomer.**

`C-0007` disarmed it for the *bulk* `χ` it measured and walked into it for the *brush* `χ` it quoted.
And the confirmation is quantitative: Lee et al.'s model carries `v_PEO/v_water = 59.2/29.9 =
**1.980**`, against the `v₀/v_site = **2.010**` that `C-0007` derives independently — **1.5 % apart**.
The same factor of two, inside a second model.

### 3. There are two transfers, they disagree by 0.089, and neither is licensed

Preserving the **ratio** to theta gives `0.567` and `0.612`. Preserving the **distance** past theta
gives `0.593` and `0.656`. Both are defensible a priori; the source licenses neither. The spread is
**0.089 — 37 % of the 0.240 shift the ratio transfer claims**. A quantity whose value on the target
axis depends by 37 % on an arbitrary choice of map is not a number that can be compared against
anything, let alone multiplied by 239.

### 4. The `239×` therefore has no denominator-independent meaning, and the effect is 4.6× smaller

`239×` is `0.228 / 0.00095` — the ratio-transfer shift over the buffer step. Both factors are
questionable and the numerator is the worse of the two. Measured independently in the **right**
geometry (Hansen et al. 2003, normal osmotic-stress compression of PEG-grafted bilayers, at
**1.5–2.5× the Gen-1 grafting density**), the brush-versus-bulk shift is

&nbsp;&nbsp;&nbsp;&nbsp;**`χ_eff = 0.346 – 0.424` against a bulk `0.372`, i.e. `|Δχ| ≤ 0.053`**

which is **4.6× smaller** than claimed and **straddles zero**. The honest ratio to the buffer step is
therefore at most ~55×, not 239× — and unlike 239× it is a bound rather than a point value.

---

## One thing `C-0007` got right that this challenge does not touch

`C-0007` also wrote:

> Note also that the `0.92` in that same sentence, giving `χ ≈ 0.46` for free chains, is the most
> likely origin of the folkloric *"χ ≈ 0.45"*.

**That inference is corroborated by the body**, and from a better direction than `C-0007` had. Lee
et al. cite, for bulk PEO in water, *"≈ 0.45 at 20 °C"* — a literature value they take from Venohr,
Fraaije, Strunk & Borchard, and the `0.92` of the abstract is `0.46/0.5` in the ordinary
Flory-Huggins convention. So the abstract's two ratios are computed in **two different conventions**
in one sentence: `0.92` against the Flory-Huggins `½`, and `1.2` against the model's `0.696`. That is
the trap in its purest form, and it explains both the folkloric `0.45` and how `0.60` got made.

---

## What changes

**In `C-0007`:** the "239×" row and still-open item 3 are annotated in place with a pointer here.
No number in `C-0007`'s own parameter table, transfer function, ion-channel cancellation, salt
ceiling or threshold moves — **the entire result of `P-6` stands**. What moves is the comparison
`C-0007` put beside it, and the status of the open item, which is now **closed** by `C-0013`.

**In `C-0002` and `C-0003`:** both quote `≈ 0.60` from `C-0007` while stating the exposure rather
than absorbing it, which was the correct handling. `C-0003`'s validity-range paragraph is annotated:
its stated exposure is now **discharged** at −11.4 % / +4.3 % in stiffness, inside its own six-model
bracket. Neither claim's numbers move.

**In `TASKS.md`:** the standing finding *"A bulk `χ` is not a brush `χ`, and that gap is 239×
everything else in this section"* needs replacing. Proposed wording is in the `P-9` report; it is not
edited here.

---

## What would overturn this challenge

- **Lee et al.'s Supporting Information**, which carries the SCF free energy and would let the two
  transfers be collapsed into a derived one. If that derivation puts the model's `χ` on the
  Flory-Huggins axis at `≈ 0.60` after all, ground 3 falls — though grounds 1 and 4 do not.
- **A normal-compression measurement of a PEG brush inside the Gen-1 window** (`σ = 0.018–0.092
  nm⁻²`) landing outside `χ_eff ∈ [0.346, 0.424]`. That is the missing measurement, and `C-0013`
  names it as such rather than estimating it.
- **Evidence that the effect is non-monotone in grafting density**, which would break the transfer
  of a bound obtained above the window down into it. Hansen et al.'s own two points do carry a
  density trend in the same direction as Lee et al.'s claim — `Δχ = +0.044` between
  `σ = 0.140` and `0.229 nm⁻²` — 5× too small to matter, inside what its own authors call fit
  scatter, and **not zero**. That is where a reopening would come from.
