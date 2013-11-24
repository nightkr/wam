package wam

import java.io.IOException
import java.nio.file.{Files, FileVisitResult, Path, SimpleFileVisitor}
import java.nio.file.attribute.BasicFileAttributes
import scala.util.Try
import scala.collection.JavaConversions._

class PathUtils(val path: Path) extends AnyVal {
  /** Convenience alias for [[java.nio.file.Path]].resolve */
  def /(subpath: String): Path = path.resolve(subpath)

  /**
   * Requivalent to /, except for case is matched
   *
   * @example file("a") // "b" would return the same as file("b") / "B" IF the directory "a" contains something named "B" but not "b"
   */
  def ~/(subpath: String): Path = {
    val subpathPath = path / subpath
    val children = Try(Files.newDirectoryStream(path)).map(_.toSeq).getOrElse(Seq())
    val candidates = subpathPath +: children.filter(_.getFileName.toString.equalsIgnoreCase(subpath))
    val choice = candidates.find(Files.exists(_))
    choice.getOrElse(subpathPath)
  }

  def isAncestorOf(other: Path): Boolean = path.toAbsolutePath.startsWith(other.toAbsolutePath)
}

object WamFiles {
  def deleteTree(path: Path) {
    Files.walkFileTree(path, new SimpleFileVisitor[Path] {
      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        Files.delete(file)
        FileVisitResult.CONTINUE
      }

      override def postVisitDirectory(dir: Path, e: IOException): FileVisitResult = {
        Option(e).foreach(throw _)

        Files.delete(dir)
        FileVisitResult.CONTINUE
      }
    })
  }

  /**
   * Alias for newDirectoryStream, but converted to a Scala collection
   */
  def directoryChildren(path: Path): Seq[Path] = Files.newDirectoryStream(path).toSeq
}
