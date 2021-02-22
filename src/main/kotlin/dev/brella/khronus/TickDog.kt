package dev.brella.khronus

import dev.brella.khronus.api.TemporalBounds
import dev.brella.khronus.watchdogs.KhronusWatchdog
import dev.brella.khronus.watchdogs.VanillaWorldProcessing
import kotlinx.coroutines.*
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.collections.HashMap
import kotlin.coroutines.CoroutineContext
import kotlin.math.log2
import kotlin.math.pow

object TickDog : CoroutineScope {
    data class TickRate(
        val ticksPerSecond: Int,
        val averageTickLength: Double,
        val minimumTickLength: Double,
        val maximumTickLength: Double,
        val firstTickLength: Double,
        val lastTickLength: Double
    )

    inline val logger get() = Khronus.logger

    val dispatcher =
        Executors.newSingleThreadExecutor { task -> Thread(task, "Khronus-TickDog").apply { isDaemon = true } }
            .asCoroutineDispatcher()

    override val coroutineContext: CoroutineContext = SupervisorJob() + dispatcher

    val ticks = WeakHashMap<World, Int>()
    val worldTickLengths = WeakHashMap<World, MutableList<Pair<Long, Double>>>()
    val worldTickRates = WeakHashMap<World, TickRate>()
//    val tickDelays = ConcurrentHashMap<Int, TemporalBounds>()
//    val tickCheckUps = ConcurrentHashMap<Int, Long>()
//    val tickLength = ConcurrentHashMap<Int, Long>()

    val maxTickTime: MutableMap<Class<*>, Int> = HashMap()

    var worldTickRateJob: Job? = null
    var blockTickRateJob: Job? = null

    var defaultMaxTickTime: Int = 400
        set(value) {
            tickRateExponent = log2(value.toDouble()).toInt()
            field = value
        }
    var tickRateExponent: Int = log2(defaultMaxTickTime.toDouble()).toInt()
        private set

    var checkupLimit: Int = 32
        private set(value) {
            var v = value
            v--
            v = v or v shr 1
            v = v or v shr 2
            v = v or v shr 4
            v = v or v shr 8
            v = v or v shr 16
            v++

            field = v
        }

    var checkupLimitExponent: Int = 5
        set(value) {
            checkupLimit = 2.0.pow(value).toInt()
            field = value
        }


    var dynamicMaxTickTime = 100_000

    //    @JvmField
    @JvmStatic
    public var watchdog: KhronusWatchdog = VanillaWorldProcessing
        set(new) {
            Khronus.proxy.onWatchdogSwitch(field, new)
            field = new
        }

    inline fun Long.abs(): Long {
        val mask = this shr 31
        return (this + mask) xor mask
    }

    inline fun Int.atLeastNonZero(): Int =
        this - ((this - 1) and (this - 1) shr 31)

    inline fun microsecondsToTickDelay(microseconds: Long): Int =
        ((microseconds shr tickRateExponent).toInt().coerceIn(0, 127) + 1)

    init {
        worldTickRateJob = launch {
            while (isActive) {
                delay(1_000)

                try {
                    val now = System.currentTimeMillis()
                    val minTime = now - 1_000
                    val timeRange = (minTime + 1) until now

                    worldTickLengths
                        .entries
                        .forEach { (world, list) ->
                            val lastSecond = list.filter { (start) -> start in timeRange }
                                .map(Pair<Long, Double>::second)

                            if (lastSecond.isEmpty()) worldTickRates.remove(world)
                            else {
                                var min: Double = Double.MAX_VALUE
                                var max: Double = Double.MIN_VALUE
                                var sum: Double = 0.0

                                lastSecond.forEach {
                                    if (it < min) min = it
                                    if (it > max) max = it
                                    sum += it
                                }
                                worldTickRates[world] =
                                    TickRate(lastSecond.size,
                                        sum / lastSecond.size.toDouble(),
                                        min,
                                        max,
                                        lastSecond.first(),
                                        lastSecond.last())

                                list.removeAll { (start) -> start < now }
                            }
                        }
                } catch (th: Throwable) {
                    th.printStackTrace()
                }
            }
        }
    }
}