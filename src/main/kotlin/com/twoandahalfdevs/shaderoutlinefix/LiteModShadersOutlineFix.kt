package com.twoandahalfdevs.shaderoutlinefix

import com.mumfrey.liteloader.*
import com.mumfrey.liteloader.modconfig.ConfigStrategy
import com.mumfrey.liteloader.modconfig.ExposableOptions
import java.io.File

@ExposableOptions(
  strategy = ConfigStrategy.Unversioned,
  filename = "shader_outline_fix.json",
  aggressive = true
)
class LiteModShadersOutlineFix : LiteMod {
  override fun upgradeSettings(v: String?, c: File?, o: File?) {}

  override fun getName(): String = "Shader Outline Fix"
  override fun getVersion(): String = "1.0"

  override fun init(configPath: File?) {
    Outline // load it up

    ResizeWindowEvent(w, h).call()
  }
}
