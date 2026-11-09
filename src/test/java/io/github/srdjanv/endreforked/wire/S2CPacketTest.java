package io.github.srdjanv.endreforked.wire;

import io.github.srdjanv.endreforked.wire.s2c.Aisle;
import io.github.srdjanv.endreforked.wire.s2c.BlockSpec;
import io.github.srdjanv.endreforked.wire.s2c.DropItemAction;
import io.github.srdjanv.endreforked.wire.s2c.EntityResult;
import io.github.srdjanv.endreforked.wire.s2c.FluidAmount;
import io.github.srdjanv.endreforked.wire.s2c.FluidBlockInteraction;
import io.github.srdjanv.endreforked.wire.s2c.FluidBlockInteractions;
import io.github.srdjanv.endreforked.wire.s2c.FluidEntityInteraction;
import io.github.srdjanv.endreforked.wire.s2c.FluidEntityInteractions;
import io.github.srdjanv.endreforked.wire.s2c.Ingredient;
import io.github.srdjanv.endreforked.wire.s2c.MachineRecipe;
import io.github.srdjanv.endreforked.wire.s2c.MachineRecipes;
import io.github.srdjanv.endreforked.wire.s2c.OreIngredient;
import io.github.srdjanv.endreforked.wire.s2c.PlayerParticle;
import io.github.srdjanv.endreforked.wire.s2c.RegistrySnapshot;
import io.github.srdjanv.endreforked.wire.s2c.S2CPacket;
import io.github.srdjanv.endreforked.wire.s2c.SetBlockAction;
import io.github.srdjanv.endreforked.wire.s2c.Stack;
import io.github.srdjanv.endreforked.wire.s2c.StructureSpec;
import io.github.srdjanv.endreforked.wire.s2c.SymbolSpec;
import io.github.srdjanv.endreforked.wire.s2c.TpParticle;
import io.github.srdjanv.endreforked.wire.s2c.WorldAction;
import io.github.srdjanv.endreforked.wire.s2c.WorldEvent;
import okio.ByteString;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class S2CPacketTest {

    private static Stack stack(int tag) {
        return new Stack(ByteString.of((byte) tag), ByteString.EMPTY);
    }

    private static FluidAmount fluid(int tag) {
        return new FluidAmount(ByteString.of((byte) tag), ByteString.EMPTY);
    }

    private static S2CPacket roundTrip(S2CPacket packet) throws IOException {
        return S2CPacket.ADAPTER.decode(S2CPacket.ADAPTER.encode(packet));
    }

    @Test
    void playerParticle() throws IOException {
        var in = new PlayerParticle(false, "fly", 200, 7, ByteString.EMPTY);
        var out = roundTrip(new S2CPacket(in, null, null, null, ByteString.EMPTY));
        assertNull(out.getTp_particle());
        assertNotNull(out.getPlayer_particle());
        assertEquals(in, out.getPlayer_particle());
        assertEquals("fly", out.getPlayer_particle().getId());
        assertEquals(200, out.getPlayer_particle().getRender_iterations());
        assertEquals(7, out.getPlayer_particle().getParticle_type());
    }

    @Test
    void playerParticleRemove() throws IOException {
        var in = new PlayerParticle(true, "fly", 0, 0, ByteString.EMPTY);
        assertEquals(in, roundTrip(new S2CPacket(in, null, null, null, ByteString.EMPTY)).getPlayer_particle());
    }

    @Test
    void tpParticle() throws IOException {
        var in = new TpParticle(1.5, 2.5, -3.5, 4, 5, 6, 0.6, 1.8, ByteString.EMPTY);
        var out = roundTrip(new S2CPacket(null, in, null, null, ByteString.EMPTY));
        assertNull(out.getPlayer_particle());
        assertEquals(in, out.getTp_particle());
        assertEquals(-3.5, out.getTp_particle().getPos_z());
        assertEquals(1.8, out.getTp_particle().getHeight());
    }

    @Test
    void registrySnapshotWithMachineRecipes() throws IOException {
        var recipe = new MachineRecipe("endreborn:entropy_chamber/example", "endreborn:entropy_chamber",
            List.of(new Ingredient(stack(4), null, ByteString.EMPTY),
                new Ingredient(null, new OreIngredient("logWood", 2, ByteString.EMPTY), ByteString.EMPTY)
            ),
            List.of(fluid(2000)), List.of(stack(2)), List.of(fluid(1000)), 500, 150, 3, List.of(), 0, ByteString.EMPTY
        );
        var snapshot = new RegistrySnapshot("endreborn:machine_recipes",
            new MachineRecipes(List.of(recipe), ByteString.EMPTY), null, null, ByteString.EMPTY
        );
        var out = roundTrip(new S2CPacket(null, null, snapshot, null, ByteString.EMPTY));
        assertNull(out.getPlayer_particle());
        assertEquals(snapshot, out.getRegistry_snapshot());
        var decoded = out.getRegistry_snapshot().getMachine_recipes().getRecipes().get(0);
        assertEquals("logWood", decoded.getItem_inputs().get(1).getOre().getName());
        assertEquals(stack(4), decoded.getItem_inputs().get(0).getItem());
        assertEquals(fluid(2000), decoded.getFluid_inputs().get(0));
        assertEquals(150, decoded.getEntropy());
    }

    @Test
    void machineRecipeWithWorldEvents() throws IOException {
        var stone = new BlockSpec("minecraft:stone", Map.of("variant", "granite"), ByteString.EMPTY);
        var structure = new StructureSpec(List.of(new Aisle(List.of("SS", "S "), ByteString.EMPTY)),
            List.of(new SymbolSpec("S", stone, ByteString.EMPTY)), ByteString.EMPTY
        );
        var setBlock = new WorldEvent(30, 15, structure,
            new WorldAction(new SetBlockAction(new BlockSpec("minecraft:iron_block", Map.of(), ByteString.EMPTY),
                    ByteString.EMPTY
                ),
                null, ByteString.EMPTY
            ),
            ByteString.EMPTY
        );
        var drop = new WorldEvent(90, 2, structure,
            new WorldAction(null, new DropItemAction(stack(2), ByteString.EMPTY), ByteString.EMPTY), ByteString.EMPTY
        );
        var recipe = new MachineRecipe("endreborn:materializer/example", "endreborn:materializer", List.of(), List.of(),
            List.of(), List.of(), 600, 0, 0, List.of(setBlock, drop), 0, ByteString.EMPTY
        );
        var snapshot = new RegistrySnapshot("endreborn:machine_recipes",
            new MachineRecipes(List.of(recipe), ByteString.EMPTY), null, null, ByteString.EMPTY
        );
        var out = roundTrip(new S2CPacket(null, null, snapshot, null, ByteString.EMPTY));
        assertEquals(snapshot, out.getRegistry_snapshot());
        var events = out.getRegistry_snapshot().getMachine_recipes().getRecipes().get(0).getWorld_events();
        assertEquals(2, events.size());
        assertEquals("granite",
            events.get(0).getStructure().getSymbols().get(0).getBlock().getProperties().get("variant")
        );
        assertEquals("minecraft:iron_block", events.get(0).getAction().getSet_block().getBlock().getBlock());
        assertNull(events.get(1).getAction().getSet_block());
        assertEquals(stack(2), events.get(1).getAction().getDrop_item().getItem());
    }

    @Test
    void fluidInteractions() throws IOException {
        var endStone = new BlockSpec("minecraft:end_stone", Map.of(), ByteString.EMPTY);
        var block = new FluidBlockInteraction("endreborn:b", "endreborn:entropy", endStone, endStone, 5, false, 0, 0,
            ByteString.EMPTY
        );
        var blocks = new RegistrySnapshot("endreborn:fluid_block_interactions", null,
            new FluidBlockInteractions(List.of(block), ByteString.EMPTY), null, ByteString.EMPTY
        );
        assertEquals(blocks, roundTrip(new S2CPacket(null, null, blocks, null, ByteString.EMPTY)).getRegistry_snapshot()
        );

        var item = new FluidEntityInteraction("endreborn:e", "endreborn:entropy", "minecraft:item", stack(4), false,
            new EntityResult(stack(4), null, null, ByteString.EMPTY), 10, true, 0, 0, ByteString.EMPTY
        );
        var watcher = new FluidEntityInteraction("endreborn:w", "endreborn:entropy", "minecraft:enderman", null, true,
            new EntityResult(null, "endreborn:watcher", null, ByteString.EMPTY), 1, true, 0, 0, ByteString.EMPTY
        );
        var fluid = new FluidEntityInteraction("endreborn:f", "endreborn:entropy", "minecraft:item", stack(1), false,
            new EntityResult(null, null, endStone, ByteString.EMPTY), 20, true, 0, 0, ByteString.EMPTY
        );
        var entities = new RegistrySnapshot("endreborn:fluid_entity_interactions", null, null,
            new FluidEntityInteractions(List.of(item, watcher, fluid), ByteString.EMPTY), ByteString.EMPTY
        );
        var out = roundTrip(new S2CPacket(null, null, entities, null, ByteString.EMPTY)).getRegistry_snapshot();
        assertEquals(entities, out);
        var list = out.getFluid_entity_interactions().getInteractions();
        assertEquals("endreborn:watcher", list.get(1).getResult().getSpawn_entity());
        assertNull(list.get(1).getItem());
        assertEquals("minecraft:end_stone", list.get(2).getResult().getSet_fluid_block().getBlock());
    }

}
