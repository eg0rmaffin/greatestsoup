package io.github.eg0rmaffin.greatestsoup.content.hbm;

import io.github.eg0rmaffin.greatestsoup.GreatestSoup;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Registers the detector and keeps the client's copy of the Mask Man rules in line with the server. */
public class MaskDetectorContent {

    public static final MaskDetectorItem DETECTOR = new MaskDetectorItem();

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(DETECTOR);
    }

    @SubscribeEvent
    public void sendRules(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            GreatestSoup.NETWORK.sendTo(MaskManRulesMessage.fromServerRules(), (EntityPlayerMP) event.player);
        }
    }

    /** Client side registrations, kept apart so a dedicated server never loads them. */
    @SideOnly(Side.CLIENT)
    public static class Client {

        @SubscribeEvent
        public void registerModels(ModelRegistryEvent event) {
            MaskDetectorClient.registerModel(DETECTOR);
        }

    }

}
