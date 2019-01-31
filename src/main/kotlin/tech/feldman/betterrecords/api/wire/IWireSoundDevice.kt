package tech.feldman.betterrecords.api.wire

interface IWireSoundDevice {

    val children: MutableList<IWireSoundDevice>

    val soundBonus: Float

    fun addChild(device: IWireSoundDevice) {
        children.add(device)
    }

    fun updateChildren(treble: Float, bass: Float) {
        for (child in children) {
            if (child is IWireSoundSink) {
                child.updateSound(treble, bass)
            }

            child.updateChildren(treble, bass)
        }
    }
}
