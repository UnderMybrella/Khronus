package dev.brella.khronus

import dev.brella.khronus.watchdogs.*
import net.minecraft.world.World

enum class WatchdogType(msg: String?, val watchdog: KhronusWatchdog) {
    NONE("Flavouring removed", VanillaWorldProcessing),
    NONE_WITH_DEBUG("⚠️ Spicy Yet Flavourless ⚠️", VanillaWorldProcessingWithTiming),
//    WATCH("\uD83D\uDC41️ Engaged \uD83D\uDC41️", KhronusSynchronousWorldProcessing),
    WATCH_WITH_DEBUG("\uD83D\uDC41️ ⚠️ Engaged ⚠️ \uD83D\uDC41️", KhronusSynchronousWorldProcessingWithTiming)

    ;

    constructor(func: KhronusWatchdog): this(null, func)

    val msg: String = msg ?: name
}