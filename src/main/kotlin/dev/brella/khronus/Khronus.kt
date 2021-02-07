package dev.brella.khronus

import com.mojang.brigadier.Command
import com.mojang.brigadier.Command.SINGLE_SUCCESS
import dev.brella.khronus.Khronus.MOD_ID
import dev.brella.khronus.api.KhronusApi
import dev.brella.khronus.examples.block.KhronusBlocks
import dev.brella.khronus.examples.item.KhronusItems
import dev.brella.khronus.integration.top.TheOneProbeKhronus.setupTheOneProbe
import dev.brella.khronus.networking.KhronusNetworking
import dev.brella.khronus.networking.KhronusUpdateTickLengthsMessage
import dev.brella.khronus.proxy.ClientProxy
import kotlinx.coroutines.*
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.RenderTypeLookup
import net.minecraft.command.Commands
import net.minecraft.command.Commands.argument
import net.minecraft.command.Commands.literal
import net.minecraft.command.arguments.DimensionArgument
import net.minecraft.data.ItemModelProvider
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.StringTextComponent
import net.minecraft.world.World
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.model.generators.ModelFile
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.fml.network.PacketDistributor
import net.minecraftforge.server.command.EnumArgument
import net.minecraftforge.server.command.EnumArgument.enumArgument
import org.apache.logging.log4j.LogManager
import java.util.*
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

@Mod(MOD_ID)
@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
object Khronus : CoroutineScope {
    public const val MOD_ID = "khronus"
    public const val MOD_NAME = "Khronus"
    public const val VERSION = "1.0.0a"

    private val compatibilityModules: Map<String, () -> Unit> = mapOf(
        "theoneprobe" to { logger.info("TheOneProbe loaded, attempting compatibility---"); setupTheOneProbe() }
    )

    public val logger = LogManager.getLogger(MOD_ID)

    public val clientProxy: ClientProxy? = if (FMLEnvironment.dist == Dist.CLIENT) ClientProxy else null

    public val itemGroup: ItemGroup = object : ItemGroup(MOD_ID) {
        override fun createIcon(): ItemStack = ItemStack(KhronusBlocks.lavaFurnace.get())
    }

    override val coroutineContext: CoroutineContext =
        SupervisorJob() + Executors.newSingleThreadExecutor { task -> Thread(task, MOD_NAME).apply { isDaemon = true } }
            .asCoroutineDispatcher()

    @SubscribeEvent
    fun clientSetup(event: FMLClientSetupEvent) {
        event.enqueueWork {
            RenderTypeLookup.setRenderLayer(KhronusBlocks.counter(), RenderType.getCutout())
            RenderTypeLookup.setRenderLayer(KhronusBlocks.warpDrive(), RenderType.getCutout())
        }
    }

    @SubscribeEvent
    fun commonSetup(event: FMLCommonSetupEvent) {
//        config = Configuration(event.suggestedConfigurationFile)

//        Minecraft.getMinecraft().connection

//        KhronusVanilla.register()

//        GameRegistry.registerTileEntity(TileEntityLavaFurnace::class.java, ResourceLocation(MOD_ID, "lava_furnace"))
//        GameRegistry.registerTileEntity(TileEntityWarpDrive::class.java, ResourceLocation(MOD_ID, "warp_drive"))
//        GameRegistry.registerTileEntity(TileEntityCounter::class.java, ResourceLocation(MOD_ID, "counter"))
    }

    @SubscribeEvent
    fun gatherData(event: GatherDataEvent) {
        if (event.includeClient()) {
            event.generator.addProvider(object: net.minecraftforge.client.model.generators.ItemModelProvider(event.generator, MOD_ID, event.existingFileHelper) {
                override fun registerModels() {
                    withExistingParent("item/counter", "$MOD_ID:block/counter")
                    withExistingParent("item/warp_drive", "$MOD_ID:block/warp_drive")
                }
            })
        }
    }

    @SubscribeEvent
    fun imc(event: InterModEnqueueEvent) {
        compatibilityModules.forEach { (modid, action) -> if (ModList.get().isLoaded(modid)) action() }
    }

    init {
        KhronusBlocks.register()
        KhronusItems.register()

        KhronusNetworking.registerMessages()
    }
}