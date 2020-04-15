/**
 * The MIT License
 *
 * Copyright (c) 2019 Nicholas Feldman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package tech.feldman.betterrecords.util

import tech.feldman.betterrecords.api.wire.IRecordWireHome
import tech.feldman.betterrecords.block.tile.TileSpeaker
import tech.feldman.betterrecords.extensions.distanceTo
import net.minecraft.client.Minecraft
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow

const val NoVolume = -80F

// https://en.wikipedia.org/wiki/Sound_pressure#Distance
fun getSPLOverDistance(baseSPL: Double, distanceMeter: Double): Double {
    // aka Lp1
    val measureDistanceMeter = 1.0
    val dL = measureDistanceMeter + (20.0 * ln(distanceMeter / measureDistanceMeter))

    return baseSPL - dL
}

// http://www.sengpielaudio.com/calculator-coherentsources.htm
@Suppress("ReplaceJavaStaticMethodWithKotlinAnalog")
fun calcCoherentPressure(vararg SPLs: Double): Double = 20.0 * log10(SPLs.sumByDouble { x -> Math.pow(10.0, x / 20.0) })

fun getIngameVolume(): Float {
    val gs = Minecraft.getMinecraft().gameSettings
    val userMultiplier = gs.getSoundLevel(SoundCategory.MASTER) * gs.getSoundLevel(SoundCategory.RECORDS)

    return userMultiplier
}

fun getGainForPlayerPosition(pos: BlockPos): Float {
    val player = Minecraft.getMinecraft().player
    val world = Minecraft.getMinecraft().world


    // The record player or Radio.
    // If it isn't one of those, we return a volume of zero.
    val te = world?.getTileEntity(pos) as? IRecordWireHome ?: return NoVolume

    val playerHeadPos = player.getPositionEyes(1.0f)

    val radioDb = 50.0
    val distanceToRadio = playerHeadPos.distanceTo(Vec3d(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()))

    val speakersDb = te.connections.mapNotNull {
        val pos = Vec3d(it.x2.toDouble(), it.y2.toDouble(), it.z2.toDouble())

        // If the tile isn't a speaker, we don't care about it
        if (world.getTileEntity(BlockPos(pos)) !is TileSpeaker) {
            null
        } else {

            // Distance in minecraft even for small is huge, so sound fades to quick
            val d = (playerHeadPos.distanceTo(pos) / 1.5)

            // Todo: use sane values for speaker loudness
            val speakerDb = 90.0


            getSPLOverDistance(speakerDb, d)
        }
    }.toDoubleArray()

    val audibleDb = calcCoherentPressure(
        // Calculate SPL for the radio
        getSPLOverDistance(radioDb, distanceToRadio),
        *speakersDb
    )

    val ret =  NoVolume + audibleDb.toFloat()

    return if (ret.isInfinite()) {
        0f
    } else {
        ret
    }
}
