package io.es.infra

import cats.effect.IO
import io.es.infra.data.{Aggregate, Event, RawEvent}
import shapeless.{::, HList, HNil, Lazy}

trait MultiEventDecoder[L <: HList, P] extends (RawEvent[P] => IO[Event]) {
  val map: Map[String, EventDecoder[Event, P]]
}

object MultiEventDecoder {

  implicit def multiDecoderHNil[P]: MultiEventDecoder[HNil, P] = new MultiEventDecoder[HNil, P] {

    val map: Map[String, EventDecoder[Event, P]] = Map()

    override def apply(rawEvent: RawEvent[P]) = {
      IO.raiseError[Event](UnprocessableEventException(rawEvent.aggregateType))
    }
  }

  implicit def multiDecoderHList[H <: Aggregate, T <: HList, E <: Event, P]
  (implicit aggregateTag: AggregateTag[H, _, E],
    decoderH: Lazy[EventDecoder[E, P]],
    multiDecoderT: MultiEventDecoder[T, P]): MultiEventDecoder[H :: T, P] = new MultiEventDecoder[H :: T, P] {

    val map: Map[String, EventDecoder[Event, P]] = multiDecoderT.map.updated(aggregateTag.aggregateType, decoderH.value)

    override def apply(rawEvent: RawEvent[P]) = {
      map.get(aggregateTag.aggregateType)
        .map(d => IO(d(rawEvent)))
        .getOrElse(IO.raiseError[Event](UnprocessableEventException(rawEvent.aggregateType)))
    }
  }

  def instance[L <: HList, P](implicit multiDecoder: MultiEventDecoder[L, P]): MultiEventDecoder[L, P] = multiDecoder
}
