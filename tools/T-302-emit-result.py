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
"""Emit `gpd/results/T-302-si-staple-order.json`.

    tools/T-302-emit-result.py
    tools/T-302-emit-result.py --selftest

`T-302` settles `CH-0251` by counting one column. A design leaving 28 unpaired scaffold bases per
helix orders `60 x 98 = 5880` staple nucleotides for the `10 x 6` honeycomb block; the deposited
caDNAno file draws `60 x 126 = 7560`. The 2009 supplementary staple sequence tables say which was
bought.

WHY PYTHON AND NOT A KOTLIN STUDY. The two inputs are a PDF text layer and a caDNAno LEGACY
`.json`; this repository has a reader for neither in Kotlin, and the arithmetic is exact integer
matching. The table reader lives beside its inputs in `gpd/data/T-302-sources/si_tables.py` with
its own self-tests, exactly as `T-255`'s legacy reader does; the design files are `T-255`'s own
retained archives and are not refetched.

WHAT IT READS. `gpd/data/T-296-sources/douglas2009-SI.pdf` (retained by `T-296`, 13.4 MB) and
`gpd/data/T-255-sources/{Nature09,NAR09}.zip` (retained by `T-255`). Every URL tried in the
search for the caDNAno paper's OWN supplementary staple lists is in
`gpd/data/T-302-sources/fetches-1.json` with its HTTP status; none of them reached the tables.
"""

import argparse
import hashlib
import importlib.util
import json
import os
import sys
import urllib.parse
import zipfile
from collections import Counter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SOURCES = os.path.join(ROOT, "gpd", "data", "T-302-sources")
SI_PDF = os.path.join(ROOT, "gpd", "data", "T-296-sources", "douglas2009-SI.pdf")
ARCHIVES = os.path.join(ROOT, "gpd", "data", "T-255-sources")
DESTINATION = os.path.join(ROOT, "gpd", "results", "T-302-si-staple-order.json")

# the SI's table pages; pages 1-11 are the strand-diagram figures, whose subsetted fonts carry no
# `/ToUnicode` -- `T-296` recorded the garble and it is a property of those pages only
# The date the supplementary PDF and the archives were READ, and the date the retrieval log below
# was taken. It is a constant and not `date.today()`: the sources are retained, immutable and
# digested below, so this file must reproduce byte for byte on any later day (`CH-0246` -- a
# result file whose subject touches the web names the state it measured; here that state is the
# retrieval log plus three sha256 digests). `--read-on` overrides it for a re-read.
READ_ON = "2026-08-23"

FIRST_TABLE_PAGE = 12
LAST_PAGE = 26

# a table page's title against the deposited design it tabulates; a design absent from the gallery
# archives has no entry and is matched against nothing
TABULATED_DESIGNS = {
    "monolith staple sequences": ("Nature09", "Nature09/01-monolith/monolith.json"),
    "square nut staple sequences": ("Nature09", "Nature09/02-squarenut/squarenut.json"),
    "railed bridge staple sequences": ("Nature09", "Nature09/03-railedbridge/railedbridge.json"),
    "slotted cross staple sequences": ("Nature09", "Nature09/04-slottedcross/slottedcross.json"),
    "stacked cross core staple sequences":
        ("Nature09", "Nature09/05-stackedcross/stackedcross.json"),
    "icosahedron monomer A core sequences":
        ("Nature09", "Nature09/07-icosahedron/icosahedron.json"),
    "icosahedron monomer B core sequences":
        ("Nature09", "Nature09/07-icosahedron/icosahedron.json"),
    "icosahedron monomer C core sequences":
        ("Nature09", "Nature09/07-icosahedron/icosahedron.json"),
}

MONOLITH_TITLE = "monolith staple sequences"
MONOLITH_FILE = "Nature09/01-monolith/monolith.json"
TEN_BY_SIX_FILE = "NAR09/ii_10x6.json"

# the two readings the challenge names, before anything is read
SENTENCE_READING = 60 * 98      # 5880 -- the caDNAno paper's Methods sentence
FILE_READING = 60 * 126         # 7560 -- what the deposited design file draws


def _load_si_tables():
    path = os.path.join(SOURCES, "si_tables.py")
    spec = importlib.util.spec_from_file_location("t302sitables", path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def _rounding():
    path = os.path.join(ROOT, "tools", "T-278-rounding-simulation.py")
    spec = importlib.util.spec_from_file_location("t302rounding", path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def sha256_of(path):
    with open(path, "rb") as handle:
        return hashlib.sha256(handle.read()).hexdigest()


def split_url(url):
    """`{"endpoint": ..., "query": {...}}`, with the query PERCENT-DECODED.

    `T-255`'s helper, for `T-255`'s reason: `tools/check-result-file-hygiene.py` reads a
    percent-encoded query as a raw Java format conversion, and the allowlist is per-FILE.
    """
    parts = urllib.parse.urlsplit(url)
    if not parts.query:
        return {"endpoint": url, "query": None}
    return {
        "endpoint": urllib.parse.urlunsplit((parts.scheme, parts.netloc, parts.path, "", "")),
        "query": dict(urllib.parse.parse_qsl(parts.query, keep_blank_values=True)),
    }


def _vstrands(archive, member):
    with zipfile.ZipFile(os.path.join(ARCHIVES, archive + ".zip")) as z:
        return json.loads(z.read(member))["vstrands"]


def build(read_on=READ_ON):
    si = _load_si_tables()
    xml = si.bbox_xml(SI_PDF, FIRST_TABLE_PAGE, LAST_PAGE)
    pages = si.pages_of(xml)

    tables = []
    for offset, page in enumerate(pages):
        title, rows = si.parse_table(page)
        if not rows:
            continue
        tables.append({"page": FIRST_TABLE_PAGE + offset, "title": title, "rows": rows})

    # ---------------------------------------------------------------- every table, matched
    per_table = []
    monolith = None
    for table in tables:
        record = {
            "page": table["page"],
            "title": table["title"],
            "rows": len(table["rows"]),
            "sumOfLengthColumn": sum(r["length"] for r in table["rows"]),
            "rowsWhereStatedLengthIsTheSequenceLength": sum(
                1 for r in table["rows"]
                if r["sequence"] is not None and len(r["sequence"]) == r["length"]),
            "deposited": None,
        }
        target = TABULATED_DESIGNS.get(table["title"])
        if target is not None:
            vstrands = _vstrands(*target)
            strands = si.staple_strands(vstrands)
            colours = si.staple_colours(vstrands)
            report = si.match(table["rows"], strands, colours)
            record["deposited"] = {k: v for k, v in report.items() if not k.startswith("_")}
            record["deposited"]["file"] = target[1]
            if table["title"] == MONOLITH_TITLE:
                monolith = (table, vstrands, strands, colours, report)
        per_table.append(record)

    if monolith is None:
        raise RuntimeError("the monolith staple table was not found in the supplementary PDF")
    table, vstrands, strands, colours, report = monolith

    # ---------------------------------------------------------------- the 10 x 6, in detail
    windows = si.paired_windows(strands, report["_orderedKeys"])
    allotted = {h: len([1 for e in v["stap"] if e != [si.NONE] * 4])
                for v in vstrands for h in [v["num"]]}
    drawn_spans = {}
    for v in vstrands:
        idx = [i for i, e in enumerate(v["stap"]) if e != [si.NONE] * 4]
        drawn_spans[v["num"]] = (idx[0], idx[-1])

    per_helix = []
    for h in sorted(windows):
        first, last, count = windows[h]
        a, z = drawn_spans[h]
        per_helix.append({
            "helix": h,
            "scaffoldAllottedBases": allotted[h],
            "drawnStapleSpan": [a, z],
            "orderedStapleWindow": [first, last],
            "basesPairedByAnOrderedStaple": count,
            "unpairedAtTheLowEnd": first - a,
            "unpairedAtTheHighEnd": z - last,
        })
    paired_counts = Counter(r["basesPairedByAnOrderedStaple"] for r in per_helix)
    splits = Counter((r["unpairedAtTheLowEnd"], r["unpairedAtTheHighEnd"]) for r in per_helix)
    ordered_windows = Counter(tuple(r["orderedStapleWindow"]) for r in per_helix)

    # the scaffold, walked, and the ssDNA between consecutive DUPLEX ends
    path = si.scaffold_path(vstrands)
    inside = [i for i, (h, b) in enumerate(path)
              if windows[h][0] <= b <= windows[h][1]]
    gaps = [z - a - 1 for a, z in zip(inside, inside[1:]) if z - a > 1]
    crossings = si.scaffold_crossings(vstrands)
    rim = [c for c in crossings
           if not all(windows[h][0] <= b <= windows[h][1] for h, b in c)]

    # ---------------------------------------------------------------- the colour proxy, tested
    # `#cc0000` is caDNAno's default staple colour. On the monolith EVERY omitted strand carries
    # it and no ordered strand does, which looks like a rule. It is tested on every design where
    # a table exists BEFORE it is carried anywhere, and it fails on three of five.
    proxy = []
    for record in per_table:
        deposited = record["deposited"]
        if deposited is None or record["title"].startswith("icosahedron"):
            continue
        omitted_red = deposited["drawnOnlyColourHistogram"].get("#cc0000", 0)
        ordered_red = deposited["tabulatedColourHistogram"].get("#cc0000", 0)
        predicted = omitted_red + ordered_red
        proxy.append({
            "design": deposited["file"],
            "strandsTheTableOmits": deposited["strandsDrawnButNotTabulated"],
            "strandsCarryingCadnanoDefaultRed": predicted,
            "strandsCarryingItThatWereORDERED": ordered_red,
            "proxyIsExact": (predicted == deposited["strandsDrawnButNotTabulated"]
                             and ordered_red == 0),
        })

    # ---------------------------------------------------------------- provenance
    digests = {}
    for archive, member in (("Nature09", MONOLITH_FILE), ("NAR09", TEN_BY_SIX_FILE)):
        digests[member] = hashlib.sha256(
            json.dumps(_vstrands(archive, member), sort_keys=True).encode()).hexdigest()
    bit_identical = len(set(digests.values())) == 1 and len(digests) == 2

    sum_of_lengths = report["sumOfLengthColumn"]
    verdict_is_the_sentence = sum_of_lengths == SENTENCE_READING

    document = {
        "task": "T-302",
        "question": ("do the 2009 supplementary staple sequence tables order 5880 staple "
                     "nucleotides for the 10 x 6 honeycomb block, or 7560"),
        "leaf": "A8.2",
        "readOn": read_on,
        "settles": "CH-0251",
        "answer": {
            "sumOfTheLengthColumn": sum_of_lengths,
            "tableRows": report["tableRows"],
            "theDepositedFileDraws": report["fileStapleNucleotides"],
            "inStrands": report["fileStrands"],
            "theSentenceReading": SENTENCE_READING,
            "theFileReading": FILE_READING,
            "theOrderIsTheSentence": verdict_is_the_sentence,
            "nucleotidesDrawnButNotOrdered": report["nucleotidesDrawnButNotTabulated"],
            "strandsDrawnButNotOrdered": report["strandsDrawnButNotTabulated"],
            "unpairedScaffoldBasesPerHelixUnderTheOrder":
                report["nucleotidesDrawnButNotTabulated"] // len(per_helix),
            "verdict": ("the 2009 supplementary information orders 5880 staple nucleotides in "
                        "144 strands for the 10 x 6 block, which is exactly 60 x 98; the "
                        "deposited caDNAno file draws 214 strands and 7560 nucleotides, and the "
                        "70 it draws and the order omits total exactly 1680 = 60 x 28 and lie "
                        "entirely in the helix end regions. The Methods sentence describes the "
                        "object that was bought and the design file is a DRAWING of it"),
            "challengeVerdict": "CH-0251 REFUTED on its central point",
        },
        "theDiscriminator": {
            "why": ("stated in CH-0251 before anything was read: a design leaving 28 unpaired "
                    "scaffold bases per helix orders 60 x 98 staple nucleotides for this block "
                    "and the deposited file draws 60 x 126"),
            "candidates": [SENTENCE_READING, FILE_READING],
            "observed": sum_of_lengths,
            "matchesTheSentence": sum_of_lengths == SENTENCE_READING,
            "matchesTheFile": sum_of_lengths == FILE_READING,
        },
        "theMonolithTable": {
            "source": ("Douglas, Dietz, Liedl, Hoegberg, Graf & Shih, Nature 459:414 (2009), "
                       "Supplementary Information, the table headed 'monolith staple sequences', "
                       "READ DIRECTLY from the retained PDF's own text layer"),
            "page": table["page"],
            "columns": ["Start", "End", "monolith staple sequences", "Length", "Color"],
            "textLayerIsClean": True,
            "whyTheTextLayerIsClean": (
                "T-296 recorded this PDF's text layer as garbled by font subsetting past the "
                "front matter, and that is true of the FIGURE pages, whose subsetted TrueType "
                "fonts carry no ToUnicode map and no post table. The TABLE pages are typeset "
                "normally and extract verbatim. The check is one pdftotext invocation and it "
                "turned a 26-page raster transcription into a geometric read"),
            "match": {k: v for k, v in report.items() if not k.startswith("_")},
        },
        "perHelixUnderTheOrder": {
            "why": ("the Methods sentence says 98 of 126 bases are paired on EVERY helix; that "
                    "is a per-helix statement and is emitted at per-helix granularity"),
            "helices": len(per_helix),
            "basesPairedByAnOrderedStapleHistogram": dict(sorted(paired_counts.items())),
            "orderedStapleWindowHistogram": {
                "%d..%d" % k: v for k, v in sorted(ordered_windows.items())},
            "unpairedEndSplitHistogram": {
                "%d low, %d high" % k: v for k, v in sorted(splits.items())},
            "everyHelixCarriesTheSentencesNinetyEight":
                set(paired_counts) == {98} and len(per_helix) == 60,
            "theSplitIsNotFourteenAndFourteen": set(splits) == {(12, 16), (16, 12)},
            "note": ("the DUPLEX is in perfect register -- all sixty helices are paired over "
                     "bases 28..125 -- and the 4 bp axial stagger the file draws lives entirely "
                     "in the single-stranded ends"),
        },
        "theScaffold": {
            "basesInTheScaffoldPath": len(path),
            "basesInsideAnOrderedDuplexWindow": len(inside),
            "basesOutside": len(path) - len(inside),
            "ssDnaGapsBetweenConsecutiveDuplexEnds": len(gaps),
            "gapSizeHistogramInNucleotides": dict(sorted(Counter(gaps).items())),
            "meanGapInNucleotides": sum(gaps) / len(gaps),
            "totalUnpairedNucleotides": sum(gaps),
            "scaffoldCrossings": len(crossings),
            "scaffoldCrossingsAtARimInsideTheUnpairedRegion": len(rim),
            "scaffoldCrossingsInsideADuplexWindow": len(crossings) - len(rim),
            "whatThisCorrects": (
                "C-0193 section 3 reads the strand diagram as 28 unpaired nucleotides between "
                "two duplex ends, 14 on each side. The 28 is exact as a MEAN and the per-turn "
                "values are 24 at thirty turns and 32 at the other thirty, because the stagger "
                "puts two 12-base ends together at one rim and two 16-base ends together at the "
                "other"),
            "ssDnaContourNanometresPerNucleotide": [0.65, 0.70],
            "contourOfATwentyFourNucleotideTurnNanometres": [24 * 0.65, 24 * 0.70],
            "contourOfAThirtyTwoNucleotideTurnNanometres": [32 * 0.65, 32 * 0.70],
            "contourOfATwelveNucleotideHelixEndNanometres": [12 * 0.65, 12 * 0.70],
            "contourOfASixteenNucleotideHelixEndNanometres": [16 * 0.65, 16 * 0.70],
            "whyThoseAreNotADuplexRise": (
                "C-0193 section 3 quotes the outboard offset of the covalent link as "
                "14 bp = 4.76 nm, which is the B-DNA rise applied to SINGLE-STRANDED scaffold -- "
                "the unit trap CLAUDE.md records as 28 nt = 9.52 nm, and which T-296 caught in "
                "the row below and left standing in the row above. The count is 12 or 16, not "
                "14, and an unpaired base has no duplex axial extent at all"),
        },
        "crossChecks": {
            "everyRowsStatedLengthIsItsOwnSequenceLength":
                report["rowsWhereStatedLengthIsTheSequenceLength"] == report["tableRows"],
            "everyRowResolvesToAStrandOfTheDepositedFile":
                report["rowsResolvingToAFileStrand"] == report["tableRows"],
            "andAtTheSameLength":
                report["rowsWhereTheLengthAlsoAgrees"] == report["tableRows"],
            "everyRowsColourIsTheFilesOwnStoredStapleColour":
                report["rowsWhereTheColourAgrees"] == report["tableRows"],
            "theTenBySixAndTheMonolithAreBitIdentical": bit_identical,
            "bitIdentityDigests": digests,
            "rowsCheckedAcrossAllTables": sum(t["rows"] for t in per_table),
            "rowsWhereStatedLengthIsItsSequenceLengthAcrossAllTables": sum(
                t["rowsWhereStatedLengthIsTheSequenceLength"] for t in per_table),
            "rowsMatchableAgainstADepositedFile": sum(
                t["deposited"]["tableRows"] for t in per_table if t["deposited"]),
            "rowsResolvingToADepositedStrandAtTheSameLength": sum(
                t["deposited"]["rowsWhereTheLengthAlsoAgrees"]
                for t in per_table if t["deposited"]),
            "rowsWhoseColourIsTheDepositedFilesOwn": sum(
                t["deposited"]["rowsWhereTheColourAgrees"]
                for t in per_table if t["deposited"]),
            "whyTheIcosahedronTablesLeaveALargeResidue": (
                "the icosahedron is deposited as ONE file carrying all three monomers, and each "
                "of the three tables orders one of them: 192 matched and 384 left over is 192 x 3 "
                "= 576, so that residue is the other two monomers and not an omission at all"),
        },
        "everyTableInTheSupplement": per_table,
        "theColourProxyIsREFUSED": {
            "why": ("on the monolith every strand the table omits carries caDNAno's default red "
                    "and no ordered strand does, which looks like a rule that could be carried "
                    "to the six blocks whose tables were not obtained. It is tested on every "
                    "design where a table EXISTS before being carried anywhere"),
            "rows": proxy,
            "exactOn": sum(1 for p in proxy if p["proxyIsExact"]),
            "testedOn": len(proxy),
            "carriedToTheUntabulatedBlocks": False,
            "verdict": ("the proxy is exact on the monolith and the square nut and WRONG on the "
                        "railed bridge, the slotted cross and the stacked cross, so it says "
                        "nothing about the six cross-sections whose tables were not obtained"),
        },
        "theOtherSixCrossSections": {
            "status": "NOT SETTLED",
            "why": ("the caDNAno paper states its own staple lists are in Supplementary Note 3 "
                    "-- 'detailed schematics and staple lists are included in Supplementary "
                    "Notes 2 and 3, respectively' -- and that supplement was NOT OBTAINED. PMC "
                    "does not host it, EuropePMC's supplementaryFiles endpoint returns the two "
                    "figures only, the publisher refuses the article page, and the Internet "
                    "Archive has no capture of the DC1 page's content"),
            "whatWouldSettleIt": ("the caDNAno paper's Supplementary Note 3, or any staple order "
                                  "for designs i, iii, iv, v, vi or vii"),
            "whatIsKnownWithoutIt": ("the Methods sentence's own scope is all seven "
                                     "cross-sections, and the one block where the sentence can "
                                     "be checked against an order reproduces it exactly"),
        },
        "whatThisDoesNotSettle": [
            "whether the object folded as ordered; a staple order is a purchase, not a "
            "micrograph. What is settled is what was BOUGHT",
            "the other six cross-sections, whose staple lists were not obtained",
            "why the design file carries end staples that were never ordered; the file is the "
            "record of a drawing and the order is the record of a purchase, and nothing here "
            "says which came first",
            "any energy, stiffness or flatness number; this result is a count",
        ],
        "falsifiers": [
            {"id": "F1",
             "firesIf": "fewer than every row of the monolith table resolves to a strand of the "
                        "deposited file, at the same length",
             "fired": report["rowsWhereTheLengthAlsoAgrees"] != report["tableRows"]},
            {"id": "F2",
             "firesIf": "the sum of the Length column is neither 5880 nor 7560",
             "fired": sum_of_lengths not in (SENTENCE_READING, FILE_READING)},
            {"id": "F3",
             "firesIf": "the strands the table omits do not total exactly 60 x 28",
             "fired": report["nucleotidesDrawnButNotTabulated"] != 60 * 28},
            {"id": "F4",
             "firesIf": "the ordered staple set does not leave exactly 98 paired bases on every "
                        "one of the sixty helices",
             "fired": not (set(paired_counts) == {98} and len(per_helix) == 60)},
            {"id": "F5",
             "firesIf": "any row's Color disagrees with the design file's own stored staple "
                        "colour, which would make the table and the file two generations of one "
                        "design",
             "fired": report["rowsWhereTheColourAgrees"] != report["tableRows"]},
            {"id": "F6",
             "firesIf": "the colour proxy is exact on every design where a table exists, which "
                        "would license carrying it to the six untabulated blocks",
             "fired": all(p["proxyIsExact"] for p in proxy)},
        ],
        "inputs": [
            {"path": "gpd/data/T-296-sources/douglas2009-SI.pdf",
             "what": "Douglas et al., Nature 459:414 (2009) Supplementary Information",
             "retainedBy": "T-296", "sha256": sha256_of(SI_PDF)},
            {"path": "gpd/data/T-255-sources/Nature09.zip",
             "what": "the cadnano.org gallery's Nature 2009 archive",
             "retainedBy": "T-255",
             "sha256": sha256_of(os.path.join(ARCHIVES, "Nature09.zip"))},
            {"path": "gpd/data/T-255-sources/NAR09.zip",
             "what": "the cadnano.org gallery's NAR 2009 archive",
             "retainedBy": "T-255",
             "sha256": sha256_of(os.path.join(ARCHIVES, "NAR09.zip"))},
        ],
        "retrieval": retrieval_record(),
    }
    return document


def retrieval_record():
    """Every URL tried in the search for the caDNAno paper's own staple lists, with its status."""
    rows = []
    notes = {
        "https://pmc.ncbi.nlm.nih.gov/articles/PMC2731887/":
            "the caDNAno paper on PMC; its only supplementary pointer is the dead OUP legacy URL",
        "https://www.ebi.ac.uk/europepmc/webservices/rest/PMC2731887/supplementaryFiles":
            "a real endpoint and it answers, but the archive holds the two FIGURES only",
        "https://web.archive.org/web/20150922014232/http://nar.oxfordjournals.org/cgi/content/"
        "full/gkp436/DC1":
            "the Internet Archive's only capture of the supplementary page is a 301 redirect",
        "https://web.archive.org/web/20160518225945/http://nar.oxfordjournals.org/content/37/15/"
        "5001/suppl/DC1":
            "the archived article page LINKS the supplementary page and the Archive never "
            "captured its content",
    }
    with open(os.path.join(SOURCES, "fetches-1.json"), encoding="utf-8") as handle:
        for fetch in json.load(handle):
            row = {"httpStatus": fetch["status"]}
            row.update(split_url(fetch["url"]))
            row["bytes"] = fetch["bytes"]
            row["note"] = notes.get(fetch["url"])
            rows.append(row)
    return rows


def _selftest():
    failures = []

    def check(name, condition):
        if not condition:
            failures.append(name)

    si = _load_si_tables()
    check("the table reader's own self-tests pass", si._selftest() == 0)

    document = build()
    answer = document["answer"]
    match = document["theMonolithTable"]["match"]

    check("the sum of the Length column is 5880, which is sixty times ninety-eight",
          answer["sumOfTheLengthColumn"] == 5880 == 60 * 98)
    check("the table carries 144 rows", answer["tableRows"] == 144)
    check("the deposited file draws 7560 nucleotides in 214 strands",
          answer["theDepositedFileDraws"] == 7560 and answer["inStrands"] == 214)
    check("the order is the sentence and not the file", answer["theOrderIsTheSentence"] is True)
    check("the residue is exactly sixty times twenty-eight",
          answer["nucleotidesDrawnButNotOrdered"] == 1680 == 60 * 28)
    check("and it is 70 strands", answer["strandsDrawnButNotOrdered"] == 70)
    check("which is 28 unpaired scaffold bases per helix",
          answer["unpairedScaffoldBasesPerHelixUnderTheOrder"] == 28)
    check("144 plus 70 is the file's own strand count",
          match["tableRows"] + match["strandsDrawnButNotTabulated"] == match["fileStrands"])
    check("5880 plus 1680 is the file's own nucleotide count",
          match["sumOfLengthColumn"] + match["nucleotidesDrawnButNotTabulated"]
          == match["fileStapleNucleotides"])

    checks = document["crossChecks"]
    check("every row's stated Length is its own sequence's length",
          checks["everyRowsStatedLengthIsItsOwnSequenceLength"] is True)
    check("every row resolves to a strand of the deposited file",
          checks["everyRowResolvesToAStrandOfTheDepositedFile"] is True)
    check("and at the same length", checks["andAtTheSameLength"] is True)
    check("every row's Color is the file's own stored staple colour",
          checks["everyRowsColourIsTheFilesOwnStoredStapleColour"] is True)
    check("no row resolves to no file strand", match["rowsResolvingToNoFileStrand"] == 0)
    check("the 10x6 and the monolith are one design, measured rather than asserted",
          checks["theTenBySixAndTheMonolithAreBitIdentical"] is True)
    check("every row matchable against a deposited file resolves to one, at the same length "
          "and carrying that file's own stored staple colour",
          checks["rowsMatchableAgainstADepositedFile"]
          == checks["rowsResolvingToADepositedStrandAtTheSameLength"]
          == checks["rowsWhoseColourIsTheDepositedFilesOwn"] == 1492)
    check("the length-against-sequence check holds over every table in the supplement",
          checks["rowsCheckedAcrossAllTables"]
          == checks["rowsWhereStatedLengthIsItsSequenceLengthAcrossAllTables"])
    check("twelve tables were read", len(document["everyTableInTheSupplement"]) == 12)

    per_helix = document["perHelixUnderTheOrder"]
    check("every one of the sixty helices carries the sentence's own ninety-eight",
          per_helix["everyHelixCarriesTheSentencesNinetyEight"] is True)
    check("the duplex is in perfect register over bases 28 to 125",
          list(per_helix["orderedStapleWindowHistogram"]) == ["28..125"])
    check("and the unpaired split is twelve and sixteen, not fourteen and fourteen",
          per_helix["theSplitIsNotFourteenAndFourteen"] is True)

    scaffold = document["theScaffold"]
    check("the scaffold path is the whole 7560", scaffold["basesInTheScaffoldPath"] == 7560)
    check("5880 of it lies inside an ordered duplex window",
          scaffold["basesInsideAnOrderedDuplexWindow"] == 5880)
    check("there are sixty single-stranded gaps between duplex ends",
          scaffold["ssDnaGapsBetweenConsecutiveDuplexEnds"] == 60)
    check("of 24 and 32 nucleotides, thirty each",
          scaffold["gapSizeHistogramInNucleotides"] == {24: 30, 32: 30})
    check("whose mean is exactly the sentence's twenty-eight",
          abs(scaffold["meanGapInNucleotides"] - 28.0) < 1e-12)
    check("sixty of the file's 118 scaffold crossings sit in unpaired scaffold",
          scaffold["scaffoldCrossings"] == 118
          and scaffold["scaffoldCrossingsAtARimInsideTheUnpairedRegion"] == 60)

    proxy = document["theColourProxyIsREFUSED"]
    check("the colour proxy is tested on five designs and exact on two",
          proxy["testedOn"] == 5 and proxy["exactOn"] == 2)
    check("and it is not carried to the untabulated blocks",
          proxy["carriedToTheUntabulatedBlocks"] is False)

    check("the other six cross-sections are reported as not settled",
          document["theOtherSixCrossSections"]["status"] == "NOT SETTLED")
    check("no declared falsifier fired", not any(f["fired"] for f in document["falsifiers"]))
    check("every retrieval carries an HTTP status",
          all("httpStatus" in r for r in document["retrieval"]))
    check("no retrieval record carries a percent-encoded string",
          not any("%" in v for r in document["retrieval"] for v in r.values()
                  if isinstance(v, str)))
    check("a query with a percent-encoded value comes back as a decoded mapping",
          split_url("https://x/y?url=a%2Fb")["query"] == {"url": "a/b"})
    check("the retrieval log records at least one refusal, so it is not a success list",
          any(r["httpStatus"] != 200 for r in document["retrieval"]))
    check("the read date is a constant and not today, so the file reproduces on any later day",
          document["readOn"] == READ_ON and build("1999-01-01")["readOn"] == "1999-01-01")
    check("every retained input carries a digest",
          all(len(i["sha256"]) == 64 for i in document["inputs"]))

    for failure in failures:
        print("FAIL " + failure)
    print("%d check(s) failed" % len(failures))
    return 1 if failures else 0


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--selftest", action="store_true")
    parser.add_argument("--read-on", default=READ_ON,
                        help="the date the retained sources were read; defaults to %s" % READ_ON)
    args = parser.parse_args(argv)
    if args.selftest:
        return _selftest()

    document = build(args.read_on)
    sys.path.insert(0, os.path.join(ROOT, "tools"))
    from emission_header import with_emission_header
    rounding = _rounding()
    document = rounding.walk(document, rounding.RESULT_SIGNIFICANT_DIGITS,
                             rounding.DEPARTURE_DIGITS_BY_KEY, 0.0)
    headed = with_emission_header(document, "honeycomb", regime=[])
    with open(DESTINATION, "w") as handle:
        json.dump(headed, handle, indent=1)
        handle.write("\n")
    print("written to gpd/results/T-302-si-staple-order.json")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
