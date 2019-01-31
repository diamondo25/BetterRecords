package tech.feldman.betterrecords.api.wire

interface IWireSoundSink : IWireSoundDevice {

    var treble: Float
    var bass: Float

    fun updateSound(treble: Float, bass: Float) {
        this.treble = treble
        this.bass = bass
    }
}
