package wam

import java.nio.file.{Files => NioFiles, _}
import java.nio.file.attribute.{FileAttribute, BasicFileAttributes}
import java.io.{OutputStream, InputStream, IOException}
import scala.collection.JavaConversions._
import scala.Some
import scala.util.Try


/**
 * Provides wrappers for [[java.nio.file.Files]], and some other utility methods
 */
trait Files {

  import Files.{SymlinkBehavior => SlB}
  import Files.SymlinkBehavior.{Follow => SlBFollow}

  def walkFileTree(start: Path,
                   node: (Path, BasicFileAttributes) => FileVisitResult = (_, _) => FileVisitResult.CONTINUE,
                   nodeFailed: (Path, IOException) => FileVisitResult = (_, e) => throw e,
                   preDirectory: (Path, BasicFileAttributes) => FileVisitResult = (_, _) => FileVisitResult.CONTINUE,
                   postDirectory: (Path, Option[IOException]) => FileVisitResult = {
                     case (_, Some(e)) => throw e
                     case (_, None) => FileVisitResult.CONTINUE
                   },
                   options: Set[FileVisitOption] = Set(),
                   maxDepth: Int = Int.MaxValue): Path =
    NioFiles.walkFileTree(start, options, maxDepth, new FileVisitor[Path] {
      def preVisitDirectory(path: Path, attrs: BasicFileAttributes): FileVisitResult = preDirectory(path, attrs)

      def visitFile(path: Path, attrs: BasicFileAttributes): FileVisitResult = node(path, attrs)

      def visitFileFailed(path: Path, e: IOException): FileVisitResult = nodeFailed(path, e)

      def postVisitDirectory(path: Path, e: IOException): FileVisitResult = postDirectory(path, Option(e))
    })

  def exists(path: Path, options: SlB = SlBFollow): Boolean = NioFiles.exists(path, options.toLinkOptions: _*)

  def isDirectory(path: Path, options: SlB = SlBFollow): Boolean = NioFiles.isDirectory(path, options.toLinkOptions: _*)

  def isFile(path: Path, options: SlB = SlBFollow): Boolean = NioFiles.isRegularFile(path, options.toLinkOptions: _*)

  def createDirectories(path: Path) {
    NioFiles.createDirectories(path)
  }

  def createDirectory(path: Path) {
    NioFiles.createDirectory(path)
  }

  def createFile(path: Path, attribs: FileAttribute[_]*) {
    NioFiles.createFile(path, attribs: _*)
  }

  def createTempDirectory(dir: Option[Path] = None, prefix: Option[String] = None, attrs: Seq[FileAttribute[_]] = Seq()) = dir match {
    case None => NioFiles.createTempDirectory(prefix.getOrElse(null), attrs: _*)
    case Some(definiteDir) => NioFiles.createTempDirectory(definiteDir, prefix.getOrElse(null), attrs: _*)
  }

  def createSymlink(from: Path, to: Path) {
    NioFiles.createSymbolicLink(from, to)
  }

  def copy(in: Path, out: Path, options: SlB = SlBFollow) {
    NioFiles.copy(in, out, options.toLinkOptions: _*)
  }

  def write(in: InputStream, out: Path, options: SlB = SlBFollow) {
    NioFiles.copy(in, out, options.toLinkOptions: _*)
  }

  def read(in: Path, out: OutputStream) {
    NioFiles.copy(in, out)
  }

  /**
   * Alias for newDirectoryStream, but converted to a Scala collection
   */
  def directoryChildren(path: Path): Try[Seq[Path]] = Try(NioFiles.newDirectoryStream(path).toSeq)

  def delete(path: Path, onlyIfExists: Boolean = false) {
    if (onlyIfExists)
      NioFiles.deleteIfExists(path)
    else
      NioFiles.delete(path)
  }

  def deleteTree(path: Path) {
    walkFileTree(path,
      node = {
        (file, attrs) =>
          delete(file)
          FileVisitResult.CONTINUE
      },
      postDirectory = {
        (dir, e) =>
          e.foreach(throw _)

          delete(dir)
          FileVisitResult.CONTINUE
      }
    )
  }
}

object Files {

  sealed trait SymlinkBehavior {
    def toLinkOptions: Seq[LinkOption]
  }

  object SymlinkBehavior {

    case object Ignore extends SymlinkBehavior {
      override def toLinkOptions = Seq(LinkOption.NOFOLLOW_LINKS)
    }

    case object Follow extends SymlinkBehavior {
      override def toLinkOptions = Seq()
    }

  }

}