package wam

import org.scalautils.Constraint
import org.scalautils.ConversionCheckedTripleEquals._

trait Compare[A] {
  def compare(left: A, right: A): Compare.Result
}

object Compare {

  sealed trait Result

  case object LessThan extends Result

  case object MoreThan extends Result

  case object Equal extends Result

}

class ComparableCompare[A <: Comparable[A]] extends Compare[A] {
  def compare(left: A, right: A): Compare.Result = left.compareTo(right) match {
    case -1 => Compare.LessThan
    case 1 => Compare.MoreThan
    case 0 => Compare.Equal
  }
}

/** Provides some sugar around [[wam.Compare]]. */
class ComparableUtils[A: Compare](val x: A) {
  def <(y: A) = implicitly[Compare[A]].compare(x, y) === Compare.LessThan

  def >(y: A) = implicitly[Compare[A]].compare(x, y) === Compare.MoreThan

  def <=(y: A) = implicitly[Compare[A]].compare(x, y) !== Compare.MoreThan

  def >=(y: A) = implicitly[Compare[A]].compare(x, y) !== Compare.LessThan
}

class ComparableEquality[A: Compare, B <: A] extends Constraint[A, B] {
  def areEqual(left: A, right: B): Boolean = implicitly[Compare[A]].compare(left, right) === Compare.Equal
}