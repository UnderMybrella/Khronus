package dev.brella.khronus

import net.minecraft.world.WorldServer
import java.lang.ref.WeakReference

class TickingWorldServer<T: WorldServer>(world: WeakReference<T>): WorldServerDelegate<T>(world) {
    constructor(world: T): this(WeakReference(world))

    override fun updateEntities() {
        super.updateEntities()
    }
}