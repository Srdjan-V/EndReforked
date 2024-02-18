package io.github.srdjanv.endreforked.common.comands.configs;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import io.github.srdjanv.endreforked.common.configs.base.ReloadableServerSideConfig;
import io.github.srdjanv.endreforked.common.configs.worldgen.GenConfigs;

public class LoadDefaultConfigCommand extends CommandBase {

    @Override
    public String getName() {
        return "LoadDefaultConfigCommand";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "null";
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          @Nullable BlockPos targetPos) {
        return Arrays.stream(GenConfigs.values())
                .map(GenConfigs::getConfig)
                .map(ReloadableServerSideConfig::getConfigName)
                .filter(configName -> configName.contains(args[0]))
                .collect(Collectors.toList());
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1 || args[0].length() <= 1) throw new WrongUsageException("commands.reloadconfig.usage");

        var config = GenConfigs.get(args[0]);
        if (Objects.isNull(config)) throw new CommandException("NotFound");
        config.loadDefaults();
    }
}
