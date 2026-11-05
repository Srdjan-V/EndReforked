package io.github.srdjanv.endreforked.common.comands.dev;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.server.command.CommandTreeBase;

public class DevCommands extends CommandTreeBase {

    public DevCommands() {
        // addSubcommand(new DisableRegionLoading());
    }

    @Override
    public String getName() {
        return "dev";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
