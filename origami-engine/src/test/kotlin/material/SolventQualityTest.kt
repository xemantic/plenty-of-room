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

package com.xemantic.nano.plentyofroom.material

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow
import kotlin.test.Test

/**
 * Task `P-6` — `χ(T, salt)` for PEG in water, and the two channels through which the Gen-1
 * buffer could reach the layer's mechanics.
 *
 * Every assertion is either a conversion between the three currencies solvent quality is quoted
 * in, or a **transfer function** — how far a shift in one propagates into the osmotic modulus.
 * The transfer functions are what `T-3` needs. The absolute value of `χ` is not, and this file
 * is arranged so that nothing load-bearing depends on it, because it turns out to be the least
 * well determined quantity in the whole task.
 */
class SolventQualityTest {

    private val peg = PegWater()
    private val chi = ReciprocalTemperatureChi()
    private val chiQuadratic = ThetaExpansionChi()
    private val virial = ChainSecondVirialCoefficient()
    private val waterSite = waterMoleculeVolume()

    private val cloudPointDerivative = chi.chiTemperatureDerivative(PEO_CLOUD_POINT)

    /** The ceiling on `k_s` from Boucher & Hines (1976), the strongest PEO salts. */
    private val strongestSaltCeiling = CloudPointDepression(
        slope = 69.0,
        saltFreeCloudPoint = 369.0,
        fittedRangeLow = 0.1,
        fittedRangeHigh = 1.0,
        source = "ceiling constructed from Boucher & Hines, J. Polym. Sci. Polym. Phys. Ed. " +
                "14:2241 (1976): theta across their whole salt survey lies between 300 and 360 K " +
                "against a salt-free 369 +/- 3 K"
    )

    // ---------------------------------------------------------------- gate 1: units and dimensions

    @Test
    fun `should convert a molarity into a number density per cubic nanometre`() {
        assert(PER_CUBIC_NANOMETRE_PER_MOLAR.isCloseTo(0.602214076, relativeTolerance = 1e-12))
        assert(ionNumberDensity(molarity = 1.0, ionsPerFormulaUnit = 1).isCloseTo(0.602214076, 1e-12))
        // 10 mM MgCl2 dissociates into three ions
        assert(ionNumberDensity(0.010, 3).isCloseTo(0.01806642228, relativeTolerance = 1e-9))
    }

    @Test
    fun `should derive the water molecule volume rather than cite it`() {
        // The Flory-Huggins lattice site of the measured PEG/water chi. 0.0300 nm^3.
        assert(waterSite.isCloseTo(0.0300179, relativeTolerance = 1e-4))
        // and the PEG monomer is almost exactly two water molecules, which is the factor that
        // gets dropped when a water-lattice chi is fed into v = v0 (1 - 2 chi)
        assert((peg.monomerVolume / waterSite).isCloseTo(2.0105, relativeTolerance = 1e-3))
    }

    @Test
    fun `should keep the blob transfer exponent dimensionless`() {
        // gate 1: alpha = v0 * v_K^(3/4) * b^(3/2) / v_Kvol^(9/4) is nm^(3+9/4+3/2-27/4) = nm^0.
        // Scaling every length by a factor must therefore leave alpha unchanged.
        val scale = 1.7
        val plain = desCloizeauxIndexFromExcludedVolume(
            kuhnPairExcludedVolume = 0.30,
            kuhnLength = peg.kuhnLength,
            kuhnSegmentVolume = peg.kuhnSegmentVolume,
            monomerVolume = peg.monomerVolume
        )
        val scaled = desCloizeauxIndexFromExcludedVolume(
            kuhnPairExcludedVolume = 0.30 * scale.pow(3.0),
            kuhnLength = peg.kuhnLength * scale,
            kuhnSegmentVolume = peg.kuhnSegmentVolume * scale.pow(3.0),
            monomerVolume = peg.monomerVolume * scale.pow(3.0)
        )
        assert(scaled.isCloseTo(plain, relativeTolerance = 1e-12))
    }

    // ------------------------------------------------------- the measured chi, and its own gates

    @Test
    fun `should evaluate the measured chi of PEG in water at the operating temperature`() {
        // Pedersen & Sommer, chi = 1.156 - 235.3/T, SAXS on PEG 4600 in D2O.
        assert(chi.chi(ROOM_TEMPERATURE).isCloseTo(0.3716667, relativeTolerance = 1e-6))
        assert(chi.chi(283.15).isCloseTo(0.3249, relativeTolerance = 1e-3))  // their "0.32 at 10 C"
    }

    @Test
    fun `should recover the published theta temperature from the chi parameters alone`() {
        // gate 5, and it is a strong one: the theta temperature is NOT a fitted parameter of the
        // chi = a + b/T form — it is where that form crosses 1/2. Solving gives 358.69 K = 85.5 C,
        // against the 85.7 +/- 1.1 C the SAME paper obtains from an independent quadratic fit.
        // Two analyses of one dataset agreeing to 0.2 K.
        assert(chi.thetaTemperature.isCloseTo(358.689, relativeTolerance = 1e-4))
        assert(abs(chi.thetaTemperature - chiQuadratic.thetaTemperature) < 1.1)
    }

    @Test
    fun `should agree between the two published parameterisations at the operating temperature`() {
        // gate 5. They agree to 1.8% in chi — and only to 5.5% in (1 - 2 chi), which is the
        // quantity that actually matters. A small disagreement in chi is a large one in
        // solvent quality, because chi sits so close to 1/2.
        val a = chi.chi(ROOM_TEMPERATURE)
        val b = chiQuadratic.chi(ROOM_TEMPERATURE)
        assert(relativeDisagreement(a, b) < 0.02)
        assert(relativeDisagreement(1.0 - 2.0 * a, 1.0 - 2.0 * b) > 0.04)
        assert(relativeDisagreement(1.0 - 2.0 * a, 1.0 - 2.0 * b) < 0.07)
    }

    @Test
    fun `should disagree between the two parameterisations at the cloud point`() {
        // gate 5 read the other way, and the reason the transfer function carries a band:
        // dchi/dT at the CLOUD POINT is where the salt shift is read, and there the quadratic
        // form has already turned over. The two differ by 44% at 369 K and by 53% at 373 K.
        val a = chi.chiTemperatureDerivative(PEO_CLOUD_POINT)
        val b = chiQuadratic.chiTemperatureDerivative(PEO_CLOUD_POINT)
        assert(a.isCloseTo(1.72810e-3, relativeTolerance = 1e-4))
        assert(b.isCloseTo(1.19670e-3, relativeTolerance = 1e-4))
        assert(relativeDisagreement(a, b) > 0.4)
        // and at the virial theta temperature they are 53% apart
        assert(
            relativeDisagreement(
                chi.chiTemperatureDerivative(373.15),
                chiQuadratic.chiTemperatureDerivative(373.15)
            ) > 0.5
        )
    }

    @Test
    fun `should make chi at the operating temperature differ from the derivative at the cloud point`() {
        // 2.61e-3 at 300 K against 1.73e-3 at the 369 K cloud point — a 51% difference, and
        // using the wrong one is a silent 51% error in the salt sensitivity.
        assert(chi.chiTemperatureDerivative(ROOM_TEMPERATURE).isCloseTo(2.61444e-3, 1e-5))
        assert(
            relativeDisagreement(
                chi.chiTemperatureDerivative(ROOM_TEMPERATURE),
                chi.chiTemperatureDerivative(PEO_CLOUD_POINT)
            ) > 0.5
        )
    }

    @Test
    fun `should reduce to theta conditions where chi is one half`() {
        // gate 2, limiting case. The excluded volume vanishes at the theta temperature and is
        // negative above it — PEG in water is LCST, so ABOVE theta the solvent is poor.
        // Getting this sign backwards would invert every conclusion about salt.
        val atTheta = monomerExcludedVolume(chi.chi(chi.thetaTemperature), peg.monomerVolume, waterSite)
        assert(atTheta.isCloseTo(0.0, relativeTolerance = 1e-14))
        assert(monomerExcludedVolume(chi.chi(400.0), peg.monomerVolume, waterSite) < 0.0)
        assert(monomerExcludedVolume(chi.chi(ROOM_TEMPERATURE), peg.monomerVolume, waterSite) > 0.0)
    }

    // ------------------------------------------------- the lattice convention, which hides a 2x

    @Test
    fun `should double the excluded volume when the lattice site is a water molecule`() {
        // THE trap of this task, and the exact analogue of C-0002's three meanings of `a`.
        // A chi fitted on a water-molecule lattice, fed naively into v0 (1 - 2 chi), understates
        // the monomer excluded volume by v0/v_water = 2.01.
        val naive = peg.monomerVolume * (1.0 - 2.0 * chi.chi())
        val correct = monomerExcludedVolume(chi.chi(), peg.monomerVolume, waterSite)
        assert((correct / naive).isCloseTo(2.0105, relativeTolerance = 1e-3))
        // and the naive form is recovered exactly when the site IS the monomer
        assert(
            monomerExcludedVolume(chi.chi(), peg.monomerVolume, peg.monomerVolume)
                .isCloseTo(naive, relativeTolerance = 1e-14)
        )
    }

    @Test
    fun `should round-trip chi through the excluded volume`() {
        val v = monomerExcludedVolume(0.41, peg.monomerVolume, waterSite)
        assert(chiFromMonomerExcludedVolume(v, peg.monomerVolume, waterSite).isCloseTo(0.41, 1e-12))
    }

    @Test
    fun `should agree with the independently measured second virial coefficient`() {
        // gate 5 — the load-bearing cross-check of this task. Two routes to the same number:
        //   chi(300 K) route:  v = v0 (v0/v_water)(1 - 2 chi)      = 0.0311 nm^3
        //   B2(300 K) route:   v = 2 B2 / N^2, B2 = 2.00 (373.2-T) = 0.0269 nm^3
        // They agree to 16%, which is close given that they rest on theta temperatures 14 K
        // apart (the Flory-Huggins 358.7 K against the virial 373.2 K).
        val fromChi = monomerExcludedVolume(chi.chi(), peg.monomerVolume, waterSite)
        val fromVirial = virial.monomerExcludedVolume(peg.monomerMolarMass)
        assert(fromChi.isCloseTo(0.031144, relativeTolerance = 1e-3))
        assert(fromVirial.isCloseTo(0.026854, relativeTolerance = 1e-3))
        assert(relativeDisagreement(fromChi, fromVirial) < 0.20)
        // and the naive lattice convention would have MISSED that agreement by a factor of two
        val naive = peg.monomerVolume * (1.0 - 2.0 * chi.chi())
        assert(relativeDisagreement(naive, fromVirial) > 0.40)
    }

    @Test
    fun `should report a second virial coefficient of the right published magnitude`() {
        // B2 = 146 nm^3 at 300 K for PEG 4600, i.e. A2 ~ 4e-3 mol cm^3 / g^2 — the value the
        // PEG light-scattering literature has carried for decades. Order-of-magnitude gate.
        assert(virial.secondVirialCoefficient(ROOM_TEMPERATURE).isCloseTo(146.4, 1e-4))
        assert(virial.secondVirialCoefficient(virial.thetaTemperature).isCloseTo(0.0, 1e-12))
        assert(virial.secondVirialCoefficient(400.0) < 0.0)
        assert(virial.monomersPerChain(peg.monomerMolarMass).isCloseTo(104.4198, 1e-4))
    }

    @Test
    fun `should place the cited chi of 0-45 outside the measured band`() {
        // C-0001 carried chi = 0.45 and C-0002 left it "neither confirmed nor used".
        // It is now falsified as a bulk value: the measurement gives 0.372-0.378 at 300 K,
        // and 0.45 would mean an excluded volume 2.6 times smaller.
        val measured = chi.chi(ROOM_TEMPERATURE)
        assert(abs(0.45 - measured) > 0.07)
        val vMeasured = monomerExcludedVolume(measured, peg.monomerVolume, waterSite)
        val vCited = monomerExcludedVolume(0.45, peg.monomerVolume, waterSite)
        assert((vMeasured / vCited) > 2.5)
    }

    // ------------------------------------------- the blob relation, used only for its derivative

    @Test
    fun `should predict the measured crossover index to within an order-unity blob prefactor`() {
        // gate 5. C-0002 says the adopted equation of state "yields neither A2 nor chi", which is
        // true VIRIALLY. Through the correlation-blob relation it does yield an excluded volume,
        // up to the unknown O(1) prefactor of Pi = C k_BT/xi^3. At C = 1 the relation returns
        // alpha = 1.22 against the measured 0.49, so C = 0.40 — order unity, as claimed.
        val v = monomerExcludedVolume(chi.chi(), peg.monomerVolume, waterSite)
        val predicted = desCloizeauxIndexFromExcludedVolume(
            kuhnPairExcludedVolume = kuhnPairExcludedVolume(v, peg.monomersPerKuhnSegment),
            kuhnLength = peg.kuhnLength,
            kuhnSegmentVolume = peg.kuhnSegmentVolume,
            monomerVolume = peg.monomerVolume
        )
        assert(predicted.isCloseTo(1.2208, relativeTolerance = 2e-3))
        assert((peg.crossoverIndex / predicted) in 0.1..1.0)
    }

    @Test
    fun `should get the same blob prefactor from the virial route`() {
        // and the check is not circular: starting from the INDEPENDENT B2 measurement the
        // implied prefactor is 0.45 rather than 0.40. Two routes, one order-unity prefactor.
        val v = virial.monomerExcludedVolume(peg.monomerMolarMass)
        val predicted = desCloizeauxIndexFromExcludedVolume(
            kuhnPairExcludedVolume = kuhnPairExcludedVolume(v, peg.monomersPerKuhnSegment),
            kuhnLength = peg.kuhnLength,
            kuhnSegmentVolume = peg.kuhnSegmentVolume,
            monomerVolume = peg.monomerVolume
        )
        assert((peg.crossoverIndex / predicted).isCloseTo(0.4484, relativeTolerance = 5e-3))
    }

    @Test
    fun `should round-trip the blob relation exactly`() {
        val v = 0.30
        val alpha = desCloizeauxIndexFromExcludedVolume(
            v, peg.kuhnLength, peg.kuhnSegmentVolume, peg.monomerVolume
        )
        val back = kuhnPairExcludedVolumeFromDesCloizeauxIndex(
            alpha, peg.kuhnLength, peg.kuhnSegmentVolume, peg.monomerVolume
        )
        assert(back.isCloseTo(v, relativeTolerance = 1e-12))
    }

    @Test
    fun `should make the crossover index sensitivity independent of the blob prefactor`() {
        // The whole reason the blob relation is carried. Whatever C is, d ln alpha / d ln v = 3/4.
        listOf(0.1, 0.4, 1.0, 3.0).forEach { prefactor ->
            val low = desCloizeauxIndexFromExcludedVolume(
                0.30, peg.kuhnLength, peg.kuhnSegmentVolume, peg.monomerVolume, prefactor
            )
            val high = desCloizeauxIndexFromExcludedVolume(
                0.30 * 1.01, peg.kuhnLength, peg.kuhnSegmentVolume, peg.monomerVolume, prefactor
            )
            assert((high / low).isCloseTo(1.01.pow(0.75), relativeTolerance = 1e-12))
        }
    }

    // ------------------------------------------------ channel 1: mobile ions, and its exact zero

    @Test
    fun `should give the mobile ions an osmotic pressure larger than the layer's own`() {
        // Why the salt cannot be dismissed on magnitude grounds: 10 mM MgCl2 = 30 mM of ions
        // carries 0.0748 pN/nm^2 of van't Hoff pressure, against the ~0.0215 pN/nm^2 the polymer
        // layer itself musters (C-0002). Three and a half times the signal.
        val ionPressure = thermalEnergy() * ionNumberDensity(0.010, 3)
        assert(ionPressure.isCloseTo(0.0748300, relativeTolerance = 1e-5))
        assert(ionPressure > 3.0 * 0.02151)
    }

    @Test
    fun `should recover the equation of state from its own free energy density`() {
        // gate 1 + gate 4: Pi = phi f'(phi) - f(phi). If the free-energy density is not the right
        // potential for the adopted equation of state, the exact-zero result below means nothing.
        val eos = peg.equationOfState(monomersPerChain = 199.4)
        listOf(0.005, 0.0289, 0.1, 0.4).forEach { phi ->
            val transformed = osmoticPressureOfFreeEnergyDensity(phi) { eos.freeEnergyDensity(it) }
            assert(transformed.isCloseTo(eos.pressure(phi), relativeTolerance = 1e-6))
        }
    }

    @Test
    fun `should converge the Legendre transform at second order in the step`() {
        // gate 4, numerical convergence: a central difference, so halving the step must cut the
        // error by about four.
        val eos = peg.equationOfState(monomersPerChain = 199.4)
        val phi = 0.0289
        val exact = eos.pressure(phi)
        val coarse = abs(
            osmoticPressureOfFreeEnergyDensity(phi, step = 1e-4) { eos.freeEnergyDensity(it) } - exact
        )
        val fine = abs(
            osmoticPressureOfFreeEnergyDensity(phi, step = 5e-5) { eos.freeEnergyDensity(it) } - exact
        )
        assert(fine < coarse)
        assert(coarse / fine > 3.0)
    }

    @Test
    fun `should exert exactly no osmotic pressure from ideal salt excluded by the polymer`() {
        // THE channel-1 result. Ideal mobile ions excluded from the polymer's own volume
        // contribute a free-energy density k_BT n_s phi — strictly LINEAR in phi — and a linear
        // term is annihilated by Pi = phi f' - f. So the ion pressure, three and a half times the
        // layer's own, cancels to machine precision rather than being neglected as small.
        val eos = peg.equationOfState(monomersPerChain = 199.4)
        val ions = ionNumberDensity(molarity = 0.010, ionsPerFormulaUnit = 3)
        val phi = 0.0289
        val withSalt = osmoticPressureOfFreeEnergyDensity(phi) {
            eos.freeEnergyDensity(it) + excludedSaltFreeEnergyDensity(ions, it)
        }
        val without = osmoticPressureOfFreeEnergyDensity(phi) { eos.freeEnergyDensity(it) }
        assert(abs(withSalt - without) / (thermalEnergy() * ions) < 1e-9)
    }

    @Test
    fun `should make the excluded salt free energy of a grafted layer independent of its height`() {
        // gate 3, conservation — the same statement in its physical form. The layer's polymer
        // volume per unit area is CONSERVED under compression, and the ideal excluded-salt energy
        // is proportional to exactly that. So the salt exerts no force on the tile at any height,
        // at any concentration, and no bias-dependent term hides here.
        val ions = ionNumberDensity(0.010, 3)
        val energies = listOf(4.0, 7.0, 10.0).map { height ->
            excludedSaltFreeEnergyDensity(
                ions, peg.volumeFraction(199.4, 0.024, height)
            ) * height
        }
        assert(energies[1].isCloseTo(energies[0], relativeTolerance = 1e-12))
        assert(energies[2].isCloseTo(energies[0], relativeTolerance = 1e-12))
    }

    // ----------------------------------------- channel 2: solvent quality, the one that survives

    @Test
    fun `should depress the cloud point linearly in salt molarity`() {
        val d = CloudPointDepression(100.0, 375.0, 0.1, 1.0, "hypothetical")
        assert(d.cloudPoint(0.0).isCloseTo(375.0, 1e-12))
        assert(d.cloudPoint(0.010).isCloseTo(374.0, 1e-12))
        assert(d.cloudPoint(0.002).isCloseTo(374.8, relativeTolerance = 1e-12))
    }

    @Test
    fun `should flag the Gen-1 buffer as an extrapolation below every published salt series`() {
        // The trap this task was warned about. Published PEG salt series sit at 0.1-1 M; the
        // Gen-1 buffer is 2-10 mM. That is 1.0 to 1.7 decades of extrapolation, and it is
        // reported as a number rather than as a caveat.
        val d = CloudPointDepression(100.0, 375.0, 0.1, 1.0, "hypothetical")
        assert(d.isExtrapolatedAt(0.002))
        assert(d.isExtrapolatedAt(0.010))
        assert(!d.isExtrapolatedAt(0.5))
        assert(d.extrapolationDecades(0.002).isCloseTo(log10(50.0), 1e-12))
        assert(d.extrapolationDecades(0.010).isCloseTo(1.0, 1e-12))
        assert(d.extrapolationDecades(0.5).isCloseTo(0.0, 1e-12))
        assert(d.extrapolationDecades(2.0).isCloseTo(log10(2.0), 1e-12))
    }

    @Test
    fun `should convert one kelvin of cloud point depression into one percent of excluded volume`() {
        // The transfer function of the whole task, and it is worth stating in words:
        // ONE KELVIN of cloud-point depression costs about ONE PER CENT of PEG's excluded volume.
        // Because (1 - 2 chi) = 0.257 is a small difference of two numbers near 1/2, a shift in
        // chi four decimal places out is a per-cent shift in solvent quality.
        val perKelvin = CloudPointDepression(1.0, 369.0, 0.1, 1.0, "unit slope")
        val shift = solventQualityShift(
            depression = perKelvin,
            lowMolarity = 0.002,
            highMolarity = 1.002,   // exactly one molar apart, so the shift is exactly one kelvin
            chiAtOperatingTemperature = chi.chi(),
            chiTemperatureDerivativeAtCloudPoint = cloudPointDerivative,
            monomerVolume = peg.monomerVolume,
            latticeSiteVolume = waterSite
        )
        assert(shift.cloudPointShift.isCloseTo(-1.0, relativeTolerance = 1e-12))
        assert(shift.chiShift.isCloseTo(1.72810e-3, relativeTolerance = 1e-4))
        assert(shift.excludedVolumeFractionalShift.isCloseTo(-0.0134657, relativeTolerance = 1e-4))
        assert(shift.crossoverIndexFractionalShift.isCloseTo(-0.0101220, relativeTolerance = 1e-3))
        assert(shift.equilibriumHeightFractionalShift.isCloseTo(-0.0045088, relativeTolerance = 1e-3))
    }

    @Test
    fun `should keep the buffer step under one percent of excluded volume at the literature ceiling`() {
        // THE RESULT. Boucher & Hines surveyed sulfates, carbonates, nitrates and chlorides and
        // found theta between 300 and 360 K against a salt-free 369 +/- 3 K, so no salt in that
        // survey depresses theta by more than 69 K per molar — and chlorides are explicitly
        // "much less effective" than the sulfates and carbonates that set the ceiling.
        // Applied to the whole 2 -> 10 mM Gen-1 buffer step, that ceiling gives 0.55 K.
        val shift = bufferStep(strongestSaltCeiling)
        assert(shift.cloudPointShift.isCloseTo(-0.552, relativeTolerance = 1e-6))
        assert(abs(shift.chiShift) < 1e-3)
        assert(abs(shift.excludedVolumeFractionalShift) < 0.01)
        assert(shift.excludedVolumeFractionalShift.isCloseTo(-0.0074331, relativeTolerance = 1e-4))
    }

    @Test
    fun `should require an unphysical salt for the buffer step to move the modulus one percent`() {
        // The result restated as a falsifiable threshold, which is the honest form given that no
        // k_s for MgCl2 exists: the cloud-point slope MgCl2 would need for the 2-10 mM step to
        // move the layer's osmotic modulus by 1% is 95 K/M on the most pessimistic transfer —
        // above the 69 K/M ceiling that the strongest salting-out salts of PEO reach, for a salt
        // that the ATPS literature places on the salting-IN side and that forms no binodal
        // with PEG at all.
        val threshold = slopeForModulusChange(0.01)
        assert(threshold.isCloseTo(92.83, relativeTolerance = 1e-3))
        assert(threshold > strongestSaltCeiling.slope)
    }

    @Test
    fun `should reverse sign when the salt salts in rather than out`() {
        // gate 3, symmetry, and not a formality: the Hofmeister literature places MgCl2 on the
        // salting-IN side for PEG, which is the opposite of the direction section 2 of the
        // problem definition asserts. The model carries both signs.
        val out = bufferStep(strongestSaltCeiling)
        val inward = bufferStep(strongestSaltCeiling.copy(slope = -strongestSaltCeiling.slope))
        assert(out.chiShift.isCloseTo(-inward.chiShift, relativeTolerance = 1e-12))
        assert(out.chiShift > 0.0)
        assert(inward.chiShift < 0.0)
        assert(out.excludedVolumeFractionalShift < 0.0)
        assert(inward.excludedVolumeFractionalShift > 0.0)
    }

    @Test
    fun `should vanish when the two buffers are the same`() {
        // gate 2, limiting case.
        val shift = solventQualityShift(
            strongestSaltCeiling, 0.005, 0.005, chi.chi(), cloudPointDerivative,
            peg.monomerVolume, waterSite
        )
        assert(shift.cloudPointShift.isCloseTo(0.0, 1e-15))
        assert(shift.chiShift.isCloseTo(0.0, 1e-15))
        assert(shift.excludedVolumeFractionalShift.isCloseTo(0.0, 1e-15))
    }

    // ---------------------------------------------------------------- the layer's own response

    @Test
    fun `should carry the osmotic modulus of the equation of state`() {
        val eos = peg.equationOfState(monomersPerChain = 199.4)
        val phi = 0.0289
        val closedForm = eos.vanTHoffPressure(phi) + 2.25 * eos.desCloizeauxPressure(phi)
        assert(eos.osmoticModulus(phi).isCloseTo(closedForm, relativeTolerance = 1e-12))
        // and it equals Pi * m_eff, which is the same statement read the other way
        assert(
            eos.osmoticModulus(phi)
                .isCloseTo(eos.pressure(phi) * eos.localExponent(phi), relativeTolerance = 1e-12)
        )
        // at the 10 nm design point the solvent-quality-dependent limb supplies 72% of it
        assert(eos.desCloizeauxModulusFraction(phi).isCloseTo(0.72374, relativeTolerance = 1e-4))
    }

    @Test
    fun `should move the layer's modulus by less than the excluded volume moves`() {
        // The end of the chain. The van't Hoff limb does not know about solvent quality at all,
        // so the modulus is LESS sensitive than the excluded volume, by the des Cloizeaux
        // fraction. Sign and magnitude both matter.
        val eos = peg.equationOfState(monomersPerChain = 199.4)
        val phi = 0.0289
        val f = -0.01
        val response = eos.osmoticModulusResponse(phi, f)
        assert(response < 0.0)
        assert(abs(response) < abs(f))
        val expected = eos.desCloizeauxModulusFraction(phi) * ((1.0 + f).pow(0.75) - 1.0)
        assert(response.isCloseTo(expected, relativeTolerance = 1e-12))
    }

    @Test
    fun `should bound the modulus response by the mean field transfer exponent`() {
        // gate 2. The two admissible transfer exponents are 3/4 (blob scaling, below the thermal
        // blob concentration) and 1 (mean field, above it). PEG's thermal-blob volume fraction is
        // v/v0 = 0.52, and the layer sits at 0.03-0.07, so the blob exponent is the right one —
        // but the mean-field one is larger and is therefore what the bound is taken on.
        val eos = peg.equationOfState(monomersPerChain = 199.4)
        val phi = 0.0289
        val blob = abs(eos.osmoticModulusResponse(phi, -0.01, DES_CLOIZEAUX_TRANSFER_EXPONENT))
        val meanField = abs(eos.osmoticModulusResponse(phi, -0.01, MEAN_FIELD_TRANSFER_EXPONENT))
        assert(meanField > blob)
        assert(meanField <= 0.01)
    }

    @Test
    fun `should keep the layer above the thermal blob volume fraction check`() {
        // gate 5, premise check on the actual material. The 9/4 exponent belongs to the
        // GOOD-SOLVENT semidilute window, whose upper edge is the thermal-blob volume fraction
        // phi** = v/v0. For PEG at 300 K that is 0.516 — five to eighteen times the layer's own
        // 0.029-0.071, so the layer is comfortably inside, and comfortably more so than the
        // cited chi = 0.45 would have suggested (which gives phi** = 0.20).
        val thermalBlob =
            monomerExcludedVolume(chi.chi(), peg.monomerVolume, waterSite) / peg.monomerVolume
        assert(thermalBlob.isCloseTo(0.516022, relativeTolerance = 1e-4))
        listOf(0.0708, 0.0439, 0.0289, 0.0335).forEach { assert(it / thermalBlob < 0.2) }
    }

    @Test
    fun `should hold the buffer step under half a percent of the layer's modulus`() {
        // The hand-off to T-3, as a number. At the literature ceiling on the strongest PEO salt,
        // applied across the whole 2 -> 10 mM buffer range, the layer's osmotic modulus — and
        // therefore its stiffness, since k/A = K/h — moves by less than half a per cent.
        val eos = peg.equationOfState(monomersPerChain = 199.4)
        val f = bufferStep(strongestSaltCeiling).excludedVolumeFractionalShift
        val response = eos.osmoticModulusResponse(0.0289, f)
        assert(abs(response) < 0.005)
        // and even on the most pessimistic transfer exponent, with every last bit of the modulus
        // attributed to interactions, it stays under a per cent
        assert(abs(f * MEAN_FIELD_TRANSFER_EXPONENT) < 0.01)
    }

    @Test
    fun `should not stay under a per cent across the layer's local ionic range`() {
        // The correction C-0005 forces. The PEG layer does not sit in the buffer: bulk salt is
        // DEPLETED into it (K_salt = 0.52-0.77) while the tile's counterions FLOOD it, so the
        // local Mg2+ span is roughly 1 mM to 66 mM — a factor of 66, not the factor of 5 the
        // buffer range suggests. Over that span the ceiling no longer buys a sub-per-cent answer.
        val eos = peg.equationOfState(monomersPerChain = 199.4)
        val local = solventQualityShift(
            strongestSaltCeiling, LOCAL_IONIC_LOW, LOCAL_IONIC_HIGH, chi.chi(),
            cloudPointDerivative, peg.monomerVolume, waterSite
        )
        assert(local.cloudPointShift.isCloseTo(-4.485, relativeTolerance = 1e-6))
        assert(abs(local.excludedVolumeFractionalShift) > 0.05)
        assert(abs(eos.osmoticModulusResponse(0.0289, local.excludedVolumeFractionalShift)) > 0.01)
        // it is still bounded, and still small, but it is no longer negligible by inspection
        assert(abs(eos.osmoticModulusResponse(0.0289, local.excludedVolumeFractionalShift)) < 0.05)
    }

    @Test
    fun `should make the counterion concentration a function of the actuator's own stroke`() {
        // The mechanism, and why the local range is not a fixed number: the counterion inventory
        // per unit area is set by the tile's charge and does not change, so halving the gap
        // doubles the concentration. The layer's ionic environment is therefore a function of
        // the stroke — a coupling nothing downstream currently carries.
        // half of C-0005's Manning-surviving tile charge (1276 e over 40 x 40 nm), the half
        // that faces the gap
        val sigma = 0.5 * 1276.0 / 1600.0
        val ten = gapAveragedCounterionMolarity(sigma, gapHeight = 10.0)
        val five = gapAveragedCounterionMolarity(sigma, gapHeight = 5.0)
        assert((five / ten).isCloseTo(2.0, relativeTolerance = 1e-12))
        assert(ten.isCloseTo(0.033107, relativeTolerance = 1e-3))
        assert(five.isCloseTo(0.066214, relativeTolerance = 1e-3))
        // and it is an order of magnitude above the 2-10 mM buffer, which is C-0005's point
        assert(ten > 0.010)
        assert(five > 6.0 * 0.010)
    }

    @Test
    fun `should dwarf every salt effect by the grafting dependence of chi`() {
        // The comparison that decides how much this task's answer matters. Lee et al. (2012)
        // fit neutron reflectivity from a dense PEO brush with SCF and obtain an EFFECTIVE
        // chi(brush)/chi(theta) ~ 1.2, i.e. chi ~ 0.60 — above theta, poor solvent — against
        // ~0.92, i.e. chi ~ 0.46, for free chains. Whatever one thinks of an effective
        // parameter, the shift is 0.23 in chi, against under 0.001 for the whole buffer range.
        val brushChi = 1.2 * THETA_CHI
        val bulkChi = chi.chi()
        val graftingShift = brushChi - bulkChi
        val saltShift = abs(bufferStep(strongestSaltCeiling).chiShift)
        assert(graftingShift > 0.2)
        assert(graftingShift / saltShift > 100.0)
        // and the grafted value is on the far side of theta, where the excluded volume is negative
        assert(monomerExcludedVolume(brushChi, peg.monomerVolume, waterSite) < 0.0)
    }

    @Test
    fun `should dwarf the buffer step by the spread in the theta temperature itself`() {
        // The other comparison. "The theta temperature of PEG in water" is a 358.7-375 K band
        // across the Flory-Huggins, cloud-point, virial and cited determinations — 16 K wide,
        // i.e. THIRTY times the 0.55 K the whole buffer range can muster at the salt ceiling.
        val thetaBand = listOf(chi.thetaTemperature, 369.0, virial.thetaTemperature, 375.0)
        val width = thetaBand.max() - thetaBand.min()
        assert(width > 16.0)
        assert(width / abs(bufferStep(strongestSaltCeiling).cloudPointShift) > 25.0)
    }

    @Test
    fun `should reject non-physical arguments`() {
        assertFails { ReciprocalTemperatureChi(interceptA = 0.4) }
        assertFails { ReciprocalTemperatureChi(slopeB = 235.3) }
        assertFails { CloudPointDepression(100.0, -1.0, 0.1, 1.0, "") }
        assertFails { CloudPointDepression(100.0, 375.0, 1.0, 0.1, "") }
        assertFails { ionNumberDensity(-1.0, 3) }
        assertFails { ionNumberDensity(0.01, 0) }
        assertFails { monomerExcludedVolume(0.4, 0.0, 0.03) }
        assertFails { gapAveragedCounterionMolarity(0.8, 0.0) }
        assertFails { osmoticPressureOfFreeEnergyDensity(0.01, step = 0.02) { it } }
    }

    // ----------------------------------------------------------------------------- helpers

    private fun bufferStep(depression: CloudPointDepression) = solventQualityShift(
        depression = depression,
        lowMolarity = BUFFER_LOW,
        highMolarity = BUFFER_HIGH,
        chiAtOperatingTemperature = chi.chi(),
        chiTemperatureDerivativeAtCloudPoint = cloudPointDerivative,
        monomerVolume = peg.monomerVolume,
        latticeSiteVolume = waterSite
    )

    /** The cloud-point slope in K/M that the 2-10 mM step would need to move `v` by [target]. */
    private fun slopeForModulusChange(target: Double): Double {
        val perUnitSlope = abs(
            solventQualityShift(
                CloudPointDepression(1.0, 369.0, 0.1, 1.0, ""), BUFFER_LOW, BUFFER_HIGH,
                chi.chi(), cloudPointDerivative, peg.monomerVolume, waterSite
            ).excludedVolumeFractionalShift
        )
        return target / perUnitSlope
    }

    private fun assertFails(block: () -> Unit) {
        val failed = try {
            block()
            false
        } catch (e: IllegalArgumentException) {
            true
        }
        assert(failed)
    }

    private companion object {
        /** The salt-free cloud point of PEO in water, 369 +/- 3 K — Boucher & Hines (1978). */
        const val PEO_CLOUD_POINT = 369.0
        const val BUFFER_LOW = 0.002
        const val BUFFER_HIGH = 0.010
        /** Bulk salt depleted into the layer at the 2 mM buffer, `C-0005` `K_salt = 0.52`. */
        const val LOCAL_IONIC_LOW = 0.001
        /** Gap-averaged counterion concentration at a 5 nm gap, `C-0005`. */
        const val LOCAL_IONIC_HIGH = 0.066
    }
}
