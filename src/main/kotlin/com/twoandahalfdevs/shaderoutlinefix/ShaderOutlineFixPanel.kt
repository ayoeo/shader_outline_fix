package com.twoandahalfdevs.shaderoutlinefix

import com.mumfrey.liteloader.core.LiteLoader
import com.mumfrey.liteloader.modconfig.AbstractConfigPanel
import com.mumfrey.liteloader.modconfig.ConfigPanelHost
import net.minecraft.client.gui.GuiButton

class ShaderOutlineFixPanel : AbstractConfigPanel() {
  override fun getPanelTitle() = "Shader Fix Config"

  override fun onPanelHidden() {
    LiteLoader.getInstance().writeConfig(LiteModShadersOutlineFix.mod)

    this.borderSize.text.toFloatOrNull()?.let {
      LiteModShadersOutlineFix.mod.borderSize = it
    }
  }

  private lateinit var borderSize: ConfigTextField

  override fun addOptions(host: ConfigPanelHost) {
    this.addControl(
      GuiButton(
        1,
        0,
        0,
        "Color Blind Mode: ${LiteModShadersOutlineFix.mod.colorBlindMode}"
      )
    ) {
      LiteModShadersOutlineFix.mod.colorBlindMode = !LiteModShadersOutlineFix.mod.colorBlindMode
      it.displayString = "Color Blind Mode: ${LiteModShadersOutlineFix.mod.colorBlindMode}"
    }

    this.addControl(
      GuiButton(
        1,
        0,
        25,
        "Nametag Fix: ${LiteModShadersOutlineFix.mod.nametagFix}"
      )
    ) {
      LiteModShadersOutlineFix.mod.nametagFix = !LiteModShadersOutlineFix.mod.nametagFix
      it.displayString = "Nametag Fix: ${LiteModShadersOutlineFix.mod.nametagFix}"
    }

    this.addLabel(0, 0, 70, 0, 0, 0xFFFFFF, "Border Size")
    this.borderSize =
      this.addTextField(0, mc.fontRenderer.getStringWidth("Border Size") + 10, 60, 40, 20)
    this.borderSize.text = LiteModShadersOutlineFix.mod.borderSize.toString()
  }
}
