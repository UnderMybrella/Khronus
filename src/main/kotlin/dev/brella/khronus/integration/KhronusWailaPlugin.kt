package dev.brella.khronus.integration

import dev.brella.khronus.examples.block.CounterBlock
import mcp.mobius.waila.api.IRegistrar
import mcp.mobius.waila.api.IWailaPlugin
import mcp.mobius.waila.api.TooltipPosition
import mcp.mobius.waila.api.WailaPlugin
import net.minecraft.block.Block

@WailaPlugin
class KhronusWailaPlugin: IWailaPlugin {

    override fun register(registrar: IRegistrar) {
        registrar.registerComponentProvider(CounterDataProvider, TooltipPosition.BODY, CounterBlock::class.java)
        registrar.registerBlockDataProvider(CounterDataProvider, CounterBlock::class.java)

        registrar.registerComponentProvider(WatchdogProvider, TooltipPosition.BODY, Block::class.java)
        registrar.registerComponentProvider(WatchdogProvider,TooltipPosition.TAIL,  Block::class.java)
        registrar.registerBlockDataProvider(WatchdogProvider, Block::class.java)

//        registrar.registerBodyProvider(nqDataProvider, ITileEntityProvider::class.java)
//        registrar.registerNBTProvider(nqDataProvider, ITileEntityProvider::class.java)
    }
}