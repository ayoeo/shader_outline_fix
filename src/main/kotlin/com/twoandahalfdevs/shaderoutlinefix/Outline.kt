package com.twoandahalfdevs.shaderoutlinefix

import com.mumfrey.liteloader.gl.GL
import com.twoandahalfdevs.shaderoutlinefix.mixins.EntityAccessor
import com.twoandahalfdevs.shaderoutlinefix.mixins.RenderManagerAccessor
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper.*
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.culling.ICamera
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.shader.Framebuffer
import net.minecraft.client.shader.ShaderLoader
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.scoreboard.Team
import net.minecraft.scoreboard.Team.EnumVisible
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL20.GL_CURRENT_PROGRAM
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
    val framebuffer = Framebuffer(w, h, false)
    framebuffer
  }

  private val nametagFramebuffer = run {
    val framebuffer = Framebuffer(w, h, true)
    framebuffer.setFramebufferColor(0f, 0f, 0f, 0f)
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

  private val nametagProgram = run {
    val vert = ShaderLoader.loadShader(
      minecraft.resourceManager,
      ShaderLoader.ShaderType.VERTEX,
      "good"
    )
    val frag = ShaderLoader.loadShader(
      minecraft.resourceManager,
      ShaderLoader.ShaderType.FRAGMENT,
      "good"
    )
    val program = glCreateProgram()
    glAttachShader(program, (vert as Shader).shader())
    glAttachShader(program, (frag as Shader).shader())
    glLinkProgram(program)

    program
  }

  private val texturedUniform = GLUniform.Float(
    glGetUniformLocation(nametagProgram, "textured")
  )

  private val colorUniform = GLUniform.Colour(
    glGetUniformLocation(outlineProgram, "outlineColor")
  )

  private val borderSizeUnifrom = GLUniform.Float(
    glGetUniformLocation(outlineProgram, "borderSize")
  )

  init {
    listenFor<ResizeWindowEvent> {
      this.framebuffer.createBindFramebuffer(it.w, it.h)
      this.nametagFramebuffer.createBindFramebuffer(it.w, it.h)
    }

    listenFor<RenderEntitiesEvent> { event ->
      this.renderingEsp = true
      this.renderEsp(event.partialTicks, event.camera)
      this.renderingEsp = false
    }

    listenFor<PostRenderEntitiesEvent> {
      if (!LiteModShadersOutlineFix.mod.nametagFix) {
        return@listenFor
      }
      val currentFb = GL.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING)
      glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, currentFb)
      glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, nametagFramebuffer.framebufferObject)
      GL30.glBlitFramebuffer(
        0,
        0,
        w,
        h,
        0,
        0,
        w,
        h,
        GL_DEPTH_BUFFER_BIT,
        GL_NEAREST
      )
      nametagFramebuffer.bindFramebuffer(false)

      val viewEntity = minecraft.renderViewEntity
      val sleeping = viewEntity is EntityLivingBase && viewEntity.isPlayerSleeping

      // Render all entities just like minecraft would do
      val world = world ?: return@listenFor
      for (i in 0..1) {
        world.loadedEntityList
          .filterIsInstance<Entity>()
          .forEach { entity ->
            val d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * it.partialTicks
            var d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * it.partialTicks
            val d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * it.partialTicks

            val x = viewEntity!!.prevPosX + (viewEntity.posX - viewEntity.prevPosX) * it.partialTicks
            val y = viewEntity.prevPosY + (viewEntity.posY - viewEntity.prevPosY) * it.partialTicks
            val z = viewEntity.prevPosZ + (viewEntity.posZ - viewEntity.prevPosZ) * it.partialTicks

            val flag =
              minecraft.renderManager.shouldRender(
                entity,
                it.camera,
                x,
                y,
                z
              ) || entity.isRidingOrBeingRiddenBy(player!!)

            val shouldRenderSelf = minecraft.gameSettings.thirdPersonView != 0 || sleeping
            if ((entity !== minecraft.renderViewEntity || shouldRenderSelf) && flag) {
              val dis = entity.getDistanceSqToEntity(minecraft.renderManager.renderViewEntity)

              val maxDistance = 64
              if (dis <= (maxDistance * maxDistance) && canRenderName(entity)) {
                val isSneaking = entity.isSneaking
                val isThirdPersonFrontal = minecraft.renderManager.options.thirdPersonView == 2
                val f2 = entity.height + 0.5F - (if (isSneaking) 0.25F else 0.0F)
                val str = entity.displayName.formattedText
                val verticalShift = if ("deadmau5" == str) -10 else 0

                if (entity is EntityPlayer && dis < 100.0) {
                  val scoreboard = entity.worldScoreboard;
                  val scoreobjective = scoreboard.getObjectiveInDisplaySlot(2);

                  if (scoreobjective != null) {
                    val score = scoreboard.getOrCreateScore(entity.getName(), scoreobjective);
                    drawNametag(
                      score.scorePoints.toString() + " " + scoreobjective.displayName,
                      d0 - (minecraft.renderManager as RenderManagerAccessor).renderPosX,
                      d1 + f2 - (minecraft.renderManager as RenderManagerAccessor).renderPosY,
                      d2 - (minecraft.renderManager as RenderManagerAccessor).renderPosZ,
                      verticalShift,
                      minecraft.renderManager.playerViewY,
                      minecraft.renderManager.playerViewX,
                      isThirdPersonFrontal,
                      isSneaking,
                      i == 0
                    )
                    d1 += (minecraft.fontRenderer.FONT_HEIGHT * 1.15 * 0.025);
                  }
                }
                drawNametag(
                  entity.displayName.formattedText,
                  d0 - (minecraft.renderManager as RenderManagerAccessor).renderPosX,
                  d1 + f2 - (minecraft.renderManager as RenderManagerAccessor).renderPosY,
                  d2 - (minecraft.renderManager as RenderManagerAccessor).renderPosZ,
                  verticalShift,
                  minecraft.renderManager.playerViewY,
                  minecraft.renderManager.playerViewX,
                  isThirdPersonFrontal,
                  isSneaking,
                  i == 0
                )
              }
            }
          }
      }

      glBindFramebuffer(GL30.GL_FRAMEBUFFER, currentFb)
    }

    // Prevent glitches with enchanted items
    listenFor<RenderEnchantGlintEvent> {
      if (this.renderingEsp) {
        it.cancel()
      }
    }

    listenFor<RenderNametagEvent> {
//      if (this.renderingEsp) {
      if (LiteModShadersOutlineFix.mod.nametagFix) {
        it.cancel()
      }
//      }
    }

    listenFor<Render2DEvent> {
      glUseProgram(this.outlineProgram)
      this.framebuffer.bindFramebufferTexture()

      colorUniform.set(Colour(0f, 0f, 0f, 0f))
      borderSizeUnifrom.set((minecraft.displayWidth / 4800f) * LiteModShadersOutlineFix.mod.borderSize)
      blitScreenImage()

      this.framebuffer.unbindFramebufferTexture()
      glUseProgram(0)

      if (LiteModShadersOutlineFix.mod.nametagFix) {
        this.nametagFramebuffer.bindFramebufferTexture()
        glColor4f(1f, 1f, 1f, 1f)

        blitScreenImage()
        this.nametagFramebuffer.unbindFramebufferTexture()
      }
    }
  }

  private fun canRenderLivingBase(entity: EntityLivingBase): Boolean {
    val entityplayersp = Minecraft.getMinecraft().player
    val flag = !entity.isInvisibleToPlayer(entityplayersp)
    if (entity !== entityplayersp) {
      val team: Team? = entity.team
      val team1 = entityplayersp.team
      if (team != null) {
        return when (team.nameTagVisibility) {
          EnumVisible.ALWAYS -> flag
          EnumVisible.NEVER -> false
          EnumVisible.HIDE_FOR_OTHER_TEAMS -> if (team1 == null) flag else team.isSameTeam(team1) && (team.seeFriendlyInvisiblesEnabled || flag)
          EnumVisible.HIDE_FOR_OWN_TEAM -> if (team1 == null) flag else !team.isSameTeam(team1) && flag
          else -> true
        }
      }
    }
    return Minecraft.isGuiEnabled() && entity !== minecraft.renderManager.renderViewEntity && flag && !entity.isBeingRidden
  }

  private fun canRenderName(entity: Entity): Boolean {
    return when (entity) {
      is EntityArmorStand -> entity.alwaysRenderNameTag

      is EntityLiving -> canRenderLivingBase(entity) &&
        (entity.alwaysRenderNameTagForRender ||
          entity.hasCustomName() &&
          entity == minecraft.renderManager.pointedEntity)

      is EntityLivingBase -> canRenderLivingBase(entity)

      else -> entity.alwaysRenderNameTagForRender && entity.hasCustomName()
    }
  }

  private fun drawNametag(
    str: String,
    x: Double,
    y: Double,
    z: Double,
    verticalShift: Int,
    viewerYaw: Float,
    viewerPitch: Float,
    isThirdPersonFrontal: Boolean,
    isSneaking: Boolean,
    firstPass: Boolean
  ) {
    val current = glGetInteger(GL_CURRENT_PROGRAM)
    GL20.glUseProgram(nametagProgram)
    texturedUniform.set(0f)

    // Draw
    GlStateManager.pushMatrix()
    GlStateManager.translate(x, y, z)
    GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F)
    GlStateManager.rotate(-viewerYaw, 0.0F, 1.0F, 0.0F)
    GlStateManager.rotate((if (isThirdPersonFrontal) -1 else 1) * viewerPitch, 1.0F, 0.0F, 0.0F)
    GlStateManager.scale(-0.025F, -0.025F, 0.025F)
    GlStateManager.disableLighting()
    GlStateManager.disableBlend()
    GlStateManager.tryBlendFuncSeparate(
      GlStateManager.SourceFactor.SRC_ALPHA,
      GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
      GlStateManager.SourceFactor.ONE,
      GlStateManager.DestFactor.ZERO
    )
    val fontRendererIn = minecraft.fontRenderer

    if (firstPass) {
      GlStateManager.depthMask(false)
      GlStateManager.disableDepth()

      val i = fontRendererIn.getStringWidth(str) / 2
      GlStateManager.disableTexture2D()
      val tessellator = Tessellator.getInstance()
      val bufferbuilder = tessellator.buffer
      bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR)
      bufferbuilder.pos((-i - 1).toDouble(), (-1 + verticalShift).toDouble(), 0.0).color(0.0F, 0.0F, 0.0F, 0.25F)
        .endVertex()
      bufferbuilder.pos((-i - 1).toDouble(), (8 + verticalShift).toDouble(), 0.0).color(0.0F, 0.0F, 0.0F, 0.25F)
        .endVertex()
      bufferbuilder.pos((i + 1).toDouble(), (8 + verticalShift).toDouble(), 0.0).color(0.0F, 0.0F, 0.0F, 0.25F)
        .endVertex()
      bufferbuilder.pos((i + 1).toDouble(), (-1 + verticalShift).toDouble(), 0.0).color(0.0F, 0.0F, 0.0F, 0.25F)
        .endVertex()
      tessellator.draw()
      GlStateManager.enableTexture2D()

      // Behind wall nametag for things that aren't sneaking
      if (!isSneaking) {
        texturedUniform.set(1f)
        fontRendererIn.drawString(str, -fontRendererIn.getStringWidth(str) / 2, verticalShift, 553648127)
      }
    } else {
      GlStateManager.enableDepth()
      GlStateManager.depthMask(true)
      texturedUniform.set(1f)
      fontRendererIn.drawString(
        str,
        -fontRendererIn.getStringWidth(str) / 2,
        verticalShift,
        if (isSneaking) 553648127 else -1
      )
    }

    GlStateManager.enableLighting()
    GlStateManager.disableBlend()
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F)
    GlStateManager.popMatrix()

    GL20.glUseProgram(current)
  }

  data class ColorMapping(
    val stencilValue: Int,
    private val outColor: Colour,
    private val colorBlindOutColor: Colour? = null
  ) {
    val color: Colour
      get() = if (!LiteModShadersOutlineFix.mod.colorBlindMode || this.colorBlindOutColor == null) {
        this.outColor
      } else {
        this.colorBlindOutColor
      }
  }

  // TODO - add all the colors that we need for glowing, check glow color, whatever
//  maybe change mythic color to make it MORE orange to be easier to see haha
  private val colours = listOf(
    ColorMapping(TextFormatting.DARK_RED.ordinal, Colour.hex(0xFFAA0000)),
    ColorMapping(TextFormatting.RED.ordinal, Colour.hex(0xFFFF5555)),
    ColorMapping(TextFormatting.GOLD.ordinal, Colour.hex(0xFFFF8C00)),
    ColorMapping(TextFormatting.YELLOW.ordinal, Colour.hex(0xFFEDE553), Colour.hex(0xFFFF69B4)),
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
    this.nametagFramebuffer.framebufferClear()

    this.framebuffer.framebufferClear()
    this.framebuffer.bindFramebuffer(false)
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

        val flag =
          minecraft.renderManager.shouldRender(entity, camera, x, y, z) || entity.isRidingOrBeingRiddenBy(player!!)

        val shouldRenderSelf = minecraft.gameSettings.thirdPersonView != 0 || sleeping
        if ((entity !== minecraft.renderViewEntity || shouldRenderSelf) && flag) {
          val color = entity.team?.color?.ordinal ?: TextFormatting.WHITE.ordinal
          colorUniform.set(colours.firstOrNull { it.stencilValue == color }?.color ?: colours[15].color)
          minecraft.renderManager.setRenderOutlines(true)
          minecraft.renderManager.renderEntityStatic(entity, partialTicks, false)
          minecraft.renderManager.setRenderOutlines(false)
        }
      }

    // Please resume your normal minecrafting
    RenderHelper.enableStandardItemLighting()
    GlStateManager.enableLighting()
    glUseProgram(0)

    // Re-bind minecraft framebuffer
    glBindFramebuffer(GL_FRAMEBUFFER, currentFb)
  }
}
