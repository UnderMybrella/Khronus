package dev.brella.khronus.proxy

import dev.brella.khronus.Khronus
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
import net.minecraft.util.math.shapes.VoxelShapes
import net.minecraft.util.math.vector.Matrix4f
import net.minecraft.world.World
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import java.util.concurrent.ConcurrentHashMap


@Mod.EventBusSubscriber(modid = Khronus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object ClientProxy : KhronusProxy() {
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

            val viewPos = Minecraft.getInstance().gameRenderer.activeRenderInfo.projectedView

            laggiest.forEach { (te, time) ->
                val iblockstate = world.getBlockState(blockpos.setPos(te))

                val matrix4f: Matrix4f = event.matrixStack.last.matrix

                if (iblockstate.material !== Material.AIR && player.world.worldBorder.contains(blockpos)) {
//                    val d3: Double =
//                        player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks.toDouble()
//                    val d4: Double =
//                        player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks.toDouble()
//                    val d5: Double =
//                        player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks.toDouble()

                    val bufferIn = buffer.getBuffer(RenderType.getLines())



                    WorldRenderer.drawBoundingBox(event.matrixStack, bufferIn, iblockstate.getShape(player.world, blockpos, context).boundingBox.offset(blockpos).offset(viewPos.inverse()), 1.0f, 0.0f, 0.0f, 1.0f)
//                        .forEachEdge { xMin: Double, yMin: Double, zMin: Double, xMax: Double, yMax: Double, zMax: Double ->
//                            bufferIn.pos(matrix4f,
//                                (xMin + blockpos.x - viewPos.x).toFloat(),
//                                (yMin + blockpos.y - viewPos.y).toFloat(),
//                                (zMin + blockpos.z - viewPos.z).toFloat()
//                            ).color(1.0f, 0.0f, 0.0f, 1.0f).endVertex()
//
//                            bufferIn.pos(matrix4f,
//                                (xMax + blockpos.x - viewPos.x).toFloat(),
//                                (yMax + blockpos.y - viewPos.y).toFloat(),
//                                (zMax + blockpos.z - viewPos.z).toFloat()
//                            ).color(1.0f, 0.0f, 0.0f, 1.0f).endVertex()
//                        }
                }
            }

            buffer.finish()
        }
    }
}