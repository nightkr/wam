package wam

import java.io.{BufferedOutputStream, FileOutputStream, BufferedInputStream, File}
import java.util.zip.ZipFile
import scala.collection.JavaConversions._
import resource._

trait Archive {
  def modules: Set[Module]

  /**
   * Extract the contained files to the specified directory.
   */
  def extract(target: File)

  def exists: Boolean
}

case class ZipArchive(file: File) extends Archive {
  override def modules: Set[Module] = (for {
    zipFile <- managed(new ZipFile(file)).map(Seq(_)).toTraversable
    entry <- zipFile.entries.toSeq
    modName = entry.getName.takeWhile(_ != '/')
  } yield Module(modName)).toSet

  override def extract(target: File) {
    for {
      zipFile <- managed(new ZipFile(file))
      entry <- zipFile.entries().toSeq
      inStream <- managed(zipFile.getInputStream(entry))
      bufInStream <- managed(new BufferedInputStream(inStream))

      targetFile = target / entry.getName
      _ = targetFile.mkparents()
      outStream <- managed(new FileOutputStream(targetFile))
      bufOutStream <- managed(new BufferedOutputStream(outStream))
    } {
      bufInStream.copyTo(bufOutStream)
    }
  }

  override def exists: Boolean = file.exists
}