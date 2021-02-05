package dev.brella.khronus.commands

import net.minecraft.command.CommandBase
import net.minecraft.command.ICommand
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos

class BasicChildCommandWithCompletion(
    private val name: String,
    val tabCompletions: (
        server: MinecraftServer,
        sender: ICommandSender,
        args: Array<String>,
        targetPos: BlockPos?
    ) -> MutableList<String>,
    val executeFunc: (server: MinecraftServer, sender: ICommandSender, args: Array<String>) -> Unit
) : CommandBase(), IChildCommand {
    override var parent: ICommand? = null

    override fun getName(): String = name
    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) =
        executeFunc(server, sender, args)
    override fun getTabCompletions(
        server: MinecraftServer,
        sender: ICommandSender,
        args: Array<String>,
        targetPos: BlockPos?
    ): MutableList<String> = tabCompletions(server, sender, args, targetPos)
}