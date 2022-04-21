package com.twoandahalfdevs.shaderoutlinefix.mixins;

import com.twoandahalfdevs.shaderoutlinefix.ResizeWindowEvent;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class)
public class MinecraftMixin {
  @Shadow
  public int displayWidth;

  @Shadow
  public int displayHeight;

  @Inject(method = "resize", at = @At("HEAD"))
  private void resize(int width, int height, CallbackInfo ci) {
    new ResizeWindowEvent(width, height).call();
  }

  @Inject(method = "updateFramebufferSize", at = @At("TAIL"))
  private void updateFramebufferSize(CallbackInfo ci) {
    new ResizeWindowEvent(displayWidth, displayHeight).call();
  }
}
