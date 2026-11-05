package io.github.srdjanv.endreforked.common.comands.configs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.command.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.common.configs.JsonConfigs;
import io.github.srdjanv.endreforked.common.configs.base.ReloadableServerSideConfig;

/**
 * /endreborn configs reload NAME. NAME is a reloadable config, or "machine_recipes" for the machine recipe registry,
 * which is read again from config/endreborn/machine_recipes/ and sent to all players.
 */
public class ReloadConfigCommand extends CommandBase {

    private static final String MACHINE_RECIPES = "machine_recipes";

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
        List<String> names = new ArrayList<>(JsonConfigs.getReloadableTabCompletions(args[0]));
        if (MACHINE_RECIPES.startsWith(args[0])) names.add(MACHINE_RECIPES);
        return names;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1 || args[0].length() <= 1) throw new WrongUsageException("commands.reloadconfig.usage");

        if (MACHINE_RECIPES.equals(args[0])) {
            List<String> errors = EndReforked.getProxy().registrySync.reloadAndSync();
            sender.sendMessage(new TextComponentString("Machine recipes reloaded, " + errors.size() + " problem(s)."));
            for (String error : errors) sender.sendMessage(new TextComponentString(error));
            return;
        }

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
