package io.es.infra.data

import io.circe.Json

trait EventFormat[E] {
  def encode(event: E): Json

  def decode(payload: RawEvent): Either[String, E]
}
