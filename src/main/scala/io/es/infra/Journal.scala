package io.es.infra

import io.circe.Json
import io.es.UUID
import io.es.infra.data.RawEvent

trait Journal[F[_]] {
  def hydrate(id: UUID): fs2.Stream[F, RawEvent]

  def register(key: String, id: UUID, payload: Seq[Json]): F[Unit]
}