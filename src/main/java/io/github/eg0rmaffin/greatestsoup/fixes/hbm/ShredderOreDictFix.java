package io.github.eg0rmaffin.greatestsoup.fixes.hbm;

import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.ShredderRecipes;
import com.hbm.items.ModItems;
import io.github.eg0rmaffin.greatestsoup.GreatestSoup;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * HBM's ShredderRecipes#registerShredder turns ore dictionary names into shredder recipes (oreX into two dustX,
 * ingotX into one, and so on), but two bugs leave most items of other mods shredding into scrap:
 * <ul>
 *     <li>it returns after the first item of each name, so when several mods register oreTin, only one of them
 *     gets a recipe;</li>
 *     <li>items registered with the wildcard meta are stored under meta 32767, and ComparableStack#hashCode
 *     includes the meta, so the shredder's exact-meta lookup never finds them.</li>
 * </ul>
 * Runs after HBM's postInit and repeats the same name rules for every item of every name, expanding wildcard items
 * into their variants. Only items without a recipe get one, so HBM's own overrides stay as they are. Ores that still
 * shred into scrap, because the pack has no dust for their material, are listed in the log.
 */
public final class ShredderOreDictFix {

    /** Name prefixes in the order registerShredder tries them: dust count, and whether it looks for tiny dust. */
    private static final Prefix[] PREFIXES = {
            new Prefix("ingot", 1, false),
            new Prefix("nugget", 1, true),
            new Prefix("ore", 2, false),
            new Prefix("block", 9, false),
            new Prefix("gem", 1, false),
            new Prefix("plate", 1, false),
            new Prefix("billet", 6, true),
            new Prefix("wire", 1, true),
            new Prefix("bolt", 1, true),
            new Prefix("pipe", 3, false),
            new Prefix("shell", 4, false),
            new Prefix("wireDense", 1, false),
            new Prefix("plateTriple", 3, false),
            new Prefix("plateSextuple", 6, false),
            new Prefix("compressed", 2, false),
            new Prefix("componentHeavy", 768, false),
    };

    private ShredderOreDictFix() {
    }

    public static void apply() {
        Map<ComparableStack, ItemStack> recipes = ShredderRecipes.shredderRecipes;
        Map<String, Integer> added = new TreeMap<>();
        Map<String, List<String>> oresWithoutDust = new TreeMap<>();

        for (String name : OreDictionary.getOreNames()) {
            List<ItemStack> matches = OreDictionary.getOres(name, false);
            if (matches.isEmpty()) {
                continue;
            }
            ItemStack output = outputFor(name);
            for (ItemStack match : matches) {
                for (ItemStack stack : variants(match)) {
                    ComparableStack key = new ComparableStack(stack).makeSingular();
                    if (recipes.containsKey(key)) {
                        continue;
                    }
                    if (!output.isEmpty()) {
                        recipes.put(key, output.copy());
                        added.merge(name, 1, Integer::sum);
                    } else if (name.startsWith("ore") && name.length() > 3) {
                        oresWithoutDust.computeIfAbsent(name, n -> new ArrayList<>()).add(describe(stack));
                    }
                }
            }
        }

        if (!added.isEmpty()) {
            // HBM builds its JEI list from the map once, on first use; make sure it includes the new recipes
            ShredderRecipes.jeiShredderRecipes = null;
        }
        int total = added.values().stream().mapToInt(Integer::intValue).sum();
        GreatestSoup.LOGGER.info("Added {} shredder recipes for ore dictionary items that HBM skipped: {}", total, added);
        for (Map.Entry<String, List<String>> entry : oresWithoutDust.entrySet()) {
            String material = entry.getKey().substring(3);
            GreatestSoup.LOGGER.info("Shredder: {} still gives scrap, the pack has no dust{} to make: {}",
                    entry.getKey(), material, entry.getValue());
        }
    }

    /** The shredder output for an ore dictionary name under HBM's rules, or empty if there is none. */
    private static ItemStack outputFor(String name) {
        for (Prefix prefix : PREFIXES) {
            if (!name.startsWith(prefix.name) || name.length() <= prefix.name.length()) {
                continue;
            }
            String material = name.substring(prefix.name.length());
            ItemStack dust = prefix.tiny ? ShredderRecipes.getTinyDustByName(material)
                    : ShredderRecipes.getDustByName(material);
            if (!dust.isEmpty() && dust.getItem() != ModItems.scrap) {
                dust.setCount(prefix.count);
                return dust;
            }
        }
        if (name.startsWith("dust") && name.length() > 4) {
            return new ItemStack(ModItems.dust);
        }
        return ItemStack.EMPTY;
    }

    /** The stack itself, or for a wildcard stack every variant its item lists. */
    private static List<ItemStack> variants(ItemStack stack) {
        if (stack.getMetadata() != OreDictionary.WILDCARD_VALUE) {
            return Collections.singletonList(stack);
        }
        NonNullList<ItemStack> subItems = NonNullList.create();
        try {
            stack.getItem().getSubItems(CreativeTabs.SEARCH, subItems);
        } catch (RuntimeException e) {
            // Some mods only expect this from the creative menu; fall back to the base variant
            subItems.clear();
        }
        if (subItems.isEmpty()) {
            return Collections.singletonList(new ItemStack(stack.getItem(), 1, 0));
        }
        return subItems;
    }

    private static String describe(ItemStack stack) {
        return stack.getItem().getRegistryName() + "@" + stack.getMetadata();
    }

    private static final class Prefix {

        final String name;
        final int count;
        final boolean tiny;

        Prefix(String name, int count, boolean tiny) {
            this.name = name;
            this.count = count;
            this.tiny = tiny;
        }

    }

}
