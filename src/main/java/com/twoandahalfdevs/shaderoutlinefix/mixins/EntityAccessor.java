package com.twoandahalfdevs.shaderoutlinefix.mixins;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
  @Accessor
  boolean getGlowing();

  @Invoker
  boolean invokeGetFlag(int flag);
}
