#!/usr/bin/env python3
"""Pull every number C-0216 will quote straight out of the artifact."""
import json, sys
p = sys.argv[1] if len(sys.argv) > 1 else "gpd/results/T-323-the-placement-and-the-distribution-together.json"
d = json.load(open(p))

def show(title, rows):
    print("\n=== " + title + " ===")
    for r in rows: print(r)

print("emission:", d["emission"])
show("PARAMETERS", ["%-38s %s" % (k, v) for k, v in d["parameters"].items()])
show("FAMILY", ["cols %d  paths %d  stations %d  opts[%s]  size %d  exh %s  csPairs %d  admits %s"
                % (r["columns"], r["pathCount"], r["candidateStations"], r["rowOptionCounts"],
                   r["familySize"], r["exhaustivelyEnumerated"], r["centroSymmetricRowPairs"],
                   r["admitsCentroSymmetry"]) for r in d["family"]])
show("CHEAP BOUND", ["%s\n    -> %s  (%s %s)" % (r["question"][:100], r["answer"][:150], r["value"], r["units"]) for r in d["cheapBound"]])
show("PLACEMENT CENSUS", ["cols %d  %-32s best %s  median %s  worst %s  spread %s  detP90 %s  detRank %s  argmin %s"
                          % (r["columns"], r["distribution"][:32], r["bestScreeningP90"], r["medianScreeningP90"],
                             r["worstScreeningP90"], r["spread"], r["determinedScreeningP90"],
                             r["determinedRankFromBest"], r["bestPlacementLabel"][:20]) for r in d["placementCensus"]])
show("SCREENS", ["%-40s rho %-14s regret %-14s rank %-8s binding %s"
                 % (r["screen"][:40], r["spearmanAgainstSearched"], r["regretOfSelectingOnThisScreen"],
                    r["jointWinnerRankInThisScreen"], r["screenIsBinding"]) for r in d["screens"]])
show("CORNERS", ["f=%.2f c=%d %-32s p90 %-14s nom %-14s train %-14s R %-14s peak %-12s flat %-5s adm %-5s unc %-6s uncZD %-5s rm %-14s stillFlat %s"
                 % (r["compositeFraction"], r["columns"], r["corner"][:32], r["p90OverStroke"], r["nominalOverStroke"],
                    r["trainingP90"], r["ratio"], r["peakStiffness"], r["flatAtP90"], r["flatAndAdmissible"],
                    r["beatsUncoupledAtP90"], r["beatsUncoupledAtZeroDefects"], r["worstSinglePathRemoval"],
                    r["stillFlatAfterWorstRemoval"]) for r in d["corners"]])
show("SPLIT", ["\n".join("    %-38s %s" % (k, v) for k, v in r.items()) for r in d["split"]])
show("GRID", ["%-46s %-16s %s" % (r["placement"][:46], r["distribution"], r["p90OverStroke"]) for r in d["grid"]])
show("INTERACTION", ["\n".join("    %-38s %s" % (k, v) for k, v in r.items()) for r in d["interaction"]])
show("FRAGILITY", ["%-32s p90 %-14s nom %-14s rm %-14s ampl %-14s R %-14s R2 %-14s p90(2) %-14s flat2 %-5s in[3.5,20] %s"
                   % (r["corner"][:32], r["p90OverStroke"], r["nominalOverStroke"], r["worstSinglePathRemovalOverStroke"],
                      r["amplification"], r["ratio"], r["twoLevelRatio"], r["twoLevelP90OverStroke"],
                      r["twoLevelFlatAtP90"], r["twoLevelRatioInsideFlatWindow"]) for r in d["fragility"]])
show("PAIRED", ["%-46s ratioOfP90 %-14s medianRatio %-14s numeratorWins %d/%d"
                % (r["comparison"][:46], r["ratioOfPercentiles"], r["medianOfPerRealisationRatio"],
                   r["realisationsWhereTheNumeratorWins"], r["realisations"]) for r in d["paired"]])
show("CONVERGENCE", ["%-58s coarse %-14s fine %-14s dep %-10s moves %s"
                     % (r["axis"][:58], r["coarse"], r["fine"], r["departure"], r["verdictMoves"]) for r in d["convergence"]])
show("REPRODUCTIONS", ["%-62s pub %-14s here %-14s dep %s" % (r["statement"][:62], r["published"], r["here"], r["relativeDeparture"]) for r in d["reproductions"]])
show("FALSIFIERS", ["%-4s open %-5s FIRED %-5s  %s" % (r["id"], r["declaredOpen"], r["fired"], r["note"][:190]) for r in d["falsifiers"]])
show("DROPPED", ["%s\n    why: %s\n    measured: %s" % (r["what"][:90], r["why"][:150], r["measured"][:150]) for r in d["dropped"]])
show("VERDICT", ["%-62s %s" % (k[:62], v) for k, v in d["verdict"].items()])
show("FINDINGS", d["findings"])
