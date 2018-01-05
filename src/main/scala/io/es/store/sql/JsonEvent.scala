package io.es.store.sql

import io.circe.{Decoder, Encoder, Json}
import io.es.infra.{AggregateTag, EventDecoder, EventEncoder}
import io.es.infra.data._

object JsonEvent {

  trait JsonEncoderDecoderBuilder[E <: Event] {
    def eventDecoder[T <: String, A <: Aggregate](implicit agg: AggregateTag.Aux[A, _, E], decoder: Decoder[E]): EventDecoder[E, Json] = new PartialFunction[RawEvent[Json], E] {
      override def isDefinedAt(event: RawEvent[Json]) = agg.aggregateType == event.aggregateType

      override def apply(event: RawEvent[Json]) = decoder.decodeJson(event.data).right.get
    }

    def eventEncoder[T <: String, A <: Aggregate](implicit agg: AggregateTag.Aux[A, _, E], encoder: Encoder[E]): EventEncoder[E, Json] = (aggId, version, date, event) => {
      RawEvent(aggId.value, version, SequenceNumber(-1L), encoder(event), date, agg.aggregateType)
    }
  }

  def apply[E <: Event] = new JsonEncoderDecoderBuilder[E] {}
}
