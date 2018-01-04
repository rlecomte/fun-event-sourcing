package io.es

import java.time.ZonedDateTime

import cats.data.ReaderWriterStateT
import io.es.infra.data.{AggregateId, Event, RawEvent, Version}
import shapeless.Coproduct

package object infra {

  type Result[A] = Either[String, A]

  type Sourcing[STATE, EVENT, A] = ReaderWriterStateT[Result, Unit, List[EVENT], STATE, A]

  type EventHandler[STATE, EVENT] = (Option[STATE], EVENT) => STATE

  object EventHandler {
    def apply[STATE, EVENT](eventHandler: EventHandler[STATE, EVENT]): EventHandler[STATE, EVENT] = eventHandler
  }

  type EventDecoder[E <: Event, P] = PartialFunction[RawEvent[P], E]

  type EventEncoder[E <: Event, P] = (AggregateId, Version, ZonedDateTime, E) => RawEvent[P]

  type MultiEventDecoder[F[_], C <: Coproduct, P] = RawEvent[P] => F[Event]
}
