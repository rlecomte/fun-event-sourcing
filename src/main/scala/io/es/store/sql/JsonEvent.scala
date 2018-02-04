package io.es.store.sql

import io.circe.{Decoder, Encoder, Json}
import io.es.infra.{EventDecoder, EventEncoder}
import io.es.infra.data._

object JsonEvent {
  def decoder[E <: Event](implicit decoder: Decoder[E]): EventDecoder[E, Json] = {
    event => decoder.decodeJson(event.data).right.get
  }

  def encoder[E <: Event](implicit encoder: Encoder[E]): EventEncoder[E, Json] = (aggId, tag, version, date, event) => {
    RawEvent(aggId, version, SequenceNumber(-1L), encoder(event), date, tag)
  }
}
