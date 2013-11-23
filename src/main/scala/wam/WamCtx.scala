package wam

import java.io.File

/**
 * Context object containing various info that some Wam operations require.
 * @param wowDir Installation directory of World of Warcraft.
 * @param repository Directory which contains installed bundles.
 */
case class WamCtx(wowDir: File, repository: File) {
  def enabledAddOnsDir = wowDir ~/ "interface" ~/ "addons"
}
