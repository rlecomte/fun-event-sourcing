package io.es.infra.data

import java.util.UUID

trait Aggregate[A, E] {
  def tag: Tag

  def id(aggregate: A): UUID

  def handle(aggregate: Option[A])(event: E): Option[A]
}
