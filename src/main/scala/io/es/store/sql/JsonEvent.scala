package io.es.store.sql

import io.circe.{Decoder, Encoder, Json}
import io.es.infra.AggregateTag
import io.es.infra.EventDecoder.EventDecoder
import io.es.infra.EventEncoder.EventEncoder
import io.es.infra.data.{Aggregate, Event, RawEvent}

object JsonEvent {

  type JsonEventDecoder[E <: Event] = EventDecoder[E, Json]

  type JsonEventEncoder[E <: Event] = EventEncoder[E, Json]

  trait JsonEncoderDecoderBuilder[E <: Event] {
    def eventDecoder[T <: String, A <: Aggregate](implicit agg: AggregateTag.Aux[A, _, E], decoder: Decoder[E]): EventDecoder[E, Json] = new PartialFunction[RawEvent[Json], E] {
      override def isDefinedAt(event: RawEvent[Json]) = agg.aggregateType == event.aggregateType

      override def apply(event: RawEvent[Json]) = decoder.decodeJson(event.data).right.get
    }

    def eventEncoder[T <: String, A <: Aggregate](implicit agg: AggregateTag.Aux[A, _, E], encoder: Encoder[E]): EventEncoder[E, Json] = (aggId, version, date, event) => {
      RawEvent(aggId.value, version.value, encoder(event), date.toInstant.toEpochMilli, agg.aggregateType)
    }
  }

  def apply[E <: Event] = new JsonEncoderDecoderBuilder[E] {}
}
