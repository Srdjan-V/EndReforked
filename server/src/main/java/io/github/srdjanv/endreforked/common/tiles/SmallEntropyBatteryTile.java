package io.github.srdjanv.endreforked.common.tiles;

import java.util.Optional;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.keys.LangKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.widgets.ButtonWidget;

import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyStorage;
import io.github.srdjanv.endreforked.api.entropy.EntropyDataProvider;
import io.github.srdjanv.endreforked.api.entropy.world.EntropyChunkReader;
import io.github.srdjanv.endreforked.common.capabilities.entropy.CapabilityEntropyHandler;
import io.github.srdjanv.endreforked.common.entropy.storage.DefaultEntropyStorage;
import io.github.srdjanv.endreforked.common.widgets.BasicTextWidget;

public class SmallEntropyBatteryTile extends TileEntity
                                     implements IGuiHolder<PosGuiData>, ITickable, EntropyDataProvider {

    private final EntropyChunkReader wrapper;
    private final DefaultEntropyStorage storage;
    private boolean linkDirty = true;
    private boolean forcedLink = false;

    @SideOnly(Side.CLIENT)
    private int chunkEntropy;
    @SideOnly(Side.CLIENT)
    private int battery_entropy;
    @SideOnly(Side.CLIENT)
    private boolean linked = false;

    public SmallEntropyBatteryTile() {
        wrapper = EntropyChunkReader.ofTileEntity(this);
        storage = new DefaultEntropyStorage(250);
    }

    @Override
    public Optional<EntropyStorage> getEntropyStorage() {
        return Optional.of(storage);
    }

    @Override
    public void update() {
        if (!world.isRemote && linkDirty) {
            linkToChunk(forcedLink);
            linkDirty = false;
        }
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, GuiSyncManager syncManager) {
        ModularPanel panel = ModularPanel.defaultPanel("small_entropy_battery_gui", 130, 96);
        syncManager.syncValue("link_dirty", 0, new BooleanSyncValue(
                () -> linkDirty, linkDirty -> this.linkDirty = linkDirty));
        syncManager.syncValue("linked", new BooleanSyncValue(
                this::isLinked, linked -> this.linked = linked));
        syncManager.syncValue("forced_link", 0, new BooleanSyncValue(
                () -> forcedLink, forcedLink -> this.forcedLink = forcedLink));
        syncManager.syncValue("battery_entropy", new IntSyncValue(
                storage::getCurrentEntropy,
                battery_entropy -> this.battery_entropy = battery_entropy));
        syncManager.syncValue("chunk_entropy", new IntSyncValue(
                () -> wrapper.getEntropyView().getCurrentEntropy(),
                chunkEntropy -> this.chunkEntropy = chunkEntropy));

        if (data.isClient()) {
            var totalEntropyText = new BasicTextWidget().leftRel(0.5f).top(3).size(120, 16)
                    .background(GuiTextures.MC_BACKGROUND);
            totalEntropyText.setKey(() -> String.valueOf(chunkEntropy + battery_entropy));
            totalEntropyText.tooltip().addLine(new LangKey("tile.small_entropy_battery.entropy.combined"));
            panel.child(totalEntropyText);

            var chunkEntropyText = new BasicTextWidget().leftRel(0.5f).top(21).size(120, 16)
                    .background(GuiTextures.MC_BACKGROUND);
            chunkEntropyText.setKey(() -> String.valueOf(chunkEntropy));
            chunkEntropyText.tooltip().addLine(new LangKey("entropy.chunk.available"));
            panel.child(chunkEntropyText);

            var batteryEntropyText = new BasicTextWidget().leftRel(0.5f).top(39).size(120, 16)
                    .background(GuiTextures.MC_BACKGROUND);
            batteryEntropyText.setKey(() -> String.valueOf(battery_entropy));
            batteryEntropyText.tooltip().addLine(new LangKey("entropy.storage.available"));
            panel.child(batteryEntropyText);

            var linkedTextBox = new BasicTextWidget().leftRel(0.5f).top(57).size(120, 16)
                    .background(GuiTextures.MC_BACKGROUND);
            linkedTextBox.setKey(
                    () -> linked ? "entropy.storage_reference.linked.true" : "entropy.storage_reference.linked.false");
            panel.child(linkedTextBox);

            var linkButton = new ButtonWidget<>().leftRel(0.5f).top(75).size(120, 16)
                    .overlay(new LangKey("tile.small_entropy_battery.entropy.link"));
            linkButton.onMousePressed(mouseButton -> {
                if (mouseButton != 0) return false;
                linkDirty = true;
                ((BooleanSyncValue) syncManager.getSyncHandler(GuiSyncManager.makeSyncKey("forced_link", 0)))
                        .setBoolValue(true, true, true);
                ((BooleanSyncValue) syncManager.getSyncHandler(GuiSyncManager.makeSyncKey("link_dirty", 0)))
                        .setBoolValue(true, true, true);
                return true;
            });
            panel.child(linkButton);

        }
        return panel;
    }

    public boolean linkToChunk(boolean force) {
        boolean linked = false;
        var data = wrapper.getCenterEntropy();
        if (data != null) linked = data.setEntropyStorageReference(storage, force);
        if (linked) {
            this.forcedLink = force;
        } else forcedLink = false;
        markDirty();
        return linked;
    }

    public boolean isLinked() {
        var data = wrapper.getCenterEntropy();
        if (data == null) return false;
        return data.getEntropyStorageReference().map(storageRef -> storageRef == storage).orElse(false);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        storage.deserializeNBT(compound.getCompoundTag("storage"));
        forcedLink = compound.getBoolean("forced_link");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("storage", storage.serializeNBT());
        compound.setBoolean("forced_link", forcedLink);
        return super.writeToNBT(compound);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEntropyHandler.STORAGE) return true;
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEntropyHandler.STORAGE) return CapabilityEntropyHandler.STORAGE.cast(storage);
        return super.getCapability(capability, facing);
    }
}
