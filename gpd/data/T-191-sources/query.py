#!/usr/bin/env python3
"""T-191 -- EuropePMC keyword sweep: measured bending rigidity of multilayer DNA origami.

Retained driver, re-runnable. Writes `europepmc-queries.json` beside itself and logs
every query, its hit count and the top titles to stdout.

CLAUDE.md: EuropePMC's REST search returns HTTP 503 under rapid sequential querying,
and the unretried failure parses as a zero-hit result. Sleep ~8 s and retry.
"""
import json, os, sys, time, urllib.parse, urllib.request

HERE = os.path.dirname(os.path.abspath(__file__))
REST = "https://www.ebi.ac.uk/europepmc/webservices/rest/search"

QUERIES = [
    # family 1 -- the primary measurement
    '"DNA origami" AND "persistence length" AND "helix bundle"',
    '"six-helix bundle" AND "persistence length"',
    '"DNA origami" AND "magnetic tweezers" AND rigidity',
    '"DNA origami" AND "bending rigidity" AND measured',
    '"DNA origami" AND "torsional rigidity"',
    # family 2 -- other bundle measurements
    '"DNA nanotube" AND "persistence length" AND "helix"',
    '"DNA origami" AND "flexural rigidity"',
    '"DNA origami" AND "Young\'s modulus"',
    '"DNA origami" AND "elastic modulus" AND bundle',
    # family 3 -- interlayer shear / composite action
    '"DNA origami" AND "shear" AND crossover AND stiffness',
    '"DNA origami" AND "shear modulus"',
    '"DNA origami" AND "crossover density" AND flexibility',
    '"DNA nanostructure" AND "shear lag"',
    '"helix bundle" AND "crossover" AND "coupling" AND mechanical',
    # family 4 -- plates / slabs
    '"DNA origami" AND "plate" AND "bending stiffness"',
    '"multilayer DNA origami" AND mechanical',
    '"DNA origami" AND "layer" AND "flexural"',
    '"DNA origami" AND cantilever AND stiffness',
    # family 5 -- modelling frameworks
    'CanDo AND "DNA origami" AND finite element',
    '"DNA origami" AND "coarse-grained" AND "elastic moduli"',
    '"oxDNA" AND "persistence length" AND origami',
]


def search(query, page_size=25, retries=4):
    params = urllib.parse.urlencode({
        "query": query, "format": "json",
        "pageSize": page_size, "resultType": "core",
    })
    url = f"{REST}?{params}"
    for attempt in range(retries):
        try:
            with urllib.request.urlopen(url, timeout=90) as r:
                body = r.read()
            return json.loads(body)
        except Exception as exc:  # 503 under rapid sequential querying
            print(f"  attempt {attempt+1} failed: {exc}", file=sys.stderr)
            time.sleep(10 * (attempt + 1))
    return None


def main():
    out = {}
    for q in QUERIES:
        print(f"QUERY {q}")
        d = search(q)
        if d is None:
            print("  FAILED after retries")
            out[q] = {"error": "failed after retries"}
            time.sleep(8)
            continue
        hits = d.get("hitCount", 0)
        results = d.get("resultList", {}).get("result", [])
        print(f"  hits={hits}")
        for r in results[:8]:
            print(f"    {r.get('pmcid')} {r.get('pmid')} OA={r.get('isOpenAccess')} :: {r.get('title')}")
        out[q] = {"hitCount": hits, "result": results}
        time.sleep(8)
    with open(os.path.join(HERE, "europepmc-queries.json"), "w") as f:
        json.dump(out, f, indent=1)
    print("wrote europepmc-queries.json")


if __name__ == "__main__":
    main()
