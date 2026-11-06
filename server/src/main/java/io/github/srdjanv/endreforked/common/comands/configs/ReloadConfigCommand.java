package io.github.srdjanv.endreforked.common.comands.configs;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.command.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import io.github.srdjanv.endreforked.common.configs.JsonConfigs;
import io.github.srdjanv.endreforked.common.configs.base.ReloadableServerSideConfig;

public class ReloadConfigCommand extends CommandBase {

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.reloadconfig.usage";
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
            config.get().sync();
        } else throw new CommandException("NotFound");
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 3;
    }
}
