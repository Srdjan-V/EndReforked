package io.github.srdjanv.endreforked.common.configs.base;

import java.util.Objects;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ResourceLocationWrapper {

    @Contract("!null -> new")
    public static @NotNull ResourceLocationWrapper of(String resourceName) {
        return of(new ResourceLocation(resourceName));
    }

    @Contract("!null,!null -> new")
    public static @NotNull ResourceLocationWrapper of(String namespaceIn, String pathIn) {
        return of(new ResourceLocation(namespaceIn, pathIn));
    }

    public static @NotNull ResourceLocationWrapper of(@NotNull ResourceLocation location) {
        return new ResourceLocationWrapper(location.getNamespace(), location.getPath());
    }

    @Contract(" -> new")
    public @NotNull ResourceLocation get() {
        return new ResourceLocation(namespace, path);
    }

    public final String namespace, path;

    private ResourceLocationWrapper(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceLocationWrapper that = (ResourceLocationWrapper) o;
        return Objects.equals(namespace, that.namespace) && Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, path);
    }
}
