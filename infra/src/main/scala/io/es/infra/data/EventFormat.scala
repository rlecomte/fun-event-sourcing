package io.es.infra.data

import io.circe.Json

/**
  * This is an typeclass to read and write event of type E from/to the event store.
  * @tparam E
  */
trait EventFormat[E] {
  def encode(event: E): Json

  def decode(payload: RawEvent): Either[String, E]
}
