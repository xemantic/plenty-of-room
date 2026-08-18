# C-0111 — The answered material collapses the bracket onto its adverse end and moves nothing; the unanswered PZC is 90–576× the deciding scale, and of the wrong sign

| | |
|---|---|
| **Task** | [`T-193`](../tasks/T-193-gold-electrode-pzc.md) |
| **Leaf** | `A1.2` (the 3.0 nm positional bound, read at zero bias), inheriting [`C-0021`](C-0021-zero-bias-resting-position.md) |
| **Verification type** | **logical** (the model's *"applied bias"* identified with the rational potential `E − E_σ=0`, by reading the code) **+ in-silico** (`C-0021`'s van der Waals term and `C-0008`'s gap field re-run at gold alone, the field parametrised by the **diffuse drop** rather than by the bias, and `C-0021`'s three published thresholds reproduced from that opposite direction) **+ literature** (a primary, open-access, directly-read `E_pzc` for Au(111) in 1 mM aqueous electrolyte) |
| **Verdict** | **Half 1 is bookkeeping and confirms `C-0021`: gold is the *stiffest* of the four candidate electrodes, so the 2.6× bracket collapses onto its ADVERSE end (10.356–17.159 pN at 5 nm on a 2 nm tile, 0.737–1.422 at 10 nm), the state's own bracket narrows by exactly 3.25905934×, the deepest gold well in the box is 8.742 `k_BT` against the 10 `k_BT` criterion, and 0 of 6 gold states confine. No verdict moves; one GROUND does. Half 2 answers `C-0021`'s second open question: `E_pzc(Au(111)) = 0.46–0.51 V vs SHE` in 1 mM HClO₄, which is 90.2–575.7× `C-0021`'s own thermal-scale threshold and NEGATIVE in rational potential — an electrode at zero volt on that scale is negatively charged and LIFTS the tile rather than holding it down.** |
| **Maturity** | **TRL 1–3. Model-consistent and traceable. NOTHING HERE IS MEASURED BY THIS PROJECT.** The `E_pzc` is somebody else's measurement, on a Au(111) single crystal in 1 mM HClO₄, not on a template-stripped film in MgCl₂. |
| **Result** | [`gpd/results/T-193-gold-electrode-pzc.json`](../results/T-193-gold-electrode-pzc.json) |
| **Sources** | [`gpd/data/T-193-sources/`](../data/T-193-sources/) — manifest, three retained drivers, 32 recorded queries, nine recorded fetches |
| **Consumes** | [`C-0021`](C-0021-zero-bias-resting-position.md) (`M3`, `M4`, the thermal scale, the whole field library), [`C-0008`](C-0008-electrostatic-force-and-decay-length.md), [`C-0005`](C-0005-mean-field-screening-validity.md) (the point-ion boundary, on **both** signs) |
| **Raises** | [`CH-0128`](../challenges/CH-0128-inverse-debye-length-called-with-a-bjerrum-length.md) against `C-0021` and `C-0023` — a **methodological** challenge on the code that produced their van der Waals low end, worth 0.93 % at 5 nm and 0.073 % at 10 nm, moving no verdict |
| **Answers** | `C-0021`'s open question 2 (*"The electrode's potential of zero charge … This is a MEASUREMENT, not a calculation"*), and it is answered **as a measurement, by somebody else** |

---

## 1. The identification that makes the whole question well posed

`diffusePotentialOfAppliedBias` solves

&nbsp;&nbsp;&nbsp;&nbsp;`V = ψ_d + σ_e(ψ_d)/C_S`,

the diffuse drop and the compact drop in series.
With no tile present, `V = 0` makes the whole interfacial drop vanish —
which is the *definition* of an electrode carrying no free charge.

> **The model's `appliedBias` is the RATIONAL potential `E − E_σ=0`, not a potential against any reference electrode.**

Every *"zero bias"* statement in this programme is therefore a statement about that quantity and about no other,
and `C-0021`'s contact-potential table is a table of **rational** potentials.
This is a reading of forty lines of existing code; it costs nothing, and it decides the shape of the answer:
the missing quantity is an `E_pzc`, and *"what contact potential does the device sit at"* is `E_zero − E_pzc`,
where `E_zero` is whatever the drive electronics define as zero.

It is confirmed arithmetically by the standing numbers: at `V = 0` and a 5 nm gap `C-0021` emits
`ψ_d = −0.0116400155 V` and `σ_e = +0.0145289304 e/nm²`, and `σ_e/C_S` at
`C_S = 1.24830181 e/(V·nm²)` is `+0.011639`, which cancels `ψ_d` to the emitted precision.

---

## 2. Half 1 — gold alone, and what collapses

`C-0021`'s `M4` re-run with the electrode set reduced to gold, beside the four-material span at the same state.

| gap | tile | **gold** [pN] | four materials [pN] | four-material ratio | gold ratio | narrowing | well [`k_BT`] | confines |
|---|---|---|---|---|---|---|---|---|
| 5 nm | 2 nm | **10.356 – 17.159** | 3.436 – 18.553 | 5.400 | 1.657 | **3.259** | 4.817 | no |
| 5 nm | 10 nm | 15.691 – 25.997 | 5.206 – 28.110 | 5.400 | 1.657 | 3.259 | **8.742** | no |
| 7 nm | 2 nm | 2.967 – 5.210 | 0.984 – 5.633 | 5.722 | 1.756 | 3.259 | 1.871 | no |
| 7 nm | 10 nm | 5.213 – 9.152 | 1.729 – 9.895 | 5.722 | 1.756 | 3.259 | 3.932 | no |
| 10 nm | 2 nm | **0.737 – 1.422** | 0.245 – 1.537 | 6.284 | 1.928 | 3.259 | 0.646 | no |
| 10 nm | 10 nm | 1.531 – 2.953 | 0.508 – 3.193 | 6.284 | 1.928 | 3.259 | 1.585 | no |

Three statements, and only the third is new.

1. **The collapse is onto the adverse end.** Gold has the largest Hamaker constant of the four,
   so naming it removes the *optimistic* half of the bracket and keeps the pessimistic one.
   `C-0021`'s gold row stops being the top of a four-material range and becomes the number.
2. **No verdict moves, and this is verified rather than repeated.** The deepest gold well anywhere in the box is
   **8.742 `k_BT`** — at the 5 nm gap on §3's *thick* tile reading, the most favourable corner for confinement —
   against `C-0021`'s declared 10 `k_BT` criterion. **0 of 6.**
   The reason is structural and survives any material: a `1/h³` force integrates to a **bounded** potential,
   and naming the electrode changes the amplitude, never the exponent.
3. **A ground moved where the verdict did not.** `C-0021` records that its retardation factor is
   *"sourced for gold only and applied across the whole electrode bracket, and that substitution is stated
   rather than justified."* At gold alone **there is no substitution**: the factor is the material's own.
   The other half of that caveat stands — the DNA constant is already retarded, so the retarded reading
   retards that half twice and the low end remains a **lower** bound, not an estimate.

The narrowing is **exactly 3.25905934× at every one of the six states**, the material entering the ratio
as a pure factor. That constancy is what led to §5.

---

## 3. Half 2 — the potential of zero charge, read directly

**Adnan, Behjati, Félez-Guerrero, Ojha & Koper, *Phys. Chem. Chem. Phys.* **26**:21419 (2024)**,
DOI `10.1039/d4cp02133a`, open access, EuropePMC `PMC11323936`, **READ DIRECTLY**.
Au(111) single crystal, flame-annealed, hanging meniscus, **Ar-saturated 1 mM HClO₄**, 20 mV/s,
`E_pzc` taken as the capacitance minimum:

| surface | vs RHE | vs SHE (the source's own conversion) | re-derived at 300 K | departure |
|---|---|---|---|---|
| thermally reconstructed | 0.69 V | **0.51 V** | 0.511420712 | +1.42 mV |
| potential-induced reconstruction | 0.674 V | **0.497 V** | 0.495420712 | −1.58 mV |
| unreconstructed, with adatom islands | 0.64 V | **0.46 V** | 0.461420712 | +1.42 mV |

**The source prints every value twice, on two scales, and the Nernst relation joins them** —
so the pair is a transcription check that a mis-read digit would break by 10×.
At pH 3.0 (1 mM strong monoprotic acid) and this project's 300 K the slope is 59.52643 mV,
against the 59.16 mV at 298.15 K where the source took it; the 1.4–1.6 mV residual **is** that
0.4 mV per pH unit, and it is carried rather than absorbed. Asserted at 5 mV as a gate-5 test.

**Corroboration, independent paper, READ DIRECTLY:** Liu, Doblhoff-Dier & Koper,
*ACS Electrochem.* **2**:995 (2026), DOI `10.1021/acselectrochem.5c00544`:
*"E_pzc values in the literature for Au(111) are around 0.5 V vs. SHE … while for Au(110) they are around
0.2 V vs. SHE"*. It is a literature consensus rather than a fourth measurement, so it is used as a
**cross-check** and not as a reading.

**Why Au(111) is the right proxy for the answered material, READ DIRECTLY:** Avedian, Trang & Inkpen,
*ACS Nanosci. Au* **5**:269 (2025): their template-stripped electrodes show a gold-oxidation component
*"that we attribute to oxidation of Au(111) crystal facets. Crystallites with this orientation are expected
to dominate the Au TS surface"*, at *"a root-mean-squared roughness of &lt;0.5 nm over micrometer length scales"*.

**What is NOT found:** no `E_pzc` of gold in MgCl₂, or in any divalent-cation aqueous electrolyte,
and none for a template-stripped film as such. **32 recorded queries**, and the negative result carries its
expected yield rather than only its budget: the divalent leg is not empty on its own
(11 + 27 + 10 = 48 records across three phrasings) and **none of them is a gold PZC in a divalent electrolyte**.

---

## 4. What the measured PZC does to `C-0021`'s zero-bias state

`C-0021`'s three thresholds, reproduced here from the **opposite direction** — the diffuse drop imposed and
the bias read out, rather than the bias bisected through the Stern series:

| gap | thermal-scale **LIFT** | no net force | thermal-scale **HOLD-DOWN** | `C-0021`'s value | departure |
|---|---|---|---|---|---|
| 5 nm | −1.639 mV | −0.368 mV | **+0.885908482 mV** | 0.885908166 | `3.6e-7` |
| 7 nm | −2.925 mV | −0.509 mV | **+1.84292354 mV** | 1.84292351 | `1.7e-8` |
| 10 nm | −6.087 mV | −0.314 mV | **+5.10177544 mV** | 5.10177542 | `4.0e-9` |

Three things follow, and the second is the one that matters.

- **The whole sign structure of the residual field lives inside 11.2 mV of rational potential** at the 10 nm
  layer, and inside 2.5 mV at 5 nm. At exactly the PZC the force is a *hold-down* — the tile's own
  countercharge is on the electrode — but a **negative** rational potential of a third of a millivolt
  already cancels it, and one of six millivolts reverses it to a thermal-scale **lift**.
- **The measured PZC is 90.2–575.7× that scale, and it is on the lifting side.** An electrode held at
  0 V on the SHE scale sits at a rational potential of **−0.46 to −0.51 V**: negatively charged, repelling
  the negatively charged tile. `C-0021`'s `M3` row reads *"DOWN but negligible"*, and that is a statement
  about an electrode **at** its PZC — which `C-0021` says in as many words and no downstream reader has.
  Gold's PZC is high, near the positive end of the aqueous window, so **almost any nominal zero — a
  potentiostat at 0 V against a common aqueous reference, or a cell at open circuit — sits below it.**
- **The offset is outside the model, and that is the answer rather than a gap in it.** `C-0005`'s point-ion
  boundary is **0.0973945485 V** of diffuse drop at a **negative** electrode (Mg²⁺ is the counterion there
  and the boundary goes as `1/z`) against 0.196568167 V at a positive one, and **9 of 9** exposure states
  fall outside it. **No force is quoted at the PZC offset.** The honest statement is the threshold:

> **The electrode must be held within 5.10 mV of its own potential of zero charge at the 10 nm layer,
> and within 0.886 mV at 5 nm, for the residual field to stay at the thermal scale.
> That is a control requirement on the drive electronics, not a property of the material.**

---

## 5. A defect in the ground the low end rests on, worth under one per cent

The narrowing in §2 is **exactly** constant across a factor of two in gap, which it cannot be:
the zero-frequency term is **10.6 %** of gold's cross constant and 24.6 % of alumina's,
so a gap-dependent screening cannot divide out of a ratio between them.

**Cause.** `C-0021` and `C-0023` both write `buffer.inverseDebyeLength(lb)`, and that method's **first**
parameter is a **temperature**. The Bjerrum length evaluated at 0.714 K gives `κ = 5.21953283 nm⁻¹`
where the buffer's own default gives **0.254655191** — the documented 3.93 nm at 2 mM — a factor of **20.5**,
and `e^(−2κd)` then saturates to `2e−23` at 5 nm against 0.0783513635.

**Consequence, and it is small.** It is used in exactly one place, the zero-frequency screening of the
**low end** of the van der Waals bracket, and there it annihilates that term — which lands the low end
exactly on *"fully screened"*, which is what `C-0021`'s own prose declares the low end to be.
So the emitted number is right for the stated bracket and the expression that produces it is not.
At the Debye `κ` the gold low end rises by **0.93 % at 5 nm, 0.34 % at 7 nm and 0.073 % at 10 nm**.
No verdict moves. Filed as [`CH-0128`](../challenges/CH-0128-inverse-debye-length-called-with-a-bjerrum-length.md)
rather than repaired here, because repairing it moves two committed result files and belongs with their re-emission.

**Two call sites, and only two**: every other `inverseDebyeLength` call in the tree uses the default.

---

## 6. Numbers, and how each was obtained

| quantity | value | flag |
|---|---|---|
| `E_pzc` Au(111), thermally reconstructed | 0.51 V vs SHE (0.69 vs RHE), 1 mM HClO₄ | **CITED, MEASURED, READ DIRECTLY** — Adnan et al. 2024 |
| `E_pzc` Au(111), potential-induced reconstruction | 0.497 V vs SHE (0.674 vs RHE) | **CITED, MEASURED, READ DIRECTLY** — same source |
| `E_pzc` Au(111), unreconstructed with islands | 0.46 V vs SHE (0.64 vs RHE) | **CITED, MEASURED, READ DIRECTLY** — same source |
| `E_pzc` Au(111), literature consensus | ~0.5 V vs SHE | **CITED, READ DIRECTLY** — Liu et al. 2026; a cross-check, not a reading |
| `E_pzc` Au(110), literature consensus | ~0.2 V vs SHE | **CITED, READ DIRECTLY** — same source |
| template-stripped gold is (111)-dominated, rms < 0.5 nm | qualitative | **CITED, READ DIRECTLY** — Avedian et al. 2025 |
| `E_pzc` of gold in MgCl₂ or any divalent electrolyte | — | **NOT FOUND**, 32 recorded queries with their marginal rates |
| Trasatti's absolute electrode potential | — | **NOT OBTAINED** (IUPAC `403`, De Gruyter empty `202`) and **not needed**: nothing here uses the vacuum scale |
| Nernst slope at 300 K | 0.0595264293 V/decade | **DERIVED**, `(k_BT/e) ln 10` from locked constants |
| the three contact-potential thresholds | 0.886 / 1.843 / 5.102 mV | **DERIVED**, and reproduced against `C-0021` at `3.6e-7` or better |
| gold van der Waals force and well depth | table in §2 | **DERIVED**, `C-0021`'s own library, reproduced at `5.0e-10` or better |
| the point-ion boundaries | 0.196568167 / 0.0973945485 V | **DERIVED**, `C-0005`'s expression at this buffer |
| the screening-argument defect | 0.93 % / 0.34 % / 0.073 % | **DERIVED**, and asserted as a test |

---

## 7. Validity range

- **TRL 1–3. Nothing here is measured by this project.**
- **The `E_pzc` is measured on a Au(111) single crystal in 1 mM HClO₄, not on a template-stripped film in
  MgCl₂**, and two exposures follow which run **opposite** ways.
  1. A template-stripped film is (111)-**dominated** but not (111)-only, and Au(110) sits 0.3 V lower,
     so the film's own facets spread further than the three readings here do —
     a heterogeneity *within one electrode* of 59–330× the deciding scale.
  2. Chloride adsorbs specifically on gold and shifts a PZC **negatively**; the size of that shift in
     MgCl₂ is **NOT SOURCED** and is the one number this task could not find.
- **Both exposures move the PZC toward the finding, not against it.** They make the rational potential at
  0 V vs SHE smaller in magnitude, and the natural scale for how much smaller is the 0.3 V facet spread,
  which still leaves ≥ 0.2 V — **39–226×** the deciding threshold, and of the same sign.
  The verdict is robust to both.
- **No force is quoted at the PZC offset**, because it is outside `C-0005`'s point-ion boundary on the
  tighter (negative-electrode, Mg²⁺) side at 9 of 9 states. A number there needs a Stern-dominated model
  this programme has not built.
- The van der Waals half inherits every validity note of `C-0021`'s `M4` unchanged, minus the
  electrode-material bracket, which NDI's answer closes, and plus `CH-0128`'s 0.93 %.
- **`C-0021`'s six reproduction targets are transcribed, not read.** The study hardcodes them rather than
  reading `gpd/results/T-13-zero-bias-resting-position.json`, deliberately — it adds no read edge to the
  dependency graph and cannot go stale silently — and every one of the six was grepped out of that file
  before being written down. The cost is that a re-emission of `T-13` will not propagate here, which is what
  `CH-0128` would cause, so the six targets must be re-grepped when it is discharged.
- The field is `C-0021`'s own library at `C-0021`'s own node counts (4000 for a force, 400 for the Stern
  inversion). This is a **re-read**, not a new model.
- **`E_zero` is a specification quantity, not a physical one.** Everything in §4 is stated on the SHE
  scale precisely so that no reference-electrode conversion constant is needed; which scale the Gen-1
  drive defines its zero on is §8.

---

## 8. Acceptance verdict

**PASS.** All four predicates of `T-193` hold:

| predicate | verdict |
|---|---|
| (a) the gold-alone re-read reproduces `C-0021`'s gold rows | **PASS** — `5.0e-10`, `1.4e-10`, `6.0e-10` on force and well depth |
| (b) whether any verdict moves is verified, and moved grounds are named | **PASS** — 0 of 6 gold states confine; the retardation caveat is half discharged; `CH-0128` names a moved ground |
| (c) a primary `E_pzc` with its reference electrode and conditions, or a recorded absence | **PASS** — both: a directly-read measurement for Au(111)/1 mM HClO₄, and a query-recorded absence for gold/MgCl₂ |
| (d) a threshold where a number could not be sourced | **PASS** — 5.10 mV at 10 nm, 0.886 mV at 5 nm, and the 0.46 V chloride shift that would be needed to reach it |

Ten tests, all passing, in `src/test/kotlin/anchoring/ElectrodePotentialOfZeroChargeTest.kt`.
The result file is **byte-identical across two runs**.
The full suite is green — **2 486 tests in 133 classes, 0 failures** — on a snapshot of the working tree
taken at 14:00 with three files a sibling agent had left mid-TDD dropped:
`src/test/kotlin/structure/InteriorCrossoverPrestrainTest.kt`,
`src/main/kotlin/actuator/TallGapDeviceBStudy.kt` and `src/test/kotlin/actuator/TallGapDeviceBTest.kt`.

---

## 9. What this returns to NDI — one line

> **Is *"zero bias"* in the Gen-1 cell 0 V against a named reference electrode, the cell at open circuit,
> or two identical electrodes shorted? Gold's potential of zero charge is 0.46–0.51 V vs SHE, and the
> residual field is only negligible within ~1–5 mV of it, so the three answers are hundreds of millivolts
> apart and they differ in the SIGN of the force at rest.**

It belongs under **decision 3** of `DECISIONS-FOR-NDI.md`, as the residue of the half that answer left open.
It is a **specification** question, not a modelling one: no calculation closes it,
because what is missing is a statement about the cell rather than a number about the physics.

## 10. Still open

- The chloride-induced PZC shift for gold in MgCl₂. Bounded rather than unbounded — the Au(111)/Au(110)
  facet spread of 0.3 V is the natural comparison — and 0.46 V is what it would take to reach the
  deciding scale.
- Whether a template-stripped film's facet distribution has ever been converted into an *effective* PZC.
  Liu, Doblhoff-Dier & Koper (2026) build exactly that model and do not apply it to a template-stripped film.
- The force at a rational potential of a few hundred millivolts. Outside point-ion Poisson-Boltzmann
  on the negative-electrode side; it needs a Stern-dominated or a strong-coupling treatment.
