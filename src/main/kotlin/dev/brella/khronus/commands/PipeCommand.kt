package dev.brella.khronus.commands

import net.minecraft.command.ICommand
import net.minecraftforge.server.command.CommandTreeBase

open class PipeCommand(private val name: String, override var parent: ICommand? = null) : CommandTreeBase(),
    IChildCommand {
    override fun getName(): String = name

    override fun addSubcommand(command: ICommand) {
        super.addSubcommand(command)

        if (command is IChildCommand) command.parent = this
    }
}