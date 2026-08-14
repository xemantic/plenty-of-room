# C-0064 — No distribution is flat at all five of `C-0022`'s solved states, and the obstruction is a **sign**: a real minimax reaches **0.1254** of the stroke against `C-0058`'s 0.1587 and stops, because the 2 nm state is the only one of `C-0022`'s 21 whose edge effect is a net **loss**, so its free-tile dishing field is **anti-parallel** to every 10 nm state's. But the five states are **four devices**, not an operating range — and over the range each device actually traverses a robust distribution **exists**, at 0.0373–0.0620 of the stroke, every one inside `T-5b`'s 0.10

| | |
|---|---|
| **Task** | [`T-123`](../tasks/T-123-robust-distribution.md), which is [`C-0058`](C-0058-non-uniform-coupling.md)'s *Still open* item 2 and [`CH-0071`](../challenges/CH-0071-the-saturation-floor-is-a-property-of-the-equal-spring-family.md)'s own second overturning condition |
| **Leaf** | **`A8.2`** (*"identify the dominant compliance term … and budget stiffness at the joints"*), with `A1.2` for the anchoring scheme the distribution belongs to |
| **Verification type** | **in-silico** (`C-0009`'s beam-and-hinge grillage and `C-0006`'s continuum plate under `C-0022`'s **solved** load read from its own result file and keyed on concentration, gap **and bias**, driven by an exact **multi-state** Woodbury surrogate with **analytic gradients**, asserted against `C-0058`'s own `InfluenceSurrogate` to `1.2e−15` and against the assembled solve to `≤ 1e−11`) **+ logical** (a **per-state** least-squares bound in the space of attachment forces, which bounds every distribution whatever, and the per-path force ceiling as arithmetic) |
| **Verdict** | **PASS on the predicate, and the answer is NO at the five states and YES over every operating range.** A smoothed minimax with analytic gradients and **42 starts** — against `C-0058`'s cyclic coordinate descent from three — reaches a worst case of **0.1254** of the free-tile stroke over `C-0022`'s five headline states, **21.0 % better** than `C-0058`'s 0.1587 and still **1.25×** `T-5b`'s 0.10. **The search is not what limits it**: 16 starts to 42 moves the answer by **exactly zero**, the six-level smoothing homotopy is worth 8.4 %, and **two states are active at the optimum**, which is what an equalised minimax looks like. **The obstruction is a sign.** Of the 31 non-empty subsets, **every one of the 14 that puts the 2 nm state together with a 10 nm state fails** (0.1086–0.1254) and **every one of the other 17 is flat** (0.0090–0.0799) — including everything-but-the-2-nm-state at **0.0799** and the 2 nm state paired with the 5 nm rest state, *its own device*, at **0.0620**. The 2 nm state's free-tile dishing field has a cosine of **−0.943 to −1.000** against every other state — exactly **−1.000** against the 10 mM one — while all six pairs among the other four run **+0.949 to +0.997**; and `C-0022`'s own table says why: the 2 nm state is the **only one of its 21** whose finite tile carries **less** total force than a 1-D pressure over the footprint (−3.91 %). **But the five states are not an operating range.** Three are the *rest* states of three different buffers at a 10 nm layer and two are the *rest and held* states of a **5 nm** layer; the 2 nm state is that device at §3's 3 nm stroke and no state of the 10 nm device at all. Over the range each device traverses — one buffer, one layer, one bias, `L₀` down to `L₀ − s` — the minimax is **0.0373** (`C-0018`'s placed 2 mM device), **0.0435** (`C-0032`'s 0.5 mM recommendation), **0.0620** (the 5 nm device) and **0.0504** (10 mM): **all four inside `T-5b`'s 0.10**, both endpoints active at each optimum, and adding two interpolated intermediate gaps moves the first by **exactly zero at nine significant digits**. **The buildability failure this task was told to look for is real and is not the one expected**: a 45-parameter optimum's **quantisability is not a well-posed quantity**, because the optimal set is a *manifold* — 2 active constraints against 44 free directions — and two builds of this search differing only in their decision precision returned equally good points (0.0373 and 0.0372) whose per-path spreads were **26.0×** and 7.9× and whose optimal two-level projections were **0.0899** and **0.1002**, on opposite sides of `T-5b`'s line. **What is well posed is the best member of the constrained family, and it already exists**: `C-0058`'s two-level rim × 5 rule is flat over the whole traversed range of both 10 nm devices at **0.0753** and **0.0683**, better than either projection and already priced buildable by `C-0060`. |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED**, and the coupling motif every station belongs to is **not demonstrated** (`C-0028`, `C-0029`, `C-0055`). |
| **Provenance** | `gpd/results/T-123-robust-distribution.json`, produced by `coupling.RobustDistributionStudyKt`; model in `src/main/kotlin/coupling/RobustDistribution.kt`; **5 state records, 10 cheap-bound records, 20 tension records, 31 subset records (every non-empty subset of the five), 4 operating-range records, 8 assembled solves, 90 path records, 8 buildability records, 18 convergence records, 18 upstream reproductions**; **17 gate-named tests in `src/test/kotlin/coupling/RobustDistributionTest.kt`**; `tools/verify.sh` **BUILD SUCCESSFUL in 8 m 58 s** on its own isolated tree — the whole suite, on the finished tree — with a concurrent agent's mid-TDD test dropped by `--drop-file` (`src/test/kotlin/anchoring/CrossbarTrioExistenceTest.kt`, `T-127`); the result file re-run through `tools/study.sh` and diffed: the two runs **agree on every verdict, every headline number and every one of the 90 emitted path stiffnesses**, with **28 of ~3 000 lines** differing — a reported limitation rather than a discharged requirement, see *Determinism* below |
| **Conditions** | T = 300 K, `k_BT = 4.141947 pN·nm`; aqueous buffer with **Mg²⁺** at 0.5 / 2 / 10 mM; 40.0 × 40.35 nm tile, 15 duplexes at the SAXS-measured 2.69 nm; 8 symmetrically centred crossover columns (`T-10`); `C-0015`'s **3 × 15** grid, 45 stations; §3's 100 pN over the footprint; `C-0017`'s **33.3333 pN/nm as a SUM**, distributed; per-path ceiling 3.3333 pN/nm (the 10 pN unzip allowable at the 3 nm stroke); `C-0001`'s foundation secant, ×1; free-tile stroke **4.90731 nm**; **13 load states** read from `C-0022`'s own result file |
| **Consumes** | [`C-0058`](C-0058-non-uniform-coupling.md) (the question, `InfluenceSurrogate`, `rimStiffenedWeights`, `loadMatchedWeights`, `normalisedStiffnesses`, `cappedStiffnesses`, `perPathStiffnessCeiling`, `admissibleStiffnessRatio`, `perPathThermalForces` and `optimiseStiffnessDistribution` — **all re-run as libraries**, and its 0.2182 / 0.0753 / 0.1867 / 0.0710 / 0.2551 reproduced as limiting cases), [`C-0022`](C-0022-tile-edge-load-profile.md) (the **solved** collars, read at run time and keyed on `(concentration, gap, bias)`), [`C-0060`](C-0060-buildable-stiffness-ratio.md) (the two levels, the measured `3.5 ≤ R ≤ 20` flat window, the 1.0–19.1 % quantum, the 34.6 % scatter threshold — **consumed, not re-derived**), [`C-0018`](C-0018-maximum-usable-bias.md)/[`C-0032`](C-0032-softening-coupling-stability.md) (where the coupled equilibrium actually sits, which is what defines an operating range), [`C-0049`](C-0049-compliance-ceiling-stroke.md) (`n·a/s`, re-derived), [`C-0014`](C-0014-lateral-confinement.md) (`√(k_BT k)/N`, reproduced exactly at equal paths), [`C-0009`](C-0009-discrete-lattice-tile.md)/[`C-0006`](C-0006-tile-load-distribution-and-flatness.md)/[`C-0015`](C-0015-crossover-phase-and-registration.md)/[`C-0026`](C-0026-one-row-per-duplex.md)/[`C-0047`](C-0047-single-column-flatness.md) (the grillage, the plate, the grid, the flatness pipeline and its normaliser), [`C-0017`](C-0017-output-coupling-stiffness.md) (the mandate, **as a sum**) |
| **Raises** | [`CH-0077`](../challenges/CH-0077-five-solved-states-are-four-devices.md), against `C-0058`'s Deliverable 4 and `CH-0071`'s overturning condition 2 |

---

## The claim, in one line

**`C-0058` asked whether one distribution can be flat at all five of `C-0022`'s solved states, found it could not, and called the result a "not found"; the answer is that it is a genuine incompatibility — one state's edge effect has the opposite sign to every other's, so their dishing fields are anti-parallel and no force vector flattens both — but that the requirement itself is mis-posed, because the five states belong to four different devices, and every real device's own traversed range admits a flat distribution.**

---

## The conventions, restated rather than inherited

- Lengths **nm**, forces **pN**, stiffness **pN/nm**, pressure **pN/nm² = 1 MPa exactly**;
  `k_BT = 4.141947 pN·nm` at **300 K** in aqueous buffer with **Mg²⁺**.
- `x` runs **along** the helices, `y` **across** them; the origin is the tile centre. `w` is positive
  **downward**, compressing the polymer layer (`T-5`).
- **Dishing** is the peak absolute departure from the area-weighted least-squares best-fit **plane** —
  piston and both tilts removed — on the same **81 × 81** grid as `C-0026`, `CH-0034`, `C-0047`,
  `C-0058`, `C-0060` and `C-0063`.
- The **free-tile stroke** is the mean deflection of the *unsupported* plate under the *uniform* load at
  the same foundation stiffness: **4.90731 nm**, the unchanged normaliser.
- **Flat** means peak dishing below **10 %** of that stroke — `T-5b`'s convention via `C-0015`,
  **a convention and not a physical threshold**.
- A **collar depth is negative for an enhancement**, which is the sign `C-0022` solved; its **rim
  residual** term may exceed one in magnitude, which means the load **reverses sign** inside the collar.
- The **coupling** is `n` linear springs to ground whose stiffnesses **sum** to `C-0017`'s
  33.3333 pN/nm. The sum is the mandate; the **distribution** is the design variable.
- A **state** is a `(concentration, gap, bias)` triple of `C-0022`'s solved profiles.
- An **operating range** is the set of states **one device** traverses: one buffer, one layer height,
  one bias, from gap `L₀` down to `L₀ − s`. **This distinction is new here and it is the claim.**

### The upstream gotcha, avoided by construction

`gpd/results/T-3b-*.json` carries **more than one** profile per `(concentration, gap)` — one per
operating bias. Every lookup here is keyed on **all three** and errors if the triple is absent.

---

## The cheap bound, which ran first, and did not fire

Dishing is **affine in the attachment forces** and every stiffness distribution produces *some* force
vector, so for each state the least-squares minimum over the whole of `ℝⁿ` — no mandate, no positivity,
no relation between a force and a stiffness — is a rigorous lower bound on the peak dishing of **every**
distribution at that state. The states **decouple** under that relaxation, so `max_s` of the per-state
floor is a rigorous lower bound on the **minimax**.

| set | states | floor / stroke | falsifier fired? |
|---|---|---|---|
| the five headline states | 5 | **0.00321** | **no** |
| 2 mM, 10 nm, 0.192 V | 1 | 0.00268 | no |
| 0.5 mM, 10 nm, 0.134 V | 1 | 0.00321 | no |
| 10 mM, 10 nm, 0.192 V | 1 | 0.00289 | no |
| 2 mM, 5 nm, 0.368 V | 1 | 0.00106 | no |
| 2 mM, 2 nm, 0.368 V | 1 | 0.00143 | no |

> The declared falsifier was *"if the largest per-state floor exceeds 0.10, no distribution whatever is
> flat at every state and the answer is a proven impossibility"*. It **did not fire**, at 31× below the
> tolerance. **So the five-state negative below is a "not found at a large budget", not a theorem** —
> and the reason the bound is loose is exactly the freedom the question forbids: it lets each state
> choose its own force vector. Saying so is the point of having run it.
>
> **Maxwell-Betti reciprocity of the influence matrix holds to `1.2e−15`**, measured between two
> different quadratures rather than imposed.

---

## Deliverable 1 — the minimax, genuinely re-run

`C-0058`'s optimiser is a **cyclic coordinate descent on a nonsmooth maximum**, which is the one
classical method that provably stalls on a kink: at a point where no single coordinate direction
descends although a combination of two does. The method here is a **log-sum-exp smoothing with
continuation**, **analytic gradients through the Woodbury solve** (`∂F/∂k_j = (F_j/k_j²) A⁻¹ e_j`,
checked against a central finite difference as gate 4), **nonlinear conjugate gradients on the
log-weights** — in which the mandate is exact by a softmax and positivity does not exist — and
`C-0058`'s own coordinate descent as a **polish stage**, from **42 starts**.

| | `C-0058` | **here** | change |
|---|---|---|---|
| **minimax over the five solved states**, / stroke | 0.1587 | **0.1254** | **−21.0 %**, still **1.25×** the tolerance |
| single-state optimum at the design point, / stroke | 0.0544 | **0.0214** | **2.54× better**, at the same state and the same 45 paths |

The five per-state optima are **0.0214 / 0.0219 / 0.0279 / 0.0144 / 0.0090** — *every state is easy on
its own*, by a factor of 3.5 to 11 inside the convention. The difficulty is entirely in sharing.

**The search is not what limits the five-state answer**, and this is measured rather than asserted:

| axis | setting | / stroke | departure |
|---|---|---|---|
| starts | 1 / 6 / 16 / **42** | 0.12649 / 0.12626 / 0.12539 / **0.12539** | **exactly zero** from 16 to 42 |
| smoothing homotopy | 1 level / 2 levels / **6 levels** | 0.13538 / 0.13014 / **0.12487** | worth **8.4 %** |
| sampling grid | 41 / 81 / **161** | 0.12538 / 0.12539 / 0.12539 | `1.3e−5` |
| **NESTED** subdivisions | 1 ⊂ 2 ⊂ **4** | 0.12588 / 0.12539 / 0.12540 | `1.0e−4` |

and **two states are active at the optimum** — 10 mM and 2 nm, both within `1e−4` of the worst — which
is the signature of a minimax that has *equalised*, not of a search that has stalled. Only 1 of the 42
starts lands within one part in a million of the best, which is the expected shape of a nonsmooth
objective: many basins, one value.

---

## Deliverable 2 — which states bind, and why: a **sign**, not a magnitude

All 31 non-empty subsets of the five were optimised.

| partition | subsets | worst / stroke | flat? |
|---|---|---|---|
| **contains the 2 nm state AND a 10 nm state** | **14** | **0.1086 – 0.1254** | **none** |
| everything else | **17** | 0.0090 – **0.0799** | **all** |

The dichotomy is exact, and it **recovers the devices**:

- the three 10 nm rest states together: **0.0434**, flat;
- all four states other than the 2 nm one: **0.0799**, flat;
- the 2 nm state paired with the 5 nm rest state — **its own device** — : **0.0620**, flat;
- the 2 nm state paired with *any* 10 nm state: 0.1086–0.1231, **never** flat.

**The mechanism is visible before any optimiser runs.** The cosine between two states' free-tile
dishing fields says whether one correction can serve both:

| pair | cosine | two-state minimax / stroke |
|---|---|---|
| 2 nm ↔ 10 mM, 10 nm | **−1.000** | 0.1231 |
| 2 nm ↔ 2 mM, 10 nm | −0.984 | 0.1120 |
| 2 nm ↔ 0.5 mM, 10 nm | −0.966 | 0.1086 |
| 2 nm ↔ 2 mM, 5 nm | −0.943 | **0.0620 — flat** |
| every pair among the other four | **+0.949 to +0.997** | 0.0255 – 0.0786, **all flat** |

**And `C-0022` already recorded the physics in one row of its own table.** The 2 nm state is the
**only one of its 21 solved states** whose finite tile carries *less* total electrostatic force than a
1-D pressure over its footprint — **−3.91 %** against +4.9 % to +19.2 % everywhere else — because at a
2 nm gap the rim loses more than the fringing adds. Its rim residual is **+1.087**, above one in
magnitude, so the load **reverses sign** inside the 1 nm rim standoff, against **−0.594** at the design
point. **A distribution that flattens an edge *enhancement* deepens an edge *deficit*.** That is a sign,
and no search removes it.

> The 10 mM state is *not* the antagonist, although it is the one whose smooth collar has the opposite
> sign (**+0.420**, a genuine taper, over a 2.40 nm collar). Its rim residual is **−2.733**, so its
> **net** edge effect is still an enhancement (+8.81 % of force) and its cosine with the design point is
> **+0.987**. It binds at the five-state optimum only because it is the *deepest* of the four
> enhancement states, i.e. the one furthest from the 2 nm compromise.

---

## Deliverable 3 — the operating range, which is the state the requirement is read at

**`C-0022`'s five headline states are four devices.** Three are the *rest* states of three different
buffers at a 10 nm layer; the other two are the *rest* and *held* states of a **5 nm** layer —
`C-0022` labels the 2 nm one *"held at 3 nm stroke"*, so it is the 5 nm device at §3's acceptable
stroke and **no state of the 10 nm device at all**. Asking one distribution to be flat at all five is
asking one tile to be flat across **three buffers and two layer heights**, which nothing upstream
requires.

What a device traverses is one buffer, one layer and one bias, from `L₀` down to `L₀ − s`.
`C-0018` places the 10 nm device at 2 mM with 1–3 % of pull-in margin; `C-0032` recommends 0.5 mM,
where every predicate clears. §3's acceptable stroke is 3 nm, so the 10 nm device traverses gaps
**10 → 7 nm** — and `C-0022` solved **both ends at 0.192 V**.

| device | states | **minimax / stroke** | flat? | uniform | `C-0058`'s rim × 5 |
|---|---|---|---|---|---|
| **2 mM, `L₀` = 10 nm, 0.192 V** (`C-0018`'s placed device) | 10 nm, 7 nm | **0.0373** | **YES** | 0.2182 | **0.0753 — also flat** |
| **0.5 mM, `L₀` = 10 nm, 0.134 V** (`C-0032`'s recommendation) | 10 nm, 7 nm × 2 biases | **0.0435** | **YES** | 0.2086 | **0.0683 — also flat** |
| **2 mM, `L₀` = 5 nm, 0.368 V** | 5 nm, 2 nm | **0.0620** | **YES** | 0.0796 | 0.1867 |
| **10 mM, `L₀` = 10 nm, 0.192 V** | 10 nm, 7 nm × 2 biases | **0.0504** | **YES** | 0.2551 | 0.1179 |

Three things follow.

1. **A robust distribution exists for every device in `C-0022`'s box**, at 0.0373–0.0620 — 1.6–2.7×
   inside the convention, and both endpoints of each range are *active* at its optimum, so these are
   equalised minima and not one state carrying the other.
2. **Where `C-0022` did not solve the compressed end at the device's own bias, the range brackets it**
   with the two neighbouring solved biases (0.082 V and 0.155 V at 7 nm) — a **wider** requirement than
   the device faces, not a narrower one.
3. **The range's discretisation is not what decides it.** Adding two linearly interpolated intermediate
   gaps to the 2 mM device's range moves its optimum's worst case by **exactly zero at nine significant
   digits** — the collar family is smooth in the gap, so two solved endpoints *are* the range.

---

## Deliverable 4 — the cost and the buildability, consumed from `C-0060`

`C-0060` prices a **two-level** coupling: all seven of its catalogue settings reach both levels, the
coarsest quantum is 19.1 % of a level's own stiffness against a **measured** flat window of
`3.5 ≤ R ≤ 20`, and the mandate — an equality on a **sum** — is settable to `1.3e−4` by trimming
individual paths one base pair. What does the robust distribution ask of that?

| design | levels | ratio | inside `C-0060`'s window? | worst / stroke over the range | flat? |
|---|---|---|---|---|---|
| the robust optimum itself | 45 | **26.04** | **no** — above 20 | **0.0373** | YES |
| robust optimum, quantised | 4 | 8.06 | yes | 0.0671 | YES |
| robust optimum, quantised | **3** | 5.57 | yes | **0.0694** | YES |
| robust optimum, quantised | **2** | **3.495** | **no** — 0.14 % below the window's lower edge | **0.0899** | **YES**, by 10 % |
| **`C-0058`'s rim × 5 over 6.70 nm** | **2** | **5.000** | **yes** | **0.0753** | **YES** |

**The failure this task was told to look for is real, and it is not the one the Plan expected: a
45-parameter optimum's quantisability is not a well-posed quantity.** The minimax over a two-state
range has **2 active constraints against 44 free directions**, so its optimal set is a *manifold* and
the search returns *a* member of it. Two builds of this search differing only in the **precision at
which it takes its decisions** (see *Determinism*) returned points of essentially equal objective —
**0.0373** and 0.0372 — whose per-path spreads were **26.0×** and 7.9× and whose optimal two-level
projections were **0.0899** and **0.1002**, i.e. **on opposite sides of `T-5b`'s 0.10**. Nothing about
the problem changed between them.

**What is well posed is the best member of the constrained family, and it must be found by searching
that family rather than by projecting onto it.** `C-0058`'s own published rim × 5 rule — two levels,
ratio exactly 5, the design `C-0060` shows all seven catalogue elements can build — is flat over the
**whole traversed range** of both 10 nm devices, at **0.0753** and **0.0683**, better than either
projection and needing nothing new. **Quantise a design to price it; never to find it.**

> `C-0058`'s rim rule is **not** flat over the 5 nm device's range (0.1867) or the 10 mM one (0.1179).
> Those two devices need a two-level design of their own, and searching for it is *Still open* item 4.
>
> The 45-parameter optimum's own ratio, **26.04**, is *outside* `C-0060`'s measured `3.5 ≤ R ≤ 20`
> window — so even before any quantisation the unconstrained optimum is not a design `C-0060` prices.

### The load paths, at the robust optimum

| | uniform | **robust optimum** | allowable |
|---|---|---|---|
| peak path stiffness [pN/nm] | 0.741 | **1.414** | 3.333 (`= a/s`) |
| **peak path force at the 3 nm stroke** [pN] | 2.222 | **4.243** | **10** — margin **2.36×** |
| peak crossover force [pN] | 0.150 | **1.106** | 10 — margin **9.0×** |
| peak duplex shear [pN] | 0.793 | **1.366** | 48–65 |
| **peak per-path thermal force** [pN] | 0.261 | **0.499** (**+91 %**) | — |

- `C-0049`'s admissible ratio `n·a/(s·K)` is **4.50** at §3's acceptable 3 nm and **1.35** at its
  desired 10 nm, tightening as `1/s` exactly. The robust optimum's stiffest path is **1.91×** the
  uniform share, so it is admissible at 3 nm and **not** at 10 nm — the same wall `C-0049` and `C-0058`
  both hit, reached here from a third direction.
- `C-0014`'s over-stiffening penalty is **linear** in a path's share, so the **+91 %** thermal premium
  is the largest single cost of the non-uniformity, and it is the price of the 5.9× improvement in
  dishing over the uniform coupling.
- The per-path ceiling of 3.3333 pN/nm was active in the search and **is not binding** at the answer.

---

## Determinism — a repair, and a lesson that generalises

`gpd/README.md` requires that a re-run which changes nothing produces no diff, and `CLAUDE.md` records
that rounding at the **serialisation** boundary does not achieve that where a result contains an
argmin. This study met the next member of that family, and it is worth stating plainly.

**Two runs of the first build, differing only in an unused local variable, disagreed in the sixth
significant digit of every searched quantity** — 1 599 lines of the result file — although every input
agreed to the last bit. The cause is arithmetic. A search takes of order `1e6` comparisons; a last-ulp
difference is `1e-15` relative; so the chance that one comparison straddles a **nine**-digit
quantisation boundary is about `1e-6` each, and the expected number of flipped decisions per run is of
order one. One flipped Armijo acceptance moves a descent into a neighbouring basin of an optimal
*manifold*, and the answer changes in the fifth digit while remaining equally optimal.

**The first repair is to decide coarser than you emit.** Every comparison inside the search — the
Armijo acceptance, the *"is this iterate better"* test, the ranking of starts, and the objective handed
to `C-0058`'s optimiser as a polish — is taken on values rounded to **six** significant digits
(`SEARCH_DECISION_DIGITS`), with the earlier candidate winning every tie, while the file is still
emitted at nine. That took the disagreement in the *objectives* from `1e-3` to `1e-6` — and left the
45-component *distributions* still differing by `8e-4`, i.e. the decisions had been stabilised and the
trajectory had not.

**The second repair is that Polak-Ribière is its own amplifier.** Its `beta` numerator is
`g·(g − g_prev)`, a difference of nearly equal vectors once the iteration has settled, so it cancels
catastrophically: an ulp in the gradient becomes an `O(1)` change of search direction, integrated over
a hundred iterations. Two standard guards fix it — restart every ten iterations, and restart whenever
`|g − g_prev|² ≤ 1e-16 |g_prev|²` — together with snapping the iterate itself to a `1e-6` lattice in
the log-weights, which is the same discipline as rounding a decision applied to the state the decisions
accumulate into.

**What that achieves, stated exactly rather than claimed.** Two independent runs of the finished code
differ in **28 lines of about 3 000**. Every verdict, every headline number, all four operating ranges,
the whole 31-subset dichotomy, the entire tension matrix and **all 90 emitted per-path stiffnesses**
are identical. What differs is **two of the roughly forty optimisation runs** — two four-state subsets,
in the sixth and seventh significant digit — one convergence diagnostic, and two `%.1e` noise-floor
values quoted inside finding prose (`6.7e−16` against `0.0e+00`). **The file is therefore not
byte-for-byte identical, and this claim says so rather than asserting a requirement it did not meet.**

The residual is structural, not a missing guard: **a descent on an optimal *manifold* has no isolated
answer to be reproducible about**, and rounding can stabilise which decisions are taken without
stabilising which point of a flat set is reached. Nothing rests on the six digits either — the finest
quantity this claim reports is four, and the search's own convergence tolerances are `1e-5`. **And the
episode is itself a result**: Deliverable 4 uses it as the direct measurement of how wide that manifold
is.

---

## The five verification gates

Executed as **17 gate-named tests** in `src/test/kotlin/coupling/RobustDistributionTest.kt`.

| gate | what was checked | outcome |
|---|---|---|
| **1 — dimensional** | the worst dishing over a one-state set is that state's own peak (`1e−15`); the dishing of every state is exactly **linear in the applied pressure** (`1e−10`); the level quantisation conserves the mandate (`1e−12`) and returns no more levels than asked; unphysical arguments throw — a stiffness vector of the wrong length, a zero stiffness, an empty state set, a state index out of range, an empty load-state list, zero levels, zero smoothing | **PASS** |
| **2 — limiting cases** | **a uniform load on a free tile dishes exactly zero, lattice AND plate** (`< 1e−9 nm`) — the free falsifier; the smoothed objective is an **upper** bound on the true maximum, decreases monotonically as `μ → 0`, and obeys its own `μ ln(2N)` bound; the minimax over a subset is never above the same distribution's worst over a superset; the search is a **descent** and never returns worse than its start, at the mandate exactly | **PASS** |
| **3 — symmetry and conservation** | **Maxwell-Betti reciprocity of the station influence matrix, measured between two different quadratures** (`< 1e−12`); a **point-reflected** distribution dishes identically at every state on the lattice, which is centro-symmetric and not mirror-symmetric (`C-0015`); the softmax parametrisation carries exactly the mandated total at every iterate (`1e−12`) with every stiffness positive | **PASS** |
| **4 — numerical convergence** | **the analytic gradient against a central finite difference at every one of 15 coordinates** (`1e−5` absolute-relative) — the test without which every CG step is unverified; the per-state reachable floor bounds the uniform distribution at every state; and in the study, the sampling grid 41/81/161, **NESTED** subdivisions 1 ⊂ 2 ⊂ 4, the number of starts, the smoothing homotopy and the operating range's own discretisation | **PASS** |
| **5 — literature and upstream cross-check** | **this task's multi-state surrogate against `C-0058`'s independent `InfluenceSurrogate`** (`1.2e−15` on the peak dishing, `1e−10` on the forces and the reachable floor) and against the **assembled** solve (`≤ 1e−11`); `C-0058`'s 0.2182, 0.0753, 0.1867, 0.0710 and 0.2551 reproduced (`≤ 5.8e−4`); `C-0026`'s free-tile stroke 4.90731 nm (`2.1e−7`); `C-0060`'s two levels 0.9208 / 0.1842 re-derived from `C-0058`'s rule; `C-0049`'s ratios 4.50 and 1.35 exactly; `C-0014`'s `√(k_BT k)/n` exactly; `C-0017`'s 33.3333 pN/nm; and **`C-0058`'s 0.1587 required to be matched or beaten**, which it is by 21.0 % | **PASS** |

---

## The declared falsifiers, and what actually happened

| # | falsifier | fired? | outcome |
|---|---|---|---|
| 1 | the largest per-state floor exceeding 0.10, proving no flat distribution exists | **no** | 0.00321, 31× below it — so the five-state negative is a *"not found"* |
| 2 | the multi-state surrogate disagreeing with `C-0058`'s `InfluenceSurrogate` | **no** | `1.2e−15` |
| 3 | the analytic gradient disagreeing with a central finite difference | **no** | it is a test, at 15 coordinates |
| 4 | **the real optimiser failing to beat `C-0058`'s 0.1587** | **no** | 0.1254, and it is asserted in a test |
| 5 | a uniform load producing non-zero dishing on the free tile | **no** | `1.1e−10` nm lattice, `8.9e−15` plate |

**Three predictions of this task's own failed, in code, and are reported rather than repaired.**

1. The Plan expected the **10 mM** state to be the antagonist, because its smooth collar is the one
   whose sign reverses. It is not: its *net* edge effect is still an enhancement and its cosine with the
   design point is +0.987. The antagonist is the **2 nm** state, and the discriminant is the sign of the
   **total force gain**, which `C-0022` reports and no downstream claim had used.
2. The Plan expected the subset structure to be *"contains the 2 nm state"*. It is
   *"contains the 2 nm state **and** a 10 nm state"* — the 2 nm state is compatible with the 5 nm rest
   state, which belongs to the same device. The subset sweep recovered the device partition that
   Deliverable 3 argues for on entirely separate grounds.
3. The Plan expected the buildability question to be answerable as *"the robust design does or does not
   survive `C-0060`'s two levels"*. **The question is not well posed**: the optimum is a member of a
   manifold, and two members of essentially equal objective project onto opposite sides of `T-5b`'s
   line. What replaces it is that the constrained family must be searched in.

---

## Validity range

- **TRL 1–3.** Model-consistent and traceable. **Nothing here is measured**, and the coupling motif is
  not demonstrated (`C-0028`, `C-0029`, `C-0055`).
- **Every station here is `C-0026`'s 3 × 15 grid**, which [`CH-0074`](../challenges/CH-0074-the-flat-distribution-lives-on-stations-no-placement-supplies.md)/`C-0061` show no
  placement claim supplies. [`C-0063`](C-0063-upward-root-placement.md) (`T-125`, this same iteration)
  **resolves `CH-0074` the other way** — a 34-root placement on `C-0055`'s upward lattice is flat at
  **0.0706 with equal springs** — but that is a **different station set**, and `C-0058`'s rim rule
  **reverses sign** on it (0.0706 uniform against 0.2214 at ×5). **So no distribution in this claim
  transfers to `C-0063`'s placement**, and the state-robustness question is **open** there: `C-0063`
  reports its 0.0706 at **one** state, the same design point `C-0058` was tuned at, and this claim's
  whole finding is that a flatness verdict at one state does not travel. The machinery here is exactly
  the instrument for it; see *Still open* item 1.
- **The load profiles are `C-0022`'s** and inherit its whole validity range: mean field, point ions, a
  2-D solve with the corner **bracketed rather than solved**, an **unsourced rim charge** worth 1.85×
  on the collar, and a gap filled with free buffer. The 2 nm state's **sign** — the whole of
  Deliverable 2 — is a −3.91 % total-force gain, i.e. a small difference of two larger numbers, and it
  is the most exposed number in this claim to that rim charge.
- **The 45-parameter optima are members of a manifold, not points.** Their per-path spread, their
  quantisability and every other property that is not the objective itself are **not stable across
  equally optimal members**, and Deliverable 4 measures how far that goes. The objective, the subset
  structure and the range verdicts are stable across both builds observed.
- **The interpolated intermediate gaps are linear interpolations** of `C-0022`'s solved
  `(depth, width, rim)` triples between two solved endpoints. They are a **discretisation check**, not
  new solves, and no verdict rests on them.
- **Where `C-0022` did not solve a device's compressed end at its own bias**, the range brackets it with
  the two neighbouring solved biases. That is a wider requirement, not a narrower one.
- **Linear Winkler foundation at `C-0001`'s secant, ×1 only.**
- **The coupling is `n` independent linear springs.** `C-0030`'s flexure strain-softens (`CH-0042`), so
  a real coupling is not exactly this one.
- **The optimiser is a DESCENT** reporting the best point it found, never a global optimum. The
  per-state bound is rigorous; the optimum is not. **The five-state negative is a "not found at a large
  budget", and the tight relaxation — a semidefinite programme over the common compliance operator
  `T = (M + diag(1/k))⁻¹`, which is what would make it a theorem — was not implemented.**
- **One crossover layout** — `T-10`'s eight symmetrically centred columns; `C-0015`'s 32 bp phase is not
  swept here (`C-0063` sweeps it, on a different station set).
- **`T-5b`'s 10 % is a CONVENTION.** At 5 % the 2 mM and 0.5 mM device ranges survive (0.0373, 0.0435)
  and the 5 nm one does not (0.0620); `C-0058`'s buildable rim rule survives at 8 % and not at 5 %.
- **Single layer, static, 300 K, aqueous buffer with Mg²⁺.** No electrostatics is solved here and no
  lateral coordinate is carried; the dishing is out-of-plane only.

## Numbers that are CITED rather than DERIVED

| number | value | flag |
|---|---|---|
| interhelical distance | 2.69 nm | **CITED, MEASURED**, Fischer et al. (2016), SAXS |
| duplex `EI` | 230 pN·nm² | **CITED, a CanDo MODEL INPUT**, not a measurement |
| crossover hinge stiffness `k_θ` | `α = 1` | **CITED, FITTED**, Chen et al. (2014) SI |
| crossover interface spacing | 32 bp | **CITED** via `C-0015` |
| `C-0022`'s solved collars | 21 states | **CITED**, read at run time from `gpd/results/T-3b-tile-edge-load-profile.json`, keyed on `(concentration, gap, bias)` |
| `C-0022`'s **total force gains** (+4.9 % … +19.2 %, and **−3.91 %** at 2 nm) | | **CITED** from `C-0022`'s own table — the discriminant of Deliverable 2 |
| `C-0060`'s flat ratio window | `3.5 ≤ R ≤ 20` | **CITED**, measured there |
| `C-0060`'s quantum / scatter threshold | 1.0–19.1 % / 34.6 % | **CITED** |
| `C-0018`/`C-0032`'s operating point | 10 nm layer, 2 mM at 1–3 % pull-in margin, 0.5 mM clearing | **CITED** |
| `C-0058`'s minimax | 0.1587 | **CITED**, and beaten |
| `C-0058`'s single-state optimum | 0.0544 | **CITED**, and beaten by 2.54× |
| `C-0063`'s flat placement | 0.0706, equal springs | **CITED** |
| `C-0017`'s mandate | 33.3333 pN/nm | **CITED**, itself §3 arithmetic |
| per-path unzip allowable | 10 pN | **CITED** via `C-0006`/`CH-0029` |
| duplex shear allowable / nicked ceiling | 48 / 65 pN | **CITED** via `C-0006` |
| `RIGID_PLATE_TOLERANCE` | 0.10 | **CITED CONVENTION** from `T-5b` |
| §3 parameters | 100 pN, 3 nm, 10 nm, 40 × 40 nm | **CITED** |

Everything else — the multi-state surrogate and its reciprocity, the analytic gradient, the per-state
bound, the whole subset sweep, the tension matrix, the operating ranges, the quantisation and the load
paths — is **derived here in code**, with `C-0058`'s pipeline **re-run rather than tabulated**.

## Still open — named, not answered

1. **Whether `C-0063`'s flat placement is flat over a RANGE.** `C-0063` reports 0.0706 at **one**
   state — the same design point `C-0058` was tuned at — and this claim's whole finding is that a
   one-state flatness verdict does not travel. The multi-state surrogate and the minimax here are the
   instrument; the 34 upward roots are the station set. **This is the largest open item this claim
   leaves, and it is cheap.** Filed as `T-129`.
2. **Whether the two-state incompatibility is a THEOREM.** The bound used here decouples the states and
   is therefore loose. The tight relaxation is a semidefinite programme over the common compliance
   operator `T = (M + diag(1/k))⁻¹`, which is convex and was not implemented.
3. **Whether a Gen-1 device is required to run in more than one buffer.** If it is, the five-state
   answer is the one that governs and it is negative; if it is not, the range answer governs and it is
   positive. **Nothing in §3 says which**, and that is a specification gap, not a modelling one.
4. **A TWO-LEVEL design searched over each range** rather than projected onto it. `C-0058`'s ratio 5 is
   flat over both 10 nm ranges at 0.0753 and 0.0683 and is *not* flat over the 5 nm device's (0.1867) or
   the 10 mM one (0.1179); `C-0060` found ratio **7** better at the design point. The best two-level
   member *of the range problem* is not known, and Deliverable 4 says it is the only well-posed
   buildable question here.
5. **The foundation multiplier**, held at `C-0001`'s secant throughout.

## Challenges

**Raises [`CH-0077`](../challenges/CH-0077-five-solved-states-are-four-devices.md)** against `C-0058`'s
Deliverable 4 and `CH-0071`'s overturning condition 2 — that *"a requirement that one distribution be
flat at every operating state"* is not the requirement those five states express, because they are four
devices. **No number of `C-0058`'s moves**; every one is reproduced here.

**None stands against this claim.** The four ways it would fail:

1. **A demonstration that a Gen-1 device must be flat across buffers.** Then the five-state negative is
   the governing answer and the range result is a weaker statement about a narrower duty.
2. **A `C-0022` rim charge materially different from the solved one.** Deliverable 2's whole
   discriminant is a −3.91 % force gain, and `C-0022` names its rim charge as unsourced and worth 1.85×.
3. **A tolerance materially tighter than `T-5b`'s 10 %.** At 5 % the 5 nm device's range fails and the
   buildable rim rule fails.
4. **A placement result that supersedes `C-0026`'s grid** — which `C-0063` already is. Every number here
   is owed on the 3 × 15 stations and none of it transfers.

A further result contradicting this claim should be raised in `gpd/challenges/` with methodological
grounds rather than overwriting it.
