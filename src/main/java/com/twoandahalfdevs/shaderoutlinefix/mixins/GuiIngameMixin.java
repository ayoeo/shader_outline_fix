package com.twoandahalfdevs.shaderoutlinefix.mixins;

import com.twoandahalfdevs.shaderoutlinefix.Render2DEvent;
import com.twoandahalfdevs.shaderoutlinefix.RenderOverlayEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.Display;
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
    GlStateManager.clear(256);
    GlStateManager.matrixMode(5889);
    GlStateManager.loadIdentity();
    GlStateManager.ortho(0.0D, mc.displayWidth, mc.displayHeight, 0.0D, 1000.0D, 3000.0D);
    GlStateManager.matrixMode(5888);
    GlStateManager.loadIdentity();
    GlStateManager.translate(0.0F, 0.0F, -2000.0F);

    Render2DEvent render2DEvent = new Render2DEvent();
    render2DEvent.call();
  }
}
