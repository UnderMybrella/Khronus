package dev.brella.khronus

import com.mojang.brigadier.Command.SINGLE_SUCCESS
import dev.brella.khronus.Khronus.MOD_ID
import dev.brella.khronus.api.KhronusApi
import dev.brella.khronus.examples.block.KhronusBlocks
import dev.brella.khronus.examples.item.KhronusItems
import dev.brella.khronus.networking.KhronusNetworking
import dev.brella.khronus.networking.KhronusUpdateTickLengthsMessage
import dev.brella.khronus.proxy.ClientProxy
import kotlinx.coroutines.*
import net.minecraft.command.Commands.argument
import net.minecraft.command.Commands.literal
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.util.text.StringTextComponent
import net.minecraft.world.World
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.fml.network.PacketDistributor
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

    public val logger = LogManager.getLogger(MOD_ID)

    public val clientProxy: ClientProxy? = if (FMLEnvironment.dist == Dist.CLIENT) ClientProxy() else null

    public val itemGroup: ItemGroup = object : ItemGroup(MOD_ID) {
        override fun createIcon(): ItemStack = ItemStack(KhronusBlocks.lavaFurnace.get())
    }

    override val coroutineContext: CoroutineContext =
        SupervisorJob() + Executors.newSingleThreadExecutor { task -> Thread(task, MOD_NAME).apply { isDaemon = true } }
            .asCoroutineDispatcher()

    @SubscribeEvent
    fun commonSetup(event: FMLCommonSetupEvent) {
//        config = Configuration(event.suggestedConfigurationFile)

        KhronusNetworking.registerMessages()

//        Minecraft.getMinecraft().connection

//        KhronusVanilla.register()

//        GameRegistry.registerTileEntity(TileEntityLavaFurnace::class.java, ResourceLocation(MOD_ID, "lava_furnace"))
//        GameRegistry.registerTileEntity(TileEntityWarpDrive::class.java, ResourceLocation(MOD_ID, "warp_drive"))
//        GameRegistry.registerTileEntity(TileEntityCounter::class.java, ResourceLocation(MOD_ID, "counter"))
    }

    init {
        KhronusBlocks.register()
        KhronusItems.register()
    }
}