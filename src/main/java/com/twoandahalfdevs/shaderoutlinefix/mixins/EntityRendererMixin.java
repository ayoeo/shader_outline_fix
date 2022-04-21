package com.twoandahalfdevs.shaderoutlinefix.mixins;

import com.mumfrey.liteloader.gl.GL;
import com.twoandahalfdevs.shaderoutlinefix.Outline;
import com.twoandahalfdevs.shaderoutlinefix.OutlineKt;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL20.GL_CURRENT_PROGRAM;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
  @Inject(method = "drawNameplate", at = @At("HEAD"), cancellable = true)
  private static void renderEntitiesHead(
      FontRenderer fontRendererIn,
      String str,
      float x,
      float y,
      float z,
      int verticalShift,
      float viewerYaw,
      float viewerPitch,
      boolean isThirdPersonFrontal,
      boolean isSneaking,
      CallbackInfo ci
  ) {
    ci.cancel();
    int current = GL11.glGetInteger(GL_CURRENT_PROGRAM);
    int currentFb = GL.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);

    GL20.glUseProgram(0);

    // Draw
    GlStateManager.pushMatrix();
    GlStateManager.translate(x, y, z);
    GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
    GlStateManager.rotate(-viewerYaw, 0.0F, 1.0F, 0.0F);
    GlStateManager.rotate((float) (isThirdPersonFrontal ? -1 : 1) * viewerPitch, 1.0F, 0.0F, 0.0F);
    GlStateManager.scale(-0.025F, -0.025F, 0.025F);
    GlStateManager.disableLighting();
    GlStateManager.depthMask(false);

    if (!isSneaking) {
      GlStateManager.disableDepth();
    }

    GlStateManager.enableBlend();
    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    int i = fontRendererIn.getStringWidth(str) / 2;
    GlStateManager.disableTexture2D();
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder bufferbuilder = tessellator.getBuffer();
    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
    bufferbuilder.pos(-i - 1, -1 + verticalShift, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
    bufferbuilder.pos(-i - 1, 8 + verticalShift, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
    bufferbuilder.pos(i + 1, 8 + verticalShift, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
    bufferbuilder.pos(i + 1, -1 + verticalShift, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
    tessellator.draw();
    GlStateManager.enableTexture2D();

    if (!isSneaking) {
      fontRendererIn.drawString(str, -fontRendererIn.getStringWidth(str) / 2, verticalShift, 553648127);
      GlStateManager.enableDepth();
    }

    GlStateManager.depthMask(true);
    fontRendererIn.drawString(str, -fontRendererIn.getStringWidth(str) / 2, verticalShift, isSneaking ? 553648127 : -1);
    GlStateManager.enableLighting();
    GlStateManager.disableBlend();
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    GlStateManager.popMatrix();

    GL20.glUseProgram(current);
    glBindFramebuffer(GL30.GL_FRAMEBUFFER, currentFb);
  }
}
