package io.github.eg0rmaffin.greatestsoup.fixes.minecraft;

import io.github.eg0rmaffin.greatestsoup.GreatestSoup;
import io.github.eg0rmaffin.greatestsoup.GreatestSoupConfig;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerArrow;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Vanilla LayerArrow picks a random part of the model from {@link ModelBase#boxList} for each stuck arrow, and then a
 * random box of that part with {@code random.nextInt(cubeList.size())}. A part without boxes makes it throw
 * "bound must be positive" whenever the entity has arrows in it. HBM's Nuclear Tech Extended adds such a part to the
 * player model (EgonBackpackRenderer, which draws an OBJ model instead of boxes). The exception escapes after
 * pushMatrix, so every frame leaks a matrix until the GL stack overflows and GUIs that draw the player render garbage
 * or turn white.
 * <p>
 * This subclass hides the parts without boxes from boxList while the vanilla layer renders, so the vanilla code, and
 * any mixins other mods apply to it (VintageFix fixes a lighting leak there), still do the rendering. Those parts are drawn as children of other
 * parts, not through boxList, so they keep rendering as usual.
 */
@SideOnly(Side.CLIENT)
public class SafeLayerArrow extends LayerArrow {

    /** Model classes already reported to the log, to name the culprit once instead of every frame. */
    private static final Set<Class<?>> reportedModels = Collections.synchronizedSet(new HashSet<>());

    private final RenderLivingBase<?> renderer;

    public SafeLayerArrow(RenderLivingBase<?> renderer) {
        super(renderer);
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks,
                              float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ModelBase model = renderer.getMainModel();
        if (!GreatestSoupConfig.minecraft.safeArrowLayer || entity.getArrowCountInEntity() <= 0
                || !hasPartWithoutBoxes(model)) {
            super.doRenderLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch,
                    scale);
            return;
        }
        List<ModelRenderer> parts = new ArrayList<>(model.boxList);
        model.boxList.removeIf(part -> part.cubeList.isEmpty());
        try {
            // With no part left to attach arrows to, the vanilla layer would throw on an empty list instead
            if (!model.boxList.isEmpty()) {
                super.doRenderLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw,
                        headPitch, scale);
            }
        } finally {
            model.boxList.clear();
            model.boxList.addAll(parts);
        }
    }

    private static boolean hasPartWithoutBoxes(ModelBase model) {
        List<String> empty = null;
        for (ModelRenderer part : model.boxList) {
            if (part.cubeList.isEmpty()) {
                if (reportedModels.contains(model.getClass())) {
                    return true;
                }
                if (empty == null) {
                    empty = new ArrayList<>();
                }
                empty.add(part.boxName != null ? part.boxName : part.getClass().getName());
            }
        }
        if (empty == null) {
            return false;
        }
        if (reportedModels.add(model.getClass())) {
            GreatestSoup.LOGGER.info("Arrows stuck in {} skip its parts without boxes: {}",
                    model.getClass().getName(), empty);
        }
        return true;
    }

}
