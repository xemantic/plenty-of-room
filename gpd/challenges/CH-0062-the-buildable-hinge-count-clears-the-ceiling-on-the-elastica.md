# CH-0062 — The buildable hinge count does NOT fail the compliance ceiling at the acceptable stroke: `C-0040`'s 42.0–54.1 pN/nm at one and two crossovers is read on `C-0034`'s series composition, which `CH-0053` supersedes; on `C-0039`'s own exact elastica the same design places at a 9.131 nm arm and **39.18 pN/nm — inside the ceiling**

| | |
|---|---|
| **Against** | [`C-0040`](../claims/C-0040-hinge-line-census.md)'s verdict clause *"At the one or two a flexure can actually own, the tangent is 42.0–54.1 pN/nm and **even the acceptable stroke fails `C-0023`'s own ceiling**"*, and its design table rows at `hingeCount` = 1–3 |
| **Raised by** | [`C-0049`](../claims/C-0049-compliance-ceiling-stroke.md) / [`C-0050`](../claims/C-0050-desired-stroke-reach.md) ([`T-107`](../tasks/T-107-compliance-ceiling-stroke.md), [`T-108`](../tasks/T-108-desired-stroke-reach.md)) |
| **Grounds** | **methodological** — a design graded on a placement pipeline that a sibling claim, filed in the same iteration, had already shown to be superseded; the two pipelines differ by **1.9× in the placed arm and 1.38× in the tangent**, and the difference is largest exactly where `C-0040`'s verdict is taken |
| **Status** | **OPEN.** `C-0040`'s census, its ledgers, its fan law and its `n_eff` series result are **untouched and re-run here**. What is challenged is one design-verdict clause and the rows behind it |

---

## What is being challenged

`C-0040` closes with two statements about the hinge counts a 40 nm tile can actually supply:

> *"At four crossovers the arm places at 7.748 nm: §3's acceptable 3 nm clears at 36.58 pN/nm … **At the one or two a flexure can actually own, the tangent is 42.0–54.1 pN/nm and even the acceptable stroke fails `C-0023`'s own ceiling.**"*

Its design records give, at 45 paths and `C-0034`'s `A2` anchorage (78.2353 pN·nm/rad):

| `h` | `C-0040` arm | `C-0040` tangent at 3 nm | inside 40 pN/nm |
|---|---|---|---|
| 1 | **4.765 nm** | **54.113** | **no** |
| 2 | 6.079 nm | 42.007 | **no** |
| 3 | 7.024 nm | 38.315 | yes |
| 4 | 7.748 nm | 36.584 | yes |

Those numbers come from `C-0034`'s `anchoredArmForStiffness` — the **series composition**, in which the hinge and the arm are composed as springs in series and the far anchorage enters through `guidedArmFactor` alone.

**[`CH-0053`](CH-0053-both-errors-run-the-same-way-and-the-desired-stroke-does-not-survive-them.md), filed by [`C-0039`](../claims/C-0039-two-spring-elastica.md) in the same iteration, established that the series composition is the *short* reading of a two-spring beam**: it *"retains only 0.726"* of the true stiffness, so a design placed on it needs a **shorter** arm to reach the same target — and a shorter arm at a fixed 3 nm stroke has a larger `δ/L`, hence more geometric stiffening, hence a larger tangent.

At sixteen crossovers the effect is modest (11.028 nm against the elastica's 12.720, 1.15×) because the hinge is stiff and the arm dominates.
**At one crossover the hinge is the whole compliance and the composition is nearly everything**, so the same correction is worth 1.9× in the arm.

## The correction, computed on `C-0039`'s own solver

`elasticaArmForStiffness(k_θ, hingeCount = 1, farStiffness = 78.2353)` and the resulting `TwoSpringElastica`, at 45 paths, §3's acceptable stroke, `EI` = 230 pN·nm²:

| `h` | `C-0040` (series, `C-0034`) | **`C-0039` elastica (exact in both)** | departure | inside `C-0023`'s 40 pN/nm |
|---|---|---|---|---|
| **1** | arm 4.765 nm, tangent **54.113** | **arm 9.131 nm, tangent 39.18** | **1.92× / 1.381×** | **YES, with 2.1 % of margin** |
| **2** | arm 6.079 nm, tangent **42.007** | **arm 9.985 nm, tangent 38.04** | 1.64× / 1.104× | **YES** |
| 4 | arm 7.748 nm, tangent 36.584 | arm 11.04 nm, tangent 37.13 | 1.42× / 0.985× | yes, both ways |

The elastica numbers are not new: **`C-0039`'s own published placement table already contains `A2` at `n` = 1 with a tangent of 39.18 pN/nm**, and marks the row *"no"* only in its *desired*-stroke column.
`C-0039` and `C-0040` were filed in the same iteration, against the same anchorage, with the same constants, and **they disagree about whether the buildable hinge count clears the ceiling at §3's acceptable stroke.** Neither noticed.

Reproduced here as a gate-5 test: the arm to 1e−3 of 9.131 nm, the tangent to 1e−3 of 39.18 pN/nm, and the ratio to `C-0040`'s own 54.1134674 as **1.3812**.

## Why it matters

1. **It reverses `C-0040`'s branch verdict at §3's acceptable clause.** `C-0040` leaves `E5a` failing the ceiling at every count the lattice can afford at 45 paths; on the exact elastica it **clears**, and `E5a1` is one of only three rows in `C-0050`'s whole catalogue that clear every predicate at 3 nm.
2. **The crossover budget makes `h = 1` the only affordable count anyway**, so this is not a corner of the design space — it *is* the design. 45 paths at one crossover each spend 45 of the tile's 49–56 (`C-0015`); at two they demand 90. `C-0046` reaches the same conclusion independently, by a different route, and reports its own best point `(56, 1)` at **38.17 pN/nm** — likewise inside the ceiling, likewise contradicting `C-0040`'s clause, and likewise without naming the disagreement.
3. **The correction is largest where the claim's verdict is taken.** A challenge against a 1.15× discrepancy at `h` = 16 would not be worth filing; the same discrepancy is 1.38× at `h` = 1 because the composition error scales with the hinge's share of the compliance, which `C-0023` puts at **92.5 %** for a single crossover.

## What is NOT challenged

- **`C-0040`'s census**: four crossovers on a 40 nm hinge line at **every one of the 32 phases**, 163.2 nm of collinear interface for sixteen, 33 duplexes across. Re-run here and reproduced exactly.
- **Its series-composition law for a raft on `m` hinge lines**, `n_eff = n_i·3(2m−1)/(m(2m+1))`, and the fan result that follows.
- **Its central finding**, that `E5g16`/`E5a16` rest on a count the lattice does not supply. Nothing here supplies sixteen; the point is that **one** is enough at the acceptable stroke.
- **Its desired-stroke verdict.** `C-0050` confirms it from three directions and one of them owes nothing to any element.
- **Any of `C-0034`'s numbers.** `CH-0053` already relocated them; this challenge only observes that `C-0040` consumed them after that relocation was available.

## What would resolve it

`C-0040`'s design section re-run with `elasticaArmForStiffness` in place of `anchoredArmForStiffness` — a one-line substitution in `anchoring.HingeLineCensusStudyKt`, since both take the same arguments and `C-0039`'s library is already on the classpath.
The census, the ledgers, the fan law and the `n_eff` result do not move; only the thirteen design rows do.

Until then, **`C-0040`'s design table should be read as the series composition's answer**, and `C-0039`'s or `C-0046`'s as the exact one — which is `CH-0053`'s own instruction, applied one claim further downstream.

## What this challenge does NOT rescue

**§3's desired stroke.** `E5a1` folds before reaching 10 nm — `C-0039`'s solver refuses the stroke rather than approximating it — and `C-0050`'s kinematic bound refuses it independently of any element.
This challenge moves a verdict at §3's **acceptable** clause only, and it moves it the favourable way.
