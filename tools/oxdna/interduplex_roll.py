#!/usr/bin/env python3
"""
Measure the interduplex ROLL between adjacent duplexes of an origami sheet, at
and between the crossover columns.

This is the coordinate the corpus's crossover hinge spring `k_theta` is written
on -- the dihedral of two adjacent duplexes about the interface line, which runs
along the helices -- and it is the coordinate Snodin et al. (NAR 47:1585) report
for a 2D origami tile as ~8-9 deg average with ~+-16 deg per junction.

Each duplex carries its own orientational reference: the base-pair vector from a
forward nucleotide to its Watson-Crick partner is perpendicular to the helix axis
and rotates with the helical twist. The relative roll of two adjacent duplexes is
the signed angle between those two vectors, projected onto the plane normal to
their common axis. Its MEAN over frames is the as-built register offset; its
VARIANCE gives a hinge stiffness by equipartition,

    k_theta <= k_B T / Var(delta_phi)

an upper bound only in the sense that the duplexes' own torsional compliance sits
in PARALLEL with the hinge and inflates the variance, so the inferred stiffness is
a LOWER bound on the hinge alone.
"""

import argparse
import json

import numpy as np

from analyse_tile import (read_configurations, pair_index,
                          assert_contiguous, centreline, BOLTZMANN_PNNM)


def signed_angle(v0, v1, axis):
    """Signed angle from v0 to v1 about `axis`, radians, arrays over (..., 3)."""
    v0 = v0 - axis * np.sum(v0 * axis, axis=-1, keepdims=True)
    v1 = v1 - axis * np.sum(v1 * axis, axis=-1, keepdims=True)
    v0 = v0 / np.linalg.norm(v0, axis=-1, keepdims=True)
    v1 = v1 / np.linalg.norm(v1, axis=-1, keepdims=True)
    cos = np.clip(np.sum(v0 * v1, axis=-1), -1.0, 1.0)
    sin = np.sum(np.cross(v0, v1) * axis, axis=-1)
    return np.arctan2(sin, cos)


def unwrap_mean_angle(a):
    """Circular mean over the frame axis (axis 0)."""
    return np.arctan2(np.sin(a).mean(0), np.cos(a).mean(0))


def rolls(positions, fwd, rev, helices, row_bp):
    """(frames, helices-1, row_bp-2) signed relative roll, radians."""
    bp = positions[:, fwd] - positions[:, rev]          # (F, h, bp, 3)
    bp = bp / np.linalg.norm(bp, axis=-1, keepdims=True)
    centre = 0.5 * (positions[:, fwd] + positions[:, rev])
    tangent = centre[:, :, 2:] - centre[:, :, :-2]      # central difference
    tangent = tangent / np.linalg.norm(tangent, axis=-1, keepdims=True)

    b = bp[:, :, 1:-1]
    axis = tangent[:, :-1] + tangent[:, 1:]
    axis = axis / np.linalg.norm(axis, axis=-1, keepdims=True)
    return signed_angle(b[:, :-1], b[:, 1:], axis)


def main():
    p = argparse.ArgumentParser()
    p.add_argument('--traj', required=True, nargs='+',
                   help='one or more replica trajectories, pooled')
    p.add_argument('--nucleotides', required=True)
    p.add_argument('--helices', type=int, default=15)
    p.add_argument('--row-bp', type=int, default=112)
    p.add_argument('--skip', type=int, default=0)
    p.add_argument('--columns', default='8,24,40,56,72,88,104')
    p.add_argument('--out', default=None)
    a = p.parse_args()

    nucleotides = json.load(open(a.nucleotides))
    fwd, rev = pair_index(nucleotides, a.helices, a.row_bp)

    frames = []
    for path in a.traj:
        for i, (t, box, pos) in enumerate(read_configurations(path)):
            if i < a.skip:
                continue
            assert_contiguous(centreline(pos, fwd, rev)[0], f'{path} frame {i}')
            frames.append(pos)
    positions = np.array(frames)
    if len(positions) < 20:
        raise SystemExit(f'only {len(positions)} frames; need >= 20')

    phi = rolls(positions, fwd, rev, a.helices, a.row_bp)   # offsets 1..row_bp-2
    columns = [int(c) - 1 for c in a.columns.split(',')
               if 1 <= int(c) <= a.row_bp - 2]

    mean = unwrap_mean_angle(phi)
    dev = np.arctan2(np.sin(phi - mean), np.cos(phi - mean))
    var = (dev ** 2).mean(0)

    # which interface actually carries a crossover at which column: interface b
    # takes the columns whose index has b's parity
    at_cross, off_cross = [], []
    for b in range(a.helices - 1):
        for k, c in enumerate(columns):
            (at_cross if k % 2 == b % 2 else off_cross).append(var[b, c])
    at_cross = np.array(at_cross)
    off_cross = np.array(off_cross)

    result = {
        'frames': int(positions.shape[0]),
        'interfaces': a.helices - 1,
        'crossoverSamples': int(at_cross.size),
        'rollSdAtCrossoverDeg': float(np.degrees(np.sqrt(at_cross.mean()))),
        'rollSdOffCrossoverDeg': float(np.degrees(np.sqrt(off_cross.mean()))),
        'rollSdAllDeg': float(np.degrees(np.sqrt(var.mean()))),
        'meanRollAtCrossoverDeg': float(np.degrees(np.abs(
            np.array([mean[b, c] for b in range(a.helices - 1)
                      for k, c in enumerate(columns) if k % 2 == b % 2])).mean())),
        'hingeStiffnessFromCrossoverRoll': float(
            BOLTZMANN_PNNM / at_cross.mean()),
        'hingeStiffnessUnits': 'pN*nm/rad^2 (lower bound: duplex torsion is in '
                               'parallel with the hinge and inflates the variance)',
    }
    text = json.dumps(result, indent=1)
    print(text)
    if a.out:
        open(a.out, 'w').write(text)


if __name__ == '__main__':
    main()
