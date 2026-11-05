package io.github.srdjanv.endreforked.wire;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.srdjanv.endreforked.wire.s2c.FluidAmount;
import io.github.srdjanv.endreforked.wire.s2c.Ingredient;
import io.github.srdjanv.endreforked.wire.s2c.ItemIngredient;
import io.github.srdjanv.endreforked.wire.s2c.ItemResult;
import io.github.srdjanv.endreforked.wire.s2c.MachineRecipe;
import io.github.srdjanv.endreforked.wire.s2c.MachineRecipes;
import io.github.srdjanv.endreforked.wire.s2c.OreIngredient;
import io.github.srdjanv.endreforked.wire.s2c.PlayerParticle;
import io.github.srdjanv.endreforked.wire.s2c.RegistrySnapshot;
import io.github.srdjanv.endreforked.wire.s2c.S2CPacket;
import io.github.srdjanv.endreforked.wire.s2c.TpParticle;
import okio.ByteString;

class S2CPacketTest {

    private static S2CPacket roundTrip(S2CPacket packet) throws IOException {
        return S2CPacket.ADAPTER.decode(S2CPacket.ADAPTER.encode(packet));
    }

    @Test
    void playerParticle() throws IOException {
        var in = new PlayerParticle(false, "fly", 200, 7, ByteString.EMPTY);
        var out = roundTrip(new S2CPacket(in, null, null, ByteString.EMPTY));
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
        assertEquals(in, roundTrip(new S2CPacket(in, null, null, ByteString.EMPTY)).getPlayer_particle());
    }

    @Test
    void tpParticle() throws IOException {
        var in = new TpParticle(1.5, 2.5, -3.5, 4, 5, 6, 0.6, 1.8, ByteString.EMPTY);
        var out = roundTrip(new S2CPacket(null, in, null, ByteString.EMPTY));
        assertNull(out.getPlayer_particle());
        assertEquals(in, out.getTp_particle());
        assertEquals(-3.5, out.getTp_particle().getPos_z());
        assertEquals(1.8, out.getTp_particle().getHeight());
    }

    @Test
    void registrySnapshotWithMachineRecipes() throws IOException {
        var recipe = new MachineRecipe(
                "endreborn:entropy_chamber/example", "endreborn:entropy_chamber",
                List.of(new Ingredient(new ItemIngredient("minecraft:end_stone", 0, 4, ByteString.EMPTY), null, ByteString.EMPTY),
                        new Ingredient(null, new OreIngredient("logWood", 2, ByteString.EMPTY), ByteString.EMPTY)),
                List.of(new FluidAmount("minecraft:lava", 2000, ByteString.EMPTY)),
                List.of(new ItemResult("minecraft:dirt", 0, 2, ByteString.EMPTY)),
                List.of(new FluidAmount("minecraft:water", 1000, ByteString.EMPTY)),
                500, 150, 3, ByteString.EMPTY);
        var snapshot = new RegistrySnapshot("endreborn:machine_recipes",
                new MachineRecipes(List.of(recipe), ByteString.EMPTY), ByteString.EMPTY);
        var out = roundTrip(new S2CPacket(null, null, snapshot, ByteString.EMPTY));
        assertNull(out.getPlayer_particle());
        assertEquals(snapshot, out.getRegistry_snapshot());
        var decoded = out.getRegistry_snapshot().getMachine_recipes().getRecipes().get(0);
        assertEquals("logWood", decoded.getItem_inputs().get(1).getOre().getName());
        assertEquals(4, decoded.getItem_inputs().get(0).getItem().getCount());
        assertEquals(2000, decoded.getFluid_inputs().get(0).getAmount());
        assertEquals(150, decoded.getEntropy());
    }
}
