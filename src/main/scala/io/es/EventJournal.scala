package io.es

import io.es.data.{Aggregate, Event}

abstract class EventJournal[F[_], STATE <: Aggregate, EVENT <: Event] {
  def write(events: Seq[EVENT]): F[Unit]

  def hydrate(id: String): F[Option[STATE]]
}