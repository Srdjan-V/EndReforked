package io.github.srdjanv.endreforked.common.comands

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.common.comands.dev.DevCommands
import net.minecraft.command.ICommand
import net.minecraft.command.ICommandSender
import net.minecraft.server.MinecraftServer
import net.minecraftforge.fml.relauncher.FMLLaunchHandler
import net.minecraftforge.server.command.CommandTreeBase

class EndRebornCommands : CommandTreeBase() {
    init {
        if (FMLLaunchHandler.isDeobfuscatedEnvironment()) super.addSubcommand(DevCommands())
    }

    override fun getName() = Tags.MODID.lowercase()
    override fun getUsage(sender: ICommandSender) = "commands.endreforked.usage"

    override fun addSubcommand(command: ICommand) = throw UnsupportedOperationException("Create your own command.")

    override fun getRequiredPermissionLevel() = 0
    override fun checkPermission(server: MinecraftServer, sender: ICommandSender) = true
}
