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

package com.xemantic.nano.plentyofroom.anchoring

/**
 * `T-71` — **the measured B-DNA backbone**, and every number in it comes from crystallographic
 * coordinates rather than from a citation or a force field.
 *
 * GENERATED — do not edit by hand. Produced by `tools/T-71-emit-kotlin-constants.py` from
 * `gpd/data/T-71-bdna-backbone-survey.json`, which is produced by
 * `tools/T-71-bdna-backbone-survey.py`. Both scripts are retained in this repository so the
 * derivation is reproducible and inspectable.
 *
 * ## Provenance
 *
 * RCSB search: X-ray, polymer composition **DNA only**, resolution ≤ 2.3 Å,
 * 900 entries returned and 876 carrying usable deoxyribonucleotides.
 * 15457 residues, 13084 phosphodiester linkages, 8883 helical steps.
 *
 * The **local frame** of a residue is `(ê_r, ê_t, ê_z)` anchored on its own phosphorus, with `ê_z`
 * the helical axis of a **three-residue window** oriented 5′→3′, `ê_r` radially outward to the
 * phosphorus and `ê_t = ê_z × ê_r`. A single dinucleotide step is a poor estimator of a helical
 * axis, which is why the window is three residues and why steps whose window superposes worse than
 * 0.45 Å are excluded — a step that is not a helix cannot define a helical frame.
 *
 * The two **templates** are not averages. Averaging local coordinates over a population whose
 * frames carry noise contracts every internal bond, so the template emitted is the population
 * **medoid** — one real, measured nucleotide, whose internal covalent geometry is a molecule's.
 *
 * Lengths are in **nm** (the survey works in Å; the conversion is applied here, once). Angles are
 * in **degrees**, IUPAC sign convention.
 */
object MeasuredBackbone {

    const val RESOLUTION_CEILING: Double = 2.3
    const val ENTRIES_RETURNED: Int = 900
    const val ENTRIES_USED: Int = 876
    const val RESIDUES: Int = 15457
    const val RESIDUES_SOUTH: Int = 9315
    const val LINKAGES: Int = 13084
    const val STEPS: Int = 8883
    const val STEPS_HELICAL: Int = 1081

    // --- the covalent geometry of the phosphodiester linkage, in nm and degrees
    const val O3_P_BOND: Double = 0.1602235969187527
    const val O3_P_BOND_SD: Double = 0.0019135271293672564
    const val P_O5_BOND: Double = 0.15954597302279788
    const val P_O5_BOND_SD: Double = 0.0017966834431214147
    const val ANGLE_C3_O3_P: Double = 121.29930085650382
    const val ANGLE_C3_O3_P_SD: Double = 3.0615245431500706
    const val ANGLE_O3_P_O5: Double = 103.29188287678615
    const val ANGLE_O3_P_O5_SD: Double = 2.5062032077781513
    const val ANGLE_P_O5_C5: Double = 120.12107872043784
    const val ANGLE_P_O5_C5_SD: Double = 2.836965428676673

    // --- the measured intrastrand P–P step, by sugar pucker, in nm
    const val STEP_SOUTH: Double = 0.6644805804152175
    const val STEP_SOUTH_SD: Double = 0.03616298498847594
    const val STEP_SOUTH_P1: Double = 0.5647615896019621
    const val STEP_SOUTH_P99: Double = 0.7567447527195827
    const val STEP_NORTH: Double = 0.6072335868104746
    const val STEP_NORTH_SD: Double = 0.04413700377180301
    const val STEP_NORTH_P1: Double = 0.5232770730653419
    const val STEP_NORTH_P99: Double = 0.7187557322852898
    const val STEP_ALL: Double = 0.648533706178641
    const val STEP_ALL_SD: Double = 0.04490057218379873
    const val STEP_ALL_P1: Double = 0.5378527656369049
    const val STEP_ALL_P99: Double = 0.7467380291585399

    // --- B-form, C2'-endo (south): medoid 8FB4 chain C residue 11 (DA), of 157 steps
    const val B_SOUTH_POPULATION: Int = 157
    const val B_SOUTH_SOURCE: String = "8FB4 C/11 DA"
    const val B_SOUTH_PHASE: Double = 150.82118443057294
    const val B_SOUTH_TWIST: Double = 36.197728146154425
    const val B_SOUTH_RISE: Double = 0.3495036957359521
    const val B_SOUTH_PHOSPHATE_RADIUS: Double = 0.890080460266049
    const val B_SOUTH_POPULATION_PHOSPHATE_RADIUS: Double = 0.9086378584708424
    const val B_SOUTH_POPULATION_PHOSPHATE_RADIUS_SD: Double = 0.06649945237217732
    const val B_SOUTH_POPULATION_TWIST: Double = 37.743977906628785
    const val B_SOUTH_POPULATION_RISE: Double = 0.3319382356361717
    const val B_SOUTH_ALPHA: Double = -44.91921346427989
    const val B_SOUTH_BETA: Double = 173.26047786792265
    const val B_SOUTH_CHI: Double = -111.26590611413327
    const val B_SOUTH_DELTA: Double = 129.5605728682969
    const val B_SOUTH_EPSILON: Double = -175.50765288581985
    const val B_SOUTH_GAMMA: Double = 45.224524907477196
    const val B_SOUTH_ZETA: Double = -99.031198206746
    val B_SOUTH_ATOMS: Map<String, Triple<Double, Double, Double>> = mapOf(
        "C1'" to Triple(-0.40861296328190655, 0.26851878965435666, 0.138124638788806),
        "C2'" to Triple(-0.29979042580951143, 0.28610187877916354, 0.24229229362896368),
        "C3'" to Triple(-0.18790785982170985, 0.35704855509831923, 0.16449408961852283),
        "C4'" to Triple(-0.21391815330385333, 0.32328548657434497, 0.019135774284249132),
        "C5'" to Triple(-0.11426168774167472, 0.22850662037402697, -0.040174135456337044),
        "CGLY" to Triple(-0.6227494461774743, 0.13715454295229665, 0.14406865944123756),
        "NGLY" to Triple(-0.48622430969155694, 0.146441992611208, 0.14564358367270233),
        "O3'" to Triple(-0.19018322844657332, 0.4948018249791343, 0.17857632434047843),
        "O4'" to Triple(-0.3448516941070274, 0.26490091582145897, 0.012686759573085155),
        "O5'" to Triple(-0.0903822010485817, 0.121712592919756, 0.04870885400379944),
        "OP1" to Triple(0.1372947449450086, 0.04782749618326037, -0.02275617760796436),
        "OP2" to Triple(-0.02314294364168668, -0.11076204936216838, 0.09490011897089634),
        "P" to Triple(0.0, 0.0, 0.0),
    )
    // the medoid's SUCCESSOR residue, in its own local frame about the same axis:
    // this is what makes the free limiting case a REAL dinucleotide
    const val B_SOUTH_NEXT_RADIUS: Double = 0.9248665162570799
    const val B_SOUTH_NEXT_PHASE: Double = 123.49291614578205
    const val B_SOUTH_STEP_TWIST: Double = 37.39397402549365
    const val B_SOUTH_STEP_RISE: Double = 0.3190008744544228
    const val B_SOUTH_NEXT_ALPHA: Double = -48.99059613882493
    const val B_SOUTH_NEXT_BETA: Double = 166.2484656090023
    const val B_SOUTH_NEXT_CHI: Double = -119.7876976812007
    const val B_SOUTH_NEXT_DELTA: Double = 117.33839382187251
    const val B_SOUTH_NEXT_EPSILON: Double = -175.50765288581985
    const val B_SOUTH_NEXT_GAMMA: Double = 49.95948046446185
    const val B_SOUTH_NEXT_ZETA: Double = -99.031198206746
    val B_SOUTH_NEXT_ATOMS: Map<String, Triple<Double, Double, Double>> = mapOf(
        "C1'" to Triple(-0.445228683340621, 0.25504292010485663, 0.1524492979162719),
        "C2'" to Triple(-0.3479105362674313, 0.27219348610375693, 0.26784208944225224),
        "C3'" to Triple(-0.22659806576615, 0.3364085828630848, 0.19843203362131323),
        "C4'" to Triple(-0.24385157266856194, 0.30041150553935414, 0.05035908901725077),
        "C5'" to Triple(-0.129904011061856, 0.22301569210251904, -0.01310721122418853),
        "CGLY" to Triple(-0.6771624911659485, 0.17912206394216065, 0.15555290664923846),
        "NGLY" to Triple(-0.5439872729391325, 0.1478631719420079, 0.16513324699615564),
        "O3'" to Triple(-0.22414926451439843, 0.4789001308094575, 0.21011202233175028),
        "O4'" to Triple(-0.36450483774883374, 0.22331859714565036, 0.04256145470487205),
        "O5'" to Triple(-0.09365377242803818, 0.11169351429284238, 0.06442538144948608),
        "OP1" to Triple(0.12937965280050198, 0.06256693693981338, -0.03385061067677973),
        "OP2" to Triple(-0.006243057499527503, -0.11954618110989493, 0.08739035882234221),
        "P" to Triple(0.0, 0.0, 0.0),
    )

    // --- A-form, C3'-endo (north): medoid 5XK1 chain C residue 5 (DC), of 152 steps
    const val A_NORTH_POPULATION: Int = 152
    const val A_NORTH_SOURCE: String = "5XK1 C/5 DC"
    const val A_NORTH_PHASE: Double = 15.39846447284867
    const val A_NORTH_TWIST: Double = 35.24798349934058
    const val A_NORTH_RISE: Double = 0.2641393273097015
    const val A_NORTH_PHOSPHATE_RADIUS: Double = 0.8508758017944393
    const val A_NORTH_POPULATION_PHOSPHATE_RADIUS: Double = 0.912842177179649
    const val A_NORTH_POPULATION_PHOSPHATE_RADIUS_SD: Double = 0.08257758077562877
    const val A_NORTH_POPULATION_TWIST: Double = 33.2513372650243
    const val A_NORTH_POPULATION_RISE: Double = 0.27391188413871587
    const val A_NORTH_ALPHA: Double = -64.85486086238404
    const val A_NORTH_BETA: Double = 164.4337297775443
    const val A_NORTH_CHI: Double = -161.16231200189327
    const val A_NORTH_DELTA: Double = 84.43243979493421
    const val A_NORTH_EPSILON: Double = -150.1798780824563
    const val A_NORTH_GAMMA: Double = 55.92050860208133
    const val A_NORTH_ZETA: Double = -65.58929963619892
    val A_NORTH_ATOMS: Map<String, Triple<Double, Double, Double>> = mapOf(
        "C1'" to Triple(-0.21432270892409333, 0.4367637673026617, -0.19663969083332267),
        "C2'" to Triple(-0.2146678176875216, 0.5188542451758046, -0.06803807986946105),
        "C3'" to Triple(-0.129001913765429, 0.43245874902863335, 0.02355985215247343),
        "C4'" to Triple(-0.02387476979788844, 0.3817868763161758, -0.07304400344884565),
        "C5'" to Triple(0.044791576721615295, 0.2537050974710743, -0.03120477803132702),
        "CGLY" to Triple(-0.4536458785835527, 0.39707098137629615, -0.2517925983682238),
        "NGLY" to Triple(-0.3315337499236008, 0.34516655828949944, -0.20937127238515055),
        "O3'" to Triple(-0.06748490619634215, 0.508849912046437, 0.1256532309413658),
        "O4'" to Triple(-0.09582792054063761, 0.3588151792424368, -0.19571084995491508),
        "O5'" to Triple(-0.04963482728812643, 0.15098032320451985, -0.00824535779312241),
        "OP1" to Triple(0.1195335505801895, -0.00826333902943459, 0.08679808473565484),
        "OP2" to Triple(-0.1208673600634981, -0.08026457924723457, 0.02649714304496106),
        "P" to Triple(0.0, 0.0, 0.0),
    )
    // the medoid's SUCCESSOR residue, in its own local frame about the same axis:
    // this is what makes the free limiting case a REAL dinucleotide
    const val A_NORTH_NEXT_RADIUS: Double = 0.8853612714743697
    const val A_NORTH_NEXT_PHASE: Double = 10.741743345030052
    const val A_NORTH_STEP_TWIST: Double = 35.62561693493827
    const val A_NORTH_STEP_RISE: Double = 0.27248381529732185
    const val A_NORTH_NEXT_ALPHA: Double = -68.11683150213865
    const val A_NORTH_NEXT_BETA: Double = 169.2630261116705
    const val A_NORTH_NEXT_CHI: Double = -157.1436461394888
    const val A_NORTH_NEXT_DELTA: Double = 83.99397671331101
    const val A_NORTH_NEXT_EPSILON: Double = -150.1798780824563
    const val A_NORTH_NEXT_GAMMA: Double = 54.19147258360821
    const val A_NORTH_NEXT_ZETA: Double = -65.58929963619892
    val A_NORTH_NEXT_ATOMS: Map<String, Triple<Double, Double, Double>> = mapOf(
        "C1'" to Triple(-0.2922995330223812, 0.3856350961937576, -0.20453120930197724),
        "C2'" to Triple(-0.29275200665434203, 0.47463794538024473, -0.0803913764347633),
        "C3'" to Triple(-0.18670472527890483, 0.40650525771763546, 0.004799063080173749),
        "C4'" to Triple(-0.0831192185825416, 0.3701348933467426, -0.0991633310726457),
        "C5'" to Triple(0.008013035724086338, 0.25608131754110525, -0.0591970443932477),
        "CGLY" to Triple(-0.5233749919767349, 0.29735981454210386, -0.23601734781333011),
        "NGLY" to Triple(-0.3914712775221713, 0.27277410557674003, -0.19856280165723061),
        "O3'" to Triple(-0.13001079592367992, 0.4942729655787986, 0.09974672145605107),
        "O4'" to Triple(-0.16104194206878028, 0.33193203486728307, -0.2149521507768192),
        "O5'" to Triple(-0.06917047692395777, 0.1429476395529394, -0.022279754674482972),
        "OP1" to Triple(0.12487941841619087, 0.015299043850446881, 0.0788811771799588),
        "OP2" to Triple(-0.106295267064933, -0.09115931297627598, 0.04747205343449896),
        "P" to Triple(0.0, 0.0, 0.0),
    )

    // --- the populated regions, marginal: ten-degree occupancy histograms from -180
    const val HISTOGRAM_BINS: Int = 36
    val TORSION_HISTOGRAM: Map<String, List<Int>> = mapOf(
        "alpha" to listOf(166, 130, 164, 120, 49, 35, 68, 111, 195, 564, 1940, 4268, 2740, 933, 323, 113, 60, 36, 25, 22, 31, 43, 96, 242, 523, 360, 238, 69, 54, 39, 61, 99, 197, 165, 238, 158),
        "beta" to listOf(2720, 1212, 469, 283, 313, 317, 150, 39, 25, 10, 5, 5, 2, 1, 0, 0, 1, 0, 0, 0, 0, 0, 2, 7, 12, 41, 30, 20, 37, 88, 172, 580, 866, 1041, 2910, 4099),
        "gamma" to listOf(623, 220, 57, 29, 24, 22, 19, 19, 18, 49, 92, 119, 83, 42, 21, 27, 22, 37, 58, 118, 288, 1150, 4232, 5233, 1525, 293, 115, 49, 25, 27, 17, 27, 33, 40, 160, 544),
        "delta" to listOf(2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 5, 50, 892, 2460, 1231, 801, 947, 1280, 2621, 3852, 1145, 148, 19),
        "epsilon" to listOf(2521, 2075, 1860, 1082, 619, 525, 506, 565, 694, 319, 168, 85, 22, 59, 46, 2, 0, 1, 1, 1, 3, 0, 8, 10, 15, 4, 8, 5, 4, 3, 2, 10, 20, 94, 335, 1412),
        "zeta" to listOf(261, 166, 127, 141, 187, 258, 469, 1046, 2254, 1965, 1894, 1245, 295, 70, 30, 13, 4, 3, 2, 4, 5, 12, 38, 111, 285, 373, 308, 126, 72, 58, 40, 63, 159, 261, 417, 322),
        "chi" to listOf(505, 1241, 1583, 869, 889, 1389, 2333, 2154, 1567, 1014, 465, 132, 41, 14, 4, 1, 2, 2, 2, 4, 5, 6, 26, 209, 480, 327, 42, 10, 6, 4, 3, 0, 1, 4, 25, 82),
    )
    val TORSION_HISTOGRAM_TOTAL: Map<String, Int> = mapOf(
        "alpha" to 14675,
        "beta" to 15457,
        "gamma" to 15457,
        "delta" to 15457,
        "epsilon" to 13084,
        "zeta" to 13084,
        "chi" to 15441,
    )

    // --- the populated regions, joint: k-means conformer classes over (α, β, γ, δ, ε, ζ, χ)
    const val CONFORMER_CLASSES: Int = 12
    val CONFORMERS: List<List<Double>> = listOf(
        // population, fraction, alpha, beta, gamma, delta, epsilon, zeta, chi,
        // radius95, radius99, radiusMax
        listOf(4735.0, 0.3807494371180444, -59.54221269863183, 172.2504575058501, 48.66895068024206, 131.27509282877273, -173.00685032031438, -98.01297117481619, -108.89651569853214, 53.76957117267497, 85.19150646031414, 139.524149206144),
        listOf(2613.0, 0.21011579285944035, -66.54863612229174, 169.12392574597342, 54.576676195443014, 85.69142259038935, -159.45700018379267, -75.03936401667904, -150.49343548072432, 47.6486517074957, 68.76723741329668, 140.79639721608584),
        listOf(1511.0, 0.12150209070440657, -64.53842310928788, 168.20927217433086, 45.86984667686401, 140.7745700300424, -119.14717940170709, 172.31607917228857, -90.88300378158947, 55.09233891115784, 76.85743596514402, 163.85178837055327),
        listOf(602.0, 0.048407848182695404, 73.29282465543295, -176.55305614738344, -176.06981309552145, 111.25518985106534, -144.90281667501887, -61.17072545008524, 64.54915346423256, 130.4617524837721, 156.3206392485057, 179.71331542128308),
        listOf(598.0, 0.048086201350916694, -170.9895518092, -146.10387381103038, 55.56496663091302, 142.85328141011797, -89.29367532993386, 78.4787048943428, -142.87238345680768, 79.55509403833344, 143.0437862044655, 162.7417163228314),
        listOf(458.0, 0.03682856223866195, 159.12708444733343, 175.31284789045844, -179.85536597958026, 104.99338148068469, -149.26652963339987, -78.670400076682, -156.19121399547876, 118.75917037104114, 146.73443632697433, 163.87686739645716),
        listOf(391.0, 0.031440977806368606, -66.46350111251942, -175.49011149143402, 52.85563009068491, 143.6379252626166, -83.40135076618148, 88.06677893297012, -108.99283770788503, 69.44041265114981, 172.9028214353218, 178.0984428576033),
        listOf(335.0, 0.02693792216146671, 173.3384260977982, -159.27844872777175, 54.67795940592533, 133.09661649580178, -152.92707619697532, -90.43130619434426, -146.13547253433393, 116.67595590721712, 154.80460997623743, 174.3377823733399),
        listOf(327.0, 0.026294628497909294, 67.61808722455577, 178.65434387204394, 54.11988043168463, 134.35085002657223, -142.48284869710315, -88.98746106226277, -130.04097186767342, 102.13699105409256, 126.88076562905746, 173.60579075219448),
        listOf(322.0, 0.025892569958185913, 68.23149904530901, -168.67819234364708, -130.69409503196195, 137.2630493871962, -142.97059734869012, -88.88076875065448, -121.80532336591479, 117.59571153449956, 156.76575462775259, 178.8388209040474),
        listOf(313.0, 0.025168864586683822, -32.57214861646498, 173.75635913390275, 44.76204046052142, 134.08770673830986, -139.8241781632199, 79.32926335854314, -129.2614537921401, 151.75535971897347, 172.1828444482466, 179.37049432830065),
        listOf(231.0, 0.01857510453522033, -65.88257973342455, -171.0491505129132, -36.033985553123685, 140.02526595697006, -160.72379656935658, -92.01616722773439, 93.67296333420227, 151.98458186663964, 170.04752209865546, 176.33062298930275),
    )
}
