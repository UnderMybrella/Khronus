package dev.brella.khronus.overrides.vanilla

import net.minecraft.entity.item.EntityItem
import net.minecraft.item.ItemStack
import net.minecraft.network.datasync.DataParameter
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.network.datasync.EntityDataManager
import net.minecraft.world.World
import java.lang.ref.WeakReference
import kotlin.math.min

class EntityKhronusItem : EntityItem {
    companion object {
        private val SIZE = EntityDataManager.createKey(EntityKhronusItem::class.java, DataSerializers.VARINT)
    }

    private var stackReference: WeakReference<ItemStack>? = null
    var existingStack: ItemStack?
        get() = stackReference?.get()
        set(value) {
            stackReference = value?.let(::WeakReference)
        }

    constructor(world: World): super(world)
    constructor(world: World, x: Double, y: Double, z: Double): super(world, x, y, z)
    constructor(world: World, x: Double, y: Double, z: Double, item: ItemStack): super(world, x, y, z, item) {
        itemSize = item.count
    }

    override fun setDead() {
        if (health <= 0 || this.age >= lifespan) {
            super.setDead()
        } else {
            val item = existingStack
            if (item == null) {
                super.setDead()
            } else {
                itemSize -= item.count
                if (itemSize <= 0) super.setDead()
            }
        }
    }

    var itemSize: Int
        get() = getDataManager().get<Int>(SIZE)
        set(value) {
            getDataManager().set(SIZE, value)
            getDataManager().setDirty(SIZE)
        }

    override fun getItem(): ItemStack {
        return existingStack ?: let {
            val item = super.getItem()
            item.count = minOf(itemSize, item.item.getItemStackLimit(item))
            existingStack = item
            item
        }
    }

    override fun setItem(stack: ItemStack) {
        val ourStack = super.getItem()

        val expectedSize = minOf(itemSize, item.item.getItemStackLimit(item))

        val stacksEqual = if (ourStack.item !== stack.item) {
            false
        } else if (ourStack.hasTagCompound() xor stack.hasTagCompound()) {
            false
        } else if (ourStack.hasTagCompound() && ourStack.tagCompound != stack.tagCompound) {
            false
        } else if (ourStack.item.hasSubtypes && ourStack.metadata != stack.metadata) {
            false
        } else stack.areCapsCompatible(ourStack)

        if (stack.isEmpty && expectedSize > 0) {
            itemSize -= expectedSize
        } else if (!stacksEqual) {
            super.setItem(stack)
            
            itemSize = stack.count
        } else if (stack.count < expectedSize) {
            itemSize -= (expectedSize - stack.count)
        } else if (stack.count > expectedSize) {
            itemSize += (stack.count - expectedSize)
        } else {
            println("Equal items? $stack, $ourStack")
        }
    }

    override fun entityInit() {
        super.entityInit()
        getDataManager().register(SIZE, 0)
    }
}