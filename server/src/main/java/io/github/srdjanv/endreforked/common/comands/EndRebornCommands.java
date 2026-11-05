package io.github.srdjanv.endreforked.common.comands;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.server.command.CommandTreeBase;

import io.github.srdjanv.endreforked.Tags;
import io.github.srdjanv.endreforked.common.comands.configs.ConfigCommands;
import io.github.srdjanv.endreforked.common.comands.dev.DevCommands;

public class EndRebornCommands extends CommandTreeBase {

    public EndRebornCommands() {
        super.addSubcommand(new ConfigCommands());

        if (FMLLaunchHandler.isDeobfuscatedEnvironment())
            super.addSubcommand(new DevCommands());
    }

    @Override
    public String getName() {
        return Tags.MODID.toLowerCase();
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.endreforked.usage";
    }

    @Override
    public void addSubcommand(ICommand command) {
        throw new UnsupportedOperationException("Create your own command.");
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }
}
