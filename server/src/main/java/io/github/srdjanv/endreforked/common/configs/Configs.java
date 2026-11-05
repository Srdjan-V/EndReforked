package io.github.srdjanv.endreforked.common.configs;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RequiresMcRestart;

import io.github.srdjanv.endreforked.Tags;

@Config(modid = Tags.MODID, name = Tags.MODID + "/BasicConfigs", category = "")
public final class Configs {

    public static final ClientSideConfigs CLIENT_SIDE_CONFIGS = new ClientSideConfigs();

    public static class ClientSideConfigs {

        @Name("Show broken textures in jei for the EntropyWand")
        public boolean entropyWandRenderBrokenTextures = false;

        @Name("Panorama")
        @RequiresMcRestart
        @Comment({ "Main menu panorama" })
        public boolean panorama = true;
    }

    public static final ServerSideConfigs SERVER_SIDE_CONFIGS = new ServerSideConfigs();

    public static class ServerSideConfigs {

        public final EntityConfigs ENTITY_CONFIGS = new EntityConfigs();

        public static class EntityConfigs {

            public final WatcherConfigs WATCHER = new WatcherConfigs();

            public static class WatcherConfigs {

                @Name("Max escapes")
                @Comment("Max escapes from payer before getting hunted")
                @RequiresMcRestart
                public int max_escapes = 3;

                @Name("Hunt cooldown")
                @Comment("Time in ticks to forget last hunted player")
                @RequiresMcRestart
                public int hunt_cooldown = 4 * 60 * 20;
            }
        }

        // todo fixup configs
        @Name("ender")
        @RequiresMcRestart
        @Comment({ "EnchantBoost" })
        public int[] enchantBoost = { 1 };

        @Name("EntropyWand tool damage per conversion")
        @RequiresMcRestart
        public int entropyWandUseDamage = 2;

        @Name("New Villagers")
        @RequiresMcRestart
        @Comment({ "Allows to spawn" })
        public boolean spawnNewVillagers = true;

        @Name("Chest Loot")
        @RequiresMcRestart
        @Comment({ "Allows to fill." })
        public boolean chestLoot = true;

        @Name("Entity Loot")
        @RequiresMcRestart
        public boolean entityLoot = true;

        @Name("End Void Teleporter")
        @RequiresMcRestart
        @Comment({ "When player falls to void in The End, he teleports to the Overworld" })
        public boolean teleporterEnd = true;

        private ServerSideConfigs() {}
    }
}
