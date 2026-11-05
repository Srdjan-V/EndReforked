package io.github.srdjanv.endreforked.compat.jei.fluids;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

import io.github.srdjanv.endreforked.api.fluids.IFluidInteractable;
import io.github.srdjanv.endreforked.api.fluids.IWorldRecipe;
import io.github.srdjanv.endreforked.api.fluids.base.EntityFluidRecipeResult;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import jeresources.util.Font;
import jeresources.util.RenderHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;

public abstract class FluidInteraction implements IRecipeWrapper {

    protected final Type type;
    protected final FluidStack fluidStack;

    protected FluidInteraction(Type type, FluidStack fluidStack) {
        this.type = type;
        this.fluidStack = fluidStack;
    }

    public static class SideRecipe extends FluidInteraction {

        public static List<SideRecipe> buildAnny(IFluidInteractable interactable) {
            var reg = interactable.getAnyFluidCollisionRegistry();
            if (reg == null) return Collections.emptyList();
            List<SideRecipe> sideRecipes = new ObjectArrayList<>();
            var fluidStack = new FluidStack(interactable.getFluid(), 1_000);
            for (var entry : reg.getRegistry().entrySet())
                sideRecipes.add(new SideRecipe(Type.SIDE_ANY,
                        fluidStack,
                        entry.getValue(),
                        entry.getValue().getInput().getDefaultState(),
                        entry.getValue().getRecipeFunction().apply(entry.getKey())));

            return sideRecipes;
        }

        public static List<SideRecipe> buildState(IFluidInteractable interactable) {
            var reg = interactable.getFluidCollisionRegistry();
            if (reg == null) return Collections.emptyList();
            List<SideRecipe> sideRecipes = new ObjectArrayList<>();
            var fluidStack = new FluidStack(interactable.getFluid(), 1_000);
            for (var entry : reg.getRegistry().entrySet())
                sideRecipes.add(new SideRecipe(Type.SIDE_STATE,
                        fluidStack,
                        entry.getValue(),
                        entry.getValue().getInput(),
                        entry.getValue().getRecipeFunction().apply(entry.getKey())));

            return sideRecipes;
        }

        private final IWorldRecipe iWorldRecipe;
        private final IBlockState inIBlockState;
        private final IBlockState outIBlockState;

        protected SideRecipe(Type type, FluidStack fluidStack, IWorldRecipe iWorldRecipe, IBlockState inIBlockState,
                             IBlockState outIBlockState) {
            super(type, fluidStack);

            this.iWorldRecipe = iWorldRecipe;
            this.inIBlockState = inIBlockState;
            this.outIBlockState = outIBlockState;
        }

        public IWorldRecipe getIWorldRecipe() {
            return iWorldRecipe;
        }

        public IBlockState getInIBlockState() {
            return inIBlockState;
        }

        public IBlockState getOutIBlockState() {
            return outIBlockState;
        }

        @Override
        public void getIngredients(IIngredients ingredients) {
            ingredients.setInput(VanillaTypes.FLUID, fluidStack);
            ingredients.setInput(VanillaTypes.ITEM, new ItemStack(inIBlockState.getBlock()));
            ingredients.setOutput(VanillaTypes.ITEM, new ItemStack(outIBlockState.getBlock()));
        }
    }

    public static class EntityRecipe extends FluidInteraction {

        public static List<EntityRecipe> build(IFluidInteractable interactable) {
            var reg = interactable.getEntityFluidCollisionRegistry();
            if (reg == null) return Collections.emptyList();
            final var fluidStack = new FluidStack(interactable.getFluid(), 1_000);

            return reg.getRegistry()
                    .values()
                    .stream()
                    .flatMap(List::stream)
                    .map(fluidRecipe -> new EntityRecipe(Type.ENTITY,
                            fluidStack,
                            fluidRecipe,
                            fluidRecipe.getInput(),
                            fluidRecipe.getEntityMather().getEntityBuilder(),
                            fluidRecipe.getRecipeFunction()))
                    .collect(Collectors.toCollection(ObjectArrayList::new));
        }

        private final IWorldRecipe iWorldRecipe;
        private final Class<Entity> inputType;
        private final Function<World, Entity> entityBuilder;
        private final BiFunction<World, Entity, EntityFluidRecipeResult<Entity>> recipeFunction;

        protected EntityRecipe(Type type,
                               FluidStack fluidStack,
                               IWorldRecipe iWorldRecipe, Class<Entity> inputType,
                               Function<World, Entity> entityBuilder,
                               BiFunction<World, Entity, EntityFluidRecipeResult<Entity>> recipeFunction) {
            super(type, fluidStack);

            this.iWorldRecipe = iWorldRecipe;
            this.inputType = inputType;
            this.entityBuilder = entityBuilder;
            this.recipeFunction = recipeFunction;
        }

        public IWorldRecipe getIWorldRecipe() {
            return iWorldRecipe;
        }

        public Class<Entity> getInputType() {
            return inputType;
        }

        public EntityFluidRecipeResult<Entity> getRecipeResult() {
            final var c_world = Minecraft.getMinecraft().world;
            return recipeFunction.apply(c_world, entityBuilder.apply(c_world));
        }

        @Override
        public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
            Entity entity = entityBuilder.apply(minecraft.world);
            if (entity instanceof EntityLivingBase livingBase) {
                // todo shadow RenderHelper
                RenderHelper.scissor(minecraft, 7, 43, 59, 79);
                var scale = this.getScale(entity);
                var offsetY = this.getOffsetY(entity);
                RenderHelper.renderEntity(25, 50 - offsetY, scale, (float) (38 - mouseX),
                        (float) (70 - offsetY - mouseY), livingBase);
                RenderHelper.stopScissor();
            }

            String mobName = entity.getName();
            Font.normal.print(mobName, 7, 2);
        }

        @Override
        public void getIngredients(IIngredients ingredients) {
            final var c_world = Minecraft.getMinecraft().world;
            ingredients.setInput(VanillaTypes.FLUID, fluidStack);
            if (inputType.isAssignableFrom(EntityItem.class)) {
                EntityItem item = (EntityItem) entityBuilder.apply(c_world);
                ingredients.setInput(VanillaTypes.ITEM, item.getItem());
            }

            var recipeResult = getRecipeResult();
            switch (recipeResult.getType()) {
                case ENTITY -> {
                    var result = recipeResult.getEntityResult();
                    if (result instanceof EntityItem entityItem) {
                        ingredients.setOutput(VanillaTypes.ITEM, entityItem.getItem());
                    }
                }

                case STATE -> {
                    var outState = recipeResult.getStateResult();
                    if (outState.getBlock() instanceof IFluidBlock fluidBlock) {
                        ingredients.setOutput(VanillaTypes.FLUID, new FluidStack(fluidBlock.getFluid(), 1_000));
                    } else if (outState.getBlock() instanceof BlockLiquid) {
                        // todo check if this will get the correct fluid
                        BlockStaticLiquid staticBlock = BlockLiquid.getStaticBlock(outState.getMaterial());
                        ingredients.setOutput(VanillaTypes.FLUID, new FluidStack(
                                FluidRegistry.getFluid(staticBlock.getRegistryName().getNamespace()), 1_000));
                    } else {
                        ingredients.setOutput(VanillaTypes.ITEM, new ItemStack(outState.getBlock()));
                    }
                }
                default -> throw new IllegalStateException("Unexpected value: " + recipeResult.getType());
            }
        }

        private void renderEntity(int x, int y, float scale, float yaw, float pitch, Entity entity) {
            if (entity.world == null) entity.world = Minecraft.getMinecraft().world;
            GlStateManager.enableColorMaterial();
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x, (float) y, 50.0F);
            GlStateManager.scale(-scale, scale, scale);
            GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
            float rotationYaw = entity.rotationYaw;
            float rotationPitch = entity.rotationPitch;
            net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
            GlStateManager.rotate(-((float) Math.atan(pitch / 40.0F)) * 20.0F, 1.0F, 0.0F, 0.0F);
            entity.rotationYaw = (float) Math.atan(yaw / 40.0F) * 40.0F;
            entity.rotationPitch = -((float) Math.atan(pitch / 40.0F)) * 20.0F;
            GlStateManager.translate(0.0, entity.getYOffset(), 0.0);
            entity.rotationYaw = rotationYaw;
            entity.rotationPitch = rotationPitch;
            GlStateManager.popMatrix();
            net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.disableTexture2D();
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        }

        private float getScale(Entity entity) {
            float width = entity.width;
            float height = entity.height;
            if (width <= height) {
                if ((double) height < 0.9) {
                    return 50.0F;
                } else if (height < 1.0F) {
                    return 35.0F;
                } else if ((double) height < 1.8) {
                    return 33.0F;
                } else if (height < 2.0F) {
                    return 32.0F;
                } else if (height < 3.0F) {
                    return 24.0F;
                } else {
                    return height < 4.0F ? 20.0F : 10.0F;
                }
            } else if (width < 1.0F) {
                return 38.0F;
            } else if (width < 2.0F) {
                return 27.0F;
            } else {
                return width < 3.0F ? 13.0F : 9.0F;
            }
        }

        private int getOffsetY(Entity entity) {
            int offsetY = 0;
            if (entity instanceof EntitySquid) {
                offsetY = 20;
            } else if (entity instanceof EntityWitch) {
                offsetY = -10;
            } else if (entity instanceof EntityGhast) {
                offsetY = 15;
            } else if (entity instanceof EntityWither) {
                offsetY = -15;
            } else if (entity instanceof EntityDragon) {
                offsetY = 15;
            } else if (entity instanceof EntityEnderman) {
                offsetY = -10;
            } else if (entity instanceof EntityGolem) {
                offsetY = -10;
            } else if (entity instanceof EntityAnimal) {
                offsetY = -20;
            } else if (entity instanceof EntityVillager) {
                offsetY = -15;
            } else if (entity instanceof EntityVindicator) {
                offsetY = -15;
            } else if (entity instanceof EntityEvoker) {
                offsetY = -10;
            } else if (entity instanceof EntityBlaze) {
                offsetY = -10;
            } else if (entity instanceof EntityCreeper) {
                offsetY = -15;
            }

            return offsetY;
        }
    }

    public enum Type {
        SIDE_ANY,
        SIDE_STATE,
        ENTITY
    }
}
