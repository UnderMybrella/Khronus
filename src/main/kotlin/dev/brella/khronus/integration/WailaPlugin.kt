//package dev.brella.khronus.integration
//
//import dev.brella.khronus.examples.block.CounterBlock
//import mcp.mobius.waila.api.IWailaPlugin
//import mcp.mobius.waila.api.IWailaRegistrar
//import net.minecraft.block.Block
//
//@WailaPlugin
//class WailaPlugin: IWailaPlugin {
//    val counterDataProvider = CounterDataProvider()
//
//    override fun register(registrar: IWailaRegistrar) {
//        registrar.registerBodyProvider(counterDataProvider, CounterBlock::class.java)
//        registrar.registerNBTProvider(counterDataProvider, CounterBlock::class.java)
//
//        registrar.registerBodyProvider(WatchdogProvider, Block::class.java)
//        registrar.registerTailProvider(WatchdogProvider, Block::class.java)
//        registrar.registerNBTProvider(WatchdogProvider, Block::class.java)
//
////        registrar.registerBodyProvider(nqDataProvider, ITileEntityProvider::class.java)
////        registrar.registerNBTProvider(nqDataProvider, ITileEntityProvider::class.java)
//    }
//}