package io.github.eg0rmaffin.tbgravefix;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Makes the backpack worn in Traveler's Backpack's capability slot part of the player's death drops,
 * so grave mods that work off PlayerDropsEvent (Corail Tombstone) pick it up.
 * <p>
 * Server-side only: clients without this mod can join a server that has it.
 */
@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
        dependencies = "before:travelersbackpack",
        acceptableRemoteVersions = "*")
public class TBGraveFix {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // The handler links against Traveler's Backpack classes, so it must not be loaded without that mod
        if (!Loader.isModLoaded("travelersbackpack")) {
            LOGGER.warn("Traveler's Backpack is not installed, {} will do nothing", Tags.MOD_NAME);
            return;
        }
        MinecraftForge.EVENT_BUS.register(new BackpackDeathHandler());
    }

}
