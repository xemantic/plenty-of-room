#!/usr/bin/env python3
"""T-296 -- EuropePMC keyword sweep: does any published honeycomb DNA origami turn its
scaffold raster at the row ends with ZERO unpaired nucleotides?

Retained driver. Re-runnable. Writes `europepmc-queries.json` beside itself and logs every
query, its hit count and the top titles to stdout.

`CLAUDE.md`: EuropePMC's REST search returns HTTP 503 under rapid sequential querying, and the
unretried failure parses as a zero-hit result. Sleep ~8 s and retry.

The question this sweep exists to make falsifiable is a NEGATIVE existence result, so the query
strings are the artifact: one paper naming a honeycomb-lattice origami whose raster turns carry
no unpaired scaffold refutes it.
"""
import json
import os
import sys
import time
import urllib.parse
import urllib.request

HERE = os.path.dirname(os.path.abspath(__file__))
REST = "https://www.ebi.ac.uk/europepmc/webservices/rest/search"

QUERIES = [
    # family 1 -- the motif itself: a scaffold crossover at a helix END
    '"DNA origami" AND "scaffold crossover" AND "helix end"',
    '"DNA origami" AND "scaffold crossover" AND honeycomb',
    '"DNA origami" AND "scaffold crossover" AND "raster"',
    '"scaffold crossover" AND "unpaired" AND origami',
    # family 2 -- the unpaired loop, named
    '"unpaired scaffold" AND "DNA origami"',
    '"scaffold loop" AND "DNA origami" AND helix',
    '"unpaired loop" AND "DNA origami"',
    '"single-stranded loop" AND "DNA origami" AND "helix end"',
    # family 3 -- the loop's stated PURPOSE: blunt-end stacking / multimerization
    '"blunt-end stacking" AND "DNA origami"',
    '"blunt end" AND "DNA origami" AND aggregation',
    '"DNA origami" AND multimerization AND "unpaired"',
    '"poly-T" AND "DNA origami" AND passivation',
    '"DNA origami" AND "polyT" AND "helix end"',
    # family 4 -- honeycomb multilayer design rules
    '"honeycomb lattice" AND "DNA origami" AND "design rules"',
    'caDNAno AND honeycomb AND scaffold AND routing',
    '"honeycomb-pleated" AND origami',
    '"multilayer DNA origami" AND scaffold AND routing',
    # family 5 -- yield as a function of scaffold routing at helix ends
    '"DNA origami" AND "folding yield" AND "scaffold routing"',
    '"DNA origami" AND yield AND "scaffold loop"',
    '"DNA origami" AND "folding yield" AND honeycomb',
    # family 6 -- the seam alternative Ke et al. name
    '"DNA origami" AND seam AND "circular scaffold"',
    '"DNA origami" AND "scaffold crossover" AND seam AND multilayer',
    # family 7 -- design/automation tools that emit honeycomb scaffold paths
    'scadnano AND "DNA origami"',
    '"DNA origami" AND "scaffold path" AND automated AND design',
    '"DNA origami" AND "routing algorithm" AND scaffold AND lattice',
    # family 8 -- reverse engineering / surveys of real designs
    '"DNA origami" AND "reverse engineering" AND scaffold AND staple',
    '"DNA origami" AND database AND designs AND scaffold',
    # family 9 -- the specific built blocks
    '"Self-assembly of DNA into nanoscale three-dimensional shapes"',
    '"Rapid prototyping of 3D DNA-origami shapes"',
    '"twist density" AND "DNA origami" AND honeycomb',
]


def search(query, page_size=25, retries=4):
    params = urllib.parse.urlencode({
        "query": query,
        "format": "json",
        "pageSize": page_size,
        "resultType": "core",
    })
    url = f"{REST}?{params}"
    for attempt in range(retries):
        try:
            with urllib.request.urlopen(url, timeout=90) as response:
                return json.loads(response.read().decode("utf-8"))
        except Exception as error:  # noqa: BLE001 - a 503 is an HTML page here
            print(f"    attempt {attempt + 1} failed: {error}", file=sys.stderr)
            time.sleep(8.0)
    return {"error": "all retries failed", "url": url}


def main():
    out = {}
    for query in QUERIES:
        print(f"[query] {query}", flush=True)
        result = search(query)
        hits = result.get("resultList", {}).get("result", [])
        out[query] = {
            "hitCount": result.get("hitCount"),
            "records": [
                {
                    "id": r.get("id"),
                    "pmcid": r.get("pmcid"),
                    "doi": r.get("doi"),
                    "title": r.get("title"),
                    "journal": (r.get("journalInfo") or {}).get("journal", {}).get("title"),
                    "year": r.get("pubYear"),
                    "isOpenAccess": r.get("isOpenAccess"),
                    "abstract": r.get("abstractText"),
                }
                for r in hits
            ],
        }
        print(f"    hitCount={result.get('hitCount')}  returned={len(hits)}", flush=True)
        for r in hits[:5]:
            print(f"      - {r.get('pubYear')} {r.get('title')!r} pmcid={r.get('pmcid')}", flush=True)
        time.sleep(8.0)
    with open(os.path.join(HERE, "europepmc-queries.json"), "w") as handle:
        json.dump(out, handle, indent=1)
    print(f"\nwrote {os.path.join(HERE, 'europepmc-queries.json')}")


if __name__ == "__main__":
    main()
