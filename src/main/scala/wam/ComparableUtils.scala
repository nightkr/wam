package wam

/** Provides some sugar around [[java.lang.Comparable]]. */
class ComparableUtils[A](val c: Comparable[A]) extends AnyVal {
  def <(x: A) = (c compareTo x) < 0

  def >(x: A) = (c compareTo x) > 0

  def ===(x: A) = (c compareTo x) == 0

  def <=(x: A) = this === x || this < x

  def >=(x: A) = this === x || this > x
}
