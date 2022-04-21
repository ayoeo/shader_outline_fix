package com.twoandahalfdevs.shaderoutlinefix

import net.minecraft.client.renderer.culling.ICamera
import net.minecraft.entity.Entity
import net.minecraft.tileentity.TileEntity

class Render2DEvent : Event()

class RenderOverlayEvent : Event()

class RenderEntitiesEvent(val partialTicks: Float, val camera: ICamera) : Event()
class PostRenderEntitiesEvent(val partialTicks: Float, val camera: ICamera) : Event()

class PreRenderEntityEvent(val entity: Entity) : Event()
class PreRenderTileEntityEvent(val entity: TileEntity) : Event()

class PostRenderEntityEvent(val entity: Entity) : Event()
class PostRenderTileEntityEvent(val entity: TileEntity) : Event()

class RenderEnchantGlintEvent : EventCancellable()

class RenderNametagEvent : EventCancellable()

class ResizeWindowEvent(val w: Int, val h: Int) : Event()
