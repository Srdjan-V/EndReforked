package io.github.srdjanv.endreforked.api.entropy.world;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyChunk;
import io.github.srdjanv.endreforked.api.entropy.EntropyRadius;
import io.github.srdjanv.endreforked.api.util.DimPos;

public final class EntropyChunkReader {

    private ChunkEntropyView entropyView;

    public EntropyChunkReader(
                              Supplier<DimPos> centerPosSup,
                              Function<DimPos, Optional<EntropyChunk>> resolver) {
        this(centerPosSup, resolver, EntropyRadius.ONE);
    }

    public EntropyChunkReader(
                              Supplier<DimPos> centerPosSup,
                              Function<DimPos, Optional<EntropyChunk>> resolver,
                              EntropyRadius radius) {
        entropyView = new ChunkEntropyView(centerPosSup, resolver, radius);
    }

    public void updateRadius(EntropyRadius radius) {
        if (getRadius() != radius)
            entropyView = new ChunkEntropyView(
                    entropyView.getCenterPosSup(),
                    entropyView.getResolver(),
                    radius);
    }

    public EntropyRadius getRadius() {
        return entropyView.getEntropyRadius().orElseThrow(() -> new IllegalStateException("Entropy radius is null"));
    }

    @NotNull
    public ChunkEntropyView getEntropyView() {
        entropyView.buildView();
        return entropyView;
    }

    @Nullable
    public EntropyChunk getCenterEntropy() {
        entropyView.buildView();
        return entropyView.getCenterEntropy();
    }

    public static EntropyChunkReader ofEntity(Entity entity) {
        return ofEntity(entity, EntropyRadius.ONE);
    }

    public static EntropyChunkReader ofEntity(Entity entity, EntropyRadius radius) {
        return new EntropyChunkReader(() -> {
            var world = entity.world;
            var tilePos = entity.getPosition();
            if (world == null || tilePos == null) return null;
            return new DimPos(world.provider.getDimension(), tilePos);
        }, EntropyWorldHandler::getEntropyChunkWorld, radius);
    }

    public static EntropyChunkReader ofTileEntity(TileEntity tileEntity) {
        return ofTileEntity(tileEntity, EntropyRadius.ONE);
    }

    public static EntropyChunkReader ofTileEntity(TileEntity tileEntity, EntropyRadius radius) {
        return new EntropyChunkReader(() -> {
            var world = tileEntity.getWorld();
            var tilePos = tileEntity.getPos();
            if (world == null || tilePos == null) return null;
            return new DimPos(world.provider.getDimension(), tilePos);
        }, EntropyWorldHandler::getEntropyChunkWorld, radius);
    }
}
