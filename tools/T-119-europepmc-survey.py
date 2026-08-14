#!/usr/bin/env python3
#
# T-119, leaf A8.2 — the recorded EuropePMC survey behind C-0055's NOT FOUND verdict.
#
#     python3 tools/T-119-europepmc-survey.py > T-119-queries.log
#
# Retained per SESSION-PROMPT: everything built on behalf of this project stays in it, so that a
# negative existence claim is falsifiable by re-running the search rather than by recollection.
# Two batches, fifteen families, 62 queries, ~8 s apart with three retries — EuropePMC 503s under
# rapid sequential querying and the unretried failure parses like a zero-hit result (CLAUDE.md).
# The emitted JSON is folded into gpd/results/T-119-literature-queries.json.
#
FAMILIES = {
 "F1 square-lattice crossover register": [
  '"square lattice" AND "DNA origami" AND "crossover" AND "10.67"',
  '"DNA origami" AND "33.75" AND twist',
  '"crossover planes" AND "DNA origami"',
  '"8 bp" AND "crossover" AND "square lattice" AND DNA',
  '"nearest neighbors" AND "antiparallel strand crossovers" AND origami',
 ],
 "F2 out-of-plane duplex on a single-layer sheet": [
  '"single-layer" AND "DNA origami" AND "out-of-plane" AND helix',
  '"DNA origami" AND "perpendicular helix" AND sheet',
  '"DNA origami" AND "protruding" AND duplex AND single-layer',
  '"second layer" AND "DNA origami" AND "single-layer" AND crossover',
  '"DNA origami" AND "vertical helix" AND plate',
  '"DNA origami" AND "helix normal to" AND plane',
 ],
 "F3 crossover as a hinge or pivot": [
  '"DNA origami" AND hinge AND crossover AND "degrees of freedom"',
  '"DNA origami" AND "hinge" AND "single crossover"',
  '"Holliday junction" AND hinge AND "DNA origami" AND flexure',
  '"DNA origami" AND "pivot" AND crossover AND rotation',
  '"DNA origami mechanisms" AND hinge',
  '"compliant" AND "DNA origami" AND joint AND stiffness',
 ],
 "F4 unused or omitted crossover positions": [
  '"omitting crossovers" AND "DNA origami"',
  '"crossover density" AND "DNA origami" AND design AND omitted',
  '"unused" AND "crossover" AND "DNA origami" AND lattice',
  '"additional crossover" AND "DNA origami" AND "same helices"',
  '"crossover spacing" AND "DNA origami" AND "16 bp"',
 ],
 "F5 mixed single- and multi-layer origami": [
  '"DNA origami" AND "two-layer" AND "single-layer" AND hybrid',
  '"DNA origami" AND "raised" AND feature AND helix AND layer',
  '"bilayer" AND "DNA origami" AND sheet AND crossover',
 ],
 "F6 cantilever / lever arm on an origami plate": [
  '"DNA origami" AND "cantilever" AND arm AND lever',
  '"DNA origami" AND "lever arm" AND rotation AND nanoscale',
  '"DNA origami" AND "flexure" AND hinge',
 ],
 "F7 attachment by a single covalent link": [
  '"DNA origami" AND "single covalent" AND attach AND helix',
  '"one covalent bond" AND "DNA origami"',
  '"DNA origami" AND "pin" AND "unpaired" AND attachment AND rigid',
 ],
 "F8 twist from lattice underwinding": [
  '"global twist" AND "DNA origami" AND underwinding',
  '"DNA origami" AND "base pair deletions" AND twist correction',
  '"Folding DNA into twisted and curved nanoscale shapes"',
 ],
 "F9 staple break length and yield": [
  '"staple" AND "8-nt" AND "DNA origami" AND yield AND domain',
  '"binding domain" AND length AND "DNA origami" AND "folding yield"',
  '"DNA origami" AND "staple breaks" AND destabilizing',
 ],
 "F10 unoccupied azimuth on a single-layer sheet": [
  '"DNA origami" AND "unoccupied" AND crossover AND position',
  '"DNA origami" AND "free azimuth"',
  '"DNA origami" AND "crossover azimuth"',
  '"DNA origami" AND "backbone azimuth" AND crossover',
  '"single-layer DNA origami" AND protrusion',
  '"single layer" AND "DNA origami" AND pillar AND helix',
 ],
 "F11 interlayer crossover to an added helix": [
  '"interlayer crossover" AND DNA',
  '"layer-to-layer" AND crossover AND "DNA origami"',
  '"DNA origami" AND "additional helix" AND crossover AND attach',
  '"DNA origami" AND "stacked helix" AND crossover',
  '"DNA origami" AND "helix added" AND "second layer"',
 ],
 "F12 a crossover used as a pivot": [
  '"crossover" AND "pivot" AND "DNA nanostructure" AND hinge',
  '"junction hinge" AND DNA AND origami',
  '"DNA origami" AND "hinge joint" AND "crossover"',
  '"DNA origami" AND "torsional spring" AND crossover',
  '"DNA origami" AND "dihedral" AND crossover AND stiffness',
 ],
 "F13 free lever on an origami plate": [
  '"DNA origami" AND "free lever" ',
  '"DNA origami" AND "cantilevered helix"',
  '"DNA origami" AND "protruding helix" AND crossover',
  '"DNA origami" AND "single crossover" AND attachment AND rigid',
 ],
 "F14 the Rothemund protruding-marker precedent": [
  '"Folding DNA to create nanoscale shapes and patterns"',
  '"DNA origami" AND "hairpin" AND marker AND protruding AND rectangle',
 ],
 "F15 mechanical frustration / nanoengine leads": [
  '"Realizing mechanical frustration at the nanoscale using DNA origami"',
  '"Mechanics of dynamic and deformable DNA nanostructures"',
  '"leaf-spring DNA-origami nanoengine"',
 ],
}
out=[]
for fam,qs in FAMILIES.items():
    for q in qs:
        url="https://www.ebi.ac.uk/europepmc/webservices/rest/search?query="+urllib.parse.quote(q)+"&format=json&pageSize=6"
        rec={"family":fam,"query":q}
        for attempt in range(3):
            try:
                with urllib.request.urlopen(url,timeout=60) as r:
                    d=json.loads(r.read().decode())
                rec["hitCount"]=d.get("hitCount")
                rec["top"]=[{"pmid":x.get("pmid"),"pmcid":x.get("pmcid"),"oa":x.get("isOpenAccess"),
                             "year":x.get("pubYear"),"title":x.get("title")} for x in d["resultList"]["result"]]
                break
            except Exception as e:
                rec["error"]=str(e); time.sleep(15)
        out.append(rec)
        print(fam,"|",q,"| hits=",rec.get("hitCount"),flush=True)
        for t in rec.get("top",[])[:6]:
            print("    ",t["year"],t["pmcid"],t["title"][:120],flush=True)
        time.sleep(8)
json.dump(out,open("T-119-europepmc-queries.json","w"),indent=1)
print("DONE",len(out))
