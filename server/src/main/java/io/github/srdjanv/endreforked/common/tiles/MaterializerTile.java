package io.github.srdjanv.endreforked.common.tiles;

import static net.minecraft.util.EnumParticleTypes.FLAME;
import static net.minecraft.util.EnumParticleTypes.PORTAL;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.keys.DynamicKey;
import com.cleanroommc.modularui.drawable.keys.LangKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.google.common.collect.Lists;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.api.base.crafting.TileStatus;
import io.github.srdjanv.endreforked.api.base.crafting.processors.BaseBiRecipeProcessor;
import io.github.srdjanv.endreforked.api.base.crafting.processors.ItemBiRecipeProcessor;
import io.github.srdjanv.endreforked.api.materializer.ItemCatalyst;
import io.github.srdjanv.endreforked.api.materializer.MaterializerHandler;
import io.github.srdjanv.endreforked.api.materializer.MaterializerRecipe;
import io.github.srdjanv.endreforked.api.materializer.WorldEvent;
import io.github.srdjanv.endreforked.common.ModBlocks;
import io.github.srdjanv.endreforked.common.blocks.BlockMaterializer;
import io.github.srdjanv.endreforked.common.tiles.base.BaseTileEntity;
import io.github.srdjanv.endreforked.common.widgets.BasicTextWidget;
import io.github.srdjanv.endreforked.common.widgets.BlockStateRendereWidget;

public class MaterializerTile extends BaseTileEntity implements ITickable, IGuiHolder<PosGuiData> {

    private final ItemStackHandler inputInventory = new InternalItemStackHandler(2) {

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return switch (slot) {
                case 1 -> {
                    if (biRecipeProcessor.validateGrouping(stack)) {
                        yield super.insertItem(slot, stack, simulate);
                    }
                    yield stack;
                }
                case 2 -> stack;
                default -> super.insertItem(slot, stack, simulate);
            };
        }
    };
    private final ItemStackHandler outInventory = new InternalItemStackHandler(1) {

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return switch (slot) {
                case 0 -> stack;
                case -1 -> super.insertItem(0, stack, simulate);
                default -> super.insertItem(slot, stack, simulate);
            };
        }
    };
    private final ItemStackHandler processingInventory = new InternalItemStackHandler(2);

    // Dynamic
    ////////////////////
    private TileStatus status = TileStatus.IDLE;
    private double percent;
    private final BaseBiRecipeProcessor<ItemStack, ItemStack, ItemStack, ItemCatalyst, MaterializerRecipe> biRecipeProcessor;

    ////////////////////
    private int ticksRun;
    private int lastTriggeredWorldEventIndex;
    private int blockStateChangeBuffer;// no need to serialize
    ////////////////////
    private int failTick;
    private boolean failed;

    public MaterializerTile() {
        biRecipeProcessor = new ItemBiRecipeProcessor<>(MaterializerHandler.getInstance());
        inventoriesToDrop.add(inputInventory);
        inventoriesToDrop.add(outInventory);
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString("container.entropy_user");
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        ticksRun = compound.getInteger("ticksRun");
        failTick = compound.getInteger("failTick");
        lastTriggeredWorldEventIndex = compound.getInteger("lastTriggeredWorldEventIndex");
        if (compound.hasKey("inputInventory"))
            inputInventory.deserializeNBT(compound.getCompoundTag("inputInventory"));
        if (compound.hasKey("outInventory"))
            outInventory.deserializeNBT(compound.getCompoundTag("outInventory"));
        if (compound.hasKey("processingInventory"))
            processingInventory.deserializeNBT(compound.getCompoundTag("processingInventory"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("ticksRun", ticksRun);
        compound.setInteger("failTick", failTick);
        compound.setInteger("lastTriggeredWorldEventIndex", lastTriggeredWorldEventIndex);
        compound.setTag("inputInventory", inputInventory.serializeNBT());
        compound.setTag("outInventory", outInventory.serializeNBT());
        compound.setTag("processingInventory", processingInventory.serializeNBT());
        return super.writeToNBT(compound);
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, GuiSyncManager syncManager) {
        ModularPanel panel = ModularPanel.defaultPanel("materializer_gui").bindPlayerInventory();

        syncManager.syncValue("lastTriggeredCriticalityIndex",
                SyncHandlers.intNumber(() -> lastTriggeredWorldEventIndex,
                        lastTriggeredCriticalityIndex -> this.lastTriggeredWorldEventIndex = lastTriggeredCriticalityIndex));
        syncManager.syncValue("tileStatus",
                SyncHandlers.enumValue(TileStatus.class, () -> status, status -> this.status = status));
        syncManager.syncValue("percentage",
                SyncHandlers.doubleNumber(() -> percent, percent -> this.percent = percent));
        syncManager.syncValue("processingInv", new SyncHandler() {

            @Override
            public void readOnClient(int id, PacketBuffer buf) throws IOException {
                if (id == 0 || id == 1) processingInventory.setStackInSlot(id, buf.readItemStack());
            }

            @Override
            public void readOnServer(int id, PacketBuffer buf) {}

            private ItemStack slot0 = ItemStack.EMPTY;
            private ItemStack slot1 = ItemStack.EMPTY;
            private boolean valid;

            @Override
            public void detectAndSendChanges(boolean init) {
                if (init || invChanged()) {
                    sync(0, buffer -> buffer.writeItemStack(processingInventory.getStackInSlot(0)));
                    sync(1, buffer -> buffer.writeItemStack(processingInventory.getStackInSlot(1)));
                }
            }

            private boolean invChanged() {
                var changed = false;
                var valid = biRecipeProcessor.hasRecipeGroupingRecipe() && biRecipeProcessor.hasRecipe();
                if (this.valid != valid) {
                    this.valid = valid;
                    changed = true;
                }
                if (valid) {
                    if (!biRecipeProcessor.getHandlerRegistry().getHashStrategy()
                            .equals(processingInventory.getStackInSlot(0), slot0)) {
                        slot0 = processingInventory.getStackInSlot(0);
                        changed = true;
                    }
                    if (!biRecipeProcessor.getRecipeGrouping().getHashStrategy()
                            .equals(processingInventory.getStackInSlot(1), slot1)) {
                        slot1 = processingInventory.getStackInSlot(1);
                        changed = true;
                    }
                }
                return changed;
            }
        });

        if (data.isClient()) {
            var textBox = new BasicTextWidget().left(20).top(3).right(20).background(GuiTextures.MC_BACKGROUND);
            textBox.setKey(() -> {
                if (failed) return TileStatus.FAILED.getLangKey();
                return status.getLangKey();
            });
            panel.child(textBox);

            final var render = new BlockStateRendereWidget()
                    .background(GuiTextures.MC_BACKGROUND).size(176, 166).leftRel(1f);
            render.onUpdateListener(thatRender -> {
                if (!biRecipeProcessor.validateGrouping(processingInventory.getStackInSlot(1)) ||
                        !biRecipeProcessor.validateRecipe(processingInventory.getStackInSlot(0))) {
                    thatRender.disableRender(true);
                    return;
                }
                if (failed) return;
                var event = biRecipeProcessor.getRecipe().getNextWorldEvent(lastTriggeredWorldEventIndex);
                if (Objects.isNull(event)) {
                    thatRender.disableRender(true);
                    return;
                }
                if (percent == 0 || event.getIntKey() > percent) {
                    thatRender.disableRender(false);
                    thatRender.setStructure(event.getValue().getStructure().getStructure());
                }
            });

            final var renderText = new BasicTextWidget().background(GuiTextures.MC_BACKGROUND).bottomRel(1f).left(8)
                    .right(8).height(25);
            renderText.setKeyArg(() -> {
                if (!biRecipeProcessor.validateRecipe(processingInventory.getStackInSlot(0))) return null;
                if (percent <= 0 || percent >= 100) return null;
                if (failed) return Pair.of("tile.materializer.render.fail", null);
                var event = biRecipeProcessor.getRecipe().getNextWorldEvent(lastTriggeredWorldEventIndex);
                if (event == null) return null;

                return Pair.of("tile.materializer.render.info",
                        new Object[] { event.getValue().getChance(), event.getIntKey() });
            });

            render.child(renderText);

            final var renderControls = new ParentWidget<>().background(GuiTextures.MC_BACKGROUND).topRel(1f).left(8)
                    .right(8).height(25);
            render.child(renderControls);

            final var followCurrentStructureButton = new ButtonWidget<>().background(GuiTextures.MC_BACKGROUND)
                    .right(20)
                    .top(3);
            followCurrentStructureButton.tooltip().addLine(new LangKey("tile.materializer.render.followStruct"));
            followCurrentStructureButton.onMousePressed(mouseButton -> {
                if (mouseButton != 0) return false;
                render.setDisableOnUpdateListener(!render.isDisableOnUpdateListener());
                return true;
            });
            renderControls.child(followCurrentStructureButton);

            {
                final var toggleFullRenderButton = new ButtonWidget<>().background(GuiTextures.MC_BACKGROUND).right(60)
                        .top(3);
                toggleFullRenderButton.tooltip().addLine(new LangKey("tile.materializer.render.toggleFullRender"));
                toggleFullRenderButton.onMousePressed(mouseButton -> {
                    if (mouseButton != 0) return false;
                    render.setRenderLayer(render.getMaxRenderLayer());
                    return true;
                });
                renderControls.child(toggleFullRenderButton);

                final var renderLayerUpButton = new ButtonWidget<>().background(GuiTextures.MC_BACKGROUND).right(80)
                        .top(3);
                renderLayerUpButton.tooltip().addLine(new LangKey("tile.materializer.render.nextLayer"));
                renderLayerUpButton.onMousePressed(mouseButton -> {
                    if (mouseButton != 0) return false;
                    render.nextRenderLayer();
                    return true;
                });
                renderControls.child(renderLayerUpButton);

                final var renderLayerDownButton = new ButtonWidget<>().background(GuiTextures.MC_BACKGROUND).right(100)
                        .top(3);
                renderLayerDownButton.tooltip().addLine(new LangKey("tile.materializer.render.prevLayer"));
                renderLayerDownButton.onMousePressed(mouseButton -> {
                    if (mouseButton != 0) return false;
                    render.prevRenderLayer();
                    return true;
                });
                renderControls.child(renderLayerDownButton);
            }

            panel.child(render);
            data.getJeiSettings().addJeiExclusionArea(render);
            data.getJeiSettings().addJeiExclusionArea(renderText);
            data.getJeiSettings().addJeiExclusionArea(renderControls);

            var button = new ButtonWidget<>().background(GuiTextures.MC_BACKGROUND).right(168).bottom(75)
                    .tooltip(tooltip -> tooltip.addLine(new LangKey("tile.materializer.render.show")));
            panel.child(button);

            button.overlay(new DynamicKey(() -> String.valueOf((int) percent)));
            button.onMousePressed(mouseButton -> {
                if (mouseButton != 0) return false;
                render.setEnabled(!render.isEnabled());
                renderText.setEnabled(!renderText.isEnabled());
                return true;
            });
            render.setEnabled(false);
            renderText.setEnabled(false);

            panel.child(new ProgressWidget()
                    .size(18).left(95).top(45)
                    .texture(GuiTextures.PROGRESS_ARROW, 25)
                    .progress(() -> percent / 100));
        }

        // TODO: 28/10/2023 center
        var itemInput = new ItemSlot().slot(new ModularSlot(inputInventory, 0)).left(50).top(45);
        itemInput.tooltip().setAutoUpdate(true).tooltipBuilder(tooltip -> {
            tooltip.addLine(biRecipeProcessor.getHandlerRegistry().translateHashStrategy());
        });
        panel.child(itemInput);

        var itemInput2 = new ItemSlot().slot(new ModularSlot(inputInventory, 1)).left(70).top(45);
        itemInput2.tooltip().setAutoUpdate(true).tooltipBuilder(tooltip -> {
            if (!biRecipeProcessor.hasRecipeGroupingRecipe()) return;
            tooltip.addLine(biRecipeProcessor.getRecipeGrouping().translateHashStrategy());
        });
        panel.child(itemInput2);

        panel.child(new ItemSlot().slot(new ModularSlot(outInventory, 0).accessibility(false, true)).left(120).top(45));
        return panel;
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            updateBlockState();
            if (prepare()) progress();
        } else if (status == TileStatus.FAILED || failTick > 0) {
            if (failTick == 0) failed = true;
            if (failTick > 20 * 3) {
                failed = false;
                failTick = 0;
            }
            failTick++;
        }
    }

    private boolean prepare() {
        var processingItem = processingInventory.getStackInSlot(0);
        var processingCatalyst = processingInventory.getStackInSlot(1);
        boolean validProcessingRecipe = true;
        if (processingItem.isEmpty() || processingCatalyst.isEmpty()) {
            if (!(processingItem.isEmpty() && processingCatalyst.isEmpty()))
                EndReforked.LOGGER.warn("Clearing processing items in {} at {}, items {}, {}",
                        this.getClass(), this.pos, processingItem, processingCatalyst);
            updateStatus(TileStatus.IDLE);
            resetProcessingInv();
            validProcessingRecipe = false;
        }

        if (validProcessingRecipe) {
            if (!biRecipeProcessor.validateGrouping(processingCatalyst)) return false;
            if (!biRecipeProcessor.validateRecipe(processingItem)) return false;
        } else {
            boolean invalid = false;

            var inputCatalyst = inputInventory.getStackInSlot(1);
            if (!biRecipeProcessor.validateGrouping(inputCatalyst)) invalid = true;

            var inputItem = inputInventory.getStackInSlot(0);
            if (!biRecipeProcessor.validateRecipe(inputItem)) invalid = true;

            if (invalid) {
                if (inputCatalyst.isEmpty() && inputItem.isEmpty()) {
                    updateStatus(TileStatus.IDLE);
                } else updateStatus(TileStatus.INVALID);
                return false;
            }

            if (!inputInventory.extractItem(0, biRecipeProcessor.getRecipe().getInput().getCount(), true).isEmpty() &&
                    !inputInventory.extractItem(0, 1, true).isEmpty()) {
                processingInventory.insertItem(0,
                        inputInventory.extractItem(0, biRecipeProcessor.getRecipe().getInput().getCount(), false),
                        false);
                processingInventory.insertItem(1, inputInventory.extractItem(1, 1, false), false);
            } else {
                updateStatus(TileStatus.INVALID);
                return false;
            }
        }
        return true;
    }

    private void progress() {
        // finished
        if (ticksRun >= biRecipeProcessor.getRecipe().getTicksToComplete()) {
            progressWorldEvents();
            var outputStack = biRecipeProcessor.getRecipe().getRecipeFunction().apply(
                    processingInventory.getStackInSlot(0),
                    processingInventory.getStackInSlot(1));
            if (outInventory.insertItem(-1, outputStack, true).isEmpty()) {
                outInventory.insertItem(-1, outputStack, false);
                spawnSpawnSuccessfulCraft();
                resetProcessingInv();
                reset();
            } else updateStatus(TileStatus.OUT_FULL);
        } else {
            progressWorldEvents();
            ticksRun++;
        }
    }

    private void progressWorldEvents() {
        updateStatus(TileStatus.RUNNING);
        final double ratio = (double) ticksRun / biRecipeProcessor.getRecipe().getTicksToComplete();
        percent = (ratio * 100);
        for (int i : biRecipeProcessor.getRecipe().getWorldEvents().keySet()) {
            if ((int) percent >= i && (lastTriggeredWorldEventIndex < i || lastTriggeredWorldEventIndex == 0)) {
                if (!triggerWorldEvent(i)) {
                    updateStatus(TileStatus.FAILED);
                    spawnFailedCraftParticles();
                    resetProcessingInv();
                    reset();
                }
                lastTriggeredWorldEventIndex = i;
            }
        }
    }

    private boolean triggerWorldEvent(int index) {
        var chance = world.rand.nextInt(100);
        WorldEvent event = biRecipeProcessor.getRecipe().getWorldEvents().get(index);
        if (chance > event.getChance()) return true;

        List<BlockPos> posList = Lists.newArrayList(BlockPos.getAllInBox(this.getPos().add(5, 5, 5),
                this.getPos().add(-5, -5, -5)));

        Collections.shuffle(posList);
        for (BlockPos blockPos : posList) {
            if (!world.isBlockLoaded(pos)) continue;
            if (blockPos.getX() == this.getPos().getX() &&
                    blockPos.getZ() == this.getPos().getZ())
                continue;

            // var state = world.getBlockState(pos);
            var pattern = event.getStructure().getPattern().match(world, blockPos);
            if (pattern != null) {
                for (int y = 0; y < event.getStructure().getPattern().getThumbLength(); y++) {
                    for (int z = 0; z < event.getStructure().getPattern().getFingerLength(); z++) {
                        for (int x = 0; x < event.getStructure().getPattern().getPalmLength(); x++) {
                            var worldInfo = pattern.translateOffset(x, y, z);
                            event.getBlockAction().particles((WorldServer) world, worldInfo);
                            event.getBlockAction().run((WorldServer) world, worldInfo);
                        }
                    }
                }
                return true;
            }
        }

        return false;
    }

    private void updateStatus(TileStatus status) {
        if (this.status != status) {
            this.status = status;
        }
    }

    private void updateBlockState() {
        if (++blockStateChangeBuffer % (20 * 2) == 0) {
            IBlockState state = world.getBlockState(pos);
            var working = state.getValue(BlockMaterializer.WORKING);
            if (status == TileStatus.RUNNING && !working) {
                world.playSound(null, pos, SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.BLOCKS, 0.5F,
                        2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

                world.setBlockState(pos, ModBlocks.MATERIALIZER_BLOCK.get().getDefaultState()
                        .withProperty(BlockMaterializer.FACING, state.getValue(BlockMaterializer.FACING))
                        .withProperty(BlockMaterializer.WORKING, true), 3);

            } else if (status != TileStatus.RUNNING && working) {
                world.setBlockState(pos, ModBlocks.MATERIALIZER_BLOCK.get().getDefaultState()
                        .withProperty(BlockMaterializer.FACING, state.getValue(BlockMaterializer.FACING))
                        .withProperty(BlockMaterializer.WORKING, false), 3);
            }
            blockStateChangeBuffer = 0;
        }
    }

    private void spawnFailedCraftParticles() {
        world.addWeatherEffect(new EntityLightningBolt(world, pos.getX(), pos.getY(), pos.getZ(), false));
        spawnCircleOnBlock(getPos(), FLAME);
    }

    private void spawnSpawnSuccessfulCraft() {
        spawnCircleOnBlock(getPos(), PORTAL);
    }

    private void spawnCircleOnBlock(BlockPos pos, EnumParticleTypes type) {
        for (int i = 0; i < 360; i += 40) {
            double xSpeed = Math.cos(Math.toRadians(i)) * 0.2;
            double zSpeed = Math.sin(Math.toRadians(i)) * 0.2;
            ((WorldServer) world).spawnParticle(type, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                    1, xSpeed, 0, zSpeed, 0.2);
        }
    }

    private void resetProcessingInv() {
        processingInventory.setStackInSlot(0, ItemStack.EMPTY);
        processingInventory.setStackInSlot(1, ItemStack.EMPTY);
    }

    private void reset() {
        ticksRun = 0;
        lastTriggeredWorldEventIndex = 0;
        markDirty();
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null) {
            switch (facing) {
                case NORTH, SOUTH, WEST, EAST, DOWN -> {
                    return true;
                }
                case UP -> {
                    return false;
                }
            }
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null) {
            switch (facing) {
                case NORTH, SOUTH, WEST, EAST -> {
                    return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inputInventory);
                }
                case DOWN -> {
                    return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(outInventory);
                }
            }
        }
        return super.getCapability(capability, facing);
    }
}
