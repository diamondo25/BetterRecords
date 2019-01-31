package tech.feldman.betterrecords.api.wire

interface IWireSoundSource : IWireSoundDevice {

    fun getDevicesForDisplay(): HashMap<String, Int> {
        TODO()
    }

    fun calculatePlayRadius(): Float {
        TODO()
    }
}
