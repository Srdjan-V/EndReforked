package io.github.srdjanv.endreforked.common.comands.configs;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import io.github.srdjanv.endreforked.common.configs.base.BaseServerSideConfig;
import io.github.srdjanv.endreforked.common.configs.content.DisabledContentConfig;
import io.github.srdjanv.endreforked.common.configs.mobs.MobConfig;
import io.github.srdjanv.endreforked.common.configs.worldgen.GenConfigs;

public class SaveDefaultConfigCommand extends CommandBase {

    @Override
    public String getName() {
        return "SaveDefaultConfigCommand";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "null";
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          @Nullable BlockPos targetPos) {
        List<BaseServerSideConfig<?>> data = Arrays.stream(GenConfigs.values()).map(GenConfigs::getConfig)
                .collect(Collectors.toList());
        data.add(MobConfig.getInstance());
        data.add(DisabledContentConfig.getInstance());
        return data.stream()
                .map(BaseServerSideConfig::getConfigName)
                .filter(configName -> configName.contains(args[0]))
                .collect(Collectors.toList());
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1 || args[0].length() <= 1) throw new WrongUsageException("commands.reloadconfig.usage");

        List<BaseServerSideConfig<?>> data = Arrays.stream(GenConfigs.values()).map(GenConfigs::getConfig)
                .collect(Collectors.toList());
        data.add(MobConfig.getInstance());
        data.add(DisabledContentConfig.getInstance());
        for (BaseServerSideConfig<?> config : data) {
            if (config.getConfigName().equalsIgnoreCase(args[0])) {
                config.saveDefaults();
                return;
            }
        }
        throw new CommandException("NotFound");
    }
}
