package io.es.algebra

import io.es.UUID
import io.es.infra.data.{RawEvent, Tag}

trait Journal[F[_]] {
  def update[A](id: UUID, key: Tag)(f: fs2.Stream[F, RawEvent] => F[List[RawEvent]]): F[UUID]

  def create[A](id: UUID, key: Tag)(f: List[RawEvent]): F[UUID]
}

object Journal {
  def apply[F[_]](implicit journal: Journal[F]): Journal[F] = journal
}