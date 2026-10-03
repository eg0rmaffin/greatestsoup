package io.github.eg0rmaffin.greatestsoup.content.hbm;

import com.hbm.capability.HbmLivingProps;
import com.hbm.config.MobConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/**
 * HBM's Mask Man spawn rules, as BossSpawnHandler applies them: once every {@code delay} ticks of total world time,
 * in a surface world, a random player with at least {@code minRad} of radiation who is underground (more than three
 * blocks under the surface) gets a 1 in {@code chance} roll.
 * <p>
 * The values come from HBM's MobConfig, so they follow hbm.cfg. On a client connected to a server they are replaced
 * by the server's values, which are the ones the roll actually uses.
 */
public final class MaskManRules {

    public static boolean enabled;
    public static int delay;
    public static int chance;
    public static int minRad;
    public static boolean underground;

    static {
        loadLocal();
    }

    private MaskManRules() {
    }

    /** Takes the values from this side's HBM config. */
    public static void loadLocal() {
        set(MobConfig.enableMaskman, MobConfig.maskmanDelay, MobConfig.maskmanChance, MobConfig.maskmanMinRad,
                MobConfig.maskmanUnderground);
    }

    public static void set(boolean enabled, int delay, int chance, int minRad, boolean underground) {
        MaskManRules.enabled = enabled;
        MaskManRules.delay = Math.max(1, delay);
        MaskManRules.chance = Math.max(1, chance);
        MaskManRules.minRad = minRad;
        MaskManRules.underground = underground;
    }

    /** Whether the world takes part in the roll at all. */
    public static boolean appliesTo(World world) {
        return enabled && world.provider.isSurfaceWorld();
    }

    /** Ticks of total world time until the next roll, 0 on the tick of the roll itself. */
    public static long ticksUntilRoll(World world) {
        long remainder = world.getTotalWorldTime() % delay;
        return remainder == 0 ? 0 : delay - remainder;
    }

    /** Whether the player could be picked if the roll happened now. */
    public static boolean isCandidate(EntityPlayer player) {
        World world = player.world;
        if (!appliesTo(world) || HbmLivingProps.getRadiation(player) < minRad) {
            return false;
        }
        return !underground || world.getHeight((int) player.posX, (int) player.posZ) > player.posY + 3.0;
    }

}
