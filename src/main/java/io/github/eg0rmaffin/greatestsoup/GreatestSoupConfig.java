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

    @Config.Comment("Traveler's Backpack fixes")
    public static final TravelersBackpack travelersBackpack = new TravelersBackpack();

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
