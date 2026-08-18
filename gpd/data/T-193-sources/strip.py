#!/usr/bin/env python3
"""T-193 — render every fetched JATS XML as flat text for grepping.

Retained driver. Re-runnable; rewrites `PMC*.txt` from `PMC*-fullTextXML.xml`.
The `.txt` files carry no information the XML does not; they exist so that a
quotation can be checked with one `grep` rather than an XML parse, which is how
every verbatim sentence in `C-0111` was verified before it was written down.
"""
import glob
import os
import re

HERE = os.path.dirname(os.path.abspath(__file__))

for path in sorted(glob.glob(os.path.join(HERE, "PMC*-fullTextXML.xml"))):
    text = open(path, encoding="utf-8", errors="replace").read()
    text = re.sub(r"<[^>]+>", " ", text)
    text = re.sub(r"\s+", " ", text)
    out = path.replace("-fullTextXML.xml", ".txt")
    open(out, "w").write(text)
    print(f"{os.path.basename(out)}  {len(text)} chars")
