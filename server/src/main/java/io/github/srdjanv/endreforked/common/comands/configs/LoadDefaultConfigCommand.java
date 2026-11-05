package io.github.srdjanv.endreforked.common.comands.configs;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import io.github.srdjanv.endreforked.common.configs.JsonConfigs;
import io.github.srdjanv.endreforked.common.configs.base.ReloadableServerSideConfig;

public class LoadDefaultConfigCommand extends CommandBase {

    @Override
    public String getName() {
        return "load-default";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "null";
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          @Nullable BlockPos targetPos) {
        return JsonConfigs.getReloadableTabCompletions(args[0]);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1 || args[0].length() <= 1) throw new WrongUsageException("commands.reloadconfig.usage");

        Optional<? extends ReloadableServerSideConfig<?>> config = JsonConfigs.getReloadableConfig(args[0]);
        if (config.isPresent()) {
            config.get().loadDefaults();
        } else throw new CommandException("NotFound");
    }
}
