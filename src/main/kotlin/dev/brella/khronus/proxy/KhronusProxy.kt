package dev.brella.khronus.proxy

import dev.brella.khronus.Khronus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.minecraft.world.World

abstract class KhronusProxy {
    inline val logger get() = Khronus.logger

    private var serverUpdateJob: Job? = null

    abstract suspend fun <T> runIn(world: World, block: suspend CoroutineScope.() -> T): T
}