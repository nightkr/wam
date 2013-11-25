import java.nio.file.{Paths, Path}
import org.scalautils.Constraint
import scala.language.implicitConversions

package object wam {
  def path(path: String): Path = Paths.get(path)

  implicit def path2pathutils(path: Path): PathUtils = new PathUtils(path)

  implicit def compare2compareUtils[A: Compare](comparable: A): ComparableUtils[A] = new ComparableUtils[A](comparable)

  implicit def compareEquality[A: Compare, B <: A]: Constraint[A, B] = new ComparableEquality[A, B]

  implicit def comparable2compare[A <: Comparable[A]]: Compare[A] = new ComparableCompare[A]
}
