package com.twoandahalfdevs.shaderoutlinefix.mixins;

import com.twoandahalfdevs.shaderoutlinefix.RenderNametagEvent;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderLivingBase.class)
public abstract class RenderLivingBaseMixin<T extends EntityLivingBase> extends Render<T> {
  protected RenderLivingBaseMixin(RenderManager renderManager) {
    super(renderManager);
  }

  @Inject(method = "renderName(Lnet/minecraft/entity/EntityLivingBase;DDD)V", at = @At("HEAD"), cancellable = true)
  public void renderName(T entity, double x, double y, double z, CallbackInfo info) {
    RenderNametagEvent renderNametagEvent = new RenderNametagEvent();
    renderNametagEvent.call();

    if (renderNametagEvent.cancelled()) {
      info.cancel();
    }
  }
}
