package com.twoandahalfdevs.shaderoutlinefix.mixins;

import com.twoandahalfdevs.shaderoutlinefix.ResizeWindowEvent;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
  @Inject(method = "resize", at = @At("RETURN"))
  private void resize(int width, int height, CallbackInfo info) {
    new ResizeWindowEvent().call();
  }
}
