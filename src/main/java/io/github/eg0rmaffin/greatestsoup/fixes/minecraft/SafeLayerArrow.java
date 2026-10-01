package io.github.eg0rmaffin.greatestsoup.fixes.minecraft;

import io.github.eg0rmaffin.greatestsoup.GreatestSoup;
import io.github.eg0rmaffin.greatestsoup.GreatestSoupConfig;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerArrow;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Vanilla LayerArrow picks a random part of the model for each stuck arrow and then a random box of that part with
 * {@code random.nextInt(cubeList.size())}. Mods that add a part without boxes to a model (a mount point, for example)
 * make it throw "bound must be positive" whenever the entity has arrows in it. The exception escapes after
 * pushMatrix and with standard item lighting off, so every frame leaks a matrix until the GL stack overflows and
 * GUIs that draw the player, like the inventory, render white.
 * <p>
 * This copy of the layer skips parts without boxes. It replaces LayerArrow on every living renderer, and falls back
 * to the original layer when the fix is switched off.
 */
@SideOnly(Side.CLIENT)
public class SafeLayerArrow implements LayerRenderer<EntityLivingBase> {

    /** Model classes already reported to the log, to name the culprit once instead of every frame. */
    private static final Set<Class<?>> reportedModels = Collections.synchronizedSet(new HashSet<>());

    private final RenderLivingBase<?> renderer;
    private final LayerArrow original;

    public SafeLayerArrow(RenderLivingBase<?> renderer, LayerArrow original) {
        this.renderer = renderer;
        this.original = original;
    }

    @Override
    public void doRenderLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks,
                              float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (!GreatestSoupConfig.minecraft.safeArrowLayer) {
            original.doRenderLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw,
                    headPitch, scale);
            return;
        }
        int arrows = entity.getArrowCountInEntity();
        if (arrows <= 0) {
            return;
        }
        ModelBase model = renderer.getMainModel();
        List<ModelRenderer> partsWithBoxes = partsWithBoxes(model);
        if (partsWithBoxes.isEmpty()) {
            return;
        }

        // From here on, the same as LayerArrow apart from how the part is picked
        Entity arrow = new EntityTippedArrow(entity.world, entity.posX, entity.posY, entity.posZ);
        Random random = new Random(entity.getEntityId());
        RenderHelper.disableStandardItemLighting();
        for (int i = 0; i < arrows; ++i) {
            GlStateManager.pushMatrix();
            ModelRenderer part = model.getRandomModelBox(random);
            if (part.cubeList.isEmpty()) {
                part = partsWithBoxes.get(random.nextInt(partsWithBoxes.size()));
            }
            ModelBox box = part.cubeList.get(random.nextInt(part.cubeList.size()));
            part.postRender(0.0625F);
            float x = random.nextFloat();
            float y = random.nextFloat();
            float z = random.nextFloat();
            GlStateManager.translate((box.posX1 + (box.posX2 - box.posX1) * x) / 16.0F,
                    (box.posY1 + (box.posY2 - box.posY1) * y) / 16.0F,
                    (box.posZ1 + (box.posZ2 - box.posZ1) * z) / 16.0F);
            x = -(x * 2.0F - 1.0F);
            y = -(y * 2.0F - 1.0F);
            z = -(z * 2.0F - 1.0F);
            float horizontal = MathHelper.sqrt(x * x + z * z);
            arrow.rotationYaw = (float) (Math.atan2(x, z) * (180D / Math.PI));
            arrow.rotationPitch = (float) (Math.atan2(y, horizontal) * (180D / Math.PI));
            arrow.prevRotationYaw = arrow.rotationYaw;
            arrow.prevRotationPitch = arrow.rotationPitch;
            renderer.getRenderManager().renderEntity(arrow, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, false);
            GlStateManager.popMatrix();
        }
        RenderHelper.enableStandardItemLighting();
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }

    private static List<ModelRenderer> partsWithBoxes(ModelBase model) {
        List<ModelRenderer> parts = new ArrayList<>(model.boxList.size());
        List<String> empty = null;
        for (ModelRenderer part : model.boxList) {
            if (!part.cubeList.isEmpty()) {
                parts.add(part);
            } else if (!reportedModels.contains(model.getClass())) {
                if (empty == null) {
                    empty = new ArrayList<>();
                }
                empty.add(part.boxName != null ? part.boxName : part.getClass().getName());
            }
        }
        if (empty != null && reportedModels.add(model.getClass())) {
            GreatestSoup.LOGGER.info("Arrows stuck in {} skip its parts without boxes: {}",
                    model.getClass().getName(), empty);
        }
        return parts;
    }

}
