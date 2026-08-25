#!/usr/bin/env python3
#
# Copyright 2026 Kazimierz Pogoda / Xemantic
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# `T-337`'s `F2`, which is the largest risk in the row: is a re-emission ADDITIVE?
#
#     gpd/data/T-337-cheap-bound/additive-diff.py <committed.json> <re-emitted.json>
#
# Exits 1 on any moved pre-existing leaf, any added key that is not one of the three the task
# carries, any removed key, or any list whose length moved.  Three of the seven re-emitted
# studies are SEARCHES, and `CLAUDE.md` records that a descent lands on a manifold rather than
# on a point -- so *"only the new fields moved"* is a MEASUREMENT here and not an assurance.

"""Is a re-emitted file's change purely ADDITIVE? -- T-337's F2."""

import json, sys

NEW = {"exceedance", "exceedanceStandardError", "exceedanceOneSidedBound"}

def walk(a, b, path, moved, added, removed):
    if isinstance(a, dict) and isinstance(b, dict):
        for k in a:
            if k not in b:
                removed.append(path + "/" + k)
            else:
                walk(a[k], b[k], path + "/" + k, moved, added, removed)
        for k in b:
            if k not in a:
                added.append((path + "/" + k, k))
    elif isinstance(a, list) and isinstance(b, list):
        if len(a) != len(b):
            moved.append((path, "length %d -> %d" % (len(a), len(b))))
            return
        for i, (x, y) in enumerate(zip(a, b)):
            walk(x, y, path + "/%d" % i, moved, added, removed)
    else:
        if a != b:
            moved.append((path, "%r -> %r" % (a, b)))

old, new = sys.argv[1], sys.argv[2]
a = json.load(open(old)); b = json.load(open(new))
moved, added, removed = [], [], []
walk(a, b, "", moved, added, removed)
unexpected = [p for p, k in added if k not in NEW]
print("%-58s moved=%-5d added=%-5d (unexpected %d)  removed=%d"
      % (old.split("/")[-1], len(moved), len(added), len(unexpected), len(removed)))
for p, why in moved[:8]:
    print("   MOVED   %s  %s" % (p, why))
for p in unexpected[:8]:
    print("   ADDED?  %s" % p)
for p in removed[:8]:
    print("   REMOVED %s" % p)
sys.exit(1 if (moved or unexpected or removed) else 0)
