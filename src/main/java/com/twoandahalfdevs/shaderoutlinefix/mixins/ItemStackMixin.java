package com.twoandahalfdevs.shaderoutlinefix.mixins;

import com.twoandahalfdevs.shaderoutlinefix.RenderEnchantGlintEvent;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
  @Inject(method = "isItemEnchanted", at = @At("HEAD"), cancellable = true)
  private void oh(CallbackInfoReturnable<Boolean> cir) {
    RenderEnchantGlintEvent renderEnchantGlintEvent = new RenderEnchantGlintEvent();
    renderEnchantGlintEvent.call();
    if (renderEnchantGlintEvent.cancelled()) {
      cir.setReturnValue(false);
    }
  }
}
