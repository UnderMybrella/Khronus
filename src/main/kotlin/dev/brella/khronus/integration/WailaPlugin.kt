package dev.brella.khronus.integration

import dev.brella.khronus.examples.block.BlockCounter
import mcp.mobius.waila.api.IWailaPlugin
import mcp.mobius.waila.api.IWailaRegistrar
import mcp.mobius.waila.api.WailaPlugin
import net.minecraft.block.Block
import net.minecraft.block.ITileEntityProvider

@WailaPlugin
class WailaPlugin: IWailaPlugin {
    val counterDataProvider = CounterDataProvider()

    override fun register(registrar: IWailaRegistrar) {
        registrar.registerBodyProvider(counterDataProvider, BlockCounter::class.java)
        registrar.registerNBTProvider(counterDataProvider, BlockCounter::class.java)

        registrar.registerBodyProvider(WatchdogProvider, Block::class.java)
        registrar.registerTailProvider(WatchdogProvider, Block::class.java)
        registrar.registerNBTProvider(WatchdogProvider, Block::class.java)

//        registrar.registerBodyProvider(nqDataProvider, ITileEntityProvider::class.java)
//        registrar.registerNBTProvider(nqDataProvider, ITileEntityProvider::class.java)
    }
}