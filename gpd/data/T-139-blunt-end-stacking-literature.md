# T-139 — Blunt-end coaxial stacking between two DNA duplexes: literature retrieval

Retrieved 2026-08-17.
Every number carries a flag: **`read directly`** (the sentence or table cell was fetched and read in this session),
**`abstract only (verbatim)`** (the publisher's own abstract text was fetched from Crossref or EuropePMC and read; the full text is paywalled),
**`derived here`** (arithmetic performed in this session on numbers that are themselves `read directly` — not a quantity any paper states),
**`not found`**.

A search-engine snippet is used nowhere in this file.

---

## 1. Summary table

### 1a. Free energy of one coaxial / blunt-end stack

| # | Quantity | Value | Conditions | Source (and where the number sits) | Flag |
|---|---|---|---|---|---|
| 1 | ΔG_ST per nicked stack, A•T contacts | AT −1.31, AA/TT −1.08, TA −0.19 kcal/mol | 37 °C, 1×TBE ([Na⁺] = 15 mM) | Yakovchuk, Protozanova & Frank-Kamenetskii, *NAR* **34**:564 (2006), doi 10.1093/nar/gkj454, Supplementary Table 2 | `read directly` |
| 2 | ΔG_ST per nicked stack, G•C contacts | GC −2.06, GG/CC −1.41, CG −0.89 kcal/mol | 37 °C, 1×TBE ([Na⁺] = 15 mM) | same, Supplementary Table 2 | `read directly` |
| 3 | ΔG_ST at room temperature (A•T contacts only measured) | AT −1.70, AA/TT −1.44, TA −0.50 kcal/mol | 22 °C, 1×TBE ([Na⁺] = 15 mM) | same, Supplementary Table 2 | `read directly` |
| 4 | ΔG_ST at physiological monovalent salt | AT −1.70, AA/TT −1.52, TA −0.53, GC −2.60, GG/CC −1.72, CG −1.29 kcal/mol | 37 °C, 100 mM Na⁺ | same, Supplementary Table 3 | `read directly` |
| 5 | Full sequence spread over the six measured contacts | −0.19 to −2.60 kcal/mol (a factor of 13.7) | 37 °C, 15–100 mM Na⁺ | min/max over Supplementary Tables 2 and 3 above | `derived here` |
| 6 | Six-contact mean at 100 mM Na⁺ | −1.56 kcal/mol = −2.53 k_BT at 310 K | 37 °C, 100 mM Na⁺ | mean of row 4 | `derived here` |
| 7 | Temperature slope of the stacking term | dΔG_ST/dT = 0.026 kcal/(mol·K) | 32–52 °C, 15 mM Na⁺ | Yakovchuk 2006, Results, "Temperature dependence…" paragraph | `read directly` |
| 8 | Salt slope of the stacking term | dΔG_ST/d ln[Na⁺] = −0.200 kcal/mol | 37 °C, 15–100 mM Na⁺ | Yakovchuk 2006, Results, "Salt dependence…" paragraph | `read directly` |
| 9 | **Blunt-end stack between two SEPARATE duplexes, single-molecule** | **−0.8 to −3.4 kcal/mol per stack** | **20 mM MgCl₂**, room temperature, dual-beam optical tweezers on origami beams | Kilchherr, Wachauf, Pelz, Rief, Zacharias, Dietz, *Science* **353**:aaf5508 (2016), doi 10.1126/science.aaf5508 — RESULTS paragraph of the structured abstract | `abstract only (verbatim)` |
| 10 | **Blunt-end stack between two origami tiles, per helix** | **−2.59 and −2.67 kcal/mol (2-patch); −1.76, −1.66, −1.56 (4-patch); −1.42 (6-patch)** | 1×TAE + **12.5 mM Mg²⁺**, 22 °C, AFM monomer/dimer counting | Woo & Rothemund, *Nature Chem.* **3**:620 (2011), doi 10.1038/nchem.1070, **Supplementary Table S4** | `read directly` |
| 11 | Woo & Rothemund's own preferred "pure stacking" value | −2.63 kcal/mol per helix | 1×TAE + 12.5 mM Mg²⁺, 22 °C | same, Supplementary Note S3.1 | `read directly` |
| 12 | Woo & Rothemund's literature cross-check | −2.42 kcal/mol at 22 °C, by linear extrapolation of the GC contact | extrapolated from Yakovchuk 2006 | same, Supplementary Note S3.1 and Fig. S12 | `read directly` |
| 13 | Reproduction of row 12 from the primary table | least-squares fit of the GC column of Yakovchuk Supp. Table 2 (32/37/42/47/52 °C) gives slope +0.0190 kcal/(mol·K) and **−2.416 kcal/mol at 22 °C** | — | my regression on rows 1–2's source table | `derived here` |
| 14 | All-atom MD standard binding free energy, two blunt 10-bp duplexes end to end | ΔG_bind = **−6.3 ± 1 kcal/mol**; expressed per pair of DNA *ends*, ΔG_bind^ends = −5.4 kcal/mol | 1 M standard state, 120 mM NaCl, AMBER parmbsc0 | Maffeo, Luan & Aksimentiev, *NAR* **40**:3812 (2012), doi 10.1093/nar/gkr1220, PMC3351176, "Standard binding free energy of end-to-end association" | `read directly` |
| 15 | Same, kinetic-rate route | ΔG_bind between −4.4 and −7.6 kcal/mol, most probable −5.2 to −6.2 kcal/mol | as row 14 | same, results text | `read directly` |
| 16 | Work to rupture an end-to-end assembly (steered MD) | 9.4 ± 1.5 kcal/mol, rupture forces 100–200 pN | fast pulling, 100 mM NaCl | same, "Rupture of the end-to-end assembly" | `read directly` |
| 17 | Nearest-neighbour prediction for the same association, as computed by Maffeo | ΔG_bind = −16.94 + 2 × 6.94 = **−3.06 kcal/mol** for two 10-bp poly(dA·dT) fragments joining into a 20-mer | 1 M Na⁺, SantaLucia unified NN | same, Discussion (citing SantaLucia 1998) | `read directly` |
| 18 | Indirect experimental estimate from liquid-crystal ordering of short blunt duplexes | G_ST between **−0.4 and −2.4 kcal/mol** (1 M standard state, 293 K) | Nakata et al. I–N transition data, refitted | De Michele, Rovigatti, Bellini & Sciortino, *Soft Matter* **8**:8388 (2012), arXiv:1204.0985, Results §IV D and Conclusions | `read directly` |
| 19 | The same quantity in the original LC paper, as quoted by Maffeo | −3.8 kcal/mol | Clark/Bellini LC condensation | Maffeo 2012 Discussion (secondary attribution to Nakata et al. *Science* **318**:1276) | `read directly` (as a secondary attribution) |
| 20 | oxDNA's own coaxial stacking free energy, measured by De Michele | G⁰_ST = **−0.086 kcal/mol** at 1 M standard state, 293 K; bonding entropy −30.6 cal/(mol·K), bonding energy −9.06 kcal/mol | oxDNA v1 | De Michele 2012, Results §IV B and Conclusions | `read directly` |
| 21 | oxDNA / oxDNA2 free energy of stacking **across a nick** | −4.3 k_BT (oxDNA), −4.4 k_BT (oxDNA2), against **−2.62 k_BT in experiment** | 20-bp duplex, central nick, 37 °C | Snodin et al., *J. Chem. Phys.* **142**:234901 (2015), arXiv:1504.00821v3, **Table A2** and the paragraph under it | `read directly` |
| 22 | oxDNA / oxDNA2 free energy of **dimerising two 6-bp duplexes by blunt-ended coaxial stacking** | **+4.9 k_BT (oxDNA), +5.3 k_BT (oxDNA2)** | monomer concentration 5.37 mM, 19.85 °C — positive because the translational entropy cost at that concentration is inside it | same, Table A2 | `read directly` |
| 23 | Conversion of row 21's experimental figure | −2.62 k_BT at 310 K = **−1.615 kcal/mol**, against row 6's measured six-contact mean of −1.56 kcal/mol at 100 mM Na⁺ | — | arithmetic | `derived here` |

**Note on standard states.**
Rows 1–13 are per-stack free energies with no translational term (a nick, or an origami-tile bond already divided by the number of helices).
Rows 14, 18, 20 and 22 are *association* free energies of two free bodies and therefore contain a concentration-dependent translational entropy;
row 22 is positive for exactly that reason.
These are not the same quantity and must not be averaged together.

### 1b. Is blunt-end stacking between separate duplexes an established design motif?

| Claim | Verdict | Source | Flag |
|---|---|---|---|
| Blunt-end stacking joins separate origami tiles and is used deliberately as a programmable bond | **Established** | Woo & Rothemund 2011 (Nature Chem.), with a per-bond free energy measured in the SI | `read directly` (SI) / main text `not found` |
| Blunt-end stacking joins separate 3D DNA components as "shape-complementary, non-base pairing" bonds | **Established** | Gerling, Wagenbauer, Neuner & Dietz, *Science* **347**:1446 (2015) | `abstract only (verbatim)` |
| Blunt-end stacking is measurable as a single-molecule mechanical bond between two origami beams | **Established** | Kilchherr et al. 2016 | `abstract only (verbatim)` |
| Blunt-end stacking of origami edges occurs *unwanted* and can be suppressed | **Established** | Rothemund, *Nature* **440**:297 (2006), main text and Supplementary Note S5.7 | `read directly` |
| Blunt-end stacking programs 3D crystal packing at atomic resolution | **Established** | *Nat. Commun.* **17** (2026), doi 10.1038/s41467-026-69973-1, PMC13043697 | `read directly` |

### 1c. Range of the interaction

| Quantity | Value | Source | Flag |
|---|---|---|---|
| **All-atom PMF, attractive limb** | force rises with end-to-end distance between **3.5 Å** ("the distance between consecutive base pairs in a DNA helix") and **6.5 Å** ("the separation allowing water molecules to penetrate the volume between the ends") | Maffeo 2012, "PMF of axially aligned DNA duplexes" | `read directly` |
| **All-atom PMF, decay and sign change** | force "rapidly decreases as the end-to-end distance exceeds 6.5 Å and becomes slightly repulsive after **∼13 Å** (2–10 pN)" | same | `read directly` |
| Mean bound end-to-end separation, all-atom | r ≈ **0.5 nm** (100 mM NaCl) | Maffeo 2012, quoted by De Michele 2012 §IV A | `read directly` (in De Michele) |
| Mean bound end-to-end separation, oxDNA | P(r) peaked at **0.39 nm** (500 mM NaCl) | De Michele 2012 §IV A | `read directly` |
| **oxDNA2 coaxial-stacking radial term, real units** | minimum δr₀ = **3.4072 Å**; hard cutoff δr_c = **5.1108 Å**; smoothing begins δr_hi = **4.94044 Å**; lower cutoff δr_lo = **1.87396 Å**; well-depth coefficient k = 58.5 (LJ) / 4.80673 (real) | LAMMPS `pair_style oxdna2/coaxstk`, real-units example in Henrich, Gutiérrez Fosado, Curk & Ouldridge, *Eur. Phys. J. E* **41**:57 (2018), arXiv:1802.07145v2, §III C — and identically in the LAMMPS manual page `pair_oxdna2` | `read directly` |
| Same in oxDNA reduced units | δr₀ = 0.4, δr_c = 0.6, δr_lo = 0.22, δr_hi = 0.58, with 1 unit = **8.518 Å** | same LAMMPS listing; the length unit is stated in the caption of Snodin et al. Table A1 | `read directly` |
| oxDNA **intra-strand** stacking radial term (a different interaction — nearest neighbours along one strand) | r₀ = 0.4 (3.4072 Å), r_low = 0.32 (2.72576 Å), r_high = 0.75 (6.3885 Å), r_c = 0.9 (**7.6662 Å**); ε = 1.2145 + 2.6568 k_BT | Ouldridge, Louis & Doye, *J. Chem. Phys.* **134**:085101 (2011), arXiv:1009.4480v1, **Table II**; real-unit equivalents from the LAMMPS listing above | `read directly` |
| Single-molecule evidence that the potential is short-ranged | "Another key feature revealed in the lifetime data was the low sensitivity of the stacking interactions on the extent of pulling force. This phenomenon reflects short-ranged interaction potentials." | Kilchherr 2016, RESULTS of the structured abstract | `abstract only (verbatim)` |
| Design-literature characterisation | "short-ranged nucleobase stacking bonds that compete against electrostatic repulsion between the components' interfaces" | Gerling 2015, abstract | `abstract only (verbatim)` |

**The single most useful number for a contact model** is the pair (3.4 Å, 5.1 Å):
the oxDNA2 coaxial-stacking term has its minimum exactly at the base-pair rise and is identically zero beyond 5.1 Å,
and the all-atom PMF independently puts the whole attractive limb inside 6.5 Å with the force sign reversing near 13 Å.
So the interaction is a contact interaction with an attractive range of **one to two base-pair rises**,
and there is no attractive tail past ~0.65 nm.

### 1d. Does Mg²⁺ change it?

| Statement | Source | Flag |
|---|---|---|
| "Typically, at low cation concentrations, stacking bonds break and thus higher-order complexes disassemble." | Gerling & Dietz, *Angew. Chem. Int. Ed.* **58**:2680–2684 (2019) "Reversible Covalent Stabilization of Stacking Contacts in DNA Assemblies", doi 10.1002/anie.201812463, PMC6984961, Abstract | `read directly` |
| Working concentrations that bracket the transition for a stacking-bonded origami switch: **30 mM MgCl₂ closes** ("high cation concentrations … that stabilize the closed state"), **5 mM MgCl₂ opens** ("low, object-opening cation concentrations") | same, Results | `read directly` |
| "the balance between attractive and repulsive interactions, and thus the conformation of the assemblies, may be finely controlled by global parameters such as cation concentration or temperature" | Gerling 2015, abstract | `abstract only (verbatim)` |
| Single-molecule stacking measured "in the presence of 20 mM MgCl₂, which is a condition typically used in DNA nanotechnology", with a subset also in 500 mM NaCl | Kilchherr 2016, RATIONALE/RESULTS of the structured abstract | `abstract only (verbatim)` |
| Origami stacking-bond free energies of row 10 were measured in 1×TAE + 12.5 mM Mg²⁺ | Woo & Rothemund 2011, SI Note S3.1 | `read directly` |
| **Monovalent** salt: "increasing the electrolyte concentration from 0.1 to 1 M has negligible effect on the PMF"; Table 1 puts the change at **ΔΔG = +0.4 kcal/mol** going 0.1 → 1.0 M NaCl at φ = 36° | Maffeo 2012, "PMF of axially aligned DNA duplexes" and Table 1 | `read directly` |
| Monovalent salt, nick experiment: dΔG_ST/d ln[Na⁺] = −0.200 kcal/mol over 15–100 mM | Yakovchuk 2006 | `read directly` |
| Divalent electrolyte, qualitative: "The small-angle X-ray scattering experiments of the Pollack group demonstrated that the end-to-end interaction dominates over electrostatic repulsion in a divalent electrolyte … which, unfortunately, is not sufficient to estimate the standard free energy of end-to-end binding." | Maffeo 2012, Discussion | `read directly` |
| **A per-stack free energy measured as a function of [Mg²⁺], with numbers** | — | **`not found`** |

**Reading.**
The published position is that the *bond* is essentially a contact hydrophobic/stacking interaction whose own strength is nearly salt-independent —
Maffeo measures a 0.4 kcal/mol change over a tenfold monovalent range and Yakovchuk a −0.200 kcal/mol per e-fold slope at a nick —
while what Mg²⁺ actually changes in an *assembly* is the electrostatic repulsion the stack has to overcome.
Gerling's wording is exactly that: the stacking bonds "compete against electrostatic repulsion between the components' interfaces".
So a Mg²⁺ titration of an origami stacking device (30 mM closed / 5 mM open) is a titration of the competition, not of the stack.
No source read here separates the two quantitatively.

---

## 2. Verbatim passages

### 2.1 Rothemund, *Nature* **440**:297 (2006) — unwanted blunt-end stacking

Main text (fetched from `http://www.dna.caltech.edu/Papers/DNAorigami-nature.pdf`, the paragraph immediately preceding "Controlled combination of shapes"):

> "Stacking of shapes along blunt-ended helices provides an uncontrolled mechanism for the creation of larger structures (Fig. 3b). Instead of removing staples on the edge of a rectangle to avoid stacking (as described previously), 4-T hairpin loops (four thymines in a row, Fig. 1e, inset) or 4-T tails can be added to edge staples (Fig. 3e, f); stacked chains of 3–5 rectangles still formed (Fig. 3g), but 30% of rectangles (S = 319) occurred as monomers (Fig. 3i). Without hairpins, all rectangles occurred in aggregates (Fig. 3h)."

Main text, earlier (the rectangle characterisation paragraph):

> "Rectangles stacked along their vertical edges, often forming chains up to 5 µm long (lower AFM image)."

Main text, on suppression and on the seam:

> "Whatever the exact value, it is consistent: aspect ratios were invariant along stacked chains with dozens of rectangles. Such stacking was almost completely abolished by omitting staples along vertical edges. On the other hand, stacking across the seam of an unbridged rectangle (as in Fig. 1c) kept 65% of structures (S = 40) well-formed; the rest showed some degree of dislocation at the seam."

Supplementary Information (Supplementary Notes, fetched from `https://static-content.springer.com/esm/art%3A10.1038%2Fnature04586/MediaObjects/41586_2006_BFnature04586_MOESM1_ESM.pdf`), **Supplementary Note S5.7: Prevention of stacking**, complete:

> "Stacking interactions based on blunt-ended helices can be quite strong; rectangles which have many parallel blunt-ends along their left and right edges stack so strongly that they may form long chains over 5 microns in length (Supplementary Fig. S45a–c). As deposited on mica, such chains exhibit frequent ∼75 nm dislocations, every few rectangles along the chain (arrows, Supplementary Fig. S45b). I hypothesize that in solution rectangles in such chains are completely stacked and and no such offsets occur.
>
> To avoid aggregation based on stacking interactions, several methods can be employed. First, the staple strands along the edges of a shape may be simply left out, and the scaffold left unstructured along these edges. Supplementary Fig. S45d shows rectangles that have been disaggregated in this way. Simply omitting staple strands out leaves unstructured scaffold at the edges of the rectangle and decreases the size of the potential pixel array by two columns. Thus a second, more aesthetically pleasing, method is that employed in Fig. 3e: the addition of 4T hairpin loops to staple strands on the edges of a shape. (The use of 4T hairpin loops to disaggregate DNA nanostructures was first demonstrated by Rebecca Schulman for her "zig-zag boundaries".) A third very similar method is to add 4T tails to staple strands that have ends on the edge of the shape. Supplementary Fig. S45e shows the normal amount of aggregation for 3-hole disks. Supplementary Fig. S45f shows that the addition of just a small number (12) of 4T tails to the 3-hole disks causes almost complete disaggregation.
>
> These experiments show that, when it is desirable to do so, stacking and the aggregation it induces can be almost entirely suppressed."

Supplementary Information, Supplementary Note on strain at edges — relevant because it is Rothemund's own evidence that the terminal base pair is intact and planar:

> "Strain at seams or edges does not appear to cause any gross defects in the origami; bases at the end of the helices are highly available for stacking against other DNA origami which suggests that the last base pair does form and assumes a planar configuration."

**What he did about it**: (i) omit the edge staples entirely, leaving unstructured scaffold — "almost completely abolished"; (ii) add 4-T hairpin loops to edge staples — leaves 30 % monomers where without hairpins "all rectangles occurred in aggregates"; (iii) add 4-T tails to edge staples — 12 tails almost completely disaggregated the 3-hole disks.

### 2.2 Woo & Rothemund, *Nature Chemistry* **3**:620 (2011) — stacking as a design motif, with numbers

The main text is paywalled (`not found`).
The Supplementary Information is open at the Caltech repository, `https://authors.library.caltech.edu/records/386nr-f7943/files/nchem.1070-s1.pdf`, and was read directly.

**Supplementary Note S3.1**, the passage that carries the number:

> "We measured the free energy of stacking bonds for various systems with different numbers and sequences of active patches (Fig. S10ab). Assuming that loop-loop interactions have a neutral effect on the free energy of stacking bonds we calculated the free energy per helix for each system (Table S4 and Fig. S11). The total binding energy was expected to be linear in the number of active patches which would imply a constant free energy per helix, yet we observed free energies that varied between -2.67 kcal/mol and -1.42 kcal/mol depending on the system."

> "Because we hypothesize that non-stacking factors are all destabilizing, we suggest that the average energy obtained for the 2-patch systems, -2.63 kcal/mol (1× TAE with 12.5 mM Mg2+, 22°C), is most reflective of a pure stacking interaction. One literature value (Ref. 25 of the main text) for the energy of GC/CG stacking is -2.17 kcal/mol (1M Na+ solution at 37°C). While buffer conditions between the two experiments differ, we did our best to make the measurements comparable by correcting the literature value using temperature-dependent data given in Ref. 25 of the main text. […] Linear extrapolation to the y-axis (T=22°C) gives an energy of -2.42 kcal/mol at 22°C, which is a very close value to the value we obtained."

Reference 25 of the main text is **Yakovchuk et al., *NAR* **34**:564 (2006)** — confirmed from the Crossref reference list of doi 10.1038/nchem.1070 (`BFnchem1070_CR25`), `read directly`.

**Supplementary Table S4** (free energy of the stacking bond per helix), read directly:

| System | binary code | [origami] (nM) | ΔG_st (kcal/mol per helix) | N (origami count) |
|---|---|---|---|---|
| 2patch-(6,7) | 000001100000 | 0.424 | −2.5889 | 362 |
| 2patch-(5,8) | 000010010000 | 0.848 | −2.6738 | 276 |
| 4patch-(5,6,7,8) | 000011110000 | 0.424 | −1.7644 | 178 |
| 4patch-(3,5,8,10) | 001010010100 | 0.424 | −1.6593 | 566 |
| 4patch-(1,4,9,12) | 100100001001 | 0.424 | −1.5578 | 360 |
| 6patch-(4,5,6,7,8,9) | 000111111000 | 0.212 | −1.4223 | 442 |

The method, from **Supplementary Note S3**:

> "The free energy of the stacking bonds was measured by assuming that monomers and dimers of 'one-sided' rectangle origami (origami with edge staples on only one side, Fig. S10a) were at equilibrium. […] The equilibrium concentrations of monomers and dimers were measured by depositing the samples on mica and counting the numbers of each in AFM images […] where R is the gas constant (8.314 J/mol·K) and T is the temperature 295 K (22°C)."

The sublinearity — worth carrying, because it says a stacking bond of *n* helices is **not** *n* times one stack:

> "First, we observed that the magnitude of the binding energy per helix decreased as the number of helices increases. We hypothesize that the resulting sublinearity of binding energy is due to residual large-scale twist (or other deformation) of the origami structure; our picture is that as the number of stacking patches increases and the patches become more spread out, the bending (or twisting) penalty per patch increases."

And on the geometry of an origami edge, **Supplementary Note S2.5**:

> "Because accurate models (backed by high resolution structural data) of origami edges do not exist, it is difficult to predict the exact structure and stacking configurations of the blunt-ends on the edges of origami."

> "Thus we would expect stacking of blunt ends between crossover free edges to be native B-form stacking, and that it should be relatively strong."

> "Because the scaffold crossovers act to pull the base pairs away from the helical twist angle that they would assume in a crossover-free edge, whatever structure forms at relaxed edges cannot be B-form DNA."

**Contemporary commentary** — A. J. Turberfield, "DNA nanotechnology: Geometrical self-assembly", News & Views, *Nature Chemistry* **3**, August 2011,
fetched from `https://www.dna.caltech.edu/Papers/stacking-bonds2011_NV.pdf`, `read directly`.
The copy served there is an **author proof**: its own reference 6 still reads "Woo, S. & Rothemund, P. W. K. Nature Chem. 3, XX–XX (2011)", so no page number is quoted here.

> "The tile–tile bonds are based on helix stacking interactions: a blunt end — where the strands of a DNA double helix end on a base pair — at a tile edge can stack on the end of a helix in another tile to form a quasi-continuous helix that is only slightly disrupted by the discontinuities in the DNA backbones. This base-stacking interaction is the same one that makes a large contribution to the stability of a double helix — larger, in fact, than that of the base-pairing hydrogen bonds. To ensure that, to a good approximation, the stability of a tile–tile bond depends only on the number of stacked helices (rather than on the details of the stacked bases), Woo and Rothemund designed each helix to terminate with the same C–G base pair."

### 2.3 Gerling, Wagenbauer, Neuner & Dietz, *Science* **347**:1446 (2015)

Full text paywalled (Semantic Scholar reports `openAccessPdf: CLOSED`; mediaTUM is behind an Anubis proof-of-work gate). Abstract read verbatim from the EuropePMC REST record for PMID 25814577:

> "We demonstrate that discrete three-dimensional (3D) DNA components can specifically self-assemble in solution on the basis of shape-complementarity and without base pairing. Using this principle, we produced homo- and heteromultimeric objects, including micrometer-scale one- and two-stranded filaments and lattices, as well as reconfigurable devices, including an actuator, a switchable gear, an unfoldable nanobook, and a nanorobot. **These multidomain assemblies were stabilized via short-ranged nucleobase stacking bonds that compete against electrostatic repulsion between the components' interfaces.** Using imaging by electron microscopy, ensemble and single-molecule fluorescence resonance energy transfer spectroscopy, and electrophoretic mobility analysis, we show that **the balance between attractive and repulsive interactions, and thus the conformation of the assemblies, may be finely controlled by global parameters such as cation concentration or temperature** and by an allosteric mechanism based on strand-displacement reactions."

(Emphasis added here; the text is verbatim.)
The *Science* editor's summary, verbatim from Crossref for doi 10.1126/science.aaa5372:

> "DNA origami—nanostructures created by programming the assembly of single-stranded DNA through base pairing—can create intricate structures. However, such structures lack the flexible and reversible interactions more typical of biomolecular recognition. Gerling et al. created three-dimensional DNA nanostructures that assemble though nucleotide base-stacking interactions (see the Perspective by Shih). These structures cycled from open to closed states with changes in salt concentration or temperature."

**Energy per stack quoted by Gerling: `not found`** — no per-stack number appears in either abstract, and the full text was not reachable.

### 2.4 Kilchherr, Wachauf, Pelz, Rief, Zacharias & Dietz, *Science* **353**:aaf5508 (2016)

Full text paywalled. Structured abstract read verbatim from Crossref, doi 10.1126/science.aaf5508.

RATIONALE:

> "The goal of this work is to measure the dynamics of DNA base-pair stacking at the level of individual base-pair steps. Because stacking interactions act perpendicularly to the hydrogen bonds, it should be possible to use mechanical forces to break stacking while leaving hydrogen bonds intact. To realize such measurements, we combine the positioning capabilities of DNA origami with single-molecule manipulation, as enabled by dual-beam optical traps. To make the weak single–base-pair stacking interactions experimentally accessible, we prepared parallel arrays of blunt-end DNA double helices to take advantage of avidity effects when these arrays form stacking interactions."

RESULTS:

> "We sampled all 16 sequence combinations of installing a particular interfacial base pair on the array on the left beam and another base pair in the array on the right beam, and we created arrays with two, four, and six blunt ends. We could measure the force-dependent lifetimes for all base-pair step sequence combinations in the presence of 20 mM MgCl2, which is a condition typically used in DNA nanotechnology. For a subset of base-pair step combinations, we also obtained data in the presence of 500 mM NaCl, which mimics the conditions in the cell nucleus. The base-pair stack arrays spontaneously dissociated at average rates ranging from 0.02 to 500 per second, where the dissociation time scale strongly depended on the sequence combination and the stack array size. […] Another key feature revealed in the lifetime data was the low sensitivity of the stacking interactions on the extent of pulling force. This phenomenon reflects short-ranged interaction potentials. […] We used a model to estimate the free-energy increments per single base-pair stack from the kinetic rates that we measured with stack arrays. The free-energy increments per stack ranged from –0.8 to –3.4 kilocalories per mole. Our data reveals a trend in the stacking-strength hierarchy that may be associated with the extent of geometrical atomic overlap between the bases within a base-pair step."

Author abstract, verbatim from EuropePMC (PMID 27609897), adds the force scale:

> "Forces in the range from 2 to 8 piconewtons that act along the helical direction only mildly accelerated the stochastic unstacking process."

### 2.5 Yakovchuk, Protozanova & Frank-Kamenetskii, *NAR* **34**:564 (2006)

Open access; full text read from `https://pmc.ncbi.nlm.nih.gov/articles/PMC1360284/`;
supplementary PDF read from the EuropePMC supplementary-files archive for PMC1360284 (`nar_34_2_564__1.pdf`).

**Supplementary Table 2** — "Temperature dependence of ΔG^ST_KL, kcal/mol, for A•T- and G•C-containing contacts", footnote a: "Stacking parameters were measured in 1xTBE which is equivalent to [Na+] = 15 mM":

| T, °C | AT | AA/TT | TA | GC | GG/CC | CG |
|---|---|---|---|---|---|---|
| 12 | −1.86 | −1.80 | −0.90 | — | — | — |
| 22 | −1.70 | −1.44 | −0.50 | — | — | — |
| 32 | −1.45 | −1.17 | −0.24 | −2.29 | −1.36 | −0.92 |
| 37 | −1.31 | −1.08 | −0.19 | −2.06 | −1.41 | −0.89 |
| 42 | −1.14 | −0.93 | −0.03 | −2.02 | −1.17 | −0.68 |
| 47 | −0.94 | −0.72 | 0.13 | −1.93 | −1.09 | −0.63 |
| 52 | −0.72 | −0.49 | 0.26 | −1.88 | −0.90 | −0.42 |

**Supplementary Table 3** — "Salt dependence of ΔG^ST_KL, kcal/mol", footnote a: "Stacking parameters were measured at 37 °C":

| [Na⁺], mM | AT | AA/TT | TA | GC | GG/CC | CG |
|---|---|---|---|---|---|---|
| 15 | −1.31 | −1.08 | −0.19 | −2.06 | −1.41 | −0.89 |
| 27 | −1.48 | −1.24 | −0.30 | −2.18 | −1.51 | −1.02 |
| 35 | −1.52 | −1.27 | −0.33 | −2.4 | −1.45 | −0.91 |
| 55 | — | — | — | −2.5 | −1.59 | −1.08 |
| 68 | −1.67 | −1.38 | −0.40 | — | — | — |
| 75 | — | — | — | −2.56 | −1.56 | −1.19 |
| 100 | −1.70 | −1.52 | −0.53 | −2.60 | −1.72 | −1.29 |

Main text, on the slopes:

> "Linear regression analysis of the temperature dependences of ΔG_ST values gives mean slope of dΔG_ST/dT = 0.026 kcal/molK, which is very close to the slope of temperature dependence of stability parameters described by Equations 6 and 7."

> "Salt dependences of stacking terms of A•T- and G•C-containing contacts are similar to dΔG_ST/d ln[Na+] = −0.200 kcal/mol."

Main text, on which contacts this paper measured:

> "We studied eight A•T-containing and eight G•C-containing nicked dinucleotide stacks."

Main text, on the headline conclusion:

> "We conclude that stability of the DNA polymer with respect to strand separation is mainly determined by stacking interactions with base pairing being destabilizing (A•T pairs) or contributing almost nothing (G•C-pairs)."

### 2.6 Protozanova, Yakovchuk & Frank-Kamenetskii, *JMB* **342**:775 (2004) — "Stacked–Unstacked Equilibrium at the Nick Site of DNA"

Elsevier; paywalled; full text and its Table of **ten** stacking parameters **`not found`**.
Abstract read verbatim from EuropePMC (PMID 15342236):

> "We have prepared a series of 32, 300 bp-long DNA fragments with solitary nicks in the same position differing only in base-pairs flanking the nick. Electrophoretic mobility of these fragments in the gel has been studied. Assuming the equilibrium between stacked and unstacked conformations at the nick site, all 32 stacking free energy parameters have been obtained. Only ten of them are essential and they govern the stacking interactions between adjacent base-pairs in intact DNA double helix. A full set of DNA stacking parameters has been determined for the first time."

The six contacts re-measured in the 2006 NAR paper (Section 2.5) are a subset of those ten;
the four mixed A•T/G•C contacts (e.g. AG, AC, GA, CA) exist only in the 2004 table and were **not** obtained here.

### 2.7 Maffeo, Luan & Aksimentiev, *NAR* **40**:3812 (2012) — the PMF and the range

Open access; read from `https://pmc.ncbi.nlm.nih.gov/articles/PMC3351176/`.

Abstract:

> "We found the end-to-end force to be short range, attractive, hydrophobic and only weakly dependent on the ion concentration."

Results, "PMF of axially aligned DNA duplexes" — **the range passage**:

> "The force sharply increases with the end-to-end distance between 3.5 Å—the distance between consecutive base pairs in a DNA helix—and 6.5 Å, the separation allowing water molecules to penetrate the volume between the ends of the fragments. The force rapidly decreases as the end-to-end distance exceeds 6.5 Å and becomes slightly repulsive after ∼13 Å (2–10 pN). Thus, the end-to-end force has a large but very short-range attractive component caused by the hydrophobic effect and a much smaller long-range repulsive component that originates from screened electrostatic interactions between the DNA fragments."

Same section, **the salt statement**:

> "These calculations (detailed in Supplementary Data) demonstrate that increasing the electrolyte concentration from 0.1 to 1 M has negligible effect on the PMF."

Table 1, "Relative free energy change, ΔΔG, upon formation of the end-to-end complex" (read directly):

| 5′ chemistry | ion concentration (M) | φ | ΔΔG (kcal/mol) |
|---|---|---|---|
| Phosphoryl | 0.1 | 36° | 0 |
| Phosphoryl | 1.0 | 36° | 0.4 |
| Phosphoryl | 1.0 | −20° | −1.8 |
| Phosphoryl | 1.0 | 180° | 0.1 |
| Hydroxyl | 1.0 | 36° | 2.3 |
| Hydroxyl | 1.0 | 180° | 1.2 |

Results, standard binding free energy:

> "The sum of these terms yielded the free energy change upon formation of the end-to-end DNA complex ΔG_bind = −6.3 ± 1 kcal/mol for a DNA concentration of 1 M in 120 mM NaCl. Since a pair of DNA ends has only one binding configuration, we can express the standard binding free energy in terms of DNA ends, so that ΔG^ends_bind = −5.4 kcal/mol."

Discussion — **the warning that a nick value is not a blunt-end value**:

> "It is tempting to conceptually equate the base stacking interactions within a continuous molecule with the base stacking interactions that drive the assembly of two disjoint DNA duplexes. Thus, the unified nearest neighbor parameters, which can predict the energy for DNA hybridization based on DNA melting data, suggest ΔG_bind = −16.94 + 2 × 6.94 = −3.06 kcal/mol for the association of two 10 bp poly(dA·dT) DNA fragments into a continuous 20-bp molecule. Such a simple calculation may be, however, flawed as additional conformational flexibility afforded by the lack of phosphodiester bonds at the end-to-end interface should allow the base stacking geometry to be optimized, magnifying the base stacking contribution to the free energy. Accordingly, the average interaction energy between adjacent base pairs with φ = −20° was measured to be 2 kcal/mol lower in bases forming the end-to-end junction than in bases in the middle of one of the DNA fragments."

Discussion, on the divalent case:

> "The small-angle X-ray scattering experiments of the Pollack group demonstrated that the end-to-end interaction dominates over electrostatic repulsion in a divalent electrolyte (12, 13), which, unfortunately, is not sufficient to estimate the standard free energy of end-to-end binding."

Discussion, on the confidence of the number:

> "We note that the value of ΔG_bind obtained in this study is larger than values reported in experiments. […] Nevertheless, we cannot rule out the possibility that the present MD force field somewhat exaggerates the interactions driving end-to-end self-assembly of duplex DNA."

Discussion, on the lifetime:

> "the lifetime of end-to-end interactions (∼70 µs) is somewhat shorter than the temporal resolution of many experimental techniques."

### 2.8 oxDNA — the coaxial-stacking term and its parameters

Šulc, Ouldridge, Romano, Doye & Louis, *J. Chem. Phys.* **137**:135101 (2012), arXiv:1207.3391v1, §II — the statement that this is the term that models the motif in question:

> "The coaxial stacking term, not shown in the Fig. 2, is designed to capture stacking interactions between non-neighboring bases, usually on different strands."

Snodin et al., *J. Chem. Phys.* **142**:234901 (2015), arXiv:1504.00821v3, Appendix A 3 — the model's own audit of blunt-end stacking, and the fact that it is treated as **too weak**:

> "For oxDNA2, we made an additional change to the coaxial stacking potential: we removed the f5(cos(φ3), a_coax,3′, cos(φ3)*_coax) and f5(cos(φ4), a_coax,4′, cos(φ4)*_coax) terms. These terms had allowed only right-handed blunt-ended stacking, disallowing left-handed blunt-ended stacking; however, since the development of the original oxDNA model, this constraint has been deemed unnecessary, as the blunt-ended coaxial stacking interaction was found to be likely to be too weak compared to experiment, and there is no experimental evidence indicating that left-handed blunt-ended coaxial stacking is not possible."

> "The free-energy change upon stacking across a nick, ΔG_nick stack, is slightly more negative in oxDNA2, indicating that a nicked duplex stacks slightly more strongly across the nick in oxDNA2 than in the original oxDNA. In both cases the stacking is stronger than is seen in experiment (−2.62 k_BT in Ref. 83 […]). […] Finally, dimerisation of two duplexes through blunt-ended coaxial stacking is found to be less favourable in oxDNA2 than in oxDNA. This is despite the mitigating effect of removing the f5 terms in the coaxial stacking interaction, which strengthens blunt-ended stacking. A previous study has shown that oxDNA already underestimated the stability of blunt-ended coaxial stacking."

Reference 83 there is Yakovchuk, Protozanova & Frank-Kamenetskii, *NAR* **34**:564 (2006) — read from the paper's own reference list, `read directly`.
Reference 82 is De Michele, Rovigatti, Bellini & Sciortino, *Soft Matter* **8**:8388 (2012).

**Table A2** of the same paper (read directly):

| Measurement | oxDNA | oxDNA2 | SantaLucia |
|---|---|---|---|
| ΔG_nick stack | −4.3 k_BT | −4.4 k_BT | — |
| ΔT_m,hairpin stem,6mer | +9.9 K | +10.6 K | +11.7 K |
| ΔT_m,hairpin stem,7mer | +7.7 K | +8.3 K | +8.5 K |
| ΔT_m,hairpin stem,8mer | +6.3 K | +6.9 K | +6.4 K |
| ΔG_blunt ended stacking | +4.9 k_BT | +5.3 k_BT | — |

with the caption defining both:

> "ΔG_nick stack = ΔG_stacked − ΔG_unstacked is the free-energy change upon stacking across a nick for a 20-bp duplex with a nick at the centre at 37 °C. […] ΔG_blunt ended stacking is the free-energy change upon dimerisation of two 6-bp duplexes through blunt-ended coaxial stacking at a monomer concentration of 5.37 mM and at 19.85 °C."

**The radial range parameters.**
Snodin et al. Table A1 changes only `k_coax` (46 → 58.5) and `θ_coax,1`; its caption fixes the length unit:

> "All angles except γ are given in radians, all lengths are defined with respect to a reduced length scale (1 unit = 8.518 Å) and all energies are defined with respect to a reduced temperature (k_BT = 0.1 corresponding to 300 K)."

The radial coefficients themselves are given explicitly, in **both** unit systems, in the LAMMPS `cgdna` package documentation and in its accompanying paper
(Henrich, Gutiérrez Fosado, Curk & Ouldridge, *Eur. Phys. J. E* **41**:57 (2018), arXiv:1802.07145v2, §III C; identical listing on `https://docs.lammps.org/pair_oxdna2.html`):

LJ units:
> `pair_coeff * * oxdna2/coaxstk 58.5 0.4 0.6 0.22 0.58 2.0 2.891592653589793 0.65 1.3 0 0.8 0.9 0 0.95 0.9 0 0.95 40.0 3.116592653589793`

Real units (Ångström):
> `pair_coeff * * oxdna2/coaxstk 4.80673207785863 3.4072 5.1108 1.87396 4.94044 2.0 2.891592653589793 0.65 1.3 0.0 0.8 0.9 0.0 0.95 0.9 0.0 0.95 40.0 3.116592653589793`

Mapping onto Snodin et al.'s Eq. (A6), `f2(δr_stack, k_coax, δr⁰_coax, δr^{c,low}, δr^{c,high}, δr^{low}, δr^{high})`,
the first five arguments are the strength and the four radial lengths:
**k = 58.5 (4.807 in real units), δr⁰ = 0.4 = 3.4072 Å, δr_c = 0.6 = 5.1108 Å, δr_lo = 0.22 = 1.87396 Å, δr_hi = 0.58 = 4.94044 Å.**
The two listings are consistent to the last digit under the 8.518 Å length unit, which is the check that the argument order is read right.

For contrast, the **intra-strand** stacking term (Ouldridge, Louis & Doye 2011, Table II — a *different* interaction, summed only over nearest neighbours along one strand):

> "stacking f1(r_stack) ε = 1.2145 + 2.6568 kT, a = 6, r0 = 0.4, r_low = 0.32, r_c = 0.9, r_high = 0.75"

i.e. minimum at 3.4072 Å and cut off at **7.666 Å**.

### 2.9 De Michele, Rovigatti, Bellini & Sciortino, *Soft Matter* **8**:8388 (2012)

arXiv:1204.0985v1, read directly.

Conclusions:

> "The stacking free energy value that properly accounts for the polymerization process observed in the molecular dynamics simulations is G⁰_ST = −0.086 kcal/mol at a standard concentration 1 M of DNADs and T = 293 K comprising a bonding entropy of −30.6 cal/mol K and a bonding energy of −9.06 kcal/mol."

> "The value of G_ST can also be used as a fitting parameter in the theory for matching c_N with the experimental results, retaining the excluded volume estimates calculated for the coarse-grained DNA model. Such procedure shows that values of the stacking free energy between −0.4 kcal/mol and −2.4 kcal/mol are compatible with the experimental location of the I-N transition line."

> "In the work of Maffeo et al., the authors report a quite smaller value of G_ST, namely G^M_ST = −6.3 kcal/mol, a value which was confirmed by the same authors by performing an investigation of the aggregation kinetic in a very lengthy all-atom simulation of DNAD with N_b = 10. If such G_ST value is selected as input in our theoretical approach (maintaining the same excluded volume term), then one finds c^M_N ≈ 250 mg/ml, a value significantly smaller than the experimental result (c_N = 650 ± 50 mg/ml). This casts some doubts on the effectiveness of the employed all-atom force-field to properly model coaxial stacking."

§IV A, the bound-state geometry:

> "P(r) is peaked at 0.39 nm, whereas Maffeo et al. found an average distance of r ≈ 0.5 nm. This difference can be understood in terms of the effect of the salt concentration which, being five times higher than the one used in Ref. [64], increases the electrostatic screening, thus effectively lowering the repulsion between DNA strands."

### 2.10 Cation dependence, open-access source

Gerling & Dietz, *Angew. Chem. Int. Ed.* **58**:2680–2684 (2019), "Reversible Covalent Stabilization of Stacking Contacts in DNA Assemblies", doi 10.1002/anie.201812463, PMC6984961, read from the PMC article page.
(Author list and pagination confirmed from `https://api.crossref.org/works/10.1002/anie.201812463`.)

Abstract:

> "Stacking bonds formed between two blunt-ended DNA double helices can be used to reversibly stabilize higher-order complexes that are assembled from rigid DNA components. Typically, at low cation concentrations, stacking bonds break and thus higher-order complexes disassemble."

Results:

> "We incubated switch particles at the high cation concentrations (30 mm MgCl2) that stabilize the closed state and exposed samples to 365 nm light."

> "We incubated previously crosslinked switch particles at low, object-opening cation concentrations (5 mm MgCl2) and exposed the samples to 310 nm light irradiation."

### 2.11 Blunt-end stacking as a crystal-packing motif (recent, open access)

*Nature Communications* (2026), "Blunt-force assembly of programmable DNA architectures using π-π stacking", doi 10.1038/s41467-026-69973-1, PMC13043697, abstract:

> "Here, we employ composable DNA tiles to form complex 3D architectures using blunt-ended motifs with single duplex interfaces, thereby leveraging the geometry of the tile and the terminal nucleobase identity to control self-assembly outcomes. These crystals yielded X-ray diffraction at resolutions between 10.0 and 1.86 Å. We establish programmability and tunable packing, including translational and inversion symmetries, 5'−3' and 5'−5' stacking, and both positive and negative helical twist values."

No per-stack free energy and no Mg²⁺ dependence were found in this paper's text.

---

## 3. Queries run

**EuropePMC REST** (`https://www.ebi.ac.uk/europepmc/webservices/rest/search`, `resultType=core`, ~9 s between calls):

1. `TITLE:"Base-stacking and base-pairing contributions into thermal stability of the DNA double helix"` — 2 hits (PMC1360284).
2. `TITLE:"Stacked-unstacked equilibrium at the nick site of DNA"` — 1 hit (PMID 15342236, closed).
3. `TITLE:"Single-molecule dissection of stacking forces in DNA"` — 1 hit (PMID 27609897, closed).
4. `TITLE:"shape-complementary, non-base pairing 3D components"` — 1 hit (PMID 25814577, closed).
5. `"stacking bonds" AND "DNA origami"` — 14 hits; yielded PMC6984961 and PMC9674029.
6. `TITLE:"Folding DNA to create nanoscale shapes and patterns"` — 1 hit (PMID 16541064, closed).
7. `TITLE:"Programmable molecular recognition based on the geometry of DNA nanostructures"` — 1 hit (PMID 21778982, closed).
8. `"blunt-end stacking" AND "origami"` — 79 hits; yielded PMC13043697.
9. `"coaxial stacking" AND "free energy" AND "nick"` — 21 hits.
10. `AUTH:"Maffeo C" AND AUTH:"Aksimentiev A" AND "end-to-end"` — 13 hits, none the target.
11. `TITLE:"End-to-end attraction of duplex DNA"` — 1 hit (PMC3351176, open).

**EuropePMC supplementary archive**: `GET /PMC1360284/supplementaryFiles` — returned a ZIP containing `nar_34_2_564__1.pdf`, which is the source of Supplementary Tables 2 and 3.

**Crossref** (`https://api.crossref.org/works/<doi>`):
`10.1126/science.aaf5508` (full structured abstract returned),
`10.1126/science.aaa5372` (editor's summary returned),
`10.1038/nchem.1070` (no abstract; **reference list returned**, which is how ref. 25 was identified),
`10.1038/nature04586` (no abstract),
`10.1016/j.jmb.2004.07.075` (no abstract),
`10.1039/c2sm25845e` (no abstract; title confirmed).

**arXiv API** (`https://export.arxiv.org/api/query`, `curl -sL`):
`all:"coarse-grained model of DNA" AND au:Ouldridge` → 1009.4480v1, 1504.00821v3;
`ti:"Sequence-dependent thermodynamics of a coarse-grained DNA model"` → 1207.3391v1;
`ti:"Coarse-grained simulation of DNA using LAMMPS"` → 1802.07145v2;
`ti:"Self-assembly of short DNA duplexes"` → 1204.0985v1.

**Direct fetches that worked**:
`http://www.dna.caltech.edu/Papers/DNAorigami-nature.pdf`;
`http://www.dna.caltech.edu/Papers/stacking-bonds2011_NV.pdf`;
`https://static-content.springer.com/esm/art%3A10.1038%2Fnature04586/MediaObjects/41586_2006_BFnature04586_MOESM1_ESM.pdf` (Rothemund Supplementary Notes; MOESM2 is the design diagrams, MOESM3 403s);
`https://authors.library.caltech.edu/records/386nr-f7943/files/nchem.1070-s1.pdf?download=1` (Woo & Rothemund SI);
`https://pmc.ncbi.nlm.nih.gov/articles/PMC1360284/`, `.../PMC3351176/`, `.../PMC6984961/`, `.../PMC13043697/`;
`https://europepmc.org/articles/PMC3351176?pdf=render`;
`https://docs.lammps.org/pair_oxdna2.html`;
`https://api.semanticscholar.org/graph/v1/paper/DOI:<doi>?fields=title,openAccessPdf,externalIds`.

**Direct fetches that failed**:
`http://www.dna.caltech.edu/Papers/DNAorigami-supp1.pdf` — 404 (the supplementary notes are only at the Springer static URL above);
`https://thesis.library.caltech.edu/7772/` — connection refused / timeout, twice, by `curl` and by WebFetch (Sungwook Woo's 2013 PhD thesis "Beyond Watson and Crick", the obvious deeper source on origami stacking bonds, was therefore **not** read);
`https://mediatum.ub.tum.de/1581061` — served an Anubis proof-of-work interstitial, not the record;
`https://www.dietzlab.org/publications/` — no PDF links.

Two **web searches** were run (Kilchherr free full text; Woo thesis).
Neither produced a citation used in this file; they were used only to locate candidate URLs, all of which were then fetched and read (or failed to fetch, as recorded above).

---

## 4. What is NOT found

1. **The ten essential per-doublet stacking parameters of Protozanova et al., *JMB* **342**:775 (2004).**
   Elsevier, paywalled; no repository copy located.
   Six of the ten (AT, AA/TT, TA, GC, GG/CC, CG) were recovered instead from the open Supplementary Tables of the 2006 NAR paper by the same group and are quoted above.
   The four **mixed** A•T/G•C contacts are missing.

2. **Any per-stack free energy quoted by Gerling et al. 2015.**
   Full text unreachable; neither abstract carries a number.

3. **Kilchherr et al. 2016's per-sequence table** (the 16 sequence combinations, and the lifetimes at 2/4/6 blunt ends).
   Only the overall range −0.8 to −3.4 kcal/mol survives in the abstract.
   The full text is the single most valuable missing item for this task and is closed everywhere checked.

4. **A per-stack free energy measured as an explicit function of [Mg²⁺].**
   The literature read here gives Mg²⁺ *thresholds* for assembly/disassembly of stacking-bonded objects (5 mM open, 30 mM closed) and per-stack energies at *one* Mg²⁺ (12.5 mM in Woo & Rothemund, 20 mM in Kilchherr), but nothing that separates the stack's own strength from the interfacial electrostatic repulsion it competes with.
   Maffeo's ion-concentration series is monovalent only.

5. **An experimental potential of mean force for blunt-end stacking.**
   The only PMF read here (Maffeo 2012) is all-atom MD, and its own authors flag the force field as possibly exaggerating the interaction (§2.7).
   No experimental distance-resolved potential was found.

6. **An `oxDNA` publication that states the coaxial-stacking radial cutoffs in prose.**
   Snodin et al. change only `k_coax` and `θ_coax,1` and refer the rest to Ouldridge's 2011 Oxford thesis, which was not retrieved.
   The numbers used above come from the LAMMPS `cgdna` implementation and its EPJ E paper, which state them in both unit systems; that agreement across unit systems is the only internal check available.

7. **A statement anywhere that reconciles the four standard states** in which a "blunt-end stacking free energy" is quoted
   (a nick with no translational term; an origami tile bond divided by helix count; a duplex-dimerisation constant at 1 M; a duplex-dimerisation constant at 5.37 mM).
   The published values −0.086, −0.4 to −2.4, −2.6, −3.06, −3.8, −6.3 kcal/mol and +4.9 k_BT are **not** in conflict merely because they differ; each has to be read with its own reference concentration, and no source read here tabulates them together.

---

## 5. Note on one internal inconsistency, recorded rather than resolved

Woo & Rothemund's SI writes:

> "One literature value (Ref. 25 of the main text) for the energy of GC/CG stacking is -2.17 kcal/mol (1M Na+ solution at 37°C)."

Reference 25 is Yakovchuk 2006, whose stacking parameters were measured in 1×TBE, i.e. **[Na⁺] = 15 mM** (footnote a of its Supplementary Table 2) and, in the salt series, only up to **100 mM** (Supplementary Table 3).
That paper publishes no 1 M Na⁺ value, and extrapolating its own −0.200 kcal/mol per e-fold slope from the 100 mM GC value of −2.60 would give ≈ −3.06, not −2.17.
The GC column of Supplementary Table 2 at 37 °C reads −2.06 measured, and the least-squares line through its five points gives −2.13 at 37 °C —
so −2.17 is very close to the 15 mM value and the "1M Na+" qualifier appears to be an error in the SI, not a different measurement.

The second half of the same passage **does** reproduce exactly.
Fitting the GC column of Supplementary Table 2 (32/37/42/47/52 °C → −2.29/−2.06/−2.02/−1.93/−1.88) gives a slope of +0.0190 kcal/(mol·K) and an intercept of **−2.416 kcal/mol at 22 °C**, against the SI's stated −2.42 (`derived here`, on numbers that are `read directly`).
That is a closed chain from the primary table to the origami paper's cross-check, and it is the reason the two independent per-stack routes —
a nick in a 300-bp fragment and a bond between two origami rectangles — can be said to agree to about 8 % (−2.42 against −2.63).
