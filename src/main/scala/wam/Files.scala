package wam

import java.nio.file.{Files => NioFiles, FileVisitor, FileVisitOption, FileVisitResult, Path}
import java.nio.file.attribute.BasicFileAttributes
import java.io.IOException


/**
 * Provides wrappers for [[java.nio.file.Files]], and some other utility methods
 */
trait Files {
  def walkFileTree(start: Path,
                   node: (Path, BasicFileAttributes) => FileVisitResult,
                   nodeFailed: (Path, IOException) => FileVisitResult,
                   preDirectory: (Path, BasicFileAttributes) => FileVisitResult,
                   postDirectory: (Path, Option[IOException]) => FileVisitResult,
                   options: Set[FileVisitOption],
                   maxDepth: Int): Path =
    NioFiles.walkFileTree(start, new FileVisitor[Path] {
      def preVisitDirectory(path: Path, attrs: BasicFileAttributes): FileVisitResult = preDirectory(path, attrs)

      def visitFile(path: Path, attrs: BasicFileAttributes): FileVisitResult = node(path, attrs)

      def visitFileFailed(path: Path, e: IOException): FileVisitResult = nodeFailed(path, e)

      def postVisitDirectory(path: Path, e: IOException): FileVisitResult = postDirectory(path, Option(e))
    })
}
