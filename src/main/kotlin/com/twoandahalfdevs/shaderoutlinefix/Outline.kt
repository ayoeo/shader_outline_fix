package com.twoandahalfdevs.shaderoutlinefix

import com.twoandahalfdevs.shaderoutlinefix.mixins.EntityAccessor
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper.*
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.culling.ICamera
import net.minecraft.client.shader.Framebuffer
import net.minecraft.client.shader.ShaderLoader
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World
import org.lwjgl.opengl.GL11.glGetInteger
import org.lwjgl.opengl.GL30

val minecraft: Minecraft
  get() = Minecraft.getMinecraft()

val w
  get() = minecraft.displayWidth
val h
  get() = minecraft.displayHeight

val player: EntityPlayerSP?
  get() = minecraft.player

val world: World?
  get() = minecraft.world

interface Shader {
  fun shader(): Int
}

object Outline {
  private var renderingEsp = false
  private val framebuffer = run {
    val framebuffer = Framebuffer((w * 1.0).toInt(), (h * 1.0).toInt(), false)
    framebuffer
  }


  //  private val framebuffer = Framebuffer(w, h, true)
  private val outlineProgram = run {
    val vert = ShaderLoader.loadShader(
      minecraft.resourceManager,
      ShaderLoader.ShaderType.VERTEX,
      "esp"
    )
    val frag = ShaderLoader.loadShader(
      minecraft.resourceManager,
      ShaderLoader.ShaderType.FRAGMENT,
      "espold"
    )
    val program = glCreateProgram()
    glAttachShader(program, (vert as Shader).shader())
    glAttachShader(program, (frag as Shader).shader())
    glLinkProgram(program)

    program
  }

  private val colorUniform = GLUniform.Colour(
    glGetUniformLocation(outlineProgram, "outlineColor")
  )

  init {
    listenFor<ResizeWindowEvent> {
      this.framebuffer.createBindFramebuffer(w, h)
    }

    listenFor<RenderEntitiesEvent> { event ->
      this.renderingEsp = true
      this.renderEsp(event.partialTicks, event.camera)
      this.renderingEsp = false
    }

    // Prevent glitches with enchanted items
    listenFor<RenderEnchantGlintEvent>
    {
      if (this.renderingEsp) {
        it.cancel()
      }
    }

    listenFor<RenderNametagEvent>
    {
      if (this.renderingEsp) {
        it.cancel()
      }
    }

    listenFor<Render2DEvent>
    {
      glUseProgram(this.outlineProgram)
      this.framebuffer.bindFramebufferTexture()

      colorUniform.set(Colour(0f, 0f, 0f, 0f))
      blitScreenImage()

      this.framebuffer.unbindFramebufferTexture()
      glUseProgram(0)
    }
  }

  data class ColorMapping(val stencilValue: Int, val outColor: Colour)

  // TODO - add all the colors that we need for glowing, check glow color, whatever
  //  maybe change mythic color to make it MORE orange to be easier to see haha
  private val colours = listOf(
    ColorMapping(TextFormatting.DARK_RED.ordinal, Colour.hex(0xFFAA0000)),
    ColorMapping(TextFormatting.RED.ordinal, Colour.hex(0xFFFF5555)),
    ColorMapping(TextFormatting.GOLD.ordinal, Colour.hex(0xFFFF8C00)),
    ColorMapping(TextFormatting.YELLOW.ordinal, Colour.hex(0xFFEDE553)),
    ColorMapping(TextFormatting.DARK_GREEN.ordinal, Colour.hex(0xFF00AA00)),
    ColorMapping(TextFormatting.GREEN.ordinal, Colour.hex(0xFF55FF55)),
    ColorMapping(TextFormatting.AQUA.ordinal, Colour.hex(0xFF55FFFF)),
    ColorMapping(TextFormatting.DARK_AQUA.ordinal, Colour.hex(0xFF00AAAA)),
    ColorMapping(TextFormatting.DARK_BLUE.ordinal, Colour.hex(0xFF0000AA)),
    ColorMapping(TextFormatting.BLUE.ordinal, Colour.hex(0xFF5555FF)),
    ColorMapping(TextFormatting.LIGHT_PURPLE.ordinal, Colour.hex(0xFFFF55FF)),
    ColorMapping(TextFormatting.DARK_PURPLE.ordinal, Colour.hex(0xFFAA00AA)),
    ColorMapping(TextFormatting.GRAY.ordinal, Colour.hex(0xFFAAAAAA)),
    ColorMapping(TextFormatting.DARK_GRAY.ordinal, Colour.hex(0xFF555555)),
    ColorMapping(TextFormatting.BLACK.ordinal, Colour.hex(0xFF000000)),
    ColorMapping(TextFormatting.WHITE.ordinal, Colour.hex(0xFFFFFFFF))
  )

  private fun renderEsp(partialTicks: Float, camera: ICamera) {
    val currentFb = glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING)

    // Bind esp framebuffer
    this.framebuffer.framebufferClear()
    this.framebuffer.bindFramebuffer(true)
    glUseProgram(this.outlineProgram)

    GlStateManager.disableFog()
    GlStateManager.disableLighting()
    RenderHelper.disableStandardItemLighting()

    val viewEntity = minecraft.renderViewEntity
    val sleeping = viewEntity is EntityLivingBase && viewEntity.isPlayerSleeping

    // Render all entities just like minecraft would do
    val world = world ?: return
    world.loadedEntityList
      .filterIsInstance<Entity>()
      .filter { entity ->
        (entity as EntityAccessor).glowing || entity.world.isRemote && (entity as EntityAccessor).invokeGetFlag(6)
      }
      .forEach { entity ->
        val x = viewEntity!!.prevPosX + (viewEntity.posX - viewEntity.prevPosX) * partialTicks
        val y = viewEntity.prevPosY + (viewEntity.posY - viewEntity.prevPosY) * partialTicks
        val z = viewEntity.prevPosZ + (viewEntity.posZ - viewEntity.prevPosZ) * partialTicks

        val canSeeEntity = entity.isInRangeToRender3d(x, y, z)

        val shouldRenderSelf = minecraft.gameSettings.thirdPersonView != 0 || sleeping
        if ((entity !== minecraft.renderViewEntity || shouldRenderSelf) && canSeeEntity) {
          val color = entity.team?.color?.ordinal ?: TextFormatting.WHITE.ordinal
          colorUniform.set(colours.first { it.stencilValue == color }.outColor)
          minecraft.renderManager.renderEntityStatic(entity, partialTicks, false)
        }
      }

    // Please resume your normal minecrafting
    RenderHelper.enableStandardItemLighting()
    GlStateManager.enableLighting()
    GlStateManager.enableFog()
    glUseProgram(0)

    // Re-bind minecraft framebuffer
    glBindFramebuffer(GL_FRAMEBUFFER, currentFb)
  }
}
