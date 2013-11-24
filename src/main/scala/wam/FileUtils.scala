package wam

import java.io.{OutputStream, InputStream, IOException, File}
import java.nio.file.{Files, FileVisitResult, Path, SimpleFileVisitor}
import java.nio.file.attribute.BasicFileAttributes
import scala.util.Try
import scala.collection.JavaConversions._

class FileUtils(val file: File) extends AnyVal {
  /** Returns a File object corresponding to the given path inside this [[java.io.File]]'s path */
  def /(subpath: String): File = new File(file, subpath)

  /** Requivalent to /, except for case is matched
    *
    * @example file("a") // "b" would return the same as file("b") / "B" IF the directory "a" contains something named "B" but not "b"
    */
  def ~/(subpath: String): File = {
    val children = Option(file.list()).map(_.toSeq).getOrElse(Seq())
    val candidates = subpath +: children.filter(_.equalsIgnoreCase(subpath))
    val choice = candidates.map(file./).find(_.exists)
    choice.getOrElse(file / subpath)
  }

  def deleteTree() {
    Files.walkFileTree(file.toPath, new SimpleFileVisitor[Path] {
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

  def getParent = file.toPath.getParent.toFile

  def mkparents() {
    getParent.mkdirs()
  }

  def isAncestorOf(other: File): Boolean = other.getAbsolutePath.startsWith(file.getAbsolutePath + File.separator)
}

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
}

class InputStreamUtils(val is: InputStream) extends AnyVal {
  /**
   * Reads the whole input stream from the currently location, and writes everything read to os
   * @param bufSize The buffer size in bytes
   */
  def copyTo(os: OutputStream, bufSize: Int = 1024) {
    val buf = new Array[Byte](bufSize)
    var read = 0
    do {
      os.write(buf, 0, read)
      read = is.read(buf)
    } while (read != -1)
  }
}