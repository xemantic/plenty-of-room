#!/usr/bin/env python3
"""
Put the oxDNA measurements beside this repository's own closed forms.

The three plate rigidities of `T-10`'s `rigidityRecovery` are the comparison;
`k_theta` is the quantity underneath them that no measurement in this corpus
has ever fixed, and `T-9` has carried as its largest open premise.
"""

import argparse
import json

BOLTZMANN_PNNM = 4.141947


def quadratic_mode_rms(dx, dy, dk, lx, ly):
    """RMS out-of-plane displacement of a FREE plate, summed over the three
    constant-curvature modes only -- the same subspace the oxDNA fit uses, so
    the two sides are the same functional of the same modes."""
    area = lx * ly
    k11 = dx * 144.0 * area / lx ** 4
    k22 = dy * 144.0 * area / ly ** 4
    k33 = dk * 64.0 * area / (lx ** 2 * ly ** 2)
    # <P2^2> = 1/5 on [-1,1]; <(xi*eta)^2> = 1/9
    var = (BOLTZMANN_PNNM / k11) / 5.0 + (BOLTZMANN_PNNM / k22) / 5.0 \
        + (BOLTZMANN_PNNM / k33) / 9.0
    return var ** 0.5


def row(name, corpus, oxdna, unit, note=''):
    if corpus in (None, 0) or oxdna is None:
        ratio = None
    else:
        ratio = oxdna / corpus
    return {'quantity': name, 'corpus': corpus, 'oxdna': oxdna,
            'oxdnaOverCorpus': ratio, 'unit': unit, 'note': note}


def main():
    p = argparse.ArgumentParser()
    p.add_argument('--t10', default='gpd/results/T-10-discrete-lattice-tile.json')
    p.add_argument('--tile', required=True)
    p.add_argument('--roll', default=None)
    p.add_argument('--duplex', default=None)
    p.add_argument('--out', default=None)
    a = p.parse_args()

    t10 = json.load(open(a.t10))
    closed = {r['quantity']: r['closedForm'] for r in t10['rigidityRecovery']}
    d_par = closed['D_parallel = EI/d']
    d_perp = closed['D_perpendicular = k_theta d/p']
    d_k = closed['D_k = GJ/(4d)']
    geom = t10['geometry']
    d, pitch = geom['interhelicalDistance'], geom['crossoverSpacing']

    tile = json.load(open(a.tile))
    r = tile['rigidities']
    lx = tile['lengthAlongHelicesNm']
    ly = tile['lengthAcrossHelicesNm']

    k_theta_corpus = d_perp * pitch / d
    k_theta_oxdna = r['D_perpendicular'] * pitch / d

    rows = [
        row('D_parallel (along the helices)', d_par, r['D_parallel'], 'pN*nm',
            'corpus: EI/d with CanDo EI = 230 pN*nm^2'),
        row('D_perpendicular (across the helices)', d_perp,
            r['D_perpendicular'], 'pN*nm',
            'corpus: k_theta d/p, k_theta FITTED, never measured'),
        row('D_k (twist)', d_k, r['D_k'], 'pN*nm',
            'corpus: GJ/(4d) with CanDo GJ = 460 pN*nm^2'),
        row('anisotropy D_parallel / D_perpendicular', d_par / d_perp,
            r['D_parallel'] / r['D_perpendicular'], '-',
            'drives every placement result in the corpus'),
        row('crossover hinge k_theta', k_theta_corpus, k_theta_oxdna,
            'pN*nm/rad', 'corpus: 2 alpha B / (100 a) -- the 1/100 is borrowed '
                         'from CanDo nick softening (TASKS.md, T-9)'),
        row('free-tile out-of-plane RMS, quadratic modes',
            quadratic_mode_rms(d_par, d_perp, d_k, lx, ly),
            quadratic_mode_rms(r['D_parallel'], r['D_perpendicular'],
                               r['D_k'], lx, ly), 'nm',
            'derived on BOTH sides from the same three modes; not T-10 thermal '
            'RMS, which sits on the PEG foundation and is a different quantity'),
    ]

    lattice = tile.get('lattice', {})
    if lattice:
        rows.append(row('interhelical distance', d,
                        lattice.get('interhelicalMeanNm'), 'nm',
                        'corpus: Fischer et al. 2016 SAXS lattice constant; '
                        'oxDNA: its own relaxed mean'))

    if a.duplex:
        dup = json.load(open(a.duplex))
        sweep = dup.get('persistenceLengthByWindowNm', {})
        rows.append(row('duplex EI (nicked, as built)', 230.0,
                        dup['bendingRigidityPnNm2'], 'pN*nm^2',
                        f"corpus: CanDo continuous duplex (L_p 55.5 nm); oxDNA: "
                        f"the nicked duplex the tile is made of, fitted over "
                        f"{dup['fitLimitNm']:.0f} nm. NOT converged in the fit "
                        f"window -- L_p by window {sweep}: an equilibrated rod "
                        f"gives one value at every window, so only the short-"
                        f"window end is usable and the rest is a lower bound "
                        f"on the trajectory length still needed."))

    if a.roll:
        roll = json.load(open(a.roll))
        rows.append(row('k_theta from interduplex roll', k_theta_corpus,
                        roll['hingeStiffnessFromCrossoverRoll'], 'pN*nm/rad',
                        'independent of the plate reduction; lower bound, '
                        'duplex torsion sits in parallel'))
        rows.append(row('interduplex roll sd at a crossover', None,
                        roll['rollSdAtCrossoverDeg'], 'deg',
                        'Snodin et al. NAR 47:1585 report ~+-16 deg per '
                        'junction on a twist-corrected 2D tile'))

    result = {
        'comparison': rows,
        'oxdnaFrames': tile['frames'],
        'tileNm': [lx, ly],
        'basePairsIntact': lattice.get('basePairsIntact'),
        'meanShape': tile.get('meanShape'),
    }
    text = json.dumps(result, indent=1)
    if a.out:
        open(a.out, 'w').write(text)

    conv = tile.get('convergence', {})
    if conv:
        print('convergence (a rigidity is only as good as this):')
        taus = conv.get('autocorrelationTimeFrames', [])
        eff = conv.get('effectiveSamples', [])
        names = ['bend along', 'bend across', 'twist']
        for i, n in enumerate(names):
            if i < len(taus):
                print(f'  {n:12s} autocorrelation {taus[i]:6.2f} frames, '
                      f'~{eff[i]:7.1f} independent samples')
        first, second = conv.get('firstHalf', {}), conv.get('secondHalf', {})
        for k in ('D_parallel', 'D_perpendicular', 'D_k'):
            if k in first:
                print(f'  {k:16s} first half {first[k]:9.3f}  '
                      f'second half {second[k]:9.3f}  '
                      f'ratio {second[k] / first[k]:.3f}')
        per = conv.get('perReplica', [])
        if len(per) > 1:
            for k in ('D_parallel', 'D_perpendicular', 'D_k'):
                vals = [p[k] for p in per]
                print(f'  {k:16s} per replica {min(vals):8.3f} .. {max(vals):8.3f} '
                      f'(spread {max(vals) / min(vals):.2f}x over {len(per)} runs)')
        broken = tile.get('brokenPairFraction')
        if broken is not None:
            print(f'  base pairs intact: {(1 - broken) * 100:.1f} % '
                  f'(the 14 lone 8-mers carry no crossover)')
        print()

    w = max(len(x['quantity']) for x in rows)
    print(f"{'quantity'.ljust(w)}  {'corpus':>14}  {'oxDNA':>14}  {'ratio':>7}  unit")
    print('-' * (w + 48))
    for x in rows:
        c = '--' if x['corpus'] is None else f"{x['corpus']:.4g}"
        o = '--' if x['oxdna'] is None else f"{x['oxdna']:.4g}"
        rr = '--' if x['oxdnaOverCorpus'] is None else f"{x['oxdnaOverCorpus']:.3f}"
        print(f"{x['quantity'].ljust(w)}  {c:>14}  {o:>14}  {rr:>7}  {x['unit']}")


if __name__ == '__main__':
    main()
