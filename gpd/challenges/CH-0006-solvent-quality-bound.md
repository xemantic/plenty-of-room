# CH-0006 — `C-0002`'s salt bound is right in its conclusion and wrong in all three of its steps

| | |
|---|---|
| **Challenges** | [`C-0002`](../claims/C-0002-peg-material-parameters.md), still-open item 1 — the bound *"10 mM of a divalent chloride shifts `θ` by O(0.1–1 K) out of 375 K, **≤ 0.7 % of `τ`**"* — and the `τ = 0.200` row of its parameter sheet |
| **Raised by** | [`C-0007`](../claims/C-0007-solvent-quality-vs-salt.md), task [`P-6`](../tasks/P-6-solvent-quality-vs-salt.md) |
| **Raised** | 2026-08-12, iteration 4 |
| **Status** | **UPHELD in conclusion, OVERTURNED in construction.** `C-0002` is not withdrawn and no number that depends on this bound changes. What changes is that the bound is now *derived* rather than asserted, and it is **7× looser** than stated. |

---

## Why this is filed at all, given that the conclusion survives

Because `C-0002` said so itself:

> **That bound is an argument, not a citation**, and it is queued as `P-6`.

`P-6` has now run the argument properly, and the argument does not hold in the form it was given — it holds
for different reasons, at a different concentration, with a sign that is not established, and with a number
1.5× larger. A bound that reaches the right conclusion through three compensating errors is not a bound;
it is a coincidence that has to be re-derived before anything is built on it. `T-3` is about to build on it.

---

## The standing statement being challenged

`C-0002`, "Still open", item 1:

> What *is* bounded: 10 mM of a divalent chloride shifts `θ` by O(0.1–1 K) out of 375 K, **≤ 0.7 % of `τ`**,
> far below the fit uncertainty on `α`.

and, from the parameter sheet:

| `θ` | theta temperature of PEO/water | 375 | K | CITED |
| `τ` | reduced temperature at 300 K | 0.200 | 1 | **DERIVED** |

---

## The three contradicting results

### 1. The conversion from `Δθ` to `Δτ` is understated by 1.5×

`τ = 1 − T/θ` is a **small difference of two large numbers**, so

&nbsp;&nbsp;&nbsp;&nbsp;`(1/τ)·dτ/dθ = T/(θ²τ) = 300/(375² × 0.200) = **1.067 % per kelvin**`

Hence `C-0002`'s own upper bracket, `Δθ = 1 K`, gives **1.07 % of `τ`** — not the ≤ 0.7 % stated.
The stated figure corresponds to `Δθ = 0.66 K`, i.e. to the middle of the bracket rather than its top.

An entirely independent route — the **measured** `dχ/dT` at the cloud point, with no `τ` in it at all —
gives **1.35 % of the excluded volume per kelvin**, 26 % above the corrected `τ`-route figure and
**1.9× above the number `C-0002` states**. Two routes that share no input agreeing to 26 % is the
verification; the 1.5× arithmetic slip is what they jointly expose.

### 2. The bound was evaluated at the wrong concentration, by up to 6.6×

`C-0002` evaluates at the **buffer** concentration, 10 mM. Per [`C-0005`](../claims/C-0005-mean-field-screening-validity.md)
the polymer layer does not sit in the buffer:

- bulk salt is **depleted** into it, `K_salt = 0.52–0.77`, i.e. down to ~1 mM at the 2 mM buffer;
- the tile's counterions **flood** it — the gap-averaged Mg²⁺ from the Manning-surviving tile charge is
  **33 mM at a 10 nm gap and 66 mM at 5 nm**.

So the layer-local span is ~**1 → 66 mM**, a factor of **66**, not the factor of 5 the buffer range suggests,
and its upper end is **6.6× the concentration `C-0002` bounded at**.

Worse for the *form* of the bound: the counterion inventory per unit area is fixed by the tile's charge while
the gap shrinks under actuation, so the local concentration goes as **`1/h`**. It is not a property of the
buffer at all — **it is a function of the actuator's own stroke**, and a bound stated per-buffer cannot
express that.

### 3. The sign is asserted, and it is not established for MgCl₂

`C-0002` inherits §2's direction — kosmotropic salts drive PEG toward poor solvent — and applies it to
"a divalent chloride". For MgCl₂ specifically the evidence points the other way
(Sadeghi & Jahani, *J. Phys. Chem. B* **116**:5234, 2012, abstract verbatim): PEG forms **no aqueous
two-phase system with MgCl₂ at all**, and *"the salting-in effect results from a direct binding of the
cations to the ether oxygens of the polymers"*. And Boucher & Hines (1976) report that for Group II chlorides
`θ(c)` shows **minima**, i.e. is non-monotonic — so "shifts `θ` by O(0.1–1 K)" is not merely uncertain in
magnitude, it is not a well-posed linear statement for this salt family.

### And, separately: `τ = 0.200` is the optimistic end of a 16 K band

`C-0002` carries `θ = 375 K` as `CITED` and derives `τ = 0.200`. The measured determinations are
**358.7 K** (Flory-Huggins analysis), **369 ± 3 K** (cloud points), **373.2 K** (virial analysis) —
a **16.3 K** band, giving `τ = 0.164–0.200`. `C-0002`'s value sits at the top. Nothing in `C-0002` depends on
`τ` numerically, which is why this is listed here rather than raised as a separate challenge, but any future
task that uses `τ` must carry the band.

---

## Methodological grounds

The bound was constructed as *"a small concentration of a weak salt cannot do much"*. That reasoning is
unsafe here for a reason specific to this material: **PEG in water is a weakly good solvent**, `1 − 2χ = 0.26`,
so solvent quality is a small residual and a small absolute change in `χ` is a large *fractional* change in
the excluded volume. `Δχ = 9.5e-4` — four decimal places out, and unmeasurable by any method — is already
0.74 % of the excluded volume. Order-of-magnitude reasoning about `χ` does not transfer to order-of-magnitude
reasoning about `v`, and the bound skipped that step.

The correct construction runs through a quantity that can actually be bounded from the literature:
the **cloud-point slope**, ceilinged at 69 K/M by the strongest salting-out salts in the PEO survey of
Boucher & Hines (1976), with chlorides explicitly weaker.

---

## The corrected bound

| span | `C-0002` says | `C-0007` derives |
|---|---|---|
| 2 → 10 mM buffer | ≤ 0.7 % of `τ` | **`Δv/v` ≤ 0.74 %, `ΔK/K` ≤ 0.40 %** (0.55 % on the pessimistic transfer exponent) |
| layer-local 1 → 66 mM | not considered | **`Δv/v` ≤ 6.0 %, `ΔK/K` ≤ 3.3 %** |
| sign | poorer solvent | **not established; possibly the reverse** |
| form | linear in `c` | **non-monotonic for Group II chlorides** |

So the defensible bound on the layer's mechanics across the *buffer* range is **tighter** than `C-0002`'s
(0.40 % of the modulus, against 0.7 % of `τ`), and across the *layer-local* range it is **7× looser**
(3.3 %). Both are small. **`T-3` is unaffected: it may treat the layer's mechanics as independent of the
buffer.** That is why this is a challenge to the construction rather than to the conclusion.

## Resolution requested

1. `C-0002`'s still-open item 1 should be marked **closed by `C-0007`**, with the bound replaced by the
   corrected one and the concentration range restated as layer-local rather than buffer.
2. `C-0002`'s `τ = 0.200` row should carry the 0.164–0.200 band.
3. `C-0002`'s `χ ≈ 0.45` remark should be replaced by the measured `χ(300 K) = 0.372`, or deleted —
   `C-0007` finds no primary source for 0.45 and identifies the circulating 0.44 as polystyrene in toluene.

None of these changes any number that any standing claim computes.
