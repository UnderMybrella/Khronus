package dev.brella.khronus.integration.top

import dev.brella.khronus.Khronus
import dev.brella.khronus.logger
import mcjty.theoneprobe.api.ITheOneProbe
import net.minecraftforge.fml.InterModComms

object TheOneProbeKhronus: java.util.function.Function<ITheOneProbe, Void?> {
    inline fun setupTheOneProbe() {
        InterModComms.sendTo("theoneprobe", "getTheOneProbe") { TheOneProbeKhronus }
    }

    var probe: ITheOneProbe? = null
    override fun apply(input: ITheOneProbe): Void? {
        probe = input

        logger.info("/Initialising TheOneProbe support/")

        return null
    }
}