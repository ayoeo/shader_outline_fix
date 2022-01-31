package com.twoandahalfdevs.shaderoutlinefix.mixins;

import com.twoandahalfdevs.shaderoutlinefix.Render2DEvent;
import com.twoandahalfdevs.shaderoutlinefix.RenderOverlayEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class GuiIngameMixin {
  @Shadow
  @Final
  private Minecraft mc;

  @Shadow
  public abstract void setupOverlayRendering();

  @Inject(method = "updateCameraAndRender",
      at = @At(
          value = "INVOKE",
          shift = At.Shift.AFTER,
          target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V"
      )
  )
  private void preGuiRender(float f, long f1, CallbackInfo ci) {
    setupOverlayRendering();

    GlStateManager.pushMatrix();
    double scaleFactor = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
    double scale = scaleFactor / Math.pow(scaleFactor, 2.0);
    GlStateManager.scale(scale, scale, 1);

    Render2DEvent render2DEvent = new Render2DEvent();
    render2DEvent.call();

    GlStateManager.popMatrix();
  }
}
