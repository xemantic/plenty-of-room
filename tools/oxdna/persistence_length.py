#!/usr/bin/env python3
"""
Bending persistence length of a duplex from an oxDNA trajectory, by
tangent-tangent correlation of the base-pair centreline:

    <t(0) . t(s)> = exp(-s / L_p)      ->      EI = L_p k_B T

Run on the NICKED duplex the tile is actually built from, this tests
`CLAUDE.md`'s "a single nick is a clamp" against the `EI = 230 pN nm^2` the
corpus takes from CanDo, and separates a duplex-constant disagreement from an
assembly one in the tile's `D_parallel`.
"""

import argparse
import json

import numpy as np

from analyse_tile import (read_configurations, pair_index,
                          assert_contiguous, centreline, BOLTZMANN_PNNM)


def main():
    p = argparse.ArgumentParser()
    p.add_argument('--traj', required=True, nargs='+',
                   help='one or more replica trajectories, pooled')
    p.add_argument('--nucleotides', required=True)
    p.add_argument('--row-bp', type=int, default=336)
    p.add_argument('--skip', type=int, default=0)
    p.add_argument('--smooth', type=int, default=4,
                   help='base pairs over which a local tangent is taken')
    p.add_argument('--trim', type=int, default=8,
                   help='base pairs discarded at each end (fraying)')
    p.add_argument('--fit-limit', type=float, default=20.0,
                   help='upper separation in nm for the L_p fit')
    p.add_argument('--out', default=None)
    a = p.parse_args()

    nucleotides = json.load(open(a.nucleotides))
    fwd, rev = pair_index(nucleotides, 1, a.row_bp)

    tangents, rises = [], []
    for path in a.traj:
      for i, (t, box, pos) in enumerate(read_configurations(path)):
        if i < a.skip:
            continue
        centre = assert_contiguous(
            centreline(pos, fwd, rev)[0], f'{path} frame {i}')[0][a.trim:a.row_bp - a.trim]
        rises.append(np.linalg.norm(np.diff(centre, axis=0), axis=-1).mean())
        d = centre[a.smooth:] - centre[:-a.smooth]
        tangents.append(d / np.linalg.norm(d, axis=-1, keepdims=True))
    tangents = np.array(tangents)
    if len(tangents) < 20:
        raise SystemExit(f'only {len(tangents)} frames; need >= 20')

    rise = float(np.mean(rises))
    n = tangents.shape[1]
    max_sep = min(n - 1, int(round(60.0 / rise)))       # out to ~60 nm
    seps, corr = [], []
    for s in range(1, max_sep):
        c = np.einsum('fix,fix->fi', tangents[:, :n - s], tangents[:, s:]).mean()
        seps.append(s * rise)
        corr.append(c)
    seps = np.array(seps)
    corr = np.array(corr)

    # Fit ln<t.t> = -s/Lp. The fit RANGE is a declared parameter, not a
    # convenience: a rod started straight equilibrates its short-wavelength
    # modes long before its long-wavelength ones, so a fit taken out to the full
    # contour reads far too stiff while the trajectory is still young. The
    # window sweep below is what shows whether that is happening -- an
    # equilibrated rod gives the same L_p at every window.
    def fit(limit_nm):
        keep = (seps <= limit_nm) & (corr > 0.05)
        if keep.sum() < 4:
            return None
        slope, intercept = np.polyfit(seps[keep], np.log(corr[keep]), 1)
        return None if slope >= 0 else -1.0 / slope

    windows = [10.0, 15.0, 20.0, 30.0, 45.0, 60.0]
    sweep = {f'{w:.0f}nm': fit(w) for w in windows}
    lp = fit(a.fit_limit)
    if lp is None:
        raise SystemExit('tangent correlation does not decay over the fit window')
    intercept = float(np.polyfit(seps[seps <= a.fit_limit],
                                 np.log(corr[seps <= a.fit_limit]), 1)[1])

    result = {
        'frames': int(tangents.shape[0]),
        'basePairs': a.row_bp,
        'risePerBasePairNm': rise,
        'fitLimitNm': a.fit_limit,
        'persistenceLengthByWindowNm': sweep,
        'interceptLog': float(intercept),
        'persistenceLengthNm': float(lp),
        'bendingRigidityPnNm2': float(lp * BOLTZMANN_PNNM),
        'separationsNm': seps.tolist(),
        'tangentCorrelation': corr.tolist(),
    }
    print(json.dumps({k: v for k, v in result.items()
                      if k not in ('separationsNm', 'tangentCorrelation')}, indent=1))
    if a.out:
        open(a.out, 'w').write(json.dumps(result, indent=1))


if __name__ == '__main__':
    main()
