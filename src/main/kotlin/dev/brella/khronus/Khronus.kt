package dev.brella.khronus

import dev.brella.khronus.Khronus.MOD_ID
import dev.brella.khronus.examples.block.KhronusBlocks
import dev.brella.khronus.examples.entity.TileEntityCounter
import dev.brella.khronus.examples.entity.TileEntityLavaFurnace
import dev.brella.khronus.examples.entity.TileEntityWarpDrive
import dev.brella.khronus.examples.item.KhronusItems
import dev.brella.khronus.networking.KhronusNetworking
import dev.brella.khronus.overrides.vanilla.KhronusVanilla
import dev.brella.khronus.proxy.KhronusProxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import net.minecraft.block.Block
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.TextComponentString
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.GameRegistry
import org.apache.logging.log4j.Logger
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlin.math.roundToInt

@Mod(
    modid = MOD_ID,
    name = Khronus.MOD_NAME,
    version = Khronus.VERSION,
    modLanguageAdapter = "net.shadowfacts.forgelin.KotlinAdapter",
    dependencies = "before:extrautils2;"
)
@Mod.EventBusSubscriber(modid = MOD_ID)
object Khronus : CoroutineScope {
    public const val MOD_ID = "khronus"
    public const val MOD_NAME = "Khronus"
    public const val VERSION = "1.0.0a"

    public lateinit var logger: Logger

    public val creativeTab: CreativeTabs = object : CreativeTabs(MOD_ID) {
        override fun createIcon(): ItemStack = ItemStack(KhronusBlocks.litLavaFurnace)
    }

    @SidedProxy(
        serverSide = "dev.brella.khronus.proxy.ServerProxy",
        clientSide = "dev.brella.khronus.proxy.ClientProxy"
    )
    public lateinit var proxy: KhronusProxy

    override val coroutineContext: CoroutineContext =
        SupervisorJob() + Executors.newSingleThreadExecutor { task -> Thread(task, MOD_NAME).apply { isDaemon = true } }
            .asCoroutineDispatcher()

    public lateinit var config: Configuration

    @Mod.EventHandler
    fun preInitialisation(event: FMLPreInitializationEvent) {
        logger = event.modLog
        config = Configuration(event.suggestedConfigurationFile)

        KhronusNetworking.registerMessages()

//        Minecraft.getMinecraft().connection

        MinecraftForge.EVENT_BUS.register(proxy)
        MinecraftForge.EVENT_BUS.register(KhronusVanilla)

        GameRegistry.registerTileEntity(TileEntityLavaFurnace::class.java, ResourceLocation(MOD_ID, "lava_furnace"))
        GameRegistry.registerTileEntity(TileEntityWarpDrive::class.java, ResourceLocation(MOD_ID, "warp_drive"))
        GameRegistry.registerTileEntity(TileEntityCounter::class.java, ResourceLocation(MOD_ID, "counter"))
    }

    @Mod.EventHandler
    fun serverStarting(event: FMLServerStartingEvent) {
        proxy.serverStarting(event)

        event.registerPipeCommand("khronus") {
            addCommand("watchdog", WatchdogType.values().map(WatchdogType::name)) { _, sender, args ->
                if (args.isEmpty()) {
                    return@addCommand sender.sendMessage(TextComponentString("Watchdog: ${TickDog.watchdog}"))
                }

                val arg = args.last()
                val watchdogTypes =
                    WatchdogType.values().firstOrNull { type -> type.name.equals(arg, true) }?.let(::listOf)
                        ?: WatchdogType.values().filter { type ->
                            type.name.startsWith(arg, true)
                        }

                when (watchdogTypes.size) {
                    0 -> sender.sendMessage(TextComponentString("Unknown watchdog type $arg"))
                    1 -> {
                        val type = watchdogTypes[0]
                        TickDog.watchdog = type.watchdog
                        sender.sendMessage(TextComponentString(type.msg))
                    }
                    else -> sender.sendMessage(TextComponentString("Ambiguous watchdog type $arg"))
                }
            }
            addCommand("maxTickTime") { _, sender, args ->
                if (args.isEmpty()) return@addCommand sender.sendMessage(TextComponentString("Max Tick Time: ${TickDog.defaultMaxTickTime} μs, or ${((TickDog.defaultMaxTickTime / 976.0) * 100).roundToInt() / 100.0} ms"))

                val asInt = args.last().toIntOrNull()
                    ?: return@addCommand sender.sendMessage(TextComponentString("${args.last()} is not a number!"))

                TickDog.defaultMaxTickTime = asInt

                sender.sendMessage(TextComponentString("Set max tick time for any tile entity to $asInt μs, or ${((asInt / 976.0) * 100).roundToInt() / 100.0} ms"))
            }
            addCommand("tps") { _, sender, _ ->
                if (TickDog.worldTickRates.isEmpty()) {
                    sender.sendMessage(TextComponentString("No tick rate data available"))
                } else {
                    sender.sendMessage(TextComponentString(buildString {
                        TickDog.worldTickRates.forEach { (world, rate) ->
                            append('[')
                            if (world.isRemote) append("Client") else append("Server")
                            append("] '")
                            append(world.provider.dimensionType.name)
                            append("' (Dim ")
                            append(world.provider.dimension)
                            append(", ${rate.ticksPerSecond}/20): ")

                            append(rate.minimumTickLength.toTwoDecimalPlaces())
                            append(" ≤ ")
                            append(rate.averageTickLength.toTwoDecimalPlaces())
                            append(" ≤ ")
                            append(rate.maximumTickLength.toTwoDecimalPlaces())

                            append(" {")
                            append(rate.firstTickLength.toTwoDecimalPlaces())
                            append(",..,")
                            append(rate.lastTickLength.toTwoDecimalPlaces())
                            append("}\n")
                        }
                    }))
                }
            }
        }
    }

    @SubscribeEvent
    fun registerBlocks(event: RegistryEvent.Register<Block>) {
        KhronusBlocks.register(event.registry)
    }

    @SubscribeEvent
    fun registerItems(event: RegistryEvent.Register<Item>) {
        KhronusBlocks.registerItemBlocks(event.registry)
        KhronusItems.register(event.registry)
//        SnugItems.register(event.registry)
    }

    @SubscribeEvent
    fun registerModels(event: ModelRegistryEvent) {
        KhronusBlocks.registerModels()
        KhronusItems.registerModels()
//        SnugItems.registerModels()
    }

    @SubscribeEvent
    fun registerRecipes(event: RegistryEvent.Register<IRecipe>) {
//        event.registry.register(CompressedRecipe)
    }

    @SubscribeEvent
    fun atlasStitching(event: TextureStitchEvent.Pre) {
//        DESTRUCTION_STAGES = Array(10) { i -> event.map.registerSprite(ResourceLocation("blocks/destroy_stage_$i")) }
//        COMPRESSION_STAGES = Array(1) { i -> event.map.registerSprite(ResourceLocation(MOD_ID, "compression/compression_$i"))}
    }
}