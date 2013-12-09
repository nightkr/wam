package wam

import java.nio.file.{Files => NioFiles, _}
import java.nio.file.attribute.{FileAttribute, BasicFileAttributes}
import java.io.{OutputStream, InputStream, IOException}
import scala.collection.JavaConversions._
import scala.Some
import scala.util.Try
import wam.Files.CopyOptions


/**
 * Provides wrappers for [[java.nio.file.Files]], and some other utility methods
 */
trait Files {

  import Files.SymlinkOptions
  import Files.SymlinkOptions.{Follow => SlFollow}

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

  def exists(path: Path, options: SymlinkOptions = SlFollow): Boolean = NioFiles.exists(path, options.toOptions: _*)

  def isDirectory(path: Path, options: SymlinkOptions = SlFollow): Boolean = NioFiles.isDirectory(path, options.toOptions: _*)

  def isFile(path: Path, options: SymlinkOptions = SlFollow): Boolean = NioFiles.isRegularFile(path, options.toOptions: _*)

  def createDirectories(path: Path) {
    NioFiles.createDirectories(path)
  }

  def createDirectory(path: Path) {
    NioFiles.createDirectory(path)
  }

  def createFile(path: Path, attribs: FileAttribute[_]*) {
    NioFiles.createFile(path, attribs: _*)
  }

  def createTempDirectory(dir: Option[Path] = None, prefix: Option[String] = None, attrs: Seq[FileAttribute[_]] = Seq()): Path = dir match {
    case None => NioFiles.createTempDirectory(prefix.getOrElse(null), attrs: _*)
    case Some(definiteDir) => NioFiles.createTempDirectory(definiteDir, prefix.getOrElse(null), attrs: _*)
  }

  def createTempFile(dir: Option[Path] = None, prefix: Option[String] = None, suffix: Option[String] = None, attrs: Seq[FileAttribute[_]] = Seq()): Path = dir match {
    case None => NioFiles.createTempFile(prefix.getOrElse(null), suffix.getOrElse(null), attrs: _*)
    case Some(definiteDir) => NioFiles.createTempFile(definiteDir, prefix.getOrElse(null), suffix.getOrElse(null), attrs: _*)
  }

  def createSymlink(from: Path, to: Path) {
    NioFiles.createSymbolicLink(from, to)
  }

  def getSymlink(link: Path): Option[Path] = Try(NioFiles.readSymbolicLink(link)).toOption

  def copy(in: Path, out: Path, options: CopyOptions = CopyOptions()) {
    NioFiles.copy(in, out, options.toOptions: _*)
  }

  def write(in: InputStream, out: Path, options: CopyOptions = CopyOptions()) {
    NioFiles.copy(in, out, options.toOptions: _*)
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

  trait ToOptions[A] {
    def toOptions: Seq[A]
  }

  sealed trait SymlinkOption[A >: LinkOption] extends ToOptions[A] {
    def followSymlinks: Boolean

    def toOptions: Seq[A] = LinkOption.NOFOLLOW_LINKS.where(!followSymlinks).toSeq
  }

  type SymlinkOptions = SymlinkOption[LinkOption]

  object SymlinkOptions {

    case object Ignore extends SymlinkOptions {
      override def followSymlinks = false
    }

    case object Follow extends SymlinkOptions {
      override def followSymlinks = true
    }

  }

  case class CopyOptions(atomicMove: Boolean = false,
                         copyAttributes: Boolean = false,
                         replaceExisting: Boolean = false,
                         followSymlinks: Boolean = true) extends SymlinkOption[CopyOption] {
    override def toOptions = super.toOptions ++ Seq(
      StandardCopyOption.ATOMIC_MOVE.where(atomicMove),
      StandardCopyOption.COPY_ATTRIBUTES.where(copyAttributes),
      StandardCopyOption.REPLACE_EXISTING.where(replaceExisting)
    ).flatten
  }

  sealed trait OpenOptions extends SymlinkOption[OpenOption]

  case class ReadOptions(followSymlinks: Boolean = false) extends OpenOptions

  case class WriteOptions(overwrite: Boolean = true,
                          createMode: WriteOptions.CreateMode = WriteOptions.CreateIfMissing,
                          synchronizationMode: WriteOptions.SynchronizationMode = WriteOptions.Asynchronous,
                          followSymlinks: Boolean = true) extends OpenOptions {
    override def toOptions = Seq(
      super.toOptions,
      createMode.toOptions,
      synchronizationMode.toOptions
    ).flatten ++ Seq(
      StandardOpenOption.APPEND.where(!overwrite),
      StandardOpenOption.TRUNCATE_EXISTING.where(overwrite)
    ).flatten
  }

  object WriteOptions {

    import StandardOpenOption._

    sealed trait SynchronizationMode extends ToOptions[OpenOption]

    case object Asynchronous extends SynchronizationMode {
      override def toOptions = Seq()
    }

    case object Synchronous extends SynchronizationMode {
      override def toOptions = Seq(SYNC)
    }

    case object SynchronousContent extends SynchronizationMode {
      override def toOptions = Seq(DSYNC)
    }

    sealed trait CreateMode extends ToOptions[OpenOption]

    case object CreateIfMissing extends CreateMode {
      override def toOptions = Seq(CREATE)
    }

    case object CreateOrFail extends CreateMode {
      override def toOptions = Seq(CREATE_NEW)
    }

    case object NeverCreate extends CreateMode {
      override def toOptions = Seq()
    }

  }

}