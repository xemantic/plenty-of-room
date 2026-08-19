#!/usr/bin/env python3
"""Health of an oxDNA configuration: FENE-range violations, base-pair integrity
and interhelical distance. Run between relaxation stages so a structure that has
not relaxed is visible BEFORE the next stage refuses to load it.

The FENE acts between oxDNA's BACKBONE SITES, not between nucleotide centres of
mass. Measuring the centre-of-mass separation instead reports ~1000 spurious
"compressed" bonds on a structure that in fact has none, because the two differ
by the backbone offset (POS_MM_BACK1/2) and by the helical twist between
consecutive bases.
"""
import json
import sys

import numpy as np

from analyse_tile import read_configurations, pair_index, centreline

POS_MM_BACK1, POS_MM_BACK2 = -0.3400, 0.3408   # oxDNA2 grooved backbone site
FENE_R0, FENE_DELTA = 0.7564, 0.25             # FENE_R0_OXDNA2, FENE_DELTA


def backbone_sites(path):
    lines = open(path).read().splitlines()
    box = [float(v) for v in lines[1].split('=')[1].split()]
    d = np.array([[float(x) for x in l.split()[:9]]
                  for l in lines[3:] if len(l.split()) >= 9])
    r, a1, a3 = d[:, 0:3], d[:, 3:6], d[:, 6:9]
    a2 = np.cross(a3, a1)
    return r + a1 * POS_MM_BACK1 + a2 * POS_MM_BACK2, box


def main():
    conf, top_path, nuc_path = sys.argv[1], sys.argv[2], sys.argv[3]
    helices, row_bp = int(sys.argv[4]), int(sys.argv[5])
    nuc = json.load(open(nuc_path))
    fwd, rev = pair_index(nuc, helices, row_bp)
    rows = [l.split() for l in open(top_path).read().splitlines()[1:]]
    bonds = [(i, int(r[2])) for i, r in enumerate(rows) if int(r[2]) >= 0]

    bb, box = backbone_sites(conf)
    length = np.array([np.linalg.norm(bb[i] - bb[j]) for i, j in bonds])
    # a nucleotide that drifted outside the box is written wrapped, so a bond of
    # about one box length is a wrap, not a physical extension
    wrapped = length > 0.5 * box[0]
    real = length[~wrapped]
    hi = int((real > FENE_R0 + FENE_DELTA).sum())
    lo = int((real < FENE_R0 - FENE_DELTA).sum())

    t, _, pos = list(read_configurations(conf))[-1]
    grid, broken = centreline(pos, fwd, rev)
    across = np.linalg.norm(np.diff(grid, axis=0), axis=-1)

    print(f'{conf}: FENE violations {hi + lo:3d} (hi {hi:3d} lo {lo:3d})  '
          f'bond mean {real.mean():.3f} min {real.min():.3f} max {real.max():.3f}  '
          f'wrapped {int(wrapped.sum()):2d}  bp intact {(1 - broken) * 100:5.1f}%  '
          f'interhelical {across.mean():.2f} nm (min {across.min():.2f})')
    return 0


if __name__ == '__main__':
    sys.exit(main())
