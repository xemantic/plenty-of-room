/*
 * Copyright 2026 Kazimierz Pogoda / Xemantic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.xemantic.nano.plentyofroom.structure

import java.io.File

/** The directory every committed result file of this repository lives in. */
const val RESULT_DIRECTORY: String = "gpd/results"

/**
 * A committed result file, as a handle rather than as a path.
 *
 * `T-272`'s `P2`, and step 6 of [ARCHITECTURE.md](../../../../../../../ARCHITECTURE.md):
 * *"studies should declare typed input handles; then the census, `tools/reemission-order.py`'s*
 * *topological sort and staleness detection are free."*
 *
 * Before this, 74 study sources built their inputs as `File("gpd/results/…json")`, so the
 * dependency graph had to be **derived** by a static analysis of the Kotlin (`P-22`/`C-0082`)
 * -- and `C-0073` audited that graph with a `grep` and reported **one** reader of `T-1d` where
 * there are three, because a path assembled from a directory in the caller and a name in a
 * helper is invisible to a search for either half (`CH-0092`). The derivation stays, and is
 * still the authority; what changes is that a handle is a **declaration** the derivation can
 * read directly, so the two can be asserted equal.
 *
 * @param tag the task id that owns the file -- the handle `tools/reemission-order.py` sorts on.
 * @param fileName the committed file's own name, without the directory.
 */
data class ResultInput(val tag: String, val fileName: String) {

    /** The repository-relative path, which is the only place the directory is spelled. */
    val path: String get() = "$RESULT_DIRECTORY/$fileName"

    /** The file, relative to the working directory a study is run from. */
    fun file(): File = File(path)

    /**
     * The file inside an explicitly given directory.
     *
     * This is the shape `window/ResynthesisInputs.kt` uses and the shape `CH-0092`'s missing
     * edges were assembled in: a directory in the caller, a name in a helper. With a handle
     * the name is no longer in the helper.
     */
    fun file(directory: File): File = File(directory, fileName)

    /** The file's text. */
    fun readText(): String = file().readText()
}

/**
 * Every committed result file of this repository, one handle each.
 *
 * **Generated** by `tools/T-272-emit-result-inputs.py`, which is also a `--check`: a result
 * file with no handle fails it. A registry of this shape maintained by hand is a census that
 * stops, which is the failure `CLAUDE.md` records for every named set in this tree.
 */
object ResultInputs {

    val P_14: ResultInput = ResultInput("P-14", "P-14-cut-rim-charge.json")
    val P_18: ResultInput = ResultInput("P-18", "P-18-determined-precision.json")
    val P_22: ResultInput = ResultInput("P-22", "P-22-result-reader-census.json")
    val P_3: ResultInput = ResultInput("P-3", "P-3-peg-material-parameters.json")
    val P_30: ResultInput = ResultInput("P-30", "P-30-queue-row-coverage.json")
    val P_31: ResultInput = ResultInput("P-31", "P-31-mutation-harness-census.json")
    val P_6: ResultInput = ResultInput("P-6", "P-6-solvent-quality-vs-salt.json")
    val P_9: ResultInput = ResultInput("P-9", "P-9-grafted-chi.json")
    val T_1: ResultInput = ResultInput("T-1", "T-1-layer-stiffness.json")
    val T_10: ResultInput = ResultInput("T-10", "T-10-discrete-lattice-tile.json")
    val T_101: ResultInput = ResultInput("T-101", "T-101-single-column-flatness.json")
    val T_106: ResultInput = ResultInput("T-106", "T-106-truss-cap.json")
    val T_108: ResultInput = ResultInput("T-108", "T-108-desired-stroke-reach.json")
    val T_110: ResultInput = ResultInput("T-110", "T-110-consumed-crossover-sheet.json")
    val T_113: ResultInput = ResultInput("T-113", "T-113-non-uniform-coupling.json")
    val T_116: ResultInput = ResultInput("T-116", "T-116-hinge-arm-array-packing.json")
    val T_117: ResultInput = ResultInput("T-117", "T-117-crossbar-junction-trio.json")
    val T_118: ResultInput = ResultInput("T-118", "T-118-window-resynthesis-two.json")
    val T_119_LITERATURE_QUERIES: ResultInput = ResultInput("T-119", "T-119-literature-queries.json")
    val T_119_UNUSED_JUNCTION_SITE: ResultInput = ResultInput("T-119", "T-119-unused-junction-site.json")
    val T_12: ResultInput = ResultInput("T-12", "T-12-lateral-confinement.json")
    val T_120: ResultInput = ResultInput("T-120", "T-120-connectivity-ceiling-plate.json")
    val T_121: ResultInput = ResultInput("T-121", "T-121-stacked-arm-sheet.json")
    val T_122: ResultInput = ResultInput("T-122", "T-122-buildable-stiffness-ratio.json")
    val T_123: ResultInput = ResultInput("T-123", "T-123-robust-distribution.json")
    val T_124: ResultInput = ResultInput("T-124", "T-124-torsion-feasible-routing.json")
    val T_125: ResultInput = ResultInput("T-125", "T-125-upward-root-placement.json")
    val T_126: ResultInput = ResultInput("T-126", "T-126-arm-slab-clearance.json")
    val T_127: ResultInput = ResultInput("T-127", "T-127-crossbar-trio-existence.json")
    val T_129: ResultInput = ResultInput("T-129", "T-129-range-robust-placement.json")
    val T_13: ResultInput = ResultInput("T-13", "T-13-zero-bias-resting-position.json")
    val T_130: ResultInput = ResultInput("T-130", "T-130-crossbar-array-placement.json")
    val T_132: ResultInput = ResultInput("T-132", "T-132-pinned-leg-budget.json")
    val T_133: ResultInput = ResultInput("T-133", "T-133-output-element-placement.json")
    val T_134: ResultInput = ResultInput("T-134", "T-134-plan-tolerance.json")
    val T_135: ResultInput = ResultInput("T-135", "T-135-output-element-recommendation.json")
    val T_136: ResultInput = ResultInput("T-136", "T-136-two-per-row-placement.json")
    val T_137: ResultInput = ResultInput("T-137", "T-137-weave-exclusion-width.json")
    val T_138: ResultInput = ResultInput("T-138", "T-138-path-count-consistency.json")
    val T_139: ResultInput = ResultInput("T-139", "T-139-duplex-pair-separation.json")
    val T_14: ResultInput = ResultInput("T-14", "T-14-crossover-phase-and-registration.json")
    val T_140: ResultInput = ResultInput("T-140", "T-140-seam-weave-congruence.json")
    val T_147: ResultInput = ResultInput("T-147", "T-147-third-answers-synthesis.json")
    val T_148: ResultInput = ResultInput("T-148", "T-148-staple-dropout.json")
    val T_149: ResultInput = ResultInput("T-149", "T-149-recommended-element-fold.json")
    val T_15: ResultInput = ResultInput("T-15", "T-15-in-plane-shear-lag.json")
    val T_151: ResultInput = ResultInput("T-151", "T-151-scaffold-routing.json")
    val T_152: ResultInput = ResultInput("T-152", "T-152-collinear-clearance.json")
    val T_153: ResultInput = ResultInput("T-153", "T-153-buildable-raster-width.json")
    val T_155: ResultInput = ResultInput("T-155", "T-155-dropout-robust-placement.json")
    val T_156: ResultInput = ResultInput("T-156", "T-156-buffer-route-census.json")
    val T_157: ResultInput = ResultInput("T-157", "T-157-large-rotation-arm-branch.json")
    val T_159: ResultInput = ResultInput("T-159", "T-159-doubling-ladder-repair.json")
    val T_16: ResultInput = ResultInput("T-16", "T-16-output-coupling-stiffness.json")
    val T_160: ResultInput = ResultInput("T-160", "T-160-edge-width-dependence.json")
    val T_161: ResultInput = ResultInput("T-161", "T-161-row-end-crossover.json")
    val T_162: ResultInput = ResultInput("T-162", "T-162-shared-body-coupling.json")
    val T_163: ResultInput = ResultInput("T-163", "T-163-path-count-fixed-geometry.json")
    val T_164: ResultInput = ResultInput("T-164", "T-164-row-end-crossover-stiffness.json")
    val T_165: ResultInput = ResultInput("T-165", "T-165-shared-body-placement.json")
    val T_169: ResultInput = ResultInput("T-169", "T-169-withdrawn-ceiling-note.json")
    val T_17: ResultInput = ResultInput("T-17", "T-17-one-row-per-duplex.json")
    val T_171: ResultInput = ResultInput("T-171", "T-171-crossover-phase-selection.json")
    val T_172: ResultInput = ResultInput("T-172", "T-172-row-end-prestrain.json")
    val T_175: ResultInput = ResultInput("T-175", "T-175-fourth-answers-synthesis.json")
    val T_178: ResultInput = ResultInput("T-178", "T-178-count-phase-interaction.json")
    val T_182: ResultInput = ResultInput("T-182", "T-182-row-end-prestrain-value.json")
    val T_183: ResultInput = ResultInput("T-183", "T-183-challenge-status-self-consistency.json")
    val T_184: ResultInput = ResultInput("T-184", "T-184-decision-file-drift.json")
    val T_188: ResultInput = ResultInput("T-188", "T-188-buildable-width-count-phase.json")
    val T_189: ResultInput = ResultInput("T-189", "T-189-twist-corrected-raster.json")
    val T_19: ResultInput = ResultInput("T-19", "T-19-attachment-entry-topology.json")
    val T_190: ResultInput = ResultInput("T-190", "T-190-interior-crossover-prestrain.json")
    val T_191: ResultInput = ResultInput("T-191", "T-191-four-layer-tile.json")
    val T_192: ResultInput = ResultInput("T-192", "T-192-device-b-tall-gap.json")
    val T_193: ResultInput = ResultInput("T-193", "T-193-gold-electrode-pzc.json")
    val T_194: ResultInput = ResultInput("T-194", "T-194-one-reserve.json")
    val T_195: ResultInput = ResultInput("T-195", "T-195-scaffold-remainder.json")
    val T_196: ResultInput = ResultInput("T-196", "T-196-composite-fraction-threshold.json")
    val T_197: ResultInput = ResultInput("T-197", "T-197-coupled-four-layer.json")
    val T_198: ResultInput = ResultInput("T-198", "T-198-honeycomb-raster-width.json")
    val T_199: ResultInput = ResultInput("T-199", "T-199-cross-section-comparison.json")
    val T_1C: ResultInput = ResultInput("T-1c", "T-1c-crossover-valid-layer-response.json")
    val T_1D: ResultInput = ResultInput("T-1d", "T-1d-scf-density-profile.json")
    val T_1E: ResultInput = ResultInput("T-1e", "T-1e-first-moment-convention.json")
    val T_1F: ResultInput = ResultInput("T-1f", "T-1f-mean-field-fluctuation-corrections.json")
    val T_2: ResultInput = ResultInput("T-2", "T-2-design-window.json")
    val T_200: ResultInput = ResultInput("T-200", "T-200-reemission-order.json")
    val T_201: ResultInput = ResultInput("T-201", "T-201-fifth-answers-synthesis.json")
    val T_202: ResultInput = ResultInput("T-202", "T-202-sixth-answers-synthesis.json")
    val T_203: ResultInput = ResultInput("T-203", "T-203-honeycomb-station-lattice.json")
    val T_204: ResultInput = ResultInput("T-204", "T-204-collar-aspect-ratio.json")
    val T_205: ResultInput = ResultInput("T-205", "T-205-four-layer-supersession.json")
    val T_206: ResultInput = ResultInput("T-206", "T-206-oblique-root.json")
    val T_207: ResultInput = ResultInput("T-207", "T-207-format-string-repair.json")
    val T_208: ResultInput = ResultInput("T-208", "T-208-result-file-hygiene.json")
    val T_21: ResultInput = ResultInput("T-21", "T-21-concentrated-crossover.json")
    val T_211: ResultInput = ResultInput("T-211", "T-211-seventh-answers-synthesis.json")
    val T_212: ResultInput = ResultInput("T-212", "T-212-departure-and-saturation-audits.json")
    val T_214: ResultInput = ResultInput("T-214", "T-214-departure-rule-scope.json")
    val T_215: ResultInput = ResultInput("T-215", "T-215-descent-manifold-width.json")
    val T_216: ResultInput = ResultInput("T-216", "T-216-mixed-domain-phase-lattice.json")
    val T_217: ResultInput = ResultInput("T-217", "T-217-honeycomb-twist-correction.json")
    val T_218: ResultInput = ResultInput("T-218", "T-218-honeycomb-raster-turn-sense.json")
    val T_219: ResultInput = ResultInput("T-219", "T-219-honeycomb-station-lattice-and-placement.json")
    val T_220: ResultInput = ResultInput("T-220", "T-220-level-not-a-stiffness-error-bar.json")
    val T_221: ResultInput = ResultInput("T-221", "T-221-planar-coupling-wall.json")
    val T_225: ResultInput = ResultInput("T-225", "T-225-departure-spelling-set.json")
    val T_226: ResultInput = ResultInput("T-226", "T-226-nonuniform-coupling-manifold.json")
    val T_23: ResultInput = ResultInput("T-23", "T-23-two-sided-coupling.json")
    val T_230: ResultInput = ResultInput("T-230", "T-230-honeycomb-turn-loop-slack.json")
    val T_231: ResultInput = ResultInput("T-231", "T-231-ragged-face-cost.json")
    val T_232: ResultInput = ResultInput("T-232", "T-232-coupled-cells-at-the-honeycomb-cross-section.json")
    val T_234: ResultInput = ResultInput("T-234", "T-234-honeycomb-correction-supersession.json")
    val T_235: ResultInput = ResultInput("T-235", "T-235-coupled-cells-at-the-two-length-raster.json")
    val T_243: ResultInput = ResultInput("T-243", "T-243-columns-from-row-spans.json")
    val T_244: ResultInput = ResultInput("T-244", "T-244-face-bond-class-residues.json")
    val T_245: ResultInput = ResultInput("T-245", "T-245-closing-raster-selection.json")
    val T_246: ResultInput = ResultInput("T-246", "T-246-forced-scaffold-crossover-price.json")
    val T_249: ResultInput = ResultInput("T-249", "T-249-unrounded-prose-interpolations.json")
    val T_25: ResultInput = ResultInput("T-25", "T-25-window-resynthesis.json")
    val T_250: ResultInput = ResultInput("T-250", "T-250-prose-interpolation-sweep.json")
    val T_252: ResultInput = ResultInput("T-252", "T-252-a-quoted-number-has-no-link-back.json")
    val T_253: ResultInput = ResultInput("T-253", "T-253-honeycomb-grillage.json")
    val T_254: ResultInput = ResultInput("T-254", "T-254-raster-turn-prestrain.json")
    val T_255: ResultInput = ResultInput("T-255", "T-255-cadnano-gallery-forced-crossovers.json")
    val T_258: ResultInput = ResultInput("T-258", "T-258-drawable-ragged-face.json")
    val T_260: ResultInput = ResultInput("T-260", "T-260-partial-discharge-predicate.json")
    val T_261: ResultInput = ResultInput("T-261", "T-261-a-price-on-an-adjudicated-challenge.json")
    val T_262: ResultInput = ResultInput("T-262", "T-262-width-restatement-predicate.json")
    val T_263: ResultInput = ResultInput("T-263", "T-263-honeycomb-grillage-regrade.json")
    val T_267: ResultInput = ResultInput("T-267", "T-267-mechanics-on-imported-design.json")
    val T_274: ResultInput = ResultInput("T-274", "T-274-recommended-block-seam.json")
    val T_275: ResultInput = ResultInput("T-275", "T-275-simulated-tile-census.json")
    val T_276: ResultInput = ResultInput("T-276", "T-276-thirteenth-answers-synthesis.json")
    val T_278: ResultInput = ResultInput("T-278", "T-278-emission-header-residue.json")
    val T_279: ResultInput = ResultInput("T-279", "T-279-tied-honeycomb-regrade.json")
    val T_280: ResultInput = ResultInput("T-280", "T-280-debt-line-as-a-ratio.json")
    val T_281: ResultInput = ResultInput("T-281", "T-281-name-the-discharge.json")
    val T_282: ResultInput = ResultInput("T-282", "T-282-classification-regeneration.json")
    val T_283: ResultInput = ResultInput("T-283", "T-283-residue-as-a-gate.json")
    val T_284: ResultInput = ResultInput("T-284", "T-284-turn-prestrain-sign.json")
    val T_285: ResultInput = ResultInput("T-285", "T-285-a-slug-is-not-a-statement.json")
    val T_286: ResultInput = ResultInput("T-286", "T-286-a-regime-is-a-set.json")
    val T_287: ResultInput = ResultInput("T-287", "T-287-a-filename-cannot-supply-a-context.json")
    val T_289: ResultInput = ResultInput("T-289", "T-289-a-verdict-in-the-wrong-column.json")
    val T_291: ResultInput = ResultInput("T-291", "T-291-common-mode-departure-and-beam-twist.json")
    val T_292: ResultInput = ResultInput("T-292", "T-292-the-column-repair.json")
    val T_293: ResultInput = ResultInput("T-293", "T-293-a-name-cannot-govern-a-token.json")
    val T_294: ResultInput = ResultInput("T-294", "T-294-the-tied-regrade-at-the-other-cross-section.json")
    val T_295: ResultInput = ResultInput("T-295", "T-295-mutation-input-census.json")
    val T_296: ResultInput = ResultInput("T-296", "T-296-zero-loop-raster-turn.json")
    val T_297: ResultInput = ResultInput("T-297", "T-297-the-common-mode-is-the-link.json")
    val T_298: ResultInput = ResultInput("T-298", "T-298-a-challenges-status-row-is-the-authority.json")
    val T_299: ResultInput = ResultInput("T-299", "T-299-tethered-raster-turn-regrade.json")
    val T_3: ResultInput = ResultInput("T-3", "T-3-stroke-and-blocking-force.json")
    val T_30: ResultInput = ResultInput("T-30", "T-30-flexure-end-joint.json")
    val T_300: ResultInput = ResultInput("T-300", "T-300-a-length-is-not-a-provenance.json")
    val T_302: ResultInput = ResultInput("T-302", "T-302-si-staple-order.json")
    val T_303: ResultInput = ResultInput("T-303", "T-303-what-link-stiffness-the-recovery-needs.json")
    val T_304: ResultInput = ResultInput("T-304", "T-304-raster-turn-anchor-azimuth.json")
    val T_307: ResultInput = ResultInput("T-307", "T-307-uniform-raster-tether-spans.json")
    val T_310: ResultInput = ResultInput("T-310", "T-310-a-bond-link-is-two-mechanisms.json")
    val T_315: ResultInput = ResultInput("T-315", "T-315-the-uniform-raster-at-the-resolved-link.json")
    val T_316: ResultInput = ResultInput("T-316", "T-316-a-searched-distribution-at-the-resolved-link.json")
    val T_319: ResultInput = ResultInput("T-319", "T-319-fourteenth-answers-synthesis.json")
    val T_322: ResultInput = ResultInput("T-322", "T-322-route-b-coupled-on-its-own-stations.json")
    val T_323: ResultInput = ResultInput("T-323", "T-323-the-placement-and-the-distribution-together.json")
    val T_326: ResultInput = ResultInput("T-326", "T-326-the-fit-and-the-sample-in-one-reconstruction.json")
    val T_330: ResultInput = ResultInput("T-330", "T-330-a-dishing-fit-and-the-parity-of-its-basis.json")
    val T_332: ResultInput = ResultInput("T-332", "T-332-fifteenth-answers-synthesis.json")
    val T_334: ResultInput = ResultInput("T-334", "T-334-the-gate-census-by-reachability.json")
    val T_3A: ResultInput = ResultInput("T-3a", "T-3a-nonlinear-pb-profile.json")
    val T_3B: ResultInput = ResultInput("T-3b", "T-3b-tile-edge-load-profile.json")
    val T_4: ResultInput = ResultInput("T-4", "T-4-maximum-usable-bias.json")
    val T_40: ResultInput = ResultInput("T-40", "T-40-standoff-base-joint.json")
    val T_5: ResultInput = ResultInput("T-5", "T-5-load-distribution.json")
    val T_50: ResultInput = ResultInput("T-50", "T-50-beyond-mean-field-gap.json")
    val T_5B: ResultInput = ResultInput("T-5b", "T-5b-tile-flatness.json")
    val T_6: ResultInput = ResultInput("T-6", "T-6-mean-field-screening-validity.json")
    val T_60: ResultInput = ResultInput("T-60", "T-60-collar-on-the-equilibrium-path.json")
    val T_65: ResultInput = ResultInput("T-65", "T-65-coupled-standoff-joint.json")
    val T_67: ResultInput = ResultInput("T-67", "T-67-perpendicular-junction-routing.json")
    val T_7: ResultInput = ResultInput("T-7", "T-7-poroelastic-drainage.json")
    val T_70: ResultInput = ResultInput("T-70", "T-70-guided-arm-anchorage.json")
    val T_71: ResultInput = ResultInput("T-71", "T-71-backbone-torsion-closure.json")
    val T_72: ResultInput = ResultInput("T-72", "T-72-triangulated-standoff.json")
    val T_75: ResultInput = ResultInput("T-75", "T-75-flexure-mounting-sense.json")
    val T_76: ResultInput = ResultInput("T-76", "T-76-softening-coupling-stability.json")
    val T_79: ResultInput = ResultInput("T-79", "T-79-two-spring-elastica.json")
    val T_8: ResultInput = ResultInput("T-8", "T-8-tile-positional-variance.json")
    val T_81: ResultInput = ResultInput("T-81", "T-81-hinge-line-census.json")
    val T_9: ResultInput = ResultInput("T-9", "T-9-crossover-hinge-constant.json")
    val T_96: ResultInput = ResultInput("T-96", "T-96-flexure-array-packing.json")
    val T_97: ResultInput = ResultInput("T-97", "T-97-paired-perpendicular-junction.json")
    val T_99: ResultInput = ResultInput("T-99", "T-99-flexure-count-hinge-trade.json")
    val T_9B: ResultInput = ResultInput("T-9b", "T-9b-crossover-vertical-compliance.json")

    /** Every handle, in the order the files sort. */
    val all: List<ResultInput> = listOf(
        P_14, P_18, P_22, P_3, P_30, P_31, P_6, P_9, T_1, T_10, T_101, T_106, T_108, T_110, T_113,
        T_116, T_117, T_118, T_119_LITERATURE_QUERIES, T_119_UNUSED_JUNCTION_SITE, T_12, T_120,
        T_121, T_122, T_123, T_124, T_125, T_126, T_127, T_129, T_13, T_130, T_132, T_133, T_134,
        T_135, T_136, T_137, T_138, T_139, T_14, T_140, T_147, T_148, T_149, T_15, T_151, T_152,
        T_153, T_155, T_156, T_157, T_159, T_16, T_160, T_161, T_162, T_163, T_164, T_165, T_169,
        T_17, T_171, T_172, T_175, T_178, T_182, T_183, T_184, T_188, T_189, T_19, T_190, T_191,
        T_192, T_193, T_194, T_195, T_196, T_197, T_198, T_199, T_1C, T_1D, T_1E, T_1F, T_2, T_200,
        T_201, T_202, T_203, T_204, T_205, T_206, T_207, T_208, T_21, T_211, T_212, T_214, T_215,
        T_216, T_217, T_218, T_219, T_220, T_221, T_225, T_226, T_23, T_230, T_231, T_232, T_234,
        T_235, T_243, T_244, T_245, T_246, T_249, T_25, T_250, T_252, T_253, T_254, T_255, T_258,
        T_260, T_261, T_262, T_263, T_267, T_274, T_275, T_276, T_278, T_279, T_280, T_281, T_282,
        T_283, T_284, T_285, T_286, T_287, T_289, T_291, T_292, T_293, T_294, T_295, T_296, T_297,
        T_298, T_299, T_3, T_30, T_300, T_302, T_303, T_304, T_307, T_310, T_315, T_316, T_319,
        T_322, T_323, T_326, T_330, T_332, T_334, T_3A, T_3B, T_4, T_40, T_5, T_50, T_5B, T_6, T_60,
        T_65, T_67, T_7, T_70, T_71, T_72, T_75, T_76, T_79, T_8, T_81, T_9, T_96, T_97, T_99, T_9B
    )

    /** The handle a task id names, or `null` where a task id names two files. */
    fun ofTag(tag: String): ResultInput? =
        all.singleOrNull { it.tag == tag }
}
