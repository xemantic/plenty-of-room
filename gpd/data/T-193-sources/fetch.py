#!/usr/bin/env python3
"""T-193 — every fetch attempt, with its URL, HTTP status and byte count.

Retained driver. Re-runnable; writes `fetches.json` beside itself. A recorded
failure is as much a result as a success (`CLAUDE.md`: two 403s and two 404s are
recorded rather than silently dropped).
"""
import json, os, subprocess

HERE = os.path.dirname(os.path.abspath(__file__))

TARGETS = [
    # (output name, url, what it is)
    ("PMC11323936-fullTextXML.xml",
     "https://www.ebi.ac.uk/europepmc/webservices/rest/PMC11323936/fullTextXML",
     "Adnan, Behjati, Felez-Guerrero, Ojha & Koper, PCCP 26:21419 (2024) — the E_pzc measurement"),
    ("PMC13051447-fullTextXML.xml",
     "https://www.ebi.ac.uk/europepmc/webservices/rest/PMC13051447/fullTextXML",
     "Liu, Doblhoff-Dier & Koper, ACS Electrochem. 2:995 (2026) — the Au(111)/Au(110) literature statement"),
    ("PMC12371504-fullTextXML.xml",
     "https://www.ebi.ac.uk/europepmc/webservices/rest/PMC12371504/fullTextXML",
     "Avedian, Trang & Inkpen, ACS Nanosci. Au 5:269 (2025) — template-stripped gold is (111)-dominated"),
    ("PMC12276039-fullTextXML.xml",
     "https://www.ebi.ac.uk/europepmc/webservices/rest/PMC12276039/fullTextXML",
     "Schalenbach, Tempel & Eichel, ChemPhysChem 26:e202401088 (2025) — PZC-from-capacitance review"),
    ("PMC10751779-fullTextXML.xml",
     "https://www.ebi.ac.uk/europepmc/webservices/rest/PMC10751779/fullTextXML",
     "Tang, Zhao & Huang, JACS Au (2023) — solvent dependence of the Au(111) PZC (non-aqueous)"),
    ("PMC11613321-fullTextXML.xml",
     "https://www.ebi.ac.uk/europepmc/webservices/rest/PMC11613321/fullTextXML",
     "Doblhoff-Dier & Koper, Chem. Rev. (2024) — double-layer review, consulted, not load-bearing"),
    ("PMC11804923-fullTextXML.xml",
     "https://www.ebi.ac.uk/europepmc/webservices/rest/PMC11804923/fullTextXML",
     "Cation effects on CO2 reduction on single-crystal gold — consulted, not load-bearing"),
    # attempted and refused — recorded, per CLAUDE.md
    ("trasatti1986-pac.pdf",
     "https://publications.iupac.org/pac/pdf/1986/pdf/5807x0955.pdf",
     "Trasatti, Pure Appl. Chem. 58:955 (1986) — the absolute electrode potential; NOT OBTAINED"),
    ("trasatti1986-degruyter.pdf",
     "https://www.degruyterbrill.com/document/doi/10.1351/pac198658070955/pdf",
     "the same paper from the publisher; NOT OBTAINED"),
]


def main():
    log = []
    for name, url, what in TARGETS:
        path = os.path.join(HERE, name)
        result = subprocess.run(
            ["curl", "-sL", "-A", "Mozilla/5.0", "-w", "%{http_code} %{size_download}",
             "-o", path, url],
            capture_output=True, text=True
        )
        status, size = (result.stdout.split() + ["?", "?"])[:2]
        keep = status == "200" and int(size) > 2000
        if not keep and os.path.exists(path):
            os.remove(path)
        log.append({"file": name if keep else None, "url": url, "what": what,
                    "httpStatus": status, "bytes": size, "kept": keep})
        print(f"{status:>4} {size:>8}  {name}")
    with open(os.path.join(HERE, "fetches.json"), "w") as handle:
        json.dump(log, handle, indent=1)


if __name__ == "__main__":
    main()
