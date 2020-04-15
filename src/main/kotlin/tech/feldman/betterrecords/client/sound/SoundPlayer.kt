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

import net.minecraft.client.Minecraft
import net.minecraft.client.audio.SoundHandler
import net.minecraft.client.audio.SoundManager
import net.minecraft.util.math.BlockPos
import net.minecraftforge.client.event.sound.SoundLoadEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.ReflectionHelper
import net.minecraftforge.fml.relauncher.Side
import org.apache.commons.io.FilenameUtils
import org.lwjgl.BufferUtils
import org.lwjgl.openal.AL10
import paulscode.sound.SoundSystem
import tech.feldman.betterrecords.ID
import tech.feldman.betterrecords.ModConfig
import tech.feldman.betterrecords.api.record.IRecordAmplitude
import tech.feldman.betterrecords.api.sound.Sound
import tech.feldman.betterrecords.api.wire.IRecordWireHome
import tech.feldman.betterrecords.client.handler.ClientRenderHandler
import tech.feldman.betterrecords.util.downloadFile
import tech.feldman.betterrecords.util.getGainForPlayerPosition
import tech.feldman.betterrecords.util.getIngameVolume
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.ByteBuffer
import java.util.*
import javax.sound.sampled.*
import kotlin.collections.HashMap
import kotlin.math.absoluteValue

@Mod.EventBusSubscriber(modid = ID, value = [Side.CLIENT])
object SoundPlayer {

    private val downloadFolder = File(Minecraft.getMinecraft().mcDataDir, "betterrecords/cache")

    private val playingSounds = HashMap<Pair<BlockPos, Int>, Sound>()

    fun playSound(pos: BlockPos, dimension: Int, sound: Sound) {
        tech.feldman.betterrecords.BetterRecords.logger.info("Playing sound at $pos in Dimension $dimension")

        ClientRenderHandler.nowDownloading = sound.name
        ClientRenderHandler.showDownloading = true

        val targetFile = File(downloadFolder, FilenameUtils.getName(sound.url).replace(Regex("[^a-zA-Z0-9_\\.]"), "_"))

        downloadFile(URL(sound.url), targetFile,
                update = { curr, total ->
                    ClientRenderHandler.downloadPercent = curr / total
                },
                success = {
                    ClientRenderHandler.showDownloading = false
                    playingSounds[Pair(pos, dimension)] = sound
                    ClientRenderHandler.showPlayingWithTimeout(sound.name)
                    playFile(targetFile, pos, dimension)
                },
                failure = {
                    ClientRenderHandler.showDownloading = false
                }
        )
    }

    fun playSoundFromStream(pos: BlockPos, dimension: Int, sound: Sound) {
        tech.feldman.betterrecords.BetterRecords.logger.info("Playing sound from stream at $pos in $dimension")

        val urlConn = tech.feldman.betterrecords.client.sound.IcyURLConnection(URL(if (sound.url.startsWith("http")) sound.url else "http://${sound.url}")).apply {
            instanceFollowRedirects = true
        }

        urlConn.connect()

        playingSounds[Pair(pos, dimension)] = sound

        ClientRenderHandler.showPlayingWithTimeout(sound.name)
        playStream(urlConn.inputStream, pos, dimension)
    }

    fun isSoundPlayingAt(pos: BlockPos, dimension: Int) =
            playingSounds.containsKey(Pair(pos, dimension))

    fun getSoundPlayingAt(pos: BlockPos, dimension: Int) =
            playingSounds[Pair(pos, dimension)]

    fun stopPlayingAt(pos: BlockPos, dimension: Int) {
        tech.feldman.betterrecords.BetterRecords.logger.info("Stopping sound at $pos in Dimension $dimension")
        playingSounds.remove(Pair(pos, dimension))
    }

    private fun playFile(file: File, pos: BlockPos, dimension: Int) {
        play(AudioSystem.getAudioInputStream(file), pos, dimension)
    }

    private fun playStream(stream: InputStream, pos: BlockPos, dimension: Int) {
        play(AudioSystem.getAudioInputStream(stream), pos, dimension)
    }

    private fun play(ain: AudioInputStream, pos: BlockPos, dimension: Int) {
        val baseFormat = ain.format
        val decodedFormat = AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.sampleRate, 16,
                baseFormat.channels,
                baseFormat.channels * 2,
                baseFormat.sampleRate,
                false
        )

        val din = AudioSystem.getAudioInputStream(decodedFormat, ain)
        rawPlay(decodedFormat, din, pos, dimension)
        ain.close()
    }

    private fun checkALError(name: String) {
        val err = AL10.alGetError()
        if (err != 0) {
            println("Found error $err ($name)")
        }
    }

    private var soundManager: net.minecraft.client.audio.SoundManager? = null

    @SubscribeEvent
    fun onSoundLoadEvent(event: SoundLoadEvent) {
        println("Sound manager loaded.")
        soundManager = event.manager
    }

    private fun rawPlay(targetFormat: AudioFormat, din: AudioInputStream, pos: BlockPos, dimension: Int) {
        val sndSystemField = ReflectionHelper.findField(SoundManager::class.java, "sndSystem", "field_148620_e")
        val soundSystem = sndSystemField.get(soundManager!!) as SoundSystem

        val streamName = "radio538"


        soundSystem.rawDataStream(targetFormat, true, streamName, pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat(), 0, 16.0f)

        soundSystem.play(streamName)
        val buffer = ByteArray(4096)

        var bytes = din.read(buffer)
        while (bytes >= 0 && isSoundPlayingAt(pos, dimension)) {

            soundSystem.feedRawAudioData(streamName, buffer)

            updateLights(buffer, pos, dimension)
            bytes = din.read(buffer)
        }

        stopPlayingAt(pos, dimension)

        din.close()
    }

    private fun rawPlay3(targetFormat: AudioFormat, din: AudioInputStream, pos: BlockPos, dimension: Int) {
        // https://stackoverflow.com/a/5518320
        // https://github.com/kcat/openal-soft/wiki/Programmer%27s-Guide#queuing-buffers-on-a-source
        val source = AL10.alGenSources()
        AL10.alSourcePlay(source)
        checkALError("Source play")
        AL10.alSource3f(source, AL10.AL_POSITION, pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat())
        checkALError("Set position")
        val minGain = AL10.alGetSourcef(source, AL10.AL_MIN_GAIN)
        val maxGain = AL10.alGetSourcef(source, AL10.AL_MAX_GAIN)

        println("min gain: ${minGain} - max gain: ${maxGain}")

        // Effects go on source
        // https://github.com/LWJGL/lwjgl/blob/master/src/java/org/lwjgl/test/openal/EFX10Test.java#L308

        // NOTE: We can play (for each speaker) a sound. However, we'd need to enqueue the data to multiple sources
        // at multiple locations with multiple gains...

        val queueMaxSize = 16
        val knownBuffers =  BufferUtils.createIntBuffer(queueMaxSize)

        AL10.alGenBuffers(knownBuffers)
        checkALError("Knownbuffer")

        val unusedBuffers = ArrayDeque<Int>() as Queue<Int>
        (0 until queueMaxSize).forEach { x -> unusedBuffers.add(knownBuffers[x]) }

        val buffer = ByteArray(4096)

        val bufferFormat =
            if (false) 0
            else if (targetFormat.channels == 1 && targetFormat.sampleSizeInBits == 8) AL10.AL_FORMAT_MONO8
            else if (targetFormat.channels == 1 && targetFormat.sampleSizeInBits == 16) AL10.AL_FORMAT_MONO16
            else if (targetFormat.channels == 2 && targetFormat.sampleSizeInBits == 8) AL10.AL_FORMAT_STEREO8
            else if (targetFormat.channels == 2 && targetFormat.sampleSizeInBits == 16) AL10.AL_FORMAT_STEREO16
            else throw Exception("what")

        var bytes = din.read(buffer)
        while (bytes >= 0 && isSoundPlayingAt(pos, dimension)) {
            // Get all buffers that are played, and put them back on the queue
            val processed = AL10.alGetSourcei(source, AL10.AL_BUFFERS_PROCESSED)
            checkALError("Buffers processed")
            if (processed > 0) {
                val ib = BufferUtils.createIntBuffer(processed)
                AL10.alSourceUnqueueBuffers(source)
                checkALError("Unqueue buffers")
                (0 until processed).forEach { x -> unusedBuffers.add(ib[x]) }
            }

            if (unusedBuffers.size == 0) {
                println("!!! Unused buffers == 0")
                // We need to wait...
                continue
            }

            val currentVolume = getIngameVolume()
            val currentGain = getGainForPlayerPosition(pos)

            val usableBuffer = unusedBuffers.poll()

            AL10.alSourcef(source, AL10.AL_GAIN,  1.0F)
            checkALError("Changing gain")

            val buf = ByteBuffer.allocateDirect(bytes).put(buffer, 0, bytes)

            AL10.alBufferData(
                usableBuffer.toInt(),
                bufferFormat,
                buf,
                (targetFormat.sampleRate).toInt()
            )
            checkALError("Setting albufferdata")


            updateLights(buffer, pos, dimension)
            bytes = din.read(buffer)
        }

        stopPlayingAt(pos, dimension)

        din.close()
        AL10.alSourceStop(source)
        AL10.alDeleteBuffers(knownBuffers)
    }

    private fun rawPlay2(targetFormat: AudioFormat, din: AudioInputStream, pos: BlockPos, dimension: Int) {
        val line = getLine(targetFormat)
        val defaultMixer = AudioSystem.getMixer(null)
        val outputLine = defaultMixer.runCatching { getLine(Port.Info.LINE_OUT)}.getOrNull()

        val gainControl = line.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
        val volumeControl = outputLine?.getControl(FloatControl.Type.VOLUME) as FloatControl?


        val reverbControl = line.runCatching { getControl(EnumControl.Type.REVERB) as EnumControl}
        //reverbControl.value = ReverbType("Cave", 2250, -2.0f, 41300, -1.4f, 10300)

        reverbControl.onSuccess { reverbCtrl ->
            reverbCtrl.values.map { x -> x as ReverbType }.forEach {
                println("Got ReverbType ${it.name}")
                if (it.decayTime > 2000) {
                    // Cavern?
                    println("Trying to set cavern type")
                    reverbCtrl.value = it
                }
            }
        }

        line.start()

        val buffer = ByteArray(2048)
        var bytes = din.read(buffer)

        while (bytes >= 0 && isSoundPlayingAt(pos, dimension)) {
            volumeControl?.value = getIngameVolume()
            gainControl.value = getGainForPlayerPosition(pos).coerceIn(gainControl.minimum, gainControl.maximum)
            line.write(buffer, 0, bytes)
            updateLights(buffer, pos, dimension)
            bytes = din.read(buffer)
        }

        stopPlayingAt(pos, dimension)

        line.drain()
        line.stop()
        line.close()
        din.close()
    }

    private fun getLine(audioFormat: AudioFormat): SourceDataLine {
        val info = DataLine.Info(SourceDataLine::class.java, audioFormat)
        val res = AudioSystem.getLine(info)
        res.open()
        return res as SourceDataLine
    }

    private fun updateLights(buffer: ByteArray, pos: BlockPos, dimension: Int) {
        if (Minecraft.getMinecraft().world?.provider?.dimension != dimension) {
            return
        }

        var unscaledTreble = -1F
        var unscaledBass = -1F

        val te = Minecraft.getMinecraft().world.getTileEntity(pos)

        (te as? IRecordWireHome)?.let {
            te.addTreble(getUnscaledWaveform(buffer, true, false))
            te.addBass(getUnscaledWaveform(buffer, false, false))

            for (connection in te.connections) {
                val connectedTe = Minecraft.getMinecraft().world.getTileEntity(BlockPos(connection.x2, connection.y2, connection.z2))

                (connectedTe as? IRecordAmplitude)?.let {
                    if (unscaledTreble == -1F || unscaledBass == 11F) {
                        unscaledTreble = getUnscaledWaveform(buffer, true, true)
                        unscaledBass = getUnscaledWaveform(buffer, false, true)
                    }

                    connectedTe.treble = unscaledTreble
                    connectedTe.bass = unscaledBass
                }
            }
        }
    }

    private fun getUnscaledWaveform(buffer: ByteArray, high: Boolean, control: Boolean): Float {
        val toReturn = ByteArray(buffer.size / 2)

        var avg = 0.0F

        for ((index, audioByte) in ((if (high) 0 else 1) until (buffer.size) step 2).withIndex()) {
            toReturn[index] = buffer[audioByte]
            avg += toReturn[index]
        }

        avg /= toReturn.size

        if (control) {
            if (avg < 0F) {
                avg = avg.absoluteValue
            }

            if (avg > 20F) {
                return if (ModConfig.client.flashMode < 3) {
                    1F
                } else {
                    2F
                }
            }
        }
        return avg
    }
}
