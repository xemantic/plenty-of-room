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
# T-267 -- emit a Rothemund origami rectangle with the REFERENCE implementation's own generator,
# so that this repository's mechanics can be graded on a design it did not parameterise.
#
#     python3 tools/scadnano/emit-reference-rectangle.py
#     python3 tools/scadnano/emit-reference-rectangle.py --check
#
# WHY THIS EXISTS. `gpd/designs/*.sc` are this corpus's OWN designs: one is `C-0151`'s recommended
# block and the other is the sheet `C-0157` simulated, round-tripped through this repository's
# writer. Grading either of them through the import path tests the plumbing and not the claim,
# because the corpus chose every integer in them. `scadnano.origami_rectangle.create` is the
# reference implementation's own canonical origami -- David Doty's code, not ours, following
# scadnano's conventions rather than this corpus's -- so what it draws is what the field draws.
#
# NOTHING IS EDITED. The emitted file is the generator's `to_json()` verbatim; `--check` asserts
# that the committed artifact is still exactly that, which is the same guarantee
# `CommittedDesignsTest` gives this repository's own designs.
#
# ENVIRONMENT. `python3 -m pip install --break-system-packages scadnano` (0.21.1 here).

import os
import sys

from scadnano import origami_rectangle as rect

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
OUTPUT = os.path.join(ROOT, 'gpd', 'designs', 'third-party',
                      'scadnano-origami-rectangle-16x8.sc')

# 16 helices x 8 columns: the shape `origami_rectangle`'s own docstring uses, and the smallest
# one that carries every feature this import has to face -- a seam, edge staples, flanking
# columns, and a scaffold whose offsets do not start at zero.
NUM_HELICES = 16
NUM_COLS = 8


def emit() -> str:
    design = rect.create(num_helices=NUM_HELICES, num_cols=NUM_COLS, assign_seq=False)
    return design.to_json()


def main() -> int:
    text = emit()
    if '--check' in sys.argv:
        if not os.path.exists(OUTPUT):
            print('MISSING: ' + OUTPUT)
            return 1
        with open(OUTPUT) as handle:
            committed = handle.read()
        if committed != text:
            print('STALE: ' + OUTPUT + ' is not what the reference generator emits today')
            return 1
        print('the committed reference rectangle is exactly what scadnano emits today')
        return 0
    os.makedirs(os.path.dirname(OUTPUT), exist_ok=True)
    with open(OUTPUT, 'w') as handle:
        handle.write(text)
    print('T-267 - wrote ' + OUTPUT)
    return 0


if __name__ == '__main__':
    sys.exit(main())
