#!/usr/bin/env python3
"""T-193 — targeted EuropePMC title/keyword queries the keyword sweep did not surface.

Retained driver. Writes `targeted-queries.json` beside itself.
"""
import json, os, sys, time, urllib.parse, urllib.request

HERE = os.path.dirname(os.path.abspath(__file__))
REST = "https://www.ebi.ac.uk/europepmc/webservices/rest/search"

QUERIES = [
    '"template-stripped" AND gold AND "(111)" AND texture',
    '"template stripped gold" AND roughness AND "111"',
    'chloride AND "specific adsorption" AND "Au(111)" AND "potential of zero charge"',
    '"potential of zero charge" AND chloride AND gold AND shift',
    '"Au(111)" AND "1 mM" AND capacitance AND "double layer"',
    'Koper AND "Au(111)" AND "double layer" AND capacitance',
    '"potential of zero charge" AND "magnesium" AND gold',
    'DNA AND origami AND "gold electrode" AND "reference electrode" AND bias',
    '"nanolever" AND DNA AND electrode AND "electric field"',
    '"switchable DNA" AND "gold electrode" AND "potential of zero charge"',
    '"open-circuit potential" AND "template-stripped gold"',
    '"work function" AND "template-stripped gold"',
]


def search(query, page_size=25, retries=4):
    params = urllib.parse.urlencode({
        "query": query, "format": "json", "pageSize": page_size, "resultType": "core"
    })
    url = f"{REST}?{params}"
    for attempt in range(retries):
        try:
            with urllib.request.urlopen(url, timeout=90) as response:
                return json.loads(response.read().decode("utf-8"))
        except Exception as error:  # noqa: BLE001
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
                {"id": r.get("id"), "pmcid": r.get("pmcid"), "doi": r.get("doi"),
                 "title": r.get("title"), "year": r.get("pubYear"),
                 "isOpenAccess": r.get("isOpenAccess"), "abstract": r.get("abstractText")}
                for r in hits
            ],
        }
        print(f"    hitCount={result.get('hitCount')} returned={len(hits)}")
        for r in hits[:6]:
            print(f"      - {r.get('pubYear')} {r.get('title')!r} pmcid={r.get('pmcid')}")
        time.sleep(8.0)
    with open(os.path.join(HERE, "targeted-queries.json"), "w") as handle:
        json.dump(out, handle, indent=1)


if __name__ == "__main__":
    main()
