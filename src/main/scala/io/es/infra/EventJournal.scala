package io.es.infra

import cats.effect.IO
import io.es.UUID
import io.es.infra.data.{Aggregate, Event}

abstract class EventJournal[P] {
  def write[S <: Aggregate, E <: Event](aggregateId: UUID, originatingVersion: Long, events: List[E])
    (implicit aggregate: AggregateTag.Aux[S, _, E], encoder: EventEncoder[E, P]): IO[Unit]

  def hydrate[S <: Aggregate, E <: Event](aggregateId: UUID)
    (implicit handler: EventHandler[S, E], aggregate: AggregateTag.Aux[S, _, E], decoder: EventDecoder[E, P]): IO[Option[(S, Long)]]
}