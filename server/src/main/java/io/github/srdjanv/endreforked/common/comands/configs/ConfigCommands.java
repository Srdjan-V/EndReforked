package io.github.srdjanv.endreforked.common.comands.configs;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.server.command.CommandTreeBase;

public class ConfigCommands extends CommandTreeBase {

    public ConfigCommands() {
        super.addSubcommand(new SaveDefaultConfigCommand());
    }

    @Override
    public String getName() {
        return "configs";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "";
    }
}
