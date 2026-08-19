#!/usr/bin/env python3
"""Emit an oxDNA input file. Every knob this study varies is an argument, so the
run is reproducible from the command line that produced it."""
import argparse

TEMPLATE = """backend = CPU
backend_precision = double
sim_type = {sim_type}
interaction_type = {interaction}
salt_concentration = {salt}
T = {temperature}

steps = {steps}
dt = {dt}
thermostat = john
newtonian_steps = 103
diff_coeff = 2.5
verlet_skin = 0.5
seed = {seed}

topology = {topology}
conf_file = {conf}
lastconf_file = {lastconf}
trajectory_file = {traj}
energy_file = {energy}
print_conf_interval = {print_conf}
print_energy_every = {print_energy}
time_scale = linear
restart_step_counter = {restart}
refresh_vel = {refresh}
"""

p = argparse.ArgumentParser()
p.add_argument('--out', required=True)
p.add_argument('--interaction', default='DNA2')
p.add_argument('--sim-type', default='MD')
p.add_argument('--salt', default='0.5')
p.add_argument('--temperature', default='27C')
p.add_argument('--steps', default='100000')
p.add_argument('--dt', default='0.003')
p.add_argument('--seed', default='42')
p.add_argument('--topology', default='gen1_tile.top')
p.add_argument('--conf', required=True)
p.add_argument('--lastconf', required=True)
p.add_argument('--traj', required=True)
p.add_argument('--energy', required=True)
p.add_argument('--print-conf', default='10000')
p.add_argument('--print-energy', default='5000')
p.add_argument('--restart', default='1')
p.add_argument('--refresh', default='1')
p.add_argument('--max-backbone-force', default=None)
p.add_argument('--max-backbone-force-far', default='0.04',
               help="oxDNA's far-field backbone force, ~2 pN. It must be MUCH "
                    "SMALLER than --max-backbone-force: the capped potential "
                    "carries a factor (fmax - finf), so finf > fmax flips its "
                    "sign and applies hundreds of pN to every stretched bond.")
p.add_argument('--extra', action='append', default=[],
               help='extra key=value lines, repeatable')
a = p.parse_args()

text = TEMPLATE.format(sim_type=a.sim_type, interaction=a.interaction, salt=a.salt,
                       temperature=a.temperature, steps=a.steps, dt=a.dt,
                       seed=a.seed, topology=a.topology, conf=a.conf,
                       lastconf=a.lastconf, traj=a.traj, energy=a.energy,
                       print_conf=a.print_conf, print_energy=a.print_energy,
                       restart=a.restart, refresh=a.refresh)
if a.max_backbone_force:
    text += f"\nmax_backbone_force = {a.max_backbone_force}\n"
    text += f"max_backbone_force_far = {a.max_backbone_force_far}\n"
for kv in a.extra:
    k, _, v = kv.partition('=')
    text += f"{k} = {v}\n"
open(a.out, 'w').write(text)
print(f'wrote {a.out}')
