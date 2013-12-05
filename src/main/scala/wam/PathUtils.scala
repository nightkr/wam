package wam

import java.nio.file.Path

class PathUtils(val path: Path) extends AnyVal {
  /** Convenience alias for [[java.nio.file.Path]].resolve */
  def /(subpath: String): Path = path.resolve(subpath)

  /**
   * Requivalent to /, except for case is matched
   *
   * @example file("a") // "b" would return the same as file("b") / "B" IF the directory "a" contains something named "B" but not "b"
   */
  def ~/(subpath: String)(implicit files: Files): Path = {
    val subpathPath = path / subpath
    val children = files.directoryChildren(path)
    val candidates = children.map(c => subpathPath +: c.filter(_.getFileName.toString.equalsIgnoreCase(subpath)))
    val choice = candidates.toOption.flatMap(_.find(files.exists(_)))
    choice.getOrElse(subpathPath)
  }

  def isAncestorOf(other: Path): Boolean = path.toAbsolutePath.startsWith(other.toAbsolutePath)
}
