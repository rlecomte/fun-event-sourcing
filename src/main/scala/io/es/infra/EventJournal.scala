package io.es.infra

import io.es.UUID
import io.es.infra.data.Event

abstract class EventJournal[F[_], P] {
  def write[EVENT](aggregateId: UUID, originatingVersion: Long, events: List[EVENT])
    (implicit event: Event[EVENT, P]): F[Unit]

  def hydrate[STATE, EVENT](aggregateId: UUID)
    (implicit handler: EventHandler[STATE, EVENT], event: Event[EVENT, P]): F[Option[(STATE, Long)]]
}