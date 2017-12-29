package io.es.infra

import io.es.infra.data.{Event, RawEvent}

object EventDecoder {
  type EventDecoder[E <: Event, P] = PartialFunction[RawEvent[P], E]
}
