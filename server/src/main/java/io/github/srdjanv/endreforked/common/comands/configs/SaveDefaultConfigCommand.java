package io.github.srdjanv.endreforked.common.comands.configs;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import io.github.srdjanv.endreforked.common.configs.JsonConfigs;
import io.github.srdjanv.endreforked.common.configs.base.BaseServerSideConfig;

public class SaveDefaultConfigCommand extends CommandBase {

    @Override
    public String getName() {
        return "save-default";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "null";
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          @Nullable BlockPos targetPos) {
        return JsonConfigs.getTabCompletions(args[0]);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1 || args[0].length() <= 1) throw new WrongUsageException("commands.reloadconfig.usage");

        for (BaseServerSideConfig<?> config : JsonConfigs.getConfigs()) {
            if (config.getConfigName().equalsIgnoreCase(args[0])) {
                config.saveDefaults();
                return;
            }
        }
        throw new CommandException("NotFound");
    }
}
