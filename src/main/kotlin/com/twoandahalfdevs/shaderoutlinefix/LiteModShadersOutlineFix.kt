package com.twoandahalfdevs.shaderoutlinefix

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mumfrey.liteloader.Configurable
import com.mumfrey.liteloader.LiteMod
import com.mumfrey.liteloader.modconfig.ConfigPanel
import com.mumfrey.liteloader.modconfig.ConfigStrategy
import com.mumfrey.liteloader.modconfig.ExposableOptions
import java.io.File

@ExposableOptions(
  strategy = ConfigStrategy.Unversioned,
  filename = "shader_outline_fix.json",
  aggressive = true
)
class LiteModShadersOutlineFix : LiteMod, Configurable {
  @Expose
  @SerializedName("border_size")
  var borderSize = 15f

  @Expose
  @SerializedName("color_blind_mode")
  var colorBlindMode = false

  @Expose
  @SerializedName("nametag_fix")
  var nametagFix = false

  companion object {
    lateinit var mod: LiteModShadersOutlineFix
  }

  override fun upgradeSettings(v: String?, c: File?, o: File?) {}

  override fun getName(): String = "Shader Outline Fix"
  override fun getVersion(): String = "1.0"

  override fun init(configPath: File?) {
    mod = this

    Outline // load it up

    ResizeWindowEvent(w, h).call()
  }

  override fun getConfigPanelClass(): Class<out ConfigPanel> {
    return ShaderOutlineFixPanel::class.java
  }
}
