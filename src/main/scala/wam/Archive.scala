package wam

import java.io.BufferedInputStream
import java.util.zip.ZipFile
import scala.collection.JavaConversions._
import resource._
import java.nio.file.Path

trait Archive {
  def modules(implicit ctx: WamCtx): Set[Module]

  /**
   * Extract the contained files to the specified directory.
   */
  def extract(target: Path)(implicit ctx: WamCtx)

  def exists(implicit ctx: WamCtx): Boolean
}

case class ZipArchive(path: Path) extends Archive {
  override def modules(implicit ctx: WamCtx): Set[Module] = (for {
    zipFile <- managed(new ZipFile(path.toFile)).map(Seq(_)).toTraversable
    entry <- zipFile.entries.toSeq
    modName = entry.getName.takeWhile(_ != '/')
  } yield Module(modName)).toSet

  override def extract(target: Path)(implicit ctx: WamCtx) {
    for {
      zipFile <- managed(new ZipFile(path.toFile))
      entry <- zipFile.entries().toSeq
      inStream <- managed(zipFile.getInputStream(entry))
      bufInStream <- managed(new BufferedInputStream(inStream))

      fileTarget = target / entry.getName
    } {
      ctx.files.createDirectories(fileTarget.getParent)
      ctx.files.write(bufInStream, fileTarget)
    }
  }

  override def exists(implicit ctx: WamCtx): Boolean = ctx.files.exists(path)
}