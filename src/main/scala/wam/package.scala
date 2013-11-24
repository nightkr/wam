import java.nio.file.{Paths, Path}
import scala.language.implicitConversions
import java.io.InputStream

package object wam {
  def path(path: String): Path = Paths.get(path)

  implicit def path2pathutils(path: Path): PathUtils = new PathUtils(path)

  implicit def inputstream2inputstreamutils(is: InputStream): InputStreamUtils = new InputStreamUtils(is)

  implicit def comparable2comparableutils[A](comparable: Comparable[A]): ComparableUtils[A] = new ComparableUtils[A](comparable)
}
