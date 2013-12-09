package wam

class CondUtils[A](val x: A) extends AnyVal {
  def where(cond: Boolean): Option[A] =
    if (cond) {
      Some(x)
    } else {
      None
    }
}
