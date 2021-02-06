package dev.brella.khronus

import com.mojang.brigadier.Command
import com.mojang.brigadier.Command.SINGLE_SUCCESS
import dev.brella.khronus.api.KhronusApi
import dev.brella.khronus.networking.KhronusNetworking
import dev.brella.khronus.networking.KhronusUpdateTickLengthsMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.minecraft.command.Commands
import net.minecraft.command.Commands.argument
import net.minecraft.command.Commands.literal
import net.minecraft.command.arguments.DimensionArgument
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.StringTextComponent
import net.minecraft.world.World
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.network.PacketDistributor
import net.minecraftforge.server.command.EnumArgument
import java.util.*

@Mod.EventBusSubscriber(modid = Khronus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object KhronusEvents {
    private val worldJobs: MutableMap<World, Job> = WeakHashMap()

    @SubscribeEvent
    fun commands(event: RegisterCommandsEvent) {
        event.dispatcher.register(
            literal("khronus")
                .then(literal("watchdog")
                    .then(Commands.argument("watchdog", EnumArgument.enumArgument(WatchdogType::class.java))
                        .executes { context ->
                            val type = context.getArgument("watchdog", WatchdogType::class.java)

                            TickDog.watchdog = type.watchdog
                            context.source.sendFeedback(StringTextComponent(type.msg), true)

                            return@executes Command.SINGLE_SUCCESS
                        }
                    ).executes { context ->
                        context.source.sendFeedback(StringTextComponent("Watchdog: ${TickDog.watchdog}"), true)
                        return@executes Command.SINGLE_SUCCESS
                    }
                )
                .then(literal("tps")
                    .then(argument("world", DimensionArgument.getDimension())
                        .executes { context ->
                            if (TickDog.worldTickRates.isEmpty()) {
                                context.source.sendErrorMessage(StringTextComponent("No tick rate data available"))
                                return@executes 0
                            } else {
                                val dimensionArgument = context.getArgument("world", ResourceLocation::class.java)

                                val feedback = buildString {
                                    TickDog.worldTickRates.forEach { (world, rate) ->
                                        if (world.dimensionKey.func_240901_a_() != dimensionArgument)
                                            return@forEach

                                        append('[')
                                        if (world.isRemote) append("Client") else append("Server")
                                        append("] '")
                                        append(world.dimensionKey.func_240901_a_())
                                        append("' (")
                                        append(rate.ticksPerSecond)
                                        append("/20): ")

                                        append(rate.minimumTickLength.toTwoDecimalPlaces())
                                        append(" ≤ ")
                                        append(rate.averageTickLength.toTwoDecimalPlaces())
                                        append(" ≤ ")
                                        append(rate.maximumTickLength.toTwoDecimalPlaces())

                                        append(" {")
                                        append(rate.firstTickLength.toTwoDecimalPlaces())
                                        append(",..,")
                                        append(rate.lastTickLength.toTwoDecimalPlaces())
                                        append("}\n")
                                    }
                                }

                                if (feedback.isNotBlank()) {
                                    context.source.sendFeedback(StringTextComponent(feedback), true)

                                    return@executes SINGLE_SUCCESS
                                } else {
                                    context.source.sendErrorMessage(StringTextComponent("No tick rate data for $dimensionArgument available"))

                                    return@executes 0
                                }
                            }
                        }
                    ).executes { context ->
                        if (TickDog.worldTickRates.isEmpty()) {
                            context.source.sendErrorMessage(StringTextComponent("No tick rate data available"))
                            return@executes 0
                        } else {
                            context.source.sendFeedback(StringTextComponent(buildString {
                                TickDog.worldTickRates.forEach { (world, rate) ->
                                    append('[')
                                    if (world.isRemote) append("Client") else append("Server")
                                    append("] '")
                                    append(world.dimensionKey.func_240901_a_())
                                    append("' (")
                                    append(rate.ticksPerSecond)
                                    append("/20): ")

                                    append(rate.minimumTickLength.toTwoDecimalPlaces())
                                    append(" ≤ ")
                                    append(rate.averageTickLength.toTwoDecimalPlaces())
                                    append(" ≤ ")
                                    append(rate.maximumTickLength.toTwoDecimalPlaces())

                                    append(" {")
                                    append(rate.firstTickLength.toTwoDecimalPlaces())
                                    append(",..,")
                                    append(rate.lastTickLength.toTwoDecimalPlaces())
                                    append("}\n")
                                }
                            }), true)

                            return@executes SINGLE_SUCCESS
                        }
                    }
                )
        )

//        event.registerPipeCommand("khronus") {
//            addCommand("watchdog", WatchdogType.values().map(WatchdogType::name)) { _, sender, args ->
//                if (args.isEmpty()) {
//                    return@addCommand sender.sendMessage(,
//                        Util.DUMMY_UUID)
//                }
//
//                val arg = args.last()
//                val watchdogTypes =
//                    WatchdogType.values().firstOrNull { type -> type.name.equals(arg, true) }?.let(::listOf)
//                        ?: WatchdogType.values().filter { type ->
//                            type.name.startsWith(arg, true)
//                        }
//
//                when (watchdogTypes.size) {
//                    0 -> sender.sendMessage(StringTextComponent("Unknown watchdog type $arg"), Util.DUMMY_UUID)
//                    1 -> {
//                        val type = watchdogTypes[0]
//                        TickDog.watchdog = type.watchdog
//                        sender.sendMessage(StringTextComponent(type.msg), Util.DUMMY_UUID)
//                    }
//                    else -> sender.sendMessage(StringTextComponent("Ambiguous watchdog type $arg"), Util.DUMMY_UUID)
//                }
//            }
//            addCommand("maxTickTime") { _, sender, args ->
//                if (args.isEmpty()) return@addCommand sender.sendMessage(StringTextComponent("Max Tick Time: ${TickDog.defaultMaxTickTime} μs, or ${((TickDog.defaultMaxTickTime / 976.0) * 100).roundToInt() / 100.0} ms"),
//                    Util.DUMMY_UUID)
//
//                val asInt = args.last().toIntOrNull()
//                    ?: return@addCommand sender.sendMessage(StringTextComponent("${args.last()} is not a number!"),
//                        Util.DUMMY_UUID)
//
//                TickDog.defaultMaxTickTime = asInt
//
//                sender.sendMessage(StringTextComponent("Set max tick time for any tile entity to $asInt μs, or ${((asInt / 976.0) * 100).roundToInt() / 100.0} ms"),
//                    Util.DUMMY_UUID)
//            }

//        }
    }

    @SubscribeEvent
    fun loadWorld(event: WorldEvent.Load) {
        val world = event.world as? World ?: return

        if (world.isRemote) return

        worldJobs.remove(world)?.cancel()
        worldJobs[world] = TickDog.launch {
            while (isActive) {
                delay(5_000)

                if (world.players.isNotEmpty()) {
                    val updateTicks = KhronusUpdateTickLengthsMessage(
                        world.dimensionTypeKey.registryName,
                        KhronusApi.getTickLength(world).mapKeys { (te) -> te.pos.toLong() }
                    )

                    KhronusNetworking.INSTANCE.send(PacketDistributor.DIMENSION.with { world.dimensionKey },
                        updateTicks)
                }
            }
        }
    }

    @SubscribeEvent
    fun unloadWorld(event: WorldEvent.Unload) {
        worldJobs.remove(event.world)?.cancel()
    }
}