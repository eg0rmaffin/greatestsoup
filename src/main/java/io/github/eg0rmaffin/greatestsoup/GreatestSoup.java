package io.github.eg0rmaffin.greatestsoup;

import io.github.eg0rmaffin.greatestsoup.fixes.minecraft.ArrowLayerInstaller;
import io.github.eg0rmaffin.greatestsoup.fixes.travelersbackpack.BackpackDeathHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Bug fixes and mod conflict fixes for The Greatest Soup modpack.
 * <p>
 * Each fix lives in its own package under {@code fixes}, is registered only when the mod it targets is installed,
 * and can be switched off in {@link GreatestSoupConfig}.
 */
@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
        dependencies = "before:travelersbackpack",
        acceptableRemoteVersions = "*")
public class GreatestSoup {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Fix classes link against the mods they patch, so they must not be loaded without them
        if (Loader.isModLoaded("travelersbackpack")) {
            MinecraftForge.EVENT_BUS.register(new BackpackDeathHandler());
        }
    }

    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        // Client only: the installer links against rendering classes that a dedicated server doesn't have
        if (event.getSide().isClient()) {
            ArrowLayerInstaller.install();
        }
    }

}
