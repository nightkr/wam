import scala.language.implicitConversions
import java.io.{InputStream, File}

package object wam {
  def file(path: String): File = new File(path)

  implicit def file2fileutils(file: File): FileUtils = new FileUtils(file)

  implicit def file2fileutils(is: InputStream): InputStreamUtils = new InputStreamUtils(is)

  implicit def comparable2comparableutils[A](comparable: Comparable[A]): ComparableUtils[A] = new ComparableUtils[A](comparable)
}
