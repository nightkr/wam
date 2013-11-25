package wam

import java.nio.file.{Files => NioFiles, _}
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
      def preVisitDirectory(p1: Path, p2: BasicFileAttributes): FileVisitResult = preDirectory(p1, p2)

      def visitFile(p1: Path, p2: BasicFileAttributes): FileVisitResult = node(p1, p2)

      def visitFileFailed(p1: Path, p2: IOException): FileVisitResult = ???

      def postVisitDirectory(p1: Path, p2: IOException): FileVisitResult = ???
    })
}
