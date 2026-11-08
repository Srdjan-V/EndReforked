package io.github.srdjanv.endreforked.common.comands.dev

import net.minecraft.command.ICommandSender
import net.minecraftforge.server.command.CommandTreeBase

class DevCommands : CommandTreeBase() {
    override fun getName() = "dev"
    override fun getUsage(sender: ICommandSender) = ""
    override fun getRequiredPermissionLevel() = 0
}
