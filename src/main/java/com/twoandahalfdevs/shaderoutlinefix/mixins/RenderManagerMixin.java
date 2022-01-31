package com.twoandahalfdevs.shaderoutlinefix.mixins;

import com.twoandahalfdevs.shaderoutlinefix.PostRenderEntityEvent;
import com.twoandahalfdevs.shaderoutlinefix.PreRenderEntityEvent;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderManager.class)
public abstract class RenderManagerMixin {
  @Inject(method = "renderEntityStatic", at = @At("HEAD"))
  private void preRenderEntityStatic(Entity entityIn, float partialTicks, boolean p_188388_3_, CallbackInfo ci) {
    new PreRenderEntityEvent(entityIn).call();
  }

  @Inject(method = "renderEntityStatic", at = @At("TAIL"))
  private void postRenderEntityStatic(Entity entityIn, float partialTicks, boolean p_188388_3_, CallbackInfo ci) {
    new PostRenderEntityEvent(entityIn).call();
  }

  @Inject(method = "renderMultipass", at = @At("HEAD"))
  private void preRenderEntityMultipass(Entity entityIn, float p_188389_2_, CallbackInfo ci) {
    new PreRenderEntityEvent(entityIn).call();
  }

  @Inject(method = "renderMultipass", at = @At("TAIL"))
  private void postRenderEntityMultipass(Entity entityIn, float p_188389_2_, CallbackInfo ci) {
    new PostRenderEntityEvent(entityIn).call();
  }
}
