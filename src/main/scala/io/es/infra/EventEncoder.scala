package io.es.infra

import java.time.ZonedDateTime

import io.es.infra.data.{AggregateId, Event, RawEvent, Version}

object EventEncoder {
  type EventEncoder[E <: Event, P] = (AggregateId, Version, ZonedDateTime, E) => RawEvent[P]
}
