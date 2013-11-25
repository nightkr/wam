package wam

import java.nio.file.{Files => NioFiles, FileVisitor, FileVisitOption, FileVisitResult, Path}
import java.nio.file.attribute.BasicFileAttributes
import java.io.IOException
import scala.collection.JavaConversions._


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
}
