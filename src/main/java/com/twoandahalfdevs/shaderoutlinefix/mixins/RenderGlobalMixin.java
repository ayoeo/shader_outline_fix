package com.twoandahalfdevs.shaderoutlinefix.mixins;

import com.twoandahalfdevs.shaderoutlinefix.RenderEntitiesEvent;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public class RenderGlobalMixin {
  @Inject(method = "renderEntities",
      at = @At(
          value = "INVOKE_STRING",
          target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V",
          args = "ldc=entities"
      )
  )
  private void renderEntitiesHead(
      Entity renderViewEntity,
      ICamera camera,
      float partialTicks,
      CallbackInfo info
  ) {
    new RenderEntitiesEvent(partialTicks, camera).call();
  }
}
