package io.github.eg0rmaffin.greatestsoup.content.hbm;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import io.github.eg0rmaffin.greatestsoup.Tags;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The "M" Detector: warns about HBM's Mask Man rolls without telling the player the rules. It works from the hotbar,
 * the offhand or a Baubles trinket slot; the clicking and the red lens are handled on the client by
 * {@link MaskDetectorClient}.
 */
@Optional.Interface(iface = "baubles.api.IBauble", modid = "baubles")
public class MaskDetectorItem extends Item implements IBauble {

    public static final String NAME = "mask_detector";

    public MaskDetectorItem() {
        setRegistryName(Tags.MOD_ID, NAME);
        setTranslationKey(Tags.MOD_ID + "." + NAME);
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.TOOLS);
    }

    @Override
    @Optional.Method(modid = "baubles")
    public BaubleType getBaubleType(ItemStack stack) {
        return BaubleType.TRINKET;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        String key = "item." + Tags.MOD_ID + "." + NAME + ".tooltip";
        tooltip.add(TextFormatting.GRAY + I18n.format(key + ".1"));
        tooltip.add(TextFormatting.GRAY + I18n.format(key + ".2"));
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            tooltip.add(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + I18n.format(key + ".lore.1"));
            tooltip.add(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + I18n.format(key + ".lore.2"));
        } else {
            tooltip.add(TextFormatting.DARK_GRAY + I18n.format(key + ".shift"));
        }
    }

}
