package io.es.store.sql

import io.circe.{Decoder, Encoder, Json}
import io.es.infra.data.Event

object JsonEvent {

  type JsonEvent[E] = Event[E, Json]

  def fromCirceInstances[EVENT](aggType: String)
    (implicit decoder: Decoder[EVENT], encoder: Encoder[EVENT]): JsonEvent[EVENT] = new Event[EVENT, Json] {
    override def toStore(e: EVENT): Json = encoder(e)

    override def fromStore(payload: Json): EVENT = decoder(payload.hcursor).right.get

    override def aggregateType: String = aggType
  }
}
