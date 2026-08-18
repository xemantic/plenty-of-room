"""T-182 — literature search for a QUANTIFIED crossover prestrain / residual twist at a
DNA-origami edge. Extends C-0104's (T-172) ten EuropePMC queries onto the modalities it did
not search: oxDNA and all-atom MD of origami, cryo-EM of rectangles, and the global-twist
measurement literature that the register mismatch manifests in.

Retained driver. Run: python3 query.py
"""
import json, time, urllib.parse, urllib.request, os, sys

BASE = "https://www.ebi.ac.uk/europepmc/webservices/rest/search"
HERE = os.path.dirname(os.path.abspath(__file__))

QUERIES = [
    # --- oxDNA / coarse-grained, the modality C-0104 did not search
    'oxDNA AND "DNA origami" AND twist',
    '"coarse-grained" AND "DNA origami" AND "structural properties"',
    'oxDNA AND origami AND "square lattice"',
    # --- all-atom MD
    '"molecular dynamics" AND "DNA origami" AND "all-atom"',
    '"DNA origami" AND "in situ structure" AND "molecular dynamics"',
    'mrdna OR "multi-resolution" AND "DNA origami" AND simulation',
    # --- cryo-EM / structure of a rectangle edge
    '"cryo-electron microscopy" AND "DNA origami" AND lattice',
    '"DNA origami" AND "cryo-EM" AND "interhelical distance"',
    # --- global twist, which is what the register mismatch manifests as
    '"DNA origami" AND "global twist"',
    '"DNA nanostructure" AND twist AND "base pairs per turn" AND deletion',
    '"DNA origami" AND "twist correction"',
    '"twisted and curved" AND DNA AND nanoscale',
    # --- corrugation / tube formation, the observable of unbalanced strain
    '"DNA nanotube" AND corrugation AND lattice',
    '"DNA lattice" AND corrugation AND tube',
    '"DNA nanotube" AND circumference AND "helix"',
    # --- the edge / seam specifically
    '"DNA origami" AND edge AND "strain" AND relief',
    '"DNA origami" AND seam AND strain AND crossover',
    '"scaffold crossover" AND strain AND angle',
    # --- the crossover junction geometry itself
    '"Holliday junction" AND "interduplex angle" AND antiparallel',
    '"double crossover" AND "inter-helix angle" AND measurement',
]

def search(q):
    url = BASE + "?" + urllib.parse.urlencode(
        {"query": q, "format": "json", "pageSize": 15, "resultType": "core"})
    for _ in range(4):
        try:
            with urllib.request.urlopen(url, timeout=90) as r:
                return json.loads(r.read().decode())
        except Exception as e:
            err = {"error": str(e)}
            time.sleep(8)
    return err

def main():
    out = {}
    for q in QUERIES:
        data = search(q)
        hits = data.get("resultList", {}).get("result", [])
        out[q] = [{"id": h.get("id"), "title": h.get("title"), "year": h.get("pubYear"),
                   "journal": (h.get("journalInfo") or {}).get("journal", {}).get("title"),
                   "oa": h.get("isOpenAccess"), "pmcid": h.get("pmcid"), "doi": h.get("doi"),
                   "abstract": (h.get("abstractText") or "")[:2000]} for h in hits]
        print("%-62s %d hits" % (q[:62], len(hits)), flush=True)
        time.sleep(8)
    json.dump(out, open(os.path.join(HERE, "europepmc-queries.json"), "w"), indent=1)
    ids = {h["id"] for v in out.values() for h in v}
    print("unique records: %d over %d queries" % (len(ids), len(out)))

if __name__ == "__main__":
    main()
