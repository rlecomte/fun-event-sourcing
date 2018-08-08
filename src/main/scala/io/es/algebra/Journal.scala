package io.es.algebra

import cats.InjectK
import cats.free.Free
import io.circe.Json
import io.es.UUID
import io.es.infra.data.RawEvent

sealed trait JournalOps[A]
case class Hydrate[A](id: java.util.UUID, process: RawEvent => Either[String, A]) extends JournalOps[A]
case class Register[A](key: String, id: UUID, payload: Seq[Json]) extends JournalOps[A]

case class Journal[F[_]](implicit I: InjectK[JournalOps, F]) {

  def hydrate[S](
                  id: java.util.UUID,
                  process: RawEvent => Either[String, S]
                ): Free[F, S] = Free.inject[JournalOps, F](Hydrate(id, process))

  def register[A](key: String, id: UUID, payload: Seq[Json]): Free[F, Unit] = {
    Free.inject[JournalOps, F](Register(key, id, payload))
  }
}