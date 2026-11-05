package io.github.srdjanv.endreforked.common.configs;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.srdjanv.endreforked.common.configs.base.BaseServerSideConfig;
import io.github.srdjanv.endreforked.common.configs.base.ReloadableServerSideConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class JsonConfigs {

    private static final List<BaseServerSideConfig<?>> configs = new ObjectArrayList<>();

    public static void addConfig(BaseServerSideConfig<?> config) {
        configs.add(config);
    }

    public static List<? extends BaseServerSideConfig<?>> getConfigs() {
        return configs;
    }

    public static List<String> getTabCompletions(String name) {
        return getMatchingConfigs(config -> config.getConfigName().contains(name))
                .map(BaseServerSideConfig::getConfigName)
                .collect(Collectors.toList());
    }

    public static Optional<? extends BaseServerSideConfig<?>> getConfig(String name) {
        return getMatchingConfigs(config -> config.getConfigName().contains(name))
                .findFirst();
    }

    public static List<? extends BaseServerSideConfig<?>> getConfigs(String name) {
        return getMatchingConfigs(config -> config.getConfigName().contains(name))
                .collect(Collectors.toList());
    }

    public static List<String> getReloadableTabCompletions(String name) {
        return getMatchingReloadableConfigs(config -> config.getConfigName().contains(name))
                .map(BaseServerSideConfig::getConfigName)
                .collect(Collectors.toList());
    }

    public static Optional<? extends ReloadableServerSideConfig<?>> getReloadableConfig(String name) {
        return getMatchingReloadableConfigs(config -> config.getConfigName().contains(name))
                .findFirst();
    }

    public static List<? extends ReloadableServerSideConfig<?>> getReloadableConfigs(String name) {
        return getMatchingReloadableConfigs(config -> config.getConfigName().contains(name))
                .collect(Collectors.toList());
    }

    public static Stream<? extends BaseServerSideConfig<?>> getMatchingConfigs(Predicate<BaseServerSideConfig<?>> predicate) {
        return configs.stream().filter(predicate);
    }

    public static Stream<? extends ReloadableServerSideConfig<?>> getMatchingReloadableConfigs(Predicate<ReloadableServerSideConfig<?>> predicate) {
        return configs.stream()
                .filter(config -> config instanceof ReloadableServerSideConfig)
                .map(config -> (ReloadableServerSideConfig<?>) config)
                .filter(predicate);
    }
}
