package dev.brella.khronus.overrides.projecte

import dev.brella.khronus.api.IKhronusTickable
import dev.brella.khronus.toFieldHandle
import dev.brella.khronus.toMethodHandle
import moze_intel.projecte.api.item.IPedestalItem
import moze_intel.projecte.gameObjs.tiles.DMPedestalTile
import net.minecraft.item.Item

//class KhronusTickablePedestal(val base: DMPedestalTile) : IKhronusTickable<DMPedestalTile> {
//    companion object {
//        val particleCooldownField = DMPedestalTile::class.java.getDeclaredField("particleCooldown")
////            .toFieldHandle<DMPedestalTile, Int>()
//            .apply { isAccessible = true }
//
//        val spawnParticlesMethod = DMPedestalTile::class.java.getDeclaredMethod("spawnParticles")
//            .toMethodHandle()
////            .apply { isAccessible = true }
//
//        inline var DMPedestalTile.particleCooldown: Int
//            get() = particleCooldownField.getInt(this)
//            set(value) {
//                particleCooldownField.setInt(this, value)
//            }
//        inline fun DMPedestalTile.spawnParticles() = spawnParticlesMethod.invoke(this)
//    }
//
//    override fun update(ticks: Int, bonusTicks: Int) {
//        base.centeredX = base.pos.x.toDouble() + 0.5
//        base.centeredY = base.pos.y.toDouble() + 0.5
//        base.centeredZ = base.pos.z.toDouble() + 0.5
//        if (base.active) {
//            val stack = base.inventory.getStackInSlot(0)
//            if (!stack.isEmpty) {
//                KhronusProjectE.KHRONUS_PEDESTAL_CAPABILITY?.let { capability ->
//                    if (stack.hasCapability(capability, null)) {
//                        stack.getCapability(capability, null)?.updateInPedestal(base.world, base.pos, ticks, bonusTicks)
//                    } else {
//                        null
//                    }
//                } ?: run {
//                    val item: Item = stack.item
//                    if (item is IPedestalItem) {
//                        (item as IPedestalItem).updateInPedestal(base.world, base.pos)
//                    }
//                }
//
//                spawnParticlesMethod.invokeExact(base)
//
//                if (base.particleCooldown <= 0) {
//                    base.spawnParticles()
//                    base.particleCooldown = 10
//                } else {
//                    --base.particleCooldown
//                }
//            }
//        } else {
//            base.active = false
//        }
//    }
//
//    override fun getSource(): DMPedestalTile = base
//}