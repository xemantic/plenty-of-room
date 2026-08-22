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
# P-30 -- the gate's firing rate over the QUEUE'S OWN HISTORY, by kind.
#
#     tools/P-30-history.py [--limit N]
#
# `P-29`'s standard, and `CLAUDE.md`'s: a gate's false-positive rate is MEASURED over the corpus's
# own past, not argued -- because *a drift checker's false positives cost more than its true ones*
# is a RATE, and a gate that fires on correct work is switched off within an iteration.
#
# Every revision of `TASKS.md` is checked with TODAY's vocabulary and TODAY's predicate.  A firing
# is classified by KIND, because the kinds have different meanings: an UNDECLARED coinage or an
# UNSEEN row is a defect the gate exists to catch and is a TRUE positive wherever the file really
# carried it; a ROW disagreement would be a defect of the vocabulary itself.
import argparse
import os
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
GATE = os.path.join(ROOT, "tools", "check-queue-vocabulary.py")


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--limit", type=int, default=0, help="check only the newest N revisions")
    args = parser.parse_args(argv)

    revisions = subprocess.check_output(
        ["git", "log", "--format=%H", "--", "TASKS.md"], cwd=ROOT, text=True
    ).split()
    if args.limit:
        revisions = revisions[: args.limit]

    kinds = {"UNDECLARED": 0, "UNSEEN": 0, "ROW": 0, "DISAGREES": 0}
    firing = []
    for sha in revisions:
        blob = subprocess.check_output(["git", "show", "%s:TASKS.md" % sha], cwd=ROOT)
        handle, path = tempfile.mkstemp(prefix="P-30-history.", suffix=".md")
        try:
            os.write(handle, blob)
            os.close(handle)
            result = subprocess.run(
                [sys.executable, GATE, "--queue", path],
                cwd=ROOT, capture_output=True, text=True,
            )
        finally:
            os.unlink(path)
        lines = [
            line for line in result.stdout.splitlines()
            if line.split(" ")[0] in kinds
        ]
        if result.returncode:
            per = {}
            for line in lines:
                kind = line.split(" ")[0]
                kinds[kind] += 1
                per[kind] = per.get(kind, 0) + 1
            firing.append((sha[:7], per, [line.strip() for line in lines]))

    print("# %d revision(s) of TASKS.md; %d fire the gate" % (len(revisions), len(firing)))
    for sha, per, lines in firing:
        print("%s  %s" % (sha, ", ".join("%s x%d" % kv for kv in sorted(per.items()))))
        for line in lines:
            print("        %s" % line[:120])
    print("# by kind: %s" % ", ".join("%s %d" % kv for kv in sorted(kinds.items())))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
