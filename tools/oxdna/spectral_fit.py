#!/usr/bin/env python3
"""
Plate rigidities from the FULL Legendre bending spectrum, by maximum likelihood.

WHY THIS EXISTS.  The three constant-curvature modes are the tile's SOFTEST, so
they carry most of the fluctuation and also the longest correlation times: on a
CPU-length trajectory they give a handful of independent samples and a rigidity
that cannot be read. Higher bending modes are stiffer, decorrelate faster, and
depend on the SAME three rigidities, so they are the well-sampled part of the
same measurement.

The energy of an orthotropic Kirchhoff plate is linear in the rigidities,

    K(D) = D_x A_x + D_1 A_1 + D_y A_y + D_k A_k

with A matrices that are pure geometry, so the model covariance is
`Sigma(D) = kT K(D)^-1` and the four rigidities are fitted by minimising the
Gaussian negative log-likelihood against the sample covariance. That fits FOUR
parameters, not a full mode-by-mode covariance, so it stays stable where
inverting a 22 x 22 sample covariance from a few hundred frames would not.
"""

import numpy as np
from numpy.polynomial import legendre as L
from scipy.optimize import minimize

BOLTZMANN_PNNM = 4.141947


def _legendre_derivatives(degree, nodes):
    """P_m, P_m', P_m'' evaluated at `nodes`, for m = 0..degree."""
    val, d1, d2 = [], [], []
    for m in range(degree + 1):
        c = np.zeros(m + 1)
        c[m] = 1.0
        val.append(L.legval(nodes, c))
        d1.append(L.legval(nodes, L.legder(c, 1)) if m >= 1 else np.zeros_like(nodes))
        d2.append(L.legval(nodes, L.legder(c, 2)) if m >= 2 else np.zeros_like(nodes))
    return np.array(val), np.array(d1), np.array(d2)


def mode_list(degree):
    """Bending modes only: the three rigid-body terms are excluded."""
    return [(m, n) for m in range(degree + 1) for n in range(degree + 1)
            if (m, n) not in ((0, 0), (1, 0), (0, 1))]


def geometry_matrices(degree, lx, ly, quadrature=24):
    """A_x, A_1, A_y, A_k such that K = D_x A_x + D_1 A_1 + D_y A_y + D_k A_k."""
    nodes, weights = np.polynomial.legendre.leggauss(quadrature)
    val, d1, d2 = _legendre_derivatives(degree, nodes)
    modes = mode_list(degree)
    sx, sy = (2.0 / lx) ** 2, (2.0 / ly) ** 2
    sxy = (2.0 / lx) * (2.0 / ly)
    jac = (lx / 2.0) * (ly / 2.0)

    wxx, wyy, wxy = [], [], []
    for (m, n) in modes:
        wxx.append(np.outer(val[n], d2[m]) * sx)      # (eta, xi)
        wyy.append(np.outer(d2[n], val[m]) * sy)
        wxy.append(np.outer(d1[n], d1[m]) * sxy)
    wxx, wyy, wxy = np.array(wxx), np.array(wyy), np.array(wxy)
    w2 = np.outer(weights, weights) * jac

    def inner(a, b):
        return np.einsum('iab,jab,ab->ij', a, b, w2)

    a_x = inner(wxx, wxx)
    a_y = inner(wyy, wyy)
    a_1 = inner(wxx, wyy) + inner(wyy, wxx)
    a_k = 4.0 * inner(wxy, wxy)
    return a_x, a_1, a_y, a_k


def design_matrix(degree, xi, eta):
    """Basis evaluated at the grid points, including the rigid-body columns so
    they can be projected out."""
    modes = mode_list(degree)
    cols = []
    for (m, n) in modes:
        cm = np.zeros(m + 1); cm[m] = 1.0
        cn = np.zeros(n + 1); cn[n] = 1.0
        cols.append(L.legval(xi, cm) * L.legval(eta, cn))
    rigid = [np.ones_like(xi), xi, eta]
    return np.stack(rigid + cols, axis=1), len(rigid)


def fit(amps, lx, ly, degree, temperature_energy=BOLTZMANN_PNNM):
    """Maximum-likelihood rigidities from mode amplitudes (frames x modes)."""
    a_x, a_1, a_y, a_k = geometry_matrices(degree, lx, ly)
    sample = np.cov(amps.T)

    def nll(log_d):
        d_x, d_y, d_k = np.exp(log_d[:3])
        d_1 = log_d[3]                      # may be negative, fitted directly
        k = d_x * a_x + d_1 * a_1 + d_y * a_y + d_k * a_k
        try:
            chol = np.linalg.cholesky(k)
        except np.linalg.LinAlgError:
            return 1e12
        # Sigma = kT K^-1, so log det Sigma = n log kT - log det K
        logdet_k = 2.0 * np.sum(np.log(np.diag(chol)))
        n = k.shape[0]
        logdet_sigma = n * np.log(temperature_energy) - logdet_k
        trace = np.trace(k @ sample) / temperature_energy
        return logdet_sigma + trace

    start = np.array([np.log(80.0), np.log(10.0), np.log(40.0), 0.0])
    res = minimize(nll, start, method='Nelder-Mead',
                   options={'maxiter': 20000, 'xatol': 1e-8, 'fatol': 1e-10})
    d_x, d_y, d_k = np.exp(res.x[:3])
    return {
        'D_parallel': float(d_x),
        'D_perpendicular': float(d_y),
        'D_k': float(d_k),
        'D_coupling': float(res.x[3]),
        'modes': len(mode_list(degree)),
        'degree': degree,
        'converged': bool(res.success),
        'negLogLikelihood': float(res.fun),
    }
