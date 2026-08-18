# `T-193` sources — where does template-stripped gold's potential of zero charge sit?

Every load-bearing number below carries a **read status**, per `CLAUDE.md`'s research practice.
`gpd/data/` was checked **before** anything was fetched, and this time it did **not** pay:
the corpus carries no electrochemistry at all,
and a `grep` for *"potential of zero charge"*, *"pzc"*, *"template strip"*, *"Au(111)"* and *"contact potential"*
over `gpd/`, `src/` and `third-party/` returns only `C-0021` itself and the two claims that quote it.
That is a negative result worth recording:
the electrode's *interface* has never been a source-backed object in this repository.

## Already in the repository — checked first

| file | source | used for | read status |
|---|---|---|---|
| `../../claims/C-0021-zero-bias-resting-position.md` | this project | `M3`'s threshold table, `M4`'s four-material bracket, the thermal scale | **READ DIRECTLY** |
| `../../results/T-13-zero-bias-resting-position.json` | this project | the six gold van der Waals rows and the three contact-potential thresholds this task reproduces | **READ DIRECTLY** |

## Fetched here

| file | source | used for | read status |
|---|---|---|---|
| `PMC11323936-fullTextXML.xml` | Adnan, Behjati, Félez-Guerrero, Ojha & Koper, *Phys. Chem. Chem. Phys.* **26**:21419 (2024), DOI `10.1039/d4cp02133a` | **the measurement this task is about.** Au(111) single crystal, flame-annealed, hanging meniscus, Ar-saturated **1 mM HClO₄**, 20 mV/s, capacitance minimum: *"The first cycle has a capacitance minimum at 0.69 V vs. RHE (0.51 vs. SHE) … in the negative-going scan, the capacitance minimum is around 0.64 V. vs. RHE (0.46 vs. SHE)"*; *"we conclude that the PZC of the potential-induced surface reconstruction of Au(111) is 0.674 V vs. RHE (0.497 V vs. SHE), whereas the PZC of a Au(111) (with islands resulting from the lifting of the reconstruction) is 0.64 V vs. RHE (0.46 V vs. SHE)"* | **READ DIRECTLY** |
| `PMC13051447-fullTextXML.xml` | Liu, Doblhoff-Dier & Koper, *ACS Electrochem.* **2**:995 (2026), DOI `10.1021/acselectrochem.5c00544` | the independent cross-check and the facet spread: *"E_pzc values in the literature for Au(111) are around 0.5 V vs. SHE, while for Au(110) they are around 0.2 V vs. SHE"*; and the whole paper's subject — that a **polycrystalline** electrode's capacitance minimum is not its facets' PZC, deviating *"by several tens of meV"* even in the favourable regime | **READ DIRECTLY** |
| `PMC12371504-fullTextXML.xml` | Avedian, Trang & Inkpen, *ACS Nanosci. Au* **5**:269 (2025), DOI `10.1021/acsnanoscienceau.5c00018` | that the answered material is the right proxy for Au(111): *"a principal component at ∼1.24 V (vs Ag/AgCl) that we attribute to oxidation of Au(111) crystal facets. Crystallites with this orientation are expected to dominate the Au TS surface"*, and *"a root-mean-squared roughness of &lt;0.5 nm over micrometer length scales"* | **READ DIRECTLY** |
| `PMC12276039-fullTextXML.xml` | Schalenbach, Tempel & Eichel, *ChemPhysChem* **26**:e202401088 (2025) | a 2025 reassessment of reading a PZC off a capacitance minimum at all — consulted, and it is the reason this task quotes the single-crystal value rather than a polished-gold one | **READ DIRECTLY**, not load-bearing |
| `PMC10751779-fullTextXML.xml` | Tang, Zhao & Huang, *JACS Au* **3**:2837 (2023) | Au(111) PZC across **non-aqueous** solvents (0.13–0.51 V vs SHE) — consulted; it does not carry the aqueous value | **READ DIRECTLY**, not load-bearing |
| `PMC11613321-fullTextXML.xml`, `PMC11804923-fullTextXML.xml` | Doblhoff-Dier & Koper double-layer review; single-crystal-gold CO₂ reduction | consulted for a second aqueous Au(111) `E_pzc`; neither states one | **READ DIRECTLY**, not load-bearing |
| — | Trasatti, *Pure Appl. Chem.* **58**:955 (1986), the absolute electrode potential | **NOT OBTAINED** — IUPAC's own PDF host returns `403` and De Gruyter returns an empty `202`. It is not needed: nothing here uses the vacuum scale, and every value is quoted against a named aqueous reference electrode. Recorded in `fetches.json` | **NOT FOUND** |

## What is NOT found, and the expected yield of the search that failed to find it

**No published `E_pzc` of gold in `MgCl₂`, or in any divalent-cation aqueous electrolyte, was found.**

Per `CLAUDE.md` — *"a negative existence result over a SEARCH needs its expected yield, not just its budget"* —
the marginal rates were measured before the conclusion was drawn.
The conjunction is (a PZC measurement) ∧ (a gold electrode) ∧ (a divalent cation),
and the three legs are **not** independently empty:

| leg | query | hits |
|---|---|---|
| PZC ∧ gold | `"potential of zero charge" AND gold` | 214 |
| PZC ∧ Au(111) | `"potential of zero charge" AND "Au(111)"` | 100 |
| PZC ∧ MgCl₂ | `"potential of zero charge" AND "MgCl2"` | 11 |
| PZC ∧ divalent ∧ electrode | `"potential of zero charge" AND "divalent" AND electrode` | 27 |
| PZC ∧ magnesium ∧ gold | `"potential of zero charge" AND "magnesium" AND gold` | 10 |

So the divalent leg has power — 48 records across three phrasings —
and **none of them is a gold PZC in a divalent electrolyte**;
every one is a battery, a capacitive-deionisation, a membrane or a sensor paper that mentions `MgCl₂` elsewhere.
The absence is a real absence, not an empty search.

**Nor is there a published `E_pzc` for a template-stripped film as such** —
the film's characterisation literature reports roughness and facet texture and stops there.

## Query log

- `query.py` — 20 EuropePMC queries in seven named families (the quantity, the surface, the electrolyte,
  the absolute scale, the DNA-on-a-biased-electrode device context, open-circuit and work-function routes,
  and the capacitance-minimum route). Results in `europepmc-queries.json`, console log in `query.log`.
- `targeted.py` — 12 further queries for what the keyword sweep did not surface,
  including the chloride-shift and template-stripped-texture questions. Results in `targeted-queries.json`.
- `fetch.py` — every fetch attempt with its URL, HTTP status and byte count, in `fetches.json`.
  Two refusals (IUPAC `403`, De Gruyter empty `202`) are recorded rather than dropped.
- `strip.py` — renders each `PMC*-fullTextXML.xml` as a flat `PMC*.txt` for grepping.
  Those `.txt` files carry no information the XML does not; they exist so that every verbatim
  sentence quoted in `C-0111` could be checked with one `grep` before it was written down,
  which is how all five were.

All three drivers are retained and re-runnable.
