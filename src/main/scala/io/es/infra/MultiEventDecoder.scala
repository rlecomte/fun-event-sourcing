package io.es.infra

import cats.effect.Sync
import io.es.infra.data.{Aggregate, Event}
import shapeless.{:+:, CNil, Coproduct}

object MultiEventDecoder {

  implicit def multiDecoderCNil[F[_], P](implicit syncF: Sync[F]): MultiEventDecoder[F, CNil, P] = { rawEvent => syncF.raiseError(UnprocessableEventException(rawEvent.aggregateType))}

  implicit def multiDecoderCoproduct[F[_], H <: Aggregate, T <: Coproduct, P, E <: Event]
    (implicit syncF: Sync[F],
      aggregateTag: AggregateTag.Aux[H, _, E],
      decoderH: EventDecoder[E, P],
      multiDecoderT: MultiEventDecoder[F, T, P]): MultiEventDecoder[F, H :+: T, P] = { rawEvent =>
    if (decoderH.isDefinedAt(rawEvent)) syncF.delay(decoderH(rawEvent))
    else multiDecoderT(rawEvent)
  }

  def apply[F[_], C <: Coproduct, P](implicit syncF: Sync[F], multiDecoder: MultiEventDecoder[F, C, P]): MultiEventDecoder[F, C, P] = multiDecoder
}
