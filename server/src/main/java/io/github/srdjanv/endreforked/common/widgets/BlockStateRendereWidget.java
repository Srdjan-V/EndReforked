package io.github.srdjanv.endreforked.common.widgets;

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widget.ParentWidget;

import io.github.srdjanv.endreforked.EndReforked;

public class BlockStateRendereWidget extends ParentWidget<BlockStateRendereWidget> implements Interactable {

    private boolean canTick = true;
    private boolean disableOnUpdateListener;
    private boolean disableRender = false;
    private int tick = 0;
    private float scale;
    private float rotX;
    private float rotY;
    private StructureRenderData renderInfo;
    private BlockAccessRendererWrapper blockAccess;

    public BlockStateRendereWidget() {
        rotX = 25.0F;
        rotY = -45.0F;
        scale = 30;
    }

    public void setDisableOnUpdateListener(boolean disableOnUpdateListener) {
        this.disableOnUpdateListener = disableOnUpdateListener;
    }

    public boolean isDisableOnUpdateListener() {
        return disableOnUpdateListener;
    }

    @Override
    public void onUpdate() {
        if (disableOnUpdateListener) return;
        super.onUpdate();
    }

    public void setStructure(IBlockState[][][] structure) {
        if (Objects.nonNull(renderInfo)) {
            if (renderInfo.structure == structure) return;
        }
        renderInfo = new StructureRenderData(structure);
        blockAccess = new BlockAccessRendererWrapper(renderInfo);
        updateScale();
    }

    @Override
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
        switch (scrollDirection) {
            case UP -> {
                increaseScale((float) amount / 100);
                return true;
            }
            case DOWN -> {
                decreaseScale((float) amount / 100);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private int lastX, lastY;

    @Override
    public void onMouseDrag(int mouseButton, long timeSinceClick) {
        if (mouseButton != 0) return;

        int dx;
        int dy;

        if (timeSinceClick == 0 || (lastX == 0 || lastY == 0)) {
            lastX = getContext().getMouseX();
            lastY = getContext().getMouseY();
            return;
        } else {
            dx = getContext().getMouseX() - lastX;
            dy = getContext().getMouseY() - lastY;
        }

        rotY = rotY + (dx / 104f) * 80;
        rotX = rotX + (dy / 100f) * 80;
        lastX = getContext().getMouseX();
        lastY = getContext().getMouseY();
    }

    @Override
    public void postResize() {
        super.postResize();
        updateScale();
    }

    public void updateScale() {
        if (Objects.isNull(renderInfo)) return;
        var scale = (getArea().width / renderInfo.structureWidth) * 0.5;
        this.scale = (float) Math.min(scale, (((double) getArea().height / renderInfo.structureHeight) * 0.5));
    }

    public void decreaseScale(float scale) {
        this.scale -= scale;
    }

    public void increaseScale(float scale) {
        this.scale += scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void prevRenderLayer() {
        setRenderLayer(Math.max(renderInfo.blockIndex - 1, -1));
    }

    public void nextRenderLayer() {
        setRenderLayer(Math.max(renderInfo.blockIndex - 1, -1));
    }

    public void setRenderLayer(int layer) {
        renderInfo.setLayerLimiter(layer);
    }

    public int getMaxRenderLayer() {
        return renderInfo.maxBlockIndex;
    }

    public void disableRender(boolean disableRender) {
        this.disableRender = disableRender;
    }

    @Override
    public void draw(GuiContext context, WidgetTheme widgetTheme) {
        if (disableRender) return;

        int stackDepth = GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH);
        int structureLength;
        try {
            if (this.canTick && ++this.tick % 20 == 0) {
                this.renderInfo.step();
            }
            // renderInfo.setShowLayer(-1);

            structureLength = this.renderInfo.structureLength;
            int structureWidth = this.renderInfo.structureWidth;
            int structureHeight = this.renderInfo.structureHeight;
            GlStateManager.enableRescaleNormal();
            GlStateManager.pushMatrix();
            RenderHelper.disableStandardItemLighting();
            BlockRendererDispatcher blockRender = Minecraft.getMinecraft().getBlockRendererDispatcher();
            // render pos xyz
            GlStateManager.translate((float) getArea().width / 2, (float) getArea().height / 2,
                    (float) Math.max(structureHeight, Math.max(structureWidth, structureLength)));
            GlStateManager.scale(this.scale, -this.scale, 1.0F);
            GlStateManager.rotate(this.rotX, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(90.0F + this.rotY, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate((float) structureLength / -2.0F, (float) structureHeight / -2.0F,
                    (float) structureWidth / -2.0F);
            GlStateManager.disableLighting();
            GlStateManager.enableDepth();
            if (Minecraft.isAmbientOcclusionEnabled()) {
                GlStateManager.shadeModel(7425);
            } else GlStateManager.shadeModel(7424);

            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            for (int h = 0; h < structureHeight; ++h) {
                for (int l = 0; l < structureLength; ++l) {
                    for (int w = 0; w < structureWidth; ++w) {
                        BlockPos pos = new BlockPos(l, h, w);
                        if (!this.blockAccess.isAirBlock(pos)) {
                            GlStateManager.translate((float) l, (float) h, (float) w);
                            GlStateManager.translate((float) (-l), (float) (-h), (float) (-w));
                            IBlockState state = this.blockAccess.getBlockState(pos);
                            Tessellator tessellator = Tessellator.getInstance();
                            BufferBuilder buffer = tessellator.getBuffer();
                            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
                            blockRender.renderBlock(state, pos, this.blockAccess, buffer);
                            tessellator.draw();
                        }
                    }
                }
            }

            GlStateManager.disableDepth();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.enableBlend();
            RenderHelper.disableStandardItemLighting();
        } catch (Exception exception) {
            EndReforked.LOGGER.error(exception);
        }

        for (structureLength = GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH); structureLength >
                stackDepth; --structureLength) {
            GlStateManager.popMatrix();
        }
    }

    static class StructureRenderData {

        private final IBlockState[][][] structure;
        private final int blockCount;
        private final int[] countPerLevel;
        private final int structureHeight;
        private final int structureLength;
        public final int structureWidth;
        private final int maxBlockIndex;
        private int blockIndex;

        public StructureRenderData(IBlockState[][][] structure) {
            this.structure = structure;
            this.structureHeight = structure.length;
            this.countPerLevel = new int[structureHeight];

            int structureWidth = 0;
            int structureLength = 0;
            int blockCount = 0;

            for (int yLayer = 0; yLayer < structure.length; ++yLayer) {
                structureLength = Math.max(structureLength, structure[yLayer].length);

                int blocksPerLayer = 0;
                for (int zFront = 0; zFront < structure[yLayer].length; ++zFront) {
                    structureWidth = Math.max(structureWidth, structure[yLayer][zFront].length);

                    for (var state : structure[yLayer][zFront]) {
                        if (state != null && state != Blocks.AIR.getDefaultState()) ++blocksPerLayer;
                    }
                }

                this.countPerLevel[yLayer] = blocksPerLayer;
                blockCount += blocksPerLayer;
            }

            this.structureWidth = structureWidth;
            this.structureLength = structureLength;
            this.blockCount = blockCount;
            this.maxBlockIndex = blockIndex = structureHeight * structureLength * structureWidth;
        }

        public void setLayerLimiter(int layer) {
            if (layer < 0) {
                reset();
            } else blockIndex = (layer + 1) * (structureLength * structureWidth) - 1;
        }

        public void reset() {
            blockIndex = maxBlockIndex;
        }

        public void step() {
            int currentIndex = blockIndex;

            do {
                if (++blockIndex >= maxBlockIndex) {
                    blockIndex = 0;
                }
            } while (isEmpty(blockIndex) && blockIndex != currentIndex);
        }

        private boolean isEmpty(int index) {
            int y = index / (structureLength * structureWidth);
            int r = index % (structureLength * structureWidth);
            int x = r / structureWidth;
            int z = r % structureWidth;

            try {
                var stare = structure[y][x][z];
                return stare == null || stare.equals(Blocks.AIR.getDefaultState());
            } catch (ArrayIndexOutOfBoundsException e) {
                return true;
            }
        }

        public IBlockState getBlockState(BlockPos pos) {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();

            if (y >= 0 && y < structure.length && x >= 0 && x < structure[y].length && z >= 0 &&
                    z < structure[y][x].length) {
                int index = y * (structureLength * structureWidth) + x * structureWidth + z;
                if (index <= blockIndex) {
                    try {
                        return structure[y][x][z];
                    } catch (ArrayIndexOutOfBoundsException e) {
                        return Blocks.AIR.getDefaultState();
                    }
                }
            }

            return Blocks.AIR.getDefaultState();
        }
    }

    static class BlockAccessRendererWrapper implements IBlockAccess {

        private final StructureRenderData data;

        public BlockAccessRendererWrapper(StructureRenderData data) {
            this.data = data;
        }

        @Nullable
        public TileEntity getTileEntity(BlockPos pos) {
            return null;
        }

        public int getCombinedLight(BlockPos pos, int lightValue) {
            return 15728880;
        }

        public @NotNull IBlockState getBlockState(BlockPos pos) {
            return data.getBlockState(pos);
        }

        public boolean isAirBlock(BlockPos pos) {
            return this.getBlockState(pos).getBlock() == Blocks.AIR;
        }

        public @NotNull Biome getBiome(BlockPos pos) {
            World world = Minecraft.getMinecraft().world;
            return world != null ? world.getBiome(pos) : Biomes.BIRCH_FOREST;
        }

        public int getStrongPower(BlockPos pos, EnumFacing direction) {
            return 0;
        }

        public @NotNull WorldType getWorldType() {
            World world = Minecraft.getMinecraft().world;
            return world != null ? world.getWorldType() : WorldType.DEFAULT;
        }

        public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
            return false;
        }
    }
}
