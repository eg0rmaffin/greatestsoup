package io.github.eg0rmaffin.greatestsoup;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Stored in config/greatestsoup.cfg, also editable in game from the mod list. Fixes check their switch on every use,
 * so changes apply without a restart.
 */
@Config(modid = Tags.MOD_ID)
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class GreatestSoupConfig {

    @Config.Comment("HBM's Nuclear Tech fixes")
    public static final Hbm hbm = new Hbm();

    @Config.Comment("Minecraft fixes, for bugs that other mods trigger in vanilla code")
    public static final Vanilla minecraft = new Vanilla();

    @Config.Comment("Traveler's Backpack fixes")
    public static final TravelersBackpack travelersBackpack = new TravelersBackpack();

    public static class Hbm {

        @Config.Comment({"Give every ore dictionary item a shredder recipe under HBM's own rules (oreX into two dustX and",
                "so on). HBM only does it for the first item of each name, so ores from other mods shred into scrap.",
                "Ores that still give scrap because the pack has no dust for them are listed in the log"})
        @Config.RequiresMcRestart
        public boolean shredderAllOreDictItems = true;

        @Config.Comment("Let the \"M\" Detector click, beep and hum. The red lens works either way. Client side")
        public boolean maskDetectorSounds = true;

        @Config.Comment("Volume of the \"M\" Detector's sounds. Client side")
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public double maskDetectorVolume = 0.7;

    }

    public static class Vanilla {

        @Config.Comment({"Render arrows stuck in entities without crashing on model parts that have no boxes.",
                "Without it, a mod that adds such a part to the player model turns the inventory screen white",
                "while arrows are stuck in the player. Client side"})
        public boolean safeArrowLayer = true;

    }

    public static class TravelersBackpack {

        @Config.Comment({"Put the worn backpack into the death drops instead of placing or dropping it separately,",
                "so grave mods like Corail Tombstone collect it"})
        public boolean backpackInGrave = true;

    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(Tags.MOD_ID)) {
            ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE);
        }
    }

}
