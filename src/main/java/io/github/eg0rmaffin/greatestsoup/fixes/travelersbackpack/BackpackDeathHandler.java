package io.github.eg0rmaffin.greatestsoup.fixes.travelersbackpack;

import com.tiviacz.travelersbackpack.capability.CapabilityUtils;
import com.tiviacz.travelersbackpack.capability.ITravelersBackpack;
import io.github.eg0rmaffin.greatestsoup.GreatestSoup;
import io.github.eg0rmaffin.greatestsoup.GreatestSoupConfig;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * EntityPlayerMP#onDeath fires LivingDeathEvent first, and only then collects the inventory into
 * PlayerDropsEvent. Traveler's Backpack handles the worn backpack on LivingDeathEvent (NORMAL priority) by
 * placing it as a block or spawning it as an item, so it never reaches PlayerDropsEvent and grave mods miss it.
 * <p>
 * Here the backpack is taken off before TB sees the death, held for the few calls in between, and handed to
 * PlayerDropsEvent as an ordinary drop.
 */
public class BackpackDeathHandler {

    /** Index of "Creeper" in TB's Reference.BACKPACK_NAMES; TB detonates it when its wearer dies. */
    private static final int CREEPER_BACKPACK_META = 64;

    /** Backpacks taken off dying players, waiting for their PlayerDropsEvent. */
    private final Map<UUID, ItemStack> pending = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void takeOffBackpack(LivingDeathEvent event) {
        if (!GreatestSoupConfig.travelersBackpack.backpackInGrave) {
            return;
        }
        if (!(event.getEntityLiving() instanceof EntityPlayerMP) || event.getEntityLiving() instanceof FakePlayer) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) event.getEntityLiving();
        // Mirrors the condition under which EntityPlayerMP#onDeath fires PlayerDropsEvent; otherwise leave it to TB
        if (player.world.getGameRules().getBoolean("keepInventory") || player.isSpectator()) {
            return;
        }
        if (!CapabilityUtils.isWearingBackpack(player)) {
            return;
        }
        ITravelersBackpack cap = CapabilityUtils.getCapability(player);
        pending.put(player.getUniqueID(), cap.getWearable());
        // With the slot empty, TB's own death handler finds nothing to do
        cap.removeWearable();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void afterDeath(LivingDeathEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) event.getEntityLiving();
        ItemStack stack = pending.get(player.getUniqueID());
        if (stack == null) {
            return;
        }
        if (event.isCanceled()) {
            // Another mod saved the player from dying: put the backpack back on
            pending.remove(player.getUniqueID());
            CapabilityUtils.getCapability(player).setWearable(stack);
            return;
        }
        // Keep TB's Creeper backpack behaviour, which its skipped handler would have done
        if (stack.getMetadata() == CREEPER_BACKPACK_META) {
            player.world.createExplosion(player, player.posX, player.posY, player.posZ, 4.0F, false);
        }
        CapabilityUtils.synchronise(player);
        CapabilityUtils.synchroniseToOthers(player);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void addToDrops(PlayerDropsEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = pending.remove(player.getUniqueID());
        if (stack != null) {
            event.getDrops().add(createDrop(player, stack));
        }
    }

    /** Safety net in case some mod skipped PlayerDropsEvent: the backpack is dropped where the player died. */
    @SubscribeEvent
    public void onClone(PlayerEvent.Clone event) {
        EntityPlayer original = event.getOriginal();
        ItemStack stack = pending.remove(original.getUniqueID());
        if (stack != null) {
            GreatestSoup.LOGGER.warn("PlayerDropsEvent never came for {}, dropping their backpack in the world",
                    original.getName());
            original.entityDropItem(stack, 1.0F);
        }
    }

    /** Same as EntityPlayer#dropItem(stack, true, false) does for the rest of the inventory, minus spawning. */
    private static EntityItem createDrop(EntityPlayer player, ItemStack stack) {
        EntityItem item = new EntityItem(player.world, player.posX, player.posY - 0.3D + player.getEyeHeight(),
                player.posZ, stack);
        item.setPickupDelay(40);
        float speed = player.getRNG().nextFloat() * 0.5F;
        float angle = player.getRNG().nextFloat() * (float) Math.PI * 2.0F;
        item.motionX = -MathHelper.sin(angle) * speed;
        item.motionZ = MathHelper.cos(angle) * speed;
        item.motionY = 0.2D;
        return item;
    }

}
