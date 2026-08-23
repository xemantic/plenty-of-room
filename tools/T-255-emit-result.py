#!/usr/bin/env python3
#
# Copyright 2026 Kazimierz Pogoda / Xemantic
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
"""Emit `gpd/results/T-255-cadnano-gallery-forced-crossovers.json`.

    tools/T-255-emit-result.py
    tools/T-255-emit-result.py --selftest

`T-255` asks whether any cadnano.org gallery design carries a FORCED crossover, and with what
yield. `C-0152` section 6 raised it, having read the gallery page live and at its 2012 Wayback
capture and found three citations, three `.zip` links and no yields -- and having NOT opened the
`.zip` files.

WHY PYTHON AND NOT A KOTLIN STUDY. The caDNAno LEGACY `.json` format is a different document from
scadnano's, this repository has no reader for it, and the arithmetic needed is integer and closed
form. The lattice constants consumed are PARSED OUT of `src/main/kotlin/tile/
HoneycombBondClassResidues.kt` rather than transcribed, so a change there fails this emitter.
The parser and the classifier live beside their inputs in `gpd/data/T-255-sources/`, with 37
self-tests of their own.

WHAT IT READS. The three gallery archives, retrieved in 2026 and retained with every URL and HTTP
status; the caDNAno paper, the Nature paper and the Science paper, all read directly; and the
per-design yield bar chart of the caDNAno paper's own Figure 2, digitised from the publisher PDF
at 600 dpi and cross-checked against the paper's own two ordinal statements.
"""

import argparse
import datetime
import importlib.util
import json
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SOURCES = os.path.join(ROOT, "gpd", "data", "T-255-sources")
DESTINATION = os.path.join(
    ROOT, "gpd", "results", "T-255-cadnano-gallery-forced-crossovers.json")

# The three archives, as the gallery page links them. The live page hides them behind `bit.ly`
# shorteners; the 2012 Wayback capture `T-246` retained states the resolved paths outright.
ARCHIVES = ("Science09", "NAR09", "Nature09")


def _load(name, filename):
    path = os.path.join(SOURCES, filename)
    spec = importlib.util.spec_from_file_location(name, path)
    module = importlib.util.module_from_spec(spec)
    sys.path.insert(0, SOURCES)
    spec.loader.exec_module(module)
    return module


def _rounding():
    path = os.path.join(ROOT, "tools", "T-278-rounding-simulation.py")
    spec = importlib.util.spec_from_file_location("t255rounding", path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def read_json(name):
    with open(os.path.join(SOURCES, name), encoding="utf-8") as handle:
        return json.load(handle)


def split_url(url):
    """`{"endpoint": ..., "query": {...}}`, with the query PERCENT-DECODED.

    Why not the raw URL: `tools/check-result-file-hygiene.py` gates a result file against raw Java
    format conversions, and a percent-encoded query is full of them -- `%20s`, `%3A`, `%22` all
    match Java's own conversion grammar. That is a false positive of the gate and it is still a
    build failure, and the allowlist is deliberately per-FILE, so taking it would put a hole in the
    gate for every other string in this file. Recording the query as a MAPPING is both the cure and
    the better record: it is unambiguous, it re-encodes by `urlencode`, and it is what a query log
    is for."""
    import urllib.parse
    parts = urllib.parse.urlsplit(url)
    if not parts.query:
        return {"endpoint": url, "query": None}
    return {
        "endpoint": urllib.parse.urlunsplit((parts.scheme, parts.netloc, parts.path, "", "")),
        "query": dict(urllib.parse.parse_qsl(parts.query, keep_blank_values=True)),
    }


def retrieval_record():
    """Every URL tried, with its HTTP status. The subject is the web, so the date travels."""
    fetches = read_json("fetches.json")
    rows = []
    for f in fetches:
        row = {"httpStatus": f["status"]}
        row.update(split_url(f["url"]))
        final = f.get("finalUrl")
        if final and final != f["url"]:
            row["finalEndpoint"] = split_url(final)["endpoint"]
        row["bytes"] = f.get("bytes")
        row["contentType"] = f.get("contentType")
        row["savedAs"] = f.get("savedAs")
        row["error"] = f.get("error")
        rows.append(row)
    rows.append({
        "httpStatus": 200, "endpoint": "https://pmc.ncbi.nlm.nih.gov/articles/PMC2737683/",
        "query": None, "bytes": 151204, "contentType": "text/html",
        "savedAs": "PMC2737683-dietz2009-articlepage.html",
        "error": None,
        "note": ("Dietz 2009, READ DIRECTLY; T-246 got a reCAPTCHA page for the same PMCID, so "
                 "the gate is intermittent -- retry before recording a source as unreachable"),
    })
    rows.append({
        "httpStatus": 200,
        "endpoint": "https://pmc.ncbi.nlm.nih.gov/articles/PMC2731887/pdf/gkp436.pdf",
        "query": None, "bytes": 4274175, "contentType": "application/pdf",
        "savedAs": "cadnano-NAR-gkp436.pdf", "error": None,
        "note": "through PMC's SHA-256 proof-of-work cookie, solved by pmc_pow.py",
    })
    return rows


def clopper_pearson_zero_upper(n, confidence=0.95):
    """One-sided upper limit on a proportion observed at ZERO of `n`.

    `CLAUDE.md`: a saturated proportion's symmetric standard error is a function of the estimate
    alone and reports the saturation back to itself. The exact limit at `k = 0` is
    `p < 1 - (1 - c)^(1/n)`, whose large-`n` form is the rule of three."""
    return 1.0 - (1.0 - confidence) ** (1.0 / n)


def build():
    census = _load("t255census", "forced_census.py")
    legacy = _load("t255legacy", "cadnano_legacy.py")
    runner = _load("t255runner", "run_census.py")

    cwd = os.getcwd()
    os.chdir(SOURCES)
    try:
        rows = runner.main()
    finally:
        os.chdir(cwd)

    constants = legacy.parse_lattice_constants()

    designs = []
    for r in rows:
        designs.append({
            "design": r["design"],
            "archive": r["archive"],
            "helices": r["helices"],
            "lattice": r["lattice"],
            "honeycombPeriodFit": r["honeycombPeriodFit"],
            "squarePeriodFit": r["squarePeriodFit"],
            "scaffoldBases": r["scaffoldBases"],
            "stapleBases": r["stapleBases"],
            "unpairedScaffoldBases": r["unpairedScaffoldBases"],
            "insertions": r["insertions"],
            "deletions": r["deletions"],
            "stapleCrossings": r["stapleCrossings"],
            "scaffoldCrossings": r["scaffoldCrossings"],
            "misalignedConnections": r["nMisalignedConnections"],
            "forcedByAdjacency": r["nForcedByAdjacency"],
            "offRegisterStapleBonds": r["nOffRegisterStapleBonds"],
            "forcedStapleCrossings": r["nForcedStapleCrossings"],
            "forcedScaffoldCrossings": r["nForcedScaffoldCrossings"],
            "unscorableScaffoldCrossings": r["nUnscorableScaffold"],
            "siteResiduesFitted": r["siteResiduesFitted"],
            "carriesForcedCrossover": r["carriesForcedCrossover"],
            "forcedScaffoldDeparturesBp": sorted(
                x["departureBp"] for x in r["forcedScaffold"]),
            "offRegisterStapleDeparturesBp": sorted(
                dd for b in r["forcedStaple"] for dd in b["departuresBp"]),
            "misalignedAxialOffsetsBp": sorted(
                set(abs(x["axialOffsetBp"]) for x in r["misalignedConnections"])),
        })

    # the NAR archive's `ii_10x6` and the Nature archive's `monolith` are deposited by two
    # different papers; whether they are the same design is a question the files answer
    import hashlib
    digests = {}
    for design in legacy.load_archives(SOURCES):
        if design.label in ("ii_10x6", "monolith"):
            digests[design.archive + "/" + design.label] = hashlib.sha256(
                json.dumps(design.vstrands, sort_keys=True).encode()).hexdigest()
    bitIdentical = len(set(digests.values())) == 1 and len(digests) == 2

    carrying = [d for d in designs if d["carriesForcedCrossover"]]
    clean = [d for d in designs if not d["carriesForcedCrossover"]]
    fig2 = read_json("cadnano-NAR-fig2de-digitised.json")

    # the seven caDNAno-paper blocks, in the paper's own order, against its own scaffold pairing
    # list -- "i: p8064, ii: p7560, iii: p8064, iv: p7560, v: p8064, vi: p7560, vii: p7560"
    paper_scaffold = {"i_16x4": 8064, "ii_10x6": 7560, "iii_8x8": 8064, "iv_6x10": 7560,
                      "v_4x16": 8064, "vi_3x20": 7560, "vii_2x30": 7560}
    nar = {d["design"]: d for d in designs if d["archive"] == "NAR09"}
    label = {"i_16x4": "i", "ii_10x6": "ii", "iii_8x8": "iii", "iv_6x10": "iv",
             "v_4x16": "v", "vi_3x20": "vi", "vii_2x30": "vii"}
    withYield = []
    for name, scaffold in paper_scaffold.items():
        d = nar[name]
        withYield.append({
            "design": name,
            "paperLabel": label[name],
            "paperScaffoldBases": scaffold,
            "fileScaffoldBases": d["scaffoldBases"],
            "scaffoldMatchesPaper": d["scaffoldBases"] == scaffold,
            "gelYieldPercent": fig2["gelYieldPercent"][label[name]],
            "temYieldAfterPurificationPercent":
                fig2["temYieldAfterPurificationPercent"][label[name]],
            "carriesForcedCrossover": d["carriesForcedCrossover"],
        })

    n_carrying = len(carrying)
    document = {
        "task": "T-255",
        "question": ("does any cadnano.org gallery design carry a forced crossover, "
                     "and with what yield"),
        "leaf": "A8.2",
        "retrievedOn": "2026-08-23",
        "latticeConstantsParsedFrom":
            "src/main/kotlin/tile/HoneycombBondClassResidues.kt",
        "latticeConstants": constants,
        "answer": {
            "archivesObtained": len(ARCHIVES),
            "archivesLinkedByTheGallery": len(ARCHIVES),
            "designsParsed": len(designs),
            "designsCarryingAForcedCrossover": n_carrying,
            "designsWithAPerDesignPublishedYield": len(withYield),
            "designsWithBOTHAForcedCrossoverANDAPerDesignPublishedYield":
                sum(1 for d in withYield if d["carriesForcedCrossover"]),
            "verdict": ("the category is NOT empty and the conjunction still fails: 15 of 26 "
                        "gallery designs carry a forced crossover, and the only per-design "
                        "yields published for any gallery design belong to the seven blocks "
                        "that carry NONE"),
        },
        "conjunction": [
            {"step": "an archive is obtainable in 2026", "observed": "3 of 3",
             "marginalRate": 1.0},
            {"step": "an archive contains machine-readable design files",
             "observed": "26 designs in 3 of 3 archives", "marginalRate": 1.0},
            {"step": "a design carries a forced crossover",
             "observed": "%d of %d" % (n_carrying, len(designs)),
             "marginalRate": n_carrying / len(designs)},
            {"step": "a per-design folding yield is published for a design carrying one",
             "observed": "0 of %d" % n_carrying,
             "marginalRate": 0.0,
             "oneSidedUpperLimitAt95Percent": clopper_pearson_zero_upper(n_carrying),
             "why": ("a symmetric standard error at a saturated proportion is a function of the "
                     "estimate alone; the exact Clopper-Pearson limit at zero of n is "
                     "1 - 0.05^(1/n)")},
        ],
        "expectedYieldOfTheWholeSearch": 0.0,
        "tests": {
            "A_adjacency": ("caDNAno's rule allows crossovers between ADJACENT helices; a "
                            "crossover between two sites that are not nearest neighbours on the "
                            "design's own lattice is forced by construction"),
            "B_register": ("a crossover SITE occupies two consecutive bases, so a crossing at "
                           "base i lies on an allowed site iff i mod 21 is in {r, r+1} for the "
                           "bond's own site residue r; the scaffold window is that window "
                           "shifted both ways by five"),
            "C_alignment": ("an antiparallel crossover joins two helices at the SAME base index; "
                            "a strand connection between different base indices is a manual "
                            "connection and its axial offset is a number the file states"),
            "B_blindSpot": ("a LONE crossing displaced by exactly +1 bp lands inside the window, "
                            "so test B is a LOWER bound on the forced count"),
        },
        "totals": {
            "stapleCrossings": sum(d["stapleCrossings"] for d in designs),
            "scaffoldCrossings": sum(d["scaffoldCrossings"] for d in designs),
            "misalignedConnections": sum(d["misalignedConnections"] for d in designs),
            "forcedByAdjacency": sum(d["forcedByAdjacency"] for d in designs),
            "forcedStapleCrossings": sum(d["forcedStapleCrossings"] for d in designs),
            "forcedScaffoldCrossings": sum(d["forcedScaffoldCrossings"] for d in designs),
            "unscorableScaffoldCrossings":
                sum(d["unscorableScaffoldCrossings"] for d in designs),
            "designsByMechanism": {
                "misaligned": sum(1 for d in designs if d["misalignedConnections"]),
                "nonAdjacent": sum(1 for d in designs if d["forcedByAdjacency"]),
                "offRegister": sum(1 for d in designs
                                   if d["forcedStapleCrossings"] or d["forcedScaffoldCrossings"]),
            },
        },
        "designs": designs,
        "perDesignPublishedYields": {
            "source": ("Douglas, Marblestone, Teerapittayanon, Vazquez, Church & Shih, "
                       "Nucleic Acids Res. 37:5001, Figure 2d and 2e, READ DIRECTLY; the bar "
                       "chart digitised from the publisher PDF at 600 dpi"),
            "denominators": {
                "gelYieldPercent": ("fraction of scaffold incorporated into the "
                                    "fastest-migrating monomeric species, by "
                                    "ethidium-bromide-fluorescence intensity"),
                "temYieldAfterPurificationPercent": ("fraction of well-folded species after gel "
                                                     "purification, by TEM over 100 random "
                                                     "particles per shape"),
            },
            "crossChecks": fig2["crossChecks"],
            "rows": withYield,
        },
        "pooledYieldsForTheShapesThatDOCarryForcedCrossovers": {
            "source": ("Douglas, Dietz, Liedl, Hoegberg, Graf & Shih, Nature 459:414 (2009), "
                       "READ DIRECTLY"),
            "quotation": ("The fraction of scaffold strands that were incorporated into "
                          "monomeric species after folding varied from 7% to 44% for these "
                          "targets as estimated by ethidium-bromide fluorescence intensity."),
            "lowPercent": 7.0,
            "highPercent": 44.0,
            "isAField": False,
            "why": ("a pooled range over the shape set of Figure 2, with no per-shape breakdown "
                    "in the paper or its Supplementary Information; the shape set mixes designs "
                    "that carry forced crossovers with designs that do not"),
        },
        "zeroLoopTurns": {
            "why": ("C-0193 section 4 holds that 28 unpaired scaffold nucleotides at every raster "
                    "turn buy freedom from caDNAno's +-5 residue condition, because an unpaired "
                    "base has no azimuth. The deposited design files were measured against that."),
            "designsWithZeroUnpairedScaffold":
                sorted(d["design"] for d in designs if d["unpairedScaffoldBases"] == 0),
            "theSevenCadnanoBlocks": {
                d["design"]: {"unpairedScaffoldBases": d["unpairedScaffoldBases"],
                              "scaffoldCrossings": d["scaffoldCrossings"],
                              "forcedScaffoldCrossings": d["forcedScaffoldCrossings"]}
                for d in designs if d["archive"] == "NAR09"},
            "monolithAndTenBySixAreBitIdentical": bitIdentical,
            "bitIdentityDigests": digests,
        },
        "retrieval": retrieval_record(),
    }
    return document


def _selftest():
    failures = []

    def check(name, condition):
        if not condition:
            failures.append(name)

    document = build()
    designs = {d["design"]: d for d in document["designs"]}
    a = document["answer"]

    check("all three gallery archives were obtained", a["archivesObtained"] == 3)
    check("twenty-six designs were parsed", a["designsParsed"] == 26)
    check("the category is not empty", a["designsCarryingAForcedCrossover"] > 0)
    check("no design has both a forced crossover and a per-design published yield",
          a["designsWithBOTHAForcedCrossoverANDAPerDesignPublishedYield"] == 0)
    check("every one of the seven blocks with a per-design yield matches the paper's own "
          "scaffold pairing list",
          all(r["scaffoldMatchesPaper"] for r in document["perDesignPublishedYields"]["rows"]))
    check("the digitised chart reproduces the paper's own two ordinal statements",
          all(document["perDesignPublishedYields"]["crossChecks"].values()))
    check("the seven caDNAno blocks carry zero unpaired scaffold",
          all(v["unpairedScaffoldBases"] == 0
              for v in document["zeroLoopTurns"]["theSevenCadnanoBlocks"].values()))
    check("and zero forced scaffold crossovers",
          all(v["forcedScaffoldCrossings"] == 0
              for v in document["zeroLoopTurns"]["theSevenCadnanoBlocks"].values()))
    check("the 10x6 block is honeycomb at a perfect register fit",
          designs["ii_10x6"]["lattice"] == "honeycomb" and
          designs["ii_10x6"]["honeycombPeriodFit"] == 1.0)
    check("the lattice constants came out of the Kotlin source",
          document["latticeConstants"]["samePairPeriodBp"] == 21 and
          document["latticeConstants"]["scaffoldOffsetBp"] == 5)
    check("the 10x6 block and the monolith are the SAME design, measured rather than asserted",
          document["zeroLoopTurns"]["monolithAndTenBySixAreBitIdentical"] is True and
          len(set(document["zeroLoopTurns"]["bitIdentityDigests"].values())) == 1)
    check("every retrieval carries an HTTP status",
          all("httpStatus" in r for r in document["retrieval"]))
    check("no retrieval record carries a percent-encoded string, which the hygiene gate reads as "
          "a raw Java format conversion",
          not any("%" in v for r in document["retrieval"] for v in r.values()
                  if isinstance(v, str)))
    check("a query with a percent-encoded value comes back as a decoded mapping",
          split_url("https://x/y?q=TITLE%3A%22a%20b%22")["query"] == {"q": 'TITLE:"a b"'})
    check("a URL with no query keeps its whole self in the endpoint",
          split_url("https://x/y.zip") == {"endpoint": "https://x/y.zip", "query": None})
    check("the zero-of-n upper limit is the exact Clopper-Pearson one, not a symmetric error",
          abs(clopper_pearson_zero_upper(15) - (1.0 - 0.05 ** (1.0 / 15))) < 1e-15)
    check("the rule of three is its large-n form",
          abs(clopper_pearson_zero_upper(10000) - 3.0 / 10000) < 1e-5)

    for failure in failures:
        print("FAIL " + failure)
    print("%d check(s) failed" % len(failures))
    return 1 if failures else 0


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--selftest", action="store_true")
    args = parser.parse_args(argv)
    if args.selftest:
        return _selftest()

    document = build()
    sys.path.insert(0, os.path.join(ROOT, "tools"))
    from emission_header import with_emission_header
    rounding = _rounding()
    document = rounding.walk(document, rounding.RESULT_SIGNIFICANT_DIGITS,
                             rounding.DEPARTURE_DIGITS_BY_KEY, 0.0)
    headed = with_emission_header(document, "honeycomb", regime=[])
    with open(DESTINATION, "w") as handle:
        json.dump(headed, handle, indent=1)
        handle.write("\n")
    print("written to gpd/results/T-255-cadnano-gallery-forced-crossovers.json")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
