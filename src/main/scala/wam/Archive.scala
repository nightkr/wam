package wam

import java.io.BufferedInputStream
import java.util.zip.ZipFile
import scala.collection.JavaConversions._
import resource._
import java.nio.file.{Files, Path}

trait Archive {
  def modules: Set[Module]

  /**
   * Extract the contained files to the specified directory.
   */
  def extract(target: Path)

  def exists: Boolean
}

case class ZipArchive(path: Path) extends Archive {
  override def modules: Set[Module] = (for {
    zipFile <- managed(new ZipFile(path.toFile)).map(Seq(_)).toTraversable
    entry <- zipFile.entries.toSeq
    modName = entry.getName.takeWhile(_ != '/')
  } yield Module(modName)).toSet

  override def extract(target: Path) {
    for {
      zipFile <- managed(new ZipFile(path.toFile))
      entry <- zipFile.entries().toSeq
      inStream <- managed(zipFile.getInputStream(entry))
      bufInStream <- managed(new BufferedInputStream(inStream))

      fileTarget = target / entry.getName
    } {
      Files.createDirectories(fileTarget.getParent)
      Files.copy(bufInStream, fileTarget)
    }
  }

  override def exists: Boolean = Files.exists(path)
}