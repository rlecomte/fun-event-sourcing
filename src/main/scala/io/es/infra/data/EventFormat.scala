package io.es.infra.data

import cats.effect.Sync
import io.circe.Json

trait EventFormat[E] {
  def encode(event: E): Json

  def decode[F[_]: Sync](payload: RawEvent): F[E]
}
