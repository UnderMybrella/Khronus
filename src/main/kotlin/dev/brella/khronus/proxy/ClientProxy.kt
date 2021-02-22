package dev.brella.khronus.proxy

import dev.brella.khronus.Khronus.MOD_ID
import dev.brella.khronus.ThreadListenerDispatcher
import dev.brella.khronus.TickDog
import dev.brella.khronus.api.EnumLagTuning
import dev.brella.khronus.api.KhronusApi
import dev.brella.khronus.data.ChunkTickData
import dev.brella.khronus.hsvToRgb
import dev.brella.khronus.networking.KhronusNetworking
import dev.brella.khronus.networking.KhronusRequestTicksMessage
import kotlinx.coroutines.*
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.Item
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack
import net.minecraft.network.EnumConnectionState
import net.minecraft.network.NetworkManager
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

class ClientProxy : KhronusProxy() {
    private val minecraftDispatcher: CoroutineDispatcher by lazy { ThreadListenerDispatcher(Minecraft.getMinecraft()) }
    public var tickTimesDimension: Int? = null
    public val tickTimes: MutableList<ChunkTickData> = CopyOnWriteArrayList()

    var lagThreshold: Double = 0.0
    val lagList: MutableList<ChunkTickData> = CopyOnWriteArrayList()
    val blockPos = BlockPos.MutableBlockPos()

    val boundingBoxList: MutableList<AxisAlignedBB> = ArrayList()

    var requestTicksJob: Job? = null

    val waiting = AtomicBoolean(false)

    override suspend fun <T> runIn(world: World, block: suspend CoroutineScope.() -> T): T =
        withContext(minecraftDispatcher, block)

    override fun registerItemRenderer(item: Item, meta: Int, id: String) {
        ModelLoader.setCustomModelResourceLocation(item, meta, ModelResourceLocation("$MOD_ID:$id", "inventory"))
    }

    @SubscribeEvent
    fun clientConnected(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
        println("Connected to ${event.handler}")

        requestTicksJob?.cancel()
        requestTicksJob = TickDog.launch(start = CoroutineStart.LAZY) {
            val player = Minecraft.getMinecraft().player
            val manager = event.manager
            val handler = event.handler
            var connectionState: EnumConnectionState
            do {
                delay(1_000)
                connectionState = manager.channel().attr(NetworkManager.PROTOCOL_ATTRIBUTE_KEY).get()
                println("Connection State: $connectionState")
            } while (isActive && connectionState != EnumConnectionState.PLAY)

            waiting.set(false)
            var timeout = 0

            while (isActive && manager.isChannelOpen) {
                timeout = 0

                while (waiting.get() && timeout++ < 50) delay(100)

                KhronusNetworking.INSTANCE.sendToServer(KhronusRequestTicksMessage())
                waiting.set(true)

                delay(1_000)
            }
        }
    }

    @SubscribeEvent
    fun clientDisconnected(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        requestTicksJob?.cancel()
    }

    @SubscribeEvent
    fun worldLoad(event: WorldEvent.Load) {
        println("World Loaded")
        requestTicksJob?.start()
    }

    private inline fun EntityPlayer.getViewSlots(): List<ItemStack> =
        EntityEquipmentSlot.values().mapNotNull { slot ->
            val stack = getItemStackFromSlot(slot)
            when {
                stack.isEmpty -> null
                slot.slotType == EntityEquipmentSlot.Type.ARMOR -> stack
                stack.item is ItemArmor && stack.item.isValidArmor(stack, slot, this) -> null
                else -> stack
            }
        }

    @SubscribeEvent
    fun postRender(event: RenderWorldLastEvent) {
        val player = Minecraft.getMinecraft().player

        val lagometerState = EntityEquipmentSlot.values().fold(EnumLagTuning.OFF) { state, slot ->
            val stack = player.getItemStackFromSlot(slot)
            if (!stack.isEmpty && stack.hasCapability(KhronusApi.LAG_TESTING, null))
                stack.getCapability(KhronusApi.LAG_TESTING, null)?.getLagTuningForStack(stack, player, slot)
                    ?.takeIf { it >= state } ?: state
            else state
        }

        if (lagometerState > EnumLagTuning.OFF) {
            GlStateManager.enableBlend()
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO)
            GlStateManager.glLineWidth(2.0f)
            GlStateManager.disableTexture2D()
            val prevDepth = GlStateManager.depthState.depthTest.currentState
            if (lagometerState >= EnumLagTuning.XRAY) GlStateManager.disableDepth()

            val world = player.world

            val now = (System.currentTimeMillis() / 1_00) % 360

            var nowRed = 1f
            var nowGreen = 1f
            var nowBlue = 1f

            hsvToRgb(now.toFloat(), .47f, .89f) { r, g, b ->
                nowRed = r
                nowGreen = g
                nowBlue = b
            }

            boundingBoxList.clear()
//            val dispatcher = Minecraft.getMinecraft().blockRendererDispatcher
            lagList.forEach { data ->
                val te = world.getTileEntity(blockPos.setPos(data.posX, data.posY, data.posZ)) ?: return@forEach

                if (data !is ChunkTickData.WithKey || TileEntity.getKey(te.javaClass) != data.key) return@forEach

                val iblockstate = world.getBlockState(blockPos)

                val d3: Double =
                    player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks.toDouble()
                val d4: Double =
                    player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks.toDouble()
                val d5: Double =
                    player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks.toDouble()

                boundingBoxList.add(iblockstate.getSelectedBoundingBox(player.world, blockPos)
                    .grow(0.02)
                    .offset(-d3, -d4, -d5))
            }

            loop@ while (true) {
                for (i in boundingBoxList.indices) {
                    val bb = boundingBoxList[i]
                    val intersectIndex = boundingBoxList.indexOfFirst { it != bb && it.intersects(bb) }
                    if (intersectIndex == -1) continue
                    val bb2 = boundingBoxList[intersectIndex]

                    boundingBoxList.remove(bb)
                    boundingBoxList.remove(bb2)

                    boundingBoxList.add(bb.union(bb2))

                    continue@loop
                }

                break
            }

            val tessellator = Tessellator.getInstance()
            val bufferBuilder = tessellator.buffer
            bufferBuilder.begin(3, DefaultVertexFormats.POSITION_COLOR)

            val red = nowRed
            val green = nowGreen
            val blue = nowBlue
            val alpha = 0.8f

            boundingBoxList.forEach { box ->
                val minX = box.minX
                val minY = box.minY
                val minZ = box.minZ
                val maxX = box.maxX
                val maxY = box.maxY
                val maxZ = box.maxZ

                bufferBuilder.pos(minX, minY, minZ).color(red, green, blue, 0.0f).endVertex()
                bufferBuilder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex()
                bufferBuilder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex()
                bufferBuilder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex()
                bufferBuilder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex()
                bufferBuilder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex()
                bufferBuilder.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex()
                bufferBuilder.pos(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex()
                bufferBuilder.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex()
                bufferBuilder.pos(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex()
                bufferBuilder.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex()
                bufferBuilder.pos(minX, maxY, maxZ).color(red, green, blue, 0.0f).endVertex()
                bufferBuilder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex()
                bufferBuilder.pos(maxX, maxY, maxZ).color(red, green, blue, 0.0f).endVertex()
                bufferBuilder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex()
                bufferBuilder.pos(maxX, maxY, minZ).color(red, green, blue, 0.0f).endVertex()
                bufferBuilder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex()
                bufferBuilder.pos(maxX, minY, minZ).color(red, green, blue, 0.0f).endVertex()

//                RenderGlobal.drawSelectionBoundingBox(box, nowRed, nowGreen, nowBlue, 0.8f)
            }
            tessellator.draw()

            GlStateManager.depthState.depthTest.setState(prevDepth)
            GlStateManager.enableTexture2D()
            GlStateManager.disableBlend()
        }
    }
}