package io.github.eg0rmaffin.greatestsoup.fixes.minecraft;

import io.github.eg0rmaffin.greatestsoup.GreatestSoup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerArrow;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Swaps LayerArrow for {@link SafeLayerArrow} on every living entity renderer, players included.
 */
@SideOnly(Side.CLIENT)
public final class ArrowLayerInstaller {

    private ArrowLayerInstaller() {
    }

    /** Called once all mods have finished loading, so layers they add to existing renderers are in place. */
    public static void install() {
        RenderManager manager = Minecraft.getMinecraft().getRenderManager();
        List<Render<?>> renderers = new ArrayList<>(manager.entityRenderMap.values());
        renderers.addAll(manager.getSkinMap().values());

        Set<Render<?>> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        int replaced = 0;
        for (Render<?> render : renderers) {
            if (!(render instanceof RenderLivingBase) || !seen.add(render)) {
                continue;
            }
            RenderLivingBase<?> living = (RenderLivingBase<?>) render;
            List<LayerRenderer<?>> layers = ObfuscationReflectionHelper.getPrivateValue(RenderLivingBase.class,
                    living, "field_177097_h"); // layerRenderers
            for (int i = 0; i < layers.size(); i++) {
                // Exact class only: a mod's own subclass may render arrows its own way
                if (layers.get(i).getClass() == LayerArrow.class) {
                    layers.set(i, new SafeLayerArrow(living, (LayerArrow) layers.get(i)));
                    replaced++;
                }
            }
        }
        GreatestSoup.LOGGER.info("Replaced the arrow layer on {} entity renderers", replaced);
    }

}
