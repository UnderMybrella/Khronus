package dev.brella.khronus.proxy

import dev.brella.khronus.ThreadListenerDispatcher
import dev.brella.khronus.examples.item.ILagometer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import net.minecraft.block.material.Material
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.inventory.EquipmentSlotType
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.shapes.ISelectionContext
import net.minecraft.world.World
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import java.util.concurrent.ConcurrentHashMap

class ClientProxy : KhronusProxy() {
    private val minecraftDispatcher: CoroutineDispatcher by lazy { ThreadListenerDispatcher(Minecraft.getInstance()) }
    public var tickTimesDimension: ResourceLocation? = null
    public val tickTimes: MutableMap<Long, Long> = ConcurrentHashMap()

    override suspend fun <T> runIn(world: World, block: suspend CoroutineScope.() -> T): T =
        withContext(minecraftDispatcher, block)

    @SubscribeEvent
    fun postRender(event: RenderWorldLastEvent) {
        val player = Minecraft.getInstance().player ?: return
        if (player.getItemStackFromSlot(EquipmentSlotType.HEAD).item is ILagometer) {
            val lagThreshold = tickTimes.values.average()
            val laggiest = tickTimes.entries.sortedByDescending(Map.Entry<Long, Long>::value)
                .filter { (_, value) -> value > lagThreshold }
            val blockpos = BlockPos.Mutable()
            val world = player.world

            val buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().buffer)
            val context = ISelectionContext.forEntity(player)

            laggiest.forEach { (te, time) ->
                val iblockstate = world.getBlockState(blockpos.setPos(te))

                if (iblockstate.material !== Material.AIR && player.world.worldBorder.contains(blockpos)) {
                    val d3: Double =
                        player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks.toDouble()
                    val d4: Double =
                        player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks.toDouble()
                    val d5: Double =
                        player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks.toDouble()

                    WorldRenderer.drawBoundingBox(
                        event.matrixStack, buffer.getBuffer(RenderType.getLines()),
                        iblockstate.getShape(player.world, blockpos, context)
                            .boundingBox
                            .grow(0.0020000000949949026).offset(-d3, -d4, -d5),
                        1.0f, 0.0f, 0.0f, 0.8f)
                }
            }
        }
    }
}