#!/usr/bin/env python3
"""Fetch and log the literature sources for T-161 — 'can a crossover be drawn at the
LAST base pair of a duplex?'

Retained in the repository per SESSION-PROMPT.md ("everything built on behalf of this
project stays in this project"), so the search is reproducible and the negative
existence result is falsifiable by one paper.

EuropePMC REST searches are spaced 8 s apart (CLAUDE.md: the endpoint returns HTTP 503
under rapid sequential querying, and the unretried failure parses as a zero-hit result).

Usage:  python3 tools/T-161-fetch-sources.py [outdir]
"""

import json
import os
import re
import sys
import time
import urllib.error
import urllib.parse
import urllib.request

OUT = sys.argv[1] if len(sys.argv) > 1 else "gpd/data/T-161-sources"
UA = ("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) "
      "Chrome/124.0 Safari/537.36")

SEARCHES = [
    '"DNA origami" AND "crossover" AND "helix end"',
    '"DNA origami" AND "edge staple"',
    '"DNA origami" AND "terminal base pair"',
    '"scaffold crossover" AND "raster"',
    '"DNA origami" AND "boustrophedon"',
    'caDNAno AND crossover AND "design rules"',
    'scadnano',
    '"DNA origami" AND "crossover" AND "last base"',
    'TITLE:"Multilayer DNA origami packed on a square lattice"',
    '"DNA origami" AND "edge" AND "strain" AND "crossover"',
    '"DNA origami" AND "crossover" AND "helix terminus"',
    '"DNA origami" AND "unpaired" AND "scaffold" AND "edge"',
]

FETCHES = [
    # (intended source, url, filename)
    ("Ke et al. 2009, JACS 131:15903, 'Multilayer DNA origami packed on a square lattice' "
     "— PMCID PMC2821935, recovered by TITLE search (a GUESSED PMCID, PMC2783486, returned an "
     "unrelated article; CLAUDE.md: never guess an identifier)",
     "https://www.ebi.ac.uk/europepmc/webservices/rest/PMC2821935/fullTextXML",
     "PMC2821935-Ke2009-fullTextXML.xml"),
    ("Ke et al. 2009 — PMC article page (new host), backup",
     "https://pmc.ncbi.nlm.nih.gov/articles/PMC2821935/",
     "PMC2821935-Ke2009-articlepage.html"),
    ("scadnano design-language documentation — Design constraints / crossovers",
     "https://scadnano.readthedocs.io/en/latest/",
     "scadnano-readthedocs-index.html"),
    ("scadnano Python scripting API — Design/Strand/crossover semantics",
     "https://scadnano-python-package.readthedocs.io/en/latest/",
     "scadnano-python-package-readthedocs.html"),
    ("cadnano2 user documentation (cadnano.org)",
     "https://cadnano.org/docs.html",
     "cadnano-org-docs.html"),
]


def get(url, timeout=60):
    req = urllib.request.Request(url, headers={"User-Agent": UA})
    try:
        with urllib.request.urlopen(req, timeout=timeout) as r:
            return r.status, r.read()
    except urllib.error.HTTPError as e:
        return e.code, e.read()
    except Exception as e:                                   # noqa: BLE001
        return -1, str(e).encode()


def main():
    os.makedirs(OUT, exist_ok=True)
    skip = os.environ.get("T161_SKIP_SEARCH") == "1"
    log = ["# EuropePMC queries run for T-161", "",
           "Endpoint: `https://www.ebi.ac.uk/europepmc/webservices/rest/search"
           "?query=<q>&format=json&resultType=core&pageSize=25`", ""]
    for q in ([] if skip else SEARCHES):
        url = ("https://www.ebi.ac.uk/europepmc/webservices/rest/search?query="
               + urllib.parse.quote(q) + "&format=json&resultType=core&pageSize=25")
        status, body = get(url)
        name = "search-" + re.sub(r"[^A-Za-z0-9]+", "_", q) + ".json"
        with open(os.path.join(OUT, name), "wb") as f:
            f.write(body)
        log.append("## `%s`" % q)
        log.append("")
        log.append("- HTTP status: %s" % status)
        try:
            d = json.loads(body)
            hits = d.get("hitCount", "?")
            log.append("- hitCount: %s" % hits)
            log.append("- top 5 results:")
            log.append("")
            for r in d.get("resultList", {}).get("result", [])[:5]:
                log.append("  - PMID %s | PMCID %s | OA=%s | %s (%s) — %s" % (
                    r.get("pmid"), r.get("pmcid"), r.get("isOpenAccess"),
                    r.get("journalTitle") or None, r.get("pubYear"), r.get("title")))
        except Exception as e:                               # noqa: BLE001
            log.append("- NOT JSON: %s" % e)
        log.append("")
        log.append("- raw JSON saved: `%s`" % name)
        log.append("")
        print("%-70s %s %s bytes" % (q[:70], status, len(body)))
        time.sleep(8)

    if not skip:
        with open(os.path.join(OUT, "queries.md"), "w") as f:
            f.write("\n".join(log) + "\n")

    rows = ["| Intended source | URL attempted | HTTP | Saved path | Bytes |",
            "|---|---|---|---|---|"]
    for intended, url, name in FETCHES:
        status, body = get(url)
        with open(os.path.join(OUT, name), "wb") as f:
            f.write(body)
        rows.append("| %s | `%s` | %s | `%s` | %d |" % (intended, url, status, name, len(body)))
        print("%-70s %s %s bytes" % (name, status, len(body)))
        time.sleep(3)
    with open(os.path.join(OUT, "fetches.md"), "w") as f:
        f.write("\n".join(rows) + "\n")


if __name__ == "__main__":
    main()
