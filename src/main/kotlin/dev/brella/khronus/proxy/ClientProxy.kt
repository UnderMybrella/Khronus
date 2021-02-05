package dev.brella.khronus.proxy

import dev.brella.khronus.Khronus.MOD_ID
import dev.brella.khronus.ThreadListenerDispatcher
import dev.brella.khronus.api.KhronusApi
import dev.brella.khronus.examples.item.ILagometer
import dev.brella.khronus.setFromLong
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.Item
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import java.util.concurrent.ConcurrentHashMap

class ClientProxy: KhronusProxy() {
    private val minecraftDispatcher: CoroutineDispatcher by lazy { ThreadListenerDispatcher(Minecraft.getMinecraft()) }
    public var tickTimesDimension: Int? = null
    public val tickTimes: MutableMap<Long, Long> = ConcurrentHashMap()

    override suspend fun <T> runIn(world: World, block: suspend CoroutineScope.() -> T): T =
            withContext(minecraftDispatcher, block)

    override fun registerItemRenderer(item: Item, meta: Int, id: String) {
        ModelLoader.setCustomModelResourceLocation(item, meta, ModelResourceLocation("$MOD_ID:$id", "inventory"))
    }

    @SubscribeEvent
    fun clientConnected(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
        println("Connected to ${event.handler}")
    }

    @SubscribeEvent
    fun postRender(event: RenderWorldLastEvent) {
        val player = Minecraft.getMinecraft().player
        if (player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).item is ILagometer) {
            GlStateManager.enableBlend()
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO)
            GlStateManager.glLineWidth(2.0f)
            GlStateManager.disableTexture2D()
            GlStateManager.depthMask(false)

            val lagThreshold = tickTimes.values.average()
            val laggiest = tickTimes.entries.sortedByDescending(Map.Entry<Long, Long>::value).filter { (_, value) -> value > lagThreshold }
            val blockpos = BlockPos.PooledMutableBlockPos.retain()
            val world = player.world

            try {
                laggiest.forEach { (te, time) ->
                    val iblockstate = world.getBlockState(blockpos.setFromLong(te))

                    if (iblockstate.material !== Material.AIR && player.world.worldBorder.contains(blockpos)) {
                        val d3: Double =
                            player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks.toDouble()
                        val d4: Double =
                            player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks.toDouble()
                        val d5: Double =
                            player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks.toDouble()

                        RenderGlobal.drawSelectionBoundingBox(iblockstate.getSelectedBoundingBox(player.world, blockpos)
                            .grow(0.0020000000949949026).offset(-d3, -d4, -d5), 1.0f, 0.0f, 0.0f, 0.8f)
                    }
                }
            } finally {
                blockpos.release()
            }

            GlStateManager.depthMask(true)
            GlStateManager.enableTexture2D()
            GlStateManager.disableBlend()
        }
    }
}