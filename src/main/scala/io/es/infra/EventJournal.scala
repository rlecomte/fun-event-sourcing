package io.es.infra

import cats.data.NonEmptyList
import cats.effect.IO
import io.es.infra.data.{Aggregate, AggregateId, Event, Version}

trait EventJournal[P] {
  def write[S <: Aggregate, E <: Event](aggregateId: AggregateId, originatingVersion: Version, events: NonEmptyList[E])
    (implicit aggregate: AggregateTag.Aux[S, _, E], encoder: EventEncoder[E, P]): IO[Unit]

  def hydrate[S <: Aggregate, E <: Event](aggregateId: AggregateId)
    (implicit handler: EventHandler[S, E], aggregate: AggregateTag.Aux[S, _, E], decoder: EventDecoder[E, P]): IO[Option[(S, Version)]]
}