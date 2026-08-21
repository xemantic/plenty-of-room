#!/usr/bin/env bash
# Relax, equilibrate and then produce N independent oxDNA replicas of the Gen-1
# tile. Each replica runs in its OWN directory: oxDNA writes to fixed paths, so
# two runs sharing a directory silently overwrite one another's trajectory.
set -euo pipefail

OXDNA=${OXDNA:-/Users/morisil/opt/oxDNA/build/bin/oxDNA}
MAKE_INPUT=${MAKE_INPUT:-$(cd "$(dirname "$0")" && pwd)/make_input.py}
RUN=${RUN:-$(pwd)}
REPLICAS=${REPLICAS:-6}
RELAX_A_STEPS=${RELAX_A_STEPS:-50000}
RELAX_STEPS=${RELAX_STEPS:-50000}
EQUIL_STEPS=${EQUIL_STEPS:-200000}
PROD_STEPS=${PROD_STEPS:-700000}
PROD_DT=${PROD_DT:-0.005}

cd "$RUN"

echo "[$(date +%H:%M:%S)] stage 1a: energy minimisation -- pull in the over-stretched crossovers"
# An idealised lattice puts every crossover backbone bond far beyond what the
# FENE can reach (2.4 oxDNA units against ~0.8 here), because the two helices'
# backbones do not face each other. Relaxing that against the real potential
# melts the short staple domains before the geometry settles. So the geometry is
# fixed FIRST by a temperature-free steepest descent with the backbone replaced
# by a harmonic spring: no thermal energy, so nothing can dissociate while the
# crossovers are being pulled in.
python "$MAKE_INPUT" --out relax_a.in --conf start.dat --lastconf relax_a.dat \
  --traj relax_a_traj.dat --energy relax_a_energy.dat --steps "$RELAX_A_STEPS" \
  --dt 0.001 --print-conf 1000000 --print-energy 5000 --seed 1 \
  --sim-type min --interaction DNA_relax --extra relax_type=harmonic_force \
  --extra relax_strength=10.0 --extra minimization_max_step=0.05
"$OXDNA" relax_a.in > relax_a.log 2>&1
echo "[$(date +%H:%M:%S)] stage 1a done, U/nt = $(tail -1 relax_a_energy.dat | awk '{print $2}')"

echo "[$(date +%H:%M:%S)] stage 1b: capped-force MD on the real potential"
python "$MAKE_INPUT" --out relax.in --conf relax_a.dat --lastconf relaxed.dat \
  --traj relax_traj.dat --energy relax_energy.dat --steps "$RELAX_STEPS" \
  --dt 0.002 --max-backbone-force 5 --print-conf 25000 --print-energy 2000 --seed 1
"$OXDNA" relax.in > relax.log 2>&1
echo "[$(date +%H:%M:%S)] stage 1b done, U/nt = $(tail -1 relax_energy.dat | awk '{print $2}')"

echo "[$(date +%H:%M:%S)] stage 2: equilibration ($EQUIL_STEPS steps, dt=$PROD_DT)"
python "$MAKE_INPUT" --out equil.in --conf relaxed.dat --lastconf equilibrated.dat \
  --traj equil_traj.dat --energy equil_energy.dat --steps "$EQUIL_STEPS" \
  --dt "$PROD_DT" --print-conf 25000 --print-energy 2000 --seed 2
"$OXDNA" equil.in > equil.log 2>&1
echo "[$(date +%H:%M:%S)] equilibration done, U/nt = $(tail -1 equil_energy.dat | awk '{print $2}')"

echo "[$(date +%H:%M:%S)] stage 3: $REPLICAS production replicas ($PROD_STEPS steps each)"
pids=()
for r in $(seq 1 "$REPLICAS"); do
  d="prod$r"; mkdir -p "$d"
  cp gen1_tile.top "$d/"; cp equilibrated.dat "$d/"
  ( cd "$d" && python "$MAKE_INPUT" --out prod.in --conf equilibrated.dat \
      --lastconf last.dat --traj traj.dat --energy energy.dat \
      --steps "$PROD_STEPS" --dt "$PROD_DT" --print-conf 5000 \
      --print-energy 5000 --seed $((1000 + r)) --refresh 1 > /dev/null \
    && "$OXDNA" prod.in > prod.log 2>&1 ) &
  pids+=($!)
  echo "  replica $r -> pid ${pids[-1]}"
done
printf '%s\n' "${pids[@]}" > production.pids
wait
echo "[$(date +%H:%M:%S)] all replicas finished"
