package io.github.eg0rmaffin.greatestsoup.content.hbm;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import io.github.eg0rmaffin.greatestsoup.GreatestSoupConfig;
import io.github.eg0rmaffin.greatestsoup.Tags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

/**
 * The detector's behaviour, all on the client: the player's radiation, the world time and the terrain above the
 * player are all known there, and the rules come from the server on login.
 * <ul>
 *     <li>Before a roll it clicks like a Geiger counter: rarely from 30 minutes before, often from 10, and it beeps
 *     through the last minute. The roll itself gives a sonar ping.</li>
 *     <li>While the player would be picked (irradiated enough and underground) the lens glows red, and every click
 *     comes twice, like a heartbeat.</li>
 *     <li>Outside surface worlds, where the roll never happens, it only gives off static now and then.</li>
 * </ul>
 */
@SideOnly(Side.CLIENT)
public final class MaskDetectorClient {

    private static final long FAR = 30 * 60 * 20;
    private static final long NEAR = 10 * 60 * 20;
    private static final long IMMINENT = 60 * 20;

    /** Read by the item model to show the red lens; true while the player would be picked by a roll. */
    public static boolean candidate;

    private final Random random = new Random();
    private int nextSound;
    private long lastRoll = -1;

    public static void registerModel(Item item) {
        item.addPropertyOverride(new ResourceLocation(Tags.MOD_ID, "candidate"), (stack, world, entity) ->
                candidate ? 1.0F : 0.0F);
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event.phase != TickEvent.Phase.END || mc.player == null || mc.world == null || mc.isGamePaused()) {
            return;
        }
        EntityPlayer player = mc.player;
        World world = mc.world;
        candidate = MaskManRules.isCandidate(player);
        if (!isCarried(player) || !GreatestSoupConfig.hbm.maskDetectorSounds) {
            lastRoll = -1;
            return;
        }
        if (nextSound > 0) {
            nextSound--;
        }
        if (!MaskManRules.enabled) {
            return;
        }
        if (!MaskManRules.appliesTo(world)) {
            // No roll here: the detector has nothing to listen to
            if (nextSound == 0) {
                play(sound("hbm", "misc.nullRadar"), 0.25F, 0.8F + random.nextFloat() * 0.3F);
                nextSound = 400 + random.nextInt(600);
            }
            lastRoll = -1;
            return;
        }

        // A new roll index means the roll tick has just passed
        long roll = world.getTotalWorldTime() / MaskManRules.delay;
        if (lastRoll >= 0 && roll != lastRoll) {
            play(sound("hbm", "block.sonarPing"), 1.0F, 0.6F);
            nextSound = 100;
        }
        lastRoll = roll;
        long until = MaskManRules.ticksUntilRoll(world);

        if (nextSound == 0 && until <= FAR) {
            if (until <= IMMINENT) {
                play(sound("hbm", "item.techBoop"), 0.8F, candidate ? 0.7F : 1.0F);
                nextSound = 20;
            } else {
                click(until <= NEAR ? 0.6F : 0.3F);
                nextSound = until <= NEAR ? 60 + random.nextInt(100) : 600 + random.nextInt(600);
            }
        }
    }

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        // Back to this side's own config until the next server sends its rules
        Minecraft.getMinecraft().addScheduledTask(MaskManRules::loadLocal);
        candidate = false;
    }

    private void click(float volume) {
        play(sound("hbm", "item.geiger" + (1 + random.nextInt(8))), volume, 1.0F);
        if (candidate) {
            play(sound("hbm", "item.geiger" + (1 + random.nextInt(8))), volume, 0.6F);
        }
    }

    private static boolean isCarried(EntityPlayer player) {
        for (int i = 0; i < 9; i++) {
            if (isDetector(player.inventory.getStackInSlot(i))) {
                return true;
            }
        }
        if (isDetector(player.getHeldItemOffhand())) {
            return true;
        }
        return Loader.isModLoaded("baubles") && isWornAsBauble(player);
    }

    private static boolean isWornAsBauble(EntityPlayer player) {
        IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
        for (int i = 0; i < baubles.getSlots(); i++) {
            if (isDetector(baubles.getStackInSlot(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDetector(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof MaskDetectorItem;
    }

    private static SoundEvent sound(String domain, String path) {
        return SoundEvent.REGISTRY.getObject(new ResourceLocation(domain, path));
    }

    private static void play(SoundEvent sound, float volume, float pitch) {
        if (sound == null) {
            return;
        }
        float scaled = volume * (float) GreatestSoupConfig.hbm.maskDetectorVolume;
        Minecraft mc = Minecraft.getMinecraft();
        mc.getSoundHandler().playSound(new PositionedSoundRecord(sound, SoundCategory.PLAYERS, scaled, pitch,
                mc.player.getPosition()));
    }

}
