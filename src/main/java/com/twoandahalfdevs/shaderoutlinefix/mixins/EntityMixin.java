package com.twoandahalfdevs.shaderoutlinefix.mixins;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
  // TODO - use below to actually check for glowing haha
  //this.glowing || this.world.isRemote && this.getFlag(6)
  @Inject(method = "isGlowing", at = @At("RETURN"), cancellable = true)
  public void isGlowing(CallbackInfoReturnable<Boolean> cir) {
    cir.setReturnValue(false);
  }
}
