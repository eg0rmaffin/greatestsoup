package io.github.eg0rmaffin.greatestsoup;

import io.github.eg0rmaffin.greatestsoup.content.hbm.MaskDetectorClient;
import io.github.eg0rmaffin.greatestsoup.content.hbm.MaskDetectorContent;
import io.github.eg0rmaffin.greatestsoup.content.hbm.MaskManRulesMessage;
import io.github.eg0rmaffin.greatestsoup.fixes.hbm.ShredderOreDictFix;
import io.github.eg0rmaffin.greatestsoup.fixes.minecraft.ArrowLayerInstaller;
import io.github.eg0rmaffin.greatestsoup.fixes.travelersbackpack.BackpackDeathHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Bug fixes, mod conflict fixes and small additions for The Greatest Soup modpack.
 * <p>
 * Each fix lives in its own package under {@code fixes} and each addition under {@code content}, one package per
 * mod they build on. Both are registered only when that mod is installed, and fixes can be switched off in
 * {@link GreatestSoupConfig}.
 * <p>
 * The mod adds items, so it is needed on both the server and the client.
 */
@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
        dependencies = "before:travelersbackpack;after:hbm;after:baubles")
public class GreatestSoup {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    public static SimpleNetworkWrapper NETWORK;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MOD_ID);
        NETWORK.registerMessage(MaskManRulesMessage.Handler.class, MaskManRulesMessage.class, 0, Side.CLIENT);

        // Fix classes link against the mods they patch, so they must not be loaded without them
        if (Loader.isModLoaded("travelersbackpack")) {
            MinecraftForge.EVENT_BUS.register(new BackpackDeathHandler());
        }
        if (Loader.isModLoaded("hbm")) {
            MinecraftForge.EVENT_BUS.register(new MaskDetectorContent());
            if (event.getSide().isClient()) {
                MinecraftForge.EVENT_BUS.register(new MaskDetectorContent.Client());
                MinecraftForge.EVENT_BUS.register(new MaskDetectorClient());
            }
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        // After HBM's postInit, which builds the shredder recipes this fix completes
        if (Loader.isModLoaded("hbm") && GreatestSoupConfig.hbm.shredderAllOreDictItems) {
            ShredderOreDictFix.apply();
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
