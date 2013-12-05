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

  def exists(path: Path, options: LinkOption*): Boolean = NioFiles.exists(path, options: _*)

  def isDirectory(path: Path, options: LinkOption*): Boolean = NioFiles.isDirectory(path, options: _*)

  def isFile(path: Path, options: LinkOption*): Boolean = NioFiles.isRegularFile(path, options: _*)

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
    case Some(dir) => NioFiles.createTempDirectory(dir, prefix.getOrElse(null), attrs: _*)
  }

  def copy(in: Path, out: Path, options: CopyOption*) {
    NioFiles.copy(in, out, options: _*)
  }

  def copy(in: InputStream, out: Path, options: CopyOption*) {
    NioFiles.copy(in, out, options: _*)
  }

  def copy(in: Path, out: OutputStream) {
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
