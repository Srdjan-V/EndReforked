package io.github.srdjanv.endreforked.api.base.crafting.processors;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import io.github.srdjanv.endreforked.api.base.crafting.EntropyRecipe;
import io.github.srdjanv.endreforked.api.base.crafting.TileStatus;
import io.github.srdjanv.endreforked.api.base.crafting.recipe.timed.TimedRecipe;
import io.github.srdjanv.endreforked.api.entropy.world.EntropyChunkReader;

public class ProcessorValidator<IN, OUT, R extends TimedRecipe<IN, OUT>> {

    public static final Predicate<ItemStack> ITEM_INPUT_EXIST_VALIDATOR = stack -> !stack.isEmpty();
    public static final Predicate<FluidStack> FLUID_INPUT_EXIST_VALIDATOR = Objects::nonNull;

    protected final BaseRecipeProcessor<IN, OUT, R> processor;

    protected final Supplier<IN> inputSupplier;
    protected final Predicate<IN> inputExistValidator;
    protected final Supplier<EntropyChunkReader> readerSupplier;
    protected final Consumer<TileStatus> statusUpdater;

    public static <R extends TimedRecipe<ItemStack, ItemStack>> ProcessorValidator<ItemStack, ItemStack, R> item2ItemOf(
                                                                                                                        BaseRecipeProcessor<ItemStack, ItemStack, R> processor,
                                                                                                                        Consumer<TileStatus> statusUpdater,
                                                                                                                        Supplier<EntropyChunkReader> readerSupplier,
                                                                                                                        Supplier<ItemStack> inputSupplier) {
        return new ProcessorValidator<>(
                processor,
                statusUpdater,
                readerSupplier,
                inputSupplier,
                ITEM_INPUT_EXIST_VALIDATOR);
    }

    public static <
            R extends TimedRecipe<FluidStack, FluidStack>> ProcessorValidator<FluidStack, FluidStack, R> fluid2FluidOf(
                                                                                                                       BaseRecipeProcessor<FluidStack, FluidStack, R> processor,
                                                                                                                       Consumer<TileStatus> statusUpdater,
                                                                                                                       Supplier<EntropyChunkReader> readerSupplier,
                                                                                                                       Supplier<FluidStack> inputSupplier) {
        return new ProcessorValidator<>(
                processor,
                statusUpdater,
                readerSupplier,
                inputSupplier,
                FLUID_INPUT_EXIST_VALIDATOR);
    }

    public ProcessorValidator(
                              BaseRecipeProcessor<IN, OUT, R> processor,
                              Consumer<TileStatus> statusUpdater,
                              Supplier<EntropyChunkReader> readerSupplier,
                              Supplier<IN> inputSupplier,
                              Predicate<IN> inputExistValidator) {
        this.processor = processor;
        this.inputSupplier = inputSupplier;
        this.inputExistValidator = inputExistValidator;
        this.readerSupplier = readerSupplier;
        this.statusUpdater = statusUpdater;
    }

    public boolean prepare() {
        IN input = inputSupplier.get();
        if (processor.validateRecipe(input)) {
            var rec = processor.getRecipe();
            if (rec instanceof EntropyRecipe entropyRecipe) {
                var data = Objects.requireNonNull(readerSupplier.get()).getEntropyView();
                if (data.getCurrentEntropy() >= entropyRecipe.getEntropyCost()) {
                    statusUpdater.accept(TileStatus.RUNNING);
                    return true;
                } else statusUpdater.accept(TileStatus.NOT_ENOUGH_ENTROPY);
            } else return true;
        } else if (!inputExistValidator.test(input)) {
            statusUpdater.accept(TileStatus.IDLE);
        } else statusUpdater.accept(TileStatus.INVALID);

        return false;
    }
}
