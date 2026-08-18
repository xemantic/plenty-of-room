#!/usr/bin/env python3
"""T-193 — EuropePMC keyword sweep for the potential of zero charge of gold.

Retained driver. Re-runnable. Writes `europepmc-queries.json` beside itself and
logs every query, its hit count and the top titles to stdout.

`CLAUDE.md`: EuropePMC's REST search returns HTTP 503 under rapid sequential
querying, and the unretried failure parses as a zero-hit result. Sleep ~8 s and
retry.
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
    # family 1 — the quantity itself
    '"potential of zero charge" AND gold',
    '"potential of zero charge" AND "Au(111)"',
    '"potential of zero charge" AND "single crystal" AND electrode',
    '"pzc" AND "Au(111)"',
    # family 2 — the surface
    '"template-stripped gold"',
    '"template stripped" AND gold AND electrode',
    '"Au(111)" AND "flame annealed" AND electrochemistry',
    # family 3 — the electrolyte
    '"potential of zero charge" AND "MgCl2"',
    '"potential of zero charge" AND "perchlorate" AND gold',
    '"potential of zero charge" AND "divalent" AND electrode',
    # family 4 — the absolute scale
    '"absolute electrode potential" AND "hydrogen electrode"',
    'Trasatti AND "absolute electrode potential"',
    # family 5 — the device context: DNA on a biased gold electrode
    '"DNA origami" AND electrode AND potential AND actuation',
    '"DNA" AND "gold electrode" AND "potential of zero charge"',
    'Rant AND DNA AND "gold electrode" AND switching',
    '"electrically driven" AND "DNA origami" AND electrode',
    # family 6 — contact / open-circuit potential of gold in dilute electrolyte
    '"open circuit potential" AND gold AND "dilute" AND electrolyte',
    '"work function" AND "Au(111)" AND "electrochemical"',
    # family 7 — the capacitance minimum route to a PZC
    '"capacitance minimum" AND "potential of zero charge" AND gold',
    '"differential capacitance" AND "Au(111)" AND electrolyte',
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
        print(f"[query] {query}")
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
        print(f"    hitCount={result.get('hitCount')}  returned={len(hits)}")
        for r in hits[:5]:
            print(f"      - {r.get('pubYear')} {r.get('title')!r} pmcid={r.get('pmcid')}")
        time.sleep(8.0)
    with open(os.path.join(HERE, "europepmc-queries.json"), "w") as handle:
        json.dump(out, handle, indent=1)
    print(f"\nwrote {os.path.join(HERE, 'europepmc-queries.json')}")


if __name__ == "__main__":
    main()
