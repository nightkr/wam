package wam

import java.io.IOException
import java.nio.file.{Files, Path}
import scala.util.Try
import org.scalautils.ConversionCheckedTripleEquals._

/**
 * A Bundle corresponds to a versioned and named add-on, containing one or more [[wam.Module m o d u l e s]].
 */
trait Bundle {
  def name: String

  def version: Version

  def modules(implicit ctx: WamCtx): Set[Module] = installedModules

  def installedModules(implicit ctx: WamCtx): Set[Module] = for {
    files <- Try(WamFiles.directoryChildren(registryPath)).toOption.toSet[Seq[Path]]
    subDir <- files.toSet
    if Files.isDirectory(subDir)
    name = subDir.getFileName.toString
    if !name.startsWith(".")
  } yield Module(name)

  /** The path to the bundle in the repository, regardless of whether it is installed. */
  def registryPath(implicit ctx: WamCtx): Path = ctx.repository / name / version.str

  /** Returns true if this Bundle is contained within the Wam repository (see [[wam.WamCtx]]). */
  def installed(implicit ctx: WamCtx): Boolean = Files.exists(registryPath)

  /**
   * Returns true if this Bundle can be installed (there is something backing it).
   *
   * @note An installed bundle is always backed by something (the repository) and is as such always considered installable.
   */
  def installable(implicit ctx: WamCtx): Boolean = installed

  /** Returns true if any module in this Bundle is enabled. */
  def enabled(implicit ctx: WamCtx): Boolean = modules.size > 0 && modules.exists(_.enabled)

  /** Returns true if all modules in this Bundle are enabled. */
  def fullyEnabled(implicit ctx: WamCtx): Boolean = modules.size > 0 && modules.forall(_.enabled)

  /**
   * Installs this Bundle into the repository.
   * @throws Bundle.NotInstallable if this Bundle is not installable (backed by nothing). Use installable to check for this.
   */
  def install()(implicit ctx: WamCtx) {
    if (!installed) {
      throw new Bundle.NotInstallable(this)
    }
  }

  /** Uninstalls this Bundle from the repository. */
  def uninstall()(implicit ctx: WamCtx) {
    if (installed) {
      WamFiles.deleteTree(registryPath)
    }
  }

  /**
   * Enables all Modules in this Bundle.
   * @param force Whether or not to overwrite modules which come from other bundles or are not managed by Wam
   * @throws Bundle.NotInstalled If this Bundle is not already installed.
   */
  def enable(force: Boolean = false)(implicit ctx: WamCtx) = {
    ???
  } // modules.foreach(_.enable())

  /**
   * Disables all Modules in this Bundle.
   * @param force Whether or not to also disable modules which are
   */
  def disable(force: Boolean = false)(implicit ctx: WamCtx) = {
    ???
  }
}

object Bundle {
  /**
   * Returns a reference to a [[wam.Bundle]] with the given name and version.
   * @note This Bundle may not be installed in the repository. See .get for a version that only returns a value if it exists.
   */
  def apply(name: String, version: Version): Bundle = BundleRef(name, version)

  /**
   * Returns a reference to a [[wam.Bundle]] with the given name and version if it is installed.
   */
  def get(name: String, version: Version)(implicit ctx: WamCtx): Option[Bundle] = Some(apply(name, version)).filter(_.installed)

  /**
   * Returns the latest installed bundle of a given name, if any.
   */
  def latest(name: String): Option[Bundle] = ???

  /**
   * A reference to a bundle which may or may not exist.
   */
  case class BundleRef(name: String, version: Version) extends Bundle

  /**
   * A reference to a bundle backed by an archive.
   * @param src The source archive the bundle can be installed from.
   */
  case class BundleArchive(name: String, version: Version, src: Archive) extends Bundle {
    override def modules(implicit ctx: WamCtx) = super.modules ++ src.modules

    override def installable(implicit ctx: WamCtx) = src.exists

    override def install()(implicit ctx: WamCtx) {
      if (!installed) {
        var success = false // Would use try/catch, but that messes up the stack traces when we rethrow them
        try {
          Files.createDirectories(registryPath)
          src.extract(registryPath)
          success = true
        } finally {
          if (!success) {
            try {
              uninstall()
            } catch {
              case _: IOException =>
            }
          }
        }
      }
    }
  }

  class NotInstallable(val bundle: Bundle) extends Exception(bundle.toString)

  class NotInstalled(val bundle: Bundle) extends Exception(bundle.toString)

}

case class Version(str: String) extends Comparable[Version] {
  override def compareTo(x: Version) = ???
}

/**
 * A Module corresponds to a single add-on from the game's perspective.
 *
 * For example, TellMeWhen is split up into TellMeWhen and TellMeWhen_Options, which the
 * game treats as separate add-ons, but the user treats as part of a cohesive whole.
 * In this case, TellMeWhen would be the Package, containing TellMeWhen and TellMeWhen_Options as separate modules.
 */
case class Module(name: String) {
  /** Returns the path to the module in the WoW Add-Ons folder. */
  def enablePath(implicit ctx: WamCtx): Path = ctx.enabledAddOnsDir / name

  /** Returns the path to the module in the repository. */
  def registryPath(bundle: Bundle)(implicit ctx: WamCtx): Path = bundle.registryPath / name

  /** Returns true if a module by this name is enabled. */
  def enabled(implicit ctx: WamCtx): Boolean = Files.exists(enablePath)

  /**
   * Returns true if a module by this name is enabled and managed by Wam.
   *
   * A module is considered managed by Wam if it is a symlink to a directory inside the Wam repository.
   */
  def managed(implicit ctx: WamCtx): Boolean = enabled && ctx.repository.isAncestorOf(enablePath.toRealPath())

  /** Returns true if a module by this name is enabled and managed by the specified bundle. */
  def managedBy(bundle: Bundle)(implicit ctx: WamCtx): Boolean = enabled && (enablePath.toRealPath() === registryPath(bundle))
}
