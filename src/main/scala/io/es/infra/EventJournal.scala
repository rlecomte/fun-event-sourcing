package io.es.infra

import cats.effect.IO
import io.es.UUID
import io.es.infra.data.Event

abstract class EventJournal[P] {
  def write[EVENT](aggregateId: UUID, originatingVersion: Long, events: List[EVENT])
    (implicit event: Event[EVENT, P]): IO[Unit]

  def hydrate[STATE, EVENT](aggregateId: UUID)
    (implicit handler: EventHandler[STATE, EVENT], event: Event[EVENT, P]): IO[Option[(STATE, Long)]]
}