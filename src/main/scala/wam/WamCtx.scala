package wam

import java.nio.file.Path

/**
 * Context object containing various info that some Wam operations require.
 * @param wowDir Installation directory of World of Warcraft.
 * @param repository Directory which contains installed bundles.
 */
case class WamCtx(wowDir: Path, repository: Path, files: Files) {
  def enabledAddOnsDir = wowDir ~/ "interface" ~/ "addons"
}
