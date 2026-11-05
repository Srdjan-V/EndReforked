package io.github.srdjanv.endreforked.api.fluids.base;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import io.github.srdjanv.endreforked.api.util.EntityMatchStrategy;

public class EntityMather<E extends Entity> implements Predicate<E> {

    protected final EntityBuilder<E> entityBuilder;
    protected final EntityMatchStrategy<E> matchStrategy;
    protected final String translation;

    public EntityMather(EntityBuilder<E> entityBuilder, EntityMatchStrategy<E> matchStrategy, String translation) {
        this.entityBuilder = entityBuilder;
        this.matchStrategy = matchStrategy;
        this.translation = translation;
    }

    public EntityBuilder<E> getEntityBuilder() {
        return entityBuilder;
    }

    public interface EntityBuilder<E extends Entity> extends Function<World, E> {

        @Override
        E apply(@Nullable World world);
    }

    @Override
    public boolean test(E e) {
        return matchStrategy.match(entityBuilder.apply(e.getEntityWorld()), e);
    }

    public static <E extends Entity> EntityMatherBuilder<E> builder() {
        return new EntityMatherBuilder<>();
    }

    public static final class EntityMatherBuilder<E extends Entity> {

        private EntityBuilder<E> entityBuilder;
        private EntityMatchStrategy<E> matchStrategy;
        private String translation;

        private EntityMatherBuilder() {}

        public EntityMatherBuilder<E> withEntityBuilder(EntityBuilder<E> entityBuilder) {
            this.entityBuilder = entityBuilder;
            return this;
        }

        public EntityMatherBuilder<E> withMatchStrategy(EntityMatchStrategy<E> matchStrategy) {
            this.matchStrategy = matchStrategy;
            return this;
        }

        public EntityMatherBuilder<E> withTranslation(String translation) {
            this.translation = translation;
            return this;
        }

        public EntityMather<E> build() {
            return new EntityMather<>(
                    Objects.requireNonNull(entityBuilder),
                    Objects.requireNonNull(matchStrategy),
                    translation);
        }
    }
}
