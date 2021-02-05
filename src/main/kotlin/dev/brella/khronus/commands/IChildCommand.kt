package dev.brella.khronus.commands

import net.minecraft.command.ICommand
import net.minecraft.command.ICommandSender

interface IChildCommand: ICommand {
    var parent: ICommand?

    override fun getUsage(sender: ICommandSender): String = "${parent?.getUsage(sender)} $name"
}