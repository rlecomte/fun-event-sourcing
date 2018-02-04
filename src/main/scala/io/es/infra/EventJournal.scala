package io.es.infra

import cats.data.NonEmptyList
import cats.effect.IO
import io.es.infra.data._

trait EventJournal[P] {
  def write[E <: Event](encoder: EventEncoder[E, P], decoder: EventDecoder[E, P], aggregateTag: String)
    (aggregateId: AggregateId, originatingVersion: Version, events: NonEmptyList[E]): IO[Unit]

  def hydrate[A <: Aggregate, E <: Event](decoder: EventDecoder[E, P], handler: EventHandler[A, E], aggregateId: AggregateId): IO[Option[(A, Version)]]
}