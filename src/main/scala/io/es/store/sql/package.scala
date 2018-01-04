package io.es.store

import io.circe.Json
import io.es.infra.{EventDecoder, EventEncoder}
import io.es.infra.data.Event

package object sql {

  type JsonEventDecoder[E <: Event] = EventDecoder[E, Json]

  type JsonEventEncoder[E <: Event] = EventEncoder[E, Json]
}
