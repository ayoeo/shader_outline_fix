package com.twoandahalfdevs.shaderoutlinefix.mixins;

import com.twoandahalfdevs.shaderoutlinefix.RenderEnchantGlintEvent;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderItem.class)
public abstract class RenderItemMixin {
  @Inject(method = "renderEffect", at = @At("HEAD"), cancellable = true)
  private void renderEffect(IBakedModel model, CallbackInfo info) {
    RenderEnchantGlintEvent renderEnchantGlintEvent = new RenderEnchantGlintEvent();
    renderEnchantGlintEvent.call();
    if (renderEnchantGlintEvent.cancelled()) {
      info.cancel();
    }
  }
}
