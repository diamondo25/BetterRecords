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
package tech.feldman.betterrecords.network

import tech.feldman.betterrecords.api.event.RadioInsertEvent
import tech.feldman.betterrecords.api.sound.Sound
import io.netty.buffer.ByteBuf
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.network.ByteBufUtils
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

class PacketRadioPlay @JvmOverloads constructor(
        var pos: BlockPos = BlockPos(0, 0, 0),
        var dimension: Int = -1,
        var playRadius: Float = -1F,
        var local: String = "",
        var url: String = ""
) : IMessage {

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(pos.x)
        buf.writeInt(pos.y)
        buf.writeInt(pos.z)
        buf.writeInt(dimension)

        buf.writeFloat(playRadius)

        ByteBufUtils.writeUTF8String(buf, local)
        ByteBufUtils.writeUTF8String(buf, url)
    }

    override fun fromBytes(buf: ByteBuf) {
        pos = BlockPos(buf.readInt(), buf.readInt(), buf.readInt())
        dimension = buf.readInt()

        playRadius = buf.readFloat()

        local = ByteBufUtils.readUTF8String(buf)
        url = ByteBufUtils.readUTF8String(buf)
    }

    fun postToBus() {
        MinecraftForge.EVENT_BUS.post(RadioInsertEvent(pos, dimension, playRadius, Sound(url, local)))
    }

    class Handler : IMessageHandler<PacketRadioPlay, IMessage> {

        override fun onMessage(message: PacketRadioPlay, ctx: MessageContext): IMessage? {
            message.postToBus()

            return null
        }
    }
}
