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
package tech.feldman.betterrecords.block.tile

import tech.feldman.betterrecords.api.connection.RecordConnection
import tech.feldman.betterrecords.api.record.IRecordAmplitude
import tech.feldman.betterrecords.api.wire.IRecordWire
import tech.feldman.betterrecords.helper.ConnectionHelper
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ITickable
import java.util.*
import kotlin.math.pow
import kotlin.math.sin

class TileLaser : ModTile(), IRecordWire, IRecordAmplitude, ITickable {

    override var connections = mutableListOf<RecordConnection>()

    var pitch = 0F
    var yaw = 0F

    var mounting: Mounting = Mounting.FLOOR

    var length = 10
    var active = false

    var r = 0F
    var g = 0F
    var b = 0F

    class MovementInfo(val maxDegrees: Float, val speed: Float) {
        var current = 0F

        private val startOffset = Random().nextInt() % 2000

        init {
            update(0)
        }

        fun update(tickCount: Int) {
            val actualTC = startOffset + tickCount
            current = sin(actualTC * 0.3f * speed) * maxDegrees
        }
    }

    private val animRandom = Random()

    val pitchAnim = MovementInfo(10F + animRandom.nextFloat() * 10f, animRandom.nextFloat())
    val yawAnim = MovementInfo(10F + animRandom.nextFloat() * 10f, animRandom.nextFloat())

    var animationTicks = 0

    var lastColorReset = 0L

    fun rayWidth() = (bass / 200f).coerceAtLeast(0.001f)
    override var treble = 0F
    override var bass = 0F
        set(value) {
            field = value

            val n = System.currentTimeMillis()
            if ((n - lastColorReset) > 100) {
                lastColorReset = n
                animRandom.run {
                    r = 0.5f + nextFloat()
                    g = 0.5f + nextFloat()
                    b = 0.5f + nextFloat()
                }

                animRandom.nextInt().also {
                    r += if (it == 0) .03f else -.01f
                    g += if (it == 1) .03f else -.01f
                    b += if (it == 2) .03f else -.01f
                }

                r = r.coerceIn(.3F, 1F)
                g = g.coerceIn(.3F, 1F)
                b = b.coerceIn(.3F, 1F)
            }
        }

    override fun getName() = "Laser"

    override val songRadiusIncrease = 0F

    override fun update() {
        if (!active) return

        if (bass > 0) bass--
        if (bass < 0) bass = 0F

        animationTicks++
        yawAnim.update(animationTicks)
        pitchAnim.update(animationTicks)
    }

    override fun readFromNBT(compound: NBTTagCompound) = compound.run {
        super.readFromNBT(compound)

        connections = ConnectionHelper.unserializeConnections(getString("connections")).toMutableList()
        pitch = getFloat("pitch")
        yaw = getFloat("yaw")
        length = getInteger("length")

        mounting = try {
            Mounting.byName(getString("mounting"))
        } catch (e: Exception) {
            // Default to floor mounting
            Mounting.FLOOR
        }
    }

    override fun writeToNBT(compound: NBTTagCompound) = compound.apply {
        super.writeToNBT(compound)

        setString("connections", ConnectionHelper.serializeConnections(connections))
        setFloat("pitch", pitch)
        setFloat("yaw", yaw)
        setInteger("length", length)
        setString("mounting", mounting.nbtName)
    }


    enum class Mounting(val nbtName: String) {
        CEILING("ceiling"),
        FLOOR("floor"),
        NORTH("north"),
        EAST("east"),
        SOUTH("south"),
        WEST("west");

        // Yaw first, then Pitch (in rendering)
        fun getPitchAndYaw(): Pair<Float, Float> {
            return when (this) {
                CEILING -> Pair(180f, 0f)
                NORTH -> Pair(90f, 0f)
                EAST -> Pair(90f, 90f)
                SOUTH -> Pair(270f, 0f)
                WEST -> Pair(270f, 90f)
                else -> Pair(0f, 0f)
            }
        }

        companion object {
            fun byName(n: String) = values().find { x -> x.nbtName == n }!!
        }
    }
}