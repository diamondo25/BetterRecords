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

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ITickable
import tech.feldman.betterrecords.api.wire.IWireSoundDevice
import tech.feldman.betterrecords.api.wire.IWireSoundSink
import java.util.*

class TileLaserCluster : ModTile(), ITickable, IWireSoundSink {

    override val children = mutableListOf<IWireSoundDevice>()

    var r = 0F
    var g = 0F
    var b = 0F

    override var treble = 0F
    override var bass = 0F
        set(value) {
            field = value
            Random(value.toLong() + System.nanoTime()).run {
                r = nextFloat()
                g = nextFloat()
                b = nextFloat()
            }

            Random().nextInt().also {
                r += if (it == 0) .3f else -.1f
                g += if (it == 1) .3f else -.1f
                b += if (it == 2) .3f else -.1f
            }

            if (r < .2F) r += r
            if (g < .2F) g += g
            if (b < .2F) b += b
        }

    override val soundBonus = 0F

    override fun update() {
        if (bass > 0) bass--
        if (bass < 0) bass = 0F
    }

    override fun readFromNBT(compound: NBTTagCompound) = compound.run {
        super.readFromNBT(compound)
    }

    override fun writeToNBT(compound: NBTTagCompound) = compound.apply {
        super.writeToNBT(compound)
    }
}
