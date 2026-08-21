# CH-0194 — **`C-0140`'s *"stagger of at most 4 bp"* filter is not merely unstated, it is EXACTLY DISJOINT from scaffold closure — and its `F9` headline, *"the minimum stagger is 3 bp = 1.02 nm"*, is a PER-HELIX minimum that no drawable raster reaches.** Every closing residue pair has `L₁ − L₂ ≡ 14 (mod 21)`, so `|L₁ − L₂| ∈ {7, 14, 28, …}` and the least stagger a two-length honeycomb x-raster can carry **on caDNAno's own default rules** is **7 bp = 2.38 nm**. `C-0140`'s selection rule could therefore not have returned a drawable pair at any lengths whatever

| | |
|---|---|
| **Against** | [`C-0140`](../claims/C-0140-honeycomb-raster-turn-sense.md)'s headline *"the remedy costs **3 base pairs = 1.02 nm**"*, its `F9` (*"the minimum stagger is not 3 bp"* — did not fire), and §4's selection rule *"minimum `\|extent − 40 nm\|` among pairs with a stagger of at most 4 bp that fit M13"* |
| **Raised by** | [`C-0151`](../claims/C-0151-closing-raster-selection.md) / [`T-245`](../tasks/T-245-closing-raster-selection.md), result [`gpd/results/T-245-closing-raster-selection.json`](../results/T-245-closing-raster-selection.json), sections `cheapBound` and `closingResidueClasses` |
| **Grounds** | **logical.** One line of modular arithmetic on the closing residue set, which is itself an exhaustive enumeration of the 441 residue pairs. No solve, no search |
| **Kind** | **a filter that selects against the constraint it does not carry.** `CH-0187` challenged the filter for being unstated and priced it as a preference; the stronger statement is that it is *anti*-correlated with drawability, so the rule and the rule's own purpose are in opposition |
| **Status** | **raised.** `C-0140`'s turn-sense theorem, its class algebra, its level walk, its *"no uniform row length exists"* negative and its `F9` **as a per-helix statement** are all upheld and consumed unmodified. What is challenged is the reading of 3 bp as *the* minimum remedy, and the status of the filter |

---

## 1. The arithmetic

`C-0148` established the closure condition: every raster crossover is a **scaffold** crossover and
sits at `b₀ + 7c ± 5 (mod 21)`, one `b₀` serving the whole design, so the reduced residues
`(level − 7·class) mod 21` must number at most two and be 10 apart.

The reduced residues are integer combinations of the two row lengths on a **fixed** class
sequence, so **closure depends on the lengths only through their residues modulo 21** — 441 cases,
enumerable exactly. `C-0151` enumerates them at both 60-helix cross-sections:

| `L₁ mod 21` | `L₂ mod 21` | `L₁ − L₂ (mod 21)` | least `\|L₁ − L₂\|` |
|---|---|---|---|
| 7 | 14 | **14** | **7** |
| 17 | 3 | **14** | **7** |
| 18 | 4 | **14** | **7** |

**All three share one difference.** So a closing pair's stagger is congruent to 14 modulo 21, its
absolute value lies in `{7, 14, 28, 35, …}`, and **7 bp = 2.38 nm is a floor, not a sample.**

## 2. Why `F9` did not fire, and why that is not a defence

`C-0140`'s `F9` is *"the minimum admissible row-length stagger is not three base pairs"*, and it
did not fire — correctly. Its `minimumRowLengthStagger` is computed from `C-0136`'s admissible
row-length residues, `{7, 17, 18}` against `{3, 4, 14}`, i.e. **`C-0136`'s rule applied one helix
at a time**. That rule is necessary and not sufficient: it fixes each helix's *own* two `±5`
choices and says nothing about whether the two helices sharing a crossover agree about which of
the two positions that crossover occupies. `CH-0188` is that gap; this challenge is its
consequence for the **minimum**.

Both numbers are right about different questions:

| question | answer |
|---|---|
| the least stagger two *admissible row lengths* can differ by | **3 bp = 1.02 nm** (`C-0140` `F9`) |
| the least stagger a *drawable raster* can carry | **7 bp = 2.38 nm** (`C-0151`) |

The headline sentence *"the remedy costs 3 base pairs = 1.02 nm"* reads as the second and is the
first.

## 3. The filter

`C-0140` selects on *"minimum `|extent − 40 nm|` among pairs with a **stagger of at most 4 bp**
that fit M13"*. Since no closing pair has a stagger below 7, **the filter's admissible set and the
closing family are disjoint**: at no pair of row lengths could that rule have returned a buildable
design.

That is why `CH-0188` found four of `C-0140`'s five candidates undrawable and the fifth drawable —
`102 / 109`, at a stagger of **7 bp**, which is exactly the pair the filter excluded and which
appears in `C-0140`'s own Deliverable-4 table only because the table is printed wider than the
rule that reads it.

## 4. What it costs, and what it does not

**Nothing on the width.** `C-0151` re-selects inside the closing family and the best
`|extent − 40 nm|` there is **1.40 %** — the identical figure `C-0140` reports, at the identical
116 bp = 39.44 nm extent. The honeycomb's width advantage over the square lattice's `−4.80 %`
survives intact.

**One crossover column on the flatness**, because the interface window is `rowSpan − stagger` and
a 7 bp stagger spends 4 bp more of it than a 4 bp one: 102 bp against 108, and 10 columns against
11 (`CH-0195`).

**And it BUYS three things**: zero forced crossovers against ten; 270 nt of scaffold; and a
blunt-end stacking clearance of **3.18 rises** against `CH-0187`'s **0.18**, which `CLAUDE.md`'s
own rule says is not a quotable margin at all.

## 5. Scope

- `C-0140`'s Deliverables 1, 2, 3 and 5 are untouched, as is `CH-0172` and the whole turn-sense
  derivation. Every number this challenge uses comes from `C-0140`'s own machinery, re-run.
- `C-0136`'s row-length rule is upheld and re-derived independently in `C-0148`; it is being read
  for more than it says, not being contradicted.
- **`CH-0187` is subsumed rather than overturned.** Its four axes are re-scored on the closing
  family in `C-0151` §2a and three of them prefer the drawable pair; its `101 / 109` alternative
  does not close either.
- The `±5 bp` rule is caDNAno's **default**. A 4 bp stagger remains **buildable off-rule**, at 10
  forced scaffold crossovers, whose **elastic** price [`C-0152`](../claims/C-0152-forced-scaffold-crossover-price.md)
  (`T-246`, same iteration) puts at `0.350894669 k_BT` each — sub-thermal — with the **kinetic**
  price unpriced by anybody. So this challenge is about a **selection rule**, not about whether the
  4 bp design can be folded.
