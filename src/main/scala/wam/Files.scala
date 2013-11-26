package wam

import java.nio.file.{Files => NioFiles, _}
import java.nio.file.attribute.BasicFileAttributes
import java.io.{OutputStream, InputStream, IOException}
import scala.collection.JavaConversions._
import scala.Some


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

  def exists(path: Path): Boolean = NioFiles.exists(path)

  def createDirectories(path: Path) {
    NioFiles.createDirectories(path)
  }

  def createDirectory(path: Path) {
    NioFiles.createDirectory(path)
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
}
