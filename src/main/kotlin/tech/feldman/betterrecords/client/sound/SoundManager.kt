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
package tech.feldman.betterrecords.client.sound

import tech.feldman.betterrecords.api.sound.Sound
import net.minecraft.util.math.BlockPos
import net.minecraftforge.event.world.ChunkEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import tech.feldman.betterrecords.ID
import kotlin.concurrent.thread

@Mod.EventBusSubscriber(Side.CLIENT, modid = ID)
object SoundManager {

    private val jobs = hashMapOf<Pair<BlockPos, Int>, Thread>()

    fun queueSongsAt(pos: BlockPos, dimension: Int, sounds: List<Sound>, shuffle: Boolean = false, repeat: Boolean = false) {
        val job = thread {
            while (true) {
                sounds.forEach {
                    SoundPlayer.playSound(pos, dimension, it)
                }

                if (!repeat) {
                    return@thread
                }
            }
        }

        jobs[Pair(pos, dimension)] = job
    }

    fun queueStreamAt(pos: BlockPos, dimension: Int, sound: Sound) {
        val job = thread {
            SoundPlayer.playSoundFromStream(pos, dimension, sound)
        }

        jobs[Pair(pos, dimension)] = job
    }

    fun stopQueueAt(pos: BlockPos, dimension: Int) {
        SoundPlayer.stopPlayingAt(pos, dimension)
        jobs.remove(Pair(pos, dimension))
    }

    @SubscribeEvent
    fun unloadChunk(evt: ChunkEvent.Unload) {
        jobs
                .map { it.key }
                .filter { t -> evt.world.getChunkFromBlockCoords(t.first) == evt.chunk }
                .forEach {
                    stopQueueAt(it.first, it.second)
                }
    }

    @SubscribeEvent
    fun unloadWorld(evt: WorldEvent.Unload) {
        jobs
                .map { it.key }
                .filter { it.second == evt.world.provider.dimension }
                .forEach {
                    stopQueueAt(it.first, it.second)
                }
    }
}
