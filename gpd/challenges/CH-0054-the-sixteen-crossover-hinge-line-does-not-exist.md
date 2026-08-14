# CH-0054 — The 16-crossover hinge line does not exist on a 40 nm tile: a hinge line carries four, and at the count 45 flexures can each own, `E5` fails `C-0023`'s own compliance ceiling at §3's ACCEPTABLE stroke

| | |
|---|---|
| **Raised by** | [`C-0040`](../claims/C-0040-hinge-line-census.md) ([`T-81`](../tasks/T-81-hinge-line-census.md)) |
| **Against** | [`C-0029`](../claims/C-0029-perpendicular-junction-routing.md)'s **`E5g16`** and [`C-0034`](../claims/C-0034-guided-arm-anchorage.md)'s **`E5a16`**, and [`C-0023`](../claims/C-0023-two-sided-coupling.md)'s free `hingeCount` |
| **Grounds** | a **count on the lattice**, checked against `C-0015`'s own construction over the complete 32 bp phase space |
| **Status** | **OPEN** — no number in any of the three claims fails to reproduce; what fails is an integer none of them checked |

---

## The statement

`C-0029` adopts *"`E5g16` — 16 antiparallel crossovers, `k_θ` = 13.53 pN·nm/rad each"*.
`C-0034` adopts `E5a16` on the same hinge and reports its own `P4` as *"PASS at 16 and 32 crossovers; **FAILS at 8**"*,
and names the gap itself as its open item 5:
*"Whether 16 crossovers can be assembled into one hinge line on a 40 nm tile at all."*

**They cannot.**

- Crossovers serve one **interface** every **32 bp = 10.88 nm** (16 bp per *helix*, alternating between its two neighbours),
  so a hinge line of `n` crossovers needs `(n − 1) × 10.88 nm` of **collinear interface**.
- Sixteen needs **163.2 nm — 4.08 tiles.**
- A **transverse** line needs `2n + 1 = 33` duplexes = **88.8 nm — 2.22 tiles** — and its crossovers are dihedral springs
  about a line running along the helices, so that topology does not supply `n k_θ` at all.
- The complete 32 bp census gives **four** at **every** phase, three on the other parity at 22 of them.
- The absolute geometric ceiling — a line spanning the tile dilated by the arm — is **six**.
- At 45 paths each flexure's own plan share (13.33 nm) carries **one or two**.
- The whole tile's crossover inventory is **49–56**; 45 paths × 16 demand **720**, i.e. **12.86×**.

---

## What moves, and what does not

**Nothing computed in `C-0023`, `C-0029` or `C-0034` is wrong.** All sixteen upstream reproductions land
inside the rounding their own claims quote — `E5g8` 10.3056, `E5g16` 12.2424, `E5a16` 11.0285, its tangents
33.565/36.780, its realised `c` 7.3557, its failing 8-crossover arm 9.5174, and the cantilever ceiling 9.76624,
worst departure `3.8e−9` outside those roundings.
What moves is the **argument of every one of those functions**.

| | at the asserted `n = 16` | **at `n = 4`** (a full-length tile interface) | **at `n = 1–2`** (45 flexures, each its own share) |
|---|---|---|---|
| placed arm | 11.03 nm | **7.748 nm** | 4.76–6.08 nm |
| tangent at §3's **acceptable** 3 nm | 33.57 pN/nm | **36.58 pN/nm** | **42.01–54.11 pN/nm** |
| inside `C-0023`'s 40 pN/nm ceiling there | yes | **yes** | **NO — by 1.05–1.35×** |
| reaches §3's **desired** 10 nm by rotation (`δ = r sin θ < r`) | yes | **NO** | **NO** |
| dominant compliance term (`A8.2`'s named quantity) | the **arm**, 58.5 % | the **hinge**, 78.3 % | the **hinge**, 89–95 % |

&nbsp;&nbsp;&nbsp;&nbsp;**So `E5g16`/`E5a16` do not reach §3's desired stroke at the count that exists — the threshold is 10 crossovers to reach it and 12 to reach it inside the ceiling — and at the count 45 independent flexures can actually own, the element fails `C-0023`'s own compliance ceiling at §3's ACCEPTABLE stroke as well.**

`C-0034`'s own `P4` therefore fails as stated, at the supply the lattice provides:
it reports the design as failing at eight, and four is fewer than eight.

---

## The one escape, priced

Sixteen crossovers **can** be assembled into one flexure — four interfaces of four, each line 32.64 nm, which fits the tile.
But parallel interfaces compose in **series**: each carries only the moment of what is outboard of it and turns through its own angle.

&nbsp;&nbsp;&nbsp;&nbsp;**`n_eff = n_i · 3(2m − 1)/(m(2m + 1))`** &nbsp;→&nbsp; sixteen crossovers as a 4 × 4 fan are worth **2.333** of hinge, **14.6 %** of their own count.

Forty-five such flexures assemble to **16.03 pN/nm**, **2.08× too soft** for §3's mandate;
the fan that would place needs **nine** crossovers per interface, i.e. **87.0 nm** of collinear line, **2.18× the tile**.

The other escape — a hinge line on an unbounded superstructure — needs **45 lines of 163.2 nm at a 2.69 nm pitch = 19,755 nm²,
12.3× the tile's footprint**, which is `T-96`'s plan-view question answered in the negative.

---

## Why this is a challenge and not a correction

Because it changes a **verdict**, not a number.
`C-0029` closed the standoff branch at §3's desired stroke and left `E5g16` as the programme's only element reaching it;
`C-0034` re-derived its end condition and kept it.
With the count checked, **`E5` reaches §3's acceptable 3 nm stroke and not its desired 10 nm**,
and the programme's answer to `A8.2` at the desired stroke now rests entirely on `C-0037`'s triangulated standoff —
which is `T-98`'s comparison with one of its two options removed.

## What would settle it

1. **A published single-layer Rothemund sheet with a per-interface crossover spacing below 32 bp.**
   The premise is swept here: at the 16 bp per-helix mis-reading the count is **8**, at honeycomb's 21 bp **6**,
   at both together **12**. **No reading in circulation reaches sixteen**, so the pitch would have to be a quarter of the cited one.
2. **Admitting `L5`** — hinge lines outside the tile — at a superstructure 12.3× the tile's footprint. `T-96`.
3. **A design at fewer, longer flexures.** The path count is bounded below at **34** by `CH-0029`'s unzip allowable
   and above by the tile's crossover inventory; that trade is the one `C-0040` does not sweep and it is named as its open item 2.
