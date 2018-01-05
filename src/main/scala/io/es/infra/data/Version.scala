package io.es.infra.data

case class Version(value: Long) extends AnyVal {

  def inc: Version = Version(value + 1L)

  def >(v: Long): Boolean = value > v
}

object Version {
  val origin = Version(0L)
}